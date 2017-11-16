/*     */ package intradoc.server.cache;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import java.util.Date;
/*     */ import java.util.TimerTask;
/*     */ 
/*     */ public class IdcCacheAutoExpiredScheduler extends TimerTask
/*     */ {
/*     */   private Workspace m_workspace;
/*     */ 
/*     */   public void setWorkspace(Workspace ws)
/*     */   {
/*  48 */     this.m_workspace = ws;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     try
/*     */     {
/*  56 */       if (this.m_workspace != null)
/*     */       {
/*  58 */         updateExpiredEntries();
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  63 */       Report.trace("idccache", "Unable to update expired cache entries", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void updateExpiredEntries()
/*     */     throws DataException
/*     */   {
/*     */     try
/*     */     {
/*  75 */       DataBinder binder = new DataBinder();
/*  76 */       String dateStr = LocaleUtils.formatODBC(new Date());
/*  77 */       binder.putLocal("dAutoExpiryTime", dateStr);
/*  78 */       ResultSet rset = this.m_workspace.createResultSet("QcacheExpiredEntries", binder);
/*  79 */       DataResultSet drset = new DataResultSet();
/*  80 */       drset.copy(rset);
/*  81 */       if (!drset.isEmpty())
/*     */       {
/*  83 */         int keyIndex = rset.getFieldInfoIndex("dCacheKey");
/*  84 */         int regionNameIndex = rset.getFieldInfoIndex("dRegionName");
/*  85 */         int count = 0;
/*  86 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/*  88 */           String key = drset.getStringValue(keyIndex);
/*  89 */           String region = drset.getStringValue(regionNameIndex);
/*  90 */           IdcCacheRegion cache = IdcCacheFactory.getCacheRegion(region);
/*  91 */           cache.remove(key);
/*  92 */           if (Report.m_verbose)
/*     */           {
/*  94 */             Report.trace("idccache", "Key '" + key + "' expired from cache '" + region + "'", null);
/*     */           }
/*  96 */           ++count;
/*     */         }
/*  98 */         Report.trace("idccache", "Number of expired cache entries " + count, null);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */       String errMsg;
/* 104 */       throw new DataException(errMsg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 108 */       this.m_workspace.releaseConnection();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 114 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99324 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcCacheAutoExpiredScheduler
 * JD-Core Version:    0.5.4
 */