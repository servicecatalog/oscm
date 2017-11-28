/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: groch                                                     
 *                                                                              
 *  Creation Date: Jan 27, 2011                                                      
 *                                                                              
 *  Completion Time: Feb 1, 2011                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOMarketplace;

/**
 * Test class for MarketplaceAssembler
 * 
 * @author groch
 * 
 */
@SuppressWarnings("boxing")
public class MarketplaceAssemblerTest {

    private Marketplace domMpDefault;
    private LocalizerFacade facade;
    private String localizedText;
    private VOMarketplace voMpDefault;

    @Before
    public void setUp() {
        // server-side entries
        domMpDefault = new Marketplace();
        domMpDefault.setKey(1);
        domMpDefault.setMarketplaceId("MP1_DO");
        domMpDefault.setOrganization(new Organization());
        domMpDefault.setOpen(false);

        LocalizerServiceStub localizerServiceStub = new LocalizerServiceStub() {

            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return localizedText;
            }

        };

        // incoming entries
        voMpDefault = new VOMarketplace();
        voMpDefault.setMarketplaceId("MP1_VO");
        voMpDefault.setKey(1);
        voMpDefault.setVersion(0);
        voMpDefault.setOpen(true);
        voMpDefault.setCategoriesEnabled(true);

        domMpDefault.setTaggingEnabled(false);
        domMpDefault.setReviewEnabled(false);
        domMpDefault.setSocialBookmarkEnabled(false);
        domMpDefault.setCategoriesEnabled(true);

        facade = new LocalizerFacade(localizerServiceStub, "en");
    }

    @Test
    public void testToVOMarketplace_NullInput() {
        assertNull(MarketplaceAssembler.toVOMarketplace(null, facade));
    }

    @Test
    public void testToVOMarketplace_OwnerNullInput() {
        Marketplace mp = new Marketplace();
        mp.setOrganization(null);
        VOMarketplace vo = MarketplaceAssembler.toVOMarketplace(mp, facade);
        assertNotNull(vo);
        assertNull(vo.getOwningOrganizationId());
    }

    @Test
    public void testToVOMarketplace() {
        VOMarketplace voMp = MarketplaceAssembler.toVOMarketplace(domMpDefault,
                facade);
        assertNotNull(voMp);
        assertEquals(domMpDefault.getKey(), voMp.getKey());
        assertEquals(domMpDefault.getVersion(), voMp.getVersion());
        assertEquals(domMpDefault.getMarketplaceId(), voMp.getMarketplaceId());
        assertEquals(facade.getText(domMpDefault.getKey(),
                LocalizedObjectTypes.MARKETPLACE_NAME), voMp.getName());
        assertEquals(domMpDefault.isOpen(), voMp.isOpen());
        assertEquals(domMpDefault.getPublicLandingpage() != null, voMp.isHasPublicLandingPage());

        // dom object initialize with FALSE in the setup
        assertFalse(domMpDefault.isTaggingEnabled());
        assertFalse(domMpDefault.isReviewEnabled());
        assertFalse(domMpDefault.isSocialBookmarkEnabled());
        assertTrue(domMpDefault.isCategoriesEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMarketplace_NullInputVO() throws Exception {
        MarketplaceAssembler.updateMarketplace(domMpDefault, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMarketplace_NullInputDO() throws Exception {
        MarketplaceAssembler.updateMarketplace(null, voMpDefault);
    }

    @Test
    public void testUpdateMarketplace() throws Exception {
        assertFalse(voMpDefault.isOpen() == domMpDefault.isOpen());
        Marketplace domMp = MarketplaceAssembler.updateMarketplace(
                domMpDefault, voMpDefault);
        assertNotNull(domMp);
        assertEquals(voMpDefault.getKey(), domMp.getKey());
        assertEquals(voMpDefault.getVersion(), domMp.getVersion());
        assertEquals(voMpDefault.getMarketplaceId(), domMp.getMarketplaceId());
        assertTrue(voMpDefault.isOpen() == domMp.isOpen());
        assertTrue(voMpDefault.isCategoriesEnabled() == domMp
                .isCategoriesEnabled());

        // The VO initialized to TRUE by default
        assertTrue(domMp.isTaggingEnabled());
        assertTrue(domMp.isReviewEnabled());
        assertTrue(domMp.isSocialBookmarkEnabled());
        assertTrue(domMp.isCategoriesEnabled());
    }

    @Test
    public void testToMarketplace() throws Exception {
        Marketplace domMp = MarketplaceAssembler.toMarketplace(voMpDefault);
        assertNotNull(domMp);
        assertEquals(0, domMp.getKey());
        assertEquals(voMpDefault.getVersion(), domMp.getVersion());
        assertEquals(voMpDefault.getMarketplaceId(), domMp.getMarketplaceId());
        assertTrue(voMpDefault.isOpen() == domMp.isOpen());
        assertTrue(voMpDefault.isCategoriesEnabled() == domMp
                .isCategoriesEnabled());
    }

    @Test
    public void testToMarketplaceWithKey() throws Exception {
        voMpDefault.setKey(5);
        Marketplace domMp = MarketplaceAssembler.toMarketplaceWithKey(voMpDefault);
        assertNotNull(domMp);
        assertEquals(voMpDefault.getKey(), domMp.getKey());
        assertEquals(voMpDefault.getVersion(), domMp.getVersion());
        assertEquals(voMpDefault.getMarketplaceId(), domMp.getMarketplaceId());
        assertTrue(voMpDefault.isOpen() == domMp.isOpen());
        assertTrue(voMpDefault.isCategoriesEnabled() == domMp
            .isCategoriesEnabled());
    }

    @Test(expected = SaaSSystemException.class)
    public void testUpdateMarketplace_conflictingKeys() throws Exception {
        voMpDefault.setKey(2);
        MarketplaceAssembler.updateMarketplace(domMpDefault, voMpDefault);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testUpdateMarketplace_conflictingVersions() throws Exception {
        voMpDefault.setVersion(-1);
        MarketplaceAssembler.updateMarketplace(domMpDefault, voMpDefault);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateMarketplace_updateTooLongMarketplaceId()
            throws Exception {
        voMpDefault.setMarketplaceId(BaseAdmUmTest.TOO_LONG_ID);
        Marketplace domObj = MarketplaceAssembler.updateMarketplace(
                domMpDefault, voMpDefault);
        assertEquals(voMpDefault.getMarketplaceId(), domObj.getMarketplaceId());
    }

}
