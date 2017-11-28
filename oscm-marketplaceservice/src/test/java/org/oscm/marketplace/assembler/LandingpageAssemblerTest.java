/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Enes Sejfi                      
 *                                                                              
 *  Creation Date: 11.06.2012                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.PublicLandingpage;
import org.oscm.domobjects.LandingpageProduct;
import org.oscm.domobjects.Marketplace;
import org.oscm.landingpageService.local.VOPublicLandingpage;
import org.oscm.landingpageService.local.VOLandingpageService;
import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOService;

/**
 * Test class for LandingpageAssembler
 */
public class LandingpageAssemblerTest {

    private PublicLandingpage domLandingpage;
    private VOPublicLandingpage voLandingpage;

    @Before
    public void setUp() {
        // server-side entries
        domLandingpage = new PublicLandingpage();
        domLandingpage.setKey(7);
        domLandingpage.setFillinCriterion(FillinCriterion.NAME_ASCENDING);
        domLandingpage.setNumberServices(19);
        domLandingpage.setLandingpageProducts(createLandingpageServices());
        domLandingpage.setMarketplace(createMarketplace());

        // incoming entries
        voLandingpage = new VOPublicLandingpage();
        voLandingpage.setKey(7897);
        voLandingpage.setVersion(0);
        voLandingpage.setMarketplaceId("4536");
        voLandingpage.setFillinCriterion(FillinCriterion.RATING_DESCENDING);
        voLandingpage.setNumberServices(98);
        voLandingpage.setLandingpageServices(createVOLandingpageServices());
    }

    @Test
    public void testToVOLandingpage() {
        VOPublicLandingpage voLandingpage = LandingpageAssembler
                .toVOLandingpage(domLandingpage);

        assertEquals(domLandingpage.getKey(), voLandingpage.getKey());
        assertEquals(domLandingpage.getVersion(), voLandingpage.getVersion());
        assertNotNull(voLandingpage.getMarketplaceId());
        assertEquals(domLandingpage.getMarketplace().getMarketplaceId(),
                voLandingpage.getMarketplaceId());
        assertEquals(domLandingpage.getFillinCriterion(),
                voLandingpage.getFillinCriterion());
        assertEquals(domLandingpage.getNumberServices(),
                voLandingpage.getNumberServices());
    }

    @Test
    public void testToVOLandingpageNullInput() {
        assertNull(LandingpageAssembler.toVOLandingpage(null));
    }

    @Test
    public void testLandingpageAssembler_UpdateLandingpage()
            throws ValidationException, ConcurrentModificationException {
        PublicLandingpage landingpage = LandingpageAssembler.updateLandingpage(
                new PublicLandingpage(), voLandingpage);

        assertEquals(landingpage.getFillinCriterion(),
                voLandingpage.getFillinCriterion());
        assertEquals(landingpage.getNumberServices(),
                voLandingpage.getNumberServices());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateLandingpage_NullInputVO() throws Exception {
        LandingpageAssembler.updateLandingpage(domLandingpage, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateLandingpage_NullInputDO() throws Exception {
        LandingpageAssembler.updateLandingpage(null, voLandingpage);
    }

    @Test(expected = SaaSSystemException.class)
    public void testUpdateLandingpage_conflictingKeys() throws Exception {
        voLandingpage.setKey(2);
        LandingpageAssembler.updateLandingpage(domLandingpage, voLandingpage);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testUpdateLandingpage_conflictingVersions() throws Exception {
        voLandingpage.setVersion(-1);
        LandingpageAssembler.updateLandingpage(domLandingpage, voLandingpage);
    }

    private List<LandingpageProduct> createLandingpageServices() {
        List<LandingpageProduct> result = new LinkedList<LandingpageProduct>();
        result.add(new LandingpageProduct());
        return result;
    }

    private List<VOLandingpageService> createVOLandingpageServices() {
        List<VOLandingpageService> result = new LinkedList<VOLandingpageService>();

        for (int i = 0; i < 2; i++) {
            VOService voService = new VOService();
            voService.setServiceId("1000" + i);
            VOLandingpageService voLandingpageService = new VOLandingpageService();
            result.add(voLandingpageService);
        }
        return result;
    }

    private Marketplace createMarketplace() {
        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId("43243");
        return marketplace;
    }
}
