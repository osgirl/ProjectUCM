/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ProxiedMonikerWatcher
/*     */ {
/*  33 */   protected static Vector m_inQueue = new IdcVector();
/*  34 */   protected static Hashtable m_inQueueMap = new Hashtable();
/*  35 */   protected static Hashtable m_curMonikerStates = new Hashtable();
/*     */ 
/*  37 */   protected static Vector m_subMonikerInQueue = new IdcVector();
/*  38 */   protected static Hashtable m_subMonikerMap = new Hashtable();
/*  39 */   protected static Hashtable m_subMonikerStates = new Hashtable();
/*     */ 
/*  41 */   protected static String m_lockObj = "ProxiedMonikerWatcherLock";
/*     */ 
/*     */   public static void requestWatch(Hashtable monikers, Hashtable subMonikers)
/*     */   {
/*  45 */     addToInQueue(monikers, m_inQueueMap, m_inQueue);
/*  46 */     addToInQueue(subMonikers, m_subMonikerMap, m_subMonikerInQueue);
/*     */   }
/*     */ 
/*     */   protected static void addToInQueue(Hashtable newMonikers, Hashtable inQueueMap, Vector inQueue)
/*     */   {
/*  51 */     if (newMonikers == null)
/*     */     {
/*  53 */       return;
/*     */     }
/*     */ 
/*  56 */     for (Enumeration en = newMonikers.keys(); en.hasMoreElements(); )
/*     */     {
/*  58 */       String location = (String)en.nextElement();
/*  59 */       MonikerList mList = (MonikerList)newMonikers.get(location);
/*     */ 
/*  61 */       curMonikers = (MonikerList)inQueueMap.get(location);
/*  62 */       if (curMonikers == null)
/*     */       {
/*  64 */         inQueueMap.put(location, mList);
/*  65 */         inQueue.addElement(mList);
/*     */       }
/*     */       else
/*     */       {
/*  69 */         Hashtable monikers = mList.m_monikerMap;
/*  70 */         for (enum1 = monikers.elements(); enum1.hasMoreElements(); )
/*     */         {
/*  72 */           MonikerInfo info = (MonikerInfo)enum1.nextElement();
/*  73 */           curMonikers.addAndUpdateList(info);
/*     */         }
/*     */       }
/*     */     }
/*     */     MonikerList curMonikers;
/*     */     Enumeration enum1;
/*     */   }
/*     */ 
/*     */   public static void updateWatched(String location, DataBinder binder) {
/*  81 */     synchronized (m_lockObj)
/*     */     {
/*  84 */       String refreshedStr = binder.getLocal("refreshMonikers");
/*  85 */       String updatedStr = binder.getLocal("updatedMonikers");
/*  86 */       String subMonikerStr = binder.getLocal("refreshSubMonikers");
/*     */ 
/*  88 */       Vector refreshedMonikers = StringUtils.parseArray(refreshedStr, ',', ',');
/*  89 */       Vector updatedMonikers = StringUtils.parseArray(updatedStr, ',', ',');
/*     */ 
/*  91 */       MonikerList mList = (MonikerList)m_curMonikerStates.get(location);
/*  92 */       if (mList == null)
/*     */       {
/*  94 */         mList = new MonikerList(location);
/*  95 */         m_curMonikerStates.put(location, mList);
/*     */       }
/*  97 */       mList.mergeInto(refreshedMonikers);
/*  98 */       mList.mergeInto(updatedMonikers);
/*     */ 
/* 101 */       Vector subMonikers = StringUtils.parseArray(subMonikerStr, ',', ',');
/* 102 */       mList = (MonikerList)m_subMonikerStates.get(location);
/* 103 */       if (mList == null)
/*     */       {
/* 105 */         mList = new MonikerList(location);
/* 106 */         m_subMonikerStates.put(location, mList);
/*     */       }
/* 108 */       mList.mergeInto(subMonikers);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean hasRequests(String location)
/*     */   {
/* 114 */     Object obj1 = m_inQueueMap.get(location);
/* 115 */     Object obj2 = m_subMonikerMap.get(location);
/*     */ 
/* 117 */     return (obj1 != null) || (obj2 != null);
/*     */   }
/*     */ 
/*     */   public static void buildRequests(String location, DataBinder binder)
/*     */   {
/* 122 */     String watchedStr = buildRequest(location, m_inQueueMap, m_inQueue);
/* 123 */     String subwatchedStr = buildRequest(location, m_subMonikerMap, m_subMonikerInQueue);
/*     */ 
/* 125 */     binder.putLocal("watchedMonikers", watchedStr);
/* 126 */     binder.putLocal("watchedSubMonikers", subwatchedStr);
/*     */   }
/*     */ 
/*     */   public static String buildRequest(String location, Hashtable inQueueMap, Vector inQueue)
/*     */   {
/* 131 */     MonikerList mList = (MonikerList)inQueueMap.get(location);
/* 132 */     if (mList == null)
/*     */     {
/* 134 */       return "";
/*     */     }
/*     */ 
/* 137 */     Vector monikers = new IdcVector();
/* 138 */     Hashtable infos = mList.m_monikerMap;
/* 139 */     for (Enumeration en = infos.elements(); en.hasMoreElements(); )
/*     */     {
/* 141 */       MonikerInfo info = (MonikerInfo)en.nextElement();
/* 142 */       monikers.addElement(info.m_moniker);
/* 143 */       monikers.addElement(String.valueOf(info.m_counter));
/*     */     }
/*     */ 
/* 147 */     int size = inQueue.size();
/* 148 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 150 */       MonikerList queueList = (MonikerList)inQueue.elementAt(i);
/* 151 */       String serverName = queueList.m_location;
/* 152 */       if (!location.equals(serverName))
/*     */         continue;
/* 154 */       inQueue.removeElementAt(i);
/* 155 */       inQueueMap.remove(location);
/* 156 */       break;
/*     */     }
/*     */ 
/* 161 */     return StringUtils.createString(monikers, ',', ',');
/*     */   }
/*     */ 
/*     */   public static boolean checkMoniker(MonikerList curStates, MonikerInfo monikerInfo)
/*     */   {
/* 166 */     boolean isChanged = false;
/* 167 */     String moniker = monikerInfo.m_moniker;
/*     */ 
/* 169 */     MonikerInfo info = curStates.getMonikerInfo(moniker);
/* 170 */     if (info != null)
/*     */     {
/* 172 */       isChanged = monikerInfo.m_counter != info.m_counter;
/* 173 */       monikerInfo.m_counter = info.m_counter;
/*     */     }
/*     */ 
/* 176 */     return isChanged;
/*     */   }
/*     */ 
/*     */   public static void notifyWatched(Hashtable refreshMonikers, Hashtable changedMonikers, Hashtable watchedMap, Hashtable updatedMap, boolean isAdded)
/*     */   {
/* 182 */     if (!isAdded)
/*     */     {
/* 184 */       synchronized (m_lockObj)
/*     */       {
/* 186 */         requestWatch(watchedMap, null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 191 */     for (Enumeration en = watchedMap.keys(); en.hasMoreElements(); )
/*     */     {
/* 193 */       String location = (String)en.nextElement();
/* 194 */       curStates = (MonikerList)m_curMonikerStates.get(location);
/* 195 */       if (curStates == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 200 */       MonikerList mList = (MonikerList)watchedMap.get(location);
/* 201 */       Hashtable monikerInfos = mList.m_monikerMap;
/* 202 */       for (enumList = monikerInfos.elements(); enumList.hasMoreElements(); )
/*     */       {
/* 204 */         MonikerInfo monikerInfo = (MonikerInfo)enumList.nextElement();
/* 205 */         boolean isChanged = checkMoniker(curStates, monikerInfo);
/* 206 */         if (isChanged)
/*     */         {
/* 208 */           String moniker = monikerInfo.m_moniker;
/* 209 */           refreshMonikers.put(moniker, new Long(monikerInfo.m_counter));
/* 210 */           changedMonikers.put(moniker, new Long(monikerInfo.m_counter));
/*     */         }
/*     */       }
/*     */     }
/*     */     MonikerList curStates;
/*     */     Enumeration enumList;
/* 217 */     for (Enumeration en = updatedMap.keys(); en.hasMoreElements(); )
/*     */     {
/* 219 */       String location = (String)en.nextElement();
/* 220 */       curStates = (MonikerList)m_curMonikerStates.get(location);
/* 221 */       if (curStates == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 226 */       MonikerList mList = (MonikerList)updatedMap.get(location);
/* 227 */       Hashtable monikerInfos = mList.m_monikerMap;
/* 228 */       for (enumList = monikerInfos.elements(); enumList.hasMoreElements(); )
/*     */       {
/* 230 */         MonikerInfo monikerInfo = (MonikerInfo)enumList.nextElement();
/* 231 */         String moniker = monikerInfo.m_moniker;
/* 232 */         boolean isChanged = checkMoniker(curStates, monikerInfo);
/* 233 */         if (isChanged)
/*     */         {
/* 235 */           refreshMonikers.put(moniker, new Long(monikerInfo.m_counter));
/*     */         }
/*     */         else
/*     */         {
/* 239 */           refreshMonikers.remove(moniker);
/*     */         }
/* 241 */         changedMonikers.put(moniker, new Long(monikerInfo.m_counter));
/*     */       }
/*     */     }
/*     */     MonikerList curStates;
/*     */     Enumeration enumList;
/*     */   }
/*     */ 
/*     */   protected static String watchSubMonikers(Hashtable subMonikers, boolean isAdded) {
/* 248 */     if (!isAdded)
/*     */     {
/* 250 */       synchronized (m_lockObj)
/*     */       {
/* 252 */         requestWatch(null, subMonikers);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 257 */     StringBuffer buffer = new StringBuffer();
/* 258 */     for (Enumeration en = subMonikers.keys(); en.hasMoreElements(); )
/*     */     {
/* 260 */       String location = (String)en.nextElement();
/* 261 */       curStates = (MonikerList)m_subMonikerStates.get(location);
/* 262 */       if (curStates == null) continue; if (curStates.m_monikerMap.isEmpty())
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 267 */       MonikerList mList = (MonikerList)subMonikers.get(location);
/* 268 */       Hashtable monikerInfos = mList.m_monikerMap;
/* 269 */       for (enumList = monikerInfos.elements(); enumList.hasMoreElements(); )
/*     */       {
/* 271 */         MonikerInfo monikerInfo = (MonikerInfo)enumList.nextElement();
/*     */ 
/* 273 */         checkMoniker(curStates, monikerInfo);
/*     */ 
/* 275 */         if (buffer.length() > 0)
/*     */         {
/* 277 */           buffer.append(',');
/*     */         }
/* 279 */         buffer.append(monikerInfo.m_moniker);
/* 280 */         buffer.append(',');
/* 281 */         buffer.append(String.valueOf(monikerInfo.m_counter));
/*     */       }
/*     */     }
/*     */     MonikerList curStates;
/*     */     Enumeration enumList;
/* 285 */     return buffer.toString();
/*     */   }
/*     */ 
/*     */   public static void startSynchronization(String location, DataBinder binder, ExecutionContext ctxt)
/*     */   {
/* 296 */     synchronized (m_lockObj)
/*     */     {
/* 298 */       binder.putLocal("IsMonitorLocalOnly", "1");
/* 299 */       if (ctxt != null)
/*     */       {
/* 301 */         ctxt.setCachedObject("MonikersAddedToQueue", "1");
/*     */ 
/* 303 */         Object[] monikerObjs = (Object[])(Object[])ctxt.getCachedObject("watchedMonikers");
/* 304 */         Object[] subMonikerObjs = (Object[])(Object[])ctxt.getCachedObject("watchedSubMonikers");
/*     */ 
/* 306 */         Hashtable monikers = null;
/* 307 */         if (monikerObjs != null)
/*     */         {
/* 309 */           monikers = (Hashtable)monikerObjs[3];
/*     */         }
/*     */ 
/* 312 */         Hashtable subMonikers = null;
/* 313 */         if (subMonikerObjs != null)
/*     */         {
/* 315 */           subMonikers = (Hashtable)subMonikerObjs[3];
/*     */         }
/* 317 */         requestWatch(monikers, subMonikers);
/*     */       }
/*     */ 
/* 320 */       buildRequests(location, binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void finishSynchronization(String location, DataBinder binder, ExecutionContext ctxt)
/*     */   {
/* 327 */     binder.removeLocal("IsMonitorLocalOnly");
/* 328 */     updateWatched(location, binder);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 333 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ProxiedMonikerWatcher
 * JD-Core Version:    0.5.4
 */