/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Author: walker
 *
 * <p>Creation Date: 31.01.2011
 *
 * <p>Completion Time: <date>
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.beans.operator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.oscm.converter.PropertiesLoader;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.tenant.ManageTenantService;
import org.oscm.internal.tenant.POTenant;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.LdapProperties;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.ImageUploader;
import org.oscm.ui.common.JSFUtils;

/**
 * This class is responsible to provide the functionality to create and manage organization to the
 * operator user.
 */
@ViewScoped
@ManagedBean(name = "operatorOrgBean")
public class OperatorOrgBean extends BaseOperatorBean implements Serializable {

  private static final long serialVersionUID = -295463152427849927L;
  public static final String APPLICATION_BEAN = "appBean";

  private static final Log4jLogger logger = LoggerFactory.getLogger(OperatorOrgBean.class);

  /**
   * Contains the IDs of the HTML fields corresponding to the attributes of an Organization which
   * are only mandatory for a supplier or a technology provider, but not for a customer.
   */
  private static final Set<String> CONDITIONAL_MANDATORY_FIELD_IDS =
      new HashSet<String>(
          Arrays.asList(
              "requiredEmail", "requiredPhone", "requiredUrl", "requiredName", "requiredAddress"));

  @ManagedProperty(value = "#{operatorSelectOrgCtrl}")
  private OperatorSelectOrgCtrl operatorSelectOrgCtrl;

  // The new organization and administrator user
  private VOOrganization newOrganization = null;
  private VOUserDetails newAdministrator = null;

  // Contains _all_ roles of the new organization
  private EnumSet<OrganizationRoleType> newRoles = EnumSet.noneOf(OrganizationRoleType.class);

  private List<SelectItem> selectableMarketplaces = new ArrayList<>();
  private String selectedMarketplace;
  private String selectedTenant;

  private ImageUploader imageUploader = new ImageUploader(ImageType.ORGANIZATION_IMAGE);

  private UploadedFile organizationProperties;
  private boolean ldapManaged;
  private boolean ldapSettingVisible;
  private boolean internalAuthMode;

  transient ApplicationBean appBean;
  private Long selectedPaymentTypeKey;

  @EJB ManageTenantService manageTenantService;

  @ManagedProperty(value = "#{menuBean}")
  MenuBean menuBean;

  @ManagedProperty(value = "#{sessionBean}")
  SessionBean sessionBean;



