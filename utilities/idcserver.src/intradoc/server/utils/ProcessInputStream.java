/*    */ package intradoc.server.utils;
/*    */ 
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.SystemUtils;
/*    */ import java.io.FilterInputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ 
/*    */ public class ProcessInputStream extends FilterInputStream
/*    */ {
/* 30 */   public Process m_process = null;
/* 31 */   public boolean m_isClosed = false;
/*    */ 
/*    */   public ProcessInputStream(Process p)
/*    */   {
/* 35 */     super(p.getInputStream());
/* 36 */     this.m_process = p;
/* 37 */     debug("Creating ProcessInputStream Object");
/*    */   }
/*    */ 
/*    */   public ProcessInputStream(InputStream is, Process p)
/*    */   {
/* 42 */     super(is);
/* 43 */     this.m_process = p;
/* 44 */     debug("Creating ProcessInputStream Object");
/*    */   }
/*    */ 
/*    */   public void close()
/*    */     throws IOException
/*    */   {
/*    */     try
/*    */     {
/* 52 */       debug("Closing ProcessInputStream");
/* 53 */       if (!this.m_isClosed)
/*    */       {
/* 55 */         super.close();
/* 56 */         this.m_isClosed = true;
/*    */       }
/*    */       else
/*    */       {
/* 60 */         debug("Redundantly closing ProcessInputStream");
/*    */       }
/*    */     }
/*    */     finally
/*    */     {
/* 65 */       closeProcess();
/*    */     }
/*    */   }
/*    */ 
/*    */   public void closeProcess()
/*    */   {
/* 71 */     if (this.m_process == null)
/*    */       return;
/* 73 */     debug("Destroying ProcessInputStream's process");
/* 74 */     this.m_process.destroy();
/* 75 */     this.m_process = null;
/*    */   }
/*    */ 
/*    */   public void debug(String msg)
/*    */   {
/* 82 */     if (!SystemUtils.m_verbose)
/*    */       return;
/* 84 */     Report.debug("system", msg, null);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 90 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ProcessInputStream
 * JD-Core Version:    0.5.4
 */