/*     */ package intradoc.server.cache;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataStreamValue;
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.ParameterPacket;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.InputStream;
/*     */ import java.io.ObjectInputStream;
/*     */ import java.io.ObjectOutputStream;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class IdcCacheStore
/*     */ {
/*     */   protected String m_cacheRegionName;
/*     */   protected long m_autoExpiryTime;
/*     */   protected Workspace m_workspace;
/*     */ 
/*     */   public IdcCacheStore(String regionName)
/*     */   {
/*  61 */     this.m_cacheRegionName = regionName;
/*  62 */     this.m_workspace = WorkspaceUtils.getWorkspace("system");
/*  63 */     this.m_autoExpiryTime = -1L;
/*     */   }
/*     */ 
/*     */   public IdcCacheStore(String regionName, long autoExpiryTime)
/*     */   {
/*  73 */     this(regionName);
/*  74 */     this.m_autoExpiryTime = autoExpiryTime;
/*     */   }
/*     */ 
/*     */   public Workspace getWorkspace()
/*     */   {
/*  83 */     return this.m_workspace;
/*     */   }
/*     */ 
/*     */   public Object load(Object oKey)
/*     */   {
/*  92 */     Object oValue = null;
/*  93 */     if ((this.m_workspace == null) || (oKey == null))
/*     */     {
/*  95 */       return null;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 100 */       oValue = loadEx(oKey, false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 104 */       String errMsg = LocaleUtils.encodeMessage("csIdcCacheStoreLoadFailed", null, String.valueOf(oKey), this.m_cacheRegionName);
/* 105 */       Report.error("idccache", errMsg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 109 */       this.m_workspace.releaseConnection();
/*     */     }
/* 111 */     return oValue;
/*     */   }
/*     */ 
/*     */   private Object loadEx(Object oKey, boolean isStoreOp) throws Exception
/*     */   {
/* 116 */     Object oValue = null;
/* 117 */     if ((this.m_workspace == null) || (oKey == null))
/*     */     {
/* 119 */       return null;
/*     */     }
/* 121 */     MapParameters props = new MapParameters(new HashMap());
/* 122 */     props.m_map.put("dRegionName", this.m_cacheRegionName);
/* 123 */     props.m_map.put("dCacheKey", String.valueOf(oKey));
/* 124 */     props.m_map.put("UseForwardOnlyCursor", "1");
/* 125 */     String query = "QcacheStoreRegion";
/* 126 */     if (isStoreOp)
/*     */     {
/* 128 */       query = "QcacheStoreRegionHasEntry";
/*     */     }
/* 130 */     ResultSet rset = this.m_workspace.createResultSet(query, props);
/* 131 */     if ((rset == null) || (rset.isEmpty()))
/*     */     {
/* 133 */       return null;
/*     */     }
/* 135 */     if ((rset.first()) && (rset.isRowPresent()))
/*     */     {
/* 137 */       DataStreamValue jset = (DataStreamValue)rset;
/* 138 */       ObjectInputStream oStream = null;
/*     */       try
/*     */       {
/* 141 */         oStream = new ObjectInputStream(jset.getDataStream("dCacheValue"));
/* 142 */         oValue = oStream.readObject();
/*     */       }
/*     */       finally
/*     */       {
/* 146 */         FileUtils.closeObject(oStream);
/*     */       }
/*     */ 
/* 149 */       if (rset.next())
/*     */       {
/* 151 */         String msg = LocaleUtils.encodeMessage("csIdcCacheNotUniqueKeyError", null, String.valueOf(oKey));
/* 152 */         throw new DataException(msg);
/*     */       }
/*     */     }
/* 155 */     return oValue;
/*     */   }
/*     */ 
/*     */   public Map loadAll(Collection colKeys)
/*     */   {
/* 165 */     Map map = new HashMap();
/* 166 */     for (Iterator iter = colKeys.iterator(); iter.hasNext(); )
/*     */     {
/* 168 */       Object oKey = iter.next();
/* 169 */       Object oValue = load(oKey);
/* 170 */       if (oValue != null)
/*     */       {
/* 172 */         map.put(oKey, oValue);
/*     */       }
/*     */     }
/* 175 */     return map;
/*     */   }
/*     */ 
/*     */   public void erase(Object oKey)
/*     */   {
/* 184 */     if (this.m_workspace == null)
/*     */       return;
/*     */     try
/*     */     {
/* 188 */       DataBinder binder = new DataBinder();
/* 189 */       binder.putLocal("dRegionName", this.m_cacheRegionName);
/* 190 */       binder.putLocal("dCacheKey", String.valueOf(oKey));
/* 191 */       binder.putLocal("dEntryStatus", "delete");
/* 192 */       Date date = new Date();
/* 193 */       Timestamp time = new Timestamp(date.getTime());
/* 194 */       String dateStr = LocaleUtils.formatODBC(time);
/* 195 */       binder.putLocal("dCreateOrUpdateTime", dateStr);
/* 196 */       this.m_workspace.execute("UcacheStoreSetDelete", binder);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 200 */       String errMsg = LocaleUtils.encodeMessage("csIdcCacheStoreUpdateToDeleteFailed", null, String.valueOf(oKey), this.m_cacheRegionName);
/* 201 */       Report.error("idccache", errMsg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 205 */       this.m_workspace.releaseConnection();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void eraseAll(Collection colKeys)
/*     */   {
/* 216 */     if (this.m_workspace == null)
/*     */       return;
/* 218 */     String query = "UcacheStoreSetDelete";
/*     */     try
/*     */     {
/* 221 */       for (Iterator iter = colKeys.iterator(); iter.hasNext(); )
/*     */       {
/* 223 */         Object oKey = iter.next();
/* 224 */         DataBinder binder = new DataBinder();
/* 225 */         binder.putLocal("dRegionName", this.m_cacheRegionName);
/* 226 */         binder.putLocal("dCacheKey", String.valueOf(oKey));
/* 227 */         binder.putLocal("dEntryStatus", "delete");
/* 228 */         this.m_workspace.addBatch(query, binder);
/*     */       }
/* 230 */       this.m_workspace.executeBatch();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 234 */       String errMsg = LocaleUtils.encodeMessage("csIdcCacheStoreUpdateAllToDeleteFailed", null, this.m_cacheRegionName);
/* 235 */       Report.error("idccache", errMsg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 239 */       this.m_workspace.releaseConnection();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void store(Object oKey, Object oValue)
/*     */   {
/* 251 */     if ((oKey == null) || (this.m_workspace == null))
/*     */     {
/* 253 */       return;
/*     */     }
/*     */ 
/* 256 */     String query = "IcacheStoreRegion";
/* 257 */     String dEntryStatus = "insert";
/*     */     try
/*     */     {
/* 260 */       if (loadEx(oKey, true) != null)
/*     */       {
/* 262 */         query = "UcacheStoreRegion";
/* 263 */         dEntryStatus = "update";
/*     */       }
/* 265 */       MapParameters props = new MapParameters(new HashMap());
/*     */ 
/* 267 */       ByteArrayOutputStream baos = new ByteArrayOutputStream();
/* 268 */       ObjectOutputStream oos = new ObjectOutputStream(baos);
/* 269 */       oos.writeObject(oValue);
/* 270 */       oos.flush();
/* 271 */       oos.close();
/*     */ 
/* 273 */       InputStream is = new ByteArrayInputStream(baos.toByteArray());
/* 274 */       ParameterPacket packet = new ParameterPacket();
/* 275 */       packet.m_name = String.valueOf(oKey);
/* 276 */       packet.m_primaryObject = is;
/* 277 */       packet.m_infoMap.put("fileLength", Integer.toString(baos.toByteArray().length));
/*     */ 
/* 279 */       props.m_map.put("dRegionName", this.m_cacheRegionName);
/* 280 */       props.m_map.put("dCacheKey", String.valueOf(oKey));
/* 281 */       props.m_map.put("dCacheValue", packet);
/* 282 */       props.m_map.put("dEntryStatus", dEntryStatus);
/* 283 */       Date date = new Date();
/* 284 */       Timestamp time = new Timestamp(date.getTime());
/* 285 */       String dateStr = LocaleUtils.formatODBC(time);
/* 286 */       props.m_map.put("dCreateOrUpdateTime", dateStr);
/* 287 */       if (this.m_autoExpiryTime != -1L)
/*     */       {
/* 289 */         Date expiryDate = new Date();
/* 290 */         Timestamp expiryTime = new Timestamp(expiryDate.getTime() + this.m_autoExpiryTime);
/* 291 */         String expiryTimeStr = LocaleUtils.formatODBC(expiryTime);
/* 292 */         props.m_map.put("dAutoExpiryTime", expiryTimeStr);
/*     */       }
/*     */       else
/*     */       {
/* 296 */         props.m_map.put("dAutoExpiryTime", "");
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 301 */         this.m_workspace.execute(query, props);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 307 */         if (dEntryStatus.equals("insert"))
/*     */         {
/* 309 */           Report.trace("idccache", "Error inserting entry for key " + String.valueOf(oKey) + " on cache region " + this.m_cacheRegionName + ". Trying an update.", null);
/* 310 */           query = "UcacheStoreRegion";
/* 311 */           dEntryStatus = "update";
/* 312 */           props.m_map.put("dEntryStatus", dEntryStatus);
/* 313 */           this.m_workspace.execute(query, props);
/*     */         }
/*     */         else
/*     */         {
/* 317 */           throw new Exception(e);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 323 */       String errMsg = LocaleUtils.encodeMessage("csIdcCacheStoreInsertFailed", null, String.valueOf(oKey), this.m_cacheRegionName);
/* 324 */       Report.error("idccache", errMsg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 328 */       this.m_workspace.releaseConnection();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void storeAll(Map mapEntries)
/*     */   {
/* 338 */     boolean fRemove = true;
/*     */ 
/* 340 */     for (Iterator iter = mapEntries.entrySet().iterator(); iter.hasNext(); )
/*     */     {
/* 342 */       Map.Entry entry = (Map.Entry)iter.next();
/* 343 */       store(entry.getKey(), entry.getValue());
/* 344 */       if (fRemove)
/*     */       {
/*     */         try
/*     */         {
/* 348 */           iter.remove();
/*     */         }
/*     */         catch (UnsupportedOperationException e)
/*     */         {
/* 352 */           fRemove = false;
/* 353 */           String errMsg = LocaleUtils.encodeMessage("csIdcCacheStoreInsertAllFailed", null, this.m_cacheRegionName);
/* 354 */           Report.error("idccache", errMsg, e);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public Iterator keys()
/*     */   {
/* 366 */     List keyList = new ArrayList();
/* 367 */     if (this.m_workspace != null)
/*     */     {
/*     */       try
/*     */       {
/* 371 */         DataBinder binder = new DataBinder();
/* 372 */         binder.putLocal("dRegionName", this.m_cacheRegionName);
/* 373 */         ResultSet rset = this.m_workspace.createResultSet("QcacheStoreKeys", binder);
/* 374 */         if ((rset == null) || (rset.isEmpty()))
/*     */         {
/* 376 */           Iterator localIterator = keyList.iterator();
/*     */           return localIterator;
/*     */         }
/* 378 */         DataResultSet drset = new DataResultSet();
/* 379 */         drset.copy(rset);
/*     */ 
/* 381 */         int cacheKeyIndex = drset.getFieldInfoIndex("dCacheKey");
/* 382 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/* 384 */           String key = drset.getStringValue(cacheKeyIndex);
/* 385 */           if (key == null)
/*     */             continue;
/* 387 */           keyList.add(key);
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 393 */         String errMsg = LocaleUtils.encodeMessage("csIdcCacheStoreKeysFailed", null, this.m_cacheRegionName);
/* 394 */         Report.error("idccache", errMsg, e);
/*     */       }
/*     */       finally
/*     */       {
/* 398 */         this.m_workspace.releaseConnection();
/*     */       }
/*     */     }
/* 401 */     return keyList.iterator();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 406 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105320 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcCacheStore
 * JD-Core Version:    0.5.4
 */