/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.i18nservice.assembler;

import org.oscm.domobjects.ImageResource;
import org.oscm.internal.vo.VOImageResource;

public class ImageResourceAssembler {

    public static ImageResource toImageResource(VOImageResource voImageResource) {
        if (voImageResource == null) {
            return null;
        }

        ImageResource result = new ImageResource();
        result.setBuffer(voImageResource.getBuffer());
        result.setContentType(voImageResource.getContentType());
        result.setImageType(voImageResource.getImageType());
        return result;
    }
}
