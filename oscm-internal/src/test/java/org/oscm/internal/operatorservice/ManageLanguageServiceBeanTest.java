/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.05.2013
 *  
 *  author cmin
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.operatorservice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.SupportedLanguage;
import org.oscm.operatorservice.bean.OperatorServiceLocalBean;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Unit Test for ManageLanguageServiceBean.
 * 
 * @author cmin
 * 
 */
public class ManageLanguageServiceBeanTest {

    private ManageLanguageServiceBean service = null;
    private OperatorServiceLocalBean operatorServiceLocal = null;
    private DataService ds;
    private final String DEFAULT_LANGUAGE_ISOCODE = "en";
    private PlatformUser currentUser;

    @Before
    public void setup() throws Exception {
        service = spy(new ManageLanguageServiceBean());
        operatorServiceLocal = mock(OperatorServiceLocalBean.class);
        service.operatorService = operatorServiceLocal;
        ds = mock(DataService.class);
        service.ds = ds;

        currentUser = new PlatformUser();
        currentUser.setLocale("en");
        doReturn(currentUser).when(ds).getCurrentUser();
    }

    @Test
    public void getLanguages() throws Exception {
        // given
        List<SupportedLanguage> activeSupportedLanguageList = new ArrayList<SupportedLanguage>();
        activeSupportedLanguageList.add(getSupportedLanguage(1, "en", true,
                true));
        activeSupportedLanguageList.add(getSupportedLanguage(2, "de", true,
                false));
        activeSupportedLanguageList.add(getSupportedLanguage(3, "ja", true,
                false));

        List<SupportedLanguage> supportedLanguageList = new ArrayList<SupportedLanguage>();
        supportedLanguageList.addAll(activeSupportedLanguageList);
        supportedLanguageList.add(getSupportedLanguage(4, "te", false, false));

        when(operatorServiceLocal.getLanguages(false)).thenReturn(
                supportedLanguageList);
        when(operatorServiceLocal.getLanguages(true)).thenReturn(
                activeSupportedLanguageList);

        // when
        List<POSupportedLanguage> poList = service.getLanguages(false);
        List<POSupportedLanguage> activePOList = service.getLanguages(true);

        // then
        assertEquals(poList.size(), supportedLanguageList.size());
        assertEquals(activePOList.size(), activeSupportedLanguageList.size());
    }

    @Test
    public void getDefaultLanguage() throws Exception {
        // given
        when(operatorServiceLocal.getDefaultLanguage()).thenReturn(
                DEFAULT_LANGUAGE_ISOCODE);

        // when
        String isoCode = service.getDefaultLanguage();

        // then
        assertEquals(DEFAULT_LANGUAGE_ISOCODE, isoCode);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getDefaultLanguage_none() throws Exception {
        // given
        when(operatorServiceLocal.getDefaultLanguage()).thenThrow(
                new ObjectNotFoundException());

        // when
        service.getDefaultLanguage();
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getDefaultLanguage_moreThanOne() throws Exception {
        // given
        when(operatorServiceLocal.getDefaultLanguage()).thenThrow(
                new ObjectNotFoundException());

        // when
        service.getDefaultLanguage();
    }

    @Test
    public void saveLangauges() throws Exception {
        // given
        List<POSupportedLanguage> poList = new ArrayList<POSupportedLanguage>();
        poList.add(getPOSupportedLanguage(1, "en", true, true));
        poList.add(getPOSupportedLanguage(2, "de", true, false));
        poList.add(getPOSupportedLanguage(3, "ja", true, false));
        poList.add(getPOSupportedLanguage(4, "te", false, false));

        doNothing().when(operatorServiceLocal).saveLanguages(
                Matchers.anyListOf(SupportedLanguage.class));

        // when
        service.saveLanguages(poList);

        // then
        verify(service.operatorService, times(1)).saveLanguages(
                Matchers.anyListOf(SupportedLanguage.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveLangauges_WithNull() throws Exception {
        // given
        List<POSupportedLanguage> poList = new ArrayList<POSupportedLanguage>();
        poList.add(getPOSupportedLanguage(1, "en", true, true));
        poList.add(getPOSupportedLanguage(2, "de", true, false));
        poList.add(getPOSupportedLanguage(3, "ja", true, false));
        poList.add(null);

        doNothing().when(operatorServiceLocal).saveLanguages(
                Matchers.anyListOf(SupportedLanguage.class));

        // when
        service.saveLanguages(poList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveLangauges_withNull1() throws Exception {
        // given
        List<POSupportedLanguage> poList = new ArrayList<POSupportedLanguage>();
        poList.add(getPOSupportedLanguage(1, "en", true, true));
        poList.add(getPOSupportedLanguage(2, "de", true, false));
        poList.add(getPOSupportedLanguage(3, "ja", true, false));
        poList.add(new POSupportedLanguage());

        doNothing().when(operatorServiceLocal).saveLanguages(
                Matchers.anyListOf(SupportedLanguage.class));

        // when
        service.saveLanguages(poList);
    }

    @Test(expected = ValidationException.class)
    public void saveLangauges_withErrorISOCode() throws Exception {
        // given
        List<POSupportedLanguage> poList = new ArrayList<POSupportedLanguage>();
        poList.add(getPOSupportedLanguage(1, "en", true, true));
        poList.add(getPOSupportedLanguage(2, "de", true, false));
        poList.add(getPOSupportedLanguage(3, "ja", true, false));
        poList.add(getPOSupportedLanguage(4, "tte", false, false));

        doThrow(new ValidationException()).when(operatorServiceLocal)
                .saveLanguages(Matchers.anyListOf(SupportedLanguage.class));

        // when
        service.saveLanguages(poList);
    }

    private SupportedLanguage getSupportedLanguage(long key,
            String languageISOCode, boolean activeStatus, boolean defaultStatus) {
        SupportedLanguage sl = new SupportedLanguage();
        sl.setKey(key);
        sl.setLanguageISOCode(languageISOCode);
        sl.setActiveStatus(activeStatus);
        sl.setDefaultStatus(defaultStatus);
        return sl;
    }

    private POSupportedLanguage getPOSupportedLanguage(long key,
            String languageISOCode, boolean activeStatus, boolean defaultStatus) {
        POSupportedLanguage po = new POSupportedLanguage();
        po.setKey(key);
        po.setLanguageISOCode(languageISOCode);
        po.setActive(activeStatus);
        po.setDefaultLanguageStatus(defaultStatus);
        return po;
    }

}
