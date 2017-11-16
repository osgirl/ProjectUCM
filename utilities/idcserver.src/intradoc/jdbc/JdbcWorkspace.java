/*      */ package intradoc.jdbc;
/*      */ 
/*      */ import intradoc.common.ClassHelperUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IntervalData;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.CallableResults;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DatabaseConfigData;
/*      */ import intradoc.data.DatabaseIndexInfo;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcDataSourceQuery;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.QueryParameterInfo;
/*      */ import intradoc.data.QueryUtils;
/*      */ import intradoc.data.SimpleQueryInfo;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceCallback;
/*      */ import intradoc.data.WorkspaceCallbackStatus;
/*      */ import intradoc.data.WorkspaceEventID;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.ProviderConfig;
/*      */ import intradoc.provider.ProviderConnection;
/*      */ import intradoc.provider.ProviderInterface;
/*      */ import intradoc.provider.WorkspaceProviderConfig;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.sql.CallableStatement;
/*      */ import java.sql.Connection;
/*      */ import java.sql.DatabaseMetaData;
/*      */ import java.sql.PreparedStatement;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.Savepoint;
/*      */ import java.sql.Statement;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Stack;
/*      */ import java.util.Vector;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ 
/*      */ public class JdbcWorkspace
/*      */   implements Workspace, ProviderInterface
/*      */ {
/*   41 */   protected Provider m_provider = null;
/*   42 */   protected JdbcManager m_manager = null;
/*   43 */   protected DatabaseConfigData m_config = null;
/*   44 */   protected boolean m_isStarted = false;
/*      */   public static final int DATE_FMT_ODBC = 0;
/*      */   public static final int DATE_FMT_ODBC_RAW = 1;
/*      */   public static final int DATE_FMT_JDBC = 2;
/*      */   public static final int DATE_FMT_JAVA = 3;
/*      */   public static final int DATE_FMT_ISO8601 = 4;
/*   52 */   protected static ConcurrentHashMap m_threadTimeout = new ConcurrentHashMap();
/*   53 */   protected int m_defaultTimeout = 3600;
/*      */ 
/*   55 */   protected Hashtable m_savedPoints = new Hashtable();
/*   56 */   protected Object m_spSyncObj = new Object();
/*   57 */   protected Hashtable m_batchs = new Hashtable();
/*   58 */   protected ConcurrentHashMap m_fakeSavepoints = new ConcurrentHashMap();
/*   59 */   protected boolean m_useFakeSavepoints = false;
/*   60 */   protected boolean m_useIsClosedStatementCall = false;
/*      */ 
/*   62 */   protected Map<String, IdcDataSourceQuery> m_dataSourceMap = new ConcurrentHashMap();
/*      */ 
/*   64 */   protected Map<String, List> m_defaultCallbacks = new ConcurrentHashMap();
/*      */ 
/*      */   public void init(Provider provider)
/*      */     throws DataException
/*      */   {
/*   76 */     this.m_provider = provider;
/*   77 */     this.m_manager = ((JdbcManager)this.m_provider.createClass("ProviderManager", "intradoc.jdbc.JdbcManager"));
/*   78 */     this.m_manager.init(provider);
/*      */ 
/*   81 */     Connection testConnection = null;
/*   82 */     if (!this.m_manager.usingExternalDataSource())
/*      */     {
/*   84 */       testConnection = JdbcConnectionUtils.getConnection(this.m_manager, provider.getProviderData());
/*      */     }
/*      */     else
/*      */     {
/*      */       try
/*      */       {
/*   90 */         testConnection = (Connection)this.m_manager.getExternalRawConnection();
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*   94 */         throw new DataException(null, e);
/*      */       }
/*      */     }
/*   97 */     if (testConnection == null)
/*      */       return;
/*      */     try
/*      */     {
/*  101 */       testConnection.close();
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/*  105 */       Report.trace("systemdatabase", null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getReportString(String key)
/*      */   {
/*  112 */     if ((key != null) && (key.equalsIgnoreCase("audit")))
/*      */     {
/*  114 */       return this.m_manager.createAuditReport();
/*      */     }
/*  116 */     return null;
/*      */   }
/*      */ 
/*      */   public void startProvider() throws DataException, ServiceException
/*      */   {
/*  121 */     if (this.m_isStarted)
/*      */       return;
/*  123 */     DataBinder providerData = this.m_provider.getProviderData();
/*      */ 
/*  127 */     int numConnections = NumberUtils.parseInteger(this.m_manager.m_config.getValueAsString("NumConnections"), 5);
/*  128 */     numConnections = NumberUtils.parseInteger(providerData.getLocal("NumConnections"), numConnections);
/*      */ 
/*  130 */     initConnectionPoolAndConfiguration(numConnections, 0, null);
/*      */   }
/*      */ 
/*      */   public void stopProvider()
/*      */   {
/*  136 */     this.m_isStarted = false;
/*  137 */     this.m_manager.cleanUp();
/*      */   }
/*      */ 
/*      */   public Provider getProvider()
/*      */   {
/*  142 */     return this.m_provider;
/*      */   }
/*      */ 
/*      */   public ProviderConfig createProviderConfig() throws DataException
/*      */   {
/*  147 */     return (WorkspaceProviderConfig)this.m_provider.createClass("ProviderConfig", "intradoc.provider.WorkspaceProviderConfig");
/*      */   }
/*      */ 
/*      */   public void testConnection(DataBinder binder, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  154 */     String testQuery = binder.getLocal("TestQuery");
/*  155 */     if (testQuery == null)
/*      */     {
/*  157 */       testQuery = this.m_config.getValueAsString("TestQuery");
/*  158 */       if (testQuery == null)
/*      */       {
/*  160 */         return;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  167 */       createResultSetSQL(testQuery);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  171 */       String msg = LocaleUtils.encodeMessage("csDbUnableToCreateConnection", e.getMessage(), this.m_manager.getDescription());
/*      */ 
/*  173 */       throw new DataException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void pollConnectionState(DataBinder provData, Properties provState)
/*      */   {
/*  179 */     if (this.m_manager.usingExternalDataSource())
/*      */     {
/*  181 */       String msg = "!csJdbcConnectionGoodStateExternallyProvided";
/*  182 */       provState.put("ConnectionState", msg);
/*  183 */       return;
/*      */     }
/*  185 */     Vector connections = this.m_manager.getAllConnections();
/*  186 */     int num = connections.size();
/*  187 */     int numGood = 0;
/*  188 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  190 */       ProviderConnection connection = (ProviderConnection)connections.elementAt(i);
/*  191 */       if (!connection.isBadConnection())
/*      */       {
/*  193 */         ++numGood;
/*      */       }
/*      */       else
/*      */       {
/*  197 */         this.m_manager.debugMsg(new StringBuilder().append("Connection has been marked bad, last error message is: ").append(((JdbcConnection)connection).m_badConnectionMsg).toString());
/*      */       }
/*      */     }
/*      */ 
/*  201 */     String str = LocaleUtils.encodeMessage("csJdbcConnectionGoodMessage", null, new StringBuilder().append("").append(numGood).toString(), new StringBuilder().append("").append(num).toString());
/*      */ 
/*  203 */     provState.put("ConnectionState", str);
/*      */   }
/*      */ 
/*      */   public void getConnectionState(Map state)
/*      */   {
/*  208 */     if (state == null)
/*      */     {
/*  210 */       state = new HashMap();
/*      */     }
/*  212 */     Boolean hasActiveConnection = Boolean.FALSE;
/*  213 */     Boolean isInTransaction = Boolean.FALSE;
/*      */     try
/*      */     {
/*  217 */       String name = Thread.currentThread().getName();
/*  218 */       JdbcConnection jCon = (JdbcConnection)this.m_manager.getConnectionEx(name, 1);
/*  219 */       if (jCon != null)
/*      */       {
/*  221 */         hasActiveConnection = Boolean.TRUE;
/*  222 */         isInTransaction = (jCon.m_inTransaction) ? Boolean.TRUE : Boolean.FALSE;
/*  223 */         Boolean isBadConnection = (jCon.isBadConnection()) ? Boolean.TRUE : Boolean.FALSE;
/*  224 */         state.put("isBadConnection", isBadConnection);
/*      */ 
/*  226 */         String id = jCon.getId();
/*  227 */         state.put("id", id);
/*      */ 
/*  229 */         String aid = jCon.getActionID();
/*  230 */         String action = jCon.getCurrentAction();
/*  231 */         if ((aid != null) && (action != null))
/*      */         {
/*  233 */           state.put("action", action);
/*  234 */           state.put("actionID", aid);
/*  235 */           String aStatus = jCon.getCurrentActionStatus();
/*  236 */           state.put("actionStatus", aStatus);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  243 */       this.m_manager.debugMsg(e.getMessage());
/*      */     }
/*      */ 
/*  246 */     state.put("hasActiveConnection", hasActiveConnection);
/*  247 */     state.put("isInTransaction", isInTransaction);
/*      */ 
/*  249 */     long timeout = getThreadTimeout();
/*  250 */     state.put("queryTimeout", new Long(timeout));
/*      */   }
/*      */ 
/*      */   public void addQueryDefs(DataResultSet rset)
/*      */     throws DataException
/*      */   {
/*  259 */     this.m_manager.addQueryDefs(rset);
/*      */   }
/*      */ 
/*      */   public void loadColumnMap(DataResultSet rset)
/*      */   {
/*  264 */     this.m_manager.loadColumnMap(rset);
/*      */   }
/*      */ 
/*      */   public String checkOrUpdateColumnAlias(String field, boolean isUpdate)
/*      */     throws DataException
/*      */   {
/*  270 */     Properties columnMap = this.m_manager.getColumnMap();
/*  271 */     String exField = columnMap.getProperty(field.toUpperCase());
/*  272 */     if ((isUpdate) && (exField == null))
/*      */     {
/*  274 */       columnMap.put(field.toUpperCase(), field);
/*      */     }
/*  276 */     return exField;
/*      */   }
/*      */ 
/*      */   public void updateColumnMapping(Vector tableFields, Properties columnMap)
/*      */   {
/*  282 */     int size = tableFields.size();
/*  283 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  285 */       FieldInfo newField = (FieldInfo)tableFields.elementAt(i);
/*  286 */       String newColumn = newField.m_name;
/*  287 */       columnMap.put(newColumn.toUpperCase(), newColumn);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void loadUpperCaseColumns(String str)
/*      */   {
/*  293 */     this.m_config.loadUpperCaseColumns(str);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void initConnections(int numConnections) throws DataException
/*      */   {
/*  299 */     initConnectionPoolAndConfiguration(numConnections, 0, null);
/*      */   }
/*      */ 
/*      */   public void initConnectionPoolAndConfiguration(int numConnections, int flags, Map params)
/*      */     throws DataException
/*      */   {
/*  309 */     this.m_isStarted = true;
/*      */ 
/*  313 */     this.m_config = this.m_manager.m_config;
/*      */ 
/*  315 */     String modifiers = this.m_config.getValueAsString("JdbcQueryModifierList");
/*  316 */     if ((modifiers != null) && (modifiers.length() > 0))
/*      */     {
/*  318 */       JdbcQueryUtils.initQueryModifier(this.m_manager, modifiers);
/*      */     }
/*      */ 
/*  321 */     DataBinder providerData = this.m_provider.getProviderData();
/*  322 */     providerData.putLocal("NumConnections", String.valueOf(numConnections));
/*      */ 
/*  324 */     DataResultSet drset = (DataResultSet)providerData.getResultSet("ColumnMap");
/*  325 */     if (drset != null)
/*      */     {
/*  327 */       loadColumnMap(drset);
/*      */     }
/*      */     else
/*      */     {
/*  332 */       throw new DataException("!csJdbcPreserveCaseDbWithoutColumnTranslationTable");
/*      */     }
/*      */ 
/*  335 */     String cols = this.m_config.getValueAsString("UpperCaseColumns");
/*  336 */     this.m_config.loadUpperCaseColumns(cols);
/*      */ 
/*  338 */     if (this.m_manager.usingExternalDataSource())
/*      */       return;
/*  340 */     this.m_manager.startKeepAlivePingConnections();
/*  341 */     for (int i = 0; i < numConnections; ++i)
/*      */     {
/*  343 */       this.m_manager.addConnectionToPool();
/*      */     }
/*      */   }
/*      */ 
/*      */   public JdbcConnection getJdbcConnection()
/*      */     throws DataException
/*      */   {
/*  350 */     Thread thrd = Thread.currentThread();
/*  351 */     String name = thrd.getName();
/*  352 */     return getJdbcConnectionEx(name, 17, false);
/*      */   }
/*      */ 
/*      */   protected JdbcConnection getJdbcConnectionEx(String name, int type, boolean isAllowMissing)
/*      */     throws DataException
/*      */   {
/*  358 */     JdbcConnection con = (JdbcConnection)this.m_manager.getConnectionEx(name, type);
/*      */ 
/*  360 */     if ((con == null) && (!isAllowMissing))
/*      */     {
/*  362 */       throw new DataException("!csDbServerBusyNoConnection");
/*      */     }
/*  364 */     if (con != null)
/*      */     {
/*  366 */       con.reset(false);
/*  367 */       this.m_manager.reserveAccess(con, false);
/*      */ 
/*  369 */       this.m_provider.markState("active");
/*      */     }
/*      */ 
/*  372 */     return con;
/*      */   }
/*      */ 
/*      */   protected void releaseAccess(ProviderConnection con)
/*      */   {
/*  377 */     if (con == null)
/*      */       return;
/*  379 */     this.m_manager.releaseAccess(con, false);
/*      */   }
/*      */ 
/*      */   public int addBatchSQL(String query)
/*      */     throws DataException
/*      */   {
/*  385 */     QueryBatchData batchData = getQueryBatchData();
/*  386 */     int index = batchData.getSize();
/*  387 */     batchData.addBatchSQL(query);
/*      */ 
/*  389 */     this.m_manager.debugMsg(new StringBuilder().append("Batched query added at index ").append(index).append(": ").append(query).toString());
/*  390 */     return index;
/*      */   }
/*      */ 
/*      */   public int addBatch(String query, Parameters args) throws DataException
/*      */   {
/*  395 */     QueryBatchData batchData = getQueryBatchData();
/*  396 */     int index = batchData.getSize();
/*  397 */     JdbcConnection jCon = getJdbcConnection();
/*  398 */     JdbcQueryDef qDef = jCon.getQueryDefAllowMissing(query, true);
/*  399 */     if (qDef != null)
/*      */     {
/*  401 */       query = batchData.addBatch(qDef, args);
/*  402 */       this.m_manager.debugMsg(new StringBuilder().append("Batched query added at index ").append(index).append(": ").append(query).toString());
/*      */     }
/*      */     else
/*      */     {
/*  406 */       this.m_manager.debugMsg(new StringBuilder().append("Batched datasource query added at index ").append(index).append(": ").append(query).toString());
/*  407 */       IdcDataSourceQuery dsQuery = (IdcDataSourceQuery)this.m_dataSourceMap.get(query);
/*  408 */       if (dsQuery != null)
/*      */       {
/*  410 */         dsQuery.addBatch(this, args, null);
/*      */       }
/*      */       else
/*      */       {
/*  414 */         throw new DataException(null, "csQueryNotFound", new Object[] { query });
/*      */       }
/*      */     }
/*  417 */     return index;
/*      */   }
/*      */ 
/*      */   public int[] executeBatch() throws DataException
/*      */   {
/*  422 */     QueryBatchData batchData = getQueryBatchData();
/*  423 */     if ((batchData == null) || (batchData.getSize() == 0))
/*      */     {
/*  425 */       return null;
/*      */     }
/*  427 */     int[] result = null;
/*  428 */     JdbcConnection jCon = getJdbcConnection();
/*  429 */     Statement stmt = null;
/*  430 */     int size = batchData.getSize();
/*  431 */     String queryReportStr = new StringBuilder().append("Batch execution. ").append(size).append(" queries.").toString();
/*  432 */     String additionalMsg = null;
/*      */     try
/*      */     {
/*  435 */       reportStartAction(jCon, queryReportStr, true, false);
/*  436 */       stmt = batchData.prepareExecution(jCon);
/*  437 */       result = stmt.executeBatch();
/*  438 */       additionalMsg = "Executed.";
/*      */     }
/*      */     catch (SQLException isClosed)
/*      */     {
/*      */       boolean isClosed;
/*  442 */       additionalMsg = e.getMessage();
/*  443 */       handleSQLException(e, jCon, "csDbUnableToExecuteBatch", "", true);
/*      */     }
/*      */     finally
/*      */     {
/*      */       boolean isClosed;
/*  447 */       clearBatch();
/*      */ 
/*  449 */       boolean isClosed = isStatementClosed(stmt);
/*  450 */       if (!isClosed)
/*      */       {
/*  452 */         closeStatement(stmt, null, null);
/*      */       }
/*      */ 
/*  455 */       reportEndAction(jCon, additionalMsg);
/*  456 */       releaseAccess(jCon);
/*      */     }
/*  458 */     return result;
/*      */   }
/*      */ 
/*      */   public ArrayList getBatch()
/*      */   {
/*  463 */     QueryBatchData batchObj = getQueryBatchData();
/*  464 */     return batchObj.getBatch();
/*      */   }
/*      */ 
/*      */   protected QueryBatchData getQueryBatchData()
/*      */   {
/*  469 */     String key = Thread.currentThread().getName();
/*  470 */     QueryBatchData batchObj = (QueryBatchData)this.m_batchs.get(key);
/*  471 */     if (batchObj == null)
/*      */     {
/*  473 */       batchObj = new QueryBatchData();
/*  474 */       batchObj.init(this, this.m_manager);
/*  475 */       this.m_batchs.put(key, batchObj);
/*      */     }
/*  477 */     return batchObj;
/*      */   }
/*      */ 
/*      */   public ArrayList clearBatch()
/*      */   {
/*  486 */     String key = Thread.currentThread().getName();
/*  487 */     QueryBatchData data = (QueryBatchData)this.m_batchs.remove(key);
/*  488 */     ArrayList batch = null;
/*  489 */     if (data != null)
/*      */     {
/*  491 */       batch = data.getBatch();
/*  492 */       data.cleanBatch();
/*      */     }
/*  494 */     return batch;
/*      */   }
/*      */ 
/*      */   public CallableResults executeCallable(String query, Parameters args)
/*      */     throws DataException
/*      */   {
/*  502 */     JdbcQueryDef qDef = null;
/*  503 */     JdbcCallableResults result = null;
/*  504 */     JdbcConnection jCon = getJdbcConnection();
/*  505 */     CallableStatement stmt = null;
/*  506 */     String queryReportStr = query;
/*  507 */     String additionalMsg = null;
/*      */     try
/*      */     {
/*  510 */       qDef = jCon.getQueryDef(query);
/*  511 */       String queryStr = new StringBuilder().append("Executing CallableStatement (").append(qDef.m_query).append(")").toString();
/*  512 */       reportStartAction(jCon, queryStr, true, false);
/*  513 */       stmt = (CallableStatement)createStatement(jCon, qDef);
/*  514 */       buildPreparedQuery(qDef, args);
/*  515 */       boolean firstIsRset = stmt.execute();
/*  516 */       result = new JdbcCallableResults(this.m_manager);
/*  517 */       result.init(query, stmt, qDef, jCon, firstIsRset);
/*  518 */       jCon.setActiveCallableResult(result);
/*  519 */       additionalMsg = new StringBuilder().append("Executed. First result is ResultSet:").append(firstIsRset).toString();
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*  523 */       additionalMsg = e.getMessage();
/*  524 */       handleSQLException(e, jCon, "csDbUnableToExecuteCallableQuery", queryReportStr, true);
/*      */     }
/*      */     finally
/*      */     {
/*  529 */       ActiveQueryUtils.removeActiveQuery();
/*  530 */       reportEndAction(jCon, additionalMsg);
/*      */     }
/*  532 */     return result;
/*      */   }
/*      */ 
/*      */   public long execute(String query, Parameters args)
/*      */     throws DataException
/*      */   {
/*  540 */     JdbcQueryDef qDef = null;
/*  541 */     long result = 0L;
/*  542 */     JdbcConnection jCon = getJdbcConnection();
/*  543 */     Statement stmt = null;
/*  544 */     String queryReportStr = query;
/*  545 */     String additionalMsg = null;
/*  546 */     Map queryBldMap = null;
/*      */     try
/*      */     {
/*  549 */       qDef = jCon.getQueryDefAllowMissing(query, true);
/*  550 */       if (qDef != null)
/*      */       {
/*  552 */         if (qDef.m_isPrepared)
/*      */         {
/*  554 */           stmt = createStatement(jCon, qDef);
/*  555 */           queryBldMap = buildPreparedQuery(qDef, args);
/*  556 */           String queryStr = new StringBuilder().append("Executing PreparedStatement (").append(qDef.m_query).append(")").toString();
/*  557 */           reportStartAction(jCon, queryStr, true, false);
/*  558 */           result = qDef.m_statement.executeUpdate();
/*      */         }
/*      */         else
/*      */         {
/*  562 */           String queryStr = buildQuery(qDef, args);
/*  563 */           queryReportStr = new StringBuilder().append(query).append("(").append(queryStr).append(")").toString();
/*  564 */           reportStartAction(jCon, queryStr, true, true);
/*  565 */           stmt = createStatement(jCon, null);
/*  566 */           result = stmt.executeUpdate(queryStr);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  571 */         IdcDataSourceQuery dsQuery = (IdcDataSourceQuery)this.m_dataSourceMap.get(query);
/*  572 */         if (dsQuery != null)
/*      */         {
/*  574 */           result = dsQuery.execute(this, args, null)[0];
/*      */         }
/*      */         else
/*      */         {
/*  578 */           throw new DataException(null, "csUnableToFindQueryOrIdcDataSource", new Object[] { query });
/*      */         }
/*      */       }
/*  581 */       additionalMsg = new StringBuilder().append("Executed. ").append(result).append(" row(s) affected.").toString();
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*  585 */       additionalMsg = e.getMessage();
/*  586 */       handleSQLException(e, jCon, "csDbUnableToExecuteQuery", queryReportStr, true);
/*      */     }
/*      */     finally
/*      */     {
/*  590 */       closeStatement(stmt, queryReportStr, queryBldMap);
/*  591 */       reportEndAction(jCon, additionalMsg);
/*  592 */       releaseAccess(jCon);
/*      */     }
/*  594 */     return result;
/*      */   }
/*      */ 
/*      */   public intradoc.data.ResultSet createResultSet(String query, Parameters args)
/*      */     throws DataException
/*      */   {
/*  600 */     HashMap data = new HashMap();
/*  601 */     Boolean isQueryObjectPersistent = Boolean.valueOf(false);
/*  602 */     Boolean resetCursorFlags = Boolean.valueOf(true);
/*  603 */     if (args != null)
/*      */     {
/*  606 */       isQueryObjectPersistent = Boolean.valueOf(StringUtils.convertToBool(args.getSystem("IsQueryObjectPersistent"), false));
/*  607 */       resetCursorFlags = Boolean.valueOf(StringUtils.convertToBool(args.getSystem("ResetCursorFlags"), true));
/*      */ 
/*  609 */       String useForwardOnlyCursor = args.getSystem("UseForwardOnlyCursor");
/*  610 */       if (useForwardOnlyCursor != null)
/*      */       {
/*  612 */         data.put("UseForwardOnlyCursor", useForwardOnlyCursor);
/*      */       }
/*      */     }
/*      */ 
/*  616 */     JdbcConnection jCon = getJdbcConnection();
/*  617 */     intradoc.data.ResultSet jdbcRset = null;
/*  618 */     java.sql.ResultSet rset = null;
/*  619 */     JdbcQueryDef qDef = null;
/*  620 */     String queryReportStr = query;
/*  621 */     String additionalMsg = null;
/*  622 */     Statement stmt = null;
/*      */     try
/*      */     {
/*  625 */       String queryStr = null;
/*  626 */       qDef = jCon.getQueryDefAllowMissing(query, true);
/*  627 */       if (qDef != null)
/*      */       {
/*  629 */         if (qDef.m_isPrepared)
/*      */         {
/*  631 */           queryStr = new StringBuilder().append("Executing Query as PreparedStatement (").append(qDef.m_query).append(")").toString();
/*  632 */           reportStartAction(jCon, queryStr, false, false);
/*  633 */           stmt = createStatement(jCon, qDef, data);
/*  634 */           buildPreparedQuery(qDef, args);
/*  635 */           rset = qDef.m_statement.executeQuery();
/*      */         }
/*      */         else
/*      */         {
/*  639 */           queryStr = buildQuery(qDef, args);
/*  640 */           queryReportStr = new StringBuilder().append(query).append("(").append(queryStr).append(")").toString();
/*  641 */           reportStartAction(jCon, queryStr, false, true);
/*  642 */           stmt = createStatement(jCon, null, data);
/*      */ 
/*  644 */           rset = stmt.executeQuery(queryStr);
/*      */         }
/*      */ 
/*  648 */         if (rset == null)
/*      */         {
/*  650 */           String msg = LocaleUtils.encodeMessage("csDbUnableToCreateResultSet", null, queryReportStr);
/*      */ 
/*  652 */           throw new DataException(msg);
/*      */         }
/*      */ 
/*  656 */         jdbcRset = new JdbcResultSet(this.m_manager);
/*  657 */         ((JdbcResultSet)jdbcRset).setQueryInfo(stmt, queryStr, jCon, rset);
/*      */ 
/*  661 */         if (isQueryObjectPersistent.booleanValue())
/*      */         {
/*  663 */           jCon.addPersistObject(jdbcRset);
/*      */         }
/*      */         else
/*      */         {
/*  667 */           jCon.setActiveResultSet((JdbcResultSet)jdbcRset);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  672 */         IdcDataSourceQuery dsQuery = (IdcDataSourceQuery)this.m_dataSourceMap.get(query);
/*  673 */         if (dsQuery != null)
/*      */         {
/*  675 */           jdbcRset = dsQuery.createResultSet(this, args, null);
/*      */         }
/*      */         else
/*      */         {
/*  679 */           throw new DataException(null, "csUnableToFindQueryOrIdcDataSource", new Object[] { query });
/*      */         }
/*      */       }
/*  682 */       additionalMsg = new StringBuilder().append("Executed. Returned row(s): ").append(!jdbcRset.isEmpty()).toString();
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*  686 */       closeStatement(stmt, queryReportStr, null);
/*  687 */       additionalMsg = e.getMessage();
/*  688 */       handleSQLException(e, jCon, "csDbUnableToCreateResultSet", queryReportStr, true);
/*      */     }
/*      */     finally
/*      */     {
/*  693 */       ActiveQueryUtils.removeActiveQuery();
/*  694 */       reportEndAction(jCon, additionalMsg);
/*      */       try
/*      */       {
/*  698 */         if ((qDef != null) && (qDef.m_isPrepared))
/*      */         {
/*  701 */           qDef.m_statement.clearParameters();
/*      */         }
/*      */       }
/*      */       catch (SQLException e)
/*      */       {
/*  706 */         String msg = LocaleUtils.encodeMessage("csDbUnableToClearParameters", e.getMessage(), queryReportStr);
/*      */ 
/*  708 */         this.m_manager.debugMsg(msg);
/*  709 */         throw new DataException(msg, e);
/*      */       }
/*      */ 
/*  715 */       if ((args != null) && (resetCursorFlags.booleanValue()))
/*      */       {
/*  717 */         if (args instanceof DataBinder)
/*      */         {
/*  719 */           ((DataBinder)args).putLocal("IsQueryObjectPersistent", "");
/*  720 */           ((DataBinder)args).putLocal("UseForwardOnlyCursor", "");
/*      */         }
/*  722 */         else if (args instanceof PropParameters)
/*      */         {
/*  724 */           ((PropParameters)args).m_properties.put("IsQueryObjectPersistent", "");
/*  725 */           ((PropParameters)args).m_properties.put("UseForwardOnlyCursor", "");
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  730 */     return jdbcRset;
/*      */   }
/*      */ 
/*      */   protected String buildQuery(JdbcQueryDef qDef, Parameters args)
/*      */     throws DataException
/*      */   {
/*  736 */     return JdbcQueryUtils.buildQuery(qDef, args, this, this.m_manager);
/*      */   }
/*      */ 
/*      */   protected String getParameterValue(String query, Parameters args, QueryParameterInfo param)
/*      */     throws DataException
/*      */   {
/*  743 */     Object obj = JdbcQueryUtils.getParameterValue(query, args, param);
/*  744 */     return obj.toString();
/*      */   }
/*      */ 
/*      */   protected void appendParam(StringBuffer buffer, int type, String value)
/*      */     throws DataException
/*      */   {
/*  750 */     IdcStringBuilder builder = new IdcStringBuilder();
/*  751 */     QueryUtils.appendParam(builder, type, value, this.m_manager);
/*  752 */     buffer.append(builder);
/*      */   }
/*      */ 
/*      */   protected Map buildPreparedQuery(JdbcQueryDef qDef, Parameters args)
/*      */     throws DataException
/*      */   {
/*  758 */     return JdbcQueryUtils.buildPreparedQuery(qDef, args, this, this.m_manager);
/*      */   }
/*      */ 
/*      */   protected void setParameter(PreparedStatement stmt, QueryParameterInfo info, int index, String value, Map map)
/*      */     throws DataException
/*      */   {
/*  764 */     JdbcQueryUtils.setParameter(stmt, info, index, value, this.m_manager, map);
/*      */   }
/*      */ 
/*      */   public intradoc.data.ResultSet createResultSetSQL(String sql) throws DataException
/*      */   {
/*  769 */     return createResultSetSQL(sql, null);
/*      */   }
/*      */ 
/*      */   public intradoc.data.ResultSet createResultSetSQL(String sql, Parameters args) throws DataException
/*      */   {
/*  774 */     HashMap data = new HashMap();
/*  775 */     JdbcConnection jCon = getJdbcConnection();
/*  776 */     JdbcResultSet jdbcRset = null;
/*  777 */     java.sql.ResultSet rset = null;
/*  778 */     Boolean isQueryObjectPersistent = Boolean.valueOf(false);
/*  779 */     Boolean resetCursorFlags = Boolean.valueOf(true);
/*      */ 
/*  781 */     if (args != null)
/*      */     {
/*  784 */       isQueryObjectPersistent = Boolean.valueOf(StringUtils.convertToBool(args.getSystem("IsQueryObjectPersistent"), false));
/*  785 */       resetCursorFlags = Boolean.valueOf(StringUtils.convertToBool(args.getSystem("ResetCursorFlags"), true));
/*      */ 
/*  787 */       String useForwardOnlyCursor = args.getSystem("UseForwardOnlyCursor");
/*  788 */       if (useForwardOnlyCursor != null)
/*      */       {
/*  790 */         data.put("UseForwardOnlyCursor", useForwardOnlyCursor);
/*      */       }
/*      */     }
/*      */ 
/*  794 */     String sqlStr = parseSQL(sql);
/*  795 */     String additionalMsg = null;
/*  796 */     Statement stmt = null;
/*      */     try
/*      */     {
/*  799 */       stmt = createStatement(jCon, null, data);
/*  800 */       reportStartAction(jCon, sqlStr, false, true);
/*  801 */       rset = stmt.executeQuery(sqlStr);
/*      */ 
/*  803 */       if (rset == null)
/*      */       {
/*  805 */         String msg = LocaleUtils.encodeMessage("csDbUnableToCreateResultSet", null, sql);
/*      */ 
/*  807 */         throw new DataException(msg);
/*      */       }
/*      */ 
/*  811 */       jdbcRset = new JdbcResultSet(this.m_manager);
/*  812 */       jdbcRset.setQueryInfo(stmt, sql, jCon, rset);
/*      */ 
/*  816 */       if (isQueryObjectPersistent.booleanValue())
/*      */       {
/*  818 */         jCon.addPersistObject(jdbcRset);
/*      */       }
/*      */       else
/*      */       {
/*  822 */         jCon.setActiveResultSet(jdbcRset);
/*      */       }
/*  824 */       additionalMsg = new StringBuilder().append("Executed. Returned row(s): ").append(!jdbcRset.isEmpty()).toString();
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*  828 */       closeStatement(stmt, sql, null);
/*  829 */       additionalMsg = e.getMessage();
/*  830 */       handleSQLException(e, jCon, "csDbUnableToCreateResultSet", sql, true);
/*      */     }
/*      */     catch (NullPointerException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       String msg;
/*  843 */       ActiveQueryUtils.removeActiveQuery();
/*  844 */       reportEndAction(jCon, additionalMsg);
/*      */ 
/*  850 */       if ((args != null) && (resetCursorFlags.booleanValue()))
/*      */       {
/*  852 */         if (args instanceof DataBinder)
/*      */         {
/*  854 */           ((DataBinder)args).putLocal("IsQueryObjectPersistent", "");
/*  855 */           ((DataBinder)args).putLocal("UseForwardOnlyCursor", "");
/*      */         }
/*  857 */         else if (args instanceof PropParameters)
/*      */         {
/*  859 */           ((PropParameters)args).m_properties.put("IsQueryObjectPersistent", "");
/*  860 */           ((PropParameters)args).m_properties.put("UseForwardOnlyCursor", "");
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  865 */     return jdbcRset;
/*      */   }
/*      */ 
/*      */   public long executeSQL(String sql) throws DataException
/*      */   {
/*  870 */     int result = 0;
/*  871 */     JdbcConnection jCon = getJdbcConnection();
/*      */ 
/*  873 */     String sqlStr = parseSQL(sql);
/*  874 */     Statement stmt = null;
/*  875 */     String additionalMsg = null;
/*      */     try
/*      */     {
/*  878 */       reportStartAction(jCon, sqlStr, true, true);
/*  879 */       stmt = createStatement(jCon, null);
/*  880 */       result = stmt.executeUpdate(sqlStr);
/*  881 */       additionalMsg = new StringBuilder().append("Executed. ").append(result).append(" rows affected.").toString();
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*  885 */       additionalMsg = e.getMessage();
/*  886 */       handleSQLException(e, jCon, "csDbUnableToExecuteSql", sqlStr, true);
/*      */     }
/*      */     finally
/*      */     {
/*  890 */       closeStatement(stmt, null, null);
/*  891 */       reportEndAction(jCon, additionalMsg);
/*  892 */       releaseAccess(jCon);
/*      */     }
/*      */ 
/*  895 */     return result;
/*      */   }
/*      */ 
/*      */   public long executeSQL(String query, Parameters args)
/*      */     throws DataException
/*      */   {
/*  901 */     return executeSQL(query);
/*      */   }
/*      */ 
/*      */   protected Statement createStatement(JdbcConnection jCon, JdbcQueryDef qDef)
/*      */     throws SQLException, DataException
/*      */   {
/*  907 */     return createStatement(jCon, qDef, null);
/*      */   }
/*      */ 
/*      */   protected Statement createStatement(JdbcConnection jCon, JdbcQueryDef qDef, Map data)
/*      */     throws SQLException, DataException
/*      */   {
/*  913 */     Connection con = (Connection)jCon.getConnection();
/*  914 */     if (con == null)
/*      */     {
/*  916 */       return null;
/*      */     }
/*      */ 
/*  919 */     boolean useForwardOnlyCursor = false;
/*  920 */     if (data != null)
/*      */     {
/*  922 */       String useForwardOnlyCursorStr = (String)data.get("UseForwardOnlyCursor");
/*  923 */       useForwardOnlyCursor = StringUtils.convertToBool(useForwardOnlyCursorStr, false);
/*      */     }
/*      */ 
/*  926 */     Statement stmt = null;
/*  927 */     if (qDef != null)
/*      */     {
/*  929 */       if (Report.m_verbose)
/*      */       {
/*  931 */         this.m_manager.debugMsg(new StringBuilder().append("Statement is '").append(qDef.m_statement).append("', and connection is:").append(con).append("QueryDef is:").append(qDef).append(", JdbcConnection is:").append(jCon).toString());
/*      */       }
/*      */ 
/*  934 */       if ((qDef.m_statement == null) || (qDef.m_statement.getConnection() != con))
/*      */       {
/*  936 */         qDef.init(con);
/*  937 */         this.m_manager.debugMsg("New prepared statement is created.");
/*      */       }
/*      */       else
/*      */       {
/*  941 */         this.m_manager.debugMsg("Prepared statement is reused.");
/*      */       }
/*  943 */       stmt = qDef.m_statement;
/*      */     }
/*  947 */     else if (useForwardOnlyCursor)
/*      */     {
/*  949 */       stmt = con.createStatement(1003, 1007);
/*      */     }
/*      */     else
/*      */     {
/*  957 */       stmt = con.createStatement(1004, 1007);
/*      */     }
/*      */ 
/*  962 */     int timeout = getThreadTimeout();
/*  963 */     if (timeout >= 0)
/*      */     {
/*  965 */       stmt.setQueryTimeout(timeout);
/*      */     }
/*  967 */     int size = this.m_config.getValueAsInt("ResultSetPrefetchSize", -1);
/*  968 */     if (size > 0)
/*      */     {
/*  970 */       stmt.setFetchSize(size);
/*      */     }
/*  972 */     ActiveQueryUtils.addActiveQuery(stmt, jCon);
/*      */ 
/*  974 */     return stmt;
/*      */   }
/*      */ 
/*      */   protected boolean isStatementClosed(Statement stmt)
/*      */   {
/*  979 */     boolean isClosed = false;
/*      */ 
/*  981 */     if (stmt != null)
/*      */     {
/*  983 */       if (this.m_useIsClosedStatementCall)
/*      */       {
/*  986 */         Boolean isClosedBoolean = (Boolean)ClassHelperUtils.executeMethodSuppressThrowable(stmt, "isClosed", new Object[0]);
/*      */ 
/*  988 */         if ((isClosedBoolean != null) && (isClosedBoolean.booleanValue()))
/*      */         {
/*  990 */           isClosed = true;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*      */         try
/*      */         {
/*  997 */           stmt.getConnection();
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 1002 */           Report.trace(this.m_manager.m_traceSection, "JDBC Statement was closed by external agent", e);
/*      */         }
/*      */       }
/*      */     }
/* 1006 */     return isClosed;
/*      */   }
/*      */ 
/*      */   protected void closeStatement(Statement stmt, String queryReportStr, Map queryMap) throws DataException
/*      */   {
/* 1012 */     boolean isPrepared = false;
/*      */     Iterator i$;
/*      */     try
/*      */     {
/* 1015 */       if (stmt != null)
/*      */       {
/* 1017 */         if (stmt instanceof PreparedStatement)
/*      */         {
/* 1019 */           isPrepared = true;
/* 1020 */           PreparedStatement pStmt = (PreparedStatement)stmt;
/* 1021 */           pStmt.clearParameters();
/*      */         }
/*      */         else
/*      */         {
/* 1025 */           stmt.close();
/*      */         }
/*      */       }
/*      */ 
/* 1029 */       if (queryMap != null)
/*      */       {
/* 1032 */         List isList = (List)queryMap.get("instreamList");
/* 1033 */         if (isList != null)
/*      */         {
/* 1035 */           for (i$ = isList.iterator(); i$.hasNext(); ) { Object is = i$.next();
/*      */ 
/* 1037 */             FileUtils.closeObject(is); }
/*      */ 
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (SQLException ignore)
/*      */     {
/* 1044 */       if (isPrepared)
/*      */       {
/* 1046 */         String msg = LocaleUtils.encodeMessage("csDbUnableToClearParameters", e.getMessage(), queryReportStr);
/*      */ 
/* 1048 */         this.m_manager.debugMsg(msg);
/* 1049 */         throw new DataException(e, "csDbUnableToClearParameters", new Object[0]);
/*      */       }
/* 1051 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1053 */         Report.debug("systemdatabase", null, e);
/*      */       }
/*      */     }
/*      */     catch (Throwable ignore)
/*      */     {
/* 1058 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1060 */         Report.debug("systemdatabase", null, ignore);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/* 1067 */         ActiveQueryUtils.removeActiveQuery();
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/* 1071 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1073 */           Report.debug("systemdatabase", null, ignore);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void cancel(String queryID) throws DataException
/*      */   {
/* 1081 */     if (queryID == null)
/*      */     {
/* 1083 */       return;
/*      */     }
/* 1085 */     ActiveQueryUtils.cancel(queryID);
/*      */   }
/*      */ 
/*      */   public String getActiveQueryID(String threadName)
/*      */   {
/* 1090 */     return ActiveQueryUtils.getActiveQueryID(threadName);
/*      */   }
/*      */ 
/*      */   public void beginTran() throws DataException
/*      */   {
/* 1095 */     beginTranEx(4);
/*      */   }
/*      */ 
/*      */   public void beginTranEx(int type) throws DataException
/*      */   {
/* 1100 */     JdbcConnection jCon = null;
/* 1101 */     Statement stmt = null;
/* 1102 */     Connection con = null;
/* 1103 */     boolean isError = false;
/*      */     try
/*      */     {
/* 1107 */       if ((type & 0x2) > 0)
/*      */       {
/* 1109 */         this.m_manager.debugMsg("begin tran - hard");
/*      */       }
/* 1111 */       else if ((type & 0x4) > 0)
/*      */       {
/* 1113 */         this.m_manager.debugMsg("begin tran - soft");
/*      */       }
/*      */       else
/*      */       {
/* 1117 */         type |= 4;
/* 1118 */         this.m_manager.debugMsg("begin tran - soft (default forced by beginTranEx())");
/*      */       }
/*      */ 
/* 1121 */       jCon = getJdbcConnection();
/* 1122 */       jCon.markTransactionState(true);
/* 1123 */       this.m_manager.reserveAccess(jCon, true);
/* 1124 */       con = (Connection)jCon.getConnection();
/* 1125 */       if (!con.getAutoCommit())
/*      */       {
/* 1127 */         boolean isSupportsSavepoint = this.m_config.getValueAsBool("SupportsSavePoint", true);
/* 1128 */         if (((!this.m_manager.isInformix()) && (!this.m_manager.isSybase())) || ((type & 0x1) == 0))
/*      */         {
/* 1132 */           if ((!isSupportsSavepoint) || ((type & 0x1) == 0))
/*      */           {
/* 1134 */             throw new SQLException("!csJdbcStartTranWithinATranNotAllowed");
/*      */           }
/*      */ 
/* 1137 */           this.m_manager.debugMsg("Starting new transaction within a transaction, save transaction point.");
/* 1138 */           Savepoint sp = con.setSavepoint();
/* 1139 */           addTransactionSavepoint(sp);
/*      */         }
/*      */         else
/*      */         {
/* 1143 */           String key = Thread.currentThread().getName();
/* 1144 */           addOrRemoveFakeSavepoints(key, true);
/* 1145 */           this.m_useFakeSavepoints = true;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1150 */         con.setAutoCommit(false);
/* 1151 */         String state = "In Soft Transaction";
/* 1152 */         if ((type & 0x2) > 0)
/*      */         {
/* 1154 */           stmt = createStatement(jCon, null);
/*      */ 
/* 1157 */           String lockDatabase = "UPDATE Counters SET dNextIndex = (dNextIndex+1) WHERE dCounterName='TranLock'";
/*      */ 
/* 1159 */           lockDatabase = parseSQL(lockDatabase);
/* 1160 */           int result = stmt.executeUpdate(lockDatabase);
/* 1161 */           if (result == 0)
/*      */           {
/* 1163 */             this.m_manager.debugMsg("Created lock in database");
/* 1164 */             String insertLock = "INSERT INTO Counters (dCounterName, dNextIndex) values ('TranLock', 0)";
/*      */ 
/* 1166 */             insertLock = parseSQL(insertLock);
/* 1167 */             stmt.executeUpdate(insertLock);
/*      */           }
/* 1169 */           if (Report.m_verbose)
/*      */           {
/* 1171 */             this.m_manager.debugMsg("tranLock updated");
/*      */           }
/* 1173 */           state = "In Transaction";
/*      */         }
/* 1175 */         jCon.setCurrentState(state);
/*      */       }
/*      */     }
/*      */     catch (SQLException t)
/*      */     {
/* 1180 */       isError = true;
/* 1181 */       handleSQLException(e, jCon, "csDbUnableToStartTransaction", "", true);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/* 1191 */       if (isError)
/*      */       {
/*      */         try
/*      */         {
/* 1195 */           this.m_manager.debugMsg("Unable to start transaction, reset back to auto commit");
/* 1196 */           con.setAutoCommit(true);
/*      */         }
/*      */         catch (Throwable t)
/*      */         {
/* 1200 */           this.m_manager.debugMsg("Unable to reset connection back to auto commit");
/*      */         }
/*      */       }
/* 1203 */       closeStatement(stmt, null, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void commitTran() throws DataException
/*      */   {
/* 1209 */     Thread thrd = Thread.currentThread();
/* 1210 */     String name = thrd.getName();
/* 1211 */     JdbcConnection jCon = getJdbcConnectionEx(name, 1, true);
/* 1212 */     if (jCon == null)
/*      */     {
/* 1214 */       this.m_manager.debugMsg(new StringBuilder().append("Unable to commit tran. No active connection is associated with the calling thread '").append(name).append("'").toString());
/*      */ 
/* 1216 */       String msg = LocaleUtils.encodeMessage("csJdbcCommitCalledWithoutActiveConnection", null, name);
/* 1217 */       Report.error(null, msg, new DataException(msg));
/* 1218 */       return;
/*      */     }
/*      */ 
/* 1221 */     if (Report.m_verbose)
/*      */     {
/* 1223 */       this.m_manager.debugMsg("commit called.");
/*      */     }
/*      */ 
/* 1226 */     Connection con = (Connection)jCon.getConnection();
/* 1227 */     boolean isNested = false;
/* 1228 */     int spID = -1;
/*      */     try
/*      */     {
/* 1231 */       if (con.getAutoCommit())
/*      */       {
/* 1233 */         String msg = LocaleUtils.encodeMessage("csJdbcCommitCalledInAutoCommitMode", null, name);
/*      */ 
/* 1235 */         Report.error(null, null, new DataException(msg));
/*      */       }
/* 1237 */       Savepoint sp = removeTransactionSavepoint();
/* 1238 */       if ((sp != null) || ((this.m_useFakeSavepoints) && (addOrRemoveFakeSavepoints(name, false) != null)))
/*      */       {
/* 1241 */         isNested = true;
/* 1242 */         if ((this.m_config.getValueAsBool("SupportReleaseSavepoint", true)) && (!this.m_useFakeSavepoints))
/*      */         {
/* 1244 */           spID = sp.getSavepointId();
/* 1245 */           con.releaseSavepoint(sp);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1251 */         con.commit();
/*      */       }
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/* 1256 */       handleSQLException(e, jCon, "csJdbcCommitError", "", true);
/*      */     }
/*      */     finally
/*      */     {
/* 1260 */       if (!isNested)
/*      */       {
/* 1262 */         setToNoCommit(con);
/* 1263 */         jCon.markTransactionState(false);
/* 1264 */         this.m_manager.releaseAccess(jCon, true);
/* 1265 */         this.m_manager.debugMsg("tran committed");
/* 1266 */         jCon.setCurrentState("Not In Transaction");
/*      */       }
/*      */       else
/*      */       {
/* 1270 */         this.m_manager.debugMsg(new StringBuilder().append("release nested tran: ").append(spID).toString());
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addTransactionSavepoint(Savepoint sp)
/*      */   {
/* 1278 */     if (sp == null)
/*      */     {
/* 1280 */       return;
/*      */     }
/* 1282 */     synchronized (this.m_spSyncObj)
/*      */     {
/* 1284 */       String key = Thread.currentThread().getName();
/* 1285 */       Stack stack = (Stack)this.m_savedPoints.get(key);
/*      */ 
/* 1287 */       if (stack == null)
/*      */       {
/* 1289 */         stack = new Stack();
/* 1290 */         this.m_savedPoints.put(key, stack);
/*      */       }
/* 1292 */       stack.push(sp);
/*      */     }
/*      */ 
/* 1295 */     int spID = -1;
/*      */     try
/*      */     {
/* 1298 */       spID = sp.getSavepointId();
/*      */     }
/*      */     catch (SQLException ignore)
/*      */     {
/* 1302 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1304 */         Report.debug("systemdatabase", null, ignore);
/*      */       }
/*      */     }
/* 1307 */     this.m_manager.debugMsg(new StringBuilder().append("Added nested transaction ").append(spID).append(".").toString());
/*      */   }
/*      */ 
/*      */   protected Savepoint removeTransactionSavepoint()
/*      */   {
/* 1312 */     Savepoint sp = null;
/* 1313 */     synchronized (this.m_spSyncObj)
/*      */     {
/* 1315 */       String key = Thread.currentThread().getName();
/* 1316 */       Stack stack = (Stack)this.m_savedPoints.get(key);
/* 1317 */       if ((stack != null) && (stack.size() > 0))
/*      */       {
/* 1319 */         sp = (Savepoint)stack.pop();
/*      */       }
/*      */     }
/* 1322 */     if (sp != null)
/*      */     {
/* 1324 */       int spID = -1;
/*      */       try
/*      */       {
/* 1327 */         spID = sp.getSavepointId();
/*      */       }
/*      */       catch (SQLException ignore)
/*      */       {
/* 1331 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1333 */           Report.debug("systemdatabase", null, ignore);
/*      */         }
/*      */       }
/* 1336 */       this.m_manager.debugMsg(new StringBuilder().append("Removed nested transaction ").append(spID).append(".").toString());
/*      */     }
/* 1338 */     return sp;
/*      */   }
/*      */ 
/*      */   protected Object addOrRemoveFakeSavepoints(String key, boolean isAdd)
/*      */   {
/* 1343 */     Stack stack = (Stack)this.m_fakeSavepoints.get(key);
/* 1344 */     if ((stack == null) && (isAdd))
/*      */     {
/* 1346 */       stack = new Stack();
/* 1347 */       this.m_fakeSavepoints.put(key, stack);
/*      */     }
/*      */ 
/* 1350 */     Object obj = null;
/* 1351 */     if (stack != null)
/*      */     {
/* 1353 */       if (isAdd)
/*      */       {
/* 1355 */         stack.push(key);
/*      */       }
/* 1357 */       else if (!stack.empty())
/*      */       {
/* 1359 */         obj = stack.pop();
/*      */       }
/*      */     }
/*      */ 
/* 1363 */     return obj;
/*      */   }
/*      */ 
/*      */   public void setToNoCommit(Connection con)
/*      */   {
/*      */     try
/*      */     {
/* 1371 */       con.setAutoCommit(true);
/*      */     }
/*      */     catch (SQLException ignore)
/*      */     {
/* 1376 */       if (!SystemUtils.m_verbose)
/*      */         return;
/* 1378 */       Report.debug("systemdatabase", null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void rollbackTran()
/*      */   {
/*      */     try
/*      */     {
/* 1388 */       Thread thrd = Thread.currentThread();
/* 1389 */       String name = thrd.getName();
/* 1390 */       JdbcConnection jCon = getJdbcConnectionEx(name, 1, true);
/* 1391 */       if (jCon != null)
/*      */       {
/* 1393 */         rollbackTranEx(jCon);
/*      */       }
/*      */       else
/*      */       {
/* 1397 */         this.m_manager.debugMsg(new StringBuilder().append("Unable to roll back transaction. No active connection associated with current thread '").append(name).append("'").toString());
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/* 1403 */       Report.trace("systemdatabase", null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void rollbackTranEx(JdbcConnection jCon)
/*      */   {
/* 1409 */     boolean isNested = false;
/* 1410 */     Connection con = null;
/* 1411 */     int spID = -1;
/*      */     try
/*      */     {
/* 1414 */       con = (Connection)jCon.getConnection();
/* 1415 */       Savepoint sp = removeTransactionSavepoint();
/* 1416 */       if ((sp != null) || ((this.m_useFakeSavepoints) && (addOrRemoveFakeSavepoints(Thread.currentThread().getName(), false) != null)))
/*      */       {
/* 1420 */         isNested = true;
/* 1421 */         if (!this.m_useFakeSavepoints)
/*      */         {
/* 1423 */           spID = sp.getSavepointId();
/* 1424 */           con.rollback(sp);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1430 */         con.rollback();
/* 1431 */         this.m_manager.releaseAccess(jCon, true);
/*      */       }
/*      */     }
/*      */     catch (SQLException ignore)
/*      */     {
/* 1436 */       jCon.determineConnectionState(ignore);
/*      */ 
/* 1439 */       Report.trace("systemdatabase", null, ignore);
/*      */     }
/*      */     finally
/*      */     {
/* 1443 */       if (!isNested)
/*      */       {
/* 1445 */         setToNoCommit(con);
/* 1446 */         jCon.markTransactionState(false);
/* 1447 */         jCon.setCurrentState("Not In Transaction");
/* 1448 */         this.m_manager.debugMsg("rollback tran");
/*      */       }
/*      */       else
/*      */       {
/* 1452 */         this.m_manager.debugMsg(new StringBuilder().append("rollback tran savepoint ").append(spID).toString());
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public String[] getQueryList()
/*      */   {
/* 1463 */     String[] ret = null;
/*      */     int i;
/*      */     try {
/* 1466 */       Map qDefs = this.m_manager.getQueryDefs();
/* 1467 */       int size = qDefs.size();
/* 1468 */       ret = new String[size];
/* 1469 */       i = 0;
/*      */ 
/* 1471 */       for (JdbcQueryDef qDef : qDefs.values())
/*      */       {
/* 1473 */         ret[(i++)] = qDef.m_name;
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1478 */       String msg = LocaleUtils.encodeMessage("csDbFailedToGetQueryList", t.getMessage());
/*      */ 
/* 1480 */       this.m_manager.debugMsg(msg);
/* 1481 */       Error err = new Error(msg);
/* 1482 */       SystemUtils.setExceptionCause(err, t);
/*      */     }
/*      */ 
/* 1485 */     return ret;
/*      */   }
/*      */ 
/*      */   public String[] getTableList() throws DataException
/*      */   {
/* 1490 */     JdbcConnection jCon = getJdbcConnection();
/*      */     String catalog;
/*      */     try {
/* 1493 */       Connection con = (Connection)jCon.getConnection();
/*      */ 
/* 1495 */       catalog = con.getCatalog();
/*      */ 
/* 1497 */       DatabaseMetaData dbMetaData = con.getMetaData();
/*      */ 
/* 1499 */       String schema = JdbcFunctions.getUserSchema(dbMetaData, this.m_manager);
/* 1500 */       String[] arrayOfString = JdbcFunctions.getTableListInternal(this.m_manager, dbMetaData, catalog, schema);
/*      */ 
/* 1509 */       return arrayOfString;
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/* 1504 */       handleSQLException(e, jCon, "csDbFailedToGetTableList", "", true);
/* 1505 */       catalog = null;
/*      */ 
/* 1509 */       return catalog; } finally { releaseAccess(jCon); }
/*      */ 
/*      */   }
/*      */ 
/*      */   public void createTable(String table, FieldInfo[] cols, String[] pkCols)
/*      */     throws DataException
/*      */   {
/* 1519 */     JdbcConnection jCon = null;
/*      */ 
/* 1521 */     String additionalMsg = null;
/*      */     try
/*      */     {
/* 1524 */       jCon = getJdbcConnection();
/* 1525 */       Connection con = (Connection)jCon.getConnection();
/*      */ 
/* 1528 */       Properties columnMap = this.m_manager.getColumnMap();
/* 1529 */       Vector tableFields = new IdcVector();
/* 1530 */       tableFields.setSize(cols.length);
/* 1531 */       for (int i = 0; i < cols.length; ++i)
/*      */       {
/* 1533 */         FieldInfo newField = cols[i];
/* 1534 */         String existingName = (String)columnMap.get(newField.m_name.toUpperCase());
/* 1535 */         if (existingName != null)
/*      */         {
/* 1539 */           newField.m_name = existingName;
/*      */         }
/* 1541 */         tableFields.setElementAt(newField, i);
/*      */       }
/*      */ 
/* 1545 */       String pkConstraintString = createPrimaryKeyConstraintString(pkCols);
/*      */ 
/* 1548 */       IdcStringBuilder tableDef = new IdcStringBuilder();
/* 1549 */       IdcStringBuilder tableDefSuffix = new IdcStringBuilder();
/* 1550 */       JdbcFunctions.appendTableDefEx(tableDef, tableDefSuffix, tableFields, pkCols, 0, tableFields.size(), this.m_manager, true, false);
/*      */ 
/* 1552 */       String pkConstraintName = createPrimaryKeyConstraintName(table);
/*      */ 
/* 1554 */       reportStartAction(jCon, new StringBuilder().append(tableDef).append("|").append(pkConstraintName).append("|").append(pkConstraintString).toString(), true, false);
/*      */ 
/* 1557 */       JdbcFunctions.executeCreateTableQuery(con, table, tableDef.toString(), pkConstraintName, pkConstraintString, tableDefSuffix.toString(), this.m_manager);
/*      */ 
/* 1560 */       updateColumnMapping(tableFields, columnMap);
/* 1561 */       additionalMsg = "Executed.";
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/* 1565 */       additionalMsg = e.getMessage();
/* 1566 */       handleSQLException(e, jCon, "csDbUnableToPerformAction_create", table, true);
/*      */     }
/*      */     finally
/*      */     {
/* 1570 */       if (jCon != null)
/*      */       {
/* 1572 */         reportEndAction(jCon, additionalMsg);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String createPrimaryKeyConstraintName(String name)
/*      */   {
/* 1583 */     name = new StringBuilder().append("PK_").append(name).toString();
/* 1584 */     if (this.m_manager.useShortIndexName())
/*      */     {
/* 1586 */       name = getShortIndexName(name);
/*      */     }
/* 1588 */     return name;
/*      */   }
/*      */ 
/*      */   protected String getShortIndexName(String name)
/*      */   {
/* 1593 */     int length = this.m_config.getValueAsInt("ShortIndexNameLen", 30);
/*      */ 
/* 1595 */     return WorkspaceUtils.getShortIndexName(name, length);
/*      */   }
/*      */ 
/*      */   public void deleteTable(String table)
/*      */     throws DataException
/*      */   {
/* 1603 */     JdbcConnection jCon = null;
/* 1604 */     String additionalMsg = null;
/*      */     try
/*      */     {
/* 1607 */       jCon = getJdbcConnection();
/* 1608 */       reportStartAction(jCon, new StringBuilder().append("Delete ").append(table).toString(), true, false);
/* 1609 */       Connection con = (Connection)jCon.getConnection();
/*      */ 
/* 1611 */       String catalog = con.getCatalog();
/*      */ 
/* 1613 */       DatabaseMetaData dbMetaData = con.getMetaData();
/* 1614 */       String schema = JdbcFunctions.getUserSchema(dbMetaData, this.m_manager);
/* 1615 */       String[] tableList = JdbcFunctions.getTableListInternal(this.m_manager, dbMetaData, catalog, schema);
/*      */ 
/* 1617 */       int index = StringUtils.findStringIndexEx(tableList, table, true);
/* 1618 */       if (index < 0)
/*      */       {
/*      */         return;
/*      */       }
/*      */ 
/* 1623 */       table = tableList[index];
/*      */ 
/* 1626 */       JdbcFunctions.dropTableInternal(con, table, this.m_manager);
/* 1627 */       additionalMsg = "Executed";
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/* 1631 */       additionalMsg = e.getMessage();
/* 1632 */       handleSQLException(e, jCon, "csDbUnableToPerformAction_delete", table, true);
/*      */     }
/*      */     finally
/*      */     {
/* 1636 */       if (jCon != null)
/*      */       {
/* 1638 */         reportEndAction(jCon, additionalMsg);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void alterTable(String table, FieldInfo[] addCols, String[] dropCols, String[] pkCols)
/*      */     throws DataException
/*      */   {
/* 1646 */     String origTable = table;
/*      */ 
/* 1648 */     JdbcConnection jCon = null;
/* 1649 */     String additionalMsg = null;
/* 1650 */     boolean recreateEBRView = false;
/*      */     try
/*      */     {
/* 1654 */       jCon = getJdbcConnection();
/* 1655 */       Connection con = (Connection)jCon.getConnection();
/* 1656 */       reportStartAction(jCon, new StringBuilder().append("Alter ").append(table).toString(), true, false);
/*      */ 
/* 1659 */       if ((WorkspaceUtils.EBRModeActive(this)) && 
/* 1663 */         (JdbcFunctions.hasEditioningView(jCon, origTable, this.m_manager)))
/*      */       {
/* 1666 */         if (SharedObjects.getEnvValueAsBoolean("EBRdropViewAllowed", true))
/*      */         {
/* 1674 */           JdbcFunctions.dropViewInternal(con, origTable, this.m_manager);
/*      */ 
/* 1676 */           recreateEBRView = true;
/*      */ 
/* 1678 */           table = new StringBuilder().append(table).append("_").toString();
/*      */         }
/*      */         else
/*      */         {
/* 1684 */           Report.trace(null, new StringBuilder().append("Attempt to alter table ").append(table).append(" rejected!  We are in EBRMode and EBRdropViewAllowed has not been set.").toString(), null);
/*      */           return;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1690 */       String catalog = con.getCatalog();
/*      */ 
/* 1694 */       this.m_manager.setAllowEmptyTableList(false);
/*      */ 
/* 1696 */       DatabaseMetaData dbMetaData = con.getMetaData();
/* 1697 */       String schema = JdbcFunctions.getUserSchema(dbMetaData, this.m_manager);
/* 1698 */       String[] tableList = JdbcFunctions.getTableListInternal(this.m_manager, dbMetaData, catalog, schema);
/*      */ 
/* 1700 */       int index = StringUtils.findStringIndexEx(tableList, table, true);
/* 1701 */       if (index < 0)
/*      */       {
/* 1703 */         throw new SQLException("!csDbTableMissing");
/*      */       }
/*      */ 
/* 1706 */       table = tableList[index];
/*      */ 
/* 1709 */       Vector tableFields = JdbcFunctions.getFieldListInternal(jCon, this.m_manager, table);
/*      */ 
/* 1714 */       String tableTemp = new StringBuilder().append(table).append("Temp").toString();
/* 1715 */       index = StringUtils.findStringIndexEx(tableList, tableTemp, true);
/* 1716 */       if (index >= 0)
/*      */       {
/* 1719 */         String msg = LocaleUtils.encodeMessage("csJdbcTempTableExists", null, tableTemp, origTable);
/* 1720 */         this.m_manager.debugMsg(msg);
/* 1721 */         SQLException e = new SQLException(msg);
/* 1722 */         Report.error(null, null, e);
/* 1723 */         throw e;
/*      */       }
/*      */ 
/* 1727 */       String[] keyName = new String[1];
/*      */ 
/* 1735 */       boolean canDelete = this.m_manager.supportsSqlColumnDelete();
/* 1736 */       boolean canChange = this.m_manager.supportsSqlColumnChange();
/* 1737 */       boolean doFullTempCopy = false;
/*      */ 
/* 1741 */       boolean[] isChangedField = null;
/* 1742 */       boolean[] isRequiredField = null;
/* 1743 */       boolean[] isSameLength = null;
/* 1744 */       if (addCols != null)
/*      */       {
/* 1746 */         isChangedField = new boolean[addCols.length];
/* 1747 */         isRequiredField = new boolean[addCols.length];
/* 1748 */         isSameLength = new boolean[addCols.length];
/* 1749 */         for (int i = 0; i < addCols.length; ++i)
/*      */         {
/* 1751 */           isChangedField[i] = false;
/* 1752 */           isRequiredField[i] = ((StringUtils.findStringIndexEx(pkCols, addCols[i].m_name, true) >= 0) ? 1 : false);
/* 1753 */           if (isRequiredField[i] != 0)
/*      */           {
/* 1755 */             doFullTempCopy = true;
/*      */           }
/* 1757 */           isSameLength[i] = false;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1763 */       Vector dropColsList = new IdcVector();
/* 1764 */       Vector coreTableFields = new IdcVector();
/* 1765 */       for (int i = 0; i < tableFields.size(); ++i)
/*      */       {
/* 1767 */         FieldInfo fi = (FieldInfo)tableFields.elementAt(i);
/* 1768 */         String fName = fi.m_name;
/*      */ 
/* 1770 */         if (fName.startsWith("TEMP_"))
/*      */         {
/* 1773 */           attemptRepairTempField(con, table, tableFields, fi);
/* 1774 */           if (canDelete)
/*      */           {
/* 1776 */             JdbcFunctions.dropColumns(con, table, new String[] { fName }, this.m_manager);
/*      */           }
/*      */           else
/*      */           {
/* 1780 */             dropColsList.addElement(fName);
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 1786 */           boolean isConflictingAdd = false;
/* 1787 */           boolean isFound = false;
/* 1788 */           boolean isChanged = false;
/* 1789 */           boolean isSameLen = false;
/* 1790 */           boolean isCoreField = true;
/* 1791 */           FieldInfo coreDef = fi;
/*      */ 
/* 1793 */           if (addCols != null)
/*      */           {
/* 1795 */             for (int j = 0; j < addCols.length; ++j)
/*      */             {
/* 1797 */               if (!addCols[j].m_name.equalsIgnoreCase(fName))
/*      */                 continue;
/* 1799 */               if (isFound)
/*      */               {
/* 1801 */                 String msg = LocaleUtils.encodeMessage("csDbFieldAlreadyExists", null, fName);
/*      */ 
/* 1803 */                 throw new DataException(msg);
/*      */               }
/* 1805 */               isFound = true;
/* 1806 */               if ((addCols[j].m_type == 6) && (fi.m_type == 6))
/*      */               {
/* 1814 */                 int addMaxLen = addCols[j].m_maxLen;
/* 1815 */                 if ((addMaxLen >= fi.m_maxLen) || ((addMaxLen <= 0) && (JdbcFunctions.m_memoLength >= fi.m_maxLen)))
/*      */                 {
/* 1818 */                   isChanged = true;
/* 1819 */                   isChangedField[j] = true;
/* 1820 */                   isSameLen = (fi.m_maxLen == addMaxLen) || ((addMaxLen <= 0) && (fi.m_maxLen == JdbcFunctions.m_memoLength));
/*      */ 
/* 1822 */                   isSameLength[j] = isSameLen;
/* 1823 */                   coreDef = addCols[j];
/*      */                 }
/*      */               }
/* 1826 */               else if (((addCols[j].m_type == 3) && (fi.m_type == 3)) || ((addCols[j].m_type == 11) && (fi.m_type == 11) && 
/* 1832 */                 (fi.m_maxLen < 19)))
/*      */               {
/* 1834 */                 isChanged = true;
/* 1835 */                 isChangedField[j] = true;
/* 1836 */                 isSameLength[j] = false;
/* 1837 */                 coreDef = addCols[j];
/*      */               }
/*      */ 
/* 1840 */               if (isChanged)
/*      */                 continue;
/* 1842 */               isConflictingAdd = true;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1848 */           if (isChanged)
/*      */           {
/* 1850 */             if ((!canChange) && (!isSameLen))
/*      */             {
/* 1852 */               if (canDelete)
/*      */               {
/* 1856 */                 dropColsList.addElement(fName);
/*      */               }
/*      */               else
/*      */               {
/* 1860 */                 doFullTempCopy = true;
/*      */               }
/*      */             }
/*      */           }
/* 1864 */           else if ((((!isConflictingAdd) || (this.m_config.getValueAsBool("AllowAlterTableDropColumn", false)))) && (dropCols != null))
/*      */           {
/* 1866 */             for (int j = 0; j < dropCols.length; ++j)
/*      */             {
/* 1868 */               if (!fName.equalsIgnoreCase(dropCols[j]))
/*      */                 continue;
/* 1870 */               dropColsList.addElement(fName);
/* 1871 */               isConflictingAdd = false;
/* 1872 */               isCoreField = false;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1877 */           if (isConflictingAdd)
/*      */           {
/* 1879 */             String msg = LocaleUtils.encodeMessage("csDbFieldAlreadyExists2", null, fName);
/*      */ 
/* 1881 */             this.m_manager.debugMsg(msg);
/* 1882 */             throw new DataException(msg);
/*      */           }
/* 1884 */           if (!isCoreField)
/*      */             continue;
/* 1886 */           coreTableFields.addElement(coreDef);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1891 */       if ((dropColsList.size() > 0) && (!canDelete))
/*      */       {
/* 1893 */         doFullTempCopy = true;
/*      */       }
/*      */ 
/* 1897 */       boolean isAltered = false;
/*      */ 
/* 1901 */       if (doFullTempCopy)
/*      */       {
/* 1903 */         int numCoreFields = coreTableFields.size();
/* 1904 */         Vector newTableFields = new IdcVector();
/* 1905 */         Vector primaryFieldsAdded = new IdcVector();
/* 1906 */         for (i = 0; i < numCoreFields; ++i)
/*      */         {
/* 1908 */           newTableFields.addElement(coreTableFields.elementAt(i));
/*      */         }
/*      */ 
/* 1911 */         if (addCols != null)
/*      */         {
/* 1913 */           for (i = 0; i < addCols.length; ++i)
/*      */           {
/* 1915 */             if (isChangedField[i] != 0)
/*      */               continue;
/* 1917 */             newTableFields.addElement(addCols[i]);
/* 1918 */             if (isRequiredField[i] == 0)
/*      */               continue;
/* 1920 */             primaryFieldsAdded.addElement(addCols[i]);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1929 */         if (keyName[0] == null)
/*      */         {
/* 1931 */           keyName[0] = createPrimaryKeyConstraintName(table);
/*      */         }
/*      */ 
/* 1935 */         String pkConstraintString = createPrimaryKeyConstraintString(pkCols);
/*      */ 
/* 1938 */         IdcStringBuilder tableDef = new IdcStringBuilder();
/* 1939 */         IdcStringBuilder tableDefSuffix = new IdcStringBuilder();
/* 1940 */         JdbcFunctions.appendTableDefEx(tableDef, tableDefSuffix, newTableFields, pkCols, 0, newTableFields.size(), this.m_manager, true, false);
/*      */ 
/* 1942 */         String tempPkConstraintName = createPrimaryKeyConstraintName(tableTemp);
/*      */ 
/* 1945 */         JdbcFunctions.executeCreateTableQuery(con, tableTemp, tableDef.toString(), tempPkConstraintName, pkConstraintString, tableDefSuffix.toString(), this.m_manager);
/*      */ 
/* 1949 */         Vector copyFields = new IdcVector();
/* 1950 */         for (i = 0; i < numCoreFields; ++i)
/*      */         {
/* 1952 */           copyFields.addElement(newTableFields.elementAt(i));
/*      */         }
/*      */ 
/* 1955 */         int primaryAdded = primaryFieldsAdded.size();
/* 1956 */         String[] useDefaultFields = new String[primaryAdded];
/* 1957 */         for (i = 0; i < primaryAdded; ++i)
/*      */         {
/* 1959 */           FieldInfo fi = (FieldInfo)primaryFieldsAdded.elementAt(i);
/* 1960 */           copyFields.addElement(fi);
/* 1961 */           useDefaultFields[i] = fi.m_name;
/*      */         }
/* 1963 */         int nCopyFields = copyFields.size();
/*      */ 
/* 1966 */         String insertFieldList = JdbcFunctions.createSqlFieldList(copyFields, 0, nCopyFields);
/*      */ 
/* 1968 */         String selectFieldList = JdbcFunctions.createSqlFieldListEx(copyFields, 0, nCopyFields, useDefaultFields);
/*      */ 
/* 1971 */         JdbcFunctions.executeTableCopyQueryEx(con, table, tableTemp, insertFieldList, selectFieldList, this.m_manager);
/*      */ 
/* 1975 */         JdbcFunctions.dropTableInternal(con, table, this.m_manager);
/*      */ 
/* 1978 */         JdbcFunctions.executeCreateTableQuery(con, table, tableDef.toString(), keyName[0], pkConstraintString, tableDefSuffix.toString(), this.m_manager);
/*      */ 
/* 1982 */         JdbcFunctions.executeTableCopyQuery(con, tableTemp, table, insertFieldList);
/*      */ 
/* 1985 */         JdbcFunctions.dropTableInternal(con, tableTemp, this.m_manager);
/* 1986 */         isAltered = true;
/*      */       }
/*      */       else
/*      */       {
/* 1993 */         boolean hasChangedField = false;
/* 1994 */         String updateQueryTo = null;
/* 1995 */         String updateQueryFrom = null;
/* 1996 */         Vector fieldsToChange = new IdcVector();
/* 1997 */         Vector fieldsToAdd = new IdcVector();
/* 1998 */         if (addCols != null)
/*      */         {
/* 2000 */           if (!canChange)
/*      */           {
/* 2002 */             updateQueryTo = new StringBuilder().append("UPDATE ").append(table).append(" SET ").toString();
/* 2003 */             updateQueryFrom = updateQueryTo;
/*      */           }
/* 2005 */           for (i = 0; i < addCols.length; ++i)
/*      */           {
/* 2007 */             boolean mustAdd = isSameLength[i] == 0;
/* 2008 */             if ((isChangedField[i] != 0) && (mustAdd))
/*      */             {
/* 2010 */               FieldInfo fi = new FieldInfo();
/* 2011 */               fi.copy(addCols[i]);
/* 2012 */               if (canChange)
/*      */               {
/* 2014 */                 mustAdd = false;
/*      */               }
/*      */               else
/*      */               {
/* 2018 */                 if (hasChangedField)
/*      */                 {
/* 2020 */                   updateQueryTo = new StringBuilder().append(updateQueryTo).append(", ").toString();
/* 2021 */                   updateQueryFrom = new StringBuilder().append(updateQueryFrom).append(", ").toString();
/*      */                 }
/* 2023 */                 fi.m_name = new StringBuilder().append("TEMP_").append(fi.m_name).toString();
/* 2024 */                 updateQueryTo = new StringBuilder().append(updateQueryTo).append(fi.m_name).append(" = ").append(addCols[i].m_name).toString();
/* 2025 */                 updateQueryFrom = new StringBuilder().append(updateQueryFrom).append(addCols[i].m_name).append(" = ").append(fi.m_name).toString();
/*      */               }
/* 2027 */               fieldsToChange.addElement(fi);
/* 2028 */               hasChangedField = true;
/*      */             }
/*      */ 
/* 2031 */             if (!mustAdd)
/*      */               continue;
/* 2033 */             fieldsToAdd.addElement(addCols[i]);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 2038 */         if (hasChangedField)
/*      */         {
/* 2040 */           String command = " ADD ";
/* 2041 */           boolean isChange = false;
/* 2042 */           if (canChange)
/*      */           {
/* 2044 */             isChange = true;
/* 2045 */             command = new StringBuilder().append(" ").append(this.m_manager.getColumnChangeSqlCommand()).append(" ").toString();
/*      */           }
/* 2047 */           JdbcFunctions.executeAlterTable(con, table, fieldsToChange, command, this.m_manager, isChange);
/*      */ 
/* 2049 */           isAltered = true;
/*      */ 
/* 2051 */           if (!canChange)
/*      */           {
/* 2054 */             JdbcFunctions.executeUpdateQuery(con, updateQueryTo, this.m_manager);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 2059 */         if (dropColsList.size() > 0)
/*      */         {
/* 2063 */           String[] dropColsArray = StringUtils.convertListToArray(dropColsList);
/* 2064 */           JdbcFunctions.dropColumns(con, table, dropColsArray, this.m_manager);
/* 2065 */           isAltered = true;
/*      */         }
/*      */ 
/* 2069 */         if (fieldsToAdd.size() > 0)
/*      */         {
/* 2071 */           String command = " ADD ";
/*      */ 
/* 2075 */           Properties columnMap = this.m_manager.getColumnMap();
/* 2076 */           int numToAdd = fieldsToAdd.size();
/* 2077 */           for (int k = 0; k < numToAdd; ++k)
/*      */           {
/* 2079 */             FieldInfo newField = (FieldInfo)fieldsToAdd.elementAt(k);
/* 2080 */             String newColumn = newField.m_name;
/* 2081 */             String existingName = (String)columnMap.get(newColumn.toUpperCase());
/* 2082 */             if (existingName == null)
/*      */               continue;
/* 2084 */             newField.m_name = existingName;
/*      */           }
/*      */ 
/* 2087 */           JdbcFunctions.executeAlterTable(con, table, fieldsToAdd, command, this.m_manager, false);
/*      */ 
/* 2089 */           isAltered = true;
/*      */ 
/* 2092 */           updateColumnMapping(fieldsToAdd, columnMap);
/*      */         }
/*      */ 
/* 2095 */         if ((hasChangedField) && (!canChange))
/*      */         {
/* 2098 */           JdbcFunctions.executeUpdateQuery(con, updateQueryFrom, this.m_manager);
/* 2099 */           String[] dropChangedTempArray = new String[fieldsToChange.size()];
/* 2100 */           for (i = 0; i < fieldsToChange.size(); ++i)
/*      */           {
/* 2102 */             FieldInfo fi = (FieldInfo)fieldsToChange.elementAt(i);
/* 2103 */             dropChangedTempArray[i] = fi.m_name;
/*      */           }
/*      */ 
/* 2106 */           JdbcFunctions.dropColumns(con, table, dropChangedTempArray, this.m_manager);
/* 2107 */           isAltered = true;
/*      */         }
/*      */       }
/* 2110 */       if (isAltered)
/*      */       {
/* 2112 */         additionalMsg = new StringBuilder().append("Table '").append(table).append("' is altered.").toString();
/*      */       }
/*      */       else
/*      */       {
/* 2116 */         additionalMsg = new StringBuilder().append("Table '").append(table).append("' is not altered.").toString();
/*      */       }
/*      */ 
/* 2119 */       if (recreateEBRView)
/*      */       {
/* 2122 */         table = table.substring(0, table.length() - 1);
/* 2123 */         JdbcFunctions.createEditioningViewInternal(jCon, table, this.m_manager);
/*      */       }
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/* 2128 */       additionalMsg = e.getMessage();
/* 2129 */       handleSQLException(e, jCon, "csDbUnableToPerformAction_alter", table, true);
/*      */     }
/*      */     finally
/*      */     {
/* 2133 */       if (jCon != null)
/*      */       {
/* 2135 */         reportEndAction(jCon, additionalMsg);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void attemptRepairTempField(Connection con, String table, Vector tableFields, FieldInfo tempField)
/*      */   {
/* 2143 */     String field = tempField.m_name.substring(5);
/* 2144 */     boolean isPresent = false;
/* 2145 */     for (int i = 0; i < tableFields.size(); ++i)
/*      */     {
/* 2147 */       FieldInfo fi = (FieldInfo)tableFields.elementAt(i);
/* 2148 */       String fName = fi.m_name;
/* 2149 */       if ((!fName.equalsIgnoreCase(field)) || (fi.m_maxLen < tempField.m_maxLen) || (fi.m_type != tempField.m_type)) {
/*      */         continue;
/*      */       }
/* 2152 */       isPresent = true;
/*      */     }
/*      */ 
/* 2156 */     if (!isPresent)
/*      */       return;
/*      */     try
/*      */     {
/* 2160 */       String updateQuery = new StringBuilder().append("UPDATE ").append(table).append(" SET ").append(field).append(" = ").append(tempField.m_name).toString();
/* 2161 */       JdbcFunctions.executeUpdateQuery(con, updateQuery, this.m_manager);
/*      */     }
/*      */     catch (SQLException ignore)
/*      */     {
/* 2165 */       Report.trace("systemdatabase", null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean supportsSqlColumnDelete()
/*      */   {
/* 2173 */     if (this.m_manager == null)
/*      */     {
/* 2175 */       return false;
/*      */     }
/* 2177 */     return this.m_manager.supportsSqlColumnDelete();
/*      */   }
/*      */ 
/*      */   public boolean supportsSqlColumnChange()
/*      */   {
/* 2183 */     if ((this.m_manager == null) || (WorkspaceUtils.EBRModeActive(this)))
/*      */     {
/* 2185 */       return false;
/*      */     }
/* 2187 */     return this.m_manager.supportsSqlColumnChange();
/*      */   }
/*      */ 
/*      */   public String[] getPrimaryKeys(String table) throws DataException
/*      */   {
/* 2192 */     JdbcConnection jCon = null;
/* 2193 */     String[] pkCols = null;
/* 2194 */     String[] keyName = new String[1];
/* 2195 */     String additionalMsg = null;
/*      */     try
/*      */     {
/* 2199 */       jCon = getJdbcConnection();
/* 2200 */       Connection con = (Connection)jCon.getConnection();
/* 2201 */       reportStartAction(jCon, new StringBuilder().append("getPrimaryKeys ").append(table).toString(), false, false);
/*      */ 
/* 2204 */       if ((WorkspaceUtils.EBRModeActive(this)) && 
/* 2208 */         (JdbcFunctions.hasEditioningView(jCon, table, this.m_manager)))
/*      */       {
/* 2211 */         table = new StringBuilder().append(table).append("_").toString();
/*      */       }
/*      */ 
/* 2215 */       DatabaseMetaData dbMetaData = con.getMetaData();
/* 2216 */       pkCols = JdbcFunctions.getPrimaryKeyColsInternal(dbMetaData, this.m_manager, table, keyName);
/* 2217 */       additionalMsg = new StringBuilder().append("Retrieved. Primary key contains ").append(pkCols.length).append(" columns").toString();
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/* 2221 */       additionalMsg = e.getMessage();
/* 2222 */       handleSQLException(e, jCon, "csDbUnableToPerformAction_primaryKeys", table, true);
/*      */     }
/*      */     finally
/*      */     {
/* 2226 */       if (jCon != null)
/*      */       {
/* 2228 */         reportEndAction(jCon, additionalMsg);
/*      */       }
/*      */     }
/* 2231 */     return pkCols;
/*      */   }
/*      */ 
/*      */   public FieldInfo[] getColumnList(String tableName) throws DataException
/*      */   {
/* 2236 */     JdbcConnection jCon = null;
/* 2237 */     String additionalMsg = null;
/*      */     FieldInfo[] fields;
/*      */     try {
/* 2240 */       jCon = getJdbcConnection();
/* 2241 */       reportStartAction(jCon, new StringBuilder().append("getColumns ").append(tableName).toString(), false, false);
/* 2242 */       Vector infos = JdbcFunctions.getFieldListInternal(jCon, this.m_manager, tableName);
/* 2243 */       fields = new FieldInfo[infos.size()];
/* 2244 */       for (int i = 0; i < fields.length; ++i)
/*      */       {
/* 2246 */         fields[i] = ((FieldInfo)infos.elementAt(i));
/*      */       }
/* 2248 */       additionalMsg = new StringBuilder().append("Retrieved ").append(fields.length).append(" columns.").toString();
/* 2249 */       i = fields;
/*      */ 
/* 2262 */       return i;
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/* 2253 */       additionalMsg = e.getMessage();
/* 2254 */       handleSQLException(e, jCon, "csDbUnableToPerformAction_getColumns", tableName, true);
/* 2255 */       fields = null;
/*      */ 
/* 2262 */       return fields;
/*      */     }
/*      */     finally
/*      */     {
/* 2259 */       if (jCon != null)
/*      */       {
/* 2261 */         reportEndAction(jCon, additionalMsg);
/* 2262 */         releaseAccess(jCon);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public DatabaseIndexInfo[] getIndexList(String tableName) throws DataException
/*      */   {
/* 2269 */     JdbcConnection jCon = null;
/* 2270 */     DatabaseIndexInfo[] list = null;
/* 2271 */     String additionalMsg = null;
/*      */     try
/*      */     {
/* 2274 */       jCon = getJdbcConnection();
/* 2275 */       Connection con = (Connection)jCon.getConnection();
/*      */ 
/* 2277 */       if ((WorkspaceUtils.EBRModeActive(this)) && 
/* 2281 */         (JdbcFunctions.hasEditioningView(jCon, tableName, this.m_manager)))
/*      */       {
/* 2284 */         tableName = new StringBuilder().append(tableName).append("_").toString();
/*      */       }
/*      */ 
/* 2287 */       reportStartAction(jCon, new StringBuilder().append("getIndexList ").append(tableName).toString(), false, false);
/* 2288 */       DatabaseMetaData dbMetaData = con.getMetaData();
/* 2289 */       HashMap map = JdbcFunctions.getIndexInternal(jCon, dbMetaData, this.m_manager, tableName);
/* 2290 */       list = new DatabaseIndexInfo[map.size()];
/* 2291 */       if (list.length > 0)
/*      */       {
/* 2293 */         int count = 0;
/* 2294 */         String[] pkIndexName = new String[1];
/* 2295 */         JdbcFunctions.getPrimaryKeyColsInternal(dbMetaData, this.m_manager, tableName, pkIndexName);
/*      */ 
/* 2297 */         if (pkIndexName[0] != null)
/*      */         {
/* 2299 */           DatabaseIndexInfo indexInfo = (DatabaseIndexInfo)map.get(pkIndexName[0]);
/* 2300 */           if (indexInfo != null)
/*      */           {
/* 2302 */             indexInfo.m_isPrimary = true;
/* 2303 */             list[0] = indexInfo;
/* 2304 */             ++count;
/*      */           }
/*      */         }
/* 2307 */         Iterator iterator = map.entrySet().iterator();
/* 2308 */         while (iterator.hasNext())
/*      */         {
/* 2310 */           Map.Entry entry = (Map.Entry)iterator.next();
/* 2311 */           DatabaseIndexInfo info = (DatabaseIndexInfo)entry.getValue();
/* 2312 */           if (info == list[0]) {
/*      */             continue;
/*      */           }
/*      */ 
/* 2316 */           list[(count++)] = info;
/*      */         }
/*      */       }
/* 2319 */       additionalMsg = new StringBuilder().append("Retrieved ").append(list.length).append(" indexes.").toString();
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/* 2324 */       additionalMsg = e.getMessage();
/* 2325 */       handleSQLException(e, jCon, "csDbUnableToPerformAction_getIndexes", tableName, true);
/*      */     }
/*      */     finally
/*      */     {
/* 2329 */       if (jCon != null)
/*      */       {
/* 2331 */         reportEndAction(jCon, additionalMsg);
/*      */       }
/*      */     }
/* 2334 */     return list;
/*      */   }
/*      */ 
/*      */   public void addIndex(String table, String[] indexCols) throws DataException
/*      */   {
/* 2339 */     String indexName = table;
/* 2340 */     for (int i = 0; i < indexCols.length; ++i)
/*      */     {
/* 2342 */       indexName = new StringBuilder().append(indexName).append("_").append(indexCols[i]).toString();
/*      */     }
/* 2344 */     if (this.m_manager.useShortIndexName())
/*      */     {
/* 2346 */       indexName = getShortIndexName(indexName);
/*      */     }
/* 2348 */     String createIndexSql = new StringBuilder().append("CREATE INDEX ").append(indexName).append(" ON ").append(table).append("(").toString();
/* 2349 */     for (int i = 0; i < indexCols.length; ++i)
/*      */     {
/* 2351 */       createIndexSql = new StringBuilder().append(createIndexSql).append(indexCols[i]).toString();
/* 2352 */       if (i >= indexCols.length - 1)
/*      */         continue;
/* 2354 */       createIndexSql = new StringBuilder().append(createIndexSql).append(", ").toString();
/*      */     }
/*      */ 
/* 2357 */     createIndexSql = new StringBuilder().append(createIndexSql).append(")").toString();
/*      */     try
/*      */     {
/* 2360 */       executeSQL(createIndexSql);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 2364 */       Report.trace("systemdatabase", null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String createPrimaryKeyConstraintString(String[] pkCols)
/*      */   {
/* 2370 */     String pkConstraintString = null;
/* 2371 */     if (pkCols.length > 0)
/*      */     {
/* 2373 */       pkConstraintString = " PRIMARY KEY (";
/* 2374 */       for (int i = 0; i < pkCols.length; ++i)
/*      */       {
/* 2376 */         pkConstraintString = new StringBuilder().append(pkConstraintString).append(pkCols[i]).toString();
/* 2377 */         if (i >= pkCols.length - 1)
/*      */           continue;
/* 2379 */         pkConstraintString = new StringBuilder().append(pkConstraintString).append(", ").toString();
/*      */       }
/*      */ 
/* 2382 */       pkConstraintString = new StringBuilder().append(pkConstraintString).append(")").toString();
/*      */     }
/* 2384 */     return pkConstraintString;
/*      */   }
/*      */ 
/*      */   public void addQuery(SimpleQueryInfo qInfo)
/*      */     throws DataException
/*      */   {
/*      */   }
/*      */ 
/*      */   public void removeQuery(String query) throws DataException
/*      */   {
/* 2394 */     this.m_manager.removeQuery(query);
/*      */   }
/*      */ 
/*      */   public void addQueryDataSourceMap(String query, IdcDataSourceQuery dataSourceQuery) throws DataException
/*      */   {
/* 2399 */     if ((query == null) || (dataSourceQuery == null))
/*      */     {
/* 2401 */       return;
/*      */     }
/* 2403 */     this.m_dataSourceMap.put(query, dataSourceQuery);
/*      */   }
/*      */ 
/*      */   public String[] getQueryParameters(String query) throws DataException
/*      */   {
/* 2408 */     QueryParameterInfo[] infoList = getQueryParameterInfos(query);
/* 2409 */     String[] list = new String[infoList.length];
/* 2410 */     for (int i = 0; i < infoList.length; ++i)
/*      */     {
/* 2412 */       list[i] = infoList[i].m_name;
/*      */     }
/* 2414 */     return list;
/*      */   }
/*      */ 
/*      */   public QueryParameterInfo[] getQueryParameterInfos(String query) throws DataException
/*      */   {
/* 2419 */     JdbcConnection con = getJdbcConnection();
/* 2420 */     QueryParameterInfo[] list = null;
/*      */     try
/*      */     {
/* 2425 */       JdbcQueryDef qDef = con.getQueryDef(query);
/* 2426 */       Vector params = qDef.m_parameters;
/*      */ 
/* 2428 */       int size = params.size();
/* 2429 */       list = new QueryParameterInfo[size];
/* 2430 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 2432 */         QueryParameterInfo param = (QueryParameterInfo)params.elementAt(i);
/* 2433 */         list[i] = param;
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 2438 */       releaseAccess(con);
/*      */     }
/*      */ 
/* 2441 */     return list;
/*      */   }
/*      */ 
/*      */   public boolean checkQuery(String query, Parameters args) throws DataException
/*      */   {
/* 2446 */     QueryParameterInfo[] params = getQueryParameterInfos(query);
/* 2447 */     if ((params == null) || (args == null))
/*      */     {
/* 2449 */       return false;
/*      */     }
/*      */ 
/* 2452 */     int numParams = params.length;
/* 2453 */     for (int i = 0; i < numParams; ++i)
/*      */     {
/* 2455 */       String value = getParameterValue(query, args, params[i]);
/* 2456 */       if (value == null)
/*      */       {
/* 2458 */         return false;
/*      */       }
/*      */     }
/* 2461 */     return true;
/*      */   }
/*      */ 
/*      */   public String dumpQuery(String query, Parameters args) throws DataException
/*      */   {
/* 2466 */     QueryParameterInfo[] params = getQueryParameterInfos(query);
/* 2467 */     if (params == null)
/*      */     {
/* 2469 */       return new StringBuilder().append("Error in retrieving parameters for query ").append(query).append(".").toString();
/*      */     }
/*      */ 
/* 2472 */     StringBuffer dumpBuff = new StringBuffer(new StringBuilder().append("Dumping query:").append(query).append("\n").toString());
/*      */ 
/* 2474 */     int numParams = params.length;
/* 2475 */     for (int i = 0; i < numParams; ++i)
/*      */     {
/* 2477 */       String name = params[i].m_name;
/* 2478 */       dumpBuff.append(name);
/* 2479 */       if (args != null)
/*      */       {
/* 2481 */         String value = getParameterValue(query, args, params[i]);
/* 2482 */         dumpBuff.append("  value:  ");
/* 2483 */         dumpBuff.append(value);
/*      */       }
/* 2485 */       dumpBuff.append("\n");
/*      */     }
/* 2487 */     return dumpBuff.toString();
/*      */   }
/*      */ 
/*      */   public void releaseConnection()
/*      */   {
/* 2492 */     Thread curThread = Thread.currentThread();
/* 2493 */     releaseConnection(curThread.getName());
/*      */   }
/*      */ 
/*      */   public void releaseConnection(String threadName)
/*      */   {
/*      */     try
/*      */     {
/* 2500 */       this.m_savedPoints.remove(threadName);
/* 2501 */       if (this.m_useFakeSavepoints)
/*      */       {
/* 2503 */         this.m_fakeSavepoints.remove(threadName);
/*      */       }
/* 2505 */       JdbcConnection con = (JdbcConnection)this.m_manager.getActiveConnection(threadName);
/*      */ 
/* 2507 */       if (con != null)
/*      */       {
/* 2509 */         if (con.m_inTransaction)
/*      */         {
/* 2512 */           ServiceException e = new ServiceException("");
/* 2513 */           Report.error(null, "!csJdbcReleaseConnectionInTransaction", e);
/* 2514 */           this.m_manager.debugMsg("releaseConnection() called in transaction");
/*      */           try
/*      */           {
/* 2519 */             rollbackTranEx(con);
/*      */           }
/*      */           catch (Exception ignore)
/*      */           {
/* 2523 */             Report.error(null, "!csJdbcTransactionUnableRollback", e);
/* 2524 */             this.m_manager.debugMsg("unable to rollback transaction while releasing");
/*      */ 
/* 2526 */             Report.trace(null, null, ignore);
/*      */           }
/*      */         }
/* 2529 */         else if (this.m_manager.usingExternalDataSource())
/*      */         {
/* 2531 */           con.closePreparedStatements();
/*      */         }
/*      */       }
/* 2534 */       clearBatch();
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/* 2538 */       if (SystemUtils.m_verbose)
/*      */       {
/* 2540 */         Report.debug("systemdatabase", null, ignore);
/*      */       }
/*      */     }
/*      */ 
/* 2544 */     this.m_manager.releaseConnection(threadName);
/*      */   }
/*      */ 
/*      */   public String parseSQL(String sql) throws DataException
/*      */   {
/* 2549 */     return JdbcQueryUtils.parseSQL(sql, this, this.m_manager);
/*      */   }
/*      */ 
/*      */   protected String fixUnicodeQuery(String sql)
/*      */   {
/* 2556 */     return JdbcQueryUtils.fixUnicodeQuery(sql, this.m_manager);
/*      */   }
/*      */ 
/*      */   protected void handleSQLException(SQLException e, JdbcConnection conn, String msg, String reportStr, boolean throwException)
/*      */     throws DataException
/*      */   {
/* 2562 */     String additionalMsg = e.getMessage();
/* 2563 */     msg = LocaleUtils.encodeMessage(msg, additionalMsg, reportStr);
/* 2564 */     if (!conn.m_isInTest)
/*      */     {
/*      */       try
/*      */       {
/* 2568 */         conn.m_isInTest = true;
/* 2569 */         testConnection(this.m_provider.getProviderData(), null);
/*      */       }
/*      */       catch (Exception testException)
/*      */       {
/* 2573 */         msg = LocaleUtils.encodeMessage("csJdbcConnectionFailure", null, msg);
/* 2574 */         Thread t = this.m_manager.m_keepAliveThread;
/* 2575 */         if (t != null)
/*      */         {
/* 2577 */           t.interrupt();
/*      */         }
/* 2579 */         conn.determineConnectionState(e);
/*      */       }
/*      */       finally
/*      */       {
/* 2583 */         conn.m_isInTest = false;
/*      */       }
/*      */     }
/*      */ 
/* 2587 */     this.m_manager.debugMsg(msg);
/* 2588 */     if (!throwException)
/*      */       return;
/* 2590 */     if (!SharedObjects.getEnvValueAsBoolean("SecureDataAllowedIntoStatusMessage", false))
/*      */     {
/* 2592 */       msg = "!csJdbcGenericError";
/*      */     }
/*      */ 
/* 2595 */     throw new DataException(msg, null);
/*      */   }
/*      */ 
/*      */   public JdbcManager getManager()
/*      */   {
/* 2601 */     return this.m_manager;
/*      */   }
/*      */ 
/*      */   public Object getManagerObject()
/*      */   {
/* 2606 */     return this.m_manager;
/*      */   }
/*      */ 
/*      */   public String getProperty(String key)
/*      */   {
/* 2611 */     return this.m_config.getValueAsString(key);
/*      */   }
/*      */ 
/*      */   public void setThreadTimeout(int seconds)
/*      */   {
/* 2616 */     if (seconds < 0)
/*      */       return;
/* 2618 */     addOrRemoveThreadTimeout(seconds, true);
/*      */   }
/*      */ 
/*      */   public int clearThreadTimeout()
/*      */   {
/* 2624 */     int result = 0;
/* 2625 */     result = addOrRemoveThreadTimeout(0, false);
/* 2626 */     return result;
/*      */   }
/*      */ 
/*      */   public int getThreadTimeout()
/*      */   {
/* 2631 */     if (this.m_config.getValueAsBool("DisableQueryTimeoutSupport", false))
/*      */     {
/* 2633 */       return -1;
/*      */     }
/* 2635 */     int timeout = this.m_defaultTimeout;
/* 2636 */     Integer threadTimeout = (Integer)m_threadTimeout.get(Thread.currentThread().getName());
/* 2637 */     if (threadTimeout != null)
/*      */     {
/* 2639 */       timeout = threadTimeout.intValue();
/*      */     }
/* 2641 */     return timeout;
/*      */   }
/*      */ 
/*      */   public void addDefaultCallback(boolean isGlobal, WorkspaceCallback callback)
/*      */   {
/* 2646 */     String threadID = Thread.currentThread().getName();
/* 2647 */     if (isGlobal)
/*      */     {
/* 2649 */       threadID = "global";
/*      */     }
/* 2651 */     List list = (List)this.m_defaultCallbacks.get(threadID);
/* 2652 */     if (list == null)
/*      */     {
/* 2654 */       list = new ArrayList();
/*      */     }
/*      */ 
/* 2657 */     list.add(callback);
/* 2658 */     this.m_defaultCallbacks.put(threadID, list);
/*      */   }
/*      */ 
/*      */   public boolean removeDefaultCallback(boolean isGlobal, WorkspaceCallback callback)
/*      */   {
/* 2663 */     boolean retVal = true;
/* 2664 */     String threadID = Thread.currentThread().getName();
/* 2665 */     if (isGlobal)
/*      */     {
/* 2667 */       threadID = "global";
/*      */     }
/*      */ 
/* 2670 */     List list = (List)this.m_defaultCallbacks.get(threadID);
/* 2671 */     if (list != null)
/*      */     {
/* 2673 */       retVal = list.remove(callback);
/* 2674 */       if (list.size() == 0)
/*      */       {
/* 2676 */         this.m_defaultCallbacks.remove(threadID);
/*      */       }
/*      */     }
/* 2679 */     return retVal;
/*      */   }
/*      */ 
/*      */   public void clearDefaultCallbacks()
/*      */   {
/* 2684 */     this.m_defaultCallbacks.clear();
/*      */   }
/*      */ 
/*      */   public synchronized int addOrRemoveThreadTimeout(int seconds, boolean isAdd)
/*      */   {
/* 2689 */     if (this.m_config.getValueAsBool("DisableQueryTimeoutSupport", false))
/*      */     {
/* 2691 */       return 0;
/*      */     }
/*      */ 
/* 2694 */     int result = 0;
/* 2695 */     String name = Thread.currentThread().getName();
/* 2696 */     if (isAdd)
/*      */     {
/* 2698 */       m_threadTimeout.put(name, new Integer(seconds));
/* 2699 */       result = 1;
/*      */     }
/*      */     else
/*      */     {
/* 2703 */       Object obj = m_threadTimeout.remove(name);
/* 2704 */       if (obj != null)
/*      */       {
/* 2706 */         result = 1;
/*      */       }
/*      */     }
/* 2709 */     return result;
/*      */   }
/*      */ 
/*      */   public void dbManagement(int type, Parameters args) throws DataException
/*      */   {
/* 2714 */     createProviderConfig();
/*      */   }
/*      */ 
/*      */   protected void reportStartAction(JdbcConnection con, String action, boolean isWrite, boolean checkQueryForDDL)
/*      */   {
/* 2719 */     HashMap data = new HashMap();
/* 2720 */     if (con != null)
/*      */     {
/* 2722 */       String name = Thread.currentThread().getName();
/* 2723 */       String id = getActiveQueryID(name);
/* 2724 */       con.startAction(action);
/* 2725 */       con.setCurrentActionID(id);
/*      */ 
/* 2727 */       data.put("activeQueryID", id);
/* 2728 */       data.put("connection", con);
/*      */     }
/* 2730 */     this.m_manager.incrementActionCount(isWrite);
/* 2731 */     if (SystemUtils.m_verbose)
/*      */     {
/* 2733 */       this.m_manager.debugLockingMsg(new StringBuilder().append("(start) ").append(action).toString());
/*      */     }
/*      */ 
/* 2736 */     if (checkQueryForDDL)
/*      */     {
/* 2738 */       this.m_manager.checkAndLogDDL(action);
/*      */     }
/*      */ 
/* 2741 */     data.put("action", action);
/* 2742 */     data.put("isWrite", new StringBuilder().append("").append(isWrite).toString());
/* 2743 */     eventCallback(WorkspaceEventID.START_ACTION, data);
/*      */   }
/*      */ 
/*      */   protected void reportEndAction(JdbcConnection con, String additionalMsg)
/*      */   {
/* 2748 */     Map data = new HashMap();
/* 2749 */     if (con != null)
/*      */     {
/* 2751 */       con.m_actionInterval.stop();
/* 2752 */       long curTime = System.currentTimeMillis();
/* 2753 */       long diff = con.computeTimePending(curTime);
/* 2754 */       long activeTime = con.computeTimeActive(curTime);
/* 2755 */       long timeOut = this.m_manager.getConnectionTimeout();
/* 2756 */       boolean hasWarnedLongActive = con.m_hasWarnedLongActive;
/* 2757 */       String action = null;
/*      */ 
/* 2759 */       if (SystemUtils.isActiveTrace("systemdatabase"))
/*      */       {
/* 2761 */         action = con.getCurrentAction();
/* 2762 */         if (null == action)
/*      */         {
/* 2764 */           action = "";
/*      */         }
/* 2766 */         if (additionalMsg != null)
/*      */         {
/* 2768 */           action = new StringBuilder().append(action).append("[").append(additionalMsg).append("]").toString();
/*      */         }
/* 2770 */         Object[] args = { action, con.m_actionInterval.toString() };
/*      */ 
/* 2775 */         String msg = LocaleUtils.encodeMessage("csDbTimeQueryReport", null, args);
/* 2776 */         this.m_manager.debugMsg(LocaleResources.localizeMessage(msg, null));
/*      */       }
/* 2778 */       if ((diff > timeOut) || ((activeTime > 2L * timeOut) && (!hasWarnedLongActive)))
/*      */       {
/* 2780 */         String id = con.getId();
/* 2781 */         String state = con.getCurrentState();
/* 2782 */         action = (null == action) ? con.getCurrentAction() : action;
/* 2783 */         String reportLine = new StringBuilder().append("!{[").append(id).append("(").append(state).append(")!wwActiveConnTimeActiveLabel!$ ").append(activeTime / 1000L).append("s]}").toString();
/*      */ 
/* 2786 */         if ((action != null) && (action.length() > 0))
/*      */         {
/* 2788 */           reportLine = new StringBuilder().append(reportLine).append("!$\n{(").toString();
/* 2789 */           reportLine = new StringBuilder().append(reportLine).append(LocaleUtils.encodeMessage("csDbConnectionActionReport", null, action)).toString();
/* 2790 */           reportLine = new StringBuilder().append(reportLine).append("!$)}\n").toString();
/*      */         }
/*      */ 
/* 2793 */         String msg = reportLine;
/* 2794 */         if (diff > timeOut)
/*      */         {
/* 2796 */           msg = new StringBuilder().append(msg).append("!$\n").toString();
/* 2797 */           msg = new StringBuilder().append(msg).append(LocaleUtils.encodeMessage("csDbConnectionLongExecutionReport", null, new Date(curTime), new StringBuilder().append("").append(diff / 1000L).toString())).toString();
/*      */         }
/*      */ 
/* 2800 */         if ((!hasWarnedLongActive) && (activeTime > 2L * timeOut))
/*      */         {
/* 2802 */           msg = new StringBuilder().append(msg).append("!$\n").toString();
/* 2803 */           msg = new StringBuilder().append(msg).append(LocaleUtils.encodeMessage("csDbConnectionLongActiveReport", null, new Date(curTime), new StringBuilder().append("").append(activeTime / 1000L).toString())).toString();
/*      */ 
/* 2805 */           con.m_hasWarnedLongActive = true;
/*      */         }
/* 2807 */         this.m_manager.addAuditMessage(msg);
/* 2808 */         this.m_manager.debugMsg(LocaleResources.localizeMessage(msg, null));
/*      */       }
/* 2810 */       con.endAction();
/* 2811 */       data.put("queryTime", Long.valueOf(diff));
/* 2812 */       data.put("connActiveTime", Long.valueOf(activeTime));
/* 2813 */       data.put("connection", con);
/* 2814 */       data.put("action", con.getCurrentAction());
/*      */     }
/*      */     else
/*      */     {
/* 2818 */       this.m_manager.debugMsg(additionalMsg);
/*      */     }
/* 2820 */     data.put("additionalMsg", additionalMsg);
/* 2821 */     eventCallback(WorkspaceEventID.END_ACTION, data);
/*      */   }
/*      */ 
/*      */   protected void eventCallback(WorkspaceEventID id, Map data)
/*      */   {
/* 2826 */     if (this.m_defaultCallbacks.isEmpty())
/*      */     {
/* 2828 */       return;
/*      */     }
/* 2830 */     String threadID = Thread.currentThread().getName();
/* 2831 */     data.put("threadID", threadID);
/*      */ 
/* 2834 */     WorkspaceCallbackStatus status = callEventCallback(threadID, id, data);
/*      */ 
/* 2837 */     if (status == WorkspaceCallbackStatus.FINISHED)
/*      */       return;
/* 2839 */     callEventCallback("global", id, data);
/*      */   }
/*      */ 
/*      */   protected WorkspaceCallbackStatus callEventCallback(String threadID, WorkspaceEventID id, Map data)
/*      */   {
/* 2846 */     WorkspaceCallbackStatus status = WorkspaceCallbackStatus.CONTINUE;
/* 2847 */     List list = (List)this.m_defaultCallbacks.get(threadID);
/* 2848 */     if (list != null)
/*      */     {
/* 2850 */       for (WorkspaceCallback wcb : list)
/*      */       {
/* 2852 */         if (!wcb.canHandle(id, data)) {
/*      */           continue;
/*      */         }
/*      */ 
/* 2856 */         status = wcb.callback(id, data);
/* 2857 */         if (status == WorkspaceCallbackStatus.FINISHED) {
/*      */           break;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2864 */     return status;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2869 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105847 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.JdbcWorkspace
 * JD-Core Version:    0.5.4
 */