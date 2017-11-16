/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.InputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class IndexerWorkObject extends ExecutionContextAdaptor
/*     */   implements ReportProgress
/*     */ {
/*     */   public static final int STANDARD_INIT = 0;
/*     */   public static final int FORCE_RELOAD_DESIGN = 1;
/*     */   public static final int READ_STATE_ONLY = 2;
/*     */   public IndexerCollectionData m_collectionDef;
/*  48 */   public String m_cycleId = null;
/*     */   protected IndexerState m_state;
/*     */   public Workspace m_workspace;
/*     */   public DocIndexerHandler m_indexer;
/*     */   public IndexerCollectionManager m_indexCollectionManager;
/*     */   public IndexerDriver m_driver;
/*     */   public IndexerConfig m_config;
/*     */   public String m_httpIntradocCgiRoot;
/*     */   public String m_sharedDir;
/*     */   public String m_searchDir;
/*     */   public String m_logFileDir;
/*     */   public int m_maxCollectionSize;
/*     */   public String m_debugLevel;
/*  81 */   protected int m_updateQueryBatchSize = 100;
/*     */ 
/*     */   @Deprecated
/*  87 */   public ReportProgress m_progress = null;
/*     */   protected ReportProgress m_externalProgress;
/*  95 */   boolean m_fileSystemDebug = false;
/*     */ 
/*  99 */   public boolean m_isRemoteCollection = false;
/*     */ 
/* 103 */   public DataResultSet m_currentResultSet = null;
/*     */   public String m_msg;
/*     */   public boolean m_isAbort;
/* 109 */   public int m_maxErrors = 50;
/* 110 */   public int m_maxIndexErrors = 3;
/*     */ 
/* 113 */   public boolean m_updateStyleFile = false;
/*     */ 
/* 116 */   public String m_killProcessIdPath = null;
/*     */ 
/*     */   public IndexerWorkObject(Object controllingObject)
/*     */   {
/* 120 */     setControllingObject(this);
/*     */   }
/*     */ 
/*     */   public String init(String cycleId, String restartId, Workspace ws, ReportProgress progress)
/*     */     throws DataException, ServiceException
/*     */   {
/* 126 */     return initEx(cycleId, restartId, 0, new HashMap(), ws, progress);
/*     */   }
/*     */ 
/*     */   public String initEx(String cycleId, String restartId, int loadFlags, Map initParams, Workspace ws, ReportProgress progress)
/*     */     throws DataException, ServiceException
/*     */   {
/* 136 */     this.m_progress = this;
/*     */ 
/* 139 */     this.m_state = new IndexerState();
/* 140 */     boolean isReadOnly = (loadFlags & 0x2) != 0;
/* 141 */     this.m_cycleId = this.m_state.loadStateData(cycleId, restartId, isReadOnly, this);
/*     */ 
/* 143 */     setCachedObject("IndexerState", this.m_state);
/*     */ 
/* 145 */     String engineName = SearchIndexerUtils.getSearchEngineName(this);
/* 146 */     boolean isForceReloadDesign = (loadFlags & 0x1) != 0;
/* 147 */     if ((this.m_state.m_isRebuild) || (isForceReloadDesign))
/*     */     {
/* 149 */       this.m_collectionDef = SearchLoader.retrieveIndexDesignWithCycleId(engineName, this.m_cycleId);
/*     */     }
/*     */     else
/*     */     {
/* 153 */       this.m_collectionDef = SearchLoader.getCurrentIndexDesign(engineName);
/*     */     }
/*     */ 
/* 156 */     this.m_externalProgress = progress;
/*     */ 
/* 158 */     this.m_killProcessIdPath = getEnvironmentValue("KillProcessIdExePath");
/*     */ 
/* 160 */     this.m_updateQueryBatchSize = getEnvironmentInt("PreferredUpdateQueryBatchSize", 100);
/*     */ 
/* 163 */     this.m_config = SearchIndexerUtils.getIndexerConfig(this, this.m_cycleId);
/*     */ 
/* 165 */     this.m_workspace = ws;
/* 166 */     setCachedObject("Workspace", ws);
/*     */ 
/* 168 */     for (Iterator i$ = initParams.keySet().iterator(); i$.hasNext(); ) { Object key = i$.next();
/*     */ 
/* 170 */       String value = (String)initParams.get(key);
/* 171 */       this.m_state.setStateValue((String)key, value); }
/*     */ 
/* 173 */     this.m_sharedDir = DirectoryLocator.getSharedDirectory();
/* 174 */     this.m_searchDir = DirectoryLocator.getSearchDirectory();
/*     */ 
/* 176 */     setRemoteCollection(getEnvValueAsBoolean("RemoteSearch", false));
/*     */ 
/* 178 */     this.m_currentResultSet = null;
/* 179 */     this.m_msg = "";
/* 180 */     this.m_isAbort = false;
/*     */ 
/* 182 */     return this.m_cycleId;
/*     */   }
/*     */ 
/*     */   public void initForIndexEngine()
/*     */     throws ServiceException
/*     */   {
/* 190 */     this.m_logFileDir = getEnvironmentValue("SearchLogFileDir");
/* 191 */     if (this.m_logFileDir == null)
/*     */     {
/* 193 */       this.m_logFileDir = (DirectoryLocator.getLogDirectory() + "verity/");
/*     */     }
/*     */ 
/* 196 */     this.m_debugLevel = getEnvironmentValue("SearchDebugLevel");
/*     */ 
/* 198 */     this.m_maxCollectionSize = getEnvironmentInt("MaxCollectionSize", 25);
/* 199 */     this.m_maxErrors = getEnvironmentInt("MaxDocIndexErrors", 50);
/* 200 */     this.m_maxIndexErrors = getEnvironmentInt("MaxExecutionIndexErrors", 3);
/*     */ 
/* 202 */     this.m_indexer = ((DocIndexerHandler)ComponentClassFactory.createClassInstance("DocIndexHandler", "intradoc.indexer.DocIndexerAdaptor", LocaleUtils.encodeMessage("csUnableToCreateObject", null, "DocIndexHandler")));
/*     */ 
/* 205 */     this.m_driver = ((IndexerDriver)ComponentClassFactory.createClassInstance("IndexerDriver", "intradoc.indexer.IndexerDriverAdaptor", LocaleUtils.encodeMessage("csUnableToCreateObject", null, "IndexerDriver")));
/*     */ 
/* 208 */     this.m_indexer.init(this);
/* 209 */     this.m_driver.init(this);
/*     */ 
/* 211 */     this.m_httpIntradocCgiRoot = DirectoryLocator.getIntradocCgiRoot(isRemoteCollection());
/*     */   }
/*     */ 
/*     */   public String getEnvironmentValue(String key)
/*     */   {
/* 217 */     String value = this.m_state.m_perBatchOverrides.getProperty(key);
/* 218 */     if (value == null)
/*     */     {
/* 220 */       value = SharedObjects.getEnvironmentValue(key);
/*     */     }
/* 222 */     return value;
/*     */   }
/*     */ 
/*     */   public boolean getEnvValueAsBoolean(String key, boolean defValue)
/*     */   {
/* 227 */     String value = getEnvironmentValue(key);
/* 228 */     return StringUtils.convertToBool(value, defValue);
/*     */   }
/*     */ 
/*     */   public int getEnvironmentInt(String key, int defValue)
/*     */   {
/* 233 */     String str = getEnvironmentValue(key);
/* 234 */     int value = defValue;
/*     */     try
/*     */     {
/* 237 */       if ((str != null) && (str.length() > 0))
/*     */       {
/* 239 */         value = Integer.parseInt(str);
/*     */       }
/*     */     }
/*     */     catch (NumberFormatException e)
/*     */     {
/* 244 */       String msg = LocaleUtils.encodeMessage("csIndexerConfigValueError", e.getMessage(), key);
/*     */ 
/* 246 */       Report.trace(null, LocaleResources.localizeMessage(msg, this), e);
/*     */     }
/* 248 */     return value;
/*     */   }
/*     */ 
/*     */   public void putEnvironmentValue(String key, String value)
/*     */   {
/* 253 */     this.m_state.m_overrideProps.put(key, value);
/*     */   }
/*     */ 
/*     */   public void putPerBatchProperty(String key, String value)
/*     */   {
/* 258 */     this.m_state.m_perBatchOverrides.put(key, value);
/*     */   }
/*     */ 
/*     */   public boolean isRestart()
/*     */   {
/* 263 */     return this.m_state.m_isRestart;
/*     */   }
/*     */ 
/*     */   public boolean isRebuild()
/*     */   {
/* 268 */     return this.m_state.m_isRebuild;
/*     */   }
/*     */ 
/*     */   public Object getReturnValue()
/*     */   {
/* 276 */     String msg = LocaleUtils.encodeMessage("csIndexerIllegalMethod", null, "getReturnValue()");
/*     */ 
/* 278 */     Report.trace(null, LocaleResources.localizeMessage(msg, this), null);
/* 279 */     return null;
/*     */   }
/*     */ 
/*     */   public void setReturnValue(Object retVal)
/*     */   {
/* 285 */     String msg = LocaleUtils.encodeMessage("csIndexerIllegalMethod", null, "setReturnValue()");
/*     */ 
/* 287 */     Report.trace(null, LocaleResources.localizeMessage(msg, this), null);
/*     */   }
/*     */ 
/*     */   public void setRemoteCollection(boolean isRemote)
/*     */   {
/* 293 */     this.m_isRemoteCollection = isRemote;
/*     */   }
/*     */ 
/*     */   public boolean isRemoteCollection()
/*     */   {
/* 298 */     return this.m_isRemoteCollection;
/*     */   }
/*     */ 
/*     */   public void setFileSystemDebug(boolean flag)
/*     */   {
/* 303 */     this.m_fileSystemDebug = flag;
/*     */   }
/*     */ 
/*     */   public boolean isFileSystemDebug()
/*     */   {
/* 308 */     return this.m_fileSystemDebug;
/*     */   }
/*     */ 
/*     */   public void clearRebuildFlags()
/*     */     throws DataException, ServiceException
/*     */   {
/* 315 */     DataBinder binder = new DataBinder();
/* 316 */     binder.putLocal("newIndexerState", " ");
/* 317 */     binder.putLocal("dIndexerState1", "X");
/* 318 */     binder.putLocal("dIndexerState2", "Y");
/* 319 */     ResultSet rset = this.m_workspace.createResultSet("INDEXER-MIN-MAX-COUNT-FINISHED", binder);
/* 320 */     this.m_msg = "!csClearingRebuildFlag";
/* 321 */     reportProgress(0, this.m_msg, -1.0F, -1.0F);
/* 322 */     if (!rset.isRowPresent())
/*     */       return;
/* 324 */     String min = ResultSetUtils.getValue(rset, "minID");
/* 325 */     String max = ResultSetUtils.getValue(rset, "maxID");
/* 326 */     String count = ResultSetUtils.getValue(rset, "theCount");
/*     */ 
/* 328 */     int countInt = Integer.parseInt(count);
/* 329 */     if (countInt <= 0)
/*     */       return;
/* 331 */     int minInt = Integer.parseInt(min);
/* 332 */     int maxInt = Integer.parseInt(max);
/* 333 */     int theMinInt = minInt;
/*     */ 
/* 335 */     while (minInt <= maxInt)
/*     */     {
/* 337 */       reportProgress(0, this.m_msg, minInt - theMinInt, maxInt - theMinInt + 1);
/*     */ 
/* 339 */       int midInt = minInt + 100;
/* 340 */       binder.putLocal("minID", "" + minInt);
/* 341 */       binder.putLocal("maxID", "" + midInt);
/* 342 */       this.m_workspace.execute("UindexerStateRange", binder);
/* 343 */       minInt = midInt;
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void checkForAbort()
/*     */     throws ServiceException
/*     */   {
/* 351 */     if (!this.m_isAbort) {
/*     */       return;
/*     */     }
/* 354 */     WebChanges changes = (WebChanges)getCachedObject("WebChanges");
/* 355 */     if (changes != null)
/*     */     {
/* 357 */       changes.destroy();
/*     */     }
/* 359 */     String flag = this.m_state.getFinishedSymbol();
/* 360 */     DataBinder binder = new DataBinder();
/* 361 */     binder.putLocal("oldIndexerState", flag);
/* 362 */     binder.putLocal("newIndexerState", " ");
/* 363 */     Exception exception = null;
/*     */     try
/*     */     {
/* 366 */       this.m_workspace.execute("UallIndexerState", binder);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 370 */       exception = e;
/*     */     }
/*     */     try
/*     */     {
/* 374 */       if (this.m_state.isRebuild())
/*     */       {
/* 376 */         clearRebuildFlags();
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 381 */       exception = e;
/*     */     }
/*     */ 
/* 384 */     this.m_state.doStateTransition("Finished");
/* 385 */     if (exception != null)
/*     */     {
/* 387 */       throw new ServiceException("!csIndexerCancelCleanupError", exception);
/*     */     }
/* 389 */     String msg = LocaleUtils.encodeMessage("csIndexerCancelled", null, new Date());
/* 390 */     throw new ServiceException(-65, msg);
/*     */   }
/*     */ 
/*     */   public void executeDependentQueries(String feederQuery, String query, Properties extraProps, String statusMsg)
/*     */     throws DataException, ServiceException
/*     */   {
/* 397 */     executeDependentQueriesEx(feederQuery, new String[] { query }, extraProps, statusMsg);
/*     */   }
/*     */ 
/*     */   public void executeDependentQueriesEx(String feederQuery, String[] queries, Properties extraProps, String statusMsg)
/*     */     throws DataException, ServiceException
/*     */   {
/* 403 */     executeDependentQueriesAllowBatch(feederQuery, queries, true, true, extraProps, statusMsg);
/*     */   }
/*     */ 
/*     */   public void executeDependentQueriesAllowBatch(String feederQuery, String[] queries, boolean isBatchable, boolean updatedRowInResult, Properties extraProps, String statusMsg)
/*     */     throws DataException, ServiceException
/*     */   {
/* 410 */     DataBinder binder = new DataBinder();
/* 411 */     if (extraProps != null)
/*     */     {
/* 413 */       binder.setLocalData(extraProps);
/*     */     }
/*     */ 
/* 416 */     boolean hasMore = true;
/* 417 */     int skipCount = 0;
/* 418 */     while (hasMore)
/*     */     {
/* 420 */       ResultSet rset = this.m_workspace.createResultSet(feederQuery, binder);
/* 421 */       if (rset == null)
/*     */       {
/*     */         return;
/*     */       }
/*     */ 
/* 427 */       if (updatedRowInResult)
/*     */       {
/* 429 */         int left = skipCount;
/* 430 */         while (left > 0)
/*     */         {
/* 432 */           int skipped = rset.skip(left);
/* 433 */           if (skipped <= 0) {
/*     */             break;
/*     */           }
/*     */ 
/* 437 */           left -= skipped;
/*     */         }
/* 439 */         if (left > 0) {
/*     */           return;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 445 */       int sublength = queries.length;
/* 446 */       DataResultSet drset = new DataResultSet();
/* 447 */       int numRows = 0;
/* 448 */       if (isBatchable)
/*     */       {
/* 450 */         numRows = this.m_updateQueryBatchSize / sublength;
/*     */       }
/*     */ 
/* 453 */       if (numRows == 0)
/*     */       {
/* 455 */         numRows = 1;
/*     */       }
/*     */ 
/* 458 */       drset.copy(rset, numRows);
/* 459 */       hasMore = rset.next();
/*     */ 
/* 461 */       binder.addResultSet(feederQuery, drset);
/* 462 */       int count = drset.getNumRows();
/* 463 */       skipCount += count;
/* 464 */       for (int i = 0; drset.isRowPresent(); ++i)
/*     */       {
/* 466 */         for (int j = 0; j < sublength; ++j)
/*     */         {
/* 468 */           this.m_workspace.addBatch(queries[j], binder);
/*     */         }
/* 470 */         if ((i % 20 == 0) && (statusMsg != null))
/*     */         {
/* 472 */           checkForAbort();
/* 473 */           reportProgress(0, statusMsg, i, count);
/*     */         }
/* 464 */         drset.next();
/*     */       }
/*     */ 
/* 476 */       this.m_workspace.executeBatch();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void executeDependentQueriesWithLock(String feederQuery, String[] queries, String lockQuery, Properties extraProps, String statusMsg)
/*     */     throws DataException, ServiceException
/*     */   {
/* 484 */     DataBinder binder = new DataBinder();
/* 485 */     if (extraProps != null)
/*     */     {
/* 487 */       binder.setLocalData(extraProps);
/*     */     }
/*     */ 
/* 490 */     ResultSet rset = this.m_workspace.createResultSet(feederQuery, binder);
/* 491 */     if (rset == null)
/*     */     {
/* 494 */       return;
/*     */     }
/*     */ 
/* 497 */     DataResultSet drset = new DataResultSet();
/* 498 */     drset.copy(rset, 0);
/* 499 */     binder.addResultSet(feederQuery, drset);
/* 500 */     int count = drset.getNumRows();
/*     */ 
/* 503 */     for (int i = 0; drset.isRowPresent(); ++i)
/*     */     {
/* 505 */       boolean isTranGood = false;
/*     */       try
/*     */       {
/* 508 */         this.m_workspace.beginTran();
/* 509 */         this.m_workspace.addBatch(lockQuery, binder);
/* 510 */         for (int j = 0; j < queries.length; ++j)
/*     */         {
/* 512 */           this.m_workspace.addBatch(queries[j], binder);
/*     */         }
/*     */ 
/* 515 */         this.m_workspace.executeBatch();
/* 516 */         isTranGood = true;
/*     */       }
/*     */       finally
/*     */       {
/* 520 */         if (isTranGood)
/*     */         {
/* 522 */           this.m_workspace.commitTran();
/*     */         }
/*     */         else
/*     */         {
/* 526 */           this.m_workspace.rollbackTran();
/*     */         }
/*     */       }
/* 529 */       if ((i % 20 == 0) && (statusMsg != null))
/*     */       {
/* 531 */         checkForAbort();
/* 532 */         reportProgress(0, statusMsg, i, count);
/*     */       }
/* 503 */       drset.next();
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean shouldExecuteBatch(long numQueries, boolean isLast)
/*     */   {
/* 539 */     if ((isLast) && (numQueries > 0L))
/*     */     {
/* 541 */       return true;
/*     */     }
/*     */ 
/* 544 */     return numQueries >= this.m_updateQueryBatchSize;
/*     */   }
/*     */ 
/*     */   public String computeNewReleaseState(String trueStates, String state, String newStateTrue, String newStateFalse)
/*     */   {
/* 557 */     if (trueStates.indexOf(state) >= 0)
/*     */     {
/* 559 */       return newStateTrue;
/*     */     }
/* 561 */     return newStateFalse;
/*     */   }
/*     */ 
/*     */   public boolean compareConfigurations(IndexerCollectionData currentDesignDef, IndexerCollectionData collectionDef)
/*     */   {
/* 567 */     return SearchLoader.compareConfigurations(currentDesignDef, collectionDef);
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, float amtDone, float max)
/*     */   {
/* 572 */     String text = StringUtils.createReportProgressString(type, msg, amtDone, max);
/* 573 */     this.m_state.m_state.putLocal("progressMessage", text);
/*     */     try
/*     */     {
/* 576 */       this.m_state.saveState();
/*     */     }
/*     */     catch (ServiceException ignore)
/*     */     {
/* 580 */       String tmp = LocaleUtils.encodeMessage("csIndexerStateSaveError", ignore.getMessage());
/*     */ 
/* 582 */       Report.trace(null, LocaleResources.localizeMessage(tmp, this), ignore);
/*     */     }
/*     */ 
/* 585 */     if (this.m_externalProgress == null)
/*     */       return;
/* 587 */     this.m_externalProgress.reportProgress(type, msg, amtDone, max);
/*     */   }
/*     */ 
/*     */   public String getProcessIdFilePath(boolean isRebuild)
/*     */   {
/* 593 */     String workingDir = FileUtils.getWorkingDir();
/* 594 */     String fileName = (isRebuild) ? "rebuildindexerprocessid.dat" : "updateindexerprocessid.dat";
/* 595 */     workingDir = FileUtils.directorySlashes(workingDir);
/* 596 */     return workingDir + fileName;
/*     */   }
/*     */ 
/*     */   public void saveIndexerProcessID(String processId)
/*     */   {
/* 601 */     boolean isRebuild = isRebuild();
/* 602 */     String path = getProcessIdFilePath(isRebuild);
/*     */     try
/*     */     {
/* 605 */       Writer w = FileUtils.openDataWriter(path);
/* 606 */       w.write(processId);
/* 607 */       w.close();
/* 608 */       Report.trace("indexerprocess", "Saving process ID " + processId, null);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 612 */       Report.trace("indexerprocess", null, t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String readIndexerProcessId(boolean isRebuild)
/*     */   {
/* 618 */     String path = getProcessIdFilePath(isRebuild);
/* 619 */     if (FileUtils.checkFile(path, true, false) == -16)
/*     */     {
/* 621 */       return null;
/*     */     }
/*     */     try
/*     */     {
/* 625 */       Reader r = FileUtils.openDataReader(path);
/* 626 */       char[] chs = new char[256];
/* 627 */       int nread = r.read(chs);
/* 628 */       if (nread > 0)
/*     */       {
/* 630 */         String result = new String(chs, 0, nread);
/* 631 */         result = result.trim();
/* 632 */         if (result.length() > 0)
/*     */         {
/* 634 */           if (isRebuild)
/*     */           {
/* 636 */             Report.trace("indexerprocess", "Reading rebuild process ID " + result, null);
/*     */           }
/*     */           else
/*     */           {
/* 640 */             Report.trace("indexerprocess", "Reading update process ID " + result, null);
/*     */           }
/* 642 */           return result;
/*     */         }
/*     */ 
/*     */       }
/* 647 */       else if (isRebuild)
/*     */       {
/* 649 */         Report.trace("indexerprocess", "Failed read rebuild process ID", null);
/*     */       }
/*     */       else
/*     */       {
/* 653 */         Report.trace("indexerprocess", "Failed read update process ID", null);
/*     */       }
/*     */ 
/* 656 */       r.close();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 660 */       Report.trace("indexerprocess", null, t);
/*     */     }
/* 662 */     return null;
/*     */   }
/*     */ 
/*     */   public void killPreviousProcessId(boolean isRebuild) throws DataException
/*     */   {
/* 667 */     String processId = readIndexerProcessId(isRebuild);
/* 668 */     if ((processId == null) || (processId.length() == 0))
/*     */     {
/* 670 */       return;
/*     */     }
/* 672 */     if ((this.m_killProcessIdPath != null) && (this.m_killProcessIdPath.length() > 0))
/*     */     {
/* 674 */       String[] cmdArray = { this.m_killProcessIdPath, processId };
/* 675 */       Runtime r = Runtime.getRuntime();
/* 676 */       boolean[] stopRunning = { false };
/* 677 */       int retVal = 0;
/*     */       try
/*     */       {
/* 680 */         Report.trace("indexerprocess", "Killing process ID " + processId, null);
/* 681 */         Process p = r.exec(cmdArray);
/* 682 */         Runnable bg = new Runnable(p, stopRunning)
/*     */         {
/*     */           public void run()
/*     */           {
/*     */             try
/*     */             {
/* 688 */               InputStream procIn = this.val$p.getInputStream();
/*     */ 
/* 693 */               InputStream procErr = this.val$p.getErrorStream();
/*     */ 
/* 695 */               Thread errThread = new Thread(procErr)
/*     */               {
/*     */                 public void run()
/*     */                 {
/*     */                   try
/*     */                   {
/* 702 */                     byte[] errbuf = new byte[1024];
/*     */ 
/* 704 */                     while (((count = this.val$procErr.read(errbuf)) > 0) && (IndexerWorkObject.1.this.val$stopRunning[0] == 0))
/*     */                     {
/*     */                       int count;
/* 706 */                       String s = new String(errbuf, 0, count);
/* 707 */                       Report.trace("indexerprocess", "killstdout: " + s, null);
/*     */                     }
/*     */                   }
/*     */                   catch (Exception ignore)
/*     */                   {
/* 712 */                     ignore.printStackTrace();
/*     */                   }
/*     */                 }
/*     */               };
/* 717 */               errThread.start();
/*     */ 
/* 719 */               byte[] buf = new byte['ÿ'];
/*     */ 
/* 721 */               while (((nread = procIn.read(buf)) > 0) && (this.val$stopRunning[0] == 0))
/*     */               {
/*     */                 int nread;
/* 723 */                 String output = new String(buf, 0, nread);
/* 724 */                 Report.trace("indexerprocess", "killstdout: " + output, null);
/*     */               }
/*     */ 
/*     */             }
/*     */             catch (Exception e)
/*     */             {
/* 730 */               Report.trace("indexerprocess", null, e);
/*     */             }
/*     */           }
/*     */         };
/* 734 */         Thread bgThread = new Thread(bg);
/* 735 */         bgThread.start();
/* 736 */         retVal = p.waitFor();
/* 737 */         stopRunning[0] = true;
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 741 */         e.printStackTrace();
/*     */       }
/*     */       finally
/*     */       {
/* 745 */         stopRunning[0] = true;
/*     */       }
/* 747 */       if (retVal != 0)
/*     */       {
/* 749 */         this.m_isAbort = true;
/* 750 */         String key = (isRebuild) ? "csIndexerCouldNotKillRebuildProcessID" : "csIndexerCouldNotKillUpdateProcessID";
/* 751 */         String msg = LocaleUtils.encodeMessage(key, null, processId);
/* 752 */         throw new DataException(msg);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 759 */         NativeOsUtils osUtils = new NativeOsUtils();
/* 760 */         if (osUtils.isKillSupported())
/*     */         {
/* 762 */           int p = NumberUtils.parseInteger(processId, 0);
/* 763 */           if ((p > 0) && 
/* 766 */             (osUtils.kill(p, 0) == 0))
/*     */           {
/* 769 */             this.m_isAbort = true;
/* 770 */             String key = (isRebuild) ? "csIndexerPreviousRebuildProcessIDActive" : "csIndexerPreviousUpdateProcessIDActive";
/* 771 */             String msg = LocaleUtils.encodeMessage(key, null, processId);
/* 772 */             throw new DataException(msg);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 779 */         Report.trace("indexerprocess", null, t);
/*     */       }
/*     */     }
/* 782 */     String filePath = getProcessIdFilePath(isRebuild);
/* 783 */     FileUtils.deleteFile(filePath);
/*     */   }
/*     */ 
/*     */   public void releaseConnection(boolean isForceRelease)
/*     */   {
/* 788 */     if (this.m_workspace == null)
/*     */       return;
/* 790 */     boolean doRelease = getEnvValueAsBoolean("AllowIndexerReleaseConnection", false);
/* 791 */     if ((!doRelease) && (!isForceRelease))
/*     */       return;
/* 793 */     this.m_workspace.releaseConnection();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 799 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96362 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerWorkObject
 * JD-Core Version:    0.5.4
 */