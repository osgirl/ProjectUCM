/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.schema.SchemaInitUtils;
/*      */ import intradoc.server.schema.SchemaManager;
/*      */ import intradoc.server.schema.SchemaStorage;
/*      */ import intradoc.server.schema.ServerSchemaManager;
/*      */ import intradoc.shared.CommonSearchConfig;
/*      */ import intradoc.shared.IndexerCollectionData;
/*      */ import intradoc.shared.MetaFieldData;
/*      */ import intradoc.shared.MetaFieldUtils;
/*      */ import intradoc.shared.SearchFieldInfo;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.schema.SchemaData;
/*      */ import intradoc.shared.schema.SchemaEditHelper;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class MetaService extends Service
/*      */ {
/*      */   public void createHandlersForService()
/*      */     throws ServiceException, DataException
/*      */   {
/*   48 */     super.createHandlersForService();
/*   49 */     createHandlers("MetaService");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   @IdcServiceAction
/*      */   public void updateUserMeta()
/*      */     throws DataException, ServiceException
/*      */   {
/*   59 */     SystemUtils.reportDeprecatedUsage("MetaService.updateUserMeta()");
/*   60 */     String pageDir = DirectoryLocator.getSystemBaseDirectory("data") + "users/config/";
/*      */ 
/*   62 */     DataResultSet drset = SharedObjects.getTable("UserMetaDefinition");
/*   63 */     String[] keys = { "umdName", "umdType", "umdCaption", "umdIsOptionList", "umdOptionListType", "umdOptionListKey", "umdIsAdminEdit", "umdIsViewOnly", "umdOverrideBitFlag" };
/*      */ 
/*   65 */     FieldInfo[] finfo = ResultSetUtils.createInfoList(drset, keys, true);
/*   66 */     String action = this.m_binder.getLocal("action");
/*      */ 
/*   68 */     if (action == null)
/*      */     {
/*   70 */       return;
/*      */     }
/*      */ 
/*   73 */     if (action.equals("ADD"))
/*      */     {
/*   75 */       validateFieldName();
/*   76 */       Vector row = drset.createEmptyRow();
/*   77 */       for (int i = 0; i < finfo.length; ++i)
/*      */       {
/*   79 */         row.setElementAt(this.m_binder.getLocal(finfo[i].m_name), finfo[i].m_index);
/*      */       }
/*   81 */       drset.addRow(row);
/*      */     }
/*   83 */     else if (action.equals("EDIT"))
/*      */     {
/*   85 */       Vector row = drset.findRow(finfo[0].m_index, this.m_binder.getLocal("umdName"));
/*   86 */       int r = drset.getCurrentRow();
/*      */ 
/*   88 */       if (row != null)
/*      */       {
/*   90 */         for (int i = 0; i < finfo.length; ++i)
/*      */         {
/*   92 */           row.setElementAt(this.m_binder.getLocal(finfo[i].m_name), finfo[i].m_index);
/*      */         }
/*   94 */         drset.setRowValues(row, r);
/*      */       }
/*      */     }
/*   97 */     else if (action.equals("DELETE"))
/*      */     {
/*  100 */       if (drset.findRow(finfo[0].m_index, this.m_binder.getLocal("umdName")) != null)
/*      */       {
/*  102 */         drset.deleteCurrentRow();
/*      */       }
/*      */     }
/*  105 */     else if (action.equals("MOVE_UP"))
/*      */     {
/*  107 */       String row = this.m_binder.getLocal("row");
/*  108 */       int r = Integer.parseInt(row);
/*      */ 
/*  110 */       Vector currentRow = drset.getRowValues(r);
/*  111 */       Vector prevRow = drset.getRowValues(r - 1);
/*      */ 
/*  114 */       drset.setRowValues(prevRow, r);
/*  115 */       drset.setRowValues(currentRow, r - 1);
/*      */     }
/*  118 */     else if (action.equals("MOVE_DOWN"))
/*      */     {
/*  120 */       String row = this.m_binder.getLocal("row");
/*  121 */       int r = Integer.parseInt(row);
/*      */ 
/*  123 */       Vector currentRow = drset.getRowValues(r);
/*  124 */       Vector nextRow = drset.getRowValues(r + 1);
/*      */ 
/*  127 */       drset.setRowValues(nextRow, r);
/*  128 */       drset.setRowValues(currentRow, r + 1);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  134 */       DataBinder binder = new DataBinder();
/*      */ 
/*  138 */       DataResultSet temp = new DataResultSet();
/*  139 */       String[] rFields = { "isCustom" };
/*      */ 
/*  141 */       temp.copy(drset, 0);
/*  142 */       temp.removeFields(rFields);
/*      */ 
/*  144 */       FileUtils.reserveDirectory(pageDir);
/*  145 */       binder.addResultSet("UserMetaDefinition", temp);
/*  146 */       ResourceUtils.serializeDataBinder(pageDir, "usermeta.hda", binder, true, true);
/*      */     }
/*      */     finally
/*      */     {
/*  150 */       FileUtils.releaseDirectory(pageDir);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void validateFieldName() throws ServiceException
/*      */   {
/*  156 */     String reservedNameStr = this.m_binder.getEnvironmentValue("ReservedMetaFieldNames");
/*  157 */     if ((reservedNameStr == null) || (reservedNameStr.trim().length() == 0))
/*      */     {
/*  159 */       reservedNameStr = "uid,union,unique,update,user,upper,using,ufullname,uusertype,uuserlocale,uemail";
/*      */     }
/*      */ 
/*  162 */     Vector reservedVect = StringUtils.parseArray(reservedNameStr.trim(), ',', ',');
/*      */ 
/*  164 */     String name = this.m_binder.getLocal("umdName");
/*  165 */     if (name == null)
/*      */       return;
/*  167 */     String origName = name;
/*  168 */     name = name.trim().toLowerCase();
/*  169 */     if (!reservedVect.contains(name))
/*      */       return;
/*  171 */     String msg = LocaleUtils.encodeMessage("csMetaFieldInvalidNameReservedWord", null, origName);
/*  172 */     createServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getDMSTables()
/*      */     throws DataException, ServiceException
/*      */   {
/*  183 */     DataResultSet namesrset = new DataResultSet(new String[] { "table" });
/*  184 */     this.m_binder.addResultSet("DmsTables", namesrset);
/*      */ 
/*  186 */     String[] tables = this.m_workspace.getTableList();
/*  187 */     DataBinder binder = new DataBinder();
/*  188 */     for (String table : tables)
/*      */     {
/*  190 */       if ((!table.startsWith("DMS")) && (!table.equalsIgnoreCase("DocMeta")))
/*      */         continue;
/*  192 */       binder.putLocal("tableName", table);
/*  193 */       ResultSet rset = this.m_workspace.createResultSet("QdmsFieldInfo", binder);
/*  194 */       DataResultSet drset = new DataResultSet();
/*  195 */       drset.copy(rset);
/*  196 */       this.m_binder.addResultSet(table, drset);
/*  197 */       Vector row = new Vector();
/*  198 */       row.add(table);
/*  199 */       namesrset.addRow(row);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateMetaTable()
/*      */     throws DataException, ServiceException
/*      */   {
/*  208 */     String delFieldsList = this.m_binder.getAllowMissing("MetaFieldsToDelete");
/*  209 */     Vector allDelFieldsNames = StringUtils.parseArray(delFieldsList, ',', ',');
/*  210 */     MetaFieldData mData = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/*  211 */     String[] tables = this.m_workspace.getTableList();
/*  212 */     DataBinder binder = new DataBinder();
/*  213 */     boolean isBouncedGlobal = false;
/*      */ 
/*  215 */     for (String table : tables)
/*      */     {
/*  217 */       if ((!table.startsWith("DMS")) && (!table.equalsIgnoreCase("DocMeta")))
/*      */         continue;
/*  219 */       binder.putLocal("tableName", table);
/*  220 */       ResultSet rset = this.m_workspace.createResultSet("QdmsFieldInfo", binder);
/*      */ 
/*  222 */       Vector addFields = new IdcVector();
/*  223 */       Vector changeFields = new IdcVector();
/*  224 */       Vector delFields = new IdcVector();
/*  225 */       Vector delFieldInfos = new IdcVector();
/*  226 */       mData.createDiffListMultiEx(table, rset, addFields, changeFields, delFieldInfos, this.m_binder);
/*      */ 
/*  228 */       if (!delFieldInfos.isEmpty())
/*      */       {
/*  230 */         for (int i = 0; i < delFieldInfos.size(); ++i)
/*      */         {
/*  232 */           FieldInfo fi = (FieldInfo)delFieldInfos.elementAt(i);
/*  233 */           delFields.addElement(fi.m_name);
/*      */         }
/*  235 */         delFields.retainAll(allDelFieldsNames);
/*      */       }
/*      */ 
/*  238 */       boolean isBounced = DataBinderUtils.getBoolean(this.m_binder, "isBounced", false);
/*  239 */       if (!isBounced)
/*      */       {
/*  241 */         if ((addFields.isEmpty()) && (changeFields.isEmpty()) && (delFields.isEmpty())) {
/*      */           continue;
/*      */         }
/*  244 */         updateMetaTableDesign(table, new String[] { "dID" }, addFields, changeFields, delFields);
/*      */ 
/*  247 */         Object[] params = { addFields, changeFields, delFields, table };
/*  248 */         setCachedObject("updateMetaTable:parameters", params);
/*  249 */         executeFilter("updateMetaTable");
/*  250 */         setCachedObject("updateMetaTable:parameters", "");
/*      */ 
/*  252 */         updateMetaDataProps(addFields, changeFields);
/*      */       }
/*      */       else
/*      */       {
/*  257 */         isBouncedGlobal = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  262 */     if (!isBouncedGlobal)
/*      */       return;
/*  264 */     this.m_binder.putLocal("isBounced", "1");
/*      */   }
/*      */ 
/*      */   protected void updateMetaDataProps(Vector addFields, Vector changeFields)
/*      */     throws DataException, ServiceException
/*      */   {
/*  270 */     ServerSchemaManager manager = SchemaManager.getManager(this.m_workspace);
/*  271 */     SchemaStorage storage = manager.getStorageImplementor("SchemaFieldConfig");
/*      */ 
/*  273 */     Vector[] fieldLists = { addFields, changeFields };
/*      */ 
/*  275 */     for (int i = 0; i < fieldLists.length; ++i)
/*      */     {
/*  277 */       if (fieldLists[i] == null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  281 */       int size = fieldLists[i].size();
/*  282 */       for (int j = 0; j < size; ++j)
/*      */       {
/*  284 */         FieldInfo fi = (FieldInfo)fieldLists[i].elementAt(j);
/*  285 */         SchemaData data = storage.load(fi.m_name, true);
/*  286 */         DataBinder binder = data.getData();
/*  287 */         binder.putLocal("dOldType", MetaFieldData.getMetaType(fi));
/*  288 */         binder.putLocal("isNewField", "false");
/*  289 */         storage.createOrUpdate(fi.m_name, binder, true);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateUserMetaTable()
/*      */     throws DataException, ServiceException
/*      */   {
/*  298 */     String delFieldsList = this.m_binder.getAllowMissing("UserMetaFieldsToDelete");
/*  299 */     Vector delFieldsNames = StringUtils.parseArray(delFieldsList, ',', ',');
/*  300 */     DataResultSet metaFields = SharedObjects.getTable("UserMetaDefinition");
/*  301 */     ResultSet mData = this.m_binder.getResultSet("Users");
/*      */ 
/*  303 */     Vector addFields = new IdcVector();
/*  304 */     Vector changeFields = new IdcVector();
/*  305 */     Vector ignore = new IdcVector();
/*  306 */     MetaFieldUtils.createDiffList(mData, metaFields, addFields, changeFields, ignore);
/*      */ 
/*  309 */     updateMetaTableDesign("Users", new String[] { "dName" }, addFields, changeFields, delFieldsNames);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateOptionList()
/*      */     throws DataException, ServiceException
/*      */   {
/*  316 */     String optKey = this.m_binder.get("dKey");
/*      */ 
/*  320 */     String textString = this.m_binder.getLocal("OptionListString");
/*  321 */     Vector v = new IdcVector();
/*  322 */     if (textString != null)
/*      */     {
/*  324 */       v = DataUtils.parseOptionList(textString);
/*      */     }
/*      */ 
/*  328 */     this.m_binder.addOptionList(optKey, v);
/*      */ 
/*  330 */     Vector opts = this.m_binder.getOptionList(optKey);
/*      */ 
/*  332 */     if (opts == null)
/*      */     {
/*  334 */       opts = new IdcVector();
/*      */     }
/*      */ 
/*  337 */     int nopts = opts.size();
/*      */ 
/*  340 */     this.m_workspace.execute("DoptionList", this.m_binder);
/*      */ 
/*  343 */     ResultSet optTable = SharedObjects.getTable("OptionsList");
/*  344 */     String optionKeyIndex = optKey;
/*  345 */     String keyName = "dKey";
/*  346 */     ResultSetFilter filter = new ResultSetFilter(optionKeyIndex)
/*      */     {
/*      */       public int checkRow(String val, int curNumRows, Vector row)
/*      */       {
/*  350 */         return (val.equals(this.val$optionKeyIndex)) ? 0 : 1;
/*      */       }
/*      */     };
/*  355 */     DataResultSet rset = new DataResultSet();
/*  356 */     rset.copyFiltered(optTable, keyName, filter);
/*      */ 
/*  358 */     String[] keys = { "dKey", "dOption", "dOrder" };
/*  359 */     FieldInfo[] finfo = ResultSetUtils.createInfoList(rset, keys, true);
/*      */ 
/*  362 */     for (int i = 0; i < nopts; ++i)
/*      */     {
/*  364 */       String opt = (String)opts.elementAt(i);
/*  365 */       String order = Integer.toString(i + 1);
/*      */ 
/*  368 */       if (opt.length() > finfo[1].m_maxLen)
/*      */       {
/*  370 */         String msg = LocaleUtils.encodeMessage("csMetaOptionListValueTooLong", null, opt, optKey, "" + finfo[1].m_maxLen);
/*      */ 
/*  372 */         createServiceException(null, msg);
/*      */       }
/*  374 */       this.m_binder.putLocal("dOption", opt);
/*  375 */       this.m_binder.putLocal("dOrder", order);
/*  376 */       this.m_workspace.execute("Ioption", this.m_binder);
/*      */ 
/*  378 */       Vector newRow = rset.createEmptyRow();
/*  379 */       newRow.setElementAt(optKey, finfo[0].m_index);
/*  380 */       newRow.setElementAt(opt, finfo[1].m_index);
/*  381 */       newRow.setElementAt(order, finfo[2].m_index);
/*  382 */       rset.addRow(newRow);
/*      */     }
/*      */ 
/*  385 */     if (opts.size() > 0)
/*      */     {
/*  388 */       ServerSchemaManager manager = SchemaManager.getManager(this.m_workspace);
/*  389 */       SchemaInitUtils schInit = new SchemaInitUtils();
/*  390 */       schInit.init(manager);
/*  391 */       schInit.promoteOptionList(optKey, "table", true);
/*      */ 
/*  394 */       manager.getSchemaData(this.m_binder, "SchemaFieldConfig");
/*  395 */       SharedObjects.tableChanged("SchemaFieldConfig");
/*  396 */       SubjectManager.notifyChanged("schema");
/*      */     }
/*      */ 
/*  401 */     SharedObjects.putOptList(optKey, opts);
/*      */ 
/*  404 */     SharedObjects.putTable("OptionsList", rset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadOptionListFromShared() throws DataException, ServiceException
/*      */   {
/*  410 */     String keyName = this.m_currentAction.getParamAt(0);
/*  411 */     String optionListName = this.m_binder.getLocal(keyName);
/*  412 */     if (null == optionListName)
/*      */     {
/*  414 */       return;
/*      */     }
/*  416 */     Vector optionListValues = SharedObjects.getOptList(optionListName);
/*  417 */     if (null == optionListValues)
/*      */     {
/*  419 */       optionListValues = new IdcVector();
/*      */     }
/*  421 */     this.m_binder.addOptionList(optionListName, optionListValues);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getOptionList() throws DataException, ServiceException
/*      */   {
/*  427 */     String tableName = this.m_currentAction.getParamAt(0);
/*  428 */     MetaFieldUtils.getOptionList(this.m_binder, tableName);
/*      */   }
/*      */ 
/*      */   protected void updateMetaTableDesign(String tableName, String[] keys, Vector addFields, Vector changeFields, Vector delFields)
/*      */     throws DataException
/*      */   {
/*  436 */     int nadd = addFields.size();
/*  437 */     int nchange = changeFields.size();
/*  438 */     int ndel = delFields.size();
/*      */ 
/*  440 */     FieldInfo[] addInfo = new FieldInfo[nadd + nchange];
/*  441 */     String[] delStrs = new String[nchange + ndel];
/*      */ 
/*  443 */     for (int i = 0; i < nadd; ++i)
/*      */     {
/*  445 */       addInfo[i] = ((FieldInfo)addFields.elementAt(i));
/*      */     }
/*      */ 
/*  448 */     for (i = 0; i < ndel; ++i)
/*      */     {
/*  450 */       delStrs[i] = ((String)delFields.elementAt(i));
/*      */     }
/*      */ 
/*  453 */     for (i = 0; i < nchange; ++i)
/*      */     {
/*  455 */       FieldInfo fi = (FieldInfo)changeFields.elementAt(i);
/*  456 */       addInfo[(nadd + i)] = fi;
/*  457 */       delStrs[(ndel + i)] = fi.m_name;
/*      */     }
/*      */ 
/*  461 */     if (tableName.equalsIgnoreCase("Users"))
/*      */     {
/*  463 */       WorkspaceUtils.getWorkspace("user").alterTable(tableName, addInfo, delStrs, keys);
/*      */     }
/*      */     else
/*      */     {
/*  467 */       this.m_workspace.alterTable(tableName, addInfo, delStrs, keys);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   @IdcServiceAction
/*      */   public void addOrEditDocMetaData()
/*      */     throws DataException, ServiceException
/*      */   {
/*  478 */     MetaFieldUtils.validateMetaDataUpdate(this.m_workspace, this.m_binder, this);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   @IdcServiceAction
/*      */   public void moveDocMetaData()
/*      */     throws ServiceException, DataException
/*      */   {
/*  488 */     SystemUtils.reportDeprecatedUsage("MetaService.moveDocMetaData()");
/*  489 */     String name = this.m_binder.get("dName");
/*  490 */     boolean isUp = DataBinderUtils.getBoolean(this.m_binder, "isMoveUp", false);
/*      */ 
/*  492 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("MetaDefs");
/*  493 */     int nameIndex = ResultSetUtils.getIndexMustExist(drset, "dName");
/*  494 */     int orderIndex = ResultSetUtils.getIndexMustExist(drset, "dOrder");
/*      */ 
/*  496 */     Vector row = drset.findRow(nameIndex, name);
/*  497 */     if (row == null)
/*      */     {
/*  499 */       createServiceException(null, "!csMetaDefinitionDoesntExist");
/*      */     }
/*      */ 
/*  502 */     int curRowIndex = drset.getCurrentRow();
/*  503 */     int newIndex = -1;
/*  504 */     if (isUp)
/*      */     {
/*  506 */       newIndex = curRowIndex - 1;
/*  507 */       if (newIndex < 0)
/*      */       {
/*  509 */         createServiceException(null, "!csMetaDefinitionTop");
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  514 */       newIndex = curRowIndex + 1;
/*  515 */       int numRows = drset.getNumRows();
/*  516 */       if (newIndex == numRows)
/*      */       {
/*  518 */         createServiceException(null, "!csMetaDefinitionBottom");
/*      */       }
/*      */     }
/*      */ 
/*  522 */     ServerSchemaManager manager = SchemaManager.getManager(this.m_workspace);
/*  523 */     SchemaStorage fields = manager.getStorageImplementor("SchemaFieldConfig");
/*      */ 
/*  526 */     String order = drset.getStringValue(orderIndex);
/*  527 */     Properties props = drset.getCurrentRowProps();
/*      */ 
/*  529 */     drset.setCurrentRow(newIndex);
/*  530 */     Properties mProps = drset.getCurrentRowProps();
/*  531 */     String newOrder = drset.getStringValue(orderIndex);
/*  532 */     String mName = drset.getStringValue(nameIndex);
/*      */ 
/*  535 */     DataBinder params = new DataBinder();
/*  536 */     params.setLocalData(props);
/*  537 */     params.putLocal("dOrder", "" + newOrder);
/*  538 */     this.m_workspace.execute("Umetadef", params);
/*      */ 
/*  540 */     params.setLocalData(mProps);
/*  541 */     params.putLocal("dOrder", "" + order);
/*  542 */     this.m_workspace.execute("Umetadef", params);
/*      */ 
/*  546 */     SchemaData fieldData = fields.getSchemaData(name);
/*  547 */     DataBinder binder = fieldData.getData();
/*  548 */     binder.putLocal("dOrder", "" + newOrder);
/*      */ 
/*  550 */     SchemaData mFieldData = fields.getSchemaData(mName);
/*  551 */     DataBinder mBinder = mFieldData.getData();
/*  552 */     mBinder.putLocal("dOrder", "" + order);
/*      */ 
/*  555 */     fields.createOrUpdate(name, binder, true);
/*  556 */     fields.createOrUpdate(mName, mBinder, true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteDocMetaData()
/*      */     throws ServiceException, DataException
/*      */   {
/*  563 */     String triggerField = DocProfileManager.getTriggerField();
/*  564 */     String name = this.m_binder.getLocal("dName");
/*  565 */     if ((triggerField != null) && (triggerField.equalsIgnoreCase(name)))
/*      */     {
/*  567 */       String msg = LocaleUtils.encodeMessage("csMetaDataIsDpTrigger", null, name);
/*  568 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/*  572 */     if (name.equals("xIdcProfile"))
/*      */     {
/*  574 */       String msg = LocaleUtils.encodeMessage("csNotAllowedToDeleteField", null, name);
/*  575 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/*  578 */     SchemaEditHelper editHelper = new SchemaEditHelper();
/*  579 */     editHelper.checkFieldUse(name);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getAdvancedSearchOptions() throws ServiceException, DataException
/*      */   {
/*  585 */     DataResultSet drset = new DataResultSet(SearchFieldInfo.DESIGN_COLUMNS);
/*      */ 
/*  587 */     Vector fi = new IdcVector();
/*  588 */     String[] extraClmns = { "currentState", "isInSync", "dCaption", "dType", "IsSortable", "IsOptimized", "IsInSearchResultFilterCategory", "isCaseInsensitive" };
/*  589 */     for (int i = 0; i < extraClmns.length; ++i)
/*      */     {
/*  591 */       FieldInfo info = new FieldInfo();
/*  592 */       info.m_name = extraClmns[i];
/*  593 */       fi.addElement(info);
/*      */     }
/*      */ 
/*  596 */     drset.mergeFieldsWithFlags(fi, 0);
/*  597 */     this.m_binder.addResultSet("SearchFieldOptions", drset);
/*      */ 
/*  599 */     CommonSearchConfig csc = SearchIndexerUtils.getCommonSearchConfig(this);
/*  600 */     this.m_binder.putLocal("allowZoneConfig", csc.getEngineValue("AllowZoneConfig"));
/*  601 */     this.m_binder.putLocal("allowReturnedFieldChange", csc.getEngineValue("AllowReturnedFieldChange"));
/*  602 */     this.m_binder.putLocal("allowOptimizedFieldChange", csc.getEngineValue("AllowOptimizedFieldChange"));
/*  603 */     this.m_binder.putLocal("hasDataTableSortableFieldLinkage", csc.getEngineValue("HasDataTableSortableFieldLinkage"));
/*  604 */     this.m_binder.putLocal("optimizedTextIsSortable", csc.getEngineValue("OptimizedTextIsSortable"));
/*  605 */     this.m_binder.putLocal("supportDrillDownFields", csc.getEngineValue("SupportDrillDownFields"));
/*      */ 
/*  611 */     String engineName = SearchIndexerUtils.getSearchEngineName(this);
/*  612 */     IndexerCollectionData collectionDesignDef = SearchLoader.retrieveIndexDesign(engineName);
/*      */ 
/*  615 */     DataBinder.mergeHashTables(this.m_binder.getLocalData(), collectionDesignDef.m_binder.getLocalData());
/*      */ 
/*  618 */     IndexerCollectionData collectionDef = SearchLoader.getCurrentIndexDesign(engineName);
/*  619 */     if (collectionDef != null)
/*      */     {
/*  621 */       SearchLoader.compareConfigurations(collectionDesignDef, collectionDef);
/*      */     }
/*      */ 
/*  624 */     Hashtable designMap = collectionDesignDef.m_fieldDesignMap;
/*  625 */     Hashtable stateMap = collectionDesignDef.m_fieldStates;
/*  626 */     Hashtable fields = null;
/*  627 */     if (collectionDef != null)
/*      */     {
/*  629 */       fields = (Hashtable)collectionDef.m_fieldDesignMap.clone();
/*      */     }
/*  631 */     for (Enumeration en = designMap.keys(); en.hasMoreElements(); )
/*      */     {
/*  633 */       String fieldName = (String)en.nextElement();
/*  634 */       if ((fieldName.equals("WEB-CGI-ROOT")) || (fieldName.equals("WEB_CGI_ROOT")) || (fieldName.equals("URL")) || (fieldName.equals("DOC_FN")) || (fieldName.equals("SCORE")) || (fieldName.equals("sdDDocTitle")))
/*      */       {
/*  638 */         if (fields == null)
/*      */           continue;
/*  640 */         fields.remove(fieldName);
/*      */       }
/*      */ 
/*  644 */       Properties designProps = (Properties)designMap.get(fieldName);
/*  645 */       Properties stateProps = (Properties)stateMap.get(fieldName);
/*  646 */       Properties props = null;
/*  647 */       if (fields != null)
/*      */       {
/*  649 */         props = (Properties)fields.get(fieldName);
/*  650 */         if (props != null)
/*      */         {
/*  655 */           props = (Properties)props.clone();
/*  656 */           DataBinder.mergeHashTables(props, designProps);
/*  657 */           DataBinder.mergeHashTables(props, stateProps);
/*      */         }
/*  659 */         fields.remove(fieldName);
/*      */       }
/*  661 */       if (props == null)
/*      */       {
/*  663 */         props = designProps;
/*      */       }
/*      */ 
/*  666 */       addFieldRow(fieldName, props, drset);
/*      */     }
/*      */ 
/*  670 */     if (fields == null)
/*      */       return;
/*  672 */     for (Enumeration en = fields.keys(); en.hasMoreElements(); )
/*      */     {
/*  674 */       String fieldName = (String)en.nextElement();
/*  675 */       Properties props = (Properties)fields.get(fieldName);
/*  676 */       addFieldRow(fieldName, props, drset);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addFieldRow(String fieldName, Properties props, DataResultSet drset)
/*      */     throws DataException
/*      */   {
/*  685 */     boolean isIndexDisabled = StringUtils.convertToBool(props.getProperty("IndexDisabled"), false);
/*  686 */     if (isIndexDisabled)
/*      */     {
/*  688 */       props.put("isInSync", "1");
/*  689 */       boolean isSearchDisabled = StringUtils.convertToBool(props.getProperty("SearchDisabled"), false);
/*  690 */       if (isSearchDisabled)
/*      */       {
/*  692 */         return;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  697 */     String[][] keys = { { "isZone", "0" }, { "hasDataTable", "0" }, { "isInSearchResult", "1" }, { "isZoneSearch", "0" }, { "isOptimized", "0" }, { "IsSortable", "0" }, { "IsInSearchResultFilterCategory", "0" }, { "isCaseInsensitive", "0" } };
/*      */ 
/*  708 */     Vector opts = new IdcVector();
/*  709 */     for (int i = 0; i < keys.length; ++i)
/*      */     {
/*  711 */       String key = keys[i][0];
/*  712 */       String defVal = keys[i][1];
/*      */ 
/*  714 */       String val = props.getProperty(key);
/*  715 */       if ((val == null) || (val.length() == 0))
/*      */       {
/*  717 */         val = defVal;
/*      */       }
/*  719 */       opts.addElement(key);
/*  720 */       opts.addElement(val);
/*      */     }
/*  722 */     props.put("advOptions", StringUtils.createString(opts, ',', '^'));
/*      */ 
/*  726 */     props.put("fieldName", fieldName);
/*      */ 
/*  728 */     checkAndUpdateFields(props, new String[][] { { "dCaption", "SearchFieldAPString" }, { "dType", "SearchFieldType" } });
/*      */ 
/*  730 */     String caption = props.getProperty("dCaption");
/*  731 */     if ((caption == null) || (caption.length() == 0))
/*      */     {
/*  734 */       caption = props.getProperty("SearchFieldAPString");
/*  735 */       if (caption != null)
/*      */       {
/*  737 */         props.put("dCaption", caption);
/*      */       }
/*      */     }
/*      */ 
/*  741 */     PropParameters params = new PropParameters(props);
/*  742 */     Vector row = drset.createRow(params);
/*  743 */     drset.addRow(row);
/*      */   }
/*      */ 
/*      */   public void checkAndUpdateFields(Properties props, String[][] keys)
/*      */   {
/*  748 */     for (int i = 0; i < keys.length; ++i)
/*      */     {
/*  750 */       String value = props.getProperty(keys[i][0]);
/*  751 */       if (value != null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  755 */       value = props.getProperty(keys[i][1]);
/*  756 */       if (value == null)
/*      */         continue;
/*  758 */       props.put(keys[i][0], value);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateAdvancedSearchOptions()
/*      */     throws ServiceException, DataException
/*      */   {
/*  766 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("SearchFieldOptions");
/*  767 */     if (drset == null)
/*      */     {
/*  769 */       throw new DataException("!csMissingSearchFieldOptionsTable");
/*      */     }
/*      */ 
/*  772 */     String engineName = SearchIndexerUtils.getSearchEngineName(this);
/*  773 */     DataBinder binder = new DataBinder();
/*  774 */     String isZoneQuickSearch = this.m_binder.getAllowMissing("isZoneQuickSearch");
/*  775 */     if (isZoneQuickSearch == null)
/*      */     {
/*  777 */       isZoneQuickSearch = "0";
/*      */     }
/*  779 */     binder.putLocal("isZoneQuickSearch", isZoneQuickSearch);
/*      */ 
/*  783 */     String isRebuildNeededString = this.m_binder.getAllowMissing("isRebuildNeeded");
/*  784 */     boolean isRebuildNeeded = StringUtils.convertToBool(isRebuildNeededString, false);
/*  785 */     binder.putLocal("isRebuildNeeded", Boolean.toString(isRebuildNeeded));
/*      */ 
/*  787 */     String validSortFields = this.m_binder.getAllowMissing("validSortFields");
/*  788 */     if ((validSortFields != null) && (validSortFields.length() > 0))
/*      */     {
/*  790 */       binder.putLocal("validSortFields", validSortFields);
/*      */     }
/*      */ 
/*  795 */     String isMetaRebuildNeededString = this.m_binder.getAllowMissing("isMetaRebuildNeeded");
/*  796 */     boolean isMetaRebuildNeeded = StringUtils.convertToBool(isMetaRebuildNeededString, false);
/*  797 */     binder.putLocal("isMetaRebuildNeeded", Boolean.toString(isMetaRebuildNeeded));
/*      */ 
/*  799 */     String validDrillDownFields = this.m_binder.getAllowMissing("validDrillDownFields");
/*  800 */     if ((validDrillDownFields != null) && (validDrillDownFields.length() > 0))
/*      */     {
/*  802 */       binder.putLocal("validDrillDownFields", validDrillDownFields);
/*      */     }
/*      */ 
/*  805 */     binder.putLocal("isSearchDesignUpdated", "1");
/*  806 */     binder.addResultSet("SearchDesignOptions", drset);
/*      */ 
/*  808 */     CommonSearchConfig commonSearchConfigObject = (CommonSearchConfig)SharedObjects.getObject("globalObjects", "CommonSearchConfig");
/*      */ 
/*  810 */     String curEngine = commonSearchConfigObject.getCurrentEngineName();
/*      */ 
/*  812 */     IndexerCollectionData collectionDesignDef = SearchLoader.retrieveIndexDesign(curEngine, binder, false);
/*      */ 
/*  814 */     commonSearchConfigObject.updateDrillDownFields(collectionDesignDef, curEngine, "SearchDesignDrillDownFields", false);
/*  815 */     commonSearchConfigObject.setIndexDesignRebuildFlag((isRebuildNeeded) || (isMetaRebuildNeeded));
/*      */ 
/*  821 */     if (!isRebuildNeeded)
/*      */     {
/*  823 */       CommonSearchConfig.updateCommonSearchCfgUsingSearchDesign(collectionDesignDef);
/*      */     }
/*  825 */     SearchLoader.writeDesign(engineName, drset, binder);
/*      */   }
/*      */ 
/*      */   public boolean computeOptionList(Vector params, Vector[] optList, String[] selVal)
/*      */     throws IOException
/*      */   {
/*  833 */     if (super.computeOptionList(params, optList, selVal))
/*      */     {
/*  835 */       return true;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  842 */       int nparams = params.size();
/*  843 */       if (nparams == 0)
/*      */       {
/*  845 */         return false;
/*      */       }
/*      */ 
/*  850 */       String name = (String)params.elementAt(0);
/*  851 */       String optionListKey = this.m_binder.getActiveValue(name);
/*  852 */       if (optionListKey != null)
/*      */       {
/*  854 */         Vector opts = SharedObjects.getOptList(optionListKey);
/*  855 */         if (opts != null)
/*      */         {
/*  857 */           optList[0] = opts;
/*  858 */           return true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  868 */       String msg = LocaleUtils.encodeMessage("csMetaOptionListUnableToBuild", e.getMessage());
/*      */ 
/*  870 */       throw new IOException(msg);
/*      */     }
/*  872 */     return false;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getDocMetaDataInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/*  879 */     boolean isAutoNumber = SharedObjects.getEnvValueAsBoolean("IsAutoNumber", false);
/*  880 */     if (isAutoNumber)
/*      */     {
/*  882 */       this.m_binder.putLocal("isAutoNumber", "1");
/*      */     }
/*      */     else
/*      */     {
/*  886 */       this.m_binder.putLocal("isAutoNumber", "0");
/*      */     }
/*      */ 
/*  889 */     this.m_binder.putLocal("useAccounts", (SecurityUtils.m_useAccounts) ? "1" : "0");
/*      */ 
/*  892 */     this.m_binder.addResultSet("DocTypes", SharedObjects.getTable("DocTypes"));
/*      */ 
/*  894 */     UserData userData = getUserData();
/*  895 */     int privilege = NumberUtils.parseInteger(this.m_binder.getLocal("privilege"), 1);
/*      */ 
/*  898 */     Vector groupList = SecurityUtils.getUserGroupsWithPrivilege(userData, privilege);
/*  899 */     this.m_binder.addOptionList("SecurityGroups", groupList);
/*      */ 
/*  902 */     Vector accountList = null;
/*      */ 
/*  904 */     if (SecurityUtils.m_useAccounts)
/*      */     {
/*  906 */       accountList = SecurityUtils.getAccessibleAccounts(userData, false, privilege, this);
/*      */     }
/*      */     else
/*      */     {
/*  910 */       accountList = new IdcVector();
/*      */     }
/*  912 */     this.m_binder.addOptionList("Accounts", accountList);
/*      */ 
/*  915 */     DataResultSet metaSet = SharedObjects.getTable("DocMetaDefinition");
/*  916 */     DataResultSet drset = new DataResultSet();
/*  917 */     drset.copy(metaSet);
/*      */ 
/*  920 */     Vector v = new IdcVector();
/*  921 */     FieldInfo fi = new FieldInfo();
/*  922 */     fi.m_name = "dOptionListValues";
/*  923 */     fi.m_type = 6;
/*  924 */     v.addElement(fi);
/*  925 */     drset.mergeFieldsWithFlags(v, 2);
/*      */ 
/*  927 */     int isOptionListIndex = ResultSetUtils.getIndexMustExist(drset, "dIsOptionList");
/*  928 */     int optionListKeyIndex = ResultSetUtils.getIndexMustExist(drset, "dOptionListKey");
/*  929 */     int optionListValuesIndex = fi.m_index;
/*      */ 
/*  931 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  933 */       String optionValues = "";
/*      */ 
/*  935 */       boolean isOptionList = StringUtils.convertToBool(drset.getStringValue(isOptionListIndex), false);
/*      */ 
/*  937 */       if (!isOptionList) {
/*      */         continue;
/*      */       }
/*  940 */       String optionListKey = drset.getStringValue(optionListKeyIndex);
/*  941 */       Vector optionList = SharedObjects.getOptList(optionListKey);
/*  942 */       if (optionList != null)
/*      */       {
/*  944 */         int numOptions = optionList.size();
/*  945 */         for (int i = 0; i < numOptions; ++i)
/*      */         {
/*  947 */           String option = (String)optionList.elementAt(i);
/*  948 */           option = StringUtils.addEscapeChars(option, ',', '^');
/*  949 */           if (!optionValues.equals(""))
/*      */           {
/*  951 */             optionValues = optionValues + ",";
/*      */           }
/*  953 */           optionValues = optionValues + option;
/*      */         }
/*      */ 
/*  957 */         this.m_binder.addOptionList(optionListKey, optionList);
/*      */       }
/*  959 */       drset.setCurrentValue(optionListValuesIndex, optionValues);
/*      */     }
/*      */ 
/*  962 */     this.m_binder.addResultSet("DocMetaDefinition", drset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getUserMetaDataInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/*  969 */     DataResultSet metaSet = SharedObjects.getTable("UserMetaDefinition");
/*  970 */     DataResultSet drset = new DataResultSet();
/*  971 */     drset.copy(metaSet);
/*      */ 
/*  974 */     Vector v = new IdcVector();
/*  975 */     FieldInfo fi = new FieldInfo();
/*  976 */     fi.m_name = "umdOptionListValues";
/*  977 */     fi.m_type = 6;
/*  978 */     v.addElement(fi);
/*  979 */     drset.mergeFieldsWithFlags(v, 2);
/*      */ 
/*  981 */     int isOptionListIndex = ResultSetUtils.getIndexMustExist(drset, "umdIsOptionList");
/*  982 */     int optionListKeyIndex = ResultSetUtils.getIndexMustExist(drset, "umdOptionListKey");
/*  983 */     int optionListValuesIndex = fi.m_index;
/*      */ 
/*  985 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  987 */       String optionValues = "";
/*      */ 
/*  989 */       boolean isOptionList = StringUtils.convertToBool(drset.getStringValue(isOptionListIndex), false);
/*      */ 
/*  991 */       if (!isOptionList) {
/*      */         continue;
/*      */       }
/*  994 */       String optionListKey = drset.getStringValue(optionListKeyIndex);
/*  995 */       Vector optionList = SharedObjects.getOptList(optionListKey);
/*  996 */       if (optionList != null)
/*      */       {
/*  998 */         this.m_binder.addOptionList(optionListKey, optionList);
/*      */ 
/* 1000 */         int numOptions = optionList.size();
/* 1001 */         for (int i = 0; i < numOptions; ++i)
/*      */         {
/* 1003 */           String option = (String)optionList.elementAt(i);
/* 1004 */           option = StringUtils.addEscapeChars(option, ',', '^');
/* 1005 */           if (!optionValues.equals(""))
/*      */           {
/* 1007 */             optionValues = optionValues + ",";
/*      */           }
/* 1009 */           optionValues = optionValues + option;
/*      */ 
/* 1012 */           this.m_binder.addOptionList(optionListKey, optionList);
/*      */         }
/*      */       }
/* 1015 */       drset.setCurrentValue(optionListValuesIndex, optionValues);
/*      */     }
/*      */ 
/* 1018 */     this.m_binder.addResultSet("UserMetaDefinition", drset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getDocMetaSetFields() throws DataException, ServiceException
/*      */   {
/* 1024 */     String drSetName = this.m_currentAction.getParamAt(0);
/*      */ 
/* 1026 */     MetaFieldData metaSet = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/* 1027 */     MetaFieldData drset = new MetaFieldData();
/* 1028 */     drset.init(metaSet);
/* 1029 */     this.m_binder.addResultSet(drSetName, drset);
/*      */ 
/* 1031 */     String dms = this.m_binder.getLocal("dDocMetaSet");
/* 1032 */     boolean isDefaultMeta = (dms == null) || (dms.length() == 0) || (dms.equals("DocMeta"));
/*      */ 
/* 1035 */     for (drset.first(); drset.isRowPresent(); )
/*      */     {
/* 1037 */       String docMetaSet = drset.getDocMetaSet();
/* 1038 */       if (((isDefaultMeta) && (((docMetaSet.length() == 0) || (docMetaSet.equals("DocMeta"))))) || (docMetaSet.equals(dms)))
/*      */       {
/* 1042 */         drset.next();
/*      */       }
/*      */       else
/*      */       {
/* 1046 */         drset.deleteCurrentRow();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1053 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98128 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.MetaService
 * JD-Core Version:    0.5.4
 */