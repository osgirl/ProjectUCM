/*      */ package intradoc.server.datastoredesign;
/*      */ 
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DatabaseConfigData;
/*      */ import intradoc.data.DatabaseIndexInfo;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.FieldInfoUtils;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.provider.ProviderConfigUtils;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DataDesignGenerator
/*      */ {
/*      */   public static final int NOCHANGE = 0;
/*      */   public static final int CHANGED = 1;
/*      */   public static final int NEW = 2;
/*      */   public static final int FAIL = 3;
/*      */   protected boolean m_init;
/*      */   protected boolean m_ebrMode;
/*      */   protected boolean m_generateSQLOnly;
/*      */   protected boolean m_dbSyntaxFound;
/*      */   protected Workspace m_ws;
/*      */   protected String m_dbType;
/*      */   protected Map<String, String> m_dsdColumnUpgradeMap;
/*      */   protected String[] m_cachedTableList;
/*      */   protected String m_wsName;
/*      */   protected String m_dbText;
/*      */   protected String m_dbMemo;
/*      */   protected String m_dbDate;
/*      */   protected String m_dbNumber;
/*      */   protected String m_dbCharacter;
/*      */   protected String m_dbBoolean;
/*      */   protected String m_dbClob;
/*      */   protected String m_dbBlob;
/*      */   protected String m_dbAllowNull;
/*      */   protected String m_dbNotAllowNull;
/*      */   protected String m_dbEndStatement;
/*      */   protected String m_dbMaxMemoSize;
/*      */   protected Map<String, String[]> m_cachedTableListMap;
/*      */   protected Map<String, Properties> m_dbProperties;
/*      */ 
/*      */   public DataDesignGenerator()
/*      */   {
/*   60 */     this.m_init = false;
/*   61 */     this.m_ebrMode = false;
/*   62 */     this.m_generateSQLOnly = false;
/*   63 */     this.m_dbSyntaxFound = false;
/*   64 */     this.m_ws = null;
/*      */ 
/*   66 */     this.m_dsdColumnUpgradeMap = new HashMap();
/*   67 */     this.m_cachedTableList = null;
/*      */ 
/*   85 */     this.m_cachedTableListMap = new HashMap();
/*   86 */     this.m_dbProperties = new HashMap();
/*      */   }
/*      */ 
/*      */   public void init(Map<String, Workspace> workspaceMap)
/*      */     throws ServiceException
/*      */   {
/*   95 */     for (Map.Entry entry : workspaceMap.entrySet())
/*      */     {
/*   97 */       String name = (String)entry.getKey();
/*   98 */       Workspace ws = (Workspace)entry.getValue();
/*   99 */       if (ws == null)
/*      */       {
/*  101 */         this.m_dbProperties.clear();
/*  102 */         return;
/*      */       }
/*  104 */       initWorkspace(name, ws);
/*      */     }
/*  106 */     this.m_init = true;
/*  107 */     this.m_dsdColumnUpgradeMap = DataDesignInstallUtils.getFieldNameUpgradeMap();
/*      */   }
/*      */ 
/*      */   public void initWorkspace(String name, Workspace ws)
/*      */     throws ServiceException
/*      */   {
/*  119 */     String dbType = ws.getProperty("DatabaseType");
/*  120 */     dbType = dbType.toLowerCase();
/*  121 */     Properties prop = new Properties();
/*  122 */     prop.put("ebrMode", Boolean.valueOf(false));
/*  123 */     if (dbType.startsWith("mssql"))
/*      */     {
/*  126 */       String dbVer = ws.getProperty("DatabaseVersion");
/*  127 */       if (dbVer.matches(".*2000.*"))
/*      */       {
/*  129 */         dbType = "mssqlserver2000";
/*      */       }
/*      */     }
/*  132 */     if (dbType.startsWith("oracle"))
/*      */     {
/*  135 */       String dbVer = ws.getProperty("DatabaseVersion");
/*  136 */       if (dbVer.matches(".*10g.*"))
/*      */       {
/*  138 */         dbType = "oracle10g";
/*      */       }
/*      */ 
/*  142 */       if (WorkspaceUtils.EBRModeActive(ws))
/*      */       {
/*  144 */         dbType = "oracleebr";
/*  145 */         prop.put("ebrMode", Boolean.valueOf(true));
/*      */       }
/*      */     }
/*  148 */     boolean isUnicode = StringUtils.convertToBool(ws.getProperty("IsUniversalUnicodeDatabase"), false);
/*  149 */     if (isUnicode)
/*      */     {
/*  151 */       dbType = dbType + "_unicode";
/*      */     }
/*      */ 
/*  154 */     if (!determineDBSyntax(prop, dbType))
/*      */     {
/*  159 */       dbType = ws.getProperty("DatabaseName");
/*  160 */       if (isUnicode)
/*      */       {
/*  162 */         dbType = dbType + "_unicode";
/*      */       }
/*  164 */       dbType = dbType.toLowerCase();
/*  165 */       determineDBSyntax(prop, dbType);
/*      */     }
/*      */ 
/*  168 */     prop.put("dbEndStatement", "");
/*  169 */     prop.put("dbType", dbType);
/*  170 */     prop.put("ws", ws);
/*  171 */     this.m_dbProperties.put(name, prop);
/*      */   }
/*      */ 
/*      */   public void loadWorkspaceInfo(String workspaceName)
/*      */   {
/*  180 */     this.m_wsName = workspaceName;
/*  181 */     this.m_cachedTableList = ((String[])this.m_cachedTableListMap.get(this.m_wsName));
/*  182 */     Properties prop = (Properties)this.m_dbProperties.get(this.m_wsName);
/*      */ 
/*  184 */     this.m_ebrMode = ((Boolean)prop.get("ebrMode")).booleanValue();
/*  185 */     this.m_dbSyntaxFound = ((Boolean)prop.get("dbSyntaxFound")).booleanValue();
/*  186 */     this.m_ws = ((Workspace)prop.get("ws"));
/*  187 */     this.m_dbType = prop.getProperty("dbType");
/*      */ 
/*  190 */     this.m_dbText = prop.getProperty("dbText");
/*  191 */     this.m_dbMemo = prop.getProperty("dbMemo");
/*  192 */     this.m_dbDate = prop.getProperty("dbDate");
/*  193 */     this.m_dbNumber = prop.getProperty("dbNumber");
/*  194 */     this.m_dbCharacter = prop.getProperty("dbCharacter");
/*  195 */     this.m_dbBoolean = prop.getProperty("dbBoolean");
/*  196 */     this.m_dbClob = prop.getProperty("dbClob");
/*  197 */     this.m_dbBlob = prop.getProperty("dbBlob");
/*  198 */     this.m_dbAllowNull = prop.getProperty("dbAllowNull");
/*  199 */     this.m_dbNotAllowNull = prop.getProperty("dbNotAllowNull");
/*  200 */     this.m_dbEndStatement = prop.getProperty("dbEndStatement");
/*  201 */     this.m_dbMaxMemoSize = prop.getProperty("dbMaxMemoSize");
/*      */   }
/*      */ 
/*      */   public String[] getWorkspaceTableList() throws DataException
/*      */   {
/*  206 */     if (this.m_cachedTableList == null)
/*      */     {
/*  208 */       this.m_cachedTableList = this.m_ws.getTableList();
/*  209 */       this.m_cachedTableListMap.put(this.m_wsName, this.m_cachedTableList);
/*      */     }
/*  211 */     return this.m_cachedTableList;
/*      */   }
/*      */ 
/*      */   public void init(String dbName)
/*      */     throws ServiceException
/*      */   {
/*  223 */     if (dbName.length() != 0)
/*      */     {
/*  225 */       this.m_generateSQLOnly = true;
/*  226 */       dbName = dbName.toLowerCase();
/*  227 */       determineDBSyntax(dbName);
/*  228 */       this.m_dbType = dbName;
/*  229 */       if (this.m_dbType.startsWith("oracleebr"))
/*      */       {
/*  231 */         this.m_ebrMode = true;
/*      */       }
/*  233 */       this.m_ws = null;
/*  234 */       this.m_dsdColumnUpgradeMap = DataDesignInstallUtils.getFieldNameUpgradeMap();
/*      */     }
/*      */     else
/*      */     {
/*  238 */       Report.trace("datastoredesign", "ERROR: init call had no dbName passed in - exiting!", null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void generateAll(boolean preserve)
/*      */     throws ServiceException, DataException
/*      */   {
/*  253 */     if (!this.m_init)
/*      */     {
/*  256 */       Report.trace("datastoredesign", "Uninitialized call to DataDesignGenerator::generateAll() - exiting!", null);
/*  257 */       return;
/*      */     }
/*      */ 
/*  260 */     DataResultSet tableList = getTable("DataStoreDesignTableList");
/*      */ 
/*  262 */     for (int i = 0; i < tableList.getNumRows(); ++i)
/*      */     {
/*  264 */       List fields = tableList.getRowAsList(i);
/*  265 */       String tableName = (String)fields.get(0);
/*  266 */       String compName = (String)fields.get(1);
/*  267 */       String dynamicTable = (String)fields.get(5);
/*  268 */       String workspace = (String)fields.get(6);
/*  269 */       if ((workspace == null) || (workspace.isEmpty()))
/*      */       {
/*  271 */         workspace = "system";
/*      */       }
/*  273 */       loadWorkspaceInfo(workspace);
/*  274 */       generateTable(compName, tableName, preserve, dynamicTable);
/*      */     }
/*      */   }
/*      */ 
/*      */   public DataBinder generateTable(String compName, String tableName, boolean preserve, String dynamicTable)
/*      */     throws ServiceException, DataException
/*      */   {
/*  293 */     DataBinder results = new DataBinder();
/*  294 */     if (!this.m_init)
/*      */     {
/*  297 */       Report.trace("datastoredesign", " Uninitialized call to DataDesignGenerator::generateTable() - exiting!", null);
/*  298 */       return results;
/*      */     }
/*      */ 
/*  302 */     if (SharedObjects.getEnvValueAsBoolean(tableName + ":ignoreGenerate", false))
/*      */     {
/*  304 */       return results;
/*      */     }
/*      */ 
/*  308 */     if (!preserve);
/*  315 */     boolean isDynamicTable = StringUtils.convertToBool(dynamicTable, false);
/*      */ 
/*  317 */     int result = interrogateTable(compName, tableName, isDynamicTable);
/*      */ 
/*  334 */     if ((result == 2) && (this.m_ebrMode) && (!isDynamicTable) && (tableName.equals("DocMetaDefinition")))
/*      */     {
/*  336 */       isDynamicTable = true;
/*  337 */       result = interrogateTable(compName, tableName, isDynamicTable);
/*      */     }
/*      */ 
/*  340 */     switch (result)
/*      */     {
/*      */     case 1:
/*  346 */       results = alterTable(compName, tableName);
/*  347 */       break;
/*      */     case 2:
/*  353 */       if (this.m_dbSyntaxFound)
/*      */       {
/*  357 */         String tableSyntax = generateSQLTableDefinition(compName, tableName, isDynamicTable);
/*      */ 
/*  360 */         String viewSyntax = null;
/*  361 */         if ((this.m_ebrMode) && (!isDynamicTable))
/*      */         {
/*  363 */           viewSyntax = generateSQLViewDefinition(compName, tableName);
/*      */         }
/*      */ 
/*      */         try
/*      */         {
/*  368 */           this.m_ws.executeSQL(tableSyntax);
/*  369 */           results.putLocal("newTable", tableName);
/*  370 */           if (viewSyntax != null)
/*      */           {
/*  372 */             this.m_ws.executeSQL(viewSyntax);
/*      */           }
/*      */ 
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  378 */           Report.error("datastoredesign", e, "Error creating table: " + tableName, new Object[0]);
/*      */         }
/*      */ 
/*  382 */         DataBinder binder = new DataBinder();
/*  383 */         loadTable(compName, tableName, binder);
/*  384 */         DataResultSet newCols = (DataResultSet)binder.getResultSet("fieldDetails");
/*  385 */         DataBinder indexResults = captureResults(compName, tableName, newCols, null, null);
/*  386 */         results.merge(indexResults);
/*      */ 
/*  389 */         this.m_cachedTableList = null;
/*  390 */         this.m_cachedTableListMap.put(this.m_wsName, this.m_cachedTableList);
/*      */       }
/*      */       else
/*      */       {
/*  394 */         Report.error("datastoredesign", null, "DB syntax not found - not creating table: " + tableName, new Object[0]);
/*      */       }
/*      */ 
/*  397 */       break;
/*      */     case 0:
/*      */     case 3:
/*  405 */       DataBinder binder = new DataBinder();
/*  406 */       loadTable(compName, tableName, binder);
/*  407 */       DataResultSet newCols = (DataResultSet)binder.getResultSet("fieldDetails");
/*  408 */       results = captureResults(compName, tableName, newCols, null, null);
/*      */     }
/*      */ 
/*  414 */     DataBinder indexResults = interrogateIndices(compName, tableName);
/*      */ 
/*  416 */     DataResultSet newIndices = (DataResultSet)indexResults.getResultSet("newIndices");
/*  417 */     if (newIndices != null)
/*      */     {
/*  420 */       String[] indexSyntax = generateSQLIndexDefinition(tableName, newIndices, isDynamicTable);
/*      */ 
/*  422 */       for (int j = 0; j < indexSyntax.length; ++j)
/*      */       {
/*  424 */         if (indexSyntax[j].length() == 0)
/*      */           continue;
/*      */         try
/*      */         {
/*  428 */           this.m_ws.executeSQL(indexSyntax[j]);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  433 */           Report.error("datastoredesign", e, "Error creating indicies for table: " + tableName, new Object[0]);
/*      */         }
/*      */       }
/*      */ 
/*  437 */       results.addResultSet("indexedCols", newIndices);
/*      */     }
/*  439 */     return results;
/*      */   }
/*      */ 
/*      */   public String generateSQLViewDefinition(String compName, String tablename)
/*      */     throws ServiceException
/*      */   {
/*  453 */     if ((!this.m_init) && (!this.m_generateSQLOnly))
/*      */     {
/*  455 */       return "Uninitialized call";
/*      */     }
/*      */ 
/*  460 */     if (!this.m_ebrMode)
/*      */     {
/*  462 */       return "";
/*      */     }
/*      */ 
/*  465 */     ExecutionContextAdaptor adaptor = new ExecutionContextAdaptor();
/*  466 */     DataBinder binder = new DataBinder();
/*      */ 
/*  468 */     loadTable(compName, tablename, binder);
/*  469 */     binder.putLocal("tableSuffix", "_");
/*      */ 
/*  477 */     if (this.m_generateSQLOnly)
/*      */     {
/*  479 */       binder.putLocal("endStatementSyntax", this.m_dbEndStatement);
/*      */     }
/*      */     else
/*      */     {
/*  483 */       binder.putLocal("endStatementSyntax", "");
/*      */     }
/*      */ 
/*  486 */     PageMerger pm = new PageMerger();
/*  487 */     pm.initImplement(binder, adaptor);
/*      */ 
/*  489 */     String resInc = "generateEditioningViewSyntax";
/*  490 */     String tablesyntax = "";
/*      */     try
/*      */     {
/*  493 */       tablesyntax = pm.evaluateResourceInclude(resInc);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  497 */       SystemUtils.dumpException("datastoredesign", e);
/*      */     }
/*  499 */     pm.releaseAllTemporary();
/*  500 */     return tablesyntax;
/*      */   }
/*      */ 
/*      */   public String generateSQLTableDefinition(String compName, String tablename, boolean isDynamicTable)
/*      */     throws ServiceException
/*      */   {
/*  513 */     if ((!this.m_init) && (!this.m_generateSQLOnly))
/*      */     {
/*  515 */       return "Uninitialized call";
/*      */     }
/*  517 */     ExecutionContextAdaptor adaptor = new ExecutionContextAdaptor();
/*  518 */     DataBinder binder = new DataBinder();
/*  519 */     if (isDynamicTable)
/*      */     {
/*  521 */       binder.putLocal("dynamicTable", "true");
/*      */     }
/*      */     else
/*      */     {
/*  525 */       binder.putLocal("dynamicTable", "false");
/*      */     }
/*  527 */     loadTable(compName, tablename, binder);
/*      */ 
/*  530 */     loadPK(binder);
/*      */ 
/*  533 */     loadEndOfTable(binder, tablename);
/*      */ 
/*  536 */     boolean useSecureFiles = SharedObjects.getEnvValueAsBoolean("UseSecureFiles", true);
/*  537 */     if (useSecureFiles)
/*      */     {
/*  539 */       String options = "SECUREFILE";
/*  540 */       String secureFileOptions = null;
/*  541 */       if (this.m_ws != null)
/*      */       {
/*  543 */         secureFileOptions = this.m_ws.getProperty("SecureFileOptions");
/*      */       }
/*      */       else
/*      */       {
/*  547 */         DatabaseConfigData config = new DatabaseConfigData();
/*  548 */         DataBinder dbConfigBinder = new DataBinder();
/*  549 */         ProviderConfigUtils.loadSharedTable(dbConfigBinder, "DatabaseConnectionConfigurations");
/*  550 */         config.init(dbConfigBinder);
/*  551 */         config.initConfigurations();
/*  552 */         secureFileOptions = (String)config.getValue("SecureFileOptions");
/*      */       }
/*  554 */       if ((secureFileOptions != null) && (secureFileOptions.length() != 0))
/*      */       {
/*  556 */         options = options + "(" + secureFileOptions + ")";
/*      */       }
/*  558 */       binder.putLocal("lobFileType", options);
/*      */     }
/*      */     else
/*      */     {
/*  562 */       binder.putLocal("lobFileType", "BASICFILE");
/*      */     }
/*      */ 
/*  565 */     if (this.m_generateSQLOnly)
/*      */     {
/*  567 */       binder.putLocal("endStatementSyntax", this.m_dbEndStatement);
/*      */     }
/*      */     else
/*      */     {
/*  571 */       binder.putLocal("endStatementSyntax", "");
/*      */     }
/*      */ 
/*  574 */     PageMerger pm = new PageMerger();
/*  575 */     pm.initImplement(binder, adaptor);
/*      */ 
/*  577 */     String resInc = "generateSqlTableSyntax";
/*  578 */     String tablesyntax = "";
/*      */     try
/*      */     {
/*  581 */       tablesyntax = pm.evaluateResourceInclude(resInc);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  585 */       SystemUtils.dumpException("datastoredesign", e);
/*      */     }
/*  587 */     pm.releaseAllTemporary();
/*  588 */     return tablesyntax;
/*      */   }
/*      */ 
/*      */   public String generateRunTimeConfigTableSQLDefinition(String table)
/*      */     throws ServiceException
/*      */   {
/*  597 */     String compName = "SystemTable";
/*  598 */     String tablename = table;
/*      */ 
/*  600 */     ExecutionContextAdaptor adaptor = new ExecutionContextAdaptor();
/*  601 */     DataBinder binder = new DataBinder();
/*  602 */     binder.putLocal("dynamicTable", "false");
/*      */ 
/*  604 */     loadTable(compName, tablename, binder);
/*      */ 
/*  607 */     loadPK(binder);
/*      */ 
/*  610 */     loadEndOfTable(binder, tablename);
/*      */ 
/*  612 */     binder.putLocal("lobFileType", "BASICFILE");
/*      */ 
/*  614 */     binder.putLocal("endStatementSyntax", "");
/*      */ 
/*  616 */     PageMerger pm = new PageMerger();
/*  617 */     pm.initImplement(binder, adaptor);
/*      */ 
/*  619 */     String resInc = "generateSqlTableSyntax";
/*  620 */     String tablesyntax = "";
/*      */     try
/*      */     {
/*  623 */       tablesyntax = pm.evaluateResourceInclude(resInc);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  627 */       SystemUtils.dumpException("datastoredesign", e);
/*      */     }
/*  629 */     pm.releaseAllTemporary();
/*  630 */     return tablesyntax;
/*      */   }
/*      */ 
/*      */   protected String[] generateSQLIndexDefinition(String tableName, DataResultSet newIndices, boolean isDynamicTable)
/*      */     throws ServiceException, DataException
/*      */   {
/*  653 */     Vector indexSyntaxVector = new Vector();
/*      */ 
/*  656 */     if ((!this.m_generateSQLOnly) || (tableName.compareToIgnoreCase("DocMeta") != 0))
/*      */     {
/*  660 */       for (int i = 0; i < newIndices.getNumRows(); ++i)
/*      */       {
/*  662 */         String indexsyntax = "";
/*  663 */         boolean foundComplexDef = false;
/*  664 */         Vector indexDef = (Vector)newIndices.getRowAsList(i).get(1);
/*  665 */         String indexName = (String)newIndices.getRowAsList(i).get(0);
/*  666 */         String indexTableName = (String)indexDef.get(0);
/*  667 */         String sizeInfo = (String)indexDef.get(4);
/*  668 */         if ((!indexTableName.isEmpty()) && (!sizeInfo.isEmpty()))
/*      */         {
/*  670 */           foundComplexDef = true;
/*      */ 
/*  674 */           DataBinder binder = new DataBinder();
/*  675 */           binder.putLocal("dbName", this.m_dbType);
/*  676 */           binder.putLocal("tableName", tableName);
/*      */ 
/*  678 */           if (isDynamicTable)
/*      */           {
/*  680 */             binder.putLocal("dynamicTable", "true");
/*      */           }
/*      */           else
/*      */           {
/*  684 */             binder.putLocal("dynamicTable", "false");
/*      */           }
/*      */ 
/*  687 */           String fieldName = (String)indexDef.get(2);
/*  688 */           String indexType = (String)indexDef.get(3);
/*  689 */           String deltas = (String)indexDef.get(5);
/*  690 */           String functionString = (String)indexDef.get(6);
/*  691 */           String additionalInfo = (String)indexDef.get(7);
/*  692 */           String indexFunction = getDeltaValue("function", functionString, "");
/*      */ 
/*  694 */           String shortIndexName = DataDesignInstallUtils.generateShortIndexName(this.m_ws, this.m_dbType, indexName + "_" + tableName);
/*  695 */           binder.putLocal("indexName", shortIndexName);
/*  696 */           binder.putLocal("fieldName", fieldName);
/*  697 */           binder.putLocal("indexType", indexType);
/*  698 */           binder.putLocal("additionalInfo", additionalInfo);
/*  699 */           binder.putLocal("indexFunction", indexFunction);
/*      */ 
/*  702 */           if (this.m_generateSQLOnly)
/*      */           {
/*  704 */             binder.putLocal("endStatementSyntax", this.m_dbEndStatement);
/*      */           }
/*      */           else
/*      */           {
/*  708 */             binder.putLocal("endStatementSyntax", "");
/*      */           }
/*      */ 
/*  713 */           loadSize(binder, sizeInfo, deltas, "");
/*      */ 
/*  715 */           PageMerger pm = new PageMerger();
/*  716 */           ExecutionContextAdaptor adaptor = new ExecutionContextAdaptor();
/*  717 */           pm.initImplement(binder, adaptor);
/*      */ 
/*  719 */           String resInc = "generateSqlIndexSyntax";
/*      */           try
/*      */           {
/*  722 */             indexsyntax = pm.evaluateResourceInclude(resInc);
/*      */           }
/*      */           catch (IOException e)
/*      */           {
/*  726 */             SystemUtils.dumpException("generateSQL", e);
/*      */           }
/*      */ 
/*  729 */           indexSyntaxVector.add(indexsyntax);
/*      */         }
/*      */ 
/*  735 */         if (foundComplexDef)
/*      */           continue;
/*  737 */         if (!this.m_generateSQLOnly)
/*      */         {
/*  741 */           String simpleColumns = (String)indexDef.get(11);
/*  742 */           Vector cols = StringUtils.parseArray(simpleColumns, ',', '^');
/*  743 */           String[] colArray = (String[])cols.toArray(new String[cols.size()]);
/*      */ 
/*  747 */           if ((this.m_ebrMode) && (!isDynamicTable))
/*      */           {
/*  749 */             this.m_ws.addIndex(tableName + "_", colArray);
/*      */           }
/*      */           else
/*      */           {
/*  753 */             this.m_ws.addIndex(tableName, colArray);
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  759 */           DataBinder binder = new DataBinder();
/*  760 */           binder.putLocal("dbName", this.m_dbType);
/*  761 */           binder.putLocal("tableName", tableName);
/*  762 */           binder.putLocal("endStatementSyntax", this.m_dbEndStatement);
/*      */ 
/*  764 */           if (isDynamicTable)
/*      */           {
/*  766 */             binder.putLocal("dynamicTable", "true");
/*      */           }
/*      */           else
/*      */           {
/*  770 */             binder.putLocal("dynamicTable", "false");
/*      */           }
/*      */ 
/*  773 */           String shortIndexName = DataDesignInstallUtils.generateShortIndexName(this.m_ws, this.m_dbType, indexName + "_" + tableName);
/*  774 */           binder.putLocal("indexName", shortIndexName);
/*      */ 
/*  776 */           String simpleColumns = (String)indexDef.get(2);
/*  777 */           Vector cols = StringUtils.parseArray(simpleColumns, ',', '^');
/*  778 */           String[] fields = { "name" };
/*  779 */           DataResultSet drset = new DataResultSet(fields);
/*  780 */           for (String colName : cols)
/*      */           {
/*  782 */             Vector v = new Vector();
/*  783 */             v.add(colName);
/*  784 */             drset.addRow(v);
/*      */           }
/*      */ 
/*  787 */           binder.addResultSet("simpleColumns", drset);
/*      */ 
/*  789 */           PageMerger pm = new PageMerger();
/*  790 */           ExecutionContextAdaptor adaptor = new ExecutionContextAdaptor();
/*  791 */           pm.initImplement(binder, adaptor);
/*      */ 
/*  793 */           String resInc = "generateSqlSimpleIndexSyntax";
/*      */           try
/*      */           {
/*  796 */             indexsyntax = pm.evaluateResourceInclude(resInc);
/*      */           }
/*      */           catch (IOException e)
/*      */           {
/*  800 */             SystemUtils.dumpException("generateSQL", e);
/*      */           }
/*      */ 
/*  803 */           indexSyntaxVector.add(indexsyntax);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  809 */     String[] retVal = (String[])indexSyntaxVector.toArray(new String[indexSyntaxVector.size()]);
/*  810 */     return retVal;
/*      */   }
/*      */ 
/*      */   public String generateSQLDropStatement(String tableName)
/*      */     throws ServiceException
/*      */   {
/*  816 */     ExecutionContextAdaptor adaptor = new ExecutionContextAdaptor();
/*  817 */     DataBinder binder = new DataBinder();
/*      */ 
/*  819 */     binder.putLocal("dbName", this.m_dbType);
/*  820 */     binder.putLocal("tableName", tableName);
/*  821 */     binder.putLocal("endStatementSyntax", this.m_dbEndStatement);
/*      */ 
/*  823 */     PageMerger pm = new PageMerger();
/*  824 */     pm.initImplement(binder, adaptor);
/*      */ 
/*  826 */     String resInc = "generateSqlDropSyntax";
/*  827 */     String dropSyntax = "";
/*      */     try
/*      */     {
/*  830 */       dropSyntax = pm.evaluateResourceInclude(resInc);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  834 */       Report.trace("datastoredesign", "Error generating drop statement for Table: " + tableName, e);
/*      */     }
/*      */ 
/*  837 */     return dropSyntax;
/*      */   }
/*      */ 
/*      */   public String generateSQLDefaultStatement(String component, String tableName, boolean isDynamicTable) throws ServiceException
/*      */   {
/*  842 */     IdcStringBuilder defaultSyntax = new IdcStringBuilder();
/*  843 */     DataBinder binder = new DataBinder();
/*  844 */     binder.putLocal("dbName", this.m_dbType);
/*  845 */     binder.putLocal("tableName", tableName);
/*  846 */     binder.putLocal("dynamicTable", (isDynamicTable) ? "true" : "false");
/*  847 */     binder.putLocal("endStatementSyntax", this.m_dbEndStatement);
/*  848 */     binder.putLocal("endBlockSyntax", "");
/*      */ 
/*  851 */     defaultSyntax.append(generateSQLSyntax(binder, "generateSqlDefaultHeaderSyntax"));
/*      */ 
/*  863 */     String[] columns = { "column", "value", "type" };
/*  864 */     DataResultSet defaultValues = getTable(component + "." + tableName + "." + "DefaultValues");
/*  865 */     for (int i = 0; i < defaultValues.getNumRows(); ++i)
/*      */     {
/*  867 */       DataBinder defaultRowBinder = new DataBinder();
/*  868 */       defaultRowBinder.copyLocalDataStateClone(binder);
/*  869 */       DataResultSet defaultRow = new DataResultSet(columns);
/*  870 */       List l = defaultValues.getRowAsList(i);
/*  871 */       String colNames = (String)l.get(0);
/*  872 */       Vector columnsVector = StringUtils.parseArray(colNames, ',', '^');
/*  873 */       String values = (String)l.get(1);
/*  874 */       Vector valuesVector = StringUtils.parseArray(values, ',', '^');
/*  875 */       Vector typesVector = determineTypes(component, tableName, columnsVector);
/*  876 */       for (int j = 0; j < columnsVector.size(); ++j)
/*      */       {
/*  878 */         String colName = (String)columnsVector.get(j);
/*  879 */         String value = (String)valuesVector.get(j);
/*  880 */         String type = (String)typesVector.get(j);
/*  881 */         if (value == null)
/*      */         {
/*  883 */           throw new ServiceException(null, "csUnableToFindValue", new Object[] { colName });
/*      */         }
/*      */ 
/*  886 */         Vector row = new Vector();
/*  887 */         row.add(colName);
/*  888 */         row.add(value);
/*  889 */         row.add(type);
/*  890 */         defaultRow.addRow(row);
/*      */       }
/*  892 */       defaultRowBinder.addResultSet("defaultRow", defaultRow);
/*  893 */       defaultSyntax.append(generateSQLSyntax(defaultRowBinder, "generateSqlDefaultSyntax"));
/*      */     }
/*      */ 
/*  896 */     defaultSyntax.append(generateSQLSyntax(binder, "generateSqlDefaultFooterSyntax"));
/*      */ 
/*  898 */     return defaultSyntax.toString();
/*      */   }
/*      */ 
/*      */   protected String generateSQLSyntax(DataBinder binder, String resource)
/*      */   {
/*  903 */     IdcStringBuilder output = new IdcStringBuilder();
/*      */ 
/*  905 */     PageMerger pm = new PageMerger();
/*  906 */     ExecutionContextAdaptor adaptor = new ExecutionContextAdaptor();
/*  907 */     pm.initImplement(binder, adaptor);
/*      */     try
/*      */     {
/*  911 */       output.append(pm.evaluateResourceInclude(resource));
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  915 */       Report.trace("datastoredesign", "Error generating sql statement from resource " + resource, e);
/*      */     }
/*      */ 
/*  918 */     return output.toString();
/*      */   }
/*      */ 
/*      */   protected Vector determineTypes(String component, String table, Vector cols) throws ServiceException
/*      */   {
/*  923 */     Vector types = new Vector();
/*  924 */     DataResultSet tableDef = getTable(component + "." + table);
/*  925 */     for (int i = 0; i < cols.size(); ++i)
/*      */     {
/*  927 */       String col = (String)cols.get(i);
/*  928 */       String type = getTypeForColumn(col, tableDef);
/*  929 */       types.add(type);
/*      */     }
/*      */ 
/*  932 */     return types;
/*      */   }
/*      */ 
/*      */   protected String getTypeForColumn(String column, DataResultSet tableDef)
/*      */   {
/*  937 */     String type = "unknown type";
/*      */     try
/*      */     {
/*  942 */       tableDef = DataDesignInstallUtils.upgradeFieldNames(tableDef, this.m_dsdColumnUpgradeMap, true);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  946 */       Report.trace("datastoredesign", "table definition upgrade error: ", e);
/*  947 */       return type;
/*      */     }
/*      */ 
/*  950 */     for (int i = 0; i < tableDef.getNumRows(); ++i)
/*      */     {
/*  952 */       List row = tableDef.getRowAsList(i);
/*  953 */       String col = (String)row.get(0);
/*  954 */       if (col.compareToIgnoreCase(column) != 0) {
/*      */         continue;
/*      */       }
/*  957 */       String[] keys = { "dsdColumnName", "dsdFieldType", "dsdFieldLength", "dsdIsPrimary", "dsdIsRequired", "dsdDefaultValue" };
/*  958 */       FieldInfo[] info = null;
/*      */       try
/*      */       {
/*  961 */         info = ResultSetUtils.createInfoList(tableDef, keys, false);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  966 */         Report.trace("datastoredesign", "failed to determine what fields are present in the table def: ", e);
/*  967 */         break label204:
/*      */       }
/*      */ 
/*  970 */       int index = tableDef.getFieldInfoIndex("dsdColumnName");
/*  971 */       if (index >= 0)
/*      */       {
/*  973 */         List field = tableDef.findRow(index, column, 0, 2);
/*  974 */         List l = finalizeField(field, info);
/*  975 */         type = convertToDSDType((String)l.get(1));
/*      */       }
/*      */       else
/*      */       {
/*  979 */         Report.trace("datastoredesign", "failed to find dsdColumnName field in the table def", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  984 */     label204: return type;
/*      */   }
/*      */ 
/*      */   protected DataResultSet getTable(String tablename)
/*      */     throws ServiceException
/*      */   {
/*  995 */     DataResultSet tableDefinition = SharedObjects.getTable(tablename);
/*      */ 
/*  997 */     if (tableDefinition == null)
/*      */     {
/*  999 */       Report.trace("datastoredesign", "Definition not found for Table: " + tablename, null);
/*      */     }
/* 1001 */     return tableDefinition;
/*      */   }
/*      */ 
/*      */   protected void loadTable(String compName, String tableName, DataBinder binder) throws ServiceException
/*      */   {
/* 1014 */     String[] fieldColumns = { "fieldname", "type", "size", "default", "allowNull", "origtype", "isPK", "generated" };
/* 1015 */     DataResultSet fieldDetails = new DataResultSet(fieldColumns);
/* 1016 */     String[] lobFieldName = { "fieldname", "filename" };
/* 1017 */     DataResultSet lobFields = new DataResultSet(lobFieldName);
/*      */ 
/* 1019 */     binder.putLocal("dbName", this.m_dbType);
/* 1020 */     binder.putLocal("tableName", tableName);
/* 1021 */     binder.putLocal("dbspecific_allowNull", this.m_dbAllowNull);
/* 1022 */     binder.putLocal("dbspecific_notAllowNull", this.m_dbNotAllowNull);
/* 1023 */     binder.putLocal("endStatementSyntax", this.m_dbEndStatement);
/*      */ 
/* 1025 */     DataResultSet tableFormat = getTable(compName + "." + tableName);
/* 1026 */     String[] keys = { "dsdColumnName", "dsdFieldType", "dsdFieldLength", "dsdIsPrimary", "dsdIsRequired", "dsdDefaultValue", "dsdGenerated" };
/*      */ 
/* 1028 */     tableFormat = DataDesignInstallUtils.upgradeFieldNames(tableFormat, this.m_dsdColumnUpgradeMap, true);
/*      */     FieldInfo[] info;
/*      */     try
/*      */     {
/* 1031 */       info = ResultSetUtils.createInfoList(tableFormat, keys, false);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1036 */       return;
/*      */     }
/*      */ 
/* 1039 */     if (tableFormat != null)
/*      */     {
/* 1041 */       for (int i = 0; i < tableFormat.getNumRows(); ++i)
/*      */       {
/* 1043 */         List field = tableFormat.getRowAsList(i);
/* 1044 */         loadField(fieldDetails, field, info, tableName);
/* 1045 */         String fieldType = (String)field.get(1);
/* 1046 */         if (fieldType.toLowerCase().indexOf("lob") <= 0)
/*      */           continue;
/* 1048 */         String fieldname = (String)field.get(0);
/* 1049 */         Vector v = new Vector();
/* 1050 */         v.add(fieldname);
/* 1051 */         String filename = DataDesignInstallUtils.generateShortIndexName(this.m_ws, this.m_dbType, tableName + "_" + fieldname);
/* 1052 */         v.add(filename);
/* 1053 */         lobFields.addRow(v);
/*      */       }
/*      */ 
/* 1057 */       binder.addResultSet("fieldDetails", fieldDetails);
/* 1058 */       binder.addResultSet("lobFields", lobFields);
/*      */     }
/*      */     else
/*      */     {
/* 1062 */       Report.trace("datastoredesign", "Table: " + tableName + " defined in DataStoreDesignTableList but definition does not exist!", null);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String convertToDBSize(String type, String size)
/*      */   {
/* 1074 */     if (type.compareToIgnoreCase("memo") == 0)
/*      */     {
/* 1077 */       if ((size.compareToIgnoreCase("max") == 0) || (size.length() == 0))
/*      */       {
/* 1079 */         size = this.m_dbMaxMemoSize;
/*      */       }
/*      */     }
/* 1082 */     else if (type.compareToIgnoreCase("text") != 0)
/*      */     {
/* 1086 */       size = "";
/*      */     }
/*      */ 
/* 1089 */     if ((size.compareToIgnoreCase("memoSize") == 0) || (size.compareToIgnoreCase("m_memoSize") == 0))
/*      */     {
/* 1093 */       size = this.m_dbMaxMemoSize;
/*      */     }
/* 1095 */     return size;
/*      */   }
/*      */ 
/*      */   protected void loadField(DataResultSet fieldDetails, List field, FieldInfo[] info, String tableName)
/*      */     throws ServiceException
/*      */   {
/* 1110 */     if (field == null)
/*      */     {
/* 1113 */       Report.trace("datastoredesign", "NULL field - aborting loading!", null);
/* 1114 */       return;
/*      */     }
/*      */ 
/* 1117 */     List l = finalizeField(field, info);
/* 1118 */     String fieldname = (String)l.get(info[0].m_index);
/* 1119 */     String origtype = convertToDSDType((String)l.get(info[1].m_index));
/* 1120 */     String type = convertToDBType(origtype);
/* 1121 */     String size = convertToDBSize(origtype, (String)l.get(info[2].m_index));
/* 1122 */     String isPK = (String)l.get(info[3].m_index);
/*      */ 
/* 1124 */     String generated = "";
/* 1125 */     if (info[6].m_index > 0)
/*      */     {
/* 1127 */       generated = (String)l.get(info[6].m_index);
/*      */     }
/* 1129 */     String allowNull = "true";
/* 1130 */     if ((StringUtils.convertToBool(isPK, false)) || ((info[4].m_index >= 0) && (StringUtils.convertToBool((String)l.get(info[4].m_index), false))))
/*      */     {
/* 1134 */       allowNull = "false";
/*      */     }
/*      */     String defaultValue;
/*      */     String defaultValue;
/* 1136 */     if (info[5].m_index >= 0)
/*      */     {
/* 1138 */       defaultValue = (String)l.get(info[5].m_index);
/*      */     }
/*      */     else
/*      */     {
/* 1142 */       defaultValue = "";
/*      */     }
/*      */ 
/* 1145 */     List row = new ArrayList();
/* 1146 */     row.add(fieldname);
/* 1147 */     row.add(type);
/* 1148 */     row.add(size);
/* 1149 */     row.add(defaultValue);
/* 1150 */     row.add(allowNull);
/* 1151 */     row.add(origtype);
/* 1152 */     row.add(isPK);
/* 1153 */     row.add("");
/*      */ 
/* 1155 */     fieldDetails.addRowWithList(row);
/*      */ 
/* 1163 */     if (generated.isEmpty())
/*      */       return;
/* 1165 */     String colFunction = getDeltaValue("function", generated, "");
/* 1166 */     if (colFunction.isEmpty())
/*      */       return;
/* 1168 */     String newFieldName = fieldname + "_" + colFunction;
/*      */ 
/* 1171 */     List generatedrow = new ArrayList();
/* 1172 */     generatedrow.add(newFieldName);
/* 1173 */     generatedrow.add("");
/* 1174 */     generatedrow.add("");
/* 1175 */     generatedrow.add("");
/* 1176 */     generatedrow.add("");
/* 1177 */     generatedrow.add("");
/* 1178 */     generatedrow.add("");
/* 1179 */     generatedrow.add(colFunction + "(" + fieldname + ")");
/*      */ 
/* 1181 */     fieldDetails.addRowWithList(generatedrow);
/*      */   }
/*      */ 
/*      */   protected List finalizeField(List<String> field, FieldInfo[] infos)
/*      */   {
/* 1188 */     if (infos == null)
/*      */     {
/* 1191 */       return field;
/*      */     }
/*      */ 
/* 1194 */     Map map = (Map)SharedObjects.getObject("SystemVariables", "SystemVariables");
/*      */ 
/* 1196 */     List finalizedField = new ArrayList(field);
/* 1197 */     String svName = null;
/* 1198 */     for (int i = 0; i < infos.length; ++i)
/*      */     {
/* 1200 */       String value = "";
/* 1201 */       if (infos[i].m_index >= 0)
/*      */       {
/* 1203 */         value = (String)field.get(infos[i].m_index);
/*      */       }
/*      */ 
/* 1206 */       boolean isChanged = false;
/*      */ 
/* 1208 */       if (value.equalsIgnoreCase("idcemptyvalue"))
/*      */       {
/* 1210 */         value = "";
/* 1211 */         isChanged = true;
/*      */       }
/* 1213 */       if (value.length() == 0)
/*      */       {
/* 1215 */         if (svName != null);
/*      */       }
/*      */       else
/*      */       {
/* 1221 */         if ((map != null) && (map.get(value) != null))
/*      */         {
/* 1223 */           svName = value;
/*      */         }
/*      */         else
/*      */         {
/* 1228 */           isChanged = true;
/*      */         }
/*      */ 
/* 1231 */         if ((svName != null) && (!isChanged))
/*      */         {
/* 1233 */           FieldInfo fi = (FieldInfo)map.get(svName);
/* 1234 */           value = FieldInfoUtils.getFieldOption(fi, infos[i].m_name);
/*      */         }
/*      */ 
/* 1237 */         if (value == null)
/*      */           continue;
/* 1239 */         finalizedField.set(infos[i].m_index, value);
/*      */       }
/*      */     }
/*      */ 
/* 1243 */     return finalizedField;
/*      */   }
/*      */ 
/*      */   protected String convertToDBType(String type)
/*      */   {
/* 1255 */     String convertedType = null;
/*      */ 
/* 1257 */     if ((type.compareToIgnoreCase("text") == 0) || (type.compareToIgnoreCase("varchar") == 0) || (type.compareToIgnoreCase("m_varchar") == 0))
/*      */     {
/* 1261 */       convertedType = this.m_dbText;
/*      */     }
/* 1263 */     else if ((type.compareToIgnoreCase("memo") == 0) || (type.compareToIgnoreCase("m_memo") == 0))
/*      */     {
/* 1266 */       convertedType = this.m_dbMemo;
/*      */     }
/* 1268 */     else if ((type.compareToIgnoreCase("date") == 0) || (type.compareToIgnoreCase("m_date") == 0))
/*      */     {
/* 1271 */       convertedType = this.m_dbDate;
/*      */     }
/* 1273 */     else if ((type.compareToIgnoreCase("number") == 0) || (type.compareToIgnoreCase("integer") == 0) || (type.compareToIgnoreCase("m_integer") == 0))
/*      */     {
/* 1277 */       convertedType = this.m_dbNumber;
/*      */     }
/* 1279 */     else if ((type.compareToIgnoreCase("boolean") == 0) || (type.compareToIgnoreCase("truefalse") == 0) || (type.compareToIgnoreCase("m_truefalse") == 0))
/*      */     {
/* 1283 */       convertedType = this.m_dbBoolean;
/*      */     }
/* 1285 */     else if (type.compareToIgnoreCase("clob") == 0)
/*      */     {
/* 1287 */       convertedType = this.m_dbClob;
/*      */     }
/* 1289 */     else if (type.compareToIgnoreCase("blob") == 0)
/*      */     {
/* 1291 */       convertedType = this.m_dbBlob;
/*      */     }
/*      */ 
/* 1294 */     if (convertedType == null)
/*      */     {
/* 1297 */       Report.trace("datastoredesign", "Unable to determine Type: " + type + " conversion. - treating this type as an integer", null);
/* 1298 */       convertedType = this.m_dbNumber;
/*      */     }
/*      */ 
/* 1301 */     return convertedType;
/*      */   }
/*      */ 
/*      */   protected String convertToDSDType(String type)
/*      */   {
/* 1314 */     String convertedType = type;
/*      */ 
/* 1316 */     if ((type.compareToIgnoreCase("varchar") == 0) || (type.compareToIgnoreCase("m_varchar") == 0))
/*      */     {
/* 1319 */       convertedType = "text";
/*      */     }
/* 1321 */     else if (type.compareToIgnoreCase("m_memo") == 0)
/*      */     {
/* 1323 */       convertedType = "memo";
/*      */     }
/* 1325 */     else if (type.compareToIgnoreCase("m_date") == 0)
/*      */     {
/* 1327 */       convertedType = "date";
/*      */     }
/* 1329 */     else if ((type.compareToIgnoreCase("integer") == 0) || (type.compareToIgnoreCase("m_integer") == 0))
/*      */     {
/* 1332 */       convertedType = "number";
/*      */     }
/* 1334 */     else if ((type.compareToIgnoreCase("truefalse") == 0) || (type.compareToIgnoreCase("m_truefalse") == 0))
/*      */     {
/* 1337 */       convertedType = "boolean";
/*      */     }
/*      */ 
/* 1340 */     return convertedType;
/*      */   }
/*      */ 
/*      */   protected boolean determineDBSyntax(String database)
/*      */     throws ServiceException
/*      */   {
/* 1350 */     DataResultSet dbConv = getTable("dsdConversionDefinitions");
/* 1351 */     String searchdb = database;
/* 1352 */     boolean dbFound = false;
/*      */ 
/* 1354 */     while (!dbFound)
/*      */     {
/* 1357 */       for (int i = 0; i < dbConv.getNumRows(); ++i)
/*      */       {
/* 1359 */         List conversionFields = dbConv.getRowAsList(i);
/*      */ 
/* 1361 */         if (!conversionFields.contains(searchdb))
/*      */           continue;
/* 1363 */         this.m_dbText = ((String)conversionFields.get(1));
/* 1364 */         this.m_dbMemo = ((String)conversionFields.get(2));
/* 1365 */         this.m_dbDate = ((String)conversionFields.get(3));
/* 1366 */         this.m_dbNumber = ((String)conversionFields.get(4));
/* 1367 */         this.m_dbBoolean = ((String)conversionFields.get(5));
/* 1368 */         this.m_dbAllowNull = ((String)conversionFields.get(6));
/* 1369 */         this.m_dbNotAllowNull = ((String)conversionFields.get(7));
/* 1370 */         this.m_dbEndStatement = ((String)conversionFields.get(8));
/* 1371 */         this.m_dbBlob = ((String)conversionFields.get(9));
/* 1372 */         this.m_dbClob = ((String)conversionFields.get(10));
/* 1373 */         this.m_dbMaxMemoSize = ((String)conversionFields.get(11));
/* 1374 */         dbFound = true;
/* 1375 */         break;
/*      */       }
/*      */ 
/* 1379 */       if (i == dbConv.getNumRows())
/*      */       {
/* 1381 */         if (searchdb.compareTo(database) != 0)
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/* 1394 */         if (!database.contains("_unicode")) {
/*      */           break;
/*      */         }
/*      */ 
/* 1398 */         searchdb = database.substring(0, database.indexOf(95));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1408 */     if (!dbFound)
/*      */     {
/* 1413 */       Report.trace(null, null, "Database: " + searchdb + " not found in datastoredesign resources - disabling dsd table creation", new Object[0]);
/* 1414 */       this.m_dbSyntaxFound = false;
/* 1415 */       this.m_dbText = (database + " type not found");
/* 1416 */       this.m_dbMemo = (database + " type not found");
/* 1417 */       this.m_dbDate = (database + " type not found");
/* 1418 */       this.m_dbNumber = (database + " type not found");
/* 1419 */       this.m_dbBoolean = (database + " type not found");
/* 1420 */       this.m_dbAllowNull = (database + " type not found");
/* 1421 */       this.m_dbNotAllowNull = (database + " type not found");
/* 1422 */       this.m_dbEndStatement = (database + " type not found");
/* 1423 */       this.m_dbBlob = (database + " type not found");
/* 1424 */       this.m_dbClob = (database + " type not found");
/*      */     }
/*      */     else
/*      */     {
/* 1428 */       this.m_dbSyntaxFound = true;
/*      */     }
/* 1430 */     return dbFound;
/*      */   }
/*      */ 
/*      */   protected boolean determineDBSyntax(Properties prop, String database)
/*      */     throws ServiceException
/*      */   {
/* 1441 */     DataResultSet dbConv = getTable("dsdConversionDefinitions");
/* 1442 */     String searchdb = database;
/* 1443 */     boolean dbFound = false;
/*      */ 
/* 1445 */     while (!dbFound)
/*      */     {
/* 1448 */       for (int i = 0; i < dbConv.getNumRows(); ++i)
/*      */       {
/* 1450 */         List conversionFields = dbConv.getRowAsList(i);
/*      */ 
/* 1452 */         if (!conversionFields.contains(searchdb))
/*      */           continue;
/* 1454 */         prop.put("dbText", conversionFields.get(1));
/* 1455 */         prop.put("dbMemo", conversionFields.get(2));
/* 1456 */         prop.put("dbDate", conversionFields.get(3));
/* 1457 */         prop.put("dbNumber", conversionFields.get(4));
/* 1458 */         prop.put("dbBoolean", conversionFields.get(5));
/* 1459 */         prop.put("dbAllowNull", conversionFields.get(6));
/* 1460 */         prop.put("dbNotAllowNull", conversionFields.get(7));
/* 1461 */         prop.put("dbEndStatement", conversionFields.get(8));
/* 1462 */         prop.put("dbBlob", conversionFields.get(9));
/* 1463 */         prop.put("dbClob", conversionFields.get(10));
/* 1464 */         prop.put("dbMaxMemoSize", conversionFields.get(11));
/* 1465 */         dbFound = true;
/* 1466 */         break;
/*      */       }
/*      */ 
/* 1470 */       if (i == dbConv.getNumRows())
/*      */       {
/* 1472 */         if (searchdb.compareTo(database) != 0)
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/* 1485 */         if (!database.contains("_unicode")) {
/*      */           break;
/*      */         }
/*      */ 
/* 1489 */         searchdb = database.substring(0, database.indexOf(95));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1499 */     if (!dbFound)
/*      */     {
/* 1504 */       Report.trace(null, null, "Database: " + searchdb + " not found in datastoredesign resources - disabling dsd table creation", new Object[0]);
/*      */ 
/* 1506 */       prop.put("dbSyntaxFound", Boolean.valueOf(false));
/* 1507 */       prop.put("dbText", database + " type not found");
/* 1508 */       prop.put("dbMemo", database + " type not found");
/* 1509 */       prop.put("dbDate", database + " type not found");
/* 1510 */       prop.put("dbNumber", database + " type not found");
/* 1511 */       prop.put("dbBoolean", database + " type not found");
/* 1512 */       prop.put("dbAllowNull", database + " type not found");
/* 1513 */       prop.put("dbNotAllowNull", database + " type not found");
/* 1514 */       prop.put("dbEndStatement", database + " type not found");
/* 1515 */       prop.put("dbBlob", database + " type not found");
/* 1516 */       prop.put("dbClob", database + " type not found");
/* 1517 */       prop.put("dbMaxMemoSize", database + " type not found");
/*      */     }
/*      */     else
/*      */     {
/* 1521 */       prop.put("dbSyntaxFound", Boolean.valueOf(true));
/*      */     }
/* 1523 */     return dbFound;
/*      */   }
/*      */ 
/*      */   protected void loadPK(DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 1535 */     DataResultSet tableInfo = getTable("dsdTableInfo");
/*      */     String tablename;
/*      */     try
/*      */     {
/* 1539 */       tablename = binder.get("tableName");
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1544 */       return;
/*      */     }
/* 1546 */     List tableList = tableInfo.findRow(0, tablename);
/*      */ 
/* 1548 */     if (tableList == null)
/*      */     {
/* 1551 */       tableList = tableInfo.findRow(0, "dsdDefaultTableSizes");
/*      */     }
/*      */ 
/* 1554 */     String[] pkColumns = { "field" };
/* 1555 */     DataResultSet pkList = new DataResultSet(pkColumns);
/* 1556 */     DataResultSet fieldDetails = (DataResultSet)binder.getResultSet("fieldDetails");
/*      */ 
/* 1560 */     for (int i = 0; i < fieldDetails.getNumRows(); ++i)
/*      */     {
/* 1562 */       boolean isPK = StringUtils.convertToBool((String)fieldDetails.getRowValues(i).get(6), false);
/* 1563 */       if (!isPK)
/*      */         continue;
/* 1565 */       ArrayList row = new ArrayList();
/* 1566 */       String pkName = (String)fieldDetails.getRowValues(i).get(0);
/* 1567 */       row.add(pkName);
/* 1568 */       pkList.addRowWithList(row);
/*      */     }
/*      */ 
/* 1572 */     String constraintName = "PK_" + tablename;
/* 1573 */     constraintName = DataDesignInstallUtils.generateShortIndexName(this.m_ws, this.m_dbType, constraintName);
/* 1574 */     binder.putLocal("pkConstraintName", constraintName);
/*      */ 
/* 1576 */     binder.putLocal("pkClusterType", (String)tableList.get(1));
/* 1577 */     binder.addResultSet("pkList", pkList);
/*      */ 
/* 1579 */     loadSize(binder, (String)tableList.get(2), (String)tableList.get(3), "pk_");
/*      */   }
/*      */ 
/*      */   protected void loadSize(DataBinder binder, String size, String deltas, String prefix)
/*      */     throws ServiceException
/*      */   {
/* 1591 */     if (size.compareTo("") == 0) {
/* 1592 */       return;
/*      */     }
/*      */ 
/* 1595 */     List sizeInfo = getSizeInfo(size);
/* 1596 */     String initial = getDeltaValue("initial", deltas, (String)sizeInfo.get(1));
/* 1597 */     String next = getDeltaValue("next", deltas, (String)sizeInfo.get(2));
/* 1598 */     String pctfree = getDeltaValue("pctfree", deltas, (String)sizeInfo.get(3));
/* 1599 */     String pctused = getDeltaValue("pctused", deltas, (String)sizeInfo.get(4));
/* 1600 */     String min = getDeltaValue("min", deltas, (String)sizeInfo.get(5));
/* 1601 */     String max = getDeltaValue("max", deltas, (String)sizeInfo.get(6));
/* 1602 */     String inc = getDeltaValue("inc", deltas, (String)sizeInfo.get(7));
/*      */ 
/* 1607 */     binder.putLocal(prefix + "sizeExists", "true");
/* 1608 */     binder.putLocal(prefix + "initial", initial);
/* 1609 */     binder.putLocal(prefix + "next", next);
/* 1610 */     binder.putLocal(prefix + "pctfree", pctfree);
/* 1611 */     binder.putLocal(prefix + "pctused", pctused);
/* 1612 */     binder.putLocal(prefix + "min", min);
/* 1613 */     binder.putLocal(prefix + "max", max);
/* 1614 */     binder.putLocal(prefix + "inc", inc);
/*      */   }
/*      */ 
/*      */   protected void loadEndOfTable(DataBinder binder, String tablename)
/*      */     throws ServiceException
/*      */   {
/* 1627 */     DataResultSet tableInfo = getTable("dsdTableInfo");
/* 1628 */     List tableList = tableInfo.findRow(0, tablename);
/*      */ 
/* 1630 */     if (tableList == null)
/*      */     {
/* 1633 */       tableList = tableInfo.findRow(0, "dsdDefaultTableSizes");
/*      */     }
/*      */ 
/* 1637 */     loadSize(binder, (String)tableList.get(4), (String)tableList.get(5), "table_");
/*      */   }
/*      */ 
/*      */   protected String getDeltaValue(String name, String deltas, String standard)
/*      */   {
/* 1651 */     String deltaValue = standard;
/*      */ 
/* 1660 */     List parsedDeltas = StringUtils.parseArray(deltas, ',', '^');
/* 1661 */     for (int i = 0; i < parsedDeltas.size(); ++i)
/*      */     {
/* 1663 */       String curDelta = (String)parsedDeltas.get(i);
/* 1664 */       if (!curDelta.contains(name))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1670 */       if (curDelta.contains("." + name))
/*      */       {
/* 1674 */         if (!curDelta.contains(this.m_dbType + "." + name)) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1678 */         List parsedValue = StringUtils.parseArray(curDelta, '=', '^');
/* 1679 */         deltaValue = (String)parsedValue.get(1);
/*      */       }
/*      */       else
/*      */       {
/* 1685 */         List parsedValue = StringUtils.parseArray(curDelta, '=', '^');
/* 1686 */         deltaValue = (String)parsedValue.get(1);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1691 */     return deltaValue;
/*      */   }
/*      */ 
/*      */   protected List getSizeInfo(String size)
/*      */     throws ServiceException
/*      */   {
/* 1703 */     DataResultSet pkSizes = getTable("dsdSizes");
/*      */ 
/* 1705 */     List sizeList = pkSizes.findRow(0, size);
/*      */ 
/* 1707 */     if (sizeList == null)
/*      */     {
/* 1710 */       Report.trace("datastoredesign", "Invalid Size: " + size + " not defined in dsdSizes! defaulting to size A", null);
/* 1711 */       sizeList = pkSizes.findRow(0, "A");
/*      */     }
/*      */ 
/* 1714 */     return sizeList;
/*      */   }
/*      */ 
/*      */   protected int interrogateTable(String compName, String tableName, boolean isDynamicTable)
/*      */     throws ServiceException
/*      */   {
/* 1729 */     int result = 0;
/*      */ 
/* 1731 */     if (tableName.compareTo("DocMeta") == 0)
/*      */     {
/* 1734 */       return result;
/*      */     }
/*      */ 
/* 1737 */     if (!this.m_init)
/*      */     {
/* 1739 */       result = 3;
/*      */     }
/*      */     else
/*      */     {
/* 1743 */       DataBinder binder = new DataBinder();
/* 1744 */       loadTable(compName, tableName, binder);
/*      */ 
/* 1748 */       if ((this.m_ebrMode) && (!isDynamicTable))
/*      */       {
/* 1750 */         tableName = tableName + "_";
/*      */       }
/*      */ 
/* 1753 */       boolean tableFound = false;
/*      */       try
/*      */       {
/* 1756 */         String[] tableList = getWorkspaceTableList();
/* 1757 */         tableFound = StringUtils.findStringIndexEx(tableList, tableName, true) >= 0;
/* 1758 */         if (!tableFound)
/*      */         {
/* 1764 */           if ((!this.m_ebrMode) || (isDynamicTable) || (!tableName.equals("DocMetaDefinition")))
/*      */           {
/* 1766 */             Report.trace("datastoredesign", null, "Table: " + tableName + " did not exist - creating..", new Object[0]);
/*      */           }
/* 1768 */           result = 2;
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (DataException e1)
/*      */       {
/* 1774 */         result = 3;
/*      */       }
/*      */ 
/* 1778 */       if (tableFound)
/*      */       {
/*      */         try
/*      */         {
/* 1782 */           FieldInfo[] fi = WorkspaceUtils.getActualColumnList(this.m_ws, tableName);
/* 1783 */           result = compareColumnsForTable(fi, binder);
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/* 1788 */           Report.trace("datastoredesign", e, "Error obtaining column list for Table: " + tableName, new Object[0]);
/* 1789 */           result = 3;
/*      */         }
/*      */       }
/*      */     }
/* 1793 */     return result;
/*      */   }
/*      */ 
/*      */   protected DataBinder interrogateIndices(String compName, String tableName)
/*      */     throws ServiceException
/*      */   {
/* 1806 */     String[] columns = { "indexName", "indexColumns" };
/* 1807 */     DataResultSet newIndices = new DataResultSet(columns);
/* 1808 */     DataBinder results = new DataBinder();
/*      */ 
/* 1816 */     DataResultSet indexTable = getTable("dsdIndexTable");
/* 1817 */     DataResultSet indexSimpleTable = getTable("DataStoreDesignColumnsIndexed");
/* 1818 */     indexSimpleTable = DataDesignInstallUtils.upgradeFieldNames(indexSimpleTable, this.m_dsdColumnUpgradeMap, true);
/* 1819 */     DataResultSet indexSimpleTable2 = getTable("ColumnsIndexed");
/* 1820 */     indexSimpleTable2 = DataDesignInstallUtils.upgradeFieldNames(indexSimpleTable2, this.m_dsdColumnUpgradeMap, true);
/*      */     try
/*      */     {
/* 1823 */       indexSimpleTable.merge(null, indexSimpleTable2, false);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1828 */       Report.trace("datastoredesign", e, "Error obtaining column list for Table: " + tableName, new Object[0]);
/* 1829 */       indexSimpleTable.reset();
/*      */     }
/* 1831 */     DataResultSet simpleTableIndices = new DataResultSet();
/* 1832 */     DataResultSet complexTableIndices = new DataResultSet();
/*      */ 
/* 1835 */     simpleTableIndices.copySimpleFiltered(indexSimpleTable, "dsdTableName", tableName);
/*      */ 
/* 1837 */     complexTableIndices.copySimpleFiltered(indexTable, "table", tableName);
/* 1838 */     DataResultSet mergedIndexes = new DataResultSet();
/*      */ 
/* 1840 */     mergedIndexes.copySimpleFiltered(indexTable, "table", tableName);
/* 1841 */     mergedIndexes.mergeFields(simpleTableIndices);
/*      */     try
/*      */     {
/* 1844 */       mergedIndexes.merge(null, simpleTableIndices, false);
/*      */     }
/*      */     catch (DataException e1)
/*      */     {
/* 1849 */       e1.printStackTrace();
/*      */     }
/*      */ 
/* 1854 */     Vector indexList = new Vector();
/* 1855 */     Map nameMap = new HashMap();
/* 1856 */     for (int i = 0; i < mergedIndexes.getNumRows(); ++i)
/*      */     {
/* 1858 */       String complexIndexName = (String)mergedIndexes.getRowAsList(i).get(1);
/* 1859 */       String simpleIndexName = (String)mergedIndexes.getRowAsList(i).get(11);
/* 1860 */       String name = (complexIndexName.isEmpty()) ? simpleIndexName : complexIndexName;
/*      */ 
/* 1862 */       if (indexList.contains(name))
/*      */       {
/* 1865 */         Report.trace("datastoredesign", "Duplicate index definitions for: " + name, null);
/* 1866 */         Report.trace("datastoredesign", "used definition " + mergedIndexes.getRowValues(i), null);
/* 1867 */         Report.trace("datastoredesign", "discarded definition " + nameMap.get(name), null);
/*      */       }
/*      */       else
/*      */       {
/* 1871 */         indexList.add(name);
/* 1872 */         nameMap.put(name, mergedIndexes.getRowValues(i));
/*      */       }
/*      */     }
/*      */ 
/* 1876 */     if (indexList.size() == 0)
/*      */     {
/* 1878 */       return results;
/*      */     }
/*      */ 
/*      */     DatabaseIndexInfo[] dbii;
/*      */     try
/*      */     {
/* 1884 */       dbii = this.m_ws.getIndexList(tableName);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1888 */       Report.trace("datastoredesign", "Error getting index list for table " + tableName, e);
/* 1889 */       return results;
/*      */     }
/*      */ 
/* 1892 */     for (String indexName : indexList)
/*      */     {
/* 1894 */       boolean foundIndex = false;
/*      */ 
/* 1896 */       for (int i = 0; i < dbii.length; ++i)
/*      */       {
/* 1898 */         if (!indexMatch(dbii[i], (List)nameMap.get(indexName)))
/*      */           continue;
/* 1900 */         foundIndex = true;
/* 1901 */         break;
/*      */       }
/*      */ 
/* 1904 */       if (!foundIndex)
/*      */       {
/* 1906 */         Report.trace("datastoredesign", "Index: " + indexName + " did not exist - creating..", null);
/*      */ 
/* 1908 */         Vector v = new Vector();
/* 1909 */         v.add(indexName);
/* 1910 */         v.add(nameMap.get(indexName));
/* 1911 */         newIndices.addRow(v);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1916 */     if (newIndices.getNumRows() != 0)
/*      */     {
/* 1918 */       results.addResultSet("newIndices", newIndices);
/*      */     }
/*      */ 
/* 1921 */     return results;
/*      */   }
/*      */ 
/*      */   protected boolean indexMatch(DatabaseIndexInfo dbii, List indexDef)
/*      */   {
/* 1926 */     boolean match = false;
/* 1927 */     String functionalDef = (String)indexDef.get(6);
/* 1928 */     String complexColumnDef = (String)indexDef.get(2);
/* 1929 */     String simpleColumnDef = (String)indexDef.get(11);
/* 1930 */     String definedColumns = (complexColumnDef.isEmpty()) ? simpleColumnDef : complexColumnDef;
/* 1931 */     boolean isDefinitionFunctional = isFunctionalIndex(functionalDef);
/*      */ 
/* 1933 */     if (dbii.m_isFunctional)
/*      */     {
/* 1936 */       if (isDefinitionFunctional)
/*      */       {
/* 1940 */         String actualFunction = dbii.m_function;
/* 1941 */         String definedFunction = generateDefinedFunction(functionalDef);
/* 1942 */         if (functionalIndexMatch(actualFunction, definedFunction))
/*      */         {
/* 1945 */           match = true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/* 1952 */     else if (!isDefinitionFunctional)
/*      */     {
/* 1955 */       ArrayList actualColumns = dbii.m_columns;
/* 1956 */       if (simpleIndexMatch(actualColumns, definedColumns))
/*      */       {
/* 1959 */         match = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1964 */     return match;
/*      */   }
/*      */ 
/*      */   protected boolean isFunctionalIndex(String functionString)
/*      */   {
/* 1982 */     boolean isFunctional = false;
/*      */ 
/* 1984 */     if (!functionString.isEmpty())
/*      */     {
/* 1989 */       String indexFunction = getDeltaValue("function", functionString, "");
/* 1990 */       if (!indexFunction.isEmpty())
/*      */       {
/* 1992 */         isFunctional = true;
/*      */       }
/*      */     }
/* 1995 */     return isFunctional;
/*      */   }
/*      */ 
/*      */   protected boolean simpleIndexMatch(ArrayList colList, String indexName)
/*      */   {
/* 2006 */     boolean match = false;
/*      */ 
/* 2008 */     Vector v = StringUtils.parseArray(indexName, ',', '^');
/* 2009 */     if (colList.size() == v.size())
/*      */     {
/* 2011 */       for (int i = 0; i < colList.size(); ++i)
/*      */       {
/* 2013 */         match = false;
/* 2014 */         String existingIndexName = (String)colList.get(i);
/* 2015 */         for (int j = 0; j < v.size(); ++j)
/*      */         {
/* 2017 */           if (existingIndexName.compareToIgnoreCase((String)v.get(j)) != 0)
/*      */             continue;
/* 2019 */           match = true;
/* 2020 */           break;
/*      */         }
/*      */ 
/* 2023 */         if (!match) {
/*      */           break;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2030 */     return match;
/*      */   }
/*      */ 
/*      */   protected boolean functionalIndexMatch(String actual, String definition)
/*      */   {
/* 2035 */     boolean match = false;
/*      */ 
/* 2038 */     actual = actual.replaceAll("\"", "");
/* 2039 */     if (actual.compareToIgnoreCase(definition) == 0)
/*      */     {
/* 2041 */       match = true;
/*      */     }
/*      */ 
/* 2044 */     return match;
/*      */   }
/*      */ 
/*      */   private String generateDefinedFunction(String functionalDef)
/*      */   {
/* 2049 */     String generatedFunction = getDeltaValue("function", functionalDef, "");
/* 2050 */     return generatedFunction;
/*      */   }
/*      */ 
/*      */   protected int compareColumnsForTable(FieldInfo[] fi, DataBinder binder)
/*      */     throws DataException
/*      */   {
/* 2062 */     int result = 0;
/* 2063 */     String tableName = binder.get("tableName");
/* 2064 */     int dbSize = fi.length;
/* 2065 */     DataResultSet fieldDetails = (DataResultSet)binder.getResultSet("fieldDetails");
/* 2066 */     int definitionSize = fieldDetails.getNumRows();
/* 2067 */     Vector defPKs = new Vector();
/* 2068 */     if (dbSize != definitionSize)
/*      */     {
/* 2071 */       Report.trace("datastoredesign", "found difference in table: " + tableName + " " + dbSize + " fields exist in actual database but " + definitionSize + " defined in resource file", null);
/* 2072 */       result = 1;
/*      */     }
/*      */     else
/*      */     {
/* 2078 */       for (int i = 0; i < dbSize; ++i)
/*      */       {
/* 2080 */         FieldInfo colFI = fi[i];
/* 2081 */         String name = colFI.m_name;
/* 2082 */         Vector definition = fieldDetails.findRow(0, name);
/* 2083 */         if (definition == null)
/*      */         {
/* 2086 */           Report.trace("datastoredesign", "found difference in table: " + tableName + " field: " + name + " exists in actual database but not defined in resources", null);
/* 2087 */           result = 1;
/* 2088 */           break;
/*      */         }
/* 2090 */         String isGenerated = (String)definition.get(7);
/* 2091 */         if (!isGenerated.isEmpty())
/*      */         {
/* 2095 */           Report.trace("datastoredesign", "detected autogenerated column at table: " + tableName + " field: " + name + " confirmed existence in actual database", null);
/*      */         }
/*      */         else
/*      */         {
/* 2099 */           boolean defIsPK = StringUtils.convertToBool((String)definition.get(6), false);
/* 2100 */           if (defIsPK)
/*      */           {
/* 2102 */             defPKs.add(name);
/*      */           }
/*      */ 
/* 2105 */           int type = colFI.m_type;
/* 2106 */           String defType = (String)definition.get(5);
/*      */ 
/* 2108 */           if (IsSameType(type, defType))
/*      */           {
/* 2110 */             if ((((type == 2) || (type == 6))) && (!defType.contains("memo")))
/*      */             {
/* 2119 */               int len = colFI.m_maxLen;
/* 2120 */               String strDefLen = (String)definition.get(2);
/* 2121 */               String size = convertToDBSize(defType, strDefLen);
/* 2122 */               int defLen = Integer.valueOf(size).intValue();
/*      */ 
/* 2124 */               if (len != defLen)
/*      */               {
/* 2127 */                 Report.trace("datastoredesign", "found difference in table: " + tableName + "  field: " + name + " defined size(" + defLen + ") does not match actual (" + len + ")", null);
/* 2128 */                 result = 1;
/* 2129 */                 break;
/*      */               }
/* 2131 */               continue;
/* 2132 */             }if (((type != 3) && (type != 11)) || (!defType.contains("number")) || 
/* 2138 */               (colFI.m_maxLen >= 19))
/*      */               continue;
/* 2140 */             result = 1;
/* 2141 */             break;
/*      */           }
/*      */ 
/* 2148 */           Report.trace("datastoredesign", "found difference in table: " + tableName + "  field:" + name + " defined type(" + defType + ") does not match actual (" + type + ")", null);
/* 2149 */           result = 1;
/* 2150 */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2165 */     return result;
/*      */   }
/*      */ 
/*      */   protected boolean IsSameType(int type1, String type2)
/*      */   {
/* 2178 */     boolean result = true;
/*      */ 
/* 2183 */     if ((type2.compareToIgnoreCase("truefalse") == 0) || (type2.compareToIgnoreCase("m_truefalse") == 0))
/*      */     {
/* 2186 */       type2 = "boolean";
/*      */     }
/* 2188 */     else if ((type2.compareToIgnoreCase("varchar") == 0) || (type2.compareToIgnoreCase("m_varchar") == 0))
/*      */     {
/* 2191 */       type2 = "text";
/*      */     }
/* 2193 */     else if ((type2.compareToIgnoreCase("integer") == 0) || (type2.compareToIgnoreCase("m_integer") == 0))
/*      */     {
/* 2196 */       type2 = "number";
/*      */     }
/* 2198 */     else if (type2.compareToIgnoreCase("m_date") == 0)
/*      */     {
/* 2200 */       type2 = "date";
/*      */     }
/* 2202 */     else if (type2.compareToIgnoreCase("m_memo") == 0)
/*      */     {
/* 2204 */       type2 = "memo";
/*      */     }
/*      */ 
/* 2207 */     switch (type1)
/*      */     {
/*      */     case 1:
/* 2210 */       if (type2.compareToIgnoreCase("boolean") == 0)
/*      */         break label290;
/* 2212 */       result = false; break;
/*      */     case 2:
/*      */     case 6:
/* 2219 */       if ((type2.compareToIgnoreCase("text") == 0) || (type2.compareToIgnoreCase("memo") == 0)) {
/*      */         break label290;
/*      */       }
/* 2222 */       result = false; break;
/*      */     case 8:
/* 2226 */       if (type2.compareToIgnoreCase("memo") == 0)
/*      */         break label290;
/* 2228 */       result = false; break;
/*      */     case 5:
/* 2232 */       if (type2.compareToIgnoreCase("date") == 0)
/*      */         break label290;
/* 2234 */       result = false; break;
/*      */     case 3:
/* 2239 */       if ((type2.compareToIgnoreCase("number") == 0) || (type2.compareToIgnoreCase("boolean") == 0)) {
/*      */         break label290;
/*      */       }
/* 2242 */       result = false; break;
/*      */     case 9:
/* 2246 */       if (type2.compareToIgnoreCase("blob") == 0)
/*      */         break label290;
/* 2248 */       result = false; break;
/*      */     case 10:
/* 2252 */       if (type2.compareToIgnoreCase("clob") == 0)
/*      */         break label290;
/* 2254 */       result = false;
/*      */     case 4:
/*      */     case 7:
/*      */     }
/* 2258 */     label290: return result;
/*      */   }
/*      */ 
/*      */   protected int convertToFieldInfoType(String type)
/*      */   {
/* 2269 */     int newType = 3;
/*      */ 
/* 2272 */     if (type.compareToIgnoreCase("boolean") == 0)
/*      */     {
/* 2274 */       newType = 1;
/*      */     }
/* 2276 */     else if (type.compareToIgnoreCase("text") == 0)
/*      */     {
/* 2279 */       newType = 6;
/*      */     }
/* 2281 */     else if (type.compareToIgnoreCase("memo") == 0)
/*      */     {
/* 2283 */       newType = 8;
/*      */     }
/* 2285 */     else if (type.compareToIgnoreCase("date") == 0)
/*      */     {
/* 2287 */       newType = 5;
/*      */     }
/* 2289 */     else if ((type.compareToIgnoreCase("number") == 0) && (type.compareToIgnoreCase("boolean") == 0))
/*      */     {
/* 2292 */       newType = 3;
/*      */     }
/* 2294 */     else if (type.compareToIgnoreCase("blob") == 0)
/*      */     {
/* 2296 */       newType = 9;
/*      */     }
/* 2298 */     else if (type.compareToIgnoreCase("clob") == 0)
/*      */     {
/* 2300 */       newType = 10;
/*      */     }
/* 2302 */     return newType;
/*      */   }
/*      */ 
/*      */   protected boolean isPKChanged(String[] curPKList, String[] newPKList)
/*      */   {
/* 2314 */     String colName = null;
/*      */ 
/* 2316 */     if (curPKList.length != newPKList.length)
/*      */     {
/* 2318 */       return true;
/*      */     }
/*      */ 
/* 2321 */     for (int i = 0; i < curPKList.length; ++i)
/*      */     {
/* 2323 */       colName = curPKList[i];
/* 2324 */       if (StringUtils.findStringIndexEx(newPKList, colName, true) == -1)
/*      */       {
/* 2326 */         return true;
/*      */       }
/*      */     }
/*      */ 
/* 2330 */     return false;
/*      */   }
/*      */ 
/*      */   protected DataBinder alterTable(String compName, String tableName)
/*      */     throws ServiceException
/*      */   {
/* 2343 */     DataBinder results = new DataBinder();
/*      */ 
/* 2345 */     if (tableName.compareTo("DocMeta") == 0)
/*      */     {
/* 2348 */       return results;
/*      */     }
/*      */ 
/* 2351 */     DataBinder binder = new DataBinder();
/* 2352 */     loadTable(compName, tableName, binder);
/*      */     try
/*      */     {
/* 2357 */       FieldInfo[] existingCols = WorkspaceUtils.getActualColumnList(this.m_ws, tableName);
/* 2358 */       DataResultSet newCols = (DataResultSet)binder.getResultSet("fieldDetails");
/* 2359 */       FieldInfo[] addCols = determineAdditionalColumns(existingCols, newCols, tableName);
/*      */ 
/* 2361 */       String[] dropCols = new String[0];
/* 2362 */       String[] primaryKeys = determinePrimaryKeys(newCols);
/* 2363 */       if (addCols.length != 0)
/*      */       {
/* 2365 */         Report.trace("datastoredesign", "Altering Table: " + tableName, null);
/* 2366 */         Report.trace("datastoredesign", "adding " + addCols.length + " columns", null);
/* 2367 */         Report.trace("datastoredesign", "dropping " + dropCols.length + " columns", null);
/* 2368 */         this.m_ws.alterTable(tableName, addCols, dropCols, primaryKeys);
/*      */       }
/* 2370 */       results = captureResults(compName, tableName, newCols, dropCols, primaryKeys);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 2376 */       Report.info("datastoredesign", e, "Error altering Table: " + tableName + " - exiting.", new Object[0]);
/*      */     }
/* 2378 */     return results;
/*      */   }
/*      */ 
/*      */   protected FieldInfo[] determineAdditionalColumns(FieldInfo[] existingCols, DataResultSet newCols, String table)
/*      */   {
/* 2389 */     Vector addCols = new Vector();
/*      */ 
/* 2391 */     for (int i = 0; i < newCols.getNumRows(); ++i)
/*      */     {
/* 2394 */       String autoGenerated = (String)newCols.getRowValues(i).get(7);
/* 2395 */       String newColName = (String)newCols.getRowValues(i).get(0);
/* 2396 */       if (autoGenerated.isEmpty())
/*      */       {
/* 2398 */         boolean colFound = false;
/* 2399 */         for (int j = 0; j < existingCols.length; ++j)
/*      */         {
/* 2401 */           if (existingCols[j].m_name.compareToIgnoreCase(newColName) != 0) {
/*      */             continue;
/*      */           }
/* 2404 */           if (existingCols[j].m_type == 6)
/*      */           {
/* 2407 */             String newColLenStr = "" + (String)newCols.getRowValues(i).get(2);
/* 2408 */             int newColLen = 0;
/*      */             try
/*      */             {
/* 2411 */               newColLen = Integer.parseInt(newColLenStr);
/*      */             }
/*      */             catch (Exception e)
/*      */             {
/* 2417 */               Report.trace("datastoredesign", "ERROR! field:" + newColName + " has non-numeric size:" + newColLenStr, null);
/* 2418 */               newColLen = SharedObjects.getEnvironmentInt("MemoFieldSize", 255);
/*      */             }
/* 2420 */             if ((newColLen == existingCols[j].m_maxLen) || (convertToFieldInfoType((String)newCols.getRowValues(i).get(5)) == 8))
/*      */             {
/* 2424 */               colFound = true;
/*      */             }
/* 2426 */             else if (newColLen < existingCols[j].m_maxLen)
/*      */             {
/* 2430 */               Report.trace("datastoredesign", "ERROR! field:" + newColName + " size is being decreased from: " + existingCols[j].m_maxLen + " to " + newColLen, null);
/* 2431 */               Report.trace("datastoredesign", "rejecting this change! ", null);
/* 2432 */               colFound = true;
/*      */             }
/* 2434 */             break;
/* 2435 */           }if ((((existingCols[j].m_type == 3) || (existingCols[j].m_type == 11))) && (convertToFieldInfoType((String)newCols.getRowValues(i).get(5)) == 3))
/*      */           {
/* 2441 */             if (existingCols[j].m_maxLen <= 18)
/*      */               break;
/* 2443 */             colFound = true; break;
/*      */           }
/*      */ 
/* 2448 */           colFound = true;
/*      */ 
/* 2450 */           break;
/*      */         }
/*      */ 
/* 2453 */         if (!colFound)
/*      */         {
/* 2456 */           FieldInfo fi = new FieldInfo();
/* 2457 */           fi.m_name = newColName;
/* 2458 */           fi.m_type = convertToFieldInfoType((String)newCols.getRowValues(i).get(5));
/* 2459 */           if (fi.m_type == 8)
/*      */           {
/* 2461 */             fi.m_isFixedLen = false;
/*      */           }
/*      */           else
/*      */           {
/* 2465 */             if (fi.m_type == 6)
/*      */             {
/* 2467 */               fi.m_maxLen = Integer.parseInt((String)newCols.getRowValues(i).get(2));
/*      */             }
/* 2469 */             fi.m_isFixedLen = true;
/*      */           }
/* 2471 */           addCols.add(fi);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 2478 */         if ((!this.m_dbType.contains("db2")) || 
/* 2481 */           (DataDesignInstall.isAutoGenerated(this.m_ws, table, newColName)))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 2490 */         Report.trace("datastoredesign", "DSD definition says that table:" + table + " column:" + newColName + " is supposed to be autogenerated, but it is not defined in the DB", null);
/*      */ 
/* 2492 */         Report.trace("datastoredesign", "This is currently not supported - rejecting request", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2541 */     FieldInfo[] retVal = (FieldInfo[])(FieldInfo[])addCols.toArray(new FieldInfo[addCols.size()]);
/* 2542 */     return retVal;
/*      */   }
/*      */ 
/*      */   protected String[] determineDroppedColumns(FieldInfo[] existingCols, DataResultSet newCols)
/*      */   {
/* 2554 */     Vector delCols = new Vector();
/*      */ 
/* 2556 */     for (int i = 0; i < existingCols.length; ++i)
/*      */     {
/* 2558 */       String colName = existingCols[i].m_name;
/* 2559 */       boolean colFound = false;
/* 2560 */       for (int j = 0; j < newCols.getNumRows(); ++j)
/*      */       {
/* 2562 */         String newColName = (String)(String)newCols.getRowValues(j).get(0);
/* 2563 */         if (colName.compareToIgnoreCase(newColName) != 0)
/*      */           continue;
/* 2565 */         colFound = true;
/* 2566 */         break;
/*      */       }
/*      */ 
/* 2569 */       if (colFound) {
/*      */         continue;
/*      */       }
/*      */ 
/* 2573 */       delCols.add(colName);
/*      */     }
/*      */ 
/* 2576 */     String[] retVal = (String[])(String[])delCols.toArray(new String[delCols.size()]);
/* 2577 */     return retVal;
/*      */   }
/*      */ 
/*      */   protected String[] determinePrimaryKeys(DataResultSet fields)
/*      */     throws ServiceException
/*      */   {
/* 2588 */     Vector pkCols = new Vector();
/*      */ 
/* 2590 */     for (int i = 0; i < fields.getNumRows(); ++i)
/*      */     {
/* 2592 */       String isPK = (String)fields.getRowValues(i).get(6);
/* 2593 */       if (!StringUtils.convertToBool(isPK, false))
/*      */         continue;
/* 2595 */       String fieldName = (String)fields.getRowValues(i).get(0);
/* 2596 */       pkCols.add(fieldName);
/*      */     }
/*      */ 
/* 2599 */     String[] retVal = (String[])(String[])pkCols.toArray(new String[pkCols.size()]);
/* 2600 */     return retVal;
/*      */   }
/*      */ 
/*      */   protected DataBinder captureResults(String compName, String tableName, DataResultSet newCols, String[] dropCols, String[] primaryKeys)
/*      */   {
/* 2614 */     DataBinder results = new DataBinder();
/* 2615 */     String[] columns = { "dsdComponentName", "dsdTableName", "dsdColumnName" };
/* 2616 */     DataResultSet newlyAddedColdrset = new DataResultSet(columns);
/* 2617 */     DataResultSet droppedColdrset = new DataResultSet(columns);
/* 2618 */     DataResultSet pkdrset = new DataResultSet(columns);
/*      */ 
/* 2620 */     if (newCols != null)
/*      */     {
/* 2622 */       for (int i = 0; i < newCols.getNumRows(); ++i)
/*      */       {
/* 2624 */         String newColName = (String)newCols.getRowAsList(i).get(0);
/* 2625 */         Vector v = new IdcVector(3);
/* 2626 */         v.add(compName);
/* 2627 */         v.add(tableName);
/* 2628 */         v.add(newColName);
/* 2629 */         newlyAddedColdrset.addRow(v);
/*      */       }
/*      */     }
/*      */ 
/* 2633 */     if (dropCols != null)
/*      */     {
/* 2635 */       for (int i = 0; i < dropCols.length; ++i)
/*      */       {
/* 2637 */         String droppedColumn = dropCols[i];
/* 2638 */         Vector v = new IdcVector(3);
/* 2639 */         v.add(compName);
/* 2640 */         v.add(tableName);
/* 2641 */         v.add(droppedColumn);
/* 2642 */         droppedColdrset.addRow(v);
/*      */       }
/*      */     }
/*      */ 
/* 2646 */     if (primaryKeys != null)
/*      */     {
/* 2648 */       for (int i = 0; i < primaryKeys.length; ++i)
/*      */       {
/* 2650 */         String pk = primaryKeys[i];
/* 2651 */         Vector v = new IdcVector(3);
/* 2652 */         v.add(compName);
/* 2653 */         v.add(tableName);
/* 2654 */         v.add(pk);
/* 2655 */         pkdrset.addRow(v);
/*      */       }
/*      */     }
/*      */ 
/* 2659 */     results.addResultSet("newCols", newlyAddedColdrset);
/* 2660 */     results.addResultSet("delCols", droppedColdrset);
/* 2661 */     results.addResultSet("pkCols", pkdrset);
/* 2662 */     return results;
/*      */   }
/*      */ 
/*      */   public boolean isDBSyntaxFound()
/*      */   {
/* 2667 */     return this.m_dbSyntaxFound;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2672 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105978 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.datastoredesign.DataDesignGenerator
 * JD-Core Version:    0.5.4
 */