package app.reppin.core.id;

import app.reppin.core.time.EpochClock;

/** Hands out new UUIDv7 ids from injected time and randomness. */
public final class IdGenerator {

    private final EpochClock clock;
    private final RandomSource random;

    public IdGenerator(EpochClock clock, RandomSource random) {
        this.clock = clock;
        this.random = random;
    }

    public String next() {
        return Uuid7.generate(clock.epochMs(), random);
    }
}
