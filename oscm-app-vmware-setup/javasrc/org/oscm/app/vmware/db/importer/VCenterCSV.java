/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.db.importer;

import java.io.InputStream;

public class VCenterCSV extends CSVFileParser {

    public final static String COL_TKEY = "TKey";
    public final static String COL_VCENTER_NAME = "Name";
    public final static String COL_VCENTER_IDENTIFIER = "Identifier";
    public final static String COL_URL = "URL";
    public final static String COL_USER_ID = "UserId";
    public final static String COL_PASSWORD = "Password";

    public final static String DEFAULT_ENCODING = "UTF-8";
    public final static char DEFAULT_DELIMITER = ',';

    private final static String MANDATORY_COLUMNS[] = new String[] { COL_TKEY,
            COL_VCENTER_NAME, COL_VCENTER_IDENTIFIER, COL_USER_ID,
            COL_PASSWORD };

    private final static String MANDATORY_COLUMN_VALUES[] = new String[] {
            COL_TKEY, COL_VCENTER_NAME, COL_VCENTER_IDENTIFIER, COL_USER_ID,
            COL_PASSWORD };

    public VCenterCSV(InputStream stream) throws Exception {
        super(stream, DEFAULT_ENCODING, DEFAULT_DELIMITER, MANDATORY_COLUMNS,
                MANDATORY_COLUMN_VALUES);
    }

}
