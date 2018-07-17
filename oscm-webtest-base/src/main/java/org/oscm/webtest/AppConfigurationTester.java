/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: 20 6, 2018                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.webtest;

import java.util.List;
import javax.security.auth.login.LoginException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Helper class for integration web tests for oscm-app/default.jsf
 */
public class AppConfigurationTester extends WebTester {

    public static final String ERROR_MSG_CONTROLLER_EXISTS = "Controller ID already exists.";
    private String appAdminMailAddress = "";
    private String appBaseUrl = "";
    private String bssUserId = "";
    private String bssUserKey = "";
    private String bssUserPwd = "";

    private String base;
    private String head;

    public AppConfigurationTester() throws Exception {
        super();

        baseUrl = loadUrl(APP_SECURE, APP_HTTPS_URL, APP_HTTP_URL);
        if (baseUrl.contains("https")) {
            head = "https://";
        } else {
            head = "http://";
        }
        base = baseUrl.replace(head, "");
    }

    /**
     * Attempts a login to the OSCM portal with the given credentials. Note that
     * this method assumes the webdriver to be at the login page.
     * 
     * @param user
     *            the user name
     * @param password
     *            the password
     * @throws InterruptedException
     * @throws Exception
     */
    public void loginAppConfig(String user, String password)
            throws LoginException, InterruptedException {

        String url = head + user + ":" + password + "@" + base
                + AppPathSegments.APP_CONFIGURATION;
        driver.get(url);
        driver.manage().window().maximize();

        wait(IMPLICIT_WAIT);

        if (verifyFoundElement(By.id(AppHtmlElements.APP_CONFIG_FORM1))) {
            logger.info(
                    "Login to " + url + " successfully with userid:" + user);
        } else {
            String info = "Login to " + url + " failed with userid:" + user;
            logger.info(info);
            throw new LoginException(info);
        }
    }

    /**
     * Navigates the webdriver to the given page of the OSCM portal.
     * 
     * @param page
     *            the page of the portal
     * @throws Exception
     */
    public void visitAppConfig() throws Exception {
        String target = baseUrl + AppPathSegments.APP_CONFIGURATION;
        driver.navigate().to(target);

        if (verifyFoundElement(By.id(AppHtmlElements.APP_CONFIG_FORM1))) {
            logger.info("Navigate to " + target
                    + " failed : HTTP Status 404 - Not Found");
            throw new Exception("Page not found!");
        } else {
            logger.info("Navigate to " + target + " successfully");
        }
    }

    /**
     * Logs out the current user from the OSCM portal. Note that this method
     * assumes that there is a logged in user and that the driverApp is at a
     * portal page.
     * 
     * @throws Exception
     */
    public void logoutAppConfig() throws Exception {
    }

    /**
     * Reads the error message from the page notification.
     * 
     * @return the error message
     * @throws NoSuchElementException
     *             if error message is not present
     */
    public String readErrorMessage() {
        WebElement element = driver.findElement(
                By.className(AppHtmlElements.APP_CONFIG_DIV_CLASS_STATUS_MSG));
        return element
                .findElement(By.className(
                        AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_ERROR))
                .getText();
    }

    /**
     * Reads the info message from the page notification.
     * 
     * @return the info message
     * @throws NoSuchElementException
     *             if info message is not present
     */
    public String readInfoMessage() {
        WebElement element = driver.findElement(
                By.className(AppHtmlElements.APP_CONFIG_DIV_CLASS_STATUS_MSG));
        return element
                .findElement(By.className(
                        AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK))
                .getText();
    }

    public boolean getExecutionResult() {
        waitForElement(
                By.className(AppHtmlElements.APP_CONFIG_DIV_CLASS_STATUS_MSG),
                10);

        if (!verifyFoundElement(By
                .className(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_ERROR))
                && verifyFoundElement(By.className(
                        AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK))) {
            logger.info(readInfoMessage());
            return true;
        } else {
            logger.info(readErrorMessage());
            return false;
        }
    }

    public void registerController(String controllerId, String orgId)
            throws Exception {
        WebElement inputCid = driver
                .findElement(By.xpath("//input[contains(@id,'"
                        + AppHtmlElements.APP_CONFIG_FORM1_INPUT_END_NEWCONTROLLERID
                        + "')]"));
        inputCid.clear();
        inputCid.sendKeys(controllerId);
        WebElement inputOrgid = driver
                .findElement(By.xpath("//input[contains(@id,'"
                        + AppHtmlElements.APP_CONFIG_FORM1_INPUT_END_NEWORGID
                        + "')]"));
        inputOrgid.clear();
        inputOrgid.sendKeys(orgId);

        WebElement baseForm = driver.findElement(By.xpath(
                "//form[@id='" + AppHtmlElements.APP_CONFIG_FORM1 + "']"));
        baseForm.findElement(
                By.className(AppHtmlElements.APP_CONFIG_FORM_BUTTON_CLASS))
                .click();
        if (!getExecutionResult()) {
            if (readErrorMessage().contains(ERROR_MSG_CONTROLLER_EXISTS))
                throw new Exception(ERROR_MSG_CONTROLLER_EXISTS);
            else
                throw new Exception("other error");
        }
    }

