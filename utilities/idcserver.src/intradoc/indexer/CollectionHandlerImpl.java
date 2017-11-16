/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.server.alert.AlertUtils;
/*     */ import intradoc.shared.ActiveIndexState;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.SearchFieldInfo;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CollectionHandlerImpl
/*     */   implements IndexerCollectionHandler
/*     */ {
/*     */   protected String m_activeIndex;
/*     */   protected DataResultSet m_collections;
/*     */   protected boolean m_isRebuild;
/*     */   protected IndexerConfig m_config;
/*     */   protected IndexerWorkObject m_data;
/*     */   protected IndexerCollectionManager m_manager;
/*     */ 
/*     */   public CollectionHandlerImpl()
/*     */   {
/*  33 */     this.m_isRebuild = false;
/*  34 */     this.m_config = null;
/*  35 */     this.m_data = null;
/*     */   }
/*     */ 
/*     */   public void init(IndexerWorkObject data, IndexerCollectionManager manager) throws ServiceException
/*     */   {
/*  40 */     this.m_isRebuild = data.isRebuild();
/*  41 */     this.m_collections = data.m_config.getTable("CollectionID");
/*  42 */     this.m_activeIndex = CollectionHandlerUtils.getActiveIndex(this.m_isRebuild, this.m_collections);
/*  43 */     this.m_config = data.m_config;
/*  44 */     this.m_data = data;
/*  45 */     this.m_manager = manager;
/*     */   }
/*     */ 
/*     */   public boolean checkActiveCollectionIdValid() throws ServiceException
/*     */   {
/*  50 */     boolean validIDRequired = this.m_config.getBoolean("ValidCollectionIDRequired", false);
/*     */ 
/*  52 */     String key = "";
/*  53 */     if (validIDRequired)
/*     */     {
/*     */       try
/*     */       {
/*  57 */         key = ResultSetUtils.findValue(this.m_collections, "IndexerLabel", this.m_activeIndex, "IndexerLabel");
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*  62 */         String msg = LocaleUtils.encodeMessage("csIndexerErrorWhileChekcinCollectionId", e.getMessage());
/*  63 */         throw new ServiceException(msg, e);
/*     */       }
/*     */     }
/*  66 */     return key != null;
/*     */   }
/*     */ 
/*     */   public boolean checkCollectionExistence() throws ServiceException
/*     */   {
/*  71 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean isCollectionUpToDate(IndexerWorkObject data) throws ServiceException
/*     */   {
/*  76 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean compareCollectionDesign(IndexerCollectionData def)
/*     */     throws ServiceException
/*     */   {
/*  82 */     return true;
/*     */   }
/*     */ 
/*     */   public String manageCollection(IndexerCollectionData def, IndexerWorkObject data) throws ServiceException
/*     */   {
/*  87 */     return "DesignUpToDate";
/*     */   }
/*     */ 
/*     */   public boolean loadCollectionDesign(IndexerCollectionData def) throws ServiceException
/*     */   {
/*  92 */     return false;
/*     */   }
/*     */ 
/*     */   public void validateConfiguration()
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void cleanUp(IndexerWorkObject data) throws ServiceException
/*     */   {
/* 102 */     boolean useRebuildOpt = data.m_state.getCurrentState().equals("FinishRebuild");
/* 103 */     if ((!useRebuildOpt) || 
/* 106 */       (!this.m_config.getBoolean("IndexerAutoSwitch", true)))
/*     */       return;
/* 108 */     ActiveIndexState.setActiveProperty("ActiveIndex", this.m_activeIndex);
/*     */ 
/* 114 */     DataResultSet drset = SharedObjects.getTable("SearchCollections");
/*     */     try
/*     */     {
/* 119 */       int idIdx = ResultSetUtils.getIndexMustExist(drset, "sCollectionID");
/* 120 */       int locIdx = ResultSetUtils.getIndexMustExist(drset, "sLocation");
/* 121 */       FieldInfo localeInfo = new FieldInfo();
/* 122 */       drset.getFieldInfo("sVerityLocale", localeInfo);
/*     */ 
/* 124 */       String idcName = data.getEnvironmentValue("IDC_Name");
/* 125 */       Vector v = drset.findRow(idIdx, idcName);
/* 126 */       if (v != null)
/*     */       {
/* 128 */         v.setElementAt(this.m_activeIndex, locIdx);
/* 129 */         if (localeInfo.m_index != -1)
/*     */         {
/* 131 */           v.setElementAt(this.m_config.getValue("IndexerLocale"), localeInfo.m_index);
/*     */         }
/* 133 */         ActiveIndexState.setSearchCollections(drset);
/*     */       }
/*     */ 
/* 136 */       if ((this.m_data.m_collectionDef.m_securityInfos != null) && (!this.m_data.m_collectionDef.m_securityInfos.isEmpty()))
/*     */       {
/* 138 */         SearchFieldInfo.setActiveIndexPropsOnCleanup(this.m_data.m_collectionDef.m_securityInfos);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 144 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 148 */     ActiveIndexState.save();
/*     */ 
/* 154 */     SharedObjects.putEnvironmentValue("ActiveIndex", this.m_activeIndex);
/* 155 */     SharedObjects.putTable("SearchCollections", (DataResultSet)ActiveIndexState.getSearchCollections());
/*     */ 
/* 158 */     String engineName = SearchIndexerUtils.getSearchEngineName(data);
/* 159 */     SearchLoader.setCurrentIndexDesign(engineName, data.m_collectionDef);
/*     */ 
/* 161 */     ((IndexerDriverAdaptor)data.m_driver).subjectNotification();
/*     */ 
/* 164 */     String alertId = "AutoIndexerRebuildWarning";
/*     */     try
/*     */     {
/* 168 */       boolean rebuildIndexAlertExists = AlertUtils.existsAlert(alertId, 1);
/*     */ 
/* 170 */       if (rebuildIndexAlertExists)
/*     */       {
/* 172 */         DataBinder alertbinder = new DataBinder();
/* 173 */         alertbinder.putLocal("alertId", alertId);
/* 174 */         AlertUtils.deleteAlert(alertbinder);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 179 */       Report.error(null, e, new IdcMessage("csAlertDeletionError", new Object[] { alertId }));
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 188 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96805 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.CollectionHandlerImpl
 * JD-Core Version:    0.5.4
 */