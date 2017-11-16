/*    */ package intradoc.taskmanager;
/*    */ 
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.VersionInfo;
/*    */ import intradoc.provider.SocketIncomingProvider;
/*    */ 
/*    */ public class TaskManagerIncomingProvider extends SocketIncomingProvider
/*    */ {
/*    */   public String getReportString(String key)
/*    */   {
/* 35 */     String msg = null;
/* 36 */     if (key.equals("startup"))
/*    */     {
/* 38 */       String pidString = getProcessIdString();
/* 39 */       msg = "!$Distributed Task Manager. Version " + VersionInfo.getProductVersion();
/* 40 */       msg = msg + "\n" + LocaleUtils.encodeMessage(VersionInfo.getProductCopyright(), null);
/* 41 */       msg = LocaleUtils.appendMessage("\n", msg);
/* 42 */       msg = msg + LocaleUtils.encodeMessage("csServerWaitingForConnectionMessage", null, new StringBuilder().append("").append(this.m_serverPort).toString(), pidString);
/*    */ 
/* 45 */       Report.info(null, null, "csTaskManagerWaitingForConnectionLogMessage", new Object[] { VersionInfo.getProductVersion(), "" + this.m_serverPort });
/*    */     }
/*    */ 
/* 48 */     return msg;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 54 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.taskmanager.TaskManagerIncomingProvider
 * JD-Core Version:    0.5.4
 */