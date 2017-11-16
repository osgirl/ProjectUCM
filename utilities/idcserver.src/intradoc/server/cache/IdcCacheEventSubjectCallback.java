/*     */ package intradoc.server.cache;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataStreamValue;
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import java.io.ObjectInputStream;
/*     */ import java.io.Serializable;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcCacheEventSubjectCallback extends SubjectCallbackAdapter
/*     */ {
/*  41 */   private static String m_subjectNamePrefix = "idccacheevent";
/*     */ 
/*  46 */   private static HashMap m_notifyTimeMap = new HashMap();
/*     */ 
/*     */   public void setWorkspace(Workspace ws)
/*     */   {
/*  55 */     this.m_workspace = ws;
/*     */   }
/*     */ 
/*     */   public void refresh(String subject)
/*     */     throws DataException, ServiceException
/*     */   {
/*  62 */     String dRegionName = subject.substring(m_subjectNamePrefix.length() + 1);
/*  63 */     if (Report.m_verbose)
/*     */     {
/*  65 */       Report.trace("idccache", "Received a subject notification indication update in the cache region " + dRegionName, null);
/*     */     }
/*  67 */     if (this.m_workspace == null)
/*     */       return;
/*  69 */     generateCacheEventsFromNotification(dRegionName);
/*     */   }
/*     */ 
/*     */   public void generateCacheEventsFromNotification(String cacheNameWithService)
/*     */     throws ServiceException, DataException
/*     */   {
/*  82 */     Object oValue = null;
/*  83 */     String key = null;
/*  84 */     String serviceName = cacheNameWithService.substring(0, cacheNameWithService.indexOf(46));
/*  85 */     String cacheRegionName = cacheNameWithService.substring(cacheNameWithService.indexOf(46) + 1);
/*  86 */     boolean isPersistent = IdcCacheFactory.isPersistentService(serviceName);
/*  87 */     boolean isAutoExpiry = IdcCacheFactory.isAutoExpiryService(serviceName);
/*  88 */     String autoExpiryTime = IdcCacheFactory.getAutoExpiryTimeFromService(serviceName);
/*     */ 
/*  90 */     IdcDefaultCacheRegion cacheRegion = (IdcDefaultCacheRegion)IdcCacheFactory.getCacheRegion(cacheRegionName, isPersistent, isAutoExpiry, autoExpiryTime);
/*     */ 
/*  93 */     String timestamp = (String)m_notifyTimeMap.get(cacheRegionName);
/*  94 */     if (timestamp == null)
/*     */     {
/*  96 */       timestamp = LocaleUtils.formatODBC(new Date(0L));
/*     */     }
/*  98 */     Date putTimestamp = LocaleUtils.parseODBC(timestamp);
/*     */     try
/*     */     {
/* 102 */       MapParameters props = new MapParameters(new HashMap());
/* 103 */       props.m_map.put("dRegionName", cacheRegionName);
/* 104 */       props.m_map.put("dCreateOrUpdateTime", timestamp);
/* 105 */       ResultSet rset = this.m_workspace.createResultSet("QcacheStoreEntriesSinceDate", props);
/* 106 */       if ((rset == null) || (rset.isEmpty())) {
/*     */         return;
/*     */       }
/*     */ 
/* 110 */       int keyIndex = rset.getFieldInfoIndex("dCacheKey");
/* 111 */       int timestampIndex = rset.getFieldInfoIndex("dCreateOrUpdateTime");
/* 112 */       int entryStatusIndex = rset.getFieldInfoIndex("dEntryStatus");
/* 113 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/* 115 */         key = rset.getStringValue(keyIndex);
/* 116 */         Date dbDate = LocaleUtils.parseODBC(rset.getStringValue(timestampIndex));
/* 117 */         String entryStatus = rset.getStringValue(entryStatusIndex);
/* 118 */         if (putTimestamp.compareTo(dbDate) < 0)
/*     */         {
/* 120 */           putTimestamp = dbDate;
/*     */         }
/* 122 */         DataStreamValue jset = (DataStreamValue)(DataStreamValue)rset;
/*     */ 
/* 124 */         ObjectInputStream oStream = null;
/*     */         try
/*     */         {
/* 127 */           oStream = new ObjectInputStream(jset.getDataStream("dCacheValue"));
/* 128 */           oValue = oStream.readObject();
/*     */         }
/*     */         finally
/*     */         {
/* 132 */           FileUtils.closeObject(oStream);
/*     */         }
/*     */ 
/* 135 */         if (entryStatus.equals("insert"))
/*     */         {
/* 137 */           cacheRegion.insertSubjectNotification(key, (Serializable)oValue);
/*     */         }
/* 140 */         else if (entryStatus.equals("update"))
/*     */         {
/* 142 */           cacheRegion.updateSubjectNotification(key, (Serializable)oValue);
/*     */         }
/* 145 */         else if (entryStatus.equals("delete"))
/*     */         {
/* 147 */           cacheRegion.deleteSubjectNotification(key);
/*     */         }
/*     */         else
/*     */         {
/* 151 */           Report.trace("idccache", "Invalid Cache Entry Status", null);
/*     */         }
/*     */       }
/* 154 */       m_notifyTimeMap.put(cacheRegionName, LocaleUtils.formatODBC(putTimestamp));
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */       String errMsg;
/* 159 */       throw new DataException(errMsg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 163 */       this.m_workspace.releaseConnection();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 170 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103947 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcCacheEventSubjectCallback
 * JD-Core Version:    0.5.4
 */