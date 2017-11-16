/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class SearchDocChangeUtils
/*     */ {
/*     */   public static final int F_ADD_ITEM = 1;
/*     */   public static final int F_REMOVE_ITEM = 2;
/*  39 */   protected static final boolean[] m_sync = { false };
/*     */ 
/*     */   public static SearchDocChangeData getOrCreateGlobalSearchChangeData(DataBinder binder, ExecutionContext cxt)
/*     */   {
/*     */     SearchDocChangeData changeData;
/*  52 */     synchronized (m_sync)
/*     */     {
/*  54 */       changeData = (SearchDocChangeData)SharedObjects.getObject("search", "SearchDocChangeData");
/*     */ 
/*  56 */       if (changeData == null)
/*     */       {
/*  58 */         changeData = new SearchDocChangeData();
/*  59 */         changeData.m_maximumItemsAllowed = SharedObjects.getEnvironmentInt("SearchMaxDocChangesCached", changeData.m_maximumItemsAllowed);
/*     */ 
/*  61 */         changeData.m_differentialUpdatesAllowed = SharedObjects.getEnvValueAsBoolean("SearchCacheAllowDiffUpdates", true);
/*     */ 
/*  63 */         SharedObjects.putObject("search", "SearchDocChangeData", changeData);
/*     */       }
/*     */     }
/*  66 */     return changeData;
/*     */   }
/*     */ 
/*     */   public static boolean checkForResetSearchChangeData(SearchDocChangeData data, SearchDocChangeData tempCopy, ExecutionContext cxt)
/*     */   {
/*     */     boolean wasValid;
/*  82 */     synchronized (data)
/*     */     {
/*  84 */       wasValid = data.m_isValid;
/*  85 */       if (!data.m_isValid)
/*     */       {
/*  87 */         data.resetToValidState();
/*     */       }
/*  89 */       if (tempCopy != null)
/*     */       {
/*  91 */         data.fillCachedAndStatusData(tempCopy);
/*     */       }
/*     */     }
/*  94 */     return wasValid;
/*     */   }
/*     */ 
/*     */   public static void getChangeFastData(SearchDocChangeData data, SearchDocChangeData dataCopy)
/*     */   {
/* 105 */     synchronized (data)
/*     */     {
/* 107 */       data.fillCachedAndStatusData(dataCopy);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void setNewAgeAndEndTimeAndGroomData(SearchDocChangeData data, int newCurAge, long newEndTime)
/*     */   {
/* 119 */     synchronized (data)
/*     */     {
/* 121 */       if (data.m_isValid)
/*     */       {
/* 124 */         checkChangeDataSizeLimitsAndCreateFastArray(data);
/* 125 */         data.m_currentAge = newCurAge;
/* 126 */         data.m_lastEndTime = newEndTime;
/* 127 */         if (!data.m_hasValidEndTime)
/*     */         {
/* 130 */           data.m_fastChangesStartTime = newEndTime;
/* 131 */           data.m_hasValidEndTime = true;
/*     */         }
/*     */ 
/* 134 */         if ((data.m_startChangeItem != null) && 
/* 136 */           (!data.m_generationChangeDataPointers.contains(data.m_startChangeItem)))
/*     */         {
/* 138 */           data.m_generationChangeDataPointers.add(data.m_startChangeItem);
/* 139 */           if (Report.m_verbose)
/*     */           {
/* 141 */             Report.debug("searchcache", "setNewAgeAndEndTimeAndGroomData - Add to m_generationChangeDataPointers item=" + data.m_startChangeItem, null);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void checkChangeDataSizeLimitsAndCreateFastArray(SearchDocChangeData data)
/*     */   {
/* 161 */     SearchDocChangeItem lastGenerationItem = null;
/* 162 */     int maxItems = data.m_maximumItemsAllowed;
/* 163 */     if (data.m_generationChangeDataPointers.size() > 0)
/*     */     {
/* 165 */       lastGenerationItem = (SearchDocChangeItem)data.m_generationChangeDataPointers.get(0);
/*     */     }
/* 167 */     boolean removedItems = false;
/* 168 */     while (data.m_changeDataCount > maxItems)
/*     */     {
/* 170 */       SearchDocChangeItem lastItem = data.m_endChangeItem;
/* 171 */       data.removeFromLinkedList(lastItem);
/* 172 */       boolean isDelete = lastItem.m_changeType == SearchDocChangeItem.CHANGE_DELETE;
/* 173 */       Map mapToUpdate = (isDelete) ? data.m_idReferences : data.m_classReferences;
/*     */ 
/* 175 */       String key = (isDelete) ? lastItem.m_specificDocID : lastItem.m_classDocID;
/* 176 */       removeChangeItemFromMap(key, mapToUpdate);
/* 177 */       if (lastItem == lastGenerationItem)
/*     */       {
/* 179 */         data.m_generationChangeDataPointers.remove(0);
/* 180 */         if (data.m_generationChangeDataPointers.size() > 0)
/*     */         {
/* 182 */           lastGenerationItem = (SearchDocChangeItem)data.m_generationChangeDataPointers.get(0);
/*     */         }
/*     */         else
/*     */         {
/* 186 */           lastGenerationItem = null;
/*     */         }
/*     */       }
/* 189 */       removedItems = true;
/*     */     }
/*     */ 
/* 192 */     data.createFastChangeList();
/* 193 */     if (!removedItems)
/*     */       return;
/* 195 */     data.m_fastChangesStartTime = data.m_endChangeItem.m_capturedTime;
/*     */   }
/*     */ 
/*     */   public static void updateSearchChangeData(SearchDocChangeData data, SearchDocChangeItem item, ExecutionContext cxt, int flags)
/*     */   {
/* 210 */     boolean isAdd = (flags & 0x1) != 0;
/* 211 */     boolean isRemove = (flags & 0x2) != 0;
/* 212 */     boolean isDeleteItem = (item.m_changeType & SearchDocChangeItem.CHANGE_DELETE) != 0;
/* 213 */     synchronized (data)
/*     */     {
/* 215 */       if (data.m_isValid)
/*     */       {
/* 217 */         String key = (isDeleteItem) ? item.m_specificDocID : item.m_classDocID;
/* 218 */         Map map = (isDeleteItem) ? data.m_idReferences : data.m_classReferences;
/* 219 */         SearchDocChangeItem curItem = (SearchDocChangeItem)map.get(key);
/* 220 */         if ((isAdd) && (curItem != null) && 
/* 223 */           (item.m_changeTime == curItem.m_changeTime) && 
/* 225 */           (item.m_cacheRow != null) && (curItem.m_cacheRow != null) && 
/* 227 */           (StringUtils.isEqualStringArrays(item.m_cacheRow.m_row, curItem.m_cacheRow.m_row)))
/*     */         {
/* 229 */           if (Report.m_verbose)
/*     */           {
/* 231 */             Report.debug("searchcache", "updateSearchChangeData, suppressing duplicate add item=" + item, null);
/*     */           }
/* 233 */           isAdd = false;
/*     */         }
/*     */ 
/* 238 */         if ((isAdd) || (isRemove))
/*     */         {
/* 240 */           if (curItem != null)
/*     */           {
/* 243 */             int indexOfCurItem = data.m_generationChangeDataPointers.indexOf(curItem);
/* 244 */             if (indexOfCurItem >= 0)
/*     */             {
/* 246 */               if ((curItem.m_next != null) && (curItem.m_itemAge == curItem.m_next.m_itemAge))
/*     */               {
/* 249 */                 if (Report.m_verbose)
/*     */                 {
/* 251 */                   Report.debug("searchcache", "updateSearchChangeData - Change m_generationChangeDataPointers from item=" + curItem + " to item=" + curItem.m_next, null);
/*     */                 }
/*     */ 
/* 255 */                 data.m_generationChangeDataPointers.set(indexOfCurItem, curItem.m_next);
/*     */               }
/*     */               else
/*     */               {
/* 260 */                 if (Report.m_verbose)
/*     */                 {
/* 262 */                   Report.debug("searchcache", "updateSearchChangeData - Remove from m_generationChangeDataPointers curItem=" + curItem, null);
/*     */                 }
/*     */ 
/* 266 */                 data.m_generationChangeDataPointers.remove(indexOfCurItem);
/*     */               }
/*     */             }
/* 269 */             removeChangeItemFromMap(key, map);
/* 270 */             data.removeFromLinkedList(curItem);
/*     */           }
/* 272 */           if (isAdd)
/*     */           {
/* 274 */             addChangeItemToMap(key, item, map);
/* 275 */             data.addToLinkedList(item);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean checkIsUsable(SearchDocChangeData data)
/*     */   {
/* 289 */     boolean isUsable = false;
/* 290 */     synchronized (data)
/*     */     {
/* 292 */       isUsable = (data.m_isValid) && (data.m_hasValidEndTime);
/*     */     }
/* 294 */     return isUsable;
/*     */   }
/*     */ 
/*     */   public static void setToErrorState(SearchDocChangeData data, int errorCode, String errorMsg)
/*     */   {
/* 305 */     synchronized (data)
/*     */     {
/* 307 */       if (data.m_isValid)
/*     */       {
/* 309 */         data.m_isValid = false;
/* 310 */         data.m_errorCode = errorCode;
/* 311 */         data.m_errorMsg = errorMsg;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void addChangeItemToMap(String key, SearchDocChangeItem item, Map<String, SearchDocChangeItem> map)
/*     */   {
/* 329 */     SearchDocChangeItem prevItem = (SearchDocChangeItem)map.put(key, item);
/* 330 */     if (prevItem != null)
/*     */     {
/* 334 */       item.m_refCounterForMapLookup = (prevItem.m_refCounterForMapLookup + 1);
/*     */     }
/*     */     else
/*     */     {
/* 338 */       item.m_refCounterForMapLookup = 1;
/*     */     }
/* 340 */     if (!Report.m_verbose)
/*     */       return;
/* 342 */     Report.debug("searchcache", "addChangeToItemToMap key=" + key + " item=" + item, null);
/*     */   }
/*     */ 
/*     */   public static void removeChangeItemFromMap(String key, Map<String, SearchDocChangeItem> map)
/*     */   {
/* 355 */     SearchDocChangeItem item = (SearchDocChangeItem)map.get(key);
/* 356 */     if (item == null)
/*     */       return;
/* 358 */     if (Report.m_verbose)
/*     */     {
/* 360 */       Report.debug("searchcache", "removeChangeItemFromMap key=" + key + " item=" + item, null);
/*     */     }
/* 362 */     item.m_refCounterForMapLookup -= 1;
/* 363 */     if (item.m_refCounterForMapLookup > 0)
/*     */       return;
/* 365 */     map.remove(key);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 372 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95923 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchDocChangeUtils
 * JD-Core Version:    0.5.4
 */