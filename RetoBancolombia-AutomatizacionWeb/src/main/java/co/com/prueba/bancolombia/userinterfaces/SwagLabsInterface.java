package co.com.prueba.bancolombia.userinterfaces;

import net.serenitybdd.screenplay.targets.Target;

public class SwagLabsInterface {

    private SwagLabsInterface() {
    }

    public static final Target BTN_ADD_TO_CART = Target.the("Button add to Cart").locatedBy("(//button[contains(@id,'add-to-cart-sauce-labs')])[{0}]");
    public static final Target BTN_CART = Target.the("Button Cart").locatedBy("//div[@id='shopping_cart_container']");
}