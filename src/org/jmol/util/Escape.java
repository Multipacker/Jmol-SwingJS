/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2005  The Jmol Development Team
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
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *  02110-1301, USA.
 */

package org.jmol.util;

import java.util.Iterator;
import java.util.Map;

import org.jmol.api.JmolDataManager;
import org.jmol.script.SV;
import org.jmol.script.T;

import javajs.api.JSONEncodable;
import javajs.util.AU;
import javajs.util.BS;
import javajs.util.Lst;
import javajs.util.M34d;
import javajs.util.M3d;
import javajs.util.M4d;
import javajs.util.P3d;
import javajs.util.P4d;
import javajs.util.PT;
import javajs.util.Qd;
import javajs.util.SB;
import javajs.util.T3d;
import javajs.util.V3d;

public class Escape {
  public static String escapeColor(int argb) {
    return (argb == 0 ? null : "[x" + getHexColorFromRGB(argb) + "]");
  }

  public static String getHexColorFromRGB(int argb) {
    if (argb == 0)
      return null;
    String r  = "00" + Integer.toHexString((argb >> 16) & 0xFF);
    r = r.substring(r.length() - 2);
    String g  = "00" + Integer.toHexString((argb >> 8) & 0xFF);
    g = g.substring(g.length() - 2);
    String b  = "00" + Integer.toHexString(argb & 0xFF);
    b = b.substring(b.length() - 2);
    return r + g + b;
  }

  
  /**
   * must be its own, because of the possibility of being null
   * @param xyz
   * @return  {x y z}
   */
  public static String eP(T3d xyz) {
    if (xyz == null)
      return "null";
    return "{" + xyz.x + " " + xyz.y + " " + xyz.z + "}";
  }

  public static String matrixToScript(Object m) {
    return PT.replaceAllCharacters(m.toString(), "\n\r ","").replace('\t',' ');
  }

  public static String eP4(P4d x) {
    return "{" + x.x + " " + x.y + " " + x.z + " " + x.w + "}";
  }

  public static String drawQuat(Qd q, String prefix, String id, P3d ptCenter, double scale) {
    String strV = " VECTOR " + eP(ptCenter) + " ";
    if (scale == 0)
      scale = 1d;
    return "draw " + prefix + "x" + id + strV
        + eP(q.getVectorScaled(0, scale)) + " color red\n"
        + "draw " + prefix + "y" + id + strV
        + eP(q.getVectorScaled(1, scale)) + " color green\n"
        + "draw " + prefix + "z" + id + strV
        + eP(q.getVectorScaled(2, scale)) + " color blue\n";
  }

  @SuppressWarnings("unchecked")
  public static String e(Object x) {
    if (x == null)
      return "null";
    if (PT.isNonStringPrimitive(x))
      return x.toString();
    if (x instanceof String)
      return PT.esc((String) x);
    if (x instanceof Lst<?>)
      return eV((Lst<SV>) x);
    if (x instanceof Map)
      return escapeMap((Map<String, Object>) x);
    if (x instanceof BS) 
      return eBS((BS) x);
    if (AU.isAP(x))
      return eAP((T3d[]) x);
    if (AU.isAS(x))
      return eAS((String[]) x, true);
    if (AU.isADD(x)) {
      // for isosurface functionXY
      double[][] ff = (double[][])x;
      SB sb = new SB().append("[");
      String sep = "";
      for (int i = 0; i < ff.length; i++) {
        sb.append(sep).append(eAD(ff[i]));
        sep = ",";
      }
      sb.append("]");
      return sb.toString();
    }
    if (AU.isADD(x)) {
      // for isosurface functionXY
      double[][] ff = (double[][])x;
      SB sb = new SB().append("[");
      String sep = "";
      for (int i = 0; i < ff.length; i++) {
        sb.append(sep).append(eAD(ff[i]));
        sep = ",";
      }
      sb.append("]");
      return sb.toString();
    }
    if (x instanceof T3d || x instanceof T3d)
      return x.toString();
    if (x instanceof JSONEncodable)
      return ((JSONEncodable) x).toJSON();
    String s = PT.nonArrayString(x);
    return (s == null ? PT.toJSON(null, x) : s);
  }

