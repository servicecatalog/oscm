/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.cdi;

import java.lang.annotation.Annotation;

import javax.enterprise.event.Event;
import javax.enterprise.util.TypeLiteral;

@SuppressWarnings("rawtypes")
public class TestEvent implements javax.enterprise.event.Event {
    private ContextManager contextManager;

    public TestEvent(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    @Override
    public void fire(Object object) {
        contextManager.executeObserverMethod(object);
    }

    @Override
    public Event select(Annotation... arg0) {
        return null;
    }

    @Override
    public Event select(Class arg0, Annotation... arg1) {
        return null;
    }

    @Override
    public Event select(TypeLiteral arg0, Annotation... arg1) {
        return null;
    }

}
