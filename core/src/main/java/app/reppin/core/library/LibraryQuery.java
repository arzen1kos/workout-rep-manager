package app.reppin.core.library;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Library search and filter request (SPEC LIB-3). Fluent setters; defaults show active cards with no filter. */
public final class LibraryQuery {

    private String text = "";
    private final Set<String> tagIds = new LinkedHashSet<>();
    private boolean matchAnyTag;
    private boolean includeArchived;

    public LibraryQuery text(String value) {
        this.text = value == null ? "" : value;
        return this;
    }

    public LibraryQuery requireTag(String tagId) {
        tagIds.add(tagId);
        return this;
    }

    /** false (default): a card must have ALL selected tags. true: ANY selected tag is enough. */
    public LibraryQuery matchAnyTag(boolean value) {
        this.matchAnyTag = value;
        return this;
    }

    public LibraryQuery includeArchived(boolean value) {
        this.includeArchived = value;
        return this;
    }

    String text() {
        return text;
    }

    Set<String> tagIds() {
        return Collections.unmodifiableSet(tagIds);
    }

    boolean matchAnyTag() {
        return matchAnyTag;
    }

    boolean includeArchived() {
        return includeArchived;
    }
}
