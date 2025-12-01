package co.com.pruea.bancolombia.Runners;

import org.junit.jupiter.api.Test;

public class UpdateUserRunner {

    private final static String PATH_UPDATEUSER = "/src/test/resources/features/updateUser.feature";
    private final static String FLOWS = "Update User";

    @Test
    void testUpdateCard() {
        GeneralRunner.testParallel(PATH_UPDATEUSER, FLOWS);
    }
}
