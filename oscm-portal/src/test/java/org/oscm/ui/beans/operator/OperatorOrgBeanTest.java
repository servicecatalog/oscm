/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 1, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.operator;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.model.SelectItem;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.*;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SelectOrganizationIncludeBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIViewRootStub;

public class OperatorOrgBeanTest {

    private OperatorOrgBean oob;
    private OperatorSelectOrgBean osob;
    private OperatorService osMock = mock(OperatorService.class);
    private VOOrganization orgMock = mock(VOOrganization.class);
    private ApplicationBean appBean = mock(ApplicationBean.class);

    private MarketplaceService msmock;
    private VOMarketplace vMp1, vMp2;

    private List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        FacesContextStub contextStub = new FacesContextStub(Locale.ENGLISH) {
            @Override
            public void addMessage(String arg0, FacesMessage arg1) {
                facesMessages.add(arg1);
            }
        };

        UIViewRootStub vrStub = new UIViewRootStub() {
            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            };
        };
        contextStub.setViewRoot(vrStub);

        oob = spy(new OperatorOrgBean());
        oob.ui = mock(UiDelegate.class);

        osob = spy(new OperatorSelectOrgBean());
        oob.setOperatorSelectOrgBean(osob);
        doReturn(osMock).when(osob).getOperatorService();
        doReturn(osMock).when(oob).getOperatorService();
        osob.setSelectOrganizationIncludeBean(new SelectOrganizationIncludeBean());

        doNothing().when(oob).resetUIComponents(anySet());
        doNothing().when(oob).resetUIInputChildren();
        when(oob.ui.findBean(eq(OperatorOrgBean.APPLICATION_BEAN))).thenReturn(
                appBean);
    }

    @Test
    public void isSupplierPersisted_true() throws Exception {
        // given
        persistedOrgWithSupplierRole();
        supplierRoleLocallySet();

        // when
        boolean persisted = oob.isSupplierOrResellerPersisted();

        // then
        assertTrue(persisted);
    }

    private void persistedOrgWithSupplierRole() throws Exception {
        persistedOrgWithRoles(Arrays.asList(OrganizationRoleType.SUPPLIER));
    }

    private void persistedOrgWithRoles(List<OrganizationRoleType> orgRoles)
            throws OrganizationAuthoritiesException, ObjectNotFoundException {
        when(osMock.getOrganization(anyString()))
                .thenReturn(createNewOrganization(orgRoles))
                .thenReturn(createNewOrganization(orgRoles))
                .thenReturn(createNewOrganization(orgRoles));
        osob.setOrganizationId("organizationId");
        osob.getOrganization();
    }

    private VOOperatorOrganization createNewOrganization(
            List<OrganizationRoleType> orgRoles) {
        VOOperatorOrganization organization = new VOOperatorOrganization();
        organization.setKey(1L);
        organization.setOrganizationId("organizationId");

        List<OrganizationRoleType> newList = new ArrayList<OrganizationRoleType>();
        for (OrganizationRoleType r : orgRoles) {
            newList.add(r);
        }
        organization.setOrganizationRoles(newList);
        return organization;
    }

    @Test
    public void isSupplierPersisted_false() throws Exception {
        // given
        persistedOrgWithoutSupplierRole();

        // when
        boolean persisted = oob.isSupplierOrResellerPersisted();

        // then
        assertFalse(persisted);
    }

    private void persistedOrgWithoutSupplierRole() throws Exception {
        List<OrganizationRoleType> roles = new ArrayList<OrganizationRoleType>();
        roles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        persistedOrgWithRoles(roles);
    }

    @Test
    public void isSupplierPersisted_supplierRoleOnlyLocalySet()
            throws Exception {
        // given
        persistedOrgWithoutSupplierRole();
        supplierRoleLocallySet();

        // when
        boolean persisted = oob.isSupplierOrResellerPersisted();

        // then
        assertFalse(persisted);
    }

    private void supplierRoleLocallySet() throws Exception {
        oob.setSupplier(true);
    }

    @Test
    public void createNewOrganization_withoutRoles() throws Exception {
        // given
        createNewOrganization(new LinkedList<OrganizationRoleType>());
        oob.getNewOrganization();
        doReturn("CustomerOrganization").when(orgMock).getOrganizationId();
        doReturn(orgMock).when(osMock).registerOrganization(
                any(VOOrganization.class), any(VOImageResource.class),
                any(VOUserDetails.class), any(LdapProperties.class),
                anyString());
        oob.setSelectedMarketplace("myMp");

        // when
        String result = oob.createOrganization();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(1, facesMessages.size());
        verify(osMock, times(1))
                .registerOrganization(any(VOOrganization.class),
                        any(VOImageResource.class), any(VOUserDetails.class),
                        any(LdapProperties.class), eq("myMp"));

    }

    @Test
    public void isCustomerOrganization() {
        createNewOrganization(new LinkedList<OrganizationRoleType>());
        assertTrue(oob.isCustomerOrganization());
    }

    @Test
    public void isCustomerOrganization_isNewSupplier() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(oob).isNewSupplier();
        assertFalse(oob.isCustomerOrganization());
    }

    @Test
    public void isCustomerOrganization_isNewTechnologyProvider()
            throws Exception {
        // given
        doReturn(Boolean.TRUE).when(oob).isNewTechnologyProvider();
        assertFalse(oob.isCustomerOrganization());
    }

    @Test
    public void isCustomerOrganization_isNewReseller() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(oob).isNewReseller();
        assertFalse(oob.isCustomerOrganization());
    }

    @Test
    public void isCustomerOrganization_isNewBroker() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(oob).isNewBroker();
        assertFalse(oob.isCustomerOrganization());
    }

    @Test
    public void getSelectableMarketplaces_NotNull() throws ObjectNotFoundException {
        setupMarketplaces();
        List<SelectItem> list = oob.getSelectableMarketplaces();
        assertNotNull(list);
        verify(msmock, times(1)).getAllMarketplacesForTenant(anyLong());
    }

    @Test
    public void getSelectableMarketplaces_Same() throws ObjectNotFoundException {
        setupMarketplaces();
        List<SelectItem> list1 = oob.getSelectableMarketplaces();
        List<SelectItem> list2 = oob.getSelectableMarketplaces();
        verify(msmock, times(2)).getAllMarketplacesForTenant(anyLong());
    }

    @Test
    public void getSelectableMarketplaces() throws ObjectNotFoundException {
        setupMarketplaces();
        List<SelectItem> list = oob.getSelectableMarketplaces();
        verify(msmock, times(1)).getAllMarketplacesForTenant(anyLong());

        assertEquals(2, list.size());
        SelectItem item = list.get(0);
        assertEquals(vMp1.getName() + " (" + vMp1.getMarketplaceId() + ")",
                item.getLabel());
        assertEquals(vMp1.getMarketplaceId(), item.getValue());
    }

    @Test
    public void getSelectableMarketplaces_notOperator() {
        setupMarketplaces();
        doReturn(Boolean.FALSE).when(oob).isLoggedInAndPlatformOperator();
        List<SelectItem> list = oob.getSelectableMarketplaces();
        verify(msmock, never()).getAccessibleMarketplaces();
        assertEquals(0, list.size());
    }

    @Test
    public void isLdapSettingVisible() {
        // given
        when(Boolean.valueOf(appBean.isInternalAuthMode())).thenReturn(
                Boolean.TRUE);
        // when
        Boolean result = Boolean.valueOf(oob.isLdapSettingVisible());
        // then
        assertEquals(Boolean.TRUE, result);
    }

    private void setupMarketplaces() {
        vMp1 = new VOMarketplace();
        vMp1.setKey(1234);
        vMp1.setVersion(3);
        vMp1.setMarketplaceId("vo marketplaceId");
        vMp1.setName("vo name");
        vMp1.setOwningOrganizationId("vo id");
        vMp1.setOwningOrganizationName("vo org name");

        vMp2 = new VOMarketplace();
        vMp2.setKey(1235);
        vMp2.setVersion(4);
        vMp2.setMarketplaceId("another vo MarketplaceId");
        vMp2.setName("another vo name");
        vMp2.setOwningOrganizationId("another vo id");
        vMp2.setOwningOrganizationName("another vo org name");
        vMp2.setOpen(true);

        msmock = mock(MarketplaceService.class);

        try {
            when(msmock.getAllMarketplacesForTenant(anyLong())).thenReturn(
                    Arrays.asList(vMp1, vMp2));
        } catch (ObjectNotFoundException e) {
            fail();
        }

        doReturn(Boolean.TRUE).when(oob).isLoggedInAndPlatformOperator();
        doReturn(msmock).when(oob).getMarketplaceService();
        doNothing().when(oob).addMessage(anyString(), any(Severity.class),
                anyString(), any(Object[].class));
    }

}
