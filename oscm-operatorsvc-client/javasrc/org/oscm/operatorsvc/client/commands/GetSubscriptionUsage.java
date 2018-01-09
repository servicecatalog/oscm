/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 24.04.17 15:08
 *
 ******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.oscm.internal.vo.VOSubscriptionUsageEntry;
import org.oscm.operatorsvc.client.CommandContext;

/**
 * @author tokoda
 * 
 */
public class GetSubscriptionUsage extends GetUserOperationLogCommand {

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_FILE_NAME);
    }

    @Override
    public String getDescription() {
        return "Returns generated a CSV report.\n"
                + "Each line in the report represents one subscription and consists of:\n"
                + "\n" + "-    Customer organization id\n"
                + "-    Customer organization name\n"
                + "-    Subscription name\n" + "-    Marketable service name\n"
                + "-    Technical service name\n"
                + "-    Supplier organization id\n"
                + "-    Supplier organization name\n" + "-    Number of users\n"
                + "-    Number of VMs ";
    }

    @Override
    public String getName() {
        return "getsubscriptionusage";
    }

    @Override
    public boolean run(CommandContext ctx) throws Exception {
        // read parameters
        String outputFileName = ctx.getString(ARG_FILE_NAME);
        String validateError = validateFileName(outputFileName);
        if (validateError != null) {
            ctx.err().print(validateError);
            ctx.err().flush();
            return false;
        }
        Collection<VOSubscriptionUsageEntry> entries = ctx.getService()
                .getSubscriptionUsageReport();

        StringBuilder csv = new StringBuilder();
        for (VOSubscriptionUsageEntry entry : entries) {
            csv.append(toCSV(entry));
        }
        return writeResults(ctx, outputFileName, csv.toString());
    }

    private StringBuffer toCSV(VOSubscriptionUsageEntry entry) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator());
        StringBuffer appendable = new StringBuffer();
        CSVPrinter csvFilePrinter = new CSVPrinter(appendable, csvFileFormat);
        List<String> columns = toList(entry);
        csvFilePrinter.printRecord(columns);
        csvFilePrinter.close();
        return appendable;
    }

    private List<String> toList(VOSubscriptionUsageEntry entry) {
        List<String> columns = new ArrayList<>();
        columns.add(entry.getCustomerOrgId());
        columns.add(entry.getCustomerOrgName());
        columns.add(entry.getSubscriptionName());
        columns.add(entry.getMarketableServiceName());
        columns.add(entry.getTechnicalServiceName());
        columns.add(entry.getSupplierOrganizationName());
        columns.add(entry.getSupplierOrganizationId());
        columns.add(entry.getNumberOfusers());
        columns.add(entry.getNumberOfVMs());
        return columns;
    }

}
