package org.jmol.awtjs2d;

import javajs.util.P3d;

/**
 * methods required by Jmol that access java.awt.Component
 * 
 * private to org.jmol.awt
 */
class Display {
  static void getFullScreenDimensions(Object canvas, int[] widthHeight) {
  }
  
  static boolean hasFocus(Object canvas) {
      System.out.println(canvas);
    return true;
  }

  static void requestFocusInWindow(Object canvas) {
      System.out.println(canvas);
  }
  
  /**
   * @param label 
   * @param data 
   * @param list 
   * @param asButtons  
   * @return "null" or result of prompt
   */
  public static String prompt(String label, String data, String[] list, boolean asButtons) {
    //TODO -- list and asButtons business
    return "null";
  }

  public static void convertPointFromScreen(Object canvas, P3d ptTemp) {    
      System.out.println("" + canvas + ptTemp);
  }
}
