/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.IndexerMonitor;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.server.utils.DocumentInfoCacheUtils;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class Indexer extends IndexerStepImpl
/*     */ {
/*     */   protected IndexerWorkObject m_data;
/*     */   protected Hashtable m_stepObjects;
/*     */   protected Hashtable m_objects;
/*     */   protected boolean m_isSuspend;
/*     */   protected IndexerTransition m_transitions;
/*     */   protected String m_cycleId;
/*     */ 
/*     */   public Indexer()
/*     */   {
/*  43 */     this.m_isSuspend = false;
/*     */ 
/*  47 */     this.m_cycleId = null;
/*     */   }
/*     */ 
/*     */   public void init(Workspace ws, ReportProgress progress, String cycleId, String restartId, Map extraParams)
/*     */     throws ServiceException
/*     */   {
/*  54 */     this.m_cycleId = cycleId;
/*     */ 
/*  56 */     this.m_data = new IndexerWorkObject(this);
/*     */     try
/*     */     {
/*  62 */       this.m_cycleId = this.m_data.initEx(this.m_cycleId, restartId, 0, extraParams, ws, progress);
/*     */ 
/*  64 */       this.m_data.initForIndexEngine();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  68 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  73 */       String cfgFile = this.m_data.m_searchDir + "search.cfg";
/*  74 */       if (FileUtils.checkFile(cfgFile, true, false) == 0)
/*     */       {
/*  76 */         DataLoader.cachePropertiesFromFile(cfgFile, null);
/*     */       }
/*  78 */       else if (SystemUtils.m_verbose)
/*     */       {
/*  80 */         Report.debug("indexer", cfgFile + " doesn't exist.", null);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  85 */       throw new ServiceException("!csIndexerUnableToLoadConfig");
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  90 */       initMachine();
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/*  94 */       throw new ServiceException("csIndexerInitializationError", e);
/*     */     }
/*     */ 
/*  97 */     validateCfg();
/*     */ 
/* 100 */     boolean useThumbnails = this.m_data.getEnvValueAsBoolean("RenditionIsActive:THUMBNAIL", false);
/* 101 */     if (useThumbnails)
/*     */     {
/*     */       try
/*     */       {
/* 105 */         Service.checkFeatureAllowed("Thumbnail");
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 109 */         String msg = LocaleUtils.encodeMessage("csIndexerThumbnailsDisabled", e.getMessage());
/*     */ 
/* 111 */         Report.error(null, msg, e);
/* 112 */         Report.trace(null, LocaleResources.localizeMessage(msg, this.m_data), null);
/*     */ 
/* 114 */         this.m_data.putEnvironmentValue("RenditionIsActive:THUMBNAIL", "0");
/* 115 */         SharedObjects.putEnvironmentValue("RenditionIsActive:THUMBNAIL", "0");
/*     */       }
/*     */     }
/*     */ 
/* 119 */     WebChanges changes = new WebChanges();
/* 120 */     this.m_data.setCachedObject("WebChanges", changes);
/* 121 */     changes.init(this.m_data);
/*     */   }
/*     */ 
/*     */   protected void initMachine() throws DataException, ServiceException
/*     */   {
/* 126 */     String fileName = this.m_data.getEnvironmentValue("IndexerMachineFile");
/*     */ 
/* 128 */     DataBinder indexerMachine = new DataBinder();
/*     */ 
/* 130 */     if (fileName == null)
/*     */     {
/* 132 */       ResultSet rset = SharedObjects.getTable("IndexerStatesTable");
/* 133 */       indexerMachine.addResultSet("IndexerStatesTable", rset);
/* 134 */       rset = SharedObjects.getTable("IndexerTransitionsTable");
/* 135 */       indexerMachine.addResultSet("IndexerTransitionsTable", rset);
/*     */     }
/*     */     else
/*     */     {
/* 139 */       String dir = FileUtils.getDirectory(fileName);
/* 140 */       String name = FileUtils.getName(fileName);
/* 141 */       ResourceUtils.serializeDataBinder(dir, name, indexerMachine, false, true);
/*     */     }
/*     */ 
/* 144 */     this.m_transitions = ((IndexerTransition)ComponentClassFactory.createClassInstance("IndexerTransition", "intradoc.indexer.IndexerTransition", LocaleUtils.encodeMessage("csUnableToCreateObject", null, "IndexerTransition")));
/*     */     try
/*     */     {
/* 150 */       this.m_transitions.init(indexerMachine);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 154 */       throw new ServiceException("!csIndexerUnableToLoadTransitions", e);
/*     */     }
/*     */ 
/* 157 */     this.m_stepObjects = new Hashtable();
/* 158 */     this.m_objects = new Hashtable();
/* 159 */     ResultSet rset = indexerMachine.getResultSet("IndexerStatesTable");
/*     */     FieldInfo[] infos;
/*     */     try
/*     */     {
/* 164 */       infos = ResultSetUtils.createInfoList(rset, new String[] { "name", "implementation", "description" }, true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 169 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 172 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 174 */       String name = rset.getStringValue(infos[0].m_index);
/* 175 */       String implementor = rset.getStringValue(infos[1].m_index);
/*     */ 
/* 177 */       int index = implementor.indexOf(",");
/*     */       Object impObject;
/*     */       Object impObject;
/* 180 */       if (index >= 0)
/*     */       {
/* 182 */         impObject = implementor;
/*     */       }
/*     */       else
/*     */       {
/*     */         Object impObject;
/* 184 */         if (implementor.equals("this"))
/*     */         {
/* 186 */           impObject = this;
/*     */         }
/*     */         else
/*     */         {
/* 190 */           impObject = this.m_objects.get(implementor);
/*     */ 
/* 192 */           if (impObject == null)
/*     */           {
/* 194 */             impObject = ComponentClassFactory.createClassInstance(implementor, implementor, LocaleUtils.encodeMessage("csUnableToCreateObject", null, implementor));
/*     */ 
/* 197 */             ((IndexerStep)impObject).initStep(this.m_data);
/* 198 */             this.m_objects.put(implementor, impObject);
/*     */           }
/*     */         }
/*     */       }
/* 202 */       this.m_stepObjects.put(name, impObject);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void validateCfg() throws ServiceException
/*     */   {
/* 208 */     this.m_data.m_indexCollectionManager.validateConfiguration();
/*     */ 
/* 210 */     FileUtils.checkOrCreateDirectory(this.m_data.m_logFileDir, 0);
/*     */ 
/* 212 */     if ((this.m_data.m_debugLevel == null) || (this.m_data.m_debugLevel.length() == 0))
/*     */     {
/* 214 */       this.m_data.m_debugLevel = "none";
/*     */     }
/*     */ 
/* 217 */     if (this.m_data.m_maxCollectionSize != 0)
/*     */       return;
/* 219 */     this.m_data.m_maxCollectionSize = 50;
/*     */   }
/*     */ 
/*     */   public String doWork(String step, IndexerWorkObject data, boolean restart)
/*     */     throws ServiceException
/*     */   {
/* 232 */     if (step.equals("Init"))
/*     */     {
/* 234 */       this.m_data.m_isAbort = false;
/*     */ 
/* 236 */       WebChanges changes = (WebChanges)this.m_data.getCachedObject("WebChanges");
/* 237 */       changes.destroy();
/*     */ 
/* 239 */       this.m_data.setCachedObject("LoadedProps", new Hashtable());
/*     */ 
/* 241 */       return "Success";
/*     */     }
/* 243 */     if (step.equals("Cleanup"))
/*     */     {
/* 245 */       doCleanUp();
/* 246 */       return "Success";
/*     */     }
/* 248 */     if (step.equals("CheckForWork"))
/*     */     {
/* 250 */       WebChanges changes = (WebChanges)this.m_data.getCachedObject("WebChanges");
/* 251 */       changes.init(this.m_data);
/* 252 */       if (changes.count() > 0)
/*     */       {
/* 254 */         char[] type = { '-', '+', '*', '!' };
/* 255 */         String[] result = { "Deletions", "Additions", "Retries", "Retries" };
/*     */ 
/* 258 */         for (int i = 0; i < type.length; ++i)
/*     */         {
/* 260 */           int count = changes.count(type[i]);
/* 261 */           if (count <= 0)
/*     */             continue;
/* 263 */           if (Report.m_verbose)
/*     */           {
/* 265 */             Report.debug("indexer", "Found work:" + result[i] + "; Type:" + type[i] + "; Change count: " + count, null);
/*     */           }
/*     */ 
/* 268 */           return result[i];
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 273 */       return "None";
/*     */     }
/* 275 */     if (step.equals("FinishRebuild"))
/*     */     {
/*     */       try
/*     */       {
/* 281 */         IndexerState indexerState = (IndexerState)this.m_data.getCachedObject("IndexerState");
/* 282 */         List failedDocRevClassIds = indexerState.failedDocRevClassIds;
/* 283 */         if (failedDocRevClassIds != null)
/*     */         {
/* 285 */           failedDocRevClassIds.clear();
/*     */         }
/*     */ 
/* 288 */         this.m_data.m_indexCollectionManager.cleanUp(data);
/*     */ 
/* 291 */         this.m_data.m_indexer.cleanup();
/*     */ 
/* 293 */         this.m_data.clearRebuildFlags();
/* 294 */         PluginFilters.filter("finishRebuild", this.m_data.m_workspace, null, this.m_data);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 298 */         throw new ServiceException("!csIndexerUnableToFinishRebuild", e);
/*     */       }
/*     */ 
/* 301 */       return "Finished";
/*     */     }
/*     */ 
/* 305 */     String extendedResult = doExtendedWork(step, data, restart);
/* 306 */     if (extendedResult != null)
/*     */     {
/* 308 */       return extendedResult;
/*     */     }
/*     */ 
/* 312 */     throw new ServiceException(LocaleUtils.encodeMessage("csIndexerUnknownStep", null, step));
/*     */   }
/*     */ 
/*     */   public String doExtendedWork(String step, IndexerWorkObject data, boolean restart)
/*     */     throws ServiceException
/*     */   {
/* 320 */     return null;
/*     */   }
/*     */ 
/*     */   public void buildIndex() throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 327 */       doIndexing();
/*     */     }
/*     */     catch (RuntimeException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       WebChanges changes;
/* 335 */       this.m_data.m_workspace.releaseConnection();
/* 336 */       WebChanges changes = (WebChanges)this.m_data.getCachedObject("WebChanges");
/* 337 */       if (changes != null)
/*     */       {
/* 339 */         changes.close();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void doIndexing() throws ServiceException
/*     */   {
/* 346 */     IndexerState stateData = (IndexerState)this.m_data.getCachedObject("IndexerState");
/* 347 */     String state = stateData.getCurrentState();
/*     */ 
/* 349 */     boolean isRestart = this.m_data.isRestart();
/* 350 */     boolean isRebuild = this.m_data.isRebuild();
/* 351 */     this.m_data.m_indexer.prepare();
/* 352 */     this.m_data.m_driver.checkConnection();
/*     */ 
/* 354 */     if (isRebuild)
/*     */     {
/* 356 */       if (isRestart)
/*     */       {
/* 358 */         Report.info(null, null, "csIndexerRebuildRestarted", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 362 */         Report.info(null, null, "csIndexerRebuildStarted", new Object[0]);
/*     */       }
/*     */     }
/*     */ 
/* 366 */     while (!state.equals("Finished"))
/*     */     {
/* 369 */       this.m_data.checkForAbort();
/* 370 */       if (this.m_isSuspend)
/*     */       {
/* 374 */         String msg = LocaleUtils.encodeMessage("csIndexerSuspendedMsg", null, "csIndexerLabel_" + this.m_cycleId);
/*     */ 
/* 376 */         throw new ServiceException(-66, msg);
/*     */       }
/*     */ 
/* 379 */       String result = null;
/*     */ 
/* 381 */       Object action = this.m_stepObjects.get(state);
/* 382 */       if (action == null)
/*     */       {
/* 384 */         throw new ServiceException(LocaleUtils.encodeMessage("csIndexerStateObjectNull", null, state));
/*     */       }
/*     */ 
/* 388 */       if (action instanceof String)
/*     */       {
/* 390 */         String text = (String)action;
/* 391 */         int i = text.indexOf(",");
/* 392 */         if (i >= 0)
/*     */         {
/* 394 */           result = text.substring(i + 1);
/* 395 */           text = text.substring(0, i);
/*     */         }
/*     */         try
/*     */         {
/* 399 */           if (SystemUtils.m_verbose)
/*     */           {
/* 401 */             Report.debug("indexer", "isRebuild?" + isRebuild + "; IndexerStep:" + state + ";  Action:" + text, null);
/*     */           }
/* 403 */           PluginFilters.filter(text, this.m_data.m_workspace, null, this.m_data);
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 407 */           throw new ServiceException(e);
/*     */         }
/*     */       }
/* 410 */       else if (action instanceof IndexerStep)
/*     */       {
/* 412 */         if (SystemUtils.m_verbose)
/*     */         {
/* 414 */           Report.debug("indexer", "isRebuild?" + isRebuild + "; IndexerStep:" + state + ";  Class:" + action.getClass().getName(), null);
/*     */         }
/*     */ 
/* 417 */         IndexerStep step = (IndexerStep)action;
/* 418 */         step.prepareUse(state, this.m_data, isRestart);
/* 419 */         result = step.doWork(state, this.m_data, isRestart);
/*     */       }
/*     */       else
/*     */       {
/* 423 */         throw new ServiceException(LocaleUtils.encodeMessage("csIndexerUnableToExecute", null, action.getClass().getName()));
/*     */       }
/*     */ 
/* 436 */       isRestart = false;
/*     */ 
/* 438 */       state = this.m_transitions.computeNextState(state, result);
/* 439 */       if (state == null)
/*     */       {
/* 441 */         throw new ServiceException("!csIndexerNullState");
/*     */       }
/* 443 */       stateData.doStateTransition(state);
/*     */     }
/*     */ 
/* 448 */     if (!this.m_data.m_state.isRebuild())
/*     */     {
/* 450 */       this.m_data.m_indexer.cleanup();
/* 451 */       this.m_data.m_indexCollectionManager.cleanUp(this.m_data);
/*     */     }
/*     */ 
/* 455 */     this.m_data.reportProgress(2, "!csIndexingFinished", -1.0F, -1.0F);
/*     */ 
/* 457 */     if (!stateData.isRebuild())
/*     */       return;
/*     */     String msg;
/*     */     String msg;
/* 460 */     if (stateData.m_errCount == 0)
/*     */     {
/*     */       String msg;
/* 462 */       if (stateData.m_totalAddIndex == 0)
/*     */       {
/* 464 */         msg = "csIndexerRebuildComplete0";
/*     */       }
/*     */       else
/*     */       {
/* 468 */         msg = "csIndexerRebuildComplete1";
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 473 */       msg = "csIndexerRebuildComplete2";
/*     */     }
/* 475 */     Report.info(null, null, msg, new Object[] { "" + stateData.m_totalAddIndex, "" + stateData.m_totalFullTextAdd, "" + stateData.m_errCount });
/*     */   }
/*     */ 
/*     */   public synchronized void cancelBuild()
/*     */   {
/* 482 */     this.m_data.m_isAbort = true;
/*     */   }
/*     */ 
/*     */   public synchronized void suspendBuild()
/*     */   {
/* 487 */     this.m_isSuspend = true;
/*     */   }
/*     */ 
/*     */   protected void cleanUpQueries(WebChanges changes)
/*     */     throws DataException, ServiceException
/*     */   {
/* 495 */     long currentSharedDate = DocumentInfoCacheUtils.synchronizeSharedTimestamp(DocumentInfoCacheUtils.F_READ_SYNC_AND_UPDATE);
/*     */ 
/* 497 */     String releaseDate = LocaleResources.m_odbcFormat.format(new Date(currentSharedDate));
/* 498 */     Properties props = new Properties();
/* 499 */     PropParameters args = new PropParameters(props);
/* 500 */     props.put("dReleaseDate", releaseDate);
/* 501 */     IndexerState state = (IndexerState)this.m_data.getCachedObject("IndexerState");
/* 502 */     boolean isRebuild = state.isRebuild();
/* 503 */     String finishedSymbol = state.getFinishedSymbol();
/* 504 */     props.put("dIndexerState", finishedSymbol);
/*     */ 
/* 513 */     boolean didDeleteCleanup = false;
/* 514 */     if (state.nonIdleCycleCount() == 1)
/*     */     {
/* 516 */       boolean shouldWait = true;
/*     */       try
/*     */       {
/* 519 */         state.requestExclusiveLock();
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 523 */         shouldWait = false;
/* 524 */         if (e.m_errorCode != -22)
/*     */         {
/* 526 */           throw e;
/*     */         }
/*     */       }
/*     */ 
/* 530 */       if (shouldWait)
/*     */       {
/*     */         try
/*     */         {
/* 535 */           if (state.waitForExclusiveLock(IndexerMonitor.m_touchMonitorInterval / 1000))
/*     */           {
/* 537 */             this.m_data.executeDependentQueries("INDEXER-REMOVE-DELETED-FEEDER", "INDEXER-REMOVE-DELETED-DOCUMENTS", props, "!csIndexerDeletingDocuments");
/*     */ 
/* 541 */             this.m_data.executeDependentQueries("INDEXER-REMOVE-DELETED-FEEDER", "DdocMeta", props, "!csIndexerDeletingMetaData");
/*     */ 
/* 545 */             this.m_data.executeDependentQueries("INDEXER-REMOVE-DELETED-FEEDER", "DrevClassesWithdIDCheckCount", props, "!csIndexerDeletingMetaData");
/*     */ 
/* 551 */             this.m_data.m_workspace.execute("INDEXER-REMOVE-DELETED-REVISIONS", args);
/*     */ 
/* 554 */             this.m_data.m_workspace.execute("INDEXER-UPDATE-WORKFLOW-DELETIONS1", args);
/* 555 */             this.m_data.m_workspace.execute("INDEXER-UPDATE-WORKFLOW-DELETIONS2", args);
/*     */ 
/* 558 */             this.m_data.killPreviousProcessId(false);
/* 559 */             this.m_data.killPreviousProcessId(true);
/*     */ 
/* 561 */             didDeleteCleanup = true;
/*     */           }
/*     */         }
/*     */         finally
/*     */         {
/* 566 */           state.releaseExclusiveLock();
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 571 */     WebChange[] changesList = changes.allChanges();
/* 572 */     int count = changesList.length;
/*     */     int i;
/* 573 */     int i = 0;
/* 574 */     ArrayList argCache = new ArrayList();
/* 575 */     for (int i = 0; i < count; ++i)
/*     */     {
/* 577 */       WebChange change = changesList[i];
/* 578 */       props.put("dID", change.m_dID);
/* 579 */       props.put("dRevClassID", change.m_dRevClassID);
/*     */ 
/* 613 */       if (change.m_dIndexerState == 'Y')
/*     */       {
/* 615 */         props.put("newIndexerState", "X");
/* 616 */         if (SystemUtils.m_verbose)
/*     */         {
/* 618 */           Report.debug("indexer", "cleanup for dID -- revision already handled by rebuild cycle " + change.m_dID + " change is " + change.m_change + " origChange is " + change.m_origChange + " newIndexerState is X", null);
/*     */         }
/*     */ 
/* 623 */         i = this.m_data.m_workspace.addBatch("UindexerState", args);
/*     */       }
/*     */       else
/*     */       {
/* 634 */         String newIndexerState = (isRebuild) ? "X" : " ";
/* 635 */         if ((isRebuild) && (change.m_dReleaseState != 'Y'))
/*     */         {
/* 638 */           newIndexerState = "Y";
/*     */         }
/* 640 */         props.put("newIndexerState", newIndexerState);
/*     */ 
/* 642 */         if (SystemUtils.m_verbose)
/*     */         {
/* 644 */           Report.debug("indexer", "cleanup for dID " + change.m_dID + " change is " + change.m_change + " origChange is " + change.m_origChange + " newIndexerState is " + newIndexerState, null);
/*     */         }
/*     */ 
/* 648 */         if (change.m_origChange == '+')
/*     */         {
/* 652 */           if (change.m_change != 'F')
/*     */           {
/* 654 */             String updateQuery = "INDEXER-POST0-UPDATE-Y-DONE-TO-RELEASED";
/* 655 */             if (change.m_dReleaseState == 'Y')
/*     */             {
/* 657 */               updateQuery = "INDEXER-POST0-UPDATE-Y-DONE-TO-RELEASED-REBUILD";
/*     */             }
/* 659 */             this.m_data.m_workspace.addBatch(updateQuery, args);
/*     */ 
/* 661 */             i = this.m_data.m_workspace.addBatch("UrevClassIndexedID", args);
/* 662 */             Object cache = { new Integer(i), args };
/* 663 */             argCache.add(cache);
/*     */           }
/*     */         }
/* 666 */         else if (!didDeleteCleanup)
/*     */         {
/* 696 */           String updateQuery = "INDEXER-POST0-UPDATE-U-DELETED-TO-Y-DELETED";
/* 697 */           if (change.m_dReleaseState == 'Y')
/*     */           {
/* 701 */             updateQuery = "INDEXER-POST0-UPDATE-U-DELETED-TO-Y-DELETED-REBUILD";
/*     */           }
/* 703 */           i = this.m_data.m_workspace.addBatch(updateQuery, args);
/*     */ 
/* 710 */           if (!isRebuild)
/*     */           {
/* 716 */             props.put("newIndexerState", "X");
/*     */           }
/* 718 */           i = this.m_data.m_workspace.addBatch("INDEXER-POST0-UPDATE-REVCLASS-DELETED-INDEXERSTATE", args);
/*     */         }
/*     */ 
/* 725 */         props.put("newIndexerState", (isRebuild) ? "X" : " ");
/*     */ 
/* 730 */         String query = "INDEXER-POST1-UPDATE-TO-RELEASED-O";
/* 731 */         if ((change.m_change == 'F') && 
/* 733 */           ("YUI".indexOf(change.m_dReleaseState) < 0))
/*     */         {
/* 735 */           query = "INDEXER-POST1-RESET-ON-FAILURE";
/*     */         }
/*     */ 
/* 739 */         i = this.m_data.m_workspace.addBatch(query, args);
/* 740 */         i = this.m_data.m_workspace.addBatch("INDEXER-POST2-UPDATE-EXPIRED-TO-O", args);
/*     */       }
/*     */ 
/* 743 */       if (this.m_data.shouldExecuteBatch(i + 1, count - i <= 1))
/*     */       {
/* 745 */         int[] updateCounts = this.m_data.m_workspace.executeBatch();
/* 746 */         Iterator it = argCache.iterator();
/* 747 */         while (it.hasNext())
/*     */         {
/* 749 */           Object[] cache = (Object[])(Object[])it.next();
/* 750 */           int updateIndex = ((Integer)cache[0]).intValue();
/* 751 */           if (updateCounts[updateIndex] == 0)
/*     */           {
/* 755 */             args = (PropParameters)cache[1];
/* 756 */             ResultSet rset = this.m_data.m_workspace.createResultSet("QdocID", args);
/* 757 */             if (rset.isRowPresent())
/*     */             {
/* 759 */               String status = ResultSetUtils.getValue(rset, "dStatus");
/* 760 */               boolean isUpdateState = true;
/* 761 */               if (status.equals("DELETED"))
/*     */               {
/* 763 */                 props.put("dReleaseState", "Y");
/*     */               }
/* 765 */               else if ((status.equals("DONE")) || (status.equals("GENWWW")))
/*     */               {
/* 767 */                 props.put("dReleaseState", "U");
/*     */               }
/*     */               else
/*     */               {
/* 771 */                 isUpdateState = false;
/*     */               }
/* 773 */               Report.trace("indexer", "POST0 updated 0 revisions, isUpdateState: " + isUpdateState + " dStatus: " + status, null);
/*     */ 
/* 775 */               if (isUpdateState)
/*     */               {
/* 777 */                 this.m_data.m_workspace.execute("UreleaseState", args);
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/* 782 */         argCache = new ArrayList();
/*     */       }
/*     */ 
/* 786 */       if (i % 20 != 0)
/*     */         continue;
/* 788 */       this.m_data.reportProgress(1, "!csPerformingCleanup", i, count);
/*     */     }
/*     */ 
/* 793 */     props.put("oldIndexerState", finishedSymbol);
/* 794 */     this.m_data.m_workspace.execute("UallIndexerState", args);
/*     */   }
/*     */ 
/*     */   protected void doCleanUp() throws ServiceException
/*     */   {
/* 799 */     this.m_data.checkForAbort();
/*     */ 
/* 801 */     WebChanges changes = (WebChanges)this.m_data.getCachedObject("WebChanges");
/*     */     try
/*     */     {
/* 806 */       cleanUpQueries(changes);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 814 */       SubjectManager.notifyChanged("documents");
/*     */     }
/*     */ 
/* 817 */     changes.destroy();
/*     */ 
/* 819 */     Hashtable cleaned = new Hashtable();
/* 820 */     for (Enumeration en = this.m_stepObjects.elements(); en.hasMoreElements(); )
/*     */     {
/* 822 */       Object object = en.nextElement();
/*     */ 
/* 824 */       if (object instanceof IndexerStep)
/*     */       {
/* 826 */         IndexerStep stepObject = (IndexerStep)object;
/*     */ 
/* 828 */         if (cleaned.get(stepObject) != null) {
/*     */           continue;
/*     */         }
/*     */ 
/* 832 */         stepObject.prepareUse("Cleanup", this.m_data, this.m_data.isRestart());
/* 833 */         stepObject.cleanUp(this.m_data);
/* 834 */         cleaned.put(stepObject, "yes");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 843 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99056 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.Indexer
 * JD-Core Version:    0.5.4
 */