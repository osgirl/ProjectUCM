/*     */ package intradoc.autosuggest.utils;
/*     */ 
/*     */ import intradoc.autosuggest.indexer.AutoSuggestIndexHandler;
/*     */ import intradoc.autosuggest.records.OccurrenceInfo;
/*     */ import intradoc.autosuggest.records.SecurityIdentifierInfo;
/*     */ import intradoc.autosuggest.records.TermInfo;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class ParameterOccurrenceFilter
/*     */   implements OccurrenceFilter
/*     */ {
/*     */   public AutoSuggestIndexHandler m_indexHandler;
/*     */   public DataBinder m_binder;
/*     */ 
/*     */   public void init(Map<String, Object> parameters)
/*     */     throws DataException, ServiceException
/*     */   {
/*  41 */     this.m_indexHandler = ((AutoSuggestIndexHandler)parameters.get("AutoSuggestIndexHandler"));
/*  42 */     this.m_binder = ((DataBinder)parameters.get("DataBinder"));
/*     */   }
/*     */ 
/*     */   public boolean validate(OccurrenceInfo occurrenceInfo)
/*     */     throws DataException
/*     */   {
/*  51 */     boolean isValid = true;
/*     */ 
/*  55 */     Map extraParameters = new HashMap();
/*  56 */     TermInfo termInfo = this.m_indexHandler.getTermInfo(occurrenceInfo);
/*  57 */     if ((termInfo != null) && (termInfo.m_extraParameters != null) && (termInfo.m_extraParameters.size() > 0))
/*     */     {
/*  59 */       extraParameters.putAll(termInfo.m_extraParameters);
/*     */     }
/*  61 */     if ((this.m_indexHandler.hasSecurityContext()) && (termInfo.hasSecurity()))
/*     */     {
/*  63 */       SecurityIdentifierInfo securityIdentifierInfo = this.m_indexHandler.getSecurityIdentifierInfo(occurrenceInfo);
/*  64 */       if ((securityIdentifierInfo != null) && (securityIdentifierInfo.m_extraParameters != null) && (securityIdentifierInfo.m_extraParameters.size() > 0))
/*     */       {
/*  66 */         extraParameters.putAll(securityIdentifierInfo.m_extraParameters);
/*     */       }
/*     */     }
/*  69 */     if (extraParameters.isEmpty())
/*     */     {
/*  71 */       return true;
/*     */     }
/*     */ 
/*  76 */     Iterator extraParametersIterator = extraParameters.keySet().iterator();
/*  77 */     while (extraParametersIterator.hasNext())
/*     */     {
/*  79 */       String param = (String)extraParametersIterator.next();
/*  80 */       String paramFilter = this.m_binder.getLocal(param);
/*  81 */       if ((paramFilter != null) && (paramFilter.length() > 0))
/*     */       {
/*  83 */         String paramValue = (String)extraParameters.get(param);
/*  84 */         if ((paramValue == null) || (!paramValue.equalsIgnoreCase(paramFilter)))
/*     */         {
/*  86 */           if (Report.m_verbose)
/*     */           {
/*  88 */             Report.trace("autosuggest", "Filter[Parameter Occurrence] - " + param + " filtered For Occurrence info - " + occurrenceInfo.toString(), null);
/*  89 */             if (paramValue != null)
/*     */             {
/*  91 */               Report.trace("autosuggest", "Filter[Parameter Occurrence] - Value is : " + paramValue + " Expected value was : " + paramFilter, null);
/*     */             }
/*     */           }
/*  94 */           isValid = false;
/*  95 */           break;
/*     */         }
/*     */       }
/*     */     }
/*  99 */     if (Report.m_verbose)
/*     */     {
/* 101 */       Report.trace("autosuggest", "Filter[Parameter Occurrence] - " + isValid + " For Occurrence info - " + occurrenceInfo.toString(), null);
/*     */     }
/* 103 */     return isValid;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 108 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103126 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.utils.ParameterOccurrenceFilter
 * JD-Core Version:    0.5.4
 */