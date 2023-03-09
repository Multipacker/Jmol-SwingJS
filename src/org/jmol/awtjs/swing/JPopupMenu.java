package org.jmol.awtjs.swing;

public class JPopupMenu extends AbstractButton {
  // note that in Java Swing JPopupMenu extends 
  // JComponent, but here we extend AbstractButton
  // so that it shares the SwingComponent interface
  
  boolean tainted = true;

  public JPopupMenu(String name) {
    super("mnu");
    this.name = name;
  }

  public void setInvoker(Object applet) {
    this.applet = applet;
  }
  
  public void show(Component applet, int x, int y) {
    if (applet != null)
       tainted = true;
  }

  public void disposeMenu() {
  }
  
  @Override
  public String toHTML() {
    return getMenuHTML();
  }
}
