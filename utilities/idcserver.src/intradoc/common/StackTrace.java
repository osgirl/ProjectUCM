/*    */ package intradoc.common;
/*    */ 
/*    */ public class StackTrace extends Exception
/*    */ {
/*    */   public StackTrace()
/*    */   {
/* 33 */     super("!$stack trace\t");
/*    */   }
/*    */ 
/*    */   public StackTrace(String msg)
/*    */   {
/* 38 */     super(msg + '\t');
/*    */   }
/*    */ 
/*    */   public StackTrace(Throwable t)
/*    */   {
/* 43 */     super("!$stack trace\t");
/* 44 */     SystemUtils.setExceptionCause(this, t);
/*    */   }
/*    */ 
/*    */   public StackTrace(String msg, Throwable t)
/*    */   {
/* 49 */     super(msg + '\t');
/* 50 */     SystemUtils.setExceptionCause(this, t);
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 56 */     return getMessage();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 61 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87950 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.StackTrace
 * JD-Core Version:    0.5.4
 */