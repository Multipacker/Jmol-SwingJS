/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2016-04-16 16:54:06 -0500 (Sat, 16 Apr 2016) $
 * $Revision: 21051 $
 *
 * Copyright (C) 2003-2005  The Jmol Development Team
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
 *  Lesser General License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jmol.render;

import java.util.Map;
import javajs.util.BS;

import org.jmol.api.Interface;
import org.jmol.api.JmolRendererInterface;
import org.jmol.api.JmolRepaintManager;
import org.jmol.api.js.JmolToJSmolInterface;
import org.jmol.modelkit.ModelKit;
import org.jmol.modelset.Atom;
import org.jmol.modelset.ModelSet;
import org.jmol.script.T;
import org.jmol.shape.Balls;
import org.jmol.shape.Frank;
import org.jmol.shape.Shape;
import org.jmol.util.C;
import org.jmol.util.GData;
import org.jmol.util.Logger;
import org.jmol.util.Rectangle;
import org.jmol.viewer.JC;
import org.jmol.viewer.ShapeManager;
import org.jmol.viewer.Viewer;

public class RepaintManager implements JmolRepaintManager {
  private Viewer vwr;
  private ShapeManager shapeManager;
  private ShapeRenderer[] renderers;

  public RepaintManager() {
    // required for reflection
  }
  
  private final BS bsTranslucent = BS.newN(JC.SHAPE_MAX);
  
  @Override
  public void set(Viewer vwr, ShapeManager shapeManager) {
    this.vwr = vwr;
    this.shapeManager = shapeManager;
  }

  /////////// thread management ///////////
  
  public int holdRepaint = 0;
  private boolean repaintPending;
  
  @Override
  public boolean isRepaintPending() {
    return repaintPending;
  }
  
  @Override
  public void pushHoldRepaint(String why) {
    ++holdRepaint;
  }
  
  @Override
  public void popHoldRepaint(boolean andRepaint, String why) {
    --holdRepaint;
    if (holdRepaint <= 0) {
      holdRepaint = 0;
      if (andRepaint) {
        repaintPending = true;
        repaintNow(why);
      }
    }
  }

  @SuppressWarnings({ "null", "unused" })
  @Override
  synchronized public void requestRepaintAndWait(String why) {
    JmolToJSmolInterface jmol = null;
    if (Viewer.isJS && !Viewer.isSwingJS) {
    }    
    if (jmol == null) {
      try {
        repaintNow(why);
        if (!Viewer.isJS) wait(vwr.g.repaintWaitMs); // more than a second probably means we are locked up here
        if (repaintPending) {
          Logger.error("repaintManager requestRepaintAndWait timeout");
          repaintDone();
        }
      } catch (InterruptedException e) {
        System.out.println("repaintManager requestRepaintAndWait interrupted thread=" + Thread.currentThread().getName());
      }
    } else {
      jmol.repaint(vwr.html5Applet,  false);
      repaintDone();
    }
  }

  @Override
  public boolean repaintIfReady(String why) {
    if (repaintPending)
      return false;
    repaintPending = true;
    if (holdRepaint == 0)
      repaintNow(why);
    return true;
  }

  private void repaintNow(String why) {
    // from RepaintManager to the System
    // -- "Send me an asynchronous update() event!"
    if (!vwr.haveDisplay)
      return;    
    vwr.apiPlatform.repaint(vwr.display);
  }

  @Override
  synchronized public void repaintDone() {
    repaintPending = false;

    notify(); // to cancel any wait in requestRepaintAndWait()
  }

  /////////// renderer management ///////////

  @Override
  public void clear(int iShape) {
    if (renderers ==  null)
      return;
    if (iShape >= 0)
      renderers[iShape] = null;
    else
      for (int i = 0; i < JC.SHAPE_MAX; ++i)
        renderers[i] = null;
  }

  private ShapeRenderer getRenderer(int shapeID) {
    if (renderers[shapeID] != null)
      return renderers[shapeID];
    String className = JC.getShapeClassName(shapeID, true) + "Renderer";
    ShapeRenderer renderer;
    if ((renderer = (ShapeRenderer) Interface.getInterface(className, vwr, "render")) == null)
      return null;
    renderer.setViewerG3dShapeID(vwr, shapeID);
    return renderers[shapeID] = renderer;
  }

  /////////// actual rendering ///////////

  private boolean renderShape(int shapeID, JmolRendererInterface g3d, ModelSet modelSet, Shape shape) {
	  switch (shapeID) {
		  case JC.SHAPE_BALLS: return RepaintManager.renderBalls(vwr, g3d, modelSet, shape);
		  case JC.SHAPE_FRANK: return RepaintManager.renderFrank(vwr, g3d, modelSet, shape);
		  default: return getRenderer(shapeID).renderShape(g3d, modelSet, shape);
	  }
  }

