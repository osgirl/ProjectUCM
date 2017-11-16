/*    */ package intradoc.server.proxy;
/*    */ 
/*    */ import intradoc.provider.Provider;
/*    */ import intradoc.server.SubjectEventMonitor;
/*    */ 
/*    */ public class OutgoingProviderSubjectMonitor
/*    */   implements SubjectEventMonitor
/*    */ {
/* 29 */   public Provider m_provider = null;
/*    */ 
/*    */   public OutgoingProviderSubjectMonitor(Provider provider)
/*    */   {
/* 33 */     this.m_provider = provider;
/*    */   }
/*    */ 
/*    */   public boolean checkForChange(String subject, long curTime)
/*    */   {
/* 38 */     return false;
/*    */   }
/*    */ 
/*    */   public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*    */   {
/* 45 */     OutgoingProviderMonitor.requestNotification(this.m_provider, subject);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 50 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.proxy.OutgoingProviderSubjectMonitor
 * JD-Core Version:    0.5.4
 */