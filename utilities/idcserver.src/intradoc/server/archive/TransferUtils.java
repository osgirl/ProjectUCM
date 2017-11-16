/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.provider.OutgoingProvider;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.ServerRequestUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.ProxiedMonikerWatcher;
/*     */ import intradoc.server.proxy.OutgoingProviderMonitor;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.util.IdcVector;
/*     */ import intradoc.zip.ZipFunctions;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TransferUtils
/*     */ {
/*  38 */   protected static Hashtable m_transfers = new Hashtable();
/*     */ 
/*     */   public static void notifyOfTransfer(String srcPath)
/*     */   {
/*  42 */     TransferInfo info = (TransferInfo)m_transfers.get(srcPath);
/*  43 */     if (info == null)
/*     */       return;
/*  45 */     synchronized (info)
/*     */     {
/*  47 */       info.notify();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void addTransferInfo(String srcPath, TransferInfo info)
/*     */   {
/*  54 */     m_transfers.put(srcPath, info);
/*     */   }
/*     */ 
/*     */   public static TransferInfo getTransferInfo(String srcPath)
/*     */   {
/*  59 */     return (TransferInfo)m_transfers.get(srcPath);
/*     */   }
/*     */ 
/*     */   public static void removeTransfer(String srcPath)
/*     */   {
/*  64 */     m_transfers.remove(srcPath);
/*     */   }
/*     */ 
/*     */   public static String createPackage(DataBinder binder, String archiveDir, boolean isLocal, String targetDir, String filename, ArchiveImportStateInformation importStateInformation)
/*     */     throws DataException, ServiceException
/*     */   {
/*  70 */     DataResultSet fileSet = new DataResultSet(ZipFunctions.ZIP_FILE_COLUMNS);
/*     */ 
/*  76 */     String batchFile = binder.getLocal("aBatchFile");
/*  77 */     String[] batchDirs = ArchiveUtils.breakUpBatchPath(archiveDir, batchFile);
/*  78 */     archiveDir = batchDirs[0];
/*  79 */     String relBatchFile = batchDirs[1];
/*     */ 
/*  81 */     String batchDir = "";
/*  82 */     int index = relBatchFile.lastIndexOf(47);
/*  83 */     if (index >= 0)
/*     */     {
/*  85 */       batchDir = relBatchFile.substring(0, index + 1);
/*     */     }
/*     */ 
/*  91 */     String state = binder.getLocal("aState");
/*  92 */     state = state.toLowerCase();
/*  93 */     boolean isDeleteBatch = state.indexOf("delete") > 0;
/*  94 */     boolean isTableArchive = relBatchFile.indexOf("_arTable~") > 0;
/*     */ 
/*  96 */     Vector row = new IdcVector();
/*  97 */     row.addElement(relBatchFile);
/*  98 */     row.addElement(archiveDir + relBatchFile);
/*  99 */     fileSet.addRow(row);
/*     */ 
/* 101 */     if (!isDeleteBatch)
/*     */     {
/* 104 */       DataResultSet batchSet = ArchiveUtils.readBatchFile(archiveDir, relBatchFile);
/*     */ 
/* 107 */       index = relBatchFile.lastIndexOf(126);
/* 108 */       if ((index < 0) || (relBatchFile.length() < index + 1))
/*     */       {
/* 111 */         String msg = LocaleUtils.encodeMessage("csArchiverBatchFileError", null, batchFile);
/*     */ 
/* 113 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/* 118 */       if ((!isTableArchive) && (!batchSet.isEmpty()))
/*     */       {
/* 120 */         String assocDir = relBatchFile.substring(index + 1);
/* 121 */         index = assocDir.indexOf(46);
/* 122 */         assocDir = assocDir.substring(0, index);
/*     */ 
/* 125 */         createFileInfos(fileSet, archiveDir, batchDir, assocDir);
/*     */       }
/*     */ 
/* 129 */       FieldInfo[] infos = ResultSetUtils.createInfoList(batchSet, importStateInformation.m_allFileFields, false);
/*     */ 
/* 131 */       int num = infos.length;
/* 132 */       int count = 0;
/* 133 */       for (; batchSet.isRowPresent(); ++count)
/*     */       {
/* 135 */         for (int i = 0; i < num; ++i)
/*     */         {
/* 137 */           index = infos[i].m_index;
/* 138 */           if (index < 0) {
/*     */             continue;
/*     */           }
/*     */ 
/* 142 */           String fileStr = batchSet.getStringValue(index);
/* 143 */           if (fileStr.length() == 0)
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 149 */           File file = new File(archiveDir + fileStr);
/* 150 */           if (file.exists())
/*     */             continue;
/* 152 */           String msg = LocaleUtils.encodeMessage("csArchiverBatchFileFileMissing", null, batchFile, fileStr);
/*     */ 
/* 154 */           throw new ServiceException(msg);
/*     */         }
/* 133 */         batchSet.next();
/*     */       }
/*     */ 
/* 160 */       binder.putLocal("aNumDocuments", String.valueOf(count));
/* 161 */       binder.putLocal("aBatchFile", relBatchFile);
/*     */     }
/*     */ 
/* 165 */     if (!isTableArchive)
/*     */     {
/* 167 */       Vector v = new IdcVector();
/* 168 */       v.addElement(batchDir + "docmetadefinition.hda");
/* 169 */       v.addElement(archiveDir + batchDir + "docmetadefinition.hda");
/* 170 */       fileSet.addRow(v);
/*     */     }
/*     */ 
/* 173 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 174 */     cxt.setCachedObject("archiveDir", archiveDir);
/* 175 */     if (targetDir != null)
/*     */     {
/* 177 */       cxt.setCachedObject("targetDir", targetDir);
/*     */     }
/* 179 */     if (filename != null)
/*     */     {
/* 181 */       cxt.setCachedObject("fileName", filename);
/*     */     }
/* 183 */     cxt.setCachedObject("batchDir", batchDir);
/* 184 */     cxt.setCachedObject("fileSet", fileSet);
/* 185 */     cxt.setCachedObject("importState", importStateInformation);
/* 186 */     PluginFilters.filter("afterCreateTransferPackageFileSet", null, binder, cxt);
/*     */ 
/* 189 */     String tempDir = null;
/* 190 */     String zipName = null;
/* 191 */     if (isLocal)
/*     */     {
/* 193 */       tempDir = targetDir + "temp/";
/* 194 */       FileUtils.checkOrCreateDirectory(tempDir, 1);
/* 195 */       zipName = tempDir + filename + ".zip";
/*     */     }
/*     */     else
/*     */     {
/* 199 */       tempDir = DataBinder.getTemporaryDirectory();
/* 200 */       zipName = tempDir + DataBinder.getNextFileCounter() + ".zip";
/*     */     }
/*     */ 
/* 203 */     ZipFunctions.createZipFile(zipName, fileSet);
/*     */ 
/* 205 */     return zipName;
/*     */   }
/*     */ 
/*     */   protected static void createFileInfos(DataResultSet fileSet, String archiveDir, String batchDir, String assocDir)
/*     */     throws DataException, ServiceException
/*     */   {
/* 211 */     String filePath = archiveDir + batchDir + assocDir;
/* 212 */     String name = batchDir + assocDir;
/*     */ 
/* 214 */     addFileToPackage(fileSet, name, filePath);
/*     */   }
/*     */ 
/*     */   protected static void addFileToPackage(DataResultSet fileSet, String name, String filePath)
/*     */     throws ServiceException
/*     */   {
/* 220 */     if (FileUtils.checkFile(filePath, true, false) == 0)
/*     */     {
/* 223 */       name = FileUtils.directorySlashesEx(name, false);
/* 224 */       filePath = FileUtils.directorySlashesEx(filePath, false);
/*     */ 
/* 226 */       Vector row = new IdcVector();
/* 227 */       row.addElement(name);
/* 228 */       row.addElement(filePath);
/* 229 */       fileSet.addRow(row);
/*     */     }
/* 231 */     else if (FileUtils.checkFile(filePath, false, false) == 0)
/*     */     {
/* 234 */       name = FileUtils.directorySlashes(name);
/* 235 */       filePath = FileUtils.directorySlashes(filePath);
/*     */ 
/* 237 */       File directory = new File(filePath);
/* 238 */       String[] files = directory.list();
/* 239 */       for (int i = 0; i < files.length; ++i)
/*     */       {
/* 241 */         addFileToPackage(fileSet, name + files[i], filePath + files[i]);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 246 */       String msg = LocaleUtils.encodeMessage("csArchiverResourceMissing", null, filePath);
/*     */ 
/* 248 */       throw new ServiceException(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void receiveTransfer(DataBinder binder, CollectionData targetCollection, String targetArchive, boolean isProxiedTarget)
/*     */     throws DataException, ServiceException
/*     */   {
/* 255 */     String targetDir = ArchiveUtils.buildArchiveDirectory(targetCollection.m_exportLocation, targetArchive);
/* 256 */     boolean isDelay = StringUtils.convertToBool(binder.getLocal("IsDelayUpload"), false);
/* 257 */     if ((isDelay) || (binder.m_isSuspended))
/*     */     {
/*     */       try
/*     */       {
/* 261 */         FileUtils.checkOrCreateDirectory(targetDir + "/temp/", 0);
/* 262 */         binder.setOverrideTemporaryDirectory(targetDir + "/temp/");
/*     */ 
/* 264 */         DataSerializeUtils.continueParse(binder, null);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 268 */         throw new DataException(e.getMessage());
/*     */       }
/*     */     }
/*     */ 
/* 272 */     if (isProxiedTarget)
/*     */     {
/* 274 */       submitTransferWork(binder, targetCollection, targetArchive);
/*     */     }
/*     */     else
/*     */     {
/* 278 */       doTransferWork(binder, targetCollection, targetArchive);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void submitTransferWork(DataBinder binder, CollectionData targetCollection, String targetArchive)
/*     */     throws DataException, ServiceException
/*     */   {
/* 285 */     String targetDir = ArchiveUtils.buildArchiveDirectory(targetCollection.m_exportLocation, targetArchive);
/*     */ 
/* 287 */     String srcCollection = binder.get("SourceCollection");
/* 288 */     String srcArchive = binder.get("SourceArchive");
/* 289 */     String batchFile = binder.get("aBatchFile");
/*     */ 
/* 291 */     String srcID = srcCollection + "/" + srcArchive + "/" + batchFile;
/* 292 */     binder.putLocal("aSourceID", srcID);
/*     */ 
/* 294 */     String lockDir = LegacyDirectoryLocator.getCollectionsDirectory();
/* 295 */     FileUtils.reserveDirectory(lockDir, true);
/*     */     try
/*     */     {
/* 299 */       DataBinder transferData = ArchiveUtils.readTransferData(false);
/* 300 */       DataResultSet transferSet = null;
/* 301 */       boolean isNew = true;
/* 302 */       if (transferData != null)
/*     */       {
/* 305 */         transferSet = (DataResultSet)transferData.getResultSet("Transfers");
/*     */       }
/*     */       else
/*     */       {
/* 309 */         transferData = new DataBinder();
/*     */       }
/*     */ 
/* 312 */       if (transferSet != null)
/*     */       {
/* 314 */         Vector row = transferSet.findRow(0, srcID);
/* 315 */         isNew = row == null;
/*     */       }
/*     */ 
/* 318 */       if (!isNew)
/*     */       {
/* 320 */         String status = ResultSetUtils.getValue(transferSet, "aTransferStatus");
/* 321 */         String msg = LocaleUtils.encodeMessage("csArchiverAlreadyTransfered", null, status);
/*     */ 
/* 323 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/* 327 */       String zipPath = binder.get("ZipFile:path");
/*     */ 
/* 330 */       String batchName = computeBatchName(batchFile);
/* 331 */       FileUtils.renameFile(zipPath, targetDir + "/temp/" + batchName + ".zip");
/*     */ 
/* 335 */       String idcName = binder.getAllowMissing("aIDC_Name");
/* 336 */       if (idcName != null)
/*     */       {
/* 338 */         binder.putLocal("IDC_Name", idcName);
/*     */       }
/*     */ 
/* 342 */       binder.putLocal("aTransferStatus", "working");
/*     */ 
/* 344 */       checkAndCreateTransferRow(transferData, binder);
/*     */     }
/*     */     finally
/*     */     {
/* 348 */       FileUtils.releaseDirectory(lockDir, true);
/*     */ 
/* 351 */       TransferMonitor.checkInit();
/* 352 */       TransferMonitor.notifyOfWork();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void doTransferWork(DataBinder binder, CollectionData targetCollection, String targetArchive)
/*     */     throws DataException, ServiceException
/*     */   {
/* 360 */     String targetDir = ArchiveUtils.buildArchiveDirectory(targetCollection.m_location, targetArchive);
/* 361 */     String targetExportDir = ArchiveUtils.buildArchiveDirectory(targetCollection.m_exportLocation, targetArchive);
/* 362 */     String zipPath = binder.get("ZipFile:path");
/* 363 */     ZipFunctions.extractZipFiles(zipPath, targetExportDir);
/*     */ 
/* 365 */     FileUtils.reserveDirectory(targetCollection.m_location);
/*     */     try
/*     */     {
/* 369 */       DataBinder exportData = ArchiveUtils.readExportsFile(targetDir, null);
/* 370 */       DataResultSet exportSet = (DataResultSet)exportData.getResultSet("BatchFiles");
/* 371 */       if (exportSet == null)
/*     */       {
/* 373 */         exportSet = new DataResultSet(ArchiveUtils.BATCHFILE_COLUMNS);
/* 374 */         exportData.addResultSet("BatchFiles", exportSet);
/*     */       }
/*     */ 
/* 378 */       validateAndFixBatchColumns(binder);
/*     */ 
/* 380 */       Vector row = exportSet.createRow(binder);
/* 381 */       exportSet.addRow(row);
/*     */ 
/* 383 */       ArchiveUtils.writeExportsFile(targetDir, exportData);
/*     */     }
/*     */     finally
/*     */     {
/* 387 */       FileUtils.releaseDirectory(targetCollection.m_location);
/* 388 */       FileUtils.deleteFile(zipPath);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void validateAndFixBatchColumns(Parameters params)
/*     */   {
/* 395 */     int len = ArchiveUtils.BATCHFILE_COLUMN_DEFAULTS.length;
/* 396 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 398 */       String key = ArchiveUtils.BATCHFILE_COLUMN_DEFAULTS[i][0];
/*     */       try
/*     */       {
/* 401 */         params.get(key);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 405 */         String val = ArchiveUtils.BATCHFILE_COLUMN_DEFAULTS[i][1];
/* 406 */         if (params instanceof PropParameters)
/*     */         {
/* 408 */           PropParameters pp = (PropParameters)params;
/* 409 */           pp.m_properties.put(key, val);
/*     */         }
/* 411 */         else if (params instanceof DataBinder)
/*     */         {
/* 413 */           DataBinder binder = (DataBinder)params;
/* 414 */           binder.putLocal(key, val);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void checkAndCreateTransferRow(DataBinder transferData, DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/* 427 */     DataResultSet transferSet = (DataResultSet)transferData.getResultSet("Transfers");
/* 428 */     if (transferSet == null)
/*     */     {
/* 430 */       transferSet = new DataResultSet(new String[] { "aSourceID", "aSourcePath", "aTargetPath", "aTransferOwner", "aTransferType", "aHasConnection", "aTransferStatus", "aCreateTransferTs", "aLastTransferTs", "aTransferErrorMsg", "aBatchFile", "aState", "IDC_Name", "aNumDocuments", "aIsTableBatch" });
/*     */ 
/* 433 */       transferData.addResultSet("Transfers", transferSet);
/*     */     }
/*     */     else
/*     */     {
/* 437 */       FieldInfo fi = new FieldInfo();
/* 438 */       Vector finfo = new IdcVector();
/* 439 */       fi.m_name = "aIsTableBatch";
/* 440 */       finfo.addElement(fi);
/* 441 */       transferSet.mergeFieldsWithFlags(finfo, 0);
/*     */     }
/*     */ 
/* 444 */     if (binder.getAllowMissing("aIsTableBatch") == null)
/*     */     {
/* 446 */       binder.putLocal("aIsTableBatch", "0");
/*     */     }
/*     */ 
/* 449 */     Vector row = transferSet.createRow(binder);
/* 450 */     transferSet.addRow(row);
/*     */ 
/* 452 */     ArchiveUtils.writeTransferData(transferData);
/*     */   }
/*     */ 
/*     */   public static String computeBatchName(String batchFile)
/*     */   {
/* 457 */     String batchName = null;
/* 458 */     int index = batchFile.lastIndexOf(47);
/* 459 */     if (index >= 0)
/*     */     {
/* 461 */       batchName = batchFile.substring(index + 1);
/*     */     }
/*     */     else
/*     */     {
/* 465 */       batchName = batchFile;
/*     */     }
/* 467 */     index = batchName.indexOf(46);
/* 468 */     if (index >= 0)
/*     */     {
/* 470 */       batchName = batchName.substring(0, index);
/*     */     }
/* 472 */     return batchName;
/*     */   }
/*     */ 
/*     */   public static void updateTransferInfo(TransferInfo info, boolean isReset)
/*     */   {
/* 481 */     Properties props = info.m_properties;
/* 482 */     String collDir = DirectoryLocator.getCollectionsDirectory();
/* 483 */     String id = props.getProperty("aSourceID");
/*     */     try
/*     */     {
/* 486 */       FileUtils.reserveDirectory(collDir);
/*     */ 
/* 488 */       DataBinder transferData = ArchiveUtils.readTransferData(false);
/* 489 */       DataResultSet transferSet = (DataResultSet)transferData.getResultSet("Transfers");
/* 490 */       if (transferSet != null)
/*     */       {
/* 492 */         Vector row = transferSet.findRow(0, id);
/* 493 */         if (row != null)
/*     */         {
/* 495 */           transferSet.deleteCurrentRow();
/*     */         }
/*     */       }
/*     */ 
/* 499 */       String srcPath = props.getProperty("aSourcePath");
/* 500 */       if (isReset)
/*     */       {
/* 502 */         transferData.removeLocal(srcPath + ":lastDone");
/* 503 */         transferData.removeLocal(srcPath + ":lastStatus");
/* 504 */         transferData.removeLocal(srcPath + ":lastTransferTs");
/* 505 */         transferData.removeLocal(srcPath + ":lastTransferError");
/*     */       }
/*     */       else
/*     */       {
/* 509 */         String batchFile = props.getProperty("aBatchFile");
/* 510 */         String status = props.getProperty("aTransferStatus");
/*     */ 
/* 512 */         transferData.putLocal(srcPath + ":lastDone", batchFile);
/* 513 */         transferData.putLocal(srcPath + ":lastStatus", status);
/* 514 */         transferData.putLocal(srcPath + ":lastTransferTs", LocaleUtils.formatODBC(new Date()));
/* 515 */         if (status.equals("failed"))
/*     */         {
/* 517 */           String errMsg = props.getProperty("aTransferErrorMsg");
/* 518 */           if (errMsg == null)
/*     */           {
/* 520 */             errMsg = "Unknown error.";
/*     */           }
/* 522 */           transferData.putLocal(srcPath + ":lastTransferError", errMsg);
/*     */         }
/*     */         else
/*     */         {
/* 526 */           transferData.removeLocal(srcPath + ":lastTransferError");
/*     */         }
/*     */       }
/* 529 */       ArchiveUtils.writeTransferData(transferData);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 534 */       String msg = LocaleUtils.encodeMessage("csArchiverUnableToUpdate", null, id);
/*     */ 
/* 536 */       Report.appError("archiver", null, msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 540 */       FileUtils.releaseDirectory(collDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void executeProxiedRequest(String psName, String request, DataBinder binder, DataBinder outBinder, ExecutionContext ctxt, ReportProgress rp)
/*     */     throws DataException, ServiceException
/*     */   {
/* 551 */     Provider provider = OutgoingProviderMonitor.getOutgoingProvider(psName);
/* 552 */     if (provider == null)
/*     */     {
/* 554 */       String msg = LocaleUtils.encodeMessage("csProxiedServerNotAvailable", null, psName);
/*     */ 
/* 556 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 559 */     OutgoingProvider sop = (OutgoingProvider)provider.getProvider();
/* 560 */     Properties props = provider.getProviderState();
/* 561 */     boolean isOnDemand = StringUtils.convertToBool(props.getProperty("IsOnDemand"), false);
/* 562 */     if ((isOnDemand) && 
/* 564 */       (!sop.isStarted()))
/*     */     {
/* 566 */       provider.startProvider();
/*     */     }
/*     */ 
/* 570 */     binder.putLocal("IdcService", request);
/*     */ 
/* 572 */     boolean doMerge = false;
/* 573 */     if (outBinder == null)
/*     */     {
/* 575 */       outBinder = new DataBinder();
/* 576 */       doMerge = true;
/*     */     }
/*     */ 
/* 581 */     String[] keys = { "monitoredSubjects", "changedSubjects", "refreshSubjects", "forceRefreshSubjects", "monitoredTopics", "changedTopics", "refreshTopics" };
/*     */ 
/* 583 */     Properties savedProps = new Properties();
/* 584 */     int num = keys.length;
/* 585 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 587 */       String str = binder.getLocal(keys[i]);
/* 588 */       if (str == null)
/*     */         continue;
/* 590 */       savedProps.put(keys[i], str);
/* 591 */       binder.removeLocal(keys[i]);
/*     */     }
/*     */ 
/* 596 */     ProxiedMonikerWatcher.startSynchronization(psName, binder, ctxt);
/*     */     try
/*     */     {
/* 600 */       binder.m_isCgi = false;
/* 601 */       ServerRequestUtils.doAdminProxyRequestEx(provider, binder, outBinder, ctxt, rp);
/*     */     }
/*     */     finally
/*     */     {
/* 605 */       ProxiedMonikerWatcher.finishSynchronization(psName, outBinder, ctxt);
/* 606 */       DataBinder.mergeHashTables(binder.getLocalData(), savedProps);
/*     */     }
/*     */ 
/* 609 */     if (doMerge)
/*     */     {
/* 611 */       binder.merge(outBinder);
/*     */     }
/*     */ 
/* 614 */     String errCode = outBinder.getLocal("StatusCode");
/* 615 */     String errMessage = outBinder.getLocal("StatusMessage");
/* 616 */     if (errCode == null)
/*     */       return;
/* 618 */     int code = NumberUtils.parseInteger(errCode, -1);
/* 619 */     if (code >= 0)
/*     */       return;
/* 621 */     if (errMessage == null)
/*     */     {
/* 623 */       errMessage = "";
/*     */     }
/* 625 */     throw new ServiceException(code, errMessage);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 632 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98955 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.TransferUtils
 * JD-Core Version:    0.5.4
 */