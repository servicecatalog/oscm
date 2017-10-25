/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                     
 *                                                                              
 *  Creation Date: 14.01.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.CompositeName;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.identityservice.local.ILdapResultMapper;
import org.oscm.identityservice.local.LdapAccessServiceLocal;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Service class to perform an LDAP access.
 * 
 */
@Stateless
@Local(LdapAccessServiceLocal.class)
public class LdapAccessServiceBean implements LdapAccessServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(LdapAccessServiceBean.class);

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    public ConfigurationServiceLocal cs;

    /**
     * Execute a LDAP search and return the result.
     * 
     * @return the search result.
     */
    public <T> List<T> search(Properties properties, String baseDN,
            String filter, ILdapResultMapper<T> mapper, boolean checkAttribute)
            throws NamingException {
        int searchLimit = getSearchLimit();

        return searchByLimit(properties, baseDN, filter, mapper,
                checkAttribute, searchLimit);
    }

    public <T> boolean searchOverLimit(Properties properties, String baseDN,
            String filter, ILdapResultMapper<T> mapper, boolean checkAttribute)
            throws NamingException {
        int searchLimit = getSearchLimit() + 1;

        List<T> list = this.searchByLimit(properties, baseDN, filter, mapper,
                checkAttribute, searchLimit);
        return list.size() == searchLimit;
    }

    private <T> List<T> searchByLimit(Properties properties, String baseDN,
            String filter, ILdapResultMapper<T> mapper, boolean checkAttribute,
            int searchLimit) throws NamingException {
        List<T> list = new ArrayList<T>();
        NamingEnumeration<SearchResult> namingEnum = null;

        DirContext ctx = getDirContext(properties);

        SearchControls ctls = new SearchControls();
        String[] attrIds = mapper.getAttributes();
        ctls.setReturningAttributes(attrIds);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setCountLimit(searchLimit);

        try {
            namingEnum = ctx.search(baseDN, escapeLDAPSearchFilter(filter),
                    ctls);
            int count = 0;
            while (count++ < searchLimit && hasMoreEnum(namingEnum)) {
                SearchResult res = namingEnum.next();
                Attributes ldapAttributes = res.getAttributes();
                String[] values = new String[attrIds.length];
                for (int i = 0; i < values.length; i++) {
                    Attribute ldapAttr = ldapAttributes
                            .get(escapeLDAPSearchFilter(attrIds[i]));
                    if (checkAttribute && ldapAttr == null) {
                        NamingException e = new NamingException(
                                "Unknown LDAP attribute " + attrIds[i]);
                        throw e;
                    }
                    if (ldapAttr != null && ldapAttr.get() != null) {
                        values[i] = ldapAttr.get().toString();
                    }
                }
                T t = mapper.map(values);
                if (t != null) {
                    list.add(t);
                }
            }
        } finally {
            if (namingEnum != null) {
                try {
                    namingEnum.close();
                } finally {
                    closeContext(ctx);
                }
            }
            closeContext(ctx);
        }
        return list;
    }

    private boolean hasMoreEnum(NamingEnumeration<SearchResult> namingEnum)
            throws NamingException {
        boolean hasMore = true;
        try {
            if (!namingEnum.hasMore()) {
                hasMore = false;
            }
        } catch (PartialResultException e) {
            hasMore = false;
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_LDAP_PARTIAL_EXCEPTION);
        }
        return hasMore;
    }

    private String escapeLDAPSearchFilter(String filter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filter.length(); i++) {
            char curChar = filter.charAt(i);
            switch (curChar) {
            case '\\':
                sb.append("\\5c");
                break;
            case '(':
                sb.append("\\28");
                break;
            case ')':
                sb.append("\\29");
                break;
            case '\u0000':
                sb.append("\\00");
                break;
            default:
                sb.append(curChar);
            }
        }
        return sb.toString();
    }

    /**
     * Search for users by "dn" attribute
     * 
     * @param ldapProps
     *            the LDAP properties
     * @param baseDN
     *            the base data name
     * @param filter
     *            the search filter
     * 
     * @return the found data name of the user
     * @throws NamingException
     */
    public String dnSearch(Properties ldapProps, String baseDN, String filter)
            throws NamingException {
        String foundDN = null;
        NamingEnumeration<SearchResult> namingEnum = null;
        final DirContext ctx = new InitialDirContext(ldapProps);

        SearchControls ctls = new SearchControls();
        ctls.setReturningAttributes(new String[] { "dn" });
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setCountLimit(1);

        try {
            namingEnum = ctx.search(baseDN, filter, ctls);
            if (hasMoreEnum(namingEnum)) {
                SearchResult res = namingEnum.next();
                StringBuffer sb = new StringBuffer();
                CompositeName compDN = new CompositeName(res.getName());
                if (!compDN.isEmpty()) {
                    String ldapDN = compDN.get(0);
                    sb.append(ldapDN);

                    if (res.isRelative()) {
                        sb.append(",");
                        sb.append(baseDN);
                    }
                    foundDN = sb.toString();
                }
            }
        } finally {
            if (namingEnum != null) {
                try {
                    namingEnum.close();
                } catch (Exception ex) {
                    logger.logError(
                            Log4jLogger.SYSTEM_LOG,
                            ex,
                            LogMessageIdentifier.ERROR_CLOSE_NAMING_ENUMURATION_FAILED);
                }
            }
            try {
                ctx.close();
            } catch (Exception ex) {
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        ex,
                        LogMessageIdentifier.ERROR_CLOSE_DIRECTORY_CONTEXT_FAILED);
            }
        }

        return foundDN;
    }

    /**
     * Closes the given directory context.
     * 
     * @param The
     *            context to close.
     */
    private void closeContext(DirContext ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (Exception e) {
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.ERROR_CLOSE_DIRECTORY_CONTEXT_FAILED);
            }
        }
    }

    /**
     * Read the LDAP_SEARCH_LIMIT configuration parameter
     * 
     * @return
     */
    protected int getSearchLimit() {
        return cs.getConfigurationSetting(ConfigurationKey.LDAP_SEARCH_LIMIT,
                Configuration.GLOBAL_CONTEXT).getIntValue();
    }

    /**
     * Creates the {@link DirContext} based on the passed properties.
     * 
     * @param props
     *            the LDAP properties
     * @return the created {@link DirContext}
     * @throws NamingException
     */
    protected DirContext getDirContext(Properties props) throws NamingException {
        DirContext ctx = new InitialDirContext(props);
        return ctx;
    }

}
