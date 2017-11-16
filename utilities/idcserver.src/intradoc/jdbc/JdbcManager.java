/*     */ package intradoc.jdbc;
/*     */ 
/*     */ import intradoc.common.IntervalData;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Log;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DatabaseConfigData;
/*     */ import intradoc.data.DatabaseTypes;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.lang.BlockingQueue;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.ProviderConnection;
/*     */ import intradoc.provider.ProviderPoolManager;
/*     */ import intradoc.util.IdcConcurrentHashMap;
/*     */ import java.sql.Connection;
/*     */ import java.sql.Statement;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JdbcManager extends ProviderPoolManager
/*     */ {
/*  35 */   protected DatabaseConfigData m_config = null;
/*  36 */   protected boolean m_isDatabaseTypeDetermined = false;
/*     */ 
/*  38 */   protected Throwable m_rememberedError = null;
/*     */ 
/*  41 */   protected IdcConcurrentHashMap m_queryDefs = null;
/*  42 */   protected Properties m_columnMap = null;
/*  43 */   protected Properties m_errorColumnMap = null;
/*  44 */   protected HashMap m_taggedQuery = null;
/*     */ 
/*  47 */   protected Thread m_keepAliveThread = null;
/*     */ 
/*     */   public JdbcManager()
/*     */   {
/*  51 */     this.m_queryDefs = new IdcConcurrentHashMap(false);
/*  52 */     this.m_queryDefs.initCaseInsensitiveKeyMap(true);
/*  53 */     this.m_columnMap = new IdcProperties();
/*  54 */     this.m_errorColumnMap = new IdcProperties();
/*  55 */     this.m_taggedQuery = new HashMap();
/*     */   }
/*     */ 
/*     */   public void init(Provider provider)
/*     */     throws DataException
/*     */   {
/*  61 */     DataBinder conData = provider.getProviderData();
/*     */     try
/*     */     {
/*  68 */       String drivers = conData.getAllowMissing("JdbcDriver");
/*  69 */       if (drivers != null)
/*     */       {
/*  71 */         conData.putLocal("drivers", drivers);
/*  72 */         if (drivers.startsWith("com.internetcds.jdbc.tds.Driver"))
/*     */         {
/*  74 */           String msg = LocaleUtils.encodeMessage("csDbUnsupportedJdbcDriver", null, drivers);
/*  75 */           throw new DataException(msg);
/*     */         }
/*     */       }
/*     */ 
/*  79 */       super.init(provider);
/*  80 */       String className = this.m_connectionData.getAllowMissing("ProviderConnection");
/*  81 */       if (className == null)
/*     */       {
/*  83 */         className = "intradoc.jdbc.JdbcConnection";
/*     */       }
/*  85 */       this.m_connectionData.putLocal("ProviderConnection", className);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  90 */       String msg = LocaleUtils.encodeMessage("csDbUnableToInitalize", e.getMessage(), provider.getName());
/*     */ 
/*  92 */       throw new DataException(msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void startKeepAlivePingConnections()
/*     */   {
/*  98 */     int interval = this.m_config.getValueAsInt("DatabaseConnectionKeepAliveInterval", -1);
/*  99 */     String testQuery = this.m_config.getValueAsString("TestQuery");
/* 100 */     if ((testQuery == null) || (testQuery.length() == 0))
/*     */     {
/* 102 */       testQuery = "SELECT * FROM CONFIG WHERE 1 = 0";
/*     */     }
/*     */ 
/* 105 */     String healthQuery = testQuery;
/* 106 */     if (interval == -1)
/*     */       return;
/* 108 */     BlockingQueue connectionPool = this.m_connectionPool;
/* 109 */     this.m_keepAliveThread = new Thread("JDBC Connection KeepAlive(" + this.m_provider.getName() + ")", interval, connectionPool, healthQuery)
/*     */     {
/*     */       int m_timeout;
/*     */ 
/*     */       public void run()
/*     */       {
/* 116 */         SystemUtils.registerSynchronizationObjectToNotifyOnStop(this);
/* 117 */         while (!SystemUtils.m_isServerStopped)
/*     */         {
/* 119 */           boolean isCheckAll = false;
/*     */           try
/*     */           {
/* 122 */             synchronized (this)
/*     */             {
/* 124 */               SystemUtils.wait(this, this.m_timeout);
/*     */             }
/*     */           }
/*     */           catch (InterruptedException e)
/*     */           {
/* 129 */             if (!SystemUtils.m_isServerStopped)
/*     */             {
/* 131 */               isCheckAll = true;
/*     */ 
/* 133 */               Report.trace(null, "Jdbc Connection keep alive thread waked up.", e);
/*     */             }
/*     */           }
/* 136 */           if (this.val$connectionPool.isEmpty()) {
/*     */             continue;
/*     */           }
/*     */ 
/* 140 */           if (!SystemUtils.m_isServerStopped)
/*     */           {
/* 142 */             checkConnections(isCheckAll);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/*     */       public void checkConnections(boolean isCheckAll)
/*     */       {
/* 149 */         int pingedCount = 0;
/* 150 */         int aliveCount = 0;
/* 151 */         int maxDataSourceCount = 5;
/* 152 */         int totalIdleCount = this.val$connectionPool.size();
/*     */         while (true)
/*     */         {
/* 155 */           JdbcConnection con = null;
/* 156 */           if (!JdbcManager.this.m_useExternalDataSource)
/*     */           {
/* 158 */             synchronized (this.val$connectionPool)
/*     */             {
/* 160 */               if (this.val$connectionPool.empty())
/*     */               {
/*     */                 break label287;
/*     */               }
/*     */ 
/* 165 */               con = (JdbcConnection)this.val$connectionPool.elementAt(0);
/* 166 */               if ((!isCheckAll) && (con.getIdleInterval() / 1000000L < this.m_timeout)) {
/*     */                 break label287;
/*     */               }
/*     */ 
/* 170 */               this.val$connectionPool.removeElementAt(0);
/*     */             }
/*     */ 
/*     */           }
/*     */           else {
/*     */             try
/*     */             {
/* 177 */               con = (JdbcConnection)JdbcManager.this.getNextAvailablePoolConnection();
/*     */             }
/*     */             catch (Exception e)
/*     */             {
/* 183 */               Report.trace("system", e, "Failed to get data source connection for ping", new Object[0]);
/*     */             }
/*     */           }
/* 186 */           if (con == null) {
/*     */             break;
/*     */           }
/*     */ 
/* 190 */           if (!con.isBadConnection())
/*     */           {
/* 192 */             boolean isAlive = keepAlive(con);
/* 193 */             con.m_isBadConnection = (!isAlive);
/*     */           }
/* 195 */           con.m_idleInterval.reset();
/* 196 */           con.m_idleInterval.start();
/*     */ 
/* 198 */           if (!con.isBadConnection())
/*     */           {
/* 200 */             ++aliveCount;
/*     */           }
/*     */ 
/* 203 */           synchronized (this.val$connectionPool)
/*     */           {
/* 205 */             this.val$connectionPool.addElement(con);
/*     */           }
/* 207 */           ++pingedCount;
/* 208 */           if ((JdbcManager.this.m_useExternalDataSource) && (pingedCount >= maxDataSourceCount)) break; if ((!JdbcManager.this.m_useExternalDataSource) && (pingedCount > totalIdleCount)) {
/*     */             break;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 214 */         label287: JdbcManager.this.debugMsg(aliveCount + " of " + pingedCount + " database connections are alive.");
/*     */       }
/*     */ 
/*     */       public boolean keepAlive(JdbcConnection con)
/*     */       {
/* 219 */         Connection c = (Connection)con.getConnection();
/* 220 */         Statement s = null;
/* 221 */         java.sql.ResultSet r = null;
/* 222 */         boolean isAlive = true;
/*     */         try
/*     */         {
/* 225 */           s = c.createStatement();
/* 226 */           r = s.executeQuery(this.val$healthQuery);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 230 */           isAlive = false;
/* 231 */           Report.trace("system", null, e);
/*     */         }
/*     */         finally
/*     */         {
/*     */           try
/*     */           {
/* 237 */             if (r != null)
/*     */             {
/* 239 */               r.close();
/*     */             }
/* 241 */             if (s != null)
/*     */             {
/* 243 */               s.close();
/*     */             }
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 248 */             isAlive = false;
/* 249 */             Report.trace(null, null, e);
/*     */           }
/*     */         }
/* 252 */         return isAlive;
/*     */       }
/*     */     };
/* 257 */     this.m_keepAliveThread.setDaemon(true);
/* 258 */     this.m_keepAliveThread.start();
/*     */   }
/*     */ 
/*     */   // ERROR //
/*     */   public void releaseConnection(String id)
/*     */   {
/*     */     // Byte code:
/*     */     //   0: aload_0
/*     */     //   1: aload_1
/*     */     //   2: invokevirtual 55	intradoc/jdbc/JdbcManager:getActiveConnection	(Ljava/lang/String;)Lintradoc/provider/ProviderConnection;
/*     */     //   5: checkcast 56	intradoc/jdbc/JdbcConnection
/*     */     //   8: astore_2
/*     */     //   9: aload_2
/*     */     //   10: ifnull +36 -> 46
/*     */     //   13: aload_2
/*     */     //   14: getfield 57	intradoc/jdbc/JdbcConnection:m_inTransaction	Z
/*     */     //   17: ifeq +29 -> 46
/*     */     //   20: new 58	intradoc/common/ServiceException
/*     */     //   23: dup
/*     */     //   24: ldc 59
/*     */     //   26: invokespecial 60	intradoc/common/ServiceException:<init>	(Ljava/lang/String;)V
/*     */     //   29: astore_3
/*     */     //   30: aload_3
/*     */     //   31: athrow
/*     */     //   32: astore_3
/*     */     //   33: aconst_null
/*     */     //   34: ldc 61
/*     */     //   36: aload_3
/*     */     //   37: invokestatic 62	intradoc/common/Report:error	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */     //   40: aload_0
/*     */     //   41: ldc 63
/*     */     //   43: invokevirtual 64	intradoc/jdbc/JdbcManager:debugMsg	(Ljava/lang/String;)V
/*     */     //   46: goto +17 -> 63
/*     */     //   49: astore_2
/*     */     //   50: getstatic 65	intradoc/common/SystemUtils:m_verbose	Z
/*     */     //   53: ifeq +10 -> 63
/*     */     //   56: ldc 66
/*     */     //   58: aconst_null
/*     */     //   59: aload_2
/*     */     //   60: invokestatic 67	intradoc/common/Report:debug	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */     //   63: aload_0
/*     */     //   64: aload_1
/*     */     //   65: invokespecial 68	intradoc/provider/ProviderPoolManager:releaseConnection	(Ljava/lang/String;)V
/*     */     //   68: return
/*     */     //
/*     */     // Exception table:
/*     */     //   from	to	target	type
/*     */     //   20	32	32	intradoc/common/ServiceException
/*     */     //   0	46	49	intradoc/data/DataException
/*     */   }
/*     */ 
/*     */   public void releaseRawConnection(ProviderConnection conn) {
/* 310 */     Connection con = (Connection)conn.getRawConnection();
/*     */     try
/*     */     {
/* 314 */       con.close();
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 318 */       Report.trace("systemdatabase", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Map getQueryDefs()
/*     */   {
/* 326 */     return this.m_queryDefs;
/*     */   }
/*     */ 
/*     */   public synchronized JdbcQueryDef getQueryDef(String name)
/*     */   {
/* 331 */     return (JdbcQueryDef)this.m_queryDefs.get(name);
/*     */   }
/*     */ 
/*     */   public synchronized void addQueryDefs(intradoc.data.ResultSet rset) throws DataException
/*     */   {
/* 336 */     Hashtable newQueries = new Hashtable();
/*     */ 
/* 338 */     int nameIndex = -1;
/* 339 */     FieldInfo info = new FieldInfo();
/* 340 */     if (rset.getFieldInfo("name", info))
/*     */     {
/* 342 */       nameIndex = info.m_index;
/*     */     }
/* 344 */     int queryIndex = -1;
/* 345 */     if (rset.getFieldInfo("queryStr", info))
/*     */     {
/* 347 */       queryIndex = info.m_index;
/*     */     }
/* 349 */     int parameterIndex = -1;
/* 350 */     if (rset.getFieldInfo("parameters", info))
/*     */     {
/* 352 */       parameterIndex = info.m_index;
/*     */     }
/*     */ 
/* 356 */     String dbName = getDBType().toUpperCase();
/* 357 */     for (; rset.isRowPresent(); rset.next())
/*     */     {
/* 359 */       boolean isPrepared = false;
/* 360 */       boolean isCallable = false;
/* 361 */       boolean hasQueryCatLabel = false;
/* 362 */       String name = rset.getStringValue(nameIndex);
/*     */ 
/* 365 */       String[] disectedQuery = disectQuery(name);
/* 366 */       name = disectedQuery[2];
/* 367 */       if (disectedQuery[0] != null)
/*     */       {
/* 369 */         hasQueryCatLabel = true;
/* 370 */         if ((disectedQuery[0].equalsIgnoreCase(dbName)) || (disectedQuery[0].equalsIgnoreCase("JDBC")) || (disectedQuery[0].equalsIgnoreCase("CALLABLE")) || (disectedQuery[0].equalsIgnoreCase("PREPARED")))
/*     */         {
/* 377 */           if ((disectedQuery[0].equalsIgnoreCase("CALLABLE")) || ((disectedQuery[1] != null) && (disectedQuery[1].equalsIgnoreCase("CALLABLE"))))
/*     */           {
/* 379 */             isPrepared = true;
/* 380 */             isCallable = true;
/*     */           }
/* 382 */           else if ((disectedQuery[0].equalsIgnoreCase("PREPARED")) || ((disectedQuery[1] != null) && (disectedQuery[1].equalsIgnoreCase("PREPARED"))))
/*     */           {
/* 384 */             isPrepared = true;
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 390 */           name = null;
/*     */         }
/*     */       }
/* 393 */       if ((name == null) || (name.length() == 0)) continue; if ((!hasQueryCatLabel) && (this.m_taggedQuery.get(name) != null))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 398 */       if ((hasQueryCatLabel) && (this.m_taggedQuery.get(name) == null))
/*     */       {
/* 400 */         this.m_taggedQuery.put(name, "1");
/*     */       }
/*     */ 
/* 403 */       String query = rset.getStringValue(queryIndex);
/* 404 */       JdbcQueryDef qDef = new JdbcQueryDef(name, query, isPrepared, isCallable);
/*     */ 
/* 406 */       String params = rset.getStringValue(parameterIndex);
/* 407 */       qDef.parseAndAddParams(params);
/*     */ 
/* 409 */       this.m_queryDefs.put(qDef.m_name, qDef);
/* 410 */       newQueries.put(qDef.m_name, qDef);
/*     */     }
/*     */ 
/* 414 */     if (this.m_allConnections == null)
/*     */       return;
/* 416 */     int size = this.m_allConnections.size();
/* 417 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 419 */       JdbcConnection con = (JdbcConnection)this.m_allConnections.elementAt(i);
/* 420 */       if (con.isBadConnection())
/*     */         continue;
/* 422 */       con.addQueries(newQueries);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeQuery(String name)
/*     */     throws DataException
/*     */   {
/* 430 */     this.m_queryDefs.remove(name);
/*     */ 
/* 432 */     if (this.m_allConnections == null)
/*     */       return;
/* 434 */     int size = this.m_allConnections.size();
/* 435 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 437 */       JdbcConnection con = (JdbcConnection)this.m_allConnections.elementAt(i);
/* 438 */       con.removeQuery(name);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadColumnMap(DataResultSet rset)
/*     */   {
/* 446 */     int columnIndex = -1;
/* 447 */     FieldInfo info = new FieldInfo();
/* 448 */     if (rset.getFieldInfo("column", info))
/*     */     {
/* 450 */       columnIndex = info.m_index;
/*     */     }
/* 452 */     int aliasIndex = -1;
/* 453 */     if (rset.getFieldInfo("alias", info))
/*     */     {
/* 455 */       aliasIndex = info.m_index;
/*     */     }
/* 457 */     for (; rset.isRowPresent(); rset.next())
/*     */     {
/* 459 */       String column = rset.getStringValue(columnIndex);
/* 460 */       String alias = rset.getStringValue(aliasIndex);
/*     */ 
/* 462 */       if ((column == null) || (column.length() <= 0) || (alias == null) || (alias.length() <= 0))
/*     */         continue;
/* 464 */       String tmp = this.m_columnMap.getProperty(column);
/* 465 */       if (tmp != null)
/*     */       {
/* 467 */         if ((tmp.equals(alias)) || (this.m_errorColumnMap.containsKey(alias)))
/*     */           continue;
/* 469 */         this.m_errorColumnMap.put(alias, "1");
/* 470 */         String msg = LocaleUtils.encodeMessage("csJdbcErrorOverrideColumnMap", null, tmp, alias, column);
/* 471 */         Log.warnEx(msg, "database");
/* 472 */         debugMsg("Warning: tried to override column '" + column + "' mapping from '" + tmp + "' to '" + alias + "'");
/*     */       }
/*     */       else
/*     */       {
/* 476 */         this.m_columnMap.put(column, alias);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public Properties getColumnMap()
/*     */   {
/* 483 */     return this.m_columnMap;
/*     */   }
/*     */ 
/*     */   protected String[] disectQuery(String query)
/*     */   {
/* 488 */     String[] labels = new String[3];
/* 489 */     labels[2] = query;
/* 490 */     int startIndex = query.indexOf(40);
/* 491 */     if (startIndex < 0)
/*     */     {
/* 493 */       return labels;
/*     */     }
/* 495 */     int endIndex = query.indexOf(41, startIndex);
/* 496 */     if (endIndex < 0)
/*     */     {
/* 498 */       return labels;
/*     */     }
/*     */ 
/* 501 */     int dotIndex = query.indexOf(".", startIndex);
/* 502 */     if ((dotIndex > endIndex) || (dotIndex < startIndex))
/*     */     {
/* 504 */       dotIndex = endIndex;
/*     */     }
/* 506 */     labels[0] = query.substring(startIndex + 1, dotIndex);
/* 507 */     if (dotIndex < endIndex - 1)
/*     */     {
/* 509 */       labels[1] = query.substring(dotIndex + 1, endIndex);
/*     */     }
/* 511 */     labels[2] = query.substring(endIndex + 1);
/* 512 */     return labels;
/*     */   }
/*     */ 
/*     */   public void setConfig(DatabaseConfigData config)
/*     */   {
/* 518 */     this.m_config = config;
/*     */   }
/*     */ 
/*     */   public DatabaseConfigData getConfig()
/*     */   {
/* 523 */     return this.m_config;
/*     */   }
/*     */ 
/*     */   public void checkAndLogDDL(String query)
/*     */   {
/* 528 */     if (this.m_config.getValueAsBool("DisableDatabaseLog", false))
/*     */       return;
/* 530 */     JdbcQueryUtils.checkAndLogDDL(query, this.m_provider.getName());
/*     */   }
/*     */ 
/*     */   public String getDescription()
/*     */   {
/* 537 */     return this.m_config.getValueAsString("JdbcConnectionString");
/*     */   }
/*     */ 
/*     */   public String getDBType()
/*     */   {
/* 542 */     return this.m_config.getValueAsString("DatabaseType");
/*     */   }
/*     */ 
/*     */   public boolean supportsSqlColumnDelete()
/*     */   {
/* 547 */     return this.m_config.getValueAsBool("SupportSqlColumnDelete", false);
/*     */   }
/*     */ 
/*     */   public boolean supportsSqlColumnChange()
/*     */   {
/* 552 */     return this.m_config.getValueAsBool("SupportSqlColumnChange", false);
/*     */   }
/*     */ 
/*     */   public String getColumnChangeSqlCommand()
/*     */   {
/* 557 */     return this.m_config.getValueAsString("ColumnChangeCommand");
/*     */   }
/*     */ 
/*     */   public String getColumnDeleteSqlCommand()
/*     */   {
/* 562 */     return this.m_config.getValueAsString("ColumnDeleteCommand");
/*     */   }
/*     */ 
/*     */   public boolean useUnicode()
/*     */   {
/* 567 */     return this.m_config.getValueAsBool("IsUniversalUnicodeDatabase", false);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void setUniversalUnicodeDatabase(boolean useUnicode)
/*     */   {
/* 577 */     this.m_config.setConfigValue("IsUniversalUnicodeDatabase", Boolean.valueOf(useUnicode));
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void setUseShortIndexName(boolean useShortIndexName)
/*     */   {
/* 587 */     this.m_config.setConfigValue("UseDatabaseShortIndexName", Boolean.valueOf(useShortIndexName));
/*     */   }
/*     */ 
/*     */   public boolean useShortIndexName()
/*     */   {
/* 592 */     return this.m_config.getValueAsBool("UseDatabaseShortIndexName", false);
/*     */   }
/*     */ 
/*     */   public void setNumBytesPerChar(int bytesPerChar)
/*     */   {
/* 601 */     this.m_config.setConfigValue("NumBytesPerCharInDB", new Integer(bytesPerChar));
/*     */   }
/*     */ 
/*     */   public int getNumBytesPerChar()
/*     */   {
/* 606 */     return this.m_config.getValueAsInt("NumBytesPerCharInDB", 1);
/*     */   }
/*     */ 
/*     */   public String getDatabaseVersion()
/*     */   {
/* 611 */     return this.m_config.getValueAsString("DatabaseVersion");
/*     */   }
/*     */ 
/*     */   public void setDisableDateEnhancement(boolean disable)
/*     */   {
/* 620 */     this.m_config.setConfigValue("DisableDBDateEnhancement", Boolean.valueOf(disable));
/*     */   }
/*     */ 
/*     */   public boolean getDisableDateEnhancement()
/*     */   {
/* 625 */     return this.m_config.getValueAsBool("DisableDBDateEnhancement", false);
/*     */   }
/*     */ 
/*     */   public boolean supportsSavepoints()
/*     */   {
/* 630 */     return this.m_config.getValueAsBool("SupportsSavepoints", true);
/*     */   }
/*     */ 
/*     */   public boolean supportsChangeFetchSize()
/*     */   {
/* 635 */     return this.m_config.getValueAsBool("SupportFetchSizeChange", true);
/*     */   }
/*     */ 
/*     */   public boolean allowEmptyTableList()
/*     */   {
/* 640 */     return this.m_config.getValueAsBool("AllowEmptyTableList", true);
/*     */   }
/*     */ 
/*     */   public void setAllowEmptyTableList(boolean allowEmptyTableList)
/*     */   {
/* 645 */     this.m_config.setConfigValue("AllowEmptyTableList", (allowEmptyTableList) ? "1" : "0");
/*     */   }
/*     */ 
/*     */   protected ProviderConnection getActiveConnection(String id)
/*     */     throws DataException
/*     */   {
/* 652 */     return super.getActiveConnection(id);
/*     */   }
/*     */ 
/*     */   public String getDatabaseName()
/*     */   {
/* 657 */     return this.m_config.getValueAsString("DatabaseName");
/*     */   }
/*     */ 
/*     */   public boolean isSqlServer()
/*     */   {
/* 662 */     return this.m_config.getValueAsString("DatabaseType").equals(DatabaseTypes.MSSQL);
/*     */   }
/*     */ 
/*     */   public boolean isOracle()
/*     */   {
/* 667 */     return this.m_config.getValueAsString("DatabaseType").equals(DatabaseTypes.ORACLE);
/*     */   }
/*     */ 
/*     */   public boolean isInformix()
/*     */   {
/* 672 */     return this.m_config.getValueAsString("DatabaseType").equals(DatabaseTypes.INFORMIX);
/*     */   }
/*     */ 
/*     */   public boolean isSybase()
/*     */   {
/* 677 */     return this.m_config.getValueAsString("DatabaseType").equals(DatabaseTypes.SYBASE);
/*     */   }
/*     */ 
/*     */   public boolean isDB2()
/*     */   {
/* 682 */     return this.m_config.getValueAsString("DatabaseType").equals(DatabaseTypes.DB2);
/*     */   }
/*     */ 
/*     */   public boolean isPostgreSQL()
/*     */   {
/* 687 */     return this.m_config.getValueAsString("DatabaseType").equals(DatabaseTypes.POSTGRESQL);
/*     */   }
/*     */ 
/*     */   public boolean isDerby()
/*     */   {
/* 692 */     return this.m_config.getValueAsString("DatabaseType").equals(DatabaseTypes.DERBY);
/*     */   }
/*     */ 
/*     */   public boolean isTamino()
/*     */   {
/* 697 */     return this.m_config.getValueAsString("DatabaseType").equals(DatabaseTypes.TAMINO);
/*     */   }
/*     */ 
/*     */   public boolean isTamino3()
/*     */   {
/* 702 */     return this.m_config.getValueAsString("DatabaseType").equals(DatabaseTypes.TAMINO3);
/*     */   }
/*     */ 
/*     */   public boolean isMySQL()
/*     */   {
/* 707 */     return this.m_config.getValueAsString("DatabaseType").equals(DatabaseTypes.MYSQL);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 713 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 91997 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.JdbcManager
 * JD-Core Version:    0.5.4
 */