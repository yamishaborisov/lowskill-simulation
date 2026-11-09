package entity;

import config.AnimalSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreatureTest {
    private final Rabbit rabbit = new Rabbit(1, new AnimalSettings(2, 6, 0, 5, true), 0);

    @Test
    void reproductionCanBeDisabledDespiteSatisfiedHealthHungerAndInterval() {
        Rabbit disabled = new Rabbit(2, new AnimalSettings(2, 6, 0, 5, false), 0);

        assertTrue(rabbit.canReproduce(5, 0));
        assertFalse(disabled.canReproduce(5, 0));
        assertFalse(disabled.canReproduce(100, 100));
    }

    @Test
    void damageIsClampedAtZero() {
        rabbit.takeDamage(100);
        assertEquals(0, rabbit.getHealth());
        assertFalse(rabbit.isAlive());
    }

    @Test
    void healingAndMealsCannotExceedMaximumHealthOrReduceHungerBelowZero() {
        rabbit.takeDamage(1);
        rabbit.heal(10);
        assertEquals(6, rabbit.getHealth());
        rabbit.increaseHunger(20);
        rabbit.eat(40, 2);
        assertEquals(0, rabbit.getHunger());
        assertEquals(6, rabbit.getHealth());
    }

    @Test
    void deadAnimalCannotHealEatBecomeHungrierOrReproduce() {
        rabbit.increaseHunger(60);
        rabbit.takeDamage(6);
        rabbit.heal(2);
        rabbit.eat(40, 2);
        rabbit.increaseHunger(20);
        assertEquals(0, rabbit.getHealth());
        assertEquals(60, rabbit.getHunger());
        assertFalse(rabbit.canReproduce(100, 100));
    }

    @Test
    void hungerIsClampedAtOneHundredEvenForLargeIncrease() {
        rabbit.increaseHunger(90);
        rabbit.increaseHunger(Integer.MAX_VALUE);
        assertEquals(100, rabbit.getHunger());
    }

    @Test
    void negativeArgumentsAreRejectedWithoutChangingState() {
        assertThrows(IllegalArgumentException.class, () -> rabbit.takeDamage(-1));
        assertThrows(IllegalArgumentException.class, () -> rabbit.heal(-1));
        assertThrows(IllegalArgumentException.class, () -> rabbit.increaseHunger(-1));
        assertThrows(IllegalArgumentException.class, () -> rabbit.eat(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> rabbit.eat(40, -1));
        assertEquals(6, rabbit.getHealth());
        assertEquals(0, rabbit.getHunger());
    }
}
