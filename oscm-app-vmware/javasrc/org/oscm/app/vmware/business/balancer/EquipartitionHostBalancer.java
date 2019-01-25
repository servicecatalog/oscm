/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import java.util.ArrayList;
import java.util.List;

import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.model.VMwareHost;
import org.oscm.app.vmware.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * Balancer implementation determining the best host for deployment by
 * statistical calculation based on usage data.
 * 
 * @author Dirk Bernsau
 * 
 */
public class EquipartitionHostBalancer extends HostBalancer {

    private static final Logger logger = LoggerFactory
            .getLogger(EquipartitionHostBalancer.class);

    private double cpuWeight = 1;
    private double memWeight = 1;
    private double vmWeight = 1;

    @Override
    public void setConfiguration(Node xmlConfig) {
        super.setConfiguration(xmlConfig);
        if (xmlConfig != null) {
            try {
                cpuWeight = Double.parseDouble(XMLHelper.getAttributeValue(xmlConfig, 
                        "cpuWeight", "1"));
            } catch (NullPointerException e) {
                // ignore
            } catch (NumberFormatException e) {
                // ignore
            }
            try {
                memWeight = Double.parseDouble(XMLHelper.getAttributeValue(xmlConfig, 
                        "memoryWeight", "1"));
            } catch (NullPointerException e) {
                // ignore
            } catch (NumberFormatException e) {
                // ignore
            }
            try {
                vmWeight = Double.parseDouble(XMLHelper.getAttributeValue(xmlConfig, 
                        "vmWeight", "1"));
            } catch (NullPointerException e) {
                // ignore
            } catch (NumberFormatException e) {
                // ignore
            }
        }
    }

    @Override
    public VMwareHost next(VMPropertyHandler properties)
            throws APPlatformException {

        List<VMwareHost> validHosts = new ArrayList<VMwareHost>();
        for (VMwareHost host : getElements()) {
            if (isValid(host, properties)) {
                validHosts.add(host);
            }
        }
        int cnt = validHosts.size();
        if (cnt == 0) {
            logger.debug("No valid host available");
            return null;
        }
        double[] vmCounts = new double[cnt];
        double[] memCounts = new double[cnt];
        double[] cpuCounts = new double[cnt];
        double[] vmNormalizer = new double[cnt];
        double[] memNormalizer = new double[cnt];
        double[] cpuNormalizer = new double[cnt];
        int i = 0;
        for (VMwareHost host : validHosts) {
            logger.debug("Add host for balancer: " + host.getName() + " "
                    + host.getAllocationAsString());
            memCounts[i] = host.getAllocatedMemoryMB();
            memNormalizer[i] = host.getMemorySizeMB();
            cpuCounts[i] = host.getAllocatedCPUs();
            cpuNormalizer[i] = host.getCpuCores();
            vmCounts[i] = host.getAllocatedVMs();
            vmNormalizer[i] = 1;
            i++;
        }
        memCounts = calculateSpread(memCounts, properties.getConfigMemoryMB(),
                memNormalizer);
        cpuCounts = calculateSpread(cpuCounts, properties.getConfigCPUs(),
                cpuNormalizer);
        vmCounts = calculateSpread(vmCounts, 1, vmNormalizer);
        if (logger.isDebugEnabled()) {
            logger.debug("Memory spread: " + getLogString(memCounts));
            logger.debug("CPU spread:    " + getLogString(cpuCounts));
            logger.debug("VM spread:     " + getLogString(vmCounts));
        }
        int indexOfHost = assess(new double[][] { memCounts, cpuCounts,
                vmCounts }, new double[] { memWeight, cpuWeight, vmWeight });
        try {
            return validHosts.get(indexOfHost);
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Host balancing resulted in invalid host index "
                    + indexOfHost);
        }

        throw new APPlatformException(Messages.getAll("error_outof_host"));
    }

    /**
     * Calculates an array of spread values while applying the addition once to
     * each of the given values.
     * <p>
     * The first element of the result then contains
     * <code>spread(v1+a, v2, ...)</code>, the second contains
     * <code>spread(v1, v2+a, ...)</code> etc.
     * <p>
     * If the normalizer values are set, the respective value and the addition
     * will be normalized (divided by the normalizer value) before calculating
     * the spread.
     * 
     * @param values
     *            base value set for the calculation
     * @param addition
     *            the additional value applied to each basic value once
     * @param normalizer
     *            the set of norms
     * @return the calculated set of spread values
     */
    public static double[] calculateSpread(double[] values, double addition,
            double[] normalizer) {
        double[] result = new double[values.length];
        double[] calcBase = new double[values.length];
        if (values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                // no normalizer => set to 100%
                double factor = normalizer.length >= i + 1 ? normalizer[i] : 1;
                calcBase[i] = values[i] / (factor > 0 ? factor : 1);
            }
            for (int i = 0; i < calcBase.length; i++) {
                double factor = normalizer.length >= i + 1 ? normalizer[i] : 1;
                double normalizedAddition = addition
                        / (factor > 0 ? factor : 1);
                calcBase[i] += normalizedAddition;
                result[i] = spread(calcBase);
                calcBase[i] -= normalizedAddition;
            }
        }
        return result;
    }

    /**
     * Calculates the average from the given set of values.
     */
    private static double average(double[] values) {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        return sum / values.length;
    }

    /**
     * Calculates the spread over the given set of values and normalizes the
     * result as percentage of the average.
     */
    private static double spread(double[] values) {
        double sum = 0;
        double avg = average(values);
        for (int i = 0; i < values.length; i++) {
            sum += square(avg - values[i]);
        }
        return Math.sqrt(sum / values.length) / (avg != 0 ? avg : 1);
    }

    /**
     * Calculate square of given value.
     */
    private static double square(double value) {
        return value * value;
    }

    /**
     * @param values
     *            an array of value sets
     * @param weights
     *            the weights to be applied to the calculation
     * @return the index of the value set that has the least value spread
     */
    public static int assess(double[][] values, double[] weights) {
        int hostCount = -1;
        int resultIndex = -1; // index of lowest weighted spread
        double lowestSpread = -1;
        double[] internalWeigths = new double[values.length];
        // check all values lines for same host count
        for (int i = 0; i < values.length; i++) {
            if (hostCount == -1) {
                hostCount = values[i].length;
            } else if (hostCount != values[i].length) {
                // should never occur
                throw new IllegalStateException(
                        "Comparing systems with unequal number of attributes.");
            }
            if (i < weights.length) {
                internalWeigths[i] = weights[i];
            } else {
                internalWeigths[i] = 1;
            }
        }
        StringBuffer weightedValues = new StringBuffer();
        // columns in array represent hosts
        for (int hostIndex = 0; hostIndex < hostCount; hostIndex++) {
            double weightedSpread = 0;
            // iterate over attributes like CPU, memory etc.
            for (int hostAttribute = 0; hostAttribute < values.length; hostAttribute++) {
                weightedSpread += values[hostAttribute][hostIndex]
                        * internalWeigths[hostAttribute];
            }
            weightedValues.append((hostIndex > 0 ? ", " : "[")).append(
                    weightedSpread);
            if (resultIndex < 0 || weightedSpread < lowestSpread) {
                resultIndex = hostIndex;
                lowestSpread = weightedSpread;
            }
        }
        if (logger.isDebugEnabled()) {
            weightedValues.append("]");
            logger.debug("Weighted spreads for hosts: "
                    + weightedValues.toString());
        }
        return resultIndex;
    }

    private static String getLogString(double[] values) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
            sb.append((i > 0 ? ", " : "[")).append(values[i]);
        }
        sb.append("]");
        return sb.toString();
    }

}