  @Override
  public void render(GData gdata, ModelSet modelSet, boolean isFirstPass, int[] navMinMax) {
    JmolRendererInterface g3d = (JmolRendererInterface) gdata;
    if (renderers == null)
      renderers = new ShapeRenderer[JC.SHAPE_MAX];
    getAllRenderers();
    try {
      boolean logTime = vwr.getBoolean(T.showtiming);
      g3d.renderBackground(null);
      if (isFirstPass) {
        bsTranslucent.clearAll();
        if (navMinMax != null)
          g3d.renderCrossHairs(navMinMax, vwr.getScreenWidth(),
              vwr.getScreenHeight(), vwr.tm.getNavigationOffset(),
              vwr.tm.navigationDepthPercent);
        Rectangle band = vwr.getRubberBandSelection();
        if (band != null && g3d.setC(vwr.cm.colixRubberband))
          g3d.drawRect(band.x, band.y, 0, 0, band.width, band.height);
        vwr.noFrankEcho = true;
      }
      String msg = null;
      for (int i = 0; i < JC.SHAPE_MAX && gdata.currentlyRendering; ++i) {
        Shape shape = shapeManager.getShape(i);
        if (shape == null)
          continue;

        if (logTime) {
          msg = "rendering " + JC.getShapeClassName(i, false);
          Logger.startTimer(msg);
        }
        if ((isFirstPass || bsTranslucent.get(i)) && renderShape(i, g3d, modelSet, shape))
          bsTranslucent.set(i);
        if (logTime)
          Logger.checkTimer(msg, false);
      }
      g3d.renderAllStrings(null);
    } catch (Exception e) {
        e.printStackTrace();
      if (vwr.async && "Interface".equals(e.getMessage()))
        throw new NullPointerException();
      Logger.error("rendering error? " + e);
    }
  }
  
  private void getAllRenderers() {
    boolean isOK = true;
    for (int i = 0; i < JC.SHAPE_MAX; ++i) {
      if (shapeManager.getShape(i) == null || getRenderer(i) != null)
        continue;
      isOK = repaintPending = !vwr.async;
    }
    if (!isOK)
      throw new NullPointerException();
  }

  @Override
  public String renderExport(GData gdata, ModelSet modelSet, Map<String, Object> params) {
    boolean isOK;
    shapeManager.finalizeAtoms(null, true);
    JmolRendererInterface exporter3D = vwr.initializeExporter(params);
    isOK = (exporter3D != null);
    if (!isOK) {
      Logger.error("Cannot export " + params.get("type"));
      return null;
    }
    if (renderers == null)
      renderers = new ShapeRenderer[JC.SHAPE_MAX];
    getAllRenderers();
    String msg = null;
    try {
      boolean logTime = vwr.getBoolean(T.showtiming);
      exporter3D.renderBackground(exporter3D);
      for (int i = 0; i < JC.SHAPE_MAX; ++i) {
        Shape shape = shapeManager.getShape(i);
        if (shape == null)
          continue;
        if (logTime) {
          msg = "rendering " + JC.getShapeClassName(i, false);
          Logger.startTimer(msg);
        }
		renderShape(i, exporter3D, modelSet, shape);
        if (logTime)
          Logger.checkTimer(msg, false);
      }
      exporter3D.renderAllStrings(exporter3D);
      msg = exporter3D.finalizeOutput();
    } catch (Exception e) {
      e.printStackTrace();
      Logger.error("rendering error? " + e);
    }
    return msg;
  }

  private static boolean renderBalls(Viewer vwr, JmolRendererInterface g3d, ModelSet ms, Shape shape) {
    boolean isExport = (g3d.getExportType() != GData.EXPORT_NOT);
	int myVisibilityFlag = JC.getShapeVisibilityFlag(JC.SHAPE_BALLS);

    boolean needTranslucent = false;
    if (isExport || vwr.checkMotionRendering(T.atoms)) {
      Atom[] atoms = ms.at;
	  if (atoms == null) {
		  return false;
	  }

      BS bsOK = vwr.shm.bsRenderableAtoms;
      short[] colixes = ((Balls) shape).colixes;
      for (int i = bsOK.nextSetBit(0); i >= 0; i = bsOK.nextSetBit(i + 1)) {
        Atom atom = atoms[i];
        if (atom == null) {
          return false;
		}

        if (atom.sD > 0 && (atom.shapeVisibilityFlags & myVisibilityFlag) != 0) {
          if (g3d.setC(colixes == null ? atom.colixAtom : Shape.getColix(colixes, i, atom))) {
            g3d.drawAtom(atom, 0);
          } else {
            needTranslucent = true;
          }
        }
      }
    }

    return needTranslucent;
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
