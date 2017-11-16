/*    */ package intradoc.data;
/*    */ 
/*    */ public class DatabaseTableRelation
/*    */ {
/*    */   public DatabaseTable m_parentTable;
/*    */   public String m_parentTableColumn;
/*    */   public DatabaseTable m_childTable;
/*    */   public String m_childTableColumn;
/*    */ 
/*    */   public boolean isDefined()
/*    */   {
/* 31 */     boolean isDefined = false;
/* 32 */     if ((this.m_parentTable != null) && (this.m_childTable != null) && (this.m_parentTableColumn != null) && (this.m_childTableColumn != null))
/*    */     {
/* 35 */       isDefined = true;
/*    */     }
/* 37 */     return isDefined;
/*    */   }
/*    */ 
/*    */   public boolean equals(Object o)
/*    */   {
/* 43 */     if ((!o instanceof DatabaseTableRelation) || (!isDefined()) || (!((DatabaseTableRelation)o).isDefined()))
/*    */     {
/* 45 */       return false;
/*    */     }
/*    */ 
/* 48 */     DatabaseTableRelation relation = (DatabaseTableRelation)o;
/* 49 */     boolean isEqual = false;
/* 50 */     if ((this.m_parentTableColumn.equalsIgnoreCase(relation.m_parentTableColumn)) && (this.m_childTableColumn.equalsIgnoreCase(relation.m_childTableColumn)) && (this.m_parentTable.m_alias.equalsIgnoreCase(relation.m_parentTable.m_alias)) && (this.m_childTable.m_alias.equalsIgnoreCase(relation.m_childTable.m_alias)))
/*    */     {
/* 55 */       isEqual = true;
/*    */     }
/* 57 */     return isEqual;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 62 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66809 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DatabaseTableRelation
 * JD-Core Version:    0.5.4
 */