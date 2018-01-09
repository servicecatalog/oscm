/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Feb 7, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.portal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for integration web tests for the OSCM portal
 * 
 * @author miethaner
 */
@RunWith(Suite.class)

@SuiteClasses({ PortalOrganizationWT.class, PortalMarketplaceWT.class})
public class PlaygroundSuiteTest {

    public static String supplierOrgId;
    public static String customerOrgId;
}
