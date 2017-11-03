/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;

import org.oscm.identityservice.local.ILdapResultMapper;
import org.oscm.identityservice.local.LdapAccessServiceLocal;

public class LdapAccessServiceStub implements LdapAccessServiceLocal {

    @Override
    public <T> List<T> search(Properties properties, String baseDN,
            String filter, ILdapResultMapper<T> mapper, boolean checkAttribute)
            throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String dnSearch(Properties ldapProps, String baseDN, String filter)
            throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> boolean searchOverLimit(Properties properties, String baseDN,
            String filter, ILdapResultMapper<T> mapper, boolean checkAttribute)
            throws NamingException {
        throw new UnsupportedOperationException();
    }

}
