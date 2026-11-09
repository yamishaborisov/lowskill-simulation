package entity;

public class Grass extends Entity implements Reproducible {

    private final int reproductionInterval;
    private int lastReproductionTurn;

    public Grass(long id, int currentTurn, int reproductionInterval) {
        super(id, EntityType.GRASS);
        if (reproductionInterval <= 0) {
            throw new IllegalArgumentException("Интервал размножения должен быть положительным");
        }
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
