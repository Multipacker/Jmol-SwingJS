package org.jmol.awtjs.swing;

import javajs.api.GenericColor;
import javajs.util.CU;

abstract public class Component {
  private boolean _visible;  
  protected boolean enabled = true;
  protected String text;    
  protected String name;
  protected int width;
  protected int height;
  protected String id;

  protected Object parent;
  
  public void setParent(Object p) {
    parent = p;
  }

  protected Object mouseListener;

  private GenericColor bgcolor;

  protected Component(String type) {
    id = newID(type);
    if (type == null)
      return;
  }
  
  public static String newID(String type) {
    return type + ("" + Math.random()).substring(3, 10);
  }

  abstract public String toHTML();
  
  public void setBackground(GenericColor color) {
    bgcolor = color;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
  
  public Object getParent() {
    return parent;
  }
  
  public void setPreferredSize(Dimension dimension) {
    this.width = dimension.width;
    this.height = dimension.height;   
  }

  public void addMouseListener(Object listener) {
    mouseListener = listener;
  }

  public String getText() {
    return text;
  }

  public boolean isEnabled() {
    return enabled;
  }
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isVisible() {
    return _visible;
  }

  public void setVisible(boolean visible) {
    this._visible = visible;
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  protected int minWidth = 30;
  protected int minHeight = 30;

  public void setMinimumSize(Dimension d) {
    minWidth = d.width;
    minHeight = d.height;
  }

  public int getSubcomponentWidth() {
    return width;
  }
  
  public int getSubcomponentHeight() {
    return height;
  }
  
  protected int renderWidth;
  protected int renderHeight;

  protected String getCSSstyle(int defaultPercentW, int defaultPercentH) {
    int width = (renderWidth > 0 ? renderWidth : getSubcomponentWidth());
    int height = (renderHeight > 0 ? renderHeight : getSubcomponentHeight());
    return (width > 0 ? "width:" + width +"px;" : defaultPercentW > 0 ? "width:"+defaultPercentW+"%;" : "")
    + (height > 0 ?"height:" + height + "px;" : defaultPercentH > 0 ? "height:"+defaultPercentH+"%;" : "")
    + (bgcolor == null ? "" : "background-color:" + CU.toCSSString(bgcolor) + ";");
  }
  
  public void repaint() {
    // for inheritance
  }
}
