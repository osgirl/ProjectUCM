/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderHelper;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.resource.ResourceCacheInfo;
/*     */ import intradoc.resource.ResourceCacheState;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.search.SearchDocChangeData;
/*     */ import intradoc.search.SearchDocChangeItem;
/*     */ import intradoc.search.SearchDocChangeUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Random;
/*     */ 
/*     */ public class DocumentInfoCacheUtils
/*     */ {
/*  41 */   public static int F_READ_SYNC_ONLY = 0;
/*  42 */   public static int F_READ_SYNC_AND_UPDATE = 1;
/*  43 */   public static int F_WRITE_SYNCH = 2;
/*  44 */   public static int F_RETURN_WRITE_TIMESTAMP = 4;
/*  45 */   public static int F_DOING_SYNCH = 8;
/*     */ 
/*  49 */   public static boolean m_allowOptimizeLatestReleaseQueryingBasedOnTimestamps = false;
/*     */ 
/*  52 */   public static boolean m_useSearchCacheRepairForLatestReleaseQuerying = true;
/*     */ 
/*  57 */   public static String m_webExtensionsAllowingTimestampUpdate = null;
/*     */ 
/*  64 */   public static String m_webExtensionsExcludeTimestampUpdate = null;
/*     */ 
/*  69 */   public static long m_timestampTimeoutInMillis = 1500L;
/*     */ 
/*  72 */   public static boolean m_isInit = false;
/*     */ 
/*  75 */   public static boolean[] m_sync = { false };
/*     */ 
/*  78 */   public static int m_maxSharedTimestampDiff = 15;
/*     */ 
/*  81 */   public static long m_lastSharedDocTimestamp = 0L;
/*     */ 
/*  84 */   public static long m_lastSyncedSharedDocTimestamp = 0L;
/*     */ 
/*  88 */   public static long m_lastSharedDocWriteTimestamp = 0L;
/*     */ 
/*  94 */   public static int m_smallRandomOffset = 1000;
/*     */   public static String m_docCacheInfoStoreDir;
/* 103 */   public static int m_maximumRepairTimestampDiff = 1800;
/*     */   public static SearchDocChangeData m_globalDocChangeData;
/*     */ 
/*     */   public static void checkInit()
/*     */     throws ServiceException
/*     */   {
/* 113 */     if (m_isInit)
/*     */       return;
/* 115 */     synchronized (m_sync)
/*     */     {
/* 117 */       if (!m_isInit)
/*     */       {
/* 121 */         m_allowOptimizeLatestReleaseQueryingBasedOnTimestamps = SharedObjects.getEnvValueAsBoolean("EnableOptimizedLatestReleaseQuerying", false);
/*     */ 
/* 124 */         m_useSearchCacheRepairForLatestReleaseQuerying = SharedObjects.getEnvValueAsBoolean("UseSearchCacheRepairForLatestReleaseQuerying", true);
/*     */ 
/* 128 */         String updateWebExtension = SharedObjects.getEnvironmentValue("IndexerTimestampUpdateFileFormat");
/*     */ 
/* 130 */         if (updateWebExtension == null)
/*     */         {
/* 134 */           updateWebExtension = "|jsp|jspx|hcsp|hcst|xml|";
/*     */         }
/* 136 */         m_webExtensionsAllowingTimestampUpdate = updateWebExtension;
/*     */ 
/* 138 */         m_webExtensionsExcludeTimestampUpdate = SharedObjects.getEnvironmentValue("IndexerTimestampUpdateExcludeWebExtensions");
/*     */ 
/* 141 */         m_timestampTimeoutInMillis = SharedObjects.getTypedEnvironmentInt("LatestReleasedTimestampTimeout", 1500, 18, 18);
/*     */ 
/* 144 */         m_maxSharedTimestampDiff = SharedObjects.getTypedEnvironmentInt("MaximumSharedDocTimestampDiffAllowed", m_maxSharedTimestampDiff, 24, 24);
/*     */ 
/* 146 */         m_maximumRepairTimestampDiff = SharedObjects.getTypedEnvironmentInt("MaximumSharedDocReapairTimestampDiff", m_maximumRepairTimestampDiff, 24, 24);
/*     */ 
/* 148 */         m_docCacheInfoStoreDir = LegacyDirectoryLocator.getAppDataDirectory() + "doccacheinfo";
/* 149 */         Random x = new Random();
/* 150 */         if (m_maxSharedTimestampDiff > 0)
/*     */         {
/* 152 */           m_smallRandomOffset = x.nextInt() % (m_maxSharedTimestampDiff * 250);
/* 153 */           if (m_smallRandomOffset < 0)
/*     */           {
/* 155 */             m_smallRandomOffset = -m_smallRandomOffset;
/*     */           }
/*     */         }
/*     */         try
/*     */         {
/* 160 */           FileUtils.checkOrCreateDirectoryPrepareForLocks(m_docCacheInfoStoreDir, 1, false);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 165 */           Report.trace("system", "Unable to prepare directory for cache timestamp store", e);
/*     */         }
/* 167 */         synchronizeSharedTimestampImplement(F_READ_SYNC_AND_UPDATE);
/* 168 */         m_isInit = true;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static long getSharedDocTimestamp()
/*     */     throws ServiceException
/*     */   {
/* 184 */     checkInit();
/* 185 */     long curTime = System.currentTimeMillis();
/* 186 */     synchronized (m_sync)
/*     */     {
/* 188 */       curTime = getSharedDocTimestampImplement(curTime, 0);
/*     */     }
/* 190 */     return curTime;
/*     */   }
/*     */ 
/*     */   protected static long getSharedDocTimestampImplement(long curTime, int flags)
/*     */   {
/* 200 */     boolean useOldTimestamp = false;
/* 201 */     boolean insideSynch = (flags & F_DOING_SYNCH) != 0;
/* 202 */     if ((curTime - m_lastSyncedSharedDocTimestamp > m_maxSharedTimestampDiff * 1000) && (!insideSynch))
/*     */     {
/*     */       try
/*     */       {
/* 208 */         curTime = synchronizeSharedTimestamp(F_READ_SYNC_ONLY);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 212 */         Report.trace("system", null, e);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 219 */       long diffInMillis = m_lastSharedDocTimestamp - curTime;
/* 220 */       if (diffInMillis > 0L)
/*     */       {
/* 222 */         long diffInSecs = diffInMillis / 1000L;
/* 223 */         if (diffInSecs > m_maximumRepairTimestampDiff)
/*     */         {
/* 225 */           String msg = LocaleUtils.encodeMessage("csDocCacheTimestampJumpTooLargeBackward", null, "" + diffInSecs);
/*     */ 
/* 227 */           Report.error(null, msg, null);
/*     */         }
/*     */         else
/*     */         {
/* 231 */           useOldTimestamp = true;
/*     */         }
/*     */       }
/* 234 */       if (useOldTimestamp)
/*     */       {
/* 236 */         if (Report.m_verbose)
/*     */         {
/* 238 */           Report.debug("system", "Forcing reported shared time forward of current time", null);
/*     */         }
/*     */ 
/* 244 */         curTime = m_lastSharedDocTimestamp + 1L;
/*     */       }
/* 246 */       else if (curTime == m_lastSharedDocTimestamp)
/*     */       {
/* 248 */         curTime += 1L;
/*     */       }
/* 250 */       m_lastSharedDocTimestamp = curTime;
/*     */     }
/*     */ 
/* 253 */     return curTime;
/*     */   }
/*     */ 
/*     */   public static long getSharedTimestampForSafeStartTime(long lastEndTime)
/*     */     throws ServiceException
/*     */   {
/* 270 */     checkInit();
/* 271 */     return lastEndTime - m_maxSharedTimestampDiff * 1000 - 60000L;
/*     */   }
/*     */ 
/*     */   protected static boolean updateSharedDocTimestamp(long currentTime, long externalDocTimestamp)
/*     */   {
/* 288 */     long diffInSecs = (externalDocTimestamp - currentTime) / 1000L;
/* 289 */     if (diffInSecs > m_maximumRepairTimestampDiff)
/*     */     {
/* 291 */       String msg = LocaleUtils.encodeMessage("csDocCacheTimestampJumpTooLargeBackward", null, "" + diffInSecs);
/*     */ 
/* 293 */       Report.error(null, msg, null);
/* 294 */       return false;
/*     */     }
/*     */ 
/* 297 */     boolean retVal = false;
/* 298 */     if (externalDocTimestamp > m_lastSharedDocTimestamp)
/*     */     {
/* 302 */       int safetyAdjustment = -(m_maxSharedTimestampDiff * 500) - m_smallRandomOffset;
/*     */ 
/* 304 */       m_lastSharedDocTimestamp = externalDocTimestamp - safetyAdjustment;
/* 305 */       retVal = true;
/*     */     }
/* 307 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static long synchronizeSharedTimestamp(int flags)
/*     */     throws ServiceException
/*     */   {
/* 324 */     long curTime = 0L;
/* 325 */     checkInit();
/* 326 */     synchronized (m_sync)
/*     */     {
/* 328 */       curTime = synchronizeSharedTimestampImplement(flags);
/*     */     }
/* 330 */     return curTime;
/*     */   }
/*     */ 
/*     */   protected static long synchronizeSharedTimestampImplement(int flags)
/*     */     throws ServiceException
/*     */   {
/* 344 */     boolean captureWriteTimestamp = (flags & F_WRITE_SYNCH) != 0;
/* 345 */     boolean allowUpdate = (captureWriteTimestamp) || ((flags & F_READ_SYNC_AND_UPDATE) != 0);
/* 346 */     boolean returnTimestampForWrite = (flags & F_RETURN_WRITE_TIMESTAMP) != 0;
/* 347 */     DataBinder data = new DataBinder(SharedObjects.getSafeEnvironment());
/* 348 */     FileUtils.reserveDirectory(m_docCacheInfoStoreDir);
/*     */ 
/* 352 */     long curTime = System.currentTimeMillis();
/* 353 */     curTime = getSharedDocTimestampImplement(curTime, F_DOING_SYNCH);
/* 354 */     long retTime = curTime;
/*     */     try
/*     */     {
/* 358 */       boolean updateCacheState = allowUpdate;
/* 359 */       String lastWriteTimestamp = null;
/* 360 */       if (ResourceUtils.serializeDataBinder(m_docCacheInfoStoreDir, "cachestate.hda", data, false, false))
/*     */       {
/* 362 */         String sharedTimeStamp = data.getLocal("sharedDocCacheTimestamp");
/* 363 */         lastWriteTimestamp = data.getLocal("sharedDocCacheLastWriteTimstamp");
/* 364 */         if ((sharedTimeStamp != null) && (sharedTimeStamp.length() > 4))
/*     */         {
/* 366 */           Date sharedDate = LocaleUtils.parseUtcODBC(sharedTimeStamp);
/*     */ 
/* 369 */           long sharedDateLong = sharedDate.getTime();
/* 370 */           if ((sharedDateLong > curTime) && 
/* 374 */             (updateSharedDocTimestamp(curTime, sharedDateLong)))
/*     */           {
/* 376 */             if (Report.m_verbose)
/*     */             {
/* 378 */               Report.trace("system", "Shared timestamp is pushing current VMs doc shared timestamp forward to " + LocaleUtils.debugDate(sharedDateLong) + " from " + LocaleUtils.debugDate(sharedDateLong), null);
/*     */             }
/*     */ 
/* 383 */             if (m_lastSharedDocTimestamp > curTime)
/*     */             {
/* 385 */               curTime = m_lastSharedDocTimestamp;
/*     */             }
/* 387 */             updateCacheState = false;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 392 */       m_lastSyncedSharedDocTimestamp = curTime;
/* 393 */       retTime = curTime;
/* 394 */       if ((captureWriteTimestamp) || (lastWriteTimestamp == null) || (lastWriteTimestamp.length() <= 4))
/*     */       {
/* 397 */         updateCacheState = true;
/* 398 */         captureWriteTimestamp = true;
/* 399 */         m_lastSharedDocWriteTimestamp = curTime;
/*     */       }
/*     */       else
/*     */       {
/* 403 */         m_lastSharedDocWriteTimestamp = LocaleUtils.parseODBC(lastWriteTimestamp).getTime();
/*     */ 
/* 407 */         if ((m_lastSharedDocWriteTimestamp < curTime) && (returnTimestampForWrite))
/*     */         {
/* 409 */           retTime = m_lastSharedDocWriteTimestamp;
/*     */         }
/*     */       }
/*     */ 
/* 413 */       if (updateCacheState)
/*     */       {
/* 415 */         m_lastSharedDocTimestamp = curTime;
/* 416 */         String curTimeStr = LocaleUtils.formatUtcODBC(new Date(curTime));
/* 417 */         data.putLocal("sharedDocCacheTimestamp", curTimeStr);
/* 418 */         if (captureWriteTimestamp)
/*     */         {
/* 420 */           data.putLocal("sharedDocCacheLastWriteTimstamp", curTimeStr);
/* 421 */           m_lastSharedDocWriteTimestamp = curTime;
/*     */         }
/* 423 */         ResourceUtils.serializeDataBinder(m_docCacheInfoStoreDir, "cachestate.hda", data, true, false);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 428 */       FileUtils.releaseDirectory(m_docCacheInfoStoreDir);
/*     */     }
/* 430 */     return retTime;
/*     */   }
/*     */ 
/*     */   public static String getActionDateConvertedTimestamp(DataBinder binder, String key, boolean isForLessThanClause)
/*     */     throws DataException
/*     */   {
/* 445 */     String val = binder.get(key);
/* 446 */     Date d = binder.parseDate(key, val);
/* 447 */     long l = d.getTime();
/* 448 */     int minute = 60000;
/* 449 */     if (isForLessThanClause)
/*     */     {
/* 451 */       l = l + minute - 1L;
/*     */     }
/* 453 */     l -= l % minute;
/* 454 */     return LocaleUtils.formatODBC(new Date(l));
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean allowOptimizeLatestReleaseQuerying()
/*     */     throws ServiceException
/*     */   {
/* 467 */     return allowOptimizeLatestReleaseQueryingBasedOnTimestamps();
/*     */   }
/*     */ 
/*     */   public static boolean allowOptimizeLatestReleaseQueryingBasedOnTimestamps()
/*     */     throws ServiceException
/*     */   {
/* 476 */     checkInit();
/* 477 */     return m_allowOptimizeLatestReleaseQueryingBasedOnTimestamps;
/*     */   }
/*     */ 
/*     */   public static boolean useSearchCacheRepairForLatestReleaseQuerying()
/*     */     throws ServiceException
/*     */   {
/* 487 */     checkInit();
/* 488 */     return m_useSearchCacheRepairForLatestReleaseQuerying;
/*     */   }
/*     */ 
/*     */   public static boolean supportsWebviewableTimestampUpdate(Map docProps)
/*     */     throws ServiceException
/*     */   {
/* 502 */     checkInit();
/* 503 */     if (docProps == null)
/*     */     {
/* 505 */       return false;
/*     */     }
/* 507 */     String webExtension = (String)docProps.get("dWebExtension");
/* 508 */     if (webExtension == null)
/*     */     {
/* 510 */       return false;
/*     */     }
/* 512 */     boolean retVal = false;
/* 513 */     if ((m_webExtensionsExcludeTimestampUpdate == null) && (m_allowOptimizeLatestReleaseQueryingBasedOnTimestamps))
/*     */     {
/* 515 */       retVal = true;
/*     */     }
/* 517 */     else if ((!m_allowOptimizeLatestReleaseQueryingBasedOnTimestamps) || (StringUtils.matchEx(webExtension, m_webExtensionsExcludeTimestampUpdate, true, true)))
/*     */     {
/* 520 */       if (m_webExtensionsAllowingTimestampUpdate.indexOf("|" + webExtension + "|") >= 0)
/*     */       {
/* 522 */         retVal = true;
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 527 */       retVal = true;
/*     */     }
/* 529 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static boolean getLatestReleasedDocInfo(String docName, DataBinder binder, String rsName, String pathKey, String tsKey, boolean useNativeForPath, long curTime, Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 572 */     String id = "infoname://" + docName;
/* 573 */     ResourceCacheInfo cacheInfo = ResourceCacheState.getTemporaryCache(id, curTime);
/* 574 */     boolean doFileTimestamps = m_allowOptimizeLatestReleaseQueryingBasedOnTimestamps;
/* 575 */     String curPath = null;
/* 576 */     String docPath = null;
/* 577 */     long docPathTS = 0L;
/* 578 */     DocInfoCacheAssociatedInfo associatedInfo = null;
/* 579 */     DataResultSet curInfo = null;
/* 580 */     SearchDocChangeData changeData = null;
/* 581 */     boolean fromCache = false;
/* 582 */     boolean putInCache = false;
/* 583 */     long curCachePathTS = 0L;
/* 584 */     long webPathLastModified = 0L;
/* 585 */     long timeoutInMillis = m_timestampTimeoutInMillis;
/* 586 */     checkInit();
/*     */ 
/* 588 */     if (cacheInfo != null)
/*     */     {
/* 593 */       synchronized (cacheInfo)
/*     */       {
/* 595 */         if ((cacheInfo.m_resourceObj instanceof DataResultSet) && (cacheInfo.m_associatedInfo instanceof DocInfoCacheAssociatedInfo))
/*     */         {
/* 598 */           curInfo = (DataResultSet)cacheInfo.m_resourceObj;
/* 599 */           curInfo = curInfo.shallowClone();
/* 600 */           associatedInfo = (DocInfoCacheAssociatedInfo)cacheInfo.m_associatedInfo;
/* 601 */           if (SystemUtils.m_verbose)
/*     */           {
/* 603 */             Report.debug("doccache", "Retrieved doc info cache item, cacheInfo=" + cacheInfo, null);
/*     */           }
/*     */ 
/* 606 */           boolean isUpToDate = false;
/* 607 */           if (doFileTimestamps)
/*     */           {
/* 609 */             if ((associatedInfo != null) && (associatedInfo.m_usingFileTimestamps))
/*     */             {
/* 611 */               if (cacheInfo.m_agedTS >= curTime)
/*     */               {
/* 615 */                 isUpToDate = true;
/* 616 */                 curCachePathTS = cacheInfo.m_lastLoaded;
/*     */               }
/*     */ 
/* 620 */               curPath = (associatedInfo.m_useNativeForPath) ? associatedInfo.m_vaultPath : associatedInfo.m_webPath;
/*     */ 
/* 622 */               if (!isUpToDate)
/*     */               {
/* 624 */                 File f = new File(curPath);
/* 625 */                 curCachePathTS = f.lastModified();
/* 626 */                 if (curCachePathTS == cacheInfo.m_lastLoaded)
/*     */                 {
/* 628 */                   isUpToDate = true;
/* 629 */                   cacheInfo.m_agedTS = (curTime + timeoutInMillis);
/*     */                 }
/*     */               }
/* 632 */               if (isUpToDate)
/*     */               {
/* 634 */                 docPath = associatedInfo.m_vaultPath;
/* 635 */                 docPathTS = associatedInfo.m_vaultPathTimestamp;
/* 636 */                 fromCache = true;
/* 637 */                 Report.trace("doccache", "Cache up to date based on file system, associatedInfo=" + associatedInfo, null);
/*     */               }
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 643 */             changeData = getDocChangeData(binder, cxt);
/* 644 */             if (changeData != null)
/*     */             {
/* 647 */               boolean canUseSearchCacheData = false;
/* 648 */               if (changeData.m_currentAge == associatedInfo.m_searchCacheAge)
/*     */               {
/* 651 */                 Report.trace("doccache", "Cache in same search cache generation, associatedInfo=" + associatedInfo, null);
/* 652 */                 fromCache = true;
/*     */               }
/*     */               else
/*     */               {
/* 656 */                 long sharedTime = getSharedTimestampForSafeStartTime(cacheInfo.m_lastLoaded);
/* 657 */                 if (sharedTime >= changeData.m_fastChangesStartTime)
/*     */                 {
/* 659 */                   canUseSearchCacheData = true;
/*     */                 }
/*     */               }
/* 662 */               if (canUseSearchCacheData)
/*     */               {
/* 665 */                 boolean hasPossiblyChanged = false;
/* 666 */                 String docId = associatedInfo.m_dID;
/* 667 */                 if (changeData.m_fastIdReferences.get(docId) != null)
/*     */                 {
/* 669 */                   Report.trace("doccache", "Cache invalidated by recent delete for " + docName + ", dID=" + docId, null);
/* 670 */                   hasPossiblyChanged = true;
/*     */                 }
/*     */                 else
/*     */                 {
/* 674 */                   String dbDocName = associatedInfo.m_dDocName;
/* 675 */                   SearchDocChangeItem changeItem = (SearchDocChangeItem)changeData.m_fastRevClassReferences.get(dbDocName);
/* 676 */                   if ((changeItem != null) && (changeItem.m_itemAge > associatedInfo.m_searchCacheAge))
/*     */                   {
/* 679 */                     Report.trace("doccache", "Cache invalidated by recent modification for " + docName + ", changeItem=" + changeItem, null);
/*     */ 
/* 681 */                     hasPossiblyChanged = true;
/*     */                   }
/*     */                 }
/* 684 */                 if (!hasPossiblyChanged)
/*     */                 {
/* 686 */                   Report.trace("doccache", "Cache not expired by search change data, associatedInfo=" + associatedInfo, null);
/* 687 */                   fromCache = true;
/*     */                 }
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 695 */     if (!fromCache)
/*     */     {
/* 697 */       if (associatedInfo == null)
/*     */       {
/* 699 */         Report.trace("doccache", "No cache found for latest released document " + docName, null);
/*     */       }
/*     */       else
/*     */       {
/* 703 */         Report.trace("doccache", "Cache considered unusable, associatedInfo=" + associatedInfo, null);
/*     */       }
/*     */ 
/* 706 */       DataBinder params = new DataBinder();
/* 707 */       params.putLocal("dDocName", docName);
/* 708 */       ResultSet rset = ws.createResultSet("QdocInfoCurrentIndexed", params);
/* 709 */       boolean currentIsValid = false;
/* 710 */       curInfo = new DataResultSet();
/* 711 */       String newPath = null;
/*     */ 
/* 713 */       FileStoreProvider fileStore = FileStoreProviderLoader.initFileStore(cxt);
/* 714 */       FileStoreProviderHelper utils = FileStoreProviderHelper.getFileStoreProviderUtils(fileStore, cxt);
/*     */ 
/* 716 */       if ((rset != null) && (!rset.isEmpty()))
/*     */       {
/* 718 */         curInfo.copy(rset);
/* 719 */         params.addResultSet("DOC_INFO", curInfo);
/*     */ 
/* 721 */         if (doFileTimestamps)
/*     */         {
/* 725 */           newPath = utils.computeRenditionPath(params, "webViewableFile", cxt);
/* 726 */           if (newPath == null)
/*     */           {
/* 728 */             Report.trace("doccache", "Could not compute path to webviewable for latest released revision of " + docName, null);
/* 729 */             return false;
/*     */           }
/* 731 */           File f = new File(newPath);
/* 732 */           webPathLastModified = f.lastModified();
/* 733 */           if (webPathLastModified > 0L)
/*     */           {
/* 736 */             currentIsValid = true;
/* 737 */             String webExtension = curInfo.getStringValueByName("dWebExtension");
/* 738 */             Map docProps = new HashMap();
/* 739 */             docProps.put("dWebExtension", webExtension);
/* 740 */             if (supportsWebviewableTimestampUpdate(docProps))
/*     */             {
/* 742 */               putInCache = true;
/*     */             }
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 748 */           putInCache = true;
/* 749 */           currentIsValid = true;
/*     */         }
/*     */       }
/* 752 */       if (!currentIsValid)
/*     */       {
/* 758 */         rset = ws.createResultSet("QlatestReleasedIDByName", params);
/* 759 */         if ((rset == null) || (!rset.isRowPresent()))
/*     */         {
/* 761 */           Report.trace("doccache", "Query QlatestReleasedIDByName returned no row " + docName, null);
/*     */ 
/* 763 */           return false;
/*     */         }
/*     */ 
/* 767 */         int docIDIndex = ResultSetUtils.getIndexMustExist(rset, "dID");
/* 768 */         String docID = rset.getStringValue(docIDIndex);
/*     */ 
/* 773 */         if (docID.length() == 0)
/*     */         {
/* 775 */           Report.trace("system", "Query QlatestReleasedIDByName returned invalid empty row for " + docName + ", likely source is a database bug.", null);
/*     */ 
/* 777 */           return false;
/*     */         }
/*     */ 
/* 780 */         params.putLocal("dID", docID);
/* 781 */         rset = ws.createResultSet("QdocInfo", params);
/* 782 */         curInfo.copy(rset);
/* 783 */         params.addResultSet("DOC_INFO", curInfo);
/*     */ 
/* 785 */         if (doFileTimestamps)
/*     */         {
/* 787 */           newPath = utils.computeRenditionPath(params, "webViewableFile", cxt);
/* 788 */           if (newPath == null)
/*     */           {
/* 790 */             Report.trace("doccache", "Could not compute path to webviewable revision queried using QlatestReleasedIDByName of " + docName, null);
/*     */ 
/* 792 */             return false;
/*     */           }
/*     */ 
/* 800 */           if (tsKey != null)
/*     */           {
/* 802 */             File f = new File(newPath);
/* 803 */             webPathLastModified = f.lastModified();
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 808 */       if (doFileTimestamps)
/*     */       {
/* 810 */         curPath = newPath;
/*     */ 
/* 815 */         docPath = utils.computeRenditionPath(params, "primaryFile", cxt);
/* 816 */         if (docPath != null)
/*     */         {
/* 818 */           File f = new File(docPath);
/* 819 */           docPathTS = f.lastModified();
/*     */         }
/*     */       }
/*     */ 
/* 823 */       if ((putInCache) && (!doFileTimestamps))
/*     */       {
/* 826 */         if (changeData == null)
/*     */         {
/* 828 */           changeData = getDocChangeData(binder, cxt);
/*     */         }
/* 830 */         if (changeData == null)
/*     */         {
/* 832 */           putInCache = false;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 837 */       if (putInCache)
/*     */       {
/* 839 */         cacheInfo = new ResourceCacheInfo(id, "DataRecord", curPath);
/* 840 */         cacheInfo.m_size = 2000L;
/* 841 */         cacheInfo.m_resourceObj = curInfo;
/* 842 */         DocInfoCacheAssociatedInfo newAssociatedInfo = new DocInfoCacheAssociatedInfo();
/* 843 */         cacheInfo.m_associatedInfo = newAssociatedInfo;
/* 844 */         if (doFileTimestamps)
/*     */         {
/* 846 */           cacheInfo.m_lastLoaded = ((useNativeForPath) ? docPathTS : webPathLastModified);
/* 847 */           newAssociatedInfo.m_useNativeForPath = useNativeForPath;
/* 848 */           newAssociatedInfo.m_webPath = newPath;
/* 849 */           newAssociatedInfo.m_webPathTimestamp = webPathLastModified;
/* 850 */           newAssociatedInfo.m_vaultPath = docPath;
/* 851 */           newAssociatedInfo.m_vaultPathTimestamp = docPathTS;
/*     */         }
/*     */         else
/*     */         {
/* 855 */           newAssociatedInfo.m_searchCacheAge = changeData.m_currentAge;
/* 856 */           cacheInfo.m_lastLoaded = curTime;
/*     */         }
/* 858 */         newAssociatedInfo.m_dID = curInfo.getStringValueByName("dID");
/* 859 */         newAssociatedInfo.m_dDocName = curInfo.getStringValueByName("dDocName");
/* 860 */         ResourceCacheState.addTimedTemporaryCache(id, cacheInfo, curTime);
/*     */ 
/* 863 */         cacheInfo.m_agedTS = (curTime + timeoutInMillis);
/*     */ 
/* 865 */         Report.trace("doccache", "Put cache in for " + docName + ", dID=" + newAssociatedInfo.m_dID, null);
/* 866 */         if (SystemUtils.m_verbose)
/*     */         {
/* 868 */           Report.debug("doccache", "Cache put in, cacheInfo=" + cacheInfo, null);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 873 */     if ((tsKey != null) && (doFileTimestamps))
/*     */     {
/* 875 */       if (fromCache)
/*     */       {
/* 877 */         webPathLastModified = curCachePathTS;
/*     */       }
/* 879 */       if ((webPathLastModified <= 0L) || (docPathTS <= 2L))
/*     */       {
/* 881 */         Report.trace("doccache", "Did not get good timestamps on files paths, doc info retrieval is considered to have failed", null);
/*     */ 
/* 883 */         return false;
/*     */       }
/* 885 */       String tsStr = "" + webPathLastModified;
/* 886 */       binder.putLocal(tsKey, tsStr);
/* 887 */       String path = (useNativeForPath) ? docPath : curPath;
/* 888 */       if (path != null)
/*     */       {
/* 890 */         binder.putLocal(pathKey, path);
/*     */       }
/*     */     }
/*     */ 
/* 894 */     if (rsName != null)
/*     */     {
/* 896 */       DataResultSet copy = new DataResultSet();
/* 897 */       copy.copy(curInfo);
/* 898 */       binder.addResultSet(rsName, copy);
/*     */     }
/*     */ 
/* 902 */     binder.putLocal("accessedDocInfoCache", "1");
/* 903 */     binder.putLocal("fromDocInfoCache", (fromCache) ? "1" : "");
/* 904 */     binder.putLocal("putInDocInfoCache", (putInCache) ? "1" : "");
/* 905 */     if (cxt != null)
/*     */     {
/* 907 */       cxt.setCachedObject("docCacheDocName", docName);
/* 908 */       if (cacheInfo != null)
/*     */       {
/* 910 */         cxt.setCachedObject("docCacheInfo", cacheInfo);
/*     */       }
/*     */     }
/* 913 */     PluginFilters.filter("postGetLatestReleasedDocInfo", ws, binder, cxt);
/* 914 */     return true;
/*     */   }
/*     */ 
/*     */   public static SearchDocChangeData getDocChangeData(DataBinder binder, ExecutionContext cxt)
/*     */   {
/* 920 */     SearchDocChangeData changeData = null;
/* 921 */     if ((m_useSearchCacheRepairForLatestReleaseQuerying) && (m_globalDocChangeData == null))
/*     */     {
/* 923 */       m_globalDocChangeData = SearchDocChangeUtils.getOrCreateGlobalSearchChangeData(binder, cxt);
/*     */     }
/* 925 */     if ((m_globalDocChangeData != null) && (m_globalDocChangeData.m_differentialUpdatesAllowed))
/*     */     {
/* 927 */       SearchDocChangeData dataCopy = (SearchDocChangeData)cxt.getCachedObject("SearchDocChangeData");
/* 928 */       if (dataCopy == null)
/*     */       {
/* 930 */         dataCopy = new SearchDocChangeData();
/* 931 */         SearchDocChangeUtils.getChangeFastData(m_globalDocChangeData, dataCopy);
/*     */ 
/* 935 */         cxt.setCachedObject("SearchDocChangeData", dataCopy);
/*     */       }
/* 937 */       if (SearchDocChangeUtils.checkIsUsable(dataCopy))
/*     */       {
/* 939 */         changeData = dataCopy;
/*     */       }
/*     */     }
/* 942 */     return changeData;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 947 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82306 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.DocumentInfoCacheUtils
 * JD-Core Version:    0.5.4
 */