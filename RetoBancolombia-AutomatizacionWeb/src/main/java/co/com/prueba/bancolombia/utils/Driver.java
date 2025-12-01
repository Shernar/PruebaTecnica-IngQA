package co.com.prueba.bancolombia.utils;

import lombok.Getter;
import org.openqa.selenium.WebDriver;
import co.com.prueba.bancolombia.enums.DriverEnum;

public class Driver {
    private Driver() {
    }

    @Getter
    private static WebDriver driverBrowser;

    public static WebDriver onUrl(String url, String navegador) {
        driverBrowser = DriverEnum.driverEnum(navegador).execute();
        driverBrowser.get(url);
        return driverBrowser;
    }
}
