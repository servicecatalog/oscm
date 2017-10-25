/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 23.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.calendar;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author roderus
 * 
 */
public class GregorianCalendars {
    /**
     * 
     * @return An object of type XMLGregorianCalendar containing the current
     *         system time and date
     * @throws DatatypeConfigurationException
     *             If the instantiation of the DatatypeFactory fails
     */
    public static XMLGregorianCalendar newXMLGregorianCalendarSystemTime()
            throws DatatypeConfigurationException {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar now = datatypeFactory
                .newXMLGregorianCalendar(gregorianCalendar);
        return now;
    }
}
