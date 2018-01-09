/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.oscm.app.vmware.business.VMwareValue.Unit;
import org.oscm.app.vmware.business.model.VMwareHost;

/**
 * Unit tests for the VMwareLimit.
 *
 * @author Dirk Bernsau
 *
 */
public class VMwareValueTest {

    @Test
    public void testLimitPercentage() throws Exception {
        VMwareValue limit = VMwareValue.parse("90%");
        assertTrue(limit.isRelative());
        assertTrue(0.9 == limit.getValue());
    }

    @Test
    public void testLimitPercentage_large() throws Exception {
        VMwareValue limit = VMwareValue.parse("500%");
        assertTrue(limit.isRelative());
        assertTrue(5 == limit.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLimitPercentage_NaN() throws Exception {
        VMwareValue.parse("as%");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLimitPercentage_empty() throws Exception {
        VMwareValue.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLimitPercentage_Negative() throws Exception {
        VMwareValue.parse("-45%");
    }

    @Test
    public void testLimitAbs1() throws Exception {
        VMwareValue limit = VMwareValue.parse("1");
        assertFalse(limit.isRelative());
        assertTrue(1 == limit.getValue());
    }

    @Test
    public void testLimitAbs0() throws Exception {
        VMwareValue limit = VMwareValue.parse("0");
        assertFalse(limit.isRelative());
        assertTrue(0 == limit.getValue());
    }

    @Test
    public void testLimitAbsNeg8() throws Exception {
        VMwareValue limit = VMwareValue.parse("-8");
        assertFalse(limit.isRelative());
        assertTrue(-8 == limit.getValue());
    }

    @Test
    public void testLimit_UnitNeg() throws Exception {
        VMwareValue limit = VMwareValue.parse("-8GB");
        assertFalse(limit.isRelative());
        assertTrue(-8 == limit.getValue());
        assertEquals(Unit.GB, limit.getUnit());
    }

    @Test
    public void testLimit_Unit() throws Exception {
        VMwareValue limit = VMwareValue.parse("20tb");
        assertFalse(limit.isRelative());
        assertTrue(20 == limit.getValue());
        assertEquals(Unit.TB, limit.getUnit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLimit_UnitUnknown() throws Exception {
        VMwareValue.parse("20iB");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLimitTrash() throws Exception {
        VMwareValue.parse("trash");
    }

    @Test
    public void testLimitCheck_absoluteCPU() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "1024", "4"));
        host.setAllocatedCPUs(10);
        host.setCPULimit(VMwareValue.parse("12"));

        assertFalse(host.checkCpuLimit(4));
        assertTrue(host.checkCpuLimit(2));
    }

    @Test
    public void testLimitCheck_relativeCPU() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "1024", "4"));
        host.setAllocatedCPUs(10);
        host.setCPULimit(VMwareValue.parse("300%"));

        assertFalse(host.checkCpuLimit(4));
        assertTrue(host.checkCpuLimit(2));
        // 0 is something like a status check
        assertTrue(host.checkCpuLimit(0));
    }

    @Test
    public void testLimitCheck_negativeCPU() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "1024", "10"));
        host.setAllocatedCPUs(6);
        host.setCPULimit(VMwareValue.parse("-2"));

        assertFalse(host.checkCpuLimit(4));
        assertTrue(host.checkCpuLimit(2));
        // 0 is something like a status check
        assertTrue(host.checkCpuLimit(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLimitCheck_invalidCPU() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "1024", "4"));
        host.setAllocatedCPUs(10);
        host.checkCpuLimit(-1);
    }

    @Test
    public void testLimitCheck_defautltCPU() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "1024", "1"));
        host.setAllocatedCPUs(5);
        assertTrue(host.checkCpuLimit(0));
        assertFalse(host.checkCpuLimit(1));
    }

    @Test
    public void testLimitCheck_VMs_OK() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "1024", "4"));
        host.setAllocatedVMs(9);
        host.setVMLimit(VMwareValue.parse("10"));

        assertTrue(host.checkVmLimit());
    }

    @Test
    public void testLimitCheck_VMs_NOK() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "1024", "4"));
        host.setAllocatedVMs(12);
        host.setVMLimit(VMwareValue.parse("10"));

        assertFalse(host.checkVmLimit());
    }

    @Test
    public void testLimitCheck_MemAbs() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "1024", "4"));
        host.setAllocatedMemoryMB(512);
        host.setMemoryLimit(VMwareValue.parse("512MB"));

        assertFalse(host.checkMemoryLimit(512));
        assertTrue(host.checkMemoryLimit(0));

        host.setAllocatedMemoryMB(900);
        host.setMemoryLimit(VMwareValue.parse("1GB"));

        assertFalse(host.checkMemoryLimit(512));
        assertFalse(host.checkMemoryLimit(125));
        assertTrue(host.checkMemoryLimit(124));
    }

    @Test
    public void testLimitCheck_MemAbsNeg() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "8192", "4"));
        host.setAllocatedMemoryMB(4096);
        host.setMemoryLimit(VMwareValue.parse("-4GB"));

        assertFalse(host.checkMemoryLimit(1));
        assertTrue(host.checkMemoryLimit(0));
    }

    @Test
    public void testLimitCheck_MemRel() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "8192", "4"));
        host.setAllocatedMemoryMB(4096);
        host.setMemoryLimit(VMwareValue.parse("50%"));

        assertFalse(host.checkMemoryLimit(1));
        assertTrue(host.checkMemoryLimit(0));
    }

    @Test
    public void testLimitAsString() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "8192", "4"));
        host.setMemoryLimit(VMwareValue.parse("8GB"));
        host.setCPULimit(VMwareValue.parse("50%"));
        host.setVMLimit(VMwareValue.parse("50"));
        assertEquals("[Mem:8GB|CPU:2|VM:50]", host.getLimitsAsString());
    }

    @Test
    public void testLimitToString() throws Exception {
        assertEquals("50%", VMwareValue.parse("50%").toString());
        assertEquals("50", VMwareValue.parse("50").toString());
        assertEquals("50GB", VMwareValue.parse("50GB").toString());
        assertEquals("50GB", VMwareValue.parse("50gb").toString());
        assertEquals("50GB", VMwareValue.parse("50 GB ").toString());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testLimitCheck_invalidMem() throws Exception {
        VMwareHost host = new VMwareDatacenterInventory()
                .addHostSystem(VMwareDatacenterInventoryTest
                        .createHostSystemProperties("host", "1024", "4"));
        host.checkMemoryLimit(-1);
    }

    @Test
    public void testUnitCalc() throws Exception {
        assertTrue(1 == VMwareValue.parse("1MB").getValue(Unit.MB));
        assertTrue(1024 == VMwareValue.parse("1GB").getValue(Unit.MB));
        assertTrue(1024 == VMwareValue.parse("1TB").getValue(Unit.GB));
        assertTrue(1024 == VMwareValue.parse("1PB").getValue(Unit.TB));
        assertTrue(1024 * 1024 * 1024 == VMwareValue.parse("1PB")
                .getValue(Unit.MB));
        assertTrue((1d / 1024d) == VMwareValue.parse("1KB").getValue(Unit.MB));
        assertTrue((2d / 1024d / 1024d) == VMwareValue.parse("2KB")
                .getValue(Unit.GB));
        assertTrue(1 == VMwareValue.parse("1").getValue(Unit.MB));
        assertTrue(1 == VMwareValue.parse("1").getValue(null));
        assertTrue(1 == VMwareValue.parse("100%").getValue(Unit.MB));
    }
}
