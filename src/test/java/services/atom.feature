Feature: Service test Atom

  Background:
#    TODO: store in property
    # Constants
    * def serviceType = 'Conformance Class: Download Service - Pre-defined Atom'
    * def reportDir = 'reports'
    * def StaticUri = 'http://localhost:63342/etf-validation/'
    * def testSuite = "EID11571c92-3940-4f42-a6cd-5e2b1c6f4d93"
    * def baseURL = 'https://inspire.ec.europa.eu/validator/v2/'

    * def records = read('../../../atom-inspire-records.json')

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
