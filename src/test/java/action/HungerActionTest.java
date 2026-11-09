package action;

import config.AnimalSettings;
import entity.Rabbit;
import entity.Rock;
import org.junit.jupiter.api.Test;
import simulation.TurnStatistics;
import world.Coordinates;
import world.World;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class HungerActionTest {
    private final TurnStatistics statistics = new TurnStatistics();
    private final Coordinates start = new Coordinates(1, 1);

    @Test
    void increasesHungerWithoutDamageBelowLimit() {
        World world = new World(3, 3);
        Rabbit rabbit = rabbit(6);
        world.placeEntity(start, rabbit);

        new HungerAction(20, 2).execute(world);

        assertEquals(20, rabbit.getHunger());
        assertEquals(6, rabbit.getHealth());
        assertSame(rabbit, world.getWorldMap().get(start));
    }

    @Test
    void damagesEveryTurnAtMaximumHunger() {
        World world = new World(3, 3);
        Rabbit rabbit = rabbit(6);
        rabbit.increaseHunger(90);
        world.placeEntity(start, rabbit);
        HungerAction action = new HungerAction(20, 2);

        action.execute(world, 1, statistics);
        assertEquals(100, rabbit.getHunger());
        assertEquals(4, rabbit.getHealth());

        action.execute(world, 1, statistics);
        assertEquals(100, rabbit.getHunger());
        assertEquals(2, rabbit.getHealth());
        assertEquals(0, statistics.getStarvedCount());
    }

    @Test
    void removesStarvedAnimalBeforeMovementAndCountsDeathOnlyOnce() {
        World world = new World(3, 3);
        Rabbit rabbit = rabbit(2);
        rabbit.increaseHunger(90);
        world.placeEntity(start, rabbit);
        HungerAction action = new HungerAction(20, 2);

        action.execute(world, 1, statistics);
        assertEquals(0, rabbit.getHealth());
        assertTrue(world.getWorldMap().isEmpty());

        new MovementAction(new Random(0)).execute(world);
        action.execute(world, 1, statistics);
        assertTrue(world.getWorldMap().isEmpty());
        assertEquals(1, statistics.getStarvedCount());
    }

    @Test
    void skipsNonAnimalsAndAlreadyDeadAnimals() {
        World world = new World(3, 3);
        Rabbit rabbit = rabbit(2);
        rabbit.takeDamage(2);
        Rock rock = new Rock(2);
        Coordinates rockCoordinates = new Coordinates(0, 0);
        world.placeEntity(start, rabbit);
        world.placeEntity(rockCoordinates, rock);
        HungerAction action = new HungerAction(20, 2);

        action.execute(world, 1, statistics);

        assertEquals(0, rabbit.getHunger());
        assertSame(rock, world.getWorldMap().get(rockCoordinates));
        assertEquals(0, statistics.getStarvedCount());
    }

    @Test
    void rejectsNegativeSettings() {
        assertThrows(IllegalArgumentException.class, () -> new HungerAction(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> new HungerAction(20, -1));
    }

    private Rabbit rabbit(int health) {
        return new Rabbit(1, new AnimalSettings(1, health, 0, 5, true), 0);
    }
}
