/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 22.07.15 13:39
 *
 *******************************************************************************/

package org.oscm.converter.strategy.domain;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.vo.VOUser;

public class ToDomUserStrategy implements
        ConversionStrategy<VOUser, PlatformUser> {

    private DataService dataService;

    @Override
    public PlatformUser convert(VOUser voUser) {
        if (voUser == null) {
            return null;
        }

        PlatformUser user = new PlatformUser();

        user.setKey(voUser.getKey());
        user.setUserId(voUser.getUserId());

        return user;
    }

    @Override
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public DataService getDataService() {
        return dataService;
    }
}
