package org.jmol.adapter.writers;

import org.apache.log4j.helpers.AbsoluteTimeDateFormat;

import javajs.util.P3d;
import javajs.util.P3d;
import javajs.util.PT;

public class XtlWriter {
  protected boolean haveUnitCell = true;

  private static final String[] twelfths = new String[] { 
      "0.000000000000000",
      "0.083333333333333", "0.166666666666667", "0.250000000000000", 
      "0.333333333333333", "0.416666666666667", "0.500000000000000", 
      "0.583333333333333", "0.666666666666667", "0.750000000000000", 
      "0.833333333333333", "0.916666666666667", "1.000000000000000", };

  private static final String[] twelfthsF = new String[] { 
      "0.0000000",
      "0.0833333", "0.1666667", "0.2500000", "0.3333333",
      "0.4166667", "0.5000000", "0.5833333", "0.6666667",
      "0.7500000", "0.8333333", "0.9166667", "1.0000000", };


  private static int twelfthsOf(double f, double p) {
    if (f == 0)
      return 0;
    f = Math.abs(f * 12);
    int i = (int) Math.round(f);
    return (i <= 12 && Math.abs(f - i) < p * 12 ? i : -1);
  }

  /**
   * Write the double-precision coord, cleaned to twelths. 
   * Do not use this method unless the value really is a float, 
   * because (double) on a float will introduce 5-6 bits of garbage.
   * @param f
   * @return 26-wide column value
   */
  protected String clean(double f) {
    int t;
    return (!haveUnitCell || (t = twelfthsOf(f, 0.00000000015)) < 0
        ? PT.formatD(f, 26, 15, false, false)
        : (f < 0 ? "        -" : "         ") + twelfths[t]);
  }

  protected String cleanF(double f) {
    return clean(f);
  }
}
