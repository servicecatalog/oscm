/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class ProductHistoryTest {
    @Test
    public void getCleanProductId() {
        // given
        Product product = new Product();
        product.dataContainer = mock(ProductData.class);

        // when
        product.getCleanProductId();

        // then
        verify(product.dataContainer, times(1)).getCleanProductId();
    }
}
