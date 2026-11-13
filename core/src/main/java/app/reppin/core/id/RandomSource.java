package app.reppin.core.id;

/** Randomness port so id generation is deterministic in tests. Web backs it with crypto.getRandomValues. */
public interface RandomSource {

    long nextLong();
}
