package app.reppin.core.timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CountdownTest {

    @Test
    void runsDownFromTheClockNotFromTicks() {
        FakeClock clock = new FakeClock();
        Countdown c = new Countdown(clock, 45_000);
        c.start();
        clock.advance(12_345);
        assertEquals(32_655, c.remainingMs());
        assertEquals(Countdown.State.RUNNING, c.state());
    }

    @Test
    void pausedTimeIsExcluded() {
        FakeClock clock = new FakeClock();
        Countdown c = new Countdown(clock, 60_000);
        c.start();
        clock.advance(10_000);
        c.pause();
        clock.advance(300_000); // phone locked for five minutes
        assertEquals(50_000, c.remainingMs());
        assertEquals(Countdown.State.PAUSED, c.state());
        c.resume();
        clock.advance(5_000);
        assertEquals(45_000, c.remainingMs());
    }

    @Test
    void finishesAtZeroAndStaysThere() {
        FakeClock clock = new FakeClock();
        Countdown c = new Countdown(clock, 3_000);
        c.start();
        clock.advance(10_000); // late repaint after tab throttling
        assertTrue(c.isFinished());
        assertEquals(0, c.remainingMs());
        assertEquals(3_000, c.elapsedMs());
        assertEquals(Countdown.State.FINISHED, c.state());
    }

    @Test
    void extendAddsRestTime() {
        FakeClock clock = new FakeClock();
        Countdown c = new Countdown(clock, 30_000);
        c.start();
        clock.advance(25_000);
        c.extendMs(15_000);
        assertEquals(20_000, c.remainingMs());
        assertFalse(c.isFinished());
    }

    @Test
    void notStartedUntilStartIsCalled() {
        FakeClock clock = new FakeClock();
        Countdown c = new Countdown(clock, 5_000);
        clock.advance(2_000);
        assertEquals(Countdown.State.NOT_STARTED, c.state());
        assertEquals(5_000, c.remainingMs());
    }
}
