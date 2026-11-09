package config;

import entity.EntityType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimulationConfigTest {
    private final int[] values = {3, 3, 0, 5, 10, 2, 40, 2, 30};
    private final Map<EntityType, Integer> counts = new EnumMap<>(EntityType.class);
    private final Map<EntityType, AnimalSettings> settings = new EnumMap<>(EntityType.class);

    SimulationConfigTest() {
        AnimalSettings animal = new AnimalSettings(2, 10, 4, 5, true);
        settings.put(EntityType.RABBIT, animal);
        settings.put(EntityType.SHEEP, animal);
        settings.put(EntityType.WOLF, animal);
    }

    @ParameterizedTest(name = "parameter {0} rejects {1}")
    @CsvSource({"0, 0", "0, -1", "1, 0", "1, -1", "2, -1",
            "3, 0", "3, -1", "4, -1", "5, -1", "6, -1", "7, -1",
            "8, -1", "8, 101"})
    void rejectsInvalidNumericSettingsOnDirectConstruction(int index, int value) {
        values[index] = value;
        assertThrows(IllegalArgumentException.class, this::create);
    }

    @Test
    void acceptsZeroEffectsAndBothHungerLimitBoundaries() {
        for (int index = 4; index <= 7; index++) {
            values[index] = 0;
        }
        values[8] = 0;
        assertDoesNotThrow(this::create);
        values[8] = 100;
        assertDoesNotThrow(this::create);
    }

    @Test
    void rejectsNegativeCountsAndOverfilledWorldButAcceptsExactCapacity() {
        counts.put(EntityType.GRASS, -1);
        assertThrows(IllegalArgumentException.class, this::create);
        counts.put(EntityType.GRASS, 10);
        assertThrows(IllegalArgumentException.class, this::create);
        counts.put(EntityType.GRASS, 9);
        assertDoesNotThrow(this::create);
    }

    @Test
    void capacityAndTotalCountDoNotOverflowInt() {
        values[0] = 65_536;
        values[1] = 65_536;
        counts.put(EntityType.GRASS, Integer.MAX_VALUE);
        counts.put(EntityType.ROCK, Integer.MAX_VALUE);
        assertDoesNotThrow(this::create);

        values[0] = 1;
        values[1] = 1;
        assertThrows(IllegalArgumentException.class, this::create);
    }

    @ParameterizedTest
    @EnumSource(value = EntityType.class, names = {"RABBIT", "SHEEP", "WOLF"})
    void rejectsMissingAnimalSettingsEvenWhenInitialCountIsZero(EntityType type) {
        settings.remove(type);
        assertThrows(IllegalArgumentException.class, this::create);
    }

    @Test
    void rejectsWolfWithZeroAttackDamage() {
        settings.put(EntityType.WOLF, new AnimalSettings(2, 10, 0, 5, true));
        assertThrows(IllegalArgumentException.class, this::create);
    }

    @Test
    void configurationKeepsImmutableCopiesOfInputMaps() {
        counts.put(EntityType.GRASS, 1);
        SimulationConfig config = create();
        counts.clear();
        settings.clear();

        assertEquals(1, config.initialCounts().get(EntityType.GRASS));
        assertEquals(3, config.animalSettings().size());
        assertThrows(UnsupportedOperationException.class, () -> config.initialCounts().clear());
        assertThrows(UnsupportedOperationException.class, () -> config.animalSettings().clear());
    }

    private SimulationConfig create() {
        return new SimulationConfig(values[0], values[1], values[2], counts, settings,
                values[3], values[4], values[5], values[6], values[7], values[8], null);
    }
}
