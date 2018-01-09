/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-08-30
 *
 *******************************************************************************/
package org.oscm.internal.types.enumtypes;

import java.util.HashSet;
import java.util.Set;

public enum IdpSettingType {

    SSO_IDP_AUTHENTICATION_REQUEST_HTTP_METHOD,

    SSO_IDP_URL,

    SSO_ISSUER_ID,

    SSO_LOGOUT_URL,

    SSO_STS_ENCKEY_LEN,

    SSO_STS_METADATA_URL, SSO_STS_URL,

    SSO_IDP_SAML_ASSERTION_ISSUER_ID;


    public static boolean contains(String enumKey) {
        Set<String> enumKeys = new HashSet<String>();
        for (IdpSettingType type : IdpSettingType.values()) {
            enumKeys.add(type.name());
        }
        return enumKeys.contains(enumKey);
    }
}
