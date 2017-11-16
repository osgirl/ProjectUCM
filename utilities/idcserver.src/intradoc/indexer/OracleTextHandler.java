/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcStringBuilder;
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
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.sql.BatchUpdateException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class OracleTextHandler extends IndexerExecutionHandler
/*     */ {
/*     */   protected Workspace m_idxWorkspace;
/*     */   protected DataResultSet m_queryTable;
/*     */   protected FieldInfo[] m_metaFields;
/*     */   protected ArrayList m_dateFields;
/*     */   protected ArrayList<String> m_textSdataFields;
/*     */   protected String m_tracingSection;
/*     */   protected String[] m_drillDownFields;
/*     */   protected String m_parallelDegree;
/*     */   protected boolean m_hasIndexedDoc;
/*     */   protected boolean m_useLog;
/*     */   protected String m_defaultNullValue;
/*     */   protected String m_defaultTrueValue;
/*     */   protected int m_maxSize;
/*     */   protected int m_maxSdataSize;
/*     */   protected boolean m_hasError;
/*     */ 
/*     */   public OracleTextHandler()
/*     */   {
/*  37 */     this.m_tracingSection = "indexer";
/*     */ 
/*  39 */     this.m_drillDownFields = null;
/*     */ 
/*  41 */     this.m_parallelDegree = null;
/*     */ 
/*  45 */     this.m_defaultNullValue = "idcnull";
/*  46 */     this.m_defaultTrueValue = "idcContentTrue";
/*  47 */     this.m_maxSize = 2000;
/*  48 */     this.m_maxSdataSize = 249;
/*     */ 
/*  50 */     this.m_hasError = false;
/*     */   }
/*     */ 
/*     */   public void init(IndexerExecution exec) throws ServiceException
/*     */   {
/*  55 */     super.init(exec);
/*  56 */     this.m_idxWorkspace = OracleTextUtils.getWorkspace(this.m_data);
/*     */ 
/*  58 */     loadMetaFields();
/*  59 */     String debugLevel = this.m_config.getValue("IndexerDebugLevel");
/*  60 */     if (this.m_data.m_debugLevel != null)
/*     */     {
/*  62 */       debugLevel = this.m_data.m_debugLevel;
/*     */     }
/*  64 */     if ((debugLevel != null) && 
/*  66 */       (!debugLevel.equalsIgnoreCase("none")))
/*     */     {
/*  68 */       this.m_tracingSection = "indexer";
/*  69 */       if (debugLevel.equalsIgnoreCase("debug"))
/*     */       {
/*  71 */         this.m_useLog = true;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  76 */     if (this.m_drillDownFields == null)
/*     */     {
/*  78 */       String drillDownFields = SharedObjects.getEnvironmentValue("DrillDownFields");
/*  79 */       if (drillDownFields == null)
/*     */       {
/*  81 */         drillDownFields = this.m_config.getValue("DrillDownFields");
/*     */       }
/*  83 */       if (drillDownFields == null)
/*     */       {
/*  85 */         drillDownFields = "dDocType,dSecurityGroup,dDocAccount";
/*     */       }
/*  87 */       this.m_drillDownFields = StringUtils.makeStringArrayFromSequenceEx(drillDownFields, ',', '^', 32);
/*     */     }
/*     */ 
/*  90 */     this.m_parallelDegree = this.m_config.getConfigValue("OracleTextParallelDegree");
/*     */ 
/*  92 */     this.m_defaultNullValue = this.m_config.getValue("DefaultNullValue");
/*  93 */     this.m_defaultTrueValue = this.m_config.getValue("DefaultTrueValue");
/*  94 */     this.m_maxSdataSize = this.m_config.getInteger("MaxSdataCharSize", this.m_maxSdataSize);
/*     */   }
/*     */ 
/*     */   protected FieldInfo[] getMetaFields(ArrayList dateFields)
/*     */   {
/*  99 */     Set set = this.m_data.m_collectionDef.m_fieldInfos.entrySet();
/* 100 */     FieldInfo[] metaFields = new FieldInfo[set.size()];
/* 101 */     Map.Entry[] entries = new Map.Entry[set.size()];
/* 102 */     set.toArray(entries);
/* 103 */     for (int i = 0; i < entries.length; ++i)
/*     */     {
/* 105 */       metaFields[i] = ((FieldInfo)entries[i].getValue());
/* 106 */       if (metaFields[i].m_type != 5)
/*     */         continue;
/* 108 */       dateFields.add(metaFields[i].m_name);
/*     */     }
/*     */ 
/* 111 */     return metaFields;
/*     */   }
/*     */ 
/*     */   protected void loadMetaFields()
/*     */   {
/* 116 */     if (this.m_dateFields != null)
/*     */     {
/* 118 */       return;
/*     */     }
/* 120 */     this.m_dateFields = new ArrayList();
/* 121 */     this.m_textSdataFields = new ArrayList();
/* 122 */     this.m_metaFields = getMetaFields(this.m_dateFields);
/* 123 */     OracleTextUtils.getTextSdataFields(this.m_data.m_collectionDef.m_fieldDesignMap, this.m_textSdataFields);
/*     */   }
/*     */ 
/*     */   public void prepareIndexDoc(Properties prop, IndexerInfo ii)
/*     */   {
/* 129 */     ii.m_indexStatus = 5;
/* 130 */     loadMetaFields();
/* 131 */     String fileName = prop.getProperty("DOC_FN");
/* 132 */     prop.put("otsContent", fileName);
/*     */ 
/* 135 */     String dDocName = prop.getProperty("dDocName");
/* 136 */     if (dDocName != null)
/*     */     {
/* 138 */       prop.put("dDocName", dDocName.toUpperCase());
/*     */     }
/*     */ 
/* 141 */     if (ii.m_isDelete) {
/*     */       return;
/*     */     }
/* 144 */     int size = this.m_dateFields.size();
/* 145 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 147 */       String key = (String)this.m_dateFields.get(i);
/* 148 */       String date = (String)prop.get(key);
/* 149 */       if ((date == null) || (date.length() == 0))
/*     */         continue;
/*     */       try
/*     */       {
/* 153 */         date = fixDate(date);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 157 */         SystemUtils.dumpException("indexer", e);
/*     */       }
/* 159 */       prop.put(key, date);
/*     */     }
/*     */ 
/* 163 */     IdcStringBuilder drillDownTag = OracleTextUtils.constructDrillDownTag(prop, this.m_drillDownFields, this.m_idxWorkspace, this.m_data.m_workspace, null, this.m_data, this.m_config);
/*     */ 
/* 165 */     IdcStringBuilder metaValue = OracleTextUtils.buildOtsMeta(prop, this.m_textSdataFields, drillDownTag, this.m_data.m_collectionDef.m_securityInfos, true, this.m_defaultNullValue, this.m_defaultTrueValue, this.m_maxSdataSize);
/*     */ 
/* 169 */     for (FieldInfo fi : this.m_metaFields)
/*     */     {
/* 171 */       if (fi.m_type != 6)
/*     */         continue;
/* 173 */       String value = prop.getProperty(fi.m_name);
/* 174 */       if ((value != null) && (value.length() == 0))
/*     */       {
/* 176 */         prop.put(fi.m_name, this.m_defaultNullValue);
/*     */       } else {
/* 178 */         if ((value == null) || (value.length() <= this.m_maxSize))
/*     */           continue;
/* 180 */         value = value.substring(0, this.m_maxSize);
/* 181 */         prop.put(fi.m_name, value);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 188 */     Object[] objs = { metaValue, prop, this.m_idxWorkspace, this.m_config };
/* 189 */     this.m_data.setCachedObject("OtsMetaValueObjs", objs);
/*     */     try
/*     */     {
/* 192 */       PluginFilters.filter("IndexingOtsMetaValueFilter", this.m_data.m_workspace, null, this.m_data);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 196 */       Report.trace("indexer", null, ignore);
/*     */     }
/*     */ 
/* 199 */     prop.put("otsMeta", metaValue.toString());
/*     */   }
/*     */ 
/*     */   public void executeIndexer(Vector list, Hashtable props)
/*     */     throws ServiceException
/*     */   {
/* 206 */     int size = list.size();
/* 207 */     ArrayList updateList = new ArrayList();
/* 208 */     ArrayList updateFullList = new ArrayList();
/* 209 */     ArrayList deleteList = new ArrayList();
/* 210 */     ArrayList insertList = new ArrayList();
/* 211 */     HashMap map = new HashMap();
/* 212 */     Map fullMap = new HashMap();
/* 213 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 215 */       IndexerInfo ii = (IndexerInfo)list.elementAt(i);
/* 216 */       if (ii.m_indexStatus != 5) {
/*     */         continue;
/*     */       }
/*     */ 
/* 220 */       boolean isDelete = ii.m_isDelete;
/*     */ 
/* 222 */       if (isDelete)
/*     */       {
/* 224 */         deleteList.add(ii);
/*     */       }
/* 228 */       else if (ii.m_isUpdate)
/*     */       {
/* 230 */         updateList.add(ii);
/*     */       }
/*     */       else
/*     */       {
/* 234 */         map.put(ii.m_indexKey, ii);
/*     */       }
/*     */ 
/* 237 */       fullMap.put(ii.m_indexKey, ii);
/*     */     }
/*     */ 
/* 240 */     int timeout = this.m_idxWorkspace.getThreadTimeout();
/* 241 */     this.m_idxWorkspace.setThreadTimeout(0);
/*     */     try
/*     */     {
/* 245 */       retrieveUpdatableRows(this.m_execution.m_activeCollectionId, insertList, updateFullList, map);
/* 246 */       this.m_hasIndexedDoc = false;
/* 247 */       updateDocuments("DidcText" + this.m_execution.m_activeCollectionId, deleteList, props);
/* 248 */       updateDocuments("UidcTextMetaOnly" + this.m_execution.m_activeCollectionId, updateList, props);
/* 249 */       updateDocuments("UidcTextFull" + this.m_execution.m_activeCollectionId, updateFullList, props);
/* 250 */       updateDocuments("IidcText" + this.m_execution.m_activeCollectionId, insertList, props);
/*     */ 
/* 252 */       if (this.m_hasIndexedDoc)
/*     */       {
/* 254 */         syncIndex(fullMap);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 268 */       this.m_idxWorkspace.setThreadTimeout(timeout);
/* 269 */       if (this.m_hasError)
/*     */       {
/* 272 */         this.m_idxWorkspace.releaseConnection();
/*     */       }
/* 274 */       this.m_hasError = false;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void syncIndex(Map<String, IndexerInfo> map) throws ServiceException
/*     */   {
/* 280 */     String tableName = OracleTextUtils.getTableName(this.m_execution.m_activeCollectionId, this.m_config);
/* 281 */     String indexName = OracleTextUtils.getIndexName(tableName, "", this.m_idxWorkspace);
/* 282 */     String parallels = this.m_config.getConfigValue("OracleTextIndexingParallelDegree");
/* 283 */     int maxTime = this.m_config.getInteger("OracleTextSyncMaxTime", 2147483647);
/* 284 */     String databaseVersion = this.m_idxWorkspace.getProperty("DatabaseVersion");
/* 285 */     DataBinder binder = new DataBinder();
/* 286 */     binder.putLocal("indexName", indexName.toUpperCase());
/* 287 */     binder.putLocal("tableName", tableName);
/* 288 */     if (parallels != null)
/*     */     {
/* 290 */       binder.putLocal("parallelDegree", parallels);
/*     */     }
/* 292 */     binder.putLocal("maxTime", "" + maxTime);
/*     */ 
/* 294 */     boolean markAllFailed = false;
/* 295 */     String msg = null;
/* 296 */     DataBinder tbinder = new DataBinder();
/*     */     try
/*     */     {
/* 299 */       if (this.m_useLog)
/*     */       {
/* 301 */         tbinder.putLocal("logFile", System.currentTimeMillis() + "_ocs_index.log");
/* 302 */         this.m_idxWorkspace.executeCallable("CtextStartLog", tbinder);
/*     */ 
/* 304 */         tbinder.putLocal("eventID", "1");
/* 305 */         this.m_idxWorkspace.executeCallable("CtextAddLogEvent", tbinder);
/* 306 */         tbinder.putLocal("eventID", "2");
/* 307 */         this.m_idxWorkspace.executeCallable("CtextAddLogEvent", tbinder);
/* 308 */         tbinder.putLocal("eventID", "4");
/* 309 */         this.m_idxWorkspace.executeCallable("CtextAddLogEvent", tbinder);
/*     */ 
/* 311 */         if (databaseVersion.compareTo("11.2") >= 0)
/*     */         {
/* 313 */           tbinder.putLocal("eventID", "8");
/* 314 */           tbinder.putLocal("errNumber", "-1");
/* 315 */           this.m_idxWorkspace.executeCallable("CtextAddLogEventWithErrorNumber", tbinder);
/*     */         }
/*     */       }
/* 318 */       this.m_idxWorkspace.execute("CoracleTextSyncIndex", binder);
/*     */     }
/*     */     catch (Exception t)
/*     */     {
/* 323 */       Report.trace(this.m_tracingSection, "csOracleTextSynchronizationFailed", e);
/* 324 */       markAllFailed = true;
/* 325 */       msg = LocaleUtils.encodeMessage("csOracleTextSynchronizationFailed", e.getMessage());
/*     */     }
/*     */     finally
/*     */     {
/*     */       try
/*     */       {
/* 331 */         if (this.m_useLog)
/*     */         {
/* 333 */           this.m_idxWorkspace.executeCallable("CtextEndLog", tbinder);
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 338 */         Report.trace(this.m_tracingSection, null, t);
/*     */       }
/*     */     }
/*     */ 
/* 342 */     boolean hasFailed = markAllFailed;
/* 343 */     if (!markAllFailed)
/*     */     {
/*     */       try
/*     */       {
/* 347 */         ResultSet errRset = this.m_idxWorkspace.createResultSet("QoracleTextErrorDocs", binder);
/* 348 */         while (errRset.isRowPresent())
/*     */         {
/* 350 */           hasFailed = true;
/* 351 */           String dDocName = ResultSetUtils.getValue(errRset, "dDocName");
/* 352 */           if ((dDocName != null) && (dDocName.length() != 0))
/*     */           {
/* 356 */             IndexerInfo ii = (IndexerInfo)map.get(dDocName.toLowerCase());
/* 357 */             String errDetails = ResultSetUtils.getValue(errRset, "OracleTextErrDetails");
/* 358 */             if (ii != null)
/*     */             {
/* 360 */               ii.m_indexStatus = 3;
/* 361 */               ii.m_indexError = errDetails;
/* 362 */               Report.trace(this.m_tracingSection, "Error occurred in content '" + dDocName + "', " + errDetails, null);
/*     */             }
/*     */             else
/*     */             {
/* 367 */               Report.trace(this.m_tracingSection, "Error occurred in content '" + dDocName + "'. The content, however, is not present in current batch. (" + errDetails + ")", null);
/*     */             }
/*     */           }
/* 370 */           errRset.next();
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 376 */         if (this.m_config.getBoolean("OracleTextMarkAllFailedWhenUnableCheckErrors", false))
/*     */         {
/* 378 */           markAllFailed = true;
/*     */         }
/* 380 */         Report.trace(this.m_tracingSection, "csOracleTextSyncErrorCheck", e);
/* 381 */         hasFailed = true;
/*     */       }
/*     */     }
/*     */ 
/* 385 */     if (markAllFailed)
/*     */     {
/* 387 */       for (Map.Entry e : map.entrySet())
/*     */       {
/* 389 */         IndexerInfo ii = (IndexerInfo)e.getValue();
/* 390 */         ii.m_indexStatus = 3;
/* 391 */         ii.m_indexError = msg;
/*     */       }
/*     */     }
/*     */ 
/* 395 */     if (!hasFailed) {
/*     */       return;
/*     */     }
/*     */     try
/*     */     {
/* 400 */       this.m_idxWorkspace.execute("DoracleTextClearErrors", binder);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 406 */       Report.trace(this.m_tracingSection, "Unable to clear ctx_user_index_errors", e);
/*     */     }
/* 408 */     this.m_hasError = true;
/*     */   }
/*     */ 
/*     */   public void updateDocuments(String query, ArrayList list, Hashtable propBundle)
/*     */   {
/* 414 */     if (list.size() == 0)
/*     */     {
/* 416 */       return;
/*     */     }
/* 418 */     int size = list.size();
/* 419 */     ArrayList bundle = new ArrayList();
/* 420 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 422 */       IndexerInfo ii = (IndexerInfo)list.get(i);
/* 423 */       Properties props = (Properties)propBundle.get(ii.m_indexKey);
/* 424 */       MapParameters params = new MapParameters(props);
/* 425 */       bundle.add(params);
/*     */     }
/*     */ 
/* 428 */     batchExecution(query, list, bundle);
/*     */   }
/*     */ 
/*     */   public void deleteDocuments(ArrayList list, Hashtable propBundle)
/*     */   {
/* 433 */     if (list.size() == 0)
/*     */     {
/* 435 */       return;
/*     */     }
/* 437 */     int size = list.size();
/* 438 */     ArrayList bundle = new ArrayList();
/* 439 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 441 */       IndexerInfo ii = (IndexerInfo)list.get(i);
/* 442 */       Properties props = (Properties)propBundle.get(ii.m_indexKey);
/* 443 */       MapParameters params = new MapParameters(props);
/* 444 */       bundle.add(params);
/*     */     }
/*     */ 
/* 447 */     batchExecution("DidcText", list, bundle);
/*     */   }
/*     */ 
/*     */   public void batchExecution(String query, ArrayList list, ArrayList bundle)
/*     */   {
/* 452 */     int index = 0;
/* 453 */     int size = list.size();
/* 454 */     boolean isInTran = false;
/*     */     try
/*     */     {
/* 457 */       this.m_idxWorkspace.beginTranEx(4);
/* 458 */       isInTran = true;
/* 459 */       while (index < size)
/*     */       {
/* 461 */         this.m_idxWorkspace.clearBatch();
/* 462 */         int batchItemCount = 0;
/* 463 */         for (int i = index; i < size; ++i)
/*     */         {
/* 465 */           IndexerInfo ii = (IndexerInfo)list.get(i);
/* 466 */           if (ii.m_indexStatus == 3)
/*     */           {
/* 468 */             if (i != index) {
/*     */               break;
/*     */             }
/* 471 */             ++index;
/*     */           }
/*     */           else
/*     */           {
/* 477 */             MapParameters params = (MapParameters)bundle.get(i);
/*     */             try
/*     */             {
/* 480 */               this.m_idxWorkspace.addBatch(query, params);
/*     */             }
/*     */             catch (DataException e)
/*     */             {
/* 484 */               Report.trace("indexer", "Unable to add batch:", e);
/* 485 */               ii.m_indexStatus = 3;
/* 486 */               ii.m_indexError = e.getMessage();
/* 487 */               break label155:
/*     */             }
/* 489 */             ++batchItemCount;
/*     */           }
/*     */         }
/* 492 */         label155: int[] updateCounts = null;
/* 493 */         if (batchItemCount > 0)
/*     */         {
/*     */           try
/*     */           {
/* 497 */             updateCounts = this.m_idxWorkspace.executeBatch();
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 501 */             Report.trace("indexer", "Unable to execute batch:", e);
/* 502 */             Throwable t = e.getCause();
/* 503 */             if (t instanceof BatchUpdateException)
/*     */             {
/* 505 */               updateCounts = ((BatchUpdateException)t).getUpdateCounts();
/*     */ 
/* 507 */               if (index + updateCounts.length < list.size())
/*     */               {
/* 509 */                 IndexerInfo ii = (IndexerInfo)list.get(index + updateCounts.length);
/* 510 */                 ii.m_indexStatus = 3;
/* 511 */                 ii.m_indexError = t.getMessage();
/*     */ 
/* 513 */                 Report.trace("indexer", "Batch update not all successful. Failed document: " + ii.m_indexKey + " Current index:" + index + ", Size: " + size + ", Updated: " + updateCounts.length, t);
/*     */               }
/*     */               else
/*     */               {
/* 519 */                 Report.trace("indexer", "Batch update not all successful.  Current index:" + index + ", Size: " + size + ", Updated: " + updateCounts.length, t);
/*     */               }
/*     */ 
/*     */             }
/*     */             else
/*     */             {
/* 526 */               throw e;
/*     */             }
/*     */           }
/* 529 */           for (int i = 0; i < updateCounts.length; ++i)
/*     */           {
/* 531 */             IndexerInfo ii = (IndexerInfo)list.get(i + index);
/* 532 */             if ((updateCounts[i] == 1) || (updateCounts[i] == -2))
/*     */             {
/* 534 */               ii.m_indexStatus = 0;
/* 535 */               this.m_hasIndexedDoc = true;
/*     */             }
/* 537 */             else if (updateCounts[i] == -3)
/*     */             {
/* 539 */               ii.m_indexStatus = 3;
/* 540 */               ii.m_indexError = "csIndexerExecutionFailure";
/*     */             }
/*     */             else
/*     */             {
/* 544 */               ii.m_indexStatus = 3;
/* 545 */               String msg = LocaleUtils.encodeMessage("csIndexerUpdateCountError", null, "" + updateCounts[i]);
/* 546 */               ii.m_indexError = msg;
/*     */             }
/*     */           }
/* 549 */           index += updateCounts.length;
/*     */         }
/*     */       }
/*     */ 
/* 553 */       this.m_idxWorkspace.commitTran();
/* 554 */       isInTran = false;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 559 */       SystemUtils.err(t, "csIndexerUnExpectedError");
/* 560 */       for (int i = index; i < size; ++i)
/*     */       {
/* 562 */         IndexerInfo ii = (IndexerInfo)list.get(i);
/* 563 */         ii.m_indexStatus = 3;
/* 564 */         ii.m_indexError = t.getMessage();
/*     */       }
/*     */       try
/*     */       {
/* 568 */         if (isInTran)
/*     */         {
/* 570 */           this.m_idxWorkspace.commitTran();
/*     */         }
/*     */       }
/*     */       catch (Throwable ignore)
/*     */       {
/* 575 */         SystemUtils.trace(null, "Unable to commit transaction  when unexpected error occurred:");
/* 576 */         SystemUtils.dumpException(null, t);
/*     */ 
/* 579 */         for (int i = 0; i < index; ++i)
/*     */         {
/* 582 */           IndexerInfo ii = (IndexerInfo)list.get(i);
/* 583 */           ii.m_indexStatus = 3;
/* 584 */           ii.m_indexError = "csIndexerUnableToCommit";
/*     */         }
/*     */ 
/* 587 */         this.m_idxWorkspace.rollbackTran();
/*     */       }
/* 589 */       this.m_hasError = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void retrieveUpdatableRows(String collectionID, ArrayList inserts, ArrayList updates, HashMap map)
/*     */     throws ServiceException, DataException
/*     */   {
/* 596 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 597 */     boolean isFirst = true;
/* 598 */     for (Iterator i$ = map.entrySet().iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*     */ 
/* 600 */       IndexerInfo ii = (IndexerInfo)((Map.Entry)obj).getValue();
/* 601 */       if (!isFirst)
/*     */       {
/* 603 */         builder.append(',');
/*     */       }
/* 605 */       if ((ii.m_indexKey == null) || (ii.m_indexKey.trim().length() <= 0))
/*     */         continue;
/* 607 */       isFirst = false;
/*     */ 
/* 613 */       String value = StringUtils.addEscapeChars(ii.m_indexKey, ',', '^');
/* 614 */       builder.append(value.toUpperCase()); }
/*     */ 
/*     */ 
/* 617 */     ResultSet result = new DataResultSet();
/* 618 */     if (!isFirst)
/*     */     {
/* 620 */       DataBinder binder = new DataBinder();
/* 621 */       String tableName = getTableName(collectionID);
/* 622 */       binder.putLocal("tableName", tableName);
/* 623 */       binder.putLocal("docNameList", builder.toString());
/* 624 */       result = this.m_idxWorkspace.createResultSet("QdocumentUpdatable", binder);
/*     */     }
/*     */ 
/* 627 */     for (; result.isRowPresent(); result.next())
/*     */     {
/* 629 */       String docName = ResultSetUtils.getValue(result, "dDocName");
/* 630 */       IndexerInfo ii = (IndexerInfo)map.remove(docName.toLowerCase());
/* 631 */       updates.add(ii);
/*     */     }
/* 633 */     for (Iterator i$ = map.entrySet().iterator(); i$.hasNext(); ) { Object entry = i$.next();
/*     */ 
/* 635 */       inserts.add(((Map.Entry)entry).getValue()); }
/*     */ 
/*     */   }
/*     */ 
/*     */   protected String getTableName(String collectionID)
/*     */     throws ServiceException
/*     */   {
/* 643 */     DataResultSet drset = this.m_config.getTable("IndexerTableNames");
/*     */     try
/*     */     {
/* 646 */       String tableName = ResultSetUtils.findValue(drset, "CollectionID", collectionID, "TableName");
/* 647 */       return tableName;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 651 */       throw new ServiceException(LocaleUtils.encodeMessage("csIndexerDBFullTextCanNotCreateTableOrIndex", null, collectionID), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String fixDate(String s)
/*     */     throws ServiceException
/*     */   {
/*     */     Date date;
/*     */     try
/*     */     {
/* 662 */       date = LocaleResources.m_bulkloadFormat.parseDate(s);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 666 */       date = LocaleResources.parseDate(s, null);
/*     */     }
/* 668 */     return LocaleUtils.formatODBC(date);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 673 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 88912 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.OracleTextHandler
 * JD-Core Version:    0.5.4
 */