  public static String eV(Lst<SV> list) {
    if (list == null)
      return PT.esc("");
    SB s = new SB();
    s.append("[");
    for (int i = 0; i < list.size(); i++) {
      if (i > 0)
        s.append(", ");
      s.append(escapeNice(list.get(i).asString()));
    }
    s.append("]");
    return s.toString();
  }

  public static String escapeMap(Map<String, Object> ht) {
    SB sb = new SB();
    sb.append("{ ");
    String sep = "";
    for (Map.Entry<String, Object> entry : ht.entrySet()) {
      String key = entry.getKey();
      sb.append(sep).append(PT.esc(key)).appendC(':');
      Object val = entry.getValue();
      if (!(val instanceof SV))
        val = SV.getVariable(val);
      sb.append(((SV)val).escape());
      sep = ","; 
    }
    sb.append(" }");
    return sb.toString();
  }
  
  /**
   * @param f
   * @param asArray -- FALSE allows bypassing of escape(Object f); TRUE: unnecssary
   * @return tabular string
   */
  public static String escapeDoubleA(double[] f, boolean asArray) {
    if (asArray)
      return PT.toJSON(null, f); // or just use escape(f)
    SB sb = new SB();
    for (int i = 0; i < f.length; i++) {
      if (i > 0)
        sb.appendC('\n');
      sb.appendD(f[i]);
    }
    return sb.toString();
  }

  public static String escapeDoubleAA(double[][] f, boolean addSemi) {
    SB sb = new SB();
    String eol = (addSemi ? ";\n" : "\n");
    for (int i = 0; i < f.length; i++)
      if (f[i] != null) {
        if (i > 0)
          sb.append(eol);
        for (int j = 0; j < f[i].length; j++)
          sb.appendD(f[i][j]).appendC('\t');
      }
    return sb.toString();
  }

  public static String escapeDoubleAAA(double[][][] f, boolean addSemi) {
    SB sb = new SB();
    String eol = (addSemi ? ";\n" : "\n");
    if (f[0] == null || f[0][0] == null)
      return "0 0 0" + eol;
    sb.appendI(f.length).append(" ")
      .appendI(f[0].length).append(" ")
      .appendI(f[0][0].length);
    for (int i = 0; i < f.length; i++)
      if (f[i] != null) {
        sb.append(eol);
        for (int j = 0; j < f[i].length; j++)
          if (f[i][j] != null) {
            sb.append(eol);
            for (int k = 0; k < f[i][j].length; k++)
              sb.appendD(f[i][j][k]).appendC('\t');
          }
      }
    return sb.toString();
  }

  /**
   * @param list
   *          list of strings to serialize
   * @param nicely TODO
   * @return serialized array
   */
  public static String eAS(String[] list, boolean nicely) {
    if (list == null)
      return PT.esc("");
    SB s = new SB();
    s.append("[");
    for (int i = 0; i < list.length; i++) {
      if (i > 0)
        s.append(", ");
      s.append(nicely ? escapeNice(list[i]) : PT.esc(list[i]));
    }
    s.append("]");
    return s.toString();
  }

  public static String eAI(int[] ilist) {
    if (ilist == null)
      return PT.esc("");
    SB s = new SB();
    s.append("[");
    for (int i = 0; i < ilist.length; i++) {
      if (i > 0)
        s.append(", ");
      s.appendI(ilist[i]);
    }
    return s.append("]").toString();
  }

  public static String eAD(double[] dlist) {
    // from isosurface area or volume calc
    if (dlist == null)
      return PT.esc("");
    SB s = new SB();
    s.append("[");
    for (int i = 0; i < dlist.length; i++) {
      if (i > 0)
        s.append(", ");
      s.appendD(dlist[i]);
    }
    return s.append("]").toString();
  }

