/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import javax.faces.application.FacesMessage.Severity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.tenant.ManageTenantService;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.ui.model.NewMarketplace;

@SuppressWarnings("boxing")
public class NewMarketplaceBeanTest {

    NewMarketplaceBean nmpb;
    TenantsUtilBean tub;
    private MenuBean mb;
    private MarketplaceService mps;
    private ManageTenantService mts;

    @Before
    public void setup() throws Exception {
        nmpb = spy(new NewMarketplaceBean());
        mb = mock(MenuBean.class);
        mps = mock(MarketplaceService.class);
        mts = mock(ManageTenantService.class);

        tub = new TenantsUtilBean();
        tub.setManageTenantService(mts);
        doReturn(mps).when(nmpb).getMarketplaceService();
        doNothing().when(nmpb).addMessage(anyString(), any(Severity.class),
                anyString(), anyString());

        doAnswer(new Answer<VOMarketplace>() {

            @Override
            public VOMarketplace answer(InvocationOnMock invocation)
                    throws Throwable {
                VOMarketplace mp = (VOMarketplace) invocation.getArguments()[0];
                mp.setMarketplaceId("marketplaceId");
                return mp;
            }
        }).when(mps).createMarketplace(any(VOMarketplace.class));

        nmpb.menuBean = mb;
    }

    @Test
    public void getModel() {
        assertNotNull(nmpb.getModel());
    }

    @Test
    public void getModel_twice() {
        NewMarketplace m1 = nmpb.getModel();
        NewMarketplace m2 = nmpb.getModel();
        assertSame(m1, m2);
    }

    @Test
    public void toVOMarketplace() {
        NewMarketplace nmp = new NewMarketplace();
        nmp.setName("newmarketplace");
        nmp.setClosed(false);
        nmp.setOwningOrganizationId("new marketplace id");
        nmp.setTaggingEnabled(false);
        nmp.setReviewEnabled(false);
        nmp.setSocialBookmarkEnabled(true);
        nmp.setCategoriesEnabled(false);
        nmp.setTenantId("tenant123");

        VOMarketplace vmp = nmpb.toVOMarketplace(nmp);

        assertEquals(nmp.getName(), vmp.getName());
        assertEquals(nmp.getOwningOrganizationId(),
                nmp.getOwningOrganizationId());

        assertEquals(!nmp.isClosed(), vmp.isOpen());
        assertEquals(nmp.isTaggingEnabled(), vmp.isTaggingEnabled());
        assertEquals(nmp.isReviewEnabled(), vmp.isReviewEnabled());
        assertEquals(nmp.isSocialBookmarkEnabled(),
                vmp.isSocialBookmarkEnabled());
        assertEquals(nmp.isCategoriesEnabled(), vmp.isCategoriesEnabled());
        assertEquals(nmp.getTenantId(), vmp.getTenantId());

    }

    @Test
    public void toVOMarketplace_null() {
        VOMarketplace vmp = nmpb.toVOMarketplace(null);
        assertNull(vmp);
    }

    @Test
    public void createMarketplace() throws Exception {
        NewMarketplace model1 = nmpb.getModel();
        String token = nmpb.getToken();
        nmpb.setToken(token);
        nmpb.createMarketplace();
        ArgumentCaptor<VOMarketplace> ac = ArgumentCaptor
                .forClass(VOMarketplace.class);
        verify(mps, times(1)).createMarketplace(ac.capture());

        verify(mb, times(1)).resetMenuVisibility();
        VOMarketplace value = ac.getValue();
        assertNotNull(value);
        NewMarketplace model2 = nmpb.getModel();

        // make sure the old model is not available any more
        assertNotSame(model1, model2);

    }

    @Test
    public void createMarketplace_invalidToken() throws Exception {

        nmpb.createMarketplace();
        verifyNoMoreInteractions(mps, mb);
    }

    @Test
    public void getSuggestionsForTenant() throws Exception {

        // given
        String tenantId = "testId";

        // when
        tub.getSuggestionsForTenants(tenantId);

        // then
        verify(mts, times(1)).getTenantsByIdPattern(tenantId+"%");
    }
}
