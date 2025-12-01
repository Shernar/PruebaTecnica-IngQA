package co.com.pruea.bancolombia.Runners;

import org.junit.jupiter.api.Test;

public class CreateUserRunner {

    private final static String PATH_CREATEUSER = "/src/test/resources/features/createUser.feature";
    private final static String FLOWS = "Create User";

    @Test
    void testUpdateCard() {
        GeneralRunner.testParallel(PATH_CREATEUSER, FLOWS);
    }
}
