/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 30 Jul 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.model;

import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOService;

/** @author worf */
public class ServiceWithImage extends Service {

  /** */
  private static final long serialVersionUID = -6959389722812528100L;
  /** */
  private VOImageResource img;

  public ServiceWithImage(VOService vo) {
    super(vo);
  }

  public ServiceWithImage(VOService vo, VOImageResource img) {
    super(vo);
    this.img = img;
  }

  public VOImageResource getImg() {
    return img;
  }

  public void setImg(VOImageResource img) {
    this.img = img;
  }
}
