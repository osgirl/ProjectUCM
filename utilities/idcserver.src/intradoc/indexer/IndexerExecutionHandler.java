/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IndexerExecutionHandler
/*     */ {
/*     */   public static final int FINISHED = 0;
/*     */   public static final int CONTINUE = 1;
/*     */   protected IndexerConfig m_config;
/*     */   protected IndexerWorkObject m_data;
/*     */   protected IndexerExecution m_execution;
/*     */   public boolean m_storeIndexOnly;
/*     */ 
/*     */   public IndexerExecutionHandler()
/*     */   {
/*  38 */     this.m_storeIndexOnly = true;
/*     */   }
/*     */ 
/*     */   public void init(IndexerExecution exec)
/*     */     throws ServiceException
/*     */   {
/*  44 */     this.m_data = exec.m_data;
/*  45 */     this.m_config = ((IndexerConfig)this.m_data.getCachedObject("IndexerConfig"));
/*  46 */     this.m_execution = exec;
/*     */   }
/*     */ 
/*     */   public int prepare(Hashtable props, Properties prop) throws ServiceException {
/*  50 */     return 0;
/*     */   }
/*     */ 
/*     */   public void executeIndexer(Vector list, Hashtable props) throws ServiceException {
/*  54 */     int size = list.size();
/*  55 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  57 */       IndexerInfo ii = (IndexerInfo)list.elementAt(i);
/*     */ 
/*  59 */       ii.m_indexStatus = 0;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void prepareIndexDoc(Properties prop, IndexerInfo ii)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public int parseResults(String input)
/*     */     throws ServiceException
/*     */   {
/*  71 */     return -1;
/*     */   }
/*     */ 
/*     */   public void verifyCollection(IndexerCollectionData curCollectionDef) throws ServiceException
/*     */   {
/*  76 */     IndexerCollectionData collectionDef = this.m_execution.m_collectionDef;
/*  77 */     if (collectionDef == null)
/*     */     {
/*  79 */       String engineName = SearchIndexerUtils.getSearchEngineName(this.m_data);
/*  80 */       collectionDef = SearchLoader.retrieveIndexDesign(engineName);
/*     */     }
/*  82 */     curCollectionDef.shallowClone(collectionDef);
/*     */   }
/*     */ 
/*     */   public boolean checkCollectionExistence(boolean mustExist, String errMsg) throws ServiceException
/*     */   {
/*  87 */     return true;
/*     */   }
/*     */ 
/*     */   public void writeBatchFile(Vector list, Hashtable props)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void validateConfig()
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public int cleanUp() throws ServiceException
/*     */   {
/* 102 */     return 0;
/*     */   }
/*     */ 
/*     */   public void createCollection(String id)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 112 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98460 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerExecutionHandler
 * JD-Core Version:    0.5.4
 */