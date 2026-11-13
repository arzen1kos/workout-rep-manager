package app.reppin.core.model;

import app.reppin.core.units.Units;

/** A standalone rest between blocks (SPEC RTN-2). */
public final class RestBlock extends RoutineBlock {

    public static final long MAX_REST_MS = Units.HOUR_MS;

    private final long durationMs;

    public RestBlock(String id, long durationMs) {
        super(id);
        if (durationMs < Units.SECOND_MS || durationMs > MAX_REST_MS) {
            throw new IllegalArgumentException("rest must be 1 s to 1 h");
        }
        this.durationMs = durationMs;
    }

    public long durationMs() {
        return durationMs;
    }
}
