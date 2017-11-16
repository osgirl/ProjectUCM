/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.QueryUtils;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.serialize.DataBinderLocalizer;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SerializeTable
/*     */ {
/*  36 */   protected Workspace m_workspace = null;
/*  37 */   protected ReportProgress m_progress = null;
/*     */ 
/*  39 */   protected String m_log = null;
/*  40 */   protected boolean m_isTolerant = false;
/*  41 */   protected boolean m_isTransactional = false;
/*  42 */   protected boolean m_isDeleteTable = false;
/*     */ 
/*  45 */   protected String m_selectSql = null;
/*  46 */   protected String m_insertSql = null;
/*  47 */   protected String m_updateSql = null;
/*  48 */   protected String m_deleteAllSql = null;
/*  49 */   protected String m_deleteSql = null;
/*     */ 
/*  52 */   protected Vector m_constraints = null;
/*  53 */   protected Vector m_parameters = null;
/*  54 */   protected Vector m_updateParameters = null;
/*  55 */   protected Vector m_insertParameters = null;
/*     */ 
/*  58 */   protected HashMap m_updateIgnoringFields = new HashMap();
/*  59 */   protected HashMap m_insertIgnoringFields = new HashMap();
/*     */ 
/*  61 */   public ExecutionContext m_cxt = null;
/*     */ 
/*  64 */   protected static boolean m_isFirstTime = true;
/*  65 */   protected boolean m_doFullReport = false;
/*     */ 
/*     */   public SerializeTable(Workspace ws, ReportProgress rp)
/*     */   {
/*  69 */     this.m_workspace = ws;
/*  70 */     this.m_progress = rp;
/*     */ 
/*  72 */     this.m_cxt = new ExecutionContextAdaptor();
/*  73 */     IdcLocale locale = new IdcLocale("system");
/*  74 */     locale.m_dateFormat = LocaleResources.m_odbcFormat;
/*  75 */     this.m_cxt.setCachedObject("UserLocale", locale);
/*     */ 
/*  77 */     if (!m_isFirstTime)
/*     */       return;
/*  79 */     this.m_doFullReport = ((SharedObjects.getEnvValueAsBoolean("SerializeTableAllowFullTrace", false)) || (SystemUtils.m_verbose));
/*     */ 
/*  81 */     m_isFirstTime = false;
/*     */   }
/*     */ 
/*     */   public void initNonModifiableFields()
/*     */   {
/*  87 */     populateNonModifiableFields(this.m_updateIgnoringFields, "ArchiveTableUpdateIgnoreFieldList");
/*  88 */     populateNonModifiableFields(this.m_insertIgnoringFields, "ArchiveTableInsertIgnoreFieldList");
/*     */   }
/*     */ 
/*     */   public void populateNonModifiableFields(HashMap map, String key)
/*     */   {
/*  94 */     String ignoringFields = SharedObjects.getEnvironmentValue(key);
/*  95 */     Vector fields = StringUtils.parseArray(ignoringFields, ',', ',');
/*  96 */     int size = fields.size();
/*  97 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 100 */       String tmp = (String)fields.elementAt(i);
/* 101 */       map.put(tmp.toLowerCase(), "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setErrorInfo(String log, boolean isTolerant)
/*     */   {
/* 107 */     this.m_log = log;
/* 108 */     this.m_isTolerant = isTolerant;
/*     */   }
/*     */ 
/*     */   public void setIsTransactional(boolean isTransactional)
/*     */   {
/* 113 */     this.m_isTransactional = isTransactional;
/*     */   }
/*     */ 
/*     */   public void setIsDeleteTable(boolean isDelete)
/*     */   {
/* 118 */     this.m_isDeleteTable = isDelete;
/*     */   }
/*     */ 
/*     */   public void serialize(String dir, String name, String table, String constraintKeys, boolean isExport)
/*     */     throws DataException, ServiceException
/*     */   {
/* 124 */     if (isExport)
/*     */     {
/* 126 */       exportTable(dir, name, table);
/*     */     }
/*     */     else
/*     */     {
/* 130 */       importTables(dir, name, table, constraintKeys);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void exportTable(String dir, String fileName, String table)
/*     */     throws DataException, ServiceException
/*     */   {
/* 137 */     ResultSet rset = getTable(table);
/* 138 */     if (rset == null)
/*     */     {
/* 140 */       String msg = LocaleUtils.encodeMessage("csUnableToFindTable", null, table);
/*     */ 
/* 142 */       throw new ServiceException(msg);
/*     */     }
/* 144 */     writeResultSet(dir, fileName, table, rset);
/*     */   }
/*     */ 
/*     */   protected void exportResultSet(String dir, String fileName, String rsName, String sql)
/*     */     throws DataException, ServiceException
/*     */   {
/* 150 */     ResultSet rset = this.m_workspace.createResultSetSQL(sql);
/* 151 */     if (rset == null)
/*     */     {
/* 153 */       String msg = LocaleUtils.encodeMessage("csQueryInError", null, sql);
/* 154 */       throw new ServiceException(msg);
/*     */     }
/* 156 */     writeResultSet(dir, fileName, rsName, rset);
/*     */   }
/*     */ 
/*     */   protected void writeResultSet(String dir, String name, String tableName, ResultSet rset)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 164 */       DataBinder data = new DataBinder(true);
/* 165 */       data.m_blDateFormat = null;
/* 166 */       data.m_localeDateFormat = ((IdcDateFormat)this.m_cxt.getLocaleResource(3));
/* 167 */       data.addResultSet(tableName, rset);
/* 168 */       DataBinderLocalizer localizer = new DataBinderLocalizer(data, this.m_cxt);
/* 169 */       localizer.localizeBinder(1);
/* 170 */       ResourceUtils.serializeDataBinder(dir, name, data, true, false);
/*     */     }
/*     */     finally
/*     */     {
/* 174 */       rset.closeInternals();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void importTables(String dir, String filename, String table, String constraintKeys)
/*     */     throws DataException, ServiceException
/*     */   {
/* 181 */     String msg = LocaleUtils.encodeMessage("csImportingFile", null, filename);
/* 182 */     reportProgress(0, msg, -1.0F, -1.0F);
/*     */ 
/* 185 */     DataBinder binder = ResourceUtils.readDataBinder(dir, filename);
/*     */ 
/* 187 */     boolean isFound = false;
/* 188 */     boolean isAll = true;
/* 189 */     if (table != null)
/*     */     {
/* 191 */       isAll = false;
/*     */     }
/*     */ 
/* 195 */     for (Enumeration en = binder.getResultSetList(); en.hasMoreElements(); )
/*     */     {
/* 197 */       String tableName = (String)en.nextElement();
/* 198 */       DataResultSet rset = (DataResultSet)binder.getResultSet(tableName);
/* 199 */       if ((!isAll) && (tableName.equalsIgnoreCase(table)))
/*     */       {
/* 201 */         isFound = true;
/*     */       }
/*     */ 
/* 204 */       if ((isAll) || (isFound))
/*     */       {
/* 206 */         importTable(tableName, rset, constraintKeys);
/*     */       }
/*     */ 
/* 209 */       if ((isFound) && (!isAll))
/*     */       {
/* 211 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 215 */     if ((isAll) || (isFound)) {
/*     */       return;
/*     */     }
/* 218 */     throw new ServiceException("Unable to find table " + table + " in file " + filename + " for import.");
/*     */   }
/*     */ 
/*     */   public void importTable(String tableName, DataResultSet rset, String constraintKeys)
/*     */     throws DataException, ServiceException
/*     */   {
/* 226 */     importTable(tableName, rset, constraintKeys, null, null, null, false);
/*     */   }
/*     */ 
/*     */   public void importTable(String tableName, DataResultSet rset, String constraintKeys, String additionalFilter, ValueMapData data, TableSerializationCallback tableCallback, boolean isDelete)
/*     */     throws DataException, ServiceException
/*     */   {
/* 234 */     if ((rset == null) || (rset.isEmpty()))
/*     */     {
/* 237 */       return;
/*     */     }
/*     */ 
/* 240 */     reportProgress(0, "Loading " + tableName, -1.0F, -1.0F);
/*     */ 
/* 243 */     ResultSet rs = getTable(tableName);
/* 244 */     DataResultSet rsTable = new DataResultSet();
/* 245 */     rsTable.copyFieldInfo(rs);
/*     */ 
/* 248 */     boolean isInTran = false;
/* 249 */     if (this.m_isTransactional)
/*     */     {
/* 252 */       this.m_workspace.beginTran();
/* 253 */       isInTran = true;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 258 */       if (this.m_isDeleteTable)
/*     */       {
/* 261 */         String sql = "DELETE FROM " + tableName;
/* 262 */         this.m_workspace.executeSQL(sql);
/*     */       }
/* 264 */       buildSql(tableName, rsTable, rset, constraintKeys, additionalFilter);
/*     */ 
/* 266 */       mergeTable(tableName, rset, constraintKeys, data, tableCallback, isDelete);
/* 267 */       if (isInTran)
/*     */       {
/* 270 */         isInTran = false;
/* 271 */         this.m_workspace.commitTran();
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 276 */       if (isInTran)
/*     */       {
/* 278 */         this.m_workspace.rollbackTran();
/*     */       }
/*     */     }
/*     */ 
/* 282 */     reportProgress(0, "Finished loading table " + tableName + ".", -1.0F, -1.0F);
/*     */   }
/*     */ 
/*     */   protected void mergeTable(String tableName, DataResultSet dset, String constraintKeys, ValueMapData data, TableSerializationCallback tableCallback, boolean isDelete)
/*     */     throws DataException, ServiceException
/*     */   {
/* 291 */     String msg = LocaleUtils.encodeMessage("csImportingTable", null, tableName);
/* 292 */     DataBinder binder = new DataBinder();
/* 293 */     binder.addResultSet(tableName, dset);
/*     */ 
/* 295 */     int count = 0;
/* 296 */     int numRows = dset.getNumRows();
/* 297 */     for (dset.first(); dset.isRowPresent(); ++count)
/*     */     {
/* 299 */       String sql = null;
/*     */       try
/*     */       {
/* 302 */         boolean isUpdate = false;
/* 303 */         if (this.m_constraints != null)
/*     */         {
/* 306 */           String selQuery = createQuery(this.m_selectSql, this.m_constraints, binder, data);
/* 307 */           ResultSet rset = this.m_workspace.createResultSetSQL(selQuery);
/* 308 */           boolean skipRow = false;
/* 309 */           if (rset.isRowPresent())
/*     */           {
/* 311 */             if (!isDelete)
/*     */             {
/* 313 */               isUpdate = true;
/*     */             }
/*     */           }
/* 316 */           else if (isDelete)
/*     */           {
/* 319 */             skipRow = true;
/*     */           }
/* 321 */           if ((!skipRow) && (((isUpdate) || (isDelete))) && 
/* 323 */             (tableCallback != null) && (!tableCallback.allowModifyAndDelete(dset, rset, isDelete)))
/*     */           {
/* 325 */             skipRow = true;
/*     */           }
/*     */ 
/* 328 */           rset.closeInternals();
/* 329 */           if (skipRow);
/*     */         }
/*     */ 
/* 335 */         if (isDelete)
/*     */         {
/* 337 */           sql = createQuery(this.m_deleteSql, this.m_parameters, binder, data);
/*     */         }
/* 339 */         else if (isUpdate)
/*     */         {
/* 341 */           sql = createQuery(this.m_updateSql, this.m_updateParameters, binder, data);
/*     */         }
/*     */         else
/*     */         {
/* 345 */           sql = createQuery(this.m_insertSql, this.m_insertParameters, binder, data);
/*     */         }
/*     */ 
/* 348 */         long result = this.m_workspace.executeSQL(sql);
/*     */ 
/* 350 */         if (tableCallback != null)
/*     */         {
/* 352 */           tableCallback.postModification(this.m_workspace, tableName, dset, constraintKeys, result, isDelete);
/*     */         }
/*     */ 
/* 356 */         if (count % 20 == 0)
/*     */         {
/* 358 */           reportProgress(0, msg, count, numRows);
/*     */         }
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 363 */         if (tableCallback != null)
/*     */         {
/* 365 */           tableCallback.handleImportError(this.m_workspace, tableName, dset, isDelete, e);
/*     */         }
/*     */ 
/* 368 */         if (this.m_isTolerant)
/*     */         {
/*     */           String errMsg;
/*     */           String errMsg;
/* 371 */           if (sql == null)
/*     */           {
/* 373 */             errMsg = LocaleUtils.encodeMessage("csUnableToImportTableValue", null, tableName);
/*     */           }
/*     */           else
/*     */           {
/* 378 */             errMsg = LocaleUtils.encodeMessage("csUnableToImportTableValue2", null, tableName, sql);
/*     */           }
/*     */ 
/* 381 */           Report.appWarning(this.m_log, null, errMsg, e);
/*     */         }
/*     */         else
/*     */         {
/* 385 */           msg = LocaleUtils.encodeMessage("csUnableToImportTableValue3", null, tableName);
/*     */ 
/* 387 */           throw new ServiceException(msg, e);
/*     */         }
/*     */       }
/* 297 */       dset.next();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void buildSql(String tableName, ResultSet rsTable, ResultSet rset, String constraintKeys, String additionalFilter)
/*     */     throws DataException, ServiceException
/*     */   {
/* 396 */     this.m_parameters = new IdcVector();
/* 397 */     this.m_updateParameters = new IdcVector();
/* 398 */     this.m_insertParameters = new IdcVector();
/*     */ 
/* 401 */     StringBuffer fieldNames = new StringBuffer();
/* 402 */     StringBuffer qMarks = new StringBuffer();
/* 403 */     StringBuffer updateClauses = new StringBuffer();
/* 404 */     String commaSep = ", ";
/*     */ 
/* 406 */     boolean pastFirstFieldUpdate = false;
/* 407 */     boolean pastFirstFieldInsert = false;
/* 408 */     int numFields = rsTable.getNumFields();
/* 409 */     String ignoreTablePrefix = tableName.toLowerCase() + ":";
/* 410 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 412 */       FieldInfo info = new FieldInfo();
/* 413 */       rsTable.getIndexFieldInfo(i, info);
/*     */ 
/* 416 */       FieldInfo fi = new FieldInfo();
/* 417 */       if (!rset.getFieldInfo(info.m_name, fi)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 421 */       String varName = info.m_name;
/* 422 */       String key = ignoreTablePrefix + varName.toLowerCase();
/* 423 */       boolean isAllowUpdate = this.m_updateIgnoringFields.get(key) == null;
/* 424 */       boolean isAllowInsert = this.m_insertIgnoringFields.get(key) == null;
/* 425 */       if (isAllowUpdate)
/*     */       {
/* 427 */         this.m_updateParameters.addElement(info);
/*     */ 
/* 429 */         if (pastFirstFieldUpdate)
/*     */         {
/* 431 */           updateClauses.append(commaSep);
/*     */         }
/* 433 */         updateClauses.append(varName);
/* 434 */         updateClauses.append(" = ?");
/* 435 */         pastFirstFieldUpdate = true;
/*     */       }
/* 437 */       if (!isAllowInsert)
/*     */         continue;
/* 439 */       this.m_insertParameters.addElement(info);
/*     */ 
/* 442 */       if (pastFirstFieldInsert)
/*     */       {
/* 444 */         fieldNames.append(commaSep);
/* 445 */         qMarks.append(commaSep);
/*     */       }
/*     */ 
/* 449 */       fieldNames.append(varName);
/* 450 */       qMarks.append('?');
/*     */ 
/* 452 */       pastFirstFieldInsert = true;
/*     */     }
/*     */ 
/* 458 */     if (this.m_updateParameters.size() == 0)
/*     */     {
/* 460 */       String msg = LocaleUtils.encodeMessage("csTableMismatch", null, tableName);
/*     */ 
/* 462 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 467 */     StringBuffer insertSql = new StringBuffer();
/* 468 */     insertSql.append("INSERT INTO ");
/* 469 */     insertSql.append(tableName);
/* 470 */     insertSql.append(" (");
/*     */ 
/* 472 */     boolean isFirst = true;
/* 473 */     if (fieldNames.length() > 0)
/*     */     {
/* 475 */       if (!isFirst)
/*     */       {
/* 477 */         insertSql.append(commaSep);
/*     */       }
/* 479 */       insertSql.append(fieldNames);
/* 480 */       isFirst = false;
/*     */     }
/*     */ 
/* 483 */     isFirst = true;
/* 484 */     insertSql.append(") values(");
/* 485 */     if (qMarks.length() > 0)
/*     */     {
/* 487 */       if (!isFirst)
/*     */       {
/* 489 */         insertSql.append(commaSep);
/*     */       }
/* 491 */       insertSql.append(qMarks);
/*     */     }
/* 493 */     insertSql.append(")");
/*     */ 
/* 495 */     this.m_insertSql = insertSql.toString();
/*     */ 
/* 498 */     StringBuffer whereClause = null;
/* 499 */     Vector cKeys = StringUtils.parseArray(constraintKeys, ',', ',');
/* 500 */     if ((cKeys.isEmpty()) || ((additionalFilter != null) && (additionalFilter.length() != 0))) {
/*     */       return;
/*     */     }
/*     */ 
/* 504 */     whereClause = new StringBuffer();
/* 505 */     this.m_constraints = new IdcVector();
/*     */ 
/* 507 */     boolean pastFirstField = false;
/* 508 */     int size = cKeys.size();
/* 509 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 512 */       String columnName = (String)cKeys.elementAt(i);
/* 513 */       FieldInfo info = new FieldInfo();
/* 514 */       boolean isPresent = rsTable.getFieldInfo(columnName, info);
/* 515 */       if (!isPresent)
/*     */       {
/* 518 */         int index = findFieldIndex(columnName, rsTable);
/* 519 */         if (index < 0)
/*     */         {
/* 521 */           String msg = LocaleUtils.encodeMessage("csInvalidMergeColumn", null, columnName);
/*     */ 
/* 523 */           throw new ServiceException(msg);
/*     */         }
/* 525 */         rsTable.getIndexFieldInfo(index, info);
/*     */       }
/* 527 */       this.m_constraints.addElement(info);
/* 528 */       this.m_updateParameters.addElement(info);
/* 529 */       this.m_parameters.addElement(info);
/*     */ 
/* 532 */       String varName = info.m_name;
/*     */ 
/* 535 */       if (pastFirstField)
/*     */       {
/* 537 */         whereClause.append(" AND ");
/*     */       }
/*     */       else
/*     */       {
/* 541 */         pastFirstField = true;
/*     */       }
/*     */ 
/* 545 */       whereClause.append(varName);
/* 546 */       whereClause.append(" = ?");
/*     */     }
/*     */ 
/* 550 */     if ((additionalFilter != null) && (additionalFilter.length() != 0))
/*     */     {
/* 552 */       if (pastFirstField)
/*     */       {
/* 554 */         whereClause.append(" AND ");
/*     */       }
/* 556 */       whereClause.append(" (" + additionalFilter + ")");
/*     */     }
/*     */ 
/* 559 */     String whereStr = whereClause.toString();
/*     */ 
/* 562 */     StringBuffer delSql = new StringBuffer("DELETE FROM ");
/* 563 */     delSql.append(tableName);
/*     */ 
/* 565 */     this.m_deleteAllSql = delSql.toString();
/* 566 */     delSql.append(" WHERE ");
/* 567 */     delSql.append(whereStr);
/* 568 */     this.m_deleteSql = delSql.toString();
/*     */ 
/* 571 */     StringBuffer selSql = new StringBuffer("SELECT * FROM ");
/* 572 */     selSql.append(tableName);
/* 573 */     selSql.append(" WHERE ");
/* 574 */     selSql.append(whereStr);
/*     */ 
/* 576 */     this.m_selectSql = selSql.toString();
/*     */ 
/* 579 */     StringBuffer updateSql = new StringBuffer();
/* 580 */     updateSql.append("UPDATE ");
/* 581 */     updateSql.append(tableName);
/* 582 */     updateSql.append(" SET ");
/* 583 */     updateSql.append(updateClauses);
/* 584 */     updateSql.append(" WHERE ");
/* 585 */     updateSql.append(whereStr);
/*     */ 
/* 587 */     this.m_updateSql = updateSql.toString();
/*     */   }
/*     */ 
/*     */   public int findFieldIndex(String name, ResultSet rset)
/*     */   {
/* 593 */     int numFields = rset.getNumFields();
/* 594 */     int index = -1;
/* 595 */     FieldInfo fi = new FieldInfo();
/* 596 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 598 */       rset.getIndexFieldInfo(i, fi);
/* 599 */       if (!fi.m_name.equalsIgnoreCase(name))
/*     */         continue;
/* 601 */       index = i;
/* 602 */       break;
/*     */     }
/*     */ 
/* 605 */     return index;
/*     */   }
/*     */ 
/*     */   protected void reportProgress(int type, String msg, float amtDone, float max)
/*     */   {
/* 610 */     if (this.m_progress == null)
/*     */       return;
/* 612 */     this.m_progress.reportProgress(type, msg, amtDone, max);
/*     */   }
/*     */ 
/*     */   protected String createQuery(String query, Vector fInfos, Parameters args, ValueMapData data)
/*     */     throws DataException
/*     */   {
/* 620 */     StringBuffer buff = new StringBuffer();
/*     */ 
/* 622 */     int prevIndex = 0;
/* 623 */     int index = 0;
/*     */ 
/* 625 */     int size = fInfos.size();
/* 626 */     for (int i = 0; (index = query.indexOf(63, prevIndex)) >= 0; ++i)
/*     */     {
/* 628 */       if (i == size)
/*     */       {
/* 630 */         String msg = LocaleUtils.encodeMessage("csCouldNotBindParameters", null, query);
/*     */ 
/* 632 */         throw new DataException(msg);
/*     */       }
/* 634 */       buff.append(query.substring(prevIndex, index));
/*     */ 
/* 637 */       FieldInfo fi = (FieldInfo)fInfos.elementAt(i);
/* 638 */       String value = args.get(fi.m_name);
/* 639 */       if (data != null)
/*     */       {
/*     */         try
/*     */         {
/* 643 */           value = data.getMappedValue(fi.m_name, value);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 647 */           throw new DataException("!csUnableToRetriveValue", e);
/*     */         }
/*     */       }
/* 650 */       QueryUtils.appendParam(buff, fi.m_type, value);
/* 651 */       prevIndex = index + 1;
/*     */     }
/*     */ 
/* 654 */     buff.append(query.substring(prevIndex));
/* 655 */     return buff.toString();
/*     */   }
/*     */ 
/*     */   public void exportTable(DataBinder binder) throws DataException, ServiceException
/*     */   {
/* 660 */     exportTable(binder, false);
/*     */   }
/*     */ 
/*     */   public boolean exportTable(DataBinder binder, boolean skipEmptyResults, boolean doCreateParentDir)
/*     */     throws DataException, ServiceException
/*     */   {
/* 666 */     ResultSet modifiedRset = getTable(binder.getLocal("tableName"), binder.getLocal("fullQueryString"));
/*     */ 
/* 668 */     binder.addResultSet("ExportResults", modifiedRset);
/* 669 */     ResultSet deleteRset = null;
/* 670 */     if (binder.getLocal("deletedTableName") != null)
/*     */     {
/* 672 */       deleteRset = getTable(null, binder.getLocal("deletedTableQuery"));
/*     */ 
/* 674 */       binder.addResultSet("DeletedRows", deleteRset);
/*     */     }
/* 676 */     if ((!skipEmptyResults) || ((modifiedRset != null) && (!modifiedRset.isEmpty())) || ((deleteRset != null) && (!deleteRset.isEmpty())))
/*     */     {
/* 679 */       if ((deleteRset == null) || (deleteRset.isEmpty()))
/*     */       {
/* 681 */         binder.putLocal("deletedRowsEmpty", "1");
/*     */       }
/* 683 */       String dir = binder.getLocal("archiveDir");
/* 684 */       if (doCreateParentDir == true)
/*     */       {
/* 687 */         FileUtils.checkOrCreateDirectory(dir, 0);
/*     */       }
/*     */ 
/* 690 */       String file = binder.getLocal("archiveFileName");
/*     */ 
/* 693 */       String[] localFieldKeys = { "archiveDir", "archiveFileName", "isExport" };
/* 694 */       for (int i = 0; i < localFieldKeys.length; ++i)
/*     */       {
/* 696 */         binder.removeLocal(localFieldKeys[i]);
/*     */       }
/* 698 */       ResourceUtils.serializeDataBinder(dir, file, binder, true, false);
/* 699 */       return true;
/*     */     }
/* 701 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean exportTable(DataBinder binder, boolean skipEmptyResults)
/*     */     throws DataException, ServiceException
/*     */   {
/* 707 */     return exportTable(binder, skipEmptyResults, false);
/*     */   }
/*     */ 
/*     */   public ResultSet getTable(String name)
/*     */     throws DataException
/*     */   {
/* 715 */     return getTable(name, null);
/*     */   }
/*     */ 
/*     */   public ResultSet getTable(String tableName, String query)
/*     */     throws DataException
/*     */   {
/* 721 */     ExportTableResultSet edrset = new ExportTableResultSet();
/*     */ 
/* 723 */     if (query == null)
/*     */     {
/* 725 */       if (!WorkspaceUtils.doesTableExist(this.m_workspace, tableName, null))
/*     */       {
/* 727 */         String msg = LocaleUtils.encodeMessage("csUnableToLocateDatabaseTable", null, tableName);
/*     */ 
/* 729 */         throw new DataException(msg);
/*     */       }
/*     */ 
/* 732 */       query = "SELECT * FROM " + tableName;
/*     */     }
/* 734 */     if (!edrset.initQuery(query, this.m_workspace))
/*     */     {
/* 736 */       throw new DataException(edrset.getErrorMsg());
/*     */     }
/* 738 */     return edrset;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 744 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96987 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SerializeTable
 * JD-Core Version:    0.5.4
 */