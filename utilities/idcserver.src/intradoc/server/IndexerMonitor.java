/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.indexer.Indexer;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.io.FilenameFilter;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Map;
/*      */ import java.util.Observable;
/*      */ import java.util.Observer;
/*      */ import java.util.Vector;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ 
/*      */ public class IndexerMonitor
/*      */ {
/*      */   public static final int IDLE = 0;
/*      */   public static final int ACTIVE = 16;
/*      */   public static final int INTERRUPTED = 32;
/*      */   public static final int SUSPENDING = 48;
/*      */   public static final int CANCELLING = 64;
/*      */   public static final int UNKNOWN = 80;
/*   52 */   public static String[] m_statusNouns = { "idle", "active", "interrupted", "suspending", "cancelling", "indeterminate" };
/*      */   public static final int START = 0;
/*      */   public static final int RESTART = 1;
/*      */   public static final int CANCEL = 2;
/*      */   public static final int SUSPEND = 3;
/*      */   public static final int FORCE_START = 4;
/*   62 */   public static String[] m_actionIds = { "start", "restart", "cancel", "suspend", "forceStart" };
/*      */   public static Hashtable m_actionStrings;
/*   67 */   public static int m_touchMonitorInterval = 30000;
/*   68 */   public static boolean m_allowAutomaticConcurrentUpdate = true;
/*      */ 
/*   71 */   protected static boolean m_backgroundDoesExclusiveLocksOnly = false;
/*      */ 
/*   74 */   protected static String m_indexDir = "./";
/*   75 */   protected static String m_indexLockDir = "./lock/";
/*      */ 
/*   77 */   protected static String m_defaultCycle = "update";
/*   78 */   protected static boolean m_isWorkPending = false;
/*      */ 
/*   84 */   protected static String m_curStatus = null;
/*      */ 
/*   87 */   protected static Workspace m_workspace = null;
/*      */ 
/*   93 */   protected static ReportProgress m_internalReportProgress = null;
/*   94 */   protected static ReportProgress m_externalReportProgress = null;
/*      */ 
/*   97 */   protected static Observable m_observable = null;
/*   98 */   protected static Vector m_singleCycleObservers = new IdcVector();
/*   99 */   protected static boolean[] m_exclusiveNotify = null;
/*  100 */   protected static String m_exclusiveCycleId = null;
/*      */   protected static String m_restartID;
/*  106 */   protected static boolean m_disableAutoIndexing = false;
/*      */ 
/*  109 */   protected static boolean m_disableAutoUpdate = false;
/*      */ 
/*  112 */   protected static Hashtable m_activeCycles = new Hashtable();
/*      */ 
/*  114 */   protected static boolean m_isInitialized = false;
/*      */ 
/*      */   public static int getActionCode(String actionName)
/*      */   {
/*  127 */     Integer val = (Integer)m_actionStrings.get(actionName);
/*  128 */     if (val == null)
/*      */     {
/*  130 */       return -1;
/*      */     }
/*  132 */     return val.intValue();
/*      */   }
/*      */ 
/*      */   public static void init(String indexDir, Workspace ws, Observer observer)
/*      */     throws ServiceException
/*      */   {
/*  138 */     if (m_isInitialized)
/*      */       return;
/*  140 */     indexDir = FileUtils.directorySlashes(indexDir);
/*  141 */     m_indexDir = indexDir;
/*  142 */     m_indexLockDir = indexDir + "lock/";
/*  143 */     m_workspace = ws;
/*  144 */     m_observable = new Observable()
/*      */     {
/*      */       public boolean hasChanged()
/*      */       {
/*  149 */         return true;
/*      */       }
/*      */     };
/*  153 */     if (observer != null)
/*      */     {
/*  155 */       m_observable.addObserver(observer);
/*      */     }
/*      */ 
/*  158 */     FileUtils.checkOrCreateDirectory(indexDir + "/lock", 0);
/*      */ 
/*  160 */     m_restartID = SharedObjects.getEnvironmentValue("AppID");
/*  161 */     if (m_restartID == null)
/*      */     {
/*  163 */       m_restartID = "no";
/*      */     }
/*      */ 
/*  166 */     m_allowAutomaticConcurrentUpdate = SharedObjects.getEnvValueAsBoolean("AllowConcurrentUpdate", false);
/*  167 */     m_backgroundDoesExclusiveLocksOnly = SharedObjects.getEnvValueAsBoolean("IndexerBackgroundThreadDoesExclusiveLocksOnly", false);
/*      */ 
/*  169 */     m_internalReportProgress = new ReportProgress()
/*      */     {
/*      */       public void reportProgress(int type, String msg, float amtDone, float max)
/*      */       {
/*  173 */         IndexerMonitor.m_curStatus = StringUtils.createReportProgressString(type, msg, amtDone, max);
/*  174 */         if (IndexerMonitor.m_externalReportProgress == null)
/*      */           return;
/*  176 */         IndexerMonitor.m_externalReportProgress.reportProgress(type, msg, amtDone, max);
/*      */       }
/*      */     };
/*  182 */     File cyclesFile = FileUtilsCfgBuilder.getCfgFile(indexDir + "/indexercycles.hda", "Search", false);
/*  183 */     if (!cyclesFile.exists())
/*      */     {
/*  185 */       DataResultSet defaults = SharedObjects.getTable("DefaultIndexerCycles");
/*  186 */       String searchDir = DirectoryLocator.getSearchDirectory();
/*  187 */       DataBinder cycles = new DataBinder();
/*      */ 
/*  190 */       FileUtils.reserveDirectory(searchDir);
/*      */       try
/*      */       {
/*  194 */         FieldInfo info = new FieldInfo();
/*  195 */         defaults.getFieldInfo("sConfigOverrides", info);
/*  196 */         for (defaults.first(); defaults.isRowPresent(); defaults.next())
/*      */         {
/*  198 */           String value = defaults.getStringValue(info.m_index);
/*      */ 
/*  200 */           defaults.setCurrentValue(info.m_index, value.replace(';', '\n'));
/*      */         }
/*  202 */         cycles.addResultSet("IndexerCycles", defaults);
/*  203 */         ResourceUtils.serializeDataBinder(searchDir, "indexercycles.hda", cycles, true, false);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  207 */         ExecutionContext cxt = new ExecutionContextAdaptor();
/*  208 */         String msg = LocaleResources.getString("csUnknownIndexerInitError", cxt);
/*  209 */         Report.trace(null, msg, e);
/*      */       }
/*      */       finally
/*      */       {
/*  213 */         FileUtils.releaseDirectory(searchDir);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  218 */       DataBinder binder = new DataBinder();
/*  219 */       if (ResourceUtils.serializeDataBinder(indexDir, "indexercycles.hda", binder, false, false))
/*      */       {
/*  222 */         String autoDisable = binder.getLocal("sDisableAutoUpdate");
/*  223 */         m_disableAutoUpdate = StringUtils.convertToBool(autoDisable, false);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  228 */     Runnable watcher = new StatusMonitor();
/*  229 */     Thread watcherThread = new Thread(watcher, "IndexerMonitor");
/*  230 */     watcherThread.setDaemon(true);
/*  231 */     watcherThread.start();
/*      */ 
/*  234 */     if (SharedObjects.getEnvValueAsBoolean("IndexerTraceStartStop", false))
/*      */     {
/*  236 */       SystemUtils.addAsDefaultTrace("indexermonitor");
/*  237 */       if ((SharedObjects.getEnvValueAsBoolean("IndexerTraceCycleListChanges", false)) || (SharedObjects.getEnvValueAsBoolean("IndexerTraceLockFileChanges", false)))
/*      */       {
/*  240 */         SystemUtils.m_verbose = true;
/*      */       }
/*  242 */       SystemUtils.reportDeprecatedUsage("IndexerTraceStartStop, IndexerTraceCycleListChanges, and IndexerTraceLockFileChanges are deprecated.  Activate the indexermonitor tracing section.");
/*      */     }
/*      */ 
/*  248 */     m_touchMonitorInterval = SharedObjects.getTypedEnvironmentInt("IndexerTouchMonitorInterval", FileUtils.m_touchMonitorInterval, 18, 18);
/*      */ 
/*  252 */     m_isInitialized = true;
/*      */   }
/*      */ 
/*      */   public static String[] allLockFiles()
/*      */   {
/*  258 */     File lockFileDir = FileUtilsCfgBuilder.getCfgFile(m_indexLockDir, "Search", true);
/*  259 */     FilenameFilter filter = new FilenameFilter()
/*      */     {
/*      */       public boolean accept(File dir, String name)
/*      */       {
/*  263 */         return name.endsWith("_lock.dat");
/*      */       }
/*      */     };
/*  266 */     String[] list = lockFileDir.list(filter);
/*  267 */     if (list == null)
/*      */     {
/*  269 */       Report.trace("indexermonitor", "unable to enumerate lock files", null);
/*      */     }
/*  271 */     return list;
/*      */   }
/*      */ 
/*      */   public static String getCurrentStatus()
/*      */   {
/*  276 */     return m_curStatus;
/*      */   }
/*      */ 
/*      */   public static void setReportProgressCallback(ReportProgress callback)
/*      */   {
/*  281 */     m_externalReportProgress = callback;
/*      */   }
/*      */ 
/*      */   public static int getCycleState(String cycleId, Date timeVal)
/*      */   {
/*  289 */     return getCycleStateEx(cycleId, timeVal, null);
/*      */   }
/*      */ 
/*      */   public static int getCycleStateEx(String cycleId, Date timeVal, boolean[] ownedByProcess)
/*      */   {
/*  294 */     String lockPath = getLockPath(cycleId, "lock");
/*  295 */     File lockFile = FileUtilsCfgBuilder.getCfgFile(lockPath, "Lock", false);
/*  296 */     File suspendedFile = FileUtilsCfgBuilder.getCfgFile(getLockPath(cycleId, "suspended"), "Lock", false);
/*  297 */     boolean isChange = false;
/*      */ 
/*  300 */     if (timeVal != null)
/*      */     {
/*  302 */       timeVal.setTime(0L);
/*      */     }
/*      */     else
/*      */     {
/*  308 */       timeVal = new Date();
/*      */     }
/*      */ 
/*  311 */     IndexLockFileStatus status = (IndexLockFileStatus)m_activeCycles.get(cycleId);
/*  312 */     boolean suspendedFileExists = suspendedFile.exists();
/*  313 */     if ((ownedByProcess != null) && (ownedByProcess.length > 0))
/*      */     {
/*  315 */       ownedByProcess[0] = (((status != null) && (status.m_processOwnsLock)) ? 1 : false);
/*      */     }
/*      */     int rc;
/*      */     int rc;
/*  318 */     if ((lockFile.exists()) || (suspendedFileExists))
/*      */     {
/*  320 */       long time = 0L;
/*  321 */       if (status != null)
/*      */       {
/*  323 */         time = lockFile.lastModified();
/*  324 */         if (time != status.m_lastTime)
/*      */         {
/*  326 */           isChange = status.m_status != 16;
/*  327 */           status.m_status = 16;
/*  328 */           status.m_hasChanged = true;
/*  329 */           if ((isChange) && (SystemUtils.m_verbose))
/*      */           {
/*  331 */             Report.debug("indexermonitor", "-Making cycle " + cycleId + " active.", null);
/*      */           }
/*      */ 
/*  334 */           if ((suspendedFileExists) && (isChange))
/*      */           {
/*  336 */             suspendedFile.delete();
/*  337 */             suspendedFileExists = false;
/*      */           }
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  343 */         isChange = true;
/*  344 */         status = new IndexLockFileStatus();
/*  345 */         status.m_file = lockFile;
/*  346 */         status.m_status = 80;
/*  347 */         status.m_hasChanged = true;
/*  348 */         time = status.m_lastTime = lockFile.lastModified();
/*  349 */         if (SystemUtils.m_verbose)
/*      */         {
/*  351 */           Report.debug("indexermonitor", "-Adding undetermined state for cycle " + cycleId, null);
/*      */         }
/*  353 */         m_activeCycles.put(cycleId, status);
/*      */       }
/*      */ 
/*  356 */       if ((suspendedFileExists) && (status.m_status != 32))
/*      */       {
/*  359 */         isChange = true;
/*  360 */         status.m_status = 32;
/*      */       }
/*      */ 
/*  363 */       timeVal.setTime(time);
/*      */ 
/*  365 */       if (status.m_status == 16)
/*      */       {
/*  368 */         File suspendFile = FileUtilsCfgBuilder.getCfgFile(getLockPath(cycleId, "suspend"), "Lock", false);
/*  369 */         File cancelFile = FileUtilsCfgBuilder.getCfgFile(getLockPath(cycleId, "cancel"), "Lock", false);
/*      */ 
/*  371 */         if ((cancelFile.exists()) && (status.m_status != 48))
/*      */         {
/*  373 */           isChange = true;
/*  374 */           status.m_status = 64;
/*      */         }
/*  376 */         else if ((suspendFile.exists()) && (status.m_status != 48))
/*      */         {
/*  378 */           isChange = true;
/*  379 */           status.m_status = 48;
/*      */         }
/*      */       }
/*      */ 
/*  383 */       rc = status.m_status;
/*      */     }
/*      */     else
/*      */     {
/*  387 */       rc = 0;
/*  388 */       if ((status != null) && (rc != status.m_status))
/*      */       {
/*  390 */         if (SystemUtils.m_verbose)
/*      */         {
/*  392 */           Report.debug("indexermonitor", "-Determined cycle " + cycleId + " is inactive.", null);
/*      */         }
/*  394 */         isChange = true;
/*  395 */         status.m_status = rc;
/*      */       }
/*      */     }
/*      */ 
/*  399 */     if (isChange)
/*      */     {
/*  401 */       notifyStatusChange();
/*      */     }
/*      */ 
/*  404 */     return rc;
/*      */   }
/*      */ 
/*      */   public static void updateIndexerConfig()
/*      */   {
/*  411 */     DataBinder binder = new DataBinder();
/*      */     try
/*      */     {
/*  415 */       FileUtils.reserveDirectory(m_indexDir);
/*  416 */       ResourceUtils.serializeDataBinder(m_indexDir, "indexercycles.hda", binder, false, true);
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/*  422 */       if (SystemUtils.m_verbose)
/*      */       {
/*  424 */         Report.debug("indexermonitor", null, ignore);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  429 */       FileUtils.releaseDirectory(m_indexDir);
/*      */     }
/*  431 */     String disableAuto = binder.getLocal("sDisableAutoUpdate");
/*  432 */     m_disableAutoUpdate = StringUtils.convertToBool(disableAuto, false);
/*      */   }
/*      */ 
/*      */   public static void getIndexerStatus(DataBinder binder)
/*      */     throws DataException, ServiceException
/*      */   {
/*  442 */     Vector fields = new IdcVector();
/*  443 */     FieldInfo cycle = new FieldInfo();
/*  444 */     FieldInfo status = new FieldInfo();
/*  445 */     FieldInfo activeDate = new FieldInfo();
/*  446 */     FieldInfo statusMsg = new FieldInfo();
/*  447 */     FieldInfo stateMsg = new FieldInfo();
/*  448 */     status.m_name = "status";
/*  449 */     activeDate.m_name = "activeDate";
/*  450 */     statusMsg.m_name = "statusMsg";
/*  451 */     stateMsg.m_name = "stateMsg";
/*  452 */     fields.addElement(status);
/*  453 */     fields.addElement(activeDate);
/*  454 */     fields.addElement(statusMsg);
/*  455 */     fields.addElement(stateMsg);
/*      */ 
/*  457 */     String[] otherFields = { "IndexerState", "startDate", "finishDate", "progressMessage", "errCount", "totalFullTextAdd", "totalAddIndex", "totalDeleteIndex", "totalDummyTextAdd" };
/*      */ 
/*  460 */     String[] finalNames = { "state", "startDate", "finishDate", "progressMessage", "errCount", "totalFullTextAdd", "totalAddIndex", "totalDeleteIndex", "totalDummyAddIndex" };
/*      */ 
/*  464 */     binder.setFieldType("sCycleLabel", "message");
/*  465 */     binder.setFieldType("sDescription", "message");
/*  466 */     binder.setFieldType("statusMsg", "message");
/*  467 */     binder.setFieldType("stateMsg", "message");
/*  468 */     binder.setFieldType("progressMessage", "message2");
/*  469 */     binder.setFieldType("errCount", "message2");
/*  470 */     binder.setFieldType("totalFullTextAdd", "message2");
/*  471 */     binder.setFieldType("totalAddIndex", "message2");
/*  472 */     binder.setFieldType("totalDeleteIndex", "message2");
/*  473 */     binder.setFieldType("totalDummyAddIndex", "message2");
/*  474 */     binder.setFieldType("startDate", "message");
/*  475 */     binder.setFieldType("finishDate", "message");
/*  476 */     binder.setFieldType("activeDate", "message");
/*      */ 
/*  478 */     FieldInfo[] fieldInfos = new FieldInfo[otherFields.length];
/*  479 */     for (int i = 0; i < otherFields.length; ++i)
/*      */     {
/*  481 */       FieldInfo info = new FieldInfo();
/*  482 */       info.m_name = finalNames[i];
/*  483 */       fields.addElement(info);
/*  484 */       fieldInfos[i] = info;
/*      */     }
/*      */ 
/*  487 */     DataBinder tmpBinder = null;
/*  488 */     FileUtils.reserveDirectory(m_indexDir);
/*      */     try
/*      */     {
/*      */       try
/*      */       {
/*  493 */         tmpBinder = new DataBinder();
/*  494 */         ResourceUtils.serializeDataBinder(m_indexDir, "indexercycles.hda", tmpBinder, false, true);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  501 */         SystemUtils.sleepRandom(200L, 500L);
/*  502 */         tmpBinder = new DataBinder();
/*  503 */         ResourceUtils.serializeDataBinder(m_indexDir, "indexercycles.hda", tmpBinder, false, true);
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/*  509 */       FileUtils.releaseDirectory(m_indexDir);
/*      */     }
/*      */ 
/*  512 */     String disableAuto = tmpBinder.getLocal("sDisableAutoUpdate");
/*  513 */     DataResultSet rset = (DataResultSet)tmpBinder.getResultSet("IndexerCycles");
/*  514 */     binder.addResultSet("INDEXER_STATUS", rset);
/*  515 */     m_disableAutoUpdate = StringUtils.convertToBool(disableAuto, false);
/*  516 */     binder.putLocal("sDisableAutoUpdate", (m_disableAutoUpdate) ? "1" : "0");
/*      */ 
/*  518 */     rset.mergeFieldsWithFlags(fields, 2);
/*  519 */     rset.getFieldInfo("sCycleID", cycle);
/*      */ 
/*  521 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  523 */       String cycleId = rset.getStringValue(cycle.m_index);
/*  524 */       Date time = new Date();
/*  525 */       int statusCode = getCycleState(cycleId, time);
/*  526 */       String statusStr = m_statusNouns[(statusCode / 16)];
/*  527 */       rset.setCurrentValue(status.m_index, statusStr);
/*  528 */       statusStr = "!csIndexerStatusMsg_" + statusStr;
/*  529 */       rset.setCurrentValue(statusMsg.m_index, statusStr);
/*      */ 
/*  532 */       String timeStr = "";
/*  533 */       if (time.getTime() != 0L)
/*      */       {
/*  535 */         timeStr = LocaleUtils.encodeMessage("csDateMessage", null, LocaleResources.m_odbcFormat.format(time));
/*      */       }
/*      */ 
/*  538 */       rset.setCurrentValue(activeDate.m_index, timeStr);
/*      */ 
/*  540 */       DataBinder stateData = null;
/*  541 */       File stateFile = FileUtilsCfgBuilder.getCfgFile(m_indexDir + "/" + cycleId + "/state.hda", "Search", false);
/*  542 */       if (stateFile.exists())
/*      */       {
/*      */         try
/*      */         {
/*  546 */           stateData = ResourceUtils.readDataBinderHeader(m_indexDir + "/" + cycleId, "state.hda");
/*      */         }
/*      */         catch (ServiceException ignore)
/*      */         {
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  554 */       for (int i = 0; i < otherFields.length; ++i)
/*      */       {
/*  556 */         String value = null;
/*  557 */         if (stateData != null)
/*      */         {
/*  559 */           value = stateData.getLocal(otherFields[i]);
/*      */         }
/*      */ 
/*  562 */         if (otherFields[i].equals("IndexerState"))
/*      */         {
/*  564 */           if (value == null)
/*      */           {
/*  566 */             rset.setCurrentValue(stateMsg.m_index, "!csUnknown");
/*      */           }
/*      */           else
/*      */           {
/*  570 */             String tmp = "!csIndexerStateMsg_" + value;
/*  571 */             rset.setCurrentValue(stateMsg.m_index, tmp);
/*      */           }
/*      */         }
/*  574 */         else if (value == null)
/*      */         {
/*  576 */           value = "!csUnknown";
/*      */         }
/*      */ 
/*  579 */         if (value == null)
/*      */           continue;
/*  581 */         rset.setCurrentValue(fieldInfos[i].m_index, value);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void cancelAllIndexing()
/*      */     throws ServiceException
/*      */   {
/*  589 */     FileUtils.reserveDirectory(m_indexLockDir);
/*      */     try
/*      */     {
/*  592 */       String[] list = allLockFiles();
/*  593 */       for (int i = 0; i < list.length; ++i)
/*      */       {
/*  595 */         String cycleId = getCycleId(list[i]);
/*  596 */         if (cycleId == null) {
/*      */           continue;
/*      */         }
/*      */ 
/*  600 */         int stat = getCycleState(cycleId, null);
/*  601 */         if (stat == 0)
/*      */           continue;
/*  603 */         adjustIndexingInternal(cycleId, 2, new HashMap());
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/*  609 */       FileUtils.releaseDirectory(m_indexLockDir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void adjustIndexing(String cycleId, int action, Map extraParams, boolean throwException)
/*      */     throws ServiceException
/*      */   {
/*  616 */     String errMsg = null;
/*  617 */     FileUtils.reserveDirectory(m_indexLockDir);
/*      */     try
/*      */     {
/*  620 */       errMsg = adjustIndexingInternal(cycleId, action, extraParams);
/*      */     }
/*      */     finally
/*      */     {
/*  624 */       FileUtils.releaseDirectory(m_indexLockDir);
/*      */     }
/*      */ 
/*  627 */     if ((!throwException) || (errMsg == null))
/*      */       return;
/*  629 */     String msg = LocaleUtils.encodeMessage("csIndexingAdjustmentError_" + m_actionIds[action], errMsg, "csIndexerLabel_" + cycleId);
/*      */ 
/*  631 */     throw new ServiceException(msg);
/*      */   }
/*      */ 
/*      */   protected static String adjustIndexingInternal(String cycleId, int action, Map extraParams)
/*      */     throws ServiceException
/*      */   {
/*  638 */     checkForExclusivity(cycleId, action);
/*      */ 
/*  640 */     int state = getCycleState(cycleId, null);
/*  641 */     String errMsg = null;
/*      */ 
/*  643 */     switch (state)
/*      */     {
/*      */     case 48:
/*  646 */       errMsg = "!csIndexerSuspending";
/*  647 */       break;
/*      */     case 64:
/*  649 */       errMsg = "!csIndexerCancelling";
/*  650 */       break;
/*      */     case 80:
/*  652 */       errMsg = "!csIndexerUndetermined";
/*      */     }
/*      */ 
/*  656 */     if (errMsg != null)
/*      */     {
/*  658 */       String msg = LocaleUtils.encodeMessage("csIndexerUnableToControl", errMsg, "csIndexerLabel_" + cycleId);
/*      */ 
/*  660 */       return msg;
/*      */     }
/*      */ 
/*  663 */     switch (action | state)
/*      */     {
/*      */     case 0:
/*  666 */       startIndexing(cycleId, state, action, extraParams);
/*  667 */       break;
/*      */     case 16:
/*  669 */       errMsg = "!csIndexerAlreadyActive";
/*  670 */       break;
/*      */     case 32:
/*  672 */       errMsg = "!csIndexerMustRestart";
/*  673 */       break;
/*      */     case 1:
/*  675 */       errMsg = "!csIndexerIsIdle";
/*  676 */       break;
/*      */     case 17:
/*  678 */       errMsg = "!csIndexerAlreadyActive";
/*  679 */       break;
/*      */     case 33:
/*  681 */       startIndexing(cycleId, state, action, extraParams);
/*  682 */       break;
/*      */     case 2:
/*  684 */       errMsg = "!csIndexerIsNotActive";
/*  685 */       break;
/*      */     case 18:
/*  687 */       suspendOrCancelIndexing(cycleId, state, action);
/*  688 */       break;
/*      */     case 34:
/*  690 */       suspendOrCancelIndexing(cycleId, state, action);
/*  691 */       break;
/*      */     case 3:
/*  693 */       errMsg = "!csIndexerIsIdle";
/*  694 */       break;
/*      */     case 19:
/*  696 */       suspendOrCancelIndexing(cycleId, state, action);
/*  697 */       break;
/*      */     case 35:
/*  699 */       errMsg = "!csIndexerIsNotActive";
/*  700 */       break;
/*      */     case 4:
/*  702 */       startIndexing(cycleId, state, action, extraParams);
/*  703 */       break;
/*      */     case 20:
/*  705 */       errMsg = "!csIndexerAlreadyActive";
/*  706 */       break;
/*      */     case 36:
/*  708 */       startIndexing(cycleId, state, action, extraParams);
/*  709 */       break;
/*      */     case 5:
/*      */     case 6:
/*      */     case 7:
/*      */     case 8:
/*      */     case 9:
/*      */     case 10:
/*      */     case 11:
/*      */     case 12:
/*      */     case 13:
/*      */     case 14:
/*      */     case 15:
/*      */     case 21:
/*      */     case 22:
/*      */     case 23:
/*      */     case 24:
/*      */     case 25:
/*      */     case 26:
/*      */     case 27:
/*      */     case 28:
/*      */     case 29:
/*      */     case 30:
/*      */     case 31:
/*      */     default:
/*  712 */       throw new ServiceException("!csIndexerUnknownChange");
/*      */     }
/*  714 */     return errMsg;
/*      */   }
/*      */ 
/*      */   public static boolean startAutomatedWork() throws ServiceException
/*      */   {
/*  719 */     if (m_disableAutoUpdate)
/*      */     {
/*  721 */       return false;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  726 */       Report.trace("indexermonitor", "Attempting to start automated work.", null);
/*  727 */       FileUtils.reserveDirectory(m_indexLockDir);
/*      */       try
/*      */       {
/*  730 */         checkForExclusivity(m_defaultCycle, 0);
/*      */       }
/*      */       catch (ServiceException ignore)
/*      */       {
/*  734 */         Report.trace("indexermonitor", "Aborting automated work because of exclusive lock.", ignore);
/*  735 */         m_isWorkPending = true;
/*  736 */         int i = 0;
/*      */ 
/*  775 */         FileUtils.releaseDirectory(m_indexLockDir); return i;
/*      */       }
/*  739 */       boolean[] processOwnsCycle = { false };
/*  740 */       int state = getCycleStateEx(m_defaultCycle, null, processOwnsCycle);
/*  741 */       if ((state == 16) || (state == 80))
/*      */       {
/*  743 */         m_isWorkPending = (processOwnsCycle[0] != 0) || (state != 16);
/*      */ 
/*  745 */         String pendingStr = (m_isWorkPending) ? "pending" : "not pending";
/*  746 */         Report.trace("indexermonitor", "Aborting automated cycle because " + m_defaultCycle + " is active (" + pendingStr + ").", null);
/*      */ 
/*  748 */         int j = 0;
/*      */         return j;
/*      */       }
/*  751 */       if (!m_allowAutomaticConcurrentUpdate)
/*      */       {
/*  753 */         String[] files = allLockFiles();
/*      */         int k;
/*  754 */         for (int k = 0; k < files.length; ++k)
/*      */         {
/*  756 */           String cycleId = getCycleId(files[k]);
/*  757 */           if (cycleId == null) {
/*      */             continue;
/*      */           }
/*      */ 
/*  761 */           int stat = getCycleState(cycleId, null);
/*  762 */           if (stat != 16)
/*      */             continue;
/*  764 */           m_isWorkPending = true;
/*  765 */           int l = 0;
/*      */           return l;
/*      */         }
/*      */       }
/*  770 */       startIndexing(m_defaultCycle, state, 0, new HashMap());
/*  771 */       m_isWorkPending = false;
/*      */     }
/*      */     finally
/*      */     {
/*  775 */       FileUtils.releaseDirectory(m_indexLockDir);
/*      */     }
/*      */ 
/*  778 */     return true;
/*      */   }
/*      */ 
/*      */   public static boolean isWorkPending()
/*      */   {
/*  785 */     return m_isWorkPending;
/*      */   }
/*      */ 
/*      */   public static void handleExternalIndexEvent(long curTime)
/*      */   {
/*      */   }
/*      */ 
/*      */   protected static String getLockPath(String cycleId, String lockType)
/*      */   {
/*  803 */     cycleId.length();
/*  804 */     return m_indexLockDir + cycleId + "_" + lockType + ".dat";
/*      */   }
/*      */ 
/*      */   protected static String getCycleId(String lockFile)
/*      */   {
/*  817 */     int index = lockFile.indexOf("_");
/*  818 */     if (index == -1)
/*      */     {
/*  820 */       return null;
/*      */     }
/*  822 */     return lockFile.substring(0, index);
/*      */   }
/*      */ 
/*      */   protected static void startIndexing(String cycleId, int state, int action, Map extraParams)
/*      */   {
/*  828 */     String id = cycleId;
/*  829 */     int startAction = action;
/*  830 */     int timeout = SharedObjects.getTypedEnvironmentInt("IdcSystemQueryTimeout", 1800, 24, 24);
/*      */ 
/*  833 */     Map map = new ConcurrentHashMap();
/*  834 */     map.putAll(extraParams);
/*      */ 
/*  836 */     Thread indexThread = new Thread("index " + cycleId + " work", timeout, id, startAction, map)
/*      */     {
/*      */       public void run()
/*      */       {
/*      */         try
/*      */         {
/*  844 */           IndexerMonitor.m_workspace.setThreadTimeout(this.val$timeout);
/*  845 */           Report.trace("indexermonitor", "Starting indexing " + this.val$id + " (action=" + this.val$startAction + ").", null);
/*  846 */           IndexerMonitor.doIndexing(this.val$id, this.val$startAction, this.val$map);
/*  847 */           Report.trace("indexermonitor", "Ending indexing " + this.val$id + ".", null);
/*      */         }
/*      */         catch (Throwable t)
/*      */         {
/*  851 */           Report.trace(null, "Aborted indexing " + this.val$id + ".", t);
/*  852 */           if (IndexerMonitor.m_externalReportProgress != null)
/*      */           {
/*  854 */             IdcMessage idcMsg = IdcMessageFactory.lc(t);
/*  855 */             String msg = LocaleUtils.encodeMessage(idcMsg);
/*  856 */             IndexerMonitor.m_externalReportProgress.reportProgress(-1, msg, -1.0F, -1.0F);
/*      */           }
/*      */           else
/*      */           {
/*  861 */             String msg = LocaleUtils.encodeMessage("csIndexerTerminatedByException", null, this.val$id);
/*  862 */             Report.error(null, msg, t);
/*      */           }
/*      */         }
/*      */         finally
/*      */         {
/*  867 */           IndexerMonitor.notifyStatusChange();
/*  868 */           IndexerMonitor.m_workspace.releaseConnection();
/*  869 */           IndexerMonitor.m_workspace.clearThreadTimeout();
/*      */         }
/*      */       }
/*      */     };
/*  876 */     String lockFile = getLockPath(id, "lock");
/*  877 */     FileUtils.deleteFile(getLockPath(id, "suspend"));
/*  878 */     FileUtils.deleteFile(getLockPath(id, "suspended"));
/*      */ 
/*  881 */     Report.trace("indexermonitor", "touching lock file " + lockFile, null);
/*  882 */     FileUtils.touchFile(lockFile);
/*      */ 
/*  884 */     m_workspace.releaseConnection();
/*  885 */     indexThread.setDaemon(true);
/*  886 */     indexThread.start();
/*      */ 
/*  888 */     IndexLockFileStatus status = (IndexLockFileStatus)m_activeCycles.get(cycleId);
/*  889 */     if (status == null)
/*      */     {
/*  891 */       status = newIndexLockFileStatus(cycleId);
/*  892 */       m_activeCycles.put(cycleId, status);
/*      */     }
/*  894 */     status.m_status = 16;
/*  895 */     status.m_processOwnsLock = true;
/*      */ 
/*  897 */     notifyStatusChange();
/*      */   }
/*      */ 
/*      */   protected static void suspendOrCancelIndexing(String cycleId, int state, int action)
/*      */   {
/*  902 */     String lockFile = getLockPath(cycleId, (action == 3) ? "suspend" : "cancel");
/*  903 */     FileUtils.touchFile(lockFile);
/*  904 */     if (state == 16)
/*      */       return;
/*  906 */     startIndexing(cycleId, state, action, new HashMap());
/*      */   }
/*      */ 
/*      */   protected static void doIndexing(String cycleId, int action, Map extraParams)
/*      */   {
/*  912 */     Thread indexerThread = Thread.currentThread();
/*  913 */     RunningIndexer indexer = new RunningIndexer(cycleId);
/*  914 */     indexer.m_lockFile = getLockPath(cycleId, "lock");
/*      */ 
/*  916 */     Thread notifyThread = new Thread("index " + cycleId + " notify", indexer, indexerThread)
/*      */     {
/*      */       public void run()
/*      */       {
/*  921 */         File cancelFile = FileUtilsCfgBuilder.getCfgFile(IndexerMonitor.getLockPath(this.val$indexer.m_cycleId, "cancel"), "Lock", false);
/*  922 */         File suspendFile = FileUtilsCfgBuilder.getCfgFile(IndexerMonitor.getLockPath(this.val$indexer.m_cycleId, "suspend"), "Lock", false);
/*  923 */         long lastTime = System.currentTimeMillis();
/*  924 */         while (!this.val$indexer.m_isFinished)
/*      */         {
/*  926 */           if (!this.val$indexerThread.isAlive())
/*      */           {
/*  928 */             this.val$indexer.m_isFinished = true;
/*  929 */             this.val$indexer.m_isError = true;
/*  930 */             String infoMsg = LocaleUtils.encodeMessage("csIndexerUnexpectedTermination", null, Thread.currentThread().getName());
/*      */ 
/*  932 */             Report.trace(null, LocaleResources.localizeMessage(infoMsg, null), null);
/*  933 */             Report.info(null, infoMsg, null);
/*  934 */             return;
/*      */           }
/*      */ 
/*      */           try
/*      */           {
/*  941 */             boolean isFinished = false;
/*  942 */             synchronized (this.val$indexer)
/*      */             {
/*  944 */               isFinished = this.val$indexer.m_isFinished;
/*  945 */               if (!isFinished)
/*      */               {
/*  947 */                 if (SystemUtils.m_verbose)
/*      */                 {
/*  949 */                   Report.debug("indexermonitor", "touching lock file " + this.val$indexer.m_lockFile, null);
/*      */                 }
/*  951 */                 FileUtils.touchFile(this.val$indexer.m_lockFile);
/*  952 */                 if (suspendFile.exists())
/*      */                 {
/*  954 */                   Report.trace("indexermonitor", "found " + suspendFile, null);
/*  955 */                   this.val$indexer.m_indexer.suspendBuild();
/*      */                 }
/*  957 */                 if (cancelFile.exists())
/*      */                 {
/*  959 */                   Report.trace("indexermonitor", "found " + cancelFile, null);
/*  960 */                   this.val$indexer.m_indexer.cancelBuild();
/*      */                 }
/*      */               }
/*      */             }
/*      */ 
/*  965 */             if (!isFinished)
/*      */             {
/*  967 */               long curTime = System.currentTimeMillis();
/*  968 */               long diff = curTime - lastTime;
/*  969 */               lastTime = curTime;
/*      */ 
/*  977 */               long reduceInterval = diff + IndexerMonitor.m_touchMonitorInterval / 2;
/*  978 */               if (diff > IndexerMonitor.m_touchMonitorInterval / 3)
/*      */               {
/*  980 */                 Report.trace(null, LocaleResources.getString("csIndexerBackgroundThreadTooLong", null), null);
/*      */               }
/*  982 */               int sleepTime = 200;
/*  983 */               if ((reduceInterval < IndexerMonitor.m_touchMonitorInterval) && (reduceInterval >= 0L))
/*      */               {
/*  985 */                 sleepTime = IndexerMonitor.m_touchMonitorInterval - (int)reduceInterval;
/*      */               }
/*  987 */               SystemUtils.sleep(sleepTime);
/*  988 */               lastTime += sleepTime;
/*      */             }
/*      */ 
/*      */           }
/*      */           catch (Throwable ignore)
/*      */           {
/*  997 */             SystemUtils.sleepRandom(150L, 250L);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     };
/*      */     try
/*      */     {
/* 1005 */       indexer.m_indexer = ((Indexer)ComponentClassFactory.createClassInstance("Indexer", "intradoc.indexer.Indexer", "!csIndexerStartFailed"));
/*      */ 
/* 1008 */       notifyThread.setDaemon(true);
/* 1009 */       notifyThread.start();
/*      */ 
/* 1011 */       indexer.m_indexer.init(m_workspace, m_internalReportProgress, cycleId, m_restartID, extraParams);
/*      */ 
/* 1016 */       if (action == 3)
/*      */       {
/* 1019 */         Report.trace(null, "Suspending a cycle that has not started -- logic error.", null);
/* 1020 */         indexer.m_indexer.suspendBuild();
/*      */       }
/* 1022 */       else if (action == 2)
/*      */       {
/* 1024 */         indexer.m_indexer.cancelBuild();
/*      */       }
/* 1026 */       indexer.m_indexer.buildIndex();
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1030 */       String msg = e.getMessage();
/* 1031 */       Date curTime = new Date();
/*      */ 
/* 1033 */       if (e.m_errorCode == -66)
/*      */       {
/* 1035 */         Report.info(null, e, "csIndexerSuspendedMsg", new Object[] { "csIndexerLabel_" + cycleId });
/* 1036 */         FileUtils.touchFile(getLockPath(cycleId, "suspended"));
/* 1037 */         m_internalReportProgress.reportProgress(0, LocaleUtils.encodeMessage("csIndexerSuspendedMsg2", msg, "csIndexerLabel_" + cycleId, curTime), -1.0F, -1.0F);
/*      */       }
/*      */       else
/*      */       {
/* 1043 */         Report.error(null, e, "csIndexerAbortedMsg", new Object[0]);
/* 1044 */         m_internalReportProgress.reportProgress(-1, LocaleUtils.encodeMessage("csIndexerAbortedMsg2", msg, curTime), -1.0F, -1.0F);
/*      */       }
/*      */ 
/* 1048 */       Throwable t = e.getCause();
/* 1049 */       if (t == null)
/*      */       {
/* 1051 */         t = e;
/*      */       }
/* 1053 */       Report.info("indexer", t, "csIndexerError", new Object[0]);
/* 1054 */       indexer.m_isError = true;
/*      */     }
/*      */     finally
/*      */     {
/* 1058 */       cleanupIndexingStatus(indexer);
/* 1059 */       m_observable.notifyObservers("IndexFinished");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void cleanupIndexingStatus(RunningIndexer indexer)
/*      */   {
/* 1067 */     SystemUtils.sleep(3000L);
/*      */ 
/* 1069 */     boolean doCleanup = false;
/* 1070 */     synchronized (indexer)
/*      */     {
/* 1074 */       if (!indexer.m_isFinished)
/*      */       {
/* 1076 */         doCleanup = true;
/* 1077 */         indexer.m_isFinished = true;
/*      */       }
/*      */     }
/*      */ 
/* 1081 */     if (!doCleanup)
/*      */       return;
/* 1083 */     File cancelFile = FileUtilsCfgBuilder.getCfgFile(getLockPath(indexer.m_cycleId, "cancel"), "Lock", false);
/* 1084 */     File suspendFile = FileUtilsCfgBuilder.getCfgFile(getLockPath(indexer.m_cycleId, "suspend"), "Lock", false);
/*      */     try
/*      */     {
/* 1088 */       FileUtils.reserveDirectory(m_indexLockDir, true);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1092 */       throw new AssertionError(e);
/*      */     }
/* 1094 */     IndexLockFileStatus stat = (IndexLockFileStatus)m_activeCycles.get(indexer.m_cycleId);
/* 1095 */     if (stat != null)
/*      */     {
/* 1097 */       stat.m_processOwnsLock = false;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1102 */       if ((!indexer.m_isError) || (cancelFile.exists()))
/*      */       {
/* 1104 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1106 */           Report.debug("indexermonitor", "deleting lock file " + indexer.m_lockFile + ".", null);
/*      */         }
/* 1108 */         FileUtils.deleteFile(indexer.m_lockFile);
/*      */       }
/* 1110 */       cancelFile.delete();
/* 1111 */       suspendFile.delete();
/* 1112 */       FileUtils.deleteFile(getLockPath(indexer.m_cycleId, "exclusive"));
/* 1113 */       FileUtils.deleteFile(getLockPath(indexer.m_cycleId, "exclusive_request"));
/* 1114 */       notifyStatusChange();
/*      */     }
/*      */     finally
/*      */     {
/* 1118 */       FileUtils.releaseDirectory(m_indexLockDir, true);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void notifyStatusChange()
/*      */   {
/* 1125 */     SubjectManager.notifyChanged("indexerstatus");
/*      */   }
/*      */ 
/*      */   public static void registerSingleCycleObserver(Observer obs)
/*      */   {
/* 1130 */     m_singleCycleObservers.addElement(obs);
/*      */   }
/*      */ 
/*      */   public static void deregisterSingleCycleObserver(Observer obs)
/*      */   {
/* 1135 */     m_singleCycleObservers.removeElement(obs);
/*      */   }
/*      */ 
/*      */   public static void requestExclusiveLock(String cycleId, boolean[] notify)
/*      */     throws ServiceException
/*      */   {
/* 1141 */     FileUtils.reserveDirectory(m_indexLockDir);
/*      */     try
/*      */     {
/* 1144 */       boolean onlyUsCycle = true;
/* 1145 */       boolean alreadyNotified = false;
/*      */ 
/* 1149 */       File dir = FileUtilsCfgBuilder.getCfgFile(m_indexLockDir, "Search", true);
/* 1150 */       String[] list = dir.list();
/* 1151 */       for (int i = 0; i < list.length; ++i)
/*      */       {
/* 1153 */         String name = list[i];
/* 1154 */         String lockedCycle = getCycleId(name);
/* 1155 */         boolean isUs = (lockedCycle != null) && (lockedCycle.equals(cycleId));
/* 1156 */         if ((lockedCycle != null) && (!isUs))
/*      */         {
/* 1158 */           onlyUsCycle = false;
/*      */         }
/* 1160 */         if ((!name.endsWith("_exclusive_request.dat")) && (!name.endsWith("_exclusive.dat")))
/*      */           continue;
/* 1162 */         if (isUs)
/*      */         {
/* 1164 */           if (!name.endsWith("_exclusive.dat"))
/*      */             break;
/* 1166 */           synchronized (notify)
/*      */           {
/* 1168 */             if (notify.length > 0)
/*      */             {
/* 1170 */               notify[0] = true;
/*      */             }
/* 1172 */             Report.trace("indexermonitor", "Discovered we are already exclusive, notifying on exclusive lock for cycle " + cycleId, null);
/*      */ 
/* 1174 */             alreadyNotified = true;
/* 1175 */             notify.notify();
/* 1176 */           }break;
/*      */         }
/*      */ 
/* 1180 */         String msg = LocaleUtils.encodeMessage("csIndexerIllegalLockRequest", null, "csIndexerLabel_" + cycleId);
/*      */         int j;
/*      */         int j;
/* 1190 */         if (name.endsWith("_exclusive_request.dat"))
/*      */         {
/*      */           int i;
/* 1192 */           int i = -22;
/* 1193 */           msg = LocaleUtils.encodeMessage("csIndexerLockCollision1", msg, "csIndexerLabel_" + lockedCycle);
/*      */         }
/*      */         else
/*      */         {
/* 1198 */           j = -34;
/* 1199 */           msg = LocaleUtils.encodeMessage("csIndexerLockCollision2", msg, "csIndexerLabel_" + lockedCycle);
/*      */         }
/*      */ 
/* 1202 */         throw new ServiceException(j, msg);
/*      */       }
/*      */ 
/* 1205 */       if (!alreadyNotified)
/*      */       {
/* 1207 */         String lockType = null;
/* 1208 */         if ((onlyUsCycle) && (!m_backgroundDoesExclusiveLocksOnly))
/*      */         {
/* 1212 */           lockType = "exclusive";
/* 1213 */           synchronized (notify)
/*      */           {
/* 1215 */             if (notify.length > 0)
/*      */             {
/* 1217 */               notify[0] = true;
/*      */             }
/* 1219 */             Report.trace("indexermonitor", "Notifying on exclusive lock (only we are active cycle) for cycle " + cycleId, null);
/* 1220 */             alreadyNotified = true;
/* 1221 */             notify.notify();
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1226 */           if (SystemUtils.m_verbose)
/*      */           {
/* 1228 */             Report.debug("indexermonitor", "Marking exclusive lock request for cycle " + cycleId, null);
/*      */           }
/* 1230 */           lockType = "exclusive_request";
/* 1231 */           m_exclusiveNotify = notify;
/* 1232 */           m_exclusiveCycleId = cycleId;
/*      */         }
/* 1234 */         String name = getLockPath(cycleId, lockType);
/* 1235 */         FileUtils.touchFile(name);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 1240 */       FileUtils.releaseDirectory(m_indexLockDir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void notifyExclusiveLock() throws ServiceException
/*      */   {
/* 1246 */     synchronized (m_activeCycles)
/*      */     {
/* 1248 */       if (m_exclusiveNotify != null)
/*      */       {
/*      */         try
/*      */         {
/* 1252 */           FileUtils.reserveDirectory(m_indexLockDir, true);
/* 1253 */           FileUtils.touchFile(getLockPath(m_exclusiveCycleId, "exclusive"));
/* 1254 */           FileUtils.deleteFile(getLockPath(m_exclusiveCycleId, "exclusive_request"));
/* 1255 */           synchronized (m_exclusiveNotify)
/*      */           {
/* 1257 */             if (SystemUtils.m_verbose)
/*      */             {
/* 1259 */               boolean[] exN = m_exclusiveNotify;
/* 1260 */               boolean state = (exN != null) && (exN.length > 0) && (exN[0] != 0);
/* 1261 */               Report.debug("indexermonitor", "Notify exclusive with exclusive notify state of " + state, null);
/*      */             }
/* 1263 */             if (m_exclusiveNotify.length > 0)
/*      */             {
/* 1265 */               m_exclusiveNotify[0] = true;
/*      */             }
/* 1267 */             m_exclusiveNotify.notify();
/* 1268 */             m_exclusiveNotify = null;
/* 1269 */             m_exclusiveCycleId = null;
/*      */           }
/*      */         }
/*      */         finally
/*      */         {
/* 1274 */           FileUtils.releaseDirectory(m_indexLockDir, true);
/*      */         }
/*      */ 
/*      */       }
/* 1279 */       else if (SystemUtils.m_verbose)
/*      */       {
/* 1281 */         Report.debug("indexermonitor", "Notification Lock is null", null);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void checkForExclusivity(String cycleId, int action)
/*      */     throws ServiceException
/*      */   {
/* 1290 */     File dir = FileUtilsCfgBuilder.getCfgFile(m_indexLockDir, "Search", true);
/* 1291 */     String[] list = dir.list();
/* 1292 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/* 1294 */       if ((!list[i].endsWith("_exclusive.dat")) && (!list[i].endsWith("_exclusive_request.dat"))) {
/*      */         continue;
/*      */       }
/* 1297 */       String exclusiveCycle = getCycleId(list[i]);
/* 1298 */       if (exclusiveCycle.equals(cycleId))
/*      */       {
/* 1300 */         return;
/*      */       }
/*      */ 
/* 1303 */       boolean isExclusive = list[i].endsWith("_exclusive.dat");
/* 1304 */       if ((!isExclusive) && (action != 0))
/*      */         continue;
/* 1306 */       throw new ServiceException(-22, LocaleUtils.encodeMessage("csIndexerExclusiveLock", null, "csIndexerLabel_" + exclusiveCycle));
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void releaseExclusiveLock(String cycleId)
/*      */   {
/* 1316 */     synchronized (m_activeCycles)
/*      */     {
/* 1318 */       Report.trace("indexermonitor", "Releasing exclusive lock for cycle " + cycleId + ".", null);
/*      */ 
/* 1320 */       if ((m_exclusiveCycleId != null) && (m_exclusiveCycleId.equals(cycleId)))
/*      */       {
/* 1322 */         m_exclusiveNotify = null;
/* 1323 */         m_exclusiveCycleId = null;
/*      */       }
/* 1325 */       FileUtils.deleteFile(getLockPath(cycleId, "exclusive"));
/* 1326 */       FileUtils.deleteFile(getLockPath(cycleId, "exclusive_request"));
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void notifySingleActiveCycle()
/*      */   {
/* 1332 */     int length = m_singleCycleObservers.size();
/* 1333 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 1335 */       Observer obs = (Observer)m_singleCycleObservers.elementAt(i);
/* 1336 */       obs.update(null, "single");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static IndexLockFileStatus newIndexLockFileStatus(String cycleId)
/*      */   {
/* 1342 */     IndexLockFileStatus stat = new IndexLockFileStatus();
/* 1343 */     stat.m_file = FileUtilsCfgBuilder.getCfgFile(getLockPath(cycleId, "lock"), "Lock", false);
/* 1344 */     stat.m_lastTime = stat.m_file.lastModified();
/* 1345 */     stat.m_hasChanged = true;
/* 1346 */     stat.m_processOwnsLock = false;
/* 1347 */     stat.m_status = 80;
/* 1348 */     return stat;
/*      */   }
/*      */ 
/*      */   public static int nonIdleCycleCount()
/*      */   {
/* 1353 */     return m_activeCycles.size();
/*      */   }
/*      */ 
/*      */   public static boolean isDisableAutoIndexing()
/*      */   {
/* 1573 */     return m_disableAutoIndexing;
/*      */   }
/*      */ 
/*      */   public static void setDisableAutoIndexing(boolean isDisableAutoIndexing)
/*      */   {
/* 1578 */     m_disableAutoIndexing = isDisableAutoIndexing;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1583 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102694 $";
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*  118 */     m_actionStrings = new Hashtable();
/*  119 */     for (int i = 0; i < m_actionIds.length; ++i)
/*      */     {
/*  121 */       m_actionStrings.put(m_actionIds[i], new Integer(i));
/*      */     }
/*      */   }
/*      */ 
/*      */   static class StatusMonitor
/*      */     implements Runnable
/*      */   {
/*      */     public void run()
/*      */     {
/* 1386 */       int count = 2;
/* 1387 */       boolean isAuto = SharedObjects.getEnvValueAsBoolean("IsAutoSearch", false);
/* 1388 */       if (!isAuto)
/*      */       {
/* 1390 */         count = -1;
/* 1391 */         IndexerMonitor.setDisableAutoIndexing(true);
/*      */       }
/*      */ 
/* 1394 */       while (!SystemUtils.m_isServerStopped)
/*      */       {
/* 1396 */         boolean notify = false;
/* 1397 */         int activeCount = 0;
/*      */         try
/*      */         {
/* 1400 */           Hashtable table = new Hashtable();
/* 1401 */           String[] list = IndexerMonitor.allLockFiles();
/* 1402 */           for (int i = 0; (list != null) && (i < list.length); ++i)
/*      */           {
/* 1404 */             String cycleId = IndexerMonitor.getCycleId(list[i]);
/* 1405 */             if (cycleId == null) {
/*      */               continue;
/*      */             }
/*      */ 
/* 1409 */             ++activeCount;
/*      */ 
/* 1412 */             IndexerMonitor.IndexLockFileStatus stat = (IndexerMonitor.IndexLockFileStatus)IndexerMonitor.m_activeCycles.get(cycleId);
/*      */ 
/* 1414 */             if (stat == null)
/*      */             {
/* 1416 */               stat = IndexerMonitor.newIndexLockFileStatus(cycleId);
/* 1417 */               notify = true;
/*      */             }
/*      */             else
/*      */             {
/* 1421 */               long fileTime = stat.m_file.lastModified();
/* 1422 */               File suspendedFile = FileUtilsCfgBuilder.getCfgFile(IndexerMonitor.getLockPath(cycleId, "suspended"), "Lock", false);
/* 1423 */               boolean hasSuspendFile = suspendedFile.exists();
/*      */ 
/* 1425 */               if ((hasSuspendFile) || (fileTime == stat.m_lastTime))
/*      */               {
/* 1427 */                 if ((stat.m_hasChanged) && (!hasSuspendFile))
/*      */                 {
/* 1429 */                   if (SystemUtils.m_verbose)
/*      */                   {
/* 1431 */                     Report.debug("indexermonitor", "-Lock file for " + cycleId + " has not changed.", null);
/*      */                   }
/* 1433 */                   stat.m_hasChanged = false;
/*      */                 }
/*      */                 else
/*      */                 {
/* 1437 */                   boolean statusChange = stat.m_status != 32;
/* 1438 */                   if (statusChange)
/*      */                   {
/* 1440 */                     if (SystemUtils.m_verbose)
/*      */                     {
/* 1442 */                       Report.debug("indexermonitor", "-Lock file for " + cycleId + " has been previously interrupted.", null);
/*      */                     }
/* 1444 */                     stat.m_status = 32;
/* 1445 */                     notify = true;
/*      */                   }
/* 1447 */                   if (!hasSuspendFile)
/*      */                   {
/* 1449 */                     FileUtils.touchFile(suspendedFile.getAbsolutePath());
/*      */                   }
/*      */                 }
/*      */               }
/*      */               else
/*      */               {
/* 1455 */                 if (stat.m_status != 16)
/*      */                 {
/* 1457 */                   notify = true;
/*      */                 }
/* 1459 */                 stat.m_status = 16;
/* 1460 */                 stat.m_hasChanged = true;
/*      */               }
/* 1462 */               stat.m_lastTime = fileTime;
/*      */             }
/* 1464 */             if ((notify) && 
/* 1466 */               (SystemUtils.m_verbose))
/*      */             {
/* 1468 */               Report.debug("indexermonitor", "-Adding " + cycleId + " to current active list.", null);
/*      */             }
/*      */ 
/* 1471 */             table.put(cycleId, stat);
/*      */           }
/*      */ 
/* 1474 */           if (IndexerMonitor.m_activeCycles.size() != table.size())
/*      */           {
/* 1476 */             if (SystemUtils.m_verbose)
/*      */             {
/* 1478 */               Report.debug("indexermonitor", "-Changed from " + IndexerMonitor.m_activeCycles.size() + " to " + table.size() + " active cycles.", null);
/*      */             }
/*      */ 
/* 1481 */             notify = true;
/*      */           }
/* 1483 */           IndexerMonitor.m_activeCycles = table;
/*      */ 
/* 1485 */           switch (count)
/*      */           {
/*      */           default:
/* 1488 */             --count;
/* 1489 */             break;
/*      */           case 0:
/* 1491 */             count = -1;
/* 1492 */             restartCycles();
/*      */           case -1:
/*      */           }
/*      */ 
/* 1498 */           if (notify)
/*      */           {
/* 1500 */             IndexerMonitor.notifyStatusChange();
/* 1501 */             if (activeCount == 1)
/*      */             {
/* 1503 */               IndexerMonitor.notifySingleActiveCycle();
/*      */             }
/*      */           }
/*      */ 
/* 1507 */           if (activeCount == 1)
/*      */           {
/* 1509 */             if (SystemUtils.m_verbose)
/*      */             {
/* 1511 */               Report.debug("indexermonitor", "Notification of Exclusive Lock", null);
/*      */             }
/* 1513 */             IndexerMonitor.notifyExclusiveLock();
/*      */           }
/*      */ 
/* 1516 */           SystemUtils.sleep(IndexerMonitor.m_touchMonitorInterval);
/*      */         }
/*      */         catch (Throwable ignore)
/*      */         {
/* 1520 */           Report.trace(null, null, ignore);
/*      */ 
/* 1523 */           SystemUtils.sleep(1000L);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*      */     public void restartCycles() throws ServiceException
/*      */     {
/* 1530 */       if (IndexerMonitor.m_restartID.equals("no"))
/*      */       {
/* 1532 */         return;
/*      */       }
/*      */ 
/* 1536 */       FileUtils.reserveDirectory(IndexerMonitor.m_indexLockDir, true);
/*      */       try
/*      */       {
/* 1539 */         String[] list = IndexerMonitor.allLockFiles();
/* 1540 */         for (int i = 0; i < list.length; ++i)
/*      */         {
/* 1542 */           String cycleId = IndexerMonitor.getCycleId(list[i]);
/* 1543 */           if (cycleId == null) {
/*      */             continue;
/*      */           }
/*      */ 
/* 1547 */           int state = IndexerMonitor.getCycleState(cycleId, null);
/* 1548 */           if (state != 32) {
/*      */             continue;
/*      */           }
/* 1551 */           DataBinder binder = new DataBinder();
/* 1552 */           if (!ResourceUtils.serializeDataBinder(IndexerMonitor.m_indexDir + cycleId, "state.hda", binder, false, false)) {
/*      */             continue;
/*      */           }
/* 1555 */           String restartID = binder.getLocal("RestartId");
/* 1556 */           if ((restartID == null) || (!IndexerMonitor.m_restartID.equals(restartID)))
/*      */             continue;
/* 1558 */           IndexerMonitor.adjustIndexingInternal(cycleId, 1, binder.getLocalData());
/*      */         }
/*      */ 
/*      */       }
/*      */       finally
/*      */       {
/* 1566 */         FileUtils.releaseDirectory(IndexerMonitor.m_indexLockDir, true);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   static class IndexLockFileStatus
/*      */   {
/*      */     public long m_lastTime;
/*      */     public boolean m_hasChanged;
/*      */     public boolean m_processOwnsLock;
/*      */     public int m_status;
/*      */     public File m_file;
/*      */   }
/*      */ 
/*      */   static class RunningIndexer
/*      */   {
/*      */     public String m_cycleId;
/*      */     public Indexer m_indexer;
/*      */     public boolean m_isFinished;
/*      */     public boolean m_isError;
/*      */     public String m_lockFile;
/*      */ 
/*      */     public RunningIndexer(String cycleId)
/*      */     {
/* 1367 */       this.m_cycleId = cycleId;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IndexerMonitor
 * JD-Core Version:    0.5.4
 */