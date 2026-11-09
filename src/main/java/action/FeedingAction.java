package action;

import entity.Creature;
import entity.Entity;
import simulation.TurnStatistics;
import world.Coordinates;
import world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeedingAction implements Action{
    private final int hungerReductionPerMeal;
    private final int healingPerMeal;

    public FeedingAction(int hungerReductionPerMeal, int healingPerMeal) {
        if (hungerReductionPerMeal < 0 || healingPerMeal < 0) {
            throw new IllegalArgumentException("Снижение голода и лечение не могут быть отрицательным");
        }

        this.hungerReductionPerMeal = hungerReductionPerMeal;
        this.healingPerMeal = healingPerMeal;
    }

    @Override
    public void execute(World world, int currentTurn, TurnStatistics statistics) {

        List<Map.Entry<Coordinates, Entity>> entities =
                new ArrayList<>(world.getWorldMap().entrySet());

        for (Map.Entry<Coordinates, Entity> entry : entities) {
            if (!(entry.getValue() instanceof Creature creature)) {
                continue;
            }

            switch (creature.tryToEat(world,entry.getKey(), hungerReductionPerMeal, healingPerMeal)) {
                case GRASS_EATEN -> statistics.recordGrassEaten();
                case ATTACKED -> statistics.recordAttack();
                case PREY_KILLED -> {
                    statistics.recordAttack();
                    statistics.recordKill();
                }
                case NONE -> {}
            }
        }

    }

}
