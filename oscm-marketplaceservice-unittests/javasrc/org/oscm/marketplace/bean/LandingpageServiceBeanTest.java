/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jul 14, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Product;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;

/**
 * @author zhaoh.fnst
 * 
 */
public class LandingpageServiceBeanTest {

    private LandingpageServiceBeanLocal landingpageServiceBean;

    @Before
    public void setup() throws Exception {
        landingpageServiceBean = spy(new LandingpageServiceBeanLocal());
        landingpageServiceBean.dm = mock(DataService.class);
        landingpageServiceBean.userGroupService = mock(UserGroupServiceLocalBean.class);
    }

    @Test
    public void removeInvisibleProducts() {
        // given
        List<Product> prods = givenProducts();
        List<Long> keys = new ArrayList<Long>();
        keys.add(Long.valueOf(1000L));

        // when
        List<Product> result = landingpageServiceBean.removeInvisibleProducts(
                prods, keys);

        // then
        assertEquals(1, result.size());
        assertEquals(prods.get(1).getKey(), result.get(0).getKey());
    }

    @Test
    public void removeInvisibleProducts_NoInvisibleService() {
        // given
        List<Product> prods = givenProducts();
        List<Long> keys = new ArrayList<Long>();
        keys.add(Long.valueOf(4000L));

        // when
        landingpageServiceBean.removeInvisibleProducts(prods, keys);

        // then
        assertEquals(prods.size(), prods.size());

    }

    @Test
    public void getKeysForLocalization() {
        // given
        List<Product> products = new ArrayList<Product>();
        Product templateProduct = new Product();
        templateProduct.setKey(1000L);
        Product customerProduct = new Product();
        customerProduct.setKey(2000L);
        customerProduct.setTemplate(templateProduct);
        products.add(templateProduct);
        products.add(customerProduct);

        // when
        Set<Long> keys = landingpageServiceBean
                .getKeysForLocalization(products);

        // then
        assertEquals(1, keys.size());
        assertTrue(keys.contains(Long.valueOf(1000L)));
    }

    private List<Product> givenProducts() {
        List<Product> prods = new ArrayList<Product>();
        Product pord1 = new Product();
        pord1.setKey(1000L);
        Product pord2 = new Product();
        pord2.setKey(2000L);
        prods.add(pord1);
        prods.add(pord2);

        return prods;
    }

}
