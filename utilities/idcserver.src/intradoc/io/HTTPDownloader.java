/*     */ package intradoc.io;
/*     */ 
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.Proxy;
/*     */ import java.net.URL;
/*     */ import java.net.URLConnection;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class HTTPDownloader
/*     */   implements StreamPositionListener
/*     */ {
/*     */   protected Proxy m_proxy;
/*     */   public int m_connectTimeout;
/*     */   public int m_readTimeout;
/*     */   public byte[] m_buffer;
/*     */ 
/*     */   public HTTPDownloader(Proxy proxy)
/*     */   {
/*  93 */     this.m_proxy = proxy;
/*  94 */     this.m_connectTimeout = 10000;
/*  95 */     this.m_readTimeout = 0;
/*  96 */     this.m_buffer = new byte[1048576];
/*     */   }
/*     */ 
/*     */   protected State createRequest(String URLString, Map<String, String> headers)
/*     */     throws IOException
/*     */   {
/* 109 */     State state = new State();
/* 110 */     URL URL = state.m_URL = new URL(URLString);
/* 111 */     URLConnection connection = (this.m_proxy == null) ? URL.openConnection() : URL.openConnection(this.m_proxy);
/* 112 */     if (!connection instanceof HttpURLConnection)
/*     */     {
/* 114 */       throw new IOException("not an HTTP connection: " + URLString);
/*     */     }
/* 116 */     HttpURLConnection conn = state.m_connection = (HttpURLConnection)connection;
/* 117 */     if (headers != null)
/*     */     {
/* 119 */       for (String key : headers.keySet())
/*     */       {
/* 121 */         String value = (String)headers.get(key);
/* 122 */         conn.addRequestProperty(key, value);
/*     */       }
/*     */     }
/* 125 */     conn.setAllowUserInteraction(false);
/* 126 */     conn.setConnectTimeout(this.m_connectTimeout);
/* 127 */     conn.setReadTimeout(this.m_readTimeout);
/* 128 */     conn.setUseCaches(true);
/* 129 */     return state;
/*     */   }
/*     */ 
/*     */   protected void checkResponse(State state)
/*     */     throws IOException
/*     */   {
/* 140 */     HttpURLConnection conn = state.m_connection;
/* 141 */     int response = conn.getResponseCode();
/* 142 */     if ((response >= 200) && (response <= 299))
/*     */       return;
/* 144 */     StringBuilder str = new StringBuilder();
/* 145 */     str.append(response);
/* 146 */     str.append(' ');
/* 147 */     str.append(conn.getResponseMessage());
/* 148 */     str.append(": ");
/* 149 */     str.append(state.m_connection.getRequestMethod());
/* 150 */     str.append(' ');
/* 151 */     str.append(state.m_URL);
/* 152 */     throw new IOException(str.toString());
/*     */   }
/*     */ 
/*     */   public String fetchAsText(State state, StateListener listener)
/*     */     throws IOException
/*     */   {
/* 166 */     state.m_listener = listener;
/* 167 */     int contentLength = (int)state.m_contentLength;
/* 168 */     ByteArrayOutputStream baos = new ByteArrayOutputStream((contentLength > 1024) ? contentLength : 1024);
/* 169 */     InputStream is = state.m_connection.getInputStream();
/*     */     try
/*     */     {
/* 172 */       IdcByteHandlerUtils.copyStreamWithListenerAndBuffer(is, baos, contentLength, this, state, this.m_buffer);
/*     */     }
/*     */     finally
/*     */     {
/* 176 */       baos.close();
/* 177 */       is.close();
/*     */     }
/* 179 */     String[] params = state.m_contentType.split(";");
/* 180 */     String charsetName = null;
/* 181 */     for (int p = params.length - 1; p > 0; --p)
/*     */     {
/* 183 */       String param = params[p];
/* 184 */       int equalsIndex = param.indexOf(61);
/* 185 */       if (equalsIndex < 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 189 */       String name = param.substring(0, equalsIndex);
/* 190 */       if (!name.equals("charset")) {
/*     */         continue;
/*     */       }
/*     */ 
/* 194 */       charsetName = param.substring(equalsIndex + 1);
/* 195 */       break;
/*     */     }
/* 197 */     return (charsetName != null) ? baos.toString(charsetName) : baos.toString();
/*     */   }
/*     */ 
/*     */   protected void processHeader(State state)
/*     */   {
/* 207 */     HttpURLConnection conn = state.m_connection;
/* 208 */     state.m_contentEncoding = conn.getContentEncoding();
/* 209 */     state.m_contentType = conn.getContentType();
/* 210 */     state.m_contentLength = conn.getContentLength();
/* 211 */     state.m_date = conn.getDate();
/* 212 */     state.m_lastModified = conn.getLastModified();
/*     */   }
/*     */ 
/*     */   public State requestHEAD(String URLString)
/*     */     throws IOException
/*     */   {
/* 224 */     return requestHEADWithHeaders(URLString, null);
/*     */   }
/*     */ 
/*     */   public State requestHEADWithHeaders(String URLString, Map<String, String> headers)
/*     */     throws IOException
/*     */   {
/* 238 */     State state = createRequest(URLString, headers);
/* 239 */     HttpURLConnection conn = state.m_connection;
/* 240 */     conn.setRequestMethod("HEAD");
/* 241 */     conn.connect();
/* 242 */     checkResponse(state);
/* 243 */     processHeader(state);
/* 244 */     conn.disconnect();
/* 245 */     state.m_connection = null;
/* 246 */     return state;
/*     */   }
/*     */ 
/*     */   public State startRequestGET(String URLString)
/*     */     throws IOException
/*     */   {
/* 259 */     return startRequestGETWithHeaders(URLString, null);
/*     */   }
/*     */ 
/*     */   public State startRequestGETWithHeaders(String URLString, Map<String, String> headers)
/*     */     throws IOException
/*     */   {
/* 273 */     State state = createRequest(URLString, headers);
/* 274 */     HttpURLConnection conn = state.m_connection;
/* 275 */     conn.setRequestMethod("GET");
/* 276 */     conn.connect();
/* 277 */     checkResponse(state);
/* 278 */     processHeader(state);
/* 279 */     return state;
/*     */   }
/*     */ 
/*     */   public void saveRequest(State state, File target, StateListener listener)
/*     */     throws IOException
/*     */   {
/* 294 */     state.m_listener = listener;
/* 295 */     FileOutputStream fos = new FileOutputStream(target);
/* 296 */     InputStream is = state.m_connection.getInputStream();
/*     */     try
/*     */     {
/* 299 */       IdcByteHandlerUtils.copyStreamWithListenerAndBuffer(is, fos, state.m_contentLength, this, state, this.m_buffer);
/* 300 */       long lastModified = state.m_lastModified;
/* 301 */       if (lastModified == 0L)
/*     */       {
/* 303 */         lastModified = state.m_date;
/*     */       }
/* 305 */       if (lastModified != 0L)
/*     */       {
/* 307 */         target.setLastModified(lastModified);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 312 */       state.m_connection.disconnect();
/* 313 */       state.m_connection = null;
/* 314 */       state.m_listener = null;
/* 315 */       fos.close();
/* 316 */       is.close();
/*     */     }
/*     */   }
/*     */ 
/*     */   public String[] processDirectoryRequest(State state, StateListener listener)
/*     */     throws IOException
/*     */   {
/* 334 */     String html = fetchAsText(state, listener);
/* 335 */     return processDirectoryHTML(html);
/*     */   }
/*     */ 
/*     */   public String[] processDirectoryHTML(String html)
/*     */   {
/* 349 */     List list = getApacheDirectoryListing(html);
/* 350 */     if (list == null)
/*     */     {
/* 352 */       list = getSubversionDirectoryListing(html);
/*     */     }
/*     */ 
/* 355 */     if (list != null)
/*     */     {
/* 357 */       int numItems = list.size();
/* 358 */       String[] listing = new String[numItems];
/* 359 */       list.toArray(listing);
/* 360 */       return listing;
/*     */     }
/* 362 */     return null;
/*     */   }
/*     */ 
/*     */   public List<String> getApacheDirectoryListing(String html)
/*     */   {
/* 373 */     int titleIndex = html.indexOf("<title>Index of ");
/* 374 */     if (titleIndex < 0)
/*     */     {
/* 376 */       return null;
/*     */     }
/* 378 */     int tableIndex = html.indexOf("<table", titleIndex + 16);
/* 379 */     if (tableIndex < 0)
/*     */     {
/* 381 */       return null;
/*     */     }
/* 383 */     int headerIndex = html.indexOf("Name</a></th><th>", tableIndex + 6);
/* 384 */     if (headerIndex < 0)
/*     */     {
/* 386 */       return null;
/*     */     }
/* 388 */     List list = new ArrayList();
/* 389 */     int rowIndex = headerIndex;
/* 390 */     while ((rowIndex = html.indexOf("<tr>", rowIndex)) >= 0)
/*     */     {
/* 392 */       rowIndex += 4;
/* 393 */       int colIndex = html.indexOf("<td><a href=\"", rowIndex);
/* 394 */       if (colIndex < 0)
/*     */       {
/* 396 */         colIndex = html.indexOf("<hr></th></tr>", rowIndex);
/* 397 */         if (colIndex < 0)
/*     */         {
/* 399 */           return null;
/*     */         }
/* 401 */         rowIndex = colIndex + 14;
/*     */       }
/*     */ 
/* 404 */       colIndex += 13;
/* 405 */       int quoteIndex = html.indexOf(34, colIndex);
/* 406 */       if (quoteIndex < 0)
/*     */       {
/* 408 */         return null;
/*     */       }
/* 410 */       rowIndex = quoteIndex + 1;
/* 411 */       String href = html.substring(colIndex, quoteIndex);
/* 412 */       int hrefLength = quoteIndex - colIndex;
/* 413 */       if (href.lastIndexOf(47, hrefLength - 2) >= 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 417 */       list.add(href);
/*     */     }
/* 419 */     return list;
/*     */   }
/*     */ 
/*     */   public List<String> getSubversionDirectoryListing(String html)
/*     */   {
/* 430 */     int svnFooterIndex = html.indexOf("Powered by <a href=\"http://subversion.tigris.org/\">Subversion</a> version");
/*     */ 
/* 432 */     if (svnFooterIndex < 0)
/*     */     {
/* 434 */       return null;
/*     */     }
/* 436 */     int ulIndex = html.indexOf("<ul>");
/* 437 */     if (ulIndex < 0)
/*     */     {
/* 439 */       return null;
/*     */     }
/* 441 */     List list = new ArrayList();
/* 442 */     int liIndex = ulIndex + 4;
/* 443 */     while ((liIndex = html.indexOf("<li><a href=\"", liIndex)) >= 0)
/*     */     {
/* 445 */       liIndex += 13;
/* 446 */       int quoteIndex = html.indexOf(34, liIndex);
/* 447 */       if (quoteIndex < 0)
/*     */       {
/* 449 */         return null;
/*     */       }
/* 451 */       String href = html.substring(liIndex, quoteIndex);
/* 452 */       int hrefLength = quoteIndex - liIndex;
/* 453 */       liIndex = quoteIndex + 1;
/* 454 */       if (href.equals("../")) continue; if (href.lastIndexOf(47, hrefLength - 2) >= 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 458 */       list.add(href);
/*     */     }
/* 460 */     return list;
/*     */   }
/*     */ 
/*     */   public void updatePosition(long position, Object custom)
/*     */   {
/* 472 */     State state = (State)custom;
/* 473 */     state.m_position = position;
/* 474 */     if (state.m_listener == null)
/*     */       return;
/* 476 */     state.m_listener.updateState(state);
/*     */   }
/*     */ 
/*     */   public void finish(long position, Object custom)
/*     */   {
/* 482 */     State state = (State)custom;
/* 483 */     state.m_isFinished = true;
/* 484 */     state.m_position = position;
/* 485 */     if (state.m_listener == null)
/*     */       return;
/* 487 */     state.m_listener.updateState(state);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 494 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98273 $";
/*     */   }
/*     */ 
/*     */   public static class State
/*     */   {
/*     */     public URL m_URL;
/*     */     public HttpURLConnection m_connection;
/*     */     public String m_contentEncoding;
/*     */     public String m_contentType;
/*     */     public long m_contentLength;
/*     */     public long m_date;
/*     */     public long m_lastModified;
/*     */     public long m_position;
/*     */     public boolean m_isFinished;
/*     */     protected HTTPDownloader.StateListener m_listener;
/*     */   }
/*     */ 
/*     */   public static abstract interface StateListener
/*     */   {
/*     */     public abstract void updateState(HTTPDownloader.State paramState);
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.HTTPDownloader
 * JD-Core Version:    0.5.4
 */