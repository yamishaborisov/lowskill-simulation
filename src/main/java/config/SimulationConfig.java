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
        initialCounts = Map.copyOf(initialCounts);
        animalSettings = Map.copyOf(animalSettings);
    }
}