  public static String eAP(T3d[] plist) {
    if (plist == null)
      return PT.esc("");
    SB s = new SB();
    s.append("[");
    for (int i = 0; i < plist.length; i++) {
      if (i > 0)
        s.append(", ");
      s.append(eP(plist[i]));
    }
    return s.append("]").toString();
  }

  private static String escapeNice(String s) {
    if (s == null)
      return "null";
    double f = PT.parseDoubleStrict(s);
    return (Double.isNaN(f) ? PT.esc(s) : s);
  }

  public static Object uABsM(String s) {
    if (s.charAt(0) == '{')
      return uP(s);
    if ((isStringArray(s)
        || s.startsWith("[{") && s.indexOf("[{") == s.lastIndexOf("[{"))
        && s.indexOf(',') < 0 && s.indexOf('.') < 0 && s.indexOf('-') < 0)
      return BS.unescape(s);
    if (s.startsWith("[["))
      return unescapeMatrixD(s);
    return s;
  }

  public static boolean isStringArray(String s) {
    return s.startsWith("({") && s.lastIndexOf("({") == 0
        && s.indexOf("})") == s.length() - 2;
  }
  public static Object uP(String strPoint) {
    if (strPoint == null || strPoint.length() == 0)
      return strPoint;
    String str = strPoint.replace('\n', ' ').trim();
    if (str.charAt(0) != '{' || str.charAt(str.length() - 1) != '}')
      return strPoint;
    double[] points = new double[5];
    int nPoints = 0;
    str = str.substring(1, str.length() - 1);
    int[] next = new int[1];
    for (; nPoints < 5; nPoints++) {
      points[nPoints] = PT.parseDoubleNext(str, next);
      if (Double.isNaN(points[nPoints])) {
        if (next[0] >= str.length() || str.charAt(next[0]) != ',')
          break;
        next[0]++;
        nPoints--;
      }
    }
    if (nPoints == 3)
      return P3d.new3(points[0], points[1], points[2]);
    if (nPoints == 4)
      return P4d.new4(points[0], points[1], points[2], points[3]);
    return strPoint;
  }
  
  public static Object unescapeMatrixD(String strMatrix) {
    if (strMatrix == null || strMatrix.length() == 0)
      return strMatrix;
    String str = strMatrix.replace('\n', ' ').trim();
    if (str.lastIndexOf("[[") != 0 || str.indexOf("]]") != str.length() - 2)
      return strMatrix;
    double[] points = new double[16];
    str = str.substring(2, str.length() - 2).replace('[',' ').replace(']',' ').replace(',',' ');
    int[] next = new int[1];
    int nPoints = 0;
    for (; nPoints < 16; nPoints++) {
      points[nPoints] = PT.parseDoubleNext(str, next);
      if (Double.isNaN(points[nPoints])) {
        break;
      }
    }
    if (!Double.isNaN(PT.parseDoubleNext(str, next)))
      return strMatrix; // overflow
    if (nPoints == 9)
      return M3d.newA9(points);
    if (nPoints == 16)
      return M4d.newA16(points);
    return strMatrix;
  }

  public static String eBS(BS bs) {
    return BS.escape(bs, '(', ')');
  }
  
  public static String eBond(BS bs) {
    return BS.escape(bs, '[', ']');
  }
  
