/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.app.vmware.business.Controller;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.remote.bes.BesClient;
import org.oscm.app.vmware.remote.bes.Credentials;
import org.oscm.intf.IdentityService;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all UI threads.
 */
public abstract class UiThreadBase implements Runnable {

    public final static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyyMMdd-h:mm:ssa");
    public final static String NEWLINE = "<br />";

    private static final Logger logger = LoggerFactory
            .getLogger(UiBeanBase.class);

    public BesClient bes;
    public APPlatformService platformService;
    public Credentials adminCredentials;
    public Credentials tpCredentials;
    public Credentials custCredentials;
    private String emailNotification;
    public volatile int progressMax;
    public volatile int progressValue;
    protected volatile StringBuffer progressStatus;
    private volatile boolean cancelRequested;

    public UiThreadBase() {
        this.bes = new BesClient();
        this.progressMax = 1;
        this.progressValue = 0;
        this.progressStatus = new StringBuffer();
        this.platformService = APPlatformServiceFactory.getInstance();

        progressStatus.append(sdf.format(Calendar.getInstance().getTime())
                + " - Import started.");
        progressStatus.append(NEWLINE);
    }

    public Logger getLogger() {
        return logger;
    }

    public void setCustCredentials(Credentials custCredentials) {
        this.custCredentials = custCredentials;
    }

    public void setTpCredentials(Credentials tpCredentials) {
        this.tpCredentials = tpCredentials;
    }

    public void setBes(BesClient bes) {
        this.bes = bes;
    }

    /**
     * Action to be implemented by subclass
     */
    public abstract void doAsnycAction(VMPropertyHandler settings)
            throws Exception;

    @Override
    public final void run() {
        try {
            addLogln("Establish CT-MG connection...");
            VMPropertyHandler settings = getVMwareAPPSettings();
            setupConnections(settings);
            doAsnycAction(settings);
        } catch (Throwable t) {
            progressValue = progressMax;
            addLogError(t);
        } finally {
            if (emailNotification != null && emailNotification.length() > 0) {
                sendResultMail(emailNotification);
            }
        }
    }

    /**
     * Setup connections and services.
     */
    private void setupConnections(VMPropertyHandler settings) throws Exception {
        Credentials iniCredentials = settings.getTPUser();
        IdentityService idSvc = bes.getWebService(IdentityService.class,
                iniCredentials);
        if (custCredentials != null) {
            custCredentials.setUserKey(getUserKeyById(idSvc,
                    custCredentials.getUserId()));
        }
        if (adminCredentials != null) {
            adminCredentials.setUserKey(getUserKeyById(idSvc,
                    adminCredentials.getUserId()));
        }

        idSvc = bes.getWebService(IdentityService.class, tpCredentials);
        VOUserDetails votpUserDetails = idSvc.getCurrentUserDetails();
        emailNotification = votpUserDetails.getEMail();
        tpCredentials.setOrgId(votpUserDetails.getOrganizationId());
    }

    /**
     * Get controller configuration from APP.
     */
    private VMPropertyHandler getVMwareAPPSettings() throws Exception {
        HashMap<String, Setting> ctrlSettings = platformService
                .getControllerSettings(Controller.ID,
                        tpCredentials.toPasswordAuthentication());
        ProvisioningSettings settings = new ProvisioningSettings(
                new HashMap<String, Setting>(), ctrlSettings,
                Messages.DEFAULT_LOCALE);
        return new VMPropertyHandler(settings);
    }

    /**
     * Send logfile to technical provider after process has been finished
     */
    public void sendResultMail(String recipient) {
        String subject = Messages
                .get("en", "mail_VM_import_completion.subject");
        String text = Messages.get("en", "mail_VM_import_completion.text",
                new Object[] { getProgressStatus() });
        // Convert line breaks
        text = text.replaceAll("<br\\s*/>", "\r\n");
        // Remove error style
        text = text.replaceAll("<b><font color='red'>", "");
        text = text.replaceAll("</font></b>", "\r\n");
        try {
            platformService.sendMail(Collections.singletonList(recipient),
                    subject, text);
        } catch (Exception e) {
            addLogError(e);
        }
    }

    /**
     * Returns current progress in percent.
     */
    public float getProgress() {
        return (progressMax != 0) ? ((float) progressValue / progressMax) : 0;
    }

    /**
     * Returns current log output.
     */
    public String getProgressStatus() {
        return progressStatus.toString();
    }

    /**
     * Cancel any currently running import.
     */
    public void cancelThread() {
        cancelRequested = true;
        addLogln("User requested cancel...");
    }

    /**
     * Checks whether cancel has been requested.
     */
    public void checkCancel() throws InterruptedException {
        if (cancelRequested) {
            throw new InterruptedException("Canceled by user.");
        }
    }

    public void addLog(String message) {
        logger.debug(message);
        progressStatus.append(message);
    }

    public void addLogln(String message) {
        logger.debug(message);
        progressStatus.append(message);
        progressStatus.append(NEWLINE);
    }

    /**
     * Add log error output
     */
    public void addLogError(Throwable t) {
        String errmsg = (t.getMessage() != null) ? t.getMessage() : t
                .getClass().getName();

        logger.error(errmsg, t);
        addLogln("<b><font color='red'>*** " + errmsg + "</font></b>");
    }

    /**
     * Retrieve user key by its id.
     */
    private long getUserKeyById(IdentityService idSvc, String userId)
            throws Exception {
        VOUser votpUser = new VOUser();
        votpUser.setUserId(userId);
        votpUser = idSvc.getUser(votpUser);
        return votpUser.getKey();
    }

}
