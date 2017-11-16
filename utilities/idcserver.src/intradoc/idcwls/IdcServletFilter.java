/*     */ package intradoc.idcwls;
/*     */ 
/*     */ import intradoc.common.ClassHelper;
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHttpImplementor;
/*     */ import intradoc.shared.FilterImplementor;
/*     */ import intradoc.util.IdcLoggerUtils;
/*     */ 
/*     */ public class IdcServletFilter
/*     */   implements FilterImplementor
/*     */ {
/*     */   DataBinder m_binder;
/*     */ 
/*     */   public IdcServletFilter()
/*     */   {
/*  37 */     this.m_binder = null;
/*     */   }
/*     */ 
/*     */   public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt) throws DataException, ServiceException
/*     */   {
/*  42 */     this.m_binder = binder;
/*     */ 
/*  44 */     Object param = cxt.getCachedObject("filterParameter");
/*  45 */     if ((param == null) || (!param instanceof String))
/*     */     {
/*  47 */       return 0;
/*     */     }
/*     */ 
/*  50 */     Service service = null;
/*  51 */     ServiceHttpImplementor httpImplementor = null;
/*  52 */     if (cxt instanceof Service)
/*     */     {
/*  54 */       service = (Service)cxt;
/*  55 */       Object o = service.getCachedObject("HttpImplementor");
/*  56 */       if (o instanceof ServiceHttpImplementor)
/*     */       {
/*  58 */         httpImplementor = (ServiceHttpImplementor)o;
/*     */       }
/*     */     }
/*     */ 
/*  62 */     if (param.equals("logoutServer"))
/*     */     {
/*  64 */       return logoutServer(service, httpImplementor);
/*     */     }
/*  66 */     if ((param.equals("loadComponentDataPostFilters")) && 
/*  68 */       (EnvUtils.isHostedInAppServer()))
/*     */     {
/*  71 */       IdcLoggerUtils.clearStringCache();
/*     */     }
/*     */ 
/*  74 */     if (param.equals("preDoResponse"))
/*     */     {
/*  76 */       fixupWebRoot(service);
/*     */     }
/*  78 */     if ((param.equals("afterHttpImplementorInit")) && 
/*  80 */       (EnvUtils.isHostedInAppServer()))
/*     */     {
/*  82 */       afterHttpImplementorInit(service);
/*     */     }
/*     */ 
/*  85 */     return 0;
/*     */   }
/*     */ 
/*     */   public int logoutServer(Service service, ServiceHttpImplementor httpImplementor)
/*     */     throws DataException, ServiceException
/*     */   {
/*  91 */     IdcServletRequestContext cxt = (IdcServletRequestContext)service.getCachedObject("IdcServletRequestContext");
/*     */ 
/*  94 */     if (cxt == null)
/*     */     {
/*  96 */       return 0;
/*     */     }
/*     */ 
/*  99 */     IdcServletAuthUtils.logout(this.m_binder, cxt);
/*     */ 
/* 105 */     httpImplementor.m_loginState = "0";
/*     */ 
/* 107 */     return 0;
/*     */   }
/*     */ 
/*     */   public int fixupWebRoot(Service service) throws DataException, ServiceException
/*     */   {
/* 112 */     IdcServletRequestContext request = (IdcServletRequestContext)service.getCachedObject("IdcServletRequestContext");
/*     */ 
/* 115 */     if (request == null)
/*     */     {
/* 117 */       return 0;
/*     */     }
/* 119 */     boolean doRedirect = IdcServletRequestUtils.searchForBooleanValue(request, "servletdoredirect", false);
/* 120 */     if (doRedirect)
/*     */     {
/* 122 */       String redirectUrl = IdcServletRequestUtils.searchForValue(request, "servletredirecturl");
/* 123 */       if (redirectUrl == null)
/*     */       {
/* 125 */         redirectUrl = IdcServletStaticEnv.m_relativeWebRoot;
/*     */       }
/*     */ 
/* 128 */       this.m_binder.putLocal("RedirectUrl", redirectUrl);
/*     */     }
/*     */ 
/* 131 */     return 0;
/*     */   }
/*     */ 
/*     */   public int afterHttpImplementorInit(Service service)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 139 */       ServiceHttpImplementor httpImpl = (ServiceHttpImplementor)service.getCachedObject("HttpImplementor");
/*     */ 
/* 142 */       ClassHelper contextHelper = ClassHelperUtils.createClassHelperRef("oracle.dms.context.ExecutionContext");
/*     */ 
/* 145 */       String ECIDContextStr = (String)contextHelper.getFieldValue("KEY");
/* 146 */       String ECIDContext = httpImpl.m_binder.getAllowMissing(ECIDContextStr);
/*     */ 
/* 148 */       if ((ECIDContext != null) && (ECIDContext.length() > 0))
/*     */       {
/* 150 */         contextHelper.setObject(contextHelper.invoke("get"));
/* 151 */         contextHelper.invoke("unwrap", ECIDContext);
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*     */     }
/*     */ 
/* 158 */     return 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 163 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89430 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.IdcServletFilter
 * JD-Core Version:    0.5.4
 */