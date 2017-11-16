/*      */ package intradoc.server.archive;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.TimeZoneFormat;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.SerializeTable;
/*      */ import intradoc.server.SubjectManager;
/*      */ import intradoc.server.ValueMapData;
/*      */ import intradoc.server.utils.TableModHistoryUtils;
/*      */ import intradoc.shared.ClausesData;
/*      */ import intradoc.shared.CollectionData;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.ExportQueryData;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.FileReader;
/*      */ import java.io.IOException;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ArchiveTableHelper
/*      */ {
/*   35 */   protected ArchiveHandler m_archiver = null;
/*   36 */   protected Workspace m_workspace = null;
/*   37 */   protected ReportProgress m_reportProgress = null;
/*   38 */   protected ExecutionContext m_context = null;
/*   39 */   protected SerializeTable m_st = null;
/*   40 */   protected boolean m_useOracleSyntaxForOuterJoin = false;
/*   41 */   protected boolean m_hasLimitedTimeResolution = false;
/*   42 */   protected boolean m_isAllowRegisterMod = false;
/*      */ 
/*   44 */   protected DataBinder m_binder = null;
/*   45 */   protected CollectionData m_collection = null;
/*   46 */   protected String m_archiveName = null;
/*   47 */   protected String m_archiveDir = null;
/*   48 */   protected String m_archiveExportDir = null;
/*   49 */   protected DataBinder m_archiveData = null;
/*   50 */   protected Date m_archiveDate = null;
/*   51 */   protected String m_curContentDir = null;
/*      */ 
/*   54 */   protected Vector m_changedTables = new IdcVector();
/*      */ 
/*   57 */   protected Hashtable m_subjectsMap = null;
/*      */ 
/*   60 */   protected int m_maxNumExportRows = 100000;
/*      */ 
/*   62 */   protected final String MODIFIED = "modified";
/*   63 */   protected final String ADD = "add";
/*   64 */   protected final String DELETED = "deleted";
/*      */ 
/*   66 */   public static final String[][] KEYS = { { "aTableName", "tableName" }, { "aCreateTimeStamp", "createTimeStamp" }, { "aModifiedTimeStamp", "modifyTimeStamp" }, { "aUseSourceID", "useSourceID" }, { "aIsCreateNewTable", "isCreateTable" }, { "aIsCreateNewField", "isSyncTableDesign" }, { "aParentTables", "parentTables" }, { "aTableRelations", "tableRelations" }, { "aIsReplicateDeletedRows", "isReplicateDeletedRows" }, { "aUseParentTS", "useParentTS" }, { "aRemoveExistingChildren", "removeExistingChildren" }, { "aDeleteParentOnlyWhenNoChild", "removeOnlyWhenNoChild" }, { "aAllowDeleteParentRows", "allowDeleteParentRows" } };
/*      */ 
/*      */   public ArchiveTableHelper(ArchiveHandler handler, Workspace ws, ReportProgress rp, ExecutionContext cxt, SerializeTable st)
/*      */   {
/*   78 */     this.m_archiver = handler;
/*   79 */     this.m_workspace = ws;
/*   80 */     this.m_reportProgress = rp;
/*   81 */     this.m_context = cxt;
/*   82 */     this.m_st = st;
/*      */   }
/*      */ 
/*      */   public void init(DataBinder binder, CollectionData collection, String archiveName, String archiveDir, String archiveExportDir, DataBinder archiveData, Date archiveDate)
/*      */   {
/*   88 */     this.m_binder = binder;
/*   89 */     this.m_collection = collection;
/*   90 */     this.m_archiveName = archiveName;
/*   91 */     this.m_archiveDir = archiveDir;
/*   92 */     this.m_archiveExportDir = archiveExportDir;
/*   93 */     this.m_archiveData = archiveData;
/*   94 */     this.m_archiveDate = archiveDate;
/*      */ 
/*   96 */     String dbType = this.m_workspace.getProperty("DatabaseType");
/*   97 */     ArchiveTableUtils.determineSyntaxOnOuterJoin(this.m_workspace);
/*   98 */     if (dbType.startsWith("microsoft"))
/*      */     {
/*  100 */       this.m_hasLimitedTimeResolution = true;
/*      */     }
/*      */ 
/*  103 */     initNotificationSubjects();
/*  104 */     initCacheObjects();
/*  105 */     this.m_isAllowRegisterMod = SharedObjects.getEnvValueAsBoolean("ArchiverAllowsRegisterModifications", true);
/*  106 */     this.m_maxNumExportRows = SharedObjects.getEnvironmentInt("MaxRowsPerTableArchiveExportBatch", this.m_maxNumExportRows);
/*      */   }
/*      */ 
/*      */   public void reset()
/*      */   {
/*  111 */     this.m_curContentDir = null;
/*      */   }
/*      */ 
/*      */   public void initNotificationSubjects()
/*      */   {
/*  116 */     this.m_subjectsMap = new Hashtable();
/*  117 */     this.m_subjectsMap.put("users", "users");
/*  118 */     this.m_subjectsMap.put("usersecurityattributes", "users");
/*  119 */     this.m_subjectsMap.put("doctypes", "doctypes");
/*      */ 
/*  121 */     String maps = this.m_binder.getAllowMissing("ArchiverTableSubjectsMap");
/*  122 */     if (maps == null)
/*      */       return;
/*  124 */     Vector mapList = StringUtils.parseArray(maps, ',', ',');
/*  125 */     int size = mapList.size();
/*  126 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  128 */       String map = (String)mapList.elementAt(i);
/*  129 */       Vector entry = StringUtils.parseArray(map, ':', '^');
/*  130 */       if (entry.size() != 2)
/*      */         continue;
/*  132 */       String table = (String)entry.elementAt(0);
/*  133 */       this.m_subjectsMap.put(table.toLowerCase(), entry.elementAt(1));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void initCacheObjects()
/*      */   {
/*  141 */     this.m_context.setCachedObject("archiveData", this.m_archiveData);
/*  142 */     this.m_context.setCachedObject("archiveName", this.m_archiveName);
/*  143 */     this.m_context.setCachedObject("archiveDir", this.m_archiveDir);
/*  144 */     this.m_context.setCachedObject("archiveExportDir", this.m_archiveExportDir);
/*  145 */     this.m_context.setCachedObject("archiver", this.m_archiver);
/*  146 */     this.m_context.setCachedObject("tableSerializer", this.m_st);
/*  147 */     this.m_context.setCachedObject("DataBinder", this.m_binder);
/*      */   }
/*      */ 
/*      */   public void doExport() throws DataException, ServiceException
/*      */   {
/*  152 */     String tables = this.m_archiveData.getLocal("aExportTables");
/*  153 */     if ((tables == null) || (tables.length() == 0))
/*      */     {
/*  156 */       Report.trace("archiver", "No table to export.", null);
/*  157 */       return;
/*      */     }
/*  159 */     Report.trace("archiver", "Export these tables: " + tables, null);
/*  160 */     doExportTables(tables);
/*      */   }
/*      */ 
/*      */   public DataBinder doExportTables(String tableList) throws DataException, ServiceException
/*      */   {
/*  165 */     reset();
/*  166 */     DataBinder binder = new DataBinder();
/*  167 */     Vector tables = StringUtils.parseArray(tableList, ',', ',');
/*  168 */     int size = tables.size();
/*  169 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  171 */       String table = (String)tables.elementAt(i);
/*  172 */       String msg = LocaleUtils.encodeMessage("csArchiverExportingTable", null, table);
/*  173 */       this.m_archiver.reportProgressPercent(msg, i, size);
/*      */ 
/*  175 */       String whereClause = this.m_archiveData.getLocal("aExportTable" + tables.elementAt(i));
/*  176 */       ExportQueryData data = new ExportQueryData();
/*  177 */       data.init(true);
/*  178 */       data.parse(whereClause);
/*      */ 
/*  180 */       Report.trace("archiver", "Export table: " + table, null);
/*  181 */       String lastExport = this.m_archiveData.getLocal("aLastExport");
/*  182 */       doExportTable(table, data, lastExport);
/*      */     }
/*      */ 
/*  185 */     String msg = LocaleUtils.encodeMessage("csArchiverExportTableFinished", null);
/*  186 */     this.m_archiver.reportProgressPercent(msg, -1.0F, -1.0F);
/*  187 */     return binder;
/*      */   }
/*      */ 
/*      */   public void doExportTable(String table, ExportQueryData data, String beginDate)
/*      */     throws ServiceException, DataException
/*      */   {
/*  193 */     this.m_context.setCachedObject("ExportQueryData", data);
/*  194 */     if (doFilter("exportTable") != 0)
/*      */     {
/*  196 */       return;
/*      */     }
/*      */ 
/*  199 */     Date bDate = null;
/*  200 */     if (beginDate != null)
/*      */     {
/*  202 */       bDate = LocaleUtils.parseODBC(beginDate);
/*      */     }
/*  204 */     TableModHistoryUtils.retrieveChangedRows(this.m_workspace, "stores", bDate, true);
/*      */ 
/*  206 */     String archiveDate = LocaleUtils.formatODBC(this.m_archiveDate);
/*  207 */     boolean isLastFile = false;
/*  208 */     int fileSuffix = 1;
/*      */ 
/*  210 */     while (!isLastFile)
/*      */     {
/*  212 */       String endDateForFile = getEndDateForFile(table, data, beginDate, archiveDate);
/*  213 */       String query = data.createExportTableQuery(beginDate, endDateForFile, false);
/*  214 */       Report.trace("archiver", "Export query: " + query, null);
/*  215 */       if (endDateForFile.equals(archiveDate))
/*      */       {
/*  217 */         isLastFile = true;
/*      */       }
/*  219 */       String tableSuffix = "";
/*  220 */       tableSuffix = "_arTable~" + fileSuffix++;
/*  221 */       String batchFile = table + tableSuffix + ".hda";
/*      */ 
/*  223 */       DataBinder binder = createBinderForExport(table, tableSuffix, query, beginDate, endDateForFile, archiveDate, data);
/*      */ 
/*  227 */       int row = DataBinderUtils.getInteger(binder, "numRows", 0);
/*  228 */       if (row > this.m_maxNumExportRows)
/*      */       {
/*  230 */         throw new ServiceException(null, "csTableArchiveMaxRowExceeded", new Object[] { "" + this.m_maxNumExportRows, "" + row, table, query });
/*      */       }
/*      */ 
/*  233 */       boolean allowEmptyResults = (!SharedObjects.getEnvValueAsBoolean("DisableInitialExportOfEmptyTable", false)) && (this.m_archiveData.getLocal("aLastExport") == null);
/*      */ 
/*  235 */       Report.trace("archiver", "Export table to " + batchFile, null);
/*  236 */       if (this.m_st.exportTable(binder, !allowEmptyResults, true))
/*      */       {
/*  239 */         binder.putLocal("archiveDir", this.m_curContentDir);
/*  240 */         binder.putLocal("archiveFileName", batchFile);
/*  241 */         binder.putLocal("exportingTable", table);
/*  242 */         if (doFilter("archiveTablePostExportFilter", binder) != 0) {
/*      */           return;
/*      */         }
/*      */ 
/*  246 */         if ((DataBinderUtils.getBoolean(binder, "deletedRowsEmpty", false)) && (DataBinderUtils.getBoolean(binder, "noFolderAllowed", false)) && (!allowEmptyResults))
/*      */         {
/*  248 */           File d = new File(this.m_curContentDir);
/*  249 */           FileUtils.deleteDirectory(d, true);
/*  250 */           return;
/*      */         }
/*  252 */         Report.trace("archiver", "Update batch file list...", null);
/*  253 */         this.m_binder.putLocal("aNumDocuments", binder.getLocal("numRows"));
/*  254 */         DataResultSet drset = this.m_archiver.getBatchFilesData();
/*  255 */         String fileName = FileUtils.getName(this.m_curContentDir) + "/" + batchFile;
/*  256 */         this.m_archiver.addBatchFile(fileName, drset, false);
/*      */       }
/*  258 */       beginDate = endDateForFile;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String getEndDateForFile(String table, ExportQueryData data, String lastDate, String archiveDate)
/*      */     throws DataException, ServiceException
/*      */   {
/*  266 */     boolean useMultiFile = SharedObjects.getEnvValueAsBoolean("UseMultiFileFormatForTableArchive", true);
/*  267 */     String createTS = data.getQueryProp("aCreateTimeStamp");
/*  268 */     String modifiedTS = data.getQueryProp("aModifiedTimeStamp");
/*  269 */     if ((!useMultiFile) || ((((createTS == null) || (createTS.length() == 0))) && (((modifiedTS == null) || (modifiedTS.length() == 0)))))
/*      */     {
/*  272 */       return archiveDate;
/*      */     }
/*      */ 
/*  275 */     int defaultRows = SharedObjects.getEnvironmentInt("NumRowsPerFileInTableArchive", 10000);
/*      */ 
/*  277 */     boolean foundIt = false;
/*  278 */     String endDate = archiveDate;
/*  279 */     String tmpBeginDate = lastDate;
/*  280 */     int counter = 0;
/*  281 */     while (!foundIt)
/*      */     {
/*  283 */       String query = data.createExportTableQuery(lastDate, endDate, false);
/*  284 */       String[] queries = assembleFullQueries(data, query);
/*  285 */       ResultSet rset = this.m_workspace.createResultSetSQL(queries[1]);
/*  286 */       int count = NumberUtils.parseInteger(rset.getStringValue(0), 0);
/*  287 */       if (SystemUtils.m_verbose)
/*      */       {
/*  289 */         Report.debug("archiver", "getting endDate. beginDate:" + lastDate + ". archiveDate:" + archiveDate + ". endDate:" + endDate + ". #Docs:" + count, null);
/*      */       }
/*      */ 
/*  292 */       if ((count <= defaultRows) && (((count > defaultRows * 0.8D) || (endDate.equals(archiveDate)))))
/*      */       {
/*  294 */         foundIt = true;
/*      */       }
/*      */       else
/*      */       {
/*  298 */         if (counter > 32)
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/*  303 */         ++counter;
/*      */ 
/*  305 */         String tmpDate = endDate;
/*  306 */         if (count > defaultRows)
/*      */         {
/*  308 */           tmpDate = getMidDate(tmpBeginDate, endDate);
/*  309 */           if (tmpDate.equals(endDate))
/*      */           {
/*  311 */             foundIt = true;
/*      */           }
/*      */         } else {
/*  314 */           if (endDate.equals(archiveDate))
/*      */             break;
/*  316 */           tmpDate = getMidDate(endDate, archiveDate);
/*  317 */           tmpBeginDate = endDate;
/*  318 */           if (tmpDate.equals(archiveDate))
/*      */           {
/*  320 */             foundIt = true;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  327 */         endDate = tmpDate;
/*      */       }
/*      */     }
/*  330 */     return endDate;
/*      */   }
/*      */ 
/*      */   protected String getMidDate(String begin, String end) throws ServiceException
/*      */   {
/*  335 */     Date beginDate = null;
/*  336 */     if (begin != null)
/*      */     {
/*  338 */       beginDate = LocaleUtils.parseODBC(begin);
/*      */     }
/*      */     else
/*      */     {
/*  342 */       String defaultBeginDate = SharedObjects.getEnvironmentValue("ArchiveTableDefaultBeginDate");
/*  343 */       if (defaultBeginDate == null)
/*      */       {
/*  345 */         defaultBeginDate = "{ts '1970-01-01 00:00:00.000'}";
/*      */       }
/*  347 */       beginDate = LocaleUtils.parseODBC(defaultBeginDate);
/*      */     }
/*      */ 
/*  350 */     Date endDate = LocaleUtils.parseODBC(end);
/*  351 */     long beginTime = beginDate.getTime();
/*  352 */     long endTime = endDate.getTime();
/*      */ 
/*  354 */     long targetTime = endTime;
/*  355 */     if (endTime - beginTime > 1000L)
/*      */     {
/*  357 */       targetTime = (beginTime + endTime) / 2L;
/*      */     }
/*      */ 
/*  360 */     String target = LocaleUtils.formatODBC(new Date(targetTime));
/*  361 */     if (SystemUtils.m_verbose)
/*      */     {
/*  363 */       Report.debug("archiver", "getting middle Date. beginDate:" + begin + ". endDate:" + end + ". middle:" + target + ". " + "beginTime:" + beginTime + " endTime:" + endTime, null);
/*      */     }
/*      */ 
/*  367 */     return target;
/*      */   }
/*      */ 
/*      */   protected DataBinder createBinderForExport(String table, String tableSuffix, String query, String beginDate, String endDate, String archiveDate, ExportQueryData data)
/*      */     throws DataException, ServiceException
/*      */   {
/*  375 */     DataBinder binder = new DataBinder();
/*      */ 
/*  377 */     String[] fullQueries = assembleFullQueries(data, query);
/*      */ 
/*  380 */     binder.putLocal("fullQueryString", fullQueries[0]);
/*  381 */     binder.putLocal("tableName", table);
/*  382 */     binder.putLocal("isTableArchive", "1");
/*      */ 
/*  384 */     if (this.m_curContentDir == null)
/*      */     {
/*  386 */       this.m_curContentDir = (this.m_archiveExportDir + this.m_archiver.getTSDirName());
/*      */     }
/*  388 */     binder.putLocal("archiveDir", this.m_curContentDir);
/*  389 */     binder.putLocal("archiveFileName", table + tableSuffix + ".hda");
/*  390 */     if (beginDate != null)
/*      */     {
/*  392 */       binder.putLocal("beginDate", beginDate);
/*      */     }
/*  394 */     binder.putLocal("endDate", endDate);
/*  395 */     binder.putLocal("archiveDate", archiveDate);
/*      */ 
/*  398 */     String timeZone = LocaleResources.getSystemTimeZone().getID();
/*  399 */     binder.putLocal("timeZone", timeZone);
/*  400 */     String instanceName = this.m_binder.get("IDC_Name");
/*  401 */     binder.putLocal("instanceName", instanceName);
/*      */ 
/*  404 */     String createTS = data.getQueryProp("aCreateTimeStamp");
/*  405 */     String modifiedTS = data.getQueryProp("aModifiedTimeStamp");
/*      */ 
/*  407 */     boolean useParentTS = StringUtils.convertToBool(data.getQueryProp("aUseParentTS"), false);
/*  408 */     binder.putLocal("useParentTS", "" + useParentTS);
/*  409 */     String[] tsNames = { "createTimeStamp", "modifyTimeStamp" };
/*  410 */     String[] tsValues = { createTS, modifiedTS };
/*      */ 
/*  412 */     addTimeStamps(tsNames, tsValues, binder);
/*  413 */     addTableInfo(table, binder);
/*      */ 
/*  415 */     String isSyncTableStr = data.getQueryProp("aIsCreateNewField");
/*      */ 
/*  417 */     if (StringUtils.convertToBool(isSyncTableStr, false))
/*      */     {
/*  419 */       binder.putLocal("isSyncTableDesign", "true");
/*      */     }
/*      */ 
/*  422 */     String rpcdDelete = data.getQueryProp("aIsReplicateDeletedRows");
/*  423 */     boolean hasDelete = StringUtils.convertToBool(rpcdDelete, false);
/*      */ 
/*  425 */     if (hasDelete)
/*      */     {
/*  427 */       binder.putLocal("isReplicateDeletedRows", "true");
/*  428 */       binder.putLocal("deletedTableName", "DeletedRows");
/*  429 */       String deleteQuery = data.createExportTableQuery(beginDate, endDate, true);
/*  430 */       deleteQuery = "SELECT * FROM DeletedRows WHERE " + deleteQuery;
/*  431 */       binder.putLocal("deletedTableQuery", deleteQuery);
/*      */     }
/*      */ 
/*  434 */     String removeExisting = data.getQueryProp("aRemoveExistingChildren");
/*  435 */     if (removeExisting != null)
/*      */     {
/*  437 */       binder.putLocal("removeExistingChildren", removeExisting);
/*      */     }
/*  439 */     String deleteOnlyWhenNoChild = data.getQueryProp("aDeleteParentOnlyWhenNoChild");
/*  440 */     if (deleteOnlyWhenNoChild != null)
/*      */     {
/*  442 */       binder.putLocal("removeOnlyWhenNoChild", deleteOnlyWhenNoChild);
/*      */     }
/*  444 */     String allowDeleteParents = data.getQueryProp("aAllowDeleteParentRows");
/*  445 */     if (allowDeleteParents != null)
/*      */     {
/*  447 */       binder.putLocal("allowDeleteParentRows", allowDeleteParents);
/*      */     }
/*  449 */     String parentTables = data.getQueryProp("aParentTables");
/*  450 */     if ((parentTables != null) && (parentTables.length() != 0))
/*      */     {
/*  452 */       binder.putLocal("parentTables", parentTables);
/*  453 */       String relations = data.getQueryProp("aTableRelations");
/*  454 */       binder.putLocal("tableRelations", relations);
/*  455 */       Vector tables = StringUtils.parseArray(parentTables, ':', ':');
/*  456 */       tables.addElement(table);
/*  457 */       int size = tables.size();
/*  458 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  460 */         String tableName = (String)tables.elementAt(i);
/*  461 */         addTableInfo(tableName, binder);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  466 */     String useSourceID = data.getQueryProp("aUseSourceID");
/*  467 */     if (useSourceID != null)
/*      */     {
/*  469 */       binder.putLocal("useSourceID", useSourceID);
/*      */     }
/*      */ 
/*  472 */     ResultSet rset = this.m_workspace.createResultSetSQL(fullQueries[1]);
/*  473 */     if (rset.first())
/*      */     {
/*  475 */       String numRows = rset.getStringValue(0);
/*      */ 
/*  477 */       binder.putLocal("numRows", numRows);
/*      */     }
/*  479 */     return binder;
/*      */   }
/*      */ 
/*      */   protected void addTableInfo(String tableName, DataBinder binder)
/*      */     throws DataException
/*      */   {
/*  485 */     FieldInfo[] fi = this.m_workspace.getColumnList(tableName);
/*  486 */     binder.putLocal("numFields" + tableName, "" + fi.length);
/*      */ 
/*  488 */     String[] pk = this.m_workspace.getPrimaryKeys(tableName);
/*      */ 
/*  490 */     String pkStr = StringUtils.createString(StringUtils.convertToVector(pk), ',', ',');
/*      */ 
/*  492 */     binder.putLocal("primaryKeys" + tableName, pkStr);
/*      */   }
/*      */ 
/*      */   protected void addTimeStamps(String[] tsNames, String[] tsValues, DataBinder binder)
/*      */   {
/*  498 */     for (int i = 0; i < tsNames.length; ++i)
/*      */     {
/*  501 */       String label = tsNames[i];
/*  502 */       String value = tsValues[i];
/*      */ 
/*  504 */       if (value == null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  508 */       int index = value.indexOf(46);
/*  509 */       if (index > 0)
/*      */       {
/*  511 */         String cTable = value.substring(0, index);
/*  512 */         binder.putLocal(label + "Table", cTable);
/*  513 */         value = value.substring(index + 1);
/*      */       }
/*  515 */       binder.putLocal(label, value);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String[] assembleFullQueries(ExportQueryData data, String query)
/*      */   {
/*  521 */     String tables = data.getQueryProp("aParentTables");
/*  522 */     String table = data.getQueryProp("aTableName");
/*  523 */     String fullQuery = "";
/*  524 */     if ((tables == null) || (tables.length() == 0))
/*      */     {
/*  526 */       fullQuery = fullQuery + " FROM " + table;
/*  527 */       if ((query != null) && (query.length() != 0))
/*      */       {
/*  529 */         fullQuery = fullQuery + " WHERE ";
/*  530 */         fullQuery = fullQuery + query;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  535 */       String relStr = data.getQueryProp("aTableRelations");
/*  536 */       fullQuery = ArchiveTableUtils.constructArchiveQueryFragment(table, tables, relStr, query);
/*      */     }
/*      */ 
/*  540 */     String[] queries = new String[2];
/*  541 */     queries[0] = ("SELECT * " + fullQuery);
/*  542 */     queries[1] = ("SELECT count(*) " + fullQuery);
/*  543 */     return queries;
/*      */   }
/*      */ 
/*      */   public void doImportTableEntries() throws ServiceException, DataException
/*      */   {
/*  548 */     DataBinder binder = new DataBinder();
/*  549 */     String batchFile = this.m_binder.getLocal("aBatchFile");
/*  550 */     if (DataBinderUtils.getBoolean(binder, "isReadBatchMeta", true))
/*      */     {
/*  552 */       String filePath = this.m_archiveExportDir + batchFile;
/*  553 */       File batch = new File(filePath);
/*  554 */       if (!batch.exists())
/*      */       {
/*  556 */         throw new ServiceException(LocaleUtils.encodeMessage("csArchiverTableFileNotExist", null, filePath));
/*      */       }
/*      */ 
/*  560 */       BufferedReader reader = null;
/*      */       try
/*      */       {
/*  563 */         reader = new BufferedReader(new FileReader(batch));
/*  564 */         binder.receiveEx(reader, true);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*      */       }
/*      */       finally
/*      */       {
/*  573 */         FileUtils.closeObject(reader);
/*      */       }
/*      */     }
/*  576 */     binder.setEnvironment(this.m_binder.getEnvironment());
/*  577 */     binder.addResultSet("ExportResults", this.m_binder.getResultSet("ExportResults"));
/*  578 */     doImportTableEx(binder, batchFile);
/*      */   }
/*      */ 
/*      */   public void doImportTables(DataResultSet drset) throws ServiceException, DataException
/*      */   {
/*  583 */     ResultSetFilter filter = drset.createSimpleResultSetFilter("1");
/*  584 */     DataResultSet batchTables = new DataResultSet();
/*  585 */     batchTables.copyFiltered(drset, "aIsTableBatch", filter);
/*      */ 
/*  587 */     int index = ResultSetUtils.getIndexMustExist(batchTables, "aBatchFile");
/*  588 */     int size = batchTables.getNumRows();
/*  589 */     String msgImportingTables = LocaleUtils.encodeMessage("csArchiverImportingTables", null);
/*  590 */     if (size > 0)
/*      */     {
/*  592 */       this.m_archiver.reportProgressPercent(msgImportingTables, 0.0F, size);
/*      */     }
/*      */ 
/*  595 */     for (batchTables.first(); batchTables.isRowPresent(); batchTables.next())
/*      */     {
/*  597 */       String file = batchTables.getStringValue(index);
/*      */       try
/*      */       {
/*  600 */         Report.trace("archiver", "Import table from batch: " + file, null);
/*  601 */         doImportTable(file);
/*      */ 
/*  603 */         if (this.m_archiver.m_isAuto)
/*      */         {
/*  605 */           this.m_archiver.cleanUpAfterImport(file);
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  610 */         Report.trace("archiver", null, e);
/*  611 */         String msg = LocaleUtils.encodeMessage("csArchiverTableUnableToImport", null, file);
/*  612 */         this.m_archiver.reportError(e, msg);
/*      */       }
/*      */       finally
/*      */       {
/*  617 */         notifySubjects();
/*      */       }
/*  619 */       this.m_archiver.reportProgressPercent(msgImportingTables, batchTables.getCurrentRow(), size);
/*      */     }
/*      */ 
/*  623 */     if (size <= 0)
/*      */       return;
/*  625 */     doFilter("postImportTables");
/*      */   }
/*      */ 
/*      */   public boolean doImportTable(String batchName)
/*      */     throws ServiceException, DataException
/*      */   {
/*  631 */     Report.trace("archiver", "Import table from batch: " + batchName, null);
/*  632 */     boolean result = false;
/*  633 */     String path = this.m_archiveExportDir + batchName;
/*  634 */     File archiveFile = new File(path);
/*  635 */     if (!archiveFile.exists())
/*      */     {
/*  637 */       Report.trace("archiver", "Archive batch file: " + path + " not exists.", null);
/*  638 */       throw new ServiceException(LocaleUtils.encodeMessage("csArchiverTableFileNotExist", null, path));
/*      */     }
/*      */ 
/*  642 */     DataBinder binder = new DataBinder();
/*      */     try
/*      */     {
/*  645 */       binder = ResourceUtils.readDataBinderFromPath(path);
/*  646 */       binder.setEnvironment(this.m_binder.getEnvironment());
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  650 */       throw new ServiceException(LocaleUtils.encodeMessage("csArchiverTableUnableToReadBatchFile", null, batchName));
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  656 */       result = doImportTableEx(binder, batchName);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  660 */       SystemUtils.dumpException("archiver", e);
/*  661 */       String msg = LocaleUtils.encodeMessage("csArchiverTableUnableToImport", null, batchName);
/*      */ 
/*  663 */       this.m_archiver.reportError(e, msg);
/*      */     }
/*      */     finally
/*      */     {
/*  668 */       notifySubjects();
/*      */     }
/*  670 */     return result;
/*      */   }
/*      */ 
/*      */   public boolean doImportTableEx(DataBinder binder, String batchName)
/*      */     throws ServiceException, DataException
/*      */   {
/*  676 */     int code = doFilter("importTable", binder);
/*  677 */     if (code != 0)
/*      */     {
/*  679 */       return code == 1;
/*      */     }
/*      */ 
/*  682 */     String baseTable = binder.getLocal("tableName");
/*  683 */     loadImportParameters(baseTable, binder);
/*      */ 
/*  685 */     String parentTableStr = binder.getLocal("parentTables");
/*  686 */     boolean hasParentTable = parentTableStr != null;
/*  687 */     Vector tables = StringUtils.parseArray(parentTableStr, ',', '^');
/*  688 */     String relations = binder.getLocal("tableRelations");
/*  689 */     Vector relationVect = StringUtils.parseArray(relations, ',', '^');
/*  690 */     tables.insertElementAt(baseTable, 0);
/*  691 */     prepareImportOverride(baseTable, binder);
/*  692 */     checkAndValidateTableDesign(tables, binder);
/*      */ 
/*  696 */     int size = tables.size();
/*  697 */     boolean useParentTS = DataBinderUtils.getBoolean(binder, "useParentTS", false);
/*  698 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  700 */       boolean hasImportedRow = false;
/*  701 */       boolean hasPrev = (hasParentTable) && (i > 0);
/*  702 */       String prevTable = null;
/*  703 */       int index = i;
/*      */ 
/*  705 */       if (useParentTS)
/*      */       {
/*  707 */         index = size - i - 1;
/*      */       }
/*  709 */       if (hasPrev)
/*      */       {
/*  711 */         if (useParentTS)
/*      */         {
/*  713 */           prevTable = (String)tables.elementAt(index + 1);
/*      */         }
/*      */         else
/*      */         {
/*  717 */           prevTable = (String)tables.elementAt(index - 1);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  722 */       boolean hasNext = (hasParentTable) && (i < size - 1);
/*  723 */       String nextTable = null;
/*  724 */       if (hasNext)
/*      */       {
/*  726 */         if (useParentTS)
/*      */         {
/*  728 */           nextTable = (String)tables.elementAt(index - 1);
/*      */         }
/*      */         else
/*      */         {
/*  732 */           nextTable = (String)tables.elementAt(index + 1);
/*      */         }
/*      */       }
/*  735 */       String table = (String)tables.elementAt(index);
/*      */ 
/*  737 */       DataBinder tableBinder = binder.createShallowCopy();
/*  738 */       tableBinder.setLocalData(new Properties(binder.getLocalData()));
/*      */       try
/*      */       {
/*  742 */         preprocessArchive(table, tableBinder, baseTable, hasParentTable, hasPrev, prevTable);
/*  743 */         ImportTableDataResultSet drset = (ImportTableDataResultSet)tableBinder.getResultSet("ExportResults" + table);
/*      */ 
/*  745 */         String constraintKeys = getConstraintString(table, tableBinder);
/*  746 */         String[] timeStamps = findTimeStampColumns(table, tableBinder, new String[] { "createTimeStamp", "modifyTimeStamp" }, true);
/*      */ 
/*  749 */         String srcID = retrieveSourceID(table, baseTable, tableBinder);
/*      */ 
/*  751 */         String filter = getAdditionalFilter(table, tableBinder);
/*  752 */         DataBinder mapBinder = new DataBinder(tableBinder.getEnvironment());
/*  753 */         mapBinder.setLocalData(tableBinder.getLocalData());
/*  754 */         mapBinder.addResultSet("ImportingRows", drset);
/*  755 */         ValueMapData map = createValueMap(table, mapBinder);
/*      */ 
/*  757 */         ArchiveTableCallBack callback = getArchiveTableCallBack(table, constraintKeys, srcID, timeStamps, tableBinder);
/*      */ 
/*  760 */         if (constraintKeys != null)
/*      */         {
/*  762 */           tableBinder.putLocal("constraintKeys", constraintKeys);
/*      */         }
/*  764 */         if (srcID != null)
/*      */         {
/*  766 */           tableBinder.putLocal("sourceID", srcID);
/*      */         }
/*      */ 
/*  769 */         this.m_st.importTable(table, drset, constraintKeys, filter, map, callback, false);
/*      */ 
/*  772 */         String modCol = null;
/*  773 */         if (timeStamps != null)
/*      */         {
/*  775 */           int numTimeStamps = timeStamps.length;
/*  776 */           if (numTimeStamps > 1)
/*      */           {
/*  778 */             modCol = timeStamps[1];
/*      */           }
/*  780 */           if ((numTimeStamps > 0) && (modCol == null))
/*      */           {
/*  782 */             modCol = timeStamps[0];
/*      */           }
/*      */         }
/*  785 */         if (!this.m_isAllowRegisterMod)
/*      */         {
/*  787 */           recordImportChanges(table, drset, srcID, constraintKeys, modCol);
/*      */         }
/*  789 */         hasImportedRow = drset.firstImportedRow();
/*  790 */         drset = (ImportTableDataResultSet)tableBinder.getResultSet("DeletedRows" + table);
/*      */ 
/*  792 */         boolean allowDeleteParentRow = DataBinderUtils.getBoolean(tableBinder, "allowDeleteParentRows", true);
/*  793 */         boolean removeOnlyWhenNoChild = DataBinderUtils.getBoolean(tableBinder, "removeOnlyWhenNoChild", true);
/*      */ 
/*  796 */         boolean allowDeleteRow = false;
/*  797 */         boolean allowDeleteWithoutTS = DataBinderUtils.getBoolean(tableBinder, "AllowArchiveDeleteWithoutTimestamp", false);
/*  798 */         boolean allowReplicateDelete = DataBinderUtils.getBoolean(tableBinder, "isReplicateDeletedRows", false);
/*  799 */         if ((allowReplicateDelete) && (((timeStamps != null) || (allowDeleteWithoutTS))))
/*      */         {
/*  801 */           allowDeleteRow = true;
/*  802 */           Report.trace("archiver", "Allow Deleted Rows Replication.", null);
/*      */         }
/*      */ 
/*  805 */         if ((drset != null) && (allowDeleteRow) && ((
/*  807 */           (baseTable.equalsIgnoreCase(table)) || (allowDeleteParentRow))))
/*      */         {
/*  809 */           callback = getArchiveTableCallBack(table, constraintKeys, srcID, timeStamps, null);
/*  810 */           if ((removeOnlyWhenNoChild) && (!baseTable.equalsIgnoreCase(table)))
/*      */           {
/*  813 */             String rel = (String)relationVect.elementAt(index - 1);
/*  814 */             prepareCallbackFilter(callback, rel, table);
/*      */           }
/*  816 */           this.m_st.importTable(table, drset, constraintKeys, null, map, callback, true);
/*  817 */           if (!hasImportedRow)
/*      */           {
/*  819 */             hasImportedRow = drset.firstImportedRow();
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  824 */         postprocessBatch(table, prevTable, nextTable, tableBinder, map, callback);
/*      */       }
/*      */       finally
/*      */       {
/*  828 */         if (hasImportedRow)
/*      */         {
/*  830 */           addChangedTable(table);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  835 */     doFilter("postImportTable", binder);
/*  836 */     return true;
/*      */   }
/*      */ 
/*      */   protected void loadImportParameters(String table, DataBinder binder)
/*      */   {
/*  841 */     String importData = this.m_archiveData.getLocal("aImportTable" + table);
/*  842 */     if (importData == null)
/*      */     {
/*  844 */       return;
/*      */     }
/*  846 */     ExportQueryData data = new ExportQueryData();
/*  847 */     data.init(true);
/*  848 */     data.parse(importData);
/*  849 */     for (int i = 0; i < KEYS.length; ++i)
/*      */     {
/*  851 */       String value = data.getQueryProp(KEYS[i][0]);
/*  852 */       if (value == null)
/*      */         continue;
/*  854 */       binder.putLocal(KEYS[i][1], value);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected ArchiveTableCallBack getArchiveTableCallBack(String table, String constraintKeys, String srcID, String[] timestamps, DataBinder tableBinder)
/*      */     throws ServiceException
/*      */   {
/*  862 */     ArchiveTableCallBack callback = (ArchiveTableCallBack)ComponentClassFactory.createClassInstance("ArchiveTableCallBack", "intradoc.server.archive.ArchiveTableCallBack", "csArchiverTableHelperErrorInstantiateCallBack");
/*      */ 
/*  867 */     callback.init(table, timestamps, srcID, this.m_context, this.m_workspace, this.m_hasLimitedTimeResolution);
/*      */ 
/*  870 */     return callback;
/*      */   }
/*      */ 
/*      */   protected void prepareCallbackFilter(ArchiveTableCallBack callback, String relation, String table)
/*      */   {
/*  875 */     Vector v = StringUtils.parseArray(relation, '=', '^');
/*  876 */     Vector row = new IdcVector();
/*      */ 
/*  878 */     int size = v.size();
/*  879 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  881 */       String value = (String)v.elementAt(i);
/*      */ 
/*  883 */       if (value.toLowerCase().startsWith(table.toLowerCase()))
/*      */       {
/*  885 */         row.insertElementAt(value, 0);
/*      */       }
/*      */       else
/*      */       {
/*  889 */         row.addElement(value);
/*      */       }
/*      */     }
/*  892 */     callback.addFilter(row);
/*      */   }
/*      */ 
/*      */   protected String retrieveSourceID(String table, String baseTable, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  898 */     if (!DataBinderUtils.getBoolean(binder, "useSourceID", false))
/*      */     {
/*  900 */       return null;
/*      */     }
/*  902 */     String srcID = binder.getAllowMissing("ArchiveSourcIDColName");
/*  903 */     if (srcID == null)
/*      */     {
/*  905 */       srcID = "schSourceID";
/*      */     }
/*  907 */     ResultSet rset = binder.getResultSet("ExportResults" + table);
/*  908 */     FieldInfo fi = new FieldInfo();
/*  909 */     if (rset != null)
/*      */     {
/*  911 */       rset.getFieldInfo(srcID, fi);
/*      */     }
/*      */ 
/*  915 */     if (fi.m_index < 0)
/*      */     {
/*  917 */       if (table.equalsIgnoreCase(baseTable))
/*      */       {
/*  919 */         throw new ServiceException("csArchiverSourceIDNotExist");
/*      */       }
/*  921 */       srcID = null;
/*      */     }
/*  923 */     return srcID;
/*      */   }
/*      */ 
/*      */   protected void preprocessArchive(String tableName, DataBinder binder, String baseTable, boolean isMultiTable, boolean hasChild, String childTable)
/*      */     throws ServiceException
/*      */   {
/*  929 */     Report.trace("archiver", "Prepare import archive.", null);
/*  930 */     DataResultSet exportResults = (DataResultSet)binder.getResultSet("ExportResults");
/*  931 */     if (hasChild)
/*      */     {
/*  933 */       preprocessExportResults(exportResults, binder, childTable);
/*      */     }
/*      */ 
/*  936 */     DataResultSet deleteTable = null;
/*  937 */     String deleteTS = "dDeleteDate";
/*  938 */     if (DataBinderUtils.getBoolean(binder, "isReplicateDeletedRows", false))
/*      */     {
/*  940 */       deleteTable = (DataResultSet)binder.getResultSet("DeletedRows");
/*      */     }
/*      */ 
/*  943 */     boolean isTranslateDate = DataBinderUtils.getBoolean(binder, "isTranslateDate", false);
/*  944 */     String oldTimeZoneStr = binder.getLocal("timeZone");
/*  945 */     TimeZone oldTimeZone = LocaleResources.m_systemTimeZoneFormat.parseTimeZone(null, oldTimeZoneStr, 0);
/*  946 */     TimeZone newTimeZone = LocaleResources.getSystemTimeZone();
/*  947 */     String rsetName = "ExportResults" + tableName;
/*  948 */     Report.trace("archiver", "Assemble resultset " + rsetName, null);
/*  949 */     DataResultSet modifyingResults = exportResults;
/*      */ 
/*  951 */     int beginIndex = getTableFieldBeginIndex(tableName, binder);
/*  952 */     int numFields = getTableNumField(tableName, binder);
/*  953 */     ImportTableDataResultSet cdrset = new ImportTableDataResultSet();
/*  954 */     if (isTranslateDate)
/*      */     {
/*  956 */       cdrset.init(oldTimeZone, newTimeZone);
/*      */     }
/*  958 */     cdrset.copyWithFilteredColumn(exportResults, beginIndex, numFields, true);
/*  959 */     modifyingResults = cdrset;
/*      */ 
/*  963 */     if (baseTable.equals(tableName))
/*      */     {
/*  965 */       processFieldMapping(tableName, modifyingResults, binder);
/*      */     }
/*  967 */     binder.addResultSet(rsetName, modifyingResults);
/*      */ 
/*  969 */     if (deleteTable == null)
/*      */       return;
/*  971 */     Report.trace("archiver", "Assemble resultset DeletedRows " + tableName, null);
/*  972 */     cdrset = new ImportTableDataResultSet();
/*  973 */     if (isTranslateDate)
/*      */     {
/*  975 */       cdrset.init(oldTimeZone, newTimeZone);
/*      */     }
/*      */ 
/*  978 */     String[] timeStamps = findTimeStampColumns(tableName, binder, new String[] { "createTimeStamp", "modifyTimeStamp" }, false);
/*      */ 
/*  981 */     String srcID = retrieveSourceID(tableName, baseTable, binder);
/*  982 */     cdrset.copyDeleteTable(deleteTable, tableName, deleteTS, timeStamps[0], timeStamps[1], srcID, modifyingResults);
/*      */ 
/*  984 */     binder.addResultSet("DeletedRows" + tableName, cdrset);
/*      */   }
/*      */ 
/*      */   protected void prepareImportOverride(String tableName, DataBinder binder)
/*      */   {
/*  990 */     Report.trace("archiver", "Prepare import configuration override.", null);
/*  991 */     String query = this.m_archiveData.getActiveAllowMissing("aImportTable" + tableName);
/*  992 */     if (query == null)
/*      */     {
/*  994 */       query = this.m_archiveData.getActiveAllowMissing("aImportTable");
/*      */     }
/*  996 */     if (query != null)
/*      */     {
/*  998 */       ExportQueryData data = new ExportQueryData();
/*  999 */       data.parse(query);
/* 1000 */       String[][] map = KEYS;
/*      */ 
/* 1002 */       for (int i = 0; i < map.length; ++i)
/*      */       {
/* 1004 */         String value = data.getQueryProp(map[i][0]);
/* 1005 */         if (value == null)
/*      */           continue;
/* 1007 */         binder.putLocal(map[i][1], value);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1012 */     boolean isTranslateDate = DataBinderUtils.getBoolean(this.m_archiveData, "aTranslateDate", false);
/* 1013 */     if (!isTranslateDate)
/*      */       return;
/* 1015 */     binder.putLocal("isTranslateDate", "true");
/*      */   }
/*      */ 
/*      */   protected void preprocessExportResults(DataResultSet drset, DataBinder binder, String prevTable)
/*      */   {
/* 1021 */     if (prevTable == null)
/*      */     {
/* 1023 */       return;
/*      */     }
/*      */ 
/* 1026 */     Report.trace("archiver", "Preprocess import resultset", null);
/* 1027 */     ResultSet prevRset = binder.getResultSet("ExportResults" + prevTable);
/* 1028 */     String doFilterStr = binder.getAllowMissing("ArchiverAlwaysImportParentRow");
/* 1029 */     boolean alwaysImport = StringUtils.convertToBool(doFilterStr, false);
/* 1030 */     if ((alwaysImport) || (!prevRset instanceof ImportTableDataResultSet))
/*      */     {
/* 1032 */       return;
/*      */     }
/* 1034 */     ImportTableDataResultSet parsetDrset = (ImportTableDataResultSet)prevRset;
/* 1035 */     int[] skippedIndex = parsetDrset.retrieveParentIndexesOfSkippedRows();
/* 1036 */     for (int i = skippedIndex.length - 1; i >= 0; --i)
/*      */     {
/* 1038 */       drset.setCurrentRow(skippedIndex[i]);
/* 1039 */       drset.deleteCurrentRow();
/*      */     }
/* 1041 */     Report.trace("archiver", "Removed " + skippedIndex.length + " items from result set", null);
/*      */   }
/*      */ 
/*      */   protected int getTableFieldBeginIndex(String table, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 1048 */     String baseTable = binder.getLocal("tableName");
/* 1049 */     if (baseTable.equalsIgnoreCase(table))
/*      */     {
/* 1051 */       return 0;
/*      */     }
/*      */ 
/* 1054 */     String parentTables = binder.getLocal("parentTables");
/* 1055 */     Vector tableList = StringUtils.parseArray(parentTables, ',', ',');
/* 1056 */     tableList.insertElementAt(baseTable, 0);
/*      */ 
/* 1058 */     int size = tableList.size();
/* 1059 */     int index = 0;
/* 1060 */     for (int i = 0; i < size; ++i) {
/* 1061 */       String tableName = (String)tableList.elementAt(i);
/* 1062 */       if (tableName.equalsIgnoreCase(table)) {
/*      */         break;
/*      */       }
/*      */ 
/* 1066 */       int numField = getTableNumField(tableName, binder);
/*      */ 
/* 1069 */       index += numField;
/*      */     }
/* 1071 */     return index;
/*      */   }
/*      */ 
/*      */   protected int getTableNumField(String tableName, DataBinder binder) throws ServiceException
/*      */   {
/* 1076 */     String param = "numFields" + tableName;
/*      */ 
/* 1078 */     int numField = DataBinderUtils.getInteger(binder, param, -1);
/* 1079 */     if (numField == -1)
/*      */     {
/* 1081 */       throw new ServiceException(LocaleUtils.encodeMessage("csArchiverTableUndefinedNumField", null, param));
/*      */     }
/*      */ 
/* 1084 */     return numField;
/*      */   }
/*      */ 
/*      */   protected void processFieldMapping(String tableName, DataResultSet drset, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 1090 */     Report.trace("archiver", "Process field mapping for table: " + tableName, null);
/* 1091 */     String fieldMap = this.m_archiveData.getLocal("aFieldMapsTable" + tableName);
/* 1092 */     if (fieldMap == null)
/*      */       return;
/* 1094 */     ClausesData data = new ClausesData();
/* 1095 */     data.parse(fieldMap);
/*      */ 
/* 1097 */     int[][] map = new int[data.m_clauses.size()][2];
/* 1098 */     boolean validated = validateFieldMap(binder, drset, data, map);
/* 1099 */     if (!validated)
/*      */       return;
/* 1101 */     DataResultSet exportResults = (DataResultSet)binder.getResultSet("ExportResults");
/* 1102 */     boolean rsetMismatch = false;
/* 1103 */     if (exportResults.getNumRows() == drset.getNumRows())
/*      */     {
/* 1105 */       rsetMismatch = true;
/*      */     }
/* 1107 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1109 */       Vector row = drset.getCurrentRowValues();
/*      */ 
/* 1111 */       int index = drset.getCurrentRow();
/* 1112 */       if (rsetMismatch)
/*      */       {
/* 1114 */         int[] array = ((ImportTableDataResultSet)drset).retrieveParentIndexesOfCurrentRow();
/*      */ 
/* 1117 */         index = array[(array.length - 1)];
/*      */       }
/* 1119 */       exportResults.setCurrentRow(index);
/* 1120 */       Vector srcRow = exportResults.getCurrentRowValues();
/* 1121 */       for (int i = 0; i < map.length; ++i)
/*      */       {
/* 1123 */         if (map[i][0] == -1) continue; if (map[i][1] == -1) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1127 */         String from = (String)srcRow.elementAt(map[i][0]);
/* 1128 */         row.setElementAt(from, map[i][1]);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean validateFieldMap(DataBinder binder, DataResultSet drset, ClausesData data, int[][] map)
/*      */     throws ServiceException
/*      */   {
/* 1138 */     if ((map.length == 0) || (drset.getNumRows() == 0))
/*      */     {
/* 1140 */       return false;
/*      */     }
/*      */ 
/* 1143 */     for (int i = 0; i < map.length; ++i)
/*      */     {
/* 1145 */       Vector elt = (Vector)data.m_clauses.elementAt(i);
/* 1146 */       String origField = (String)elt.elementAt(0);
/* 1147 */       String tgtField = (String)elt.elementAt(1);
/*      */ 
/* 1149 */       map[i] = { -1, -1 };
/* 1150 */       FieldInfo fi = new FieldInfo();
/* 1151 */       if (getSourceFieldInfo(origField, binder, fi))
/*      */       {
/* 1153 */         map[i][0] = fi.m_index;
/*      */       }
/* 1155 */       if (!drset.getFieldInfo(tgtField, fi))
/*      */         continue;
/* 1157 */       map[i][1] = fi.m_index;
/*      */     }
/*      */ 
/* 1161 */     return true;
/*      */   }
/*      */ 
/*      */   protected boolean getSourceFieldInfo(String fieldName, DataBinder binder, FieldInfo fi)
/*      */     throws ServiceException
/*      */   {
/* 1167 */     String tableName = "";
/* 1168 */     int index = fieldName.indexOf(46);
/* 1169 */     if (index != -1)
/*      */     {
/* 1171 */       tableName = fieldName.substring(0, index);
/* 1172 */       fieldName = fieldName.substring(index + 1);
/*      */     }
/*      */     else
/*      */     {
/* 1176 */       tableName = binder.getLocal("tableName");
/*      */     }
/*      */ 
/* 1179 */     DataResultSet drset = (DataResultSet)binder.getResultSet("ExportResults");
/* 1180 */     index = getTableFieldBeginIndex(tableName, binder);
/* 1181 */     int defaultLen = 0;
/* 1182 */     if (index == 0)
/*      */     {
/* 1184 */       defaultLen = drset.getNumFields();
/*      */     }
/*      */ 
/* 1187 */     int numFields = NumberUtils.parseInteger(binder.getLocal("numFields" + tableName), defaultLen);
/* 1188 */     boolean foundIt = false;
/* 1189 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/* 1191 */       String name = drset.getFieldName(index + i);
/* 1192 */       if (!name.equals(fieldName))
/*      */         continue;
/* 1194 */       drset.getIndexFieldInfo(index + i, fi);
/* 1195 */       foundIt = true;
/* 1196 */       break;
/*      */     }
/*      */ 
/* 1199 */     return foundIt;
/*      */   }
/*      */ 
/*      */   protected String[] findTimeStampColumns(String table, DataBinder binder, String[] names, boolean isRemoveNull)
/*      */   {
/* 1211 */     if (names == null)
/*      */     {
/* 1213 */       return null;
/*      */     }
/* 1215 */     String[] tsCols = new String[names.length];
/* 1216 */     Vector tsList = new IdcVector();
/* 1217 */     for (int i = 0; i < names.length; ++i)
/*      */     {
/* 1219 */       String colName = null;
/* 1220 */       if (table != null)
/*      */       {
/* 1222 */         colName = binder.getLocal(names[i]);
/*      */       }
/* 1224 */       if (colName != null)
/*      */       {
/* 1226 */         String colTable = binder.getLocal(names[i] + "Table");
/* 1227 */         if (colTable != null)
/*      */         {
/* 1229 */           if (!colTable.equals(table))
/*      */           {
/* 1231 */             colName = null;
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1236 */           boolean useTSOnArchiveTableOnly = SharedObjects.getEnvValueAsBoolean("ArchiveUseTSOnOrigTableOnly", false);
/*      */ 
/* 1238 */           if (useTSOnArchiveTableOnly)
/*      */           {
/* 1240 */             String origTable = binder.getLocal("tableName");
/* 1241 */             if ((origTable == null) || (!origTable.equals(table)))
/*      */             {
/* 1243 */               colName = null;
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/* 1248 */             ResultSet rset = binder.getResultSet("ExportResults" + table);
/* 1249 */             if (rset == null) {
/*      */               continue;
/*      */             }
/*      */ 
/* 1253 */             String tsOverride = binder.getAllowMissing(names[i] + table);
/* 1254 */             if ((tsOverride != null) && (tsOverride.length() != 0))
/*      */             {
/* 1256 */               colName = tsOverride;
/*      */             }
/* 1258 */             FieldInfo fi = new FieldInfo();
/* 1259 */             rset.getFieldInfo(colName, fi);
/* 1260 */             if (fi.m_index == -1)
/*      */             {
/* 1262 */               colName = null;
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/* 1267 */       tsCols[i] = colName;
/* 1268 */       if (colName == null)
/*      */         continue;
/* 1270 */       tsList.addElement(colName);
/*      */     }
/*      */ 
/* 1274 */     if (isRemoveNull)
/*      */     {
/* 1276 */       tsCols = null;
/* 1277 */       int size = tsList.size();
/* 1278 */       if (size > 0)
/*      */       {
/* 1280 */         tsCols = new String[size];
/* 1281 */         for (int i = 0; i < size; ++i)
/*      */         {
/* 1283 */           tsCols[i] = ((String)tsList.elementAt(i));
/*      */         }
/*      */       }
/*      */     }
/* 1287 */     return tsCols;
/*      */   }
/*      */ 
/*      */   protected boolean checkAndValidateTableDesign(Vector tableList, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 1294 */     int size = tableList.size();
/*      */ 
/* 1296 */     DataResultSet drset = (DataResultSet)binder.getResultSet("ExportResults");
/*      */ 
/* 1298 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1300 */       String table = (String)tableList.elementAt(i);
/* 1301 */       int beginIndex = getTableFieldBeginIndex(table, binder);
/* 1302 */       int numField = DataBinderUtils.getInteger(binder, "numFields" + table, -1);
/* 1303 */       DataResultSet rset = new DataResultSet();
/* 1304 */       Vector fieldInfos = new IdcVector();
/* 1305 */       for (int j = 0; j < numField; ++j)
/*      */       {
/* 1307 */         FieldInfo fi = new FieldInfo();
/* 1308 */         drset.getIndexFieldInfo(j + beginIndex, fi);
/* 1309 */         fieldInfos.addElement(fi);
/*      */       }
/* 1311 */       rset.mergeFieldsWithFlags(fieldInfos, 0);
/* 1312 */       if (!checkAndValidateTableDesign(table, binder, rset))
/*      */       {
/* 1314 */         return false;
/*      */       }
/*      */     }
/*      */ 
/* 1318 */     return true;
/*      */   }
/*      */ 
/*      */   protected boolean checkAndValidateTableDesign(String tableName, DataBinder binder, DataResultSet drset)
/*      */     throws ServiceException
/*      */   {
/* 1324 */     boolean isSyncTableDesign = DataBinderUtils.getBoolean(binder, "isSyncTableDesign", false);
/* 1325 */     boolean allowDeleteTableFields = DataBinderUtils.getBoolean(binder, "AllowArchiverDeleteTableFields", false);
/* 1326 */     Properties ignoreFields = getIgnoredFields(binder);
/* 1327 */     Hashtable diff = getFieldDiff(tableName, drset, ignoreFields);
/* 1328 */     if (!allowDeleteTableFields)
/*      */     {
/* 1330 */       diff.remove("deleted");
/*      */     }
/* 1332 */     if (diff.get("createNewTable") == null)
/*      */     {
/* 1335 */       validatePrimaryKeys(tableName, binder);
/*      */     }
/* 1337 */     else if (isSyncTableDesign)
/*      */     {
/* 1339 */       Report.trace("archiver", "Prepare to create new table.", null);
/* 1340 */       String primaryKeys = binder.getLocal("primaryKeys" + tableName);
/* 1341 */       String[] keyArray = StringUtils.convertListToArray(StringUtils.parseArray(primaryKeys, ',', ','));
/*      */ 
/* 1344 */       int size = drset.getNumFields();
/* 1345 */       FieldInfo[] infos = new FieldInfo[size];
/* 1346 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 1348 */         infos[i] = new FieldInfo();
/* 1349 */         drset.getIndexFieldInfo(i, infos[i]);
/*      */       }
/* 1351 */       diff.put("primaryKeys", keyArray);
/* 1352 */       diff.put("fieldInfos", infos);
/*      */     }
/*      */     else
/*      */     {
/* 1356 */       throw new ServiceException(LocaleUtils.encodeMessage("csArchiverTableNotExist", null, tableName));
/*      */     }
/*      */ 
/* 1359 */     if (isSyncTableDesign)
/*      */     {
/* 1361 */       return updateTableDesign(tableName, diff);
/*      */     }
/* 1363 */     Object add = diff.get("add");
/* 1364 */     return add == null;
/*      */   }
/*      */ 
/*      */   protected Properties getIgnoredFields(DataBinder binder)
/*      */   {
/* 1369 */     Properties prop = new Properties();
/* 1370 */     String fieldsStr = binder.getAllowMissing("TableArchiveIgnoreFields");
/* 1371 */     if (fieldsStr == null)
/*      */     {
/* 1373 */       fieldsStr = SharedObjects.getEnvironmentValue("TableArchiveIgnoreFields");
/* 1374 */       if ((fieldsStr == null) || (fieldsStr.length() == 0))
/*      */       {
/* 1379 */         fieldsStr = "folderArchiveFolderPath,folderArchiveGUIDPath,folderArchiveCollectionPath";
/*      */       }
/*      */     }
/* 1382 */     Vector fields = StringUtils.parseArray(fieldsStr, ',', '^');
/* 1383 */     int size = fields.size();
/* 1384 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1386 */       String field = (String)fields.elementAt(i);
/* 1387 */       field = field.trim().toLowerCase();
/* 1388 */       prop.put(field, "1");
/*      */     }
/* 1390 */     return prop;
/*      */   }
/*      */ 
/*      */   protected Hashtable getFieldDiff(String tableName, DataResultSet drset, Properties ignoredFields)
/*      */     throws ServiceException
/*      */   {
/* 1396 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1398 */       Report.debug("archiver", "Checking for DB design on table " + tableName, null);
/*      */     }
/* 1400 */     FieldInfo[] curInfos = new FieldInfo[0];
/* 1401 */     Hashtable diff = new Hashtable();
/*      */     try
/*      */     {
/* 1404 */       curInfos = this.m_workspace.getColumnList(tableName);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1410 */       diff.put("createNewTable", "1");
/*      */     }
/* 1412 */     Hashtable oldFields = new Hashtable();
/* 1413 */     for (int i = 0; i < curInfos.length; ++i)
/*      */     {
/* 1415 */       String key = curInfos[i].m_name.toLowerCase();
/* 1416 */       if (ignoredFields.getProperty(key) != null) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1420 */       oldFields.put(key, curInfos[i]);
/*      */     }
/*      */ 
/* 1423 */     int size = drset.getNumFields();
/* 1424 */     Vector addFields = new IdcVector();
/* 1425 */     Vector deletedFields = new IdcVector();
/* 1426 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1428 */       FieldInfo fi = new FieldInfo();
/* 1429 */       drset.getIndexFieldInfo(i, fi);
/* 1430 */       String key = fi.m_name.toLowerCase();
/* 1431 */       FieldInfo ofi = (FieldInfo)oldFields.remove(key);
/* 1432 */       if (ofi == null)
/*      */       {
/* 1434 */         if (ignoredFields.getProperty(key) != null)
/*      */           continue;
/* 1436 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1438 */           Report.debug("archiver", "Field " + fi.m_name + " does not exist in target system.", null);
/*      */         }
/* 1440 */         addFields.addElement(fi);
/*      */       }
/*      */       else
/*      */       {
/* 1445 */         if ((ofi.m_maxLen >= fi.m_maxLen) || ((fi.m_type != 6) && (fi.m_type != 2))) {
/*      */           continue;
/*      */         }
/* 1448 */         addFields.addElement(fi);
/* 1449 */         if (!SystemUtils.m_verbose)
/*      */           continue;
/* 1451 */         Report.debug("archiver", "Field " + fi.m_name + " needs to be modified to " + fi.m_maxLen + " character long", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1457 */     for (Enumeration en = oldFields.keys(); en.hasMoreElements(); )
/*      */     {
/* 1459 */       String key = (String)en.nextElement();
/* 1460 */       FieldInfo fi = (FieldInfo)oldFields.get(key);
/* 1461 */       deletedFields.addElement(fi.m_name);
/* 1462 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1464 */         Report.debug("archiver", "Field " + fi.m_name + " is no longer needed.", null);
/*      */       }
/*      */     }
/* 1467 */     String[] deletedArray = StringUtils.convertListToArray(deletedFields);
/* 1468 */     FieldInfo[] addArray = new FieldInfo[addFields.size()];
/* 1469 */     for (int i = 0; i < addArray.length; ++i)
/*      */     {
/* 1471 */       addArray[i] = ((FieldInfo)addFields.elementAt(i));
/*      */     }
/* 1473 */     if (addArray.length > 0)
/*      */     {
/* 1475 */       diff.put("add", addArray);
/*      */     }
/* 1477 */     if (deletedFields.size() > 0)
/*      */     {
/* 1479 */       diff.put("deleted", deletedArray);
/*      */     }
/* 1481 */     return diff;
/*      */   }
/*      */ 
/*      */   protected boolean updateTableDesign(String tableName, Hashtable diffs)
/*      */     throws ServiceException
/*      */   {
/* 1487 */     boolean isCreateNewTable = false;
/* 1488 */     if (diffs.get("createNewTable") != null)
/*      */     {
/* 1490 */       isCreateNewTable = true;
/*      */     }
/*      */ 
/* 1493 */     FieldInfo[] addArray = (FieldInfo[])(FieldInfo[])diffs.get("add");
/* 1494 */     String[] deletedArray = (String[])(String[])diffs.get("deleted");
/* 1495 */     if (((addArray != null) && (addArray.length > 0)) || ((deletedArray != null) && (deletedArray.length > 0)))
/*      */     {
/*      */       try
/*      */       {
/* 1500 */         if (!isCreateNewTable)
/*      */         {
/* 1502 */           String[] keys = this.m_workspace.getPrimaryKeys(tableName);
/* 1503 */           this.m_workspace.alterTable(tableName, addArray, deletedArray, keys);
/*      */         }
/*      */         else
/*      */         {
/* 1507 */           String[] keys = (String[])(String[])diffs.get("primaryKeys");
/* 1508 */           FieldInfo[] cols = (FieldInfo[])(FieldInfo[])diffs.get("fieldInfos");
/* 1509 */           this.m_workspace.createTable(tableName, cols, keys);
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1514 */         throw new ServiceException(e);
/*      */       }
/*      */     }
/* 1517 */     return true;
/*      */   }
/*      */ 
/*      */   protected boolean validatePrimaryKeys(String tableName, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 1523 */     String primaryKeyString = binder.getLocal("primaryKeys" + tableName);
/* 1524 */     if (primaryKeyString == null)
/*      */     {
/* 1526 */       throw new ServiceException(LocaleUtils.encodeMessage("csArchiverTablePrimaryKeysNotExist", null, tableName));
/*      */     }
/*      */ 
/* 1530 */     Vector archivePrimaryKeys = StringUtils.parseArray(primaryKeyString, ',', ',');
/*      */     try
/*      */     {
/* 1533 */       String[] pkList = this.m_workspace.getPrimaryKeys(tableName);
/*      */ 
/* 1538 */       int size = archivePrimaryKeys.size();
/* 1539 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 1541 */         boolean match = false;
/* 1542 */         String archiveKey = (String)archivePrimaryKeys.elementAt(i);
/*      */ 
/* 1544 */         for (int j = 0; j < pkList.length; ++j)
/*      */         {
/* 1546 */           if (!archiveKey.equalsIgnoreCase(pkList[j]))
/*      */             continue;
/* 1548 */           match = true;
/* 1549 */           break;
/*      */         }
/*      */ 
/* 1552 */         if (match)
/*      */           continue;
/* 1554 */         String archivePKs = StringUtils.createString(archivePrimaryKeys, ',', ',');
/* 1555 */         Vector curPKsVec = StringUtils.convertToVector(pkList);
/* 1556 */         String curTablePKs = StringUtils.createString(curPKsVec, ',', ',');
/* 1557 */         Report.trace("archiver", "Mismatch primary key, unable to proceed to sync table design.", null);
/* 1558 */         throw new ServiceException(LocaleUtils.encodeMessage("csArchiverTablePKMismatch", null, archivePKs, curTablePKs));
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1565 */       throw new ServiceException("!csArchiverTableUnableToVerifyPrimaryKeys", e);
/*      */     }
/* 1567 */     return true;
/*      */   }
/*      */ 
/*      */   protected String getConstraintString(String tableName, DataBinder binder) throws ServiceException
/*      */   {
/* 1572 */     String keys = binder.getLocal("primaryKeys" + tableName);
/* 1573 */     if ((keys == null) || (keys.length() == 0))
/*      */     {
/* 1575 */       String msg = LocaleUtils.encodeMessage("csArchiverTablePrimaryKeysNotExist", null, tableName);
/* 1576 */       throw new ServiceException(msg);
/*      */     }
/* 1578 */     return keys;
/*      */   }
/*      */ 
/*      */   protected String getAdditionalFilter(String tableName, DataBinder binder) throws ServiceException, DataException
/*      */   {
/* 1583 */     doFilter("additionalImportFilter", binder);
/* 1584 */     String filter = binder.getLocal("additionalImportFilter");
/* 1585 */     if (filter == null)
/*      */     {
/* 1587 */       filter = "";
/*      */     }
/* 1589 */     return filter;
/*      */   }
/*      */ 
/*      */   protected ValueMapData createValueMap(String table, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 1595 */     ValueMapData mapData = (ValueMapData)ComponentClassFactory.createClassInstance("tableArchiveValueMapData", "intradoc.server.ValueMapData", "csArchiveTableUnableFindeValueMapDate");
/* 1596 */     if (binder == null)
/*      */     {
/* 1598 */       binder = new DataBinder();
/*      */     }
/* 1600 */     mapData.setDynamicHtmlMerger(new PageMerger(binder, this.m_context));
/*      */ 
/* 1602 */     String baseTable = binder.getLocal("tableName");
/*      */ 
/* 1604 */     String mapStr = this.m_archiveData.getLocal("aValueMapsTable" + baseTable);
/* 1605 */     if ((mapStr != null) && (mapStr.length() != 0))
/*      */     {
/* 1607 */       ClausesData clauseData = new ClausesData();
/* 1608 */       clauseData.parse(mapStr);
/*      */ 
/* 1610 */       Vector values = clauseData.m_clauses;
/* 1611 */       int size = values.size();
/*      */ 
/* 1613 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 1615 */         Vector elts = (Vector)values.elementAt(i);
/* 1616 */         boolean isAll = StringUtils.convertToBool((String)elts.elementAt(0), false);
/* 1617 */         String input = (String)elts.elementAt(1);
/* 1618 */         String field = (String)elts.elementAt(2);
/* 1619 */         String output = (String)elts.elementAt(3);
/*      */ 
/* 1621 */         String fieldTable = baseTable;
/* 1622 */         int index = field.indexOf(46);
/* 1623 */         if (index > 0)
/*      */         {
/* 1625 */           fieldTable = field.substring(0, index);
/* 1626 */           field = field.substring(index + 1);
/*      */         }
/* 1628 */         if ((SharedObjects.getEnvValueAsBoolean("DisableValueMappingForParentTable", false)) && (!fieldTable.equals(table))) {
/*      */           continue;
/*      */         }
/* 1631 */         mapData.addMap(field, isAll, input, output);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1636 */     return mapData;
/*      */   }
/*      */ 
/*      */   public void recordImportChanges(String tableName, ImportTableDataResultSet drset, String srcIDCol, String constraintKeys, String timestamp)
/*      */     throws DataException
/*      */   {
/* 1642 */     int srcIndex = -1;
/* 1643 */     if (srcIDCol != null)
/*      */     {
/* 1645 */       FieldInfo fi = new FieldInfo();
/* 1646 */       drset.getFieldInfo(srcIDCol, fi);
/* 1647 */       srcIndex = fi.m_index;
/*      */     }
/* 1649 */     int timeIndex = -1;
/* 1650 */     if (timestamp != null)
/*      */     {
/* 1652 */       FieldInfo fi = new FieldInfo();
/* 1653 */       drset.getFieldInfo(timestamp, fi);
/* 1654 */       timeIndex = fi.m_index;
/*      */     }
/*      */ 
/* 1657 */     for (drset.firstImportedRow(); drset.isRowPresent(); drset.nextImportedRow())
/*      */     {
/*      */       try
/*      */       {
/* 1661 */         TableModHistoryUtils.insertEntry(this.m_workspace, tableName, drset, constraintKeys, srcIndex, timeIndex, false, true);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1666 */         Report.appError("archiver", null, LocaleUtils.encodeMessage("csArchiverTableUnableRecordTableChange", null, e.getMessage()), e);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String constructPKStringOrTypes(DataResultSet drset, String keys, boolean isTypes)
/*      */   {
/* 1675 */     StringBuffer buf = new StringBuffer();
/* 1676 */     Vector constraints = StringUtils.parseArray(keys, ',', '\\');
/* 1677 */     int size = constraints.size();
/* 1678 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1680 */       String key = (String)constraints.elementAt(i);
/* 1681 */       FieldInfo fi = new FieldInfo();
/* 1682 */       if (!drset.getFieldInfo(key, fi))
/*      */         continue;
/* 1684 */       if (buf.length() > 0)
/*      */       {
/* 1686 */         buf.append(',');
/*      */       }
/* 1688 */       if (isTypes);
/* 1690 */       switch (fi.m_type)
/*      */       {
/*      */       case 1:
/* 1693 */         buf.append("Boolean");
/* 1694 */         break;
/*      */       case 5:
/* 1696 */         buf.append("Date");
/* 1697 */         break;
/*      */       case 3:
/* 1699 */         buf.append("Number");
/* 1700 */         break;
/*      */       case 2:
/* 1702 */         buf.append("Char");
/* 1703 */         if (fi.m_maxLen < 0)
/*      */           continue;
/* 1705 */         buf.append(' ');
/* 1706 */         buf.append(fi.m_maxLen); break;
/*      */       case 4:
/*      */       default:
/* 1710 */         buf.append("Text");
/* 1711 */         if (fi.m_maxLen < 0)
/*      */           continue;
/* 1713 */         buf.append(' ');
/* 1714 */         buf.append(fi.m_maxLen); continue;
/*      */ 
/* 1720 */         buf.append(drset.getStringValue(fi.m_index));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1725 */     return buf.toString();
/*      */   }
/*      */ 
/*      */   protected String getImportModifyDate(DataResultSet drset, String[] timestamps)
/*      */   {
/* 1730 */     String modifyDate = null;
/* 1731 */     if (timestamps != null)
/*      */     {
/* 1733 */       FieldInfo fi = new FieldInfo();
/* 1734 */       if (drset.getFieldInfo(timestamps[1], fi))
/*      */       {
/* 1736 */         modifyDate = drset.getStringValue(fi.m_index);
/*      */       }
/*      */     }
/* 1739 */     return modifyDate;
/*      */   }
/*      */ 
/*      */   protected void addChangedTable(String table)
/*      */   {
/* 1744 */     if ((table == null) || (table.length() == 0) || (this.m_changedTables.contains(table)))
/*      */       return;
/* 1746 */     this.m_changedTables.addElement(table);
/*      */   }
/*      */ 
/*      */   protected void notifySubjects()
/*      */   {
/* 1752 */     Vector notifiedSubjects = new IdcVector();
/*      */ 
/* 1754 */     int size = this.m_changedTables.size();
/* 1755 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1757 */       String table = (String)this.m_changedTables.elementAt(i);
/* 1758 */       String subject = (String)this.m_subjectsMap.get(table.toLowerCase());
/* 1759 */       if (subject == null)
/*      */       {
/* 1762 */         subject = "schema";
/*      */       }
/*      */ 
/* 1766 */       if (notifiedSubjects.contains(subject))
/*      */         continue;
/* 1768 */       SubjectManager.notifyChanged(subject);
/* 1769 */       SubjectManager.forceRefresh(subject);
/* 1770 */       notifiedSubjects.addElement(subject);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void postprocessBatch(String table, String prevTable, String nextTable, DataBinder tableBinder, ValueMapData map, ArchiveTableCallBack callBack)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1780 */     Report.trace("archiver", "Do batch post processing for table " + table, null);
/* 1781 */     String baseTable = tableBinder.getLocal("tableName");
/* 1782 */     if ((DataBinderUtils.getBoolean(tableBinder, "removeExistingChildren", false)) && (!table.equals(baseTable)))
/*      */     {
/* 1785 */       doRemoveExistingChildren(table, nextTable, tableBinder, map);
/*      */     }
/*      */ 
/* 1788 */     doFilter("postProcessBatch", tableBinder);
/*      */   }
/*      */ 
/*      */   protected void doRemoveExistingChildren(String table, String nextTable, DataBinder tableBinder, ValueMapData map)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1795 */     Report.trace("archiver", "Removing existing children in table " + nextTable, null);
/* 1796 */     ImportTableDataResultSet rset = (ImportTableDataResultSet)tableBinder.getResultSet("ExportResults" + table);
/*      */ 
/* 1799 */     DataResultSet children = getChildrenRowsFromParents(nextTable, table, tableBinder, rset, null);
/*      */ 
/* 1802 */     rset = (ImportTableDataResultSet)tableBinder.getResultSet("DeletedRows" + table);
/* 1803 */     children = getChildrenRowsFromParents(nextTable, table, tableBinder, rset, children);
/*      */ 
/* 1805 */     String constraints = getConstraintString(nextTable, tableBinder);
/* 1806 */     String[] timestamps = findTimeStampColumns(nextTable, tableBinder, new String[] { "createTimeStamp", "modifyTimeStamp" }, true);
/*      */ 
/* 1808 */     String srcID = retrieveSourceID(nextTable, tableBinder.getLocal("tableName"), tableBinder);
/* 1809 */     ArchiveTableCallBack callback = getArchiveTableCallBack(nextTable, constraints, srcID, timestamps, tableBinder);
/*      */ 
/* 1812 */     this.m_st.importTable(nextTable, children, constraints, null, null, callback, true);
/*      */   }
/*      */ 
/*      */   protected DataResultSet getChildrenRowsFromParents(String childTable, String parentTable, DataBinder tableBinder, ImportTableDataResultSet parentRset, DataResultSet children)
/*      */     throws DataException
/*      */   {
/* 1818 */     if (parentRset == null)
/*      */     {
/* 1820 */       return null;
/*      */     }
/*      */ 
/* 1823 */     String tableRelation = getTableRelations(childTable, parentTable, tableBinder);
/* 1824 */     Vector keyFields = getKeyFields(parentTable, tableBinder, parentRset);
/*      */ 
/* 1826 */     StringBuffer queryBuf = new StringBuffer();
/* 1827 */     queryBuf.append("SELECT ");
/* 1828 */     queryBuf.append(childTable + ".* ");
/* 1829 */     queryBuf.append("FROM " + childTable + ", " + parentTable);
/* 1830 */     queryBuf.append(" WHERE ");
/* 1831 */     queryBuf.append(tableRelation);
/* 1832 */     String query = queryBuf.toString();
/*      */ 
/* 1834 */     for (parentRset.firstImportedRow(); parentRset.isRowPresent(); parentRset.nextImportedRow())
/*      */     {
/* 1836 */       DataResultSet newRows = getChildrenRowsFromParent(childTable, parentTable, query, keyFields, parentRset);
/*      */ 
/* 1839 */       if (children == null)
/*      */       {
/* 1841 */         children = newRows;
/*      */       }
/*      */       else
/*      */       {
/* 1845 */         for (newRows.first(); newRows.isRowPresent(); newRows.next())
/*      */         {
/* 1847 */           Vector row = newRows.getCurrentRowValues();
/* 1848 */           children.addRow(row);
/*      */         }
/*      */       }
/*      */     }
/* 1852 */     return children;
/*      */   }
/*      */ 
/*      */   protected DataResultSet getChildrenRowsFromParent(String childTable, String parentTable, String query, Vector keyFields, DataResultSet rset)
/*      */     throws DataException
/*      */   {
/* 1860 */     StringBuffer queryBuf = new StringBuffer(query);
/*      */ 
/* 1862 */     int size = keyFields.size();
/* 1863 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1865 */       FieldInfo fi = (FieldInfo)keyFields.elementAt(i);
/* 1866 */       String value = rset.getStringValue(fi.m_index);
/*      */ 
/* 1868 */       queryBuf.append(" AND ");
/* 1869 */       queryBuf.append(parentTable + ".");
/* 1870 */       queryBuf.append(fi.m_name);
/* 1871 */       if ((value == null) || (value.length() == 0))
/*      */       {
/* 1873 */         queryBuf.append(" IS NULL");
/*      */       }
/*      */       else {
/* 1876 */         queryBuf.append(" = ");
/*      */ 
/* 1878 */         if (fi.m_type == 6)
/*      */         {
/* 1880 */           value = "'" + StringUtils.createQuotableString(value) + "'";
/*      */         }
/* 1882 */         queryBuf.append(value);
/*      */       }
/*      */     }
/* 1885 */     query = queryBuf.toString();
/*      */ 
/* 1887 */     ResultSet result = this.m_workspace.createResultSetSQL(query);
/* 1888 */     DataResultSet drset = new DataResultSet();
/* 1889 */     drset.copy(result, 0);
/* 1890 */     return drset;
/*      */   }
/*      */ 
/*      */   protected String getTableRelations(String childTable, String parentTable, DataBinder tableBinder)
/*      */   {
/* 1895 */     String relationStr = tableBinder.getLocal("tableRelations");
/* 1896 */     Vector relations = StringUtils.parseArray(relationStr, ',', '^');
/* 1897 */     int size = relations.size();
/* 1898 */     String relation = null;
/* 1899 */     boolean foundIt = false;
/* 1900 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1902 */       relation = (String)relations.elementAt(i);
/* 1903 */       if ((relation.indexOf(childTable + ".") < 0) || (relation.indexOf(parentTable + ".") < 0)) {
/*      */         continue;
/*      */       }
/* 1906 */       foundIt = true;
/* 1907 */       break;
/*      */     }
/*      */ 
/* 1911 */     return (foundIt) ? relation : null;
/*      */   }
/*      */ 
/*      */   protected Vector getKeyFields(String parentTable, DataBinder tableBinder, DataResultSet rset)
/*      */   {
/* 1916 */     String keys = null;
/*      */     try
/*      */     {
/* 1919 */       keys = getConstraintString(parentTable, tableBinder);
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/* 1923 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1925 */         Report.debug("archiver", null, ignore);
/*      */       }
/*      */     }
/*      */ 
/* 1929 */     Vector keyFields = new IdcVector();
/* 1930 */     if (keys == null)
/*      */     {
/* 1932 */       int len = rset.getNumFields();
/*      */ 
/* 1934 */       for (int i = 0; i < len; ++i)
/*      */       {
/* 1936 */         FieldInfo fi = new FieldInfo();
/* 1937 */         rset.getIndexFieldInfo(i, fi);
/* 1938 */         keyFields.addElement(fi);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1943 */       Vector keyNames = StringUtils.parseArray(keys, ',', '^');
/* 1944 */       int len = keyNames.size();
/* 1945 */       for (int i = 0; i < len; ++i)
/*      */       {
/* 1947 */         FieldInfo fi = new FieldInfo();
/* 1948 */         String name = (String)keyNames.elementAt(i);
/* 1949 */         rset.getFieldInfo(name, fi);
/* 1950 */         keyFields.addElement(fi);
/*      */       }
/*      */     }
/* 1953 */     return keyFields;
/*      */   }
/*      */ 
/*      */   protected int doFilter(String filterType)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1959 */     return doFilter(filterType, this.m_binder);
/*      */   }
/*      */ 
/*      */   protected int doFilter(String filterType, DataBinder binder) throws ServiceException, DataException
/*      */   {
/* 1964 */     return PluginFilters.filter(filterType, this.m_workspace, binder, this.m_context);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1969 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97191 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiveTableHelper
 * JD-Core Version:    0.5.4
 */