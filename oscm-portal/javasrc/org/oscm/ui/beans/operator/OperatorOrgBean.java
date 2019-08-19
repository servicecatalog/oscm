/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: walker                                                     
 *                                                                              
 *  Creation Date: 31.01.2011                                                      
 *                                                                              
 *  Completion Time: <date>                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans.operator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.oscm.converter.PropertiesLoader;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.tenant.ManageTenantService;
import org.oscm.internal.tenant.POTenant;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.LdapProperties;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPAccount;
import org.oscm.internal.vo.VOPSPSetting;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.ImageUploader;
import org.oscm.ui.model.PSPSettingRow;

/**
 * This class is responsible to provide the functionality to create and manage
 * organization to the operator user.
 */
@ViewScoped
@ManagedBean(name = "operatorOrgBean")
public class OperatorOrgBean extends BaseOperatorBean implements Serializable {

    private static final long serialVersionUID = -295463152427849927L;
    public static final String APPLICATION_BEAN = "appBean";

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(OperatorOrgBean.class);

    /**
     * Contains the IDs of the HTML fields corresponding to the attributes of an
     * Organization which are only mandatory for a supplier or a technology
     * provider, but not for a customer.
     */
    private static final Set<String> CONDITIONAL_MANDATORY_FIELD_IDS = new HashSet<String>(
            Arrays.asList("requiredEmail", "requiredPhone", "requiredUrl",
                    "requiredName", "requiredAddress"));

    @ManagedProperty(value = "#{operatorSelectOrgBean}")
    private OperatorSelectOrgBean operatorSelectOrgBean;

    // The new organization and administrator user
    private VOOrganization newOrganization = null;
    private VOUserDetails newAdministrator = null;
    private VOPSPAccount newPspAccount = null;
    private VOPaymentType selectedPaymentType;
    private VOPSP newPSP = null;
    private String pspId;

    // Contains _all_ roles of the new organization
    private EnumSet<OrganizationRoleType> newRoles = EnumSet
            .noneOf(OrganizationRoleType.class);

    private VOOperatorOrganization selectedOrganization = null;
    private List<VOPSPAccount> selectedPSPAccounts = null;
    private VOPSP selectedPSP = null;
    private Long selectedPspAccountKey = null;
    private String pspAccountPaymentTypesAsString = null;
    private List<SelectItem> selectableMarketplaces = new ArrayList<>();
    private String selectedMarketplace;
    private String selectedTenant;

    private ImageUploader imageUploader = new ImageUploader(
            ImageType.ORGANIZATION_IMAGE);

    private final List<PSPSettingRow> pspSettingRowList = new ArrayList<PSPSettingRow>();
    private UploadedFile organizationProperties;
    private boolean ldapManaged;
    private boolean ldapSettingVisible;
    private boolean internalAuthMode;

    transient ApplicationBean appBean;
    private Long selectedPaymentTypeKey;

    @EJB
    ManageTenantService manageTenantService;

    @ManagedProperty(value = "#{menuBean}")
    MenuBean menuBean;

