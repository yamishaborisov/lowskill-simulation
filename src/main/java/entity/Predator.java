package entity;

import config.AnimalSettings;

public abstract class Predator extends Creature{
    public Predator(long id, EntityType entityType, AnimalSettings settings, int currentTurn) {
        super(id, entityType, settings, currentTurn);
    }

    @Override
    public boolean canEat(Entity target) {
        return target instanceof Herbivore herbivore && herbivore.isAlive();
    }
}
