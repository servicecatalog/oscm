/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.db.importer;

import java.io.InputStream;

public class IPPoolCSV extends CSVFileParser {

    public final static String COL_VCENTER = "VCenter";
    public final static String COL_DATACENTER = "Datacenter";
    public final static String COL_CLUSTER = "Cluster";
    public final static String COL_VLAN = "VLAN";
    public final static String COL_IPADDRESS = "IPAddress";

    public final static String DEFAULT_ENCODING = "UTF-8";
    public final static char DEFAULT_DELIMITER = ',';

    private final static String MANDATORY_COLUMNS[] = new String[] {
            COL_IPADDRESS, COL_VCENTER, COL_DATACENTER, COL_CLUSTER, COL_VLAN };

    private final static String MANDATORY_COLUMN_VALUES[] = new String[] {
            COL_IPADDRESS, COL_VCENTER, COL_DATACENTER, COL_CLUSTER, COL_VLAN };

    public IPPoolCSV(InputStream stream) throws Exception {
        super(stream, DEFAULT_ENCODING, DEFAULT_DELIMITER, MANDATORY_COLUMNS,
                MANDATORY_COLUMN_VALUES);
    }

}
