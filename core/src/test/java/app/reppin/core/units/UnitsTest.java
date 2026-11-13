package app.reppin.core.units;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UnitsTest {

    @Test
    void knownDistanceConstants() {
        assertEquals(1_609_344, Units.Distance.MILE.toMm(1_000));      // 1 mi
        assertEquals(914, Units.Distance.YARD.toMm(1_000));            // 1 yd = 914.4 mm -> 914
        assertEquals(1_000_000, Units.Distance.KILOMETRE.toMm(1_000)); // 1 km
        assertEquals(25_000, Units.Distance.METRE.toMm(25_000));       // 25 m pool
    }

    @Test
    void knownWeightConstants() {
        assertEquals(1_000_000, Units.Weight.KILOGRAM.toMg(1_000));    // 1 kg
        assertEquals(453_592, Units.Weight.POUND.toMg(1_000));         // 1 lb = 453,592.37 mg
        assertEquals(20_411_657, Units.Weight.POUND.toMg(45_000));     // 45 lb
        assertEquals(45_000, Units.Weight.POUND.fromMg(20_411_657));
    }

    @Test
    void ucumCodes() {
        assertEquals("[mi_i]", Units.Distance.MILE.ucum());
        assertEquals("[lb_av]", Units.Weight.POUND.ucum());
        assertEquals("kg", Units.Weight.KILOGRAM.ucum());
    }

    @Test
    void durationFormattingRoundsPartialSecondsUp() {
        assertEquals("00:00", Units.formatDurationCeil(0));
        assertEquals("00:01", Units.formatDurationCeil(1));
        assertEquals("00:01", Units.formatDurationCeil(1_000));
        assertEquals("00:02", Units.formatDurationCeil(1_001));
        assertEquals("01:05", Units.formatDurationCeil(65_000));
        assertEquals("1:00:00", Units.formatDurationCeil(3_600_000));
    }

    @Test
    void roundDivRejectsNegativeInput() {
        assertThrows(IllegalArgumentException.class, () -> Units.roundDiv(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> Units.roundDiv(1, 0));
    }
}
