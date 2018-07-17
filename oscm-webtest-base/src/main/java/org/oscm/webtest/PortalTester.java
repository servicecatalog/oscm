/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: 20 6, 2018                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SubjectTerm;
import javax.security.auth.login.LoginException;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Helper class for integration web tests using selenium and java mail.
 * 
 * @author miethaner
 */
public class PortalTester extends WebTester {
    private static final String EMAIL_ADDRESS = "email.address";
    private static final String EMAIL_HOST = "email.host";
    private static final String EMAIL_USER = "email.user";
    private static final String EMAIL_PASSWORD = "email.password";
    private static final String EMAIL_PROTOCOL = "email.protocol";
    public static final String TECHSERVICE_PARAM_EMAIL = "The receiver of emails.";
    public static final String TECHSERVICE_PARAM_USER = "IAAS user";
    public static final String TECHSERVICE_PARAM_PWD = "IAAS password";
    public static final String TECHSERVICE_PARAM_MESSAGETEXT = "The message text for emails.";
    // path schemas
    private static final String BASE_PATH_PORTAL = "%s/oscm-portal/%s";
    private static final String BASE_PATH_MARKETPLACE = "%s/oscm-portal/marketplace/%s";

    // email parameters
    private static final String MAIL_INBOX = "INBOX";

    private String address;
    private Session mailSession;
    private Properties emailProp;

    public PortalTester() throws Exception {
        super();

        baseUrl = loadUrl(BES_SECURE, BES_HTTPS_URL, BES_HTTP_URL);
        initMailSession();

        visitPortal("");
    }

