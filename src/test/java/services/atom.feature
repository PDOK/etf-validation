@parallel=false
Feature: Service test Atom

  Background:
#    TODO: store in property
    # Constants
    * def serviceType = 'Conformance Class: Download Service - Pre-defined Atom'
    * def reportDir = 'reports'
    * def StaticUri = 'http://localhost:63342/etf-validation/'
    * def baseURL = 'https://inspire.ec.europa.eu/validator/v2/'
    * url baseURL

    * def records = read('../../../atom-inspire-records.copy.json')

    # Result handling
    * def ETFReport = Java.type("report.ETFReport")
    * def report = new ETFReport(baseURL, reportDir, serviceType, "atom")
    * configure afterFeature = function(){ report.close() }

  Scenario Outline: <protocol> <title> <label> <uuid> <serviceAccessPoint> <metadataStandardVersion> <getRecordByIdUrl>
    * print 'using: ' + validatorBaseUrl + ' for ' + title
    * def record = report.makeRecord(label, title, protocol, uuid, serviceAccessPoint, "UNDEFINED", metadataStandardVersion, getRecordByIdUrl)

    * def testRunRequest =
      """
      {
        "label": "<label>",
        "executableTestSuiteIds": [
          "EID11571c92-3940-4f42-a6cd-5e2b1c6f4d93"
        ],
        "arguments": {
          "testRunTags": "<label>"
        },
        "testObject": {
          "resources": {
            "serviceEndpoint": "<serviceAccessPoint>"
          }
        }
      }
      """

    * print testRunRequest

    Given path 'TestRuns'
    And request testRunRequest
    When method post
    Then assert responseStatus == 200 || responseStatus == 201
    * def RunId = response.EtfItemCollection.testRuns.TestRun.id
    * def statusPath = "TestRuns/" + RunId
    * def progressPath = "TestRuns/" + RunId + "/progress"

    # retry for 10 minutes (120 times every 5 seconds)
    * configure retry = { count: 120, interval: 5000 }

    Given path progressPath
    And retry until response.val == response.max
    When method get

    # reset retry to default
    * configure retry = { count: 3, interval: 1000 }

    Given path statusPath
    When method get
    Then assert response.EtfItemCollection.testRuns.TestRun.status != "UNDEFINED"

    # save the status:
    * def status = response.EtfItemCollection.testRuns.TestRun.status
    * print status

    * record.setStatus(status)
    * report.downloadStatus(statusPath, label, record)

    * assert status == "PASSED" || status == "PASSED_MANUAL"

    Examples:
      | records |
