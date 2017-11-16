/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.lang.Queue;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocIndexerAdaptor
/*     */   implements DocIndexerHandler
/*     */ {
/*     */   public ExecutionContext m_context;
/*     */   public IndexerWorkObject m_data;
/*     */   public Queue m_updateQueue;
/*     */   public Queue m_deleteQueue;
/*     */   public Vector m_finishedList;
/*     */   public Hashtable m_docProps;
/*     */   protected boolean[] m_finishedLock;
/*     */   public long m_batchSize;
/*     */   public int m_batchCounter;
/*     */   public int m_docsPerBatch;
/*     */   public long m_maxBatchSize;
/*     */   public boolean m_ignoreIndexingActivity;
/*     */   public boolean m_restart;
/*     */   public boolean m_isRebuild;
/*     */   public Vector m_indexerInfoList;
/*     */   public IndexerConfig m_config;
/*     */   public IndexerExecution m_execution;
/*     */   public IndexerDriverAdaptor m_driver;
/*     */ 
/*     */   public DocIndexerAdaptor()
/*     */   {
/*  42 */     this.m_updateQueue = new Queue();
/*  43 */     this.m_deleteQueue = new Queue();
/*     */ 
/*  46 */     this.m_finishedList = new IdcVector();
/*     */ 
/*  49 */     this.m_docProps = new Hashtable();
/*     */ 
/*  53 */     this.m_finishedLock = new boolean[0];
/*     */ 
/*  55 */     this.m_batchSize = 0L;
/*  56 */     this.m_batchCounter = 0;
/*     */ 
/*  58 */     this.m_docsPerBatch = 25;
/*  59 */     this.m_maxBatchSize = 104857600L;
/*     */ 
/*  61 */     this.m_ignoreIndexingActivity = false;
/*     */ 
/*  63 */     this.m_restart = false;
/*     */   }
/*     */ 
/*     */   public void init(IndexerWorkObject data)
/*     */     throws ServiceException
/*     */   {
/*  73 */     this.m_data = data;
/*     */     try
/*     */     {
/*  76 */       this.m_config = SearchIndexerUtils.getIndexerConfig(data, data.m_state.m_cycleId);
/*  77 */       this.m_execution = SearchIndexerUtils.getIndexerExecution(this.m_config, data);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  81 */       throw new ServiceException("!csCmnSchIndxUnableToInitConfigOrImplementor", e);
/*     */     }
/*     */ 
/*  84 */     this.m_driver = ((IndexerDriverAdaptor)this.m_data.m_driver);
/*  85 */     this.m_docsPerBatch = this.m_data.getEnvironmentInt("MaxCollectionSize", this.m_docsPerBatch);
/*     */ 
/*  87 */     this.m_maxBatchSize = this.m_data.getEnvironmentInt("IndexerLargeFileSize", (int)this.m_maxBatchSize);
/*     */   }
/*     */ 
/*     */   public void validateConfig()
/*     */     throws ServiceException
/*     */   {
/*  93 */     this.m_data.m_driver.validateConfig();
/*     */   }
/*     */ 
/*     */   public void prepare() throws ServiceException
/*     */   {
/*  98 */     Hashtable infos = null;
/*  99 */     if (this.m_data.m_collectionDef != null)
/*     */     {
/* 101 */       infos = (Hashtable)this.m_data.m_collectionDef.m_fieldInfos.clone();
/*     */     }
/* 103 */     this.m_data.m_driver.prepare(infos);
/*     */   }
/*     */ 
/*     */   public void cleanup() throws ServiceException
/*     */   {
/* 108 */     this.m_data.m_driver.cleanup();
/*     */   }
/*     */ 
/*     */   public void finishIndexing(boolean abort)
/*     */   {
/* 113 */     this.m_batchSize = 0L;
/* 114 */     this.m_batchCounter = 0;
/*     */ 
/* 119 */     Queue[] queueList = { this.m_deleteQueue, this.m_updateQueue };
/* 120 */     this.m_ignoreIndexingActivity = false;
/* 121 */     for (int i = 0; i < queueList.length; ++i)
/*     */     {
/* 123 */       Queue queue = queueList[i];
/* 124 */       Vector loadList = new IdcVector();
/*     */ 
/* 126 */       while (!queue.isEmpty())
/*     */       {
/* 128 */         IndexerInfo info = (IndexerInfo)queue.remove();
/* 129 */         loadList.addElement(info);
/*     */       }
/* 131 */       indexList(loadList);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void indexDocument(Properties props, IndexerInfo info)
/*     */   {
/* 144 */     Queue queue = (info.m_isDelete) ? this.m_deleteQueue : this.m_updateQueue;
/* 145 */     synchronized (queue)
/*     */     {
/* 147 */       queue.insert(info);
/* 148 */       this.m_docProps.put(info.m_indexKey, props);
/*     */     }
/* 150 */     this.m_batchCounter += 1;
/* 151 */     this.m_batchSize += info.m_size;
/*     */ 
/* 153 */     if ((!shouldIndexNow(info)) && (((!this.m_config.getBoolean("IsDeleteSingleDocument", false)) || (!info.m_isDelete)))) {
/*     */       return;
/*     */     }
/* 156 */     finishIndexing(false);
/*     */   }
/*     */ 
/*     */   public Vector computeFinishedDocList()
/*     */   {
/*     */     Vector v;
/* 163 */     synchronized (this.m_finishedLock)
/*     */     {
/* 165 */       v = this.m_finishedList;
/* 166 */       this.m_finishedList = new IdcVector();
/*     */     }
/* 168 */     return v;
/*     */   }
/*     */ 
/*     */   public boolean shouldIndexNow(IndexerInfo next)
/*     */   {
/* 177 */     return (next.m_size > this.m_maxBatchSize) || (this.m_batchCounter == this.m_docsPerBatch) || (this.m_batchSize >= this.m_maxBatchSize);
/*     */   }
/*     */ 
/*     */   public void indexList(Vector list)
/*     */   {
/* 188 */     int size = list.size();
/* 189 */     if (size == 0)
/*     */     {
/* 191 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 196 */       this.m_data.setCachedObject("indexerConfig", this.m_config);
/* 197 */       this.m_data.setCachedObject("indexerDriver", this.m_driver);
/* 198 */       this.m_data.setCachedObject("indexList", list);
/* 199 */       this.m_data.setCachedObject("docProperties", this.m_docProps);
/*     */ 
/* 203 */       DataBinder tempBinder = new DataBinder(SharedObjects.getSecureEnvironment());
/* 204 */       boolean isAbort = PluginFilters.filter("preIndexList", this.m_data.m_workspace, tempBinder, this.m_data) == -1;
/*     */ 
/* 206 */       if (!isAbort)
/*     */       {
/* 208 */         prepareIndexing(list);
/* 209 */         this.m_driver.executeIndexer(list, this.m_docProps);
/*     */       }
/* 211 */       PluginFilters.filter("postIndexList", this.m_data.m_workspace, tempBinder, this.m_data);
/* 212 */       this.m_data.setCachedObject("indexList", "");
/* 213 */       this.m_data.setCachedObject("docProperties", "");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 217 */       if (SystemUtils.m_verbose)
/*     */       {
/* 219 */         Report.debug("indexer", null, e);
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 224 */       Report.error(null, "!csIndexerUnableToIndex", e);
/*     */     }
/*     */ 
/* 227 */     if (!this.m_ignoreIndexingActivity)
/*     */     {
/* 229 */       updateListStateAfterFinish(list);
/*     */     }
/*     */ 
/* 232 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 234 */       this.m_docProps.remove(((IndexerInfo)list.elementAt(i)).m_indexKey);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void prepareIndexing(Vector list)
/*     */     throws DataException, ServiceException
/*     */   {
/* 241 */     prepareDoc(list);
/* 242 */     createBulkloadFile(list);
/*     */   }
/*     */ 
/*     */   public void createBulkloadFile(Vector list) throws ServiceException
/*     */   {
/* 247 */     if (!this.m_config.getBoolean("UseBatchFile", true))
/*     */     {
/* 249 */       return;
/*     */     }
/*     */ 
/* 253 */     writeBatchFile(list);
/*     */     try
/*     */     {
/* 260 */       PluginFilters.filter("postWriteBatchFile", this.m_data.m_workspace, null, this.m_data);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 264 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void prepareDoc(Vector list) throws DataException, ServiceException
/*     */   {
/* 270 */     prepareForBatchExecution(list);
/* 271 */     executeCustomParametersOverride();
/*     */ 
/* 273 */     long start = System.currentTimeMillis();
/*     */ 
/* 276 */     int len = list.size();
/* 277 */     Report.trace("indexer", "preparing " + len + " items", null);
/*     */ 
/* 279 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 281 */       IndexerInfo ii = (IndexerInfo)list.elementAt(i);
/* 282 */       Properties props = (Properties)this.m_docProps.get(ii.m_indexKey);
/* 283 */       props.put("conversionPerformed", "" + this.m_execution.performConversion(props, ii));
/*     */     }
/*     */ 
/* 287 */     Report.trace("indexer", "waiting for conversion handlers to finish", null);
/* 288 */     this.m_execution.waitForConversionHandlers();
/*     */ 
/* 291 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 293 */       IndexerInfo ii = (IndexerInfo)list.elementAt(i);
/* 294 */       Properties props = (Properties)this.m_docProps.get(ii.m_indexKey);
/* 295 */       boolean conversionPerformed = StringUtils.convertToBool((String)props.get("conversionPerformed"), false);
/* 296 */       String outputFilePath = props.getProperty("DOC_FN");
/* 297 */       if ((!conversionPerformed) || (outputFilePath.length() == 0)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 301 */       DataBinder binder = new DataBinder();
/* 302 */       binder.setLocalData(props);
/*     */       try
/*     */       {
/* 306 */         PluginFilters.filter("textConversionFilter", this.m_data.m_workspace, binder, this.m_data);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 310 */         Report.trace("indexer", null, e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 315 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 317 */       IndexerInfo ii = (IndexerInfo)list.elementAt(i);
/* 318 */       if ((len == 1) || (!this.m_config.getBoolean("AllowSingleDocIndexing", false)))
/*     */       {
/* 320 */         ii.m_processedAlone = true;
/*     */       }
/* 322 */       Properties props = (Properties)this.m_docProps.get(ii.m_indexKey);
/* 323 */       if (props == null)
/*     */       {
/* 325 */         Report.error(null, LocaleUtils.encodeMessage("csIndexerUnableToFindDocProps", null, ii.m_indexKey), null);
/*     */ 
/* 327 */         list.removeElementAt(i);
/* 328 */         --i;
/* 329 */         --len;
/*     */       }
/*     */       else
/*     */       {
/* 333 */         DataBinder docBinder = new DataBinder();
/* 334 */         docBinder.setLocalData(props);
/* 335 */         this.m_data.setCachedObject("indexerInfo", ii);
/* 336 */         boolean isAbort = false;
/*     */         try
/*     */         {
/* 339 */           isAbort = PluginFilters.filter("prepareDocForBatchFile", this.m_data.m_workspace, docBinder, this.m_data) == -1;
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 344 */           throw new ServiceException(e);
/*     */         }
/*     */ 
/* 347 */         if (!isAbort)
/*     */         {
/* 349 */           addZoneFields(props);
/* 350 */           this.m_execution.prepareIndexDoc(props, ii);
/* 351 */           removeNonBulkloadFields(props);
/*     */         }
/* 353 */         this.m_data.setCachedObject("indexerInfo", "");
/*     */       }
/*     */     }
/* 356 */     long end = System.currentTimeMillis();
/* 357 */     Report.trace("indexer", "prepareDoc complete duration<" + (end - start) / 1000L + ">", null);
/*     */   }
/*     */ 
/*     */   public void removeNonBulkloadFields(Properties props)
/*     */   {
/* 362 */     Vector nonBulkloadFields = this.m_config.getVector("NonBulkloadFields");
/* 363 */     int size = nonBulkloadFields.size();
/* 364 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 366 */       props.remove(nonBulkloadFields.elementAt(i));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addZoneFields(Properties props)
/*     */   {
/* 372 */     if (!this.m_config.getBoolean("IsSupportZoneSearch", true))
/*     */     {
/* 374 */       return;
/*     */     }
/* 376 */     DocIndexerUtils.addZoneFields(props, this.m_execution.m_collectionDef.m_securityInfos);
/*     */   }
/*     */ 
/*     */   public void executeCustomParametersOverride() {
/* 380 */     DataBinder scriptData = new DataBinder(SharedObjects.getSecureEnvironment());
/* 381 */     scriptData.setLocalData(this.m_data.m_state.m_perBatchOverrides);
/*     */ 
/* 384 */     DynamicHtmlMerger merger = new PageMerger(scriptData, this.m_data);
/* 385 */     this.m_data.setCachedObject("PageMerger", merger);
/*     */     try
/*     */     {
/* 388 */       merger.evaluateResourceInclude(this.m_config.getValue("IndexerParamsOverride"));
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 392 */       Report.trace("indexer", null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void prepareForBatchExecution(Vector list) throws ServiceException
/*     */   {
/* 398 */     this.m_data.m_state.m_perBatchOverrides = new Properties(this.m_data.m_state.m_overrideProps);
/* 399 */     DataBinder collValues = this.m_data.m_state.getCollectionValues();
/* 400 */     DataBinder.mergeHashTables(this.m_data.m_state.m_perBatchOverrides, collValues.getLocalData());
/*     */ 
/* 402 */     this.m_data.putPerBatchProperty("ignoreIndexingActivity", (this.m_ignoreIndexingActivity) ? "1" : "");
/*     */ 
/* 404 */     this.m_data.putPerBatchProperty("batchSize", "" + list.size());
/*     */   }
/*     */ 
/*     */   public void writeBatchFile(Vector list) throws ServiceException
/*     */   {
/* 409 */     this.m_execution.writeBatchFile(list, this.m_docProps);
/*     */   }
/*     */ 
/*     */   public void updateListStateAfterFinish(Vector list)
/*     */   {
/* 415 */     int length = list.size();
/* 416 */     synchronized (this.m_finishedLock)
/*     */     {
/* 418 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 420 */         IndexerInfo info = (IndexerInfo)list.elementAt(i);
/* 421 */         if (info.m_indexStatus == -1)
/*     */         {
/* 423 */           info.m_indexStatus = 1;
/*     */         }
/* 425 */         this.m_finishedList.addElement(info);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean verifyCollection(IndexerCollectionData collectionDesignDef, IndexerCollectionData collectionDef)
/*     */     throws ServiceException
/*     */   {
/* 434 */     return verifyCollectionEx(collectionDesignDef, collectionDef, false);
/*     */   }
/*     */ 
/*     */   public boolean verifyCollectionEx(IndexerCollectionData collectionDesignDef, IndexerCollectionData collectionDef, boolean isRefresh)
/*     */     throws ServiceException
/*     */   {
/* 442 */     if (!this.m_config.getBoolean("UseCollectionVerification", true))
/*     */     {
/* 450 */       return this.m_config.getBoolean("CollectionVerificationDefault", true);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 455 */       this.m_data.m_driver.verifyCollection(collectionDef);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 459 */       if (e.m_errorCode == -16)
/*     */       {
/* 462 */         if (!isRefresh)
/*     */         {
/* 464 */           Report.trace("indexer", null, e);
/*     */         }
/* 466 */         return true;
/*     */       }
/* 468 */       throw e;
/*     */     }
/*     */ 
/* 471 */     return this.m_data.compareConfigurations(collectionDesignDef, collectionDef);
/*     */   }
/*     */ 
/*     */   public void createStyleFile(Properties props, Hashtable infos)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public String findIndexExtension(String format)
/*     */   {
/* 483 */     Vector excpMap = this.m_config.getVector("ExceptionFormatMap");
/* 484 */     Vector fmtMap = this.m_config.getVector("FormatMap");
/* 485 */     if ((format == null) || (fmtMap == null))
/*     */     {
/* 487 */       return null;
/*     */     }
/*     */ 
/* 490 */     String headStr = null;
/* 491 */     String tailStr = null;
/* 492 */     int index = format.indexOf("/");
/* 493 */     if (index >= 0)
/*     */     {
/* 495 */       headStr = format.substring(0, index);
/* 496 */       tailStr = format.substring(index + 1);
/*     */     }
/*     */     else
/*     */     {
/* 500 */       headStr = format;
/*     */     }
/*     */ 
/* 504 */     int excSize = 0;
/* 505 */     if (excpMap != null)
/*     */     {
/* 507 */       excSize = excpMap.size();
/*     */     }
/* 509 */     for (int ii = 0; ii < excSize; ++ii)
/*     */     {
/* 511 */       String formatType = (String)excpMap.elementAt(ii);
/* 512 */       if (((headStr != null) && (headStr.equalsIgnoreCase(formatType))) || ((tailStr != null) && (tailStr.equalsIgnoreCase(formatType))))
/*     */       {
/* 515 */         return null;
/*     */       }
/*     */     }
/*     */ 
/* 519 */     int size = fmtMap.size();
/* 520 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 522 */       String formatType = (String)fmtMap.elementAt(i);
/* 523 */       if (((headStr != null) && (isOfFormatType(headStr, formatType))) || ((tailStr != null) && (isOfFormatType(tailStr, formatType))))
/*     */       {
/* 526 */         return "basic";
/*     */       }
/*     */     }
/*     */ 
/* 530 */     return null;
/*     */   }
/*     */ 
/*     */   protected boolean isOfFormatType(String typeSpec, String formatType)
/*     */   {
/* 535 */     String typeSpecLower = typeSpec.toLowerCase();
/* 536 */     int index = typeSpecLower.indexOf(formatType);
/* 537 */     if (index < 0)
/*     */     {
/* 539 */       return false;
/*     */     }
/*     */ 
/* 544 */     if ((index > 0) && 
/* 546 */       (typeSpec.charAt(index - 1) != '.'))
/*     */     {
/* 548 */       return false;
/*     */     }
/*     */ 
/* 551 */     int typeLength = typeSpec.length();
/* 552 */     int formatLength = formatType.length();
/*     */ 
/* 557 */     return (index + formatLength >= typeLength) || 
/* 555 */       (typeSpec.charAt(index + formatLength) == '.');
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 567 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96895 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.DocIndexerAdaptor
 * JD-Core Version:    0.5.4
 */