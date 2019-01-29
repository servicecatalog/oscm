/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.model;

import java.text.DecimalFormat;

import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.VMwareValue;
import org.oscm.app.vmware.business.VMwareValue.Unit;
import org.oscm.app.vmware.business.balancer.DynamicEquipartitionStorageBalancer;
import org.oscm.app.vmware.business.balancer.VMwareBalancer;

/**
 * Implements one ESX host in the VMware server structure.
 * 
 * @author soehnges
 */
public class VMwareHost {

    private static final VMwareValue DEFAULT_CPU_LIMIT = VMwareValue
            .parse("500%");
    private static final VMwareValue DEFAULT_MEMORY_LIMIT = VMwareValue
            .parse("-1GB");
    private static final VMwareValue DEFAULT_VM_LIMIT = VMwareValue.parse("40");

    private static final DecimalFormat DF = new DecimalFormat("#0.##");

    private String name;
    private boolean enabled;
    private int cpuCores;
    private double memorySize;
    private int allocatedVMs;
    private int allocatedCPUs;
    private long allocatedMemory;

    private VMwareValue vmLimit = DEFAULT_VM_LIMIT;
    private VMwareValue cpuLimit = DEFAULT_CPU_LIMIT;
    private VMwareValue memoryLimit = DEFAULT_MEMORY_LIMIT;

    private VMwareBalancer<VMwareStorage> balancer;

    public VMwareHost(VMwareDatacenterInventory inventory) {
        balancer = new DynamicEquipartitionStorageBalancer();
        balancer.setInventory(inventory);
    }

    public VMwareStorage getNextStorage(VMPropertyHandler properties)
            throws APPlatformException {

        VMwareStorage storage = balancer.next(properties);
        return storage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean checkVmLimit() {
        return allocatedVMs < getVmLimit();
    }

    public double getVmLimit() {
        return Math.max(0, vmLimit.getValue());
    }

    public boolean checkCpuLimit(int requestedCores) {
        if (requestedCores < 0) {
            throw new IllegalArgumentException(
                    "Cannot request negative CPU cores");
        }
        return (allocatedCPUs + requestedCores) <= getCpuLimit();
    }

    public double getCpuLimit() {
        double cpu = cpuLimit.getValue();
        if (cpuLimit.isRelative()) {
            cpu = cpuCores * cpu;
        } else if (cpu < 0) {
            cpu = cpuCores + cpu;
        }
        return Math.max(0, cpu);
    }

    public boolean checkMemoryLimit(long requestedMegaBytes) {
        if (requestedMegaBytes < 0) {
            throw new IllegalArgumentException("Cannot request negative memory");
        }
        return (allocatedMemory + requestedMegaBytes) <= getMemoryLimit();
    }

    public double getMemoryLimit() {
        double memLimit = memoryLimit.getValue(Unit.MB);
        if (memoryLimit.isRelative()) {
            memLimit = memorySize * memLimit;
        } else if (memLimit < 0) {
            memLimit = memorySize + memLimit;
        }
        return Math.max(0, memLimit);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public VMwareBalancer<VMwareStorage> getBalancer() {
        return balancer;
    }

    public void setBalancer(VMwareBalancer<VMwareStorage> balancer) {
        this.balancer = balancer;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }

    public int getAllocatedCPUs() {
        return allocatedCPUs;
    }

    public void setAllocatedCPUs(int allocatedCPUs) {
        this.allocatedCPUs = allocatedCPUs;
    }

    public double getMemorySizeMB() {
        return memorySize;
    }

    public void setMemorySizeMB(double memorySize) {
        this.memorySize = memorySize;
    }

    public long getAllocatedMemoryMB() {
        return allocatedMemory;
    }

    public void setAllocatedMemoryMB(long allocatedMemory) {
        this.allocatedMemory = allocatedMemory;
    }

    public int getAllocatedVMs() {
        return allocatedVMs;
    }

    public void setAllocatedVMs(int allocatedVMs) {
        this.allocatedVMs = allocatedVMs;
    }

    public void setVMLimit(VMwareValue vmLimit) {
        this.vmLimit = (vmLimit != null ? vmLimit : DEFAULT_VM_LIMIT);
    }

    public void setCPULimit(VMwareValue cpuLimit) {
        this.cpuLimit = (cpuLimit != null ? cpuLimit : DEFAULT_CPU_LIMIT);
    }

    public void setMemoryLimit(VMwareValue memoryLimit) {
        this.memoryLimit = (memoryLimit != null ? memoryLimit
                : DEFAULT_MEMORY_LIMIT);
    }

    /**
     * Creates a human-readable string representation of the hosts limit
     * settings.
     */
    public String getLimitsAsString() {
        StringBuffer sb = new StringBuffer("[Mem:");
        double memory = getMemoryLimit();
        VMwareValue memL = VMwareValue.parse(DF.format(memory) + "MB");
        sb.append(memory >= 1024 ? DF.format(memL.getValue(Unit.GB)) + "GB"
                : DF.format(memL.getValue(Unit.MB)) + "MB");
        sb.append("|CPU:").append(DF.format(getCpuLimit()));
        sb.append("|VM:").append(DF.format(getVmLimit()));
        sb.append("]");
        return sb.toString();
    }

    /**
     * Creates a human-readable string representation of the hosts allocation
     * data.
     */
    public String getAllocationAsString() {
        StringBuffer sb = new StringBuffer("[Mem:");
        VMwareValue memL = VMwareValue.parse(allocatedMemory + "MB");
        sb.append(allocatedMemory >= 1024 ? DF.format(memL.getValue(Unit.GB))
                + "GB" : DF.format(memL.getValue(Unit.MB)) + "MB");
        sb.append("|CPU:").append(allocatedCPUs);
        sb.append("|VM:").append(allocatedVMs);
        sb.append("]");
        return sb.toString();
    }
}
