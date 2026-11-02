package action;

import entity.Creature;
import entity.Entity;
import entity.Grass;
import simulation.TurnStatistics;
import world.Direction;
import world.Position;
import world.World;

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
        for (var entry : world.getWorldMap().entrySet()) {
            if (!(entry.getValue() instanceof Creature creature) || !creature.isAlive()
                    || creature.getHunger() == 0) {
                continue;
            }
            Position foodPosition = findFood(world, entry.getKey(), creature);
            if (foodPosition == null){
                continue;
            }
            Entity food = world.getWorldMap().get(foodPosition);

            if (food instanceof Grass) {
                world.removeEntity(foodPosition);
                statistics.recordGrassEaten();
                creature.eat(hungerReductionPerMeal, healingPerMeal);
            }
            else if (food instanceof Creature prey) {
                prey.takeDamage(creature.getAttackDamage());
                statistics.recordAttack();

                if (!prey.isAlive()) {
                    world.removeEntity(foodPosition);
                    statistics.recordKill();
                    creature.eat(hungerReductionPerMeal, healingPerMeal);
                }
            }
        }
    }

    private Position findFood(World world, Position position, Creature creature) {

        for (Direction direction : Direction.values()) {
            Position neighbour = new Position(
                    position.x() + direction.getDeltaX(),
                    position.y() + direction.getDeltaY()
            );

            Entity target = world.getWorldMap().get(neighbour);

            if (target != null && creature.canEat(target)) {
                return neighbour;
            }
        }

        return null;
    }
}
