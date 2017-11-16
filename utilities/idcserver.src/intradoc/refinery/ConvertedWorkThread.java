/*     */ package intradoc.refinery;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.QueueProcessor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class ConvertedWorkThread
/*     */ {
/*     */   protected Workspace m_workspace;
/*     */   protected TransferManager m_transferManager;
/*     */   protected static boolean m_needCheckAbortedJobs;
/*     */ 
/*     */   public void init(ExecutionContext refineryQueueContext)
/*     */     throws ServiceException
/*     */   {
/*  58 */     this.m_workspace = ((Workspace)refineryQueueContext.getCachedObject("Workspace"));
/*  59 */     this.m_transferManager = new TransferManager(refineryQueueContext);
/*  60 */     this.m_transferManager.init();
/*     */   }
/*     */ 
/*     */   public void startCheckRefineryWorkThread()
/*     */   {
/*  65 */     Runnable monitorForConvertedWork = new Runnable()
/*     */     {
/*     */       public void run()
/*     */       {
/*  69 */         ConvertedWorkThread.this.monitorForConvertedJobs();
/*     */       }
/*     */     };
/*  72 */     Thread monitorConvertedWorkThread = new Thread(monitorForConvertedWork);
/*  73 */     monitorConvertedWorkThread.setName("Refinery QueryRefineryJobs");
/*  74 */     monitorConvertedWorkThread.setDaemon(true);
/*  75 */     SystemUtils.registerSynchronizationObjectToNotifyOnStop(monitorConvertedWorkThread);
/*  76 */     monitorConvertedWorkThread.start();
/*     */   }
/*     */ 
/*     */   public static void enableCheckForAbortedJobs()
/*     */   {
/*  81 */     m_needCheckAbortedJobs = true;
/*     */   }
/*     */ 
/*     */   protected void monitorForConvertedJobs()
/*     */   {
/*  86 */     long secondsBetweenCheckForJobs = SharedObjects.getTypedEnvironmentInt("RefineryJobSubmittedCheckIdleSecs", 5000, 18, 24);
/*     */ 
/*  90 */     m_needCheckAbortedJobs = true;
/*  91 */     int provLastChecked = -1;
/*  92 */     int numToCheck = RefineryProviderManager.m_numRefProviders;
/*  93 */     IdcVector refProviders = new IdcVector(numToCheck);
/*  94 */     for (Iterator iter = RefineryProviderManager.m_refineryDataMap.entrySet().iterator(); iter.hasNext(); )
/*     */     {
/*  96 */       Map.Entry entry = (Map.Entry)iter.next();
/*  97 */       RefineryStatus rstat = (RefineryStatus)entry.getValue();
/*  98 */       refProviders.add(rstat);
/*     */     }
/*     */ 
/* 101 */     while (!SystemUtils.m_isServerStopped)
/*     */     {
/* 103 */       boolean jobWaiting = false;
/* 104 */       RefineryStatus rstat = null;
/* 105 */       int numConvertedJobs = 0;
/* 106 */       while ((!jobWaiting) && (!SystemUtils.m_isServerStopped))
/*     */       {
/* 108 */         if (m_needCheckAbortedJobs)
/*     */         {
/* 110 */           m_needCheckAbortedJobs = false;
/* 111 */           cleanupAbortedJobs();
/*     */         }
/* 113 */         int iter = provLastChecked;
/* 114 */         for (int tries = 0; tries < numToCheck; ++tries)
/*     */         {
/* 116 */           ++iter;
/* 117 */           if (iter >= numToCheck)
/*     */           {
/* 119 */             iter = 0;
/*     */           }
/* 121 */           rstat = (RefineryStatus)refProviders.elementAt(iter);
/* 122 */           if (rstat.m_postConvertedJobs != null)
/*     */           {
/* 124 */             numConvertedJobs = rstat.m_postConvertedJobs.getNumRows();
/* 125 */             if ((numConvertedJobs != -1) && (numConvertedJobs > 0))
/*     */             {
/* 127 */               jobWaiting = true;
/* 128 */               provLastChecked = iter;
/* 129 */               break;
/*     */             }
/*     */           }
/* 132 */           if (SystemUtils.m_isServerStopped) {
/*     */             break;
/*     */           }
/*     */         }
/*     */ 
/* 137 */         if (SystemUtils.m_isServerStopped) {
/*     */           break;
/*     */         }
/*     */ 
/* 141 */         if (!jobWaiting)
/*     */         {
/* 143 */           SystemUtils.sleep(secondsBetweenCheckForJobs);
/*     */         }
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 149 */         if (!SystemUtils.m_isServerStopped)
/*     */         {
/* 151 */           processConverterJobsStatusTable(rstat);
/*     */         }
/*     */       }
/*     */       catch (Exception err)
/*     */       {
/* 156 */         String dConvProvider = rstat.m_name;
/* 157 */         IdcMessage msg = IdcMessageFactory.lc("csRefineryConvertedJobUnexpectedError", new Object[] { dConvProvider });
/* 158 */         Report.error(null, msg.toString(), err);
/*     */       }
/*     */       finally
/*     */       {
/* 162 */         this.m_workspace.releaseConnection();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void processConverterJobsStatusTable(RefineryStatus rstat) throws DataException, ServiceException
/*     */   {
/* 169 */     Provider prov = rstat.m_prov;
/* 170 */     String dConvProvider = prov.getName();
/* 171 */     boolean jobAborted = false;
/* 172 */     boolean hasProcessed = false;
/*     */ 
/* 183 */     DataResultSet convertedJobs = new DataResultSet();
/* 184 */     convertedJobs.copy(rstat.m_postConvertedJobs);
/* 185 */     int numJobs = convertedJobs.getNumRows();
/* 186 */     Report.trace("ibrstatus", "IBR '" + rstat.m_name + "' has jobs waiting: " + numJobs, null);
/* 187 */     for (convertedJobs.first(); convertedJobs.isRowPresent(); convertedJobs.next())
/*     */     {
/* 189 */       if (SystemUtils.m_isServerStopped) {
/*     */         break;
/*     */       }
/*     */ 
/* 193 */       Map row = convertedJobs.getCurrentRowMap();
/* 194 */       String dConvJobID = (String)row.get("dConvJobID");
/* 195 */       Report.trace("ibrsupport", "starting retrieval of job: " + dConvJobID + "; from " + rstat.m_name, null);
/*     */ 
/* 197 */       boolean canRetrieve = RefineryUtils.forceUpdateConversionJobState(this.m_workspace, dConvJobID, "Processing");
/* 198 */       if (canRetrieve)
/*     */       {
/* 201 */         Report.trace("ibrsupport", "Validating provider " + dConvProvider + " to get conversion of job " + dConvJobID, null);
/* 202 */         if ((prov == null) || (!prov.isEnabled()))
/*     */         {
/* 205 */           IdcMessage msg = null;
/* 206 */           if (prov == null)
/*     */           {
/* 209 */             msg = IdcMessageFactory.lc("csRefineryTransferConvertedJobNoProvider", new Object[] { dConvJobID, dConvProvider });
/*     */           }
/* 211 */           else if (!prov.isEnabled())
/*     */           {
/* 216 */             msg = IdcMessageFactory.lc("csRefineryTransferConvertedJobProviderNotEnabled", new Object[] { dConvJobID, dConvProvider });
/*     */           }
/* 218 */           Report.error(null, msg.toString(), null);
/*     */ 
/* 220 */           RefineryUtils.updateConversionJobState(this.m_workspace, dConvJobID, "Processing", "Failed", msg.toString());
/*     */         }
/* 222 */         else if (prov.checkState("IsBadConnection", false) == true)
/*     */         {
/* 224 */           Report.trace("ibrsupport", "Provider " + dConvProvider + " had bad connection. Unable to retrieve work.", null);
/* 225 */           RefineryUtils.updateConversionJobState(this.m_workspace, dConvJobID, "Processing", "Failed", null);
/*     */         }
/*     */         else
/*     */         {
/* 229 */           DataBinder processedBinder = retieveConvertedWork(prov, dConvJobID);
/* 230 */           String dConversionState = processedBinder.getLocal("dConversionState");
/* 231 */           hasProcessed = true;
/* 232 */           RefineryUtils.updateConversionJobState(this.m_workspace, dConvJobID, "Processing", dConversionState, null);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 237 */         Properties props = new Properties();
/* 238 */         props.put("dConvJobID", dConvJobID);
/* 239 */         props.put("dConvProvider", prov.getName());
/*     */ 
/* 241 */         DataResultSet currJobData = RefineryProviderManager.doQueryGetResults("QconversionCurrentJob", props);
/* 242 */         boolean isJobValid = currJobData.getNumRows() == 1;
/* 243 */         Report.trace("ibrsupport", "Job " + dConvJobID + " failed to move to 'Processing' state; is valid job: " + isJobValid, null);
/* 244 */         if (!isJobValid)
/*     */         {
/* 246 */           boolean markedAborted = RefineryUtils.forceUpdateConversionJobState(this.m_workspace, dConvJobID, "Aborted");
/* 247 */           if (!markedAborted)
/*     */           {
/* 250 */             processAbortedJob(prov, props);
/* 251 */             jobAborted = true;
/*     */           }
/*     */           else
/*     */           {
/* 255 */             m_needCheckAbortedJobs = true;
/*     */           }
/*     */         }
/*     */       }
/* 259 */       if (dConvJobID == null)
/*     */         continue;
/* 261 */       rstat.removeJobFromConvertedJobs(dConvJobID);
/*     */     }
/*     */ 
/* 265 */     Report.trace("ibrsupport", "IBR '" + rstat.m_name + "' processed jobs: " + numJobs + "; transfered jobs: " + hasProcessed + "; had aborted: " + jobAborted, null);
/* 266 */     if (jobAborted)
/*     */     {
/* 268 */       this.m_workspace.execute("DrefineryFinishedJobCleanUp", null);
/*     */     }
/* 270 */     if (!hasProcessed)
/*     */       return;
/* 272 */     QueueProcessor.setRefineryJobChanged();
/*     */   }
/*     */ 
/*     */   protected DataBinder retieveConvertedWork(Provider provWithWork, String dConvJobID)
/*     */     throws DataException, ServiceException
/*     */   {
/* 279 */     Report.trace("ibrsupport", "Getting job: " + dConvJobID + "; from: " + provWithWork.getName(), null);
/*     */ 
/* 281 */     DataBinder results = this.m_transferManager.pullConversionOver(provWithWork, dConvJobID);
/* 282 */     String statusCodeStr = results.getLocal("StatusCode");
/* 283 */     if (statusCodeStr != null)
/*     */     {
/* 285 */       int statusCode = Integer.parseInt(statusCodeStr);
/* 286 */       if (statusCode < 0)
/*     */       {
/* 288 */         String statusMsg = results.getLocal("StatusMessage");
/* 289 */         Report.trace(null, "Refinery provider reported error: " + statusMsg, null);
/* 290 */         ServiceException err = new ServiceException(statusMsg);
/* 291 */         provWithWork.markErrorState(statusCode, err);
/* 292 */         IdcMessage msg = IdcMessageFactory.lc("csRefineryTransferBadPackageError", new Object[] { dConvJobID, provWithWork.getName() });
/* 293 */         Report.error(null, msg.toString(), err);
/* 294 */         results.putLocal("dConversionState", "Failed");
/* 295 */         results.putLocal("dConvMessage", LocaleUtils.encodeMessage(msg));
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 300 */       Properties completedJobProps = results.getLocalData();
/* 301 */       Report.trace("ibrsupport", "Process converted work", null);
/* 302 */       PostConversionActions postActions = new PostConversionActions(results, this.m_workspace);
/*     */       try
/*     */       {
/* 305 */         postActions.init();
/* 306 */         postActions.processRefineryConversionResults();
/* 307 */         completedJobProps = postActions.getConvertedProps();
/*     */       }
/*     */       catch (Throwable jobID)
/*     */       {
/*     */         String jobID;
/*     */         String dir;
/*     */         DataBinder convData;
/* 311 */         String docName = completedJobProps.getProperty("dDocName");
/* 312 */         IdcMessage idcMsg = IdcMessageFactory.lc("csRefineryTransferConvertedJobWebviewableError", new Object[] { cantPlaceWebviewable.getMessage(), docName });
/*     */ 
/* 314 */         String msg = LocaleUtils.encodeMessage(idcMsg);
/* 315 */         Report.error(null, msg, cantPlaceWebviewable);
/* 316 */         results.putLocal("dConversionState", "Failed");
/* 317 */         results.putLocal("dConvMessage", msg);
/*     */       }
/*     */       finally
/*     */       {
/*     */         String jobID;
/*     */         String dir;
/*     */         DataBinder convData;
/* 321 */         if (completedJobProps != null)
/*     */         {
/* 324 */           String jobID = completedJobProps.getProperty("dConvJobID");
/* 325 */           String dir = LegacyDirectoryLocator.getAppDataDirectory() + "refinery/convertedjobs/";
/* 326 */           DataBinder convData = new DataBinder();
/* 327 */           convData.setLocalData(completedJobProps);
/* 328 */           FileUtils.checkOrCreateDirectory(dir, 2);
/* 329 */           ResourceUtils.serializeDataBinder(dir, jobID + ".hda", convData, true, false);
/*     */         }
/* 331 */         postActions.cleanup();
/* 332 */         signOffOnJob(provWithWork, completedJobProps);
/*     */       }
/*     */     }
/* 335 */     return results;
/*     */   }
/*     */ 
/*     */   protected void signOffOnJob(Provider provWithWork, Properties completedJobProps)
/*     */     throws ServiceException, DataException
/*     */   {
/* 341 */     String dConvJobID = completedJobProps.getProperty("dConvJobID");
/* 342 */     String provName = provWithWork.getName();
/* 343 */     Properties xferProps = new Properties();
/* 344 */     xferProps.put("dConvJobID", dConvJobID);
/* 345 */     boolean signedOff = this.m_transferManager.signalRefineryJobComplete(provWithWork, xferProps);
/* 346 */     Report.trace("ibrsupport", "sign off on Job Id " + dConvJobID + " with " + provName + "; successfully: " + signedOff, null);
/*     */   }
/*     */ 
/*     */   protected void cleanupAbortedJobs()
/*     */   {
/*     */     try
/*     */     {
/* 353 */       boolean jobAborted = false;
/* 354 */       DataResultSet abortedJobs = RefineryProviderManager.doQueryGetResults("QrefineryAbortedWork", null);
/* 355 */       Report.trace("ibrsupport", "Number jobs aborted: " + abortedJobs.getNumRows(), null);
/* 356 */       for (abortedJobs.first(); abortedJobs.isRowPresent(); abortedJobs.next())
/*     */       {
/* 358 */         Properties abortedJob = abortedJobs.getCurrentRowProps();
/* 359 */         if (!processAbortedJob(null, abortedJob))
/*     */           continue;
/* 361 */         jobAborted = true;
/*     */       }
/*     */ 
/* 364 */       if (jobAborted)
/*     */       {
/* 366 */         this.m_workspace.execute("DrefineryFinishedJobCleanUp", null);
/*     */       }
/*     */     }
/*     */     catch (DataException de)
/*     */     {
/* 371 */       Report.trace("ibrsupport", null, de);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean processAbortedJob(Provider prov, Properties abortedJobs) throws DataException
/*     */   {
/* 377 */     String dConvJobID = abortedJobs.getProperty("dConvJobID");
/* 378 */     String dConvProvider = abortedJobs.getProperty("dConvProvider");
/* 379 */     boolean jobAborted = false;
/*     */ 
/* 381 */     Report.trace("ibrsupport", "aborting job " + dConvJobID + " from Provider '" + dConvProvider + "'", null);
/* 382 */     if (dConvProvider.length() == 0)
/*     */     {
/* 385 */       jobAborted = RefineryUtils.updateConversionJobState(this.m_workspace, dConvJobID, "Aborted", "Finished", null);
/*     */     }
/*     */     else
/*     */     {
/* 389 */       if ((prov == null) && (dConvProvider.length() > 0))
/*     */       {
/* 391 */         prov = Providers.getProvider(dConvProvider);
/*     */       }
/* 393 */       if (prov != null)
/*     */       {
/*     */         try
/*     */         {
/* 397 */           RefineryUtils.updateConversionJobState(this.m_workspace, dConvJobID, "Aborted", "Processing", null);
/* 398 */           DataBinder notifyBinder = this.m_transferManager.notifyRefineryAbortedJob(prov, abortedJobs);
/* 399 */           boolean successAbortSignOff = DataBinderUtils.getBoolean(notifyBinder, "SuccessAbortSignOff", false);
/* 400 */           Report.trace("ibrsupport", "Provider '" + dConvProvider + "' abort signoff status: " + successAbortSignOff, null);
/* 401 */           if (successAbortSignOff)
/*     */           {
/* 403 */             jobAborted = RefineryUtils.updateConversionJobState(this.m_workspace, dConvJobID, "Processing", "Finished", null);
/*     */           }
/*     */           else
/*     */           {
/* 407 */             RefineryUtils.updateConversionJobState(this.m_workspace, dConvJobID, "Processing", "Aborted", null);
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 412 */           jobAborted = false;
/* 413 */           String traceMsg = "Failed to abort conversion job " + dConvJobID;
/* 414 */           Report.trace("ibrsupport", traceMsg, e);
/* 415 */           RefineryUtils.updateConversionJobState(this.m_workspace, dConvJobID, "Processing", "Aborted", null);
/*     */         }
/*     */       }
/*     */     }
/* 419 */     return jobAborted;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 424 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97182 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.ConvertedWorkThread
 * JD-Core Version:    0.5.4
 */