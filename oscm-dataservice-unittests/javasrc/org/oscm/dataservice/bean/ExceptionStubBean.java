/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: schmid                                                 
 *                                                                              
 *  Creation Date: 04.03.2009                                                      
 *                                                                              
 *  Completion Time:                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.bean;

import static org.oscm.test.Numbers.TIMESTAMP;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * @author schmid
 * 
 */
@Stateless
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ExceptionStubBean implements ExceptionStub {

    @EJB
    DataService dataManager;

    public void throwApplicationException() throws SaaSApplicationException {
        String id = "AppExcNonsense";
        try {
            insertSomeData(id);
        } catch (NonUniqueBusinessKeyException e) {
            throw new SaaSApplicationException(id, e);
        }
        throw new SaaSApplicationException(id);
    }

    public void throwSystemException() {
        String id = "SysExcNonsense";
        try {
            insertSomeData(id);
        } catch (NonUniqueBusinessKeyException e) {
            throw new SaaSSystemException(id, e);
        }
        throw new SaaSSystemException(id);
    }

    public boolean findData(String id) {
        Organization cust = new Organization();
        cust.setOrganizationId("1000");
        cust = (Organization) dataManager.find(cust);
        Product qry = new Product();
        qry.setVendor(cust);
        qry.setProductId(id);
        Product product = (Product) dataManager.find(qry);
        return product != null;
    }

    private void insertSomeData(String id) throws NonUniqueBusinessKeyException {
        Organization cust = new Organization();
        cust.setOrganizationId("1000");
        cust.setName("Name of organization " + cust.getOrganizationId());
        cust.setAddress("Address of organization " + cust.getOrganizationId());
        cust.setEmail(cust.getOrganizationId() + "@organization.com");
        cust.setPhone("012345/678" + cust.getOrganizationId());
        cust.setCutOffDay(1);
        dataManager.persist(cust);
        TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                dataManager, cust, "testTP", false, ServiceAccessType.LOGIN);
        Product prod = new Product();
        prod.setVendor(cust);
        prod.setProductId(id);
        prod.setType(ServiceType.TEMPLATE);
        prod.setProvisioningDate(TIMESTAMP);
        PriceModel pi = new PriceModel();
        prod.setPriceModel(pi);
        prod.setStatus(ServiceStatus.ACTIVE);
        ParameterSet emptyPS = new ParameterSet();
        prod.setParameterSet(emptyPS);
        prod.setTechnicalProduct(tp);
        dataManager.persist(prod);
    }

}
