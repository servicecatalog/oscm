/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Oct 20, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.MandatoryUdaMissingException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.model.UdaRow;

/**
 * Manages the custom attributes of all suppliers that are allowed to publish to
 * the marketplace for the customer.
 * 
 * @author miethaner
 */
@ManagedBean(name = "manageAttributesCtrl")
@ViewScoped
public class ManageAttributesCtrl {

    private static final Logger LOGGER = LogManager.getLogger(ManageAttributesCtrl.class);

    @ManagedProperty(value = "#{manageAttributesModel}")
    private ManageAttributesModel model;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    @EJB
    private AccountService accountService;

    @EJB
    private MarketplaceService marketplaceService;

    public ManageAttributesModel getModel() {
        return model;
    }

    public void setModel(ManageAttributesModel model) {
        this.model = model;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public void setMarketplaceService(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @PostConstruct
    public void construct() {
        model.setAttributeMap(initModel());
    }

    private Map<String, UdaRow> initModel() {

        Map<String, UdaRow> result = new HashMap<>();
        VOOrganization customer = userBean.getOrganizationBean()
                .getOrganization();

        try {
            String marketplaceId = sessionBean.getMarketplaceId();
            List<VOOrganization> orgList = marketplaceService
                    .getSuppliersForMarketplace(marketplaceId);

            for (VOOrganization org : orgList) {

                List<VOUdaDefinition> udaDefList = accountService
                        .getUdaDefinitionsForCustomer(org.getOrganizationId());

                for (VOUdaDefinition udaDef : udaDefList) {
                    if (udaDef.getTargetType().equals("CUSTOMER")) {
                        VOUda uda = new VOUda();
                        uda.setUdaDefinition(udaDef);
                        uda.setTargetObjectKey(customer.getKey());
                        uda.setUdaValue(udaDef.getDefaultValue());
                        result.put(udaDef.getUdaId(),
                                new UdaRow(udaDef, uda, org));
                    }
                }

                List<VOUda> udaList = accountService.getUdasForCustomer(
                        "CUSTOMER", customer.getKey(), org.getOrganizationId());
                for (VOUda uda : udaList) {
                    result.put(uda.getUdaDefinition().getUdaId(),
                            new UdaRow(uda.getUdaDefinition(), uda, org));
                }
            }
        } catch (ObjectNotFoundException | OperationNotPermittedException
                | ValidationException | OrganizationAuthoritiesException e) {
            LOGGER.error(e.getMessage());
        }
        initPasswordFields(result);
        return result;
    }

    private void initPasswordFields(Map<String, UdaRow> udas) {
        for (Map.Entry<String, UdaRow> row : udas.entrySet()) {
            row.getValue().initPasswordValueToStore();
        }
    }

    public String saveAttributes() {

        List<VOUda> list = new ArrayList<>();

        for (UdaRow row : model.getAttributes()) {

            VOUda uda = row.getUda();
            row.rewriteEncryptedValues();
            list.add(uda);
        }

        try {
            accountService.saveUdas(list);
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_INFO,
                    BaseBean.INFO_UDA_SAVED, null);

            return BaseBean.OUTCOME_SUCCESS;

        } catch (ObjectNotFoundException | NonUniqueBusinessKeyException
                | ValidationException | OperationNotPermittedException
                | ConcurrentModificationException | MandatoryUdaMissingException
                | OrganizationAuthoritiesException e) {
            LOGGER.error(e.getMessage());

            return BaseBean.OUTCOME_ERROR;
        }

    }
}
