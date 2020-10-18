package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MustacheContext {
    private List<Record> records;
    private String feature;

    public MustacheContext(String feature) {
        this.records = new ArrayList<Record>();
        this.feature = feature;
    }

    public void addRecord(Record record) {
        this.records.add(record);
    }

    public void setStatusForLabel(String status, String label) throws IllegalArgumentException {
        boolean labelIsNotSet = true;
        for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            if (record.getLabel().equals(label)) {
                record.setStatus(status);
                records.set(i, record);
                labelIsNotSet = false;
                break;
            }
        }
        if (labelIsNotSet) {
            throw new IllegalArgumentException();
        }
    }

    public List<Record> getRecords() {
        return records;
    }

    public String getFeature() {
        return feature;
    }

    public static class Record {
        private Map<String, String> STATUS_COLOR_MAP  = new HashMap<String, String>() {{
            put("PASSED", "table-success");
            put("PASSED_MANUAL", "table-info");
            put("FAILED", "table-danger");
            put("UNDEFINED", "table-warning");
            put("NOT_APPLICABLE", "table-warning");
            put("SKIPPED", "table-warning");
        }};

        private final String htmlPath;
        private final String logPath;
        private final String label;
        private final String title;
        private final String protocol;
        private final String uuid;
        private final String serviceAccessPoint;
        private String status;
        private String statusColor;
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

        public void setStatus(String status) {
            this.status = status;
            this.statusColor = STATUS_COLOR_MAP.get(status);
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
