/*     */ package intradoc.server.script;
/*     */ 
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.ScriptInfo;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcMessage;
/*     */ 
/*     */ public class ScriptExtensionUtils
/*     */ {
/*     */   public static PageMerger getOrCreatePageMerger(DataBinder binder, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/*  46 */     PageMerger retVal = null;
/*  47 */     if (context == null)
/*     */     {
/*  49 */       retVal = new PageMerger(binder, null);
/*     */     }
/*  51 */     if (retVal == null)
/*     */     {
/*  53 */       retVal = getPageMerger(context);
/*     */     }
/*  55 */     if (retVal == null)
/*     */     {
/*  57 */       retVal = new PageMerger(binder, context);
/*     */     }
/*  59 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static PageMerger getPageMerger(ExecutionContext context)
/*     */   {
/*  64 */     if (context == null)
/*     */     {
/*  66 */       return null;
/*     */     }
/*     */ 
/*  69 */     PageMerger merger = (PageMerger)context.getCachedObject("PageMerger");
/*  70 */     if (merger == null)
/*     */     {
/*  72 */       DynamicHtmlMerger m = getDynamicHtmlMerger(context);
/*  73 */       if (m instanceof PageMerger)
/*     */       {
/*  75 */         merger = (PageMerger)m;
/*     */       }
/*     */     }
/*  78 */     return merger;
/*     */   }
/*     */ 
/*     */   public static PageMerger getPageMerger(ExecutionContext context, String errorMsg)
/*     */     throws ServiceException
/*     */   {
/*  84 */     PageMerger merger = getPageMerger(context);
/*  85 */     if (merger == null)
/*     */     {
/*  87 */       throw new ServiceException(errorMsg);
/*     */     }
/*  89 */     return merger;
/*     */   }
/*     */ 
/*     */   public static DynamicHtmlMerger getDynamicHtmlMerger(ExecutionContext context)
/*     */   {
/*  94 */     if (context == null)
/*     */     {
/*  96 */       return null;
/*     */     }
/*  98 */     DynamicHtmlMerger merger = (DynamicHtmlMerger)context.getCachedObject("DynamicHtmlMerger");
/*     */ 
/* 100 */     return merger;
/*     */   }
/*     */ 
/*     */   public static DynamicHtmlMerger getDynamicHtmlMerger(ExecutionContext context, String errorMsg)
/*     */     throws ServiceException
/*     */   {
/* 107 */     DynamicHtmlMerger merger = getDynamicHtmlMerger(context);
/* 108 */     if (merger == null)
/*     */     {
/* 110 */       throw new ServiceException(errorMsg);
/*     */     }
/* 112 */     return merger;
/*     */   }
/*     */ 
/*     */   public static Service getService(ExecutionContext context)
/*     */   {
/* 117 */     if (context == null)
/*     */     {
/* 119 */       return null;
/*     */     }
/*     */ 
/* 122 */     if (context instanceof Service)
/*     */     {
/* 124 */       return (Service)context;
/*     */     }
/*     */ 
/* 127 */     return null;
/*     */   }
/*     */ 
/*     */   public static Service getService(ExecutionContext context, String errorMsg)
/*     */     throws ServiceException
/*     */   {
/* 133 */     Service service = getService(context);
/* 134 */     if (service == null)
/*     */     {
/* 136 */       throw new ServiceException(errorMsg);
/*     */     }
/* 138 */     return service;
/*     */   }
/*     */ 
/*     */   public static DataBinder getBinder(ExecutionContext context)
/*     */   {
/* 143 */     if (context == null)
/*     */     {
/* 145 */       return null;
/*     */     }
/*     */ 
/* 148 */     DataBinder binder = (DataBinder)context.getCachedObject("DataBinder");
/* 149 */     if ((binder == null) && (context instanceof Service))
/*     */     {
/* 151 */       binder = ((Service)context).getBinder();
/*     */     }
/*     */ 
/* 154 */     return binder;
/*     */   }
/*     */ 
/*     */   public static DataBinder getBinder(ExecutionContext context, String errorMsg)
/*     */     throws ServiceException
/*     */   {
/* 160 */     DataBinder binder = getBinder(context);
/*     */ 
/* 162 */     if (binder == null)
/*     */     {
/* 164 */       throw new ServiceException(errorMsg);
/*     */     }
/*     */ 
/* 167 */     return binder;
/*     */   }
/*     */ 
/*     */   public static Object computeReturnObject(int type, boolean bResult, int iResult, double dResult, Object oResult)
/*     */   {
/* 173 */     switch (type)
/*     */     {
/*     */     case 1:
/* 176 */       oResult = new Long((bResult) ? 1L : 0L);
/* 177 */       break;
/*     */     case 2:
/* 179 */       oResult = new Long(iResult);
/* 180 */       break;
/*     */     case 3:
/* 182 */       oResult = new Double(dResult);
/*     */     }
/* 184 */     return oResult;
/*     */   }
/*     */ 
/*     */   public static void appendScriptFunctionReport(IdcAppendable appendable, ScriptInfo info, Object[] args, ExecutionContext context)
/*     */   {
/* 190 */     appendable.append(info.m_key);
/* 191 */     appendable.append("(");
/* 192 */     for (int i = 0; i < args.length - 1; ++i)
/*     */     {
/* 194 */       if (i > 0)
/*     */       {
/* 196 */         appendable.append(',');
/*     */       }
/* 198 */       String str = ScriptUtils.getDisplayString(args[i], context);
/* 199 */       appendable.append(str);
/*     */     }
/* 201 */     appendable.append(")");
/*     */   }
/*     */ 
/*     */   public static void checkSecurityForIdocscript(ExecutionContext context, String role)
/*     */     throws ServiceException
/*     */   {
/* 207 */     UserData userData = null;
/*     */ 
/* 209 */     if (context != null)
/*     */     {
/* 211 */       userData = (UserData)context.getCachedObject("TargetUserData");
/*     */     }
/*     */ 
/* 214 */     if (SecurityUtils.isUserOfRole(userData, role))
/*     */       return;
/* 216 */     throw new ServiceException(null, new IdcMessage("csInsufficientPrivilege", new Object[0]));
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 222 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71806 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.ScriptExtensionUtils
 * JD-Core Version:    0.5.4
 */