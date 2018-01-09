/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Feb 7, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SubjectTerm;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Helper class for integration web tests using selenium and java mail.
 * 
 * @author miethaner
 */
public class WebTester {

    // selenium parameters
    private static final int IMPLICIT_WAIT = 10;

    // property keys
    private static final String BES_SECURE = "bes.secure";
    private static final String BES_HTTP_URL = "bes.http.url";
    private static final String BES_HTTPS_URL = "bes.https.url";
    private static final String EMAIL_ADDRESS = "email.address";
    private static final String EMAIL_HOST = "email.host";
    private static final String EMAIL_USER = "email.user";
    private static final String EMAIL_PASSWORD = "email.password";
    private static final String EMAIL_PROTOCOL = "email.protocol";

    // path schemas
    private static final String PROPERTY_PATH = "../oscm-devruntime/javares/local/%s/webtest.properties";
    private static final String BASE_PATH_PORTAL = "%s/oscm-portal/%s";
    private static final String BASE_PATH_MARKETPLACE = "%s/oscm-portal/marketplace/%s";

    // email parameters
    private static final String MAIL_INBOX = "INBOX";

    // web element keys
    private static final String ATTRIUBTE_VALUE = "value";

    private static final String ELEMENT_PORTAL_USERID = "userId";
    private static final String ELEMENT_PORTAL_PASSWORD = "password";
    private static final String ELEMENT_PORTAL_LOGIN = "loginButton";
    private static final String ELEMENT_PORTAL_LOGOUT = "formLogout:logout";

    private static final String ELEMENT_PORTAL_ERRORS = "errorMessages:";
    private static final String ELEMENT_PORTAL_ERRORCLASS = "rf-msgs-sum";

    private static final String ELEMENT_PORTAL_INFOS = "infoMessages:";
    private static final String ELEMENT_PORTAL_INFOCLASS = "rf-msgs-sum";

    private static final String ELEMENT_MARKETPLACE_USERID = "loginForm:loginUserId";
    private static final String ELEMENT_MARKETPLACE_PASSWORD = "loginForm:loginPassword";
    private static final String ELEMENT_MARKETPLACE_LOGIN = "loginForm:loginButtonLink";
    private static final String ELEMENT_MARKETPLACE_LOGOUT = "formLogout:logout";

    private String base;
    private WebDriver driver;

    private String address;
    private Session mailSession;

    public WebTester() throws Exception {
        Map<String, String> env = System.getenv();
        // load properties from personal devruntime folder
        String localhost = env.get("HOSTNAME");
        if (StringUtils.isEmpty(localhost)) {
            localhost = InetAddress.getLocalHost().getHostName();
        }
        String filePath = String.format(PROPERTY_PATH, localhost);

        Properties prop = new Properties();
        FileInputStream fis = new FileInputStream(filePath);
        prop.load(fis);
        fis.close();

        boolean secure = Boolean.parseBoolean(prop.getProperty(BES_SECURE));

        if (secure) {
            base = prop.getProperty(BES_HTTPS_URL);
        } else {
            base = prop.getProperty(BES_HTTP_URL);
        }

        // initialize selenium webdriver
        driver = new HtmlUnitDriver(true);
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT,
                TimeUnit.SECONDS);

        // initialize java mail session
        address = prop.getProperty(EMAIL_ADDRESS);
        String host = prop.getProperty(EMAIL_HOST);
        final String user = prop.getProperty(EMAIL_USER);
        final String password = prop.getProperty(EMAIL_PASSWORD);
        String protocol = prop.getProperty(EMAIL_PROTOCOL);

        Properties emailProp = new Properties();
        emailProp.setProperty("mail.store.protocol", protocol);
        emailProp.setProperty("mail.host", host);
        emailProp.setProperty("mail.user", user);
        emailProp.setProperty("mail.from", address);
        emailProp.setProperty("mail.debug", "true");

        mailSession = Session.getInstance(emailProp, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
    }

    /**
     * Closes all open resources of the helper
     */
    public void close() {
        driver.close();
    }

    /**
     * Attempts a login to the OSCM portal with the given credentials. Note that
     * this method assumes the webdriver to be at the login page.
     * 
     * @param user
     *            the user name
     * @param password
     *            the password
     */
    public void loginPortal(String user, String password) {
        WebElement userInput = driver.findElement(By.id(ELEMENT_PORTAL_USERID));
        userInput.sendKeys(user);

        WebElement pwdInput = driver
                .findElement(By.name(ELEMENT_PORTAL_PASSWORD));
        pwdInput.sendKeys(password);

        driver.findElement(By.id(ELEMENT_PORTAL_LOGIN)).click();

        System.out.println("Login OSCM Portal");
    }

    /**
     * Navigates the webdriver to the given page of the OSCM portal.
     * 
     * @param page
     *            the page of the portal
     */
    public void visitPortal(String page) {
        String target = String.format(BASE_PATH_PORTAL, base, page);

        driver.navigate().to(target);

        System.out.println("Goto " + target);
    }

    /**
     * Logs out the current user from the OSCM portal. Note that this method
     * assumes that there is a logged in user and that the driver is at a portal
     * page.
     */
    public void logoutPortal() {
        driver.findElement(By.id(ELEMENT_PORTAL_LOGOUT)).click();

        System.out.println("Logout OSCM Portal");
    }

    /**
     * Attempts a login to the OSCM marketplace with the given credentials. Note
     * that this method assumes the webdriver to be at the login page.
     * 
     * @param user
     *            the user name
     * @param password
     *            the password
     */
    public void loginMarketplace(String user, String password) {
        WebElement userInput = driver
                .findElement(By.id(ELEMENT_MARKETPLACE_USERID));
        userInput.sendKeys(user);

        WebElement pwdInput = driver
                .findElement(By.id(ELEMENT_MARKETPLACE_PASSWORD));
        pwdInput.sendKeys(password);

        driver.findElement(By.id(ELEMENT_MARKETPLACE_LOGIN)).click();

        System.out.println("Login OSCM Marketplace");
    }

