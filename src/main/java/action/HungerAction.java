package action;

import entity.Creature;
import simulation.TurnStatistics;
import world.World;

public class HungerAction implements Action {
    private final int hungerPerTurn;
    private final int starvationDamage;

    public HungerAction(int hungerPerTurn, int starvationDamage) {
        if (hungerPerTurn < 0 || starvationDamage < 0) {
            throw new IllegalArgumentException("Прирост голода и урон не могут быть отрицательными");
        }
        this.hungerPerTurn = hungerPerTurn;
        this.starvationDamage = starvationDamage;
    }

    @Override
    public void execute(World world, int currentTurn, TurnStatistics statistics) {
        for (var entry : world.getWorldMap().entrySet()) {
            if (!(entry.getValue() instanceof Creature creature) || !creature.isAlive()) {
                continue;
            }

            creature.increaseHunger(hungerPerTurn);
            if (creature.getHunger() == 100) {
                creature.takeDamage(starvationDamage);
                if (!creature.isAlive()) {
                    world.removeEntity(entry.getKey());
                    statistics.recordStarvation();
                }
            }
        }
    }
}
