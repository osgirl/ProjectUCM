/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ClassHelper;
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.provider.OutgoingProvider;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.ServerRequest;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class EnterpriseSearchThread extends Thread
/*     */ {
/*     */   protected Properties m_requestProps;
/*     */   protected Provider m_searchProvider;
/*     */   protected Hashtable m_threads;
/*     */   protected DataBinder m_request;
/*     */   protected DataBinder m_results;
/*     */   protected ExecutionContext m_context;
/*     */ 
/*     */   public EnterpriseSearchThread(Properties props, Provider searchProvider, DataBinder request, DataBinder results, Hashtable runningThreads, ExecutionContext context)
/*     */   {
/*  43 */     this.m_requestProps = props;
/*  44 */     this.m_searchProvider = searchProvider;
/*  45 */     this.m_threads = runningThreads;
/*  46 */     this.m_request = request;
/*  47 */     this.m_results = results;
/*  48 */     this.m_context = context;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*  54 */     String name = this.m_searchProvider.getName();
/*     */     try
/*     */     {
/*  57 */       doRemoteSearch();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  61 */       Report.trace("searchquery", null, t);
/*  62 */       String msg = t.getMessage();
/*  63 */       if (msg == null)
/*     */       {
/*  65 */         msg = "!syNullPointerException";
/*     */       }
/*  67 */       this.m_results.putLocal("StatusMessageKey", msg);
/*  68 */       this.m_results.putLocal("StatusMessage", msg);
/*     */     }
/*     */     finally
/*     */     {
/*  72 */       synchronized (this.m_threads)
/*     */       {
/*  74 */         this.m_threads.remove(name);
/*  75 */         this.m_threads.notify();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void doRemoteSearch() throws DataException, ServiceException
/*     */   {
/*  82 */     Object p = this.m_searchProvider.getProvider();
/*  83 */     if (!p instanceof OutgoingProvider)
/*     */     {
/*  85 */       throw new ServiceException(-26, "!csCacheSearchProviderError");
/*     */     }
/*  87 */     ServerRequest serverRequest = null;
/*     */     try
/*     */     {
/*  90 */       DataBinder data = this.m_searchProvider.getProviderData();
/*  91 */       String relativeWebRoot = data.getLocal("HttpRelativeWebRoot");
/*  92 */       if ((relativeWebRoot != null) && (relativeWebRoot.length() > 0))
/*     */       {
/*  94 */         this.m_requestProps.put("HTTP_RELATIVEURL", relativeWebRoot);
/*     */       }
/*  96 */       OutgoingProvider provider = (OutgoingProvider)p;
/*     */ 
/*  98 */       serverRequest = provider.createRequest();
/*     */ 
/* 100 */       serverRequest.setRequestProperties(this.m_requestProps);
/* 101 */       serverRequest.doRequest(this.m_request, this.m_results, this.m_context);
/*     */     }
/*     */     finally
/*     */     {
/* 105 */       if (serverRequest != null)
/*     */       {
/* 107 */         closeRequest(serverRequest, this.m_context);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void closeRequest(ServerRequest serverRequest, ExecutionContext cxt)
/*     */   {
/*     */     try
/*     */     {
/* 116 */       Object[] args = { cxt };
/* 117 */       ClassHelperUtils.executeMethod(serverRequest, "closeRequest", args, new Class[] { ClassHelper.m_executionContextClass });
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 121 */       Report.trace("system", null, t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 127 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98038 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.EnterpriseSearchThread
 * JD-Core Version:    0.5.4
 */