/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class ResultSetJoin
/*     */ {
/*     */   public static final String DEFAULT_SEARCH_PATTERN_STRING = "@@([^@.]*)(\\.([^@]*))?@@";
/*     */   public static final String DEFAULT_JOIN_TABLE_NAME = "\\1";
/*     */   public static final String DEFAULT_JOIN_FIELD_NAME = "\\3";
/*     */   public ResultSet m_source;
/*     */   public MutableResultSet m_target;
/*     */   public FieldInfo[] m_searchFields;
/*     */   public FieldInfo[] m_targetFields;
/*     */   public Pattern m_searchPattern;
/*     */   public String m_joinTableName;
/*     */   public String m_joinFieldName;
/*     */   public DataBinder m_binder;
/*     */   public boolean m_canSearchSharedObjects;
/* 165 */   public static Map m_tables = null;
/*     */   public Pattern m_joinedTableFilterPattern;
/*     */   public Pattern m_joinedFieldFilterPattern;
/*     */   public Pattern m_joinedValueFilterPattern;
/*     */   public boolean m_isJoinedFilterInclusive;
/*     */   protected IdcStringBuilder m_builder;
/*     */   protected String[] m_searchRowArray;
/*     */   protected String[] m_searchRowValues;
/*     */   protected List[] m_searchRowTokens;
/*     */   protected Map m_joinTablesMap;
/*     */   protected int m_numJoinTables;
/*     */   protected boolean[] m_isTargetFieldSearched;
/*     */   protected int[] m_targetFieldMapping;
/*     */   protected ResultSet[] m_joinTables;
/*     */   protected int[] m_joinTablesNumFields;
/*     */   protected boolean[][] m_joinTableFieldsFiltered;
/*     */ 
/*     */   public ResultSetJoin()
/*     */   {
/*     */   }
/*     */ 
/*     */   public ResultSetJoin(ResultSet source, DataBinder binder)
/*     */   {
/* 213 */     this.m_source = source;
/* 214 */     this.m_binder = binder;
/*     */   }
/*     */ 
/*     */   public void join()
/*     */     throws DataException
/*     */   {
/* 224 */     joinWithSimpleTokenSubstitution();
/*     */   }
/*     */ 
/*     */   protected void setDefaultsForSimpleTokenSubstitution()
/*     */   {
/* 233 */     if (null == this.m_searchFields)
/*     */     {
/* 235 */       int n = this.m_source.getNumFields();
/* 236 */       this.m_searchFields = new FieldInfo[n];
/* 237 */       for (int i = 0; i < n; ++i)
/*     */       {
/* 239 */         this.m_searchFields[i] = new FieldInfo();
/* 240 */         this.m_source.getIndexFieldInfo(i, this.m_searchFields[i]);
/*     */       }
/*     */     }
/* 243 */     if (null == this.m_targetFields)
/*     */     {
/* 245 */       int n = this.m_source.getNumFields();
/* 246 */       this.m_targetFields = new FieldInfo[n];
/* 247 */       for (int i = 0; i < n; ++i)
/*     */       {
/* 249 */         this.m_targetFields[i] = new FieldInfo();
/* 250 */         this.m_source.getIndexFieldInfo(i, this.m_targetFields[i]);
/*     */       }
/*     */     }
/* 253 */     if (null == this.m_target)
/*     */     {
/* 255 */       this.m_target = new DataResultSet();
/* 256 */       Vector fields = new Vector(this.m_targetFields.length);
/* 257 */       for (int i = 0; i < this.m_targetFields.length; ++i)
/*     */       {
/* 259 */         fields.add(this.m_targetFields[i]);
/*     */       }
/* 261 */       this.m_target.appendFields(fields);
/*     */     }
/* 263 */     if (null == this.m_searchPattern)
/*     */     {
/* 265 */       this.m_searchPattern = Pattern.compile("@@([^@.]*)(\\.([^@]*))?@@");
/* 266 */       this.m_joinTableName = "\\1";
/* 267 */       this.m_joinFieldName = "\\3";
/*     */     }
/* 269 */     if (null != this.m_joinedValueFilterPattern)
/*     */       return;
/* 271 */     this.m_joinedTableFilterPattern = null;
/*     */   }
/*     */ 
/*     */   protected void initializeForSimpleTokenSubstitution()
/*     */     throws DataException
/*     */   {
/* 282 */     if ((this.m_searchFields.length < 0) || (this.m_targetFields.length < 0))
/*     */     {
/* 284 */       throw new DataException("!syTooFewArguments");
/*     */     }
/* 286 */     this.m_builder = new IdcStringBuilder();
/*     */ 
/* 288 */     this.m_searchRowArray = new String[this.m_searchFields.length];
/* 289 */     this.m_searchRowValues = new String[this.m_searchFields.length];
/* 290 */     this.m_searchRowTokens = new List[this.m_searchFields.length];
/* 291 */     for (int i = 0; i < this.m_searchFields.length; ++i)
/*     */     {
/* 293 */       this.m_searchRowTokens[i] = new ArrayList();
/*     */     }
/* 295 */     this.m_joinTablesMap = new HashMap();
/*     */ 
/* 298 */     this.m_isTargetFieldSearched = new boolean[this.m_targetFields.length];
/* 299 */     this.m_targetFieldMapping = new int[this.m_targetFields.length];
/* 300 */     for (int i = 0; i < this.m_targetFields.length; ++i)
/*     */     {
/* 302 */       FieldInfo finfo = new FieldInfo();
/* 303 */       if (!this.m_source.getFieldInfo(this.m_targetFields[i].m_name, finfo))
/*     */       {
/* 305 */         String msg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, this.m_targetFields[i].m_name);
/* 306 */         throw new DataException(msg);
/*     */       }
/* 308 */       this.m_targetFieldMapping[i] = finfo.m_index;
/* 309 */       for (int j = 0; j < this.m_searchFields.length; ++j)
/*     */       {
/* 311 */         if (!this.m_targetFields[i].m_name.equals(this.m_searchFields[j].m_name))
/*     */           continue;
/* 313 */         this.m_targetFieldMapping[i] = j;
/* 314 */         this.m_isTargetFieldSearched[i] = true;
/* 315 */         break;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String computeGroupReplacementString(Matcher matcher, String str)
/*     */   {
/* 330 */     int numGroups = matcher.groupCount();
/* 331 */     int length = str.length();
/* 332 */     char[] chars = new char[length];
/* 333 */     str.getChars(0, length, chars, 0);
/* 334 */     IdcStringBuilder ret = new IdcStringBuilder();
/* 335 */     int start = 0;
/* 336 */     for (int index = 0; index < length; ++index)
/*     */     {
/* 338 */       if ('\\' != chars[index]) {
/*     */         continue;
/*     */       }
/*     */ 
/* 342 */       ret.append(chars, start, index++ - start);
/* 343 */       char c = chars[index];
/* 344 */       if ((c < '0') || (c > '9'))
/*     */       {
/* 346 */         start = index;
/*     */       }
/*     */       else {
/* 349 */         if (c - '0' <= numGroups)
/*     */         {
/* 351 */           String group = matcher.group(c - '0');
/* 352 */           if (null != group)
/*     */           {
/* 354 */             ret.append(group);
/*     */           }
/*     */         }
/* 357 */         start = index + 1;
/*     */       }
/*     */     }
/* 359 */     if (start < length)
/*     */     {
/* 361 */       ret.append(chars, start, length - start);
/*     */     }
/* 363 */     return ret.toString();
/*     */   }
/*     */ 
/*     */   protected ResultSetJoinSimpleToken createSimpleToken(Matcher matcher)
/*     */   {
/* 374 */     String table = computeGroupReplacementString(matcher, this.m_joinTableName);
/* 375 */     String field = computeGroupReplacementString(matcher, this.m_joinFieldName);
/* 376 */     if (table.length() < 1)
/*     */     {
/* 378 */       return null;
/*     */     }
/* 380 */     ResultSetJoinSimpleToken token = new ResultSetJoinSimpleToken();
/* 381 */     token.m_startIndex = matcher.start();
/* 382 */     token.m_endIndex = matcher.end();
/* 383 */     token.m_tableName = table;
/* 384 */     token.m_fieldName = ((field.length() < 1) ? null : field);
/* 385 */     return token;
/*     */   }
/*     */ 
/*     */   protected void collectTokensForRow()
/*     */   {
/* 394 */     for (int i = 0; i < this.m_searchFields.length; ++i)
/*     */     {
/* 396 */       this.m_searchRowArray[i] = this.m_source.getStringValue(this.m_searchFields[i].m_index);
/* 397 */       this.m_searchRowTokens[i].clear();
/* 398 */       Matcher matcher = this.m_searchPattern.matcher(this.m_searchRowArray[i]);
/* 399 */       while (matcher.find())
/*     */       {
/* 402 */         ResultSetJoinSimpleToken token = createSimpleToken(matcher);
/* 403 */         if (null != token)
/*     */         {
/* 405 */           if (!this.m_joinTablesMap.containsKey(token.m_tableName))
/*     */           {
/* 407 */             ResultSet table = null;
/* 408 */             if (null != this.m_binder)
/*     */             {
/* 410 */               table = this.m_binder.getResultSet(token.m_tableName);
/*     */             }
/* 412 */             if ((null == table) && (this.m_canSearchSharedObjects))
/*     */             {
/* 416 */               table = (DataResultSet)m_tables.get(token.m_tableName);
/*     */             }
/* 418 */             if ((null != table) && (table.first()))
/*     */             {
/* 420 */               this.m_joinTablesMap.put(token.m_tableName, table);
/* 421 */               this.m_searchRowTokens[i].add(token);
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 426 */             this.m_searchRowTokens[i].add(token);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void computeJoinTableFilters()
/*     */   {
/* 439 */     this.m_joinTables = new ResultSet[this.m_numJoinTables];
/* 440 */     this.m_joinTablesNumFields = new int[this.m_numJoinTables];
/* 441 */     this.m_joinTableFieldsFiltered = new boolean[this.m_numJoinTables][];
/* 442 */     Iterator iter = this.m_joinTablesMap.keySet().iterator();
/* 443 */     for (int t = 0; iter.hasNext(); ++t)
/*     */     {
/* 445 */       String tableName = (String)iter.next();
/* 446 */       ResultSet table = (ResultSet)this.m_joinTablesMap.get(tableName);
/* 447 */       this.m_joinTables[t] = table;
/* 448 */       this.m_joinTablesNumFields[t] = table.getNumFields();
/* 449 */       if (null == this.m_joinedTableFilterPattern) {
/*     */         continue;
/*     */       }
/*     */ 
/* 453 */       Matcher filter = this.m_joinedTableFilterPattern.matcher(tableName);
/* 454 */       if (!filter.find())
/*     */         continue;
/* 456 */       this.m_joinTableFieldsFiltered[t] = new boolean[this.m_joinTablesNumFields[t]];
/* 457 */       for (int f = 0; f < this.m_joinTablesNumFields[t]; ++f)
/*     */       {
/* 459 */         if (null == this.m_joinedFieldFilterPattern)
/*     */         {
/* 461 */           this.m_joinTableFieldsFiltered[t][f] = 1;
/*     */         }
/*     */         else {
/* 464 */           String fieldName = table.getFieldName(f);
/* 465 */           filter = this.m_joinedFieldFilterPattern.matcher(fieldName);
/* 466 */           this.m_joinTableFieldsFiltered[t][f] = filter.find();
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean checkIfJoinIsFilteredOut()
/*     */   {
/* 478 */     if ((null == this.m_joinedTableFilterPattern) || (null == this.m_joinedValueFilterPattern))
/*     */     {
/* 480 */       return false;
/*     */     }
/* 482 */     for (int t = 0; t < this.m_numJoinTables; ++t)
/*     */     {
/* 484 */       if (null == this.m_joinTableFieldsFiltered[t]) {
/*     */         continue;
/*     */       }
/*     */ 
/* 488 */       for (int f = 0; f < this.m_joinTablesNumFields[t]; ++f)
/*     */       {
/* 490 */         if (this.m_joinTableFieldsFiltered[t][f] == 0) {
/*     */           continue;
/*     */         }
/*     */ 
/* 494 */         String value = this.m_joinTables[t].getStringValue(f);
/* 495 */         Matcher filter = this.m_joinedValueFilterPattern.matcher(value);
/* 496 */         if (filter.find())
/*     */         {
/* 498 */           return !this.m_isJoinedFilterInclusive;
/*     */         }
/*     */       }
/*     */     }
/* 502 */     return this.m_isJoinedFilterInclusive;
/*     */   }
/*     */ 
/*     */   protected List createDuplicateRow()
/*     */   {
/* 511 */     List row = new ArrayList(this.m_targetFields.length);
/* 512 */     for (int i = 0; i < this.m_targetFields.length; ++i)
/*     */     {
/* 514 */       int index = this.m_targetFieldMapping[i];
/* 515 */       if (this.m_isTargetFieldSearched[i] != 0)
/*     */       {
/* 517 */         index = this.m_searchFields[index].m_index;
/*     */       }
/* 519 */       String value = this.m_source.getStringValue(index);
/* 520 */       row.add(value);
/*     */     }
/* 522 */     return row;
/*     */   }
/*     */ 
/*     */   protected List createReplacementRow()
/*     */   {
/* 533 */     for (int i = 0; i < this.m_searchFields.length; ++i)
/*     */     {
/* 535 */       this.m_builder.setLength(0);
/* 536 */       int numTokens = this.m_searchRowTokens[i].size();
/* 537 */       int start = 0;
/* 538 */       for (int k = 0; k < numTokens; ++k)
/*     */       {
/* 540 */         ResultSetJoinSimpleToken token = (ResultSetJoinSimpleToken)this.m_searchRowTokens[i].get(k);
/* 541 */         int len = token.m_startIndex - start;
/* 542 */         if (len > 0)
/*     */         {
/* 544 */           this.m_builder.append(this.m_searchRowArray[i], start, len);
/*     */         }
/* 546 */         ResultSet table = (ResultSet)this.m_joinTablesMap.get(token.m_tableName);
/*     */         String value;
/*     */         String value;
/* 548 */         if (null == token.m_fieldName)
/*     */         {
/* 550 */           value = table.getStringValue(0);
/*     */         }
/*     */         else
/*     */         {
/* 554 */           value = table.getStringValueByName(token.m_fieldName);
/*     */         }
/* 556 */         this.m_builder.append(value);
/* 557 */         start = token.m_endIndex;
/*     */       }
/* 559 */       int len = this.m_searchRowArray[i].length() - start;
/* 560 */       if (len > 0)
/*     */       {
/* 562 */         this.m_builder.append(this.m_searchRowArray[i], start, len);
/*     */       }
/* 564 */       this.m_searchRowValues[i] = this.m_builder.toStringNoRelease();
/*     */     }
/*     */ 
/* 567 */     List row = new ArrayList(this.m_targetFields.length);
/* 568 */     for (int i = 0; i < this.m_targetFields.length; ++i)
/*     */     {
/* 570 */       int index = this.m_targetFieldMapping[i];
/* 571 */       String value = (this.m_isTargetFieldSearched[i] != 0) ? this.m_searchRowValues[index] : this.m_source.getStringValue(index);
/* 572 */       row.add(value);
/*     */     }
/* 574 */     return row;
/*     */   }
/*     */ 
/*     */   protected void joinWithSimpleTokenSubstitution()
/*     */     throws DataException
/*     */   {
/* 584 */     setDefaultsForSimpleTokenSubstitution();
/* 585 */     initializeForSimpleTokenSubstitution();
/*     */ 
/* 588 */     for (this.m_source.first(); this.m_source.isRowPresent(); this.m_source.next())
/*     */     {
/* 591 */       collectTokensForRow();
/* 592 */       this.m_numJoinTables = this.m_joinTablesMap.size();
/* 593 */       if (this.m_numJoinTables < 1)
/*     */       {
/* 595 */         List row = createDuplicateRow();
/* 596 */         this.m_target.addRowWithList(row);
/*     */       }
/*     */       else
/*     */       {
/* 599 */         computeJoinTableFilters();
/*     */         int t;
/*     */         do
/*     */         {
/* 605 */           if (!checkIfJoinIsFilteredOut())
/*     */           {
/* 607 */             List row = createReplacementRow();
/* 608 */             this.m_target.addRowWithList(row);
/*     */           }
/*     */ 
/* 611 */           for (t = 0; t < this.m_numJoinTables; ++t)
/*     */           {
/* 613 */             if (this.m_joinTables[t].next()) {
/*     */               break;
/*     */             }
/*     */ 
/* 617 */             this.m_joinTables[t].first();
/*     */           }
/*     */         }
/* 619 */         while (t < this.m_numJoinTables);
/*     */ 
/* 622 */         this.m_joinTablesMap.clear();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 631 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70600 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ResultSetJoin
 * JD-Core Version:    0.5.4
 */