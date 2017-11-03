/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import java.util.concurrent.Callable;

/**
 * Creates a new invocation handler from two chained invocation handlers.
 * 
 * @author hoffmann
 */
class ChainedInvocationHandler implements IInvocationHandler {

    private final IInvocationHandler h1;
    private final IInvocationHandler h2;

    public ChainedInvocationHandler(IInvocationHandler h1, IInvocationHandler h2) {
        this.h1 = h1;
        this.h2 = h2;
    }

    public Object call(final Callable<Object> callable, final IInvocationCtx ctx)
            throws Exception {
        return h1.call(new Callable<Object>() {
            public Object call() throws Exception {
                return h2.call(callable, ctx);
            }
        }, ctx);
    }

}
