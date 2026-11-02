package entity;

import config.AnimalSettings;

public class Wolf extends Predator{
    public Wolf(long id, AnimalSettings settings, int currentTurn) {
        super(id, EntityType.WOLF, settings, currentTurn);
}}
