/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 30, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.upgrade.info;

/**
 * @author qiu
 * 
 */
public class UpgradeInfoVersion1 implements UpgradeInfoGenerator {

    @Override
    public VORecords generateVORecords() {
        return null;
    }

    @Override
    public ServiceRecords generateRequestRecords() {
        // ServiceRecords r = new ServiceRecords();
        //
        // Map<ServiceInfo, List<ModificationDetail>> recordsMap = new
        // HashMap<ServiceInfo, List<ModificationDetail>>();
        // ServiceInfo info1 = new ServiceInfo("IdentityService",
        // "getCurrentUserDetailsNew");
        // List<ModificationDetail> de1 = new ArrayList<ModificationDetail>();
        // UpdateDetail m1 = new UpdateDetail(ModificationType.UPDATE,
        // ModificationPart.METHOD, null, null, true,
        // "getCurrentUserDetails", info1.getMethodName(), true);
        // de1.add(m1);
        //
        // ServiceInfo info2 = new ServiceInfo("IdentityService", "createUser");
        // List<ModificationDetail> de2 = new ArrayList<ModificationDetail>();
        // VariableInfo oldv = new VariableInfo("", "usernew");
        // VariableInfo newv = new VariableInfo("", "user");
        // UpdateDetail m2 = new UpdateDetail(ModificationType.UPDATE,
        // ModificationPart.PARAMETER, newv, oldv, false, "", "", true);
        //
        // VariableInfo vinfo = new VariableInfo("", "user");
        // List<FieldInfo> fs = new ArrayList<FieldInfo>();
        // FieldInfo f1 = new FieldInfo();
        // f1.setNewField(new VariableInfo("", "locale"));
        // f1.setOldField(new VariableInfo("", "localeNew"));
        // fs.add(f1);
        // UpdateFieldDetail m3 = new UpdateFieldDetail(
        // ModificationType.UPDATEFIELD, vinfo,
        // ModificationPart.PARAMETER, fs);
        //
        // de2.add(m2);
        // de2.add(m3);
        //
        // recordsMap.put(info1, de1);
        // recordsMap.put(info2, de2);
        //
        // r.setRecordsMap(recordsMap);
        // return r;

        return null;
    }

    @Override
    public ServiceRecords generateResponseRecords() {
        // ServiceRecords r = new ServiceRecords();
        //
        // Map<ServiceInfo, List<ModificationDetail>> recordsMap = new
        // HashMap<ServiceInfo, List<ModificationDetail>>();
        // ServiceInfo info = new ServiceInfo("IdentityService",
        // "getCurrentUserDetailsNew");
        // List<FieldInfo> fs = new ArrayList<FieldInfo>();
        // FieldInfo f1 = new FieldInfo();
        // f1.setNewField(new VariableInfo("", "locale"));
        // f1.setOldField(new VariableInfo("", "localeNew"));
        // fs.add(f1);
        //
        // List<ModificationDetail> de = new ArrayList<ModificationDetail>();
        // UpdateFieldDetail m1 = new UpdateFieldDetail(
        // ModificationType.UPDATEFIELD, null,
        // ModificationPart.RETURNVALUE, fs);
        //
        // de.add(m1);
        //
        // UpdateDetail m2 = new UpdateDetail(ModificationType.UPDATE,
        // ModificationPart.METHOD, null, null, true,
        // "getCurrentUserDetailsNew", "getCurrentUserDetails", false);
        // de.add(m2);
        //
        // recordsMap.put(info, de);
        //
        // ServiceInfo info2 = new ServiceInfo("IdentityService", "createUser");
        // List<FieldInfo> fs2 = new ArrayList<FieldInfo>();
        // FieldInfo f2 = new FieldInfo();
        // f2.setNewField(new VariableInfo("", "locale"));
        // f2.setOldField(new VariableInfo("", "localeNew"));
        // fs2.add(f2);
        //
        // List<ModificationDetail> de2 = new ArrayList<ModificationDetail>();
        // UpdateFieldDetail m3 = new UpdateFieldDetail(
        // ModificationType.UPDATEFIELD, null,
        // ModificationPart.RETURNVALUE, fs2);
        //
        // de2.add(m3);
        // recordsMap.put(info2, de2);
        //
        // recordsMap.put(info, de);
        //
        // r.setRecordsMap(recordsMap);
        // return r;

        return null;
    }

    @Override
    public ServiceRecords generateExceptionRecords() {

        // ServiceRecords r = new ServiceRecords();
        //
        // Map<ServiceInfo, List<ModificationDetail>> recordsMap = new
        // HashMap<ServiceInfo, List<ModificationDetail>>();
        // ServiceInfo info = new ServiceInfo("IdentityService",
        // "lockUserAccount");
        //
        // List<ModificationDetail> de = new ArrayList<ModificationDetail>();
        // ExceptionDetail m1 = new
        // ExceptionDetail(ModificationType.ADDEXCEPTION,
        // ModificationPart.EXCEPTION, null, "ObjectNotFoundException");
        //
        // de.add(m1);
        //
        // recordsMap.put(info, de);
        //
        // r.setRecordsMap(recordsMap);
        // return r;

        return null;
    }

}
