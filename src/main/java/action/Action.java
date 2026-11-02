package action;

import simulation.TurnStatistics;
import world.World;

public interface Action {
    void execute(World world, int currentTurn, TurnStatistics statistics);

    default void execute(World world) {
        execute(world, 0, new TurnStatistics());
    }
}
