package co.com.prueba.bancolombia.tasks;

import co.com.prueba.bancolombia.interactions.CompleteCheckoutInteraction;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

public class CheckoutProcessTask implements Task {

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(CompleteCheckoutInteraction.now());
    }

    public static CheckoutProcessTask execute() {
        return Tasks.instrumented(CheckoutProcessTask.class);
    }
}
