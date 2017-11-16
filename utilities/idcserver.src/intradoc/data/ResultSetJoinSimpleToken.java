/*    */ package intradoc.data;
/*    */ 
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ 
/*    */ public class ResultSetJoinSimpleToken
/*    */ {
/*    */   public int m_startIndex;
/*    */   public int m_endIndex;
/*    */   public String m_tableName;
/*    */   public String m_fieldName;
/*    */ 
/*    */   public String toString()
/*    */   {
/* 32 */     IdcStringBuilder str = new IdcStringBuilder("token: ");
/* 33 */     str.append((null == this.m_tableName) ? "null" : this.m_tableName);
/* 34 */     str.append('.');
/* 35 */     str.append((null == this.m_fieldName) ? "default" : this.m_fieldName);
/* 36 */     str.append(" (");
/* 37 */     str.append(this.m_startIndex);
/* 38 */     str.append('-');
/* 39 */     str.append(this.m_endIndex);
/* 40 */     str.append(')');
/* 41 */     return str.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 46 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ResultSetJoinSimpleToken
 * JD-Core Version:    0.5.4
 */