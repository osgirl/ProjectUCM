/*     */ package intradoc.server.proxy;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.provider.OutgoingProvider;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.ProxiedMonikerWatcher;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class OutgoingProviderMonitor
/*     */ {
/*  35 */   protected static OutgoingProviderManager m_providerManager = null;
/*  36 */   protected static String m_lockObject = "lock";
/*     */ 
/*  38 */   protected static boolean m_isInitialized = false;
/*  39 */   protected static boolean m_isStarted = false;
/*  40 */   protected static boolean m_isOnDemand = false;
/*  41 */   protected static boolean m_disableConnectionPolling = false;
/*  42 */   protected static ExecutionContext m_monitorContext = null;
/*     */ 
/*     */   public static void init(boolean isOnDemand) throws ServiceException
/*     */   {
/*  46 */     m_providerManager = (OutgoingProviderManager)ComponentClassFactory.createClassInstance("OutgoingProviderManager", "intradoc.server.proxy.OutgoingProviderManager", "!csProviderManagerMissing");
/*     */ 
/*  49 */     m_monitorContext = new ExecutionContextAdaptor();
/*  50 */     m_providerManager.init(isOnDemand, m_monitorContext);
/*  51 */     m_isOnDemand = isOnDemand;
/*  52 */     m_isInitialized = true;
/*  53 */     m_disableConnectionPolling = SharedObjects.getEnvValueAsBoolean("DisableConnectionPolling", true);
/*     */ 
/*  55 */     initMonitor();
/*     */   }
/*     */ 
/*     */   public static void startMonitor()
/*     */   {
/*  60 */     synchronized (m_lockObject)
/*     */     {
/*  62 */       m_isStarted = true;
/*  63 */       m_lockObject.notify();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void stopMonitor()
/*     */   {
/*  69 */     synchronized (m_lockObject)
/*     */     {
/*  71 */       m_isStarted = false;
/*  72 */       m_lockObject.notify();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void initMonitor()
/*     */   {
/*  81 */     Runnable run = new Object()
/*     */     {
/*     */       public void run()
/*     */       {
/*  85 */         SystemUtils.registerSynchronizationObjectToNotifyOnStop(OutgoingProviderMonitor.m_lockObject);
/*  86 */         while (!SystemUtils.m_isServerStopped)
/*     */         {
/*     */           try
/*     */           {
/*  90 */             synchronized (OutgoingProviderMonitor.m_lockObject)
/*     */             {
/*  92 */               while ((!OutgoingProviderMonitor.m_isStarted) && (!SystemUtils.m_isServerStopped))
/*     */               {
/*  94 */                 OutgoingProviderMonitor.m_lockObject.wait();
/*     */               }
/*     */             }
/*  97 */             if (SystemUtils.m_isServerStopped) {
/*     */               return;
/*     */             }
/*     */ 
/* 101 */             if (OutgoingProviderMonitor.m_isStarted)
/*     */             {
/* 103 */               OutgoingProviderMonitor.monitorOutgoingConnections(OutgoingProviderMonitor.m_monitorContext);
/*     */             }
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/* 108 */             Report.trace(null, null, t);
/*     */           }
/*     */ 
/*     */           try
/*     */           {
/* 113 */             synchronized (OutgoingProviderMonitor.m_lockObject)
/*     */             {
/* 115 */               SystemUtils.wait(OutgoingProviderMonitor.m_lockObject, 30000L);
/*     */             }
/*     */           }
/*     */           catch (Throwable ignore)
/*     */           {
/* 120 */             Report.trace(null, null, ignore);
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 126 */     Thread bgThread = new Thread(run, "OutgoingProviderMonitor");
/* 127 */     bgThread.setDaemon(true);
/* 128 */     bgThread.start();
/*     */   }
/*     */ 
/*     */   public static void monitorOutgoingConnections(ExecutionContext ctxt)
/*     */   {
/* 133 */     Hashtable providers = m_providerManager.getOutgoingProviders();
/* 134 */     for (Enumeration en = providers.elements(); en.hasMoreElements(); )
/*     */     {
/* 136 */       Provider provider = (Provider)en.nextElement();
/* 137 */       monitorOutgoingProvider(provider, ctxt);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void monitorOutgoingProvider(Provider provider, ExecutionContext ctxt)
/*     */   {
/* 145 */     Properties provState = provider.getProviderState();
/* 146 */     if (!checkProviderForRetry(provider, provState))
/*     */     {
/* 148 */       return;
/*     */     }
/*     */ 
/* 151 */     OutgoingProvider op = (OutgoingProvider)provider.getProvider();
/* 152 */     if (!op.isStarted())
/*     */     {
/* 154 */       return;
/*     */     }
/*     */ 
/* 157 */     DataBinder provData = provider.getProviderData();
/* 158 */     boolean isNotifyTarget = StringUtils.convertToBool(provData.getLocal("IsNotifyTarget"), false);
/* 159 */     boolean isProxiedServer = StringUtils.convertToBool(provData.getLocal("IsProxiedServer"), false);
/*     */ 
/* 162 */     String idcName = provData.getLocal("IDC_Name");
/* 163 */     boolean isSyncMonikers = ProxiedMonikerWatcher.hasRequests(idcName);
/*     */ 
/* 165 */     if ((isSyncMonikers) || (isNotifyTarget))
/*     */     {
/* 168 */       notifySubjects(provider, provState, isSyncMonikers, ctxt);
/*     */     }
/*     */ 
/* 171 */     if ((m_isOnDemand) || (!isProxiedServer)) {
/*     */       return;
/*     */     }
/* 174 */     refreshNotifiedSubjects(provider, provState, ctxt);
/*     */   }
/*     */ 
/*     */   protected static void notifySubjects(Provider provider, Properties provState, boolean isSyncCounters, ExecutionContext ctxt)
/*     */   {
/* 184 */     DataBinder provData = provider.getProviderData();
/* 185 */     DataBinder inBinder = new DataBinder(provData.getLocalData());
/* 186 */     Vector toNotifySubjects = new IdcVector();
/*     */ 
/* 188 */     if (!m_isOnDemand)
/*     */     {
/* 190 */       String str = provData.getLocal("NotifySubjects");
/* 191 */       Vector subjects = StringUtils.parseArray(str, ',', ',');
/* 192 */       int num = subjects.size();
/* 193 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 195 */         String subject = (String)subjects.elementAt(i);
/* 196 */         boolean isNotify = StringUtils.convertToBool(provState.getProperty(subject + ":isNotify"), false);
/* 197 */         if (!isNotify)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 202 */         provState.remove(subject + ":isNotify");
/* 203 */         inBinder.putLocal(subject + ":isNotify", "1");
/* 204 */         inBinder.putLocal(subject + ":counter", String.valueOf(SubjectManager.getMarker(subject)));
/*     */ 
/* 206 */         toNotifySubjects.addElement(subject);
/*     */       }
/*     */ 
/* 210 */       if ((!isSyncCounters) && (toNotifySubjects.size() == 0))
/*     */       {
/* 213 */         ProviderStateUtils.testConnection(provider);
/* 214 */         return;
/*     */       }
/*     */     }
/* 217 */     else if (!isSyncCounters)
/*     */     {
/* 220 */       return;
/*     */     }
/*     */ 
/* 224 */     String idcName = provData.getLocal("IDC_Name");
/* 225 */     if (isSyncCounters)
/*     */     {
/* 227 */       ProxiedMonikerWatcher.buildRequests(idcName, inBinder);
/*     */     }
/*     */ 
/* 230 */     if (m_isOnDemand)
/*     */     {
/* 232 */       inBinder.putLocal("IdcService", "PING_SERVER");
/*     */     }
/*     */     else
/*     */     {
/* 236 */       inBinder.putLocal("IdcService", "NOTIFY_CHANGE");
/*     */     }
/* 238 */     inBinder.putLocal("NotifiedSubjects", StringUtils.createString(toNotifySubjects, ',', ','));
/* 239 */     inBinder.putLocal("IDC_Name", SharedObjects.getEnvironmentValue("IDC_Name"));
/* 240 */     inBinder.putLocal("ActAsAnonymous", "1");
/*     */ 
/* 242 */     boolean isSuccess = true;
/* 243 */     DataBinder outBinder = null;
/*     */     try
/*     */     {
/* 246 */       outBinder = OutgoingProviderManager.doSecureRequest(provider, inBinder, ctxt);
/* 247 */       ProviderStateUtils.checkReturnData(provider, outBinder);
/*     */ 
/* 249 */       if (isSyncCounters)
/*     */       {
/* 252 */         ProxiedMonikerWatcher.updateWatched(idcName, outBinder);
/*     */       }
/*     */     }
/*     */     catch (Exception currentTs)
/*     */     {
/*     */       String currentTs;
/*     */       int numSubjects;
/*     */       int i;
/*     */       String subject;
/* 257 */       ProviderStateUtils.handleRequestError(provider, e);
/* 258 */       isSuccess = false;
/*     */     }
/*     */     finally
/*     */     {
/*     */       String currentTs;
/*     */       int numSubjects;
/*     */       int i;
/*     */       String subject;
/* 262 */       String currentTs = String.valueOf(System.currentTimeMillis());
/* 263 */       int numSubjects = toNotifySubjects.size();
/* 264 */       for (int i = 0; i < numSubjects; ++i)
/*     */       {
/* 266 */         String subject = (String)toNotifySubjects.elementAt(i);
/* 267 */         if (isSuccess)
/*     */         {
/* 269 */           provState.put(subject + ":lastNotifySuccessTs", currentTs);
/*     */         }
/*     */         else
/*     */         {
/* 274 */           provState.put(subject + ":isNotify", "1");
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void refreshNotifiedSubjects(Provider provider, Properties provState, ExecutionContext ctxt)
/*     */   {
/* 283 */     String str = provState.getProperty("NotifiedSubjects");
/* 284 */     Vector subjects = StringUtils.parseArray(str, ',', ',');
/*     */ 
/* 286 */     boolean hasAttempted = false;
/* 287 */     int num = subjects.size();
/* 288 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 290 */       String subject = (String)subjects.elementAt(i);
/*     */ 
/* 294 */       boolean isNotified = StringUtils.convertToBool(provState.getProperty(subject + ":isNotified"), false);
/* 295 */       if (!isNotified)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 300 */       str = provState.getProperty(subject + ":counter");
/* 301 */       long counter = Long.parseLong(str);
/*     */ 
/* 303 */       str = provState.getProperty(subject + ":refreshCounter");
/* 304 */       long refreshCounter = 0L;
/* 305 */       if (str != null)
/*     */       {
/* 307 */         refreshCounter = Long.parseLong(str);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 312 */         if (refreshCounter != counter)
/*     */         {
/* 314 */           if (refreshSubject(provider, subject, ctxt))
/*     */           {
/* 316 */             hasAttempted = true;
/*     */           }
/*     */ 
/*     */         }
/*     */         else {
/* 321 */           provState.remove(subject + ":isNotified");
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 326 */         ProviderStateUtils.handleRequestError(provider, e);
/* 327 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 331 */     if ((num != 0) && (hasAttempted)) {
/*     */       return;
/*     */     }
/* 334 */     if (m_disableConnectionPolling)
/*     */     {
/* 337 */       ProviderStateUtils.updateProviderConnectionState(provider, true);
/*     */     }
/*     */     else
/*     */     {
/* 341 */       ProviderStateUtils.testConnection(provider);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void requestNotification(Provider provider, String subject)
/*     */   {
/* 351 */     synchronized (m_lockObject)
/*     */     {
/* 353 */       Properties provState = provider.getProviderState();
/*     */ 
/* 355 */       String currentTs = String.valueOf(System.currentTimeMillis());
/*     */ 
/* 357 */       provState.put(subject + ":isNotify", "1");
/* 358 */       provState.put(subject + ":lastNotifyTs", currentTs);
/* 359 */       provState.put("LastRequestTs", currentTs);
/*     */ 
/* 361 */       m_lockObject.notify();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void requestRefresh(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 368 */     if (!m_isInitialized)
/*     */     {
/* 370 */       return;
/*     */     }
/*     */ 
/* 373 */     String idcName = binder.getLocal("IDC_Name");
/* 374 */     if (idcName == null)
/*     */     {
/* 376 */       throw new DataException("!csNameNotSpecified");
/*     */     }
/*     */ 
/* 379 */     Provider provider = m_providerManager.getOutgoingProvider(idcName);
/* 380 */     if (provider == null)
/*     */     {
/* 382 */       String msg = LocaleUtils.encodeMessage("csProviderRefreshServerUnknown", null, idcName);
/*     */ 
/* 384 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 387 */     String str = binder.getLocal("NotifiedSubjects");
/* 388 */     Vector notifiedSubjects = StringUtils.parseArray(str, ',', ',');
/* 389 */     if (str == null)
/*     */     {
/* 391 */       throw new DataException("!csProviderUnableToRefreshSubjects");
/*     */     }
/*     */ 
/* 394 */     synchronized (m_lockObject)
/*     */     {
/* 396 */       Properties provState = provider.getProviderState();
/*     */ 
/* 399 */       str = provState.getProperty("NotifiedSubjects");
/* 400 */       Vector subjects = StringUtils.parseArray(str, ',', ',');
/*     */ 
/* 402 */       int num = notifiedSubjects.size();
/* 403 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 405 */         String subject = (String)notifiedSubjects.elementAt(i);
/*     */ 
/* 408 */         boolean isFound = false;
/* 409 */         int numSubjects = subjects.size();
/* 410 */         for (int j = 0; j < numSubjects; ++j)
/*     */         {
/* 412 */           str = (String)subjects.elementAt(j);
/* 413 */           if (!str.equals(subject))
/*     */             continue;
/* 415 */           isFound = true;
/* 416 */           break;
/*     */         }
/*     */ 
/* 420 */         if (!isFound)
/*     */         {
/* 422 */           subjects.addElement(subject);
/* 423 */           provState.put("NotifiedSubjects", StringUtils.createString(subjects, ',', ','));
/*     */         }
/*     */ 
/* 427 */         provState.put(subject + ":counter", binder.getLocal(subject + ":counter"));
/* 428 */         provState.put(subject + ":isNotified", "1");
/* 429 */         provState.put(subject + ":lastNotifiedTs", String.valueOf(System.currentTimeMillis()));
/*     */       }
/*     */ 
/* 432 */       provState.put("LastRequestTs", String.valueOf(System.currentTimeMillis()));
/* 433 */       m_lockObject.notify();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void forceRefresh(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 440 */     if (!m_isInitialized)
/*     */     {
/* 442 */       return;
/*     */     }
/*     */ 
/* 445 */     String idcName = binder.getLocal("IDC_Name");
/* 446 */     if (idcName == null)
/*     */     {
/* 448 */       throw new DataException("!csNameNotSpecified");
/*     */     }
/*     */ 
/* 451 */     Provider provider = m_providerManager.getOutgoingProvider(idcName);
/* 452 */     if (provider == null)
/*     */     {
/* 454 */       String msg = LocaleUtils.encodeMessage("csProviderUnableToTestUnknownServer", null, idcName);
/*     */ 
/* 456 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 459 */     monitorOutgoingProvider(provider, ctxt);
/*     */   }
/*     */ 
/*     */   protected static boolean refreshSubject(Provider provider, String subject, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 468 */     if (subject.equals("users"))
/*     */     {
/* 470 */       refreshUsers(provider, ctxt);
/*     */     }
/* 472 */     else if (subject.equals("releaseddocuments"))
/*     */     {
/* 474 */       refreshDocuments(provider, ctxt);
/*     */     }
/*     */     else
/*     */     {
/* 478 */       String msg = LocaleUtils.encodeMessage("csProviderCannotRefreshSubject", null, subject);
/*     */ 
/* 480 */       Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/* 481 */       return false;
/*     */     }
/* 483 */     return true;
/*     */   }
/*     */ 
/*     */   public static void refreshUsers(Provider provider, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 489 */     Properties provState = provider.getProviderState();
/* 490 */     DataBinder provData = provider.getProviderData();
/* 491 */     String location = LegacyDirectoryLocator.getUserPublishCacheDir() + "proxied" + provData.getLocal("HttpRelativeWebRoot");
/*     */ 
/* 494 */     DataBinder binder = m_providerManager.requestAndSaveSecurityInfo(provider, provData, location, ctxt);
/*     */ 
/* 497 */     updateSubjectState("users", provState, binder);
/* 498 */     SearchLoader.processUsersChange(provider, ctxt);
/*     */   }
/*     */ 
/*     */   public static void refreshDocuments(Provider provider, ExecutionContext ctxt)
/*     */   {
/* 503 */     SearchLoader.processExternalReleasedDocumentsChange(provider, ctxt);
/*     */ 
/* 506 */     Properties provState = provider.getProviderState();
/* 507 */     DataBinder binder = new DataBinder();
/* 508 */     binder.setLocalData(provState);
/*     */ 
/* 510 */     updateSubjectState("releaseddocuments", provState, binder);
/*     */   }
/*     */ 
/*     */   protected static void updateSubjectState(String subject, Properties provState, DataBinder binder)
/*     */   {
/* 515 */     String counter = binder.getLocal(subject + ":counter");
/* 516 */     provState.put(subject + ":counter", counter);
/* 517 */     provState.put(subject + ":refreshCounter", counter);
/* 518 */     provState.put(subject + ":lastRefreshTs", String.valueOf(System.currentTimeMillis()));
/*     */ 
/* 520 */     provState.remove(subject + ":isNotified");
/*     */   }
/*     */ 
/*     */   protected static boolean checkProviderForRetry(Provider provider, Properties provState)
/*     */   {
/* 526 */     if (!provider.isEnabled())
/*     */     {
/* 528 */       return false;
/*     */     }
/*     */ 
/* 532 */     boolean isRetry = true;
/* 533 */     boolean isBad = StringUtils.convertToBool(provState.getProperty("IsBadConnection"), false);
/* 534 */     if (!isBad)
/*     */     {
/* 536 */       return isRetry;
/*     */     }
/*     */ 
/* 541 */     long lastActivityTs = getPropertyAsLong(provState, "LastActivityTs", 0L);
/* 542 */     long notificationTs = getPropertyAsLong(provState, "LastRequestTs", 0L);
/* 543 */     if (lastActivityTs < notificationTs)
/*     */     {
/* 545 */       return isRetry;
/*     */     }
/*     */ 
/* 548 */     DataBinder provData = provider.getProviderData();
/* 549 */     long retryTimeout = DataBinderUtils.getInteger(provData, "RetryTimeout", 30) * 1000;
/* 550 */     int retryCount = getPropertyAsInteger(provState, "RetryCount", 1);
/* 551 */     long currentTs = System.currentTimeMillis();
/*     */ 
/* 553 */     int errCode = getPropertyAsInteger(provState, "LastConnectionErrorCode", -1);
/* 554 */     switch (errCode)
/*     */     {
/*     */     case -5:
/*     */     case -4:
/*     */     case -3:
/* 559 */       int retryFactor = (retryCount < 5) ? 1 : 5;
/* 560 */       if (currentTs - lastActivityTs > retryTimeout * retryFactor)
/*     */         break label163;
/* 562 */       isRetry = false; break;
/*     */     default:
/* 568 */       isRetry = false;
/*     */     }
/*     */ 
/* 572 */     label163: return isRetry;
/*     */   }
/*     */ 
/*     */   public static long getPropertyAsLong(Map props, String key, long dflt)
/*     */   {
/* 577 */     Object o = props.get(key);
/* 578 */     if (o != null)
/*     */     {
/* 580 */       return NumberUtils.parseLong(o.toString(), dflt);
/*     */     }
/* 582 */     return dflt;
/*     */   }
/*     */ 
/*     */   public static int getPropertyAsInteger(Map props, String key, int dflt)
/*     */   {
/* 587 */     Object o = props.get(key);
/* 588 */     if (o != null)
/*     */     {
/* 590 */       return NumberUtils.parseInteger(o.toString(), dflt);
/*     */     }
/* 592 */     return dflt;
/*     */   }
/*     */ 
/*     */   public static Hashtable get0utgoingProviders()
/*     */   {
/* 600 */     return m_providerManager.getOutgoingProviders();
/*     */   }
/*     */ 
/*     */   public static Provider getOutgoingProvider(String idcName)
/*     */   {
/* 605 */     if (m_providerManager == null)
/*     */     {
/* 607 */       if (SystemUtils.m_verbose)
/*     */       {
/* 609 */         Exception e = new Exception("Provider manager not initialized, looking for provider " + idcName);
/* 610 */         Report.trace("system", null, e);
/*     */       }
/* 612 */       return null;
/*     */     }
/* 614 */     return m_providerManager.getOutgoingProvider(idcName);
/*     */   }
/*     */ 
/*     */   public static boolean isStarted()
/*     */   {
/* 619 */     return m_isStarted;
/*     */   }
/*     */ 
/*     */   public static OutgoingProviderManager getOutgoingProviderManager()
/*     */   {
/* 624 */     return m_providerManager;
/*     */   }
/*     */ 
/*     */   public static boolean isOnDemand()
/*     */   {
/* 634 */     return m_isOnDemand;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 639 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 74173 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.proxy.OutgoingProviderMonitor
 * JD-Core Version:    0.5.4
 */