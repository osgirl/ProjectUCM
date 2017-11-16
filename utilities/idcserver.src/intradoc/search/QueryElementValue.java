/*    */ package intradoc.search;
/*    */ 
/*    */ import java.util.Date;
/*    */ 
/*    */ public class QueryElementValue
/*    */ {
/*    */   public String m_originalValue;
/*    */   public Object m_compareValue;
/*    */ 
/*    */   public QueryElementValue(String originalValue, Object compareValue)
/*    */   {
/* 42 */     this.m_originalValue = originalValue;
/* 43 */     if (compareValue instanceof Date)
/*    */     {
/* 47 */       long l = ((Date)compareValue).getTime();
/* 48 */       compareValue = new Long(l);
/*    */     }
/* 50 */     this.m_compareValue = compareValue;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 55 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 77596 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.QueryElementValue
 * JD-Core Version:    0.5.4
 */