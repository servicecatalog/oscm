/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 26.03.2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.beans.operator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.convert.Converter;

import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPAccount;
import org.oscm.internal.vo.VOPSPSetting;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.ui.converter.PSPConverter;
import org.oscm.ui.model.PSPSettingRow;

/**
 * Controller for managing payment service provider.
 *
 * @author goebel
 */
@ManagedBean(name = "managePSPCtrl")
@ViewScoped
public class ManagePSPCtrl extends BaseOperatorBean implements Serializable {

  private static final long serialVersionUID = -3483098936961111754L;

  @ManagedProperty(value = "#{managePSPModel}")
  private ManagePSPModel model;

  @ManagedProperty(value = "#{operatorSelectOrgCtrl}")
  private OperatorSelectOrgCtrl operatorSelectOrgCtrl;

  @PostConstruct
  public void init() {
    if (!model.isInitialized()) {
      operatorSelectOrgCtrl.init();
      ManagePSPModel m = new ManagePSPModel();
      initializeModel(m, operatorSelectOrgCtrl.getModel().getExistingOrganization());
      model.setInitialized(true);
    }
  }

  private void initializeModel(ManagePSPModel m, VOOperatorOrganization org) {
    model = m;
    initPSPs();
    initPSPAccounts(org);
  }

  void initPSPs() {
    final List<VOPSP> psps = getOperatorService().getPSPs();
    model.setPSPs(psps);
    for (VOPSP p : psps) {
      setSelectedPSP(p);
      break;
    }
    model.setNewPSP(new VOPSP());
  }

  void initPSPAccounts(VOOperatorOrganization org) {
    model.setSelectedPspAccountKey(Long.valueOf(0L));
    try {
      List<VOPSPAccount> ac = getOperatorService().getPSPAccounts(org);
      model.setPSPAccounts(ac);
      for (VOPSPAccount a : ac) {
        model.setSelectedPspAccountKey(Long.valueOf(a.getKey()));
        break;
      }
    } catch (ObjectNotFoundException e) {
    }
    initPspAccountPaymentTypes(org);
  }

  void initPspAccountPaymentTypes(VOOperatorOrganization org) {
    String s = ",";
    final List<VOPaymentType> pts = org.getPaymentTypes();
    for (VOPaymentType pt : pts) {
      s += pt.getKey() + ",";
    }
    model.setPspAccountPaymentTypesAsString(s);
  }

  public void prepareDataForNewPaymentType() {
    model.setSelectedPaymentType(new VOPaymentType());
    model
        .getSelectedPaymentType()
        .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
  }

  public void prepareDataForEditPaymentType() {
    for (VOPaymentType voPaymentType : model.getSelectedPSP().getPaymentTypes()) {
      if (new Long(voPaymentType.getKey()).equals(model.getSelectedPaymentTypeKey())) {
        model.setSelectedPaymentType(voPaymentType);
        return;
      }
    }
  }

  /**
   * This functions persists the changed data of the currently selected organization.
   *
   * @throws SaaSApplicationException if any problems occurs while persisting the values
   * @throws ImageException Thrown in case the access to the uploaded file failed.
   */
  public void savePSP() throws SaaSApplicationException {
    if (!isTokenValid() && model.getPspId() != null) {
      updateSelectedPSP();
    }

    final List<VOPSPSetting> list = new ArrayList<>();
    for (PSPSettingRow row : model.getPspSettingRowList()) {
      if (!row.isSelected()
          && (row.getDefinition().getSettingKey() != null
                  && row.getDefinition().getSettingKey().trim().length() > 0
              || row.getDefinition().getSettingValue() != null
                  && row.getDefinition().getSettingValue().trim().length() > 0)) {
        list.add(row.getDefinition());
      }
    }
    final VOPSP psp = model.getSelectedPSP();
    psp.setPspSettings(list);

    OperatorService operatorService = getOperatorService();
    model.setSelectedPSP(operatorService.savePSP(psp));

    resetToken();
    addMessage(null, FacesMessage.SEVERITY_INFO, INFO_PSP_SAVED, model.getSelectedPSP().getId());

    // reload
    model.setInitialized(false);
    init();
  }

  /**
   * Registers payment types for PSP.
   *
   * @return <code>OUTCOME_SUCCESS</code> if the payment type was successfully registered, otherwise
   *     <code>OUTCOME_ERROR</code>.
   */
  public void savePaymentType() throws SaaSApplicationException {
    getOperatorService().savePaymentType(model.getSelectedPSP(), model.getSelectedPaymentType());

    resetToken();
    addMessage(null, FacesMessage.SEVERITY_INFO, INFO_PAYMENT_TYPE_SAVED);

    // reload
    model.setInitialized(false);
    init();
  }

