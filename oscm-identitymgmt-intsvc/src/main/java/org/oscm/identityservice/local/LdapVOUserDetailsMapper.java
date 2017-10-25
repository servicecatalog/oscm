/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                            
 *                                                                              
 *  Creation Date: 01.02.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.vo.VOUserDetails;

public class LdapVOUserDetailsMapper implements
        ILdapResultMapper<VOUserDetails> {

    private VOUserDetails user;
    private List<SettingType> settingList;
    private String attributes[];

    public LdapVOUserDetailsMapper(VOUserDetails user,
            Map<SettingType, String> attrMap) {
        this.user = user;
        settingList = new ArrayList<SettingType>(attrMap.keySet());
        attributes = new String[settingList.size()];
        for (int i = 0; i < attributes.length; i++) {
            attributes[i] = attrMap.get(settingList.get(i));
        }
    }

    public String[] getAttributes() {
        return attributes;
    }

    public VOUserDetails map(String[] values) {
        VOUserDetails targetUser = user;
        if (targetUser == null) {
            targetUser = new VOUserDetails();
        }
        return UserDataAssembler.updateVOUserDetails(values, settingList,
                targetUser);
    }

}
