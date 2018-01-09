/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertNull;
import org.junit.Assert;

import org.junit.Test;

public class ProductDataTest {
    private static final String PRODUCT_ID = "productId";

    @Test
    public void getCleanProductId_Null() {
        // given
        ProductData product = new ProductData();
        product.setProductId(null);

        // when
        String productId = product.getCleanProductId();

        // then
        assertNull(productId);
    }

    @Test
    public void getCleanProductId_Empty() {
        // given
        ProductData product = new ProductData();
        product.setProductId(" ");

        // when
        String cleanProductId = product.getCleanProductId();

        // then
        Assert.assertEquals("", cleanProductId);
    }

    @Test
    public void getCleanProductId_WithoutSharp() throws Exception {
        // given
        ProductData product = new ProductData();
        product.setProductId(PRODUCT_ID);

        // when
        String cleanProductId = product.getCleanProductId();

        // then
        Assert.assertEquals(PRODUCT_ID, cleanProductId);
    }

    @Test
    public void getCleanProductId_WithSharp() throws Exception {
        // given
        ProductData product = new ProductData();
        product.setProductId(PRODUCT_ID + "#" + System.currentTimeMillis());

        // when
        String cleanProductId = product.getCleanProductId();

        // then
        Assert.assertEquals(PRODUCT_ID, cleanProductId);
    }
}
