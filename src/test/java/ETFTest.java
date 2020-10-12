import com.intuit.karate.KarateOptions;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;

import com.intuit.karate.junit5.Karate;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@KarateOptions(tags = {"~@ignore"})
public class ETFTest {

    static void generateReport(String karateOutputPath) {

        try (Stream<Path> walk = Files.walk(Paths.get(karateOutputPath))) {

            List<String> result = walk
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(f -> f.endsWith(".json"))
                    .collect(Collectors.toList());

            Configuration config = new Configuration(new File(karateOutputPath), "PDOK ETF TEST");
            ReportBuilder reportBuilder = new ReportBuilder(result, config);
            reportBuilder.generateReports();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testParallel() {
        //Results results = Runner.path(".").parallel(5);
        //results.setReportDir("target/surefire-reports");
        Results results = Runner.parallel(getClass(), 4, "target/surefire-reports");
        generateReport(results.getReportDir());
        assertEquals(results.getFailCount(), 0, results.getErrorMessages());
    }

}
