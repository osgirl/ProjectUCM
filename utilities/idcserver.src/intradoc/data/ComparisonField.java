/*    */ package intradoc.data;
/*    */ 
/*    */ public class ComparisonField
/*    */ {
/*    */   public static final short COMPARE_NO_ACTION = -1;
/*    */   public static final short COMPARE_EQUALS = 0;
/*    */   public static final short COMPARE_LESS_THAN = 1;
/*    */   public static final short COMPARE_GREATER_THAN = 2;
/*    */   public static final short COMPARE_NOT_EQUALS = 3;
/*    */   public static final short COMPARE_GT_OR_EQ = 4;
/*    */   public static final short COMPARE_LT_OR_EQ = 5;
/*    */   public static final short COMPARE_LIKE = 6;
/*    */   public String m_name;
/*    */   public String m_alias;
/*    */   public int m_type;
/*    */ 
/*    */   public ComparisonField()
/*    */   {
/* 75 */     this.m_name = null;
/* 76 */     this.m_alias = null;
/* 77 */     this.m_type = -1;
/*    */   }
/*    */ 
/*    */   public String getArgName()
/*    */   {
/* 87 */     if (this.m_alias != null)
/* 88 */       return this.m_alias;
/* 89 */     return "a" + this.m_name;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 94 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ComparisonField
 * JD-Core Version:    0.5.4
 */