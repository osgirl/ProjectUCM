/*     */ package intradoc.autosuggest;
/*     */ 
/*     */ import intradoc.autosuggest.datastore.ContextInfoStorage;
/*     */ import intradoc.autosuggest.indexer.AutoSuggestIndexHandler;
/*     */ import intradoc.autosuggest.indexer.AutoSuggestIndexerThread;
/*     */ import intradoc.autosuggest.records.ContextInfo;
/*     */ import intradoc.autosuggest.utils.AutoSuggestUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHandler;
/*     */ import intradoc.server.cache.IdcCacheFactory;
/*     */ import intradoc.server.cache.IdcCacheRegion;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.Callable;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.Future;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ 
/*     */ public class AutoSuggestServiceHandler extends ServiceHandler
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void getSuggestions()
/*     */     throws DataException, ServiceException
/*     */   {
/*  65 */     validateRequiredFields(this.m_binder, "query");
/*  66 */     String query = this.m_binder.getLocal("query");
/*  67 */     int suggestionCount = DataBinderUtils.getInteger(this.m_binder, "suggestionCount", 10);
/*  68 */     List contextKeysList = getContextKeys(this.m_binder);
/*  69 */     if ((contextKeysList == null) || (contextKeysList.size() == 0))
/*     */     {
/*  71 */       String errorMsg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, "context");
/*  72 */       this.m_service.createServiceException(null, errorMsg);
/*     */     }
/*  74 */     IdcStringBuilder disabledContexts = new IdcStringBuilder();
/*  75 */     Map suggesterThreadParams = new HashMap();
/*  76 */     suggesterThreadParams.put("Service", this.m_service);
/*  77 */     suggesterThreadParams.put("Workspace", this.m_workspace);
/*  78 */     suggesterThreadParams.put("Query", query);
/*  79 */     suggesterThreadParams.put("DataBinder", this.m_binder);
/*  80 */     suggesterThreadParams.put("suggestionCount", Integer.valueOf(suggestionCount));
/*  81 */     int autoSuggesterParentThreadCount = SharedObjects.getEnvironmentInt("AutoSuggesterThreadCount", 2);
/*  82 */     long autoSuggesterThreadTimeOut = AutoSuggestUtils.getAutoSuggesterThreadTimeOut();
/*  83 */     ExecutorService pool = Executors.newFixedThreadPool(autoSuggesterParentThreadCount);
/*  84 */     Set futureResultSet = new HashSet();
/*  85 */     for (String contextKey : contextKeysList)
/*     */     {
/*  90 */       suggesterThreadParams.put("originalContextKey", contextKey);
/*  91 */       Properties contextProperties = AutoSuggestUtils.parseContextKey(contextKey);
/*  92 */       String parsedContextKey = (contextProperties.get("contextKey") != null) ? (String)contextProperties.get("contextKey") : contextKey;
/*     */ 
/*  94 */       boolean isEnabled = ContextInfoStorage.isContextEnabled(parsedContextKey);
/*  95 */       if (!isEnabled)
/*     */       {
/*  97 */         if (disabledContexts.length() > 0)
/*     */         {
/*  99 */           disabledContexts.append(",");
/*     */         }
/* 101 */         disabledContexts.append(parsedContextKey);
/*     */       }
/*     */ 
/* 104 */       suggesterThreadParams.put("parsedContextKey", parsedContextKey);
/* 105 */       suggesterThreadParams.put("contextProperties", contextProperties);
/* 106 */       Callable autoSuggesterThread = new AutoSuggesterParentThread(suggesterThreadParams);
/* 107 */       Future future = pool.submit(autoSuggesterThread);
/* 108 */       futureResultSet.add(future);
/*     */     }
/*     */ 
/* 113 */     for (Future future : futureResultSet)
/*     */     {
/*     */       try
/*     */       {
/* 117 */         future.get(autoSuggesterThreadTimeOut, TimeUnit.MILLISECONDS);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 121 */         Report.error("autosuggest", "Error waiting for the suggestion thread to finish", e);
/*     */       }
/*     */     }
/* 124 */     pool.shutdownNow();
/* 125 */     if ((disabledContexts != null) && (disabledContexts.length() > 0))
/*     */     {
/* 127 */       String statusMessage = LocaleUtils.encodeMessage("csAutoSuggestContextDisabled", null, disabledContexts.toString());
/* 128 */       this.m_binder.putLocal("StatusMessage", statusMessage);
/* 129 */       this.m_binder.putLocal("StatusMessageKey", "!csAutoSuggestContextDisabled," + disabledContexts.toString());
/* 130 */       this.m_binder.putLocal("disabledContexts", disabledContexts.toString());
/*     */     }
/* 132 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addSuggestions()
/*     */     throws DataException, ServiceException
/*     */   {
/* 145 */     validateRequiredFields(this.m_binder, "context, suggestions");
/* 146 */     String suggestions = this.m_binder.getLocal("suggestions");
/* 147 */     AutoSuggestContext context = AutoSuggestUtils.prepareContext(this.m_service, this.m_workspace, this.m_binder);
/* 148 */     boolean isEnabled = ContextInfoStorage.isContextEnabled(context.m_contextKey);
/* 149 */     if (!isEnabled)
/*     */     {
/* 151 */       return;
/*     */     }
/* 153 */     AutoSuggestManager manager = new AutoSuggestManager(context);
/* 154 */     AutoSuggestIndexHandler indexHandler = manager.m_defaultIndexHandler;
/* 155 */     List suggestionsList = StringUtils.makeListFromSequenceSimple(suggestions);
/* 156 */     for (String term : suggestionsList)
/*     */     {
/* 158 */       indexHandler.enqueueTermAddition(term, "-1", null);
/*     */     }
/* 160 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteSuggestions()
/*     */     throws DataException, ServiceException
/*     */   {
/* 173 */     validateRequiredFields(this.m_binder, "context, suggestions");
/* 174 */     String suggestions = this.m_binder.getLocal("suggestions");
/* 175 */     AutoSuggestContext context = AutoSuggestUtils.prepareContext(this.m_service, this.m_workspace, this.m_binder);
/* 176 */     boolean isEnabled = ContextInfoStorage.isContextEnabled(context.m_contextKey);
/* 177 */     if (!isEnabled)
/*     */     {
/* 179 */       return;
/*     */     }
/* 181 */     AutoSuggestManager manager = new AutoSuggestManager(context);
/* 182 */     AutoSuggestIndexHandler indexHandler = manager.m_defaultIndexHandler;
/* 183 */     List suggestionsList = StringUtils.makeListFromSequenceSimple(suggestions);
/* 184 */     for (String term : suggestionsList)
/*     */     {
/* 186 */       indexHandler.enqueueTermDeletion(term, "-1", null);
/*     */     }
/* 188 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void buildIndex()
/*     */     throws DataException, ServiceException
/*     */   {
/* 203 */     String partition = this.m_binder.getLocal("partition");
/*     */ 
/* 205 */     String contexts = null;
/* 206 */     List contextsList = null;
/* 207 */     int numberOfContexts = ContextInfoStorage.getNumberOfContexts();
/* 208 */     if (contexts != null)
/*     */     {
/* 210 */       contextsList = StringUtils.makeListFromSequenceSimple(contexts.toLowerCase());
/* 211 */       numberOfContexts = contextsList.size();
/*     */     }
/*     */ 
/* 214 */     boolean isRebuild = DataBinderUtils.getBoolean(this.m_binder, "isRebuild", true);
/*     */ 
/* 218 */     Iterator contextsIterator = ContextInfoStorage.getContextsIterator();
/* 219 */     if (contextsIterator == null)
/*     */     {
/* 221 */       return;
/*     */     }
/* 223 */     PluginFilters.filter("preAutoSuggestIndexing", this.m_workspace, this.m_binder, this.m_service);
/* 224 */     Thread[] indexerThreads = new Thread[numberOfContexts];
/* 225 */     int threadCount = 0;
/*     */ 
/* 227 */     if (isRebuild)
/*     */     {
/* 229 */       cleanupIndex();
/*     */     }
/*     */ 
/* 232 */     while (contextsIterator.hasNext())
/*     */     {
/* 234 */       String contextKey = (String)contextsIterator.next();
/* 235 */       if ((contextsList != null) && (contextsList.size() > 0) && (!contextsList.contains(contextKey))) {
/*     */         continue;
/*     */       }
/*     */ 
/* 239 */       boolean isEnabled = ContextInfoStorage.isContextEnabled(contextKey);
/* 240 */       if (!isEnabled) {
/*     */         continue;
/*     */       }
/*     */ 
/* 244 */       Report.trace("autosuggest", "Start indexing for field - " + contextKey, null);
/* 245 */       AutoSuggestContext context = AutoSuggestUtils.prepareContext(this.m_service, this.m_workspace, partition, contextKey);
/* 246 */       AutoSuggestIndexerThread autoSuggestIndexerThread = new AutoSuggestIndexerThread(context, isRebuild);
/* 247 */       indexerThreads[threadCount] = new Thread(autoSuggestIndexerThread, AutoSuggestConstants.AUTO_SUGGEST_INDEXER_THREAD_NAME + "." + contextKey);
/* 248 */       indexerThreads[threadCount].start();
/* 249 */       ++threadCount;
/*     */     }
/*     */ 
/* 254 */     for (threadCount = 0; threadCount < numberOfContexts; ++threadCount)
/*     */     {
/*     */       try
/*     */       {
/* 258 */         if (indexerThreads[threadCount] != null)
/*     */         {
/* 260 */           indexerThreads[threadCount].join();
/*     */         }
/*     */       }
/*     */       catch (InterruptedException exception)
/*     */       {
/* 265 */         Report.error("autosuggest", "Interrupted while waiting for auto suggest indexer threads to join.", exception);
/*     */       }
/*     */     }
/* 268 */     PluginFilters.filter("postAutoSuggestIndexing", this.m_workspace, this.m_binder, this.m_service);
/* 269 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   public void cleanupIndex() throws DataException, ServiceException
/*     */   {
/* 274 */     Iterator contextsIterator = ContextInfoStorage.getContextsIterator();
/* 275 */     if ((contextsIterator == null) || (!contextsIterator.hasNext()))
/*     */     {
/* 277 */       return;
/*     */     }
/* 279 */     String contextKey = null;
/*     */     do
/*     */     {
/* 282 */       contextKey = (String)contextsIterator.next();
/* 283 */       boolean isEnabled = ContextInfoStorage.isContextEnabled(contextKey);
/* 284 */       if (isEnabled) {
/*     */         break;
/*     */       }
/*     */     }
/*     */ 
/* 289 */     while (contextsIterator.hasNext());
/* 290 */     if (contextKey == null)
/*     */     {
/* 292 */       return;
/*     */     }
/* 294 */     AutoSuggestContext context = new AutoSuggestContext(contextKey, this.m_service, this.m_workspace);
/* 295 */     AutoSuggestManager manager = new AutoSuggestManager(context);
/* 296 */     manager.prepareRebuildContext();
/* 297 */     String indexName = manager.m_context.getActiveIndex();
/* 298 */     Report.trace("autosuggest", "Cleaning the index " + indexName, null);
/* 299 */     IdcCacheRegion autosuggestIndexCache = IdcCacheFactory.getCacheRegion(indexName, true);
/* 300 */     autosuggestIndexCache.clear();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addSuggestFields()
/*     */     throws DataException, ServiceException
/*     */   {
/* 312 */     validateRequiredFields(this.m_binder, "table, fields");
/*     */ 
/* 316 */     boolean isSuccess = ContextInfoStorage.addFields(this.m_binder);
/* 317 */     if (isSuccess == true)
/*     */     {
/* 319 */       ContextInfoStorage.writeContextStore();
/*     */     }
/* 321 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   public void getSuggestFields()
/*     */     throws DataException, ServiceException
/*     */   {
/* 333 */     Iterator iterator = ContextInfoStorage.getContextsIterator();
/* 334 */     DataResultSet contextsResultSet = new DataResultSet();
/* 335 */     List contextFields = ContextInfo.getFields();
/* 336 */     contextFields.add("activeIndex");
/* 337 */     contextsResultSet.mergeFieldsWithFlags(contextFields, 0);
/*     */ 
/* 339 */     while (iterator.hasNext())
/*     */     {
/* 341 */       String contextKey = (String)iterator.next();
/* 342 */       ContextInfo contextInfo = ContextInfoStorage.getContextInfo(contextKey);
/* 343 */       if (contextInfo == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 347 */       List contextFieldValues = contextInfo.getFieldValues();
/*     */ 
/* 349 */       AutoSuggestContext context = new AutoSuggestContext(contextInfo.getKey(), this.m_workspace);
/* 350 */       context.prepareActiveContext();
/* 351 */       String activeIndex = context.getActiveIndex();
/* 352 */       contextFieldValues.add(activeIndex);
/*     */ 
/* 354 */       contextsResultSet.addRowWithList(contextFieldValues);
/*     */     }
/* 356 */     this.m_binder.addResultSet("AutoSuggestFields", contextsResultSet);
/* 357 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void enableSuggestFields()
/*     */     throws DataException, ServiceException
/*     */   {
/* 367 */     validateRequiredFields(this.m_binder, "table, fields");
/*     */ 
/* 371 */     String fields = this.m_binder.getLocal("fields");
/* 372 */     if ((fields != null) && (fields.length() > 0))
/*     */     {
/* 374 */       String table = this.m_binder.getLocal("table");
/* 375 */       ContextInfoStorage.enableFields(fields, table);
/* 376 */       ContextInfoStorage.writeContextStore();
/*     */     }
/* 378 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void disableSuggestFields()
/*     */     throws DataException, ServiceException
/*     */   {
/* 388 */     validateRequiredFields(this.m_binder, "table, fields");
/*     */ 
/* 392 */     String fields = this.m_binder.getLocal("fields");
/* 393 */     if ((fields != null) && (fields.length() > 0))
/*     */     {
/* 395 */       String table = this.m_binder.getLocal("table");
/* 396 */       ContextInfoStorage.disableFields(fields, table);
/* 397 */       ContextInfoStorage.writeContextStore();
/*     */     }
/* 399 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   public List<String> getContextKeys(DataBinder binder)
/*     */   {
/* 410 */     String contextKeys = this.m_binder.getLocal("context");
/* 411 */     if ((contextKeys == null) || (contextKeys.length() == 0))
/*     */     {
/* 413 */       contextKeys = this.m_binder.getLocal("contexts");
/*     */     }
/* 415 */     List contextKeysList = StringUtils.makeListFromSequenceSimple(contextKeys);
/* 416 */     return contextKeysList;
/*     */   }
/*     */ 
/*     */   public void validateRequiredFields(DataBinder binder, String fieldNames)
/*     */     throws ServiceException
/*     */   {
/* 427 */     List fieldNamesList = StringUtils.makeListFromSequenceSimple(fieldNames);
/* 428 */     for (String fieldName : fieldNamesList)
/*     */     {
/* 430 */       String fieldValue = binder.getLocal(fieldName);
/* 431 */       if ((fieldValue == null) || (fieldValue.length() == 0))
/*     */       {
/* 433 */         String errorMsg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, fieldName);
/* 434 */         this.m_service.createServiceException(null, errorMsg);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 440 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105502 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.AutoSuggestServiceHandler
 * JD-Core Version:    0.5.4
 */