package entity;

public abstract class Entity {
    private final String name;

    protected Entity(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
}
