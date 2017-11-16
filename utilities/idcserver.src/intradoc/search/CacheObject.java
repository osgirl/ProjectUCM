/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import java.util.HashMap;
/*     */ 
/*     */ public class CacheObject
/*     */ {
/*  32 */   public static int CACHE_IS_EMPTY = 0;
/*  33 */   public static int CACHE_IS_DOC_LIST = 1;
/*  34 */   public static int CACHE_IS_DOC_INFO = 2;
/*     */ 
/*  38 */   public static String[] CACHE_TYPE_STRINGS = { "Empty", "List", "Info" };
/*     */   public int m_type;
/*     */   public String m_query;
/*     */   public boolean m_isValid;
/*     */   public boolean m_isPending;
/*     */   public CacheObject m_next;
/*     */   public CacheObject m_prev;
/*     */   public ProviderCache m_providerCache;
/*     */   public CacheDataDesign m_dataDesign;
/*     */   public CacheRow m_resultSetRow;
/*     */   public SearchListItem[] m_docsList;
/*     */   public ParsedQueryFieldMap[] m_parsedFieldPointersArray;
/*     */   public int[] m_docsListSync;
/*     */   public boolean m_isSorted;
/*     */   public boolean m_sortIsAscending;
/*     */   public FieldInfo m_sortFieldInfo;
/*     */   public String[] m_supplementaryDataKeys;
/*     */   public String[] m_supplementaryDataValues;
/*     */   public boolean m_hasAllPossibleRows;
/*     */   public int m_rowCount;
/*     */   public int m_requestedRows;
/*     */   public int m_ageCounter;
/*     */   public long m_generationalCounter;
/*     */   public long m_lastAccessTime;
/*     */   public long m_sharedSearchTime;
/*     */   public boolean m_isLinked;
/*     */   public HashMap m_additionalResultSets;
/*     */ 
/*     */   public CacheObject(CacheUpdateParameters updateParameters)
/*     */   {
/* 135 */     this.m_query = updateParameters.m_query;
/* 136 */     this.m_type = updateParameters.m_updateType;
/*     */ 
/* 139 */     this.m_providerCache = updateParameters.m_providerCache;
/* 140 */     this.m_dataDesign = updateParameters.m_cacheDataDesign;
/* 141 */     this.m_ageCounter = this.m_providerCache.m_currentAge;
/*     */ 
/* 143 */     update(updateParameters);
/*     */   }
/*     */ 
/*     */   public int update(CacheUpdateParameters updateParameters)
/*     */   {
/* 156 */     this.m_lastAccessTime = updateParameters.m_startTime;
/* 157 */     this.m_sharedSearchTime = updateParameters.m_sharedSearchTime;
/* 158 */     int oldCount = this.m_rowCount;
/* 159 */     this.m_isValid = false;
/*     */ 
/* 161 */     if ((this.m_type != CACHE_IS_EMPTY) && (updateParameters.m_cacheDataDesign != null))
/*     */     {
/* 163 */       if ((this.m_type == CACHE_IS_DOC_INFO) && (updateParameters.m_updateHasResultSetRow))
/*     */       {
/* 165 */         this.m_rowCount = 1;
/* 166 */         this.m_requestedRows = 1;
/*     */ 
/* 170 */         if ((this.m_resultSetRow == null) || (!this.m_resultSetRow.m_dataDesign.equals(updateParameters.m_cacheDataDesign)) || (!StringUtils.isEqualStringArrays(this.m_resultSetRow.m_row, updateParameters.m_updateResultSetRow)))
/*     */         {
/* 175 */           CacheRow oldRow = this.m_resultSetRow;
/* 176 */           this.m_generationalCounter = updateParameters.m_updateGenerationalCounter;
/* 177 */           String[] perQueryValues = null;
/* 178 */           if (updateParameters.m_updateCapturedPerQueryValues != null)
/*     */           {
/* 180 */             perQueryValues = updateParameters.m_updateCapturedPerQueryValues;
/*     */           }
/* 182 */           else if (oldRow != null)
/*     */           {
/* 184 */             perQueryValues = oldRow.m_capturedPerQueryValues;
/*     */           }
/*     */ 
/* 187 */           this.m_resultSetRow = new CacheRow(updateParameters.m_updateResultSetRow, perQueryValues, updateParameters.m_cacheDataDesign, updateParameters.m_updateGenerationalCounter);
/*     */ 
/* 193 */           if ((oldRow != null) && (Report.m_verbose))
/*     */           {
/* 195 */             String msg = "Cache object being replaced, oldRow=" + oldRow.toString() + ", resultSetRow=" + this.m_resultSetRow.toString();
/*     */ 
/* 197 */             Report.debug("searchcache", msg, null);
/*     */           }
/*     */ 
/*     */         }
/* 202 */         else if (updateParameters.m_updateCapturedPerQueryValues != null)
/*     */         {
/* 208 */           this.m_resultSetRow.m_capturedPerQueryValues = updateParameters.m_updateCapturedPerQueryValues;
/*     */         }
/*     */ 
/* 212 */         this.m_hasAllPossibleRows = true;
/* 213 */         this.m_isValid = true;
/*     */       }
/* 215 */       else if (updateParameters.m_updateHasDocsList)
/*     */       {
/* 217 */         this.m_rowCount = updateParameters.m_updateRowCount;
/* 218 */         this.m_requestedRows = updateParameters.m_requestedRows;
/* 219 */         this.m_hasAllPossibleRows = (this.m_rowCount < this.m_requestedRows);
/* 220 */         this.m_docsList = updateParameters.m_updateDocsList;
/* 221 */         this.m_generationalCounter = updateParameters.m_updateGenerationalCounter;
/* 222 */         if (this.m_docsList == null)
/*     */         {
/* 224 */           this.m_docsList = new SearchListItem[0];
/*     */         }
/* 226 */         this.m_isSorted = updateParameters.m_isSorted;
/* 227 */         this.m_sortIsAscending = updateParameters.m_sortIsAscending;
/* 228 */         this.m_sortFieldInfo = updateParameters.m_cacheSortFieldInfo;
/* 229 */         this.m_supplementaryDataKeys = updateParameters.m_updateBinderCaptureKeys;
/* 230 */         this.m_supplementaryDataValues = updateParameters.m_updateBinderCapturedValues;
/* 231 */         this.m_isValid = true;
/*     */       }
/*     */     }
/* 234 */     if (this.m_isValid)
/*     */     {
/* 237 */       this.m_providerCache = updateParameters.m_providerCache;
/* 238 */       this.m_dataDesign = updateParameters.m_cacheDataDesign;
/* 239 */       this.m_ageCounter = this.m_providerCache.m_currentAge;
/*     */     }
/*     */ 
/* 242 */     if (!this.m_isValid)
/*     */     {
/* 244 */       this.m_isValid = false;
/* 245 */       this.m_rowCount = 0;
/* 246 */       this.m_requestedRows = 0;
/*     */     }
/* 248 */     return this.m_rowCount - oldCount;
/*     */   }
/*     */ 
/*     */   public int updateChangedCacheDocList(CacheUpdateParameters updateParameters)
/*     */   {
/* 259 */     if (!updateParameters.m_isValidCache)
/*     */     {
/* 261 */       this.m_isValid = false;
/* 262 */       return 0;
/*     */     }
/* 264 */     int oldCount = this.m_rowCount;
/* 265 */     this.m_docsList = updateParameters.m_cacheDocsList;
/* 266 */     this.m_rowCount = this.m_docsList.length;
/* 267 */     this.m_ageCounter = updateParameters.m_capturedCurrentAge;
/* 268 */     this.m_generationalCounter = updateParameters.m_updateGenerationalCounter;
/* 269 */     this.m_sharedSearchTime = updateParameters.m_sharedSearchTime;
/* 270 */     int rowCountDiff = this.m_rowCount - oldCount;
/* 271 */     if ((this.m_supplementaryDataKeys != null) && (this.m_supplementaryDataKeys.length > 0) && (rowCountDiff != 0))
/*     */     {
/* 273 */       for (int i = 0; i < this.m_supplementaryDataKeys.length; ++i)
/*     */       {
/* 275 */         if (!this.m_supplementaryDataKeys[i].equals("TotalRows"))
/*     */           continue;
/* 277 */         int totalRows = NumberUtils.parseInteger(this.m_supplementaryDataValues[i], -1);
/* 278 */         totalRows += rowCountDiff;
/* 279 */         if (totalRows < this.m_rowCount)
/*     */         {
/* 281 */           Report.trace("searchcache", "Fixed up total rows " + totalRows + " less than row count " + this.m_rowCount + " with previous count " + oldCount, null);
/*     */ 
/* 284 */           totalRows = this.m_rowCount;
/*     */         }
/* 286 */         this.m_supplementaryDataValues[i] = ("" + totalRows);
/*     */       }
/*     */     }
/*     */ 
/* 290 */     return rowCountDiff;
/*     */   }
/*     */ 
/*     */   public void insertBefore(CacheObject obj)
/*     */   {
/* 295 */     this.m_prev = obj.m_prev;
/* 296 */     if (this.m_prev != null)
/*     */     {
/* 298 */       this.m_prev.m_next = this;
/*     */     }
/*     */ 
/* 301 */     obj.m_prev = this;
/* 302 */     this.m_next = obj;
/*     */   }
/*     */ 
/*     */   public void insertAfter(CacheObject obj)
/*     */   {
/* 307 */     this.m_next = obj.m_next;
/* 308 */     if (this.m_next != null)
/*     */     {
/* 310 */       this.m_next.m_prev = this;
/*     */     }
/*     */ 
/* 313 */     obj.m_next = this;
/* 314 */     this.m_prev = obj;
/*     */   }
/*     */ 
/*     */   public void remove()
/*     */   {
/* 319 */     CacheObject next = this.m_next;
/* 320 */     if (this.m_next != null)
/*     */     {
/* 322 */       this.m_next.m_prev = this.m_prev;
/* 323 */       this.m_next = null;
/*     */     }
/*     */ 
/* 326 */     if (this.m_prev == null)
/*     */       return;
/* 328 */     this.m_prev.m_next = next;
/* 329 */     this.m_prev = null;
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 339 */     appendable.append(this.m_query);
/* 340 */     appendable.append(" (");
/* 341 */     appendable.append("type=");
/* 342 */     appendable.append(getTypeString(this.m_type));
/* 343 */     appendable.append(" isValid=");
/* 344 */     appendable.append((this.m_isValid) ? "true" : "false");
/* 345 */     appendable.append(" isPending=");
/* 346 */     appendable.append((this.m_isPending) ? "true" : "false");
/* 347 */     appendable.append(" rowCount=");
/* 348 */     NumberUtils.appendLong(appendable, this.m_rowCount);
/* 349 */     appendable.append(" ageCounter=");
/* 350 */     NumberUtils.appendLong(appendable, this.m_ageCounter);
/* 351 */     appendable.append(" lastAccessTime=");
/* 352 */     appendable.append(LocaleUtils.debugDate(this.m_lastAccessTime));
/* 353 */     appendable.append(")");
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 363 */     IdcStringBuilder output = new IdcStringBuilder();
/* 364 */     appendDebugFormat(output);
/* 365 */     return output.toString();
/*     */   }
/*     */ 
/*     */   public static String getTypeString(int index)
/*     */   {
/* 375 */     if ((index < 0) || (index >= CACHE_TYPE_STRINGS.length))
/*     */     {
/* 377 */       index = 0;
/*     */     }
/* 379 */     return CACHE_TYPE_STRINGS[index];
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 384 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70868 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.CacheObject
 * JD-Core Version:    0.5.4
 */