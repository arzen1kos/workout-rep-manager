package app.reppin.core.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A library card (SPEC 6.1). Mutable entity; every mutator takes the current epoch ms so
 * {@code updatedAtMs} stays truthful and tests stay deterministic. Never deleted, only archived.
 */
public final class Exercise {

    public static final long NOT_ARCHIVED = -1;

    private final String id;
    private String name;
    private String description;
    private String instructionUrl;
    private String thumbnailRef;
    private final Set<String> tagIds = new LinkedHashSet<>();
    private WorkSpec lastUsedSpec;
    private long archivedAtMs = NOT_ARCHIVED;
    private final long createdAtMs;
    private long updatedAtMs;

    public Exercise(String id, String name, long nowMs) {
        this.id = requireId(id);
        this.name = Names.clean(name, Names.MAX_NAME, "Exercise name");
        this.createdAtMs = nowMs;
        this.updatedAtMs = nowMs;
    }

    private static String requireId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id is required");
        }
        return id;
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

    public String instructionUrl() {
        return instructionUrl;
    }

    /** Reference (key) of the thumbnail blob in media storage; null if none. */
    public String thumbnailRef() {
        return thumbnailRef;
    }

    public Set<String> tagIds() {
        return Collections.unmodifiableSet(tagIds);
    }

    /** The spec most recently performed for this exercise; null until it has been used. */
    public WorkSpec lastUsedSpec() {
        return lastUsedSpec;
    }

    public boolean isArchived() {
        return archivedAtMs != NOT_ARCHIVED;
    }

    public long archivedAtMs() {
        return archivedAtMs;
    }

    public long createdAtMs() {
        return createdAtMs;
    }

    public long updatedAtMs() {
        return updatedAtMs;
    }

    public void setName(String newName, long nowMs) {
        this.name = Names.clean(newName, Names.MAX_NAME, "Exercise name");
        touch(nowMs);
    }

    public void setDescription(String text, long nowMs) {
        this.description = Names.optionalText(text, Names.MAX_DESCRIPTION, "Description");
        touch(nowMs);
    }

    public void setInstructionUrl(String url, long nowMs) {
        this.instructionUrl = Names.optionalUrl(url);
        touch(nowMs);
    }

    public void setThumbnailRef(String ref, long nowMs) {
        this.thumbnailRef = (ref == null || ref.isEmpty()) ? null : ref;
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

    public void setLastUsedSpec(WorkSpec spec, long nowMs) {
        this.lastUsedSpec = spec;
        touch(nowMs);
    }

    public void archive(long nowMs) {
        if (!isArchived()) {
            this.archivedAtMs = nowMs;
            touch(nowMs);
        }
    }

    public void unarchive(long nowMs) {
        if (isArchived()) {
            this.archivedAtMs = NOT_ARCHIVED;
            touch(nowMs);
        }
    }

    /** Copy for the "duplicate card" action: new id, name + " (copy)" (trimmed to fit), never archived. */
    public Exercise duplicate(String newId, long nowMs) {
        String suffix = " (copy)";
        String base = name.length() + suffix.length() > Names.MAX_NAME
                ? name.substring(0, Names.MAX_NAME - suffix.length()) : name;
        Exercise copy = new Exercise(newId, base + suffix, nowMs);
        copy.description = description;
        copy.instructionUrl = instructionUrl;
        copy.thumbnailRef = thumbnailRef;
        copy.tagIds.addAll(tagIds);
        copy.lastUsedSpec = lastUsedSpec;
        return copy;
    }

    private void touch(long nowMs) {
        this.updatedAtMs = nowMs;
    }
}
