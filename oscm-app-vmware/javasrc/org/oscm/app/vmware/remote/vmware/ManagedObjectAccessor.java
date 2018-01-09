/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.remote.vmware;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;

/**
 * @author Dirk Bernsau
 *
 */
public class ManagedObjectAccessor {

    private ServiceConnection connection;
    private ServiceContent serviceContent;
    private VimPortType vimPort;

    public ManagedObjectAccessor(ServiceConnection connection) {
        this.connection = connection;
        serviceContent = connection.getServiceContent();
        vimPort = connection.getService();
    }

    /**
     * Retrieve a property from the given object reference.
     *
     * @param mor
     *            the object reference
     * @param propertyName
     *            the name of the property
     * @return the property if present
     * @throws Exception
     */
    public Object getDynamicProperty(ManagedObjectReference mor,
            String propertyName) throws Exception {

        ObjectContent[] objContent = getObjectProperties(mor,
                new String[] { propertyName });

        Object propertyValue = null;
        if (objContent != null) {
            List<DynamicProperty> listdp = objContent[0].getPropSet();
            if (listdp != null && listdp.size() > 0) {
                /*
                 * Check the dynamic property for ArrayOfXXX object
                 */
                Object dynamicPropertyVal = listdp.get(0).getVal();
                String dynamicPropertyName = dynamicPropertyVal.getClass()
                        .getName();
                if (dynamicPropertyName.indexOf("ArrayOf") != -1) {
                    String methodName = dynamicPropertyName.substring(
                            dynamicPropertyName.indexOf("ArrayOf")
                                    + "ArrayOf".length(),
                            dynamicPropertyName.length());
                    /*
                     * If object is ArrayOfXXX object, then get the XXX[] by
                     * invoking getXXX() on the object. For Ex:
                     * ArrayOfManagedObjectReference.getManagedObjectReference()
                     * returns ManagedObjectReference[] array.
                     */
                    if (methodExists(dynamicPropertyVal, "get" + methodName)) {
                        methodName = "get" + methodName;
                    } else {
                        /*
                         * Construct methodName for ArrayOf primitive types Ex:
                         * For ArrayOfInt, methodName is get_int
                         */
                        methodName = "get_" + methodName.toLowerCase();
                    }
                    Method getMorMethod = dynamicPropertyVal.getClass()
                            .getDeclaredMethod(methodName, (Class[]) null);
                    propertyValue = getMorMethod.invoke(dynamicPropertyVal,
                            (Object[]) null);
                } else if (dynamicPropertyVal.getClass().isArray()) {
                    /*
                     * Handle the case of an unwrapped array being deserialized.
                     */
                    propertyValue = dynamicPropertyVal;
                } else {
                    propertyValue = dynamicPropertyVal;
                }
            }
        }
        return propertyValue;
    }

    public List<DynamicProperty> getDynamicProperty(ManagedObjectReference mor,
            String[] propertyNames) throws Exception {
        ObjectContent[] objContent = getObjectProperties(mor, propertyNames);
        if (objContent != null) {
            return objContent[0].getPropSet();
        }
        return null;
    }

