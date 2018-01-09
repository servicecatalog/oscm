/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 13.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author baumann
 * 
 */
public class TestData {

    private List<VendorData> testVendors = new ArrayList<VendorData>();

    public TestData(VendorData... vendors) {
        for (VendorData vendorData : vendors) {
            addVendor(vendorData);
        }
    }

    public void addVendor(VendorData vendorData) {
        testVendors.add(vendorData);
    }

    public VendorData getVendor(int index) {
        return testVendors.get(index);
    }

}
