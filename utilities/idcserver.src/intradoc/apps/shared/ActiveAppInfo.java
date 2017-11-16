/*     */ package intradoc.apps.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ActiveAppInfo
/*     */ {
/*  39 */   protected Hashtable m_allObjects = new Hashtable();
/*  40 */   protected Hashtable m_monitoredObjects = new Hashtable();
/*  41 */   protected HashMap m_activeEvent = new HashMap();
/*  42 */   protected String[] m_defaultObjects = null;
/*     */ 
/*  44 */   protected boolean m_isMoniker = false;
/*     */ 
/*  46 */   protected String m_idStr = null;
/*  47 */   protected String m_monitorString = null;
/*  48 */   protected String m_changedString = null;
/*  49 */   protected String m_refreshString = null;
/*  50 */   protected String m_notifyChangedString = null;
/*     */ 
/*  52 */   protected ExecutionContext m_cxt = null;
/*     */ 
/*     */   public ActiveAppInfo(String idStr, String[] defaults, boolean isMoniker, ExecutionContext cxt)
/*     */   {
/*  58 */     String str1 = idStr.substring(0, 1);
/*  59 */     String str2 = idStr.substring(1);
/*  60 */     String plId = str1.toUpperCase() + str2 + "s";
/*  61 */     if (isMoniker)
/*     */     {
/*  63 */       this.m_monitorString = ("watched" + plId);
/*  64 */       this.m_allObjects = this.m_monitoredObjects;
/*     */     }
/*     */     else
/*     */     {
/*  68 */       this.m_monitorString = ("monitored" + plId);
/*     */     }
/*     */ 
/*  71 */     this.m_changedString = ("changed" + plId);
/*  72 */     this.m_refreshString = ("refresh" + plId);
/*     */ 
/*  74 */     this.m_idStr = idStr;
/*  75 */     this.m_notifyChangedString = (this.m_idStr + "NotifyChanged");
/*     */ 
/*  77 */     this.m_defaultObjects = defaults;
/*  78 */     this.m_isMoniker = isMoniker;
/*  79 */     this.m_cxt = cxt;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  85 */     int size = this.m_defaultObjects.length;
/*  86 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  88 */       String name = this.m_defaultObjects[i];
/*  89 */       SubjectInfo info = (SubjectInfo)this.m_monitoredObjects.get(name);
/*  90 */       if (info != null)
/*     */         continue;
/*  92 */       info = new SubjectInfo(name);
/*  93 */       this.m_monitoredObjects.put(name, info);
/*  94 */       this.m_allObjects.put(name, info);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addMonitorObjects(String[] objects)
/*     */   {
/* 104 */     int size = objects.length;
/* 105 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 107 */       String name = objects[i];
/* 108 */       SubjectInfo info = (SubjectInfo)this.m_allObjects.get(name);
/* 109 */       if (info == null)
/*     */       {
/* 112 */         Report.trace(null, LocaleResources.getString("apUnableToFindSubjectInList", this.m_cxt, this.m_idStr, name), null);
/*     */       }
/*     */       else {
/* 115 */         info.m_refCount += 1;
/* 116 */         this.m_monitoredObjects.put(name, info);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeMonitoredObjects(String[] objects) {
/* 122 */     int size = objects.length;
/* 123 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 125 */       SubjectInfo info = (SubjectInfo)this.m_monitoredObjects.get(objects[i]);
/* 126 */       if (info == null)
/*     */         continue;
/* 128 */       info.m_refCount -= 1;
/* 129 */       if (info.m_refCount != 0)
/*     */         continue;
/* 131 */       this.m_monitoredObjects.remove(objects[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void prepForMonitoredObjects(DataBinder binder)
/*     */   {
/* 139 */     StringBuffer buff = new StringBuffer();
/* 140 */     for (Enumeration en = this.m_monitoredObjects.elements(); en.hasMoreElements(); )
/*     */     {
/* 142 */       SubjectInfo info = (SubjectInfo)en.nextElement();
/* 143 */       if (buff.length() != 0)
/*     */       {
/* 145 */         buff.append(",");
/*     */       }
/* 147 */       buff.append(info.m_name);
/* 148 */       buff.append(",");
/* 149 */       buff.append(Long.toString(info.m_counter));
/*     */     }
/*     */ 
/* 152 */     binder.putLocal(this.m_monitorString, buff.toString());
/*     */ 
/* 155 */     binder.removeLocal(this.m_changedString);
/* 156 */     binder.removeLocal(this.m_refreshString);
/*     */   }
/*     */ 
/*     */   public void addObjectObserver(String name, Observer obs)
/*     */   {
/* 164 */     Observable obsable = (Observable)this.m_allObjects.get(name);
/* 165 */     if (obsable == null)
/*     */     {
/* 167 */       Report.trace(null, LocaleResources.getString("apNoObjectForObserver", this.m_cxt, this.m_idStr, name), null);
/* 168 */       return;
/*     */     }
/*     */ 
/* 171 */     obsable.addObserver(obs);
/*     */   }
/*     */ 
/*     */   public void removeObjectObserver(String name, Observer obs)
/*     */   {
/* 176 */     Observable obsable = (Observable)this.m_allObjects.get(name);
/* 177 */     if (obsable == null)
/*     */     {
/* 179 */       Report.trace(null, LocaleResources.getString("apNoObjectToRemoteObserver", this.m_cxt, this.m_idStr, name), null);
/* 180 */       return;
/*     */     }
/* 182 */     obsable.deleteObserver(obs);
/*     */   }
/*     */ 
/*     */   public void notifyObjectObservers(String name, DataBinder binder)
/*     */   {
/* 187 */     Observable obs = (Observable)this.m_allObjects.get(name);
/* 188 */     if (obs == null)
/*     */       return;
/* 190 */     if (binder != null)
/*     */     {
/* 192 */       binder.putLocal(this.m_notifyChangedString, name);
/*     */     }
/* 194 */     obs.notifyObservers(binder);
/*     */   }
/*     */ 
/*     */   public boolean hasActiveEvent()
/*     */   {
/* 200 */     boolean hasActiveEvent = false;
/* 201 */     for (Iterator i$ = this.m_activeEvent.keySet().iterator(); i$.hasNext(); ) { Object key = i$.next();
/*     */ 
/* 203 */       Long time = (Long)this.m_activeEvent.get(key);
/* 204 */       if (time != null)
/*     */       {
/* 206 */         if (System.currentTimeMillis() > time.longValue() + 300000L)
/*     */         {
/* 208 */           removeActiveEvent((String)key);
/*     */         }
/*     */         else
/*     */         {
/* 212 */           hasActiveEvent = true;
/* 213 */           break;
/*     */         }
/*     */       } }
/*     */ 
/* 217 */     return hasActiveEvent;
/*     */   }
/*     */ 
/*     */   public void addOrRefreshActiveEvent(String name)
/*     */   {
/* 222 */     long time = System.currentTimeMillis();
/* 223 */     this.m_activeEvent.put(name, Long.valueOf(time));
/*     */   }
/*     */ 
/*     */   public void removeActiveEvent(String name)
/*     */   {
/* 228 */     this.m_activeEvent.remove(name);
/*     */   }
/*     */ 
/*     */   public boolean isActiveEvent(String name)
/*     */   {
/* 233 */     return this.m_activeEvent.get(name) != null;
/*     */   }
/*     */ 
/*     */   public void setObjectChanged(String name)
/*     */   {
/* 238 */     SubjectInfo obs = (SubjectInfo)this.m_allObjects.get(name);
/* 239 */     if (obs == null)
/*     */       return;
/* 241 */     obs.setInfoChanged();
/*     */   }
/*     */ 
/*     */   public void updateObjectCounters(String str, DataBinder binder, boolean isNotify)
/*     */   {
/* 247 */     if ((str == null) || (str.length() == 0))
/*     */     {
/* 249 */       return;
/*     */     }
/*     */ 
/* 252 */     Vector changedObjects = StringUtils.parseArray(str, ',', '^');
/* 253 */     int size = changedObjects.size();
/* 254 */     for (int i = 0; i < size; i += 2)
/*     */     {
/* 256 */       String name = (String)changedObjects.elementAt(i);
/* 257 */       long counter = Long.parseLong((String)changedObjects.elementAt(i + 1));
/* 258 */       boolean isActiveEvent = isActiveEvent(name);
/*     */ 
/* 260 */       SubjectInfo info = (SubjectInfo)this.m_allObjects.get(name);
/* 261 */       if (info == null)
/*     */       {
/* 263 */         if (!this.m_isMoniker)
/*     */         {
/* 265 */           Report.trace(null, LocaleResources.getString("apCannotFindChangedObject", this.m_cxt, this.m_idStr, name), null);
/*     */         }
/*     */ 
/* 268 */         if (!isActiveEvent)
/*     */           continue;
/* 270 */         removeActiveEvent(name);
/*     */       }
/*     */       else
/*     */       {
/* 274 */         if (info.m_counter == counter)
/*     */           continue;
/* 276 */         info.m_counter = counter;
/*     */ 
/* 278 */         if (!isNotify)
/*     */           continue;
/* 280 */         updateLocalData(name, binder);
/* 281 */         setObjectChanged(name);
/* 282 */         notifyObjectObservers(name, binder);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateLocalData(String name, DataBinder binder)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void synchronizeCounters(DataBinder binder)
/*     */   {
/* 296 */     String str = binder.getLocal(this.m_changedString);
/* 297 */     updateObjectCounters(str, binder, false);
/*     */ 
/* 299 */     str = binder.getLocal(this.m_refreshString);
/* 300 */     updateObjectCounters(str, binder, true);
/*     */   }
/*     */ 
/*     */   public void addMonikerObserver(String moniker, Observer obs)
/*     */   {
/* 308 */     SubjectInfo info = (SubjectInfo)this.m_monitoredObjects.get(moniker);
/* 309 */     if (info == null)
/*     */     {
/* 311 */       info = new SubjectInfo(moniker);
/* 312 */       this.m_monitoredObjects.put(moniker, info);
/*     */     }
/*     */ 
/* 315 */     info.m_refCount += 1;
/* 316 */     info.addObserver(obs);
/*     */   }
/*     */ 
/*     */   public void removeMonikerObserver(String moniker, Observer obs)
/*     */   {
/* 321 */     SubjectInfo info = (SubjectInfo)this.m_monitoredObjects.get(moniker);
/* 322 */     if (info == null)
/*     */     {
/* 325 */       Report.trace(null, LocaleResources.getString("apUnableToRemoveObserver", this.m_cxt, this.m_idStr, moniker), null);
/* 326 */       return;
/*     */     }
/*     */ 
/* 329 */     info.deleteObserver(obs);
/*     */ 
/* 332 */     info.m_refCount -= 1;
/* 333 */     if (info.m_refCount != 0)
/*     */       return;
/* 335 */     this.m_monitoredObjects.remove(moniker);
/*     */   }
/*     */ 
/*     */   public Hashtable getAllObjects()
/*     */   {
/* 344 */     return this.m_allObjects;
/*     */   }
/*     */ 
/*     */   public Hashtable getMonitoredObjects()
/*     */   {
/* 349 */     return this.m_monitoredObjects;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 354 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.ActiveAppInfo
 * JD-Core Version:    0.5.4
 */