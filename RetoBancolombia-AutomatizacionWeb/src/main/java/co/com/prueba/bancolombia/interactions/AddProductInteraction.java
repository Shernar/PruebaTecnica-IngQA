package co.com.prueba.bancolombia.interactions;

import co.com.prueba.bancolombia.interactions.waits.WaitMomentInteraction;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.targets.Target;

import static co.com.prueba.bancolombia.userinterfaces.SwagLabsInterface.*;

public class AddProductInteraction implements Interaction {

    private Target addToCart;

    public AddProductInteraction(Target addToCart) {
        this.addToCart = addToCart;
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        for (int i = 0; i <= 2; i++) {
            if (BTN_ADD_TO_CART.of(String.valueOf(i)).resolveFor(actor).isClickable()) {
                actor.attemptsTo(Click.on(addToCart.of(String.valueOf(i))), WaitMomentInteraction.pauseForSeconds(1));
            }
        }
    }

    public static AddProductInteraction fromCatalog(Target addToCart) {
        return Tasks.instrumented(AddProductInteraction.class, addToCart);
    }
}