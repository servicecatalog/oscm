/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 12.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects.similarity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author barzu
 */
public class CustomSimilarityTest {

    @Test
    public void idf() {
        // when
        float idf = new CustomSimilarity().idf(501, 502);

        // then
        assertEquals(Float.valueOf(1F), Float.valueOf(idf));
    }

}
