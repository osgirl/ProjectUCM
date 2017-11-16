/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.SharedObjects;
/*     */ 
/*     */ public abstract class CommonStateHandlerThread extends Thread
/*     */ {
/*     */   public DataResultSet m_currentJob;
/*     */   public String m_threadID;
/*     */   public Workspace m_workspace;
/*     */   public Object m_waitObj;
/*     */   public long m_waitTime;
/*     */ 
/*     */   public void init(Workspace workspace, Object waitObj)
/*     */   {
/*  46 */     String serverID = SharedObjects.getEnvironmentValue("IDC_Id");
/*  47 */     if ((serverID == null) || (serverID.length() == 0))
/*     */     {
/*  49 */       serverID = SharedObjects.getEnvironmentValue("IDC_Name");
/*     */     }
/*  51 */     this.m_threadID = ("Server-" + serverID + "; Thread-" + Thread.currentThread().getName());
/*  52 */     this.m_workspace = workspace;
/*  53 */     this.m_waitTime = 3000L;
/*  54 */     this.m_waitObj = waitObj;
/*     */   }
/*     */ 
/*     */   public abstract boolean pickUpJob();
/*     */ 
/*     */   public abstract void processJob();
/*     */ 
/*     */   public void resetWaitTime()
/*     */   {
/*  71 */     this.m_waitTime = 3000L;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*  78 */     long waitTimeThreshold = SharedObjects.getEnvironmentInt("StateHandlerMaxWaitTime", 300000);
/*  79 */     while (!SharedObjects.getEnvValueAsBoolean("StopStateHandlerThreads", false))
/*     */     {
/*     */       try
/*     */       {
/*  83 */         if (pickUpJob())
/*     */         {
/*  85 */           SubjectManager.notifyChanged("commonStateHandlerThread");
/*  86 */           processJob();
/*     */         }
/*     */         else
/*     */         {
/*  90 */           synchronized (this.m_waitObj)
/*     */           {
/*     */             try
/*     */             {
/*  94 */               SystemUtils.wait(this.m_waitObj, this.m_waitTime);
/*     */             }
/*     */             catch (InterruptedException e)
/*     */             {
/*     */             }
/*     */           }
/*     */ 
/* 101 */           long nextWaitTime = this.m_waitTime * 2L;
/* 102 */           if (nextWaitTime > waitTimeThreshold)
/*     */           {
/* 104 */             nextWaitTime = this.m_waitTime;
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (Exception ex)
/*     */       {
/* 111 */         Report.error(null, "Runtime exception.", ex);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 118 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99539 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.CommonStateHandlerThread
 * JD-Core Version:    0.5.4
 */