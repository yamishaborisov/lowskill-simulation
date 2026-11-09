package action;

import entity.Creature;
import entity.Entity;
import simulation.TurnStatistics;
import world.Coordinates;
import world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MovementAction implements Action{
    private final Random random;

    public MovementAction(Random random) {
        this.random = random;
    }

    @Override
    public void execute(World world, int currentTurn, TurnStatistics statistics) {
        List<Map.Entry<Coordinates, Entity>> entities =
                new ArrayList<>(world.getWorldMap().entrySet());

        for (Map.Entry<Coordinates, Entity> entry : entities) {
            if (!(entry.getValue() instanceof Creature creature)) {
                continue;
            }

            if (creature.move(world, entry.getKey(), random)) {
                statistics.recordMoved();
            }
        }
    }
}
