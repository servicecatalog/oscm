/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-06-27
 *
 *******************************************************************************/

package org.oscm.app.vmware.importer;

import java.io.InputStream;

public interface Importer {
    void load(InputStream csvFile) throws Exception;
}
