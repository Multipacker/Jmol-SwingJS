/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2006  The Jmol Development Team
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
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *  02110-1301, USA.
 */

package org.jmol.awtjs2d;

import java.util.Map;

import org.jmol.api.GenericImageDialog;
import org.jmol.util.Font;
import org.jmol.viewer.Viewer;

/**
 * methods required by Jmol that access java.awt.Image
 * 
 * private to org.jmol.awt
 */
class Image {
  static int getWidth(Object canvas) {
      return 0;
  }

  static int getHeight(Object canvas) {
      return 0;
  }

  /**
   * @param context
   * @param width
   * @param height
   * @return null
   */
  static int[] grabPixels(Object context, int width, int height) {
    int[] data = null;
    return toIntARGB(data);
  }

  static int[] toIntARGB(int[] imgData) {
    /*
     * red=imgData.data[0];
     * green=imgData.data[1];
     * blue=imgData.data[2];
     * alpha=imgData.data[3];
     */
    int n = imgData.length / 4;
    int[] iData = new int[n];
    for (int i = 0, j = 0; i < n;) {
      iData[i++] = (imgData[j++] << 16) | (imgData[j++] << 8) | imgData[j++] | (imgData[j++] << 24);
    }
    return iData;
  }      
  
  @SuppressWarnings("unused")
  public static int[] getTextPixels(String text, Font font3d, Object context, int width, int height, int ascent) {
      if (true) {
        return null;
	  }
    return grabPixels(context, width, height);
  }

  /**
   * @param windowWidth
   * @param windowHeight
   * @param pBuffer
   * @param windowSize
   * @param backgroundTransparent
   * @param canvas
   * @return a canvas
   */
  static Object allocateRgbImage(int windowWidth, int windowHeight, int[] pBuffer, int windowSize, boolean backgroundTransparent, Object canvas) {
    return canvas;
  }

  public static GenericImageDialog getImageDialog(Viewer vwr, String title, Map<String, GenericImageDialog> imageMap) {
    return Platform.Jmol().consoleGetImageDialog(vwr, title, imageMap);
  }

  /**
     * Draw the completed image from rendering. Note that the
     * image buffer (org.jmol.g3d.Graphics3D.
     * @param context
     * @param canvas
     * @param x
     * @param y
     * @param width  unused in Jmol proper
     * @param height unused in Jmol proper
     * @param isDTI 
     */
    static void drawImage(Object context, Object canvas, int x, int y, int width, int height, boolean isDTI) {
    }
}