  /**
   * Used only for getProperty("readable",...)
   * 
   * @param name
   * @param info
   * @return tabular listing, with array types
   */
  public static String toReadable(String name, Object info) {
    SB sb =new SB();
    String sep = "";
    if (info == null)
      return "null";
    if (PT.isNonStringPrimitive(info))
      return packageReadable(name, null, info.toString());
    if (info instanceof String)
      return packageReadable(name, null, PT.esc((String) info));
    if (info instanceof SV)
      return packageReadable(name, null, ((SV) info).escape());
    if (AU.isAS(info)) {
      sb.append("[");
      int imax = ((String[]) info).length;
      for (int i = 0; i < imax; i++) {
        sb.append(sep).append(PT.esc(((String[]) info)[i]));
        sep = ",";
      }
      sb.append("]");
      return packageReadableSb(name, "String[" + imax + "]", sb);
    }
    if (AU.isAI(info)) {
      sb.append("[");
      int imax = ((int[]) info).length;
      for (int i = 0; i < imax; i++) {
        sb.append(sep).appendI(((int[]) info)[i]);
        sep = ",";
      }
      sb.append("]");
      return packageReadableSb(name, "int[" + imax + "]", sb);
    }
    if (AU.isAD(info)) {
      sb.append("[");
      int imax = ((double[]) info).length;
      for (int i = 0; i < imax; i++) {
        sb.append(sep).appendD(((double[]) info)[i]);
        sep = ",";
      }
      sb.append("]");
      return packageReadableSb(name, "decimal[" + imax + "]", sb);
    }
    if (AU.isAP(info)) {
      sb.append("[");
      int imax = ((T3d[]) info).length;
      for (int i = 0; i < imax; i++) {
        sb.append(sep).append(eP(((T3d[])info)[i]));
        sep = ",";
      }
      sb.append("]");
      return packageReadableSb(name, "point3f[" + imax + "]", sb);
    }
    if (AU.isASS(info)) {
      sb.append("[");
      int imax = ((String[][]) info).length;
      for (int i = 0; i < imax; i++) {
        sb.append(sep).append(toReadable(null, ((String[][]) info)[i]));
        sep = ",\n";
      }
      sb.append("]");
      return packageReadableSb(name, "String[" + imax + "][]", sb);
    }
    if (AU.isAII(info)) {
      sb.append("[");
      int imax = ((int[][]) info).length;
      for (int i = 0; i < imax; i++) {
        sb.append(sep).append(toReadable(null, ((int[][]) info)[i]));
        sep = ",";
      }
      sb.append("]");
      return packageReadableSb(name, "int[" + imax + "][]", sb);
    }
    if (AU.isADD(info)) {
      sb.append("[\n");
      int imax = ((double[][]) info).length;
      for (int i = 0; i < imax; i++) {
        sb.append(sep).append(toReadable(null, ((double[][]) info)[i]));
        sep = ",\n";
      }
      sb.append("]");
      return packageReadableSb(name, "decimal[][]", sb);
    }
    if (info instanceof Lst<?>) {
      int imax = ((Lst<?>) info).size();
      for (int i = 0; i < imax; i++) {
        sb.append(toReadable(name + "[" + (i + 1) + "]", ((Lst<?>) info).get(i)));
      }
      return packageReadableSb(name, "List[" + imax + "]", sb);
    }
    if (info instanceof M34d || info instanceof T3d) {
      sb.append(e(info));
      return packageReadableSb(name, null, sb);
    }
    if (info instanceof Map<?, ?>) {
      Iterator<?> e = ((Map<?, ?>) info).keySet().iterator();
      while (e.hasNext()) {
        String key = (String) e.next();
        sb.append(toReadable((name == null ? "" : name + ".") + key,
            ((Map<?, ?>) info).get(key)));
      }
      return sb.toString();
    }
    return packageReadable(name, null, PT.toJSON(null, info));
  }

  private static String packageReadableSb(String infoName, String infoType, SB sb) {
    return packageReadable(infoName, infoType, sb.toString());
  }
  
  private static String packageReadable(String infoName, String infoType, String info) {
    String s = (infoType == null ? "" : infoType + "\t");
    if (infoName == null)
      return s + info;
    return "\n" + infoName + "\t" + (infoType == null ? "" : "*" + infoType + "\t") + info;
  }

  public static String escapeModelFileNumber(int iv) {
    return "" + (iv / 1000000) + "." + (iv % 1000000);
  }

