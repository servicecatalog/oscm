/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 10.04.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.generator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.test.BaseAdmUmTest;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.ServiceDetails;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscription;

/**
 * @author weiser
 * 
 */
public class IdGeneratorTest {

    private static final String COPY_OF_PREFIX = "Copy of ";
    private static final String BASE_ID = "id1";

    private ServiceDetails sd;
    private Service s;
    private List<VOService> services;
    private List<VOSubscription> subscriptions;

    private IdGenerator gen;

    @Before
    public void setup() {
        VOServiceDetails voDetails = new VOServiceDetails();
        voDetails.setServiceId(BASE_ID);
        voDetails.setName(BASE_ID);

        sd = new ServiceDetails(voDetails);
        services = excludeService(BASE_ID, new ArrayList<VOService>());

        s = new Service(voDetails);
        subscriptions = new ArrayList<VOSubscription>();
    }

    @Test
    public void generateNewId_Service_CopyCopy() throws Exception {
        gen = new IdGenerator(COPY_OF_PREFIX, sd, services);
        String genId = gen.generateNewId();
        assertEquals(COPY_OF_PREFIX + BASE_ID, genId);

        sd.setServiceId(genId);
        gen = new IdGenerator(COPY_OF_PREFIX, sd, excludeService(genId,
                services));
        genId = gen.generateNewId();
        assertEquals(COPY_OF_PREFIX + COPY_OF_PREFIX + BASE_ID, genId);
    }

    @Test
    public void generateNewId_Service_CopyCopyToLong() throws Exception {
        String baseId = BaseAdmUmTest.TOO_LONG_ID;
        sd.setServiceId(baseId);
        gen = new IdGenerator(COPY_OF_PREFIX, sd, services);

        String expected = (COPY_OF_PREFIX + baseId).substring(0,
                ADMValidator.LENGTH_ID);
        String genId = gen.generateNewId();
        assertEquals(expected, genId);

        sd.setServiceId(genId);
        gen = new IdGenerator(COPY_OF_PREFIX, sd, excludeService(genId,
                services));
        expected = (COPY_OF_PREFIX + COPY_OF_PREFIX + baseId).substring(0,
                ADMValidator.LENGTH_ID);
        genId = gen.generateNewId();
        assertEquals(expected, genId);
    }

    @Test
    public void generateNewId_Service_CopyCopyTwice() throws Exception {
        gen = new IdGenerator(COPY_OF_PREFIX, sd, services);
        String genId = gen.generateNewId();
        String copyId = genId;
        assertEquals(COPY_OF_PREFIX + BASE_ID, genId);

        sd.setServiceId(copyId);
        gen = new IdGenerator(COPY_OF_PREFIX, sd, excludeService(genId,
                services));
        genId = gen.generateNewId();
        assertEquals(COPY_OF_PREFIX + COPY_OF_PREFIX + BASE_ID, genId);

        gen = new IdGenerator(COPY_OF_PREFIX, sd, excludeService(genId,
                services));
        genId = gen.generateNewId();
        assertEquals(COPY_OF_PREFIX + COPY_OF_PREFIX + BASE_ID + "(" + 2 + ")",
                genId);
    }

    @Test
    public void generateNewId_Service_CopyToLong() throws Exception {
        String baseId = BaseAdmUmTest.TOO_LONG_ID;
        sd.setServiceId(baseId);
        gen = new IdGenerator(COPY_OF_PREFIX, sd, excludeService(baseId,
                services));

        String expected = (COPY_OF_PREFIX + baseId).substring(0,
                ADMValidator.LENGTH_ID);
        String genId = gen.generateNewId();
        assertEquals(expected, genId);
    }

    @Test
    public void generateNewId_Service_CopyTwice() throws Exception {
        gen = new IdGenerator(COPY_OF_PREFIX, sd, services);
        String genId = gen.generateNewId();
        assertEquals(COPY_OF_PREFIX + BASE_ID, genId);

        gen = new IdGenerator(COPY_OF_PREFIX, sd, excludeService(genId,
                services));
        genId = gen.generateNewId();
        assertEquals(COPY_OF_PREFIX + BASE_ID + "(" + 2 + ")", genId);
    }

