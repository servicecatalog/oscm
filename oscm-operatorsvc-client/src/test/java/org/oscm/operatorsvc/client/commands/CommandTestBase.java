/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.junit.Before;
import org.junit.Test;
import org.oscm.converter.ResourceLoader;
import org.oscm.internal.intf.OperatorService;
import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;

import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public abstract class CommandTestBase implements InvocationHandler {

    protected Map<String, String> args;

    private CharArrayWriter out;

    private CharArrayWriter err;

    protected CommandContext ctx;

    protected IOperatorCommand command;

    protected String stubMethodName;

    protected Object[] stubCallArgs;

    protected Object stubCallReturn;

    @Before
    public void setup() {
        args = new HashMap<String, String>();
        out = new CharArrayWriter();
        err = new CharArrayWriter();
        OperatorService service = (OperatorService) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] { OperatorService.class }, this);
        ctx = new CommandContext(service, args, new PrintWriter(out, true),
                new PrintWriter(err, true));
        command = createCommand();
    }

    @Test
    public void testDescription() {
        assertTrue(command.getDescription().length() > 20);
    }

    protected abstract IOperatorCommand createCommand();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        stubMethodName = method.getName();
        stubCallArgs = args;
        return stubCallReturn;
    }

    protected void assertOut(String expected) {
        assertEquals(String.format(expected), out.toString());
    }

    protected void assertErr(String expected) {
        assertEquals(String.format(expected), err.toString());
    }

    /**
     * Tests whether all the arguments defined as "ARG_*" constants by the
     * specified class are also contained by the specified properties file.
     */
    static void checkArgsAvailableInFile(Class<?> clazz, String fileName)
            throws Exception {

        Properties props = new Properties();
        try (InputStream in = ResourceLoader.getResourceAsStream(clazz,
                fileName);) {
            assertNotNull(in);
            props.load(in);
        }

        // check if all declared arguments are also contained by the file
        Class<?> clazzInstance = Class.forName(clazz.getName());
        Field[] fields = clazzInstance.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().startsWith("ARG_")) {
                Object fieldValue = fields[i].get(null);
                assertTrue("Properties file '" + fileName
                        + "' does not contain property '" + fieldValue + "'",
                        props.containsKey(fieldValue));
            }
        }
    }

}
