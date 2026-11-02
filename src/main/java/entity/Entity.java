package entity;

public abstract class Entity {
    private final EntityType entityType;
    private final long id;

    protected Entity(long id, EntityType entityType) {
        this.id = id;
        this.entityType = entityType;
    }

    public long getId() {
        return id;
    }

    public EntityType getentityType(){
        return this.entityType;
    }
}
