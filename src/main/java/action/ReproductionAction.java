package action;

import config.SimulationConfig;
import entity.Entity;
import entity.EntityFactory;
import entity.Reproducible;
import simulation.IdGenerator;
import simulation.TurnStatistics;
import world.Direction;
import world.Position;
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

            List<Position> freeNeighbours = freeNeighbours(world, entry.getKey());
            if (freeNeighbours.isEmpty()) {
                continue;
            }

            Position position = freeNeighbours.get(random.nextInt(freeNeighbours.size()));
            Entity child = EntityFactory.create(parent.getentityType(), idGenerator.nextId(), config, currentTurn);
            world.placeEntity(position, child);
            if (world.getWorldMap().get(position) == child) {
                reproducible.markReproduced(currentTurn);
                statistics.recordBirth();
            }
        }
    }

    private List<Position> freeNeighbours(World world, Position position) {
        List<Position> neighbours = new ArrayList<>();
        var occupied = world.getWorldMap();
        for (Direction direction : Direction.values()) {
            Position neighbour = new Position(
                    position.x() + direction.getDeltaX(),
                    position.y() + direction.getDeltaY()
            );
            if (neighbour.x() >= 0 && neighbour.x() < world.getWidth()
                    && neighbour.y() >= 0 && neighbour.y() < world.getHeight()
                    && !occupied.containsKey(neighbour)) {
                neighbours.add(neighbour);
            }
        }
        return neighbours;
    }
}
