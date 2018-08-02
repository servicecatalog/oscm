/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: 02.08.2018                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.vmware.business;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Matchers;
import org.oscm.app.v2_0.exceptions.APPlatformException;

import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.GuestNicInfo;

/**
 * Unit tests for VMwareAccessInfo.
 * 
 * @author goebel
 */
public class VMwareAccessInfoTest {

    private VMPropertyHandler ph = mock(VMPropertyHandler.class);

    private static final String PATTERN_IP = "IP: ${IP}";
    private static final String PATTERN_HOST = "HOST: ${HOST}";
    private static final String PATTERN_CPU = "CPU: ${CPU}";
    private static final String PATTERN_MEM = "MEM: ${MEM}";
    private static final String PATTERN_DISKS = "DISKS: ${DISK}";
    private static final String PATTERN_RESPUSER = "USER: ${RESPUSER}";

    private Long MEMORY = new Long("32");

    private final String[] NETWORK1_IPS = new String[] { "10.124.3.1",
            "10.124.3.2" };
    private final String[] NETWORK2_IPS = new String[] { "10.124.2.1",
            "10.124.2.2" };

    @Test
    public void generateAccessInfo() throws APPlatformException {
        // given
        GuestInfo gi = mock(GuestInfo.class);
        VMwareAccessInfo accInfo = givenAccessInfo(gi);
        givenAccessInfoPatterns(PATTERN_HOST, PATTERN_IP);

        // when
        String info = accInfo.generateAccessInfo(gi);

        // then
        assertNotNull(info);

        assertHost(info);
        assertNet1IPs(info);
        assertNet2IPs(info);
    }

    @Test
    public void generateAccessInfo_CPUs() throws APPlatformException {
        // given
        GuestInfo gi = mock(GuestInfo.class);
        VMwareAccessInfo accInfo = givenAccessInfo(gi);
        givenAccessInfoPatterns(PATTERN_CPU);

        // when
        String info = accInfo.generateAccessInfo(gi);

        // then
        assertNotNull(info);

        assertOneCPU(info);
    }

    @Test
    public void generateAccessInfo_Mem() throws APPlatformException {
        // given
        GuestInfo gi = mock(GuestInfo.class);
        VMwareAccessInfo accInfo = givenAccessInfo(gi);
        givenAccessInfoPatterns(PATTERN_DISKS, PATTERN_MEM);

        // when
        String info = accInfo.generateAccessInfo(gi);

        // then
        assertNotNull(info);

        assertMem(info);
    }

    @Test
    public void generateAccessInfo_Disks() throws APPlatformException {
        // given
        GuestInfo gi = mock(GuestInfo.class);
        VMwareAccessInfo accInfo = givenAccessInfo(gi);
        givenAccessInfoPatterns(PATTERN_DISKS, PATTERN_MEM);

        // when
        String info = accInfo.generateAccessInfo(gi);

        // then
        assertNotNull(info);

        assertDisks(info);
    }

    @Test
    public void generateAccessInfo_User() throws APPlatformException {
        // given
        GuestInfo gi = mock(GuestInfo.class);
        VMwareAccessInfo accInfo = givenAccessInfo(gi);
        givenAccessInfoPatterns(PATTERN_RESPUSER);

        // when
        String info = accInfo.generateAccessInfo(gi);

        // then
        assertNotNull(info);

        assertUser(info);

    }

    @Test
    public void generateAccessInfo_UndifinedHost() throws APPlatformException {
        // given
        GuestInfo gi = mock(GuestInfo.class);
        VMwareAccessInfo accInfo = givenNetworkWithUndefinedHost(gi);

        // when
        String info = accInfo.generateAccessInfo(gi);

        // then
        assertHostUndefined(info);

    }
    
    @Test
    public void generateAccessInfo_MissingPattern() throws APPlatformException {
        // given
        GuestInfo gi = mock(GuestInfo.class);
        VMwareAccessInfo accInfo = givenAccessInfoWithoutPattern(gi);
       
        // when
        String info = accInfo.generateAccessInfo(gi);

        // then
        assertMissingPattern(info);

    }
 
