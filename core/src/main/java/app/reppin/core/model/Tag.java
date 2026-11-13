package app.reppin.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** A label. A tag with both scopes is a "linked" tag shared by exercises and routines. */
public final class Tag {

    private final String id;
    private String displayName;
    private String normalizedName;
    private final Set<TagScope> scopes = new HashSet<>();

    Tag(String id, String displayName, TagScope scope) {
        this.id = id;
        rename(displayName);
        this.scopes.add(scope);
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String normalizedName() {
        return normalizedName;
    }

    public Set<TagScope> scopes() {
        return Collections.unmodifiableSet(scopes);
    }

    public boolean hasScope(TagScope scope) {
        return scopes.contains(scope);
    }

    void rename(String newName) {
        this.displayName = Names.clean(newName, Names.MAX_TAG, "Tag name");
        this.normalizedName = Names.normalize(this.displayName);
    }

    void addScope(TagScope scope) {
        scopes.add(scope);
    }

    void removeScope(TagScope scope) {
        scopes.remove(scope);
    }
}
