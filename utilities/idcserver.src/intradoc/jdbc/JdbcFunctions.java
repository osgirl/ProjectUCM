/*      */ package intradoc.jdbc;
/*      */ 
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DatabaseConfigData;
/*      */ import intradoc.data.DatabaseIndexInfo;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.FieldInfoUtils;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.sql.Connection;
/*      */ import java.sql.DatabaseMetaData;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.Statement;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class JdbcFunctions
/*      */ {
/*   59 */   public static int m_memoLength = 2000;
/*      */ 
/*   64 */   public static int m_memoMandateLength = 1000000;
/*      */ 
/*   69 */   public static boolean m_useLongIntegerField = true;
/*      */ 
/*   74 */   public static boolean m_usePre80MetaFieldType = false;
/*      */ 
/*      */   public static Vector getFieldListInternal(JdbcConnection jCon, JdbcManager manager, String table)
/*      */     throws SQLException
/*      */   {
/*   79 */     JdbcResultSet jdbcRset = null;
/*   80 */     Statement stmt = null;
/*   81 */     Vector tableFields = new IdcVector();
/*      */     try
/*      */     {
/*   85 */       String queryStr = "SELECT * FROM " + table;
/*      */ 
/*   87 */       Connection con = (Connection)jCon.getConnection();
/*   88 */       stmt = con.createStatement();
/*   89 */       if (manager.supportsChangeFetchSize())
/*      */       {
/*   92 */         stmt.setFetchSize(1);
/*      */       }
/*      */ 
/*   96 */       queryStr = queryStr + " WHERE 1 = 0";
/*   97 */       java.sql.ResultSet rset = stmt.executeQuery(queryStr);
/*      */ 
/*  100 */       jdbcRset = new JdbcResultSet(manager);
/*  101 */       jdbcRset.setQueryInfo(stmt, queryStr, jCon, rset);
/*      */ 
/*  103 */       int numFields = jdbcRset.getNumFields();
/*  104 */       tableFields.setSize(numFields);
/*      */ 
/*  107 */       for (int i = 0; i < numFields; ++i)
/*      */       {
/*  109 */         FieldInfo fi = new FieldInfo();
/*  110 */         jdbcRset.getIndexFieldInfo(i, fi);
/*  111 */         tableFields.setElementAt(fi, i);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  116 */       if (stmt != null)
/*      */       {
/*  118 */         stmt.close();
/*      */       }
/*  120 */       if (jdbcRset != null)
/*      */       {
/*  122 */         jdbcRset.closeInternals();
/*      */       }
/*      */     }
/*  125 */     return tableFields;
/*      */   }
/*      */ 
/*      */   public static String[] getPrimaryKeyColsInternal(DatabaseMetaData dbMetaData, JdbcManager manager, String table, String[] keyName)
/*      */     throws SQLException
/*      */   {
/*  131 */     java.sql.ResultSet rset = null;
/*  132 */     String[] retVal = null;
/*  133 */     keyName[0] = null;
/*      */ 
/*  135 */     String catalog = null;
/*  136 */     String schema = getUserSchema(dbMetaData, manager);
/*  137 */     if (manager.isOracle())
/*      */     {
/*  139 */       table = table.toUpperCase();
/*      */     }
/*  141 */     else if (manager.isDB2())
/*      */     {
/*  143 */       table = table.toUpperCase();
/*      */     }
/*  145 */     else if (manager.isPostgreSQL())
/*      */     {
/*  147 */       table = table.toLowerCase();
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  152 */       manager.debugMsg("Retrieving primary keys for Catalog:" + catalog + " Schema:" + schema + " Table:" + table);
/*      */ 
/*  154 */       rset = dbMetaData.getPrimaryKeys(catalog, schema, table);
/*  155 */       Vector pkColsV = new IdcVector();
/*  156 */       Vector pkOrder = new IdcVector();
/*      */ 
/*  158 */       Properties columnMap = manager.getColumnMap();
/*      */ 
/*  160 */       Properties addedColumns = new Properties();
/*  161 */       while (rset.next())
/*      */       {
/*  163 */         String colName = rset.getString("COLUMN_NAME");
/*  164 */         short order = rset.getShort("KEY_SEQ");
/*  165 */         String keyTemp = rset.getString("PK_NAME");
/*  166 */         if (columnMap != null)
/*      */         {
/*  168 */           String alias = columnMap.getProperty(colName.toUpperCase());
/*  169 */           if (alias != null)
/*      */           {
/*  171 */             colName = alias;
/*      */           }
/*      */         }
/*  174 */         if (addedColumns.getProperty(colName) == null)
/*      */         {
/*  176 */           addedColumns.put(colName, "1");
/*      */         }
/*      */         else
/*      */         {
/*  180 */           manager.debugMsg("Duplicate primary key column retrieved: " + colName);
/*  181 */           continue;
/*      */         }
/*  183 */         pkColsV.addElement(colName);
/*  184 */         pkOrder.addElement(new Integer(order));
/*  185 */         if (keyTemp != null)
/*      */         {
/*  187 */           keyName[0] = keyTemp;
/*      */         }
/*      */       }
/*  190 */       retVal = StringUtils.convertListToArray(pkColsV);
/*      */     }
/*      */     finally
/*      */     {
/*  194 */       if (rset != null)
/*      */       {
/*  196 */         rset.close();
/*      */       }
/*      */     }
/*  199 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static HashMap getIndexInternal(JdbcConnection jCon, DatabaseMetaData dbMetaData, JdbcManager manager, String table)
/*      */     throws SQLException
/*      */   {
/*  205 */     String catalog = null;
/*  206 */     if (manager.isOracle())
/*      */     {
/*  208 */       return getIndexInternalOracle(jCon, manager, table);
/*      */     }
/*  210 */     if (manager.isDB2())
/*      */     {
/*  212 */       table = table.toUpperCase();
/*      */     }
/*  214 */     else if (manager.isPostgreSQL())
/*      */     {
/*  216 */       table = table.toLowerCase();
/*      */     }
/*  218 */     String schema = getUserSchema(dbMetaData, manager);
/*  219 */     java.sql.ResultSet rset = dbMetaData.getIndexInfo(catalog, schema, table, false, true);
/*      */ 
/*  221 */     HashMap map = new HashMap();
/*  222 */     Properties columnMap = manager.getColumnMap();
/*  223 */     while (rset.next())
/*      */     {
/*  225 */       String indexName = rset.getString("INDEX_NAME");
/*  226 */       String column = rset.getString("COLUMN_NAME");
/*  227 */       if (indexName == null) continue; if (column == null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  231 */       DatabaseIndexInfo indexInfo = (DatabaseIndexInfo)map.get(indexName);
/*  232 */       if (indexInfo == null)
/*      */       {
/*  234 */         indexInfo = new DatabaseIndexInfo();
/*  235 */         indexInfo.m_name = indexName;
/*  236 */         map.put(indexName, indexInfo);
/*      */       }
/*  238 */       String tmp = columnMap.getProperty(column.toUpperCase());
/*  239 */       if (tmp != null)
/*      */       {
/*  241 */         column = tmp;
/*      */       }
/*  243 */       if (indexInfo.m_columns.size() == 0)
/*      */       {
/*  245 */         tmp = rset.getString("TABLE_NAME");
/*  246 */         indexInfo.m_table = tmp;
/*      */ 
/*  248 */         boolean isTrue = rset.getBoolean("NON_UNIQUE");
/*  249 */         indexInfo.m_isUnique = (!isTrue);
/*      */ 
/*  251 */         short type = rset.getShort("TYPE");
/*  252 */         if (type == 2)
/*      */         {
/*  254 */           indexInfo.m_type = 2;
/*      */         }
/*  256 */         else if (type == 1)
/*      */         {
/*  258 */           indexInfo.m_isClustered = true;
/*      */         }
/*      */ 
/*  261 */         tmp = rset.getString("ASC_OR_DESC");
/*  262 */         if (tmp != null)
/*      */         {
/*  264 */           indexInfo.m_additionalProps.put("sortOrder_" + column, tmp);
/*      */         }
/*      */ 
/*  267 */         int tmpInt = rset.getInt("CARDINALITY");
/*  268 */         indexInfo.m_additionalProps.put("cardinality", "" + tmpInt);
/*      */       }
/*  270 */       indexInfo.m_columns.add(column);
/*      */     }
/*  272 */     DatabaseIndexInfo[] fullTextIndices = getFullTextIndexListInternal(jCon, manager, table);
/*  273 */     for (int i = 0; i < fullTextIndices.length; ++i)
/*      */     {
/*  275 */       map.put(fullTextIndices[i].m_name, fullTextIndices[i]);
/*      */     }
/*  277 */     return map;
/*      */   }
/*      */ 
/*      */   public static String getUserSchema(DatabaseMetaData meta, JdbcManager manager) throws SQLException
/*      */   {
/*  282 */     DatabaseConfigData config = manager.m_config;
/*  283 */     String schema = config.getValueAsString("DatabaseSchemaName");
/*  284 */     if ((schema == null) || (schema.length() == 0))
/*      */     {
/*  286 */       if ((manager.isOracle()) || (manager.isDB2()))
/*      */       {
/*  288 */         schema = meta.getUserName();
/*      */       }
/*  290 */       else if (manager.isSqlServer())
/*      */       {
/*  292 */         schema = "dbo";
/*      */ 
/*  296 */         String version = config.getValueAsString("DatabaseVersion");
/*  297 */         int versionCompare = version.compareTo("09");
/*  298 */         if (versionCompare >= 0)
/*      */         {
/*  300 */           schema = meta.getUserName();
/*      */         }
/*      */       }
/*      */     }
/*  304 */     if ((manager.isOracle()) || (manager.isDB2()))
/*      */     {
/*  306 */       schema = schema.toUpperCase();
/*      */     }
/*  308 */     return schema;
/*      */   }
/*      */ 
/*      */   protected static DatabaseIndexInfo[] getFullTextIndexListInternal(JdbcConnection jCon, JdbcManager manager, String table)
/*      */   {
/*  313 */     if (!manager.isSqlServer())
/*      */     {
/*  315 */       return new DatabaseIndexInfo[0];
/*      */     }
/*      */ 
/*  319 */     String query = "EXEC sp_help_fulltext_columns '" + table + "'";
/*      */ 
/*  321 */     intradoc.data.ResultSet jdbcRset = null;
/*      */ 
/*  323 */     ArrayList list = new ArrayList();
/*      */     try
/*      */     {
/*  326 */       jdbcRset = createResultSet(jCon, query, manager);
/*  327 */       for (jdbcRset.first(); jdbcRset.isRowPresent(); jdbcRset.next())
/*      */       {
/*  330 */         DatabaseIndexInfo indexInfo = new DatabaseIndexInfo();
/*  331 */         indexInfo.m_type = 3;
/*  332 */         String tmp = ResultSetUtils.getValue(jdbcRset, "FULLTEXT_COLUMN_NAME");
/*  333 */         if (tmp != null)
/*      */         {
/*  335 */           indexInfo.m_columns.add(tmp);
/*      */         }
/*  337 */         indexInfo.m_name = ("FT_" + table + tmp);
/*  338 */         tmp = ResultSetUtils.getValue(jdbcRset, "FULLTEXT_BLOBTP_COLNAME");
/*  339 */         if (tmp != null)
/*      */         {
/*  341 */           indexInfo.m_additionalProps.put("fullTextFormatColumn", tmp);
/*      */         }
/*  343 */         tmp = ResultSetUtils.getValue(jdbcRset, "FULLTEXT_LANGUAGE");
/*  344 */         if (tmp != null)
/*      */         {
/*  346 */           indexInfo.m_additionalProps.put("fullTextLanguageCode", tmp);
/*      */         }
/*  348 */         list.add(indexInfo);
/*      */       }
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*  353 */       if (SystemUtils.m_verbose)
/*      */       {
/*  355 */         manager.debugMsg(e.getMessage());
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  360 */       if (jdbcRset != null)
/*      */       {
/*  362 */         jdbcRset.closeInternals();
/*      */       }
/*      */     }
/*      */ 
/*  366 */     intradoc.data.ResultSet rset = null;
/*  367 */     String catelog = null;
/*  368 */     String keyIndex = null;
/*      */     try
/*      */     {
/*  371 */       rset = createResultSet(jCon, "EXEC sp_help_fulltext_tables @table_name = '" + table + "'", manager);
/*  372 */       catelog = ResultSetUtils.getValue(rset, "FULLTEXT_CATALOG_NAME");
/*  373 */       keyIndex = ResultSetUtils.getValue(rset, "FULLTEXT_KEY_INDEX_NAME");
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*  377 */       if (SystemUtils.m_verbose)
/*      */       {
/*  379 */         manager.debugMsg(e.getMessage());
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  384 */       if (rset != null)
/*      */       {
/*  386 */         rset.closeInternals();
/*      */       }
/*      */     }
/*  389 */     DatabaseIndexInfo[] info = new DatabaseIndexInfo[list.size()];
/*  390 */     Iterator iterator = list.iterator();
/*  391 */     short counter = 0;
/*  392 */     while (iterator.hasNext())
/*      */     {
/*  394 */       DatabaseIndexInfo indexInfo = (DatabaseIndexInfo)iterator.next();
/*      */ 
/*  396 */       if (catelog != null)
/*      */       {
/*  398 */         indexInfo.m_additionalProps.put("fullTextCatelog", catelog);
/*      */       }
/*  400 */       if (keyIndex != null)
/*      */       {
/*  402 */         indexInfo.m_additionalProps.put("fullTextKeyIndex", keyIndex);
/*      */       }
/*  404 */       info[counter] = indexInfo;
/*  405 */       counter = (short)(counter + 1);
/*      */     }
/*  407 */     return info;
/*      */   }
/*      */ 
/*      */   public static HashMap getIndexInternalOracle(JdbcConnection jCon, JdbcManager manager, String table)
/*      */     throws SQLException
/*      */   {
/*  413 */     String query = "SELECT ind.INDEX_NAME, ind.TABLE_NAME, INDEX_TYPE, UNIQUENESS, TABLESPACE_NAME, COLUMN_NAME, ITYP_OWNER, ITYP_NAME, PARAMETERS, DOMIDX_STATUS, FUNCIDX_STATUS FROM USER_INDEXES ind, USER_IND_COLUMNS col WHERE ind.INDEX_NAME =col.INDEX_NAME and ind.TABLE_NAME = col.TABLE_NAME and ind.table_name ='";
/*      */ 
/*  418 */     query = query + table.toUpperCase() + "'";
/*      */ 
/*  421 */     intradoc.data.ResultSet jdbcRset = null;
/*      */     try
/*      */     {
/*  424 */       jdbcRset = createResultSet(jCon, query, manager);
/*  425 */       HashMap map = new HashMap();
/*  426 */       Properties columnMap = manager.getColumnMap();
/*  427 */       for (jdbcRset.first(); jdbcRset.isRowPresent(); jdbcRset.next())
/*      */       {
/*  429 */         indexName = ResultSetUtils.getValue(jdbcRset, "INDEX_NAME");
/*  430 */         DatabaseIndexInfo indexInfo = (DatabaseIndexInfo)map.get(indexName);
/*  431 */         if (indexInfo == null)
/*      */         {
/*  433 */           indexInfo = new DatabaseIndexInfo();
/*  434 */           indexInfo.m_name = indexName;
/*  435 */           indexInfo.m_table = table;
/*  436 */           map.put(indexName, indexInfo);
/*      */         }
/*  438 */         String column = ResultSetUtils.getValue(jdbcRset, "COLUMN_NAME");
/*  439 */         if (column == null) {
/*      */           continue;
/*      */         }
/*      */ 
/*  443 */         String tmp = columnMap.getProperty(column.toUpperCase());
/*  444 */         if (tmp != null)
/*      */         {
/*  446 */           column = tmp;
/*      */         }
/*  448 */         if (indexInfo.m_columns.contains(column)) {
/*      */           continue;
/*      */         }
/*      */ 
/*  452 */         if (indexInfo.m_columns.size() == 0)
/*      */         {
/*  454 */           tmp = ResultSetUtils.getValue(jdbcRset, "INDEX_TYPE");
/*  455 */           if (tmp.equals("DOMAIN"))
/*      */           {
/*  457 */             indexInfo.m_type = 3;
/*  458 */             tmp = ResultSetUtils.getValue(jdbcRset, "ITYP_OWNER");
/*  459 */             if (tmp != null)
/*      */             {
/*  461 */               indexInfo.m_additionalProps.put("fullTextIndexOwner", tmp);
/*      */             }
/*  463 */             tmp = ResultSetUtils.getValue(jdbcRset, "ITYP_NAME");
/*  464 */             if (tmp != null)
/*      */             {
/*  466 */               indexInfo.m_additionalProps.put("fullTextIndexType", tmp);
/*      */             }
/*  468 */             tmp = ResultSetUtils.getValue(jdbcRset, "PARAMETERS");
/*  469 */             if (tmp != null)
/*      */             {
/*  471 */               indexInfo.m_additionalProps.put("fullTextParams", tmp);
/*      */             }
/*  473 */             tmp = ResultSetUtils.getValue(jdbcRset, "DOMIDX_STATUS");
/*  474 */             if (tmp != null)
/*      */             {
/*  476 */               indexInfo.m_additionalProps.put("indexStatus", tmp);
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/*  481 */             if (tmp.indexOf("BITMAP") >= 0)
/*      */             {
/*  483 */               indexInfo.m_type = 1;
/*      */             }
/*  485 */             if (tmp.indexOf("FUNCTION") >= 0)
/*      */             {
/*  487 */               indexInfo.m_isFunctional = true;
/*  488 */               tmp = ResultSetUtils.getValue(jdbcRset, "FUNCIDX_STATUS");
/*  489 */               if (tmp != null)
/*      */               {
/*  491 */                 indexInfo.m_additionalProps.put("indexStatus", tmp);
/*      */               }
/*  493 */               intradoc.data.ResultSet funcRset = null;
/*      */               try
/*      */               {
/*  496 */                 query = "SELECT COLUMN_EXPRESSION FROM USER_IND_EXPRESSIONS WHERE INDEX_NAME = '" + indexInfo.m_name + "' AND TABLE_NAME = '" + table.toUpperCase() + "'";
/*      */ 
/*  498 */                 funcRset = createResultSet(jCon, query, manager);
/*  499 */                 indexInfo.m_function = ResultSetUtils.getValue(funcRset, "COLUMN_EXPRESSION");
/*      */ 
/*  501 */                 column = indexInfo.m_function;
/*      */               }
/*      */               finally
/*      */               {
/*  505 */                 if (funcRset != null)
/*      */                 {
/*  507 */                   funcRset.closeInternals();
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/*  512 */           tmp = ResultSetUtils.getValue(jdbcRset, "UNIQUENESS");
/*  513 */           indexInfo.m_isUnique = tmp.startsWith("UNIQUE");
/*      */         }
/*  515 */         indexInfo.m_columns.add(column);
/*      */       }
/*  517 */       String indexName = map;
/*      */ 
/*  523 */       return indexName;
/*      */     }
/*      */     finally
/*      */     {
/*  521 */       if (jdbcRset != null)
/*      */       {
/*  523 */         jdbcRset.closeInternals();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String[] getTableListInternal(JdbcManager manager, DatabaseMetaData dbMetaData, String catalog)
/*      */     throws SQLException, DataException
/*      */   {
/*  531 */     return getTableListInternal(manager, dbMetaData, catalog, null);
/*      */   }
/*      */ 
/*      */   public static String[] getTableListInternal(JdbcManager manager, DatabaseMetaData dbMetaData, String catalog, String schema)
/*      */     throws SQLException, DataException
/*      */   {
/*  537 */     java.sql.ResultSet rsTables = null;
/*  538 */     List tableNames = new ArrayList();
/*      */ 
/*  541 */     if ((catalog != null) && (catalog.length() == 0))
/*      */     {
/*  545 */       catalog = null;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  550 */       rsTables = dbMetaData.getTables(catalog, schema, null, null);
/*      */ 
/*  553 */       while (rsTables.next())
/*      */       {
/*  555 */         String tableName = rsTables.getString("TABLE_NAME");
/*  556 */         String tableType = rsTables.getString("TABLE_TYPE");
/*      */ 
/*  560 */         if (SharedObjects.getEnvValueAsBoolean("EBRIncludeViewsInTableList", false))
/*      */         {
/*  562 */           if (tableType == null) continue; if (!tableType.equalsIgnoreCase("INDEX"))
/*      */           {
/*      */             break label124;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  569 */         if ((tableType == null) || (tableType.equalsIgnoreCase("INDEX"))) continue; if (tableType.equalsIgnoreCase("VIEW"))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  574 */         label124: tableNames.add(tableName);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  579 */       if (rsTables != null)
/*      */       {
/*  581 */         rsTables.close();
/*      */       }
/*      */     }
/*  584 */     if (tableNames.size() == 0)
/*      */     {
/*  586 */       if (manager.allowEmptyTableList())
/*      */       {
/*  588 */         Report.warning("systemdatabase", null, "csDbFailedToGetNonEmtpyTableList", new Object[] { catalog, schema });
/*      */       }
/*      */       else
/*      */       {
/*  593 */         throw new DataException(null, "csDbFailedToGetNonEmtpyTableList", new Object[] { catalog, schema });
/*      */       }
/*      */     }
/*      */ 
/*  597 */     return StringUtils.convertListToArray(tableNames);
/*      */   }
/*      */ 
/*      */   public static String[] getViewListInternal(JdbcManager manager, DatabaseMetaData dbMetaData, String catalog, String schema)
/*      */     throws SQLException, DataException
/*      */   {
/*  603 */     java.sql.ResultSet rsTables = null;
/*  604 */     List viewNames = new ArrayList();
/*      */ 
/*  607 */     if ((catalog != null) && (catalog.length() == 0))
/*      */     {
/*  611 */       catalog = null;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  616 */       rsTables = dbMetaData.getTables(catalog, schema, null, null);
/*      */ 
/*  619 */       while (rsTables.next())
/*      */       {
/*  621 */         String tableName = rsTables.getString("TABLE_NAME");
/*  622 */         String tableType = rsTables.getString("TABLE_TYPE");
/*  623 */         if ((tableType == null) || (tableType.equalsIgnoreCase("INDEX"))) continue; if (tableType.equalsIgnoreCase("TABLE")) {
/*      */           continue;
/*      */         }
/*      */ 
/*  627 */         viewNames.add(tableName);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  632 */       if (rsTables != null)
/*      */       {
/*  634 */         rsTables.close();
/*      */       }
/*      */     }
/*  637 */     if (viewNames.size() == 0)
/*      */     {
/*  639 */       if (manager.allowEmptyTableList())
/*      */       {
/*  641 */         Report.warning("systemdatabase", null, "csDbFailedToGetNonEmtpyTableList", new Object[] { catalog, schema });
/*      */       }
/*      */       else
/*      */       {
/*  646 */         throw new DataException(null, "csDbFailedToGetNonEmtpyTableList", new Object[] { catalog, schema });
/*      */       }
/*      */     }
/*      */ 
/*  650 */     return StringUtils.convertListToArray(viewNames);
/*      */   }
/*      */ 
/*      */   public static void executeAlterTable(Connection con, String table, Vector fieldInfo, String command, JdbcManager manager, boolean isChange)
/*      */     throws SQLException
/*      */   {
/*  657 */     Vector tempFieldInfo = null;
/*  658 */     boolean appendAllowNullStr = (!manager.isOracle()) || (!isChange);
/*  659 */     boolean oneAtATime = (isChange) || (manager.isPostgreSQL()) || (manager.isDB2()) || (manager.isInformix()) || (manager.isTamino3()) || (manager.isMySQL());
/*      */ 
/*  662 */     for (int i = 0; i < fieldInfo.size(); ++i)
/*      */     {
/*  664 */       if (oneAtATime)
/*      */       {
/*  666 */         tempFieldInfo = new IdcVector();
/*  667 */         tempFieldInfo.addElement(fieldInfo.elementAt(i));
/*      */       }
/*      */       else
/*      */       {
/*  671 */         tempFieldInfo = fieldInfo;
/*      */       }
/*      */ 
/*  674 */       String tableChangeRoot = "ALTER TABLE " + table + " " + command + " ";
/*  675 */       if ((manager.isOracle()) || (manager.isInformix()))
/*      */       {
/*  677 */         tableChangeRoot = tableChangeRoot + "(";
/*      */       }
/*  679 */       IdcStringBuilder queryDef = new IdcStringBuilder(tableChangeRoot);
/*  680 */       IdcStringBuilder querySuffix = new IdcStringBuilder();
/*  681 */       appendTableDefEx(queryDef, querySuffix, tempFieldInfo, null, 0, tempFieldInfo.size(), manager, appendAllowNullStr, isChange);
/*      */ 
/*  683 */       if ((manager.isOracle()) || (manager.isInformix()))
/*      */       {
/*  685 */         queryDef.append(")");
/*      */       }
/*  687 */       queryDef.append(' ');
/*  688 */       queryDef.append(querySuffix);
/*  689 */       String query = queryDef.toString();
/*  690 */       executeUpdateQuery(con, query, manager);
/*  691 */       if (!oneAtATime)
/*      */         return;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void appendTableDef(IdcStringBuilder tableDef, Vector fieldInfo, String[] pkCols, int start, int end, JdbcManager manager)
/*      */   {
/*  701 */     appendTableDefEx(tableDef, fieldInfo, pkCols, start, end, manager, true);
/*      */   }
/*      */ 
/*      */   public static void appendTableDefEx(IdcStringBuilder tableDef, Vector fieldInfo, String[] pkCols, int start, int end, JdbcManager manager, boolean appendAllowNullStr)
/*      */   {
/*  710 */     appendTableDefEx(tableDef, null, fieldInfo, pkCols, start, end, manager, appendAllowNullStr, false);
/*      */   }
/*      */ 
/*      */   protected static void appendTableDefEx(IdcStringBuilder tableDef, IdcStringBuilder tableDefSuffix, Vector fieldInfo, String[] pkCols, int start, int end, JdbcManager manager, boolean appendAllowNullStr, boolean isChange)
/*      */   {
/*  718 */     for (int i = start; i < end; ++i)
/*      */     {
/*  720 */       FieldInfo fi = (FieldInfo)fieldInfo.elementAt(i);
/*  721 */       tableDef.append(fi.m_name);
/*  722 */       tableDef.append(" ");
/*      */ 
/*  726 */       if ((isChange) && (manager.isPostgreSQL()) && (manager.supportsSqlColumnChange()))
/*      */       {
/*  728 */         tableDef.append("TYPE ");
/*      */       }
/*  730 */       boolean isPrimaryCol = false;
/*  731 */       if (pkCols != null)
/*      */       {
/*  733 */         isPrimaryCol = StringUtils.findStringIndexEx(pkCols, fi.m_name, true) >= 0;
/*      */       }
/*      */ 
/*  736 */       appendDataTypeDesEx(tableDef, tableDefSuffix, fi, isPrimaryCol, manager, appendAllowNullStr, isChange);
/*  737 */       if (i >= end - 1)
/*      */         continue;
/*  739 */       tableDef.append(", ");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void appendDataTypeDes(IdcStringBuilder buf, FieldInfo fi, boolean noNulls, JdbcManager manager)
/*      */   {
/*  747 */     appendDataTypeDesEx(buf, fi, noNulls, manager, true);
/*      */   }
/*      */ 
/*      */   public static void appendDataTypeDesEx(IdcStringBuilder buf, FieldInfo fi, boolean noNulls, JdbcManager manager, boolean appendAllowNullStr)
/*      */   {
/*  753 */     appendDataTypeDesEx(buf, null, fi, noNulls, manager, appendAllowNullStr, false);
/*      */   }
/*      */ 
/*      */   protected static void appendDataTypeDesEx(IdcStringBuilder buf, IdcStringBuilder suffix, FieldInfo fi, boolean noNulls, JdbcManager manager, boolean appendAllowNullStr, boolean isChange)
/*      */   {
/*  759 */     DatabaseConfigData config = manager.m_config;
/*  760 */     boolean usePre80MetaFieldType = config.getValueAsBool("UsePre80MetaFieldType", false);
/*      */ 
/*  763 */     String appendStr = null;
/*  764 */     boolean hasLen = false;
/*  765 */     switch (fi.m_type)
/*      */     {
/*      */     case 1:
/*  768 */       if (manager.isOracle())
/*      */       {
/*  770 */         appendStr = "number(1) DEFAULT (0)";
/*      */       }
/*  772 */       else if ((manager.isInformix()) || (manager.isDB2()) || (manager.isTamino3()))
/*      */       {
/*  776 */         appendStr = "smallint DEFAULT 0";
/*      */       }
/*  779 */       else if (manager.isPostgreSQL())
/*      */       {
/*  781 */         if (config.getValueAsString("DatabaseVersion").indexOf("08") == 0)
/*      */         {
/*  783 */           appendStr = "numeric(1,0) DEFAULT (0)";
/*  784 */           noNulls = true;
/*      */         }
/*      */         else
/*      */         {
/*  788 */           appendStr = "numeric(1,0)";
/*      */         }
/*      */       }
/*  791 */       else if (manager.isSybase())
/*      */       {
/*  793 */         appendStr = "bit DEFAULT (0)";
/*  794 */         noNulls = true;
/*      */       }
/*  796 */       else if (manager.isMySQL())
/*      */       {
/*  798 */         appendStr = "tinyint(1) DEFAULT 0";
/*  799 */         noNulls = true;
/*      */       }
/*      */       else
/*      */       {
/*  803 */         appendStr = "bit DEFAULT (0)";
/*  804 */         noNulls = true;
/*      */       }
/*  806 */       break;
/*      */     case 2:
/*      */     case 6:
/*      */     case 8:
/*  810 */       if ((!fi.m_isFixedLen) || (fi.m_maxLen >= config.getValueAsInt("MemoMandateMinSize", 1000000)))
/*      */       {
/*  813 */         if ((manager.isDB2()) && (isChange))
/*      */         {
/*  815 */           appendStr = "SET DATA TYPE varchar";
/*  816 */           if (manager.useUnicode())
/*      */           {
/*  818 */             appendStr = "vargraphic";
/*  819 */             if (!usePre80MetaFieldType)
/*      */             {
/*  821 */               appendStr = "long " + appendStr;
/*      */             }
/*  823 */             appendStr = "SET DATA TYPE " + appendStr;
/*      */           }
/*      */         }
/*  826 */         else if (manager.isTamino3())
/*      */         {
/*  828 */           appendStr = "char";
/*      */         }
/*  831 */         else if ((manager.isInformix()) && (config.getValueAsInt("MemoFieldSize", 2000) > 255))
/*      */         {
/*  833 */           appendStr = "lvarchar";
/*      */         }
/*  835 */         else if (manager.useUnicode())
/*      */         {
/*  837 */           if (manager.isDB2())
/*      */           {
/*  839 */             appendStr = "vargraphic";
/*  840 */             if (!usePre80MetaFieldType)
/*      */             {
/*  842 */               appendStr = "long " + appendStr;
/*      */             }
/*      */           }
/*  845 */           else if (manager.isOracle())
/*      */           {
/*  847 */             appendStr = "nvarchar2";
/*      */           }
/*  849 */           else if (manager.isSybase())
/*      */           {
/*  851 */             appendStr = "univarchar";
/*      */           }
/*  853 */           else if ((manager.isSqlServer()) && (!usePre80MetaFieldType))
/*      */           {
/*  855 */             String version = manager.getDatabaseVersion();
/*  856 */             if (version.compareTo("09") < 0)
/*      */             {
/*  858 */               appendStr = "ntext";
/*      */             }
/*      */             else
/*      */             {
/*  862 */               appendStr = "nvarchar(max)";
/*      */             }
/*      */           }
/*  865 */           else if (manager.isMySQL())
/*      */           {
/*  867 */             appendStr = "varchar";
/*      */           }
/*      */           else
/*      */           {
/*  871 */             appendStr = "nvarchar";
/*      */           }
/*      */ 
/*      */         }
/*  876 */         else if (manager.isSqlServer())
/*      */         {
/*  878 */           if (usePre80MetaFieldType)
/*      */           {
/*  880 */             appendStr = "varchar";
/*      */           }
/*      */           else
/*      */           {
/*  884 */             String version = manager.getDatabaseVersion();
/*  885 */             if (version.compareTo("09") < 0)
/*      */             {
/*  887 */               appendStr = "text";
/*      */             }
/*      */             else
/*      */             {
/*  891 */               appendStr = "varchar(max)";
/*      */             }
/*      */           }
/*      */         }
/*  895 */         else if ((manager.isDB2()) && (!usePre80MetaFieldType))
/*      */         {
/*  897 */           appendStr = "long varchar";
/*      */         }
/*      */         else
/*      */         {
/*  901 */           appendStr = "varchar";
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  907 */         hasLen = true;
/*  908 */         if (fi.m_type == 2)
/*      */         {
/*  910 */           appendStr = "char";
/*      */ 
/*  912 */           if (manager.useUnicode())
/*      */           {
/*  914 */             if (manager.isDB2())
/*      */             {
/*  916 */               appendStr = "graphic";
/*      */             }
/*  918 */             else if (manager.isSybase())
/*      */             {
/*  920 */               appendStr = "unichar";
/*      */             }
/*  922 */             else if (manager.isMySQL())
/*      */             {
/*  924 */               appendStr = "char";
/*      */             }
/*      */             else
/*      */             {
/*  929 */               appendStr = "nchar";
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*  935 */         else if ((manager.isDB2()) && (isChange))
/*      */         {
/*  937 */           appendStr = "SET DATA TYPE varchar";
/*  938 */           if (manager.useUnicode())
/*      */           {
/*  940 */             appendStr = "SET DATA TYPE vargraphic";
/*      */           }
/*      */         }
/*  943 */         else if (manager.isTamino3())
/*      */         {
/*  945 */           appendStr = "char";
/*      */         }
/*  947 */         else if ((manager.isInformix()) && (fi.m_maxLen > 255))
/*      */         {
/*  949 */           appendStr = "lvarchar";
/*      */         }
/*      */         else
/*      */         {
/*  953 */           appendStr = "varchar";
/*  954 */           if (manager.useUnicode())
/*      */           {
/*  956 */             if (manager.isDB2())
/*      */             {
/*  958 */               appendStr = "vargraphic";
/*      */             }
/*  960 */             else if (manager.isOracle())
/*      */             {
/*  962 */               appendStr = "nvarchar2";
/*      */             }
/*  964 */             else if (manager.isSybase())
/*      */             {
/*  966 */               appendStr = "univarchar";
/*      */             }
/*  968 */             else if (manager.isMySQL())
/*      */             {
/*  970 */               appendStr = "varchar";
/*      */             }
/*      */             else
/*      */             {
/*  975 */               appendStr = "nvarchar"; } 
/*      */           }
/*      */         }
/*  975 */       }break;
/*      */     case 3:
/*  983 */       if (!config.getValueAsBool("DisableDBLongIntegerField", false))
/*      */       {
/*  985 */         if (manager.isTamino())
/*      */         {
/*  987 */           appendStr = "fixed(18)";
/*      */         }
/*  989 */         else if (manager.isSybase())
/*      */         {
/*  991 */           appendStr = "numeric(19)";
/*      */         }
/*  993 */         else if (manager.isInformix())
/*      */         {
/*  995 */           appendStr = "numeric(19, 0)";
/*      */         }
/*  997 */         else if (manager.isPostgreSQL())
/*      */         {
/*  999 */           appendStr = "int8";
/*      */         }
/* 1001 */         else if ((manager.isOracle()) || (manager.isTamino3()))
/*      */         {
/* 1004 */           appendStr = "int";
/*      */         }
/*      */         else
/*      */         {
/* 1008 */           appendStr = "bigint";
/*      */         }
/*      */ 
/*      */       }
/* 1013 */       else if ((manager.isDB2()) || (manager.isTamino()))
/*      */       {
/* 1015 */         appendStr = "integer";
/*      */       }
/*      */       else
/*      */       {
/* 1019 */         appendStr = "int";
/*      */       }
/*      */ 
/* 1022 */       break;
/*      */     case 4:
/* 1024 */       appendStr = "float";
/* 1025 */       break;
/*      */     case 5:
/* 1027 */       if (manager.isOracle())
/*      */       {
/* 1029 */         String version = manager.getDatabaseVersion();
/* 1030 */         if ((version.compareTo("09") < 0) || (getOptionAsBool(config, fi, "DisableDBDateEnhancement", false)))
/*      */         {
/* 1032 */           appendStr = "date";
/*      */         }
/*      */         else
/*      */         {
/* 1036 */           appendStr = "timestamp";
/*      */         }
/*      */       }
/* 1039 */       else if (manager.isInformix())
/*      */       {
/* 1045 */         if (manager.getDisableDateEnhancement())
/*      */         {
/* 1047 */           appendStr = "datetime year to second";
/*      */         }
/*      */         else
/*      */         {
/* 1051 */           appendStr = "datetime year to fraction";
/*      */         }
/*      */       }
/* 1054 */       else if ((manager.isDB2()) || (manager.isPostgreSQL()) || (manager.isTamino()))
/*      */       {
/* 1056 */         appendStr = "timestamp";
/*      */       }
/* 1058 */       else if (manager.isTamino3())
/*      */       {
/* 1060 */         appendStr = "integer";
/*      */       }
/*      */       else
/*      */       {
/* 1064 */         appendStr = "datetime";
/*      */       }
/* 1066 */       break;
/*      */     case 7:
/* 1068 */       if (manager.isOracle())
/*      */       {
/* 1070 */         appendStr = "long raw";
/*      */       }
/* 1072 */       else if (manager.isTamino3())
/*      */       {
/* 1074 */         appendStr = "binary";
/*      */       }
/* 1076 */       else if (manager.isInformix())
/*      */       {
/* 1078 */         appendStr = "byte";
/*      */       }
/* 1080 */       else if (manager.isPostgreSQL())
/*      */       {
/* 1082 */         appendStr = "bytea";
/*      */       }
/* 1084 */       else if (manager.isMySQL())
/*      */       {
/* 1086 */         appendStr = "blob";
/*      */       }
/*      */       else
/*      */       {
/* 1090 */         appendStr = "image";
/*      */       }
/* 1092 */       break;
/*      */     case 9:
/* 1094 */       if (manager.isOracle())
/*      */       {
/* 1096 */         if (!isChange)
/*      */         {
/* 1098 */           appendStr = "blob";
/*      */         }
/* 1100 */         addSecureFileSuffixes(config, fi, suffix, isChange);
/*      */       }
/* 1102 */       else if (manager.isInformix())
/*      */       {
/* 1104 */         appendStr = "byte";
/*      */       }
/* 1106 */       else if (manager.isDB2())
/*      */       {
/* 1108 */         appendStr = "blob";
/* 1109 */         hasLen = true;
/*      */       }
/* 1111 */       else if (manager.isPostgreSQL())
/*      */       {
/* 1113 */         appendStr = "bytea";
/*      */       }
/* 1115 */       else if (manager.isSqlServer())
/*      */       {
/* 1117 */         String version = manager.getDatabaseVersion();
/* 1118 */         if (version.compareTo("05") < 0)
/*      */         {
/* 1120 */           appendStr = "image";
/*      */         }
/*      */         else
/*      */         {
/* 1124 */           appendStr = "varbinary(max)";
/*      */         }
/*      */       }
/* 1127 */       else if (manager.isMySQL())
/*      */       {
/* 1129 */         appendStr = "blob";
/*      */       }
/*      */       else
/*      */       {
/* 1133 */         appendStr = "image";
/*      */       }
/* 1135 */       break;
/*      */     case 11:
/* 1137 */       String precision = fi.m_maxLen + "";
/* 1138 */       String scale = fi.m_scale + "";
/* 1139 */       appendStr = addDecimalType(manager, precision, scale);
/* 1140 */       break;
/*      */     case 10:
/* 1142 */       if (manager.isSqlServer())
/*      */       {
/* 1144 */         String version = manager.getDatabaseVersion();
/* 1145 */         if (version.compareTo("09") < 0)
/*      */         {
/* 1147 */           appendStr = "text";
/*      */         }
/*      */         else
/*      */         {
/* 1151 */           appendStr = "varchar(max)";
/*      */         }
/*      */       }
/* 1154 */       else if (manager.isOracle())
/*      */       {
/* 1156 */         if (!isChange)
/*      */         {
/* 1158 */           appendStr = "clob";
/*      */         }
/*      */ 
/* 1161 */         addSecureFileSuffixes(config, fi, suffix, isChange);
/*      */       }
/* 1163 */       else if (manager.isDB2())
/*      */       {
/* 1165 */         appendStr = "clob";
/* 1166 */         if (manager.useUnicode())
/*      */         {
/* 1168 */           appendStr = "dbclob";
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1174 */         appendStr = "text";
/*      */       }
/*      */     }
/*      */ 
/* 1178 */     int maxlen = fi.m_maxLen;
/* 1179 */     if (((fi.m_type == 6) && (!hasLen)) || ((fi.m_type == 8) && ((
/* 1186 */       (usePre80MetaFieldType) || ((!manager.isSqlServer()) && (!manager.isDB2()))))))
/*      */     {
/* 1188 */       hasLen = true;
/* 1189 */       boolean setMemoToMaxSize = StringUtils.convertToBool(FieldInfoUtils.getFieldOption(fi, "setMemoToMaxSize"), false);
/* 1190 */       if (setMemoToMaxSize)
/*      */       {
/* 1193 */         String oldKey = SharedObjects.getEnvironmentValue("ComponentDBInstallMaxMemoSize");
/* 1194 */         if ((oldKey != null) && (oldKey.length() > 0))
/*      */         {
/* 1196 */           maxlen = SharedObjects.getEnvironmentInt("ComponentDBInstallMaxMemoSixe", 4000);
/*      */         }
/*      */         else
/*      */         {
/* 1200 */           maxlen = SharedObjects.getEnvironmentInt("DataStoreDesignMaxMemoSize", 4000);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1205 */         maxlen = config.getValueAsInt("MinMemoFieldSize", 2000);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1210 */     buf.append(appendStr);
/*      */ 
/* 1212 */     if (hasLen)
/*      */     {
/* 1214 */       buf.append("(" + maxlen + ")");
/*      */     }
/* 1216 */     if (!appendAllowNullStr)
/*      */       return;
/* 1218 */     if (noNulls)
/*      */     {
/* 1220 */       buf.append(" NOT NULL");
/*      */     }
/*      */     else
/*      */     {
/* 1226 */       if ((manager.isInformix()) || (manager.isDB2()) || (manager.isPostgreSQL()) || (manager.isTamino3())) {
/*      */         return;
/*      */       }
/* 1229 */       buf.append(" NULL");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addSecureFileSuffixes(DatabaseConfigData config, FieldInfo fi, IdcStringBuilder suffix, boolean isChange)
/*      */   {
/* 1238 */     if (!config.getValueAsBool("UseSecureFiles", true))
/*      */       return;
/* 1240 */     if (suffix.length() > 0)
/*      */     {
/* 1242 */       suffix.append(',');
/*      */     }
/* 1244 */     suffix.append("LOB(");
/* 1245 */     suffix.append(fi.m_name);
/* 1246 */     suffix.append(')');
/* 1247 */     suffix.append(' ');
/* 1248 */     if (!isChange)
/*      */     {
/* 1250 */       suffix.append(" STORE AS SECUREFILE");
/*      */     }
/* 1252 */     suffix.append(' ');
/* 1253 */     String options = getOption(config, fi, "SecureFileOptions");
/* 1254 */     if ((options == null) || (options.length() == 0))
/*      */       return;
/* 1256 */     suffix.append('(');
/* 1257 */     suffix.append(options);
/* 1258 */     suffix.append(')');
/*      */   }
/*      */ 
/*      */   public static String getOption(DatabaseConfigData config, FieldInfo fi, String key)
/*      */   {
/* 1265 */     String value = FieldInfoUtils.getFieldOption(fi, key);
/* 1266 */     if (value == null)
/*      */     {
/* 1268 */       value = config.getValueAsString(key);
/*      */     }
/* 1270 */     return value;
/*      */   }
/*      */ 
/*      */   public static boolean getOptionAsBool(DatabaseConfigData config, FieldInfo fi, String key, boolean defaultBool)
/*      */   {
/* 1275 */     boolean value = defaultBool;
/* 1276 */     String valueStr = FieldInfoUtils.getFieldOption(fi, key);
/* 1277 */     if (valueStr == null)
/*      */     {
/* 1279 */       value = config.getValueAsBool(key, value);
/*      */     }
/*      */     else
/*      */     {
/* 1283 */       value = StringUtils.convertToBool(valueStr, value);
/*      */     }
/* 1285 */     return value;
/*      */   }
/*      */ 
/*      */   public static String addDecimalType(JdbcManager manager, String precision, String scale)
/*      */   {
/*      */     String columnDeclaration;
/*      */     String columnDeclaration;
/* 1292 */     if (manager.isPostgreSQL()) {
/* 1293 */       columnDeclaration = "numeric(" + precision + ", " + scale + ")";
/*      */     }
/*      */     else
/*      */     {
/* 1297 */       columnDeclaration = "decimal(" + precision + ", " + scale + ")";
/*      */     }
/*      */ 
/* 1300 */     return columnDeclaration;
/*      */   }
/*      */ 
/*      */   public static boolean copyTablesInternal(DatabaseMetaData dbMetaData, JdbcConnection jCon, JdbcManager manager, Vector tableFields, String tableFrom, String tableTo)
/*      */     throws SQLException
/*      */   {
/* 1306 */     Vector fromTableFields = getFieldListInternal(jCon, manager, tableFrom);
/* 1307 */     Vector toTableFields = getFieldListInternal(jCon, manager, tableTo);
/* 1308 */     int nfields = tableFields.size();
/* 1309 */     if ((nfields != fromTableFields.size()) || (nfields != toTableFields.size()))
/*      */     {
/* 1311 */       return false;
/*      */     }
/* 1313 */     for (int i = 0; i < nfields; ++i)
/*      */     {
/* 1315 */       FieldInfo fi = (FieldInfo)tableFields.elementAt(i);
/* 1316 */       FieldInfo fromfi = (FieldInfo)fromTableFields.elementAt(i);
/* 1317 */       FieldInfo tofi = (FieldInfo)toTableFields.elementAt(i);
/* 1318 */       if (!compareFieldInfo(fi, fromfi))
/*      */       {
/* 1320 */         return false;
/*      */       }
/* 1322 */       if (!compareFieldInfo(fi, tofi))
/*      */       {
/* 1324 */         return false;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1329 */     String deleteStmt = "delete from " + tableTo;
/* 1330 */     Connection con = (Connection)jCon.getConnection();
/* 1331 */     executeUpdateQuery(con, deleteStmt, manager);
/*      */ 
/* 1333 */     String sqlFieldList = createSqlFieldList(tableFields, 0, nfields);
/* 1334 */     manager.debugMsg("List of Fields to copy from " + tableFrom + " to " + tableTo + ": " + sqlFieldList);
/*      */ 
/* 1336 */     executeTableCopyQuery(con, tableFrom, tableTo, sqlFieldList);
/* 1337 */     return true;
/*      */   }
/*      */ 
/*      */   public static void executeCreateTableQuery(Connection con, String table, String tableDef, String pkConstraintName, String pkConstraintString, String tableDefSuffix, JdbcManager manager)
/*      */     throws SQLException
/*      */   {
/* 1344 */     String createSql = "CREATE TABLE " + table + " (" + tableDef;
/* 1345 */     if (pkConstraintString != null)
/*      */     {
/* 1347 */       if (manager.isInformix())
/*      */       {
/* 1349 */         createSql = createSql + ", " + pkConstraintString + " CONSTRAINT " + pkConstraintName;
/*      */       }
/*      */       else
/*      */       {
/* 1354 */         createSql = createSql + ", CONSTRAINT " + pkConstraintName + pkConstraintString;
/*      */       }
/*      */     }
/* 1357 */     createSql = createSql + ") ";
/* 1358 */     if (manager.isMySQL())
/*      */     {
/* 1360 */       createSql = createSql + "ENGINE=InnoDB ";
/*      */     }
/* 1362 */     createSql = createSql + tableDefSuffix;
/* 1363 */     executeUpdateQuery(con, createSql, manager);
/*      */   }
/*      */ 
/*      */   public static void executeTableCopyQuery(Connection con, String tableFrom, String tableTo, String fieldListClause)
/*      */     throws SQLException
/*      */   {
/* 1369 */     executeTableCopyQueryEx(con, tableFrom, tableTo, fieldListClause, fieldListClause, null);
/*      */   }
/*      */ 
/*      */   public static void executeTableCopyQueryEx(Connection con, String tableFrom, String tableTo, String insertFieldListClause, String selectFieldListClause)
/*      */     throws SQLException
/*      */   {
/* 1375 */     executeTableCopyQueryEx(con, tableFrom, tableTo, insertFieldListClause, selectFieldListClause, null);
/*      */   }
/*      */ 
/*      */   public static void executeTableCopyQueryEx(Connection con, String tableFrom, String tableTo, String insertFieldListClause, String selectFieldListClause, JdbcManager manager)
/*      */     throws SQLException
/*      */   {
/* 1382 */     String insertSql = "insert into " + tableTo + " (" + insertFieldListClause + ") select " + selectFieldListClause + " from " + tableFrom;
/*      */ 
/* 1384 */     executeUpdateQuery(con, insertSql, manager);
/*      */   }
/*      */ 
/*      */   public static String createSqlFieldList(Vector tableFields, int start, int end)
/*      */   {
/* 1389 */     return createSqlFieldListEx(tableFields, start, end, null);
/*      */   }
/*      */ 
/*      */   public static String createSqlFieldListEx(Vector tableFields, int start, int end, String[] useDefaultFields)
/*      */   {
/* 1395 */     String retVal = "";
/* 1396 */     for (int i = start; i < end; ++i)
/*      */     {
/* 1398 */       FieldInfo fi = (FieldInfo)tableFields.elementAt(i);
/* 1399 */       boolean useDefault = false;
/* 1400 */       if (useDefaultFields != null)
/*      */       {
/* 1402 */         int index = StringUtils.findStringIndexEx(useDefaultFields, fi.m_name, true);
/*      */ 
/* 1404 */         if (index >= 0)
/*      */         {
/* 1406 */           useDefault = true;
/*      */         }
/*      */       }
/* 1409 */       if (useDefault == true)
/*      */       {
/* 1411 */         String def = "' '";
/* 1412 */         switch (fi.m_type)
/*      */         {
/*      */         case 1:
/*      */         case 3:
/* 1416 */           def = "0";
/* 1417 */           break;
/*      */         case 4:
/* 1419 */           def = "0.0";
/*      */         case 2:
/*      */         }
/* 1422 */         retVal = retVal + def;
/*      */       }
/*      */       else
/*      */       {
/* 1426 */         retVal = retVal + fi.m_name;
/*      */       }
/* 1428 */       if (i >= end - 1)
/*      */         continue;
/* 1430 */       retVal = retVal + ", ";
/*      */     }
/*      */ 
/* 1433 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static int executeUpdateQuery(Connection con, String query)
/*      */     throws SQLException
/*      */   {
/* 1441 */     SystemUtils.reportDeprecatedUsage("executeUpdateQuery(Connection, String) has been changed to executeUpdateQuery(Connection, String, JdbcManager). Please update your code.");
/*      */ 
/* 1444 */     return executeUpdateQuery(con, query, null);
/*      */   }
/*      */ 
/*      */   public static int executeUpdateQuery(Connection con, String query, JdbcManager manager)
/*      */     throws SQLException
/*      */   {
/* 1450 */     Statement stmt = con.createStatement();
/* 1451 */     int result = 0;
/* 1452 */     if (manager != null)
/*      */     {
/* 1454 */       manager.debugMsg(query);
/* 1455 */       manager.checkAndLogDDL(query);
/*      */     }
/*      */     try
/*      */     {
/* 1459 */       result = stmt.executeUpdate(query);
/*      */     }
/*      */     finally
/*      */     {
/* 1463 */       stmt.close();
/*      */     }
/*      */ 
/* 1466 */     return result;
/*      */   }
/*      */ 
/*      */   public static intradoc.data.ResultSet createResultSet(JdbcConnection jCon, String query, JdbcManager manager)
/*      */     throws SQLException
/*      */   {
/* 1472 */     Connection con = (Connection)jCon.getConnection();
/* 1473 */     Statement stmt = con.createStatement();
/* 1474 */     manager.debugMsg(query);
/*      */ 
/* 1476 */     JdbcResultSet jdbcRset = null;
/*      */     try
/*      */     {
/* 1479 */       java.sql.ResultSet result = stmt.executeQuery(query);
/*      */ 
/* 1482 */       jdbcRset = new JdbcResultSet(manager);
/* 1483 */       jdbcRset.setQueryInfo(stmt, query, jCon, result);
/* 1484 */       JdbcResultSet localJdbcResultSet1 = jdbcRset;
/*      */ 
/* 1490 */       return localJdbcResultSet1;
/*      */     }
/*      */     finally
/*      */     {
/* 1488 */       if ((jdbcRset == null) && (stmt != null))
/*      */       {
/* 1490 */         stmt.close();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void dropTableInternal(Connection con, String table)
/*      */     throws SQLException
/*      */   {
/* 1500 */     SystemUtils.reportDeprecatedUsage("dropTableInternal(Connection, String) has been changed to dropTableInternal(Connection, String, JdbcManager). Please update your code.");
/*      */ 
/* 1503 */     dropTableInternal(con, table, null);
/*      */   }
/*      */ 
/*      */   public static void dropViewInternal(Connection con, String view, JdbcManager manager) throws SQLException
/*      */   {
/* 1508 */     executeUpdateQuery(con, "drop view " + view, manager);
/*      */   }
/*      */ 
/*      */   public static boolean hasEditioningView(JdbcConnection jCon, String table, JdbcManager manager)
/*      */   {
/* 1513 */     boolean hasView = false;
/* 1514 */     JdbcResultSet jdbcRset = null;
/* 1515 */     Statement stmt = null;
/*      */     try
/*      */     {
/* 1519 */       String queryStr = "SELECT * FROM all_views where view_name like '" + table.toUpperCase() + "'";
/*      */ 
/* 1521 */       Connection con = (Connection)jCon.getConnection();
/* 1522 */       stmt = con.createStatement();
/* 1523 */       java.sql.ResultSet rset = stmt.executeQuery(queryStr);
/*      */ 
/* 1526 */       jdbcRset = new JdbcResultSet(manager);
/* 1527 */       jdbcRset.setQueryInfo(stmt, queryStr, jCon, rset);
/*      */ 
/* 1530 */       hasView = !jdbcRset.isEmpty();
/*      */     }
/*      */     catch (Exception t)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/* 1539 */       if (jdbcRset != null)
/*      */       {
/* 1541 */         jdbcRset.closeInternals();
/*      */       }
/* 1543 */       if (stmt != null)
/*      */       {
/*      */         try
/*      */         {
/* 1547 */           stmt.close();
/*      */         }
/*      */         catch (Throwable t)
/*      */         {
/* 1551 */           Report.trace(null, "Unable to close statement while executing hasEditioningView()", t);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1556 */     return hasView;
/*      */   }
/*      */ 
/*      */   public static void createEditioningViewInternal(JdbcConnection jCon, String view, JdbcManager manager) throws SQLException
/*      */   {
/* 1561 */     String tableName = view + "_";
/* 1562 */     Vector tableFields = getFieldListInternal(jCon, manager, tableName);
/* 1563 */     String editioningViewStatement = "create editioning view " + view + " as select ";
/*      */ 
/* 1565 */     if ((tableFields != null) && (tableFields.size() > 0))
/*      */     {
/* 1567 */       for (FieldInfo field : tableFields)
/*      */       {
/* 1569 */         editioningViewStatement = editioningViewStatement + field.m_name + ",";
/*      */       }
/*      */ 
/* 1572 */       editioningViewStatement = editioningViewStatement.substring(0, editioningViewStatement.length() - 1);
/*      */ 
/* 1574 */       editioningViewStatement = editioningViewStatement + " from " + tableName;
/*      */     }
/*      */ 
/* 1577 */     executeUpdateQuery((Connection)jCon.getConnection(), editioningViewStatement, manager);
/*      */   }
/*      */ 
/*      */   public static void dropTableInternal(Connection con, String table, JdbcManager manager)
/*      */     throws SQLException
/*      */   {
/* 1585 */     executeUpdateQuery(con, "drop table " + table, manager);
/*      */   }
/*      */ 
/*      */   public static void dropColumns(Connection con, String table, String[] cols, JdbcManager manager) throws SQLException
/*      */   {
/* 1590 */     String queryRoot = "ALTER TABLE " + table + " " + manager.getColumnDeleteSqlCommand() + " ";
/*      */ 
/* 1592 */     for (int i = 0; i < cols.length; ++i)
/*      */     {
/* 1594 */       manager.getColumnMap().remove(cols[i].toUpperCase());
/* 1595 */       String query = queryRoot + cols[i];
/* 1596 */       executeUpdateQuery(con, query, manager);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static boolean compareFieldInfo(FieldInfo fi1, FieldInfo fi2)
/*      */   {
/* 1603 */     if (!fi1.m_name.equals(fi2.m_name))
/*      */     {
/* 1605 */       return false;
/*      */     }
/* 1607 */     if (fi1.m_type != fi2.m_type)
/*      */     {
/* 1609 */       return false;
/*      */     }
/*      */ 
/* 1613 */     return fi1.m_maxLen == fi2.m_maxLen;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1620 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98563 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.JdbcFunctions
 * JD-Core Version:    0.5.4
 */