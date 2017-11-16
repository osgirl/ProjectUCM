/*     */ package intradoc.apputilities.idccommand;
/*     */ 
/*     */ import intradoc.apps.shared.StandAloneApp;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.CharArrayWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.StringReader;
/*     */ import java.net.InetAddress;
/*     */ import java.net.Socket;
/*     */ import java.util.HashMap;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class IdcExecuteServer
/*     */ {
/*  40 */   int m_port = 4444;
/*  41 */   StandAloneApp m_standAlone = null;
/*  42 */   String m_user = null;
/*  43 */   boolean m_isSerializeResult = true;
/*     */ 
/*  47 */   String m_lastErrorMessage = null;
/*     */ 
/*  50 */   boolean m_isConnected = false;
/*  51 */   Socket m_socket = null;
/*  52 */   OutputStream m_outStream = null;
/*  53 */   InputStream m_inStream = null;
/*     */ 
/*  56 */   String m_connectionMode = "auto";
/*     */ 
/*  59 */   public final String[] SERVER_ONLY_SERVICES = { "START_SEARCH_INDEX", "CANCEL_SEARCH_INDEX", "CONTROL_SEARCH_INDEX", "EXPORT_ARCHIVE", "IMPORT_ARCHIVE", "CANCEL_ARCHIVE" };
/*     */ 
/*  69 */   public final String[] CONNECTION_MODE = { "auto", "server", "standalone" };
/*     */ 
/*     */   public boolean init(String user, String intradocDir)
/*     */   {
/*  83 */     if (user == null)
/*     */     {
/*  85 */       this.m_lastErrorMessage = "!csIDCCommandUserUndefined";
/*  86 */       return false;
/*     */     }
/*  88 */     this.m_user = user;
/*     */ 
/*  91 */     if ((intradocDir != null) && (intradocDir.length() > 0))
/*     */     {
/*  93 */       Properties sysProps = System.getProperties();
/*  94 */       sysProps.put("user.dir", intradocDir);
/*     */     }
/*     */ 
/*  97 */     return true;
/*     */   }
/*     */ 
/*     */   public String getLastErrorMessage()
/*     */   {
/* 102 */     return this.m_lastErrorMessage;
/*     */   }
/*     */ 
/*     */   public void setIsSerializeResult(boolean isSerialize)
/*     */   {
/* 107 */     this.m_isSerializeResult = isSerialize;
/*     */   }
/*     */ 
/*     */   public void setConnectionMode(String mode) throws ServiceException
/*     */   {
/* 112 */     if (mode == null)
/*     */     {
/* 114 */       mode = "auto";
/*     */     }
/*     */ 
/* 117 */     String connectionMode = mode.toLowerCase();
/*     */ 
/* 119 */     boolean isFound = false;
/* 120 */     int num = this.CONNECTION_MODE.length;
/* 121 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 123 */       if (!connectionMode.equals(this.CONNECTION_MODE[i]))
/*     */         continue;
/* 125 */       isFound = true;
/* 126 */       break;
/*     */     }
/*     */ 
/* 129 */     if (isFound)
/*     */     {
/* 131 */       this.m_connectionMode = connectionMode;
/*     */     }
/*     */     else
/*     */     {
/* 135 */       throw new ServiceException(LocaleUtils.encodeMessage("csIDCCommandConnectionModeUndefined", null, mode));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void connectToServer()
/*     */     throws ServiceException
/*     */   {
/* 142 */     closeServerConnection();
/*     */     try
/*     */     {
/* 147 */       this.m_port = SharedObjects.getEnvironmentInt("IntradocServerPort", this.m_port);
/* 148 */       String host = SharedObjects.getEnvironmentValue("IdcCommandServerHost");
/* 149 */       if (host == null)
/*     */       {
/* 151 */         host = SharedObjects.getEnvironmentValue("HttpServerAddress");
/*     */       }
/*     */ 
/* 154 */       if (host == null)
/*     */       {
/* 156 */         this.m_socket = new Socket(InetAddress.getLocalHost(), this.m_port);
/*     */       }
/*     */       else
/*     */       {
/* 160 */         int index = host.indexOf(":");
/* 161 */         if (index > 0)
/*     */         {
/* 163 */           host = host.substring(0, index);
/*     */         }
/* 165 */         this.m_socket = new Socket(host, this.m_port);
/*     */       }
/* 167 */       this.m_outStream = this.m_socket.getOutputStream();
/* 168 */       this.m_inStream = this.m_socket.getInputStream();
/* 169 */       this.m_isConnected = true;
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/* 173 */       throw new ServiceException("!csIDCCommandServerUnavailable", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String executeCommand(String data)
/*     */   {
/* 179 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 182 */       binder.receive(new BufferedReader(new StringReader(data)));
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 186 */       return reportError("!csIDCCommandParseRequestError", binder);
/*     */     }
/*     */ 
/* 189 */     binder.setEncodeFlags(true, true);
/* 190 */     return executeCommand(binder);
/*     */   }
/*     */ 
/*     */   public String executeCommand(DataBinder binder)
/*     */   {
/* 195 */     String service = binder.getLocal("IdcService");
/* 196 */     if ((service == null) || (service.trim().length() == 0))
/*     */     {
/* 198 */       return reportError("!csIDCCommandUndefinedCommand", binder);
/*     */     }
/*     */ 
/* 201 */     boolean isStandAlone = false;
/*     */ 
/* 203 */     if ((!this.m_isConnected) && (!this.m_connectionMode.equals("standalone")))
/*     */     {
/*     */       try
/*     */       {
/* 208 */         connectToServer();
/*     */       }
/*     */       catch (Throwable e)
/*     */       {
/* 212 */         if (this.m_connectionMode.equals("server"))
/*     */         {
/* 214 */           return reportError(e.getMessage(), binder);
/*     */         }
/*     */ 
/* 217 */         isStandAlone = true;
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 222 */       isStandAlone = true;
/*     */     }
/*     */ 
/* 225 */     String result = null;
/* 226 */     String errMsg = null;
/*     */     try
/*     */     {
/* 229 */       if (this.m_isConnected == true)
/*     */       {
/* 231 */         CharArrayWriter caw = new CharArrayWriter();
/*     */ 
/* 237 */         errMsg = "!csIDCCommandSendRequestError";
/* 238 */         caw.write("IdcService=" + service + "&IsJava=1&NoHttpHeaders=1\r\n");
/* 239 */         binder.send(caw);
/* 240 */         String encoding = DataSerializeUtils.determineEncoding(binder, null);
/* 241 */         String b = caw.toString();
/*     */ 
/* 246 */         byte[] body = StringUtils.getBytes(b, encoding);
/*     */ 
/* 248 */         CharArrayWriter hBuf = new CharArrayWriter();
/*     */ 
/* 251 */         hBuf.write("REQUEST_METHOD=POST\n");
/* 252 */         hBuf.write("REMOTE_USER=" + this.m_user + "\n");
/* 253 */         hBuf.write("CONTENT_TYPE=text/html\n");
/* 254 */         hBuf.write("CONTENT_LENGTH=" + body.length + "\n");
/* 255 */         hBuf.write("HTTP_HOST=IDCCOMMANDCLIENT\n");
/* 256 */         hBuf.write("$$$$\n");
/* 257 */         String h = hBuf.toString();
/* 258 */         byte[] header = h.getBytes();
/* 259 */         this.m_outStream.write(header);
/* 260 */         this.m_outStream.write(body);
/* 261 */         this.m_outStream.flush();
/*     */ 
/* 264 */         String mergeInclude = binder.getLocal("MergeInclude");
/* 265 */         if ((mergeInclude != null) && (mergeInclude.length() > 0))
/*     */         {
/* 268 */           ByteArrayOutputStream buf = new ByteArrayOutputStream();
/* 269 */           byte[] tempBuf = new byte[10000];
/* 270 */           int nread = 0;
/* 271 */           while ((nread = this.m_inStream.read(tempBuf)) > 0)
/*     */           {
/* 273 */             buf.write(tempBuf, 0, nread);
/*     */           }
/* 275 */           byte[] mergeb = buf.toByteArray();
/* 276 */           String str1 = StringUtils.getString(mergeb, encoding);
/*     */           return str1;
/*     */         }
/* 280 */         errMsg = "!csIDCCommandParseResponseError";
/* 281 */         DataBinder inBinder = new DataBinder();
/* 282 */         BufferedInputStream bstream = new BufferedInputStream(this.m_inStream);
/* 283 */         BufferedReader reader = FileUtils.openDataReader(bstream, encoding);
/*     */ 
/* 285 */         inBinder.receive(reader);
/* 286 */         binder.merge(inBinder);
/*     */       }
/*     */       else
/*     */       {
/* 290 */         executeCommandViaStandAlone(binder);
/*     */       }
/*     */ 
/* 293 */       parseResult(binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 297 */       buildResult(e, null, binder);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 301 */       buildResult(e, errMsg, binder);
/*     */     }
/*     */     finally
/*     */     {
/* 305 */       if (this.m_isSerializeResult)
/*     */       {
/* 307 */         result = sendResult(binder);
/*     */       }
/*     */ 
/* 310 */       closeServerConnection();
/*     */ 
/* 312 */       binder.putLocal("IsStandAlone", (isStandAlone) ? "1" : "0");
/*     */     }
/*     */ 
/* 315 */     return result;
/*     */   }
/*     */ 
/*     */   public void closeServerConnection()
/*     */   {
/* 320 */     closeStreams(this.m_socket, this.m_outStream, this.m_inStream);
/* 321 */     this.m_socket = null;
/* 322 */     this.m_outStream = null;
/* 323 */     this.m_inStream = null;
/* 324 */     this.m_isConnected = false;
/*     */   }
/*     */ 
/*     */   protected void executeCommandViaStandAlone(DataBinder binder) throws ServiceException
/*     */   {
/* 329 */     if (this.m_standAlone == null)
/*     */     {
/*     */       try
/*     */       {
/* 336 */         IdcSystemLoader.finishInit(false);
/*     */ 
/* 339 */         SharedObjects.putEnvironmentValue("DisableSubjectMonitoringThread", "1");
/* 340 */         this.m_standAlone = new StandAloneApp();
/* 341 */         this.m_standAlone.finishLoad(false);
/*     */ 
/* 344 */         this.m_standAlone.doStandaloneAppInit("weblayout");
/* 345 */         this.m_standAlone.doStandaloneAppInit("archiver");
/*     */ 
/* 348 */         this.m_standAlone.setUser(this.m_user);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 352 */         this.m_standAlone = null;
/* 353 */         throw new ServiceException("!csIDCCommandStandaloneInitError", e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 358 */     String cmd = binder.getLocal("IdcService");
/*     */ 
/* 361 */     if (!isCommandForServerOnly(cmd))
/*     */     {
/* 363 */       this.m_standAlone.executeService(cmd, binder);
/*     */     }
/*     */     else
/*     */     {
/* 367 */       reportError(LocaleUtils.encodeMessage("csIDCCommandServerOnlyCommand", null, cmd), binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean isCommandForServerOnly(String cmd)
/*     */   {
/* 376 */     for (int i = 0; i < this.SERVER_ONLY_SERVICES.length; ++i)
/*     */     {
/* 378 */       if (cmd.equals(this.SERVER_ONLY_SERVICES[i]))
/*     */       {
/* 380 */         return true;
/*     */       }
/*     */     }
/* 383 */     return false;
/*     */   }
/*     */ 
/*     */   protected String reportError(String errMsg, DataBinder binder)
/*     */   {
/* 388 */     buildResult("-1", errMsg, errMsg, binder);
/* 389 */     if (this.m_isSerializeResult)
/*     */     {
/* 391 */       return sendResult(binder);
/*     */     }
/* 393 */     return null;
/*     */   }
/*     */ 
/*     */   protected void closeStreams(Socket sock, OutputStream os, InputStream is)
/*     */   {
/*     */     try
/*     */     {
/* 400 */       if (sock != null)
/*     */       {
/* 402 */         sock.close();
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 407 */       if (SystemUtils.m_verbose)
/*     */       {
/* 409 */         Report.debug("system", null, ignore);
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 415 */       if (os != null)
/*     */       {
/* 417 */         os.close();
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 422 */       if (SystemUtils.m_verbose)
/*     */       {
/* 424 */         Report.debug("system", null, ignore);
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 430 */       if (is != null)
/*     */       {
/* 432 */         is.close();
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 437 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 439 */       Report.debug("system", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void parseResult(DataBinder binder)
/*     */   {
/* 446 */     String statusCode = binder.getLocal("StatusCode");
/* 447 */     String statusMessageKey = binder.getLocal("StatusMessageKey");
/* 448 */     String statusMessage = binder.getLocal("StatusMessage");
/*     */ 
/* 450 */     int code = 0;
/*     */     try
/*     */     {
/* 453 */       if (statusCode != null)
/*     */       {
/* 455 */         code = Integer.parseInt(statusCode);
/*     */       }
/*     */     }
/*     */     catch (NumberFormatException e)
/*     */     {
/* 460 */       Report.trace("systemparse", LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csIDCCommandStatusCodeIncorrect", null, statusCode), null), e);
/*     */     }
/*     */ 
/* 464 */     if (code >= 0)
/*     */     {
/* 466 */       if (statusMessage == null)
/*     */       {
/* 468 */         statusMessage = "!syOK";
/* 469 */         statusMessageKey = "!syOK";
/*     */       }
/*     */ 
/*     */     }
/* 474 */     else if (statusMessage == null)
/*     */     {
/* 476 */       statusMessage = "!syUnknownError";
/* 477 */       statusMessageKey = "!syUnknownError";
/*     */     }
/*     */ 
/* 480 */     buildResult(String.valueOf(code), statusMessageKey, statusMessage, binder);
/*     */   }
/*     */ 
/*     */   protected String sendResult(DataBinder binder)
/*     */   {
/*     */     try
/*     */     {
/* 487 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/* 488 */       binder.send(sw);
/*     */ 
/* 490 */       return sw.toStringRelease();
/*     */     }
/*     */     catch (IOException e) {
/*     */     }
/* 494 */     return LocaleResources.getString("csIDCCommandSendResultError", null);
/*     */   }
/*     */ 
/*     */   protected void buildResult(String code, String msgKey, String msg, DataBinder binder)
/*     */   {
/* 500 */     binder.putLocal("StatusCode", code);
/* 501 */     binder.putLocal("StatusMessageKey", msgKey);
/* 502 */     binder.putLocal("StatusMessage", msg);
/*     */   }
/*     */ 
/*     */   protected void buildResult(String code, String msg, DataBinder binder)
/*     */   {
/* 507 */     binder.putLocal("StatusCode", code);
/* 508 */     binder.putLocal("StatusMessage", msg);
/*     */   }
/*     */ 
/*     */   protected void buildResult(Exception e, String msg, DataBinder binder)
/*     */   {
/* 513 */     if (msg == null)
/*     */     {
/* 515 */       msg = "";
/*     */     }
/*     */ 
/* 518 */     if (e != null)
/*     */     {
/* 520 */       IdcMessage errMsg = IdcMessageFactory.lc(e);
/* 521 */       errMsg = IdcMessageFactory.lc(errMsg, "!syGeneralError", new Object[0]);
/* 522 */       msg = LocaleUtils.appendMessage(LocaleUtils.encodeMessage(errMsg), msg);
/*     */     }
/*     */ 
/* 526 */     buildResult("-1", msg, msg, binder);
/*     */   }
/*     */ 
/*     */   public String computeNativeFilePath(String data)
/*     */   {
/* 532 */     return computePath(data, "NativeFilePath", true);
/*     */   }
/*     */ 
/*     */   public String computeWebFilePath(String data)
/*     */   {
/* 537 */     return computePath(data, "WebFilePath", true);
/*     */   }
/*     */ 
/*     */   public String computeURL(String data, boolean isAbsolute)
/*     */   {
/* 542 */     return computePath(data, "URL", isAbsolute);
/*     */   }
/*     */ 
/*     */   protected String computePath(String data, String type, boolean isAbsolute)
/*     */   {
/* 547 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 550 */       binder.receive(new BufferedReader(new StringReader(data)));
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 554 */       return reportError("!csIDCCommandParseRequestError", binder);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 559 */       ExecutionContext context = new ExecutionContextAdaptor();
/* 560 */       FileStoreProvider fileStore = FileStoreProviderLoader.initFileStore(context);
/*     */ 
/* 562 */       if ((type.equals("URL")) || (type.equals("WebFilePath")))
/*     */       {
/* 564 */         binder.putLocal("RenditionId", "webViewableFile");
/*     */       }
/* 566 */       else if (type.equals("VaultFilePath"))
/*     */       {
/* 568 */         binder.putLocal("RenditionId", "primaryFile");
/*     */       }
/*     */       else
/*     */       {
/* 572 */         binder.putLocal("RenditionId", type);
/*     */       }
/*     */ 
/* 575 */       IdcFileDescriptor descriptor = fileStore.createDescriptor(binder, null, context);
/* 576 */       String path = null;
/* 577 */       if (type.equals("URL"))
/*     */       {
/* 579 */         HashMap args = new HashMap();
/* 580 */         args.put("useAbsolute", (isAbsolute) ? "1" : "0");
/* 581 */         path = fileStore.getClientURL(descriptor, null, args, context);
/*     */       }
/*     */       else
/*     */       {
/* 585 */         path = fileStore.getFilesystemPath(descriptor, context);
/*     */       }
/* 587 */       if (path != null)
/*     */       {
/* 589 */         binder.putLocal(type, path);
/*     */       }
/*     */       else
/*     */       {
/* 593 */         binder.putLocal("Error", LocaleResources.getString("csIDCCommandPathError", null));
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 598 */       buildResult(e, "!csIDCCommandPathError", binder);
/*     */     }
/* 600 */     return sendResult(binder);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 605 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98072 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idccommand.IdcExecuteServer
 * JD-Core Version:    0.5.4
 */