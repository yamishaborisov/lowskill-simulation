package entity;

import config.AnimalSettings;

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
        if (damage > 0){
            if (damage < health ){
                health = health - damage;
            }else {
                health = 0;
            }
        }
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

    @Override
    public boolean canReproduce(int currentTurn, int hungerLimit) {
        return isAlive()
                && hunger <= hungerLimit
                && currentTurn - lastReproductionTurn >= settings.reproductionInterval();
    }

    @Override
    public void markReproduced(int currentTurn) {
        lastReproductionTurn = currentTurn;
    }
}
