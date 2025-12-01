package co.com.prueba.bancolombia.tasks;

import co.com.prueba.bancolombia.interactions.CompleteLoginInteraction;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

public class LoginTask implements Task {

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(CompleteLoginInteraction.now());
    }

    public static LoginTask onThePage() {
        return Tasks.instrumented(LoginTask.class);
    }

}