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

import csw.CSWClient;
import csw.CSWClientException;
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
    public void testParallel() throws CSWClientException, IOException {
        CSWClient.GenerateJSONFromMetadataRecords();

        String timestamp = String.valueOf(java.lang.System.currentTimeMillis());

        // PROPERTIES:
        System.setProperty("StaticUri", "http://localhost:63342/etf-validation/");
        System.setProperty("baseURL", "https://inspire.ec.europa.eu/validator/v2/");
        System.setProperty("reportDir", "reports/" + timestamp);
        System.setProperty("atomRecords", "../../../atom-inspire-records.json");
        System.setProperty("wmsRecords", "../../../wms-inspire-records.json");
        System.setProperty("wfsRecords", "../../../wfs-inspire-records.json");

        Results results = Runner.parallel(getClass(), 4, "target/surefire-reports");
        generateReport(results.getReportDir());
        createSymbolicLink("reports/latest", timestamp);

        assertEquals(results.getFailCount(), 0, results.getErrorMessages());
    }

    public void createSymbolicLink(String from, String to) throws IOException {
        Path linkPath = Paths.get(from);
        Path targetPath = Paths.get(to);
        if (Files.exists(linkPath)) {
            Files.delete(linkPath);
        }
        Files.createSymbolicLink(linkPath, targetPath);
    }
}
