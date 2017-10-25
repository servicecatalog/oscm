/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017                            .
 *                                                                              
 *  Author: kulle                                       
 *                                                                              
 *  Creation Date: 09.11.2011                                                      
 *                                                                              
 *  Completion Time: 18.11.2011                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author afschar
 */
public class MigrationPSP extends DatabaseUpgradeTask {

    @Override
    public void execute() throws Exception {
        final Connection con = getConnection();
        final ResultSet rs = con
                .createStatement()
                .executeQuery(
                        "SELECT tkey, pspidentifier FROM organization WHERE pspidentifier IS NOT NULL");
        final PreparedStatement st = con
                .prepareStatement("INSERT INTO pspaccount (tkey, version, pspidentifier, psp_tkey, organization_tkey) VALUES (?, 0, ?, 2, ?)");
        final PreparedStatement stHistory = con
                .prepareStatement("INSERT INTO pspaccounthistory (tkey, objversion, objkey, invocationdate, moddate, modtype, moduser, pspidentifier, pspobjkey, organizationobjkey) VALUES (?, 0, ?, now(), now(), 'ADD', '1000', ?, 2, ?)");
        long i = 0, iHistory = 0;
        while (rs.next()) {
            st.setObject(1, new Long(++i));
            st.setObject(2, rs.getString("pspidentifier"));
            st.setObject(3, rs.getObject("tkey"));
            st.executeUpdate();
            stHistory.setObject(1, new Long(++iHistory));
            stHistory.setObject(2, new Long(i));
            stHistory.setObject(3, rs.getString("pspidentifier"));
            stHistory.setObject(4, rs.getObject("tkey"));
            stHistory.executeUpdate();
        }
        con.createStatement().execute(
                "ALTER TABLE organization DROP COLUMN pspidentifier");
        con.createStatement().execute(
                "ALTER TABLE organizationhistory DROP COLUMN pspidentifier");
        con.createStatement().execute(
                "UPDATE hibernate_sequences SET sequence_next_hi_value = "
                        + (i / 1000 + 10)
                        + " WHERE sequence_name = 'PSPAccount'");
        con.createStatement().execute(
                "UPDATE hibernate_sequences SET sequence_next_hi_value = "
                        + (iHistory / 10000 + 10)
                        + " WHERE sequence_name = 'PSPAccountHistory'");

        final PreparedStatement stPropsRead = con
                .prepareStatement("select env_value from configurationsetting where information_id=?");
        final PreparedStatement stPropsInsert = con
                .prepareStatement("INSERT INTO pspsetting (tkey, version, settingkey, settingvalue, psp_tkey) VALUES (?, 0, ?, ?, 2)");
        final PreparedStatement stPropsDelete = con
                .prepareStatement("DELETE FROM configurationsetting where information_id=?");
        final PreparedStatement stPropsInsertHistory = con
                .prepareStatement("INSERT INTO pspsettinghistory (tkey, objversion, objkey, invocationdate, moddate, modtype, moduser, settingkey, settingvalue, pspobjkey) VALUES (?, 0, 1, now(), now(), 'ADD', '1000', ?, ?, 2)");

        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 1, "PSP_POST_URL");
        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 2, "PSP_RESPONSE_SERVLET_URL");
        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 3, "PSP_USER_PWD");
        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 4, "PSP_USER_LOGIN");
        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 5, "PSP_TRANSACTION_CHANNEL");
        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 6, "PSP_SECURITY_SENDER");
        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 7, "PSP_XML_URL");
        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 8, "PSP_TXN_MODE");
        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 9, "BASE_URL");
        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 10, "PSP_SUPPORTED_DD_COUNTRIES");
        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 11, "PSP_SUPPORTED_CC_BRANDS");
        setConfigEntry(stPropsRead, stPropsInsert, stPropsInsertHistory,
                stPropsDelete, 12, "PSP_PAYMENT_REGISTRATION_WSDL");
    }

    private void setConfigEntry(PreparedStatement stPropsRead,
            PreparedStatement stPropsInsert,
            PreparedStatement stPropsInsertHistory,
            PreparedStatement stPropsDelete, int tkey, String key)
            throws SQLException {
        stPropsRead.setString(1, key);
        final ResultSet rs = stPropsRead.executeQuery();
        String value = "";
        if (rs.next()) {
            value = rs.getString(1);
        }

        stPropsInsert.setObject(1, new Integer(tkey));
        stPropsInsert.setObject(2, key);
        stPropsInsert.setObject(3, value);
        stPropsInsert.execute();

        stPropsInsertHistory.setObject(1, new Integer(tkey));
        stPropsInsertHistory.setObject(2, key);
        stPropsInsertHistory.setObject(3, value);
        stPropsInsertHistory.execute();

        stPropsDelete.setString(1, key);
        stPropsDelete.execute();
    }

}
