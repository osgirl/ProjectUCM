/*     */ package intradoc.autosuggest;
/*     */ 
/*     */ import intradoc.autosuggest.indexer.AutoSuggestIndexHandler;
/*     */ import intradoc.autosuggest.records.GramInfo;
/*     */ import intradoc.autosuggest.records.OccurrenceInfo;
/*     */ import intradoc.autosuggest.records.SecurityIdentifierInfo;
/*     */ import intradoc.autosuggest.records.TermGramParameters;
/*     */ import intradoc.autosuggest.records.TermInfo;
/*     */ import intradoc.autosuggest.utils.AutoSuggestUtils;
/*     */ import intradoc.autosuggest.utils.DefaultOccurrenceFilter;
/*     */ import intradoc.autosuggest.utils.OccurrenceFilter;
/*     */ import intradoc.autosuggest.utils.ResultTermInfo;
/*     */ import intradoc.autosuggest.utils.Scorer;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.Callable;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class AutoSuggesterChildThread
/*     */   implements Callable
/*     */ {
/*     */   public String m_gram;
/*     */   public AutoSuggestManager m_manager;
/*     */   public UserData m_userData;
/*     */   public String m_query;
/*     */   public Map<String, TermGramParameters> m_queryGramMap;
/*     */   public ConcurrentHashMap<String, String> m_processed;
/*  45 */   public int m_resultCutOff = 10;
/*  46 */   public int m_termsProcessedPerGramLimit = 2000;
/*     */ 
/*     */   public AutoSuggesterChildThread(Map<String, Object> suggesterThreadParams)
/*     */   {
/*  50 */     this.m_gram = ((String)suggesterThreadParams.get("Gram"));
/*  51 */     this.m_manager = ((AutoSuggestManager)suggesterThreadParams.get("AutoSuggestManager"));
/*  52 */     this.m_userData = ((UserData)suggesterThreadParams.get("UserData"));
/*  53 */     this.m_query = ((String)suggesterThreadParams.get("Query"));
/*  54 */     this.m_queryGramMap = ((Map)suggesterThreadParams.get("QueryGramMap"));
/*  55 */     this.m_processed = ((ConcurrentHashMap)suggesterThreadParams.get("processed"));
/*  56 */     this.m_resultCutOff = ((Integer)suggesterThreadParams.get("suggestionCount")).intValue();
/*  57 */     this.m_termsProcessedPerGramLimit = SharedObjects.getEnvironmentInt("AutoSuggesterPerGramTermsProcessedLimit", 2000);
/*     */   }
/*     */ 
/*     */   public List<ResultTermInfo> call()
/*     */   {
/*  64 */     List resultList = null;
/*  65 */     int termCount = 0;
/*     */     try
/*     */     {
/*  68 */       long currentThreadID = Thread.currentThread().getId();
/*  69 */       Thread.currentThread().setName(AutoSuggestConstants.AUTO_SUGGESTER_CHILD_THREAD_NAME + "." + this.m_manager.m_context.m_contextKey + "." + this.m_gram + "-" + currentThreadID);
/*  70 */       Map filterParameters = new HashMap();
/*  71 */       resultList = new ArrayList();
/*     */ 
/*  73 */       filterParameters.put("AutoSuggestContext", this.m_manager.m_context);
/*  74 */       filterParameters.put("AutoSuggestIndexHandler", this.m_manager.m_defaultIndexHandler);
/*  75 */       filterParameters.put("UserData", this.m_userData);
/*  76 */       filterParameters.put("DataBinder", this.m_manager.m_context.m_service.getBinder());
/*     */ 
/*  80 */       GramInfo gramInfo = this.m_manager.m_defaultIndexHandler.getGramInfo(this.m_gram);
/*  81 */       if (gramInfo != null)
/*     */       {
/*  83 */         TermGramParameters gramParams = (TermGramParameters)this.m_queryGramMap.get(this.m_gram);
/*  84 */         filterParameters.put("TermGramParameters", gramParams);
/*  85 */         for (OccurrenceFilter occurrenceFilter : this.m_manager.m_occurrenceFilters)
/*     */         {
/*  87 */           occurrenceFilter.init(filterParameters);
/*     */         }
/*  89 */         int occurenceProcessedCount = 0;
/*  90 */         Iterator occurrenceIterator = this.m_manager.m_defaultIndexHandler.getOccurrenceIterator(gramInfo);
/*  91 */         while ((occurrenceIterator != null) && 
/*  93 */           (occurrenceIterator.hasNext()))
/*     */         {
/*  95 */           if (termCount >= this.m_termsProcessedPerGramLimit)
/*     */           {
/*  97 */             Report.trace("autosuggest", "Processed " + this.m_termsProcessedPerGramLimit + " number of terms for the gram " + this.m_gram + ". Reached the limit for this gram.", null);
/*  98 */             break;
/*     */           }
/* 100 */           ++termCount;
/* 101 */           OccurrenceInfo occurrenceInfo = (OccurrenceInfo)occurrenceIterator.next();
/* 102 */           if (Report.m_verbose)
/*     */           {
/* 104 */             Report.trace("autosuggest", "Processing OccurenceInfo for " + this.m_gram + " - " + occurrenceInfo.toString(), null);
/*     */           }
/* 106 */           TermInfo termInfo = this.m_manager.m_defaultIndexHandler.getTermInfo(occurrenceInfo);
/* 107 */           if (termInfo == null) {
/*     */             continue;
/*     */           }
/*     */ 
/* 111 */           if (Report.m_verbose)
/*     */           {
/* 113 */             Report.trace("autosuggest", "Fetched termInfo for " + this.m_gram + " - " + termInfo.toString(), null);
/*     */           }
/* 115 */           if (this.m_processed.containsKey(termInfo.getKey()))
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 124 */           boolean isValid = true;
/* 125 */           DefaultOccurrenceFilter defaultFilter = new DefaultOccurrenceFilter();
/* 126 */           isValid = defaultFilter.validate(occurrenceInfo);
/* 127 */           if (!isValid) {
/*     */             continue;
/*     */           }
/*     */ 
/* 131 */           this.m_processed.put(termInfo.getKey(), "");
/*     */ 
/* 135 */           for (OccurrenceFilter occurrenceFilter : this.m_manager.m_occurrenceFilters)
/*     */           {
/* 137 */             isValid = (isValid) && (occurrenceFilter.validate(occurrenceInfo));
/* 138 */             if (!isValid) {
/*     */               break;
/*     */             }
/*     */           }
/*     */ 
/* 143 */           if (!isValid)
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 150 */           Report.trace("autosuggest", "Processing termInfo for similarity :" + termInfo.toString(), null);
/* 151 */           double proximity = this.m_manager.m_scorer.proximityIndex(this.m_query, termInfo.m_indexedTerm);
/* 152 */           double proximityCutOff = AutoSuggestUtils.getProximityCutOff();
/*     */ 
/* 154 */           if (Report.m_verbose)
/*     */           {
/* 156 */             Report.trace("autosuggest", "Proximity computed for the term is : " + proximity, null);
/*     */           }
/* 158 */           if (proximity > proximityCutOff)
/*     */           {
/* 160 */             if (Report.m_verbose)
/*     */             {
/* 162 */               Report.trace("autosuggest", "Term passed the proximity cut off. " + proximity + " > " + proximityCutOff, null);
/*     */             }
/* 164 */             double termScore = this.m_manager.m_scorer.score(this.m_queryGramMap, termInfo);
/* 165 */             SecurityIdentifierInfo securityIdentifierInfo = this.m_manager.m_defaultIndexHandler.getSecurityIdentifierInfo(termInfo);
/* 166 */             resultList.add(new ResultTermInfo(termInfo, securityIdentifierInfo, termScore, proximity));
/* 167 */             ++occurenceProcessedCount;
/* 168 */             if (occurenceProcessedCount > this.m_resultCutOff) {
/*     */               break;
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 179 */       Report.error("autosuggest", "Error processing gram " + this.m_gram + " for query " + this.m_query, e);
/*     */     }
/*     */     finally
/*     */     {
/* 183 */       this.m_manager.m_context.m_workspace.releaseConnection();
/*     */     }
/* 185 */     return resultList;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 189 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105661 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.AutoSuggesterChildThread
 * JD-Core Version:    0.5.4
 */