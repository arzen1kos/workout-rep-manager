package app.reppin.core.units;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.LongRange;

/** Property tests for SPEC section 5.1: conversions must be stable and bounded. */
class UnitsProperties {

    private static final long MAX_MILLI = 100_000_000L;      // 100,000 display units
    private static final long MAX_CANONICAL = 100_000_000_000L;

    /** Display units whose canonical step is finer than one milli-unit round-trip exactly. */
    @Property
    void fineCanonicalUnitsRoundTripExactly(@ForAll @LongRange(min = 0, max = MAX_MILLI) long milli) {
        for (Units.Distance d : new Units.Distance[] {
            Units.Distance.METRE, Units.Distance.KILOMETRE, Units.Distance.MILE }) {
            assertEquals(milli, d.fromMm(d.toMm(milli)), d.name());
        }
        for (Units.Weight w : Units.Weight.values()) {
            assertEquals(milli, w.fromMg(w.toMg(milli)), w.name());
        }
    }

    /** Going canonical -> display -> canonical never drifts by more than half a display step. */
    @Property
    void canonicalRoundTripErrorIsBounded(@ForAll @LongRange(min = 0, max = MAX_CANONICAL) long canonical) {
        for (Units.Distance d : Units.Distance.values()) {
            long back = d.toMm(d.fromMm(canonical));
            assertTrue(Math.abs(back - canonical) <= d.halfStepCanonical(), d.name());
        }
        for (Units.Weight w : Units.Weight.values()) {
            long back = w.toMg(w.fromMg(canonical));
            assertTrue(Math.abs(back - canonical) <= w.halfStepCanonical(), w.name());
        }
    }

    /** Conversion is monotonic: more input never yields less canonical. */
    @Property
    void conversionIsMonotonic(
            @ForAll @LongRange(min = 0, max = MAX_MILLI) long a,
            @ForAll @LongRange(min = 0, max = MAX_MILLI) long b) {
        long lo = Math.min(a, b);
        long hi = Math.max(a, b);
        for (Units.Distance d : Units.Distance.values()) {
            assertTrue(d.toMm(lo) <= d.toMm(hi), d.name());
        }
        for (Units.Weight w : Units.Weight.values()) {
            assertTrue(w.toMg(lo) <= w.toMg(hi), w.name());
        }
    }
}
