/*     */ package intradoc.autosuggest;
/*     */ 
/*     */ import intradoc.autosuggest.datastore.ContextInfoStorage;
/*     */ import intradoc.autosuggest.indexer.AutoSuggestIndexHandler;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.FilterImplementor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class AutoSuggestFilterImplementor
/*     */   implements FilterImplementor
/*     */ {
/*     */   Workspace m_ws;
/*     */   DataBinder m_binder;
/*     */   ExecutionContext m_cxt;
/*     */ 
/*     */   public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  44 */     this.m_ws = ws;
/*  45 */     this.m_binder = binder;
/*  46 */     this.m_cxt = cxt;
/*  47 */     boolean isAutoSuggestEnabled = SharedObjects.getEnvValueAsBoolean("EnableAutoSuggest", false);
/*  48 */     if (!isAutoSuggestEnabled)
/*     */     {
/*  50 */       return 0;
/*     */     }
/*  52 */     String parameter = (String)cxt.getCachedObject("filterParameter");
/*  53 */     if (parameter == null)
/*     */     {
/*  55 */       return 0;
/*     */     }
/*  57 */     int returnCode = 0;
/*  58 */     if (parameter.equals("postAdd"))
/*     */     {
/*  60 */       returnCode = postAdd();
/*     */     }
/*  62 */     else if (parameter.equals("postDelete"))
/*     */     {
/*  64 */       returnCode = postDelete();
/*     */     }
/*  66 */     return returnCode;
/*     */   }
/*     */ 
/*     */   public int postAdd()
/*     */     throws DataException, ServiceException
/*     */   {
/*  79 */     String table = this.m_binder.getLocal("table");
/*  80 */     Iterator contextIterator = ContextInfoStorage.getContextsIterator();
/*  81 */     while (contextIterator.hasNext())
/*     */     {
/*  83 */       String contextKey = (String)contextIterator.next();
/*  84 */       String contextTable = ContextInfoStorage.getTable(contextKey);
/*  85 */       String contextField = ContextInfoStorage.getField(contextKey);
/*  86 */       if (contextTable.equalsIgnoreCase(table))
/*     */       {
/*  88 */         String contextFieldValue = this.m_binder.getLocal(contextField);
/*  89 */         if ((contextFieldValue != null) && (contextFieldValue.length() > 0))
/*     */         {
/*  91 */           AutoSuggestContext context = new AutoSuggestContext(contextKey, this.m_ws);
/*  92 */           AutoSuggestManager manager = new AutoSuggestManager(context);
/*     */ 
/*  94 */           String extraParameterKeys = ContextInfoStorage.getExtraParameters(contextKey);
/*  95 */           List extraParameterKeysList = StringUtils.makeListFromSequenceSimple(extraParameterKeys);
/*  96 */           Map extraParameters = new HashMap();
/*  97 */           for (String extraParam : extraParameterKeysList)
/*     */           {
/*  99 */             String extraParamValue = this.m_binder.getLocal(extraParam);
/*     */ 
/* 101 */             if ((extraParamValue == null) && (extraParam.startsWith(contextTable + ".")))
/*     */             {
/* 103 */               extraParam = extraParam.substring(contextTable.length() + 1);
/* 104 */               extraParamValue = this.m_binder.getLocal(extraParam);
/*     */             }
/* 106 */             if (extraParamValue != null)
/*     */             {
/* 108 */               extraParameters.put(extraParam, extraParamValue);
/*     */             }
/*     */           }
/* 111 */           AutoSuggestIndexHandler indexHandler = manager.m_defaultIndexHandler;
/* 112 */           indexHandler.enqueueTermAddition(contextFieldValue, "-1", (!extraParameters.isEmpty()) ? extraParameters : null);
/*     */         }
/*     */       }
/*     */     }
/* 116 */     return 0;
/*     */   }
/*     */ 
/*     */   public int postDelete()
/*     */     throws DataException, ServiceException
/*     */   {
/* 129 */     String table = this.m_binder.getLocal("table");
/* 130 */     Iterator contextIterator = ContextInfoStorage.getContextsIterator();
/* 131 */     while (contextIterator.hasNext())
/*     */     {
/* 133 */       String contextKey = (String)contextIterator.next();
/* 134 */       String contextTable = ContextInfoStorage.getTable(contextKey);
/* 135 */       String contextField = ContextInfoStorage.getField(contextKey);
/* 136 */       if (contextTable.equalsIgnoreCase(table))
/*     */       {
/* 138 */         String contextFieldValue = this.m_binder.getLocal(contextField);
/* 139 */         if ((contextFieldValue != null) && (contextFieldValue.length() > 0))
/*     */         {
/* 141 */           AutoSuggestContext context = new AutoSuggestContext(contextKey, this.m_ws);
/* 142 */           AutoSuggestManager manager = new AutoSuggestManager(context);
/* 143 */           AutoSuggestIndexHandler indexHandler = manager.m_defaultIndexHandler;
/* 144 */           indexHandler.enqueueTermDeletion(contextFieldValue, "-1", null);
/*     */         }
/*     */       }
/*     */     }
/* 148 */     return 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 152 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103581 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.AutoSuggestFilterImplementor
 * JD-Core Version:    0.5.4
 */