/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.Query;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.junit.After;
import org.junit.Before;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.ResourceLoader;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.interceptor.DateFactory;
import org.oscm.test.ejb.TestContainer;

/**
 * Test base for all tests that require a EJB test container.
 * 
 * @author hoffmann
 */
public abstract class EJBTestBase extends BaseAdmUmTest {

    protected TestContainer container;

    private Caller caller;

    private String hsSearchBackup;

    @Before
    public void setup() throws Exception {
        enableJndiMock();
        restoreDateFactory();
        // remember state of hibernate search listener property
        hsSearchBackup = System.getProperty(HS_SEARCH_LISTENERS);
        enableHibernateSearchListeners(false);
        initPersistence();
        container = new TestContainer(PERSISTENCE);
        container.addBean(new TransactionBean());
        caller = container.get(Caller.class);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                setup(container);
                return null;
            }
        });
    }

    /**
     * The class DateFactory is used to create the history modification
     * timestamps. Sometimes test cases redefine the behavior to set artificial
     * data in the history tables. This method restores the normal date factory
     * in case a test case did not clean up properly.
     */
    private void restoreDateFactory() {
        DateFactory.setInstance(new DateFactory());
    }

    protected void initPersistence() throws Exception {
        PERSISTENCE.initialize();
    }

    @After
    public void tearDown() throws Exception {
        if (hsSearchBackup != null) {
            // reset property value
            System.setProperty(HS_SEARCH_LISTENERS, hsSearchBackup);
        }
    }

    protected abstract void setup(TestContainer container) throws Exception;

    protected <T> T runTX(Callable<T> callable) throws Exception {
        DateFactory.getInstance().takeCurrentTime();
        return caller.call(callable);
    }

    protected <T> T unproxyEntity(T template) {
        if (template instanceof HibernateProxy) {
            Hibernate.initialize(template);
            template = (T) ((HibernateProxy) template)
                    .getHibernateLazyInitializer()
                    .getImplementation();
        }
        return template;
    }

    public static interface Caller {
        public <V> V call(Callable<V> callable) throws Exception;
    }

    /**
     * This little bit of magic allows our test code to execute in the scope of
     * a container controlled transaction.
     */
    @Stateless
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private static class TransactionBean implements Caller {
        @Override
        public <V> V call(Callable<V> callable) throws Exception {
            return callable.call();
        }
    }

    /**
     * Returns the history entries for the given domain object.
     * 
     * @param <T>
     * @param obj
     *            The domain object to retrieve the history for.
     * @param clazz
     *            The history object class.
     * @return The history entries.
     * @throws Exception
     */
    protected <T> List<T> getHistory(final DomainObject<?> obj,
            final Class<T> clazz) throws Exception {
        return runTX(new Callable<List<T>>() {
            @Override
            public List<T> call() {
                return ParameterizedTypes.list(container.get(DataService.class)
                        .findHistory(obj), clazz);
            }
        });
    }

    /**
     * Deletes the given domain object.
     * 
     * @param <T>
     * @param obj
     *            The domain object to be deleted.
     * @param clazz
     *            The domain object class.
     * @return The deleted object.
     * @throws Exception
     */
    protected <T extends DomainObject<?>> T removeDomainObject(
            final DomainObject<?> obj, final Class<T> clazz) throws Exception {
        return runTX(new Callable<T>() {
            @Override
            public T call() throws Exception {
                T reference = container.get(DataService.class).getReference(
                        clazz, obj.getKey());
                container.get(DataService.class).remove(reference);
                return reference;
            }
        });
    }

    /**
     * Returns the domain object for the given domain object search template.
     * 
     * @param <T>
     * @param obj
     *            The domain object search template.
     * @param clazz
     *            The class for the domain object.
     * @return The domain object.
     * @throws Exception
     */
    protected <T extends DomainObject<?>> T getDomainObject(
            final DomainObject<?> obj, final Class<T> clazz) throws Exception {
        return runTX(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return container.get(DataService.class).getReference(clazz,
                        obj.getKey());
            }
        });
    }

    /**
     * Reads all currently existing objects of one type from the database.
     * 
     * @param <T>
     *            The type.
     * @param clazz
     *            The type class.
     * @return The list of existing objects.
     * @throws Exception
     */
    protected <T> List<T> getAllPersistedObjectsOfType(final Class<T> clazz)
            throws Exception {
        return runTX(new Callable<List<T>>() {
            @Override
            public List<T> call() {
                String typeName = clazz.getSimpleName();
                String queryString = String.format("SELECT d FROM %s d",
                        typeName);
                Query query = container.get(DataService.class).createQuery(
                        queryString);
                return ParameterizedTypes.list(query.getResultList(), clazz);
            }
        });
    }

    /**
     * Reads the content of a file with the given name.
     * 
     * @param fileName
     *            The path to the file relative to the classpath.
     * @return The file content.
     */
    protected byte[] readBytesFromFile(String fileName) throws IOException {
        try (InputStream is = ResourceLoader.getResourceAsStream(getClass(),
                fileName);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
            byte[] content = new byte[1];
            while (is.read(content) > -1) {
                bos.write(content);
            }
            return bos.toByteArray();
        }
    }

    /**
     * Loads the given object into memory. This is required in case the session
     * is closed, before accessing it.
     */
    protected void load(DomainObject<?> domainObject) {
        if (domainObject instanceof PlatformUser) {
            for (RoleAssignment assignment : ((PlatformUser) domainObject)
                    .getAssignedRoles()) {
                load(assignment.getRole());
            }
        }
        if (domainObject instanceof Organization) {
            for (OrganizationToRole orgToRole : ((Organization) domainObject)
                    .getGrantedRoles()) {
                load(orgToRole.getOrganizationRole());
            }
        }
        domainObject.toString(); // access a method in order to resolve
    }

    /**
     * Loads the given object into memory. This is required in case the session
     * is closed, before accessing it.
     */
    protected void load(List<?> domainObjects) {
        for (Object domainObject : domainObjects) {
            domainObject.toString();
        }
    }

    /**
     * Reloads the given domain object from the database
     */
    protected <T extends DomainObject<?>> T reload(final T domainObject)
            throws Exception {
        return runTX(new Callable<T>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public T call() {
                return (T) container.get(DataService.class).find(
                        (Class) DomainObject.getDomainClass(domainObject),
                        domainObject.getKey());
            }
        });
    }

    /**
     * Reloads the given domain objects from the database
     */
    protected <T extends DomainObject<?>> List<T> reload(
            final List<T> domainObjects) throws Exception {
        return runTX(new Callable<List<T>>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public List<T> call() {
                DataService ds = container.get(DataService.class);
                List<T> result = new ArrayList<T>();
                for (T domainObject : domainObjects) {
                    DomainObject reloaded = ds.find(
                            (Class) DomainObject.getDomainClass(domainObject),
                            domainObject.getKey());
                    if (reloaded != null) {
                        result.add((T) reloaded);
                    }
                }
                return result;
            }
        });
    }
}
