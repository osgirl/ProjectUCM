/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.shared.MonikerInterface;
/*    */ 
/*    */ public class MonikerInfo
/*    */ {
/*    */   public MonikerInterface m_monikerData;
/*    */   public String m_moniker;
/*    */   public String m_filename;
/*    */   public long m_counter;
/*    */ 
/*    */   public MonikerInfo()
/*    */   {
/* 29 */     this.m_monikerData = null;
/* 30 */     this.m_moniker = null;
/* 31 */     this.m_filename = null;
/* 32 */     this.m_counter = -2L;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 36 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.MonikerInfo
 * JD-Core Version:    0.5.4
 */