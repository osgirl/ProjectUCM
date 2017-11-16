/*    */ package intradoc.common;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.FileNotFoundException;
/*    */ import java.io.IOException;
/*    */ import java.io.RandomAccessFile;
/*    */ 
/*    */ public class RandomAccessConfigFile
/*    */ {
/*    */   public RandomAccessFile m_randomAccessFile;
/*    */ 
/*    */   public RandomAccessConfigFile()
/*    */   {
/*    */   }
/*    */ 
/*    */   public RandomAccessConfigFile(File file, String flags)
/*    */     throws FileNotFoundException
/*    */   {
/* 40 */     this.m_randomAccessFile = new RandomAccessFile(file, flags);
/*    */   }
/*    */ 
/*    */   public void close() throws IOException
/*    */   {
/* 45 */     this.m_randomAccessFile.close();
/*    */   }
/*    */ 
/*    */   public long length() throws IOException
/*    */   {
/* 50 */     return this.m_randomAccessFile.length();
/*    */   }
/*    */ 
/*    */   public int read(byte[] b)
/*    */     throws IOException
/*    */   {
/* 56 */     return this.m_randomAccessFile.read(b);
/*    */   }
/*    */ 
/*    */   public int read(byte[] b, int off, int len) throws IOException
/*    */   {
/* 61 */     return this.m_randomAccessFile.read(b, off, len);
/*    */   }
/*    */ 
/*    */   public void seek(long pos) throws IOException
/*    */   {
/* 66 */     this.m_randomAccessFile.seek(pos);
/*    */   }
/*    */ 
/*    */   public void write(byte[] b) throws IOException
/*    */   {
/* 71 */     this.m_randomAccessFile.write(b);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 76 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97526 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.RandomAccessConfigFile
 * JD-Core Version:    0.5.4
 */