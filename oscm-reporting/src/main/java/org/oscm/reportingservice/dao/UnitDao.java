/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import java.util.ArrayList;
import java.util.List;

import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;

/**
 * The DAO class responsible for getting all units where the given user is unit
 * administrator.
 * 
 */
public class UnitDao {

    private final DataService ds;

    private static final String QUERY_UNITS_KEYS = "SELECT usergrouptouser.usergroup_tkey FROM usergroup JOIN usergrouptouser ON usergroup.tkey=usergrouptouser.usergroup_tkey JOIN unitroleassignment ON usergrouptouser.tkey=unitroleassignment.usergrouptouser_tkey JOIN unituserrole ON unitroleassignment.unituserrole_tkey=unituserrole.tkey WHERE usergrouptouser.platformuser_tkey=? AND unituserrole.rolename='ADMINISTRATOR'";

    public UnitDao(DataService ds) {
        this.ds = ds;
    }

    public List<Long> retrieveUnitKeysForUnitAdmin(long unitAdminKey) {
        SqlQuery sqlQuery = new SqlQuery(QUERY_UNITS_KEYS);
        sqlQuery.setLong(1, unitAdminKey);
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        return convertToResultData(dataSet);
    }

    private List<Long> convertToResultData(DataSet rs) {
        List<Long> result = new ArrayList<Long>();
        while (rs.next()) {
            result.add(Long.valueOf(rs.getLong(1)));
        }
        return result;
    }

}
