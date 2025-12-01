package co.com.prueba.bancolombia.interactions.waits;

import net.serenitybdd.model.time.InternalSystemClock;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Tasks;

import java.util.concurrent.TimeUnit;

public class WaitMomentInteraction implements Interaction {

    private final long seconds;

    public WaitMomentInteraction(long seconds) {
        this.seconds = seconds;
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        new InternalSystemClock().pauseFor(seconds);
    }

    public static WaitMomentInteraction pauseForSeconds(long seconds) {
        return Tasks.instrumented(WaitMomentInteraction.class, TimeUnit.SECONDS.toMillis(seconds));
    }
}