/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.IndexerMonitor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.Writer;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IndexerState
/*     */ {
/*     */   protected String m_cycleId;
/*     */   protected String m_cycleDescription;
/*     */   protected String m_cycleWhereClause;
/*     */   protected String m_indexDirectory;
/*     */   protected String m_workDir;
/*     */   protected DataBinder m_cycleList;
/*     */   protected DataBinder m_collectionValues;
/*     */   protected DataBinder m_state;
/*     */   protected boolean[] m_lockObject;
/*  50 */   protected static final String[] m_cycleFields = { "sCycleID", "sWhereClause", "sDescription", "sIsRebuild", "sConfigOverrides" };
/*     */   public Properties m_overrideProps;
/*     */   public Properties m_perBatchOverrides;
/*     */   public boolean m_isRebuild;
/*     */   public boolean m_isRestart;
/*     */   protected String m_currentFinishedSymbol;
/*     */   public int m_totalAddIndex;
/*     */   public int m_cumTotalAddIndex;
/*     */   public int m_totalDeleteIndex;
/*     */   public int m_cumTotalDeleteIndex;
/*     */   public int m_totalFullTextAdd;
/*     */   public int m_cumTotalFullTextAdd;
/*     */   public int m_totalDummyTextAdd;
/*     */   public int m_cumTotalDummyTextAdd;
/*     */   public int m_totalAddBatchloads;
/*     */   public int m_cumTotalAddBatchloads;
/*     */   public int m_totalDeleteBatchloads;
/*     */   public int m_cumTotalDeleteBatchloads;
/*     */   public int m_cumTotalFileSize;
/*     */   public int m_errCount;
/*     */   public int m_totalErrCount;
/*     */   public int m_cumTotalErrCount;
/*     */   public List<String> failedDocRevClassIds;
/*     */ 
/*     */   public IndexerState()
/*     */   {
/*  47 */     this.m_lockObject = null;
/*     */ 
/*  61 */     this.m_currentFinishedSymbol = " ";
/*     */ 
/*  80 */     this.m_errCount = 0;
/*     */   }
/*     */ 
/*     */   public String loadStateData(String cycleId, String restartId, boolean readOnly, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  93 */     this.m_collectionValues = new DataBinder();
/*  94 */     this.m_cycleList = new DataBinder();
/*  95 */     this.m_indexDirectory = DirectoryLocator.getSearchDirectory();
/*     */ 
/*  97 */     ResourceUtils.serializeDataBinder(this.m_indexDirectory, "indexercycles.hda", this.m_cycleList, false, true);
/*     */ 
/* 100 */     DataResultSet rset = (DataResultSet)this.m_cycleList.getResultSet("IndexerCycles");
/* 101 */     if (rset == null)
/*     */     {
/* 103 */       throw new ServiceException("!csIndexerUnableToLoadIndexerCycles");
/*     */     }
/*     */ 
/*     */     FieldInfo[] infos;
/*     */     try
/*     */     {
/* 109 */       infos = ResultSetUtils.createInfoList(rset, m_cycleFields, true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 113 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 116 */     Vector v = null;
/* 117 */     if (cycleId == null)
/*     */     {
/* 119 */       v = rset.getCurrentRowValues();
/*     */     }
/*     */     else
/*     */     {
/* 123 */       v = rset.findRow(infos[0].m_index, cycleId);
/*     */     }
/*     */ 
/* 126 */     if (v != null)
/*     */     {
/* 129 */       this.m_cycleId = rset.getStringValue(infos[0].m_index);
/* 130 */       this.m_cycleWhereClause = rset.getStringValue(infos[1].m_index);
/* 131 */       this.m_cycleDescription = rset.getStringValue(infos[2].m_index);
/* 132 */       this.m_isRebuild = StringUtils.convertToBool(rset.getStringValue(infos[3].m_index), false);
/*     */     }
/*     */     else
/*     */     {
/* 137 */       this.m_cycleId = cycleId;
/* 138 */       this.m_cycleWhereClause = "";
/* 139 */       this.m_cycleDescription = "";
/* 140 */       this.m_isRebuild = this.m_cycleId.equals("rebuild");
/* 141 */       addCycle(rset);
/* 142 */       saveCycleTable();
/*     */     }
/*     */ 
/* 146 */     this.m_state = new DataBinder();
/*     */ 
/* 149 */     this.m_workDir = (this.m_indexDirectory + this.m_cycleId + "/");
/* 150 */     FileUtils.checkOrCreateDirectory(this.m_workDir, 1);
/* 151 */     cxt.setCachedObject("WorkDirectory", this.m_workDir);
/* 152 */     ResourceUtils.serializeDataBinder(this.m_workDir, "state.hda", this.m_state, false, false);
/* 153 */     String state = getCurrentState();
/* 154 */     this.m_state.putLocal("RestartId", restartId);
/*     */ 
/* 156 */     this.m_isRestart = ((state != null) && (!state.equals("Finished")));
/*     */ 
/* 159 */     String configOverrides = this.m_cycleList.get("sConfigOverrides");
/* 160 */     this.m_overrideProps = new Properties(this.m_state.getLocalData());
/* 161 */     this.m_perBatchOverrides = new Properties(this.m_overrideProps);
/* 162 */     StringUtils.parseProperties(this.m_overrideProps, configOverrides);
/*     */ 
/* 170 */     loadStateCounters();
/* 171 */     if (!this.m_isRestart)
/*     */     {
/* 173 */       this.m_state.putLocal("IsRebuild", (this.m_isRebuild) ? "Yes" : "No");
/* 174 */       this.m_state.putLocal("CurrentFinishedSymbol", this.m_currentFinishedSymbol);
/* 175 */       this.m_state.putLocal("startDate", LocaleUtils.encodeMessage("csDateMessage", null, new Date()));
/*     */ 
/* 177 */       putStateCounters();
/*     */ 
/* 179 */       state = "Init";
/* 180 */       if (!readOnly)
/*     */       {
/* 182 */         doStateTransition(state);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 187 */       this.m_isRebuild = StringUtils.convertToBool(this.m_state.getLocal("IsRebuild"), false);
/* 188 */       this.m_currentFinishedSymbol = this.m_state.getLocal("CurrentFinishedSymbol");
/* 189 */       if (this.m_currentFinishedSymbol == null)
/*     */       {
/* 191 */         this.m_currentFinishedSymbol = " ";
/*     */       }
/*     */     }
/*     */ 
/* 195 */     return this.m_cycleId;
/*     */   }
/*     */ 
/*     */   protected void addCycle(DataResultSet rset) throws DataException
/*     */   {
/* 200 */     FieldInfo[] infos = ResultSetUtils.createInfoList(rset, m_cycleFields, true);
/*     */ 
/* 202 */     Vector row = new IdcVector();
/* 203 */     row.setSize(infos.length);
/* 204 */     row.setElementAt(this.m_cycleId, infos[0].m_index);
/* 205 */     row.setElementAt(this.m_cycleWhereClause, infos[1].m_index);
/* 206 */     row.setElementAt(this.m_cycleDescription, infos[2].m_index);
/* 207 */     row.setElementAt((this.m_isRebuild) ? "1" : "0", infos[3].m_index);
/* 208 */     row.setElementAt("", infos[4].m_index);
/* 209 */     rset.addRow(row);
/* 210 */     rset.last();
/*     */   }
/*     */ 
/*     */   public boolean isRebuild()
/*     */   {
/* 215 */     return this.m_isRebuild;
/*     */   }
/*     */ 
/*     */   public String getCurrentState()
/*     */   {
/* 220 */     String state = this.m_state.getLocal("IndexerState");
/* 221 */     return state;
/*     */   }
/*     */ 
/*     */   public void doStateTransition(String state) throws ServiceException
/*     */   {
/* 226 */     this.m_state.putLocal("IndexerState", state);
/* 227 */     if (state.equals("Finished"))
/*     */     {
/* 229 */       this.m_state.putLocal("finishDate", LocaleUtils.encodeMessage("csDateMessage", null, new Date()));
/*     */ 
/* 231 */       this.m_state.putLocal("timeCompleted", "" + System.currentTimeMillis());
/*     */     }
/* 233 */     saveState();
/*     */   }
/*     */ 
/*     */   public void saveCycleTable() throws ServiceException
/*     */   {
/* 238 */     FileUtils.reserveDirectory(this.m_indexDirectory);
/*     */     try
/*     */     {
/* 241 */       ResourceUtils.serializeDataBinder(this.m_indexDirectory, "indexercycles.hda", this.m_cycleList, true, true);
/*     */     }
/*     */     finally
/*     */     {
/* 246 */       FileUtils.releaseDirectory(this.m_indexDirectory);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void loadStateCounters()
/*     */   {
/* 252 */     this.m_totalAddIndex = getStateInteger("totalAddIndex", this.m_isRestart);
/* 253 */     this.m_totalDeleteIndex = getStateInteger("totalDeleteIndex", this.m_isRestart);
/* 254 */     this.m_totalFullTextAdd = getStateInteger("totalFullTextAdd", this.m_isRestart);
/* 255 */     this.m_totalDummyTextAdd = getStateInteger("totalDummyTextAdd", this.m_isRestart);
/* 256 */     this.m_totalAddBatchloads = getStateInteger("totalAddBatchloads", this.m_isRestart);
/* 257 */     this.m_totalDeleteBatchloads = getStateInteger("totalDeleteBatchloads", this.m_isRestart);
/* 258 */     this.m_errCount = 0;
/* 259 */     this.m_totalErrCount = getStateInteger("totalErrCount", this.m_isRestart);
/*     */   }
/*     */ 
/*     */   protected void putStateCounters()
/*     */   {
/* 264 */     putStateInteger("totalAddIndex", this.m_totalAddIndex);
/* 265 */     putStateInteger("totalDeleteIndex", this.m_totalDeleteIndex);
/* 266 */     putStateInteger("totalFullTextAdd", this.m_totalFullTextAdd);
/* 267 */     putStateInteger("totalDummyTextAdd", this.m_totalDummyTextAdd);
/* 268 */     putStateInteger("totalAddBatchloads", this.m_totalAddBatchloads);
/* 269 */     putStateInteger("totalDeleteBatchloads", this.m_totalDeleteBatchloads);
/* 270 */     putStateInteger("errCount", this.m_errCount);
/* 271 */     putStateInteger("totalErrCount", this.m_totalErrCount);
/*     */   }
/*     */ 
/*     */   protected int getStateInteger(String key, boolean restoreState)
/*     */   {
/* 277 */     if (!restoreState)
/*     */     {
/* 279 */       return 0;
/*     */     }
/* 281 */     return DataBinderUtils.getInteger(this.m_state, key, 0);
/*     */   }
/*     */ 
/*     */   protected void putStateInteger(String key, int value)
/*     */   {
/* 286 */     this.m_state.putLocal(key, "" + value);
/*     */   }
/*     */ 
/*     */   public String getStateValue(String key)
/*     */   {
/* 291 */     return this.m_state.getLocal(key);
/*     */   }
/*     */ 
/*     */   public void setStateValue(String key, String value)
/*     */   {
/* 296 */     this.m_state.putLocal(key, value);
/*     */   }
/*     */ 
/*     */   public void loadCollectionCounters()
/*     */   {
/* 303 */     boolean newStart = DataBinderUtils.getBoolean(this.m_collectionValues, "isNewStart", false);
/* 304 */     this.m_cumTotalAddIndex = getCollectionInteger("cumTotalAddIndex", !newStart);
/* 305 */     this.m_cumTotalDeleteIndex = getCollectionInteger("cumTotalDeleteIndex", !newStart);
/* 306 */     this.m_cumTotalFullTextAdd = getCollectionInteger("cumTotalFullTextAdd", !newStart);
/* 307 */     this.m_cumTotalDummyTextAdd = getCollectionInteger("cumTotalDummyTextAdd", !newStart);
/* 308 */     this.m_cumTotalAddBatchloads = getCollectionInteger("cumTotalAddBatchloads", !newStart);
/* 309 */     this.m_cumTotalDeleteBatchloads = getCollectionInteger("cumTotalDeleteBatchloads", !newStart);
/* 310 */     this.m_cumTotalFileSize = getCollectionInteger("cumTotalFileSize", !newStart);
/* 311 */     this.m_cumTotalErrCount = getCollectionInteger("cumTotalErrCount", !newStart);
/*     */   }
/*     */ 
/*     */   public void putCollectionCounters()
/*     */   {
/* 316 */     putCollectionInteger("cumTotalAddIndex", this.m_cumTotalAddIndex);
/* 317 */     putCollectionInteger("cumTotalDeleteIndex", this.m_cumTotalDeleteIndex);
/* 318 */     putCollectionInteger("cumTotalFullTextAdd", this.m_cumTotalFullTextAdd);
/* 319 */     putCollectionInteger("cumTotalDummyTextAdd", this.m_cumTotalDummyTextAdd);
/* 320 */     putCollectionInteger("cumTotalAddBatchloads", this.m_cumTotalAddBatchloads);
/* 321 */     putCollectionInteger("cumTotalDeleteBatchloads", this.m_cumTotalDeleteBatchloads);
/* 322 */     putCollectionInteger("cumTotalFileSize", this.m_cumTotalFileSize);
/* 323 */     putCollectionInteger("cumTotalErrCount", this.m_cumTotalErrCount);
/*     */   }
/*     */ 
/*     */   public DataBinder getCollectionValues()
/*     */   {
/* 328 */     return this.m_collectionValues;
/*     */   }
/*     */ 
/*     */   public int getCollectionInteger(String key, boolean restoreState)
/*     */   {
/* 333 */     if (!restoreState)
/*     */     {
/* 335 */       this.m_collectionValues.putLocal(key, "0");
/* 336 */       return 0;
/*     */     }
/* 338 */     return DataBinderUtils.getInteger(this.m_collectionValues, key, 0);
/*     */   }
/*     */ 
/*     */   public void putCollectionInteger(String key, int value)
/*     */   {
/* 343 */     this.m_collectionValues.putLocal(key, "" + value);
/*     */   }
/*     */ 
/*     */   public String getCollectionValue(String key)
/*     */   {
/* 348 */     return this.m_collectionValues.getLocal(key);
/*     */   }
/*     */ 
/*     */   public void setCollectionValue(String key, String value)
/*     */   {
/* 353 */     this.m_collectionValues.putLocal(key, value);
/*     */   }
/*     */ 
/*     */   public void saveState() throws ServiceException
/*     */   {
/* 358 */     putStateCounters();
/*     */ 
/* 367 */     int firstDelay = 10;
/* 368 */     int delay = 10;
/* 369 */     int attempts = 3;
/* 370 */     boolean success = false;
/* 371 */     Exception theException = null;
/* 372 */     while ((attempts-- > 0) && (!success))
/*     */     {
/* 374 */       success = true;
/*     */       try
/*     */       {
/* 377 */         if ((attempts == 0) && (FileUtils.checkFile(this.m_workDir + "/state.hda", true, true) != 0))
/*     */         {
/* 385 */           if (SystemUtils.m_verbose)
/*     */           {
/* 387 */             Report.debug("indexer", "last chance write in " + this.m_workDir, null);
/*     */           }
/*     */ 
/* 390 */           Writer w = FileUtils.openDataWriter(this.m_workDir, "state.hda");
/* 391 */           this.m_state.send(w);
/* 392 */           w.close();
/*     */         }
/*     */         else
/*     */         {
/* 396 */           ResourceUtils.serializeDataBinder(this.m_workDir, "state.hda", this.m_state, true, false);
/*     */         }
/*     */ 
/* 401 */         theException = null;
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 405 */         theException = e;
/* 406 */         Report.trace("indexer", null, theException);
/* 407 */         SystemUtils.sleep(delay);
/* 408 */         delay *= firstDelay;
/* 409 */         success = false;
/*     */       }
/*     */     }
/* 412 */     if (theException != null)
/*     */     {
/* 414 */       if (theException instanceof ServiceException)
/*     */       {
/* 416 */         throw ((ServiceException)theException);
/*     */       }
/* 418 */       Report.trace("indexer", null, theException);
/* 419 */       String msg = theException.getMessage();
/* 420 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 423 */     IndexerMonitor.notifyStatusChange();
/*     */   }
/*     */ 
/*     */   public void mergeStateToState(String cycleID, DataBinder newStateVals, String[] excludedValues)
/*     */     throws ServiceException
/*     */   {
/* 429 */     String workDir = this.m_indexDirectory + cycleID + "/";
/* 430 */     FileUtils.checkOrCreateDirectory(workDir, 1);
/* 431 */     DataBinder temp = new DataBinder();
/* 432 */     ResourceUtils.serializeDataBinder(workDir, "state.hda", temp, false, false);
/* 433 */     Properties props = newStateVals.getLocalData();
/* 434 */     for (Enumeration e = props.keys(); e.hasMoreElements(); )
/*     */     {
/* 436 */       String key = (String)e.nextElement();
/* 437 */       if (StringUtils.findStringIndex(excludedValues, key) < 0)
/*     */       {
/* 439 */         String val = props.getProperty(key);
/* 440 */         temp.putLocal(key, val);
/*     */       }
/*     */     }
/* 443 */     ResourceUtils.serializeDataBinder(workDir, "state.hda", temp, true, true);
/*     */   }
/*     */ 
/*     */   protected boolean[] getUsedSymbols(DataBinder binder, Hashtable symbolTable)
/*     */     throws ServiceException
/*     */   {
/* 450 */     boolean[] usedSymbols = new boolean[''];
/* 451 */     DataResultSet rset = (DataResultSet)binder.getResultSet("IndexerStateSymbols");
/* 452 */     if (rset == null)
/*     */     {
/* 454 */       throw new ServiceException("!csIndexerStateSymbolsMissing");
/*     */     }
/*     */     FieldInfo[] infos;
/*     */     try
/*     */     {
/* 459 */       infos = ResultSetUtils.createInfoList(rset, new String[] { "id", "symbol" }, true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 464 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 467 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 469 */       String id = rset.getStringValue(infos[0].m_index);
/* 470 */       String symbol = rset.getStringValue(infos[1].m_index);
/*     */ 
/* 472 */       symbolTable.put(id, symbol);
/* 473 */       char symChar = symbol.charAt(0);
/* 474 */       usedSymbols[(symChar - 'A')] = true;
/*     */     }
/*     */ 
/* 477 */     return usedSymbols;
/*     */   }
/*     */ 
/*     */   public String computeFinishedSymbol(String descript) throws ServiceException
/*     */   {
/* 482 */     FileUtils.reserveDirectory(this.m_indexDirectory);
/*     */     try
/*     */     {
/* 485 */       DataBinder binder = new DataBinder();
/* 486 */       boolean exists = ResourceUtils.serializeDataBinder(this.m_indexDirectory, "cyclesymbols.hda", binder, false, false);
/*     */       DataResultSet drset;
/* 489 */       if (!exists)
/*     */       {
/* 491 */         DataResultSet drset = new DataResultSet(new String[] { "id", "symbol" });
/* 492 */         binder.addResultSet("IndexerStateSymbols", drset);
/*     */       }
/*     */       else
/*     */       {
/* 496 */         drset = (DataResultSet)binder.getResultSet("IndexerStateSymbols");
/*     */       }
/*     */ 
/* 499 */       Hashtable symbolTable = new Hashtable();
/* 500 */       boolean[] usedSymbols = getUsedSymbols(binder, symbolTable);
/*     */ 
/* 502 */       String id = this.m_cycleId + ":" + descript;
/* 503 */       String symbol = (String)symbolTable.get(id);
/* 504 */       if (symbol == null)
/*     */       {
/* 506 */         for (i = 0; i < usedSymbols.length; ++i)
/*     */         {
/* 508 */           if (usedSymbols[i] != 0) {
/*     */             continue;
/*     */           }
/* 510 */           char tmp = (char)(65 + i);
/* 511 */           StringBuffer buffer = new StringBuffer();
/* 512 */           buffer.append(tmp);
/* 513 */           symbol = buffer.toString();
/*     */           FieldInfo[] infos;
/*     */           try
/*     */           {
/* 518 */             infos = ResultSetUtils.createInfoList(drset, new String[] { "id", "symbol" }, true);
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 523 */             throw new ServiceException(e);
/*     */           }
/* 525 */           Vector row = new IdcVector();
/* 526 */           row.setSize(2);
/* 527 */           row.setElementAt(this.m_cycleId + ":" + descript, infos[0].m_index);
/* 528 */           row.setElementAt(symbol, infos[1].m_index);
/* 529 */           drset.addRow(row);
/* 530 */           ResourceUtils.serializeDataBinder(this.m_indexDirectory, "cyclesymbols.hda", binder, true, false);
/*     */ 
/* 532 */           break;
/*     */         }
/*     */       }
/*     */ 
/* 536 */       int i = symbol;
/*     */ 
/* 540 */       return i; } finally { FileUtils.releaseDirectory(this.m_indexDirectory); }
/*     */ 
/*     */   }
/*     */ 
/*     */   public void freeFinishedSymbol(String descript)
/*     */     throws ServiceException
/*     */   {
/* 548 */     if (!SharedObjects.getEnvValueAsBoolean("IndexerReleaseStateSymbols", false))
/*     */       return;
/*     */     try
/*     */     {
/* 552 */       FileUtils.reserveDirectory(this.m_indexDirectory);
/* 553 */       DataBinder binder = new DataBinder();
/* 554 */       boolean exists = ResourceUtils.serializeDataBinder(this.m_indexDirectory, "cyclesymbols.hda", binder, false, false);
/*     */ 
/* 556 */       if (!exists)
/*     */       {
/*     */         return;
/*     */       }
/*     */ 
/* 561 */       String id = this.m_cycleId + ":" + descript;
/* 562 */       DataResultSet drset = (DataResultSet)binder.getResultSet("IndexerStateSymbols");
/* 563 */       if (drset != null)
/*     */       {
/* 565 */         FieldInfo info = new FieldInfo();
/* 566 */         if (!drset.getFieldInfo("id", info))
/*     */         {
/* 568 */           throw new ServiceException(LocaleUtils.encodeMessage("csUnableToFindField", null, "id", "IndexerStateSymbols"));
/*     */         }
/*     */ 
/* 571 */         if (drset.findRow(info.m_index, id) != null)
/*     */         {
/* 573 */           drset.deleteCurrentRow();
/*     */         }
/*     */ 
/* 576 */         ResourceUtils.serializeDataBinder(this.m_indexDirectory, "cyclesymbols.hda", binder, true, false);
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 582 */       FileUtils.releaseDirectory(this.m_indexDirectory);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setFinishedSymbol(String symbol)
/*     */     throws ServiceException
/*     */   {
/* 589 */     this.m_currentFinishedSymbol = symbol;
/* 590 */     this.m_state.putLocal("CurrentFinishedSymbol", symbol);
/*     */   }
/*     */ 
/*     */   public String getFinishedSymbol()
/*     */   {
/* 595 */     return this.m_currentFinishedSymbol;
/*     */   }
/*     */ 
/*     */   public String getWhereClause()
/*     */   {
/* 600 */     return this.m_cycleWhereClause;
/*     */   }
/*     */ 
/*     */   public int nonIdleCycleCount()
/*     */   {
/* 605 */     return IndexerMonitor.nonIdleCycleCount();
/*     */   }
/*     */ 
/*     */   public void requestExclusiveLock() throws ServiceException
/*     */   {
/* 610 */     if (this.m_lockObject != null)
/*     */     {
/* 612 */       return;
/*     */     }
/*     */ 
/* 615 */     this.m_lockObject = new boolean[1];
/* 616 */     IndexerMonitor.requestExclusiveLock(this.m_cycleId, this.m_lockObject);
/*     */   }
/*     */ 
/*     */   public boolean waitForExclusiveLock(int timeInSeconds)
/*     */   {
/* 621 */     int progressNotifyRate = 30;
/* 622 */     int loopCount = timeInSeconds / progressNotifyRate;
/* 623 */     int lastWaitCount = timeInSeconds % progressNotifyRate;
/* 624 */     int waitTime = progressNotifyRate;
/* 625 */     while ((this.m_lockObject[0] == 0) && (loopCount-- >= 0))
/*     */     {
/* 628 */       if ((loopCount == 0) && (((timeInSeconds == 0) || (lastWaitCount != 0))))
/*     */       {
/* 630 */         waitTime = lastWaitCount;
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 635 */         synchronized (this.m_lockObject)
/*     */         {
/* 637 */           if ((this.m_lockObject[0] == 0) && 
/* 640 */             (waitTime > 0))
/*     */           {
/* 642 */             this.m_lockObject.wait(waitTime * 1000);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (InterruptedException ignore)
/*     */       {
/* 649 */         Report.trace("indexer", null, ignore);
/*     */       }
/*     */     }
/*     */ 
/* 653 */     return this.m_lockObject[0];
/*     */   }
/*     */ 
/*     */   public void releaseExclusiveLock()
/*     */   {
/* 658 */     IndexerMonitor.releaseExclusiveLock(this.m_cycleId);
/*     */ 
/* 661 */     this.m_lockObject = null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 666 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98170 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerState
 * JD-Core Version:    0.5.4
 */