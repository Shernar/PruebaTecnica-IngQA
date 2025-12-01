package co.com.prueba.bancolombia.questions;

import co.com.prueba.bancolombia.interactions.waits.WaitElementVisibleInteraction;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.RememberThat;
import net.serenitybdd.screenplay.conditions.Check;

import java.util.List;

import static co.com.prueba.bancolombia.userinterfaces.ConfirmOrderInterface.*;
import static co.com.prueba.bancolombia.utils.Constants.*;


public class VerifyAddedProducts implements Question<Boolean> {

    private List<String> rowExcel;

    public VerifyAddedProducts(List<String> rowExcel) {
        this.rowExcel = rowExcel;
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        actor.attemptsTo(WaitElementVisibleInteraction.of(LBL_TITLE_PRODUCTS, 2));
        actor.attemptsTo(Check.whether(LBL_TITLE_PRODUCTS.resolveFor(actor).getText().equals(rowExcel.get(1)))
                .andIfSo(RememberThat.theValueOf(RESULT_VIEW_MESSAGE_CART).is(true))
                .otherwise(RememberThat.theValueOf(RESULT_VIEW_MESSAGE_CART).is(false)));

        return actor.recall(RESULT_VIEW_MESSAGE_CART);
    }

    public static VerifyAddedProducts inTheCart(List<String> rowExcel) {
        return new VerifyAddedProducts(rowExcel);
    }
}
