package entity;

import config.SimulationConfig;

public class EntityFactory {
    private EntityFactory() {

    }
    public static Entity create(EntityType entityType,
                                long id,
                                SimulationConfig config,
                                int currentTurn){
        return switch (entityType) {
            case RABBIT ->  new Rabbit(
                    id,
                    config.animalSettings().get(EntityType.RABBIT),
                    currentTurn
            );
            case SHEEP -> new Sheep(
                    id,
                    config.animalSettings().get(EntityType.SHEEP),
                    currentTurn
            );
            case WOLF -> new Wolf(
                    id,
                    config.animalSettings().get(EntityType.WOLF),
                    currentTurn
            );

            case GRASS -> new Grass(
                    id,
                    currentTurn,
                    config.grassReproductionInterval()
            );
            case ROCK -> new Rock(
                    id,
                    EntityType.ROCK

            );
        };
    }
}
