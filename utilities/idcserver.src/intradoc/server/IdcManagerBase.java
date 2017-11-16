/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IntervalData;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.VersionInfo;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderProtocolInterface;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataSerialize;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.IncomingProvider;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.server.proxy.OutgoingProviderMonitor;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.SharedUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.IOException;
/*     */ import java.sql.Driver;
/*     */ import java.sql.DriverManager;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ 
/*     */ public abstract class IdcManagerBase
/*     */ {
/*     */   protected static final int m_pauseTimeout = 10000;
/*     */   protected static final int m_stopTimeout = 10000;
/*  58 */   protected static IntervalData m_serverStartTime = null;
/*     */   public Workspace m_workspace;
/*     */   protected static int m_port;
/*     */   protected String m_errorMsg;
/*     */ 
/*     */   public IdcManagerBase()
/*     */   {
/*  60 */     this.m_workspace = null;
/*     */ 
/*  62 */     this.m_errorMsg = null;
/*     */   }
/*     */ 
/*     */   protected abstract int getThreadCount();
/*     */ 
/*     */   public static long getUpTime()
/*     */   {
/*  74 */     if (m_serverStartTime == null)
/*     */     {
/*  76 */       return 0L;
/*     */     }
/*  78 */     return m_serverStartTime.getInterval();
/*     */   }
/*     */ 
/*     */   public abstract String getServiceDisplayName();
/*     */ 
/*     */   public abstract void init()
/*     */     throws DataException, ServiceException;
/*     */ 
/*     */   protected void startProviders()
/*     */     throws ServiceException, DataException
/*     */   {
/*  98 */     String msg = LocaleUtils.encodeMessage("csUnableToStartProviders", null, getServiceDisplayName());
/*     */     try
/*     */     {
/* 103 */       IdcSystemLoader.prepareStartMonitorProviders(false);
/*     */ 
/* 105 */       if (this.m_workspace != null)
/*     */       {
/* 108 */         Object protocolObj = ComponentClassFactory.createClassInstance("DataBinderProtocol", "intradoc.server.DataBinderProtocolImplementor", "!csDataBinderProtocolMissing");
/*     */ 
/* 112 */         DataBinderProtocolInterface dataBinderProtocol = (DataBinderProtocolInterface)protocolObj;
/*     */ 
/* 115 */         DataBinder data = new DataBinder(SharedObjects.getSecureEnvironment());
/* 116 */         dataBinderProtocol.init(this.m_workspace, data, IdcSystemLoader.m_extendedLoader);
/*     */ 
/* 118 */         DataSerialize ds = DataSerializeUtils.getDataSerialize();
/* 119 */         ds.setDataBinderProtocol(dataBinderProtocol);
/*     */ 
/* 122 */         ServiceManager mgr = new ServiceManager();
/* 123 */         DataBinder binder = new DataBinder();
/* 124 */         mgr.init(binder, this.m_workspace);
/* 125 */         UserService service = (UserService)ServiceManager.getInitializedService("GET_USERS", binder, this.m_workspace);
/*     */ 
/* 127 */         service.refreshUsers();
/*     */       }
/*     */ 
/* 131 */       IdcSystemLoader.startMonitoringProviders();
/*     */ 
/* 134 */       boolean isReady = true;
/* 135 */       for (int count = 0; count < 3; ++count)
/*     */       {
/* 137 */         isReady = true;
/* 138 */         Hashtable providers = Providers.getProviders();
/* 139 */         for (Enumeration en = providers.elements(); en.hasMoreElements(); )
/*     */         {
/* 141 */           Provider provider = (Provider)en.nextElement();
/* 142 */           if (provider.isInError()) continue; if (!provider.isEnabled()) {
/*     */             continue;
/*     */           }
/*     */ 
/* 146 */           Object providerObj = provider.getProvider();
/* 147 */           if (providerObj instanceof IncomingProvider)
/*     */           {
/* 149 */             IncomingProvider ip = (IncomingProvider)providerObj;
/* 150 */             if (!ip.isReady())
/*     */             {
/* 152 */               isReady = false;
/* 153 */               SystemUtils.sleep(1000 * (count + 1));
/* 154 */               break;
/*     */             }
/*     */           }
/*     */         }
/* 158 */         if (isReady) {
/*     */           break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 164 */       if (!isReady)
/*     */       {
/* 166 */         Report.warning(null, null, "csProviderAllIncomingNotReady", new Object[0]);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 171 */       SharedUtils.handleCommonException(e, msg, 9);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 176 */       SharedUtils.handleCommonException(e, msg, 1);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 180 */       SharedUtils.handleCommonException(e, msg, 1);
/*     */     }
/*     */     finally
/*     */     {
/* 184 */       Providers.releaseConnections();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void stopProviders()
/*     */   {
/* 194 */     List providers = Providers.getProvidersOfType("incoming");
/* 195 */     if (providers == null)
/*     */     {
/* 197 */       return;
/*     */     }
/* 199 */     for (Iterator i$ = providers.iterator(); i$.hasNext(); ) { Object pObj = i$.next();
/*     */ 
/* 201 */       Provider p = (Provider)pObj;
/* 202 */       Object providerObj = p.getProvider();
/* 203 */       if (providerObj instanceof IncomingProvider)
/*     */       {
/* 205 */         IncomingProvider ip = (IncomingProvider)providerObj;
/* 206 */         ip.close();
/*     */       } }
/*     */ 
/* 209 */     OutgoingProviderMonitor.stopMonitor();
/*     */   }
/*     */ 
/*     */   public void stopAndClearAllProviders()
/*     */   {
/* 217 */     OutgoingProviderMonitor.stopMonitor();
/* 218 */     List providers = Providers.getProviderList();
/* 219 */     if (providers == null)
/*     */     {
/* 221 */       return;
/*     */     }
/* 223 */     for (Iterator i$ = providers.iterator(); i$.hasNext(); ) { Object pObj = i$.next();
/*     */ 
/* 225 */       Provider p = (Provider)pObj;
/* 226 */       Object providerObj = p.getProvider();
/* 227 */       if (providerObj instanceof IncomingProvider)
/*     */       {
/* 229 */         IncomingProvider ip = (IncomingProvider)providerObj;
/* 230 */         ip.close();
/*     */       }
/* 232 */       p.stopProvider(true); }
/*     */ 
/* 234 */     Providers.clearAll();
/* 235 */     Enumeration drivers = DriverManager.getDrivers();
/* 236 */     ArrayList driversToUnload = new ArrayList();
/* 237 */     ClassLoader ourClassLoader = super.getClass().getClassLoader();
/* 238 */     if (ourClassLoader == null)
/*     */       return;
/* 240 */     while (drivers.hasMoreElements())
/*     */     {
/* 242 */       Driver driver = (Driver)drivers.nextElement();
/* 243 */       ClassLoader driverClassLoader = driver.getClass().getClassLoader();
/* 244 */       if ((driverClassLoader != null) && (driverClassLoader.equals(ourClassLoader)))
/*     */       {
/* 246 */         driversToUnload.add(driver);
/*     */       }
/*     */     }
/*     */     try
/*     */     {
/* 251 */       for (Driver driver : driversToUnload)
/*     */       {
/* 253 */         DriverManager.deregisterDriver(driver);
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 258 */       Report.trace(null, null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void servicePause()
/*     */   {
/* 270 */     Report.info(null, null, "csPausingService", new Object[] { getServiceDisplayName() });
/*     */ 
/* 272 */     long stamp = System.currentTimeMillis();
/* 273 */     long stopTime = stamp + 10000L;
/* 274 */     while ((System.currentTimeMillis() < stopTime) && (getThreadCount() > 0))
/*     */     {
/* 276 */       SystemUtils.sleep(2000L);
/*     */     }
/* 278 */     int numThreads = getThreadCount();
/* 279 */     if (numThreads > 0)
/*     */     {
/* 281 */       Report.error(null, null, "csServiceUnableToPause1", new Object[] { getServiceDisplayName(), "10", "" + numThreads });
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 288 */         stopProviders();
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 292 */         Report.error(null, e, "csServiceUnableToPause2", new Object[] { getServiceDisplayName() });
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void serviceContinue()
/*     */   {
/* 303 */     Report.info(null, null, "csServiceContinuing", new Object[] { getServiceDisplayName() });
/*     */     try
/*     */     {
/* 306 */       startProviders();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 310 */       Report.error(null, e, "csServiceUnableToContinue", new Object[] { getServiceDisplayName() });
/*     */     }
/*     */   }
/*     */ 
/*     */   public void serviceStop()
/*     */   {
/* 319 */     Report.info(null, null, "csServiceStopping", new Object[] { getServiceDisplayName() });
/*     */ 
/* 321 */     long stopTime = System.currentTimeMillis() + 10000L;
/* 322 */     while ((System.currentTimeMillis() < stopTime) && (getThreadCount() > 0))
/*     */     {
/* 324 */       SystemUtils.sleep(2000L);
/*     */     }
/* 326 */     int numThreads = getThreadCount();
/* 327 */     if (numThreads > 0)
/*     */     {
/* 329 */       Report.error(null, null, "csServiceStopThreadCount", new Object[] { "" + numThreads });
/*     */     }
/*     */     try
/*     */     {
/* 333 */       stopProviders();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 337 */       Report.error(null, e, "csServiceUnableToStop", new Object[] { getServiceDisplayName() });
/*     */     }
/*     */   }
/*     */ 
/*     */   public void serviceStart(int numArgs, int pArgs)
/*     */     throws ServiceException, DataException
/*     */   {
/* 349 */     String pidString = SystemUtils.getProcessIdString();
/* 350 */     IdcMessage firstCompMsg = IdcMessageFactory.lc("csServiceStart2", new Object[] { getServiceDisplayName(), VersionInfo.getProductVersion(), pidString });
/*     */ 
/* 352 */     IdcMessage lastCompMsg = firstCompMsg;
/* 353 */     List messages = (List)SharedObjects.getObject("globalObjects", "LoadedComponentMessages");
/*     */ 
/* 355 */     if (messages != null)
/*     */     {
/* 357 */       for (IdcMessage compMsg : messages)
/*     */       {
/* 359 */         compMsg = compMsg.makeClone();
/* 360 */         IdcMessage tmp = IdcMessageFactory.lc();
/* 361 */         tmp.m_msgSimple = "\n";
/* 362 */         tmp.m_prior = compMsg;
/* 363 */         lastCompMsg.m_prior = tmp;
/* 364 */         lastCompMsg = tmp.m_prior;
/*     */       }
/*     */     }
/* 367 */     Report.info("startup", null, firstCompMsg);
/* 368 */     startProviders();
/*     */ 
/* 370 */     IntervalData serverStartTime = new IntervalData();
/* 371 */     serverStartTime.init("Server Up Time");
/* 372 */     serverStartTime.start();
/* 373 */     m_serverStartTime = serverStartTime;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 378 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75084 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IdcManagerBase
 * JD-Core Version:    0.5.4
 */