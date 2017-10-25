/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015年3月3日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.enums;

/**
 * @author gaowenxin
 * 
 */
public enum APIVersion {

    V1_9("v1_9", "v1.9", true),

    V1_8("v1_8", "v1.8", false),

    V1_7("v1_7", "v1.7", false);

    private String sourceLocation;
    private String versionName;
    private boolean isCurrentVersion;

    private APIVersion(String sourceLocation, String versionName,
            boolean isCurrentVersion) {
        this.sourceLocation = sourceLocation;
        this.versionName = versionName;
        this.isCurrentVersion = isCurrentVersion;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public boolean isCurrentVersion() {
        return isCurrentVersion;
    }

    public void setCurrentVersion(boolean isCurrentVersion) {
        this.isCurrentVersion = isCurrentVersion;
    }

    public static APIVersion getForURLString(String version) {
        for (APIVersion current : APIVersion.values()) {
            if (version.contains(current.versionName)) {
                return current;
            }
        }
        return null;
    }

    public static APIVersion getCurrentVersion() {
        for (APIVersion current : APIVersion.values()) {
            if (current.isCurrentVersion) {
                return current;
            }
        }
        return null;
    }
}
