package app.reppin.core.model;

/** Thrown when a tag name already exists in the same scope. */
public final class TagNameTakenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TagNameTakenException(String name, TagScope scope) {
        super("A " + scope + " tag named '" + name + "' already exists");
    }
}
