/*     */ package intradoc.jdbc;
/*     */ 
/*     */ import intradoc.common.IntervalData;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.provider.ProviderConnection;
/*     */ import intradoc.provider.ProviderConnectionManager;
/*     */ import intradoc.provider.ProviderConnectionStatus;
/*     */ import intradoc.util.IdcConcurrentHashMap;
/*     */ import java.sql.Connection;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JdbcConnection
/*     */   implements ProviderConnection, ProviderConnectionStatus
/*     */ {
/*  34 */   protected JdbcManager m_manager = null;
/*  35 */   protected DataBinder m_connectionData = null;
/*     */ 
/*  37 */   protected Connection m_connection = null;
/*     */ 
/*  42 */   protected IdcConcurrentHashMap m_queryDefs = null;
/*  43 */   protected List<String> m_preparedStatements = null;
/*     */ 
/*  45 */   protected JdbcResultSet m_activeResultSet = null;
/*  46 */   protected JdbcCallableResults m_callableResults = null;
/*     */ 
/*  50 */   protected String m_curId = "<unassigned>";
/*     */ 
/*  53 */   protected boolean m_isBadConnection = false;
/*  54 */   protected int m_errorCount = 0;
/*  55 */   public static int m_maxErrorCount = 1;
/*     */ 
/*  57 */   protected static int m_conCount = 0;
/*  58 */   protected int m_myCount = 0;
/*     */ 
/*  61 */   protected String m_curState = "Not In Transction";
/*  62 */   protected String m_curAction = null;
/*  63 */   protected String m_curActionID = null;
/*  64 */   protected String m_actionStatus = "NOT STARTED";
/*  65 */   protected boolean m_canSetTransactionIsolation = true;
/*  66 */   protected int m_curIsolationLevel = 4;
/*  67 */   protected boolean m_inTransaction = false;
/*  68 */   protected int m_isolationLevelInTran = 4;
/*  69 */   protected int m_isolationLevelOutTran = 1;
/*  70 */   protected boolean m_hasWarnedLongActive = false;
/*     */ 
/*     */   @Deprecated
/*  72 */   protected long m_idleTimestamp = -1L;
/*     */ 
/*  74 */   protected IntervalData m_idleInterval = null;
/*     */ 
/*     */   @Deprecated
/*  76 */   protected long m_holdTimestamp = -1L;
/*     */ 
/*  78 */   protected IntervalData m_holdInterval = null;
/*     */ 
/*     */   @Deprecated
/*  81 */   protected long m_actionStart = -1L;
/*     */ 
/*  83 */   protected IntervalData m_actionInterval = null;
/*     */ 
/*  85 */   protected String m_badConnectionMsg = null;
/*     */ 
/*  87 */   protected List m_persistObjectList = new ArrayList();
/*     */ 
/*  89 */   protected String m_stackTrace = null;
/*  90 */   protected String m_serviceName = null;
/*  91 */   protected String m_subService = null;
/*     */ 
/*  93 */   public boolean m_isInTest = false;
/*     */ 
/*     */   public JdbcConnection()
/*     */   {
/*  98 */     this.m_queryDefs = new IdcConcurrentHashMap(false);
/*  99 */     this.m_queryDefs.initCaseInsensitiveKeyMap(true);
/* 100 */     this.m_preparedStatements = new ArrayList();
/*     */   }
/*     */ 
/*     */   public void init(ProviderConnectionManager manager, DataBinder data) throws DataException
/*     */   {
/* 105 */     init(manager, data, null, null, 0, null);
/*     */   }
/*     */ 
/*     */   public void init(ProviderConnectionManager manager, DataBinder data, String defaultClass, Object rawConnection, int flags, Map params)
/*     */     throws DataException
/*     */   {
/* 111 */     boolean thisObjIsReused = (flags & 0x2) != 0;
/* 112 */     boolean rawIsReused = (flags & 0x1) != 0;
/* 113 */     this.m_manager = ((JdbcManager)manager);
/* 114 */     this.m_connectionData = data;
/*     */     try
/*     */     {
/* 118 */       if (rawConnection != null)
/*     */       {
/* 120 */         this.m_connection = ((Connection)rawConnection);
/*     */       }
/*     */       else
/*     */       {
/* 124 */         this.m_connection = JdbcConnectionUtils.getConnection(manager, data);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 129 */       if (this.m_connection == null)
/*     */       {
/* 131 */         this.m_isBadConnection = true;
/*     */       }
/*     */     }
/* 134 */     if (!thisObjIsReused)
/*     */     {
/* 137 */       this.m_idleInterval = new IntervalData();
/* 138 */       this.m_idleInterval.m_name = "idle";
/* 139 */       this.m_holdInterval = new IntervalData();
/* 140 */       this.m_holdInterval.m_name = "hold";
/* 141 */       this.m_actionInterval = new IntervalData();
/* 142 */       this.m_actionInterval.m_name = "";
/*     */ 
/* 144 */       String tilInTran = this.m_connectionData.getAllowMissing("DBIsolationLevelInTran");
/* 145 */       String tilOutTran = this.m_connectionData.getAllowMissing("DBIsolationLevelOutTran");
/* 146 */       if ((tilInTran != null) && (tilInTran.length() != 0))
/*     */       {
/* 148 */         this.m_isolationLevelInTran = getTranIsolationLevel(tilInTran);
/* 149 */         this.m_curIsolationLevel = this.m_isolationLevelInTran;
/*     */       }
/*     */ 
/* 152 */       if ((tilOutTran != null) && (tilOutTran.length() != 0))
/*     */       {
/* 154 */         this.m_isolationLevelOutTran = getTranIsolationLevel(tilOutTran);
/*     */       }
/*     */ 
/* 157 */       m_conCount += 1;
/* 158 */       this.m_myCount = m_conCount;
/*     */     }
/*     */ 
/* 161 */     if (rawIsReused)
/*     */       return;
/*     */     int i;
/* 163 */     int i = -1;
/*     */     try
/*     */     {
/* 166 */       i = this.m_connection.getTransactionIsolation();
/* 167 */       this.m_connection.setTransactionIsolation(this.m_curIsolationLevel);
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 171 */       this.m_canSetTransactionIsolation = false;
/* 172 */       if (SystemUtils.m_verbose)
/*     */       {
/* 174 */         this.m_manager.debugMsg("Unable to set transaction isolation level.  " + e.getMessage());
/*     */       }
/*     */ 
/* 177 */       if (i != -1)
/*     */       {
/*     */         try
/*     */         {
/* 181 */           this.m_connection.setTransactionIsolation(i);
/*     */         }
/*     */         catch (Exception ignore)
/*     */         {
/* 185 */           this.m_manager.debugMsg(ignore.getMessage());
/*     */         }
/*     */       }
/*     */     }
/* 189 */     prepareConnection(data);
/*     */   }
/*     */ 
/*     */   public Object getRawConnection()
/*     */   {
/* 195 */     return this.m_connection;
/*     */   }
/*     */ 
/*     */   public void prepareConnection(DataBinder data)
/*     */   {
/* 200 */     JdbcManager manager = this.m_manager;
/* 201 */     String initQueries = data.getAllowMissing("ConnInitQueries");
/* 202 */     if (initQueries == null)
/*     */     {
/* 204 */       String db = manager.getDBType();
/* 205 */       initQueries = data.getAllowMissing(db + "ConnPrepQueries");
/*     */     }
/* 207 */     Vector queries = StringUtils.parseArray(initQueries, ',', '^');
/*     */ 
/* 209 */     String query = null;
/* 210 */     Statement stat = null;
/*     */     try
/*     */     {
/* 213 */       int size = queries.size();
/* 214 */       if (size > 0)
/*     */       {
/* 216 */         stat = this.m_connection.createStatement();
/*     */       }
/* 218 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 220 */         query = (String)queries.elementAt(i);
/* 221 */         JdbcQueryDef def = manager.getQueryDef(query);
/* 222 */         if (def != null)
/*     */         {
/* 224 */           query = JdbcQueryUtils.buildQuery(def, data, null, manager);
/*     */         }
/*     */         else
/*     */         {
/* 228 */           query = data.getAllowMissing(query);
/*     */         }
/* 230 */         if (query != null)
/*     */         {
/* 232 */           if (SystemUtils.m_verbose)
/*     */           {
/* 234 */             this.m_manager.debugMsg("Initializing Connection. Executing query: " + query);
/*     */           }
/* 236 */           stat.execute(query);
/*     */         }
/*     */         else
/*     */         {
/* 240 */           if (!SystemUtils.m_verbose)
/*     */             continue;
/* 242 */           this.m_manager.debugMsg("Initializing connection.  No initialization query.");
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception t)
/*     */     {
/* 249 */       manager.debugMsg(e.getMessage());
/* 250 */       String msg = LocaleUtils.encodeMessage("csJdbcConnectionUnableExecuteInitQuery", null, query);
/* 251 */       Report.error(null, msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 256 */       if (stat != null)
/*     */       {
/*     */         try
/*     */         {
/* 260 */           stat.close();
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/* 264 */           Report.trace(null, "Unable to close statement while preparing connection", t);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getTranIsolationLevel(String isolationLevelStr) throws DataException
/*     */   {
/* 272 */     int isolationLevel = -1;
/* 273 */     if (isolationLevelStr.equalsIgnoreCase("Serializable"))
/*     */     {
/* 275 */       isolationLevel = 8;
/*     */     }
/* 277 */     else if (isolationLevelStr.equalsIgnoreCase("RepeatableRead"))
/*     */     {
/* 279 */       isolationLevel = 4;
/*     */     }
/* 281 */     else if (isolationLevelStr.equalsIgnoreCase("ReadCommitted"))
/*     */     {
/* 283 */       isolationLevel = 2;
/*     */     }
/* 285 */     else if (isolationLevelStr.equalsIgnoreCase("ReadUnCommitted"))
/*     */     {
/* 287 */       isolationLevel = 1;
/*     */     }
/* 289 */     else if (isolationLevelStr.equalsIgnoreCase("None"))
/*     */     {
/* 291 */       isolationLevel = 0;
/*     */     }
/*     */     else
/*     */     {
/* 295 */       String err = LocaleUtils.encodeMessage("csJdbcUnrecongnizableTranIsolationLevel", null, isolationLevelStr);
/*     */ 
/* 297 */       throw new DataException(err);
/*     */     }
/* 299 */     return isolationLevel;
/*     */   }
/*     */ 
/*     */   public String getId()
/*     */   {
/* 304 */     return this.m_curId;
/*     */   }
/*     */ 
/*     */   public String getCurrentState()
/*     */   {
/* 309 */     return this.m_curState;
/*     */   }
/*     */ 
/*     */   public void setCurrentState(String curState)
/*     */   {
/* 314 */     this.m_curState = curState;
/*     */   }
/*     */ 
/*     */   public void startAction(String action)
/*     */   {
/* 319 */     this.m_curAction = action;
/* 320 */     this.m_actionStatus = "EXECUTING";
/* 321 */     this.m_actionInterval.reset();
/* 322 */     this.m_actionInterval.start();
/*     */   }
/*     */ 
/*     */   public void setCurrentActionID(String actionID) {
/* 326 */     this.m_curActionID = actionID;
/*     */   }
/*     */ 
/*     */   public String getActionID()
/*     */   {
/* 331 */     return this.m_curActionID;
/*     */   }
/*     */ 
/*     */   public void setCurrentActionStatus(String status) {
/* 335 */     this.m_actionStatus = status;
/*     */   }
/*     */ 
/*     */   public String getCurrentActionStatus()
/*     */   {
/* 340 */     return this.m_actionStatus;
/*     */   }
/*     */ 
/*     */   public String getCurrentAction()
/*     */   {
/* 345 */     return this.m_curAction;
/*     */   }
/*     */ 
/*     */   public long computeTimePending(long curTime)
/*     */   {
/* 350 */     if (this.m_curAction == null)
/*     */     {
/* 352 */       return 0L;
/*     */     }
/* 354 */     return this.m_actionInterval.getInterval() / 1000000L;
/*     */   }
/*     */ 
/*     */   public long getActionInterval()
/*     */   {
/* 359 */     if (this.m_curAction == null)
/*     */     {
/* 361 */       return 0L;
/*     */     }
/* 363 */     return this.m_actionInterval.getInterval();
/*     */   }
/*     */ 
/*     */   public long computeTimeActive(long curTime)
/*     */   {
/* 368 */     return this.m_holdInterval.getInterval() / 1000000L;
/*     */   }
/*     */ 
/*     */   public long getHoldInterval()
/*     */   {
/* 373 */     return this.m_holdInterval.getInterval();
/*     */   }
/*     */ 
/*     */   public void endAction()
/*     */   {
/* 378 */     this.m_actionInterval.stop();
/* 379 */     this.m_curAction = null;
/* 380 */     this.m_actionStatus = "NOT STARTED";
/* 381 */     this.m_curActionID = null;
/*     */   }
/*     */ 
/*     */   public void addQueries(Hashtable queries)
/*     */     throws DataException
/*     */   {
/* 388 */     for (Enumeration en = queries.elements(); en.hasMoreElements(); )
/*     */     {
/* 390 */       JdbcQueryDef query = (JdbcQueryDef)en.nextElement();
/* 391 */       JdbcQueryDef qDef = query.copy(this.m_connection);
/*     */ 
/* 393 */       this.m_queryDefs.put(qDef.m_name, qDef);
/*     */     }
/*     */   }
/*     */ 
/*     */   public JdbcQueryDef removeQuery(String name)
/*     */     throws DataException
/*     */   {
/* 418 */     if (Report.m_verbose)
/*     */     {
/* 420 */       this.m_manager.debugMsg("Removing query: " + name);
/*     */     }
/* 422 */     JdbcQueryDef qDef = (JdbcQueryDef)this.m_queryDefs.remove(name);
/* 423 */     removeQuery(qDef);
/* 424 */     return qDef;
/*     */   }
/*     */ 
/*     */   public void removeQuery(JdbcQueryDef qDef) throws DataException
/*     */   {
/* 429 */     if ((qDef == null) || (qDef.m_statement == null))
/*     */       return;
/*     */     try
/*     */     {
/* 433 */       if (Report.m_verbose)
/*     */       {
/* 435 */         this.m_manager.debugMsg("Closing statement.");
/*     */       }
/* 437 */       qDef.m_statement.close();
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 441 */       throw new DataException(e, "csErrorWhileRemovingQueryUnableToCloseStatment", new Object[] { qDef.m_name });
/*     */     }
/*     */   }
/*     */ 
/*     */   public JdbcQueryDef getQueryDef(String name)
/*     */     throws DataException
/*     */   {
/* 449 */     return getQueryDefAllowMissing(name, false);
/*     */   }
/*     */ 
/*     */   public JdbcQueryDef getQueryDefAllowMissing(String name, boolean isAllowMissing)
/*     */     throws DataException
/*     */   {
/* 455 */     JdbcQueryDef qDef = (JdbcQueryDef)this.m_queryDefs.get(name);
/* 456 */     JdbcManager jMan = this.m_manager;
/* 457 */     if ((qDef == null) || (jMan.usingExternalDataSource()))
/*     */     {
/* 460 */       JdbcQueryDef mQDef = jMan.getQueryDef(name);
/* 461 */       if ((mQDef != null) && ((
/* 463 */         (qDef == null) || (!mQDef.isEquivalentQuery(qDef)))))
/*     */       {
/* 465 */         if (qDef != null)
/*     */         {
/* 467 */           removeQuery(qDef);
/*     */         }
/* 469 */         qDef = mQDef.copy(this.m_connection);
/* 470 */         this.m_queryDefs.put(name, qDef);
/* 471 */         if ((jMan.usingExternalDataSource()) && (qDef.m_isPrepared))
/*     */         {
/* 473 */           this.m_preparedStatements.add(qDef.m_name);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 478 */     if ((qDef == null) && (!isAllowMissing))
/*     */     {
/* 480 */       String msg = LocaleUtils.encodeMessage("csUnableToFindQuery", null, name);
/* 481 */       throw new DataException(msg);
/*     */     }
/* 483 */     return qDef;
/*     */   }
/*     */ 
/*     */   public Map getQueryDefs()
/*     */   {
/* 488 */     return this.m_queryDefs;
/*     */   }
/*     */ 
/*     */   public Object getConnection()
/*     */   {
/* 493 */     updateIsolationLevel();
/* 494 */     return this.m_connection;
/*     */   }
/*     */ 
/*     */   public void determineConnectionState(SQLException e)
/*     */   {
/* 501 */     this.m_errorCount += 1;
/* 502 */     String errMsg = e.getMessage();
/* 503 */     if (errMsg == null)
/*     */     {
/* 506 */       errMsg = "";
/*     */     }
/*     */ 
/* 509 */     errMsg = errMsg.toLowerCase();
/* 510 */     if ((this.m_errorCount < m_maxErrorCount) && (errMsg.indexOf("connect") < 0) && (errMsg.indexOf("socket") < 0) && (errMsg.indexOf("session timeout") < 0)) {
/*     */       return;
/*     */     }
/* 513 */     this.m_isBadConnection = true;
/* 514 */     this.m_badConnectionMsg = errMsg;
/*     */   }
/*     */ 
/*     */   public boolean isBadConnection()
/*     */   {
/* 520 */     return this.m_isBadConnection;
/*     */   }
/*     */ 
/*     */   public void setActiveResultSet(JdbcResultSet rset)
/*     */   {
/* 525 */     this.m_activeResultSet = rset;
/*     */   }
/*     */ 
/*     */   public void setActiveCallableResult(JdbcCallableResults result)
/*     */   {
/* 530 */     this.m_callableResults = result;
/*     */   }
/*     */ 
/*     */   public void addPersistObject(Object obj)
/*     */   {
/* 535 */     this.m_persistObjectList.add(obj);
/*     */   }
/*     */ 
/*     */   public void prepareUse()
/*     */   {
/* 542 */     this.m_curId = (SystemUtils.getCurrentReportingThreadID(1) + "." + this.m_myCount);
/* 543 */     this.m_actionInterval.stop();
/* 544 */     this.m_actionInterval.reset();
/* 545 */     this.m_curAction = null;
/* 546 */     this.m_holdInterval.reset();
/* 547 */     this.m_holdInterval.start();
/* 548 */     this.m_hasWarnedLongActive = false;
/*     */ 
/* 551 */     setAutoCommit(true);
/* 552 */     this.m_curState = "Not In Transaction";
/*     */ 
/* 554 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 556 */     this.m_manager.debugMsg("Preparing connection for use, id initialized as " + this.m_curId);
/*     */   }
/*     */ 
/*     */   public void setAutoCommit(boolean isAuto)
/*     */   {
/*     */     try
/*     */     {
/* 564 */       this.m_connection.setAutoCommit(isAuto);
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 568 */       Report.trace(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 574 */     reset(true);
/*     */   }
/*     */ 
/*     */   public void reset(boolean resetAll)
/*     */   {
/* 579 */     if (this.m_activeResultSet != null)
/*     */     {
/* 581 */       if (Report.m_verbose)
/*     */       {
/* 583 */         this.m_manager.debugMsg("Closing active result set");
/*     */       }
/* 585 */       this.m_activeResultSet.closeInternals();
/* 586 */       this.m_activeResultSet = null;
/*     */     }
/* 588 */     if (this.m_callableResults != null)
/*     */     {
/* 590 */       if (Report.m_verbose)
/*     */       {
/* 592 */         this.m_manager.debugMsg("Closing callable results set.");
/*     */       }
/* 594 */       this.m_callableResults.close();
/* 595 */       this.m_callableResults = null;
/*     */     }
/*     */ 
/* 598 */     if (resetAll)
/*     */     {
/* 600 */       for (Iterator i$ = this.m_persistObjectList.iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*     */ 
/* 602 */         if (obj instanceof JdbcResultSet)
/*     */         {
/* 604 */           if (Report.m_verbose)
/*     */           {
/* 606 */             this.m_manager.debugMsg("Closing persisted jdbc results set.");
/*     */           }
/* 608 */           JdbcResultSet rset = (JdbcResultSet)obj;
/* 609 */           rset.closeInternals();
/*     */         }
/* 611 */         else if (obj instanceof JdbcCallableResults)
/*     */         {
/* 613 */           if (Report.m_verbose)
/*     */           {
/* 615 */             this.m_manager.debugMsg("Closing persisted jdbc callable results set.");
/*     */           }
/* 617 */           JdbcCallableResults jcr = (JdbcCallableResults)obj;
/* 618 */           jcr.close();
/*     */         } }
/*     */ 
/* 621 */       this.m_persistObjectList.clear();
/*     */     }
/*     */ 
/* 624 */     if (this.m_idleInterval == null)
/*     */       return;
/* 626 */     this.m_idleInterval.reset();
/* 627 */     this.m_idleInterval.start();
/*     */   }
/*     */ 
/*     */   protected void closePreparedStatements()
/*     */   {
/* 637 */     for (String name : this.m_preparedStatements)
/*     */     {
/*     */       try
/*     */       {
/* 641 */         removeQuery(name);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 645 */         if (Report.m_verbose)
/*     */         {
/* 647 */           this.m_manager.debugMsg("Error while removing query: " + name + "(" + e.getMessage() + ")");
/*     */         }
/*     */       }
/*     */     }
/* 651 */     this.m_preparedStatements.clear();
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/*     */     Enumeration en;
/*     */     try {
/* 658 */       reset(true);
/* 659 */       if (this.m_connection != null)
/*     */       {
/* 663 */         this.m_manager.debugMsg("closing connection: " + this.m_curId);
/* 664 */         this.m_connection.close();
/* 665 */         this.m_connection = null;
/*     */       }
/*     */ 
/* 669 */       for (en = this.m_queryDefs.elements(); en.hasMoreElements(); )
/*     */       {
/* 671 */         JdbcQueryDef queryDef = (JdbcQueryDef)en.nextElement();
/* 672 */         if (queryDef.m_statement != null)
/*     */         {
/* 674 */           queryDef.m_statement.close();
/* 675 */           queryDef.m_statement = null;
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 681 */       this.m_manager.debugMsg(LocaleUtils.encodeMessage("csJdbcErrorWhileClosingConnection", e.getMessage()));
/* 682 */       Report.trace(null, "Error closing JdbcConnection.", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void markTransactionState(boolean inTransaction)
/*     */   {
/* 691 */     this.m_inTransaction = inTransaction;
/*     */   }
/*     */ 
/*     */   public void updateIsolationLevel()
/*     */   {
/* 698 */     if (!this.m_canSetTransactionIsolation)
/*     */       return;
/* 700 */     int newLevel = (this.m_inTransaction) ? this.m_isolationLevelInTran : this.m_isolationLevelOutTran;
/*     */     try
/*     */     {
/* 703 */       if (newLevel != this.m_curIsolationLevel)
/*     */       {
/* 707 */         this.m_curIsolationLevel = newLevel;
/*     */ 
/* 709 */         this.m_connection.setTransactionIsolation(newLevel);
/* 710 */         this.m_connection.setAutoCommit(true);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/* 716 */       this.m_manager.debugMsg("Connection does not support changing transaction isolation level.");
/* 717 */       this.m_canSetTransactionIsolation = false;
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public long getIdleTimestamp()
/*     */   {
/* 728 */     return this.m_idleTimestamp;
/*     */   }
/*     */ 
/*     */   public long getIdleInterval()
/*     */   {
/* 733 */     return this.m_idleInterval.getInterval();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 738 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96600 $";
/*     */   }
/*     */ 
/*     */   public String getServiceName()
/*     */   {
/* 743 */     return this.m_serviceName;
/*     */   }
/*     */ 
/*     */   public String getStackTrace()
/*     */   {
/* 748 */     return this.m_stackTrace;
/*     */   }
/*     */ 
/*     */   public void setServiceName(String serviceName)
/*     */   {
/* 753 */     this.m_serviceName = serviceName;
/*     */   }
/*     */ 
/*     */   public void setStackTrace(String stackTrace)
/*     */   {
/* 758 */     this.m_stackTrace = stackTrace;
/*     */   }
/*     */ 
/*     */   public String getSubServiceName()
/*     */   {
/* 763 */     return this.m_subService;
/*     */   }
/*     */ 
/*     */   public void setSubServiceName(String subServiceName)
/*     */   {
/* 768 */     this.m_subService = subServiceName;
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.JdbcConnection
 * JD-Core Version:    0.5.4
 */