package entity;

public class Grass extends Entity implements Reproducible {

    private final int reproductionInterval;
    private int lastReproductionTurn;

    public Grass(long id, int currentTurn, int reproductionInterval) {
        super(id, EntityType.GRASS);
        this.lastReproductionTurn = currentTurn;
        this.reproductionInterval = reproductionInterval;
    }

    @Override
    public boolean canReproduce(int currentTurn, int hungerLimit) {
        return currentTurn - lastReproductionTurn >= reproductionInterval;
    }

    @Override
    public void markReproduced(int currentTurn) {
        lastReproductionTurn = currentTurn;
    }
}
