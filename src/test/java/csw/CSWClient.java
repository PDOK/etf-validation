package csw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Collectors;
import org.apache.http.client.utils.URIBuilder;


public class CSWClient {


    private static final String BASE_URL =  "https://nationaalgeoregister.nl/geonetwork";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static String nodeToString(Node node) throws CSWClientException {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            throw new CSWClientException("error converting node to string", te);
        }
        return sw.toString();
    }

    private static  List<Map<String, String>> getRecords(String constraint) throws CSWClientException {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
//        String getRecordsUrlTemplate = "%s/srv/dut/csw-inspire?request=GetRecords&Service=CSW&Version=2.0.2&typeNames=gmd:MD_Metadata&resultType=&constraintLanguage=CQL_TEXT&constraint_language_version=1.1.0";
        String getRecordsUrlTemplate = "%s/srv/dut/csw-inspire?request=GetRecords&Service=CSW&Version=2.0.2&typeNames=gmd:MD_Metadata&resultType=results&constraintLanguage=CQL_TEXT&constraint_language_version=1.1.0";
        String getRecordsBaseUrl = String.format(getRecordsUrlTemplate, BASE_URL);
        try{
            // example -  constraint (String): " AND protocol='OGC:WMS'"
            String constraintTemplate  = "type='service' AND organisationName='Beheer PDOK'%s";
            String constraintValue =String.format(constraintTemplate, constraint);
            URIBuilder ub = new URIBuilder(getRecordsBaseUrl);
            ub.addParameter("constraint", constraintValue);
            String startPos = "1";
            ub.addParameter("startPosition", constraintValue);

            while (true){
                ub.setParameter("startPosition", startPos);
                String url = ub.toString();
                System.out.println(url);
                String responseBody = doHTTPRequest(url);
                // TODO check for CSW exception in response body, see for instance:
                // https://geonetwork-opensource.org/manuals/2.10.4/eng/developer/xml_services/csw_services.html#errors
                Document doc = getDocument(responseBody);
                Node exNode = (Node) queryXPATH(doc, "/ows:ExceptionReport", XPathConstants.NODE);
                if (exNode != null){
                    String exceptionCode = (String) queryXPATH(doc, "/ows:ExceptionReport/ows:Exception/@exceptionCode", XPathConstants.STRING);
                    if (exceptionCode != ""){
                        throw new CSWClientException("Exception in CSW response, exceptionCode: " + exceptionCode);
                    }
                }
                startPos = (String) queryXPATH(doc, "//csw:SearchResults/@nextRecord", XPathConstants.STRING);
                NodeList records = (NodeList) queryXPATH(doc, ".//csw:SummaryRecord", XPathConstants.NODESET);
                for (int i = 0; i < records.getLength(); i++) {
                    Node node = records.item(i);
                    String title = (String) queryXPATH(node, "dc:title/text()", XPathConstants.STRING);
                    String identifier = (String) queryXPATH(node, "dc:identifier/text()", XPathConstants.STRING);
                    Map <String, String> recordResult = new HashMap<>();
                    recordResult.put("uuid", identifier);
                    recordResult.put("title", title);
                    recordResult.put("label", title.replaceAll(" ", "_").toLowerCase());
                    result.add(recordResult);
                }
                if (startPos.equals("0")){
                    break;
                }
            }
        }catch (java.net.URISyntaxException e){
            throw new CSWClientException("unable to build URI from " + getRecordsBaseUrl, e);
        }
        return result;
    }
    private static String getServiceType(String url) {
        if (url.contains("/wms")){
            return "WMS";
        }else if(url.contains("/wfs")){
            return "WFS";
        }else if(url.contains("/atom")){
            return "ATOM";
        }else if(url.contains("/wcs")){
            return "WCS";
        }else if(url.contains("/wmts")){
            return "WMTS";
        }else if(url.contains("/tms")){
            return "TMS";
        }else if (url.contains("/csw")){
            return "CSW";
        }
        return "";
    }

    private static Map<String, String> getRecordInfo(String uuid) throws  CSWClientException  {
        Map<String, String> result =  new HashMap<>();
        String getRecordBaseUrl = String.format("%s/srv/dut/csw?service=CSW&request=GetRecordById&version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full#MD_DataIdentification", BASE_URL);
        try{
            URIBuilder ub = new URIBuilder(getRecordBaseUrl);
            ub.addParameter("id", uuid);
            String url = ub.toString();
            String responseBody = doHTTPRequest(url);
            Document doc = getDocument(responseBody);

            String serviceAccesPoint = (String) queryXPATH(doc, "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL/text()", XPathConstants.STRING);
            // old dutch profile uses gco:CharacterString for protocol string, from version it should use gmx:Anchor
            String protocol = (String) queryXPATH(doc, "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString/text()", XPathConstants.STRING);
            if (protocol.equals("")){
                protocol = (String) queryXPATH(doc, "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gmx:Anchor/text()", XPathConstants.STRING);
            }
            String profileVersion = (String) queryXPATH(doc, "//gmd:metadataStandardVersion/gco:CharacterString/text()", XPathConstants.STRING);

            result.put("pdokServiceType",  getServiceType(serviceAccesPoint));
            result.put("serviceAccessPoint", serviceAccesPoint);
            result.put("metadataStandardVersion", profileVersion);
            result.put("protocol", protocol);
            result.put("getRecordByIdUrl", url);
        }catch (java.net.URISyntaxException e){
            throw new CSWClientException("unable to build URI from " + getRecordBaseUrl, e);
        }
        return result;
    }

    public static void WriteFile(String fileContent, String fileName) throws  CSWClientException {
        String fileNameWithExt = String.format("%s.json", fileName);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileNameWithExt), "utf-8"))) {
            writer.write(fileContent);
        } catch (FileNotFoundException e) {
            throw new CSWClientException("FileNotFoundException while writing to file " + fileNameWithExt,e);
        } catch (IOException e) {
            throw new CSWClientException("IOException while writing to file " + fileNameWithExt,e);
        }
    }

    public static void writeFiles( List<Map<String, String>> records) throws CSWClientException {
        try{
            // all records
            ObjectMapper mapper = new ObjectMapper();
            String allRecordsJSONString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(records);
            WriteFile(allRecordsJSONString, "all-inspire-records");
            // wms records
            List<Map<String, String>> wmsList = records.stream()
                    .filter(hashmap -> hashmap.containsKey("pdokServiceType"))
                    .filter(hashmap -> (String) hashmap.get("pdokServiceType") == "WMS")
                    .collect(Collectors.toList());
            String wmsRecordsJSONString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(wmsList);
            WriteFile(wmsRecordsJSONString, "wms-inspire-records");
            // atom records
            List<Map<String, String>> atomList = records.stream()
                    .filter(hashmap -> hashmap.containsKey("pdokServiceType"))
                    .filter(hashmap -> (String) hashmap.get("pdokServiceType") == "ATOM")
                    .collect(Collectors.toList());
            String atomRecordsJSONString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(atomList);
            WriteFile(atomRecordsJSONString, "atom-inspire-records");
            // wfs records
            List<Map<String, String>> wfsList = records.stream()
                    .filter(hashmap -> hashmap.containsKey("pdokServiceType"))
                    .filter(hashmap -> (String) hashmap.get("pdokServiceType") == "WFS")
                    .collect(Collectors.toList());
            String wfsRecordsJSONString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(wfsList);
            WriteFile(atomRecordsJSONString, "wfs-inspire-records");
        }catch (JsonProcessingException e){
            throw new CSWClientException("JsonProcessingException while writing JSON files", e);
        }
    }

    private static Document getDocument(String xmlString) throws CSWClientException{
        Document doc = null;
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlString));
            doc = builder.parse(is);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new CSWClientException("Exception occured while parsing XML document", e);
        }
        return doc;
    }

    private static Object queryXPATH(Node node, String xpathQuery, QName nodeType) throws CSWClientException{
        try{
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            xpath.setNamespaceContext(new NamespaceContext() {
                public String getNamespaceURI(String prefix) {
                    if (prefix == null) throw new NullPointerException("Null prefix");
                    else if ("csw".equals(prefix)) return "http://www.opengis.net/cat/csw/2.0.2";
                    else if ("gmd".equals(prefix)) return "http://www.isotc211.org/2005/gmd";
                    else if ("dc".equals(prefix)) return "http://purl.org/dc/elements/1.1/";
                    else if ("dct".equals(prefix)) return "http://purl.org/dc/terms/";
                    else if ("geonet".equals(prefix)) return "http://www.fao.org/geonetwork";
                    else if ("gco".equals(prefix)) return "http://www.isotc211.org/2005/gco";
                    else if ("gmx".equals(prefix)) return "http://www.isotc211.org/2005/gmx";
                    else if ("ows".equals(prefix)) return "http://www.opengis.net/ows";
                    else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
                    return XMLConstants.NULL_NS_URI;
                }
                // This method isn't necessary for XPath processing.
                public String getPrefix(String uri) {
                    throw new UnsupportedOperationException();
                }
                // This method isn't necessary for XPath processing either.
                public Iterator getPrefixes(String uri) {
                    throw new UnsupportedOperationException();
                }
            });
            XPathExpression expr = xpath.compile(xpathQuery);
            return expr.evaluate(node, nodeType);
        } catch (XPathExpressionException e) {
            throw new CSWClientException("XPathExpressionException on query: " + xpathQuery, e);
        }
    }

    private static String doHTTPRequest(String url) throws CSWClientException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url)) // add request header
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody =response.body();
                return responseBody;
            }
            throw new CSWClientException("unexpected response received from: " + url + " , status_code: " + response.statusCode());
        } catch (InterruptedException | IOException e) {
            throw new CSWClientException("error while doing HTTP request,url: " + url, e);
        }
    }

    public static void GenerateJSONFromMetadataRecords() throws CSWClientException {
        List<Map<String, String>> records = getRecords(""); // " AND protocol='INSPIRE Atom'"
        for (Map<String, String> record : records) {
            Map<String, String> recordInfo = getRecordInfo(record.get("uuid"));
            record.putAll(recordInfo);
        }
        writeFiles(records);
    }

    public static void main(String[] args) throws CSWClientException {
        GenerateJSONFromMetadataRecords();
    }
}
