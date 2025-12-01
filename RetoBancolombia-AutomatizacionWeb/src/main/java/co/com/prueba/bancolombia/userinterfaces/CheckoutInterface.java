package co.com.prueba.bancolombia.userinterfaces;

import net.serenitybdd.screenplay.targets.Target;

public class CheckoutInterface {

    private CheckoutInterface() {
    }

    public static final Target BTN_CHECKOUT = Target.the("Button Checkout Cart").locatedBy("//button[@id='checkout']");
    public static final Target TXT_FIRST_NAME = Target.the("Text First Name").locatedBy("//input[@id='first-name']");
    public static final Target TXT_LAST_NAME = Target.the("Text Last Name").locatedBy("//input[@id='last-name']");
    public static final Target TXT_POSTAL_CODE = Target.the("Text Postal Code").locatedBy("//input[@id='postal-code']");
    public static final Target BTN_CONTINUE = Target.the("Button Continue").locatedBy("//input[@id='continue']");
    public static final Target BTN_FINISH = Target.the("Button Finish").locatedBy("//button[@id='finish']");
}
