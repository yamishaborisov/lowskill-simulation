package config;

import entity.EntityType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AnimalAnnotationReaderTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void annotationDisablesReproductionAndUnannotatedSpeciesKeepConfiguredFlag(boolean enabled) {
        AnimalSettings original = new AnimalSettings(2, 10, 4, 5, enabled);
        Map<EntityType, AnimalSettings> input = Map.of(
                EntityType.RABBIT, original,
                EntityType.SHEEP, original,
                EntityType.WOLF, original
        );

        Map<EntityType, AnimalSettings> result = new AnimalAnnotationReader().process(input);

        assertEquals(original, result.get(EntityType.RABBIT));
        assertEquals(original, result.get(EntityType.SHEEP));
        assertEquals(new AnimalSettings(2, 10, 4, 5, false), result.get(EntityType.WOLF));
        assertFalse(result.get(EntityType.WOLF).reproductionEnabled());
        assertEquals(enabled, input.get(EntityType.WOLF).reproductionEnabled());
    }
}
