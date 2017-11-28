/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-8-1                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.verification;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.UpdateConstraintException;

/**
 * @author yuyin
 * 
 */
public class UpdateParameterCheckTest {
    private TechnicalProduct techProd;
    private String modificationType = ParameterModificationType.ONE_TIME.name();
    private List<Product> products;
    private ParameterDefinition paramDef;

    @Before
    public void setUp() {
        paramDef = new ParameterDefinition();
        paramDef.setModificationType(ParameterModificationType.ONE_TIME);
        techProd = new TechnicalProduct();
        products = new ArrayList<Product>();
    }

    @Test
    public void updateParameterDefinition_NoMarketplaceService()
            throws Exception {
        techProd.setProducts(null);
        UpdateParameterCheck.updateParameterDefinition(paramDef, techProd,
                modificationType);
    }

    @Test
    public void updateParameterDefinition_NoNeedToUpdateParamModificationType()
            throws Exception {
        products.add(new Product());
        techProd.setProducts(products);
        UpdateParameterCheck.updateParameterDefinition(paramDef, techProd,
                modificationType);
    }

    @Test
    public void updateParameterDefinition_MarketplaceServiceDeleted()
            throws Exception {
        Product product = new Product();
        product.setStatus(ServiceStatus.DELETED);
        products.add(product);
        techProd.setProducts(products);
        UpdateParameterCheck.updateParameterDefinition(paramDef, techProd,
                modificationType);
    }

    @Test(expected = UpdateConstraintException.class)
    public void updateParameterDefinition_UpdateConstraintExceptione()
            throws Exception {
        Product product = new Product();
        product.setStatus(ServiceStatus.ACTIVE);
        products.add(product);
        techProd.setProducts(products);
        UpdateParameterCheck.updateParameterDefinition(paramDef, techProd,
                ParameterModificationType.STANDARD.name());
    }
}
