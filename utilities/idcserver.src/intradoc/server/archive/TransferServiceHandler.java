/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.MonikerWatcher;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHandler;
/*     */ import intradoc.server.proxy.OutgoingProviderMonitor;
/*     */ import intradoc.shared.ArchiveCollections;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TransferServiceHandler extends ServiceHandler
/*     */ {
/*     */   public ArchiveImportStateInformation m_importStateInfo;
/*     */ 
/*     */   public TransferServiceHandler()
/*     */   {
/*  37 */     this.m_importStateInfo = null;
/*     */   }
/*     */ 
/*     */   public void init(Service service) throws ServiceException, DataException
/*     */   {
/*  42 */     super.init(service);
/*  43 */     Object o = service.getCachedObject("importState");
/*  44 */     if ((o != null) && (o instanceof ArchiveImportStateInformation))
/*     */     {
/*  46 */       this.m_importStateInfo = ((ArchiveImportStateInformation)o);
/*     */     }
/*     */     else
/*     */     {
/*  50 */       this.m_importStateInfo = ((ArchiveImportStateInformation)ComponentClassFactory.createClassInstance("ArchiveImportStateInformation", "intradoc.server.archive.ArchiveImportStateInformation", ""));
/*     */ 
/*  52 */       this.m_importStateInfo.init(this.m_binder, this.m_workspace, service);
/*  53 */       this.m_service.setCachedObject("importInfo", this.m_importStateInfo);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getProxiedArchiveCollections() throws DataException, ServiceException
/*     */   {
/*  60 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  61 */     String psName = this.m_binder.get("psIDC_Name");
/*     */ 
/*  63 */     boolean isLocal = idcName.equals(psName);
/*  64 */     ResultSet drset = null;
/*  65 */     if (isLocal)
/*     */     {
/*  67 */       drset = SharedObjects.getTable(ArchiveCollections.m_tableName);
/*     */     }
/*     */     else
/*     */     {
/*  71 */       TransferUtils.executeProxiedRequest(psName, "GET_ARCHIVECOLLECTIONS", this.m_binder, this.m_binder, this.m_service, null);
/*     */ 
/*  73 */       drset = this.m_binder.getResultSet(ArchiveCollections.m_tableName);
/*  74 */       if (drset != null)
/*     */       {
/*  76 */         this.m_binder.removeResultSet(ArchiveCollections.m_tableName);
/*     */       }
/*     */     }
/*  79 */     if (drset == null)
/*     */       return;
/*  81 */     this.m_binder.addResultSet("ProxiedCollections", drset);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void receiveTransfer()
/*     */     throws DataException, ServiceException
/*     */   {
/*  91 */     String targetCollectionName = this.m_binder.get("TargetCollection");
/*  92 */     String targetArchive = this.m_binder.get("TargetArchive");
/*     */ 
/*  94 */     CollectionData targetCollection = ArchiveUtils.getCollection(targetCollectionName);
/*  95 */     TransferUtils.receiveTransfer(this.m_binder, targetCollection, targetArchive, true);
/*     */ 
/*  97 */     updateTransferTotals(true, targetCollection, targetArchive, true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void retrieveTargetInformation() throws DataException, ServiceException
/*     */   {
/* 103 */     String targetCollection = this.m_binder.get("TargetCollection");
/* 104 */     String targetArchive = this.m_binder.get("TargetArchive");
/* 105 */     CollectionData targetCollectionData = ArchiveUtils.getCollection(targetCollection);
/*     */ 
/* 107 */     if (targetCollectionData == null)
/*     */     {
/* 109 */       String msg = LocaleUtils.encodeMessage("csArchiverTargetCollectionNotDefined", null, targetCollection);
/*     */ 
/* 111 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 115 */     DataBinder targetArchiveData = ArchiveUtils.readArchiveFile(targetCollectionData.m_location, targetArchive, true);
/* 116 */     this.m_binder.merge(targetArchiveData);
/*     */ 
/* 119 */     checkForTransferOwner();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void retrieveTargetTransferStatus()
/*     */     throws DataException, ServiceException
/*     */   {
/* 128 */     boolean isCheckTransfer = StringUtils.convertToBool(this.m_binder.getLocal("IsCheckTransfer"), false);
/*     */ 
/* 130 */     String targetCollection = this.m_binder.get("TargetCollection");
/* 131 */     String targetArchive = this.m_binder.get("TargetArchive");
/* 132 */     CollectionData targetCollectionData = ArchiveUtils.getCollection(targetCollection);
/*     */ 
/* 135 */     DataBinder targetArchiveData = ArchiveUtils.readArchiveFile(targetCollectionData.m_location, targetArchive, false);
/* 136 */     this.m_binder.merge(targetArchiveData);
/*     */ 
/* 138 */     String srcCollection = this.m_binder.get("SourceCollection");
/* 139 */     String srcArchive = this.m_binder.get("SourceArchive");
/* 140 */     String batchFile = this.m_binder.get("aBatchFile");
/*     */ 
/* 142 */     String srcID = srcCollection + "/" + srcArchive + "/" + batchFile;
/* 143 */     boolean isFound = false;
/*     */ 
/* 145 */     String lockDir = LegacyDirectoryLocator.getCollectionsDirectory();
/* 146 */     FileUtils.reserveDirectory(lockDir);
/*     */     try
/*     */     {
/* 150 */       DataBinder transferData = ArchiveUtils.readTransferData(false);
/* 151 */       DataResultSet transferSet = null;
/* 152 */       if (transferData != null)
/*     */       {
/* 155 */         transferSet = (DataResultSet)transferData.getResultSet("Transfers");
/*     */       }
/*     */       else
/*     */       {
/* 159 */         transferData = new DataBinder();
/*     */       }
/*     */ 
/* 162 */       if (transferSet != null)
/*     */       {
/* 164 */         Vector row = transferSet.findRow(0, srcID);
/* 165 */         isFound = row != null;
/*     */       }
/*     */ 
/* 168 */       if (isFound)
/*     */       {
/* 170 */         String status = ResultSetUtils.getValue(transferSet, "aTransferStatus");
/* 171 */         this.m_binder.putLocal("aTransferStatus", status);
/*     */       }
/*     */       else
/*     */       {
/* 176 */         String srcPath = ArchiveUtils.buildLocation(srcCollection, srcArchive);
/* 177 */         String lastDone = transferData.getLocal(srcPath + ":lastDone");
/* 178 */         if ((lastDone != null) && (lastDone.equals(batchFile)))
/*     */         {
/* 180 */           isFound = isCheckTransfer;
/* 181 */           this.m_binder.putLocal("IsLastDone", "1");
/*     */ 
/* 183 */           String[] keys = { "lastStatus", "lastTransferTs", "lastTransferError" };
/* 184 */           for (int i = 0; i < keys.length; ++i)
/*     */           {
/* 186 */             String value = transferData.getLocal(srcPath + ":" + keys[i]);
/* 187 */             if (value == null)
/*     */               continue;
/* 189 */             this.m_binder.putLocal(keys[i], value);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 197 */       FileUtils.releaseDirectory(lockDir);
/*     */     }
/*     */ 
/* 200 */     if (isFound)
/*     */       return;
/* 202 */     if (isCheckTransfer)
/*     */     {
/* 205 */       String msg = LocaleUtils.encodeMessage("csArchiverTransferInfoNotFound", null, srcID);
/*     */ 
/* 207 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 211 */     String targetDir = ArchiveUtils.buildArchiveDirectory(targetCollectionData.m_location, targetArchive);
/* 212 */     DataBinder binder = ArchiveUtils.readExportsFile(targetDir, targetCollectionData.m_location);
/* 213 */     DataResultSet exportSet = (DataResultSet)binder.getResultSet("BatchFiles");
/* 214 */     if (exportSet == null)
/*     */       return;
/* 216 */     Vector row = exportSet.findRow(0, batchFile);
/* 217 */     boolean isTransfered = row != null;
/* 218 */     this.m_binder.putLocal("IsTransfered", String.valueOf(isTransfered));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateTransferStatus()
/*     */     throws DataException, ServiceException
/*     */   {
/* 229 */     String srcPath = this.m_binder.getLocal("aSourcePath");
/* 230 */     String batchFile = this.m_binder.getLocal("aBatchFile");
/*     */ 
/* 232 */     TransferInfo info = TransferUtils.getTransferInfo(srcPath);
/* 233 */     String bf = info.m_properties.getProperty("aBatchFile");
/* 234 */     if (!bf.equals(batchFile))
/*     */       return;
/* 236 */     info.m_properties = this.m_binder.getLocalData();
/* 237 */     TransferUtils.notifyOfTransfer(srcPath);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateTransferInTotals()
/*     */     throws DataException, ServiceException
/*     */   {
/* 244 */     String targetCollectionName = this.m_binder.get("TargetCollection");
/* 245 */     String targetArchive = this.m_binder.get("TargetArchive");
/*     */ 
/* 247 */     CollectionData targetCollection = ArchiveUtils.getCollection(targetCollectionName);
/* 248 */     updateTransferTotals(true, targetCollection, targetArchive, false);
/*     */ 
/* 250 */     boolean isAuto = StringUtils.convertToBool(this.m_binder.getAllowMissing("IsAutoTransfer"), false);
/* 251 */     if (isAuto) {
/*     */       return;
/*     */     }
/* 254 */     TransferInfo info = new TransferInfo(this.m_binder.getLocalData(), this.m_service);
/* 255 */     TransferUtils.updateTransferInfo(info, true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void retrieveSourceInformation()
/*     */     throws DataException, ServiceException
/*     */   {
/* 265 */     String srcArchive = this.m_binder.get("SourceArchive");
/* 266 */     String archiveDir = (String)this.m_service.getCachedObject("ArchiveDir");
/* 267 */     String archiveExportDir = (String)this.m_service.getCachedObject("ArchiveExportDir");
/* 268 */     CollectionData currentCollection = (CollectionData)this.m_service.getCachedObject("CurrentCollection");
/*     */ 
/* 270 */     DataBinder archiveData = ArchiveUtils.readArchiveFile(currentCollection.m_location, srcArchive, false);
/* 271 */     this.m_binder.merge(archiveData);
/*     */ 
/* 274 */     this.m_binder.putLocal("SourceArchiveDir", archiveDir);
/* 275 */     this.m_binder.putLocal("SourceArchiveExportDir", archiveExportDir);
/*     */ 
/* 277 */     DataBinder exportsData = ArchiveUtils.readExportsFile(archiveDir, null);
/* 278 */     DataResultSet exportSet = (DataResultSet)exportsData.getResultSet("BatchFiles");
/* 279 */     if (exportSet != null)
/*     */     {
/* 281 */       this.m_binder.addResultSet("BatchFiles", exportSet);
/*     */     }
/*     */ 
/* 285 */     checkForTransferOwner();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void requestTransfer()
/*     */     throws DataException, ServiceException
/*     */   {
/* 292 */     CollectionData collData = (CollectionData)this.m_service.getCachedObject("CurrentCollection");
/* 293 */     String archiveName = this.m_binder.getLocal("aArchiveName");
/* 294 */     updateTransferTotals(false, collData, archiveName, false);
/*     */ 
/* 297 */     String archiveDir = (String)this.m_service.getCachedObject("ArchiveDir");
/* 298 */     String archiveExportDir = (String)this.m_service.getCachedObject("ArchiveExportDir");
/* 299 */     DataBinder exportData = ArchiveUtils.readExportsFile(archiveDir, null);
/* 300 */     DataResultSet exportSet = (DataResultSet)exportData.getResultSet("BatchFiles");
/* 301 */     if ((exportSet == null) || (exportSet.isEmpty()))
/*     */     {
/* 304 */       return;
/*     */     }
/*     */ 
/* 308 */     boolean isAuto = StringUtils.convertToBool(this.m_binder.getLocal("IsAutoTransfer"), false);
/* 309 */     if (isAuto)
/*     */     {
/* 311 */       boolean isChanged = false;
/* 312 */       String transferedStr = this.m_binder.getLocal("TransferedBatchFiles");
/* 313 */       Vector transfered = StringUtils.parseArray(transferedStr, ',', '^');
/* 314 */       Vector deleteDirs = new IdcVector();
/*     */ 
/* 316 */       if (transferedStr == null)
/*     */       {
/* 318 */         Report.trace("transfermonitor", "No transferred batch files", null);
/*     */       }
/*     */       else
/*     */       {
/* 322 */         Report.trace("transfermonitor", "Transferred batch files=" + transferedStr, null);
/*     */       }
/* 324 */       int num = transfered.size();
/* 325 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 327 */         String transferedFile = (String)transfered.elementAt(i);
/* 328 */         Vector v = exportSet.findRow(0, transferedFile);
/* 329 */         if (v == null)
/*     */           continue;
/* 331 */         isChanged = true;
/* 332 */         exportSet.deleteCurrentRow();
/*     */ 
/* 335 */         Object[] triplet = new Object[3];
/* 336 */         triplet[0] = transferedFile;
/* 337 */         triplet[1] = archiveExportDir;
/* 338 */         triplet[2] = exportSet;
/*     */ 
/* 340 */         Report.trace("transfermonitor", "Removing batch file '" + transferedFile + "'", null);
/* 341 */         deleteDirs.addElement(triplet);
/*     */       }
/*     */ 
/* 346 */       if (isChanged)
/*     */       {
/* 348 */         ArchiveUtils.writeExportsFile(archiveDir, exportData);
/*     */ 
/* 351 */         int size = deleteDirs.size();
/* 352 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 354 */           Object[] triplet = (Object[])(Object[])deleteDirs.elementAt(i);
/* 355 */           ArchiveUtils.deleteAssociatedBatchFiles((String)triplet[0], (String)triplet[1], (DataResultSet)triplet[2]);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 360 */     String batchFile = this.m_binder.getLocal("aBatchFile");
/* 361 */     if ((batchFile == null) || (batchFile.length() == 0))
/*     */     {
/* 363 */       return;
/*     */     }
/*     */ 
/* 366 */     Vector row = exportSet.findRow(0, batchFile);
/* 367 */     if (row == null)
/*     */     {
/* 369 */       String msg = LocaleUtils.encodeMessage("csArchiverBatchFileDoesNotExist", null, batchFile);
/*     */ 
/* 371 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 375 */     Properties props = exportSet.getCurrentRowProps();
/* 376 */     DataBinder.mergeHashTables(this.m_binder.getLocalData(), props);
/*     */ 
/* 378 */     String zipName = TransferUtils.createPackage(this.m_binder, archiveExportDir, false, null, null, this.m_importStateInfo);
/* 379 */     this.m_binder.addTempFile(zipName);
/* 380 */     this.m_binder.putLocal("ZipFile:path", zipName);
/* 381 */     this.m_binder.putLocal("IsDelayDownload", "1");
/* 382 */     this.m_binder.m_hasAttachedFiles = true;
/*     */   }
/*     */ 
/*     */   protected void updateTransferTotals(boolean isIn, CollectionData collectionData, String archiveName, boolean isLock)
/*     */     throws DataException, ServiceException
/*     */   {
/* 392 */     String totalKey = null;
/* 393 */     String tsKey = null;
/* 394 */     if (isIn)
/*     */     {
/* 396 */       totalKey = "aTotalTransferedIn";
/* 397 */       tsKey = "aLastTransferIn";
/*     */     }
/*     */     else
/*     */     {
/* 401 */       totalKey = "aTotalTransferedOut";
/* 402 */       tsKey = "aLastTransferOut";
/*     */     }
/*     */ 
/* 405 */     String total = this.m_binder.getLocal(totalKey);
/* 406 */     if ((total == null) || (total.length() == 0))
/*     */       return;
/* 408 */     if (isLock)
/*     */     {
/* 410 */       FileUtils.reserveDirectory(collectionData.m_location);
/*     */     }
/*     */     try
/*     */     {
/* 414 */       DataBinder archiveData = ArchiveUtils.readArchiveFile(collectionData.m_location, archiveName, false);
/*     */ 
/* 417 */       archiveData.putLocal(totalKey, total);
/* 418 */       archiveData.putLocal(tsKey, this.m_binder.get(tsKey));
/*     */ 
/* 420 */       String archiveDir = ArchiveUtils.buildArchiveDirectory(collectionData.m_location, archiveName);
/* 421 */       ArchiveUtils.writeArchiveFile(archiveDir, archiveData, false);
/*     */ 
/* 423 */       String monikerString = collectionData.getMoniker();
/* 424 */       MonikerWatcher.notifyChanged(monikerString);
/*     */     }
/*     */     finally
/*     */     {
/* 428 */       if (isLock)
/*     */       {
/* 430 */         FileUtils.releaseDirectory(collectionData.m_location);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void checkForTransferOwner()
/*     */     throws DataException
/*     */   {
/* 438 */     String transferOwner = this.m_binder.get("aTransferOwner");
/* 439 */     Provider provider = OutgoingProviderMonitor.getOutgoingProvider(transferOwner);
/* 440 */     if (provider == null)
/*     */       return;
/* 442 */     this.m_binder.putLocal("HasTransferOwnerProvider", "1");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 448 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97046 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.TransferServiceHandler
 * JD-Core Version:    0.5.4
 */