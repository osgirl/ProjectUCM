/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.QueryUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.server.schema.SchemaManager;
/*      */ import intradoc.server.schema.SchemaStorage;
/*      */ import intradoc.server.schema.SchemaUtils;
/*      */ import intradoc.server.schema.ServerSchemaManager;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.LRUManager;
/*      */ import intradoc.shared.MetaFieldUtils;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.schema.NamedRelationship;
/*      */ import intradoc.shared.schema.SchemaCacheItem;
/*      */ import intradoc.shared.schema.SchemaData;
/*      */ import intradoc.shared.schema.SchemaEditHelper;
/*      */ import intradoc.shared.schema.SchemaFieldConfig;
/*      */ import intradoc.shared.schema.SchemaFieldData;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import intradoc.shared.schema.SchemaLoader;
/*      */ import intradoc.shared.schema.SchemaRelationData;
/*      */ import intradoc.shared.schema.SchemaSecurityFilter;
/*      */ import intradoc.shared.schema.SchemaTableConfig;
/*      */ import intradoc.shared.schema.SchemaTableData;
/*      */ import intradoc.shared.schema.SchemaViewConfig;
/*      */ import intradoc.shared.schema.SchemaViewData;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.CharConversionException;
/*      */ import java.io.OutputStream;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SchemaService extends Service
/*      */ {
/*      */   protected ServerSchemaManager m_schemaManager;
/*      */   protected SchemaUtils m_schemaUtils;
/*      */   protected SchemaHelper m_schemaHelper;
/*      */   protected SchemaEditHelper m_editHelper;
/*      */   protected SchemaStorage m_tables;
/*      */   protected SchemaStorage m_views;
/*      */   protected SchemaStorage m_relations;
/*      */   protected SchemaStorage m_fields;
/*      */ 
/*      */   public void init(Workspace ws, OutputStream os, DataBinder binder, ServiceData serviceData)
/*      */     throws DataException
/*      */   {
/*   49 */     super.init(ws, os, binder, serviceData);
/*      */     try
/*      */     {
/*   52 */       this.m_schemaManager = SchemaManager.getManager(this.m_workspace);
/*   53 */       this.m_tables = this.m_schemaManager.getStorageImplementor("SchemaTableConfig");
/*   54 */       this.m_views = this.m_schemaManager.getStorageImplementor("SchemaViewConfig");
/*   55 */       this.m_relations = this.m_schemaManager.getStorageImplementor("SchemaRelationConfig");
/*   56 */       this.m_fields = this.m_schemaManager.getStorageImplementor("SchemaFieldConfig");
/*      */ 
/*   58 */       this.m_schemaUtils = ((SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", null));
/*      */ 
/*   61 */       this.m_schemaUtils.init();
/*   62 */       this.m_schemaHelper = ((SchemaHelper)ComponentClassFactory.createClassInstance("SchemaHelper", "intradoc.shared.schema.SchemaHelper", null));
/*      */ 
/*   65 */       this.m_schemaHelper.init();
/*   66 */       this.m_editHelper = ((SchemaEditHelper)ComponentClassFactory.createClassInstance("SchemaEditHelper", "intradoc.shared.schema.SchemaEditHelper", null));
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*   72 */       throw new DataException(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   public void createHandlersForService()
/*      */     throws ServiceException, DataException
/*      */   {
/*   79 */     super.createHandlersForService();
/*   80 */     createHandlers("SchemaService");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getSchemaTables()
/*      */     throws ServiceException, DataException
/*      */   {
/*   94 */     SchemaTableConfig tables = (SchemaTableConfig)SharedObjects.getTable("SchemaTableConfig");
/*   95 */     if (tables == null)
/*      */     {
/*   97 */       throw new DataException("!$SchemaTableConfig missing.");
/*      */     }
/*   99 */     this.m_schemaManager.getSchemaData(this.m_binder, "SchemaTableConfig");
/*      */ 
/*  101 */     String loadTables = this.m_binder.getLocal("LoadDatabaseTables");
/*  102 */     if (!StringUtils.convertToBool(loadTables, false))
/*      */       return;
/*  104 */     String[] dbTables = this.m_workspace.getTableList();
/*  105 */     int num = dbTables.length;
/*  106 */     DataBinder binder = new DataBinder();
/*      */ 
/*  108 */     DataResultSet unknownTables = new DataResultSet(new String[] { SchemaTableConfig.TABLE_COLUMNS[0] });
/*      */ 
/*  111 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  116 */       String name = dbTables[i];
/*      */ 
/*  118 */       if (name.endsWith("_"))
/*      */       {
/*  121 */         name = name.substring(0, name.length() - "_".length());
/*      */       }
/*      */ 
/*  125 */       SchemaData data = tables.getData(name);
/*  126 */       if (data != null) {
/*      */         continue;
/*      */       }
/*  129 */       binder.putLocal(SchemaTableConfig.TABLE_COLUMNS[0], name);
/*      */ 
/*  131 */       Vector row = unknownTables.createRow(binder);
/*  132 */       unknownTables.addRow(row);
/*      */     }
/*      */ 
/*  135 */     this.m_binder.addResultSet("UnknownTableList", unknownTables);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getSchemaTableInfo()
/*      */     throws ServiceException, DataException
/*      */   {
/*  148 */     String viewType = this.m_binder.getLocal("schViewType");
/*  149 */     if (viewType != null)
/*      */     {
/*  151 */       if (viewType.equalsIgnoreCase("optionList"))
/*      */       {
/*  154 */         MetaFieldUtils.getOptionList(this.m_binder, "OptionList");
/*  155 */         return;
/*      */       }
/*  157 */       if (!viewType.equalsIgnoreCase("table"))
/*      */       {
/*  167 */         return;
/*      */       }
/*      */     }
/*  170 */     boolean isCreateTable = DataBinderUtils.getBoolean(this.m_binder, "IsCreateTable", false);
/*  171 */     String tableName = this.m_binder.getLocal("schTableName");
/*  172 */     String resultSetName = this.m_binder.getLocal("schTableResultSetName");
/*  173 */     String useDatabase = this.m_binder.getLocal("schQueryDatabase");
/*  174 */     if (resultSetName == null)
/*      */     {
/*  176 */       resultSetName = "TableDefinition";
/*      */     }
/*      */ 
/*  179 */     boolean doesExist = checkTableExistence(tableName);
/*  180 */     this.m_binder.putLocal("TableExists", "" + doesExist);
/*      */ 
/*  185 */     SchemaTableConfig tables = (SchemaTableConfig)SharedObjects.getTable("SchemaTableConfig");
/*      */ 
/*  187 */     SchemaTableData data = (SchemaTableData)tables.getData(tableName);
/*  188 */     if ((data != null) && (!StringUtils.convertToBool(useDatabase, false)))
/*      */     {
/*  190 */       data.populateBinder(this.m_binder);
/*  191 */       return;
/*      */     }
/*      */ 
/*  194 */     DataResultSet drset = null;
/*  195 */     if (doesExist)
/*      */     {
/*  197 */       drset = this.m_schemaUtils.createTableDefinition(this.m_workspace, tableName);
/*      */     }
/*      */     else
/*      */     {
/*  201 */       drset = new DataResultSet(SchemaTableConfig.TABLE_DEFINITION_COLUMNS);
/*  202 */       if (!isCreateTable)
/*      */       {
/*  212 */         String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_table", null, tableName);
/*      */ 
/*  214 */         createServiceException(null, msg);
/*      */       }
/*      */     }
/*  217 */     this.m_binder.addResultSet(resultSetName, drset);
/*  218 */     this.m_binder.putLocal("TableExists", "" + doesExist);
/*      */ 
/*  220 */     String description = this.m_binder.getAllowMissing("schTableDescription");
/*  221 */     if (description == null)
/*      */     {
/*  223 */       description = "";
/*      */     }
/*      */ 
/*  226 */     String[] cols = SchemaTableConfig.TABLE_COLUMNS;
/*  227 */     this.m_binder.putLocal(cols[0], tableName);
/*  228 */     this.m_binder.putLocal(cols[2], description);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addOrEditSchemaTable()
/*      */     throws ServiceException, DataException
/*      */   {
/*  235 */     boolean isCreate = DataBinderUtils.getBoolean(this.m_binder, "IsCreateTable", false);
/*  236 */     boolean isAddExisting = DataBinderUtils.getBoolean(this.m_binder, "IsAddExistingTable", false);
/*  237 */     String msg = null;
/*  238 */     String table = this.m_binder.getLocal("schTableName");
/*      */     try
/*      */     {
/*  241 */       if (isCreate)
/*      */       {
/*  243 */         msg = "csDbUnableToPerformAction_create";
/*  244 */         this.m_schemaManager.addSchemaTable(this.m_workspace, this.m_binder);
/*      */       }
/*  246 */       else if (isAddExisting)
/*      */       {
/*  248 */         msg = "csDbUnableToPerformAction_addToList";
/*  249 */         this.m_schemaManager.addSchemaExistingTable(this.m_workspace, this.m_binder);
/*      */       }
/*      */       else
/*      */       {
/*  253 */         msg = "csDbUnableToPerformAction_alter";
/*  254 */         this.m_schemaManager.editSchemaTable(this.m_workspace, this.m_binder);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  259 */       Report.trace("schema", null, e);
/*  260 */       msg = LocaleUtils.encodeMessage(msg, null, table);
/*  261 */       createServiceException(e, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void controlSchema() throws ServiceException, DataException
/*      */   {
/*  268 */     if (DataBinderUtils.getBoolean(this.m_binder, "PublishSchema", false))
/*      */     {
/*  270 */       publishSchema();
/*      */     }
/*      */ 
/*  273 */     LRUManager manager = (LRUManager)SharedObjects.getObject("globalObjects", "SchemaViewLRUManager");
/*      */ 
/*  276 */     int maxSize = DataBinderUtils.getInteger(this.m_binder, "SchemaViewCacheMaxSize", -1);
/*  277 */     if (maxSize != -1)
/*      */     {
/*  279 */       manager.setUsage(maxSize * 1024 * 1024);
/*      */     }
/*  281 */     int maxAge = DataBinderUtils.getInteger(this.m_binder, "SchemaViewCacheMaxAge", -1);
/*  282 */     if (maxAge != -1)
/*      */     {
/*  284 */       manager.setMaximumAge(maxAge * 1000);
/*      */     }
/*  286 */     getSchemaStats();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getSchemaStats() throws ServiceException, DataException
/*      */   {
/*  292 */     LRUManager manager = (LRUManager)SharedObjects.getObject("globalObjects", "SchemaViewLRUManager");
/*      */ 
/*  294 */     if (DataBinderUtils.getBoolean(this.m_binder, "CheckSchemaCache", false))
/*      */     {
/*  296 */       manager.checkCache("SchemaViewLRUCache", this.m_binder);
/*  297 */       checkViewCache();
/*      */     }
/*  299 */     manager.getCacheProperties("SchemaViewLRUManager", this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkViewCache()
/*      */   {
/*  305 */     SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*      */ 
/*  307 */     DataResultSet summarySet = new DataResultSet(new String[] { "ViewName", "CacheUsage" });
/*      */ 
/*  309 */     DataResultSet drset = new DataResultSet(new String[] { "ViewName", "ObjectDescription" });
/*      */ 
/*  311 */     int totalUsage = 0;
/*  312 */     for (views.first(); views.isRowPresent(); views.next())
/*      */     {
/*  314 */       SchemaViewData data = (SchemaViewData)views.getData();
/*  315 */       Hashtable values = data.getViewValuesTable();
/*  316 */       Enumeration en = values.keys();
/*  317 */       int viewUsage = 0;
/*  318 */       while (en.hasMoreElements())
/*      */       {
/*  320 */         SchemaCacheItem item = (SchemaCacheItem)en.nextElement();
/*  321 */         if (item == null)
/*      */         {
/*  323 */           return;
/*      */         }
/*  325 */         Vector row = new IdcVector();
/*  326 */         row.addElement(data.m_name);
/*  327 */         row.addElement(item.toString());
/*  328 */         int size = item.getSize();
/*  329 */         totalUsage += size;
/*  330 */         viewUsage += size;
/*  331 */         drset.addRow(row);
/*      */       }
/*      */ 
/*  334 */       Vector row = new IdcVector();
/*  335 */       row.addElement(data.m_name);
/*  336 */       row.addElement("" + viewUsage);
/*  337 */       summarySet.addRow(row);
/*      */     }
/*      */ 
/*  340 */     this.m_binder.addResultSet("SchemaViewCacheSummary", summarySet);
/*  341 */     this.m_binder.addResultSet("SchemaViewCacheDetails", drset);
/*  342 */     this.m_binder.putLocal("SchemaViewCache.size", "" + totalUsage);
/*  343 */     this.m_binder.putLocal("SchemaViewCache.itemCount", "" + drset.getNumRows());
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void publishSchema() throws ServiceException
/*      */   {
/*  349 */     boolean userRequest = DataBinderUtils.getBoolean(this.m_binder, "UserPublishingRequest", false);
/*      */ 
/*  351 */     if (userRequest)
/*      */     {
/*  353 */       this.m_schemaManager.resetPublishingTimers();
/*      */     }
/*  355 */     this.m_schemaManager.publish(0L, true, this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteSchemaTable() throws ServiceException, DataException
/*      */   {
/*  361 */     this.m_editHelper.checkTableUse(this.m_binder);
/*      */ 
/*  363 */     this.m_schemaManager.deleteSchemaTable(this.m_workspace, this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void synchronizeSchemaTableDefinition()
/*      */     throws ServiceException, DataException
/*      */   {
/*      */     try
/*      */     {
/*  372 */       String name = this.m_binder.get("schTableName");
/*  373 */       this.m_schemaManager.synchronizeSchemaTableDefinition(this.m_workspace, name);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  378 */       createServiceException(e, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean checkTableExistence(String tableName)
/*      */     throws DataException, ServiceException
/*      */   {
/*  385 */     if (this.m_workspace != null)
/*      */     {
/*  387 */       return WorkspaceUtils.doesTableExist(this.m_workspace, tableName, null);
/*      */     }
/*  389 */     return false;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getSchemaViews() throws ServiceException, DataException
/*      */   {
/*  395 */     this.m_schemaManager.getSchemaData(this.m_binder, "SchemaViewConfig");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getSchemaRelations() throws ServiceException, DataException
/*      */   {
/*  401 */     this.m_schemaManager.getSchemaData(this.m_binder, "SchemaRelationConfig");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getSchemaFields() throws ServiceException, DataException
/*      */   {
/*  407 */     this.m_schemaManager.getSchemaData(this.m_binder, "SchemaFieldConfig");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addSchemaRelation() throws ServiceException, DataException
/*      */   {
/*  413 */     addOrEditSchemaRelation(true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void editSchemaRelation() throws ServiceException, DataException
/*      */   {
/*  419 */     addOrEditSchemaRelation(false);
/*      */   }
/*      */ 
/*      */   public void addOrEditSchemaRelation(boolean isNew) throws ServiceException, DataException
/*      */   {
/*  424 */     String relName = this.m_binder.get("schRelationName");
/*  425 */     SchemaRelationData relData = (SchemaRelationData)this.m_relations.load(relName, !isNew);
/*      */ 
/*  428 */     this.m_editHelper.validateRelationIntegrity(relData, this.m_binder, isNew);
/*      */ 
/*  430 */     this.m_relations.createOrUpdate(relName, this.m_binder, !isNew);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteSchemaRelation() throws ServiceException, DataException
/*      */   {
/*  436 */     this.m_editHelper.checkRelationUse(this.m_binder);
/*      */ 
/*  438 */     String name = this.m_binder.get("schRelationName");
/*  439 */     this.m_relations.delete(name);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getSchemaViewInfo() throws ServiceException, DataException
/*      */   {
/*  445 */     String isNewStr = this.m_binder.getLocal("IsNew");
/*  446 */     boolean isNew = StringUtils.convertToBool(isNewStr, false);
/*  447 */     if (!isNew)
/*      */     {
/*  449 */       String viewName = this.m_binder.get("schViewName");
/*  450 */       SchemaViewData viewData = (SchemaViewData)this.m_views.load(viewName, true);
/*      */ 
/*  453 */       viewData.populateBinder(this.m_binder);
/*      */     }
/*      */ 
/*  456 */     String viewType = this.m_binder.getLocal("schViewType");
/*  457 */     if (!viewType.equals("table")) {
/*      */       return;
/*      */     }
/*      */ 
/*  461 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("ViewLocales");
/*  462 */     if (drset == null)
/*      */     {
/*  464 */       drset = new DataResultSet(SchemaViewData.VIEW_LOCALE_COLUMNS);
/*  465 */       this.m_binder.addResultSet("ViewLocales", drset);
/*      */     }
/*      */ 
/*  469 */     String systemLocale = SharedObjects.getEnvironmentValue("SystemLocale");
/*  470 */     DataResultSet localeSet = SharedObjects.getTable("LocaleConfig");
/*  471 */     String[] columns = { "lcLocaleId", "lcIsEnabled" };
/*  472 */     FieldInfo[] fis = ResultSetUtils.createInfoList(localeSet, columns, true);
/*  473 */     int idIndex = fis[0].m_index;
/*  474 */     int enabledIndex = fis[1].m_index;
/*  475 */     for (localeSet.first(); localeSet.isRowPresent(); localeSet.next())
/*      */     {
/*  477 */       boolean isEnabled = StringUtils.convertToBool(localeSet.getStringValue(enabledIndex), false);
/*  478 */       String id = localeSet.getStringValue(idIndex);
/*  479 */       if (id.equals(systemLocale))
/*      */       {
/*  481 */         isEnabled = true;
/*      */       }
/*  483 */       if (!isEnabled)
/*      */         continue;
/*  485 */       Vector row = drset.findRow(0, id);
/*  486 */       if (row != null)
/*      */         continue;
/*  488 */       row = drset.createEmptyRow();
/*  489 */       row.setElementAt(id, 0);
/*  490 */       drset.addRow(row);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addSchemaView()
/*      */     throws ServiceException, DataException
/*      */   {
/*  500 */     addOrEditSchemaView(true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void editSchemaView() throws ServiceException, DataException
/*      */   {
/*  506 */     addOrEditSchemaView(false);
/*      */   }
/*      */ 
/*      */   public void addOrEditSchemaView(boolean isNew) throws ServiceException, DataException
/*      */   {
/*  511 */     String viewName = this.m_binder.get("schViewName");
/*  512 */     if ((viewName == null) || (viewName.length() == 0))
/*      */     {
/*  514 */       String msg = LocaleUtils.encodeMessage("apValueRequired", null, "schViewName");
/*      */ 
/*  516 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*  522 */     SchemaViewData viewData = (SchemaViewData)this.m_views.load(viewName, !isNew);
/*  523 */     this.m_editHelper.validateViewIntegrity(this.m_binder, viewData, isNew);
/*      */ 
/*  525 */     this.m_views.createOrUpdate(viewName, this.m_binder, !isNew);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteSchemaView() throws ServiceException, DataException
/*      */   {
/*  531 */     String viewName = this.m_binder.get("schViewName");
/*  532 */     if ((viewName.equals("docFormats")) || (viewName.equals("docTypes")))
/*      */     {
/*  535 */       String msg = LocaleUtils.encodeMessage("apSchemaViewIsSystem", null, viewName);
/*      */ 
/*  537 */       createServiceException(null, msg);
/*      */     }
/*  539 */     SchemaViewData viewData = (SchemaViewData)this.m_views.getSchemaData(viewName);
/*      */ 
/*  541 */     if (viewData == null)
/*      */     {
/*  543 */       String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_view", null, viewName);
/*      */ 
/*  545 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/*  548 */     SchemaFieldConfig config = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/*      */ 
/*  550 */     String viewType = viewData.get("schViewType");
/*  551 */     String tableName = viewData.get("schTableName");
/*  552 */     if ((viewType != null) && (viewType.equals("table")) && (tableName != null) && (tableName.equals("OptionsList")))
/*      */     {
/*  558 */       String key = viewData.get("schCriteriaValue0");
/*  559 */       if (key != null)
/*      */       {
/*  561 */         Vector list = config.fieldsUsingView(key);
/*  562 */         if (list.size() > 0)
/*      */         {
/*  564 */           SchemaFieldData fieldData = (SchemaFieldData)config.getData();
/*      */ 
/*  566 */           String msg = LocaleUtils.encodeMessage("apSchOptionsListUsedByField", null, key, fieldData.get("dCaption"));
/*      */ 
/*  569 */           createServiceException(null, msg);
/*      */         }
/*  571 */         this.m_binder.putLocal("dKey", key);
/*  572 */         this.m_binder.removeLocal("OptionListString");
/*  573 */         executeService2("UPDATE_OPTION_LIST");
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  578 */       Vector list = config.fieldsUsingView(viewName);
/*  579 */       if (list.size() > 0)
/*      */       {
/*  581 */         SchemaFieldData fieldData = (SchemaFieldData)config.getData();
/*      */ 
/*  583 */         String msg = LocaleUtils.encodeMessage("apSchViewInUseError", null, viewName, fieldData.get("dCaption"));
/*      */ 
/*  586 */         createServiceException(null, msg);
/*      */       }
/*      */     }
/*      */ 
/*  590 */     this.m_views.delete(viewName);
/*      */   }
/*      */ 
/*      */   public void executeService2(String command)
/*      */     throws ServiceException, DataException
/*      */   {
/*  598 */     executeServiceSimple(command);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getViewEditInfo()
/*      */     throws ServiceException, DataException
/*      */   {
/*  608 */     getSchemaViewInfo(true, this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getViewValues()
/*      */     throws ServiceException, DataException
/*      */   {
/*  615 */     getSchemaViewInfo(false, this.m_binder);
/*      */   }
/*      */ 
/*      */   protected void getSchemaViewInfo(boolean isFieldInfoOnly, DataBinder binder)
/*      */     throws ServiceException, DataException
/*      */   {
/*  622 */     String viewName = binder.get("schViewName");
/*  623 */     SchemaViewData viewData = (SchemaViewData)this.m_views.load(viewName, true);
/*      */ 
/*  626 */     retrieveViewValues(viewName, viewData, isFieldInfoOnly, binder);
/*      */   }
/*      */ 
/*      */   protected void retrieveViewValues(String viewName, SchemaViewData viewData, boolean isFieldInfoOnly, DataBinder binder)
/*      */     throws DataException, ServiceException
/*      */   {
/*  633 */     String tableName = viewData.get("schTableName");
/*  634 */     String viewType = viewData.get("schViewType");
/*      */ 
/*  636 */     if (!viewType.equals("table"))
/*      */     {
/*  639 */       String msg = LocaleUtils.encodeMessage("csSchViewTypeMismatch_table", null, viewData.m_name);
/*      */ 
/*  641 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  645 */     String[] pkColumns = this.m_workspace.getPrimaryKeys(tableName);
/*  646 */     Vector v = new IdcVector();
/*  647 */     for (int i = 0; i < pkColumns.length; ++i)
/*      */     {
/*  649 */       v.addElement(pkColumns[i]);
/*      */     }
/*  651 */     String str = StringUtils.createString(v, ',', '^');
/*  652 */     binder.putLocal("PrimaryColumns", str);
/*      */ 
/*  655 */     viewData.populateBinder(binder);
/*  656 */     StringBuffer sql = new StringBuffer();
/*  657 */     String distinctColumn = binder.getLocal("distinctColumn");
/*  658 */     if (distinctColumn != null)
/*      */     {
/*  660 */       sql.append("SELECT DISTINCT ");
/*  661 */       sql.append(distinctColumn);
/*  662 */       sql.append(" FROM " + tableName);
/*      */     }
/*      */     else
/*      */     {
/*  666 */       sql.append("SELECT * FROM " + tableName);
/*      */     }
/*      */ 
/*  670 */     ResultSet rset = this.m_workspace.createResultSetSQL(sql.toString());
/*  671 */     DataResultSet infoSet = new DataResultSet();
/*  672 */     infoSet.copyFieldInfo(rset);
/*      */ 
/*  675 */     appendCompleteWhereClause(sql, viewData, infoSet, tableName, binder);
/*      */ 
/*  678 */     this.m_schemaUtils.appendBaseOrderClause(sql, viewData, false, tableName);
/*      */ 
/*  680 */     computeViewResultSet(tableName, sql.toString(), viewData, isFieldInfoOnly, binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getSchemaViewFragment() throws DataException, ServiceException
/*      */   {
/*  686 */     boolean getParentRelations = DataBinderUtils.getBoolean(this.m_binder, "GetParentRelations", false);
/*  687 */     boolean getParentViews = DataBinderUtils.getBoolean(this.m_binder, "GetParentViews", false);
/*  688 */     boolean getChildViews = DataBinderUtils.getBoolean(this.m_binder, "GetChildViews", false);
/*  689 */     if ((getParentRelations) || (getParentViews) || (getChildViews))
/*      */     {
/*  691 */       getParentOrChildInfo();
/*  692 */       return;
/*      */     }
/*      */ 
/*  695 */     String viewName = this.m_binder.get("schViewName");
/*  696 */     String relationName = this.m_binder.getAllowMissing("schRelationName");
/*  697 */     String[] parentValues = null;
/*      */ 
/*  699 */     Map args = new HashMap();
/*  700 */     String fieldName = this.m_binder.getAllowMissing("schFieldName0");
/*  701 */     if (fieldName != null)
/*      */     {
/*  703 */       List fieldList = new ArrayList();
/*  704 */       List valueList = new ArrayList();
/*  705 */       for (int i = 0; ; ++i)
/*      */       {
/*  707 */         fieldName = this.m_binder.getAllowMissing("schFieldName" + i);
/*  708 */         if (fieldName == null) {
/*      */           break;
/*      */         }
/*      */ 
/*      */         try
/*      */         {
/*  714 */           fieldName = StringUtils.decodeJavascriptFilename(fieldName);
/*  715 */           fieldList.add(fieldName);
/*  716 */           String value = this.m_binder.get("schFieldValue" + i);
/*  717 */           value = StringUtils.decodeJavascriptFilename(value);
/*  718 */           valueList.add(value);
/*      */         }
/*      */         catch (CharConversionException e)
/*      */         {
/*  723 */           throw new ServiceException(e);
/*      */         }
/*      */       }
/*  726 */       String[] fieldNames = new String[fieldList.size()];
/*  727 */       String[] fieldValues = new String[fieldList.size()];
/*  728 */       fieldNames = (String[])(String[])fieldList.toArray(fieldNames);
/*  729 */       fieldValues = (String[])(String[])valueList.toArray(fieldValues);
/*  730 */       args.put("fieldNames", fieldNames);
/*  731 */       args.put("fieldValues", fieldValues);
/*      */     }
/*      */ 
/*  736 */     String keyType = this.m_binder.getAllowMissing("schViewKeyType");
/*  737 */     if ((keyType != null) && (keyType.startsWith("relation:")))
/*      */     {
/*  739 */       relationName = keyType.substring("relation:".length());
/*      */     }
/*      */ 
/*  742 */     if (relationName != null)
/*      */     {
/*  744 */       List parentList = new ArrayList();
/*      */ 
/*  746 */       for (int i = 0; ; ++i)
/*      */       {
/*  748 */         String parentValue = this.m_binder.getAllowMissing("schParentValue" + i);
/*  749 */         if (parentValue == null) {
/*      */           break;
/*      */         }
/*      */ 
/*  753 */         parentList.add(parentValue);
/*      */       }
/*  755 */       if (parentList.size() == 0)
/*      */       {
/*  758 */         for (int i = 0; ; ++i)
/*      */         {
/*  760 */           String parentValue = this.m_binder.getAllowMissing("schViewKeyValue" + i);
/*  761 */           if (parentValue == null) {
/*      */             break;
/*      */           }
/*      */ 
/*  765 */           parentList.add(parentValue);
/*      */         }
/*      */       }
/*  768 */       if (parentList.size() == 0)
/*      */       {
/*  770 */         String parentValue = this.m_binder.getAllowMissing("schParentValue");
/*  771 */         if (parentValue != null)
/*      */         {
/*      */           try
/*      */           {
/*  775 */             String tmp = StringUtils.decodeJavascriptFilename(parentValue);
/*  776 */             parentList.add(tmp);
/*      */           }
/*      */           catch (CharConversionException e)
/*      */           {
/*  780 */             throw new ServiceException(e);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  785 */       if (parentList.size() == 0)
/*      */       {
/*  788 */         this.m_binder.get("schParentValue0");
/*      */       }
/*      */ 
/*  791 */       parentValues = (String[])(String[])parentList.toArray(new String[0]);
/*      */     }
/*      */ 
/*  794 */     String privString = (String)getCachedObject("SchemaViewPrivilege");
/*  795 */     if (privString != null)
/*      */     {
/*  797 */       args.put("privilege", privString);
/*      */     }
/*      */ 
/*  800 */     String page = this.m_schemaUtils.prepareSendSchemaViewFragment(this, this.m_binder, this.m_schemaHelper, viewName, relationName, parentValues, args);
/*      */ 
/*  802 */     this.m_serviceData.m_htmlPage = page;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getParentOrChildInfo() throws DataException, ServiceException
/*      */   {
/*  808 */     boolean getParentRelations = DataBinderUtils.getBoolean(this.m_binder, "GetParentRelations", false);
/*      */ 
/*  810 */     boolean getParentViews = DataBinderUtils.getBoolean(this.m_binder, "GetParentViews", false);
/*      */ 
/*  812 */     boolean getChildViews = DataBinderUtils.getBoolean(this.m_binder, "GetChildViews", false);
/*      */ 
/*  815 */     SchemaViewConfig views = this.m_schemaHelper.m_views;
/*  816 */     String relationName = null;
/*  817 */     SchemaRelationData[] relations = null;
/*      */ 
/*  819 */     if (getParentRelations)
/*      */     {
/*  821 */       String viewName = this.m_binder.get("schViewName");
/*  822 */       SchemaViewData view = (SchemaViewData)this.m_schemaHelper.requireSchemaData("SchemaViewConfig", viewName);
/*      */ 
/*  825 */       SchemaLoader viewLoader = views.findLoader(view, null, null);
/*  826 */       relations = viewLoader.getParentRelations(view);
/*      */ 
/*  828 */       DataResultSet drset = new DataResultSet(new String[] { "RelationName" });
/*      */ 
/*  830 */       for (int i = 0; i < relations.length; ++i)
/*      */       {
/*  832 */         Vector row = new IdcVector();
/*  833 */         row.addElement(relations[i].m_name);
/*  834 */         drset.addRow(row);
/*      */       }
/*  836 */       this.m_binder.addResultSet("ParentRelations", drset);
/*      */     }
/*      */ 
/*  839 */     if ((!getParentViews) && (!getChildViews))
/*      */       return;
/*  841 */     if ((relations == null) || (getChildViews))
/*      */     {
/*  843 */       relationName = this.m_binder.get("schRelationName");
/*  844 */       SchemaData data = this.m_schemaHelper.requireSchemaData("SchemaRelationConfig", relationName);
/*      */ 
/*  846 */       relations = new SchemaRelationData[] { (SchemaRelationData)data };
/*      */     }
/*      */ 
/*  850 */     for (int i = 0; i < relations.length; ++i)
/*      */     {
/*  852 */       DataResultSet drset = new DataResultSet(new String[] { "ViewName" });
/*      */ 
/*  855 */       Vector loaders = views.getLoaders();
/*  856 */       ArrayList viewsList = new ArrayList();
/*      */       String setPrefix;
/*      */       String setPrefix;
/*  857 */       if (getChildViews)
/*      */       {
/*  859 */         setPrefix = "ChildViews";
/*      */       }
/*      */       else
/*      */       {
/*  863 */         setPrefix = "ParentViews";
/*      */       }
/*  865 */       for (int j = 0; j < loaders.size(); ++j)
/*      */       {
/*  867 */         SchemaLoader viewLoader = (SchemaLoader)loaders.elementAt(j);
/*      */         SchemaViewData[] viewsArray;
/*      */         SchemaViewData[] viewsArray;
/*  869 */         if (getChildViews)
/*      */         {
/*  871 */           viewsArray = viewLoader.getChildViews(relations[i]);
/*      */         }
/*      */         else
/*      */         {
/*  875 */           viewsArray = viewLoader.getParentViews(relations[i]);
/*      */         }
/*      */ 
/*  878 */         if (viewsArray == null)
/*      */           continue;
/*  880 */         viewsList.addAll(Arrays.asList(viewsArray));
/*      */       }
/*      */ 
/*  883 */       for (int j = 0; j < viewsList.size(); ++j)
/*      */       {
/*  885 */         Vector row = new IdcVector();
/*  886 */         SchemaViewData view = (SchemaViewData)viewsList.get(j);
/*      */ 
/*  888 */         row.addElement(view.m_name);
/*  889 */         drset.addRow(row);
/*      */       }
/*  891 */       if ((i == 0) && (relations.length == 1))
/*      */       {
/*  893 */         this.m_binder.addResultSet(setPrefix, drset);
/*      */       }
/*      */       else
/*      */       {
/*  897 */         this.m_binder.addResultSet(setPrefix + "_" + relations[i].m_name, drset);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void appendCompleteWhereClause(StringBuffer sql, SchemaViewData viewData, DataResultSet infoSet, String tableName, DataBinder binder)
/*      */     throws DataException
/*      */   {
/*  909 */     boolean inClause = this.m_schemaUtils.appendBaseWhereClause(sql, viewData, false, null, infoSet);
/*      */ 
/*  913 */     String paramClmn = binder.getLocal("paramClmn");
/*  914 */     if ((paramClmn != null) && (paramClmn.length() > 0))
/*      */     {
/*  916 */       FieldInfo info = new FieldInfo();
/*  917 */       boolean exists = infoSet.getFieldInfo(paramClmn, info);
/*  918 */       if (!exists)
/*      */       {
/*  920 */         String msg = LocaleUtils.encodeMessage("csSchUnableToBuildWhereClause", null, viewData.m_name);
/*      */ 
/*  923 */         msg = LocaleUtils.encodeMessage("csSchWhereClauseColumnMissing", msg, tableName, paramClmn);
/*      */ 
/*  926 */         throw new DataException(msg);
/*      */       }
/*  928 */       String val = binder.getLocal("paramValue");
/*  929 */       QueryUtils.createAndAppendSubclause(sql, info, val, "=", " WHERE ", " AND ", inClause);
/*      */     }
/*      */ 
/*  932 */     String filterWhereClause = binder.getLocal("whereClause");
/*  933 */     if ((filterWhereClause == null) || (filterWhereClause.length() <= 0))
/*      */       return;
/*  935 */     QueryUtils.appendSubclause(sql, inClause, " WHERE ", " AND ", filterWhereClause);
/*      */   }
/*      */ 
/*      */   protected void computeViewResultSet(String tableName, String sql, SchemaViewData viewData, boolean isFieldInfoOnly, DataBinder binder)
/*      */     throws DataException, ServiceException
/*      */   {
/*  942 */     SchemaTableData tableData = (SchemaTableData)this.m_tables.load(tableName, true);
/*      */ 
/*  944 */     if (tableData == null)
/*      */     {
/*  946 */       String msg = LocaleUtils.encodeMessage("csSchMissingDefinition_table", null, tableName);
/*      */ 
/*  948 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/*  951 */     ResultSet rset = this.m_workspace.createResultSetSQL(sql);
/*      */ 
/*  954 */     DataResultSet drset = new DataResultSet();
/*  955 */     drset.copyFieldInfo(rset);
/*      */ 
/*  961 */     Hashtable fieldMap = new Hashtable();
/*  962 */     Vector viewColumnFields = null;
/*  963 */     if (binder.getLocal("distinctColumn") == null)
/*      */     {
/*  965 */       this.m_schemaUtils.determineAndPopulateSpecialFields(viewData, drset, fieldMap, null);
/*  966 */       this.m_schemaUtils.determineAndPopulateTimeStampFields(viewData, tableData, drset, fieldMap, null);
/*  967 */       this.m_schemaUtils.determineAndPopulateCriteriaFields(viewData, drset, fieldMap, null);
/*  968 */       viewColumnFields = this.m_schemaUtils.buildViewFields(viewData, drset, fieldMap);
/*      */     }
/*      */ 
/*  971 */     if (viewColumnFields != null)
/*      */     {
/*  973 */       drset = new DataResultSet();
/*  974 */       drset.mergeFieldsWithFlags(viewColumnFields, 2);
/*      */     }
/*      */     else
/*      */     {
/*  978 */       int len = fieldMap.size();
/*  979 */       String[] remFields = new String[len];
/*  980 */       Enumeration en = fieldMap.elements();
/*  981 */       for (int i = 0; en.hasMoreElements(); ++i)
/*      */       {
/*  983 */         FieldInfo info = (FieldInfo)en.nextElement();
/*  984 */         remFields[i] = info.m_name;
/*      */       }
/*  986 */       drset.removeFields(remFields);
/*      */     }
/*      */ 
/*  989 */     if (!isFieldInfoOnly)
/*      */     {
/*  991 */       int maxRows = SharedObjects.getEnvironmentInt("MaxViewEditRows", 10000);
/*  992 */       drset.mergeEx(null, rset, false, maxRows);
/*      */ 
/*  994 */       boolean isCopyAborted = drset.isCopyAborted();
/*  995 */       binder.putLocal("copyAborted", "" + isCopyAborted);
/*      */     }
/*  997 */     binder.addResultSet(tableName, drset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void editSchemaViewValues()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1005 */     String viewName = this.m_binder.get("schViewName");
/* 1006 */     SchemaViewData viewData = (SchemaViewData)this.m_views.load(viewName, true);
/*      */ 
/* 1008 */     if (viewData == null)
/*      */     {
/* 1010 */       String msg = LocaleUtils.encodeMessage("csSchMissingDefinition_view", null, viewName);
/*      */ 
/* 1012 */       createServiceException(null, msg);
/*      */     }
/* 1014 */     this.m_binder.putLocal("schViewName", viewData.m_name);
/*      */ 
/* 1016 */     String tableName = viewData.get("schTableName");
/* 1017 */     SchemaTableData tableData = (SchemaTableData)this.m_tables.load(tableName, true);
/*      */ 
/* 1019 */     if (tableData == null)
/*      */     {
/* 1021 */       String msg = LocaleUtils.encodeMessage("csSchMissingDefinition_table", null, tableName);
/*      */ 
/* 1023 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1029 */     DataBinder workBinder = new DataBinder();
/*      */ 
/* 1034 */     FieldInfo[] origInfos = this.m_workspace.getColumnList(tableName);
/* 1035 */     Vector origInfoVector = new IdcVector();
/* 1036 */     origInfoVector.addAll(Arrays.asList(origInfos));
/* 1037 */     DataResultSet prepSet = new DataResultSet();
/* 1038 */     prepSet.mergeFieldsWithFlags(origInfoVector, 0);
/*      */ 
/* 1042 */     Hashtable fieldMap = new Hashtable();
/* 1043 */     this.m_schemaUtils.determineAndPopulateSpecialFields(viewData, prepSet, fieldMap, workBinder);
/* 1044 */     this.m_schemaUtils.determineAndPopulateTimeStampFields(viewData, tableData, prepSet, fieldMap, workBinder);
/* 1045 */     this.m_schemaUtils.determineAndPopulateCriteriaFields(viewData, prepSet, fieldMap, workBinder);
/* 1046 */     this.m_schemaUtils.buildViewFields(viewData, prepSet, fieldMap);
/*      */ 
/* 1049 */     String action = this.m_binder.get("editViewValueAction");
/* 1050 */     DataResultSet changedSet = (DataResultSet)this.m_binder.getResultSet(tableName);
/* 1051 */     DataResultSet deleteSet = null;
/*      */ 
/* 1053 */     boolean isError = true;
/*      */     try
/*      */     {
/* 1058 */       this.m_schemaUtils.determinePKColumnInfos(tableName, this.m_workspace, prepSet);
/*      */ 
/* 1060 */       this.m_workspace.beginTranEx(0x4 | 0x1);
/*      */ 
/* 1064 */       String internalClmn = viewData.get("schInternalColumn");
/* 1065 */       if (action.equals("batch"))
/*      */       {
/* 1067 */         StringBuffer sqlBuff = null;
/* 1068 */         boolean isDoCheck = SharedObjects.getEnvValueAsBoolean("SchemaBatchEditCheck", true);
/* 1069 */         if (isDoCheck)
/*      */         {
/* 1071 */           sqlBuff = new StringBuffer("SELECT * FROM ");
/*      */         }
/*      */         else
/*      */         {
/* 1075 */           sqlBuff = new StringBuffer("DELETE FROM ");
/*      */         }
/* 1077 */         sqlBuff.append(tableName);
/* 1078 */         appendCompleteWhereClause(sqlBuff, viewData, prepSet, tableName, this.m_binder);
/*      */ 
/* 1080 */         if (isDoCheck)
/*      */         {
/* 1083 */           ResultSet currentSet = this.m_workspace.createResultSetSQL(sqlBuff.toString());
/*      */ 
/* 1086 */           deleteSet = new DataResultSet();
/* 1087 */           deleteSet.copy(currentSet);
/* 1088 */           deleteSet.mergeDelete(internalClmn, changedSet, true);
/*      */         }
/*      */         else
/*      */         {
/* 1096 */           this.m_workspace.executeSQL(sqlBuff.toString());
/* 1097 */           doViewValueEdit("batch", viewData, workBinder, changedSet, prepSet);
/* 1098 */           changedSet = null;
/*      */         }
/*      */       }
/* 1101 */       else if (action.equals("jsbatch"))
/*      */       {
/* 1103 */         List clientColumnNames = DataBinderUtils.getArrayList(this.m_binder, "clientViewColumns", ',', '^');
/*      */ 
/* 1105 */         String relationName = this.m_binder.getLocal("schRelationName");
/* 1106 */         String parentValue = this.m_binder.getLocal("schParentValue");
/* 1107 */         int parentValueColumnIndex = -1;
/* 1108 */         if ((relationName != null) && (parentValue != null) && (relationName.length() > 0) && (parentValue.length() > 0))
/*      */         {
/* 1111 */           String parentColumn = this.m_schemaHelper.getMyTableColumn(viewData.m_name, relationName);
/*      */ 
/* 1113 */           clientColumnNames.add(parentColumn);
/* 1114 */           parentValueColumnIndex = clientColumnNames.size() - 1;
/*      */         }
/* 1116 */         int internalColumnIndex = clientColumnNames.indexOf(internalClmn);
/* 1117 */         int lastRowIndex = DataBinderUtils.getInteger(this.m_binder, "lastRowIndex", -1);
/*      */ 
/* 1119 */         int clientColumnNamesSize = clientColumnNames.size();
/* 1120 */         DataResultSet newSet = new DataResultSet(clientColumnNames);
/* 1121 */         changedSet = new DataResultSet(clientColumnNames);
/* 1122 */         deleteSet = new DataResultSet(clientColumnNames);
/* 1123 */         for (int i = 0; i <= lastRowIndex; ++i)
/*      */         {
/* 1125 */           Vector row = new IdcVector();
/* 1126 */           String flags = this.m_binder.getLocal("f " + i);
/* 1127 */           this.m_binder.removeLocal("f " + i);
/* 1128 */           boolean isSkip = false;
/* 1129 */           for (int j = 0; j < clientColumnNamesSize; ++j)
/*      */           {
/* 1131 */             String key = "d " + i + " " + j;
/* 1132 */             String value = this.m_binder.getLocal(key);
/* 1133 */             this.m_binder.removeLocal(key);
/* 1134 */             if (j == parentValueColumnIndex)
/*      */             {
/* 1136 */               value = parentValue;
/*      */             }
/* 1138 */             if (value == null)
/*      */             {
/* 1140 */               value = "";
/*      */             }
/* 1142 */             if ((j == internalColumnIndex) && (value.length() == 0))
/*      */             {
/* 1144 */               isSkip = true;
/*      */             }
/*      */             else
/* 1147 */               row.addElement(value);
/*      */           }
/* 1149 */           if (isSkip) {
/*      */             continue;
/*      */           }
/*      */ 
/* 1153 */           if ((flags != null) && (flags.indexOf("new") >= 0))
/*      */           {
/* 1155 */             newSet.addRow(row);
/*      */           }
/* 1157 */           else if ((flags != null) && (flags.indexOf("delete") >= 0))
/*      */           {
/* 1159 */             deleteSet.addRow(row);
/*      */           }
/*      */           else
/*      */           {
/* 1163 */             changedSet.addRow(row);
/*      */           }
/*      */         }
/*      */ 
/* 1167 */         doViewValueEdit("add", viewData, workBinder, newSet, prepSet);
/* 1168 */         doViewValueEdit("edit", viewData, workBinder, changedSet, prepSet);
/* 1169 */         doViewValueEdit("delete", viewData, workBinder, deleteSet, prepSet);
/* 1170 */         tableData.notifyChanged(deleteSet, newSet);
/* 1171 */         tableData.notifyChanged(null, changedSet);
/* 1172 */         changedSet = null;
/*      */       }
/* 1174 */       else if (action.equals("delete"))
/*      */       {
/* 1176 */         deleteSet = changedSet;
/* 1177 */         changedSet = null;
/*      */       }
/*      */ 
/* 1180 */       if (deleteSet != null)
/*      */       {
/* 1182 */         doViewValueEdit("delete", viewData, workBinder, deleteSet, prepSet);
/*      */       }
/* 1184 */       if (changedSet != null)
/*      */       {
/* 1186 */         doViewValueEdit(action, viewData, workBinder, changedSet, prepSet);
/*      */       }
/* 1188 */       isError = false;
/*      */     }
/*      */     finally
/*      */     {
/* 1192 */       if (isError)
/*      */       {
/* 1194 */         this.m_workspace.rollbackTran();
/*      */       }
/*      */       else
/*      */       {
/* 1198 */         this.m_workspace.commitTran();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1203 */     tableData.notifyChanged(deleteSet, changedSet);
/* 1204 */     if ((tableName == null) || (!tableName.equals("OptionsList")))
/*      */       return;
/* 1206 */     SubjectManager.forceRefresh("metaoptlists");
/*      */   }
/*      */ 
/*      */   protected void doViewValueEdit(String action, SchemaViewData viewData, DataBinder workBinder, DataResultSet changedSet, DataResultSet prepSet)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1214 */     String tableName = viewData.get("schTableName");
/* 1215 */     String internalClmn = viewData.get("schInternalColumn");
/* 1216 */     workBinder.addResultSet(tableName, changedSet);
/*      */ 
/* 1218 */     if (!SecurityUtils.isUserOfRole(this.m_userData, "admin"))
/*      */     {
/* 1220 */       int requiredAuth = 255;
/* 1221 */       if (action.equals("add"))
/*      */       {
/* 1223 */         requiredAuth = 2;
/*      */       }
/*      */       else
/*      */       {
/* 1227 */         requiredAuth = 4;
/*      */       }
/* 1229 */       String msg = LocaleUtils.encodeMessage("csSystemAccessDenied", null, viewData.m_name);
/*      */ 
/* 1231 */       SchemaSecurityFilter filter = this.m_schemaUtils.getSecurityImplementor(viewData);
/*      */ 
/* 1233 */       filter.init(this);
/* 1234 */       this.m_schemaHelper.validateViewModificationAuthorization(msg, requiredAuth, viewData, filter, changedSet);
/*      */     }
/*      */ 
/* 1238 */     boolean isDoCheck = SharedObjects.getEnvValueAsBoolean("SchemaBatchEditCheck", true);
/* 1239 */     for (changedSet.first(); changedSet.isRowPresent(); changedSet.next())
/*      */     {
/* 1242 */       String[] sqlClauses = this.m_schemaUtils.createSql(viewData, prepSet, tableName, workBinder);
/*      */ 
/* 1246 */       ResultSet rset = this.m_workspace.createResultSetSQL(sqlClauses[0]);
/* 1247 */       if (rset.isRowPresent())
/*      */       {
/* 1249 */         if (action.equals("delete"))
/*      */         {
/* 1252 */           this.m_workspace.executeSQL(sqlClauses[3]);
/*      */ 
/* 1255 */           this.m_schemaUtils.insertIntoDeleteTable(tableName, workBinder, prepSet, this.m_workspace);
/*      */         }
/* 1257 */         else if ((action.equals("edit")) || ((isDoCheck) && (action.equals("batch"))))
/*      */         {
/* 1260 */           this.m_workspace.executeSQL(sqlClauses[1]);
/*      */         }
/* 1262 */         else if (!isDoCheck)
/*      */         {
/* 1265 */           this.m_workspace.executeSQL(sqlClauses[2]);
/*      */         }
/*      */         else
/*      */         {
/* 1270 */           String val = workBinder.get(internalClmn);
/* 1271 */           String msg = LocaleUtils.encodeMessage("csSchCantAddViewValuePrimaryKeyViolation", null, val, tableName);
/*      */ 
/* 1274 */           createServiceExceptionEx(null, msg, -64);
/*      */         }
/*      */ 
/*      */       }
/* 1279 */       else if ((action.equals("add")) || (action.equals("batch")))
/*      */       {
/* 1282 */         this.m_workspace.executeSQL(sqlClauses[2]);
/*      */       } else {
/* 1284 */         if (!action.equals("edit")) {
/*      */           continue;
/*      */         }
/* 1287 */         String val = workBinder.get(internalClmn);
/* 1288 */         String msg = LocaleUtils.encodeMessage("csSchCantEditMissingViewValue", null, val);
/* 1289 */         createServiceExceptionEx(null, msg, -64);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkViewNode()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1300 */     String nodeAction = this.m_binder.get("editViewValueAction");
/* 1301 */     if (nodeAction.equals("add"))
/*      */     {
/* 1303 */       return;
/*      */     }
/*      */ 
/* 1306 */     boolean isEdit = nodeAction.equals("edit");
/*      */ 
/* 1311 */     SchemaHelper schHelper = new SchemaHelper();
/* 1312 */     schHelper.computeMaps();
/*      */ 
/* 1314 */     String viewName = this.m_binder.get("schViewName");
/* 1315 */     SchemaViewData viewData = schHelper.getView(viewName);
/*      */ 
/* 1317 */     String tableName = viewData.get("schTableName");
/* 1318 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(tableName);
/* 1319 */     DataResultSet oldSet = (DataResultSet)this.m_binder.getResultSet("Old" + tableName);
/*      */ 
/* 1321 */     Vector views = schHelper.computeNamedViews(tableName);
/* 1322 */     int size = views.size();
/* 1323 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1325 */       NamedRelationship namedRel = (NamedRelationship)views.elementAt(i);
/* 1326 */       String internalClmn = namedRel.m_view.get("schInternalColumn");
/* 1327 */       String val = ResultSetUtils.getValue(drset, internalClmn);
/*      */ 
/* 1332 */       boolean isChildCheck = nodeAction.startsWith("delete");
/*      */ 
/* 1334 */       if (isEdit)
/*      */       {
/* 1336 */         String oldVal = ResultSetUtils.getValue(oldSet, internalClmn);
/* 1337 */         isChildCheck = !val.equals(oldVal);
/*      */       }
/*      */ 
/* 1340 */       if ((!isChildCheck) || ((namedRel.m_children == null) && (!namedRel.m_isTree)))
/*      */         continue;
/* 1342 */       Vector children = null;
/* 1343 */       if (namedRel.m_isTree)
/*      */       {
/* 1345 */         children = namedRel.m_tree;
/*      */       }
/*      */       else
/*      */       {
/* 1349 */         children = namedRel.m_children;
/*      */       }
/*      */ 
/* 1352 */       int num = children.size();
/* 1353 */       Properties props = this.m_binder.getLocalData();
/* 1354 */       for (int j = 0; j < num; ++j)
/*      */       {
/* 1356 */         DataBinder workBinder = new DataBinder();
/* 1357 */         workBinder.setLocalData((Properties)props.clone());
/*      */ 
/* 1359 */         NamedRelationship childRel = null;
/* 1360 */         Object obj = children.elementAt(j);
/* 1361 */         if (obj instanceof SchemaFieldData)
/*      */         {
/* 1363 */           SchemaFieldData field = (SchemaFieldData)obj;
/* 1364 */           childRel = schHelper.getNamedRelationship(field);
/*      */         }
/* 1366 */         else if (obj instanceof NamedRelationship)
/*      */         {
/* 1368 */           childRel = (NamedRelationship)obj;
/*      */         }
/*      */ 
/* 1371 */         SchemaViewData childViewData = childRel.m_view;
/* 1372 */         String childViewName = childViewData.get("schViewName");
/*      */ 
/* 1374 */         String table2 = childRel.m_relation.get("schTable2Table");
/* 1375 */         String clmn2 = childRel.m_relation.get("schTable2Column");
/* 1376 */         if (table2 == null) continue; if (clmn2 == null)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1381 */         workBinder.putLocal("paramClmn", clmn2);
/*      */ 
/* 1386 */         String clmn1 = childRel.m_relation.get("schTable1Column");
/* 1387 */         if (!clmn1.equals(internalClmn))
/*      */         {
/* 1389 */           val = ResultSetUtils.getValue(drset, clmn1);
/*      */         }
/* 1391 */         workBinder.putLocal("paramValue", val);
/* 1392 */         retrieveViewValues(childViewName, childViewData, false, workBinder);
/*      */ 
/* 1394 */         DataResultSet childValues = (DataResultSet)workBinder.getResultSet(table2);
/* 1395 */         if (childValues.isEmpty())
/*      */           continue;
/* 1397 */         String errMsg = LocaleUtils.encodeMessage("csSchNodeHasChildren", null, childViewName, val);
/*      */ 
/* 1399 */         createServiceExceptionEx(null, errMsg, -64);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1408 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102338 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SchemaService
 * JD-Core Version:    0.5.4
 */