package renderer;

import simulation.TurnStatistics;
import world.World;
import entity.Entity;
import entity.EntityType;
import java.util.EnumMap;
import java.util.Map;

public class ConsoleRenderer {
    public void render(World world, int currentTurn, TurnStatistics statistics) {
        var entities = world.getWorldMap();
        Map<EntityType, Integer> counts = new
                EnumMap<>(EntityType.class);

        for (Entity entity : entities.values()){
            EntityType type = entity.getEntityType();
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }

        System.out.println(
                "turn=" + currentTurn
                        + " total=" + entities.size()
                        + " rabbit=" + counts.getOrDefault(EntityType.RABBIT, 0)
                        + " sheep=" + counts.getOrDefault(EntityType.SHEEP, 0)
                        + " wolf=" + counts.getOrDefault(EntityType.WOLF, 0)
                        + " grass=" + counts.getOrDefault(EntityType.GRASS, 0)
                        + " rock=" + counts.getOrDefault(EntityType.ROCK, 0)
                        + " starved=" + statistics.getStarvedCount()
                        + " moved=" + statistics.getMovedCount()
                        + " attacks=" + statistics.getAttackCount()
                        + " born=" + statistics.getBirthCount()
                        + " grassEaten=" + statistics.getEatenGrassCount()
                        + " killed=" + statistics.getKilledCount()
        );
    }


}
