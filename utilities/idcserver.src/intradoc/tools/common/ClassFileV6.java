/*    */ package intradoc.tools.common;
/*    */ 
/*    */ import java.io.IOException;
/*    */ 
/*    */ public class ClassFileV6 extends ClassFileV5
/*    */ {
/*    */   protected void checkVersion()
/*    */     throws IOException
/*    */   {
/* 34 */     int major = this.major_version;
/* 35 */     if ((major >= 45) && (major <= 50))
/*    */       return;
/* 37 */     String msg = "classfile version out of range [45.0, 50.0]: " + major + (this.minor_version & 0xFFFF);
/* 38 */     throw new IOException(msg);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 45 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99523 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.ClassFileV6
 * JD-Core Version:    0.5.4
 */