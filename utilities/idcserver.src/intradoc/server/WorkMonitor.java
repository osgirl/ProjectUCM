/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ 
/*     */ public class WorkMonitor
/*     */ {
/*  35 */   protected static boolean m_workActive = false;
/*     */ 
/*  38 */   protected static long m_lastActiveTimestamp = 0L;
/*     */ 
/*  41 */   protected static Workspace m_workspace = null;
/*     */ 
/*  44 */   protected static ExecutionContext m_context = null;
/*     */ 
/*  47 */   protected static Thread m_bgThread = null;
/*     */ 
/*  50 */   protected static Observable m_observable = null;
/*     */ 
/*  53 */   protected static long m_primaryTimeout = 0L;
/*  54 */   protected static long m_secondaryTimeout = 0L;
/*     */ 
/*  57 */   protected static int m_numberOfTasks = 0;
/*     */ 
/*  60 */   protected static int m_maxErrors = 0;
/*     */ 
/*  63 */   protected static boolean m_doSecondaryTimeout = false;
/*     */ 
/*     */   public static void init(Workspace ws, Observer observer)
/*     */   {
/*  67 */     m_workspace = ws;
/*  68 */     m_context = new ExecutionContextAdaptor();
/*  69 */     m_observable = new Observable()
/*     */     {
/*     */       public boolean hasChanged()
/*     */       {
/*  74 */         return true;
/*     */       }
/*     */     };
/*  78 */     if (observer != null)
/*     */     {
/*  80 */       m_observable.addObserver(observer);
/*     */     }
/*     */ 
/*  84 */     m_primaryTimeout = SharedObjects.getTypedEnvironmentInt("PrimaryWorkQueueTimeout", 600000, 18, 24);
/*     */ 
/*  87 */     m_secondaryTimeout = SharedObjects.getTypedEnvironmentInt("SecondaryWorkQueueTimeout", 300000, 18, 24);
/*     */ 
/*  91 */     m_numberOfTasks = SharedObjects.getEnvironmentInt("NumberOfTasksInWorkQueue", 500);
/*  92 */     m_maxErrors = SharedObjects.getEnvironmentInt("MaxWorkQueueErrors", 5);
/*  93 */     m_lastActiveTimestamp = System.currentTimeMillis();
/*     */   }
/*     */ 
/*     */   public static boolean isWorkToDo(long curTime)
/*     */   {
/*  98 */     long timeout = m_primaryTimeout;
/*     */ 
/* 100 */     if (m_doSecondaryTimeout)
/*     */     {
/* 102 */       timeout = m_secondaryTimeout;
/*     */     }
/*     */ 
/* 105 */     if ((m_workActive) || (curTime - m_lastActiveTimestamp < timeout))
/*     */     {
/* 107 */       if (m_workActive)
/*     */       {
/* 109 */         m_lastActiveTimestamp = curTime;
/*     */       }
/* 111 */       return false;
/*     */     }
/*     */ 
/* 114 */     return true;
/*     */   }
/*     */ 
/*     */   public static void watchWork(long curTime)
/*     */   {
/* 121 */     if (m_workActive)
/*     */     {
/* 123 */       return;
/*     */     }
/*     */ 
/* 127 */     Runnable workRun = new Runnable()
/*     */     {
/*     */       public void run()
/*     */       {
/*     */         try
/*     */         {
/* 133 */           int timeout = SharedObjects.getTypedEnvironmentInt("IdcSystemQueryTimeout", 1800, 24, 24);
/*     */ 
/* 136 */           WorkMonitor.m_workspace.setThreadTimeout(timeout);
/* 137 */           WorkMonitor.m_doSecondaryTimeout = WorkQueueProcessor.collateWork(WorkMonitor.m_numberOfTasks);
/* 138 */           WorkQueueProcessor.processWork(WorkMonitor.m_workspace, WorkMonitor.m_maxErrors, WorkMonitor.m_context);
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/* 142 */           Report.error(null, t, "csQueueError", new Object[0]);
/*     */         }
/*     */         finally
/*     */         {
/* 147 */           WorkMonitor.m_workActive = false;
/* 148 */           WorkMonitor.m_lastActiveTimestamp = System.currentTimeMillis();
/* 149 */           WorkMonitor.m_workspace.releaseConnection();
/* 150 */           WorkMonitor.m_workspace.clearThreadTimeout();
/*     */         }
/*     */       }
/*     */     };
/* 155 */     m_bgThread = new Thread(workRun, "WorkMonitor");
/* 156 */     m_bgThread.setPriority(1);
/*     */ 
/* 158 */     m_workActive = true;
/* 159 */     m_bgThread.setDaemon(true);
/* 160 */     m_bgThread.start();
/*     */   }
/*     */ 
/*     */   public static void handleExternal(long curTime)
/*     */   {
/* 166 */     m_lastActiveTimestamp = curTime;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 171 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69834 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.WorkMonitor
 * JD-Core Version:    0.5.4
 */