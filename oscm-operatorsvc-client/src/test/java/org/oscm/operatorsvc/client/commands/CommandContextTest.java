/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.test.stubs.OperatorServiceStub;
import org.oscm.internal.intf.OperatorService;

public class CommandContextTest {

    private OperatorService service;

    private Map<String, String> args;

    private CharArrayWriter out;

    private CharArrayWriter err;

    private CommandContext ctx;

    @Before
    public void setup() {
        service = new OperatorServiceStub();
        args = new HashMap<String, String>();
        out = new CharArrayWriter();
        err = new CharArrayWriter();
        ctx = new CommandContext(service, args, new PrintWriter(out, true),
                new PrintWriter(err, true));
    }

    @Test
    public void testOut() {
        ctx.out().println("Hello out!");
        assertEquals(String.format("Hello out!%n"), out.toString());
    }

    @Test
    public void testErr() {
        ctx.err().println("Hello err!");
        assertEquals(String.format("Hello err!%n"), err.toString());
    }

    @Test
    public void testService() {
        assertSame(service, ctx.getService());
    }

    @Test
    public void testGetString() {
        args.put("key", "value");
        assertEquals("value", ctx.getString("key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStringMissing() {
        ctx.getString("key");
    }

    @Test
    public void testGetStringOptional() {
        args.put("key", "value");
        assertEquals("value", ctx.getStringOptional("key"));
    }

    @Test
    public void testGetStringOptionalMissing() {
        ctx.getStringOptional("key");
        assertEquals(null, ctx.getStringOptional("key"));
    }

    @Test
    public void testGetEnum() {
        args.put("key", "TYPE");
        assertEquals(ElementType.TYPE,
                ctx.getEnum("key", EnumSet.allOf(ElementType.class)));
    }

    @Test
    public void testGetEnumInvalid() {
        args.put("key", "CONSTRUCTOR");
        Set<ElementType> all = EnumSet.noneOf(ElementType.class);
        all.add(ElementType.FIELD);
        all.add(ElementType.METHOD);
        System.out.print(all);
        try {
            ctx.getEnum("key", all);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Invalid parameter value 'CONSTRUCTOR' for key. Valid values are [FIELD, METHOD].",
                    e.getMessage());
        }
    }

    @Test
    public void testGetEnumOptional() {
        args.put("key", "TYPE");
        assertEquals(ElementType.TYPE,
                ctx.getEnumOptional("key", EnumSet.allOf(ElementType.class)));
    }

    @Test
    public void testGetEnumOptionalInvalid() {
        args.put("key", "CONSTRUCTOR");
        Set<ElementType> all = EnumSet.noneOf(ElementType.class);
        all.add(ElementType.FIELD);
        all.add(ElementType.METHOD);
        assertEquals(null, ctx.getEnumOptional("key", all));
    }

    @Test
    public void testGetList() {
        args.put("key", "v1,v2,v3");
        assertEquals(Arrays.asList("v1", "v2", "v3"), ctx.getList("key"));
    }

    @Test
    public void testGetListEmpty() {
        args.put("key", "");
        assertEquals(Collections.emptyList(), ctx.getList("key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetListMissing() {
        ctx.getList("key");
    }

    @Test
    public void testGetEnumList() {
        args.put("key", "FIELD,TYPE");
        final HashSet<ElementType> expected = new HashSet<ElementType>(
                Arrays.asList(ElementType.FIELD, ElementType.TYPE));
        assertEquals(expected,
                ctx.getEnumList("key", EnumSet.allOf(ElementType.class)));
    }

    @Test
    public void testGetEnumListEmpty() {
        args.put("key", "");
        assertEquals(Collections.emptySet(),
                ctx.getEnumList("key", EnumSet.allOf(ElementType.class)));
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testGetEnumListInvalid() {
        args.put("key", "FIELD");
        Set<ElementType> all = EnumSet.noneOf(ElementType.class);
        all.add(ElementType.CONSTRUCTOR);
        all.add(ElementType.METHOD);

        ctx.getEnumList("key", all);
        fail("IllegalArgumentException expected.");
    }

}
