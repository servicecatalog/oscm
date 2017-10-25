/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter.api;

import org.oscm.dataservice.local.DataService;

public interface DataServiceHolder {
    void setDataService(DataService dataService);

    DataService getDataService();
}
