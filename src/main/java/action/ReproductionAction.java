package action;

import config.SimulationConfig;
import entity.Entity;
import entity.EntityFactory;
import entity.Reproducible;
import simulation.IdGenerator;
import simulation.TurnStatistics;
import world.Coordinates;
import world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReproductionAction implements Action {
    private final SimulationConfig config;
    private final IdGenerator idGenerator;
    private final Random random;

    public ReproductionAction(SimulationConfig config, IdGenerator idGenerator, Random random) {
        this.config = config;
        this.idGenerator = idGenerator;
        this.random = random;
    }

    @Override
    public void execute(World world, int currentTurn, TurnStatistics statistics) {
        for (var entry : world.getWorldMap().entrySet()) {
            Entity parent = entry.getValue();
            if (!(parent instanceof Reproducible reproducible)
                    || !reproducible.canReproduce(currentTurn, config.reproductionHungerLimit())) {
                continue;
            }

            List<Coordinates> freeNeighbours = freeNeighbours(world, entry.getKey());
            if (freeNeighbours.isEmpty()) {
                continue;
            }

            Coordinates coordinates = freeNeighbours.get(random.nextInt(freeNeighbours.size()));
            Entity child = EntityFactory.create(parent.getEntityType(), idGenerator.nextId(), config, currentTurn);
            if (world.placeEntity(coordinates, child)) {
                reproducible.markReproduced(currentTurn);
                statistics.recordBirth();
            }
        }
    }

    private List<Coordinates> freeNeighbours(World world, Coordinates parentCoordinates) {
        List<Coordinates> freeCoordinates = new ArrayList<>();

        for (Coordinates coordinates : world.getNeighborsCoordinates(parentCoordinates)) {
            if (world.isCellAvailable(coordinates)) {
                freeCoordinates.add(coordinates);
            }
        }

        return freeCoordinates;
    }
}
