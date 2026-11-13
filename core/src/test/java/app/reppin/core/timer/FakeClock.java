package app.reppin.core.timer;

import app.reppin.core.time.Clock;

/** Hand-advanced clock for deterministic tests. */
final class FakeClock implements Clock {

    private long now;

    @Override
    public long nowMs() {
        return now;
    }

    void advance(long ms) {
        now += ms;
    }
}