    /**
     * Navigates the webdriver to the given page of the OSCM marketplace.
     * 
     * @param page
     *            the page of the portal
     */
    public void visitMarketplace(String context) {
        String target = String.format(BASE_PATH_MARKETPLACE, base, context);

        driver.navigate().to(target);

        System.out.println("Goto " + target);
    }

    /**
     * Logs out the current user from the OSCM marketplace. Note that this
     * method assumes that there is a logged in user and that the driver is at a
     * marketplace page.
     */
    public void logoutMarketplace() {
        driver.findElement(By.id(ELEMENT_MARKETPLACE_LOGOUT)).click();

        System.out.println("Logout OSCM Marketplace");
    }

    /**
     * Reads the error message from the page notification.
     * 
     * @return the error message
     * @throws NoSuchElementException
     *             if error message is not present
     */
    public String readErrorMessage() {
        WebElement element = driver.findElement(By.id(ELEMENT_PORTAL_ERRORS));
        return element.findElement(By.className(ELEMENT_PORTAL_ERRORCLASS))
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
        WebElement element = driver.findElement(By.id(ELEMENT_PORTAL_INFOS));
        return element.findElement(By.className(ELEMENT_PORTAL_INFOCLASS))
                .getText();
    }

    /**
     * Verifies if the content of the element with the given id is equal to the
     * given value.
     * 
     * @param id
     *            the element id
     * @param value
     *            the value to compare with
     * @return true if equal
     * @throws NoSuchElementException
     *             if element is not present
     */
    public boolean verifyElement(String id, String value) {
        WebElement element = driver.findElement(By.id(id));
        String attribute = element.getAttribute(ATTRIUBTE_VALUE);

        if (attribute != null && attribute.equals(value)) {
            System.out.println("Element with id " + id + " and value " + value
                    + " is valid");
            return true;
        } else {
            System.out.println("Element with id " + id + " is invalid (" + value
                    + " != " + attribute + ")");
            return false;
        }
    }

    /**
     * Clicks the element with the given id.
     * 
     * @param id
     *            the element id
     * @throws NoSuchElementException
     *             if element is not present
     */
    public void clickElement(String id) {
        driver.findElement(By.id(id)).click();

        System.out.println("Clicked the element with id " + id);
    }

    /**
     * Reads the value of the element with the given id. This is used for fields
     * that use the value attribute, e.g. input fields.
     *
     * @return the value of the element
     * @throws NoSuchElementException
     *             if element is not present
     */
    public String readValue(String id) {
        WebElement element = driver.findElement(By.id(id));
        return element.getAttribute(ATTRIUBTE_VALUE);
    }

    /**
     * Reads the text of the element with the given id. This is used for text
     * within an element, e.g. &lt;p id="id"&gt;text&lt;/p&gt;
     *
     * @return the text of the element
     * @throws NoSuchElementException
     *             if element is not present
     */
    public String readText(String id) {
        WebElement element = driver.findElement(By.id(id));
        return element.getText();
    }

    /**
     * Takes the given value as input for the element with the given id.
     * 
     * @param id
     *            the element id
     * @param value
     *            the input value
     * @throws NoSuchElementException
     *             if element is not present
     */
    public void writeValue(String id, String value) {
        WebElement element = driver.findElement(By.id(id));
        element.sendKeys(value);
    }

    /**
     * Selects in the dropdown (select) element with the given id the option
     * with the given value.
     * 
     * @param id
     *            the element id
     * @param value
     *            the option value
     * @throws NoSuchElementException
     *             if element is not present
     */
    public void selectDropdown(String id, String value) {
        Select select = new Select(driver.findElement(By.id(id)));
        select.selectByValue(value);
    }

    /**
     * Submits the form with the given id.
     * 
     * @param id
     *            the element id
     * @throws NoSuchElementException
     *             if element is not present
     */
    public void submitForm(String id) {
        driver.findElement(By.id(id)).submit();

        System.out.println("Submitted form with id " + id);
    }

    /**
     * Waits for the element with the given id to be present or until the given
     * amount of seconds has passed.
     * 
     * @param id
     *            the element id
     * @param seconds
     *            the seconds until timeout
     * @throws TimeoutException
     *             if the timeout is reached
     */
    public void waitForElement(String id, int seconds) {
        (new WebDriverWait(driver, seconds))
                .until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
    }

    /**
     * Reads the latest email with the given subject from the email inbox.
     * 
     * @param subject
     *            the email subject
     * @return the email body or null if no email was found
     * @throws Exception
     */
    public String readLatestEmailWithSubject(String subject) throws Exception {
        Store store = mailSession.getStore();
        store.connect();

        Folder folder = store.getFolder(MAIL_INBOX);
        folder.open(Folder.READ_WRITE);

        Message[] messages = null;

        messages = folder.search(new SubjectTerm(subject));

        String body = null;
        if (messages.length > 0) {
            Message latest = messages[0];

            for (Message m : messages) {
                if (latest.getSentDate().compareTo(m.getSentDate()) < 0) {
                    latest = m;
                }
            }
            body = (String) latest.getContent();
        }

        folder.close(false);
        store.close();

        return body;
    }

    /**
     * Returns the current URL that the webdriver is visiting.
     * 
     * @return the current URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Returns the configured email address.
     * 
     * @return the email address
     */
    public String getEmailAddress() {
        return address;
    }
}
