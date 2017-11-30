/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Enes Sejfi
 *                                                                              
 *  Creation Date: 07.03.2011                                                      
 *                                                                              
 *  Completion Time: <date>                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.marketplace;

import static org.junit.Assert.assertNull;

import java.util.LinkedList;

import org.junit.Test;

import org.oscm.ui.model.ServiceTag;

/**
 * @author Enes Sejfi
 */
public class TagCloudBeanTest {
    private TagCloudBean tagCloudBean;

    @Test
    public void resetTagsForMarketplace() {
        // given
        tagCloudBean = new TagCloudBean();
        tagCloudBean.tagsForMarketplace = new LinkedList<ServiceTag>();
        tagCloudBean.tagsForMarketplace.add(new ServiceTag("Tag1", "en", 5));

        // when
        tagCloudBean.resetTagsForMarketplace();

        // then
        assertNull(tagCloudBean.tagsForMarketplace);
    }

}
