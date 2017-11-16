/*    */ package intradoc.server.jobs;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ 
/*    */ public class JobExceptionData
/*    */ {
/*    */   public String m_errorMsgStub;
/*    */   public boolean m_isTolerant;
/*    */   public int m_maxErrors;
/*    */   public int m_maxSevereErrors;
/*    */   public int m_maxConsecutiveErrors;
/*    */   public int m_percentageLoss;
/*    */   public int m_errorCount;
/*    */   public int m_consecutiveErrorCount;
/*    */   public int m_severeErrorCount;
/*    */ 
/*    */   public JobExceptionData()
/*    */   {
/* 32 */     this.m_errorCount = 0;
/* 33 */     this.m_consecutiveErrorCount = 0;
/* 34 */     this.m_severeErrorCount = 0;
/*    */   }
/*    */ 
/*    */   public void init(JobState jState) {
/* 38 */     DataBinder binder = jState.m_data;
/* 39 */     this.m_isTolerant = ScheduledJobUtils.getConfigBoolean("SjIsErrorTolerant", binder, true, true);
/*    */ 
/* 41 */     if (!this.m_isTolerant)
/*    */     {
/* 43 */       this.m_maxErrors = 0;
/* 44 */       this.m_maxSevereErrors = 0;
/* 45 */       this.m_maxConsecutiveErrors = 0;
/* 46 */       this.m_percentageLoss = 0;
/*    */     }
/*    */     else
/*    */     {
/* 50 */       this.m_maxErrors = ScheduledJobUtils.getConfigInteger("SjMaxErrors", binder, 50, true);
/* 51 */       this.m_maxSevereErrors = ScheduledJobUtils.getConfigInteger("SjMaxSevereErrors", binder, 0, true);
/*    */ 
/* 53 */       this.m_maxConsecutiveErrors = ScheduledJobUtils.getConfigInteger("SjMaxConsecutiveErrors", binder, 10, true);
/*    */ 
/* 55 */       this.m_percentageLoss = ScheduledJobUtils.getConfigInteger("SjPercentageLoss", binder, 10, true);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void setTolerance(boolean isTolerant)
/*    */   {
/* 62 */     this.m_isTolerant = isTolerant;
/*    */   }
/*    */ 
/*    */   public void resetTolerance(JobState jState)
/*    */   {
/* 67 */     this.m_isTolerant = ScheduledJobUtils.getConfigBoolean("SjIsErrorTolerant", jState.m_data, true, true);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 73 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66344 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.JobExceptionData
 * JD-Core Version:    0.5.4
 */