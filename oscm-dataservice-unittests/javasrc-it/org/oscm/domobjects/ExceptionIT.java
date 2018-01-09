/********************************************************************************
 *                                                                             
 *  Copyright FUJITSU LIMITED 2018
 *                                                                             
 *  Author: schmid
 *                                                                             
 *  Creation Date: 20.01.2009                                                     
 *                                                                            
 *  Completion Time:                            
 *                                                                             
 ********************************************************************************/
package org.oscm.domobjects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.ejb.EJBException;

import org.junit.Test;

import org.oscm.dataservice.bean.ExceptionStub;
import org.oscm.dataservice.bean.ExceptionStubBean;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Tests of the organization-related domain objects (incl. auditing
 * functionality)
 * 
 * @author schmid
 * 
 */
public class ExceptionIT extends DomainObjectTestBase {

    ExceptionStub exceptionStub;

    @Override
    public void setup(TestContainer container) throws Exception {
        super.setup(container);
        container.addBean(new ExceptionStubBean());
        exceptionStub = container.get(ExceptionStub.class);
    }

    @Test(expected = SaaSSystemException.class)
    public void testSaasSystemException() throws Exception {
        SaaSSystemException ex = new SaaSSystemException(new Exception(
                "My SystemException"));
        System.err.println(ex.getMessage());
        System.err.println(ex.getCauseStackTrace());
        throw ex;
    }

    @Test(expected = SaaSApplicationException.class)
    public void testSaasApplicationException() throws SaaSApplicationException {
        SaaSApplicationException ex = new SaaSApplicationException(
                new Exception("My ApplicationException"));
        System.err.println(ex.getMessage());
        System.err.println(ex.getCauseStackTrace());
        throw ex;
    }

    @Test
    public void testTXRollbackWithApplicationException() {
        try {
            exceptionStub.throwApplicationException();
        } catch (SaaSApplicationException e) {
            assertTrue(exceptionStub.findData("AppExcNonsense"));
        }
    }

    @Test
    public void testTXRollbackWithSystemException() {
        try {
            exceptionStub.throwSystemException();
        } catch (EJBException e) {
            assertTrue(e.getCause().getClass() == SaaSSystemException.class);
            assertFalse(exceptionStub.findData("SysExcNonsense"));
        }
    }

}
