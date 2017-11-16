/*    */ package intradoc.common;
/*    */ 
/*    */ import intradoc.util.IdcVector;
/*    */ import java.util.Hashtable;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class HashVector
/*    */ {
/*    */   public Hashtable m_hash;
/*    */   public Vector m_values;
/*    */ 
/*    */   public HashVector()
/*    */   {
/* 30 */     this.m_hash = new Hashtable();
/* 31 */     this.m_values = new IdcVector();
/*    */   }
/*    */ 
/*    */   public void addValue(String value) {
/* 35 */     String cValue = value.toLowerCase();
/*    */ 
/* 37 */     Object temp = this.m_hash.put(cValue, value);
/* 38 */     if (temp != null)
/*    */       return;
/* 40 */     this.m_values.addElement(value);
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 47 */     StringBuffer buffer = new StringBuffer();
/* 48 */     int size = this.m_values.size();
/* 49 */     for (int i = 0; i < size; ++i)
/*    */     {
/* 51 */       String value = (String)this.m_values.elementAt(i);
/* 52 */       if (buffer.length() > 0)
/*    */       {
/* 54 */         buffer.append(' ');
/*    */       }
/* 56 */       buffer.append(value);
/*    */     }
/* 58 */     return buffer.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 63 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.HashVector
 * JD-Core Version:    0.5.4
 */