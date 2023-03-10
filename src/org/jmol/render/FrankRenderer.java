/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2015-08-25 00:56:29 -0500 (Tue, 25 Aug 2015) $
 * $Revision: 20734 $
 *
 * Copyright (C) 2002-2005  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.render;
import org.jmol.modelkit.ModelKit;
import org.jmol.script.T;
import org.jmol.shape.Frank;
import org.jmol.shape.Shape;
import org.jmol.util.C;
import org.jmol.viewer.Viewer;
import org.jmol.api.JmolRendererInterface;
import org.jmol.modelset.ModelSet;
import org.jmol.util.GData;

public class FrankRenderer extends ShapeRenderer {
  //we render Frank last just for the touch that if there are translucent
  //objects, then it becomes translucent. Just for fun.

  // no Frank export

  @Override
  protected boolean render() {
	  return FrankRenderer.renderFrank(vwr, g3d, ms, shape);
  }

  public static boolean renderFrank(Viewer vwr, JmolRendererInterface g3d, ModelSet ms, Shape shape) {
    boolean isExport = (g3d.getExportType() != GData.EXPORT_NOT);

    Frank frank = (Frank) shape;
    boolean allowKeys = vwr.getBooleanProperty("allowKeyStrokes");
    boolean modelKitMode = vwr.getBoolean(T.modelkitmode);
    short colix = (modelKitMode && !vwr.getModelkit(false).isHidden() ? C.MAGENTA : allowKeys ? C.BLUE : C.GRAY);

    if (isExport || !vwr.getShowFrank() || !g3d.setC(colix)) {
      return false;
	}
    if (vwr.frankOn && !vwr.noFrankEcho) {
      return vwr.noFrankEcho;
	}
    vwr.noFrankEcho = true;
    double imageFontScaling = vwr.imageFontScaling;
    frank.getFont(imageFontScaling);
    int dx = (int) (frank.frankWidth + Frank.frankMargin * imageFontScaling);
    int dy = frank.frankDescent;
    g3d.drawStringNoSlab(frank.frankString, frank.font3d, vwr.gdata.width - dx, vwr.gdata.height - dy, 0, (short) 0);
    ModelKit kit = (modelKitMode ? vwr.getModelkit(false) : null);
    if (modelKitMode && !kit.isHidden()) {
      g3d.setC(C.GRAY);
      int w = 10;
      int h = 26;
      g3d.fillTextRect(0, 0, 1, 0, w, h*4);
      String active = kit.getActiveMenu();  
      if (active != null) {
        if ("atomMenu".equals(active)) {
          g3d.setC(C.YELLOW);
          g3d.fillTextRect(0, 0, 0, 0, w, h);
        } else if ("bondMenu".equals(active)) {
          g3d.setC(C.BLUE);
          g3d.fillTextRect(0, h, 0, 0, w, h);
        } else if ("xtalMenu".equals(active)) {
          g3d.setC(C.WHITE);
          g3d.fillTextRect(0, h<<1, 0, 0, w, h);
        }
      }
    }
    return false;
  }
}
