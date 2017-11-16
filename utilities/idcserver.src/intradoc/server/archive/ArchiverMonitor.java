/*      */ package intradoc.server.archive;
/*      */ 
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.shared.CollectionData;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.ProgressState;
/*      */ import intradoc.shared.ProgressStateUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Observable;
/*      */ import java.util.Observer;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ArchiverMonitor
/*      */ {
/*   38 */   protected static String m_replicationDir = "./";
/*      */ 
/*   40 */   public static Workspace m_workspace = null;
/*      */ 
/*   43 */   protected static Observable m_observable = null;
/*      */ 
/*   46 */   protected static ArchiveHandler m_archiver = null;
/*   47 */   protected static TransferHandler m_transfer = null;
/*      */ 
/*   50 */   protected static Vector m_workPackets = null;
/*      */ 
/*   53 */   protected static boolean m_startArchiving = false;
/*      */ 
/*   56 */   protected static boolean m_archiverActive = false;
/*      */ 
/*   59 */   protected static int m_counterThresholdMax = 60;
/*   60 */   protected static int m_counter = 0;
/*      */ 
/*   65 */   protected static String m_curStatus = null;
/*      */ 
/*   70 */   protected static ProgressState m_internalReportProgress = null;
/*   71 */   protected static ReportProgress m_externalReportProgress = null;
/*      */ 
/*   74 */   protected static Thread m_bgThread = null;
/*      */ 
/*   77 */   protected static boolean m_doLocks = false;
/*      */ 
/*   80 */   public static int m_touchMonitorInterval = 12000;
/*      */ 
/*   83 */   static Hashtable m_activeLocks = new Hashtable();
/*      */ 
/*   87 */   protected static String m_curLockKey = null;
/*      */ 
/*      */   public static void init(String dir, Workspace ws, Observer obs)
/*      */     throws ServiceException
/*      */   {
/*   92 */     FileUtils.checkOrCreateDirectory(dir, 1);
/*      */ 
/*   94 */     m_replicationDir = dir;
/*   95 */     m_workspace = ws;
/*      */ 
/*   97 */     m_observable = new Observable()
/*      */     {
/*      */       public boolean hasChanged()
/*      */       {
/*  102 */         return true;
/*      */       }
/*      */     };
/*  106 */     if (obs != null)
/*      */     {
/*  108 */       m_observable.addObserver(obs);
/*      */     }
/*      */ 
/*  111 */     m_internalReportProgress = new ProgressState()
/*      */     {
/*      */       public void reportProgress(int type, String msg, float amtDone, float max)
/*      */       {
/*  116 */         ArchiverMonitor.m_curStatus = StringUtils.createReportProgressString(type, msg, amtDone, max);
/*      */ 
/*  118 */         if (ArchiverMonitor.m_externalReportProgress != null)
/*      */         {
/*  120 */           ArchiverMonitor.m_externalReportProgress.reportProgress(type, msg, amtDone, max);
/*      */         }
/*  122 */         ProgressStateUtils.reportProgress(ArchiverMonitor.m_internalReportProgress, "archiver", "archiver", 3, ArchiverMonitor.m_curStatus, null);
/*      */       }
/*      */ 
/*      */       public void reportProgress(int type, Throwable t, String key, Object[] args)
/*      */       {
/*  129 */         if ((type == -1) && (t != null)) {
/*  130 */           ArchiverMonitor.m_curStatus = StringUtils.createReportProgressString(type, t.getMessage(), -1.0F, -1.0F);
/*      */         }
/*      */ 
/*  133 */         boolean isReport = checkReport(type, t);
/*  134 */         if (!isReport)
/*      */           return;
/*  136 */         IdcMessage idcMsg = IdcMessageFactory.lc(key, args);
/*  137 */         String msg = LocaleUtils.encodeMessage(idcMsg);
/*  138 */         report(type, t, msg);
/*      */       }
/*      */     };
/*  144 */     m_internalReportProgress.init("Archiver");
/*      */ 
/*  146 */     m_doLocks = SharedObjects.getEnvValueAsBoolean("ArchiverDoLocks", false);
/*  147 */     m_touchMonitorInterval = SharedObjects.getTypedEnvironmentInt("ArchiverTouchMonitorInterval", m_touchMonitorInterval, 18, 18);
/*      */ 
/*  151 */     if (!m_doLocks)
/*      */       return;
/*  153 */     createLockUpdateThread();
/*      */   }
/*      */ 
/*      */   public static void createLockUpdateThread()
/*      */   {
/*  159 */     Thread notifyThread = new Thread("Archive lock")
/*      */     {
/*      */       public void run()
/*      */       {
/*  164 */         long lastTime = System.currentTimeMillis();
/*  165 */         boolean checkStatusAll = true;
/*      */         while (true)
/*      */         {
/*      */           try
/*      */           {
/*  170 */             String curLockKey = ArchiverMonitor.m_curLockKey;
/*  171 */             if (curLockKey != null)
/*      */             {
/*  173 */               if (SystemUtils.m_verbose)
/*      */               {
/*  175 */                 Report.debug("archiverlocks", "*Background touching file " + curLockKey, null);
/*      */               }
/*  177 */               FileUtils.touchFile(ArchiverMonitor.m_replicationDir + curLockKey);
/*      */             }
/*      */ 
/*  180 */             long curTime = System.currentTimeMillis();
/*  181 */             long diff = curTime - lastTime;
/*  182 */             lastTime = curTime;
/*      */ 
/*  190 */             long reduceInterval = diff + ArchiverMonitor.m_touchMonitorInterval / 2;
/*  191 */             if (diff > ArchiverMonitor.m_touchMonitorInterval / 3)
/*      */             {
/*  193 */               ArchiverMonitor.m_internalReportProgress.reportProgress(3, null, null, new Object[] { "csArchiveBackgroundThreadTooLong" });
/*      */             }
/*      */ 
/*  196 */             int sleepTime = 200;
/*  197 */             if ((reduceInterval < ArchiverMonitor.m_touchMonitorInterval) && (reduceInterval >= 0L))
/*      */             {
/*  199 */               sleepTime = ArchiverMonitor.m_touchMonitorInterval - (int)reduceInterval;
/*      */             }
/*  201 */             SystemUtils.sleep(sleepTime);
/*  202 */             lastTime += sleepTime;
/*      */ 
/*  205 */             if (checkStatusAll)
/*      */             {
/*  207 */               curLockKey = ArchiverMonitor.m_curLockKey;
/*  208 */               String[] list = FileUtils.getMatchingFileNames(ArchiverMonitor.m_replicationDir, "*.lck");
/*  209 */               Hashtable newActiveList = new Hashtable();
/*  210 */               if ((list != null) && (list.length > 0))
/*      */               {
/*  212 */                 for (int i = 0; i < list.length; ++i)
/*      */                 {
/*  214 */                   String key = list[i].toLowerCase();
/*  215 */                   ArchiverIndexLockStatus curLock = (ArchiverIndexLockStatus)ArchiverMonitor.m_activeLocks.get(key);
/*  216 */                   boolean isNew = false;
/*  217 */                   if (curLock == null)
/*      */                   {
/*  219 */                     String path = ArchiverMonitor.m_replicationDir + key;
/*  220 */                     curLock = ArchiverMonitor.createNewLock(path);
/*  221 */                     isNew = true;
/*      */                   }
/*      */ 
/*  227 */                   boolean processOwns = (curLockKey != null) && (curLockKey.equals(key));
/*  228 */                   if (processOwns != curLock.m_processOwnsLock)
/*      */                   {
/*  230 */                     ProgressStateUtils.reportProgress(ArchiverMonitor.m_internalReportProgress, null, "archiverlocks", 3, "Archive lock " + key + " improperly indicates process ownership " + curLock.m_processOwnsLock + " " + System.currentTimeMillis(), null);
/*      */ 
/*  236 */                     curLock.m_processOwnsLock = processOwns;
/*      */                   }
/*  238 */                   boolean addToActive = true;
/*  239 */                   if (!isNew)
/*      */                   {
/*  241 */                     long newLastModified = curLock.m_file.lastModified();
/*  242 */                     if (!curLock.m_processOwnsLock)
/*      */                     {
/*  244 */                       if ((newLastModified != curLock.m_lastModified) && (newLastModified > 0L))
/*      */                       {
/*  246 */                         curLock.m_hasChanged = true;
/*  247 */                         curLock.m_lastTime = System.currentTimeMillis();
/*      */                       }
/*  251 */                       else if (!curLock.m_hasChanged)
/*      */                       {
/*  254 */                         addToActive = false;
/*  255 */                         ProgressStateUtils.reportProgress(ArchiverMonitor.m_internalReportProgress, null, "archiverlocks", 3, "Archive lock " + key + " deleted (not maintained)", null);
/*      */ 
/*  259 */                         if (newLastModified > 0L)
/*      */                         {
/*  261 */                           curLock.m_file.delete();
/*      */                         }
/*  263 */                         curLock.m_isLocked = false;
/*      */                       }
/*      */                       else
/*      */                       {
/*  267 */                         curLock.m_hasChanged = false;
/*      */                       }
/*      */ 
/*      */                     }
/*  273 */                     else if (curLock.m_isLocked)
/*      */                     {
/*  275 */                       curLock.m_hasChanged = true;
/*      */                     }
/*      */ 
/*  278 */                     curLock.m_lastModified = newLastModified;
/*  279 */                     if (curLock.m_hasChanged)
/*      */                     {
/*  281 */                       curLock.m_lastTime = System.currentTimeMillis();
/*      */                     }
/*      */                   }
/*  284 */                   if (!addToActive)
/*      */                     continue;
/*  286 */                   if ((isNew) || ((!curLock.m_isLocked) && (!curLock.m_processOwnsLock)))
/*      */                   {
/*  288 */                     curLock.m_isLocked = true;
/*  289 */                     if (SystemUtils.m_verbose)
/*      */                     {
/*  291 */                       Report.debug("archiverlocks", "*Archive lock " + key + " now active", null);
/*      */                     }
/*      */                   }
/*  294 */                   newActiveList.put(key, curLock);
/*      */                 }
/*      */               }
/*      */ 
/*  298 */               if (SystemUtils.m_verbose)
/*      */               {
/*  300 */                 Enumeration en = ArchiverMonitor.m_activeLocks.keys();
/*  301 */                 while (en.hasMoreElements())
/*      */                 {
/*  303 */                   String key = (String)en.nextElement();
/*  304 */                   if (newActiveList.get(key) == null)
/*      */                   {
/*  306 */                     Report.debug("archiverlocks", "*Archive lock " + key + " no longer active", null);
/*      */                   }
/*      */                 }
/*      */               }
/*      */ 
/*  311 */               ArchiverMonitor.m_activeLocks = newActiveList;
/*  312 */               checkStatusAll = false;
/*      */             }
/*      */             else
/*      */             {
/*  316 */               checkStatusAll = true;
/*      */             }
/*      */           }
/*      */           catch (Throwable ignore)
/*      */           {
/*  321 */             Report.trace(null, null, ignore);
/*  322 */             ignore.printStackTrace();
/*      */ 
/*  328 */             SystemUtils.sleep(200L);
/*      */           }
/*      */         }
/*      */       }
/*      */     };
/*  334 */     notifyThread.setDaemon(true);
/*  335 */     notifyThread.start();
/*      */   }
/*      */ 
/*      */   static boolean checkArchiveLock(String key, boolean assertLock)
/*      */     throws ServiceException
/*      */   {
/*  342 */     if ((key == null) || (key.length() == 0))
/*      */     {
/*  344 */       return false;
/*      */     }
/*  346 */     ArchiverIndexLockStatus curLock = (ArchiverIndexLockStatus)m_activeLocks.get(key);
/*  347 */     if ((curLock != null) && (curLock.m_processOwnsLock) && (curLock.m_isLocked) && (m_curLockKey != null) && (key.equals(m_curLockKey)))
/*      */     {
/*  350 */       return false;
/*      */     }
/*      */ 
/*  353 */     boolean isLocked = true;
/*      */     try
/*      */     {
/*  356 */       FileUtils.reserveDirectory(m_replicationDir);
/*  357 */       boolean isNew = false;
/*  358 */       if (curLock == null)
/*      */       {
/*  360 */         isNew = true;
/*  361 */         String path = new StringBuilder().append(m_replicationDir).append(key).toString();
/*  362 */         curLock = createNewLock(path);
/*  363 */         isLocked = curLock.m_lastModified > 0L;
/*      */       }
/*      */       else
/*      */       {
/*  367 */         long newLastModified = curLock.m_file.lastModified();
/*  368 */         if (newLastModified <= 0L)
/*      */         {
/*  370 */           isLocked = false;
/*      */         }
/*      */         else
/*      */         {
/*  374 */           long curTime = System.currentTimeMillis();
/*  375 */           if (newLastModified != curLock.m_lastModified)
/*      */           {
/*  377 */             curLock.m_hasChanged = true;
/*  378 */             curLock.m_lastModified = newLastModified;
/*  379 */             curLock.m_lastTime = curTime;
/*      */           }
/*  383 */           else if (curTime - curLock.m_lastTime > m_touchMonitorInterval)
/*      */           {
/*  385 */             isLocked = false;
/*  386 */             curLock.m_hasChanged = false;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  392 */       if ((isNew) && (((assertLock) || (isLocked))))
/*      */       {
/*  394 */         m_activeLocks.put(key, curLock);
/*      */       }
/*  396 */       if (!isLocked)
/*      */       {
/*  398 */         if (assertLock)
/*      */         {
/*  402 */           FileUtils.touchFile(new StringBuilder().append(m_replicationDir).append(key).toString());
/*  403 */           m_curLockKey = key;
/*  404 */           curLock.m_processOwnsLock = true;
/*  405 */           curLock.m_isLocked = true;
/*  406 */           ProgressStateUtils.traceProgress(m_internalReportProgress, "archiveLocks", new StringBuilder().append("Archive lock ").append(key).append(" created ").append(System.currentTimeMillis()).toString(), null);
/*      */         }
/*      */         else
/*      */         {
/*  411 */           curLock.m_isLocked = false;
/*      */         }
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  417 */       FileUtils.releaseDirectory(m_replicationDir);
/*      */     }
/*      */ 
/*  420 */     return isLocked;
/*      */   }
/*      */ 
/*      */   public static void setReportProgressCallback(ReportProgress callback)
/*      */   {
/*  425 */     m_externalReportProgress = callback;
/*      */   }
/*      */ 
/*      */   public static String getCurrentStatus()
/*      */   {
/*  430 */     return m_curStatus;
/*      */   }
/*      */ 
/*      */   public static void reserveAndStartArchiving(DataBinder binder) throws DataException, ServiceException
/*      */   {
/*  435 */     synchronized (m_observable)
/*      */     {
/*  437 */       if (m_startArchiving)
/*      */       {
/*  439 */         throw new ServiceException("!csArchiverAlreadyStarting");
/*      */       }
/*      */ 
/*  442 */       if (m_archiverActive)
/*      */       {
/*  444 */         throw new ServiceException("!csArchiverAlreadyActive");
/*      */       }
/*      */ 
/*  450 */       Vector workPackets = new IdcVector();
/*      */ 
/*  453 */       DataBinder archData = new DataBinder(SharedObjects.getSecureEnvironment());
/*  454 */       archData.merge(binder);
/*      */ 
/*  456 */       if (m_doLocks)
/*      */       {
/*  458 */         String fileKey = getArchiveFileKeyFromData(archData);
/*  459 */         if ((fileKey != null) && 
/*  461 */           (checkArchiveLock(fileKey, true)))
/*      */         {
/*  464 */           throw new ServiceException("!csArchiveLockedByExecutionThread");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  469 */       workPackets.addElement(archData);
/*      */ 
/*  471 */       startArchiving(workPackets);
/*      */ 
/*  473 */       m_internalReportProgress.reportProgress(1, "!csArchiverInitiating", -1.0F, -1.0F);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void startArchiving(Vector workPackets)
/*      */   {
/*  489 */     m_startArchiving = true;
/*  490 */     m_workPackets = workPackets;
/*      */   }
/*      */ 
/*      */   public static void cancelArchiving() throws ServiceException
/*      */   {
/*  495 */     synchronized (m_observable)
/*      */     {
/*  497 */       if (!m_archiverActive)
/*      */       {
/*  499 */         throw new ServiceException("!csArchiverCannotCancel");
/*      */       }
/*      */ 
/*  502 */       if (m_archiver != null)
/*      */       {
/*  504 */         m_archiver.cancelArchiving();
/*  505 */         m_internalReportProgress.reportProgress(-1, "!csArchiverCanceling", -1.0F, -1.0F);
/*      */       }
/*  508 */       else if (m_transfer != null)
/*      */       {
/*  510 */         m_transfer.cancelTransfer();
/*  511 */         m_internalReportProgress.reportProgress(-1, "!csTransferCanceling", -1.0F, -1.0F);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void doArchiving(DataBinder binder)
/*      */     throws ServiceException, DataException
/*      */   {
/*  519 */     if (binder == null)
/*      */     {
/*  522 */       return;
/*      */     }
/*      */ 
/*  525 */     String archiveName = ArchiveUtils.getArchiveName(binder);
/*  526 */     String collName = ArchiveUtils.getCollectionName(binder);
/*      */ 
/*  528 */     m_internalReportProgress.updateState(binder);
/*  529 */     ProgressStateUtils.reportProgress(m_internalReportProgress, "archiver", "archiver", 4, null, "csArchiveStart", new Object[] { archiveName, collName });
/*      */ 
/*  532 */     boolean isExport = StringUtils.convertToBool(binder.getLocal("IsExport"), false);
/*  533 */     boolean isAutoImport = StringUtils.convertToBool(binder.getLocal("IsAutoImport"), false);
/*      */ 
/*  535 */     boolean isTransfer = StringUtils.convertToBool(binder.getLocal("IsTransfer"), false);
/*  536 */     boolean isAutoTransfer = StringUtils.convertToBool(binder.getLocal("IsAutoTransfer"), false);
/*      */ 
/*  538 */     boolean isQueued = StringUtils.convertToBool(binder.getLocal("IsQueued"), false);
/*      */ 
/*  540 */     String repTypeStr = binder.getLocal("AutomationType");
/*  541 */     int repType = -1;
/*  542 */     if (repTypeStr != null)
/*      */     {
/*  544 */       repType = ReplicationData.determineType(repTypeStr);
/*      */     }
/*  546 */     if (isAutoTransfer)
/*      */     {
/*  548 */       isTransfer = true;
/*      */     }
/*  550 */     String fileKey = null;
/*  551 */     if ((!isExport) && (m_doLocks))
/*      */     {
/*  553 */       fileKey = getArchiveFileKey(archiveName, collName, isTransfer);
/*  554 */       if (checkArchiveLock(fileKey, true))
/*      */       {
/*  557 */         ProgressStateUtils.traceProgress(m_internalReportProgress, "archiverlocks", new StringBuilder().append("Archive lock ").append(fileKey).append(" prevents action").toString(), null);
/*      */ 
/*  559 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  563 */     boolean isError = false;
/*      */     try
/*      */     {
/*  566 */       if (isTransfer)
/*      */       {
/*  568 */         Object obj = ComponentClassFactory.createClassInstance("TransferHandler", "intradoc.server.archive.TransferHandler", "!csTransferHandlerError");
/*      */ 
/*  570 */         m_transfer = (TransferHandler)obj;
/*  571 */         m_transfer.initObjects(m_workspace, m_internalReportProgress, m_observable);
/*  572 */         m_transfer.init(binder);
/*  573 */         m_transfer.transferArchive();
/*      */       }
/*      */       else
/*      */       {
/*  577 */         Object obj = ComponentClassFactory.createClassInstance("ArchiveHandler", "intradoc.server.archive.ArchiveHandler", "!csArchiveHandlerError");
/*      */ 
/*  579 */         m_archiver = (ArchiveHandler)obj;
/*  580 */         m_archiver.initObjects(m_workspace, m_internalReportProgress, m_observable);
/*  581 */         m_archiver.doArchiving(binder, isExport);
/*      */       }
/*      */ 
/*  584 */       if ((((isAutoImport) || (isAutoTransfer))) && (!isQueued))
/*      */       {
/*  588 */         updateTargetTS(binder, isTransfer, repType);
/*      */       }
/*      */     }
/*      */     catch (Exception curLock)
/*      */     {
/*      */       String location;
/*      */       ArchiverIndexLockStatus curLock;
/*  593 */       ProgressStateUtils.reportAppError(m_internalReportProgress, "archiver", "archiver", e, "csArchiveMonitorError", new Object[0]);
/*      */ 
/*  595 */       Report.trace("archiverLocks", e, "csArchiveMonitorError", new Object[0]);
/*  596 */       isError = true;
/*      */     }
/*      */     finally
/*      */     {
/*      */       String location;
/*      */       ArchiverIndexLockStatus curLock;
/*  600 */       if (repType >= 0)
/*      */       {
/*  602 */         String location = binder.getLocal("aLocation");
/*  603 */         if (location != null)
/*      */         {
/*  605 */           updateReplicationCounters(location, isError, repType);
/*      */         }
/*      */ 
/*  610 */         if (isQueued)
/*      */         {
/*  612 */           ReplicationData.changeTable(repType, collName, archiveName, false);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  620 */       if ((!isExport) && (m_doLocks))
/*      */       {
/*  622 */         m_curLockKey = null;
/*  623 */         FileUtils.deleteFile(new StringBuilder().append(m_replicationDir).append(fileKey).toString());
/*  624 */         ArchiverIndexLockStatus curLock = (ArchiverIndexLockStatus)m_activeLocks.get(fileKey);
/*  625 */         if (curLock != null)
/*      */         {
/*  627 */           curLock.m_isLocked = false;
/*  628 */           curLock.m_processOwnsLock = false;
/*      */         }
/*  630 */         ProgressStateUtils.traceProgress(m_internalReportProgress, "archiverLocks", new StringBuilder().append("Deleted the file key ").append(fileKey).toString(), null);
/*      */       }
/*      */ 
/*  633 */       ProgressStateUtils.reportProgress(m_internalReportProgress, "archiver", "archiver", 2, null, "csArchiveFinished", new Object[] { archiveName, collName });
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void watchArchiving()
/*      */   {
/*  645 */     synchronized (m_observable)
/*      */     {
/*  647 */       if (m_archiverActive)
/*      */       {
/*  649 */         return;
/*      */       }
/*      */ 
/*  652 */       if (m_startArchiving)
/*      */       {
/*  654 */         m_archiverActive = true;
/*      */ 
/*  657 */         Runnable archiveRun = new Runnable()
/*      */         {
/*      */           public void run()
/*      */           {
/*      */             try
/*      */             {
/*  663 */               ArchiverMonitor.m_startArchiving = false;
/*  664 */               int size = ArchiverMonitor.m_workPackets.size();
/*  665 */               for (int i = 0; i < size; ++i)
/*      */               {
/*  667 */                 DataBinder binder = (DataBinder)ArchiverMonitor.m_workPackets.elementAt(i);
/*  668 */                 ArchiverMonitor.doArchiving(binder);
/*      */               }
/*      */             }
/*      */             catch (Throwable t)
/*      */             {
/*  673 */               IdcMessage idcMsg = IdcMessageFactory.lc(t);
/*  674 */               String msg = LocaleUtils.encodeMessage(idcMsg);
/*  675 */               if (ArchiverMonitor.m_externalReportProgress != null)
/*      */               {
/*  677 */                 Report.trace(null, t, null);
/*  678 */                 ArchiverMonitor.m_externalReportProgress.reportProgress(-1, msg, -1.0F, -1.0F);
/*      */               }
/*      */               else
/*      */               {
/*  683 */                 ProgressStateUtils.reportAppError(ArchiverMonitor.m_internalReportProgress, "archiver", "archiver", t, "csArchiveMonitorError", new Object[0]);
/*      */               }
/*      */ 
/*      */             }
/*      */             finally
/*      */             {
/*  690 */               ArchiverMonitor.endArchivingFlags();
/*      */             }
/*      */           }
/*      */         };
/*  695 */         m_bgThread = new Thread(archiveRun, "Archive run");
/*  696 */         m_bgThread.setDaemon(true);
/*  697 */         m_bgThread.start();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void endArchivingFlags()
/*      */   {
/*  704 */     synchronized (m_observable)
/*      */     {
/*  707 */       m_archiverActive = false;
/*      */ 
/*  709 */       m_bgThread = null;
/*  710 */       m_archiver = null;
/*  711 */       m_transfer = null;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void updateTargetTS(DataBinder binder, boolean isTransfer, int repType)
/*      */   {
/*  718 */     String location = binder.getLocal("aLocation");
/*  719 */     if (repType < 0)
/*      */     {
/*  721 */       ProgressStateUtils.reportProgress(m_internalReportProgress, "archiver", "archiver", 3, "Illegal automation type when updating timestamps.", null);
/*      */     }
/*      */ 
/*  724 */     if ((location == null) || (repType < 0))
/*      */     {
/*  727 */       return;
/*      */     }
/*      */ 
/*  730 */     AutomatedArchiveData data = null;
/*  731 */     data = ReplicationData.getReplicationData(location, repType);
/*  732 */     if (data == null)
/*      */       return;
/*  734 */     String archiveTargetTS = binder.getLocal("aArchiveTargetTS");
/*  735 */     String exportTargetTS = binder.getLocal("aExportTargetTS");
/*  736 */     if ((archiveTargetTS == null) || (exportTargetTS == null))
/*      */       return;
/*  738 */     data.resetForTarget(Long.parseLong(exportTargetTS), Long.parseLong(exportTargetTS));
/*      */   }
/*      */ 
/*      */   protected static void updateReplicationCounters(String location, boolean isError, int type)
/*      */   {
/*  746 */     AutomatedArchiveData data = ReplicationData.getReplicationData(location, type);
/*  747 */     if (data == null)
/*      */     {
/*  749 */       return;
/*      */     }
/*      */ 
/*  752 */     if (isError)
/*      */     {
/*  755 */       int curThreshold = data.m_counterThreshold;
/*  756 */       if (curThreshold > 0)
/*      */       {
/*  758 */         data.m_counterThreshold = ((curThreshold * 2 > m_counterThresholdMax) ? m_counterThresholdMax : curThreshold * 2);
/*      */       }
/*      */       else
/*      */       {
/*  763 */         data.m_counterThreshold = 1;
/*      */       }
/*      */ 
/*  766 */       data.m_counter = data.m_counterThreshold;
/*      */     }
/*      */     else
/*      */     {
/*  771 */       data.m_counter = 0;
/*  772 */       data.m_counterThreshold = 0;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static boolean isActive()
/*      */   {
/*  778 */     return m_archiverActive;
/*      */   }
/*      */ 
/*      */   public static boolean isStarting()
/*      */   {
/*  783 */     return m_startArchiving;
/*      */   }
/*      */ 
/*      */   public static boolean checkAutomated()
/*      */     throws ServiceException
/*      */   {
/*  793 */     m_counter += 1;
/*  794 */     if (m_counter % 5 != 0)
/*      */     {
/*  796 */       return false;
/*      */     }
/*  798 */     if (m_counter == 20)
/*      */     {
/*  801 */       m_counter = 0;
/*      */     }
/*      */ 
/*  805 */     ReplicationData.loadFromFile();
/*      */ 
/*  808 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  809 */     boolean hasChanged = false;
/*  810 */     for (int i = 0; i < ReplicationData.m_typeNames.length; ++i)
/*      */     {
/*  812 */       if (!ReplicationData.isArchiverMonitorAutomated(i))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  819 */       if (!checkForChange(idcName, i))
/*      */         continue;
/*  821 */       hasChanged = true;
/*      */     }
/*      */ 
/*  826 */     return hasChanged;
/*      */   }
/*      */ 
/*      */   protected static boolean checkForChange(String idcName, int repType)
/*      */     throws ServiceException
/*      */   {
/*  834 */     String tableName = ReplicationData.m_tableNames[repType];
/*  835 */     String typeStr = ReplicationData.m_typeNames[repType];
/*  836 */     DataResultSet rset = SharedObjects.getTable(tableName);
/*  837 */     if (rset == null)
/*      */     {
/*  840 */       return false;
/*      */     }
/*      */ 
/*  843 */     boolean markChanged = false;
/*  844 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  846 */       String location = rset.getStringValue(0);
/*  847 */       AutomatedArchiveData data = ReplicationData.getReplicationData(location, repType);
/*      */ 
/*  849 */       label305: if (data == null)
/*      */       {
/*  852 */         ProgressStateUtils.reportAppError(m_internalReportProgress, "archiver", "archiver", null, "csArchiverLocationNotFound", new Object[] { location, typeStr });
/*      */       }
/*  858 */       else if (data.m_counter > 0)
/*      */       {
/*  860 */         data.m_counter -= 1;
/*      */       }
/*      */       else {
/*  863 */         data.m_counter = data.m_counterThreshold;
/*      */ 
/*  869 */         if (ReplicationData.isQueued(repType))
/*      */         {
/*  871 */           data.m_isChanged = true;
/*      */         }
/*  873 */         boolean isProxied = false;
/*  874 */         if (!data.m_isChanged)
/*      */         {
/*  879 */           CollectionData collectionData = null;
/*      */           try
/*      */           {
/*  882 */             collectionData = ArchiveUtils.getCollection(data.m_collectionName);
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/*  886 */             ProgressStateUtils.reportAppError(m_internalReportProgress, "archiver", "archiver", e, "csArchiverUnableToRetrieveCollectionInfo", new Object[] { data.m_collectionName });
/*      */ 
/*  889 */             break label305:
/*      */           }
/*  891 */           if (collectionData == null)
/*      */           {
/*  893 */             ProgressStateUtils.reportAppError(m_internalReportProgress, "archiver", "archiver", null, "csArchiverAutomatedNoLongerExists", new Object[] { new StringBuilder().append("csArchiverType_").append(typeStr).toString(), location });
/*      */           }
/*      */           else
/*      */           {
/*  898 */             isProxied = collectionData.isProxied();
/*  899 */             if (isProxied)
/*      */             {
/*  901 */               if (m_counter % 20 == 0)
/*      */               {
/*  903 */                 data.m_isChanged = true;
/*  904 */                 markChanged = true;
/*      */               }
/*      */             }
/*      */             else
/*      */             {
/*  909 */               boolean isChanged = checkForLocalChange(idcName, location, data, repType);
/*  910 */               if (isChanged)
/*      */               {
/*  912 */                 markChanged = true;
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */         else {
/*  918 */           markChanged = true;
/*      */         }
/*      */       }
/*      */     }
/*  921 */     return markChanged;
/*      */   }
/*      */ 
/*      */   protected static boolean checkForLocalChange(String idcName, String location, AutomatedArchiveData data, int replicationType)
/*      */     throws ServiceException
/*      */   {
/*  931 */     if (!data.m_isChanged)
/*      */     {
/*  934 */       boolean isChanged = data.checkArchiveTS();
/*  935 */       if (!isChanged)
/*      */       {
/*  937 */         return false;
/*      */       }
/*      */     }
/*      */ 
/*  941 */     boolean markChanged = false;
/*  942 */     DataBinder binder = null;
/*      */     try
/*      */     {
/*  945 */       binder = ArchiveUtils.readArchiveFileForCollection(data.m_collectionName, data.m_archiveName, true);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  951 */       ProgressStateUtils.reportAppError(m_internalReportProgress, "archiver", "archiver", e, "csArchiverReplicationFileError", new Object[] { data.m_archiveName, data.m_collectionName });
/*      */ 
/*  953 */       updateReplicationCounters(location, true, replicationType);
/*  954 */       return false;
/*      */     }
/*      */ 
/*  957 */     String errMsg = null;
/*  958 */     if (!ReplicationData.isTransfer(replicationType))
/*      */     {
/*  960 */       String archiveImporter = binder.getLocal("aRegisteredImporter");
/*  961 */       if ((archiveImporter == null) || (!archiveImporter.equals(idcName)))
/*      */       {
/*  965 */         errMsg = "csArchiverAutoExportNotFound";
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  970 */       String targetArchive = binder.getLocal("aTargetArchive");
/*  971 */       boolean isAuto = StringUtils.convertToBool(binder.getLocal("aIsAutomatedTransfer"), false);
/*  972 */       if (!isAuto)
/*      */       {
/*  974 */         errMsg = "csArchiverTransferNoLongerAutomated";
/*      */       }
/*  976 */       else if (targetArchive == null)
/*      */       {
/*  978 */         errMsg = "csArchiverTransferNoTarget";
/*      */       }
/*      */     }
/*      */ 
/*  982 */     if (errMsg != null)
/*      */     {
/*  984 */       ProgressStateUtils.reportAppError(m_internalReportProgress, "archiver", "archiver", null, errMsg, new Object[] { data.m_archiveName, data.m_collectionName });
/*      */ 
/*  986 */       updateReplicationCounters(location, true, replicationType);
/*  987 */       return false;
/*      */     }
/*      */ 
/*  991 */     if ((data.m_isChanged) || (data.checkExportsTS()))
/*      */     {
/*  993 */       markChanged = true;
/*      */     }
/*  995 */     return markChanged;
/*      */   }
/*      */ 
/*      */   public static boolean checkForWork()
/*      */     throws ServiceException
/*      */   {
/* 1001 */     synchronized (m_observable)
/*      */     {
/* 1003 */       if ((m_startArchiving) || (m_archiverActive))
/*      */       {
/* 1005 */         return false;
/*      */       }
/*      */     }
/* 1008 */     return checkAutomated();
/*      */   }
/*      */ 
/*      */   public static void doWork()
/*      */   {
/* 1013 */     synchronized (m_observable)
/*      */     {
/* 1015 */       if ((m_startArchiving) || (m_archiverActive))
/*      */       {
/* 1017 */         return;
/*      */       }
/*      */ 
/* 1021 */       Vector changed = new IdcVector();
/*      */       try
/*      */       {
/*      */         boolean isTransfers;
/*      */         boolean isQueued;
/*      */         Enumeration en;
/* 1024 */         for (int i = 0; i < ReplicationData.m_typeNames.length; ++i)
/*      */         {
/* 1026 */           if (!ReplicationData.isArchiverMonitorAutomated(i)) {
/*      */             continue;
/*      */           }
/*      */ 
/* 1030 */           isTransfers = ReplicationData.isTransfer(i);
/* 1031 */           isQueued = ReplicationData.isQueued(i);
/* 1032 */           Hashtable autoMap = ReplicationData.getAutomatedLookup(i);
/*      */ 
/* 1034 */           for (en = autoMap.elements(); en.hasMoreElements(); )
/*      */           {
/* 1036 */             AutomatedArchiveData data = (AutomatedArchiveData)en.nextElement();
/* 1037 */             if (!data.m_isChanged)
/*      */             {
/*      */               continue;
/*      */             }
/*      */ 
/* 1042 */             DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/* 1043 */             binder.putLocal("IDC_Name", data.m_collectionName);
/* 1044 */             binder.putLocal("aArchiveName", data.m_archiveName);
/* 1045 */             binder.putLocal("aArchiveTargetTS", String.valueOf(data.m_archiveTargetTS));
/* 1046 */             binder.putLocal("aExportTargetTS", String.valueOf(data.m_exportTargetTS));
/* 1047 */             binder.putLocal("aLocation", data.m_location);
/* 1048 */             binder.putLocal("AutomationType", ReplicationData.m_typeNames[i]);
/*      */ 
/* 1050 */             if (isTransfers)
/*      */             {
/* 1052 */               binder.putLocal("aTargetArchive", data.getProperty("aTargetArchive"));
/* 1053 */               binder.putLocal("IsAutoTransfer", "1");
/*      */             }
/*      */             else
/*      */             {
/* 1057 */               binder.putLocal("IsAutoImport", "1");
/*      */             }
/* 1059 */             if (isQueued)
/*      */             {
/* 1061 */               binder.putLocal("IsQueued", "1");
/*      */ 
/* 1063 */               if (!SharedObjects.getEnvValueAsBoolean("DisableArchiveUserAdminRole", false))
/*      */               {
/* 1065 */                 binder.setEnvironmentValue("EXTERNAL_ROLE", "admin");
/*      */               }
/*      */             }
/* 1068 */             changed.addElement(binder);
/*      */           }
/*      */         }
/*      */ 
/* 1072 */         if (changed.size() > 0)
/*      */         {
/* 1075 */           startArchiving(changed);
/* 1076 */           watchArchiving();
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1082 */         ProgressStateUtils.reportAppError(m_internalReportProgress, "archiver", "archiver", e, "csUnableToDoReplication", new Object[0]);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String getArchiveFileKeyFromData(DataBinder archiveData)
/*      */     throws DataException
/*      */   {
/* 1090 */     String archiveName = ArchiveUtils.getArchiveName(archiveData);
/* 1091 */     String collName = ArchiveUtils.getCollectionName(archiveData);
/*      */ 
/* 1093 */     boolean isExport = StringUtils.convertToBool(archiveData.getLocal("IsExport"), false);
/* 1094 */     if (isExport)
/*      */     {
/* 1096 */       return null;
/*      */     }
/* 1098 */     boolean isTransfer = StringUtils.convertToBool(archiveData.getLocal("IsTransfer"), false);
/* 1099 */     if (!isTransfer)
/*      */     {
/* 1101 */       isTransfer = StringUtils.convertToBool(archiveData.getLocal("IsAutoTransfer"), false);
/*      */     }
/* 1103 */     return getArchiveFileKey(archiveName, collName, isTransfer);
/*      */   }
/*      */ 
/*      */   public static String getArchiveFileKey(String archiveName, String collectionName, boolean isTransfer)
/*      */   {
/* 1108 */     String encodedCollName = StringUtils.encodeUrlStyle(collectionName, '@', false);
/* 1109 */     String encodedArchiveName = StringUtils.encodeUrlStyle(archiveName, '@', false);
/* 1110 */     String encoded = encodedArchiveName;
/* 1111 */     encoded = new StringBuilder().append(encoded).append("@").toString();
/* 1112 */     encoded = new StringBuilder().append(encoded).append(encodedCollName).toString();
/* 1113 */     encoded = encoded.toLowerCase();
/* 1114 */     encoded = new StringBuilder().append(encoded).append((isTransfer) ? "#transfer" : "#import").toString();
/*      */ 
/* 1116 */     return new StringBuilder().append(encoded).append(".lck").toString();
/*      */   }
/*      */ 
/*      */   protected static ArchiverIndexLockStatus createNewLock(String filePath)
/*      */   {
/* 1121 */     ArchiverIndexLockStatus newStatus = new ArchiverIndexLockStatus();
/* 1122 */     newStatus.m_lastTime = System.currentTimeMillis();
/* 1123 */     newStatus.m_hasChanged = true;
/* 1124 */     newStatus.m_processOwnsLock = false;
/* 1125 */     File file = FileUtilsCfgBuilder.getCfgFile(filePath, "Archive", false);
/* 1126 */     newStatus.m_lastModified = file.lastModified();
/* 1127 */     newStatus.m_isLocked = false;
/* 1128 */     newStatus.m_file = file;
/* 1129 */     return newStatus;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1134 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97046 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiverMonitor
 * JD-Core Version:    0.5.4
 */