package entity;

public interface Reproducible {
     boolean canReproduce(int currentTurn, int hungerLimit);
     void markReproduced(int currentTurn);
}
