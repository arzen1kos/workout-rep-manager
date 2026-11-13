package app.reppin.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A routine (group): a reusable template of ordered blocks (SPEC 6.2). The same exercise may be
 * used many times. Never deleted, only archived. Running it creates a Session (later milestone).
 */
public final class Routine {

    public static final long NOT_ARCHIVED = -1;

    private final String id;
    private String name;
    private String description;
    private final Set<String> tagIds = new LinkedHashSet<>();
    private final List<RoutineBlock> blocks = new ArrayList<>();
    private long archivedAtMs = NOT_ARCHIVED;
    private final long createdAtMs;
    private long updatedAtMs;

    public Routine(String id, String name, long nowMs) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id is required");
        }
        this.id = id;
        this.name = Names.clean(name, Names.MAX_NAME, "Routine name");
        this.createdAtMs = nowMs;
        this.updatedAtMs = nowMs;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Set<String> tagIds() {
        return Collections.unmodifiableSet(tagIds);
    }

    public List<RoutineBlock> blocks() {
        return Collections.unmodifiableList(blocks);
    }

    public boolean isArchived() {
        return archivedAtMs != NOT_ARCHIVED;
    }

    public long createdAtMs() {
        return createdAtMs;
    }

    public long updatedAtMs() {
        return updatedAtMs;
    }

    public void setName(String newName, long nowMs) {
        this.name = Names.clean(newName, Names.MAX_NAME, "Routine name");
        touch(nowMs);
    }

    public void setDescription(String text, long nowMs) {
        this.description = Names.optionalText(text, Names.MAX_DESCRIPTION, "Description");
        touch(nowMs);
    }

    public void addTag(String tagId, long nowMs) {
        if (tagIds.add(tagId)) {
            touch(nowMs);
        }
    }

    public void removeTag(String tagId, long nowMs) {
        if (tagIds.remove(tagId)) {
            touch(nowMs);
        }
    }

    public void archive(long nowMs) {
        if (!isArchived()) {
            archivedAtMs = nowMs;
            touch(nowMs);
        }
    }

    public void unarchive(long nowMs) {
        if (isArchived()) {
            archivedAtMs = NOT_ARCHIVED;
            touch(nowMs);
        }
    }

    public void addBlock(RoutineBlock block, long nowMs) {
        insertBlock(blocks.size(), block, nowMs);
    }

    public void insertBlock(int index, RoutineBlock block, long nowMs) {
        if (indexOf(block.id()) >= 0) {
            throw new IllegalArgumentException("Duplicate block id " + block.id());
        }
        if (index < 0 || index > blocks.size()) {
            throw new IndexOutOfBoundsException("index " + index);
        }
        blocks.add(index, block);
        touch(nowMs);
    }

    public void removeBlock(String blockId, long nowMs) {
        int index = requireIndex(blockId);
        blocks.remove(index);
        touch(nowMs);
    }

    /** Moves a block so it ends up at {@code newIndex} in the resulting list (0..size-1). */
    public void moveBlock(String blockId, int newIndex, long nowMs) {
        int from = requireIndex(blockId);
        if (newIndex < 0 || newIndex >= blocks.size()) {
            throw new IndexOutOfBoundsException("index " + newIndex);
        }
        RoutineBlock block = blocks.remove(from);
        blocks.add(newIndex, block);
        touch(nowMs);
    }

    /**
     * Summary panel data. Rest between sets counts between sets only (not after the last set), as
     * in the session expansion in SPEC 8.1.
     *
     * @param estMsPerRep assumed ms per manually counted rep (SPEC A11 default 4000)
     */
    public RoutineSummary summarize(long estMsPerRep) {
        List<RoutineSummary.Line> lines = new ArrayList<>();
        int totalSets = 0;
        long totalRepEquivalents = 0;
        long estimatedMs = 0;
        boolean incomplete = false;
        for (RoutineBlock block : blocks) {
            if (block instanceof ExerciseBlock) {
                ExerciseBlock eb = (ExerciseBlock) block;
                int perSet = eb.spec().repEquivalents();
                lines.add(new RoutineSummary.Line(eb.id(), eb.exerciseId(), eb.sets(), perSet));
                totalSets += eb.sets();
                totalRepEquivalents += (long) eb.sets() * perSet;
                long setMs = eb.spec().estimateMs(estMsPerRep);
                if (setMs < 0) {
                    incomplete = true;
                } else {
                    estimatedMs += eb.sets() * setMs;
                }
                estimatedMs += (long) (eb.sets() - 1) * eb.restBetweenSetsMs();
            } else {
                estimatedMs += ((RestBlock) block).durationMs();
            }
        }
        return new RoutineSummary(lines, totalSets, totalRepEquivalents, estimatedMs, incomplete);
    }

    private int indexOf(String blockId) {
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).id().equals(blockId)) {
                return i;
            }
        }
        return -1;
    }

    private int requireIndex(String blockId) {
        int index = indexOf(blockId);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown block " + blockId);
        }
        return index;
    }

    private void touch(long nowMs) {
        this.updatedAtMs = nowMs;
    }
}
