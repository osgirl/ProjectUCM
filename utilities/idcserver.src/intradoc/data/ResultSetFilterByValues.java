/*    */ package intradoc.data;
/*    */ 
/*    */ import java.util.Collection;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class ResultSetFilterByValues
/*    */   implements ResultSetFilter
/*    */ {
/*    */   public int m_columnIndex;
/*    */   public Collection<String> m_values;
/*    */   public boolean m_isExclusive;
/*    */ 
/*    */   public ResultSetFilterByValues(int columnIndex, Collection<String> values)
/*    */   {
/* 57 */     this.m_columnIndex = columnIndex;
/* 58 */     this.m_values = values;
/*    */   }
/*    */ 
/*    */   public int checkRow(String key, int numRows, Vector row)
/*    */   {
/*    */     String value;
/*    */     String value;
/* 73 */     if (this.m_columnIndex < 0)
/*    */     {
/* 75 */       value = key;
/*    */     }
/*    */     else
/*    */     {
/* 79 */       Object obj = row.get(this.m_columnIndex);
/*    */       String value;
/* 80 */       if (obj instanceof String)
/*    */       {
/* 82 */         value = (String)obj;
/*    */       }
/*    */       else
/*    */       {
/* 86 */         value = obj.toString();
/*    */       }
/*    */     }
/* 89 */     boolean isMatch = this.m_values.contains(value);
/* 90 */     boolean isMerge = isMatch ^ this.m_isExclusive;
/* 91 */     return (isMerge) ? 1 : 0;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 97 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73791 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ResultSetFilterByValues
 * JD-Core Version:    0.5.4
 */