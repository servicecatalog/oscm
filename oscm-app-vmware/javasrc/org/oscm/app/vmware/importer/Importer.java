package org.oscm.app.vmware.importer;

import java.io.InputStream;

public interface Importer {
    void load(InputStream csvFile) throws Exception;
}
