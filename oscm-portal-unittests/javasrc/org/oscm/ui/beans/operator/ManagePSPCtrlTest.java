/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 01.04.2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.beans.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPSPSetting;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.ui.converter.PSPConverter;
import org.oscm.ui.model.PSPSettingRow;
import org.oscm.ui.stubs.FacesContextStub;

/**
 * Unit test for Manage PSP Controller.
 *
 * @author goebel
 */
public class ManagePSPCtrlTest {

  private ManagePSPCtrl ctrl;
  private VOOperatorOrganization org;
  private OperatorSelectOrgCtrl orgSelector;

  private final String orgId = "organizationId";
  private List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();

  @SuppressWarnings("serial")
  @Before
  public void setup() throws Exception {

    new FacesContextStub(Locale.ENGLISH) {
      @Override
      public void addMessage(String arg0, FacesMessage arg1) {
        facesMessages.add(arg1);
      }
    };

    OperatorService os = mock(OperatorService.class);

    ctrl =
        new ManagePSPCtrl() {
          protected OperatorService getOperatorService() {
            return os;
          }
        };
    ctrl.setModel(new ManagePSPModel());

    OperatorSelectOrgModel m = new OperatorSelectOrgModel();
    org = new VOOperatorOrganization();
    org.setOrganizationId(orgId);
    m.setExistingOrganization(org);
    m.setOrganization(org);
    org.setPaymentTypes(new ArrayList<VOPaymentType>());

    orgSelector = mock(OperatorSelectOrgCtrl.class);

    doReturn(m).when(orgSelector).getModel();
    doReturn(org).when(orgSelector).getExistingOrganization();
    doReturn(org).when(orgSelector).getOrganization();
    ctrl.setOperatorSelectOrgCtrl(orgSelector);

    HttpSession sm = mock(HttpSession.class);
    HttpServletRequest rm = mock(HttpServletRequest.class);

    mockPSPs(os);
  }

  @Test
  public void init() {
    // given
    List<VOPSP> psps = ctrl.getOperatorService().getPSPs();

    // when
    ctrl.init();

    // then
    assertSame(psps.get(0), ctrl.getSelectedPSP());
  }

  @Test
  public void getPSPConverter() {

    // when
    PSPConverter pc = (PSPConverter) ctrl.getPSPConverter();

    // then
    assertSame(ctrl.getModel(), pc.getModel());
  }

  @Test
  public void getPspSettingRowList() {

    // when
    ctrl.addPSPSettingRow();
    List<PSPSettingRow> l = ctrl.getModel().getPspSettingRowList();

    // then
    assertEquals(1, l.size());
    assertTrue(l.get(0).isNewDefinition());
  }

  @Test
  public void createPSP() throws Exception {
    // given
    givenPSP("PSP-ID");

    // when
    ctrl.getModel().setNewPSP(null);
    ctrl.createPSP();

    // then
    assertNotNull(ctrl.getModel().getNewPSP());
    assertEquals(1, facesMessages.size());
    assertEquals(FacesMessage.SEVERITY_INFO, facesMessages.get(0).getSeverity());
  }

  @Test
  public void savePSP() throws Exception {
    // given
    givenPSPSettings();

    // when
    ctrl.setToken("token");
    ctrl.savePSP();

    // then
    assertEquals(1, facesMessages.size());
    assertEquals(FacesMessage.SEVERITY_INFO, facesMessages.get(0).getSeverity());
  }

  @Test
  public void savePSP_differentSettings() throws Exception {
    // given
    VOPSPSetting s = new VOPSPSetting();
    s.setSettingKey(null);
    s.setSettingValue("v1");
    givenRowDefintions(s);

    // when
    ctrl.setToken("token");
    ctrl.savePSP();

    // then
    assertEquals(1, facesMessages.size());
    assertEquals(FacesMessage.SEVERITY_INFO, facesMessages.get(0).getSeverity());
  }

  @Test
  public void savePaymentType() throws Exception {
    // given
    givenPSPSettings();

    // when
    ctrl.setToken("token");
    ctrl.savePaymentType();
  }

  @Test
  public void prepareForEdit() throws Exception {
    // given
    givenPSPSettings();
    long current = ctrl.getModel().getSelectedPaymentType().getKey();

    // when
    ctrl.getModel().setSelectedPaymentTypeKey(Long.valueOf(current));
    ctrl.prepareDataForEditPaymentType();
  }

