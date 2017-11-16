/*      */ package intradoc.indexer;
/*      */ 
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataStreamValue;
/*      */ import intradoc.data.DatabaseTypes;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.FieldInfoUtils;
/*      */ import intradoc.data.MapParameters;
/*      */ import intradoc.data.QueryUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.server.jobs.JobState;
/*      */ import intradoc.server.jobs.ScheduledJobManager;
/*      */ import intradoc.server.jobs.ScheduledJobStorage;
/*      */ import intradoc.server.jobs.ScheduledJobUtils;
/*      */ import intradoc.server.jobs.ScheduledJobsProcessor;
/*      */ import intradoc.shared.ActiveIndexState;
/*      */ import intradoc.shared.IndexerCollectionData;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SearchFieldInfo;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.Reader;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Calendar;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class OracleTextCollectionHandler extends CollectionHandlerImpl
/*      */ {
/*   30 */   public static String m_defaultNullValue = "idcnull";
/*      */   protected Workspace m_idxWorkspace;
/*      */   protected String m_tracingSection;
/*      */   protected String m_shadowIndexName;
/*      */   protected boolean m_hasWarnedOptimizationError;
/*      */   protected boolean m_hasWarnedOptimizationTaskError;
/*      */   protected boolean m_isFastRebuild;
/*      */   protected String[] m_deletedColumns;
/*      */   protected String m_fieldOrders;
/*      */   protected int SDATA;
/*      */   protected int TEXT;
/*      */ 
/*      */   public OracleTextCollectionHandler()
/*      */   {
/*   31 */     this.m_idxWorkspace = null;
/*   32 */     this.m_tracingSection = "indexer.oracletextsearch";
/*   33 */     this.m_shadowIndexName = null;
/*   34 */     this.m_hasWarnedOptimizationError = false;
/*   35 */     this.m_hasWarnedOptimizationTaskError = false;
/*   36 */     this.m_isFastRebuild = false;
/*      */ 
/*   38 */     this.m_deletedColumns = null;
/*   39 */     this.m_fieldOrders = null;
/*      */ 
/*   41 */     this.SDATA = 0;
/*   42 */     this.TEXT = 1;
/*      */   }
/*      */ 
/*      */   public void init(IndexerWorkObject data, IndexerCollectionManager manager) throws ServiceException
/*      */   {
/*   47 */     super.init(data, manager);
/*   48 */     this.m_idxWorkspace = OracleTextUtils.getWorkspace(data);
/*      */ 
/*   50 */     if ((this.m_idxWorkspace != null) && (!WorkspaceUtils.isDatabaseType(this.m_idxWorkspace, DatabaseTypes.ORACLE)))
/*      */     {
/*   52 */       String msg = LocaleUtils.encodeMessage("csIndexerDatabaseNotSupported", null, this.m_idxWorkspace.getProperty("DatabaseName"));
/*   53 */       throw new ServiceException(msg);
/*      */     }
/*   55 */     if (this.m_idxWorkspace == null)
/*      */     {
/*   57 */       Report.trace("indexer", "Workspace is not found for indexer", null);
/*      */     }
/*   59 */     this.m_isFastRebuild = DataBinderUtils.getBoolean(data.m_state.m_state, "fastRebuild", false);
/*   60 */     if (this.m_isFastRebuild)
/*      */     {
/*   62 */       this.m_activeIndex = CollectionHandlerUtils.getActiveIndex(false, this.m_collections);
/*      */     }
/*   64 */     Object[] fields = (Object[])(Object[])SharedObjects.getObject("OracleTextSearch", "Fields" + this.m_activeIndex);
/*   65 */     if (fields == null)
/*      */     {
/*   67 */       cacheFields(null);
/*      */     }
/*      */ 
/*   70 */     this.m_fieldOrders = this.m_config.getValue("MetaFieldsOrderList");
/*      */ 
/*   74 */     if ((this.m_isRebuild) || ((this.m_idxWorkspace != null) && (!OracleTextUtils.getWorkspaceProviderName(data).equalsIgnoreCase("SystemDatabase"))))
/*      */     {
/*   77 */       OracleTextUtils.initColumnMappings(this.m_idxWorkspace);
/*      */     }
/*      */ 
/*   80 */     if ((this.m_idxWorkspace == null) || (this.m_activeIndex == null))
/*      */       return;
/*   82 */     initQueries(false, this.m_activeIndex);
/*      */   }
/*      */ 
/*      */   public boolean checkCollectionExistence()
/*      */     throws ServiceException
/*      */   {
/*   89 */     String tableName = OracleTextUtils.getTableName(this.m_activeIndex, this.m_config);
/*   90 */     boolean doesExist = WorkspaceUtils.doesTableExist(this.m_idxWorkspace, tableName, null);
/*   91 */     if (this.m_isRebuild)
/*      */     {
/*   93 */       doesExist = compareCollectionDesign(this.m_data.m_collectionDef);
/*      */     }
/*   95 */     return doesExist;
/*      */   }
/*      */ 
/*      */   public boolean isCollectionUpToDate(IndexerWorkObject data)
/*      */     throws ServiceException
/*      */   {
/*  101 */     boolean isSame = true;
/*  102 */     Boolean createdCollection = (Boolean)data.getCachedObject("CollectionDesignUpToDate");
/*  103 */     if (createdCollection != null)
/*      */     {
/*  105 */       isSame = createdCollection.booleanValue();
/*      */     }
/*      */     else
/*      */     {
/*  109 */       if (!this.m_isRebuild)
/*      */       {
/*  111 */         IndexerCollectionData oldDef = new IndexerCollectionData();
/*  112 */         loadCollectionDesign(oldDef);
/*      */ 
/*  114 */         HashMap[] diffs = compareMaps(oldDef.m_fieldInfos, data.m_collectionDef.m_fieldInfos);
/*  115 */         if ((diffs[0] != null) || (diffs[1] != null) || (diffs[2] != null))
/*      */         {
/*  117 */           isSame = false;
/*      */         }
/*      */ 
/*  120 */         if (isSame)
/*      */         {
/*  122 */           diffs = compareMaps(oldDef.m_fieldDesignMap, data.m_collectionDef.m_fieldDesignMap);
/*  123 */           if ((diffs[0] != null) || (diffs[1] != null) || (diffs[2] != null))
/*      */           {
/*  125 */             isSame = false;
/*      */           }
/*      */         }
/*  128 */         String cid = CollectionHandlerUtils.getActiveIndex(this.m_isRebuild, this.m_collections);
/*  129 */         initQueries(false, cid);
/*      */       }
/*      */       else
/*      */       {
/*  133 */         isSame = false;
/*      */       }
/*  135 */       data.setCachedObject("CollectionDesignUpToDate", Boolean.valueOf(isSame));
/*      */     }
/*  137 */     return isSame;
/*      */   }
/*      */ 
/*      */   public HashMap[] compareMaps(Map oldMap, Map newMap)
/*      */   {
/*  142 */     HashMap[] diffs = new HashMap[3];
/*  143 */     int added = 0;
/*  144 */     int deleted = 1;
/*  145 */     for (Iterator i$ = oldMap.keySet().iterator(); i$.hasNext(); ) { Object key = i$.next();
/*      */ 
/*  147 */       Object value = newMap.get(key);
/*  148 */       Object oldValue = oldMap.get(key);
/*  149 */       if (value == null)
/*      */       {
/*  151 */         if (oldValue != null)
/*      */         {
/*  153 */           if (diffs[deleted] == null)
/*      */           {
/*  155 */             diffs[deleted] = new HashMap();
/*      */           }
/*  157 */           diffs[deleted].put(key, value);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  162 */         boolean isEqual = false;
/*  163 */         if ((value instanceof FieldInfo) && (oldValue instanceof FieldInfo))
/*      */         {
/*  165 */           isEqual = isExistingFieldValid((FieldInfo)oldValue, (FieldInfo)value);
/*      */         }
/*      */         else
/*      */         {
/*  169 */           isEqual = value.equals(oldValue);
/*      */         }
/*  171 */         if ((oldValue == null) || (!isEqual))
/*      */         {
/*  173 */           if (diffs[added] == null)
/*      */           {
/*  175 */             diffs[added] = new HashMap();
/*      */           }
/*  177 */           if (diffs[deleted] == null)
/*      */           {
/*  179 */             diffs[deleted] = new HashMap();
/*      */           }
/*  181 */           diffs[deleted].put(key, value);
/*  182 */           diffs[added].put(key, newMap.get(key));
/*      */         }
/*      */       } }
/*      */ 
/*      */ 
/*  187 */     for (Iterator i$ = newMap.keySet().iterator(); i$.hasNext(); ) { Object key = i$.next();
/*      */ 
/*  189 */       if (!oldMap.containsKey(key))
/*      */       {
/*  191 */         if (diffs[added] == null)
/*      */         {
/*  193 */           diffs[added] = new HashMap();
/*      */         }
/*  195 */         diffs[added].put(key, newMap.get(key));
/*      */       } }
/*      */ 
/*  198 */     return diffs;
/*      */   }
/*      */ 
/*      */   public boolean isExistingFieldValid(FieldInfo oldField, FieldInfo newField)
/*      */   {
/*  209 */     return (oldField.m_name.equals(newField.m_name)) && (oldField.m_type == newField.m_type) && (oldField.m_scale == newField.m_scale) && (((oldField.m_maxLen == 0) || (oldField.m_maxLen >= newField.m_maxLen))) && (oldField.m_index == newField.m_index);
/*      */   }
/*      */ 
/*      */   public String manageCollection(IndexerCollectionData def, IndexerWorkObject data)
/*      */     throws ServiceException
/*      */   {
/*  217 */     IndexerCollectionData oldDef = new IndexerCollectionData();
/*  218 */     loadCollectionDesign(oldDef);
/*      */ 
/*  220 */     String status = "CollectionUpToDate";
/*  221 */     boolean needNewCollection = !DataBinderUtils.getBoolean(data.m_state.m_state, "fastRebuild", false);
/*      */ 
/*  223 */     ArrayList deletedColumns = new ArrayList();
/*  224 */     ArrayList addedColumns = new ArrayList();
/*      */ 
/*  226 */     boolean hasNewSdata = false;
/*  227 */     if (!needNewCollection)
/*      */     {
/*  229 */       String tableName = OracleTextUtils.getTableName(this.m_activeIndex, this.m_config);
/*  230 */       needNewCollection = !WorkspaceUtils.doesTableExist(this.m_idxWorkspace, tableName, null);
/*      */     }
/*  232 */     if (!needNewCollection)
/*      */     {
/*  234 */       Map[] diffs = compareMaps(oldDef.m_fieldInfos, def.m_fieldInfos);
/*      */       Iterator i$;
/*  235 */       if (diffs[1] != null)
/*      */       {
/*  237 */         for (i$ = diffs[1].keySet().iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*      */ 
/*  239 */           deletedColumns.add((String)obj); }
/*      */ 
/*      */       }
/*      */       Iterator i$;
/*  242 */       if (diffs[0] != null)
/*      */       {
/*  246 */         for (i$ = diffs[0].entrySet().iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*      */ 
/*  248 */           FieldInfo fi = (FieldInfo)((Map.Entry)obj).getValue();
/*  249 */           if (fi.m_type == 5)
/*      */           {
/*  251 */             FieldInfoUtils.setFieldOption(fi, "DisableDBDateEnhancement", "1");
/*      */           }
/*  253 */           addedColumns.add(fi); }
/*      */ 
/*      */       }
/*      */ 
/*  257 */       hasNewSdata = checkNewSdata(oldDef.m_fieldDesignMap, def.m_fieldDesignMap);
/*      */     }
/*      */ 
/*  260 */     boolean needAdd = addedColumns.size() > 0;
/*  261 */     boolean needDelete = deletedColumns.size() > 0;
/*  262 */     if (needNewCollection)
/*      */     {
/*  265 */       createCollection(this.m_activeIndex);
/*  266 */       status = "DesignUpToDate";
/*      */     }
/*      */     else
/*      */     {
/*  270 */       this.m_activeIndex = CollectionHandlerUtils.getActiveIndex(false, this.m_collections);
/*      */ 
/*  272 */       String tableName = OracleTextUtils.getTableName(this.m_activeIndex, this.m_config);
/*  273 */       String indexName = OracleTextUtils.getIndexName(tableName, "", this.m_idxWorkspace).toUpperCase();
/*  274 */       String parallels = this.m_config.getConfigValue("OracleTextIndexingParallelDegree");
/*      */ 
/*  276 */       int timeout = this.m_idxWorkspace.getThreadTimeout();
/*  277 */       this.m_idxWorkspace.setThreadTimeout(0);
/*  278 */       if ((needAdd) || (needDelete))
/*      */       {
/*  280 */         FieldInfo[] fis = new FieldInfo[addedColumns.size()];
/*  281 */         addedColumns.toArray(fis);
/*  282 */         this.m_deletedColumns = new String[deletedColumns.size()];
/*  283 */         deletedColumns.toArray(this.m_deletedColumns);
/*      */         try
/*      */         {
/*  286 */           if (needAdd)
/*      */           {
/*  288 */             String msg = LocaleUtils.encodeMessage("csOracleTextAddingNewField", null, "" + fis.length);
/*  289 */             this.m_data.reportProgress(2, msg, 0.0F, -1.0F);
/*  290 */             this.m_idxWorkspace.alterTable(tableName, fis, null, new String[] { "dDocName" });
/*  291 */             updateIndexUsingBaseTable(tableName, fis, def, data);
/*      */           }
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*  296 */           String msg = LocaleUtils.encodeMessage("csUnableToModifyTextCollectionTable", null, tableName);
/*  297 */           throw new ServiceException(msg, e);
/*      */         }
/*      */       }
/*      */ 
/*  301 */       int totalCount = -2;
/*      */       try
/*      */       {
/*  304 */         totalCount = WorkspaceUtils.getRowCount(tableName, null, this.m_idxWorkspace);
/*      */       }
/*      */       catch (DataException ignore)
/*      */       {
/*  308 */         Report.debug("indexer", "Error retrieving totalCount for " + tableName, ignore);
/*      */       }
/*      */ 
/*  312 */       if (hasNewSdata)
/*      */       {
/*      */         try
/*      */         {
/*  316 */           String[] columnArr = WorkspaceUtils.getColumnList(tableName, this.m_idxWorkspace, new String[] { "otsContent", "otsMeta", "otsCounter" });
/*  317 */           String columns = StringUtils.createStringFromArray(columnArr);
/*  318 */           String fileSizeStr = this.m_config.getValue("FastRebuildBatchSize");
/*  319 */           int batchSize = NumberUtils.parseInteger(fileSizeStr, 100);
/*      */ 
/*  321 */           ArrayList textSdataFields = OracleTextUtils.getTextSdataFields(this.m_data.m_collectionDef.m_fieldDesignMap, null);
/*      */ 
/*  323 */           String[] drillDownFields = OracleTextUtils.getDrillDownField(this.m_config);
/*      */ 
/*  327 */           constructOtsMetaUpdateQuery("(PREPARED)UFastRebuildOtsMeta", tableName);
/*      */ 
/*  329 */           String[] selectQueryParams = { "lowdID int", "highdID int" };
/*  330 */           constructSelectQuery("(PREPARED)QFastRebuildColumns", tableName, columns, "dID BETWEEN ? AND ?", "dID ASC", selectQueryParams);
/*      */ 
/*  332 */           int sdataUpdatedCount = 0;
/*  333 */           String defaultNullValue = this.m_config.getValue("DefaultNullValue");
/*  334 */           String defaultTrueValue = this.m_config.getValue("DefaultTrueValue");
/*  335 */           int maxSdataSize = this.m_config.getInteger("MaxSdataCharSize", 249);
/*      */ 
/*  337 */           boolean hasProcessedAll = false;
/*  338 */           int lowdID = getMinimumdID(tableName);
/*  339 */           int highdID = lowdID + batchSize;
/*  340 */           int maxdID = getMaximumdID(tableName);
/*      */ 
/*  342 */           Report.trace("indexer", "Process otsmeta for dIDs between " + lowdID + " and " + maxdID + " for " + tableName, null);
/*      */ 
/*  344 */           if (highdID > maxdID)
/*      */           {
/*  346 */             highdID = maxdID;
/*      */           }
/*      */ 
/*  349 */           while ((lowdID <= highdID) && (highdID <= maxdID) && (!hasProcessedAll))
/*      */           {
/*  351 */             DataBinder selectQueryArgs = new DataBinder();
/*  352 */             selectQueryArgs.putLocal("lowdID", Integer.toString(lowdID));
/*  353 */             selectQueryArgs.putLocal("highdID", Integer.toString(highdID));
/*  354 */             ResultSet rset = this.m_idxWorkspace.createResultSet("QFastRebuildColumns", selectQueryArgs);
/*  355 */             DataResultSet drset = new DataResultSet();
/*  356 */             drset.copy(rset);
/*      */ 
/*  358 */             if (Report.m_verbose)
/*      */             {
/*  360 */               Report.trace("indexer", "Updating otsmeta for dIDs between " + lowdID + " and " + highdID + " : Number of Records = " + drset.getNumRows(), null);
/*      */             }
/*      */ 
/*  363 */             for (drset.first(); drset.isRowPresent(); drset.next())
/*      */             {
/*  365 */               Properties props = drset.getCurrentRowProps();
/*  366 */               DocIndexerUtils.addZoneFields(props, this.m_data.m_collectionDef.m_securityInfos);
/*      */ 
/*  368 */               IdcStringBuilder drillDownTag = OracleTextUtils.constructDrillDownTag(props, drillDownFields, this.m_idxWorkspace, this.m_data.m_workspace, null, this.m_data, this.m_config);
/*      */ 
/*  370 */               IdcStringBuilder metaValue = OracleTextUtils.buildOtsMeta(props, textSdataFields, drillDownTag, this.m_data.m_collectionDef.m_securityInfos, true, defaultNullValue, defaultTrueValue, maxSdataSize);
/*      */ 
/*  376 */               Object[] objs = { metaValue, props, this.m_idxWorkspace, this.m_config };
/*  377 */               this.m_data.setCachedObject("OtsMetaValueObjs", objs);
/*      */               try
/*      */               {
/*  380 */                 PluginFilters.filter("IndexingOtsMetaValueFilter", this.m_data.m_workspace, null, this.m_data);
/*      */               }
/*      */               catch (Exception ignore)
/*      */               {
/*  384 */                 Report.trace("indexer", null, ignore);
/*      */               }
/*      */ 
/*  388 */               DataBinder queryArgs = new DataBinder();
/*  389 */               queryArgs.putLocal("dID", props.getProperty("dID"));
/*  390 */               queryArgs.putLocal("otsMeta", metaValue.toString());
/*  391 */               this.m_idxWorkspace.addBatch("UFastRebuildOtsMeta", queryArgs);
/*      */             }
/*      */ 
/*  394 */             if (drset.getNumRows() > 0)
/*      */             {
/*  396 */               this.m_idxWorkspace.executeBatch();
/*      */             }
/*  398 */             sdataUpdatedCount += drset.getNumRows();
/*  399 */             this.m_data.reportProgress(0, "!csOracleTextOptimizedFieldUpdatedInCore", sdataUpdatedCount, totalCount);
/*      */ 
/*  403 */             if (highdID >= maxdID)
/*      */             {
/*  405 */               hasProcessedAll = true;
/*      */             }
/*      */             else
/*      */             {
/*  410 */               lowdID = highdID + 1;
/*  411 */               highdID += batchSize;
/*      */ 
/*  413 */               if (highdID > maxdID)
/*      */               {
/*  415 */                 highdID = maxdID;
/*      */               }
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*  424 */           String msg = LocaleUtils.encodeMessage("csOracleTextUnableToUpdateDataForTextSdataFields", null, tableName);
/*  425 */           throw new ServiceException(msg, e);
/*      */         }
/*      */       }
/*  428 */       boolean resetSemBack = false;
/*  429 */       String semInSession = null;
/*      */       try
/*      */       {
/*  432 */         DataBinder binder = new DataBinder();
/*  433 */         createPreference(tableName, binder);
/*      */ 
/*  435 */         String sectionGroupName = createSections(tableName);
/*  436 */         this.m_config.setValue("OracleTextSectionGroup", sectionGroupName);
/*  437 */         this.m_config.setValue("OracleTextSyncOption", "manual");
/*  438 */         this.m_config.setValue("OracleTextAdditionalFullTextParameters", "NOPOPULATE");
/*  439 */         String params = this.m_config.getScriptValue("OracleTextFullTextParameters");
/*  440 */         binder.putLocal("indexName", indexName);
/*  441 */         binder.putLocal("indexParameters", "Replace " + params);
/*  442 */         if (parallels != null)
/*      */         {
/*  444 */           binder.putLocal("indexRebuildParallelDegree", parallels);
/*      */         }
/*      */ 
/*      */         try
/*      */         {
/*  449 */           this.m_idxWorkspace.executeCallable("CoracleTextDropShadowIndex", binder);
/*      */         }
/*      */         catch (Exception ignore)
/*      */         {
/*  453 */           if (SystemUtils.m_verbose)
/*      */           {
/*  455 */             Report.debug("indexer", null, ignore);
/*      */           }
/*      */         }
/*      */ 
/*  459 */         semInSession = OracleTextUtils.getLengthSemanticsFromSession(this.m_idxWorkspace);
/*  460 */         resetSemBack = OracleTextUtils.checkAndSetLengthSemantics(this.m_idxWorkspace, "DR$" + indexName.toUpperCase() + "$I", "TOKEN_TEXT", semInSession);
/*      */ 
/*  462 */         this.m_data.reportProgress(2, "!csOracleTextCreatingShadowIndex", -1.0F, -1.0F);
/*      */ 
/*  464 */         this.m_idxWorkspace.executeCallable("CoracleTextCreateShadowIndex", binder);
/*      */ 
/*  466 */         this.m_data.reportProgress(2, "!csOracleTextPopulatingPendingJob", -1.0F, -1.0F);
/*      */ 
/*  468 */         ResultSet rset = this.m_idxWorkspace.createResultSet("QoracleTextInternalIndexInfo", binder);
/*  469 */         String ctxid = ResultSetUtils.getValue(rset, "idx_id");
/*  470 */         this.m_shadowIndexName = ("RIO$" + ctxid);
/*  471 */         binder.putLocal("shadowIndexName", this.m_shadowIndexName);
/*  472 */         this.m_idxWorkspace.executeCallable("CoracleTextPopulatePending", binder);
/*      */ 
/*  474 */         binder.putLocal("indexName", this.m_shadowIndexName);
/*  475 */         rset = this.m_idxWorkspace.createResultSet("QoracleTextInternalIndexInfo", binder);
/*  476 */         ctxid = ResultSetUtils.getValue(rset, "idx_id");
/*  477 */         binder.putLocal("idxID", ctxid);
/*  478 */         while (!checkSyncStatus(binder))
/*      */         {
/*  480 */           this.m_idxWorkspace.executeCallable("CoracleTextSyncIndex", binder);
/*      */ 
/*  482 */           int pendingCount = -1;
/*      */           try
/*      */           {
/*  485 */             pendingCount = WorkspaceUtils.getRowCount("CTXSYS.DR$PENDING", "PND_CID = '" + ctxid + "'", this.m_idxWorkspace);
/*      */           }
/*      */           catch (DataException ignore)
/*      */           {
/*  490 */             Report.debug("indexer", "Error retrieving totalCount for CTXSYS.DR$PENDING", ignore);
/*      */           }
/*      */ 
/*  493 */           this.m_data.reportProgress(0, "!csOracleTextIndexSyncInCore", totalCount - pendingCount, totalCount);
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*      */       }
/*      */       finally
/*      */       {
/*      */         String msg;
/*  504 */         if (resetSemBack)
/*      */         {
/*  507 */           OracleTextUtils.setLengthSemantics(this.m_idxWorkspace, semInSession);
/*      */         }
/*  509 */         this.m_idxWorkspace.setThreadTimeout(timeout);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  514 */     storeCollectionDesign(this.m_activeIndex, def);
/*      */ 
/*  517 */     initQueries(true, this.m_activeIndex);
/*      */ 
/*  519 */     data.setCachedObject("CollectionDesignUpToDate", Boolean.TRUE);
/*  520 */     return status;
/*      */   }
/*      */ 
/*      */   public FieldInfo[] filterFieldsForIndexUpdate(FieldInfo[] fieldInfoAddedCols)
/*      */   {
/*  531 */     List fieldListForIndexing = new ArrayList();
/*      */ 
/*  534 */     for (int fNo = 0; fNo < fieldInfoAddedCols.length; ++fNo)
/*      */     {
/*  536 */       if (isPlaceHolderIndexField(fieldInfoAddedCols[fNo]))
/*      */         continue;
/*  538 */       fieldListForIndexing.add(fieldInfoAddedCols[fNo]);
/*      */     }
/*      */ 
/*  543 */     FieldInfo[] fieldInfosForIndexing = new FieldInfo[fieldListForIndexing.size()];
/*  544 */     for (int fNo = 0; fNo < fieldListForIndexing.size(); ++fNo)
/*      */     {
/*  546 */       fieldInfosForIndexing[fNo] = ((FieldInfo)fieldListForIndexing.get(fNo));
/*      */     }
/*      */ 
/*  549 */     return fieldInfosForIndexing;
/*      */   }
/*      */ 
/*      */   public boolean isPlaceHolderIndexField(FieldInfo fi)
/*      */   {
/*  559 */     String isPlaceHolderField = "0";
/*      */ 
/*  561 */     Map fieldDesignMap = this.m_data.m_collectionDef.m_fieldDesignMap;
/*  562 */     if ((fieldDesignMap != null) && (fieldDesignMap.get(fi.m_name) != null))
/*      */     {
/*  564 */       Properties prop = (Properties)fieldDesignMap.get(fi.m_name);
/*  565 */       isPlaceHolderField = prop.getProperty("dIsPlaceholderField");
/*      */     }
/*      */     else
/*      */     {
/*  569 */       DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/*  570 */       if ((drset != null) && (drset.getNumRows() > 0))
/*      */       {
/*  572 */         FieldInfo dNameFieldInfo = new FieldInfo();
/*  573 */         FieldInfo dIsPlaceHolderFieldInfo = new FieldInfo();
/*  574 */         drset.getFieldInfo("dName", dNameFieldInfo);
/*  575 */         drset.getFieldInfo("dIsPlaceHolderField", dIsPlaceHolderFieldInfo);
/*      */ 
/*  577 */         List fieldRow = drset.findRow(dNameFieldInfo.m_index, fi.m_name, 0, 0);
/*  578 */         if (fieldRow != null)
/*      */         {
/*  580 */           isPlaceHolderField = (String)fieldRow.get(dIsPlaceHolderFieldInfo.m_index);
/*      */         }
/*      */       }
/*      */     }
/*  584 */     return StringUtils.convertToBool(isPlaceHolderField, false);
/*      */   }
/*      */ 
/*      */   public void constructOtsMetaUpdateQuery(String queryDef, String tableName)
/*      */     throws DataException
/*      */   {
/*  594 */     IdcStringBuilder updateQueryTemplate = new IdcStringBuilder("UPDATE ");
/*  595 */     updateQueryTemplate.append(tableName);
/*  596 */     updateQueryTemplate.append(" SET otsMeta = ? WHERE dID = ?");
/*      */ 
/*  598 */     IdcStringBuilder updateParams = new IdcStringBuilder("otsMeta clob");
/*  599 */     updateParams.append("\n");
/*  600 */     updateParams.append("dID int");
/*      */ 
/*  602 */     DataResultSet drset = new DataResultSet(new String[] { "name", "queryStr", "parameters" });
/*  603 */     QueryUtils.addQueryDef(drset, queryDef, updateQueryTemplate.toString(), updateParams.toString());
/*      */ 
/*  605 */     this.m_idxWorkspace.addQueryDefs(drset);
/*      */   }
/*      */ 
/*      */   public void constructSelectQuery(String queryDef, String tableName, String columns, String whereClause, String orderByClause, String[] queryParameters)
/*      */     throws DataException
/*      */   {
/*  622 */     IdcStringBuilder selectQueryTemplate = new IdcStringBuilder("SELECT ");
/*  623 */     selectQueryTemplate.append(columns);
/*  624 */     selectQueryTemplate.append(" FROM ");
/*  625 */     selectQueryTemplate.append(tableName);
/*      */ 
/*  627 */     if ((whereClause != null) && (whereClause.length() > 0))
/*      */     {
/*  629 */       selectQueryTemplate.append(" WHERE ");
/*  630 */       selectQueryTemplate.append(whereClause);
/*      */     }
/*      */ 
/*  633 */     if ((orderByClause != null) && (orderByClause.length() > 0))
/*      */     {
/*  635 */       selectQueryTemplate.append(" ORDER BY ");
/*  636 */       selectQueryTemplate.append(orderByClause);
/*      */     }
/*      */ 
/*  639 */     IdcStringBuilder selectParams = new IdcStringBuilder();
/*      */ 
/*  641 */     if ((queryParameters != null) && (queryParameters.length > 0))
/*      */     {
/*  643 */       for (int paramNo = 0; paramNo < queryParameters.length; ++paramNo)
/*      */       {
/*  645 */         if (selectParams.length() > 0)
/*      */         {
/*  647 */           selectParams.append("\n");
/*      */         }
/*  649 */         selectParams.append(queryParameters[paramNo]);
/*      */       }
/*      */     }
/*      */ 
/*  653 */     DataResultSet drset = new DataResultSet(new String[] { "name", "queryStr", "parameters" });
/*  654 */     QueryUtils.addQueryDef(drset, queryDef, selectQueryTemplate.toString(), selectParams.toString());
/*      */ 
/*  656 */     this.m_idxWorkspace.addQueryDefs(drset);
/*      */   }
/*      */ 
/*      */   public int getMinimumdID(String tableName)
/*      */     throws DataException
/*      */   {
/*  667 */     IdcStringBuilder query = new IdcStringBuilder("SELECT MIN(dID) FROM ");
/*  668 */     query.append(tableName);
/*      */ 
/*  670 */     ResultSet rSet = this.m_idxWorkspace.createResultSetSQL(query.toString());
/*      */ 
/*  672 */     rSet.first();
/*  673 */     String minimumdIDString = rSet.getStringValue(0);
/*  674 */     int minimumdID = NumberUtils.parseInteger(minimumdIDString, -1);
/*      */ 
/*  676 */     return minimumdID;
/*      */   }
/*      */ 
/*      */   public int getMaximumdID(String tableName)
/*      */     throws DataException
/*      */   {
/*  687 */     IdcStringBuilder query = new IdcStringBuilder("SELECT MAX(dID) FROM ");
/*  688 */     query.append(tableName);
/*      */ 
/*  690 */     ResultSet rSet = this.m_idxWorkspace.createResultSetSQL(query.toString());
/*      */ 
/*  692 */     rSet.first();
/*  693 */     String maximumdIDString = rSet.getStringValue(0);
/*  694 */     int maximumdID = NumberUtils.parseInteger(maximumdIDString, -1);
/*      */ 
/*  696 */     return maximumdID;
/*      */   }
/*      */ 
/*      */   public void updateIndexUsingBaseTable(String tableName, FieldInfo[] fieldInfoAddedCols, IndexerCollectionData collectionData, IndexerWorkObject data)
/*      */     throws DataException, ServiceException
/*      */   {
/*  715 */     Report.trace(this.m_tracingSection, "Updating index " + tableName + " using base table for metadata fields added", null);
/*      */ 
/*  717 */     fieldInfoAddedCols = filterFieldsForIndexUpdate(fieldInfoAddedCols);
/*      */ 
/*  719 */     if (this.m_idxWorkspace.equals(data.m_workspace))
/*      */     {
/*  721 */       String query = constructUpdateQuery(tableName, fieldInfoAddedCols, collectionData);
/*  722 */       if (query != null)
/*      */       {
/*  729 */         this.m_data.reportProgress(2, "!csOracleTextUpdatingValues", 0.0F, -1.0F);
/*  730 */         this.m_idxWorkspace.executeSQL(query);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  735 */       Report.trace(this.m_tracingSection, "External provider detected hence performing batch updates", null);
/*      */ 
/*  739 */       setDefaultValueForFields(this.m_idxWorkspace, tableName, fieldInfoAddedCols);
/*      */ 
/*  741 */       int indexerUpdateBucketSize = SharedObjects.getEnvironmentInt("IndexerFastRebuildBucketSize", 10000);
/*  742 */       int indexerUpdateBatchSize = SharedObjects.getEnvironmentInt("IndexerFastRebuildBatchSize", 100);
/*      */ 
/*  744 */       HashMap baseTableFieldMap = (HashMap)groupFieldsPerBaseTable(fieldInfoAddedCols, collectionData);
/*      */ 
/*  746 */       Set baseTableKeys = baseTableFieldMap.keySet();
/*  747 */       Iterator keysIterator = baseTableKeys.iterator();
/*  748 */       while (keysIterator.hasNext())
/*      */       {
/*  750 */         String baseTableName = (String)keysIterator.next();
/*  751 */         List selectFields = (List)baseTableFieldMap.get(baseTableName);
/*      */ 
/*  753 */         FieldInfo firstField = (FieldInfo)selectFields.get(0);
/*  754 */         String relColumn = getRelationalColumnForField(collectionData, firstField);
/*      */ 
/*  759 */         DataBinder queryArgs = new DataBinder();
/*      */ 
/*  761 */         for (int fieldNo = 0; fieldNo < selectFields.size(); ++fieldNo)
/*      */         {
/*  763 */           FieldInfo currentFieldInfo = (FieldInfo)selectFields.get(fieldNo);
/*  764 */           IdcStringBuilder selectQuery = new IdcStringBuilder();
/*  765 */           appendSelectQuery(selectQuery, currentFieldInfo, baseTableName, relColumn);
/*      */ 
/*  767 */           ResultSet baseTableResultSet = data.m_workspace.createResultSetSQL(selectQuery.toString());
/*  768 */           FieldInfo relColumnFieldInfo = new FieldInfo();
/*  769 */           baseTableResultSet.getFieldInfo(relColumn, relColumnFieldInfo);
/*      */ 
/*  771 */           constructColumnUpdateQueryDef("UFastRebuildColumnUpdate", tableName, currentFieldInfo, relColumnFieldInfo);
/*      */ 
/*  773 */           if (Report.m_verbose)
/*      */           {
/*  775 */             Report.trace(this.m_tracingSection, "Updating index for : " + currentFieldInfo.m_name + " using baseTable : " + baseTableName, null);
/*      */           }
/*      */ 
/*  778 */           while (baseTableResultSet.isRowPresent())
/*      */           {
/*  780 */             DataResultSet processBucket = new DataResultSet();
/*  781 */             ResultSetFilter filter = processBucket.createMaxNumResultSetFilter(indexerUpdateBucketSize);
/*  782 */             processBucket.copyFilteredEx(baseTableResultSet, null, filter, false);
/*      */ 
/*  784 */             for (int rowNo = 0; rowNo < processBucket.getNumRows(); ++rowNo)
/*      */             {
/*  786 */               List currentRowValues = processBucket.getRowAsList(rowNo);
/*  787 */               String relColumnValue = (String)currentRowValues.get(0);
/*  788 */               String currentColumnValue = (String)currentRowValues.get(1);
/*      */ 
/*  790 */               if ((currentColumnValue != null) && (currentColumnValue.length() > 0))
/*      */               {
/*  792 */                 queryArgs.putLocal(currentFieldInfo.m_name, currentColumnValue);
/*  793 */                 queryArgs.putLocal(relColumnFieldInfo.m_name, relColumnValue);
/*  794 */                 this.m_idxWorkspace.addBatch("UFastRebuildColumnUpdate", queryArgs);
/*      */               }
/*      */ 
/*  799 */               if ((rowNo <= 0) || (rowNo % indexerUpdateBatchSize != 0))
/*      */                 continue;
/*  801 */               this.m_idxWorkspace.executeBatch();
/*      */             }
/*      */ 
/*  807 */             this.m_idxWorkspace.executeBatch();
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public String appendSelectQuery(IdcStringBuilder selectQuery, Object[] fieldsToSelect, String baseTableName, String relColumn)
/*      */   {
/*  826 */     if (selectQuery == null)
/*      */     {
/*  828 */       selectQuery = new IdcStringBuilder();
/*      */     }
/*      */ 
/*  831 */     selectQuery.append("SELECT ");
/*  832 */     selectQuery.append(relColumn);
/*      */ 
/*  834 */     for (int fieldNo = 0; fieldNo < fieldsToSelect.length; ++fieldNo)
/*      */     {
/*  836 */       FieldInfo currentFieldInfo = (FieldInfo)(FieldInfo)fieldsToSelect[fieldNo];
/*      */ 
/*  838 */       selectQuery.append(",");
/*  839 */       selectQuery.append(currentFieldInfo.m_name);
/*      */     }
/*      */ 
/*  842 */     selectQuery.append(" FROM ");
/*  843 */     selectQuery.append(baseTableName);
/*      */ 
/*  845 */     return selectQuery.toString();
/*      */   }
/*      */ 
/*      */   public String appendSelectQuery(IdcStringBuilder selectQuery, Object fieldToSelect, String baseTableName, String relColumn)
/*      */   {
/*  850 */     return appendSelectQuery(selectQuery, new Object[] { fieldToSelect }, baseTableName, relColumn);
/*      */   }
/*      */ 
/*      */   public void constructColumnUpdateQueryDef(String queryDef, String tableName, FieldInfo fieldInfo, FieldInfo relColumnFieldInfo)
/*      */     throws DataException
/*      */   {
/*  865 */     if (Report.m_verbose)
/*      */     {
/*  867 */       Report.trace(this.m_tracingSection, "Adding column update query template for " + fieldInfo.m_name, null);
/*      */     }
/*      */ 
/*  870 */     IdcStringBuilder updateQueryTemplate = new IdcStringBuilder("UPDATE ");
/*  871 */     updateQueryTemplate.append(tableName);
/*  872 */     updateQueryTemplate.append(" SET ");
/*  873 */     updateQueryTemplate.append(fieldInfo.m_name);
/*  874 */     updateQueryTemplate.append(" = ?");
/*  875 */     if (relColumnFieldInfo != null)
/*      */     {
/*  877 */       updateQueryTemplate.append(" WHERE ");
/*  878 */       updateQueryTemplate.append(relColumnFieldInfo.m_name);
/*  879 */       updateQueryTemplate.append(" = ?");
/*      */     }
/*      */ 
/*  883 */     IdcStringBuilder updateParams = new IdcStringBuilder(fieldInfo.m_name);
/*  884 */     updateParams.append(" ");
/*  885 */     updateParams.append(fieldInfo.getTypeName());
/*      */ 
/*  887 */     if (relColumnFieldInfo != null)
/*      */     {
/*  889 */       updateParams.append("\n");
/*  890 */       updateParams.append(relColumnFieldInfo.m_name);
/*  891 */       updateParams.append(" ");
/*  892 */       updateParams.append(relColumnFieldInfo.getTypeName());
/*      */     }
/*      */ 
/*  895 */     DataResultSet drset = new DataResultSet(new String[] { "name", "queryStr", "parameters" });
/*  896 */     QueryUtils.addQueryDef(drset, queryDef, updateQueryTemplate.toString(), updateParams.toString());
/*      */ 
/*  898 */     this.m_idxWorkspace.addQueryDefs(drset);
/*      */   }
/*      */ 
/*      */   public void setDefaultValueForFields(Workspace ws, String tableName, FieldInfo[] fieldInfoAddedCols)
/*      */     throws DataException
/*      */   {
/*  911 */     Report.trace(this.m_tracingSection, "Setting default values for the metadata fields in " + tableName, null);
/*      */ 
/*  913 */     IdcStringBuilder setDefaultValueQuery = new IdcStringBuilder("UPDATE ");
/*  914 */     setDefaultValueQuery.append(tableName);
/*  915 */     setDefaultValueQuery.append(" SET ");
/*      */ 
/*  917 */     boolean hasUpdate = false;
/*  918 */     for (int fieldNo = 0; fieldNo < fieldInfoAddedCols.length; ++fieldNo)
/*      */     {
/*  920 */       FieldInfo currentFieldInfo = fieldInfoAddedCols[fieldNo];
/*      */ 
/*  922 */       if (currentFieldInfo.m_type != 6)
/*      */         continue;
/*  924 */       if (Report.m_verbose)
/*      */       {
/*  926 */         Report.trace(this.m_tracingSection, "Setting " + m_defaultNullValue + " for " + currentFieldInfo.m_name, null);
/*      */       }
/*  928 */       if (hasUpdate)
/*      */       {
/*  930 */         setDefaultValueQuery.append(",");
/*      */       }
/*      */ 
/*  933 */       setDefaultValueQuery.append(currentFieldInfo.m_name);
/*  934 */       setDefaultValueQuery.append(" = '");
/*  935 */       setDefaultValueQuery.append(m_defaultNullValue);
/*  936 */       setDefaultValueQuery.append("'");
/*  937 */       hasUpdate = true;
/*      */     }
/*      */ 
/*  941 */     if (hasUpdate != true)
/*      */       return;
/*  943 */     this.m_idxWorkspace.executeSQL(setDefaultValueQuery.toString());
/*      */   }
/*      */ 
/*      */   public Map groupFieldsPerBaseTable(FieldInfo[] fieldInfoAddedCols, IndexerCollectionData collectionData)
/*      */   {
/*  957 */     Map baseTableFieldMap = new HashMap();
/*      */ 
/*  959 */     if ((fieldInfoAddedCols != null) && (fieldInfoAddedCols.length > 0))
/*      */     {
/*  961 */       for (int fieldNo = 0; fieldNo < fieldInfoAddedCols.length; ++fieldNo)
/*      */       {
/*  963 */         String baseTable = getBaseTableForField(collectionData, fieldInfoAddedCols[fieldNo]);
/*      */ 
/*  965 */         Object listOfFields = baseTableFieldMap.get(baseTable);
/*  966 */         if (listOfFields == null)
/*      */         {
/*  968 */           listOfFields = new ArrayList();
/*  969 */           baseTableFieldMap.put(baseTable, listOfFields);
/*      */         }
/*      */ 
/*  972 */         ((List)listOfFields).add(fieldInfoAddedCols[fieldNo]);
/*      */       }
/*      */     }
/*      */ 
/*  976 */     return baseTableFieldMap;
/*      */   }
/*      */ 
/*      */   public String getBaseTableForField(IndexerCollectionData def, FieldInfo fInfo)
/*      */   {
/*  981 */     Properties prop = (Properties)def.m_fieldDesignMap.get(fInfo.m_name);
/*  982 */     String baseTable = prop.getProperty("baseTable");
/*      */ 
/*  984 */     return baseTable;
/*      */   }
/*      */ 
/*      */   public String getRelationalColumnForField(IndexerCollectionData def, FieldInfo fInfo)
/*      */   {
/*  989 */     Properties prop = (Properties)def.m_fieldDesignMap.get(fInfo.m_name);
/*  990 */     String relColumn = prop.getProperty("relColumn");
/*  991 */     if (relColumn == null)
/*      */     {
/*  993 */       relColumn = "dID";
/*      */     }
/*      */ 
/*  996 */     return relColumn;
/*      */   }
/*      */ 
/*      */   public boolean checkNewSdata(Map oldDesignMap, Map newDesignMap)
/*      */   {
/* 1001 */     boolean hasNewSdata = false;
/*      */ 
/* 1003 */     for (Map.Entry entry : newDesignMap.entrySet())
/*      */     {
/* 1005 */       Properties props = (Properties)entry.getValue();
/*      */ 
/* 1007 */       String optimized = props.getProperty("isOptimized");
/* 1008 */       boolean isOptimized = StringUtils.convertToBool(optimized, false);
/* 1009 */       if (isOptimized)
/*      */       {
/* 1011 */         String key = (String)entry.getKey();
/* 1012 */         Properties oldProps = (Properties)oldDesignMap.get(key);
/* 1013 */         if (oldProps != null)
/*      */         {
/* 1015 */           boolean usedToBeOptimized = StringUtils.convertToBool(oldProps.getProperty(key), false);
/* 1016 */           if (!usedToBeOptimized)
/*      */           {
/* 1018 */             hasNewSdata = true;
/* 1019 */             break;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 1024 */       String isInFilterCatString = props.getProperty("IsInSearchResultFilterCategory", "false");
/* 1025 */       boolean isInFilterCat = StringUtils.convertToBool(isInFilterCatString, false);
/* 1026 */       String key = (String)entry.getKey();
/* 1027 */       Properties oldProps = (Properties)oldDesignMap.get(key);
/* 1028 */       if (oldProps != null)
/*      */       {
/* 1030 */         String isInFilterCatStringOld = oldProps.getProperty("IsInSearchResultFilterCategory", "false");
/* 1031 */         boolean isInFilterCatOld = StringUtils.convertToBool(isInFilterCatStringOld, false);
/*      */ 
/* 1033 */         if (isInFilterCat != isInFilterCatOld)
/*      */         {
/* 1035 */           hasNewSdata = true;
/* 1036 */           break;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1041 */     return hasNewSdata;
/*      */   }
/*      */ 
/*      */   public String constructUpdateQuery(String tableName, FieldInfo[] fInfos, IndexerCollectionData def) {
/* 1045 */     IdcStringBuilder builder = new IdcStringBuilder("UPDATE ");
/* 1046 */     builder.append(tableName);
/* 1047 */     builder.append(" SET ");
/*      */ 
/* 1049 */     boolean hasUpdate = false;
/* 1050 */     for (int i = 0; i < fInfos.length; ++i)
/*      */     {
/* 1052 */       Properties prop = (Properties)def.m_fieldDesignMap.get(fInfos[i].m_name);
/* 1053 */       String baseTable = prop.getProperty("baseTable");
/* 1054 */       if (baseTable == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1059 */       String relColumn = prop.getProperty("relColumn");
/* 1060 */       if (relColumn == null)
/*      */       {
/* 1062 */         relColumn = "dID";
/*      */       }
/* 1064 */       if (hasUpdate)
/*      */       {
/* 1066 */         builder.append(',');
/*      */       }
/* 1068 */       builder.append(fInfos[i].m_name);
/* 1069 */       builder.append(" = (SELECT ");
/* 1070 */       builder.append(fInfos[i].m_name);
/* 1071 */       builder.append(" FROM ");
/* 1072 */       builder.append(baseTable);
/* 1073 */       builder.append(" WHERE ");
/* 1074 */       builder.append(tableName);
/* 1075 */       builder.append(".");
/* 1076 */       builder.append(relColumn);
/* 1077 */       builder.append(" = ");
/* 1078 */       builder.append(baseTable);
/* 1079 */       builder.append(".");
/* 1080 */       builder.append(relColumn);
/* 1081 */       if (baseTable.compareToIgnoreCase("Documents") == 0)
/*      */       {
/* 1086 */         builder.append(" AND dIsPrimary = 1");
/*      */       }
/* 1088 */       builder.append(')');
/* 1089 */       hasUpdate = true;
/*      */     }
/*      */ 
/* 1092 */     if (hasUpdate)
/*      */     {
/* 1094 */       return builder.toString();
/*      */     }
/* 1096 */     return null;
/*      */   }
/*      */ 
/*      */   protected boolean checkSyncStatus(DataBinder params) throws DataException
/*      */   {
/* 1101 */     ResultSet rset = this.m_idxWorkspace.createResultSet("QoracleTextPendingDocIDs", params);
/* 1102 */     if (rset.isEmpty())
/*      */     {
/* 1105 */       return true;
/*      */     }
/*      */ 
/* 1108 */     String batchTimeStr = this.m_config.getValue("IndexerEngineRebuildBatchTime");
/* 1109 */     long maxTime = NumberUtils.parseLong(batchTimeStr, 15L);
/* 1110 */     params.putLocal("maxTime", "" + maxTime);
/* 1111 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean loadCollectionDesign(IndexerCollectionData colData)
/*      */     throws ServiceException
/*      */   {
/* 1117 */     boolean loaded = false;
/* 1118 */     boolean cfgTableExists = WorkspaceUtils.doesTableExist(this.m_idxWorkspace, "SearchCollectionConfig", null);
/* 1119 */     if (cfgTableExists)
/*      */     {
/*      */       try
/*      */       {
/* 1123 */         MapParameters param = new MapParameters(new Properties());
/* 1124 */         param.setObject("collectionId", this.m_activeIndex);
/* 1125 */         DataStreamValue rset = (DataStreamValue)this.m_idxWorkspace.createResultSet("QsearchCollectionConfig", param);
/* 1126 */         if (((ResultSet)rset).isRowPresent())
/*      */         {
/* 1128 */           Reader reader = rset.getCharacterReader("dTextCollectionConfig");
/* 1129 */           DataBinder binder = new DataBinder();
/* 1130 */           binder.receive(new BufferedReader(reader));
/* 1131 */           SearchFieldInfo.loadFieldInfo("OracleTextSearch", binder, this.m_data.m_workspace, colData);
/* 1132 */           loaded = true;
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1137 */         String msg = LocaleUtils.encodeMessage("csOracleTextUnableLoadCollectionDesign", null);
/* 1138 */         throw new ServiceException(msg, e);
/*      */       }
/*      */     }
/* 1141 */     return loaded;
/*      */   }
/*      */ 
/*      */   public void storeCollectionDesign(String collectionId, IndexerCollectionData data) throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1148 */       checkOrCreateCollectionConfigTable("SearchCollectionConfig");
/* 1149 */       DataBinder binder = data.prepareBinderForSerialization();
/* 1150 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/* 1151 */       binder.send(sw);
/* 1152 */       sw.close();
/* 1153 */       binder.putLocal("collectionConfig", sw.toStringRelease());
/* 1154 */       binder.putLocal("collectionId", collectionId);
/* 1155 */       ResultSet rset = this.m_idxWorkspace.createResultSet("QsearchCollectionConfig", binder);
/* 1156 */       if (rset.isRowPresent())
/*      */       {
/* 1158 */         this.m_idxWorkspace.execute("UsearchCollectionConfig", binder);
/*      */       }
/*      */       else
/*      */       {
/* 1162 */         this.m_idxWorkspace.execute("IsearchCollectionConfig", binder);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1168 */       String msg = LocaleUtils.encodeMessage("csOracleTextUnableStoreCollectionDesign", null);
/* 1169 */       throw new ServiceException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkOrCreateCollectionConfigTable(String table) throws ServiceException
/*      */   {
/* 1175 */     boolean cfgTableExists = WorkspaceUtils.doesTableExist(this.m_idxWorkspace, "SearchCollectionConfig", null);
/* 1176 */     if (cfgTableExists)
/*      */       return;
/* 1178 */     FieldInfo idFI = new FieldInfo();
/* 1179 */     idFI.m_name = "dTextCollectionID";
/* 1180 */     idFI.m_isFixedLen = true;
/* 1181 */     idFI.m_maxLen = 50;
/*      */ 
/* 1183 */     FieldInfo config = new FieldInfo();
/* 1184 */     config.m_name = "dTextCollectionConfig";
/* 1185 */     config.m_type = 10;
/*      */     try
/*      */     {
/* 1189 */       this.m_idxWorkspace.createTable(table, new FieldInfo[] { idFI, config }, new String[] { idFI.m_name });
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1193 */       String msg = LocaleUtils.encodeMessage("csOracleTextUnableCreateConfigTable", null);
/* 1194 */       throw new ServiceException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void createCollection(String collectionID)
/*      */     throws ServiceException
/*      */   {
/* 1201 */     String tableName = OracleTextUtils.getTableName(collectionID, this.m_config);
/*      */ 
/* 1203 */     if (WorkspaceUtils.doesTableExist(this.m_idxWorkspace, tableName, null))
/*      */     {
/*      */       try
/*      */       {
/* 1207 */         Report.trace(this.m_tracingSection, "Collection table " + tableName + " exists.", null);
/* 1208 */         Report.trace(this.m_tracingSection, "In preparation of creating new collection, deleting existing table " + tableName + ".", null);
/*      */ 
/* 1210 */         this.m_idxWorkspace.deleteTable(tableName);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1214 */         throw new ServiceException(LocaleUtils.encodeMessage("csIndexerOracleTextCanNotDeleteTable", e.getLocalizedMessage(), tableName));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1220 */     boolean resetSemBack = false;
/* 1221 */     String semInSession = null;
/*      */     try
/*      */     {
/* 1226 */       Report.trace(this.m_tracingSection, "Creating collection table " + tableName + ".", null);
/* 1227 */       createTable(tableName);
/* 1228 */       DataBinder binder = new DataBinder();
/* 1229 */       createPreference(collectionID, binder);
/*      */ 
/* 1231 */       String sectionGroupName = createSections(collectionID);
/* 1232 */       this.m_config.setValue("OracleTextSectionGroup", sectionGroupName);
/*      */ 
/* 1236 */       semInSession = OracleTextUtils.getLengthSemanticsFromSession(this.m_idxWorkspace);
/* 1237 */       if (!semInSession.equalsIgnoreCase("BYTE"))
/*      */       {
/* 1239 */         OracleTextUtils.setLengthSemantics(this.m_idxWorkspace, "BYTE");
/* 1240 */         resetSemBack = true;
/*      */       }
/*      */ 
/* 1244 */       Report.trace(this.m_tracingSection, "Creating full text index", null);
/* 1245 */       addFullTextIndex(tableName, OracleTextUtils.getIndexName(tableName, "", this.m_idxWorkspace), binder);
/*      */ 
/* 1248 */       Report.trace(this.m_tracingSection, "Creating additional indexes", null);
/* 1249 */       addIndexes(tableName);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/* 1259 */       if (resetSemBack)
/*      */       {
/* 1262 */         OracleTextUtils.setLengthSemantics(this.m_idxWorkspace, semInSession);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void createTable(String tableName) throws DataException
/*      */   {
/* 1269 */     if ((tableName == null) || (tableName.trim().length() == 0))
/*      */     {
/* 1271 */       String msg = LocaleUtils.encodeMessage("csIndexerCollectionTableNameIsNull", null);
/* 1272 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 1275 */     this.m_config.setValue("fullTextTableName", tableName);
/*      */ 
/* 1278 */     FieldInfo[] tableFields = computeCollectionTableFields();
/* 1279 */     OracleTextUtils.updateColumnMap(this.m_idxWorkspace, tableFields);
/*      */ 
/* 1281 */     this.m_idxWorkspace.createTable(tableName, tableFields, new String[] { "dDocName" });
/* 1282 */     cacheFields(tableFields);
/*      */   }
/*      */ 
/*      */   public void cacheFields(FieldInfo[] tableFields)
/*      */   {
/* 1287 */     if (tableFields == null)
/*      */     {
/* 1289 */       tableFields = computeCollectionTableFields();
/*      */     }
/* 1291 */     Object[] fields = new Object[2];
/* 1292 */     fields[1] = new ArrayList();
/* 1293 */     fields[0] = getMetaFields((ArrayList)fields[1]);
/* 1294 */     SharedObjects.putObject("OracleTextSearch", "Fields" + this.m_activeIndex, fields);
/*      */   }
/*      */ 
/*      */   public void createPreference(String collectionID, DataBinder binder) throws DataException
/*      */   {
/* 1299 */     DataResultSet preferences = this.m_config.getTable("OracleFullTextPreferenceTable");
/* 1300 */     for (preferences.first(); preferences.isRowPresent(); preferences.next())
/*      */     {
/* 1302 */       String id = preferences.getStringValueByName("oftId");
/* 1303 */       if (id.length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1307 */       id = id + "_" + collectionID.toLowerCase();
/* 1308 */       String name = preferences.getStringValueByName("oftName");
/* 1309 */       if (name.length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1313 */       String type = preferences.getStringValueByName("oftType");
/* 1314 */       if (type.length() == 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1319 */       binder.putLocal("preferenceID", id);
/* 1320 */       binder.putLocal("preferenceName", name);
/*      */       try
/*      */       {
/* 1323 */         this.m_idxWorkspace.executeCallable("CdropTextPreference", binder);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1328 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1330 */           Report.trace("indexer", null, e);
/*      */         }
/*      */       }
/* 1333 */       this.m_idxWorkspace.executeCallable("CaddTextPreference", binder);
/*      */ 
/* 1336 */       String additionalSearchableFields = getMetaFieldsAsString();
/* 1337 */       this.m_config.setValue("AdditionalSearchableFields", "," + additionalSearchableFields);
/* 1338 */       String attributes = preferences.getStringValueByName("oftAttributes");
/* 1339 */       Vector v = StringUtils.parseArray(attributes, ';', '^');
/* 1340 */       int size = v.size();
/*      */ 
/* 1342 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 1344 */         String pair = (String)v.elementAt(i);
/* 1345 */         Vector attribs = StringUtils.parseArrayEx(pair, '=', '^', true);
/* 1346 */         if (attribs.size() != 2) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1350 */         String key = (String)attribs.elementAt(0);
/* 1351 */         String value = (String)attribs.elementAt(1);
/* 1352 */         if ((key.length() == 0) && (value.length() == 0)) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1356 */         value = this.m_data.m_config.parseScriptValue(value);
/* 1357 */         binder.putLocal("attribName", key);
/* 1358 */         binder.putLocal("attribValue", value);
/*      */ 
/* 1360 */         this.m_idxWorkspace.executeCallable("CaddTextPrefAttribute", binder);
/*      */       }
/* 1362 */       this.m_config.setValue("OracleText" + type, id);
/*      */     }
/*      */   }
/*      */ 
/*      */   public FieldInfo[] getImplicitSDATAList()
/*      */   {
/* 1369 */     HashMap implicitSdata = getImplicitSDATAFields();
/* 1370 */     FieldInfo[] list = new FieldInfo[implicitSdata.size()];
/*      */ 
/* 1372 */     implicitSdata.entrySet().toArray(list);
/* 1373 */     return list;
/*      */   }
/*      */ 
/*      */   public HashMap getImplicitSDATAFields()
/*      */   {
/* 1378 */     Hashtable map = this.m_data.m_collectionDef.m_fieldDesignMap;
/* 1379 */     Hashtable info = this.m_data.m_collectionDef.m_fieldInfos;
/*      */ 
/* 1382 */     HashMap implicitSdata = new HashMap();
/* 1383 */     for (Enumeration enu = map.keys(); enu.hasMoreElements(); )
/*      */     {
/* 1385 */       String name = (String)enu.nextElement();
/* 1386 */       Object obj = map.get(name);
/* 1387 */       if (obj instanceof Properties)
/*      */       {
/* 1389 */         Properties prop = (Properties)map.get(name);
/* 1390 */         if (prop != null)
/*      */         {
/* 1392 */           String isOptimizedStr = prop.getProperty("IsFieldSearchOptimized");
/* 1393 */           boolean isOptimized = StringUtils.convertToBool(isOptimizedStr, false);
/* 1394 */           if (isOptimized)
/*      */           {
/* 1396 */             FieldInfo fi = (FieldInfo)info.get(name);
/* 1397 */             implicitSdata.put(name, fi);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1404 */         Report.trace("indexer", "Error key-value pair in Field Design Map. key: " + name + "; value: " + obj, null);
/*      */       }
/*      */     }
/* 1407 */     return implicitSdata;
/*      */   }
/*      */ 
/*      */   public HashMap[][] retrieveSections(IndexerCollectionData data)
/*      */   {
/* 1412 */     Hashtable map = data.m_fieldDesignMap;
/* 1413 */     Hashtable info = data.m_fieldInfos;
/*      */ 
/* 1416 */     HashMap sdataSections = new HashMap();
/* 1417 */     HashMap implicitSdata = new HashMap();
/* 1418 */     HashMap fieldSections = new HashMap();
/* 1419 */     HashMap textSections = new HashMap();
/* 1420 */     for (Enumeration e = info.keys(); e.hasMoreElements(); )
/*      */     {
/* 1422 */       String name = (String)e.nextElement();
/* 1423 */       FieldInfo fi = (FieldInfo)info.get(name);
/* 1424 */       Properties prop = (Properties)map.get(name);
/* 1425 */       switch (fi.m_type)
/*      */       {
/*      */       case 3:
/*      */       case 5:
/*      */       case 11:
/* 1430 */         if (prop != null)
/*      */         {
/* 1432 */           String isOptimizedStr = prop.getProperty("isOptimized");
/* 1433 */           boolean isOptimized = StringUtils.convertToBool(isOptimizedStr, false);
/* 1434 */           if (isOptimized)
/*      */           {
/* 1436 */             implicitSdata.put(name, fi);
/*      */           }
/*      */         }
/* 1439 */         sdataSections.put(name, fi);
/* 1440 */         break;
/*      */       default:
/* 1442 */         if (prop != null)
/*      */         {
/* 1444 */           String isFieldSectionStr = prop.getProperty("IsFieldSection");
/* 1445 */           boolean isFieldSection = StringUtils.convertToBool(isFieldSectionStr, false);
/* 1446 */           if (isFieldSection)
/*      */           {
/* 1448 */             fieldSections.put(name, fi);
/*      */           }
/*      */ 
/* 1451 */           String isTextSdataStr = prop.getProperty("isOptimized");
/* 1452 */           boolean isTextSdata = StringUtils.convertToBool(isTextSdataStr, false);
/* 1453 */           if (isTextSdata)
/*      */           {
/* 1455 */             String sdName = "sd" + name;
/* 1456 */             FieldInfo sdFI = new FieldInfo();
/* 1457 */             sdFI.copy(fi);
/* 1458 */             sdFI.m_name = sdName;
/* 1459 */             sdataSections.put(sdName, sdFI);
/*      */           }
/*      */         }
/* 1462 */         textSections.put(name, fi);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1467 */     String textSdatas = this.m_config.getValue("TextSdataFields");
/* 1468 */     List l = StringUtils.appendListFromSequence(null, textSdatas, 0, textSdatas.length(), ',', '^', 32);
/* 1469 */     for (String textField : l)
/*      */     {
/* 1471 */       FieldInfo fi = (FieldInfo)info.get(textField);
/* 1472 */       if (fi != null)
/*      */       {
/* 1474 */         FieldInfo sdfi = new FieldInfo();
/* 1475 */         sdfi.copy(fi);
/* 1476 */         String sdName = "sd" + textField;
/* 1477 */         sdfi.m_name = sdName;
/* 1478 */         sdataSections.put(sdName, sdfi);
/*      */       }
/*      */       else
/*      */       {
/* 1483 */         FieldInfo sdfi = new FieldInfo();
/* 1484 */         String sdName = "sd" + textField;
/* 1485 */         sdfi.m_name = sdName;
/* 1486 */         sdataSections.put(sdName, sdfi);
/*      */       }
/*      */     }
/* 1489 */     return new HashMap[][] { { sdataSections, implicitSdata }, { textSections, fieldSections } };
/*      */   }
/*      */ 
/*      */   public String getMetaFieldsAsString()
/*      */   {
/* 1495 */     FieldInfo[] fis = getMetaFields(null);
/*      */ 
/* 1497 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 1498 */     for (int i = 0; i < fis.length; ++i)
/*      */     {
/* 1500 */       if (i != 0)
/*      */       {
/* 1502 */         builder.append(',');
/*      */       }
/* 1504 */       builder.append(fis[i].m_name);
/*      */     }
/* 1506 */     return builder.toString();
/*      */   }
/*      */ 
/*      */   public String createSections(String collectionID) throws DataException, ServiceException
/*      */   {
/* 1511 */     String sectionGroupName = "section_group_" + collectionID;
/*      */ 
/* 1513 */     HashMap[][] sections = retrieveSections(this.m_data.m_collectionDef);
/* 1514 */     this.m_data.setCachedObject("IndexableSections", sections);
/* 1515 */     this.m_data.setCachedObject("IndexSectionGroupName", sectionGroupName);
/* 1516 */     int returnCode = PluginFilters.filter("beforeOracleTextSectionsCreationFilter", this.m_idxWorkspace, null, this.m_data);
/* 1517 */     if (returnCode == -1)
/*      */     {
/* 1520 */       throw new DataException("csIndexerAbortByOracleTextSectionCreationFilter");
/*      */     }
/* 1522 */     if (returnCode == 1)
/*      */     {
/* 1524 */       return (String)this.m_data.getCachedObject("IndexSectionGroupName");
/*      */     }
/*      */ 
/* 1527 */     int maxSdataSize = this.m_config.getInteger("MaxNumSdataSections", 32);
/* 1528 */     if (sections[this.SDATA][0].size() > maxSdataSize)
/*      */     {
/* 1530 */       String msg = LocaleUtils.encodeMessage("csOracleTextNumSDataSectionExceedLimits", null, "" + sections[this.SDATA][0].size());
/* 1531 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 1544 */     DataBinder binder = new DataBinder();
/* 1545 */     binder.putLocal("groupName", sectionGroupName);
/*      */     try
/*      */     {
/* 1548 */       this.m_idxWorkspace.executeCallable("CdropSectionGroup", binder);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1552 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1554 */         Report.trace(this.m_tracingSection, null, e);
/*      */       }
/*      */     }
/*      */ 
/* 1558 */     String groupType = this.m_config.getValue("TextSectionGroupType");
/* 1559 */     binder.putLocal("groupType", groupType);
/* 1560 */     this.m_idxWorkspace.executeCallable("CaddSectionGroup", binder);
/*      */ 
/* 1563 */     String noExplicitSdataDefStr = this.m_config.getValue("OracleTextNoExplicitSdataSectionFields");
/* 1564 */     List noExplicitSdataDef = StringUtils.appendListFromSequenceSimple(null, noExplicitSdataDefStr);
/*      */ 
/* 1566 */     for (Iterator i$ = sections[this.SDATA][0].entrySet().iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*      */ 
/* 1568 */       FieldInfo fi = (FieldInfo)((Map.Entry)obj).getValue();
/* 1569 */       binder.putLocal("sectionName", fi.m_name);
/* 1570 */       binder.putLocal("tagName", fi.m_name);
/*      */ 
/* 1572 */       if (noExplicitSdataDef.contains(fi.m_name)) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1576 */       switch (fi.m_type)
/*      */       {
/*      */       case 5:
/* 1579 */         binder.putLocal("dataType", "DATE");
/* 1580 */         break;
/*      */       case 6:
/* 1582 */         binder.putLocal("dataType", "VARCHAR2");
/* 1583 */         break;
/*      */       default:
/* 1585 */         binder.putLocal("dataType", "NUMBER");
/*      */       }
/* 1587 */       if (sections[this.SDATA][1].get(fi.m_name) != null)
/*      */       {
/* 1589 */         this.m_idxWorkspace.executeCallable("CaddSDATAColumn", binder);
/*      */       }
/*      */       else
/*      */       {
/* 1593 */         this.m_idxWorkspace.executeCallable("CaddSDATASection", binder);
/*      */       }
/*      */  }
/*      */ 
/*      */ 
/* 1599 */     for (Iterator i$ = sections[this.TEXT][0].entrySet().iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*      */ 
/* 1601 */       FieldInfo fi = (FieldInfo)((Map.Entry)obj).getValue();
/* 1602 */       binder.putLocal("sectionName", fi.m_name);
/* 1603 */       binder.putLocal("tagName", fi.m_name);
/* 1604 */       binder.putLocal("dataType", "VARCHAR2");
/*      */ 
/* 1606 */       if (noExplicitSdataDef.contains(fi.m_name))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1628 */       this.m_idxWorkspace.executeCallable("CaddZoneSection", binder); }
/*      */ 
/*      */ 
/* 1632 */     for (String fieldName : this.m_data.m_collectionDef.m_securityInfos.keySet())
/*      */     {
/* 1634 */       fieldName = "z" + fieldName;
/* 1635 */       binder.putLocal("sectionName", fieldName);
/* 1636 */       binder.putLocal("tagName", fieldName);
/* 1637 */       binder.putLocal("dataType", "VARCHAR2");
/*      */ 
/* 1639 */       this.m_idxWorkspace.executeCallable("CaddZoneSection", binder);
/*      */     }
/*      */ 
/* 1645 */     binder.putLocal("sectionName", "IdcContent");
/* 1646 */     binder.putLocal("tagName", "IdcContent");
/* 1647 */     this.m_idxWorkspace.executeCallable("CaddZoneSection", binder);
/* 1648 */     return sectionGroupName;
/*      */   }
/*      */ 
/*      */   public void addIndexes(String tableName) throws DataException, ServiceException
/*      */   {
/* 1653 */     FieldInfo[] fields = this.m_idxWorkspace.getColumnList(tableName);
/* 1654 */     String indexableFields = this.m_config.getValue("OracleTextTableIndexList");
/* 1655 */     if (indexableFields == null)
/*      */     {
/* 1657 */       indexableFields = "dID,dDocTitle,dInDate";
/*      */     }
/* 1659 */     List indexableFieldList = StringUtils.appendListFromSequenceSimple(null, indexableFields);
/* 1660 */     for (int i = 0; i < fields.length; ++i)
/*      */     {
/* 1662 */       if ((fields[i].m_type != 3) && (fields[i].m_type != 5) && (fields[i].m_type != 6) && (!indexableFieldList.contains(fields[i].m_name)))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1667 */       Report.trace(this.m_tracingSection, "Creating index for column " + fields[i].m_name, null);
/* 1668 */       this.m_idxWorkspace.addIndex(tableName, new String[] { fields[i].m_name });
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addFullTextIndex(String tableName, String indexName, DataBinder binder) throws DataException, ServiceException
/*      */   {
/* 1674 */     this.m_config.setValue("fullTextIndexName", indexName);
/*      */ 
/* 1676 */     String params = this.m_config.getScriptValue("OracleTextFullTextParameters");
/* 1677 */     this.m_config.setValue("OracleTextFullTextParameters", params);
/* 1678 */     String query = this.m_config.getScriptValue("OracleFullTextIndexQuery");
/*      */ 
/* 1680 */     this.m_idxWorkspace.executeSQL(query);
/* 1681 */     PluginFilters.filter("afterOracleTextIndexCreation", this.m_idxWorkspace, binder, null);
/*      */   }
/*      */ 
/*      */   protected FieldInfo[] getMetaFields(ArrayList dateFields)
/*      */   {
/* 1707 */     List metaFieldOrders = StringUtils.makeListFromSequence(this.m_fieldOrders, ',', ',', 0);
/* 1708 */     Set set = this.m_data.m_collectionDef.m_fieldInfos.entrySet();
/* 1709 */     ArrayList metaFields = new ArrayList();
/* 1710 */     Map.Entry[] entries = new Map.Entry[set.size()];
/* 1711 */     set.toArray(entries);
/* 1712 */     Map fieldDesignMap = this.m_data.m_collectionDef.m_fieldDesignMap;
/* 1713 */     for (int i = 0; i < entries.length; ++i)
/*      */     {
/* 1715 */       FieldInfo fi = (FieldInfo)entries[i].getValue();
/* 1716 */       if ((fi.m_name == null) || (fi.m_name.length() == 0)) continue; if (fi.m_name.equalsIgnoreCase("web-cgi-root")) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1720 */       Properties prop = (Properties)fieldDesignMap.get(fi.m_name);
/* 1721 */       String isPlaceHolderField = prop.getProperty("dIsPlaceholderField");
/* 1722 */       if (StringUtils.convertToBool(isPlaceHolderField, false)) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1726 */       if ((dateFields != null) && (fi.m_type == 5))
/*      */       {
/* 1728 */         dateFields.add(fi.m_name);
/*      */       }
/* 1730 */       if (metaFieldOrders.contains(fi.m_name))
/*      */       {
/* 1732 */         metaFields.add(0, fi);
/*      */       }
/*      */       else
/*      */       {
/* 1736 */         metaFields.add(fi);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1742 */     int counter = 0;
/* 1743 */     for (int i = 0; i < metaFieldOrders.size(); ++i)
/*      */     {
/* 1745 */       String fieldName = (String)metaFieldOrders.get(i);
/* 1746 */       for (int j = counter; j < metaFields.size(); ++j)
/*      */       {
/* 1748 */         FieldInfo fi = (FieldInfo)metaFields.get(j);
/* 1749 */         if (!fi.m_name.equals(fieldName))
/*      */           continue;
/* 1751 */         if (j != counter)
/*      */         {
/* 1753 */           metaFields.set(j, metaFields.get(counter));
/* 1754 */           metaFields.set(counter, fi);
/*      */         }
/* 1756 */         ++counter;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1763 */     int memoSize = SharedObjects.getEnvironmentInt("MemoFieldSize", 2000);
/* 1764 */     for (FieldInfo fi : metaFields)
/*      */     {
/* 1766 */       if (fi.m_type != 6) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1770 */       if ((!fi.m_isFixedLen) || (fi.m_maxLen > memoSize))
/*      */       {
/* 1772 */         fi.m_maxLen = memoSize;
/*      */       }
/* 1774 */       else if (fi.m_maxLen < 7)
/*      */       {
/* 1776 */         fi.m_maxLen = 7;
/*      */       }
/*      */     }
/*      */ 
/* 1780 */     FieldInfo[] metaFieldArray = new FieldInfo[metaFields.size()];
/* 1781 */     metaFields.toArray(metaFieldArray);
/* 1782 */     return metaFieldArray;
/*      */   }
/*      */ 
/*      */   protected FieldInfo[] computeCollectionTableFields()
/*      */   {
/* 1789 */     FieldInfo[] additionalColumns = getMetaFields(null);
/* 1790 */     FieldInfo[] tableFields = new FieldInfo[additionalColumns.length + 5];
/*      */ 
/* 1793 */     tableFields[(tableFields.length - 5)] = new FieldInfo();
/* 1794 */     tableFields[(tableFields.length - 5)].m_name = "otsMeta";
/* 1795 */     tableFields[(tableFields.length - 5)].m_type = 10;
/*      */ 
/* 1797 */     tableFields[(tableFields.length - 4)] = new FieldInfo();
/* 1798 */     tableFields[(tableFields.length - 4)].m_name = "otsFormat";
/* 1799 */     tableFields[(tableFields.length - 4)].m_type = 6;
/* 1800 */     tableFields[(tableFields.length - 4)].m_maxLen = 50;
/*      */ 
/* 1802 */     tableFields[(tableFields.length - 3)] = new FieldInfo();
/* 1803 */     tableFields[(tableFields.length - 3)].m_name = "otsCharset";
/* 1804 */     tableFields[(tableFields.length - 3)].m_type = 6;
/* 1805 */     tableFields[(tableFields.length - 4)].m_maxLen = 50;
/*      */ 
/* 1807 */     tableFields[(tableFields.length - 2)] = new FieldInfo();
/* 1808 */     tableFields[(tableFields.length - 2)].m_name = "otsLanguage";
/* 1809 */     tableFields[(tableFields.length - 2)].m_type = 6;
/* 1810 */     tableFields[(tableFields.length - 4)].m_maxLen = 50;
/*      */ 
/* 1812 */     tableFields[(tableFields.length - 1)] = new FieldInfo();
/* 1813 */     tableFields[(tableFields.length - 1)].m_name = "otsContent";
/* 1814 */     tableFields[(tableFields.length - 1)].m_type = 9;
/*      */ 
/* 1818 */     for (int i = 0; i < additionalColumns.length; ++i)
/*      */     {
/* 1820 */       tableFields[i] = new FieldInfo();
/* 1821 */       tableFields[i].copy(additionalColumns[i]);
/* 1822 */       if (tableFields[i].m_type != 5)
/*      */         continue;
/* 1824 */       FieldInfoUtils.setFieldOption(tableFields[i], "DisableDBDateEnhancement", "true");
/*      */     }
/*      */ 
/* 1827 */     return tableFields;
/*      */   }
/*      */ 
/*      */   public void initQueries(boolean forceInit, String activeIndex) throws ServiceException
/*      */   {
/* 1832 */     if (!forceInit)
/*      */     {
/* 1834 */       Object queries = SharedObjects.getObject("OracleTextSearch", "Queries" + activeIndex);
/* 1835 */       if (queries != null)
/*      */       {
/* 1837 */         return;
/*      */       }
/*      */     }
/*      */ 
/* 1841 */     String tableName = OracleTextUtils.getTableName(activeIndex, this.m_config);
/* 1842 */     if (tableName == null)
/*      */     {
/* 1845 */       return;
/*      */     }
/* 1847 */     String deleteQuery = "DELETE FROM " + tableName + " WHERE dDocName = ?";
/* 1848 */     String deleteParam = "dDocName varchar";
/*      */ 
/* 1850 */     IdcStringBuilder updateMetaOnly = new IdcStringBuilder();
/* 1851 */     IdcStringBuilder metaOnlyParam = new IdcStringBuilder();
/*      */ 
/* 1853 */     IdcStringBuilder updateFull = new IdcStringBuilder();
/* 1854 */     IdcStringBuilder fullUpdateParams = new IdcStringBuilder();
/*      */ 
/* 1856 */     IdcStringBuilder insertFull = new IdcStringBuilder();
/* 1857 */     IdcStringBuilder insertFullValues = new IdcStringBuilder();
/* 1858 */     IdcStringBuilder fullParams = new IdcStringBuilder();
/*      */ 
/* 1860 */     updateMetaOnly.append("UPDATE ");
/* 1861 */     updateMetaOnly.append(tableName);
/* 1862 */     updateMetaOnly.append(" SET ");
/*      */ 
/* 1864 */     updateFull.append("UPDATE ");
/* 1865 */     updateFull.append(tableName);
/* 1866 */     updateFull.append(" SET ");
/*      */ 
/* 1868 */     insertFull.append("INSERT INTO ");
/* 1869 */     insertFull.append(tableName);
/* 1870 */     insertFull.append('(');
/* 1871 */     insertFullValues.append(" VALUES(");
/*      */ 
/* 1873 */     boolean metaOnlyParamAdded = false;
/* 1874 */     boolean updateFullParamAdded = false;
/*      */ 
/* 1876 */     FieldInfo[] tableFields = null;
/*      */     try
/*      */     {
/* 1879 */       tableFields = this.m_idxWorkspace.getColumnList(tableName);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1884 */       Report.trace("indexer", "Cannot retrieve columns from indexer table '" + tableName + "'", e);
/* 1885 */       return;
/*      */     }
/* 1887 */     for (int i = 0; i < tableFields.length; ++i)
/*      */     {
/* 1889 */       FieldInfo fi = tableFields[i];
/*      */ 
/* 1891 */       if (i != 0)
/*      */       {
/* 1893 */         insertFull.append(',');
/* 1894 */         insertFullValues.append(',');
/*      */       }
/*      */ 
/* 1898 */       if (!fi.m_name.equalsIgnoreCase("otsContent"))
/*      */       {
/* 1900 */         if (metaOnlyParamAdded)
/*      */         {
/* 1902 */           updateMetaOnly.append(", ");
/*      */         }
/*      */         else
/*      */         {
/* 1906 */           metaOnlyParamAdded = true;
/*      */         }
/* 1908 */         updateMetaOnly.append(fi.m_name);
/* 1909 */         updateMetaOnly.append(" = ? ");
/*      */ 
/* 1911 */         metaOnlyParam.append(fi.m_name);
/* 1912 */         metaOnlyParam.append(' ');
/* 1913 */         metaOnlyParam.append(fi.getTypeName());
/* 1914 */         metaOnlyParam.append('\n');
/*      */       }
/*      */ 
/* 1917 */       if (updateFullParamAdded)
/*      */       {
/* 1919 */         updateFull.append(", ");
/*      */       }
/*      */       else
/*      */       {
/* 1923 */         updateFullParamAdded = true;
/*      */       }
/* 1925 */       updateFull.append(fi.m_name);
/* 1926 */       updateFull.append(" = ? ");
/* 1927 */       fullUpdateParams.append(fi.m_name);
/* 1928 */       fullUpdateParams.append(' ');
/* 1929 */       fullUpdateParams.append(fi.getTypeName());
/* 1930 */       fullUpdateParams.append('\n');
/*      */ 
/* 1933 */       fullParams.append(fi.m_name);
/* 1934 */       fullParams.append(' ');
/* 1935 */       fullParams.append(fi.getTypeName());
/* 1936 */       fullParams.append('\n');
/*      */ 
/* 1938 */       insertFullValues.append('?');
/*      */ 
/* 1941 */       insertFull.append(fi.m_name);
/*      */     }
/*      */ 
/* 1944 */     insertFull.append(')');
/* 1945 */     insertFull.append(insertFullValues);
/* 1946 */     insertFull.append(')');
/*      */ 
/* 1948 */     updateFull.append(" WHERE dDocName = ?");
/* 1949 */     fullUpdateParams.append("dDocName varchar");
/* 1950 */     updateMetaOnly.append(" WHERE dDocName = ?");
/* 1951 */     metaOnlyParam.append("dDocName varchar");
/*      */ 
/* 1953 */     DataResultSet drset = new DataResultSet(new String[] { "name", "queryStr", "parameters" });
/* 1954 */     QueryUtils.addQueryDef(drset, "(Oracle.PREPARED)DidcText" + activeIndex, deleteQuery, deleteParam);
/* 1955 */     QueryUtils.addQueryDef(drset, "(Oracle.PREPARED)UidcTextMetaOnly" + activeIndex, updateMetaOnly.toString(), metaOnlyParam.toString());
/*      */ 
/* 1957 */     QueryUtils.addQueryDef(drset, "(Oracle.PREPARED)UidcTextFull" + activeIndex, updateFull.toString(), fullUpdateParams.toString());
/* 1958 */     QueryUtils.addQueryDef(drset, "(Oracle.PREPARED)IidcText" + activeIndex, insertFull.toString(), fullParams.toString());
/*      */     try
/*      */     {
/* 1961 */       this.m_idxWorkspace.addQueryDefs(drset);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1965 */       throw new ServiceException(e);
/*      */     }
/* 1967 */     SharedObjects.putObject("OracleTextSearch", "Queries" + activeIndex, Boolean.TRUE);
/*      */   }
/*      */ 
/*      */   public void cleanUp(IndexerWorkObject data)
/*      */     throws ServiceException
/*      */   {
/* 1973 */     if (this.m_idxWorkspace == null)
/*      */     {
/* 1975 */       Report.trace("indexer", "Workspace is null. Skipping cleanup", null);
/* 1976 */       return;
/*      */     }
/* 1978 */     boolean useRebuildOpt = false;
/* 1979 */     String indexName = null;
/* 1980 */     if (this.m_isRebuild)
/*      */     {
/* 1982 */       indexName = this.m_shadowIndexName;
/* 1983 */       if (indexName == null)
/*      */       {
/* 1985 */         indexName = OracleTextUtils.getFullTextIndexName(this.m_activeIndex, this.m_config, this.m_idxWorkspace);
/*      */       }
/*      */ 
/* 1988 */       int timeout = this.m_idxWorkspace.getThreadTimeout();
/* 1989 */       this.m_idxWorkspace.clearThreadTimeout();
/*      */       try
/*      */       {
/* 1996 */         useRebuildOpt = data.m_state.getCurrentState().equals("FinishRebuild");
/*      */ 
/* 1998 */         if (this.m_shadowIndexName != null)
/*      */         {
/* 2000 */           indexName = OracleTextUtils.getFullTextIndexName(this.m_activeIndex, this.m_config, this.m_idxWorkspace);
/* 2001 */           DataBinder binder = new DataBinder();
/* 2002 */           binder.putLocal("indexName", indexName);
/* 2003 */           this.m_idxWorkspace.executeCallable("CoracleTextExchangeShadowIndex", binder);
/* 2004 */           this.m_shadowIndexName = null;
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*      */       }
/*      */       finally
/*      */       {
/* 2013 */         this.m_idxWorkspace.setThreadTimeout(timeout);
/*      */       }
/*      */ 
/* 2017 */       String tableName = null;
/*      */       try
/*      */       {
/* 2020 */         if ((useRebuildOpt) && (this.m_deletedColumns != null) && (this.m_deletedColumns.length > 0))
/*      */         {
/* 2022 */           tableName = OracleTextUtils.getTableName(this.m_activeIndex, this.m_config);
/* 2023 */           this.m_idxWorkspace.alterTable(tableName, null, this.m_deletedColumns, new String[] { "dDocName" });
/*      */         }
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 2028 */         String columns = StringUtils.createStringFromArray(this.m_deletedColumns);
/* 2029 */         Report.info("indexer", t, "csOracleTextUnableToDeleteColumnsFromIndexingTable", new Object[] { tableName, columns });
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 2035 */       IndexerState state = (IndexerState)data.getCachedObject("IndexerState");
/*      */ 
/* 2037 */       int lastQuickOptimizationCount = state.getCollectionInteger("lastFastOptimizationCount", true);
/* 2038 */       int lastRebuildOptimizationCount = state.getCollectionInteger("lastRebuildOptimizationCount", true);
/* 2039 */       int rebuildDirtyRatio = this.m_config.getInteger("RebuildOptimizationDirtyRatio", 20);
/*      */ 
/* 2041 */       int quickOpInterval = this.m_config.getInteger("FastOptimizationInterval", 5000);
/* 2042 */       int rebuildOptimizationInterval = NumberUtils.parseInteger(this.m_config.getConfigValue("RebuildOptimizationInterval"), 50000);
/*      */ 
/* 2044 */       indexName = OracleTextUtils.getFullTextIndexName(this.m_activeIndex, this.m_config, this.m_idxWorkspace);
/*      */ 
/* 2046 */       int totalCount = state.m_cumTotalAddIndex + state.m_cumTotalDeleteIndex;
/* 2047 */       if ((totalCount > rebuildOptimizationInterval + lastRebuildOptimizationCount) && (totalCount > lastRebuildOptimizationCount * (1.0D + rebuildDirtyRatio / 100.0D)))
/*      */       {
/* 2051 */         useRebuildOpt = data.m_state.getCurrentState().equals("Finished");
/*      */       }
/* 2053 */       else if ((totalCount >= lastQuickOptimizationCount + quickOpInterval) && (this.m_config.getBoolean("EnableFastOptimizationDuringIndexing", false)))
/*      */       {
/* 2058 */         doFastOptimization(indexName);
/* 2059 */         state.setCollectionValue("lastFastOptimizationCount", "" + totalCount);
/*      */       }
/*      */     }
/*      */ 
/* 2063 */     if (useRebuildOpt)
/*      */     {
/* 2065 */       String jobName = "Opt_" + indexName;
/*      */       try
/*      */       {
/* 2068 */         if (data.isRebuild())
/*      */         {
/* 2071 */           Report.trace("indexer", "Check and remove optimization job for old index '" + indexName + "'", null);
/* 2072 */           String oldIndexName = ActiveIndexState.getActiveProperty("ActiveIndex");
/* 2073 */           oldIndexName = OracleTextUtils.getFullTextIndexName(oldIndexName, this.m_config, this.m_idxWorkspace);
/* 2074 */           checkScheduledOptimizationJob(oldIndexName, data, true);
/*      */         }
/* 2076 */         checkAndRegisterRebuildOptimization(jobName, indexName, data);
/*      */ 
/* 2078 */         this.m_hasWarnedOptimizationTaskError = false;
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 2082 */         if (!this.m_hasWarnedOptimizationTaskError)
/*      */         {
/* 2084 */           Report.warning("indexer", e, "csOracleTextAddTaskError", new Object[0]);
/* 2085 */           this.m_hasWarnedOptimizationTaskError = true;
/*      */         }
/* 2087 */         Report.trace("indexer", "Error occurred while adding the optimization task '" + jobName + "'.", e);
/*      */       }
/*      */     }
/* 2090 */     super.cleanUp(data);
/*      */ 
/* 2092 */     this.m_idxWorkspace.releaseConnection();
/*      */   }
/*      */ 
/*      */   protected void checkAndRegisterRebuildOptimization(String jobName, String indexName, IndexerWorkObject data)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 2100 */       ScheduledJobStorage jobStorage = ScheduledJobManager.getStorage(data.m_workspace);
/* 2101 */       DataBinder binder = new DataBinder();
/* 2102 */       binder.putLocal("dSjName", jobName);
/* 2103 */       binder.putLocal("dSjCategory", "indexer");
/* 2104 */       binder.putLocal("dSjInitUser", "sysadmin");
/*      */ 
/* 2106 */       boolean taskExists = checkScheduledOptimizationJob(indexName, data, false);
/*      */ 
/* 2108 */       if (!taskExists)
/*      */       {
/* 2110 */         binder.putLocal("sjClassName", "intradoc.indexer.OracleTextOptimizerScheduledJobs");
/* 2111 */         binder.putLocal("cycleId", data.m_cycleId);
/* 2112 */         binder.putLocal("restartId", data.m_state.getStateValue("RestartId"));
/* 2113 */         binder.putLocal("CollectionDatabaseProvider", OracleTextUtils.getWorkspaceProviderName(data));
/* 2114 */         binder.putLocal("indexName", indexName);
/* 2115 */         binder.putLocal("optLevel", "REBUILD");
/* 2116 */         binder.putLocal("dSjInterval", "0h");
/* 2117 */         binder.putLocal("dSjDescription", "csOracleTextOptimizeRebuildJobDescription");
/* 2118 */         setStartTokenAndType(binder);
/*      */ 
/* 2120 */         String collDir = this.m_config.getConfigValue("IndexerCollectionDir");
/* 2121 */         if (collDir != null)
/*      */         {
/* 2123 */           binder.putLocal("collectionDir", collDir);
/*      */         }
/*      */ 
/* 2126 */         Report.trace("indexer", "Adding task '" + jobName + "' to scheduled jobs.", null);
/* 2127 */         jobStorage.addTask(binder, data.m_workspace, data);
/*      */       }
/* 2129 */       this.m_hasWarnedOptimizationTaskError = false;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2133 */       if (!this.m_hasWarnedOptimizationTaskError)
/*      */       {
/* 2135 */         Report.warning("indexer", e, "csOracleTextAddTaskError", new Object[] { e });
/* 2136 */         this.m_hasWarnedOptimizationTaskError = true;
/*      */       }
/* 2138 */       Report.trace("indexer", "Error occurred while adding optimization task '" + jobName + "'.", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean checkScheduledOptimizationJob(String indexName, IndexerWorkObject data, boolean isOld) throws ServiceException, DataException
/*      */   {
/* 2144 */     String dSjName = "Opt_" + indexName;
/* 2145 */     ScheduledJobStorage jobStorage = ScheduledJobManager.getStorage(data.m_workspace);
/* 2146 */     DataBinder binder = new DataBinder();
/* 2147 */     binder.putLocal("dSjName", dSjName);
/*      */ 
/* 2149 */     boolean taskExists = false;
/* 2150 */     ResultSet rset = data.m_workspace.createResultSet("QscheduledJob", binder);
/* 2151 */     if (rset.isRowPresent())
/*      */     {
/* 2153 */       Map props = ResultSetUtils.getCurrentRowMap(rset);
/* 2154 */       String lastStatus = (String)props.get("dSjLastProcessedStatus");
/* 2155 */       String state = (String)props.get("dSjState");
/* 2156 */       if ((((isOld) || ((lastStatus != null) && (lastStatus.equals("F"))))) && (((state == null) || (!state.equals("A")))))
/*      */       {
/* 2158 */         if (isOld)
/*      */         {
/* 2160 */           Report.trace("indexer", "Old task '" + dSjName + "' exists, and will be deleted. " + " dSjState is '" + state + "'. Last processed status is '" + lastStatus + "'", null);
/*      */         }
/*      */         else
/*      */         {
/* 2165 */           Report.trace("indexer", "Failed task '" + dSjName + "' exists, and will be deleted. " + " dSjState is '" + state + "'. Last processed status is '" + lastStatus + "'", null);
/*      */         }
/*      */ 
/* 2169 */         ScheduledJobsProcessor proc = ScheduledJobManager.getProcessor(data.m_workspace);
/*      */ 
/* 2172 */         JobState jState = new JobState();
/* 2173 */         String dir = jobStorage.getActiveDir();
/* 2174 */         String id = ScheduledJobUtils.getId(props);
/* 2175 */         proc.initJobStateObject(jState, props, dir, id);
/*      */ 
/* 2177 */         jobStorage.deleteJob(jState, false, data.m_workspace, data);
/*      */       }
/*      */       else
/*      */       {
/* 2181 */         taskExists = true;
/* 2182 */         Report.trace("indexer", "Task '" + dSjName + "' already exists. " + " dSjState is '" + state + "'. Last processed status is '" + lastStatus + "'", null);
/*      */       }
/*      */     }
/*      */ 
/* 2186 */     return taskExists;
/*      */   }
/*      */ 
/*      */   protected void setStartTokenAndType(DataBinder binder)
/*      */   {
/* 2191 */     Calendar c = Calendar.getInstance();
/* 2192 */     int hour = c.get(11);
/* 2193 */     String dedicatedHourStr = this.m_config.getConfigValue("OracleTextOptStartTime");
/* 2194 */     int dedicatedHour = NumberUtils.parseInteger(dedicatedHourStr, 0);
/* 2195 */     int diff = dedicatedHour - hour;
/*      */ 
/* 2197 */     if (diff == 0)
/*      */     {
/* 2199 */       binder.putLocal("dSjType", "I");
/*      */     }
/*      */     else
/*      */     {
/* 2203 */       if (diff < 0)
/*      */       {
/* 2205 */         diff += 24;
/*      */       }
/* 2207 */       c.add(11, diff);
/*      */     }
/* 2209 */     String time = LocaleResources.m_odbcFormat.format(c.getTime());
/* 2210 */     binder.putLocal("dSjStartToken", "\"" + time + "\"");
/*      */   }
/*      */ 
/*      */   protected void doFastOptimization(String indexName)
/*      */   {
/* 2215 */     int timeout = this.m_idxWorkspace.getThreadTimeout();
/* 2216 */     this.m_idxWorkspace.clearThreadTimeout();
/* 2217 */     String semInSession = null;
/* 2218 */     boolean resetSemBack = false;
/*      */     try
/*      */     {
/* 2221 */       DataBinder binder = new DataBinder();
/* 2222 */       binder.putLocal("indexName", indexName);
/* 2223 */       binder.putLocal("optLevel", "FAST");
/*      */ 
/* 2225 */       semInSession = OracleTextUtils.getLengthSemanticsFromSession(this.m_idxWorkspace);
/* 2226 */       resetSemBack = OracleTextUtils.checkAndSetLengthSemantics(this.m_idxWorkspace, "DR$" + indexName.toUpperCase() + "$I", "TOKEN_TEXT", semInSession);
/*      */ 
/* 2228 */       String parallels = this.m_config.getConfigValue("IndexRebuildParallelDegree");
/* 2229 */       if (parallels != null)
/*      */       {
/* 2231 */         binder.putLocal("parallelDegree", parallels);
/*      */       }
/* 2233 */       Report.trace(this.m_tracingSection, "Starting index optimization on '" + indexName + "' with parallel degree of " + parallels + ".", null);
/*      */ 
/* 2235 */       this.m_data.reportProgress(2, "csOracleTextFastOptimization", -1.0F, -1.0F);
/*      */ 
/* 2237 */       this.m_idxWorkspace.executeCallable("CoracleTextOptimizeIndex", binder);
/* 2238 */       Report.trace(this.m_tracingSection, "End index optimization", null);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 2242 */       if (!this.m_hasWarnedOptimizationError)
/*      */       {
/* 2244 */         Report.warning("indexer", e, null);
/* 2245 */         this.m_hasWarnedOptimizationError = true;
/*      */       }
/* 2247 */       Report.trace(this.m_tracingSection, null, e);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 2251 */       String msg = LocaleUtils.encodeMessage("Failed to get semantics from table. Index Optimization failed", null);
/* 2252 */       Report.trace(this.m_tracingSection, msg, e);
/*      */     }
/*      */     finally
/*      */     {
/* 2256 */       if (resetSemBack)
/*      */       {
/*      */         try
/*      */         {
/* 2260 */           OracleTextUtils.setLengthSemantics(this.m_idxWorkspace, semInSession);
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/* 2264 */           String msg = LocaleUtils.encodeMessage("Failed to set Length Semantics in session.", null);
/* 2265 */           Report.trace(this.m_tracingSection, msg, e);
/*      */         }
/*      */       }
/* 2268 */       this.m_idxWorkspace.setThreadTimeout(timeout);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void validateConfiguration()
/*      */   {
/* 2275 */     String isValidConfig = "true";
/*      */ 
/* 2277 */     if (this.m_idxWorkspace == null)
/*      */     {
/* 2279 */       isValidConfig = "false";
/*      */     }
/*      */ 
/* 2282 */     this.m_config.setValue("SearchEngineValidConfig", isValidConfig);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2288 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94019 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.OracleTextCollectionHandler
 * JD-Core Version:    0.5.4
 */