  /**
   * Registers the newly created organization.
   *
   * @return <code>OUTCOME_SUCCESS</code> if the organization was successfully registered.
   * @throws ImageException Thrown in case the access to the uploaded file failed.
   */
  public String createOrganization() throws SaaSApplicationException {

    VOOrganization newVoOrganization = null;

    OrganizationRoleType[] selectedRoles =
        newRoles.toArray(new OrganizationRoleType[newRoles.size()]);
    LdapProperties ldapProperties = null;
    if (ldapManaged && organizationProperties != null) {
      try {
        Properties props = PropertiesLoader.loadProperties(organizationProperties.getInputStream());
        ldapProperties = new LdapProperties(props);
      } catch (IOException e) {
        logger.logError(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_CREATE_ORGANIZATION);
        addMessage(null, FacesMessage.SEVERITY_ERROR, ERROR_UPLOAD);
        return OUTCOME_ERROR;
      }
    }
    if (!newRoles.contains(OrganizationRoleType.SUPPLIER)
        && null != newOrganization.getOperatorRevenueShare()) {
      newOrganization.setOperatorRevenueShare(null);
    }
    if (StringUtils.isNotBlank(getSelectedTenant())) {
      try {
        Long tenantKey = Long.valueOf(getSelectedTenant());
        newOrganization.setTenantKey(tenantKey.longValue());
        String selectedTenantId = determineIdForTenant(tenantKey);
        manageTenantService.validateOrgNameUniquenessInTenant(
            newOrganization.getName(), selectedTenantId);
        newAdministrator =
            getIdService()
                .loadUserDetailsFromOIDCProvider(newAdministrator.getUserId(), selectedTenantId);
        String groupId =
            getIdService()
                .createAccessGroupInOIDCProvider(selectedTenantId, newOrganization.getName());
        getIdService()
            .addMemberToAccessGroupInOIDCProvider(groupId, selectedTenantId, newAdministrator);
        newOrganization.setOidcGroupId(groupId);
      } catch (RegistrationException ex) {
        String message = JSFUtils.getText(ex.getMessageKey(), ex.getMessageParams());
        RegistrationException re = new RegistrationException();
        re.setMessageKey(ERROR_CREATE_ORGANISATION_FAILURE);
        re.setMessageParams(new String[] {message});
        throw re;
      }
    }
    newVoOrganization =
        getOperatorService()
            .registerOrganization(
                newOrganization,
                getImageUploader().getVOImageResource(),
                newAdministrator,
                ldapProperties,
                selectedMarketplace,
                selectedRoles);

    getRequest().getSession().setAttribute("organizationId", newOrganization.getOrganizationId());

    addMessage(
        null,
        FacesMessage.SEVERITY_INFO,
        INFO_ORGANIZATION_CREATED,
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
  

  /**
   * This functions persists the changed data of the currently selected organization.
   *
   * @return <code>OUTCOME_SUCCESS</code> if the organization was successfully updated.
   * @throws SaaSApplicationException if any problems occurs while persisting the values
   * @throws ImageException Thrown in case the access to the uploaded file failed.
   */
  public String saveOrganization() throws SaaSApplicationException {

    OperatorService operatorService = getOperatorService();
    VOOperatorOrganization org = getSelectedOrganization();

    long updatedTenantKey = org.getTenantKey();

    manageTenantService.validateOrgUsersUniqnessInTenant(org.getOrganizationId(), updatedTenantKey);

    VOOperatorOrganization updated =
        operatorService.updateOrganization(org, getImageUploader().getVOImageResource());

    operatorSelectOrgCtrl.setExistingOrganization(updated);
    operatorSelectOrgCtrl.setOrganization(updated);
    
  
    addMessage(
        null, FacesMessage.SEVERITY_INFO, INFO_ORGANIZATION_SAVED, updated.getOrganizationId());

    

    return OUTCOME_SUCCESS;
  }

  private String determineIdForTenant(Long tenantKey) {
    for (POTenant t : manageTenantService.getAllTenantsWithDefaultTenant()) {
      if (t.getKey() == tenantKey.longValue()) {
        return t.getTenantId();
      }
    }
    return null;
  }

  // *****************************************************
  // *** Getter and setter for _marketplaces ***
  // *****************************************************

  /** @return all marketplaces which are owned and to which the supplier can publish */
  public List<SelectItem> getSelectableMarketplaces() {
    if (!isLoggedInAndPlatformOperator()) {
      return selectableMarketplaces;
    }
    List<VOMarketplace> marketplaces;
    Long tenantKey;
    try {
      tenantKey = Long.valueOf(this.selectedTenant);
    } catch (Exception exc) {
      // Selected tenant is not parseable, results will be displayed for default.
      tenantKey = null;
    }

    try {
      marketplaces = getMarketplaceService().getAllMarketplacesForTenant(tenantKey);
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

  /** For Unit Test only */
  @Override
  protected boolean isLoggedInAndPlatformOperator() {
    return super.isLoggedInAndPlatformOperator();
  }

  /** For Unit Test only */
  @Override
  protected MarketplaceService getMarketplaceService() {
    return super.getMarketplaceService();
  }

  /** For Unit Test only */
  @Override
  protected void addMessage(
      final String clientId,
      final FacesMessage.Severity severity,
      final String key,
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

  public OperatorSelectOrgCtrl getOperatorSelectOrgCtrl() {
    return operatorSelectOrgCtrl;
  }

  public void setOperatorSelectOrgCtrl(OperatorSelectOrgCtrl operatorSelectOrgCtrl) {
    this.operatorSelectOrgCtrl = operatorSelectOrgCtrl;
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
   * Returns the locally selected organization. On page load or if the selected organization in the
   * corresponding operatorSelectOrgBean have changed this object will be in sync with the
   * organization of the operatorSelectOrgBean.
   */
  public VOOperatorOrganization getSelectedOrganization() {
    return operatorSelectOrgCtrl.getOrganization();
  }

  @Override
  protected OperatorService getOperatorService() {
    return super.getOperatorService();
  }

  // ********************************************************************
  // Methods for handling changes of the organization roles
  // of the selected organization
  // ********************************************************************

  /**
   * Returns whether the organization has the supplier or the technology provider role both locally
   * and persisted in DB.
   *
   * @return <code>true</code> if the organization has whether the supplier or the technology
   *     provider role both locally and persisted in DB, or <code>false</code> otherwise.
   */
  public boolean isVendorDisabled() {
    return isSupplierDisabled()
        || isTechnologyProviderDisabled()
        || isResellerDisabled()
        || isBrokerDisabled();
  }

  /**
   * Returns whether the organization has the supplier or the technology provider role locally set.
   *
   * @return <code>true</code> if the organization has supplier or the technology provider role, or
   *     <code>false</code> otherwise.
   */
  public boolean isVendor() {
    return isSupplier() || isTechnologyProvider() || isReseller() || isBroker();
  }

  /**
   * Reflect the state of the local organization object, which might be changed be the user but was
   * not committed yet.
   *
   * @return true is the local local organization object has the corresponding role
   */
  public boolean isTechnologyProvider() {
    return isRoleAvailable(getSelectedOrganization(), OrganizationRoleType.TECHNOLOGY_PROVIDER);
  }

  /** Sets or removes the corresponding role from the local organization object. */
  public void setTechnologyProvider(boolean setRole) {
    if (setRole && !this.isTechnologyProvider()) {
      addOrgRole(OrganizationRoleType.TECHNOLOGY_PROVIDER);
    } else if (!setRole) {
      removeOrgRole(OrganizationRoleType.TECHNOLOGY_PROVIDER);
    }
  }

  /*
   * value change listener for supplier role check-box
   */
  public void supplierRoleChanged(ValueChangeEvent event) {
    Boolean checkBoxChecked = (Boolean) event.getNewValue();
    setSupplier(((checkBoxChecked.booleanValue())));
  }
  
  /** Sets or removes the corresponding role from the local organization object. */
  public void setReseller(boolean setRole) {
    if (setRole && !this.isReseller()) {
      addOrgRole(OrganizationRoleType.RESELLER);
    } else if (!setRole) {
      removeOrgRole(OrganizationRoleType.RESELLER);
    }
  }

  /** Sets or removes the corresponding role from the local organization object. */
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
    return isTechnologyProvider() && isPersistedRole(OrganizationRoleType.TECHNOLOGY_PROVIDER);
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

  /** value change listener for technology provider role check-box */
  public void technologyProviderRoleChanged(ValueChangeEvent event) {
    Boolean checkBoxChecked = (Boolean) event.getNewValue();
    setTechnologyProvider(checkBoxChecked.booleanValue());
  }

  /** value change listener for technology provider role check-box */
  public void resellerRoleChanged(ValueChangeEvent event) {
    Boolean checkBoxChecked = (Boolean) event.getNewValue();
    setReseller(checkBoxChecked.booleanValue());
  }

  /** value change listener for technology provider role check-box */
  public void brokerRoleChanged(ValueChangeEvent event) {
    Boolean checkBoxChecked = (Boolean) event.getNewValue();
    setBroker(checkBoxChecked.booleanValue());
  }

  /**
   * Reflect the state of the local organization object, which might be changed be the user but was
   * not committed yet.
   *
   * @return true is the local organization object has the corresponding role
   */
  public boolean isSupplier() {
    return isRoleAvailable(getSelectedOrganization(), OrganizationRoleType.SUPPLIER);
  }

  /**
   * Reflect the state of the local organization object, which might be changed be the user but was
   * not committed yet.
   *
   * @return true is the local organization object has the corresponding role
   */
  public boolean isBroker() {
    return isRoleAvailable(getSelectedOrganization(), OrganizationRoleType.BROKER);
  }

  /**
   * Reflect the state of the local organization object, which might be changed be the user but was
   * not committed yet.
   *
   * @return true is the local organization object has the corresponding role
   */
  public boolean isReseller() {
    return isRoleAvailable(getSelectedOrganization(), OrganizationRoleType.RESELLER);
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

  /** Sets of removed the corresponding role from the local organization object. */
  public void setSupplier(boolean setRole) {
    if (setRole && !this.isSupplier()) {
      addOrgRole(OrganizationRoleType.SUPPLIER);
    } else if (!setRole) {
      removeOrgRole(OrganizationRoleType.SUPPLIER);
    }
  }

  /**
   * @return true if the currently selected and persistent organization has already the supplier
   *     role, false otherwise
   */
  public boolean isSupplierOrResellerPersisted() {
    return isPersistedRole(OrganizationRoleType.SUPPLIER, OrganizationRoleType.RESELLER);
  }

  /**
   * Reflects the state of the role in relation to persisted object.
   *
   * @return true if the role is set locally and in the DB object.
   */
  public boolean isSupplierDisabled() {
    return isSupplier() && isPersistedRole(OrganizationRoleType.SUPPLIER);
  }

  /** Returns true if the passed roletype is available for the passed organization. */
  private boolean isRoleAvailable(VOOperatorOrganization voorg, OrganizationRoleType... roles) {
    if (operatorSelectOrgCtrl.getOrganization() == null) {
      return false;
    }
    List<OrganizationRoleType> orgRoles = voorg.getOrganizationRoles();
    for (OrganizationRoleType role : roles) {
      if (orgRoles.contains(role)) return true;
    }
    return false;
  }

  /** Returns true if the passed role is set on the organization of the operatorSelectOrgBean. */
  private boolean isPersistedRole(OrganizationRoleType... role) {
    return isRoleAvailable(operatorSelectOrgCtrl.getExistingOrganization(), role);
  }

  /** Adds the passed role type to the local organization. */
  private void addOrgRole(OrganizationRoleType type) {
    List<OrganizationRoleType> orgRoles = getSelectedOrganization().getOrganizationRoles();
    if (!orgRoles.contains(type)) {
      orgRoles.add(type);
    }
  }

  /** Removes the passed roletype to the local organization. */
  private void removeOrgRole(OrganizationRoleType type) {
    List<OrganizationRoleType> orgRoles = getSelectedOrganization().getOrganizationRoles();
    orgRoles.remove(type);
  }

  // ********************************************************************
  // Methods for handling changes of the payment type
  // of the selected organization
  // ********************************************************************

  public ImageUploader getImageUploader() {
    return imageUploader;
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

  /** @param selectedMarketplace the selectedMarketplace to set */
  public void setSelectedMarketplace(String selectedMarketplace) {
    this.selectedMarketplace = selectedMarketplace;
  }

  /** @return the selectedMarketplace */
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

  public SessionBean getSessionBean() {
    return sessionBean;
  }

  public void setSessionBean(SessionBean sessionBean) {
    this.sessionBean = sessionBean;
  }
}
