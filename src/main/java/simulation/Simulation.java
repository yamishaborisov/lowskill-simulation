package simulation;

import action.Action;
import action.FeedingAction;
import action.HungerAction;
import action.MovementAction;
import action.ReproductionAction;
import config.AnimalAnnotationReader;
import config.ConfigLoader;
import config.SimulationConfig;
import renderer.ConsoleRenderer;
import world.World;

import java.util.*;

public class Simulation {
    private final SimulationConfig config;
    private final World world;
    private final Random random;
    private final IdGenerator idGenerator;
    private final List<Action> actions;
    private final ConsoleRenderer renderer = new ConsoleRenderer();
    private final WorldInitializer worldInitializer;
    private boolean started = false;

    private int currentTurn = 0;

    public Simulation() {
        this(ConfigLoader.load());
    }

    public Simulation(SimulationConfig config) {
        var preparedSettings =
                new AnimalAnnotationReader().process(config.animalSettings());
        this.config = new SimulationConfig(
                config.worldWidth(),
                config.worldHeight(),
                config.turnCount(),
                config.initialCounts(),
                preparedSettings,
                config.grassReproductionInterval(),
                config.hungerPerTurn(),
                config.starvationDamage(),
                config.hungerReductionPerMeal(),
                config.healingPerMeal(),
                config.reproductionHungerLimit(),
                config.randomSeed()
        );
        this.world = new World(this.config.worldWidth(), this.config.worldHeight());
        this.random = this.config.randomSeed() == null
                ? new Random()
                : new Random((this.config.randomSeed()));
        this.idGenerator = new IdGenerator();
        this.worldInitializer = new WorldInitializer(this.config, random, idGenerator);
        this.actions = List.of(
                new HungerAction(this.config.hungerPerTurn(), this.config.starvationDamage()),
                new MovementAction(random),
                new FeedingAction(this.config.hungerReductionPerMeal(), this.config.healingPerMeal()),
                new ReproductionAction(this.config, idGenerator, random)
        );

    }

    public World getWorld() {
        return world;
    }

   public void start() {

       if (started) {
           throw new IllegalStateException("Симуляция уже запускалась");
       }
        started = true;
        worldInitializer.initialize(world, currentTurn);
        renderer.render(world, currentTurn, new TurnStatistics());
        for (int turn = 0; turn < config.turnCount(); turn++) {
            nextTurn();
        }
    }


    public void nextTurn() {
        currentTurn++;
        TurnStatistics statistics = new TurnStatistics();

        for (Action action : actions) {
            action.execute(world, currentTurn, statistics);
        }

        renderer.render(world, currentTurn, statistics);
    }
}
