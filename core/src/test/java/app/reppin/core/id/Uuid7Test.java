package app.reppin.core.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Uuid7Test {

    /** Deterministic randomness: cycles through a fixed list. */
    private static final class FixedRandom implements RandomSource {
        private final long[] values;
        private int next;

        FixedRandom(long... values) {
            this.values = values;
        }

        @Override
        public long nextLong() {
            return values[next++ % values.length];
        }
    }

    @Test
    void generatesValidVersion7Ids() {
        String id = Uuid7.generate(1_800_000_000_000L, new FixedRandom(0x123456789ABCDEFL, -1L));
        assertTrue(Uuid7.isValid(id), id);
        assertEquals(36, id.length());
        assertEquals('7', id.charAt(14));
        assertTrue("89ab".indexOf(id.charAt(19)) >= 0, id);
    }

    @Test
    void embedsTheTimestamp() {
        long t = 1_800_000_123_456L;
        String id = Uuid7.generate(t, new FixedRandom(5L, 9L));
        assertEquals(t, Uuid7.timestampMs(id));
    }

    @Test
    void laterTimestampsSortAfterEarlierOnesAsStrings() {
        String a = Uuid7.generate(1_000L, new FixedRandom(-1L, -1L));  // max random bits
        String b = Uuid7.generate(1_001L, new FixedRandom(0L, 0L));    // min random bits
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    void rejectsMalformedAndOutOfRange() {
        assertFalse(Uuid7.isValid(null));
        assertFalse(Uuid7.isValid("not-a-uuid"));
        assertFalse(Uuid7.isValid("018f1234-5678-4abc-8def-0123456789ab")); // version 4
        assertThrows(IllegalArgumentException.class, () -> Uuid7.generate(-1, new FixedRandom(0L)));
        assertThrows(IllegalArgumentException.class, () -> Uuid7.generate(1L << 48, new FixedRandom(0L)));
    }
}
