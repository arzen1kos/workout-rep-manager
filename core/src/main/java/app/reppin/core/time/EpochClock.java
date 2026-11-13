package app.reppin.core.time;

/** Wall-clock port: milliseconds since the Unix epoch (UTC). Used for timestamps and ids. */
public interface EpochClock {

    long epochMs();
}
