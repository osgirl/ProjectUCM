/*     */ package intradoc.shared.schema;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.QueryUtils;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SchemaTableData extends SchemaData
/*     */ {
/*  31 */   public static String[] TABLE_DEFINITION_COLUMNS = { "ColumnName", "ColumnType", "ColumnLength", "IsPrimaryKey" };
/*     */ 
/*  39 */   public static int COLUMN_NAME_INDEX = 0;
/*  40 */   public static int COLUMN_TYPE_INDEX = 1;
/*  41 */   public static int COLUMN_LENGTH_INDEX = 2;
/*  42 */   public static int PRIMARY_KEY_INDEX = 3;
/*     */ 
/*  44 */   public static String[] TABLE_CHANGE_COLUMNS = { TABLE_DEFINITION_COLUMNS[COLUMN_NAME_INDEX], "ColumnChangeType" };
/*     */ 
/*  50 */   public static int COLUMN_CHANGE_TYPE_INDEX = 1;
/*     */   public FieldInfo[] m_columns;
/*  53 */   public FieldInfo[] m_primaryKeyColumns = new FieldInfo[0];
/*     */   public FieldInfo m_createTimestampColumn;
/*     */   public FieldInfo m_modifyTimestampColumn;
/*  59 */   protected Hashtable m_columnNameInfoMap = null;
/*     */ 
/*     */   public SchemaTableData()
/*     */   {
/*  64 */     this.m_localDataCruftKeys.addAll(Arrays.asList(new String[] { "schColumnList", "IsCreateTable", "IsAddExistingTable" }));
/*     */ 
/*  68 */     this.m_baseResultSets.add("TableDefinition");
/*     */   }
/*     */ 
/*     */   public String getNameField()
/*     */   {
/*  74 */     return "schTableName";
/*     */   }
/*     */ 
/*     */   public String getTimestampField()
/*     */   {
/*  80 */     return "schTableLastLoaded";
/*     */   }
/*     */ 
/*     */   public String getIsUpToDateField()
/*     */   {
/*  86 */     return "schTableIsUpToDate";
/*     */   }
/*     */ 
/*     */   public void updateRow(Vector row)
/*     */   {
/*  92 */     for (int i = 0; i < SchemaTableConfig.TABLE_COLUMNS.length; ++i)
/*     */     {
/*  95 */       String column = SchemaTableConfig.TABLE_COLUMNS[i];
/*     */       String element;
/*     */       String element;
/*  96 */       if (column.equals("schTableCanonicalName"))
/*     */       {
/*  98 */         element = canonicalName(get("schTableName"));
/*     */       }
/*     */       else
/*     */       {
/*     */         String element;
/* 100 */         if (column.equals("schColumnList"))
/*     */         {
/* 102 */           DataResultSet drset = getResultSet("TableDefinition");
/* 103 */           if (drset == null)
/*     */           {
/* 105 */             String element = "";
/* 106 */             Report.trace("schemastorage", "TableDefinition missing for " + this.m_name, null);
/*     */           }
/*     */           else
/*     */           {
/* 111 */             Vector list = new IdcVector();
/* 112 */             for (drset.first(); drset.isRowPresent(); drset.next())
/*     */             {
/* 114 */               Properties props = drset.getCurrentRowProps();
/* 115 */               String name = props.getProperty("ColumnName");
/* 116 */               list.addElement(name);
/*     */             }
/* 118 */             element = StringUtils.createString(list, ',', '^');
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 123 */           element = get(SchemaTableConfig.TABLE_COLUMNS[i]);
/*     */         }
/*     */       }
/* 125 */       if (element == null)
/*     */       {
/* 127 */         element = "";
/*     */       }
/* 129 */       row.setElementAt(element, this.m_resultSet.m_indexes[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initIndexes()
/*     */   {
/* 136 */     super.initIndexes();
/*     */   }
/*     */ 
/*     */   public void update(DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 144 */     String tableName = binder.getLocal(getNameField());
/* 145 */     DataResultSet tableDef = (DataResultSet)binder.getResultSet("TableDefinition");
/*     */ 
/* 147 */     if (tableDef == null)
/*     */     {
/* 149 */       String msg = LocaleUtils.encodeMessage("apSchMissingTableDefinition", null, tableName);
/*     */ 
/* 151 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 155 */     ResultSetUtils.createInfoList(tableDef, TABLE_DEFINITION_COLUMNS, true);
/*     */ 
/* 159 */     String createTimestampColumn = binder.getLocal("schTableRowCreateTimestamp");
/*     */ 
/* 161 */     if ((createTimestampColumn != null) && (createTimestampColumn.equalsIgnoreCase("<none>")))
/*     */     {
/* 164 */       createTimestampColumn = null;
/* 165 */       binder.removeLocal("schTableRowCreateTimestamp");
/*     */     }
/* 167 */     String modifyTimestampColumn = binder.getLocal("schTableRowModifyTimestamp");
/*     */ 
/* 169 */     if ((modifyTimestampColumn != null) && (modifyTimestampColumn.equalsIgnoreCase("<none>")))
/*     */     {
/* 172 */       modifyTimestampColumn = null;
/* 173 */       binder.removeLocal("schTableRowModifyTimestamp");
/*     */     }
/* 175 */     if ((createTimestampColumn != null) && (modifyTimestampColumn != null) && (createTimestampColumn.equalsIgnoreCase(modifyTimestampColumn)))
/*     */     {
/* 180 */       throw new DataException("!apSchSameTimestampColumns");
/*     */     }
/* 182 */     super.update(binder);
/*     */ 
/* 184 */     if ((createTimestampColumn != null) && (this.m_createTimestampColumn == null))
/*     */     {
/* 187 */       String msg = LocaleUtils.encodeMessage("apSchemaCreateTimestampColumnMissing", null, createTimestampColumn);
/*     */ 
/* 190 */       throw new DataException(msg);
/*     */     }
/* 192 */     if ((modifyTimestampColumn == null) || (this.m_modifyTimestampColumn != null)) {
/*     */       return;
/*     */     }
/* 195 */     String msg = LocaleUtils.encodeMessage("apSchemaModifyTimestampColumnMissing", null, modifyTimestampColumn);
/*     */ 
/* 198 */     throw new DataException(msg);
/*     */   }
/*     */ 
/*     */   public void updateEx(DataBinder binder)
/*     */   {
/* 205 */     DataResultSet tableDef = (DataResultSet)binder.getResultSet("TableDefinition");
/*     */ 
/* 208 */     this.m_columns = new FieldInfo[0];
/* 209 */     this.m_primaryKeyColumns = new FieldInfo[0];
/* 210 */     this.m_createTimestampColumn = null;
/* 211 */     this.m_modifyTimestampColumn = null;
/* 212 */     boolean hasError = false;
/* 213 */     if (tableDef != null)
/*     */     {
/* 215 */       Hashtable fieldInfoHashtable = new Hashtable();
/* 216 */       FieldInfo[] fis = null;
/*     */       try
/*     */       {
/* 219 */         fis = ResultSetUtils.createInfoList(tableDef, TABLE_DEFINITION_COLUMNS, true);
/*     */       }
/*     */       catch (DataException ignore)
/*     */       {
/* 224 */         hasError = true;
/*     */       }
/*     */ 
/* 227 */       int fieldCount = tableDef.getNumRows();
/*     */ 
/* 229 */       FieldInfo[] fieldInfos = new FieldInfo[fieldCount];
/* 230 */       Vector primaryKeyColumns = new IdcVector();
/* 231 */       for (int i = 0; (!hasError) && (i < fieldInfos.length); ++i)
/*     */       {
/* 234 */         FieldInfo info = new FieldInfo();
/* 235 */         Vector v = tableDef.getRowValues(i);
/*     */ 
/* 237 */         info.m_name = ((String)v.elementAt(fis[COLUMN_NAME_INDEX].m_index));
/*     */ 
/* 239 */         String typeStr = (String)v.elementAt(fis[COLUMN_TYPE_INDEX].m_index);
/*     */ 
/* 241 */         String lengthStr = (String)v.elementAt(fis[COLUMN_LENGTH_INDEX].m_index);
/*     */ 
/* 243 */         String isPrimaryKeyStr = (String)v.elementAt(fis[PRIMARY_KEY_INDEX].m_index);
/*     */ 
/* 245 */         boolean isPrimaryKey = StringUtils.convertToBool(isPrimaryKeyStr, false);
/* 246 */         info.m_type = QueryUtils.convertInfoStringToType(typeStr);
/* 247 */         info.m_maxLen = NumberUtils.parseInteger(lengthStr, 0);
/* 248 */         info.m_isFixedLen = true;
/* 249 */         fieldInfos[i] = info;
/* 250 */         fieldInfoHashtable.put(info.m_name.toLowerCase(), info);
/*     */ 
/* 252 */         if (!isPrimaryKey)
/*     */           continue;
/* 254 */         primaryKeyColumns.addElement(info);
/*     */       }
/*     */ 
/* 258 */       FieldInfo[] primaryKey = new FieldInfo[primaryKeyColumns.size()];
/* 259 */       primaryKeyColumns.copyInto(primaryKey);
/*     */ 
/* 261 */       String createTimestampColumn = binder.getLocal("schTableRowCreateTimestamp");
/*     */ 
/* 263 */       String modifyTimestampColumn = binder.getLocal("schTableRowModifyTimestamp");
/*     */ 
/* 266 */       if ((createTimestampColumn != null) && (modifyTimestampColumn != null) && (createTimestampColumn.equals(modifyTimestampColumn)))
/*     */       {
/* 271 */         hasError = true;
/*     */       }
/*     */ 
/* 274 */       if (!hasError)
/*     */       {
/* 277 */         if (createTimestampColumn != null)
/*     */         {
/* 279 */           this.m_createTimestampColumn = ((FieldInfo)fieldInfoHashtable.get(createTimestampColumn.toLowerCase()));
/*     */         }
/*     */ 
/* 282 */         if (modifyTimestampColumn != null)
/*     */         {
/* 284 */           this.m_modifyTimestampColumn = ((FieldInfo)fieldInfoHashtable.get(modifyTimestampColumn.toLowerCase()));
/*     */         }
/*     */ 
/* 287 */         this.m_columns = fieldInfos;
/* 288 */         this.m_primaryKeyColumns = primaryKey;
/* 289 */         this.m_columnNameInfoMap = fieldInfoHashtable;
/*     */       }
/*     */     }
/* 292 */     super.updateEx(binder);
/*     */   }
/*     */ 
/*     */   protected void removeServerCruft(DataBinder binder)
/*     */   {
/* 298 */     super.removeServerCruft(binder);
/* 299 */     binder.removeLocal("IsCreateTable");
/* 300 */     binder.removeLocal("TableExists");
/* 301 */     if (this.m_name == null)
/*     */       return;
/* 303 */     binder.removeResultSet(this.m_name);
/*     */   }
/*     */ 
/*     */   public void populateBinder(DataBinder binder)
/*     */   {
/* 310 */     super.populateBinder(binder);
/*     */   }
/*     */ 
/*     */   public synchronized void notifyChanged(ResultSet deletedRows, ResultSet changedRows)
/*     */     throws DataException
/*     */   {
/* 327 */     SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*     */ 
/* 329 */     Hashtable notifiedViews = new Hashtable();
/* 330 */     for (views.first(); views.isRowPresent(); views.next())
/*     */     {
/* 332 */       SchemaViewData data = (SchemaViewData)views.getData();
/*     */ 
/* 334 */       String viewType = data.get("schViewType");
/* 335 */       String tableName = data.get("schTableName");
/* 336 */       if ((viewType.equals("table")) && (tableName != null) && (tableName.equalsIgnoreCase(this.m_name)) && (notifiedViews.get(data.m_name) == null))
/*     */       {
/* 340 */         Report.trace("schemacache", "SchemaTableData.notifyChanged() notifying view '" + data.m_name + "' of change on table '" + this.m_name + "'", null);
/*     */ 
/* 343 */         data.markCacheDirty(data, null, null);
/* 344 */         notifiedViews.put(data.m_name, data);
/*     */       }
/*     */       else
/*     */       {
/* 348 */         SchemaLoader loader = views.findLoader(data, null, null);
/* 349 */         if (loader == null)
/*     */         {
/* 351 */           Report.trace("schemacache", "SchemaTableData.notifyChanged() could not find loader for view '" + data.m_name + "' of change on table '" + this.m_name + "'", null);
/*     */         }
/*     */         else
/*     */         {
/* 356 */           SchemaRelationData[] relations = loader.getParentRelations(data);
/* 357 */           if (relations == null)
/*     */           {
/* 359 */             Report.trace("schemacache", "SchemaTableData.notifyChanged() could not find relations for view '" + data.m_name + "' of change on table '" + this.m_name + "'", null);
/*     */           }
/*     */           else
/*     */           {
/* 364 */             for (int i = 0; i < relations.length; ++i)
/*     */             {
/* 366 */               SchemaViewData[] parentViews = loader.getParentViews(relations[i]);
/*     */ 
/* 368 */               for (int j = 0; j < parentViews.length; ++j)
/*     */               {
/* 370 */                 viewType = parentViews[j].get("schViewType");
/* 371 */                 tableName = parentViews[j].get("schTableName");
/* 372 */                 if ((!viewType.equals("table")) || (!tableName.equalsIgnoreCase(this.m_name)) || (notifiedViews.get(parentViews[j].m_name) != null)) {
/*     */                   continue;
/*     */                 }
/*     */ 
/* 376 */                 Report.trace("schemacache", "SchemaTableData.notifyChanged() notifying view '" + parentViews[j].m_name + "' of change on table '" + this.m_name + "'", null);
/*     */ 
/* 380 */                 parentViews[j].markCacheDirty(parentViews[j], null, null);
/* 381 */                 notifiedViews.put(parentViews[j].m_name, parentViews);
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public Vector checkSynchronization(FieldInfo[] dbInfos)
/*     */   {
/* 399 */     Vector mismatches = new IdcVector();
/* 400 */     Hashtable myMap = (Hashtable)this.m_columnNameInfoMap.clone();
/* 401 */     for (int i = 0; i < dbInfos.length; ++i)
/*     */     {
/* 403 */       FieldInfo dbInfo = dbInfos[i];
/* 404 */       FieldInfo myInfo = (FieldInfo)myMap.get(dbInfo.m_name.toLowerCase());
/*     */ 
/* 407 */       if (myInfo == null)
/*     */       {
/* 409 */         if (SystemUtils.m_verbose)
/*     */         {
/* 411 */           Report.debug("schemastorage", "checkSynchronization(): adding missing column '" + dbInfo.m_name + "'", null);
/*     */         }
/*     */ 
/* 414 */         mismatches.addElement(dbInfo);
/*     */       }
/*     */       else
/*     */       {
/* 418 */         if ((((myInfo.m_type != dbInfo.m_type) || (myInfo.m_maxLen != dbInfo.m_maxLen))) && 
/* 421 */           (myInfo.m_type == dbInfo.m_type) && (myInfo.m_type != 1) && (myInfo.m_type != 3) && (myInfo.m_type != 4) && (myInfo.m_type != 5))
/*     */         {
/* 427 */           if (SystemUtils.m_verbose)
/*     */           {
/* 429 */             Report.debug("schemastorage", "checkSynchronization(): adding mismatched '" + dbInfo.m_name + "' types: " + myInfo.m_type + " " + dbInfo.m_type + "  lengths: " + myInfo.m_maxLen + " " + dbInfo.m_maxLen, null);
/*     */           }
/*     */ 
/* 436 */           mismatches.addElement(myInfo);
/*     */         }
/*     */ 
/* 439 */         myMap.remove(dbInfo.m_name.toLowerCase());
/*     */       }
/*     */     }
/*     */ 
/* 443 */     Enumeration en = myMap.keys();
/* 444 */     while (en.hasMoreElements())
/*     */     {
/* 446 */       String key = (String)en.nextElement();
/* 447 */       FieldInfo info = (FieldInfo)myMap.get(key);
/* 448 */       mismatches.addElement(info);
/* 449 */       if (SystemUtils.m_verbose)
/*     */       {
/* 451 */         Report.debug("schemastorage", "checkSynchronization(): adding new column '" + info.m_name + "'", null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 457 */     return mismatches;
/*     */   }
/*     */ 
/*     */   public FieldInfo getFieldInfo(String name)
/*     */   {
/* 466 */     return (FieldInfo)this.m_columnNameInfoMap.get(name.toLowerCase());
/*     */   }
/*     */ 
/*     */   public FieldInfo requireFieldInfo(String name)
/*     */     throws DataException
/*     */   {
/* 477 */     FieldInfo info = (FieldInfo)this.m_columnNameInfoMap.get(name.toLowerCase());
/* 478 */     if (info == null)
/*     */     {
/* 480 */       String msg = LocaleUtils.encodeMessage("syColumnDoesNotExist", null, name);
/*     */ 
/* 482 */       throw new DataException(msg);
/*     */     }
/* 484 */     return info;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 490 */     return this.m_data.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 495 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75786 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaTableData
 * JD-Core Version:    0.5.4
 */