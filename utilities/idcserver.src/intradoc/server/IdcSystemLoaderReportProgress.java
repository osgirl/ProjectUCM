/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import intradoc.common.LocaleResources;
/*    */ import intradoc.common.ReportProgress;
/*    */ import java.io.PrintStream;
/*    */ 
/*    */ public class IdcSystemLoaderReportProgress
/*    */   implements ReportProgress
/*    */ {
/* 30 */   protected ExecutionContext m_cxt = null;
/* 31 */   protected IdcStringBuilder m_percentString = new IdcStringBuilder(4);
/*    */ 
/*    */   public IdcSystemLoaderReportProgress()
/*    */   {
/*    */   }
/*    */ 
/*    */   public IdcSystemLoaderReportProgress(ExecutionContext cxt)
/*    */   {
/* 40 */     this.m_cxt = cxt;
/*    */   }
/*    */ 
/*    */   public void reportProgress(int type, String msg, float p, float t) {
/* 44 */     if (t > 0.0F)
/*    */     {
/* 46 */       this.m_percentString.setLength(0);
/* 47 */       double percent = p / t * 100.0F + 0.5D;
/* 48 */       int percentAsInt = (int)percent;
/* 49 */       String percentAsString = String.valueOf(percentAsInt);
/* 50 */       this.m_percentString.append(percentAsString);
/* 51 */       while (this.m_percentString.length() < 3)
/*    */       {
/* 53 */         this.m_percentString.insert(0, ' ');
/*    */       }
/* 55 */       this.m_percentString.insert(0, '(');
/* 56 */       this.m_percentString.append("%) ");
/* 57 */       System.out.print(this.m_percentString.toStringNoRelease());
/*    */     }
/* 59 */     msg = LocaleResources.localizeMessage(msg, this.m_cxt);
/* 60 */     System.out.println(msg);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 66 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IdcSystemLoaderReportProgress
 * JD-Core Version:    0.5.4
 */