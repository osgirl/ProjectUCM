/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ 
/*     */ public class IndexerCollectionManager extends IndexerStepImpl
/*     */ {
/*     */   IndexerCollectionHandler m_handler;
/*     */   IndexerConfig m_config;
/*     */   String m_activeIndex;
/*     */ 
/*     */   public IndexerCollectionManager()
/*     */   {
/*  25 */     this.m_handler = null;
/*  26 */     this.m_config = null;
/*  27 */     this.m_activeIndex = null;
/*     */   }
/*     */ 
/*     */   public void init(IndexerWorkObject data) throws ServiceException {
/*  31 */     this.m_config = data.m_config;
/*  32 */     String handler = this.m_config.getConfigValue("IndexerCollectionHandler");
/*  33 */     this.m_handler = ((IndexerCollectionHandler)ComponentClassFactory.createClassInstance("IndexerCollectionHandler", handler, "!csIndexerUnableToCreateCollectionHandler"));
/*     */ 
/*  35 */     this.m_handler.init(data, this);
/*  36 */     data.m_indexCollectionManager = this;
/*     */   }
/*     */ 
/*     */   public void initStep(IndexerWorkObject data)
/*     */     throws ServiceException
/*     */   {
/*  42 */     init(data);
/*     */   }
/*     */ 
/*     */   public String doWork(String step, IndexerWorkObject data, boolean restart)
/*     */     throws ServiceException
/*     */   {
/*  48 */     String returnState = "DesignUpToDate";
/*  49 */     Boolean collectionHandled = (Boolean)data.getCachedObject("CollectionUpToDate");
/*  50 */     if ((collectionHandled == null) || (!collectionHandled.booleanValue()))
/*     */     {
/*  52 */       String fastRebuildStr = data.m_state.getStateValue("fastRebuild");
/*  53 */       boolean isFastRebuild = StringUtils.convertToBool(fastRebuildStr, false);
/*  54 */       boolean engineCapable = this.m_config.getBoolean("SupportFastRebuild", false);
/*     */ 
/*  56 */       if (!engineCapable)
/*     */       {
/*  58 */         if (isFastRebuild == true)
/*     */         {
/*  60 */           String msg = LocaleUtils.encodeMessage("csIndexerEngineNotSupportingFastRebuild", null, null);
/*  61 */           Report.trace("indexer", msg, null);
/*     */ 
/*  63 */           if (Report.m_verbose)
/*     */           {
/*  65 */             Report.trace("indexer", "FastRebuild invoked for " + this.m_config.getCurrentEngineName() + " which does not support it. Setting it to false.", null);
/*     */           }
/*     */         }
/*     */ 
/*  69 */         isFastRebuild = false;
/*     */       }
/*     */ 
/*  72 */       if (step.equalsIgnoreCase("CheckCollection"))
/*     */       {
/*  74 */         boolean canHandle = this.m_handler.checkActiveCollectionIdValid();
/*  75 */         if (!canHandle)
/*     */         {
/*  77 */           returnState = "Error";
/*  78 */           Report.trace("indexer", null, "csIndexerCollectionIDError", new Object[0]);
/*     */         }
/*  80 */         else if ((data.isRebuild()) || (isFastRebuild))
/*     */         {
/*  82 */           boolean collUpToDate = this.m_handler.checkCollectionExistence();
/*     */ 
/*  84 */           if ((!data.isRebuild()) && (!collUpToDate))
/*     */           {
/*  86 */             Report.info("indexer", null, "csIndexerSearchCollectionMissing", new Object[0]);
/*     */           }
/*  88 */           if ((!data.isRestart()) && (data.isRebuild()))
/*     */           {
/*  90 */             collUpToDate = this.m_handler.isCollectionUpToDate(data);
/*     */           }
/*  92 */           if (!collUpToDate)
/*     */           {
/*  94 */             Report.info("indexer", null, "csIndexerCollectionNotUpToDate", new Object[0]);
/*  95 */             returnState = "Changed";
/*     */           }
/*     */         }
/*     */       }
/*  99 */       else if (step.equalsIgnoreCase("ManageCollection"))
/*     */       {
/* 101 */         returnState = this.m_handler.manageCollection(data.m_collectionDef, data);
/* 102 */         if ((returnState != null) && (returnState.equalsIgnoreCase("CollectionUpToDate")))
/*     */         {
/* 104 */           data.setCachedObject("CollectionUpToDate", Boolean.TRUE);
/*     */         }
/*     */       }
/*     */     }
/* 108 */     else if (data.isRebuild())
/*     */     {
/* 110 */       returnState = "FinishRebuild";
/*     */     }
/* 112 */     return returnState;
/*     */   }
/*     */ 
/*     */   public IndexerCollectionHandler getHandler()
/*     */   {
/* 117 */     return this.m_handler;
/*     */   }
/*     */ 
/*     */   public boolean loadCollectionDefinition(IndexerCollectionData dataDef) throws ServiceException
/*     */   {
/* 122 */     return this.m_handler.loadCollectionDesign(dataDef);
/*     */   }
/*     */ 
/*     */   public boolean checkCollectionExistence() throws ServiceException
/*     */   {
/* 127 */     return this.m_handler.checkCollectionExistence();
/*     */   }
/*     */ 
/*     */   public void validateConfiguration() throws ServiceException
/*     */   {
/* 132 */     this.m_handler.validateConfiguration();
/*     */   }
/*     */ 
/*     */   public void cleanUp(IndexerWorkObject data)
/*     */     throws ServiceException
/*     */   {
/* 138 */     this.m_handler.cleanUp(data);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 143 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84554 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerCollectionManager
 * JD-Core Version:    0.5.4
 */