/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dataaccess.validator;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.MandatoryCustomerUdaMissingException;
import org.oscm.internal.types.exception.MandatoryUdaMissingException;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * @author weiser
 * 
 */
public class MandatoryUdaValidatorTest {

    private MandatoryUdaValidator muv;
    private UdaDefinition defSupplier;
    private UdaDefinition defCustMandatory;
    private UdaDefinition defSubMandatory;
    private UdaDefinition defOptional;

    @Before
    public void setup() {
        muv = new MandatoryUdaValidator();

        defSupplier = new UdaDefinition();
        defSupplier.setTargetType(UdaTargetType.CUSTOMER);
        defSupplier.setConfigurationType(UdaConfigurationType.SUPPLIER);
        defSupplier.setKey(1);

        defCustMandatory = new UdaDefinition();
        defCustMandatory.setTargetType(UdaTargetType.CUSTOMER);
        defCustMandatory.setConfigurationType(
                UdaConfigurationType.USER_OPTION_MANDATORY);
        defCustMandatory.setKey(2);

        defSubMandatory = new UdaDefinition();
        defSubMandatory.setTargetType(UdaTargetType.CUSTOMER_SUBSCRIPTION);
        defSubMandatory.setConfigurationType(
                UdaConfigurationType.USER_OPTION_MANDATORY);
        defSubMandatory.setKey(4);

        defOptional = new UdaDefinition();
        defOptional.setTargetType(UdaTargetType.CUSTOMER);
        defOptional.setConfigurationType(
                UdaConfigurationType.USER_OPTION_OPTIONAL);
        defOptional.setKey(3);
    }

    @Test
    public void checkMandatory_SUPPLIER() throws Exception {
        muv.checkMandatory(defSupplier);
    }

    @Test
    public void checkMandatory_USER_OPTION_OPTIONAL() throws Exception {
        defSupplier.setConfigurationType(
                UdaConfigurationType.USER_OPTION_OPTIONAL);

        muv.checkMandatory(defSupplier);
    }

    @Test(expected = MandatoryUdaMissingException.class)
    public void checkMandatory_USER_OPTION_MANDATORY() throws Exception {
        defSupplier.setConfigurationType(
                UdaConfigurationType.USER_OPTION_MANDATORY);

        muv.checkMandatory(defSupplier);
    }

    @Test(expected = MandatoryCustomerUdaMissingException.class)
    public void checkAllRequiredUdasPassed_Missing() throws Exception {
        muv.checkAllRequiredUdasPassed(Arrays.asList(defCustMandatory),
                new ArrayList<Uda>(), new ArrayList<VOUda>());
    }

    @Test(expected = MandatoryCustomerUdaMissingException.class)
    public void checkAllRequiredUdasPassed_EmptyDefaultValue()
            throws Exception {
        defCustMandatory.setDefaultValue("");
        muv.checkAllRequiredUdasPassed(Arrays.asList(defCustMandatory),
                new ArrayList<Uda>(), new ArrayList<VOUda>());
    }

    @Test
    public void checkAllRequiredUdasPassed_DefaultValue() throws Exception {
        defCustMandatory.setDefaultValue("test");
        muv.checkAllRequiredUdasPassed(Arrays.asList(defCustMandatory),
                new ArrayList<Uda>(), new ArrayList<VOUda>());
    }

    @Test
    public void checkAllRequiredUdasPassed_Passed() throws Exception {
        muv.checkAllRequiredUdasPassed(Arrays.asList(defCustMandatory),
                new ArrayList<Uda>(),
                Arrays.asList(createVOUda(defCustMandatory)));
    }

    @Test
    public void checkAllRequiredUdasPassed_Existing() throws Exception {
        muv.checkAllRequiredUdasPassed(Arrays.asList(defCustMandatory),
                Arrays.asList(createUda(defCustMandatory)),
                new ArrayList<VOUda>());
    }

    @Test(expected = MandatoryUdaMissingException.class)
    public void checkAllRequiredUdasPassed() throws Exception {
        muv.checkAllRequiredUdasPassed(Arrays.asList(defSubMandatory),
                Arrays.asList(createUda(defOptional)),
                Arrays.asList(createVOUda(defOptional)));
    }

    private static VOUda createVOUda(UdaDefinition def) {
        VOUdaDefinition voDef = new VOUdaDefinition();
        voDef.setKey(def.getKey());

        VOUda voUda = new VOUda();
        voUda.setUdaDefinition(voDef);
        return voUda;
    }

    private static Uda createUda(UdaDefinition def) {
        Uda uda = new Uda();
        uda.setUdaDefinition(def);
        return uda;
    }
}
