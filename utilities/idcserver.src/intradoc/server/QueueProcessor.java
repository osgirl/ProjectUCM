/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileMessageHeader;
/*     */ import intradoc.common.FileQueue;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Log;
/*     */ import intradoc.common.LoggingUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StackTrace;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.IdcCounterUtils;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.BasicIdcFileDescriptor;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.refinery.RefineryUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.workflow.WfCompanionManager;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class QueueProcessor
/*     */ {
/*  65 */   public static long m_lastRefineryJobCheckTime = -1L;
/*  66 */   public static long m_refineryCheckInterval = -1L;
/*  67 */   public static boolean m_retryPostProcessErrors = false;
/*     */ 
/*  70 */   public static long m_jobExpirationTime = -1L;
/*  71 */   public static long m_jobExpirationActionTime = -1L;
/*     */ 
/*  73 */   public static boolean m_refineryJobChanged = false;
/*     */ 
/*     */   public static void init()
/*     */   {
/*  77 */     m_retryPostProcessErrors = SharedObjects.getEnvValueAsBoolean("RefineryRetryPostProcessErrors", false);
/*  78 */     m_refineryCheckInterval = SharedObjects.getTypedEnvironmentInt("RefineryJobPostProcessCheckInterval", 300000, 18, 24);
/*     */ 
/*  82 */     m_jobExpirationTime = SharedObjects.getTypedEnvironmentInt("RefineryJobExpirationTime", 14400000, 18, 24);
/*     */ 
/*  86 */     m_jobExpirationActionTime = SharedObjects.getTypedEnvironmentInt("RefineryJobActionExpirationTime", 1800000, 18, 24);
/*     */ 
/*  91 */     if (!SystemUtils.m_verbose)
/*     */       return;
/*  93 */     Report.trace("ibrsupport", "RefineryJobPostProcessCheckInterval: " + m_refineryCheckInterval, null);
/*  94 */     Report.trace("ibrsupport", "RefineryJobExpirationTime: " + m_jobExpirationTime, null);
/*  95 */     Report.trace("ibrsupport", "RefineryJobActionExpirationTime: " + m_jobExpirationActionTime, null);
/*  96 */     Report.trace("ibrsupport", "RefineryRetryPostProcessErrors: " + m_retryPostProcessErrors, null);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void addDocToConversionQueue(String docID, DataBinder data, String inFile, String outFile)
/*     */     throws ServiceException, DataException
/*     */   {
/* 106 */     SystemUtils.reportDeprecatedUsage("addDocToConversionQueue without ExecutionContext parameter is deprecated.");
/* 107 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 108 */     addDocToConversionQueue(docID, data, inFile, outFile, cxt, null);
/*     */   }
/*     */ 
/*     */   public static void addDocToConversionQueue(String docID, DataBinder data, String inFile, String outFile, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/* 115 */     addDocToConversionQueue(docID, data, inFile, outFile, cxt, null);
/*     */   }
/*     */ 
/*     */   public static void addDocToConversionQueue(String docID, DataBinder data, String inFile, String outFile, ExecutionContext cxt, String agentName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 124 */     Workspace ws = (Workspace)cxt.getCachedObject("Workspace");
/* 125 */     if ((cxt instanceof Service) && (ws == null))
/*     */     {
/* 127 */       ws = ((Service)cxt).getWorkspace();
/*     */     }
/* 129 */     boolean inTran = false;
/*     */     try
/*     */     {
/* 132 */       ws.beginTranEx(1);
/* 133 */       inTran = true;
/*     */ 
/* 138 */       long nextID = IdcCounterUtils.nextValue(ws, "RefineryJobID");
/*     */ 
/* 140 */       data.putLocal("dConvJobID", "" + nextID);
/* 141 */       Date now = new Date();
/* 142 */       data.putLocalDate("dConvStartDate", now);
/* 143 */       data.putLocalDate("dConvActionDate", now);
/* 144 */       data.putLocal("dConversionState", "New");
/* 145 */       ws.execute("IrefineryJobs", data);
/* 146 */       ws.commitTran();
/* 147 */       inTran = false;
/* 148 */       SubjectManager.notifyChanged("refineryjob");
/*     */     }
/*     */     finally
/*     */     {
/* 152 */       if (inTran)
/*     */       {
/* 154 */         ws.rollbackTran();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean needFullCheck()
/*     */   {
/* 162 */     long curTime = System.currentTimeMillis();
/* 163 */     boolean needed = false;
/* 164 */     if ((m_lastRefineryJobCheckTime + m_refineryCheckInterval < curTime) || (m_refineryJobChanged))
/*     */     {
/* 166 */       m_lastRefineryJobCheckTime = curTime;
/* 167 */       needed = true;
/* 168 */       m_refineryJobChanged = false;
/*     */     }
/* 170 */     if (SystemUtils.m_verbose)
/*     */     {
/* 172 */       Report.trace("ibrsupport", "Start full check for converted jobs: " + needed, null);
/*     */     }
/* 174 */     return needed;
/*     */   }
/*     */ 
/*     */   public static void setRefineryJobChanged()
/*     */   {
/* 179 */     if (SystemUtils.m_verbose)
/*     */     {
/* 181 */       Report.trace("ibrsupport", "signal need full converted jobs check", null);
/*     */     }
/* 183 */     m_refineryJobChanged = true;
/*     */   }
/*     */ 
/*     */   public static boolean processConvertedDocuments(Workspace ws, boolean[] isReleaseChanged, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 189 */     if (!needFullCheck())
/*     */     {
/* 191 */       return false;
/*     */     }
/*     */ 
/* 194 */     Service srv = null;
/* 195 */     boolean inTran = false;
/* 196 */     boolean docChanged = false;
/* 197 */     if ((!hasProcessedWork(ws)) && 
/* 199 */       (expireOverdueWork(ws) == 0L))
/*     */     {
/* 201 */       return false;
/*     */     }
/*     */ 
/* 205 */     boolean isProcessed = false;
/* 206 */     Properties docProps = null;
/* 207 */     String jobDataDir = DirectoryLocator.getAppDataDirectory() + "refinery/convertedjobs/";
/*     */     try
/*     */     {
/* 212 */       while ((docProps = RefineryUtils.getNextWork(ws, null, "QrefineryProcessedWork", "PostProc")) != null)
/*     */       {
/* 214 */         docProps = loadConvertedProperties(jobDataDir, docProps);
/* 215 */         String dConvJobID = docProps.getProperty("dConvJobID");
/*     */ 
/* 218 */         DataBinder params = new DataBinder(SharedObjects.getSecureEnvironment());
/* 219 */         params.setLocalData(docProps);
/* 220 */         if (PluginFilters.filter("modifyPropsProcessConvertedDocuments", ws, params, cxt) != 0)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 225 */         char FAILURE = 'F';
/* 226 */         char SUCCESS = 'Y';
/* 227 */         char PASSTHRU = 'P';
/* 228 */         char INCOMPLETE = 'I';
/*     */ 
/* 230 */         char processState = 'F';
/*     */ 
/* 232 */         cxt.setCachedObject("Workspace", ws);
/* 233 */         FileStoreProvider fs = FileStoreProviderLoader.initFileStore(cxt);
/*     */ 
/* 235 */         IdcFileDescriptor outD = null;
/* 236 */         IdcFileDescriptor vaultD = null;
/* 237 */         boolean isPresent = false;
/*     */         try
/*     */         {
/* 241 */           if (srv != null)
/*     */           {
/* 243 */             srv.clear();
/*     */           }
/* 245 */           srv = WfCompanionManager.createService(params, ws);
/* 246 */           srv.beginTransaction();
/* 247 */           inTran = true;
/*     */ 
/* 249 */           RefineryUtils.buildCurrentDocData(ws, params, docProps);
/* 250 */           isPresent = DataBinderUtils.getBoolean(params, "isWebRowPresent", false);
/* 251 */           if (isPresent)
/*     */           {
/* 253 */             params.putLocal("RenditionId", "webViewableFile");
/* 254 */             params.putLocal("reserveLocation", "1");
/* 255 */             Map args = new HashMap();
/* 256 */             args.put("isNew", "1");
/* 257 */             args.put("isRetainMetadata", "1");
/* 258 */             outD = fs.createDescriptor(params, args, cxt);
/*     */ 
/* 260 */             params.putLocal("RenditionId", "primaryFile");
/* 261 */             vaultD = fs.createDescriptor(params, null, cxt);
/*     */ 
/* 264 */             DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/* 265 */             binder.setLocalData(docProps);
/* 266 */             cxt.setCachedObject("WebfileDescriptor", outD);
/* 267 */             cxt.setCachedObject("VaultfileDescriptor", vaultD);
/* 268 */             int ret = PluginFilters.filter("postWebfileCreation", ws, binder, cxt);
/* 269 */             if (ret != -1)
/*     */             {
/* 271 */               String convState = docProps.getProperty("dConversionState");
/* 272 */               if (convState.equalsIgnoreCase("Expired"))
/*     */               {
/* 274 */                 boolean refineryExpiredJobsFail = SharedObjects.getEnvValueAsBoolean("RefineryExpiredJobsFail", true);
/* 275 */                 if (!refineryExpiredJobsFail)
/*     */                 {
/* 277 */                   convState = "PassThru";
/*     */                 }
/*     */               }
/* 280 */               if (convState.equalsIgnoreCase("Converted"))
/*     */               {
/* 282 */                 processState = 'Y';
/*     */               }
/* 284 */               else if (convState.equalsIgnoreCase("PassThru"))
/*     */               {
/* 286 */                 processState = 'P';
/*     */               }
/* 288 */               else if (convState.equalsIgnoreCase("Incomplete"))
/*     */               {
/* 290 */                 processState = 'I';
/*     */               }
/*     */               else
/*     */               {
/* 294 */                 processState = 'F';
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 301 */           Report.trace(null, null, e);
/* 302 */           processState = 'F';
/* 303 */           String errMsg = LocaleUtils.encodeMessage("csFilterError", e.getMessage(), "postWebFileCreation");
/*     */ 
/* 305 */           docProps.put("dMessage", errMsg);
/*     */         }
/*     */ 
/* 308 */         if (processState != 'F')
/*     */         {
/* 310 */           Date curDate = new Date();
/*     */ 
/* 312 */           curDate.setTime(curDate.getTime() + 5000L);
/* 313 */           String dateStr = docProps.getProperty("dInDate");
/* 314 */           boolean isIndexable = true;
/* 315 */           if (dateStr != null)
/*     */           {
/* 317 */             isIndexable = false;
/*     */             try
/*     */             {
/* 320 */               Date inDate = LocaleResources.m_iso8601Format.parseDate(dateStr);
/* 321 */               if (curDate.after(inDate))
/*     */               {
/* 323 */                 isIndexable = true;
/*     */               }
/*     */             }
/*     */             catch (Exception e)
/*     */             {
/* 328 */               String errMsg = LocaleUtils.encodeMessage("csQueueInvalidCheckInDate", null, docProps.getProperty("dName"), dateStr);
/*     */ 
/* 330 */               Report.error(null, errMsg, e);
/*     */             }
/*     */           }
/* 333 */           if ((isIndexable) && (isReleaseChanged != null) && (isReleaseChanged.length > 0))
/*     */           {
/* 335 */             isReleaseChanged[0] = true;
/*     */           }
/*     */         }
/* 338 */         String message = docProps.getProperty("dConvMessage");
/* 339 */         if ((message == null) || (message.length() == 0))
/*     */         {
/* 341 */           switch (processState)
/*     */           {
/*     */           case 'Y':
/* 344 */             message = "!csQueueSuccessMessage";
/* 345 */             break;
/*     */           case 'P':
/* 347 */             message = "!csQueuePassthruMessage";
/* 348 */             break;
/*     */           case 'I':
/* 350 */             message = "!csQueueIncompleteMessage";
/* 351 */             break;
/*     */           default:
/* 353 */             message = "!csQueueUnknownMessage";
/*     */           }
/*     */ 
/* 356 */           params.putLocal("dMessage", message);
/*     */         }
/*     */         else
/*     */         {
/* 360 */           if (!message.startsWith("!"))
/*     */           {
/* 362 */             Report.trace("localization", "A localized message was provided by the refinery: " + message, null);
/*     */ 
/* 364 */             message = LocaleUtils.appendMessage(null, message);
/*     */           }
/* 366 */           if (message.length() > 255)
/*     */           {
/* 368 */             message = message.substring(0, 252) + "...";
/*     */           }
/* 370 */           params.putLocal("dMessage", message);
/*     */         }
/*     */ 
/* 374 */         String dDocID = params.get("dDocID");
/* 375 */         String docName = params.get("dDocName");
/* 376 */         String latestId = null;
/* 377 */         if (isPresent)
/*     */         {
/* 379 */           ResultSet rset = ws.createResultSet("QlatestID", params);
/* 380 */           latestId = ResultSetUtils.getValue(rset, "dID");
/*     */         }
/* 382 */         if ((isPresent) && (latestId != null) && (latestId.length() > 0))
/*     */         {
/* 386 */           String orgdExtension = null;
/* 387 */           if (dDocID.equals(docProps.getProperty("dDocID")))
/*     */           {
/* 389 */             String curDocState = params.get("dStatus");
/* 390 */             boolean isConverting = curDocState.equals("GENWWW");
/* 391 */             boolean isDone = curDocState.equalsIgnoreCase("DONE");
/* 392 */             if ((isConverting) || (isDone))
/*     */             {
/* 394 */               String dID = params.get("dID");
/* 395 */               boolean isNotLatestRev = (latestId != null) && (!dID.equals(latestId));
/* 396 */               if (isNotLatestRev)
/*     */               {
/* 399 */                 params.putLocal("IsNotLatestRev", "1");
/*     */               }
/*     */ 
/* 403 */               if (isConverting)
/*     */               {
/* 406 */                 String dExtension = docProps.getProperty("dExtension");
/* 407 */                 String orgName = docProps.getProperty("dOriginalName");
/* 408 */                 if (orgName == null)
/*     */                 {
/* 410 */                   String outPath = outD.getProperty("path");
/* 411 */                   orgName = FileUtils.getName(outPath);
/*     */                 }
/*     */ 
/* 414 */                 if ((processState == 'Y') || (processState == 'P') || (processState == 'I'))
/*     */                 {
/* 416 */                   params.putLocal("wfAction", "CONVERSION");
/* 417 */                   DocStateTransition.advanceDocumentState(params, ws, isNotLatestRev, true, false, false, srv);
/*     */                 }
/*     */ 
/* 421 */                 computeRenditionInfo(params, fs, cxt);
/*     */ 
/* 423 */                 params.putLocal("dProcessingState", "" + processState);
/* 424 */                 if (dExtension != null)
/*     */                 {
/* 426 */                   params.putLocal("dExtension", dExtension);
/*     */                 }
/* 428 */                 if (orgName != null)
/*     */                 {
/* 430 */                   params.putLocal("dOriginalName", orgName);
/*     */                 }
/*     */ 
/* 435 */                 orgdExtension = params.getLocal("dExtension");
/* 436 */                 String dWebExtension = docProps.getProperty("dWebExtension");
/* 437 */                 if (dWebExtension != null)
/*     */                 {
/* 439 */                   params.putLocal("dExtension", dWebExtension);
/*     */                 }
/* 441 */                 ws.execute("UdocConversionState", params);
/*     */               }
/*     */ 
/* 445 */               String fileSize = ((BasicIdcFileDescriptor)outD).getCacheProperty("fileSize");
/*     */ 
/* 447 */               if (fileSize != null)
/*     */               {
/* 449 */                 params.putLocal("dFileSize", fileSize);
/*     */               }
/*     */ 
/* 454 */               ws.execute("Udocument", params);
/*     */ 
/* 458 */               if (orgdExtension != null)
/*     */               {
/* 460 */                 params.putLocal("dExtension", orgdExtension);
/*     */               }
/* 462 */               if ((isConverting) || (processState == 'Y'))
/*     */               {
/* 464 */                 docChanged = true;
/*     */               }
/* 466 */               if (SystemUtils.m_verbose)
/*     */               {
/* 468 */                 String reportMsg = "Processed conversion for " + docName + " and revision ID " + dID + " with webviewable file size of " + fileSize;
/*     */ 
/* 471 */                 Report.debug("ibrsupport", reportMsg, null);
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 478 */           Report.trace("ibrsupport", "DocName: " + docName + "(DocID: " + dDocID + "); JobID: " + dConvJobID + "; is no longer in the system.", null);
/*     */ 
/* 482 */           boolean deletedVault = false;
/* 483 */           if (vaultD != null)
/*     */           {
/* 490 */             boolean isPrimaryPresent = DataBinderUtils.getBoolean(params, "isPrimaryRowPresent", false);
/*     */ 
/* 492 */             if (isPrimaryPresent)
/*     */             {
/*     */               try
/*     */               {
/* 496 */                 fs.deleteFile(vaultD, null, cxt);
/* 497 */                 deletedVault = true;
/*     */               }
/*     */               catch (Exception e)
/*     */               {
/* 501 */                 if (SystemUtils.m_verbose)
/*     */                 {
/* 503 */                   Report.trace("ibrsupport", "After document was deleted from system. Failed to delete primary file.", e);
/*     */                 }
/* 505 */                 LoggingUtils.warning(e, null, null);
/*     */               }
/*     */             }
/*     */           }
/* 509 */           processRenditionInfo(params, true, fs, cxt);
/* 510 */           String dID = params.get("dID");
/* 511 */           String infoMsgRoot = (deletedVault) ? "csQueueDeletedAllRenditionsForRevision" : "csQueueDeletedAllWebviewabledsForRevision";
/* 512 */           Report.info(null, null, infoMsgRoot, new Object[] { docName, dID, dDocID });
/*     */         }
/*     */ 
/* 515 */         srv.commitTransaction();
/* 516 */         inTran = false;
/*     */ 
/* 518 */         RefineryUtils.updateConversionJobState(ws, dConvJobID, "PostProc", "Finished", null);
/*     */ 
/* 520 */         FileUtils.deleteFile(jobDataDir + dConvJobID + ".hda");
/*     */       }
/*     */ 
/* 523 */       isProcessed = true;
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       DataBinder binder;
/*     */       String lastState;
/*     */       String msg;
/*     */       String newState;
/*     */       String docName;
/*     */       String jobId;
/*     */       boolean finishJob;
/* 532 */       if (srv != null)
/*     */       {
/* 534 */         if (inTran)
/*     */         {
/* 536 */           srv.rollbackTransaction();
/*     */         }
/* 538 */         srv.clear();
/*     */       }
/*     */ 
/* 541 */       DataBinder binder = new DataBinder();
/* 542 */       binder.setLocalData(new Properties(docProps));
/* 543 */       if (!isProcessed)
/*     */       {
/* 545 */         String lastState = docProps.getProperty("dConversionState");
/* 546 */         String msg = null;
/* 547 */         String newState = "Finished";
/* 548 */         String docName = docProps.getProperty("dDocName");
/* 549 */         String jobId = docProps.getProperty("dConvJobID");
/* 550 */         boolean finishJob = (lastState.equals("Failed")) || (lastState.equals("Expired"));
/* 551 */         if (!finishJob)
/*     */         {
/* 553 */           if (m_retryPostProcessErrors)
/*     */           {
/* 556 */             msg = LocaleUtils.encodeMessage("csRefineryPostProcessFailed", null, docName, jobId, lastState);
/*     */ 
/* 560 */             newState = "Failed";
/*     */           }
/*     */           else
/*     */           {
/* 564 */             finishJob = true;
/*     */           }
/*     */         }
/* 567 */         if (finishJob)
/*     */         {
/* 569 */           msg = LocaleUtils.encodeMessage("csRefineryPostProcessError", null, docName, jobId, lastState);
/*     */ 
/* 572 */           FileUtils.deleteFile(jobDataDir + jobId + ".hda");
/*     */         }
/* 574 */         Log.error(msg);
/* 575 */         Report.trace(null, msg, null);
/*     */ 
/* 577 */         binder.putLocal("dConversionState", newState);
/* 578 */         binder.putLocal("oldConversionState", "PostProc");
/* 579 */         binder.putLocal("dConvMessage", msg);
/* 580 */         RefineryUtils.updateConversionJobStateEx(ws, binder);
/*     */       }
/* 582 */       ws.execute("DrefineryFinishedJobCleanUp", binder);
/*     */     }
/*     */ 
/* 585 */     return docChanged;
/*     */   }
/*     */ 
/*     */   protected static Properties loadConvertedProperties(String dir, Properties props) throws ServiceException
/*     */   {
/* 590 */     String jobID = props.getProperty("dConvJobID");
/* 591 */     DataBinder binder = new DataBinder();
/* 592 */     ResourceUtils.serializeDataBinder(dir, jobID + ".hda", binder, false, false);
/* 593 */     props.putAll(binder.getLocalData());
/* 594 */     return props;
/*     */   }
/*     */ 
/*     */   public static long expireOverdueWork(Workspace ws)
/*     */     throws DataException
/*     */   {
/* 601 */     long curTime = System.currentTimeMillis();
/*     */ 
/* 603 */     DataBinder binder = new DataBinder();
/* 604 */     long expireTime = curTime - m_jobExpirationTime;
/* 605 */     binder.putLocalDate("dConvStartDate", new Date(expireTime));
/* 606 */     expireTime = curTime - m_jobExpirationActionTime;
/* 607 */     binder.putLocalDate("dConvActionDate", new Date(expireTime));
/* 608 */     long num = ws.execute("UrefineryJobExpiration", binder);
/* 609 */     Report.trace("ibrsupport", "Expired refiner job(s) expired: " + num, null);
/* 610 */     return num;
/*     */   }
/*     */ 
/*     */   public static boolean hasProcessedWork(Workspace ws) throws DataException
/*     */   {
/* 615 */     ResultSet rset = ws.createResultSet("QrefineryProcessedWork", new DataBinder());
/* 616 */     boolean workReady = !rset.isEmpty();
/* 617 */     if (SystemUtils.m_verbose)
/*     */     {
/* 619 */       Report.trace("ibrsupport", "Converted work ready to be processed: " + workReady, null);
/*     */     }
/* 621 */     return workReady;
/*     */   }
/*     */ 
/*     */   public static String getNextConvertedQueueMessage(FileQueue docQueue, FileMessageHeader msgHeader)
/*     */     throws DataException
/*     */   {
/*     */     try
/*     */     {
/* 629 */       return docQueue.getFirstAvailableMessage("postconverted", true, msgHeader);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 634 */       String msg = LocaleUtils.encodeMessage("csQueueUnableToReadMessage", e.getMessage());
/*     */ 
/* 636 */       throw new DataException(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void deleteConvertedQueueMessage(FileQueue docQueue, String id, Properties docProps)
/*     */     throws DataException
/*     */   {
/*     */     try
/*     */     {
/* 645 */       docQueue.deleteMessage("postconverted", id);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 649 */       String name = null;
/* 650 */       if (docProps != null)
/*     */       {
/* 652 */         name = docProps.getProperty("dDocName");
/*     */       }
/* 654 */       if (name == null)
/*     */       {
/* 656 */         name = id;
/*     */       }
/* 658 */       String errMsg = LocaleUtils.encodeMessage("csQueueUnableToDeleteMessage", e.getMessage(), name);
/*     */ 
/* 660 */       throw new DataException(errMsg);
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void executeDocQueryWithRetry(String docName, Workspace ws, String query, DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 671 */     SystemUtils.reportDeprecatedUsage("QueueProcessor.executeDocQueryWithRetry() should not be needed, there are better way to retru a query");
/*     */ 
/* 676 */     for (int i = 0; i < 3; ++i)
/*     */     {
/* 678 */       if (i > 0)
/*     */       {
/* 680 */         SystemUtils.sleep(i * 2000);
/*     */       }
/* 682 */       if (ws.execute(query, binder) > 0L)
/*     */       {
/* 684 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 688 */     String msg = LocaleUtils.encodeMessage("csQueueUnableToExecQuery", null, query, docName);
/*     */ 
/* 690 */     throw new DataException(msg);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String[] removeDocFromConversionQueue(String docID, Properties docProps)
/*     */     throws DataException, ServiceException
/*     */   {
/* 700 */     SystemUtils.reportDeprecatedUsage("removeDocFromConversionQueue without workspace parameter is deprecated.");
/* 701 */     return null;
/*     */   }
/*     */ 
/*     */   public static String[] removeDocFromConversionQueue(Workspace ws, String docID, Properties docProps)
/*     */     throws DataException, ServiceException
/*     */   {
/* 710 */     String[] lockedIDs = null;
/* 711 */     PropParameters params = new PropParameters(docProps);
/* 712 */     ResultSet rset = ws.createResultSet("QisActiveRefineryJob", params);
/* 713 */     if (!rset.isEmpty())
/*     */     {
/* 715 */       String[] temp = { docID };
/* 716 */       lockedIDs = temp;
/*     */     }
/* 718 */     return lockedIDs;
/*     */   }
/*     */ 
/*     */   public static FileQueue loadConversionQueue(String queueType, String agentName)
/*     */     throws ServiceException
/*     */   {
/* 724 */     String docQueueDir = DirectoryLocator.getDocConversionDirectory();
/* 725 */     if ((agentName != null) && (agentName.length() > 0))
/*     */     {
/* 727 */       docQueueDir = docQueueDir + agentName + "/";
/*     */     }
/* 729 */     if (queueType == null)
/*     */     {
/* 731 */       queueType = "";
/*     */     }
/* 733 */     docQueueDir = docQueueDir + queueType + "/";
/* 734 */     FileUtils.checkOrCreateDirectory(docQueueDir, 5);
/* 735 */     return new FileQueue(docQueueDir);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static FileQueue createConversionQueue()
/*     */   {
/* 744 */     FileQueue fq = null;
/*     */     try
/*     */     {
/* 747 */       fq = loadConversionQueue("", null);
/*     */     }
/*     */     catch (ServiceException ignore)
/*     */     {
/* 751 */       if (SystemUtils.m_verbose == true)
/*     */       {
/* 753 */         Report.debug(null, null, ignore);
/*     */       }
/*     */     }
/* 756 */     return fq;
/*     */   }
/*     */ 
/*     */   protected static void computeRenditionInfo(DataBinder params, FileStoreProvider fs, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 762 */     processRenditionInfo(params, false, fs, cxt);
/*     */   }
/*     */ 
/*     */   protected static void processRenditionInfo(DataBinder params, boolean doDelete, FileStoreProvider fs, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 768 */     AdditionalRenditions renSet = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*     */ 
/* 770 */     String rends = params.getLocal("AdditionalRenditions");
/* 771 */     Vector renditions = StringUtils.parseArray(rends, ',', ',');
/*     */ 
/* 773 */     int num = renditions.size();
/* 774 */     if (num > AdditionalRenditions.m_maxNum)
/*     */     {
/* 776 */       String docName = params.getLocal("dDocName");
/* 777 */       IdcMessage msg = IdcMessageFactory.lc("csQueueTooManyRenditions", new Object[] { docName, Integer.valueOf(AdditionalRenditions.m_maxNum), Integer.valueOf(num), rends });
/* 778 */       Report.trace(null, msg.toString(), new StackTrace());
/* 779 */       return;
/*     */     }
/*     */ 
/* 782 */     int count = 1;
/* 783 */     for (int i = 0; i < num; ++count)
/*     */     {
/* 785 */       String prStep = (String)renditions.elementAt(i);
/*     */ 
/* 787 */       String[][] info = ResultSetUtils.createFilteredStringTable(renSet, new String[] { "renProductionStep", "renFlag", "renExtension" }, prStep.trim());
/*     */ 
/* 790 */       if ((info == null) || (info.length == 0))
/*     */       {
/* 792 */         String docName = params.getLocal("dDocName");
/* 793 */         IdcMessage msg = IdcMessageFactory.lc("csQueueUnableToFindRendition", new Object[] { prStep, docName });
/* 794 */         Report.trace(null, msg.toString(), new StackTrace());
/*     */       }
/* 796 */       String renFlag = info[0][0];
/* 797 */       String renExtension = info[0][1];
/* 798 */       params.putLocal("dRendition" + count, renFlag);
/* 799 */       params.putLocal("renExtension" + count, renExtension);
/*     */ 
/* 801 */       if (doDelete)
/*     */       {
/* 803 */         params.putLocal("RenditionId", "rendition:" + renFlag);
/*     */         try
/*     */         {
/* 806 */           IdcFileDescriptor descriptor = fs.createDescriptor(params, null, cxt);
/* 807 */           fs.deleteFile(descriptor, null, cxt);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 814 */           String docName = params.getLocal("dDocName");
/* 815 */           String dDocID = params.getLocal("dDocID");
/* 816 */           Report.trace(null, "Error encountered while deleting rendition " + renFlag + " for " + docName + " with dDocID=" + dDocID + ".", e);
/*     */         }
/*     */       }
/* 783 */       ++i;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 826 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97326 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.QueueProcessor
 * JD-Core Version:    0.5.4
 */