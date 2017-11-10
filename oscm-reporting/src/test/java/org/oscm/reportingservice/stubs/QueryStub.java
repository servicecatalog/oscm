/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 19.02.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.stubs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.oscm.domobjects.Report;

/**
 * @author Mike J&auml;ger
 * 
 */
public class QueryStub implements Query {

    private List<Report> reports;

    public QueryStub() {
        reports = new ArrayList<Report>();
    }

    @Override
    public int executeUpdate() {
        return 0;
    }

    @Override
    public List<Report> getResultList() {
        return reports;
    }

    @Override
    public Object getSingleResult() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query setFirstResult(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query setFlushMode(FlushModeType arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query setHint(String arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query setMaxResults(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query setParameter(String arg0, Object arg1) {
        return this;
    }

    @Override
    public Query setParameter(int arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query setParameter(String arg0, Date arg1, TemporalType arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query setParameter(String arg0, Calendar arg1, TemporalType arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query setParameter(int arg0, Date arg1, TemporalType arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query setParameter(int arg0, Calendar arg1, TemporalType arg2) {
        throw new UnsupportedOperationException();
    }

    public void setReports(List<Report> newReports) {
        this.reports = newReports;
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
