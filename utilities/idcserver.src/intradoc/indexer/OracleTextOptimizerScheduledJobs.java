/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.jobs.JobState;
/*     */ import intradoc.server.jobs.ScheduledJobImplementor;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class OracleTextOptimizerScheduledJobs
/*     */   implements ScheduledJobImplementor
/*     */ {
/*     */   protected JobState m_jState;
/*     */   protected Workspace m_workspace;
/*     */   protected ExecutionContext m_cxt;
/*     */ 
/*     */   public OracleTextOptimizerScheduledJobs()
/*     */   {
/*  33 */     this.m_jState = null;
/*  34 */     this.m_workspace = null;
/*  35 */     this.m_cxt = null;
/*     */   }
/*     */ 
/*     */   public Object processJob(JobState jState, Workspace ws, ExecutionContext cxt) throws ServiceException
/*     */   {
/*  40 */     this.m_jState = jState;
/*  41 */     this.m_workspace = ws;
/*  42 */     this.m_cxt = cxt;
/*     */ 
/*  44 */     String providerName = jState.m_data.getLocal("CollectionDatabaseProvider");
/*  45 */     if (providerName == null)
/*     */     {
/*  48 */       Report.info("indexer", null, "csUnableToOptimizeCollectionDatabaseProviderDoesNotExist", new Object[0]);
/*     */ 
/*  51 */       updateProgress(-1, null, "csUnableToOptimizeCollectionDatabaseProviderDoesNotExist", new Object[0]);
/*     */     }
/*     */     else
/*     */     {
/*  56 */       Provider prov = Providers.getProvider(providerName);
/*  57 */       if (prov != null)
/*     */       {
/*  59 */         boolean resetSemBack = false;
/*  60 */         String semInSession = null;
/*     */         try
/*     */         {
/*  64 */           String tableName = this.m_jState.m_data.getLocal("indexName").toUpperCase();
/*  65 */           semInSession = OracleTextUtils.getLengthSemanticsFromSession(ws);
/*  66 */           resetSemBack = OracleTextUtils.checkAndSetLengthSemantics(ws, "DR$" + tableName + "$I", "TOKEN_TEXT", semInSession);
/*     */ 
/*  68 */           Report.trace("indexer", null, "csOracleTextIndexOptimizedWithRebuildStart", new Object[] { this.m_jState.m_data.getLocal("indexName") });
/*     */ 
/*  70 */           updateProgress(4, null, "csOracleTextIndexOptimizedWithRebuildStart", new Object[] { this.m_jState.m_data.getLocal("indexName") });
/*     */ 
/*  74 */           Workspace idxWS = (Workspace)prov.getProvider();
/*  75 */           idxWS.executeCallable("CoracleTextOptimizeIndex", this.m_jState.m_data);
/*     */ 
/*  77 */           Report.info("indexer", null, "csOracleTextIndexOptimizedWithRebuildFinished", new Object[] { this.m_jState.m_data.getLocal("indexName") });
/*     */ 
/*  79 */           updateIndexerState();
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/*  84 */           Report.warning("indexer", t, "csOracleTextUnableToOptWithRebuild", new Object[0]);
/*  85 */           updateProgress(-1, t, "csOracleTextUnableToOptWithRebuild", new Object[0]);
/*     */         }
/*     */         finally
/*     */         {
/*  89 */           if (resetSemBack)
/*     */           {
/*  92 */             OracleTextUtils.setLengthSemantics(ws, semInSession);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/*  99 */         Report.warning("indexer", null, "csOracleTextNoProviderFoundForOptRebuild", new Object[] { providerName });
/*     */ 
/* 101 */         updateProgress(2, null, "csOracleTextNoProviderFoundForOptRebuild", new Object[] { providerName });
/*     */       }
/*     */     }
/*     */ 
/* 105 */     return null;
/*     */   }
/*     */ 
/*     */   protected void updateProgress(int type, Throwable t, String key, Object[] args)
/*     */   {
/*     */     try
/*     */     {
/* 112 */       IdcMessage idcMsg = IdcMessageFactory.lc(key, args);
/* 113 */       idcMsg.m_throwable = t;
/* 114 */       this.m_jState.updateAndCheckProgress(3, idcMsg, this.m_jState.m_data, null, this.m_workspace, this.m_cxt);
/*     */     }
/*     */     catch (Throwable et)
/*     */     {
/* 119 */       this.m_jState.reportProgress(-1, "Unable to update progress for ", et);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateProgress(int type, IdcMessage idcMsg, DataBinder binder, Map args, Workspace ws)
/*     */     throws ServiceException, DataException
/*     */   {
/* 126 */     this.m_jState.updateAndCheckProgress(3, idcMsg, this.m_jState.m_data, null, this.m_workspace, this.m_cxt);
/*     */   }
/*     */ 
/*     */   public void updateIndexerState()
/*     */     throws Exception
/*     */   {
/* 132 */     String collDir = this.m_jState.m_data.getLocal("collectionDir");
/* 133 */     if (collDir == null)
/*     */     {
/* 135 */       Report.warning("indexer", null, "csOracleTextOptRebuildNoCollectionDir", new Object[0]);
/* 136 */       this.m_jState.updateAndCheckProgress(-1, IdcMessageFactory.lc("csOracleTextOptRebuildNoCollectionDir", new Object[0]), this.m_jState.m_data, null, this.m_workspace, this.m_cxt);
/*     */ 
/* 139 */       return;
/*     */     }
/* 141 */     if (FileUtils.checkFile(collDir + "values.hda", true, false) == -16)
/*     */       return;
/*     */     try
/*     */     {
/* 145 */       DataBinder collValues = ResourceUtils.readDataBinder(collDir, "values.hda");
/*     */ 
/* 147 */       String cumTotalAddIndexStr = collValues.getLocal("cumTotalAddIndex");
/* 148 */       String cumTotalDeletedIndexStr = collValues.getLocal("cumTotalDeleteIndex");
/* 149 */       int cumTotalChanged = NumberUtils.parseInteger(cumTotalAddIndexStr, 0) + NumberUtils.parseInteger(cumTotalDeletedIndexStr, 0);
/*     */ 
/* 152 */       collValues.putLocal("lastRebuildOptimizationCount", "" + cumTotalChanged);
/* 153 */       collValues.putLocal("lastFastOptimizationCount", "" + cumTotalChanged);
/* 154 */       ResourceUtils.serializeDataBinder(collDir, "values.hda", collValues, true, false);
/*     */ 
/* 156 */       updateProgress(2, null, "csOracleTextIndexOptimizedWithRebuildFinished", new Object[] { this.m_jState.m_data.getLocal("indexName") });
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 162 */       Report.warning("indexer", t, "csOracleTextUnableToUpdateLastRebuildOptCount", new Object[0]);
/* 163 */       IdcMessage idcMsg = IdcMessageFactory.lc("csOracleTextUnableToUpdateLastRebuildOptCount", new Object[0]);
/* 164 */       idcMsg.m_throwable = t;
/* 165 */       this.m_jState.updateAndCheckProgress(-1, idcMsg, this.m_jState.m_data, null, this.m_workspace, this.m_cxt);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void finishJob()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void buildResult()
/*     */   {
/*     */   }
/*     */ 
/*     */   public DataResultSet createExceptionSet(DataBinder binder)
/*     */   {
/* 183 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 188 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 91782 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.OracleTextOptimizerScheduledJobs
 * JD-Core Version:    0.5.4
 */