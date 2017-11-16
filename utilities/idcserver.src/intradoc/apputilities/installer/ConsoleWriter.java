/*    */ package intradoc.apputilities.installer;
/*    */ 
/*    */ import intradoc.common.NativeOsUtils;
/*    */ import java.io.IOException;
/*    */ import java.io.Writer;
/*    */ 
/*    */ public class ConsoleWriter extends Writer
/*    */ {
/*    */   protected NativeOsUtils m_utils;
/*    */ 
/*    */   public ConsoleWriter(NativeOsUtils utils)
/*    */   {
/* 32 */     this.m_utils = utils;
/*    */   }
/*    */ 
/*    */   public void write(int ch)
/*    */     throws IOException
/*    */   {
/* 38 */     String s = "" + (char)ch;
/* 39 */     writeString(s);
/*    */   }
/*    */ 
/*    */   public void write(char[] buf, int start, int length)
/*    */     throws IOException
/*    */   {
/* 45 */     String s = new String(buf, start, length);
/* 46 */     writeString(s);
/*    */   }
/*    */ 
/*    */   public void write(String s)
/*    */     throws IOException
/*    */   {
/* 52 */     writeString(s);
/*    */   }
/*    */ 
/*    */   protected void writeString(String s) throws IOException
/*    */   {
/* 57 */     int rc = this.m_utils.writeConsole(s, 0);
/* 58 */     if (rc == 0)
/*    */       return;
/* 60 */     throw new IOException(this.m_utils.getErrorMessage(rc));
/*    */   }
/*    */ 
/*    */   public void flush()
/*    */   {
/*    */   }
/*    */ 
/*    */   public void close()
/*    */   {
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 78 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.ConsoleWriter
 * JD-Core Version:    0.5.4
 */