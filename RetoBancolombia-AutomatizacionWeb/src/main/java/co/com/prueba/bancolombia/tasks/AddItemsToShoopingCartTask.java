package co.com.prueba.bancolombia.tasks;

import co.com.prueba.bancolombia.interactions.AddProductInteraction;
import co.com.prueba.bancolombia.interactions.waits.WaitMomentInteraction;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.actions.Click;

import static co.com.prueba.bancolombia.userinterfaces.SwagLabsInterface.*;

public class AddItemsToShoopingCartTask implements Task {

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(AddProductInteraction.fromCatalog(BTN_ADD_TO_CART));
        actor.attemptsTo(Click.on(BTN_CART),(WaitMomentInteraction.pauseForSeconds(1)));
    }

    public static AddItemsToShoopingCartTask fromPage() {
        return Tasks.instrumented(AddItemsToShoopingCartTask.class);
    }
}