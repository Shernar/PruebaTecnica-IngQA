package co.com.prueba.bancolombia.userinterfaces;

import net.serenitybdd.screenplay.targets.Target;

public class LoginInterface {

    private LoginInterface() {
    }

    public static final Target TXT_USERNAME = Target.the("Text User Name").locatedBy("//input[@id='user-name']");
    public static final Target TXT_PASSWORD = Target.the("Text Password").locatedBy("//input[@id='password']");
    public static final Target BTN_LOGIN = Target.the("Button Login").locatedBy("//input[@id='login-button']");
}