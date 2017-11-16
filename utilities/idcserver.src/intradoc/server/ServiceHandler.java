/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ 
/*     */ public class ServiceHandler
/*     */ {
/*     */   protected String m_className;
/*     */   protected int m_searchOrder;
/*     */   protected Service m_service;
/*     */   protected Workspace m_workspace;
/*     */   protected DataBinder m_binder;
/*     */   protected Action m_currentAction;
/*     */   protected boolean m_useSecurity;
/*     */ 
/*     */   public ServiceHandler()
/*     */   {
/*  34 */     this.m_searchOrder = 1;
/*     */ 
/*  42 */     this.m_useSecurity = true;
/*     */   }
/*     */ 
/*     */   public void init(Service service) throws ServiceException, DataException {
/*  46 */     this.m_service = service;
/*  47 */     this.m_workspace = service.getWorkspace();
/*  48 */     this.m_binder = service.getBinder();
/*  49 */     this.m_useSecurity = service.getUseSecurity();
/*     */   }
/*     */ 
/*     */   public void setInfo(String name, int searchOrder)
/*     */   {
/*  54 */     this.m_className = name;
/*  55 */     this.m_searchOrder = searchOrder;
/*     */   }
/*     */ 
/*     */   public String getClassName()
/*     */   {
/*  60 */     return this.m_className;
/*     */   }
/*     */ 
/*     */   public int getSearchOrder()
/*     */   {
/*  65 */     return this.m_searchOrder;
/*     */   }
/*     */ 
/*     */   public void setCurrentAction(Action action)
/*     */   {
/*  70 */     this.m_currentAction = action;
/*     */   }
/*     */ 
/*     */   public boolean executeAction(String actFunction) throws ServiceException, DataException
/*     */   {
/*  75 */     this.m_currentAction = this.m_service.getCurrentAction();
/*  76 */     boolean returnValue = false;
/*     */     try
/*     */     {
/*  79 */       returnValue = ClassHelperUtils.executeMethodReportStatus(this, actFunction);
/*     */     }
/*     */     catch (IllegalAccessException e)
/*     */     {
/*  83 */       String msg = LocaleUtils.encodeMessage("csMethodIllegalAccess2", null, actFunction);
/*     */ 
/*  85 */       this.m_service.createServiceException(e, msg);
/*     */     }
/*     */     catch (InvocationTargetException e)
/*     */     {
/*  89 */       Throwable tException = e.getTargetException();
/*  90 */       if (tException instanceof DataException)
/*     */       {
/*  92 */         throw ((DataException)tException);
/*     */       }
/*  94 */       if (tException instanceof ServiceException)
/*     */       {
/*  96 */         throw ((ServiceException)tException);
/*     */       }
/*     */ 
/* 100 */       String msg = LocaleUtils.encodeMessage("csUnableToExecMethod", null, actFunction);
/*     */ 
/* 102 */       this.m_service.createServiceException(tException, msg);
/*     */     }
/*     */ 
/* 105 */     return returnValue;
/*     */   }
/*     */ 
/*     */   public void cleanup(boolean isError)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void callPluginFilter()
/*     */     throws DataException, ServiceException
/*     */   {
/* 117 */     String filterName = this.m_currentAction.getParamAt(0);
/* 118 */     if (PluginFilters.filter(filterName, this.m_workspace, this.m_binder, this.m_service) != -1) {
/*     */       return;
/*     */     }
/* 121 */     String errorMsg = (String)this.m_service.getCachedObject("errorMessage");
/* 122 */     if (errorMsg == null)
/*     */       return;
/* 124 */     this.m_service.createServiceException(null, errorMsg);
/*     */   }
/*     */ 
/*     */   public void doRemoteTrace()
/*     */     throws ServiceException, DataException
/*     */   {
/* 142 */     String section = this.m_binder.getLocal("traceSection");
/* 143 */     String message = this.m_binder.getLocal("traceMessage");
/* 144 */     String ifVerbose = this.m_binder.getLocal("traceIfVerbose");
/* 145 */     boolean onlyIfVerbose = StringUtils.convertToBool(ifVerbose, false);
/*     */ 
/* 147 */     if (onlyIfVerbose)
/*     */     {
/* 149 */       if (Report.m_verbose) {
/* 150 */         Report.trace(section, message, null);
/*     */       }
/*     */     }
/*     */     else
/* 154 */       Report.trace(section, message, null);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 160 */     return "SERVICEHANDLER: " + super.getClass().getName() + " on service \n" + this.m_service;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 166 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101120 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ServiceHandler
 * JD-Core Version:    0.5.4
 */