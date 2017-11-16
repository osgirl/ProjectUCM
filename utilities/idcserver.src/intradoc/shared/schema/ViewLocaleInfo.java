/*    */ package intradoc.shared.schema;
/*    */ 
/*    */ import intradoc.common.DynamicHtml;
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.IdcLocale;
/*    */ 
/*    */ public class ViewLocaleInfo
/*    */ {
/*    */   public String m_name;
/*    */   public boolean m_isKeyField;
/*    */   public String m_fieldName;
/*    */   public int m_fieldIndex;
/*    */   public boolean m_isScript;
/*    */   public String m_displayScript;
/*    */   public DynamicHtml m_script;
/*    */   public IdcLocale m_idcLocale;
/*    */   public ExecutionContext m_context;
/*    */   public int m_fieldRuleIndex;
/*    */   public String m_localizationMessage;
/*    */ 
/*    */   public ViewLocaleInfo()
/*    */   {
/* 26 */     this.m_name = null;
/* 27 */     this.m_isKeyField = false;
/*    */ 
/* 29 */     this.m_fieldName = null;
/* 30 */     this.m_fieldIndex = -1;
/*    */ 
/* 32 */     this.m_isScript = false;
/* 33 */     this.m_displayScript = null;
/* 34 */     this.m_script = null;
/* 35 */     this.m_idcLocale = null;
/* 36 */     this.m_context = null;
/* 37 */     this.m_fieldRuleIndex = -1;
/*    */ 
/* 39 */     this.m_localizationMessage = null;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 44 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.ViewLocaleInfo
 * JD-Core Version:    0.5.4
 */