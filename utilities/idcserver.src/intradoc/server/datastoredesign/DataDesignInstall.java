/*      */ package intradoc.server.datastoredesign;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.FieldInfoUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.jdbc.JdbcWorkspace;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.IdcExtendedLoader;
/*      */ import intradoc.server.LegacyDirectoryLocator;
/*      */ import intradoc.server.schema.SchemaManager;
/*      */ import intradoc.server.schema.SchemaStorage;
/*      */ import intradoc.server.schema.ServerSchemaManager;
/*      */ import intradoc.server.utils.CompInstallUtils;
/*      */ import intradoc.server.utils.ComponentLocationUtils;
/*      */ import intradoc.shared.MetaFieldUtils;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.schema.SchemaData;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.IOException;
/*      */ import java.io.Writer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collection;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DataDesignInstall
/*      */ {
/*   75 */   public List<String> m_addNewColListIndex = new ArrayList();
/*   76 */   public List<String> m_newlyAddedColumns = new ArrayList();
/*   77 */   public List<String> m_removedColList = new ArrayList();
/*   78 */   public List<String> m_indexColList = new ArrayList();
/*   79 */   public List<String> m_primaryKeyList = new ArrayList();
/*   80 */   public List<String> m_modifiedFieldsIndex = new ArrayList();
/*   81 */   public Map<String, String> m_columnTranslationMap = new HashMap();
/*   82 */   public Map<String, String> m_newlyCreatedTableMap = new HashMap();
/*   83 */   public Map<String, Set<String>> m_indexedColMap = new HashMap();
/*   84 */   public DataResultSet m_indexdrset = new DataResultSet();
/*   85 */   public List<String> m_newlyCreatedTableList = new ArrayList();
/*   86 */   public List<String> m_updatedTableList = new ArrayList();
/*      */ 
/*   89 */   public Map<String, String> m_dsdColumnUpgradeMap = new HashMap();
/*      */   public DataDesignConfigInfo m_configInfo;
/*      */   public Set<String> m_systemTables;
/*      */   public static final String QUERY_STR = "QonlyComponentVersion";
/*      */   public static final String SECTION_PREFIX = "ComponentDBInstallComponent";
/*  107 */   protected static String m_compName = null;
/*      */ 
/*  109 */   protected DataBinder m_data = new DataBinder();
/*  110 */   boolean m_isNew = false;
/*  111 */   boolean m_isUpdated = false;
/*      */ 
/*  114 */   int m_memoSize = 0;
/*      */   public String m_dataDir;
/*      */ 
/*      */   public DataDesignInstall()
/*      */     throws ServiceException
/*      */   {
/*  120 */     DataDesignInstallUtils.upgradeDataDirectoryLocation();
/*      */ 
/*  123 */     String dir = DirectoryLocator.getAppDataDirectory();
/*  124 */     String subDir = DataDesignInstallUtils.getDataDirectoryLocation();
/*  125 */     FileUtils.checkOrCreateDirectory(FileUtils.getAbsolutePath(dir, subDir), 1, 1);
/*      */ 
/*  127 */     this.m_dataDir = (dir + subDir);
/*      */ 
/*  129 */     this.m_configInfo = new DataDesignConfigInfo();
/*  130 */     CompInstallUtils.setDataDesignConfigInfo(this.m_configInfo, 1);
/*      */ 
/*  132 */     this.m_dsdColumnUpgradeMap = DataDesignInstallUtils.getFieldNameUpgradeMap();
/*      */   }
/*      */ 
/*      */   protected void clearLists()
/*      */   {
/*  140 */     this.m_addNewColListIndex.clear();
/*  141 */     this.m_removedColList.clear();
/*  142 */     this.m_indexColList.clear();
/*  143 */     this.m_primaryKeyList.clear();
/*  144 */     this.m_modifiedFieldsIndex.clear();
/*  145 */     this.m_newlyAddedColumns.clear();
/*      */   }
/*      */ 
/*      */   public void configTableForComponents(Workspace workspace, DataBinder binder, IdcExtendedLoader loader)
/*      */     throws DataException, ServiceException
/*      */   {
/*  160 */     DataResultSet drset = new DataResultSet();
/*  161 */     this.m_memoSize = SharedObjects.getEnvironmentInt("MemoFieldSize", 255);
/*  162 */     drset = DataDesignInstallUtils.getUpgradedAndRenamedTable("DataStoreDesignTableList", "TableList", this.m_dsdColumnUpgradeMap);
/*      */ 
/*  167 */     this.m_indexdrset = DataDesignInstallUtils.getUpgradedAndRenamedTable("DataStoreDesignColumnsIndexed", "ColumnsIndexed", this.m_dsdColumnUpgradeMap);
/*      */ 
/*  169 */     ResultSetUtils.sortResultSet(this.m_indexdrset, new String[] { "dsdComponentName", "dsdTableName" });
/*      */ 
/*  172 */     if ((drset == null) || (drset.getNumRows() == 0))
/*      */     {
/*  174 */       return;
/*      */     }
/*      */ 
/*  178 */     loadIndexedColInHash();
/*      */ 
/*  180 */     columnTranslationMap(workspace);
/*      */ 
/*  182 */     String[] columns = { "dsdComponentName", "dsdTableName", "dsdColumnName" };
/*  183 */     DataResultSet newlyAddedColdrset = new DataResultSet(columns);
/*      */ 
/*  185 */     String tableName = null;
/*  186 */     String checkFlag = null;
/*  187 */     String dynamicTable = null;
/*  188 */     String workspaceName = null;
/*  189 */     boolean isCSTable = false;
/*      */ 
/*  191 */     String[] keys = { "dsdTableName", "dsdIsCSTable", "dsdComponentName", "dsdCheckFlag", "dsdIsDynamicTable", "dsdWorkspace" };
/*      */ 
/*  194 */     FieldInfo[] fi = ResultSetUtils.createInfoList(drset, keys, true);
/*      */ 
/*  197 */     loader.setCachedObject("TableList", drset);
/*  198 */     loader.setCachedObject("DataStoreDesignTableList", drset);
/*      */ 
/*  200 */     if (PluginFilters.filter("preConfigComponentTables", workspace, binder, loader) != 0)
/*      */     {
/*  203 */       return;
/*      */     }
/*      */ 
/*  206 */     boolean IsFirstTimeDataStoreDesignRun = !DataDesignInstallUtils.isComponentInstalled(workspace, "DataStoreDesign");
/*      */ 
/*  208 */     ServerSchemaManager schemaMan = null;
/*  209 */     if (!SharedObjects.getEnvValueAsBoolean("SkipSchemaUpgradeForComponents", false))
/*      */     {
/*  211 */       schemaMan = SchemaManager.getManager(workspace);
/*      */     }
/*      */ 
/*  214 */     Map wsMap = new HashMap();
/*      */ 
/*  217 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  219 */       Vector v = drset.getCurrentRowValues();
/*  220 */       String wsName = (String)v.get(fi[5].m_index);
/*  221 */       if ((wsName == null) || (wsName.isEmpty()))
/*      */       {
/*  223 */         wsName = "system";
/*  224 */         v.set(fi[5].m_index, wsName);
/*  225 */         if (wsMap.containsKey(wsName))
/*      */           continue;
/*  227 */         Workspace tableWorkspace = WorkspaceUtils.getWorkspace(wsName);
/*  228 */         wsMap.put(wsName, tableWorkspace);
/*      */       }
/*      */       else
/*      */       {
/*  233 */         String[] workspaceList = StringUtils.makeStringArrayFromSequence(wsName);
/*  234 */         for (String name : workspaceList)
/*      */         {
/*  236 */           if (wsMap.containsKey(name))
/*      */             continue;
/*  238 */           Workspace tableWorkspace = WorkspaceUtils.getWorkspace(name);
/*  239 */           wsMap.put(name, tableWorkspace);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  245 */     DataDesignGenerator generator = new DataDesignGenerator();
/*  246 */     generator.init(wsMap);
/*      */ 
/*  248 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  250 */       Vector v = drset.getCurrentRowValues();
/*  251 */       tableName = (String)v.get(fi[0].m_index);
/*  252 */       isCSTable = false;
/*  253 */       if (fi[1].m_index >= 0)
/*      */       {
/*  255 */         isCSTable = StringUtils.convertToBool((String)v.get(fi[1].m_index), false);
/*      */       }
/*  257 */       m_compName = (String)v.get(fi[2].m_index);
/*  258 */       checkFlag = (String)v.get(fi[3].m_index);
/*  259 */       dynamicTable = (String)v.get(fi[4].m_index);
/*  260 */       workspaceName = (String)v.get(fi[5].m_index);
/*      */ 
/*  262 */       String tempCompName = null;
/*      */ 
/*  264 */       String[] workspaceList = StringUtils.makeStringArrayFromSequence(workspaceName);
/*  265 */       for (String wsName : workspaceList)
/*      */       {
/*  267 */         Workspace ws = (Workspace)wsMap.get(wsName);
/*  268 */         if (ws == null)
/*      */         {
/*  270 */           String msg = LocaleUtils.encodeMessage("csWorkspaceDoesntExists", null, wsName);
/*  271 */           throw new ServiceException(msg);
/*      */         }
/*  273 */         generator.loadWorkspaceInfo(wsName);
/*      */ 
/*  275 */         boolean canPerformOperations = true;
/*      */ 
/*  279 */         boolean isSystemTable = isSystemTable(tableName);
/*  280 */         if ((isCSTable) && (!isSystemTable))
/*      */         {
/*  282 */           Report.trace("datastoredesign", "dsdIsCSTable ('" + isCSTable + "') is " + "incorrect for table '" + tableName + "'; it is not a system table.", null);
/*      */         }
/*  287 */         else if ((!isCSTable) && (isSystemTable))
/*      */         {
/*  295 */           isCSTable = true;
/*      */         }
/*      */ 
/*  299 */         String forceKey = m_compName + ":" + tableName + ":IsForceUpdate";
/*  300 */         String isForceUpdateStr = (String)loader.getCachedObject(forceKey);
/*  301 */         boolean isForceUpdate = StringUtils.convertToBool(isForceUpdateStr, false);
/*      */ 
/*  307 */         if (isForceUpdate)
/*      */         {
/*  309 */           tempCompName = m_compName;
/*  310 */           m_compName = m_compName + ":" + tableName;
/*      */         }
/*      */ 
/*  314 */         canPerformOperations = DataDesignInstallUtils.evaluateCheckFlag(checkFlag);
/*      */ 
/*  316 */         if (isForceUpdate)
/*      */         {
/*  318 */           m_compName = tempCompName;
/*      */         }
/*      */ 
/*  323 */         if ((!SharedObjects.getEnvValueAsBoolean("EnableDSD", true)) || (!generator.isDBSyntaxFound()))
/*      */         {
/*  326 */           canPerformOperations = false;
/*      */         }
/*  328 */         if (!canPerformOperations)
/*      */           continue;
/*  330 */         DataBinder results = generator.generateTable(m_compName, tableName, true, dynamicTable);
/*      */ 
/*  332 */         String newTable = results.getLocal("newTable");
/*  333 */         if (newTable != null)
/*      */         {
/*  335 */           if (!isCSTable)
/*      */           {
/*  338 */             DataBinder myBinder = new DataBinder();
/*  339 */             myBinder.putLocal("schTableName", tableName);
/*  340 */             if (schemaMan != null)
/*      */             {
/*  342 */               schemaMan.addSchemaExistingTable(ws, myBinder);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  347 */           cleanUpDefaultValues(tableName);
/*      */         }
/*      */ 
/*  350 */         newlyAddedColdrset = (DataResultSet)results.getResultSet("newCols");
/*      */ 
/*  352 */         if (newlyAddedColdrset != null)
/*      */         {
/*  354 */           updateNewlyAddedColumns(ws, tableName);
/*  355 */           for (int i = 0; i < newlyAddedColdrset.getNumRows(); ++i)
/*      */           {
/*  357 */             String colName = (String)newlyAddedColdrset.getRowAsList(i).get(2);
/*  358 */             this.m_columnTranslationMap.put(colName.toUpperCase(), colName);
/*      */           }
/*      */ 
/*  362 */           ResultSet indexedColsRset = results.getResultSet("indexedCols");
/*  363 */           if (indexedColsRset != null)
/*      */           {
/*  365 */             List tempIndexedCols = ResultSetUtils.loadValuesFromSet(indexedColsRset, "indexName");
/*  366 */             Set indexedCols = new HashSet();
/*  367 */             indexedCols.addAll(tempIndexedCols);
/*  368 */             this.m_indexedColMap.put(tableName, indexedCols);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  375 */         if (IsFirstTimeDataStoreDesignRun)
/*      */         {
/*  378 */           if ((newTable != null) || (tableName.compareToIgnoreCase("config") == 0))
/*      */           {
/*  380 */             checkAndAddAdditionalDefaultValues(ws, tableName);
/*      */           }
/*      */           else
/*      */           {
/*  384 */             recordDefaultValues(ws, tableName);
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  390 */           checkAndAddAdditionalDefaultValues(ws, tableName);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  397 */     SharedObjects.putObject("ComponentDBInstall", "updateComponentProps", this.m_configInfo.m_configTableValues);
/*  398 */     SharedObjects.putObject("ComponentDBInstall", "compVersionMap", this.m_configInfo.m_versionMap);
/*      */ 
/*  400 */     SharedObjects.putObject("DataDesignInstall", "dataDesignConfigInfo", this.m_configInfo);
/*      */ 
/*  402 */     if (this.m_isUpdated)
/*      */     {
/*  404 */       SharedObjects.putEnvironmentValue("UpdateConfigTable", "true");
/*      */     }
/*      */ 
/*  407 */     if (PluginFilters.filter("postConfigComponentTables", workspace, binder, loader) == 0) {
/*      */       return;
/*      */     }
/*  410 */     loader.setCachedObject("UpdateComponentProps", this.m_configInfo.m_configTableValues);
/*  411 */     loader.setCachedObject("CompVersionMap", this.m_configInfo.m_versionMap);
/*  412 */     loader.setCachedObject("dataDesignConfigInfo", this.m_configInfo);
/*      */ 
/*  414 */     return;
/*      */   }
/*      */ 
/*      */   protected boolean isSystemTable(String tableName)
/*      */   {
/*  427 */     checkCreateSystemTableSet();
/*  428 */     return this.m_systemTables.contains(tableName);
/*      */   }
/*      */ 
/*      */   public void checkCreateSystemTableSet()
/*      */   {
/*  436 */     if (this.m_systemTables != null)
/*      */       return;
/*  438 */     this.m_systemTables = new HashSet();
/*  439 */     DataResultSet sysTables = SharedObjects.getTable("SystemTables");
/*  440 */     int index = sysTables.getFieldInfoIndex("tableName");
/*  441 */     for (sysTables.first(); sysTables.isRowPresent(); sysTables.next())
/*      */     {
/*  443 */       this.m_systemTables.add(sysTables.getStringValue(index));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void updateNewlyAddedColumns(Workspace ws, String tableName)
/*      */     throws DataException, ServiceException
/*      */   {
/*  459 */     DataResultSet drset = DataDesignInstallUtils.getUpgradedTable(m_compName + "." + tableName + ".UpdateValueForNewColumns", this.m_dsdColumnUpgradeMap);
/*      */ 
/*  462 */     if ((drset == null) || (drset.isEmpty()))
/*      */     {
/*  464 */       return;
/*      */     }
/*      */ 
/*  467 */     FieldInfo[] fi = WorkspaceUtils.getActualColumnList(ws, tableName);
/*      */ 
/*  469 */     String[] newlyAddedColumnsName = StringUtils.convertListToArray(this.m_newlyAddedColumns);
/*      */ 
/*  471 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  473 */       String colName = ResultSetUtils.getValue(drset, "dsdNewColumnName");
/*  474 */       String value = ResultSetUtils.getValue(drset, "dsdNewColumnValue");
/*      */ 
/*  476 */       int index = StringUtils.findStringIndexEx(newlyAddedColumnsName, colName, true);
/*      */ 
/*  478 */       if (index < 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  483 */       int pos = getFieldIndex(colName, fi);
/*  484 */       String updateSQL = "UPDATE " + tableName + " SET " + colName + " = ";
/*      */ 
/*  486 */       if (fi[pos].m_type == 5)
/*      */       {
/*  488 */         updateSQL = updateSQL + formatDateString(value);
/*      */       }
/*  490 */       else if (fi[pos].m_type == 6)
/*      */       {
/*  492 */         updateSQL = updateSQL + "'" + value + "'";
/*      */       }
/*      */       else
/*      */       {
/*  496 */         updateSQL = updateSQL + value;
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/*  501 */         ws.executeSQL(updateSQL);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  505 */         String errMsg = LocaleUtils.encodeMessage("csErrorUpdatingNewlyAddedCol", null, colName, value);
/*      */ 
/*  507 */         Report.error("datastoredesign", errMsg, e);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkAndAddAdditionalDefaultValues(Workspace ws, String tableName)
/*      */     throws DataException, ServiceException
/*      */   {
/*  519 */     DataResultSet drset = SharedObjects.getTable(m_compName + "." + tableName + ".DefaultValues");
/*      */ 
/*  521 */     if ((drset == null) || (drset.getNumRows() <= 0))
/*      */       return;
/*  523 */     FieldInfo[] fi = ws.getColumnList(tableName);
/*      */ 
/*  525 */     checkAndAddDefaultValue(ws, tableName, fi, drset);
/*      */   }
/*      */ 
/*      */   protected void checkAndAddDefaultValue(Workspace ws, String tableName, FieldInfo[] fiInfo, DataResultSet drset)
/*      */     throws DataException, ServiceException
/*      */   {
/*  541 */     if ((drset == null) || (drset.getNumRows() == 0))
/*      */     {
/*  543 */       return;
/*      */     }
/*      */ 
/*  546 */     String[] columns = { "tablename", "pks", "pkvalues", "version" };
/*  547 */     DataResultSet defaultRows = new DataResultSet(columns);
/*  548 */     String[] pkArry = ws.getPrimaryKeys(tableName);
/*  549 */     String pks = StringUtils.createStringFromArray(pkArry);
/*  550 */     String pkValues = "";
/*      */ 
/*  553 */     drset = DataDesignInstallUtils.upgradeFieldNames(drset, this.m_dsdColumnUpgradeMap, true);
/*      */ 
/*  555 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  557 */       String version = ResultSetUtils.getValue(drset, "dsdVersion");
/*  558 */       if (version == null)
/*      */       {
/*  561 */         version = "0";
/*      */       }
/*      */ 
/*  564 */       String insertSQL = "INSERT INTO " + tableName;
/*  565 */       String selectSQL = "Select * FROM " + tableName;
/*  566 */       String deleteSQL = "DELETE FROM " + tableName;
/*      */ 
/*  568 */       String columnNames = ResultSetUtils.getValue(drset, "dsdNameOfColumns");
/*  569 */       if (columnNames == null)
/*      */       {
/*  571 */         String fieldList = "";
/*  572 */         Report.trace("datastoredesign", null, "Could not find default column names for table : " + tableName + " aborting addition of default rows.", new Object[0]);
/*  573 */         for (int i = 0; i < drset.getNumFields(); ++i)
/*      */         {
/*  575 */           fieldList = fieldList + drset.getFieldName(i) + ",";
/*      */         }
/*  577 */         Report.trace("datastoredesign", null, "Looked for dsdNameOfColumns in fields : " + fieldList, new Object[0]);
/*  578 */         break;
/*      */       }
/*  580 */       String defColumnValues = ResultSetUtils.getValue(drset, "dsdColumnsValues");
/*  581 */       if (defColumnValues == null)
/*      */       {
/*  583 */         Report.trace("datastoredesign", null, "Could not find default column values for table : " + tableName + " aborting addition of default rows.", new Object[0]);
/*  584 */         String fieldList = "";
/*  585 */         for (int i = 0; i < drset.getNumFields(); ++i)
/*      */         {
/*  587 */           fieldList = fieldList + drset.getFieldName(i) + ",";
/*      */         }
/*  589 */         Report.trace("datastoredesign", null, "Looked for dsdColumnsValues in fields : " + fieldList, new Object[0]);
/*      */ 
/*  591 */         break;
/*      */       }
/*      */ 
/*  594 */       int canInsert = -1;
/*      */ 
/*  596 */       pkValues = extractPKValues(pks, columnNames, defColumnValues);
/*  597 */       canInsert = checkDefaultRow(tableName, pks, pkValues, version);
/*      */ 
/*  602 */       if (canInsert < 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  608 */       String lookupColumns = ResultSetUtils.getValue(drset, "dsdLookupColumns");
/*  609 */       if (lookupColumns == null)
/*      */       {
/*  612 */         lookupColumns = pks;
/*      */       }
/*      */ 
/*  615 */       if (columnNames.equalsIgnoreCase("all"))
/*      */       {
/*  617 */         columnNames = "";
/*  618 */         for (int i = 0; i < fiInfo.length; ++i)
/*      */         {
/*  620 */           columnNames = columnNames + fiInfo[i].m_name + ",";
/*      */         }
/*      */ 
/*  623 */         columnNames = columnNames.substring(0, columnNames.length() - 1);
/*      */       }
/*      */ 
/*  626 */       String[] columnNameArr = StringUtils.makeStringArrayFromSequence(columnNames);
/*  627 */       String[] defColumnValuesArr = StringUtils.makeStringArrayFromSequence(defColumnValues);
/*      */ 
/*  629 */       columnNames = "(" + columnNames + ")";
/*      */ 
/*  631 */       insertSQL = insertSQL + columnNames;
/*      */ 
/*  633 */       if (columnNameArr.length != defColumnValuesArr.length)
/*      */       {
/*  635 */         String errMsg = LocaleUtils.encodeMessage("csCanNotAddDefaultValues", null, m_compName, tableName);
/*      */ 
/*  637 */         String errMsg2 = LocaleUtils.encodeMessage("csNumberValuesMustMatchNumberOfColumns", null);
/*      */ 
/*  639 */         throw new ServiceException(errMsg + errMsg2);
/*      */       }
/*      */ 
/*  642 */       String values = "(";
/*  643 */       String whereClause = "";
/*      */ 
/*  645 */       for (int i = 0; i < defColumnValuesArr.length; ++i)
/*      */       {
/*  647 */         String temp = defColumnValuesArr[i];
/*  648 */         String colName = columnNameArr[i];
/*      */ 
/*  650 */         int num = getFieldIndex(colName, fiInfo);
/*      */ 
/*  652 */         if (num == -1)
/*      */         {
/*  654 */           String errMsg = LocaleUtils.encodeMessage("csCanNotAddDefaultValues", null, m_compName, tableName);
/*      */ 
/*  656 */           String errMsg2 = LocaleUtils.encodeMessage("csColumnDoesNotExist", null, colName, tableName);
/*      */ 
/*  658 */           throw new ServiceException(errMsg + errMsg2);
/*      */         }
/*  660 */         if (temp.equals("#null"))
/*      */         {
/*  662 */           temp = "null";
/*      */         }
/*  664 */         else if (fiInfo[num].m_type == 6)
/*      */         {
/*  666 */           temp = "'" + temp + "'";
/*      */         }
/*  668 */         else if (fiInfo[num].m_type == 5)
/*      */         {
/*  670 */           temp = formatDateString(temp);
/*      */         }
/*      */ 
/*  673 */         values = values + temp + ",";
/*      */ 
/*  675 */         if (lookupColumns.toLowerCase().indexOf(colName.toLowerCase()) < 0)
/*      */           continue;
/*  677 */         if (whereClause.equals(""))
/*      */         {
/*  679 */           whereClause = " WHERE";
/*      */         }
/*      */         else
/*      */         {
/*  683 */           whereClause = whereClause + " AND ";
/*      */         }
/*  685 */         whereClause = whereClause + " " + colName + " = " + temp;
/*      */       }
/*      */ 
/*  688 */       values = values.substring(0, values.length() - 1);
/*  689 */       values = values + ")";
/*      */ 
/*  691 */       insertSQL = insertSQL + " values " + values;
/*      */ 
/*  695 */       if (canInsert == 0)
/*      */       {
/*  700 */         ResultSet rset = ws.createResultSetSQL(selectSQL + whereClause);
/*  701 */         if (rset.isRowPresent())
/*      */         {
/*  703 */           canInsert = -1;
/*      */         }
/*      */       }
/*      */ 
/*  707 */       boolean isSuccess = false;
/*  708 */       if (canInsert == 1)
/*      */       {
/*      */         try
/*      */         {
/*  714 */           ws.executeSQL(deleteSQL + whereClause);
/*  715 */           isSuccess = true;
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*  719 */           String errMsg = LocaleUtils.encodeMessage("csErrorAddingDefaultValuesRow", null, values, tableName);
/*      */ 
/*  721 */           Report.error("datastoredesign", errMsg, e);
/*      */         }
/*      */       }
/*      */ 
/*  725 */       if (canInsert >= 0)
/*      */       {
/*      */         try
/*      */         {
/*  729 */           Report.trace("datastoredesign", null, "Inserting default row for table: " + tableName + " : " + insertSQL, new Object[0]);
/*  730 */           ws.executeSQL(insertSQL);
/*  731 */           isSuccess = true;
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*  735 */           String errMsg = LocaleUtils.encodeMessage("csErrorAddingDefaultValuesRow", null, values, tableName);
/*      */ 
/*  737 */           Report.error("datastoredesign", errMsg, e);
/*      */         }
/*      */       }
/*      */ 
/*  741 */       if (!isSuccess) {
/*      */         continue;
/*      */       }
/*  744 */       Vector row = new Vector();
/*  745 */       row.add(tableName);
/*  746 */       row.add(pks);
/*  747 */       row.add(pkValues);
/*  748 */       row.add(version);
/*  749 */       defaultRows.addRow(row);
/*      */     }
/*      */ 
/*  752 */     recordDefaultRows(defaultRows);
/*      */   }
/*      */ 
/*      */   protected String extractPKValues(String pks, String columnNames, String columnValues)
/*      */   {
/*  757 */     String pkValues = "";
/*      */ 
/*  759 */     Vector pkVect = StringUtils.parseArray(pks, ',', '^');
/*  760 */     Vector nameVect = StringUtils.parseArray(columnNames, ',', '^');
/*  761 */     Vector valueVect = StringUtils.parseArray(columnValues, ',', '^');
/*      */ 
/*  763 */     Iterator pki = pkVect.iterator();
/*  764 */     while (pki.hasNext())
/*      */     {
/*  766 */       String pk = (String)pki.next();
/*  767 */       Iterator namei = nameVect.iterator();
/*  768 */       Iterator valuei = valueVect.iterator();
/*  769 */       boolean found = false;
/*  770 */       while ((!found) && (namei.hasNext()))
/*      */       {
/*  772 */         String name = (String)namei.next();
/*  773 */         String value = (String)valuei.next();
/*  774 */         if (pk.compareToIgnoreCase(name) == 0)
/*      */         {
/*  776 */           found = true;
/*  777 */           if (pkValues.length() == 0)
/*      */           {
/*  779 */             pkValues = value;
/*      */           }
/*      */           else
/*      */           {
/*  783 */             pkValues = pkValues + "," + value;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  789 */     return pkValues;
/*      */   }
/*      */ 
/*      */   protected void recordDefaultRows(DataResultSet defaultRows) throws DataException
/*      */   {
/*  794 */     if (defaultRows.getNumRows() <= 0)
/*      */       return;
/*  796 */     DataResultSet currentRows = loadDefaultRows();
/*  797 */     if (currentRows == null)
/*      */     {
/*  800 */       currentRows = defaultRows;
/*      */     }
/*      */     else
/*      */     {
/*  804 */       currentRows.merge("pkvalues", defaultRows, false);
/*      */     }
/*  806 */     writeDefaultRows(currentRows);
/*      */   }
/*      */ 
/*      */   protected void recordDefaultValues(Workspace ws, String tableName)
/*      */     throws DataException, ServiceException
/*      */   {
/*  820 */     DataResultSet drset = SharedObjects.getTable(m_compName + "." + tableName + "." + "DefaultValues");
/*  821 */     if (drset == null)
/*      */       return;
/*  823 */     String[] columns = { "tablename", "pks", "pkvalues", "version" };
/*  824 */     DataResultSet defaultRows = new DataResultSet(columns);
/*  825 */     String[] pkArry = ws.getPrimaryKeys(tableName);
/*  826 */     String pks = StringUtils.createStringFromArray(pkArry);
/*  827 */     String pkValues = "";
/*      */ 
/*  830 */     drset = DataDesignInstallUtils.upgradeFieldNames(drset, this.m_dsdColumnUpgradeMap, true);
/*      */ 
/*  832 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  835 */       String version = ResultSetUtils.getValue(drset, "dsdVersion");
/*  836 */       if (version == null)
/*      */       {
/*  839 */         version = "0";
/*      */       }
/*      */ 
/*  842 */       String columnNames = ResultSetUtils.getValue(drset, "dsdNameOfColumns");
/*  843 */       if (columnNames == null)
/*      */       {
/*  845 */         String fieldList = "";
/*  846 */         Report.trace("datastoredesign", null, "Could not find default column names for table : " + tableName + " aborting addition of default rows.", new Object[0]);
/*  847 */         for (int i = 0; i < drset.getNumFields(); ++i)
/*      */         {
/*  849 */           fieldList = fieldList + drset.getFieldName(i) + ",";
/*      */         }
/*  851 */         Report.trace("datastoredesign", null, "Looked for dsdNameOfColumns in fields : " + fieldList, new Object[0]);
/*  852 */         break;
/*      */       }
/*  854 */       String defColumnValues = ResultSetUtils.getValue(drset, "dsdColumnsValues");
/*  855 */       if (defColumnValues == null)
/*      */       {
/*  857 */         Report.trace("datastoredesign", null, "Could not find default column values for table : " + tableName + " aborting addition of default rows.", new Object[0]);
/*  858 */         String fieldList = "";
/*  859 */         for (int i = 0; i < drset.getNumFields(); ++i)
/*      */         {
/*  861 */           fieldList = fieldList + drset.getFieldName(i) + ",";
/*      */         }
/*  863 */         Report.trace("datastoredesign", null, "Looked for dsdColumnsValues in fields : " + fieldList, new Object[0]);
/*      */ 
/*  865 */         break;
/*      */       }
/*      */ 
/*  868 */       pkValues = extractPKValues(pks, columnNames, defColumnValues);
/*      */ 
/*  870 */       Vector row = new Vector();
/*  871 */       row.add(tableName);
/*  872 */       row.add(pks);
/*  873 */       row.add(pkValues);
/*  874 */       row.add(version);
/*  875 */       defaultRows.addRow(row);
/*      */     }
/*  877 */     recordDefaultRows(defaultRows);
/*      */   }
/*      */ 
/*      */   protected void cleanUpDefaultValues(String tablename)
/*      */     throws DataException
/*      */   {
/*  888 */     DataResultSet drset = loadDefaultRows();
/*  889 */     DataResultSet newDrSet = new DataResultSet();
/*  890 */     String tableName = tablename;
/*  891 */     ResultSetFilter rsFilter = new ResultSetFilter(tableName)
/*      */     {
/*      */       public int checkRow(String val, int curNumRow, Vector row)
/*      */       {
/*  895 */         if (val.equalsIgnoreCase(this.val$tableName))
/*      */         {
/*  897 */           return 0;
/*      */         }
/*  899 */         return 1;
/*      */       }
/*      */     };
/*  902 */     newDrSet.copyFiltered(drset, "tablename", rsFilter);
/*      */ 
/*  904 */     writeDefaultRows(newDrSet);
/*      */   }
/*      */ 
/*      */   protected DataResultSet loadDefaultRows()
/*      */   {
/*  909 */     String basedir = LegacyDirectoryLocator.getIntradocDir();
/*  910 */     String defaultFile = basedir + "data/datastoredesign/DefaultRows.hda";
/*  911 */     DataBinder binder = new DataBinder();
/*  912 */     BufferedReader input = null;
/*      */     try
/*      */     {
/*  916 */       input = new BufferedReader(FileUtilsCfgBuilder.getCfgReader(defaultFile));
/*  917 */       binder.receive(input);
/*      */     }
/*      */     catch (FileNotFoundException e)
/*      */     {
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  928 */       Report.error("datastoredesign", "IOException reading " + defaultFile, e);
/*      */     }
/*      */     finally
/*      */     {
/*  932 */       FileUtils.closeObject(input);
/*      */     }
/*      */ 
/*  935 */     DataResultSet defaultRows = (DataResultSet)binder.getResultSet("defaultRows");
/*  936 */     if (defaultRows == null)
/*      */     {
/*  938 */       String[] columns = { "tablename", "pks", "pkvalues", "version" };
/*  939 */       defaultRows = new DataResultSet(columns);
/*      */     }
/*  941 */     return defaultRows;
/*      */   }
/*      */ 
/*      */   protected void writeDefaultRows(DataResultSet drset) throws DataException
/*      */   {
/*  946 */     String dataDir = DirectoryLocator.getAppDataDirectory();
/*  947 */     String defaultFile = dataDir + "datastoredesign/DefaultRows.hda";
/*  948 */     DataBinder binder = new DataBinder();
/*  949 */     binder.addResultSet("defaultRows", drset);
/*  950 */     Writer output = null;
/*      */     try
/*      */     {
/*  953 */       output = FileUtilsCfgBuilder.getCfgWriter(defaultFile, "datastoredesign");
/*  954 */       binder.send(output);
/*      */     }
/*      */     catch (IOException e1)
/*      */     {
/*  959 */       Report.error("datastoredesign", "Error creating default rows file " + defaultFile, e1);
/*      */     }
/*      */     finally
/*      */     {
/*  963 */       FileUtils.closeObject(output);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected int checkDefaultRow(String tableName, String pks, String pkValues, String version)
/*      */   {
/*  980 */     int addThisRow = -1;
/*  981 */     boolean foundOldRecord = false;
/*      */ 
/*  983 */     DataResultSet currentDefaultRows = loadDefaultRows();
/*  984 */     DataResultSet tableDefaults = new DataResultSet();
/*  985 */     tableDefaults.copySimpleFiltered(currentDefaultRows, "tablename", tableName);
/*  986 */     for (int i = 0; i < tableDefaults.getNumRows(); ++i)
/*      */     {
/* 1001 */       String rowPKValues = (String)tableDefaults.getRowAsList(i).get(2);
/* 1002 */       Vector previousValues = StringUtils.parseArray(rowPKValues, ',', '^');
/* 1003 */       Vector newValues = StringUtils.parseArray(pkValues, ',', '^');
/*      */ 
/* 1006 */       if (compareVectors(previousValues, newValues) != true)
/*      */         continue;
/* 1008 */       foundOldRecord = true;
/*      */ 
/* 1011 */       String oldVersion = (String)tableDefaults.getRowAsList(i).get(3);
/* 1012 */       int oldVer = Integer.parseInt(oldVersion);
/* 1013 */       int newVer = Integer.parseInt(version);
/* 1014 */       if (newVer <= oldVer) {
/*      */         break;
/*      */       }
/* 1017 */       addThisRow = 1; break;
/*      */     }
/*      */ 
/* 1023 */     if (!foundOldRecord)
/*      */     {
/* 1026 */       addThisRow = 0;
/*      */     }
/* 1028 */     return addThisRow;
/*      */   }
/*      */ 
/*      */   protected boolean compareVectors(Vector v1, Vector v2)
/*      */   {
/* 1040 */     boolean vectorsMatch = false;
/* 1041 */     int numPKs = v1.size();
/* 1042 */     if (v2.size() == numPKs)
/*      */     {
/* 1045 */       for (int j = 0; j < numPKs; ++j)
/*      */       {
/* 1047 */         vectorsMatch = false;
/* 1048 */         String pk = (String)v1.get(j);
/* 1049 */         for (int k = 0; k < numPKs; ++k)
/*      */         {
/* 1051 */           if (pk.compareTo((String)v2.get(k)) != 0)
/*      */             continue;
/* 1053 */           vectorsMatch = true;
/* 1054 */           break;
/*      */         }
/*      */ 
/* 1057 */         if (!vectorsMatch) {
/*      */           break;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1064 */     return vectorsMatch;
/*      */   }
/*      */ 
/*      */   protected String formatDateString(String dateStr)
/*      */     throws ServiceException
/*      */   {
/* 1076 */     if (dateStr.equalsIgnoreCase("currentDateAndTime"))
/*      */     {
/* 1078 */       return LocaleUtils.formatODBC(new Date());
/*      */     }
/*      */ 
/* 1081 */     if (dateStr.trim().length() == 0)
/*      */     {
/* 1083 */       return "null";
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1089 */       Date date = LocaleResources.parseDate(dateStr, null);
/* 1090 */       return LocaleUtils.formatODBC(date);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1095 */       String errMsg = LocaleUtils.encodeMessage("csCBUnableToParseDate", null);
/* 1096 */       Report.error("datastoredesign", errMsg, e);
/* 1097 */     }return "null";
/*      */   }
/*      */ 
/*      */   protected int getFieldIndex(String colName, FieldInfo[] fi)
/*      */   {
/* 1110 */     int index = -1;
/*      */ 
/* 1112 */     for (int i = 0; i < fi.length; ++i)
/*      */     {
/* 1114 */       if (!fi[i].m_name.equalsIgnoreCase(colName))
/*      */         continue;
/* 1116 */       index = i;
/* 1117 */       break;
/*      */     }
/*      */ 
/* 1120 */     return index;
/*      */   }
/*      */ 
/*      */   protected FieldInfo[] parseColInfoForTable(DataResultSet tabledrset, String tableName, Workspace ws, boolean isCSTable, boolean isChangeMemoToVarchar, boolean getIndexedCols)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1139 */     List colNames = new ArrayList();
/* 1140 */     int num = tabledrset.getNumRows();
/*      */ 
/* 1143 */     tabledrset = DataDesignInstallUtils.upgradeFieldNames(tabledrset, this.m_dsdColumnUpgradeMap, true);
/*      */ 
/* 1145 */     FieldInfo[] fi = new FieldInfo[num];
/*      */ 
/* 1147 */     String[] keys = { "dsdColumnName", "dsdFieldType", "dsdFieldLength", "dsdIsPrimary", "dsdDefaultValue" };
/*      */ 
/* 1149 */     FieldInfo[] info = ResultSetUtils.createInfoList(tabledrset, keys, false);
/*      */ 
/* 1151 */     String colName = null;
/* 1152 */     String colType = null;
/* 1153 */     String colLength = null;
/* 1154 */     boolean isPrimary = false;
/* 1155 */     String colDefVal = null;
/*      */ 
/* 1157 */     tabledrset.first();
/*      */ 
/* 1159 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1161 */       Vector v = tabledrset.getRowValues(i);
/* 1162 */       colName = (String)v.get(info[0].m_index);
/* 1163 */       colType = (String)v.get(info[1].m_index);
/* 1164 */       colLength = (String)v.get(info[2].m_index);
/* 1165 */       isPrimary = StringUtils.convertToBool((String)v.get(info[3].m_index), false);
/* 1166 */       if (info[4].m_index >= 0)
/*      */       {
/* 1168 */         colDefVal = (String)v.get(info[4].m_index);
/*      */       }
/*      */ 
/* 1171 */       colName = colName.trim();
/* 1172 */       colNames.add(colName);
/*      */ 
/* 1174 */       fi[i] = new FieldInfo();
/*      */ 
/* 1176 */       fi[i].m_name = colName;
/* 1177 */       fi[i].m_type = DataDesignInstallUtils.changeToFieldType(colType);
/*      */ 
/* 1179 */       if ((colLength.length() > 0) && (!colLength.equalsIgnoreCase("null")))
/*      */       {
/* 1181 */         int defLen = 0;
/* 1182 */         if (colLength.toLowerCase().equalsIgnoreCase("max"))
/*      */         {
/* 1184 */           defLen = 0;
/*      */         }
/* 1186 */         else if (colLength.toLowerCase().contains("memosize"))
/*      */         {
/* 1188 */           defLen = this.m_memoSize;
/*      */         }
/*      */         else
/*      */         {
/* 1192 */           defLen = NumberUtils.parseInteger(colLength, 0);
/* 1193 */           if (defLen == 0)
/*      */           {
/* 1195 */             String errMsg = LocaleUtils.encodeMessage("csCanNotParseFieldLength", null, m_compName, colName, tableName);
/*      */ 
/* 1197 */             throw new DataException(errMsg);
/*      */           }
/*      */         }
/*      */ 
/* 1201 */         fi[i].m_isFixedLen = (defLen > 0);
/* 1202 */         fi[i].m_maxLen = defLen;
/*      */       }
/*      */ 
/* 1205 */       if (fi[i].m_type == 8)
/*      */       {
/* 1207 */         fi[i].m_isFixedLen = false;
/*      */ 
/* 1209 */         if (isChangeMemoToVarchar)
/*      */         {
/* 1212 */           fi[i].m_type = DataDesignInstallUtils.changeToFieldType("varchar");
/*      */         }
/*      */ 
/* 1217 */         if (SharedObjects.getEnvValueAsBoolean("UsePre80MetaFieldType", false))
/*      */         {
/* 1219 */           fi[i].m_type = DataDesignInstallUtils.changeToFieldType("varchar");
/* 1220 */           fi[i].m_isFixedLen = true;
/*      */         }
/*      */ 
/* 1223 */         if (isPrimary)
/*      */         {
/* 1225 */           String errMsg = LocaleUtils.encodeMessage("csCBMemoFieldCannotBePrimaryField", null, m_compName, colName, tableName);
/*      */ 
/* 1227 */           throw new ServiceException(errMsg);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1232 */       if (isPrimary)
/*      */       {
/* 1234 */         this.m_primaryKeyList.add(colName);
/*      */       }
/*      */ 
/* 1238 */       if ((colDefVal == null) || (colDefVal.length() <= 0)) {
/*      */         continue;
/*      */       }
/* 1241 */       if (fi[i].m_type == 5)
/*      */       {
/* 1243 */         colDefVal = formatDateString(colDefVal);
/*      */       }
/*      */ 
/* 1246 */       FieldInfoUtils.setFieldOption(fi[i], "DefaultValue", colDefVal);
/*      */     }
/*      */ 
/* 1253 */     if (isCSTable)
/*      */     {
/* 1255 */       FieldInfo[] curFi = WorkspaceUtils.getActualColumnList(ws, tableName);
/* 1256 */       for (int i = 0; i < curFi.length; ++i)
/*      */       {
/* 1258 */         colNames.add(curFi[i].m_name);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1264 */     if (getIndexedCols)
/*      */     {
/* 1266 */       getIndexedColNamesFromSharedObjects(colNames, tableName);
/*      */     }
/*      */ 
/* 1269 */     return fi;
/*      */   }
/*      */ 
/*      */   public void synchronizeUpdatedTablesWithSchema(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1285 */     ServerSchemaManager schemaMan = SchemaManager.getManager(ws);
/* 1286 */     SchemaStorage tables = schemaMan.getStorageImplementor("SchemaTableConfig");
/* 1287 */     if (ws == null)
/*      */     {
/* 1289 */       Report.trace("datastoredesign", "not synchronizing tables because workspace is null.", null);
/* 1290 */       return;
/*      */     }
/*      */ 
/* 1293 */     for (String tableName : this.m_updatedTableList)
/*      */     {
/* 1296 */       if (!WorkspaceUtils.doesTableExist(ws, tableName, null))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1302 */       SchemaData schemaData = tables.getSchemaData(tableName);
/* 1303 */       if (schemaData == null)
/*      */       {
/* 1305 */         binder.putLocal("schTableName", tableName);
/* 1306 */         schemaMan.addSchemaExistingTable(ws, binder);
/*      */       }
/*      */       else
/*      */       {
/* 1311 */         schemaMan.synchronizeSchemaTableDefinition(ws, tableName);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void configDocMetaForComponent(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1331 */     this.m_isUpdated = SharedObjects.getEnvValueAsBoolean("UpdateConfigTable", false);
/* 1332 */     this.m_configInfo = ((DataDesignConfigInfo)SharedObjects.getObject("DataDesignInstall", "dataDesignConfigInfo"));
/*      */ 
/* 1335 */     if (this.m_configInfo == null)
/*      */     {
/* 1337 */       this.m_configInfo = new DataDesignConfigInfo();
/* 1338 */       CompInstallUtils.setDataDesignConfigInfo(this.m_configInfo, 2);
/*      */     }
/*      */ 
/* 1342 */     if (addOrUpdateDocMetaColumns(ws, binder, cxt))
/*      */     {
/* 1344 */       this.m_isUpdated = true;
/*      */     }
/*      */ 
/* 1347 */     if (!this.m_isUpdated)
/*      */       return;
/* 1349 */     List updateComponentList = new ArrayList();
/*      */ 
/* 1351 */     for (String key : this.m_configInfo.m_configTableValues.keySet())
/*      */     {
/* 1353 */       updateComponentList.add(this.m_configInfo.m_configTableValues.get(key));
/*      */     }
/*      */ 
/* 1356 */     DataDesignInstallUtils.updateConfigTable(ws, updateComponentList);
/*      */   }
/*      */ 
/*      */   protected boolean addOrUpdateDocMetaColumns(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1368 */     if (binder == null)
/*      */     {
/* 1370 */       binder = new DataBinder();
/*      */     }
/*      */ 
/* 1373 */     boolean isUpdated = false;
/*      */ 
/* 1376 */     Set indexDocMetaForComponent = new HashSet();
/* 1377 */     DataResultSet drset = DataDesignInstallUtils.getUpgradedAndRenamedTable("DataStoreDesignCreateDocMetaDefinition", "CreateDocMetaDefinition", this.m_dsdColumnUpgradeMap);
/*      */ 
/* 1379 */     clearLists();
/* 1380 */     if ((drset == null) || (drset.getNumRows() == 0))
/*      */     {
/* 1382 */       return false;
/*      */     }
/*      */ 
/* 1385 */     String version = null;
/* 1386 */     String columnName = null;
/*      */ 
/* 1388 */     boolean isSchemaEnabled = SharedObjects.getEnvValueAsBoolean("EnableSchemaPublish", false);
/*      */ 
/* 1390 */     if (isSchemaEnabled)
/*      */     {
/* 1392 */       SharedObjects.putEnvironmentValue("EnableSchemaPublish", "false");
/*      */     }
/*      */ 
/* 1395 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1397 */       String[] keys = { "dsdComponentName", "dsdVersion", "dsdCheckFlag", "dName" };
/*      */ 
/* 1399 */       FieldInfo[] fi = ResultSetUtils.createInfoList(drset, keys, true);
/*      */ 
/* 1401 */       Vector v = drset.getCurrentRowValues();
/* 1402 */       m_compName = (String)v.get(fi[0].m_index);
/* 1403 */       version = (String)v.get(fi[1].m_index);
/* 1404 */       columnName = (String)v.get(fi[3].m_index);
/*      */ 
/* 1406 */       boolean canPerformOperations = DataDesignInstallUtils.evaluateCheckFlag((String)v.get(fi[2].m_index));
/*      */ 
/* 1408 */       if (!canPerformOperations)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1414 */       String forceKey = columnName + ":IsForceUpdate";
/* 1415 */       String isForceUpdateStr = (String)cxt.getCachedObject(forceKey);
/* 1416 */       boolean isForceUpdate = StringUtils.convertToBool(isForceUpdateStr, false);
/*      */ 
/* 1421 */       if (isForceUpdate)
/*      */       {
/* 1423 */         m_compName = m_compName + ":" + columnName;
/*      */       }
/*      */ 
/* 1426 */       if (!DataDesignInstallUtils.isNewVersion(ws, version, m_compName, columnName, this.m_configInfo))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1431 */       if (!canPerformOperations)
/*      */         continue;
/* 1433 */       populateDocMetaDef(v, drset, ws, binder, cxt);
/* 1434 */       indexDocMetaForComponent.add(m_compName);
/* 1435 */       isUpdated = true;
/*      */     }
/*      */ 
/* 1439 */     if (isUpdated)
/*      */     {
/* 1441 */       CompInstallUtils.executeService(ws, "UPDATE_META_TABLE", binder);
/*      */     }
/*      */ 
/* 1444 */     SharedObjects.putEnvironmentValue("EnableSchemaPublish", "true");
/*      */ 
/* 1446 */     if (isUpdated)
/*      */     {
/* 1448 */       CompInstallUtils.executeService(ws, "PUBLISH_SCHEMA", binder);
/*      */     }
/*      */ 
/* 1451 */     if (!isSchemaEnabled)
/*      */     {
/* 1453 */       SharedObjects.putEnvironmentValue("EnableSchemaPublish", "false");
/*      */     }
/*      */ 
/* 1456 */     serializeDocMetaInfoToHDAForUninstall(drset, "DocMetaColumns", "dsdComponentName", "dName");
/* 1457 */     indexDocMetaTableForComponents(ws, indexDocMetaForComponent);
/*      */ 
/* 1459 */     return isUpdated;
/*      */   }
/*      */ 
/*      */   protected void indexDocMetaTableForComponents(Workspace ws, Set<String> compNames)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1473 */     for (String compName : compNames)
/*      */     {
/* 1475 */       clearLists();
/* 1476 */       m_compName = compName;
/* 1477 */       this.m_indexdrset = DataDesignInstallUtils.getUpgradedAndRenamedTable("DataStoreDesignColumnsIndexed", "ColumnsIndexed", this.m_dsdColumnUpgradeMap);
/*      */ 
/* 1480 */       ResultSetUtils.sortResultSet(this.m_indexdrset, new String[] { "dsdComponentName", "dsdTableName" });
/*      */ 
/* 1482 */       if ((this.m_indexdrset != null) || (this.m_indexdrset.getNumRows() > 0))
/*      */       {
/* 1484 */         FieldInfo[] fi = ws.getColumnList("DocMeta");
/*      */ 
/* 1486 */         List columnsInDocMeta = new ArrayList(fi.length);
/* 1487 */         for (FieldInfo fieldInfo : fi)
/*      */         {
/* 1489 */           columnsInDocMeta.add(fieldInfo.m_name);
/*      */         }
/*      */ 
/* 1492 */         loadIndexedColInHash();
/*      */ 
/* 1494 */         getIndexedColNamesFromSharedObjects(columnsInDocMeta, "DocMeta");
/*      */ 
/* 1496 */         if (this.m_indexColList.size() > 0)
/*      */         {
/* 1498 */           indexColForTable(ws, "DocMeta");
/* 1499 */           serializeHDAIndexColList();
/* 1500 */           DataResultSet docMetaIndexDrset = new DataResultSet();
/* 1501 */           docMetaIndexDrset.copySimpleFiltered(this.m_indexdrset, "dsdTableName", "DocMeta");
/* 1502 */           serializeDocMetaInfoToHDAForUninstall(docMetaIndexDrset, "ColumnsIndexedInDocMeta", "dsdComponentName", "dsdColumnName");
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void populateDocMetaDef(List<String> DocMetaDefRow, DataResultSet drset, Workspace ws, DataBinder binder, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1517 */     DataBinder docBinder = new DataBinder();
/* 1518 */     drset = DataDesignInstallUtils.upgradeFieldNames(drset, this.m_dsdColumnUpgradeMap, true);
/*      */ 
/* 1549 */     String[] keys = { "dName", "dCaption", "dType", "dIsRequired", "dIsEnabled", "dIsSearchable", "dIsOptionList", "dDefaultValue", "dOptionListKey", "dOptionListType", "dOrder", "dsdIsSchema" };
/*      */ 
/* 1552 */     String[] optionalKeys = { "dIsPlaceholderField", "dsdComponentName" };
/* 1553 */     FieldInfo[] fi = ResultSetUtils.createInfoList(drset, keys, true);
/* 1554 */     FieldInfo[] info = ResultSetUtils.createInfoList(drset, optionalKeys, false);
/*      */ 
/* 1556 */     String name = (String)DocMetaDefRow.get(fi[0].m_index);
/* 1557 */     String caption = (String)DocMetaDefRow.get(fi[1].m_index);
/* 1558 */     String type = (String)DocMetaDefRow.get(fi[2].m_index);
/* 1559 */     String isRequired = (StringUtils.convertToBool((String)DocMetaDefRow.get(fi[3].m_index), false)) ? "1" : "0";
/* 1560 */     String isEnabled = (StringUtils.convertToBool((String)DocMetaDefRow.get(fi[4].m_index), true)) ? "1" : "0";
/* 1561 */     String isSearchable = (StringUtils.convertToBool((String)DocMetaDefRow.get(fi[5].m_index), true)) ? "1" : "0";
/* 1562 */     String isOptionList = (StringUtils.convertToBool((String)DocMetaDefRow.get(fi[6].m_index), false)) ? "1" : "0";
/* 1563 */     String defaultValue = (String)DocMetaDefRow.get(fi[7].m_index);
/* 1564 */     String optionListKey = (String)DocMetaDefRow.get(fi[8].m_index);
/* 1565 */     String optionListType = (String)DocMetaDefRow.get(fi[9].m_index);
/* 1566 */     String order = (String)DocMetaDefRow.get(fi[10].m_index);
/*      */ 
/* 1568 */     int isPlaceHolderField = 0;
/* 1569 */     if (info[0].m_index >= 0)
/*      */     {
/* 1571 */       String isPlaceHolderFieldStr = (String)DocMetaDefRow.get(info[0].m_index);
/* 1572 */       isPlaceHolderField = NumberUtils.parseInteger(isPlaceHolderFieldStr, 0);
/*      */     }
/*      */ 
/* 1576 */     if (info[1].m_index >= 0)
/*      */     {
/* 1578 */       String cmpName = (String)DocMetaDefRow.get(info[1].m_index);
/* 1579 */       docBinder.putLocal("dComponentName", cmpName);
/*      */     }
/*      */ 
/* 1582 */     if (type.equalsIgnoreCase("Checkbox"))
/*      */     {
/* 1584 */       type = "Text";
/*      */     }
/*      */ 
/* 1587 */     docBinder.putLocal("dName", name);
/* 1588 */     docBinder.putLocal("dCaption", caption);
/* 1589 */     docBinder.putLocal("dType", type);
/* 1590 */     docBinder.putLocal("dIsRequired", isRequired);
/* 1591 */     docBinder.putLocal("dIsEnabled", isEnabled);
/* 1592 */     docBinder.putLocal("dIsSearchable", isSearchable);
/* 1593 */     docBinder.putLocal("dIsOptionList", isOptionList);
/* 1594 */     docBinder.putLocal("dDefaultValue", defaultValue);
/* 1595 */     docBinder.putLocal("dOptionListKey", optionListKey);
/* 1596 */     docBinder.putLocal("dOptionListType", optionListType);
/* 1597 */     docBinder.putLocal("dOrder", order);
/* 1598 */     docBinder.putLocal("dIsPlaceholderField", "" + isPlaceHolderField);
/*      */ 
/* 1600 */     boolean bIsOptionList = StringUtils.convertToBool(isOptionList, false);
/*      */ 
/* 1602 */     if (bIsOptionList)
/*      */     {
/* 1604 */       configureOptionList(optionListKey, docBinder);
/*      */     }
/*      */ 
/* 1607 */     boolean isSchema = StringUtils.convertToBool((String)DocMetaDefRow.get(fi[11].m_index), false);
/*      */ 
/* 1610 */     if (MetaFieldUtils.hasDocMetaDef(name))
/*      */     {
/* 1612 */       docBinder.putLocal("isNewMetaDataField", "false");
/*      */     }
/*      */ 
/* 1615 */     if (isSchema)
/*      */     {
/* 1617 */       docBinder = getSchemaField(name);
/*      */     }
/* 1619 */     MetaFieldUtils.updateDocMetaDefinitionTable(ws, docBinder, cxt);
/*      */ 
/* 1621 */     if ((!bIsOptionList) || (hasOptionList(optionListKey)) || (isSchema))
/*      */       return;
/* 1623 */     setOptionList(ws, optionListKey);
/*      */   }
/*      */ 
/*      */   protected void configureOptionList(String optionListKey, DataBinder binder)
/*      */   {
/* 1629 */     boolean isView = false;
/* 1630 */     boolean isTree = false;
/*      */ 
/* 1633 */     String key = optionListKey;
/*      */ 
/* 1635 */     isView = key.startsWith(SchemaHelper.VIEW_PREFIX);
/* 1636 */     isTree = key.startsWith(SchemaHelper.TREE_PREFIX);
/*      */ 
/* 1638 */     if (isView)
/*      */     {
/* 1640 */       key = key.substring(SchemaHelper.VIEW_PREFIX.length());
/* 1641 */       binder.putLocal("UseOptionList", "0");
/* 1642 */       binder.putLocal("UseViewList", "1");
/* 1643 */       binder.putLocal("UseTreeControl", "0");
/* 1644 */       binder.putLocal("OptionViewKey", key);
/*      */     }
/* 1647 */     else if (isTree)
/*      */     {
/* 1649 */       key = key.substring(SchemaHelper.TREE_PREFIX.length());
/* 1650 */       binder.putLocal("UseOptionList", "0");
/* 1651 */       binder.putLocal("UseViewList", "0");
/* 1652 */       binder.putLocal("UseTreeControl", "1");
/*      */     }
/*      */     else
/*      */     {
/* 1656 */       binder.putLocal("UseOptionList", "1");
/* 1657 */       binder.putLocal("UseViewList", "0");
/* 1658 */       binder.putLocal("UseTreeControl", "0");
/* 1659 */       binder.putLocal("OptionListKey", key);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected DataBinder getSchemaField(String columnName)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1670 */     DataBinder sBinder = new DataBinder();
/*      */ 
/* 1672 */     String path = ComponentLocationUtils.computeAbsoluteComponentLocation(m_compName);
/* 1673 */     if ((path != null) && (path.length() > 0))
/*      */     {
/* 1675 */       sBinder = ResourceUtils.readDataBinder(FileUtils.getDirectory(path), "/data/schema/fields/" + columnName.toLowerCase() + ".hda");
/*      */     }
/*      */ 
/* 1680 */     return sBinder;
/*      */   }
/*      */ 
/*      */   protected boolean hasOptionList(String optionListKey)
/*      */   {
/* 1688 */     List opts = SharedObjects.getOptList(optionListKey);
/*      */ 
/* 1690 */     return (opts != null) && (opts.size() >= 2);
/*      */   }
/*      */ 
/*      */   protected void setOptionList(Workspace ws, String optionListKey)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1699 */     ResultSet rs = SharedObjects.getTable(optionListKey);
/*      */ 
/* 1701 */     String tempOptionListKey = optionListKey;
/*      */ 
/* 1703 */     int index = optionListKey.indexOf("view://");
/* 1704 */     if (index >= 0)
/*      */     {
/* 1706 */       tempOptionListKey = optionListKey.substring(7, optionListKey.length());
/*      */     }
/*      */ 
/* 1709 */     if ((rs == null) || (rs.isEmpty()))
/*      */     {
/* 1711 */       rs = SharedObjects.getTable(tempOptionListKey + "Install");
/*      */     }
/*      */ 
/* 1714 */     if ((rs == null) || (rs.isEmpty()))
/*      */       return;
/* 1716 */     StringBuffer list = new StringBuffer();
/* 1717 */     int listSize = 0;
/*      */     do
/*      */     {
/* 1721 */       if (listSize > 0)
/*      */       {
/* 1723 */         list.append("\n");
/*      */       }
/* 1725 */       list.append(rs.getStringValue(0));
/* 1726 */       ++listSize;
/* 1727 */     }while (rs.next());
/*      */ 
/* 1729 */     MetaFieldUtils.setOptionList(ws, null, tempOptionListKey, list.toString());
/*      */   }
/*      */ 
/*      */   protected void getIndexedColNamesFromSharedObjects(List<String> colNamesList, String tableName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1744 */     String[] colNameArray = StringUtils.convertListToArray(colNamesList);
/*      */ 
/* 1746 */     List temp = new ArrayList();
/* 1747 */     List tempIndexColList = new ArrayList();
/* 1748 */     Set indexedCols = (Set)this.m_indexedColMap.get(tableName);
/*      */ 
/* 1750 */     boolean isAddToHashTable = false;
/*      */ 
/* 1753 */     if ((this.m_indexdrset == null) || (this.m_indexdrset.getNumRows() == 0))
/*      */     {
/* 1755 */       return;
/*      */     }
/*      */ 
/* 1758 */     if (indexedCols == null)
/*      */     {
/* 1760 */       indexedCols = new HashSet();
/* 1761 */       isAddToHashTable = true;
/*      */     }
/*      */ 
/* 1764 */     String[] keys = { "dsdTableName", "dsdColumnName" };
/* 1765 */     FieldInfo[] fi = ResultSetUtils.createInfoList(this.m_indexdrset, keys, true);
/*      */ 
/* 1767 */     DataResultSet drset = new DataResultSet();
/*      */ 
/* 1772 */     drset = DataDesignInstallUtils.filterColumnsForTable(this.m_indexdrset, m_compName, tableName);
/*      */ 
/* 1774 */     if ((drset == null) || (drset.getNumRows() == 0))
/*      */     {
/* 1776 */       return;
/*      */     }
/*      */ 
/* 1779 */     int countOfAlreadyIndexed = 0;
/*      */ 
/* 1781 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1783 */       String colName = drset.getStringValue(fi[1].m_index);
/* 1784 */       String[] tempColArray = { colName };
/*      */ 
/* 1787 */       if (colName.indexOf(44) != -1)
/*      */       {
/* 1789 */         tempColArray = StringUtils.makeStringArrayFromSequence(colName);
/*      */       }
/*      */ 
/* 1792 */       for (int i = 0; i < tempColArray.length; ++i)
/*      */       {
/* 1794 */         String tempColName = tempColArray[i];
/*      */ 
/* 1796 */         if (StringUtils.findStringIndexEx(colNameArray, tempColName, true) != -1)
/*      */           continue;
/* 1798 */         String errMsg = LocaleUtils.encodeMessage("csColumnCanNotBeIndexed", null, m_compName, tempColName, tableName);
/*      */ 
/* 1800 */         Report.error("datastoredesign", errMsg, null);
/* 1801 */         throw new ServiceException(errMsg);
/*      */       }
/*      */ 
/* 1805 */       tempIndexColList.add(colName);
/*      */ 
/* 1807 */       if (this.m_isNew)
/*      */       {
/* 1809 */         indexedCols.add(colName);
/*      */       }
/* 1813 */       else if (!indexedCols.contains(colName))
/*      */       {
/* 1815 */         temp.add(colName);
/*      */       }
/*      */       else
/*      */       {
/* 1819 */         ++countOfAlreadyIndexed;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1824 */     if (this.m_isNew)
/*      */     {
/* 1826 */       this.m_indexColList = tempIndexColList;
/* 1827 */       this.m_indexedColMap.put(tableName, indexedCols);
/*      */     }
/*      */     else
/*      */     {
/* 1832 */       if (countOfAlreadyIndexed < indexedCols.size());
/* 1851 */       for (String colName : temp)
/*      */       {
/* 1853 */         indexedCols.add(colName);
/*      */       }
/*      */ 
/* 1859 */       this.m_indexColList = temp;
/*      */ 
/* 1861 */       if (!isAddToHashTable)
/*      */         return;
/* 1863 */       this.m_indexedColMap.put(tableName, indexedCols);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void loadIndexedColInHash()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1878 */     String fileName = "columnIndexedList.hda";
/*      */ 
/* 1880 */     File file = FileUtilsCfgBuilder.getCfgFile(this.m_dataDir + fileName, null, false);
/*      */ 
/* 1882 */     if (file.exists())
/*      */     {
/* 1884 */       this.m_data = ResourceUtils.readDataBinder(this.m_dataDir, fileName);
/*      */ 
/* 1886 */       Enumeration enumSet = this.m_data.getResultSetList();
/*      */ 
/* 1888 */       while (enumSet.hasMoreElements())
/*      */       {
/* 1890 */         String tableName = (String)enumSet.nextElement();
/* 1891 */         Set indexedCols = getIndexedColsName(tableName);
/* 1892 */         this.m_indexedColMap.put(tableName, indexedCols);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1897 */       this.m_isNew = true;
/*      */     }
/*      */ 
/* 1900 */     this.m_data.clearResultSets();
/*      */   }
/*      */ 
/*      */   protected Set<String> getIndexedColsName(String tableName)
/*      */     throws DataException
/*      */   {
/* 1920 */     ResultSet rs = this.m_data.getResultSet(tableName);
/* 1921 */     List tempIndexedCols = ResultSetUtils.loadValuesFromSet(rs, "columnsIndexed");
/*      */ 
/* 1923 */     Set indexedCols = new HashSet();
/* 1924 */     indexedCols.addAll(tempIndexedCols);
/*      */ 
/* 1926 */     return indexedCols;
/*      */   }
/*      */ 
/*      */   protected void indexColForTable(Workspace ws, String tableName)
/*      */     throws DataException
/*      */   {
/*      */     try
/*      */     {
/* 1940 */       for (int i = 0; i < this.m_indexColList.size(); ++i)
/*      */       {
/* 1942 */         String colName = (String)this.m_indexColList.get(i);
/*      */ 
/* 1944 */         if (colName.indexOf(",") == -1)
/*      */         {
/* 1946 */           String[] temp = new String[1];
/* 1947 */           temp[0] = colName;
/*      */           try
/*      */           {
/* 1950 */             ws.addIndex(tableName, temp);
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/* 1954 */             Report.warning("datastoredesign", e, "csUnabletoCreateIndexfortable", new Object[] { tableName });
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 1960 */           String[] indexArray = StringUtils.makeStringArrayFromSequence(colName);
/*      */           try
/*      */           {
/* 1964 */             ws.addIndex(tableName, indexArray);
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/* 1968 */             Report.warning("datastoredesign", e, "csUnabletoCreateIndexfortable", new Object[] { null, tableName });
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1976 */       Report.warning("datastoredesign", e, "csUnabletoCreateIndexfortable", new Object[] { tableName });
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void serializeHDAIndexColList()
/*      */     throws ServiceException
/*      */   {
/* 1989 */     String fileName = "columnIndexedList.hda";
/*      */ 
/* 1991 */     for (String tableName : this.m_indexedColMap.keySet())
/*      */     {
/* 1993 */       List l = new ArrayList();
/* 1994 */       l.addAll((Collection)this.m_indexedColMap.get(tableName));
/* 1995 */       DataResultSet drset = ResultSetUtils.createResultSetFromList(tableName, l, "columnsIndexed");
/*      */ 
/* 1997 */       this.m_data.addResultSet(tableName, drset);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2002 */       FileUtils.reserveDirectory(this.m_dataDir);
/* 2003 */       ResourceUtils.serializeDataBinder(this.m_dataDir, fileName, this.m_data, true, false);
/*      */     }
/*      */     finally
/*      */     {
/* 2007 */       FileUtils.releaseDirectory(this.m_dataDir);
/*      */     }
/*      */ 
/* 2010 */     this.m_data.clearResultSets();
/*      */   }
/*      */ 
/*      */   public void cleanTablesForComponentUninstall(Workspace ws, String compName, DataBinder binder)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2030 */     m_compName = compName;
/*      */ 
/* 2033 */     DataResultSet drset = new DataResultSet();
/* 2034 */     Set props = new HashSet();
/* 2035 */     drset = loadDocMetaInfo("DocMetaColumns", "dsdComponentName", "dName");
/* 2036 */     props = convertDataResultSetToSet(drset, "dsdComponentName", "dName");
/*      */ 
/* 2038 */     DataResultSet compDocMetaColumndrset = new DataResultSet();
/* 2039 */     compDocMetaColumndrset.copyFieldInfo(drset);
/*      */ 
/* 2041 */     compDocMetaColumndrset.copySimpleFiltered(drset, "dsdComponentName", compName);
/*      */ 
/* 2043 */     hideCustomDocMetaFields(ws, binder, compDocMetaColumndrset, props, compName);
/* 2044 */     drset = new DataResultSet();
/* 2045 */     props = new HashSet();
/* 2046 */     drset = loadDocMetaInfo("ColumnsIndexedInDocMeta", "dsdComponentName", "dsdColumnName");
/* 2047 */     props = convertDataResultSetToSet(drset, "dsdComponentName", "dsdColumnName");
/*      */ 
/* 2051 */     DataResultSet compDocMetaIndexColumndrset = new DataResultSet();
/*      */ 
/* 2053 */     compDocMetaIndexColumndrset.copySimpleFiltered(drset, "dsdComponentName", compName);
/*      */ 
/* 2055 */     dropCustomDocMetaIndexes(ws, compDocMetaIndexColumndrset, props, compName);
/* 2056 */     writeDocMetaInfoToHDA(props, "ColumnsIndexedInDocMeta", "dsdComponentName", "dsdColumnName");
/* 2057 */     serializeHDAIndexColList();
/*      */ 
/* 2059 */     cleanConfigTable(ws, compName);
/*      */   }
/*      */ 
/*      */   protected void cleanConfigTable(Workspace ws, String compName)
/*      */   {
/* 2070 */     String deleteSQL = "delete from config where dName = '" + compName + "' And " + "(dSection = 'ComponentDBInstallComponentUpdate' or " + "dSection = 'ComponentDBInstallComponentInstall')";
/*      */     try
/*      */     {
/* 2077 */       ws.executeSQL(deleteSQL);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void hideCustomDocMetaFields(Workspace ws, DataBinder binder, DataResultSet drset, Set<String> props, String compName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2100 */     if (binder == null)
/*      */     {
/* 2102 */       binder = new DataBinder();
/*      */     }
/*      */ 
/* 2105 */     boolean isUpdated = false;
/*      */ 
/* 2107 */     boolean isSchemaEnabled = SharedObjects.getEnvValueAsBoolean("EnableSchemaPublish", false);
/*      */ 
/* 2109 */     if (isSchemaEnabled)
/*      */     {
/* 2111 */       SharedObjects.putEnvironmentValue("EnableSchemaPublish", "false");
/*      */     }
/*      */ 
/* 2114 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 2116 */       String fieldName = ResultSetUtils.getValue(drset, "dName");
/*      */ 
/* 2118 */       if (!MetaFieldUtils.hasDocMetaDef(fieldName))
/*      */         continue;
/* 2120 */       hideDocMetaData(ws, fieldName);
/* 2121 */       props.remove(compName + "." + fieldName);
/* 2122 */       isUpdated = true;
/*      */     }
/*      */ 
/* 2125 */     if (isUpdated)
/*      */     {
/* 2127 */       CompInstallUtils.executeService(ws, "UPDATE_META_TABLE", binder);
/* 2128 */       writeDocMetaInfoToHDA(props, "DocMetaColumns", "dsdComponentName", "dName");
/*      */     }
/*      */ 
/* 2131 */     SharedObjects.putEnvironmentValue("EnableSchemaPublish", "true");
/*      */ 
/* 2133 */     if (isUpdated)
/*      */     {
/* 2135 */       CompInstallUtils.executeService(ws, "PUBLISH_SCHEMA", binder);
/*      */     }
/*      */ 
/* 2138 */     if (isSchemaEnabled)
/*      */       return;
/* 2140 */     SharedObjects.putEnvironmentValue("EnableSchemaPublish", "false");
/*      */   }
/*      */ 
/*      */   public void hideDocMetaData(Workspace ws, String name)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2154 */     DataResultSet docMetaDefs = SharedObjects.getTable("DocMetaDefinition");
/* 2155 */     FieldInfo[] fi = ResultSetUtils.createInfoList(docMetaDefs, new String[] { "dName", "dCaption", "dType", "dIsRequired", "dIsEnabled", "dIsSearchable", "dIsOptionList", "dDefaultValue", "dOptionListKey", "dOptionListType", "dOrder" }, true);
/*      */ 
/* 2160 */     if (docMetaDefs.isEmpty())
/*      */       return;
/* 2162 */     DataBinder binder = new DataBinder();
/* 2163 */     if (docMetaDefs.findRow(fi[0].m_index, name) == null)
/*      */       return;
/* 2165 */     for (int i = 0; i < fi.length; ++i)
/*      */     {
/* 2167 */       String key = fi[i].m_name;
/* 2168 */       String val = docMetaDefs.getStringValue(fi[i].m_index);
/*      */ 
/* 2170 */       if ((key.equals("dIsEnabled")) || (key.equals("dIsRequired")))
/*      */       {
/* 2172 */         val = "0";
/*      */       }
/* 2174 */       binder.putLocal(fi[i].m_name, val);
/*      */     }
/* 2176 */     CompInstallUtils.executeService(ws, "EDIT_METADEF", binder);
/*      */   }
/*      */ 
/*      */   protected void dropCustomDocMetaIndexes(Workspace ws, DataResultSet drset, Set<String> uninstallProps, String compName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2194 */     loadIndexedColInHash();
/* 2195 */     Set indexedCols = (Set)this.m_indexedColMap.get("DocMeta");
/* 2196 */     drset = DataDesignInstallUtils.upgradeFieldNames(drset, this.m_dsdColumnUpgradeMap, true);
/*      */ 
/* 2198 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 2200 */       String indexName = ResultSetUtils.getValue(drset, "dsdColumnName");
/*      */ 
/* 2202 */       if ((indexName == null) || (indexName.length() <= 0))
/*      */         continue;
/* 2204 */       if (indexedCols != null)
/*      */       {
/* 2206 */         indexedCols.remove(indexName);
/*      */       }
/*      */       try
/*      */       {
/* 2210 */         uninstallProps.remove(compName + "." + indexName);
/* 2211 */         dropIndex(ws, indexName);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 2215 */         Report.error("datastoredesign", compName + "Uninstall:" + e.getMessage(), e);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void dropIndex(Workspace ws, String indexName)
/*      */     throws DataException
/*      */   {
/* 2231 */     boolean useShortVersion = SharedObjects.getEnvValueAsBoolean("UseDatabaseShortIndexName", false);
/* 2232 */     String[] indexCols = StringUtils.makeStringArrayFromSequence(indexName);
/*      */ 
/* 2234 */     String indexSQL = "DocMeta";
/* 2235 */     for (int i = 0; i < indexCols.length; ++i)
/*      */     {
/* 2237 */       indexSQL = indexSQL + "_" + indexCols[i];
/*      */     }
/*      */ 
/* 2240 */     if (useShortVersion)
/*      */     {
/* 2243 */       indexSQL = getShortIndexName(indexSQL);
/*      */     }
/*      */ 
/* 2246 */     String createIndexSql = "DROP INDEX DocMeta." + indexSQL;
/* 2247 */     ws.executeSQL(createIndexSql);
/*      */   }
/*      */ 
/*      */   protected String getShortIndexName(String name)
/*      */   {
/* 2259 */     if (name.length() < 19)
/*      */     {
/* 2261 */       return name;
/*      */     }
/*      */ 
/* 2264 */     int hash = name.hashCode();
/* 2265 */     String hashStr = Integer.toHexString(hash).toUpperCase();
/* 2266 */     String begin = name.substring(0, 6);
/* 2267 */     String end = name.substring(name.length() - 2);
/* 2268 */     return begin + hashStr + end;
/*      */   }
/*      */ 
/*      */   protected void serializeDocMetaInfoToHDAForUninstall(DataResultSet drset, String infoName, String col1, String col2)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2282 */     DataResultSet docMetaIndexList = loadDocMetaInfo(infoName, col1, col2);
/* 2283 */     Set props = convertDataResultSetToSet(docMetaIndexList, col1, col2);
/*      */ 
/* 2285 */     String[] keys = { col1, col2 };
/*      */ 
/* 2287 */     FieldInfo[] fi = ResultSetUtils.createInfoList(drset, keys, true);
/*      */ 
/* 2289 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 2291 */       Vector v = drset.getCurrentRowValues();
/*      */ 
/* 2293 */       String compName = (String)v.get(fi[0].m_index);
/* 2294 */       String colname = (String)v.get(fi[1].m_index);
/* 2295 */       props.add(compName + "." + colname);
/*      */     }
/*      */ 
/* 2298 */     writeDocMetaInfoToHDA(props, infoName, col1, col2);
/*      */   }
/*      */ 
/*      */   protected void writeDocMetaInfoToHDA(Set<String> props, String infoName, String col1, String col2)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2313 */     if (props.isEmpty())
/*      */       return;
/* 2315 */     String[] columns = { col1, col2 };
/* 2316 */     DataResultSet tableDrset = new DataResultSet(columns);
/*      */ 
/* 2318 */     for (String test : props)
/*      */     {
/* 2320 */       String[] split = test.split("\\.");
/* 2321 */       tableDrset.addRow(StringUtils.convertToVector(split));
/*      */     }
/*      */ 
/* 2324 */     String fileName = infoName + ".hda";
/*      */ 
/* 2326 */     this.m_data.addResultSet(infoName, tableDrset);
/*      */ 
/* 2328 */     ResourceUtils.serializeDataBinder(this.m_dataDir, fileName, this.m_data, true, false);
/*      */ 
/* 2330 */     this.m_data.clearResultSets();
/*      */   }
/*      */ 
/*      */   protected DataResultSet loadDocMetaInfo(String infoName, String col1, String col2)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2346 */     String fileName = infoName + ".hda";
/* 2347 */     String[] columns = { col1, col2 };
/* 2348 */     DataResultSet drset = new DataResultSet(columns);
/* 2349 */     drset = DataDesignInstallUtils.upgradeFieldNames(drset, this.m_dsdColumnUpgradeMap, true);
/*      */ 
/* 2351 */     File file = FileUtilsCfgBuilder.getCfgFile(this.m_dataDir + fileName, null, false);
/*      */ 
/* 2353 */     if (file.exists())
/*      */     {
/* 2355 */       this.m_data = ResourceUtils.readDataBinder(this.m_dataDir, fileName);
/*      */ 
/* 2357 */       ResultSet rs = this.m_data.getResultSet(infoName);
/* 2358 */       drset.copy(rs);
/* 2359 */       drset = DataDesignInstallUtils.upgradeFieldNames(drset, this.m_dsdColumnUpgradeMap, true);
/*      */     }
/*      */ 
/* 2362 */     this.m_data.clearResultSets();
/* 2363 */     return drset;
/*      */   }
/*      */ 
/*      */   protected Set<String> convertDataResultSetToSet(DataResultSet drset, String col1, String col2)
/*      */   {
/* 2377 */     Set props = new HashSet();
/*      */ 
/* 2379 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 2381 */       String compName = ResultSetUtils.getValue(drset, col1);
/* 2382 */       String columnName = ResultSetUtils.getValue(drset, col2);
/* 2383 */       props.add(compName + "." + columnName);
/*      */     }
/*      */ 
/* 2386 */     return props;
/*      */   }
/*      */ 
/*      */   protected void loadLegacyColumnTranslationMap()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2394 */     String fileName = "ComponentDBInstallColumnTranslationMap.hda";
/*      */ 
/* 2397 */     File file = new File(this.m_dataDir + fileName);
/* 2398 */     if (!file.exists())
/*      */       return;
/* 2400 */     Report.trace(null, "DEPRECATED: legacy file: " + file.getAbsolutePath() + " detected.  Loading this table.", null);
/* 2401 */     this.m_data = ResourceUtils.readDataBinder(this.m_dataDir, fileName);
/*      */ 
/* 2403 */     DataResultSet drset = (DataResultSet)this.m_data.getResultSet("ComponentDBInstallColumnTranslationMap");
/* 2404 */     drset.first();
/* 2405 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 2407 */       List v = drset.getCurrentRowValues();
/* 2408 */       String colName = (String)v.get(0);
/* 2409 */       String alias = (String)v.get(1);
/* 2410 */       this.m_columnTranslationMap.put(colName, alias);
/*      */     }
/*      */ 
/* 2413 */     this.m_data.clearResultSets();
/*      */   }
/*      */ 
/*      */   protected void columnTranslationMap(Workspace ws)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2427 */     this.m_columnTranslationMap.clear();
/*      */ 
/* 2430 */     loadLegacyColumnTranslationMap();
/*      */ 
/* 2433 */     DataResultSet drset = DataDesignInstallUtils.getUpgradedAndRenamedTable("DataStoreDesignTableList", "TableList", this.m_dsdColumnUpgradeMap);
/*      */ 
/* 2436 */     if ((drset == null) || (drset.getNumRows() == 0))
/*      */     {
/* 2439 */       return;
/*      */     }
/*      */ 
/* 2442 */     String[] keys = { "dsdTableName", "dsdComponentName" };
/*      */ 
/* 2444 */     FieldInfo[] fi = ResultSetUtils.createInfoList(drset, keys, true);
/*      */ 
/* 2446 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 2448 */       Vector v = drset.getCurrentRowValues();
/* 2449 */       String tableName = (String)v.elementAt(fi[0].m_index);
/* 2450 */       String compName = (String)v.elementAt(fi[1].m_index);
/*      */ 
/* 2452 */       DataResultSet tableColumnsdrset = DataDesignInstallUtils.getUpgradedTable(compName + "." + tableName, this.m_dsdColumnUpgradeMap);
/*      */ 
/* 2454 */       if ((tableColumnsdrset == null) || (tableColumnsdrset.getNumRows() == 0))
/*      */         continue;
/* 2456 */       String[] tableKeys = { "dsdColumnName" };
/*      */ 
/* 2458 */       FieldInfo[] info = ResultSetUtils.createInfoList(tableColumnsdrset, tableKeys, true);
/*      */ 
/* 2460 */       for (tableColumnsdrset.first(); tableColumnsdrset.isRowPresent(); tableColumnsdrset.next())
/*      */       {
/* 2462 */         Vector v1 = tableColumnsdrset.getCurrentRowValues();
/* 2463 */         String colName = (String)v1.elementAt(info[0].m_index);
/* 2464 */         this.m_columnTranslationMap.put(colName.toUpperCase(), colName);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2469 */     if (this.m_columnTranslationMap.isEmpty())
/*      */       return;
/* 2471 */     String[] columns = { "column", "alias" };
/* 2472 */     DataResultSet columnTranslationDrset = new DataResultSet(columns);
/* 2473 */     for (String upperName : this.m_columnTranslationMap.keySet())
/*      */     {
/* 2475 */       Vector v = new Vector();
/* 2476 */       v.add(upperName);
/* 2477 */       v.add(this.m_columnTranslationMap.get(upperName));
/* 2478 */       columnTranslationDrset.addRow(v);
/*      */     }
/*      */ 
/* 2481 */     JdbcWorkspace jdbcws = (JdbcWorkspace)ws;
/* 2482 */     jdbcws.loadColumnMap(columnTranslationDrset);
/*      */   }
/*      */ 
/*      */   public static boolean isAutoGenerated(Workspace ws, String table, String column)
/*      */   {
/* 2496 */     boolean isGenerated = false;
/* 2497 */     String dbType = ws.getProperty("DatabaseType");
/* 2498 */     dbType = dbType.toLowerCase();
/* 2499 */     if (dbType.contains("db2"))
/*      */     {
/* 2501 */       String GeneratedCheck = "select TABNAME,COLNAME from syscat.columns WHERE GENERATED = 'A'";
/* 2502 */       GeneratedCheck = GeneratedCheck + " AND TABNAME = '" + table.toUpperCase() + "'";
/* 2503 */       GeneratedCheck = GeneratedCheck + " AND COLNAME = '" + column.toUpperCase() + "'";
/*      */ 
/* 2505 */       ResultSet results = null;
/*      */       try
/*      */       {
/* 2508 */         results = ws.createResultSetSQL(GeneratedCheck);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 2512 */         Report.trace("datastoredesign", "Failure detecting autogenerated column.", e);
/*      */       }
/* 2514 */       if ((results != null) && (!results.isEmpty()))
/*      */       {
/* 2516 */         Report.trace("datastoredesign", "Tab:" + table + " Col:" + column + " detected as autogenerated.", null);
/* 2517 */         isGenerated = true;
/*      */       }
/*      */     }
/* 2520 */     return isGenerated;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2525 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101215 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.datastoredesign.DataDesignInstall
 * JD-Core Version:    0.5.4
 */