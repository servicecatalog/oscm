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
import java.util.Properties;

import javax.naming.Context;
import javax.sql.DataSource;

import org.oscm.internal.types.enumtypes.SettingType;

/**
 * SQL query to read the organization specific LDAP properties from the
 * OrganizationSetting table.
 * 
 */
public class OrganizationSettingQuery extends AbstractQuery {

    private String baseDn;
    private String attrUid = SettingType.LDAP_ATTR_UID.getDefaultValue();
    private String contextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
    private String credentials;
    private String principal;
    private String url;
    private Long orgTkey;
    private String referral;

    OrganizationSettingQuery(DataSource ds, Long orgTkey) {
        super(ds);
        this.orgTkey = orgTkey;
    }

    @Override
    protected String getStatement() {
        return "SELECT os.settingtype, os.settingvalue, ps.settingvalue FROM organizationsetting os LEFT OUTER JOIN platformsetting ps ON os.settingtype = ps.settingtype WHERE organization_tkey = ?";
    }

    @Override
    protected void mapResult(ResultSet rs) throws SQLException {
        int col = 1;
        String type = rs.getString(col++);
        String orgValue = rs.getString(col++);
        String platformValue = rs.getString(col++);
        if (SettingType.LDAP_ATTR_UID.toString().equals(type)) {
            attrUid = resolveSettingValue(orgValue, platformValue);
        } else if (SettingType.LDAP_BASE_DN.toString().equals(type)) {
            baseDn = resolveSettingValue(orgValue, platformValue);
        } else if (SettingType.LDAP_CONTEXT_FACTORY.toString().equals(type)) {
            contextFactory = resolveSettingValue(orgValue, platformValue);
        } else if (SettingType.LDAP_CREDENTIALS.toString().equals(type)) {
            credentials = resolveSettingValue(orgValue, platformValue);
        } else if (SettingType.LDAP_PRINCIPAL.toString().equals(type)) {
            principal = resolveSettingValue(orgValue, platformValue);
        } else if (SettingType.LDAP_URL.toString().equals(type)) {
            url = resolveSettingValue(orgValue, platformValue);
        } else if (SettingType.LDAP_ATTR_REFERRAL.toString().equals(type)) {
            referral = resolveSettingValue(orgValue, platformValue);
        }
    }

    @Override
    protected void setParameters(PreparedStatement p) throws SQLException {
        p.setLong(1, orgTkey.longValue());
    }

    /**
     * Return the read LDAP properties of the organization.
     * 
     * @return the read LDAP properties of the organization.
     */
    public Properties getProperties() {

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        props.put(Context.PROVIDER_URL, url);
        if (referral != null && referral.trim().length() != 0) {
            props.put(Context.REFERRAL, referral);
        }
        if (principal != null) {
            props.put(Context.SECURITY_PRINCIPAL, principal);
        }
        if (credentials != null) {
            props.put(Context.SECURITY_CREDENTIALS, credentials);
        }

        return props;
    }

    /**
     * Return the read base data name.
     */
    public String getBaseDN() {
        return baseDn;
    }

    /**
     * Return the LDAP attribute for the userId
     */
    public String getAttrUid() {
        return attrUid;
    }

    public String getReferral() {
        return referral;
    }

    public void setReferral(String referral) {
        this.referral = referral;
    }

    /**
     * Returns the organization setting value in case it is set. Otherwise the
     * platform setting will be returned.
     * 
     * @param orgSettingValue
     *            The organization setting value.
     * @param platformSettingValue
     *            The platform setting value.
     * @return The setting value to be used for further processing.
     */
    private String resolveSettingValue(String orgSettingValue,
            String platformSettingValue) {
        if (orgSettingValue != null && orgSettingValue.length() == 0) {
            return platformSettingValue;
        }
        return orgSettingValue;
    }
}
