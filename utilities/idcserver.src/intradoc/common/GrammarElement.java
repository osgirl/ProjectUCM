/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.List;
/*     */ 
/*     */ public class GrammarElement
/*     */ {
/*     */   public static final int EVAL = 0;
/*     */   public static final int LITERAL = 1;
/*     */   public static final int FUNCTION = 2;
/*     */   public static final int OPERATOR = 3;
/*     */   public static final int MULTI = 4;
/*     */   public static final int STRING_VAL = 0;
/*     */   public static final int INTEGER_VAL = 1;
/*     */   public static final int FLOAT_VAL = 2;
/*     */   public static final int DATE_VAL = 3;
/*     */   public static final int RESULTSET_VAL = 4;
/*     */   public String m_id;
/*     */   public int m_type;
/*     */   public int m_idContentType;
/*     */   public int m_priority;
/*     */   protected List<GrammarElement> m_subElements;
/*     */   public GrammarElement[] m_subElementArray;
/*     */   public GrammarElement[] m_namedFunctionParameters;
/*     */   public String m_uniOperator;
/*     */   public int m_parseLines;
/*     */   public int m_lineCharOffset;
/*     */   public int m_parseLength;
/*     */   public char m_xxOpFirstChar;
/*     */   public char m_xxOpSecondChar;
/*     */ 
/*     */   public GrammarElement()
/*     */   {
/* 177 */     this.m_type = 0;
/* 178 */     this.m_idContentType = 0;
/* 179 */     this.m_priority = -1;
/*     */   }
/*     */ 
/*     */   public GrammarElement(String id, int type, int line, int offset, int length)
/*     */   {
/* 187 */     this.m_id = id;
/* 188 */     this.m_type = type;
/* 189 */     this.m_idContentType = 0;
/* 190 */     this.m_priority = -1;
/* 191 */     this.m_parseLines = line;
/* 192 */     this.m_lineCharOffset = offset;
/* 193 */     this.m_parseLength = length;
/*     */ 
/* 195 */     if (this.m_type != 3)
/*     */       return;
/* 197 */     this.m_xxOpFirstChar = Character.toLowerCase(this.m_id.charAt(0));
/* 198 */     if (this.m_id.length() <= 1)
/*     */       return;
/* 200 */     this.m_xxOpSecondChar = Character.toLowerCase(this.m_id.charAt(1));
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 211 */     StringBuffer buf = new StringBuffer();
/* 212 */     if (this.m_uniOperator != null)
/*     */     {
/* 214 */       buf.append(this.m_uniOperator);
/*     */     }
/*     */ 
/* 217 */     switch (this.m_type)
/*     */     {
/*     */     case 0:
/* 220 */       if (this.m_subElements != null)
/*     */       {
/* 222 */         appendSubElementStr(buf, this.m_subElements, " "); break label291:
/*     */       }
/*     */ 
/* 226 */       buf.append(this.m_id);
/*     */ 
/* 228 */       break;
/*     */     case 1:
/* 230 */       if (this.m_idContentType == 0)
/*     */       {
/* 232 */         buf.append("\"");
/*     */       }
/*     */       else
/*     */       {
/* 236 */         buf.append('0');
/*     */       }
/* 238 */       buf.append(StringUtils.encodeLiteralStringEscapeSequence(this.m_id));
/* 239 */       if (this.m_idContentType != 0)
/*     */         break label291;
/* 241 */       buf.append("\""); break;
/*     */     case 2:
/* 245 */       buf.append(this.m_id);
/* 246 */       buf.append("(");
/* 247 */       appendSubElementStr(buf, this.m_subElements, ", ");
/* 248 */       buf.append(")");
/* 249 */       break;
/*     */     case 3:
/* 251 */       buf.append("(");
/* 252 */       if (this.m_subElements != null)
/*     */       {
/* 254 */         appendSubElementStr(buf, this.m_subElements, " " + this.m_id + " ");
/*     */       }
/*     */       else
/*     */       {
/* 258 */         buf.append(this.m_id);
/*     */       }
/* 260 */       buf.append(")");
/* 261 */       break;
/*     */     case 4:
/* 263 */       buf.append("(");
/* 264 */       if (this.m_subElements != null)
/*     */       {
/* 266 */         appendSubElementStr(buf, this.m_subElements, ", ");
/*     */       }
/* 268 */       buf.append(")");
/*     */     }
/* 270 */     label291: return buf.toString();
/*     */   }
/*     */ 
/*     */   public void appendSubElementStr(StringBuffer buf, List<GrammarElement> subElements, String separator)
/*     */   {
/* 275 */     for (int i = 0; i < subElements.size(); ++i)
/*     */     {
/* 277 */       GrammarElement elt = (GrammarElement)subElements.get(i);
/* 278 */       buf.append(elt.toString());
/* 279 */       if (i >= subElements.size() - 1)
/*     */         continue;
/* 281 */       buf.append(separator);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 288 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.GrammarElement
 * JD-Core Version:    0.5.4
 */