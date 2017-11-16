/*    */ package intradoc.server.utils;
/*    */ 
/*    */ import intradoc.apputilities.installer.InstallLog;
/*    */ import intradoc.apputilities.installer.PromptUser;
/*    */ import intradoc.common.IdcMessageFactory;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.util.IdcMessage;
/*    */ 
/*    */ public class ServerInstallUtilsInstallLog extends InstallLog
/*    */ {
/*    */   public boolean m_isStrict;
/*    */ 
/*    */   public ServerInstallUtilsInstallLog(PromptUser promptUser)
/*    */   {
/* 38 */     super(promptUser);
/*    */   }
/*    */ 
/*    */   public void notice(String message)
/*    */     throws ServiceException
/*    */   {
/* 44 */     this.m_promptUser.outputMessage(message);
/*    */   }
/*    */ 
/*    */   public void warning(String message)
/*    */     throws ServiceException
/*    */   {
/* 50 */     error(message);
/*    */   }
/*    */ 
/*    */   public void error(String message)
/*    */     throws ServiceException
/*    */   {
/* 56 */     IdcMessage msg = IdcMessageFactory.lc();
/* 57 */     msg.m_msgLocalized = message;
/* 58 */     throw new ServiceException(null, msg);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 63 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78300 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ServerInstallUtilsInstallLog
 * JD-Core Version:    0.5.4
 */