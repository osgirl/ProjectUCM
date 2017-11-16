/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.StringUtils;
/*     */ 
/*     */ public class DataFormatHTML
/*     */   implements DataFormat
/*     */ {
/*  26 */   public static Object[][] TOKENS = { { DEFINE_DATABINDER, "<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=", T_SYSTEM_ISO_ENCODING, "\">\n<title>\n", T_NAME, "</title></head>\n<body>\n", T_NAME, "\n", T_LOCAL_DATA, T_ENVIRONMENT, T_RESULT_SETS, "</body>\n</html>\n" }, { DEFINE_PROPERTIES, "<h3>", T_NAME, "</h3>\n<dl compact=\"compact\">\n", T_PAIRS, "</dl>\n\n<hr />\n" }, { DEFINE_PROPERTY_PAIR, "<dt><b>", T_NAME, "</b></dt> <dd>", IF_IS_DEFAULT, "<i>", ENDIF, T_VALUE, IF_IS_DEFAULT, "</i>", ENDIF, "</dd><br/></dt>\n" }, { DEFINE_RESULT_SET, IF_SHOW_CURRENT_ONLY, "<!-- only current row is shown: -->\n", ELSE, IF_SHOW_LIMITED, "<!-- only showing rows ", T_ROW_INDEX_FIRST, " to ", T_ROW_INDEX_LAST, " -->\n", ENDIF, ENDIF, "<table>\n<caption>", T_NAME, "</caption>\n", "<tr>", T_FIELDS, "</tr>\n", T_ROWS, "</table><br />\n" }, { DEFINE_RESULT_SET_FIELD, "<th>", T_NAME, "</th>" }, { DEFINE_RESULT_SET_ROW, "<tr>", T_CELLS, "</tr>\n" }, { DEFINE_RESULT_SET_CELL, "<td>", T_VALUE, "</td>" } };
/*     */ 
/*     */   public Object[][] getTokens()
/*     */   {
/*  79 */     return TOKENS;
/*     */   }
/*     */ 
/*     */   public void appendLiteral(IdcStringBuilder builder, String literal)
/*     */   {
/*  84 */     literal = (null == literal) ? "NULL" : StringUtils.encodeXmlEscapeSequence(literal);
/*  85 */     builder.append(literal);
/*     */   }
/*     */ 
/*     */   public static void appendBegin(IdcAppendable app, String title)
/*     */   {
/*  97 */     String sysEncoding = DataSerializeUtils.getSystemEncoding();
/*  98 */     String isoEncoding = DataSerializeUtils.getIsoEncoding(sysEncoding);
/*  99 */     app.append((String)TOKENS[0][1]);
/* 100 */     app.append(isoEncoding);
/* 101 */     app.append((String)TOKENS[0][3]);
/* 102 */     if (null != title)
/*     */     {
/* 104 */       app.append(StringUtils.encodeXmlEscapeSequence(title));
/*     */     }
/* 106 */     app.append((String)TOKENS[0][5]);
/*     */   }
/*     */ 
/*     */   public static void appendEnd(IdcAppendable app)
/*     */   {
/* 111 */     app.append((String)TOKENS[0][11]);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 117 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97675 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataFormatHTML
 * JD-Core Version:    0.5.4
 */