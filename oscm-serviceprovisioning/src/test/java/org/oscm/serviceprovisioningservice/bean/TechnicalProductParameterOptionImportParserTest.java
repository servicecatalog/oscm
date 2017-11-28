/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.UpdateConstraintException;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * Unit tests for {@link TechnicalProductParameterOptionImportParser}.
 * 
 * @author barzu
 */
public class TechnicalProductParameterOptionImportParserTest {

    private static final String EN = "en";

    private ParameterDefinition definition;
    private LocalizerServiceLocal localizer;

    @Before
    public void setup() {
        definition = new ParameterDefinition();
        localizer = mock(LocalizerServiceLocal.class);

        Parameter param = new Parameter();
        definition.setParameters(Collections.singletonList(param));
        ParameterSet parameterSet = new ParameterSet();
        param.setParameterSet(parameterSet);
        Product product = new Product();
        product.setStatus(ServiceStatus.ACTIVE);
        parameterSet.setProduct(product);
    }

    private void addParameterOption(long key, String optionId, String locale,
            String text) {
        ParameterOption option = new ParameterOption();
        option.setKey(key);
        option.setOptionId(optionId);
        definition.getOptionList().add(option);

        VOLocalizedText localizedText = new VOLocalizedText();
        localizedText.setLocale(locale);
        localizedText.setText(text);
        List<VOLocalizedText> texts = new ArrayList<VOLocalizedText>();
        texts.add(localizedText);
        doReturn(texts).when(localizer).getLocalizedValues(eq(key),
                eq(LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC));
    }

    @Test
    public void processLocalizedOption() {
        // given
        addParameterOption(1L, "option1", EN, "localizedOption1");
        TechnicalProductParameterOptionImportParser parser = new TechnicalProductParameterOptionImportParser(
                definition, null, null, localizer);
        parser.getOrCreateOption("option1");

        // when
        parser.processLocalizedOption(EN, "newLocalizedOption1");

        // then
        verify(localizer, times(1)).storeLocalizedResource(eq(EN), eq(1L),
                eq(LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC),
                eq("newLocalizedOption1"));
    }

    @Test
    public void getCurrentOptionID_Null() {
        // given
        TechnicalProductParameterOptionImportParser parser = new TechnicalProductParameterOptionImportParser(
                definition, null, null, localizer);

        // when
        String optionId = parser.getCurrentOptionID();

        // then
        assertNull(optionId);
    }

    @Test
    public void getCurrentOptionID() {
        // given
        addParameterOption(1L, "option1", EN, "localizedOption1");
        TechnicalProductParameterOptionImportParser parser = new TechnicalProductParameterOptionImportParser(
                definition, null, null, localizer);
        parser.getOrCreateOption("option1");

        // when
        String optionId = parser.getCurrentOptionID();

        // then
        assertEquals("option1", optionId);
    }

    @Test
    public void cleanupObsoleteOptionDescriptions() throws Exception {
        // given
        addParameterOption(1L, "option1", EN, "localizedOption1");
        TechnicalProductParameterOptionImportParser parser = new TechnicalProductParameterOptionImportParser(
                definition, null, null, localizer);
        parser.getOrCreateOption("option1");
        definition.getParameters().get(0).getParameterSet().getProduct()
                .setStatus(ServiceStatus.DELETED);

        // when
        parser.cleanupObsoleteOptionDescriptions();

        // then
        verify(localizer, times(1)).removeLocalizedValue(eq(1L),
                eq(LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC), eq(EN));
    }

    @Test(expected = UpdateConstraintException.class)
    public void cleanupObsoleteOptionDescriptions_NotDeletedProduct()
            throws Exception {
        // given
        addParameterOption(1L, "option1", EN, "localizedOption1");
        TechnicalProductParameterOptionImportParser parser = new TechnicalProductParameterOptionImportParser(
                definition, null, null, localizer);
        parser.getOrCreateOption("option1");

        // when
        parser.cleanupObsoleteOptionDescriptions();
    }

    @Test(expected = UpdateConstraintException.class)
    public void finishOptions_NotDeletedProduct() throws Exception {
        // given
        addParameterOption(1L, "option1", EN, "localizedOption1");
        TechnicalProductParameterOptionImportParser parser = new TechnicalProductParameterOptionImportParser(
                definition, null, null, localizer);

        // when
        parser.finishOptions();
    }
}
