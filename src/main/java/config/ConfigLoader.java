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
        int worldWidth = integer(properties, "world.width");
        int worldHeight = integer(properties, "world.height");
        int turnCount = integer(properties, "simulation.turn-count");

        Map<EntityType, Integer> initialCounts = new EnumMap<>(EntityType.class);
        for (EntityType entityType : EntityType.values()) {
            initialCounts.put(entityType, integer(properties, key(entityType, "initial-count")));
        }

        Map<EntityType, AnimalSettings> animalSettings = new EnumMap<>(EntityType.class);
        for (EntityType entityType : new EntityType[]{EntityType.RABBIT, EntityType.SHEEP, EntityType.WOLF}) {
            animalSettings.put(entityType, new AnimalSettings(
                    integer(properties, key(entityType, "max-speed")),
                    integer(properties, key(entityType, "max-health")),
                    integer(properties, key(entityType, "attack-damage")),
                    integer(properties, key(entityType, "reproduction-interval")),
                    booleanValue(properties, key(entityType, "reproduction-enabled"))
            ));
        }


        int grassReproductionInterval = integer(properties, "species.grass.reproduction-interval");
        int hungerPerTurn = integer(properties, "animals.hunger-per-turn");
        int starvationDamage = integer(properties, "animals.starvation-damage");
        int hungerReductionPerMeal = integer(properties, "animals.hunger-reduction-per-meal");
        int healingPerMeal = integer(properties, "animals.healing-per-meal");
        int reproductionHungerLimit = integer(properties, "animals.reproduction-hunger-limit");

        return new SimulationConfig(
                worldWidth, worldHeight, turnCount, initialCounts, animalSettings,
                grassReproductionInterval, hungerPerTurn, starvationDamage,
                hungerReductionPerMeal, healingPerMeal, reproductionHungerLimit,
                randomSeed(properties)
        );
    }
    private static boolean booleanValue(Properties properties, String key) {
        String value = required(properties, key);

        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }

        throw new IllegalArgumentException(key + " должен быть true или false");
    }

    private static String key(EntityType entityType, String setting) {
        return "species." + entityType.name().toLowerCase() + "." + setting;
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
