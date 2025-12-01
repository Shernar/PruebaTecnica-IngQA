package co.com.prueba.bancolombia.questions;

import co.com.prueba.bancolombia.interactions.waits.WaitElementVisibleInteraction;
import co.com.prueba.bancolombia.userinterfaces.CheckoutInterface;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.RememberThat;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.conditions.Check;

import java.util.List;

import static co.com.prueba.bancolombia.userinterfaces.CheckoutInterface.*;
import static co.com.prueba.bancolombia.userinterfaces.ConfirmOrderInterface.*;
import static co.com.prueba.bancolombia.utils.Constants.*;

public class VerifyPriceTotal implements Question<Boolean> {

    private List<String> rowExcel;

    public VerifyPriceTotal(List<String> rowExcel) {
        this.rowExcel = rowExcel;
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        actor.attemptsTo(WaitElementVisibleInteraction.of(LBL_PRICE_TOTAL, 2));
        actor.attemptsTo(Check.whether(LBL_PRICE_TOTAL.resolveFor(actor).getText().equals(rowExcel.get(3)))
                .andIfSo(RememberThat.theValueOf(RESULT_VIEW_MESSAGE_CART).is(true))
                .otherwise(RememberThat.theValueOf(RESULT_VIEW_MESSAGE_CART).is(false)));
        actor.attemptsTo(Click.on(BTN_FINISH));

        return actor.recall(RESULT_VIEW_MESSAGE_CART);
    }

    public static VerifyPriceTotal Overview(List<String> rowExcel) {
        return new VerifyPriceTotal(rowExcel);
    }
}
