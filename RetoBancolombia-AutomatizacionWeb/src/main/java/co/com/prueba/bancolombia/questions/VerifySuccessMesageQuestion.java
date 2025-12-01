package co.com.prueba.bancolombia.questions;

import co.com.prueba.bancolombia.interactions.waits.WaitElementVisibleInteraction;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.RememberThat;
import net.serenitybdd.screenplay.conditions.Check;

import java.util.List;

import static co.com.prueba.bancolombia.userinterfaces.ConfirmOrderInterface.*;
import static co.com.prueba.bancolombia.utils.Constants.*;

public class VerifySuccessMesageQuestion implements Question<Boolean> {

    private List<String> rowExcel;

    public VerifySuccessMesageQuestion(List<String> rowExcel) {
        this.rowExcel = rowExcel;
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        actor.attemptsTo(WaitElementVisibleInteraction.of(LBL_TITLE_SUCCESS, 2));
        actor.attemptsTo(Check.whether(LBL_TITLE_SUCCESS.resolveFor(actor).getText().equals(rowExcel.get(4)))
                .andIfSo(RememberThat.theValueOf(RESULT_VIEW_MESSAGE_CART).is(true))
                .otherwise(RememberThat.theValueOf(RESULT_VIEW_MESSAGE_CART).is(false)));

        return actor.recall(RESULT_VIEW_MESSAGE_CART);
    }

    public static VerifySuccessMesageQuestion displayed(List<String> rowExcel) {
        return new VerifySuccessMesageQuestion(rowExcel);
    }
}
