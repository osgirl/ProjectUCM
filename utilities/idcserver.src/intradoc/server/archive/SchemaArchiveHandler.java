/*     */ package intradoc.server.archive;
/*     */ 
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
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.ArchiveService;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.MonikerWatcher;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHandler;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileReader;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SchemaArchiveHandler extends ServiceHandler
/*     */ {
/*     */   protected String m_archiveDir;
/*     */   protected String m_archiveExportDir;
/*     */   protected String m_archiveName;
/*     */   protected CollectionData m_currentCollection;
/*     */   protected DataBinder m_batch;
/*     */ 
/*     */   public SchemaArchiveHandler()
/*     */   {
/*  34 */     this.m_archiveDir = null;
/*  35 */     this.m_archiveExportDir = null;
/*  36 */     this.m_archiveName = null;
/*  37 */     this.m_currentCollection = null;
/*  38 */     this.m_batch = new DataBinder();
/*     */   }
/*     */ 
/*     */   public void init(Service service) throws ServiceException, DataException
/*     */   {
/*  43 */     super.init(service);
/*  44 */     ArchiveTableUtils.determineSyntaxOnOuterJoin(this.m_workspace);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getTableColumnList()
/*     */     throws ServiceException, DataException
/*     */   {
/*  54 */     this.m_currentCollection = ArchiveUtils.getCollection(this.m_binder);
/*  55 */     this.m_archiveName = this.m_binder.get("aArchiveName");
/*  56 */     this.m_archiveDir = ArchiveUtils.buildArchiveDirectory(this.m_currentCollection.m_location, this.m_archiveName);
/*  57 */     this.m_archiveExportDir = ArchiveUtils.buildArchiveDirectory(this.m_currentCollection.m_exportLocation, this.m_archiveName);
/*     */ 
/*  60 */     String tables = this.m_binder.getLocal("tableNames");
/*  61 */     Vector tableList = StringUtils.parseArray(tables, ',', ',');
/*     */ 
/*  63 */     String[] fields = { "tableName", "columnName", "columnIndex", "columnType", "columnLength", "isPrimaryKey" };
/*     */ 
/*  65 */     DataResultSet drset = new DataResultSet(fields);
/*     */ 
/*  67 */     int size = tableList.size();
/*     */ 
/*  69 */     String tablesNotInDB = "";
/*  70 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  72 */       String tableName = (String)tableList.elementAt(i);
/*  73 */       boolean existInDB = WorkspaceUtils.doesTableExist(this.m_workspace, tableName, null);
/*  74 */       if (!existInDB)
/*     */       {
/*  76 */         if (tableName.length() == 0)
/*     */         {
/*  78 */           tablesNotInDB = tablesNotInDB + ",";
/*     */         }
/*  80 */         tablesNotInDB = tablesNotInDB + '|' + tableName.toLowerCase() + '|';
/*     */       }
/*     */       else {
/*  83 */         tableName = findProperNameFromMap(tableName);
/*  84 */         FieldInfo[] infos = new FieldInfo[0];
/*  85 */         String[] keys = null;
/*     */         try
/*     */         {
/*  89 */           infos = getColumnList(tableName);
/*  90 */           keys = getPrimaryKeys(tableName);
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/*  94 */           Report.trace("archiver", null, e);
/*     */         }
/*     */ 
/*  97 */         for (int j = 0; j < infos.length; ++j)
/*     */         {
/*  99 */           Vector row = new IdcVector();
/* 100 */           row.addElement(tableName);
/* 101 */           row.addElement(infos[j].m_name);
/* 102 */           String type = "Text";
/* 103 */           switch (infos[j].m_type)
/*     */           {
/*     */           case 1:
/* 106 */             type = "Yes/No";
/* 107 */             break;
/*     */           case 3:
/* 109 */             type = "Int";
/* 110 */             break;
/*     */           case 5:
/* 112 */             type = "Date";
/* 113 */             break;
/*     */           case 2:
/*     */           case 4:
/*     */           default:
/* 115 */             type = "Text";
/*     */           }
/*     */ 
/* 118 */           row.addElement("" + infos[j].m_index);
/* 119 */           row.addElement(type);
/* 120 */           row.addElement("" + infos[j].m_maxLen);
/* 121 */           boolean isPrimary = StringUtils.findStringIndexEx(keys, infos[j].m_name, true) > -1;
/*     */ 
/* 123 */           int isPrimaryValue = (isPrimary) ? 1 : 0;
/* 124 */           row.addElement("" + isPrimaryValue);
/* 125 */           drset.addRow(row);
/*     */         }
/*     */       }
/*     */     }
/* 128 */     this.m_binder.putLocal("TablesNotInDatabase", tablesNotInDB);
/* 129 */     this.m_binder.addResultSet("TableColumnList", drset);
/*     */   }
/*     */ 
/*     */   protected FieldInfo[] getColumnList(String tableName) throws DataException
/*     */   {
/* 134 */     boolean isSuppressDataException = DataBinderUtils.getBoolean(this.m_binder, "isSuppressDataException", false);
/*     */ 
/* 137 */     boolean isRetrievingFromBatch = false;
/* 138 */     if (this.m_binder.getLocal("aBatchFile" + tableName) != null)
/*     */     {
/* 140 */       isRetrievingFromBatch = true;
/*     */     }
/* 142 */     FieldInfo[] fis = new FieldInfo[0];
/*     */     try
/*     */     {
/* 145 */       return this.m_workspace.getColumnList(tableName);
/*     */     }
/*     */     catch (DataException path)
/*     */     {
/* 150 */       if ((!isSuppressDataException) && (!isRetrievingFromBatch))
/*     */       {
/* 152 */         throw e;
/*     */       }
/*     */ 
/* 156 */       String path = this.m_binder.getLocal("aBatchFile" + tableName);
/* 157 */       if (path == null)
/*     */         break label257;
/* 159 */       path = this.m_archiveExportDir + '/' + path;
/* 160 */       File batch = new File(path);
/*     */       try
/*     */       {
/* 163 */         BufferedReader reader = new BufferedReader(new FileReader(batch));
/* 164 */         this.m_batch.receive(reader);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 168 */         throw new DataException(e.getMessage());
/*     */       }
/*     */ 
/* 171 */       DataResultSet drset = (DataResultSet)this.m_batch.getResultSet("ExportResults");
/* 172 */       fis = new FieldInfo[drset.getNumFields()];
/* 173 */       int i = 0; if (i >= fis.length)
/*     */         break label257;
/* 175 */       fis[i] = new FieldInfo();
/* 176 */       drset.getIndexFieldInfo(i, fis[i]);
/*     */ 
/* 173 */       ++i;
/*     */     }
/*     */ 
/* 179 */     label257: return fis;
/*     */   }
/*     */ 
/*     */   protected String[] getPrimaryKeys(String tableName) throws DataException
/*     */   {
/* 184 */     boolean isSuppressDataException = DataBinderUtils.getBoolean(this.m_binder, "isSuppressDataException", false);
/*     */     String[] pKeys;
/*     */     try
/*     */     {
/* 189 */       return this.m_workspace.getPrimaryKeys(tableName);
/*     */     }
/*     */     catch (DataException pKeys)
/*     */     {
/* 193 */       if (!isSuppressDataException)
/*     */       {
/* 195 */         throw e;
/*     */       }
/*     */ 
/* 198 */       pKeys = null;
/* 199 */       if (this.m_batch != null)
/*     */       {
/* 201 */         String pKeyStr = this.m_batch.getLocal("primaryKey" + tableName);
/* 202 */         if (pKeyStr != null)
/*     */         {
/* 204 */           Vector keyVect = StringUtils.parseArray(pKeyStr, ',', '\\');
/* 205 */           pKeys = StringUtils.convertListToArray(keyVect);
/*     */         }
/*     */       }
/*     */     }
/* 208 */     return pKeys;
/*     */   }
/*     */ 
/*     */   protected String findProperNameFromMap(String tableName)
/*     */   {
/* 213 */     String[] maps = { "Users", "UserSecurityAttributes", "DocumentAccounts", "OptionsList", "DocTypes", "Subscription", "SecurityGroups", "RoleDefinition", "AliasUser", "Alias", "DocFormats", "ExtensionFormatMap" };
/*     */ 
/* 218 */     for (int i = 0; i < maps.length; ++i)
/*     */     {
/* 220 */       if (tableName.equalsIgnoreCase(maps[i]))
/*     */       {
/* 222 */         return maps[i];
/*     */       }
/*     */     }
/* 225 */     return tableName;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getTableContent() throws DataException, ServiceException
/*     */   {
/* 231 */     String collectionNames = this.m_binder.getLocal("collectionNames");
/* 232 */     String whereClause = this.m_binder.getLocal("whereClause");
/* 233 */     int maxRow = DataBinderUtils.getInteger(this.m_binder, "MaxQueryRows", 500);
/* 234 */     String returnTables = this.m_binder.get("resultName");
/*     */ 
/* 236 */     StringBuffer query = new StringBuffer();
/* 237 */     query.append("SELECT ");
/*     */ 
/* 239 */     Vector tables = StringUtils.parseArray(returnTables, ',', ',');
/* 240 */     int size = tables.size();
/* 241 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 243 */       String table = (String)tables.elementAt(i);
/* 244 */       if ((table == null) || (table.length() == 0))
/*     */       {
/* 246 */         String msg = LocaleUtils.encodeMessage("csArchiverTableNameFormatError", null, returnTables);
/*     */ 
/* 248 */         throw new ServiceException(msg);
/*     */       }
/* 250 */       if (i != 0)
/*     */       {
/* 252 */         query.append(',');
/*     */       }
/* 254 */       query.append(table);
/* 255 */       query.append(".*");
/*     */     }
/* 257 */     if (whereClause.trim().startsWith("FROM "))
/*     */     {
/* 259 */       query.append(whereClause);
/*     */     }
/*     */     else
/*     */     {
/* 263 */       query.append(" FROM ");
/* 264 */       if ((collectionNames == null) || (collectionNames.length() == 0))
/*     */       {
/* 266 */         collectionNames = returnTables;
/*     */       }
/* 268 */       query.append(collectionNames);
/* 269 */       if (whereClause.length() != 0)
/*     */       {
/* 271 */         query.append(" WHERE ");
/* 272 */         query.append(whereClause);
/*     */       }
/*     */     }
/*     */ 
/* 276 */     ResultSet rset = this.m_workspace.createResultSetSQL(query.toString());
/* 277 */     DataResultSet drset = new DataResultSet();
/* 278 */     drset.copy(rset, maxRow);
/* 279 */     this.m_binder.addResultSet(returnTables, drset);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteBatchFileTables()
/*     */     throws ServiceException, DataException
/*     */   {
/* 288 */     getGlobalVariables();
/*     */ 
/* 291 */     DataResultSet drset = (DataResultSet)this.m_binder.removeResultSet("DeletedRows");
/* 292 */     if ((drset == null) || (drset.isEmpty()))
/*     */     {
/* 294 */       throw new DataException("!csArchiverNoDeletedRows");
/*     */     }
/*     */ 
/* 297 */     String filename = this.m_binder.getLocal("aBatchFile");
/* 298 */     DataBinder batchData = ArchiveUtils.readBatchData(this.m_archiveExportDir, filename);
/* 299 */     DataResultSet bset = (DataResultSet)batchData.getResultSet("ExportResults");
/* 300 */     if (bset == null)
/*     */     {
/* 302 */       this.m_service.createServiceException(null, "!csArchiverTableNoContentInfo");
/*     */     }
/*     */ 
/* 305 */     if (bset.isEmpty())
/*     */     {
/* 308 */       this.m_binder.addResultSet("ExportResults", bset);
/* 309 */       return;
/*     */     }
/*     */ 
/* 313 */     for (; drset.isRowPresent(); bset.first())
/*     */     {
/* 315 */       Vector row = findRow(bset, drset.getCurrentRowValues());
/*     */ 
/* 317 */       if (row != null)
/*     */       {
/* 322 */         bset.deleteCurrentRow();
/*     */       }
/* 313 */       drset.next();
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 328 */       String numRowsStr = String.valueOf(bset.getNumRows());
/* 329 */       batchData.putLocal("NumRows", numRowsStr);
/*     */ 
/* 331 */       ResourceUtils.serializeDataBinder(this.m_archiveExportDir, filename, batchData, true, false);
/*     */ 
/* 334 */       this.m_binder.putLocal("StartRow", "0");
/* 335 */       getRowsAt(bset, "ExportResults");
/*     */ 
/* 338 */       File batchFile = new File(this.m_archiveExportDir, filename);
/* 339 */       long ts = batchFile.lastModified();
/* 340 */       ArchiveUtils.addTemporaryResourceCache(filename, batchData, ts);
/*     */ 
/* 343 */       DataBinder exports = ArchiveUtils.readExportsFile(this.m_archiveDir, null);
/* 344 */       DataResultSet rset = (DataResultSet)exports.getResultSet("BatchFiles");
/* 345 */       if ((rset != null) && (!rset.isEmpty()))
/*     */       {
/* 347 */         FieldInfo info = new FieldInfo();
/* 348 */         if (!rset.getFieldInfo("aNumDocuments", info))
/*     */         {
/* 350 */           return;
/*     */         }
/*     */ 
/* 353 */         Vector row = rset.findRow(0, filename);
/* 354 */         if (row != null)
/*     */         {
/* 356 */           row.setElementAt(numRowsStr, info.m_index);
/* 357 */           ArchiveUtils.writeExportsFile(this.m_archiveDir, exports);
/*     */ 
/* 359 */           String str = LocaleUtils.encodeMessage("csArchiverDeletingBatch", null, filename, this.m_archiveName, this.m_currentCollection.m_name);
/*     */ 
/* 361 */           ((ArchiveService)this.m_service).report(str);
/*     */ 
/* 363 */           String monikerString = this.m_currentCollection.getMoniker();
/* 364 */           MonikerWatcher.notifyChanged(monikerString);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 370 */       this.m_service.createServiceException(e, LocaleUtils.encodeMessage("csArchiverFileMissing", null, filename));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Vector findRow(DataResultSet brset, Vector values)
/*     */   {
/* 377 */     boolean match = true;
/*     */ 
/* 379 */     for (brset.first(); brset.isRowPresent(); brset.next())
/*     */     {
/* 381 */       match = true;
/* 382 */       int size = brset.getNumFields();
/* 383 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 385 */         if (brset.getStringValue(i).equals(values.elementAt(i)))
/*     */           continue;
/* 387 */         match = false;
/*     */       }
/*     */ 
/* 390 */       if (match) {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 396 */     if (match)
/*     */     {
/* 398 */       return brset.getCurrentRowValues();
/*     */     }
/* 400 */     return null;
/*     */   }
/*     */ 
/*     */   protected void getRowsAt(DataResultSet bset, String resultSetName)
/*     */   {
/* 405 */     int maxRows = 50;
/* 406 */     String maxStr = this.m_binder.getAllowMissing("MaxRowsPerPage");
/* 407 */     if ((maxStr != null) && (maxStr.length() > 0))
/*     */     {
/*     */       try
/*     */       {
/* 411 */         maxRows = Integer.parseInt(maxStr);
/*     */       }
/*     */       catch (Throwable ignore)
/*     */       {
/* 415 */         if (SystemUtils.m_verbose)
/*     */         {
/* 417 */           Report.debug("systemparse", null, ignore);
/*     */         }
/*     */       }
/*     */ 
/* 421 */       if (maxRows == 0)
/*     */       {
/* 424 */         maxRows = 50;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 429 */     this.m_binder.putLocal("MaxRowsPerPage", String.valueOf(maxRows));
/*     */ 
/* 432 */     int startRow = 0;
/*     */     try
/*     */     {
/* 435 */       String startRowStr = this.m_binder.getLocal("StartRow");
/* 436 */       if (startRowStr != null)
/*     */       {
/* 438 */         startRow = Integer.parseInt(startRowStr);
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 443 */       if (SystemUtils.m_verbose)
/*     */       {
/* 445 */         Report.debug("systemparse", null, t);
/*     */       }
/*     */     }
/*     */ 
/* 449 */     int numRows = bset.getNumRows();
/* 450 */     if (startRow < 0)
/*     */     {
/* 452 */       startRow = 0;
/*     */     }
/* 454 */     else if (startRow > numRows)
/*     */     {
/* 456 */       startRow = numRows / maxRows * maxRows;
/*     */     }
/*     */ 
/* 459 */     bset.setCurrentRow(startRow);
/*     */ 
/* 465 */     this.m_binder.addResultSet(resultSetName, bset);
/*     */ 
/* 468 */     this.m_binder.putLocal("StartRow", String.valueOf(startRow));
/* 469 */     this.m_binder.putLocal("NumRows", String.valueOf(numRows));
/*     */   }
/*     */ 
/*     */   protected void getGlobalVariables()
/*     */   {
/* 474 */     this.m_archiveName = this.m_binder.getLocal("aArchiveName");
/* 475 */     this.m_archiveDir = ((String)this.m_service.getCachedObject("ArchiveDir"));
/* 476 */     this.m_archiveExportDir = ((String)this.m_service.getCachedObject("ArchiveExportDir"));
/* 477 */     this.m_currentCollection = ((CollectionData)this.m_service.getCachedObject("CurrentCollection"));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getArchivableTables()
/*     */   {
/* 485 */     DataResultSet drset = new DataResultSet(new String[] { "tableName", "createColumn", "modifiedColumn", "dependencies", "dependencyMapping" });
/*     */ 
/* 487 */     Vector row = new IdcVector();
/* 488 */     row.addElement("Users");
/* 489 */     row.addElement("dUserArriveDate");
/* 490 */     row.addElement("dUserChangeDate");
/* 491 */     row.addElement("");
/* 492 */     row.addElement("");
/* 493 */     drset.addRow(row);
/* 494 */     row = new IdcVector();
/* 495 */     row.addElement("UserSecurityAttributes");
/* 496 */     row.addElement("");
/* 497 */     row.addElement("");
/* 498 */     row.addElement("Users");
/* 499 */     row.addElement("UserSecurityAttributes.dUserName=Users.dName");
/* 500 */     drset.addRow(row);
/*     */ 
/* 502 */     addSchemaArchivableTables(drset);
/* 503 */     this.m_binder.addResultSet("ArchivableTables", drset);
/*     */     try
/*     */     {
/* 506 */       if (PluginFilters.filter("ArchivableTableRelationsDefinition", this.m_workspace, this.m_binder, this.m_service) != 0)
/*     */       {
/* 509 */         return;
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 514 */       Report.appError("archiver", "archiver", "csArchiverAssembleArchivableTableFilterError", e);
/*     */     }
/*     */ 
/* 518 */     String tableList = "|workflowaliases|workflowcriteria|workflowdocattributes|workflowdocuments|workflowsteps|workflowstates|workflows|projectdocuments|registeredprojects|config|counters|datedcaches|docmeta|documents|revisions|htmlconversions|htmlconversionsums|";
/*     */ 
/* 522 */     if (this.m_binder.getAllowMissing("NotArchivableTables") == null)
/*     */     {
/* 524 */       this.m_binder.putLocal("NotArchivableTables", tableList.toLowerCase());
/*     */     }
/*     */     else
/*     */     {
/* 528 */       tableList = this.m_binder.getAllowMissing("NotArchivableTables");
/*     */     }
/* 530 */     String override = SharedObjects.getEnvironmentValue("ArchiverOverrideTables");
/* 531 */     if (override != null)
/*     */     {
/* 533 */       this.m_binder.putLocal("ArchiverOverrideTables", override.toLowerCase());
/*     */     }
/*     */ 
/* 536 */     for (drset.last(); drset.isRowPresent(); drset.previous())
/*     */     {
/* 538 */       String table = drset.getStringValue(0).toLowerCase();
/* 539 */       if ((tableList.indexOf("|" + table + "|") >= 0) && (((override == null) || (override.indexOf("|" + table + "|") < 0))))
/*     */       {
/* 542 */         drset.deleteCurrentRow();
/*     */       }
/* 544 */       if (drset.getCurrentRow() == 0)
/*     */         return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addSchemaArchivableTables(DataResultSet drset)
/*     */   {
/* 553 */     DataResultSet tableConfig = SharedObjects.getTable("SchemaTableConfig");
/* 554 */     DataResultSet relationConfig = SharedObjects.getTable("SchemaRelationConfig").shallowClone();
/*     */ 
/* 556 */     int size = tableConfig.getNumRows();
/* 557 */     String[] tableColumns = { "schTableName", "schTableRowCreateTimestamp", "schTableRowModifyTimestamp" };
/*     */ 
/* 560 */     int[] tableIndexes = retrieveFieldIndexes(tableConfig, tableColumns);
/*     */ 
/* 562 */     String[] relationColumns = { "schTable1Table", "schTable1Column", "schTable2Table", "schTable2Column" };
/*     */ 
/* 564 */     int[] relationIndexes = retrieveFieldIndexes(relationConfig, relationColumns);
/* 565 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 567 */       Vector tableRow = tableConfig.getRowValues(i);
/* 568 */       String name = (String)tableRow.elementAt(tableIndexes[0]);
/* 569 */       String create = (String)tableRow.elementAt(tableIndexes[1]);
/* 570 */       String modify = (String)tableRow.elementAt(tableIndexes[2]);
/* 571 */       ResultSetFilter filter = relationConfig.createSimpleResultSetFilter(name);
/* 572 */       DataResultSet filteredRelation = new DataResultSet();
/* 573 */       filteredRelation.copyFiltered(relationConfig, "schTable2Table", filter);
/*     */ 
/* 575 */       StringBuffer parents = new StringBuffer();
/* 576 */       StringBuffer relations = new StringBuffer();
/*     */ 
/* 578 */       for (filteredRelation.first(); filteredRelation.isRowPresent(); )
/*     */       {
/* 581 */         String parent = filteredRelation.getStringValue(relationIndexes[0]);
/* 582 */         String parentCol = filteredRelation.getStringValue(relationIndexes[1]);
/* 583 */         String child = filteredRelation.getStringValue(relationIndexes[2]);
/* 584 */         String childCol = filteredRelation.getStringValue(relationIndexes[3]);
/* 585 */         if (parents.length() > 0)
/*     */         {
/* 587 */           parents.append(",");
/* 588 */           relations.append(",");
/*     */         }
/* 590 */         parents.append(parent);
/* 591 */         relations.append(parent);
/* 592 */         relations.append(".");
/* 593 */         relations.append(parentCol);
/* 594 */         relations.append("=");
/* 595 */         relations.append(child);
/* 596 */         relations.append(".");
/* 597 */         relations.append(childCol);
/*     */ 
/* 579 */         filteredRelation.next();
/*     */       }
/*     */ 
/* 599 */       Vector row = new IdcVector();
/* 600 */       row.addElement(name);
/* 601 */       row.addElement(create);
/* 602 */       row.addElement(modify);
/* 603 */       row.addElement(parents.toString());
/* 604 */       row.addElement(relations.toString());
/* 605 */       boolean skip = getTableExistInResultSet(name, 0, drset);
/* 606 */       if (skip)
/*     */         continue;
/* 608 */       drset.addRow(row);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean getTableExistInResultSet(String name, int index, DataResultSet drset)
/*     */   {
/* 615 */     boolean hasTable = false;
/* 616 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 618 */       String existName = drset.getStringValue(index).toLowerCase();
/* 619 */       if (!existName.equals(name.toLowerCase()))
/*     */         continue;
/* 621 */       hasTable = true;
/* 622 */       break;
/*     */     }
/*     */ 
/* 625 */     return hasTable;
/*     */   }
/*     */ 
/*     */   protected int[] retrieveFieldIndexes(DataResultSet drset, String[] colNames)
/*     */   {
/* 630 */     int[] indexes = new int[colNames.length];
/* 631 */     for (int i = 0; i < colNames.length; ++i)
/*     */     {
/* 633 */       FieldInfo fi = new FieldInfo();
/* 634 */       drset.getFieldInfo(colNames[i], fi);
/* 635 */       indexes[i] = fi.m_index;
/*     */     }
/* 637 */     return indexes;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void constructRelationQuery() throws ServiceException, DataException
/*     */   {
/* 643 */     String table = this.m_binder.get("table");
/* 644 */     String parentTable = this.m_binder.get("parentTable");
/* 645 */     String relations = this.m_binder.get("relations");
/* 646 */     String queries = this.m_binder.get("whereClause");
/*     */ 
/* 648 */     queries = ArchiveTableUtils.constructArchiveQueryFragment(table, parentTable, relations, queries);
/* 649 */     this.m_binder.putLocal("QueryString", queries);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 654 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97046 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.SchemaArchiveHandler
 * JD-Core Version:    0.5.4
 */