/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 9, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.enums;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * @author zhaoh.fnst
 * 
 */
public class ApiVersionTest {

    @Test
    public void getVersions_LargeVersion() throws Exception {
        // given
        ApiVersion v = ApiVersion.VERSION_1_9;

        // when
        List<ApiVersion> versions = ApiVersion.getVersions(v);

        // then
        assertEquals(versions.size(), 1);
        assertEquals(versions.get(0).getVersion(), new String("1.9"));
    }

}
