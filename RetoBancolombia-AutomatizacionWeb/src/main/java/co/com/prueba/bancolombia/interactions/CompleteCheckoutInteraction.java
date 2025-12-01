package co.com.prueba.bancolombia.interactions;

import co.com.prueba.bancolombia.interactions.waits.WaitElementVisibleInteraction;
import co.com.prueba.bancolombia.interactions.waits.WaitMomentInteraction;
import com.github.javafaker.Faker;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;

import java.util.Locale;

import static co.com.prueba.bancolombia.userinterfaces.CheckoutInterface.*;

public class CompleteCheckoutInteraction implements Interaction {

    private final Faker faker = new Faker(Locale.of("es-CO"));

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(Click.on(BTN_CHECKOUT), WaitMomentInteraction.pauseForSeconds(1));
        actor.attemptsTo(WaitElementVisibleInteraction.of(TXT_FIRST_NAME, 2));
        actor.attemptsTo(Enter.theValue(faker.name().firstName()).into(TXT_FIRST_NAME));
        actor.attemptsTo(Enter.theValue(faker.name().lastName()).into(TXT_LAST_NAME));
        actor.attemptsTo(Enter.theValue(faker.address().zipCode()).into(TXT_POSTAL_CODE));
        actor.attemptsTo(Click.on(BTN_CONTINUE));
    }

    public static CompleteCheckoutInteraction now() {
        return new CompleteCheckoutInteraction();
    }
}
