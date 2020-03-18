/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 14.03.2013                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.usesubscriptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.subscriptions.OperationModel;
import org.oscm.internal.subscriptions.OperationParameterModel;
import org.oscm.internal.subscriptions.POSubscription;
import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.triggerprocess.TriggerProcessesService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOServiceOperationParameterValues;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTechnicalServiceOperation;
import org.oscm.string.Strings;
import org.oscm.types.constants.Configuration;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;

@ManagedBean
@ViewScoped
public class MySubscriptionsCtrl implements Serializable {

    private static final long serialVersionUID = -9209968842729517052L;

    private static final Logger LOGGER = LogManager.getLogger(MySubscriptionsCtrl.class);

    @ManagedProperty(value = "#{mySubscriptionsLazyDataModel}")
    private MySubscriptionsLazyDataModel model;

    @ManagedProperty(value = "#{appBean}")
    ApplicationBean applicationBean;

    @ManagedProperty(value = "#{myTriggerProcessesModel}")
    private MyTriggerProcessesModel myTriggerProcessesModel;

    UiDelegate ui = new UiDelegate();
    String selectId;

    public static final String OUTCOME_ERROR = "error";
    public static final String OUTCOME_SUCCESS = "success";
    public static final String INFO_OPERATION_EXECUTED = "info.operation.executed";
    public static final String ERROR_SUBSCRIPTION_CONCURRENTMODIFY = "error.subscription.concurrentModify";

