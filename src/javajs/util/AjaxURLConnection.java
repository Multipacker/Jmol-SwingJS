package javajs.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javajs.api.js.J2SObjectInterface;
import swingjs.api.JSUtilI;

/**
 * A method to allow a JavaScript Ajax
 */
public class AjaxURLConnection extends HttpURLConnection {
	static private JSUtilI jsutil = null;
	static {
		try {
			jsutil = (JSUtilI) Class.forName("swingjs.JSUtil").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static class AjaxHttpsURLConnection extends AjaxURLConnection {
		protected AjaxHttpsURLConnection(URL url) {
			super(url);
		}
	}

	public static URLConnection newConnection(URL url) {
		return (url.getProtocol() == "https" ? new AjaxHttpsURLConnection(url) : new AjaxURLConnection(url));
	}

	protected AjaxURLConnection(URL url) {
		super(url);
		ajax = null;
	}

	byte[] bytesOut;
	String postOut = "";
	ByteArrayOutputStream streamOut;

	private Object ajax;
	Object info;

	@Override
	public String getHeaderField(String name) {
		try {
			if (getResponseCode() != -1) {
				return null;
			}
		} catch (IOException e) {
		}
		return null;
	}

	@SuppressWarnings({ "unused", "null" })
	@Override
	public Map<String, List<String>> getHeaderFields() {
		try {
			getResponseCode();
		} catch (IOException e) {
		}
		return new HashMap<String, List<String>>();
	}

	/**
	 * doAjax() is where the synchronous call to AJAX is to happen. or at least
	 * where we wait for the asynchronous call to return. This method should fill
	 * the dataIn field with either a string or byte array, or null if you want to
	 * throw an error.
	 * 
	 * url, bytesOut, and postOut are all available for use
	 * 
	 * the method is "private", but in JavaScript that can still be overloaded. Just
	 * set something to org.jmol.awtjs.JmolURLConnection.prototype.doAjax
	 * 
	 * @param isBinary
	 * 
	 * @return file data as a javajs.util.SB or byte[] depending upon the file type.
	 */
	@SuppressWarnings("null")
	private Object doAjax(boolean isBinary, Function<Object, Void> whenDone) {
		getBytesOut();
		J2SObjectInterface J2S = null;
		Object info = ajax;
		this.info = info;
		Map<String, List<String>> map = getRequestProperties();
		boolean isnocache = false;
		String type = null;
		if (map != null) {
			// Unfortunately, AJAX now disallows just about all headers.
			// Even cache-control can be blocked.
			// We could set this up to check if it is cross-domain CORS and
			// then not do this. But for now just not allowing headers.
			for (Entry<String, List<String>> e : map.entrySet()) {
				String key = e.getKey();
				switch (key.toLowerCase()) {
				case "content-type":
					type = e.getValue().get(0);
					break;
				case "cache-control":
					isnocache = e.getValue().get(0).equalsIgnoreCase("no-cache");
					break;
				}
				String s = "";
				List<String> values = e.getValue();
				for (int i = 0; i < values.size(); i++) {
					s += (i == 0 ? "" : ", ") + values.get(i);
				}
				if (s.length() > 0) {
					/**
					 * For now we are not enabling this. Causes too much problem with CORS.
					 */
				}
			}
		}
		if ("application/json".equals(type)) {
		}

		Object result;
		String myURL = url.toString();
		boolean isEmpty = false;
		if (myURL.startsWith("file:/TEMP/")) {
			result = jsutil.getCachedBytes(myURL);
			isEmpty = (result == null);
			if (whenDone != null) {
				whenDone.apply(isEmpty ? null : result);
				return null;
			}
			responseCode = (isEmpty ? HTTP_NOT_FOUND : HTTP_ACCEPTED);
		} else {
			if (myURL.startsWith("file:")) {
				String j2s = /** @j2sNative Clazz._Loader.getJ2SLibBase() || */
						null;
				if (myURL.startsWith("file:/./")) {
					// file:/./xxxx
					myURL = j2s + myURL.substring(7);
				} else if (myURL.startsWith("file:/" + j2s)) {
					// from classLoader
					myURL = myURL.substring(6);
				} else {
					String base = getFileDocumentDir();
					if (base != null && myURL.indexOf(base) == 0) {
						myURL = myURL.substring(base.length());
					} else {
						URL path = jsutil.getCodeBase();
						if (path != null) {
							j2s = path.toString();
							if (myURL.indexOf(j2s) >= 0) {
								myURL = path + myURL.split(j2s)[1];
							} else {
								myURL = path + myURL.substring(5);
							}
						}
					}
				}
			}
			result = J2S.doAjax(myURL, postOut, bytesOut, info);
			if (whenDone != null) {
				return null;
			}
			// the problem is that jsmol.php is still returning crlf even if output is 0
			// bytes
			// and it is not passing through the not-found state, just 200

			responseCode = isEmpty ? HTTP_NOT_FOUND : 0;
		}
		return result;
	}

	private String getFileDocumentDir() {
		String base = jsutil.getDocumentBase().getPath();
		int pt = base.lastIndexOf("/");
		return "file:" + base.substring(0, pt + 1);
	}

	@Override
	public void connect() throws IOException {
		// not expected to be used.
	}

	public void outputBytes(byte[] bytes) {
		// type = "application/octet-stream;";
		bytesOut = bytes;
	}

	private Object formData;

	public void setFormData(Map<String, Object> map) {
		formData = map;
	}

	/**
	 * @j2sAlias addFormData
	 * 
	 * @param name
	 * @param value
	 * @param contentType
	 * @param fileName
	 */
	public void addFormData(String name, Object value, String contentType, String fileName) {
		if (formData == null) {
			formData = new Object[0][];
		}
	}

	/**
	 * a map of key/value pairs where values are either String or byte[].
	 * 
	 */
	@SuppressWarnings("unused")
	private byte[] getBytesOut() {
		if (streamOut != null) {
			if (formData == null) {
				bytesOut = streamOut.toByteArray();
			}
			streamOut = null;
		}

// JavaScript (use ptsv2.com to get a valid toilet)
//
//		fd = new FormData();
//		fd.append("testing", "here");
//		fd.append("andbytes", new Blob([new Int8Array([65,66,67])]));
//
//		                  $.ajax({
//		                      url: 'https://ptsv2.com/t/j1gqe-1592433958/post',
//		                      data: fd,
//		                      processData: false,
//		                      contentType: false,
//		                      type: 'POST',
//		                      success: function(data){
//		                        console.log('upload success!');
//		                      }
//		                    }); 
//

		if (formData != null) {
			Object map = ajax = null;
			if (formData instanceof Map<?, ?>) {
				@SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) formData;
				for (Entry<String, Object> e : data.entrySet()) {
					String key = e.getKey();
					Object val = e.getValue();
					if (val instanceof byte[]) {
						val = toBlob((byte[]) val, null);
					}
				}
			} else {
				Object[][] adata = (Object[][]) formData;
				for (int i = 0; i < adata.length; i++) {
					Object[] d = adata[i];
					String name = (String) d[0];
					Object value = d[1];
					String contentType = (String) d[2];
					String filename = (String) d[3];
					if (value instanceof String && (contentType != null || filename != null)) {
						value = ((String) value).getBytes();
					}
					if (value instanceof byte[]) {
						value = toBlob((byte[]) value, contentType);
					}
				}
			}
			formData = null;
			bytesOut = null;
			useCaches = false;
		}
		return bytesOut;
	}

	private static Object toBlob(byte[] val, String contentType) {
		return null;
	}

	public void outputString(String post) {
		postOut = post;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return streamOut = new ByteArrayOutputStream();
	}

	@SuppressWarnings({ "null", "unused" })
  @Override
	public InputStream getInputStream() throws FileNotFoundException {
	  InputStream is = getInputStreamAndResponse(false);
		if (is == null) {
			throw new FileNotFoundException("opening " + url);
		}
		return is;
	}

	// dont @Override
	public void getBytesAsync(Function<byte[], Void> whenDone) {
		getInputStreamAsync(new Function<InputStream, Void>() {
			@Override
			public Void apply(InputStream is) {
				try {
					if (is != null) {
						byte[] bytes = null;
						whenDone.apply(bytes);
						return null;
					}
				} catch (Exception e) {
				}
				whenDone.apply(null);
				return null;
			}
		});
	}

	@SuppressWarnings({ "null", "unused" })
  private void getInputStreamAsync(Function<InputStream, Void> whenDone) {
		getInputStreamAndResponseAsync(whenDone);
	}

	private void getInputStreamAndResponseAsync(Function<InputStream, Void> whenDone) {
		BufferedInputStream is = getAttachedStreamData(url, false);
		if (is != null || doCache() && (is = getCachedStream(false)) != null) {
			whenDone.apply(is);
			return;
		}
		doAjax(true, new Function<Object, Void>() {

			@Override
			public Void apply(Object data) {
				if (data instanceof String) {
					whenDone.apply(null);
					return null;
				}
				BufferedInputStream is = attachStreamData(url, data);
				if (doCache() && is != null) {
					isNetworkError(is);
					setCachedStream();
				} else if (isNetworkError(is)) {
					is = null;
				}
				whenDone.apply(is);
				return null;
			}

		});
	}

	private InputStream getInputStreamAndResponse(boolean allowNWError) {
		BufferedInputStream is = getAttachedStreamData(url, false);
		if (is != null || doCache() && (is = getCachedStream(allowNWError)) != null) {
			return is;
		}
		is = attachStreamData(url, doAjax(ajax == null, null));
		if (doCache() && is != null) {
			isNetworkError(is);
			setCachedStream();
			return is;
		}
		if (!isNetworkError(is)) {
		}
		return is;
	}

	/**
	 * We have to consider that POST is not
	 */
	boolean doCache() {
		if (!useCaches || !getRequestMethod().equals("POST")) {
			return useCaches;
		}
		String cc = getRequestProperty("Cache-Control");
		return cc == null || !cc.equals("no-cache");
	}

	static Map<String, Object> urlCache = new Hashtable<String, Object>();

	private BufferedInputStream getCachedStream(boolean allowNWError) {
		Object data = urlCache.get(getCacheKey());
		if (data == null) {
			return null;
		}
		@SuppressWarnings("unused")
		URL url = this.url;
		boolean isAjax = false;
		BufferedInputStream bis = getBIS(data, isAjax);
		return (!isNetworkError(bis) || allowNWError ? bis : null);
	}

	private static BufferedInputStream getBIS(Object data, boolean isJSON) {
		if (data == null) {
			return null;
		}
		return Rdr.toBIS(data);
	}

	@SuppressWarnings({ "unused", "null" })
	void setCachedStream() {
	}

	private String getCacheKey() {
		String key = url.toString();
		if (getRequestMethod().equals("POST")) {
			key += (postOut != null ? postOut.hashCode() : 0) | (getBytesOut() != null ? getBytesOut().hashCode() : 0);
		}
		return key;
	}

	@SuppressWarnings("unused")
	boolean isNetworkError(BufferedInputStream is) {
		if (is != null) {
			responseCode = HTTP_OK;
			is.mark(15);
			byte[] bytes = new byte[13];
			try {
				is.read(bytes);
				is.reset();
				for (int i = NETWORK_ERROR.length; --i >= 0;) {
					if (bytes[i] != NETWORK_ERROR[i]) {
						return false;
					}
				}
			} catch (IOException e) {
			}
		}
		responseCode = HTTP_NOT_FOUND;
		return true;
	}

	final private static int[] NETWORK_ERROR = new int[] { 78, 101, 116, 119, 111, 114, 107, 69, 114, 114, 111, 114 };

	/**
	 * J2S will attach the data (String, SB, or byte[]) to any URL that is retrieved
	 * using a ClassLoader. This improves performance by not going back to the
	 * server every time a second time, since the first time in Java is usually just
	 * to see if it exists.
	 * 
	 * @param url
	 * @return String, SB, or byte[], or JSON data
	 */
	@SuppressWarnings("unused")
	public static BufferedInputStream getAttachedStreamData(URL url, boolean andDelete) {
		Object data = null;
		boolean isJSON = false;
		return getBIS(data, isJSON);
	}

	/**
	 * @param url
	 * @param o
	 * @return InputStream or possibly a wrapper for an empty string, but also with
	 *         JSON data.
	 */
	public static BufferedInputStream attachStreamData(URL url, Object o) {
		return getBIS(o, false);
	}

	/**
	 * @return javajs.util.SB or byte[], depending upon the file type
	 */
	public Object getContents() {
		return doAjax(false, null);
	}

	@Override
	public int getResponseCode() throws IOException {
		/*
		 * Check to see if have the response code already
		 */
		if (responseCode == -1) {
			/*
			 * Ensure that we have connected to the server. Record exception as we need to
			 * re-throw it if there isn't a status line.
			 */
			try {
				getInputStreamAndResponse(true);
			} catch (Exception e) {
			}
		}
		return responseCode;
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean usingProxy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getContentLength() {
		try {
			InputStream is = getInputStream();
			return is.available();
		} catch (IOException e) {
			return -1;
		}
	}

	@Override
	public String toString() {
		return (url == null ? "[AjaxURLConnection]" : url.toString());
	}
}
