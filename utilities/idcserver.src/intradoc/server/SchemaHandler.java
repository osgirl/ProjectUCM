/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.schema.SchemaManager;
/*     */ import intradoc.server.schema.SchemaStorage;
/*     */ import intradoc.server.schema.SchemaUtils;
/*     */ import intradoc.server.schema.ServerSchemaManager;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.schema.SchemaData;
/*     */ import intradoc.shared.schema.SchemaFieldConfig;
/*     */ import intradoc.shared.schema.SchemaFieldData;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.shared.schema.SchemaSecurityFilter;
/*     */ import intradoc.shared.schema.SchemaTableData;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SchemaHandler extends ServiceHandler
/*     */ {
/*  65 */   public Map m_originalResultSets = null;
/*     */ 
/*  67 */   public SchemaHelper m_schemaHelper = null;
/*  68 */   public SchemaUtils m_schemaUtils = null;
/*     */ 
/*     */   public void init(Service service)
/*     */     throws ServiceException, DataException
/*     */   {
/*  81 */     super.init(service);
/*  82 */     this.m_originalResultSets = this.m_binder.getResultSets();
/*     */ 
/*  84 */     this.m_schemaHelper = new SchemaHelper();
/*  85 */     this.m_schemaUtils = new SchemaUtils();
/*  86 */     this.m_schemaUtils.init((Map)AppObjectRepository.getObject("tables"));
/*     */ 
/*  88 */     this.m_service.setCachedObject("SchemaHelper", this.m_schemaHelper);
/*  89 */     this.m_service.setCachedObject("SchemaUtils", this.m_schemaUtils);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getSchemaFields()
/*     */     throws ServiceException, DataException
/*     */   {
/*  96 */     ServerSchemaManager manager = SchemaManager.getManager(this.m_workspace);
/*     */ 
/*  98 */     manager.getSchemaData(this.m_binder, "SchemaFieldConfig");
/*     */   }
/*     */ 
/*     */   protected String getFieldName() throws DataException
/*     */   {
/* 103 */     Vector params = this.m_service.m_currentAction.getParams();
/* 104 */     String fieldType = null;
/* 105 */     if (params.size() > 0)
/*     */     {
/* 107 */       fieldType = (String)params.elementAt(0);
/*     */     }
/*     */ 
/* 110 */     String nameField = "schFieldName";
/* 111 */     if (fieldType != null)
/*     */     {
/* 113 */       if (fieldType.equals("docmeta"))
/*     */       {
/* 115 */         nameField = "dName";
/* 116 */         this.m_binder.putLocal("IsDocMetaField", "true");
/*     */       }
/* 118 */       else if (fieldType.equals("usermeta"))
/*     */       {
/* 120 */         nameField = "umdName";
/* 121 */         this.m_binder.putLocal("IsUserMetaField", "true");
/*     */       }
/*     */     }
/* 124 */     String name = this.m_binder.get(nameField);
/* 125 */     if (name != null)
/*     */     {
/* 127 */       this.m_binder.putLocal("schFieldName", name);
/*     */     }
/* 129 */     return name;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addSchemaField() throws ServiceException, DataException
/*     */   {
/* 135 */     String name = getFieldName();
/*     */ 
/* 137 */     ServerSchemaManager manager = SchemaManager.getManager(this.m_workspace);
/* 138 */     manager.getStorageImplementor("SchemaFieldConfig").createOrUpdate(name, this.m_binder, false);
/* 139 */     SharedObjects.tableChanged("SchemaFieldConfig");
/* 140 */     SubjectManager.notifyChanged("schema");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editSchemaField() throws ServiceException, DataException
/*     */   {
/* 146 */     String name = getFieldName();
/* 147 */     SchemaFieldData field = (SchemaFieldData)this.m_schemaHelper.requireSchemaData("SchemaFieldConfig", name);
/*     */ 
/* 150 */     String targetName = null;
/* 151 */     String orderField = null;
/* 152 */     String keyField = null;
/*     */ 
/* 154 */     targetName = field.get("schFieldTarget");
/* 155 */     if ((targetName == null) && (this.m_service.m_currentAction.getNumParams() > 0))
/*     */     {
/* 157 */       targetName = this.m_service.m_currentAction.getParamAt(0);
/*     */     }
/* 159 */     if (targetName == null)
/*     */     {
/* 161 */       targetName = this.m_binder.getLocal("schFieldTarget");
/*     */     }
/*     */ 
/* 164 */     SchemaData target = null;
/* 165 */     if (targetName != null)
/*     */     {
/* 167 */       target = this.m_schemaHelper.requireSchemaData("SchemaTargetConfig", targetName);
/*     */ 
/* 169 */       orderField = target.get("schOrderFieldName");
/* 170 */       keyField = target.get("schKeyFieldName");
/*     */     }
/* 172 */     if (orderField == null)
/*     */     {
/* 174 */       orderField = "schOrder";
/*     */     }
/* 176 */     if (keyField == null)
/*     */     {
/* 178 */       keyField = "schFieldName";
/*     */     }
/*     */ 
/* 181 */     boolean isMove = DataBinderUtils.getBoolean(this.m_binder, "isMove", false);
/* 182 */     boolean isMoveUp = DataBinderUtils.getBoolean(this.m_binder, "isMoveUp", false);
/*     */ 
/* 184 */     if (isMove)
/*     */     {
/* 186 */       int oldOrder = NumberUtils.parseInteger(field.get(orderField), 1);
/*     */ 
/* 188 */       int max = oldOrder;
/* 189 */       if (!isMoveUp)
/*     */       {
/* 192 */         ResultSet rset = this.m_workspace.createResultSet("Qmetadefs", this.m_binder);
/* 193 */         DataResultSet drset = new DataResultSet();
/* 194 */         drset.copy(rset);
/* 195 */         drset.last();
/* 196 */         String str = ResultSetUtils.getValue(drset, "dOrder");
/* 197 */         max = NumberUtils.parseInteger(str, oldOrder);
/*     */ 
/* 199 */         if (max == oldOrder)
/*     */         {
/* 202 */           return;
/*     */         }
/*     */       }
/* 205 */       int newOrder = oldOrder + ((isMoveUp) ? -1 : 1);
/* 206 */       if (newOrder <= 0)
/*     */       {
/* 208 */         newOrder = 1;
/*     */       }
/* 210 */       SchemaFieldConfig fields = this.m_schemaHelper.m_fields;
/* 211 */       for (fields.first(); fields.isRowPresent(); fields.next())
/*     */       {
/* 213 */         SchemaData tmpField = fields.getData();
/* 214 */         String tmpTarget = tmpField.get("schFieldTarget");
/* 215 */         if ((tmpTarget != null) && (!tmpTarget.equals(targetName)))
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 220 */         int tmpOrder = NumberUtils.parseInteger(tmpField.get(orderField), -1);
/*     */ 
/* 222 */         if (tmpOrder != newOrder)
/*     */           continue;
/* 224 */         tmpField.getData().putLocal(orderField, "" + oldOrder);
/* 225 */         doFieldEdit(target, tmpField.m_name, keyField, orderField, 4);
/*     */       }
/*     */ 
/* 229 */       field.getData().putLocal(orderField, "" + newOrder);
/* 230 */       doFieldEdit(target, field.m_name, keyField, orderField, 4);
/*     */     }
/*     */     else
/*     */     {
/* 235 */       doFieldEdit(target, field.m_name, keyField, orderField, 1);
/*     */     }
/*     */ 
/* 238 */     this.m_binder.putLocal("StatusCode", "0");
/*     */   }
/*     */ 
/*     */   public void doFieldEdit(SchemaData target, String name, String keyField, String orderField, int flags)
/*     */     throws DataException, ServiceException
/*     */   {
/* 245 */     SchemaFieldData field = null;
/* 246 */     boolean isAdd = (flags & 0x2) != 0;
/* 247 */     boolean isDelete = (flags & 0x8) != 0;
/* 248 */     if (!isAdd)
/*     */     {
/* 250 */       field = (SchemaFieldData)this.m_schemaHelper.requireSchemaData("SchemaFieldConfig", name);
/*     */     }
/*     */ 
/* 254 */     String dbTable = null;
/* 255 */     String resourceTable = null;
/* 256 */     String resourceFile = null;
/* 257 */     String updateQuery = null;
/* 258 */     String insertQuery = null;
/* 259 */     String deleteQuery = null;
/* 260 */     if (target != null)
/*     */     {
/* 262 */       dbTable = target.get("schTargetFieldTable");
/* 263 */       updateQuery = target.get("schTargetFieldUpdateQuery");
/* 264 */       insertQuery = target.get("schTargetFieldInsertQuery");
/* 265 */       deleteQuery = target.get("schTargetFieldDeleteQuery");
/* 266 */       resourceTable = target.get("schTargetFieldResourceTable");
/* 267 */       resourceFile = target.get("schTargetFieldResourceFile");
/*     */     }
/*     */ 
/* 270 */     DataBinder params = this.m_binder;
/* 271 */     if ((flags & 0x4) > 0)
/*     */     {
/* 273 */       params = field.getData();
/*     */     }
/*     */ 
/* 280 */     DataResultSet targetFields = target.getResultSet("TargetFieldInfo");
/* 281 */     FieldInfo nameField = new FieldInfo();
/* 282 */     targetFields.getFieldInfo("schFieldName", nameField);
/* 283 */     assert (nameField.m_index >= 0);
/* 284 */     for (targetFields.first(); targetFields.isRowPresent(); targetFields.next())
/*     */     {
/* 286 */       String fieldName = targetFields.getStringValue(nameField.m_index);
/* 287 */       String value = params.getAllowMissing(fieldName);
/* 288 */       if ((fieldName.equals(orderField)) && (((value == null) || (value.length() == 0))))
/*     */       {
/* 294 */         DataResultSet targetData = null;
/* 295 */         if (dbTable != null)
/*     */         {
/* 297 */           targetData = SharedObjects.getTable(dbTable);
/*     */         }
/* 299 */         else if (resourceTable != null)
/*     */         {
/* 301 */           targetData = SharedObjects.getTable(resourceTable);
/*     */         }
/* 303 */         value = "1";
/* 304 */         if (targetData != null)
/*     */         {
/* 306 */           FieldInfo orderFieldInfo = new FieldInfo();
/* 307 */           targetData.getFieldInfo(orderField, orderFieldInfo);
/* 308 */           assert (orderFieldInfo.m_index >= 0);
/* 309 */           int maxOrder = 0;
/* 310 */           targetData.first();
/* 311 */           while ((orderFieldInfo.m_index >= 0) && (targetData.isRowPresent()))
/*     */           {
/* 314 */             String orderString = targetData.getStringValue(orderFieldInfo.m_index);
/*     */ 
/* 316 */             int order = NumberUtils.parseInteger(orderString, 0);
/* 317 */             if (order > maxOrder)
/*     */             {
/* 319 */               maxOrder = order;
/*     */             }
/* 312 */             targetData.next();
/*     */           }
/*     */ 
/* 322 */           value = "" + (maxOrder + 1);
/*     */         }
/* 324 */         params.putLocal(fieldName, value);
/*     */       }
/* 326 */       if (value == null)
/*     */       {
/* 328 */         params.putLocal(fieldName, "");
/*     */       }
/* 330 */       this.m_schemaUtils.validateTypeDowngrade(fieldName, value, field, params);
/*     */     }
/*     */ 
/* 333 */     String theQuery = null;
/* 334 */     if (isAdd)
/*     */     {
/* 336 */       theQuery = insertQuery;
/*     */     }
/* 338 */     else if (isDelete)
/*     */     {
/* 340 */       theQuery = deleteQuery;
/*     */     }
/*     */     else
/*     */     {
/* 344 */       theQuery = updateQuery;
/*     */     }
/*     */ 
/* 347 */     if (theQuery != null)
/*     */     {
/* 349 */       this.m_workspace.execute(theQuery, params);
/*     */     }
/*     */     else
/*     */     {
/* 353 */       String msg = LocaleUtils.encodeMessage("csObjMisconfigured_target", null, target.m_name);
/*     */ 
/* 355 */       if (dbTable != null)
/*     */       {
/* 357 */         theQuery = this.m_schemaUtils.createQueryForTable(target, field, dbTable, this.m_binder, flags);
/*     */ 
/* 359 */         this.m_workspace.executeSQL(theQuery);
/*     */       }
/* 361 */       else if ((resourceFile != null) && (resourceTable != null))
/*     */       {
/* 364 */         throw new DataException(msg);
/*     */       }
/*     */     }
/*     */ 
/* 368 */     ServerSchemaManager manager = SchemaManager.getManager(this.m_workspace);
/* 369 */     manager.getStorageImplementor("SchemaFieldConfig").createOrUpdate(name, params, (flags & 0x2) == 0);
/*     */ 
/* 371 */     SharedObjects.tableChanged("SchemaFieldConfig");
/* 372 */     SubjectManager.notifyChanged("schema");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteSchemaField() throws ServiceException, DataException
/*     */   {
/* 378 */     String name = getFieldName();
/* 379 */     ServerSchemaManager manager = SchemaManager.getManager(this.m_workspace);
/* 380 */     manager.getStorageImplementor("SchemaFieldConfig").delete(name);
/* 381 */     retrieveOriginalResultSet(this.m_binder, "SchemaFieldConfig");
/* 382 */     manager.getSchemaData(this.m_binder, "SchemaFieldConfig");
/* 383 */     SharedObjects.tableChanged("SchemaFieldConfig");
/* 384 */     SubjectManager.notifyChanged("schema");
/*     */   }
/*     */ 
/*     */   public void retrieveOriginalResultSet(DataBinder binder, String name)
/*     */   {
/* 389 */     ResultSet set = binder.getResultSet(name);
/* 390 */     if (set != null)
/*     */       return;
/* 392 */     set = (ResultSet)this.m_originalResultSets.get(name);
/* 393 */     if (set == null)
/*     */       return;
/* 395 */     this.m_binder.addResultSet(name, set);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getSchemaFieldInfo()
/*     */     throws ServiceException, DataException
/*     */   {
/* 403 */     String fieldName = this.m_binder.get("schFieldName");
/* 404 */     boolean allowMissing = DataBinderUtils.getBoolean(this.m_binder, "AllowMissing", false);
/*     */ 
/* 406 */     ServerSchemaManager manager = SchemaManager.getManager(this.m_workspace);
/*     */ 
/* 408 */     SchemaFieldData data = (SchemaFieldData)manager.getStorageImplementor("SchemaFieldConfig").load(fieldName, !allowMissing);
/*     */ 
/* 410 */     if (data == null)
/*     */       return;
/* 412 */     data.populateBinder(this.m_binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void handleSchemaSubmission()
/*     */     throws DataException, ServiceException
/*     */   {
/* 420 */     String targetName = this.m_schemaHelper.getAndDecode(this.m_binder, "targetName");
/* 421 */     String fieldViewName = this.m_schemaHelper.getAndDecode(this.m_binder, "targetFieldView");
/*     */ 
/* 423 */     if (doSubmissionFilters(targetName, fieldViewName) == 1)
/*     */     {
/* 426 */       if (SystemUtils.m_verbose)
/*     */       {
/* 428 */         Report.debug("schemaedit", "a filter completed this operation", null);
/*     */       }
/*     */ 
/* 431 */       return;
/*     */     }
/*     */ 
/* 434 */     SchemaData target = validateTarget(targetName, fieldViewName);
/* 435 */     SchemaViewData fieldView = validateFieldView(targetName, fieldViewName);
/* 436 */     SchemaTableData table = findTargetTable(target, fieldView);
/* 437 */     checkSubmissionSecurity(table);
/*     */ 
/* 439 */     this.m_workspace.beginTran();
/*     */ 
/* 442 */     String query = this.m_schemaUtils.buildSubmissionQuery(this.m_workspace, this.m_binder, target, fieldView, table);
/*     */ 
/* 444 */     this.m_workspace.executeSQL(query);
/* 445 */     this.m_workspace.commitTran();
/*     */   }
/*     */ 
/*     */   public int doSubmissionFilters(String targetName, String fieldViewName)
/*     */     throws DataException, ServiceException
/*     */   {
/* 451 */     if ((targetName != null) && (fieldViewName != null))
/*     */     {
/* 453 */       int rc = PluginFilters.filter("handleSchemaSubmission_" + targetName + "_" + fieldViewName, this.m_workspace, this.m_binder, this.m_service);
/*     */ 
/* 456 */       if (rc == 1)
/*     */       {
/* 458 */         return rc;
/*     */       }
/*     */     }
/*     */ 
/* 462 */     if (targetName != null)
/*     */     {
/* 464 */       int rc = PluginFilters.filter("handleSchemaSubmission_" + targetName, this.m_workspace, this.m_binder, this.m_service);
/*     */ 
/* 467 */       if (rc == 1)
/*     */       {
/* 469 */         return rc;
/*     */       }
/*     */     }
/*     */ 
/* 473 */     int rc = PluginFilters.filter("handleSchemaSubmission", this.m_workspace, this.m_binder, this.m_service);
/*     */ 
/* 475 */     if (rc == 1)
/*     */     {
/* 477 */       return rc;
/*     */     }
/*     */ 
/* 480 */     return 0;
/*     */   }
/*     */ 
/*     */   public SchemaData validateTarget(String targetName, String fieldViewName)
/*     */     throws DataException, ServiceException
/*     */   {
/* 486 */     SchemaData target = this.m_schemaHelper.getSchemaData("SchemaTargetConfig", targetName);
/*     */ 
/* 488 */     if (target == null)
/*     */     {
/* 490 */       String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_target", null, targetName);
/*     */ 
/* 492 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 495 */     if ((target.m_name.equalsIgnoreCase("DocMeta")) || (target.m_name.equalsIgnoreCase("UserMeta")))
/*     */     {
/* 498 */       String msg = LocaleUtils.encodeMessage("apSchemaIllegalOperation", null);
/*     */ 
/* 500 */       throw new ServiceException(msg);
/*     */     }
/* 502 */     return target;
/*     */   }
/*     */ 
/*     */   public SchemaViewData validateFieldView(String targetName, String fieldViewName)
/*     */     throws DataException, ServiceException
/*     */   {
/* 508 */     SchemaViewData fieldView = null;
/* 509 */     if (fieldViewName != null)
/*     */     {
/* 511 */       fieldView = this.m_schemaHelper.getView(fieldViewName);
/* 512 */       if (fieldView == null)
/*     */       {
/* 514 */         String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_view", null, fieldViewName);
/*     */ 
/* 516 */         throw new ServiceException(msg);
/*     */       }
/*     */     }
/* 519 */     return fieldView;
/*     */   }
/*     */ 
/*     */   public SchemaTableData findTargetTable(SchemaData target, SchemaViewData fieldListView)
/*     */     throws ServiceException, DataException
/*     */   {
/* 531 */     String tableName = this.m_schemaHelper.getAndDecode(this.m_binder, "targetTableName");
/* 532 */     boolean badTable = false;
/* 533 */     if (tableName != null)
/*     */     {
/* 537 */       Vector tableList = target.getVector("schTargetTableList");
/* 538 */       if (tableList.indexOf(tableName) < 0)
/*     */       {
/* 540 */         if (SystemUtils.m_verbose)
/*     */         {
/* 542 */           Report.debug("schemaedits", "table " + tableName + " is missing from schTargetTableList in target " + target.m_name, null);
/*     */         }
/*     */ 
/* 546 */         badTable = true;
/*     */       }
/* 548 */       else if (SystemUtils.m_verbose)
/*     */       {
/* 550 */         Report.debug("schemaedits", "considering updating table " + tableName, null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 555 */     if (fieldListView != null)
/*     */     {
/* 558 */       for (int counter = 0; ; ++counter)
/*     */       {
/* 560 */         String key = fieldListView.get("schCriteriaField" + counter);
/* 561 */         if (key == null) {
/*     */           break;
/*     */         }
/*     */ 
/* 565 */         String value = fieldListView.get("schCriteriaValue" + counter);
/* 566 */         if (key.startsWith("schFieldForTable_"))
/*     */         {
/* 568 */           String tmpTableName = key.substring("schFieldForTable_".length());
/*     */ 
/* 570 */           if (tableName == null)
/*     */           {
/* 572 */             tableName = tmpTableName;
/*     */           }
/* 574 */           else if (!tableName.equalsIgnoreCase(tmpTableName))
/*     */           {
/* 576 */             badTable = true;
/* 577 */             if (SystemUtils.m_verbose)
/*     */             {
/* 579 */               Report.debug("schemaedits", "the field view " + fieldListView.m_name + " is for table " + tmpTableName + ", not " + tableName, null);
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 585 */         if ((!key.equals("schFieldTargetTable")) || (value == null))
/*     */           continue;
/* 587 */         if (tableName == null)
/*     */         {
/* 589 */           tableName = value;
/* 590 */           break;
/*     */         }
/* 592 */         if (tableName.equalsIgnoreCase(value))
/*     */           continue;
/* 594 */         badTable = true;
/* 595 */         if (!SystemUtils.m_verbose)
/*     */           continue;
/* 597 */         Report.debug("schemaedits", "the field view " + fieldListView.m_name + " is for table " + value + ", not " + tableName, null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 606 */     if (badTable)
/*     */     {
/* 609 */       String msg = LocaleUtils.encodeMessage("csSchUnableToEditTable", null, tableName);
/*     */ 
/* 611 */       throw new ServiceException(-18, msg);
/*     */     }
/*     */ 
/* 614 */     if (tableName == null)
/*     */     {
/* 616 */       tableName = target.get("schTargetTableName");
/*     */     }
/*     */ 
/* 619 */     if (tableName == null)
/*     */     {
/* 621 */       String msg = LocaleUtils.encodeMessage("csSchUnableToComputeTarget", null);
/*     */ 
/* 623 */       throw new ServiceException(-26, msg);
/*     */     }
/* 625 */     if (SystemUtils.m_verbose)
/*     */     {
/* 627 */       Report.debug("schemaedits", "using table " + tableName, null);
/*     */     }
/*     */ 
/* 630 */     SchemaTableData table = this.m_schemaHelper.getTable(tableName);
/* 631 */     if (table == null)
/*     */     {
/* 633 */       String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_table", null, tableName);
/*     */ 
/* 635 */       throw new ServiceException(msg);
/*     */     }
/* 637 */     return table;
/*     */   }
/*     */ 
/*     */   public void checkSubmissionSecurity(SchemaTableData table)
/*     */     throws DataException, ServiceException
/*     */   {
/* 643 */     int rc = PluginFilters.filter("checkSchemaSubmissionSecurity", this.m_workspace, this.m_binder, this.m_service);
/*     */ 
/* 645 */     String deniedMessage = (String)this.m_service.getCachedObject("SchemaSubmissionSecurityDenied");
/*     */ 
/* 647 */     if ((deniedMessage != null) && (deniedMessage.length() > 0))
/*     */     {
/* 649 */       throw new ServiceException(deniedMessage);
/*     */     }
/* 651 */     String allowedMessage = (String)this.m_service.getCachedObject("SchemaSubmissionSecurityAllowed");
/*     */ 
/* 653 */     if ((allowedMessage != null) && (allowedMessage.length() > 0))
/*     */     {
/* 655 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 657 */       Report.debug("schemaedits", allowedMessage, null);
/*     */     }
/*     */     else
/*     */     {
/* 662 */       String tableSecurity = table.get("schTableSecurityMode");
/* 663 */       if (tableSecurity == null)
/*     */       {
/* 665 */         String msg = LocaleUtils.encodeMessage("csSchUnableToEditData_table", null, table.m_name);
/*     */ 
/* 667 */         throw new ServiceException(msg);
/*     */       }
/* 669 */       if (tableSecurity.equalsIgnoreCase("admin"))
/*     */       {
/* 671 */         if (SecurityUtils.isUserOfRole(this.m_service.m_userData, "admin"))
/*     */           return;
/* 673 */         String msg = LocaleUtils.encodeMessage("csCheckinAdminPermissionDenied", null);
/*     */ 
/* 675 */         throw new ServiceException(-20, msg);
/*     */       }
/*     */ 
/* 679 */       if (tableSecurity.equalsIgnoreCase("role"))
/*     */       {
/* 681 */         Vector roleList = table.getVector("schRoleList");
/* 682 */         boolean isIntersection = table.getBoolean("schRequireAllRoles", false);
/*     */ 
/* 684 */         boolean foundOne = false;
/* 685 */         boolean missedOne = false;
/* 686 */         for (int i = 0; i < roleList.size(); ++i)
/*     */         {
/* 688 */           String role = (String)roleList.elementAt(i);
/* 689 */           if (SecurityUtils.isUserOfRole(this.m_service.m_userData, role))
/*     */           {
/* 691 */             foundOne = true;
/*     */           }
/*     */           else
/*     */           {
/* 695 */             missedOne = true;
/*     */           }
/*     */         }
/* 698 */         if (((isIntersection) && (missedOne)) || (!foundOne))
/*     */         {
/* 700 */           String msg = LocaleUtils.encodeMessage("csInsufficientPrivilege", null);
/*     */ 
/* 702 */           throw new ServiceException(-20, msg);
/*     */         }
/*     */       }
/*     */       else {
/* 706 */         if (!tableSecurity.equalsIgnoreCase("standard"))
/*     */           return;
/* 708 */         SchemaSecurityFilter security = this.m_schemaUtils.getSecurityImplementor(table, "schSecurityImplementor", this.m_service);
/*     */ 
/* 711 */         DataResultSet tableFields = table.getResultSet("TableDefinition");
/* 712 */         String[] fieldList = new String[tableFields.getNumRows()];
/* 713 */         Vector values = new IdcVector();
/* 714 */         boolean isEdit = false;
/* 715 */         tableFields.first();
/* 716 */         int i = 0;
/* 717 */         for (; tableFields.isRowPresent(); ++i)
/*     */         {
/* 719 */           Properties props = tableFields.getCurrentRowProps();
/* 720 */           String isPrimary = props.getProperty("IsPrimaryKey");
/* 721 */           fieldList[i] = props.getProperty("ColumnName");
/* 722 */           String value = this.m_binder.getAllowMissing(fieldList[i]);
/* 723 */           if (value == null)
/*     */           {
/* 725 */             value = "";
/*     */           }
/* 727 */           if ((value.length() > 0) && (StringUtils.convertToBool(isPrimary, false)))
/*     */           {
/* 732 */             isEdit = true;
/*     */           }
/* 734 */           values.addElement(value);
/* 735 */           tableFields.next();
/*     */         }
/* 737 */         if (this.m_binder.getLocal("schIsEdit") == null)
/*     */         {
/* 739 */           this.m_binder.putLocal("schIsEdit", "" + isEdit);
/*     */         }
/* 741 */         DataResultSet resultSet = new DataResultSet(fieldList);
/* 742 */         resultSet.addRow(values);
/* 743 */         int authorizationNeeded = 2;
/*     */         try
/*     */         {
/* 746 */           security.prepareFilterEx(resultSet, table, authorizationNeeded);
/* 747 */           rc = security.checkRow(null, 0, values);
/*     */         }
/*     */         finally
/*     */         {
/* 751 */           security.releaseFilterResultSet();
/*     */         }
/* 753 */         if (rc != 0)
/*     */           return;
/* 755 */         String msg = LocaleUtils.encodeMessage("csInsufficientPrivilege", null);
/*     */ 
/* 757 */         throw new ServiceException(-20, msg);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 766 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102817 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SchemaHandler
 * JD-Core Version:    0.5.4
 */