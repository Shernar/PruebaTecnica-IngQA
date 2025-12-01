package co.com.prueba.bancolombia.stepdefinitions;

import co.com.prueba.bancolombia.utils.Driver;
import co.com.prueba.bancolombia.utils.ReadExcel;
import com.codoid.products.exception.FilloException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;

import java.util.List;

import static co.com.prueba.bancolombia.utils.Constants.*;

public class Hook {

    @Before
    public void inicializeActor() {
        OnStage.setTheStage(new OnlineCast());
        OnStage.theActor("El usuario");
    }

    @Given("the user opens the browser")
    public void theUserOpensTheBrowser(DataTable data) throws FilloException {
        List<String> info = data.values();
        List<String> rowExcel = ReadExcel.readRow(info.get(0), info.get(1), info.get(2));
        OnStage.theActorInTheSpotlight().can(BrowseTheWeb.with(Driver.onUrl(URL, rowExcel.get(0))));
    }
}