/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-4-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reviewservice.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.List;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.ProductReview;

/**
 * @author Mao
 * 
 */
public class ProductReviewDaoTest {
    
    private ProductReviewDao dao;

    @Before
    public void setup() {
        dao = spy(new ProductReviewDao());
        DataService dm = mock(DataService.class);
        dao.dm = dm;
        Query query = mock(Query.class);
        doReturn(query).when(dm).createNamedQuery(anyString());
    }

    @Test
    public void getProductReviewsForUser() throws Exception {
        // when
        List<ProductReview> result = dao
                .getProductReviewsForUser(any(PlatformUser.class));

        // then
        assertEquals(0, result.size());

    }
}