    /**
     * Determines if a method 'methodName' without arguments exists for the
     * given object.
     *
     * @param object
     *            the object in question
     * @param methodName
     *            the method name
     * @return <code>true</code> if the method exists, <code>false</code>
     *         otherwise
     */
    private boolean methodExists(Object object, String methodName) {
        boolean exists = false;
        try {
            Method method = object.getClass().getMethod(methodName);
            if (method != null) {
                exists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }

    /**
     * Retrieve contents for a single object based on the property collector
     * registered with the service.
     *
     * @param collector
     *            Property collector registered with service
     * @param mobj
     *            Managed Object Reference to get contents for
     * @param properties
     *            names of properties of object to retrieve
     *
     * @return retrieved object contents
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    private ObjectContent[] getObjectProperties(ManagedObjectReference mobj,
            String[] properties)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        if (mobj == null) {
            return null;
        }

        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().add(new PropertySpec());
        if ((properties == null || properties.length == 0)) {
            spec.getPropSet().get(0).setAll(Boolean.TRUE);
        } else {
            spec.getPropSet().get(0).setAll(Boolean.FALSE);
        }
        spec.getPropSet().get(0).setType(mobj.getType());
        spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(properties));
        spec.getObjectSet().add(new ObjectSpec());
        spec.getObjectSet().get(0).setObj(mobj);
        spec.getObjectSet().get(0).setSkip(Boolean.FALSE);
        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(spec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
        return listobjcont.toArray(new ObjectContent[listobjcont.size()]);
    }

    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method
     *
     * @param filterSpecs
     * @return list of object content
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     * @throws Exception
     */
    private List<ObjectContent> retrievePropertiesAllObjects(
            List<PropertyFilterSpec> filterSpecs)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

        RetrieveOptions retrieveOptions = new RetrieveOptions();
        ManagedObjectReference collector = serviceContent
                .getPropertyCollector();

        List<ObjectContent> contents = new ArrayList<ObjectContent>();

        RetrieveResult results = vimPort.retrievePropertiesEx(collector,
                filterSpecs, retrieveOptions);
        if (results != null && results.getObjects() != null
                && !results.getObjects().isEmpty()) {
            contents.addAll(results.getObjects());
        }
        String token = null;
        if (results != null && results.getToken() != null) {
            token = results.getToken();
        }
        while (token != null && token.length() > 0) {
            results = vimPort.continueRetrievePropertiesEx(collector, token);
            token = null;
            if (results != null) {
                token = results.getToken();
                if (results.getObjects() != null
                        && !results.getObjects().isEmpty()) {
                    contents.addAll(results.getObjects());
                }
            }
        }

        return contents;
    }

    /**
     * Retrieves an object with given name and type.
     *
     * @param object
     *            the starting point for the hierarchical search (
     *            <code>null</code> to start from root folder
     * @param type
     *            the type of object to be searched
     * @param name
     *            the name of the object to be searched
     * @return the object in question or <code>null</code>
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    public ManagedObjectReference getDecendentMoRef(
            ManagedObjectReference object, String type, String name)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        if (object == null) {
            object = connection.getServiceContent().getRootFolder();
        }
        return getMoRefsInContainerByType(object, type).get(name);
    }

    /**
     * Returns all the managed object references of the specified type that are
     * present under the container.
     *
     * @param folder
     *            {@link ManagedObjectReference} of the container to begin the
     *            search from
     * @param morefType
     *            type of the managed entity that needs to be searched
     *
     * @return map of name and MoRef of the managed objects present. May be
     *         empty but not <code>null</code>
     *
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    public Map<String, ManagedObjectReference> getMoRefsInContainerByType(
            ManagedObjectReference folder, String morefType)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

        String PROP_ME_NAME = "name";
        ManagedObjectReference viewManager = serviceContent.getViewManager();
        ManagedObjectReference containerView = vimPort.createContainerView(
                viewManager, folder, Arrays.asList(morefType), true);

        Map<String, ManagedObjectReference> tgtMoref = new HashMap<String, ManagedObjectReference>();

        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.setType(morefType);
        propertySpec.getPathSet().add(PROP_ME_NAME);

        TraversalSpec ts = new TraversalSpec();
        ts.setName("view");
        ts.setPath("view");
        ts.setSkip(Boolean.FALSE);
        ts.setType("ContainerView");

        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(containerView);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().add(ts);

        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
        propertyFilterSpecs.add(propertyFilterSpec);

        RetrieveResult rslts = vimPort.retrievePropertiesEx(
                serviceContent.getPropertyCollector(), propertyFilterSpecs,
                new RetrieveOptions());

        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();
        if (rslts != null && rslts.getObjects() != null
                && !rslts.getObjects().isEmpty()) {
            listobjcontent.addAll(rslts.getObjects());
        }
        String token = null;
        if (rslts != null && rslts.getToken() != null) {
            token = rslts.getToken();
        }
        while (token != null && token.length() > 0) {
            rslts = vimPort.continueRetrievePropertiesEx(
                    serviceContent.getPropertyCollector(), token);
            token = null;
            if (rslts != null) {
                token = rslts.getToken();
                if (rslts.getObjects() != null
                        && !rslts.getObjects().isEmpty()) {
                    listobjcontent.addAll(rslts.getObjects());
                }
            }
        }
        for (ObjectContent oc : listobjcontent) {
            ManagedObjectReference mr = oc.getObj();
            String entityNm = null;
            List<DynamicProperty> dps = oc.getPropSet();
            if (dps != null) {
                for (DynamicProperty dp : dps) {
                    entityNm = (String) dp.getVal();
                }
            }
            tgtMoref.put(entityNm, mr);
        }
        return tgtMoref;
    }

    /**
     * Assembles the property specification required to retrieve inventory
     * information.
     */
    public List<PropertySpec> buildInventoryPropertySpec() {
        PropertySpec pSpecDatacenter = new PropertySpec();
        pSpecDatacenter.setType("Datacenter");
        pSpecDatacenter.getPathSet().add("hostFolder");

        PropertySpec pSpecHost = new PropertySpec();
        pSpecHost.setType("HostSystem");
        pSpecHost.getPathSet().add("name");
        // ManagedObjectReference[] to a Datastore[]
        // A collection of references to the subset of datastore objects in the
        // datacenter that are available in this HostSystem.
        pSpecHost.getPathSet().add("datastore");

        pSpecHost.getPathSet().add("runtime.connectionState"); // connected,
                                                               // disconnected,
                                                               // notResponding
        pSpecHost.getPathSet().add("runtime.powerState"); // poweredOff,
                                                          // poweredOn, standBy,
                                                          // unknown
        pSpecHost.getPathSet().add("runtime.healthSystemRuntime");

        pSpecHost.getPathSet().add("summary.hardware.cpuMhz");
        pSpecHost.getPathSet().add("summary.hardware.memorySize");
        pSpecHost.getPathSet().add("summary.hardware.numCpuCores");
        pSpecHost.getPathSet().add("summary.quickStats.distributedCpuFairness");
        pSpecHost.getPathSet()
                .add("summary.quickStats.distributedMemoryFairness");
        pSpecHost.getPathSet().add("summary.quickStats.overallCpuUsage");
        pSpecHost.getPathSet().add("summary.quickStats.overallMemoryUsage");

        PropertySpec pSpecVM = new PropertySpec();
        pSpecVM.setType("VirtualMachine");
        pSpecVM.getPathSet().add("name");
        pSpecVM.getPathSet().add("runtime.host");
        pSpecVM.getPathSet().add("summary.config.template");
        pSpecVM.getPathSet().add("summary.config.numCpu");
        pSpecVM.getPathSet().add("summary.config.memorySizeMB");

        PropertySpec pSpecDataStore = new PropertySpec();
        pSpecDataStore.setType("Datastore");
        // DatastoreHostMount[] Hosts attached to this datastore.
        pSpecDataStore.getPathSet().add("host");
        pSpecDataStore.getPathSet().add("summary.name");
        pSpecDataStore.getPathSet().add("summary.capacity");
        pSpecDataStore.getPathSet().add("summary.freeSpace");

        ArrayList<PropertySpec> result = new ArrayList<PropertySpec>();
        result.add(pSpecDatacenter);
        result.add(pSpecHost);
        result.add(pSpecVM);
        result.add(pSpecDataStore);
        return result;
    }

    /**
     * Creates the traversal specification for data center inventory retrieval.
     */
    public List<SelectionSpec> buildFullTraversal() {

        TraversalSpec rpToRp = new TraversalSpec();
        rpToRp.setName("rpToRp");
        rpToRp.setType("ResourcePool");
        rpToRp.setPath("resourcePool");
        rpToRp.setSkip(Boolean.FALSE);
        SelectionSpec selSpec = new SelectionSpec();
        selSpec.setName("rpToRp");
        rpToRp.getSelectSet().add(selSpec);
        selSpec = new SelectionSpec();
        selSpec.setName("rpToVm");
        rpToRp.getSelectSet().add(selSpec);

        TraversalSpec rpToVm = new TraversalSpec();
        rpToVm.setName("rpToVm");
        rpToVm.setType("ResourcePool");
        rpToVm.setPath("vm");
        rpToVm.setSkip(Boolean.FALSE);

        TraversalSpec crToRp = new TraversalSpec();
        crToRp.setName("crToRp");
        crToRp.setType("ComputeResource");
        crToRp.setPath("resourcePool");
        crToRp.setSkip(Boolean.FALSE);
        selSpec = new SelectionSpec();
        selSpec.setName("rpToRp");
        crToRp.getSelectSet().add(selSpec);
        selSpec = new SelectionSpec();
        selSpec.setName("rpToVm");
        crToRp.getSelectSet().add(selSpec);

        TraversalSpec crToH = new TraversalSpec();
        crToH.setName("crToH");
        crToH.setType("ComputeResource");
        crToH.setPath("host");
        crToH.setSkip(Boolean.FALSE);

        TraversalSpec dcToHf = new TraversalSpec();
        dcToHf.setName("dcToHf");
        dcToHf.setType("Datacenter");
        dcToHf.setPath("hostFolder");
        dcToHf.setSkip(Boolean.FALSE);
        selSpec = new SelectionSpec();
        selSpec.setName("visitFolders");
        dcToHf.getSelectSet().add(selSpec);

        TraversalSpec dcToDs = new TraversalSpec();
        dcToDs.setName("dcToDs");
        dcToDs.setType("Datacenter");
        dcToDs.setPath("datastore");
        dcToDs.setSkip(Boolean.FALSE);
        selSpec = new SelectionSpec();
        selSpec.setName("visitFolders");
        dcToDs.getSelectSet().add(selSpec);

        TraversalSpec HToVm = new TraversalSpec();
        HToVm.setName("HToVm");
        HToVm.setType("HostSystem");
        HToVm.setPath("vm");
        HToVm.setSkip(Boolean.FALSE);
        selSpec = new SelectionSpec();
        selSpec.setName("visitFolders");
        HToVm.getSelectSet().add(selSpec);

        TraversalSpec visitFolders = new TraversalSpec();
        visitFolders.setName("visitFolders");
        visitFolders.setType("Folder");
        visitFolders.setPath("childEntity");
        visitFolders.setSkip(Boolean.FALSE);
        selSpec = new SelectionSpec();
        selSpec.setName("visitFolders");
        visitFolders.getSelectSet().add(selSpec);
        selSpec = new SelectionSpec();
        selSpec.setName("dcToHf");
        visitFolders.getSelectSet().add(selSpec);
        selSpec = new SelectionSpec();
        selSpec.setName("dcToDs");
        visitFolders.getSelectSet().add(selSpec);
        selSpec = new SelectionSpec();
        selSpec.setName("crToH");
        visitFolders.getSelectSet().add(selSpec);
        selSpec = new SelectionSpec();
        selSpec.setName("crToRp");
        visitFolders.getSelectSet().add(selSpec);
        selSpec = new SelectionSpec();
        selSpec.setName("HToVm");
        visitFolders.getSelectSet().add(selSpec);
        selSpec = new SelectionSpec();
        selSpec.setName("rpToVm");

        ArrayList<SelectionSpec> result = new ArrayList<SelectionSpec>();
        result.add(visitFolders);
        result.add(dcToDs);
        result.add(dcToHf);
        result.add(crToH);
        result.add(crToRp);
        result.add(rpToRp);
        result.add(HToVm);
        result.add(rpToVm);
        return result;
    }

}
