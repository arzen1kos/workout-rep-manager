package app.reppin.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RoutineTest {

    private static Routine sample() {
        Routine r = new Routine("r1", "Core blast", 100);
        // Russian twist 3 x 20 reps, 30 s rest between sets
        r.addBlock(new ExerciseBlock("b1", "twist", 3, WorkSpec.repsManual(20), 30_000), 110);
        r.addBlock(new RestBlock("b2", 60_000), 120);
        // Plank 2 x 45 s
        r.addBlock(new ExerciseBlock("b3", "plank", 2, WorkSpec.duration(45_000), 15_000), 130);
        return r;
    }

    @Test
    void summaryTotalsSetsAndRepEquivalents() {
        RoutineSummary s = sample().summarize(4_000);
        assertEquals(2, s.lines().size());
        assertEquals(5, s.totalSets());
        assertEquals(3 * 20 + 2 * 1, s.totalRepEquivalents());
        assertEquals(20, s.lines().get(0).repEquivalentsPerSet());
    }

    @Test
    void summaryEstimateCountsRestOnlyBetweenSets() {
        RoutineSummary s = sample().summarize(4_000);
        long twist = 3 * (20 * 4_000L) + 2 * 30_000L;  // work + 2 gaps between 3 sets
        long rest = 60_000L;
        long plank = 2 * 45_000L + 1 * 15_000L;        // work + 1 gap between 2 sets
        assertEquals(twist + rest + plank, s.estimatedMs());
        assertFalse(s.isEstimateIncomplete());
    }

    @Test
    void distanceBlocksMakeTheEstimateIncomplete() {
        Routine r = new Routine("r1", "Run", 100);
        r.addBlock(new ExerciseBlock("b1", "run", 1,
                WorkSpec.distance(WorkSpec.DistanceMode.BLOCKS, 1_000_000, 5), 0), 110);
        r.addBlock(new RestBlock("b2", 30_000), 120);
        RoutineSummary s = r.summarize(4_000);
        assertTrue(s.isEstimateIncomplete());
        assertEquals(30_000, s.estimatedMs());
        assertEquals(5, s.totalRepEquivalents());
    }

    @Test
    void sameExerciseCanAppearTwiceWithDifferentSpecs() {
        Routine r = new Routine("r1", "Mixed", 100);
        r.addBlock(new ExerciseBlock("b1", "twist", 3, WorkSpec.repsManual(20), 0), 110);
        r.addBlock(new ExerciseBlock("b2", "twist", 2, WorkSpec.duration(30_000).withWeightMg(5_000_000), 0), 120);
        assertEquals(2, r.summarize(4_000).lines().size());
        assertEquals("twist", ((ExerciseBlock) r.blocks().get(0)).exerciseId());
        assertEquals("twist", ((ExerciseBlock) r.blocks().get(1)).exerciseId());
    }

    @Test
    void reorderAndRemove() {
        Routine r = sample();
        r.moveBlock("b3", 0, 200);
        assertEquals("b3", r.blocks().get(0).id());
        assertEquals("b1", r.blocks().get(1).id());
        r.removeBlock("b2", 210);
        assertEquals(2, r.blocks().size());
        assertEquals(210, r.updatedAtMs());
    }

    @Test
    void rejectsBadBlocks() {
        Routine r = sample();
        assertThrows(IllegalArgumentException.class, () -> r.addBlock(new RestBlock("b1", 10_000), 300)); // duplicate id
        assertThrows(IllegalArgumentException.class, () -> new RestBlock("x", 100));                        // under 1 s
        assertThrows(IllegalArgumentException.class,
                () -> new ExerciseBlock("x", "e", 0, WorkSpec.repsManual(5), 0));
        assertThrows(IndexOutOfBoundsException.class, () -> r.moveBlock("b1", 9, 300));
        assertThrows(IllegalArgumentException.class, () -> r.removeBlock("nope", 300));
    }

    @Test
    void specResolverPrefersSessionThenBlockThenLastUsed() {
        WorkSpec session = WorkSpec.repsManual(5);
        WorkSpec block = WorkSpec.repsManual(10);
        WorkSpec last = WorkSpec.duration(30_000);
        assertEquals(session, SpecResolver.resolve(session, block, last));
        assertEquals(block, SpecResolver.resolve(null, block, last));
        assertEquals(last, SpecResolver.resolve(null, null, last));
        assertEquals(SpecResolver.FALLBACK_REPS, SpecResolver.resolve(null, null, null).reps());
    }
}
