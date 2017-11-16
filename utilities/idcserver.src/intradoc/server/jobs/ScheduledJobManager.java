/*    */ package intradoc.server.jobs;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.server.SubjectManager;
/*    */ import intradoc.shared.SharedObjects;
/*    */ 
/*    */ public class ScheduledJobManager
/*    */ {
/* 27 */   protected static ScheduledJobStorage m_storage = null;
/* 28 */   protected static ScheduledJobsProcessor m_processor = null;
/*    */ 
/* 30 */   protected static boolean m_isInitialized = false;
/* 31 */   protected static boolean m_isQueueStarted = false;
/*    */ 
/*    */   public static void init(Workspace ws) throws ServiceException, DataException
/*    */   {
/* 35 */     if (m_isInitialized)
/*    */       return;
/* 37 */     m_storage = new ScheduledJobStorage();
/* 38 */     m_storage.init(ws);
/*    */ 
/* 40 */     m_processor = new ScheduledJobsProcessor();
/* 41 */     m_isInitialized = true;
/*    */   }
/*    */ 
/*    */   public static ScheduledJobStorage getStorage(Workspace ws)
/*    */     throws ServiceException, DataException
/*    */   {
/* 48 */     init(ws);
/* 49 */     return m_storage;
/*    */   }
/*    */ 
/*    */   public static ScheduledJobsProcessor getProcessor(Workspace ws)
/*    */     throws ServiceException, DataException
/*    */   {
/* 55 */     init(ws);
/* 56 */     return m_processor;
/*    */   }
/*    */ 
/*    */   public static void initProcessing(Workspace workspace) throws ServiceException
/*    */   {
/* 61 */     if ((!SharedObjects.getEnvValueAsBoolean("SjIsAutoQueue", true)) || (m_isQueueStarted))
/*    */     {
/* 63 */       return;
/*    */     }
/*    */ 
/* 66 */     ScheduledJobsMonitor jobMonitor = new ScheduledJobsMonitor(workspace);
/* 67 */     SubjectManager.addSubjectMonitor("scheduledjobs", jobMonitor);
/* 68 */     m_isQueueStarted = true;
/*    */ 
/* 71 */     ScheduledJobsCleaner jobCleaner = new ScheduledJobsCleaner();
/* 72 */     jobCleaner.init(workspace);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 77 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82123 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.ScheduledJobManager
 * JD-Core Version:    0.5.4
 */