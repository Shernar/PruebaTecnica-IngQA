package co.com.prueba.bancolombia.userinterfaces;

import net.serenitybdd.screenplay.targets.Target;

public class ConfirmOrderInterface {

    private ConfirmOrderInterface() {
    }

    public static final Target LBL_TITLE_PRODUCTS = Target.the("Label Title Products").locatedBy("(//div[contains(@class, 'inventory_item_name')])");
    public static final Target LBL_PRICE_TOTAL = Target.the("Label Price Total").locatedBy("(//div[contains(@class, 'summary_total_label')])");
    public static final Target LBL_TITLE_SUCCESS = Target.the("Label Title Success").locatedBy("(//h2[contains(@class, 'complete-header')])");
}
