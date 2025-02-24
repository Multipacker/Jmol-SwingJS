package javajs.api;

import javajs.util.V3d;
import javajs.util.V3d;

public interface EigenInterface {
	EigenInterface setM(double[][] n);

	double[] getEigenvalues();

	void fillDoubleArrays(V3d[] eigenVectors, double[] eigenValues);

	double[][] getEigenvectorsDoubleTransposed();
}
