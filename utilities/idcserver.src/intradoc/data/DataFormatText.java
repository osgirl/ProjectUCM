/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ 
/*     */ public class DataFormatText
/*     */   implements DataFormat
/*     */ {
/*  24 */   public static Object[][] TOKENS = { { DEFINE_DATABINDER, T_NAME, "\n", T_LOCAL_DATA, T_ENVIRONMENT, "***  RESULT SETS  ***", T_RESULT_SETS }, { DEFINE_PROPERTIES, "*** ", T_NAME, " ***\n", "(", T_COUNT_DEFINED, " keys + ", T_COUNT_DEFAULT, " defaults", IF_SHOW_DEFAULTS, ELSE, " NOT SHOWN", ENDIF, ")", "\n", T_PAIRS, "\n\n" }, { DEFINE_PROPERTY_PAIR, IF_IS_DEFAULT, "[", ENDIF, T_NAME, "=", T_VALUE, IF_IS_DEFAULT, "]", ENDIF, "\n" }, { DEFINE_RESULT_SET, "\n\n--- @ResultSet ", T_NAME, " ---\nnumFields=", T_COUNT_FIELDS, IF_IS_SEEKABLE, ",numRows=", T_COUNT_ROWS, ",currentRow=", T_ROW_INDEX_CURRENT, ENDIF, "\n", IF_DEAD, "(NOTE: ResultSet is dead)\n", ENDIF, IF_SHOW_CURRENT_ONLY, ELSE, IF_SHOW_LIMITED, "(only showing ", T_COUNT_ROWS_LIMIT, " rows, starting at row ", T_ROW_INDEX_FIRST, ")\n", ENDIF, ENDIF, IF_SHOW_CURRENT_ONLY, ELSE, "[", T_FIELDS, "]\n", ENDIF, T_ROWS }, { DEFINE_RESULT_SET_FIELD, IF_NOT_FIRST, ", ", ENDIF, T_NAME }, { DEFINE_RESULT_SET_ROW, IF_NOT_FIRST, "\n", ENDIF, T_CELLS }, { DEFINE_RESULT_SET_CELL, IF_SHOW_CURRENT_ONLY, IF_NOT_FIRST, "\n", ENDIF, T_NAME, "[", T_FIELD_INDEX, "]", IF_HAS_ROW, "=(", T_VALUE, ")", ENDIF, ELSE, IF_NOT_FIRST, ", ", ENDIF, T_VALUE, ENDIF } };
/*     */ 
/*     */   public Object[][] getTokens()
/*     */   {
/*  92 */     return TOKENS;
/*     */   }
/*     */ 
/*     */   public void appendLiteral(IdcStringBuilder builder, String literal)
/*     */   {
/*  97 */     builder.append((null == literal) ? "<NULL>" : literal);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 104 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70368 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataFormatText
 * JD-Core Version:    0.5.4
 */