/*      */ package intradoc.data;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.util.IdcConcurrentHashMap;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ 
/*      */ public class IdcDataSource
/*      */ {
/*      */   public static final int QUERY_TYPE_SELECT = 1;
/*      */   public static final int QUERY_TYPE_INSERT = 2;
/*      */   public static final int QUERY_TYPE_UPDATE = 4;
/*      */   public static final int QUERY_TYPE_DELETE = 8;
/*      */   public static final int QUERY_TYPE_CALLABLE = 16;
/*      */   public static final int QUERY_TYPE_CREATE = 32;
/*      */   public static final int QUERY_TYPE_ALTER = 64;
/*      */   public String m_dataSourceName;
/*      */   public List m_insertQuery;
/*      */   public Map<String, String> m_dependentInsertQueries;
/*      */   public List m_updateQuery;
/*      */   public Map<String, String> m_dependentUpdateQueries;
/*      */   public List m_deleteQuery;
/*      */   public Map<String, String> m_dependentDeleteQueries;
/*      */   public String m_selectQuery;
/*      */   public Map<String, String> m_dependentSelectQueries;
/*      */   public Map m_fieldMap;
/*      */   public Map<String, Map<String, String>> m_drivingFields;
/*      */   public Map<String, String> m_MergeToParentRSFields;
/*      */ 
/*      */   public IdcDataSource()
/*      */   {
/*   62 */     this.m_fieldMap = new HashMap();
/*      */ 
/*   64 */     this.m_drivingFields = new HashMap();
/*   65 */     this.m_MergeToParentRSFields = new HashMap();
/*      */   }
/*      */ 
/*      */   public void initQueries(Workspace ws, String name, String[] tableStr, String[] selectList, String[] relationStrs, String[] filters, String[] fieldMapStrs) throws DataException
/*      */   {
/*   70 */     this.m_dataSourceName = name;
/*   71 */     this.m_insertQuery = new ArrayList();
/*   72 */     this.m_dependentInsertQueries = new HashMap();
/*      */ 
/*   74 */     this.m_updateQuery = new ArrayList();
/*   75 */     this.m_dependentUpdateQueries = new HashMap();
/*      */ 
/*   77 */     this.m_deleteQuery = new ArrayList();
/*   78 */     this.m_dependentDeleteQueries = new HashMap();
/*      */ 
/*   80 */     this.m_dependentSelectQueries = new HashMap();
/*      */ 
/*   82 */     DatabaseTable[] tables = getTablesFromString(tableStr, filters, ws);
/*      */ 
/*   84 */     DatabaseTableRelation[] relations = getTableRelations(relationStrs, tables);
/*   85 */     this.m_fieldMap = getFieldMapFromStrings(fieldMapStrs);
/*   86 */     int typeField = 15;
/*   87 */     initQueries(ws, selectList, tables, relations, this.m_fieldMap, typeField, false, null);
/*      */   }
/*      */ 
/*      */   public void addDependentQueries(String name, Workspace ws, String[] tableStrs, String[] selectList, String[] relationStrs, String[] filters, String[] fieldMapStrs, int typeFlag)
/*      */     throws DataException
/*      */   {
/*   93 */     if ((name == null) && ((typeFlag & 0x1) > 0))
/*      */     {
/*   95 */       IdcStringBuilder builder = new IdcStringBuilder();
/*   96 */       boolean hasValue = false;
/*   97 */       for (String table : tableStrs)
/*      */       {
/*   99 */         if (hasValue)
/*      */         {
/*  101 */           builder.append(',');
/*      */         }
/*      */         else
/*      */         {
/*  105 */           hasValue = true;
/*      */         }
/*  107 */         builder.append(table);
/*      */       }
/*  109 */       name = builder.toString();
/*      */     }
/*      */ 
/*  112 */     DatabaseTable[] tables = getTablesFromString(tableStrs, filters, ws);
/*  113 */     Map fieldMap = getFieldMapFromStrings(fieldMapStrs);
/*      */ 
/*  115 */     if ((typeFlag & 0x1) > 0)
/*      */     {
/*  117 */       changeRelationToFilter(relationStrs, tables);
/*  118 */       initQueries(ws, selectList, tables, null, fieldMap, typeFlag, true, name);
/*      */     }
/*      */     else
/*      */     {
/*  122 */       DatabaseTableRelation[] relations = getTableRelations(relationStrs, tables);
/*  123 */       initQueries(ws, selectList, tables, relations, fieldMap, typeFlag, true, name);
/*      */     }
/*      */   }
/*      */ 
/*      */   public DatabaseTableRelation[] getTableRelations(String[] relationStrs, DatabaseTable[] tables) throws DataException
/*      */   {
/*  129 */     List relations = new ArrayList();
/*      */ 
/*  131 */     for (int i = 0; i < relationStrs.length; ++i)
/*      */     {
/*  133 */       if (relationStrs[i].trim().length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*  137 */       List relationStrList = StringUtils.makeListFromSequence(relationStrs[i], ',', ',', 32);
/*      */ 
/*  139 */       for (String relationStr : relationStrList)
/*      */       {
/*  141 */         DatabaseTableRelation relation = new DatabaseTableRelation();
/*  142 */         List relationList = StringUtils.makeListFromSequence(relationStr, '=', '=', 32);
/*  143 */         if (relationList.size() != 2)
/*      */         {
/*  146 */           throw new DataException(null, "csMalformedRelationList", new Object[] { relationStr });
/*      */         }
/*  148 */         List tableColumns = StringUtils.makeListFromSequence((CharSequence)relationList.get(0), '.', '.', 32);
/*  149 */         if (tableColumns.size() != 2)
/*      */         {
/*  151 */           throw new DataException(null, "csMalformedParentTableColumn", new Object[0]);
/*      */         }
/*  153 */         String tableName = (String)tableColumns.get(0);
/*  154 */         for (DatabaseTable table : tables)
/*      */         {
/*  156 */           if (!table.m_alias.equalsIgnoreCase(tableName))
/*      */             continue;
/*  158 */           relation.m_parentTable = table;
/*  159 */           break;
/*      */         }
/*      */ 
/*  162 */         relation.m_parentTableColumn = ((String)tableColumns.get(1));
/*      */ 
/*  164 */         tableColumns = StringUtils.makeListFromSequence((CharSequence)relationList.get(1), '.', '.', 32);
/*  165 */         if (tableColumns.size() != 2)
/*      */         {
/*  167 */           throw new DataException(null, "csMalformedChildTableColumn", new Object[] { tableColumns });
/*      */         }
/*      */ 
/*  170 */         tableName = (String)tableColumns.get(0);
/*  171 */         for (DatabaseTable table : tables)
/*      */         {
/*  173 */           if (!table.m_alias.equalsIgnoreCase(tableName))
/*      */             continue;
/*  175 */           relation.m_childTable = table;
/*  176 */           break;
/*      */         }
/*      */ 
/*  179 */         relation.m_childTableColumn = ((String)tableColumns.get(1));
/*  180 */         if (!relations.contains(relation))
/*      */         {
/*  182 */           relations.add(relation);
/*      */         }
/*      */       }
/*      */     }
/*  186 */     return (DatabaseTableRelation[])relations.toArray(new DatabaseTableRelation[0]);
/*      */   }
/*      */ 
/*      */   public DatabaseTable[] getTablesFromString(String[] tableStr, String[] filters, Workspace ws) throws DataException
/*      */   {
/*  191 */     List tableList = new ArrayList();
/*  192 */     HashMap addedTables = new HashMap();
/*  193 */     for (int i = 0; i < tableStr.length; ++i)
/*      */     {
/*  195 */       if (addedTables.containsKey(tableStr[i].toLowerCase())) {
/*      */         continue;
/*      */       }
/*      */ 
/*  199 */       List tableAlias = StringUtils.makeListFromSequence(tableStr[i], ' ', ' ', 0);
/*  200 */       DatabaseTable table = new DatabaseTable();
/*  201 */       table.m_name = ((String)tableAlias.get(0));
/*  202 */       if (tableAlias.size() > 1)
/*      */       {
/*  204 */         table.m_alias = ((String)tableAlias.get(1));
/*      */       }
/*      */       else
/*      */       {
/*  208 */         table.m_alias = table.m_name;
/*      */       }
/*      */ 
/*  211 */       table.m_columnNames = WorkspaceUtils.getColumnList(table.m_name, ws, null);
/*  212 */       table.m_columns = ws.getColumnList(table.m_name);
/*  213 */       table.m_pkColumnNames = ws.getPrimaryKeys(table.m_name);
/*  214 */       table.m_indexes = ws.getIndexList(table.m_name);
/*  215 */       if (table.m_pkColumnNames != null)
/*      */       {
/*  217 */         table.m_primaryKeys = new FieldInfo[table.m_pkColumnNames.length];
/*  218 */         for (int j = 0; j < table.m_pkColumnNames.length; ++j)
/*      */         {
/*  220 */           for (FieldInfo fi : table.m_columns)
/*      */           {
/*  222 */             if (!table.m_pkColumnNames[j].equalsIgnoreCase(fi.m_name))
/*      */               continue;
/*  224 */             table.m_primaryKeys[j] = fi;
/*  225 */             break;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  230 */       tableList.add(table);
/*  231 */       addedTables.put(tableStr[i].toLowerCase(), "1");
/*      */     }
/*      */ 
/*  234 */     DatabaseTable[] tables = (DatabaseTable[])tableList.toArray(new DatabaseTable[0]);
/*      */ 
/*  237 */     for (int i = 0; i < filters.length; ++i)
/*      */     {
/*  239 */       List filterItems = StringUtils.makeListFromSequence(filters[i], ',', ',', 32);
/*  240 */       label826: for (String filterItem : filterItems)
/*      */       {
/*  242 */         DatabaseQueryFilter filter = new DatabaseQueryFilter();
/*      */ 
/*  244 */         char[] filterChars = filterItem.toCharArray();
/*  245 */         int lastIndex = 0;
/*  246 */         boolean hasTableAlias = false;
/*  247 */         boolean hasColumnName = false;
/*  248 */         boolean hasOp = false;
/*  249 */         for (int j = 0; j < filterChars.length; ++j)
/*      */         {
/*  251 */           if (Character.isLetterOrDigit(filterChars[j])) continue; if (filterChars[j] == '_') {
/*      */             continue;
/*      */           }
/*      */ 
/*  255 */           if ((!hasTableAlias) && (filterChars[j] == '.'))
/*      */           {
/*  257 */             hasTableAlias = true;
/*  258 */             filter.m_table = new DatabaseTable();
/*  259 */             filter.m_table.m_alias = new String(filterChars, lastIndex, j - lastIndex);
/*  260 */             lastIndex = j + 1;
/*      */           }
/*  262 */           else if (!hasColumnName)
/*      */           {
/*  264 */             hasColumnName = true;
/*  265 */             filter.m_columnName = new String(filterChars, lastIndex, j - lastIndex);
/*  266 */             lastIndex = j;
/*      */           } else {
/*  268 */             if (hasOp)
/*      */               continue;
/*  270 */             for (; ; ++j) { if (j >= filterChars.length)
/*      */                 break label826;
/*  272 */               if ((filterChars[j] != '\'') && (filterChars[j] != '{') && (filterChars[j] != '?') && (!Character.isLetterOrDigit(filterChars[j]))) {
/*      */                 continue;
/*      */               }
/*  275 */               filter.m_operator = new String(filterChars, lastIndex, j - lastIndex).trim();
/*  276 */               filter.m_value = new String(filterChars, j, filterChars.length - j).trim();
/*  277 */               char[] tmpChars = filter.m_value.toCharArray();
/*  278 */               if ((tmpChars[0] != '\'') || (tmpChars[(tmpChars.length - 1)] != '\'')) {
/*      */                 break label826;
/*      */               }
/*  281 */               IdcStringBuilder builder = new IdcStringBuilder();
/*      */ 
/*  283 */               lastIndex = 0;
/*  284 */               for (int k = 0; k < tmpChars.length - 1; ++k)
/*      */               {
/*  286 */                 if ((tmpChars[k] != '\'') || (tmpChars[(k + 1)] != '\''))
/*      */                   continue;
/*  288 */                 builder.append(tmpChars, lastIndex, k - lastIndex);
/*  289 */                 lastIndex = k + 2;
/*      */               }
/*      */ 
/*  292 */               if (lastIndex < tmpChars.length - 1)
/*      */               {
/*  294 */                 builder.append(tmpChars, lastIndex + 1, tmpChars.length - lastIndex - 2);
/*      */               }
/*  296 */               filter.m_value = builder.toString();
/*  297 */               break label826: }
/*      */ 
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  307 */         if (filter.m_table != null)
/*      */         {
/*  309 */           boolean foundIt = false;
/*  310 */           for (DatabaseTable table : tables)
/*      */           {
/*  312 */             if (!table.m_alias.equalsIgnoreCase(filter.m_table.m_alias))
/*      */               continue;
/*  314 */             filter.m_table = table;
/*  315 */             table.m_filters.add(filter);
/*  316 */             int index = StringUtils.findStringIndexEx(table.m_columnNames, filter.m_columnName, true);
/*  317 */             if (index >= 0)
/*      */             {
/*  319 */               filter.m_column = table.m_columns[index];
/*      */             }
/*  321 */             foundIt = true;
/*  322 */             break;
/*      */           }
/*      */ 
/*  326 */           if (!foundIt)
/*      */           {
/*  328 */             throw new DataException(null, "csDSTableFilterDefErrorTableNotFound", new Object[] { filters[i] });
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  334 */           for (DatabaseTable table : tables)
/*      */           {
/*  336 */             int index = StringUtils.findStringIndexEx(table.m_columnNames, filter.m_columnName, true);
/*  337 */             if (index < 0)
/*      */               continue;
/*  339 */             filter.m_table = table;
/*  340 */             filter.m_column = table.m_columns[index];
/*  341 */             table.m_filters.add(filter);
/*      */           }
/*      */         }
/*      */ 
/*  345 */         filter.m_filter = filterItem;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  350 */     return tables;
/*      */   }
/*      */ 
/*      */   public Map getFieldMapFromStrings(String[] fieldMapStrs) throws DataException
/*      */   {
/*  355 */     IdcConcurrentHashMap fieldMap = new IdcConcurrentHashMap(false);
/*  356 */     if (fieldMapStrs == null)
/*      */     {
/*  358 */       return fieldMap;
/*      */     }
/*  360 */     for (String fieldAlias : fieldMapStrs)
/*      */     {
/*  362 */       List fieldAliasList = StringUtils.makeListFromSequence(fieldAlias, '=', '=', 32);
/*  363 */       if (fieldAliasList.size() != 2)
/*      */       {
/*  365 */         throw new DataException(null, "csErrorFieldAliasFormat", new Object[] { fieldAlias });
/*      */       }
/*  367 */       fieldMap.put(fieldAliasList.get(0), fieldAliasList.get(1));
/*      */     }
/*  369 */     return fieldMap;
/*      */   }
/*      */ 
/*      */   public void initQueries(Workspace ws, String[] selectList, DatabaseTable[] tables, DatabaseTableRelation[] relations, Map<String, String> fieldMap, int typeFlag, boolean isDependent, String depSelQueryName)
/*      */     throws DataException
/*      */   {
/*  375 */     if ((tables == null) || (tables.length == 0))
/*      */     {
/*  377 */       Report.trace(null, "tables not exist. Cannot create query", null);
/*  378 */       return;
/*      */     }
/*      */ 
/*  381 */     boolean isSelect = (typeFlag & 0x1) > 0;
/*  382 */     boolean isInsert = (typeFlag & 0x2) > 0;
/*  383 */     boolean isDelete = (typeFlag & 0x8) > 0;
/*  384 */     boolean isUpdate = (typeFlag & 0x4) > 0;
/*      */ 
/*  386 */     DataResultSet drset = new DataResultSet(new String[] { "name", "queryStr", "parameters" });
/*  387 */     for (int i = 0; i < tables.length; ++i)
/*      */     {
/*  389 */       FieldInfo[] fis = tables[i].m_columns;
/*  390 */       FieldInfo[] pkFields = tables[i].m_primaryKeys;
/*  391 */       if ((pkFields == null) || (pkFields.length == 0))
/*      */       {
/*  393 */         String errorMsg = LocaleUtils.encodeMessage("csDSTablePrimaryKeyNotDefined", null, tables[i].m_name);
/*  394 */         throw new DataException(errorMsg);
/*      */       }
/*      */ 
/*  397 */       IdcStringBuilder whereClause = new IdcStringBuilder();
/*  398 */       IdcStringBuilder whereParams = new IdcStringBuilder();
/*      */ 
/*  400 */       computeWhereClause(whereClause, new DatabaseTable[] { tables[i] }, null, whereParams);
/*  401 */       if (relations != null)
/*      */       {
/*  403 */         for (DatabaseTableRelation relation : relations)
/*      */         {
/*  405 */           if (!relation.m_childTable.m_alias.equals(tables[i].m_alias))
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/*  410 */           if (whereClause.length() > 0)
/*      */           {
/*  412 */             whereClause.append(" AND ");
/*      */           }
/*      */           else
/*      */           {
/*  416 */             whereClause.append(" WHERE ");
/*      */           }
/*  418 */           whereClause.append(relation.m_childTable.m_alias);
/*  419 */           whereClause.append('.');
/*  420 */           whereClause.append(relation.m_childTableColumn);
/*  421 */           whereClause.append(" = ?");
/*  422 */           if (whereParams.length() > 0)
/*      */           {
/*  424 */             whereParams.append('\n');
/*      */           }
/*      */ 
/*  427 */           FieldInfo fi = tables[i].getColumn(relation.m_childTableColumn);
/*  428 */           whereParams.append(fi.m_name);
/*  429 */           String defValue = FieldInfoUtils.getFieldOption(fi, "defaultValue");
/*  430 */           if (defValue != null)
/*      */           {
/*  432 */             whereParams.append(":");
/*  433 */             whereParams.append(defValue);
/*      */           }
/*  435 */           whereParams.append(' ');
/*  436 */           whereParams.append(fi.getTypeName());
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  441 */       if (whereClause.length() == 0)
/*      */       {
/*  443 */         for (int j = 0; j < pkFields.length; ++j)
/*      */         {
/*  445 */           if (j != 0)
/*      */           {
/*  447 */             whereClause.append(" AND ");
/*      */           }
/*      */           else
/*      */           {
/*  451 */             whereClause.append(" WHERE ");
/*      */           }
/*  453 */           whereClause.append(pkFields[j].m_name);
/*  454 */           whereClause.append(" = ?");
/*      */ 
/*  456 */           if (whereParams.length() > 0)
/*      */           {
/*  458 */             whereParams.append('\n');
/*      */           }
/*  460 */           whereParams.append(pkFields[j].m_name);
/*  461 */           String defValue = (String)fieldMap.get(tables[i].m_name + "." + pkFields[j].m_name + ":default");
/*  462 */           if (defValue == null)
/*      */           {
/*  464 */             defValue = (String)fieldMap.get(pkFields[j].m_name + ":default");
/*      */           }
/*  466 */           if (defValue == null)
/*      */           {
/*  468 */             FieldInfoUtils.getFieldOption(pkFields[j], "defaultValue");
/*      */           }
/*  470 */           if (defValue != null)
/*      */           {
/*  472 */             whereParams.append(":");
/*  473 */             whereParams.append(defValue);
/*      */           }
/*  475 */           whereParams.append(' ');
/*  476 */           whereParams.append(pkFields[j].getTypeName());
/*      */         }
/*      */       }
/*      */ 
/*  480 */       IdcStringBuilder insertBuilder = new IdcStringBuilder();
/*  481 */       insertBuilder.append("INSERT INTO ");
/*  482 */       insertBuilder.append(tables[i].m_name);
/*  483 */       insertBuilder.append("(");
/*  484 */       IdcStringBuilder valueBuilder = new IdcStringBuilder();
/*  485 */       IdcStringBuilder insertParams = new IdcStringBuilder();
/*      */ 
/*  487 */       IdcStringBuilder updateBuilder = new IdcStringBuilder();
/*  488 */       updateBuilder.append("UPDATE ");
/*  489 */       updateBuilder.append(tables[i].m_name);
/*  490 */       updateBuilder.append(" SET ");
/*      */ 
/*  492 */       IdcStringBuilder setClause = new IdcStringBuilder();
/*  493 */       IdcStringBuilder updateParams = new IdcStringBuilder();
/*      */ 
/*  495 */       for (FieldInfo fi : fis)
/*      */       {
/*  498 */         String value = getFieldValue(fi.m_name, tables[i]);
/*      */ 
/*  500 */         String paramKeyName = fi.m_name;
/*  501 */         String tmp = (String)fieldMap.get(tables[i].m_name + "." + fi.m_name + ":alias");
/*  502 */         if (tmp == null)
/*      */         {
/*  504 */           tmp = (String)fieldMap.get(paramKeyName);
/*      */         }
/*  506 */         if (tmp != null)
/*      */         {
/*  508 */           paramKeyName = tmp;
/*      */         }
/*      */ 
/*  511 */         String defaultValue = (String)fieldMap.get(tables[i].m_name + "." + fi.m_name + ":default");
/*  512 */         if (defaultValue == null)
/*      */         {
/*  514 */           defaultValue = (String)fieldMap.get(fi.m_name + ":default");
/*      */         }
/*  516 */         if (defaultValue == null)
/*      */         {
/*  518 */           defaultValue = FieldInfoUtils.getFieldOption(fi, "defaultValue");
/*      */         }
/*      */ 
/*  521 */         String insertExclusion = (String)fieldMap.get(tables[i].m_alias + "." + fi.m_name + ":exclude.insert");
/*      */ 
/*  523 */         if (!StringUtils.convertToBool(insertExclusion, false))
/*      */         {
/*  525 */           if (valueBuilder.length() > 0)
/*      */           {
/*  527 */             insertBuilder.append(',');
/*  528 */             valueBuilder.append(',');
/*      */           }
/*  530 */           insertBuilder.append(fi.m_name);
/*  531 */           valueBuilder.append(value);
/*  532 */           if (value.equals("?"))
/*      */           {
/*  534 */             if (insertParams.length() > 0)
/*      */             {
/*  536 */               insertParams.append('\n');
/*      */             }
/*  538 */             insertParams.append(paramKeyName);
/*      */ 
/*  540 */             if (defaultValue != null)
/*      */             {
/*  542 */               insertParams.append(':');
/*  543 */               insertParams.append(defaultValue);
/*      */             }
/*  545 */             insertParams.append(' ');
/*  546 */             insertParams.append(fi.getTypeName());
/*      */           }
/*      */         }
/*      */ 
/*  550 */         String updateExclusion = (String)fieldMap.get(tables[i].m_alias + "." + fi.m_name + ":exclude.update");
/*  551 */         if (StringUtils.convertToBool(updateExclusion, false))
/*      */           continue;
/*  553 */         if (setClause.length() > 0)
/*      */         {
/*  555 */           setClause.append(',');
/*      */         }
/*  557 */         setClause.append(fi.m_name);
/*  558 */         setClause.append(" = ");
/*  559 */         setClause.append(value);
/*      */ 
/*  562 */         if (!value.equals("?"))
/*      */           continue;
/*  564 */         if (updateParams.length() > 0)
/*      */         {
/*  566 */           updateParams.append('\n');
/*      */         }
/*  568 */         updateParams.append(paramKeyName);
/*      */ 
/*  570 */         if (defaultValue != null)
/*      */         {
/*  572 */           updateParams.append(':');
/*  573 */           updateParams.append(defaultValue);
/*      */         }
/*  575 */         updateParams.append(' ');
/*  576 */         updateParams.append(fi.getTypeName());
/*      */       }
/*      */ 
/*  581 */       IdcVector row = new IdcVector();
/*      */ 
/*  583 */       if (isInsert)
/*      */       {
/*  585 */         insertBuilder.append(") VALUES(");
/*  586 */         insertBuilder.append(valueBuilder);
/*  587 */         insertBuilder.append(")");
/*      */ 
/*  589 */         String queryName = getProperQueryName(2, tables[i].m_name);
/*  590 */         row.add(queryName);
/*  591 */         row.add(insertBuilder);
/*  592 */         row.add(insertParams);
/*  593 */         drset.addRow(row);
/*  594 */         if (isDependent)
/*      */         {
/*  596 */           this.m_dependentInsertQueries.put(tables[i].m_name, queryName);
/*      */         }
/*      */         else
/*      */         {
/*  600 */           this.m_insertQuery.add(queryName);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  606 */       if (isUpdate)
/*      */       {
/*  608 */         updateBuilder.append(setClause);
/*  609 */         updateBuilder.append(' ');
/*  610 */         updateBuilder.append(whereClause);
/*      */ 
/*  612 */         String queryName = getProperQueryName(4, tables[i].m_name);
/*  613 */         if (updateParams.length() > 0)
/*      */         {
/*  615 */           updateParams.append('\n');
/*      */         }
/*  617 */         updateParams.append(whereParams);
/*  618 */         row = new IdcVector();
/*  619 */         row.add(queryName);
/*  620 */         row.add(updateBuilder);
/*  621 */         row.add(updateParams);
/*  622 */         drset.addRow(row);
/*      */ 
/*  624 */         if (isDependent)
/*      */         {
/*  626 */           this.m_dependentUpdateQueries.put(tables[i].m_name, queryName);
/*      */         }
/*      */         else
/*      */         {
/*  630 */           this.m_updateQuery.add(queryName);
/*      */         }
/*      */       }
/*      */ 
/*  634 */       if (!isDelete)
/*      */         continue;
/*  636 */       IdcStringBuilder deleteBuilder = new IdcStringBuilder();
/*  637 */       deleteBuilder.append("DELETE FROM ");
/*  638 */       deleteBuilder.append(tables[i].m_name);
/*  639 */       deleteBuilder.append(whereClause);
/*      */ 
/*  641 */       String queryName = getProperQueryName(8, tables[i].m_name);
/*  642 */       row = new IdcVector();
/*  643 */       row.add(queryName);
/*  644 */       row.add(deleteBuilder);
/*  645 */       row.add(whereParams);
/*  646 */       drset.addRow(row);
/*      */ 
/*  648 */       if (isDependent)
/*      */       {
/*  650 */         this.m_dependentDeleteQueries.put(tables[i].m_name, queryName);
/*      */       }
/*      */       else
/*      */       {
/*  654 */         this.m_deleteQuery.add(queryName);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  660 */     if (isSelect)
/*      */     {
/*  662 */       if (isDependent)
/*      */       {
/*  664 */         String selectQuery = createSelectQuery(ws, tables, selectList, relations, drset, depSelQueryName);
/*  665 */         this.m_dependentSelectQueries.put(depSelQueryName, selectQuery);
/*      */       }
/*      */       else
/*      */       {
/*  669 */         String selectQuery = createSelectQuery(ws, tables, selectList, relations, drset, null);
/*  670 */         this.m_selectQuery = selectQuery;
/*      */       }
/*      */     }
/*      */ 
/*  674 */     ws.addQueryDefs(drset);
/*      */   }
/*      */ 
/*      */   protected void addDependentQueryConditions(Map extraConditions)
/*      */   {
/*  679 */     String drivingField = (String)extraConditions.get("drivingField");
/*  680 */     String tmpValueTableStr = (String)extraConditions.get("drivingFieldTableMap");
/*  681 */     Map drivingFieldValueTableMap = getValueTableMap(tmpValueTableStr);
/*  682 */     this.m_drivingFields.put(drivingField, drivingFieldValueTableMap);
/*  683 */     this.m_MergeToParentRSFields.put(drivingField, (String)extraConditions.get("mergeToDrivingRSField"));
/*      */   }
/*      */ 
/*      */   protected String getFieldValue(String fieldName, DatabaseTable table) throws DataException
/*      */   {
/*  688 */     String value = "?";
/*      */ 
/*  690 */     for (DatabaseQueryFilter filter : table.m_filters)
/*      */     {
/*  692 */       if ((filter.m_columnName.equalsIgnoreCase(fieldName)) && 
/*  694 */         (filter.m_value != null) && (!filter.m_value.trim().equals("?")))
/*      */       {
/*  696 */         IdcStringBuilder builder = new IdcStringBuilder();
/*  697 */         QueryUtils.appendParam(builder, filter.m_column.m_type, filter.m_value);
/*  698 */         value = builder.toString();
/*      */       }
/*      */     }
/*      */ 
/*  702 */     return value;
/*      */   }
/*      */ 
/*      */   protected Map<String, String> getValueTableMap(String valueTableStr)
/*      */   {
/*  707 */     Map valueTableMap = new IdcConcurrentHashMap(false);
/*  708 */     List valueTablePairs = StringUtils.makeListFromSequence(valueTableStr, ',', ',', 32);
/*  709 */     for (String pair : valueTablePairs)
/*      */     {
/*  711 */       List valueTableList = StringUtils.makeListFromSequence(pair, ':', ':', 32);
/*  712 */       String value = (String)valueTableList.get(0);
/*  713 */       String table = value;
/*  714 */       if (valueTableList.size() > 1)
/*      */       {
/*  716 */         table = (String)valueTableList.get(1);
/*      */       }
/*  718 */       valueTableMap.put(value, table);
/*      */     }
/*  720 */     return valueTableMap;
/*      */   }
/*      */ 
/*      */   protected String createSelectQuery(Workspace ws, DatabaseTable[] tables, String[] selectList, DatabaseTableRelation[] relations, DataResultSet drset, String nameSuffix)
/*      */     throws DataException
/*      */   {
/*  726 */     IdcStringBuilder params = new IdcStringBuilder();
/*  727 */     String selections = getSelectList(ws, selectList, tables, params);
/*  728 */     String queryName = getProperQueryName(1, this.m_dataSourceName);
/*  729 */     if ((nameSuffix != null) && (nameSuffix.length() > 0))
/*      */     {
/*  731 */       queryName = queryName + nameSuffix;
/*      */     }
/*      */ 
/*  734 */     IdcStringBuilder selectQueryBuilder = new IdcStringBuilder();
/*  735 */     selectQueryBuilder.append("SELECT ");
/*  736 */     selectQueryBuilder.append(selections);
/*  737 */     selectQueryBuilder.append(" FROM ");
/*  738 */     boolean isFirstTable = true;
/*  739 */     for (DatabaseTable table : tables)
/*      */     {
/*  741 */       if (isFirstTable)
/*      */       {
/*  743 */         isFirstTable = false;
/*      */       }
/*      */       else
/*      */       {
/*  747 */         selectQueryBuilder.append(',');
/*      */       }
/*  749 */       if (!table.m_name.equalsIgnoreCase(table.m_alias))
/*      */       {
/*  751 */         selectQueryBuilder.append(table.m_name);
/*  752 */         selectQueryBuilder.append(' ');
/*      */       }
/*  754 */       selectQueryBuilder.append(table.m_alias);
/*      */     }
/*      */ 
/*  757 */     computeWhereClause(selectQueryBuilder, tables, relations, params);
/*      */ 
/*  762 */     IdcVector row = new IdcVector();
/*  763 */     row.add(queryName);
/*  764 */     row.add(selectQueryBuilder.toString());
/*  765 */     row.add(params.toString());
/*  766 */     drset.addRow(row);
/*      */ 
/*  768 */     return queryName;
/*      */   }
/*      */ 
/*      */   protected IdcAppendable computeWhereClause(IdcStringBuilder whereClause, DatabaseTable[] tables, DatabaseTableRelation[] relations, IdcStringBuilder params)
/*      */     throws DataException
/*      */   {
/*  775 */     boolean addedWhereClause = false;
/*      */ 
/*  777 */     for (int i = 0; (relations != null) && (i < relations.length); ++i)
/*      */     {
/*  779 */       if (relations[i] == null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  783 */       if (!addedWhereClause)
/*      */       {
/*  785 */         whereClause.append(" WHERE ");
/*  786 */         addedWhereClause = true;
/*      */       }
/*      */       else
/*      */       {
/*  790 */         whereClause.append(" AND ");
/*      */       }
/*      */ 
/*  793 */       whereClause.append(relations[i].m_parentTable.m_alias);
/*  794 */       whereClause.append('.');
/*  795 */       whereClause.append(relations[i].m_parentTableColumn);
/*  796 */       whereClause.append(" = ");
/*  797 */       whereClause.append(relations[i].m_childTable.m_alias);
/*  798 */       whereClause.append('.');
/*  799 */       whereClause.append(relations[i].m_childTableColumn);
/*      */     }
/*      */ 
/*  803 */     for (int i = 0; (tables != null) && (i < tables.length); ++i)
/*      */     {
/*  805 */       if (tables[i].m_filters == null) continue; if (tables[i].m_filters.size() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*  809 */       if (!addedWhereClause)
/*      */       {
/*  811 */         whereClause.append(" WHERE ");
/*  812 */         addedWhereClause = true;
/*      */       }
/*      */       else
/*      */       {
/*  816 */         whereClause.append(" AND ");
/*      */       }
/*      */ 
/*  819 */       DatabaseQueryFilter[] filters = (DatabaseQueryFilter[])tables[i].m_filters.toArray(new DatabaseQueryFilter[0]);
/*  820 */       for (int j = 0; j < filters.length; ++j)
/*      */       {
/*  822 */         if (j != 0)
/*      */         {
/*  824 */           whereClause.append(" AND ");
/*      */         }
/*  826 */         whereClause.append(filters[j].m_filter);
/*  827 */         if (!filters[j].m_value.trim().equals("?"))
/*      */           continue;
/*  829 */         if (params.length() > 0)
/*      */         {
/*  831 */           params.append('\n');
/*      */         }
/*  833 */         params.append(filters[j].m_columnName);
/*  834 */         params.append(' ');
/*  835 */         params.append(filters[j].m_column.getTypeName());
/*      */       }
/*      */     }
/*      */ 
/*  839 */     return whereClause;
/*      */   }
/*      */ 
/*      */   protected String getSelectList(Workspace ws, String[] selectList, DatabaseTable[] tables, IdcStringBuilder params)
/*      */     throws DataException
/*      */   {
/*  846 */     if ((selectList == null) || (selectList.length == 0))
/*      */     {
/*  848 */       return "*";
/*      */     }
/*      */ 
/*  851 */     IdcStringBuilder selectionBuilder = new IdcStringBuilder();
/*  852 */     boolean isFirst = true;
/*  853 */     for (int i = 0; i < selectList.length; ++i)
/*      */     {
/*  855 */       DatabaseTable table = tables[i];
/*      */       String[] columns;
/*  856 */       if ((selectList[i] != null) && (selectList[i].length() != 0))
/*      */       {
/*  858 */         columns = tables[i].m_columnNames;
/*  859 */         List selections = StringUtils.parseArray(selectList[i], ',', '^');
/*  860 */         for (String columnAndAlias : selections)
/*      */         {
/*  862 */           if (!isFirst)
/*      */           {
/*  864 */             selectionBuilder.append(',');
/*      */           }
/*      */           else
/*      */           {
/*  868 */             isFirst = false;
/*      */           }
/*  870 */           if (columnAndAlias.startsWith("?"))
/*      */           {
/*  872 */             selectionBuilder.append('?');
/*  873 */             int index = columnAndAlias.indexOf(35);
/*  874 */             if (index != -1)
/*      */             {
/*  876 */               if (params.length() != 0)
/*      */               {
/*  878 */                 params.append('\n');
/*      */               }
/*  880 */               params.append(columnAndAlias.substring(index + 1));
/*      */             }
/*      */ 
/*  884 */             throw new DataException(null, "csDataSourceSelectDefError", new Object[] { selectList[i] });
/*      */           }
/*      */ 
/*  889 */           List columnAliasList = StringUtils.parseArray(columnAndAlias, ' ', ' ');
/*  890 */           String column = (String)columnAliasList.get(0);
/*  891 */           if (StringUtils.findStringIndexEx(columns, column, true) >= 0)
/*      */           {
/*  893 */             selectionBuilder.append(table.m_alias);
/*  894 */             selectionBuilder.append('.');
/*  895 */             selectionBuilder.append(column);
/*      */           }
/*      */           else
/*      */           {
/*  899 */             selectionBuilder.append(column);
/*      */           }
/*      */ 
/*  902 */           if (columnAliasList.size() > 1)
/*      */           {
/*  904 */             String alias = (String)columnAliasList.get(1);
/*  905 */             if ((alias != null) && (alias.length() != 0))
/*      */             {
/*  907 */               selectionBuilder.append(" AS ");
/*  908 */               selectionBuilder.append(alias);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  915 */         if (!isFirst)
/*      */         {
/*  917 */           selectionBuilder.append(", ");
/*      */         }
/*      */         else
/*      */         {
/*  921 */           isFirst = false;
/*      */         }
/*  923 */         selectionBuilder.append(table.m_name);
/*  924 */         selectionBuilder.append('.');
/*  925 */         selectionBuilder.append('*');
/*      */       }
/*      */     }
/*      */ 
/*  929 */     return selectionBuilder.toString();
/*      */   }
/*      */ 
/*      */   protected String getProperQueryName(int type, String tableName)
/*      */   {
/*  934 */     String queryName = "Q";
/*  935 */     if (type == 2)
/*      */     {
/*  937 */       queryName = "I";
/*      */     }
/*  939 */     else if (type == 4)
/*      */     {
/*  941 */       queryName = "U";
/*      */     }
/*  943 */     else if (type == 8)
/*      */     {
/*  945 */       queryName = "D";
/*      */     }
/*  947 */     else if (type == 16)
/*      */     {
/*  949 */       queryName = "C";
/*      */     }
/*  951 */     queryName = queryName + "ds";
/*  952 */     queryName = queryName + this.m_dataSourceName;
/*  953 */     queryName = queryName + tableName;
/*  954 */     return queryName;
/*      */   }
/*      */ 
/*      */   public ResultSet createResultSet(Workspace ws, Parameters params, ExecutionContext cxt) throws DataException
/*      */   {
/*  959 */     ResultSet rset = ws.createResultSet(this.m_selectQuery, params);
/*      */ 
/*  961 */     if ((!rset.isEmpty()) && (!this.m_dependentSelectQueries.isEmpty()) && (!this.m_drivingFields.isEmpty()) && (params instanceof DataBinder))
/*      */     {
/*  963 */       DataResultSet drset = new DataResultSet();
/*  964 */       drset.copy(rset);
/*  965 */       Set dfset = this.m_drivingFields.keySet();
/*  966 */       Iterator dfit = dfset.iterator();
/*  967 */       while (dfit.hasNext())
/*      */       {
/*  969 */         String drivingfield = (String)dfit.next();
/*  970 */         Map drivingFieldValueTableMap = (Map)this.m_drivingFields.get(drivingfield);
/*  971 */         String mergeField = (String)this.m_MergeToParentRSFields.get(drivingfield);
/*  972 */         String value = drset.getStringValueByName(drivingfield);
/*  973 */         if (value != null)
/*      */         {
/*  975 */           IdcStringBuilder valueBuilder = new IdcStringBuilder();
/*  976 */           List values = StringUtils.makeListFromSequence(value, ',', ',', 32);
/*  977 */           boolean hasValues = false;
/*  978 */           for (String val : values)
/*      */           {
/*  980 */             if (hasValues)
/*      */             {
/*  982 */               valueBuilder.append(',');
/*      */             }
/*      */             else
/*      */             {
/*  986 */               hasValues = true;
/*      */             }
/*  988 */             valueBuilder.append((String)drivingFieldValueTableMap.get(val));
/*      */           }
/*      */ 
/*  991 */           List aliasList = StringUtils.makeListFromSequence(valueBuilder.toString(), ',', ',', 32);
/*      */ 
/*  993 */           if ((mergeField != null) && (mergeField.length() > 0))
/*      */           {
/*  995 */             for (String alias : aliasList)
/*      */             {
/*  997 */               String selectQuery = (String)this.m_dependentSelectQueries.get(alias);
/*  998 */               if (selectQuery != null)
/*      */               {
/* 1000 */                 ResultSet tmp = ws.createResultSet(selectQuery, params);
/* 1001 */                 if (!tmp.isEmpty())
/*      */                 {
/* 1003 */                   drset.mergeFields(tmp);
/* 1004 */                   drset.merge(mergeField, tmp, true);
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/* 1011 */             String drivingRsetName = "ids." + this.m_dataSourceName;
/* 1012 */             ((DataBinder)params).addResultSet(drivingRsetName, rset);
/*      */ 
/* 1014 */             IdcStringBuilder builder = new IdcStringBuilder();
/* 1015 */             boolean hasRset = false;
/* 1016 */             for (String alias : aliasList)
/*      */             {
/* 1018 */               String selectQuery = (String)this.m_dependentSelectQueries.get(alias);
/* 1019 */               if (selectQuery != null)
/*      */               {
/* 1021 */                 ResultSet tmp = ws.createResultSet(selectQuery, params);
/*      */ 
/* 1023 */                 ((DataBinder)params).addResultSet(drivingRsetName + alias, tmp);
/* 1024 */                 if (hasRset)
/*      */                 {
/* 1026 */                   builder.append(',');
/*      */                 }
/*      */                 else
/*      */                 {
/* 1030 */                   hasRset = true;
/*      */                 }
/* 1032 */                 builder.append(alias);
/*      */               }
/*      */             }
/* 1035 */             if (hasRset)
/*      */             {
/* 1037 */               ((DataBinder)params).putLocal("dependentResultSubKeys:" + drivingRsetName, builder.toString());
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 1043 */       return drset;
/*      */     }
/*      */ 
/* 1046 */     return rset;
/*      */   }
/*      */ 
/*      */   public int[] addBatch(Workspace ws, int type, Parameters params, ExecutionContext cxt) throws DataException
/*      */   {
/* 1051 */     List batchQueries = getQueries(type, params);
/* 1052 */     int[] batchIndex = new int[batchQueries.size()];
/* 1053 */     int index = 0;
/* 1054 */     for (String query : batchQueries)
/*      */     {
/* 1056 */       batchIndex[(index++)] = ws.addBatch(query, params);
/*      */     }
/*      */ 
/* 1059 */     return batchIndex;
/*      */   }
/*      */ 
/*      */   public long[] modData(Workspace ws, int type, Parameters params, ExecutionContext cxt) throws DataException
/*      */   {
/* 1064 */     List modQueries = getQueries(type, params);
/* 1065 */     long[] modRows = new long[modQueries.size()];
/* 1066 */     int i = 0;
/* 1067 */     for (String query : modQueries)
/*      */     {
/* 1069 */       modRows[i] = ws.execute(query, params);
/* 1070 */       ++i;
/*      */     }
/* 1072 */     return modRows;
/*      */   }
/*      */ 
/*      */   public List<String> getQueries(int queryType, Parameters params) throws DataException
/*      */   {
/* 1077 */     List tmpQueries = null;
/* 1078 */     Map depQueries = null;
/* 1079 */     switch (queryType) { case 2:
/* 1082 */       tmpQueries = this.m_insertQuery;
/* 1083 */       depQueries = this.m_dependentInsertQueries;
/* 1084 */       break;
/*      */     case 4:
/* 1086 */       tmpQueries = this.m_updateQuery;
/* 1087 */       depQueries = this.m_dependentUpdateQueries;
/* 1088 */       break;
/*      */     case 8:
/* 1090 */       tmpQueries = this.m_deleteQuery;
/* 1091 */       depQueries = this.m_dependentDeleteQueries;
/* 1092 */       break;
/*      */     case 1:
/* 1094 */       tmpQueries = new ArrayList();
/* 1095 */       tmpQueries.add(this.m_selectQuery);
/* 1096 */       depQueries = this.m_dependentSelectQueries;
/*      */     case 3:
/*      */     case 5:
/*      */     case 6:
/*      */     case 7: }
/* 1099 */     List queries = tmpQueries;
/*      */ 
/* 1101 */     if ((!depQueries.isEmpty()) && (!this.m_drivingFields.isEmpty()) && (params instanceof DataBinder))
/*      */     {
/* 1103 */       queries = new ArrayList();
/* 1104 */       queries.addAll(tmpQueries);
/*      */ 
/* 1106 */       Set dfset = this.m_drivingFields.keySet();
/* 1107 */       Iterator dfit = dfset.iterator();
/*      */       Map drivingFieldValueTableMap;
/* 1108 */       while (dfit.hasNext())
/*      */       {
/* 1110 */         String drivingfield = (String)dfit.next();
/* 1111 */         drivingFieldValueTableMap = (Map)this.m_drivingFields.get(drivingfield);
/* 1112 */         String driveFieldValues = ((DataBinder)params).getAllowMissing(drivingfield);
/* 1113 */         if (driveFieldValues != null)
/*      */         {
/* 1115 */           List values = StringUtils.makeListFromSequence(driveFieldValues, ',', ',', 32);
/*      */ 
/* 1117 */           for (String value : values)
/*      */           {
/* 1119 */             String tables = (String)drivingFieldValueTableMap.get(value);
/* 1120 */             if (tables == null)
/*      */             {
/* 1122 */               tables = value;
/*      */             }
/*      */ 
/* 1125 */             List tablelist = StringUtils.makeListFromSequence(tables, ';', ';', 32);
/* 1126 */             for (String table : tablelist)
/*      */             {
/* 1128 */               String dq = (String)depQueries.get(table);
/* 1129 */               if (dq != null)
/*      */               {
/* 1131 */                 queries.add(dq);
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1138 */     return queries;
/*      */   }
/*      */ 
/*      */   protected void changeRelationToFilter(String[] relationStr, DatabaseTable[] tables) throws DataException
/*      */   {
/* 1143 */     DatabaseTableRelation[] relations = getTableRelations(relationStr, tables);
/* 1144 */     String[] tableNames = getTableNames(tables);
/* 1145 */     for (DatabaseTableRelation relation : relations)
/*      */     {
/* 1147 */       int index = StringUtils.findStringIndexEx(tableNames, relation.m_childTable.m_alias, true);
/* 1148 */       if (index < 0)
/*      */         continue;
/* 1150 */       DatabaseQueryFilter filter = new DatabaseQueryFilter();
/* 1151 */       filter.m_table = relation.m_childTable;
/* 1152 */       filter.m_columnName = relation.m_childTableColumn;
/* 1153 */       filter.m_column = relation.m_childTable.getColumn(filter.m_columnName);
/* 1154 */       filter.m_value = "?";
/* 1155 */       filter.m_operator = "=";
/* 1156 */       filter.m_filter = (filter.m_table.m_alias + "." + filter.m_columnName + " " + filter.m_operator + " ?");
/*      */ 
/* 1158 */       filter.m_table.m_filters.add(filter);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String[] getTableNames(DatabaseTable[] tables)
/*      */   {
/* 1166 */     String[] names = new String[tables.length];
/* 1167 */     for (int i = 0; i < tables.length; ++i)
/*      */     {
/* 1169 */       names[i] = tables[i].m_alias;
/*      */     }
/* 1171 */     return names;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1176 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97264 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdcDataSource
 * JD-Core Version:    0.5.4
 */