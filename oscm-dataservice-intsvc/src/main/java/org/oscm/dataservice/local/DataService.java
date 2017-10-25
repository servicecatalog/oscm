/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                            
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.local;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * DataManager provides functionality for persisting, updating and removing
 * domain objects. It shall be used instead of direct use of EntityManager - all
 * public methods of EntityManager are offered as Delegates in DataManager.
 * Additionally a persist, save and remove method exist with a DomainObject as
 * parameter; these implementations automatically create history entries used
 * for auditing purposes.
 * 
 * @author schmid
 * 
 */
@Local
public interface DataService {

    /**
     * Finds a DomainObject by it's artificial key .
     * 
     * @param objclazz
     *            Class of the expected result
     * @param id
     *            PrimaryKey value
     * @return the DomainObject if found, null if not found or found object is
     *         not of type DomainObject
     */
    public <T extends DomainObject<?>> T find(Class<T> objclazz, long id);

    /**
     * Works similarly as the find method of EntityManager, but works on
     * DomainObjects and uses business keys instead of (artificial) primary
     * keys. It uses a NamedQuery which must named as
     * "<DomClass>.findByBusinessKey". The field of the Business Keys are
     * defined by the BusinessKey-Annotation; only these field will be read from
     * the passed DomainObject and used as parameters for the query.
     * 
     * @param idobj
     *            Domain object holding the search criteria
     * @return the found DomainObject
     */
    public DomainObject<?> find(DomainObject<?> idobj);

    /**
     * Works similarly as the #{find} method but is dedicated for PlatformUsers only.
     * It uses findByBusinessKey method if tenantIs set on user and findByUserId if
     * tenant is null or equals to default tenant in the system.
     *
     * @param pu
     *            PlatformUser object holding the search criteria
     * @return the found DomainObject
     */
    PlatformUser find(PlatformUser pu);

    /**
     * Persists a domain object and automatically creates a corresponding
     * history entry. Additionally it is checked whether an object with the same
     * business key already exists in the database.
     * 
     * @param obj
     *            domain object
     * @throws NonUniqueBusinessKeyException
     *             Thrown if an object with the same business key already exists
     *             in the database
     */
    public void persist(DomainObject<?> obj)
            throws NonUniqueBusinessKeyException;

    /**
     * Note: This method is intended for internal usage only.
     * 
     * Persists a domain history object in the database.
     * 
     * @param obj
     *            domain history object
     * @throws NonUniqueBusinessKeyException
     *             Thrown if an object with the same business key already exists
     *             in the database
     */
    public void persist(DomainHistoryObject<?> obj)
            throws NonUniqueBusinessKeyException;

    /**
     * Removes a domain object and automatically creates a corresponding history
     * entry
     * 
     * @param obj
     *            domain object
     */
    public void remove(DomainObject<?> obj);

    /**
     * Replacing the EntityManagers find method, returning a DomainObject
     * 
     * @param clazz
     *            Class of the object to be found
     * @param key
     * @return
     */
    public <T extends DomainObject<?>> T find(Class<T> clazz, Object key);

    /**
     * Load history objects for a given Domain Object
     * 
     * @param obj
     *            DomainObject holding at least the artificial Id
     * @return Arraylist of found history objects, sorted by modification date
     *         (oldest first). Returns <code>null</code> if the input parameter
     *         is <code>null</code>.
     */
    public List<DomainHistoryObject<?>> findHistory(DomainObject<?> obj);

    /**
     * Load the last history object for a given Domain Object
     * 
     * @param obj
     *            DomainObject holding at least the artificial Id
     * @return the last created HistoryObject
     */
    public DomainHistoryObject<?> findLastHistory(DomainObject<?> obj);

    /**
     * Returns a reference to the used EntityManager
     * 
     * @return EntityManager
     */
    public Session getSession();

