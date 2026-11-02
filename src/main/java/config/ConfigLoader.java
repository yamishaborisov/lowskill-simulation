package config;

import entity.EntityType;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class ConfigLoader {
    private static final String CONFIG_FILE = "simulation.properties";

    private ConfigLoader() {
    }

    public static SimulationConfig load() {
        Properties properties = new Properties();

        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Не найден файл настроек: " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось прочитать " + CONFIG_FILE, exception);
        }

        return createConfig(properties);
    }

    private static SimulationConfig createConfig(Properties properties) {
        int worldWidth = positive(properties, "world.width");
        int worldHeight = positive(properties, "world.height");
        int turnCount = nonNegative(properties, "simulation.turn-count");

        Map<EntityType, Integer> initialCounts = new EnumMap<>(EntityType.class);
        for (EntityType entityType : EntityType.values()) {
            initialCounts.put(entityType, nonNegative(properties, key(entityType, "initial-count")));
        }

        Map<EntityType, AnimalSettings> animalSettings = new EnumMap<>(EntityType.class);
        for (EntityType entityType : new EntityType[]{EntityType.RABBIT, EntityType.SHEEP, EntityType.WOLF}) {
            animalSettings.put(entityType, new AnimalSettings(
                    nonNegative(properties, key(entityType, "max-speed")),
                    positive(properties, key(entityType, "max-health")),
                    nonNegative(properties, key(entityType, "attack-damage")),
                    positive(properties, key(entityType, "reproduction-interval"))
            ));
        }

        int grassReproductionInterval = positive(properties, "species.grass.reproduction-interval");
        int hungerPerTurn = nonNegative(properties, "animals.hunger-per-turn");
        int starvationDamage = nonNegative(properties, "animals.starvation-damage");
        int hungerReductionPerMeal = nonNegative(properties, "animals.hunger-reduction-per-meal");
        int healingPerMeal = nonNegative(properties, "animals.healing-per-meal");
        int reproductionHungerLimit = nonNegative(properties, "animals.reproduction-hunger-limit");

        if (animalSettings.get(EntityType.WOLF).attackDamage() == 0) {
            throw new IllegalArgumentException("У волка должен быть положительный урон");
        }
        if (reproductionHungerLimit > 100) {
            throw new IllegalArgumentException("animals.reproduction-hunger-limit должен быть от 0 до 100");
        }
        if (initialCounts.values().stream().mapToInt(Integer::intValue).sum() > worldWidth * worldHeight) {
            throw new IllegalArgumentException("Начальное количество объектов превышает площадь карты");
        }

        return new SimulationConfig(
                worldWidth, worldHeight, turnCount, initialCounts, animalSettings,
                grassReproductionInterval, hungerPerTurn, starvationDamage,
                hungerReductionPerMeal, healingPerMeal, reproductionHungerLimit,
                randomSeed(properties)
        );
    }

    private static String key(EntityType entityType, String setting) {
        return "species." + entityType.name().toLowerCase() + "." + setting;
    }

    private static int positive(Properties properties, String key) {
        int value = integer(properties, key);
        if (value <= 0) {
            throw new IllegalArgumentException(key + " должен быть положительным");
        }
        return value;
    }

    private static int nonNegative(Properties properties, String key) {
        int value = integer(properties, key);
        if (value < 0) {
            throw new IllegalArgumentException(key + " не может быть отрицательным");
        }
        return value;
    }

    private static int integer(Properties properties, String key) {
        String value = required(properties, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " должен быть целым числом", exception);
        }
    }

    private static Long randomSeed(Properties properties) {
        String key = "simulation.random-seed";
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " должен быть целым числом", exception);
        }
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Отсутствует обязательная настройка: " + key);
        }
        return value.trim();
    }
}
