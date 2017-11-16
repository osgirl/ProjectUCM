/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcPipedInputStream;
/*     */ import intradoc.common.IdcPipedOutputStream;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.chunker.ChunkedIncomingConnection;
/*     */ import intradoc.server.chunker.ChunkedRequestSession;
/*     */ import intradoc.server.chunker.ChunkedRequestSessionManager;
/*     */ import intradoc.server.chunker.ChunkedUploadSession;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ChunkedService extends Service
/*     */ {
/*     */   ChunkedRequestSessionManager m_manager;
/*     */   ChunkedUploadSession m_cus;
/*     */   protected boolean m_isEnd;
/*     */   protected boolean m_waitResponse;
/*     */   protected InputStream m_inStream;
/*     */   protected boolean m_isTimeout;
/*     */ 
/*     */   public ChunkedService()
/*     */   {
/*  56 */     this.m_isEnd = false;
/*  57 */     this.m_waitResponse = false;
/*     */ 
/*  61 */     this.m_isTimeout = false;
/*     */   }
/*     */ 
/*     */   public void init(Workspace ws, OutputStream output, DataBinder binder, ServiceData serviceData) throws DataException
/*     */   {
/*  66 */     super.init(ws, output, binder, serviceData);
/*  67 */     setConditionVar("isChunking", true);
/*     */   }
/*     */ 
/*     */   public void initDelegatedObjects() throws DataException, ServiceException
/*     */   {
/*  72 */     super.initDelegatedObjects();
/*  73 */     this.m_requestImplementor.doNotAllowProxyForwarding();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void doUpload() throws ServiceException, DataException
/*     */   {
/*     */     try
/*     */     {
/*  81 */       Report.trace("chunkedrequest", "In doUpload()", null);
/*  82 */       Report.trace("chunkedrequest", "TranedSize: " + this.m_binder.getLocal("TranedSize"), null);
/*     */ 
/*  84 */       this.m_manager = ((ChunkedRequestSessionManager)SharedObjects.getObject("chunkedservice.chunkedrequest.ChunkedRequestSessionManager", "ChunkedRequest"));
/*     */ 
/*  86 */       if (this.m_manager == null)
/*     */       {
/*  88 */         this.m_manager = new ChunkedRequestSessionManager();
/*  89 */         SharedObjects.putObject("chunkedservice.chunkedrequest.ChunkedRequestSessionManager", "ChunkedRequest", this.m_manager);
/*     */       }
/*     */ 
/*  92 */       doUploadEx();
/*     */     }
/*     */     finally
/*     */     {
/*  97 */       if ((!this.m_isEnd) && (this.m_cus != null) && (!this.m_cus.isClosed()) && (this.m_manager != null))
/*     */       {
/*  99 */         this.m_manager.register(this.m_cus, getTimeOut());
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void doUploadEx()
/*     */     throws ServiceException, DataException
/*     */   {
/* 109 */     String session = this.m_binder.getLocal("ChunkSessionID");
/* 110 */     String tmp = this.m_binder.getLocal("ChunkedRequest");
/* 111 */     if (tmp != null)
/*     */     {
/* 113 */       if (tmp.equals("end"))
/*     */       {
/* 115 */         this.m_isEnd = true;
/*     */       }
/* 117 */       else if (tmp.equals("error"))
/*     */       {
/* 119 */         doError(session);
/*     */       }
/*     */     }
/* 122 */     if (session != null)
/*     */     {
/* 124 */       if (!doContinueLoad(session))
/*     */       {
/* 126 */         Report.trace("chunkedrequest", "Cannot find session with ID: " + session, null);
/* 127 */         Report.trace("chunkedrequest", "Set ChunkResponse as failed", null);
/* 128 */         this.m_binder.putLocal("ChunkResponse", "failed");
/*     */ 
/* 130 */         return;
/*     */       }
/*     */ 
/* 133 */       int code = this.m_cus.verify(this.m_binder);
/*     */ 
/* 135 */       switch (code)
/*     */       {
/*     */       case -1:
/* 139 */         Report.trace("chunkedrequest", "Data corrupted. Bail out.", null);
/* 140 */         Report.trace("chunkedrequest", "TranedSize: " + this.m_cus.getTranedSize(), null);
/* 141 */         Report.trace("chunkedrequest", "ChunkSize: " + this.m_cus.getChunkSize(), null);
/* 142 */         Report.trace("chunkedrequest", "FileSize: " + this.m_cus.getFileSize(), null);
/* 143 */         this.m_binder.putLocal("ChunkResponse", "failed");
/* 144 */         this.m_binder.putLocal("StatusMessage", "Unresolvable size difference.");
/* 145 */         this.m_cus.closeSession();
/* 146 */         return;
/*     */       case 0:
/* 149 */         Report.trace("chunkedrequest", "DataExists. Skip upload.", null);
/* 150 */         this.m_binder.putLocal("ChunkResponse", "continue");
/* 151 */         this.m_binder.putLocal("ChunkSessionID", this.m_cus.getSessionID());
/* 152 */         return;
/*     */       case 1:
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 160 */       doStart();
/*     */     }
/*     */ 
/* 163 */     checkInput(this.m_cus);
/* 164 */     uploadData(this.m_cus);
/* 165 */     this.m_waitResponse = this.m_isEnd;
/* 166 */     checkInput(this.m_cus);
/*     */ 
/* 168 */     if (this.m_binder.getLocal("ChunkResponse") == null)
/*     */     {
/* 170 */       this.m_binder.putLocal("ChunkResponse", "continue");
/*     */     }
/* 172 */     this.m_binder.putLocal("ChunkSessionID", this.m_cus.getSessionID());
/* 173 */     Report.trace("chunkedrequest", "Leaving doUpload()", null);
/*     */   }
/*     */ 
/*     */   protected int getTimeOut()
/*     */   {
/* 178 */     int timeOut = 180000;
/*     */ 
/* 180 */     timeOut += this.m_cus.getTimeOut();
/* 181 */     return timeOut;
/*     */   }
/*     */ 
/*     */   protected void doError(String session)
/*     */   {
/* 187 */     ChunkedRequestSession crs = retrieveSession(session);
/* 188 */     crs.closeSession();
/*     */ 
/* 191 */     this.m_binder.putLocal("ChunkResponse", "failed");
/*     */   }
/*     */ 
/*     */   protected void doStart() throws DataException, ServiceException
/*     */   {
/* 196 */     this.m_cus = createNewSession();
/* 197 */     createNewThread(this.m_cus);
/* 198 */     this.m_cus.init(this.m_binder);
/*     */   }
/*     */ 
/*     */   protected ChunkedUploadSession createNewSession()
/*     */   {
/* 203 */     this.m_cus = new ChunkedUploadSession();
/* 204 */     this.m_cus.setInputStream(new IdcPipedInputStream());
/* 205 */     this.m_cus.setOutputStream(new IdcPipedOutputStream());
/*     */ 
/* 207 */     this.m_binder.putLocal("SessionID", this.m_cus.getSessionID());
/* 208 */     return this.m_cus;
/*     */   }
/*     */ 
/*     */   protected Thread createNewThread(ChunkedUploadSession cus)
/*     */     throws DataException, ServiceException
/*     */   {
/* 214 */     IdcPipedInputStream ipis = new IdcPipedInputStream((IdcPipedOutputStream)cus.getOutputStream());
/*     */ 
/* 216 */     IdcPipedOutputStream ipos = new IdcPipedOutputStream((IdcPipedInputStream)cus.getInputStream());
/*     */ 
/* 219 */     ChunkedIncomingConnection cic = new ChunkedIncomingConnection();
/*     */     try
/*     */     {
/* 224 */       cic.setInputStream(ipis);
/* 225 */       cic.setOutputStream(ipos);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 229 */       throw new DataException("!csChunkingErrorCreatingConn", e);
/*     */     }
/*     */ 
/* 232 */     IdcServerThread t = new IdcServerThread();
/* 233 */     t.init(null, cic);
/* 234 */     SystemUtils.startClientThread(t);
/* 235 */     return t;
/*     */   }
/*     */ 
/*     */   protected boolean doContinueLoad(String session)
/*     */   {
/* 240 */     this.m_cus = retrieveSession(session);
/*     */ 
/* 244 */     return this.m_cus != null;
/*     */   }
/*     */ 
/*     */   protected ChunkedUploadSession retrieveSession(String session)
/*     */   {
/* 251 */     this.m_manager = ((ChunkedRequestSessionManager)SharedObjects.getObject("chunkedservice.chunkedrequest.ChunkedRequestSessionManager", "ChunkedRequest"));
/*     */ 
/* 254 */     return (ChunkedUploadSession)this.m_manager.retrieve(session);
/*     */   }
/*     */ 
/*     */   protected void uploadData(ChunkedUploadSession cus)
/*     */     throws DataException
/*     */   {
/* 260 */     String filePath = getFilePath("Chunked");
/*     */ 
/* 262 */     if ((filePath == null) || (filePath.length() == 0))
/*     */     {
/* 264 */       throw new DataException("!syFileMissing");
/*     */     }
/*     */ 
/* 267 */     OutputStream out = cus.getOutputStream();
/* 268 */     BufferedInputStream bis = null;
/*     */     try
/*     */     {
/* 271 */       File f = new File(filePath);
/*     */ 
/* 273 */       bis = new BufferedInputStream(new FileInputStream(filePath));
/*     */ 
/* 275 */       byte[] b = new byte[2048];
/*     */ 
/* 277 */       while ((len = bis.read(b)) != -1)
/*     */       {
/*     */         int len;
/* 280 */         if (!cus.m_isHeaderCheckDone)
/*     */         {
/* 282 */           cus.headerCheck(b, len);
/*     */         }
/* 284 */         out.write(b, 0, len);
/*     */       }
/*     */ 
/* 287 */       if (this.m_isEnd)
/*     */       {
/* 289 */         out.write("$$\n".getBytes());
/*     */       }
/* 291 */       out.flush();
/*     */ 
/* 294 */       this.m_cus.addTranedSize(f.length());
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 299 */       e.printStackTrace();
/*     */     }
/*     */     finally
/*     */     {
/* 303 */       FileUtils.closeObject(bis);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void checkInput(ChunkedUploadSession cus)
/*     */   {
/* 310 */     InputStream in = cus.getInputStream();
/*     */     try
/*     */     {
/* 313 */       while (this.m_waitResponse)
/*     */       {
/* 315 */         int timeout = 0;
/* 316 */         int target = 18000;
/* 317 */         if (in.available() > 0) {
/*     */           break;
/*     */         }
/* 320 */         SystemUtils.sleep(50L);
/*     */ 
/* 322 */         ++timeout;
/* 323 */         if (timeout > target)
/*     */         {
/* 325 */           this.m_binder.putLocal("ChunkResponse", "response");
/* 326 */           this.m_binder.putLocal("StatusMessageKey", "!csChunkingResponseTimeOut");
/* 327 */           this.m_binder.putLocal("StatusMessage", LocaleResources.localizeMessage("!csChunkingResponseTimeOut", null));
/*     */ 
/* 330 */           this.m_isTimeout = true;
/* 331 */           break;
/*     */         }
/*     */       }
/* 334 */       if (in.available() > 0)
/*     */       {
/* 336 */         this.m_inStream = in;
/* 337 */         this.m_isEnd = true;
/*     */ 
/* 339 */         this.m_binder.putLocal("ChunkResponse", "response");
/* 340 */         if (SystemUtils.m_verbose)
/*     */         {
/* 342 */           IdcCharArrayWriter s = new IdcCharArrayWriter();
/* 343 */           this.m_binder.send(s);
/* 344 */           Report.debug("chunkedrequest", s.toStringRelease(), null);
/*     */         }
/* 346 */         Report.trace("chunkedrequest", "Found response", null);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 351 */       new DataException("Cann't read from InputStream.");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String getFilePath(String fileKey)
/*     */   {
/* 359 */     String filePath = this.m_binder.getAllowMissing(fileKey + ":path");
/*     */ 
/* 368 */     if (this.m_binder.m_isExternalRequest)
/*     */     {
/* 370 */       if (filePath == null)
/*     */       {
/* 372 */         return null;
/*     */       }
/* 374 */       Vector tempFiles = this.m_binder.getTempFiles();
/* 375 */       if (tempFiles == null)
/*     */       {
/* 377 */         return null;
/*     */       }
/* 379 */       boolean uploaded = false;
/* 380 */       for (int i = 0; i < tempFiles.size(); ++i)
/*     */       {
/* 382 */         String path = (String)tempFiles.elementAt(i);
/* 383 */         if (!path.equalsIgnoreCase(filePath))
/*     */           continue;
/* 385 */         uploaded = true;
/* 386 */         break;
/*     */       }
/*     */ 
/* 389 */       if (!uploaded)
/*     */       {
/* 391 */         return null;
/*     */       }
/* 393 */       return filePath;
/*     */     }
/*     */ 
/* 398 */     if ((filePath == null) || (filePath.trim().length() == 0))
/*     */     {
/* 400 */       filePath = this.m_binder.getAllowMissing(fileKey);
/*     */     }
/*     */ 
/* 403 */     return filePath;
/*     */   }
/*     */ 
/*     */   public void doResponse(boolean isError, ServiceException err)
/*     */     throws ServiceException
/*     */   {
/* 413 */     if (isError)
/*     */     {
/* 415 */       super.doResponse(isError, err);
/* 416 */       return;
/*     */     }
/* 418 */     byte[] tempBuf = new byte[2048];
/* 419 */     if ((this.m_isEnd) && (!this.m_isTimeout))
/*     */     {
/* 423 */       BufferedInputStream bis = new BufferedInputStream(this.m_inStream, tempBuf.length);
/*     */       try
/*     */       {
/* 427 */         bis.mark(tempBuf.length);
/*     */ 
/* 429 */         byte[] firstBytes = tempBuf;
/* 430 */         int len = bis.read(firstBytes);
/* 431 */         String tmpStr = StringUtils.toStringRaw(firstBytes, 0, len);
/* 432 */         int index = -1;
/* 433 */         int skipIndex = 0;
/* 434 */         String decodedStr = null;
/* 435 */         String encoding = null;
/* 436 */         boolean useEncoding = true;
/* 437 */         byte[] preBytes = null;
/*     */ 
/* 443 */         boolean hadEndHeader = false;
/* 444 */         int endHeader = tmpStr.indexOf("\r\n\r\n");
/* 445 */         if (endHeader < 0)
/*     */         {
/* 447 */           endHeader = len;
/*     */         }
/*     */         else
/*     */         {
/* 451 */           hadEndHeader = true;
/* 452 */           endHeader += 4;
/*     */         }
/* 454 */         int endHeaderInString = 0;
/* 455 */         if (tmpStr.startsWith("HTTP/"))
/*     */         {
/* 457 */           encoding = DataSerializeUtils.getSystemEncoding();
/*     */           try
/*     */           {
/* 460 */             decodedStr = new String(firstBytes, 0, endHeader, encoding);
/*     */           }
/*     */           catch (UnsupportedEncodingException ignore)
/*     */           {
/* 464 */             Report.trace("chunkedrequest", null, ignore);
/*     */           }
/*     */           catch (IndexOutOfBoundsException ignore)
/*     */           {
/* 468 */             if (SystemUtils.m_verbose)
/*     */             {
/* 470 */               Report.debug("chunkedrequest", null, ignore);
/*     */             }
/*     */           }
/*     */           finally
/*     */           {
/* 475 */             if ((decodedStr == null) || (decodedStr.length() == 0))
/*     */             {
/* 477 */               decodedStr = tmpStr.substring(0, endHeader);
/* 478 */               useEncoding = false;
/*     */             }
/*     */           }
/* 481 */           endHeaderInString = decodedStr.length();
/* 482 */           int keepAlive = decodedStr.toLowerCase().indexOf("connection: keep-alive");
/*     */ 
/* 484 */           IdcStringBuilder sb = new IdcStringBuilder();
/* 485 */           String userAgent = this.m_binder.getEnvironmentValue("HTTP_USER_AGENT");
/* 486 */           if ((((userAgent == null) || (!userAgent.equals("MSJAVA")))) && (keepAlive != -1))
/*     */           {
/* 489 */             sb.append(decodedStr.substring(0, keepAlive));
/* 490 */             sb.append("Connection: closed");
/* 491 */             sb.append(decodedStr.substring(keepAlive + 24));
/*     */ 
/* 493 */             tmpStr = sb.toString();
/*     */           }
/*     */           else
/*     */           {
/* 497 */             int newLine = decodedStr.indexOf("\n", 0) + 1;
/* 498 */             sb.append(decodedStr, 0, newLine);
/* 499 */             sb.append("Chunked-Response-Code: response\r\n");
/* 500 */             sb.append(decodedStr, newLine, decodedStr.length() - newLine);
/* 501 */             tmpStr = sb.toString();
/*     */           }
/* 503 */           skipIndex = endHeader;
/* 504 */           preBytes = tmpStr.getBytes(encoding);
/*     */         }
/* 506 */         else if ((index = tmpStr.indexOf("@Properties LocalData")) != -1)
/*     */         {
/* 508 */           if (hadEndHeader)
/*     */           {
/* 510 */             String beginLine = tmpStr.substring(endHeader, index);
/* 511 */             encoding = DataSerializeUtils.parseHdaEncoding(beginLine);
/*     */           }
/* 513 */           if (encoding == null)
/*     */           {
/* 515 */             encoding = DataSerializeUtils.getSystemEncoding();
/*     */           }
/* 517 */           int endPropertiesLine = tmpStr.indexOf(10, index) + 1;
/* 518 */           skipIndex = endPropertiesLine;
/* 519 */           decodedStr = new String(firstBytes, 0, endPropertiesLine, encoding);
/* 520 */           if (SystemUtils.m_verbose)
/*     */           {
/* 522 */             Report.debug("chunkedrequest", "IsJava beginning part of response used for splicing:\n" + decodedStr, null);
/*     */           }
/*     */ 
/* 526 */           String response = "ChunkResponse=response\n";
/* 527 */           IdcStringBuilder sBuf = new IdcStringBuilder();
/* 528 */           sBuf.append(decodedStr);
/* 529 */           sBuf.append("ChunkResponse=response\n");
/*     */ 
/* 533 */           String key = "Content-Length";
/* 534 */           int lengthIndex = sBuf.indexOf(0, endHeader, key, 0, key.length(), true);
/* 535 */           int lengthStart = sBuf.indexOf(lengthIndex, ':') + 1;
/* 536 */           int lengthEnd = sBuf.indexOf(lengthStart, '\n');
/* 537 */           String lengthStr = sBuf.getTrimmedString(lengthStart, lengthEnd);
/*     */           try
/*     */           {
/* 541 */             int length = Integer.parseInt(lengthStr);
/* 542 */             length += "ChunkResponse=response\n".length();
/*     */ 
/* 544 */             IdcStringBuilder sb = new IdcStringBuilder();
/* 545 */             sb.append("HTTP/1.1 200 OK\r\n");
/*     */ 
/* 547 */             boolean closeConnection = false;
/*     */ 
/* 549 */             if (this.m_binder.getEnvironmentValue("IDCPPROXY-RELATIVEURL") != null)
/*     */             {
/* 551 */               String userAgent = this.m_binder.getEnvironmentValue("HTTP_USER_AGENT");
/* 552 */               closeConnection = (userAgent != null) && (userAgent.equals("MSJAVA"));
/* 553 */               sb.append("Connection: closed\r\n");
/*     */             }
/*     */ 
/* 557 */             String connectionStr = (closeConnection) ? "Connection: closed\r\n" : "Connection: keep-alive\r\n";
/* 558 */             sb.append(connectionStr);
/*     */ 
/* 561 */             sb.append(sBuf, 0, lengthStart);
/* 562 */             sb.append(" ");
/* 563 */             sb.append(length);
/* 564 */             sb.append("\r\n");
/* 565 */             sb.append(sBuf, lengthEnd + 1, sBuf.length() - lengthEnd - 1);
/* 566 */             tmpStr = sb.toString();
/*     */ 
/* 569 */             if (hadEndHeader)
/*     */             {
/* 571 */               endHeader = tmpStr.indexOf("\r\n\r\n");
/* 572 */               if (endHeader > 0)
/*     */               {
/* 574 */                 endHeader += 4;
/* 575 */                 String header = tmpStr.substring(0, endHeader);
/* 576 */                 endHeaderInString = header.getBytes().length;
/*     */               }
/*     */             }
/*     */           }
/*     */           catch (NumberFormatException ignore)
/*     */           {
/* 582 */             ignore.printStackTrace();
/*     */           }
/* 584 */           if (useEncoding)
/*     */           {
/* 586 */             preBytes = tmpStr.getBytes(encoding);
/*     */           }
/*     */           else
/*     */           {
/* 590 */             preBytes = tmpStr.getBytes();
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 595 */         int totalByte = 0;
/* 596 */         Report.trace("chunkedrequest", "-------------------Start response ----------", null);
/* 597 */         if (SystemUtils.m_verbose)
/*     */         {
/* 599 */           Report.debug("chunkedrequest", tmpStr, null);
/*     */         }
/*     */         int i;
/* 601 */         int i = 0;
/* 602 */         if (endHeader < skipIndex)
/*     */         {
/* 604 */           i = preBytes.length - endHeaderInString;
/*     */         }
/* 606 */         totalByte = i;
/* 607 */         byte[] stream = tempBuf;
/* 608 */         this.m_output.write(preBytes);
/* 609 */         bis.reset();
/* 610 */         bis.skip(skipIndex);
/*     */ 
/* 612 */         while ((len = bis.read(stream, 0, stream.length)) != -1)
/*     */         {
/* 614 */           if (SystemUtils.m_verbose)
/*     */           {
/* 616 */             Report.message(null, "chunkedrequest", 7000, null, stream, 0, len, null, null);
/*     */           }
/*     */ 
/* 620 */           this.m_output.write(stream, 0, len);
/* 621 */           totalByte += len;
/*     */         }
/* 623 */         Report.trace("chunkedrequest", "---------------------End response ------------", null);
/* 624 */         Report.trace("chunkedrequest", "Content-Length: " + totalByte, null);
/* 625 */         this.m_cus.closeSession();
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 629 */         throw new ServiceException(e);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 634 */       super.doResponse(isError, err);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 641 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98038 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ChunkedService
 * JD-Core Version:    0.5.4
 */