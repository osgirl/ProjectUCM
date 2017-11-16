/*     */ package intradoc.server.schema;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.schema.HashableStringArray;
/*     */ import intradoc.shared.schema.SchemaCacheItem;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.shared.schema.SchemaLoader;
/*     */ import intradoc.shared.schema.SchemaLoaderUtils;
/*     */ import intradoc.shared.schema.SchemaRelationConfig;
/*     */ import intradoc.shared.schema.SchemaRelationData;
/*     */ import intradoc.shared.schema.SchemaTableConfig;
/*     */ import intradoc.shared.schema.SchemaTableData;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.shared.schema.ViewCacheCallback;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ServerSchemaLoader extends ExecutionContextAdaptor
/*     */   implements SchemaLoader
/*     */ {
/*     */   protected Workspace m_workspace;
/*     */   protected SchemaUtils m_schemaUtils;
/*     */   protected SchemaPublisherThread m_publisherThread;
/*     */   protected SchemaViewConfig m_views;
/*     */   protected SchemaTableConfig m_tables;
/*     */   protected SchemaRelationConfig m_relations;
/*     */   protected int m_schemaCacheTimeout;
/*     */   protected int m_schemaSubjectNotificationTimeout;
/*     */   protected int m_timestampCheckMinimumInterval;
/*     */   protected Map m_capabilities;
/*     */   protected Hashtable m_lastGlobalViewTimestamps;
/*     */   protected Map<String, String> m_tableWorkspaceMap;
/*     */ 
/*     */   public ServerSchemaLoader()
/*     */   {
/*  46 */     this.m_lastGlobalViewTimestamps = new Hashtable();
/*     */   }
/*     */ 
/*     */   public boolean init(Map initData)
/*     */     throws ServiceException
/*     */   {
/*  52 */     this.m_workspace = ((Workspace)initData.get("Workspace"));
/*  53 */     this.m_publisherThread = ((SchemaPublisherThread)initData.get("SchemaPublisherThread"));
/*     */ 
/*  57 */     this.m_schemaCacheTimeout = getTimeout("ServerSchemaLoaderDefaultTimeout", 300, initData);
/*     */ 
/*  61 */     this.m_schemaSubjectNotificationTimeout = getTimeout("ServerSchemaLoaderSubjectNotificationTimeout", 18000, initData);
/*     */ 
/*  64 */     this.m_timestampCheckMinimumInterval = getTimeout("ServerSchemaLoaderMinimumTimestampInterval", 30, initData);
/*     */ 
/*  67 */     this.m_schemaUtils = ((SchemaUtils)initData.get("SchemaUtils"));
/*  68 */     if (this.m_schemaUtils == null)
/*     */     {
/*  70 */       this.m_schemaUtils = ((SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", "!csSchemaUnableToLoadUtils"));
/*     */ 
/*  74 */       this.m_schemaUtils.init(initData);
/*     */     }
/*     */ 
/*  77 */     this.m_views = ((SchemaViewConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaViewConfig"));
/*     */ 
/*  79 */     this.m_tables = ((SchemaTableConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaTableConfig"));
/*     */ 
/*  81 */     this.m_relations = ((SchemaRelationConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaRelationConfig"));
/*     */ 
/*  84 */     this.m_capabilities = new HashMap();
/*  85 */     SchemaLoaderUtils.addStandardCapabilities(this.m_capabilities, 7);
/*     */ 
/*  90 */     this.m_tableWorkspaceMap = new HashMap();
/*  91 */     DataResultSet drset = SharedObjects.getTable("NonsystemWorkspaceTables");
/*  92 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/*  94 */       String ws = drset.getStringValueByName("Workspace");
/*  95 */       String tables = drset.getStringValueByName("Tables");
/*  96 */       Vector vtables = StringUtils.parseArray(tables, ',', ',');
/*  97 */       for (int i = 0; i < vtables.size(); ++i)
/*     */       {
/*  99 */         this.m_tableWorkspaceMap.put(vtables.get(i), ws);
/*     */       }
/*     */     }
/*     */ 
/* 103 */     return true;
/*     */   }
/*     */ 
/*     */   protected int getTimeout(String key, int defaultValue, Map initData)
/*     */   {
/* 109 */     String timeout = (String)initData.get(key);
/*     */     int rc;
/*     */     int rc;
/* 110 */     if (timeout == null)
/*     */     {
/* 112 */       rc = SharedObjects.getTypedEnvironmentInt(key, defaultValue * 1000, 18, 24);
/*     */     }
/*     */     else
/*     */     {
/* 118 */       rc = NumberUtils.parseTypedInteger(timeout, defaultValue * 1000, 18, 24);
/*     */     }
/*     */ 
/* 122 */     return rc;
/*     */   }
/*     */ 
/*     */   public Map getLoaderCapabilities(SchemaViewData view, Map args)
/*     */   {
/* 127 */     String viewType = view.get("schViewType");
/* 128 */     if ((viewType != null) && (((viewType.equalsIgnoreCase("table")) || (viewType.equalsIgnoreCase("optionList")))))
/*     */     {
/* 132 */       return this.m_capabilities;
/*     */     }
/*     */ 
/* 135 */     return new HashMap();
/*     */   }
/*     */ 
/*     */   public Map getLoaderCapabilities(SchemaRelationData relationship, Map args)
/*     */   {
/* 140 */     String relationType = relationship.get("schRelationType");
/* 141 */     if ((relationType != null) && (!relationType.equalsIgnoreCase("table")))
/*     */     {
/* 143 */       return new HashMap();
/*     */     }
/* 145 */     return this.m_capabilities;
/*     */   }
/*     */ 
/*     */   public boolean isDirty(SchemaCacheItem item, Map args) throws DataException
/*     */   {
/* 150 */     long now = System.currentTimeMillis();
/*     */ 
/* 155 */     item.m_externalData.remove("ServerSchemaLoaderResultSetIsComplete");
/*     */ 
/* 157 */     String viewName = item.getViewName();
/* 158 */     SchemaViewData viewDefinition = (SchemaViewData)this.m_views.getData(viewName);
/*     */ 
/* 160 */     if (viewDefinition == null)
/*     */     {
/* 162 */       Report.trace("schemaloader", "isDirty() view '" + viewName + "' doesn't exist.", null);
/*     */ 
/* 166 */       return false;
/*     */     }
/*     */ 
/* 169 */     String tableName = viewDefinition.get("schTableName");
/* 170 */     SchemaTableData tableDefinition = (SchemaTableData)this.m_tables.getData(tableName);
/*     */ 
/* 172 */     if (tableDefinition == null)
/*     */     {
/* 174 */       Report.trace("schemaloader", "isDirty() table '" + tableName + "' doesn't exist.", null);
/*     */     }
/*     */ 
/* 179 */     String relationName = item.getRelationshipName();
/* 180 */     SchemaRelationData relationDefinition = null;
/* 181 */     if (relationName != null)
/*     */     {
/* 183 */       relationDefinition = (SchemaRelationData)this.m_relations.getData(relationName);
/*     */ 
/* 185 */       if (relationDefinition == null)
/*     */       {
/* 187 */         Report.trace("schemaloader", "isDirty() relationship '" + relationName + "' doesn't exist.", null);
/*     */ 
/* 191 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 195 */     boolean isDirty = item.isMarkedDirty();
/* 196 */     if (isDirty)
/*     */     {
/* 203 */       HashableStringArray key = item.computeCacheKey(args);
/* 204 */       if (key != null)
/*     */       {
/* 206 */         isDirty = item.getCacheEntryFromKey(key) == null;
/*     */       }
/* 208 */       if (isDirty)
/*     */       {
/* 210 */         if (SystemUtils.m_verbose)
/*     */         {
/* 212 */           Report.debug("schemaloader", "item '" + item + "' is dirty because it is marked.", null);
/*     */         }
/*     */ 
/* 215 */         return true;
/*     */       }
/* 217 */       if (SystemUtils.m_verbose)
/*     */       {
/* 219 */         Report.debug("schemaloader", "item '" + item + "' is not dirty even though marked because it has been updated " + "since being marked dirty for this entry.", null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 225 */     isDirty = item.objectVersionsDirty(viewDefinition, tableDefinition, relationDefinition);
/*     */ 
/* 227 */     if (isDirty)
/*     */     {
/* 229 */       if (SystemUtils.m_verbose)
/*     */       {
/* 231 */         Report.debug("schemaloader", "item '" + item + "' dirty due to version mismatch.", null);
/*     */       }
/*     */ 
/* 234 */       return true;
/*     */     }
/*     */ 
/* 237 */     int interval = this.m_timestampCheckMinimumInterval;
/* 238 */     String intervalStr = viewDefinition.get("ServerSchemaLoaderMinimumTimestampInterval");
/*     */ 
/* 240 */     if (intervalStr != null)
/*     */     {
/* 242 */       interval = NumberUtils.parseTypedInteger(intervalStr, this.m_timestampCheckMinimumInterval, 18, 24);
/*     */     }
/*     */ 
/* 246 */     long[] loaderTimestamp = (long[])(long[])item.m_externalData.get("ServerSchemaLoaderTimestamp");
/*     */ 
/* 248 */     if (loaderTimestamp != null)
/*     */     {
/* 250 */       long delta = now - loaderTimestamp[0];
/* 251 */       if (delta < interval)
/*     */       {
/* 253 */         if (SystemUtils.m_verbose)
/*     */         {
/* 255 */           Report.debug("schemaloader", "skipping query for item " + item, null);
/*     */         }
/*     */ 
/* 264 */         return false;
/*     */       }
/* 266 */       if (SystemUtils.m_verbose)
/*     */       {
/* 268 */         Report.debug("schemaloader", "not skipping query for item " + item + ".  delta was " + delta + " limit is " + interval, null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 274 */     StringBuffer query = new StringBuffer();
/* 275 */     int flags = this.m_schemaUtils.appendUptodateQuery(query, item, viewDefinition, relationDefinition);
/*     */ 
/* 277 */     boolean isAllRow = (flags & SchemaUtils.F_ALLROWS_TIMESTAMP_QUERY) != 0;
/*     */ 
/* 279 */     if (isAllRow)
/*     */     {
/* 281 */       long[] timestampInfo = (long[])(long[])this.m_lastGlobalViewTimestamps.get(viewDefinition.m_name);
/*     */ 
/* 283 */       if (timestampInfo != null)
/*     */       {
/* 285 */         long queryResult = timestampInfo[0];
/* 286 */         long queryTime = timestampInfo[1];
/* 287 */         if (now - queryTime < interval)
/*     */         {
/* 289 */           return item.isDirty(queryResult);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 294 */     if ((flags & SchemaUtils.F_NO_QUERY) == 0)
/*     */     {
/* 296 */       boolean isMultirow = (flags & SchemaUtils.F_MULTIROW_TIMESTAMP_QUERY) != 0;
/*     */ 
/* 298 */       String theQuery = query.toString();
/* 299 */       if (SystemUtils.m_verbose)
/*     */       {
/* 301 */         Report.debug("schemaloader", "checking timestamps for view '" + viewName + "' relation '" + relationName + "' with query '" + theQuery, null);
/*     */       }
/*     */ 
/* 306 */       item.m_externalData.remove("ServerSchemaLoaderResultSet");
/*     */ 
/* 308 */       SchemaHelper helper = new SchemaHelper();
/* 309 */       DataResultSet drset = null;
/*     */       try
/*     */       {
/* 312 */         ResultSet rset = this.m_workspace.createResultSetSQL(theQuery);
/* 313 */         rset.setDateFormat(LocaleResources.m_odbcFormat);
/* 314 */         drset = new DataResultSet();
/* 315 */         int limit = helper.getViewLoadLimit(viewDefinition);
/* 316 */         drset.copy(rset, limit);
/*     */       }
/*     */       finally
/*     */       {
/*     */         Object obj;
/* 320 */         if (args != null)
/*     */         {
/* 322 */           Object obj = args.get("context");
/* 323 */           if (obj != null)
/*     */           {
/* 325 */             this.m_workspace.releaseConnection();
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 335 */       if (drset.isRowPresent())
/*     */       {
/* 337 */         item.m_externalData.put("ServerSchemaLoaderTimestamp", new long[] { now });
/*     */ 
/* 339 */         long maxTimestamp = 0L;
/* 340 */         Date newestTimestamp = null;
/* 341 */         if (isMultirow)
/*     */         {
/* 343 */           helper.checkCopyAborted(drset, viewDefinition);
/*     */ 
/* 345 */           String tsName = viewDefinition.get("schTableRowModifyTimestamp");
/* 346 */           FieldInfo tsInfo = new FieldInfo();
/* 347 */           if ((tsName == null) || (!drset.getFieldInfo(tsName, tsInfo)))
/*     */           {
/* 349 */             String msg = LocaleUtils.encodeMessage("csSchTimestampFieldMissing", null, tsName, viewDefinition.m_name, tableDefinition.m_name);
/*     */ 
/* 353 */             throw new DataException(msg);
/*     */           }
/* 355 */           for (drset.first(); drset.isRowPresent(); drset.next())
/*     */           {
/* 357 */             Date date = drset.getDateValue(tsInfo.m_index);
/*     */             long dateValue;
/* 359 */             if ((date == null) || ((dateValue = date.getTime()) <= maxTimestamp)) {
/*     */               continue;
/*     */             }
/* 362 */             maxTimestamp = dateValue;
/* 363 */             newestTimestamp = date;
/*     */           }
/*     */ 
/* 366 */           item.m_externalData.put("ServerSchemaLoaderResultSet", drset);
/*     */ 
/* 371 */           if (!args.containsKey("primaryKey"))
/*     */           {
/* 373 */             item.m_externalData.put("ServerSchemaLoaderResultSetIsComplete", "1");
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 378 */           newestTimestamp = drset.getDateValue(0);
/* 379 */           if (newestTimestamp != null)
/*     */           {
/* 381 */             maxTimestamp = newestTimestamp.getTime();
/*     */           }
/*     */         }
/*     */ 
/* 385 */         if (isAllRow)
/*     */         {
/* 387 */           long[] timestampInfo = { maxTimestamp, now };
/*     */ 
/* 389 */           this.m_lastGlobalViewTimestamps.put(viewDefinition.m_name, timestampInfo);
/*     */ 
/* 391 */           item.m_externalData.put("CachedAllRowTimestampInfo", timestampInfo);
/*     */         }
/*     */ 
/* 395 */         if (SystemUtils.m_verbose)
/*     */         {
/* 397 */           Report.debug("schemaloader", "got value '" + newestTimestamp + "'", null);
/*     */         }
/*     */ 
/* 400 */         if (newestTimestamp != null)
/*     */         {
/* 402 */           return item.isDirty(maxTimestamp);
/*     */         }
/* 404 */         if (SystemUtils.m_verbose)
/*     */         {
/* 406 */           Report.debug("schemaloader", "null timestamp, using notification checks", null);
/*     */         }
/*     */ 
/*     */       }
/* 412 */       else if (SystemUtils.m_verbose)
/*     */       {
/* 414 */         Report.debug("schemaloader", "no rows for view " + viewName, null);
/*     */       }
/*     */ 
/*     */     }
/* 421 */     else if (SystemUtils.m_verbose)
/*     */     {
/* 423 */       Report.debug("schemaloader", "no query for view " + viewName, null);
/*     */     }
/*     */ 
/* 428 */     if (this.m_publisherThread != null)
/*     */     {
/* 430 */       long time = this.m_publisherThread.getLastNotificationTime();
/* 431 */       if (item.isDirty(time))
/*     */       {
/* 433 */         return true;
/*     */       }
/* 435 */       long timestamp = item.getTimestamp();
/*     */ 
/* 438 */       return now - timestamp > this.m_schemaSubjectNotificationTimeout;
/*     */     }
/*     */ 
/* 448 */     return item.isDirty(now - this.m_schemaCacheTimeout);
/*     */   }
/*     */ 
/*     */   public SchemaViewData[] getParentViews(SchemaRelationData relationship)
/*     */     throws DataException
/*     */   {
/* 454 */     String relationshipType = relationship.get("schRelationType");
/* 455 */     if ((relationshipType != null) && (!relationshipType.equals("table")))
/*     */     {
/* 457 */       return null;
/*     */     }
/* 459 */     SchemaHelper helper = new SchemaHelper();
/* 460 */     String tableName = relationship.get("schTable1Table");
/* 461 */     SchemaTableData parentTable = (SchemaTableData)this.m_tables.getData(tableName);
/*     */ 
/* 463 */     if (parentTable == null)
/*     */     {
/* 465 */       throw new DataException("!apArchiveExportTableRelationExistsWithoutParent");
/*     */     }
/*     */ 
/* 468 */     Vector views = helper.computeViews(parentTable);
/* 469 */     Vector usableViews = new IdcVector();
/* 470 */     String parentColumn = relationship.get("schTable1Column");
/* 471 */     for (int i = 0; i < views.size(); ++i)
/*     */     {
/* 473 */       SchemaViewData view = (SchemaViewData)views.elementAt(i);
/* 474 */       String columnString = view.get("schViewColumns");
/* 475 */       Vector columns = StringUtils.parseArray(columnString, ',', '^');
/* 476 */       if (columns.indexOf(parentColumn) < 0)
/*     */         continue;
/* 478 */       usableViews.addElement(view);
/*     */     }
/*     */ 
/* 481 */     SchemaViewData[] viewsArray = new SchemaViewData[usableViews.size()];
/* 482 */     usableViews.copyInto(viewsArray);
/* 483 */     return viewsArray;
/*     */   }
/*     */ 
/*     */   public SchemaViewData[] getChildViews(SchemaRelationData relationship)
/*     */     throws DataException
/*     */   {
/* 489 */     String relationshipType = relationship.get("schRelationType");
/* 490 */     if ((relationshipType != null) && (!relationshipType.equals("table")))
/*     */     {
/* 492 */       return null;
/*     */     }
/*     */ 
/* 495 */     SchemaHelper helper = new SchemaHelper();
/* 496 */     String tableName = relationship.getRequired("schTable2Table");
/* 497 */     SchemaTableData childTable = (SchemaTableData)this.m_tables.getData(tableName);
/*     */ 
/* 499 */     if (childTable == null)
/*     */     {
/* 501 */       String msg = LocaleUtils.encodeMessage("csSchMissingDefinition_table", null, tableName);
/*     */ 
/* 503 */       throw new DataException(msg);
/*     */     }
/* 505 */     Vector views = helper.computeViews(childTable);
/* 506 */     SchemaViewData[] viewsArray = new SchemaViewData[views.size()];
/* 507 */     views.copyInto(viewsArray);
/* 508 */     return viewsArray;
/*     */   }
/*     */ 
/*     */   public SchemaRelationData[] getParentRelations(SchemaViewData childView)
/*     */     throws DataException
/*     */   {
/* 514 */     String viewType = childView.get("schViewType");
/* 515 */     if ((viewType == null) || ((!viewType.equalsIgnoreCase("table")) && (!viewType.equalsIgnoreCase("optionlist"))))
/*     */     {
/* 518 */       return null;
/*     */     }
/* 520 */     String tableName = childView.getRequired("schTableName");
/* 521 */     SchemaRelationConfig relations = (SchemaRelationConfig)this.m_relations.shallowClone();
/* 522 */     Vector list = new IdcVector();
/* 523 */     for (relations.first(); relations.isRowPresent(); relations.next())
/*     */     {
/* 525 */       SchemaRelationData relationship = (SchemaRelationData)relations.getData();
/*     */ 
/* 527 */       String relationType = relationship.get("schRelationType");
/* 528 */       if ((relationType != null) && (!relationType.equalsIgnoreCase("table"))) {
/*     */         continue;
/*     */       }
/*     */ 
/* 532 */       String table2 = relationship.get("schTable2Table");
/* 533 */       if (!tableName.equalsIgnoreCase(table2))
/*     */         continue;
/* 535 */       list.addElement(relationship);
/*     */     }
/*     */ 
/* 538 */     SchemaRelationData[] relationArray = new SchemaRelationData[list.size()];
/*     */ 
/* 540 */     list.copyInto(relationArray);
/* 541 */     return relationArray;
/*     */   }
/*     */ 
/*     */   public String[] constructParentFieldsArray(SchemaViewData parentView, SchemaRelationData relationship, SchemaViewData childView, Map args)
/*     */     throws DataException
/*     */   {
/* 548 */     if (parentView != null)
/*     */     {
/* 550 */       String viewType = parentView.get("schViewType");
/* 551 */       if ((viewType == null) || ((!viewType.equalsIgnoreCase("table")) && (!viewType.equalsIgnoreCase("optionlist"))))
/*     */       {
/* 554 */         return null;
/*     */       }
/* 556 */       String tableName = parentView.get("schTableName");
/* 557 */       if (tableName == null)
/*     */       {
/* 559 */         String msg = LocaleUtils.encodeMessage("csRequiredConfigFieldMissing", null, "schTableName");
/*     */ 
/* 561 */         throw new DataException(msg);
/*     */       }
/*     */     }
/* 564 */     String childTable = childView.get("schTableName");
/* 565 */     String table1 = relationship.get("schTable1Table");
/* 566 */     String table2 = relationship.get("schTable2Table");
/* 567 */     String field = null;
/* 568 */     Object isBackwardsRelationshipObj = null;
/* 569 */     if (args != null)
/*     */     {
/* 571 */       isBackwardsRelationshipObj = args.get("isBackwardsRelationship");
/*     */     }
/*     */ 
/* 576 */     boolean doingBackwardsRelationship = ScriptUtils.convertObjectToBool(isBackwardsRelationshipObj, false);
/*     */ 
/* 578 */     if ((isBackwardsRelationshipObj == null) && (table1.equalsIgnoreCase(childTable)) && (!table2.equalsIgnoreCase(childTable)))
/*     */     {
/* 584 */       doingBackwardsRelationship = true;
/*     */     }
/*     */     String key;
/*     */     String reportPath;
/*     */     String key;
/* 588 */     if (doingBackwardsRelationship)
/*     */     {
/* 590 */       String reportPath = "child -> parent (reversing normal), parentView: ";
/* 591 */       key = "schTable2Column";
/*     */     }
/*     */     else
/*     */     {
/* 595 */       reportPath = "parent -> child, parentView: ";
/* 596 */       key = "schTable1Column";
/*     */     }
/* 598 */     field = relationship.get(key);
/* 599 */     String parentName = (parentView != null) ? parentView.m_name : "<null>";
/* 600 */     Report.trace("schemaloader", "following relationships " + reportPath + parentName + ", relationship: " + relationship.m_name + ", childView: " + childView.m_name + ", field: " + field, null);
/*     */ 
/* 605 */     return new String[] { field };
/*     */   }
/*     */ 
/*     */   public String[] constructParentValuesArray(SchemaViewData parentView, SchemaRelationData relationship, SchemaViewData childView, ResultSet resultSet, Map args)
/*     */     throws DataException
/*     */   {
/* 613 */     SchemaHelper helper = new SchemaHelper();
/* 614 */     String[] fields = constructParentFieldsArray(parentView, relationship, childView, args);
/*     */ 
/* 616 */     String[] values = helper.constructParentValuesArray(fields, resultSet);
/* 617 */     return values;
/*     */   }
/*     */ 
/*     */   protected void loadAllValues(SchemaViewData viewDef, ViewCacheCallback callback, SchemaCacheItem item, Map args)
/*     */     throws DataException
/*     */   {
/* 623 */     String viewType = viewDef.get("schViewType");
/* 624 */     if (viewType.equalsIgnoreCase("table"))
/*     */     {
/* 626 */       String tableName = viewDef.get("schTableName");
/* 627 */       String wsname = (String)this.m_tableWorkspaceMap.get(tableName);
/* 628 */       if (wsname != null)
/*     */       {
/* 630 */         this.m_schemaUtils.validateViewTableColumns(viewDef, WorkspaceUtils.getWorkspace(wsname));
/*     */       }
/*     */       else
/*     */       {
/* 634 */         this.m_schemaUtils.validateViewTableColumns(viewDef, this.m_workspace);
/*     */       }
/* 636 */       StringBuffer queryBuf = new StringBuffer();
/* 637 */       this.m_schemaUtils.appendLoadQuery(queryBuf, viewDef, "child", (String[][])null, (String[][])null);
/*     */ 
/* 640 */       String theQuery = queryBuf.toString();
/* 641 */       if (SystemUtils.m_verbose)
/*     */       {
/* 643 */         Report.debug("schemaloader", "loading view \"" + viewDef.m_name + "\" with query \"" + theQuery + "\".", null);
/*     */       }
/*     */ 
/* 646 */       loadValuesFromResultSet(theQuery, item, viewDef, callback, null, null, false, args);
/*     */     }
/* 648 */     else if (viewType.equalsIgnoreCase("optionlist"))
/*     */     {
/* 650 */       String optionListName = viewDef.get("schOptionList");
/* 651 */       if (optionListName == null)
/*     */       {
/* 653 */         optionListName = viewDef.m_name;
/*     */       }
/* 655 */       Vector list = SharedObjects.getOptList(optionListName);
/* 656 */       if (list == null)
/*     */       {
/* 659 */         Report.trace("schemaloader", "The optionList view " + viewDef.m_name + " didn't have an option list.", null);
/*     */ 
/* 661 */         list = new IdcVector();
/*     */       }
/*     */ 
/* 664 */       int length = list.size();
/* 665 */       DataResultSet drset = new DataResultSet(new String[] { "dOption" });
/*     */ 
/* 667 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 669 */         Vector row = new IdcVector();
/* 670 */         String element = (String)list.elementAt(i);
/* 671 */         row.addElement(element);
/* 672 */         drset.addRow(row);
/*     */       }
/* 674 */       long timestamp = System.currentTimeMillis();
/*     */ 
/* 676 */       DataBinder binder = new DataBinder();
/* 677 */       ExecutionContext context = this.m_schemaUtils.constructExecutionContext(args);
/* 678 */       PageMerger merger = new PageMerger(binder, context);
/* 679 */       callback.updateCache(item, viewDef, timestamp, null, null, null, drset, merger, context, false, false, args);
/*     */     }
/*     */     else
/*     */     {
/* 684 */       String msg = LocaleUtils.encodeMessage("apSchemaUnknownViewType", null, viewType);
/*     */ 
/* 686 */       msg = LocaleUtils.encodeMessage("apSchemaUnableToLoadView", msg, viewDef.m_name);
/*     */ 
/* 688 */       throw new DataException(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadValues(SchemaViewData viewDef, ViewCacheCallback callback, SchemaCacheItem item, Map args)
/*     */     throws DataException
/*     */   {
/* 695 */     boolean isPartial = false;
/* 696 */     String relationName = (String)args.get("relationName");
/* 697 */     ResultSet parentValues = (ResultSet)args.get("parentValues");
/* 698 */     String[] primaryKeyValue = (String[])(String[])args.get("primaryKey");
/* 699 */     SchemaRelationData relation = null;
/* 700 */     if (relationName != null)
/*     */     {
/* 702 */       relation = (SchemaRelationData)this.m_relations.getData(relationName);
/*     */     }
/*     */ 
/* 705 */     if (item.m_externalData.containsKey("ServerSchemaLoaderResultSetIsComplete"))
/*     */     {
/* 708 */       DataResultSet cachedResultSet = (DataResultSet)item.m_externalData.get("ServerSchemaLoaderResultSet");
/*     */ 
/* 710 */       DataResultSet rset = cachedResultSet.shallowClone();
/* 711 */       if (SystemUtils.m_verbose)
/*     */       {
/* 713 */         Report.debug("schemacache", "using cached results to load " + item, null);
/*     */       }
/*     */ 
/* 718 */       long[] timestampArray = (long[])(long[])item.m_externalData.get("ServerSchemaLoaderTimestamp");
/*     */       long timestamp;
/*     */       long timestamp;
/* 719 */       if (timestampArray != null)
/*     */       {
/* 721 */         timestamp = timestampArray[0];
/*     */       }
/*     */       else
/*     */       {
/* 725 */         timestamp = System.currentTimeMillis();
/*     */       }
/*     */ 
/* 728 */       DataBinder binder = new DataBinder();
/* 729 */       ExecutionContext context = this.m_schemaUtils.constructExecutionContext(args);
/* 730 */       PageMerger merger = new PageMerger(binder, context);
/* 731 */       callback.updateCache(item, viewDef, timestamp, relation, parentValues, null, rset, merger, context, false, false, args);
/*     */     }
/*     */ 
/* 735 */     List criteriaList = new ArrayList();
/*     */ 
/* 737 */     if (relationName != null)
/*     */     {
/* 743 */       SchemaHelper helper = new SchemaHelper();
/* 744 */       int flags = item.getFlags();
/* 745 */       int oppositeFlags = flags ^ 0x1;
/*     */ 
/* 747 */       String childColumnName = helper.getTableColumnWithData(viewDef, relation, flags);
/* 748 */       String parentColumnName = helper.getTableColumnWithData(viewDef, relation, oppositeFlags);
/*     */ 
/* 753 */       String childColumnValue = parentValues.getStringValueByName(parentColumnName);
/*     */ 
/* 755 */       criteriaList.add(new String[] { childColumnName, childColumnValue, "child" });
/* 756 */       item.m_externalData.put("loaderHandled:" + SchemaCacheItem.RELATION_KEY_STRING, "1");
/*     */     }
/* 758 */     else if (primaryKeyValue != null)
/*     */     {
/* 769 */       String childColumnName = viewDef.get("schInternalColumn");
/* 770 */       if (childColumnName == null)
/*     */       {
/* 772 */         String msg = LocaleUtils.encodeMessage("apSchemaNoPrimaryKey", null, viewDef.get("schTableName"));
/*     */ 
/* 774 */         throw new DataException(msg);
/*     */       }
/*     */ 
/* 777 */       criteriaList.add(new String[] { childColumnName, primaryKeyValue[0], "child" });
/* 778 */       item.m_externalData.put("loaderHandled:" + SchemaCacheItem.PRIMARY_KEY_STRING, "1");
/* 779 */       isPartial = true;
/*     */     }
/*     */     else
/*     */     {
/* 786 */       loadAllValues(viewDef, callback, item, args);
/* 787 */       return;
/*     */     }
/*     */ 
/* 790 */     this.m_schemaUtils.validateViewTableColumns(viewDef, this.m_workspace);
/*     */ 
/* 792 */     int fillLimitRowCount = -1;
/*     */ 
/* 798 */     String fillLimitString = viewDef.get("ServerSchemaLoaderMaxQueryAheadRows", null);
/* 799 */     if (relation != null)
/*     */     {
/* 801 */       fillLimitString = relation.get("ServerSchemaLoaderMaxQueryAheadRows", fillLimitString);
/*     */     }
/* 803 */     int fillLimit = NumberUtils.parseInteger(fillLimitString, 100);
/*     */ 
/* 805 */     if (item.isComplete())
/*     */     {
/* 809 */       ResultSet rset = item.getResultSet();
/* 810 */       if (rset != null)
/*     */       {
/* 812 */         fillLimitRowCount = rset.skip(fillLimit + 1);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 817 */       String[][] criteria = new String[criteriaList.size()][];
/* 818 */       criteriaList.toArray(criteria);
/* 819 */       StringBuffer query = new StringBuffer();
/* 820 */       this.m_schemaUtils.appendCountQuery(query, viewDef, "child", criteria);
/*     */ 
/* 822 */       String theQuery = query.toString();
/* 823 */       ResultSet rset = this.m_workspace.createResultSetSQL(theQuery);
/* 824 */       rset.first();
/* 825 */       String countString = rset.getStringValue(0);
/* 826 */       fillLimitRowCount = NumberUtils.parseInteger(countString, 0);
/*     */     }
/*     */ 
/* 829 */     if (fillLimitRowCount > fillLimit)
/*     */     {
/* 836 */       if ((primaryKeyValue != null) && (relationName != null))
/*     */       {
/* 839 */         String childColumnName = viewDef.get("schInternalColumn");
/* 840 */         if (childColumnName == null)
/*     */         {
/* 842 */           String msg = LocaleUtils.encodeMessage("apSchemaNoPrimaryKey", null, viewDef.get("schTableName"));
/*     */ 
/* 844 */           throw new DataException(msg);
/*     */         }
/*     */ 
/* 847 */         criteriaList.add(new String[] { childColumnName, primaryKeyValue[0], "child" });
/* 848 */         item.m_externalData.put("loaderHandled:" + SchemaCacheItem.PRIMARY_KEY_STRING, "1");
/* 849 */         isPartial = true;
/*     */       }
/*     */       else
/*     */       {
/* 853 */         String[] fields = (String[])(String[])args.get("fieldNames");
/* 854 */         String[] values = (String[])(String[])args.get("fieldValues");
/* 855 */         if (fields != null)
/*     */         {
/* 857 */           for (int i = 0; i < fields.length; ++i)
/*     */           {
/* 859 */             criteriaList.add(new String[] { fields[i], values[i], "child" });
/*     */           }
/* 861 */           isPartial = true;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 866 */     String[][] criteria = new String[criteriaList.size()][];
/* 867 */     criteriaList.toArray(criteria);
/*     */ 
/* 869 */     String[][] sortOrder = new String[0][];
/*     */ 
/* 871 */     StringBuffer query = new StringBuffer();
/* 872 */     this.m_schemaUtils.appendLoadQuery(query, viewDef, "child", criteria, sortOrder);
/*     */ 
/* 874 */     String theQuery = query.toString();
/*     */ 
/* 876 */     if (SystemUtils.m_verbose)
/*     */     {
/* 878 */       if (relation != null)
/*     */       {
/* 880 */         Report.debug("schemaloader", "loading view \"" + viewDef.m_name + "\" under relationship \"" + relation.m_name + "\" with query \"" + theQuery, null);
/*     */       }
/*     */       else
/*     */       {
/* 886 */         Report.debug("schemaloader", "loading view \"" + viewDef.m_name + "\" with query \"" + theQuery, null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 891 */     loadValuesFromResultSet(theQuery, item, viewDef, callback, relation, parentValues, isPartial, args);
/*     */   }
/*     */ 
/*     */   protected void loadValuesFromResultSet(String theQuery, SchemaCacheItem item, SchemaViewData viewDef, ViewCacheCallback callback, SchemaRelationData relation, ResultSet parentValuesRset, boolean isPartial, Map args)
/*     */     throws DataException
/*     */   {
/* 899 */     DataBinder binder = new DataBinder();
/* 900 */     ExecutionContext context = this.m_schemaUtils.constructExecutionContext(args);
/* 901 */     PageMerger merger = new PageMerger(binder, context);
/*     */     try
/*     */     {
/* 906 */       String tableName = viewDef.get("schTableName");
/* 907 */       String wsname = (String)this.m_tableWorkspaceMap.get(tableName);
/*     */       ResultSet rset;
/* 908 */       if (wsname != null)
/*     */       {
/* 910 */         rset = WorkspaceUtils.getWorkspace(wsname).createResultSetSQL(theQuery);
/*     */       }
/*     */       else
/*     */       {
/* 914 */         rset = this.m_workspace.createResultSetSQL(theQuery);
/*     */       }
/* 916 */       rset.setDateFormat(LocaleResources.m_odbcFormat);
/* 917 */       ResultSet rset = this.m_schemaUtils.doServerProcessing(viewDef, rset, args);
/* 918 */       long timestamp = System.currentTimeMillis();
/* 919 */       callback.updateCache(item, viewDef, timestamp, relation, parentValuesRset, null, rset, merger, context, false, isPartial, args);
/*     */     }
/*     */     finally
/*     */     {
/*     */       Object obj;
/* 924 */       if (args != null)
/*     */       {
/* 926 */         Object obj = args.get("context");
/* 927 */         if (obj != null)
/*     */         {
/* 929 */           this.m_workspace.releaseConnection();
/*     */         }
/*     */       }
/* 932 */       merger.releaseAllTemporary();
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean needsMoreData(SchemaCacheItem item, Map args) throws DataException
/*     */   {
/* 938 */     if (item.isComplete())
/*     */     {
/* 940 */       return false;
/*     */     }
/*     */ 
/* 943 */     if (item.getRelationshipName() == null)
/*     */     {
/* 945 */       String[] primaryKey = (String[])(String[])args.get("primaryKey");
/*     */ 
/* 947 */       if (primaryKey != null)
/*     */       {
/* 952 */         Map tmpArgs = new HashMap();
/* 953 */         tmpArgs.put("primaryKey", primaryKey);
/* 954 */         return item.getCacheEntry(args) == null;
/*     */       }
/*     */     }
/*     */ 
/* 958 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 963 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98302 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.ServerSchemaLoader
 * JD-Core Version:    0.5.4
 */