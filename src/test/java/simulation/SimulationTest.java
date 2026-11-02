package simulation;

import config.ConfigLoader;
import config.SimulationConfig;
import entity.EntityType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimulationTest {
    @Test
    void zeroTurnsPrintsOnlyInitialState() {
        Simulation simulation = new Simulation(config(12, 8, 0, ConfigLoader.load().initialCounts()));
        List<String> lines = run(simulation);

        assertEquals(1, lines.size());
        Map<String, Integer> initial = parse(lines.getFirst());
        assertEquals(0, initial.get("turn"));
        assertEquals(42, initial.get("total"));
        assertEquals(6, initial.get("rabbit"));
        assertEquals(4, initial.get("sheep"));
        assertEquals(2, initial.get("wolf"));
        assertEquals(20, initial.get("grass"));
        assertEquals(10, initial.get("rock"));
        assertNoEvents(initial);
    }

    @Test
    void emptyWorldCompletesAllTurns() {
        List<String> lines = run(new Simulation(config(1, 1, 50, Map.of())));

        assertEquals(51, lines.size());
        for (int turn = 0; turn < lines.size(); turn++) {
            Map<String, Integer> values = parse(lines.get(turn));
            assertEquals(turn, values.get("turn"));
            assertEquals(0, values.get("total"));
            assertNoEvents(values);
        }
    }

    @Test
    void fullWorldCannotProduceOffspringAndCompletesAllTurns() {
        for (int size : new int[]{1, 2}) {
            Simulation simulation = new Simulation(config(size, size, 50, Map.of(EntityType.GRASS, size * size)));
            List<String> lines = run(simulation);

            assertEquals(51, lines.size());
            for (String line : lines) {
                Map<String, Integer> values = parse(line);
                assertEquals(size * size, values.get("total"));
                assertEquals(size * size, values.get("grass"));
                assertNoEvents(values);
            }
            assertEquals(size * size, simulation.getWorld().getWorldMap().size());
        }
    }

    @Test
    void defaultRunCompletesFiftyTurnsWithBalancedStatistics() {
        List<String> lines = run(new Simulation());
        assertEquals(51, lines.size());
        int previousTotal = parse(lines.getFirst()).get("total");
        for (int turn = 0; turn < lines.size(); turn++) {
            Map<String, Integer> values = parse(lines.get(turn));
            assertEquals(turn, values.get("turn"));
            assertEquals(values.get("total"), Integer.valueOf(values.get("rabbit") + values.get("sheep")
                    + values.get("wolf") + values.get("grass") + values.get("rock")));
            assertTrue(values.get("total") <= 96);
            assertEquals(previousTotal + values.get("born") - values.get("grassEaten")
                    - values.get("killed") - values.get("starved"), values.get("total"));
            previousTotal = values.get("total");
        }
    }

    private SimulationConfig config(int width, int height, int turns, Map<EntityType, Integer> counts) {
        SimulationConfig defaults = ConfigLoader.load();
        return new SimulationConfig(width, height, turns, counts, defaults.animalSettings(),
                defaults.grassReproductionInterval(), defaults.hungerPerTurn(), defaults.starvationDamage(),
                defaults.hungerReductionPerMeal(), defaults.healingPerMeal(), defaults.reproductionHungerLimit(), 42L);
    }

    private List<String> run(Simulation simulation) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(output);
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> simulation.start(0));
        } finally {
            System.setOut(original);
        }
        return bytes.toString(StandardCharsets.UTF_8).lines().toList();
    }

    private Map<String, Integer> parse(String line) {
        Map<String, Integer> values = new HashMap<>();
        for (String token : line.split(" ")) {
            String[] pair = token.split("=");
            values.put(pair[0], Integer.parseInt(pair[1]));
        }
        return values;
    }

    private void assertNoEvents(Map<String, Integer> values) {
        for (String event : List.of("moved", "attacks", "born", "grassEaten", "killed", "starved")) {
            assertEquals(0, values.get(event), event);
        }
    }
}
