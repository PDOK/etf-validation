Feature: Service test Atom

  Background:
#    TODO: store in property
    # Constants
    * def validatorBaseUrl = 'https://inspire.ec.europa.eu/validator/v2/'
    * def reportDir = 'reports'
    * def StaticUri = 'http://localhost:63342/etf-validation/'

    * def records = read('../../../../atom-inspire-records.json')
    * url validatorBaseUrl

    # Result handling
    * def ETFReport = Java.type("report.ETFReport")
    * def report = new ETFReport(validatorBaseUrl, reportDir, "atom")
    * configure afterFeature = function(){ report.close() }

  Scenario Outline: <protocol> <title> <label> <uuid> <serviceAccessPoint> <metadataStandardVersion> <getRecordByIdUrl>
    * print 'using: ' + validatorBaseUrl + ' for ' + title

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
    * report.downloadStatus(statusPath, status, protocol, label, title, uuid, serviceAccessPoint, metadataStandardVersion, getRecordByIdUrl)

    * assert status == "SUCCESS"

    Examples:
      | records |
