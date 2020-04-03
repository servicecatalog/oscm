This project contains UI tests

<<<<<<<<<<To run the tests locally, change the following>>>>>>>>>>

In the webtest.properties configuration file
 * chrome.driver.path -> path to the locally installed chromedriver
 * change url addresses
 * select auth.mode -> INTERNAL or OIDC

In the WebTester.java file should be commented out:
 * options.setHeadless(true)
 * options.addArguments("--no-sandbox", "--lang=en");

The PlaygroundSuiteTest.java file should contain values for a given test case