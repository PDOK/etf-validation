package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MustacheContext {
    List<Record> records;
    String feature;

    public MustacheContext(String feature) {
        this.records = new ArrayList<Record>();
        this.feature = feature;
    }

    static class Record {
        private static Map<String, String> STATUS_COLOR_MAP  = new HashMap<String, String>() {{
            put("PASSED", "bg-success");
            put("PASSED_MANUAL", "bg-info");
            put("FAILED", "bg-danger");
            put("UNDEFINED", "bg-warning");
            put("NOT_APPLICABLE", "bg-warning");
            put("SKIPPED", "bg-warning");
        }};

        private final String htmlPath;
        private final String logPath;
        private final String label;
        private final String title;
        private final String protocol;
        private final String uuid;
        private final String serviceAccessPoint;
        private final String status;
        private final String statusColor;
        private final String metadataStandardVersion;
        private final String getRecordByIdUrl;

        public Record(String label, String title, String protocol, String uuid, String serviceAccessPoint, String status, String metadataStandardVersion, String getRecordByIdUrl) {
            this.htmlPath = label + ".html";
            this.logPath = label + ".log";
            this.label = label;
            this.title = title;
            this.protocol = protocol;
            this.uuid = uuid;
            this.serviceAccessPoint = serviceAccessPoint;
            this.status = status;
            this.statusColor = STATUS_COLOR_MAP.get(status);
            this.metadataStandardVersion = metadataStandardVersion;
            this.getRecordByIdUrl = getRecordByIdUrl;
        }

        public String getHtmlPath() {
            return htmlPath;
        }

        public String getLogPath() {
            return logPath;
        }

        public String getLabel() {
            return label;
        }

        public String getTitle() {
            return title;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getUuid() {
            return uuid;
        }

        public String getServiceAccessPoint() {
            return serviceAccessPoint;
        }

        public String getStatus() {
            return status;
        }

        public String getStatusColor() {
            return statusColor;
        }

        public String getMetadataStandardVersion() {
            return metadataStandardVersion;
        }

        public String getGetRecordByIdUrl() {
            return getRecordByIdUrl;
        }
    }
}
