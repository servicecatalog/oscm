/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import static org.oscm.test.Numbers.L0;
import static org.oscm.test.Numbers.L100;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.convert.ConverterException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOService;

public class PricedParameterRowTest {

    private VOService product = new VOService();
    private List<VOParameter> parameterList = new ArrayList<VOParameter>();
    private VOParameter parameter;
    private VOPricedParameter pricedParameter;
    private VOParameter parameterWithOption;

    @Before
    public void setup() {
        for (int i = 0; i < 20; i++) {
            VOParameterDefinition paramDef = new VOParameterDefinition(
                    ParameterType.PLATFORM_PARAMETER, "USER" + i, "Description"
                            + i, ParameterValueType.LONG, "0", L0, L100, false,
                    true, null);
            VOParameter param = new VOParameter(paramDef);
            param.setValue(String.valueOf(i));
            parameterList.add(param);
        }

        parameter = new VOParameter(new VOParameterDefinition(
                ParameterType.SERVICE_PARAMETER, "URL", "URL Description",
                ParameterValueType.STRING, "", null, null, false, true, null));
        parameterList.add(10, parameter);

        String paramDefId = "SIZE";
        List<VOParameterOption> parameterOptionList = new ArrayList<VOParameterOption>();
        for (int i = 0; i < 6; i++) {
            VOParameterOption option = new VOParameterOption(paramDefId + i,
                    paramDefId + " description " + i, paramDefId);
            parameterOptionList.add(option);
        }
        parameterWithOption = new VOParameter(new VOParameterDefinition(
                ParameterType.SERVICE_PARAMETER, paramDefId,
                "Size Description", ParameterValueType.ENUMERATION, "", null,
                null, false, true, parameterOptionList));
        parameterList.add(15, parameterWithOption);

        VOPriceModel priceModel = new VOPriceModel();
        List<VOPricedParameter> selectedParameters = new ArrayList<VOPricedParameter>();
        pricedParameter = new VOPricedParameter(
                parameter.getParameterDefinition());
        pricedParameter.setPricePerUser(BigDecimal.valueOf(100));
        selectedParameters.add(pricedParameter);
        priceModel.setSelectedParameters(selectedParameters);
        product.setPriceModel(priceModel);
        product.setParameters(parameterList);
    }

    @Test
    public void testCreatePricedParameterRowListNull()
            throws ConverterException {
        Assert.assertEquals(0, PricedParameterRow
                .createPricedParameterRowListForPriceModelRoles(null).size());
    }

    @Test
    public void testCreatePricedParameterRowList() throws ConverterException {
        List<PricedParameterRow> selectedParameters;
        selectedParameters = PricedParameterRow
                .createPricedParameterRowListForPriceModel(product);
        Assert.assertEquals(0, selectedParameters.size());

        for (VOParameter param : product.getParameters()) {
            param.setConfigurable(true);
        }

        selectedParameters = PricedParameterRow
                .createPricedParameterRowListForPriceModel(product);
        int size = parameterList.size()
                + parameterWithOption.getParameterDefinition()
                        .getParameterOptions().size();
        size--; // one parameter is of type string, must not be listed
        Assert.assertEquals(size, selectedParameters.size());
    }

    @Test
    public void testCreatePricedParameterRowListCreateMissing()
            throws ConverterException {
        product.getPriceModel().setSelectedParameters(null);

        int size = parameterList.size()
                + parameterWithOption.getParameterDefinition()
                        .getParameterOptions().size();
        size--; // one parameter is of type string, must not be listed

        List<PricedParameterRow> selectedParameters;

        selectedParameters = PricedParameterRow.createPricedParameterRowList(
                product, false, true, false, true, true);
        Assert.assertEquals(size, selectedParameters.size());
        for (PricedParameterRow row : selectedParameters) {
            Assert.assertNotNull(row.getPricedParameter());
        }
    }

    @Test
    public void testCreatePricedParameterRowList_IncludeConfigurableOneTimeParams()
            throws ConverterException {

        // Given product with two one time parameter, 1 configurable, 1 not
        // configurable
        VOService product = givenProductWithParams();
        List<PricedParameterRow> selectedParameters;

        // when
        selectedParameters = PricedParameterRow.createPricedParameterRowList(
                product, true, true, true, true, true);

        // then
        Assert.assertEquals(2, selectedParameters.size());
        assertTrue(selectedParameters.get(0).getParameter().isConfigurable());
        assertTrue(selectedParameters.get(1).isOneTimeParameter());
        assertTrue(!selectedParameters.get(1).getParameter().isConfigurable());
    }

    @Test
    public void testCreatePricedParameterRowList_NotIncludeConfigurableOneTimeParams()
            throws ConverterException {

        // Given product with two one time parameter, 1 configurable, 1 not
        // configurable
        VOService product = givenProductWithParams();
        List<PricedParameterRow> selectedParameters;

        // when
        selectedParameters = PricedParameterRow.createPricedParameterRowList(
                product, true, true, true, true, false);

        // then
        Assert.assertEquals(1, selectedParameters.size());
        assertTrue(selectedParameters.get(0).isOneTimeParameter());

    }

    @Test
    public void createPricedParameterRowListForSubscription()
            throws ConverterException {

        // Given product with two one time parameter, 1 configurable, 1 not
        // configurable
        VOService product = givenProductWithParams();
        List<PricedParameterRow> selectedParameters;

        // when
        selectedParameters = PricedParameterRow
                .createPricedParameterRowListForSubscription(product);

        // then
        Assert.assertEquals(1, selectedParameters.size());
        assertTrue(selectedParameters.get(0).isOneTimeParameter());
        Assert.assertNull(selectedParameters.get(0).getParameter().getValue());

    }

    @Test
    public void createPricedParameterRowListForService()
            throws ConverterException {

        // Given product with two one time parameter, 1 configurable, 1 not
        // configurable
        VOService product = givenProductWithParams();
        List<PricedParameterRow> selectedParameters;

        // when
        selectedParameters = PricedParameterRow
                .createPricedParameterRowListForService(product);

        // then
        Assert.assertEquals(1, selectedParameters.size());
        assertTrue(selectedParameters.get(0).isOneTimeParameter());
        Assert.assertEquals("d1", selectedParameters.get(0).getParameter()
                .getValue());

    }

    VOService givenProductWithParams() {
        VOService prod = new VOService();
        List<VOParameter> params = new ArrayList<VOParameter>();

        VOParameterDefinition pd = new VOParameterDefinition(
                ParameterType.SERVICE_PARAMETER, "Voucher code",
                "One-time Parameter", ParameterValueType.STRING, "d1", null,
                null, false, true, null);
        pd.setModificationType(ParameterModificationType.ONE_TIME);
        VOParameter param = new VOParameter(pd);
        param.setValue(null);
        param.setConfigurable(true);
        params.add(param);

        pd = new VOParameterDefinition(ParameterType.SERVICE_PARAMETER,
                "Ticket number", "One-time Parameter",
                ParameterValueType.STRING, "d2", null, null, false, true, null);
        pd.setModificationType(ParameterModificationType.ONE_TIME);
        param = new VOParameter(pd);
        param.setValue("1234");
        param.setConfigurable(false);
        params.add(param);

        prod.setParameters(params);
        return prod;

    }

}
