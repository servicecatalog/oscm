/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                     
 *                                                                              
 *  Creation Date: 14.01.2010                                             
 *                                                                              
 *******************************************************************************/
package org.oscm.security;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

/**
 * SQL query to read some PlatformUser attributes, the key of his organization
 * and a possible configure LDAP URL from the data base.
 * 
 */
class UserQuery extends AbstractQuery {
    private String userId = null;
    private long passwordSalt = 0;
    private byte[] passwordHash = null;
    private Long orgKey = null;
    private String realmUserId = null;
    private String status = null;
    private boolean remoteLdapActive;

    private String userKey = null;

    public UserQuery(DataSource ds, String userKey) {
        super(ds);
        this.userKey = userKey;
    }

    @Override
    protected String getStatement() {
        return "SELECT u.userId, u.passwordsalt, u.passwordhash, o.tkey, o.remoteldapactive, u.realmuserid, u.status FROM PlatformUser u, Organization o WHERE u.organizationkey=o.tkey AND u.tkey=?";
    }

    @Override
    protected void mapResult(ResultSet rs) throws SQLException {
        userId = rs.getString(1);
        passwordSalt = rs.getLong(2);
        passwordHash = rs.getBytes(3);
        orgKey = Long.valueOf(rs.getLong(4));
        remoteLdapActive = rs.getBoolean(5);
        realmUserId = rs.getString(6);
        status = rs.getString(7);
    }

    @Override
    protected void setParameters(PreparedStatement p) throws SQLException {
        long queryUserKey = 0;
        try {
            queryUserKey = Long.parseLong(userKey);
            p.setLong(1, queryUserKey);
        } catch (NumberFormatException e) {
            p.setNull(1, Types.BIGINT);
        }
    }

    public String getUserId() {
        return userId;
    }

    public long getPasswordSalt() {
        return passwordSalt;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public Long getOrgKey() {
        return orgKey;
    }

    public String getRealmUserId() {
        return realmUserId;
    }

    public boolean isRemoteLdapActive() {
        return remoteLdapActive;
    }

    public String getStatus() {
        return status;
    }

}
