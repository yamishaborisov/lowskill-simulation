package world;

public record Coordinates(int x, int y) {
    public Coordinates step(Direction direction) {
        return new Coordinates(
                x + direction.getDeltaX(),
                y + direction.getDeltaY()
        );
    }
}
