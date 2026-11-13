package app.reppin.core.model;

import app.reppin.core.units.Units;

/**
 * What one set of an exercise asks for (SPEC section 5.2): exactly one work measure plus an
 * optional load (weight). Immutable. All quantities are canonical integers (ms, mm, mg).
 */
public final class WorkSpec {

    public enum Measure { REPS_MANUAL, REPS_PACED, DURATION, DISTANCE }

    public enum DistanceMode { BLOCKS, LAPS }

    public static final int MAX_REPS = 10_000;
    public static final long MIN_INTERVAL_MS = 500;
    public static final long MAX_INTERVAL_MS = 60_000;
    public static final long DEFAULT_COUNT_IN_MS = 3_000;
    public static final long MAX_COUNT_IN_MS = 30_000;
    public static final long MAX_DURATION_MS = 24 * Units.HOUR_MS;
    public static final int MAX_UNIT_COUNT = 1_000;
    public static final long MAX_UNIT_LENGTH_MM = 100_000_000L; // 100 km
    public static final long MAX_WEIGHT_MG = 1_000_000_000L;    // 1,000 kg

    private final Measure measure;
    private final int reps;
    private final long intervalMs;
    private final long countInMs;
    private final long durationMs;
    private final DistanceMode distanceMode;
    private final long unitLengthMm;
    private final int unitCount;
    private final long weightMg;

    private WorkSpec(Measure measure, int reps, long intervalMs, long countInMs, long durationMs,
                     DistanceMode distanceMode, long unitLengthMm, int unitCount, long weightMg) {
        this.measure = measure;
        this.reps = reps;
        this.intervalMs = intervalMs;
        this.countInMs = countInMs;
        this.durationMs = durationMs;
        this.distanceMode = distanceMode;
        this.unitLengthMm = unitLengthMm;
        this.unitCount = unitCount;
        this.weightMg = weightMg;
    }

    public static WorkSpec repsManual(int reps) {
        requireReps(reps);
        return new WorkSpec(Measure.REPS_MANUAL, reps, 0, 0, 0, null, 0, 0, 0);
    }

    public static WorkSpec repsPaced(int reps, long intervalMs, long countInMs) {
        requireReps(reps);
        if (intervalMs < MIN_INTERVAL_MS || intervalMs > MAX_INTERVAL_MS) {
            throw new IllegalArgumentException("interval must be 0.5 s to 60 s");
        }
        if (countInMs < 0 || countInMs > MAX_COUNT_IN_MS) {
            throw new IllegalArgumentException("count-in must be 0 to 30 s");
        }
        return new WorkSpec(Measure.REPS_PACED, reps, intervalMs, countInMs, 0, null, 0, 0, 0);
    }

    public static WorkSpec duration(long durationMs) {
        if (durationMs < Units.SECOND_MS || durationMs > MAX_DURATION_MS) {
            throw new IllegalArgumentException("duration must be 1 s to 24 h");
        }
        return new WorkSpec(Measure.DURATION, 0, 0, 0, durationMs, null, 0, 0, 0);
    }

    /** {@code unitCount} whole blocks or laps of {@code unitLengthMm} each (SPEC 5.3: never divided or rounded). */
    public static WorkSpec distance(DistanceMode mode, long unitLengthMm, int unitCount) {
        if (mode == null) {
            throw new IllegalArgumentException("distance mode is required");
        }
        if (unitLengthMm < 1 || unitLengthMm > MAX_UNIT_LENGTH_MM) {
            throw new IllegalArgumentException("unit length out of range");
        }
        if (unitCount < 1 || unitCount > MAX_UNIT_COUNT) {
            throw new IllegalArgumentException("count must be 1 to " + MAX_UNIT_COUNT);
        }
        return new WorkSpec(Measure.DISTANCE, 0, 0, 0, 0, mode, unitLengthMm, unitCount, 0);
    }

    /** Copy with a load. Pass 0 to remove the load. */
    public WorkSpec withWeightMg(long mg) {
        if (mg < 0 || mg > MAX_WEIGHT_MG) {
            throw new IllegalArgumentException("weight out of range");
        }
        return new WorkSpec(measure, reps, intervalMs, countInMs, durationMs, distanceMode,
                unitLengthMm, unitCount, mg);
    }

    private static void requireReps(int reps) {
        if (reps < 1 || reps > MAX_REPS) {
            throw new IllegalArgumentException("reps must be 1 to " + MAX_REPS);
        }
    }

    public Measure measure() {
        return measure;
    }

    public int reps() {
        return reps;
    }

    public long intervalMs() {
        return intervalMs;
    }

    public long countInMs() {
        return countInMs;
    }

    public long durationMs() {
        return durationMs;
    }

    public DistanceMode distanceMode() {
        return distanceMode;
    }

    public long unitLengthMm() {
        return unitLengthMm;
    }

    public int unitCount() {
        return unitCount;
    }

    public long weightMg() {
        return weightMg;
    }

    public boolean hasLoad() {
        return weightMg > 0;
    }

    /** Total distance for DISTANCE specs (count x length), else 0. */
    public long totalDistanceMm() {
        return measure == Measure.DISTANCE ? unitLengthMm * unitCount : 0;
    }

    /**
     * Rep equivalents (SPEC 5.4), for display and completion only: reps for rep measures, 1 for a
     * completed duration, one per block/lap for distance.
     */
    public int repEquivalents() {
        switch (measure) {
            case REPS_MANUAL:
            case REPS_PACED:
                return reps;
            case DURATION:
                return 1;
            default:
                return unitCount;
        }
    }

    /**
     * Estimated time for one set, in ms, or -1 when it cannot be known (distance has no pace data yet).
     *
     * @param estMsPerRep assumed time per manually counted rep (SPEC A11: default 4000)
     */
    public long estimateMs(long estMsPerRep) {
        switch (measure) {
            case REPS_MANUAL:
                return reps * estMsPerRep;
            case REPS_PACED:
                return countInMs + reps * intervalMs;
            case DURATION:
                return durationMs;
            default:
                return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WorkSpec)) {
            return false;
        }
        WorkSpec s = (WorkSpec) o;
        return measure == s.measure && reps == s.reps && intervalMs == s.intervalMs
                && countInMs == s.countInMs && durationMs == s.durationMs
                && distanceMode == s.distanceMode && unitLengthMm == s.unitLengthMm
                && unitCount == s.unitCount && weightMg == s.weightMg;
    }

    @Override
    public int hashCode() {
        long h = measure.ordinal();
        h = h * 31 + reps;
        h = h * 31 + intervalMs;
        h = h * 31 + countInMs;
        h = h * 31 + durationMs;
        h = h * 31 + (distanceMode == null ? 0 : distanceMode.ordinal() + 1);
        h = h * 31 + unitLengthMm;
        h = h * 31 + unitCount;
        h = h * 31 + weightMg;
        return (int) (h ^ (h >>> 32));
    }

    @Override
    public String toString() {
        return "WorkSpec{" + measure + ", reps=" + reps + ", durationMs=" + durationMs
                + ", units=" + unitCount + "x" + unitLengthMm + "mm, weightMg=" + weightMg + "}";
    }
}
