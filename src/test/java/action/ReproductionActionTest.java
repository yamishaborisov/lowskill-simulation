package action;

import config.AnimalSettings;
import config.SimulationConfig;
import entity.*;
import org.junit.jupiter.api.Test;
import simulation.IdGenerator;
import simulation.TurnStatistics;
import world.Coordinates;
import world.World;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ReproductionActionTest {
    private final AnimalSettings settings = new AnimalSettings(0, 10, 4, 5, true);
    private final SimulationConfig config = new SimulationConfig(
            3, 3, 10, Map.of(),
            Map.of(EntityType.RABBIT, settings, EntityType.SHEEP, settings, EntityType.WOLF, settings),
            5, 10, 2, 40, 2, 30, 0L
    );
    private final IdGenerator ids = new IdGenerator();
    private final ReproductionAction action = new ReproductionAction(config, ids, new Random(0));

    @Test
    void grassWaitsForIntervalAndResetsItAfterBirth() {
        World world = new World(3, 3);
        Grass parent = new Grass(ids.nextId(), 0, 5);
        world.placeEntity(new Coordinates(1, 1), parent);

        action.execute(world, 4, new TurnStatistics());
        assertEquals(1, world.getWorldMap().size());
        action.execute(world, 5, new TurnStatistics());

        assertEquals(2, world.getWorldMap().size());
        assertFalse(parent.canReproduce(9, 0));
        assertTrue(parent.canReproduce(10, 0));
        Entity child = childOf(world, parent);
        assertInstanceOf(Grass.class, child);
        assertFalse(((Grass) child).canReproduce(9, 0));
        assertTrue(((Grass) child).canReproduce(10, 0));
        assertEquals(3, ids.nextId());
    }

    @Test
    void animalsAtHungerLimitProduceHealthyFedChildrenOfSameSpecies() {
        for (EntityType type : List.of(EntityType.RABBIT, EntityType.SHEEP)) {
            World world = new World(3, 3);
            Creature parent = (Creature) EntityFactory.create(type, ids.nextId(), config, 0);
            parent.increaseHunger(30);
            parent.takeDamage(3);
            world.placeEntity(new Coordinates(1, 1), parent);

            action.execute(world, 5, new TurnStatistics());

            Creature child = (Creature) childOf(world, parent);
            assertEquals(type, child.getEntityType());
            assertEquals(10, child.getHealth());
            assertEquals(0, child.getHunger());
            assertEquals(7, parent.getHealth());
            assertFalse(parent.canReproduce(5, 30));
        }
    }

    @Test
    void disabledAnimalDoesNotProduceOffspring() {
        World world = new World(3, 3);
        Rabbit parent = new Rabbit(ids.nextId(), new AnimalSettings(0, 10, 4, 5, false), 0);
        world.placeEntity(new Coordinates(1, 1), parent);
        TurnStatistics statistics = new TurnStatistics();

        action.execute(world, 5, statistics);

        assertEquals(1, world.getWorldMap().size());
        assertSame(parent, world.getEntityAt(new Coordinates(1, 1)));
        assertEquals(0, statistics.getBirthCount());
    }

    @Test
    void actionUsesPreparedSettingsInsteadOfCheckingWolfAnnotation() {
        World world = new World(3, 3);
        Wolf parent = new Wolf(ids.nextId(), settings, 0);
        world.placeEntity(new Coordinates(1, 1), parent);
        TurnStatistics statistics = new TurnStatistics();

        action.execute(world, 5, statistics);

        Creature child = (Creature) childOf(world, parent);
        assertInstanceOf(Wolf.class, child);
        assertFalse(child.canReproduce(9, 30));
        assertTrue(child.canReproduce(10, 30));
        assertEquals(1, statistics.getBirthCount());
    }

    @Test
    void skipsHungryDeadAndRemovedParentsAndRocks() {
        World world = new World(4, 1);
        Rabbit hungry = new Rabbit(ids.nextId(), settings, 0);
        hungry.increaseHunger(31);
        Rabbit dead = new Rabbit(ids.nextId(), settings, 0);
        dead.takeDamage(10);
        Grass removed = new Grass(ids.nextId(), 0, 5);
        world.placeEntity(new Coordinates(0, 0), hungry);
        world.placeEntity(new Coordinates(1, 0), dead);
        world.placeEntity(new Coordinates(2, 0), removed);
        world.removeEntity(new Coordinates(2, 0));
        world.placeEntity(new Coordinates(3, 0), new Rock(ids.nextId()));

        action.execute(world, 5, new TurnStatistics());

        assertEquals(3, world.getWorldMap().size());
        assertTrue(removed.canReproduce(5, 30));
    }

    @Test
    void fullWorldDoesNotResetIntervalAndParentRetriesNextTurn() {
        World world = new World(2, 1);
        Grass parent = new Grass(ids.nextId(), 0, 5);
        world.placeEntity(new Coordinates(0, 0), parent);
        world.placeEntity(new Coordinates(1, 0), new Rock(ids.nextId()));

        action.execute(world, 5, new TurnStatistics());
        assertTrue(parent.canReproduce(5, 30));
        world.removeEntity(new Coordinates(1, 0));
        action.execute(world, 6, new TurnStatistics());

        assertEquals(2, world.getWorldMap().size());
        assertFalse(parent.canReproduce(10, 30));
        assertTrue(parent.canReproduce(11, 30));
    }

    @Test
    void onlyOneParentWinsLastCell() {
        World world = new World(3, 1);
        Grass first = new Grass(ids.nextId(), 0, 5);
        Grass second = new Grass(ids.nextId(), 0, 5);
        world.placeEntity(new Coordinates(0, 0), first);
        world.placeEntity(new Coordinates(2, 0), second);

        action.execute(world, 5, new TurnStatistics());

        assertEquals(3, world.getWorldMap().size());
        assertNotEquals(first.canReproduce(5, 30), second.canReproduce(5, 30));
    }

    @Test
    void selectsFreeNeighboursInDirectionOrder() {
        List<Coordinates> expected = List.of(
                new Coordinates(1, 2), new Coordinates(2, 1), new Coordinates(1, 0), new Coordinates(0, 1)
        );
        for (int index = 0; index < expected.size(); index++) {
            final int choice = index;
            Random random = new Random(0) {
                @Override
                public int nextInt(int bound) {
                    assertEquals(4, bound);
                    return choice;
                }
            };
            World world = new World(3, 3);
            Grass parent = new Grass(ids.nextId(), 0, 5);
            world.placeEntity(new Coordinates(1, 1), parent);

            new ReproductionAction(config, ids, random).execute(world, 5, new TurnStatistics());

            assertInstanceOf(Grass.class, world.getWorldMap().get(expected.get(index)));
            assertEquals(2, world.getWorldMap().size());
        }
    }

    @Test
    void newbornSkipsAllPhasesOfBirthTurn() {
        World world = new World(3, 1);
        Rabbit parent = new Rabbit(ids.nextId(), settings, 0);
        world.placeEntity(new Coordinates(1, 0), parent);
        List<Action> actions = List.of(
                new HungerAction(10, 2), new MovementAction(new Random(0)),
                new FeedingAction(40, 2), action
        );

        actions.forEach(phase -> phase.execute(world, 5, new TurnStatistics()));

        Creature child = (Creature) childOf(world, parent);
        assertEquals(0, child.getHunger());
        assertEquals(10, child.getHealth());
        assertFalse(child.canReproduce(5, 30));
        assertEquals(2, world.getWorldMap().size());
        actions.forEach(phase -> phase.execute(world, 6, new TurnStatistics()));
        assertEquals(10, child.getHunger());
        assertEquals(2, world.getWorldMap().size());
    }

    @Test
    void failedPlacementDoesNotResetParentInterval() {
        Coordinates parentCoordinates = new Coordinates(0, 0);
        World world = new World(2, 1) {
            @Override
            public boolean placeEntity(Coordinates coordinates, Entity entity) {
                if (coordinates.equals(parentCoordinates)) {
                    return super.placeEntity(coordinates, entity);
                }
                return false;
            }
        };
        Grass parent = new Grass(ids.nextId(), 0, 5);
        world.placeEntity(parentCoordinates, parent);

        action.execute(world, 5, new TurnStatistics());

        assertEquals(1, world.getWorldMap().size());
        assertTrue(parent.canReproduce(5, 30));
    }

    private Entity childOf(World world, Entity parent) {
        assertEquals(2, world.getWorldMap().size());
        return world.getWorldMap().values().stream()
                .filter(entity -> entity != parent).findFirst().orElseThrow();
    }
}
