/*     */ package intradoc.server.jobs;
/*     */ 
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.SubjectEventMonitor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Date;
/*     */ 
/*     */ public class ScheduledJobsMonitor
/*     */   implements SubjectEventMonitor
/*     */ {
/*  29 */   protected int m_reportCount = 0;
/*  30 */   protected int m_noChangeCount = 0;
/*  31 */   protected Workspace m_workspace = null;
/*     */   protected int m_defaultTimeout;
/*     */ 
/*     */   public ScheduledJobsMonitor(Workspace ws)
/*     */   {
/*  36 */     this.m_workspace = ws;
/*  37 */     this.m_defaultTimeout = SharedObjects.getTypedEnvironmentInt("SjAutoWorkInterval", 300, 24, 24);
/*     */   }
/*     */ 
/*     */   public boolean checkForChange(String subject, long curTime)
/*     */   {
/*  43 */     boolean retVal = true;
/*     */     try
/*     */     {
/*  46 */       this.m_noChangeCount += 1;
/*  47 */       int maxWaitCount = this.m_defaultTimeout / 3;
/*  48 */       if (this.m_noChangeCount < maxWaitCount)
/*     */       {
/*  50 */         retVal = false;
/*     */       }
/*     */       else
/*     */       {
/*  54 */         this.m_noChangeCount = 0;
/*     */ 
/*  58 */         if (startScheduledWork(this.m_workspace))
/*     */         {
/*  60 */           retVal = false;
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  66 */       if (this.m_reportCount % 1000 == 0)
/*     */       {
/*  68 */         Report.error(null, t, "csBackgroudProcessingError", new Object[0]);
/*     */       }
/*  70 */       else if (SystemUtils.m_verbose)
/*     */       {
/*  72 */         Report.error("scheduledjbos", "Error encountered while checking for change", t);
/*     */       }
/*  74 */       this.m_reportCount += 1;
/*     */     }
/*  76 */     return retVal;
/*     */   }
/*     */ 
/*     */   public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*     */   {
/*  81 */     startScheduledWork(this.m_workspace);
/*     */   }
/*     */ 
/*     */   public boolean startScheduledWork(Workspace ws)
/*     */   {
/*  86 */     Workspace workspace = ws;
/*  87 */     int timeout = SharedObjects.getTypedEnvironmentInt("IdcSystemQueryTimeout", 1800, 24, 24);
/*     */ 
/*  89 */     Thread jobThread = new Thread("sj_immediate_work", workspace, timeout)
/*     */     {
/*     */       public void run()
/*     */       {
/*     */         try
/*     */         {
/*  96 */           Report.trace("scheduledjobs", "ScheduledJobManager.startScheduledWork: date=" + new Date(), null);
/*     */ 
/*  98 */           this.val$workspace.setThreadTimeout(this.val$timeout);
/*     */ 
/* 100 */           DataBinder binder = new DataBinder();
/* 101 */           binder.putLocal("dSjQueueType", "");
/* 102 */           binder.putLocal("dSjType", "I");
/* 103 */           binder.putLocal("isImmediateJob", "1");
/*     */ 
/* 105 */           ScheduledJobsProcessor processor = ScheduledJobManager.getProcessor(this.val$workspace);
/* 106 */           processor.processJobs(this.val$workspace, binder, new ExecutionContextAdaptor());
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/* 110 */           Report.trace("scheduledjobs", "ScheduledJobManager.startScheduledWork: processing has been aborted at " + new Date(), t);
/*     */         }
/*     */         finally
/*     */         {
/* 115 */           this.val$workspace.releaseConnection();
/* 116 */           this.val$workspace.clearThreadTimeout();
/*     */         }
/*     */       }
/*     */     };
/* 121 */     workspace.releaseConnection();
/* 122 */     jobThread.setDaemon(true);
/* 123 */     jobThread.start();
/*     */ 
/* 125 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 130 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69834 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.ScheduledJobsMonitor
 * JD-Core Version:    0.5.4
 */