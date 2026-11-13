package app.reppin.core.model;

/** One entry in a routine: either an exercise block or a rest block. */
public abstract class RoutineBlock {

    private final String id;

    RoutineBlock(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("block id is required");
        }
        this.id = id;
    }

    public final String id() {
        return id;
    }
}
