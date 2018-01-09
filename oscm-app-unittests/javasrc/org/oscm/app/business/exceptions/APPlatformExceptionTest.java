/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Sep 17, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.business.exceptions;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.exceptions.APPlatformException;

/**
 * @author Dirk Bernsau
 * 
 */
public class APPlatformExceptionTest {

    @Test
    public void testLocales() {
        List<LocalizedText> messages = new ArrayList<LocalizedText>();
        messages.add(new LocalizedText("de", "Deutsch"));
        messages.add(new LocalizedText("en", "English"));
        APPlatformException ex = new APPlatformException(messages);
        assertEquals("Deutsch", ex.getLocalizedMessage("de"));
        assertEquals("English", ex.getLocalizedMessage("en"));
        assertEquals("English", ex.getLocalizedMessage("fr"));

        messages = new ArrayList<LocalizedText>();
        messages.add(new LocalizedText("de", "Deutsch"));
        messages.add(new LocalizedText("it", "italiano"));
        ex = new APPlatformException(messages);
        assertEquals("Deutsch", ex.getLocalizedMessage("de"));
        assertEquals("Deutsch", ex.getLocalizedMessage("fr"));
        assertEquals("italiano", ex.getLocalizedMessage("it"));

    }

}
