/* $RCSfile$
 * $Author: hansonr $
 * $Date: 2015-01-05 11:26:39 -0600 (Mon, 05 Jan 2015) $
 * $Revision: 20170 $
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
package org.openscience.jmol.app.jmolpanel;


import javax.swing.JFrame;

import org.jmol.i18n.GT;

public class WhatsNewDialog extends HelpDialog {

  public WhatsNewDialog(JFrame fr) {

    super(fr, GT.$("What's New in Jmol"), true);

    init(null, "WhatsNew.changeLogURL");
  }

}