    public ApplicationBean getApplicationBean() {
        return applicationBean;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    /**
     * Services injected through setters.
     */
    SubscriptionsService subscriptionsService;
    SubscriptionService subscriptionService;

    @EJB
    TriggerProcessesService triggerProcessService;

    @EJB
    ConfigurationService config;

    @PostConstruct
    public void initialize() {
        initializeTriggerSubscriptions();
        checkSelectedSubscription();
    }

    public void initializeTriggerSubscriptions() {
        myTriggerProcessesModel.setWaitingForApprovalSubs(
                triggerProcessService.getMyWaitingForApprovalSubscriptions()
                        .getResultList(POSubscription.class));
    }

    @EJB
    public void setSubscriptionsService(
            SubscriptionsService subscriptionsService) {
        this.subscriptionsService = subscriptionsService;
    }

    @EJB
    public void setSubscriptionService(
            SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public MySubscriptionsLazyDataModel getModel() {
        return model;
    }

    public void setModel(MySubscriptionsLazyDataModel model) {
        this.model = model;
    }

    /**
     * Execute the selected operation for the selected subscription
     *
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     * @return the logical outcome.
     */
    public void executeOperation() throws SaaSApplicationException {
        POSubscription sub = model.getSelectedSubscription();
        if (sub == null) {
            return;
        }
        OperationModel selectedOperation = sub.getSelectedOperation();
        if (selectedOperation == null
                || selectedOperation.getOperation() == null) {
            return;
        }
        VOTechnicalServiceOperation operation = selectedOperation
                .getOperation();
        try {
            subscriptionService.executeServiceOperation(sub.getVOSubscription(),
                    operation);
        } catch (ConcurrentModificationException e) {
            ui.handleError(null, ERROR_SUBSCRIPTION_CONCURRENTMODIFY);
            return;
        }
        ui.handle(INFO_OPERATION_EXECUTED, operation.getOperationName());
    }

    VOTechnicalServiceOperation findSelectedOperation(VOSubscription sub,
            String operationId) {
        List<VOTechnicalServiceOperation> ops = sub
                .getTechnicalServiceOperations();
        for (VOTechnicalServiceOperation op : ops) {
            if (op.getOperationId().equals(operationId)) {
                return op;
            }
        }
        return null;
    }

    public void operationChanged() {
        POSubscription subscription = model.getSelectedSubscription();
        String operationId = subscription.getSelectedOperationId();
        if (Strings.isEmpty(operationId)) {
            subscription.setSelectedOperation(null);
            subscription.setSelectedOperationId(null);
            subscription.setExecuteDisabled(true);
        } else {
            VOTechnicalServiceOperation op = findSelectedOperation(
                    subscription.getVOSubscription(), operationId);
            OperationModel operationModel = new OperationModel();
            operationModel.setOperation(op);

            try {
                operationModel.setParameters(
                        convert(op, subscription.getVOSubscription()));
            } catch (SaaSApplicationException e) {
                subscription.setExecuteDisabled(true);
                ui.handleException(e);
            }
            subscription.setSelectedOperation(operationModel);
            subscription.setExecuteDisabled(false);
        }
    }

    List<OperationParameterModel> convert(VOTechnicalServiceOperation op,
            VOSubscription sub) throws SaaSApplicationException {
        Map<String, List<String>> paramValues = new HashMap<>();
        if (requestValuesNecessary(op)) {
            List<VOServiceOperationParameterValues> values = subscriptionService
                    .getServiceOperationParameterValues(sub, op);
            for (VOServiceOperationParameterValues v : values) {
                paramValues.put(v.getParameterId(), v.getValues());
            }
        }
        List<OperationParameterModel> result = new LinkedList<>();
        List<VOServiceOperationParameter> list = op.getOperationParameters();
        for (VOServiceOperationParameter param : list) {
            OperationParameterModel opm = new OperationParameterModel();
            opm.setParameter(param);
            if (paramValues.containsKey(param.getParameterId())) {
                opm.setValues(convert(paramValues.get(param.getParameterId())));
            }
            result.add(opm);
        }
        return result;
    }

    List<SelectItem> convert(List<String> list) {
        List<SelectItem> result = new LinkedList<>();
        if (list != null) {
            for (String s : list) {
                result.add(new SelectItem(s, s));
            }
        }
        return result;
    }

    boolean requestValuesNecessary(VOTechnicalServiceOperation op) {
        List<VOServiceOperationParameter> list = op.getOperationParameters();
        for (VOServiceOperationParameter p : list) {
            if (p.getType().isRequestValues()) {
                return true;
            }
        }
        return false;
    }

    public String getSelectId() {
        return selectId;
    }

    public void setSelectId(String selectId) {
        this.selectId = selectId;
    }

    public MyTriggerProcessesModel getMyTriggerProcessesModel() {
        return myTriggerProcessesModel;
    }

    public void setMyTriggerProcessesModel(
            MyTriggerProcessesModel myTriggerProcessesModel) {
        this.myTriggerProcessesModel = myTriggerProcessesModel;
    }

    public void validateSubscriptionStatus() {
        String subKey = model.getSelectedSubscriptionId();
        POSubscription mySubscriptionDetails = subscriptionsService
                .getMySubscriptionDetails(Long.parseLong(subKey));
        if (mySubscriptionDetails == null) {
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR,
                    BaseBean.ERROR_SUBSCRIPTION_MODIFIED_OR_DELETED_CONCURRENTLY,
                    null);
            model.setSelectedSubscription(null);
            model.setSelectedSubscriptionId(null);
        } else {
            model.setSelectedSubscription(mySubscriptionDetails);
        }
    }

    public void checkSelectedSubscription() {
        String subKey = model.getSelectedSubscriptionId();
        if (subKey != null) {
            POSubscription mySubscriptionDetails = subscriptionsService
                    .getMySubscriptionDetails(Long.parseLong(subKey));
            if (mySubscriptionDetails == null) {
                model.setSelectedSubscription(null);
                model.setSelectedSubscriptionId(null);
            }
        }
    }

    public String getCustomTabUrlWithParameters() {

        // load and encode parameters
        String orgId = encodeBase64(
                model.getSelectedSubscription().getOrganizationId());
        String subId = encodeBase64(
                model.getSelectedSubscription().getSubscriptionId());
        String instId = encodeBase64(
                model.getSelectedSubscription().getServiceInstanceId());
        String timestamp = Long.toString(System.currentTimeMillis());

        // build token string
        String token = instId + subId + orgId + timestamp;

        // load config settings for keystore
        VOConfigurationSetting settingLoc = config.getVOConfigurationSetting(
                ConfigurationKey.SSO_SIGNING_KEYSTORE,
                Configuration.GLOBAL_CONTEXT);

        VOConfigurationSetting settingPwd = config.getVOConfigurationSetting(
                ConfigurationKey.SSO_SIGNING_KEYSTORE_PASS,
                Configuration.GLOBAL_CONTEXT);

        VOConfigurationSetting settingAlias = config.getVOConfigurationSetting(
                ConfigurationKey.SSO_SIGNING_KEY_ALIAS,
                Configuration.GLOBAL_CONTEXT);

        if (settingLoc == null || settingPwd == null || settingAlias == null) {
            LOGGER.error("Missing configuration settings for token creation");
            return "";
        }

        String loc = settingLoc.getValue();
        String pwd = settingPwd.getValue();
        String alias = settingAlias.getValue();

        InputStream is = null;
        try {

            // create token hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(token.getBytes(StandardCharsets.UTF_8));
            byte[] tokenHash = md.digest();

            // load keystore from file
            is = new FileInputStream(loc);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, pwd.toCharArray());

            // get private key for alias
            Key key = keystore.getKey(alias, pwd.toCharArray());

            if (key == null) {
                LOGGER.error("Unable to retrieve private key from keystore");
                return "";
            }

            // encrypt and encode token hash
            Cipher c = Cipher.getInstance(key.getAlgorithm());
            c.init(Cipher.ENCRYPT_MODE, key);

            String tokenSignature = encodeBase64(c.doFinal(tokenHash));

            // build URI
            UriBuilder builder = UriBuilder.fromPath(
                    model.getSelectedSubscription().getCustomTabUrl());
            builder.queryParam("lang", ui.getViewLocale().toLanguageTag());
            builder.queryParam("instId", instId);
            builder.queryParam("orgId", orgId);
            builder.queryParam("subId", subId);
            builder.queryParam("timestamp", timestamp);
            builder.queryParam("signature", tokenSignature);

            return builder.build().toString();

        } catch (KeyStoreException | CertificateException | IOException
                | UnrecoverableKeyException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | NoSuchAlgorithmException e) {

            LOGGER.error("Unable to build custom tab URI", e);
            return "";
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }

    }

    private String encodeBase64(String str) {
        return Base64.encodeBase64URLSafeString(
                str.getBytes(StandardCharsets.UTF_8));
    }

    private String encodeBase64(byte[] b) {
        return Base64.encodeBase64URLSafeString(b);
    }
}
