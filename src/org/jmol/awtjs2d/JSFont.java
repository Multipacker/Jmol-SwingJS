package org.jmol.awtjs2d;

import org.jmol.util.Font;

/**
 * methods required by Jmol that access java.awt.Font
 * 
 * private to org.jmol.awtjs2d
 */
class JSFont {
  static Object newFont(String fontFace, boolean isBold, boolean isItalic, float fontSize, String type) {
    // "px" are different from "pt" here.
    // "pt" is the height of an X, ascent.
    // "px" is the ascent + descent.
    fontFace = (fontFace.equals("Monospaced") ? "Courier" : fontFace.startsWith("Sans") ? "Helvetica Neue, Sans-serif" : "Serif");    
    return (isBold ? "bold " : "") + (isItalic ? "italic " : "") + fontSize + type + " " + fontFace;
  }

  /**
   * @param font 
   * @param context  
   * @return the context
   */
  static Object getFontMetrics(Font font, Object context) {
    return context;
  }

  /**
   * @param context  
   * @return height of the font 
   */
  static int getAscent(Object context) {
    return 0;
  }

  /**
   * @param context  
   * @return descent of "g"
   */
  static int getDescent(Object context) {
    return 0;
  }

  /**
   * @param font 
   * @param context 
   * @param text 
   * @return width
   */
  static int stringWidth(Font font, Object context, String text) {
     return 0;
  }
}
