/*    */ package intradoc.provider;
/*    */ 
/*    */ public abstract class IncomingThread extends Thread
/*    */ {
/*    */   public abstract void init(Provider paramProvider, IncomingConnection paramIncomingConnection);
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 30 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.IncomingThread
 * JD-Core Version:    0.5.4
 */