/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.data.FieldInfo;
/*    */ 
/*    */ public class EntityValue
/*    */ {
/*    */   public String m_field;
/*    */   public String m_type;
/*    */   public char m_symbol;
/*    */   public FieldInfo m_fieldInfo;
/*    */   public String m_entityListStr;
/*    */   public String m_clauseField;
/*    */   public boolean m_isSecurityField;
/*    */ 
/*    */   public EntityValue(String field, String type, String symbol)
/*    */   {
/* 36 */     this.m_field = field;
/* 37 */     this.m_type = type;
/* 38 */     if (symbol.length() > 0)
/*    */     {
/* 40 */       this.m_symbol = symbol.charAt(0);
/*    */     }
/* 42 */     this.m_fieldInfo = new FieldInfo();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 47 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85059 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.EntityValue
 * JD-Core Version:    0.5.4
 */