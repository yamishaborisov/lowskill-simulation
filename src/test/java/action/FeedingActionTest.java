package action;

import config.AnimalSettings;
import entity.*;
import org.junit.jupiter.api.Test;
import world.Direction;
import world.Position;
import world.World;

import static org.junit.jupiter.api.Assertions.*;

class FeedingActionTest {
    private final FeedingAction action = new FeedingAction(40, 2);
    private final Position centre = new Position(2, 2);

    @Test
    void eatsGrassHealsAndStaysInPlace() {
        World world = new World(5, 5);
        Rabbit rabbit = rabbit(1);
        rabbit.increaseHunger(60);
        rabbit.takeDamage(3);
        Position food = neighbour(centre, Direction.UP);
        world.placeEntity(centre, rabbit);
        world.placeEntity(food, grass(2));

        action.execute(world);

        assertSame(rabbit, world.getWorldMap().get(centre));
        assertFalse(world.getWorldMap().containsKey(food));
        assertEquals(20, rabbit.getHunger());
        assertEquals(5, rabbit.getHealth());
    }

    @Test
    void sheepAlsoEatsGrassWithHealthAndHungerLimits() {
        World world = new World(5, 5);
        Sheep sheep = new Sheep(1, new AnimalSettings(1, 10, 0, 7), 0);
        sheep.increaseHunger(10);
        world.placeEntity(centre, sheep);
        world.placeEntity(neighbour(centre, Direction.RIGHT), grass(2));

        action.execute(world);

        assertEquals(0, sheep.getHunger());
        assertEquals(10, sheep.getHealth());
        assertEquals(1, world.getWorldMap().size());
    }

    @Test
    void checksDirectionsInOrderAndEatsOnlyOneTargetPerTurn() {
        World world = new World(5, 5);
        Rabbit rabbit = rabbit(1);
        world.placeEntity(centre, rabbit);
        long id = 2;
        for (Direction direction : Direction.values()) {
            world.placeEntity(neighbour(centre, direction), grass(id++));
        }

        for (Direction direction : Direction.values()) {
            rabbit.increaseHunger(100);
            int before = world.getWorldMap().size();
            action.execute(world);
            assertFalse(world.getWorldMap().containsKey(neighbour(centre, direction)));
            assertEquals(before - 1, world.getWorldMap().size());
        }
    }

    @Test
    void satiatedRabbitDoesNotEatAndSatiatedWolfDoesNotAttack() {
        World world = new World(5, 5);
        Rabbit rabbit = rabbit(1);
        Wolf wolf = wolf(2);
        Grass grass = grass(3);
        Position wolfPosition = neighbour(centre, Direction.RIGHT);
        Position food = neighbour(centre, Direction.UP);
        world.placeEntity(centre, rabbit);
        world.placeEntity(wolfPosition, wolf);
        world.placeEntity(food, grass);

        action.execute(world);

        assertEquals(6, rabbit.getHealth());
        assertSame(grass, world.getWorldMap().get(food));
        assertEquals(3, world.getWorldMap().size());
    }

    @Test
    void firstAttackOnlyDamagesAndSecondAttackKillsAndFeedsWolf() {
        World world = new World(5, 5);
        Wolf wolf = wolf(1);
        wolf.increaseHunger(60);
        wolf.takeDamage(6);
        Rabbit rabbit = rabbit(2);
        Position preyPosition = neighbour(centre, Direction.RIGHT);
        world.placeEntity(centre, wolf);
        world.placeEntity(preyPosition, rabbit);

        action.execute(world);

        assertEquals(2, rabbit.getHealth());
        assertSame(rabbit, world.getWorldMap().get(preyPosition));
        assertEquals(60, wolf.getHunger());
        assertEquals(10, wolf.getHealth());

        action.execute(world);

        assertEquals(0, rabbit.getHealth());
        assertFalse(world.getWorldMap().containsKey(preyPosition));
        assertEquals(20, wolf.getHunger());
        assertEquals(12, wolf.getHealth());
        assertSame(wolf, world.getWorldMap().get(centre));
    }

    @Test
    void twoWolvesHitSamePreyButOnlyKillerGetsFood() {
        World world = new World(5, 5);
        Rabbit rabbit = rabbit(1);
        Wolf first = wolf(2);
        Wolf second = wolf(3);
        first.increaseHunger(60);
        second.increaseHunger(60);
        first.takeDamage(6);
        second.takeDamage(6);
        world.placeEntity(centre, rabbit);
        world.placeEntity(neighbour(centre, Direction.LEFT), first);
        world.placeEntity(neighbour(centre, Direction.RIGHT), second);

        action.execute(world);

        assertEquals(0, rabbit.getHealth());
        assertFalse(world.getWorldMap().containsKey(centre));
        // Порядок обхода карты пока не задан, но еда достаётся ровно одному волку.
        assertEquals(80, first.getHunger() + second.getHunger());
        assertEquals(22, first.getHealth() + second.getHealth());
        assertTrue((first.getHunger() == 20 && first.getHealth() == 12
                && second.getHunger() == 60 && second.getHealth() == 10)
                || (second.getHunger() == 20 && second.getHealth() == 12
                && first.getHunger() == 60 && first.getHealth() == 10));
    }

    @Test
    void doesNotEatDistantOrDiagonalGrassOrAttackUnsuitableTargets() {
        World world = new World(6, 6);
        Rabbit rabbit = rabbit(1);
        rabbit.increaseHunger(60);
        Wolf wolf = wolf(2);
        wolf.increaseHunger(60);
        Wolf otherWolf = wolf(3);
        world.placeEntity(new Position(0, 0), rabbit);
        world.placeEntity(new Position(0, 1), new Rock(4, EntityType.ROCK));
        world.placeEntity(new Position(0, 2), grass(5));
        world.placeEntity(new Position(1, 1), grass(6));
        world.placeEntity(new Position(4, 4), wolf);
        world.placeEntity(new Position(4, 5), grass(7));
        world.placeEntity(new Position(5, 4), otherWolf);

        action.execute(world);

        assertEquals(7, world.getWorldMap().size());
        assertEquals(60, rabbit.getHunger());
        assertEquals(60, wolf.getHunger());
        assertEquals(16, otherWolf.getHealth());
    }

    @Test
    void deadAnimalDoesNotEatAndDeadPreyDoesNotFeedWolf() {
        World world = new World(5, 5);
        Rabbit rabbit = rabbit(1);
        rabbit.increaseHunger(60);
        rabbit.takeDamage(6);
        Wolf wolf = wolf(2);
        wolf.increaseHunger(60);
        Position food = neighbour(centre, Direction.UP);
        world.placeEntity(centre, rabbit);
        world.placeEntity(food, grass(3));
        world.placeEntity(neighbour(centre, Direction.RIGHT), wolf);

        action.execute(world);

        assertEquals(0, rabbit.getHealth());
        assertTrue(world.getWorldMap().containsKey(food));
        assertEquals(60, wolf.getHunger());
    }

    @Test
    void rejectsNegativeMealSettings() {
        assertThrows(IllegalArgumentException.class, () -> new FeedingAction(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> new FeedingAction(40, -1));
    }

    private Rabbit rabbit(long id) {
        return new Rabbit(id, new AnimalSettings(2, 6, 0, 5), 0);
    }

    private Wolf wolf(long id) {
        return new Wolf(id, new AnimalSettings(3, 16, 4, 8), 0);
    }

    private Grass grass(long id) {
        return new Grass(id, 0, 4);
    }

    private Position neighbour(Position position, Direction direction) {
        return new Position(position.x() + direction.getDeltaX(),
                position.y() + direction.getDeltaY());
    }
}
