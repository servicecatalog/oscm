/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 16.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * @author weiser
 * 
 */
public class UdaDefinitionDeleteTask extends WebtestTask {

    private String udaIds;

    @Override
    public void executeInternal() throws Exception {
        AccountService as = getServiceInterface(AccountService.class);
        List<VOUdaDefinition> udaDefinitions = as.getUdaDefinitions();
        Set<String> ids = new HashSet<String>(Arrays.asList(udaIds.split(",")));
        List<VOUdaDefinition> toDelete = new ArrayList<VOUdaDefinition>();
        for (VOUdaDefinition def : udaDefinitions) {
            if (ids.contains(def.getUdaId())) {
                toDelete.add(def);
            }
        }
        as.saveUdaDefinitions(new ArrayList<VOUdaDefinition>(), toDelete);
    }

    public void setUdaIds(String udaIds) {
        this.udaIds = udaIds;
    }

}
