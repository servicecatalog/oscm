/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.model;

import java.text.DecimalFormat;

import org.oscm.app.vmware.business.VMwareValue;
import org.oscm.app.vmware.business.VMwareValue.Unit;

/**
 * Implements a storage in the VMware server structure
 * <p>
 * One storage can be assigned to one or more ESX hosts.
 *
 * @author soehnges
 */
public class VMwareStorage {

    public static final VMwareValue DEFAULT_STORAGE_LIMIT = VMwareValue
            .parse("90%");

    private static final DecimalFormat DF = new DecimalFormat("#0.##");

    private String name;
    private boolean enabled;
    private VMwareValue limit;
    private double capacity;
    private double free;

    public double getLimit() {
        double storageLimit = limit.getValue(Unit.MB);
        if (limit.isRelative()) {
            storageLimit = capacity * storageLimit;
        } else if (storageLimit < 0) {
            storageLimit = capacity + storageLimit;
        }
        return Math.max(0, storageLimit);
    }

    public void setLimit(VMwareValue limit) {
        this.limit = (limit == null ? DEFAULT_STORAGE_LIMIT : limit);
    }

    public void setCapacity(VMwareValue capacity) {
        this.capacity = capacity == null ? 0 : capacity.getValue(Unit.MB);
    }

    public void setFreeStorage(VMwareValue freeStorage) {
        this.free = freeStorage == null ? 0 : freeStorage.getValue(Unit.MB);
    }

    public boolean checkLimit(double requestedMegaBytes) {
        if (requestedMegaBytes < 0) {
            throw new IllegalArgumentException(
                    "Cannot request negative storage");
        }
        return (capacity - free + requestedMegaBytes) <= getLimit();
    }

    public double getCapacity() {
        return capacity;
    }

    public double getFree() {
        return free;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getLevel() {
        return (capacity > 0 && free >= 0) ? 1 - (free / capacity) : 1;
    }

    @Override
    public String toString() {
        return getName() + " [Cap:" + toGbString(capacity) + "GB|Used:"
                + toGbString(capacity - free) + "GB|Level:"
                + DF.format(getLevel() * 100) + "%|Limit:"
                + toGbString(getLimit()) + "GB]";
    }

    private String toGbString(double mbValue) {
        return DF.format(VMwareValue.fromMegaBytes(mbValue).getValue(Unit.GB));
    }
}
