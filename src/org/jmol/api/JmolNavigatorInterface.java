package org.jmol.api;

import javajs.util.Lst;
import javajs.util.V3d;
import org.jmol.viewer.TransformManager;
import org.jmol.viewer.Viewer;

public interface JmolNavigatorInterface extends Runnable {
	void set(TransformManager transformManager, Viewer vwr);

	void zoomByFactor(double factor, int x, int y);

	void calcNavigationPoint();

	void setNavigationOffsetRelative();//boolean navigatingSurface);

	void navigateKey(int keyCode, int modifiers);

	void navigateList(JmolScriptEvaluator eval, Lst<Object[]> list);

	void navigateAxis(V3d rotAxis, double degrees);

	void setNavigationDepthPercent(double percent);

	String getNavigationState();

	void navTranslatePercentOrTo(double seconds, double x, double y);

	void interrupt();
}
