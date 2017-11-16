/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcLinguisticComparatorAdapter;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.SortOptions;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.MutableResultSet;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ResultSetTreeSort extends IdcLinguisticComparatorAdapter
/*     */ {
/*     */   public boolean m_isTableSort;
/*     */   public MutableResultSet m_set;
/*     */   public List m_tableList;
/*     */   public boolean m_isMulticolumnSort;
/*     */   public int m_sortColIndex;
/*     */   public int[] m_sortColIndices;
/*     */   public boolean m_isTreeSort;
/*     */   public boolean m_isCaseSensitive;
/*     */   public boolean[] m_isCaseSensitiveArray;
/*     */   public int m_itemIdColIndex;
/*     */   public int m_parentIdColIndex;
/*     */   public int m_nestLevelColIndex;
/*     */   public int m_fieldSortType;
/*     */   public int[] m_fieldSortTypes;
/*     */   public boolean m_isAscending;
/*     */   public boolean[] m_isAscendingArray;
/*     */   protected Object[][] m_sortValues;
/*     */   protected Object[] m_proxyArray;
/*     */   protected Hashtable m_itemLookups;
/*     */   protected int[] m_mapToParent;
/*     */   protected int[] m_treeDepths;
/*     */ 
/*     */   public ResultSetTreeSort()
/*     */   {
/*     */   }
/*     */ 
/*     */   public ResultSetTreeSort(DataResultSet drset, int sortColIndex, boolean isTreeSort)
/*     */   {
/* 121 */     init(drset, sortColIndex, isTreeSort);
/*     */   }
/*     */ 
/*     */   public ResultSetTreeSort(MutableResultSet set, int sortColIndex, boolean isTreeSort)
/*     */   {
/* 126 */     init(set, sortColIndex, isTreeSort);
/*     */   }
/*     */ 
/*     */   public ResultSetTreeSort(MutableResultSet set)
/*     */   {
/* 131 */     init(set, -1, false);
/*     */   }
/*     */ 
/*     */   public ResultSetTreeSort(List tableList)
/*     */   {
/* 141 */     this.m_isTableSort = true;
/* 142 */     this.m_tableList = tableList;
/*     */ 
/* 144 */     initImplement(-1, false);
/*     */   }
/*     */ 
/*     */   public ResultSetTreeSort(List tableList, int sortColIndex, boolean isTreeSort)
/*     */   {
/* 157 */     this.m_isTableSort = true;
/* 158 */     this.m_tableList = tableList;
/*     */ 
/* 160 */     initImplement(sortColIndex, isTreeSort);
/*     */   }
/*     */ 
/*     */   public void init(MutableResultSet set, int sortColIndex, boolean isTreeSort)
/*     */   {
/* 165 */     this.m_set = set;
/* 166 */     initImplement(sortColIndex, isTreeSort);
/*     */   }
/*     */ 
/*     */   public void initImplement(int sortColIndex, boolean isTreeSort)
/*     */   {
/* 171 */     this.m_sortColIndex = sortColIndex;
/* 172 */     this.m_isTreeSort = isTreeSort;
/*     */ 
/* 174 */     this.m_itemIdColIndex = -1;
/* 175 */     this.m_parentIdColIndex = -1;
/* 176 */     this.m_nestLevelColIndex = -1;
/* 177 */     this.m_fieldSortType = 6;
/* 178 */     this.m_isAscending = true;
/*     */ 
/* 181 */     this.m_sortValues = ((Object[][])null);
/* 182 */     this.m_proxyArray = null;
/* 183 */     this.m_itemLookups = null;
/* 184 */     this.m_mapToParent = null;
/*     */ 
/* 186 */     super.init(IdcLinguisticComparatorAdapter.m_defaultRule);
/*     */   }
/*     */ 
/*     */   public void setNlsSortRule(String nlsSortRule)
/*     */   {
/* 197 */     if ((nlsSortRule == null) || (nlsSortRule.equals("")))
/*     */       return;
/* 199 */     super.init(nlsSortRule);
/*     */   }
/*     */ 
/*     */   public void setNlsSortUsingLocale(IdcLocale locale)
/*     */   {
/* 210 */     if (locale == null)
/*     */       return;
/* 212 */     super.init(locale);
/*     */   }
/*     */ 
/*     */   public void sort()
/*     */   {
/* 219 */     if (!this.m_isMulticolumnSort)
/*     */     {
/* 221 */       this.m_sortColIndices = new int[1];
/* 222 */       this.m_sortColIndices[0] = this.m_sortColIndex;
/*     */     }
/* 224 */     int ncols = this.m_sortColIndices.length;
/* 225 */     int nrows = (this.m_isTableSort) ? this.m_tableList.size() : this.m_set.getNumRows();
/* 226 */     this.m_sortValues = new Object[nrows][ncols];
/* 227 */     this.m_proxyArray = new Object[nrows];
/* 228 */     if (this.m_isTreeSort)
/*     */     {
/* 230 */       this.m_itemLookups = new Hashtable();
/* 231 */       this.m_mapToParent = new int[nrows];
/*     */     }
/*     */ 
/* 234 */     for (int i = 0; i < nrows; ++i)
/*     */     {
/* 236 */       if (!this.m_isTableSort)
/*     */       {
/* 238 */         this.m_set.setCurrentRow(i);
/*     */       }
/* 240 */       this.m_sortValues[i] = new Object[ncols];
/* 241 */       for (int j = 0; j < ncols; ++j)
/*     */       {
/* 243 */         Object val = null;
/* 244 */         int fieldType = (this.m_isMulticolumnSort) ? this.m_fieldSortType : (null != this.m_fieldSortTypes) ? this.m_fieldSortTypes[j] : this.m_fieldSortType;
/*     */ 
/* 246 */         int colIndex = (this.m_isMulticolumnSort) ? this.m_sortColIndices[j] : this.m_sortColIndex;
/* 247 */         if (fieldType == 5)
/*     */         {
/* 249 */           Date d = null;
/* 250 */           if (this.m_isTableSort)
/*     */           {
/* 252 */             Object o = ((Object[])(Object[])this.m_tableList.get(i))[colIndex];
/* 253 */             if (o instanceof Date)
/*     */             {
/* 255 */               d = (Date)o;
/*     */             }
/* 259 */             else if (o != null)
/*     */             {
/*     */               try
/*     */               {
/* 263 */                 d = LocaleResources.parseDate(o.toString(), null);
/*     */               }
/*     */               catch (ServiceException e)
/*     */               {
/* 267 */                 Report.trace("system", null, e);
/*     */               }
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/* 274 */             d = this.m_set.getDateValue(colIndex);
/*     */           }
/* 276 */           if (d == null)
/*     */           {
/* 278 */             val = new Long(9223372036854775807L);
/*     */           }
/*     */           else
/*     */           {
/* 282 */             long l = d.getTime();
/* 283 */             val = new Long(l);
/*     */           }
/*     */         }
/* 286 */         else if (fieldType == 3)
/*     */         {
/* 288 */           long l = 0L;
/* 289 */           String sVal = null;
/*     */ 
/* 291 */           if (this.m_isTableSort)
/*     */           {
/* 293 */             Object o = ((Object[])(Object[])this.m_tableList.get(i))[colIndex];
/* 294 */             if (o instanceof Number)
/*     */             {
/* 296 */               l = ((Number)o).longValue();
/*     */             }
/* 300 */             else if (o != null)
/*     */             {
/* 302 */               sVal = o.toString();
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/* 308 */             sVal = this.m_set.getStringValue(colIndex);
/*     */           }
/*     */ 
/* 311 */           if (sVal != null)
/*     */           {
/* 313 */             l = NumberUtils.parseLong(sVal, 0L);
/*     */           }
/* 315 */           val = new Long(l);
/*     */         }
/*     */         else
/*     */         {
/* 319 */           val = (this.m_isTableSort) ? (String)((Object[])(Object[])this.m_tableList.get(i))[colIndex] : this.m_set.getStringValue(colIndex);
/*     */         }
/* 321 */         this.m_sortValues[i][j] = val;
/*     */       }
/* 323 */       Integer index = new Integer(i);
/* 324 */       Object[] indexArray = new Object[2];
/* 325 */       indexArray[0] = index;
/* 326 */       indexArray[1] = null;
/* 327 */       this.m_proxyArray[i] = indexArray;
/* 328 */       if ((!this.m_isTreeSort) || (this.m_itemIdColIndex < 0))
/*     */         continue;
/* 330 */       String id = (this.m_isTableSort) ? ((Object[])(Object[])this.m_tableList.get(i))[this.m_itemIdColIndex].toString() : this.m_set.getStringValue(this.m_itemIdColIndex);
/* 331 */       this.m_itemLookups.put(id, new Integer(i));
/*     */     }
/*     */ 
/* 336 */     if ((this.m_isTreeSort) && (this.m_parentIdColIndex >= 0))
/*     */     {
/* 338 */       for (int j = 0; j < nrows; ++j)
/*     */       {
/* 340 */         if (!this.m_isTableSort)
/*     */         {
/* 342 */           this.m_set.setCurrentRow(j);
/*     */         }
/* 344 */         Object parentIdObject = (this.m_isTableSort) ? ((Object[])(Object[])this.m_tableList.get(j))[this.m_parentIdColIndex] : this.m_set.getStringValue(this.m_parentIdColIndex);
/* 345 */         String parentId = null;
/* 346 */         if (parentIdObject != null)
/*     */         {
/* 348 */           parentId = parentIdObject.toString();
/*     */         }
/* 350 */         this.m_mapToParent[j] = -1;
/* 351 */         if ((parentId == null) || (parentId.length() <= 0))
/*     */           continue;
/* 353 */         Integer index = (Integer)this.m_itemLookups.get(parentId);
/* 354 */         if (index == null)
/*     */           continue;
/* 356 */         this.m_mapToParent[j] = index.intValue();
/*     */       }
/*     */ 
/* 363 */       int[] alreadyUsed = new int[nrows];
/* 364 */       for (int k = 0; k < nrows; ++k)
/*     */       {
/* 366 */         if (!this.m_isTableSort)
/*     */         {
/* 368 */           this.m_set.setCurrentRow(k);
/*     */         }
/*     */ 
/* 371 */         int curIndex = k;
/* 372 */         int depth = 0;
/* 373 */         while ((depth < nrows) && (curIndex >= 0))
/*     */         {
/* 375 */           alreadyUsed[depth] = curIndex;
/* 376 */           int parentIndex = this.m_mapToParent[curIndex];
/* 377 */           if (parentIndex >= 0)
/*     */           {
/* 380 */             for (int l = 0; l <= depth; ++l)
/*     */             {
/* 382 */               if (alreadyUsed[l] != parentIndex) {
/*     */                 continue;
/*     */               }
/* 385 */               this.m_mapToParent[curIndex] = -1;
/* 386 */               parentIndex = -1;
/*     */             }
/*     */           }
/*     */ 
/* 390 */           if (parentIndex >= 0)
/*     */           {
/* 393 */             ++depth;
/*     */           }
/* 395 */           curIndex = parentIndex;
/*     */         }
/*     */ 
/* 400 */         if (depth > 0)
/*     */         {
/* 402 */           int[] parentSequence = new int[depth];
/* 403 */           for (int m = 0; m < depth; ++m)
/*     */           {
/* 406 */             parentSequence[m] = alreadyUsed[(depth - m)];
/*     */           }
/*     */ 
/* 410 */           Object[] valArr = (Object[])(Object[])this.m_proxyArray[k];
/* 411 */           valArr[1] = parentSequence;
/*     */         }
/*     */ 
/* 415 */         if (this.m_nestLevelColIndex < 0)
/*     */           continue;
/* 417 */         String depthStr = "" + depth;
/*     */         try
/*     */         {
/* 420 */           if (this.m_isTableSort)
/*     */           {
/* 422 */             ((Object[])(Object[])this.m_tableList.get(k))[this.m_nestLevelColIndex] = depthStr;
/*     */           }
/*     */           else
/*     */           {
/* 426 */             this.m_set.setCurrentValue(this.m_nestLevelColIndex, depthStr);
/*     */           }
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 431 */           e.printStackTrace();
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 438 */     Sort.sort(this.m_proxyArray, 0, nrows - 1, this);
/*     */ 
/* 441 */     List[] newRowsRset = null;
/* 442 */     Object[][] newRowsTable = (Object[][])null;
/*     */ 
/* 444 */     if (this.m_isTableSort)
/*     */     {
/* 446 */       newRowsTable = new Object[nrows][];
/*     */     }
/*     */     else
/*     */     {
/* 450 */       newRowsRset = new List[nrows];
/*     */     }
/*     */ 
/* 453 */     for (int n = 0; n < nrows; ++n)
/*     */     {
/* 455 */       Object[] arr = (Object[])(Object[])this.m_proxyArray[n];
/* 456 */       Integer newIndex = (Integer)arr[0];
/* 457 */       int curLocation = newIndex.intValue();
/*     */ 
/* 459 */       if (this.m_isTableSort)
/*     */       {
/* 461 */         newRowsTable[n] = ((Object[])(Object[])this.m_tableList.get(curLocation));
/*     */       }
/*     */       else
/*     */       {
/* 465 */         Vector row = this.m_set.getRowValues(curLocation);
/* 466 */         newRowsRset[n] = row;
/*     */       }
/*     */     }
/*     */ 
/* 470 */     for (int n = 0; n < nrows; ++n)
/*     */     {
/* 472 */       if (this.m_isTableSort)
/*     */       {
/* 474 */         this.m_tableList.set(n, newRowsTable[n]);
/*     */       }
/*     */       else
/*     */       {
/* 478 */         this.m_set.setRowValues((Vector)newRowsRset[n], n);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setSortOptions(SortOptions options)
/*     */   {
/* 485 */     this.m_sortColIndex = options.m_sortColIndex;
/* 486 */     this.m_isTreeSort = options.m_isTreeSort;
/* 487 */     this.m_itemIdColIndex = options.m_childSortColIndex;
/* 488 */     this.m_parentIdColIndex = options.m_parentSortColIndex;
/* 489 */     this.m_nestLevelColIndex = options.m_sortNestLevelColIndex;
/* 490 */     determineFieldType(options.m_sortType);
/* 491 */     determineIsAscending(options.m_sortOrder);
/*     */   }
/*     */ 
/*     */   public void determineFieldType(String sortType)
/*     */   {
/* 496 */     if (sortType == null)
/*     */     {
/* 498 */       sortType = "";
/*     */     }
/* 500 */     if (sortType.length() > 0)
/*     */     {
/* 502 */       char ch = Character.toLowerCase(sortType.charAt(0));
/* 503 */       if (ch == 'i')
/*     */       {
/* 505 */         this.m_fieldSortType = 3;
/*     */       }
/* 507 */       else if (ch == 's')
/*     */       {
/* 509 */         this.m_fieldSortType = 6;
/*     */       }
/* 511 */       else if (ch == 'd')
/*     */       {
/* 513 */         this.m_fieldSortType = 5;
/*     */       }
/*     */     } else {
/* 516 */       if (this.m_isTableSort)
/*     */         return;
/* 518 */       FieldInfo fi = new FieldInfo();
/* 519 */       this.m_set.getIndexFieldInfo(this.m_sortColIndex, fi);
/* 520 */       this.m_fieldSortType = fi.m_type;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void findOrAppendNestLevelField(String nestLevelCol)
/*     */   {
/* 532 */     FieldInfo fi = new FieldInfo();
/* 533 */     if (!this.m_set.getFieldInfo(nestLevelCol, fi))
/*     */     {
/* 535 */       fi.m_name = nestLevelCol;
/* 536 */       fi.m_type = 3;
/* 537 */       Vector v = new IdcVector();
/* 538 */       v.addElement(fi);
/* 539 */       this.m_set.appendFields(v);
/*     */     }
/* 541 */     this.m_nestLevelColIndex = fi.m_index;
/*     */   }
/*     */ 
/*     */   public void determineIsAscending(String sortOrder)
/*     */   {
/* 546 */     if ((sortOrder == null) || (sortOrder.length() <= 0))
/*     */       return;
/* 548 */     char ch = Character.toLowerCase(sortOrder.charAt(0));
/* 549 */     if (ch == 'd')
/*     */     {
/* 551 */       this.m_isAscending = false;
/*     */     } else {
/* 553 */       if ((ch != 'a') && (ch != 'i'))
/*     */         return;
/* 555 */       this.m_isAscending = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   public int compare(Object obj1, Object obj2)
/*     */   {
/* 563 */     Object[] vals1 = (Object[])(Object[])obj1;
/* 564 */     Object[] vals2 = (Object[])(Object[])obj2;
/* 565 */     Integer i1 = (Integer)vals1[0];
/* 566 */     Integer i2 = (Integer)vals2[0];
/* 567 */     int curIndex1 = i1.intValue();
/* 568 */     int curIndex2 = i2.intValue();
/* 569 */     int result = 0;
/*     */ 
/* 572 */     if (this.m_isTreeSort)
/*     */     {
/* 574 */       int[] p1 = (int[])(int[])vals1[1];
/* 575 */       int[] p2 = (int[])(int[])vals2[1];
/* 576 */       boolean p1Consumed = false;
/* 577 */       boolean p2Consumed = false;
/*     */ 
/* 579 */       int parentIndex = 0;
/* 580 */       while ((!p1Consumed) && (!p2Consumed))
/*     */       {
/* 582 */         p1Consumed = (p1 == null) || (parentIndex >= p1.length);
/* 583 */         p2Consumed = (p2 == null) || (parentIndex >= p2.length);
/* 584 */         if (!p1Consumed)
/*     */         {
/* 586 */           curIndex1 = p1[parentIndex];
/*     */         }
/*     */         else
/*     */         {
/* 590 */           curIndex1 = i1.intValue();
/*     */         }
/* 592 */         if (!p2Consumed)
/*     */         {
/* 594 */           curIndex2 = p2[parentIndex];
/*     */         }
/*     */         else
/*     */         {
/* 598 */           curIndex2 = i2.intValue();
/*     */         }
/* 600 */         if (curIndex1 == curIndex2)
/*     */         {
/* 604 */           if (p1Consumed)
/*     */           {
/* 606 */             result = -1;
/*     */           }
/* 608 */           else if (p2Consumed)
/*     */           {
/* 610 */             result = 1;
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 616 */           p1Consumed = true;
/* 617 */           p2Consumed = true;
/*     */         }
/*     */ 
/* 620 */         ++parentIndex;
/*     */       }
/*     */     }
/*     */ 
/* 624 */     if (result == 0)
/*     */     {
/* 626 */       int ncols = (this.m_isMulticolumnSort) ? this.m_sortColIndices.length : 1;
/* 627 */       for (int j = 0; j < ncols; ++j)
/*     */       {
/* 630 */         Object val1 = this.m_sortValues[curIndex1][j];
/* 631 */         Object val2 = this.m_sortValues[curIndex2][j];
/* 632 */         int fieldType = (this.m_isMulticolumnSort) ? this.m_fieldSortType : (null != this.m_fieldSortTypes) ? this.m_fieldSortTypes[j] : this.m_fieldSortType;
/*     */ 
/* 634 */         if ((fieldType == 3) || (fieldType == 5))
/*     */         {
/* 636 */           Long lo1 = (Long)val1;
/* 637 */           Long lo2 = (Long)val2;
/* 638 */           long l1 = lo1.longValue();
/* 639 */           long l2 = lo2.longValue();
/* 640 */           if (l1 < l2)
/*     */           {
/* 642 */             result = -1;
/*     */           }
/* 644 */           else if (l1 > l2)
/*     */           {
/* 646 */             result = 1;
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 651 */           String s1 = (String)val1;
/* 652 */           String s2 = (String)val2;
/*     */ 
/* 654 */           boolean isCaseSensitive = (this.m_isMulticolumnSort) ? this.m_isCaseSensitive : (null != this.m_isCaseSensitiveArray) ? this.m_isCaseSensitiveArray[j] : this.m_isCaseSensitive;
/*     */ 
/* 656 */           int sensitivityLevel = (isCaseSensitive) ? 2 : 1;
/*     */ 
/* 658 */           if (sensitivityLevel != this.m_caseSensitivityStrenthLevel)
/*     */           {
/* 660 */             setComparatorLevel(1);
/*     */           }
/* 662 */           result = super.compare(s1, s2);
/*     */         }
/*     */ 
/* 665 */         boolean isAscending = (this.m_isMulticolumnSort) ? this.m_isAscending : (null != this.m_isAscendingArray) ? this.m_isAscendingArray[j] : this.m_isAscending;
/*     */ 
/* 667 */         if (!isAscending)
/*     */         {
/* 669 */           result = -result;
/*     */         }
/*     */ 
/* 672 */         if (result != 0)
/*     */         {
/*     */           break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 679 */       if (result == 0)
/*     */       {
/* 681 */         if (curIndex1 > curIndex2)
/*     */         {
/* 683 */           result = 1;
/*     */         }
/* 685 */         else if (curIndex1 < curIndex2)
/*     */         {
/* 687 */           result = -1;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 692 */     return result;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 698 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84221 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ResultSetTreeSort
 * JD-Core Version:    0.5.4
 */