package config;

public record AnimalSettings(
        int maxSpeed,
        int maxHealth,
        int attackDamage,
        int reproductionInterval,
        boolean reproductionEnabled
) {
    public AnimalSettings {
        if (maxSpeed < 0) {
            throw new IllegalArgumentException("Максимальная скорость не может быть отрицательной");
        }
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Максимальное здоровье должно быть положительным");
        }
        if (attackDamage < 0) {
            throw new IllegalArgumentException("Урон не может быть отрицательным");
        }
        if (reproductionInterval <= 0) {
            throw new IllegalArgumentException("Интервал размножения должен быть положительным");
        }
    }
}
