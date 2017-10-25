/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 22.07.15 13:39
 *
 *******************************************************************************/

package org.oscm.converter.strategy.api;

import org.oscm.domobjects.PlatformUser;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.vo.VOUser;

public class ToExtUserStrategy extends AbstractConversionStrategy implements
        ConversionStrategy<PlatformUser, VOUser> {

    @Override
    public VOUser convert(PlatformUser platformUser) {
        if (platformUser == null) {
            return null;
        }

        VOUser user = new VOUser();

        // Base user data
        user.setKey(platformUser.getKey());
        user.setVersion(platformUser.getVersion());
        user.setUserId(platformUser.getUserId());

        return user;
    }
}
