/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.11.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices.handler;

import java.util.EnumSet;

/**
 * @author stavreva
 * 
 */
public enum SupportedVersions {

    API_VERSION_V1_9("v1.9");

    private SupportedVersions(String version) {
        this.version = version;
    }

    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return this.version;
    }

    public static boolean contains(String version) {
        EnumSet<SupportedVersions> supportedVersions = EnumSet
                .allOf(SupportedVersions.class);
        for (SupportedVersions supportedVersion : supportedVersions) {
            if (supportedVersion.toString().equals(version)) {
                return true;
            }
        }
        return false;
    }

    public static String getSupportedVersionsAsString() {
        String out = "";
        SupportedVersions[] allVersions = SupportedVersions.values();
        out += allVersions[0].getVersion();
        for (int i = 1; i < allVersions.length; i++) {
            out += "," + allVersions[0].getVersion();
        }
        return out;
    }
}
