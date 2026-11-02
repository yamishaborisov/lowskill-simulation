package entity;

import config.AnimalSettings;

public class Sheep extends Herbivore{
    public Sheep(long id, AnimalSettings settings, int currentTurn) {
        super(id, EntityType.SHEEP, settings, currentTurn);
    }
}
