/*      */ package intradoc.indexer;
/*      */ 
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.filestore.BaseFileStore;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.shared.AdditionalRenditions;
/*      */ import intradoc.shared.IndexerCollectionData;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class CommonIndexerBulkLoader extends IndexerBulkLoader
/*      */ {
/*      */   protected boolean m_doDelayedNotify;
/*      */   public boolean m_allowIndexRenditions;
/*      */   public Vector m_indexableRenditions;
/*      */   public AdditionalRenditions m_additionalRenditions;
/*      */   public boolean m_allowIndexVault;
/*      */   public boolean m_indexVaultFile;
/*      */   public long m_minFileSize;
/*   44 */   public static HashMap m_indexableFormat = new HashMap();
/*      */ 
/*   48 */   public static HashMap m_convertibleFormats = new HashMap();
/*   49 */   public static HashMap m_conversionHandlers = new HashMap();
/*   50 */   public static HashMap m_indexerExtensionMap = null;
/*      */ 
/*      */   public CommonIndexerBulkLoader()
/*      */   {
/*   37 */     this.m_allowIndexRenditions = false;
/*   38 */     this.m_indexableRenditions = null;
/*   39 */     this.m_additionalRenditions = null;
/*   40 */     this.m_allowIndexVault = false;
/*   41 */     this.m_indexVaultFile = false;
/*      */ 
/*   43 */     this.m_minFileSize = 5L;
/*      */   }
/*      */ 
/*      */   public void prepareUse(String step, IndexerWorkObject data, boolean isRestart)
/*      */     throws ServiceException
/*      */   {
/*   55 */     super.prepareUse(step, data, isRestart);
/*      */ 
/*   57 */     if (this.m_config == null)
/*      */     {
/*   59 */       this.m_config = ((IndexerConfig)data.getCachedObject("IndexerConfig"));
/*      */     }
/*      */ 
/*   62 */     IndexerState state = (IndexerState)data.getCachedObject("IndexerState");
/*   63 */     boolean isDelayNotifyFromConifg = this.m_config.getBoolean("IndexerDelayedNotify", true);
/*   64 */     this.m_doNotify = ((!state.isRebuild()) && (!isDelayNotifyFromConifg));
/*   65 */     this.m_doDelayedNotify = ((!state.isRebuild()) && (isDelayNotifyFromConifg));
/*      */ 
/*   67 */     this.m_allowIndexRenditions = this.m_config.getBoolean("IsAllowIndexRenditions", false);
/*   68 */     this.m_indexableRenditions = this.m_config.getVector("IndexableRenditions");
/*   69 */     this.m_additionalRenditions = ((AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions"));
/*      */ 
/*   71 */     this.m_allowIndexVault = this.m_config.getBoolean("IsAllowIndexVault", false);
/*      */ 
/*   75 */     this.m_indexVaultFile = this.m_config.getBoolean("UseNativeFormatInIndex", false);
/*   76 */     this.m_indexVaultFile = this.m_config.getBoolean("IndexVaultFile", this.m_indexVaultFile);
/*      */ 
/*   78 */     initIndexerExtensionMap();
/*      */   }
/*      */ 
/*      */   public void initIndexerExtensionMap()
/*      */   {
/*   85 */     if (m_indexerExtensionMap != null)
/*      */     {
/*   87 */       return;
/*      */     }
/*      */ 
/*   90 */     String extensionListStr = this.m_config.getValue("TextIndexerFilterExtensionMaps");
/*   91 */     if (extensionListStr == null)
/*      */     {
/*   93 */       DataResultSet drset = this.m_config.getTable("IndexerExtensionMap");
/*   94 */       HashMap map = new HashMap();
/*   95 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/*   97 */         String name = ResultSetUtils.getValue(drset, "iemSource");
/*   98 */         String value = ResultSetUtils.getValue(drset, "iemTarget");
/*   99 */         name = name.toLowerCase();
/*  100 */         value = value.toLowerCase();
/*  101 */         map.put(name, value);
/*  102 */         if (!Report.m_verbose)
/*      */           continue;
/*  104 */         Report.trace("indexer", "Added to IndexerExtensionMap:[" + name + ":" + value + "]", null);
/*      */       }
/*      */ 
/*  108 */       m_indexerExtensionMap = map;
/*      */     }
/*      */     else
/*      */     {
/*  112 */       getTifIndexerExtensionMap(extensionListStr);
/*  113 */       Report.trace("indexer", "Configuration TextIndexerFilterExtensionMaps is deprecated. Please use IndexerExtensionMap", null);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void getTifIndexerExtensionMap(String extensionListStr)
/*      */   {
/*  121 */     Vector extensionList = StringUtils.parseArrayEx(extensionListStr, ',', '^', true);
/*  122 */     int numExtensions = extensionList.size();
/*  123 */     for (int i = 0; i < numExtensions; ++i)
/*      */     {
/*  125 */       String nameValue = (String)extensionList.elementAt(i);
/*  126 */       int index = nameValue.indexOf(":");
/*  127 */       if (index <= 0)
/*      */         continue;
/*  129 */       String name = nameValue.substring(0, index);
/*  130 */       name = name.toLowerCase();
/*      */ 
/*  132 */       String value = nameValue.substring(index + 1);
/*  133 */       value = value.toLowerCase();
/*      */ 
/*  135 */       if ((name.equals("")) && (value.equals("")))
/*      */         continue;
/*  137 */       m_indexerExtensionMap.put(name, value);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Properties loadRecordWebChange(boolean deleteFlag, IndexerInfo info, WebChange change)
/*      */     throws DataException, ServiceException
/*      */   {
/*  147 */     Properties pairs = new Properties();
/*  148 */     String id = get("dID");
/*  149 */     String revClassID = get("dRevClassID");
/*  150 */     String docNameOriginal = get("dDocName");
/*  151 */     String docName = docNameOriginal.toLowerCase();
/*      */ 
/*  153 */     Properties currentRow = this.m_data.m_currentResultSet.getCurrentRowProps();
/*  154 */     IdcDateFormat blDateFormat = this.m_data.m_currentResultSet.getDateFormat();
/*  155 */     if (this.m_loadedProps != null)
/*      */     {
/*  157 */       this.m_loadedProps.put(id, currentRow);
/*      */     }
/*      */ 
/*  161 */     info.m_dID = id;
/*  162 */     info.m_dRevClassID = revClassID;
/*  163 */     info.m_indexKey = docName;
/*  164 */     info.m_isMetaDataOnly = ((this.m_retryIndexing) && (!this.m_isFirstRetry));
/*  165 */     info.m_isDelete = deleteFlag;
/*  166 */     String state = currentRow.getProperty("dReleaseState");
/*  167 */     String status = currentRow.getProperty("dStatus");
/*  168 */     if ((state.equals("U")) && (status.equals("RELEASED")))
/*      */     {
/*  170 */       info.m_isUpdate = true;
/*      */     }
/*      */ 
/*  173 */     DataBinder fileBinder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  174 */     fileBinder.m_blDateFormat = blDateFormat;
/*  175 */     fileBinder.setLocalData(new Properties(currentRow));
/*  176 */     fileBinder.putLocal("dRevClassID", revClassID);
/*  177 */     fileBinder.putLocal("dID", id);
/*  178 */     fileBinder.putLocal("dIndexerState", this.m_finishedFlag);
/*      */ 
/*  180 */     fileBinder.putLocal("RenditionId", "webViewableFile");
/*      */ 
/*  182 */     this.m_data.setCachedObject("Workspace", this.m_data.m_workspace);
/*  183 */     Object dateObj = this.m_data.getLocaleResource(3);
/*  184 */     this.m_data.setCachedObject("UserDateFormat", blDateFormat);
/*  185 */     IdcFileDescriptor descriptor = this.m_fileStore.createDescriptor(fileBinder, null, this.m_data);
/*  186 */     Map storageData = null;
/*  187 */     boolean isInTransaction = false;
/*      */     try
/*      */     {
/*  192 */       this.m_data.m_workspace.beginTran();
/*  193 */       this.m_data.m_workspace.execute("UrevClassesLockBydRevClassID", fileBinder);
/*  194 */       isInTransaction = true;
/*      */       String existsString;
/*  195 */       if (!deleteFlag)
/*      */       {
/*      */         try
/*      */         {
/*  199 */           storageData = this.m_fileStore.getStorageData(descriptor, null, this.m_data);
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*  203 */           throw new ServiceException(e);
/*      */         }
/*  205 */         existsString = (String)storageData.get("fileExists");
/*      */         IndexerState indexerState;
/*  206 */         if (!StringUtils.convertToBool(existsString, false))
/*      */         {
/*  208 */           fileBinder.putLocal("dReleaseState", "R");
/*  209 */           descriptor = this.m_fileStore.createDescriptor(fileBinder, null, this.m_data);
/*      */           try
/*      */           {
/*  212 */             storageData = this.m_fileStore.getStorageData(descriptor, null, this.m_data);
/*      */           }
/*      */           catch (IOException e)
/*      */           {
/*  216 */             throw new ServiceException(e);
/*      */           }
/*  218 */           existsString = (String)storageData.get("fileExists");
/*  219 */           if (!StringUtils.convertToBool(existsString, false))
/*      */           {
/*  221 */             handleLoadError("syFileMissing", null, info, change);
/*      */ 
/*  226 */             indexerState = (IndexerState)this.m_data.getCachedObject("IndexerState");
/*  227 */             Boolean isRebuild = Boolean.valueOf(indexerState.isRebuild());
/*      */ 
/*  230 */             if ((isRebuild.booleanValue()) && (status.equals("DONE")))
/*      */             {
/*  235 */               if (indexerState.failedDocRevClassIds == null)
/*      */               {
/*  237 */                 indexerState.failedDocRevClassIds = new ArrayList();
/*      */               }
/*  239 */               indexerState.failedDocRevClassIds.add(change.m_dRevClassID);
/*      */             }
/*      */ 
/*  242 */             descriptor = null;
/*      */           }
/*      */         }
/*  245 */         if (descriptor == null)
/*      */         {
/*  247 */           handleLoadError("csFailedToFindIndexableFile", null, info, change);
/*  248 */           indexerState = null;
/*      */           return indexerState;
/*      */         }
/*  251 */         long size = NumberUtils.parseLong((String)storageData.get("fileSize"), 0L);
/*  252 */         info.m_size = size;
/*  253 */         pairs.put("WebFileSize", "" + size);
/*      */       }
/*      */       else
/*      */       {
/*  258 */         pairs.put("dID", info.m_dID);
/*      */       }
/*      */ 
/*  261 */       if (!notifyOfRelease(fileBinder, info, descriptor, change))
/*      */       {
/*  263 */         existsString = null;
/*      */         return existsString;
/*      */       }
/*  266 */       this.m_data.m_workspace.commitTran();
/*  267 */       isInTransaction = false;
/*      */ 
/*  269 */       String processingState = get("dProcessingState");
/*  270 */       if (!info.m_isMetaDataOnly)
/*      */       {
/*  272 */         String msg = get("dMessage");
/*  273 */         if (((processingState.equals("M")) && (!msg.startsWith("!csTextConversionFailed"))) || (processingState.equals("P")))
/*      */         {
/*  276 */           if (processingState.equals("M"))
/*      */           {
/*  278 */             Report.trace("indexer", "Not submitting document " + info.m_indexKey + " to indexer because an error occurred " + "the last time it was indexed.", null);
/*      */           }
/*      */ 
/*  282 */           info.m_isMetaDataOnly = true;
/*      */         }
/*      */       }
/*      */ 
/*  286 */       if (!deleteFlag)
/*      */       {
/*  289 */         DataResultSet drset = this.m_data.m_currentResultSet;
/*  290 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*      */         {
/*  292 */           boolean isPrimary = StringUtils.convertToBool(ResultSetUtils.getValue(drset, "dIsPrimary"), false);
/*      */ 
/*  294 */           boolean isWeb = StringUtils.convertToBool(ResultSetUtils.getValue(drset, "dIsWebFormat"), false);
/*      */           Hashtable fieldInfos;
/*      */           Enumeration e;
/*  296 */           if (isPrimary)
/*      */           {
/*  298 */             computeAndSetVaultFileSize(pairs);
/*  299 */             if (this.m_allowIndexVault)
/*      */             {
/*  301 */               isWeb = true;
/*      */             }
/*      */ 
/*  304 */             fieldInfos = this.m_data.m_collectionDef.m_fieldInfos;
/*  305 */             for (e = fieldInfos.keys(); e.hasMoreElements(); )
/*      */             {
/*  307 */               String name = (String)e.nextElement();
/*  308 */               FieldInfo fieldInfo = (FieldInfo)fieldInfos.get(name);
/*  309 */               String value = getValue(fieldInfo.m_name, true);
/*  310 */               if (value == null)
/*      */               {
/*      */                 continue;
/*      */               }
/*      */ 
/*  315 */               pairs.put(name, value);
/*      */             }
/*      */           }
/*  318 */           if (isWeb)
/*      */           {
/*  320 */             String webFormat = null;
/*  321 */             if (!info.m_isMetaDataOnly)
/*      */             {
/*  323 */               webFormat = ResultSetUtils.getValue(drset, "dFormat");
/*  324 */               if ((webFormat == null) || (webFormat.trim().length() == 0))
/*      */               {
/*  326 */                 webFormat = "application/unknown";
/*      */               }
/*  328 */               if (SystemUtils.m_verbose)
/*      */               {
/*  330 */                 Report.debug("indexer", "webFormat is " + webFormat, null);
/*      */               }
/*      */             }
/*  333 */             if (webFormat == null)
/*      */             {
/*  335 */               pairs.put("webFormat", "");
/*      */             }
/*      */             else
/*      */             {
/*  339 */               pairs.put("webFormat", webFormat);
/*      */             }
/*      */           }
/*  342 */           if ((isPrimary) || (isWeb))
/*      */             continue;
/*  344 */           String val = ResultSetUtils.getValue(drset, "dFormat");
/*  345 */           pairs.put("AlternateFormat", val);
/*      */ 
/*  347 */           val = ResultSetUtils.getValue(drset, "dFileSize");
/*  348 */           pairs.put("AlternateFileSize", val);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  354 */       prepareIndexFile(pairs, info, fileBinder);
/*      */ 
/*  356 */       if (SystemUtils.m_verbose)
/*      */       {
/*  358 */         Report.debug("indexer", "indexWebFile: " + info.m_indexWebFile + " m_retryIndexing: " + this.m_retryIndexing + " m_isFirstRetry: " + this.m_isFirstRetry, null);
/*      */       }
/*      */ 
/*  363 */       if ((!info.m_isDelete) && 
/*  365 */         (((!info.m_indexWebFile) || (info.m_isMetaDataOnly))) && (this.m_retryIndexing) && (this.m_isFirstRetry))
/*      */       {
/*  372 */         if (change != null)
/*      */         {
/*  374 */           this.m_changes.setChangeType(change, "!");
/*      */         }
/*      */ 
/*  377 */         pairs.put("SkipRevision", "true");
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/*  383 */       this.m_data.setCachedObject("UserDateFormat", dateObj);
/*  384 */       if (isInTransaction)
/*      */       {
/*  386 */         this.m_data.m_workspace.rollbackTran();
/*      */       }
/*      */     }
/*  389 */     return pairs;
/*      */   }
/*      */ 
/*      */   public boolean notifyOfRelease(DataBinder fileBinder, IndexerInfo info, IdcFileDescriptor descriptor, WebChange change)
/*      */     throws DataException, ServiceException
/*      */   {
/*  398 */     Properties unreleaseEventData = null;
/*  399 */     IdcFileDescriptor unreleaseDescriptor = null;
/*  400 */     Properties releaseEventData = null;
/*  401 */     this.m_data.setCachedObject("unreleasedData", "");
/*      */ 
/*  403 */     if (info.m_isDelete)
/*      */     {
/*  405 */       releaseEventData = new Properties();
/*  406 */       releaseEventData.put("EventType", "event_unreleased");
/*      */     }
/*      */     else
/*      */     {
/*  412 */       Map capabilities = this.m_fileStore.getCapabilities(null);
/*  413 */       Hashtable storageClasses = (Hashtable)capabilities.get("storage_classes");
/*      */ 
/*  415 */       String storageRule = descriptor.getProperty("StorageRule");
/*      */ 
/*  417 */       ResultSet rset = this.m_data.m_workspace.createResultSet("INDEXER-LATEST-RELEASED", fileBinder);
/*      */ 
/*  419 */       FieldInfo fieldInfo = new FieldInfo();
/*  420 */       rset.getFieldInfo("dID", fieldInfo);
/*  421 */       if (fileBinder.m_blDateFormat != null)
/*      */       {
/*  423 */         rset.setDateFormat(fileBinder.m_blDateFormat);
/*      */       }
/*  425 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*      */       {
/*  427 */         String pendingID = rset.getStringValue(fieldInfo.m_index);
/*  428 */         if (info.m_dID.equals(pendingID))
/*      */           continue;
/*  430 */         unreleaseEventData = new Properties();
/*  431 */         unreleaseEventData.put("EventType", "event_unreleased");
/*      */ 
/*  433 */         DataResultSet tmpset = new DataResultSet();
/*  434 */         tmpset.copy(rset, 1);
/*  435 */         rset = null;
/*      */ 
/*  437 */         DataBinder unreleaseParams = new DataBinder(SharedObjects.getSecureEnvironment());
/*      */ 
/*  439 */         unreleaseParams.setLocalData(tmpset.getCurrentRowProps());
/*  440 */         unreleaseParams.m_blDateFormat = this.m_data.m_currentResultSet.getDateFormat();
/*      */ 
/*  443 */         String type = ((BaseFileStore)this.m_fileStore).computeStorageKey("web", storageRule);
/*      */ 
/*  445 */         if ((storageClasses != null) && (storageClasses.get(type) != null))
/*      */         {
/*  448 */           unreleaseParams.putLocal("RenditionId", "webViewableFile");
/*      */         }
/*      */         else
/*      */         {
/*  453 */           unreleaseParams.putLocal("RenditionId", "primaryFile");
/*      */         }
/*      */ 
/*  459 */         String webExtension = unreleaseParams.get("dWebExtension");
/*  460 */         unreleaseParams.putLocal("dExtension", webExtension);
/*  461 */         unreleaseDescriptor = this.m_fileStore.createDescriptor(unreleaseParams, null, this.m_data);
/*      */ 
/*  463 */         this.m_data.setCachedObject("unreleasedData", unreleaseParams);
/*  464 */         break;
/*      */       }
/*      */ 
/*  468 */       releaseEventData = new Properties();
/*  469 */       releaseEventData.put("EventType", "event_released");
/*      */     }
/*      */ 
/*  472 */     int numRetries = 2;
/*  473 */     boolean revisionNoLongerToBeIndexed = false;
/*  474 */     for (int i = 0; (i < numRetries) && (!revisionNoLongerToBeIndexed); ++i)
/*      */     {
/*  476 */       if (i > 0)
/*      */       {
/*  478 */         Report.trace("indexer", "Retrying access to webviewable, validating current database state of revision", null);
/*      */ 
/*  480 */         if (!isStillValidIndexingDoc(fileBinder))
/*      */         {
/*  482 */           Report.trace("indexer", "Document no longer has a database state that indicates that it is part of this indexing cycle", null);
/*      */ 
/*  484 */           revisionNoLongerToBeIndexed = true;
/*  485 */           break;
/*      */         }
/*      */       }
/*      */       try
/*      */       {
/*  490 */         if (unreleaseEventData != null)
/*      */         {
/*  492 */           this.m_fileStore.notifyOfEvent(unreleaseDescriptor, unreleaseEventData, this.m_data);
/*      */         }
/*  494 */         if (releaseEventData != null)
/*      */         {
/*  496 */           this.m_fileStore.notifyOfEvent(descriptor, releaseEventData, this.m_data);
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  502 */         if ((i == numRetries - 1) && (e instanceof ServiceException))
/*      */         {
/*  504 */           handleLoadError("csFailedToFindIndexableFile", e, info, change);
/*  505 */           revisionNoLongerToBeIndexed = true;
/*      */         }
/*  507 */         Report.trace("indexer", null, e);
/*      */       }
/*      */     }
/*  510 */     if (revisionNoLongerToBeIndexed)
/*      */     {
/*  512 */       return false;
/*      */     }
/*      */ 
/*  520 */     String releaseState = get("dReleaseState");
/*  521 */     if (releaseState.equals("R"))
/*      */     {
/*  523 */       fileBinder.putLocal("dReleaseState", "I");
/*  524 */       this.m_data.m_workspace.addBatch("UreleaseState", fileBinder);
/*  525 */       this.m_data.m_workspace.addBatch("INDEXER-Y-TO-O", fileBinder);
/*  526 */       this.m_data.m_workspace.addBatch("INDEXER-U-TO-R", fileBinder);
/*      */ 
/*  528 */       this.m_data.m_workspace.executeBatch();
/*      */     }
/*  530 */     return true;
/*      */   }
/*      */ 
/*      */   protected boolean isStillValidIndexingDoc(Parameters parameters)
/*      */     throws DataException, ServiceException
/*      */   {
/*  543 */     boolean inTran = false;
/*  544 */     boolean isStillValid = false;
/*      */     try
/*      */     {
/*  555 */       ResultSet stateInfoRset = this.m_data.m_workspace.createResultSet("QindexerStateInfoByID", parameters);
/*  556 */       if (!stateInfoRset.isEmpty())
/*      */       {
/*  558 */         String indexerState = stateInfoRset.getStringValueByName("dIndexerState");
/*  559 */         String releaseState = stateInfoRset.getStringValueByName("dStatus");
/*  560 */         if ((indexerState.trim().length() > 0) && (((releaseState.equalsIgnoreCase("DONE")) || (releaseState.equalsIgnoreCase("RELEASED")))))
/*      */         {
/*  563 */           isStillValid = true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/*  573 */       if (!inTran);
/*      */     }
/*      */ 
/*  579 */     return isStillValid;
/*      */   }
/*      */ 
/*      */   public void handleLoadError(String msg, Throwable e, IndexerInfo info, WebChange change)
/*      */     throws ServiceException, DataException
/*      */   {
/*  590 */     String revLabel = get("dRevLabel");
/*  591 */     String logMessage = LocaleUtils.encodeMessage(msg, null, new Object[] { info.m_indexKey, revLabel, info.m_dID });
/*      */ 
/*  593 */     Report.error(null, logMessage, e);
/*      */ 
/*  595 */     String eMsg = null;
/*  596 */     if (e != null)
/*      */     {
/*  598 */       eMsg = e.getMessage();
/*      */     }
/*  600 */     String traceMessage = LocaleUtils.encodeMessage("csFailedToFindIndexableFile", eMsg, new Object[] { info.m_indexKey, revLabel, info.m_dID });
/*      */ 
/*  602 */     Report.trace("indexer", LocaleResources.localizeMessage(traceMessage, null), null);
/*      */ 
/*  605 */     this.m_state.m_errCount += 1;
/*  606 */     this.m_state.m_totalErrCount += 1;
/*  607 */     this.m_state.m_cumTotalErrCount += 1;
/*      */ 
/*  609 */     String docMessage = LocaleUtils.encodeMessage("!csIndexingFailed", eMsg);
/*  610 */     resetDocumentToGenWWW(info.m_dID, docMessage, change);
/*      */ 
/*  612 */     if (this.m_state.m_errCount >= this.m_data.m_maxErrors)
/*      */     {
/*  614 */       throw new ServiceException("!csIndexerRenameFailedAbort");
/*      */     }
/*  616 */     this.m_changes.setChangeType(change, "F");
/*      */   }
/*      */ 
/*      */   public void prepareIndexingEngineValues(Properties props, String indexKey, String filePath, String URL)
/*      */   {
/*  623 */     String keyName = "VdkVgwKey";
/*  624 */     if (this.m_config != null)
/*      */     {
/*  626 */       keyName = this.m_config.getValue("IndexerKeyName");
/*      */     }
/*  628 */     if (props.get(keyName) == null)
/*      */     {
/*  630 */       props.put(keyName, indexKey);
/*      */     }
/*  632 */     props.put("URL", URL);
/*  633 */     props.put("DOC_FN", filePath);
/*      */ 
/*  635 */     String format = "";
/*  636 */     String noTextPath = this.m_config.getConfigValue("IndexerNoTextFile");
/*  637 */     if ((filePath.length() > 0) && (((noTextPath == null) || (noTextPath.length() == 0) || (!filePath.endsWith(noTextPath)))))
/*      */     {
/*  640 */       int index = filePath.lastIndexOf(46);
/*  641 */       if (index >= 0)
/*      */       {
/*  643 */         format = filePath.substring(index + 1);
/*      */       }
/*      */     }
/*  646 */     props.put("dFullTextFormat", format);
/*      */   }
/*      */ 
/*      */   public void cleanUp(IndexerWorkObject data)
/*      */     throws ServiceException
/*      */   {
/*  652 */     super.cleanUp(data);
/*  653 */     if ((!this.m_doDelayedNotify) || 
/*  655 */       (!this.m_hasReleaseDocumentChange))
/*      */       return;
/*  657 */     notifyReleasedDocumentsChanged();
/*  658 */     this.m_hasReleaseDocumentChange = false;
/*      */   }
/*      */ 
/*      */   public void prepareIndexFile(Properties props, IndexerInfo ii, DataBinder fileBinder)
/*      */     throws ServiceException, DataException
/*      */   {
/*  677 */     Properties tmpProps = new Properties(props);
/*  678 */     if (ii.m_isDelete)
/*      */     {
/*  680 */       setIndexFileInfo(ii, props, tmpProps);
/*  681 */       return;
/*      */     }
/*      */ 
/*  684 */     String vaultFormat = props.getProperty("dFormat");
/*  685 */     String webFormat = (String)props.get("webFormat");
/*  686 */     if (ii.m_isMetaDataOnly)
/*      */     {
/*  689 */       String[] attribs = computeIndexableFileAttributes("webviewable", props, ii, -1, webFormat, false, false, null, fileBinder);
/*      */ 
/*  691 */       if (attribs[2] != null)
/*      */       {
/*  693 */         props.put("URL", attribs[2]);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  700 */       ii.m_indexWebFile = true;
/*  701 */       DataResultSet drset = new DataResultSet(new String[] { "format", "type", "filePath", "fileSize", "url", "allowDirectIndex", "allowConversion", "conversionHandler", "useMap", "mapExtension" });
/*      */       try
/*      */       {
/*  707 */         if (this.m_indexVaultFile)
/*      */         {
/*  709 */           addIndexableVaultFileToList(props, ii, vaultFormat, drset, fileBinder);
/*      */         }
/*      */ 
/*  713 */         String[] fileAttrib = updateFormatResultSet(drset, props, ii, webFormat, "webviewable", -1, false, fileBinder);
/*      */ 
/*  715 */         if (fileAttrib[2] != null)
/*      */         {
/*  717 */           props.put("URL", fileAttrib[2]);
/*      */         }
/*      */ 
/*  720 */         if ((this.m_allowIndexVault) && (!this.m_indexVaultFile))
/*      */         {
/*  723 */           addIndexableVaultFileToList(props, ii, vaultFormat, drset, fileBinder);
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  729 */         Report.trace("indexer", "Error occurred while computing allowable format in webviewable.", e);
/*      */       }
/*      */ 
/*  734 */       if (this.m_allowIndexRenditions)
/*      */       {
/*  736 */         int numRenditions = AdditionalRenditions.m_maxNum;
/*  737 */         for (int i = 0; i < numRenditions; ++i)
/*      */         {
/*  739 */           String keyRen = "dRendition" + (i + 1);
/*  740 */           String tmpRen = (String)props.get(keyRen);
/*      */ 
/*  743 */           if ((tmpRen == null) || (tmpRen.length() <= 0) || ((this.m_indexableRenditions != null) && (!this.m_indexableRenditions.contains(tmpRen)))) {
/*      */             continue;
/*      */           }
/*      */           try
/*      */           {
/*  748 */             String extension = this.m_additionalRenditions.getExtension(tmpRen);
/*  749 */             updateFormatResultSet(drset, props, ii, extension, tmpRen, i + 1, false, fileBinder);
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/*  754 */             Report.trace("indexer", "Error occurred while computing allowable format in rendition" + (i + 1) + ": " + tmpRen + ".", e);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  765 */       if (drset.getNumRows() > 0)
/*      */       {
/*  767 */         ExecutionContextAdaptor adaptor = new ExecutionContextAdaptor();
/*  768 */         adaptor.setParentContext(this.m_data);
/*      */ 
/*  770 */         DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  771 */         binder.setLocalData(tmpProps);
/*  772 */         binder.addResultSet("DocIndexableFormats", drset);
/*      */ 
/*  774 */         PageMerger pm = new PageMerger();
/*  775 */         pm.initImplement(binder, adaptor);
/*      */ 
/*  777 */         tmpProps.put("MaxIndexableFileSize", this.m_config.getValue("MaxIndexableFileSize"));
/*  778 */         tmpProps.put("MinIndexableFileSize", this.m_config.getValue("MinIndexableFileSize"));
/*      */ 
/*  780 */         String resInc = this.m_config.getValue("IndexableFormatResIncName");
/*      */         try
/*      */         {
/*  783 */           pm.evaluateResourceInclude(resInc);
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*  787 */           Report.trace("indexer", null, e);
/*      */         }
/*      */         finally
/*      */         {
/*  791 */           pm.releaseAllTemporary();
/*      */         }
/*      */       }
/*      */     }
/*  795 */     setIndexFileInfo(ii, props, tmpProps);
/*      */   }
/*      */ 
/*      */   protected String[] addIndexableVaultFileToList(Properties props, IndexerInfo ii, String format, DataResultSet drset, DataBinder fileBinder)
/*      */   {
/*      */     try
/*      */     {
/*  813 */       return updateFormatResultSet(drset, props, ii, format, "vault", -1, true, fileBinder);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  817 */       Report.trace("indexer", "Error occurred while computing allowable format in vault.", e);
/*      */     }
/*      */ 
/*  820 */     return null;
/*      */   }
/*      */ 
/*      */   protected String[] updateFormatResultSet(DataResultSet drset, Properties props, IndexerInfo ii, String format, String formatType, int renditionCount, boolean isVault, DataBinder fileBinder)
/*      */     throws ServiceException, DataException
/*      */   {
/*  828 */     Map argResults = new HashMap();
/*  829 */     String[] fileAttrib = computeIndexableFileAttributes(formatType, props, ii, renditionCount, format, true, isVault, argResults, fileBinder);
/*      */ 
/*  833 */     boolean allowDirectIndex = StringUtils.convertToBool((String)argResults.get("allowDirectIndex"), false);
/*      */ 
/*  835 */     boolean isConversionAllowed = StringUtils.convertToBool((String)argResults.get("isConversionAllowed"), false);
/*      */ 
/*  837 */     boolean useMap = StringUtils.convertToBool((String)argResults.get("useMap"), false);
/*      */ 
/*  839 */     String mapFormat = (String)argResults.get("mapFormat");
/*      */ 
/*  841 */     if ((allowDirectIndex) || (isConversionAllowed) || (useMap))
/*      */     {
/*  844 */       Vector row = new IdcVector();
/*  845 */       row.add(format);
/*  846 */       row.add(formatType);
/*  847 */       row.add(fileAttrib[0]);
/*  848 */       row.add(fileAttrib[1]);
/*  849 */       row.add(fileAttrib[2]);
/*  850 */       row.add("" + allowDirectIndex);
/*  851 */       row.add("" + isConversionAllowed);
/*      */ 
/*  853 */       String conversionHandler = "";
/*  854 */       if (isConversionAllowed)
/*      */       {
/*  856 */         conversionHandler = getConversionHandler(format, props);
/*      */       }
/*  858 */       row.add(conversionHandler);
/*  859 */       row.add("" + useMap);
/*  860 */       row.add(mapFormat);
/*  861 */       drset.addRow(row);
/*      */ 
/*  863 */       if (Report.m_verbose)
/*      */       {
/*  865 */         Report.trace("indexer", "Added new item for indexing consideration. [format:" + format + ", formatType:" + formatType + ", filePath:" + fileAttrib[0] + ", fileSize:" + fileAttrib[1] + ", url:" + fileAttrib[2] + ",allowDirectIndex:" + allowDirectIndex + ", allowConversion:" + isConversionAllowed + ", conversionHandler:" + conversionHandler + ", useMap:" + useMap + ", mapExtension:" + mapFormat, null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  872 */     return fileAttrib;
/*      */   }
/*      */ 
/*      */   protected String[] computeIndexableFileAttributes(String formatType, Properties props, IndexerInfo ii, int renditionCount, String format, boolean isAllowMap, boolean isVault, Map argResults, DataBinder fileBinder)
/*      */     throws ServiceException, DataException
/*      */   {
/*  898 */     if (Report.m_verbose)
/*      */     {
/*  900 */       Report.trace("indexer", "computeIndexableFileAttributes(); item:" + ii.m_indexKey + ",formatType:" + formatType + ",renditionCount:" + renditionCount + ",format:" + format + ",isAllowMap:" + isAllowMap + ",isVault:" + isVault, null);
/*      */     }
/*      */ 
/*  907 */     boolean isUpdateAndFileNotRequired = ii.m_isUpdate;
/*  908 */     if (isUpdateAndFileNotRequired)
/*      */     {
/*  910 */       isUpdateAndFileNotRequired = !this.m_config.getBoolean("UpdateNeedsSourceFile", true);
/*      */     }
/*      */ 
/*  913 */     boolean allowDirectIndex = isFormatIndexAllowed(format);
/*  914 */     boolean isConversionAllowed = false;
/*  915 */     if ((!allowDirectIndex) && (!isUpdateAndFileNotRequired))
/*      */     {
/*  917 */       isConversionAllowed = isConversionAllowed(format);
/*      */     }
/*  919 */     if (argResults != null)
/*      */     {
/*  921 */       argResults.put("allowDirectIndex", "" + allowDirectIndex);
/*  922 */       argResults.put("isConversionAllowed", "" + isConversionAllowed);
/*      */     }
/*      */ 
/*  925 */     boolean toIndex = (!isVault) || (allowDirectIndex) || (isConversionAllowed);
/*  926 */     if ((!toIndex) && (!ii.m_isMetaDataOnly))
/*      */     {
/*  928 */       return null;
/*      */     }
/*      */ 
/*  931 */     String rendition = "";
/*  932 */     String[] fileAttrib = new String[3];
/*  933 */     boolean hasDistinctURL = true;
/*  934 */     if (formatType.equals("webviewable"))
/*      */     {
/*  936 */       rendition = "webViewableFile";
/*      */     }
/*  938 */     else if (formatType.equals("vault"))
/*      */     {
/*  940 */       rendition = "primaryFile";
/*      */     }
/*      */     else
/*      */     {
/*  944 */       rendition = "rendition:" + formatType;
/*  945 */       hasDistinctURL = false;
/*      */     }
/*      */ 
/*  948 */     ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/*  949 */     cxt.setParentContext(this.m_data);
/*      */     try
/*      */     {
/*  952 */       fileBinder.putLocal("RenditionId", rendition);
/*      */ 
/*  954 */       String state = fileBinder.getLocal("dReleaseState");
/*  955 */       if ((state != null) && (state.equals("O")))
/*      */       {
/*  959 */         fileBinder.putLocal("dReleaseState", "Y");
/*      */       }
/*  961 */       IdcFileDescriptor desc = this.m_fileStore.createDescriptor(fileBinder, null, cxt);
/*  962 */       Map storageData = this.m_fileStore.getStorageData(desc, null, cxt);
/*      */ 
/*  964 */       String ext = (String)storageData.get("dExtension");
/*  965 */       boolean useMap = false;
/*  966 */       if ((isAllowMap) && (!isUpdateAndFileNotRequired))
/*      */       {
/*  968 */         String mapFormat = "";
/*  969 */         if (ext != null)
/*      */         {
/*  971 */           mapFormat = (String)m_indexerExtensionMap.get(ext.toLowerCase());
/*  972 */           useMap = mapFormat != null;
/*      */         }
/*  974 */         if (argResults != null)
/*      */         {
/*  976 */           argResults.put("mapFormat", mapFormat);
/*  977 */           argResults.put("useMap", "" + useMap);
/*      */         }
/*      */       }
/*      */ 
/*  981 */       Map args = new HashMap();
/*  982 */       String path = "";
/*  983 */       if ((!ii.m_isMetaDataOnly) && (((allowDirectIndex) || (isConversionAllowed) || (useMap))) && (!isUpdateAndFileNotRequired))
/*      */       {
/*  986 */         path = this.m_fileStore.getFilesystemPathWithArgs(desc, args, cxt);
/*      */       }
/*  988 */       fileAttrib[0] = path;
/*  989 */       fileAttrib[1] = ((String)storageData.get("fileSize"));
/*  990 */       fileAttrib[2] = "";
/*  991 */       if (hasDistinctURL)
/*      */       {
/*  993 */         args.put("useAbsolute", "0");
/*  994 */         fileAttrib[2] = this.m_fileStore.getClientURL(desc, null, args, cxt);
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  999 */       String msg = LocaleUtils.encodeMessage("csIndexerErrorGetFileAttrib", null, e.getMessage());
/*      */ 
/* 1001 */       throw new DataException(msg, e);
/*      */     }
/*      */ 
/* 1004 */     Object[] params = { rendition, fileAttrib, this.m_fileStore };
/* 1005 */     cxt.setCachedObject("computeIndexableFileAttributes:parameters", params);
/* 1006 */     DataBinder binder = new DataBinder();
/* 1007 */     binder.setLocalData(props);
/* 1008 */     PluginFilters.filter("postComputeIndexableFileAttributes", this.m_data.m_workspace, binder, cxt);
/* 1009 */     return fileAttrib;
/*      */   }
/*      */ 
/*      */   protected void setIndexFileInfo(IndexerInfo ii, Properties target, Properties src)
/*      */   {
/* 1014 */     Vector usedConversionFormatFields = this.m_config.getVector("ConversionFormatFieldNames");
/* 1015 */     int size = usedConversionFormatFields.size();
/* 1016 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1018 */       String fieldName = (String)usedConversionFormatFields.elementAt(i);
/* 1019 */       String value = src.getProperty(fieldName);
/* 1020 */       if (value == null)
/*      */         continue;
/* 1022 */       target.put(fieldName, value);
/*      */     }
/*      */ 
/* 1026 */     String filePath = src.getProperty("indexableFilePath");
/* 1027 */     if ((filePath == null) || (filePath.length() == 0))
/*      */     {
/* 1029 */       filePath = "";
/*      */ 
/* 1031 */       ii.m_indexWebFile = false;
/*      */     }
/*      */ 
/* 1034 */     if (!ii.m_indexWebFile)
/*      */     {
/* 1036 */       filePath = this.m_config.getValue("IndexerNoTextFile");
/* 1037 */       if (filePath == null)
/*      */       {
/* 1039 */         filePath = "";
/*      */       }
/* 1041 */       size = NumberUtils.parseInteger(this.m_config.getValue("NoTextFileSize"), 0);
/* 1042 */       ii.m_size = size;
/*      */     }
/*      */     else
/*      */     {
/* 1046 */       String value = src.getProperty("indexableFileSize");
/* 1047 */       if (value != null)
/*      */       {
/* 1049 */         ii.m_size = NumberUtils.parseLong(value, ii.m_size);
/*      */       }
/* 1051 */       else if (SystemUtils.m_verbose)
/*      */       {
/* 1053 */         Report.debug("indexer", "indexFileSize is not set", null);
/*      */       }
/*      */     }
/*      */ 
/* 1057 */     String url = src.getProperty("indexableFileURL");
/* 1058 */     if (url == null)
/*      */     {
/* 1060 */       url = src.getProperty("URL");
/*      */ 
/* 1062 */       if (url == null)
/*      */       {
/* 1064 */         url = "";
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1069 */     prepareIndexingEngineValues(target, ii.m_indexKey, filePath, url);
/*      */   }
/*      */ 
/*      */   protected String getConversionHandler(String webFormat, Properties prop)
/*      */   {
/* 1074 */     String conversionHandler = null;
/* 1075 */     webFormat = webFormat.toLowerCase();
/* 1076 */     conversionHandler = (String)m_conversionHandlers.get(webFormat);
/* 1077 */     if (conversionHandler == null)
/*      */     {
/* 1079 */       Properties tmpProps = new Properties(prop);
/* 1080 */       tmpProps.put("webFormat", webFormat);
/* 1081 */       DataBinder binder = new DataBinder();
/* 1082 */       binder.setLocalData(tmpProps);
/*      */ 
/* 1084 */       conversionHandler = "TextIndexerFilter";
/* 1085 */       tmpProps.setProperty("conversionHandler", conversionHandler);
/*      */       try
/*      */       {
/* 1088 */         PluginFilters.filter("getConversionHandlerNameFilter", this.m_data.m_workspace, binder, this.m_data);
/*      */ 
/* 1090 */         conversionHandler = tmpProps.getProperty("conversionHandler");
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1094 */         Report.trace("indexer", null, e);
/*      */       }
/* 1096 */       if (conversionHandler == null)
/*      */       {
/* 1098 */         conversionHandler = "";
/*      */       }
/* 1100 */       m_conversionHandlers.put(webFormat, conversionHandler);
/*      */     }
/* 1102 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1104 */       Report.debug("indexer", "Conversion handler for format '" + webFormat + "' is '" + conversionHandler + "'", null);
/*      */     }
/* 1106 */     return conversionHandler;
/*      */   }
/*      */ 
/*      */   protected boolean isConversionAllowed(String format)
/*      */   {
/* 1111 */     if ((format == null) || (format.length() == 0) || (this.m_config.getBoolean("DisableIndexerConversionHandling", false)))
/*      */     {
/* 1114 */       return false;
/*      */     }
/* 1116 */     format = format.toLowerCase();
/* 1117 */     Boolean isAllowed = (Boolean)m_convertibleFormats.get(format);
/* 1118 */     if (isAllowed == null)
/*      */     {
/* 1120 */       isAllowed = isFormatAllowed(format, "ConvertibleFormatTable", "cftFormatList", "TextIndexerFilterFormats", null);
/*      */ 
/* 1122 */       m_convertibleFormats.put(format, isAllowed);
/*      */     }
/* 1124 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1126 */       Report.debug("indexer", "Conversion allowed: " + isAllowed, null);
/*      */     }
/* 1128 */     return isAllowed.booleanValue();
/*      */   }
/*      */ 
/*      */   protected boolean isFormatIndexAllowed(String format)
/*      */   {
/* 1133 */     if ((format == null) || (format.length() == 0))
/*      */     {
/* 1135 */       return false;
/*      */     }
/* 1137 */     format = format.toLowerCase();
/* 1138 */     Boolean isAllowed = (Boolean)m_indexableFormat.get(format);
/* 1139 */     if (isAllowed == null)
/*      */     {
/* 1141 */       isAllowed = isFormatAllowed(format, "IndexableFormatTable", "iftFormatList", "FormatMap", "ExceptionFormatMap");
/*      */ 
/* 1143 */       m_indexableFormat.put(format, isAllowed);
/*      */     }
/* 1145 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1147 */       Report.debug("indexer", "Direct indexing allowed: " + isAllowed, null);
/*      */     }
/* 1149 */     return isAllowed.booleanValue();
/*      */   }
/*      */ 
/*      */   protected Boolean isFormatAllowed(String format, String formatTableName, String formatsColumnName, String formatMapName, String exceptionFormatMapName)
/*      */   {
/* 1155 */     Boolean isAllowed = Boolean.FALSE;
/* 1156 */     String exceptionMap = null;
/* 1157 */     if (exceptionFormatMapName != null)
/*      */     {
/* 1159 */       exceptionMap = this.m_config.getValue(exceptionFormatMapName);
/*      */     }
/* 1161 */     if ((exceptionMap == null) || (!formatMatched(format, exceptionMap)))
/*      */     {
/* 1163 */       String formatMap = this.m_config.getValue(formatMapName);
/* 1164 */       if (formatMap != null)
/*      */       {
/* 1167 */         if (formatMatched(format, formatMap))
/*      */         {
/* 1169 */           isAllowed = Boolean.TRUE;
/*      */         }
/* 1171 */         Report.trace("indexer", "Using deprecated configuration variable: " + formatMapName + ". Please use " + formatTableName + ".", null);
/*      */       }
/*      */       else
/*      */       {
/* 1176 */         DataResultSet drset = this.m_config.getTable(formatTableName);
/* 1177 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*      */         {
/* 1179 */           String indexableFormats = drset.getStringValueByName(formatsColumnName);
/* 1180 */           if (!formatMatched(format, indexableFormats))
/*      */             continue;
/* 1182 */           isAllowed = Boolean.TRUE;
/* 1183 */           break;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1188 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1190 */       Report.debug("indexer", "Format '" + format + "' allowed: " + isAllowed, null);
/* 1191 */       Report.debug("indexer", "Format table name: " + formatTableName + "; Format map name: " + formatMapName, null);
/*      */     }
/* 1193 */     return isAllowed;
/*      */   }
/*      */ 
/*      */   protected boolean formatMatched(String format, String formatMap)
/*      */   {
/* 1198 */     if ((format == null) || (format.equals("")) || (formatMap == null))
/*      */     {
/* 1200 */       return false;
/*      */     }
/*      */ 
/* 1204 */     String separators = SharedObjects.getEnvironmentValue("MIMEFormatSeparators");
/* 1205 */     if (separators == null)
/*      */     {
/* 1207 */       separators = "/.";
/*      */     }
/* 1209 */     ArrayList list = splitFormat(format, separators);
/*      */ 
/* 1211 */     Vector indexableList = StringUtils.parseArray(formatMap, ',', ',');
/* 1212 */     int size = indexableList.size();
/* 1213 */     boolean isAllowed = false;
/* 1214 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1216 */       String indexableFormat = (String)indexableList.elementAt(i);
/* 1217 */       int len = list.size();
/* 1218 */       for (int j = 0; j < len; ++j)
/*      */       {
/* 1220 */         String frag = (String)list.get(j);
/* 1221 */         if (!StringUtils.matchEx(frag, indexableFormat, false, true))
/*      */           continue;
/* 1223 */         isAllowed = true;
/* 1224 */         break;
/*      */       }
/*      */ 
/* 1227 */       if (isAllowed) {
/*      */         break;
/*      */       }
/*      */     }
/*      */ 
/* 1232 */     return isAllowed;
/*      */   }
/*      */ 
/*      */   protected ArrayList splitFormat(String format, String separators)
/*      */   {
/* 1237 */     if ((separators == null) || (separators.length() == 0))
/*      */     {
/* 1239 */       return null;
/*      */     }
/* 1241 */     ArrayList splitted = new ArrayList();
/* 1242 */     splitted.add(format);
/*      */ 
/* 1244 */     for (int i = 0; i < separators.length(); ++i)
/*      */     {
/* 1246 */       char separator = separators.charAt(i);
/* 1247 */       ArrayList tempList = new ArrayList();
/* 1248 */       Iterator iter = splitted.iterator();
/* 1249 */       while (iter.hasNext())
/*      */       {
/* 1251 */         format = (String)iter.next();
/* 1252 */         StringUtils.appendListFromSequence(tempList, format, 0, format.length(), separator, '^', 32);
/*      */       }
/*      */ 
/* 1255 */       splitted = tempList;
/*      */     }
/* 1257 */     return splitted;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1262 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99857 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.CommonIndexerBulkLoader
 * JD-Core Version:    0.5.4
 */