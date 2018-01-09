/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.db.importer;

import java.io.InputStream;

public class ClusterCSV extends CSVFileParser {

    public final static String COL_VCENTER = "VCenter";
    public final static String COL_CLUSTER_NAME = "Clustername";
    public final static String COL_DATACENTER = "Datacenter";

    public final static String DEFAULT_ENCODING = "UTF-8";
    public final static char DEFAULT_DELIMITER = ',';

    private final static String MANDATORY_COLUMNS[] = new String[] {
            COL_VCENTER, COL_CLUSTER_NAME, COL_DATACENTER };

    private final static String MANDATORY_COLUMN_VALUES[] = new String[] {
            COL_VCENTER, COL_CLUSTER_NAME, COL_DATACENTER };

    public ClusterCSV(InputStream stream) throws Exception {
        super(stream, DEFAULT_ENCODING, DEFAULT_DELIMITER, MANDATORY_COLUMNS,
                MANDATORY_COLUMN_VALUES);
    }

}
