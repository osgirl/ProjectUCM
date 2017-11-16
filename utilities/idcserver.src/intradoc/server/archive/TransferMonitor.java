/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.LoggingUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.zip.ZipFunctions;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TransferMonitor
/*     */ {
/*  34 */   protected static String m_curStatus = null;
/*     */ 
/*  36 */   protected static boolean m_isInitialized = false;
/*  37 */   protected static long m_waitTime = 30000L;
/*     */ 
/*  39 */   protected static long m_lastTimestamp = -2L;
/*  40 */   protected static String m_transferError = null;
/*     */ 
/*  42 */   protected static String m_lockObject = "TransferMonitorLock";
/*     */ 
/*     */   public static void checkInit()
/*     */   {
/*  46 */     if (m_isInitialized)
/*     */     {
/*  48 */       return;
/*     */     }
/*     */ 
/*  51 */     Runnable run = new Object()
/*     */     {
/*     */       public void run()
/*     */       {
/*  55 */         SystemUtils.registerSynchronizationObjectToNotifyOnStop(TransferMonitor.m_lockObject);
/*  56 */         while (!SystemUtils.m_isServerStopped)
/*     */         {
/*  58 */           long ts = 0L;
/*     */ 
/*  62 */           int count = 0;
/*  63 */           synchronized (TransferMonitor.m_lockObject)
/*     */           {
/*  65 */             ts = ArchiveUtils.checkTransferFile();
/*  66 */             int retryCount = (TransferMonitor.m_transferError == null) ? 1000 : 20;
/*  67 */             if ((ts == TransferMonitor.m_lastTimestamp) && (count++ < retryCount))
/*     */             {
/*  69 */               count = 0;
/*     */               try
/*     */               {
/*  72 */                 SystemUtils.wait(TransferMonitor.m_lockObject, TransferMonitor.m_waitTime);
/*     */               }
/*     */               catch (Exception e)
/*     */               {
/*  76 */                 Report.trace("transfermonitor", null, e);
/*     */               }
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */           try
/*     */           {
/*  84 */             Report.trace("transfermonitor", "Timestamp changed, looking for work.", null);
/*  85 */             TransferMonitor.checkForAndDoTransferWork(ts);
/*     */           }
/*     */           catch (ServiceException e)
/*     */           {
/*  90 */             Report.appError("archiver", "transfermonitor", "!csArchiverTargetTransferError", e);
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/*  96 */             Report.appFatal("archiver", "transfermonitor", "!csArchiverTargetTransferError", t);
/*     */ 
/* 100 */             SystemUtils.sleep(1800000L);
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 106 */     Thread bgThread = new Thread(run);
/* 107 */     bgThread.setDaemon(true);
/* 108 */     bgThread.setName("Archive TransferMonitor");
/* 109 */     bgThread.start();
/*     */ 
/* 111 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public static void notifyOfWork()
/*     */   {
/* 116 */     synchronized (m_lockObject)
/*     */     {
/* 118 */       m_lockObject.notify();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void checkForAndDoTransferWork(long ts)
/*     */     throws ServiceException
/*     */   {
/* 129 */     String workListingDir = LegacyDirectoryLocator.getCollectionsDirectory();
/* 130 */     String prefix = LoggingUtils.getLogFileMsgPrefix();
/* 131 */     String agent = "";
/* 132 */     if (prefix != null)
/*     */     {
/* 134 */       agent = prefix;
/*     */     }
/* 136 */     agent = agent + "transfer-monitor";
/* 137 */     if (!FileUtils.reserveLongTermLock(workListingDir, "transfer", agent, 5L * m_waitTime, false))
/*     */     {
/* 139 */       Report.trace("transfermonitor", "Background transfer operation blocked by lock placed by another process.", null);
/* 140 */       return;
/*     */     }
/* 142 */     m_lastTimestamp = ts;
/*     */     try
/*     */     {
/* 146 */       boolean moreWork = true;
/* 147 */       while (moreWork)
/*     */       {
/* 149 */         Report.trace("transfermonitor", "Checking for transfer work.", null);
/* 150 */         TransferInfo info = checkForWork();
/* 151 */         if (info != null)
/*     */         {
/*     */           try
/*     */           {
/* 155 */             Report.trace("transfermonitor", "Checking for transfer work.", null);
/* 156 */             doTransferWork(info);
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 160 */             info.m_properties.put("aTransferStatus", "failed");
/* 161 */             String msg = LocaleUtils.encodeMessage("csArchiverTargetTransferError", e.getMessage());
/*     */ 
/* 163 */             info.m_properties.put("aTransferErrorMsg", msg);
/* 164 */             info.m_failed = true;
/* 165 */             Report.appError("archiver", null, "!csArchiverTargetTransferError", e);
/*     */           }
/*     */           finally
/*     */           {
/* 169 */             info.m_isFinished = true;
/* 170 */             TransferUtils.updateTransferInfo(info, false);
/* 171 */             sendTransferStatus(info);
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 177 */           moreWork = false;
/*     */         }
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 183 */       FileUtils.releaseLongTermLock(workListingDir, "transfer", agent);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static TransferInfo checkForWork()
/*     */   {
/* 189 */     TransferInfo info = null;
/*     */     try
/*     */     {
/* 194 */       DataBinder transferData = ArchiveUtils.readTransferData(false);
/* 195 */       DataResultSet transferSet = (DataResultSet)transferData.getResultSet("Transfers");
/* 196 */       if (transferSet != null)
/*     */       {
/* 198 */         int index = ResultSetUtils.getIndexMustExist(transferSet, "aTransferStatus");
/* 199 */         for (; transferSet.isRowPresent(); transferSet.next())
/*     */         {
/* 201 */           String status = transferSet.getStringValue(index);
/* 202 */           if (status.equals("success"))
/*     */             continue;
/* 204 */           info = new TransferInfo(transferSet.getCurrentRowProps(), null);
/* 205 */           break;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 212 */       m_transferError = "!csArchiverUnableToCheckForWork";
/* 213 */       Report.appError("archiver", null, "!csArchiverUnableToCheckForWork", e);
/*     */     }
/*     */ 
/* 216 */     return info;
/*     */   }
/*     */ 
/*     */   protected static void doTransferWork(TransferInfo info) throws DataException, ServiceException
/*     */   {
/* 221 */     Properties props = info.m_properties;
/* 222 */     String targetPath = props.getProperty("aTargetPath");
/*     */ 
/* 224 */     String[] location = ArchiveUtils.parseLocation(targetPath);
/* 225 */     CollectionData targetCollection = ArchiveUtils.getCollection(location[0]);
/* 226 */     if (targetCollection == null)
/*     */     {
/* 228 */       String msg = LocaleUtils.encodeMessage("csArchiverCollectionNotFound", null, location[0]);
/*     */ 
/* 230 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 233 */     Report.trace("transfermonitor", "doTransferWork tragetPath = " + targetPath, null);
/*     */ 
/* 236 */     String archiveDir = ArchiveUtils.buildArchiveDirectory(targetCollection.m_location, location[1]);
/* 237 */     String archiveExportDir = ArchiveUtils.buildArchiveDirectory(targetCollection.m_exportLocation, location[1]);
/*     */ 
/* 239 */     Report.trace("transfermonitor", "doTransferWork archiveDir = " + archiveDir + ", archiveExportDir = " + archiveExportDir, null);
/*     */ 
/* 242 */     String batchFile = props.getProperty("aBatchFile");
/* 243 */     String batchName = null;
/* 244 */     int index = batchFile.lastIndexOf('/');
/* 245 */     if (index >= 0)
/*     */     {
/* 247 */       batchName = batchFile.substring(index + 1);
/*     */     }
/*     */     else
/*     */     {
/* 251 */       batchName = batchFile;
/*     */     }
/* 253 */     index = batchName.indexOf('.');
/* 254 */     if (index >= 0)
/*     */     {
/* 256 */       batchName = batchName.substring(0, index);
/*     */     }
/*     */ 
/* 259 */     String zipPath = archiveExportDir + "temp/" + batchName + ".zip";
/* 260 */     Report.trace("transfermonitor", "Extracting " + zipPath + " to " + archiveExportDir, null);
/* 261 */     ZipFunctions.extractZipFiles(zipPath, archiveExportDir);
/*     */ 
/* 264 */     FileUtils.reserveDirectory(targetCollection.m_location);
/*     */     try
/*     */     {
/* 267 */       PropParameters params = new PropParameters(props);
/* 268 */       DataBinder exportData = ArchiveUtils.readExportsFile(archiveDir, null);
/* 269 */       DataResultSet exportSet = (DataResultSet)exportData.getResultSet("BatchFiles");
/* 270 */       if (exportSet == null)
/*     */       {
/* 272 */         exportSet = new DataResultSet(ArchiveUtils.BATCHFILE_COLUMNS);
/* 273 */         exportData.addResultSet("BatchFiles", exportSet);
/*     */       }
/*     */ 
/* 277 */       TransferUtils.validateAndFixBatchColumns(params);
/*     */ 
/* 279 */       Vector row = exportSet.createRow(params);
/* 280 */       exportSet.addRow(row);
/*     */ 
/* 282 */       ArchiveUtils.writeExportsFile(archiveDir, exportData);
/*     */     }
/*     */     finally
/*     */     {
/* 286 */       FileUtils.releaseDirectory(targetCollection.m_location);
/*     */     }
/*     */ 
/* 289 */     FileUtils.deleteFile(zipPath);
/* 290 */     props.put("aTransferStatus", "success");
/*     */   }
/*     */ 
/*     */   public static void sendTransferStatus(TransferInfo info)
/*     */   {
/* 300 */     Properties props = info.m_properties;
/* 301 */     boolean hasConnection = StringUtils.convertToBool(props.getProperty("aHasConnection"), false);
/* 302 */     if (!hasConnection)
/*     */     {
/* 304 */       Report.trace("transfermonitor", "Not sending back update -- recipient does not request notification.", null);
/* 305 */       return;
/*     */     }
/*     */ 
/* 308 */     String transferOwner = props.getProperty("aTransferOwner");
/*     */     try
/*     */     {
/* 311 */       DataBinder outBinder = new DataBinder();
/* 312 */       DataBinder binder = new DataBinder();
/* 313 */       binder.setLocalData(props);
/* 314 */       Report.trace("transfermonitor", "Notifying original sender of transfer that transfer has been unpackaged.", null);
/*     */ 
/* 316 */       TransferUtils.executeProxiedRequest(transferOwner, "UPDATE_TRANSFER_STATUS", binder, outBinder, info.m_context, null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 321 */       String msg = LocaleUtils.encodeMessage("csArchiverTransferUnableToSend", null, transferOwner);
/*     */ 
/* 323 */       Report.appError("archiver", null, msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 329 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97783 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.TransferMonitor
 * JD-Core Version:    0.5.4
 */