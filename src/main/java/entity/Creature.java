package entity;

import config.AnimalSettings;
import world.Coordinates;
import world.Direction;
import world.World;

import java.util.Random;

public abstract class Creature extends Entity implements Reproducible {
    private final AnimalSettings settings;
    private int health;
    private int hunger;
    private int lastReproductionTurn;

    public int getHealth() {
        return health;
    }

    public int getMaxSpeed(){
        return settings.maxSpeed();
    }

    public int getHunger() {
        return hunger;
    }

    public abstract boolean canEat(Entity target);

    public Creature(long id, EntityType entityType, AnimalSettings settings, int currentTurn) {
        super(id, entityType);
        this.settings = settings;
        this.health = settings.maxHealth();
        this.hunger = 0;
        this.lastReproductionTurn = currentTurn;
    }

    public boolean isAlive(){
        return health > 0;
    }

    public void takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Урон не может быть отрицательным");
        }
        health = Math.max(0, health - damage);
    }

    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Количество лечения не может быть отрицательным");
        }
        if (isAlive()){
            int healedHealth = health + amount;
            health = Math.min(healedHealth, settings.maxHealth());
        }


    }

    public void increaseHunger(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Прирост голода не может быть отрицательным");
        }
        if (!isAlive()) {
            return;
        }

        hunger += Math.min(amount, 100 - hunger);
    }

    public void eat(int hungerReduction, int healing) {
        if (hungerReduction < 0 || healing < 0) {
            throw new IllegalArgumentException("Эффекты еды не могут быть отрицательными");
        }
        if (!isAlive()) {
            return;
        }

        hunger = Math.max(0, hunger - hungerReduction);
        heal(healing);
    }

    public int getAttackDamage() {
        return settings.attackDamage();
    }

    public boolean move(World world, Coordinates currentCoordinates, Random random){

        if (!isAlive()) return false;

        if (getHunger() > 0 && findFoodNearby(world, currentCoordinates) != null){
            return false;
        }


        Direction direction = randomDirection(random);
        int distance = randomDistance(random);

        boolean moved = false;

        for (int step = 0; step < distance; step++) {
            Coordinates nextCoordinates = currentCoordinates.step(direction);

            if (!world.replaceEntity(currentCoordinates, nextCoordinates)) {
                break;
            }

            currentCoordinates = nextCoordinates;
            moved = true;

        }
        return moved;
    }

    public FeedingResult tryToEat(World world, Coordinates currentCoordinates, int hungerReductionPerMeal,
                                  int healingPerMeal){
        if (!isAlive() || getHunger() == 0) {
            return FeedingResult.NONE;
        }
        Coordinates foodCoordinates = findFoodNearby(world, currentCoordinates);
        if (foodCoordinates == null){
            return FeedingResult.NONE;
        }

        Entity food = world.getEntityAt(foodCoordinates);

        if (food instanceof Grass) {
            world.removeEntity(foodCoordinates);
            eat(hungerReductionPerMeal, healingPerMeal);
            return FeedingResult.GRASS_EATEN;
        }else if (food instanceof Creature prey) {
            prey.takeDamage(getAttackDamage());

            if (!prey.isAlive()) {
                world.removeEntity(foodCoordinates);
                eat(hungerReductionPerMeal, healingPerMeal);
                return FeedingResult.PREY_KILLED;
            } else {
                return FeedingResult.ATTACKED;
            }
        }
        return FeedingResult.NONE;
    }

    private Direction randomDirection(Random random) {
        Direction[] directions = Direction.values();

        return directions[random.nextInt(directions.length)];
    }

    private int randomDistance(Random random) {

        return random.nextInt(getMaxSpeed() + 1);
    }

    private Coordinates findFoodNearby(World world, Coordinates coordinates) {

        for (Coordinates neighbour : world.getNeighborsCoordinates(coordinates)) {
            Entity target = world.getEntityAt(neighbour);

            if (target != null && canEat(target)) {
                return neighbour;
            }
        }

        return null;
    }

    @Override
    public boolean canReproduce(int currentTurn, int hungerLimit) {
        return settings.reproductionEnabled()
                && isAlive()
                && hunger <= hungerLimit
                && currentTurn - lastReproductionTurn >= settings.reproductionInterval();
    }

    @Override
    public void markReproduced(int currentTurn) {
        lastReproductionTurn = currentTurn;
    }
}