    /**
     * Registers the newly created organization.
     * 
     * @return <code>OUTCOME_SUCCESS</code> if the organization was successfully
     *         registered.
     * @throws ImageException
     *             Thrown in case the access to the uploaded file failed.
     */
    public String createOrganization() throws SaaSApplicationException {

        VOOrganization newVoOrganization = null;

        OrganizationRoleType[] selectedRoles = newRoles
                .toArray(new OrganizationRoleType[newRoles.size()]);
        LdapProperties ldapProperties = null;
        if (ldapManaged && organizationProperties != null) {
            try {
                Properties props = PropertiesLoader
                        .loadProperties(organizationProperties.getInputStream());
                ldapProperties = new LdapProperties(props);
            } catch (IOException e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_CREATE_ORGANIZATION);
                addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_UPLOAD);
                return OUTCOME_ERROR;
            }
        }
        if (!newRoles.contains(OrganizationRoleType.SUPPLIER)
                && null != newOrganization.getOperatorRevenueShare()) {
            newOrganization.setOperatorRevenueShare(null);
        }
        if (StringUtils.isNotBlank(getSelectedTenant())) {
            Long tenantKey = Long.valueOf(getSelectedTenant());
            newOrganization.setTenantKey(tenantKey.longValue());
            newAdministrator = getIdService().loadUserDetailsFromOIDCProvider(
                    newAdministrator.getUserId(), getSelectedTenantId(), getIdToken());
        }
        
        newVoOrganization = getOperatorService().registerOrganization(
                newOrganization, getImageUploader().getVOImageResource(),
                newAdministrator, ldapProperties, selectedMarketplace,
                selectedRoles);

        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_ORGANIZATION_CREATED,
                newVoOrganization.getOrganizationId());

        // Reset the form
        newOrganization = null;
        newAdministrator = null;
        newRoles.clear();
        organizationProperties = null;
        selectedMarketplace = null;
        selectedTenant = null;

        return OUTCOME_SUCCESS;
    }

    private String getSelectedTenantId() {
        for (SelectItem selectedTenantItem : getSelectableTenants()) {
            if (selectedTenantItem.getValue().toString().equals(selectedTenant)) {
                return selectedTenantItem.getLabel();
            }
        }
        return ""; 
    }
    
    private String getIdToken() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(true);
        String idToken = (String) session
                .getAttribute(Constants.SESS_ATTR_ACCESS_TOKEN);
        return idToken;
    }

    // *****************************************************
    // *** Getter and setter for _marketplaces ***
    // *****************************************************

    /**
     * @return all marketplaces which are owned and to which the supplier can
     *         publish
     */
    public List<SelectItem> getSelectableMarketplaces() {
        if (!isLoggedInAndPlatformOperator()) {
            return selectableMarketplaces;
        }
        List<VOMarketplace> marketplaces;
        Long tenantKey;
        try {
            tenantKey = Long.valueOf(this.selectedTenant);
        } catch (Exception exc) {
            //Selected tenant is not parseable, results will be displayed for default.
            tenantKey = null;
        }

        try {
            marketplaces = getMarketplaceService()
                    .getAllMarketplacesForTenant(
                            tenantKey);
        } catch (ObjectNotFoundException e) {
            selectableMarketplaces = new ArrayList<>();
            return selectableMarketplaces;
        }
        selectableMarketplaces = toMarketplacesList(marketplaces);
        return selectableMarketplaces;
    }

    private List<SelectItem> toMarketplacesList(List<VOMarketplace> marketplaces) {
        List<SelectItem> result = new ArrayList<SelectItem>();
        if (marketplaces == null) {
            return result;
        }
        for (VOMarketplace vMp : marketplaces) {
            result.add(new SelectItem(vMp.getMarketplaceId(), getLabel(vMp)));
        }
        return result;
    }

    public List<SelectItem> getSelectableTenants() {
        if (!isLoggedInAndPlatformOperator()) {
            return null;
        }
        List<POTenant> tenants = manageTenantService.getAllTenants();
        List<SelectItem> result = new ArrayList<SelectItem>();
        for (POTenant poTenant : tenants) {
            result.add(new SelectItem(Long.valueOf(poTenant.getKey()), poTenant.getName()));
        }
        return result;
    }

    /**
     * For Unit Test only
     * */
    @Override
    protected boolean isLoggedInAndPlatformOperator() {
        return super.isLoggedInAndPlatformOperator();
    }

    /**
     * For Unit Test only
     * */
    @Override
    protected MarketplaceService getMarketplaceService() {
        return super.getMarketplaceService();
    }

    /**
     * For Unit Test only
     * */
    @Override
    protected void addMessage(final String clientId,
            final FacesMessage.Severity severity, final String key,
            final Object[] params) {
        super.addMessage(clientId, severity, key, params);
    }

    String getLabel(VOMarketplace vMp) {
        if (vMp == null) {
            return "";
        }
        if (vMp.getName() == null || Strings.isEmpty(vMp.getName())) {
            return vMp.getMarketplaceId();
        }
        return String.format("%s (%s)", vMp.getName(), vMp.getMarketplaceId());
    }

    public void prepareDataForNewPaymentType() {
        selectedPaymentType = new VOPaymentType();
        selectedPaymentType
                .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
    }

    /**
     * Prepares payment type data for update
     */
    public void prepareDataForEditPaymentType() {
        if (selectedPaymentTypeKey == null) {
            return;
        }
        for (VOPaymentType voPaymentType : getSelectedPSP().getPaymentTypes()) {
            if (selectedPaymentTypeKey.equals(new Long(voPaymentType.getKey()))) {
                selectedPaymentType = voPaymentType;
                return;
            }
        }
    }

    /**
     * Registers payment types for PSP.
     * 
     * @return <code>OUTCOME_SUCCESS</code> if the payment type was successfully
     *         registered, otherwise <code>OUTCOME_ERROR</code>.
     */
    public void savePaymentType() throws SaaSApplicationException {
        getOperatorService().savePaymentType(getSelectedPSP(),
                getSelectedPaymentType());
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_PAYMENT_TYPE_SAVED);
    }

    /**
     * http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=
     * 1775079 Registers payment types for an organization.
     * 
     * @return <code>OUTCOME_SUCCESS</code> if the payment type for organization
     *         was successfully registered, otherwise <code>OUTCOME_ERROR</code>
     *         .
     */
    public String savePaymentTypeForOrganization()
            throws SaaSApplicationException {

        final List<VOPSP> psps = getPSPs();
        final Map<String, String> ptMap = new HashMap<String, String>();
        for (VOPSP psp : psps) {
            for (VOPaymentType pt : psp.getPaymentTypes()) {
                ptMap.put("" + pt.getKey(), pt.getPaymentTypeId());
            }
        }

        final String[] pts = pspAccountPaymentTypesAsString.split(",");
        Set<String> ptsSet = new HashSet<String>();
        for (String s : pts) {
            if (s.trim().length() > 0) {
                if (ptMap.containsKey(s)) {
                    ptsSet.add(ptMap.get(s));
                } else {
                    throw new SaaSSystemException("payment type fpr key " + s
                            + " not found!");
                }
            }
        }
        getOperatorService().savePSPAccount(getSelectedOrganization(),
                getSelectedPspAccount());
        getOperatorService().addAvailablePaymentTypes(
                getSelectedOrganization(), ptsSet);

        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_PAYMENT_INFO_SAVED);

        newPspAccount = null;
        selectedOrganization = null;
        selectedPSP = null;
        selectedPspAccountKey = null;
        pspAccountPaymentTypesAsString = null;

        return OUTCOME_SUCCESS;
    }

    /**
     * Registers the newly created PSP.
     * 
     * @return <code>OUTCOME_SUCCESS</code> if the organization was successfully
     *         registered, otherwise <code>OUTCOME_ERROR</code>.
     */
    public String createPSP() throws SaaSApplicationException {

        VOPSP newVoPSP = getOperatorService().savePSP(newPSP);

        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_PSP_CREATED,
                newVoPSP.getId());

        // Reset the form
        newPSP = null;

        return OUTCOME_SUCCESS;
    }

    /**
     * This functions persists the changed data of the currently selected
     * organization.
     * 
     * @return <code>OUTCOME_SUCCESS</code> if the organization was successfully
     *         updated.
     * 
     * @throws SaaSApplicationException
     *             if any problems occurs while persisting the values
     * @throws ImageException
     *             Thrown in case the access to the uploaded file failed.
     */
    public String saveOrganization() throws SaaSApplicationException {

        OperatorService operatorService = getOperatorService();
        VOOperatorOrganization org = getSelectedOrganization();

        long updatedTenantKey = selectedOrganization.getTenantKey();

        manageTenantService.validateOrgUsersUniqnessInTenant(
                org.getOrganizationId(), updatedTenantKey);

        selectedOrganization = operatorService.updateOrganization(org,
                getImageUploader().getVOImageResource());

        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_ORGANIZATION_SAVED,
                selectedOrganization.getOrganizationId());

        // make sure the next page access will trigger the reload of the
        // selected organization the next time getSelectedOrg will be called
        this.selectedOrganization = null;
        selectedPSPAccounts = null;

        return OUTCOME_SUCCESS;
    }

    /**
     * This functions persists the changed data of the currently selected
     * organization.
     * 
     * @throws SaaSApplicationException
     *             if any problems occurs while persisting the values
     * @throws ImageException
     *             Thrown in case the access to the uploaded file failed.
     */
    public void savePSP() throws SaaSApplicationException {
        if (!isTokenValid() && pspId != null) {
            updateSelectedPSP();
        }

        final List<VOPSPSetting> list = new ArrayList<>();
        for (PSPSettingRow row : pspSettingRowList) {
            if (!row.isSelected()
                    && (row.getDefinition().getSettingKey() != null
                            && row.getDefinition().getSettingKey().trim()
                                    .length() > 0 || row.getDefinition()
                            .getSettingValue() != null
                            && row.getDefinition().getSettingValue().trim()
                                    .length() > 0)) {
                list.add(row.getDefinition());
            }
        }
        final VOPSP psp = getSelectedPSP();
        psp.setPspSettings(list);

        OperatorService operatorService = getOperatorService();
        selectedPSP = operatorService.savePSP(psp);

        resetToken();
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_PSP_SAVED,
                selectedPSP.getId());

        // make sure the next page access will trigger the reload of the
        // selected organization the next time getSelectedOrg will be called
        setSelectedPSP(selectedPSP);
    }

    public OperatorSelectOrgBean getOperatorSelectOrgBean() {
        return operatorSelectOrgBean;
    }

    public void setOperatorSelectOrgBean(
            OperatorSelectOrgBean operatorSelectOrgBean) {
        this.operatorSelectOrgBean = operatorSelectOrgBean;
    }

    // *****************************************************
    // *** Getter and setter for _new_ organization ***
    // *****************************************************

    public VOOrganization getNewOrganization() {
        if (newOrganization == null) {
            newOrganization = new VOOrganization();
        }
        return newOrganization;
    }

    public VOPSP getNewPSP() {
        if (newPSP == null) {
            newPSP = new VOPSP();
        }
        return newPSP;
    }

    public VOUserDetails getNewAdministrator() {
        if (newAdministrator == null) {
            newAdministrator = new VOUserDetails();
        }
        return newAdministrator;
    }

    public boolean isNewTechnologyProvider() {
        return newRoles.contains(OrganizationRoleType.TECHNOLOGY_PROVIDER);
    }

    public void setNewTechnologyProvider(boolean setRole) {
        if (setRole && !this.isNewTechnologyProvider()) {
            newRoles.add(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        } else if (!setRole) {
            newRoles.remove(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        }
    }

    public boolean isNewSupplier() {
        return newRoles.contains(OrganizationRoleType.SUPPLIER);
    }

    public boolean isNewBroker() {
        return newRoles.contains(OrganizationRoleType.BROKER);
    }

    public void setNewBroker(boolean setRole) {
        if (setRole && !this.isNewBroker()) {
            newRoles.add(OrganizationRoleType.BROKER);
        } else if (!setRole) {
            newRoles.remove(OrganizationRoleType.BROKER);
        }
    }

    public boolean isNewReseller() {
        return newRoles.contains(OrganizationRoleType.RESELLER);
    }

    public void setNewReseller(boolean setRole) {
        if (setRole && !this.isNewReseller()) {
            newRoles.add(OrganizationRoleType.RESELLER);
        } else if (!setRole) {
            newRoles.remove(OrganizationRoleType.RESELLER);
        }
    }

    public void setNewSupplier(boolean setRole) {
        if (setRole && !this.isNewSupplier()) {
            newRoles.add(OrganizationRoleType.SUPPLIER);
        } else if (!setRole) {
            newRoles.remove(OrganizationRoleType.SUPPLIER);
        }
    }

    // *****************************************************
    // *** Getter and setter for _selected_ organization ***
    // *****************************************************

    /**
     * Returns the locally selected organization. On page load or if the
     * selected organization in the corresponding operatorSelectOrgBean have
     * changed this object will be in sync with the organization of the
     * operatorSelectOrgBean.
     */
    public VOOperatorOrganization getSelectedOrganization() {

        if (operatorSelectOrgBean.getOrganization() == null) {
            resetUIInputChildren(); // similar case for Bug#7589
            selectedOrganization = null;
            selectedPSPAccounts = null;
            pspAccountPaymentTypesAsString = null;
            // The organization is not set yet (e.g. on page load)
            return new VOOperatorOrganization();

        }

        String opSelectOrgId = operatorSelectOrgBean.getOrganizationId();
        try {
            if (selectedOrganization == null
                    || !selectedOrganization.getOrganizationId().equals(
                            opSelectOrgId)) {

                // Bug #7726: The object might have been (concurrently) changed
                // => reload the organization
                operatorSelectOrgBean.reloadOrganization();

                // On page load or after save or if the user selected another
                // organization, make sure to update the local instance of the
                // selected organization. On summit the attributes of this
                // object will might be changed so it'll be acquired directly
                // from the operator service instead of the
                // operatorSelectOrgBean
                selectedOrganization = getOperatorService().getOrganization(
                        opSelectOrgId);
                selectedPSPAccounts = null;
                pspAccountPaymentTypesAsString = null;

                // reset the '*' for the conditional mandatory attributes
                resetUIComponents(CONDITIONAL_MANDATORY_FIELD_IDS);
                resetUIInputChildren(); // similar case for Bug#7589
            }
        } catch (SaaSApplicationException e) {
            ExceptionHandler.execute(e);
        }
        if (selectedOrganization == null) {
            selectedPSPAccounts = null;
            selectedPspAccountKey = null;
        }
        return selectedOrganization;
    }

    public List<VOPSP> getPSPs() {
        return getOperatorService().getPSPs();
    }

    public List<VOPSPAccount> getPSPAccounts() {
        if (selectedPSPAccounts == null) {
            final VOOrganization org = getSelectedOrganization();
            if (org.getOrganizationId() != null) {
                try {
                    selectedPSPAccounts = getOperatorService().getPSPAccounts(
                            org);
                } catch (ObjectNotFoundException e) {
                    // will return null at the end, which is correct, since
                    // the organization has been deleted
                }
            }
        }
        return selectedPSPAccounts;
    }

    /**
     * Returns the locally selected PSP. On page load or if the selected
     * organization in the corresponding operatorSelectOrgBean have changed this
     * object will be in sync with the organization of the
     * operatorSelectOrgBean.
     */
    public VOPSP getSelectedPSP() {
        return selectedPSP;
    }

    @Override
    protected OperatorService getOperatorService() {
        return super.getOperatorService();
    }

    public void setSelectedPSP(VOPSP selectedPSP) {
        this.selectedPSP = selectedPSP;
        pspSettingRowList.clear();
        if (selectedPSP != null) {
            for (VOPSPSetting setting : selectedPSP.getPspSettings()) {
                pspSettingRowList.add(new PSPSettingRow(setting));
            }
        }
    }

    private void updateSelectedPSP() {
        final List<VOPSP> psps = getOperatorService().getPSPs();
        selectedPSP = null;
        pspSettingRowList.clear();
        for (VOPSP psp : psps) {
            if (pspId.equals("" + psp.getKey())) {
                selectedPSP = psp;
                for (VOPSPSetting setting : psp.getPspSettings()) {
                    pspSettingRowList.add(new PSPSettingRow(setting));
                }
                break;
            }
        }
    }

    // ********************************************************************
    // Methods for handling changes of the organization roles
    // of the selected organization
    // ********************************************************************

    /**
     * Returns whether the organization has the supplier or the technology
     * provider role both locally and persisted in DB.
     * 
     * @return <code>true</code> if the organization has whether the supplier or
     *         the technology provider role both locally and persisted in DB, or
     *         <code>false</code> otherwise.
     */
    public boolean isVendorDisabled() {
        return isSupplierDisabled() || isTechnologyProviderDisabled()
                || isResellerDisabled() || isBrokerDisabled();
    }

    /**
     * Returns whether the organization has the supplier or the technology
     * provider role locally set.
     * 
     * @return <code>true</code> if the organization has supplier or the
     *         technology provider role, or <code>false</code> otherwise.
     */
    public boolean isVendor() {
        return isSupplier() || isTechnologyProvider() || isReseller()
                || isBroker();
    }

    /**
     * Reflect the state of the local organization object, which might be
     * changed be the user but was not committed yet.
     * 
     * @return true is the local local organization object has the corresponding
     *         role
     */
    public boolean isTechnologyProvider() {
        return isRoleAvailable(getSelectedOrganization(),
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
    }

    /**
     * Sets or removes the corresponding role from the local organization
     * object.
     */
    public void setTechnologyProvider(boolean setRole) {
        if (setRole && !this.isTechnologyProvider()) {
            addOrgRole(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        } else if (!setRole) {
            removeOrgRole(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        }
    }

    /**
     * Sets or removes the corresponding role from the local organization
     * object.
     */
    public void setReseller(boolean setRole) {
        if (setRole && !this.isReseller()) {
            addOrgRole(OrganizationRoleType.RESELLER);
        } else if (!setRole) {
            removeOrgRole(OrganizationRoleType.RESELLER);
        }
    }

    /**
     * Sets or removes the corresponding role from the local organization
     * object.
     */
    public void setBroker(boolean setRole) {
        if (setRole && !this.isBroker()) {
            addOrgRole(OrganizationRoleType.BROKER);
        } else if (!setRole) {
            removeOrgRole(OrganizationRoleType.BROKER);
        }
    }

    /**
     * Reflects the state of the role in relation to persisted object.
     * 
     * @return true if the role is set locally and in the DB object.
     */
    public boolean isTechnologyProviderDisabled() {
        return isTechnologyProvider()
                && isPersistedRole(OrganizationRoleType.TECHNOLOGY_PROVIDER);
    }

    /**
     * Reflects the state of the role in relation to persisted object.
     * 
     * @return true if the role is set locally and in the DB object.
     */
    public boolean isBrokerDisabled() {
        return isBroker() && isPersistedRole(OrganizationRoleType.BROKER);
    }

    /**
     * Reflects the state of the role in relation to persisted object.
     * 
     * @return true if the role is set locally and in the DB object.
     */
    public boolean isResellerDisabled() {
        return isReseller() && isPersistedRole(OrganizationRoleType.RESELLER);
    }

    /**
     * value change listener for technology provider role check-box
     */
    public void technologyProviderRoleChanged(ValueChangeEvent event) {
        Boolean checkBoxChecked = (Boolean) event.getNewValue();
        setTechnologyProvider(checkBoxChecked.booleanValue());
    }

    /**
     * value change listener for technology provider role check-box
     */
    public void resellerRoleChanged(ValueChangeEvent event) {
        Boolean checkBoxChecked = (Boolean) event.getNewValue();
        setReseller(checkBoxChecked.booleanValue());
    }

    /**
     * value change listener for technology provider role check-box
     */
    public void brokerRoleChanged(ValueChangeEvent event) {
        Boolean checkBoxChecked = (Boolean) event.getNewValue();
        setBroker(checkBoxChecked.booleanValue());
    }

    /**
     * Reflect the state of the local organization object, which might be
     * changed be the user but was not committed yet.
     * 
     * @return true is the local organization object has the corresponding role
     */
    public boolean isSupplier() {
        return isRoleAvailable(getSelectedOrganization(),
                OrganizationRoleType.SUPPLIER);
    }

    /**
     * Reflect the state of the local organization object, which might be
     * changed be the user but was not committed yet.
     * 
     * @return true is the local organization object has the corresponding role
     */
    public boolean isBroker() {
        return isRoleAvailable(getSelectedOrganization(),
                OrganizationRoleType.BROKER);
    }

    /**
     * Reflect the state of the local organization object, which might be
     * changed be the user but was not committed yet.
     * 
     * @return true is the local organization object has the corresponding role
     */
    public boolean isReseller() {
        return isRoleAvailable(getSelectedOrganization(),
                OrganizationRoleType.RESELLER);
    }

    /*
     * value change listener for supplier role check-box
     */
    public void supplierRoleChanged(ValueChangeEvent event) {
        Boolean checkBoxChecked = (Boolean) event.getNewValue();
        setSupplier(((checkBoxChecked.booleanValue())));
    }

    public void newSupplierRoleChanged(ValueChangeEvent event) {
        Boolean checkBoxChecked = (Boolean) event.getNewValue();
        this.setNewSupplier(((checkBoxChecked.booleanValue())));
    }

    public void newResellerRoleChanged(ValueChangeEvent event) {
        Boolean checkBoxChecked = (Boolean) event.getNewValue();
        this.setNewReseller(((checkBoxChecked.booleanValue())));
    }

    public void newBrokerRoleChanged(ValueChangeEvent event) {
        Boolean checkBoxChecked = (Boolean) event.getNewValue();
        this.setNewBroker(((checkBoxChecked.booleanValue())));
    }

    public void newTechnologyProviderRoleChanged(ValueChangeEvent event) {
        Boolean checkBoxChecked = (Boolean) event.getNewValue();
        this.setNewTechnologyProvider(((checkBoxChecked.booleanValue())));
    }

    /**
     * Sets of removed the corresponding role from the local organization
     * object.
     */
    public void setSupplier(boolean setRole) {
        if (setRole && !this.isSupplier()) {
            addOrgRole(OrganizationRoleType.SUPPLIER);
        } else if (!setRole) {
            removeOrgRole(OrganizationRoleType.SUPPLIER);
        }
    }

    /**
     * @return true if the currently selected and persistent organization has
     *         already the supplier role, false otherwise
     */
    public boolean isSupplierOrResellerPersisted() {
        return isPersistedRole(OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.RESELLER);
    }

    /**
     * Reflects the state of the role in relation to persisted object.
     * 
     * @return true if the role is set locally and in the DB object.
     */
    public boolean isSupplierDisabled() {
        return isSupplier() && isPersistedRole(OrganizationRoleType.SUPPLIER);
    }

    /**
     * Returns true if the passed roletype is available for the passed
     * organization.
     */
    private boolean isRoleAvailable(VOOperatorOrganization voorg,
            OrganizationRoleType... roles) {
        if (operatorSelectOrgBean.getOrganization() == null) {
            return false;
        }
        List<OrganizationRoleType> orgRoles = voorg.getOrganizationRoles();
        for (OrganizationRoleType role : roles) {
            if (orgRoles.contains(role))
                return true;
        }
        return false;
    }

    /**
     * Returns true if the passed role is set on the organization of the
     * operatorSelectOrgBean.
     */
    private boolean isPersistedRole(OrganizationRoleType... role) {
        return isRoleAvailable(operatorSelectOrgBean.getOrganization(), role);
    }

    /**
     * Adds the passed role type to the local organization.
     */
    private void addOrgRole(OrganizationRoleType type) {
        List<OrganizationRoleType> orgRoles = getSelectedOrganization()
                .getOrganizationRoles();
        if (!orgRoles.contains(type)) {
            orgRoles.add(type);
        }
    }

    /**
     * Removes the passed roletype to the local organization.
     */
    private void removeOrgRole(OrganizationRoleType type) {
        List<OrganizationRoleType> orgRoles = getSelectedOrganization()
                .getOrganizationRoles();
        orgRoles.remove(type);
    }

    // ********************************************************************
    // Methods for handling changes of the payment type
    // of the selected organization
    // ********************************************************************

    /**
     * Indicates if the corresponding payment type is available for the local
     * organization.
     */
    public boolean isCreditCardAvailable() {
        return isPaymentTypeAvailable(getSelectedOrganization(),
                PaymentInfoType.CREDIT_CARD);
    }

    /**
     * Reflects the state of the payment type in relation to persisted object.
     * 
     * @return true if the payment type is set locally and in the DB object.
     */
    public boolean isCreditCardDisabled() {
        return (isPersistedType(PaymentInfoType.CREDIT_CARD) && isCreditCardAvailable()) ? true
                : false;
    }

    /**
     * Sets or removes the corresponding payment type locally.
     */
    public void setCreditCardAvailable(boolean setpaymentType) {
        if (setpaymentType && !this.isCreditCardAvailable()) {
            addVoPayment(PaymentInfoType.CREDIT_CARD);
        } else if (!setpaymentType) {
            removeVoPayment(PaymentInfoType.CREDIT_CARD);
        }
    }

    /**
     * Indicates if the corresponding payment type is available for the local
     * organization.
     */
    public boolean isInvoiceAvailable() {
        return isPaymentTypeAvailable(getSelectedOrganization(),
                PaymentInfoType.INVOICE);
    }

    /**
     * Reflects the state of the payment type in relation to persisted object.
     * 
     * @return true if the payment type is set locally and in the DB object.
     */
    public boolean isInvoiceDisabled() {
        return (isPersistedType(PaymentInfoType.INVOICE) && isInvoiceAvailable()) ? true
                : false;
    }

    /**
     * Sets or removes the corresponding payment type locally.
     */
    public void setInvoiceAvailable(boolean setpaymentType) {
        if (setpaymentType && !this.isInvoiceAvailable()) {
            addVoPayment(PaymentInfoType.INVOICE);
        } else if (!setpaymentType) {
            removeVoPayment(PaymentInfoType.INVOICE);
        }
    }

    /**
     * Indicates if the corresponding payment type is available for the local
     * organization.
     */
    public boolean isDirectDebitAvailable() {
        return isPaymentTypeAvailable(getSelectedOrganization(),
                PaymentInfoType.DIRECT_DEBIT);
    }

    /**
     * Reflects the state of the payment type in relation to persisted object.
     * 
     * @return true if the payment type is set locally and in the DB object.
     */
    public boolean isDirectDebitDisabled() {
        return (isPersistedType(PaymentInfoType.DIRECT_DEBIT) && isDirectDebitAvailable()) ? true
                : false;
    }

    /**
     * Sets or removes the corresponding payment type locally.
     */
    public void setDirectDebitAvailable(boolean setpaymentType) {
        if (setpaymentType && !this.isDirectDebitAvailable()) {
            addVoPayment(PaymentInfoType.DIRECT_DEBIT);
        } else if (!setpaymentType) {
            removeVoPayment(PaymentInfoType.DIRECT_DEBIT);
        }
    }

    /**
     * Returns true if the passed payment type is available for the passed
     * organization.
     */
    private boolean isPaymentTypeAvailable(VOOperatorOrganization voOrg,
            PaymentInfoType type) {
        if (operatorSelectOrgBean.getOrganization() == null) {
            return false;
        }
        List<VOPaymentType> paymentTypes = voOrg.getPaymentTypes();
        for (VOPaymentType voPaymentType : paymentTypes) {
            if (voPaymentType.getPaymentTypeId().equals(type.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the passed payment type to the local organization object.
     */
    private void addVoPayment(PaymentInfoType type) {
        VOPaymentType voPaymentType = new VOPaymentType();
        voPaymentType.setPaymentTypeId(type.name());
        getSelectedOrganization().getPaymentTypes().add(voPaymentType);
    }

    /**
     * Removes the passed payment type to the local organization object.
     */
    private void removeVoPayment(PaymentInfoType type) {
        List<VOPaymentType> voPaymentTypes = getSelectedOrganization()
                .getPaymentTypes();
        for (VOPaymentType voPaymentType : voPaymentTypes) {
            if (voPaymentType.getPaymentTypeId().equals(type.name())) {
                voPaymentTypes.remove(voPaymentType);
                return;
            }
        }
    }

    /**
     * Returns true if the passed payment type is available in the object which
     * is in sync with the DB.
     */
    private boolean isPersistedType(PaymentInfoType type) {
        return isPaymentTypeAvailable(operatorSelectOrgBean.getOrganization(),
                type);
    }

    public ImageUploader getImageUploader() {
        return imageUploader;
    }

    /**
     * @param pspAccountKey
     *            the pspAccountKey to set
     */
    public void setSelectedPspAccountKey(Long pspAccountKey) {
        if (pspAccountKey != null) {
            this.selectedPspAccountKey = pspAccountKey;
            pspAccountPaymentTypesAsString = null;
        }
        newPspAccount = null;
    }

    public VOPSPAccount getSelectedPspAccount() {
        if (selectedPspAccountKey != null
                && selectedPspAccountKey.longValue() != 0
                && getPSPAccounts() != null) {
            for (VOPSPAccount acc : getPSPAccounts()) {
                if (selectedPspAccountKey.equals(new Long(acc.getKey()))) {
                    return acc;
                }
            }
        }
        if (newPspAccount == null) {
            newPspAccount = new VOPSPAccount();
            newPspAccount.setPsp(new VOPSP());
        }
        return newPspAccount;
    }

    /**
     * @param paymentTypeKey
     *            the paymentTypeKey to set
     */
    public void setSelectedPaymentTypeKey(Long paymentTypeKey) {
        selectedPaymentTypeKey = paymentTypeKey;
    }

    public void setPSPAccountPSPKey(final Long key) {
        if (key != null) {
            getSelectedPspAccount().getPsp().setKey(key.longValue());
        }
    }

    public Long getPSPAccountPSPKey() {
        return Long.valueOf(getSelectedPspAccount().getPsp().getKey());
    }

    public String getPSPAccountPaymentTypesAsString() {
        if (pspAccountPaymentTypesAsString == null) {
            if (getSelectedOrganization().getOrganizationId() == null) {
                return "";
            }
            String s = ",";
            final List<VOPaymentType> pts = getSelectedOrganization()
                    .getPaymentTypes();
            for (VOPaymentType pt : pts) {
                s += pt.getKey() + ",";
            }
            pspAccountPaymentTypesAsString = s;
        }
        return pspAccountPaymentTypesAsString;
    }

    public void setPSPAccountPaymentTypesAsString(String value) {
        pspAccountPaymentTypesAsString = value;
    }

    public VOPaymentType getSelectedPaymentType() {
        return selectedPaymentType;
    }

    /**
     * Adds a new {@link VOPSPSetting} to the list.
     * 
     * @return the modified list
     */
    public final List<PSPSettingRow> addPSPSettingRow() {
        PSPSettingRow pspSettingRow = new PSPSettingRow(new VOPSPSetting());
        pspSettingRow.setNewDefinition(true);
        pspSettingRowList.add(0, pspSettingRow);
        return pspSettingRowList;
    }

    public final List<PSPSettingRow> getPSPSettings() {
        return pspSettingRowList;
    }

    public String getJSForPaymentTypeSelection() {
        final StringBuilder b = new StringBuilder("");
        final List<VOPSP> psps = getPSPs();
        String s;
        for (VOPSP psp : psps) {
            b.append("paymentType['" + psp.getKey() + "'] = new Object();\n");
            s = "paymentType['" + psp.getKey() + "']['";
            for (VOPaymentType pt : psp.getPaymentTypes()) {
                b.append(s + pt.getKey() + "'] = '" + pt.getName() + "';\n");
            }
        }
        return b.toString();
    }

    public boolean isCustomerOrganization() {
        if (isNewSupplier()) {
            return false;
        }

        if (isNewTechnologyProvider()) {
            return false;
        }

        if (isNewReseller()) {
            return false;
        }

        if (isNewBroker()) {
            return false;
        }

        return true;
    }

    @Override
    protected void resetUIComponents(Set<String> componentIds) {
        super.resetUIComponents(componentIds);
    }

    @Override
    protected void resetUIInputChildren() {
        super.resetUIInputChildren();
    }

    /**
     * @param selectedMarketplace
     *            the selectedMarketplace to set
     */
    public void setSelectedMarketplace(String selectedMarketplace) {
        this.selectedMarketplace = selectedMarketplace;
    }

    /**
     * @return the selectedMarketplace
     */
    public String getSelectedMarketplace() {
        return selectedMarketplace;
    }

    public UploadedFile getOrganizationProperties() {
        return organizationProperties;
    }

    public void setOrganizationProperties(UploadedFile organizationProperties) {
        this.organizationProperties = organizationProperties;
    }

    public boolean isInternalAuthMode() {
        internalAuthMode = getApplicationBean().isInternalAuthMode();
        return internalAuthMode;
    }
    
    public boolean isLdapManaged() {
        return ldapManaged;
    }

    public void setLdapManaged(boolean ldapManaged) {
        this.ldapManaged = ldapManaged;
    }

    ApplicationBean getApplicationBean() {
        if (appBean == null) {
            appBean = ui.findBean(APPLICATION_BEAN);
        }
        return appBean;
    }

    public boolean isLdapSettingVisible() {
        ldapSettingVisible = getApplicationBean().isInternalAuthMode();
        return ldapSettingVisible;
    }

    public void setLdapSettingVisible(boolean ldapSettingVisible) {
        this.ldapSettingVisible = ldapSettingVisible;
    }

    public String getSelectedTenant() {
        return selectedTenant;
    }

    public void setSelectedTenant(String selectedTenant) {
        this.selectedTenant = selectedTenant;
    }

    public void processValueChange(ValueChangeEvent e) {

        if (e.getNewValue() == null) {
            setSelectedTenant(null);
            return;
        }

        setSelectedTenant(e.getNewValue().toString());
        selectedMarketplace = null;
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    public boolean isTenantSelectionAvailable() {
        return menuBean.getApplicationBean().isSSOAuthMode();
    }
    
}
