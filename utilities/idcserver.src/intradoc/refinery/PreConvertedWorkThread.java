/*     */ package intradoc.refinery;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.server.QueueProcessor;
/*     */ import intradoc.server.SubjectEventMonitor;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class PreConvertedWorkThread
/*     */ {
/*  51 */   protected long m_queueWatchRestTime = 300000L;
/*  52 */   protected long m_sleepCount = 60L;
/*  53 */   public boolean m_needCheck = true;
/*     */ 
/*  56 */   protected Properties m_curDocProps = null;
/*     */ 
/*  58 */   protected ExecutionContext m_refineryQueueContext = new ExecutionContextAdaptor();
/*     */ 
/*  60 */   protected boolean m_curItemSuccess = false;
/*     */ 
/*  63 */   public boolean m_isExiting = false;
/*     */ 
/*  66 */   public boolean m_isBgInit = false;
/*     */ 
/*  69 */   public boolean m_noRetry = false;
/*     */   protected TransferManager m_transferManager;
/*     */   protected Workspace m_workspace;
/*     */ 
/*     */   public PreConvertedWorkThread(ExecutionContext refineryQueueContext)
/*     */   {
/*  76 */     this.m_refineryQueueContext = refineryQueueContext;
/*  77 */     this.m_workspace = ((Workspace)refineryQueueContext.getCachedObject("Workspace"));
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  82 */     this.m_transferManager = new TransferManager(this.m_refineryQueueContext);
/*  83 */     this.m_transferManager.init();
/*  84 */     RefineryProviderManager.buildRefineryProviderData(this.m_transferManager);
/*  85 */     this.m_isBgInit = false;
/*  86 */     this.m_curDocProps = new Properties();
/*  87 */     this.m_queueWatchRestTime = SharedObjects.getTypedEnvironmentInt("RefineryNewJobMonitorMaxIntervalInSec", 120000, 18, 24);
/*     */ 
/*  90 */     this.m_sleepCount = (this.m_queueWatchRestTime / 5000L);
/*     */ 
/*  92 */     this.m_noRetry = SharedObjects.getEnvValueAsBoolean("RefineryNotRetryAfterFailedSubmission", false);
/*     */   }
/*     */ 
/*     */   public void startQueueMonitor()
/*     */   {
/*  97 */     if (this.m_isBgInit)
/*     */       return;
/*  99 */     this.m_isBgInit = true;
/*     */ 
/* 101 */     Runnable runProcessDocs = new Runnable()
/*     */     {
/*     */       public void run()
/*     */       {
/* 105 */         SubjectEventMonitor monitor = new SubjectEventMonitor()
/*     */         {
/*     */           public boolean checkForChange(String subject, long time)
/*     */           {
/* 109 */             return false;
/*     */           }
/*     */ 
/*     */           public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*     */           {
/* 115 */             PreConvertedWorkThread.this.m_needCheck = true;
/*     */           }
/*     */         };
/* 118 */         SubjectManager.addSubjectMonitor("refineryjob", monitor);
/* 119 */         PreConvertedWorkThread.this.monitorQueue();
/*     */       }
/*     */     };
/* 122 */     Thread processThread = new Thread(runProcessDocs);
/* 123 */     processThread.setName("Refinery MonitorDocsToTransfer");
/* 124 */     processThread.setDaemon(true);
/* 125 */     processThread.start();
/*     */   }
/*     */ 
/*     */   public void startRefineryStatusThread()
/*     */   {
/* 131 */     long statusSleepTime = SharedObjects.getTypedEnvironmentInt("RefineryUpdateStatusSeconds", 15000, 18, 24);
/*     */ 
/* 134 */     Runnable runUpdateIBRStatus = new Runnable(statusSleepTime)
/*     */     {
/*     */       public void run()
/*     */       {
/* 138 */         while ((!PreConvertedWorkThread.this.m_isExiting) && (!SystemUtils.m_isServerStopped))
/*     */         {
/*     */           try
/*     */           {
/* 142 */             PreConvertedWorkThread.this.updateProviderStatus();
/*     */           }
/*     */           catch (Exception ex)
/*     */           {
/* 146 */             Report.debug("ibrsupport", null, ex);
/*     */           }
/* 148 */           SystemUtils.sleep(this.val$statusSleepTime);
/*     */         }
/*     */       }
/*     */     };
/* 152 */     Thread processThread = new Thread(runUpdateIBRStatus);
/* 153 */     processThread.setName("Refinery MonitorIBRProvider");
/* 154 */     processThread.setDaemon(true);
/* 155 */     processThread.start();
/*     */   }
/*     */ 
/*     */   public void monitorQueue()
/*     */   {
/* 160 */     while ((!this.m_isExiting) && (!SystemUtils.m_isServerStopped))
/*     */     {
/* 162 */       int count = 0;
/*     */ 
/* 164 */       while ((!this.m_needCheck) && (count < this.m_sleepCount) && (!RefineryUtils.isForceCheck()) && (!SystemUtils.m_isServerStopped))
/*     */       {
/* 166 */         SystemUtils.sleep(5000L);
/* 167 */         ++count;
/*     */       }
/* 169 */       this.m_needCheck = false;
/*     */ 
/* 171 */       boolean inTransitState = false;
/* 172 */       Exception exception = null;
/* 173 */       boolean hasFailed = false;
/*     */       try
/*     */       {
/* 176 */         String failureStatus = "Retry";
/* 177 */         if (this.m_noRetry)
/*     */         {
/* 179 */           failureStatus = "Failed";
/*     */         }
/* 181 */         boolean isLoaded = true;
/* 182 */         boolean hasNewJob = true;
/*     */         do { while (true) { if ((!isLoaded) || ((this.m_noRetry) && (!hasNewJob)) || (SystemUtils.m_isServerStopped))
/*     */               break label203;
/* 185 */             if (this.m_isExiting) {
/*     */               break label203;
/*     */             }
/*     */ 
/* 189 */             if (this.m_curDocProps != null)
/*     */             {
/* 191 */               this.m_curDocProps.clear();
/*     */             }
/*     */ 
/* 196 */             if (!hasNewJob)
/*     */               break;
/* 198 */             hasNewJob = loadNextConversionJob(true);
/* 199 */             if (hasNewJob)
/*     */             {
/*     */               break label164;
/*     */             }
/*     */  }
/*     */ 
/*     */ 
/* 206 */           isLoaded = loadNextConversionJob(false);
/*     */ 
/* 208 */           label164: if (!isLoaded)
/*     */           {
/*     */             break;
/*     */           }
/*     */ 
/* 213 */           inTransitState = true;
/* 214 */           loadAdditionalParameters();
/*     */ 
/* 216 */           handleCurrentQueueItem();
/*     */ 
/* 218 */           afterQueueItemHandled("Submitted", failureStatus);
/* 219 */           label203: inTransitState = false; }
/*     */ 
/* 221 */         while (!this.m_isExiting);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 229 */         Report.trace("ibrsupport", null, err);
/* 230 */         exception = err;
/*     */       }
/*     */       finally
/*     */       {
/* 234 */         if (inTransitState)
/*     */         {
/*     */           try
/*     */           {
/* 238 */             if (exception != null)
/*     */             {
/* 240 */               String jobID = this.m_curDocProps.getProperty("dConvJobID");
/* 241 */               String dID = this.m_curDocProps.getProperty("dID");
/* 242 */               String msg = LocaleUtils.encodeMessage("csRefineryUnableToSubmitJob", exception.getMessage(), jobID, dID);
/* 243 */               this.m_curDocProps.put("dConvMessage", msg);
/*     */             }
/* 245 */             afterQueueItemHandled("Submitted", "Failed");
/* 246 */             hasFailed = true;
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 251 */             Report.trace(null, "Unable to move document out of transient 'Preparing' conversion state, database connection might be lost. (" + this.m_curDocProps + ")", e);
/*     */           }
/*     */         }
/*     */ 
/* 255 */         if (hasFailed)
/*     */         {
/* 257 */           QueueProcessor.setRefineryJobChanged();
/*     */         }
/* 259 */         this.m_workspace.releaseConnection();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void updateProviderStatus()
/*     */     throws DataException
/*     */   {
/* 267 */     long curTime = System.currentTimeMillis();
/* 268 */     boolean sortRefineries = false;
/* 269 */     for (Iterator iter = RefineryProviderManager.m_refineryDataMap.entrySet().iterator(); iter.hasNext(); )
/*     */     {
/* 271 */       Map.Entry entry = (Map.Entry)iter.next();
/* 272 */       String name = (String)entry.getKey();
/* 273 */       RefineryStatus rstat = (RefineryStatus)entry.getValue();
/* 274 */       if (rstat.m_nextUpdate < curTime)
/*     */       {
/* 276 */         boolean isSortNeeded = RefineryProviderManager.updateProviderStateByName(name, this.m_transferManager);
/* 277 */         if (isSortNeeded == true)
/*     */         {
/* 279 */           sortRefineries = true;
/*     */         }
/*     */       }
/*     */     }
/* 283 */     if (!sortRefineries)
/*     */       return;
/* 285 */     RefineryProviderManager.sortRefineryLoadRS();
/*     */   }
/*     */ 
/*     */   protected boolean loadNextConversionJob(boolean checkOnlyNewJobs)
/*     */     throws DataException
/*     */   {
/* 293 */     String query = "QconversionNextJob";
/* 294 */     DataBinder binder = null;
/* 295 */     if (!checkOnlyNewJobs)
/*     */     {
/* 297 */       query = "QconversionNextRetryJob";
/*     */ 
/* 299 */       long time = System.currentTimeMillis();
/* 300 */       if (!RefineryUtils.isForceCheck())
/*     */       {
/* 303 */         time -= this.m_queueWatchRestTime;
/*     */       }
/*     */       else
/*     */       {
/* 307 */         time = RefineryUtils.m_forceCheckInitTime;
/*     */       }
/* 309 */       Date d = new Date(time);
/* 310 */       binder = new DataBinder();
/* 311 */       binder.putLocalDate("latestRetryTime", d);
/*     */     }
/* 313 */     this.m_curDocProps = RefineryUtils.getNextWork(this.m_workspace, binder, query, "Preparing");
/* 314 */     if (this.m_curDocProps != null)
/*     */     {
/* 316 */       return true;
/*     */     }
/* 318 */     if ((!checkOnlyNewJobs) && (RefineryUtils.isForceCheck()))
/*     */     {
/* 320 */       RefineryUtils.turnOffForcedCheck();
/*     */     }
/* 322 */     return false;
/*     */   }
/*     */ 
/*     */   protected void loadAdditionalParameters()
/*     */     throws DataException
/*     */   {
/* 331 */     DataBinder convBinder = new DataBinder();
/* 332 */     RefineryUtils.buildCurrentDocData(this.m_workspace, convBinder, this.m_curDocProps);
/*     */ 
/* 335 */     int thumbnailWidth = SharedObjects.getEnvironmentInt("ThumbnailWidth", 100);
/* 336 */     int thumbnailHeight = SharedObjects.getEnvironmentInt("ThumbnailHeight", 100);
/* 337 */     convBinder.putLocal("ThumbnailWidth", Integer.toString(thumbnailWidth));
/* 338 */     convBinder.putLocal("ThumbnailHeight", Integer.toString(thumbnailHeight));
/*     */ 
/* 341 */     String extraParams = SharedObjects.getEnvironmentValue("ConversionExtraParameters");
/* 342 */     Vector v = null;
/* 343 */     if (extraParams != null)
/*     */     {
/* 345 */       v = StringUtils.parseArray(extraParams, ',', ',');
/*     */     }
/*     */     else
/*     */     {
/* 349 */       v = new IdcVector();
/*     */     }
/*     */ 
/* 352 */     ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/* 353 */     cxt.setParentContext(this.m_refineryQueueContext);
/* 354 */     String docID = convBinder.getLocal("dDocID");
/* 355 */     Object[] o = { v, docID, this.m_curDocProps };
/* 356 */     cxt.setCachedObject("preWebFileCreation:parameters", o);
/*     */     try
/*     */     {
/* 359 */       int ret = PluginFilters.filter("preWebfileCreation", this.m_workspace, convBinder, cxt);
/* 360 */       if (ret == -1)
/*     */       {
/* 362 */         String errMsg = LocaleResources.localizeMessage("!csFilterError,preWebfileCreation", null);
/* 363 */         throw new DataException(errMsg);
/*     */       }
/* 365 */       v = (Vector)o[0];
/* 366 */       docID = (String)o[1];
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 370 */       String errMsg = LocaleUtils.encodeMessage("csFilterError", e.getMessage(), "preWebfileCreation");
/*     */ 
/* 372 */       throw new DataException(errMsg);
/*     */     }
/* 374 */     String[] extraPropsToCopy = new String[v.size()];
/* 375 */     v.copyInto(extraPropsToCopy);
/*     */ 
/* 377 */     this.m_curDocProps = convBinder.getLocalData();
/*     */   }
/*     */ 
/*     */   protected void handleCurrentQueueItem() throws DataException
/*     */   {
/* 382 */     this.m_curItemSuccess = false;
/* 383 */     Provider provSubmit = null;
/*     */     try
/*     */     {
/* 386 */       provSubmit = this.m_transferManager.findProviderForWork(this.m_curDocProps);
/* 387 */       if (provSubmit != null)
/*     */       {
/* 389 */         this.m_curItemSuccess = this.m_transferManager.submitConversionToRefinery(provSubmit, this.m_curDocProps);
/*     */ 
/* 391 */         this.m_curDocProps.setProperty("dConvMessage", "");
/*     */       }
/*     */       else
/*     */       {
/* 395 */         this.m_curDocProps.setProperty("dConvMessage", "!csRefineryNoValidProviderFound");
/*     */       }
/*     */     }
/*     */     catch (ServiceException noTransfer)
/*     */     {
/* 400 */       Report.trace("ibrsupport", null, noTransfer);
/*     */     }
/*     */ 
/* 403 */     if (!this.m_curItemSuccess)
/*     */       return;
/* 405 */     String docID = this.m_curDocProps.getProperty("dDocID");
/* 406 */     String dDocName = this.m_curDocProps.getProperty("dDocName");
/* 407 */     String dConvJobID = this.m_curDocProps.getProperty("dConvJobID");
/* 408 */     String convType = this.m_curDocProps.getProperty("dConversion");
/* 409 */     String provName = provSubmit.getName();
/* 410 */     Report.trace("ibrsupport", "The '" + provName + "' provider accepted Job: " + dConvJobID + " ContentID: " + dDocName + " dDocID: " + docID + " Type: " + convType + " for conversion", null);
/*     */   }
/*     */ 
/*     */   protected String afterQueueItemHandled(String successState, String failureState)
/*     */     throws DataException
/*     */   {
/* 419 */     String dConversionState = successState;
/* 420 */     if (!this.m_curItemSuccess)
/*     */     {
/* 422 */       dConversionState = failureState;
/*     */     }
/* 424 */     String msg = this.m_curDocProps.getProperty("dConvMessage");
/* 425 */     String jobID = this.m_curDocProps.getProperty("dConvJobID");
/* 426 */     String prov = this.m_curDocProps.getProperty("dConvProvider");
/* 427 */     if (prov != null)
/*     */     {
/* 429 */       DataBinder binder = new DataBinder();
/* 430 */       binder.setLocalData(this.m_curDocProps);
/* 431 */       this.m_workspace.execute("UrefineryJobProvider", binder);
/*     */     }
/* 433 */     RefineryUtils.updateConversionJobState(this.m_workspace, jobID, "Preparing", dConversionState, msg);
/* 434 */     return dConversionState;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 439 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104383 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.PreConvertedWorkThread
 * JD-Core Version:    0.5.4
 */