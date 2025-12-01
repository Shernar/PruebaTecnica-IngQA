package co.com.prueba.bancolombia.enums;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Arrays;
import java.util.EmptyStackException;

import static co.com.prueba.bancolombia.utils.Constants.*;

public enum DriverEnum {

    CHROME("chrome") {
        @Override
        public WebDriver execute() {
            WebDriver webDriver;
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--incognito");
            options.addArguments(IGNORE_CERTIFICATES_ERROR);
            options.addArguments(IGNORE_CERTIFICATES_ERROR_SSL);
            options.addArguments(REMOTE_ALLOW_ORIGINS);
            options.addArguments("force-device-scale-factor=0.80", "high-dpi-support=0.80");
            webDriver = new ChromeDriver(options);
            webDriver.manage().window().maximize();
            webDriver.manage().deleteAllCookies();
            return webDriver;
        }
    };

    private String driver;

    public String getDriver() {
        return driver;
    }

    DriverEnum(String driver) {
        this.driver = driver;
    }

    public static DriverEnum driverEnum(String driver) {
        return Arrays.stream(DriverEnum.values()).filter(select -> select.getDriver().equalsIgnoreCase(driver)).findFirst().orElseThrow(EmptyStackException::new);
    }

    public abstract WebDriver execute();
}
