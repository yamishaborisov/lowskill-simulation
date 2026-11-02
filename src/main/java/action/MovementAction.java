package action;

import entity.Creature;
import entity.Entity;
import simulation.TurnStatistics;
import world.Direction;
import world.Position;
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
        List<Map.Entry<Position, Entity>> entities =
                new ArrayList<>(world.getWorldMap().entrySet());

        for (Map.Entry<Position, Entity> entry : entities) {
            if (!(entry.getValue() instanceof Creature creature)) {
                continue;
            }

            Position current = entry.getKey();
            if (creature.getHunger() > 0 && hasAdjacentFood(world, current, creature)) {
                continue;
            }
            Direction direction = randomDirection();
            int distance = randomDistance(creature);

            for (int step = 0; step < distance; step++) {
                Position wanted = new Position(
                        current.x() + direction.getDeltaX(),
                        current.y() + direction.getDeltaY()
                );

                Position next = constrain(
                        wanted,
                        world.getWidth(),
                        world.getHeight()
                );

                boolean moved = world.replaceEntity(current, next);

                if (!moved) {
                    break;
                }

                current = next;
            }

            if (!current.equals(entry.getKey())) {
                statistics.recordMoved();
            }
        }
    }

    public static Position constrain(Position wanted, int width, int height) {
        int x = Math.max(0, Math.min(wanted.x(), width - 1));
        int y = Math.max(0, Math.min(wanted.y(), height - 1));

        return new Position(x, y);
    }

    private boolean hasAdjacentFood(World world, Position position, Creature creature) {
        Map<Position, Entity> entities = world.getWorldMap();
        for (Direction direction : Direction.values()) {
            Position neighbour = new Position(
                    position.x() + direction.getDeltaX(),
                    position.y() + direction.getDeltaY()
            );
            Entity target = entities.get(neighbour);
            if (target != null && creature.canEat(target)) {
                return true;
            }
        }
        return false;
    }

    private Direction randomDirection() {
        Direction[] directions = Direction.values();

        return directions[random.nextInt(directions.length)];
    }

    private int randomDistance(Creature creature) {
        return random.nextInt(creature.getMaxSpeed() + 1);
    }
}
