package app.reppin.core.time;

/**
 * Time source port. Everything in core reads time through this interface so the engine is
 * deterministic in tests and independent of the platform.
 *
 * <p>Implementations MUST be monotonic (never go backwards) for timer use. The web module
 * backs this with {@code performance.now()}; tests use a hand-advanced fake.
 */
public interface Clock {

    /** Milliseconds from an arbitrary fixed origin; monotonic. */
    long nowMs();
}
