package app.reppin.core.timer;

import app.reppin.core.time.Clock;

/**
 * Timestamp-based countdown (SPEC section 8.6). It never counts ticks: remaining time is derived
 * from the clock each time it is asked, so repaint frequency, throttled background timers and
 * dropped frames cannot cause drift. Pause stores elapsed time; resume continues from it.
 *
 * <p>Not thread-safe; intended for single-threaded UI use.
 */
public final class Countdown {

    public enum State { NOT_STARTED, RUNNING, PAUSED, FINISHED }

    private final Clock clock;
    private long durationMs;
    private long accumulatedMs;
    private long runStartedAtMs;
    private boolean running;
    private boolean started;

    public Countdown(Clock clock, long durationMs) {
        if (durationMs < 0) {
            throw new IllegalArgumentException("durationMs must be >= 0");
        }
        this.clock = clock;
        this.durationMs = durationMs;
    }

    public void start() {
        if (started) {
            return;
        }
        started = true;
        running = true;
        runStartedAtMs = clock.nowMs();
    }

    public void pause() {
        if (!running) {
            return;
        }
        accumulatedMs = elapsedMs();
        running = false;
    }

    public void resume() {
        if (!started || running || isFinished()) {
            return;
        }
        running = true;
        runStartedAtMs = clock.nowMs();
    }

    /** Add (or remove, if negative) time, e.g. the "+15 s" rest control. Never below zero total. */
    public void extendMs(long deltaMs) {
        durationMs = Math.max(0, durationMs + deltaMs);
    }

    /** Active elapsed time, excluding paused time, capped at the duration. */
    public long elapsedMs() {
        long current = accumulatedMs;
        if (running) {
            current += Math.max(0, clock.nowMs() - runStartedAtMs);
        }
        return Math.min(current, durationMs);
    }

    public long remainingMs() {
        return durationMs - elapsedMs();
    }

    public long durationMs() {
        return durationMs;
    }

    public boolean isFinished() {
        return started && remainingMs() == 0;
    }

    public State state() {
        if (!started) {
            return State.NOT_STARTED;
        }
        if (isFinished()) {
            return State.FINISHED;
        }
        return running ? State.RUNNING : State.PAUSED;
    }
}
