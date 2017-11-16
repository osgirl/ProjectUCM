/*    */ import intradoc.apputilities.componentwizard.ComponentToolLauncher;
/*    */ 
/*    */ public class ComponentTool
/*    */ {
/*    */   public static void main(String[] args)
/*    */   {
/* 26 */     ComponentToolLauncher launcher = new ComponentToolLauncher();
/* 27 */     int rc = launcher.launch(args);
/* 28 */     System.exit(rc);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 33 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73293 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     ComponentTool
 * JD-Core Version:    0.5.4
 */