package simulation;

public class TurnStatistics {
    private int starvedCount;

    private int movedCount;
    private int attackCount;
    private int birthCount;
    private int eatenGrassCount;
    private int killedCount;

    public void recordStarvation() {
        starvedCount++;
    }

    public int getStarvedCount() {
        return starvedCount;
    }

    public void recordAttack() {
        attackCount++;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public void recordMoved() {
        movedCount++;
    }

    public int getMovedCount() {
        return movedCount;
    }

    public void recordBirth() {
        birthCount++;
    }

    public int getBirthCount() {
        return birthCount;
    }

    public void recordGrassEaten() {
        eatenGrassCount++;
    }

    public int getEatenGrassCount() {
        return eatenGrassCount;
    }

    public void recordKill() {
        killedCount++;
    }

    public int getKilledCount() {
        return killedCount;
    }
}
