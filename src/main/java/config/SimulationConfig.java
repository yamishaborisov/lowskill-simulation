package config;

import entity.EntityType;

import java.util.Map;

public record SimulationConfig(
        int worldWidth,
        int worldHeight,
        int turnCount,
        Map<EntityType, Integer> initialCounts,
        Map<EntityType, AnimalSettings> animalSettings,
        int grassReproductionInterval,
        int hungerPerTurn,
        int starvationDamage,
        int hungerReductionPerMeal,
        int healingPerMeal,
        int reproductionHungerLimit,
        Long randomSeed
) {
    public SimulationConfig {
        if (worldWidth <= 0 || worldHeight <= 0) {
            throw new IllegalArgumentException("Ширина и высота мира должны быть положительными");
        }
        if (turnCount < 0) {
            throw new IllegalArgumentException("Количество ходов не может быть отрицательным");
        }
        initialCounts = Map.copyOf(initialCounts);
        animalSettings = Map.copyOf(animalSettings);

        if (grassReproductionInterval <= 0) {
            throw new IllegalArgumentException("Интервал размножения травы должен быть положительным");
        }
        if (hungerPerTurn < 0) {
            throw new IllegalArgumentException("Прирост голода за ход не может быть отрицательным");
        }
        if (starvationDamage < 0) {
            throw new IllegalArgumentException("Урон от голода не может быть отрицательным");
        }
        if (hungerReductionPerMeal < 0) {
            throw new IllegalArgumentException("Уменьшение голода при питании не может быть отрицательным");
        }
        if (healingPerMeal < 0) {
            throw new IllegalArgumentException("Лечение при питании не может быть отрицательным");
        }
        if (reproductionHungerLimit < 0 || reproductionHungerLimit > 100) {
            throw new IllegalArgumentException("Порог голода для размножения должен быть от 0 до 100");
        }

        for (EntityType type : new EntityType[]{EntityType.RABBIT, EntityType.SHEEP, EntityType.WOLF}) {
            if (!animalSettings.containsKey(type)) {
                throw new IllegalArgumentException("Отсутствуют настройки животного: " + type);
            }
        }
        if (animalSettings.get(EntityType.WOLF).attackDamage() == 0) {
            throw new IllegalArgumentException("У волка должен быть положительный урон");
        }

        long totalCount = 0;
        for (int count : initialCounts.values()) {
            if (count < 0) {
                throw new IllegalArgumentException("Начальное количество сущностей не может быть отрицательным");
            }
            totalCount += count;
        }

        long capacity = (long) worldWidth * worldHeight;
        if (totalCount > capacity) {
            throw new IllegalArgumentException("Начальное количество объектов превышает площадь карты");
        }
    }
}
