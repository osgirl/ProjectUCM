/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ 
/*     */ public class InternetSearchQuery
/*     */ {
/*     */   public static final int NONE = -1;
/*     */   public static final int AND = 0;
/*     */   public static final int OR = 1;
/*     */   public static final int NOT = 2;
/*     */   public static final int ANDNOT = 3;
/*     */   public static final int OPEN_PAREN = 10;
/*     */   public static final int CLOSE_PAREN = 11;
/*     */   public static final int VALUE = 20;
/*     */   public static final int PHRASE = 21;
/*     */   public static final char PHRASE_ESCAPE_CHAR = '"';
/*     */   public static final char WC_ANY = '*';
/*     */   public static final char WC_ONE = '?';
/*     */   protected ArrayList m_segments;
/*     */   protected String m_wcAny;
/*     */   protected String m_wcOne;
/*     */   protected char m_wcFullTextAny;
/*     */   protected char m_wcFullTextOne;
/*     */   protected CommonSearchConfig m_config;
/*     */   protected char[] m_originalQuery;
/*     */   protected boolean m_containsAnd;
/*     */   protected ParsedQueryElements m_parsedElements;
/*     */   protected DataBinder m_binder;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected boolean m_useFullTextPhraseEnclosure;
/*     */   protected boolean m_useFullTextWordEnclosure;
/*     */   protected boolean m_useFullTextWildCardEnclosure;
/*     */ 
/*     */   public InternetSearchQuery()
/*     */   {
/*  47 */     this.m_segments = new ArrayList();
/*     */ 
/*  55 */     this.m_config = null;
/*  56 */     this.m_originalQuery = null;
/*     */ 
/*  58 */     this.m_containsAnd = false;
/*     */ 
/*  69 */     this.m_useFullTextPhraseEnclosure = false;
/*  70 */     this.m_useFullTextWordEnclosure = false;
/*  71 */     this.m_useFullTextWildCardEnclosure = false;
/*     */   }
/*     */ 
/*     */   public void init(CommonSearchConfig cfg, ParsedQueryElements parsedElts, char[] origQuery, DataBinder binder, ExecutionContext cxt)
/*     */   {
/*  77 */     String wcAny = cfg.getEngineValue("WildCardAny");
/*  78 */     String wcOne = cfg.getEngineValue("WildCardOne");
/*  79 */     if (wcAny != null)
/*     */     {
/*  81 */       this.m_wcAny = wcAny;
/*     */     }
/*  83 */     if (wcOne != null)
/*     */     {
/*  85 */       this.m_wcOne = wcOne;
/*     */     }
/*  87 */     this.m_config = cfg;
/*  88 */     this.m_parsedElements = parsedElts;
/*     */ 
/*  90 */     String tmpValue = cfg.getEngineValue("UseFullTextPhraseEnclosure");
/*  91 */     this.m_useFullTextPhraseEnclosure = StringUtils.convertToBool(tmpValue, false);
/*     */ 
/*  93 */     tmpValue = cfg.getEngineValue("UseFullTextValueEnclosure");
/*  94 */     this.m_useFullTextWordEnclosure = StringUtils.convertToBool(tmpValue, false);
/*     */ 
/*  96 */     tmpValue = cfg.getEngineValue("UseFullTextWildCardValueEnclosure");
/*  97 */     this.m_useFullTextWildCardEnclosure = StringUtils.convertToBool(tmpValue, false);
/*  98 */     this.m_binder = binder;
/*  99 */     this.m_cxt = cxt;
/*     */   }
/*     */ 
/*     */   public InternetSearchQueryField insertSegment(int type, String value)
/*     */   {
/* 104 */     InternetSearchQueryField field = new InternetSearchQueryField(this.m_wcAny, this.m_wcOne);
/* 105 */     field.setType(type);
/* 106 */     field.setValue(value);
/* 107 */     this.m_segments.add(field);
/* 108 */     if (type == 0)
/*     */     {
/* 110 */       this.m_containsAnd = true;
/*     */     }
/*     */ 
/* 113 */     return field;
/*     */   }
/*     */ 
/*     */   public InternetSearchQueryField insertSegment(int type, char[] value, int beginIndex, int len)
/*     */   {
/* 118 */     InternetSearchQueryField field = new InternetSearchQueryField(this.m_wcAny, this.m_wcOne);
/* 119 */     field.setType(type);
/* 120 */     field.setValue(value, beginIndex, len);
/* 121 */     this.m_segments.add(field);
/* 122 */     if (type == 0)
/*     */     {
/* 124 */       this.m_containsAnd = true;
/*     */     }
/* 126 */     return field;
/*     */   }
/*     */ 
/*     */   public void appendQuery(IdcAppendable appendable, String field, String operator, String engineName)
/*     */     throws DataException, ServiceException
/*     */   {
/* 132 */     appendQuery(appendable, field, operator, engineName, false);
/*     */   }
/*     */ 
/*     */   public void appendQuery(IdcAppendable appendable, String field, String operator, String engineName, boolean useProcessedValue)
/*     */     throws DataException, ServiceException
/*     */   {
/* 138 */     Iterator it = this.m_segments.iterator();
/* 139 */     boolean hasNext = it.hasNext();
/* 140 */     while (hasNext)
/*     */     {
/* 142 */       InternetSearchQueryField esqField = (InternetSearchQueryField)it.next();
/*     */ 
/* 144 */       boolean isOpSet = false;
/* 145 */       int opCode = -1;
/* 146 */       String op = null;
/* 147 */       hasNext = it.hasNext();
/* 148 */       switch (esqField.m_type)
/*     */       {
/*     */       case 0:
/* 151 */         if (!hasNext) {
/*     */           continue;
/*     */         }
/*     */ 
/* 155 */         opCode = 16;
/* 156 */         op = "and";
/* 157 */         isOpSet = true;
/*     */       case 1:
/* 159 */         if (!hasNext) {
/*     */           continue;
/*     */         }
/*     */ 
/* 163 */         if (!isOpSet)
/*     */         {
/* 165 */           isOpSet = true;
/* 166 */           opCode = 17;
/* 167 */           op = "or";
/*     */         }
/*     */       case 2:
/* 170 */         if (!isOpSet)
/*     */         {
/* 172 */           opCode = 18;
/* 173 */           op = "not";
/*     */         }
/* 175 */         if (this.m_parsedElements != null)
/*     */         {
/* 177 */           this.m_parsedElements.m_rawParsedElements.add(new Integer(opCode));
/*     */         }
/* 179 */         appendable.append(' ');
/* 180 */         this.m_config.appendClauseElement(appendable, op, engineName, null, null);
/* 181 */         appendable.append(' ');
/* 182 */         break;
/*     */       case 10:
/* 184 */         appendable.append(' ');
/* 185 */         appendable.append('(');
/* 186 */         if (this.m_parsedElements != null)
/*     */         {
/* 188 */           this.m_parsedElements.m_rawParsedElements.add("("); } break;
/*     */       case 11:
/* 192 */         appendable.append(')');
/* 193 */         if (this.m_parsedElements != null)
/*     */         {
/* 195 */           this.m_parsedElements.m_rawParsedElements.add(")"); } break;
/*     */       case 20:
/*     */       case 21:
/* 200 */         char[] segmentValue = esqField.m_values;
/* 201 */         int length = esqField.m_length;
/*     */ 
/* 203 */         if (useProcessedValue == true)
/*     */         {
/* 205 */           segmentValue = esqField.getValue(useProcessedValue, operator);
/* 206 */           length = segmentValue.length;
/*     */         }
/* 208 */         String tmp = "";
/* 209 */         if (segmentValue != null) {
/* 210 */           tmp = new String(segmentValue, 0, length);
/*     */         }
/* 212 */         if (this.m_parsedElements != null)
/*     */         {
/* 214 */           int clauseOpCode = SearchQueryUtils.convertToOperatorConstant(operator);
/* 215 */           QueryElement qElt = SearchQueryUtils.createQueryElement(field, clauseOpCode, tmp, null, this.m_binder, this.m_config, this.m_cxt);
/*     */ 
/* 217 */           this.m_parsedElements.m_rawParsedElements.add(qElt); } this.m_config.appendClauseElement(appendable, operator, engineName, field, tmp);
/*     */       case 3:
/*     */       case 4:
/*     */       case 5:
/*     */       case 6:
/*     */       case 7:
/*     */       case 8:
/*     */       case 9:
/*     */       case 12:
/*     */       case 13:
/*     */       case 14:
/*     */       case 15:
/*     */       case 16:
/*     */       case 17:
/*     */       case 18:
/*     */       case 19: }  }  } 
/* 226 */   public void appendFullTextQuery(IdcAppendable appendable, String queryDefLabel, DataBinder binder, ExecutionContext cxt) throws DataException { appendFullTextQuery(appendable, queryDefLabel, binder, cxt, false); }
/*     */ 
/*     */   public void appendFullTextQuery(IdcAppendable appendable, String queryDefLabel, DataBinder binder, ExecutionContext cxt, boolean useProcessedValue)
/*     */     throws DataException
/*     */   {
/* 231 */     appendFullTextQuery(appendable, queryDefLabel, binder, cxt, "fullText", useProcessedValue, false, false);
/*     */   }
/*     */ 
/*     */   public void appendFullTextQuery(IdcAppendable appendable, String queryDefLabel, DataBinder binder, ExecutionContext cxt, String operator, boolean useProcessedValue, boolean doAppendOnlyValue, boolean escapeExtraFormatting)
/*     */     throws DataException
/*     */   {
/* 252 */     boolean useFullTextPhraseEnclosure = DataBinderUtils.getBoolean(binder, "UseFullTextPhraseEnclosure", this.m_useFullTextPhraseEnclosure);
/*     */ 
/* 254 */     boolean useFullTextWordEnclosure = DataBinderUtils.getBoolean(binder, "UseFullTextValueEnclosure", this.m_useFullTextWordEnclosure);
/*     */ 
/* 256 */     boolean useFullTextWildCardEnclosure = DataBinderUtils.getBoolean(binder, "UseFullTextWildCardValueEnclosure", this.m_useFullTextWildCardEnclosure);
/*     */ 
/* 258 */     char[][] textExtra = (char[][])(char[][])cxt.getCachedObject("SearchQueryTextExtras");
/* 259 */     if (textExtra == null)
/*     */     {
/* 261 */       String msg = LocaleUtils.encodeMessage("csSearchQueryTextExtrasUnavailable", null);
/* 262 */       throw new DataException(msg);
/*     */     }
/* 264 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 265 */     Iterator it = this.m_segments.iterator();
/*     */ 
/* 267 */     boolean useWordEnclosure = false;
/* 268 */     boolean isEndSegClause = false;
/*     */ 
/* 271 */     char[][] wildCards = { textExtra[4], textExtra[5] };
/*     */ 
/* 274 */     InternetSearchQueryField nextField = null;
/* 275 */     while ((it.hasNext()) || (nextField != null))
/*     */     {
/* 277 */       InternetSearchQueryField esqField = nextField;
/* 278 */       if (esqField == null)
/*     */       {
/* 280 */         esqField = (InternetSearchQueryField)it.next();
/*     */       }
/*     */       else
/*     */       {
/* 284 */         nextField = null;
/*     */       }
/*     */ 
/* 287 */       boolean isOpSet = false;
/* 288 */       boolean isValue = esqField.m_type == 20;
/* 289 */       boolean allowResetEndClause = true;
/* 290 */       char[] op = null;
/* 291 */       switch (esqField.m_type)
/*     */       {
/*     */       case 0:
/* 294 */         if (!it.hasNext())
/*     */           continue;
/* 296 */         nextField = (InternetSearchQueryField)it.next();
/*     */ 
/* 302 */         if ((nextField == null) || (nextField.m_type != 2))
/*     */         {
/* 304 */           op = textExtra[0];
/* 305 */           isOpSet = true;
/*     */         }
/*     */         else
/*     */         {
/* 309 */           isEndSegClause = true;
/* 310 */           allowResetEndClause = false;
/* 311 */         }break;
/*     */       case 1:
/* 314 */         if (!isOpSet)
/*     */         {
/* 316 */           if (!it.hasNext()) {
/*     */             continue;
/*     */           }
/*     */ 
/* 320 */           op = textExtra[1];
/* 321 */           isOpSet = true;
/*     */         }
/*     */       case 2:
/* 324 */         if (!isOpSet)
/*     */         {
/* 326 */           if (!it.hasNext()) {
/*     */             continue;
/*     */           }
/*     */ 
/* 330 */           if (isEndSegClause)
/*     */           {
/* 333 */             op = textExtra[2];
/*     */           }
/*     */           else
/*     */           {
/* 338 */             op = textExtra[3];
/*     */           }
/*     */         }
/*     */ 
/* 342 */         if (escapeExtraFormatting == true)
/*     */         {
/* 344 */           builder.append(op);
/*     */         }
/*     */         else
/*     */         {
/* 348 */           builder.append(' ');
/* 349 */           builder.append(op);
/* 350 */           builder.append(' ');
/*     */         }
/* 352 */         break;
/*     */       case 10:
/* 354 */         if (!escapeExtraFormatting)
/*     */         {
/* 356 */           builder.append(' ');
/*     */         }
/* 358 */         builder.append('(');
/* 359 */         break;
/*     */       case 11:
/* 361 */         builder.append(')');
/* 362 */         isEndSegClause = true;
/* 363 */         allowResetEndClause = false;
/* 364 */         break;
/*     */       case 20:
/* 366 */         if ((useFullTextWordEnclosure) || ((useFullTextWildCardEnclosure) && (esqField.containsWildCard())))
/*     */         {
/* 368 */           useWordEnclosure = true;
/*     */         }
/*     */       case 21:
/* 371 */         if ((!isValue) && (useFullTextPhraseEnclosure))
/*     */         {
/* 373 */           useWordEnclosure = true;
/*     */         }
/* 375 */         char[] value = esqField.getTextValue(wildCards, useProcessedValue, operator);
/* 376 */         if ((builder.length() != 0) && (!escapeExtraFormatting))
/*     */         {
/* 378 */           builder.append(' ');
/*     */         }
/* 380 */         if (useWordEnclosure)
/*     */         {
/* 382 */           builder.append("\"");
/*     */         }
/* 384 */         builder.append(value);
/* 385 */         if (useWordEnclosure)
/*     */         {
/* 387 */           builder.append("\""); } 
/*     */ isEndSegClause = true;
/* 391 */         allowResetEndClause = false;
/*     */       case 3:
/*     */       case 4:
/*     */       case 5:
/*     */       case 6:
/*     */       case 7:
/*     */       case 8:
/*     */       case 9:
/*     */       case 12:
/*     */       case 13:
/*     */       case 14:
/*     */       case 15:
/*     */       case 16:
/*     */       case 17:
/*     */       case 18:
/*     */       case 19: } if (allowResetEndClause)
/*     */       {
/* 396 */         isEndSegClause = false;
/*     */       }
/*     */     }
/*     */ 
/* 400 */     if (doAppendOnlyValue == true)
/*     */     {
/* 402 */       appendable.append(builder.toString());
/*     */     }
/*     */     else
/*     */     {
/* 406 */       this.m_config.appendClauseElement(appendable, operator, queryDefLabel, "", builder);
/*     */     }
/* 408 */     builder.releaseBuffers();
/*     */   }
/*     */ 
/*     */   public boolean containsAnd()
/*     */   {
/* 413 */     return this.m_containsAnd;
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/* 418 */     boolean isEmpty = false;
/* 419 */     if (this.m_segments.size() == 0)
/*     */     {
/* 421 */       isEmpty = true;
/*     */     }
/* 423 */     return isEmpty;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 428 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98365 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.InternetSearchQuery
 * JD-Core Version:    0.5.4
 */