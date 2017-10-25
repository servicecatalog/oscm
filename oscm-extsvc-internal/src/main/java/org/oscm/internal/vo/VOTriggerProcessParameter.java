/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 17.06.15 09:58
 *
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.types.enumtypes.TriggerProcessParameterType;

public class VOTriggerProcessParameter extends BaseVO {

    private static final long serialVersionUID = 5507145722122133878L;

    private TriggerProcessParameterType type;

    private Object value;

    private Long triggerProcessKey;

    public TriggerProcessParameterType getType() {
        return type;
    }

    public void setType(TriggerProcessParameterType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Long getTriggerProcessKey() {
        return triggerProcessKey;
    }

    public void setTriggerProcessKey(Long triggerProcessKey) {
        this.triggerProcessKey = triggerProcessKey;
    }
}
