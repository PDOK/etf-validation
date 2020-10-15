Feature: Service test Wms

  Background:
#    TODO: store in property
    # Constants
    * def serviceType = 'Conformance Class: View Service - WMS'
    * def reportDir = 'reports'
    * def StaticUri = 'http://localhost:63342/etf-validation/'
    * def testSuite = "EIDeec9d674-d94b-4d8d-b744-1309c6cae1d2"
    * def baseURL = 'https://inspire.ec.europa.eu/validator/v2/'

    * def records = read('../../../wms-inspire-records.json')

    # Result handling
    * def ETFReport = Java.type("report.ETFReport")
    * def report = new ETFReport(baseURL, reportDir, serviceType)
    * configure afterFeature = function(){ report.close() }

  Scenario Outline: <protocol> <title> <label> <uuid> <serviceAccessPoint> <metadataStandardVersion> <getRecordByIdUrl>
    * print 'using: ' + validatorBaseUrl + ' for ' + title
    * def record = report.makeRecord(label, title, protocol, uuid, serviceAccessPoint, "UNDEFINED", metadataStandardVersion, getRecordByIdUrl)
    * def result = call read('main.feature')
    * record.setStatus(result.status)
    * report.downloadStatus(result.statusPath, label, record)

    * assert result.status == "PASSED" || result.status == "PASSED_MANUAL"

    Examples:
      | records |
