@parallel=false
Feature: Service test Wfs

  Background:
    # Constants
    * def serviceType = 'Conformance Class: Download Service - Direct WFS'
    * def reportDir = karate.properties["reportDir"]
    * def StaticUri = karate.properties["StaticUri"]
    * def baseURL = karate.properties["baseURL"]
    * print 'reportDir', reportDir
    * print 'StaticUri', StaticUri
    * print 'baseURL', baseURL
    * url baseURL

    * def records = read(karate.properties["wfsRecords"])

    # Result handling
    * def ETFReport = Java.type("report.ETFReport")
    * def report = new ETFReport(baseURL, reportDir, serviceType, "wfs")
    * configure afterFeature = function(){ report.close() }
    * configure connectTimeout = 300000
    * configure readTimeout = 300000

  Scenario Outline: <protocol> <title> <label> <uuid> <serviceAccessPoint> <metadataStandardVersion> <getRecordByIdUrl>
    * print 'using: ' + validatorBaseUrl + ' for ' + title
    * def record = report.makeRecord(label, title, protocol, uuid, serviceAccessPoint, "UNDEFINED", metadataStandardVersion, getRecordByIdUrl)

    * def testRunRequest =
      """
      {
        "label": "<label>",
        "executableTestSuiteIds": [
          "EIDed2d3501-d700-4ff9-b9bf-070dece8ddbd"
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

    Given path 'TestRuns'
    And request testRunRequest
    When method post
    * print "statuscode WFS: ", responseStatus, serviceAccessPoint
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

    * report.downloadStatus(statusPath, label, status)

    * assert status == "PASSED" || status == "PASSED_MANUAL"

    Examples:
      | records |
