package app.reppin.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns all tags and enforces the naming rules (SPEC section 4, assumption A6):
 * <ul>
 *   <li>within one scope, names are unique after trim / whitespace collapse / lower-casing;</li>
 *   <li>the same name in different scopes is allowed and means two different tags;</li>
 *   <li>the user may <b>link</b> an exercise tag and a routine tag into one shared tag.</li>
 * </ul>
 * Linking and unlinking change which tag id other objects should point at; the returned results
 * tell the caller which references to remap.
 */
public final class TagRegistry {

    /** Outcome of {@link #link}: routines/exercises referencing {@code removedId} must use {@code survivorId}. */
    public static final class LinkResult {
        private final String survivorId;
        private final String removedId;

        LinkResult(String survivorId, String removedId) {
            this.survivorId = survivorId;
            this.removedId = removedId;
        }

        public String survivorId() {
            return survivorId;
        }

        public String removedId() {
            return removedId;
        }
    }

    private final Map<String, Tag> byId = new LinkedHashMap<>();

    public Tag create(String id, String displayName, TagScope scope) {
        if (byId.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate tag id " + id);
        }
        Tag tag = new Tag(id, displayName, scope);
        if (taken(tag.normalizedName(), scope, null)) {
            throw new TagNameTakenException(tag.displayName(), scope);
        }
        byId.put(id, tag);
        return tag;
    }

    public Tag get(String id) {
        return byId.get(id);
    }

    /** Finds a tag by name within a scope, or null. */
    public Tag find(String name, TagScope scope) {
        String key = Names.normalize(Names.clean(name, Names.MAX_TAG, "Tag name"));
        for (Tag tag : byId.values()) {
            if (tag.hasScope(scope) && tag.normalizedName().equals(key)) {
                return tag;
            }
        }
        return null;
    }

    public List<Tag> all() {
        return Collections.unmodifiableList(new ArrayList<>(byId.values()));
    }

    public List<Tag> inScope(TagScope scope) {
        List<Tag> result = new ArrayList<>();
        for (Tag tag : byId.values()) {
            if (tag.hasScope(scope)) {
                result.add(tag);
            }
        }
        return result;
    }

    /** Renames a tag. The new name must be free in every scope the tag belongs to. */
    public void rename(String id, String newName) {
        Tag tag = require(id);
        String key = Names.normalize(Names.clean(newName, Names.MAX_TAG, "Tag name"));
        for (TagScope scope : tag.scopes()) {
            if (taken(key, scope, id)) {
                throw new TagNameTakenException(newName, scope);
            }
        }
        tag.rename(newName);
    }

    /**
     * Merges an exercise-only tag and a routine-only tag into one tag with both scopes.
     * The exercise tag survives; the routine tag is removed.
     *
     * @param keepExerciseName true to keep the exercise tag's name, false to adopt the routine tag's
     */
    public LinkResult link(String exerciseTagId, String routineTagId, boolean keepExerciseName) {
        Tag exerciseTag = require(exerciseTagId);
        Tag routineTag = require(routineTagId);
        if (exerciseTag.scopes().size() != 1 || !exerciseTag.hasScope(TagScope.EXERCISE)) {
            throw new IllegalArgumentException("First tag must be an exercise-only tag");
        }
        if (routineTag.scopes().size() != 1 || !routineTag.hasScope(TagScope.ROUTINE)) {
            throw new IllegalArgumentException("Second tag must be a routine-only tag");
        }
        Tag nameSource = keepExerciseName ? exerciseTag : routineTag;
        String key = nameSource.normalizedName();
        for (Tag other : byId.values()) {
            if (other == exerciseTag || other == routineTag) {
                continue;
            }
            for (TagScope scope : new TagScope[] {TagScope.EXERCISE, TagScope.ROUTINE}) {
                if (other.hasScope(scope) && other.normalizedName().equals(key)) {
                    throw new TagNameTakenException(nameSource.displayName(), scope);
                }
            }
        }
        exerciseTag.rename(nameSource.displayName());
        exerciseTag.addScope(TagScope.ROUTINE);
        byId.remove(routineTagId);
        return new LinkResult(exerciseTagId, routineTagId);
    }

    /**
     * Splits a linked tag again: the original keeps the exercise scope and a new routine tag with
     * the same name is created. The caller must repoint routines from the original to the result.
     */
    public Tag unlink(String linkedTagId, String newRoutineTagId) {
        Tag tag = require(linkedTagId);
        if (!tag.hasScope(TagScope.EXERCISE) || !tag.hasScope(TagScope.ROUTINE)) {
            throw new IllegalArgumentException("Tag is not linked");
        }
        tag.removeScope(TagScope.ROUTINE);
        return create(newRoutineTagId, tag.displayName(), TagScope.ROUTINE);
    }

    private Tag require(String id) {
        Tag tag = byId.get(id);
        if (tag == null) {
            throw new IllegalArgumentException("Unknown tag " + id);
        }
        return tag;
    }

    private boolean taken(String normalizedName, TagScope scope, String exceptId) {
        for (Tag other : byId.values()) {
            if (other.id().equals(exceptId)) {
                continue;
            }
            if (other.hasScope(scope) && other.normalizedName().equals(normalizedName)) {
                return true;
            }
        }
        return false;
    }
}
