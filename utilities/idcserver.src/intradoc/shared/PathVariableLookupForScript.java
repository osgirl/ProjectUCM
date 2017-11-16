/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.PathScriptConstructInfo;
/*     */ import intradoc.common.PathVariableLookupCallback;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class PathVariableLookupForScript
/*     */   implements PathVariableLookupCallback
/*     */ {
/*     */   public DataBinder m_binder;
/*     */   public ExecutionContext m_cxt;
/*     */   public DynamicHtmlMerger m_pageMerger;
/*     */ 
/*     */   public PathVariableLookupForScript(DataBinder binder, ExecutionContext cxt, DynamicHtmlMerger pageMerger)
/*     */   {
/*  39 */     this.m_binder = binder;
/*  40 */     this.m_cxt = cxt;
/*  41 */     this.m_pageMerger = pageMerger;
/*     */   }
/*     */ 
/*     */   public void prepareScript(PathScriptConstructInfo info, int flags)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public CharSequence executeScript(PathScriptConstructInfo info, int flags)
/*     */     throws ServiceException
/*     */   {
/*  53 */     String val = null;
/*  54 */     if ((info.m_isFunction) && (this.m_pageMerger != null))
/*     */     {
/*  57 */       int nargs = 1;
/*  58 */       Object[] functionArgs = null;
/*  59 */       if (info.m_evaluatedArgs != null)
/*     */       {
/*  61 */         nargs = info.m_evaluatedArgs.length + 1;
/*  62 */         functionArgs = new Object[nargs];
/*  63 */         for (int i = 0; i < info.m_evaluatedArgs.length; ++i)
/*     */         {
/*  65 */           functionArgs[i] = info.m_evaluatedArgs[i];
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/*  70 */         functionArgs = new Object[] { null };
/*     */       }
/*     */       try
/*     */       {
/*  74 */         this.m_pageMerger.computeFunction(info.m_coreName, functionArgs);
/*     */       }
/*     */       catch (IllegalArgumentException e)
/*     */       {
/*  78 */         PathScriptConstructInfo reportInfo = info;
/*  79 */         boolean hadParent = false;
/*  80 */         while (reportInfo.m_parentInfo != null)
/*     */         {
/*  82 */           hadParent = true;
/*  83 */           reportInfo = reportInfo.m_parentInfo;
/*     */         }
/*  85 */         String msg = "Unable to evaluate part of expression '" + info.m_charSequence + "'.";
/*  86 */         if (hadParent)
/*     */         {
/*  88 */           msg = msg + " Expression is sub expression in '" + reportInfo.m_charSequence + "'.";
/*     */         }
/*     */ 
/*  91 */         Report.trace("idocscript", msg, e);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/*  95 */         throw new ServiceException(e);
/*     */       }
/*  97 */       Object retVal = functionArgs[(nargs - 1)];
/*  98 */       if (retVal != null)
/*     */       {
/* 100 */         val = ScriptUtils.getDisplayString(retVal, this.m_cxt);
/*     */       }
/* 102 */       info.m_scriptEvaluated = true;
/*     */     }
/* 104 */     else if (this.m_binder != null)
/*     */     {
/* 106 */       val = this.m_binder.getAllowMissing(info.m_coreName);
/* 107 */       if (val != null)
/*     */       {
/* 109 */         info.m_scriptEvaluated = true;
/*     */       }
/*     */     }
/* 112 */     return val;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 117 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69754 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.PathVariableLookupForScript
 * JD-Core Version:    0.5.4
 */