package config;

import entity.*;

import java.util.EnumMap;
import java.util.Map;

public class AnimalAnnotationReader {
    private final Map<EntityType, Class<? extends Creature>> animalClasses = Map.of(
            EntityType.RABBIT, Rabbit.class,
            EntityType.SHEEP, Sheep.class,
            EntityType.WOLF, Wolf.class
    );

    public Map<EntityType, AnimalSettings> process(Map<EntityType, AnimalSettings> originalSettings){
        Map<EntityType, AnimalSettings> result =
                new EnumMap<>(EntityType.class);
        result.putAll(originalSettings);


        for (var entry : animalClasses.entrySet()) {
            EntityType type = entry.getKey();
            AnimalSettings original = originalSettings.get(type);
            Class<? extends Creature> animalClass = entry.getValue();

            boolean disabled =
                    animalClass.isAnnotationPresent(ReproductionDisabled.class);

            if (disabled){
                AnimalSettings settings = new AnimalSettings(original.maxSpeed(),
                        original.maxHealth(),
                        original.attackDamage(),
                        original.reproductionInterval(),
                        false);
                result.put(type, settings);
            }
        }
        return result;
    }
}
