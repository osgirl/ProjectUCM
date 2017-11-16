/*    */ package intradoc.common;
/*    */ 
/*    */ import intradoc.util.IdcAppenderBase;
/*    */ 
/*    */ public class IdcAppendableStringBuffer
/*    */   implements IdcAppendable
/*    */ {
/* 26 */   public StringBuffer m_buf = null;
/*    */ 
/*    */   public IdcAppendableStringBuffer(StringBuffer buf)
/*    */   {
/* 30 */     this.m_buf = buf;
/*    */   }
/*    */ 
/*    */   public IdcAppendable append(char c)
/*    */   {
/* 35 */     this.m_buf.append(c);
/* 36 */     return this;
/*    */   }
/*    */ 
/*    */   public IdcAppendable append(char[] srcArray, int start, int length)
/*    */   {
/* 41 */     this.m_buf.append(srcArray, start, length);
/* 42 */     return this;
/*    */   }
/*    */ 
/*    */   public IdcAppendable append(CharSequence seq)
/*    */   {
/* 47 */     this.m_buf.append(seq);
/* 48 */     return this;
/*    */   }
/*    */ 
/*    */   public IdcAppendable append(CharSequence seq, int start, int length)
/*    */   {
/* 53 */     seq = new IdcSubSequence(seq, start, start + length);
/* 54 */     this.m_buf.append(seq);
/* 55 */     return this;
/*    */   }
/*    */ 
/*    */   public IdcAppendable append(IdcAppenderBase appendable)
/*    */   {
/* 60 */     appendable.appendTo(this);
/* 61 */     return this;
/*    */   }
/*    */ 
/*    */   public IdcAppendable append(IdcAppender appendable)
/*    */   {
/* 66 */     appendable.appendTo(this);
/* 67 */     return this;
/*    */   }
/*    */ 
/*    */   public IdcAppendable appendObject(Object obj)
/*    */   {
/* 72 */     this.m_buf.append(obj);
/* 73 */     return this;
/*    */   }
/*    */ 
/*    */   public boolean truncate(int l)
/*    */   {
/* 78 */     this.m_buf.setLength(l);
/* 79 */     return true;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 84 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71949 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcAppendableStringBuffer
 * JD-Core Version:    0.5.4
 */