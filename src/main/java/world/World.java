package world;

import entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class World {

    public World(int width, int height){
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Ширина и высота мира должны быть положительными"
            );
        }
        this.width = width;
        this.height = height;
    }

    private final int width;
    private final int height;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

   private final HashMap<Coordinates, Entity> worldMap = new HashMap<>();


    public Map<Coordinates, Entity> getWorldMap(){
        return Map.copyOf(worldMap);
    }

    private boolean areCoordinatesInsideWorld(Coordinates coordinates){
        return coordinates.x() >= 0 && coordinates.x() < width && coordinates.y() >= 0 && coordinates.y() < height;
    }

    private boolean areCoordinatesOccupied(Coordinates coordinates){
        return worldMap.containsKey(coordinates);
    }

    public boolean placeEntity(Coordinates coordinates, Entity entity){
        if (!areCoordinatesInsideWorld(coordinates) || areCoordinatesOccupied(coordinates)){
            return false;
        }
        worldMap.put(coordinates, entity);
        return true;
    }

    public boolean replaceEntity(Coordinates from, Coordinates to) {
        if (!areCoordinatesInsideWorld(to) || areCoordinatesOccupied(to)) {
            return false;
        }

        Entity entity = worldMap.remove(from);

        if (entity == null) {
            return false;
        }

        worldMap.put(to, entity);
        return true;
    }

    public void removeEntity(Coordinates coordinates){
        if(areCoordinatesOccupied(coordinates) && areCoordinatesInsideWorld(coordinates)){
            worldMap.remove(coordinates);
        }
    }

    public Entity getEntityAt(Coordinates coordinates){
            return worldMap.get(coordinates);
    }

    public boolean isCellAvailable(Coordinates coordinates){
        return areCoordinatesInsideWorld(coordinates)
                && !areCoordinatesOccupied(coordinates);
    }

    public List<Coordinates> getNeighborsCoordinates(Coordinates coordinates) {
        List<Coordinates> neighborsCoordinates = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            Coordinates neighborCoordinates = coordinates.step(direction);
            if (areCoordinatesInsideWorld(neighborCoordinates)) {
                neighborsCoordinates.add(neighborCoordinates);
            }
        }
        return neighborsCoordinates;
    }

}