    /**
     * initialize java mail session
     */
    private void initMailSession() {

        address = prop.getProperty(EMAIL_ADDRESS);
        String host = prop.getProperty(EMAIL_HOST);
        final String user = prop.getProperty(EMAIL_USER);
        final String password = prop.getProperty(EMAIL_PASSWORD);
        String protocol = prop.getProperty(EMAIL_PROTOCOL);

        emailProp = new Properties();
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
    public void loginPortal(String user, String password)
            throws LoginException, InterruptedException {

        WebElement userInput = driver
                .findElement(By.id(PortalHtmlElements.PORTAL_INPUT_USERID));
        userInput.sendKeys(user);

        WebElement pwdInput = driver
                .findElement(By.name(PortalHtmlElements.PORTAL_INPUT_PASSWORD));
        pwdInput.sendKeys(password);

        driver.findElement(By.id(PortalHtmlElements.PORTAL_BUTTON_LOGIN))
                .click();

        wait(IMPLICIT_WAIT);

        if (!verifyFoundElement(
                By.id(PortalHtmlElements.PORTAL_DIV_LOGIN_FAILED))) {
            log("Login to OSCM Portal successfully with userid:" + user);
        } else {
            String info = "Login to OSCM Portal failed with userid:" + user;
            log(info);
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
    public void visitPortal(String segments) throws Exception {
        String target = String.format(BASE_PATH_PORTAL, baseUrl, segments);
        driver.navigate().to(target);

        String actualTitle = driver.getTitle();
        if (actualTitle == null || !actualTitle
                .contentEquals(PortalHtmlElements.PORTAL_TITLE)) {
            log("Navigate to " + target
                    + " failed : HTTP Status 404 - Not Found");
            throw new Exception("Page not found!");
        } else {
            log("Navigate to " + target + " successfully");
        }
    }

    /**
     * Logs out the current user from the OSCM portal. Note that this method
     * assumes that there is a logged in user and that the driverApp is at a
     * portal page.
     */
    public void logoutPortal() {
        driver.findElement(By.id(PortalHtmlElements.PORTAL_LINK_LOGOUT))
                .click();
        log("Login out from OSCM Portal successfully");
    }

    /**
     * Attempts a login to the OSCM marketplace with the given credentials. Note
     * that this method assumes the webdriver to be at the login page.
     * 
     * @param user
     *            the user name
     * @param password
     *            the password
     * @throws Exception
     */
    public void loginMarketplace(String user, String password,
            String supplierOrgId) throws Exception {
        visitMarketplace(PortalPathSegments.URL_MARKETPLACE_ID + supplierOrgId);

        if (verifyFoundElement(
                By.id(PortalHtmlElements.MARKETPLACE_LINK_LOGOUT))) {
            driver.findElement(
                    By.id(PortalHtmlElements.MARKETPLACE_LINK_LOGOUT)).click();
            waitForElement(By.id(PortalHtmlElements.MARKETPLACE_LINKTEXT_LOGIN),
                    WebTester.IMPLICIT_WAIT);
        }

        if (verifyFoundElement(
                By.linkText(PortalHtmlElements.MARKETPLACE_LINKTEXT_LOGIN))) {
            driver.findElement(
                    By.linkText(PortalHtmlElements.MARKETPLACE_LINKTEXT_LOGIN))
                    .click();
            waitForElement(By.id(PortalHtmlElements.MARKETPLACE_BUTTON_LOGIN),
                    WebTester.IMPLICIT_WAIT);
        }

        WebElement userInput = driver.findElement(
                By.id(PortalHtmlElements.MARKETPLACE_INPUT_USERID));
        userInput.sendKeys(user);
        WebElement pwdInput = driver.findElement(
                By.id(PortalHtmlElements.MARKETPLACE_INPUT_PASSWORD));
        pwdInput.sendKeys(password);

        driver.findElement(By.id(PortalHtmlElements.MARKETPLACE_BUTTON_LOGIN))
                .click();
        if (verifyFoundElement(
                By.id(PortalHtmlElements.MARKETPLACE_SPAN_WELCOME))) {
            log("Login to OSCM Marketplace successfully with userid:" + user);
        } else {
            String info = "Login to Marketplace Portal failed with userid:"
                    + user;
            log(info);
            throw new LoginException(info);
        }

    }

    /**
     * Navigates the webdriver to the given page of the OSCM marketplace.
     * 
     * @param page
     *            the page of the portal
     */
    public void visitMarketplace(String context) {
        String target = String.format(BASE_PATH_MARKETPLACE, baseUrl, context);

        driver.navigate().to(target);

        System.out.println("Goto " + target);
    }

    /**
     * Logs out the current user from the OSCM marketplace. Note that this
     * method assumes that there is a logged in user and that the driverApp is
     * at a marketplace page.
     */
    public void logoutMarketplace() {
        driver.findElement(By.id(PortalHtmlElements.MARKETPLACE_LINK_LOGOUT))
                .click();
        System.out.println("Logout OSCM Marketplace");
    }

    /**
     * found the text between two given text in String
     * 
     * @param msg
     * @return
     */
    public String getCreatedId(String msg) {

        return findTextBetween(msg, "ID ", " has");

    }

    /**
     * Reads the error message from the page notification.
     * 
     * @return the error message
     * @throws NoSuchElementException
     *             if error message is not present
     */
    public String readErrorMessage() {
        WebElement element = driver
                .findElement(By.id(PortalHtmlElements.PORTAL_SPAN_ERRORS));
        return element
                .findElement(By.className(PortalHtmlElements.PORTAL_ERRORCLASS))
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
        WebElement element = driver
                .findElement(By.id(PortalHtmlElements.PORTAL_SPAN_INFOS));
        return element
                .findElement(By.className(PortalHtmlElements.PORTAL_INFOCLASS))
                .getText();
    }

    public boolean getExecutionResult() {
        String idPanel = PortalHtmlElements.PORTAL_DIV_SHOWMESSAGE;
        if (driver.getCurrentUrl().contains("/marketplace/"))
            idPanel = PortalHtmlElements.MARKETPLACE_SPAN_SHOWMESSAGE;
        waitForElement(By.id(idPanel), getWaitingTime());
        if (!verifyFoundElement(By.id(PortalHtmlElements.PORTAL_SPAN_ERRORS))
                && verifyFoundElement(
                        By.id(PortalHtmlElements.PORTAL_SPAN_INFOS))) {
            log(readInfoMessage());
            return true;
        } else {
            log(readErrorMessage());
            return false;
        }
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
     * Returns the configured email address.
     * 
     * @return the email address
     */
    public String getEmailAddress() {
        return address;
    }

}
