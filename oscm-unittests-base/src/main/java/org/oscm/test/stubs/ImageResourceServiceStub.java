/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import org.oscm.domobjects.ImageResource;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.internal.types.enumtypes.ImageType;

public class ImageResourceServiceStub implements ImageResourceServiceLocal {

    @Override
    public void delete(long objectKey, ImageType imageType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImageResource read(long objectKey, ImageType imageType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(ImageResource imageResource) {
        throw new UnsupportedOperationException();
    }

}