  /**
   * Registers payment types for an organization.
   *
   * @return <code>OUTCOME_SUCCESS</code> if the payment type for organization was successfully
   *     registered, otherwise <code>OUTCOME_ERROR</code> .
   */
  public String savePaymentTypeForOrganization() throws SaaSApplicationException {
    VOOperatorOrganization org = operatorSelectOrgCtrl.getModel().getExistingOrganization();

    final List<VOPSP> psps = model.getPSPs();
    final Map<String, String> ptMap = new HashMap<String, String>();
    for (VOPSP psp : psps) {
      for (VOPaymentType pt : psp.getPaymentTypes()) {
        ptMap.put("" + pt.getKey(), pt.getPaymentTypeId());
      }
    }

    final String[] pts = model.getPspAccountPaymentTypesAsString().split(",");
    Set<String> ptsSet = new HashSet<String>();
    for (String s : pts) {
      if (s.trim().length() > 0) {
        if (ptMap.containsKey(s)) {
          ptsSet.add(ptMap.get(s));
        } else {
          throw new SaaSSystemException("payment type for key " + s + " not found!");
        }
      }
    }
    getOperatorService().savePSPAccount(org, getSelectedPspAccount());
    getOperatorService().addAvailablePaymentTypes(org, ptsSet);

    addMessage(null, FacesMessage.SEVERITY_INFO, INFO_PAYMENT_INFO_SAVED);

    model.setNewPspAccount(null);
    model.setInitialized(false);
    init();

    return OUTCOME_SUCCESS;
  }

  /**
   * Registers the newly created PSP.
   *
   * @return <code>OUTCOME_SUCCESS</code> if the organization was successfully registered, otherwise
   *     <code>OUTCOME_ERROR</code>.
   */
  public String createPSP() throws SaaSApplicationException {

    VOPSP newVoPSP = getOperatorService().savePSP(model.getNewPSP());

    addMessage(null, FacesMessage.SEVERITY_INFO, INFO_PSP_CREATED, newVoPSP.getId());

    // Reset the form
    model.setNewPSP(new VOPSP());

    return OUTCOME_SUCCESS;
  }

  /** Indicates if the corresponding payment type is available for the local organization. */
  public boolean isCreditCardAvailable() {
    return isPaymentTypeAvailable(
        operatorSelectOrgCtrl.getOrganization(), PaymentInfoType.CREDIT_CARD);
  }

  /** Adds the passed payment type to the local organization object. */
  private void addVoPayment(PaymentInfoType type) {
    VOPaymentType voPaymentType = new VOPaymentType();
    voPaymentType.setPaymentTypeId(type.name());
    operatorSelectOrgCtrl.getOrganization().getPaymentTypes().add(voPaymentType);
  }

  /** Removes the passed payment type to the local organization object. */
  private void removeVoPayment(PaymentInfoType type) {
    List<VOPaymentType> voPaymentTypes = operatorSelectOrgCtrl.getOrganization().getPaymentTypes();
    for (VOPaymentType voPaymentType : voPaymentTypes) {
      if (voPaymentType.getPaymentTypeId().equals(type.name())) {
        voPaymentTypes.remove(voPaymentType);
        return;
      }
    }
  }

  /**
   * Returns true if the passed payment type is available in the object which is in sync with the
   * DB.
   */
  private boolean isPersistedType(PaymentInfoType type) {
    return isPaymentTypeAvailable(operatorSelectOrgCtrl.getExistingOrganization(), type);
  }

  /**
   * Reflects the state of the payment type in relation to persisted object.
   *
   * @return true if the payment type is set locally and in the DB object.
   */
  public boolean isCreditCardDisabled() {
    return (isPersistedType(PaymentInfoType.CREDIT_CARD) && isCreditCardAvailable()) ? true : false;
  }

  /** Sets or removes the corresponding payment type locally. */
  public void setCreditCardAvailable(boolean setpaymentType) {
    if (setpaymentType && !this.isCreditCardAvailable()) {
      addVoPayment(PaymentInfoType.CREDIT_CARD);
    } else if (!setpaymentType) {
      removeVoPayment(PaymentInfoType.CREDIT_CARD);
    }
  }

  /** Indicates if the corresponding payment type is available for the local organization. */
  public boolean isInvoiceAvailable() {
    return isPaymentTypeAvailable(operatorSelectOrgCtrl.getOrganization(), PaymentInfoType.INVOICE);
  }

  /**
   * Reflects the state of the payment type in relation to persisted object.
   *
   * @return true if the payment type is set locally and in the DB object.
   */
  public boolean isInvoiceDisabled() {
    return (isPersistedType(PaymentInfoType.INVOICE) && isInvoiceAvailable()) ? true : false;
  }

  /** Sets or removes the corresponding payment type locally. */
  public void setInvoiceAvailable(boolean setpaymentType) {
    if (setpaymentType && !this.isInvoiceAvailable()) {
      addVoPayment(PaymentInfoType.INVOICE);
    } else if (!setpaymentType) {
      removeVoPayment(PaymentInfoType.INVOICE);
    }
  }

