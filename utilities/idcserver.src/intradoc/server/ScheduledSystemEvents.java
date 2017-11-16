/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IntervalData;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.jobs.ScheduledJobsProcessor;
/*     */ import intradoc.server.workflow.WfCompanionManager;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ScheduledSystemEvents
/*     */   implements Runnable
/*     */ {
/*     */   public static final int DATABASE_MAINTENANCE = 1;
/*     */   public static final int CACHE_MAINTENANCE = 2;
/*     */   public static final int WORKFLOW_TIMER = 4;
/*     */   public static final int SCHEDULED_WORK = 8;
/*     */   protected String[] m_scheduledEventsActiveCategories;
/*     */   protected DataResultSet m_eventsList;
/*     */   protected Workspace m_workspace;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected int m_eventIndex;
/*     */   protected int m_categoryIndex;
/*     */   protected int m_configIntervalIndex;
/*     */   protected int m_intervalTypeIndex;
/*     */   protected int m_defaultIntervalIndex;
/*     */   protected int m_allowEventScriptIndex;
/*     */   protected boolean m_doDebugFastEvents;
/*     */   protected Hashtable m_activeEvents;
/*     */   protected Vector m_orderedEventKeys;
/*     */   protected Hashtable m_initiatedCounts;
/*     */   protected boolean m_enableAccessDBCompact;
/*     */   protected int m_numGCPerRun;
/*     */ 
/*     */   public ScheduledSystemEvents()
/*     */   {
/*  42 */     this.m_scheduledEventsActiveCategories = null;
/*     */ 
/*  45 */     this.m_eventsList = null;
/*     */ 
/*  48 */     this.m_workspace = null;
/*     */ 
/*  51 */     this.m_cxt = null;
/*     */ 
/*  62 */     this.m_doDebugFastEvents = false;
/*     */ 
/*  66 */     this.m_activeEvents = new Hashtable();
/*  67 */     this.m_orderedEventKeys = new IdcVector();
/*     */ 
/*  70 */     this.m_initiatedCounts = new Hashtable();
/*     */ 
/*  73 */     this.m_enableAccessDBCompact = false;
/*     */ 
/*  76 */     this.m_numGCPerRun = 0;
/*     */   }
/*     */ 
/*     */   public synchronized void setScheduledEventsActiveCategories(String[] categories) {
/*  80 */     this.m_scheduledEventsActiveCategories = categories;
/*     */   }
/*     */ 
/*     */   public synchronized String[] getScheduledEventsActiveCategories()
/*     */   {
/*  85 */     return this.m_scheduledEventsActiveCategories;
/*     */   }
/*     */ 
/*     */   public void init(DataResultSet eventsList, Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  93 */     this.m_eventsList = eventsList;
/*  94 */     this.m_workspace = ws;
/*  95 */     this.m_cxt = cxt;
/*  96 */     prepareLookupIndices();
/*  97 */     this.m_doDebugFastEvents = SharedObjects.getEnvValueAsBoolean("DoDebugFastScheduledEvents", false);
/*  98 */     this.m_enableAccessDBCompact = SharedObjects.getEnvValueAsBoolean("EnableDBCompact", false);
/*  99 */     this.m_numGCPerRun = SharedObjects.getEnvironmentInt("NumberGCPerSECheck", 0);
/* 100 */     ActiveState.load();
/*     */ 
/* 103 */     Thread t = new Thread(this, "Scheduled System Events");
/* 104 */     t.setDaemon(true);
/* 105 */     t.start();
/*     */   }
/*     */ 
/*     */   public synchronized void checkScheduledEvents()
/*     */     throws DataException, ServiceException
/*     */   {
/* 114 */     if ((this.m_eventsList == null) || (this.m_scheduledEventsActiveCategories == null) || (this.m_scheduledEventsActiveCategories.length == 0) || (this.m_workspace == null))
/*     */     {
/* 117 */       return;
/*     */     }
/*     */ 
/* 120 */     long curTime = System.currentTimeMillis();
/* 121 */     boolean haveWorkToDo = false;
/*     */ 
/* 124 */     DataBinder data = new DataBinder(SharedObjects.getSecureEnvironment());
/* 125 */     PageMerger pageMerger = new PageMerger(data, null);
/*     */ 
/* 127 */     for (int i = 0; i < this.m_scheduledEventsActiveCategories.length; ++i)
/*     */     {
/* 129 */       String category = this.m_scheduledEventsActiveCategories[i];
/* 130 */       for (this.m_eventsList.first(); this.m_eventsList.isRowPresent(); this.m_eventsList.next())
/*     */       {
/* 132 */         if (!category.equalsIgnoreCase(this.m_eventsList.getStringValue(this.m_categoryIndex)))
/*     */           continue;
/* 134 */         Vector v = this.m_eventsList.getCurrentRowValues();
/* 135 */         if (!checkIfShouldExecute(v, pageMerger, curTime, false)) {
/*     */           continue;
/*     */         }
/* 138 */         String event = this.m_eventsList.getStringValue(this.m_eventIndex);
/* 139 */         v = (Vector)v.clone();
/*     */ 
/* 143 */         if (this.m_activeEvents.get(event) != null)
/*     */           continue;
/* 145 */         this.m_activeEvents.put(event, v);
/* 146 */         this.m_orderedEventKeys.addElement(event);
/* 147 */         haveWorkToDo = true;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 153 */     pageMerger.releaseAllTemporary();
/*     */     try
/*     */     {
/* 157 */       data.putLocal("curTime", "" + curTime);
/* 158 */       if (haveWorkToDo)
/*     */       {
/* 160 */         data.putLocal("haveWorkToDo", "1");
/*     */       }
/* 162 */       PluginFilters.filter("checkScheduledEvents", this.m_workspace, data, this.m_cxt);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 166 */       Report.error(null, "!csScheduledEventFilterError", e);
/*     */     }
/*     */ 
/* 169 */     IntervalData finalizationInterval = new IntervalData("");
/* 170 */     for (int i = 0; i < this.m_numGCPerRun; ++i)
/*     */     {
/* 172 */       System.runFinalization();
/* 173 */       finalizationInterval.trace("system", "System.runFinalization()");
/* 174 */       System.gc();
/* 175 */       finalizationInterval.trace("system", "System.gc()");
/* 176 */       finalizationInterval.stop();
/*     */     }
/*     */ 
/* 179 */     if (!haveWorkToDo) {
/*     */       return;
/*     */     }
/* 182 */     super.notify();
/*     */   }
/*     */ 
/*     */   public void prepareLookupIndices()
/*     */     throws DataException
/*     */   {
/* 188 */     if (this.m_eventsList == null)
/*     */     {
/* 190 */       return;
/*     */     }
/* 192 */     this.m_eventIndex = ResultSetUtils.getIndexMustExist(this.m_eventsList, "action");
/* 193 */     this.m_categoryIndex = ResultSetUtils.getIndexMustExist(this.m_eventsList, "eventCategory");
/* 194 */     this.m_configIntervalIndex = ResultSetUtils.getIndexMustExist(this.m_eventsList, "configIntervalKey");
/* 195 */     this.m_intervalTypeIndex = ResultSetUtils.getIndexMustExist(this.m_eventsList, "intervalType");
/* 196 */     this.m_defaultIntervalIndex = ResultSetUtils.getIndexMustExist(this.m_eventsList, "defaultInterval");
/* 197 */     this.m_allowEventScriptIndex = ResultSetUtils.getIndexMustExist(this.m_eventsList, "allowEventScript");
/*     */   }
/*     */ 
/*     */   public boolean checkIfShouldExecute(Vector eventInfo, PageMerger pageMerger, long curTime, boolean updateStatus)
/*     */     throws ServiceException
/*     */   {
/* 215 */     String activeStateDir = ActiveState.getActiveStateDirectory();
/* 216 */     if (updateStatus)
/*     */     {
/* 218 */       FileUtils.reserveDirectory(activeStateDir);
/*     */     }
/*     */ 
/* 223 */     boolean retVal = true;
/*     */     try
/*     */     {
/* 227 */       String action = (String)eventInfo.elementAt(this.m_eventIndex);
/* 228 */       String configKey = (String)eventInfo.elementAt(this.m_configIntervalIndex);
/* 229 */       String increment = (String)eventInfo.elementAt(this.m_intervalTypeIndex);
/* 230 */       String defaultIntervalStr = (String)eventInfo.elementAt(this.m_defaultIntervalIndex);
/* 231 */       String category = (String)eventInfo.elementAt(this.m_categoryIndex);
/* 232 */       int defaultInterval = NumberUtils.parseInteger(defaultIntervalStr, 1);
/* 233 */       int numInc = SharedObjects.getEnvironmentInt(configKey, defaultInterval);
/* 234 */       long stdInterval = (this.m_doDebugFastEvents) ? 60000L : 3600000L;
/* 235 */       long intervalMillis = numInc * stdInterval;
/* 236 */       if (increment.equals("inDays"))
/*     */       {
/* 238 */         intervalMillis *= 24L;
/*     */       }
/* 240 */       if (intervalMillis <= 0L)
/*     */       {
/* 242 */         int i = 0;
/*     */         return i;
/*     */       }
/* 247 */       if (updateStatus)
/*     */       {
/* 249 */         ActiveState.load();
/*     */       }
/*     */ 
/* 252 */       Date prevVal = ActiveState.getResultSetDate("ScheduledEvents", action);
/*     */ 
/* 254 */       if (SystemUtils.m_verbose)
/*     */       {
/* 256 */         String traceMsg = "checkIfShouldExecute " + action + " " + configKey + " " + increment + " " + defaultIntervalStr + " " + category;
/*     */ 
/* 258 */         String prevValStr = "(empty)";
/* 259 */         if (prevVal != null)
/*     */         {
/* 261 */           prevValStr = LocaleResources.m_iso8601Format.format(prevVal, 2);
/*     */         }
/* 263 */         traceMsg = traceMsg + "\nprevDateVal=" + prevValStr + " updateStatus=" + updateStatus;
/* 264 */         Report.debug("scheduledevents", traceMsg, null);
/*     */       }
/* 266 */       String reportStr = null;
/*     */ 
/* 268 */       if (prevVal == null)
/*     */       {
/* 270 */         if (updateStatus)
/*     */         {
/* 272 */           reportStr = "!csScheduledEventStart";
/*     */ 
/* 275 */           retVal = false;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 280 */         long prevTime = prevVal.getTime();
/* 281 */         if (prevTime != 0L)
/*     */         {
/* 283 */           long prevCount = prevTime / intervalMillis;
/* 284 */           long curCount = curTime / intervalMillis;
/* 285 */           String initiatedCountStr = (String)this.m_initiatedCounts.get(action);
/*     */ 
/* 290 */           boolean doCheck = (updateStatus) || (initiatedCountStr == null);
/*     */ 
/* 293 */           retVal = curCount != prevCount;
/*     */ 
/* 296 */           if ((retVal) && (doCheck))
/*     */           {
/* 298 */             if (updateStatus)
/*     */             {
/* 300 */               reportStr = LocaleUtils.encodeMessage("csScheduledEventStart", null, action);
/*     */             }
/*     */             else
/*     */             {
/* 306 */               String catDisabledStr = category + ":isDisabled";
/* 307 */               boolean isDisabled = SharedObjects.getEnvValueAsBoolean(catDisabledStr, false);
/* 308 */               if (isDisabled)
/*     */               {
/* 310 */                 retVal = false;
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/* 315 */             if (retVal)
/*     */             {
/* 317 */               DataBinder binder = pageMerger.getDataBinder();
/* 318 */               binder.clearResultSets();
/* 319 */               binder.getLocalData().clear();
/* 320 */               String script = (String)eventInfo.elementAt(this.m_allowEventScriptIndex);
/* 321 */               if (pageMerger.m_isReportErrorStack)
/*     */               {
/* 323 */                 String name = action + "(allowEventScript)";
/* 324 */                 String msg = LocaleUtils.encodeMessage("csDynHTMLEvalVariableInMethod", null, "ScheduledSystemEvents.checkIfShouldExecute", name);
/*     */ 
/* 326 */                 pageMerger.pushStackMessage(msg);
/*     */               }
/*     */               try
/*     */               {
/* 330 */                 binder.putLocal("action", action);
/* 331 */                 binder.putLocal("prevTime", "" + prevTime);
/* 332 */                 binder.putLocal("curTime", "" + curTime);
/* 333 */                 binder.putLocal("intervalMillis", "" + intervalMillis);
/* 334 */                 binder.putLocal("category", category);
/* 335 */                 if (updateStatus)
/*     */                 {
/* 337 */                   binder.putLocal("updateStatus", "1");
/*     */                 }
/*     */ 
/* 340 */                 pageMerger.evaluateScript(script);
/*     */ 
/* 342 */                 if (SystemUtils.m_verbose)
/*     */                 {
/* 344 */                   Report.debug("scheduledevents", "checkScheduledEvents:" + action + " script= " + script, null);
/*     */ 
/* 346 */                   Properties props = binder.getLocalData();
/* 347 */                   Report.debug("scheduledevents", "checkScheduledEvents:" + action + "  Local data: " + props, null);
/*     */                 }
/*     */ 
/*     */               }
/*     */               catch (IOException e)
/*     */               {
/*     */               }
/*     */               finally
/*     */               {
/* 359 */                 if (pageMerger.m_isReportErrorStack)
/*     */                 {
/* 361 */                   pageMerger.popStack();
/*     */                 }
/*     */ 
/*     */               }
/*     */ 
/* 370 */               retVal = DataBinderUtils.getBoolean(binder, "doEvent", false);
/* 371 */               if (SystemUtils.m_verbose)
/*     */               {
/* 373 */                 String msg = "checkScheduledEvents:" + action + " scriptEvalResult doEvent=" + retVal;
/*     */ 
/* 375 */                 Report.debug("scheduledevents", msg, null);
/*     */               }
/*     */             }
/*     */           }
/*     */ 
/* 380 */           if ((retVal) && (!updateStatus))
/*     */           {
/* 382 */             if (initiatedCountStr != null)
/*     */             {
/* 384 */               long initiatedCount = NumberUtils.parseLong(initiatedCountStr, 0L);
/* 385 */               boolean initiatedCountOld = (curCount != initiatedCount + 1L) && (curCount != initiatedCount - 1L) && (initiatedCount != curCount);
/*     */ 
/* 387 */               if (initiatedCountOld)
/*     */               {
/* 389 */                 Report.error(null, null, "csScheduledEventNotBeingHandled", new Object[] { action, "" + intervalMillis / 1000L });
/*     */ 
/* 393 */                 this.m_initiatedCounts.put(action, "" + curCount);
/*     */               }
/*     */ 
/* 397 */               retVal = false;
/*     */             }
/*     */             else
/*     */             {
/* 402 */               this.m_initiatedCounts.put(action, "" + curCount);
/*     */             }
/*     */           }
/*     */         }
/* 406 */         else if (updateStatus)
/*     */         {
/* 408 */           reportStr = "!csScheduledEventRestart";
/*     */ 
/* 411 */           retVal = false;
/*     */         }
/*     */       }
/*     */ 
/* 415 */       if (updateStatus)
/*     */       {
/* 417 */         if (reportStr != null)
/*     */         {
/* 419 */           updateEventState(action, reportStr, curTime);
/*     */         }
/*     */ 
/* 422 */         this.m_initiatedCounts.remove(action);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 427 */       if (updateStatus)
/*     */       {
/* 429 */         FileUtils.releaseDirectory(activeStateDir);
/*     */       }
/*     */     }
/* 432 */     return retVal;
/*     */   }
/*     */ 
/*     */   public void updateEventStateWithLock(String eventID, String des)
/*     */   {
/* 437 */     String activeStateDir = ActiveState.getActiveStateDirectory();
/*     */     try
/*     */     {
/* 440 */       FileUtils.reserveDirectory(activeStateDir);
/* 441 */       long curTime = System.currentTimeMillis();
/* 442 */       updateEventState(eventID, des, curTime);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 446 */       String msg = "Failed updateEventState with eventID " + eventID + " and description " + des + ". ";
/*     */ 
/* 448 */       Report.trace("system", msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 452 */       FileUtils.releaseDirectory(activeStateDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateEventState(String eventId, String des, long curTime)
/*     */     throws ServiceException
/*     */   {
/* 459 */     ActiveState.createResultSetIfNone("ScheduledEvents", new String[] { "eventid", "lastmodified", "lastaction" });
/*     */ 
/* 461 */     String curTimeStr = LocaleUtils.formatODBC(new Date(curTime));
/* 462 */     des = LocaleResources.localizeMessage(des, null);
/*     */ 
/* 465 */     String updateTrace = "Scheduled event update " + eventId + " " + des;
/* 466 */     Report.trace("scheduledevents", updateTrace, null);
/*     */ 
/* 468 */     ActiveState.setResultSetValue("ScheduledEvents", eventId, curTimeStr, des);
/* 469 */     ActiveState.save();
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     try
/*     */     {
/* 476 */       int timeout = SharedObjects.getTypedEnvironmentInt("IdcApplicationQueryTimeout", 900, 24, 24);
/*     */ 
/* 479 */       if (this.m_workspace != null)
/*     */       {
/* 481 */         this.m_workspace.setThreadTimeout(timeout);
/*     */       }
/* 483 */       SystemUtils.registerSynchronizationObjectToNotifyOnStop(this);
/* 484 */       while (!SystemUtils.m_isServerStopped)
/*     */       {
/* 486 */         DataBinder eventData = checkForWork();
/* 487 */         if (eventData != null)
/*     */         {
/* 489 */           processWork(eventData);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 496 */       Report.error(null, null, e);
/*     */     }
/*     */     finally
/*     */     {
/* 500 */       if (this.m_workspace != null)
/*     */       {
/* 502 */         this.m_workspace.clearThreadTimeout();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected synchronized DataBinder checkForWork()
/*     */   {
/* 509 */     DataBinder workPackage = null;
/* 510 */     if (this.m_activeEvents.size() > 0)
/*     */     {
/* 512 */       String key = null;
/* 513 */       if (this.m_orderedEventKeys.size() > 0)
/*     */       {
/* 515 */         String nextKey = (String)this.m_orderedEventKeys.elementAt(0);
/* 516 */         if (this.m_activeEvents.get(nextKey) != null)
/*     */         {
/* 518 */           key = nextKey;
/*     */         }
/*     */       }
/* 521 */       if (key == null)
/*     */       {
/* 524 */         Enumeration e = this.m_activeEvents.keys();
/* 525 */         key = (String)e.nextElement();
/*     */       }
/* 527 */       if (key != null)
/*     */       {
/* 529 */         long curTime = System.currentTimeMillis();
/* 530 */         DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/* 531 */         PageMerger pageMerger = new PageMerger(binder, this.m_cxt);
/* 532 */         Vector v = (Vector)this.m_activeEvents.get(key);
/*     */         try
/*     */         {
/* 535 */           if (checkIfShouldExecute(v, pageMerger, curTime, true))
/*     */           {
/* 537 */             DataResultSet drset = new DataResultSet();
/* 538 */             drset.copyFieldInfo(this.m_eventsList);
/* 539 */             drset.addRow(v);
/* 540 */             Properties props = drset.getCurrentRowProps();
/* 541 */             DataBinder.mergeHashTables(binder.getLocalData(), props);
/* 542 */             workPackage = binder;
/*     */           }
/*     */         }
/*     */         catch (Exception excep)
/*     */         {
/* 547 */           String msg = "Failed checkIfShouldExecute with key " + key + ". ";
/* 548 */           Report.trace("system", msg, excep);
/*     */         }
/*     */         finally
/*     */         {
/* 552 */           pageMerger.releaseAllTemporary();
/* 553 */           pageMerger = null;
/*     */         }
/* 555 */         if (workPackage == null)
/*     */         {
/* 557 */           this.m_activeEvents.remove(key);
/* 558 */           if (this.m_orderedEventKeys.size() > 0)
/*     */           {
/* 560 */             this.m_orderedEventKeys.removeElementAt(0);
/*     */           }
/* 562 */           this.m_initiatedCounts.remove(key);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 569 */       if ((this.m_activeEvents.size() == 0) && (workPackage == null))
/*     */       {
/* 571 */         SystemUtils.wait(this, 300000L);
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 576 */       Report.trace(null, null, ignore);
/*     */     }
/* 578 */     return workPackage;
/*     */   }
/*     */ 
/*     */   protected void processWork(DataBinder eventData)
/*     */   {
/* 586 */     String action = eventData.getLocal("action");
/*     */     try
/*     */     {
/* 590 */       if (checkHandleEvent(action, eventData))
/*     */       {
/* 593 */         String finishMsg = LocaleUtils.encodeMessage("csScheduledEventFinished", null, action);
/* 594 */         updateEventStateWithLock(action, finishMsg);
/*     */       }
/*     */       else
/*     */       {
/* 599 */         String msg = LocaleUtils.encodeMessage("csScheduledEventUnhandled", null, action);
/* 600 */         Report.error(null, msg, null);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 605 */       String errMsg = LocaleUtils.encodeMessage("csScheduledEventError", null, action);
/* 606 */       Report.error(null, errMsg, e);
/*     */ 
/* 610 */       updateEventStateWithLock(action, errMsg);
/*     */     }
/*     */     finally
/*     */     {
/* 615 */       this.m_workspace.releaseConnection();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean checkHandleEvent(String action, DataBinder eventData)
/*     */     throws DataException, ServiceException
/*     */   {
/* 623 */     if (PluginFilters.filter("scheduledSystemEvent", this.m_workspace, eventData, this.m_cxt) != 0)
/*     */     {
/* 626 */       return true;
/*     */     }
/*     */ 
/* 630 */     boolean result = true;
/* 631 */     if ((this.m_enableAccessDBCompact) && (action.equals("DatabaseCompact")))
/*     */     {
/* 633 */       this.m_workspace.dbManagement(1, null);
/*     */     }
/* 635 */     else if (action.equals("DatedCacheRemoval"))
/*     */     {
/* 637 */       DatedCacheUtils.removeDatedCaches(this.m_workspace);
/*     */     }
/* 639 */     else if (action.equals("WorkflowTimer"))
/*     */     {
/* 641 */       WfCompanionManager.updateWorkflowItems(this.m_workspace, "TIMED_UPDATE");
/*     */     }
/* 643 */     else if (action.equals("NotificationOfExpiration"))
/*     */     {
/* 645 */       ExpirationNotifier expNotf = (ExpirationNotifier)ComponentClassFactory.createClassInstance("ExpirationNotifier", "intradoc.server.ExpirationNotifier", "!csExpirationNotifierUnableToInit");
/*     */ 
/* 648 */       expNotf.init(this.m_workspace, eventData, this.m_cxt);
/* 649 */       expNotf.runNotification();
/*     */     }
/* 651 */     else if ((action.equals("ScheduledLongJobs")) || (action.equals("ScheduledShortJobs")))
/*     */     {
/* 653 */       String qType = (action.equals("ScheduledLongJobs")) ? "L" : "S";
/* 654 */       eventData.putLocal("dSjQueueType", qType);
/*     */ 
/* 656 */       ScheduledJobsProcessor sJobs = (ScheduledJobsProcessor)ComponentClassFactory.createClassInstance("ScheduledJobsProcessor", "intradoc.server.jobs.ScheduledJobsProcessor", "!csScheduledLongJobsManagerUnableToInit");
/*     */ 
/* 660 */       sJobs.processJobs(this.m_workspace, eventData, this.m_cxt);
/*     */     }
/*     */     else
/*     */     {
/* 664 */       result = false;
/*     */     }
/*     */ 
/* 667 */     return result;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 673 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84490 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ScheduledSystemEvents
 * JD-Core Version:    0.5.4
 */