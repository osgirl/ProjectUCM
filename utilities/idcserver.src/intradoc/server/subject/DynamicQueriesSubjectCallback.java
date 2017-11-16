/*     */ package intradoc.server.subject;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DocClassUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.IdcDataSourceUtils;
/*     */ import intradoc.data.QueryUtils;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.indexer.IndexerCollectionManager;
/*     */ import intradoc.indexer.IndexerWorkObject;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.server.MailInfo;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import intradoc.server.alert.AlertUtils;
/*     */ import intradoc.shared.ActiveIndexState;
/*     */ import intradoc.shared.MetaFieldData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.schema.SchemaData;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.StringReader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class DynamicQueriesSubjectCallback extends SubjectCallbackAdapter
/*     */ {
/*     */   public static final int DOC_META = 1;
/*     */   public static final int USER = 2;
/*     */   public static final int UPDATE = 3;
/*     */   public static final int INSERT = 4;
/*     */   public static final int DELETE = 5;
/*     */   public static final int SELECT = 6;
/*  46 */   public static long m_lastRefreshTime = 0L;
/*     */ 
/*     */   public void refresh(String subject)
/*     */     throws DataException, ServiceException
/*     */   {
/*  51 */     long time = System.currentTimeMillis();
/*  52 */     refreshDynamicMetaQueries();
/*  53 */     checkSearchIndexDesign();
/*  54 */     MailInfo.initCollatedFieldList(true);
/*  55 */     m_lastRefreshTime = time;
/*     */   }
/*     */ 
/*     */   public void refreshDynamicMetaQueries() throws DataException, ServiceException
/*     */   {
/*  60 */     MetaFieldData metaData = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/*     */ 
/*  63 */     String[] queryDefCols = { "name", "queryStr", "parameters" };
/*  64 */     DataResultSet qlist = new DataResultSet(queryDefCols);
/*     */ 
/*  67 */     DataResultSet newColsMap = new DataResultSet(new String[] { "column", "alias" });
/*  68 */     for (metaData.first(); metaData.isRowPresent(); metaData.next())
/*     */     {
/*  70 */       String name = metaData.getStringValue(metaData.m_nameIndex);
/*  71 */       QueryUtils.addColumnMapRow(newColsMap, name);
/*     */     }
/*     */ 
/*  74 */     StringBuffer iSql = new StringBuffer();
/*  75 */     StringBuffer uSql = new StringBuffer();
/*  76 */     StringBuffer iParams = new StringBuffer();
/*  77 */     StringBuffer uParams = new StringBuffer();
/*  78 */     Set missingFields = new LinkedHashSet();
/*     */ 
/*  80 */     DataResultSet data = SharedObjects.getTable("UserMetaDefinition");
/*  81 */     generateSqlQuery(WorkspaceUtils.getWorkspace("user"), data, "Users", iSql, uSql, iParams, uParams, 2, missingFields);
/*     */ 
/*  85 */     if (missingFields.size() > 0)
/*     */     {
/*  87 */       IdcMessage msg = IdcMessageFactory.lc("csDynamicQueryMetaMissingFields", new Object[] { missingFields, "Users" });
/*     */ 
/*  89 */       String alertReportStr = LocaleUtils.encodeMessage(msg);
/*  90 */       AlertUtils.setAlertSimple("UsersTableMissingFields", alertReportStr, null, 2);
/*  91 */       Report.warning("system", null, msg);
/*     */     }
/*     */     else
/*     */     {
/*  95 */       AlertUtils.deleteAlertSimple("UsersTableMissingFields");
/*     */     }
/*     */ 
/*  99 */     QueryUtils.addQueryDef(qlist, "Iuser", iSql.toString(), iParams.toString());
/* 100 */     QueryUtils.addQueryDef(qlist, "Uuser", uSql.toString(), uParams.toString());
/*     */ 
/* 103 */     WorkspaceUtils.getWorkspace("user").addQueryDefs(qlist);
/*     */ 
/* 106 */     this.m_workspace.loadColumnMap(newColsMap);
/*     */ 
/* 108 */     DataBinder binder = new DataBinder();
/* 109 */     binder.putLocal("drivingField", "xExternalDataSet");
/* 110 */     binder.addResultSet("DocMetaDefiniton", metaData);
/* 111 */     PluginFilters.filter("PreMetaQueryUpdateFilter", this.m_workspace, binder, new ExecutionContextAdaptor());
/*     */     try
/*     */     {
/* 116 */       defineMetaDataQueries(binder, metaData);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 120 */       boolean failCSStart = SharedObjects.getEnvValueAsBoolean("AllowMetaQueriesInitFailCS", false);
/* 121 */       if ((m_lastRefreshTime == 0L) && (!failCSStart))
/*     */       {
/* 123 */         Report.error(null, "csUnableToInitMetaDataQueries", e);
/*     */       }
/*     */       else
/*     */       {
/* 127 */         throw e;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 132 */     String[][] sqlStrs = { { "SELECT * from DocMeta WHERE dID=0", "system" }, { "SELECT * from Users WHERE dName='0'", "user" } };
/*     */ 
/* 137 */     for (int i = 0; i < sqlStrs.length; ++i)
/*     */     {
/* 139 */       IdcSystemLoader.loadDatabaseFieldInfo(WorkspaceUtils.getWorkspace(sqlStrs[i][1]), sqlStrs[i][0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void defineMetaDataQueries(DataBinder binder, MetaFieldData metaData) throws DataException, ServiceException
/*     */   {
/* 145 */     String[] tableArr = { "DocMeta" };
/* 146 */     String[] selectArr = null;
/* 147 */     String[] relationArr = new String[0];
/* 148 */     String[] filterArr = new String[0];
/* 149 */     String[] fieldMapArr = null;
/* 150 */     IdcDataSourceUtils.registerOrRefresh(this.m_workspace, "DocMetaData", tableArr, selectArr, relationArr, filterArr, fieldMapArr);
/* 151 */     IdcDataSourceUtils.registerQueryWithDDName(this.m_workspace, "Imeta", "DocMetaData", 2);
/* 152 */     IdcDataSourceUtils.registerQueryWithDDName(this.m_workspace, "Umeta", "DocMetaData", 4);
/* 153 */     IdcDataSourceUtils.registerQueryWithDDName(this.m_workspace, "DdocMeta", "DocMetaData", 8);
/*     */ 
/* 155 */     selectArr = new String[] { "Revisions.*", "Documents.*", "?#docmetaColumns:DocMeta.* column", "RevClasses.*" };
/* 156 */     tableArr = new String[] { "Revisions", "Documents", "DocMeta", "RevClasses" };
/* 157 */     relationArr = new String[] { "", "Revisions.dID = Documents.dID", "Revisions.dID = DocMeta.dID", "Revisions.dRevClassID = RevClasses.dRevClassID" };
/*     */ 
/* 159 */     filterArr = new String[] { "Revisions.dID = ?, dStatus <> 'DELETED'", "dIsPrimary<> 0", "" };
/* 160 */     IdcDataSourceUtils.registerOrRefresh(this.m_workspace, "DocInfoData", tableArr, selectArr, relationArr, filterArr, null);
/*     */ 
/* 162 */     IdcDataSourceUtils.registerQueryWithDDName(this.m_workspace, "QdocInfo", "DocInfoData", 1);
/*     */ 
/* 165 */     List tables = new ArrayList();
/* 166 */     List relations = new ArrayList();
/* 167 */     List filters = new ArrayList();
/* 168 */     List fieldMapList = new ArrayList();
/* 169 */     Map fieldAliasMap = new HashMap();
/*     */ 
/* 171 */     for (metaData.first(); metaData.isRowPresent(); metaData.next())
/*     */     {
/* 173 */       String defaultValue = metaData.getStringValueByName("dDefaultValue");
/* 174 */       boolean isPlaceholder = StringUtils.convertToBool(metaData.getStringValue(metaData.m_isPlaceholderIndex), false);
/*     */ 
/* 176 */       String fieldName = metaData.getStringValueByName("dName");
/* 177 */       String extraProperties = metaData.getStringValueByName("dExtraDefinition");
/* 178 */       if ((extraProperties != null) && (extraProperties.trim().length() > 0))
/*     */       {
/* 180 */         DataBinder tmpBinder = new DataBinder();
/*     */         try
/*     */         {
/* 183 */           tmpBinder.receive(new BufferedReader(new StringReader(extraProperties)));
/*     */         }
/*     */         catch (IOException e)
/*     */         {
/* 187 */           throw new DataException("csUnableToParseDocMetaDefExtraProps", e);
/*     */         }
/* 189 */         String tmp = null;
/* 190 */         if (isPlaceholder)
/*     */         {
/* 193 */           String extTable = tmpBinder.getLocal("externalTable");
/* 194 */           if (extTable == null)
/*     */             continue;
/* 196 */           tables.add(extTable);
/*     */ 
/* 204 */           tmp = tmpBinder.getLocal("externalTableRelations");
/* 205 */           if (tmp != null)
/*     */           {
/* 207 */             relations.add(tmp);
/*     */           }
/*     */           else
/*     */           {
/* 211 */             Report.trace("system", "Error in placeholder field '" + fieldName + "' definition, external table '" + extTable + "'does not have relationship defined", null);
/*     */ 
/* 214 */             throw new DataException(null, "csPlaceHolderFieldNoRelationshipDefined", new Object[] { fieldName, extTable });
/*     */           }
/* 216 */           tmp = tmpBinder.getLocal("externalTableFilters");
/* 217 */           if (tmp == null)
/*     */           {
/* 219 */             tmp = "";
/*     */           }
/* 221 */           filters.add(tmp);
/*     */ 
/* 223 */           tmp = tmpBinder.getLocal("externalTableFieldName");
/* 224 */           if (tmp != null)
/*     */           {
/* 226 */             if (tmp.indexOf(46) < 0)
/*     */             {
/* 228 */               tmp = extTable + "." + tmp;
/*     */             }
/* 230 */             String tmpKey = tmp + ":alias";
/* 231 */             fieldMapList.add(tmpKey + "=" + fieldName);
/* 232 */             fieldAliasMap.put(tmpKey, fieldName);
/* 233 */             fieldName = tmp;
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 238 */           tmp = tmpBinder.getLocal("externalTableFieldName");
/* 239 */           if (tmp != null)
/*     */           {
/* 241 */             if (tmp.indexOf(46) < 0)
/*     */             {
/* 243 */               String extTable = tmpBinder.getLocal("externalTable");
/* 244 */               if (extTable != null)
/*     */               {
/* 246 */                 tmp = extTable + "." + tmp;
/*     */               }
/*     */               else
/*     */               {
/* 250 */                 Report.trace("system", "externalTableFieldName '" + tmp + "' is defined without " + "table.", null);
/*     */               }
/*     */             }
/*     */ 
/* 254 */             String tmpKey = tmp + ":alias";
/* 255 */             fieldMapList.add(tmpKey + "=" + fieldName);
/* 256 */             fieldAliasMap.put(tmpKey, fieldName);
/* 257 */             fieldMapList.add(tmp + ":default=" + defaultValue);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 262 */       fieldMapList.add(fieldName + ":default=" + defaultValue);
/*     */     }
/*     */ 
/* 265 */     Object[] filterObjects = { tables, relations, filters, fieldMapList };
/* 266 */     ExecutionContext ec = new ExecutionContextAdaptor();
/* 267 */     ec.setCachedObject("FilterObjects", filterObjects);
/* 268 */     PluginFilters.filter("PreRegisterOrRefreshMetaQuery", this.m_workspace, binder, ec);
/*     */ 
/* 270 */     String[] tmp = new String[0];
/* 271 */     tableArr = (String[])tables.toArray(tmp);
/* 272 */     relationArr = (String[])relations.toArray(tmp);
/* 273 */     filterArr = (String[])filters.toArray(tmp);
/* 274 */     fieldMapArr = (String[])fieldMapList.toArray(tmp);
/*     */ 
/* 276 */     int nTables = tables.size();
/* 277 */     if (nTables > 0)
/*     */     {
/* 280 */       int type = 14;
/* 281 */       IdcDataSourceUtils.addDependentQueryConditions("DocMetaData", binder.getLocalData());
/* 282 */       IdcDataSourceUtils.addDependentQueries("DocMetaData", null, this.m_workspace, tableArr, null, relationArr, filterArr, fieldMapArr, type);
/*     */ 
/* 286 */       type = 1;
/* 287 */       IdcDataSourceUtils.addDependentQueryConditions("DocInfoData", binder.getLocalData());
/* 288 */       for (int i = 0; i < nTables; ++i)
/*     */       {
/* 290 */         String[] selectList = getSelectList((String)tables.get(i), fieldAliasMap, this.m_workspace);
/* 291 */         IdcDataSourceUtils.addDependentQueries("DocInfoData", null, this.m_workspace, new String[] { (String)tables.get(i) }, selectList, new String[] { (String)relations.get(i) }, new String[] { (String)filters.get(i) }, fieldMapArr, type);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 297 */     if (!DocClassUtils.areDocClassesUsed())
/*     */       return;
/* 299 */     HashSet dmsset = new HashSet();
/* 300 */     Map docClassExtraConditions = new HashMap();
/* 301 */     docClassExtraConditions.put("drivingField", "dDocClass");
/* 302 */     docClassExtraConditions.put("mergeToDrivingRSField", "dID");
/*     */ 
/* 304 */     Iterator docClassIter = DocClassUtils.getNonemptyClassesIterator();
/* 305 */     IdcStringBuilder drivingFieldTableMapMod = new IdcStringBuilder();
/* 306 */     IdcStringBuilder drivingFieldTableMapSelect = new IdcStringBuilder();
/* 307 */     while (docClassIter.hasNext())
/*     */     {
/* 309 */       if (drivingFieldTableMapMod.length() > 0)
/*     */       {
/* 311 */         drivingFieldTableMapMod.append(',');
/* 312 */         drivingFieldTableMapSelect.append(',');
/*     */       }
/*     */ 
/* 315 */       String dclass = (String)docClassIter.next();
/* 316 */       drivingFieldTableMapMod.append(dclass + ":");
/* 317 */       drivingFieldTableMapSelect.append(dclass);
/*     */ 
/* 319 */       List sets = DocClassUtils.getClassDMSTables(dclass);
/* 320 */       tableArr = (String[])sets.toArray(tmp);
/* 321 */       filterArr = new String[tableArr.length];
/* 322 */       for (int i = 0; i < sets.size(); ++i)
/*     */       {
/* 324 */         dmsset.add(sets.get(i));
/* 325 */         if (i > 0)
/*     */         {
/* 327 */           drivingFieldTableMapMod.append(';');
/*     */         }
/* 329 */         drivingFieldTableMapMod.append((String)sets.get(i));
/* 330 */         filterArr[i] = (tableArr[i] + ".dID = ?");
/*     */       }
/* 332 */       IdcDataSourceUtils.addDependentQueries("DocInfoData", dclass, this.m_workspace, tableArr, null, new String[0], filterArr, fieldMapArr, 1);
/*     */     }
/*     */ 
/* 337 */     tableArr = (String[])dmsset.toArray(tmp);
/* 338 */     int type = 14;
/* 339 */     docClassExtraConditions.put("drivingFieldTableMap", drivingFieldTableMapMod.toString());
/* 340 */     IdcDataSourceUtils.addDependentQueryConditions("DocMetaData", docClassExtraConditions);
/* 341 */     IdcDataSourceUtils.addDependentQueries("DocMetaData", null, this.m_workspace, tableArr, null, new String[0], new String[0], null, type);
/*     */ 
/* 345 */     docClassExtraConditions.put("drivingFieldTableMap", drivingFieldTableMapSelect.toString());
/* 346 */     IdcDataSourceUtils.addDependentQueryConditions("DocInfoData", docClassExtraConditions);
/*     */   }
/*     */ 
/*     */   protected String[] getSelectList(String table, Map<String, String> fieldAliasMap, Workspace ws)
/*     */     throws DataException
/*     */   {
/* 352 */     String[] columns = WorkspaceUtils.getColumnList(table, ws, null);
/*     */ 
/* 354 */     IdcStringBuilder columnStrBuilder = new IdcStringBuilder();
/* 355 */     for (int i = 0; i < columns.length; ++i)
/*     */     {
/* 357 */       if (i != 0)
/*     */       {
/* 359 */         columnStrBuilder.append(',');
/*     */       }
/*     */ 
/* 362 */       String column = columns[i];
/* 363 */       columnStrBuilder.append(column);
/* 364 */       String alias = (String)fieldAliasMap.get(table + "." + column + ":alias");
/* 365 */       if (alias == null)
/*     */         continue;
/* 367 */       columnStrBuilder.append(" ");
/* 368 */       columnStrBuilder.append(alias);
/*     */     }
/*     */ 
/* 371 */     String columnListStr = columnStrBuilder.toString();
/* 372 */     return new String[] { columnListStr };
/*     */   }
/*     */ 
/*     */   public void checkSearchIndexDesign()
/*     */     throws DataException, ServiceException
/*     */   {
/* 378 */     SearchLoader.initEx(this.m_workspace);
/*     */ 
/* 381 */     ActiveIndexState.load();
/*     */ 
/* 385 */     IndexerWorkObject data = new IndexerWorkObject(null);
/* 386 */     boolean isForceReload = isForceReloadDesign();
/*     */     try
/*     */     {
/* 389 */       int flags = 2;
/* 390 */       if (isForceReload)
/*     */       {
/* 392 */         flags |= 1;
/*     */       }
/* 394 */       data.initEx("update", "", flags, new HashMap(), this.m_workspace, null);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 399 */       SearchLoader.refreshCollectionData(null);
/* 400 */       return;
/*     */     }
/*     */ 
/* 403 */     data.m_indexCollectionManager = new IndexerCollectionManager();
/* 404 */     data.m_indexCollectionManager.init(data);
/*     */ 
/* 406 */     if ((isForceReload) && (data.m_collectionDef != null))
/*     */     {
/* 408 */       SearchLoader.setCurrentIndexDesign(SearchIndexerUtils.getSearchEngineName(data), data.m_collectionDef);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 413 */       SearchLoader.refreshCollectionData(data);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 417 */       Report.error("system", null, e);
/*     */     }
/*     */ 
/* 421 */     if (!SharedObjects.getEnvValueAsBoolean("SearchQueryTrace", false))
/*     */       return;
/* 423 */     SystemUtils.reportDeprecatedUsage("SearchQueryTrace is deprecated.  Activate the searchquery tracing section and verbose tracing.");
/*     */ 
/* 426 */     SystemUtils.addAsDefaultTrace("searchquery");
/* 427 */     SystemUtils.m_verbose = true;
/*     */   }
/*     */ 
/*     */   public boolean isForceReloadDesign()
/*     */   {
/* 433 */     boolean isForceReloadDesign = false;
/* 434 */     String workDir = DirectoryLocator.getSearchDirectory() + "rebuild/";
/* 435 */     if (FileUtils.checkFile(workDir + "state.hda", true, false) == 0)
/*     */     {
/* 437 */       DataBinder binder = new DataBinder();
/*     */       try
/*     */       {
/* 440 */         ResourceUtils.serializeDataBinder(workDir, "state.hda", binder, false, false);
/* 441 */         String finishTimeStr = binder.getLocal("timeCompleted");
/* 442 */         long finishTime = NumberUtils.parseLong(finishTimeStr, -1L);
/* 443 */         if ((finishTime > m_lastRefreshTime) || (finishTime == -1L))
/*     */         {
/* 445 */           isForceReloadDesign = true;
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 455 */       isForceReloadDesign = true;
/*     */     }
/*     */ 
/* 458 */     return isForceReloadDesign;
/*     */   }
/*     */ 
/*     */   public void generateSqlQuery(Workspace ws, ResultSet data, String tableName, StringBuffer iSql, StringBuffer uSql, StringBuffer iParams, StringBuffer uParams, int qType, Set<String> missingFields)
/*     */     throws DataException, ServiceException
/*     */   {
/* 466 */     DataResultSet newColsMap = new DataResultSet(new String[] { "column", "alias" });
/*     */ 
/* 469 */     FieldInfo[] fia = ws.getColumnList(tableName);
/* 470 */     Set tableFieldsSet = new HashSet();
/* 471 */     for (FieldInfo fi : fia)
/*     */     {
/* 475 */       tableFieldsSet.add(fi.m_name.toUpperCase());
/*     */     }
/*     */ 
/* 479 */     StringBuffer fieldNames = new StringBuffer();
/* 480 */     StringBuffer qMarks = new StringBuffer();
/* 481 */     StringBuffer updateClauses = new StringBuffer();
/* 482 */     StringBuffer params = new StringBuffer();
/*     */ 
/* 485 */     String nameCol = null;
/* 486 */     String typeCol = null;
/* 487 */     String keyCol = null;
/*     */ 
/* 489 */     SchemaHelper schemaHelper = new SchemaHelper();
/* 490 */     switch (qType)
/*     */     {
/*     */     case 1:
/* 493 */       nameCol = "dName";
/* 494 */       typeCol = "dType";
/* 495 */       keyCol = "dID";
/*     */ 
/* 497 */       iParams.append("dID int\n");
/* 498 */       iSql.append("insert into " + tableName + " (" + keyCol);
/* 499 */       uParams.append("\ndID int");
/* 500 */       uSql.append("UPDATE " + tableName + " SET ");
/* 501 */       break;
/*     */     case 2:
/* 503 */       nameCol = "umdName";
/* 504 */       typeCol = "umdType";
/* 505 */       keyCol = "dName";
/*     */ 
/* 508 */       iParams.append("dName varchar\n");
/* 509 */       iParams.append("dPassword varchar\n");
/* 510 */       iParams.append("dPasswordEncoding varchar\n");
/* 511 */       iParams.append("dUserAuthType varchar\n");
/* 512 */       iParams.append("dUserOrgPath varchar\n");
/* 513 */       iParams.append("dUserSourceOrgPath varchar\n");
/* 514 */       iParams.append("dUserSourceFlags int\n");
/* 515 */       iParams.append("dUserArriveDate date\n");
/* 516 */       iParams.append("dUserChangeDate date\n");
/*     */ 
/* 518 */       iSql.append("insert into " + tableName + " (" + keyCol);
/* 519 */       iSql.append(", dPassword, dPasswordEncoding, dUserAuthType, dUserOrgPath, dUserSourceOrgPath");
/* 520 */       iSql.append(", dUserSourceFlags, dUserArriveDate, dUserChangeDate");
/*     */ 
/* 522 */       qMarks.append("?, ?, ?, ?, ?, ?, ?, ?, ");
/*     */ 
/* 524 */       uParams.append("dPassword varchar\n");
/* 525 */       uParams.append("dPasswordEncoding varchar\n");
/* 526 */       uParams.append("dUserAuthType varchar\n");
/* 527 */       uParams.append("dUserOrgPath varchar\n");
/* 528 */       uParams.append("dUserSourceOrgPath varchar\n");
/* 529 */       uParams.append("dUserSourceFlags int\n");
/* 530 */       uParams.append("dUserArriveDate date\n");
/* 531 */       uParams.append("dUserChangeDate date\n");
/* 532 */       uSql.append("UPDATE " + tableName + " SET ");
/* 533 */       uSql.append("dPassword=?, dPasswordEncoding=?, dUserAuthType=?, dUserOrgPath=?, ");
/* 534 */       uSql.append("dUserSourceOrgPath=?, dUserSourceFlags=?, dUserArriveDate=?, dUserChangeDate=?, ");
/*     */     }
/*     */ 
/* 538 */     int nameIndex = -1;
/* 539 */     int typeIndex = -1;
/* 540 */     boolean pastFirstField = false;
/* 541 */     String commaSep = ", ";
/*     */ 
/* 543 */     for (data.first(); data.isRowPresent(); data.next())
/*     */     {
/*     */       try
/*     */       {
/* 547 */         nameIndex = ResultSetUtils.getIndexMustExist(data, nameCol);
/* 548 */         typeIndex = ResultSetUtils.getIndexMustExist(data, typeCol);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 552 */         if (SystemUtils.m_verbose)
/*     */         {
/* 554 */           Report.debug("searchquery", null, e);
/*     */         }
/*     */       }
/*     */ 
/* 558 */       String name = data.getStringValue(nameIndex);
/* 559 */       String type = data.getStringValue(typeIndex);
/* 560 */       SchemaData field = schemaHelper.getSchemaData("SchemaFieldConfig", name);
/*     */ 
/* 563 */       if (field == null)
/*     */       {
/* 565 */         Report.trace("schemainit", "unable to find field " + name, null);
/*     */       }
/*     */       else {
/* 568 */         if (field.getBoolean("schIsPlaceholderField", false)) continue; if (field.getBoolean("dIsPlaceholderField", false)) {
/*     */           continue;
/*     */         }
/*     */       }
/*     */ 
/* 573 */       if (!tableFieldsSet.contains(name.toUpperCase()))
/*     */       {
/* 576 */         missingFields.add(name);
/*     */       }
/*     */       else {
/* 579 */         if (newColsMap != null)
/*     */         {
/* 581 */           QueryUtils.addColumnMapRow(newColsMap, name);
/*     */         }
/*     */ 
/* 585 */         if (pastFirstField)
/*     */         {
/* 587 */           fieldNames.append(commaSep);
/* 588 */           qMarks.append(commaSep);
/* 589 */           updateClauses.append(commaSep);
/* 590 */           params.append("\n");
/*     */         }
/*     */ 
/* 594 */         fieldNames.append(name);
/* 595 */         qMarks.append('?');
/* 596 */         updateClauses.append(name);
/* 597 */         updateClauses.append(" = ?");
/* 598 */         params.append(name);
/* 599 */         params.append(" ");
/* 600 */         params.append(type);
/*     */ 
/* 602 */         pastFirstField = true;
/*     */       }
/*     */     }
/*     */ 
/* 606 */     if (fieldNames.length() > 0)
/*     */     {
/* 608 */       iSql.append(", " + fieldNames);
/*     */     }
/* 610 */     iSql.append(") values(?");
/* 611 */     if (qMarks.length() > 0)
/*     */     {
/* 613 */       iSql.append(", " + qMarks);
/*     */     }
/* 615 */     iSql.append(")");
/* 616 */     iParams.append(params);
/*     */ 
/* 618 */     if (updateClauses.length() > 0)
/*     */     {
/* 620 */       uSql.append(updateClauses);
/*     */     }
/* 622 */     if ((qType == 1) && (updateClauses.length() <= 0))
/*     */     {
/* 624 */       uSql.append(keyCol + "=?");
/* 625 */       params.append("dID int");
/*     */     }
/* 627 */     uSql.append(" WHERE " + keyCol + " = ?");
/*     */ 
/* 629 */     uParams.append(params);
/* 630 */     if (qType == 2)
/*     */     {
/* 632 */       uParams.append("\ndName varchar");
/*     */     }
/*     */ 
/* 636 */     ws.loadColumnMap(newColsMap);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 641 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98095 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.DynamicQueriesSubjectCallback
 * JD-Core Version:    0.5.4
 */