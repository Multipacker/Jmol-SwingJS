package javajs.util;

/**
 * byte converter
 * 
 * @author Bob Hanson hansonr@stolaf.edu
 */
public class BC {
  public BC() {
    // unnecessary to instantialize unless subclassed
  }
  
  public static float bytesToFloat(byte[] bytes, int j, boolean isBigEndian) throws Exception {
    return intToFloat(bytesToInt(bytes, j, isBigEndian));
  }

  public static int bytesToShort(byte[] bytes, int j, boolean isBigEndian) {
    int n = (isBigEndian ? (bytes[j + 1] & 0xff) | (bytes[j] & 0xff) << 8 : (bytes[j++] & 0xff) | (bytes[j++] & 0xff) << 8);
      return (n > 0x7FFF ? n - 0x10000 : n);
  }

  public static int bytesToInt(byte[] bytes, int j, boolean isBigEndian) {
    int n = (isBigEndian ? (bytes[j + 3] & 0xff) | (bytes[j + 2] & 0xff) << 8
        | (bytes[j + 1] & 0xff) << 16 | (bytes[j] & 0xff) << 24
        : (bytes[j++] & 0xff) | (bytes[j++] & 0xff) << 8
            | (bytes[j++] & 0xff) << 16 | (bytes[j++] & 0xff) << 24);
    
    return n; 
  }

  public static int intToSignedInt(int n) {
	    return n; 
  }
  public static float intToFloat(int x) throws Exception {
    return Float.intBitsToFloat(x);
  }

  /**
   * see http://en.wikipedia.org/wiki/Binary64
   *  
   * not concerning ourselves with very small or very large numbers and getting
   * this exactly right. Just need a float here.
   * 
   * @param bytes
   * @param j
   * @param isBigEndian
   * @return float
   */
  public static float bytesToDoubleToFloat(byte[] bytes, int j, boolean isBigEndian) { {
      // IEEE754: sign (1 bit), exponent (11 bits), fraction (52 bits).
      // seeeeeee eeeeffff ffffffff ffffffff ffffffff xxxxxxxx xxxxxxxx xxxxxxxx
      //     b1      b2       b3       b4       b5    ---------float ignores----

        if (fracIEEE == null) {
           setFracIEEE();
		}
        
        double d;
        
        if (isBigEndian) {
          d = Double.longBitsToDouble((((long) bytes[j]) & 0xff) << 56
             | (((long) bytes[j + 1]) & 0xff) << 48
             | (((long) bytes[j + 2]) & 0xff) << 40
             | (((long) bytes[j + 3]) & 0xff) << 32
             | (((long) bytes[j + 4]) & 0xff) << 24
             | (((long) bytes[j + 5]) & 0xff) << 16
             | (((long) bytes[j + 6]) & 0xff) << 8 
             | (((long) bytes[7]) & 0xff));
		} else {
          d = Double.longBitsToDouble((((long) bytes[j + 7]) & 0xff) << 56
             | (((long) bytes[j + 6]) & 0xff) << 48
             | (((long) bytes[j + 5]) & 0xff) << 40
             | (((long) bytes[j + 4]) & 0xff) << 32
             | (((long) bytes[j + 3]) & 0xff) << 24
             | (((long) bytes[j + 2]) & 0xff) << 16
             | (((long) bytes[j + 1]) & 0xff) << 8 
             | (((long) bytes[j]) & 0xff));
		}
        return (float) d;
      }
  }

  private static float[] fracIEEE;

  private static void setFracIEEE() {
    fracIEEE = new float[270];
    for (int i = 0; i < 270; i++) {
      fracIEEE[i] = (float) Math.pow(2, i - 141);
	}
  }

  /**
   * only concerned about reasonable float values here -- private but not designated; called by JavaScript
   * 
   * @param f
   * @param i
   * @return f * 2^i
   */
  static double shiftIEEE(double f, int i) {
    if (f == 0 || i < -140) {
      return 0;
	}
    if (i > 128) {
      return Float.MAX_VALUE;
	}
    return f * fracIEEE[i + 140];
  }

	public static void bytesToFloats(byte[] src, int srcpos, float[] dst, int dstpos, int nfloats) {
		float[] f = /** @j2sNative new Float32Array(src.buffer) || */null;
		System.arraycopy(f, srcpos << 2, dst, dstpos, nfloats);
	}

	public static void bytesToDouble(byte[] src, int srcpos, double[] dst, int dstpos, int ndoubles) {
		double[] f = /** @j2sNative new Float64Array(src.buffer) || */null;
		System.arraycopy(f, srcpos << 2, dst, dstpos, ndoubles);
	}
}