    private VMwareAccessInfo givenNetworkWithUndefinedHost(GuestInfo gi)
            throws APPlatformException {
        VMwareAccessInfo accInfo = givenAccessInfo(gi);
        givenUndefinedHost(gi);
        givenAccessInfoPatterns(PATTERN_HOST);
        return accInfo;
    }
 
    
    private VMwareAccessInfo givenAccessInfoWithoutPattern(GuestInfo gi)
            throws APPlatformException {
        VMwareAccessInfo accInfo = givenAccessInfo(gi);
        doReturn("").when(ph).getAccessInfo();
        return accInfo;
    }
 
 

    private void givenAccessInfoPatterns(String... patterns) {
        StringBuffer sb = new StringBuffer();
        for (String pattern : patterns) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(pattern);
        }
        String accPat = sb.toString();
        doReturn(accPat).when(ph).getAccessInfo();
    }

    private void assertUser(String info) {
        assertTrue(info, info.indexOf("USER: admin") >= 0);

    }

    private void assertDisks(String info) {
        assertTrue(info, info.indexOf("DISKS: 20.0 GB/5.0 GB") >= 0);
    }

    private void assertHost(String info) {
        assertTrue(info, info.indexOf("testserver.netdomain.com") >= 0);
    }

    private void assertHostUndefined(String info) {
        assertNotNull(info);
        assertTrue(info, info.indexOf("HOST: Unknown") >= 0);
    }

    private void assertOneCPU(String info) {
        assertTrue(info, info.indexOf("CPU: 1") >= 0);
    }

    private void assertNet1IPs(String info) {
        assertTrue(info, info.indexOf(NETWORK1_IPS[0]) >= 0);
        assertTrue(info, info.indexOf(NETWORK1_IPS[1]) >= 0);
    }

    private void assertNet2IPs(String info) {
        assertTrue(info, info.indexOf(NETWORK2_IPS[0]) >= 0);
        assertTrue(info, info.indexOf(NETWORK2_IPS[1]) >= 0);
    }

    private void assertMem(String info) {
        assertTrue(info, info.indexOf("MEM: " + MEMORY + " GB") >= 0);
    }
    
    private void assertMissingPattern(String info) {
        assertTrue(info, info.indexOf("testserver") >= 0);
    }


    private void givenUndefinedHost(GuestInfo gi) {
        doReturn(null).when(gi).getHostName();
    }

    @SuppressWarnings("boxing")
    private VMwareAccessInfo givenAccessInfo(GuestInfo gi)
            throws APPlatformException {

        doReturn(1).when(ph).getConfigCPUs();

        doReturn(MEMORY).when(ph).getConfigMemoryMB();

        doReturn(MEMORY + " GB").when(ph).formatMBasGB(Matchers.anyDouble());

        doReturn("20.0 GB/5.0 GB").when(ph).getDataDisksAsString();
        doReturn("admin").when(ph)
                .getResponsibleUserAsString(Matchers.anyString());

        doReturn(2).when(ph).getNumberOfNetworkAdapter();
        doReturn("vm-instance-1").when(ph).getInstanceName();
        VMwareAccessInfo accInfo = new VMwareAccessInfo(ph);

        doReturn("VM Network1").when(ph).getNetworkAdapter(1);
        doReturn("VM Network2").when(ph).getNetworkAdapter(2);

        doReturn("10.124.1.1").when(gi).getIpAddress();
        doReturn("testserver.netdomain.com").when(gi).getHostName();

        List<GuestNicInfo> nics = new ArrayList<GuestNicInfo>();
        GuestNicInfo guestInfo = mock(GuestNicInfo.class);
        doReturn("VM Network1").when(guestInfo).getNetwork();

        List<String> ips = new ArrayList<String>();
        ips.add(NETWORK1_IPS[0]);
        ips.add(NETWORK1_IPS[1]);
        doReturn(ips).when(guestInfo).getIpAddress();
        nics.add(guestInfo);

        GuestNicInfo guestInfo2 = mock(GuestNicInfo.class);

        doReturn("VM Network2").when(guestInfo2).getNetwork();
        List<String> ips2 = new ArrayList<String>();
        ips2.add(NETWORK2_IPS[0]);
        ips2.add(NETWORK2_IPS[1]);
        doReturn(ips2).when(guestInfo2).getIpAddress();
        nics.add(guestInfo2);

        doReturn(nics).when(gi).getNet();
        return accInfo;
    }
}
