/*    */ package intradoc.io;
/*    */ 
/*    */ import java.io.BufferedWriter;
/*    */ import java.io.IOException;
/*    */ import java.io.Writer;
/*    */ 
/*    */ public class BufferedWriterWithLineSeparator extends BufferedWriter
/*    */ {
/*    */   public String m_lineSeparator;
/*    */ 
/*    */   public BufferedWriterWithLineSeparator(Writer out, String lineSeparator)
/*    */   {
/* 33 */     super(out);
/* 34 */     this.m_lineSeparator = lineSeparator;
/*    */   }
/*    */ 
/*    */   public BufferedWriterWithLineSeparator(Writer out, int sz, String lineSeparator)
/*    */   {
/* 39 */     super(out, sz);
/* 40 */     this.m_lineSeparator = lineSeparator;
/*    */   }
/*    */ 
/*    */   public void newLine()
/*    */     throws IOException
/*    */   {
/* 46 */     write(this.m_lineSeparator);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 52 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92690 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.BufferedWriterWithLineSeparator
 * JD-Core Version:    0.5.4
 */