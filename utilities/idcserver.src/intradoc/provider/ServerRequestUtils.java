/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ 
/*     */ public class ServerRequestUtils
/*     */ {
/*     */   public static void doAnonymousProxyRequest(Provider provider, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  34 */     binder.putLocal("ActAsAnonymous", "1");
/*  35 */     doAdminProxyRequest(provider, binder, binder, cxt);
/*     */   }
/*     */ 
/*     */   public static void doAdminProxyRequest(Provider provider, DataBinder inBinder, DataBinder outBinder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  41 */     doAdminProxyRequestEx(provider, inBinder, outBinder, cxt, null);
/*     */   }
/*     */ 
/*     */   public static void doAdminProxyRequestEx(Provider provider, DataBinder inBinder, DataBinder outBinder, ExecutionContext cxt, ReportProgress rp)
/*     */     throws DataException, ServiceException
/*     */   {
/*  58 */     ServerRequest sr = null;
/*  59 */     String isJava = null;
/*     */     try
/*     */     {
/*  62 */       isJava = inBinder.getLocal("IsJava");
/*  63 */       inBinder.putLocal("IsJava", "1");
/*  64 */       sr = createAdminProxyRequest(provider, inBinder, cxt, rp);
/*  65 */       sr.doRequest(inBinder, outBinder, cxt);
/*     */     }
/*     */     finally
/*     */     {
/*  69 */       if (isJava != null)
/*     */       {
/*  71 */         inBinder.putLocal("IsJava", isJava);
/*     */       }
/*     */       else
/*     */       {
/*  75 */         inBinder.removeLocal("IsJava");
/*     */       }
/*  77 */       if ((sr != null) && (outBinder != null) && (!outBinder.m_isSuspended))
/*     */       {
/*  79 */         sr.closeRequest(cxt);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static ServerRequest createAdminProxyRequest(Provider provider, DataBinder binder, ExecutionContext cxt, ReportProgress rp)
/*     */     throws DataException, ServiceException
/*     */   {
/*  87 */     DataBinder provData = provider.getProviderData();
/*  88 */     String hostName = provData.getLocal("IntradocServerHostName");
/*  89 */     if (hostName == null)
/*     */     {
/*  91 */       hostName = provData.getLocal("HttpServerAddress");
/*     */     }
/*  93 */     binder.setEnvironmentValue("HTTP_HOST", hostName);
/*  94 */     binder.setEnvironmentValue("PROXY_USER", "sysadmin");
/*  95 */     binder.setEnvironmentValue("HTTP_USER_AGENT", "JAVA");
/*     */ 
/*  97 */     OutgoingProvider op = (OutgoingProvider)provider.getProvider();
/*  98 */     ServerRequest sr = op.createRequest();
/*  99 */     if (rp != null)
/*     */     {
/* 101 */       sr.setReportProgress(rp);
/*     */     }
/* 103 */     return sr;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 108 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ServerRequestUtils
 * JD-Core Version:    0.5.4
 */