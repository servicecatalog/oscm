/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-06-27
 *
 *******************************************************************************/

package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.parser.VCenterParser;
import org.oscm.app.vmware.parser.model.VCenter;
import org.oscm.app.vmware.persistence.DataAccessService;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VCenterImporter implements Importer {
    private final DataAccessService das;

    private void save(VCenter vCenter) throws Exception {
        String query = "insert into vcenter (TKEY, NAME, IDENTIFIER, URL, USERID, PASSWORD) values(?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(vCenter.tKey));
            stmt.setString(2, vCenter.name);
            stmt.setString(3, vCenter.identifier);
            stmt.setString(4, vCenter.url);
            stmt.setString(5, vCenter.userId);
            stmt.setString(6, vCenter.password);
            stmt.execute();
        }

        this.createSequence(vCenter.identifier);
    }

    private void update(VCenter vCenter) throws Exception {
        String query = "UPDATE vcenter set NAME=?, IDENTIFIER=?, URL=?, USERID=?, PASSWORD=? WHERE TKEY=?";
        try (PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.setString(1, vCenter.name);
            stmt.setString(2, vCenter.identifier);
            stmt.setString(3, vCenter.url);
            stmt.setString(4, vCenter.userId);
            stmt.setString(5, vCenter.password);
            stmt.setInt(6, Integer.parseInt(vCenter.tKey));
            stmt.execute();
        }

        String vCenterId = this.getVCenterId(vCenter.tKey);
        if (!vCenter.identifier.equals(vCenterId)) {
            this.updateSequence(vCenterId, vCenter.identifier);
        }
    }

    private void createSequence(String vCenterId) throws Exception {
        String query = "create sequence vcenter_" + vCenterId.trim() + "_seq increment 1 minvalue 1 maxvalue 10000 start 1 cycle";
        try (PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.execute();
        }
    }

    private void updateSequence(String oldVCenterId, String newVCenterId) throws Exception {
        String query = "alter sequence vcenter_" + oldVCenterId.trim() + "_seq rename to vcenter_" + newVCenterId.trim() + "_seq";
        try (PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.execute();
        }
    }

    private boolean exists(String tKey) throws Exception {
        String query = "select count(*) as count FROM vcenter WHERE TKEY=?";
        try (PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(tKey));
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next() && resultSet.getInt("count") > 0;
        }
    }

    private String getVCenterId(String tkey) throws Exception {
        String query = "SELECT identifier FROM vcenter WHERE tkey = ?";
        try (PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.setInt(1, Integer.parseInt(tkey));
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new Exception("VCenter with key " + tkey + " not found");
            }

            return rs.getString(1);
        }
    }

    VCenterImporter(DataAccessService das) {
        this.das = das;
    }

    @Override
    public void load(InputStream csvFile) throws Exception {
        try (VCenterParser parser = new VCenterParser(csvFile)) {
            VCenter vCenter;
            while ((vCenter = parser.readNextObject()) != null) {
                if (exists(vCenter.tKey)) {
                    this.save(vCenter);
                } else {
                    this.update(vCenter);
                }
            }
        }
    }
}