    private void clearNewEntry() {
        WebElement inputCid = driver
                .findElement(By.xpath("//input[contains(@id,'"
                        + AppHtmlElements.APP_CONFIG_FORM1_INPUT_END_NEWCONTROLLERID
                        + "')]"));
        inputCid.clear();
        WebElement inputOrgid = driver
                .findElement(By.xpath("//input[contains(@id,'"
                        + AppHtmlElements.APP_CONFIG_FORM1_INPUT_END_NEWORGID
                        + "')]"));
        inputOrgid.clear();
    }

    public void changeOrgIdOnController(String controllerId, String newOrdId)
            throws Exception {
        clearNewEntry();
        log("xpath := //form[@id='" + AppHtmlElements.APP_CONFIG_FORM1
                + "']/table/tbody[1]/tr/td[./text()='" + controllerId
                + "']/../td[2]/input");
        WebElement input = driver.findElement(
                By.xpath("//form[@id='" + AppHtmlElements.APP_CONFIG_FORM1
                        + "']/table/tbody[1]/tr/td[./text()='" + controllerId
                        + "']/../td[2]/input"));
        input.clear();
        input.sendKeys(newOrdId);

        driver.findElement(By.xpath("//form[@id='"
                + AppHtmlElements.APP_CONFIG_FORM1 + "']" + "//input[@class='"
                + AppHtmlElements.APP_CONFIG_FORM_BUTTON_CLASS + "']")).click();

        if (!getExecutionResult())
            throw new Exception();
    }

    public List<WebElement> getContentAppConfigTable(String formId) {

        WebElement baseTableBody = driver.findElement(
                By.xpath("//form[@id='" + formId + "']/table/tbody"));
        List<WebElement> tableRows = baseTableBody
                .findElements(By.tagName("tr"));

        return tableRows;

    }

    public String returnControllerId(List<WebElement> tableRows, int index) {

        WebElement td = tableRows.get(index).findElement(By.xpath("//td[0]"));
        return td.getText();
    }

    public String returnOrgId(List<WebElement> tableRows, int index) {

        WebElement td = tableRows.get(index).findElement(By.xpath("//td[1]"));
        WebElement input = td.findElement(By.xpath("//*[ends-with(@id,'"
                + AppHtmlElements.APP_CONFIG_FORM1_INPUT_END_EXISTORGID
                + "')]"));
        return input.getAttribute("value");
    }

    public void clickRemoveLink(List<WebElement> tableRows, int index) {

        WebElement td = tableRows.get(index).findElement(By.xpath("//td[2]"));
        WebElement href = td.findElement(By.tagName("a"));
        href.click();
    }

    private String returnInputValueForm2(int index) {

        return driver
                .findElement(By.xpath("//form[@id='"
                        + AppHtmlElements.APP_CONFIG_FORM2
                        + "']/table/tbody[1]/tr/[" + index + "]/td[2]/input"))
                .getAttribute(ATTRIUBTE_VALUE);
    }

    private void changeInputValueForm2(int index, String keyword)
            throws Exception {
        WebElement input = driver.findElement(
                By.xpath("//form[@id='" + AppHtmlElements.APP_CONFIG_FORM2
                        + "']/table/tbody[1]/tr[" + index + "]/td[2]/input"));
        input.clear();
        input.sendKeys(keyword);

        driver.findElement(By.xpath("//form[@id='"
                + AppHtmlElements.APP_CONFIG_FORM2 + "']" + "//input[@class='"
                + AppHtmlElements.APP_CONFIG_FORM_BUTTON_CLASS + "']")).click();

        if (!getExecutionResult())
            throw new Exception();
    }

    public String getAppAdminMailAddress() {

        this.appAdminMailAddress = returnInputValueForm2(1);
        return this.appAdminMailAddress;
    }

    public void setAppAdminMailAddress(String appAdminMailAddress)
            throws Exception {
        changeInputValueForm2(1, appAdminMailAddress);
        this.appAdminMailAddress = appAdminMailAddress;
    }

    public String getAppBaseUrl() {
        this.appBaseUrl = returnInputValueForm2(2);
        return this.appBaseUrl;
    }

    public void setAppBaseUrl(String appBaseUrl) throws Exception {
        changeInputValueForm2(2, appBaseUrl);
        this.appBaseUrl = appBaseUrl;
    }

    public String getBssUserId() {
        this.bssUserId = returnInputValueForm2(3);
        return this.bssUserId;
    }

    public void setBssUserId(String bssUserId) throws Exception {
        changeInputValueForm2(3, bssUserId);
        this.bssUserId = bssUserId;
    }

    public String getBssUserKey() {
        this.bssUserKey = returnInputValueForm2(4);
        return this.bssUserKey;

    }

    public void setBssUserKey(String bssUserKey) throws Exception {
        changeInputValueForm2(4, bssUserKey);
        this.bssUserKey = bssUserKey;
    }

    public String getBssUserPwd() {
        this.bssUserPwd = returnInputValueForm2(5);
        return this.bssUserPwd;
    }

    public void setBssUserPwd(String bssUserPwd) throws Exception {
        changeInputValueForm2(5, bssUserPwd);
        this.bssUserPwd = bssUserPwd;
    }

}
