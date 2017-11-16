/*     */ package intradoc.common;
/*     */ 
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcMethodHolder
/*     */ {
/*     */   public Method m_method;
/*     */   public String m_methodName;
/*     */   public boolean m_hasValidMethod;
/*     */   public Class m_invokeeClass;
/*     */   public NoSuchMethodException m_exception;
/*     */   public Boolean m_suppressAccessControlCheck;
/*     */ 
/*     */   public IdcMethodHolder()
/*     */   {
/*  25 */     this.m_method = null;
/*  26 */     this.m_methodName = null;
/*  27 */     this.m_hasValidMethod = false;
/*  28 */     this.m_invokeeClass = null;
/*  29 */     this.m_exception = null;
/*  30 */     this.m_suppressAccessControlCheck = Boolean.valueOf(false);
/*     */   }
/*     */ 
/*     */   public void init(Class invokeeClass, String methodName, Class[] params, Object[] args) {
/*  34 */     this.m_invokeeClass = invokeeClass;
/*     */     try
/*     */     {
/*  38 */       this.m_methodName = methodName;
/*  39 */       this.m_method = invokeeClass.getMethod(methodName, params);
/*  40 */       this.m_hasValidMethod = true;
/*     */     }
/*     */     catch (NoSuchMethodException e)
/*     */     {
/*     */       try
/*     */       {
/*  46 */         if (args != null)
/*     */         {
/*  48 */           this.m_method = ((Method)ClassHelperUtils.findMatchingMethod(invokeeClass, methodName, params, args, false));
/*     */         }
/*     */       }
/*     */       catch (ServiceException ignore)
/*     */       {
/*  53 */         Report.debug("system", "Unable to find matching method", ignore);
/*     */       }
/*  55 */       if (this.m_method == null)
/*     */       {
/*  57 */         this.m_exception = e;
/*     */       }
/*     */       else
/*     */       {
/*  61 */         this.m_hasValidMethod = true;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public Object invokeMethod(Object invokee, Object[] args, boolean reportExecStatus)
/*     */     throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/*  69 */     if (!this.m_hasValidMethod)
/*     */     {
/*  71 */       if (reportExecStatus)
/*     */       {
/*  73 */         return Boolean.FALSE;
/*     */       }
/*  75 */       if (this.m_exception != null)
/*     */       {
/*  77 */         throw this.m_exception;
/*     */       }
/*  79 */       return null;
/*     */     }
/*  81 */     if (this.m_suppressAccessControlCheck.booleanValue() == true)
/*     */     {
/*  83 */       this.m_method.setAccessible(true);
/*     */     }
/*  85 */     Object returnValue = null;
/*  86 */     returnValue = this.m_method.invoke(invokee, args);
/*  87 */     if (reportExecStatus)
/*     */     {
/*  89 */       returnValue = Boolean.TRUE;
/*     */     }
/*  91 */     return returnValue;
/*     */   }
/*     */ 
/*     */   public Object invokeMethod(Object invokee, Map invokeInfo)
/*     */     throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/*  97 */     Object[] arguments = (Object[])(Object[])invokeInfo.get("arguments");
/*     */ 
/*  99 */     Boolean reportExecStatus = (Boolean)invokeInfo.get("reportExecStatus");
/* 100 */     if (reportExecStatus == null)
/*     */     {
/* 102 */       reportExecStatus = Boolean.FALSE;
/*     */     }
/* 104 */     this.m_suppressAccessControlCheck = ((Boolean)invokeInfo.get("suppressAccessControlCheck"));
/* 105 */     if (this.m_suppressAccessControlCheck == null)
/*     */     {
/* 107 */       this.m_suppressAccessControlCheck = Boolean.FALSE;
/*     */     }
/* 109 */     Object returnObject = null;
/* 110 */     returnObject = invokeMethod(invokee, arguments, reportExecStatus.booleanValue());
/*     */ 
/* 112 */     return returnObject;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 117 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 76763 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcMethodHolder
 * JD-Core Version:    0.5.4
 */