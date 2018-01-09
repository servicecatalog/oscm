/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.remote.vmware;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

public class VMClientPool {

    private KeyedObjectPool<String, VMwareClient> pool;

    private static class SingletonHolder {
        public static final VMClientPool INSTANCE = new VMClientPool();
    }

    public static VMClientPool getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private VMClientPool() {
        startPool();
    }

    public KeyedObjectPool<String, VMwareClient> getPool() {
        return pool;
    }

    public void startPool() {
        pool = new GenericKeyedObjectPool<String, VMwareClient>(
                new VMClientFactory());
        ((GenericKeyedObjectPool<String, VMwareClient>) pool)
                .setMaxIdlePerKey(10);
        ((GenericKeyedObjectPool<String, VMwareClient>) pool).setMaxTotal(20);
        ((GenericKeyedObjectPool<String, VMwareClient>) pool)
                .setMaxTotalPerKey(20);
        ((GenericKeyedObjectPool<String, VMwareClient>) pool)
                .setTestOnBorrow(true);
    }
}