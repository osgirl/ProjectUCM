/*     */ package intradoc.autosuggest;
/*     */ 
/*     */ import intradoc.autosuggest.datastore.ContextInfoStorage;
/*     */ import intradoc.autosuggest.records.SecurityIdentifierInfo;
/*     */ import intradoc.autosuggest.records.TermInfo;
/*     */ import intradoc.autosuggest.utils.AutoSuggestUtils;
/*     */ import intradoc.autosuggest.utils.ResultTermInfo;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ import java.util.concurrent.Callable;
/*     */ 
/*     */ public class AutoSuggesterParentThread
/*     */   implements Callable
/*     */ {
/*     */   public String m_originalcontextKey;
/*     */   public String m_contextKey;
/*     */   public Workspace m_workspace;
/*     */   public Service m_service;
/*     */   public DataBinder m_binder;
/*     */   public String m_query;
/*     */   public Properties m_contextProperties;
/*  45 */   public int m_suggestionCount = 10;
/*     */ 
/*     */   public AutoSuggesterParentThread(Map<String, Object> suggesterThreadParams)
/*     */   {
/*  49 */     this.m_originalcontextKey = ((String)suggesterThreadParams.get("originalContextKey"));
/*  50 */     this.m_contextKey = ((String)suggesterThreadParams.get("parsedContextKey"));
/*  51 */     this.m_workspace = ((Workspace)suggesterThreadParams.get("Workspace"));
/*  52 */     this.m_service = ((Service)suggesterThreadParams.get("Service"));
/*  53 */     this.m_binder = ((DataBinder)suggesterThreadParams.get("DataBinder"));
/*  54 */     this.m_query = ((String)suggesterThreadParams.get("Query"));
/*  55 */     this.m_suggestionCount = ((Integer)suggesterThreadParams.get("suggestionCount")).intValue();
/*  56 */     this.m_contextProperties = ((Properties)suggesterThreadParams.get("contextProperties"));
/*     */   }
/*     */ 
/*     */   public DataResultSet call()
/*     */   {
/*  63 */     DataResultSet suggestions = new DataResultSet();
/*     */     try
/*     */     {
/*  66 */       long currentThreadID = Thread.currentThread().getId();
/*  67 */       Thread.currentThread().setName(AutoSuggestConstants.AUTO_SUGGESTER_PARENT_THREAD_NAME + "." + this.m_originalcontextKey + "-" + currentThreadID);
/*     */ 
/*  71 */       String partition = this.m_binder.getLocal("partition");
/*  72 */       AutoSuggestContext context = AutoSuggestUtils.prepareContext(this.m_service, this.m_workspace, partition, this.m_contextKey);
/*  73 */       DataBinder contextBinder = context.m_service.getBinder();
/*  74 */       if ((this.m_contextProperties != null) && (!this.m_contextProperties.isEmpty()))
/*     */       {
/*  76 */         Iterator iterator = this.m_contextProperties.keySet().iterator();
/*  77 */         while (iterator.hasNext())
/*     */         {
/*  79 */           String propertyKey = (String)iterator.next();
/*  80 */           String propertyValue = this.m_contextProperties.getProperty(propertyKey);
/*  81 */           contextBinder.putLocal(propertyKey, propertyValue);
/*     */         }
/*     */       }
/*  84 */       AutoSuggestManager manager = new AutoSuggestManager(context);
/*  85 */       List resultList = null;
/*  86 */       if (manager != null)
/*     */       {
/*  88 */         resultList = manager.suggestSimilar(this.m_query.toLowerCase(), this.m_service.getUserData(), this.m_suggestionCount);
/*     */       }
/*  90 */       if ((resultList != null) && (resultList.size() > 0))
/*     */       {
/*  92 */         int resultCount = 0;
/*  93 */         List finfo = new ArrayList();
/*  94 */         FieldInfo termFieldInfo = new FieldInfo();
/*  95 */         termFieldInfo.m_name = "term";
/*  96 */         FieldInfo scoreFieldInfo = new FieldInfo();
/*  97 */         scoreFieldInfo.m_name = "score";
/*  98 */         FieldInfo proximityFieldInfo = new FieldInfo();
/*  99 */         proximityFieldInfo.m_name = "proximity";
/* 100 */         finfo.add(termFieldInfo);
/* 101 */         finfo.add(scoreFieldInfo);
/* 102 */         finfo.add(proximityFieldInfo);
/* 103 */         suggestions.mergeFieldsWithFlags(finfo, 2);
/*     */ 
/* 107 */         ResultTermInfo initResultInfo = (ResultTermInfo)resultList.get(0);
/* 108 */         FieldInfo securityIdentifierFieldInfo = appendSecurityIdentifierField(suggestions, initResultInfo);
/* 109 */         List extraParamsFieldInfos = appendExtraParameters(suggestions, initResultInfo);
/*     */ 
/* 113 */         for (ResultTermInfo resultInfo : resultList)
/*     */         {
/* 115 */           if (resultCount >= this.m_suggestionCount) {
/*     */             break;
/*     */           }
/*     */ 
/* 119 */           if (resultInfo.m_termInfo == null) {
/*     */             continue;
/*     */           }
/*     */ 
/* 123 */           Vector suggestionRow = suggestions.createEmptyRow();
/* 124 */           suggestionRow.set(termFieldInfo.m_index, resultInfo.m_termInfo.m_actualTerm);
/* 125 */           suggestionRow.set(scoreFieldInfo.m_index, Double.valueOf(resultInfo.m_score));
/* 126 */           suggestionRow.set(proximityFieldInfo.m_index, Double.valueOf(resultInfo.m_proximityIndex));
/* 127 */           if (securityIdentifierFieldInfo != null)
/*     */           {
/* 129 */             String securityIdentifier = (resultInfo.m_securityIdentifierInfo != null) ? resultInfo.m_securityIdentifierInfo.m_identifier : "";
/* 130 */             suggestionRow.set(securityIdentifierFieldInfo.m_index, securityIdentifier);
/*     */           }
/* 132 */           appendExtraParameterValues(suggestionRow, extraParamsFieldInfos, resultInfo);
/* 133 */           suggestions.addRow(suggestionRow);
/* 134 */           ++resultCount;
/*     */         }
/* 136 */         if (Report.m_verbose)
/*     */         {
/* 138 */           Report.trace("autosuggest", "---" + this.m_originalcontextKey + " Suggestions Output --- ", null);
/* 139 */           for (suggestions.first(); suggestions.isRowPresent(); suggestions.next())
/*     */           {
/* 141 */             String term = suggestions.getStringValueByName("term");
/* 142 */             Report.trace("autosuggest", term + "\n", null);
/*     */           }
/*     */         }
/* 145 */         this.m_binder.addResultSet("suggestions." + this.m_originalcontextKey, suggestions);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 150 */       Report.error("autosuggest", "Error processing context " + this.m_contextKey + " for query " + this.m_query, e);
/*     */     }
/*     */     finally
/*     */     {
/* 154 */       this.m_workspace.releaseConnection();
/*     */     }
/* 156 */     return suggestions;
/*     */   }
/*     */ 
/*     */   public FieldInfo appendSecurityIdentifierField(DataResultSet resultset, ResultTermInfo resultInfo)
/*     */   {
/* 166 */     SecurityIdentifierInfo securityIdentifierInfo = resultInfo.m_securityIdentifierInfo;
/* 167 */     FieldInfo securityIdentifierFieldInfo = null;
/*     */ 
/* 169 */     if (securityIdentifierInfo != null)
/*     */     {
/* 171 */       securityIdentifierFieldInfo = new FieldInfo();
/* 172 */       String securityContextKey = securityIdentifierInfo.m_context.getBaseContextKey();
/* 173 */       String securityIdentifierFieldName = ContextInfoStorage.getField(securityContextKey);
/* 174 */       securityIdentifierFieldInfo.m_name = securityIdentifierFieldName;
/* 175 */       List securityFields = new ArrayList();
/* 176 */       securityFields.add(securityIdentifierFieldInfo);
/* 177 */       resultset.mergeFieldsWithFlags(securityFields, 2);
/*     */     }
/* 179 */     return securityIdentifierFieldInfo;
/*     */   }
/*     */ 
/*     */   public List<FieldInfo> appendExtraParameters(DataResultSet resultset, ResultTermInfo resultInfo)
/*     */   {
/* 189 */     Map extraParameters = getExtraParameters(resultInfo);
/* 190 */     List extraParamsFieldInfos = new ArrayList();
/* 191 */     Iterator paramIterator = extraParameters.keySet().iterator();
/* 192 */     while (paramIterator.hasNext())
/*     */     {
/* 194 */       String paramKey = (String)paramIterator.next();
/* 195 */       FieldInfo paramFieldInfo = new FieldInfo();
/* 196 */       paramFieldInfo.m_name = paramKey;
/* 197 */       extraParamsFieldInfos.add(paramFieldInfo);
/*     */     }
/* 199 */     resultset.mergeFieldsWithFlags(extraParamsFieldInfos, 2);
/* 200 */     return extraParamsFieldInfos;
/*     */   }
/*     */ 
/*     */   public void appendExtraParameterValues(Vector row, List<FieldInfo> extraParamsFieldInfos, ResultTermInfo resultInfo)
/*     */   {
/* 210 */     if ((extraParamsFieldInfos == null) || (extraParamsFieldInfos.size() == 0))
/*     */     {
/* 212 */       return;
/*     */     }
/* 214 */     Map extraParameters = getExtraParameters(resultInfo);
/* 215 */     Iterator extraParamsFieldInfoIterator = extraParamsFieldInfos.iterator();
/* 216 */     while (extraParamsFieldInfoIterator.hasNext())
/*     */     {
/* 218 */       FieldInfo paramFieldInfo = (FieldInfo)extraParamsFieldInfoIterator.next();
/* 219 */       String paramValue = (String)extraParameters.get(paramFieldInfo.m_name);
/* 220 */       row.set(paramFieldInfo.m_index, (paramValue != null) ? paramValue : "");
/*     */     }
/*     */   }
/*     */ 
/*     */   public Map<String, String> getExtraParameters(ResultTermInfo resultInfo)
/*     */   {
/* 231 */     Map extraParameters = new HashMap();
/* 232 */     if ((resultInfo.m_termInfo != null) && (resultInfo.m_termInfo.m_extraParameters != null))
/*     */     {
/* 234 */       extraParameters.putAll(resultInfo.m_termInfo.m_extraParameters);
/*     */     }
/* 236 */     if ((resultInfo.m_securityIdentifierInfo != null) && (resultInfo.m_securityIdentifierInfo.m_extraParameters != null))
/*     */     {
/* 238 */       extraParameters.putAll(resultInfo.m_securityIdentifierInfo.m_extraParameters);
/*     */     }
/* 240 */     return extraParameters;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 244 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103908 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.AutoSuggesterParentThread
 * JD-Core Version:    0.5.4
 */