/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 19, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.rest.common.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.rest.common.Representation;
import org.oscm.rest.common.RepresentationCollection;

/**
 * Unit test for RepresentationCollection
 * 
 * @author miethaner
 */
public class RepresentationCollectionTest {

    private class RepTest extends Representation {

        @Override
        public void validateContent() throws WebApplicationException {
        }

        @Override
        public void update() {
        }

        @Override
        public void convert() {
        }

    }

    @Test
    public void testCreate() {

        RepTest rep = Mockito.spy(new RepTest());

        RepresentationCollection<RepTest> coll = new RepresentationCollection<RepresentationCollectionTest.RepTest>();
        coll.setItems(Arrays.asList(rep));

        coll.setVersion(new Integer(1));

        assertEquals(1, rep.getVersion().intValue());

        coll.convert();

        Mockito.verify(rep).convert();

        coll.update();

        Mockito.verify(rep).update();

        coll.validateContent();

        Mockito.verify(rep).validateContent();

        coll = new RepresentationCollection<RepresentationCollectionTest.RepTest>(
                Arrays.asList(rep));

        assertTrue(coll.getItems().contains(rep));

    }

}
