package world;

import entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class World {

    public World(int width, int height){

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

   private final HashMap<Position, Entity> worldMap = new HashMap<>();


    public Map<Position, Entity> getWorldMap(){
        return Map.copyOf(worldMap);
    }

    private boolean isPositionInsideWorld(Position position){
        return position.x() >= 0 && position.x() < width && position.y() >= 0 && position.y() < height;
    }

    private boolean isPositionOccupied(Position position){
        return worldMap.containsKey(position);
    }

    public void placeEntity(Position position, Entity entity){
        if (!isPositionInsideWorld(position) || isPositionOccupied(position)){
            return;
        }
        worldMap.put(position, entity);
    }

    public boolean replaceEntity(Position from, Position to) {
        if (!isPositionInsideWorld(to) || isPositionOccupied(to)) {
            return false;
        }

        Entity entity = worldMap.remove(from);

        if (entity == null) {
            return false;
        }

        worldMap.put(to, entity);
        return true;
    }

    public void removeEntity(Position position){
        if(isPositionOccupied(position) && isPositionInsideWorld(position)){
            worldMap.remove(position);
        }
    }

}
