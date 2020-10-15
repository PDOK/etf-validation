Feature: Service test Wms

  Background:
#    TODO: store in property
    # Constants
    * def serviceType = 'Conformance Class: Download Service - Direct WFS'
    * def reportDir = 'reports'
    * def StaticUri = 'http://localhost:63342/etf-validation/'
    * def testSuite = "EIDed2d3501-d700-4ff9-b9bf-070dece8ddbd"
    * def baseURL = 'https://inspire.ec.europa.eu/validator/v2/'

    * def records = read('../../../wfs-inspire-records.json')

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
