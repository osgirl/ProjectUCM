/*      */ package intradoc.provider;
/*      */ 
/*      */ import intradoc.common.DynamicHtmlMerger;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.VersionInfo;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.server.proxy.ProviderUtils;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.ByteArrayOutputStream;
/*      */ import java.io.CharArrayWriter;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.InputStreamReader;
/*      */ import java.io.InterruptedIOException;
/*      */ import java.io.OutputStream;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class StandardServerRequest
/*      */   implements ServerRequest
/*      */ {
/*      */   public static final int UNKNOWN_FORMAT = 0;
/*      */   public static final int STANDARD_FORMAT = 1;
/*      */   public static final int UPLOAD_FORMAT = 2;
/*      */   public static final int DOWNLOAD_FORMAT = 3;
/*      */   public static final int HTTP_UNDETERMINED = 0;
/*      */   public static final int HTTP_GET = 1;
/*      */   public static final int HTTP_POST = 2;
/*      */   public static final int HTTP_MULTIPART_POST = 3;
/*   73 */   public static final String F_IDCFILE_MARKER = new String("IDCFILE");
/*      */   public static final int F_IDCFILE_MARKER_LENGTH = 24;
/*   77 */   public OutgoingConnection m_connection = null;
/*   78 */   public Properties m_requestProps = new Properties();
/*   79 */   public ReportProgress m_progress = null;
/*   80 */   public IdcStringBuilder m_responseHeaders = null;
/*   81 */   public BufferedInputStream m_responseBody = null;
/*   82 */   public BufferedInputStream m_bisDuplicate = null;
/*   83 */   public boolean m_finishedBinder = false;
/*   84 */   public boolean m_doFullHeaders = false;
/*      */ 
/*   87 */   public DataBinder m_activeInBinder = null;
/*   88 */   public DataBinder m_activeOutBinder = null;
/*   89 */   public int m_requestType = 0;
/*      */ 
/*   93 */   public boolean m_isStreamingResponse = false;
/*      */ 
/*      */   public void setOutgoingConnection(OutgoingConnection con)
/*      */   {
/*  102 */     this.m_connection = con;
/*      */   }
/*      */ 
/*      */   public void setRequestProperties(Properties props)
/*      */   {
/*  107 */     this.m_requestProps = props;
/*      */   }
/*      */ 
/*      */   public void setReportProgress(ReportProgress rp)
/*      */   {
/*  112 */     this.m_progress = rp;
/*      */   }
/*      */ 
/*      */   public void doRequest(DataBinder inBinder, DataBinder outBinder, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  118 */     if (ctxt != null)
/*      */     {
/*  121 */       ctxt.setCachedObject("ServiceRequest", this);
/*  122 */       ctxt.setCachedObject("ServiceRequestConnection", this.m_connection);
/*  123 */       ctxt.setCachedObject("Provider", ProviderUtils.getProvider(this.m_connection.getProviderData()));
/*      */     }
/*      */ 
/*  128 */     this.m_activeInBinder = inBinder;
/*  129 */     this.m_activeOutBinder = outBinder;
/*      */ 
/*  132 */     this.m_responseBody = null;
/*      */ 
/*  135 */     this.m_doFullHeaders = true;
/*      */ 
/*  141 */     String noHttpHeaders = null;
/*  142 */     boolean hasFiles = inBinder.m_hasAttachedFiles;
/*      */     try
/*      */     {
/*  145 */       if (hasFiles)
/*      */       {
/*  147 */         doMultiPartPost(inBinder, ctxt);
/*      */       }
/*      */       else
/*      */       {
/*  158 */         String url = null;
/*  159 */         if (outBinder == null)
/*      */         {
/*  161 */           String service = inBinder.getLocal("IdcService");
/*  162 */           if ((service != null) && (service.equals("GET_DYNAMIC_URL")))
/*      */           {
/*  164 */             url = inBinder.getLocal("fileUrl");
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  169 */           noHttpHeaders = inBinder.getLocal("NoHttpHeaders");
/*  170 */           if (noHttpHeaders != null)
/*      */           {
/*  172 */             inBinder.removeLocal("NoHttpHeaders");
/*      */           }
/*      */         }
/*  175 */         if (url != null)
/*      */         {
/*  177 */           doGet(inBinder, url, ctxt);
/*      */         }
/*      */         else
/*      */         {
/*  181 */           doPost(inBinder, ctxt);
/*      */         }
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  187 */       if (noHttpHeaders != null)
/*      */       {
/*  190 */         inBinder.putLocal("NoHttpHeaders", noHttpHeaders);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  197 */     if (outBinder == null)
/*      */       return;
/*  199 */     boolean isDuplicateResponse = DataBinderUtils.getBoolean(inBinder, "isDuplicateResponse", false);
/*      */ 
/*  201 */     receive(outBinder, isDuplicateResponse);
/*  202 */     this.m_finishedBinder = true;
/*  203 */     reportProgress("!csFinishedRequest");
/*      */   }
/*      */ 
/*      */   public void doGet(DataBinder inBinder, String url, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  210 */     this.m_requestType = 1;
/*  211 */     reportProgress("!csSendingRequest");
/*      */ 
/*  214 */     prepareRequestProps(inBinder, ctxt);
/*      */ 
/*  216 */     this.m_requestProps.put("REQUEST_METHOD", "GET");
/*  217 */     this.m_requestProps.put("REQUEST_URL", url);
/*      */ 
/*  222 */     startRequest(inBinder, ctxt);
/*      */   }
/*      */ 
/*      */   public void doPost(DataBinder inBinder, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  228 */     this.m_requestType = 2;
/*  229 */     String service = inBinder.getLocal("IdcService");
/*  230 */     if (service == null)
/*      */     {
/*  232 */       throw new DataException("!csUnableToDoRequest");
/*      */     }
/*      */ 
/*  235 */     String queryString = inBinder.getEnvironmentValue("QUERY_STRING");
/*  236 */     String requestMethod = inBinder.getEnvironmentValue("REQUEST_METHOD");
/*  237 */     if ((queryString != null) && (requestMethod != null) && (requestMethod.equalsIgnoreCase("GET")))
/*      */     {
/*  241 */       inBinder.putLocal("QUERY_STRING", queryString);
/*      */     }
/*      */ 
/*  244 */     reportProgress("!csSendingRequest");
/*      */     try
/*      */     {
/*  247 */       CharArrayWriter caw = new CharArrayWriter();
/*  248 */       caw.write("IdcService=" + service + "&IsJava=1\r\n");
/*  249 */       inBinder.send(caw);
/*      */ 
/*  252 */       String str = caw.toString();
/*  253 */       byte[] bytes = StringUtils.getBytes(str, DataSerializeUtils.determineEncoding(inBinder, ctxt));
/*      */ 
/*  256 */       prepareRequestProps(inBinder, ctxt);
/*      */ 
/*  258 */       this.m_requestProps.put("CONTENT_LENGTH", String.valueOf(bytes.length));
/*  259 */       this.m_requestProps.put("REQUEST_METHOD", "POST");
/*  260 */       this.m_requestProps.put("CONTENT_TYPE", "text/hda");
/*      */ 
/*  263 */       OutputStream os = startRequest(inBinder, ctxt);
/*      */ 
/*  266 */       os.write(bytes);
/*      */ 
/*  268 */       os.flush();
/*      */     }
/*      */     catch (InterruptedIOException e)
/*      */     {
/*  272 */       String msg = LocaleUtils.encodeMessage("csInterruptedSend", null, service);
/*      */ 
/*  274 */       throw new ServiceException(-4, msg, e);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  278 */       String msg = LocaleUtils.encodeMessage("csUnableToSendRequest", null, service);
/*      */ 
/*  280 */       throw new ServiceException(-4, msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void computeMultiPartEncoding(DataBinder inBinder)
/*      */   {
/*  286 */     String encoding = SharedObjects.getEnvironmentValue("MultiPartEncoding");
/*  287 */     if (encoding == null)
/*      */     {
/*  289 */       encoding = inBinder.m_javaEncoding;
/*      */     }
/*  291 */     if (encoding == null)
/*      */     {
/*  293 */       encoding = FileUtils.m_javaSystemEncoding;
/*      */     }
/*  295 */     if (encoding == null)
/*      */     {
/*  297 */       encoding = "UFT8";
/*      */     }
/*      */ 
/*  300 */     String qString = inBinder.getEnvironmentValue("QUERY_STRING");
/*  301 */     if (qString != null)
/*      */     {
/*  303 */       Properties props = new Properties();
/*  304 */       StringUtils.parseProperties(props, qString);
/*  305 */       String cencoding = props.getProperty("ClientEncoding");
/*  306 */       if (cencoding != null)
/*      */       {
/*  309 */         inBinder.m_javaEncoding = cencoding;
/*  310 */         return;
/*      */       }
/*  312 */       qString = "ClientEncoding=" + encoding + "&" + qString;
/*      */     }
/*      */     else
/*      */     {
/*  316 */       qString = "ClientEncoding=" + encoding;
/*      */     }
/*  318 */     inBinder.m_javaEncoding = encoding;
/*  319 */     inBinder.setEnvironmentValue("QUERY_STRING", qString);
/*      */   }
/*      */ 
/*      */   public void doMultiPartPost(DataBinder inBinder, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  325 */     this.m_requestType = 3;
/*  326 */     MultiRequest mr = createMultiRequest(inBinder);
/*      */     try
/*      */     {
/*  329 */       mr.prepareMultiPartPost();
/*  330 */       mr.setReportProgress(this.m_progress);
/*      */ 
/*  332 */       computeMultiPartEncoding(inBinder);
/*      */ 
/*  334 */       prepareRequestProps(inBinder, ctxt);
/*      */ 
/*  337 */       long contentLen = mr.countBytes();
/*  338 */       this.m_requestProps.put("CONTENT_LENGTH", String.valueOf(contentLen));
/*      */ 
/*  340 */       mr.populateEnv(this.m_requestProps);
/*      */ 
/*  342 */       OutputStream os = startRequest(inBinder, ctxt);
/*  343 */       mr.sendMultiPartPost(os);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  347 */       flushConnection(inBinder);
/*  348 */       throw new ServiceException("!csUnableToPerformMultiPartPost", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public MultiRequest createMultiRequest(DataBinder inBinder)
/*      */   {
/*  354 */     return new MultiRequest(inBinder);
/*      */   }
/*      */ 
/*      */   public OutputStream startRequest(DataBinder inBinder, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  360 */     DataBinder providerData = this.m_connection.getProviderData();
/*  361 */     ProxyConnectionUtils.prepareOutgoingAuthKey(this.m_requestProps, inBinder, providerData, ctxt);
/*  362 */     return this.m_connection.startRequest(this.m_requestProps);
/*      */   }
/*      */ 
/*      */   public void receive(DataBinder outBinder, boolean isDuplicateResponse)
/*      */     throws ServiceException, DataException
/*      */   {
/*  368 */     reportProgress("!csReceivingRequest");
/*      */     try
/*      */     {
/*  371 */       InputStream is = this.m_connection.getInputStream();
/*  372 */       readResponse(is, outBinder, isDuplicateResponse);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  376 */       throw new ServiceException(-5, "!csUnableToReceiveRequest", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void readResponse(InputStream inStream, DataBinder outBinder, boolean isDuplicateResponse)
/*      */     throws ServiceException, DataException
/*      */   {
/*      */     try
/*      */     {
/*  386 */       BufferedInputStream bis = new BufferedInputStream(inStream);
/*  387 */       if (isDuplicateResponse)
/*      */       {
/*      */         try
/*      */         {
/*  396 */           Thread.sleep(200L);
/*      */         }
/*      */         catch (InterruptedException e)
/*      */         {
/*  400 */           e.printStackTrace();
/*      */         }
/*  402 */         bis.mark(2147483647);
/*  403 */         ByteArrayOutputStream baos = new ByteArrayOutputStream();
/*  404 */         byte[] bigBuff = new byte[1024];
/*      */         int len;
/*  406 */         while ((len = bis.read(bigBuff)) >= 1024)
/*      */         {
/*  408 */           baos.write(bigBuff);
/*      */         }
/*  410 */         if (len > 0)
/*      */         {
/*  412 */           baos.write(bigBuff, 0, len);
/*      */         }
/*  414 */         bis.reset();
/*  415 */         this.m_bisDuplicate = new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));
/*      */       }
/*      */ 
/*  420 */       int formatType = parseHeaders(outBinder, bis, false);
/*  421 */       switch (formatType)
/*      */       {
/*      */       case 1:
/*  424 */         String encoding = DataSerializeUtils.detectEncoding(outBinder, bis, null);
/*  425 */         outBinder.m_javaEncoding = encoding;
/*  426 */         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(bis, encoding));
/*  427 */         outBinder.receive(bufferedReader);
/*  428 */         break;
/*      */       case 3:
/*  433 */         String fileInfo = outBinder.getLocal(F_IDCFILE_MARKER);
/*      */         int fileSize;
/*      */         int fileSize;
/*  434 */         if ((fileInfo != null) && (fileInfo.length() > 0))
/*      */         {
/*  436 */           String fileSizeStr = fileInfo.substring(11);
/*  437 */           fileSizeStr = fileSizeStr.substring(0, fileSizeStr.indexOf(" "));
/*  438 */           fileSize = Integer.parseInt(fileSizeStr);
/*      */         }
/*      */         else
/*      */         {
/*  448 */           String contentLength = outBinder.getEnvironmentValue("CONTENT_LENGTH");
/*  449 */           fileSize = Integer.parseInt(contentLength);
/*      */         }
/*      */ 
/*  452 */         String dir = DataBinder.getTemporaryDirectory();
/*  453 */         long fCounter = DataBinder.getNextFileCounter();
/*      */ 
/*  457 */         String extension = null;
/*  458 */         String cDisp = outBinder.getLocal("Content-Disposition");
/*  459 */         if (cDisp == null)
/*      */         {
/*  461 */           cDisp = outBinder.getEnvironmentValue("CONTENT_DISPOSITION");
/*      */         }
/*      */ 
/*  468 */         if (cDisp != null)
/*      */         {
/*  470 */           Vector cDistParams = StringUtils.parseArray(cDisp, ';', '^');
/*  471 */           for (int p = 0; p < cDistParams.size(); ++p)
/*      */           {
/*  473 */             String param = (String)cDistParams.elementAt(p);
/*  474 */             int index = param.indexOf("filename*=UTF-8''");
/*  475 */             if (index <= -1)
/*      */               continue;
/*  477 */             param = param.substring(index + 17);
/*  478 */             int dot = param.lastIndexOf(46);
/*  479 */             if (dot <= -1)
/*      */               break;
/*  481 */             extension = param.substring(dot); break;
/*      */           }
/*      */ 
/*  486 */           if (extension == null)
/*      */           {
/*  488 */             int index = cDisp.lastIndexOf(".");
/*  489 */             if (index >= 0)
/*      */             {
/*  491 */               extension = cDisp.substring(index);
/*  492 */               index = extension.lastIndexOf("\"");
/*  493 */               if (index > 0)
/*      */               {
/*  495 */                 extension = extension.substring(0, index);
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*  500 */         if (extension == null)
/*      */         {
/*  502 */           extension = ".tmp";
/*      */         }
/*  504 */         String path = dir + fCounter + extension.trim();
/*  505 */         saveFile(bis, path, fileSize);
/*  506 */         outBinder.putLocal("downloadFile:path", path);
/*      */ 
/*  508 */         break;
/*      */       case 2:
/*  511 */         outBinder.setReportProgress(this.m_progress);
/*  512 */         outBinder.m_inStream = bis;
/*  513 */         DataSerializeUtils.parseRequestBody(outBinder, null);
/*  514 */         break;
/*      */       case 0:
/*  517 */         throw new ServiceException("!csResponseInUnknownFormat");
/*      */       }
/*      */     }
/*      */     catch (InterruptedIOException e)
/*      */     {
/*  522 */       throw new ServiceException("!csInterruptedRead", e);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  526 */       throw new ServiceException("!csUnableToReadResponse", e);
/*      */     }
/*      */     catch (NumberFormatException e)
/*      */     {
/*  530 */       throw new ServiceException("!csResponseInUnexpectedFormat", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getResponseHeaders(ExecutionContext cxt, DataBinder headers)
/*      */     throws DataException, ServiceException
/*      */   {
/*  538 */     reportProgress("!csReceivingRequest");
/*      */     try
/*      */     {
/*  541 */       InputStream is = this.m_connection.getInputStream();
/*  542 */       BufferedInputStream bis = null;
/*  543 */       if ((this.m_finishedBinder) && (this.m_bisDuplicate != null))
/*      */       {
/*  545 */         bis = this.m_bisDuplicate;
/*      */       }
/*      */       else
/*      */       {
/*  549 */         bis = new BufferedInputStream(is);
/*      */       }
/*      */ 
/*  552 */       this.m_isStreamingResponse = true;
/*      */ 
/*  561 */       parseHeaders(headers, bis, true);
/*      */ 
/*  564 */       boolean didBody = false;
/*  565 */       String responseInclude = headers.getEnvironmentValue("IDCRESPONSEINCLUDE");
/*  566 */       if (responseInclude != null)
/*      */       {
/*  568 */         Object pageMergerObj = cxt.getCachedObject("PageMerger");
/*  569 */         if ((pageMergerObj != null) && (pageMergerObj instanceof DynamicHtmlMerger))
/*      */         {
/*      */           try
/*      */           {
/*  573 */             DynamicHtmlMerger merger = (DynamicHtmlMerger)pageMergerObj;
/*  574 */             String bodyStr = merger.evaluateResourceInclude(responseInclude);
/*  575 */             byte[] respBytes = StringUtils.getBytes(bodyStr, DataSerializeUtils.determineEncoding(headers, cxt));
/*  576 */             headers.setEnvironmentValue("CONTENT_LENGTH", "" + respBytes.length);
/*  577 */             if (this.m_responseHeaders == null)
/*      */             {
/*  579 */               this.m_responseHeaders = new IdcStringBuilder(100);
/*      */             }
/*  581 */             this.m_responseHeaders.append("Content-Length: " + respBytes.length + "\r\n");
/*  582 */             this.m_responseBody = new BufferedInputStream(new ByteArrayInputStream(respBytes));
/*  583 */             didBody = true;
/*      */           }
/*      */           catch (IOException e)
/*      */           {
/*  587 */             throw new DataException(e.getMessage());
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  593 */       if (!didBody)
/*      */       {
/*  595 */         this.m_responseBody = bis;
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  600 */       throw new ServiceException(-5, "!csUnableToReceiveRequest", e);
/*      */     }
/*      */ 
/*  605 */     return (this.m_responseHeaders != null) ? this.m_responseHeaders.toString() : null;
/*      */   }
/*      */ 
/*      */   public BufferedInputStream getResponseBodyInputStream(ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  611 */     return this.m_responseBody;
/*      */   }
/*      */ 
/*      */   public void prepareRequestProps(DataBinder inBinder, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  617 */     if (ctxt != null)
/*      */     {
/*  619 */       ctxt.setCachedObject("requestHeaders", this.m_requestProps);
/*  620 */       int retVal = PluginFilters.filter("outgoingRequestHeaders", null, inBinder, ctxt);
/*      */ 
/*  622 */       if (retVal == -1)
/*      */       {
/*  624 */         return;
/*      */       }
/*  626 */       if (retVal == 0)
/*      */       {
/*  628 */         addAdditionalHeaders(inBinder, this.m_requestProps, ctxt);
/*      */       }
/*      */     }
/*      */ 
/*  632 */     if (!this.m_doFullHeaders)
/*      */       return;
/*  634 */     String userAssigned = inBinder.getEnvironmentValue("PROXY_USER");
/*  635 */     if (userAssigned != null)
/*      */     {
/*  638 */       this.m_requestProps.put("REMOTE_USER", userAssigned);
/*      */ 
/*  641 */       if (this.m_connection != null)
/*      */       {
/*  643 */         DataBinder provData = this.m_connection.getProviderData();
/*  644 */         if (provData != null)
/*      */         {
/*  646 */           boolean isProxying = StringUtils.convertToBool(provData.getLocal("IsProxiedServer"), false);
/*  647 */           if (isProxying)
/*      */           {
/*  649 */             String relRoot = provData.getLocal("HttpRelativeWebRoot");
/*  650 */             if (relRoot != null)
/*      */             {
/*  652 */               this.m_requestProps.put("HTTP_RELATIVEURL", relRoot);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  664 */       copyEnvProperty("REMOTE_USER", inBinder);
/*  665 */       copyEnvProperty("SERVER_PROTOCOL", inBinder);
/*  666 */       copyEnvProperty("SERVER_NAME", inBinder);
/*  667 */       copyEnvProperty("SERVER_PORT", inBinder);
/*  668 */       copyEnvProperty("HTTP_INTERNETUSER", inBinder);
/*  669 */       copyEnvProperty("EXTERNAL_ROLES", inBinder);
/*  670 */       copyEnvProperty("EXTERNAL_ACCOUNTS", inBinder);
/*  671 */       copyEnvProperty("EXTERNAL_EXTENDEDUSERINFO", inBinder);
/*  672 */       copyEnvProperty("EXTERNAL_USERSOURCE", inBinder);
/*  673 */       copyEnvProperty("EXTERNAL_USERORG", inBinder);
/*  674 */       copyEnvProperty("HTTP_COOKIE", inBinder);
/*  675 */       copyEnvProperty("HTTP_RELATIVEURL", inBinder);
/*  676 */       copyEnvProperty("HTTP_TARGETINSTANCE", inBinder);
/*  677 */       copyEnvProperty("HTTP_IF_MODIFIED_SINCE", inBinder);
/*  678 */       copyEnvProperty("HTTP_IF_NONE_MATCH", inBinder);
/*  679 */       copyEnvProperty("HTTP_REFERER", inBinder);
/*      */     }
/*  681 */     copyEnvProperty("HTTP_HOST", inBinder);
/*  682 */     copyEnvProperty("HTTP_USER_AGENT", inBinder);
/*      */ 
/*  687 */     this.m_requestProps.put("SERVER_PROTOCOL_TYPE", "NONE");
/*      */ 
/*  694 */     this.m_requestProps.put("IDC_REQUEST_AGENT", "javaserver");
/*      */ 
/*  698 */     String versionInfo = "IDC " + VersionInfo.getProductVersion();
/*  699 */     this.m_requestProps.put("SERVER_SOFTWARE", versionInfo);
/*      */ 
/*  703 */     this.m_requestProps.put("HTTP_IDCVERSION", versionInfo);
/*      */   }
/*      */ 
/*      */   public void addAdditionalHeaders(DataBinder inBinder, Properties headers, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  718 */     if ((inBinder == null) || (headers == null))
/*      */     {
/*  720 */       return;
/*      */     }
/*      */ 
/*  723 */     boolean isJava = StringUtils.convertToBool(inBinder.getLocal("IsJava"), inBinder.m_isJava);
/*      */ 
/*  725 */     String responseRule = (isJava) ? "hda" : "cgi";
/*  726 */     headers.put("IDCRESPONSERULE", responseRule);
/*      */ 
/*  728 */     String proxyCgiPathRoot = inBinder.getEnvironmentValue("HTTP_CGIPATHROOT");
/*      */ 
/*  730 */     if (proxyCgiPathRoot != null)
/*      */     {
/*  732 */       headers.put("HTTP_CGIPATHROOT", proxyCgiPathRoot);
/*      */     }
/*      */ 
/*  735 */     ProxyConnectionUtils.addAdditionalOutgoingProxyHeaders(headers, inBinder, cxt);
/*      */   }
/*      */ 
/*      */   public void prepareParentPath(String fileName) throws ServiceException
/*      */   {
/*  740 */     String parent = FileUtils.getDirectory(fileName);
/*  741 */     if (parent == null)
/*      */     {
/*  743 */       String msg = LocaleUtils.encodeMessage("syPathInvalid", null, fileName);
/*      */ 
/*  745 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  748 */     FileUtils.checkOrCreateDirectory(parent, 2);
/*      */   }
/*      */ 
/*      */   public void saveFile(InputStream bis, String outFileName, int size)
/*      */     throws ServiceException, IOException
/*      */   {
/*  754 */     FileOutputStream outStream = null;
/*      */     try
/*      */     {
/*  757 */       prepareParentPath(outFileName);
/*  758 */       outStream = new FileOutputStream(outFileName);
/*      */ 
/*  761 */       int numRead = 0;
/*  762 */       long totalCount = 0L;
/*  763 */       int arraySize = 10240;
/*  764 */       byte[] buff = new byte[arraySize];
/*  765 */       boolean firstTime = true;
/*  766 */       while ((numRead = bis.read(buff, 0, arraySize)) != -1)
/*      */       {
/*  768 */         int offset = 0;
/*  769 */         if ((firstTime) && (numRead >= 24))
/*      */         {
/*  777 */           String t = new String(buff, 0, 24);
/*  778 */           if (t.indexOf(F_IDCFILE_MARKER) >= 0)
/*      */           {
/*  780 */             offset = 24;
/*      */           }
/*      */         }
/*  783 */         outStream.write(buff, offset, numRead);
/*  784 */         totalCount += numRead;
/*  785 */         firstTime = false;
/*  786 */         String msg = LocaleUtils.encodeMessage("csReceivingFile", null, outFileName);
/*      */ 
/*  788 */         reportProgress(msg, (float)totalCount, size);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  793 */       FileUtils.closeFiles(outStream, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void flushConnection(DataBinder binder)
/*      */   {
/*  799 */     flushInputStream(binder.m_inStream);
/*  800 */     flushInputStream(this.m_connection.getInputStream());
/*      */   }
/*      */ 
/*      */   public void flushInputStream(InputStream is)
/*      */   {
/*      */     try
/*      */     {
/*  807 */       if (is != null)
/*      */       {
/*  809 */         byte[] buff = new byte[1024];
/*  810 */         while (is.available() > 0)
/*      */         {
/*  812 */           int readCount = is.read(buff, 0, 1024);
/*  813 */           if (readCount <= 0) {
/*      */             break;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  822 */       if (!SystemUtils.m_verbose)
/*      */         return;
/*  824 */       Report.debug(null, null, t);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void reportProgress(String msg)
/*      */   {
/*  834 */     reportProgress(msg, -1.0F, -1.0F);
/*      */   }
/*      */ 
/*      */   public void reportProgress(String msg, float amtDone, float max)
/*      */   {
/*  839 */     if (this.m_progress == null)
/*      */       return;
/*  841 */     this.m_progress.reportProgress(1, msg, amtDone, max);
/*      */   }
/*      */ 
/*      */   protected void copyEnvProperty(String name, DataBinder requestBinder)
/*      */     throws DataException
/*      */   {
/*  850 */     if (this.m_requestProps.get(name) != null)
/*      */       return;
/*  852 */     String value = requestBinder.getEnvironmentValue(name);
/*  853 */     if (value == null)
/*      */       return;
/*  855 */     this.m_requestProps.put(name, value);
/*      */   }
/*      */ 
/*      */   public int parseHeaders(DataBinder binder, BufferedInputStream bis, boolean createHttpHeaders)
/*      */     throws ServiceException
/*      */   {
/*  880 */     int formatType = 0;
/*      */ 
/*  883 */     this.m_responseHeaders = null;
/*      */     try
/*      */     {
/*  889 */       boolean isHttpResponse = false;
/*  890 */       boolean isRequestStyleResponse = false;
/*  891 */       byte[] inByte = new byte[100];
/*  892 */       bis.mark(1000);
/*  893 */       int numRead = bis.read(inByte);
/*  894 */       if (numRead > 0)
/*      */       {
/*  896 */         String lookAheadStr = new String(inByte, 0, numRead);
/*  897 */         lookAheadStr = lookAheadStr.toLowerCase();
/*  898 */         int index1 = lookAheadStr.indexOf("http/1.");
/*  899 */         int index2 = lookAheadStr.indexOf("@properties");
/*  900 */         int index3 = lookAheadStr.indexOf("$$");
/*  901 */         if ((index1 >= 0) && (((index2 < 0) || (index2 > index1))) && (((index3 < 0) || (index3 > index2))))
/*      */         {
/*  904 */           isHttpResponse = true;
/*      */         }
/*      */         else
/*      */         {
/*  909 */           int index4 = lookAheadStr.indexOf("=");
/*  910 */           int index5 = lookAheadStr.indexOf("<?hda");
/*  911 */           if ((index4 > 0) && (((index2 < 0) || (index2 > index4))) && (((index5 < 0) || (index5 > index4))))
/*      */           {
/*  914 */             isRequestStyleResponse = true;
/*  915 */             formatType = 2;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  920 */       bis.reset();
/*  921 */       IdcStringBuilder responseHeaders = new IdcStringBuilder(100);
/*  922 */       if (isRequestStyleResponse)
/*      */       {
/*  924 */         DataSerializeUtils.prepareParseRequest(binder, bis, null);
/*      */       }
/*  926 */       else if (isHttpResponse)
/*      */       {
/*  928 */         String line = null;
/*  929 */         while ((line = DataSerializeUtils.readLineEx(binder, bis, false, true, null)) != null)
/*      */         {
/*  932 */           if (line.length() == 0)
/*      */           {
/*      */             break;
/*      */           }
/*      */ 
/*  938 */           boolean keepLine = true;
/*  939 */           int index = line.indexOf(58);
/*  940 */           if (index >= 0)
/*      */           {
/*  943 */             String name = line.substring(0, index);
/*  944 */             String value = line.substring(index + 1).trim();
/*  945 */             name = name.replace('-', '_');
/*  946 */             name = name.toUpperCase();
/*      */ 
/*  948 */             if (name.indexOf(32) < 0)
/*      */             {
/*  950 */               if ((name.equals("CONTENT_TYPE")) && 
/*  952 */                 (value.indexOf("multipart") >= 0))
/*      */               {
/*  956 */                 binder.setEnvironmentValue("REQUEST_METHOD", "POST");
/*      */ 
/*  960 */                 formatType = 2;
/*      */               }
/*      */ 
/*  963 */               binder.setEnvironmentValue(name, value);
/*      */             }
/*      */           }
/*  966 */           if ((createHttpHeaders) && (keepLine))
/*      */           {
/*  968 */             responseHeaders.append(line);
/*  969 */             responseHeaders.append("\r\n");
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  974 */       if ((formatType == 0) && (!this.m_isStreamingResponse))
/*      */       {
/*  982 */         String fileInfo = checkForFileFormat(bis);
/*  983 */         if (fileInfo == null)
/*      */         {
/*  985 */           formatType = 1;
/*      */         }
/*      */         else
/*      */         {
/*  989 */           binder.putLocal(F_IDCFILE_MARKER, fileInfo);
/*  990 */           formatType = 3;
/*      */         }
/*      */       }
/*      */ 
/*  994 */       if (createHttpHeaders)
/*      */       {
/*  996 */         this.m_responseHeaders = responseHeaders;
/*      */       }
/*      */     }
/*      */     catch (InterruptedIOException e)
/*      */     {
/* 1001 */       throw new ServiceException("!csInterruptedRead", e);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1005 */       throw new ServiceException("!syUnableToReadStream", e);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1009 */       throw new ServiceException("!syUnableToReadStream", e);
/*      */     }
/*      */ 
/* 1013 */     return formatType;
/*      */   }
/*      */ 
/*      */   public String checkForFileFormat(BufferedInputStream bis)
/*      */     throws IOException
/*      */   {
/* 1028 */     String fileInfo = null;
/*      */ 
/* 1031 */     bis.mark(1000);
/*      */ 
/* 1035 */     byte[] inByte = new byte[24];
/* 1036 */     int numRead = bis.read(inByte);
/*      */ 
/* 1038 */     if (numRead == 24)
/*      */     {
/* 1040 */       fileInfo = new String(inByte);
/* 1041 */       int index = fileInfo.indexOf(F_IDCFILE_MARKER);
/* 1042 */       if (index < 0)
/*      */       {
/* 1044 */         fileInfo = null;
/*      */       }
/*      */     }
/* 1047 */     if (fileInfo == null)
/*      */     {
/* 1049 */       bis.reset();
/*      */     }
/* 1051 */     return fileInfo;
/*      */   }
/*      */ 
/*      */   public void closeRequest(ExecutionContext ctxt)
/*      */   {
/*      */     try
/*      */     {
/* 1058 */       PluginFilters.filter("closeSocketRequest", null, this.m_activeOutBinder, ctxt);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1063 */       SystemUtils.dumpException(null, e);
/*      */     }
/*      */ 
/* 1066 */     if ((ctxt != null) && (ctxt.getCachedObject("closeSocketRequest") != null))
/*      */       return;
/* 1068 */     this.m_connection.closeServerConnection();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1074 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102737 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.StandardServerRequest
 * JD-Core Version:    0.5.4
 */