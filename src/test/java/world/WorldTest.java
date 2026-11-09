package world;

import entity.Rock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldTest {
    @ParameterizedTest
    @CsvSource({"0, 1", "1, 0", "-1, 1", "1, -1"})
    void rejectsNonPositiveDimensions(int width, int height) {
        assertThrows(IllegalArgumentException.class, () -> new World(width, height));
    }

    @Test
    void placementReportsResultAndDoesNotOverwriteOccupiedCell() {
        World world = new World(1, 1);
        Coordinates coordinates = new Coordinates(0, 0);
        Rock rock = new Rock(1);

        assertTrue(world.isCellAvailable(coordinates));
        assertNull(world.getEntityAt(coordinates));
        assertTrue(world.placeEntity(coordinates, rock));
        assertFalse(world.isCellAvailable(coordinates));
        assertFalse(world.placeEntity(coordinates, new Rock(2)));
        assertSame(rock, world.getEntityAt(coordinates));

        for (Coordinates outside : List.of(new Coordinates(-1, 0), new Coordinates(1, 0),
                new Coordinates(0, -1), new Coordinates(0, 1))) {
            assertFalse(world.isCellAvailable(outside));
            assertFalse(world.placeEntity(outside, new Rock(3)));
            assertNull(world.getEntityAt(outside));
        }
        assertEquals(1, world.getWorldMap().size());
    }

    @Test
    void failedMovesPreserveWorldAndSuccessfulMoveTransfersSameEntity() {
        World world = new World(3, 1);
        Coordinates from = new Coordinates(0, 0);
        Coordinates occupied = new Coordinates(1, 0);
        Coordinates free = new Coordinates(2, 0);
        Rock rock = new Rock(1);
        world.placeEntity(from, rock);
        world.placeEntity(occupied, new Rock(2));
        var initial = world.getWorldMap();

        assertFalse(world.replaceEntity(from, occupied));
        assertFalse(world.replaceEntity(from, new Coordinates(-1, 0)));
        assertFalse(world.replaceEntity(free, from));
        assertFalse(world.replaceEntity(free, free));
        assertEquals(initial, world.getWorldMap());

        assertTrue(world.replaceEntity(from, free));
        assertNull(world.getEntityAt(from));
        assertSame(rock, world.getEntityAt(free));
        assertEquals(2, world.getWorldMap().size());
    }

    @Test
    void neighboursRespectBordersDirectionOrderAndIncludeOccupiedCells() {
        World world = new World(3, 3);
        world.placeEntity(new Coordinates(1, 2), new Rock(1));

        assertEquals(List.of(new Coordinates(1, 2), new Coordinates(2, 1),
                new Coordinates(1, 0), new Coordinates(0, 1)),
                world.getNeighborsCoordinates(new Coordinates(1, 1)));
        assertEquals(List.of(new Coordinates(0, 1), new Coordinates(1, 0)),
                world.getNeighborsCoordinates(new Coordinates(0, 0)));
        assertTrue(new World(1, 1).getNeighborsCoordinates(new Coordinates(0, 0)).isEmpty());
    }

    @Test
    void snapshotIsUnmodifiableAndUnaffectedByLaterWorldChanges() {
        World world = new World(2, 1);
        Coordinates coordinates = new Coordinates(0, 0);
        Rock rock = new Rock(1);
        world.placeEntity(coordinates, rock);
        var snapshot = world.getWorldMap();

        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        world.removeEntity(coordinates);

        assertSame(rock, snapshot.get(coordinates));
        assertNull(world.getEntityAt(coordinates));
    }
}
