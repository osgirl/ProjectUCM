/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.provider.IncomingConnection;
/*     */ import intradoc.provider.IncomingThread;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.server.audit.ServerRequestsAudit;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class IdcServerThread extends IncomingThread
/*     */ {
/*  36 */   protected ExecutionContext m_auditTraceContext = null;
/*  37 */   protected IncomingConnection m_connection = null;
/*     */   protected Provider m_provider;
/*     */   protected ServiceManager m_sman;
/*     */   protected boolean m_isStandAlone;
/*     */   protected DataBinder m_preExistingBinder;
/*     */   protected DataBinder m_capturedResponseBinder;
/*     */   protected UserData m_capturedUserData;
/*     */   protected String m_tracingSection;
/*     */ 
/*     */   public IdcServerThread()
/*     */   {
/*     */   }
/*     */ 
/*     */   public IdcServerThread(String threadName, ExecutionContext auditContext)
/*     */   {
/*  62 */     this.m_auditTraceContext = auditContext;
/*  63 */     setName(threadName);
/*     */   }
/*     */ 
/*     */   public void init(Provider prov, IncomingConnection con)
/*     */   {
/*  72 */     this.m_provider = prov;
/*  73 */     this.m_connection = con;
/*     */   }
/*     */ 
/*     */   public void setUpfrontBinder(DataBinder binder, boolean isStandAlone, String tracingSection)
/*     */   {
/*  84 */     this.m_preExistingBinder = binder;
/*  85 */     this.m_isStandAlone = isStandAlone;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static int getAlteredCount()
/*     */   {
/*  99 */     return SystemUtils.m_numThreads;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static long getTotalCount()
/*     */   {
/* 110 */     return SystemUtils.m_totalThreads;
/*     */   }
/*     */ 
/*     */   public IncomingConnection getConnection()
/*     */   {
/* 118 */     return this.m_connection;
/*     */   }
/*     */ 
/*     */   public ServiceManager getServiceManager()
/*     */   {
/* 128 */     return this.m_sman;
/*     */   }
/*     */ 
/*     */   public DataBinder getResponseDataBinder()
/*     */   {
/* 138 */     return this.m_capturedResponseBinder;
/*     */   }
/*     */ 
/*     */   public UserData getCapturedUserData()
/*     */   {
/* 147 */     return this.m_capturedUserData;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     try
/*     */     {
/* 158 */       prepareThread();
/*     */ 
/* 160 */       processRequest();
/*     */     }
/*     */     finally
/*     */     {
/* 164 */       releaseThread();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void prepareThread()
/*     */   {
/* 170 */     if (Thread.currentThread() == this)
/*     */     {
/* 172 */       int nextThreadCount = SystemUtils.getNextThreadCount();
/* 173 */       String threadName = "IdcServerThread-" + nextThreadCount;
/* 174 */       SystemUtils.alterCount(1);
/* 175 */       Report.trace("threads", "Initializing request thread with with thread name " + threadName + " and " + SystemUtils.getThreadCount() + " active request threads", null);
/*     */ 
/* 177 */       setName(threadName);
/*     */     }
/*     */     else
/*     */     {
/* 182 */       SystemUtils.assignReportingThreadIdToCurrentThread(0);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void releaseThread()
/*     */   {
/* 188 */     if (Thread.currentThread() == this)
/*     */     {
/* 190 */       SystemUtils.alterCount(-1);
/* 191 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 193 */       Report.debug("threads", "Ending request thread leaving behind " + SystemUtils.getThreadCount() + " active request threads", null);
/*     */     }
/*     */     else
/*     */     {
/* 199 */       SystemUtils.releaseReportingThreadIdForCurrentThread();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void processRequest()
/*     */   {
/* 205 */     IdcMessage errMsg = null;
/* 206 */     OutputStream outStream = null;
/* 207 */     boolean isAuditTrace = SystemUtils.isActiveTrace("requestaudit");
/* 208 */     if (this.m_auditTraceContext == null)
/*     */     {
/* 210 */       this.m_auditTraceContext = new ExecutionContextAdaptor();
/*     */     }
/* 212 */     DataBinder binder = null;
/* 213 */     int curThreadCount = SystemUtils.getThreadCount();
/* 214 */     String service = null;
/* 215 */     Throwable throwableThrown = null;
/*     */     try
/*     */     {
/* 218 */       this.m_sman = new ServiceManager();
/* 219 */       if (isAuditTrace)
/*     */       {
/* 221 */         this.m_auditTraceContext.setCachedObject("RequestAuditIncomingThread", this);
/* 222 */         this.m_auditTraceContext.setCachedObject("RequestAuditServiceManager", this.m_sman);
/* 223 */         ServerRequestsAudit.reportStartRequest(this.m_auditTraceContext);
/*     */       }
/* 225 */       this.m_sman.setExecutionContext(this.m_auditTraceContext);
/* 226 */       this.m_sman.setControlFlags(false, this.m_isStandAlone);
/*     */       try
/*     */       {
/* 230 */         if (this.m_preExistingBinder != null)
/*     */         {
/* 232 */           this.m_sman.init(this.m_preExistingBinder, this.m_connection, this.m_tracingSection);
/*     */         }
/*     */         else
/*     */         {
/* 236 */           this.m_sman.init(this.m_connection);
/*     */         }
/* 238 */         binder = this.m_sman.getDataBinder();
/* 239 */         this.m_connection.prepareUse(binder);
/*     */ 
/* 242 */         binder.setEnvironmentValue("ThreadCount", "" + SystemUtils.getThreadCount());
/*     */ 
/* 246 */         service = binder.getLocal("IdcService");
/*     */ 
/* 248 */         if (isAuditTrace)
/*     */         {
/* 250 */           ServerRequestsAudit.reportParsedRequest(service, binder, this.m_auditTraceContext);
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/*     */       }
/*     */       finally
/*     */       {
/*     */         String msg;
/* 261 */         outStream = this.m_connection.getOutputStream();
/* 262 */         this.m_sman.setOutputStream(outStream);
/*     */       }
/*     */ 
/* 265 */       this.m_sman.processCommand();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 269 */       throwableThrown = e;
/* 270 */       errMsg = IdcMessageFactory.lc("csNetworkMessageFormatError", new Object[0]);
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/* 274 */       throwableThrown = e;
/* 275 */       if ((this.m_sman.m_isStandAlone) && (this.m_sman.m_service != null) && (e instanceof ServiceException))
/*     */       {
/* 280 */         ServiceException sExpt = (ServiceException)e;
/* 281 */         DataBinder serviceBinder = this.m_sman.m_service.getBinder();
/* 282 */         this.m_sman.m_service.buildUpErrorResponse(serviceBinder, sExpt);
/*     */       }
/*     */       else
/*     */       {
/* 286 */         Report.trace(null, null, e);
/* 287 */         errMsg = IdcMessageFactory.lc("csSystemCodeExecutionError", new Object[0]);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 292 */       if (this.m_sman.m_service != null)
/*     */       {
/* 294 */         this.m_capturedResponseBinder = this.m_sman.m_service.getBinder();
/* 295 */         this.m_capturedUserData = this.m_sman.m_service.getUserData();
/*     */       }
/* 297 */       this.m_sman.cleanup();
/* 298 */       Providers.releaseConnections();
/*     */     }
/*     */ 
/* 301 */     if (errMsg != null)
/*     */     {
/* 303 */       this.m_sman.onError(throwableThrown, errMsg);
/*     */     }
/*     */ 
/* 307 */     if (this.m_connection != null)
/*     */     {
/* 309 */       this.m_connection.close();
/*     */     }
/* 311 */     if (isAuditTrace)
/*     */     {
/*     */       try
/*     */       {
/* 315 */         ServerRequestsAudit.reportEndRequest(service, binder, curThreadCount, this.m_auditTraceContext);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 320 */         Report.trace(null, null, e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 325 */     this.m_sman.clear();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 330 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82080 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IdcServerThread
 * JD-Core Version:    0.5.4
 */