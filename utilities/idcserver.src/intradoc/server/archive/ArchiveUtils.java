/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.resource.ResourceCacheInfo;
/*     */ import intradoc.resource.ResourceCacheState;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.MonikerWatcher;
/*     */ import intradoc.shared.ArchiveCollections;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.LegacyDocumentPathBuilder;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ArchiveUtils
/*     */ {
/*     */   public static final String m_archivesFile = "collection.hda";
/*     */   public static final String m_archivesTableName = "Archives";
/*  39 */   public static final String[] ARCHIVE_COLUMNS = { "aArchiveName", "aArchiveDescription" };
/*     */   public static final String m_batchDefFile = "exports.hda";
/*     */   public static final String m_batchTableName = "BatchFiles";
/*  48 */   public static final String[] BATCHFILE_COLUMNS = { "aBatchFile", "aState", "IDC_Name", "aNumDocuments", "aIsTableBatch" };
/*     */ 
/*  56 */   public static final String[][] BATCHFILE_COLUMN_DEFAULTS = { { "aIsTableBatch", "0" } };
/*     */ 
/*  61 */   public static Map m_archiveDataMap = new Hashtable();
/*     */ 
/*     */   public static void createDefaultCollection()
/*     */     throws ServiceException
/*     */   {
/*  67 */     String defCollection = LegacyDirectoryLocator.getDefaultCollection();
/*  68 */     String name = SharedObjects.getEnvironmentValue("IDC_Name");
/*  69 */     if (name == null)
/*     */     {
/*  71 */       throw new ServiceException("!csArchiverNoIdcName");
/*     */     }
/*     */ 
/*  74 */     createCollection(defCollection, name);
/*  75 */     FileUtils.checkOrCreateDirectory(DirectoryLocator.getCollectionsDirectory(), 2, 1);
/*     */ 
/*  77 */     FileUtils.checkOrCreateDirectory(DirectoryLocator.getCollectionExport(), 2, 1);
/*     */   }
/*     */ 
/*     */   public static void createCollection(String dir, String name)
/*     */     throws ServiceException
/*     */   {
/*  84 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 2, true);
/*  85 */     FileUtils.reserveDirectory(dir);
/*     */     try
/*     */     {
/*  88 */       File colFile = FileUtilsCfgBuilder.getCfgFile(dir + "collection.hda", "Archive", false);
/*  89 */       if (colFile.exists() == true)
/*     */       {
/*     */         return;
/*     */       }
/*     */ 
/*  94 */       DataBinder binder = new DataBinder();
/*  95 */       DataResultSet rset = new DataResultSet(ARCHIVE_COLUMNS);
/*  96 */       binder.addResultSet("Archives", rset);
/*  97 */       binder.putLocal("IDC_Name", name);
/*  98 */       ResourceUtils.serializeDataBinder(dir, "collection.hda", binder, true, false);
/*     */     }
/*     */     finally
/*     */     {
/* 102 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static DataBinder readCollectionsData(boolean isLock)
/*     */     throws ServiceException, DataException
/*     */   {
/* 112 */     String dir = LegacyDirectoryLocator.getCollectionsDirectory();
/* 113 */     String lockDir = null;
/* 114 */     if (isLock)
/*     */     {
/* 116 */       lockDir = dir;
/* 117 */       FileUtils.reserveDirectory(dir);
/*     */     }
/* 119 */     DataBinder data = readFileEx(dir, "collections.hda", lockDir, true);
/*     */ 
/* 121 */     DataResultSet rset = (DataResultSet)data.getResultSet(ArchiveCollections.m_tableName);
/* 122 */     if (rset != null)
/*     */     {
/* 124 */       ArchiveCollections ac = new ArchiveCollections();
/* 125 */       ac.checkAndUpgradeCollections(rset);
/*     */     }
/* 127 */     return data;
/*     */   }
/*     */ 
/*     */   public static DataBinder readCollectionData(String dir, boolean isLock)
/*     */     throws ServiceException
/*     */   {
/* 133 */     String lockDir = null;
/* 134 */     if (isLock)
/*     */     {
/* 136 */       lockDir = dir;
/*     */     }
/* 138 */     return readFile(dir, "collection.hda", lockDir);
/*     */   }
/*     */ 
/*     */   public static DataBinder readTransferData(boolean isLock)
/*     */     throws ServiceException, DataException
/*     */   {
/* 144 */     String dir = LegacyDirectoryLocator.getCollectionsDirectory();
/* 145 */     String lockDir = null;
/* 146 */     if (isLock)
/*     */     {
/* 148 */       lockDir = dir;
/* 149 */       FileUtils.reserveDirectory(dir);
/*     */     }
/*     */ 
/* 152 */     return readFile(dir, "transfer.hda", lockDir);
/*     */   }
/*     */ 
/*     */   public static DataBinder readArchiveFile(String collDir, String archiveDir, boolean isLock)
/*     */     throws ServiceException
/*     */   {
/* 158 */     String name = archiveDir.toLowerCase();
/* 159 */     String dir = collDir + name;
/* 160 */     String lockDir = null;
/* 161 */     if (isLock)
/*     */     {
/* 163 */       lockDir = dir;
/*     */     }
/* 165 */     DataBinder binder = readFile(dir, "archive.hda", lockDir);
/* 166 */     boolean isEmpty = DataBinderUtils.getBoolean(binder, "IsMissingHdaFile", false);
/* 167 */     if (isEmpty)
/*     */     {
/* 170 */       Report.warning("system", null, "csArcvhiveDefMissing", new Object[] { archiveDir, collDir });
/*     */ 
/* 173 */       DataBinder oldBinder = (DataBinder)m_archiveDataMap.get(name);
/* 174 */       if (oldBinder != null)
/*     */       {
/* 176 */         binder = oldBinder;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 181 */       m_archiveDataMap.put(name, binder);
/*     */     }
/* 183 */     return binder;
/*     */   }
/*     */ 
/*     */   public static DataBinder readArchiveFileForCollection(String collName, String archiveName, boolean isLock)
/*     */     throws ServiceException, DataException
/*     */   {
/* 189 */     CollectionData collectionData = getCollection(collName);
/* 190 */     if (collectionData == null)
/*     */     {
/* 192 */       String msg = LocaleUtils.encodeMessage("csArchiveReadError", null, archiveName, collName);
/* 193 */       throw new ServiceException(msg);
/*     */     }
/* 195 */     String archiveDir = archiveName.toLowerCase();
/* 196 */     return readArchiveFile(collectionData.m_location, archiveDir, isLock);
/*     */   }
/*     */ 
/*     */   public static DataBinder readExportsFile(String dir, String lockDir) throws ServiceException
/*     */   {
/* 201 */     return readFile(dir, "exports.hda", lockDir);
/*     */   }
/*     */ 
/*     */   public static DataBinder readReplicationFile(boolean isLock) throws ServiceException
/*     */   {
/* 206 */     String dir = LegacyDirectoryLocator.getCollectionsDirectory();
/* 207 */     String lockDir = null;
/* 208 */     if (isLock)
/*     */     {
/* 210 */       lockDir = dir;
/*     */     }
/* 212 */     return readFile(dir, "automated.hda", lockDir);
/*     */   }
/*     */ 
/*     */   public static DataBinder readFile(String dir, String filename, String lockDir)
/*     */     throws ServiceException
/*     */   {
/* 218 */     return readFileEx(dir, filename, lockDir, false);
/*     */   }
/*     */ 
/*     */   public static DataBinder readFileEx(String dir, String filename, String lockDir, boolean mustExist)
/*     */     throws ServiceException
/*     */   {
/* 224 */     if (SystemUtils.m_verbose)
/*     */     {
/* 226 */       Report.debug("archiver", "reading archive file(" + dir + ", " + filename + ", " + lockDir + ")", null);
/*     */     }
/*     */ 
/* 229 */     DataBinder binder = new DataBinder(true);
/* 230 */     binder.putLocal("IsMissingHdaFile", "1");
/* 231 */     if (lockDir != null)
/*     */     {
/* 233 */       FileUtils.reserveDirectory(lockDir);
/*     */     }
/*     */     try
/*     */     {
/* 237 */       if (!ResourceUtils.serializeDataBinder(dir, filename, binder, false, mustExist))
/*     */       {
/* 241 */         FileUtils.testFileSystem(dir);
/*     */       }
/*     */       else
/*     */       {
/* 245 */         binder.removeLocal("IsMissingHdaFile");
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 250 */       if (lockDir != null)
/*     */       {
/* 252 */         FileUtils.releaseDirectory(lockDir);
/*     */       }
/*     */     }
/* 255 */     return binder;
/*     */   }
/*     */ 
/*     */   public static long writeCollections(ArchiveCollections collections)
/*     */     throws ServiceException, DataException
/*     */   {
/* 266 */     String dir = LegacyDirectoryLocator.getCollectionsDirectory();
/* 267 */     DataBinder binder = collections.makeData();
/*     */ 
/* 269 */     return writeFile(dir, "collections.hda", binder, false);
/*     */   }
/*     */ 
/*     */   public static long writeCollectionData(String dir, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 275 */     return writeFile(dir, "collection.hda", binder, false);
/*     */   }
/*     */ 
/*     */   public static long writeArchiveFile(String dir, DataBinder binder, boolean isNew)
/*     */     throws ServiceException
/*     */   {
/* 281 */     binder.setFieldType("aTotalTransferedOut", "message");
/* 282 */     binder.setFieldType("aTotalTransferedIn", "message");
/*     */ 
/* 284 */     return writeFile(dir, "archive.hda", binder, !isNew);
/*     */   }
/*     */ 
/*     */   public static long writeExportsFile(String dir, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 290 */     return writeFile(dir, "exports.hda", binder, false);
/*     */   }
/*     */ 
/*     */   public static long writeReplicationFile(DataBinder binder) throws ServiceException
/*     */   {
/* 295 */     String dir = DirectoryLocator.getCollectionsDirectory();
/* 296 */     return writeFile(dir, "automated.hda", binder, false);
/*     */   }
/*     */ 
/*     */   public static long writeTransferData(DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 302 */     String dir = DirectoryLocator.getCollectionsDirectory();
/* 303 */     return writeFile(dir, "transfer.hda", binder, false);
/*     */   }
/*     */ 
/*     */   public static long writeFile(String dir, String name, DataBinder binder, boolean isCheckExist)
/*     */     throws ServiceException
/*     */   {
/* 310 */     Report.trace("archiver", "writing archive file(" + dir + ", " + name + ")", null);
/* 311 */     if (binder != null)
/*     */     {
/* 313 */       if (SystemUtils.m_verbose)
/*     */       {
/* 315 */         Report.debug("archiver", "on write of '" + name + "' dumping local data of archive file: " + binder.getLocalData(), null);
/* 316 */         ResultSet rset = binder.getResultSet("BatchFiles");
/* 317 */         if ((rset != null) && (rset instanceof DataResultSet))
/*     */         {
/* 319 */           DataResultSet drset = (DataResultSet)rset;
/* 320 */           if (drset.isEmpty())
/*     */           {
/* 322 */             Report.debug("archiver", "on write of '" + name + "' no batch files listed.", null);
/*     */           }
/*     */           else
/*     */           {
/* 326 */             boolean isTrimmed = false;
/* 327 */             if (drset.getNumRows() > 5)
/*     */             {
/* 329 */               drset.setCurrentRow(drset.getNumRows() - 5);
/* 330 */               isTrimmed = true;
/*     */             }
/*     */             else
/*     */             {
/* 334 */               drset.setCurrentRow(0);
/*     */             }
/* 336 */             StringBuffer buf = new StringBuffer();
/* 337 */             if (isTrimmed)
/*     */             {
/* 339 */               buf.append("(trimmed to last five)...");
/*     */             }
/* 341 */             boolean firstTime = true;
/* 342 */             for (; drset.isRowPresent(); drset.next())
/*     */             {
/* 344 */               String batchName = ResultSetUtils.getValue(drset, "aBatchFile");
/* 345 */               if (!firstTime)
/*     */               {
/* 347 */                 buf.append(",");
/*     */               }
/* 349 */               if (batchName == null)
/*     */               {
/* 351 */                 batchName = "<null>";
/*     */               }
/* 353 */               buf.append(batchName);
/* 354 */               firstTime = false;
/*     */             }
/* 356 */             Report.debug("archiver", "on write of '" + name + "' the batch files listed: " + buf.toString(), null);
/*     */           }
/*     */         }
/*     */       }
/* 360 */       if (isCheckExist)
/*     */       {
/* 362 */         File f = FileUtilsCfgBuilder.getCfgFile(dir + name, "Archive", false);
/* 363 */         if (!f.exists())
/*     */         {
/* 365 */           String msg = LocaleUtils.encodeMessage("csArchiveWriteError", null, dir + name);
/* 366 */           throw new ServiceException(msg);
/*     */         }
/*     */       }
/* 369 */       ResourceUtils.serializeDataBinder(dir, name, binder, true, false);
/*     */     }
/*     */ 
/* 372 */     File f = FileUtilsCfgBuilder.getCfgFile(dir + name, "Archive", false);
/* 373 */     return f.lastModified();
/*     */   }
/*     */ 
/*     */   public static long checkReplicationFile()
/*     */   {
/* 381 */     String dir = DirectoryLocator.getCollectionsDirectory();
/*     */     try
/*     */     {
/* 385 */       File f = FileUtilsCfgBuilder.getCfgFile(dir + "automated.hda", "Archive", false);
/* 386 */       return f.lastModified();
/*     */     }
/*     */     catch (Exception e) {
/*     */     }
/* 390 */     return -2L;
/*     */   }
/*     */ 
/*     */   public static long checkTransferFile()
/*     */   {
/* 396 */     String dir = DirectoryLocator.getCollectionsDirectory();
/*     */     try
/*     */     {
/* 400 */       File f = FileUtilsCfgBuilder.getCfgFile(dir + "transfer.hda", "Archive", false);
/* 401 */       return f.lastModified();
/*     */     }
/*     */     catch (Exception e) {
/*     */     }
/* 405 */     return -2L;
/*     */   }
/*     */ 
/*     */   public static long checkArchiveFile(String collName, String archiveName)
/*     */   {
/* 411 */     return checkFile(collName, archiveName, "archive.hda");
/*     */   }
/*     */ 
/*     */   public static long checkExportFile(String collName, String archiveName)
/*     */   {
/* 416 */     return checkFile(collName, archiveName, "exports.hda");
/*     */   }
/*     */ 
/*     */   public static long checkFile(String collName, String archiveName, String filename)
/*     */   {
/*     */     try
/*     */     {
/* 423 */       CollectionData data = getCollection(collName);
/*     */ 
/* 425 */       String location = data.m_location + archiveName.toLowerCase();
/* 426 */       File f = FileUtilsCfgBuilder.getCfgFile(location + "/" + filename, "Archive", false);
/* 427 */       return f.lastModified();
/*     */     }
/*     */     catch (Exception e) {
/*     */     }
/* 431 */     return -2L;
/*     */   }
/*     */ 
/*     */   public static String[] parseLocation(String location)
/*     */   {
/* 438 */     String[] data = new String[2];
/* 439 */     int index = location.indexOf(47);
/* 440 */     if (index < 0)
/*     */     {
/* 443 */       data[0] = location;
/* 444 */       data[1] = "";
/*     */     }
/*     */     else
/*     */     {
/* 448 */       data[0] = location.substring(0, index);
/* 449 */       data[1] = location.substring(index + 1);
/*     */     }
/* 451 */     return data;
/*     */   }
/*     */ 
/*     */   public static String buildLocation(String collectionName, String archiveName)
/*     */   {
/* 456 */     return collectionName + "/" + archiveName;
/*     */   }
/*     */ 
/*     */   public static String buildArchiveDirectory(String dir, String archiveName)
/*     */   {
/* 461 */     return dir + archiveName.toLowerCase() + "/";
/*     */   }
/*     */ 
/*     */   public static CollectionData getCollection(DataBinder binder)
/*     */     throws ServiceException, DataException
/*     */   {
/* 468 */     String collName = binder.getLocal("IDC_Name");
/* 469 */     return getCollection(collName);
/*     */   }
/*     */ 
/*     */   public static CollectionData getCollection(String collName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 475 */     if (collName == null)
/*     */     {
/* 477 */       throw new DataException("!csArchiverCollectionNameMissing");
/*     */     }
/* 479 */     ArchiveCollections collections = (ArchiveCollections)SharedObjects.getTable(ArchiveCollections.m_tableName);
/*     */ 
/* 481 */     if (collections == null)
/*     */     {
/* 484 */       String msg = LocaleUtils.encodeMessage("csUnableToFindTable", null, "Collections");
/*     */ 
/* 486 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 489 */     CollectionData collection = collections.getCollectionData(collName);
/* 490 */     if (collection == null)
/*     */     {
/* 495 */       String msg = LocaleUtils.encodeMessage("csArchiverCollectionNotFound", null, collName);
/*     */ 
/* 497 */       throw new DataException(msg);
/*     */     }
/* 499 */     return collection;
/*     */   }
/*     */ 
/*     */   public static String getArchiveName(DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 505 */     return getArchiveParameterMustExist(binder, "aArchiveName");
/*     */   }
/*     */ 
/*     */   public static String getCollectionName(DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 512 */     return getArchiveParameterMustExist(binder, "IDC_Name");
/*     */   }
/*     */ 
/*     */   public static String getArchiveParameterMustExist(DataBinder binder, String key)
/*     */     throws DataException
/*     */   {
/* 518 */     String value = binder.getLocal(key);
/* 519 */     if (value == null)
/*     */     {
/* 521 */       throw new DataException(LocaleUtils.encodeMessage("syParameterNotFound", null, key));
/*     */     }
/*     */ 
/* 524 */     return value;
/*     */   }
/*     */ 
/*     */   public static DataResultSet readBatchFile(String archiveDir, String filename)
/*     */     throws ServiceException, DataException
/*     */   {
/* 535 */     DataBinder data = readBatchData(archiveDir, filename);
/*     */ 
/* 537 */     DataResultSet bset = (DataResultSet)data.getResultSet("ExportResults");
/* 538 */     if (bset == null)
/*     */     {
/* 541 */       throw new ServiceException("!csArchiverNoInfoExists");
/*     */     }
/*     */ 
/* 544 */     return bset.shallowClone();
/*     */   }
/*     */ 
/*     */   public static DataBinder readBatchData(String archiveDir, String filename)
/*     */     throws ServiceException, DataException
/*     */   {
/* 550 */     if ((filename == null) || (filename.length() == 0))
/*     */     {
/* 552 */       throw new DataException("!csArchiverBatchFileNotSpecfied");
/*     */     }
/*     */ 
/* 558 */     String batchPath = FileUtils.getAbsolutePath(archiveDir, filename);
/* 559 */     File batchFile = new File(batchPath);
/* 560 */     if (!batchFile.exists())
/*     */     {
/* 563 */       String msg = LocaleUtils.encodeMessage("csArchiverBatchFileDoesNotExist", null, filename);
/*     */ 
/* 565 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 568 */     long ts = batchFile.lastModified();
/*     */ 
/* 570 */     DataBinder batchDocData = null;
/* 571 */     long curTime = System.currentTimeMillis();
/* 572 */     ResourceCacheInfo cacheInfo = ResourceCacheState.getTemporaryCache(filename, curTime);
/* 573 */     if ((cacheInfo != null) && 
/* 575 */       (ts == cacheInfo.m_lastLoaded))
/*     */     {
/* 577 */       batchDocData = (DataBinder)cacheInfo.m_resourceObj;
/*     */     }
/*     */ 
/* 581 */     if (batchDocData == null)
/*     */     {
/*     */       try
/*     */       {
/* 585 */         String[] batch = breakUpBatchPath(batchPath);
/* 586 */         batchDocData = ResourceUtils.readDataBinder(batch[0], batch[1]);
/* 587 */         addTemporaryResourceCache(filename, batchDocData, ts);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 591 */         String msg = LocaleUtils.encodeMessage("csArchiverBatchFileDoesNotExist", null, filename);
/*     */ 
/* 593 */         throw new ServiceException(msg, e);
/*     */       }
/*     */     }
/*     */ 
/* 597 */     return cloneBatchData(batchDocData);
/*     */   }
/*     */ 
/*     */   public static void addTemporaryResourceCache(String key, DataBinder binder, long ts)
/*     */   {
/* 602 */     ResourceCacheInfo cacheInfo = new ResourceCacheInfo(key);
/* 603 */     cacheInfo.m_resourceObj = binder;
/* 604 */     cacheInfo.m_lastLoaded = ts;
/* 605 */     cacheInfo.m_size = 1000000L;
/* 606 */     long curTime = System.currentTimeMillis();
/* 607 */     ResourceCacheState.addTimedTemporaryCache(key, cacheInfo, curTime);
/*     */   }
/*     */ 
/*     */   public static DataBinder cloneBatchData(DataBinder data)
/*     */   {
/* 613 */     DataBinder clone = new DataBinder();
/* 614 */     DataBinder.mergeHashTables(clone.getLocalData(), data.getLocalData());
/*     */ 
/* 616 */     DataResultSet exptSet = (DataResultSet)data.getResultSet("ExportResults");
/* 617 */     if (exptSet != null)
/*     */     {
/* 619 */       DataResultSet drset = exptSet.shallowClone();
/* 620 */       clone.addResultSet("ExportResults", drset);
/*     */     }
/* 622 */     return clone;
/*     */   }
/*     */ 
/*     */   public static String[] breakUpBatchPath(String archiveDir, String batchFile)
/*     */   {
/* 627 */     String batchPath = FileUtils.getAbsolutePath(archiveDir, batchFile);
/*     */ 
/* 629 */     return breakUpBatchPath(batchPath);
/*     */   }
/*     */ 
/*     */   public static String[] breakUpBatchPath(String batchPath)
/*     */   {
/* 640 */     String dir = FileUtils.getParent(batchPath);
/* 641 */     String name = FileUtils.getName(batchPath);
/*     */ 
/* 643 */     String[] batchInfo = new String[2];
/* 644 */     batchInfo[0] = (FileUtils.getParent(dir) + "/");
/* 645 */     batchInfo[1] = (FileUtils.getName(dir) + "/" + name);
/*     */ 
/* 647 */     return batchInfo;
/*     */   }
/*     */ 
/*     */   public static void deleteBatchFile(String filename, String archiveDir, String archiveExportDir, CollectionData currentCollection)
/*     */     throws ServiceException, DataException
/*     */   {
/* 656 */     boolean isFound = false;
/* 657 */     DataResultSet rset = null;
/* 658 */     FileUtils.reserveDirectory(archiveDir);
/*     */     try
/*     */     {
/* 662 */       DataBinder batchData = readExportsFile(archiveDir, null);
/* 663 */       rset = (DataResultSet)batchData.getResultSet("BatchFiles");
/*     */ 
/* 665 */       if (rset != null)
/*     */       {
/* 668 */         Vector v = rset.findRow(0, filename);
/* 669 */         if (v != null)
/*     */         {
/* 671 */           isFound = true;
/* 672 */           rset.deleteCurrentRow();
/* 673 */           writeExportsFile(archiveDir, batchData);
/*     */         }
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 679 */       FileUtils.releaseDirectory(archiveDir);
/*     */     }
/*     */ 
/* 682 */     String monikerString = currentCollection.getMoniker();
/* 683 */     MonikerWatcher.notifyChanged(monikerString);
/*     */ 
/* 685 */     if (!isFound)
/*     */     {
/* 687 */       return;
/*     */     }
/*     */ 
/* 690 */     deleteAssociatedBatchFiles(filename, archiveExportDir, rset);
/*     */   }
/*     */ 
/*     */   public static void deleteAssociatedBatchFiles(String filename, String archiveExportDir, DataResultSet rset)
/*     */   {
/*     */     try
/*     */     {
/* 701 */       String batchPath = FileUtils.getAbsolutePath(archiveExportDir, filename);
/* 702 */       String[] batchInfo = breakUpBatchPath(batchPath);
/* 703 */       File batchFile = new File(batchPath);
/* 704 */       if (batchFile.exists())
/*     */       {
/* 706 */         batchFile.delete();
/* 707 */         if (SystemUtils.m_verbose)
/*     */         {
/* 709 */           Report.trace("archiver", "Delete batch file '" + batchPath + "'", null);
/*     */         }
/*     */       }
/* 712 */       int endIndex = filename.lastIndexOf("/");
/* 713 */       if (endIndex < 0)
/*     */       {
/* 715 */         return;
/*     */       }
/*     */ 
/* 718 */       String batchDir = filename.substring(0, endIndex + 1);
/*     */ 
/* 721 */       String lookupVal = batchDir;
/* 722 */       ResultSetFilter filter = new ResultSetFilter(lookupVal)
/*     */       {
/*     */         public int checkRow(String val, int curNumRows, Vector row)
/*     */         {
/* 726 */           if (val.startsWith(this.val$lookupVal))
/*     */           {
/* 728 */             return 1;
/*     */           }
/* 730 */           return 0;
/*     */         }
/*     */       };
/* 734 */       DataResultSet expSet = new DataResultSet();
/* 735 */       expSet.copyFiltered(rset, "aBatchFile", filter);
/* 736 */       if (expSet.isEmpty())
/*     */       {
/* 739 */         File d = new File(batchInfo[0], batchDir);
/* 740 */         FileUtils.deleteDirectory(d, true);
/* 741 */         if (SystemUtils.m_verbose)
/*     */         {
/* 743 */           Report.trace("archiver", "Delete batch directory '" + d.getPath() + "'", null);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 749 */         int beginIndex = filename.lastIndexOf("~");
/* 750 */         endIndex = filename.lastIndexOf(".");
/* 751 */         if ((beginIndex < 0) || (endIndex < 0) || (endIndex <= beginIndex + 1))
/*     */         {
/* 753 */           return;
/*     */         }
/*     */ 
/* 756 */         String dirStubName = batchInfo[0] + batchDir + filename.substring(beginIndex + 1, endIndex);
/* 757 */         File dir = new File(dirStubName);
/* 758 */         FileUtils.deleteDirectory(dir, true);
/* 759 */         if (SystemUtils.m_verbose)
/*     */         {
/* 761 */           Report.trace("archiver", "Delete batch directory '" + dir + "'", null);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 767 */       String msg = LocaleUtils.encodeMessage("csArchiverUnableToDeleteDirectories", null, filename);
/*     */ 
/* 769 */       Report.appWarning("archiver", null, msg, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void doMergeInFileInfo(MergeInFileData data, String dID, String status)
/*     */     throws DataException, ServiceException
/*     */   {
/* 776 */     data.getClass(); String strIsPrimary = data.m_docSet.getStringValue(data.m_docFieldInfos[0].m_index);
/*     */ 
/* 778 */     data.m_isPrimary = StringUtils.convertToBool(strIsPrimary, false);
/*     */ 
/* 780 */     data.getClass(); String strIsWebFormat = data.m_docSet.getStringValue(data.m_docFieldInfos[1].m_index);
/*     */ 
/* 782 */     data.m_isWebFormat = StringUtils.convertToBool(strIsWebFormat, false);
/*     */ 
/* 784 */     data.m_isInVault = true;
/* 785 */     if (data.m_isWebFormat)
/*     */     {
/* 787 */       data.m_isInVault = false;
/* 788 */       data.m_extension = data.m_docData.getLocal("dWebExtension");
/*     */     }
/*     */     else
/*     */     {
/* 792 */       data.getClass(); data.m_extension = data.m_docSet.getStringValue(data.m_docFieldInfos[2].m_index);
/*     */     }
/*     */ 
/* 796 */     data.getClass(); data.m_format = data.m_docSet.getStringValue(data.m_docFieldInfos[4].m_index);
/*     */ 
/* 798 */     data.getClass(); data.m_originalName = data.m_docSet.getStringValue(data.m_docFieldInfos[3].m_index);
/*     */ 
/* 801 */     data.m_didCalculateFileNames = false;
/* 802 */     data.m_didCalculateDirectoryNames = false;
/* 803 */     data.m_didAddFileInformationToDatabase = false;
/* 804 */     data.m_didCreateSpecificArchiveDirectory = false;
/* 805 */     data.m_didCreateSourceFileDescriptor = false;
/*     */ 
/* 807 */     data.m_cxt.setCachedObject("mergeInFileInfoData", data);
/* 808 */     if (PluginFilters.filter("mergeInFileInfo", data.m_workspace, data.m_archiveData, data.m_cxt) == 1)
/*     */     {
/* 811 */       return;
/*     */     }
/*     */ 
/* 814 */     if (!data.m_didCalculateFileNames)
/*     */     {
/* 816 */       if (data.m_isWebFormat)
/*     */       {
/* 819 */         if ((!data.m_isCopyWebDocs) || (status.equals("GENWWW")))
/*     */         {
/* 821 */           return;
/*     */         }
/*     */ 
/* 824 */         String revlabel = data.m_batchSet.getStringValue(data.m_revInfoFields[6].m_index);
/* 825 */         String docName = data.m_batchSet.getStringValue(data.m_revInfoFields[1].m_index);
/* 826 */         data.m_newFileName = (docName + "~" + revlabel);
/*     */ 
/* 828 */         data.m_fileName = (docName + "~" + revlabel);
/*     */       }
/*     */       else
/*     */       {
/* 832 */         data.m_fileName = dID;
/* 833 */         data.m_newFileName = data.m_fileName;
/*     */       }
/*     */ 
/* 837 */       if ((data.m_extension != null) && (data.m_extension.length() > 0))
/*     */       {
/* 839 */         data.m_fileName = (data.m_fileName + "." + data.m_extension);
/* 840 */         data.m_newFileName = (data.m_newFileName + "." + data.m_extension);
/*     */       }
/* 842 */       data.m_fileName = data.m_fileName.toLowerCase();
/* 843 */       data.m_newFileName = data.m_newFileName.toLowerCase();
/*     */ 
/* 845 */       data.m_didCalculateFileNames = true;
/*     */     }
/*     */ 
/* 849 */     if (!data.m_didCalculateDirectoryNames)
/*     */     {
/* 853 */       String relBatchDocDirSuffix = null;
/* 854 */       if (data.m_isInVault)
/*     */       {
/* 856 */         String relDocVaultDir = LegacyDocumentPathBuilder.computeRelativeVaultDir(data.m_batchData);
/*     */ 
/* 858 */         relBatchDocDirSuffix = "/vault/" + relDocVaultDir;
/*     */       }
/*     */       else
/*     */       {
/* 862 */         relBatchDocDirSuffix = "/weblayout/" + LegacyDocumentPathBuilder.computeWebDirPartialPath(data.m_batchData);
/*     */       }
/*     */ 
/* 866 */       String relBatchDir = data.m_tsDir + relBatchDocDirSuffix;
/* 867 */       data.m_relativeNewFilePath = (relBatchDir + data.m_newFileName);
/* 868 */       data.m_relativeBatchDocDirSuffix = relBatchDocDirSuffix;
/*     */ 
/* 870 */       data.m_didCalculateDirectoryNames = true;
/*     */     }
/*     */ 
/* 873 */     if (!data.m_didAddFileInformationToDatabase)
/*     */     {
/* 875 */       if (!data.m_isWebFormat)
/*     */       {
/* 878 */         if (data.m_isPrimary)
/*     */         {
/* 880 */           data.m_batchSet.setCurrentValue(data.m_revInfoFields[7].m_index, data.m_relativeNewFilePath);
/*     */ 
/* 882 */           data.m_batchSet.setCurrentValue(data.m_revInfoFields[9].m_index, data.m_originalName);
/*     */ 
/* 886 */           data.m_batchSet.setCurrentValue(data.m_revInfoFields[12].m_index, data.m_format);
/*     */         }
/*     */         else
/*     */         {
/* 891 */           data.m_batchSet.setCurrentValue(data.m_revInfoFields[8].m_index, data.m_relativeNewFilePath);
/*     */ 
/* 893 */           data.m_batchSet.setCurrentValue(data.m_revInfoFields[13].m_index, data.m_format);
/*     */ 
/* 895 */           data.m_batchSet.setCurrentValue(data.m_revInfoFields[15].m_index, data.m_originalName);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 901 */         data.m_batchSet.setCurrentValue(data.m_revInfoFields[10].m_index, data.m_relativeNewFilePath);
/*     */ 
/* 903 */         data.m_batchSet.setCurrentValue(data.m_revInfoFields[11].m_index, data.m_extension);
/*     */ 
/* 905 */         data.m_batchSet.setCurrentValue(data.m_revInfoFields[14].m_index, data.m_format);
/*     */ 
/* 907 */         data.m_batchSet.setCurrentValue(data.m_revInfoFields[16].m_index, data.m_originalName);
/*     */       }
/*     */ 
/* 911 */       data.m_didAddFileInformationToDatabase = true;
/*     */     }
/*     */ 
/* 914 */     if (!data.m_didCreateSpecificArchiveDirectory)
/*     */     {
/* 916 */       if (SystemUtils.m_verbose)
/*     */       {
/* 918 */         Report.trace("archiver", "Checking archive directory (creating if missing): " + data.m_archiveExportDir + data.m_tsDir + "/" + data.m_relativeBatchDocDirSuffix, null);
/*     */       }
/* 920 */       FileUtils.checkOrCreateSubDirectory(data.m_archiveExportDir, data.m_tsDir + '/' + data.m_relativeBatchDocDirSuffix);
/*     */ 
/* 922 */       data.m_didCreateSpecificArchiveDirectory = true;
/*     */     }
/*     */ 
/* 925 */     if (data.m_didCopyFile) {
/*     */       return;
/*     */     }
/* 928 */     if (data.m_doSimpleFileCopy)
/*     */     {
/* 930 */       if (SystemUtils.m_verbose)
/*     */       {
/* 932 */         Report.trace("archiver", "Creating archived file: " + data.m_archiveExportDir + data.m_relativeNewFilePath, null);
/*     */       }
/* 934 */       FileUtils.copyFile(data.m_filePath, data.m_archiveExportDir + data.m_relativeNewFilePath);
/*     */     }
/*     */     else
/*     */     {
/* 938 */       boolean doSupportFallbackPath = SharedObjects.getEnvValueAsBoolean("FileStoreSupportFallbackPath", true);
/* 939 */       Map args = null;
/* 940 */       if (!data.m_didCreateSourceFileDescriptor)
/*     */       {
/* 942 */         String rendition = "webViewableFile";
/* 943 */         if (data.m_isInVault)
/*     */         {
/* 945 */           if (data.m_isPrimary)
/*     */           {
/* 947 */             rendition = "primaryFile";
/*     */           }
/*     */           else
/*     */           {
/* 951 */             rendition = "alternateFile";
/*     */           }
/*     */ 
/*     */         }
/* 956 */         else if (doSupportFallbackPath == true)
/*     */         {
/* 958 */           args = new HashMap();
/* 959 */           args.put("FileStoreSupportFallbackPath", "1");
/*     */         }
/*     */ 
/* 962 */         data.m_docData.putLocal("RenditionId", rendition);
/* 963 */         data.m_sourceDescriptor = data.m_fileStore.createDescriptor(data.m_docData, args, data.m_cxt);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 968 */         if (SystemUtils.m_verbose)
/*     */         {
/* 970 */           Report.trace("archiver", "Creating archived file: " + data.m_archiveExportDir + data.m_relativeNewFilePath, null);
/*     */         }
/* 972 */         data.m_fileStore.copyToLocalFile(data.m_sourceDescriptor, new File(data.m_archiveExportDir + data.m_relativeNewFilePath), args);
/*     */ 
/* 975 */         if (args != null)
/*     */         {
/* 977 */           boolean handledFallbackPath = StringUtils.convertToBool((String)args.get("FileStoreHandledFallbackPath"), false);
/* 978 */           if (handledFallbackPath == true)
/*     */           {
/* 980 */             String msg = LocaleUtils.encodeMessage("csArchiverFallbackPathUsed", null, data.m_sourceDescriptor.get("fallbackPath"), data.m_sourceDescriptor.get("path"));
/* 981 */             Report.appInfo("archiver", null, msg, null);
/* 982 */             Report.trace("archiver", msg, null);
/*     */           }
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 988 */         throw new ServiceException(e);
/*     */       }
/*     */     }
/*     */ 
/* 992 */     data.m_didCopyFile = true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 998 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97046 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiveUtils
 * JD-Core Version:    0.5.4
 */