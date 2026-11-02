package entity;

import config.AnimalSettings;

public abstract class Herbivore extends Creature{
    public Herbivore(long id, EntityType entityType, AnimalSettings settings, int currentTurn) {
        super(id, entityType, settings, currentTurn);
    }

    @Override
    public boolean canEat(Entity target) {
        return target instanceof Grass;
    }
}
