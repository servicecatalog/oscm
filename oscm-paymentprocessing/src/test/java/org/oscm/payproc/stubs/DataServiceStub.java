/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 08.01.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.payproc.stubs;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;
import org.oscm.domobjects.*;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author Mike J&auml;ger
 * 
 */
public abstract class DataServiceStub implements DataService {

    private Object savedObject;
    private Object persistedObject;
    public QueryStub query;

    private PaymentInfo paymentInfo;
    private boolean failForPaymentRetrieval = false;
    private String pspPaymentTypeId = "CC";
    private boolean paymentInfoWrongUser = false;
    private PSP psp;
    private PSPAccount pspAccount;
    private PaymentType paymentType;

    @Override
    public boolean contains(Object arg0) {
        return false;
    }

    @Override
    public Query createNamedQuery(String arg0) {
        query.queryString = arg0;
        return query;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> TypedQuery<T> createNamedQuery(String arg0, Class<T> resultClass) {
        query.queryString = arg0;
        return (TypedQuery) query;
    }

    @Override
    public Query createQuery(String arg0) {
        return null;
    }

    @Override
    public Query createNativeQuery(String arg0) {
        return null;
    }

    @Override
    public Query createNativeQuery(String arg0, Class<?> objclass) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainObject<?>> T find(Class<T> objclazz, long id) {
        if (objclazz.equals(PaymentType.class)) {
            return (T) paymentInfo.getPaymentType();
        }
        return null;
    }

    @Override
    public DomainObject<?> find(DomainObject<?> idobj) {
        return null;
    }

    @Override
    public <T extends DomainObject<?>> T find(Class<T> clazz, Object key) {
        return null;
    }

    @Override
    public List<DomainHistoryObject<?>> findHistory(DomainObject<?> obj) {
        return null;
    }

    @Override
    public DomainHistoryObject<?> findLastHistory(DomainObject<?> obj) {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public void flush() {

    }

    @Override
    public Session getSession() {
        return null;
    }

    @Override
    public Object merge(Object arg0) {
        return null;
    }

    @Override
    public <T extends DomainObject<?>> T getReference(Class<T> objclass, long id)
            throws ObjectNotFoundException {
        if (objclass == PaymentInfo.class) {
            if (failForPaymentRetrieval) {
                throw new ObjectNotFoundException(ClassEnum.PAYMENT_INFO,
                        "caused by test");
            }
            if (paymentInfoWrongUser) {
                Organization newOrg = new Organization();
                newOrg.setOrganizationId("anotherBadCompany");
                paymentInfo.setOrganization(newOrg);
            } else {
                paymentInfo.setOrganization(query.org);
            }
            return objclass.cast(paymentInfo);
        } else if (objclass == PaymentType.class) {
            if (paymentInfo != null && paymentInfo.getPaymentType() != null) {
                return objclass.cast(paymentInfo.getPaymentType());
            }
            return objclass.cast(getPaymentType());
        }

        return null;
    }

    @Override
    public DomainObject<?> getReferenceByBusinessKey(
            DomainObject<?> findTemplate) throws ObjectNotFoundException {
        PaymentType paymentType = getPaymentType();

        paymentInfo.setOrganization(query.org);
        if (findTemplate instanceof PaymentType) {
            if (this.paymentType != null) {
                return this.paymentType;
            }
            return paymentType;
        }
        if (paymentInfo.getKey() == -1) {
            return null;
        }
        return paymentInfo;
    }

    protected PaymentType getPaymentType() {
        PaymentType paymentType = new PaymentType();
        paymentType
                .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        if ("CC".equals(pspPaymentTypeId)) {
            paymentType.setPaymentTypeId("CREDIT_CARD");
            paymentType.setKey(1);
            paymentType.setPsp(psp);
        } else if ("DD".equals(pspPaymentTypeId)) {
            paymentType.setPaymentTypeId("DIRECT_DEBIT");
            paymentType.setKey(2);
            paymentType.setPsp(psp);
        } else {
            paymentType.setPaymentTypeId("INVOICE");
            paymentType.setKey(3);
            paymentType.setCollectionType(PaymentCollectionType.ORGANIZATION);
        }
        return paymentType;
    }

    @Override
    public void persist(DomainObject<?> obj)
            throws NonUniqueBusinessKeyException {
        persistedObject = obj;
    }

    @Override
    public void persist(DomainHistoryObject<?> obj)
            throws NonUniqueBusinessKeyException {
        persistedObject = obj;
    }

    @Override
    public void refresh(Object arg0) {
    }

    @Override
    public void remove(DomainObject<?> obj) {
    }

    @Override
    public void validateBusinessKeyUniqueness(DomainObject<?> obj)
            throws NonUniqueBusinessKeyException {

    }

    public Object getSavedObject() {
        return savedObject;
    }

    public Object getPersistedObject() {
        return persistedObject;
    }

    public void setPaymentInfo(PaymentInfo pi) {
        this.paymentInfo = pi;
    }

    public void setFailForPaymentRetrieval(boolean failForPaymentRetrieval) {
        this.failForPaymentRetrieval = failForPaymentRetrieval;
    }

    public void setPSPPaymentId(String pspPaymentTypeId) {
        this.pspPaymentTypeId = pspPaymentTypeId;
    }

    public void setWrongPaymentUser(boolean paymentInfoWrongUser) {
        this.paymentInfoWrongUser = paymentInfoWrongUser;
    }

    @Override
    public PlatformUser getCurrentUserIfPresent() {
        return getCurrentUser();
    }

    public PSP getPsp() {
        return psp;
    }

    public void setPsp(PSP psp) {
        this.psp = psp;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public PSPAccount getPspAccount() {
        return pspAccount;
    }

    public void setPspAccount(PSPAccount pspAccount) {
        this.pspAccount = pspAccount;
    }

    @Override
    public DataSet executeQueryForRawData(SqlQuery sqlQuery) {
        return null;
    }

    @Override
    public void setCurrentUserKey(Long key) {
    }

    @Override
    public EntityManager getEntityManager() {
        return null;
    }

    @Override
    public PlatformUser find(PlatformUser pu) {
        return null;
    }

    @Override
    public void persistPlatformUserWithTenant(PlatformUser pu, String tenantId) throws NonUniqueBusinessKeyException {

    }
}
