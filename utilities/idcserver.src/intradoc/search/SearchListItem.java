/*    */ package intradoc.search;
/*    */ 
/*    */ import intradoc.common.IdcAppendable;
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import intradoc.common.StringUtils;
/*    */ 
/*    */ public class SearchListItem
/*    */ {
/*    */   public String m_key;
/*    */   public int m_rowIndex;
/*    */   public String m_sortValue;
/*    */   public String m_sortValuePrepared;
/*    */   public long m_sortValueParsed;
/*    */   public String[] m_perQueryDocMetadata;
/*    */ 
/*    */   public void appendDebugFormat(IdcAppendable appendable)
/*    */   {
/* 69 */     appendable.append(this.m_key);
/* 70 */     appendable.append(" (");
/* 71 */     StringUtils.appendDebugProperty(appendable, "sortValue", this.m_sortValue, false);
/* 72 */     StringUtils.appendDebugProperty(appendable, "perQueryDocMetadata", this.m_perQueryDocMetadata, true);
/* 73 */     appendable.append(")");
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 83 */     IdcStringBuilder output = new IdcStringBuilder();
/* 84 */     appendDebugFormat(output);
/* 85 */     return output.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 90 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80988 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchListItem
 * JD-Core Version:    0.5.4
 */