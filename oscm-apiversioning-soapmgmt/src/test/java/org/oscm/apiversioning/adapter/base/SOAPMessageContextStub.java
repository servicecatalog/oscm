/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015年2月9日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.adapter.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * @author gaowenxin
 * 
 */
public class SOAPMessageContextStub implements SOAPMessageContext {

    private SOAPMessage message;

    private Map<Object, Object> maps = new HashMap<Object, Object>();

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.handler.MessageContext#getScope(java.lang.String)
     */
    @Override
    public Scope getScope(String arg0) {
        return null;
    }

    /**
     * @return the maps
     */
    public Map<Object, Object> getMaps() {
        return maps;
    }

    /**
     * @param maps
     *            the maps to set
     */
    public void setMaps(Map<Object, Object> maps) {
        this.maps = maps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.handler.MessageContext#setScope(java.lang.String,
     * javax.xml.ws.handler.MessageContext.Scope)
     */
    @Override
    public void setScope(String arg0, Scope arg1) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {

        // TODO Auto-generated method stub
        return this.getMaps().get(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#values()
     */
    @Override
    public Collection<Object> values() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.xml.ws.handler.soap.SOAPMessageContext#getHeaders(javax.xml.namespace
     * .QName, javax.xml.bind.JAXBContext, boolean)
     */
    @Override
    public Object[] getHeaders(QName arg0, JAXBContext arg1, boolean arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SOAPMessage getMessage() {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.handler.soap.SOAPMessageContext#getRoles()
     */
    @Override
    public Set<String> getRoles() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.xml.ws.handler.soap.SOAPMessageContext#setMessage(javax.xml.soap
     * .SOAPMessage)
     */
    @Override
    public void setMessage(SOAPMessage arg0) {
        message = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(String key, Object value) {
        this.getMaps().put(key, value);
        // TODO Auto-generated method stub
        return null;
    }

}
