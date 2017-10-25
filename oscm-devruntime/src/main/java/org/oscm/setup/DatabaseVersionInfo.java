/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *******************************************************************************/
package org.oscm.setup;

/**
 * Representation of a particular data base version.
 * 
 * @author hoffmann
 */
public class DatabaseVersionInfo implements Comparable<DatabaseVersionInfo> {

    /**
     * Database version which is considered to be lower then all real versions.
     */
    public static final DatabaseVersionInfo MIN = new DatabaseVersionInfo(
            Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    /**
     * Database version which is considered to be higher then all real versions.
     */
    public static final DatabaseVersionInfo MAX = new DatabaseVersionInfo(
            Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private final int productMajorVersion;
    private final int productMinorVersion;
    private final int schemaVersion;

    public DatabaseVersionInfo(int productMajorVersion,
            int productMinorVersion, int schemaVersion) {
        this.productMajorVersion = productMajorVersion;
        this.productMinorVersion = productMinorVersion;
        this.schemaVersion = schemaVersion;
    }

    public int getProductMajorVersion() {
        return productMajorVersion;
    }

    public int getProductMinorVersion() {
        return productMinorVersion;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public int compareTo(DatabaseVersionInfo other) {
        // Product major has highest priority:
        if (getProductMajorVersion() > other.getProductMajorVersion()) {
            return 1;
        }
        if (getProductMajorVersion() < other.getProductMajorVersion()) {
            return -1;
        }

        // Then product minor:
        if (getProductMinorVersion() > other.getProductMinorVersion()) {
            return 1;
        }
        if (getProductMinorVersion() < other.getProductMinorVersion()) {
            return -1;
        }

        // Then schema version:
        return Integer.valueOf(getSchemaVersion()).compareTo(
                Integer.valueOf(other.getSchemaVersion()));
    }

    public String toString() {
        return productMajorVersion + "_" + productMinorVersion + "_"
                + schemaVersion;
    }

}
