/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 16.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.stubs;

import java.io.Serializable;
import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

public class ObjectMessageStub implements ObjectMessage {

    private Serializable storedObject;

    @Override
    public Serializable getObject() throws JMSException {
        return storedObject;
    }

    @Override
    public void setObject(Serializable arg0) throws JMSException {
        storedObject = arg0;
    }

    @Override
    public void acknowledge() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearBody() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getBody(Class<T> aClass) throws JMSException {
        return null;
    }

    @Override
    public boolean isBodyAssignableTo(Class aClass) throws JMSException {
        return false;
    }

    @Override
    public void clearProperties() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBooleanProperty(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByteProperty(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDoubleProperty(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloatProperty(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIntProperty(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getJMSExpiration() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getJMSMessageID() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getJMSPriority() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getJMSType() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLongProperty(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObjectProperty(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Enumeration getPropertyNames() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShortProperty(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStringProperty(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean propertyExists(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBooleanProperty(String arg0, boolean arg1)
            throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByteProperty(String arg0, byte arg1) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDoubleProperty(String arg0, double arg1) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFloatProperty(String arg0, float arg1) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIntProperty(String arg0, int arg1) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSCorrelationID(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSDeliveryMode(int arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSDestination(Destination arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSExpiration(long arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getJMSDeliveryTime() throws JMSException {
        return 0;
    }

    @Override
    public void setJMSDeliveryTime(long l) throws JMSException {

    }

    @Override
    public void setJMSMessageID(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSPriority(int arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSRedelivered(boolean arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSReplyTo(Destination arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSTimestamp(long arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSType(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLongProperty(String arg0, long arg1) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObjectProperty(String arg0, Object arg1) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShortProperty(String arg0, short arg1) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStringProperty(String arg0, String arg1) throws JMSException {
        throw new UnsupportedOperationException();
    }

}
