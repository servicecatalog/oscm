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

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPAccount;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.ui.model.PSPSettingRow;

/**
 * This is the model implementation for the manage payment service provider views.
 *
 * @author goebel
 */
@ManagedBean(name = "managePSPModel")
@ViewScoped
public class ManagePSPModel {

  private List<VOPSPAccount> selectedPSPAccounts = null;
  private VOPSP selectedPSP = null;
  private Long selectedPspAccountKey = Long.valueOf(0L);

  private List<PSPSettingRow> pspSettingRowList = new ArrayList<PSPSettingRow>();
  private String pspAccountPaymentTypesAsString = "";
  private VOPSPAccount newPspAccount = null;

  private VOPaymentType selectedPaymentType;
  private Long selectedPaymentTypeKey = null;
  private VOPSP newPSP = null;

  private String pspId = "";
  private List<VOPSPAccount> pspAccounts;
  private boolean isInitialized;
  private List<VOPSP> psps;

  public Long getSelectedPspAccountKey() {
    return selectedPspAccountKey;
  }

  public void setSelectedPspAccountKey(Long selectedPspAccountKey) {
    this.selectedPspAccountKey = selectedPspAccountKey;
  }

  public String getPspAccountPaymentTypesAsString() {
    return pspAccountPaymentTypesAsString;
  }

  public VOPSPAccount getNewPspAccount() {
    return newPspAccount;
  }

  public void setNewPspAccount(VOPSPAccount newPspAccount) {
    this.newPspAccount = newPspAccount;
  }

  public String getPspId() {
    return pspId;
  }

  public void setPspId(String pspId) {
    this.pspId = pspId;
  }

  public VOPSP getNewPSP() {
    return newPSP;
  }

  public void setNewPSP(VOPSP newPSP) {
    this.newPSP = newPSP;
  }

  public List<PSPSettingRow> getPspSettingRowList() {
    return pspSettingRowList;
  }

  public void setPspSettingRowList(List<PSPSettingRow> list) {
    pspSettingRowList = list;
  }

  public VOPaymentType getSelectedPaymentType() {
    return selectedPaymentType;
  }

  public void setSelectedPaymentType(VOPaymentType voPaymentType) {
    selectedPaymentType = voPaymentType;
  }

  public VOPSP getSelectedPSP() {
    return selectedPSP;
  }

  public void setSelectedPSP(VOPSP psp) {
    selectedPSP = psp;
  }

  public void setPspAccountPaymentTypesAsString(String value) {
    pspAccountPaymentTypesAsString = value;
  }

  public Long getSelectedPaymentTypeKey() {
    return selectedPaymentTypeKey;
  }

  public void setSelectedPaymentTypeKey(Long selPtk) {
    selectedPaymentTypeKey = selPtk;
  }

  public void setPSPAccounts(List<VOPSPAccount> pspAccounts) {
    this.pspAccounts = pspAccounts;
  }

  public List<VOPSPAccount> getPSPAccounts() {
    return pspAccounts;
  }

  public void setPSPs(List<VOPSP> psPs) {

    this.psps = psPs;
  }

  public List<VOPSP> getPSPs() {
    return psps;
  }

  public boolean isInitialized() {
    return isInitialized;
  }

  public void setInitialized(boolean b) {
    isInitialized = b;
  }
}
