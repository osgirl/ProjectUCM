/*    */ package intradoc.server.archive;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class TransferInfo
/*    */ {
/* 30 */   public ExecutionContext m_context = null;
/* 31 */   public Properties m_properties = null;
/*    */ 
/* 34 */   public boolean m_isFinished = false;
/* 35 */   public boolean m_failed = false;
/*    */ 
/*    */   public TransferInfo(Properties props, ExecutionContext ctxt)
/*    */   {
/* 39 */     this.m_properties = props;
/* 40 */     this.m_context = ctxt;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 45 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.TransferInfo
 * JD-Core Version:    0.5.4
 */