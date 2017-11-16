/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.serialize.DataBinderLocalizer;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.server.utils.DocumentInfoCacheUtils;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public abstract class IndexerBulkLoader extends IndexerStepImpl
/*     */   implements Parameters
/*     */ {
/*     */   public IndexerWorkObject m_data;
/*     */   public IndexerState m_state;
/*     */   public WebChanges m_changes;
/*     */   public Hashtable m_loadedProps;
/*     */   public FileStoreProvider m_fileStore;
/*     */   public IndexerConfig m_config;
/*     */   public boolean m_isCurFileLarge;
/*     */   public boolean m_indexingFailed;
/*     */   public boolean m_retryIndexing;
/*     */   public boolean m_isFirstRetry;
/*     */   public String m_finishedFlag;
/*     */   public String m_dbSymbol;
/*     */   public char m_change;
/*     */   public int m_count;
/*     */   public int m_maxCount;
/*     */   public int m_total;
/*     */   public boolean m_hasReleaseDocumentChange;
/*     */   public boolean m_doNotify;
/*     */ 
/*     */   public IndexerBulkLoader()
/*     */   {
/*  46 */     this.m_isCurFileLarge = false;
/*     */ 
/*  48 */     this.m_indexingFailed = false;
/*  49 */     this.m_retryIndexing = false;
/*  50 */     this.m_isFirstRetry = true;
/*     */ 
/*  52 */     this.m_finishedFlag = null;
/*     */   }
/*     */ 
/*     */   public void prepareUse(String step, IndexerWorkObject data, boolean isRestart)
/*     */     throws ServiceException
/*     */   {
/*  66 */     if (this.m_data == null)
/*     */     {
/*  69 */       this.m_data = data;
/*     */ 
/*  71 */       this.m_state = ((IndexerState)this.m_data.getCachedObject("IndexerState"));
/*  72 */       this.m_finishedFlag = this.m_state.computeFinishedSymbol("BulkLoaded");
/*     */     }
/*  74 */     this.m_dbSymbol = this.m_state.getFinishedSymbol();
/*  75 */     this.m_changes = ((WebChanges)data.getCachedObject("WebChanges"));
/*  76 */     this.m_changes.init(data);
/*     */ 
/*  81 */     this.m_loadedProps = ((Hashtable)data.getCachedObject("LoadedProps"));
/*     */     try
/*     */     {
/*  86 */       this.m_fileStore = FileStoreProviderLoader.initFileStore(data);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  90 */       throw new ServiceException("!csIndexerUnableToInitFileStore", e);
/*     */     }
/*     */ 
/*  93 */     this.m_config = ((IndexerConfig)data.getCachedObject("IndexerConfig"));
/*     */   }
/*     */ 
/*     */   public void cleanUp(IndexerWorkObject data)
/*     */     throws ServiceException
/*     */   {
/*  99 */     if (this.m_finishedFlag == null)
/*     */       return;
/* 101 */     this.m_state.freeFinishedSymbol("BulkLoaded");
/*     */   }
/*     */ 
/*     */   public String doWork(String step, IndexerWorkObject data, boolean restart)
/*     */     throws ServiceException
/*     */   {
/* 109 */     boolean foundStep = true;
/*     */ 
/* 111 */     String result = "Success";
/* 112 */     this.m_change = ' ';
/* 113 */     this.m_maxCount = this.m_data.getEnvironmentInt("MaxBatchloadSize", 0);
/* 114 */     if (this.m_maxCount == 0)
/*     */     {
/* 116 */       this.m_maxCount = this.m_data.getEnvironmentInt("IndexerCheckpointCount", 200);
/*     */     }
/* 118 */     this.m_count = 0;
/*     */ 
/* 120 */     if (step.equals("IndexDeletions"))
/*     */     {
/* 122 */       this.m_retryIndexing = false;
/* 123 */       this.m_data.m_msg = "!csIndexingDeletions";
/* 124 */       this.m_change = '-';
/*     */     }
/* 126 */     else if (step.equals("IndexAdditions"))
/*     */     {
/* 128 */       this.m_retryIndexing = false;
/* 129 */       this.m_data.m_msg = "!csIndexingAdditions";
/* 130 */       this.m_change = '+';
/*     */     }
/* 132 */     else if (step.equals("IndexRetries"))
/*     */     {
/* 134 */       this.m_data.m_msg = "!csIndexingRetries";
/* 135 */       if (this.m_retryIndexing)
/*     */       {
/* 137 */         this.m_isFirstRetry = false;
/* 138 */         result = "Success";
/* 139 */         this.m_change = '!';
/*     */       }
/*     */       else
/*     */       {
/* 143 */         this.m_retryIndexing = true;
/* 144 */         this.m_isFirstRetry = true;
/* 145 */         result = "Retry";
/* 146 */         this.m_change = '*';
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 151 */       foundStep = false;
/*     */     }
/*     */ 
/* 154 */     if (foundStep)
/*     */     {
/* 156 */       this.m_total = this.m_changes.count(this.m_change);
/* 157 */       if (this.m_total > this.m_maxCount)
/*     */       {
/* 159 */         this.m_total = this.m_maxCount;
/*     */       }
/*     */ 
/* 162 */       if (Report.m_verbose)
/*     */       {
/* 164 */         Report.debug("indexer", "IndexerBulkLoader doing '" + step + "' with m_change '" + this.m_change + "'" + "; total count: " + this.m_total + "; max count: " + this.m_maxCount, null);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 170 */         createBulkLoad(this.m_change, this.m_maxCount);
/* 171 */         this.m_changes.save();
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 175 */         throw new ServiceException(e);
/*     */       }
/*     */ 
/* 178 */       return result;
/*     */     }
/* 180 */     String extendedResult = doExtendedWork(step, data, restart);
/* 181 */     if (extendedResult != null)
/*     */     {
/* 183 */       return extendedResult;
/*     */     }
/*     */ 
/* 186 */     throw new ServiceException(LocaleUtils.encodeMessage("csIndexerUnknownStep", null, step));
/*     */   }
/*     */ 
/*     */   public String doExtendedWork(String step, IndexerWorkObject data, boolean restart)
/*     */     throws ServiceException
/*     */   {
/* 194 */     return null;
/*     */   }
/*     */ 
/*     */   public void createBulkLoad(char changeType, int max)
/*     */     throws ServiceException, DataException
/*     */   {
/* 200 */     reportProgress();
/*     */ 
/* 205 */     Properties props = new Properties(SharedObjects.getSecureEnvironment());
/* 206 */     PropParameters args = new PropParameters(props);
/* 207 */     args.m_allowDefaults = false;
/*     */ 
/* 210 */     boolean deleteFlag = changeType == '-';
/* 211 */     boolean isSingleDocIndexing = (this.m_retryIndexing) && (this.m_isFirstRetry) && (this.m_config.getBoolean("AllowSingleDocIndexing", false));
/*     */ 
/* 214 */     Object obj = this.m_changes.first(changeType);
/* 215 */     if (obj == null)
/*     */     {
/* 217 */       return;
/*     */     }
/* 219 */     props.put("dIndexerState", this.m_dbSymbol);
/* 220 */     Object dateObj = this.m_data.getLocaleResource(3);
/* 221 */     while ((change = this.m_changes.nextChangeEx(obj, "IS")) != null)
/*     */     {
/*     */       WebChange change;
/* 223 */       if (Report.m_verbose)
/*     */       {
/* 225 */         Report.debug("indexer", "WebChange retrieved. RevClassID:" + change.m_dRevClassID + "; dID:" + change.m_dID + "; Indexer State:" + change.m_dIndexerState + "; ReleaseState:" + change.m_dReleaseState, null);
/*     */       }
/*     */ 
/* 229 */       this.m_data.checkForAbort();
/*     */ 
/* 232 */       props.put("indexID", change.m_dID);
/* 233 */       String query = (deleteFlag) ? "INDEXER-QUERY-DELETE-INFO" : "INDEXER-QUERY-INSERT-INFO";
/*     */ 
/* 235 */       ResultSet rset = this.m_data.m_workspace.createResultSet(query, args);
/* 236 */       if ((rset == null) || (!rset.isRowPresent()))
/*     */       {
/* 239 */         this.m_changes.deleteChange(change);
/* 240 */         Report.trace("indexer", "skipping webchange in this iteration for " + change.m_dID, null);
/*     */       }
/*     */ 
/* 244 */       rset.setDateFormat(LocaleResources.m_bulkloadFormat);
/* 245 */       this.m_data.m_currentResultSet = new DataResultSet();
/* 246 */       this.m_data.m_currentResultSet.setDateFormat(LocaleResources.m_bulkloadFormat);
/* 247 */       this.m_data.m_currentResultSet.copy(rset);
/*     */ 
/* 251 */       rset.closeInternals();
/*     */ 
/* 254 */       IndexerInfo info = new IndexerInfo();
/* 255 */       DataBinder webChangeFilterParams = new DataBinder();
/* 256 */       webChangeFilterParams.m_blDateFormat = LocaleResources.m_bulkloadFormat;
/* 257 */       webChangeFilterParams.addResultSet("DOC_INFO", this.m_data.m_currentResultSet);
/* 258 */       if (deleteFlag)
/*     */       {
/* 260 */         webChangeFilterParams.putLocal("isDelete", "1");
/*     */       }
/* 262 */       if (this.m_retryIndexing)
/*     */       {
/* 264 */         webChangeFilterParams.putLocal("isRetryIndexing", "1");
/*     */       }
/* 266 */       this.m_data.setCachedObject("WebChangeFilterParams", webChangeFilterParams);
/* 267 */       this.m_data.setCachedObject("FilterIndexerInfo", info);
/* 268 */       this.m_data.setCachedObject("IndexerBulkLoader", this);
/* 269 */       this.m_data.setCachedObject("WebChange", change);
/* 270 */       this.m_data.setCachedObject("DemotedRevisionInfo", "");
/* 271 */       this.m_data.setCachedObject("UserDateFormat", webChangeFilterParams.m_blDateFormat);
/* 272 */       FileStoreProviderLoader.prepareAndClearContext(this.m_fileStore, this.m_data);
/*     */ 
/* 274 */       Properties pairs = null;
/* 275 */       boolean isSuccess = false;
/* 276 */       boolean doIndexing = true;
/* 277 */       int retVal = PluginFilters.filter("beforeLoadRecordWebChange", this.m_data.m_workspace, webChangeFilterParams, this.m_data);
/*     */ 
/* 282 */       IdcDateFormat rsFormat = this.m_data.m_currentResultSet.getDateFormat();
/* 283 */       if (!rsFormat.equals(LocaleResources.m_bulkloadFormat))
/*     */       {
/* 285 */         Report.trace("indexer", "createBulkload -- filter event changed date format to " + rsFormat.toString() + " forcing back to bulkload format", null);
/*     */ 
/* 287 */         DataBinderLocalizer localizer = new DataBinderLocalizer(webChangeFilterParams, this.m_data);
/* 288 */         DataResultSet newRset = localizer.coerceResultSet(this.m_data.m_currentResultSet, LocaleResources.m_bulkloadFormat, 1);
/*     */ 
/* 290 */         if (newRset != null)
/*     */         {
/* 292 */           this.m_data.m_currentResultSet = newRset;
/*     */         }
/*     */       }
/* 295 */       webChangeFilterParams.m_blDateFormat = LocaleResources.m_bulkloadFormat;
/*     */ 
/* 298 */       switch (retVal)
/*     */       {
/*     */       case -1:
/* 301 */         break;
/*     */       case 1:
/* 304 */         String beforeLoadRecordWebChangeSuccess = (String)this.m_data.getCachedObject("beforeLoadRecordWebChangeSuccess");
/*     */ 
/* 306 */         pairs = (Properties)this.m_data.getCachedObject("beforeLoadRecordWebChangePairs");
/*     */ 
/* 308 */         if (beforeLoadRecordWebChangeSuccess != null)
/*     */         {
/* 310 */           isSuccess = StringUtils.convertToBool((String)this.m_data.getCachedObject("beforeLoadRecordWebChangeSuccess"), false);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 320 */         if (retVal == 0)
/*     */         {
/* 322 */           pairs = loadRecordWebChange(deleteFlag, info, change);
/* 323 */           if ((pairs == null) || (pairs.size() == 0))
/*     */           {
/* 325 */             this.m_changes.setChangeType(change, "F");
/*     */           }
/*     */           else
/*     */           {
/* 329 */             String skip = pairs.getProperty("SkipRevision");
/* 330 */             if (skip != null)
/*     */             {
/* 332 */               webChangeFilterParams.putLocal("SkipRevision", skip);
/*     */             }
/* 334 */             isSuccess = true;
/*     */           }
/*     */         }
/*     */       }
/*     */       finally
/*     */       {
/*     */         String skip;
/* 340 */         if (isSuccess)
/*     */         {
/* 343 */           this.m_data.setCachedObject("Pairs", pairs);
/* 344 */           webChangeFilterParams.putLocal("isSuccess", "1");
/*     */         }
/* 346 */         if (PluginFilters.filter("afterLoadRecordWebChange", this.m_data.m_workspace, webChangeFilterParams, this.m_data) != 0)
/*     */         {
/* 350 */           doIndexing = false;
/*     */         }
/* 352 */         String skip = webChangeFilterParams.getLocal("SkipRevision");
/* 353 */         if (StringUtils.convertToBool(skip, false))
/*     */         {
/* 355 */           doIndexing = false;
/*     */         }
/*     */       }
/* 358 */       if (!isSuccess) continue; if (!doIndexing)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 365 */       this.m_count += 1;
/* 366 */       this.m_data.m_indexer.indexDocument(pairs, info);
/* 367 */       if (isSingleDocIndexing)
/*     */       {
/* 369 */         this.m_data.m_indexer.finishIndexing(false);
/*     */       }
/*     */ 
/* 372 */       Vector finished = this.m_data.m_indexer.computeFinishedDocList();
/* 373 */       handleIndexerResults(finished);
/*     */ 
/* 376 */       if (this.m_count == max) {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 382 */     this.m_data.m_indexer.finishIndexing(false);
/* 383 */     Vector finished = this.m_data.m_indexer.computeFinishedDocList();
/* 384 */     handleIndexerResults(finished);
/*     */ 
/* 387 */     this.m_data.releaseConnection(false);
/* 388 */     this.m_data.setCachedObject("UserDateFormat", dateObj);
/* 389 */     reportProgress();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected Properties loadRecord(boolean deleteFlag, IndexerInfo ii)
/*     */     throws DataException, ServiceException
/*     */   {
/* 398 */     Report.trace(null, "Obsolete call to loadRecord(...)", null);
/* 399 */     String revClassID = get("dRevClassID");
/* 400 */     WebChange change = this.m_changes.findNoCheckStatus(revClassID);
/* 401 */     return loadRecordWebChange(deleteFlag, ii, change);
/*     */   }
/*     */ 
/*     */   protected abstract Properties loadRecordWebChange(boolean paramBoolean, IndexerInfo paramIndexerInfo, WebChange paramWebChange)
/*     */     throws DataException, ServiceException;
/*     */ 
/*     */   public void handleIndexerResults(Vector results)
/*     */     throws ServiceException
/*     */   {
/* 413 */     int aSize = results.size();
/*     */ 
/* 416 */     if (aSize == 0)
/*     */     {
/* 418 */       return;
/*     */     }
/*     */ 
/* 421 */     boolean hasError = false;
/* 422 */     int metaOnlyCount = 0;
/* 423 */     for (int i = 0; i < aSize; ++i)
/*     */     {
/*     */       try
/*     */       {
/* 427 */         IndexerInfo ii = (IndexerInfo)results.elementAt(i);
/* 428 */         WebChange change = this.m_changes.find(ii.m_dRevClassID);
/*     */ 
/* 431 */         this.m_data.setCachedObject("IndexerInfo", ii);
/* 432 */         this.m_data.setCachedObject("WebChange", change);
/* 433 */         if (PluginFilters.filter("handleIndexerResult", this.m_data.m_workspace, this.m_state.m_state, this.m_data) == 0)
/*     */         {
/* 438 */           Properties props = new Properties();
/* 439 */           PropParameters args = new PropParameters(props);
/* 440 */           props.put("dID", ii.m_dID);
/* 441 */           if (ii.m_indexStatus != 0)
/*     */           {
/* 443 */             hasError = true;
/*     */ 
/* 445 */             String newChangeType = "*";
/* 446 */             if ((ii.m_processedAlone) || (!ii.m_indexWebFile))
/*     */             {
/* 451 */               newChangeType = "!";
/*     */             }
/* 453 */             this.m_changes.setChangeType(change, newChangeType);
/*     */ 
/* 455 */             if ((ii.m_isMetaDataOnly) || (!ii.m_indexWebFile))
/*     */             {
/* 457 */               ++metaOnlyCount;
/*     */             }
/*     */ 
/* 462 */             if (ii.m_processedAlone)
/*     */             {
/* 465 */               if (ii.m_isMetaDataOnly)
/*     */               {
/* 472 */                 resetDocumentToGenWWW(ii.m_dID, "!csIndexerAbortedMsg3", change);
/*     */               }
/*     */               else
/*     */               {
/* 477 */                 String query = "UrevisionMetaDataOnly";
/*     */                 String msg;
/* 478 */                 switch (ii.m_indexStatus)
/*     */                 {
/*     */                 case 1:
/* 481 */                   msg = "!csIndexerSkipped";
/* 482 */                   break;
/*     */                 case 2:
/* 484 */                   msg = "!csIndexerTimeoutFullTextFailed";
/* 485 */                   break;
/*     */                 case 3:
/*     */                 default:
/* 488 */                   msg = "!csIndexerFailure";
/*     */                 }
/*     */ 
/* 491 */                 props.put("dMessage", msg);
/* 492 */                 this.m_data.m_workspace.execute(query, args);
/*     */               }
/*     */ 
/*     */             }
/* 495 */             else if (!ii.m_isMetaDataOnly);
/*     */           }
/*     */           else
/*     */           {
/* 503 */             String newChangeType = (change.m_change == '-') ? "D" : "I";
/* 504 */             this.m_changes.setChangeType(change, newChangeType);
/*     */ 
/* 506 */             if (ii.m_isDelete)
/*     */             {
/* 508 */               this.m_state.m_totalDeleteIndex += 1;
/* 509 */               this.m_state.m_cumTotalDeleteIndex += 1;
/*     */             }
/*     */             else
/*     */             {
/* 513 */               this.m_state.m_totalAddIndex += 1;
/* 514 */               this.m_state.m_cumTotalAddIndex += 1;
/* 515 */               if (ii.m_indexWebFile)
/*     */               {
/* 517 */                 this.m_state.m_totalFullTextAdd += 1;
/* 518 */                 this.m_state.m_cumTotalFullTextAdd += 1;
/*     */               }
/*     */               else
/*     */               {
/* 522 */                 this.m_state.m_totalDummyTextAdd += 1;
/* 523 */                 this.m_state.m_cumTotalDummyTextAdd += 1;
/*     */               }
/*     */             }
/* 526 */             if (ii.m_indexError != null)
/*     */             {
/* 528 */               String query = "UrevisionMetaDataOnly";
/* 529 */               props.put("dMessage", ii.m_indexError);
/* 530 */               this.m_data.m_workspace.execute(query, args);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */       catch (Exception e) {
/* 536 */         throw new ServiceException("!csIndexerGeneralFailure", e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 541 */     if ((results.size() > 0) && 
/* 543 */       (!this.m_data.m_debugLevel.equalsIgnoreCase("all")) && (!this.m_data.m_debugLevel.equalsIgnoreCase("trace")))
/*     */     {
/* 546 */       String exportTempDir = DirectoryLocator.getSystemBaseDirectory("binary") + "search/bulkload/~export/";
/*     */ 
/* 548 */       String activeCollectionID = this.m_config.getConfigValue("IndexerActiveCollectionID");
/* 549 */       if ((activeCollectionID != null) && (activeCollectionID.length() > 0))
/*     */       {
/* 551 */         exportTempDir = DirectoryLocator.getSystemBaseDirectory("binary") + "search/" + activeCollectionID + "/bulkload/~export/";
/*     */       }
/*     */ 
/* 555 */       File file = new File(exportTempDir);
/* 556 */       FileUtils.deleteDirectory(file, false);
/*     */ 
/* 559 */       String manifestDir = LegacyDirectoryLocator.getVaultTempDirectory() + "textexport/";
/* 560 */       File manifestFile = new File(manifestDir);
/* 561 */       FileUtils.deleteDirectory(manifestFile, false);
/*     */     }
/*     */ 
/* 565 */     if (SystemUtils.m_verbose)
/*     */     {
/* 567 */       Report.debug("indexer", "after handling indexing results: hasError: " + hasError + " m_retryIndexing: " + this.m_retryIndexing + " m_isFirstRetry: " + this.m_isFirstRetry + " metaOnlyCount: " + metaOnlyCount + " aSize: " + aSize, null);
/*     */     }
/*     */ 
/* 571 */     if (((hasError) && (this.m_retryIndexing) && (!this.m_isFirstRetry)) || (metaOnlyCount == aSize))
/*     */     {
/* 574 */       for (int i = 0; i < aSize; ++i)
/*     */       {
/* 576 */         IndexerInfo ii = (IndexerInfo)results.elementAt(i);
/* 577 */         Properties props = new Properties();
/* 578 */         PropParameters args = new PropParameters(props);
/* 579 */         props.put("dID", ii.m_dID);
/* 580 */         props.put("dRevClassID", ii.m_dRevClassID);
/* 581 */         props.put("dIndexerState", " ");
/*     */         try
/*     */         {
/* 589 */           this.m_data.m_workspace.execute("UindexerStateRevClass", args);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 593 */           Report.trace("indexer", null, e);
/* 594 */           String msg = LocaleUtils.encodeMessage("csIndexerCriticalErrorWhileAborting", null, ii.m_dID);
/*     */ 
/* 596 */           Report.error(null, msg, e);
/*     */         }
/*     */       }
/* 599 */       String msg = "!csIndexerMetaOnlyFailed";
/* 600 */       Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/* 601 */       throw new ServiceException(msg);
/*     */     }
/* 603 */     if (this.m_doNotify)
/*     */     {
/* 605 */       notifyReleasedDocumentsChanged();
/*     */     }
/*     */     else
/*     */     {
/* 609 */       this.m_hasReleaseDocumentChange = true;
/*     */     }
/* 611 */     reportProgress();
/*     */   }
/*     */ 
/*     */   public String getValue(String name, boolean allowMissing)
/*     */     throws DataException
/*     */   {
/* 617 */     if (name.equals("dIndexerState"))
/*     */     {
/* 619 */       return this.m_finishedFlag;
/*     */     }
/*     */ 
/* 622 */     FieldInfo info = new FieldInfo();
/* 623 */     if (this.m_data.m_currentResultSet.getFieldInfo(name, info) == true)
/*     */     {
/* 625 */       String value = this.m_data.m_currentResultSet.getStringValue(info.m_index);
/*     */ 
/* 627 */       if (value == null)
/*     */       {
/* 629 */         return "";
/*     */       }
/*     */ 
/* 632 */       return value;
/*     */     }
/*     */ 
/* 635 */     if (allowMissing)
/*     */     {
/* 637 */       return null;
/*     */     }
/*     */ 
/* 640 */     throw new DataException(LocaleUtils.encodeMessage("csIndexerUnableToFind", null, name));
/*     */   }
/*     */ 
/*     */   public String get(String name)
/*     */     throws DataException
/*     */   {
/* 646 */     return getValue(name, false);
/*     */   }
/*     */ 
/*     */   public String getSystem(String name) throws DataException
/*     */   {
/* 651 */     return getValue(name, true);
/*     */   }
/*     */ 
/*     */   public void computeAndSetVaultFileSize(Properties pairs)
/*     */     throws DataException, ServiceException
/*     */   {
/* 658 */     String vaultFileSize = pairs.getProperty("VaultFileSize");
/* 659 */     if ((vaultFileSize != null) && (vaultFileSize.length() > 0))
/*     */     {
/* 661 */       if (SystemUtils.m_verbose)
/*     */       {
/* 663 */         Report.debug("filestore", "VaultFileSize already set to " + vaultFileSize, null);
/*     */       }
/*     */ 
/* 666 */       return;
/*     */     }
/* 668 */     vaultFileSize = getSystem("dFileSize");
/*     */ 
/* 670 */     if ((vaultFileSize != null) && (vaultFileSize.length() > 0))
/*     */     {
/* 672 */       pairs.put("VaultFileSize", vaultFileSize);
/* 673 */       if (SystemUtils.m_verbose)
/*     */       {
/* 675 */         Report.debug("filestore", "found vault file size " + vaultFileSize, null);
/*     */       }
/*     */ 
/* 678 */       return;
/*     */     }
/*     */ 
/* 682 */     DataBinder binder = new DataBinder();
/* 683 */     binder.setLocalData(pairs);
/* 684 */     binder.addResultSet("DocInfo", this.m_data.m_currentResultSet);
/*     */ 
/* 686 */     Properties localData = new Properties();
/* 687 */     PropParameters params = new PropParameters(localData, binder);
/* 688 */     localData.put("RenditionId", "primaryFile");
/*     */ 
/* 690 */     IdcFileDescriptor vaultFile = this.m_fileStore.createDescriptor(params, null, this.m_data);
/*     */     try
/*     */     {
/* 693 */       Map storageData = this.m_fileStore.getStorageData(vaultFile, null, this.m_data);
/* 694 */       String fileSize = (String)storageData.get("fileSize");
/* 695 */       pairs.put("VaultFileSize", fileSize);
/* 696 */       localData.put("dFileSize", fileSize);
/* 697 */       this.m_data.m_workspace.execute("UdocumentFileSize", params);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 701 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected void resetDocument(String id, String message)
/*     */   {
/* 714 */     Report.trace(null, "Obsolete call to resetDocument(...)", null);
/* 715 */     resetDocumentToGenWWW(id, message, null);
/*     */   }
/*     */ 
/*     */   public void resetDocumentToGenWWW(String id, String message, WebChange change)
/*     */   {
/* 720 */     Properties props = new Properties();
/* 721 */     props.put("dID", id);
/* 722 */     if (message.length() > 255)
/*     */     {
/* 724 */       message = message.substring(0, 252) + "...";
/*     */     }
/* 726 */     props.put("dMessage", message);
/* 727 */     String newState = "N";
/*     */ 
/* 729 */     if ((change != null) && ((
/* 731 */       (change.m_dReleaseState == 'Y') || (change.m_dReleaseState == 'I') || (change.m_dReleaseState == 'U'))))
/*     */     {
/* 734 */       newState = "U";
/*     */     }
/*     */ 
/* 737 */     props.put("dReleaseState", newState);
/*     */     try
/*     */     {
/* 741 */       Parameters args = new PropParameters(props);
/* 742 */       this.m_data.m_workspace.execute("UrevisionIndexFailed", args);
/* 743 */       if (change != null)
/*     */       {
/* 745 */         this.m_changes.setChangeTypeNoCheckStatus(change, "F");
/*     */       }
/*     */     }
/*     */     catch (DataException ignore)
/*     */     {
/* 750 */       String msg = LocaleUtils.encodeMessage("csIndexerUnableToMarkFailure", ignore.getMessage(), id);
/*     */ 
/* 752 */       Report.trace(null, LocaleResources.localizeMessage(msg, null), ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void renameFileToLabel(String id)
/*     */     throws DataException, ServiceException
/*     */   {
/* 762 */     DataBinder binder = new DataBinder();
/* 763 */     binder.putLocal("dID", id);
/* 764 */     ResultSet rs = this.m_data.m_workspace.createResultSet("QdocID", binder);
/* 765 */     if ((rs == null) || (rs.isEmpty()))
/*     */     {
/* 767 */       return;
/*     */     }
/* 769 */     DataResultSet drset = new DataResultSet();
/* 770 */     drset.copy(rs);
/* 771 */     binder.addResultSet("RENAME_FILE", drset);
/*     */ 
/* 775 */     this.m_data.setCachedObject("DemotedRevisionInfo", drset);
/*     */ 
/* 777 */     String docName = binder.get("dDocName");
/* 778 */     String revLabel = binder.get("dRevLabel");
/* 779 */     String extension = binder.get("dWebExtension");
/* 780 */     String status = binder.get("dStatus");
/* 781 */     String webDocumentDir = LegacyDirectoryLocator.computeWebPathDir(binder);
/*     */     try
/*     */     {
/* 785 */       String docPathStart = docName;
/* 786 */       String oldNameStr = docPathStart;
/* 787 */       String newNameStr = docPathStart + "~" + revLabel;
/*     */ 
/* 789 */       if ((extension != null) && (extension.length() > 0))
/*     */       {
/* 791 */         oldNameStr = oldNameStr + "." + extension;
/* 792 */         newNameStr = newNameStr + "." + extension;
/*     */       }
/* 794 */       oldNameStr = oldNameStr.toLowerCase();
/* 795 */       newNameStr = newNameStr.toLowerCase();
/* 796 */       oldNameStr = webDocumentDir + oldNameStr;
/* 797 */       newNameStr = webDocumentDir + newNameStr;
/* 798 */       File oldName = new File(oldNameStr);
/* 799 */       File newName = new File(newNameStr);
/* 800 */       boolean oldNameExists = oldName.exists();
/* 801 */       if (SystemUtils.m_verbose)
/*     */       {
/* 803 */         if (oldNameExists)
/*     */         {
/* 805 */           Report.debug("indexer", "Demoting current release webviewable to path " + newNameStr, null);
/*     */         }
/*     */         else
/*     */         {
/* 809 */           Report.debug("indexer", "We are assuming path " + newNameStr + " is already demoted", null);
/*     */         }
/*     */       }
/* 812 */       if (oldNameExists)
/*     */       {
/* 814 */         if (!newName.exists())
/*     */         {
/* 816 */           oldName.renameTo(newName);
/* 817 */           if (!newName.exists())
/*     */           {
/* 819 */             String msg = LocaleUtils.encodeMessage("csIndexerRenameDisappeared4", null, oldName.getAbsolutePath(), newName.getAbsolutePath());
/*     */ 
/* 821 */             Report.error("indexer", msg, null);
/* 822 */             Report.trace("indexer", LocaleResources.localizeMessage(msg, this.m_data), null);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 827 */           Report.trace("indexer", "Stopped demotion to " + newNameStr + " because file is already present", null);
/*     */         }
/*     */ 
/*     */       }
/* 834 */       else if ((!newName.exists()) && (!status.equals("DELETED")))
/*     */       {
/* 836 */         String msg = LocaleUtils.encodeMessage("csIndexerRenameTargetMissing", null, newName.getAbsolutePath());
/*     */ 
/* 838 */         Report.error("indexer", msg, null);
/* 839 */         Report.trace("indexer", LocaleResources.localizeMessage(msg, this.m_data), null);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 845 */       ServiceException e = new ServiceException(LocaleUtils.encodeMessage("csIndexerRenameError", null, docName), t);
/*     */ 
/* 847 */       String msg = e.getMessage();
/* 848 */       Report.error("indexer", msg, t);
/* 849 */       Report.trace("indexer", LocaleResources.localizeMessage(msg, this.m_data), null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void notifyReleasedDocumentsChanged()
/*     */     throws ServiceException
/*     */   {
/* 863 */     DocumentInfoCacheUtils.synchronizeSharedTimestamp(DocumentInfoCacheUtils.F_WRITE_SYNCH);
/*     */ 
/* 865 */     SubjectManager.notifyChanged("releaseddocuments");
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected void putUpdatedToReleased(Parameters params)
/*     */     throws DataException
/*     */   {
/* 874 */     this.m_data.m_workspace.execute("UindexerState", params);
/*     */   }
/*     */ 
/*     */   public void reportProgress()
/*     */   {
/* 879 */     this.m_data.reportProgress(0, this.m_data.m_msg, this.m_count, this.m_total);
/*     */   }
/*     */ 
/*     */   public void prepareIndexingEngineValues(Properties props, String indexKey, String filePath, String URL)
/*     */   {
/*     */   }
/*     */ 
/*     */   public String getIndexKey(Properties props)
/*     */   {
/* 890 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 895 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104409 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerBulkLoader
 * JD-Core Version:    0.5.4
 */