package app.reppin.core.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.LongRange;

class Uuid7Properties {

    private static final long MAX_TS = (1L << 48) - 1;

    @Property
    void alwaysValidAndTimestampRoundTrips(
            @ForAll @LongRange(min = 0, max = MAX_TS) long ts, @ForAll long r1, @ForAll long r2) {
        String id = Uuid7.generate(ts, new Fixed(r1, r2));
        assertTrue(Uuid7.isValid(id));
        assertEquals(ts, Uuid7.timestampMs(id));
    }

    @Property
    void orderingFollowsTimestampForAnyRandomness(
            @ForAll @LongRange(min = 0, max = MAX_TS - 1) long ts,
            @ForAll long a1, @ForAll long a2, @ForAll long b1, @ForAll long b2) {
        String earlier = Uuid7.generate(ts, new Fixed(a1, a2));
        String later = Uuid7.generate(ts + 1, new Fixed(b1, b2));
        assertTrue(earlier.compareTo(later) < 0);
    }

    private static final class Fixed implements RandomSource {
        private final long first;
        private final long second;
        private int calls;

        Fixed(long first, long second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public long nextLong() {
            return (calls++ % 2 == 0) ? first : second;
        }
    }
}