    @Test
    public void generateNewId_Service_CopyTwiceToLong() throws Exception {
        String baseId = BaseAdmUmTest.TOO_LONG_ID;
        sd.setServiceId(baseId);
        gen = new IdGenerator(COPY_OF_PREFIX, sd, excludeService(baseId,
                services));

        String expected = (COPY_OF_PREFIX + baseId).substring(0,
                ADMValidator.LENGTH_ID);
        String genId = gen.generateNewId();
        assertEquals(expected, genId);

        gen = new IdGenerator(COPY_OF_PREFIX, sd, excludeService(genId,
                services));
        genId = gen.generateNewId();
        assertEquals(expected.substring(0, ADMValidator.LENGTH_ID - 7) + "("
                + 2 + ")", genId);
    }

    @Test
    public void generateNewId_Service_EmptyPrefix() throws Exception {
        String prefix = "";
        gen = new IdGenerator(prefix, sd, services);

        String genId = gen.generateNewId();
        assertEquals(BASE_ID + "(" + 2 + ")", genId);
    }

    @Test
    public void generateNewId_Service_NullPrefix() throws Exception {
        String prefix = null;
        gen = new IdGenerator(prefix, sd, services);

        String genId = gen.generateNewId();
        assertEquals(BASE_ID + "(" + 2 + ")", genId);
    }

    @Test
    public void generateNewId_Service_BlanksPrefix() throws Exception {
        String prefix = "   ";
        gen = new IdGenerator(prefix, sd, services);

        String genId = gen.generateNewId();
        assertEquals(BASE_ID + "(" + 2 + ")", genId);
    }

    @Test
    public void generateNewId_Subscription() {
        gen = new IdGenerator("", s, subscriptions);
        assertEquals(s.getName(), gen.generateNewId());
    }

    @Test
    public void generateNewId_Subscription_NullName() {
        s.setName(null);
        gen = new IdGenerator("", s, subscriptions);
        assertEquals("", gen.generateNewId());
    }

    @Test
    public void generateNewId_Subscription_EmptyName() {
        s.setName("");
        gen = new IdGenerator("", s, subscriptions);
        assertEquals("", gen.generateNewId());
    }

    @Test
    public void generateNewId_NullService() {
        gen = new IdGenerator("", null, subscriptions);
        assertEquals("", gen.generateNewId());
    }

    @Test
    public void generateNewId_Subscription_AlreadyExisting() {
        gen = new IdGenerator("", s, excludeSubscription(s.getName(),
                subscriptions));
        assertEquals(s.getName() + "(2)", gen.generateNewId());
    }

    @Test
    public void generateNewId_SpecialCharacters() {
        char[] invalidChars = new char[] { 0x19, 0x27, 0x2A, 0x2B };
        s.setName("hello" + String.valueOf(invalidChars) + " World" + "\uFFFF");

        gen = new IdGenerator("", s, new ArrayList<VOSubscription>());

        assertEquals("hello" + String.valueOf(invalidChars) + " World" + "\uFFFF", gen.generateNewId());
    }

    @Test
    public void generateNewId_LineBreak() {
        s.setName("id\n1 (2.5)");

        gen = new IdGenerator("", s, new ArrayList<VOSubscription>());

        assertEquals("id\n1 (2.5)", gen.generateNewId());
    }

    private static final List<VOService> excludeService(final String id,
            final List<VOService> list) {
        VOService svc = new VOService();
        svc.setServiceId(id);
        list.add(svc);
        return list;
    }

    private static final List<VOSubscription> excludeSubscription(
            final String id, final List<VOSubscription> list) {
        VOSubscription svc = new VOSubscription();
        svc.setSubscriptionId(id);
        list.add(svc);
        return list;
    }

}
