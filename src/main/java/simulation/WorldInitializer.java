package simulation;

import config.SimulationConfig;
import entity.Entity;
import entity.EntityFactory;
import entity.EntityType;
import world.Coordinates;
import world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WorldInitializer {

    private final SimulationConfig config;
    private final Random random;
    private final IdGenerator idGenerator;

    public WorldInitializer(SimulationConfig config, Random random, IdGenerator idGenerator) {
        this.config = config;
        this.random = random;
        this.idGenerator = idGenerator;
    }

    private List<Coordinates> allCoordinates() {
        List<Coordinates> coordinates = new ArrayList<>();

        for (int x = 0; x < config.worldWidth(); x++) {
            for (int y = 0; y < config.worldHeight(); y++) {
                coordinates.add(new Coordinates(x, y));
            }
        }

        return coordinates;
    }

    public void initialize(World world, int currentTurn) {
        List<Coordinates> freeCoordinates = allCoordinates();
        Collections.shuffle(freeCoordinates, random);

        int coordinatesIndex = 0;

        for (var entry : config.initialCounts().entrySet()) {
            EntityType type = entry.getKey();
            int count = entry.getValue();

            for (int i = 0; i < count; i++) {
                Entity entity = EntityFactory.create(
                        type,
                        idGenerator.nextId(),
                        config,
                        currentTurn
                );

                world.placeEntity(freeCoordinates.get(coordinatesIndex++), entity);
            }
        }
    }
}
