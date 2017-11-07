/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.components;

import static org.oscm.test.matchers.JavaMatchers.hasItems;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.vo.VOMarketplace;

/**
 * Test case for MarketplaceSelector. MarketplaceSelector constructs select
 * items for a combo box. The user can select from his owned marketplaces.
 * 
 * @author cheld
 * 
 */
public class MarketplaceSelectorIT extends EJBTestBase {

    MarketplaceSelector marketplaceSelector;

    MarketplaceService marketplaceServiceMock;
    MarketplaceServiceLocal marketplaceServiceLocalMock;

    private MarketplaceSelectorBean bean;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new MarketplaceSelectorBean());
        marketplaceServiceMock = mock(MarketplaceService.class);

        container.addBean(marketplaceServiceMock);
        marketplaceSelector = container.get(MarketplaceSelector.class);
    }

    public void setupWithDSMock() {
        bean = new MarketplaceSelectorBean();
        DataService dsMock = mock(DataService.class);
        PlatformUser user = new PlatformUser();
        user.setLocale("en");
        when(dsMock.getCurrentUser()).thenReturn(user);
        bean.dm = dsMock;
        bean.localizer = mock(LocalizerServiceLocal.class);
    }

    private void setMockMarketplaceServiceLocal(List<Marketplace> marketplaces) {
        MarketplaceServiceLocal mockMarketplaceServiceLocal = mock(MarketplaceServiceLocal.class);
        when(mockMarketplaceServiceLocal.getMarketplacesForSupplier())
                .thenReturn(marketplaces);
        bean.marketplaceServiceLocal = mockMarketplaceServiceLocal;
    }

    /**
     * The VOMarketplace must be converted into select items for the combobox
     * 
     * @throws Exception
     */
    @Test
    public void getMarketplaceSelections() throws Exception {
        // given
        given(marketplaceServiceMock.getMarketplacesOwned()).willReturn(
                twoVOMarketplaces());

        // when
        List<POMarketplace> selection = marketplaceSelector.getMarketplaces();

        // then
        assertThat(selection, hasItems(2));

        for (POMarketplace poMarketplace : selection) {
            if (poMarketplace.getMarketplaceId() == null) {
            } else if (poMarketplace.getMarketplaceId().equals("ID of MP1")) {
                assertThat(poMarketplace.getDisplayName(),
                        equalTo("Name of MP1 (ID of MP1)"));
            } else {
                fail();
            }
        }
    }

    @Test
    public void getMarketplacesForPublishing() throws Exception {
        // given
        setupWithDSMock();
        setMockMarketplaceServiceLocal(twoMarketplaces());

        // when
        List<POMarketplace> selection = bean.getMarketplacesForPublishing();

        // then
        assertThat(selection, hasItems(2));
        assertThat(selection.get(0).getMarketplaceId(), equalTo("ID of MP1"));
        assertThat(selection.get(1).getMarketplaceId(), equalTo("ID of MP2"));
    }

    private List<VOMarketplace> twoVOMarketplaces() {
        List<VOMarketplace> result = new ArrayList<VOMarketplace>();
        VOMarketplace mp1 = new VOMarketplace();
        mp1.setName("Name of MP1");
        mp1.setMarketplaceId("ID of MP1");
        mp1.setKey(123);
        mp1.setVersion(7);
        result.add(mp1);
        VOMarketplace mp2 = new VOMarketplace();
        result.add(mp2);
        return result;
    }

    private List<Marketplace> twoMarketplaces() {
        List<Marketplace> result = new ArrayList<Marketplace>();
        Marketplace mp1 = new Marketplace();

        mp1.setMarketplaceId("ID of MP1");
        mp1.setKey(123);

        result.add(mp1);
        Marketplace mp2 = new Marketplace();
        mp2.setMarketplaceId("ID of MP2");
        result.add(mp2);
        return result;
    }

}
