package action;

import config.AnimalSettings;
import entity.Rabbit;
import entity.Rock;
import entity.Grass;
import entity.Wolf;
import org.junit.jupiter.api.Test;
import simulation.TurnStatistics;
import world.Direction;
import world.Coordinates;
import world.World;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MovementActionTest {

    @Test
    void hungryRabbitWaitsForAdjacentGrassAndThenEats() {
        for (Direction direction : Direction.values()) {
            World world = new World(5, 5);
            Coordinates start = new Coordinates(2, 2);
            Rabbit rabbit = rabbitWithSpeed(2);
            rabbit.increaseHunger(20);
            Coordinates foodCoordinates = new Coordinates(start.x() + direction.getDeltaX(),
                    start.y() + direction.getDeltaY());
            world.placeEntity(start, rabbit);
            world.placeEntity(foodCoordinates, new Grass(2, 0, 4));
            TurnStatistics statistics = new TurnStatistics();

            // No random direction or distance is needed when food is adjacent.
            new MovementAction(new FixedRandom()).execute(world, 1, statistics);
            assertSame(rabbit, world.getWorldMap().get(start));
            assertEquals(0, statistics.getMovedCount());
            assertEquals(0, statistics.getEatenGrassCount());

            new FeedingAction(40, 2).execute(world, 1, statistics);
            assertFalse(world.getWorldMap().containsKey(foodCoordinates));
            assertEquals(0, rabbit.getHunger());
            assertEquals(1, statistics.getEatenGrassCount());
        }
    }

    @Test
    void satiatedRabbitStillMovesPastAdjacentGrass() {
        World world = new World(5, 5);
        Rabbit rabbit = rabbitWithSpeed(2);
        world.placeEntity(new Coordinates(2, 2), rabbit);
        world.placeEntity(new Coordinates(2, 3), new Grass(2, 0, 4));
        TurnStatistics statistics = new TurnStatistics();

        new MovementAction(new FixedRandom(1, 1)).execute(world, 1, statistics);

        assertSame(rabbit, world.getWorldMap().get(new Coordinates(3, 2)));
        assertEquals(1, statistics.getMovedCount());
    }

    @Test
    void diagonalGrassDoesNotStopHungryRabbit() {
        World world = new World(5, 5);
        Rabbit rabbit = rabbitWithSpeed(2);
        rabbit.increaseHunger(20);
        world.placeEntity(new Coordinates(2, 2), rabbit);
        world.placeEntity(new Coordinates(1, 3), new Grass(2, 0, 4));
        TurnStatistics statistics = new TurnStatistics();

        new MovementAction(new FixedRandom(1, 1)).execute(world, 1, statistics);

        assertSame(rabbit, world.getWorldMap().get(new Coordinates(3, 2)));
        assertEquals(1, statistics.getMovedCount());
    }

    @Test
    void hungryWolfWaitsForPreyAndAttacksDuringFeeding() {
        World world = new World(5, 5);
        Wolf wolf = new Wolf(1, new AnimalSettings(3, 16, 4, 8, true), 0);
        wolf.increaseHunger(20);
        Rabbit prey = new Rabbit(2, new AnimalSettings(0, 6, 0, 5, true), 0);
        Coordinates start = new Coordinates(2, 2);
        world.placeEntity(start, wolf);
        world.placeEntity(new Coordinates(2, 3), prey);
        TurnStatistics statistics = new TurnStatistics();

        new MovementAction(new FixedRandom(1, 0)).execute(world, 1, statistics);
        assertSame(wolf, world.getWorldMap().get(start));
        assertEquals(6, prey.getHealth());
        assertEquals(0, statistics.getMovedCount());

        new FeedingAction(40, 2).execute(world, 1, statistics);
        assertEquals(2, prey.getHealth());
        assertEquals(1, statistics.getAttackCount());
    }

    @Test
    void stopsAtWorldBorder() {
        World world = new World(5, 3);
        Rabbit rabbit = rabbitWithSpeed(2);
        Coordinates start = new Coordinates(0, 0);
        world.placeEntity(start, rabbit);

        new MovementAction(new FixedRandom(2, 2)).execute(world);

        assertSame(rabbit, world.getWorldMap().get(start));
    }

    @Test
    void stopsBeforeOccupiedCell() {
        World world = new World(5, 3);
        Rabbit rabbit = rabbitWithSpeed(4);
        world.placeEntity(new Coordinates(1, 1), rabbit);
        world.placeEntity(new Coordinates(3, 1), new Rock(2));

        new MovementAction(new FixedRandom(1, 4)).execute(world);

        assertSame(rabbit, world.getWorldMap().get(new Coordinates(2, 1)));
    }

    @Test
    void doesNotMoveWhenSpeedIsZero() {
        World world = new World(5, 3);
        Rabbit rabbit = rabbitWithSpeed(0);
        Coordinates start = new Coordinates(2, 1);
        world.placeEntity(start, rabbit);

        new MovementAction(new FixedRandom(1, 0)).execute(world);

        assertSame(rabbit, world.getWorldMap().get(start));
    }

    @Test
    void stopsWhenAllNeighboursAreOccupied() {
        World world = new World(3, 3);
        Rabbit rabbit = rabbitWithSpeed(1);
        Coordinates start = new Coordinates(1, 1);
        world.placeEntity(start, rabbit);
        world.placeEntity(new Coordinates(1, 2), new Rock(2));
        world.placeEntity(new Coordinates(2, 1), new Rock(3));
        world.placeEntity(new Coordinates(1, 0), new Rock(4));
        world.placeEntity(new Coordinates(0, 1), new Rock(5));

        new MovementAction(new FixedRandom(1, 1)).execute(world);

        assertSame(rabbit, world.getWorldMap().get(start));
    }

    @Test
    void movesOnlyUntilWorldBorder() {
        World world = new World(5, 3);
        Rabbit rabbit = rabbitWithSpeed(4);
        world.placeEntity(new Coordinates(3, 1), rabbit);

        new MovementAction(new FixedRandom(1, 4)).execute(world);

        assertSame(rabbit, world.getWorldMap().get(new Coordinates(4, 1)));
    }

    private Rabbit rabbitWithSpeed(int maxSpeed) {
        AnimalSettings settings = new AnimalSettings(maxSpeed, 6, 0, 5, true);
        return new Rabbit(1, settings, 0);
    }

    private static class FixedRandom extends Random {
        private final int[] values;
        private int index;

        private FixedRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            int value = values[index++];

            if (value < 0 || value >= bound) {
                throw new IllegalArgumentException("Значение не входит в границы nextInt: " + value);
            }

            return value;
        }
    }
}
