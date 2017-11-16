/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.StringUtils;
/*     */ 
/*     */ public class DataFormatJson
/*     */   implements DataFormat
/*     */ {
/*  25 */   static final Object[][] TOKENS = { { DEFINE_DATABINDER, "{\n", T_NAME, T_LOCAL_DATA, T_ENVIRONMENT, "\n\n\"ResultSets\": {\n", T_RESULT_SETS, "\n}\n}\n" }, { DEFINE_DATABINDER_NAME, "// ", T_NAME, "\n" }, { DEFINE_PROPERTIES, "\n", T_NAME, ": {\n", T_PAIRS, "\n},\n" }, { DEFINE_PROPERTY_PAIR, IF_NOT_FIRST, ",\n", ENDIF, T_NAME, ": ", T_VALUE }, { DEFINE_RESULT_SET, IF_DEAD, "// ResultSet ", T_NAME, " is dead, ignoring.\n", ELSE, IF_NOT_FIRST, ",\n", ENDIF, T_NAME, ": {\n", IF_SHOW_CURRENT_ONLY, "\"rowsAvailable\": ", IF_IS_SEEKABLE, "[ ", T_ROW_INDEX_FIRST, ", ", T_ROW_INDEX_LAST, " ],\n", ELSE, "\"current\",\n", ENDIF, ELSE, IF_SHOW_LIMITED, "\"rowsAvailable\": [ ", T_ROW_INDEX_FIRST, ", ", T_ROW_INDEX_LAST, " ],\n", ENDIF, ENDIF, IF_IS_SEEKABLE, "\"currentRow\": ", T_ROW_INDEX_CURRENT, ",\n", ENDIF, "\"fields\": [\n", T_FIELDS, "\n]", IF_SHOW_CURRENT_ONLY, IF_HAS_ROW, ",\n\"row\":\n", T_ROWS, ENDIF, ELSE, ",\n\"rows\": [\n", T_ROWS, "\n]", ENDIF, "\n}", ENDIF }, { DEFINE_RESULT_SET_FIELD, IF_NOT_FIRST, ",\n", ENDIF, "{ \"name\": ", T_NAME, " }" }, { DEFINE_RESULT_SET_ROW, IF_NOT_FIRST, ",\n", ENDIF, "[\n", T_CELLS, "\n]" }, { DEFINE_RESULT_SET_CELL, IF_NOT_FIRST, ",\n", ENDIF, T_VALUE } };
/*     */   public static final long ESCAPE_FLAGS = 2106884L;
/*     */ 
/*     */   public Object[][] getTokens()
/*     */   {
/*  95 */     return TOKENS;
/*     */   }
/*     */ 
/*     */   public void appendLiteral(IdcStringBuilder builder, String literal)
/*     */   {
/* 103 */     if (null == literal)
/*     */     {
/* 105 */       builder.append("null");
/* 106 */       return;
/*     */     }
/* 108 */     builder.append('"');
/* 109 */     StringUtils.appendEscapedString(builder, literal, 2106884L);
/* 110 */     builder.append('"');
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 117 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84217 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataFormatJson
 * JD-Core Version:    0.5.4
 */