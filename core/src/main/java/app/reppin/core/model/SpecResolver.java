package app.reppin.core.model;

/** Default-unit resolution (SPEC 5.5): session override, then routine block spec, then the exercise's last-used spec. */
public final class SpecResolver {

    /** Used when an exercise has never been performed. */
    public static final int FALLBACK_REPS = 10;

    private SpecResolver() {
    }

    public static WorkSpec resolve(WorkSpec sessionOverride, WorkSpec blockSpec, WorkSpec exerciseLastUsed) {
        if (sessionOverride != null) {
            return sessionOverride;
        }
        if (blockSpec != null) {
            return blockSpec;
        }
        if (exerciseLastUsed != null) {
            return exerciseLastUsed;
        }
        return WorkSpec.repsManual(FALLBACK_REPS);
    }

    /** Prefill for a new routine block: what the user last did with this exercise. */
    public static WorkSpec prefillForNewBlock(Exercise exercise) {
        return resolve(null, null, exercise.lastUsedSpec());
    }

    /** Call when a session ends: remembers what was actually performed as the exercise's new default. */
    public static void recordPerformed(Exercise exercise, WorkSpec performed, long nowMs) {
        exercise.setLastUsedSpec(performed, nowMs);
    }
}
