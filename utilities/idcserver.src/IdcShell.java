/*    */ import intradoc.apputilities.idcshell.InteractiveShellStartup;
/*    */ 
/*    */ public class IdcShell
/*    */ {
/*    */   public static void main(String[] args)
/*    */   {
/* 26 */     InteractiveShellStartup startup = new InteractiveShellStartup();
/* 27 */     int rc = startup.startup(args);
/* 28 */     System.exit(rc);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 33 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     IdcShell
 * JD-Core Version:    0.5.4
 */