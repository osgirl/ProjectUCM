/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.VersionInfo;
/*     */ import java.lang.reflect.Field;
/*     */ import java.text.ParseException;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class DataFormatUtils
/*     */ {
/*     */   public static final int F_IS_FIRST = 1;
/*     */   public static final int F_TO_STRING = 2;
/*  48 */   protected static final Object[] DEFAULT_DATABINDER_NAME = { DataFormat.DEFINE_DATABINDER_NAME, DataFormat.T_NAME };
/*     */ 
/*  50 */   protected static final Object[] DEFAULT_RESULT_SET_FIELD = { DataFormat.DEFINE_RESULT_SET_FIELD, DataFormat.T_NAME };
/*     */ 
/*  52 */   protected static final Object[] DEFAULT_RESULT_SET_ROW = { DataFormat.DEFINE_RESULT_SET_ROW, DataFormat.T_CELLS };
/*     */ 
/*  54 */   protected static final Object[] DEFAULT_RESULT_SET_CELL = { DataFormat.DEFINE_RESULT_SET_CELL, DataFormat.T_VALUE };
/*     */   protected static Class m_clDataFormat;
/*     */   protected static Map<String, DataFormat> m_formats;
/*     */   protected static Map<String, Map<Integer, Object[]>> m_formatDefinedTokens;
/*     */   protected static Map<Integer, String> m_tokenNames;
/*     */ 
/*     */   public static void init()
/*     */   {
/*  68 */     if (null != m_formats)
/*     */     {
/*  70 */       return;
/*     */     }
/*  72 */     m_formatDefinedTokens = new ConcurrentHashMap();
/*  73 */     m_formats = new ConcurrentHashMap();
/*  74 */     registerFormat("hda", "intradoc.data.DataFormatHDA");
/*  75 */     registerFormat("html", "intradoc.data.DataFormatHTML");
/*  76 */     registerFormat("json", "intradoc.data.DataFormatJson");
/*  77 */     registerFormat("text", "intradoc.data.DataFormatText");
/*     */   }
/*     */ 
/*     */   public static void registerFormat(String formatName, String className)
/*     */   {
/*  88 */     Class c = null;
/*     */     try
/*     */     {
/*  91 */       if (null == m_clDataFormat)
/*     */       {
/*  93 */         m_clDataFormat = Class.forName("intradoc.data.DataFormat");
/*     */       }
/*  95 */       c = Class.forName(className);
/*  96 */       if (!m_clDataFormat.isAssignableFrom(c))
/*     */       {
/*  98 */         Report.warning("system", "!$" + className + " is not a DataFormat class", null);
/*  99 */         return;
/*     */       }
/*     */     }
/*     */     catch (ClassNotFoundException e)
/*     */     {
/* 104 */       Report.error("system", "!$Missing DataFormat class", e);
/* 105 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 109 */       DataFormat dft = (DataFormat)c.newInstance();
/* 110 */       m_formats.put(formatName, dft);
/*     */ 
/* 112 */       Map tokens = new HashMap();
/* 113 */       Object[][] tokensArray = dft.getTokens();
/* 114 */       for (int i = 0; i < tokensArray.length; ++i)
/*     */       {
/* 116 */         Object token = tokensArray[i][0];
/* 117 */         if (!token instanceof Integer)
/*     */           continue;
/* 119 */         tokens.put((Integer)tokensArray[i][0], tokensArray[i]);
/*     */       }
/*     */ 
/* 122 */       m_formatDefinedTokens.put(formatName, tokens);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 126 */       Report.error("system", "!$Unable to instantiate " + className, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void computeTokenNames()
/*     */   {
/* 272 */     if (null == m_clDataFormat)
/*     */     {
/* 274 */       return;
/*     */     }
/*     */     Class clInteger;
/*     */     try
/*     */     {
/* 279 */       clInteger = Class.forName("java.lang.Integer");
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 283 */       Report.error(null, null, e);
/* 284 */       return;
/*     */     }
/* 286 */     Map names = new HashMap();
/* 287 */     Field[] fields = m_clDataFormat.getFields();
/* 288 */     for (Field field : fields)
/*     */     {
/* 290 */       int modifiers = field.getModifiers();
/* 291 */       if ((0 == (modifiers & 0x1)) || (0 == (modifiers & 0x8)) || (0 == (modifiers & 0x10))) continue; if (field.getType() != clInteger)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 300 */         Integer integer = (Integer)field.get(null);
/* 301 */         names.put(integer, field.getName());
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/*     */       }
/*     */     }
/*     */ 
/* 308 */     m_tokenNames = names;
/*     */   }
/*     */ 
/*     */   protected static void appendToken(IdcAppendable str, Object token)
/*     */   {
/* 319 */     if (token instanceof String)
/*     */     {
/* 321 */       str.append('"');
/* 322 */       str.append((String)token);
/* 323 */       str.append('"');
/*     */     }
/* 325 */     else if (token instanceof Integer)
/*     */     {
/* 327 */       if (null == m_tokenNames)
/*     */       {
/* 329 */         computeTokenNames();
/*     */       }
/* 331 */       String name = (null == m_tokenNames) ? null : (String)m_tokenNames.get(token);
/* 332 */       if (null == name)
/*     */       {
/* 334 */         name = "?";
/*     */       }
/* 336 */       str.append(name);
/*     */     }
/*     */     else
/*     */     {
/* 340 */       str.append(token.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static String makeStringFromTokens(Object[] tokens, int[] ptr)
/*     */   {
/* 355 */     if ((null == tokens) || (tokens.length < 1))
/*     */     {
/* 357 */       return "";
/*     */     }
/* 359 */     int index = -1;
/* 360 */     if (null != ptr)
/*     */     {
/* 362 */       index = ptr[0];
/*     */     }
/* 364 */     IdcStringBuilder str = new IdcStringBuilder("[ Token ");
/* 365 */     if (index == 0)
/*     */     {
/* 367 */       ptr[0] = str.m_length;
/*     */     }
/* 369 */     appendToken(str, tokens[0]);
/* 370 */     str.append(": ");
/* 371 */     for (int i = 1; i < tokens.length; ++i)
/*     */     {
/* 373 */       if (i > 1)
/*     */       {
/* 375 */         str.append(", ");
/*     */       }
/* 377 */       if (i == index)
/*     */       {
/* 379 */         ptr[0] = str.m_length;
/*     */       }
/* 381 */       appendToken(str, tokens[i]);
/*     */     }
/* 383 */     if (index == tokens.length)
/*     */     {
/* 385 */       ptr[0] = str.m_length;
/*     */     }
/* 387 */     str.append(']');
/* 388 */     return str.toString();
/*     */   }
/*     */ 
/*     */   public static void appendDataBinder(DataFormatter formatter, String desc, DataBinder binder, int flags)
/*     */   {
/* 403 */     IdcStringBuilder builder = formatter.m_output;
/* 404 */     DataFormat format = formatter.m_format;
/* 405 */     Map definedTokens = formatter.m_definedTokens;
/* 406 */     Object[] binderTokens = (Object[])definedTokens.get(DataFormat.DEFINE_DATABINDER);
/* 407 */     if (null == binderTokens)
/*     */       return;
/* 409 */     Object[] nameTokens = (Object[])definedTokens.get(DataFormat.DEFINE_DATABINDER_NAME);
/* 410 */     boolean hasDesc = (desc != null) && (desc.length() > 0);
/* 411 */     TokenProcessor process = new TokenProcessor(binderTokens);
/* 412 */     TokenProcessor processDesc = new TokenProcessor((null != nameTokens) ? nameTokens : DEFAULT_DATABINDER_NAME);
/*     */ 
/* 415 */     while (null != (token = process.getNext(builder)))
/*     */     {
/*     */       Integer token;
/* 417 */       if (token == DataFormat.T_NAME)
/*     */       {
/* 419 */         if (!hasDesc)
/*     */           continue;
/* 421 */         processDesc.reset();
/*     */         while (true) { if (null != (token = processDesc.getNext(builder)));
/* 424 */           if (token != DataFormat.T_NAME)
/*     */             continue;
/* 426 */           format.appendLiteral(builder, desc); }
/*     */ 
/*     */ 
/*     */       }
/*     */ 
/* 431 */       if (token == DataFormat.T_LOCAL_DATA)
/*     */       {
/* 433 */         appendProperties(formatter, "LocalData", binder.m_localData, 0);
/*     */       }
/* 435 */       if (token == DataFormat.T_ENVIRONMENT)
/*     */       {
/* 437 */         if (!formatter.m_showEnv)
/*     */           continue;
/* 439 */         appendProperties(formatter, "Environment", binder.m_environment, 0);
/*     */       }
/*     */ 
/* 442 */       if (token == DataFormat.IF_HAS_RESULTSET)
/*     */       {
/* 444 */         process.startConditional(!binder.m_resultSets.isEmpty());
/*     */       }
/* 446 */       if (token == DataFormat.T_RESULT_SETS)
/*     */       {
/* 448 */         Set keyset = binder.m_resultSets.keySet();
/* 449 */         String[] keys = new String[keyset.size()];
/* 450 */         keyset.toArray(keys);
/* 451 */         Arrays.sort(keys);
/* 452 */         for (int i = 0; i < keys.length; ++i)
/*     */         {
/* 454 */           String name = keys[i];
/* 455 */           ResultSet rset = (ResultSet)binder.m_resultSets.get(name);
/* 456 */           if (null == rset) {
/*     */             continue;
/*     */           }
/*     */ 
/* 460 */           if (i == 0)
/*     */           {
/* 462 */             flags |= 1;
/*     */           }
/*     */           else
/*     */           {
/* 466 */             flags &= -2;
/*     */           }
/* 468 */           appendResultSet(formatter, name, rset, flags);
/*     */         }
/*     */       }
/* 471 */       if (token == DataFormat.T_PRODUCT_VERSION)
/*     */       {
/* 473 */         String version = VersionInfo.getProductVersion();
/* 474 */         builder.append(version);
/*     */       }
/* 476 */       if (token != DataFormat.T_SYSTEM_ISO_ENCODING)
/*     */         continue;
/* 478 */       String sysEncoding = DataSerializeUtils.getSystemEncoding();
/* 479 */       String isoEncoding = DataSerializeUtils.getIsoEncoding(sysEncoding);
/* 480 */       builder.append(isoEncoding);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void appendProperties(DataFormatter formatter, String name, Properties props, int flags)
/*     */   {
/* 496 */     IdcStringBuilder builder = formatter.m_output;
/* 497 */     DataFormat format = formatter.m_format;
/* 498 */     Map definedTokens = formatter.m_definedTokens;
/* 499 */     Object[] propTokens = (Object[])definedTokens.get(DataFormat.DEFINE_PROPERTIES);
/* 500 */     Object[] pairTokens = (Object[])definedTokens.get(DataFormat.DEFINE_PROPERTY_PAIR);
/* 501 */     if ((null == propTokens) || (null == pairTokens))
/*     */       return;
/* 503 */     Enumeration allKeysEnum = props.propertyNames();
/* 504 */     List allKeysList = Collections.list(allKeysEnum);
/* 505 */     int allKeysCount = allKeysList.size();
/* 506 */     String[] allKeys = new String[allKeysCount];
/* 507 */     allKeysList.toArray(allKeys);
/* 508 */     Arrays.sort(allKeys);
/*     */ 
/* 510 */     Enumeration realKeysEnum = props.keys();
/* 511 */     List realKeysList = Collections.list(realKeysEnum);
/* 512 */     Set realKeys = new HashSet(realKeysList);
/* 513 */     int realKeysCount = realKeys.size();
/* 514 */     int defaultKeysCount = allKeysCount - realKeysCount;
/*     */ 
/* 516 */     int defaultsThreshold = (formatter.m_defaultsThreshold > 0) ? formatter.m_defaultsThreshold : DataFormatter.DEFAULT_NUM_PROPS_DEFAULTS_THRESHOLD;
/*     */ 
/* 518 */     boolean showDefaults = defaultKeysCount < defaultsThreshold;
/* 519 */     TokenProcessor process = new TokenProcessor(propTokens);
/* 520 */     TokenProcessor processPair = new TokenProcessor(pairTokens);
/*     */ 
/* 522 */     while (null != (token = process.getNext(builder)))
/*     */     {
/*     */       Integer token;
/* 524 */       if (token == DataFormat.T_NAME)
/*     */       {
/* 526 */         format.appendLiteral(builder, name);
/*     */       }
/* 528 */       if (token == DataFormat.T_PAIRS)
/*     */       {
/* 530 */         int i = 0; for (int n = 0; i < allKeysCount; ++n)
/*     */         {
/* 532 */           String key = allKeys[i];
/* 533 */           boolean isDefault = !realKeys.contains(key);
/* 534 */           if ((!isDefault) || (showDefaults))
/*     */           {
/* 538 */             String value = props.getProperty(key);
/* 539 */             processPair.reset();
/* 540 */             while (null != (token = processPair.getNext(builder)))
/*     */             {
/* 542 */               if (token == DataFormat.T_NAME)
/*     */               {
/* 544 */                 format.appendLiteral(builder, key);
/*     */               }
/* 546 */               if (token == DataFormat.T_VALUE)
/*     */               {
/* 548 */                 format.appendLiteral(builder, value);
/*     */               }
/* 550 */               if (token == DataFormat.IF_NOT_FIRST)
/*     */               {
/* 552 */                 processPair.startConditional(n > 0);
/*     */               }
/* 554 */               if (token == DataFormat.IF_IS_DEFAULT)
/*     */               {
/* 556 */                 processPair.startConditional(isDefault);
/*     */               }
/* 558 */               if (token != DataFormat.IF_HAS_VALUE)
/*     */                 continue;
/* 560 */               if ((value != null) && (value.length() > 0))
/*     */               {
/* 562 */                 processPair.startConditional(true);
/*     */               }
/*     */ 
/* 566 */               --n;
/* 567 */               processPair.startConditional(false);
/*     */             }
/*     */           }
/* 530 */           ++i;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 573 */       if (token == DataFormat.T_COUNT_TOTAL)
/*     */       {
/* 575 */         builder.append(allKeysCount);
/*     */       }
/* 577 */       if (token == DataFormat.T_COUNT_DEFAULT)
/*     */       {
/* 579 */         builder.append(defaultKeysCount);
/*     */       }
/* 581 */       if (token == DataFormat.T_COUNT_DEFINED)
/*     */       {
/* 583 */         builder.append(realKeysCount);
/*     */       }
/* 585 */       if (token != DataFormat.IF_SHOW_DEFAULTS)
/*     */         continue;
/* 587 */       process.startConditional(showDefaults);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void appendResultSet(DataFormatter formatter, String name, ResultSet rset, int flags)
/*     */   {
/* 604 */     boolean isFirst = (flags & 0x1) != 0;
/* 605 */     boolean toString = (flags & 0x2) != 0;
/* 606 */     boolean isDead = false;
/* 607 */     IdcStringBuilder builder = formatter.m_output;
/* 608 */     DataFormat format = formatter.m_format;
/* 609 */     Map definedTokens = formatter.m_definedTokens;
/* 610 */     Object[] rsetTokens = (Object[])definedTokens.get(DataFormat.DEFINE_RESULT_SET);
/* 611 */     if (null == rsetTokens)
/*     */       return;
/* 613 */     MutableResultSet mrset = (rset instanceof MutableResultSet) ? (MutableResultSet)rset : null;
/* 614 */     boolean onlyCurrentRow = (0 == formatter.m_numRowsThreshold) || (null == mrset);
/* 615 */     int numFields = rset.getNumFields();
/* 616 */     if (numFields < 0)
/*     */     {
/* 618 */       isDead = true;
/* 619 */       numFields = 0;
/*     */     }
/* 621 */     String[] fieldNames = new String[numFields];
/* 622 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 624 */       fieldNames[i] = rset.getFieldName(i);
/*     */     }
/* 626 */     boolean hasRow = rset.isRowPresent();
/* 627 */     int numRows = (null == mrset) ? 0 : (hasRow) ? 1 : mrset.getNumRows();
/* 628 */     int limitRows = numRows;
/* 629 */     if ((formatter.m_numRowsThreshold > 0) && (numRows > formatter.m_numRowsThreshold))
/*     */     {
/* 631 */       limitRows = formatter.m_numRowsThreshold;
/*     */     }
/* 633 */     if ((mrset != null) && (limitRows + formatter.m_startRow >= numRows))
/*     */     {
/* 635 */       limitRows = numRows - formatter.m_startRow;
/* 636 */       if (limitRows < 0)
/*     */       {
/* 638 */         limitRows = 0;
/*     */       }
/*     */     }
/* 641 */     int currentRow = (null == mrset) ? -1 : mrset.getCurrentRow();
/*     */ 
/* 643 */     Object[] fieldTokens = (Object[])definedTokens.get(DataFormat.DEFINE_RESULT_SET_FIELD);
/* 644 */     Object[] rowTokens = (Object[])definedTokens.get(DataFormat.DEFINE_RESULT_SET_ROW);
/* 645 */     Object[] cellTokens = (Object[])definedTokens.get(DataFormat.DEFINE_RESULT_SET_CELL);
/* 646 */     TokenProcessor process = new TokenProcessor(rsetTokens);
/* 647 */     TokenProcessor processField = new TokenProcessor((null != fieldTokens) ? fieldTokens : DEFAULT_RESULT_SET_FIELD);
/*     */ 
/* 649 */     TokenProcessor processRow = new TokenProcessor((null != rowTokens) ? rowTokens : DEFAULT_RESULT_SET_ROW);
/*     */ 
/* 651 */     TokenProcessor processCell = new TokenProcessor((null != cellTokens) ? cellTokens : DEFAULT_RESULT_SET_CELL);
/*     */ 
/* 654 */     while (null != (token = process.getNext(builder)))
/*     */     {
/*     */       Integer token;
/* 656 */       if (token == DataFormat.T_NAME)
/*     */       {
/* 658 */         format.appendLiteral(builder, name);
/*     */       }
/* 660 */       if (token == DataFormat.T_FIELDS)
/*     */       {
/* 662 */         for (int i = 0; i < numFields; ++i)
/*     */         {
/* 664 */           processField.reset();
/* 665 */           while (null != (token = processField.getNext(builder)))
/*     */           {
/* 667 */             if (token == DataFormat.T_NAME)
/*     */             {
/* 669 */               format.appendLiteral(builder, fieldNames[i]);
/*     */             }
/* 671 */             if (token == DataFormat.T_FIELD_INDEX)
/*     */             {
/* 673 */               builder.append(i);
/*     */             }
/* 675 */             if (token != DataFormat.IF_NOT_FIRST)
/*     */               continue;
/* 677 */             processField.startConditional(i > 0);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 682 */       if (token == DataFormat.T_ROWS)
/*     */       {
/* 684 */         if (onlyCurrentRow)
/*     */         {
/* 686 */           if (!hasRow)
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 691 */           List row = ((toString) && (null != mrset)) ? mrset.getCurrentRowAsList() : null;
/* 692 */           processRow.reset();
/* 693 */           while (null != (token = processRow.getNext(builder)))
/*     */           {
/* 695 */             if (token == DataFormat.T_CELLS)
/*     */             {
/* 697 */               int i = 0; for (int n = 0; i < numFields; ++n)
/*     */               {
/*     */                 String value;
/*     */                 String value;
/* 700 */                 if (toString)
/*     */                 {
/*     */                   String value;
/* 702 */                   if (null == row)
/*     */                   {
/* 704 */                     value = "";
/*     */                   }
/*     */                   else
/*     */                   {
/* 708 */                     value = row.get(i).toString();
/*     */                   }
/*     */                 }
/*     */                 else
/*     */                 {
/* 713 */                   value = rset.getStringValue(i);
/*     */                 }
/* 715 */                 processCell.reset();
/* 716 */                 while (null != (token = processCell.getNext(builder)))
/*     */                 {
/* 718 */                   if (token == DataFormat.T_NAME)
/*     */                   {
/* 720 */                     format.appendLiteral(builder, fieldNames[i]);
/*     */                   }
/* 722 */                   if (token == DataFormat.T_VALUE)
/*     */                   {
/* 724 */                     format.appendLiteral(builder, value);
/*     */                   }
/* 726 */                   if (token == DataFormat.T_FIELD_INDEX)
/*     */                   {
/* 728 */                     builder.append(i);
/*     */                   }
/* 730 */                   if (token == DataFormat.IF_NOT_FIRST)
/*     */                   {
/* 732 */                     processCell.startConditional(i > 0);
/*     */                   }
/* 734 */                   if (token == DataFormat.IF_SHOW_CURRENT_ONLY)
/*     */                   {
/* 736 */                     processCell.startConditional(onlyCurrentRow);
/*     */                   }
/* 738 */                   if (token == DataFormat.IF_HAS_ROW)
/*     */                   {
/* 740 */                     processCell.startConditional(hasRow);
/*     */                   }
/* 742 */                   if (token != DataFormat.IF_HAS_VALUE)
/*     */                     continue;
/* 744 */                   if ((value != null) && (value.length() > 0))
/*     */                   {
/* 746 */                     processCell.startConditional(true);
/*     */                   }
/*     */ 
/* 750 */                   --n;
/* 751 */                   processCell.startConditional(false);
/*     */                 }
/* 697 */                 ++i;
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/* 757 */             if (token == DataFormat.IF_NOT_FIRST)
/*     */             {
/* 759 */               processRow.startConditional(false);
/*     */             }
/* 761 */             if (token != DataFormat.IF_HAS_ROW)
/*     */               continue;
/* 763 */             processRow.startConditional(hasRow);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 770 */         Object[] rowValues = new Object[numFields];
/* 771 */         for (int j = 0; j < limitRows; ++j)
/*     */         {
/* 773 */           List row = mrset.getRowAsList(j + formatter.m_startRow);
/* 774 */           row.toArray(rowValues);
/* 775 */           processRow.reset();
/* 776 */           while (null != (token = processRow.getNext(builder)))
/*     */           {
/* 778 */             if (token == DataFormat.T_CELLS)
/*     */             {
/* 780 */               int i = 0; for (int n = 0; i < numFields; ++n)
/*     */               {
/* 782 */                 Object cell = rowValues[i];
/* 783 */                 String value = "";
/* 784 */                 if (cell != null)
/*     */                 {
/* 786 */                   value = (cell instanceof String) ? (String)cell : cell.toString();
/*     */                 }
/*     */ 
/* 789 */                 processCell.reset();
/* 790 */                 while (null != (token = processCell.getNext(builder)))
/*     */                 {
/* 792 */                   if (token == DataFormat.T_NAME)
/*     */                   {
/* 794 */                     format.appendLiteral(builder, fieldNames[i]);
/*     */                   }
/* 796 */                   if (token == DataFormat.T_VALUE)
/*     */                   {
/* 798 */                     format.appendLiteral(builder, value);
/*     */                   }
/* 800 */                   if (token == DataFormat.T_ROW_INDEX)
/*     */                   {
/* 802 */                     builder.append(j + formatter.m_startRow);
/*     */                   }
/* 804 */                   if (token == DataFormat.T_FIELD_INDEX)
/*     */                   {
/* 806 */                     builder.append(i);
/*     */                   }
/* 808 */                   if (token == DataFormat.IF_NOT_FIRST)
/*     */                   {
/* 810 */                     processCell.startConditional(i > 0);
/*     */                   }
/* 812 */                   if (token == DataFormat.IF_SHOW_CURRENT_ONLY)
/*     */                   {
/* 814 */                     processCell.startConditional(onlyCurrentRow);
/*     */                   }
/* 816 */                   if (token == DataFormat.IF_HAS_ROW)
/*     */                   {
/* 818 */                     processCell.startConditional(hasRow);
/*     */                   }
/* 820 */                   if (token != DataFormat.IF_HAS_VALUE)
/*     */                     continue;
/* 822 */                   if ((value != null) && (value.length() > 0))
/*     */                   {
/* 824 */                     processCell.startConditional(true);
/*     */                   }
/*     */ 
/* 828 */                   --n;
/* 829 */                   processCell.startConditional(false);
/*     */                 }
/* 780 */                 ++i;
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/* 835 */             if (token == DataFormat.T_ROW_INDEX)
/*     */             {
/* 837 */               builder.append(j + formatter.m_startRow);
/*     */             }
/* 839 */             if (token == DataFormat.IF_NOT_FIRST)
/*     */             {
/* 841 */               processRow.startConditional(j > 0);
/*     */             }
/* 843 */             if (token != DataFormat.IF_HAS_ROW)
/*     */               continue;
/* 845 */             processRow.startConditional(hasRow);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 851 */       if (token == DataFormat.T_COUNT_FIELDS)
/*     */       {
/* 853 */         builder.append(numFields);
/*     */       }
/* 855 */       if (token == DataFormat.T_COUNT_ROWS)
/*     */       {
/* 857 */         builder.append(numRows);
/*     */       }
/* 859 */       if (token == DataFormat.T_COUNT_ROWS_LIMIT)
/*     */       {
/* 861 */         builder.append(limitRows);
/*     */       }
/* 863 */       if (token == DataFormat.T_ROW_INDEX_CURRENT)
/*     */       {
/* 865 */         builder.append(currentRow);
/*     */       }
/* 867 */       if (token == DataFormat.T_ROW_INDEX_FIRST)
/*     */       {
/* 869 */         builder.append(formatter.m_startRow);
/*     */       }
/* 871 */       if (token == DataFormat.T_ROW_INDEX_LAST)
/*     */       {
/* 873 */         builder.append(formatter.m_startRow + limitRows - 1);
/*     */       }
/* 875 */       if (token == DataFormat.IF_NOT_FIRST)
/*     */       {
/* 877 */         process.startConditional(!isFirst);
/*     */       }
/* 879 */       if (token == DataFormat.IF_IS_SEEKABLE)
/*     */       {
/* 881 */         process.startConditional(null != mrset);
/*     */       }
/* 883 */       if (token == DataFormat.IF_SHOW_CURRENT_ONLY)
/*     */       {
/* 885 */         process.startConditional(onlyCurrentRow);
/*     */       }
/* 887 */       if (token == DataFormat.IF_SHOW_LIMITED)
/*     */       {
/* 889 */         process.startConditional(limitRows < numRows);
/*     */       }
/* 891 */       if (token == DataFormat.IF_HAS_ROW)
/*     */       {
/* 893 */         process.startConditional(hasRow);
/*     */       }
/* 895 */       if (token != DataFormat.IF_DEAD)
/*     */         continue;
/* 897 */       process.startConditional(isDead);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 906 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98395 $";
/*     */   }
/*     */ 
/*     */   protected static class TokenProcessor
/*     */   {
/*     */     Object[] m_tokens;
/*     */     int m_index;
/*     */     Conditional m_conditional;
/*     */ 
/*     */     public TokenProcessor(Object[] tokens)
/*     */     {
/* 152 */       this.m_tokens = tokens;
/*     */     }
/*     */ 
/*     */     public void reset()
/*     */     {
/* 158 */       this.m_index = 0;
/* 159 */       this.m_conditional = null;
/*     */     }
/*     */ 
/*     */     public void startConditional(boolean isTrue)
/*     */     {
/* 169 */       Conditional conditional = new Conditional();
/* 170 */       conditional.m_previous = this.m_conditional;
/* 171 */       conditional.m_isTrue = isTrue;
/* 172 */       conditional.m_inElse = false;
/* 173 */       conditional.m_ignore = (!isTrue);
/* 174 */       if (null != conditional.m_previous)
/*     */       {
/* 177 */         conditional.m_ignore |= this.m_conditional.m_ignore;
/*     */       }
/* 179 */       this.m_conditional = conditional;
/*     */     }
/*     */ 
/*     */     protected Integer getNext(IdcStringBuilder builder)
/*     */     {
/* 191 */       if (this.m_index == 0)
/*     */       {
/* 194 */         this.m_index += 1;
/*     */       }
/* 196 */       while (this.m_index < this.m_tokens.length)
/*     */       {
/* 198 */         Object token = this.m_tokens[(this.m_index++)];
/* 199 */         if (token == DataFormat.ELSE)
/*     */         {
/* 201 */           if ((null == this.m_conditional) || (this.m_conditional.m_inElse))
/*     */           {
/* 203 */             int[] index = { --this.m_index };
/* 204 */             String tokenStr = DataFormatUtils.makeStringFromTokens(this.m_tokens, index);
/* 205 */             ParseException e = new ParseException(tokenStr, index[0]);
/* 206 */             Report.debug("systemparse", "ELSE without IF_*", e);
/* 207 */             return null;
/*     */           }
/* 209 */           this.m_conditional.m_inElse = true;
/* 210 */           this.m_conditional.m_ignore = this.m_conditional.m_isTrue;
/* 211 */           if (null == this.m_conditional.m_previous)
/*     */             continue;
/* 213 */           this.m_conditional.m_ignore |= this.m_conditional.m_previous.m_ignore;
/*     */         }
/*     */ 
/* 217 */         if (token == DataFormat.ENDIF)
/*     */         {
/* 219 */           if (null == this.m_conditional)
/*     */           {
/* 221 */             int[] index = { --this.m_index };
/* 222 */             String tokenStr = DataFormatUtils.makeStringFromTokens(this.m_tokens, index);
/* 223 */             ParseException e = new ParseException(tokenStr, index[0]);
/* 224 */             Report.debug("systemparse", "ENDIF without IF_*", e);
/* 225 */             return null;
/*     */           }
/* 227 */           this.m_conditional = this.m_conditional.m_previous;
/*     */         }
/*     */ 
/* 230 */         if ((null != this.m_conditional) && (this.m_conditional.m_ignore))
/*     */         {
/* 233 */           if (!token instanceof Integer) {
/*     */             continue;
/*     */           }
/*     */ 
/* 237 */           if (((Integer)token).intValue() < DataFormat.ENDIF.intValue()) {
/*     */             continue;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 243 */         if (token instanceof String)
/*     */         {
/* 245 */           builder.append((String)token);
/*     */         }
/* 247 */         else if (token instanceof Integer)
/*     */         {
/* 249 */           return (Integer)token;
/*     */         }
/*     */       }
/* 252 */       if (null != this.m_conditional)
/*     */       {
/* 254 */         int[] index = { this.m_index };
/* 255 */         String tokenStr = DataFormatUtils.makeStringFromTokens(this.m_tokens, index);
/* 256 */         ParseException e = new ParseException(tokenStr, index[0]);
/* 257 */         Report.debug("systemparse", "unterminated IF_*", e);
/*     */       }
/* 259 */       return null;
/*     */     }
/*     */ 
/*     */     class Conditional
/*     */     {
/*     */       Conditional m_previous;
/*     */       boolean m_isTrue;
/*     */       boolean m_inElse;
/*     */       boolean m_ignore;
/*     */ 
/*     */       Conditional()
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataFormatUtils
 * JD-Core Version:    0.5.4
 */