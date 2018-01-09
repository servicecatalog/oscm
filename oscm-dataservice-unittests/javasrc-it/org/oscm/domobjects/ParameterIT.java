/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Peter Pock                                                      
 *                                                                              
 *  Creation Date: 29.06.2009                                                      
 *                                                                              
 *  Completion Time: 30.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.Numbers.TIMESTAMP;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Test of the parameter domain object.
 * 
 * @author Peter Pock
 * 
 */
@SuppressWarnings("boxing")
public class ParameterIT extends DomainObjectTestBase {

    private List<Parameter> createdParameters = new ArrayList<Parameter>();
    private long parameterSetKey;
    private long parameterDefKey;

    @Override
    protected void dataSetup() throws Exception {
        ParameterDefinition parameterDef = new ParameterDefinition();
        parameterDef.setParameterId("parameterId");
        parameterDef.setParameterType(ParameterType.PLATFORM_PARAMETER);
        parameterDef.setValueType(ParameterValueType.LONG);
        mgr.persist(parameterDef);
        parameterDefKey = parameterDef.getKey();

        Organization organization = Organizations.createOrganization(mgr);
        // create technical product
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP_ID", false, ServiceAccessType.LOGIN);

        Product prod = new Product();
        prod.setVendor(organization);
        prod.setProductId("Product");
        prod.setTechnicalProduct(tProd);
        prod.setProvisioningDate(TIMESTAMP);
        prod.setStatus(ServiceStatus.ACTIVE);
        prod.setType(ServiceType.TEMPLATE);

        ParameterSet parameterSet = new ParameterSet();
        prod.setParameterSet(parameterSet);

        mgr.persist(prod);
        parameterSetKey = parameterSet.getKey();

    }

    @Test
    public void testAdd() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestCheckCreation();
                return null;
            }
        });
    }

    @Test
    public void testDelete() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestAdd();
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doTestRemoval();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                doVerifyRemoval();
                ParameterSet parameterSet = getParameterSet();
                parameterSet.getProduct().setParameterSet(null);
                mgr.remove(parameterSet);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                try {
                    mgr.getReference(Parameter.class, createdParameters.get(0)
                            .getKey());
                    Assert.fail("Parameter not removed although parameter set was removed!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    private void doTestRemoval() throws ObjectNotFoundException {
        Parameter param = mgr.getReference(Parameter.class, createdParameters
                .get(0).getKey());
        mgr.remove(param);
    }

    private void doVerifyRemoval() throws Exception {
        try {
            mgr.getReference(Parameter.class, createdParameters.get(0).getKey());
            Assert.fail("Parameter not removed!");
        } catch (Exception e) {
        }
        createdParameters.remove(0);

        Assert.assertEquals("Wrong number of referenced parameters",
                getParameterSet().getParameters().size(),
                createdParameters.size());
    }

    private void doTestAdd() throws Exception {
        ParameterSet parameterSet = getParameterSet();
        Parameter param = new Parameter();
        param.setParameterDefinition(getParameterDef());
        param.setValue("value");
        param.setParameterSet(parameterSet);
        mgr.persist(param);

        createdParameters.add((Parameter) ReflectiveClone.clone(param));
    }

    private void doTestCheckCreation() throws Exception {
        Assert.assertEquals("Wrong number of referenced parameters",
                getParameterSet().getParameters().size(),
                createdParameters.size());

        for (Parameter param : createdParameters) {
            DomainObject<?> savedParam = mgr.getReference(Parameter.class,
                    param.getKey());
            List<DomainHistoryObject<?>> historizedParams = mgr
                    .findHistory(savedParam);
            Assert.assertEquals("Wrong number of history objects found",
                    createdParameters.size(), historizedParams.size());
            if (historizedParams.size() > 0) {
                DomainHistoryObject<?> hist = historizedParams.get(0);
                Assert.assertEquals(ModificationType.ADD, hist.getModtype());
                Assert.assertEquals("modUser", "guest", hist.getModuser());
                Assert.assertTrue(
                        ReflectiveCompare.showDiffs(savedParam, hist),
                        ReflectiveCompare.compare(savedParam, hist));
                Assert.assertEquals("OBJID in history different",
                        savedParam.getKey(), hist.getObjKey());
            }
            // now compare the objects themselves
            Assert.assertTrue(ReflectiveCompare.showDiffs(param, savedParam),
                    ReflectiveCompare.compare(param, savedParam));
        }
    }

    private ParameterSet getParameterSet() throws Exception {
        return mgr.getReference(ParameterSet.class, parameterSetKey);
    }

    private ParameterDefinition getParameterDef() throws Exception {
        return mgr.getReference(ParameterDefinition.class, parameterDefKey);
    }

    @Test
    public void testGetIntValue() throws Exception {
        Parameter p = new Parameter();
        p.setValue("1");
        Assert.assertEquals(1, p.getIntValue());
    }

    @Test
    public void testGetIntValueNoInt() throws Exception {
        Parameter p = new Parameter();
        p.setValue("kjhgf");
        Assert.assertEquals(-1, p.getIntValue());
    }

    @Test
    public void testGetBooleanValue() throws Exception {
        Parameter p = new Parameter();
        p.setValue("true");
        Assert.assertEquals(true, p.getBooleanValue());
    }

    @Test
    public void testGetBooleanValueNoBoolean() throws Exception {
        Parameter p = new Parameter();
        p.setValue("kjhgf");
        Assert.assertEquals(false, p.getBooleanValue());
    }
}
