/*     */ package intradoc.idcwls;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class WorkManagerUtils
/*     */ {
/*     */   public static String getDefaultWorkManagerKey(IdcServletConfig servletConfig)
/*     */   {
/*  38 */     return getEnvConfigString(servletConfig, "WorkManagerInitialContextKey", "java:comp/env/wm/default");
/*     */   }
/*     */ 
/*     */   public static Object getWorkManager(String workManagerKey, IdcServletConfig servletConfig)
/*     */     throws DataException, ServiceException
/*     */   {
/*  55 */     String initialContextClassName = getEnvConfigString(servletConfig, "InitialContextClass", "javax.naming.InitialContext");
/*     */ 
/*  57 */     Object workManager = null;
/*  58 */     Object workManagerCommonJ = null;
/*     */     try
/*     */     {
/*  62 */       Class initialContextClass = Class.forName(initialContextClassName);
/*  63 */       Object initialContext = initialContextClass.newInstance();
/*  64 */       String lookupMethodName = getEnvConfigString(servletConfig, "WorkManagerLookupMethodName", "lookup");
/*     */ 
/*  67 */       workManagerCommonJ = ClassHelperUtils.executeMethodConvertToStandardExceptions(initialContext, lookupMethodName, new Object[] { workManagerKey });
/*     */ 
/*  70 */       String appServDelegateMethod = getAppServWMDelegateMethodName();
/*  71 */       String getDelegateMethodName = getEnvConfigString(servletConfig, "WorkManagerGetDelegateMethodName", appServDelegateMethod);
/*     */ 
/*  74 */       if (getDelegateMethodName != null)
/*     */       {
/*  76 */         workManager = ClassHelperUtils.executeMethodConvertToStandardExceptions(workManagerCommonJ, getDelegateMethodName, new Object[0]);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  83 */       throw e;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  87 */       throw new ServiceException(e, "csServletFailedToGetWorkManager", new Object[] { workManagerKey });
/*     */     }
/*     */ 
/*  92 */     if ((workManager == null) && (workManagerCommonJ != null))
/*     */     {
/*  94 */       workManager = workManagerCommonJ;
/*     */     }
/*     */ 
/*  97 */     return workManager;
/*     */   }
/*     */ 
/*     */   public static String getAppServWMDelegateMethodName()
/*     */   {
/* 102 */     String delegateMethod = null;
/* 103 */     String appServerName = EnvUtils.getAppServerType();
/*     */ 
/* 105 */     if (appServerName.equalsIgnoreCase("weblogic"))
/*     */     {
/* 107 */       delegateMethod = "getDelegate";
/*     */     }
/*     */ 
/* 110 */     return delegateMethod;
/*     */   }
/*     */ 
/*     */   public static Object scheduleClientInitiatedManagedThread(Object workManager, Thread t, Map workParams)
/*     */     throws DataException, ServiceException
/*     */   {
/* 126 */     String appServerName = EnvUtils.getAppServerType();
/*     */ 
/* 128 */     if ((appServerName != null) && (appServerName.equalsIgnoreCase("websphere")))
/*     */     {
/*     */       try
/*     */       {
/* 132 */         Class WorkManagerWrapper = ClassHelperUtils.createClass("idcservlet.WorkManagerWrapper");
/* 133 */         Constructor con = WorkManagerWrapper.getConstructor(new Class[] { Runnable.class });
/* 134 */         Object wMWrapper = con.newInstance(new Object[] { t });
/* 135 */         ClassHelperUtils.executeMethodConvertToStandardExceptions(workManager, "schedule", new Object[] { wMWrapper });
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 139 */         SystemUtils.traceDumpException("socketrequests", "Unable to schedule the thread " + t, e);
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 144 */       ClassHelperUtils.executeMethodConvertToStandardExceptions(workManager, "schedule", new Object[] { t });
/*     */     }
/*     */ 
/* 147 */     return null;
/*     */   }
/*     */ 
/*     */   public static String getEnvConfigString(IdcServletConfig servletConfig, String key, String defVal)
/*     */   {
/* 152 */     String val = null;
/* 153 */     Object oVal = servletConfig.getAttribute(key);
/* 154 */     if (oVal != null)
/*     */     {
/* 156 */       val = oVal.toString();
/*     */     }
/* 158 */     if ((val == null) || (val.length() == 0))
/*     */     {
/* 160 */       val = SharedObjects.getEnvironmentValue(key);
/*     */     }
/* 162 */     if ((val == null) || (val.length() == 0))
/*     */     {
/* 164 */       val = defVal;
/*     */     }
/* 166 */     return val;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 171 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93944 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.WorkManagerUtils
 * JD-Core Version:    0.5.4
 */