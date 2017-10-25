/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 22, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qiu
 * 
 */
public enum ApiVersion {

    VERSION_1_9("1.9", true);

    private ApiVersion(String version) {
        this.version = version;
        this.isCurrentVersion = false;
    }

    private ApiVersion(String version, boolean isCurrentVersion) {
        this.version = version;
        this.isCurrentVersion = isCurrentVersion;
    }

    private String version;
    private boolean isCurrentVersion;

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

    private static final Map<String, ApiVersion> stringToEnum = new HashMap<String, ApiVersion>();
    static {
        for (ApiVersion version : values()) {
            stringToEnum.put(version.toString(), version);
        }
    }

    public static ApiVersion fromString(String v) {
        return stringToEnum.get(v);
    }

    public static List<ApiVersion> getVersions(ApiVersion version) {
        List<ApiVersion> versions = new ArrayList<ApiVersion>();
        for (ApiVersion v : ApiVersion.values()) {
            if (v.ordinal() <= version.ordinal()) {
                versions.add(v);
            }
        }
        Collections.sort(versions);
        return versions;
    }

    /**
     * @return the isCurrentVersion
     */
    public boolean isCurrentVersion() {
        return isCurrentVersion;
    }

    /**
     * @param isCurrentVersion
     *            the isCurrentVersion to set
     */
    public void setCurrentVersion(boolean isCurrentVersion) {
        this.isCurrentVersion = isCurrentVersion;
    }
}
