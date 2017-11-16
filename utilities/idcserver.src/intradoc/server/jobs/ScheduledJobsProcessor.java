/*     */ package intradoc.server.jobs;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.ProgressState;
/*     */ import intradoc.shared.ProgressStateUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ScheduledJobsProcessor
/*     */ {
/*     */   public void processJobs(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/*  72 */     ScheduledJobStorage jStorage = ScheduledJobManager.getStorage(ws);
/*     */ 
/*  74 */     String jobType = binder.getLocal("dSjQueueType");
/*  75 */     ProgressState progress = new ProgressState();
/*  76 */     progress.init(jobType + "ScheduledJobs");
/*  77 */     ProgressStateUtils.reportProgress(progress, null, "scheduledjobs", 4, null, "csSjStartProcessing", new Object[0]);
/*     */ 
/*  81 */     DataResultSet jobSet = createScheduledJobList(binder, ws, cxt);
/*     */ 
/*  83 */     int count = 1;
/*  84 */     int numRows = jobSet.getNumRows();
/*  85 */     for (jobSet.first(); jobSet.isRowPresent(); ++count)
/*     */     {
/*  87 */       Map map = jobSet.getCurrentRowMap();
/*  88 */       String jobId = ScheduledJobUtils.getId(map);
/*     */ 
/*  90 */       ProgressStateUtils.reportProgress(progress, null, "scheduledjobs", 3, null, "csSjStartingNewJob", new Object[] { jobId, Integer.valueOf(count), Integer.valueOf(numRows) });
/*     */ 
/*  92 */       processJob(map, ws, cxt, jStorage, progress);
/*  93 */       ProgressStateUtils.reportProgress(progress, null, "scheduledjobs", 3, null, "csSjFinishedJob", new Object[] { jobId, Integer.valueOf(count), Integer.valueOf(numRows) });
/*     */ 
/*  85 */       jobSet.next();
/*     */     }
/*     */ 
/*  96 */     ProgressStateUtils.reportProgress(progress, null, "scheduledjobs", 2, null, "csSjStopProcessing", new Object[0]);
/*     */   }
/*     */ 
/*     */   public DataResultSet createScheduledJobList(DataBinder binder, Workspace ws, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/* 103 */     String query = "QscheduledJobsInQueue";
/* 104 */     boolean isImmediate = DataBinderUtils.getBoolean(binder, "isImmediateJob", false);
/* 105 */     if (isImmediate)
/*     */     {
/* 107 */       query = "QscheduledJobsOfType";
/*     */     }
/*     */ 
/* 110 */     ResultSet rset = ws.createResultSet(query, binder);
/* 111 */     DataResultSet jobSet = new DataResultSet();
/* 112 */     jobSet.copy(rset);
/*     */ 
/* 114 */     Object[] params = { jobSet, binder };
/* 115 */     cxt.setCachedObject("sortScheduledJobs:parameters", params);
/* 116 */     int ret = PluginFilters.filter("sortScheduledJobs", ws, binder, cxt);
/* 117 */     if (ret != 1);
/* 122 */     return jobSet;
/*     */   }
/*     */ 
/*     */   public void processJob(Map jobInfo, Workspace ws, ExecutionContext cxt, ScheduledJobStorage jStorage, ProgressState globalProgress)
/*     */     throws DataException, ServiceException
/*     */   {
/* 151 */     if (SystemUtils.m_verbose)
/*     */     {
/* 153 */       Report.debug("scheduledjobs", "processJob: job info=" + jobInfo, null);
/*     */     }
/* 155 */     String id = ScheduledJobUtils.getId(jobInfo);
/* 156 */     String dir = jStorage.getActiveDir();
/* 157 */     String lockName = "sj" + id;
/*     */ 
/* 159 */     boolean isLocked = FileUtils.reserveLongTermLock(dir, lockName, "ScheduledJobs", 4 * FileUtils.m_touchMonitorInterval, false);
/*     */ 
/* 161 */     if (!isLocked)
/*     */     {
/* 163 */       ProgressStateUtils.reportError(globalProgress, "scheduledjobs", null, "csSjLockedJob", new Object[] { id });
/* 164 */       return;
/*     */     }
/* 166 */     JobState jState = new JobState(globalProgress);
/* 167 */     ScheduledJobImplementor sJobImp = null;
/*     */     try
/*     */     {
/* 171 */       prepareForWork(jobInfo, dir, id, jState, ws, cxt);
/*     */ 
/* 174 */       if (jState.m_hasWork)
/*     */       {
/* 177 */         String clName = jState.m_data.getLocal("sjClassName");
/* 178 */         if (clName == null)
/*     */         {
/* 180 */           clName = "intradoc.server.jobs.ServiceJobImplementor";
/*     */         }
/*     */ 
/* 183 */         sJobImp = (ScheduledJobImplementor)ComponentClassFactory.createClassInstance("JobClass" + id, clName, "!csScheduledJobUnableToInit");
/*     */ 
/* 185 */         sJobImp.processJob(jState, ws, cxt);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 190 */       handleTerminatingException(e, jState);
/*     */     }
/*     */     finally
/*     */     {
/* 198 */       doCleanUp(jState, jStorage, sJobImp, ws, cxt);
/*     */ 
/* 200 */       FileUtils.releaseLongTermLock(dir, lockName, "ScheduledJobs");
/* 201 */       ws.releaseConnection();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void prepareForWork(Map jobInfo, String dir, String id, JobState jState, Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 209 */     ws.beginTranEx(4);
/* 210 */     boolean isInTran = true;
/*     */     try
/*     */     {
/* 214 */       MapParameters params = new MapParameters(jobInfo);
/* 215 */       ResultSet rset = ws.createResultSet("QscheduledJob", params);
/* 216 */       if (!rset.isEmpty())
/*     */       {
/* 220 */         DataResultSet drset = new DataResultSet();
/* 221 */         drset.copy(rset);
/* 222 */         jobInfo = drset.getCurrentRowMap();
/*     */ 
/* 224 */         initJobStateObject(jState, jobInfo, dir, id);
/* 225 */         jState.m_hasWork = checkIfShouldProcess(jState, ws, cxt);
/*     */ 
/* 228 */         if (jState.m_hasWork)
/*     */         {
/* 231 */           ProgressStateUtils.reportProgress(jState.m_globalProgress, null, "scheduledjobs", 4, null, "csSjStartWork", new Object[0]);
/*     */ 
/* 233 */           jState.m_data.putLocal("dSjState", "A");
/* 234 */           jState.m_data.putLocal("dSjProgress", "!wwSjStarted");
/* 235 */           ws.execute("UscheduledJobProgressState", jState.m_data);
/*     */ 
/* 238 */           jState.m_data.putLocal("dSjMessage", "!wwSjStarted");
/* 239 */           jState.m_data.putLocal("dSjLastProcessedStatus", "A");
/* 240 */           ScheduledJobUtils.addJobHistoryEvent(jState.m_data, ws, cxt);
/*     */         }
/*     */       }
/* 243 */       ws.commitTran();
/* 244 */       isInTran = false;
/*     */     }
/*     */     finally
/*     */     {
/* 248 */       if (isInTran)
/*     */       {
/* 250 */         ws.rollbackTran();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void handleTerminatingException(Exception e, JobState jState)
/*     */   {
/* 261 */     boolean isFatal = false;
/* 262 */     if (e instanceof ServiceException)
/*     */     {
/* 264 */       ServiceException se = (ServiceException)e;
/* 265 */       if ((se.m_errorCode == -32) || (se.m_errorCode == -65))
/*     */       {
/* 268 */         isFatal = true;
/*     */       }
/*     */     }
/* 271 */     else if (e != null)
/*     */     {
/* 273 */       isFatal = true;
/*     */     }
/*     */ 
/* 276 */     jState.setErrorState(e, "csSjTerminatedJob", isFatal);
/* 277 */     Report.trace("scheduledjobs", e, "csSjTerminatingException", new Object[0]);
/* 278 */     jState.reportProgress(-1, e, "csSjTerminatingException", new Object[0]);
/*     */   }
/*     */ 
/*     */   public void initJobStateObject(JobState jState, Map jobInfo, String dir, String id)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 286 */       DataBinder binder = ResourceUtils.readDataBinder(dir, id + ".hda");
/* 287 */       jState.init(binder, jobInfo);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 292 */       String errMsg = LocaleUtils.encodeMessage("csSjUnableToReadJobDef", null, id, dir);
/* 293 */       jState.setErrorState(e, errMsg, true);
/*     */ 
/* 296 */       jState.m_data = new DataBinder();
/* 297 */       DataBinder.mergeHashTables(jState.m_data.getLocalData(), jobInfo);
/*     */ 
/* 300 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean checkIfShouldProcess(JobState jState, Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 311 */     if (jState.m_isInError)
/*     */     {
/* 313 */       return false;
/*     */     }
/*     */ 
/* 316 */     String state = jState.m_data.getLocal("dSjState");
/* 317 */     if (!state.equals("I"))
/*     */     {
/* 321 */       return false;
/*     */     }
/*     */ 
/* 325 */     jState.m_data.setFieldType("dSjProcessTs", "date");
/* 326 */     long curTime = System.currentTimeMillis();
/* 327 */     Date curDte = new Date(curTime);
/* 328 */     String curDteStr = LocaleUtils.formatODBC(curDte);
/* 329 */     jState.m_data.putLocal("dSjProcessTs", curDteStr);
/* 330 */     jState.m_data.putLocal("dSjEndUser", jState.m_data.getLocal("dSjInitUser"));
/*     */ 
/* 333 */     boolean retVal = false;
/* 334 */     String name = jState.m_data.getLocal("dSjName");
/* 335 */     String type = jState.m_data.getLocal("dSjType");
/* 336 */     if (type.equals("I"))
/*     */     {
/* 339 */       retVal = true;
/*     */     }
/*     */     else
/*     */     {
/* 343 */       DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/* 344 */       PageMerger pageMerger = new PageMerger(binder, cxt);
/*     */ 
/* 346 */       String intervalStr = jState.m_data.getLocal("dSjInterval");
/* 347 */       int intervalInMinutes = NumberUtils.parseTypedInteger(intervalStr, 1, 19, 20);
/*     */ 
/* 349 */       long intervalMillis = intervalInMinutes * 60 * 1000L;
/*     */ 
/* 351 */       Date prevDte = determineStartTime(jState, pageMerger);
/* 352 */       long prevTime = prevDte.getTime();
/*     */ 
/* 354 */       String script = jState.m_data.getLocal("sjActivationScript");
/*     */ 
/* 360 */       if ((script != null) && (script.length() > 0))
/*     */       {
/* 362 */         if (pageMerger.m_isReportErrorStack)
/*     */         {
/* 364 */           String msg = LocaleUtils.encodeMessage("csDynHTMLEvalVariableInMethod", null, "ScheduledJobsManager.checkIfShouldProcess", name);
/*     */ 
/* 366 */           pageMerger.pushStackMessage(msg);
/*     */         }
/*     */         try
/*     */         {
/* 370 */           DataBinder.mergeHashTables(binder.getLocalData(), jState.m_data.getLocalData());
/* 371 */           binder.putLocal("prevTime", "" + prevTime);
/* 372 */           binder.putLocal("curTime", "" + curTime);
/* 373 */           binder.putLocal("intervalMillis", "" + intervalMillis);
/*     */ 
/* 375 */           pageMerger.evaluateScript(script);
/*     */ 
/* 377 */           if (SystemUtils.m_verbose)
/*     */           {
/* 379 */             Properties props = binder.getLocalData();
/* 380 */             Report.debug("scheduledjobs", "ScheduledJobsManager.checkIfShouldProcess:" + name + "  Local data: " + props, null);
/*     */           }
/*     */ 
/*     */         }
/*     */         catch (IOException e)
/*     */         {
/*     */         }
/*     */         finally
/*     */         {
/* 390 */           if (pageMerger.m_isReportErrorStack)
/*     */           {
/* 392 */             pageMerger.popStack();
/*     */           }
/* 394 */           pageMerger.releaseAllTemporary();
/*     */         }
/* 396 */         retVal = DataBinderUtils.getBoolean(binder, "doEvent", false);
/*     */       }
/* 398 */       if ((retVal) || (script == null) || (script.length() == 0))
/*     */       {
/* 400 */         if (intervalMillis != 0L)
/*     */         {
/* 402 */           long prevCount = prevTime / intervalMillis;
/* 403 */           long curCount = curTime / intervalMillis;
/* 404 */           if (prevCount != curCount)
/*     */           {
/* 406 */             retVal = true;
/*     */           }
/*     */           else
/*     */           {
/* 410 */             retVal = false;
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 415 */           retVal = true;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 420 */     jState.m_data.putLocal("dSjLastProcessedTs", curDteStr);
/*     */ 
/* 422 */     Report.trace("scheduledjobs", "checkIfShouldProcess: job=" + name + "  retVal=" + retVal, null);
/*     */ 
/* 424 */     jState.reportProgress(3, "checkIfShouldProcess: job=" + name + "  retVal=" + retVal, null);
/*     */ 
/* 426 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected Date determineStartTime(JobState jState, PageMerger pageMerger)
/*     */   {
/* 431 */     Date startDte = null;
/* 432 */     String dateToken = null;
/*     */     try
/*     */     {
/* 435 */       dateToken = jState.m_data.getLocal("dSjStartToken");
/* 436 */       if ((dateToken != null) && (dateToken.length() > 0))
/*     */       {
/* 438 */         if (!dateToken.startsWith("@"))
/*     */         {
/* 440 */           startDte = jState.m_data.parseDate("dSjCreateTs", dateToken);
/*     */         }
/*     */         else
/*     */         {
/* 444 */           dateToken = dateToken.substring(1);
/* 445 */           String str = pageMerger.computeValue(dateToken, false).toString();
/* 446 */           startDte = jState.m_data.parseDate("dSjCreateTs", str);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 451 */         String processStr = jState.m_data.getLocal("dSjLastProcessedTs");
/* 452 */         if ((processStr != null) && (processStr.length() > 0))
/*     */         {
/* 454 */           startDte = jState.m_data.parseDate("dSjLastProcessedTs", processStr);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 461 */       if (startDte == null)
/*     */       {
/* 463 */         String createStr = jState.m_data.getLocal("dSjCreateTs");
/* 464 */         startDte = jState.m_data.parseDate("dSjCreateTs", createStr);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 469 */       startDte = new Date();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 473 */       Report.trace("scheduledjobs", "Error in job " + jState.m_data.getLocal("dSjName") + " unable to evaluate the start token script dSjStartToken= " + dateToken, e);
/*     */ 
/* 475 */       jState.reportProgress(3, "Error in job " + jState.m_data.getLocal("dSjName") + " unable to evaluate the start token script dSjStartToken= " + dateToken, e);
/*     */     }
/*     */ 
/* 479 */     return startDte;
/*     */   }
/*     */ 
/*     */   public void doCleanUp(JobState jState, ScheduledJobStorage jStorage, ScheduledJobImplementor jobImp, Workspace ws, ExecutionContext cxt)
/*     */   {
/* 485 */     Service service = null;
/*     */     try
/*     */     {
/* 488 */       if (jobImp != null)
/*     */       {
/* 490 */         finishJob(jState, jobImp, ws, cxt);
/* 491 */         ProgressStateUtils.reportProgress(jState.m_globalProgress, null, "scheduledjobs", 3, null, "csSjDoCleanup", new Object[0]);
/*     */       }
/*     */ 
/* 495 */       Date dte = new Date(System.currentTimeMillis());
/* 496 */       if (!jState.m_hasWork)
/*     */       {
/* 499 */         if (jState.m_isFatalError)
/*     */         {
/* 501 */           DataBinder binder = jState.m_data;
/*     */ 
/* 503 */           binder.putLocal("dSjState", "E");
/* 504 */           binder.putLocal("dSjLastProcessedStatus", "F");
/*     */ 
/* 506 */           if (jState.m_errorMsg == null)
/*     */           {
/* 508 */             jState.m_errorMsg = "!csSjUnknownError";
/*     */           }
/* 510 */           binder.putLocal("dSjProgress", jState.m_errorMsg);
/* 511 */           binder.putLocal("dSjMessage", jState.m_errorMsg);
/*     */ 
/* 513 */           ws.execute("UscheduledJobProgressState", binder);
/* 514 */           ScheduledJobUtils.addJobHistoryEvent(binder, ws, cxt);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 519 */         String status = "S";
/* 520 */         if ((jState.m_isInError) || (jState.m_isFatalError))
/*     */         {
/* 522 */           status = "F";
/*     */         }
/*     */ 
/* 525 */         DataBinder binder = jState.m_data;
/* 526 */         binder.putLocal("dSjLastProcessedTs", LocaleUtils.formatODBC(dte));
/* 527 */         binder.putLocal("dSjLastProcessedStatus", status);
/* 528 */         binder.putLocal("dSjProgress", jState.getLastProgress());
/* 529 */         binder.putLocal("dSjState", "I");
/*     */ 
/* 531 */         service = ScheduledJobUtils.initService("SJ_UPDATE_AFTER_PROCESSING", binder, ws, jState, jobImp);
/*     */ 
/* 533 */         binder.setEnvironmentValue("REMOTE_USER", "sysadmin");
/* 534 */         service.doRequestInternal();
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 539 */       Report.trace("schedulejobs", "Unable to update state job information for dSjName=" + jState.m_data.getLocal("dSjName"), t);
/*     */ 
/* 541 */       jState.reportProgress(3, "Unable to update state job information for dSjName=" + jState.m_data.getLocal("dSjName"), t);
/*     */     }
/*     */     finally
/*     */     {
/* 547 */       if (service != null)
/*     */       {
/* 549 */         service.clear();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void finishJob(JobState jState, ScheduledJobImplementor jobImp, Workspace ws, ExecutionContext cxt)
/*     */   {
/* 558 */     jState.m_isFinished = true;
/*     */     try
/*     */     {
/* 561 */       jobImp.finishJob();
/*     */ 
/* 564 */       Map args = new HashMap();
/* 565 */       args.put("isCheckAbort", "0");
/* 566 */       args.put("isFinished", "1");
/*     */ 
/* 569 */       IdcMessage idcMsg = IdcMessageFactory.lc("csSjFinalProgress", new Object[] { Integer.valueOf(jState.m_workCount), Integer.valueOf(jState.m_totalCount) });
/* 570 */       jState.updateCounters(jState.m_workCount, jState.m_totalCount);
/* 571 */       jState.updateAndCheckProgress(2, idcMsg, jState.m_data, args, ws, cxt);
/*     */ 
/* 574 */       if (jState.m_workCount > 0)
/*     */       {
/* 576 */         String subjectStr = ScheduledJobUtils.getConfigValue("sjNotifiedSubjects", jState.m_orgData, "", false);
/*     */ 
/* 578 */         List subjects = StringUtils.makeListFromSequenceSimple(subjectStr);
/* 579 */         int count = subjects.size();
/* 580 */         for (int i = 0; i < count; ++i)
/*     */         {
/* 582 */           String subject = (String)subjects.get(i);
/* 583 */           SubjectManager.notifyChanged(subject);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 590 */       Report.trace("scheduledjobs", "Unable to cancel a completed job.", e);
/* 591 */       jState.reportProgress(3, "Unable to cancel a completed job.", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 598 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99284 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.ScheduledJobsProcessor
 * JD-Core Version:    0.5.4
 */