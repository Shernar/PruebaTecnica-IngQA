package co.com.pruea.bancolombia.Runners;

import org.junit.jupiter.api.Test;

public class GetUsersRunner {

    private final static String PATH_GETUSER = "/src/test/resources/features/getUsers.feature";
    private final static String FLOWS = "Get User";

    @Test
    void testUpdateCard() {
        GeneralRunner.testParallel(PATH_GETUSER, FLOWS);
    }
}
