/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class DynamicDataUtils
/*     */ {
/*     */   public static final int F_DEFAULT_PROCESSING = 0;
/*     */   public static final int F_INSIDE_TAG_ALREADY = 1;
/*     */   public static final int F_ALLOW_MINUS_SIGNATURE_ON_ATTRIBUTES = 2;
/*     */   public static final int F_ADD_NO_REPLACE = 1;
/*  36 */   public static final char[] STANDARD_BEGIN_FORMAT_TAG = { '<', '?' };
/*     */ 
/*  41 */   public static final char[] STANDARD_END_FORMAT_TAG = { '?', '>' };
/*     */   public static final char DEFAULT_MERGE_APPEND_SEPARATOR = ':';
/*     */   public static final char DEFAULT_MERGE_APPEND_EQUALS = '=';
/*     */ 
/*     */   public static boolean parseSpecialXmlTag(char[] startSequence, char[] endSequence, char[] data, int start, int end, DynamicDataHandleAttribute handler, int[] endIndex, int flags)
/*     */   {
/*  72 */     boolean retVal = false;
/*  73 */     boolean startingInsideTag = (flags & 0x1) != 0;
/*  74 */     boolean allowMinusSignatureOnAttributes = (flags & 0x2) != 0;
/*  75 */     boolean insideTag = startingInsideTag;
/*  76 */     int startTagIndex = -1;
/*  77 */     boolean startingTagIndex = false;
/*  78 */     boolean insideAttribute = false;
/*  79 */     int insideAttributeStart = -1;
/*  80 */     boolean lookingForAttributeValue = false;
/*  81 */     boolean lookingForEquals = false;
/*  82 */     boolean insideAttributeValue = false;
/*  83 */     int insideValueStart = -1;
/*  84 */     boolean insideLiteral = false;
/*  85 */     char literalChar = ' ';
/*  86 */     String activeAttributeKey = null;
/*  87 */     int finalIndex = end;
/*  88 */     int endLoopIndex = end;
/*  89 */     if (endSequence == null)
/*     */     {
/*  93 */       endLoopIndex = end + 1;
/*     */     }
/*     */ 
/*  96 */     for (int i = start; i < endLoopIndex; ++i)
/*     */     {
/*  98 */       char ch = (i < end) ? data[i] : '\000';
/*  99 */       if (!insideTag)
/*     */       {
/* 102 */         if ((ch != startSequence[0]) || 
/* 104 */           (!lookAheadMatch(startSequence, data, i, end)))
/*     */           continue;
/* 106 */         retVal = true;
/* 107 */         insideTag = true;
/* 108 */         startingTagIndex = true;
/* 109 */         startTagIndex = i + startSequence.length;
/* 110 */         i = startTagIndex - 1;
/*     */       }
/* 116 */       else if (!insideLiteral)
/*     */       {
/* 120 */         if ((((i == end) || ((endSequence != null) && (ch == endSequence[0])))) && ((
/* 122 */           (endSequence == null) || (lookAheadMatch(endSequence, data, i, end)))))
/*     */         {
/* 125 */           if (startingTagIndex)
/*     */           {
/* 127 */             String tag = new String(data, startTagIndex, i - startTagIndex);
/* 128 */             handler.handleTag(tag);
/* 129 */             startingTagIndex = false;
/*     */           }
/*     */ 
/* 133 */           String attribute = null;
/* 134 */           String value = null;
/* 135 */           if (insideAttribute)
/*     */           {
/* 137 */             if ((!lookingForAttributeValue) && (!insideAttributeValue))
/*     */             {
/* 139 */               attribute = new String(data, insideAttributeStart, i - insideAttributeStart);
/*     */             }
/*     */             else
/*     */             {
/* 143 */               attribute = activeAttributeKey;
/* 144 */               if (insideAttributeValue)
/*     */               {
/* 146 */                 value = new String(data, insideValueStart, i - insideValueStart);
/*     */               }
/* 148 */               lookingForAttributeValue = false;
/* 149 */               insideAttributeValue = false;
/*     */             }
/* 151 */             if (attribute != null)
/*     */             {
/* 153 */               handler.handleValue(attribute, value);
/*     */             }
/* 155 */             insideAttribute = false;
/*     */           }
/* 157 */           insideTag = false;
/* 158 */           finalIndex = i;
/* 159 */           if (endSequence == null)
/*     */             break;
/* 161 */           finalIndex += endSequence.length; break;
/*     */         }
/*     */ 
/* 166 */         if (startingTagIndex)
/*     */         {
/* 168 */           if (ch > ' ') {
/*     */             continue;
/*     */           }
/* 171 */           String tag = new String(data, startTagIndex, i - startTagIndex);
/* 172 */           handler.handleTag(tag);
/* 173 */           startingTagIndex = false;
/*     */         }
/*     */         else
/*     */         {
/* 180 */           boolean isCharOrDigit = Character.isJavaIdentifierPart(ch);
/* 181 */           if (!insideAttribute)
/*     */           {
/* 184 */             if ((isCharOrDigit) || ((allowMinusSignatureOnAttributes) && (ch == '-')))
/*     */             {
/* 187 */               insideAttribute = true;
/* 188 */               insideAttributeStart = i;
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/* 194 */             if ((!lookingForAttributeValue) && (!insideAttributeValue) && 
/* 197 */               (!isCharOrDigit))
/*     */             {
/* 200 */               activeAttributeKey = new String(data, insideAttributeStart, i - insideAttributeStart);
/* 201 */               lookingForEquals = true;
/* 202 */               lookingForAttributeValue = true;
/*     */             }
/*     */ 
/* 205 */             if (lookingForAttributeValue)
/*     */             {
/* 207 */               if (lookingForEquals)
/*     */               {
/* 209 */                 if (isCharOrDigit)
/*     */                 {
/* 213 */                   handler.handleValue(activeAttributeKey, null);
/* 214 */                   lookingForAttributeValue = false;
/* 215 */                   lookingForEquals = false;
/* 216 */                   insideAttributeStart = i;
/*     */                 }
/* 218 */                 else if (ch == '=')
/*     */                 {
/* 221 */                   lookingForEquals = false;
/*     */                 }
/*     */ 
/*     */               }
/* 227 */               else if ((ch == '\'') || (ch == '"') || (isCharOrDigit))
/*     */               {
/* 229 */                 insideValueStart = i;
/* 230 */                 if (!isCharOrDigit)
/*     */                 {
/* 234 */                   insideLiteral = true;
/* 235 */                   literalChar = ch;
/* 236 */                   ++insideValueStart;
/*     */                 }
/* 238 */                 lookingForAttributeValue = false;
/* 239 */                 insideAttributeValue = true;
/*     */               }
/*     */ 
/*     */             }
/* 246 */             else if (!isCharOrDigit)
/*     */             {
/* 249 */               String value = new String(data, insideValueStart, i - insideValueStart);
/* 250 */               handler.handleValue(activeAttributeKey, value);
/* 251 */               insideAttribute = false;
/* 252 */               insideAttributeValue = false;
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 260 */         if (ch != literalChar) {
/*     */           continue;
/*     */         }
/* 263 */         String value = new String(data, insideValueStart, i - insideValueStart);
/* 264 */         handler.handleValue(activeAttributeKey, value);
/* 265 */         insideAttribute = false;
/* 266 */         insideAttributeValue = false;
/* 267 */         insideLiteral = false;
/* 268 */         literalChar = ' ';
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 275 */     String errMsg = null;
/* 276 */     String errKey = null;
/* 277 */     if (startingTagIndex)
/*     */     {
/* 279 */       errKey = "insidetagdeclaration";
/* 280 */       errMsg = "Starting tag never closed";
/* 281 */       activeAttributeKey = null;
/* 282 */       insideAttributeStart = -1;
/* 283 */       finalIndex = startTagIndex;
/*     */     }
/* 285 */     else if (insideAttribute)
/*     */     {
/* 287 */       finalIndex = insideAttributeStart;
/* 288 */       if (insideLiteral)
/*     */       {
/* 290 */         errKey = "insideliteral";
/* 291 */         errMsg = "Literal character " + literalChar + " was never closed starting at offset " + insideValueStart;
/* 292 */         finalIndex = insideValueStart;
/*     */       }
/* 294 */       else if (lookingForEquals)
/*     */       {
/* 296 */         errKey = "lookingforequals";
/* 297 */         errMsg = "Never found = after attribute name " + activeAttributeKey;
/*     */       }
/* 299 */       else if (lookingForAttributeValue)
/*     */       {
/* 301 */         errKey = "lookingforattributevalue";
/* 302 */         errMsg = "Never found value after attribute name " + activeAttributeKey;
/*     */       }
/*     */       else
/*     */       {
/* 306 */         finalIndex = insideValueStart;
/* 307 */         errKey = "insideattributevalue";
/* 308 */         errMsg = "Never terminated value for attribute name " + activeAttributeKey;
/*     */       }
/*     */     }
/* 311 */     else if (insideTag)
/*     */     {
/* 313 */       errKey = "insideattribute";
/* 314 */       errMsg = "Looking for an attribute";
/* 315 */       activeAttributeKey = null;
/* 316 */       insideAttributeStart = -1;
/*     */     }
/* 318 */     if (errMsg != null)
/*     */     {
/* 320 */       handler.handleError(errKey, errMsg, insideAttributeStart, activeAttributeKey, start, end, data);
/*     */     }
/* 322 */     if ((retVal) && (endIndex != null) && (endIndex.length > 0))
/*     */     {
/* 324 */       if (finalIndex < start + 1)
/*     */       {
/* 326 */         finalIndex = start + 1;
/*     */       }
/* 328 */       if (finalIndex <= end)
/*     */       {
/* 330 */         endIndex[0] = finalIndex;
/*     */       }
/*     */     }
/*     */ 
/* 334 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static boolean lookAheadMatch(char[] seq, char[] data, int start, int end)
/*     */   {
/* 354 */     int endLookAhead = start + seq.length;
/* 355 */     if (endLookAhead > end)
/*     */     {
/* 357 */       return false;
/*     */     }
/* 359 */     int index = 1;
/* 360 */     boolean matchedIt = true;
/* 361 */     for (int j = start + 1; (j < endLookAhead) && (j < end); ++j)
/*     */     {
/* 363 */       if (data[j] == seq[(index++)])
/*     */         continue;
/* 365 */       matchedIt = false;
/*     */     }
/*     */ 
/* 368 */     return matchedIt;
/*     */   }
/*     */ 
/*     */   public static Table applyColumnRenaming(Table curTable, Object[] args, int argIndex, String[] inclusionFilterNameParam, List[] includeColumnsParam, DynamicHtmlMerger merger, ExecutionContext context)
/*     */     throws ParseSyntaxException
/*     */   {
/* 391 */     int endArgs = args.length - 1;
/* 392 */     if ((argIndex >= endArgs) || (curTable == null) || (curTable.m_colNames == null) || (curTable.m_colNames.length == 0))
/*     */     {
/* 394 */       return null;
/*     */     }
/* 396 */     String otherTableKey = ScriptUtils.getDisplayString(args[(argIndex++)], context);
/* 397 */     if (otherTableKey.length() == 0)
/*     */     {
/* 399 */       return null;
/*     */     }
/* 401 */     DynamicData result = merger.getDynamicDataResource(otherTableKey, null);
/* 402 */     if (result == null)
/*     */     {
/* 404 */       return null;
/*     */     }
/*     */ 
/* 409 */     inclusionFilterNameParam[0] = result.m_inclusionFilterName;
/* 410 */     includeColumnsParam[0] = result.m_includeColumns;
/*     */ 
/* 412 */     Table otherTable = null;
/* 413 */     String colName = null;
/* 414 */     String value = null;
/* 415 */     if (argIndex + 2 <= endArgs)
/*     */     {
/* 417 */       colName = ScriptUtils.getDisplayString(args[(argIndex++)], context);
/* 418 */       value = ScriptUtils.getDisplayString(args[(argIndex++)], context);
/*     */     }
/* 420 */     if ((colName != null) && (colName.length() > 0))
/*     */     {
/* 422 */       otherTable = result.getIndexedTable(colName, value);
/*     */     }
/*     */     else
/*     */     {
/* 426 */       otherTable = result.m_mergedTable;
/*     */     }
/* 428 */     if ((otherTable == null) || (otherTable.m_colNames == null) || (otherTable.m_colNames.length == 0) || (otherTable.m_rows.size() == 0))
/*     */     {
/* 431 */       return null;
/*     */     }
/*     */ 
/* 435 */     Table newTable = curTable.shallowClone();
/* 436 */     String[] colNames = newTable.m_colNames;
/* 437 */     String[] oldNames = otherTable.m_colNames;
/* 438 */     String[] newNames = (String[])(String[])otherTable.m_rows.get(0);
/* 439 */     for (int i = 0; i < colNames.length; ++i)
/*     */     {
/* 441 */       String colNameTest = colNames[i];
/* 442 */       for (int j = 0; j < newNames.length; ++j)
/*     */       {
/* 444 */         if ((!colNameTest.equalsIgnoreCase(oldNames[j])) || (newNames[j].length() <= 0))
/*     */           continue;
/* 446 */         colNames[i] = newNames[j];
/* 447 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 452 */     return newTable;
/*     */   }
/*     */ 
/*     */   public static List mergeObjectListsNoDuplicates(List newData, List mergedData, int flags)
/*     */   {
/* 466 */     if (newData == null)
/*     */     {
/* 468 */       return mergedData;
/*     */     }
/*     */     List retData;
/* 471 */     if (mergedData != null)
/*     */     {
/* 473 */       boolean isAddOnly = (flags & 0x1) != 0;
/* 474 */       List retData = mergedData;
/* 475 */       for (int i = 0; i < newData.size(); ++i)
/*     */       {
/* 477 */         Object o1 = newData.get(i);
/*     */         String s1;
/*     */         String s1;
/* 479 */         if (o1 instanceof String[])
/*     */         {
/* 481 */           s1 = ((String[])(String[])o1)[0];
/*     */         }
/*     */         else
/*     */         {
/*     */           String s1;
/* 483 */           if (o1 instanceof DynamicDataDerivedColumn)
/*     */           {
/* 485 */             s1 = ((DynamicDataDerivedColumn)o1).m_name;
/*     */           }
/*     */           else
/*     */           {
/* 489 */             s1 = (String)o1;
/*     */           }
/*     */         }
/* 491 */         boolean foundIt = false;
/* 492 */         for (int j = 0; j < mergedData.size(); ++j)
/*     */         {
/* 494 */           Object o2 = mergedData.get(j);
/*     */           String s2;
/*     */           String s2;
/* 496 */           if (o2 instanceof String[])
/*     */           {
/* 498 */             s2 = ((String[])(String[])o2)[0];
/*     */           }
/*     */           else
/*     */           {
/*     */             String s2;
/* 500 */             if (o2 instanceof DynamicDataDerivedColumn)
/*     */             {
/* 502 */               s2 = ((DynamicDataDerivedColumn)o2).m_name;
/*     */             }
/*     */             else
/*     */             {
/* 506 */               s2 = (String)o2;
/*     */             }
/*     */           }
/* 508 */           if (!s1.equalsIgnoreCase(s2))
/*     */             continue;
/* 510 */           foundIt = true;
/* 511 */           if (isAddOnly)
/*     */             continue;
/* 513 */           if ((o1 instanceof DynamicDataDerivedColumn) && (o2 instanceof DynamicDataDerivedColumn))
/*     */           {
/* 516 */             DynamicDataDerivedColumn newColumns = (DynamicDataDerivedColumn)o1;
/* 517 */             DynamicDataDerivedColumn originalColumns = (DynamicDataDerivedColumn)o2;
/* 518 */             originalColumns.mergeIfNewIsNotNull(newColumns, 0);
/*     */           }
/*     */           else
/*     */           {
/* 523 */             mergedData.set(j, o1);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 528 */         if (foundIt)
/*     */           continue;
/* 530 */         retData.add(o1);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 536 */       retData = new ArrayList(newData.size());
/* 537 */       for (int i = 0; i < newData.size(); ++i)
/*     */       {
/* 539 */         Object o = newData.get(i);
/* 540 */         if (o instanceof DynamicDataDerivedColumn)
/*     */         {
/* 545 */           o = ((DynamicDataDerivedColumn)o).shallowClone();
/*     */         }
/* 547 */         retData.add(o);
/*     */       }
/*     */     }
/* 550 */     return retData;
/*     */   }
/*     */ 
/*     */   public static String mergeAppendColumnValue(String mergedColumnValue, char sep, char eq)
/*     */   {
/* 560 */     if ((mergedColumnValue == null) || (mergedColumnValue.length() == 0))
/*     */     {
/* 562 */       return mergedColumnValue;
/*     */     }
/*     */ 
/* 565 */     List values = StringUtils.makeListFromSequence(mergedColumnValue, sep, '*', 0);
/* 566 */     List newValues = new ArrayList(values.size());
/* 567 */     for (String val : values)
/*     */     {
/* 569 */       boolean isNegated = false;
/* 570 */       if (val.startsWith("~"))
/*     */       {
/* 572 */         isNegated = true;
/* 573 */         val = val.substring(1);
/*     */       }
/* 575 */       int valLen = val.length();
/* 576 */       int endTestChar = val.indexOf(eq);
/* 577 */       if (endTestChar < 0)
/*     */       {
/* 579 */         endTestChar = valLen;
/*     */       }
/*     */ 
/* 582 */       boolean foundIt = false;
/* 583 */       int valIndex = 0;
/* 584 */       for (String newVal : newValues)
/*     */       {
/* 586 */         if (newVal.regionMatches(0, val, 0, endTestChar))
/*     */         {
/* 588 */           if (newVal.length() == endTestChar)
/*     */           {
/* 590 */             foundIt = true;
/*     */           }
/*     */           else
/*     */           {
/* 596 */             char ch = newVal.charAt(endTestChar);
/* 597 */             if (ch == eq)
/*     */             {
/* 599 */               foundIt = true;
/*     */             }
/*     */           }
/*     */         }
/* 603 */         if (foundIt) {
/*     */           break;
/*     */         }
/*     */ 
/* 607 */         ++valIndex;
/*     */       }
/* 609 */       if (foundIt)
/*     */       {
/* 611 */         if (isNegated)
/*     */         {
/* 614 */           newValues.remove(valIndex);
/*     */         }
/* 616 */         else if (endTestChar != valLen)
/*     */         {
/* 619 */           newValues.set(valIndex, val);
/*     */         }
/*     */ 
/*     */       }
/* 624 */       else if (!isNegated)
/*     */       {
/* 626 */         newValues.add(val);
/*     */       }
/*     */     }
/*     */ 
/* 630 */     return StringUtils.createString(newValues, sep, '*');
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 636 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 67644 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicDataUtils
 * JD-Core Version:    0.5.4
 */