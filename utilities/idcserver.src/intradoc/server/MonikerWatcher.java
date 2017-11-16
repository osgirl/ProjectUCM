/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.server.archive.ArchiveUtils;
/*     */ import intradoc.shared.MonikerInterface;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class MonikerWatcher
/*     */ {
/*     */   static final String SEPARATOR = "://";
/*  37 */   static final String[][] MONIKER_TYPES = { { "archives", "ArchiveCollections" } };
/*     */ 
/*  42 */   protected static Hashtable m_monikerTypes = new Hashtable();
/*     */ 
/*     */   public static void init()
/*     */   {
/*  46 */     for (int i = 0; i < MONIKER_TYPES.length; ++i)
/*     */     {
/*  48 */       m_monikerTypes.put(MONIKER_TYPES[i][0], MONIKER_TYPES[i][1]);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static String getMonikerFile(String moniker)
/*     */   {
/*  54 */     MonikerInfo monikerInfo = parseMonikerInfo(moniker);
/*  55 */     return getFilename(monikerInfo);
/*     */   }
/*     */ 
/*     */   protected static String getFilename(MonikerInfo monikerInfo)
/*     */   {
/*  62 */     if (monikerInfo == null)
/*     */     {
/*  64 */       return null;
/*     */     }
/*     */ 
/*  67 */     MonikerInterface md = monikerInfo.m_monikerData;
/*  68 */     String location = md.getMonikerLocation();
/*  69 */     if (location == null)
/*     */     {
/*  71 */       String msg = LocaleUtils.encodeMessage("csMonikerMissing", null, monikerInfo.m_moniker);
/*     */ 
/*  73 */       Report.error(null, msg, null);
/*  74 */       return null;
/*     */     }
/*     */ 
/*  77 */     return location + monikerInfo.m_filename.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static void updateMoniker(String moniker, DataBinder binder)
/*     */   {
/*  82 */     StringBuffer buffer = null;
/*  83 */     String updateStr = binder.getLocal("updatedMonikers");
/*  84 */     if (updateStr != null)
/*     */     {
/*  86 */       buffer = new StringBuffer(updateStr);
/*  87 */       buffer.append(",");
/*     */     }
/*     */     else
/*     */     {
/*  91 */       buffer = new StringBuffer();
/*     */     }
/*     */ 
/*  94 */     MonikerInfo monikerInfo = parseMonikerInfo(moniker);
/*  95 */     long ts = checkMoniker(monikerInfo, null);
/*     */ 
/*  97 */     buffer.append(StringUtils.addEscapeChars(moniker, ',', '^'));
/*  98 */     buffer.append(",");
/*  99 */     buffer.append(ts);
/*     */ 
/* 101 */     binder.putLocal("updatedMonikers", buffer.toString());
/*     */   }
/*     */ 
/*     */   public static long checkMoniker(MonikerInfo monikerInfo, Hashtable curMonikers)
/*     */   {
/* 106 */     if (curMonikers != null)
/*     */     {
/* 108 */       Long ts = (Long)curMonikers.get(monikerInfo.m_moniker);
/* 109 */       if (ts != null)
/*     */       {
/* 111 */         return ts.longValue();
/*     */       }
/*     */     }
/*     */ 
/* 115 */     String filename = getFilename(monikerInfo);
/* 116 */     if (filename == null)
/*     */     {
/* 118 */       return -2L;
/*     */     }
/*     */ 
/* 122 */     File file = new File(filename);
/* 123 */     long lastModified = file.lastModified();
/*     */ 
/* 125 */     if (curMonikers != null)
/*     */     {
/* 127 */       curMonikers.put(monikerInfo.m_moniker, new Long(lastModified));
/*     */     }
/* 129 */     return lastModified;
/*     */   }
/*     */ 
/*     */   public static void notifyChanged(String moniker)
/*     */   {
/* 134 */     synchronized (m_monikerTypes)
/*     */     {
/* 136 */       String filename = getMonikerFile(moniker);
/* 137 */       if (filename == null)
/*     */       {
/* 139 */         return;
/*     */       }
/* 141 */       FileUtils.touchFile(filename);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void notifyWatched(DataBinder binder, ExecutionContext context)
/*     */   {
/* 147 */     Object[] watchedObjs = (Object[])(Object[])context.getCachedObject("watchedMonikers");
/* 148 */     Object[] subMonikerObjs = (Object[])(Object[])context.getCachedObject("watchedSubMonikers");
/*     */ 
/* 150 */     if ((watchedObjs == null) && (subMonikerObjs == null))
/*     */     {
/* 152 */       return;
/*     */     }
/*     */ 
/* 155 */     Object[] updateObjs = computeMonikers("updatedMonikers", binder);
/*     */ 
/* 157 */     Hashtable refreshMonikers = new Hashtable();
/* 158 */     Hashtable changedMonikers = new Hashtable();
/* 159 */     Hashtable curMonikers = new Hashtable();
/*     */ 
/* 162 */     Vector watchedMonikers = (Vector)watchedObjs[2];
/* 163 */     int size = watchedMonikers.size();
/* 164 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 166 */       MonikerInfo monikerInfo = (MonikerInfo)watchedMonikers.elementAt(i);
/* 167 */       long counter = checkMoniker(monikerInfo, curMonikers);
/* 168 */       if (counter == monikerInfo.m_counter)
/*     */         continue;
/* 170 */       String moniker = monikerInfo.m_moniker;
/* 171 */       refreshMonikers.put(moniker, new Long(counter));
/* 172 */       changedMonikers.put(moniker, new Long(counter));
/*     */     }
/*     */ 
/* 178 */     Vector updatedMonikers = (Vector)updateObjs[2];
/* 179 */     size = updatedMonikers.size();
/* 180 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 182 */       MonikerInfo monikerInfo = (MonikerInfo)updatedMonikers.elementAt(i);
/* 183 */       String moniker = monikerInfo.m_moniker;
/* 184 */       long counter = checkMoniker(monikerInfo, curMonikers);
/* 185 */       if (counter != monikerInfo.m_counter)
/*     */       {
/* 187 */         refreshMonikers.put(moniker, new Long(counter));
/*     */       }
/*     */       else
/*     */       {
/* 191 */         refreshMonikers.remove(moniker);
/*     */       }
/* 193 */       changedMonikers.put(moniker, new Long(counter));
/*     */     }
/*     */ 
/* 196 */     boolean isAdded = false;
/* 197 */     boolean isLocalOnly = StringUtils.convertToBool(binder.getLocal("IsMonitorLocalOnly"), false);
/* 198 */     if (!isLocalOnly)
/*     */     {
/* 200 */       isAdded = StringUtils.convertToBool((String)context.getCachedObject("MonikersAddedToQueue"), false);
/* 201 */       ProxiedMonikerWatcher.notifyWatched(refreshMonikers, changedMonikers, (Hashtable)watchedObjs[3], (Hashtable)updateObjs[3], isAdded);
/*     */     }
/*     */ 
/* 206 */     StringBuffer changedBuff = new StringBuffer();
/* 207 */     StringBuffer refreshBuff = new StringBuffer();
/* 208 */     for (Enumeration en = changedMonikers.keys(); en.hasMoreElements(); )
/*     */     {
/* 210 */       String name = (String)en.nextElement();
/* 211 */       Long counter = (Long)changedMonikers.get(name);
/* 212 */       StringBuffer buff = changedBuff;
/* 213 */       if (refreshMonikers.get(name) != null)
/*     */       {
/* 215 */         buff = refreshBuff;
/*     */       }
/*     */ 
/* 218 */       if (buff.length() != 0)
/*     */       {
/* 220 */         buff.append(",");
/*     */       }
/* 222 */       buff.append(StringUtils.addEscapeChars(name, ',', '^'));
/* 223 */       buff.append(",");
/* 224 */       buff.append(counter.toString());
/*     */     }
/*     */ 
/* 227 */     binder.putLocal("changedMonikers", changedBuff.toString());
/* 228 */     binder.putLocal("refreshMonikers", refreshBuff.toString());
/*     */ 
/* 230 */     watchSubMonikers(binder, subMonikerObjs, isLocalOnly, isAdded);
/*     */   }
/*     */ 
/*     */   protected static Object[] computeMonikers(String key, DataBinder binder)
/*     */   {
/* 235 */     String str = binder.getLocal(key);
/* 236 */     Vector monikers = StringUtils.parseArray(str, ',', '^');
/*     */ 
/* 238 */     Vector localMonikers = new IdcVector();
/* 239 */     Hashtable proxiedMonikers = new Hashtable();
/* 240 */     int num = monikers.size();
/* 241 */     for (int i = 0; i < num; i += 2)
/*     */     {
/* 243 */       String moniker = (String)monikers.elementAt(i);
/* 244 */       long counter = NumberUtils.parseLong((String)monikers.elementAt(i + 1), -2L);
/*     */ 
/* 246 */       MonikerInfo monikerInfo = parseMonikerInfo(moniker);
/* 247 */       if (monikerInfo == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 251 */       monikerInfo.m_counter = counter;
/*     */ 
/* 253 */       MonikerInterface data = monikerInfo.m_monikerData;
/* 254 */       if (data.isProxied())
/*     */       {
/* 256 */         String serverName = data.getProxiedServer();
/* 257 */         MonikerList mList = (MonikerList)proxiedMonikers.get(serverName);
/* 258 */         if (mList == null)
/*     */         {
/* 260 */           mList = new MonikerList(serverName);
/* 261 */           proxiedMonikers.put(serverName, mList);
/*     */         }
/* 263 */         mList.addAndUpdateList(monikerInfo);
/*     */       }
/*     */       else
/*     */       {
/* 267 */         localMonikers.addElement(monikerInfo);
/*     */       }
/*     */     }
/*     */ 
/* 271 */     Object[] obj = new Object[4];
/* 272 */     obj[0] = str;
/* 273 */     obj[1] = monikers;
/* 274 */     obj[2] = localMonikers;
/* 275 */     obj[3] = proxiedMonikers;
/*     */ 
/* 277 */     return obj;
/*     */   }
/*     */ 
/*     */   protected static MonikerInfo parseMonikerInfo(String moniker)
/*     */   {
/* 284 */     int index = moniker.indexOf("://");
/* 285 */     if (index < 0)
/*     */     {
/* 288 */       String msg = LocaleUtils.encodeMessage("csMonikerWrongFormat", null, moniker);
/*     */ 
/* 290 */       Report.error(null, msg, null);
/* 291 */       return null;
/*     */     }
/* 293 */     String type = moniker.substring(0, index);
/* 294 */     String locationID = moniker.substring(index + "://".length());
/*     */ 
/* 296 */     index = locationID.indexOf(47);
/* 297 */     if (index < 0)
/*     */     {
/* 299 */       String msg = LocaleUtils.encodeMessage("csMonikerInvalidLocation", null, locationID);
/*     */ 
/* 301 */       Report.error(null, msg, null);
/* 302 */       return null;
/*     */     }
/*     */ 
/* 305 */     String dirID = locationID.substring(0, index);
/* 306 */     String fileName = locationID.substring(index + 1);
/*     */ 
/* 308 */     String typeClass = (String)m_monikerTypes.get(type);
/* 309 */     if (typeClass == null)
/*     */     {
/* 312 */       String msg = LocaleUtils.encodeMessage("csMonikerTypeClassUnknown", null, typeClass);
/*     */ 
/* 314 */       Report.error(null, msg, null);
/* 315 */       return null;
/*     */     }
/*     */ 
/* 318 */     MonikerInterface md = null;
/* 319 */     if (typeClass.equals("ArchiveCollections"))
/*     */     {
/*     */       try
/*     */       {
/* 323 */         md = ArchiveUtils.getCollection(dirID);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 327 */         String msg = LocaleUtils.encodeMessage("csMonikerUnableToFindArchiveInfo", null, typeClass);
/*     */ 
/* 329 */         Report.error(null, msg, e);
/* 330 */         return null;
/*     */       }
/*     */     }
/*     */ 
/* 334 */     MonikerInfo monikerInfo = null;
/* 335 */     if (md != null)
/*     */     {
/* 337 */       monikerInfo = new MonikerInfo();
/* 338 */       monikerInfo.m_monikerData = md;
/* 339 */       monikerInfo.m_moniker = moniker;
/* 340 */       monikerInfo.m_filename = fileName;
/*     */     }
/*     */ 
/* 343 */     return monikerInfo;
/*     */   }
/*     */ 
/*     */   protected static void watchSubMonikers(DataBinder binder, Object[] subMonikerObjs, boolean isLocalOnly, boolean isAdded)
/*     */   {
/* 349 */     Vector subMonikers = (Vector)subMonikerObjs[2];
/* 350 */     int size = subMonikers.size();
/* 351 */     StringBuffer buffer = new StringBuffer();
/* 352 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 354 */       if (buffer.length() > 0)
/*     */       {
/* 356 */         buffer.append(',');
/*     */       }
/*     */ 
/* 359 */       MonikerInfo monikerInfo = (MonikerInfo)subMonikers.elementAt(i);
/*     */ 
/* 361 */       long counter = checkMoniker(monikerInfo, null);
/* 362 */       buffer.append(StringUtils.addEscapeChars(monikerInfo.m_moniker, ',', '^'));
/* 363 */       buffer.append(',');
/* 364 */       buffer.append(String.valueOf(counter));
/*     */     }
/*     */ 
/* 367 */     if (!isLocalOnly)
/*     */     {
/* 369 */       String str = ProxiedMonikerWatcher.watchSubMonikers((Hashtable)subMonikerObjs[3], isAdded);
/* 370 */       if ((str != null) && (str.length() > 0))
/*     */       {
/* 372 */         if (buffer.length() > 0)
/*     */         {
/* 374 */           buffer.append(',');
/*     */         }
/* 376 */         buffer.append(str);
/*     */       }
/*     */     }
/*     */ 
/* 380 */     binder.putLocal("refreshSubMonikers", buffer.toString());
/*     */   }
/*     */ 
/*     */   public static void cacheWatched(DataBinder binder, ExecutionContext context)
/*     */   {
/* 388 */     Object[] watchedObjs = computeMonikers("watchedMonikers", binder);
/* 389 */     Object[] subMonikerObjs = computeMonikers("watchedSubMonikers", binder);
/*     */ 
/* 391 */     context.setCachedObject("watchedMonikers", watchedObjs);
/* 392 */     context.setCachedObject("watchedSubMonikers", subMonikerObjs);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 397 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.MonikerWatcher
 * JD-Core Version:    0.5.4
 */