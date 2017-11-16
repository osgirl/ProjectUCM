/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.util.IdcVector;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class MonikerData
/*    */ {
/*    */   public String m_name;
/*    */   public long m_marker;
/*    */   public Vector m_callbacks;
/*    */ 
/*    */   public MonikerData(String name)
/*    */   {
/* 35 */     this.m_name = name;
/* 36 */     this.m_marker = -2L;
/* 37 */     this.m_callbacks = new IdcVector();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 42 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.MonikerData
 * JD-Core Version:    0.5.4
 */