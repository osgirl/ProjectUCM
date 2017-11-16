/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.DataUtils;
/*     */ import intradoc.server.IndexerMonitor;
/*     */ import intradoc.shared.CollaborationUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IndexerBuildChanges extends IndexerStepImpl
/*     */ {
/*     */   protected IndexerWorkObject m_data;
/*     */   protected IndexerState m_state;
/*     */   protected WebChanges m_changes;
/*     */   protected int m_maxChanges;
/*     */   protected boolean m_stopIndexing;
/*     */   protected boolean m_isWaitingForExclusive;
/*     */   protected String m_finishedFlag;
/*     */   protected String m_noActionSymbol;
/*     */   protected int m_changeCount;
/*  50 */   protected static long m_prevDateMillis = 0L;
/*     */ 
/*     */   public IndexerBuildChanges()
/*     */   {
/*  34 */     this.m_data = null;
/*  35 */     this.m_state = null;
/*  36 */     this.m_changes = null;
/*     */ 
/*  39 */     this.m_stopIndexing = false;
/*  40 */     this.m_isWaitingForExclusive = false;
/*     */ 
/*  43 */     this.m_noActionSymbol = "NoChange";
/*     */   }
/*     */ 
/*     */   public void prepareUse(String step, IndexerWorkObject data, boolean isRestart)
/*     */     throws ServiceException
/*     */   {
/*  55 */     if (this.m_data == null)
/*     */     {
/*  57 */       this.m_data = data;
/*  58 */       this.m_state = ((IndexerState)data.getCachedObject("IndexerState"));
/*  59 */       this.m_finishedFlag = this.m_state.computeFinishedSymbol("ToBeIndexed");
/*  60 */       this.m_maxChanges = this.m_data.getEnvironmentInt("IndexerCheckpointCount", 200);
/*     */     }
/*     */ 
/*  63 */     this.m_changes = ((WebChanges)data.getCachedObject("WebChanges"));
/*  64 */     this.m_changes.destroy();
/*     */ 
/*  66 */     this.m_changes = new WebChanges();
/*  67 */     this.m_changes.init(data);
/*     */ 
/*  69 */     data.setCachedObject("WebChanges", this.m_changes);
/*     */ 
/*  71 */     if ((this.m_data == null) || (!this.m_data.getEnvValueAsBoolean("IsAllowIndexerExitWhenNoChanges", false))) {
/*     */       return;
/*     */     }
/*  74 */     this.m_noActionSymbol = "None";
/*     */   }
/*     */ 
/*     */   public void cleanUp(IndexerWorkObject data)
/*     */     throws ServiceException
/*     */   {
/*  81 */     this.m_changes.destroy();
/*     */ 
/*  83 */     this.m_state.freeFinishedSymbol("ToBeIndexed");
/*     */   }
/*     */ 
/*     */   public String doWork(String step, IndexerWorkObject data, boolean restart)
/*     */     throws ServiceException
/*     */   {
/*  90 */     Boolean executed = (Boolean)this.m_data.getCachedObject("BuildChangeExecuted");
/*  91 */     if ((executed != null) && (executed == Boolean.TRUE))
/*     */     {
/*  94 */       this.m_noActionSymbol = "Exit";
/*     */     }
/*     */     else
/*     */     {
/*  98 */       this.m_data.setCachedObject("BuildChangeExecuted", Boolean.TRUE);
/*     */     }
/*     */ 
/* 101 */     Boolean noAction = (Boolean)this.m_data.getCachedObject("BuildChangeNoAction");
/* 102 */     if ((noAction != null) && (noAction == Boolean.TRUE))
/*     */     {
/* 106 */       return "Exit";
/*     */     }
/*     */ 
/* 109 */     if (restart)
/*     */     {
/* 118 */       DataBinder binder = new DataBinder();
/* 119 */       binder.putLocal("oldIndexerState", this.m_finishedFlag);
/* 120 */       binder.putLocal("newIndexerState", " ");
/*     */       try
/*     */       {
/* 124 */         this.m_data.m_workspace.execute("UallIndexerState", binder);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 128 */         throw new ServiceException("!csIndexerUnableToRestartBuildChanges", e);
/*     */       }
/*     */     }
/*     */ 
/* 132 */     boolean isRebuild = this.m_state.isRebuild();
/*     */ 
/* 134 */     if (this.m_stopIndexing)
/*     */     {
/* 136 */       if (isRebuild)
/*     */       {
/* 138 */         this.m_state.requestExclusiveLock();
/* 139 */         this.m_stopIndexing = false;
/*     */ 
/* 142 */         this.m_isWaitingForExclusive = true;
/*     */       }
/*     */       else
/*     */       {
/* 146 */         this.m_changes.destroy();
/* 147 */         this.m_stopIndexing = false;
/* 148 */         this.m_data.setCachedObject("BuildChangeNoAction", Boolean.TRUE);
/* 149 */         return this.m_noActionSymbol;
/*     */       }
/*     */     }
/*     */ 
/* 153 */     this.m_state.setFinishedSymbol(this.m_finishedFlag);
/*     */ 
/* 155 */     this.m_data.m_msg = "!csLoadingWebChanges";
/*     */ 
/* 157 */     if (isRebuild)
/*     */     {
/* 159 */       this.m_changeCount = 0;
/* 160 */       this.m_isWaitingForExclusive = false;
/*     */ 
/* 164 */       int attemptCounter = 0;
/*     */       while (true)
/*     */       {
/* 167 */         buildChanges();
/* 168 */         if (this.m_changeCount > 0)
/*     */         {
/* 170 */           if (SystemUtils.m_verbose)
/*     */           {
/* 172 */             Report.debug("indexer", "Saving " + this.m_changeCount + " update changes", null);
/*     */           }
/* 174 */           this.m_changes.save();
/* 175 */           return "Success";
/*     */         }
/*     */ 
/* 178 */         this.m_state.requestExclusiveLock();
/* 179 */         if ((attemptCounter == 0) && 
/* 181 */           (this.m_state.waitForExclusiveLock(IndexerMonitor.m_touchMonitorInterval / 1000)))
/*     */         {
/* 183 */           return "FinishRebuild";
/*     */         }
/*     */ 
/* 186 */         String msg = LocaleUtils.encodeMessage("csWaitingForExclusiveForRebuild", null, "" + (attemptCounter + 1));
/* 187 */         this.m_data.reportProgress(1, msg, -1.0F, -1.0F);
/* 188 */         if (this.m_state.waitForExclusiveLock(IndexerMonitor.m_touchMonitorInterval / 1000 * 5))
/*     */         {
/* 190 */           return "FinishRebuild";
/*     */         }
/* 192 */         ++attemptCounter;
/*     */       }
/*     */     }
/* 195 */     this.m_changeCount = 0;
/* 196 */     buildChanges();
/* 197 */     if (this.m_changeCount > 0)
/*     */     {
/* 199 */       if (SystemUtils.m_verbose)
/*     */       {
/* 201 */         Report.debug("indexer", "Saving " + this.m_changeCount + " update changes", null);
/*     */       }
/* 203 */       this.m_changes.save();
/* 204 */       return "Success";
/*     */     }
/* 206 */     this.m_changes.destroy();
/* 207 */     this.m_data.setCachedObject("BuildChangeNoAction", Boolean.TRUE);
/* 208 */     return this.m_noActionSymbol;
/*     */   }
/*     */ 
/*     */   protected boolean buildChanges() throws ServiceException
/*     */   {
/* 213 */     this.m_data.reportProgress(1, this.m_data.m_msg, 0.0F, 100.0F);
/* 214 */     boolean result = false;
/*     */     try
/*     */     {
/* 217 */       if (!this.m_isWaitingForExclusive)
/*     */       {
/* 219 */         prepChanges();
/*     */       }
/* 221 */       this.m_data.checkForAbort();
/*     */ 
/* 223 */       loadChanges();
/* 224 */       this.m_data.checkForAbort();
/*     */ 
/* 226 */       result = true;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 230 */       throw new ServiceException("!csIndexerUnableToBuildChanges", e);
/*     */     }
/* 232 */     return result;
/*     */   }
/*     */ 
/*     */   protected void prepChanges() throws DataException, ServiceException
/*     */   {
/* 237 */     this.m_data.reportProgress(1, this.m_data.m_msg, 0.0F, 100.0F);
/*     */ 
/* 239 */     DataBinder binder = new DataBinder();
/*     */ 
/* 241 */     long now = System.currentTimeMillis();
/* 242 */     Date dte = new Date(now + 60000L);
/* 243 */     String str = LocaleResources.m_odbcFormat.format(dte);
/* 244 */     binder.putLocal("dDate", str);
/*     */ 
/* 255 */     binder.putLocal("dIndexerState", this.m_finishedFlag);
/* 256 */     boolean expireRevisionOnly = this.m_data.getEnvValueAsBoolean("ExpireRevisionOnly", false);
/*     */ 
/* 259 */     Properties original = binder.getLocalData();
/* 260 */     binder.setLocalData(new Properties(original));
/*     */     while (true)
/*     */     {
/* 264 */       ResultSet rset = this.m_data.m_workspace.createResultSet("INDEXER-PRE1-TO-EXPIRED-FEEDER", binder);
/* 265 */       DataResultSet drset = new DataResultSet();
/* 266 */       drset.copy(rset, 200);
/* 267 */       rset = null;
/* 268 */       if (expireRevisionOnly)
/*     */       {
/* 270 */         binder.addResultSet("feeder", drset);
/*     */       }
/*     */ 
/* 273 */       int count = drset.getNumRows();
/* 274 */       long expiredRows = 0L;
/*     */ 
/* 276 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 278 */         binder.mergeResultSetRowIntoLocalData(drset);
/* 279 */         if (expireRevisionOnly)
/*     */         {
/* 281 */           String releaseState = binder.get("dReleaseState");
/* 282 */           String newReleaseState = this.m_data.computeNewReleaseState("YIU", releaseState, "U", "R");
/*     */ 
/* 284 */           binder.putLocal("newReleaseState", newReleaseState);
/* 285 */           expiredRows = this.m_data.m_workspace.execute("INDEXER-PRE1-TO-EXPIRED-SPECIFIC-REV", binder);
/* 286 */           if (expiredRows > 0L)
/*     */           {
/* 288 */             logExpiration(binder);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 293 */           String id = binder.get("dID");
/* 294 */           long idValue = NumberUtils.parseLong(id, 0L);
/*     */ 
/* 296 */           rset = this.m_data.m_workspace.createResultSet("INDEXER-MAX-ALL", binder);
/* 297 */           DataResultSet revisionSet = new DataResultSet();
/* 298 */           revisionSet.copy(rset);
/* 299 */           rset = null;
/*     */ 
/* 301 */           for (revisionSet.first(); revisionSet.isRowPresent(); revisionSet.next())
/*     */           {
/* 303 */             binder.mergeResultSetRowIntoLocalData(revisionSet);
/*     */ 
/* 305 */             id = binder.get("dID");
/* 306 */             long thisId = NumberUtils.parseLong(id, 0L);
/* 307 */             if (thisId > idValue) {
/*     */               continue;
/*     */             }
/*     */ 
/* 311 */             String releaseState = binder.get("dReleaseState");
/* 312 */             String newReleaseState = releaseState;
/* 313 */             if (!releaseState.equals("O"))
/*     */             {
/* 316 */               newReleaseState = this.m_data.computeNewReleaseState("YIU", releaseState, "U", "R");
/*     */             }
/*     */ 
/* 319 */             binder.putLocal("newReleaseState", newReleaseState);
/* 320 */             expiredRows = this.m_data.m_workspace.execute("INDEXER-PRE1-TO-EXPIRED-SPECIFIC-REV", binder);
/* 321 */             if (expiredRows <= 0L)
/*     */               continue;
/* 323 */             logExpiration(binder);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 329 */       if (count < 200) {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 335 */     this.m_data.executeDependentQueriesWithLock("INDEXER-PRE2-UPDATE-TO-R-FEEDER", new String[] { "INDEXER-PRE2-UPDATE-TO-R" }, "UrevClassesLockBydID", original, "!csIndexerMarkingNew");
/*     */ 
/* 337 */     this.m_data.reportProgress(1, this.m_data.m_msg, 30.0F, 100.0F);
/*     */   }
/*     */ 
/*     */   public void logExpiration(DataBinder binder) throws DataException
/*     */   {
/* 342 */     Date date = new Date();
/* 343 */     long curTime = date.getTime() + 1L;
/* 344 */     if (curTime <= m_prevDateMillis)
/*     */     {
/*     */       try
/*     */       {
/* 349 */         Thread.sleep(1L);
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 353 */         Report.trace(null, null, ignore);
/*     */       }
/* 355 */       curTime = m_prevDateMillis + 1L;
/*     */     }
/* 357 */     DataUtils.computeActionDates(binder, curTime);
/* 358 */     binder.putLocal("dUser", "Automated");
/* 359 */     binder.putLocal("dAction", "Expiration");
/*     */ 
/* 361 */     CollaborationUtils.setCollaborationName(binder);
/* 362 */     this.m_data.m_workspace.execute("IdocHistory", binder);
/*     */ 
/* 366 */     m_prevDateMillis = curTime;
/*     */   }
/*     */ 
/*     */   protected void loadChanges() throws DataException, ServiceException
/*     */   {
/* 371 */     WebChange change = null;
/* 372 */     WebChange tmpChange = null;
/* 373 */     int rowCount = 0;
/*     */ 
/* 376 */     this.m_data.reportProgress(1, this.m_data.m_msg, 0.0F, 100.0F);
/*     */ 
/* 378 */     Properties props = new Properties();
/* 379 */     PropParameters args = new PropParameters(props);
/* 380 */     props.put("dIndexerState", this.m_finishedFlag);
/* 381 */     boolean isRebuild = this.m_state.isRebuild();
/* 382 */     String queries = (isRebuild) ? "INDEXER-ALL-NOT-N" : "INDEXER-ALL-CHANGES-FEED-1,INDEXER-ALL-CHANGES-FEED-2";
/* 383 */     ResultSet queryTable = SharedObjects.getTable("IndexerQueryTable");
/* 384 */     List queryList = StringUtils.makeListFromSequenceSimple(queries);
/*     */ 
/* 386 */     Vector rows = null;
/* 387 */     Vector allRows = new IdcVector();
/* 388 */     ResultSet rset = null;
/* 389 */     String query = "";
/*     */ 
/* 391 */     for (int queryNo = 0; queryNo < queryList.size(); ++queryNo)
/*     */     {
/* 393 */       String queryName = (String)queryList.get(queryNo);
/* 394 */       query = ResultSetUtils.findValue(queryTable, "name", queryName, "queryStr");
/* 395 */       if (!isRebuild)
/*     */       {
/* 397 */         String whereClause = this.m_state.getWhereClause();
/* 398 */         if (whereClause.trim().length() > 0)
/*     */         {
/* 400 */           query = query + " AND (" + whereClause + ")";
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 408 */         this.m_data.m_workspace.beginTranEx(4);
/* 409 */         rset = this.m_data.m_workspace.createResultSetSQL(query);
/* 410 */         rows = getFieldFromRows(rset, "dRevClassID", "csIndexerGettingItems", this.m_maxChanges);
/*     */       }
/*     */       finally
/*     */       {
/* 415 */         this.m_data.m_workspace.commitTran();
/*     */       }
/*     */ 
/* 421 */       if ((isRebuild) && (rows != null) && (rows.size() > 0))
/*     */       {
/* 423 */         IndexerState indexerState = (IndexerState)this.m_data.getCachedObject("IndexerState");
/*     */ 
/* 426 */         List failedDocRevClassIds = indexerState.failedDocRevClassIds;
/* 427 */         if ((failedDocRevClassIds != null) && (failedDocRevClassIds.size() > 0))
/*     */         {
/* 429 */           Iterator failedDocRevClassIdsItr = failedDocRevClassIds.iterator();
/* 430 */           while (failedDocRevClassIdsItr.hasNext())
/*     */           {
/* 432 */             String failedDocRevClassId = (String)failedDocRevClassIdsItr.next();
/* 433 */             if (rows.contains(failedDocRevClassId))
/*     */             {
/* 435 */               rows.remove(failedDocRevClassId);
/* 436 */               failedDocRevClassIdsItr.remove();
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 443 */       allRows.addAll(rows);
/*     */     }
/*     */ 
/* 446 */     if (allRows.size() > 0)
/*     */     {
/* 448 */       rowCount = 0;
/* 449 */       FieldInfo[] infos = null;
/* 450 */       int totalCount = allRows.size();
/* 451 */       this.m_stopIndexing = (totalCount < this.m_maxChanges);
/*     */ 
/* 453 */       int rowUpdateCount = 0;
/* 454 */       ArrayList updateChanges = new ArrayList();
/*     */ 
/* 456 */       for (int i = 0; i < totalCount; ++i)
/*     */       {
/* 458 */         String classID = (String)allRows.elementAt(i);
/* 459 */         props.put("dRevClassID", classID);
/* 460 */         DataBinder binder = new DataBinder();
/* 461 */         binder.putLocal("dRevClassID", classID);
/* 462 */         boolean addChange = false;
/* 463 */         boolean isTranGood = false;
/*     */         try
/*     */         {
/* 467 */           this.m_data.m_workspace.beginTran();
/* 468 */           this.m_data.m_workspace.execute("UrevClassesLockBydRevClassID", binder);
/* 469 */           rset = this.m_data.m_workspace.createResultSet("INDEXER-MAX-ALL", args);
/*     */ 
/* 471 */           if ((rset != null) && (rset.isRowPresent()))
/*     */           {
/* 473 */             if (infos == null)
/*     */             {
/* 475 */               infos = ResultSetUtils.createInfoList(rset, new String[] { "dID", "dReleaseState", "dStatus", "dIndexerState" }, true);
/*     */             }
/*     */ 
/* 479 */             change = new WebChange();
/* 480 */             change.m_dRevClassID = classID;
/* 481 */             change.m_dWebExtension = "";
/*     */ 
/* 483 */             char releaseState = computeReleaseState(rset, change, infos);
/* 484 */             if (releaseState != ' ')
/*     */             {
/* 486 */               props.put("dRevClassID", change.m_dRevClassID);
/*     */ 
/* 491 */               props.put("dID", "0");
/*     */ 
/* 495 */               if (change.m_dID != null)
/*     */               {
/* 505 */                 if ((change.m_change == '-') || (releaseState != 'Y') || (isRebuild) || (change.m_dIndexerState == 'Y'))
/*     */                 {
/* 508 */                   props.put("dID", change.m_dID);
/*     */                 }
/*     */                 else
/*     */                 {
/* 519 */                   change.m_change = 'I';
/*     */                 }
/*     */ 
/* 522 */                 change.m_origChange = change.m_change;
/*     */ 
/* 524 */                 addChange = true;
/*     */               }
/*     */ 
/* 533 */               if (change.m_dIndexerState != 'Y')
/*     */               {
/* 537 */                 query = "INDEXER-REVCLASS-TO-INDEXABLE";
/* 538 */                 this.m_data.m_workspace.addBatch(query, args);
/* 539 */                 ++rowUpdateCount;
/* 540 */                 updateChanges.add(change);
/* 541 */                 addChange = false;
/*     */               }
/*     */ 
/* 544 */               ++rowCount;
/*     */             }
/*     */           }
/*     */ 
/* 548 */           boolean shouldExecuteBatch = this.m_data.shouldExecuteBatch(rowUpdateCount, totalCount - i <= 1);
/* 549 */           if (shouldExecuteBatch)
/*     */           {
/* 551 */             int[] updateCounts = this.m_data.m_workspace.executeBatch();
/* 552 */             isTranGood = true;
/* 553 */             Object[] updateChangeArr = updateChanges.toArray();
/* 554 */             for (int j = 0; (updateCounts != null) && (j < updateCounts.length); ++j)
/*     */             {
/* 556 */               if (updateCounts[j] <= 0) {
/*     */                 continue;
/*     */               }
/* 559 */               WebChange updateChange = (WebChange)updateChangeArr[j];
/* 560 */               if (updateChange.m_dID == null)
/*     */                 continue;
/* 562 */               tmpChange = this.m_changes.addChange(updateChange);
/* 563 */               if (tmpChange == null) {
/*     */                 continue;
/*     */               }
/* 566 */               throw new ServiceException(-34, LocaleUtils.encodeMessage("csIndexerDoubleAddError", null, change.m_dID));
/*     */             }
/*     */ 
/* 572 */             rowUpdateCount = 0;
/* 573 */             updateChanges = new ArrayList();
/*     */           }
/* 575 */           if (addChange)
/*     */           {
/* 578 */             tmpChange = this.m_changes.addChange(change);
/* 579 */             if (tmpChange != null)
/*     */             {
/* 583 */               throw new ServiceException(-34, LocaleUtils.encodeMessage("csIndexerDoubleAddError", null, change.m_dID));
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */         finally
/*     */         {
/* 590 */           if (isTranGood)
/*     */           {
/* 592 */             this.m_data.m_workspace.commitTran();
/*     */           }
/*     */           else
/*     */           {
/* 596 */             this.m_data.m_workspace.rollbackTran();
/*     */           }
/*     */         }
/*     */ 
/* 600 */         if (rowCount % 100 != 0)
/*     */           continue;
/* 602 */         this.m_data.reportProgress(1, LocaleUtils.encodeMessage("csLoadedChanges", null, "" + rowCount), rowCount, totalCount);
/*     */ 
/* 605 */         this.m_data.checkForAbort();
/*     */       }
/*     */ 
/* 608 */       this.m_changes.save();
/*     */     }
/* 610 */     this.m_changeCount = rowCount;
/* 611 */     this.m_data.reportProgress(1, this.m_data.m_msg, 100.0F, 100.0F);
/*     */   }
/*     */ 
/*     */   protected char computeReleaseState(ResultSet rset, WebChange change, FieldInfo[] infos)
/*     */   {
/* 616 */     char releaseState = ' ';
/* 617 */     boolean foundDeletedCurrentReleased = false;
/* 618 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 620 */       String status = rset.getStringValue(infos[2].m_index);
/* 621 */       String id = rset.getStringValue(infos[0].m_index);
/* 622 */       String tmp = rset.getStringValue(infos[1].m_index);
/* 623 */       releaseState = tmp.charAt(0);
/* 624 */       tmp = rset.getStringValue(infos[3].m_index) + " ";
/* 625 */       change.m_dIndexerState = tmp.charAt(0);
/* 626 */       if ((change.m_dIndexerState != ' ') && (change.m_dIndexerState != 'Y'))
/*     */       {
/* 628 */         if (SystemUtils.m_verbose)
/*     */         {
/* 630 */           Report.debug("indexer", "computeReleaseState, skipping for indexerState - releaseState='" + releaseState + "' indexerState='" + tmp + "' status=" + status + " id=" + id, null);
/*     */         }
/*     */ 
/* 634 */         releaseState = ' ';
/* 635 */         break;
/*     */       }
/*     */ 
/* 638 */       boolean isReleasedCurrent = (releaseState == 'U') || (releaseState == 'Y') || (releaseState == 'I') || (releaseState == 'D');
/*     */ 
/* 640 */       boolean isNewToIndexer = releaseState == 'R';
/* 641 */       boolean isOld = releaseState == 'O';
/*     */ 
/* 643 */       if ((!isReleasedCurrent) && (!isNewToIndexer) && (!isOld))
/*     */       {
/* 645 */         if (!SystemUtils.m_verbose)
/*     */           continue;
/* 647 */         Report.debug("indexer", "computeReleaseState - skipping for releaseState - releaseState='" + releaseState + "' indexerState='" + tmp + "' status=" + status + " id=" + id, null);
/*     */       }
/*     */       else
/*     */       {
/* 653 */         boolean isDeleted = (status.equals("DELETED")) || (status.equals("EXPIRED")) || (releaseState == 'D');
/*     */ 
/* 674 */         if (isDeleted)
/*     */         {
/* 676 */           if ((((change.m_dID != null) || (!isNewToIndexer))) && (((foundDeletedCurrentReleased) || (!isReleasedCurrent)))) {
/*     */             continue;
/*     */           }
/* 679 */           change.m_dID = id;
/* 680 */           change.m_change = '-';
/* 681 */           change.m_dReleaseState = releaseState;
/* 682 */           if (!isReleasedCurrent)
/*     */             continue;
/* 684 */           foundDeletedCurrentReleased = true;
/*     */         }
/*     */         else
/*     */         {
/* 695 */           change.m_dID = id;
/* 696 */           change.m_change = '+';
/* 697 */           change.m_dReleaseState = releaseState;
/* 698 */           break;
/*     */         }
/*     */       }
/*     */     }
/* 702 */     return releaseState;
/*     */   }
/*     */ 
/*     */   protected Vector getFieldFromRows(ResultSet rset, String fieldName, String statusMsg, int limit)
/*     */     throws DataException
/*     */   {
/* 708 */     if (rset == null)
/*     */     {
/* 710 */       return null;
/*     */     }
/* 712 */     int index = ResultSetUtils.getIndexMustExist(rset, fieldName);
/* 713 */     Vector rows = new IdcVector();
/*     */ 
/* 715 */     int rowCount = 0;
/* 716 */     Hashtable revClassIds = new Hashtable();
/* 717 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 719 */       String revClassId = rset.getStringValue(index);
/* 720 */       if (revClassIds.get(revClassId) != null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 724 */       revClassIds.put(revClassId, "1");
/* 725 */       rows.addElement(revClassId);
/* 726 */       ++rowCount;
/* 727 */       if (rowCount % 100 == 0)
/*     */       {
/* 729 */         String msg = LocaleUtils.encodeMessage(statusMsg, null, "" + rowCount);
/*     */ 
/* 731 */         this.m_data.reportProgress(1, msg, -1.0F, -1.0F);
/*     */       }
/* 733 */       if (rowCount == limit) {
/*     */         break;
/*     */       }
/*     */     }
/*     */ 
/* 738 */     rset.closeInternals();
/* 739 */     return rows;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 745 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98170 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerBuildChanges
 * JD-Core Version:    0.5.4
 */