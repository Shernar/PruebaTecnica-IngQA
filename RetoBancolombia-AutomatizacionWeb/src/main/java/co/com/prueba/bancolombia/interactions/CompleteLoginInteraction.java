package co.com.prueba.bancolombia.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;

import static co.com.prueba.bancolombia.userinterfaces.LoginInterface.*;
import static co.com.prueba.bancolombia.utils.Constants.*;

public class CompleteLoginInteraction implements Interaction {

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(Enter.theValue(USERNAME).into(TXT_USERNAME));
        actor.attemptsTo(Enter.theValue(PASSWORD).into(TXT_PASSWORD));
        actor.attemptsTo(Click.on(BTN_LOGIN));
    }

    public static CompleteLoginInteraction now() {
        return new CompleteLoginInteraction();
    }
}