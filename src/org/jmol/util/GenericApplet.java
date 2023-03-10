package org.jmol.util;

import java.awt.Event;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import org.jmol.api.JmolAppletInterface;
import org.jmol.api.JmolCallbackListener;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolSyncInterface;
import org.jmol.api.js.JmolToJSmolInterface;
import org.jmol.c.CBK;
import org.jmol.i18n.GT;
import org.jmol.viewer.JC;
import org.jmol.viewer.Viewer;

import javajs.api.JSInterface;
import javajs.util.Lst;
import javajs.util.PT;
import javajs.util.SB;

/**
 * A collection of all methods necessary for initialization of and communication with the applet.
 * JavaScript and Java applet (which is still part of Jmol-SwingJS).
 * 
 */
public abstract class GenericApplet implements JSInterface, JmolAppletInterface, JmolStatusListener {
  protected static Map<String, Object> htRegistry;

  protected static boolean isJS = /** @j2sNative true || */false;

  private final static int SCRIPT_CHECK = 0;
  private final static int SCRIPT_WAIT = 1;
  private final static int SCRIPT_NOWAIT = 2;

  protected String codeBase;
  protected String documentBase;
  protected boolean isSigned;
  protected String language;

  protected boolean doTranslate = true;
  protected boolean haveDocumentAccess;
  protected boolean isStereoSlave;
  protected boolean mayScript;
  protected String htmlName;
  protected String fullName;
  protected String statusForm;
  protected String statusText;
  protected String statusTextarea;

  protected Object gRight;
  protected Viewer viewer;
  protected Map<CBK, String> callbacks;
  
  protected Map<String, Object> vwrOptions;

  protected boolean haveNotifiedError;

  protected Object appletObject;

  private boolean loading;
  private String syncId;
  private SB outputBuffer;

  @Override
  public Object setStereoGraphics(boolean isStereo) {
    return null;
  }

  @Override
  public boolean processMouseEvent(int id, int x, int y, int modifiers, long time) {
    return viewer.processMouseEvent(id, x, y, modifiers, time);
  }

  @Override
  public void processKeyEvent(Object event) {
    viewer.processKeyEvent(event);
  }

  @Override
  public void setDisplay(Object canvas) {
    viewer.setDisplay(canvas);
  }

  @Override
  public boolean setStatusDragDropped(int mode, int x, int y, String fileName, String[] retType) {
    return viewer.setStatusDragDropped(mode, x, y, fileName, retType);
  }

  @Override
  public void startHoverWatcher(boolean enable) {
    viewer.startHoverWatcher(enable);
  }

  @Override
  public void update() {
    viewer.updateJS();
  }

  @Override
  public void openFileAsyncSpecial(String fileName, int flags) {
    viewer.openFileAsyncSpecial(fileName, flags);
  }

  @Override
  public void openFileAsyncSpecialType(String fileName, int flags, String type) {
    viewer.openFileAsyncSpecialType(fileName, flags, type);
  }

  @Override
  public void processTwoPointGesture(double[][][] touches) {
    viewer.processTwoPointGesture(touches);
  }

  @Override
  public void setScreenDimension(int width, int height) {
    viewer.setScreenDimension(width, height);
  }

  void resizeDisplay(int width, int height) {
    JmolToJSmolInterface jmol = org.jmol.awtjs2d.Platform.Jmol();
    jmol.resizeApplet(viewer.html5Applet, new int[] {width, height});
  }


  protected void init(Object applet) {
    callbacks = new Hashtable<CBK, String>();
    if (htRegistry == null)
      htRegistry = new Hashtable<String, Object>();

    appletObject = applet;
    htmlName = PT.split("" + getJmolParameter("name"), "_object")[0];
    syncId = getJmolParameter("syncId");
    fullName = htmlName + "__" + syncId + "__";
    System.out.println("Jmol JavaScript applet " + fullName + " initializing");
    int iLevel = (getValue("logLevel", (getBooleanValue("debug", false) ? "5" : "4"))).charAt(0) - '0';
    if (iLevel != 4)
      System.out.println("setting logLevel=" + iLevel + " -- To change, use script \"set logLevel [0-5]\"");
    Logger.setLogLevel(iLevel);
    GT.ignoreApplicationBundle();
    initOptions();
    checkIn(fullName, appletObject);
    initApplication();
  }

