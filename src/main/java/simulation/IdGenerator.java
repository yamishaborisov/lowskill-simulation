package simulation;

public class IdGenerator {
    private long currentId = 0;

    public long nextId() {
        currentId = currentId + 1;
        return currentId;
    }


}
