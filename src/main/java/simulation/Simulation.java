package simulation;

import action.Action;
import action.FeedingAction;
import action.HungerAction;
import action.MovementAction;
import action.ReproductionAction;
import config.ConfigLoader;
import config.SimulationConfig;
import entity.Entity;
import entity.EntityFactory;
import entity.EntityType;
import world.Position;
import world.World;

import java.util.*;

public class Simulation {
    private final SimulationConfig config;
    private final World world;
    private final Random random;
    private final IdGenerator idGenerator;
    private final List<Action> actions;

    private int currentTurn = 0;

    public Simulation() {
        this(ConfigLoader.load());
    }

    public Simulation(SimulationConfig config) {
        this.config = config;
        this.world = new World(config.worldWidth(), config.worldHeight());
        this.random = config.randomSeed() == null
                ? new Random()
                : new Random((config.randomSeed()));
        this.idGenerator = new IdGenerator();
        this.actions = List.of(
                new HungerAction(config.hungerPerTurn(), config.starvationDamage()),
                new MovementAction(random),
                new FeedingAction(config.hungerReductionPerMeal(), config.healingPerMeal()),
                new ReproductionAction(config, idGenerator, random)
        );

    }

    public void initialize() {
        List<Position> freePositions = allPositions();
        Collections.shuffle(freePositions, random);

        int positionIndex = 0;

        for (var entry : config.initialCounts().entrySet()) {
            EntityType type = entry.getKey();
            int count = entry.getValue();

            for (int i = 0; i < count; i++) {
                Entity entity = EntityFactory.create(
                        type,
                        idGenerator.nextId(),
                        config,
                        currentTurn
                );

                world.placeEntity(freePositions.get(positionIndex++), entity);
            }
        }
    }

    public World getWorld() {
        return world;
    }

    private List<Position> allPositions() {
        List<Position> positions = new ArrayList<>();

        for (int x = 0; x < config.worldWidth(); x++) {
            for (int y = 0; y < config.worldHeight(); y++) {
                positions.add(new Position(x, y));
            }
        }

        return positions;
    }

    public void start() {
        start(1000);
    }

    void start(long turnDelayMillis) {
        initialize();
        printStatistics(new TurnStatistics());

        for (int turn = 0; turn < config.turnCount(); turn++) {
            try {
                Thread.sleep(turnDelayMillis);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
            nextTurn();
        }
    }

    private void printStatistics(TurnStatistics statistics) {


        var entities = world.getWorldMap();
        Map<EntityType, Integer> counts = new
                EnumMap<>(EntityType.class);

        for (Entity entity : entities.values()){
            EntityType type = entity.getentityType();
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }

        System.out.println(
                "turn=" + currentTurn
                        + " total=" + entities.size()
                        + " rabbit=" + counts.getOrDefault(EntityType.RABBIT, 0)
                        + " sheep=" + counts.getOrDefault(EntityType.SHEEP, 0)
                        + " wolf=" + counts.getOrDefault(EntityType.WOLF, 0)
                        + " grass=" + counts.getOrDefault(EntityType.GRASS, 0)
                        + " rock=" + counts.getOrDefault(EntityType.ROCK, 0)
                        + " starved=" + statistics.getStarvedCount()
                        + " moved=" + statistics.getMovedCount()
                        + " attacks=" + statistics.getAttackCount()
                        + " born=" + statistics.getBirthCount()
                        + " grassEaten=" + statistics.getEatenGrassCount()
                        + " killed=" + statistics.getKilledCount()
        );

    }

    public void nextTurn() {
        currentTurn++;
        TurnStatistics statistics = new TurnStatistics();

        for (Action action : actions) {
            action.execute(world, currentTurn, statistics);
        }

        printStatistics(statistics);
    }

}
