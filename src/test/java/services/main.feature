@ignore
Feature: Service test

  Scenario:
    * url baseURL
    * def testRunRequest = { label: #(label), executableTestSuiteIds: [#(testSuite)], arguments: { testRunTags: #(label)}, testObject: { resources: { serviceEndpoint: #(serviceAccessPoint) } } }

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
