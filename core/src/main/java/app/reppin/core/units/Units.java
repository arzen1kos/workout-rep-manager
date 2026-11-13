package app.reppin.core.units;

/**
 * Canonical storage units and conversions (SPEC section 5).
 *
 * <ul>
 *   <li>time: milliseconds ({@code long})</li>
 *   <li>distance: millimetres ({@code long})</li>
 *   <li>weight: milligrams ({@code long})</li>
 * </ul>
 *
 * <p>All maths is integer. User-facing quantities are carried as "milli-units" (1/1000 of the
 * display unit, e.g. 12.5 kg = 12_500) so no floating point is ever involved. Conversion happens
 * once at input; stored values are never re-converted, so rounding error cannot accumulate.
 *
 * <p>Range: values up to roughly 10^12 canonical units are safe from {@code long} overflow.
 */
public final class Units {

    public static final long SECOND_MS = 1_000L;
    public static final long MINUTE_MS = 60 * SECOND_MS;
    public static final long HOUR_MS = 60 * MINUTE_MS;

    private Units() {
    }

    /** Integer division rounding half up. Both arguments must be non-negative; denominator positive. */
    public static long roundDiv(long numerator, long denominator) {
        if (numerator < 0 || denominator <= 0) {
            throw new IllegalArgumentException("numerator >= 0 and denominator > 0 required");
        }
        return (numerator + denominator / 2) / denominator;
    }

    /**
     * Countdown-style duration text: {@code mm:ss}, or {@code h:mm:ss} from one hour up.
     * Partial seconds round UP, so a timer shows 00:01 until it truly reaches zero.
     */
    public static String formatDurationCeil(long ms) {
        if (ms < 0) {
            ms = 0;
        }
        long totalSeconds = (ms + SECOND_MS - 1) / SECOND_MS;
        long hours = totalSeconds / 3_600;
        long minutes = (totalSeconds % 3_600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return hours + ":" + pad2(minutes) + ":" + pad2(seconds);
        }
        return pad2(minutes) + ":" + pad2(seconds);
    }

    private static String pad2(long value) {
        return value < 10 ? "0" + value : Long.toString(value);
    }

    /** Distance display units. Canonical value is millimetres. */
    public enum Distance {
        // canonical mm per ONE milli-unit = num / den
        METRE(1L, 1L),             // 1 mm
        KILOMETRE(1_000L, 1L),     // 1 m = 1000 mm
        YARD(9_144L, 10_000L),     // 0.9144 mm
        MILE(1_609_344L, 1_000L);  // 1609.344 mm

        private final long num;
        private final long den;

        Distance(long num, long den) {
            this.num = num;
            this.den = den;
        }

        /** Milli-units of this display unit to canonical millimetres (rounded half up). */
        public long toMm(long milliUnits) {
            return roundDiv(milliUnits * num, den);
        }

        /** Canonical millimetres to milli-units of this display unit (rounded half up). */
        public long fromMm(long mm) {
            return roundDiv(mm * den, num);
        }

        /** Worst-case canonical error after canonical -> display -> canonical. */
        public long halfStepCanonical() {
            return num / (2 * den) + 1;
        }

        /** UCUM code for export files. */
        public String ucum() {
            switch (this) {
                case METRE:
                    return "m";
                case KILOMETRE:
                    return "km";
                case YARD:
                    return "[yd_i]";
                default:
                    return "[mi_i]";
            }
        }
    }

    /** Weight display units. Canonical value is milligrams. */
    public enum Weight {
        GRAM(1L, 1L),                      // 1 mg
        KILOGRAM(1_000L, 1L),              // 1 g = 1000 mg
        POUND(45_359_237L, 100_000L);      // 453.59237 mg

        private final long num;
        private final long den;

        Weight(long num, long den) {
            this.num = num;
            this.den = den;
        }

        public long toMg(long milliUnits) {
            return roundDiv(milliUnits * num, den);
        }

        public long fromMg(long mg) {
            return roundDiv(mg * den, num);
        }

        public long halfStepCanonical() {
            return num / (2 * den) + 1;
        }

        public String ucum() {
            switch (this) {
                case GRAM:
                    return "g";
                case KILOGRAM:
                    return "kg";
                default:
                    return "[lb_av]";
            }
        }
    }
}
