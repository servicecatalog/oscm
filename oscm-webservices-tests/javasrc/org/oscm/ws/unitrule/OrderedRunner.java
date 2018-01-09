/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                  
 *  Creation Date: 18.06.15 15:42
 *
 *******************************************************************************/

package org.oscm.ws.unitrule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Created by FlorekS
 */
public class OrderedRunner extends BlockJUnit4ClassRunner {

    public OrderedRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> list = super.computeTestMethods();
        List<FrameworkMethod> copy = new ArrayList<>(list);
        Collections.sort(copy, new Comparator<FrameworkMethod>() {

            @Override
            public int compare(FrameworkMethod f1, FrameworkMethod f2) {
                Order o1 = f1.getAnnotation(Order.class);
                Order o2 = f2.getAnnotation(Order.class);

                if(o1 == null || o2 == null) {
                    return -1;
                }

                return o1.order() - o2.order();
            }
        });

        return copy;
    }
}
