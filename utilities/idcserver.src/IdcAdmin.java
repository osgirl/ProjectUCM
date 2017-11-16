/*    */ import intradoc.admin.IdcAdminManager;
/*    */ import intradoc.common.LocaleResources;
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import java.io.PrintStream;
/*    */ 
/*    */ public class IdcAdmin
/*    */ {
/* 28 */   protected static IdcAdminManager m_adminManager = new IdcAdminManager();
/*    */   protected static int m_port;
/*    */ 
/*    */   public static void main(String[] args)
/*    */   {
/* 38 */     IdcAdmin admin = new IdcAdmin();
/*    */     try
/*    */     {
/* 41 */       admin.init();
/*    */     }
/*    */     catch (Throwable t)
/*    */     {
/*    */       try
/*    */       {
/* 47 */         Report.fatal(null, "!csFailedToInitAdminServer", t);
/* 48 */         String msg = LocaleUtils.createMessageStringFromThrowable(t);
/* 49 */         System.err.println(LocaleResources.localizeMessage(msg, null));
/* 50 */         System.exit(-1);
/*    */       }
/*    */       catch (Throwable t2)
/*    */       {
/* 54 */         t2.printStackTrace();
/*    */       }
/* 56 */       System.exit(-1);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void init()
/*    */     throws DataException, ServiceException
/*    */   {
/* 65 */     m_adminManager.init();
/* 66 */     m_adminManager.serviceStart(0, 0);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 71 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     IdcAdmin
 * JD-Core Version:    0.5.4
 */