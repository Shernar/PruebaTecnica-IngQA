package co.com.prueba.bancolombia.stepdefinitions;

import co.com.prueba.bancolombia.questions.VerifyAddedProducts;
import co.com.prueba.bancolombia.questions.VerifyPriceTotal;
import co.com.prueba.bancolombia.questions.VerifySuccessMesageQuestion;
import co.com.prueba.bancolombia.tasks.AddItemsToShoopingCartTask;
import co.com.prueba.bancolombia.tasks.CheckoutProcessTask;
import co.com.prueba.bancolombia.tasks.LoginTask;
import co.com.prueba.bancolombia.utils.ReadExcel;
import com.codoid.products.exception.FilloException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.GivenWhenThen;
import net.serenitybdd.screenplay.actors.OnStage;

import java.util.List;

public class BuyProductStepDefinitions {

    @When("the user log in")
    public void theUserLogIn() {
        OnStage.theActorInTheSpotlight().attemptsTo(LoginTask.onThePage());
    }

    @When("selects the products to purchase and completes the checkout")
    public void selectsTheProductsToPurchaseAndCompletesTheCheckout() {
        OnStage.theActorInTheSpotlight().attemptsTo(AddItemsToShoopingCartTask.fromPage());
    }

    @Then("they should verify added products")
    public void theyShouldVerifyAddedProducts(DataTable products) throws FilloException {
        List<String> info = products.values();
        List<String> rowExcel = ReadExcel.readRow(info.get(0),info.get(1),info.get(2));
        OnStage.theActorInTheSpotlight().should(GivenWhenThen.seeThat(VerifyAddedProducts.inTheCart(rowExcel)));
        OnStage.theActorInTheSpotlight().attemptsTo(CheckoutProcessTask.execute());
    }

    @Then("the price total")
    public void thePriceTotal(DataTable price) throws FilloException {
        List<String> info = price.values();
        List<String> rowExcel = ReadExcel.readRow(info.get(0),info.get(1),info.get(2));
        OnStage.theActorInTheSpotlight().should(GivenWhenThen.seeThat(VerifyPriceTotal.Overview(rowExcel)));
    }

    @Then("that the message is displayed")
    public void thatTheMessageIsDisplayed(DataTable message) throws FilloException {
        List<String> info = message.values();
        List<String> rowExcel = ReadExcel.readRow(info.get(0),info.get(1),info.get(2));
        OnStage.theActorInTheSpotlight().should(GivenWhenThen.seeThat(VerifySuccessMesageQuestion.displayed(rowExcel)));
    }
}