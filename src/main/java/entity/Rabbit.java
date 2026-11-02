package entity;

import config.AnimalSettings;

public class Rabbit extends Herbivore {
    public Rabbit(long id, AnimalSettings settings, int currentTurn) {
        super(id, EntityType.RABBIT, settings, currentTurn);
    }
}