  @Test
  public void prepareForEdit_otherSelection() throws Exception {
    // given
    givenPSPSettings();

    // when
    ctrl.getModel().setSelectedPaymentTypeKey(Long.valueOf(1));
    ctrl.prepareDataForEditPaymentType();
  }

  @Test
  public void prepareForEdit_noSelection() throws Exception {
    // given
    givenPSPSettings();

    // when
    ctrl.getModel().setSelectedPaymentTypeKey(null);
    ctrl.prepareDataForEditPaymentType();
  }

  @Test
  public void prepareForAdd() throws Exception {
    // given
    givenPSPSettings();
    VOPaymentType pt = ctrl.getModel().getSelectedPaymentType();

    // when
    ctrl.prepareDataForNewPaymentType();

    // then
    VOPaymentType ptNew = ctrl.getModel().getSelectedPaymentType();
    assertNotSame(pt, ptNew);
    assertEquals(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER, ptNew.getCollectionType());
  }

  @Test
  public void isPaymentTypeAvailable() {

    List<VOPaymentType> pts = givenOrgPaymentTypes();
    long nrOfDDs =
        pts.stream()
            .filter(pt -> pt.getPaymentTypeId().equals(PaymentInfoType.DIRECT_DEBIT.name()))
            .count();

    // When
    boolean rc = ctrl.isDirectDebitAvailable();

    assertTrue("DirectDebit available?", rc);
  }

  @Test
  public void initPspAccountPaymentTypes() {
    // given
    List<VOPaymentType> pts = givenOrgPaymentTypes();
    String pt1Key = String.valueOf(pts.get(0).getKey());
    String pt2Key = String.valueOf(pts.get(1).getKey());

    // when
    ctrl.getModel().setPspAccountPaymentTypesAsString("");
    ctrl.initPspAccountPaymentTypes(org);

    // then
    final String pStr = ctrl.getModel().getPspAccountPaymentTypesAsString();
    assertTrue(pStr, pStr.contains(pt1Key));
    assertTrue(pStr, pStr.contains(pt2Key));
  }

  @Test
  public void savePaymentTypeForOrganization() throws Exception {
    // given
    List<VOPaymentType> pts = givenOrgPaymentTypes();
    String pt1Key = String.valueOf(pts.get(0).getKey());
    String pt2Key = String.valueOf(pts.get(1).getKey());

    // when
    ctrl.getModel().setPSPs(ctrl.getOperatorService().getPSPs());
    ;

    ctrl.getModel().setPspAccountPaymentTypesAsString("");
    ctrl.savePaymentTypeForOrganization();

    // then
    final String pStr = ctrl.getModel().getPspAccountPaymentTypesAsString();
    assertTrue(pStr, pStr.contains(pt1Key));
    assertTrue(pStr, pStr.contains(pt2Key));
  }

  @Test
  public void getJSForPaymentTypeSelection() {
    // given
    ctrl.initPSPs();

    // when
    String pts = ctrl.getJSForPaymentTypeSelection();

    // then
    assertTrue(pts, pts.contains(PaymentInfoType.CREDIT_CARD.name()));
    assertTrue(pts, pts.contains(PaymentInfoType.DIRECT_DEBIT.name()));
  }

  @Test
  public void setCreditCardAvailable() {

    // given
    givenOrgPaymentTypes();
    int count = org.getPaymentTypes().size();
    assertTrue(ctrl.isCreditCardAvailable());

    // when
    ctrl.setCreditCardAvailable(false);

    // then
    assertFalse(ctrl.isCreditCardAvailable());
    assertEquals(count - 1, org.getPaymentTypes().size());

    // when
    ctrl.setCreditCardAvailable(true);

    // then
    assertTrue(ctrl.isCreditCardAvailable());
    assertEquals(count, org.getPaymentTypes().size());
  }

  @Test
  public void setInvoiceAvailable() {

    // given
    givenOrgPaymentTypes();
    int count = org.getPaymentTypes().size();
    assertFalse(ctrl.isInvoiceAvailable());

    // when
    ctrl.setInvoiceAvailable(true);

    // then
    assertTrue(ctrl.isInvoiceAvailable());
    assertEquals(count + 1, org.getPaymentTypes().size());
  }

