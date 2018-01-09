/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.db.importer;

import java.io.InputStream;

public class VLANCSV extends CSVFileParser {

    public final static String COL_VCENTER = "VCenter";
    public final static String COL_DATACENTER = "Datacenter";
    public final static String COL_CLUSTER = "Cluster";
    public final static String COL_NAME = "Name";
    public final static String COL_GATEWAY = "Gateway";
    public final static String COL_SUBNET_MASK = "SubnetMask";
    public final static String COL_DNSSERVER = "DNSServer";
    public final static String COL_DNSSUFFIX = "DNSSuffix";
    public final static String COL_ENABLED = "Enabled";

    public final static String DEFAULT_ENCODING = "UTF-8";
    public final static char DEFAULT_DELIMITER = ',';

    private final static String MANDATORY_COLUMNS[] = new String[] { COL_NAME,
            COL_GATEWAY, COL_SUBNET_MASK, COL_ENABLED, COL_VCENTER,
            COL_DATACENTER, COL_CLUSTER, COL_DNSSERVER, COL_DNSSUFFIX };

    private final static String MANDATORY_COLUMN_VALUES[] = new String[] {
            COL_NAME, COL_GATEWAY, COL_SUBNET_MASK, COL_ENABLED, COL_VCENTER,
            COL_DATACENTER, COL_CLUSTER, COL_DNSSERVER, COL_DNSSUFFIX };

    public VLANCSV(InputStream stream) throws Exception {
        super(stream, DEFAULT_ENCODING, DEFAULT_DELIMITER, MANDATORY_COLUMNS,
                MANDATORY_COLUMN_VALUES);
    }

}
