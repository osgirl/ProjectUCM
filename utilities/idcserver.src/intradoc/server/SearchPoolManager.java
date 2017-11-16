/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.lang.BlockingQueue;
/*     */ import intradoc.provider.ProviderConnection;
/*     */ import intradoc.provider.ProviderPoolManager;
/*     */ import intradoc.search.CommonSearchConnection;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SearchPoolManager extends ProviderPoolManager
/*     */ {
/*     */   protected int m_maxConnections;
/*     */   protected int m_connectionWaitTimeout;
/*     */   protected int m_connectionKeepAliveTimeout;
/*     */   protected String m_engineName;
/*     */   protected String m_defaultClass;
/*     */   protected Properties m_env;
/*     */ 
/*     */   public SearchPoolManager(String engineName, String defaultClass, Properties env)
/*     */   {
/*  48 */     this.m_engineName = engineName;
/*  49 */     this.m_defaultClass = defaultClass;
/*  50 */     this.m_maxConnections = 5;
/*  51 */     this.m_connectionWaitTimeout = 300000;
/*  52 */     this.m_connectionKeepAliveTimeout = 300000;
/*  53 */     this.m_env = env;
/*     */ 
/*  56 */     this.m_traceSection = "searchquery";
/*     */   }
/*     */ 
/*     */   public void init() throws DataException
/*     */   {
/*  61 */     initConnectionDefinitions();
/*     */ 
/*  63 */     initConnectionShare();
/*     */   }
/*     */ 
/*     */   protected void initConnectionShare()
/*     */     throws DataException
/*     */   {
/*  69 */     this.m_maxConnections = SharedObjects.getEnvironmentInt("MaxSearchConnections", this.m_maxConnections);
/*     */ 
/*  73 */     this.m_connectionWaitTimeout = SharedObjects.getTypedEnvironmentInt("SearchConnectionWaitTimeout", this.m_connectionWaitTimeout, 18, 18);
/*     */ 
/*  78 */     this.m_connectionKeepAliveTimeout = SharedObjects.getTypedEnvironmentInt("SearchConnectionKeepAliveTimeout", this.m_connectionKeepAliveTimeout, 18, 18);
/*     */ 
/*  84 */     for (int i = 0; i < this.m_maxConnections; ++i)
/*     */     {
/*  86 */       addConnectionToPool();
/*     */     }
/*     */   }
/*     */ 
/*     */   public ProviderConnection createConnection(Object rawCon)
/*     */     throws DataException
/*     */   {
/*  94 */     CommonSearchConnection con = null;
/*     */     try
/*     */     {
/*  97 */       con = new CommonSearchConnection();
/*  98 */       DataBinder binder = new DataBinder(this.m_env);
/*  99 */       con.init(this, binder, this.m_defaultClass, rawCon, 0, null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 103 */       String msg = LocaleUtils.encodeMessage("csSearchUnableToCreateConnection", e.getMessage());
/*     */ 
/* 105 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 108 */     this.m_allConnections.addElement(con);
/*     */ 
/* 110 */     return con;
/*     */   }
/*     */ 
/*     */   public void forceRefreshCurrentConnections(boolean useTimeout) throws ServiceException
/*     */   {
/* 115 */     long curTime = System.currentTimeMillis();
/* 116 */     synchronized (this.m_activeConnections)
/*     */     {
/* 118 */       for (int i = 0; i < this.m_allConnections.size(); ++i)
/*     */       {
/* 120 */         CommonSearchConnection con = (CommonSearchConnection)this.m_allConnections.elementAt(i);
/* 121 */         long lastOpenTs = con.getTimeStampLastOpen();
/* 122 */         if ((useTimeout) && (((lastOpenTs <= 0L) || (curTime - lastOpenTs <= this.m_connectionKeepAliveTimeout))))
/*     */           continue;
/* 124 */         Enumeration e = this.m_activeConnections.elements();
/* 125 */         boolean isActive = false;
/* 126 */         while (e.hasMoreElements())
/*     */         {
/* 128 */           CommonSearchConnection activeCon = (CommonSearchConnection)e.nextElement();
/* 129 */           if (activeCon == con)
/*     */           {
/* 131 */             isActive = true;
/* 132 */             break;
/*     */           }
/*     */         }
/*     */ 
/* 136 */         con.setIsBadConnection(true);
/* 137 */         if (isActive)
/*     */           continue;
/* 139 */         con.reset();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected ProviderConnection assignConnection(String id)
/*     */     throws DataException
/*     */   {
/* 149 */     ProviderConnection con = null;
/*     */     try
/*     */     {
/* 153 */       debugLockingMsg("assigning connection");
/*     */ 
/* 155 */       con = (ProviderConnection)this.m_connectionPool.removeWithTimeout(this.m_connectionWaitTimeout);
/* 156 */       setActiveConnection(id, con);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 160 */       throw new DataException("!csProviderNoConnections");
/*     */     }
/* 162 */     return con;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 167 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98386 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SearchPoolManager
 * JD-Core Version:    0.5.4
 */