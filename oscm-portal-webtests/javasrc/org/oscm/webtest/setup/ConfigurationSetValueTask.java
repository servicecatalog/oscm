/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jun 12, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import org.oscm.internal.intf.ConfigurationService;

/**
 * @author tokoda
 * 
 */
public class ConfigurationSetValueTask extends WebtestTask {

    private String informationId;
    private String value;

    @Override
    public void executeInternal() throws Exception {
        ConfigurationService cs = getServiceInterface(ConfigurationService.class);
        cs.setConfigurationSetting(informationId, value);
    }

    public void setInformationId(String informationId) {
        this.informationId = informationId;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
