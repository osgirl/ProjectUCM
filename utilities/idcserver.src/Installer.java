/*    */ import intradoc.apputilities.installer.StartInstaller;
/*    */ 
/*    */ public class Installer
/*    */ {
/*    */   public static void main(String[] args)
/*    */   {
/* 28 */     StartInstaller installer = new StartInstaller();
/* 29 */     int rc = installer.main(args);
/* 30 */     System.exit(rc);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 35 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     Installer
 * JD-Core Version:    0.5.4
 */