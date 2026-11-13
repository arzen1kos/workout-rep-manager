package app.reppin.web;

import app.reppin.core.time.Clock;
import app.reppin.core.timer.Countdown;
import app.reppin.core.units.Units;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

/**
 * Walking skeleton: proves the core module runs in the browser, a timestamp-based countdown
 * renders, and the service worker registers. Replace with the real UI (M3+).
 *
 * <p>NOTE: written against the documented TeaVM JSO API but NOT compiled in the environment that
 * generated this boilerplate (no network for Gradle). First task: run
 * {@code ./gradlew :web:assemblePwa} and fix any signature drift.
 */
public final class Main {

    private Main() {
    }

    /** Monotonic clock backed by the browser's performance.now(). */
    private static final Clock BROWSER_CLOCK = () -> (long) perfNowMs();

    @JSBody(script = "return performance.now();")
    private static native double perfNowMs();

    @JSBody(script = "if ('serviceWorker' in navigator) { navigator.serviceWorker.register('sw.js'); }")
    private static native void registerServiceWorker();

    public static void main(String[] args) {
        registerServiceWorker();

        HTMLDocument document = HTMLDocument.current();
        HTMLElement app = document.getElementById("app");

        HTMLElement title = document.createElement("h1");
        title.setTextContent("reppin");
        app.appendChild(title);

        HTMLElement timer = document.createElement("p");
        timer.setClassName("timer");
        app.appendChild(timer);

        HTMLElement units = document.createElement("p");
        units.setTextContent("1 mile = " + Units.Distance.MILE.toMm(1_000) + " mm; 45 lb = "
                + Units.Weight.POUND.toMg(45_000) + " mg");
        app.appendChild(units);

        HTMLElement button = document.createElement("button");
        button.setTextContent("Start 45 s countdown");
        app.appendChild(button);

        Countdown countdown = new Countdown(BROWSER_CLOCK, 45 * Units.SECOND_MS);
        timer.setTextContent(Units.formatDurationCeil(countdown.remainingMs()));

        button.addEventListener("click", event -> {
            if (countdown.state() == Countdown.State.NOT_STARTED) {
                countdown.start();
                button.setTextContent("Pause");
            } else if (countdown.state() == Countdown.State.RUNNING) {
                countdown.pause();
                button.setTextContent("Resume");
            } else {
                countdown.resume();
                button.setTextContent("Pause");
            }
        });

        // The interval only repaints. Truth always comes from the clock, never from tick counts.
        Window.setInterval(
                () -> timer.setTextContent(Units.formatDurationCeil(countdown.remainingMs())), 100);
    }
}
