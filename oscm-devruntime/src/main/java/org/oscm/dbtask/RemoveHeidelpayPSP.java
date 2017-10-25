/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-4-18                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author qiu
 * 
 */
public class RemoveHeidelpayPSP extends DatabaseUpgradeTask {

    private Connection con;

    final static String QUERY_PSP_HEIDELPAYWITHBLANKWSDL = "SELECT tkey FROM psp WHERE identifier = 'heidelpay' AND wsdlurl = ''";
    final static String QUERY_PAYMENTINFOHISTORY_PAYMENTTYPE = "SELECT pih.tkey FROM paymentinfohistory AS pih,paymenttype AS pt WHERE pih.paymenttypeobjkey = pt.tkey AND pt.psp_tkey = 2 ";
    final static String QUERY_PAYMENTINFOHISTORY_PAYMENTTYPEHISTORY = "SELECT pih.tkey FROM paymentinfohistory AS pih,paymenttypehistory AS pth WHERE pih.paymenttypeobjkey = pth.objkey AND pth.pspobjkey = 2 ";
    final static String QUERY_PAYMENTINFO_PAYMENTTYPE = "SELECT pi.tkey FROM paymentinfo AS pi,paymenttype AS pt WHERE pi.paymenttype_tkey = pt.tkey AND pt.psp_tkey = 2 ";
    final static String QUERY_PAYMENTINFO_PAYMENTTYPEHISTORY = "SELECT pi.tkey FROM paymentinfo AS pi,paymenttypehistory AS pth WHERE pi.paymenttype_tkey = pth.objkey AND pth.pspobjkey = 2 ";
    final static String QUERY_PSPACCOUNT = "SELECT tkey FROM pspaccount WHERE psp_tkey = 2";
    final static String QUERY_PSPACCOUNTHISTORY = "SELECT tkey FROM pspaccounthistory WHERE pspobjkey = 2";
    final static String DELETE_PSPSETTING = "DELETE FROM pspsetting WHERE psp_tkey = 2";
    final static String DELETE_PSPSETTINGHISTORY = "DELETE FROM pspsettinghistory WHERE pspobjkey = 2";
    final static String DELETE_PAYMENTTYPE = "DELETE FROM paymenttype WHERE psp_tkey = 2";
    final static String DELETE_PAYMENTTYPEHISTORY = "DELETE FROM paymenttypehistory WHERE pspobjkey = 2";
    final static String DELETE_PSP = "DELETE FROM psp WHERE identifier = 'heidelpay' AND wsdlurl = ''";
    final static String DELETE_PSPHISTORY = "DELETE FROM psphistory WHERE identifier = 'heidelpay' AND wsdlurl = ''";

    @Override
    public void execute() throws Exception {
        con = getConnection();
        if (!isHeidelpayWithBlankWsdl()) {
            return;
        }
        if (isPSPUsed()) {
            return;
        }
        removeEntries();
    }

    private boolean isPSPUsed() throws SQLException {
        return isPSPUsedInPaymentInfo() || isPSPUsedInAccount();
    }

    private boolean isPSPUsedInPaymentInfo() throws SQLException {
        return isEntryQueried(QUERY_PAYMENTINFOHISTORY_PAYMENTTYPE)
                || isEntryQueried(QUERY_PAYMENTINFOHISTORY_PAYMENTTYPEHISTORY)
                || isEntryQueried(QUERY_PAYMENTINFO_PAYMENTTYPE)
                || isEntryQueried(QUERY_PAYMENTINFO_PAYMENTTYPEHISTORY);
    }

    private boolean isPSPUsedInAccount() throws SQLException {
        return isEntryQueried(QUERY_PSPACCOUNT)
                || isEntryQueried(QUERY_PSPACCOUNTHISTORY);
    }

    private boolean isHeidelpayWithBlankWsdl() throws SQLException {
        return isEntryQueried(QUERY_PSP_HEIDELPAYWITHBLANKWSDL);
    }

    private boolean isEntryQueried(String sql) throws SQLException {
        ResultSet rs = con.createStatement().executeQuery(sql);
        if (rs.next()) {
            return true;
        }
        return false;
    }

    private void removeEntries() throws SQLException {
        con.createStatement().executeUpdate(DELETE_PSPSETTING);
        con.createStatement().executeUpdate(DELETE_PSPSETTINGHISTORY);
        con.createStatement().executeUpdate(DELETE_PAYMENTTYPE);
        con.createStatement().executeUpdate(DELETE_PAYMENTTYPEHISTORY);
        con.createStatement().executeUpdate(DELETE_PSP);
        con.createStatement().executeUpdate(DELETE_PSPHISTORY);
    }
}
