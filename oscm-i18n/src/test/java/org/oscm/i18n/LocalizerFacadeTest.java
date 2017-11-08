/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.03.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.LocalizedObjectTypes.InformationSource;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.stubs.LocalizerServiceStub;

public class LocalizerFacadeTest {

    private LocalizerServiceStub localizer;
    private LocalizerFacade localizerFacade;

    @Before
    public void setUp() {
        localizer = new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                if (objectType.getSource() == InformationSource.DATABASE_AND_RESOURCE_BUNDLE) {
                    return null;
                }
                return "localizedText";
            }

            @Override
            public String getLocalizedTextFromBundle(
                    LocalizedObjectTypes objectType, Marketplace shop,
                    String localeString, String key) {
                return "localizedTextFromBundle";
            }
        };
        localizerFacade = new LocalizerFacade(localizer, "locale");
    }

    @Test
    public void testCreate() {
        LocalizerFacade lf = new LocalizerFacade(localizer, "locale");
        assertNotNull(lf);
    }

    @Test
    public void testGetTextFromNonDBResource() {
        String text = localizerFacade.getText(1L,
                LocalizedObjectTypes.MAIL_CONTENT);
        assertNull("No text must be retrieved", text);
    }

    @Test
    public void testGetTextFromDB() {
        String text = localizerFacade.getText(1L,
                LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
        assertEquals("Wrong localized text retrieved", "localizedText", text);
    }

    @Test
    public void getText() {
        String text = localizerFacade.getText(1L,
                LocalizedObjectTypes.EVENT_DESC);
        assertEquals("localizedTextFromBundle", text);
    }
}