  public static String encapsulateData(String name, Object data, int depth) {
    String s;
    switch (depth) {
    case JmolDataManager.DATA_TYPE_AD:
      s = escapeDoubleA((double[]) data, false) + ";\n";
      break;
    case JmolDataManager.DATA_TYPE_ADD:
      s = escapeDoubleAA((double[][]) data, true) + ";\n";
      break;
    case JmolDataManager.DATA_TYPE_ADDD:
      s = escapeDoubleAAA((double[][][]) data, true) + ";\n";
      break;
    default:
      s = data.toString();
      break;
    }
    return "  DATA \"" + name + "\"\n" + s + "    END \"" + name + "\";\n";
    
  }

  public static String unescapeUnicode(String s) {
    int ichMax = s.length();
    SB sb = SB.newN(ichMax);
    int ich = 0;
    while (ich < ichMax) {
      char ch = s.charAt(ich++);
      if (ch == '\\' && ich < ichMax) {
        ch = s.charAt(ich++);
        switch (ch) {
        case 'u':
          if (ich < ichMax) {
            int unicode = 0;
            for (int k = 4; --k >= 0 && ich < ichMax;) {
              char chT = s.charAt(ich);
              int hexit = getHexitValue(chT);
              if (hexit < 0)
                break;
              unicode <<= 4;
              unicode += hexit;
              ++ich;
            }
            ch = (char) unicode;
          }
        }
      }
      sb.appendC(ch);
    }
    return sb.toString();
  }
  
  public static int getHexitValue(char ch) {
    if (ch >= 48 && ch <= 57)
      return ch - 48;
    else if (ch >= 97 && ch <= 102)
      return 10 + ch - 97;
    else if (ch >= 65 && ch <= 70)
      return 10 + ch - 65;
    else
      return -1;
  }

  public static String[] unescapeStringArray(String data) {
    // was only used for  LOAD "[\"...\",\"....\",...]" (coming from implicit string)
    // now also used for simulation peaks array from JSpecView,
    // which double-escapes strings, I guess
    //TODO -- should recognize '..' as well as "..." ?
    if (data == null || !data.startsWith("[") || !data.endsWith("]"))
      return null; 
    Lst<String> v = new  Lst<String>();
    int[] next = new int[1];
    next[0] = 1;
    while (next[0] < data.length()) {
      String s = PT.getQuotedStringNext(data, next);
      if (s == null)
        return null;
      v.addLast(PT.rep(s, "\\\"", "\""));      
      while (next[0] < data.length() && data.charAt(next[0]) != '"')
        next[0]++;
    }    
    return v.toArray(new String[v.size()]);
  }

  /**
   * Jmol-specific post-processing of 
   * the array data returned by Measure.computeHelicalAxis
   * 
   * @param id
   * @param tokType
   * @param a
   * @param b
   * @param pts
   * @return various objects depending upon tokType
   */
  public static Object escapeHelical(String id, int tokType, P3d a, P3d b, T3d[] pts) {
    switch (tokType) {
    case T.point:
      return (pts == null ? new P3d() : pts[0]);
    case T.axis:
    case T.radius:
      return (pts == null ? new V3d() : pts[tokType == T.axis ? 1 : 2]);
    case T.angle:
      return Double.valueOf(pts == null ? Double.NaN : pts[3].x);
    case T.draw:
      return (pts == null ? "" : "draw ID \"" + id + "\" VECTOR " + Escape.eP(pts[0]) + " " + Escape.eP(pts[1]) + " color " + (pts[3].x < 0 ? "{255.0 200.0 0.0}" : "{255.0 0.0 128.0}"));
    case T.measure:
      return (pts == null ? "" : "measure " + Escape.eP(a) + Escape.eP(pts[0]) + Escape.eP(pts[4])) + Escape.eP(b);
    default:
      return (pts == null ? new T3d[0] : pts);
    }
  }
}
