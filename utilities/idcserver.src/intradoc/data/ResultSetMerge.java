/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.DynamicDataUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ResultSetMerge
/*     */ {
/*     */   public static final int F_APPEND_ONLY = 1;
/*     */   public static final int F_REPLACE_ONLY = 2;
/*     */   public static final int F_REPLACE_ONLY_FIRST_ROW_FOUND = 4;
/*     */   public static final int F_DELETE_ONLY = 8;
/*     */   public static final int F_MERGE_FIELDS = 16;
/*     */   public static final int F_MERGE_USE_WILDCARD = 32;
/*     */   public static final int F_IGNORE_CASE = 64;
/*     */   public static final int F_SKIP_DUPLICATES = 128;
/* 100 */   public ResultSet m_targetRset = null;
/*     */ 
/* 105 */   public ResultSet m_sourceRset = null;
/*     */ 
/* 111 */   public Set<String> m_mergeAppendColumns = null;
/*     */ 
/* 117 */   public char m_mergeAppendSep = ':';
/* 118 */   public char m_mergeAppendEq = '=';
/*     */ 
/* 125 */   public String m_colKey = null;
/*     */ 
/* 130 */   public int m_maxRows = -1;
/*     */ 
/* 135 */   public int m_flags = 0;
/*     */ 
/*     */   public ResultSetMerge(ResultSet targetRset, ResultSet sourceRset)
/*     */   {
/* 139 */     init(targetRset, sourceRset, 0);
/*     */   }
/*     */ 
/*     */   public ResultSetMerge(ResultSet targetRset, ResultSet sourceRset, int flags)
/*     */   {
/* 144 */     init(targetRset, sourceRset, flags);
/*     */   }
/*     */ 
/*     */   public void init(ResultSet targetRset, ResultSet sourceRset, int flags)
/*     */   {
/* 149 */     this.m_targetRset = targetRset;
/* 150 */     this.m_sourceRset = sourceRset;
/* 151 */     this.m_flags = flags;
/*     */   }
/*     */ 
/*     */   public void merge() throws DataException, ServiceException
/*     */   {
/* 156 */     if (this.m_targetRset == this.m_sourceRset)
/*     */     {
/* 158 */       Report.trace("system", "Resultset being merged into itself.  Aborting", null);
/* 159 */       return;
/*     */     }
/*     */ 
/* 162 */     int k1 = -1;
/* 163 */     int k2 = -1;
/* 164 */     FieldInfo fi1 = new FieldInfo();
/* 165 */     FieldInfo fi2 = new FieldInfo();
/*     */ 
/* 168 */     DataResultSet sourceDrset = null;
/* 169 */     if (this.m_sourceRset instanceof DataResultSet)
/*     */     {
/* 171 */       sourceDrset = (DataResultSet)this.m_sourceRset;
/*     */     }
/* 173 */     DataResultSet targetDrset = null;
/* 174 */     if (this.m_targetRset instanceof DataResultSet)
/*     */     {
/* 176 */       targetDrset = (DataResultSet)this.m_targetRset;
/*     */     }
/*     */     else
/*     */     {
/* 181 */       throw new ServiceException(null, -24, IdcMessageFactory.lc("syRsetMustBeTypeDrset", new Object[0]));
/*     */     }
/*     */ 
/* 184 */     boolean alwaysAppend = (this.m_flags & 0x1) != 0;
/* 185 */     boolean replaceOnly = (this.m_flags & 0x2) != 0;
/* 186 */     boolean replaceFirstRowFound = (this.m_flags & 0x4) != 0;
/* 187 */     boolean mergeFields = (this.m_flags & 0x10) != 0;
/* 188 */     boolean doWildcard = (this.m_flags & 0x20) != 0;
/* 189 */     boolean deleteOnly = (this.m_flags & 0x8) != 0;
/* 190 */     boolean ignoreCase = (this.m_flags & 0x40) != 0;
/* 191 */     boolean skipDuplicates = (this.m_flags & 0x80) != 0;
/*     */ 
/* 193 */     boolean isDataRS = sourceDrset != null;
/* 194 */     boolean simpleMerge = true;
/* 195 */     boolean mergedFields = false;
/*     */ 
/* 198 */     if ((this.m_colKey == null) || (this.m_colKey.length() == 0) || (this.m_colKey.equalsIgnoreCase("null")) || (this.m_colKey.charAt(0) == '-'))
/*     */     {
/* 201 */       if (deleteOnly)
/*     */       {
/* 203 */         throw new DataException(null, "syMergeKeyMissing", new Object[] { this.m_colKey });
/*     */       }
/* 205 */       alwaysAppend = true;
/*     */     }
/*     */     else
/*     */     {
/* 209 */       boolean sourceMissing = !this.m_sourceRset.getFieldInfo(this.m_colKey, fi2);
/* 210 */       boolean targetMissing = !this.m_targetRset.getFieldInfo(this.m_colKey, fi1);
/* 211 */       if ((targetMissing) && (mergeFields))
/*     */       {
/* 213 */         targetMissing = false;
/* 214 */         int curNumFields = this.m_targetRset.getNumFields();
/* 215 */         fi1.copy(fi2);
/* 216 */         fi1.m_index = curNumFields;
/* 217 */         targetDrset.m_fieldList.add(fi1);
/* 218 */         targetDrset.m_fieldMapping.put(fi1.m_name, fi1);
/* 219 */         mergedFields = true;
/*     */       }
/* 221 */       if ((targetMissing) || (sourceMissing))
/*     */       {
/* 223 */         throw new DataException(null, "syMergeKeyMissing", new Object[] { this.m_colKey });
/*     */       }
/*     */ 
/* 226 */       k1 = fi1.m_index;
/* 227 */       k2 = fi2.m_index;
/*     */     }
/*     */ 
/* 230 */     if (deleteOnly)
/*     */     {
/* 236 */       Map valuesUsed = new HashMap();
/* 237 */       Vector newRows = new IdcVector();
/* 238 */       for (this.m_sourceRset.first(); this.m_sourceRset.isRowPresent(); this.m_sourceRset.next())
/*     */       {
/* 240 */         String val = this.m_sourceRset.getStringValue(k2);
/* 241 */         if (ignoreCase)
/*     */         {
/* 243 */           val = val.toLowerCase();
/*     */         }
/* 245 */         valuesUsed.put(val, "1");
/*     */       }
/*     */ 
/* 248 */       int curRow = targetDrset.m_currentRow;
/* 249 */       for (targetDrset.first(); targetDrset.isRowPresent(); targetDrset.next())
/*     */       {
/* 251 */         String compareVal = this.m_targetRset.getStringValue(k1);
/* 252 */         if (ignoreCase)
/*     */         {
/* 254 */           compareVal = compareVal.toLowerCase();
/*     */         }
/* 256 */         if (valuesUsed.get(compareVal) == null)
/*     */         {
/* 258 */           newRows.addElement(targetDrset.getCurrentRowValues());
/*     */         }
/*     */         else
/*     */         {
/* 263 */           if (targetDrset.m_currentRow > curRow)
/*     */             continue;
/* 265 */           --curRow;
/*     */         }
/*     */       }
/*     */ 
/* 269 */       targetDrset.m_values = newRows;
/* 270 */       targetDrset.m_currentRow = ((curRow >= 0) ? curRow : 0);
/* 271 */       targetDrset.m_numRows = targetDrset.m_values.size();
/*     */     }
/*     */     else
/*     */     {
/* 276 */       int curNumFields = this.m_targetRset.getNumFields();
/* 277 */       int[] colMap = new int[this.m_sourceRset.getNumFields()];
/* 278 */       boolean[] mergeCols = new boolean[this.m_sourceRset.getNumFields()];
/* 279 */       for (int i = 0; i < colMap.length; ++i)
/*     */       {
/* 281 */         this.m_sourceRset.getIndexFieldInfo(i, fi2);
/* 282 */         if (this.m_targetRset.getFieldInfo(fi2.m_name, fi1))
/*     */         {
/* 284 */           int index = fi1.m_index;
/* 285 */           colMap[i] = index;
/* 286 */           if (fi2.m_index != index)
/*     */           {
/* 288 */             simpleMerge = false;
/*     */           }
/*     */ 
/*     */         }
/* 293 */         else if (mergeFields)
/*     */         {
/* 295 */           FieldInfo newField = new FieldInfo();
/* 296 */           newField.copy(fi2);
/* 297 */           newField.m_index = curNumFields;
/* 298 */           targetDrset.m_fieldList.add(newField);
/* 299 */           targetDrset.m_fieldMapping.put(newField.m_name, newField);
/* 300 */           colMap[i] = curNumFields;
/* 301 */           if (i != curNumFields)
/*     */           {
/* 303 */             simpleMerge = false;
/*     */           }
/* 305 */           ++curNumFields;
/* 306 */           mergedFields = true;
/*     */         }
/*     */         else
/*     */         {
/* 310 */           colMap[i] = -1;
/* 311 */           simpleMerge = false;
/*     */         }
/*     */ 
/* 315 */         if ((this.m_mergeAppendColumns != null) && (this.m_mergeAppendColumns.contains(fi2.m_name)))
/*     */         {
/* 317 */           simpleMerge = false;
/* 318 */           mergeCols[i] = true;
/*     */         }
/*     */         else
/*     */         {
/* 322 */           mergeCols[i] = false;
/*     */         }
/*     */       }
/* 325 */       if (colMap.length != curNumFields)
/*     */       {
/* 327 */         simpleMerge = false;
/*     */       }
/*     */ 
/* 331 */       int curRow = targetDrset.m_currentRow;
/*     */ 
/* 338 */       int curNumValues = targetDrset.getNumRows();
/* 339 */       String[] valueList = null;
/* 340 */       if (!alwaysAppend)
/*     */       {
/* 342 */         valueList = new String[curNumValues];
/* 343 */         int rowNum = 0;
/* 344 */         for (this.m_targetRset.first(); this.m_targetRset.isRowPresent(); ++rowNum)
/*     */         {
/* 346 */           String val = this.m_targetRset.getStringValue(k1);
/*     */ 
/* 349 */           if ((ignoreCase) && (val != null))
/*     */           {
/* 351 */             val = val.toLowerCase();
/*     */           }
/* 353 */           valueList[rowNum] = val;
/*     */ 
/* 344 */           this.m_targetRset.next();
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 358 */       int count = 0;
/* 359 */       String empty = "";
/* 360 */       for (this.m_sourceRset.first(); this.m_sourceRset.isRowPresent(); ++count)
/*     */       {
/* 362 */         if ((this.m_maxRows > 0) && (count == this.m_maxRows))
/*     */         {
/* 365 */           targetDrset.m_copyAborted = true;
/* 366 */           break;
/*     */         }
/*     */ 
/* 369 */         boolean moreRowsToMerge = true;
/* 370 */         boolean foundMatchingRow = false;
/* 371 */         int rowSearchIndex = 0;
/* 372 */         String keyVal = null;
/* 373 */         if (!alwaysAppend)
/*     */         {
/* 376 */           keyVal = this.m_sourceRset.getStringValue(k2);
/* 377 */           if (keyVal == null)
/*     */           {
/* 379 */             keyVal = "";
/*     */           }
/* 381 */           else if (ignoreCase)
/*     */           {
/* 383 */             keyVal = keyVal.toLowerCase();
/*     */           }
/*     */         }
/*     */ 
/* 387 */         while (moreRowsToMerge)
/*     */         {
/* 392 */           Vector v = null;
/* 393 */           Vector mergeRow = null;
/* 394 */           boolean append = false;
/*     */ 
/* 396 */           while ((!alwaysAppend) && 
/* 399 */             (rowSearchIndex < curNumValues))
/*     */           {
/* 401 */             boolean foundIt = false;
/* 402 */             String val = valueList[rowSearchIndex];
/* 403 */             if (val != null)
/*     */             {
/* 405 */               if (doWildcard)
/*     */               {
/* 407 */                 if (StringUtils.matchEx(val, keyVal, true, ignoreCase))
/*     */                 {
/* 409 */                   foundIt = true;
/*     */                 }
/*     */ 
/*     */               }
/*     */               else {
/* 414 */                 foundIt = val.equals(keyVal);
/*     */               }
/*     */             }
/* 417 */             if (foundIt)
/*     */             {
/* 419 */               foundMatchingRow = true;
/* 420 */               targetDrset.m_currentRow = rowSearchIndex;
/* 421 */               v = targetDrset.getRowValues(rowSearchIndex);
/*     */ 
/* 424 */               for (int a = v.size(); a < curNumFields; ++a)
/*     */               {
/* 426 */                 v.add(empty);
/*     */               }
/*     */ 
/* 430 */               ++rowSearchIndex;
/* 431 */               break;
/*     */             }
/* 433 */             ++rowSearchIndex;
/*     */           }
/*     */ 
/* 437 */           if ((skipDuplicates) && (foundMatchingRow))
/*     */           {
/*     */             break;
/*     */           }
/*     */ 
/* 442 */           if (v == null)
/*     */           {
/* 444 */             if (foundMatchingRow) break; if (replaceOnly)
/*     */             {
/*     */               break;
/*     */             }
/*     */ 
/* 449 */             append = true;
/*     */           }
/*     */ 
/* 452 */           if ((simpleMerge) && (isDataRS))
/*     */           {
/* 454 */             mergeRow = (Vector)sourceDrset.getRowValues(sourceDrset.getCurrentRow()).clone();
/*     */           }
/* 458 */           else if (append)
/*     */           {
/* 460 */             mergeRow = targetDrset.createEmptyRow();
/*     */           }
/*     */           else
/*     */           {
/* 464 */             mergeRow = v;
/*     */           }
/*     */ 
/* 469 */           if (append)
/*     */           {
/* 471 */             targetDrset.addRow(mergeRow);
/*     */           }
/* 475 */           else if ((simpleMerge) && (isDataRS))
/*     */           {
/* 477 */             targetDrset.m_values.set(targetDrset.m_currentRow, mergeRow);
/*     */           }
/*     */ 
/* 480 */           if ((!simpleMerge) || (!isDataRS))
/*     */           {
/* 482 */             for (i = 0; i < colMap.length; ++i)
/*     */             {
/* 484 */               int index = i;
/* 485 */               if (!simpleMerge)
/*     */               {
/* 487 */                 index = colMap[i];
/*     */               }
/* 489 */               if (index < 0)
/*     */                 continue;
/* 491 */               String val = this.m_sourceRset.getStringValue(i);
/* 492 */               if ((foundMatchingRow) && (mergeCols[i] != 0))
/*     */               {
/* 494 */                 String prevVal = targetDrset.getStringValue(index);
/* 495 */                 if ((prevVal != null) && (prevVal.length() > 0))
/*     */                 {
/* 497 */                   val = prevVal + this.m_mergeAppendSep + val;
/* 498 */                   val = DynamicDataUtils.mergeAppendColumnValue(val, this.m_mergeAppendSep, this.m_mergeAppendEq);
/*     */                 }
/*     */               }
/* 501 */               mergeRow.setElementAt(val, index);
/*     */             }
/*     */           }
/*     */ 
/* 505 */           if ((replaceFirstRowFound) || (append))
/*     */           {
/* 507 */             moreRowsToMerge = false;
/*     */           }
/*     */         }
/* 360 */         this.m_sourceRset.next();
/*     */       }
/*     */ 
/* 512 */       if (mergedFields)
/*     */       {
/* 514 */         int nrows = targetDrset.m_values.size();
/* 515 */         int newlen = targetDrset.m_fieldList.size();
/* 516 */         for (int j = 0; j < nrows; ++j)
/*     */         {
/* 518 */           List v = (List)targetDrset.m_values.get(j);
/* 519 */           int start = v.size();
/* 520 */           for (int k = start; k < newlen; ++k)
/*     */           {
/* 522 */             v.add(empty);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 527 */       targetDrset.m_currentRow = curRow;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 534 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 91535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ResultSetMerge
 * JD-Core Version:    0.5.4
 */