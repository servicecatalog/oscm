/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 05.10.2009                                                      
 *                                                                              
 *  Completion Time: 05.10.2009                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.AccessException;
import java.util.*;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.oscm.converter.PropertiesLoader;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.operatorsvc.client.commands.*;

/**
 * Client to invoke the operator related services.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class OperatorClient {

    private static final Map<String, IOperatorCommand> COMMANDS = new HashMap<String, IOperatorCommand>();

    private static void registerCommand(IOperatorCommand c) {
        COMMANDS.put(c.getName(), c);
    }

    static {
        registerCommand(new CreateOrganizationCommand());
        registerCommand(new UpdateOrganizationCommand());
        registerCommand(new AddOrganizationToRoleCommand());
        registerCommand(new SetPSPAccountForOrganizationCommand());
        registerCommand(new AddAvailablePaymentTypesCommand());
        registerCommand(new RetryFailedPaymentProcessesCommand());
        registerCommand(new RetrieveTimerExpirationsCommand());
        registerCommand(new StartBillingRunCommand());
        registerCommand(new ReinitTimersCommand());
        registerCommand(new LockUserCommand());
        registerCommand(new UnlockUserCommand());
        registerCommand(new SetDistinguishedNameCommand());
        registerCommand(new GetOrganizationBillingDataCommand());
        registerCommand(new GetRevenueListCommand());
        registerCommand(new ResetPasswordForUserCommand());
        registerCommand(new AddCurrencyCommand());
        registerCommand(new GetConfigurationSettingsCommand());
        registerCommand(new SaveConfigurationSettingCommand());
        registerCommand(new StartPaymentProcessingCommand());
        registerCommand(new GetUserOperationLogCommand());
        registerCommand(new GetSubscriptionUsage());
    }

    /**
     * Returns the names of the available commands.
     */
    public static List<String> getCommands() {
        List<String> names = new ArrayList<String>(COMMANDS.keySet());
        Collections.sort(names);
        return names;
    }

    /**
     * Returns the command with the given name.
     */
    public static IOperatorCommand getCommand(String name) {
        return COMMANDS.get(name);
    }

    private final PrintWriter out;

    private final PrintWriter err;

    private final Context namingContext;

    public OperatorClient(Context namingContext, PrintWriter out,
            PrintWriter err) {
        this.namingContext = namingContext;
        this.out = out;
        this.err = err;
    }

    public boolean execute(final String commandName, Map<String, String> args) {
        final IOperatorCommand command = COMMANDS.get(commandName);
        try {
            OperatorService service = (OperatorService) namingContext
                    .lookup(OperatorService.class.getName());
            final CommandContext ctx = new CommandContext(service, args, out,
                    err);
            return command.run(ctx);
        } catch (Exception e) {
            handleException(e);
            return false;
        }
    }

    protected void handleException(Exception e) {
        StringBuffer sb = new StringBuffer();
        Throwable cause = e;
        while (cause != null) {
            final String msg = cause.getMessage();
            if (sb.length() > 0) {
                sb.append("\n");
                sb.append("caused by: ");
            }
            if (cause instanceof MailOperationException) {
                final String returnText = "Operation aborted as an email could not be sent. Please check that the mail server is reachable and running.";
                addTextToBuffer(sb, true, returnText);
            } else if (cause instanceof AccessException
                    || (msg != null && msg.contains("java.rmi.AccessException"))) {
                // an java.rmi.AccessException indicates that the caller
                // does not have permission to perform the requested action
                final String returnText = "Operation aborted. Either the login failed or the user does not have the appropriate permissions.";
                addTextToBuffer(sb, true, returnText);
            } else if (msg != null && msg.trim().length() > 0) {
                addTextToBuffer(sb, false, msg);
            } else {
                addTextToBuffer(sb, false, cause.toString());
            }
            cause = cause.getCause();
        }
        err.print(sb.toString());
        err.flush();
    }

    protected void addTextToBuffer(StringBuffer sb, boolean resetSb,
            String textToAdd) {
        if (resetSb) {
            sb.setLength(0);
        }
        sb.append(textToAdd);
    }

    public static void login(final String username, final String password)
            throws Exception {
        new ProgrammaticLoginHandler().login(username, password);
    }

    private static Properties readPropertyFile(final String file)
            throws IOException {
        System.out.println("Loading: " + new File(file).getAbsolutePath());
        return PropertiesLoader.loadProperties(new FileInputStream(file));
    }

    // === Running the Operator Client a a Java main ===

    private static final String ENVIRONMENT_PROPERTIES = "env.properties";

    public static void main(String[] argarray) throws Exception {

        final PrintWriter out = new PrintWriter(System.out, true);
        final PrintWriter err = new PrintWriter(System.err, true);

        String username = null;
        String password = null;
        String command = null;
        Map<String, String> cmdArguments = null;

        final Iterator<String> args = Arrays.asList(argarray).iterator();

        try {
            username = args.next();
            password = args.next();
            command = args.next();
            cmdArguments = readArguments(args);
        } catch (NoSuchElementException e) {
            printHelp(err);
            System.exit(-1);
        }

        if (getCommand(command) == null) {
            err.printf("Unknown command '%s'.%nAvailable Commands are %s.%n",
                    command, getCommands());
            System.exit(-1);
        }

        final Properties ctxProperties = new Properties();
        ctxProperties.putAll(System.getProperties());
        if (new File(ENVIRONMENT_PROPERTIES).exists()) {
            ctxProperties.putAll(readPropertyFile(ENVIRONMENT_PROPERTIES));
        } else {
            System.out.printf("No %s file, using system properties only.%n",
                    ENVIRONMENT_PROPERTIES);
        }

        final InitialContext initialContext = new InitialContext(ctxProperties);
        ctxProperties.put(Context.SECURITY_PRINCIPAL, username);
        ctxProperties.put(Context.SECURITY_CREDENTIALS, password);

        final OperatorClient client = new OperatorClient(initialContext, out,
                err);
        if (!client.execute(command, cmdArguments)) {
            System.exit(-1);
        }
    }

    protected static Map<String, String> readArguments(
            final Iterator<String> args) throws IOException {
        Map<String, String> result = new HashMap<String, String>();
        while (args.hasNext()) {
            final String s = args.next();
            if (s.equals("-f")) {
                readArgumentsFromPropertyFile(result, args.next());
                continue;
            }
            final int sep = s.indexOf('=');
            if (sep == -1) {
                throw new NoSuchElementException();
            }
            result.put(s.substring(0, sep), s.substring(sep + 1));
        }
        return result;
    }

    protected static void readArgumentsFromPropertyFile(
            final Map<String, String> result, final String file)
            throws IOException {
        final Properties properties = readPropertyFile(file);
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            result.put((String) entry.getKey(), (String) entry.getValue());
        }
    }

    protected static void printHelp(PrintWriter err) {
        err.println("Usage: java -jar oscm-operatorsvc-client.jar "
                + "<operator-name> <operator-pwd> <command> (<key>=<value>)*");
        err.println();
        err.println("The following command are available: " + getCommands());
        err.println("In addition to the key=value syntax command arguments "
                + "can be read from a property file specified with -f <filename>.");
    }

}
