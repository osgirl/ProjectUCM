/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IndexerDriverAdaptor
/*     */   implements IndexerDriver
/*     */ {
/*     */   protected ExecutionContext m_context;
/*     */   protected IndexerWorkObject m_data;
/*     */   protected IndexerConfig m_config;
/*     */   protected String m_collectionDirectory;
/*     */   protected boolean m_hasStarted;
/*     */   protected IndexerExecution m_execution;
/*     */ 
/*     */   public IndexerDriverAdaptor()
/*     */   {
/*  38 */     this.m_hasStarted = false;
/*     */   }
/*     */ 
/*     */   public void init(IndexerWorkObject data)
/*     */   {
/*  44 */     this.m_data = data;
/*     */     try
/*     */     {
/*  47 */       this.m_config = SearchIndexerUtils.getIndexerConfig(data, data.m_state.m_cycleId);
/*  48 */       this.m_execution = SearchIndexerUtils.getIndexerExecution(this.m_config, data);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  52 */       Report.error(null, "!csCommonSearchIndexerUnableToInitConfigOrImplementor", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Properties prepare(Hashtable infos)
/*     */     throws ServiceException
/*     */   {
/*  60 */     Properties prop = this.m_execution.prepare(infos);
/*  61 */     this.m_collectionDirectory = this.m_config.getValue("IndexerCollectionDir");
/*  62 */     if (!this.m_hasStarted)
/*     */     {
/*  64 */       this.m_hasStarted = true;
/*  65 */       loadCollectionValues();
/*     */     }
/*  67 */     return prop;
/*     */   }
/*     */ 
/*     */   public DataBinder getIndexerStateBinder() {
/*  71 */     return this.m_data.m_state.m_state;
/*     */   }
/*     */ 
/*     */   public void loadCollectionValues()
/*     */   {
/*  76 */     DataBinder collectionValues = this.m_data.m_state.getCollectionValues();
/*  77 */     String colValuesPath = this.m_collectionDirectory + "/values.hda";
/*  78 */     if ((FileUtils.checkFile(colValuesPath, true, true) == 0) && (!this.m_data.isRebuild()))
/*     */     {
/*     */       try
/*     */       {
/*  83 */         ResourceUtils.serializeDataBinder(this.m_collectionDirectory, "values.hda", collectionValues, false, true);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/*  88 */         Report.error(null, null, e);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/*  94 */       collectionValues.putLocal("isNewStart", "1");
/*     */     }
/*  96 */     this.m_data.m_state.loadCollectionCounters();
/*     */   }
/*     */ 
/*     */   public void saveCollectionValues()
/*     */   {
/* 101 */     this.m_data.m_state.putCollectionCounters();
/* 102 */     if (FileUtils.checkFile(this.m_collectionDirectory, false, false) != 0)
/*     */       return;
/* 104 */     DataBinder collectionValues = this.m_data.m_state.getCollectionValues();
/*     */     try
/*     */     {
/* 107 */       ResourceUtils.serializeDataBinder(this.m_collectionDirectory, "values.hda", collectionValues, true, true);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 112 */       Report.error(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void checkConnection()
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public boolean checkCollectionExistence(boolean mustExist, String errMsg)
/*     */     throws ServiceException
/*     */   {
/* 125 */     return this.m_execution.checkCollectionExistence(mustExist, errMsg);
/*     */   }
/*     */ 
/*     */   public void validateConfig() throws ServiceException
/*     */   {
/* 130 */     this.m_execution.validateConfig();
/*     */   }
/*     */ 
/*     */   public void verifyCollection(IndexerCollectionData collectionDef) throws ServiceException
/*     */   {
/* 135 */     this.m_execution.verifyCollection(collectionDef);
/*     */   }
/*     */ 
/*     */   public void executeIndexer(Vector list, Hashtable docProps) throws ServiceException
/*     */   {
/* 140 */     this.m_execution.executeIndexer(list, docProps);
/*     */ 
/* 144 */     this.m_data.m_state.setCollectionValue("isNewStart", "");
/* 145 */     this.m_data.m_state.m_cumTotalFileSize += NumberUtils.parseInteger(this.m_config.getValue("IndexerBatchFileSize"), 0);
/*     */ 
/* 147 */     saveCollectionValues();
/*     */   }
/*     */ 
/*     */   public void executeIndexer() throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public String getCollectionID()
/*     */   {
/* 156 */     return SharedObjects.getEnvironmentValue("IDC_Name");
/*     */   }
/*     */ 
/*     */   public void cleanup() throws ServiceException
/*     */   {
/* 161 */     saveCollectionValues();
/*     */ 
/* 163 */     this.m_execution.cleanUp();
/*     */   }
/*     */ 
/*     */   public void subjectNotification()
/*     */   {
/* 168 */     SubjectManager.forceRefresh("searchapi");
/* 169 */     SubjectManager.forceRefresh("dynamicqueries");
/* 170 */     SubjectManager.forceRefresh("metadata");
/*     */   }
/*     */ 
/*     */   public String findIndexExtension(String ext) {
/* 174 */     return null;
/*     */   }
/*     */ 
/*     */   public void finishBulkFile()
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void prepareBulkFile()
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void writeBulkEntry(IndexerInfo ii, Properties props) throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 193 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerDriverAdaptor
 * JD-Core Version:    0.5.4
 */