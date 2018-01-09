/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 06.10.2011                                                      
 *                                                                              
 *  Completion Time: 06.10.2011                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Collections;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;

/**
 * Tests for the PSP domain object.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PSPIT extends DomainObjectTestBase {

    @Test
    public void add() throws Exception {
        PSP psp = createPSP();
        verify(ModificationType.ADD, Collections.singletonList(psp), PSP.class);
    }

    @Test
    public void modify() throws Exception {
        final PSP psp = createPSP();
        PSP modPsp = runTX(new Callable<PSP>() {
            public PSP call() throws Exception {
                PSP lpsp = mgr.getReference(PSP.class, psp.getKey());
                lpsp.setIdentifier("identifierForPSP2");
                lpsp.setWsdlUrl("wsdlUrl2");
                return lpsp;
            }
        });
        verify(ModificationType.MODIFY, Collections.singletonList(modPsp),
                PSP.class);
    }

    @Test
    public void delete() throws Exception {
        final PSP psp = createPSP();
        PSP modPsp = runTX(new Callable<PSP>() {
            public PSP call() throws Exception {
                PSP lpsp = mgr.getReference(PSP.class, psp.getKey());
                mgr.remove(lpsp);
                return lpsp;
            }
        });
        verify(ModificationType.DELETE, Collections.singletonList(modPsp),
                PSP.class);
    }

    private PSP createPSP() throws Exception {
        PSP psp = runTX(new Callable<PSP>() {
            public PSP call() throws Exception {
                PSP psp = new PSP();
                psp.setIdentifier("identifierForPSP");
                psp.setWsdlUrl("wsdlUrl");
                psp.setDistinguishedName("distinguishedName");
                mgr.persist(psp);
                return psp;
            }
        });
        return psp;
    }

}
