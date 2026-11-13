package app.reppin.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WorkSpecTest {

    @Test
    void repEquivalentsFollowSpec54() {
        assertEquals(20, WorkSpec.repsManual(20).repEquivalents());
        assertEquals(12, WorkSpec.repsPaced(12, 2_000, 3_000).repEquivalents());
        assertEquals(1, WorkSpec.duration(45_000).repEquivalents());
        assertEquals(2, WorkSpec.distance(WorkSpec.DistanceMode.BLOCKS, 5_000_000, 2).repEquivalents());
    }

    @Test
    void distanceIsWholeBlocksOrLapsAndNeverDivided() {
        WorkSpec laps = WorkSpec.distance(WorkSpec.DistanceMode.LAPS, 25_000, 8); // 8 x 25 m pool laps
        assertEquals(200_000, laps.totalDistanceMm());
        assertEquals(WorkSpec.DistanceMode.LAPS, laps.distanceMode());
        assertThrows(IllegalArgumentException.class,
                () -> WorkSpec.distance(WorkSpec.DistanceMode.BLOCKS, 1_000_000, 0));
    }

    @Test
    void weightIsAnOptionalSeparateLoad() {
        WorkSpec plain = WorkSpec.repsManual(10);
        WorkSpec loaded = plain.withWeightMg(20_000_000); // 20 kg
        assertFalse(plain.hasLoad());
        assertTrue(loaded.hasLoad());
        assertEquals(10, loaded.reps());
        assertFalse(loaded.withWeightMg(0).hasLoad());
        assertFalse(plain.equals(loaded));
        assertEquals(loaded, plain.withWeightMg(20_000_000));
        assertEquals(loaded.hashCode(), plain.withWeightMg(20_000_000).hashCode());
    }

    @Test
    void estimates() {
        assertEquals(40_000, WorkSpec.repsManual(10).estimateMs(4_000));
        assertEquals(3_000 + 10 * 2_000, WorkSpec.repsPaced(10, 2_000, 3_000).estimateMs(4_000));
        assertEquals(60_000, WorkSpec.duration(60_000).estimateMs(4_000));
        assertEquals(-1, WorkSpec.distance(WorkSpec.DistanceMode.BLOCKS, 100_000, 3).estimateMs(4_000));
    }

    @Test
    void validatesRanges() {
        assertThrows(IllegalArgumentException.class, () -> WorkSpec.repsManual(0));
        assertThrows(IllegalArgumentException.class, () -> WorkSpec.repsManual(WorkSpec.MAX_REPS + 1));
        assertThrows(IllegalArgumentException.class, () -> WorkSpec.repsPaced(10, 100, 0));
        assertThrows(IllegalArgumentException.class, () -> WorkSpec.duration(500));
        assertThrows(IllegalArgumentException.class, () -> WorkSpec.repsManual(5).withWeightMg(-1));
    }
}
