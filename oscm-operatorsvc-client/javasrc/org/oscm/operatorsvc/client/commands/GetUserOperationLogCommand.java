/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                    
 *                                                                              
 *  Creation Date: Oct 5, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 5, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oscm.converter.DateConverter;
import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;

/**
 * @author tokoda
 * 
 */
public class GetUserOperationLogCommand implements IOperatorCommand {

    protected static final String ARG_FILE_NAME = "filename";
    private static final String ARG_ENTITY_TYPE = "entitytype";
    private static final String ARG_FROM_DATE = "from";
    private static final String ARG_TO_DATE = "to";

    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_FILE_NAME, ARG_ENTITY_TYPE, ARG_FROM_DATE,
                ARG_TO_DATE);
    }

    public String getDescription() {
        return "Returns the log data of user operations in the specified time frame."
                + " The format for 'from' and 'to' is yyyy-MM-dd.";
    }

    public String getName() {
        return "getuseroperationlog";
    }

    public boolean run(CommandContext ctx) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // read parameters
        String outputFileName = ctx.getString(ARG_FILE_NAME);
        String validateError = validateFileName(outputFileName);
        if (validateError != null) {
            ctx.err().print(validateError);
            ctx.err().flush();
            return false;
        }
        String fromDateStr = ctx.getString(ARG_FROM_DATE);
        long fromDate = DateConverter.getBeginningOfDayInCurrentTimeZone(sdf
                .parse(fromDateStr).getTime());
        String toDateStr = ctx.getString(ARG_TO_DATE);
        long toDate = DateConverter.getBeginningOfDayInCurrentTimeZone(sdf
                .parse(toDateStr).getTime());
        List<String> operationsList = new ArrayList<String>();
        // call service
        byte[] result = ctx.getService().getUserOperationLog(operationsList,
                fromDate, toDate);
        String csv = "";
        if (result != null) {
            csv = new String(result, "UTF-8");
        }

        return writeResults(ctx, outputFileName, csv);
    }

    PrintWriter createPrintWriter(File outputFile) throws IOException {
        return new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
    }

    public boolean replaceGreateAndLessThan() {
        return true;
    }

    protected String validateFileName(String fileName) {
        if (fileName == null || fileName.trim().length() == 0) {
            return "File name can not be empty.\n";
        }
        File file = new File(fileName);
        if (file.exists()) {
            return "Specified file already exists.\n";
        }
        return null;
    }

    protected boolean writeResults(CommandContext ctx, String outputFileName, String content) throws IOException {
        // write result
        File outputFile = new File(outputFileName);
        PrintWriter pw = null;
        try {
            pw = createPrintWriter(outputFile);
            pw.print(content);
            pw.flush();
        } catch (Exception e) {
            ctx.err().print("The file can not be created.\n");
            ctx.err().flush();
            return false;
        } finally {
            if (pw != null) {
                pw.close();
            }
        }

        ctx.out().print(String.format("Successfully created the file: %s%n",
                outputFile.getCanonicalPath()));
        ctx.out().flush();
        return true;
    }
}
