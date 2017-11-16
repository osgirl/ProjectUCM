/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ 
/*     */ public class LRUManager
/*     */ {
/*     */   protected LRUManagerItem m_oldestItem;
/*     */   protected LRUManagerItem m_newestItem;
/*     */   protected Hashtable m_items;
/*     */   protected int m_cacheUsage;
/*     */   protected int m_itemCount;
/*     */   protected int m_maximumUsage;
/*     */   protected long m_maximumAge;
/*     */   protected String m_traceSection;
/*     */ 
/*     */   public LRUManager()
/*     */   {
/*  30 */     this.m_oldestItem = null;
/*  31 */     this.m_newestItem = null;
/*  32 */     this.m_items = new Hashtable();
/*     */ 
/*  36 */     this.m_maximumUsage = 10485760;
/*     */ 
/*  39 */     this.m_traceSection = null;
/*     */   }
/*     */ 
/*     */   public void setUsage(int bytes) {
/*  43 */     this.m_maximumUsage = bytes;
/*  44 */     cleanIfNeeded();
/*     */   }
/*     */ 
/*     */   public int getCurrentUsage()
/*     */   {
/*  49 */     return this.m_cacheUsage;
/*     */   }
/*     */ 
/*     */   public int getItemCount()
/*     */   {
/*  54 */     return this.m_itemCount;
/*     */   }
/*     */ 
/*     */   public int getMaximumUsage()
/*     */   {
/*  59 */     return this.m_maximumUsage;
/*     */   }
/*     */ 
/*     */   public void setMaximumAge(long age)
/*     */   {
/*  64 */     this.m_maximumAge = age;
/*  65 */     cleanIfNeeded();
/*     */   }
/*     */ 
/*     */   public long getMaximumAge()
/*     */   {
/*  70 */     return this.m_maximumAge;
/*     */   }
/*     */ 
/*     */   public void setTracingSection(String section)
/*     */   {
/*  75 */     this.m_traceSection = section;
/*     */   }
/*     */ 
/*     */   public synchronized void touchObject(LRUManagerContainer container, Object data, boolean isUpdate)
/*     */   {
/*  81 */     boolean isNew = false;
/*  82 */     if (SystemUtils.m_verbose)
/*     */     {
/*  84 */       Report.debug(this.m_traceSection, "touching object " + data + " in container " + container, null);
/*     */     }
/*     */ 
/*  87 */     LRUManagerItem item = (LRUManagerItem)this.m_items.get(data);
/*  88 */     if (item == null)
/*     */     {
/*  90 */       if (container == null)
/*     */       {
/*  93 */         throw new AssertionError("!$LRUManager items missing item " + data);
/*     */       }
/*  95 */       isNew = true;
/*  96 */       if (SystemUtils.m_verbose)
/*     */       {
/*  98 */         Report.debug(this.m_traceSection, "creating new object " + data + " for container " + container, null);
/*     */       }
/*     */ 
/* 101 */       item = new LRUManagerItem(data, this, container);
/* 102 */       this.m_items.put(data, item);
/* 103 */       this.m_itemCount += 1;
/* 104 */       this.m_cacheUsage += item.m_size;
/*     */     }
/*     */     else
/*     */     {
/* 108 */       long now = System.currentTimeMillis();
/* 109 */       if (isUpdate)
/*     */       {
/* 111 */         item.m_updateTime = now;
/*     */       }
/* 113 */       item.m_accessTime = now;
/* 114 */       isUpdate = true;
/*     */     }
/* 116 */     update(item, !isNew);
/* 117 */     cleanIfNeeded();
/*     */   }
/*     */ 
/*     */   protected synchronized void update(LRUManagerItem item, boolean isUpdate)
/*     */   {
/* 123 */     if (isUpdate)
/*     */     {
/* 127 */       int size = item.m_container.getSize(item.m_data);
/* 128 */       if (size != item.m_size)
/*     */       {
/* 131 */         int delta = size - item.m_size;
/* 132 */         item.m_size = size;
/* 133 */         this.m_cacheUsage += delta;
/*     */       }
/*     */     }
/* 136 */     if (this.m_oldestItem == null)
/*     */     {
/* 138 */       this.m_newestItem = (this.m_oldestItem = item);
/* 139 */       return;
/*     */     }
/* 141 */     if (this.m_newestItem == item)
/*     */     {
/* 143 */       return;
/*     */     }
/* 145 */     if (isUpdate)
/*     */     {
/* 147 */       removeItemFromList(item);
/*     */     }
/*     */ 
/* 150 */     item.m_newer = null;
/* 151 */     item.m_older = this.m_newestItem;
/* 152 */     this.m_newestItem.m_newer = item;
/* 153 */     this.m_newestItem = item;
/*     */   }
/*     */ 
/*     */   public synchronized void cleanIfNeeded()
/*     */   {
/* 161 */     int itemCount = 0;
/* 162 */     while ((this.m_itemCount > 1) && (this.m_cacheUsage > this.m_maximumUsage))
/*     */     {
/* 164 */       removeOldestItem();
/* 165 */       ++itemCount;
/*     */     }
/*     */ 
/* 168 */     if ((this.m_itemCount > 1) && (this.m_maximumAge > 0L))
/*     */     {
/* 170 */       long ageLimit = System.currentTimeMillis() - this.m_maximumAge;
/* 171 */       while ((this.m_itemCount > 1) && (this.m_oldestItem.m_accessTime < ageLimit))
/*     */       {
/* 173 */         removeOldestItem();
/* 174 */         ++itemCount;
/*     */       }
/*     */     }
/*     */ 
/* 178 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 180 */     Report.debug(this.m_traceSection, "cleanIfNeeded() cleaned " + itemCount + " items.  Size is now " + this.m_cacheUsage + " bytes in " + this.m_itemCount + " items", null);
/*     */   }
/*     */ 
/*     */   public synchronized void removeOldestItem()
/*     */   {
/* 188 */     if (this.m_oldestItem == null)
/*     */     {
/* 190 */       return;
/*     */     }
/* 192 */     removeItem(this.m_oldestItem.m_data);
/*     */   }
/*     */ 
/*     */   public synchronized void removeItem(Object data)
/*     */   {
/* 197 */     LRUManagerItem cacheObject = (LRUManagerItem)this.m_items.get(data);
/* 198 */     if (cacheObject == null)
/*     */       return;
/* 200 */     cacheObject.m_container.discard(cacheObject.m_data);
/* 201 */     this.m_cacheUsage -= cacheObject.m_size;
/* 202 */     this.m_itemCount -= 1;
/* 203 */     this.m_items.remove(data);
/* 204 */     removeItemFromList(cacheObject);
/* 205 */     cacheObject.m_data = null;
/*     */   }
/*     */ 
/*     */   protected synchronized void removeItemFromList(LRUManagerItem cacheObject)
/*     */   {
/* 211 */     if (cacheObject.m_newer != null)
/*     */     {
/* 213 */       cacheObject.m_newer.m_older = cacheObject.m_older;
/*     */     }
/*     */     else
/*     */     {
/* 217 */       this.m_newestItem = cacheObject.m_older;
/*     */     }
/* 219 */     if (cacheObject.m_older != null)
/*     */     {
/* 221 */       cacheObject.m_older.m_newer = cacheObject.m_newer;
/*     */     }
/*     */     else
/*     */     {
/* 225 */       this.m_oldestItem = cacheObject.m_newer;
/*     */     }
/* 227 */     cacheObject.m_newer = (cacheObject.m_older = null);
/*     */   }
/*     */ 
/*     */   public void getCacheProperties(String prefix, DataBinder binder)
/*     */   {
/* 232 */     binder.putLocal(prefix + "ItemCount", "" + this.m_itemCount);
/* 233 */     binder.putLocal(prefix + "MemoryUsage", "" + this.m_cacheUsage);
/* 234 */     binder.putLocal(prefix + "MaximumMemoryUsage", "" + this.m_maximumUsage);
/* 235 */     binder.putLocal(prefix + "MaximumAge", "" + this.m_maximumAge);
/*     */ 
/* 237 */     if (this.m_oldestItem != null)
/*     */     {
/* 239 */       String date = binder.m_blDateFormat.format(new Date(this.m_oldestItem.m_accessTime));
/*     */ 
/* 241 */       binder.putLocal(prefix + "LRUDate", date);
/*     */     }
/* 243 */     if (this.m_newestItem == null)
/*     */       return;
/* 245 */     String date = binder.m_blDateFormat.format(new Date(this.m_newestItem.m_accessTime));
/*     */ 
/* 247 */     binder.putLocal(prefix + "MRUDate", date);
/*     */   }
/*     */ 
/*     */   public synchronized void checkCache(String prefix, DataBinder binder)
/*     */   {
/* 253 */     int count = 0;
/* 254 */     int size = 0;
/* 255 */     LRUManagerItem item = this.m_newestItem;
/* 256 */     while (item != null)
/*     */     {
/* 258 */       size += item.m_container.getSize(item.m_data);
/* 259 */       ++count;
/* 260 */       item = item.m_older;
/*     */     }
/*     */ 
/* 263 */     StringBuffer msg = new StringBuffer();
/* 264 */     if (count != this.m_itemCount)
/*     */     {
/* 266 */       msg.append("Counted " + count + " items");
/*     */     }
/* 268 */     if (size != this.m_cacheUsage)
/*     */     {
/* 270 */       msg.append("Found " + size + " bytes of data.");
/*     */     }
/* 272 */     String fullMessage = msg.toString();
/* 273 */     if (fullMessage.length() <= 0)
/*     */       return;
/* 275 */     binder.putLocal(prefix + "Health", fullMessage);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 281 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.LRUManager
 * JD-Core Version:    0.5.4
 */