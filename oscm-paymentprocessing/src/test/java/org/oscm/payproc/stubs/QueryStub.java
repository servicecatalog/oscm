/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 20.01.2010                                                      
 *                                                                              
 *  Completion Time: 20.01.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.payproc.stubs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.oscm.domobjects.BillingContactHistory;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.PSP;
import org.oscm.domobjects.PSPAccount;
import org.oscm.domobjects.PSPAccountHistory;
import org.oscm.domobjects.PSPHistory;
import org.oscm.domobjects.PSPSetting;
import org.oscm.domobjects.PSPSettingHistory;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentInfoHistory;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PaymentTypeHistory;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductHistory;

/**
 * Test stub for the Query object.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class QueryStub implements Query {

    String queryString;

    public Organization org;
    public Organization supplier;

    private PaymentInfo paymentInfo;
    private PaymentType paymentType;
    private PSPAccount pspAccount;
    private Product product;
    private List<PSPSetting> pspSettings = new ArrayList<PSPSetting>();
    private PSP psp;
    private BillingContactHistory billingContactHistory;

    private List<?> queryResultList;
    private Object parameter;

    private boolean returnOrganizationHistoryEntries = true;
    private boolean returnPaymentInfoHistoryEntries = true;

    @Override
    public int executeUpdate() {
        return 0;
    }

    @Override
    public List<?> getResultList() {
        List<OrganizationHistory> orgData = new ArrayList<OrganizationHistory>();
        if (returnOrganizationHistoryEntries) {
            if (parameter.equals(Long.valueOf(supplier.getKey()))) {
                orgData.add(new OrganizationHistory(supplier));
            } else {
                orgData.add(new OrganizationHistory(org));
            }
        }

        Object[] data = new Object[4];
        if (returnPaymentInfoHistoryEntries) {
            if (paymentInfo != null) {
                data[0] = new PaymentInfoHistory(paymentInfo);
            } else {
                data[0] = new PaymentInfoHistory();
            }
            if (paymentType != null) {
                data[1] = new PaymentTypeHistory(paymentType);
            } else {
                data[1] = new PaymentTypeHistory();
            }
            if (psp != null) {
                data[2] = new PSPHistory(psp);
            } else {
                data[2] = new PSPHistory();
            }
            if (product != null) {
                data[3] = new ProductHistory(product);
            } else {
                data[3] = new ProductHistory();
            }
        }

        if (queryString.startsWith("PaymentInfoHistory")) {
            List<Object[]> dataList = new ArrayList<Object[]>();
            dataList.add(data);
            return dataList;
        }

        if (queryString.startsWith("PaymentResult")) {
            return queryResultList;
        }

        if (queryString.startsWith("BillingContactHistory")) {
            ArrayList<BillingContactHistory> contacts = new ArrayList<BillingContactHistory>();
            if (billingContactHistory != null) {
                contacts.add(billingContactHistory);
            }
            return contacts;
        }

        if (queryString.startsWith("PSPSettingHistory")) {
            List<PSPSettingHistory> pspHistory = new ArrayList<PSPSettingHistory>();
            for (PSPSetting s : pspSettings) {
                pspHistory.add(new PSPSettingHistory(s));
            }
            return pspHistory;
        }

        if (parameter instanceof Long) {
            Long value = (Long) parameter;
            if (value.longValue() == -1L) {
                return Collections.EMPTY_LIST;
            }
        }

        return orgData;
    }

    @Override
    public Object getSingleResult() {
        if (queryString.equals("PaymentInfoHistory.findPSPAccount")) {
            if (pspAccount == null) {
                throw new NoResultException();
            }
            return new PSPAccountHistory(pspAccount);
        }
        return null;
    }

    @Override
    public Query setFirstResult(int arg0) {
        return null;
    }

    @Override
    public Query setFlushMode(FlushModeType arg0) {
        return null;
    }

    @Override
    public Query setHint(String arg0, Object arg1) {
        return null;
    }

    @Override
    public Query setMaxResults(int arg0) {
        return null;
    }

    @Override
    public Query setParameter(String arg0, Object arg1) {
        parameter = arg1;
        return null;
    }

    @Override
    public Query setParameter(int arg0, Object arg1) {
        return null;
    }

    @Override
    public Query setParameter(String arg0, Date arg1, TemporalType arg2) {
        return null;
    }

    @Override
    public Query setParameter(String arg0, Calendar arg1, TemporalType arg2) {
        return null;
    }

    @Override
    public Query setParameter(int arg0, Date arg1, TemporalType arg2) {
        return null;
    }

    @Override
    public Query setParameter(int arg0, Calendar arg1, TemporalType arg2) {
        return null;
    }

    public List<?> getQueryResultList() {
        return queryResultList;
    }

    public void setQueryResultList(List<?> resultList) {
        this.queryResultList = resultList;
    }

    public void setBillingContactHistory(BillingContactHistory billingContact) {
        this.billingContactHistory = billingContact;
    }

    public void setReturnOrganizationHistoryEntries(
            boolean returnOrganizationHistoryEntries) {
        this.returnOrganizationHistoryEntries = returnOrganizationHistoryEntries;
    }

    public void setReturnPaymentInfoHistoryEntries(
            boolean returnPaymentInfoHistoryEntries) {
        this.returnPaymentInfoHistoryEntries = returnPaymentInfoHistoryEntries;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public PSP getPsp() {
        return psp;
    }

    public void setPsp(PSP psp) {
        this.psp = psp;
    }

    public List<PSPSetting> getPspSettings() {
        return pspSettings;
    }

    public void setPspSettings(List<PSPSetting> pspSettings) {
        this.pspSettings = pspSettings;
    }

    public PSPAccount getPspAccount() {
        return pspAccount;
    }

    public void setPspAccount(PSPAccount pspAccount) {
        this.pspAccount = pspAccount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getFirstResult()
     */
    @Override
    public int getFirstResult() {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getFlushMode()
     */
    @Override
    public FlushModeType getFlushMode() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getHints()
     */
    @Override
    public Map<String, Object> getHints() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getLockMode()
     */
    @Override
    public LockModeType getLockMode() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getMaxResults()
     */
    @Override
    public int getMaxResults() {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getParameter(java.lang.String)
     */
    @Override
    public Parameter<?> getParameter(String arg0) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getParameter(int)
     */
    @Override
    public Parameter<?> getParameter(int arg0) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getParameter(java.lang.String,
     * java.lang.Class)
     */
    @Override
    public <T> Parameter<T> getParameter(String arg0, Class<T> arg1) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getParameter(int, java.lang.Class)
     */
    @Override
    public <T> Parameter<T> getParameter(int arg0, Class<T> arg1) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.persistence.Query#getParameterValue(javax.persistence.Parameter)
     */
    @Override
    public <T> T getParameterValue(Parameter<T> arg0) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getParameterValue(java.lang.String)
     */
    @Override
    public Object getParameterValue(String arg0) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getParameterValue(int)
     */
    @Override
    public Object getParameterValue(int arg0) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#getParameters()
     */
    @Override
    public Set<Parameter<?>> getParameters() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#isBound(javax.persistence.Parameter)
     */
    @Override
    public boolean isBound(Parameter<?> arg0) {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#setLockMode(javax.persistence.LockModeType)
     */
    @Override
    public Query setLockMode(LockModeType arg0) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#setParameter(javax.persistence.Parameter,
     * java.lang.Object)
     */
    @Override
    public <T> Query setParameter(Parameter<T> arg0, T arg1) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#setParameter(javax.persistence.Parameter,
     * java.util.Calendar, javax.persistence.TemporalType)
     */
    @Override
    public Query setParameter(Parameter<Calendar> arg0, Calendar arg1,
            TemporalType arg2) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#setParameter(javax.persistence.Parameter,
     * java.util.Date, javax.persistence.TemporalType)
     */
    @Override
    public Query setParameter(Parameter<Date> arg0, Date arg1, TemporalType arg2) {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.Query#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap(Class<T> arg0) {

        return null;
    }

}
