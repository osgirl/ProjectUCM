/*     */ package intradoc.client;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.CharArrayWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.Socket;
/*     */ import java.net.URL;
/*     */ import java.net.URLConnection;
/*     */ import java.security.AccessControlException;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UrlClient
/*     */ {
/*     */   public URL m_destURL;
/*  52 */   public String[][] m_sessionIDList = (String[][])null;
/*     */ 
/*  57 */   public boolean m_allowUserInteraction = true;
/*     */ 
/*  62 */   protected String m_appletUser = null;
/*     */ 
/*  67 */   protected String m_appletAuthHash = null;
/*     */ 
/*  72 */   protected boolean m_useSockets = false;
/*     */ 
/*  77 */   protected boolean m_isInit = false;
/*     */ 
/*  82 */   public String m_fileEncoding = null;
/*     */ 
/*     */   public UrlClient(String url)
/*     */     throws IOException
/*     */   {
/*  87 */     this.m_destURL = new URL(url);
/*     */   }
/*     */ 
/*     */   public UrlClient(URL url)
/*     */   {
/*  92 */     this.m_destURL = url;
/*     */   }
/*     */ 
/*     */   public void request(String service, DataBinder data)
/*     */     throws ServiceException
/*     */   {
/* 101 */     if (this.m_useSockets)
/*     */     {
/* 103 */       doSocketRequest(service, data);
/*     */     }
/*     */     else
/*     */     {
/* 107 */       doURLRequest(service, data);
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void setSocketParams(String user, String hash)
/*     */   {
/* 116 */     if (this.m_isInit)
/*     */       return;
/* 118 */     this.m_useSockets = true;
/* 119 */     this.m_appletUser = user;
/* 120 */     this.m_appletAuthHash = hash;
/* 121 */     this.m_isInit = true;
/*     */   }
/*     */ 
/*     */   public void doURLRequest(String service, DataBinder data)
/*     */     throws ServiceException
/*     */   {
/* 130 */     CharArrayWriter dataChars = new CharArrayWriter();
/* 131 */     if ((!data.m_isJava) || (data.m_isCgi == true))
/*     */     {
/* 133 */       throw new ServiceException("!syDataObjectIncorrectFlags");
/*     */     }
/*     */ 
/* 137 */     URLConnection urlConn = null;
/*     */     try
/*     */     {
/* 142 */       if (service != null)
/*     */       {
/* 146 */         data.putLocal("IdcService", service);
/*     */       }
/*     */       else
/*     */       {
/* 150 */         service = data.getAllowMissing("IdcService");
/*     */       }
/* 152 */       data.m_clientEncoding = this.m_fileEncoding;
/* 153 */       data.m_javaEncoding = this.m_fileEncoding;
/* 154 */       data.send(dataChars);
/*     */ 
/* 157 */       urlConn = getConnection(this.m_destURL);
/* 158 */       if (this.m_sessionIDList != null)
/*     */       {
/* 160 */         String cookie = "";
/* 161 */         for (int i = 0; i < this.m_sessionIDList.length; ++i)
/*     */         {
/* 163 */           cookie = cookie + this.m_sessionIDList[i][0] + "=" + this.m_sessionIDList[i][1] + ";";
/*     */         }
/*     */ 
/* 166 */         urlConn.addRequestProperty("Cookie", cookie);
/*     */       }
/*     */       else
/*     */       {
/* 170 */         Report.trace("socketprotocol", "No sessionID provided to set as a cookie", null);
/*     */       }
/*     */ 
/* 173 */       sendRequest(getHeader(service, data), dataChars.toString(), urlConn);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 177 */       String msg = LocaleUtils.encodeMessage("syFailedToSendURLRequest", e.getMessage());
/*     */ 
/* 179 */       throw new ServiceException(-4, msg);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 184 */       int responseCode = 200;
/* 185 */       InputStream in = null;
/* 186 */       if (urlConn instanceof HttpURLConnection)
/*     */       {
/* 188 */         HttpURLConnection httpConnection = (HttpURLConnection)urlConn;
/*     */         try
/*     */         {
/* 191 */           responseCode = httpConnection.getResponseCode();
/*     */         }
/*     */         catch (RuntimeException e)
/*     */         {
/* 195 */           Throwable cause = e.getCause();
/* 196 */           if (cause instanceof AccessControlException)
/*     */           {
/* 199 */             Report.debug("system", "Probable session timeout redirect page issue", e);
/* 200 */             String msg = "!syLoginSessionAccessControlException";
/* 201 */             throw new ServiceException(msg);
/*     */           }
/* 203 */           throw e;
/*     */         }
/* 205 */         if (responseCode >= 400)
/*     */         {
/* 207 */           in = httpConnection.getErrorStream();
/*     */         }
/*     */       }
/* 210 */       if (in == null)
/*     */       {
/* 212 */         in = urlConn.getInputStream();
/*     */       }
/* 214 */       BufferedInputStream bstream = new BufferedInputStream(in);
/* 215 */       BufferedReader reader = null;
/*     */ 
/* 217 */       DataBinder inData = new DataBinder(true);
/*     */ 
/* 220 */       inData.m_javaEncoding = DataSerializeUtils.detectEncoding(data, bstream, null);
/* 221 */       String responseEncoding = inData.m_javaEncoding;
/*     */ 
/* 229 */       if ((responseEncoding != null) && (responseEncoding.length() > 0) && (!responseEncoding.equals("ASCII")) && ((
/* 231 */         (this.m_fileEncoding == null) || (!this.m_fileEncoding.equals("UTF8")))))
/*     */       {
/* 233 */         this.m_fileEncoding = responseEncoding;
/*     */         try
/*     */         {
/* 238 */           String test = "test";
/* 239 */           test.getBytes(this.m_fileEncoding);
/*     */         }
/*     */         catch (UnsupportedEncodingException e)
/*     */         {
/* 243 */           this.m_fileEncoding = "UTF8";
/* 244 */           if (data.getLocal("IsSecondRequest") == null)
/*     */           {
/* 246 */             bstream.close();
/* 247 */             data.putLocal("IsSecondRequest", "1");
/* 248 */             doURLRequest(service, data);
/* 249 */             return;
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 255 */       if ((responseEncoding != null) && (responseEncoding.length() > 0))
/*     */       {
/* 257 */         reader = new BufferedReader(new InputStreamReader(bstream, responseEncoding));
/*     */       }
/*     */       else
/*     */       {
/* 261 */         reader = new BufferedReader(new InputStreamReader(bstream));
/*     */       }
/* 263 */       inData.receive(reader);
/*     */ 
/* 266 */       String errCode = inData.getLocal("StatusCode");
/* 267 */       if (errCode != null)
/*     */       {
/* 269 */         int code = Integer.parseInt(errCode);
/* 270 */         if (code < 0)
/*     */         {
/* 274 */           String statusReason = inData.getLocal("StatusReason");
/* 275 */           if (statusReason != null)
/*     */           {
/* 277 */             data.putLocal("StatusReason", statusReason);
/*     */           }
/*     */ 
/* 280 */           String errMsg = inData.getLocal("StatusMessage");
/* 281 */           if (errMsg == null)
/*     */           {
/* 283 */             throw new ServiceException("!syUnknownErrorFromServer");
/*     */           }
/* 285 */           throw new ServiceException(code, errMsg);
/*     */         }
/*     */       }
/* 288 */       else if (inData.getLocalData().size() == 0)
/*     */       {
/* 290 */         if ((responseCode == 403) || (responseCode == 404))
/*     */         {
/* 292 */           String docName = inData.getLocal("dDocName");
/* 293 */           String errMsgKey = (responseCode == 403) ? "syFileUtilsFileNoAccess" : "syFileUtilsFileNotFound";
/*     */ 
/* 295 */           String msg = LocaleUtils.encodeMessage(errMsgKey, null, docName);
/* 296 */           int err = (responseCode == 403) ? -18 : -16;
/*     */ 
/* 298 */           throw new ServiceException(err, msg);
/*     */         }
/*     */ 
/* 305 */         boolean isIdcLoginForm = false;
/* 306 */         StringBuffer buf = new StringBuffer("HTTP error response code: " + responseCode);
/* 307 */         Vector v = inData.m_unstructuredData;
/* 308 */         if ((v != null) && (v.size() > 0))
/*     */         {
/* 310 */           buf.append("\n---Response Body---");
/* 311 */           int nrows = (v.size() > 200) ? 200 : v.size();
/* 312 */           for (int i = 0; i < nrows; ++i)
/*     */           {
/* 314 */             buf.append('\n');
/* 315 */             String line = v.elementAt(i).toString();
/* 316 */             if (line.indexOf("IdcClientLoginForm=1") >= 0)
/*     */             {
/* 318 */               isIdcLoginForm = true;
/*     */ 
/* 321 */               break;
/*     */             }
/* 323 */             buf.append(v.elementAt(i).toString());
/*     */           }
/*     */         }
/*     */         String msg;
/*     */         String msg;
/* 327 */         if (isIdcLoginForm)
/*     */         {
/* 329 */           msg = "!syLoginSessionInvalid";
/*     */         }
/*     */         else
/*     */         {
/* 333 */           msg = LocaleUtils.encodeMessage("syFailedToReceiveResponseWithProtocolData", buf.toString());
/*     */         }
/*     */ 
/* 337 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/* 341 */       mergeResponseData(data, inData);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 345 */       String msg = LocaleUtils.encodeMessage("syFailedToReceiveResponseFromServer", e.getMessage());
/*     */ 
/* 347 */       throw new ServiceException(-5, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public BufferedReader postRequest(String service, String request) throws IOException
/*     */   {
/* 353 */     URLConnection urlConn = getConnection(this.m_destURL);
/*     */ 
/* 355 */     sendRequest(getHeader(service, new DataBinder()), request, urlConn);
/* 356 */     Reader reader = new InputStreamReader(urlConn.getInputStream());
/*     */ 
/* 358 */     return new BufferedReader(reader);
/*     */   }
/*     */ 
/*     */   protected void sendRequest(String header, String request, URLConnection urlConn)
/*     */     throws IOException
/*     */   {
/* 364 */     byte[] headerBuf = null;
/* 365 */     byte[] requestBuf = null;
/*     */ 
/* 367 */     if ((this.m_fileEncoding != null) && (this.m_fileEncoding.length() > 0))
/*     */     {
/* 369 */       headerBuf = header.getBytes(this.m_fileEncoding);
/* 370 */       requestBuf = request.getBytes(this.m_fileEncoding);
/*     */     }
/*     */     else
/*     */     {
/* 374 */       headerBuf = header.getBytes();
/* 375 */       requestBuf = request.getBytes();
/*     */     }
/* 377 */     int totLen = headerBuf.length + requestBuf.length;
/*     */ 
/* 379 */     Report.trace("socketprotocol", "Sending " + totLen + " bytes to " + urlConn.getURL().toString() + " using URLConnection wrapper", null);
/*     */ 
/* 381 */     OutputStream output = getOutputStream(urlConn, totLen);
/*     */     try
/*     */     {
/* 384 */       output.write(headerBuf);
/* 385 */       output.write(requestBuf);
/*     */     }
/*     */     finally
/*     */     {
/* 389 */       FileUtils.closeObject(output);
/*     */     }
/*     */   }
/*     */ 
/*     */   public URLConnection getConnection(URL url) throws IOException
/*     */   {
/* 395 */     URLConnection urlConn = url.openConnection();
/* 396 */     urlConn.setDoOutput(true);
/* 397 */     urlConn.setDoInput(true);
/* 398 */     urlConn.setUseCaches(false);
/* 399 */     if (this.m_allowUserInteraction)
/*     */     {
/* 401 */       Report.trace("socketprotocol", "Setting allow user interaction for request", null);
/* 402 */       urlConn.setAllowUserInteraction(this.m_allowUserInteraction);
/*     */     }
/*     */ 
/* 406 */     urlConn.setRequestProperty("Content-type", "text/html");
/*     */ 
/* 408 */     return urlConn;
/*     */   }
/*     */ 
/*     */   public OutputStream getOutputStream(URLConnection urlConn, int contentLen) throws IOException
/*     */   {
/* 413 */     urlConn.setRequestProperty("Content-length", Integer.toString(contentLen));
/*     */ 
/* 415 */     OutputStream outStream = urlConn.getOutputStream();
/*     */ 
/* 417 */     return outStream;
/*     */   }
/*     */ 
/*     */   public void doSocketRequest(String service, DataBinder data)
/*     */     throws ServiceException
/*     */   {
/* 434 */     if (service != null)
/*     */     {
/* 436 */       data.putLocal("IdcService", service);
/*     */     }
/*     */     else
/*     */     {
/* 440 */       service = data.getAllowMissing("IdcService");
/*     */     }
/*     */ 
/* 444 */     data.putLocal("AppletAuth", this.m_appletAuthHash);
/* 445 */     data.putLocal("AppletUser", this.m_appletUser);
/*     */ 
/* 447 */     CharArrayWriter dataChars = new CharArrayWriter();
/* 448 */     Socket sock = null;
/* 449 */     OutputStream out = null;
/* 450 */     InputStream in = null;
/*     */     try
/*     */     {
/* 454 */       dataChars.write("IdcService=" + service + "&IsJava=1\r\n");
/* 455 */       if ((!data.m_isJava) || (data.m_isCgi == true))
/*     */       {
/* 457 */         throw new ServiceException("!syDataObjectIncorrectFlags");
/*     */       }
/* 459 */       data.send(dataChars);
/*     */ 
/* 461 */       byte[] bytes = dataChars.toString().getBytes();
/* 462 */       Report.trace("socketprotocol", "Sending " + bytes.length + " bytes to " + this.m_destURL.toString() + " using direct socket protocol", null);
/* 463 */       sock = getSocketConnection(this.m_destURL, bytes.length);
/* 464 */       out = sock.getOutputStream();
/* 465 */       out.write(bytes);
/* 466 */       out.flush();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 470 */       throw new ServiceException(-4, "!syFailedToSendURLRequest", e);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 476 */       in = sock.getInputStream();
/* 477 */       DataBinder inData = new DataBinder(true);
/* 478 */       inData.receive(new BufferedReader(new InputStreamReader(in)));
/*     */ 
/* 481 */       String errCode = inData.getLocal("StatusCode");
/* 482 */       if (errCode != null)
/*     */       {
/* 484 */         int code = Integer.parseInt(errCode);
/* 485 */         if (code < 0)
/*     */         {
/* 487 */           String errMsg = inData.getLocal("StatusMessage");
/* 488 */           if (errMsg == null)
/*     */           {
/* 490 */             throw new ServiceException("!syUnknownErrorFromServer");
/*     */           }
/* 492 */           IdcMessage msg = IdcMessageFactory.lc();
/* 493 */           msg.m_msgLocalized = errMsg;
/* 494 */           throw new ServiceException(null, code, msg);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 499 */       mergeResponseData(data, inData);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */       String msg;
/* 505 */       throw new ServiceException(-5, msg);
/*     */     }
/*     */     finally
/*     */     {
/* 509 */       closeStreams(in, out, sock);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void mergeResponseData(DataBinder curData, DataBinder inData)
/*     */     throws ServiceException
/*     */   {
/* 518 */     Vector v = inData.m_unstructuredData;
/* 519 */     if ((v != null) && (v.size() > 0))
/*     */     {
/* 521 */       StringBuffer buf = new StringBuffer();
/* 522 */       buf.append("--Unstructured response data\n");
/* 523 */       for (int i = 0; i < v.size(); ++i)
/*     */       {
/* 525 */         Object o = v.elementAt(i);
/* 526 */         if (o == null)
/*     */           continue;
/* 528 */         buf.append(o.toString());
/* 529 */         buf.append("\n");
/*     */       }
/*     */ 
/* 532 */       Report.trace("socketprotocol", buf.toString(), null);
/*     */     }
/*     */ 
/* 535 */     if (inData.getLocalData().size() == 0)
/*     */     {
/* 537 */       throw new ServiceException("!syFailedToReceiveResponseWithProtocolData");
/*     */     }
/* 539 */     curData.merge(inData);
/*     */   }
/*     */ 
/*     */   public static Socket getSocketConnection(URL url, int length)
/*     */     throws IOException
/*     */   {
/* 548 */     int port = url.getPort();
/* 549 */     if (port < 0)
/*     */     {
/* 551 */       port = 80;
/*     */     }
/* 553 */     Socket sock = new Socket(url.getHost(), port);
/*     */ 
/* 555 */     String header = "POST " + url + " HTTP/1.0\n" + "Content-length: " + String.valueOf(length) + "\n" + "Content-type: text/html\n" + "Host: JAVA_HOST\n" + "User-Agent: Java\n\n";
/*     */ 
/* 562 */     OutputStream out = sock.getOutputStream();
/* 563 */     out.write(header.getBytes());
/* 564 */     out.flush();
/*     */ 
/* 566 */     return sock;
/*     */   }
/*     */ 
/*     */   public static void closeStreams(InputStream in, OutputStream out, Socket sock)
/*     */   {
/*     */     try
/*     */     {
/* 576 */       if (in != null)
/*     */       {
/* 578 */         in.close();
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 583 */       if (SystemUtils.m_verbose)
/*     */       {
/* 585 */         Report.debug("system", null, e);
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 591 */       if (out != null)
/*     */       {
/* 593 */         out.close();
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 598 */       if (SystemUtils.m_verbose)
/*     */       {
/* 600 */         Report.debug("system", null, e);
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 606 */       if (sock != null)
/*     */       {
/* 608 */         sock.close();
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 613 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 615 */       Report.debug("system", null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static String getHeader(String service, DataBinder data)
/*     */     throws IOException
/*     */   {
/* 622 */     String header = "IdcService=" + service + "&IsJava=1\r\n";
/*     */ 
/* 635 */     return header;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 640 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.client.UrlClient
 * JD-Core Version:    0.5.4
 */