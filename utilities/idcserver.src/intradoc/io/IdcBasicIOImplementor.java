/*    */ package intradoc.io;
/*    */ 
/*    */ import java.io.FileInputStream;
/*    */ import java.io.FileOutputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.OutputStream;
/*    */ 
/*    */ public class IdcBasicIOImplementor
/*    */   implements IdcBasicIO
/*    */ {
/*    */   public InputStream getReadStream(String path)
/*    */     throws IOException
/*    */   {
/* 26 */     return new FileInputStream(path);
/*    */   }
/*    */ 
/*    */   public OutputStream getWriteStream(String path) throws IOException
/*    */   {
/* 31 */     return new FileOutputStream(path);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 36 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcBasicIOImplementor
 * JD-Core Version:    0.5.4
 */