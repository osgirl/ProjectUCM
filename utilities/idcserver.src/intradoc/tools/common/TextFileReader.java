/*    */ package intradoc.tools.common;
/*    */ 
/*    */ import intradoc.tools.utils.SimpleFileUtils;
/*    */ import java.io.BufferedReader;
/*    */ import java.io.Closeable;
/*    */ import java.io.File;
/*    */ import java.io.FileInputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStreamReader;
/*    */ 
/*    */ public class TextFileReader
/*    */   implements Closeable
/*    */ {
/*    */   public File m_file;
/*    */   public String m_charsetName;
/*    */   public int m_lineNumber;
/*    */   protected BufferedReader m_reader;
/*    */ 
/*    */   public TextFileReader(File file)
/*    */   {
/* 38 */     this.m_file = file;
/* 39 */     this.m_charsetName = "UTF-8";
/*    */   }
/*    */ 
/*    */   public void open() throws IOException
/*    */   {
/* 44 */     close();
/* 45 */     FileInputStream fis = null;
/* 46 */     InputStreamReader isr = null;
/* 47 */     BufferedReader br = null;
/*    */     try
/*    */     {
/* 50 */       fis = new FileInputStream(this.m_file);
/* 51 */       isr = new InputStreamReader(fis, this.m_charsetName);
/* 52 */       br = new BufferedReader(isr);
/*    */     }
/*    */     catch (IOException ioe)
/*    */     {
/* 56 */       if (br != null)
/*    */       {
/* 58 */         SimpleFileUtils.close(br);
/*    */       }
/* 60 */       else if (isr != null)
/*    */       {
/* 62 */         SimpleFileUtils.close(isr);
/*    */       }
/*    */       else
/*    */       {
/* 66 */         SimpleFileUtils.close(fis);
/*    */       }
/* 68 */       throw ioe;
/*    */     }
/* 70 */     this.m_reader = br;
/*    */   }
/*    */ 
/*    */   public void close()
/*    */   {
/* 75 */     SimpleFileUtils.close(this.m_reader);
/* 76 */     this.m_reader = null;
/* 77 */     this.m_lineNumber = 0;
/*    */   }
/*    */ 
/*    */   public String readLine() throws IOException
/*    */   {
/* 82 */     String line = this.m_reader.readLine();
/* 83 */     this.m_lineNumber += 1;
/* 84 */     return line;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 89 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99054 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.TextFileReader
 * JD-Core Version:    0.5.4
 */