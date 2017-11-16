/*    */ package intradoc.data;
/*    */ 
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import intradoc.common.StringUtils;
/*    */ 
/*    */ public class DataFormatHDA
/*    */   implements DataFormat
/*    */ {
/* 25 */   static final Object[][] TOKENS = { { DEFINE_DATABINDER, "<?hda version=\"", T_PRODUCT_VERSION, "\" jcharset=\"UTF8\" encoding=\"utf-8\"?>\n", T_NAME, T_LOCAL_DATA, T_ENVIRONMENT, T_RESULT_SETS }, { DEFINE_DATABINDER_NAME, "# ", T_NAME, "\n" }, { DEFINE_PROPERTIES, "@Properties ", T_NAME, "\n", T_PAIRS, "@end\n" }, { DEFINE_PROPERTY_PAIR, T_NAME, "=", T_VALUE, "\n" }, { DEFINE_RESULT_SET, "@ResultSet ", T_NAME, "\n", IF_DEAD, "###  This ResultSet is dead.", ENDIF, IF_SHOW_CURRENT_ONLY, "###  Only current row is shown\n", ELSE, IF_SHOW_LIMITED, "###  Only showing rows ", T_ROW_INDEX_FIRST, "-", T_ROW_INDEX_LAST, "\n", ENDIF, ENDIF, T_COUNT_FIELDS, "\n", T_FIELDS, T_ROWS, "@end\n" }, { DEFINE_RESULT_SET_FIELD, T_NAME, "\n" }, { DEFINE_RESULT_SET_CELL, T_VALUE, "\n" } };
/*    */ 
/*    */   public Object[][] getTokens()
/*    */   {
/* 72 */     return TOKENS;
/*    */   }
/*    */ 
/*    */   public void appendLiteral(IdcStringBuilder builder, String literal)
/*    */   {
/* 77 */     if (null == literal)
/*    */     {
/* 79 */       builder.append("");
/* 80 */       return;
/*    */     }
/* 82 */     StringUtils.appendEscapedString(builder, literal, 9728L);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 89 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70368 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataFormatHDA
 * JD-Core Version:    0.5.4
 */