  @Test
  public void isInvoiceDisabled() {
    givenPersistedOrgWithOnlyInvoice();

    // when
    ctrl.setCreditCardAvailable(true);
    ctrl.setDirectDebitAvailable(true);

    // then
    assertTrue("CC not in DB", ctrl.isCreditCardDisabled());
    assertTrue("DD not in DB", ctrl.isDirectDebitDisabled());
    assertFalse("Invoice enabled", ctrl.isInvoiceDisabled());
    
    assertTrue("CC selected", ctrl.isCreditCardAvailable());
    assertTrue("DD selected",  ctrl.isDirectDebitAvailable());
    assertFalse("Invoice not selected", ctrl.isInvoiceAvailable());
  }

  private List<VOPaymentType> givenOrgPaymentTypes() {
    VOPSP psp = ctrl.getOperatorService().getPSPs().get(0);
    org.setPaymentTypes(psp.getPaymentTypes());
    return org.getPaymentTypes();
  }

  private void givenPSPSettings()
      throws ConcurrentModificationException, ValidationException, NonUniqueBusinessKeyException,
          ObjectNotFoundException {
    VOPSP psp = ctrl.getOperatorService().getPSPs().get(0);

    ctrl.addPSPSettingRow().get(0).setDefinition(psp.getPspSettings().get(0));
    ctrl.getModel().setPspId(psp.getId());
    ctrl.getModel().setSelectedPSP(psp);
    ctrl.getModel().setSelectedPaymentType(psp.getPaymentTypes().get(0));
    givenPSP(psp.getId());
  }

  private void givenRowDefintions(VOPSPSetting pspSettings)
      throws ConcurrentModificationException, ValidationException, NonUniqueBusinessKeyException,
          ObjectNotFoundException {
    VOPSP psp = ctrl.getOperatorService().getPSPs().get(0);
    ctrl.addPSPSettingRow().get(0).setDefinition(pspSettings);
    ctrl.getModel().setPspId(psp.getId());
    ctrl.getModel().setSelectedPSP(psp);
    givenPSP(psp.getId());
  }

  private VOPSP givenPSP(String id)
      throws ConcurrentModificationException, ValidationException, NonUniqueBusinessKeyException,
          ObjectNotFoundException {
    VOPSP psp = new VOPSP();
    psp.setId(id);
    doReturn(psp).when(ctrl.getOperatorService()).savePSP(any());
    return psp;
  }

  private void mockPSPs(OperatorService os) {
    List<VOPSP> psps = new ArrayList<VOPSP>();
    VOPSP psp = new VOPSP();
    List<VOPSPSetting> settings = new ArrayList<VOPSPSetting>();
    VOPSPSetting setting = new VOPSPSetting();
    setting.setSettingKey("key");
    setting.setSettingKey("value");
    settings.add(setting);
    psp.setPspSettings(settings);
    mockPaymentTypes(psp);
    psps.add(psp);
    doReturn(psps).when(os).getPSPs();
  }

  private void mockPaymentTypes(VOPSP psp) {
    List<VOPaymentType> paymentTypes = new ArrayList<VOPaymentType>();
    VOPaymentType cc = new VOPaymentType();
    cc.setName(PaymentInfoType.CREDIT_CARD.name());
    cc.setKey(2);
    cc.setPaymentTypeId(PaymentInfoType.CREDIT_CARD.name());
    paymentTypes.add(cc);
    VOPaymentType db = new VOPaymentType();
    db.setName(PaymentInfoType.DIRECT_DEBIT.name());
    db.setKey(3);
    db.setPaymentTypeId(PaymentInfoType.DIRECT_DEBIT.name());
    paymentTypes.add(db);
    psp.setPaymentTypes(paymentTypes);
  }

  private VOOperatorOrganization givenPersistedOrgWithOnlyInvoice() {
    VOOperatorOrganization dbOrg = new VOOperatorOrganization();

    List<VOPaymentType> paymentTypes = new ArrayList<VOPaymentType>();
    VOPaymentType cc = new VOPaymentType();
    cc.setName(PaymentInfoType.INVOICE.name());
    cc.setKey(0);
    cc.setPaymentTypeId(PaymentInfoType.INVOICE.name());
    paymentTypes.add(cc);
    dbOrg.setPaymentTypes(paymentTypes);
    doReturn(org).when(orgSelector).getExistingOrganization();
    return dbOrg;
  }
}
