/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2015年1月27日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationtests.apiversioning.factory;

import java.util.Locale;

import org.oscm.types.enumtypes.Salutation;
import org.oscm.types.enumtypes.UserAccountStatus;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * Factory for value objects.
 */
public class VOFactory {

    public static String createUniqueKey() {
        return Long.toString(System.currentTimeMillis());
    }

    public static VOUserDetails createVOUserDetails(String organizationId) {
        VOUserDetails user = new VOUserDetails();
        user.setUserId("User_" + createUniqueKey());
        user.setOrganizationId(organizationId);
        user.setAdditionalName("additionalName");
        user.setAddress("address");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setPhone("08154711");
        user.setEMail("gaowenxin@test.fnst.cn.fujitsu.com");
        user.setLocale(Locale.ENGLISH.getLanguage());
        user.setSalutation(Salutation.MR);
        user.setStatus(UserAccountStatus.ACTIVE);
        return user;
    }

    public static VOUser createVOUser(String userId) {
        VOUser user = new VOUser();
        user.setUserId(userId);
        return user;
    }

    public static VOUser createVOUserWithKey(long userKey) {
        VOUser user = new VOUser();
        user.setKey(userKey);
        return user;
    }
}
