/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import java.text.DecimalFormat;

/**
 * @author Dirk Bernsau
 *
 */
public class VMwareValue {

    private static final DecimalFormat DF = new DecimalFormat("#0.##");

    public enum Unit {
        BY(0), KB(1), MB(2), GB(3), TB(4), PB(5);
        private final int factor;

        private Unit(int factor) {
            this.factor = factor;
        }
    }

    private boolean isRelative;
    private double value = 0;
    private Unit unit;

    public static VMwareValue parse(String expression) {
        if (expression == null) {
            return null;
        }
        VMwareValue result = new VMwareValue();
        expression = expression.replaceAll(" ", "").replaceAll(",", ".");
        expression = expression.toUpperCase();
        if (expression.endsWith("%")) {
            try {
                // Parse percentage value
                long limitPercentage = Long.parseLong(
                        expression.substring(0, expression.length() - 1));
                if (limitPercentage >= 0) {
                    result.value = limitPercentage / 100.0;
                    result.isRelative = true;
                    return result;
                }
            } catch (NumberFormatException e) {
                // Ignore, will be handled below...
            }
        }
        if (expression.length() > 2) {
            String end = expression.substring(expression.length() - 2);
            try {
                result.unit = Unit.valueOf(end);
                expression = expression.substring(0, expression.length() - 2);
            } catch (IllegalArgumentException e) {
                // Ignore, will be handled below...
            }
        }
        try {
            double limit = Double.parseDouble(expression);
            result.value = limit;
            result.isRelative = false;
            return result;
        } catch (NumberFormatException e) {
            // Ignore, will be handled below...
        }

        throw new IllegalArgumentException(
                "Specified limit is invalid or out of range: " + expression);
    }

    public static VMwareValue fromBytes(long value) {
        VMwareValue result = new VMwareValue();
        result.isRelative = false;
        result.value = ((double) value / 1024);
        result.unit = Unit.KB;
        return result;
    }

    public static VMwareValue fromMegaBytes(double value) {
        VMwareValue result = new VMwareValue();
        result.isRelative = false;
        result.value = value;
        result.unit = Unit.MB;
        return result;
    }

    public static VMwareValue fromGigaBytes(double value) {
        VMwareValue result = new VMwareValue();
        result.isRelative = false;
        result.value = value;
        result.unit = Unit.GB;
        return result;
    }

    public boolean isRelative() {
        return isRelative;
    }

    public double getValue() {
        return value;
    }

    public double getValue(Unit targetUnit) {
        if (targetUnit != null && unit != null) {
            double pow = Math.pow(1024, (unit.factor - targetUnit.factor));
            return value * pow;
        }
        return value;
    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return (isRelative ? DF.format(value * 100) + "%"
                : DF.format(value) + (unit != null ? unit.name() : ""));
    }
}
