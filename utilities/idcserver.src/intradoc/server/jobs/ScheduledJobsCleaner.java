/*     */ package intradoc.server.jobs;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Date;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ScheduledJobsCleaner
/*     */ {
/*     */   protected Workspace m_workspace;
/*     */   protected Thread m_bgThread;
/*     */   protected String m_lockObject;
/*     */   protected boolean m_isBusy;
/*     */   protected int m_threadWaitTime;
/*     */   protected int m_counter;
/*     */   protected boolean m_isAbort;
/*     */ 
/*     */   public ScheduledJobsCleaner()
/*     */   {
/*  43 */     this.m_lockObject = "lock";
/*     */   }
/*     */ 
/*     */   public void init(Workspace ws)
/*     */   {
/*  53 */     String threadWaitTimeString = SharedObjects.getEnvironmentValue("ScheduledJobsCleanupInterval");
/*  54 */     this.m_threadWaitTime = NumberUtils.parseInteger(threadWaitTimeString, 86400000);
/*  55 */     this.m_workspace = ws;
/*     */ 
/*  57 */     Runnable run = new Object()
/*     */     {
/*     */       public void run()
/*     */       {
/*  61 */         SystemUtils.registerSynchronizationObjectToNotifyOnStop(this);
/*  62 */         while (!SystemUtils.m_isServerStopped)
/*     */         {
/*     */           try
/*     */           {
/*  66 */             synchronized (ScheduledJobsCleaner.this.m_lockObject)
/*     */             {
/*  68 */               SystemUtils.wait(ScheduledJobsCleaner.this.m_lockObject, ScheduledJobsCleaner.this.m_threadWaitTime);
/*     */             }
/*  70 */             if (SystemUtils.m_isServerStopped) {
/*     */               return;
/*     */             }
/*     */ 
/*  74 */             if (ScheduledJobsCleaner.this.m_isBusy)
/*     */             {
/*  76 */               if (ScheduledJobsCleaner.this.m_counter > 30)
/*     */               {
/*  78 */                 ScheduledJobsCleaner.this.m_isAbort = true;
/*  79 */                 Report.trace("scheduledjobs", "The scheduled jobs cleaner thread has been busy and will now be reinitialized.", null);
/*     */               }
/*     */ 
/*  83 */               ScheduledJobsCleaner.this.m_counter += 1;
/*     */             }
/*     */             else
/*     */             {
/*  87 */               ScheduledJobsCleaner.this.cleanActive();
/*     */             }
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/*  92 */             Report.trace("scheduledjobs", t, "csSjCleanerError", new Object[0]);
/*     */           }
/*     */ 
/*  95 */           Providers.releaseConnections();
/*     */         }
/*     */       }
/*     */     };
/* 100 */     this.m_bgThread = new Thread(run, "CleanScheduledJobs");
/* 101 */     this.m_bgThread.setDaemon(true);
/* 102 */     this.m_bgThread.start();
/*     */   }
/*     */ 
/*     */   public void cleanActive()
/*     */     throws DataException, ServiceException
/*     */   {
/* 116 */     this.m_isBusy = true;
/*     */     try
/*     */     {
/* 119 */       ResultSet rset = this.m_workspace.createResultSet("QscheduledJobsActive", null);
/* 120 */       DataResultSet jobSet = new DataResultSet();
/* 121 */       jobSet.copy(rset);
/*     */ 
/* 123 */       ScheduledJobStorage jStorage = ScheduledJobManager.getStorage(this.m_workspace);
/* 124 */       for (jobSet.first(); jobSet.isRowPresent(); jobSet.next())
/*     */       {
/* 126 */         Map jobInfo = jobSet.getCurrentRowMap();
/* 127 */         String id = ScheduledJobUtils.getId(jobInfo);
/* 128 */         String dir = jStorage.getActiveDir();
/* 129 */         String lockName = "sj" + id;
/*     */ 
/* 131 */         if (this.m_isAbort)
/*     */         {
/* 133 */           Report.trace("scheduledjobs", null, "csSjCleanerAbortRequest", new Object[] { id });
/* 134 */           break;
/*     */         }
/*     */ 
/* 137 */         boolean isLocked = FileUtils.reserveLongTermLock(dir, lockName, "ScheduledJobs", 4 * FileUtils.m_touchMonitorInterval, false);
/*     */ 
/* 140 */         if (isLocked) {
/*     */           continue;
/*     */         }
/*     */ 
/* 144 */         unlockJob(jobInfo, dir, lockName);
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 150 */       this.m_isBusy = false;
/* 151 */       this.m_counter = 0;
/* 152 */       this.m_isAbort = false;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void unlockJob(Map jobInfo, String dir, String lockName)
/*     */   {
/* 158 */     boolean isInTran = false;
/*     */     try
/*     */     {
/* 161 */       this.m_workspace.beginTranEx(4);
/* 162 */       isInTran = true;
/*     */ 
/* 165 */       DataBinder binder = new DataBinder();
/* 166 */       DataBinder.mergeHashTables(binder.getLocalData(), jobInfo);
/* 167 */       ResultSet rset = this.m_workspace.createResultSet("QscheduledJob", binder);
/* 168 */       if (rset.isRowPresent())
/*     */       {
/* 170 */         String state = ResultSetUtils.getValue(rset, "dSjState");
/* 171 */         if (state.equals("A"))
/*     */         {
/* 173 */           String processTsString = ResultSetUtils.getValue(rset, "dSjProcessTs");
/* 174 */           if (processTsString.length() == 0)
/*     */           {
/* 176 */             processTsString = ResultSetUtils.getValue(rset, "dSjLastProcessedTs");
/*     */           }
/* 178 */           Date processTs = LocaleUtils.parseODBC(processTsString);
/* 179 */           long processTsInMillis = processTs.getTime();
/* 180 */           long processDurationInMillis = System.currentTimeMillis() - processTsInMillis;
/* 181 */           long processDurationInDays = (processDurationInMillis + 86399999L) / 86400000L;
/* 182 */           long maxDays = SharedObjects.getEnvironmentInt("ScheduledJobsDurationInDaysBeforeCleanup", 7);
/* 183 */           if (processDurationInDays >= maxDays)
/*     */           {
/* 187 */             binder.putLocal("dSjState", "I");
/* 188 */             binder.putLocal("dSjLastProcessedStatus", "C");
/*     */ 
/* 190 */             binder.putLocal("dSjEndUser", "sysadmin");
/* 191 */             String msg = LocaleUtils.encodeMessage("csSjJobCleaned", null);
/*     */ 
/* 193 */             binder.putLocal("dSjProgress", msg);
/* 194 */             binder.putLocal("dSjMessage", msg);
/*     */ 
/* 196 */             this.m_workspace.execute("UscheduledJobProgressState", binder);
/* 197 */             ScheduledJobUtils.addJobHistoryEvent(binder, this.m_workspace, null);
/*     */           }
/*     */         }
/*     */       }
/* 201 */       this.m_workspace.commitTran();
/* 202 */       isInTran = false;
/*     */ 
/* 205 */       FileUtils.releaseLongTermLock(dir, lockName, "ScheduledJobs");
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 210 */       String job = (String)jobInfo.get("dSjName");
/* 211 */       Report.info("scheduledjobs", t, "csSjCleanerUpdateError", new Object[] { job });
/*     */     }
/*     */     finally
/*     */     {
/* 215 */       if (isInTran)
/*     */       {
/* 217 */         this.m_workspace.rollbackTran();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 224 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87863 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.ScheduledJobsCleaner
 * JD-Core Version:    0.5.4
 */