/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-06-27
 *
 *******************************************************************************/

package org.oscm.app.vmware.parser;

import org.oscm.app.vmware.parser.model.Datacenter;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DatacenterParser extends CSVParser<Datacenter> {
    enum Columns {
        VCENTER("VCenter"),
        DATACENTER("Datacenter"),
        DATACENTER_ID("DatacenterID");

        private final String text;

        Columns(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    public DatacenterParser(InputStream stream) throws Exception {
        super(stream);
    }

    @Override
    public List<String> getRequiredColumns() {
        return Arrays.asList(
                Columns.VCENTER.toString(),
                Columns.DATACENTER.toString(),
                Columns.DATACENTER_ID.toString()
        );
    }

    @Override
    public Datacenter readNextObject() throws Exception {
        Datacenter result = new Datacenter();
        Map<String, String> entries = this.readNext();

        if(entries == null) {
            return null;
        }

        result.vCenter = entries.get(Columns.VCENTER.toString());
        result.datacenter = entries.get(Columns.DATACENTER.toString());
        result.datacenterID = entries.get(Columns.DATACENTER_ID.toString());

        return result;
    }
}
