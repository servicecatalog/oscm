package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.parser.DatacenterParser;
import org.oscm.app.vmware.parser.model.Datacenter;
import org.oscm.app.vmware.persistence.DataAccessService;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatacenterImporter implements Importer {
    private final DataAccessService das;

    private void save(Datacenter datacenter) throws Exception {
        int vCenterKey = this.getVCenterKey(datacenter.vCenter);
        String query = "INSERT INTO datacenter (TKEY, NAME, IDENTIFIER, VCENTER_TKEY) VALUES (DEFAULT, ?, ?, ?)";
        try (PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.setString(1, datacenter.datacenter);
            stmt.setString(2, datacenter.datacenterID);
            stmt.setInt(3, vCenterKey);
            stmt.execute();
        }
    }

    private int getVCenterKey(String vCenter) throws Exception {
        String query = "SELECT tkey FROM vcenter WHERE name = ?";
        try(PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.setString(1, vCenter);
            ResultSet rs = stmt.executeQuery();

            if(!rs.next()) {
                throw new Exception("VCenter " + vCenter + " not found");
            }

            return rs.getInt(1);
        }
    }

    DatacenterImporter(DataAccessService das) {
        this.das = das;
    }

    @Override
    public void load(InputStream csvFile) throws Exception {
        try(DatacenterParser parser = new DatacenterParser(csvFile)) {
            Datacenter datacenter;
            while((datacenter = parser.readNextObject()) != null) {
                this.save(datacenter);
            }
        }
    }
}
