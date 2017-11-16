/*      */ package intradoc.server.schema;
/*      */ 
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.QueryUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.SimpleParameters;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.server.DataLoader;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.utils.DeletesTableUtils;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.ResultSetTreeSort;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.schema.SchemaCacheItem;
/*      */ import intradoc.shared.schema.SchemaData;
/*      */ import intradoc.shared.schema.SchemaFieldConfig;
/*      */ import intradoc.shared.schema.SchemaFieldData;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import intradoc.shared.schema.SchemaLoader;
/*      */ import intradoc.shared.schema.SchemaRelationConfig;
/*      */ import intradoc.shared.schema.SchemaRelationData;
/*      */ import intradoc.shared.schema.SchemaSecurityFilter;
/*      */ import intradoc.shared.schema.SchemaTableConfig;
/*      */ import intradoc.shared.schema.SchemaTableData;
/*      */ import intradoc.shared.schema.SchemaTargetConfig;
/*      */ import intradoc.shared.schema.SchemaViewConfig;
/*      */ import intradoc.shared.schema.SchemaViewData;
/*      */ import intradoc.util.IdcAppendableBase;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.io.Writer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SchemaUtils
/*      */ {
/*   35 */   public static int F_USE_DEFAULTS = 0;
/*      */ 
/*   37 */   public static int F_NO_QUERY = 1;
/*   38 */   public static int F_MULTIROW_TIMESTAMP_QUERY = 2;
/*   39 */   public static int F_ALLROWS_TIMESTAMP_QUERY = 4;
/*      */ 
/*   41 */   public static int F_APPLY_CRITERIA = 8;
/*      */   public static final int F_IS_TRUE_BOOLEAN = 1;
/*      */   public static final int F_IS_FALSE_BOOLEAN = 2;
/*      */   public static final int F_IGNORE_CASE = 4;
/*      */   public static final int UPDATE = 1;
/*      */   public static final int ADD = 2;
/*      */   public static final int ORDER_ONLY = 4;
/*      */   public static final int DELETE = 8;
/*      */   protected Vector m_specialFields;
/*      */   protected Vector m_tsFields;
/*      */   protected Vector m_criteriaFields;
/*      */   protected Vector m_viewColumnFields;
/*      */   protected boolean m_hasSourceID;
/*      */   protected FieldInfo[] m_pkColumnInfos;
/*      */   protected SchemaViewConfig m_views;
/*      */   protected SchemaTableConfig m_tables;
/*      */   protected SchemaRelationConfig m_relations;
/*      */   protected SchemaFieldConfig m_fields;
/*      */   protected SchemaTargetConfig m_targets;
/*      */   protected ExecutionContext m_dataLoaderExecutionContext;
/*      */ 
/*      */   public SchemaUtils()
/*      */   {
/*   52 */     this.m_specialFields = null;
/*   53 */     this.m_tsFields = null;
/*   54 */     this.m_criteriaFields = null;
/*   55 */     this.m_viewColumnFields = null;
/*      */ 
/*   57 */     this.m_hasSourceID = false;
/*   58 */     this.m_pkColumnInfos = null;
/*      */   }
/*      */ 
/*      */   public void init()
/*      */     throws ServiceException
/*      */   {
/*   69 */     DataResultSet schemaTypes = SharedObjects.getTable("SchemaTypes");
/*   70 */     Map initData = new HashMap();
/*   71 */     for (SimpleParameters params : schemaTypes.getSimpleParametersIterable())
/*      */     {
/*   73 */       String name = params.get("SourceName");
/*   74 */       ResultSet set = SharedObjects.getTable(name);
/*   75 */       initData.put(name, set);
/*      */     }
/*      */ 
/*   78 */     init(initData);
/*      */   }
/*      */ 
/*      */   public void init(Map initData)
/*      */     throws ServiceException
/*      */   {
/*   84 */     this.m_views = ((SchemaViewConfig)getInitResultSet(initData, "SchemaViewConfig"));
/*      */ 
/*   86 */     this.m_tables = ((SchemaTableConfig)getInitResultSet(initData, "SchemaTableConfig"));
/*      */ 
/*   88 */     this.m_relations = ((SchemaRelationConfig)getInitResultSet(initData, "SchemaRelationConfig"));
/*      */ 
/*   90 */     this.m_fields = ((SchemaFieldConfig)getInitResultSet(initData, "SchemaFieldConfig"));
/*      */ 
/*   92 */     this.m_targets = ((SchemaTargetConfig)getInitResultSet(initData, "SchemaTargetConfig"));
/*      */ 
/*   95 */     this.m_dataLoaderExecutionContext = ((ExecutionContext)initData.get("ExecutionContext"));
/*      */ 
/*   97 */     if (this.m_dataLoaderExecutionContext != null)
/*      */       return;
/*   99 */     this.m_dataLoaderExecutionContext = new ExecutionContextAdaptor();
/*      */   }
/*      */ 
/*      */   public ResultSet getInitResultSet(Map initData, String name)
/*      */     throws ServiceException
/*      */   {
/*  106 */     ResultSet rset = (ResultSet)initData.get(name);
/*  107 */     if (rset == null)
/*      */     {
/*  112 */       String msg = LocaleUtils.encodeMessage("csResultSetMissing", null, name);
/*      */ 
/*  114 */       throw new ServiceException(-26, msg);
/*      */     }
/*  116 */     return rset;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public boolean appendWhereClause(StringBuffer sql, SchemaViewData data, boolean alreadyInWhereClause, String columnName, String columnValue, String criteriaTableName)
/*      */     throws DataException
/*      */   {
/*  132 */     SystemUtils.reportDeprecatedUsage("SchemaUtils.appendWhereClause() called.  Use SchemaUtils.appendSimpleWhereClause() and SchemaUtils.appendBaseWhereClause() for this functionality.");
/*      */ 
/*  137 */     alreadyInWhereClause = appendSimpleWhereClause(sql, data, alreadyInWhereClause, new String[][] { { columnName, columnValue, criteriaTableName } }, null);
/*      */ 
/*  140 */     alreadyInWhereClause = appendBaseWhereClause(sql, data, alreadyInWhereClause, criteriaTableName, null);
/*      */ 
/*  142 */     return alreadyInWhereClause;
/*      */   }
/*      */ 
/*      */   public boolean appendSimpleWhereClause(StringBuffer sql, SchemaViewData data, boolean alreadyInWhereClause, String[][] criteria, ResultSet infos)
/*      */     throws DataException
/*      */   {
/*  149 */     if (criteria == null)
/*      */     {
/*  151 */       return alreadyInWhereClause;
/*      */     }
/*      */ 
/*  154 */     for (int i = 0; i < criteria.length; ++i)
/*      */     {
/*  156 */       String columnName = criteria[i][0];
/*  157 */       String columnValue = criteria[i][1];
/*  158 */       String criteriaTableName = criteria[i][2];
/*  159 */       if ((columnName == null) || (columnName.length() <= 0) || (columnValue == null)) {
/*      */         continue;
/*      */       }
/*  162 */       String subclause = buildSubclause(data, columnName, columnValue, criteriaTableName, null);
/*      */ 
/*  164 */       alreadyInWhereClause = QueryUtils.appendSubclause(sql, alreadyInWhereClause, "WHERE", " AND ", subclause);
/*      */     }
/*      */ 
/*  168 */     return alreadyInWhereClause;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public boolean appendWhereClause(StringBuffer sql, SchemaViewData data, boolean alreadyInWhereClause, String criteriaTableName, ResultSet infoSet)
/*      */     throws DataException
/*      */   {
/*  178 */     SystemUtils.reportDeprecatedUsage("SchemaUtils.appendWhereClause() called.  Use SchemaUtils.appendBaseWhereClause() instead.");
/*      */ 
/*  181 */     return appendBaseWhereClause(sql, data, alreadyInWhereClause, criteriaTableName, infoSet);
/*      */   }
/*      */ 
/*      */   public boolean appendBaseWhereClause(StringBuffer sql, SchemaViewData data, boolean alreadyInWhereClause, String criteriaTableName, ResultSet infoSet)
/*      */     throws DataException
/*      */   {
/*  189 */     for (int criteriaIndex = 0; ; ++criteriaIndex)
/*      */     {
/*  191 */       String field = data.get("schCriteriaField" + criteriaIndex);
/*  192 */       String value = data.get("schCriteriaValue" + criteriaIndex);
/*  193 */       if ((field == null) || (field.length() == 0)) break; if (value == null)
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/*  199 */       String subclause = buildSubclause(data, field, value, criteriaTableName, infoSet);
/*  200 */       alreadyInWhereClause = QueryUtils.appendSubclause(sql, alreadyInWhereClause, "WHERE", " AND ", subclause);
/*      */     }
/*      */ 
/*  204 */     return alreadyInWhereClause;
/*      */   }
/*      */ 
/*      */   protected String buildSubclause(SchemaViewData data, String field, String value, String criteriaTableName, ResultSet infoSet)
/*      */     throws DataException
/*      */   {
/*  212 */     String errMsg = LocaleUtils.encodeMessage("csSchViewFieldMissing", null, field, data.get("schViewName"), data.get("schTableName"));
/*      */     FieldInfo info;
/*  214 */     if (infoSet != null)
/*      */     {
/*  216 */       FieldInfo info = new FieldInfo();
/*      */ 
/*  218 */       boolean exists = infoSet.getFieldInfo(field, info);
/*  219 */       if (!exists)
/*      */       {
/*  221 */         throw new DataException(errMsg);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  226 */       String tableName = data.get("schTableName");
/*  227 */       SchemaTableData table = (SchemaTableData)this.m_tables.getData(tableName);
/*      */ 
/*  229 */       if (table == null)
/*      */       {
/*  231 */         String msg = LocaleUtils.encodeMessage("apSchMissingTableDefinition", null, tableName);
/*      */ 
/*  233 */         Report.warning(null, null, "csSchClauseFieldTypeMismatchPossible", new Object[] { msg, data.m_name, tableName });
/*      */ 
/*  235 */         FieldInfo info = new FieldInfo();
/*  236 */         info.m_name = field;
/*      */       }
/*      */       else
/*      */       {
/*  240 */         info = table.getFieldInfo(field);
/*  241 */         if (info == null)
/*      */         {
/*  243 */           throw new DataException(errMsg);
/*      */         }
/*      */       }
/*      */     }
/*  247 */     String subclause = QueryUtils.createSubclause(info, value, "=", criteriaTableName);
/*  248 */     return subclause;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public boolean appendOrderClause(StringBuffer buf, SchemaViewData data, boolean inOrderClause, String prependOrderColumn, String prependOrderDirection, String orderTableName)
/*      */   {
/*  260 */     SystemUtils.reportDeprecatedUsage("SchemaUtils.appendOrderClause() called.  Use SchemaUtils.appendBaseOrderClause() and SchemaUtils.appendSimpleOrderClause() instead.");
/*      */ 
/*  264 */     inOrderClause = appendSimpleOrderClause(buf, data, inOrderClause, new String[][] { { prependOrderColumn, prependOrderDirection, orderTableName } });
/*      */ 
/*  267 */     inOrderClause = appendBaseOrderClause(buf, data, inOrderClause, orderTableName);
/*      */ 
/*  269 */     return inOrderClause;
/*      */   }
/*      */ 
/*      */   public boolean appendSimpleOrderClause(StringBuffer buf, SchemaViewData data, boolean inOrderClause, String[][] prependOrderRules)
/*      */   {
/*  275 */     if (prependOrderRules == null)
/*      */     {
/*  277 */       return inOrderClause;
/*      */     }
/*      */ 
/*  280 */     for (int i = 0; i < prependOrderRules.length; ++i)
/*      */     {
/*  282 */       String column = prependOrderRules[i][0];
/*  283 */       String direction = prependOrderRules[i][1];
/*  284 */       String table = null;
/*  285 */       if (prependOrderRules[i].length > 2)
/*      */       {
/*  287 */         table = prependOrderRules[i][2];
/*      */       }
/*  289 */       if (table != null)
/*      */       {
/*  291 */         column = table + "." + column;
/*      */       }
/*  293 */       inOrderClause = QueryUtils.appendSubclause(buf, inOrderClause, "ORDER BY", ", ", column);
/*      */ 
/*  295 */       QueryUtils.appendAscendingOrDescending(buf, column, direction);
/*      */     }
/*  297 */     return inOrderClause;
/*      */   }
/*      */ 
/*      */   public boolean appendBaseOrderClause(StringBuffer buf, SchemaViewData data, boolean inOrderClause, String orderTableName)
/*      */   {
/*  303 */     boolean isSorted = data.getBoolean("schIsDatabaseSorted", false);
/*  304 */     if (isSorted)
/*      */     {
/*  307 */       String sortField = data.get("schSortField");
/*  308 */       String sortOrder = data.get("schSortOrder");
/*  309 */       int i = 0;
/*  310 */       for (; ; ++i)
/*      */       {
/*  312 */         if ((sortOrder == null) || (sortOrder.length() == 0))
/*      */         {
/*  314 */           sortOrder = "ascending";
/*      */         }
/*  316 */         if (sortField != null)
/*      */         {
/*  318 */           if (orderTableName != null)
/*      */           {
/*  320 */             sortField = orderTableName + "." + sortField;
/*      */           }
/*  322 */           inOrderClause = QueryUtils.appendSubclause(buf, inOrderClause, "ORDER BY", ", ", sortField);
/*      */ 
/*  324 */           QueryUtils.appendAscendingOrDescending(buf, sortField, sortOrder);
/*      */         }
/*  326 */         sortField = data.get("schSortField" + i);
/*  327 */         sortOrder = data.get("schSortOrder" + i);
/*  328 */         if (sortField == null) break; if (sortField.length() == 0) {
/*      */           break;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  334 */     return inOrderClause;
/*      */   }
/*      */ 
/*      */   public void determineAndPopulateTimeStampFields(SchemaViewData viewData, SchemaTableData tableData, ResultSet rset, Hashtable fieldMap, DataBinder binder)
/*      */     throws DataException
/*      */   {
/*  345 */     this.m_tsFields = new IdcVector();
/*  346 */     String[][] tsColumns = { { "schTableRowCreateTimestamp" }, { "schTableRowModifyTimestamp" } };
/*      */ 
/*  352 */     Date dte = new Date();
/*  353 */     int len = tsColumns.length;
/*  354 */     for (int i = 0; i < len; ++i)
/*      */     {
/*  356 */       String clmn = tableData.get(tsColumns[i][0]);
/*  357 */       if ((clmn == null) || (clmn.length() <= 0) || (clmn.equals("<none>")))
/*      */         continue;
/*  359 */       FieldInfo info = new FieldInfo();
/*  360 */       boolean isPresent = rset.getFieldInfo(clmn, info);
/*  361 */       if (isPresent)
/*      */       {
/*  364 */         if (info.m_type != 5)
/*      */         {
/*  366 */           String errMsg = LocaleUtils.encodeMessage("csSchTimeStampFieldWrongType", null, info.m_name, viewData.get("schViewName"), viewData.get("schTableName"));
/*      */ 
/*  368 */           throw new DataException(errMsg);
/*      */         }
/*  370 */         this.m_tsFields.addElement(info);
/*  371 */         fieldMap.put(clmn, info);
/*  372 */         if (binder == null)
/*      */           continue;
/*  374 */         binder.putLocal(clmn, LocaleUtils.formatODBC(dte));
/*      */       }
/*      */       else
/*      */       {
/*  379 */         String errMsg = LocaleUtils.encodeMessage("csSchTimestampFieldMissing", null, clmn, viewData.get("schViewName"), viewData.get("schTableName"));
/*      */ 
/*  381 */         throw new DataException(errMsg);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void determineAndPopulateCriteriaFields(SchemaViewData viewData, ResultSet rset, Hashtable fieldMap, DataBinder binder)
/*      */     throws DataException
/*      */   {
/*  390 */     this.m_criteriaFields = new IdcVector();
/*  391 */     for (int criteriaIndex = 0; ; ++criteriaIndex)
/*      */     {
/*  393 */       String field = viewData.get("schCriteriaField" + criteriaIndex);
/*  394 */       String value = viewData.get("schCriteriaValue" + criteriaIndex);
/*  395 */       if ((field == null) || (field.length() == 0) || (value == null)) return; if (value.length() == 0)
/*      */       {
/*      */         return;
/*      */       }
/*      */ 
/*  400 */       FieldInfo info = new FieldInfo();
/*  401 */       boolean isPresent = rset.getFieldInfo(field, info);
/*  402 */       if (isPresent)
/*      */       {
/*  404 */         this.m_criteriaFields.addElement(info);
/*  405 */         fieldMap.put(field, info);
/*  406 */         if (binder == null)
/*      */           continue;
/*  408 */         binder.putLocal(field, value);
/*      */       }
/*      */       else
/*      */       {
/*  413 */         String errMsg = LocaleUtils.encodeMessage("csSchCriteriaFieldMissing", null, field, viewData.get("schViewName"), viewData.get("schTableName"));
/*      */ 
/*  415 */         throw new DataException(errMsg);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void determineAndPopulateSpecialFields(SchemaViewData viewData, ResultSet rset, Hashtable fieldMap, DataBinder binder)
/*      */     throws DataException
/*      */   {
/*  423 */     this.m_specialFields = new IdcVector();
/*      */ 
/*  426 */     FieldInfo info = new FieldInfo();
/*  427 */     this.m_hasSourceID = rset.getFieldInfo("schSourceID", info);
/*  428 */     if (!this.m_hasSourceID)
/*      */       return;
/*  430 */     this.m_specialFields.addElement(info);
/*  431 */     fieldMap.put("schSourceID", info);
/*  432 */     if (binder == null)
/*      */       return;
/*  434 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  435 */     binder.putLocal("schSourceID", idcName);
/*      */   }
/*      */ 
/*      */   public Vector buildViewFields(SchemaViewData viewData, ResultSet drset, Hashtable fieldMap)
/*      */     throws DataException, ServiceException
/*      */   {
/*  443 */     String viewColumnStr = viewData.get("schViewColumns");
/*  444 */     Vector viewColumns = StringUtils.parseArray(viewColumnStr, ',', '^');
/*      */ 
/*  447 */     this.m_viewColumnFields = new IdcVector();
/*  448 */     int size = viewColumns.size();
/*  449 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  451 */       String clmn = (String)viewColumns.elementAt(i);
/*  452 */       FieldInfo info = new FieldInfo();
/*  453 */       boolean isPresent = drset.getFieldInfo(clmn, info);
/*  454 */       if (isPresent)
/*      */       {
/*  456 */         FieldInfo fi = (FieldInfo)fieldMap.get(clmn);
/*  457 */         if (fi == null)
/*      */         {
/*  459 */           this.m_viewColumnFields.addElement(info);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  466 */         String viewName = viewData.get("schViewName");
/*  467 */         String tableName = viewData.get("schTableName");
/*  468 */         String msg = LocaleUtils.encodeMessage("csSchViewReconfigureForTable", null, clmn, viewName, tableName);
/*      */ 
/*  470 */         throw new ServiceException(msg);
/*      */       }
/*      */     }
/*  473 */     return this.m_viewColumnFields;
/*      */   }
/*      */ 
/*      */   public String[] createSql(SchemaViewData viewData, DataResultSet drset, String tableName, DataBinder workBinder)
/*      */     throws DataException
/*      */   {
/*  490 */     StringBuffer selectStub = new StringBuffer("SELECT ");
/*  491 */     StringBuffer whereClause = new StringBuffer();
/*  492 */     int size = this.m_criteriaFields.size();
/*  493 */     boolean inClause = false;
/*  494 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  496 */       FieldInfo info = (FieldInfo)this.m_criteriaFields.elementAt(i);
/*  497 */       String val = workBinder.get(info.m_name);
/*  498 */       inClause = QueryUtils.createAndAppendSubclause(whereClause, info, val, "=", " WHERE ", " AND ", inClause);
/*      */     }
/*      */ 
/*  503 */     String internalClmn = viewData.get("schInternalColumn");
/*  504 */     FieldInfo fi = new FieldInfo();
/*  505 */     boolean isPresent = drset.getFieldInfo(internalClmn, fi);
/*  506 */     if (!isPresent)
/*      */     {
/*  509 */       String msg = LocaleUtils.encodeMessage("csSchViewInternalColumnMissing", null, internalClmn, viewData.get("schViewName"), tableName);
/*      */ 
/*  511 */       throw new DataException(msg);
/*      */     }
/*  513 */     selectStub.append(fi.m_name);
/*  514 */     selectStub.append(" FROM ");
/*  515 */     selectStub.append(tableName);
/*      */ 
/*  518 */     FieldInfo[] clmnFields = null;
/*  519 */     int len = this.m_pkColumnInfos.length;
/*  520 */     if (len == 0)
/*      */     {
/*  522 */       len = 1;
/*  523 */       clmnFields = new FieldInfo[1];
/*  524 */       clmnFields[0] = fi;
/*      */     }
/*      */     else
/*      */     {
/*  528 */       clmnFields = this.m_pkColumnInfos;
/*      */     }
/*  530 */     for (int i = 0; i < len; ++i)
/*      */     {
/*  532 */       FieldInfo info = clmnFields[i];
/*  533 */       String val = workBinder.get(info.m_name);
/*  534 */       inClause = QueryUtils.createAndAppendSubclause(whereClause, info, val, "=", " WHERE ", " AND ", inClause);
/*      */     }
/*      */ 
/*  538 */     StringBuffer[] sqlStubs = new StringBuffer[3];
/*      */ 
/*  544 */     sqlStubs[0] = new StringBuffer("UPDATE " + tableName + " SET ");
/*  545 */     sqlStubs[1] = new StringBuffer("INSERT INTO " + tableName + " (");
/*  546 */     sqlStubs[2] = new StringBuffer();
/*      */ 
/*  548 */     inClause = false;
/*  549 */     inClause = QueryUtils.createInsertAndUpdateClauses(sqlStubs, this.m_viewColumnFields, workBinder, inClause);
/*  550 */     inClause = QueryUtils.createInsertAndUpdateClauses(sqlStubs, this.m_specialFields, workBinder, inClause);
/*  551 */     inClause = QueryUtils.createInsertAndUpdateClauses(sqlStubs, this.m_criteriaFields, workBinder, inClause);
/*      */ 
/*  554 */     size = this.m_tsFields.size();
/*  555 */     if (size == 2)
/*      */     {
/*  558 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  560 */         FieldInfo info = (FieldInfo)this.m_tsFields.elementAt(i);
/*  561 */         String val = workBinder.getActiveAllowMissing(info.m_name);
/*  562 */         QueryUtils.createInsertClauses(sqlStubs[1], sqlStubs[2], info, val, inClause);
/*      */ 
/*  564 */         if ((i == 0) && 
/*  567 */           (val != null) && (val.length() > 0))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  572 */         QueryUtils.createAndAppendSubclause(sqlStubs[0], info, val, "=", "", ", ", inClause);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  577 */     StringBuffer selectClause = new StringBuffer();
/*  578 */     selectClause.append(selectStub);
/*  579 */     selectClause.append(whereClause);
/*      */ 
/*  581 */     StringBuffer updateClause = new StringBuffer();
/*  582 */     updateClause.append(sqlStubs[0]);
/*  583 */     updateClause.append(whereClause);
/*      */ 
/*  585 */     StringBuffer insertClause = new StringBuffer();
/*  586 */     insertClause.append(sqlStubs[1]);
/*  587 */     insertClause.append(") values (");
/*  588 */     insertClause.append(sqlStubs[2]);
/*  589 */     insertClause.append(")");
/*      */ 
/*  591 */     StringBuffer deleteClause = new StringBuffer("DELETE FROM " + tableName);
/*  592 */     deleteClause.append(whereClause);
/*      */ 
/*  594 */     Report.trace("schemasql", selectClause.toString(), null);
/*  595 */     Report.trace("schemasql", updateClause.toString(), null);
/*  596 */     Report.trace("schemasql", insertClause.toString(), null);
/*  597 */     Report.trace("schemasql", deleteClause.toString(), null);
/*      */ 
/*  599 */     String[] sqlClauses = new String[4];
/*  600 */     sqlClauses[0] = selectClause.toString();
/*  601 */     sqlClauses[1] = updateClause.toString();
/*  602 */     sqlClauses[2] = insertClause.toString();
/*  603 */     sqlClauses[3] = deleteClause.toString();
/*      */ 
/*  605 */     return sqlClauses;
/*      */   }
/*      */ 
/*      */   public void validateViewTableColumns(SchemaViewData view, Workspace ws)
/*      */     throws DataException
/*      */   {
/*  611 */     String tableName = view.get("schTableName");
/*  612 */     FieldInfo[] origInfos = ws.getColumnList(tableName);
/*      */ 
/*  614 */     String viewColumnStr = view.get("schViewColumns");
/*  615 */     Vector viewColumns = StringUtils.parseArray(viewColumnStr, ',', '^');
/*      */ 
/*  618 */     this.m_viewColumnFields = new IdcVector();
/*  619 */     int size = viewColumns.size();
/*  620 */     int fiLen = origInfos.length;
/*  621 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  623 */       String clmn = (String)viewColumns.elementAt(i);
/*  624 */       boolean isPresent = false;
/*  625 */       for (int j = 0; j < fiLen; ++j)
/*      */       {
/*  627 */         String field = origInfos[j].m_name;
/*  628 */         if (!field.equals(clmn))
/*      */           continue;
/*  630 */         isPresent = true;
/*  631 */         break;
/*      */       }
/*      */ 
/*  634 */       if (isPresent) {
/*      */         continue;
/*      */       }
/*      */ 
/*  638 */       String msg = LocaleUtils.encodeMessage("csSchViewReconfigureForTable", null, clmn, view.get("schViewName"), tableName);
/*      */ 
/*  640 */       throw new DataException(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public int appendCountQuery(StringBuffer queryBuf, SchemaViewData view, String tableAlias, String[][] criteria)
/*      */     throws DataException
/*      */   {
/*  649 */     int flags = 0;
/*  650 */     boolean inWhereClause = false;
/*      */ 
/*  652 */     String tableName = view.get("schTableName");
/*  653 */     if (tableAlias == null)
/*      */     {
/*  655 */       tableAlias = tableName;
/*      */     }
/*      */ 
/*  658 */     queryBuf.append("SELECT COUNT(*) ");
/*      */ 
/*  660 */     queryBuf.append(" FROM ");
/*  661 */     queryBuf.append(tableName);
/*  662 */     queryBuf.append(" ");
/*  663 */     queryBuf.append(tableAlias);
/*  664 */     inWhereClause = appendBaseWhereClause(queryBuf, view, inWhereClause, tableAlias, null);
/*      */ 
/*  666 */     inWhereClause = appendSimpleWhereClause(queryBuf, view, inWhereClause, criteria, null);
/*      */ 
/*  668 */     if (inWhereClause)
/*      */     {
/*  670 */       String postWhereText = view.get("schPostWhereText");
/*  671 */       if (postWhereText != null)
/*      */       {
/*  673 */         queryBuf.append(postWhereText);
/*      */       }
/*      */     }
/*  676 */     String postQueryText = view.get("schPostQueryText");
/*  677 */     if (postQueryText != null)
/*      */     {
/*  679 */       queryBuf.append(postQueryText);
/*      */     }
/*  681 */     return flags;
/*      */   }
/*      */ 
/*      */   public int appendLoadQuery(StringBuffer queryBuf, SchemaViewData view, String tableAlias, String[][] criteria, String[][] sortOrder)
/*      */     throws DataException
/*      */   {
/*  688 */     int flags = 0;
/*  689 */     boolean inWhereClause = false;
/*  690 */     boolean inOrderClause = false;
/*  691 */     String useDistinctQueryString = SharedObjects.getEnvironmentValue("schUseDistinctValues");
/*      */ 
/*  693 */     useDistinctQueryString = view.get("schUseDistinctValues", useDistinctQueryString);
/*      */ 
/*  695 */     boolean useDistinctQuery = StringUtils.convertToBool(useDistinctQueryString, true);
/*      */ 
/*  698 */     String tableName = view.get("schTableName");
/*  699 */     if (tableAlias == null)
/*      */     {
/*  701 */       tableAlias = tableName;
/*      */     }
/*      */ 
/*  704 */     queryBuf.append("SELECT ");
/*  705 */     if (useDistinctQuery)
/*      */     {
/*  707 */       queryBuf.append("DISTINCT ");
/*      */     }
/*  709 */     String postSelectText = view.get("schPostSelectText");
/*  710 */     if (postSelectText != null)
/*      */     {
/*  712 */       queryBuf.append(postSelectText);
/*      */     }
/*  714 */     appendViewColumns(queryBuf, view, tableAlias, 0);
/*      */ 
/*  716 */     queryBuf.append(" FROM ");
/*  717 */     queryBuf.append(tableName);
/*  718 */     queryBuf.append(" ");
/*  719 */     queryBuf.append(tableAlias);
/*  720 */     inWhereClause = appendBaseWhereClause(queryBuf, view, inWhereClause, tableAlias, null);
/*      */ 
/*  722 */     inWhereClause = appendSimpleWhereClause(queryBuf, view, inWhereClause, criteria, null);
/*      */ 
/*  724 */     if (inWhereClause)
/*      */     {
/*  726 */       String postWhereText = view.get("schPostWhereText");
/*  727 */       if (postWhereText != null)
/*      */       {
/*  729 */         queryBuf.append(postWhereText);
/*      */       }
/*      */     }
/*  732 */     inOrderClause = appendSimpleOrderClause(queryBuf, view, inOrderClause, sortOrder);
/*      */ 
/*  734 */     inOrderClause = appendBaseOrderClause(queryBuf, view, inOrderClause, tableAlias);
/*      */ 
/*  736 */     if (inOrderClause)
/*      */     {
/*  738 */       String postOrderText = view.get("schPostOrderText");
/*  739 */       if (postOrderText != null)
/*      */       {
/*  741 */         queryBuf.append(postOrderText);
/*      */       }
/*      */     }
/*  744 */     String postQueryText = view.get("schPostQueryText");
/*  745 */     if (postQueryText != null)
/*      */     {
/*  747 */       queryBuf.append(postQueryText);
/*      */     }
/*  749 */     return flags;
/*      */   }
/*      */ 
/*      */   public String buildSubmissionQuery(Workspace workspace, DataBinder binder, SchemaData target, SchemaViewData fieldListView, SchemaTableData table)
/*      */     throws DataException, ServiceException
/*      */   {
/*  764 */     IdcStringBuilder builder = new IdcStringBuilder();
/*  765 */     boolean isEdit = DataBinderUtils.getBoolean(binder, "schIsEdit", false);
/*      */ 
/*  768 */     if (isEdit)
/*      */     {
/*  770 */       builder.append("UPDATE ");
/*      */     }
/*      */     else
/*      */     {
/*  774 */       builder.append("INSERT INTO ");
/*      */     }
/*      */ 
/*  777 */     DataResultSet userFieldList = (DataResultSet)fieldListView.getAllViewValues();
/*      */ 
/*  779 */     HashMap fields = new HashMap();
/*  780 */     for (userFieldList.first(); userFieldList.isRowPresent(); userFieldList.next())
/*      */     {
/*  782 */       Properties props = userFieldList.getCurrentRowProps();
/*  783 */       String name = props.getProperty("schFieldName");
/*  784 */       SchemaData data = this.m_fields.getData(name);
/*  785 */       fields.put(name, data);
/*      */     }
/*  787 */     DataResultSet tableDef = table.getResultSet("TableDefinition");
/*      */ 
/*  790 */     StringBuffer tmpBuffer = new StringBuffer();
/*  791 */     String now = null;
/*  792 */     for (int loop = 0; loop < 2; ++loop)
/*      */     {
/*  794 */       if (loop == 0)
/*      */       {
/*  796 */         builder.append(table.m_name);
/*  797 */         if (isEdit)
/*      */         {
/*  799 */           builder.append(" SET ");
/*      */         }
/*      */         else
/*      */         {
/*  803 */           builder.append(" (");
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  808 */         if (isEdit) {
/*      */           continue;
/*      */         }
/*      */ 
/*  812 */         builder.append(") VALUES (");
/*      */       }
/*  814 */       boolean isFirstField = true;
/*  815 */       SchemaHelper helper = new SchemaHelper();
/*  816 */       for (tableDef.first(); tableDef.isRowPresent(); tableDef.next())
/*      */       {
/*  818 */         Properties props = tableDef.getCurrentRowProps();
/*  819 */         FieldInfo info = helper.makeFieldInfo(tableDef);
/*  820 */         SchemaFieldData field = (SchemaFieldData)fields.get(info.m_name);
/*  821 */         String value = binder.getAllowMissing(info.m_name);
/*  822 */         if (field != null)
/*      */         {
/*  824 */           if (value == null)
/*      */           {
/*  826 */             value = "";
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  831 */           String createTimestamp = table.get("schTableRowCreateTimestamp");
/*  832 */           String modifyTimestamp = table.get("schTableRowModifyTimestamp");
/*  833 */           String primaryKey = props.getProperty("IsPrimaryKey");
/*  834 */           if ((value == null) && (StringUtils.convertToBool(primaryKey, false)))
/*      */           {
/*  836 */             if (!isEdit)
/*      */             {
/*  839 */               ResultSet rset = workspace.createResultSetSQL("SELECT dNextIndex from Counters WHERE dCounterName='TranLock'");
/*      */ 
/*  842 */               value = rset.getStringValue(0);
/*      */             }
/*      */           }
/*  845 */           else if ((value == null) && (info.m_name.equals(createTimestamp)))
/*      */           {
/*  847 */             if (isEdit) {
/*      */               continue;
/*      */             }
/*      */ 
/*  851 */             if (now == null)
/*      */             {
/*  853 */               now = LocaleResources.m_dbFormat.format(new Date());
/*      */             }
/*      */ 
/*  856 */             value = now;
/*      */           }
/*  858 */           else if ((value == null) && (info.m_name.equals(modifyTimestamp)))
/*      */           {
/*  860 */             if (now == null)
/*      */             {
/*  862 */               now = LocaleResources.m_dbFormat.format(new Date());
/*      */             }
/*      */ 
/*  865 */             value = now;
/*      */           }
/*  867 */           else if ((!isEdit) && (value == null) && (info.m_name.equals("schSourceID")))
/*      */           {
/*  870 */             value = SharedObjects.getEnvironmentValue("IDC_Name");
/*      */           }
/*      */ 
/*  873 */           if (value == null)
/*      */           {
/*  877 */             String msg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, info.m_name);
/*      */ 
/*  879 */             throw new DataException(msg);
/*      */           }
/*      */         }
/*      */ 
/*  883 */         if ((info.m_type == 6) && (info.m_isFixedLen) && (value.length() > info.m_maxLen))
/*      */         {
/*  886 */           String msg = LocaleUtils.encodeMessage("apValueExeedsMaxLength", null, info.m_name, value, "" + info.m_maxLen);
/*      */ 
/*  889 */           throw new DataException(msg);
/*      */         }
/*  891 */         if (isEdit)
/*      */         {
/*  893 */           if (!isFirstField)
/*      */           {
/*  895 */             builder.append(", ");
/*      */           }
/*  897 */           builder.append(info.m_name);
/*  898 */           builder.append(" = ");
/*  899 */           QueryUtils.appendParam(tmpBuffer, info.m_type, value);
/*  900 */           builder.append(tmpBuffer);
/*  901 */           tmpBuffer.setLength(0);
/*      */         }
/*      */         else
/*      */         {
/*  905 */           if (!isFirstField)
/*      */           {
/*  907 */             builder.append(", ");
/*      */           }
/*  909 */           if (loop == 0)
/*      */           {
/*  911 */             builder.append(info.m_name);
/*      */           }
/*      */           else
/*      */           {
/*  915 */             QueryUtils.appendParam(tmpBuffer, info.m_type, value);
/*  916 */             builder.append(tmpBuffer);
/*  917 */             tmpBuffer.setLength(0);
/*      */           }
/*      */         }
/*  920 */         isFirstField = false;
/*      */       }
/*      */     }
/*  923 */     if (!isEdit)
/*      */     {
/*  929 */       builder.append(")");
/*      */     }
/*      */ 
/*  932 */     return builder.toString();
/*      */   }
/*      */ 
/*      */   public String createQueryForTable(SchemaData target, SchemaFieldData field, String dbTable, DataBinder binder, int opType)
/*      */   {
/*  942 */     IdcStringBuilder builder = new IdcStringBuilder();
/*      */ 
/*  944 */     String orderField = target.get("schOrderFieldName");
/*  945 */     String keyField = target.get("schKeyFieldName");
/*  946 */     if (orderField == null)
/*      */     {
/*  948 */       orderField = "schOrder";
/*      */     }
/*  950 */     if (keyField == null)
/*      */     {
/*  952 */       keyField = "schFieldName";
/*      */     }
/*      */ 
/*  955 */     if ((opType & 0x4) > 0)
/*      */     {
/*  957 */       builder.append("UPDATE ").append(dbTable).append(" set ").append(orderField).append(" = ").append(field.get(orderField)).append(" where ").append(keyField).append(" = '").append(StringUtils.createQuotableString(field.m_name)).append("'");
/*      */     }
/*      */ 
/*  970 */     return builder.toString();
/*      */   }
/*      */ 
/*      */   public int appendUptodateQuery(StringBuffer queryBuf, SchemaCacheItem item, SchemaViewData view, SchemaRelationData relationship)
/*      */     throws DataException
/*      */   {
/*  986 */     int flags = 0;
/*  987 */     String theQuery = view.get("schModifyTimestampQuery");
/*  988 */     String tableName = view.get("schTableName");
/*  989 */     String modifyTimestamp = null;
/*  990 */     SchemaTableData table = (SchemaTableData)this.m_tables.getData(tableName);
/*  991 */     if (table != null)
/*      */     {
/*  993 */       if (theQuery == null)
/*      */       {
/*  995 */         theQuery = table.get("schModifyTimestampQuery");
/*      */       }
/*  997 */       modifyTimestamp = table.get("schTableRowModifyTimestamp");
/*      */     }
/*      */ 
/* 1000 */     boolean foundQuery = false;
/* 1001 */     boolean useAllRowsTimestampQuery = false;
/* 1002 */     if (theQuery != null)
/*      */     {
/* 1004 */       foundQuery = true;
/* 1005 */       queryBuf.append(theQuery);
/*      */     }
/* 1007 */     else if ((modifyTimestamp != null) && (modifyTimestamp.length() > 0) && (!modifyTimestamp.equals("<none>")))
/*      */     {
/* 1010 */       SchemaHelper helper = new SchemaHelper();
/* 1011 */       foundQuery = true;
/* 1012 */       String useCompleteQueryString = view.get("ServerSchemaLoaderCheckTimestampsWithCompleteQuery", null);
/*      */ 
/* 1014 */       boolean useCompleteQuery = StringUtils.convertToBool(useCompleteQueryString, false);
/*      */ 
/* 1017 */       String childColumnName = null;
/* 1018 */       String parentValue = null;
/* 1019 */       if ((relationship != null) && (!useAllRowsTimestampQuery))
/*      */       {
/* 1021 */         childColumnName = helper.getMyTableColumn(view.m_name, relationship.m_name);
/*      */ 
/* 1023 */         String[] parentValues = item.getParentValues();
/* 1024 */         if (parentValues.length > 1)
/*      */         {
/* 1026 */           Report.trace("schemasql", "warning, multiple parent values specified.", null);
/*      */         }
/*      */ 
/* 1029 */         parentValue = parentValues[0];
/*      */       }
/*      */ 
/* 1032 */       if (useCompleteQuery)
/*      */       {
/* 1034 */         flags |= F_MULTIROW_TIMESTAMP_QUERY;
/* 1035 */         String[][] criteria = new String[0][];
/* 1036 */         if (childColumnName != null)
/*      */         {
/* 1038 */           criteria = new String[][] { { childColumnName, parentValue, "child" } };
/*      */         }
/*      */ 
/* 1043 */         appendLoadQuery(queryBuf, view, "child", criteria, (String[][])null);
/*      */       }
/*      */       else
/*      */       {
/* 1047 */         String useAllRowsTimestampQueryString = view.get("ServerSchemaLoaderUseAllRowsTimestampChecks", null);
/*      */ 
/* 1049 */         useAllRowsTimestampQuery = StringUtils.convertToBool(useAllRowsTimestampQueryString, false);
/*      */ 
/* 1051 */         if (useAllRowsTimestampQuery)
/*      */         {
/* 1053 */           flags |= F_ALLROWS_TIMESTAMP_QUERY;
/*      */         }
/* 1055 */         String expression = view.get("schModifyTimestampQueryExpression");
/*      */ 
/* 1057 */         if (expression == null)
/*      */         {
/* 1059 */           expression = "max(" + modifyTimestamp + ")";
/*      */         }
/* 1061 */         theQuery = "SELECT " + expression + " FROM " + tableName;
/* 1062 */         queryBuf.append("SELECT ");
/* 1063 */         queryBuf.append(expression);
/* 1064 */         queryBuf.append(" FROM ");
/* 1065 */         queryBuf.append(tableName);
/* 1066 */         boolean inWhereClause = false;
/* 1067 */         if ((childColumnName != null) && (!useAllRowsTimestampQuery))
/*      */         {
/* 1069 */           inWhereClause = appendSimpleWhereClause(queryBuf, view, inWhereClause, new String[][] { { childColumnName, parentValue, tableName } }, null);
/*      */ 
/* 1072 */           inWhereClause = appendBaseWhereClause(queryBuf, view, inWhereClause, table.m_name, null);
/*      */         }
/*      */ 
/* 1076 */         String orderClause = view.get("schModifyTimestampQueryOrderClause");
/*      */ 
/* 1078 */         if (orderClause != null)
/*      */         {
/* 1080 */           queryBuf.append(" ORDER BY ");
/* 1081 */           queryBuf.append(orderClause);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1086 */     if (!foundQuery)
/*      */     {
/* 1088 */       return F_NO_QUERY;
/*      */     }
/* 1090 */     return flags;
/*      */   }
/*      */ 
/*      */   public boolean isArchiveable()
/*      */   {
/* 1099 */     return (this.m_hasSourceID) || (this.m_tsFields.size() == 2);
/*      */   }
/*      */ 
/*      */   public void determinePKColumnInfos(String tableName, Workspace ws, ResultSet rset)
/*      */     throws DataException
/*      */   {
/* 1107 */     String[] pkColumns = ws.getPrimaryKeys(tableName);
/* 1108 */     this.m_pkColumnInfos = ResultSetUtils.createInfoList(rset, pkColumns, true);
/*      */   }
/*      */ 
/*      */   public void insertIntoDeleteTable(String tableName, DataBinder workBinder, DataResultSet infoSet, Workspace ws)
/*      */     throws DataException
/*      */   {
/* 1117 */     if (!isArchiveable())
/*      */     {
/* 1119 */       return;
/*      */     }
/*      */ 
/* 1122 */     if (this.m_pkColumnInfos == null)
/*      */     {
/* 1125 */       determinePKColumnInfos(tableName, ws, infoSet);
/*      */     }
/*      */ 
/* 1128 */     Vector clmns = new IdcVector();
/* 1129 */     Vector values = new IdcVector();
/*      */ 
/* 1131 */     int len = this.m_pkColumnInfos.length;
/* 1132 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1134 */       FieldInfo fi = this.m_pkColumnInfos[i];
/* 1135 */       String name = fi.m_name;
/* 1136 */       String val = workBinder.get(name);
/*      */ 
/* 1138 */       clmns.addElement(name);
/* 1139 */       values.addElement(val);
/*      */     }
/*      */ 
/* 1142 */     String pkStr = StringUtils.createString(clmns, ',', '^');
/* 1143 */     String valStr = StringUtils.createString(values, ',', '^');
/*      */ 
/* 1145 */     String sourceID = workBinder.getLocal("schSourceID");
/* 1146 */     DeletesTableUtils.insertEntry(ws, tableName, valStr, pkStr, null, sourceID, null);
/*      */   }
/*      */ 
/*      */   public String prepareSendSchemaViewFragment(Service service, DataBinder binder, SchemaHelper helper, String viewName, String relationName, String parentValue)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1154 */     String[] parentValues = { parentValue };
/* 1155 */     return prepareSendSchemaViewFragment(service, binder, helper, viewName, relationName, parentValues, new HashMap());
/*      */   }
/*      */ 
/*      */   public String prepareSendSchemaViewFragmentEx(Service service, DataBinder binder, SchemaHelper helper, String viewName, String relationName, String[] parentValues)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1164 */     return prepareSendSchemaViewFragment(service, binder, helper, viewName, relationName, parentValues, new HashMap());
/*      */   }
/*      */ 
/*      */   public String prepareSendSchemaViewFragment(Service service, DataBinder binder, SchemaHelper helper, String viewName, String relationName, String[] parentValues, Map args)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1177 */     SchemaRelationData relationDef = null;
/* 1178 */     String[] parentFieldNames = null;
/*      */ 
/* 1180 */     SchemaViewData viewDef = helper.getView(viewName);
/*      */     String exceptionMessage;
/*      */     String page;
/*      */     String exceptionMessage;
/* 1182 */     if (relationName == null)
/*      */     {
/* 1184 */       String page = "SCHEMA_VIEW_JS";
/* 1185 */       exceptionMessage = LocaleUtils.encodeMessage("csSchemaErrorGeneratingViewData", null, viewName);
/*      */     }
/*      */     else
/*      */     {
/* 1190 */       relationDef = helper.getRelation(relationName);
/* 1191 */       if (relationDef == null)
/*      */       {
/* 1193 */         parentFieldNames = new String[] { "ParentColumn" };
/*      */ 
/* 1195 */         String msg = LocaleUtils.encodeMessage("wwSchemaObjectDoesntExist_relationship", null, relationName);
/*      */ 
/* 1198 */         binder.setFieldType("schClientErrorMessage", "message");
/* 1199 */         binder.putLocal("schClientErrorMessage", msg);
/*      */       }
/*      */       else
/*      */       {
/* 1203 */         SchemaLoader loader = this.m_views.findLoader(viewDef, relationDef, null);
/* 1204 */         parentFieldNames = loader.constructParentFieldsArray(null, relationDef, viewDef, null);
/*      */ 
/* 1206 */         binder.putLocal("RelationName", relationDef.m_name);
/*      */       }
/* 1208 */       binder.putLocal("ParentValue", parentValues[0]);
/* 1209 */       page = "SCHEMA_VIEW_JS_FRAGMENT";
/* 1210 */       exceptionMessage = LocaleUtils.encodeMessage("csSchemaErrorGeneratingViewFragmentData", null, viewName, relationName, parentValues[0]);
/*      */     }
/*      */     String resultSetName;
/* 1215 */     if (viewDef == null)
/*      */     {
/* 1217 */       String resultSetName = "UnknownTable";
/* 1218 */       binder.putLocal("schInternalColumn", "KeyUnknown");
/* 1219 */       binder.putLocal("schLabelColumn", "LabelUnknown");
/*      */     }
/*      */     else
/*      */     {
/* 1223 */       viewDef.populateBinder(binder);
/* 1224 */       resultSetName = viewDef.m_canonicalName;
/*      */     }
/* 1226 */     binder.putLocal("PublishedTableName", resultSetName);
/*      */     try
/*      */     {
/* 1230 */       SchemaSecurityFilter securityImplementor = null;
/* 1231 */       if (viewDef != null)
/*      */       {
/* 1233 */         securityImplementor = getSecurityImplementor(viewDef, service);
/*      */       }
/*      */       ResultSet rset;
/* 1236 */       if (viewDef == null)
/*      */       {
/* 1238 */         DataResultSet drset = new DataResultSet(new String[] { "KeyUnknown", "LabelUnknown" });
/*      */ 
/* 1240 */         ResultSet rset = drset;
/* 1241 */         binder.setFieldType("schClientErrorMessage", "message");
/* 1242 */         String msg = LocaleUtils.encodeMessage("wwSchemaObjectDoesntExist_view", null, viewName);
/*      */ 
/* 1244 */         binder.putLocal("schClientErrorMessage", msg);
/*      */       }
/*      */       else
/*      */       {
/* 1248 */         if (securityImplementor != null)
/*      */         {
/* 1250 */           args.put("filter", securityImplementor);
/*      */         }
/*      */         ResultSet rset;
/* 1252 */         if (relationName == null)
/*      */         {
/* 1254 */           rset = viewDef.getViewValues(args);
/*      */         }
/*      */         else
/*      */         {
/* 1258 */           DataResultSet parentValuesSet = helper.createParentSelectorSetEx(parentFieldNames, parentValues);
/*      */ 
/* 1260 */           Report.trace("schemapagecreation", "getSchemaViewFragment() creating '" + viewName + "' relation '" + relationName + "' parentValue '" + parentValues[0] + "' parentColumn '" + parentFieldNames[0] + "'", null);
/*      */           ResultSet rset;
/* 1265 */           if (relationDef != null)
/*      */           {
/* 1267 */             args.put("relationName", relationName);
/* 1268 */             args.put("parentValues", parentValuesSet);
/* 1269 */             rset = viewDef.getViewValues(args);
/*      */           }
/*      */           else
/*      */           {
/* 1273 */             binder.putLocal("viewPath", StringUtils.encodeJavascriptFilename(viewDef.m_name) + "/" + relationName + "/" + StringUtils.encodeJavascriptFilename(parentValues[0]));
/*      */ 
/* 1277 */             binder.putLocal("RelationName", relationName);
/* 1278 */             Vector columns = viewDef.getVector("schViewColumns");
/* 1279 */             String[] columnsArray = new String[columns.size()];
/* 1280 */             columns.copyInto(columnsArray);
/* 1281 */             DataResultSet drset = new DataResultSet(columnsArray);
/* 1282 */             rset = drset;
/*      */           }
/*      */         }
/* 1285 */         int flags = (binder.m_isJava) ? 1 : 0;
/* 1286 */         rset = viewDef.prepareForConsumption(rset, service, flags);
/*      */       }
/* 1288 */       binder.addResultSet(resultSetName, rset);
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1292 */       t.printStackTrace();
/* 1293 */       DataResultSet drset = new DataResultSet();
/* 1294 */       binder.addResultSet(viewDef.m_canonicalName, drset);
/* 1295 */       Report.error(null, exceptionMessage, t);
/* 1296 */       String msg = LocaleUtils.appendMessage(t.getMessage(), exceptionMessage);
/*      */ 
/* 1298 */       binder.putLocal("schClientErrorMessage", msg);
/* 1299 */       binder.setFieldType("schClientErrorMessage", "message");
/*      */     }
/*      */ 
/* 1302 */     binder.setContentType("application/x-javascript");
/* 1303 */     return page;
/*      */   }
/*      */ 
/*      */   public SchemaSecurityFilter getSecurityImplementor(SchemaViewData viewDef)
/*      */     throws ServiceException
/*      */   {
/* 1309 */     return getSecurityImplementor(viewDef, "schSecurityImplementor", null);
/*      */   }
/*      */ 
/*      */   public SchemaSecurityFilter getSecurityImplementor(SchemaViewData viewDef, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/* 1316 */     SchemaSecurityFilter filter = getSecurityImplementor(viewDef, "schSecurityImplementor", context);
/*      */ 
/* 1318 */     return filter;
/*      */   }
/*      */ 
/*      */   public SchemaSecurityFilter getSecurityImplementor(SchemaData def, String fieldName, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/* 1325 */     String securityImplementorName = def.get(fieldName);
/* 1326 */     SchemaSecurityFilter securityImplementor = null;
/* 1327 */     if (securityImplementorName != null)
/*      */     {
/* 1329 */       String msg = LocaleUtils.encodeMessage("csSchUnableToConstructSecurityClass", null, securityImplementorName, def.m_name);
/*      */ 
/* 1332 */       securityImplementor = (SchemaSecurityFilter)ComponentClassFactory.createClassInstance(securityImplementorName, securityImplementorName, msg);
/*      */     }
/*      */ 
/* 1336 */     if ((securityImplementor != null) && (context != null))
/*      */     {
/* 1338 */       securityImplementor.init(context);
/*      */     }
/* 1340 */     return securityImplementor;
/*      */   }
/*      */ 
/*      */   public DataResultSet createTableDefinition(Workspace ws, String tableName)
/*      */     throws DataException
/*      */   {
/* 1347 */     DataResultSet drset = new DataResultSet(SchemaTableConfig.TABLE_DEFINITION_COLUMNS);
/*      */ 
/* 1349 */     String primaryKey = "";
/* 1350 */     String[] pKeys = ws.getPrimaryKeys(tableName);
/* 1351 */     int len = pKeys.length;
/* 1352 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1354 */       if (primaryKey.length() > 0)
/*      */       {
/* 1356 */         primaryKey = primaryKey + ",";
/*      */       }
/* 1358 */       primaryKey = primaryKey + pKeys[i];
/*      */     }
/*      */ 
/* 1361 */     DataBinder binder = new DataBinder();
/* 1362 */     FieldInfo[] fieldInfo = ws.getColumnList(tableName);
/* 1363 */     for (int i = 0; i < fieldInfo.length; ++i)
/*      */     {
/* 1365 */       FieldInfo info = fieldInfo[i];
/* 1366 */       String name = info.m_name;
/* 1367 */       binder.putLocal("ColumnName", name);
/* 1368 */       binder.putLocal("ColumnType", QueryUtils.convertInfoTypeToString(info.m_type));
/*      */ 
/* 1370 */       binder.putLocal("ColumnLength", "" + info.m_maxLen);
/*      */ 
/* 1372 */       boolean isPrimary = false;
/* 1373 */       for (int j = 0; j < len; ++j)
/*      */       {
/* 1375 */         String pKey = pKeys[j];
/* 1376 */         if (!pKey.equals(name))
/*      */           continue;
/* 1378 */         isPrimary = true;
/* 1379 */         break;
/*      */       }
/*      */ 
/* 1382 */       binder.putLocal("IsPrimaryKey", "" + isPrimary);
/* 1383 */       Vector v = drset.createRow(binder);
/* 1384 */       drset.addRow(v);
/*      */     }
/* 1386 */     return drset;
/*      */   }
/*      */ 
/*      */   public int appendViewColumns(StringBuffer queryBuf, SchemaViewData viewDef, String prefix, int flags)
/*      */     throws DataException
/*      */   {
/* 1392 */     String viewColumnsString = viewDef.get("schViewColumns");
/* 1393 */     String createTimestamp = viewDef.get("schTableRowCreateTimestamp");
/* 1394 */     if ((createTimestamp != null) && (((createTimestamp.length() == 0) || (createTimestamp.equals("<none>")))))
/*      */     {
/* 1397 */       createTimestamp = null;
/*      */     }
/*      */ 
/* 1400 */     String modifyTimestamp = viewDef.get("schTableRowModifyTimestamp");
/* 1401 */     if ((modifyTimestamp != null) && (((modifyTimestamp.length() == 0) || (modifyTimestamp.equals("<none>")))))
/*      */     {
/* 1404 */       modifyTimestamp = null;
/*      */     }
/*      */ 
/* 1407 */     Vector viewColumns = StringUtils.parseArray(viewColumnsString, ',', '^');
/* 1408 */     for (int i = 0; i < viewColumns.size(); ++i)
/*      */     {
/* 1410 */       if (i > 0)
/*      */       {
/* 1412 */         queryBuf.append(", ");
/*      */       }
/* 1414 */       if (prefix != null)
/*      */       {
/* 1416 */         queryBuf.append(prefix);
/* 1417 */         queryBuf.append(".");
/*      */       }
/* 1419 */       String column = (String)viewColumns.elementAt(i);
/* 1420 */       if ((createTimestamp != null) && (column.equals(modifyTimestamp)))
/*      */       {
/* 1422 */         createTimestamp = null;
/*      */       }
/* 1424 */       if ((modifyTimestamp != null) && (column.equals(modifyTimestamp)))
/*      */       {
/* 1426 */         modifyTimestamp = null;
/*      */       }
/* 1428 */       queryBuf.append(column);
/*      */     }
/*      */ 
/* 1431 */     if (createTimestamp != null)
/*      */     {
/* 1433 */       queryBuf.append(", ");
/* 1434 */       if (prefix != null)
/*      */       {
/* 1436 */         queryBuf.append(prefix);
/* 1437 */         queryBuf.append(".");
/*      */       }
/* 1439 */       queryBuf.append(createTimestamp);
/*      */     }
/* 1441 */     if (modifyTimestamp != null)
/*      */     {
/* 1443 */       queryBuf.append(", ");
/* 1444 */       if (prefix != null)
/*      */       {
/* 1446 */         queryBuf.append(prefix);
/* 1447 */         queryBuf.append(".");
/*      */       }
/* 1449 */       queryBuf.append(modifyTimestamp);
/*      */     }
/*      */ 
/* 1452 */     return 0;
/*      */   }
/*      */ 
/*      */   public ResultSet doServerProcessing(SchemaViewData viewDef, ResultSet rset, Map args)
/*      */     throws DataException
/*      */   {
/* 1458 */     return doServerProcessing(viewDef, rset, args, F_USE_DEFAULTS);
/*      */   }
/*      */ 
/*      */   public ResultSet doServerProcessing(SchemaViewData viewDef, ResultSet rset, Map args, int flags)
/*      */     throws DataException
/*      */   {
/* 1465 */     if ((flags & F_APPLY_CRITERIA) != 0)
/*      */     {
/* 1467 */       DataResultSet newResultSet = null;
/* 1468 */       int count = 0;
/* 1469 */       ArrayList indexes = new ArrayList();
/* 1470 */       ArrayList values = new ArrayList();
/* 1471 */       ArrayList flagsList = new ArrayList();
/* 1472 */       FieldInfo info = new FieldInfo();
/* 1473 */       for (int i = 0; ; ++i)
/*      */       {
/* 1475 */         String field = viewDef.get("schCriteriaField" + i);
/* 1476 */         String value = viewDef.get("schCriteriaValue" + i);
/* 1477 */         if (field == null) break; if (value == null) {
/*      */           break;
/*      */         }
/*      */ 
/* 1481 */         if (!rset.getFieldInfo(field, info))
/*      */         {
/* 1484 */           throw new DataException(null, -26, "csSchCriteriaFieldMissing2", new Object[] { field, viewDef.m_name });
/*      */         }
/*      */ 
/* 1487 */         indexes.add(new int[] { info.m_index });
/* 1488 */         values.add(value);
/* 1489 */         int fieldFlags = 4;
/* 1490 */         if (value.equals("true"))
/*      */         {
/* 1492 */           fieldFlags |= 1;
/*      */         }
/* 1494 */         else if (value.equals("false"))
/*      */         {
/* 1496 */           fieldFlags |= 2;
/*      */         }
/* 1498 */         flagsList.add(new int[] { fieldFlags });
/*      */       }
/* 1500 */       int[] indexesArray = new int[indexes.size()];
/* 1501 */       String[] valuesArray = new String[indexesArray.length];
/* 1502 */       int[] flagsArray = new int[indexesArray.length];
/* 1503 */       for (int i = 0; i < indexesArray.length; ++i)
/*      */       {
/* 1505 */         indexesArray[i] = ((int[])(int[])indexes.get(i))[0];
/* 1506 */         valuesArray[i] = ((String)values.get(i));
/* 1507 */         flagsArray[i] = ((int[])(int[])flagsList.get(i))[0];
/*      */       }
/* 1509 */       while (rset.isRowPresent())
/*      */       {
/* 1511 */         Vector r = ((DataResultSet)rset).getCurrentRowValues();
/* 1512 */         boolean passed = rowPassesCriteria(rset, r, viewDef, indexesArray, valuesArray, flagsArray);
/*      */ 
/* 1514 */         if ((passed) && (newResultSet == null))
/*      */         {
/* 1516 */           ++count;
/*      */         }
/* 1520 */         else if (newResultSet == null)
/*      */         {
/* 1522 */           newResultSet = new DataResultSet();
/* 1523 */           rset.first();
/* 1524 */           if (count > 0)
/*      */           {
/* 1526 */             newResultSet.copy(rset, count);
/*      */           }
/*      */           else
/*      */           {
/* 1530 */             newResultSet.copyFieldInfo(rset);
/*      */           }
/*      */         }
/* 1533 */         else if (passed)
/*      */         {
/* 1535 */           newResultSet.addRow(r);
/*      */         }
/*      */ 
/* 1538 */         rset.next();
/*      */       }
/*      */ 
/* 1541 */       if (newResultSet != null)
/*      */       {
/* 1543 */         newResultSet.first();
/* 1544 */         rset = newResultSet;
/*      */       }
/*      */       else
/*      */       {
/* 1548 */         if (count == 0)
/*      */         {
/* 1551 */           newResultSet = new DataResultSet();
/* 1552 */           newResultSet.copyFieldInfo(rset);
/* 1553 */           rset = newResultSet;
/*      */         }
/* 1555 */         rset.first();
/*      */       }
/*      */     }
/*      */ 
/* 1559 */     boolean serverSorted = viewDef.getBoolean("schIsServerSorted", false);
/* 1560 */     String sortField = viewDef.get("schSortField");
/* 1561 */     if ((serverSorted) && (sortField != null) && (rset.isRowPresent()))
/*      */     {
/* 1563 */       SchemaHelper helper = new SchemaHelper();
/* 1564 */       rset = helper.sortViewData(viewDef, rset);
/*      */     }
/*      */ 
/* 1567 */     return rset;
/*      */   }
/*      */ 
/*      */   protected boolean rowPassesCriteria(ResultSet r, Vector row, SchemaViewData viewDef, int[] criteriaIndexes, String[] criteriaValues, int[] criteriaFlags)
/*      */   {
/* 1574 */     for (int i = 0; i < criteriaIndexes.length; ++i)
/*      */     {
/* 1576 */       String value = (String)row.elementAt(criteriaIndexes[i]);
/* 1577 */       if ((criteriaFlags[i] & 0x1) != 0)
/*      */       {
/* 1579 */         if (!StringUtils.convertToBool(value, false))
/*      */         {
/* 1581 */           return false;
/*      */         }
/*      */       }
/* 1584 */       else if ((criteriaFlags[i] & 0x2) != 0)
/*      */       {
/* 1586 */         if (StringUtils.convertToBool(value, false))
/*      */         {
/* 1588 */           return false;
/*      */         }
/*      */       }
/* 1591 */       else if ((criteriaFlags[i] & 0x4) != 0)
/*      */       {
/* 1593 */         if (!criteriaValues[i].equalsIgnoreCase(value))
/*      */         {
/* 1595 */           return false;
/*      */         }
/*      */ 
/*      */       }
/* 1600 */       else if (!criteriaValues[i].equals(value))
/*      */       {
/* 1602 */         return false;
/*      */       }
/*      */     }
/*      */ 
/* 1606 */     return true;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public DataResultSet doServerSorting(int sortColumnIndex, SchemaViewData viewDef, ResultSet rset)
/*      */     throws DataException
/*      */   {
/* 1616 */     SystemUtils.reportDeprecatedUsage("SchemaUtils.doServerSorting()");
/*      */     DataResultSet sortedResultSet;
/*      */     DataResultSet sortedResultSet;
/* 1618 */     if (rset instanceof DataResultSet)
/*      */     {
/* 1620 */       sortedResultSet = (DataResultSet)rset;
/*      */     }
/*      */     else
/*      */     {
/* 1624 */       sortedResultSet = new DataResultSet();
/* 1625 */       sortedResultSet.copy(rset);
/*      */     }
/* 1627 */     checkCopyAborted(sortedResultSet, viewDef);
/* 1628 */     ResultSetTreeSort sorter = new ResultSetTreeSort(sortedResultSet, sortColumnIndex, false);
/*      */ 
/* 1630 */     sorter.determineFieldType(null);
/* 1631 */     String sortOrder = viewDef.get("schSortOrder");
/* 1632 */     sorter.determineIsAscending(sortOrder);
/* 1633 */     sorter.sort();
/* 1634 */     sortedResultSet.first();
/* 1635 */     return sortedResultSet;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public boolean checkCopyAborted(DataResultSet drset, SchemaViewData data)
/*      */     throws DataException
/*      */   {
/* 1643 */     SystemUtils.reportDeprecatedUsage("use SchemaHelper.checkCopyAborted() instead of SchemaUtils.checkCopyAborted()");
/*      */ 
/* 1645 */     SchemaHelper helper = new SchemaHelper();
/* 1646 */     return helper.checkCopyAborted(drset, data);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public int getViewLoadLimit(SchemaViewData data)
/*      */   {
/* 1653 */     SystemUtils.reportDeprecatedUsage("use SchemaHelper.getViewLoadLimit() instead of SchemaUtils.getViewLoadLimit()");
/*      */ 
/* 1655 */     SchemaHelper helper = new SchemaHelper();
/* 1656 */     return helper.getViewLoadLimit(data);
/*      */   }
/*      */ 
/*      */   public ExecutionContext constructExecutionContext(Map args)
/*      */   {
/* 1661 */     SchemaHelper helper = new SchemaHelper();
/* 1662 */     return helper.constructExecutionContext(args);
/*      */   }
/*      */ 
/*      */   public void mergePage(String page, DataBinder binder, ExecutionContext context, Writer w)
/*      */     throws DataException, ServiceException, IOException, ParseSyntaxException
/*      */   {
/* 1668 */     PageMerger merger = new PageMerger(binder, context);
/* 1669 */     context.setCachedObject("PageMerger", merger);
/* 1670 */     DataLoader.checkCachedPage(page, this.m_dataLoaderExecutionContext);
/* 1671 */     DynamicHtml dynHtml = SharedObjects.getHtmlPage(page);
/* 1672 */     if (dynHtml == null)
/*      */     {
/* 1674 */       String msg = LocaleUtils.encodeMessage("csPageMergerUnableToCreateMergedPage", null, page);
/*      */ 
/* 1676 */       throw new DataException(msg);
/*      */     }
/* 1678 */     merger.outputNonPersonalizedHtml(dynHtml, w);
/* 1679 */     merger.releaseAllTemporary();
/*      */   }
/*      */ 
/*      */   public void validateTypeDowngrade(String fieldName, String fieldValue, SchemaFieldData field, DataBinder binder) throws ServiceException
/*      */   {
/* 1684 */     if ((!fieldName.equals("dType")) || (field == null))
/*      */       return;
/* 1686 */     String typeOld = field.getData().getLocal("dType");
/* 1687 */     String typeNew = fieldValue;
/* 1688 */     if (typeNew == null)
/*      */     {
/* 1690 */       typeNew = "";
/*      */     }
/* 1692 */     boolean isValidTypeUpgrade = (typeOld != null) && (((typeOld.equalsIgnoreCase(typeNew)) || ((typeOld.equalsIgnoreCase("BigText")) && (typeNew.equalsIgnoreCase("Memo"))) || ((typeOld.equalsIgnoreCase("Text")) && (((typeNew.equalsIgnoreCase("BigText")) || (typeNew.equalsIgnoreCase("Memo")))))));
/*      */ 
/* 1697 */     if ((isValidTypeUpgrade) && (typeOld.equalsIgnoreCase("Decimal")) && (typeNew.equalsIgnoreCase("Decimal")))
/*      */     {
/* 1699 */       String decimalScale = binder.getAllowMissing("dDecimalScale");
/* 1700 */       String decimalScaleOld = field.getData().getLocal("dDecimalScale");
/* 1701 */       int decimalScaleInt = NumberUtils.parseInteger(decimalScale, -1);
/* 1702 */       int decimalScaleOldInt = NumberUtils.parseInteger(decimalScaleOld, -1);
/* 1703 */       if (decimalScaleInt != decimalScaleOldInt)
/*      */       {
/* 1705 */         typeOld = "Decimal (scale " + decimalScaleOldInt + ")";
/* 1706 */         typeNew = "Decimal (scale " + decimalScaleInt + ")";
/* 1707 */         isValidTypeUpgrade = false;
/*      */       }
/*      */     }
/* 1710 */     if (isValidTypeUpgrade)
/*      */       return;
/* 1712 */     String msg = LocaleUtils.encodeMessage("csUnableToDowngradeFieldType", null, typeOld, typeNew);
/*      */ 
/* 1714 */     throw new ServiceException(msg);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1721 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99213 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.SchemaUtils
 * JD-Core Version:    0.5.4
 */