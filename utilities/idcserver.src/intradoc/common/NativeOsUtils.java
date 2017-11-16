/*    */ package intradoc.common;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class NativeOsUtils extends NativeOsUtilsBase
/*    */ {
/*    */   public NativeOsUtils()
/*    */   {
/* 28 */     super(null);
/*    */   }
/*    */ 
/*    */   public NativeOsUtils(Map args)
/*    */   {
/* 33 */     super(args);
/*    */   }
/*    */ 
/*    */   public void report(String section, Object arg)
/*    */   {
/* 41 */     System.err.println(arg);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 46 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98970 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.NativeOsUtils
 * JD-Core Version:    0.5.4
 */