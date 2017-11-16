/*    */ package intradoc.filterdata;
/*    */ 
/*    */ public class FilterDataInputSpecialRules
/*    */ {
/*    */   public static final int ENCODE_DOUBLE_QUOTES = 0;
/*    */   public static final int ENCODE_FULLY_AS_XML_VALUE = 1;
/*    */   public static final int IS_SCRIPTING_PROTECTED = 2;
/* 42 */   public boolean m_encodingLevelExplicitlySet = false;
/*    */ 
/* 47 */   public int m_encodingLevel = 0;
/*    */ 
/* 53 */   public boolean m_encodeDoubleQuotes = false;
/*    */ 
/* 59 */   public boolean m_encodeFullyAsXmlValue = false;
/*    */ 
/* 65 */   public boolean m_isScriptingProtected = false;
/*    */ 
/*    */   public void setBooleanAttribute(int index, boolean value)
/*    */   {
/* 74 */     switch (index)
/*    */     {
/*    */     case 0:
/* 77 */       this.m_encodeDoubleQuotes = value;
/* 78 */       break;
/*    */     case 1:
/* 80 */       this.m_encodeFullyAsXmlValue = value;
/* 81 */       break;
/*    */     case 2:
/* 83 */       this.m_isScriptingProtected = value;
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 91 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filterdata.FilterDataInputSpecialRules
 * JD-Core Version:    0.5.4
 */