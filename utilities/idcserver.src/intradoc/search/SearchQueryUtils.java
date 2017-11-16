/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.QueryElementField;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class SearchQueryUtils
/*     */ {
/*  37 */   public static boolean m_isInit = false;
/*  38 */   public static boolean[] m_syncObj = { false };
/*  39 */   public static Map m_operatorLookup = null;
/*     */   public static final int IS_ORING = 1;
/*     */   public static final int IS_NEGATING = 2;
/*     */   public static final int EXPECT_CLOSE_PARENTHESIS = 4;
/*     */   public static final int NO_END_PARENTHESIS = -1;
/*     */   public static final int INVALID_CONJUCTION_OPERATOR = -2;
/*     */   public static final int INVALID_PARENTHESIS = -3;
/*     */   public static final int INVALID_CLAUSE = -4;
/*     */   public static final int INVALID_CONSTRUCTION = -5;
/*  54 */   public static final String[] ERROR_KEYS = { "", "csSearchQueryParserNoEndParenthesis", "csSearchQueryParserInvalidConjuctionOperator", "csSearchQueryParserInvalidParenthesis", "csSearchQueryParserInvalidClause", "csSearchQueryParserInvalidConstruction" };
/*     */ 
/*     */   public static void checkInit()
/*     */   {
/*  69 */     synchronized (m_syncObj)
/*     */     {
/*  71 */       if (!m_isInit)
/*     */       {
/*  73 */         String[] list = UniversalSearchQueryParser.OPERATORKEYS;
/*  74 */         Map m = new HashMap(list.length);
/*  75 */         for (int i = 0; i < list.length; ++i)
/*     */         {
/*  77 */           m.put(list[i], new Integer(i));
/*     */         }
/*  79 */         m_operatorLookup = m;
/*  80 */         m_isInit = true;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static QueryElement createQueryElement(String name, int operator, String originalValue, Object convertedVal, DataBinder binder, CommonSearchConfig searchConfig, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 100 */     QueryElementField qeF = null;
/* 101 */     boolean isCompoundElt = false;
/* 102 */     if ((name != null) && (name.length() > 0))
/*     */     {
/* 104 */       qeF = searchConfig.getQueryElementField(name, binder, cxt);
/*     */     }
/* 108 */     else if (operator != 5)
/*     */     {
/* 110 */       isCompoundElt = true;
/*     */     }
/*     */ 
/* 115 */     QueryElement queryElement = null;
/* 116 */     if (isCompoundElt)
/*     */     {
/* 118 */       queryElement = new QueryElement(null, operator);
/*     */     }
/*     */     else
/*     */     {
/* 122 */       queryElement = new QueryElement(qeF, operator, originalValue, convertedVal);
/*     */     }
/* 124 */     return queryElement;
/*     */   }
/*     */ 
/*     */   public static int convertToOperatorConstant(String operator)
/*     */   {
/* 135 */     Map m = m_operatorLookup;
/* 136 */     if (m == null)
/*     */     {
/* 138 */       checkInit();
/* 139 */       m = m_operatorLookup;
/*     */     }
/* 141 */     Integer iVal = (Integer)m.get(operator);
/* 142 */     int retVal = -1;
/* 143 */     if (iVal != null)
/*     */     {
/* 145 */       retVal = iVal.intValue();
/*     */     }
/* 147 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static ParsedQueryElements lookupSearchParsingObject(ExecutionContext cxt)
/*     */   {
/* 157 */     if (cxt == null)
/*     */     {
/* 159 */       return null;
/*     */     }
/* 161 */     ParsedQueryElements queryElts = null;
/* 162 */     Object queryEltsObj = cxt.getCachedObject("ParsedQueryElements");
/* 163 */     if ((queryEltsObj != null) && (queryEltsObj instanceof ParsedQueryElements))
/*     */     {
/* 165 */       ParsedQueryElements qEltsTemp = (ParsedQueryElements)queryEltsObj;
/* 166 */       if (!qEltsTemp.m_isFinishedParsing)
/*     */       {
/* 168 */         queryElts = qEltsTemp;
/*     */       }
/*     */     }
/* 171 */     return queryElts;
/*     */   }
/*     */ 
/*     */   public static void processSecurityClauseElements(ParsedQueryElements queryElts, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 185 */     queryElts.m_securityClauseElement = compactAndCreateQueryElement(queryElts, cxt);
/*     */   }
/*     */ 
/*     */   public static void processQueryElements(ParsedQueryElements queryElts, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 199 */     queryElts.m_searchQuery = compactAndCreateQueryElement(queryElts, cxt);
/*     */   }
/*     */ 
/*     */   public static QueryElement compactAndCreateQueryElement(ParsedQueryElements queryElts, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 213 */     if (queryElts.m_isError)
/*     */     {
/* 215 */       return null;
/*     */     }
/* 217 */     if (queryElts.m_nameToField == null)
/*     */     {
/* 219 */       queryElts.m_nameToField = new HashMap();
/*     */     }
/* 221 */     QueryElement elt = new QueryElement(new ArrayList());
/* 222 */     compactQueryElements(queryElts, 0, queryElts.m_rawParsedElements, elt, 0, cxt);
/* 223 */     if (!queryElts.m_isError)
/*     */     {
/* 225 */       if (elt.m_subElements.size() == 1)
/*     */       {
/* 228 */         elt = (QueryElement)elt.m_subElements.get(0);
/*     */       }
/*     */ 
/* 232 */       queryElts.m_rawParsedElements.clear();
/*     */     }
/*     */ 
/* 235 */     return elt;
/*     */   }
/*     */ 
/*     */   public static int compactQueryElements(ParsedQueryElements queryElts, int startOffset, List rawElements, QueryElement elt, int flags, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 268 */     int endOffset = startOffset;
/* 269 */     int nRaw = rawElements.size();
/* 270 */     boolean isActiveOr = false;
/* 271 */     boolean foundConjunction = false;
/* 272 */     boolean isNegating = (flags & 0x2) != 0;
/* 273 */     boolean isOriginalNegating = isNegating;
/* 274 */     boolean foundEnd = false;
/* 275 */     boolean endedAnElement = false;
/* 276 */     List unresolvedElements = new ArrayList();
/* 277 */     Map nameToField = queryElts.m_nameToField;
/* 278 */     for (int i = startOffset; i < nRaw; ++i)
/*     */     {
/* 280 */       Object o = rawElements.get(i);
/* 281 */       boolean doingConjunctionOpCode = false;
/* 282 */       boolean doingAddElement = false;
/* 283 */       boolean createSubClauseElement = false;
/* 284 */       int opCode = -1;
/* 285 */       QueryElement activeElt = null;
/* 286 */       if (o instanceof String)
/*     */       {
/* 288 */         String s = (String)o;
/* 289 */         if (s.equals("("))
/*     */         {
/* 291 */           doingAddElement = true;
/* 292 */           createSubClauseElement = true;
/*     */         }
/* 295 */         else if (s.equals(")"))
/*     */         {
/* 297 */           endOffset = i;
/* 298 */           foundEnd = true;
/* 299 */           break;
/*     */         }
/*     */       }
/* 302 */       else if (o instanceof Integer)
/*     */       {
/* 304 */         Integer opI = (Integer)o;
/* 305 */         doingConjunctionOpCode = true;
/* 306 */         opCode = opI.intValue();
/*     */       }
/* 308 */       else if (o instanceof QueryElement)
/*     */       {
/* 310 */         QueryElement qElt = (QueryElement)o;
/* 311 */         if (qElt.m_type == 101)
/*     */         {
/* 313 */           doingConjunctionOpCode = true;
/* 314 */           opCode = qElt.m_operator;
/*     */         }
/*     */         else
/*     */         {
/* 318 */           activeElt = qElt;
/* 319 */           doingAddElement = true;
/* 320 */           QueryElementField queryField = qElt.m_field;
/* 321 */           if ((queryField != null) && (queryField.m_name != null))
/*     */           {
/* 323 */             Integer fieldIndex = (Integer)nameToField.get(queryField.m_name);
/* 324 */             if (fieldIndex == null)
/*     */             {
/* 326 */               int newIndex = nameToField.size();
/* 327 */               fieldIndex = new Integer(newIndex);
/* 328 */               nameToField.put(queryField.m_name, fieldIndex);
/*     */             }
/* 330 */             qElt.m_fieldIndexIntoQueryList = fieldIndex.intValue();
/*     */           }
/* 332 */           if (qElt.m_operator == 5)
/*     */           {
/* 334 */             queryElts.m_hasFullTextElement = true;
/*     */           }
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 340 */         queryElts.setError(-5, i);
/* 341 */         break;
/*     */       }
/*     */ 
/* 344 */       if (doingConjunctionOpCode)
/*     */       {
/* 346 */         if (opCode == 18)
/*     */         {
/* 348 */           if (endedAnElement)
/*     */           {
/* 350 */             queryElts.setError(-2, i);
/* 351 */             break;
/*     */           }
/* 353 */           isNegating = !isNegating;
/*     */         }
/* 355 */         else if ((opCode == 17) || (opCode == 16))
/*     */         {
/* 357 */           if (!endedAnElement)
/*     */           {
/* 359 */             queryElts.setError(-2, i);
/* 360 */             break;
/*     */           }
/*     */ 
/* 365 */           isNegating = isOriginalNegating;
/*     */ 
/* 367 */           boolean isOrCode = opCode == 17;
/*     */ 
/* 370 */           if (!foundConjunction)
/*     */           {
/* 372 */             elt.m_operator = opCode;
/*     */           }
/* 374 */           else if (isActiveOr != isOrCode)
/*     */           {
/* 379 */             elt.m_operator = 17;
/*     */ 
/* 384 */             int nUnresolved = unresolvedElements.size();
/* 385 */             QueryElement remainder = null;
/* 386 */             if (isOrCode)
/*     */             {
/* 390 */               QueryElement combinedAndsElt = new QueryElement(new ArrayList());
/* 391 */               combinedAndsElt.m_subElements.addAll(unresolvedElements);
/* 392 */               if (isNegating)
/*     */               {
/* 395 */                 combinedAndsElt.m_operator = 17;
/*     */               }
/* 397 */               elt.m_subElements.add(combinedAndsElt);
/*     */             }
/*     */             else
/*     */             {
/* 403 */               remainder = (QueryElement)unresolvedElements.remove(nUnresolved - 1);
/*     */ 
/* 407 */               elt.m_subElements.addAll(unresolvedElements);
/*     */             }
/* 409 */             unresolvedElements.clear();
/* 410 */             if (remainder != null)
/*     */             {
/* 412 */               unresolvedElements.add(remainder);
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 418 */           isActiveOr = isOrCode;
/*     */ 
/* 421 */           foundConjunction = true;
/*     */         }
/*     */         else
/*     */         {
/* 425 */           queryElts.setError(-5, i);
/* 426 */           break;
/*     */         }
/* 428 */         endedAnElement = false;
/*     */       }
/*     */ 
/* 431 */       if ((doingAddElement) && (endedAnElement))
/*     */       {
/* 433 */         queryElts.setError(-4, i);
/* 434 */         break;
/*     */       }
/*     */ 
/* 437 */       boolean extractElements = false;
/* 438 */       if (createSubClauseElement)
/*     */       {
/* 440 */         activeElt = new QueryElement(new ArrayList());
/* 441 */         int flagsParam = flags;
/* 442 */         if (isNegating)
/*     */         {
/* 444 */           flagsParam |= 2;
/*     */         }
/*     */         else
/*     */         {
/* 448 */           flagsParam &= -3;
/*     */         }
/* 450 */         flagsParam |= 4;
/* 451 */         i = compactQueryElements(queryElts, i + 1, rawElements, activeElt, flagsParam, cxt);
/* 452 */         if (queryElts.m_isError) {
/*     */           break;
/*     */         }
/*     */ 
/* 456 */         List subElts = activeElt.m_subElements;
/* 457 */         int nSubElts = subElts.size();
/* 458 */         extractElements = true;
/* 459 */         if (nSubElts > 1)
/*     */         {
/* 463 */           boolean isSubOr = activeElt.m_operator == 17;
/* 464 */           if (isNegating != isOriginalNegating)
/*     */           {
/* 466 */             isSubOr = !isSubOr;
/*     */           }
/* 468 */           if (isSubOr != isActiveOr)
/*     */           {
/* 470 */             extractElements = false;
/*     */           }
/*     */         }
/* 473 */         if ((extractElements) && 
/* 475 */           (nSubElts > 0))
/*     */         {
/* 477 */           unresolvedElements.addAll(subElts);
/*     */         }
/*     */       }
/*     */ 
/* 481 */       if (!doingAddElement)
/*     */         continue;
/* 483 */       if (!extractElements)
/*     */       {
/* 485 */         if (isNegating)
/*     */         {
/* 491 */           flipOperator(activeElt);
/*     */         }
/* 493 */         unresolvedElements.add(activeElt);
/*     */       }
/* 495 */       endedAnElement = true;
/*     */     }
/*     */ 
/* 500 */     if ((!queryElts.m_isError) && (unresolvedElements.size() > 0))
/*     */     {
/* 502 */       boolean createNewElement = false;
/* 503 */       if (elt.m_subElements.size() > 0)
/*     */       {
/* 507 */         boolean eltIsOr = elt.m_operator == 17;
/* 508 */         if (eltIsOr != isActiveOr)
/*     */         {
/* 510 */           createNewElement = true;
/*     */         }
/*     */       }
/* 513 */       if (createNewElement)
/*     */       {
/* 515 */         QueryElement combinedAndsElt = new QueryElement(new ArrayList());
/* 516 */         combinedAndsElt.m_subElements.addAll(unresolvedElements);
/* 517 */         if (isOriginalNegating != isActiveOr)
/*     */         {
/* 520 */           combinedAndsElt.m_operator = 17;
/*     */         }
/* 522 */         elt.m_subElements.add(combinedAndsElt);
/*     */       }
/*     */       else
/*     */       {
/* 526 */         elt.m_subElements.addAll(unresolvedElements);
/*     */ 
/* 530 */         if (isActiveOr)
/*     */         {
/* 532 */           elt.m_operator = 17;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 538 */     if (!queryElts.m_isError)
/*     */     {
/* 540 */       boolean expectingClose = (flags & 0x4) != 0;
/* 541 */       if ((foundEnd) && (!endedAnElement) && (foundConjunction))
/*     */       {
/* 543 */         queryElts.setError(-3, endOffset - 1);
/*     */       }
/* 545 */       else if (expectingClose != foundEnd)
/*     */       {
/* 547 */         int badIndex = (foundEnd) ? endOffset - 1 : startOffset;
/* 548 */         queryElts.setError(-3, badIndex);
/*     */       }
/* 550 */       else if (!foundEnd)
/*     */       {
/* 552 */         endOffset = nRaw;
/*     */       }
/*     */     }
/* 555 */     return endOffset;
/*     */   }
/*     */ 
/*     */   public static String createErrorReport(ParsedQueryElements queryElts)
/*     */   {
/* 564 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 565 */     appendErrorReport(queryElts, buf);
/* 566 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public static void appendErrorReport(ParsedQueryElements queryElts, IdcAppendable appendable)
/*     */   {
/* 577 */     int[] queryStrOffset = { 0 };
/* 578 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 579 */     computeRawDisplayStringAndOffset(buf, queryElts.m_rawParsedElements, queryElts.m_rawElementIndex, queryStrOffset);
/*     */ 
/* 581 */     queryElts.m_rawElementDebugStringIndex = queryStrOffset[0];
/* 582 */     queryElts.m_rawParsedElementsDebugString = buf.toString();
/*     */ 
/* 584 */     String msg = LocaleUtils.encodeMessage("csSearchQueryParserErrorAtIndex", null, queryElts.m_rawParsedElementsDebugString, "" + queryElts.m_rawElementDebugStringIndex);
/*     */ 
/* 586 */     String errKey = getParsingErrorKey(queryElts.m_errorReason);
/* 587 */     msg = LocaleUtils.encodeMessage(errKey, msg);
/* 588 */     appendable.append(msg);
/*     */   }
/*     */ 
/*     */   public static void computeRawDisplayStringAndOffset(IdcAppendable buf, List rawElements, int rawIndex, int[] retOffset)
/*     */   {
/* 602 */     int nraw = rawElements.size();
/*     */ 
/* 604 */     boolean needSpacing = false;
/*     */ 
/* 606 */     for (int i = 0; i < nraw; ++i)
/*     */     {
/* 608 */       if ((i == rawIndex) && 
/* 610 */         (retOffset != null) && (retOffset.length > 0) && (buf instanceof CharSequence))
/*     */       {
/* 614 */         CharSequence seq = (CharSequence)buf;
/* 615 */         retOffset[0] = seq.length();
/*     */       }
/*     */ 
/* 618 */       Object o = rawElements.get(i);
/* 619 */       String s = null;
/* 620 */       if (o instanceof String)
/*     */       {
/* 622 */         s = (String)o;
/*     */       }
/* 624 */       if ((needSpacing) && (((s == null) || (!s.equals(")")))))
/*     */       {
/* 626 */         buf.append(" ");
/*     */       }
/* 628 */       needSpacing = (s == null) || (!s.equals("("));
/* 629 */       if (o == null)
/*     */       {
/* 631 */         buf.append("null");
/*     */       }
/* 633 */       else if (s != null)
/*     */       {
/* 635 */         buf.append(s);
/*     */       }
/* 637 */       else if (o instanceof Integer)
/*     */       {
/* 639 */         Integer opCode = (Integer)o;
/* 640 */         String op = convertToString(opCode.intValue());
/* 641 */         buf.append(op);
/*     */       }
/* 643 */       else if (o instanceof QueryElement)
/*     */       {
/* 645 */         QueryElement qElt = (QueryElement)o;
/* 646 */         qElt.appendDebugFormat(buf);
/*     */       }
/*     */       else
/*     */       {
/* 650 */         buf.append("invalidObject");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void flipOperator(QueryElement elt)
/*     */   {
/* 662 */     if (elt.m_type == 101)
/*     */     {
/* 664 */       boolean isCurrentOr = (elt.m_operator & 0xFF) == 17;
/*     */ 
/* 666 */       int newOp = (isCurrentOr) ? 16 : 17;
/*     */ 
/* 668 */       elt.m_operator = (elt.m_operator & 0xFFFFFF00 | newOp);
/*     */     }
/*     */     else
/*     */     {
/* 673 */       elt.m_operator ^= 256;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getParsingErrorKey(int errCode)
/*     */   {
/* 684 */     int index = -errCode;
/* 685 */     String result = "csSearchQueryParserUnknownError";
/* 686 */     if ((index > 0) && (index < ERROR_KEYS.length))
/*     */     {
/* 688 */       result = ERROR_KEYS[index];
/*     */     }
/* 690 */     return result;
/*     */   }
/*     */ 
/*     */   public static String convertToString(int opCode)
/*     */   {
/* 700 */     int index = opCode & 0xFF;
/* 701 */     if ((index < 0) || (index > UniversalSearchQueryParser.OPERATORKEYS.length))
/*     */     {
/* 703 */       return "invalidOperator";
/*     */     }
/* 705 */     return UniversalSearchQueryParser.OPERATORKEYS[index];
/*     */   }
/*     */ 
/*     */   public static boolean hasNotFlag(int opCode)
/*     */   {
/* 716 */     return (opCode & 0x100) != 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 721 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86059 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchQueryUtils
 * JD-Core Version:    0.5.4
 */