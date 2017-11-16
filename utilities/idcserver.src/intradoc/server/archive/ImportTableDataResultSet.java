/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ImportTableDataResultSet extends DataResultSet
/*     */ {
/*     */   protected Hashtable m_rowHash;
/*     */   protected Vector m_skippedRows;
/*     */   protected Hashtable m_skippedRowMap;
/*     */   protected TimeZone m_oldTimeZone;
/*     */   protected TimeZone m_newTimeZone;
/*     */ 
/*     */   public ImportTableDataResultSet()
/*     */   {
/*  32 */     this.m_rowHash = new Hashtable(1000);
/*  33 */     this.m_skippedRows = new IdcVector();
/*  34 */     this.m_skippedRowMap = new Hashtable();
/*  35 */     this.m_oldTimeZone = null;
/*  36 */     this.m_newTimeZone = null;
/*     */   }
/*     */ 
/*     */   public void init(TimeZone oldTimeZone, TimeZone newTimeZone) {
/*  40 */     this.m_oldTimeZone = oldTimeZone;
/*  41 */     this.m_newTimeZone = newTimeZone;
/*     */   }
/*     */ 
/*     */   public void copyWithFilteredColumn(DataResultSet rset, int beginIndex, int numFields, boolean isRemoveDuplicate)
/*     */   {
/*  47 */     if ((beginIndex < 0) || (numFields < 0) || (rset == null) || (rset.getNumFields() < beginIndex + numFields))
/*     */     {
/*  50 */       return;
/*     */     }
/*  52 */     initFields(rset, beginIndex, numFields);
/*     */ 
/*  54 */     int endIndex = beginIndex + numFields;
/*  55 */     int rowNum = 0;
/*  56 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/*  58 */       Vector oldRow = rset.getCurrentRowValues();
/*  59 */       List row = createNewRowList(32);
/*     */ 
/*  61 */       boolean isNotNull = false;
/*  62 */       for (int i = beginIndex; i < endIndex; ++i)
/*     */       {
/*  64 */         Object obj = oldRow.elementAt(i);
/*  65 */         FieldInfo fi = new FieldInfo();
/*  66 */         rset.getIndexFieldInfo(i, fi);
/*     */ 
/*  68 */         if ((obj != null) && (((!obj instanceof String) || (((String)obj).length() > 0))))
/*     */         {
/*  70 */           isNotNull = true;
/*     */         }
/*  72 */         if (fi.m_type == 5)
/*     */         {
/*  74 */           obj = translateDate((String)obj);
/*     */         }
/*     */ 
/*  77 */         row.add(obj);
/*     */       }
/*  79 */       boolean allowCopy = (isNotNull) && (((!isRemoveDuplicate) || (!isDuplicateRow(row, rowNum))));
/*  80 */       if (allowCopy)
/*     */       {
/*  82 */         addRowWithList(row);
/*     */       }
/*  86 */       else if (!isNotNull)
/*     */       {
/*  88 */         Report.trace("archiver", "removing empty row from resultset:" + row, null);
/*     */       }
/*     */       else
/*     */       {
/*  92 */         Report.trace("archiver", "removing duplicate row: " + row, null);
/*     */       }
/*     */ 
/*  96 */       ++rowNum;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String translateDate(String dateStr)
/*     */   {
/* 102 */     if ((this.m_oldTimeZone != null) && (this.m_newTimeZone != null) && (!this.m_oldTimeZone.equals(this.m_newTimeZone)))
/*     */     {
/* 105 */       IdcDateFormat odbcFormat = LocaleResources.m_odbcFormat;
/*     */       try
/*     */       {
/* 108 */         Date date = odbcFormat.parseDateWithTimeZone(dateStr, this.m_oldTimeZone, null, 256);
/*     */ 
/* 110 */         Calendar cal = Calendar.getInstance();
/* 111 */         cal.setTime(date);
/* 112 */         cal.setTimeZone(this.m_newTimeZone);
/* 113 */         dateStr = odbcFormat.format(cal.getTime());
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 118 */         Report.trace("archiver", null, ignore);
/*     */       }
/*     */     }
/*     */ 
/* 122 */     return dateStr;
/*     */   }
/*     */ 
/*     */   public void initFields(ResultSet rset, int beginIndex, int numFields)
/*     */   {
/* 128 */     reset();
/* 129 */     FieldInfo[] infos = new FieldInfo[numFields];
/* 130 */     FieldInfo fi = null;
/* 131 */     for (int i = beginIndex; i < beginIndex + numFields; ++i)
/*     */     {
/* 133 */       fi = new FieldInfo();
/* 134 */       rset.getIndexFieldInfo(i, fi);
/*     */ 
/* 136 */       infos[(i - beginIndex)] = fi;
/*     */     }
/* 138 */     initFields(infos, rset.getDateFormat());
/*     */ 
/* 140 */     this.m_dateFormat[0] = rset.getDateFormat();
/*     */   }
/*     */ 
/*     */   public void initFields(FieldInfo[] infos, IdcDateFormat dateFormat)
/*     */   {
/* 145 */     if (infos == null)
/*     */     {
/* 147 */       Report.trace("archiver", "Unable to initialize ResultSet, FieldInfo array is null", null);
/* 148 */       return;
/*     */     }
/*     */ 
/* 151 */     for (int i = 0; i < infos.length; ++i)
/*     */     {
/* 153 */       infos[i].m_index = i;
/* 154 */       this.m_fieldMapping.put(infos[i].m_name, infos[i]);
/* 155 */       this.m_fieldList.add(infos[i]);
/*     */     }
/*     */ 
/* 158 */     this.m_dateFormat[0] = dateFormat;
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 164 */     this.m_rowHash = new Hashtable();
/* 165 */     super.reset();
/*     */   }
/*     */ 
/*     */   protected boolean isDuplicateRow(List row, int rowIndex)
/*     */   {
/* 184 */     boolean isDuplicate = false;
/* 185 */     String str = row.toString();
/* 186 */     String hash = "" + str.hashCode();
/* 187 */     String sum = getSum(str);
/* 188 */     Object obj = this.m_rowHash.get(hash);
/* 189 */     if (obj != null)
/*     */     {
/* 191 */       String objStr = (String)obj;
/* 192 */       int beginIndex = objStr.indexOf(sum);
/* 193 */       if (beginIndex != -1)
/*     */       {
/* 195 */         isDuplicate = true;
/*     */ 
/* 198 */         beginIndex = objStr.indexOf(124, beginIndex + 1);
/* 199 */         ++beginIndex;
/* 200 */         int endIndex = objStr.indexOf(124, beginIndex);
/* 201 */         if (endIndex == -1)
/*     */         {
/* 204 */           objStr = objStr + "," + rowIndex;
/*     */         }
/*     */         else
/*     */         {
/* 209 */           StringBuffer buf = new StringBuffer();
/* 210 */           buf.append(objStr.substring(0, endIndex));
/* 211 */           buf.append(',');
/* 212 */           buf.append(rowIndex);
/* 213 */           buf.append(objStr.substring(endIndex));
/* 214 */           objStr = buf.toString();
/*     */         }
/* 216 */         this.m_rowHash.put(hash, objStr);
/*     */       }
/*     */       else
/*     */       {
/* 220 */         objStr = objStr + sum + this.m_values.size() + "-" + rowIndex;
/* 221 */         this.m_rowHash.put(hash, objStr);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 226 */       String value = sum + this.m_values.size() + "-" + rowIndex;
/* 227 */       this.m_rowHash.put(hash, value);
/*     */     }
/* 229 */     return isDuplicate;
/*     */   }
/*     */ 
/*     */   protected String getSum(String str)
/*     */   {
/* 234 */     char[] chars = str.toCharArray();
/* 235 */     long sum = 0L;
/* 236 */     for (int i = 0; i < chars.length; ++i)
/*     */     {
/* 238 */       sum += chars[0];
/*     */     }
/* 240 */     return "|" + sum + "|";
/*     */   }
/*     */ 
/*     */   public boolean copyDeleteTable(DataResultSet drset, String tableName, String deleteTS, String createTS, String modifiedTS, String srcIDCol, DataResultSet origTable)
/*     */   {
/* 247 */     int tableIndex = -1;
/* 248 */     int pkIndex = -1;
/* 249 */     int pkNameIndex = -1;
/* 250 */     int pkTypeIndex = -1;
/* 251 */     int srcIDIndex = -1;
/* 252 */     int tsIndex = -1;
/*     */     try
/*     */     {
/* 256 */       tableIndex = ResultSetUtils.getIndexMustExist(drset, "dTable");
/* 257 */       pkIndex = ResultSetUtils.getIndexMustExist(drset, "dPrimaryKeys");
/* 258 */       pkNameIndex = ResultSetUtils.getIndexMustExist(drset, "dPKColumns");
/* 259 */       pkTypeIndex = ResultSetUtils.getIndexMustExist(drset, "dPKTypes");
/* 260 */       srcIDIndex = ResultSetUtils.getIndexMustExist(drset, "dSourceID");
/* 261 */       tsIndex = ResultSetUtils.getIndexMustExist(drset, "dDeleteDate");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 265 */       Report.error(null, "!csArchiveTableImportUnableConvertDeletesTable", e);
/* 266 */       return false;
/*     */     }
/*     */ 
/* 269 */     boolean isFirst = true;
/* 270 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 272 */       String table = drset.getStringValue(tableIndex);
/* 273 */       if (!table.equals(tableName))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 278 */       if (isFirst)
/*     */       {
/* 285 */         String pkNames = drset.getStringValue(pkNameIndex);
/* 286 */         String pkTypes = drset.getStringValue(pkTypeIndex);
/* 287 */         if (srcIDCol == null)
/*     */         {
/* 289 */           FieldInfo fi = new FieldInfo();
/* 290 */           origTable.getFieldInfo("schSourceID", fi);
/* 291 */           if (fi.m_index > -1)
/*     */           {
/* 293 */             srcIDCol = "schSourceID";
/*     */           }
/*     */         }
/* 296 */         isFirst = !initDeleteTableFields(pkNames, pkTypes, createTS, modifiedTS, srcIDCol, drset.getDateFormat(), origTable);
/*     */ 
/* 299 */         if (isFirst)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 306 */       String pk = drset.getStringValue(pkIndex);
/* 307 */       Vector row = StringUtils.parseArray(pk, ',', '^');
/* 308 */       if (createTS != null)
/*     */       {
/* 310 */         String ts = drset.getStringValue(tsIndex);
/* 311 */         ts = translateDate(ts);
/* 312 */         row.addElement(ts);
/*     */       }
/* 314 */       if (modifiedTS != null)
/*     */       {
/* 316 */         String ts = drset.getStringValue(tsIndex);
/* 317 */         ts = translateDate(ts);
/* 318 */         row.addElement(ts);
/*     */       }
/*     */ 
/* 321 */       if (srcIDCol != null)
/*     */       {
/* 323 */         String srcID = drset.getStringValue(srcIDIndex);
/* 324 */         row.addElement(srcID);
/*     */       }
/* 326 */       if (row.size() != this.m_fieldMapping.size())
/*     */       {
/* 328 */         Report.trace("archiver", "Row size mismatch. Row:" + row.toString() + " Expecting " + this.m_fieldMapping.size() + " fields.", null);
/*     */       }
/*     */       else
/*     */       {
/* 333 */         this.m_values.add(row);
/*     */       }
/*     */     }
/* 336 */     return true;
/*     */   }
/*     */ 
/*     */   protected boolean initDeleteTableFields(String pkNames, String pkTypes, String createTS, String modifiedTS, String sourceID, IdcDateFormat dateFormat, DataResultSet origTable)
/*     */   {
/* 343 */     Vector colNames = StringUtils.parseArray(pkNames, ',', '^');
/* 344 */     Vector types = StringUtils.parseArray(pkTypes, ',', '^');
/* 345 */     int typeSize = types.size();
/* 346 */     int colSize = colNames.size();
/*     */ 
/* 348 */     if (typeSize == 0)
/*     */     {
/* 350 */       for (int i = 0; i < colSize; ++i)
/*     */       {
/* 352 */         types.addElement("Text");
/*     */       }
/*     */     }
/* 355 */     else if (colSize != typeSize)
/*     */     {
/* 357 */       Report.trace("archiver", "Unable to convert delete table. Size mismatch in column names:" + colNames + " and types:" + pkTypes + ". Row Skipped", null);
/*     */ 
/* 360 */       return false;
/*     */     }
/*     */ 
/* 363 */     if (createTS != null)
/*     */     {
/* 365 */       colNames.addElement(createTS);
/* 366 */       types.addElement("Date");
/*     */     }
/* 368 */     if (modifiedTS != null)
/*     */     {
/* 370 */       colNames.addElement(modifiedTS);
/* 371 */       types.addElement("Date");
/*     */     }
/*     */ 
/* 374 */     if (sourceID != null)
/*     */     {
/* 376 */       colNames.addElement(sourceID);
/* 377 */       types.addElement("Text");
/*     */     }
/*     */ 
/* 380 */     FieldInfo[] fi = new FieldInfo[colNames.size()];
/* 381 */     for (int i = 0; i < fi.length; ++i)
/*     */     {
/* 383 */       String name = (String)colNames.elementAt(i);
/* 384 */       String type = (String)types.elementAt(i);
/*     */ 
/* 386 */       fi[i] = new FieldInfo();
/* 387 */       fi[i].m_name = name;
/* 388 */       if (type.equalsIgnoreCase("date"))
/*     */       {
/* 390 */         fi[i].m_index = 5;
/*     */       }
/* 392 */       else if (type.equalsIgnoreCase("int"))
/*     */       {
/* 394 */         fi[i].m_index = 3;
/*     */       }
/* 396 */       else if (type.equalsIgnoreCase("yes/no"))
/*     */       {
/* 398 */         fi[i].m_index = 1;
/*     */       }
/*     */ 
/* 401 */       FieldInfo temp = new FieldInfo();
/* 402 */       origTable.getFieldInfo(name, temp);
/* 403 */       if (temp.m_maxLen <= 0)
/*     */         continue;
/* 405 */       fi[i].m_maxLen = temp.m_maxLen;
/*     */     }
/*     */ 
/* 409 */     initFields(fi, dateFormat);
/*     */ 
/* 411 */     return getNumFields() == fi.length;
/*     */   }
/*     */ 
/*     */   public boolean markRowSkipped()
/*     */   {
/* 417 */     this.m_skippedRowMap.put("" + this.m_currentRow, "skipped");
/* 418 */     this.m_skippedRows.addElement(new Integer(this.m_currentRow));
/* 419 */     return true;
/*     */   }
/*     */ 
/*     */   public void retrieveParentIndexes(int index, List parentRows)
/*     */   {
/* 424 */     List row = (List)this.m_values.get(index);
/* 425 */     String rowStr = row.toString();
/* 426 */     String hashCode = "" + rowStr.hashCode();
/* 427 */     String sums = (String)this.m_rowHash.get(hashCode);
/* 428 */     String sum = getSum(rowStr);
/* 429 */     int begin = sums.indexOf(sum);
/* 430 */     if (begin == -1)
/*     */     {
/* 433 */       Report.trace("archiver", "Row does not exist in hash. Sum:" + sum + " Row:" + rowStr, null);
/*     */ 
/* 435 */       return;
/*     */     }
/* 437 */     begin = sums.indexOf(45, begin);
/* 438 */     if (begin == -1)
/*     */     {
/* 441 */       Report.trace("archiver", "Cannot retrieve row number. String:" + sums + ". Sum:" + sum, null);
/*     */     }
/*     */ 
/* 444 */     ++begin;
/* 445 */     int end = sums.indexOf(124, begin);
/* 446 */     if (end == -1)
/*     */     {
/* 448 */       end = sums.length();
/*     */     }
/*     */ 
/* 451 */     String indexStr = sums.substring(begin, end);
/* 452 */     parseParentIndexes(indexStr, parentRows);
/*     */   }
/*     */ 
/*     */   public int[] retrieveParentIndexesOfCurrentRow()
/*     */   {
/* 457 */     Vector parentRows = new IdcVector();
/*     */ 
/* 459 */     retrieveParentIndexes(this.m_currentRow, parentRows);
/* 460 */     int size = parentRows.size();
/* 461 */     int[] indexArray = new int[size];
/* 462 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 464 */       String str = (String)parentRows.elementAt(i);
/*     */       try
/*     */       {
/* 467 */         indexArray[i] = Integer.parseInt(str);
/*     */       }
/*     */       catch (NumberFormatException e)
/*     */       {
/* 472 */         Report.trace("archiver", null, e);
/*     */       }
/*     */     }
/* 475 */     return indexArray;
/*     */   }
/*     */ 
/*     */   public int[] retrieveParentIndexesOfSkippedRows()
/*     */   {
/* 480 */     int size = this.m_skippedRows.size();
/*     */ 
/* 482 */     Vector skippedParentRows = new IdcVector();
/* 483 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 485 */       int index = ((Integer)this.m_skippedRows.elementAt(i)).intValue();
/* 486 */       retrieveParentIndexes(index, skippedParentRows);
/*     */     }
/* 488 */     size = skippedParentRows.size();
/* 489 */     int[] skippedArray = new int[size];
/* 490 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 492 */       String str = (String)skippedParentRows.elementAt(i);
/*     */       try
/*     */       {
/* 495 */         skippedArray[i] = Integer.parseInt(str);
/*     */       }
/*     */       catch (NumberFormatException e)
/*     */       {
/* 500 */         Report.trace("archiver", null, e);
/*     */       }
/*     */     }
/* 503 */     sort(skippedArray);
/* 504 */     return skippedArray;
/*     */   }
/*     */ 
/*     */   protected void parseParentIndexes(String indexStr, List skippedRows)
/*     */   {
/* 509 */     int begin = 0;
/* 510 */     int end = -1;
/* 511 */     int size = indexStr.length();
/* 512 */     while (begin < size)
/*     */     {
/* 514 */       end = indexStr.indexOf(44, begin);
/* 515 */       if (end == -1)
/*     */       {
/* 517 */         end = indexStr.length();
/*     */       }
/* 519 */       String index = indexStr.substring(begin, end);
/*     */       try
/*     */       {
/* 522 */         Integer.parseInt(index);
/* 523 */         skippedRows.add(index);
/*     */       }
/*     */       catch (NumberFormatException e)
/*     */       {
/* 527 */         Report.trace("archiver", "Parent index error in hash.", e);
/*     */       }
/* 529 */       begin = end + 1;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void sort(int[] array)
/*     */   {
/* 539 */     for (int i = (array.length - 1) / 2; i >= 0; --i)
/*     */     {
/* 541 */       updateHeap(array, i, array.length - 1);
/*     */     }
/*     */ 
/* 544 */     for (int i = array.length - 1; i >= 1; --i)
/*     */     {
/* 547 */       int tmp = array[0];
/* 548 */       array[0] = array[i];
/* 549 */       array[i] = tmp;
/* 550 */       updateHeap(array, 0, i - 1);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateHeap(int[] array, int topIndex, int len)
/*     */   {
/* 557 */     int curIndex = topIndex * 2;
/* 558 */     while (curIndex <= len)
/*     */     {
/*     */       int maxIndex;
/*     */       int maxIndex;
/* 560 */       if ((curIndex == len) || (array[curIndex] > array[(curIndex + 1)]))
/*     */       {
/* 562 */         maxIndex = curIndex;
/*     */       }
/*     */       else
/*     */       {
/* 566 */         maxIndex = curIndex + 1;
/*     */       }
/*     */ 
/* 569 */       if (array[topIndex] >= array[maxIndex])
/*     */         return;
/* 571 */       int temp = array[topIndex];
/* 572 */       array[topIndex] = array[maxIndex];
/* 573 */       array[maxIndex] = temp;
/* 574 */       topIndex = maxIndex;
/*     */ 
/* 580 */       curIndex = topIndex * 2;
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean firstImportedRow()
/*     */   {
/* 586 */     boolean isSuccess = first();
/* 587 */     while ((isSuccess) && (isCurrentRowSkipped()))
/*     */     {
/* 589 */       isSuccess = next();
/*     */     }
/* 591 */     return isSuccess;
/*     */   }
/*     */ 
/*     */   public boolean nextImportedRow()
/*     */   {
/* 596 */     if ((!next()) || (!isCurrentRowSkipped()));
/* 601 */     boolean isSuccess = isRowPresent();
/* 602 */     return isSuccess;
/*     */   }
/*     */ 
/*     */   public boolean isCurrentRowSkipped()
/*     */   {
/* 607 */     boolean isSkipped = false;
/* 608 */     if ((isRowPresent()) && (this.m_skippedRowMap.get("" + this.m_currentRow) != null))
/*     */     {
/* 610 */       isSkipped = true;
/*     */     }
/* 612 */     return isSkipped;
/*     */   }
/*     */ 
/*     */   public boolean getFieldInfo(String fieldName, FieldInfo info)
/*     */   {
/* 618 */     boolean foundIt = false;
/* 619 */     if (!super.getFieldInfo(fieldName, info))
/*     */     {
/* 621 */       int size = this.m_fieldList.size();
/* 622 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 624 */         FieldInfo tmpInfo = (FieldInfo)this.m_fieldList.get(i);
/* 625 */         if (!tmpInfo.m_name.equalsIgnoreCase(fieldName))
/*     */           continue;
/* 627 */         info.copy(tmpInfo);
/* 628 */         foundIt = true;
/* 629 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 635 */       foundIt = true;
/*     */     }
/* 637 */     return foundIt;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 642 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 90166 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ImportTableDataResultSet
 * JD-Core Version:    0.5.4
 */