  private void initApplication() {
    vwrOptions.put("applet", Boolean.TRUE);
    if (getJmolParameter("statusListener") == null)
      vwrOptions.put("statusListener", this);
    language = getJmolParameter("language");
    if (language != null)
      vwrOptions.put("language", language);
    viewer = new Viewer(vwrOptions);
    viewer.pushHoldRepaint();
    String emulate = getValueLowerCase("emulate", "jmol");
    setStringProperty("defaults", emulate.equals("chime") ? "RasMol" : "Jmol");
    setStringProperty("backgroundColor", getValue("bgcolor", getValue("boxbgcolor", "black")));
    viewer.setBooleanProperty("frank", true);
    loading = true;
    for (CBK item : CBK.values()) {
      String name = item.name();
      Object o = getValue(name + "Callback", null);
      if (o != null) {
        if (o instanceof String) {
          setStringProperty(name + "Callback", (String) o);
        } else {
          String def = null;
          setStringProperty(name + "Callback", def);
          setCallback(name, o);
        }
      }
    }
    loading = false;
    if (language != null)
      System.out.print("requested language=" + language + "; ");
    doTranslate = (!"none".equals(language) && getBooleanValue("doTranslate",
        true));
    language = GT.getLanguage();
    System.out.println("language=" + language);

    if (callbacks.get(CBK.SCRIPT) == null
        && callbacks.get(CBK.ERROR) == null)
      if (callbacks.get(CBK.MESSAGE) != null || statusForm != null
          || statusText != null) {
        if (doTranslate && (getValue("doTranslate", null) == null)) {
          doTranslate = false;
          Logger
              .warn("Note -- Presence of message callback disables disable translation;"
                  + " to enable message translation use jmolSetTranslation(true) prior to jmolApplet()");
        }
        if (doTranslate)
          Logger
              .warn("Note -- Automatic language translation may affect parsing of message callbacks"
                  + " messages; use scriptCallback or errorCallback to process errors");
      }

    if (!doTranslate) {
      GT.setDoTranslate(false);
      Logger.warn("Note -- language translation disabled");
    }

    // should the popupMenu be loaded ?
    if (!getBooleanValue("popupMenu", true))
      viewer.getProperty("DATA_API", "disablePopupMenu", null);
    //experimental; never documented loadNodeId(getValue("loadNodeId", null));

    String menuFile = getJmolParameter("menuFile");
    if (menuFile != null)
      viewer.setMenu(menuFile, true);

    String script = getValue("script", "");

    String loadParam = getValue("loadInline", null);
    if (loadParam == null) {
      if ((loadParam = getValue("load", null)) != null)
        script = "load \"" + loadParam + "\";" + script;
      loadParam = null;
    }
    viewer.popHoldRepaint("applet init");
    if (loadParam != null && viewer.loadInline(loadParam) != null)
      script = "";
    if (script.length() > 0)
      scriptProcessor(script, null, SCRIPT_WAIT);
    viewer.notifyStatusReady(true);
  }

  @Override
  public void destroy() {
    gRight = null;
    viewer.notifyStatusReady(false);
    viewer = null;
    checkOut(fullName);
  }

