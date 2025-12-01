package co.com.pruea.bancolombia.Runners;

import org.junit.jupiter.api.Test;

public class ParallelRunner {

    private final static String PATH_PARALLEL = "/src/test/resources/features";
    private final static String FLOWS = "test parallel";

    @Test
    void testParallelRunner() {
        GeneralRunner.testParallel(PATH_PARALLEL, FLOWS);
    }
}
