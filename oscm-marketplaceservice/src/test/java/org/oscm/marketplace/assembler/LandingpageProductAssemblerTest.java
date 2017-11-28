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

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.LandingpageProduct;
import org.oscm.domobjects.Product;
import org.oscm.landingpageService.local.VOLandingpageService;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOService;

/**
 * Test class for LandingpageAssembler
 */
public class LandingpageProductAssemblerTest {

    private Product domProduct;
    private LandingpageProduct domLandingpageProduct;
    private VOLandingpageService voLandingpageService;

    @Before
    public void setUp() {
        // server-side entries
        domLandingpageProduct = new LandingpageProduct();
        domLandingpageProduct.setKey(54323);
        domLandingpageProduct.setPosition(5);

        domProduct = new Product();
        domProduct.setKey(432);
        domLandingpageProduct.setProduct(domProduct);

        // incoming entries
        voLandingpageService = new VOLandingpageService();
        voLandingpageService.setKey(7897);
        voLandingpageService.setVersion(0);
        voLandingpageService.setPosition(2);

        VOService service = new VOService();
        service.setServiceId("345345");
        voLandingpageService.setService(service);
    }

    @Test
    public void toVOLandingpageService() {
        VOLandingpageService voLandingpageService = LandingpageProductAssembler
                .toVOLandingpageService(domLandingpageProduct);

        assertEquals(domLandingpageProduct.getKey(),
                voLandingpageService.getKey());
        assertEquals(domLandingpageProduct.getVersion(),
                voLandingpageService.getVersion());
        assertEquals(domLandingpageProduct.getPosition(),
                voLandingpageService.getPosition());
    }

    @Test
    public void testToVOLandingpageServiceNullInput() {
        assertNull(LandingpageProductAssembler.toVOLandingpageService(null));
    }

    @Test
    public void toLandingpageService() throws ValidationException {
        LandingpageProduct landingpageProduct = LandingpageProductAssembler
                .toLandingpageProduct(voLandingpageService);

        assertNotNull(landingpageProduct);
        assertEquals(0, landingpageProduct.getKey());
        assertEquals(voLandingpageService.getVersion(),
                landingpageProduct.getVersion());
        assertEquals(landingpageProduct.getPosition(),
                voLandingpageService.getPosition());
    }
}
