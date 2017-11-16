/*    */ package intradoc.util;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.io.Writer;
/*    */ 
/*    */ public class WriterToIdcAppendable extends Writer
/*    */ {
/*    */   public IdcAppendableBase m_app;
/* 27 */   public IOException m_exception = null;
/*    */ 
/*    */   public WriterToIdcAppendable(IdcAppendableBase appendable)
/*    */   {
/* 32 */     this.m_app = appendable;
/*    */   }
/*    */ 
/*    */   public void write(char[] buf, int start, int length)
/*    */   {
/* 38 */     this.m_app.append(buf, start, length);
/*    */   }
/*    */ 
/*    */   public void close()
/*    */   {
/*    */   }
/*    */ 
/*    */   public void flush()
/*    */   {
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 55 */     return "releaseInfo=dev,releaseRevision=$Rev: 84156 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.WriterToIdcAppendable
 * JD-Core Version:    0.5.4
 */