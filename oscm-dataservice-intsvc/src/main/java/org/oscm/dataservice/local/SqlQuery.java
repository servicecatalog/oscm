/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.local;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kulle
 * 
 */
public class SqlQuery {

    private int max = Integer.MAX_VALUE;
    private String query;
    private final Map<Integer, Object> parameters = new HashMap<Integer, Object>();

    public SqlQuery(String sql) {
        query = sql;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<Integer, Object> getParameters() {
        return parameters;
    }

    private void setParameter(int index, Object value) {
        parameters.put(Integer.valueOf(index), value);
    }

    public void setLong(int index, long value) {
        setParameter(index, Long.valueOf(value));
    }

    public void setDate(int index, Date date) {
        setParameter(index, new java.sql.Timestamp(date.getTime()));
    }

    public void setString(int index, String value) {
        setParameter(index, value);
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

}