    /**
     * Works like the {@link #find(DomainObject)} method and returns the domain
     * object that matches the business key of the given domain object search
     * template.
     * 
     * <p>
     * <b>NOTE:</b> This method never returns null bu t will throw an exception
     * of type {@link ObjectNotFoundException} in case no corresponding entry is
     * found.
     * </p>
     * 
     * @param findTemplate
     *            The domain object template which must have the business key
     *            set.
     * @return The found domain object matching the business key.
     * @throws ObjectNotFoundException
     */
    public DomainObject<?> getReferenceByBusinessKey(
            DomainObject<?> findTemplate) throws ObjectNotFoundException;

    /**
     * Finds a DomainObject by it's artificial key. If the object cannot be
     * found, an ObjectNotFound will be thrown.
     * 
     * @param objclass
     *            Class of the expected result
     * @param key
     *            PrimaryKey value
     * @return the DomainObject if found
     * @throws ObjectNotFoundException
     *             Thrown in case the object does not exist.
     */
    public <T extends DomainObject<?>> T getReference(Class<T> objclass,
            long key) throws ObjectNotFoundException;

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager
     */
    public boolean contains(Object arg0);

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager
     */
    public Query createQuery(String jpql);

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager
     */
    public Query createNamedQuery(String jpql);

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager
     */
    public <T> TypedQuery<T> createNamedQuery(String jpql, Class<T> resultClass);

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager
     */
    public Query createNativeQuery(String arg0);

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager
     */
    public Query createNativeQuery(String arg0, Class<?> objclass);

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager
     */
    public void clear();

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager
     */
    public void flush();

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager
     */
    public void refresh(Object arg0);

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager
     */
    public Object merge(Object arg0);

    /**
     * Verify that the the new business key of the object doesn't already
     * exists.
     * 
     * @param obj
     *            the domain object to check
     * 
     * @throws NonUniqueBusinessKeyException
     *             if the new business key already exists.
     * 
     */
    public void validateBusinessKeyUniqueness(DomainObject<?> obj)
            throws NonUniqueBusinessKeyException;

    /**
     * Returns the platform user that corresponds to the user that is the
     * principal of the current session.
     * 
     * <p>
     * <b>NOTE:</b> If the platform user cannot be found, the session has to be
     * considered invalid and an {@link InvalidUserSession} will be thrown.
     * 
     * @return The platform users owning this session.
     */
    public PlatformUser getCurrentUser();

    /**
     * Sets explicitly the platform user. Needed for backend tasks like
     * triggers.
     * 
     * @param id
     *            the user id, e.g. in case of triggers the
     *            TriggerProcess.getUser().getKey()
     */
    public void setCurrentUserKey(Long key);

    /**
     * Tries to return the platform user that corresponds to the user that is
     * the principal of the current session.
     * 
     * <p>
     * <b>NOTE:</b> If the platform user cannot be found, <code>null</code> will
     * be returned.
     * 
     * @return The platform users owning this session or <code>null</code> of no
     *         user is logged in
     */
    public PlatformUser getCurrentUserIfPresent();

    /**
     * Executes a SQL Query and return the query results as a List.
     * 
     * @param sqlQuery
     *            query instance
     * @return Data set. A technology independent abstraction of the SQL
     *         ResultSet with similar API.
     */
    public abstract DataSet executeQueryForRawData(SqlQuery sqlQuery);

    /**
     * Returns the raw entity manager
     * 
     * @return the entity manager
     */
    public EntityManager getEntityManager();

    /**
     * Note: This method is intended for internal usage only.
     *
     * Persists a PlatformUser object in the database. It should be only used while persisting with tenant.
     * If you are not 100% sure that you need to use that, go with the {@link #persist(DomainObject)} instead.
     *
     * @param pu
     *            platform user object
     * @param tenantId tenant id to which user will belong
     * @throws NonUniqueBusinessKeyException
     *             Thrown if an object with the same business key already exists
     *             in the database
     */
    void persistPlatformUserWithTenant(PlatformUser pu, String tenantId) throws NonUniqueBusinessKeyException;
}
