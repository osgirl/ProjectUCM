/*      */ package intradoc.server.archive;
/*      */ 
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.server.MonikerWatcher;
/*      */ import intradoc.shared.CollectionData;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.Date;
/*      */ import java.util.Observable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class TransferHandler
/*      */   implements ReportProgress
/*      */ {
/*      */   public Workspace m_workspace;
/*      */   public ReportProgress m_progress;
/*      */   public DataBinder m_binder;
/*      */   public Properties m_transferProps;
/*      */   public boolean m_isAuto;
/*      */   public boolean m_isAbort;
/*      */   public String m_statusMsg;
/*      */   public TransferData m_transferData;
/*      */   public ExecutionContextAdaptor m_context;
/*      */   public ArchiveImportStateInformation m_importStateInfo;
/*      */   public CollectionData m_srcCollection;
/*      */   public String m_srcArchive;
/*      */   public String m_srcArchiveDir;
/*      */   public String m_srcArchiveExportDir;
/*      */   public CollectionData m_targetCollection;
/*      */   public String m_targetArchive;
/*      */   public String m_targetArchiveDir;
/*      */   public static final int LOCAL_TO_LOCAL = 0;
/*      */   public static final int SOURCE_PROXIED = 1;
/*      */   public static final int TARGET_PROXIED = 2;
/*      */   public int m_transferType;
/*      */   public String reportMsg;
/*      */ 
/*      */   public TransferHandler()
/*      */   {
/*   50 */     this.m_workspace = null;
/*   51 */     this.m_progress = null;
/*   52 */     this.m_binder = null;
/*   53 */     this.m_transferProps = null;
/*      */ 
/*   55 */     this.m_isAuto = false;
/*   56 */     this.m_isAbort = false;
/*   57 */     this.m_statusMsg = null;
/*      */ 
/*   59 */     this.m_transferData = null;
/*      */ 
/*   62 */     this.m_context = null;
/*      */ 
/*   65 */     this.m_importStateInfo = null;
/*      */ 
/*   68 */     this.m_srcCollection = null;
/*   69 */     this.m_srcArchive = null;
/*   70 */     this.m_srcArchiveDir = null;
/*   71 */     this.m_srcArchiveExportDir = null;
/*      */ 
/*   74 */     this.m_targetCollection = null;
/*   75 */     this.m_targetArchive = null;
/*   76 */     this.m_targetArchiveDir = null;
/*      */ 
/*   83 */     this.m_transferType = 0;
/*      */ 
/*   86 */     this.reportMsg = null;
/*      */   }
/*      */ 
/*      */   public void initObjects(Workspace ws, ReportProgress rp, Observable obs) {
/*   90 */     this.m_workspace = ws;
/*   91 */     this.m_progress = rp;
/*      */ 
/*   94 */     this.m_context = new ExecutionContextAdaptor();
/*   95 */     this.m_context.setCachedObject("TransferHandler", this);
/*      */   }
/*      */ 
/*      */   public void init(DataBinder binder) throws DataException, ServiceException
/*      */   {
/*  100 */     this.m_binder = binder;
/*  101 */     this.m_isAuto = StringUtils.convertToBool(this.m_binder.getLocal("IsAutoTransfer"), false);
/*  102 */     this.m_transferData = new TransferData();
/*      */ 
/*  105 */     this.m_srcCollection = ArchiveUtils.getCollection(this.m_binder);
/*  106 */     this.m_srcArchive = this.m_binder.get("aArchiveName");
/*      */ 
/*  109 */     String targetPath = this.m_binder.get("aTargetArchive");
/*  110 */     if (targetPath.length() == 0)
/*      */     {
/*  112 */       throw new ServiceException("!csArchiverTargetNotDefined");
/*      */     }
/*  114 */     String[] targetLocation = ArchiveUtils.parseLocation(targetPath);
/*  115 */     this.m_targetCollection = ArchiveUtils.getCollection(targetLocation[0]);
/*  116 */     this.m_targetArchive = targetLocation[1];
/*      */ 
/*  118 */     this.m_importStateInfo = ((ArchiveImportStateInformation)ComponentClassFactory.createClassInstance("ArchiveImportStateInformation", "intradoc.server.archive.ArchiveImportStateInformation", ""));
/*      */ 
/*  120 */     this.m_importStateInfo.init(this.m_binder, this.m_workspace, this.m_context);
/*      */ 
/*  122 */     buildMessageStub();
/*      */   }
/*      */ 
/*      */   public void transferArchive()
/*      */     throws DataException, ServiceException
/*      */   {
/*  135 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  136 */     if ((this.m_targetCollection.isProxied()) && (this.m_srcCollection.isProxied()))
/*      */     {
/*  138 */       throw new ServiceException("!csArchiverBothAreProxied");
/*      */     }
/*      */ 
/*  141 */     if (!this.m_isAuto)
/*      */     {
/*  143 */       String msg = this.m_statusMsg + "!csArchiveSuffixStarted";
/*  144 */       Report.appInfo("archiver", null, msg, null);
/*      */     }
/*  146 */     createTransferProperties(idcName);
/*      */     try
/*      */     {
/*  150 */       this.m_transferType = 0;
/*  151 */       if (this.m_targetCollection.isProxied())
/*      */       {
/*  154 */         this.m_transferType = 2;
/*  155 */         sendTransferToProxied();
/*      */       }
/*  157 */       else if (this.m_srcCollection.isProxied())
/*      */       {
/*  160 */         this.m_transferType = 1;
/*  161 */         requestTransferFromProxied();
/*      */       }
/*      */       else
/*      */       {
/*  166 */         doLocalTransfer();
/*      */       }
/*      */ 
/*  170 */       reportTransferProgress(LocaleUtils.encodeMessage("csProgressFinishedTime", null, new Date()), -1.0F, -1.0F);
/*      */ 
/*  173 */       Report.trace("transfermonitor", "Transferred " + this.m_transferData.m_numRevisions + " items", null);
/*      */ 
/*  175 */       if (!this.m_isAuto)
/*      */       {
/*  177 */         String msg = LocaleUtils.encodeMessage("csSuccessfullyTransferedRevisionsInBatchFiles", null, "" + this.m_transferData.m_numBatchFiles, "" + this.m_transferData.m_numRevisions);
/*      */ 
/*  179 */         msg = LocaleUtils.encodeMessage("csFinishedTransferingToTargetCollection", msg, this.m_targetArchive, this.m_targetCollection.m_name);
/*      */ 
/*  181 */         msg = this.m_statusMsg + msg;
/*  182 */         Report.appInfo("archiver", null, msg, null);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  187 */       createServiceException(e);
/*      */     }
/*      */     finally
/*      */     {
/*  191 */       this.m_binder.cleanUpTempFiles();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void createTransferProperties(String idcName)
/*      */   {
/*  198 */     this.m_transferProps = new Properties();
/*      */ 
/*  200 */     this.m_transferProps.put("IsAutoTransfer", String.valueOf(this.m_isAuto));
/*      */ 
/*  202 */     String srcPath = ArchiveUtils.buildLocation(this.m_srcCollection.m_name, this.m_srcArchive);
/*  203 */     this.m_transferProps.put("aSourcePath", srcPath);
/*  204 */     this.m_transferProps.put("aTargetPath", ArchiveUtils.buildLocation(this.m_targetCollection.m_name, this.m_targetArchive));
/*  205 */     this.m_transferProps.put("aTransferOwner", idcName);
/*      */ 
/*  207 */     this.m_transferProps.put("SourceCollection", this.m_srcCollection.m_name);
/*  208 */     this.m_transferProps.put("SourceArchive", this.m_srcArchive);
/*  209 */     this.m_transferProps.put("TargetCollection", this.m_targetCollection.m_name);
/*  210 */     this.m_transferProps.put("TargetArchive", this.m_targetArchive);
/*      */ 
/*  213 */     String dte = LocaleUtils.formatODBC(new Date());
/*  214 */     this.m_transferProps.put("aHasConnection", "0");
/*  215 */     this.m_transferProps.put("aTransferErrorMsg", "");
/*  216 */     this.m_transferProps.put("aCreateTransferTs", dte);
/*  217 */     this.m_transferProps.put("aLastTransferTs", dte);
/*      */ 
/*  219 */     this.m_transferProps.put("aLastTransferIn", dte);
/*  220 */     this.m_transferProps.put("aTotalTransferedIn", "");
/*  221 */     this.m_transferProps.put("aLastTransferOut", dte);
/*  222 */     this.m_transferProps.put("aTotalTransferedOut", "");
/*      */   }
/*      */ 
/*      */   protected void validateTransfer(DataBinder archiveData, boolean isTargetLocal)
/*      */     throws ServiceException, DataException
/*      */   {
/*  228 */     String curTransferOwner = archiveData.getLocal("aTransferOwner");
/*  229 */     String curTargetArchivePath = archiveData.getLocal("aTargetArchive");
/*      */ 
/*  231 */     String transferOwner = this.m_transferProps.getProperty("aTransferOwner");
/*  232 */     String targetArchivePath = this.m_transferProps.getProperty("aTargetPath");
/*  233 */     if ((curTransferOwner == null) || (!transferOwner.equals(curTransferOwner)))
/*      */     {
/*  235 */       throw new ServiceException("!csCurrentSystemNotTransferOwner");
/*      */     }
/*  237 */     if ((curTargetArchivePath == null) || (!targetArchivePath.equals(curTargetArchivePath)))
/*      */     {
/*  239 */       String msg = LocaleUtils.encodeMessage("csTargetArchiveHasBeenChanged", null, targetArchivePath, curTargetArchivePath);
/*      */ 
/*  241 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  244 */     if (!isTargetLocal) {
/*      */       return;
/*      */     }
/*  247 */     DataBinder targetData = ArchiveUtils.readArchiveFile(this.m_targetCollection.m_location, this.m_targetArchive, false);
/*  248 */     boolean isTargetable = StringUtils.convertToBool(targetData.getLocal("aIsTargetable"), false);
/*  249 */     if (isTargetable)
/*      */       return;
/*  251 */     String msg = LocaleUtils.encodeMessage("csArchiveNotTargetable", this.m_targetArchive);
/*  252 */     throw new ServiceException(msg);
/*      */   }
/*      */ 
/*      */   public void sendTransferToProxied()
/*      */     throws DataException, ServiceException
/*      */   {
/*  276 */     this.m_transferProps.put("aTransferType", "targetProxied");
/*      */ 
/*  278 */     FileUtils.reserveDirectory(this.m_srcCollection.m_location);
/*  279 */     DataBinder exportData = new DataBinder();
/*      */     try
/*      */     {
/*  282 */       DataBinder archiveData = ArchiveUtils.readArchiveFile(this.m_srcCollection.m_location, this.m_srcArchive, false);
/*      */ 
/*  284 */       validateTransfer(archiveData, false);
/*      */ 
/*  287 */       this.m_srcArchiveDir = ArchiveUtils.buildArchiveDirectory(this.m_srcCollection.m_location, this.m_srcArchive);
/*  288 */       this.m_srcArchiveExportDir = ArchiveUtils.buildArchiveDirectory(this.m_srcCollection.m_exportLocation, this.m_srcArchive);
/*  289 */       exportData = ArchiveUtils.readExportsFile(this.m_srcArchiveDir, null);
/*      */     }
/*      */     finally
/*      */     {
/*  293 */       FileUtils.releaseDirectory(this.m_srcCollection.m_location);
/*      */     }
/*      */ 
/*  296 */     DataResultSet exportSet = (DataResultSet)exportData.getResultSet("BatchFiles");
/*  297 */     if ((exportSet == null) || ((exportSet.isEmpty()) && (this.m_isAuto)))
/*      */     {
/*  300 */       return;
/*      */     }
/*      */ 
/*  304 */     this.m_transferProps.put("IDC_Name", this.m_targetCollection.m_name);
/*  305 */     this.m_transferProps.put("aArchiveName", this.m_targetArchive);
/*      */ 
/*  308 */     String serverName = this.m_targetCollection.getProxiedServer();
/*  309 */     checkTarget(serverName);
/*      */ 
/*  311 */     for (exportSet.first(); exportSet.isRowPresent(); exportSet.next())
/*      */     {
/*  313 */       Properties props = exportSet.getCurrentRowProps();
/*  314 */       String idcName = (String)props.remove("IDC_Name");
/*  315 */       props.put("aIDC_Name", idcName);
/*      */ 
/*  317 */       DataBinder binder = new DataBinder();
/*  318 */       binder.setLocalData((Properties)this.m_transferProps.clone());
/*  319 */       DataBinder.mergeHashTables(binder.getLocalData(), props);
/*      */ 
/*  322 */       String[] batchLocations = ArchiveUtils.breakUpBatchPath(this.m_srcArchiveDir, props.getProperty("aBatchFile"));
/*  323 */       props.put("aBatchFile", batchLocations[1]);
/*      */ 
/*  326 */       if (uploadToTarget(binder))
/*      */       {
/*  329 */         binder.m_hasAttachedFiles = false;
/*      */ 
/*  332 */         monitorTransfer(binder);
/*      */ 
/*  335 */         updateArchiveTransferData(props, true);
/*      */       }
/*      */ 
/*  338 */       if (!this.m_isAuto) {
/*      */         continue;
/*      */       }
/*  341 */       removeFromExports(props);
/*      */     }
/*      */ 
/*  345 */     if (this.m_transferData.m_numBatchFiles == 0)
/*      */     {
/*  347 */       updateArchiveTransferData(null, false);
/*      */     }
/*      */ 
/*  350 */     DataBinder binder = new DataBinder();
/*  351 */     binder.setLocalData(this.m_transferProps);
/*  352 */     this.reportMsg = "Executing service UPDATE_TARGET_TOTALS";
/*  353 */     Report.trace("transfermonitor", this.reportMsg, null);
/*  354 */     TransferUtils.executeProxiedRequest(serverName, "UPDATE_TARGET_TOTALS", binder, binder, this.m_context, this);
/*      */   }
/*      */ 
/*      */   protected void checkTarget(String target)
/*      */     throws DataException, ServiceException
/*      */   {
/*  363 */     DataBinder binder = new DataBinder();
/*  364 */     binder.setLocalData((Properties)this.m_transferProps.clone());
/*  365 */     this.reportMsg = "Executing service GET_TARGET_INFO";
/*  366 */     Report.trace("transfermonitor", this.reportMsg, null);
/*  367 */     TransferUtils.executeProxiedRequest(target, "GET_TARGET_INFO", binder, binder, this.m_context, this);
/*      */ 
/*  370 */     String str = binder.getLocal("HasTransferOwnerProvider");
/*  371 */     if (str == null)
/*      */     {
/*  373 */       str = "0";
/*      */     }
/*  375 */     this.m_transferProps.put("HasTransferOwnerProvider", str);
/*      */ 
/*  378 */     boolean isTargetable = StringUtils.convertToBool(binder.getLocal("aIsTargetable"), false);
/*  379 */     if (isTargetable)
/*      */       return;
/*  381 */     throw new ServiceException("!csTargetNotConfiguredTargetable");
/*      */   }
/*      */ 
/*      */   protected boolean uploadToTarget(DataBinder binder)
/*      */     throws DataException, ServiceException
/*      */   {
/*  387 */     boolean isTransfered = checkTransferTargetStatus(binder, false);
/*  388 */     if (isTransfered)
/*      */     {
/*  390 */       boolean isInProcess = StringUtils.convertToBool(binder.getLocal("IsInProcess"), false);
/*      */ 
/*  393 */       return isInProcess;
/*      */     }
/*      */ 
/*  398 */     String batchFile = binder.getLocal("aBatchFile");
/*  399 */     binder.putLocal("IsDelayUpload", "1");
/*      */ 
/*  401 */     reportTransferProgress(LocaleUtils.encodeMessage("csProgressPackagingBatchFile", null, batchFile), -1.0F, -1.0F);
/*  402 */     String zipName = TransferUtils.createPackage(binder, this.m_srcArchiveExportDir, false, null, null, this.m_importStateInfo);
/*      */ 
/*  404 */     reportTransferProgress(LocaleUtils.encodeMessage("csProgressUploadingBatchFile", null, batchFile), -1.0F, -1.0F);
/*      */     try
/*      */     {
/*  407 */       this.m_binder.addTempFile(zipName);
/*  408 */       binder.m_hasAttachedFiles = true;
/*  409 */       binder.putLocal("ZipFile:path", zipName);
/*  410 */       binder.putLocal("IdcService", "UPLOAD_ARCHIVE_TRANSFER");
/*      */ 
/*  412 */       binder.setEnvironmentValue("CONTENT_TYPE", "text/html");
/*      */ 
/*  414 */       DataBinder outBinder = new DataBinder();
/*  415 */       String serverName = this.m_targetCollection.getProxiedServer();
/*  416 */       this.reportMsg = "Executing service UPLOAD_ARCHIVE_TRANSFER";
/*  417 */       Report.trace("transfermonitor", this.reportMsg, null);
/*  418 */       TransferUtils.executeProxiedRequest(serverName, "UPLOAD_ARCHIVE_TRANSFER", binder, outBinder, this.m_context, this);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  424 */       throw new ServiceException(LocaleUtils.encodeMessage("csUnableToUploadBatchFile", null, batchFile), e);
/*      */     }
/*      */ 
/*  427 */     reportTransferProgress(LocaleUtils.encodeMessage("csProgressSuccessfullyUploaded", null, batchFile), -1.0F, -1.0F);
/*  428 */     return true;
/*      */   }
/*      */ 
/*      */   protected void monitorTransfer(DataBinder binder) throws DataException, ServiceException
/*      */   {
/*  433 */     String batchFile = binder.getLocal("aBatchFile");
/*  434 */     reportTransferProgress(LocaleUtils.encodeMessage("csProgressMonitoringTransfer", null, batchFile), -1.0F, -1.0F);
/*      */ 
/*  436 */     String srcPath = binder.getLocal("aSourcePath");
/*  437 */     TransferInfo info = new TransferInfo(binder.getLocalData(), this.m_context);
/*  438 */     TransferUtils.addTransferInfo(srcPath, info);
/*      */ 
/*  440 */     int timeout = SharedObjects.getEnvironmentInt("ArchiveTransferCheckIntervalInMillis", 1000);
/*  441 */     boolean hasBackPointer = StringUtils.convertToBool(this.m_transferProps.getProperty("HasTransferOwnerProvider"), false);
/*      */ 
/*  443 */     if (hasBackPointer)
/*      */     {
/*  445 */       timeout *= 5;
/*      */     }
/*      */ 
/*  448 */     boolean isFinished = false;
/*  449 */     while (!isFinished)
/*      */     {
/*  451 */       synchronized (info)
/*      */       {
/*      */         try
/*      */         {
/*  455 */           info.wait(timeout);
/*      */         }
/*      */         catch (Throwable ignore)
/*      */         {
/*  459 */           Report.trace(null, null, ignore);
/*      */         }
/*      */       }
/*  462 */       checkForAbort();
/*  463 */       isFinished = checkTransferTargetStatus(binder, true);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean checkTransferTargetStatus(DataBinder binder, boolean isCheck)
/*      */     throws DataException, ServiceException
/*      */   {
/*  470 */     if (isCheck)
/*      */     {
/*  472 */       String status = binder.getLocal("aTransferStatus");
/*  473 */       if ((status != null) && (status.equals("success")))
/*      */       {
/*  475 */         TransferUtils.removeTransfer(binder.getLocal("aSourceID"));
/*  476 */         return true;
/*      */       }
/*  478 */       String errMsg = binder.getLocal("aTransferErrorMsg");
/*  479 */       if ((errMsg != null) && (errMsg.length() > 0))
/*      */       {
/*  481 */         throw new ServiceException(LocaleUtils.encodeMessage("csTransferNotCompleted", errMsg));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  486 */     boolean isTransfered = false;
/*  487 */     String batchFile = binder.getLocal("aBatchFile");
/*  488 */     DataBinder outBinder = new DataBinder();
/*      */ 
/*  493 */     if (isCheck)
/*      */     {
/*  495 */       binder.putLocal("IsCheckTransfer", "1");
/*  496 */       binder.putLocal("IsForceTransfer", "0");
/*      */     }
/*      */     else
/*      */     {
/*  500 */       binder.putLocal("IsForceTransfer", String.valueOf(!this.m_isAuto));
/*  501 */       binder.putLocal("IsCheckTransfer", "0");
/*      */     }
/*      */ 
/*  504 */     reportTransferProgress(LocaleUtils.encodeMessage("csProgressCheckingUploadStatus", null, batchFile), -1.0F, -1.0F);
/*      */ 
/*  507 */     String psName = this.m_targetCollection.getProxiedServer();
/*  508 */     this.reportMsg = "Executing service GET_TARGET_TRANSFER_STATUS";
/*  509 */     Report.trace("transfermonitor", this.reportMsg, null);
/*  510 */     TransferUtils.executeProxiedRequest(psName, "GET_TARGET_TRANSFER_STATUS", binder, outBinder, this.m_context, this);
/*      */ 
/*  513 */     String transferStatus = outBinder.getLocal("aTransferStatus");
/*  514 */     if (transferStatus != null)
/*      */     {
/*  517 */       if (isCheck)
/*      */       {
/*  519 */         isTransfered = false;
/*      */       }
/*      */       else
/*      */       {
/*  524 */         binder.putLocal("IsInProcess", "1");
/*  525 */         isTransfered = true;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  530 */       boolean isUpdateCounters = false;
/*  531 */       boolean isLastDone = StringUtils.convertToBool(outBinder.getLocal("IsLastDone"), false);
/*  532 */       if (isLastDone)
/*      */       {
/*  535 */         transferStatus = outBinder.getLocal("lastStatus");
/*  536 */         isTransfered = transferStatus.equals("success");
/*  537 */         if ((!isTransfered) && (isCheck))
/*      */         {
/*  539 */           String errMsg = outBinder.getLocal("lastTransferError");
/*  540 */           throw new ServiceException(errMsg);
/*      */         }
/*      */ 
/*  543 */         isUpdateCounters = (isTransfered) && (!isCheck);
/*      */       }
/*      */ 
/*  546 */       if (!isCheck)
/*      */       {
/*  549 */         isTransfered = StringUtils.convertToBool(outBinder.getLocal("IsTransfered"), false);
/*      */       }
/*      */ 
/*  552 */       if ((isTransfered) && (isUpdateCounters))
/*      */       {
/*  556 */         updateArchiveTransferData(binder.getLocalData(), true);
/*      */       }
/*      */     }
/*      */ 
/*  560 */     if (isCheck)
/*      */     {
/*  562 */       if (isTransfered)
/*      */       {
/*  564 */         reportTransferProgress(LocaleUtils.encodeMessage("csProgressSuccessfullyUploaded", null, batchFile), -1.0F, -1.0F);
/*      */       }
/*      */       else
/*      */       {
/*  569 */         reportTransferProgress(LocaleUtils.encodeMessage("csProgressMonitoringUpload", null, batchFile), -1.0F, -1.0F);
/*      */       }
/*      */ 
/*      */     }
/*  575 */     else if (isTransfered)
/*      */     {
/*  577 */       reportTransferProgress(LocaleUtils.encodeMessage("csProgressAlreadyTransfered", null, batchFile), -1.0F, -1.0F);
/*      */     }
/*      */ 
/*  581 */     return isTransfered;
/*      */   }
/*      */ 
/*      */   public void requestTransferFromProxied()
/*      */     throws DataException, ServiceException
/*      */   {
/*  592 */     this.m_transferProps.put("aTransferType", "sourceProxied");
/*      */ 
/*  595 */     DataBinder binder = new DataBinder();
/*  596 */     binder.setLocalData(this.m_transferProps);
/*  597 */     binder.putLocal("IDC_Name", this.m_srcCollection.m_name);
/*  598 */     binder.putLocal("aArchiveName", this.m_srcArchive);
/*      */ 
/*  600 */     DataResultSet srcExportSet = retrieveSourceInformation(binder);
/*  601 */     if ((srcExportSet == null) || ((srcExportSet.isEmpty()) && (this.m_isAuto)))
/*      */     {
/*  604 */       return;
/*      */     }
/*      */ 
/*  607 */     this.m_targetArchiveDir = ArchiveUtils.buildArchiveDirectory(this.m_targetCollection.m_location, this.m_targetArchive);
/*  608 */     DataBinder exportData = ArchiveUtils.readExportsFile(this.m_targetArchiveDir, null);
/*  609 */     DataResultSet exportSet = (DataResultSet)exportData.getResultSet("BatchFiles");
/*      */ 
/*  611 */     Vector alreadyTransfered = new IdcVector();
/*  612 */     for (srcExportSet.first(); srcExportSet.isRowPresent(); srcExportSet.next())
/*      */     {
/*  614 */       Properties props = srcExportSet.getCurrentRowProps();
/*  615 */       String batchFile = props.getProperty("aBatchFile");
/*      */ 
/*  617 */       boolean isTransfered = false;
/*  618 */       if (exportSet != null)
/*      */       {
/*  621 */         String[] batchLocations = ArchiveUtils.breakUpBatchPath(this.m_srcArchiveExportDir, batchFile);
/*      */ 
/*  623 */         Vector row = exportSet.findRow(0, batchLocations[1]);
/*  624 */         isTransfered = row != null;
/*      */       }
/*  626 */       if (isTransfered)
/*      */       {
/*  628 */         alreadyTransfered.addElement(batchFile);
/*      */       }
/*      */       else
/*      */       {
/*  632 */         requestSourceTransfer(binder, batchFile, alreadyTransfered);
/*  633 */         alreadyTransfered.removeAllElements();
/*  634 */         alreadyTransfered.addElement(batchFile);
/*      */ 
/*  637 */         updateArchiveTransferData(props, true);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  642 */     if (this.m_transferData.m_numBatchFiles == 0)
/*      */     {
/*  644 */       updateArchiveTransferData(null, false);
/*      */     }
/*  646 */     requestSourceTransfer(binder, null, alreadyTransfered);
/*      */   }
/*      */ 
/*      */   protected DataResultSet retrieveSourceInformation(DataBinder binder)
/*      */     throws DataException, ServiceException
/*      */   {
/*  652 */     DataBinder outBinder = new DataBinder();
/*  653 */     String serverName = this.m_srcCollection.getProxiedServer();
/*      */ 
/*  655 */     reportTransferProgress("!csProgressRequestingSourceInfo", -1.0F, -1.0F);
/*  656 */     this.reportMsg = "Executing service GET_TRANSFER_SOURCE_INFO";
/*  657 */     Report.trace("transfermonitor", this.reportMsg, null);
/*  658 */     TransferUtils.executeProxiedRequest(serverName, "GET_TRANSFER_SOURCE_INFO", binder, outBinder, this.m_context, this);
/*      */ 
/*  660 */     reportTransferProgress("!csProgressSuccessfullyRequestedSourceInfo", -1.0F, -1.0F);
/*      */ 
/*  662 */     validateTransfer(outBinder, true);
/*      */ 
/*  665 */     this.m_srcArchiveDir = outBinder.getLocal("SourceArchiveDir");
/*  666 */     this.m_srcArchiveExportDir = outBinder.getLocal("SourceArchiveExportDir");
/*      */ 
/*  668 */     return (DataResultSet)outBinder.getResultSet("BatchFiles");
/*      */   }
/*      */ 
/*      */   protected void requestSourceTransfer(DataBinder binder, String batchFile, Vector alreadyTransfered)
/*      */     throws DataException, ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  676 */       if (batchFile != null)
/*      */       {
/*  678 */         reportTransferProgress(LocaleUtils.encodeMessage("csProgressRequestingBatchFile", null, batchFile), -1.0F, -1.0F);
/*      */       }
/*      */ 
/*  682 */       String transfered = StringUtils.createString(alreadyTransfered, ',', '^');
/*  683 */       binder.putLocal("TransferedBatchFiles", transfered);
/*  684 */       if (batchFile != null)
/*      */       {
/*  686 */         binder.putLocal("aBatchFile", batchFile);
/*      */       }
/*      */ 
/*  690 */       DataBinder outBinder = new DataBinder();
/*  691 */       String serverName = this.m_srcCollection.getProxiedServer();
/*  692 */       this.reportMsg = "Executing service REQUEST_TRANSFER";
/*  693 */       Report.trace("transfermonitor", this.reportMsg, null);
/*  694 */       TransferUtils.executeProxiedRequest(serverName, "REQUEST_TRANSFER", binder, outBinder, this.m_context, this);
/*      */ 
/*  697 */       if (batchFile != null)
/*      */       {
/*  700 */         outBinder.setReportProgress(this);
/*  701 */         TransferUtils.receiveTransfer(outBinder, this.m_targetCollection, this.m_targetArchive, false);
/*      */ 
/*  703 */         reportTransferProgress(LocaleUtils.encodeMessage("csProgressSuccessfullyDownloaded", null, batchFile), -1.0F, -1.0F);
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/*  709 */       binder.removeLocal("TransferedBatchFiles");
/*  710 */       binder.removeLocal("aBatchFile");
/*      */     }
/*      */   }
/*      */ 
/*      */   public void doLocalTransfer()
/*      */     throws DataException, ServiceException
/*      */   {
/*  720 */     this.m_transferProps.put("aTransferType", "local");
/*  721 */     this.m_srcArchiveDir = ArchiveUtils.buildArchiveDirectory(this.m_srcCollection.m_location, this.m_srcArchive);
/*  722 */     this.m_srcArchiveExportDir = ArchiveUtils.buildArchiveDirectory(this.m_srcCollection.m_exportLocation, this.m_srcArchive);
/*  723 */     this.m_targetArchiveDir = ArchiveUtils.buildArchiveDirectory(this.m_targetCollection.m_location, this.m_targetArchive);
/*      */ 
/*  726 */     DataBinder sourceData = null;
/*  727 */     DataBinder exportData = null;
/*  728 */     FileUtils.reserveDirectory(this.m_srcArchiveDir);
/*      */     try
/*      */     {
/*  731 */       sourceData = ArchiveUtils.readArchiveFile(this.m_srcCollection.m_location, this.m_srcArchive, false);
/*  732 */       exportData = ArchiveUtils.readExportsFile(this.m_srcArchiveDir, null);
/*      */     }
/*      */     finally
/*      */     {
/*  736 */       FileUtils.releaseDirectory(this.m_srcCollection.m_location);
/*      */     }
/*      */ 
/*  740 */     validateTransfer(sourceData, true);
/*      */ 
/*  743 */     DataResultSet drset = (DataResultSet)exportData.getResultSet("BatchFiles");
/*  744 */     if ((drset == null) || ((drset.isEmpty()) && (this.m_isAuto)))
/*      */     {
/*  747 */       return;
/*      */     }
/*      */ 
/*  750 */     boolean isUpdated = false;
/*  751 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  753 */       Properties props = drset.getCurrentRowProps();
/*  754 */       boolean hasBeenTransfered = checkTransferStatus(props, null);
/*  755 */       if (!hasBeenTransfered)
/*      */       {
/*  757 */         transferFile(props);
/*  758 */         updateArchiveTransferData(props, true);
/*  759 */         isUpdated = true;
/*      */       }
/*  761 */       if (!this.m_isAuto) {
/*      */         continue;
/*      */       }
/*  764 */       removeFromExports(props);
/*      */     }
/*      */ 
/*  768 */     if (!isUpdated)
/*      */     {
/*  770 */       updateArchiveTransferData(null, false);
/*      */     }
/*      */ 
/*  773 */     String monikerString = this.m_srcCollection.getMoniker();
/*  774 */     MonikerWatcher.notifyChanged(monikerString);
/*      */ 
/*  776 */     monikerString = this.m_targetCollection.getMoniker();
/*  777 */     MonikerWatcher.notifyChanged(monikerString);
/*      */   }
/*      */ 
/*      */   protected void transferFile(Properties props)
/*      */     throws DataException, ServiceException
/*      */   {
/*  783 */     DataBinder binder = new DataBinder();
/*  784 */     binder.setLocalData(this.m_transferProps);
/*  785 */     DataBinder.mergeHashTables(binder.getLocalData(), props);
/*      */ 
/*  788 */     String batchFile = props.getProperty("aBatchFile");
/*      */ 
/*  791 */     String batchName = TransferUtils.computeBatchName(batchFile);
/*  792 */     String targetDir = ArchiveUtils.buildArchiveDirectory(this.m_targetCollection.m_exportLocation, this.m_targetArchive);
/*      */ 
/*  794 */     reportTransferProgress(LocaleUtils.encodeMessage("csProgressTransferringBatchFile", null, batchFile), -1.0F, -1.0F);
/*      */ 
/*  796 */     String zipName = TransferUtils.createPackage(binder, this.m_srcArchiveExportDir, true, targetDir, batchName, this.m_importStateInfo);
/*  797 */     this.m_binder.addTempFile(zipName);
/*  798 */     binder.putLocal("ZipFile:path", zipName);
/*      */ 
/*  801 */     reportTransferProgress(LocaleUtils.encodeMessage("csProgressUnpackagingTransfer", null, batchFile), -1.0F, -1.0F);
/*      */ 
/*  803 */     binder.setReportProgress(this);
/*  804 */     TransferUtils.receiveTransfer(binder, this.m_targetCollection, this.m_targetArchive, false);
/*  805 */     reportTransferProgress(LocaleUtils.encodeMessage("csProgressSuccessfullyUnpackaged", null, batchFile), -1.0F, -1.0F);
/*      */   }
/*      */ 
/*      */   protected boolean checkTransferStatus(Properties props, DataBinder transferData)
/*      */     throws ServiceException
/*      */   {
/*  812 */     String targetArchiveDir = ArchiveUtils.buildArchiveDirectory(this.m_targetCollection.m_location, this.m_targetArchive);
/*  813 */     DataBinder exportData = ArchiveUtils.readExportsFile(targetArchiveDir, null);
/*  814 */     DataResultSet exportSet = (DataResultSet)exportData.getResultSet("BatchFiles");
/*      */ 
/*  816 */     Vector row = null;
/*  817 */     if (exportSet != null)
/*      */     {
/*  819 */       String batchFile = props.getProperty("aBatchFile");
/*  820 */       String[] batchLocations = ArchiveUtils.breakUpBatchPath(this.m_srcArchiveExportDir, batchFile);
/*  821 */       row = exportSet.findRow(0, batchLocations[1]);
/*      */     }
/*  823 */     return row != null;
/*      */   }
/*      */ 
/*      */   protected void removeFromExports(Properties props) throws ServiceException, DataException
/*      */   {
/*  828 */     String batchFile = props.getProperty("aBatchFile");
/*  829 */     ArchiveUtils.deleteBatchFile(batchFile, this.m_srcArchiveDir, this.m_srcArchiveExportDir, this.m_srcCollection);
/*      */   }
/*      */ 
/*      */   protected void updateArchiveTransferData(Properties props, boolean isIncrement)
/*      */     throws ServiceException
/*      */   {
/*  838 */     this.m_transferData.m_lastUpdated = LocaleUtils.formatODBC(new Date());
/*  839 */     if (isIncrement)
/*      */     {
/*  841 */       this.m_transferData.m_numBatchFiles += 1;
/*  842 */       this.m_transferData.m_numRevisions += NumberUtils.parseInteger(props.getProperty("aNumDocuments"), 0);
/*      */     }
/*      */ 
/*  846 */     boolean isSourceTarget = this.m_targetCollection.m_name.equals(this.m_srcCollection.m_name);
/*      */ 
/*  848 */     boolean isNotifySource = false;
/*  849 */     boolean isNotifyTarget = false;
/*      */ 
/*  851 */     String totalStr = LocaleUtils.encodeMessage("csBatchFilesRevisionsTotal", null, "" + this.m_transferData.m_numBatchFiles, "" + this.m_transferData.m_numRevisions);
/*      */     try
/*      */     {
/*  855 */       if (isSourceTarget)
/*      */       {
/*  857 */         FileUtils.reserveDirectory(this.m_srcArchiveDir);
/*      */         try
/*      */         {
/*  860 */           updateSourceCounts(totalStr, null);
/*  861 */           updateTargetCounts(totalStr, null);
/*  862 */           isNotifySource = true;
/*  863 */           isNotifyTarget = true;
/*      */         }
/*      */         finally
/*      */         {
/*  867 */           FileUtils.releaseDirectory(this.m_srcArchiveDir);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  872 */         if ((this.m_transferType == 0) || (this.m_transferType == 2))
/*      */         {
/*  875 */           updateSourceCounts(totalStr, this.m_srcArchiveDir);
/*  876 */           isNotifySource = true;
/*      */         }
/*      */ 
/*  879 */         if ((this.m_transferType == 0) || (this.m_transferType == 1))
/*      */         {
/*  882 */           updateTargetCounts(totalStr, this.m_targetArchiveDir);
/*  883 */           isNotifyTarget = true;
/*      */         }
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*      */       String monikerString;
/*      */       String monikerString;
/*  890 */       if (this.m_transferType == 2)
/*      */       {
/*  892 */         this.m_transferProps.put("aLastTransferIn", this.m_transferData.m_lastUpdated);
/*  893 */         this.m_transferProps.put("aTotalTransferedIn", totalStr);
/*      */       }
/*  895 */       else if (this.m_transferType == 1)
/*      */       {
/*  897 */         this.m_transferProps.put("aLastTransferOut", this.m_transferData.m_lastUpdated);
/*  898 */         this.m_transferProps.put("aTotalTransferedOut", totalStr);
/*      */       }
/*      */ 
/*  901 */       if (isNotifySource)
/*      */       {
/*  903 */         String monikerString = this.m_srcCollection.getMoniker();
/*  904 */         MonikerWatcher.notifyChanged(monikerString);
/*  905 */         if ((isNotifyTarget) && (isSourceTarget))
/*      */         {
/*  907 */           isNotifyTarget = false;
/*      */         }
/*      */       }
/*  910 */       if (isNotifyTarget)
/*      */       {
/*  912 */         String monikerString = this.m_targetCollection.getMoniker();
/*  913 */         MonikerWatcher.notifyChanged(monikerString);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void updateSourceCounts(String totalStr, String dir) throws ServiceException
/*      */   {
/*  920 */     if (dir != null)
/*      */     {
/*  922 */       FileUtils.reserveDirectory(dir);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  927 */       DataBinder archiveData = ArchiveUtils.readArchiveFile(this.m_srcCollection.m_location, this.m_srcArchive, false);
/*  928 */       archiveData.putLocal("aLastTransferOut", this.m_transferData.m_lastUpdated);
/*  929 */       archiveData.putLocal("aTotalTransferedOut", totalStr);
/*      */ 
/*  931 */       ArchiveUtils.writeArchiveFile(this.m_srcArchiveDir, archiveData, false);
/*      */     }
/*      */     finally
/*      */     {
/*  935 */       if (dir != null)
/*      */       {
/*  937 */         FileUtils.releaseDirectory(dir);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void updateTargetCounts(String totalStr, String dir) throws ServiceException
/*      */   {
/*  944 */     if (dir != null)
/*      */     {
/*  946 */       FileUtils.reserveDirectory(dir);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  951 */       DataBinder archiveData = ArchiveUtils.readArchiveFile(this.m_targetCollection.m_location, this.m_targetArchive, false);
/*  952 */       archiveData.putLocal("aLastTransferIn", this.m_transferData.m_lastUpdated);
/*  953 */       archiveData.putLocal("aTotalTransferedIn", totalStr);
/*      */ 
/*  955 */       String targetArchiveDir = ArchiveUtils.buildArchiveDirectory(this.m_targetCollection.m_location, this.m_targetArchive);
/*  956 */       ArchiveUtils.writeArchiveFile(targetArchiveDir, archiveData, false);
/*      */     }
/*      */     finally
/*      */     {
/*  960 */       if (dir != null)
/*      */       {
/*  962 */         FileUtils.releaseDirectory(dir);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void reportProgress(int type, String msg, float amtDone, float max)
/*      */   {
/*  972 */     if (this.m_progress == null)
/*      */       return;
/*  974 */     String localReportMsg = this.m_statusMsg + msg;
/*  975 */     this.m_progress.reportProgress(1, localReportMsg, amtDone, max);
/*      */   }
/*      */ 
/*      */   protected void reportTransferProgress(String msg, float amtDone, float total)
/*      */     throws ServiceException
/*      */   {
/*  991 */     checkForAbort();
/*  992 */     if (this.m_progress == null)
/*      */       return;
/*  994 */     String locaReportMsg = this.m_statusMsg + msg;
/*  995 */     this.m_progress.reportProgress(1, locaReportMsg, amtDone, total);
/*      */   }
/*      */ 
/*      */   protected void checkForAbort()
/*      */     throws ServiceException
/*      */   {
/* 1010 */     if ((this.m_isAuto) || (!this.m_isAbort))
/*      */       return;
/* 1012 */     throw new ServiceException(LocaleUtils.encodeMessage("csArchiveSuffixAbortedTime", null, new Date()));
/*      */   }
/*      */ 
/*      */   public void cancelTransfer()
/*      */   {
/* 1019 */     this.m_isAbort = true;
/*      */   }
/*      */ 
/*      */   protected void createServiceException(Exception e)
/*      */     throws ServiceException
/*      */   {
/*      */     String errMsg;
/*      */     String errMsg;
/* 1027 */     if (this.m_targetArchive != null)
/*      */     {
/*      */       String errMsg;
/* 1029 */       if (this.m_targetCollection != null)
/*      */       {
/* 1031 */         errMsg = LocaleUtils.encodeMessage("csTransferErrorBetweenSourceAndTargetArchiveCollection", null, new Object[] { this.m_srcArchive, this.m_srcCollection.m_name, this.m_targetArchive, this.m_targetCollection.m_name });
/*      */       }
/*      */       else
/*      */       {
/* 1037 */         errMsg = LocaleUtils.encodeMessage("csTransferErrorBetweenSourceAndTargetArchive", null, new Object[] { this.m_srcArchive, this.m_srcCollection.m_name, this.m_targetArchive });
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*      */       String errMsg;
/* 1041 */       if (this.m_targetCollection != null)
/*      */       {
/* 1043 */         errMsg = LocaleUtils.encodeMessage("csTransferErrorBetweenSourceAndTargetCollection", null, new Object[] { this.m_srcArchive, this.m_srcCollection.m_name }, this.m_targetCollection.m_name);
/*      */       }
/*      */       else
/*      */       {
/* 1048 */         errMsg = LocaleUtils.encodeMessage("csTransferErrorBetweenSourceAndTarget", null, new Object[] { this.m_srcArchive, this.m_srcCollection.m_name });
/*      */       }
/*      */     }
/*      */ 
/* 1052 */     Report.appError("archiver", null, errMsg, e);
/*      */ 
/* 1055 */     String statusMsg = LocaleUtils.encodeMessage("csTransferErrorForArchiveInCollection", null, this.m_srcArchive, this.m_srcCollection.m_name);
/*      */ 
/* 1058 */     throw new ServiceException(statusMsg, e);
/*      */   }
/*      */ 
/*      */   protected void buildMessageStub()
/*      */   {
/* 1063 */     this.m_statusMsg = LocaleUtils.encodeMessage("csTransferingArchiveInCollection", null, this.m_srcArchive, this.m_srcCollection.m_name);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1069 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97046 $";
/*      */   }
/*      */ 
/*      */   static class TransferData
/*      */   {
/* 1075 */     public int m_numBatchFiles = 0;
/* 1076 */     public int m_numRevisions = 0;
/* 1077 */     public String m_dateStarted = null;
/* 1078 */     public String m_lastUpdated = null;
/*      */ 
/*      */     public TransferData()
/*      */     {
/* 1082 */       this.m_dateStarted = LocaleUtils.formatODBC(new Date());
/* 1083 */       this.m_lastUpdated = this.m_dateStarted;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.TransferHandler
 * JD-Core Version:    0.5.4
 */