  /** Indicates if the corresponding payment type is available for the local organization. */
  public boolean isDirectDebitAvailable() {
    return isPaymentTypeAvailable(
        operatorSelectOrgCtrl.getOrganization(), PaymentInfoType.DIRECT_DEBIT);
  }

  /**
   * Reflects the state of the payment type in relation to persisted object.
   *
   * @return true if the payment type is set locally and in the DB object.
   */
  public boolean isDirectDebitDisabled() {
    return (isPersistedType(PaymentInfoType.DIRECT_DEBIT) && isDirectDebitAvailable());
  }

  /** Sets or removes the corresponding payment type locally. */
  public void setDirectDebitAvailable(boolean setpaymentType) {
    if (setpaymentType && !this.isDirectDebitAvailable()) {
      addVoPayment(PaymentInfoType.DIRECT_DEBIT);
    } else if (!setpaymentType) {
      removeVoPayment(PaymentInfoType.DIRECT_DEBIT);
    }
  }

  /** Returns true if the passed payment type is available for the passed organization. */
  private boolean isPaymentTypeAvailable(VOOperatorOrganization voOrg, PaymentInfoType type) {

    List<VOPaymentType> paymentTypes = voOrg.getPaymentTypes();
    for (VOPaymentType voPaymentType : paymentTypes) {
      if (voPaymentType.getPaymentTypeId().equals(type.name())) {
        return true;
      }
    }
    return false;
  }

  public void setSelectedPSP(VOPSP selectedPSP) {
    model.setSelectedPSP(selectedPSP);
    model.getPspSettingRowList().clear();
    if (selectedPSP != null) {
      for (VOPSPSetting setting : selectedPSP.getPspSettings()) {
        model.getPspSettingRowList().add(new PSPSettingRow(setting));
      }
    }
  }

  public VOPSP getSelectedPSP() {
    return model.getSelectedPSP();
  }

  private void updateSelectedPSP() {
    final List<VOPSP> psps = getOperatorService().getPSPs();
    model.setSelectedPSP(null);
    model.getPspSettingRowList().clear();
    for (VOPSP psp : psps) {
      if (model.getPspId().equals("" + psp.getKey())) {
        model.setSelectedPSP(psp);

        for (VOPSPSetting setting : psp.getPspSettings()) {
          model.getPspSettingRowList().add(new PSPSettingRow(setting));
        }
        break;
      }
    }
  }

  public void setSelectedPspAccountKey(Long pspAccountKey) {
    model.setSelectedPspAccountKey(pspAccountKey);
  }

  public void setPSPAccountPSPKey(final Long key) {
    if (key != null) {
      getSelectedPspAccount().getPsp().setKey(key.longValue());
    }
  }

  public Long getPSPAccountPSPKey() {
    return Long.valueOf(getSelectedPspAccount().getPsp().getKey());
  }

  public VOPSPAccount getSelectedPspAccount() {
    if (model.getSelectedPspAccountKey().longValue() != 0 && model.getPSPAccounts() != null) {
      for (VOPSPAccount acc : model.getPSPAccounts()) {
        if (model.getSelectedPspAccountKey().equals(new Long(acc.getKey()))) {
          return acc;
        }
      }
    }
    if (model.getNewPspAccount() == null) {
      model.setNewPspAccount(new VOPSPAccount());
      model.getNewPspAccount().setPsp(new VOPSP());
    }
    return model.getNewPspAccount();
  }

  public Converter getPSPConverter() {
    init();
    PSPConverter pspc = new PSPConverter();
    pspc.setModel(model);
    return pspc;
  }

  /**
   * Adds a new {@link VOPSPSetting} to the list.
   *
   * @return the modified list
   */
  public final List<PSPSettingRow> addPSPSettingRow() {
    PSPSettingRow pspSettingRow = new PSPSettingRow(new VOPSPSetting());
    pspSettingRow.setNewDefinition(true);
    model.getPspSettingRowList().add(0, pspSettingRow);
    return model.getPspSettingRowList();
  }

  public String getJSForPaymentTypeSelection() {
    final StringBuilder b = new StringBuilder("");
    final List<VOPSP> psps = model.getPSPs();
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

  public ManagePSPModel getModel() {
    return model;
  }

  public void setModel(ManagePSPModel model) {
    this.model = model;
  }

  public OperatorSelectOrgCtrl getOperatorSelectOrgCtrl() {
    return operatorSelectOrgCtrl;
  }

  public void setOperatorSelectOrgCtrl(OperatorSelectOrgCtrl operatorSelectOrgCtrl) {
    this.operatorSelectOrgCtrl = operatorSelectOrgCtrl;
  }
}
