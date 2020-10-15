package report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ETFReport {

    private final MustacheContext context;
    private final String baseUrl;
    private final String feature;
    private final String baseReportDir;
    private final String reportDir;
    private final String timestamp = String.valueOf(java.lang.System.currentTimeMillis());

    public ETFReport(String baseUrl, String reportDir, String feature) {
        this.baseUrl = baseUrl;
        this.baseReportDir = reportDir;
        this.feature = feature;
        this.reportDir = reportDir + "/" + timestamp + "/";
        this.context = new MustacheContext(feature);
        new File(this.reportDir).mkdirs();
    }

    public void downloadStatus(
            String statusPath, String status, String protocol, String label, String title, String uuid, String serviceAccessPoint, String metadataStandardVersion, String getRecordByIdUrl) {
        String htmlPath = buildPath(label, ".html");
        String logPath = buildPath(label, ".log");
        download(statusPath + "/log", logPath);
        download(statusPath + ".html", htmlPath);

        context.records.add(new MustacheContext.Record(label, title, protocol, uuid, serviceAccessPoint, status, metadataStandardVersion, getRecordByIdUrl));
    }

    public void close() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File(buildPath(feature, ".json")), context.records);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writeTemplate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public void writeTemplate() throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(String.format("report/%sReport.mustache", feature));
        File outputPath = new File(buildPath(feature, ".html"));
        Writer writer = new OutputStreamWriter(new FileOutputStream(outputPath));
        mustache.execute(writer, context).flush();
    }

    private String buildPath(String label, String extension){
        return String.format("%s%s%s", reportDir, label, extension);
    };

    private void download(String target, String filePath) {
        String targetUrl = baseUrl + target;

        URL urlObj = null;
        ReadableByteChannel rbcObj = null;
        FileOutputStream fOutStream  = null;

        // Checking If The File Exists At The Specified Location Or Not
        Path filePathObj = Paths.get(filePath);
        boolean fileExists = Files.exists(filePathObj);
        if(!fileExists) {
            try {
                urlObj = new URL(targetUrl);
                rbcObj = Channels.newChannel(urlObj.openStream());
                fOutStream = new FileOutputStream(filePath);

                fOutStream.getChannel().transferFrom(rbcObj, 0, Long.MAX_VALUE);
                System.out.println("Downloaded: " + targetUrl);
            } catch (IOException ioExObj) {
                System.out.println("Problem Occured While Downloading The File= " + ioExObj.getMessage());
            } finally {
                try {
                    if(fOutStream != null){
                        fOutStream.close();
                    }
                    if(rbcObj != null) {
                        rbcObj.close();
                    }
                } catch (IOException ioExObj) {
                    System.out.println("Problem Occured While Closing The Object= " + ioExObj.getMessage());
                }
            }
        } else {
            System.out.println("File already Present! Please Check!");
        }
    }

}
