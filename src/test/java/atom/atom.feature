Feature: Service test Atom

  Background:
#    TODO: store in property
    * def validatorBaseUrl = 'https://inspire.ec.europa.eu/validator/v2/'
    * url validatorBaseUrl

  Scenario Outline: <testsuite> <label> <serviceEndpoint>
    * print 'using: ', validatorBaseUrl

    * def testRunRequest =
      """
      {
        "label": "<label>",
        "executableTestSuiteIds": [
          "<testsuite>"
        ],
        "arguments": {
          "testRunTags": "<label>"
        },
        "testObject": {
          "resources": {
            "serviceEndpoint": "<serviceEndpoint>"
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
    * print 'status path: ', statusPath
    * print 'progress path: ', progressPath

    # retry for 10 minutes (120 times every 5 seconds)
    * configure retry = { count: 120, interval: 5000 }

    Given path progressPath
    And retry until response.val == response.max
    When method get

    * configure retry = { count: 3, interval: 1000 }

    Given path statusPath
    When method get
    Then assert response.EtfItemCollection.testRuns.TestRun.status != "UNDEFINED"

    # save the status:
    * def logURL = validatorBaseUrl + statusPath + "/log"
    * def htmlReportURL = validatorBaseUrl + statusPath + ".html"
    * def timestamp = new Date().getTime() + "_"
    * def logPath = "target/" + timestamp + label + "_atom.log"
    * def htmlPath = "target/" + timestamp + label + "_atom.html"

    * def DownloadHTMLReport = Java.type("nl.pdok.DownloadHTMLReport")
    * DownloadHTMLReport.download(logURL, logPath)
    * DownloadHTMLReport.download(htmlReportURL, htmlPath)
    * eval karate.embed('<p><a href="./' + htmlPath + '">HTML rapport</a></p><p><a href="./' + logPath + '">Logbestand</a></p>', 'text/html')

    * def status = response.EtfItemCollection.testRuns.TestRun.status
    * print 'status: ', status
    * assert status == "SUCCESS"

    Examples:
      | testsuite                               | label         | serviceEndpoint                                                         |
      | EID11571c92-3940-4f42-a6cd-5e2b1c6f4d93 | atom_inspire1 | https://geodata.nationaalgeoregister.nl/provincies/ps/atom/v1/index.xml |
      | EID11571c92-3940-4f42-a6cd-5e2b1c6f4d93 | atom_inspire2 | https://geodata.nationaalgeoregister.nl/provincies/ps/atom/v1/index.xml |
