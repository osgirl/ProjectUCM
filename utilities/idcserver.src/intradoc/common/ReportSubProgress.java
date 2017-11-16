/*    */ package intradoc.common;
/*    */ 
/*    */ public class ReportSubProgress
/*    */   implements ReportProgress
/*    */ {
/*    */   public int m_curProgress;
/*    */   public int m_maxProgress;
/*    */   protected ReportProgress m_progress;
/*    */ 
/*    */   public ReportSubProgress(ReportProgress progress, int amtDone, int max)
/*    */   {
/* 32 */     this.m_curProgress = amtDone;
/* 33 */     this.m_maxProgress = max;
/* 34 */     this.m_progress = progress;
/*    */   }
/*    */ 
/*    */   public void reportProgress(int type, String msg, float amtDone, float max)
/*    */   {
/* 39 */     if ((this.m_progress == null) || (this.m_curProgress >= this.m_maxProgress) || (max < 0.0F))
/*    */       return;
/* 41 */     float amt = this.m_curProgress + amtDone / max;
/* 42 */     this.m_progress.reportProgress(1, msg, amt, this.m_maxProgress);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 49 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ReportSubProgress
 * JD-Core Version:    0.5.4
 */