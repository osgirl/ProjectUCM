/*     */ package intradoc.autosuggest;
/*     */ 
/*     */ import intradoc.autosuggest.datastore.ContextInfoStorage;
/*     */ import intradoc.autosuggest.datastore.MetaStorage;
/*     */ import intradoc.autosuggest.indexer.AutoSuggestIndexHandler;
/*     */ import intradoc.autosuggest.partition.AutoSuggestPartitionedContext;
/*     */ import intradoc.autosuggest.partition.AutoSuggestPartitioner;
/*     */ import intradoc.autosuggest.partition.DefaultAutoSuggestPartitioner;
/*     */ import intradoc.autosuggest.records.ContextInfo;
/*     */ import intradoc.autosuggest.records.MetaInfo;
/*     */ import intradoc.autosuggest.utils.AutoSuggestUtils;
/*     */ import intradoc.autosuggest.utils.GramParameterConstructor;
/*     */ import intradoc.autosuggest.utils.OccurrenceFilter;
/*     */ import intradoc.autosuggest.utils.ParameterOccurrenceFilter;
/*     */ import intradoc.autosuggest.utils.ResultComparator;
/*     */ import intradoc.autosuggest.utils.ResultTermInfo;
/*     */ import intradoc.autosuggest.utils.Scorer;
/*     */ import intradoc.autosuggest.utils.SecurityOccurrenceFilter;
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ import java.util.concurrent.Callable;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.Future;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ 
/*     */ public class AutoSuggestManager
/*     */ {
/*     */   public AutoSuggestContext m_context;
/*     */   public GramParameterConstructor m_gramParameterConstructor;
/*     */   public AutoSuggestIndexHandler m_defaultIndexHandler;
/*     */   public AutoSuggestPartitioner m_defaultPartitioner;
/*     */   public Map<String, AutoSuggestIndexHandler> m_partitionIndexHandlers;
/*     */   public List<OccurrenceFilter> m_occurrenceFilters;
/*     */   public Scorer m_scorer;
/*     */   public MetaStorage m_metaStorage;
/*     */   public MetaInfo m_metaInfo;
/*     */ 
/*     */   public AutoSuggestManager(AutoSuggestContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/*  74 */     this.m_context = context;
/*     */ 
/*  78 */     this.m_metaStorage = new MetaStorage(this.m_context);
/*  79 */     this.m_metaInfo = getMetaInfo(this.m_context.m_contextKey);
/*     */ 
/*  81 */     this.m_context.prepareActiveContext(this.m_metaInfo);
/*  82 */     this.m_gramParameterConstructor = new GramParameterConstructor();
/*  83 */     this.m_defaultIndexHandler = new AutoSuggestIndexHandler(this.m_context, this.m_metaInfo);
/*  84 */     this.m_defaultPartitioner = new DefaultAutoSuggestPartitioner();
/*  85 */     this.m_partitionIndexHandlers = new HashMap();
/*  86 */     this.m_partitionIndexHandlers.put("Default", this.m_defaultIndexHandler);
/*  87 */     this.m_occurrenceFilters = new ArrayList();
/*  88 */     this.m_occurrenceFilters.add(new SecurityOccurrenceFilter());
/*  89 */     this.m_occurrenceFilters.add(new ParameterOccurrenceFilter());
/*  90 */     this.m_scorer = new Scorer(this.m_context, this.m_metaInfo);
/*  91 */     this.m_context.m_service.setCachedObject("AutoSuggestManager", this);
/*  92 */     DataBinder binder = this.m_context.m_service.getBinder();
/*  93 */     PluginFilters.filter("postInitAutoSuggestManager", this.m_context.m_workspace, binder, this.m_context.m_service);
/*     */   }
/*     */ 
/*     */   public AutoSuggestManager(AutoSuggestContext context, GramParameterConstructor gramParametersConstructor, AutoSuggestIndexHandler indexHandler, List<OccurrenceFilter> occurenceFilters, Scorer scorer)
/*     */     throws DataException, ServiceException
/*     */   {
/* 108 */     this.m_context = context;
/* 109 */     this.m_gramParameterConstructor = gramParametersConstructor;
/* 110 */     this.m_defaultIndexHandler = indexHandler;
/* 111 */     this.m_occurrenceFilters = occurenceFilters;
/* 112 */     this.m_scorer = scorer;
/*     */   }
/*     */ 
/*     */   public void addOccurrenceFilter(OccurrenceFilter filter)
/*     */   {
/* 121 */     this.m_occurrenceFilters.add(filter);
/*     */   }
/*     */ 
/*     */   public List<ResultTermInfo> suggestSimilar(String query, UserData userData, int suggestionCount)
/*     */     throws DataException, ServiceException
/*     */   {
/* 131 */     List resultList = new ArrayList();
/*     */ 
/* 133 */     Map queryGramMap = this.m_gramParameterConstructor.contructGramParameters(query);
/* 134 */     Map suggesterThreadParams = new HashMap();
/* 135 */     suggesterThreadParams.put("AutoSuggestManager", this);
/* 136 */     suggesterThreadParams.put("UserData", userData);
/* 137 */     suggesterThreadParams.put("Query", query);
/* 138 */     suggesterThreadParams.put("QueryGramMap", queryGramMap);
/* 139 */     ConcurrentHashMap processed = new ConcurrentHashMap();
/* 140 */     suggesterThreadParams.put("processed", processed);
/* 141 */     suggesterThreadParams.put("suggestionCount", Integer.valueOf(suggestionCount));
/* 142 */     Iterator queryGramIterator = queryGramMap.keySet().iterator();
/* 143 */     int autoSuggesterThreadCount = SharedObjects.getEnvironmentInt("AutoSuggesterPerContextThreadCount", 2);
/* 144 */     long autoSuggesterPerGramThreadTimeOut = AutoSuggestUtils.getAutoSuggesterPerGramThreadTimeOut();
/* 145 */     ExecutorService pool = Executors.newFixedThreadPool(autoSuggesterThreadCount);
/* 146 */     Set futureResultSet = new HashSet();
/* 147 */     while (queryGramIterator.hasNext())
/*     */     {
/* 152 */       String queryGram = (String)queryGramIterator.next();
/* 153 */       suggesterThreadParams.put("Gram", queryGram);
/*     */ 
/* 155 */       Callable autoSuggesterThread = new AutoSuggesterChildThread(suggesterThreadParams);
/* 156 */       Future future = pool.submit(autoSuggesterThread);
/* 157 */       futureResultSet.add(future);
/*     */     }
/*     */ 
/* 162 */     for (Future future : futureResultSet)
/*     */     {
/*     */       try
/*     */       {
/* 166 */         List list = (List)future.get(autoSuggesterPerGramThreadTimeOut, TimeUnit.MILLISECONDS);
/* 167 */         if ((list != null) && (list.size() > 0))
/*     */         {
/* 169 */           resultList.addAll(list);
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 174 */         Report.error("autosuggest", "Error waiting for the suggestion thread to finish", e);
/*     */       }
/*     */     }
/* 177 */     pool.shutdownNow();
/* 178 */     Collections.sort(resultList, new ResultComparator());
/* 179 */     return resultList;
/*     */   }
/*     */ 
/*     */   public void rebuildIndex()
/*     */     throws DataException, ServiceException
/*     */   {
/* 188 */     index(true);
/*     */   }
/*     */ 
/*     */   public void index()
/*     */     throws DataException, ServiceException
/*     */   {
/* 197 */     index(false);
/*     */   }
/*     */ 
/*     */   public void index(boolean isRebuild)
/*     */     throws DataException, ServiceException
/*     */   {
/* 207 */     String contextKey = this.m_context.m_contextKey;
/*     */ 
/* 213 */     Report.trace("autosuggest", "Processing index queues for field " + contextKey, null);
/* 214 */     this.m_defaultIndexHandler.indexQueues();
/*     */ 
/* 218 */     ContextInfo contextInfo = ContextInfoStorage.getContextInfo(contextKey);
/* 219 */     DataBinder binder = prepareIndexerBinder((this.m_context.m_service != null) ? this.m_context.m_service.getBinder() : null, contextInfo, this.m_metaInfo, isRebuild);
/* 220 */     Service service = (this.m_context.m_service != null) ? this.m_context.m_service : createDummyService(binder, this.m_context.m_workspace);
/* 221 */     service.setBinder(binder);
/* 222 */     service.setConditionVar("AllowDataSourceAccess", true);
/* 223 */     this.m_context.m_service = service;
/* 224 */     if ((this.m_metaInfo == null) || (isRebuild == true))
/*     */     {
/* 230 */       if (isRebuild == true)
/*     */       {
/* 232 */         prepareRebuildContext();
/*     */       }
/*     */ 
/* 235 */       String initDataSource = contextInfo.getInitDataSource();
/* 236 */       if ((isValidDataSource(initDataSource)) && (!contextInfo.isInitQueued()))
/*     */       {
/* 238 */         binder.putLocal("dataSource", initDataSource);
/* 239 */         Report.trace("autosuggest", "Index Initialization/Rebuild for field " + contextKey + " using data source " + initDataSource, null);
/* 240 */         service.createResultSetSQL();
/* 241 */         DataResultSet drset = (DataResultSet)binder.getResultSet("AutoSuggestResultSet");
/* 242 */         Map partitionedResultSets = this.m_defaultPartitioner.partition("init", this.m_context, contextInfo, drset);
/* 243 */         index(partitionedResultSets);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 252 */       String addDataSource = contextInfo.getAddDataSource();
/* 253 */       if ((isValidDataSource(addDataSource)) && (!contextInfo.isAddQueued()))
/*     */       {
/* 255 */         binder.putLocal("dataSource", addDataSource);
/* 256 */         Report.trace("autosuggest", "Index insertions for field " + contextKey + " using data source " + addDataSource, null);
/* 257 */         service.createResultSetSQL();
/* 258 */         DataResultSet drset = (DataResultSet)binder.getResultSet("AutoSuggestResultSet");
/* 259 */         Map partitionedResultSets = this.m_defaultPartitioner.partition("add", this.m_context, contextInfo, drset);
/* 260 */         index(partitionedResultSets);
/*     */       }
/*     */ 
/* 265 */       String deleteDataSource = contextInfo.getDeleteDataSource();
/* 266 */       if ((deleteDataSource != null) && (deleteDataSource.length() > 0))
/*     */       {
/* 268 */         binder.removeResultSet("AutoSuggestResultSet");
/* 269 */         if ((isValidDataSource(deleteDataSource)) && (!contextInfo.isDeleteQueued()))
/*     */         {
/* 271 */           Report.trace("autosuggest", "Index deletions for field " + contextKey + " using data source " + deleteDataSource, null);
/* 272 */           binder.putLocal("dataSource", deleteDataSource);
/* 273 */           service.createResultSetSQL();
/* 274 */           DataResultSet drset = (DataResultSet)binder.getResultSet("AutoSuggestResultSet");
/* 275 */           Map partitionedResultSets = this.m_defaultPartitioner.partition("delete", this.m_context, contextInfo, drset);
/* 276 */           remove(partitionedResultSets);
/*     */         }
/*     */       }
/*     */     }
/* 280 */     binder.removeResultSet("AutoSuggestResultSet");
/*     */ 
/* 284 */     if (this.m_metaInfo == null)
/*     */     {
/* 286 */       this.m_metaInfo = new MetaInfo(this.m_context);
/*     */     }
/* 288 */     String indexedDate = binder.getLocal("endTimestamp");
/* 289 */     String activeIndex = this.m_context.getActiveIndex();
/* 290 */     this.m_metaInfo.init(indexedDate, activeIndex);
/* 291 */     putMetaInfo(this.m_context.getBaseContextKey(), this.m_metaInfo);
/*     */   }
/*     */ 
/*     */   public DataBinder prepareIndexerBinder(DataBinder binder, ContextInfo contextInfo, MetaInfo metaInfo, boolean isRebuild)
/*     */     throws ServiceException, DataException
/*     */   {
/* 305 */     long currentTimeMilli = System.currentTimeMillis();
/* 306 */     String table = contextInfo.getTable();
/* 307 */     String field = contextInfo.getField();
/* 308 */     if (binder == null)
/*     */     {
/* 310 */       binder = new DataBinder();
/*     */     }
/* 312 */     binder.putLocal("field", field);
/* 313 */     binder.putLocal("table", table);
/*     */ 
/* 316 */     boolean useAccounts = SharedObjects.getEnvValueAsBoolean("UseAccounts", false);
/* 317 */     boolean useEntitySecurity = SharedObjects.getEnvValueAsBoolean("UseEntitySecurity", false);
/* 318 */     boolean useRoleSecurity = SharedObjects.getEnvValueAsBoolean("UseRoleSecurity", false);
/* 319 */     if (useAccounts == true)
/*     */     {
/* 321 */       binder.putLocal("UseAccounts", "1");
/*     */     }
/* 323 */     if (useEntitySecurity == true)
/*     */     {
/* 325 */       binder.putLocal("UseEntitySecurity", "1");
/*     */     }
/* 327 */     if (useRoleSecurity == true)
/*     */     {
/* 329 */       binder.putLocal("UseRoleSecurity", "1");
/*     */     }
/* 331 */     String extraParameters = contextInfo.getExtraParameters();
/* 332 */     if ((extraParameters != null) && (extraParameters.length() > 0))
/*     */     {
/* 334 */       binder.putLocal("extraParameters", extraParameters);
/*     */     }
/*     */ 
/* 341 */     String endTimestamp = LocaleUtils.formatODBC(new Date(currentTimeMilli));
/* 342 */     binder.putLocal("endTimestamp", endTimestamp);
/*     */ 
/* 344 */     long truncatedEndDate = currentTimeMilli / 60000L * 60000L;
/* 345 */     String endDate = LocaleUtils.formatODBC(new Date(truncatedEndDate));
/* 346 */     binder.putLocal("endDate", endDate);
/* 347 */     if ((metaInfo != null) && (!isRebuild))
/*     */     {
/* 350 */       binder.putLocal("beginTimestamp", metaInfo.m_indexedDate);
/*     */ 
/* 352 */       Date beginTimestamp = LocaleUtils.parseODBC(metaInfo.m_indexedDate);
/* 353 */       long truncatedBeginDate = beginTimestamp.getTime() / 60000L * 60000L;
/* 354 */       String beginDate = LocaleUtils.formatODBC(new Date(truncatedBeginDate));
/* 355 */       binder.putLocal("beginDate", beginDate);
/*     */     }
/*     */ 
/* 358 */     binder.putLocal("resultName", "AutoSuggestResultSet");
/* 359 */     PluginFilters.filter("postPrepareIndexerBinder", this.m_context.m_workspace, binder, this.m_context.m_service);
/* 360 */     return binder;
/*     */   }
/*     */ 
/*     */   public static Service createDummyService(DataBinder binder, Workspace ws)
/*     */     throws DataException, ServiceException
/*     */   {
/* 374 */     Service s = new Service();
/* 375 */     s.init(ws, null, binder, new ServiceData());
/* 376 */     s.initDelegatedObjects();
/* 377 */     UserData userData = SecurityUtils.createDefaultAdminUserData();
/* 378 */     s.setUserData(userData);
/* 379 */     return s;
/*     */   }
/*     */ 
/*     */   public static Service createDummyService(DataBinder binder, Workspace ws, UserData userData) throws DataException, ServiceException {
/* 383 */     Service s = new Service();
/* 384 */     s.init(ws, null, binder, new ServiceData());
/* 385 */     s.initDelegatedObjects();
/* 386 */     s.setUserData(userData);
/* 387 */     return s;
/*     */   }
/*     */ 
/*     */   public static boolean isValidDataSource(String dataSource)
/*     */   {
/* 396 */     if ((dataSource == null) || (dataSource.length() == 0))
/*     */     {
/* 398 */       return false;
/*     */     }
/* 400 */     DataResultSet definedDataSources = SharedObjects.getTable("DataSources");
/* 401 */     Vector dataSourceRow = definedDataSources.findRow(0, dataSource);
/*     */ 
/* 404 */     return dataSourceRow != null;
/*     */   }
/*     */ 
/*     */   public Map<String, DataResultSet> partition(String mode, ContextInfo contextInfo, DataResultSet inputResultSet)
/*     */     throws DataException, ServiceException
/*     */   {
/* 419 */     Map partitionedMap = new HashMap();
/* 420 */     String partitionerIdentifier = contextInfo.getPartitioner(mode);
/* 421 */     if ((partitionerIdentifier == null) || (partitionerIdentifier.length() == 0))
/*     */     {
/* 423 */       partitionedMap.put("Default", inputResultSet);
/* 424 */       return partitionedMap;
/*     */     }
/*     */ 
/* 429 */     List partitionerSplit = StringUtils.makeListFromSequence(partitionerIdentifier, '=', '=', 0);
/* 430 */     if (partitionerSplit.size() == 2)
/*     */     {
/* 432 */       String partitionerType = (String)partitionerSplit.get(0);
/* 433 */       String partitioner = (String)partitionerSplit.get(1);
/*     */ 
/* 437 */       if (partitionerType.equalsIgnoreCase("class"))
/*     */       {
/* 439 */         Class partitionerClass = ClassHelperUtils.createClass(partitioner);
/* 440 */         AutoSuggestPartitioner partitionerObject = (AutoSuggestPartitioner)ClassHelperUtils.createInstance(partitionerClass);
/*     */ 
/* 442 */         partitionedMap = partitionerObject.partition(mode, this.m_context, contextInfo, inputResultSet);
/*     */       }
/*     */ 
/* 447 */       if (partitionerType.equalsIgnoreCase("datasource"))
/*     */       {
/* 449 */         DataBinder binder = this.m_context.m_service.getBinder();
/* 450 */         binder.putLocal("resultName", "PartitionResultSet");
/* 451 */         binder.putLocal("dataSource", partitioner);
/*     */ 
/* 453 */         DataResultSet partitionRset = null;
/* 454 */         IdcStringBuilder identifiers = new IdcStringBuilder();
/* 455 */         int identifiersCount = 0;
/* 456 */         for (inputResultSet.first(); inputResultSet.isRowPresent(); inputResultSet.next())
/*     */         {
/* 458 */           if (identifiers.length() > 0)
/*     */           {
/* 460 */             identifiers.append(",");
/*     */           }
/* 462 */           identifiers.append(inputResultSet.getStringValueByName(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER));
/* 463 */           ++identifiersCount;
/* 464 */           if (identifiersCount < 200)
/*     */             continue;
/* 466 */           binder.putLocal("identifiers", identifiers.toString());
/* 467 */           this.m_context.m_service.createResultSetSQL();
/* 468 */           DataResultSet drset = (DataResultSet)binder.getResultSet("PartitionResultSet");
/* 469 */           if (partitionRset == null)
/*     */           {
/* 471 */             partitionRset = drset;
/*     */           }
/*     */           else
/*     */           {
/* 475 */             partitionRset.mergeWithFlags(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER, drset, 16, 0);
/*     */           }
/* 477 */           identifiers = new IdcStringBuilder();
/* 478 */           identifiersCount = 0;
/*     */         }
/*     */ 
/* 481 */         if (identifiers.length() > 0)
/*     */         {
/* 483 */           binder.putLocal("identifiers", identifiers.toString());
/* 484 */           this.m_context.m_service.createResultSetSQL();
/* 485 */           DataResultSet drset = (DataResultSet)binder.getResultSet("PartitionResultSet");
/* 486 */           if (partitionRset == null)
/*     */           {
/* 488 */             partitionRset = drset;
/*     */           }
/*     */           else
/*     */           {
/* 492 */             partitionRset.mergeWithFlags(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER, drset, 16, 0);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 497 */         for (inputResultSet.first(); inputResultSet.isRowPresent(); inputResultSet.next())
/*     */         {
/* 499 */           Vector inputRow = inputResultSet.getCurrentRowValues();
/* 500 */           String identifier = inputResultSet.getStringValueByName(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER);
/* 501 */           Vector partitionRow = partitionRset.findRow(0, identifier);
/* 502 */           if (partitionRow == null)
/*     */             continue;
/* 504 */           String partition = (String)partitionRow.get(1);
/*     */ 
/* 506 */           DataResultSet inputPartitionedRset = (DataResultSet)partitionedMap.get(partition);
/* 507 */           if (inputPartitionedRset == null)
/*     */           {
/* 509 */             inputPartitionedRset = new DataResultSet();
/* 510 */             inputPartitionedRset.copyFieldInfo(inputResultSet);
/* 511 */             partitionedMap.put(partition, inputPartitionedRset);
/*     */           }
/* 513 */           inputPartitionedRset.addRow(inputRow);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 518 */     return partitionedMap;
/*     */   }
/*     */ 
/*     */   public void index(Map<String, DataResultSet> partitionedResultSets)
/*     */     throws DataException, ServiceException
/*     */   {
/* 530 */     Iterator partitionIterator = partitionedResultSets.keySet().iterator();
/* 531 */     while (partitionIterator.hasNext())
/*     */     {
/* 533 */       String partitionId = (String)partitionIterator.next();
/* 534 */       DataResultSet partitionedDrset = (DataResultSet)partitionedResultSets.get(partitionId);
/* 535 */       AutoSuggestIndexHandler indexHandler = getIndexHandler(partitionId);
/* 536 */       indexHandler.index(partitionedDrset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void remove(Map<String, DataResultSet> partitionedResultSets)
/*     */     throws DataException, ServiceException
/*     */   {
/* 549 */     Iterator partitionIterator = partitionedResultSets.keySet().iterator();
/* 550 */     while (partitionIterator.hasNext())
/*     */     {
/* 552 */       String partitionId = (String)partitionIterator.next();
/* 553 */       DataResultSet partitionedDrset = (DataResultSet)partitionedResultSets.get(partitionId);
/* 554 */       AutoSuggestIndexHandler indexHandler = getIndexHandler(partitionId);
/* 555 */       indexHandler.remove(partitionedDrset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cleanup()
/*     */     throws DataException, ServiceException
/*     */   {
/* 565 */     this.m_defaultIndexHandler.clear();
/*     */   }
/*     */ 
/*     */   public AutoSuggestIndexHandler getIndexHandler(String partitionId)
/*     */     throws DataException, ServiceException
/*     */   {
/* 576 */     if ((partitionId == null) || (partitionId.length() == 0))
/*     */     {
/* 578 */       return this.m_defaultIndexHandler;
/*     */     }
/* 580 */     AutoSuggestIndexHandler indexHandler = (AutoSuggestIndexHandler)this.m_partitionIndexHandlers.get(partitionId);
/* 581 */     if (indexHandler == null)
/*     */     {
/* 583 */       AutoSuggestPartitionedContext context = new AutoSuggestPartitionedContext(partitionId, this.m_context.m_contextKey, this.m_context.m_workspace);
/* 584 */       indexHandler = new AutoSuggestIndexHandler(context, this.m_metaInfo);
/* 585 */       this.m_partitionIndexHandlers.put(partitionId, indexHandler);
/*     */     }
/* 587 */     return indexHandler;
/*     */   }
/*     */ 
/*     */   public MetaInfo getMetaInfo(String contextKey)
/*     */     throws DataException
/*     */   {
/* 598 */     contextKey = contextKey.toLowerCase();
/* 599 */     MetaInfo metaInfo = this.m_metaStorage.get(contextKey);
/* 600 */     return metaInfo;
/*     */   }
/*     */ 
/*     */   public void putMetaInfo(String contextKey, MetaInfo metaInfo) throws DataException {
/* 604 */     contextKey = contextKey.toLowerCase();
/* 605 */     this.m_metaStorage.put(contextKey, metaInfo);
/*     */   }
/*     */ 
/*     */   public void prepareRebuildContext()
/*     */     throws DataException, ServiceException
/*     */   {
/* 619 */     if (this.m_metaInfo == null)
/*     */       return;
/* 621 */     String switchedActiveIndex = this.m_metaInfo.getSwitchedActiveIndex();
/* 622 */     Report.trace("autosuggest", "Switched active index " + switchedActiveIndex + " for context " + this.m_context.m_contextKey, null);
/* 623 */     this.m_context.prepareActiveContext(switchedActiveIndex);
/* 624 */     this.m_defaultIndexHandler = new AutoSuggestIndexHandler(this.m_context, this.m_metaInfo);
/* 625 */     this.m_defaultPartitioner = new DefaultAutoSuggestPartitioner();
/* 626 */     this.m_partitionIndexHandlers = new HashMap();
/* 627 */     this.m_partitionIndexHandlers.put("Default", this.m_defaultIndexHandler);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 632 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105661 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.AutoSuggestManager
 * JD-Core Version:    0.5.4
 */