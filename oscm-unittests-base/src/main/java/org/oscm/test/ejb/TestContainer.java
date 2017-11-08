/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import static org.mockito.Mockito.mock;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.xml.ws.WebServiceContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.oscm.test.cdi.ContextManager;
import org.oscm.test.cdi.TestEvent;

/**
 * Simple container for session bean testing. Use the add methods to add
 * resources and beans that should be tested or are required as dependencies.
 * 
 * @author hoffmann
 */
public class TestContainer {

    public final TestPersistence persistence;

    private final InterfaceMap<DeployedSessionBean> sessionBeans = new InterfaceMap<DeployedSessionBean>();

    private final TestSessionContext sessionContext;
    private final TestTimerService timerService;

    private Map<Class<?>, Object> resources = new HashMap<Class<?>, Object>();

    private boolean infaceMockingEnabled = false;

    private HashSet<Object> dependenciesProcessed = new HashSet<Object>();

    private ContextManager contextManager;

    public TestContainer(TestPersistence persistence) throws Exception {
        this.persistence = persistence;
        this.sessionContext = new TestSessionContext(
                persistence.getTransactionManager(), sessionBeans);
        this.timerService = new TestTimerService();

        resources.put(SessionContext.class, sessionContext);
        resources.put(TimerService.class, timerService);
        resources.put(ConnectionFactory.class, new TestJMSConnectionFactory());
        resources.put(Queue.class, TestJMSQueue.getInstance());
        resources.put(WebServiceContext.class, new TestWebServiceContext(
                sessionContext));
        contextManager = new ContextManager(this);
        addBean(new TestEvent(contextManager));
    }

    public void login(long userkey, String... roles) {
        login(String.valueOf(userkey), roles);
    }

    public void login(String username, String... roles) {
        sessionContext.setUsername(username);
        sessionContext.setRoles(roles);
    }

    public void logout() {
        sessionContext.setUsername(null);
        sessionContext.setRoles(new String[0]);
    }

    public void addBean(Object bean) throws Exception {
        if (!infaceMockingEnabled) {
            injectDependencies(bean);
        }
        sessionBeans.put(bean,
                new DeployedSessionBean(persistence.getTransactionManager(),
                        sessionContext, bean));
        contextManager.scanMethods(bean);
    }

    public <T> T get(Class<T> type) {
        T result = null;
        try {
            DeployedSessionBean dsb = sessionBeans.get(type);
            if (!dependenciesProcessed.contains(dsb.getBean())) {
                try {
                    injectDependencies(dsb.getBean());
                } catch (Exception e) {
                    throw new RuntimeException("Dependency injection for "
                            + dsb.getBean() + " failed", e);
                }
            }
            result = dsb.getInterfaceOrClass(type);
            callPostConstruct(dsb.getBean());
        } catch (NoSuchElementException e) {
            if (infaceMockingEnabled) {
                return mock(type);
            }
            throw e;
        }
        return result;
    }

    private void callPostConstruct(Object element) {
        Method[] methods = element.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            PostConstruct annotation = m.getAnnotation(PostConstruct.class);
            if (annotation != null) {
                if (m.getParameterTypes().length == 0
                        && Modifier.isPublic(m.getModifiers())
                        && !Modifier.isStatic(m.getModifiers())) {
                    try {
                        m.invoke(element, new Object[0]);
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Could not call @PostConstruct method", e);
                    }
                }
            }
        }
    }

    private void injectDependencies(Object bean) throws Exception {
        dependenciesProcessed.add(bean);
        for (Field f : getAllFields(bean.getClass())) {
            EJB ejb = f.getAnnotation(EJB.class);
            if (ejb != null) {
                if (java.lang.Object.class.equals(ejb.beanInterface())) {
                    Reference ref = Reference.createFor(f);
                    ref.inject(bean, get(ref.getInterfaceOrClass()));
                } else {
                    Reference ref = Reference.createFor(ejb, f);
                    ref.inject(bean, get(ref.getInterfaceOrClass()));
                }

            }
            Resource resource = f.getAnnotation(Resource.class);

            PersistenceContext persistenceContext = f
                    .getAnnotation(PersistenceContext.class);

            if (resource != null) {
                Reference ref = Reference.createFor(resource, f);

                Object res = resources.get(ref.getInterfaceOrClass());

                if (res == null && resource.name().equalsIgnoreCase("BSSDS")) {
                    if (persistenceContext != null) {
                        res = persistence
                                .getDataSourceByName(persistenceContext
                                        .unitName());
                    } else {
                        res = persistence
                                .getDataSourceByName("oscm-domainobjects");
                    }
                }
                if (res == null) {
                    throw new RuntimeException("Unsupported Resource Type: "
                            + ref.getInterfaceOrClass());
                }
                ref.inject(bean, res);
            }

            if (persistenceContext != null) {
                Reference ref = Reference.createFor(persistenceContext, f);
                ref.inject(bean,
                        createLazyEntityManager(persistence
                                .getEntityManagerFactory(persistenceContext
                                        .unitName())));
            }

            Inject inject = f.getAnnotation(Inject.class);
            if (inject != null) {
                Reference ref = Reference.createFor(f);
                ref.inject(bean, get(ref.getInterfaceOrClass()));
            }
        }
    }

    private List<Field> getAllFields(Class<?> type) {
        final List<Field> fields = new ArrayList<Field>();
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        final Class<?> superType = type.getSuperclass();
        if (superType != null) {
            fields.addAll(getAllFields(superType));
        }
        return fields;
    }

    public EntityManager getPersistenceUnit(String unitName) throws Exception {
        return createLazyEntityManager(persistence
                .getEntityManagerFactory(unitName));
    }

    private EntityManager createLazyEntityManager(
            final EntityManagerFactory factory) {
        InvocationHandler h = new InvocationHandler() {

            private final Map<Transaction, EntityManager> delegates = new HashMap<Transaction, EntityManager>();

            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                final Transaction tx = persistence.getTransactionManager()
                        .getTransaction();
                if (tx == null) {
                    throw new RuntimeException("No active Transaction");
                }
                EntityManager delegate = delegates.get(tx);
                if (delegate == null) {
                    delegate = factory.createEntityManager();
                    SessionFactory sessionFactory = persistence.getSf();
                    final Session session = sessionFactory.openSession();
                    delegates.put(tx, delegate);
                    tx.registerSynchronization(new Synchronization() {

                        @Override
                        public void beforeCompletion() {
                        }

                        @Override
                        public void afterCompletion(int status) {
                            delegates.remove(tx).close();
                            session.close();
                        }
                    });
                }
                try {
                    return method.invoke(delegate, args);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
        };
        return (EntityManager) Proxy.newProxyInstance(this.getClass()
                .getClassLoader(), new Class[] { EntityManager.class }, h);
    }

    public TestJMSQueue getJMSQueue() {
        return (TestJMSQueue) resources.get(Queue.class);
    }

    public void enableInterfaceMocking(boolean value) {
        infaceMockingEnabled = value;
    }

    public ContextManager getContextManager() {
        return contextManager;
    }
}
