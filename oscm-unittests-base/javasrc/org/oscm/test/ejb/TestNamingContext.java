/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 19, 2011                                                      
 *                                                                              
 *  Completion Time: July 20, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.test.ejb;

import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.*;

import org.oscm.dataservice.bean.HibernateIndexer;
import org.oscm.internal.types.exception.UnsupportedOperationException;

/**
 * Test stub implementation for an initial context. Only a basic bind() and
 * lookup() is supported.
 * 
 * @author Dirk Bernsau
 * 
 */
public class TestNamingContext implements Context {

    private HashMap<String, Object> ctxMap = new HashMap<String, Object>();
    private Hashtable<?, ?> env;

    public TestNamingContext(Hashtable<?, ?> environment)
            throws NamingException {
        env = environment;
        bind("BSSDS", TestDataSources.get("oscm-domainobjects")
                .getDataSource());
        bind("BSSAppDS", TestDataSources.get("oscm-app")
                .getDataSource());
        bind("java:appserver/TransactionManager", env.get("java:appserver/TransactionManager"));
        bind("org.oscm.dataservice.bean.HibernateIndexer", env.get("org.oscm.dataservice.bean.HibernateIndexer"));
    }

    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        throw new UnsupportedOperationException(
                "Not yet implemented in MOCK! - " + propName + ", " + propVal);
    }

    public void bind(Name name, Object obj) throws NamingException {
        bind(name.toString(), obj);
    }

    public void bind(String name, Object obj) throws NamingException {
        ctxMap.put(name, obj);
    }

    public void close() throws NamingException {
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        return new CompoundName(
                composeName(name.toString(), prefix.toString()),
                System.getProperties());
    }

    public String composeName(String name, String prefix)
            throws NamingException {
        throw new NamingException("Not yet implemented in MOCK! - " + name
                + ", " + prefix);
    }

    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

    public Context createSubcontext(String name) throws NamingException {
        throw new UnsupportedOperationException(
                "Not yet implemented in MOCK! - " + name);
    }

    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    public void destroySubcontext(String name) throws NamingException {
        throw new UnsupportedOperationException(
                "Not yet implemented in MOCK! - " + name);
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return env;
    }

    public String getNameInNamespace() throws NamingException {
        throw new UnsupportedOperationException("Not yet implemented in MOCK!");
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return new NameParser() {
            @Override
            public Name parse(String name) throws NamingException {
                return new CompoundName("" + name, System.getProperties());
            }
        };
    }

    public NameParser getNameParser(String name) throws NamingException {
        return getNameParser((Name) null);
    }

    public NamingEnumeration<NameClassPair> list(Name name)
            throws NamingException {
        return list(name.toString());
    }

    public NamingEnumeration<NameClassPair> list(String name)
            throws NamingException {
        throw new UnsupportedOperationException(
                "Not yet implemented in MOCK! - " + name);
    }

    public NamingEnumeration<Binding> listBindings(Name name)
            throws NamingException {
        return listBindings(name.toString());
    }

    public NamingEnumeration<Binding> listBindings(String name)
            throws NamingException {
        throw new UnsupportedOperationException(
                "Not yet implemented in MOCK! - " + name);
    }

    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    public Object lookup(String name) throws NamingException {
        if (!ctxMap.containsKey(name)) {
            throw new NamingException("Name " + name + " not bound!");
        }
        return ctxMap.get(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        return lookup(name.toString());
    }

    public Object lookupLink(String name) throws NamingException {
        throw new UnsupportedOperationException(
                "Not yet implemented in MOCK! - " + name);
    }

    public void rebind(Name name, Object obj) throws NamingException {
        bind(name, obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
        bind(name, obj);
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        throw new UnsupportedOperationException(
                "Not yet implemented in MOCK! - " + propName);
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        rename(oldName.toString(), newName.toString());
    }

    public void rename(String oldName, String newName) throws NamingException {
        throw new UnsupportedOperationException(
                "Not yet implemented in MOCK! - " + oldName + ", " + newName);
    }

    public void unbind(Name name) throws NamingException {
        unbind(name.toString());
    }

    public void unbind(String name) throws NamingException {
        ctxMap.remove(name);
    }

}
