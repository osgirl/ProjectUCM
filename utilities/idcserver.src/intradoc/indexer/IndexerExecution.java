/*      */ package intradoc.indexer;
/*      */ 
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NativeOsUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.server.LegacyDirectoryLocator;
/*      */ import intradoc.server.SearchIndexerUtils;
/*      */ import intradoc.shared.ActiveIndexState;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.IndexerCollectionData;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.taskmanager.TaskInfo;
/*      */ import intradoc.taskmanager.TaskInfo.STATUS;
/*      */ import intradoc.taskmanager.TaskMonitor;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class IndexerExecution
/*      */ {
/*      */   public IndexerWorkObject m_data;
/*      */   public IndexerConfig m_config;
/*      */   public IndexerDriverAdaptor m_driver;
/*      */   public DocIndexerAdaptor m_indexer;
/*      */   public String m_buildCollectionDir;
/*      */   public String m_activeCollectionDir;
/*      */   protected boolean m_maintainIndexProcess;
/*      */   protected DataResultSet m_collections;
/*      */   public String m_collectionID1;
/*      */   public String m_collectionID2;
/*      */   public String m_activeCollectionId;
/*      */   public String m_bulkDir;
/*      */   protected boolean m_isCreateCollection;
/*      */   public NativeOsUtils m_nativeOsUtils;
/*      */   public long m_semaphoreHandle;
/*      */   public String m_indexerSemaphoreName;
/*      */   public String m_currentPid;
/*      */   public Vector m_cmdLine;
/*      */   protected byte[] m_eod;
/*      */   protected String m_bulkloadFileName;
/*      */   protected FileOutputStream m_bulkFileOstream;
/*      */   protected Process m_bulkExe;
/*      */   protected boolean m_isIndexerProcessStarted;
/*      */   public IndexerCollectionData m_collectionDef;
/*      */   protected String m_collectionDirectory;
/*      */   protected long m_bulkLoadSize;
/*      */   public Vector m_indexerInfoList;
/*      */   protected boolean m_isDelete;
/*      */   protected String m_bulkLoadFileName;
/*      */   protected boolean m_haveWarnedUserAboutCollectionMismatch;
/*      */   protected boolean m_abort;
/*      */   protected boolean m_doneIndexing;
/*      */   protected boolean m_indexingFailed;
/*      */   protected boolean m_countMismatch;
/*      */   protected String m_bgExceptionMsg;
/*      */   protected boolean m_docCountCheck;
/*      */   protected boolean m_storeIndexOnly;
/*      */   public IndexerExecutionHandler m_handler;
/*      */   public HashMap<String, IndexerConversionHandler> m_conversionHandlers;
/*   96 */   protected static boolean m_populated = false;
/*   97 */   protected static ArrayList<IndexerConversionHandler> m_conversionHandlerVect = new ArrayList();
/*      */ 
/*      */   public IndexerExecution()
/*      */   {
/*   44 */     this.m_maintainIndexProcess = false;
/*      */ 
/*   51 */     this.m_isCreateCollection = false;
/*      */ 
/*   64 */     this.m_bulkExe = null;
/*   65 */     this.m_isIndexerProcessStarted = false;
/*      */ 
/*   68 */     this.m_collectionDef = null;
/*      */ 
/*   77 */     this.m_haveWarnedUserAboutCollectionMismatch = false;
/*      */ 
/*   81 */     this.m_abort = false;
/*   82 */     this.m_doneIndexing = false;
/*   83 */     this.m_indexingFailed = false;
/*   84 */     this.m_countMismatch = false;
/*   85 */     this.m_bgExceptionMsg = null;
/*      */ 
/*   87 */     this.m_docCountCheck = false;
/*   88 */     this.m_storeIndexOnly = false;
/*      */ 
/*   93 */     this.m_conversionHandlers = new HashMap();
/*      */   }
/*      */ 
/*      */   public void init(IndexerWorkObject data)
/*      */     throws ServiceException
/*      */   {
/*  102 */     this.m_data = data;
/*  103 */     this.m_config = ((IndexerConfig)data.getCachedObject("IndexerConfig"));
/*  104 */     this.m_driver = ((IndexerDriverAdaptor)data.m_driver);
/*  105 */     this.m_indexer = ((DocIndexerAdaptor)data.m_indexer);
/*  106 */     this.m_handler = SearchIndexerUtils.createExecutionHandler(this.m_config, this);
/*      */ 
/*  108 */     this.m_collectionDef = data.m_collectionDef;
/*      */ 
/*  110 */     this.m_collections = this.m_config.getTable("CollectionID");
/*  111 */     if (this.m_collections.getNumRows() != 2)
/*      */     {
/*  113 */       Report.error(null, "", new DataException("csCmnSrchIndxCollectionIDError"));
/*      */     }
/*      */ 
/*  116 */     if (this.m_maintainIndexProcess)
/*      */     {
/*  118 */       this.m_isIndexerProcessStarted = false;
/*  119 */       Object exe = SharedObjects.getObject("searchIndexerObjects", "IndexerProcessBulkExe");
/*  120 */       if ((exe != null) && (exe instanceof Process))
/*      */       {
/*  122 */         this.m_bulkExe = ((Process)exe);
/*  123 */         this.m_isIndexerProcessStarted = true;
/*  124 */         this.m_semaphoreHandle = NumberUtils.parseInteger((String)SharedObjects.getObject("searchIndexerObjects", "IndexerSemaphoreHandle"), 0);
/*      */       }
/*      */ 
/*  127 */       this.m_config.setValue("isMaintainProcess", (this.m_maintainIndexProcess) ? "1" : "");
/*  128 */       this.m_config.setValue("isProcessStarted", (this.m_isIndexerProcessStarted) ? "1" : "");
/*      */       try
/*      */       {
/*  131 */         NativeOsUtils nou = new NativeOsUtils();
/*  132 */         this.m_nativeOsUtils = nou;
/*  133 */         if (!this.m_isIndexerProcessStarted)
/*      */         {
/*  135 */           String pid = "" + nou.getPid();
/*  136 */           Date dte = new Date();
/*  137 */           SimpleDateFormat frmt = new SimpleDateFormat("Hmmss");
/*  138 */           String tstamp = frmt.format(dte);
/*  139 */           this.m_indexerSemaphoreName = (pid + "_" + tstamp);
/*  140 */           this.m_currentPid = pid;
/*  141 */           this.m_config.setValue("semaphoreName", this.m_indexerSemaphoreName);
/*  142 */           this.m_config.setValue("currentPid", this.m_currentPid);
/*      */         }
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/*  147 */         throw new ServiceException("csUnableToCreateNativeUtils", t);
/*      */       }
/*      */     }
/*      */ 
/*  151 */     this.m_docCountCheck = this.m_config.getBoolean("UseIndexerDocCountCheck", this.m_docCountCheck);
/*      */   }
/*      */ 
/*      */   public void writeBatchFile(Vector list, Hashtable docProps) throws ServiceException
/*      */   {
/*  156 */     FileUtils.checkOrCreateDirectory(this.m_bulkDir, 1);
/*  157 */     if (!this.m_config.getBoolean("UseBulkloadFormat", true))
/*      */     {
/*  159 */       this.m_handler.writeBatchFile(list, docProps);
/*  160 */       return;
/*      */     }
/*  162 */     SimpleDateFormat frmt = new SimpleDateFormat("Hmmss.S");
/*  163 */     Date dte = new Date();
/*  164 */     String tstamp = frmt.format(dte);
/*  165 */     String bulkloadFileName = this.m_bulkDir + tstamp + ".txt";
/*      */ 
/*  167 */     File bFile = new File(bulkloadFileName);
/*  168 */     if (bFile.exists() == true)
/*      */     {
/*  170 */       bFile.delete();
/*      */     }
/*      */ 
/*  173 */     FileOutputStream fos = null;
/*  174 */     String encoding = this.m_config.getValue("IndexerEncoding");
/*      */     try
/*      */     {
/*  177 */       fos = new FileOutputStream(bFile);
/*      */ 
/*  182 */       this.m_bulkLoadSize = 0L;
/*  183 */       for (int i = 0; i < list.size(); ++i)
/*      */       {
/*  185 */         IndexerInfo ii = (IndexerInfo)list.elementAt(i);
/*  186 */         String key = (ii.m_encodedKey == null) ? ii.m_indexKey : ii.m_encodedKey;
/*  187 */         Properties prop = (Properties)docProps.get(key);
/*  188 */         if (prop == null) continue; if (prop.size() == 0)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  193 */         StringBuffer entries = new StringBuffer();
/*  194 */         for (Enumeration en = prop.keys(); en.hasMoreElements(); )
/*      */         {
/*  196 */           String propKey = (String)en.nextElement();
/*  197 */           String value = (String)prop.get(propKey);
/*  198 */           value = StringUtils.encodeLiteralStringEscapeSequence(value);
/*  199 */           entries.append(propKey);
/*  200 */           entries.append(": ");
/*  201 */           entries.append(value);
/*  202 */           entries.append("\n");
/*      */         }
/*  204 */         entries.append("<<EOD>>\n");
/*  205 */         String entriesStr = entries.toString();
/*  206 */         fos.write(entriesStr.getBytes(encoding));
/*  207 */         this.m_bulkLoadSize += ii.m_size;
/*      */       }
/*  209 */       fos.flush();
/*  210 */       this.m_bulkloadFileName = bulkloadFileName;
/*  211 */       this.m_config.setValue("IndexerBulkloadFile", bulkloadFileName);
/*  212 */       this.m_config.setValue("IndexerBatchTotalSize", "" + this.m_bulkLoadSize);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  222 */       FileUtils.closeObject(fos);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void prepareIndexDoc(Properties prop, IndexerInfo ii)
/*      */     throws DataException, ServiceException
/*      */   {
/*  229 */     this.m_handler.prepareIndexDoc(prop, ii);
/*      */   }
/*      */ 
/*      */   public boolean performConversion(Properties prop, IndexerInfo ii)
/*      */     throws DataException, ServiceException
/*      */   {
/*  235 */     boolean conversionPerformed = false;
/*  236 */     IndexerConversionHandler cHandler = getConversionHandler(prop);
/*  237 */     if (cHandler != null)
/*      */     {
/*  239 */       if (SystemUtils.m_verbose)
/*      */       {
/*  241 */         Report.trace("indexer", "doing conversion with " + cHandler + " on " + ii, null);
/*      */       }
/*      */ 
/*  244 */       cHandler.convertDocument(prop, ii, this.m_data);
/*  245 */       conversionPerformed = true;
/*      */     }
/*  249 */     else if (SystemUtils.m_verbose)
/*      */     {
/*  251 */       Report.debug("indexer", "Conversion is not performed", null);
/*      */     }
/*      */ 
/*  254 */     return conversionPerformed;
/*      */   }
/*      */ 
/*      */   public void waitForConversionHandlers()
/*      */   {
/*  267 */     for (IndexerConversionHandler handler : m_conversionHandlerVect)
/*      */     {
/*  269 */       Report.trace("indexer", "finishing with " + handler, null);
/*  270 */       handler.finish();
/*      */     }
/*  272 */     Report.trace("indexer", "all ConversionHandlers finished ", null);
/*      */   }
/*      */ 
/*      */   protected IndexerConversionHandler getConversionHandler(Properties prop)
/*      */     throws DataException, ServiceException
/*      */   {
/*  278 */     String key = prop.getProperty("indexerConversionHandler");
/*  279 */     IndexerConversionHandler cHandler = null;
/*  280 */     if (key == null)
/*      */     {
/*  282 */       String useMapStr = prop.getProperty("indexerUseMap");
/*  283 */       boolean useMap = StringUtils.convertToBool(useMapStr, false);
/*  284 */       if (useMap)
/*      */       {
/*  286 */         key = SearchIndexerUtils.getConversionEngineName(this.m_data);
/*      */       }
/*      */     }
/*  289 */     if (key != null)
/*      */     {
/*  293 */       key = prop.getProperty("dFormat");
/*  294 */       boolean handlerFound = false;
/*      */ 
/*  296 */       populateConversionHandlers();
/*      */ 
/*  299 */       for (IndexerConversionHandler handler : m_conversionHandlerVect)
/*      */       {
/*  301 */         handlerFound = handler.IsFormatSupported(prop);
/*  302 */         if (handlerFound)
/*      */         {
/*  304 */           cHandler = handler;
/*  305 */           break;
/*      */         }
/*      */       }
/*      */ 
/*  309 */       if (handlerFound)
/*      */       {
/*      */         try
/*      */         {
/*  313 */           Report.trace("indexer", "initializing conversionHandler " + cHandler, null);
/*  314 */           cHandler.init(this.m_data, this);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  318 */           Report.error(null, "!csTextConversionHandlerInitError", e);
/*      */         }
/*      */       }
/*      */     }
/*  322 */     prop.remove("indexerConversionHandler");
/*  323 */     return cHandler;
/*      */   }
/*      */ 
/*      */   protected void populateConversionHandlers()
/*      */   {
/*  332 */     if (!m_populated)
/*      */     {
/*  335 */       DataResultSet conversionHandlers = SharedObjects.getTable("ConversionHandlers");
/*  336 */       boolean done = false;
/*      */       while (true)
/*      */       {
/*  341 */         if (conversionHandlers.getNumRows() == 0)
/*      */         {
/*  343 */           done = true;
/*      */         }
/*      */ 
/*  346 */         if (done)
/*      */         {
/*  351 */           conversionHandlers = SharedObjects.getTable("DefaultConversionHandler");
/*      */         }
/*  353 */         Vector row = conversionHandlers.getCurrentRowValues();
/*  354 */         String key = (String)row.get(0);
/*  355 */         String className = (String)row.get(1);
/*      */         try
/*      */         {
/*  358 */           IndexerConversionHandler cHandler = (IndexerConversionHandler)ComponentClassFactory.createClassInstance(key, className, "csIndexerUnableToInstantiateConversionHandler");
/*      */ 
/*  360 */           m_conversionHandlerVect.add(cHandler);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  364 */           Report.error("indexer", "Error instantiating Conversion Handler<" + key + "> with classname:<" + className + ">", e);
/*      */         }
/*  366 */         if (done) {
/*      */           break;
/*      */         }
/*      */ 
/*  370 */         done = !conversionHandlers.next();
/*      */       }
/*      */     }
/*  373 */     m_populated = true;
/*      */   }
/*      */ 
/*      */   public Properties prepare(Hashtable infos)
/*      */     throws ServiceException
/*      */   {
/*  380 */     Properties props = new Properties();
/*  381 */     if ((this.m_config.getBoolean("UseCustomPrepare", false)) && 
/*  383 */       (this.m_handler.prepare(infos, props) == 0))
/*      */     {
/*  385 */       return props;
/*      */     }
/*      */ 
/*  389 */     String activeIndexRoot = ActiveIndexState.getActiveProperty("ActiveIndex");
/*  390 */     String activeIndex = activeIndexRoot;
/*  391 */     if (this.m_data.isRebuild())
/*      */     {
/*  393 */       activeIndex = getNextIndexerID(activeIndex);
/*  394 */       this.m_activeCollectionId = activeIndex;
/*      */ 
/*  396 */       this.m_activeCollectionDir = getCollectionPath(activeIndex);
/*  397 */       this.m_buildCollectionDir = this.m_data.getEnvironmentValue("IndexerRebuildStagingPath");
/*  398 */       if (this.m_buildCollectionDir == null)
/*      */       {
/*  400 */         this.m_buildCollectionDir = this.m_activeCollectionDir;
/*      */       }
/*      */       else
/*      */       {
/*  408 */         this.m_buildCollectionDir = FileUtils.directorySlashes(this.m_buildCollectionDir);
/*  409 */         this.m_buildCollectionDir += "collection/";
/*  410 */         FileUtils.checkOrCreateDirectory(this.m_buildCollectionDir, 1);
/*      */       }
/*  412 */       if (!this.m_data.isRestart())
/*      */       {
/*  415 */         this.m_isCreateCollection = true;
/*      */       }
/*  417 */       else if (!this.m_data.m_isAbort)
/*      */       {
/*  419 */         this.m_isCreateCollection = (!checkCollectionExistence(false, "!csCannotRestartRebuildCollectionMissing"));
/*      */ 
/*  421 */         if (this.m_isCreateCollection)
/*      */         {
/*  424 */           ResultSet rset = null;
/*  425 */           if (this.m_data.m_workspace != null)
/*      */           {
/*      */             try
/*      */             {
/*  429 */               rset = this.m_data.m_workspace.createResultSet("QhasDocumentIndexedInRebuild", this.m_driver.getIndexerStateBinder());
/*      */             }
/*      */             catch (DataException ignore)
/*      */             {
/*  434 */               if (SystemUtils.m_verbose)
/*      */               {
/*  436 */                 SystemUtils.dumpException("indexer", ignore);
/*      */               }
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  442 */           if ((this.m_data.m_workspace == null) || (rset == null) || (!rset.isEmpty()))
/*      */           {
/*  444 */             throw new ServiceException("csCannotRestartRebuildCollectionMissing");
/*      */           }
/*      */         }
/*      */       }
/*  448 */       this.m_collectionDef.m_fieldInfos = infos;
/*      */     }
/*      */     else
/*      */     {
/*  452 */       if (activeIndex == null)
/*      */       {
/*  454 */         activeIndex = getNextIndexerID(activeIndex);
/*      */       }
/*  456 */       this.m_activeCollectionId = activeIndex;
/*  457 */       this.m_activeCollectionDir = getCollectionPath(activeIndex);
/*  458 */       this.m_buildCollectionDir = this.m_activeCollectionDir;
/*      */     }
/*      */ 
/*  461 */     String extension = this.m_config.getConfigValue("IndexerCollectionSubDir");
/*  462 */     this.m_collectionDirectory = (this.m_buildCollectionDir + extension);
/*      */ 
/*  464 */     if ((!this.m_isCreateCollection) && (!this.m_data.m_isAbort) && (!checkCollectionExistence(false, null)))
/*      */     {
/*      */       try
/*      */       {
/*  469 */         boolean isCreateCollection = true;
/*  470 */         if (this.m_data.m_workspace != null)
/*      */         {
/*  472 */           ResultSet rset = this.m_data.m_workspace.createResultSet("QreleasedIndexedDocuments", this.m_driver.getIndexerStateBinder());
/*      */ 
/*  474 */           if (rset.isRowPresent())
/*      */           {
/*  476 */             String id = ResultSetUtils.getValue(rset, "dID");
/*  477 */             String docName = ResultSetUtils.getValue(rset, "dDocName");
/*  478 */             String msg = LocaleUtils.encodeMessage("csActiveCollectionIsMissing", null, id, docName);
/*      */ 
/*  481 */             checkCollectionExistence(true, msg);
/*  482 */             isCreateCollection = false;
/*      */           }
/*      */         }
/*      */ 
/*  486 */         if ((isCreateCollection) && (infos != null))
/*      */         {
/*  488 */           this.m_collectionDef.m_fieldInfos = infos;
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  493 */         throw new ServiceException(e);
/*      */       }
/*      */     }
/*  496 */     this.m_config.setValue("IndexerActiveCollectionID", this.m_activeCollectionId);
/*  497 */     this.m_config.setValue("IndexerActiveCollectionDir", this.m_activeCollectionDir);
/*  498 */     this.m_config.setValue("IndexerBuildCollectionDir", this.m_buildCollectionDir);
/*  499 */     this.m_config.setValue("IndexerCollectionDir", this.m_collectionDirectory);
/*      */ 
/*  501 */     String temp = this.m_buildCollectionDir.replace(LegacyDirectoryLocator.getSearchDirectory(), LegacyDirectoryLocator.getSystemBaseDirectory("binary") + "search/");
/*      */ 
/*  503 */     this.m_bulkDir = (temp + "bulkload/");
/*      */ 
/*  506 */     validateActiveIndexChoice(activeIndexRoot);
/*      */ 
/*  509 */     return props;
/*      */   }
/*      */ 
/*      */   public String getCollectionPath(String id) throws ServiceException
/*      */   {
/*  514 */     String searchDir = this.m_data.m_searchDir;
/*  515 */     String path = null;
/*      */     try
/*      */     {
/*  518 */       path = ResultSetUtils.findValue(this.m_collections, "IndexerLabel", id, "IndexerExtension");
/*  519 */       if (path == null)
/*      */       {
/*  522 */         String engineName = this.m_config.getValue("SearchIndexerEngineName");
/*  523 */         if ((!this.m_config.getBoolean("hasWarnedActiveIndexNotValid", false)) && ((
/*  526 */           (engineName == null) || ((!engineName.equalsIgnoreCase("DATABASE")) && (!engineName.equalsIgnoreCase("DATABASE.METADATA"))))))
/*      */         {
/*  529 */           Report.trace("indexer", "Warning: ActiveIndex '" + id + "' is not valid in indexer engine '" + engineName + "'. Please rebuild your collection", null);
/*      */ 
/*  532 */           Report.warning(null, null, "csActiveIndexNotValidInEngine", new Object[] { id, engineName });
/*  533 */           this.m_config.m_currentConfig.put("hasWarnedActiveIndexNotValid", "true");
/*      */         }
/*      */ 
/*  536 */         this.m_collections.first();
/*  537 */         FieldInfo fi = new FieldInfo();
/*  538 */         this.m_collections.getFieldInfo("IndexerExtension", fi);
/*  539 */         path = this.m_collections.getStringValue(fi.m_index);
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  544 */       Report.trace("indexer", null, e);
/*  545 */       path = id;
/*      */     }
/*      */ 
/*  548 */     if (path == null)
/*      */     {
/*  550 */       if (FileUtils.checkFile(id, false, true) == 0)
/*      */       {
/*  554 */         String pathName = FileUtils.directorySlashes(id); break label299:
/*      */       }
/*      */ 
/*  558 */       Report.trace("indexer", "unable to find the collection directory for '" + id + "'.", null);
/*      */ 
/*  561 */       String msg = LocaleUtils.encodeMessage("csIndexerUnableToFindCollection", null, id);
/*      */ 
/*  563 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  568 */     String pathName = searchDir + path;
/*      */ 
/*  570 */     label299: return FileUtils.directorySlashes(pathName);
/*      */   }
/*      */ 
/*      */   protected String getNextIndexerID(String id)
/*      */     throws ServiceException
/*      */   {
/*  590 */     return CollectionHandlerUtils.getActiveIndex(this.m_data.isRebuild(), this.m_collections);
/*      */   }
/*      */ 
/*      */   protected void validateActiveIndexChoice(String activeIndex)
/*      */     throws ServiceException
/*      */   {
/*  596 */     DataBinder d = new DataBinder();
/*  597 */     ActiveIndexState.serializeActiveStateIndexData(d, false);
/*      */ 
/*  599 */     String curIndex = d.getLocal("ActiveIndex");
/*  600 */     if ((curIndex == activeIndex) || (
/*  602 */       (curIndex != null) && (activeIndex != null) && (curIndex.equals(activeIndex))))
/*      */       return;
/*  604 */     if (curIndex == null)
/*      */     {
/*  606 */       curIndex = "<null>";
/*      */     }
/*  608 */     if (activeIndex == null)
/*      */     {
/*  610 */       activeIndex = "<null>";
/*      */     }
/*  612 */     String msg = LocaleUtils.encodeMessage("csActiveIndexMismatch", null, activeIndex, curIndex);
/*  613 */     throw new ServiceException(msg);
/*      */   }
/*      */ 
/*      */   protected void createCollection(String id)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  626 */       File dir = FileUtilsCfgBuilder.getCfgFile(this.m_buildCollectionDir, "Search", true);
/*  627 */       if (!dir.exists())
/*      */       {
/*  629 */         FileUtils.checkOrCreateDirectory(this.m_buildCollectionDir, 0);
/*      */       }
/*  638 */       else if ((this.m_data.isRebuild()) && (!this.m_data.isRestart()))
/*      */       {
/*  640 */         FileUtils.deleteDirectory(dir, false);
/*  641 */         String[] dirList = dir.list();
/*  642 */         if ((dirList != null) && (dirList.length > 0))
/*      */         {
/*  644 */           throw new ServiceException(LocaleUtils.encodeMessage("csIndexerUnableToDeleteDirectory", null, this.m_buildCollectionDir + dirList[0]));
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  650 */       this.m_handler.createCollection(id);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  654 */       throw new ServiceException(LocaleUtils.encodeMessage("csIndexerUnableToDeleteCollection", e.getMessage()));
/*      */     }
/*      */   }
/*      */ 
/*      */   public void executeIndexer(Vector list, Hashtable props)
/*      */     throws ServiceException
/*      */   {
/*  661 */     if (!this.m_config.getBoolean("IsSupportDefaultExec", false))
/*      */     {
/*  663 */       this.m_handler.executeIndexer(list, props);
/*      */     }
/*      */     else
/*      */     {
/*  667 */       this.m_indexerInfoList = list;
/*  668 */       IndexerInfo ii = (IndexerInfo)list.elementAt(0);
/*  669 */       if (ii != null)
/*      */       {
/*  671 */         this.m_isDelete = ii.m_isDelete;
/*      */       }
/*  673 */       executeIndexer();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void executeIndexer() throws ServiceException
/*      */   {
/*  679 */     this.m_indexingFailed = false;
/*  680 */     boolean deleteBeforeUpdate = this.m_config.getBoolean("IsDeleteBeforeUpdate", false);
/*  681 */     if ((!this.m_isDelete) && (deleteBeforeUpdate))
/*      */     {
/*  683 */       this.m_isDelete = true;
/*  684 */       executeIndexer();
/*  685 */       this.m_isDelete = false;
/*      */     }
/*      */     String msg;
/*      */     String msg;
/*  694 */     if (this.m_isDelete)
/*      */     {
/*  696 */       msg = LocaleResources.getString("csIndexerDeletingItems", this.m_data, this.m_config.getValue("IndexerBulkloadFile"));
/*      */     }
/*      */     else
/*      */     {
/*  701 */       msg = LocaleResources.getString("csIndexerAddingItems", this.m_data, this.m_config.getValue("IndexerBulkloadFile"));
/*      */     }
/*      */ 
/*  705 */     Report.trace("indexer", msg, null);
/*  706 */     checkAndCleanupWorkDir();
/*      */ 
/*  708 */     DataResultSet drset = new DataResultSet(new String[] { "key", "value", "removeAfterWard" });
/*  709 */     Vector row = new IdcVector();
/*  710 */     row.addElement("isDelete");
/*  711 */     row.addElement("" + this.m_isDelete);
/*  712 */     row.addElement("true");
/*  713 */     drset.addRow(row);
/*      */ 
/*  715 */     row = new IdcVector();
/*  716 */     row.addElement("IndexerDebugLevel");
/*  717 */     row.addElement(this.m_data.m_debugLevel);
/*  718 */     row.addElement("false");
/*  719 */     drset.addRow(row);
/*  720 */     Vector cmdLine = VerityIndexUtils.prepareCommandLine("IndexerCommandLine", !this.m_maintainIndexProcess, drset, this.m_config);
/*      */ 
/*  722 */     TaskInfo ti = new TaskInfo("Verity_Indexer", cmdLine, null);
/*  723 */     ti.m_traceSubject = "indexer";
/*  724 */     long bulkloadSize = NumberUtils.parseLong(this.m_config.getValue("IndexerBatchTotalSize"), 0L);
/*  725 */     ti.m_timeout = getTotalTimeAllowed(bulkloadSize);
/*  726 */     TaskMonitor.addToQueue(ti);
/*  727 */     int index = 0;
/*  728 */     synchronized (ti)
/*      */     {
/*  730 */       while (!ti.m_isFinished)
/*      */       {
/*  732 */         if (this.m_abort)
/*      */         {
/*  734 */           TaskMonitor.stop("Verity_Indexer");
/*  735 */           checkForAbort();
/*      */         }
/*      */         try
/*      */         {
/*  739 */           ti.wait(5000L);
/*      */         }
/*      */         catch (InterruptedException e)
/*      */         {
/*  743 */           Report.debug("indexer", null, e);
/*      */         }
/*  745 */         index = setTimeout(index, ti);
/*      */       }
/*      */     }
/*  748 */     TaskMonitor.stop("Verity_Indexer");
/*      */ 
/*  750 */     int len = this.m_indexerInfoList.size();
/*  751 */     if ((ti.m_status == TaskInfo.STATUS.FAILURE) || (ti.m_status == TaskInfo.STATUS.TIMEOUT))
/*      */     {
/*  753 */       this.m_indexingFailed = true;
/*      */     }
/*      */     else
/*      */     {
/*  757 */       int count = parseResults(ti.m_output);
/*  758 */       this.m_countMismatch = ((!this.m_isDelete) && (count != len));
/*  759 */       if ((this.m_docCountCheck) && (this.m_countMismatch))
/*      */       {
/*  761 */         this.m_indexingFailed = true;
/*      */       }
/*      */     }
/*      */ 
/*  765 */     if (this.m_indexingFailed)
/*      */     {
/*  769 */       FileUtils.deleteFile(this.m_collectionDirectory + "/trans/data.trn");
/*      */ 
/*  771 */       if (ti.m_status == TaskInfo.STATUS.TIMEOUT)
/*      */       {
/*  773 */         Report.trace("indexer", LocaleResources.getString("csIndexerTimeout", this.m_data), null);
/*      */       }
/*  778 */       else if (this.m_countMismatch)
/*      */       {
/*  780 */         Report.trace("indexer", LocaleResources.getString("csIndexerPartialFailure", this.m_data), null);
/*      */       }
/*      */       else
/*      */       {
/*  785 */         Report.trace("indexer", LocaleResources.getString("csIndexerUnexpectedAbort", this.m_data), null);
/*      */       }
/*      */ 
/*  789 */       for (int i = 0; i < len; ++i)
/*      */       {
/*  791 */         IndexerInfo info = (IndexerInfo)this.m_indexerInfoList.elementAt(i);
/*  792 */         info.m_indexStatus = ((ti.m_status == TaskInfo.STATUS.TIMEOUT) ? 2 : 3);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  798 */       for (int i = 0; i < len; ++i)
/*      */       {
/*  800 */         IndexerInfo info = (IndexerInfo)this.m_indexerInfoList.elementAt(i);
/*  801 */         info.m_indexStatus = 0;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public int setTimeout(int scannedOutputOffset, TaskInfo info)
/*      */   {
/*  808 */     DataResultSet interestingText = SharedObjects.getTable("VerityContextTimeoutText");
/*      */ 
/*  810 */     if (interestingText == null)
/*      */     {
/*  812 */       Report.trace("indexer", "VerityContextTimeoutText table is missing.", null);
/*  813 */       return scannedOutputOffset;
/*      */     }
/*      */     try
/*      */     {
/*  817 */       FieldInfo[] infos = ResultSetUtils.createInfoList(interestingText, new String[] { "lcIndexerEngine", "lcIndexerLocale", "lcTextPattern", "lcTimeoutAdjustmentRule" }, true);
/*      */ 
/*  820 */       int newIndex = -1;
/*  821 */       String matchingText = null;
/*  822 */       String matchingRule = null;
/*  823 */       String myEngine = SharedObjects.getEnvironmentValue("SearchIndexerEngineName");
/*  824 */       String myLocale = this.m_config.getValue("IndexerLocale");
/*  825 */       interestingText.first();
/*  826 */       while ((newIndex == -1) && (interestingText.isRowPresent()))
/*      */       {
/*  829 */         Vector row = interestingText.getRowValues(interestingText.getCurrentRow());
/*      */ 
/*  831 */         String engine = (String)row.elementAt(infos[0].m_index);
/*  832 */         String locale = (String)row.elementAt(infos[1].m_index);
/*  833 */         matchingText = (String)row.elementAt(infos[2].m_index);
/*  834 */         matchingRule = (String)row.elementAt(infos[3].m_index);
/*  835 */         if ((engine.equalsIgnoreCase(myEngine)) && (locale.equalsIgnoreCase(myLocale)))
/*      */         {
/*  838 */           newIndex = info.m_output.indexOf(matchingText.toLowerCase(), scannedOutputOffset + 1);
/*      */         }
/*  827 */         interestingText.next();
/*      */       }
/*      */ 
/*  843 */       if (newIndex > -1)
/*      */       {
/*  845 */         scannedOutputOffset = newIndex;
/*  846 */         matchingRule = this.m_config.parseScriptValue(matchingRule);
/*  847 */         int timeDelta = NumberUtils.parseInteger(matchingRule, 0);
/*  848 */         info.m_timeout += timeDelta;
/*  849 */         if (SystemUtils.m_verbose)
/*      */         {
/*  851 */           Report.trace("indexer", "saw interesting text '" + matchingText + "', increasing timeAllowed by " + timeDelta / 1000.0D + " seconds.", null);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  859 */       Report.trace("indexer", "VerityContextTimeoutText table is malformed.", e);
/*      */     }
/*      */ 
/*  863 */     return scannedOutputOffset;
/*      */   }
/*      */ 
/*      */   protected int parseResults(String input)
/*      */     throws ServiceException
/*      */   {
/*  871 */     return this.m_handler.parseResults(input);
/*      */   }
/*      */ 
/*      */   protected long getTotalTimeAllowed(long size)
/*      */   {
/*  880 */     long unitTime = NumberUtils.parseLong(this.m_config.getValue("TimeoutPerOneMegInSec"), 30L);
/*  881 */     long timeAllowed = NumberUtils.parseLong(this.m_config.getValue("TimeoutForIndexingHousekeepingInSec"), 600L) * 1000L;
/*      */ 
/*  883 */     timeAllowed += (size / 1000000L + 1L) * unitTime * 1000L;
/*      */ 
/*  886 */     int extraTime = this.m_data.getEnvironmentInt("extraIndexingTimeout", 0);
/*  887 */     timeAllowed += extraTime;
/*      */ 
/*  889 */     return timeAllowed;
/*      */   }
/*      */ 
/*      */   public String getCollectionID()
/*      */   {
/*  895 */     return this.m_data.getEnvironmentValue("IDC_Name");
/*      */   }
/*      */ 
/*      */   public void saveProcessID(String processId)
/*      */   {
/*  900 */     this.m_data.saveIndexerProcessID(processId);
/*      */   }
/*      */ 
/*      */   public void abort()
/*      */   {
/*  905 */     this.m_abort = true;
/*      */   }
/*      */ 
/*      */   protected void checkForAbort() throws ServiceException
/*      */   {
/*  910 */     if (!this.m_abort)
/*      */       return;
/*  912 */     throw new ServiceException("!csIndexerAbort");
/*      */   }
/*      */ 
/*      */   protected void checkAndCleanupWorkDir()
/*      */   {
/*  918 */     if (this.m_config.getBoolean("DisableWorkDirCleanup", false))
/*      */     {
/*  920 */       return;
/*      */     }
/*  922 */     File f = FileUtilsCfgBuilder.getCfgFile(this.m_buildCollectionDir + "/intradocbasic/work", "Search", true);
/*  923 */     if (!f.exists())
/*      */       return;
/*  925 */     String[] files = f.list();
/*  926 */     if ((files == null) || (files.length <= 0)) {
/*      */       return;
/*      */     }
/*  929 */     Report.warning(null, null, "csIndexerNonEmptyWorkDir", new Object[0]);
/*  930 */     Report.trace("indexer", "Work directory (" + f.getPath() + ") is not empty.", null);
/*      */     try
/*      */     {
/*  933 */       FileUtils.deleteDirectory(f, false);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  937 */       Report.warning(null, e, "csIndexerUnableToCleanWorkDir", new Object[0]);
/*  938 */       Report.trace("indexer", null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void verifyCollection(IndexerCollectionData collectionDef)
/*      */     throws ServiceException
/*      */   {
/*  946 */     this.m_handler.verifyCollection(collectionDef);
/*      */   }
/*      */ 
/*      */   public void validateConfig() throws ServiceException
/*      */   {
/*  951 */     this.m_handler.validateConfig();
/*      */   }
/*      */ 
/*      */   public boolean checkCollectionExistence(boolean mustExist, String errMsg) throws ServiceException
/*      */   {
/*  956 */     return this.m_handler.checkCollectionExistence(mustExist, errMsg);
/*      */   }
/*      */ 
/*      */   public void cleanUp() throws ServiceException
/*      */   {
/*  961 */     if ((this.m_config.getBoolean("UseCustomCleanUp", false)) && 
/*  963 */       (this.m_handler.cleanUp() == 0))
/*      */     {
/*  965 */       return;
/*      */     }
/*      */ 
/*  970 */     for (Map.Entry entry : this.m_conversionHandlers.entrySet())
/*      */     {
/*  972 */       IndexerConversionHandler handler = (IndexerConversionHandler)entry.getValue();
/*  973 */       handler.cleanUp();
/*      */     }
/*      */ 
/*  976 */     if ((!this.m_data.isRebuild()) || 
/*  979 */       (!this.m_config.getBoolean("IndexerAutoSwitch", true)) || 
/*  982 */       (this.m_buildCollectionDir.equals(this.m_activeCollectionDir)))
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/*  997 */     String activePath = this.m_activeCollectionDir;
/*  998 */     if (activePath.endsWith("/"))
/*      */     {
/* 1000 */       activePath = activePath.substring(0, activePath.length() - 1);
/*      */     }
/* 1002 */     String backupPath = activePath + ".old";
/* 1003 */     File backupDir = FileUtilsCfgBuilder.getCfgFile(backupPath, "Search", true);
/* 1004 */     File activeDir = FileUtilsCfgBuilder.getCfgFile(activePath, "Search", true);
/* 1005 */     File buildDir = FileUtilsCfgBuilder.getCfgFile(this.m_buildCollectionDir, "Search", true);
/* 1006 */     FileUtils.deleteDirectory(backupDir, true);
/* 1007 */     FileUtils.renameFile(activePath, backupPath);
/* 1008 */     FileUtils.copyDirectoryWithFlags(buildDir, activeDir, 1, null, 1);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1019 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerExecution
 * JD-Core Version:    0.5.4
 */