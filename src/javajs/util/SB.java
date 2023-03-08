
package javajs.util;

import java.nio.charset.Charset;

/**
 * Interesting thing here is that JavaScript is 3x faster than Java in handling strings.
 * 
 * Java StringBuilder is final, unfortunately. I guess they weren't thinking about Java2Script!
 * 
 * The reason we have to do this that several overloaded append methods is WAY too expensive
 */
public class SB {
  private java.lang.StringBuilder sb;
  
  //TODO: JS experiment with using array and .push() here

  public SB() {
      sb = new java.lang.StringBuilder();
  }

  public static SB newN(int n) {
      // not perfect, because it requires defining sb twice. 
      // We can do better...
      SB sb = new SB();
      sb.sb = new java.lang.StringBuilder(n);
      return sb;
  }

  public static SB newS(String s) {
    SB sb = new SB();
    sb.sb = new java.lang.StringBuilder(s);
    return sb;
  }

  public SB append(String s) {
      sb.append(s);
    return this;
  }
  
  public SB appendC(char c) {
      sb.append(c);
    return this;
    
  }

  public SB appendI(int i) {
      sb.append(i);
    return this;
  }

  public SB appendB(boolean b) {
      sb.append(b);
    return this;
  }

  /**
   * note that JavaScript could drop off the ".0" in "1.0"
   * @param f
   * @return this
   */
  public SB appendF(float f) {
      sb.append(f);
    return this;
  }

  public SB appendD(double d) {
      sb.append(d);
    return this;
  }

  public SB appendSB(SB buf) {
      sb.append(buf.sb);
    return this;
  }

  public SB appendO(Object data) {
    if (data != null) {
        sb.append(data);
    }
    return this;
  }

  public void appendCB(char[] cb, int off, int len) {
       sb.append(cb, off, len);
  }

  @Override
  public String toString() {
      return sb.toString();
  }

  public int length() {
      return sb.length();
  }

  public int indexOf(String s) {
      return sb.indexOf(s);
  }

  public char charAt(int i) {
      return sb.charAt(i);
  }

  public int charCodeAt(int i) {
      return sb.codePointAt(i);
  }

  public void setLength(int n) {
      sb.setLength(n);
  }

  public int lastIndexOf(String s) {
      return sb.lastIndexOf(s);
  }

  public int indexOf2(String s, int i) {
      return sb.indexOf(s, i);
  }

  public String substring(int i) {
      return sb.substring(i);
  }

  public String substring2(int i, int j) {
      return sb.substring(i, j);
  }

  /**
   * simple byte conversion properly implementing UTF-8. * Used for base64
   * conversion and allows for offset
   * 
   * @param off
   * @param len
   *        or -1 for full length (then off must = 0)
   * @return byte[]
   */
  public byte[] toBytes(int off, int len) {
    if (len == 0) {
      return new byte[0];
	}
    Charset cs = Charset.forName("UTF-8");
    return (len > 0 ? substring2(off, off + len) : off == 0 ? toString() : substring2(off, length() - off)).getBytes(cs);
  }

	public void replace(int start, int end, String str) {
		sb.replace(start, end, str);
	}

	public void insert(int offset, String str) {
		replace(offset, offset, str);
	}
}
