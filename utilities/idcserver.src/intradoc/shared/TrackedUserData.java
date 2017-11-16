/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.StackTrace;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class TrackedUserData extends UserData
/*    */ {
/*    */   public StackTrace m_origin;
/*    */ 
/*    */   protected TrackedUserData(String username, Properties props)
/*    */   {
/* 40 */     super(username, props);
/* 41 */     this.m_origin = new StackTrace();
/*    */   }
/*    */ 
/*    */   public void finalize()
/*    */   {
/* 47 */     Report.trace("userstorage", "Deallocating TrackedUserData object allocated ", this.m_origin);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 52 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87454 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.TrackedUserData
 * JD-Core Version:    0.5.4
 */