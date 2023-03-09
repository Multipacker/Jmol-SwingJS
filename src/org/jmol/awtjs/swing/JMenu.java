package org.jmol.awtjs.swing;

public class JMenu extends JMenuItem {
  public JMenu() {
    super("mnu",TYPE_MENU);
  }

  public int getItemCount() {
    return getComponentCount();
  }

  public Component getItem(int i) {
    return getComponent(i);
  }

  @Override
  public Object getPopupMenu() {
    return this;
  }

  @Override
  public String toHTML() {
    return getMenuHTML();
  }
}