  protected boolean getBooleanValue(String propertyName, boolean defaultValue) {
    String value = getValue(propertyName, defaultValue ? "true" : "");
    return (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on") || value.equalsIgnoreCase("yes"));
  }

  protected String getValue(String propertyName, String defaultValue) {
    String s = getJmolParameter(propertyName);
    System.out.println("Jmol getValue " + propertyName + " " + s);
    return (s == null ? defaultValue : s);
  }

  private String getValueLowerCase(String paramName, String defaultValue) {
    String value = getValue(paramName, defaultValue);
    if (value != null) {
      value = value.trim().toLowerCase();
      if (value.length() == 0)
        value = null;
    }
    return value;
  }

  private void setStringProperty(String name, String value) {
    if (value == null)
      return;
    Logger.info(name + " = \"" + value + "\"");
    viewer.setStringProperty(name, value);
  }

  private String scriptProcessor(String script, String statusParams, int processType) {
    /*
    * Idea here is to provide a single point of entry
    * Synchronization may not work, because it is possible for the NOWAIT variety of
    * scripts to return prior to full execution 
    * 
    */
    //System.out.println("Jmol.script: " + script);
    if (script == null || script.length() == 0)
      return "";
    switch (processType) {
    case SCRIPT_CHECK:
      Object err = viewer.scriptCheck(script);
      return (err instanceof String ? (String) err : "");
    case SCRIPT_WAIT:
      if (statusParams != null)
        return viewer.scriptWaitStatus(script, statusParams).toString();
      return viewer.scriptWait(script);
    case SCRIPT_NOWAIT:
    default:
      return viewer.script(script);
    }
  }

  ///////// JmolSyncInterface //////////

  @Override
  public void register(String id, JmolSyncInterface jsi) {
    checkIn(id, jsi);
  }

  /**
   * JSpecView shares the JmolSyncInterface; used to get JSpecView
   */
  @Override
  public Map<String, Object> getJSpecViewProperty(String key) {
    // only on JSpecView side, as it is also JmolSyncInterface
    return null;
  }

  @Override
  synchronized public void syncScript(String script) {
    viewer.syncScript(script, "~", 0);
  }

  ////////////// JmolAppletInterface //////////////

  @Override
  public boolean handleEvent(Event e) {
    if (viewer == null)
      return false;
    return viewer.processMouseEvent(e.id, e.x, e.y, e.modifiers, e.when);
  }

  @Override
  public String getAppletInfo() {
    return GT
        .o(GT
            .$("Jmol Applet version {0} {1}.\n\nAn OpenScience project.\n\nSee http://www.jmol.org for more information"),
            new Object[] { JC.version, JC.date })
        + "\nhtmlName = "
        + PT.esc(htmlName)
        + "\nsyncId = "
        + PT.esc(syncId)
        + "\ndocumentBase = "
        + PT.esc(documentBase)
        + "\ncodeBase = " + PT.esc(codeBase);
  }

  @Override
  public void script(String script) {
    scriptNoWait(script);
  }

  @Override
  public String scriptCheck(String script) {
    if (script == null || script.length() == 0)
      return "";
    return scriptProcessor(script, null, SCRIPT_CHECK);
  }

  @Override
  public String scriptNoWait(String script) {
    if (script == null || script.length() == 0)
      return "";
    return scriptProcessor(script, null, SCRIPT_NOWAIT);
  }

  @Override
  public String scriptWait(String script) {
    return scriptWait(script, null);
  }

  @Override
  public String scriptWait(String script, String statusParams) {
    if (script == null || script.length() == 0)
      return "";
    outputBuffer = null;
    return scriptProcessor(script, statusParams, SCRIPT_WAIT);
  }

  @Override
  public String scriptWaitOutput(String script) {
    if (script == null || script.length() == 0)
      return "";
    outputBuffer = new SB();
    viewer.scriptWaitStatus(script, "");
    String str = (outputBuffer == null ? "" : outputBuffer.toString());
    outputBuffer = null;
    return str;
  }

  @Override
  public int getModelIndexFromId(String id) {
    return viewer.getModelIndexFromId(id);
  }
  
  @Override
  public Object getProperty(String infoType) {
    return viewer.getProperty(null, infoType, "");
  }

  @Override
  public Object getProperty(String infoType, String paramInfo) {
    return viewer.getProperty(null, infoType, paramInfo);
  }

  @Override
  public String getPropertyAsString(String infoType) {
    return viewer.getProperty("readable", infoType, "").toString();
  }

  @Override
  public String getPropertyAsString(String infoType, String paramInfo) {
    return viewer.getProperty("readable", infoType, paramInfo).toString();
  }

  @Override
  public String getPropertyAsJSON(String infoType) {
    return viewer.getProperty("JSON", infoType, "").toString();
  }

  @Override
  public String getPropertyAsJSON(String infoType, String paramInfo) {
    return viewer.getProperty("JSON", infoType, paramInfo).toString();
  }

  @Override
  public String loadInlineString(String strModel, String script, boolean isAppend) {
    String errMsg = viewer.loadInlineAppend(strModel, isAppend);
    if (errMsg == null)
      script(script);
    return errMsg;
  }

  @Override
  public String loadInlineArray(String[] strModels, String script, boolean isAppend) {
    if (strModels == null || strModels.length == 0)
      return null;
    String errMsg = viewer.loadInline(strModels, isAppend);
    if (errMsg == null)
      script(script);
    return errMsg;
  }

  @Override
  public String loadDOMNode(Object DOMNode) {
    // This should provide a route to pass in a browser DOM node
    // directly as a JSObject. Unfortunately does not seem to work with
    // current browsers
    return viewer.openDOM(DOMNode);
  }

  /// called by mystatuslisteners

  public void output(String s) {
    if (outputBuffer != null && s != null)
      outputBuffer.append(s).appendC('\n');
  }

  /**
   * set a callback either as a function or a function name from JavaScript
   * 
   */
  @Override
  public void setCallback(String name, Object callbackObject) {
    viewer.sm.setCallbackFunction(name, callbackObject);
  }

  /**
   * From StatusManager
   */
  @Override
  public void setCallbackFunction(String callbackName, String callbackObject) {
    //also serves to change language for callbacks and menu
    if (callbackName.equalsIgnoreCase("language")) {
      consoleMessage(""); // clear
      consoleMessage(null); // show default message
      return;
    }
    CBK callback = CBK.getCallback(callbackName);
    if (callback != null && (loading || callback != CBK.EVAL)) {
      if (callbackObject == null)
        callbacks.remove(callback);
      else
        callbacks.put(callback, callbackObject);
      return;
    }
    consoleMessage("Available callbacks include: " + CBK.getNameList().replace(';', ' ').trim());
  }

  private void consoleMessage(String message) {
    notifyCallback(CBK.ECHO, new Object[] { "", message });
  }

  /////////////  JmolStatusListener ///////////
  
  @Override
  public boolean notifyEnabled(CBK type) {
    switch (type) {
    case SYNC:
      if (!isJS)
        return false;
      //$FALL-THROUGH$
    case ANIMFRAME:
    case DRAGDROP:
    case ECHO:
    case ERROR:
    case EVAL:
    case IMAGE:
    case LOADSTRUCT:
    case MEASURE:
    case MESSAGE:
    case PICK:
    case SCRIPT:
      return true;
    case AUDIO: // Jmol 14.29.2
    case APPLETREADY: // Jmol 12.1.48
    case ATOMMOVED: // Jmol 12.1.48
    case CLICK:
    case HOVER:
    case MINIMIZATION:
    case MODELKIT:
    case RESIZE:
    case SELECT:
    case SERVICE:
    case STRUCTUREMODIFIED:
      break;
    }
    return (callbacks.get(type) != null);
  }

  /**
   * @param type the callback type or null for getJsObjectInfo() -- Java applet only
   * @param data type-dependent
   */
  @Override
  public void notifyCallback(CBK type, Object[] data) {
    Object callback = (type == null ? null : callbacks.get(type));
    boolean doCallback = (type == null || callback != null && (data == null || data[0] == null));
    boolean toConsole = false;
    if (data != null)
      data[0] = htmlName;
    String strInfo = (data == null || data[1] == null ? null : data[1].toString());

    if (type != null)
    switch (type) {
    case APPLETREADY:
      data[3] = appletObject;
      break;
    case AUDIO:
    case ERROR:
    case EVAL:
    case HOVER:
    case IMAGE:
    case MINIMIZATION:
    case SERVICE:
    case RESIZE:
    case DRAGDROP:
    case ATOMMOVED:
    case SELECT:
    case MODELKIT:
    case STRUCTUREMODIFIED:
//    int mode = ((Integer) data[1]).intValue();
//    int atomIndex = ((Integer) data[2]).intValue();
//    int modelIndex = ((Integer) data[3]).intValue();
//    switch (mode) {
//    case 1/-1: // assign atom
//    case 2/-2: // assign bond
//    case 3/-3: // 
//    case 4/-4: // delete atoms
//    case 5/-5: // delete models
//    }
      // just send it
      break;
    case CLICK:
      // x, y, action, int[] {action}
      // the fourth parameter allows an application to change the action
      if ("alert".equals(callback))
        strInfo = "x=" + data[1] + " y=" + data[2] + " action=" + data[3]
            + " clickCount=" + data[4];
      break;
    case ANIMFRAME:
      // Note: twos-complement. To get actual frame number, use
      // Math.max(frameNo, -2 - frameNo)
      // -1 means all frames are now displayed
      int[] iData = (int[]) data[1];
      int frameNo = iData[0];
      int fileNo = iData[1];
      int modelNo = iData[2];
      int firstNo = iData[3];
      int lastNo = iData[4];
      boolean isAnimationRunning = (frameNo <= -2);
      int animationDirection = (firstNo < 0 ? -1 : 1);
      int currentDirection = (lastNo < 0 ? -1 : 1);

      /*
       * animationDirection is set solely by the "animation direction +1|-1"
       * script command currentDirection is set by operations such as
       * "anim playrev" and coming to the end of a sequence in
       * "anim mode palindrome"
       * 
       * It is the PRODUCT of these two numbers that determines what direction
       * the animation is going.
       */
      if (doCallback) {
        data = new Object[] { htmlName,
            Integer.valueOf(Math.max(frameNo, -2 - frameNo)),
            Integer.valueOf(fileNo), Integer.valueOf(modelNo),
            Integer.valueOf(Math.abs(firstNo)),
            Integer.valueOf(Math.abs(lastNo)),
            Integer.valueOf(isAnimationRunning ? 1 : 0),
            Integer.valueOf(animationDirection),
            Integer.valueOf(currentDirection),
            data[2]/*entryName*/, data[3]/*morphModel*/
            };
      }
      break;
    case ECHO:
      boolean isPrivate = (data.length == 2);
      boolean isScriptQueued = (isPrivate || ((Integer) data[2]).intValue() == 1);
      if (!doCallback) {
        if (isScriptQueued)
          toConsole = true;
        doCallback = (!isPrivate && (callback = callbacks
            .get((type = CBK.MESSAGE))) != null);
      }
      if (!toConsole)
        output(strInfo);
      break;
    case LOADSTRUCT:
      String errorMsg = (String) data[4];
      if (errorMsg != null) {
        errorMsg = (errorMsg.indexOf("NOTE:") >= 0 ? "" : GT.$("File Error:"))
            + errorMsg;
        doShowStatus(errorMsg);
        notifyCallback(CBK.MESSAGE, new Object[] { "", errorMsg });
        return;
      }
      break;
    case MEASURE:
      // pending, deleted, or completed
      if (!doCallback)
        doCallback = ((callback = callbacks.get((type = CBK.MESSAGE))) != null);
      String status = (String) data[3];
      if (status.indexOf("Picked") >= 0 || status.indexOf("Sequence") >= 0) {// picking mode
        doShowStatus(strInfo); // set picking measure distance
        toConsole = true;
      } else if (status.indexOf("Completed") >= 0) {
        strInfo = status + ": " + strInfo;
        toConsole = true;
      }
      break;
    case MESSAGE:
      toConsole = !doCallback;
      doCallback &= (strInfo != null);
      if (!toConsole)
        output(strInfo);
      break;
    case PICK:
      doShowStatus(strInfo);
      toConsole = true;
      break;
    case SCRIPT:
      int msWalltime = ((Integer) data[3]).intValue();
      // general message has msWalltime = 0
      // special messages have msWalltime < 0
      // termination message has msWalltime > 0 (1 + msWalltime)
      if (msWalltime > 0) {
        // termination -- button legacy -- unused
      } else if (!doCallback) {
        // termination messsage ONLY if script callback enabled -- not to
        // message queue
        // for compatibility reasons
        doCallback = ((callback = callbacks.get((type = CBK.MESSAGE))) != null);
      }
      output(strInfo);
      doShowStatus(strInfo);
      break;
    case SYNC:
      sendScript(strInfo, (String) data[2], true, doCallback);
      return;
    }
    if (toConsole) {
      JmolCallbackListener appConsole = (JmolCallbackListener) viewer
          .getProperty("DATA_API", "getAppConsole", null);
      if (appConsole != null) {
        appConsole.notifyCallback(type, data);
        output(strInfo);
      }
    }
    if (!doCallback || !mayScript)
      return;
    try {
      doSendCallback(type, callback, data, strInfo);
    } catch (Exception e) {
      if (!haveNotifiedError)
        if (Logger.debugging) {
          Logger.debug(type.name() + "Callback call error to " + callback
              + ": " + e);
        }
      haveNotifiedError = true;
    }
  }

  private String sendScript(String script, String appletName, boolean isSync,
                            boolean doCallback) {
    if (!isJS)
      return "";
    if (doCallback) {
      script = notifySync(script, appletName);
      // if the notified JavaScript function returns "" or 0, then 
      // we do NOT continue to notify the other applets
      if (script == null || script.length() == 0 || script.equals("0"))
        return "";
    }

    Lst<String> apps = new Lst<String>();
    findApplets(appletName, syncId, fullName, apps);
    int nApplets = apps.size();
    if (nApplets == 0) {
      if (!doCallback && !appletName.equals("*"))
        Logger.error(fullName + " couldn't find applet " + appletName);
      return "";
    }
    SB sb = (isSync ? null : new SB());
    boolean getGraphics = (isSync && script.equals(Viewer.SYNC_GRAPHICS_MESSAGE));
    boolean setNoGraphics = (isSync && script.equals(Viewer.SYNC_NO_GRAPHICS_MESSAGE));
    if (getGraphics)
      viewer.setStereo(false, (gRight = null));
    for (int i = 0; i < nApplets; i++) {
      String theApplet = apps.get(i);
      JmolSyncInterface app = (JmolSyncInterface) htRegistry.get(theApplet);
      boolean isScriptable = true;//(app instanceof JmolScriptInterface);
      if (Logger.debugging)
        Logger.debug(fullName + " sending to " + theApplet + ": " + script);
      try {
        if (isScriptable && (getGraphics || setNoGraphics)) {
          viewer.setStereo(isStereoSlave = getGraphics, gRight = ((JmolAppletInterface) app).setStereoGraphics(getGraphics));
          return "";
        }
        if (isSync)
          app.syncScript(script);
        else if (isScriptable)
          sb.append(((JmolAppletInterface) app).scriptWait(script, "output")).append("\n");
      } catch (Exception e) {
        String msg = htmlName + " couldn't send to " + theApplet + ": "
            + script + ": " + e;
        Logger.error(msg);
        if (!isSync)
          sb.append(msg);
      }
    }
    return (isSync ? "" : sb.toString());
  }

  private String notifySync(String info, String appletName) {
    String syncCallback = callbacks.get(CBK.SYNC);
    if (!mayScript || syncCallback == null)
      return info;
    try {
      return doSendCallback(CBK.SYNC, syncCallback, new Object[] { fullName, info,
          appletName }, null);
    } catch (Exception e) {
      if (!haveNotifiedError)
        if (Logger.debugging) {
          Logger.debug("syncCallback call error to " + syncCallback + ": " + e);
        }
      haveNotifiedError = true;
    }
    return info;
  }

  @Override
  public String eval(String strEval) {
    // may be appletName\1script
    int pt = strEval.indexOf("\1");
    if (pt >= 0)
      return sendScript(strEval.substring(pt + 1), strEval.substring(0, pt),
          false, false);
    if (!haveDocumentAccess)
      return "NO EVAL ALLOWED";
    if (callbacks.get(CBK.EVAL) != null) {
      notifyCallback(CBK.EVAL, new Object[] { null, strEval });
      return "";
    }
    return doEval(strEval);
  }

  @Override
  public String createImage(String fileName, String type, Object text_or_bytes,
                            int quality) {
    // not implemented
    return null;
  }

  @Override
  public Map<String, Object> getRegistryInfo() {
    checkIn(null, null); //cleans registry
    return htRegistry;
  }

  @Override
  public void showUrl(String urlString) {
    if (Logger.debugging)
      Logger.debug("showUrl(" + urlString + ")");
    if (urlString != null && urlString.length() > 0)
      try {
        doShowDocument(new URL((URL) null, urlString, null));
      } catch (MalformedURLException mue) {
        consoleMessage("Malformed URL:" + urlString);
      }
  }

  @Override
  public int[] resizeInnerPanel(String data) {
    double[] dims = new double[2];
    Parser.parseStringInfestedDoubleArray(data, null, dims);
    resizeDisplay((int) dims[0], (int) dims[1]);
    return new int[] { (int) dims[0], (int) dims[1] };
  }

  //////////// applet registration for direct applet-applet communication ////////////

  synchronized static void checkIn(String name, Object applet) {
    if (name != null) {
      Logger.info("AppletRegistry.checkIn(" + name + ")");
      htRegistry.put(name, applet);
    }
    if (Logger.debugging) {
      for (Map.Entry<String, Object> entry : htRegistry.entrySet()) {
        String theApplet = entry.getKey();
        Logger.debug(theApplet + " " + entry.getValue());
      }
    }
  }

  synchronized static void checkOut(String name) {
    htRegistry.remove(name);
  }

  synchronized static void findApplets(String appletName, String mySyncId,
                                       String excludeName, Lst<String> apps) {
    if (appletName != null && appletName.indexOf(",") >= 0) {
      String[] names = PT.split(appletName, ",");
      for (int i = 0; i < names.length; i++)
        findApplets(names[i], mySyncId, excludeName, apps);
      return;
    }
    String ext = "__" + mySyncId + "__";
    //System.out.println("findApplet looking for " + ext + " appletName=" + appletName + " " + htRegistry.containsKey(appletName));
    if (appletName == null || appletName.equals("*") || appletName.equals(">")) {
      for (String appletName2 : htRegistry.keySet()) {
        //System.out.println("findApplet key=" + appletName2);
        if (!appletName2.equals(excludeName) && appletName2.indexOf(ext) > 0) {
          //System.out.println("findApplet found " + appletName2);
          apps.addLast(appletName2);
        }
      }
      return;
    }
    if (excludeName.indexOf("_object") >= 0 && appletName.indexOf("_object") < 0)
      appletName += "_object";
    if (appletName.indexOf("__") < 0)
      appletName += ext;
    if (!htRegistry.containsKey(appletName))
      appletName = "jmolApplet" + appletName;
    if (!appletName.equals(excludeName) && htRegistry.containsKey(appletName)) {
      apps.addLast(appletName);
      //System.out.println("findApplet found2 " + appletName);
    }
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void notifyAudioEnded(Object htParams) {
    viewer.sm.notifyAudioStatus((Map<String, Object>) htParams);
  }
  
  protected Map<String, Object> htParams;

  protected void setJSOptions(Map<String, Object> vwrOptions) {
    htParams = new Hashtable<String, Object>();
    if (vwrOptions == null)
      vwrOptions = new Hashtable<String, Object>();
    this.vwrOptions = vwrOptions;
    for (Map.Entry<String, Object> entry : vwrOptions.entrySet())
      htParams.put(entry.getKey().toLowerCase(), entry.getValue());
    documentBase = "" + vwrOptions.get("documentBase");
    codeBase = "" + vwrOptions.get("codePath");
  }

  protected void initOptions() {
    vwrOptions.remove("debug");
    vwrOptions.put("fullName", fullName);
    haveDocumentAccess = "true".equalsIgnoreCase(""
        + getValue("allowjavascript", "true"));
    mayScript = true;
  }

  protected String getJmolParameter(String paramName) {
    Object o = htParams.get(paramName.toLowerCase());
    return (o == null ? null : "" + o);
  }

  @Override
  public double[][] functionXY(String functionName, int nX, int nY) {
    /*three options:
     * 
     *  nX > 0  and  nY > 0        return one at a time, with (slow) individual function calls
     *  nX < 0  and  nY > 0        return a string that can be parsed to give the list of values
     *  nX < 0  and  nY < 0        fill the supplied double[-nX][-nY] array directly in JavaScript 
     *  
     */

    //System.out.println("functionXY" + nX + " " + nY  + " " + functionName);
    double[][] fxy = new double[Math.abs(nX)][Math.abs(nY)];
    if (!mayScript || !haveDocumentAccess || nX == 0 || nY == 0)
      return fxy;
    try {
      if (nX > 0 && nY > 0) { // fill with individual function calls (slow)
        for (int i = 0; i < nX; i++)
          for (int j = 0; j < nY; j++) {
            /**
             * @j2sNative
             * 
             *            fxy[i][j] = window.eval(functionName)(this.htmlName, i, j);
             */
            {
            }
          }
      } else if (nY > 0) { // fill with parsed values from a string (pretty fast)
        String data;
        /**
         * @j2sNative
         * 
         *            data = window.eval(functionName)(this.htmlName, nX, nY);
         * 
         */
        {
          data = "";
        }
        nX = Math.abs(nX);
        double[] fdata = new double[nX * nY];
        Parser.parseStringInfestedDoubleArray(data, null, fdata);
        for (int i = 0, ipt = 0; i < nX; i++) {
          for (int j = 0; j < nY; j++, ipt++) {
            fxy[i][j] = fdata[ipt];
          }
        }
      } else { // fill double[][] directly using JavaScript
        /**
         * @j2sNative
         * 
         *            data = window.eval(functionName)(this.htmlName, nX, nY, fxy);
         * 
         */
        {
          System.out.println(functionName);
        }
      }
    } catch (Exception e) {
      Logger.error("Exception " + e + " with nX, nY: " + nX + " " + nY);
    }
    // for (int i = 0; i < nX; i++)
    // for (int j = 0; j < nY; j++)
    //System.out.println("i j fxy " + i + " " + j + " " + fxy[i][j]);
    return fxy;
  }

  @Override
  public double[][][] functionXYZ(String functionName, int nX, int nY, int nZ) {
    double[][][] fxyz = new double[Math.abs(nX)][Math.abs(nY)][Math.abs(nZ)];
    if (!mayScript || !haveDocumentAccess || nX == 0 || nY == 0 || nZ == 0)
      return fxyz;
    try {
      /**
       * @j2sNative
       * 
       *            window.eval(functionName)(this.htmlName, nX, nY, nZ, fxyz);
       * 
       */
      {
      }
    } catch (Exception e) {
      Logger.error("Exception " + e + " for " + functionName
          + " with nX, nY, nZ: " + nX + " " + nY + " " + nZ);
    }
    // for (int i = 0; i < nX; i++)
    // for (int j = 0; j < nY; j++)
    // for (int k = 0; k < nZ; k++)
    //System.out.println("i j k fxyz " + i + " " + j + " " + k + " " + fxyz[i][j][k]);
    return fxyz;
  }

  protected void doShowDocument(URL url) {
    String[] surl = PT.split(url.toString(), "?POST?");
    if (surl.length == 1) {
      /**
       * @j2sNative
       * 
       *            window.open(surl[0]);
       * 
       */
    {}
     return;
      }
   
    String f = "<form id=f method=POST action='" + surl[0] + "'>";
    f += "<input type='hidden' name='name' value='nmr-1h-prediction' id='name'>";
    f += "<input type='submit' value='working...'>";
    String[] fields = surl[1].split("&");
    for (int i = 0; i < fields.length; i++) {
      String field = fields[i];
      int pt = field.indexOf("=");
      String name = field.substring(0, pt);
      String value = field.substring(pt);
      if (value.indexOf("\n") >= 0) {
        f +="<textarea style='display:none' name=" + name + ">" + value +"</textarea>";
      } else {
        f +="<input type=hidden name=" + name + " value=\""+ value +"\">";
      }
    }
    f += "</form>";
    /**
     * @j2sNative
     * var w=window.open("");w.document.write(f);w.document.getElementById("f").submit();
     * 
     */
    {
      System.out.println(f + url);
    }
  }

  @SuppressWarnings("unused")
  protected String doSendCallback(CBK type, Object callback, Object[] data, String strInfo) {
    boolean isString = (callback instanceof String);
    if (callback == null || isString && ((String) callback).length() == 0) {
    } else {
      if (isString && "alert".equals(callback)) {
        /**
         * @j2sNative alert(strInfo);
         */
        {
        }
        return "";
      }
      String[] tokens = (isString ? PT.split(((String) callback), ".") : null);
      try{
        /**
         * @j2sNative
         * 
         *              var o; 
         *              if (isString) {
         *                o = window[tokens[0]]; 
         *                for (var i = 1; i < tokens.length; i++) 
         *                o = o[tokens[i]];
         *              } else {
         *                o = callback;
         *              }
         *              for (var i = 0; i < data.length; i++) {
         *                data[i] && data[i].booleanValue && (data[i] = data[i].booleanValue());
         *                data[i] instanceof Number && (data[i] = +data[i]);
         *              }
         *              return o.apply(this,data)
         */
        {
        }
      } catch (Throwable e) { 
        System.out.println("callback " + type + " failed " + e); 
      }
    }
    return "";
  }

  /**
   * return RAW JAVASCRIPT OBJECT, NOT A STRING 
   * 
   * @param strEval 
   * @return result, not necessarily a String
   */
  protected String doEval(String strEval) {
    try {
      /**
       * 
       * @j2sNative
       * 
       *            return window.eval(strEval);
       */
      {
      }
    } catch (Throwable e) {
      Logger.error("# error evaluating " + strEval + ":" + e.toString());
    }
    return "";
  }

  protected void doShowStatus(String message) {
    try {
      System.out.println(message);
    } catch (Exception e) {
      //ignore if page is closing
    }
  }

  /**
   * This method is only called by JmolGLmol applet._refresh();
   * 
   * @return enough data to update a WebGL view
   * 
   */
  public Object getGLmolView() {
    return viewer.getGLmolView();
  }

  /**
   * possibly called from JSmolApplet.js upon start up
   *  
   * @param fileName
   * @return error or null
   */
  
  public String openFile(String fileName) {
    return viewer.openFile(fileName);
  }

  // JSInterface -- methods called from JSmol JavaScript library
  
  @Override
  public int cacheFileByName(String fileName, boolean isAdd) {
    return viewer.cacheFileByName(fileName, isAdd);
  }

  @Override
  public void cachePut(String key, Object data) {
    viewer.cachePut(key, data);
  }

  @Override
  public String getFullName() {
    return fullName;
  }

}
