/*      */ package intradoc.provider;
/*      */ 
/*      */ import intradoc.common.ClassHelperUtils;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcMethodHolder;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IdcThreadLocalUtils;
/*      */ import intradoc.common.IntervalData;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.lang.BlockingQueue;
/*      */ import intradoc.lang.TimeoutQueueException;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.sql.Connection;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.List;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ProviderPoolManager
/*      */   implements ProviderConnectionManager
/*      */ {
/*   36 */   protected Provider m_provider = null;
/*   37 */   protected DataBinder m_connectionData = null;
/*      */ 
/*   40 */   protected boolean m_isForceSync = false;
/*   41 */   protected int m_attemptingReserve = 0;
/*   42 */   protected boolean m_allowSharedLocks = false;
/*      */ 
/*   45 */   protected BlockingQueue m_connectionPool = null;
/*   46 */   protected Hashtable m_activeConnections = null;
/*   47 */   protected Vector m_allConnections = null;
/*   48 */   protected Hashtable m_waitingConnections = null;
/*      */ 
/*   51 */   protected ProviderConnection m_lockedConnection = null;
/*   52 */   protected Vector m_reservedConnections = null;
/*      */ 
/*   55 */   public String m_traceSection = "providers";
/*      */ 
/*   58 */   protected int m_connectionTimeout = 60000;
/*      */ 
/*   61 */   protected int m_numActionsRead = 0;
/*   62 */   protected int m_numActionsWrite = 0;
/*      */ 
/*   65 */   protected Vector m_recentAuditMessages = null;
/*   66 */   protected int m_maxAuditingMessages = 25;
/*      */ 
/*   69 */   public boolean m_useExternalDataSource = false;
/*   70 */   protected String m_externalDataSource = null;
/*      */   protected Object m_externalDataSourceObject;
/*      */   protected IdcMethodHolder m_getConnectionMethod;
/*      */   protected IdcMethodHolder m_releaseConnectionMethod;
/*      */   protected IdcMethodHolder m_getInternalConnectionMethod;
/*      */   protected Object[] m_getInternalConnectionMethodArgs;
/*      */   protected LinkedHashMap<Object, ProviderConnection> m_processedExternalDataSourceConnections;
/*      */   protected int m_maxProcessedDataSourceConnections;
/*   83 */   public List m_connectionListeners = null;
/*      */ 
/*      */   public ProviderPoolManager()
/*      */   {
/*   91 */     this.m_activeConnections = new Hashtable();
/*   92 */     this.m_waitingConnections = new Hashtable();
/*      */ 
/*   94 */     this.m_reservedConnections = new IdcVector();
/*   95 */     this.m_recentAuditMessages = new IdcVector();
/*   96 */     this.m_connectionListeners = new IdcVector();
/*      */   }
/*      */ 
/*      */   public void init(Provider provider) throws DataException
/*      */   {
/*  101 */     this.m_provider = provider;
/*  102 */     this.m_connectionData = this.m_provider.getProviderData();
/*      */ 
/*  104 */     boolean allowDataSource = getEnvConfigBool("AllowDataSource", EnvUtils.isHostedInAppServer());
/*  105 */     this.m_useExternalDataSource = ((allowDataSource) && (getEnvConfigBool("UseDataSource", false)));
/*      */ 
/*  108 */     if (this.m_useExternalDataSource)
/*      */     {
/*  110 */       this.m_connectionData.putLocal("UsingDataSource", "1");
/*      */     }
/*  112 */     this.m_externalDataSource = getEnvConfigString("DataSource", null);
/*  113 */     if ((this.m_externalDataSource != null) && (this.m_externalDataSource.length() > 0))
/*      */     {
/*  116 */       this.m_connectionData.putLocal("DataSource", this.m_externalDataSource);
/*      */     }
/*      */ 
/*  120 */     if (this.m_useExternalDataSource)
/*      */     {
/*  122 */       this.m_processedExternalDataSourceConnections = new LinkedHashMap();
/*  123 */       this.m_maxProcessedDataSourceConnections = getEnvConfigInt("MaxProcessedDataSourceConnections", 200);
/*  124 */       if ((this.m_externalDataSource == null) || (this.m_externalDataSource.length() == 0))
/*      */       {
/*  126 */         throw new DataException(null, "csProviderExternalDataSourceNameMissing", new Object[0]);
/*      */       }
/*      */       try
/*      */       {
/*  130 */         computeExternalConnectionObject();
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  134 */         throw new DataException(e, "csProviderFailedToExtractAndUseDataSource", new Object[] { this.m_externalDataSource });
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  139 */       this.m_allConnections = new IdcVector();
/*  140 */       this.m_connectionPool = new BlockingQueue();
/*      */     }
/*      */ 
/*  147 */     if (!this.m_useExternalDataSource)
/*      */     {
/*  149 */       String driverStr = this.m_connectionData.getLocal("drivers");
/*  150 */       Vector drivers = StringUtils.parseArray(driverStr, ',', ',');
/*  151 */       int num = drivers.size();
/*  152 */       for (int i = 0; i < num; ++i)
/*      */       {
/*  154 */         String driver = (String)drivers.elementAt(i);
/*      */         try
/*      */         {
/*  157 */           Class.forName(driver);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  161 */           throw new DataException(e, "csProviderUnableToLoadDriver", new Object[] { driver });
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  166 */     this.m_traceSection = this.m_provider.getName().toLowerCase();
/*      */   }
/*      */ 
/*      */   public void initConnectionDefinitions()
/*      */     throws DataException
/*      */   {
/*  176 */     this.m_useExternalDataSource = getEnvConfigBool("UseDataSource", false);
/*  177 */     this.m_externalDataSource = getEnvConfigString("DataSource", null);
/*      */ 
/*  179 */     if (this.m_useExternalDataSource)
/*      */     {
/*  181 */       this.m_processedExternalDataSourceConnections = new LinkedHashMap();
/*  182 */       this.m_maxProcessedDataSourceConnections = getEnvConfigInt("MaxProcessedDataSourceConnections", 200);
/*  183 */       if ((this.m_externalDataSource == null) || (this.m_externalDataSource.length() == 0))
/*      */       {
/*  185 */         throw new DataException(null, "csProviderExternalDataSourceNameMissing", new Object[0]);
/*      */       }
/*      */       try
/*      */       {
/*  189 */         computeExternalConnectionObject();
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  193 */         throw new DataException(e, "csProviderFailedToExtractAndUseDataSource", new Object[] { this.m_externalDataSource });
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  198 */       this.m_allConnections = new IdcVector();
/*  199 */       this.m_connectionPool = new BlockingQueue();
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean usingExternalDataSource()
/*      */   {
/*  206 */     return this.m_useExternalDataSource;
/*      */   }
/*      */ 
/*      */   public void computeExternalConnectionObject() throws DataException, ServiceException
/*      */   {
/*  211 */     Object externalConnectionSource = null;
/*      */     try
/*      */     {
/*  214 */       String initialContextClassName = getEnvConfigString("InitialContextClass", "javax.naming.InitialContext");
/*  215 */       Class initialContextClass = Class.forName(initialContextClassName);
/*  216 */       Object initialContext = initialContextClass.newInstance();
/*  217 */       ServiceException excep = null;
/*      */       try
/*      */       {
/*  220 */         String lookupMethodName = getEnvConfigString("ConnectionLookupMethodName", "lookup");
/*  221 */         externalConnectionSource = ClassHelperUtils.executeMethodConvertToStandardExceptions(initialContext, lookupMethodName, new Object[] { this.m_externalDataSource });
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  226 */         excep = e;
/*      */       }
/*      */ 
/*  230 */       if (externalConnectionSource == null)
/*      */       {
/*  232 */         throw new DataException(excep, "csProviderCannotFindExternalDataSource", new Object[] { this.m_externalDataSource });
/*      */       }
/*      */ 
/*  236 */       String getConnectionMethodName = getEnvConfigString("GetConnectionFromPoolMethodName", "getConnection");
/*  237 */       IdcMethodHolder connectionGetMethod = ClassHelperUtils.getMethodHolder(externalConnectionSource.getClass(), getConnectionMethodName, null, 1);
/*      */ 
/*  239 */       this.m_externalDataSourceObject = externalConnectionSource;
/*  240 */       this.m_getConnectionMethod = connectionGetMethod;
/*      */ 
/*  243 */       Object rawConnection = getExternalRawConnection();
/*  244 */       Class rawConnectionClass = rawConnection.getClass();
/*      */ 
/*  250 */       setAppServerConnectionMethodAndArgs(rawConnectionClass);
/*      */ 
/*  252 */       String releaseConnectionMethodName = getEnvConfigString("ReleaseConnectionMethodName", "close");
/*  253 */       this.m_releaseConnectionMethod = ClassHelperUtils.getMethodHolder(rawConnectionClass, releaseConnectionMethodName, null, 1);
/*      */ 
/*  255 */       releaseExternalRawConnection(rawConnection);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  259 */       throw e;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  263 */       throw new ServiceException(e, "csProviderFailedToAccessDataSource", new Object[0]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setAppServerConnectionMethodAndArgs(Class rawConnectionClass)
/*      */     throws ServiceException
/*      */   {
/*  276 */     Class[] params = null;
/*      */ 
/*  278 */     String appServerConnectionMethod = "getConnectionEnv";
/*  279 */     String appServerName = EnvUtils.getAppServerType();
/*      */ 
/*  281 */     if (appServerName.equalsIgnoreCase("websphere"))
/*      */     {
/*  283 */       appServerConnectionMethod = "getNativeConnection";
/*  284 */       params = new Class[] { Long.TYPE };
/*      */ 
/*  288 */       long methodKey = 29497789L;
/*  289 */       this.m_getInternalConnectionMethodArgs = new Object[] { Long.valueOf(methodKey) };
/*      */     }
/*      */ 
/*  292 */     String getInternalConnectionMethodName = getEnvConfigString("GetInternalConnectionMethodName", appServerConnectionMethod);
/*  293 */     this.m_getInternalConnectionMethod = ClassHelperUtils.getMethodHolder(rawConnectionClass, getInternalConnectionMethodName, params, 1);
/*      */   }
/*      */ 
/*      */   public String getEnvConfigString(String key, String defVal)
/*      */   {
/*  300 */     if (this.m_connectionData == null)
/*      */     {
/*  302 */       return defVal;
/*      */     }
/*  304 */     String val = this.m_connectionData.getLocal(key);
/*  305 */     if (val == null)
/*      */     {
/*  307 */       String providerName = this.m_provider.getName();
/*  308 */       String envKey = providerName + ":" + key;
/*  309 */       val = this.m_connectionData.getAllowMissing(envKey);
/*      */     }
/*  311 */     if (val == null)
/*      */     {
/*  313 */       val = defVal;
/*      */     }
/*  315 */     return val;
/*      */   }
/*      */ 
/*      */   public boolean getEnvConfigBool(String key, boolean defVal)
/*      */   {
/*  320 */     String val = getEnvConfigString(key, null);
/*  321 */     return StringUtils.convertToBool(val, defVal);
/*      */   }
/*      */ 
/*      */   public int getEnvConfigInt(String key, int defVal)
/*      */   {
/*  326 */     String val = getEnvConfigString(key, null);
/*  327 */     return (int)NumberUtils.parseLong(val, defVal);
/*      */   }
/*      */ 
/*      */   public WorkspaceConfigImplementor createConfig(Provider provider) throws DataException
/*      */   {
/*  332 */     return (WorkspaceConfigImplementor)provider.createClass("WorkspaceConfigImplementor", "intradoc.jdbc.JdbcConfigImplementor");
/*      */   }
/*      */ 
/*      */   public void incrementActionCount(boolean isWrite)
/*      */   {
/*  338 */     if (isWrite)
/*      */     {
/*  340 */       this.m_numActionsWrite += 1;
/*  341 */       Report.trace(this.m_traceSection, null, "csMonitorWriteActions", new Object[] { Integer.valueOf(this.m_numActionsWrite) });
/*      */     }
/*      */     else
/*      */     {
/*  345 */       this.m_numActionsRead += 1;
/*  346 */       Report.trace(this.m_traceSection, null, "csMonitorReadActions", new Object[] { Integer.valueOf(this.m_numActionsRead) });
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addAuditMessage(String msg)
/*      */   {
/*  352 */     synchronized (this.m_recentAuditMessages)
/*      */     {
/*  354 */       int size = this.m_recentAuditMessages.size();
/*  355 */       if (size >= this.m_maxAuditingMessages)
/*      */       {
/*  357 */         this.m_recentAuditMessages.removeElementAt(0);
/*      */       }
/*  359 */       this.m_recentAuditMessages.addElement(msg);
/*  360 */       Report.trace(this.m_traceSection, null, "csMonitorTotalAuditMessages", new Object[] { Integer.valueOf(this.m_recentAuditMessages.size()) });
/*      */     }
/*      */   }
/*      */ 
/*      */   public int getConnectionTimeout()
/*      */   {
/*  367 */     return this.m_connectionTimeout;
/*      */   }
/*      */ 
/*      */   public void setConnectionTimeout(int timeout)
/*      */   {
/*  373 */     this.m_connectionTimeout = timeout;
/*      */   }
/*      */ 
/*      */   public ProviderConnection getConnection(String id)
/*      */     throws DataException
/*      */   {
/*  381 */     return getConnectionEx(id, 17);
/*      */   }
/*      */ 
/*      */   public ProviderConnection getConnectionEx(String id, int type)
/*      */     throws DataException
/*      */   {
/*  387 */     ProviderConnection con = null;
/*  388 */     if ((type & 0x1) != 0)
/*      */     {
/*  390 */       con = getActiveConnection(id);
/*  391 */       setServiceAndStackAuditMessages(con);
/*      */     }
/*      */ 
/*  394 */     if ((con == null) && ((type & 0x10) != 0))
/*      */     {
/*  396 */       con = assignConnection(id);
/*  397 */       if ((con.isBadConnection()) && (!this.m_useExternalDataSource))
/*      */       {
/*  400 */         ProviderConnection newCon = null;
/*      */         try
/*      */         {
/*  403 */           newCon = createConnection(null);
/*      */         }
/*      */         finally
/*      */         {
/*  407 */           if (newCon == null)
/*      */           {
/*  412 */             releaseConnection(id);
/*      */           }
/*      */         }
/*      */ 
/*  416 */         removeActiveConnection(id);
/*  417 */         setActiveConnection(id, newCon);
/*      */ 
/*  420 */         con.close();
/*  421 */         this.m_allConnections.removeElement(con);
/*  422 */         con = newCon;
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/*  427 */         con.prepareUse();
/*      */ 
/*  430 */         for (int i = 0; i < this.m_connectionListeners.size(); ++i)
/*      */         {
/*  432 */           ProviderConnectionListener listener = (ProviderConnectionListener)this.m_connectionListeners.get(i);
/*      */ 
/*  434 */           listener.prepareConnection(id, type, this);
/*      */         }
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/*  439 */         Report.trace(this.m_traceSection, null, ignore);
/*      */       }
/*      */     }
/*  442 */     return con;
/*      */   }
/*      */ 
/*      */   public synchronized void reserveAccess(ProviderConnection con, boolean isTranStart)
/*      */     throws DataException
/*      */   {
/*  448 */     if (isTranStart)
/*      */     {
/*  451 */       for (int i = 0; i < this.m_connectionListeners.size(); ++i)
/*      */       {
/*  453 */         ProviderConnectionListener listener = (ProviderConnectionListener)this.m_connectionListeners.get(i);
/*      */ 
/*  455 */         listener.beginTran(this);
/*      */       }
/*      */     }
/*  458 */     if (!this.m_isForceSync)
/*      */     {
/*  460 */       return;
/*      */     }
/*      */ 
/*  463 */     ProviderConnectionStatus conStatus = null;
/*  464 */     String id = null;
/*  465 */     if (con instanceof ProviderConnectionStatus)
/*      */     {
/*  467 */       conStatus = (ProviderConnectionStatus)con;
/*  468 */       id = conStatus.getId();
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  473 */       if (conStatus != null)
/*      */       {
/*  475 */         this.m_waitingConnections.put(id, conStatus);
/*      */       }
/*      */ 
/*  479 */       boolean isError = false;
/*      */ 
/*  481 */       this.m_attemptingReserve += 1;
/*  482 */       long startTime = System.currentTimeMillis();
/*  483 */       boolean isFirst = true;
/*      */ 
/*  485 */       while (!isError)
/*      */       {
/*  487 */         boolean fullReserve = (!isFirst) || (this.m_attemptingReserve == 1);
/*  488 */         if (attemptReserve(con, isTranStart, fullReserve)) {
/*      */           break;
/*      */         }
/*      */ 
/*  492 */         long curTime = System.currentTimeMillis();
/*  493 */         long left = this.m_connectionTimeout - (curTime - startTime);
/*      */ 
/*  495 */         if (left > 0L)
/*      */         {
/*      */           try
/*      */           {
/*  499 */             String tName = Thread.currentThread().getName();
/*  500 */             if (!isFirst)
/*      */             {
/*  502 */               Report.trace(null, tName + " waiting for connection at " + new Date(), null);
/*      */             }
/*      */ 
/*  505 */             super.wait(left);
/*      */           }
/*      */           catch (InterruptedException e)
/*      */           {
/*  509 */             isError = true;
/*      */           }
/*      */ 
/*      */         }
/*      */         else {
/*  514 */           isError = true;
/*      */         }
/*  516 */         isFirst = false;
/*      */       }
/*  518 */       this.m_attemptingReserve -= 1;
/*      */ 
/*  520 */       if (isError)
/*      */       {
/*  522 */         String msg = LocaleUtils.encodeMessage("csProviderLocked", null, this.m_provider.getName());
/*      */ 
/*  524 */         throw new DataException(msg);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  529 */       if (id != null)
/*      */       {
/*  531 */         this.m_waitingConnections.remove(id);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public synchronized void releaseAccess(ProviderConnection con, boolean isEndTran)
/*      */   {
/*  538 */     if (isEndTran)
/*      */     {
/*  541 */       for (int i = 0; i < this.m_connectionListeners.size(); ++i)
/*      */       {
/*  543 */         ProviderConnectionListener listener = (ProviderConnectionListener)this.m_connectionListeners.get(i);
/*      */ 
/*  545 */         listener.commitTran(this);
/*      */       }
/*      */     }
/*  548 */     if (!this.m_isForceSync)
/*      */     {
/*  550 */       return;
/*      */     }
/*      */ 
/*  553 */     if ((isEndTran == true) && (this.m_lockedConnection == con))
/*      */     {
/*  555 */       this.m_lockedConnection = null;
/*  556 */       super.notify();
/*  557 */       debugLockingMsg("make lock not exclusive");
/*      */     }
/*  559 */     for (int i = 0; i < this.m_reservedConnections.size(); ++i)
/*      */     {
/*  561 */       ProviderConnection c = (ProviderConnection)this.m_reservedConnections.elementAt(i);
/*  562 */       if (c != con)
/*      */         continue;
/*  564 */       this.m_reservedConnections.removeElementAt(i);
/*  565 */       if (this.m_lockedConnection == null)
/*      */       {
/*  567 */         super.notify();
/*      */       }
/*  569 */       debugLockingMsg("release lock");
/*      */     }
/*      */   }
/*      */ 
/*      */   public void releaseConnection(String id)
/*      */   {
/*  576 */     ProviderConnection con = removeActiveConnection(id);
/*  577 */     if (con != null)
/*      */     {
/*      */       try
/*      */       {
/*  584 */         releaseAccess(con, true);
/*  585 */         con.reset();
/*      */       }
/*      */       finally
/*      */       {
/*  589 */         debugMsg("release pool connection");
/*  590 */         if (!this.m_useExternalDataSource)
/*      */         {
/*  592 */           this.m_connectionPool.insert(con);
/*      */         }
/*      */         else
/*      */         {
/*  596 */           releaseRawConnection(con);
/*      */         }
/*      */       }
/*      */     }
/*      */     int i;
/*  602 */     for (int i = 0; i < this.m_connectionListeners.size(); ++i)
/*      */     {
/*  604 */       ProviderConnectionListener listener = (ProviderConnectionListener)this.m_connectionListeners.get(i);
/*      */ 
/*  606 */       listener.releaseConnection(id, this);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void releaseRawConnection(ProviderConnection conn)
/*      */   {
/*  617 */     Connection rawCon = (Connection)conn.getRawConnection();
/*      */     try
/*      */     {
/*  621 */       releaseExternalRawConnection(rawCon);
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/*  625 */       Report.trace("systemdatabase", null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void closeConnection(ProviderConnection conn)
/*      */   {
/*  638 */     if (this.m_useExternalDataSource)
/*      */     {
/*  640 */       Object rawCon = conn.getRawConnection();
/*  641 */       if (rawCon != null)
/*      */       {
/*  643 */         unregisterExternalConnection(rawCon, conn);
/*      */       }
/*      */     }
/*  646 */     conn.close();
/*      */   }
/*      */ 
/*      */   public void setForceSync(boolean isForce)
/*      */   {
/*  651 */     this.m_isForceSync = isForce;
/*      */   }
/*      */ 
/*      */   public void addConnectionToPool()
/*      */     throws DataException
/*      */   {
/*  657 */     ProviderConnection con = createConnection(null);
/*  658 */     this.m_connectionPool.insert(con);
/*      */   }
/*      */ 
/*      */   public ProviderConnection createConnection(Object rawCon)
/*      */     throws DataException
/*      */   {
/*  670 */     ProviderConnection con = null;
/*      */     try
/*      */     {
/*  673 */       con = (ProviderConnection)this.m_provider.createClass("ProviderConnection", null);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  677 */       String msg = LocaleUtils.encodeMessage("csProviderUnableToCreateConnection", e.getMessage(), this.m_provider.getName());
/*      */ 
/*  679 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  684 */       initConnection(con, rawCon, 0);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  688 */       if (this.m_provider.isSystemProvider())
/*      */       {
/*  690 */         throw e;
/*      */       }
/*      */     }
/*  693 */     if (!this.m_useExternalDataSource)
/*      */     {
/*  695 */       this.m_allConnections.addElement(con);
/*      */     }
/*      */ 
/*  698 */     return con;
/*      */   }
/*      */ 
/*      */   public void initConnection(ProviderConnection con, Object rawCon, int flags) throws DataException
/*      */   {
/*  703 */     con.init(this, this.m_connectionData, null, rawCon, flags, null);
/*      */   }
/*      */ 
/*      */   public ProviderConnection getRegisteredExternalConnection(Object rawCon)
/*      */   {
/*  709 */     ProviderConnection con = null;
/*  710 */     synchronized (this.m_processedExternalDataSourceConnections)
/*      */     {
/*  712 */       con = (ProviderConnection)this.m_processedExternalDataSourceConnections.get(rawCon);
/*      */     }
/*  714 */     return con;
/*      */   }
/*      */ 
/*      */   public void registerExternalConnection(Object rawCon, ProviderConnection con)
/*      */   {
/*      */     Iterator i$;
/*  719 */     synchronized (this.m_processedExternalDataSourceConnections)
/*      */     {
/*  721 */       this.m_processedExternalDataSourceConnections.put(rawCon, con);
/*  722 */       if (this.m_processedExternalDataSourceConnections.size() > this.m_maxProcessedDataSourceConnections)
/*      */       {
/*  724 */         Set keys = this.m_processedExternalDataSourceConnections.keySet();
/*  725 */         for (i$ = keys.iterator(); i$.hasNext(); ) { Object existingCon = i$.next();
/*      */ 
/*  727 */           this.m_processedExternalDataSourceConnections.remove(existingCon);
/*  728 */           int curSize = this.m_processedExternalDataSourceConnections.size();
/*  729 */           if (curSize <= 0) break; if (curSize <= this.m_maxProcessedDataSourceConnections)
/*      */             break; }
/*      */ 
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void unregisterExternalConnection(Object rawCon, ProviderConnection con)
/*      */   {
/*  740 */     if (rawCon == null)
/*      */       return;
/*  742 */     synchronized (this.m_processedExternalDataSourceConnections)
/*      */     {
/*  744 */       this.m_processedExternalDataSourceConnections.remove(rawCon);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Vector getAllConnections()
/*      */   {
/*  751 */     return this.m_allConnections;
/*      */   }
/*      */ 
/*      */   public synchronized void cleanUp()
/*      */   {
/*      */     Iterator i$;
/*  757 */     if (this.m_useExternalDataSource)
/*      */     {
/*  760 */       Set keys = this.m_activeConnections.keySet();
/*  761 */       for (i$ = keys.iterator(); i$.hasNext(); ) { Object key = i$.next();
/*      */ 
/*  763 */         ProviderConnection conn = (ProviderConnection)this.m_activeConnections.get(key);
/*      */         try
/*      */         {
/*  766 */           releaseRawConnection(conn);
/*      */         }
/*      */         catch (Throwable ignore)
/*      */         {
/*  770 */           Report.trace(null, null, ignore);
/*      */         } }
/*      */ 
/*      */     }
/*  774 */     this.m_activeConnections.clear();
/*      */ 
/*  776 */     Report.trace(this.m_traceSection, null, "csMonitorActiveDbConnections", new Object[] { Integer.valueOf(this.m_activeConnections.size()) });
/*      */ 
/*  779 */     if (this.m_useExternalDataSource)
/*      */     {
/*  781 */       this.m_processedExternalDataSourceConnections.clear();
/*      */     }
/*      */     else
/*      */     {
/*  785 */       int size = this.m_connectionPool.size();
/*  786 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  788 */         this.m_connectionPool.remove();
/*      */       }
/*      */ 
/*  791 */       size = this.m_allConnections.size();
/*  792 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  794 */         ProviderConnection con = (ProviderConnection)this.m_allConnections.elementAt(i);
/*  795 */         con.close();
/*      */       }
/*  797 */       this.m_allConnections.removeAllElements();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void debugMsg(String msg)
/*      */   {
/*  806 */     Report.trace(this.m_traceSection, msg, null);
/*      */   }
/*      */ 
/*      */   public void debugLockingMsg(String msg)
/*      */   {
/*  811 */     if (!SystemUtils.m_verbose)
/*      */       return;
/*  813 */     Report.debug(this.m_traceSection, msg, null);
/*      */   }
/*      */ 
/*      */   public void printThreadMsg(String msg)
/*      */   {
/*  819 */     Report.trace(this.m_traceSection, msg, null);
/*      */   }
/*      */ 
/*      */   public void setDebugTraceFlags()
/*      */   {
/*      */   }
/*      */ 
/*      */   protected boolean attemptReserve(ProviderConnection con, boolean isTranStart, boolean fullAccess)
/*      */   {
/*  832 */     if (this.m_lockedConnection == con)
/*      */     {
/*  834 */       return true;
/*      */     }
/*  836 */     if (this.m_lockedConnection != null)
/*      */     {
/*  838 */       return false;
/*      */     }
/*  840 */     if (!fullAccess)
/*      */     {
/*  842 */       return false;
/*      */     }
/*      */ 
/*  845 */     for (int i = 0; i < this.m_reservedConnections.size(); ++i)
/*      */     {
/*  847 */       ProviderConnection c = (ProviderConnection)this.m_reservedConnections.elementAt(i);
/*  848 */       if (c == con)
/*      */       {
/*  850 */         if (isTranStart)
/*      */         {
/*  852 */           debugLockingMsg("make lock exclusive");
/*  853 */           this.m_lockedConnection = con;
/*      */         }
/*  855 */         if (!this.m_allowSharedLocks)
/*      */         {
/*  857 */           this.m_lockedConnection = con;
/*      */         }
/*  859 */         return true;
/*      */       }
/*  861 */       if (!this.m_allowSharedLocks)
/*      */       {
/*  863 */         return false;
/*      */       }
/*      */     }
/*      */ 
/*  867 */     this.m_reservedConnections.addElement(con);
/*  868 */     if (isTranStart)
/*      */     {
/*  870 */       debugLockingMsg("reserve lock and make exclusive");
/*  871 */       this.m_lockedConnection = con;
/*      */     }
/*      */     else
/*      */     {
/*  875 */       debugLockingMsg("reserve lock");
/*      */     }
/*      */ 
/*  878 */     return true;
/*      */   }
/*      */ 
/*      */   protected ProviderConnection assignConnection(String id) throws DataException
/*      */   {
/*  883 */     ProviderConnection con = null;
/*  884 */     IntervalData interval = new IntervalData("connect");
/*      */     try
/*      */     {
/*  888 */       debugLockingMsg("assigning connection");
/*  889 */       con = getNextAvailablePoolConnection();
/*  890 */       setActiveConnection(id, con);
/*  891 */       if (SystemUtils.isActiveTrace(this.m_traceSection))
/*      */       {
/*  893 */         interval.stop();
/*  894 */         IdcStringBuilder str = new IdcStringBuilder();
/*  895 */         str.append("Assigned connection to this thread, took ");
/*  896 */         interval.appendTo(str);
/*  897 */         debugMsg(str.toString());
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  902 */       String msg = LocaleUtils.encodeMessage("csNoConnectionsAvailable", e.getMessage(), this.m_provider.getName());
/*      */ 
/*  904 */       DataException dataException = new DataException(msg);
/*  905 */       SystemUtils.setExceptionCause(dataException, e);
/*  906 */       throw dataException;
/*      */     }
/*  908 */     return con;
/*      */   }
/*      */ 
/*      */   public ProviderConnection getNextAvailablePoolConnection()
/*      */     throws TimeoutQueueException, DataException, ServiceException
/*      */   {
/*  914 */     ProviderConnection con = null;
/*  915 */     if (this.m_useExternalDataSource)
/*      */     {
/*  917 */       Object rawConnection = getExternalRawConnection();
/*  918 */       Object internalRaw = getInternalConnectionInsideRawConnection(rawConnection);
/*  919 */       con = getRegisteredExternalConnection(internalRaw);
/*  920 */       if (Report.m_verbose)
/*      */       {
/*  922 */         Report.trace("systemdatabasepool", "InternalRaw:" + internalRaw + ", RawConn:" + rawConnection + ", JdbcCon:" + con, null);
/*      */       }
/*      */ 
/*  925 */       if (con == null)
/*      */       {
/*  927 */         con = createConnection(rawConnection);
/*  928 */         registerExternalConnection(internalRaw, con);
/*  929 */         if (Report.m_verbose)
/*      */         {
/*  931 */           debugMsg("New connection retrieved from external pool.");
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  936 */         initConnection(con, rawConnection, 3);
/*      */ 
/*  938 */         debugMsg("Reusing connection retrieved from external pool.");
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  943 */       con = (ProviderConnection)this.m_connectionPool.removeWithTimeout(this.m_connectionTimeout);
/*      */     }
/*  945 */     return con;
/*      */   }
/*      */ 
/*      */   public Object getExternalRawConnection() throws DataException, ServiceException
/*      */   {
/*  950 */     return ClassHelperUtils.executeIdcMethodConvertToStandardExceptions(this.m_externalDataSourceObject, this.m_getConnectionMethod, new Object[0]);
/*      */   }
/*      */ 
/*      */   public void releaseExternalRawConnection(Object externalConnection)
/*      */     throws DataException, ServiceException
/*      */   {
/*  956 */     ClassHelperUtils.executeIdcMethodConvertToStandardExceptions(externalConnection, this.m_releaseConnectionMethod, new Object[0]);
/*      */   }
/*      */ 
/*      */   public Object getInternalConnectionInsideRawConnection(Object rawConnection)
/*      */     throws DataException, ServiceException
/*      */   {
/*  963 */     return ClassHelperUtils.executeIdcMethodConvertToStandardExceptions(rawConnection, this.m_getInternalConnectionMethod, this.m_getInternalConnectionMethodArgs);
/*      */   }
/*      */ 
/*      */   protected ProviderConnection getActiveConnection(String id)
/*      */     throws DataException
/*      */   {
/*  970 */     synchronized (this.m_activeConnections)
/*      */     {
/*  972 */       return (ProviderConnection)this.m_activeConnections.get(id);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void setActiveConnection(String id, ProviderConnection con)
/*      */   {
/*  978 */     synchronized (this.m_activeConnections)
/*      */     {
/*  980 */       this.m_activeConnections.put(id, con);
/*  981 */       setServiceAndStackAuditMessages(con);
/*      */ 
/*  983 */       Report.trace(this.m_traceSection, null, "csMonitorActiveDbConnections", new Object[] { Integer.valueOf(this.m_activeConnections.size()) });
/*      */ 
/*  986 */       if (SystemUtils.m_verbose)
/*      */       {
/*  988 */         if (con instanceof ProviderConnectionStatus)
/*      */         {
/*  990 */           debugMsg("Connection with last id of " + ((ProviderConnectionStatus)con).getId() + " is added to active connections with key of '" + id + "'.");
/*      */         }
/*      */         else
/*      */         {
/*  995 */           debugMsg("Connection is added to active connections with key of '" + id + "'.");
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected ProviderConnection removeActiveConnection(String id)
/*      */   {
/* 1003 */     synchronized (this.m_activeConnections)
/*      */     {
/* 1005 */       ProviderConnection con = (ProviderConnection)this.m_activeConnections.remove(id);
/*      */ 
/* 1007 */       if (con != null)
/*      */       {
/* 1009 */         Report.trace(this.m_traceSection, null, "csMonitorActiveDbConnections", new Object[] { Integer.valueOf(this.m_activeConnections.size()) });
/*      */       }
/*      */ 
/* 1013 */       if ((SystemUtils.m_verbose) && (con != null))
/*      */       {
/* 1015 */         if (con instanceof ProviderConnectionStatus)
/*      */         {
/* 1017 */           debugMsg("Connection with id of '" + ((ProviderConnectionStatus)con).getId() + "' is removed from active connections with key of '" + id + "'.");
/*      */         }
/*      */         else
/*      */         {
/* 1022 */           debugMsg("Connection is removed from active connections with key of '" + id + "'.");
/*      */         }
/*      */       }
/* 1025 */       return con;
/*      */     }
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 1032 */     return "Object: " + super.toString() + "\n" + createAuditReportAsMessage();
/*      */   }
/*      */ 
/*      */   public String createAuditReport()
/*      */   {
/* 1037 */     DataBinder binder = new DataBinder();
/* 1038 */     createAuditReportData(binder);
/* 1039 */     IdcCharArrayWriter writer = new IdcCharArrayWriter();
/*      */     try
/*      */     {
/* 1042 */       binder.send(writer);
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 1046 */       Report.trace(this.m_traceSection, null, ignore);
/*      */     }
/* 1048 */     return writer.toStringRelease();
/*      */   }
/*      */ 
/*      */   public void createAuditReportData(DataBinder binder)
/*      */   {
/* 1053 */     binder.putLocal("numActionsRead", "" + this.m_numActionsRead);
/* 1054 */     binder.putLocal("numActionsWrite", "" + this.m_numActionsWrite);
/* 1055 */     if (!this.m_useExternalDataSource)
/*      */     {
/* 1057 */       binder.putLocal("numBlockedThreads", "" + this.m_connectionPool.namesOfBlockedThreads().size());
/* 1058 */       binder.putLocal("numWaitingConnections", "" + this.m_waitingConnections.size());
/*      */     }
/*      */     else
/*      */     {
/* 1062 */       binder.putLocal("numBlockedThreads", "N/A");
/* 1063 */       binder.putLocal("numWaitingConnections", "N/A");
/*      */     }
/* 1065 */     if (this.m_activeConnections != null)
/*      */     {
/* 1067 */       DataResultSet drset = new DataResultSet(new String[] { "key", "id", "state", "action", "actionID", "actionStatus", "timePending", "timeActive", "serviceName", "subServiceName", "stackTrace" });
/*      */ 
/* 1069 */       synchronized (this.m_activeConnections)
/*      */       {
/* 1071 */         long curTime = System.currentTimeMillis();
/* 1072 */         Enumeration e = this.m_activeConnections.keys();
/* 1073 */         while (e.hasMoreElements())
/*      */         {
/* 1075 */           Vector v = new IdcVector();
/* 1076 */           String key = (String)e.nextElement();
/* 1077 */           v.addElement(key);
/* 1078 */           ProviderConnection con = (ProviderConnection)this.m_activeConnections.get(key);
/* 1079 */           if (con instanceof ProviderConnectionStatus)
/*      */           {
/* 1081 */             ProviderConnectionStatus status = (ProviderConnectionStatus)con;
/* 1082 */             String id = status.getId();
/* 1083 */             String state = status.getCurrentState();
/* 1084 */             String action = status.getCurrentAction();
/* 1085 */             String actionID = status.getActionID();
/* 1086 */             String actionStatus = status.getCurrentActionStatus();
/* 1087 */             String serviceName = status.getServiceName();
/* 1088 */             String subServiceName = status.getSubServiceName();
/* 1089 */             String stackTrace = status.getStackTrace();
/*      */ 
/* 1091 */             long timePending = status.computeTimePending(curTime) / 1000L;
/* 1092 */             long timeActive = status.computeTimeActive(curTime) / 1000L;
/* 1093 */             v.addElement(id);
/* 1094 */             if (state == null)
/*      */             {
/* 1096 */               state = "";
/*      */             }
/* 1098 */             v.addElement(state);
/* 1099 */             if (action == null)
/*      */             {
/* 1101 */               action = "";
/*      */             }
/* 1103 */             v.addElement(action);
/* 1104 */             if (actionID == null)
/*      */             {
/* 1106 */               actionID = "";
/*      */             }
/* 1108 */             v.addElement(actionID);
/* 1109 */             if (actionStatus == null)
/*      */             {
/* 1111 */               actionStatus = "";
/*      */             }
/* 1113 */             v.addElement(actionStatus);
/* 1114 */             v.addElement("" + timePending);
/* 1115 */             v.addElement("" + timeActive);
/* 1116 */             v.addElement(serviceName);
/* 1117 */             v.addElement(subServiceName);
/* 1118 */             v.addElement(stackTrace);
/*      */           }
/*      */           else
/*      */           {
/* 1122 */             for (int i = 0; i < 5; ++i)
/*      */             {
/* 1124 */               v.addElement("");
/*      */             }
/*      */           }
/* 1127 */           drset.addRow(v);
/*      */         }
/*      */       }
/* 1130 */       binder.addResultSet("ActiveConnections", drset);
/*      */     }
/*      */ 
/* 1133 */     binder.setFieldType("auditMessage", "message");
/* 1134 */     synchronized (this.m_recentAuditMessages)
/*      */     {
/* 1136 */       DataResultSet drset = new DataResultSet(new String[] { "auditMessage" });
/* 1137 */       int n = this.m_recentAuditMessages.size();
/* 1138 */       if (n > 0)
/*      */       {
/* 1140 */         for (int i = n - 1; i >= 0; --i)
/*      */         {
/* 1142 */           Vector v = new IdcVector();
/* 1143 */           String msg = this.m_recentAuditMessages.elementAt(i).toString();
/* 1144 */           v.addElement(msg);
/* 1145 */           drset.addRow(v);
/*      */         }
/*      */       }
/* 1148 */       binder.addResultSet("AuditMessages", drset);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setServiceAndStackAuditMessages(ProviderConnection con)
/*      */   {
/* 1159 */     if ((con == null) || (!con instanceof ProviderConnectionStatus))
/*      */       return;
/* 1161 */     ProviderConnectionStatus status = (ProviderConnectionStatus)con;
/* 1162 */     status.setServiceName(IdcThreadLocalUtils.get("IdcServiceName"));
/* 1163 */     status.setSubServiceName(IdcThreadLocalUtils.get("IdcSubServiceName"));
/*      */ 
/* 1165 */     StackTraceElement[] currentStackTrace = Thread.currentThread().getStackTrace();
/* 1166 */     IdcStringBuilder msg = new IdcStringBuilder();
/* 1167 */     int i = 0;
/* 1168 */     for (StackTraceElement s : currentStackTrace)
/*      */     {
/* 1170 */       if (i < 2)
/*      */       {
/* 1172 */         ++i;
/*      */       }
/*      */       else
/* 1175 */         msg.append(s.toString() + "\r\n");
/*      */     }
/* 1177 */     status.setStackTrace(msg.toString());
/*      */   }
/*      */ 
/*      */   public String createAuditReportAsMessage()
/*      */   {
/* 1183 */     StringBuffer report = new StringBuffer();
/* 1184 */     report.append(LocaleUtils.encodeMessage("csCacheNumReadActions", null, "" + this.m_numActionsRead));
/* 1185 */     report.append(LocaleUtils.encodeMessage("csCacheNumWriteActions", null, "" + this.m_numActionsWrite));
/* 1186 */     report.append(LocaleUtils.encodeMessage("csCacheWaitingForConnectionMsg", null, "" + this.m_connectionPool.namesOfBlockedThreads().size()));
/*      */ 
/* 1188 */     report.append(LocaleUtils.encodeMessage("csCacheWaitingForDatabaseAction", null, "" + this.m_waitingConnections.size()));
/*      */ 
/* 1190 */     report.append("!csCacheActiveConnectionsMsg");
/*      */ 
/* 1192 */     if ((this.m_activeConnections == null) || (this.m_activeConnections.size() == 0))
/*      */     {
/* 1194 */       report.append("!csCacheNoActiveConnectionsMsg");
/*      */     }
/*      */     else
/*      */     {
/* 1198 */       synchronized (this.m_activeConnections)
/*      */       {
/* 1200 */         long curTime = System.currentTimeMillis();
/* 1201 */         Enumeration e = this.m_activeConnections.keys();
/* 1202 */         while (e.hasMoreElements())
/*      */         {
/* 1204 */           String key = (String)e.nextElement();
/* 1205 */           ProviderConnection con = (ProviderConnection)this.m_activeConnections.get(key);
/* 1206 */           if (con instanceof ProviderConnectionStatus)
/*      */           {
/* 1208 */             ProviderConnectionStatus status = (ProviderConnectionStatus)con;
/* 1209 */             String id = status.getId();
/* 1210 */             String state = status.getCurrentState();
/* 1211 */             String action = status.getCurrentAction();
/* 1212 */             long timePending = status.computeTimePending(curTime) / 1000L;
/* 1213 */             long timeActive = status.computeTimeActive(curTime) / 1000L;
/* 1214 */             String reportLine = LocaleUtils.encodeMessage("csCacheActiveConnId", null, id);
/* 1215 */             if ((state != null) && (state.length() > 0))
/*      */             {
/* 1217 */               reportLine = reportLine + LocaleUtils.encodeMessage("csCacheActiveConnState", null, state);
/*      */             }
/* 1219 */             reportLine = reportLine + LocaleUtils.encodeMessage("csCacheActiveConnTimeActive", null, new StringBuilder().append("").append(timeActive).toString());
/*      */ 
/* 1221 */             if ((action != null) && (action.length() > 0))
/*      */             {
/* 1223 */               reportLine = reportLine + LocaleUtils.encodeMessage("csCacheActiveConnActionPending", null, new StringBuilder().append("").append(timePending).toString(), action);
/*      */             }
/*      */ 
/* 1226 */             report.append(reportLine);
/*      */           }
/*      */           else
/*      */           {
/* 1230 */             report.append("!csCacheActiveConnIdUnknown");
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1235 */     report.append("!csCacheRecentAuditMsg");
/* 1236 */     synchronized (this.m_recentAuditMessages)
/*      */     {
/* 1238 */       int n = this.m_recentAuditMessages.size();
/* 1239 */       if (n == 0)
/*      */       {
/* 1241 */         report.append("!csCacheNoRecentAuditMsg");
/*      */       }
/*      */       else
/*      */       {
/* 1245 */         for (int i = n - 1; i >= 0; --i)
/*      */         {
/* 1247 */           report.append("!csCacheRecentAuditMsgSep");
/* 1248 */           report.append(this.m_recentAuditMessages.elementAt(i).toString());
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1253 */     return report.toString();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1258 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98083 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ProviderPoolManager
 * JD-Core Version:    0.5.4
 */