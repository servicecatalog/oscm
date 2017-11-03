/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

public class DataServiceStub implements DataService {

    @Override
    public boolean contains(Object arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String jpql, Class<T> resultClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query createNamedQuery(String jpql) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query createQuery(String jpql) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query createNativeQuery(String sql) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query createNativeQuery(String arg0, Class<?> objclass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends DomainObject<?>> T find(Class<T> objclazz, long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends DomainObject<?>> T find(Class<T> clazz, Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DomainObject<?> find(DomainObject<?> idobj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlatformUser find(PlatformUser pu) {
        return null;
    }

    @Override
    public List<DomainHistoryObject<?>> findHistory(DomainObject<?> obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends DomainObject<?>> T getReference(Class<T> objclass,
            long key) throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DomainObject<?> getReferenceByBusinessKey(
            DomainObject<?> findTemplate) throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void persist(DomainObject<?> obj)
            throws NonUniqueBusinessKeyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(Object arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(DomainObject<?> obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validateBusinessKeyUniqueness(DomainObject<?> obj)
            throws NonUniqueBusinessKeyException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlatformUser getCurrentUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlatformUser getCurrentUserIfPresent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DomainHistoryObject<?> findLastHistory(DomainObject<?> obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataSet executeQueryForRawData(SqlQuery sqlQuery) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCurrentUserKey(Long key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Session getSession() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object merge(Object arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void persist(DomainHistoryObject<?> obj)
            throws NonUniqueBusinessKeyException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityManager getEntityManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void persistPlatformUserWithTenant(PlatformUser pu, String tenantId) throws NonUniqueBusinessKeyException {

    }
}
