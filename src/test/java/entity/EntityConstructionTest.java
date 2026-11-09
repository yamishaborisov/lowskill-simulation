package entity;

import config.AnimalSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityConstructionTest {
    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void grassRejectsNonPositiveReproductionInterval(int interval) {
        assertThrows(IllegalArgumentException.class, () -> new Grass(1, 0, interval));
    }

    @ParameterizedTest
    @CsvSource({"-1, 10, 0, 5", "0, 0, 0, 5", "0, -1, 0, 5",
            "0, 10, -1, 5", "0, 10, 0, 0", "0, 10, 0, -1"})
    void animalSettingsRejectInvalidValues(int speed, int health, int damage, int interval) {
        assertThrows(IllegalArgumentException.class,
                () -> new AnimalSettings(speed, health, damage, interval, true));
    }

    @Test
    void acceptsMinimumValidAnimalSettingsAndGrassInterval() {
        assertDoesNotThrow(() -> new AnimalSettings(0, 1, 0, 1, true));
        assertDoesNotThrow(() -> new Grass(1, 0, 1));
    }

    @Test
    void rockAlwaysHasRockType() {
        assertEquals(EntityType.ROCK, new Rock(1).getEntityType());
    }
}
