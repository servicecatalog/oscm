/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: hoffmann                                                      
 *                                                                              
 *  Creation Date: 21.10.2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.menu;

import org.oscm.ui.authorization.Condition;
import org.oscm.ui.authorization.UIStatus;


/**
 * Internal base class for menu element implementations. Needs to be public
 * otherwise JSF can't access the methods (even if they are part of the public
 * interface).
 * 
 * @author hoffmann
 */
public abstract class AbstractMenuElement implements MenuElement {

    private final String id;
    private final String labelKey;
    private final Condition visibility;
    protected final UIStatus uiStatus;

    AbstractMenuElement(final String id, final String labelKey,
            final Condition visibility, UIStatus uiStatus) {
        this.id = id;
        this.labelKey = labelKey;
        this.visibility = visibility;
        this.uiStatus = uiStatus;
    }

    public final String getId() {
        return id;
    }

    public final String getLabelKey() {
        return labelKey;
    }

    public final boolean isVisible() {
        return visibility.eval() && !uiStatus.isHidden(id);
    }

    @Override
    public final String toString() {
        StringBuilder buffer = new StringBuilder();
        toString(buffer, "");
        return buffer.toString();
    }

    protected void toString(StringBuilder buffer, String indent) {
        buffer.append(indent).append("+-- ");
        buffer.append(id == null ? "ROOT" : id);
        buffer.append(" visible=").append(isVisible());
        buffer.append('\n');
    }

}
