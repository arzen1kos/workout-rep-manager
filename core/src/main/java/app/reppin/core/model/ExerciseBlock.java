package app.reppin.core.model;

import app.reppin.core.units.Units;

/**
 * An exercise slot in a routine: which exercise, how many sets, what each set asks for, and an
 * optional rest that is inserted automatically between sets (SPEC RTN-2). The same exercise can
 * appear in several blocks with different specs.
 */
public final class ExerciseBlock extends RoutineBlock {

    public static final int MAX_SETS = 99;
    public static final long MAX_REST_MS = Units.HOUR_MS;

    private final String exerciseId;
    private final int sets;
    private final WorkSpec spec;
    private final long restBetweenSetsMs;

    public ExerciseBlock(String id, String exerciseId, int sets, WorkSpec spec, long restBetweenSetsMs) {
        super(id);
        if (exerciseId == null || exerciseId.isEmpty()) {
            throw new IllegalArgumentException("exerciseId is required");
        }
        if (sets < 1 || sets > MAX_SETS) {
            throw new IllegalArgumentException("sets must be 1 to " + MAX_SETS);
        }
        if (spec == null) {
            throw new IllegalArgumentException("spec is required");
        }
        if (restBetweenSetsMs < 0 || restBetweenSetsMs > MAX_REST_MS) {
            throw new IllegalArgumentException("rest between sets must be 0 to 1 h");
        }
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.spec = spec;
        this.restBetweenSetsMs = restBetweenSetsMs;
    }

    public String exerciseId() {
        return exerciseId;
    }

    public int sets() {
        return sets;
    }

    public WorkSpec spec() {
        return spec;
    }

    public long restBetweenSetsMs() {
        return restBetweenSetsMs;
    }
}
