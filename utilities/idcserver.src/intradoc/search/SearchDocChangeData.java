/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcDebugOutput;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class SearchDocChangeData
/*     */   implements IdcDebugOutput
/*     */ {
/*     */   public static final int E_VALID = 0;
/*     */   public static final int E_DATES_NOT_INCREMENTING = 1;
/*     */   public static final int E_CACHE_FLUSHED = 2;
/*     */   public boolean m_isInit;
/*     */   public boolean m_differentialUpdatesAllowed;
/*     */   public Map<String, SearchDocChangeItem> m_classReferences;
/*     */   public Map<String, SearchDocChangeItem> m_idReferences;
/*     */   public SearchDocChangeItem m_startChangeItem;
/*     */   public SearchDocChangeItem m_endChangeItem;
/*     */   public int m_currentAge;
/*     */   public boolean m_hasValidEndTime;
/*     */   public long m_lastEndTime;
/*     */   public int m_changeDataCount;
/*     */   public int m_maximumItemsAllowed;
/*     */   public boolean m_isValid;
/*     */   public int m_errorCode;
/*     */   public String m_errorMsg;
/*     */   public List<SearchDocChangeItem> m_generationChangeDataPointers;
/*     */   public boolean m_isValidFastData;
/*     */   public SearchDocChangeItem[] m_fastChangesArray;
/*     */   public Map<String, SearchDocChangeItem> m_fastRevClassReferences;
/*     */   public Map<String, SearchDocChangeItem> m_fastIdReferences;
/*     */   public long m_fastChangesStartTime;
/*     */ 
/*     */   public SearchDocChangeData()
/*     */   {
/* 166 */     this.m_maximumItemsAllowed = 1000;
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 175 */     appendable.append("" + this.m_changeDataCount + " items");
/* 176 */     appendable.append(" (");
/* 177 */     StringUtils.appendDebugProperty(appendable, "isValid", "" + this.m_isValid, false);
/* 178 */     StringUtils.appendDebugProperty(appendable, "isValidFastData", "" + this.m_isValidFastData, true);
/*     */ 
/* 180 */     StringUtils.appendDebugProperty(appendable, "lastEndTime", new Date(this.m_lastEndTime), true);
/*     */ 
/* 182 */     if (this.m_fastChangesStartTime > 0L)
/*     */     {
/* 184 */       StringUtils.appendDebugProperty(appendable, "fastChangesStartTime", new Date(this.m_fastChangesStartTime), true);
/*     */     }
/*     */ 
/* 187 */     StringUtils.appendDebugProperty(appendable, "errorCode", "" + this.m_errorCode, true);
/* 188 */     appendable.append("\nnchangeItems=");
/* 189 */     appendLinkedListToDebug(appendable, this.m_startChangeItem);
/* 190 */     appendable.append(")");
/*     */   }
/*     */ 
/*     */   public void appendLinkedListToDebug(IdcAppendable appendable, SearchDocChangeItem items)
/*     */   {
/* 195 */     int count = 0;
/* 196 */     int max = 100;
/* 197 */     appendable.append("[");
/* 198 */     for (SearchDocChangeItem cur = items; cur != null; cur = cur.m_next)
/*     */     {
/* 200 */       if (count > 0)
/*     */       {
/* 202 */         appendable.append("\n");
/*     */       }
/* 204 */       appendable.append("[");
/* 205 */       cur.appendDebugFormat(appendable);
/* 206 */       appendable.append("]");
/* 207 */       ++count;
/* 208 */       if (count <= max)
/*     */         continue;
/* 210 */       appendable.append("\n[Too Many Items (Over " + max + ") To Show In Debug]");
/*     */     }
/*     */ 
/* 213 */     appendable.append("]");
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 223 */     IdcStringBuilder output = new IdcStringBuilder();
/* 224 */     appendDebugFormat(output);
/* 225 */     return output.toString();
/*     */   }
/*     */ 
/*     */   public void resetToValidState()
/*     */   {
/* 231 */     this.m_startChangeItem = null;
/* 232 */     this.m_endChangeItem = null;
/* 233 */     this.m_changeDataCount = 0;
/* 234 */     this.m_hasValidEndTime = false;
/* 235 */     this.m_lastEndTime = 0L;
/* 236 */     this.m_currentAge = 0;
/* 237 */     this.m_classReferences = new HashMap();
/* 238 */     this.m_idReferences = new HashMap();
/* 239 */     this.m_generationChangeDataPointers = new ArrayList();
/* 240 */     this.m_isValidFastData = false;
/* 241 */     this.m_fastChangesArray = null;
/* 242 */     this.m_fastIdReferences = null;
/* 243 */     this.m_fastRevClassReferences = null;
/* 244 */     this.m_errorCode = 0;
/* 245 */     this.m_errorMsg = null;
/* 246 */     this.m_isValid = true;
/*     */   }
/*     */ 
/*     */   public void fillCachedAndStatusData(SearchDocChangeData data)
/*     */   {
/* 251 */     data.m_differentialUpdatesAllowed = this.m_differentialUpdatesAllowed;
/* 252 */     data.m_lastEndTime = this.m_lastEndTime;
/* 253 */     data.m_hasValidEndTime = this.m_hasValidEndTime;
/* 254 */     data.m_currentAge = this.m_currentAge;
/* 255 */     data.m_errorCode = this.m_errorCode;
/* 256 */     data.m_errorMsg = this.m_errorMsg;
/* 257 */     data.m_changeDataCount = this.m_changeDataCount;
/* 258 */     data.m_maximumItemsAllowed = this.m_maximumItemsAllowed;
/* 259 */     data.m_isValid = this.m_isValid;
/* 260 */     data.m_isValidFastData = this.m_isValidFastData;
/* 261 */     data.m_fastChangesArray = this.m_fastChangesArray;
/* 262 */     data.m_fastChangesStartTime = this.m_fastChangesStartTime;
/* 263 */     data.m_fastRevClassReferences = this.m_fastRevClassReferences;
/* 264 */     data.m_fastIdReferences = this.m_fastIdReferences;
/*     */   }
/*     */ 
/*     */   public void addToLinkedList(SearchDocChangeItem changeItem)
/*     */   {
/* 274 */     if (this.m_endChangeItem == null)
/*     */     {
/* 276 */       assert (this.m_startChangeItem == null);
/* 277 */       this.m_endChangeItem = changeItem;
/*     */     }
/*     */     else
/*     */     {
/* 281 */       changeItem.insertBefore(this.m_startChangeItem);
/*     */     }
/*     */ 
/* 285 */     this.m_startChangeItem = changeItem;
/* 286 */     this.m_changeDataCount += 1;
/* 287 */     this.m_isValidFastData = false;
/*     */   }
/*     */ 
/*     */   public void removeFromLinkedList(SearchDocChangeItem changeItem)
/*     */   {
/* 296 */     if (changeItem == this.m_endChangeItem)
/*     */     {
/* 298 */       this.m_endChangeItem = this.m_endChangeItem.m_prev;
/*     */     }
/* 300 */     if (changeItem == this.m_startChangeItem)
/*     */     {
/* 302 */       this.m_startChangeItem = changeItem.m_next;
/*     */     }
/* 304 */     changeItem.remove();
/* 305 */     this.m_changeDataCount -= 1;
/* 306 */     this.m_isValidFastData = false;
/*     */   }
/*     */ 
/*     */   public void createFastChangeList()
/*     */   {
/* 311 */     if (this.m_isValidFastData)
/*     */     {
/* 313 */       return;
/*     */     }
/*     */ 
/* 316 */     int numChanges = 0;
/* 317 */     SearchDocChangeItem[] changeArray = null;
/* 318 */     for (int i = 0; i < 2; ++i)
/*     */     {
/* 320 */       boolean isCountingOnly = i == 0;
/* 321 */       if (!isCountingOnly)
/*     */       {
/* 323 */         changeArray = new SearchDocChangeItem[numChanges];
/*     */       }
/*     */ 
/* 326 */       int index = 0;
/* 327 */       SearchDocChangeItem changeItem = this.m_startChangeItem;
/* 328 */       for (; changeItem != null; changeItem = changeItem.m_next)
/*     */       {
/* 330 */         boolean isDelete = changeItem.m_changeType == SearchDocChangeItem.CHANGE_DELETE;
/* 331 */         if (isDelete) {
/*     */           continue;
/*     */         }
/* 334 */         if (!isCountingOnly)
/*     */         {
/* 336 */           changeArray[index] = changeItem;
/*     */         }
/* 338 */         ++index;
/*     */       }
/*     */ 
/* 341 */       if (!isCountingOnly)
/*     */         continue;
/* 343 */       numChanges = index;
/*     */     }
/*     */ 
/* 346 */     this.m_fastChangesArray = changeArray;
/* 347 */     this.m_fastRevClassReferences = ((Map)((HashMap)this.m_classReferences).clone());
/*     */ 
/* 349 */     this.m_fastIdReferences = ((Map)((HashMap)this.m_idReferences).clone());
/*     */ 
/* 351 */     this.m_isValidFastData = true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 356 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81312 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchDocChangeData
 * JD-Core Version:    0.5.4
 */