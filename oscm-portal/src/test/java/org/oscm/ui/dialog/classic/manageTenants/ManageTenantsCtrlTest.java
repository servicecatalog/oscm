/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 05.09.2016
 *
 *******************************************************************************/
package org.oscm.ui.dialog.classic.manageTenants;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.tenant.ManageTenantService;
import org.oscm.internal.tenant.POTenant;
import org.oscm.internal.types.enumtypes.IdpSettingType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.profile.FieldData;

public class ManageTenantsCtrlTest {

    private static final String GENERATED_TENANT_ID = "9hjgadhf";

    private ManageTenantsCtrl ctrl;
    private ManageTenantsModel model;
    private ManageTenantService manageTenantService;
    private UiDelegate uiDelegate;

    @Before
    public void setup() throws Exception {
        ctrl = spy(new ManageTenantsCtrl());
        model = spy(new ManageTenantsModel());
        ctrl.setModel(model);
        manageTenantService = mock(ManageTenantService.class);
        ctrl.setManageTenantService(manageTenantService);
        doNothing().when(ctrl).handleSuccessMessage(anyString(), anyString());
        uiDelegate = mock(UiDelegate.class);
        ctrl.ui = uiDelegate;
        doNothing().when(uiDelegate).handleError(anyString(), anyString());
    }

    @Test
    public void testSetSelectedTenant() throws ObjectNotFoundException {
        //given
        POTenant selectedTenant = prepareTenant();
        when(manageTenantService.getTenantByTenantId(anyString())).thenReturn(selectedTenant);
        when(manageTenantService.getTenantSettings(selectedTenant.getKey())).thenReturn(new Properties());
        //when
        ctrl.setSelectedTenant();

        //then
        assertFalse(model.isClearExportAvailable());
        assertFalse(model.isSaveDisabled());
        assertFalse(model.isImportDisabled());
        assertFalse(model.isDeleteDisabled());
        assertEquals(selectedTenant.getTenantId(), model.getTenantId().getValue());
        assertEquals(selectedTenant.getDescription(), model.getTenantDescription().getValue());
        assertEquals(selectedTenant.getName(), model.getTenantName().getValue());
        assertEquals(selectedTenant.getIdp(), model.getTenantIdp().getValue());
    }

    @Test
    public void testSetSelectedTenantWithProperties() throws ObjectNotFoundException {
        //given
        POTenant selectedTenant = prepareTenant();
        when(manageTenantService.getTenantByTenantId(anyString())).thenReturn(selectedTenant);
        when(manageTenantService.getTenantSettings(selectedTenant.getKey())).thenReturn(prepareProperties());
        //when
        ctrl.setSelectedTenant();

        //then
        assertTrue(model.isClearExportAvailable());
        assertFalse(model.isSaveDisabled());
        assertFalse(model.isImportDisabled());
        assertFalse(model.isDeleteDisabled());
    }

    @Test
    public void testSave_add() throws NonUniqueBusinessKeyException {
        //given
        model.setSelectedTenant(null);
        POTenant poTenant = prepareTenant();
        model.setTenantId(new FieldData<String>(poTenant.getTenantId(), false, true));
        model.setTenantDescription(new FieldData<String>(poTenant.getDescription(), false, true));
        model.setTenantName(new FieldData<String>(poTenant.getName(), false, true));
        when(manageTenantService.addTenant(any(POTenant.class))).thenReturn(GENERATED_TENANT_ID);

        //when
        ctrl.save();

        //then
        assertEquals(model.getSelectedTenantId(), GENERATED_TENANT_ID);
        verify(manageTenantService, times(1)).addTenant(any(POTenant.class));
        verify(model, times(1)).setTenants(anyList());
    }

    @Test
    public void testSave_edit()
        throws NonUniqueBusinessKeyException, ObjectNotFoundException, ConcurrentModificationException {
        //given
        POTenant poTenant = prepareTenant();
        model.setSelectedTenant(poTenant);
        model.setTenantId(new FieldData<String>("edited tenant id", false, true));
        model.setTenantDescription(new FieldData<String>(poTenant.getDescription(), false, true));
        model.setTenantName(new FieldData<String>(poTenant.getName(), false, true));
        doNothing().when(manageTenantService).updateTenant(any(POTenant.class));

        //when
        ctrl.save();

        //then
        assertEquals(model.getSelectedTenantId(), poTenant.getTenantId());
        verify(manageTenantService, times(1)).updateTenant(any(POTenant.class));
        verify(model, times(1)).setTenants(anyList());
    }

    @Test
    public void testAddTenant() {
        //given

        //when
        ctrl.addTenant();

        //then
        assertEquals(model.getSelectedTenant(), null);
        assertEquals(model.getSelectedTenantId(), null);
        assertFalse(model.isClearExportAvailable());
        assertEquals(model.getTenantId().getValue(), null);
        assertEquals(model.getTenantName().getValue(), null);
        assertEquals(model.getTenantDescription().getValue(), null);
        assertEquals(model.getTenantIdp().getValue(), null);
        assertFalse(model.isSaveDisabled());
        assertFalse(model.isImportDisabled());
        assertTrue(model.isDeleteDisabled());
    }

    @Test
    public void testImportSettings_emptyFile() throws SaaSApplicationException {
        //given
        model.setFile(null);

        //when
        String returnValue = ctrl.importSettings();

        //then
        assertEquals(returnValue, "error");
    }

    @Test
    public void testExportSettings() throws IOException {
        //given
        model.setSelectedTenant(prepareTenant());
        doReturn(prepareProperties()).when(manageTenantService).getTenantSettings(anyLong());
        doNothing().when(ctrl).writeSettings(any(byte[].class));

        //when
        String returnValue = ctrl.exportSettings();

        //then
        assertEquals(returnValue, "success");
    }

    private POTenant prepareTenant() {
        POTenant poTenant = new POTenant();
        poTenant.setTenantId(GENERATED_TENANT_ID);
        poTenant.setDescription("description");
        poTenant.setKey(1L);
        poTenant.setName("tenantName");
        poTenant.setVersion(0);
        poTenant.setIdp("");
        return poTenant;
    }

    private Properties prepareProperties() {
        Properties properties = new Properties();
        properties.put(IdpSettingType.SSO_IDP_URL.name(), "idp.url");
        return properties;
    }
}
