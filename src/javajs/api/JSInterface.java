package javajs.api;

/** 
 * called by JSmol JavaScript methods using
 * 
 *  this._applet.xxxx()
 */
public interface JSInterface {
	int cacheFileByName(String fileName, boolean isAdd);
	void cachePut(String key, Object data);
	void destroy();
	String getFullName();
	void openFileAsyncSpecial(String fileName, int flags);
	void openFileAsyncSpecialType(String fileName, int flags, String type);
	boolean processMouseEvent(int id, int x, int y, int modifiers, long time);
	void processTwoPointGesture(double[][][] touches);
	void processKeyEvent(Object event);
	void setDisplay(Object canvas);
	void setScreenDimension(int width, int height);
	boolean setStatusDragDropped(int mode, int x, int y, String fileName, String[] retType);
	void startHoverWatcher(boolean enable);
	void update();
}
