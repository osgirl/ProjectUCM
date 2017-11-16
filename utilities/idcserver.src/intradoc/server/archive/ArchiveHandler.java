/*      */ package intradoc.server.archive;
/*      */ 
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.HashVector;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.filter.PurgerInterface;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.filestore.BaseFileHelper;
/*      */ import intradoc.filestore.BaseFileStore;
/*      */ import intradoc.filestore.FilePurgerFactory;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.FileStoreProviderHelper;
/*      */ import intradoc.filestore.FileStoreProviderLoader;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.indexer.WebChange;
/*      */ import intradoc.indexer.WebChanges;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.serialize.DataBinderLocalizer;
/*      */ import intradoc.server.DataUtils;
/*      */ import intradoc.server.IndexerReplication;
/*      */ import intradoc.server.MonikerWatcher;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.SerializeTable;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.ServiceManager;
/*      */ import intradoc.server.SubjectManager;
/*      */ import intradoc.server.ValueMapData;
/*      */ import intradoc.shared.AdditionalRenditions;
/*      */ import intradoc.shared.CollectionData;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.DocumentPathBuilder;
/*      */ import intradoc.shared.ExportQueryData;
/*      */ import intradoc.shared.LegacyDocumentPathBuilder;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Calendar;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Observable;
/*      */ import java.util.Properties;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ArchiveHandler
/*      */ {
/*      */   protected Workspace m_workspace;
/*      */   protected FileStoreProvider m_fileStore;
/*      */   protected FileStoreProviderHelper m_fileUtils;
/*      */   protected ReportProgress m_progress;
/*      */   protected SerializeTable m_st;
/*      */   protected boolean m_isAuto;
/*      */   protected boolean m_isQueued;
/*      */   protected boolean m_isLastFile;
/*      */   protected boolean m_isAbort;
/*      */   protected boolean m_isMostRecentMatching;
/*      */   protected String m_statusMsg;
/*      */   protected String m_errorMsg;
/*      */   protected boolean m_isLogError;
/*      */   protected ExecutionContextAdaptor m_cxt;
/*      */   protected DataBinder m_binder;
/*      */   protected int m_errorCount;
/*      */   protected int m_maxErrorAllowed;
/*      */   protected int m_maxBatchRows;
/*      */   protected CollectionData m_collection;
/*      */   protected String m_archiveName;
/*      */   protected String m_archiveDir;
/*      */   protected String m_archiveExportDir;
/*      */   protected DataBinder m_archiveData;
/*      */   protected ExportQueryData m_queryData;
/*      */   protected Properties m_updatedData;
/*      */   protected Date m_archiveDate;
/*      */   protected long m_lastModified;
/*      */   protected ArchiveExportHelper m_exportHelper;
/*      */   protected ArchiveExportStateInformation m_exportState;
/*      */   protected ArchiveImportStateInformation m_importState;
/*      */   protected String m_curContentDir;
/*      */   protected boolean m_isBatchTableUpToDate;
/*   95 */   protected static int m_dirCounter = 0;
/*   96 */   protected static final Object SYNC_OBJ = new Object();
/*      */   protected ArchiveTableHelper m_archiveTable;
/*      */   protected DataResultSet m_extraExportAdditionRevInfos;
/*      */   protected Hashtable m_valueMapBackups;
/*      */   protected Map m_indexerChangeMap;
/*      */ 
/*      */   public ArchiveHandler()
/*      */   {
/*   44 */     this.m_workspace = null;
/*   45 */     this.m_fileStore = null;
/*   46 */     this.m_fileUtils = null;
/*   47 */     this.m_progress = null;
/*   48 */     this.m_st = null;
/*      */ 
/*   50 */     this.m_isAuto = false;
/*   51 */     this.m_isQueued = false;
/*   52 */     this.m_isLastFile = false;
/*   53 */     this.m_isAbort = false;
/*   54 */     this.m_isMostRecentMatching = false;
/*      */ 
/*   57 */     this.m_statusMsg = null;
/*   58 */     this.m_errorMsg = null;
/*   59 */     this.m_isLogError = true;
/*      */ 
/*   62 */     this.m_cxt = null;
/*      */ 
/*   65 */     this.m_binder = null;
/*      */ 
/*   68 */     this.m_errorCount = 0;
/*   69 */     this.m_maxErrorAllowed = 50;
/*   70 */     this.m_maxBatchRows = 1000;
/*      */ 
/*   73 */     this.m_collection = null;
/*   74 */     this.m_archiveName = null;
/*   75 */     this.m_archiveDir = null;
/*   76 */     this.m_archiveExportDir = null;
/*   77 */     this.m_archiveData = null;
/*   78 */     this.m_queryData = null;
/*      */ 
/*   81 */     this.m_updatedData = null;
/*      */ 
/*   84 */     this.m_archiveDate = null;
/*   85 */     this.m_lastModified = -2L;
/*      */ 
/*   88 */     this.m_exportHelper = null;
/*   89 */     this.m_exportState = null;
/*   90 */     this.m_importState = null;
/*      */ 
/*   93 */     this.m_curContentDir = null;
/*   94 */     this.m_isBatchTableUpToDate = false;
/*      */ 
/*   99 */     this.m_archiveTable = null;
/*      */ 
/*  105 */     this.m_extraExportAdditionRevInfos = null;
/*      */ 
/*  110 */     this.m_valueMapBackups = null;
/*      */ 
/*  114 */     this.m_indexerChangeMap = null;
/*      */   }
/*      */ 
/*      */   public void initObjects(Workspace ws, ReportProgress rp, Observable obs) throws DataException, ServiceException
/*      */   {
/*  119 */     this.m_workspace = ws;
/*  120 */     this.m_progress = rp;
/*      */ 
/*  122 */     this.m_updatedData = new Properties();
/*  123 */     this.m_st = new SerializeTable(this.m_workspace, this.m_progress);
/*  124 */     this.m_st.initNonModifiableFields();
/*  125 */     this.m_st.setErrorInfo("archiver", true);
/*      */ 
/*  127 */     this.m_cxt = new ExecutionContextAdaptor();
/*  128 */     this.m_cxt.setCachedObject("UserDateFormat", LocaleResources.m_odbcFormat);
/*      */ 
/*  130 */     this.m_fileStore = FileStoreProviderLoader.initFileStore(this.m_cxt);
/*  131 */     this.m_fileUtils = FileStoreProviderHelper.getFileStoreProviderUtils(this.m_fileStore, this.m_cxt);
/*      */ 
/*  134 */     this.m_maxErrorAllowed = SharedObjects.getEnvironmentInt("MaxArchiveErrorsAllowed", 50);
/*      */ 
/*  136 */     this.m_archiveTable = new ArchiveTableHelper(this, ws, rp, this.m_cxt, this.m_st);
/*      */ 
/*  138 */     this.m_maxBatchRows = SharedObjects.getEnvironmentInt("MaxArchiveDocumentBatch", 1000);
/*      */ 
/*  140 */     this.m_indexerChangeMap = new HashMap();
/*  141 */     if (!this.m_progress instanceof IndexerReplication)
/*      */       return;
/*  143 */     IndexerReplication iReplication = (IndexerReplication)this.m_progress;
/*  144 */     WebChanges webChanges = iReplication.getWebChanges();
/*  145 */     WebChange[] allChanges = webChanges.allChanges();
/*  146 */     for (int i = 0; i < allChanges.length; ++i)
/*      */     {
/*  148 */       WebChange wChange = allChanges[i];
/*  149 */       this.m_indexerChangeMap.put(wChange.m_dID, "" + wChange.m_change);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void init(DataBinder binder, boolean isExport)
/*      */     throws ServiceException, DataException
/*      */   {
/*  156 */     this.m_binder = binder;
/*      */ 
/*  160 */     this.m_collection = ArchiveUtils.getCollection(this.m_binder);
/*  161 */     this.m_archiveName = this.m_binder.get("aArchiveName");
/*  162 */     this.m_archiveDir = ArchiveUtils.buildArchiveDirectory(this.m_collection.m_location, this.m_archiveName);
/*  163 */     this.m_archiveExportDir = ArchiveUtils.buildArchiveDirectory(this.m_collection.m_exportLocation, this.m_archiveName);
/*      */ 
/*  165 */     buildMessageStubs(isExport);
/*      */ 
/*  168 */     this.m_archiveData = ArchiveUtils.readArchiveFile(this.m_collection.m_location, this.m_archiveName, true);
/*      */ 
/*  171 */     this.m_queryData = createQueryData(this.m_archiveData);
/*      */ 
/*  174 */     computeFlags(isExport);
/*      */ 
/*  177 */     this.m_archiveDate = new Date();
/*      */ 
/*  180 */     if (isExport)
/*      */     {
/*  182 */       this.m_exportState = new ArchiveExportStateInformation();
/*      */     }
/*      */     else
/*      */     {
/*  186 */       this.m_importState = ((ArchiveImportStateInformation)ComponentClassFactory.createClassInstance("ArchiveImportStateInformation", "intradoc.server.archive.ArchiveImportStateInformation", ""));
/*      */ 
/*  188 */       this.m_importState.init(this.m_binder, this.m_workspace, this.m_cxt);
/*      */     }
/*      */ 
/*  191 */     this.m_archiveTable.init(binder, this.m_collection, this.m_archiveName, this.m_archiveDir, this.m_archiveExportDir, this.m_archiveData, this.m_archiveDate);
/*      */ 
/*  193 */     initCachedObject();
/*      */ 
/*  195 */     PluginFilters.filter("afterArchiveHandlerInit", this.m_workspace, this.m_binder, this.m_cxt);
/*      */   }
/*      */ 
/*      */   public ExportQueryData createQueryData(DataBinder archiveData)
/*      */   {
/*  200 */     String exportQueryStr = archiveData.getLocal("aExportQuery");
/*  201 */     ExportQueryData queryData = new ExportQueryData();
/*  202 */     String wildcards = SharedObjects.getEnvironmentValue("DatabaseWildcards");
/*  203 */     if (wildcards != null)
/*      */     {
/*  205 */       queryData.setWildcards(wildcards);
/*      */     }
/*      */ 
/*  208 */     queryData.parse(exportQueryStr);
/*  209 */     return queryData;
/*      */   }
/*      */ 
/*      */   protected void initCachedObject()
/*      */   {
/*  217 */     this.m_cxt.setCachedObject("ArchiveHandler", this);
/*      */ 
/*  219 */     this.m_cxt.setCachedObject("DataBinder", this.m_binder);
/*  220 */     this.m_cxt.setCachedObject("Workspace", this.m_workspace);
/*  221 */     this.m_cxt.setCachedObject("collectionData", this.m_collection);
/*  222 */     this.m_cxt.setCachedObject("serializeTable", this.m_st);
/*      */ 
/*  224 */     this.m_cxt.setCachedObject("archivename", this.m_archiveName);
/*  225 */     this.m_cxt.setCachedObject("archiveDir", this.m_archiveDir);
/*  226 */     this.m_cxt.setCachedObject("archiveExportDir", this.m_archiveExportDir);
/*  227 */     this.m_cxt.setCachedObject("archiveData", this.m_archiveData);
/*      */ 
/*  229 */     if (this.m_exportState != null)
/*      */     {
/*  231 */       this.m_cxt.setCachedObject("exportStateInfo", this.m_exportState);
/*      */     }
/*  233 */     if (this.m_importState == null)
/*      */       return;
/*  235 */     this.m_cxt.setCachedObject("importStateInfo", this.m_importState);
/*      */   }
/*      */ 
/*      */   public void doArchiving(DataBinder binder, boolean isExport)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  242 */     String dateID = null;
/*  243 */     boolean isUpdateData = false;
/*      */     try
/*      */     {
/*  246 */       init(binder, isExport);
/*      */ 
/*  248 */       if ((!this.m_isAuto) || (this.m_isQueued))
/*      */       {
/*  250 */         String suffixMsg = (this.m_isQueued) ? "!csArchiveSuffixStartedQueuedImport" : "!csArchiveSuffixStarted";
/*  251 */         Report.appInfo("archiver", null, LocaleUtils.appendMessage(suffixMsg, this.m_statusMsg), null);
/*      */       }
/*      */ 
/*  254 */       if (isExport)
/*      */       {
/*  256 */         dateID = "aLastExport";
/*  257 */         adjustLastExportDate(dateID);
/*  258 */         doExport();
/*  259 */         isUpdateData = true;
/*  260 */         if (PluginFilters.filter("afterDoArchivingExport", this.m_workspace, this.m_binder, this.m_cxt) == -1)
/*      */         {
/*      */           String monikerString;
/*      */           return;
/*      */         }
/*      */       }
/*      */       else {
/*  267 */         dateID = "aLastImport";
/*  268 */         isUpdateData = doImport();
/*  269 */         boolean[] isUpdateDataParam = { isUpdateData };
/*  270 */         this.m_cxt.setCachedObject("isUpdateData", isUpdateDataParam);
/*  271 */         if (PluginFilters.filter("afterDoArchivingImport", this.m_workspace, this.m_binder, this.m_cxt) == -1) {
/*      */           String monikerString;
/*      */           return;
/*      */         }
/*  275 */         isUpdateData = isUpdateDataParam[0];
/*  276 */         Report.trace("archiver", "Imported " + this.m_importState.m_importCount + " items", null);
/*      */       }
/*      */ 
/*  279 */       if (this.m_progress != null)
/*      */       {
/*  281 */         String msg = LocaleUtils.encodeMessage("csArchiveSuffixFinishedTime", null, new Date());
/*      */ 
/*  283 */         msg = LocaleUtils.appendMessage(msg, this.m_statusMsg);
/*  284 */         this.m_progress.reportProgress(2, msg, -1.0F, -1.0F);
/*      */       }
/*      */ 
/*  287 */       if ((!this.m_isAuto) || (this.m_isQueued))
/*      */       {
/*      */         String msg;
/*  290 */         if (isExport)
/*      */         {
/*      */           String msg;
/*  292 */           if (DataBinderUtils.getBoolean(this.m_archiveData, "aExportTableOnly", false))
/*      */           {
/*  294 */             msg = LocaleUtils.encodeMessage("csFinishedTableExportSuccess", null);
/*      */           }
/*      */           else
/*      */           {
/*  298 */             msg = LocaleUtils.encodeMessage("csFinishedExportSuccess", null, this.m_updatedData.getProperty("aTotalLastExported"));
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  304 */           msg = LocaleUtils.encodeMessage("csFinishedImportSuccess", null, "" + this.m_importState.m_importCount);
/*      */         }
/*      */ 
/*  307 */         String msg = LocaleUtils.appendMessage(msg, this.m_statusMsg);
/*      */ 
/*  309 */         Report.appInfo("archiver", null, msg, null);
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       String monikerString;
/*      */       String errMsg;
/*      */       String errMsg;
/*  324 */       this.m_workspace.releaseConnection();
/*      */ 
/*  327 */       if (this.m_archiveData != null)
/*      */       {
/*  329 */         this.m_archiveData.cleanUpTempFiles();
/*      */       }
/*  331 */       if (this.m_binder != null)
/*      */       {
/*  333 */         this.m_binder.cleanUpTempFiles();
/*      */       }
/*      */ 
/*  337 */       if ((dateID != null) && (isUpdateData))
/*      */       {
/*  339 */         this.m_updatedData.put(dateID, LocaleUtils.formatODBC(this.m_archiveDate));
/*  340 */         updateArchiveData();
/*      */       }
/*      */ 
/*  343 */       if (this.m_collection != null)
/*      */       {
/*  345 */         String monikerString = this.m_collection.getMoniker();
/*  346 */         MonikerWatcher.notifyChanged(monikerString);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void adjustLastExportDate(String dateID)
/*      */     throws ServiceException
/*      */   {
/*  362 */     String lastExportDate = this.m_archiveData.getLocal(dateID);
/*      */ 
/*  364 */     if ((lastExportDate == null) || (lastExportDate.length() == 0))
/*      */     {
/*  366 */       return;
/*      */     }
/*      */ 
/*  369 */     int archiverSyncSeconds = SharedObjects.getEnvironmentInt("ArchiverSyncBackDateSeconds", 0);
/*      */ 
/*  371 */     if (archiverSyncSeconds == 0)
/*      */       return;
/*  373 */     this.m_archiveData.putLocal(dateID + "Original", lastExportDate);
/*      */ 
/*  375 */     Date exportBeginDate = LocaleUtils.parseODBC(lastExportDate);
/*  376 */     Calendar calendar = Calendar.getInstance();
/*  377 */     calendar.setTime(exportBeginDate);
/*  378 */     calendar.add(13, archiverSyncSeconds * -1);
/*      */ 
/*  380 */     exportBeginDate = calendar.getTime();
/*  381 */     lastExportDate = LocaleUtils.formatODBC(exportBeginDate);
/*      */ 
/*  383 */     if ((lastExportDate == null) || (lastExportDate.length() <= 0))
/*      */       return;
/*  385 */     this.m_archiveData.putLocal(dateID, lastExportDate);
/*      */   }
/*      */ 
/*      */   public void cancelArchiving()
/*      */   {
/*  392 */     this.m_isAbort = true;
/*      */   }
/*      */ 
/*      */   protected void checkForAbort()
/*      */     throws ServiceException
/*      */   {
/*  399 */     if ((this.m_isAuto) || (!this.m_isAbort))
/*      */       return;
/*  401 */     String msg = LocaleUtils.encodeMessage("csArchiveSuffixAbortedTime", null, new Date());
/*  402 */     throw new ServiceException(LocaleUtils.appendMessage(msg, this.m_statusMsg));
/*      */   }
/*      */ 
/*      */   protected void computeFlags(boolean isExport)
/*      */   {
/*  408 */     if (isExport)
/*      */     {
/*  410 */       this.m_isAuto = StringUtils.convertToBool(this.m_archiveData.getLocal("aIsAutomatedExport"), false);
/*      */     }
/*      */     else
/*      */     {
/*  415 */       this.m_isAuto = StringUtils.convertToBool(this.m_binder.getLocal("IsAutoImport"), false);
/*  416 */       this.m_isQueued = StringUtils.convertToBool(this.m_binder.getLocal("IsQueued"), false);
/*      */     }
/*      */ 
/*  419 */     this.m_isMostRecentMatching = StringUtils.convertToBool(this.m_queryData.getQueryProp("MostRecentMatching"), false);
/*      */ 
/*  422 */     if (!this.m_isAuto) {
/*      */       return;
/*      */     }
/*  425 */     this.m_maxErrorAllowed = SharedObjects.getEnvironmentInt("MaxAutoReplicationErrorsAllowed", 0);
/*      */ 
/*  428 */     if (!isExport)
/*      */       return;
/*  430 */     this.m_archiveData.putLocal("aDoReplace", "0");
/*      */   }
/*      */ 
/*      */   public void doExport()
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/*  445 */     reportProgressPercent(LocaleUtils.encodeMessage("csProgressStarted", null), -1.0F, -1.0F);
/*      */ 
/*  448 */     validateExportQuery();
/*      */ 
/*  452 */     DataResultSet batchTable = getBatchFilesData();
/*      */ 
/*  455 */     if (SharedObjects.getEnvValueAsBoolean("CleanUpInvalidExports", false) == true)
/*      */     {
/*  457 */       File directory = new File(this.m_archiveExportDir);
/*  458 */       File[] subdirs = directory.listFiles();
/*  459 */       deleteInvalidDirectories(subdirs, batchTable);
/*      */     }
/*      */ 
/*  463 */     if (StringUtils.convertToBool(this.m_archiveData.getLocal("aDoReplace"), false))
/*      */     {
/*  465 */       cleanUpBatch(batchTable);
/*      */     }
/*      */ 
/*  468 */     if (StringUtils.convertToBool(this.m_binder.getLocal("aDoExportTable"), true))
/*      */     {
/*  470 */       this.m_archiveTable.doExport();
/*      */     }
/*      */ 
/*  473 */     if (!DataBinderUtils.getBoolean(this.m_archiveData, "aExportTableOnly", false))
/*      */     {
/*  476 */       Vector exportedDocs = buildBatch(batchTable);
/*      */ 
/*  479 */       this.m_updatedData.put("aTotalLastExported", String.valueOf(exportedDocs.size()));
/*      */ 
/*  482 */       boolean isDeleteBatch = StringUtils.convertToBool(this.m_binder.getLocal("aDoDelete"), false);
/*      */ 
/*  484 */       if (isDeleteBatch)
/*      */       {
/*  486 */         deleteBatchedDocs(exportedDocs);
/*      */       }
/*      */ 
/*  489 */       checkForAbort();
/*      */     }
/*      */ 
/*  493 */     if (StringUtils.convertToBool(this.m_archiveData.getLocal("aExportUserConfig"), false))
/*      */     {
/*  495 */       this.m_st.serialize(this.m_archiveDir, "users.hda", "Users", null, true);
/*  496 */       this.m_updatedData.put("aUsersExportDate", LocaleUtils.formatODBC(this.m_archiveDate));
/*      */     }
/*      */ 
/*  499 */     if (!StringUtils.convertToBool(this.m_archiveData.getLocal("aExportDocConfig"), false))
/*      */       return;
/*  501 */     this.m_st.serialize(this.m_archiveDir, "doctypes.hda", "DocTypes", null, true);
/*  502 */     this.m_updatedData.put("aDocConfigExportDate", LocaleUtils.formatODBC(this.m_archiveDate));
/*      */   }
/*      */ 
/*      */   protected Vector buildBatch(DataResultSet batchTable)
/*      */     throws ServiceException, DataException
/*      */   {
/*  510 */     this.m_exportHelper = ((ArchiveExportHelper)ComponentClassFactory.createClassInstance("ArchiveExportHelper", "intradoc.server.archive.ArchiveExportHelper", ""));
/*      */ 
/*  514 */     this.m_exportHelper.prepareHelperSets(this.m_workspace, this.m_cxt);
/*      */ 
/*  516 */     Vector exportedDocs = new IdcVector();
/*  517 */     this.m_cxt.setCachedObject("exportedDocs", exportedDocs);
/*  518 */     this.m_exportState.m_masterSet = doExportQuery();
/*  519 */     if (PluginFilters.filter("prepareWriteBatchFiles", this.m_workspace, this.m_binder, this.m_cxt) == -1)
/*      */     {
/*  521 */       return exportedDocs;
/*      */     }
/*      */ 
/*  524 */     writeBatchFiles(exportedDocs, batchTable);
/*      */ 
/*  526 */     return exportedDocs;
/*      */   }
/*      */ 
/*      */   protected void writeBatchFiles(Vector exportedDocs, DataResultSet batchTable)
/*      */     throws DataException, ServiceException
/*      */   {
/*  532 */     int totalRows = this.m_exportState.m_masterSet.getNumRows();
/*  533 */     int totalPromotions = 0;
/*  534 */     if (this.m_extraExportAdditionRevInfos != null)
/*      */     {
/*  536 */       totalPromotions = this.m_extraExportAdditionRevInfos.getNumRows();
/*      */     }
/*  538 */     if ((totalRows == 0) && (totalPromotions == 0))
/*      */     {
/*  541 */       return;
/*      */     }
/*      */ 
/*  545 */     SimpleDateFormat frmt = new SimpleDateFormat("yyDHHmmss");
/*  546 */     frmt.setTimeZone(TimeZone.getDefault());
/*  547 */     String tstamp = frmt.format(this.m_archiveDate);
/*      */ 
/*  549 */     String tsDir = getTSDirName();
/*  550 */     this.m_curContentDir = (this.m_archiveExportDir + tsDir);
/*      */ 
/*  552 */     String tableName = "DocMetaDefinition";
/*  553 */     String metaFileName = tableName.toLowerCase() + ".hda";
/*      */ 
/*  556 */     if (this.m_isAuto)
/*      */     {
/*  558 */       this.m_exportState.m_isDeleteExport = StringUtils.convertToBool(this.m_binder.getLocal("aIsDeleteExport"), false);
/*      */     }
/*      */ 
/*  562 */     int numFiles = 1;
/*  563 */     boolean hasMetaFieldWritten = false;
/*  564 */     for (; this.m_exportState.m_masterSet.isRowPresent(); ++numFiles)
/*      */     {
/*  566 */       checkForAbort();
/*      */ 
/*  569 */       this.m_exportState.m_fileDirectory = (tsDir + "/" + numFiles);
/*  570 */       DataResultSet batchSet = createBatchResultSet(exportedDocs);
/*      */ 
/*  572 */       if ((batchSet == null) || (batchSet.isEmpty()))
/*      */         continue;
/*  574 */       if (!hasMetaFieldWritten)
/*      */       {
/*  576 */         writeMetaFieldData(tableName, metaFileName);
/*  577 */         hasMetaFieldWritten = true;
/*      */       }
/*      */ 
/*  580 */       boolean isLastFile = (!this.m_exportState.m_masterSet.isRowPresent()) && (totalPromotions == 0);
/*      */ 
/*  582 */       serializeExportFile(batchSet, numFiles, isLastFile, batchTable, tstamp, tsDir, metaFileName);
/*      */     }
/*      */ 
/*  586 */     if (totalPromotions <= 0)
/*      */       return;
/*  588 */     this.m_extraExportAdditionRevInfos.first();
/*  589 */     for (; this.m_extraExportAdditionRevInfos.isRowPresent(); ++numFiles)
/*      */     {
/*  591 */       checkForAbort();
/*      */ 
/*  594 */       this.m_exportState.m_fileDirectory = (tsDir + "/" + numFiles);
/*  595 */       DataResultSet batchSet = createPromoteBatchResultSet(exportedDocs);
/*      */ 
/*  597 */       if ((batchSet == null) || (batchSet.isEmpty()))
/*      */         continue;
/*  599 */       if (!hasMetaFieldWritten)
/*      */       {
/*  601 */         writeMetaFieldData(tableName, metaFileName);
/*  602 */         hasMetaFieldWritten = true;
/*      */       }
/*  604 */       boolean isLastFile = !this.m_extraExportAdditionRevInfos.isRowPresent();
/*      */ 
/*  606 */       serializeExportFile(batchSet, numFiles, isLastFile, batchTable, tstamp, tsDir, metaFileName);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void writeMetaFieldData(String tableName, String metaFileName)
/*      */     throws ServiceException, DataException
/*      */   {
/*  615 */     if (SystemUtils.m_verbose)
/*      */     {
/*  617 */       Report.trace("archiver", "Creating batch directory " + this.m_curContentDir, null);
/*      */     }
/*  619 */     FileUtils.checkOrCreateDirectory(this.m_curContentDir, 0);
/*      */ 
/*  622 */     if (SystemUtils.m_verbose)
/*      */     {
/*  624 */       Report.trace("archiver", "Exporting meta definition table: " + this.m_curContentDir + "/" + metaFileName, null);
/*      */     }
/*  626 */     this.m_st.serialize(this.m_curContentDir, metaFileName, tableName, null, true);
/*      */   }
/*      */ 
/*      */   protected DataResultSet createPromoteBatchResultSet(Vector exportedDocs)
/*      */     throws ServiceException
/*      */   {
/*  634 */     this.m_exportState.resetBatchInfo();
/*  635 */     DataResultSet batchSet = this.m_exportHelper.createBatchSet();
/*      */ 
/*  637 */     DataBinder binder = new DataBinder();
/*  638 */     while ((this.m_extraExportAdditionRevInfos.isRowPresent()) && (this.m_exportState.m_numBatched < this.m_maxBatchRows))
/*      */     {
/*  641 */       DataResultSet revInfoSet = new DataResultSet();
/*  642 */       revInfoSet.copyFieldInfo(this.m_extraExportAdditionRevInfos);
/*  643 */       revInfoSet.addRow(this.m_extraExportAdditionRevInfos.getCurrentRowValues());
/*  644 */       this.m_exportState.m_revInfoSet = revInfoSet;
/*      */ 
/*  646 */       String id = "";
/*      */       try
/*      */       {
/*  649 */         id = ResultSetUtils.getValue(revInfoSet, "dID");
/*  650 */         binder.putLocal("dID", id);
/*      */ 
/*  653 */         Vector newRow = revInfoSet.getCurrentRowValues();
/*      */ 
/*  655 */         int size = newRow.size();
/*  656 */         int newSize = batchSet.getNumFields();
/*  657 */         for (int i = size; i < newSize; ++i)
/*      */         {
/*  659 */           newRow.addElement("");
/*      */         }
/*  661 */         this.m_exportHelper.m_helperSet.addRow(newRow);
/*      */ 
/*  663 */         ResultSet rset = this.m_workspace.createResultSet("Qdocuments", binder);
/*  664 */         DataResultSet drset = new DataResultSet();
/*  665 */         drset.copy(rset);
/*  666 */         rset.closeInternals();
/*  667 */         this.m_exportState.m_docSet = drset;
/*      */ 
/*  669 */         this.m_cxt.setCachedObject("mergeInFileInfoBatchSet", batchSet);
/*  670 */         if (PluginFilters.filter("preMergeInFileInfoPromote", this.m_workspace, this.m_archiveData, this.m_cxt) != 0)
/*      */         {
/*  712 */           this.m_exportHelper.m_helperSet.removeAll();
/*      */         }
/*      */         else
/*      */         {
/*  676 */           mergeInFileInfo();
/*      */ 
/*  678 */           batchSet.merge("dID", this.m_exportHelper.m_helperSet, false);
/*  679 */           exportedDocs.addElement(id);
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*      */         String msgStr;
/*      */         String msgStr;
/*  685 */         if (batchSet.isRowPresent())
/*      */         {
/*  687 */           String docName = ResultSetUtils.getValue(revInfoSet, "dDocName");
/*      */           String msgStr;
/*  688 */           if (id != null)
/*      */           {
/*  690 */             msgStr = LocaleUtils.encodeMessage("csExportFailedForItemNameId", null, docName, id);
/*      */           }
/*      */           else
/*      */           {
/*  694 */             msgStr = LocaleUtils.encodeMessage("csExportFailedForItemName", null, docName);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*      */           String msgStr;
/*  697 */           if (id != null)
/*      */           {
/*  699 */             msgStr = LocaleUtils.encodeMessage("csExportFailedForItemId", null, id);
/*      */           }
/*      */           else
/*      */           {
/*  703 */             msgStr = "!csExportFailedForItem";
/*      */           }
/*      */         }
/*  706 */         incrementAndCheckErrorCount(e, msgStr);
/*      */       }
/*      */       finally
/*      */       {
/*  712 */         this.m_exportHelper.m_helperSet.removeAll();
/*      */       }
/*  639 */       this.m_extraExportAdditionRevInfos.next(); this.m_exportState.m_classExportCount += 1; this.m_exportState.m_numBatched += 1;
/*      */     }
/*      */ 
/*  716 */     return batchSet;
/*      */   }
/*      */ 
/*      */   protected void serializeExportFile(DataResultSet batchSet, int fileNum, boolean isLastFile, DataResultSet batchTable, String tstamp, String tsDir, String metaFileName)
/*      */     throws ServiceException, DataException
/*      */   {
/*  723 */     DataBinder curData = new DataBinder(true);
/*  724 */     curData.addResultSet("ExportResults", batchSet);
/*      */ 
/*  726 */     DataBinderLocalizer localizer = new DataBinderLocalizer(curData, this.m_cxt);
/*  727 */     int binderType = localizer.getType("date");
/*  728 */     localizer.localizeBinder(binderType);
/*      */ 
/*  734 */     if (this.m_exportState.m_isDeleteExport)
/*      */     {
/*  736 */       curData.putLocal("aIsDeleteExport", "1");
/*      */     }
/*      */ 
/*  741 */     if (this.m_isMostRecentMatching)
/*      */     {
/*  743 */       curData.putLocal("MostRecentMatching", "1");
/*      */     }
/*      */ 
/*  747 */     String numFilesStr = Integer.toString(fileNum);
/*  748 */     curData.putLocal("ContentFileNumber", numFilesStr);
/*  749 */     if (isLastFile)
/*      */     {
/*  752 */       curData.putLocal("IsLastFile", "1");
/*      */     }
/*      */ 
/*  755 */     String numRowsStr = String.valueOf(batchSet.getNumRows());
/*  756 */     curData.putLocal("NumRows", numRowsStr);
/*  757 */     this.m_binder.putLocal("aNumDocuments", numRowsStr);
/*      */ 
/*  759 */     PluginFilters.filter("beforeArchiveBatchFile", this.m_workspace, curData, this.m_cxt);
/*      */ 
/*  761 */     this.m_workspace.releaseConnection();
/*      */ 
/*  763 */     String fileName = tstamp + "~" + numFilesStr + ".hda";
/*  764 */     reportProgressPercent(LocaleUtils.encodeMessage("csProgressWritingBatchFiles", null, fileName), -1.0F, -1.0F);
/*      */ 
/*  766 */     if (SystemUtils.m_verbose)
/*      */     {
/*  768 */       Report.trace("archiver", "Serializing export file: " + this.m_curContentDir + "/" + fileName, null);
/*      */     }
/*  770 */     ResourceUtils.serializeDataBinder(this.m_curContentDir, fileName, curData, true, false);
/*      */ 
/*  774 */     batchTable = addBatchFile(tsDir + "/" + fileName, batchTable);
/*      */ 
/*  776 */     doArchiveHistory(batchSet, tsDir + "/" + fileName);
/*      */ 
/*  779 */     prepareContextForFilter(fileName, metaFileName, "" + fileNum);
/*  780 */     PluginFilters.filter("afterArchiveBatchFile", this.m_workspace, curData, this.m_cxt);
/*      */   }
/*      */ 
/*      */   protected void doArchiveHistory(DataResultSet dset, String batchFileName)
/*      */     throws DataException, ServiceException
/*      */   {
/*  789 */     DataBinder binder = new DataBinder();
/*  790 */     binder.addResultSet("ARCHIVE_HISTORY", dset);
/*      */ 
/*  792 */     DataUtils.computeActionDates(binder, 0L);
/*  793 */     String name = ArchiveUtils.buildLocation(this.m_collection.m_name, this.m_archiveName);
/*  794 */     binder.putLocal("dArchiveName", name);
/*      */ 
/*  796 */     binder.putLocal("dBatchFile", batchFileName);
/*  797 */     binder.setEnvironment(this.m_binder.getEnvironment());
/*      */ 
/*  800 */     int count = 0;
/*  801 */     int num = dset.getNumRows();
/*  802 */     for (dset.first(); dset.isRowPresent(); ++count)
/*      */     {
/*  804 */       this.m_workspace.execute("IarchiveHistory", binder);
/*  805 */       PluginFilters.filter("archiveHistoryItem", this.m_workspace, binder, this.m_cxt);
/*      */ 
/*  807 */       if (count % 50 == 0)
/*      */       {
/*  809 */         reportProgressPercent("!csProgressRecordingArchiveHistory", count, num);
/*  810 */         this.m_workspace.releaseConnection();
/*      */       }
/*  802 */       dset.next();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void validateExportQuery()
/*      */   {
/*  817 */     String exportQueryStr = this.m_archiveData.getLocal("aExportQuery");
/*  818 */     if (exportQueryStr != null) {
/*      */       return;
/*      */     }
/*  821 */     this.m_archiveData.putLocal("aExportQuery", "");
/*      */   }
/*      */ 
/*      */   protected DataResultSet doExportQuery()
/*      */     throws ServiceException, DataException
/*      */   {
/*  827 */     reportProgressPercent(LocaleUtils.encodeMessage("csProgressPerformingExportQuery", null), -1.0F, -1.0F);
/*      */ 
/*  829 */     DataResultSet rset = null;
/*      */ 
/*  832 */     if (PluginFilters.filter("prepareExportQuery", this.m_workspace, this.m_archiveData, this.m_cxt) != 0)
/*      */     {
/*  835 */       rset = (DataResultSet)this.m_archiveData.getResultSet("ExportQueryResults");
/*  836 */       if (rset == null)
/*      */       {
/*  838 */         throw new ServiceException("!csErrorPrepareExportQueryFilter");
/*      */       }
/*  840 */       return rset;
/*      */     }
/*      */ 
/*  844 */     boolean useExportDate = false;
/*  845 */     if (!this.m_isAuto)
/*      */     {
/*  847 */       useExportDate = StringUtils.convertToBool(this.m_queryData.getQueryProp("UseExportDate"), false);
/*      */     }
/*      */ 
/*  850 */     String lastExportDate = this.m_archiveData.getLocal("aLastExport");
/*      */ 
/*  852 */     boolean isAllowPublished = StringUtils.convertToBool(this.m_queryData.getQueryProp("AllowExportPublished"), false);
/*      */ 
/*  854 */     this.m_exportHelper.m_exportWhereClause = this.m_queryData.createQueryStringEx(useExportDate, lastExportDate, isAllowPublished, false);
/*      */ 
/*  857 */     String dataSource = this.m_binder.getLocal("dataSource");
/*  858 */     String extraWhereClause = this.m_binder.getLocal("extraWhereClause");
/*  859 */     rset = this.m_exportHelper.performQuery(this.m_workspace, dataSource, extraWhereClause, null);
/*      */ 
/*  861 */     if (rset == null)
/*      */     {
/*  863 */       throw new ServiceException(LocaleUtils.encodeMessage("csErrorDoingExportQuery", null, dataSource));
/*      */     }
/*      */ 
/*  866 */     return rset;
/*      */   }
/*      */ 
/*      */   protected DataResultSet createBatchResultSet(Vector exportedDocs)
/*      */     throws DataException, ServiceException
/*      */   {
/*  879 */     this.m_exportState.resetBatchInfo();
/*      */ 
/*  881 */     DataResultSet revClassSet = this.m_exportState.m_masterSet;
/*  882 */     DataBinder binder = new DataBinder();
/*  883 */     binder.addResultSet("RevisionClasses", revClassSet);
/*  884 */     if (revClassSet.isEmpty())
/*      */     {
/*  887 */       if (SystemUtils.m_verbose)
/*      */       {
/*  889 */         Report.trace("archiver", "No content to export", null);
/*      */       }
/*  891 */       return null;
/*      */     }
/*      */ 
/*  895 */     String dataSource = "RevisionInfo";
/*  896 */     if (this.m_isAuto)
/*      */     {
/*  898 */       if (this.m_exportState.m_isDeleteExport)
/*      */       {
/*  900 */         dataSource = "IndexerExportDeleteInfo";
/*      */       }
/*      */       else
/*      */       {
/*  904 */         dataSource = "IndexerExportInsertInfo";
/*      */       }
/*      */     }
/*  907 */     String exportInfoSql = this.m_exportHelper.createExportQuery(dataSource, "Revisions.dRevClassID=<$dRevClassID$>", "<$orderClause$>");
/*      */ 
/*  910 */     DataResultSet batchSet = this.m_exportHelper.createBatchSet();
/*      */ 
/*  912 */     int totalRows = revClassSet.getNumRows();
/*  913 */     int idIndex = this.m_exportHelper.m_idIndex;
/*      */ 
/*  915 */     DataBinder queryBinder = new DataBinder(SharedObjects.getSafeEnvironment());
/*  916 */     queryBinder.addResultSet("RevClassIDs", this.m_exportState.m_masterSet);
/*  917 */     queryBinder.putLocal("isPerClass", "1");
/*  918 */     queryBinder.putLocal("orderClause", " order by Revisions.dID");
/*      */ 
/*  920 */     while ((revClassSet.isRowPresent()) && (this.m_exportState.m_numBatched < this.m_maxBatchRows))
/*      */     {
/*  923 */       reportProgressPercent(LocaleUtils.encodeMessage("csProgressBuildingBatchFile", null), this.m_exportState.m_classExportCount, totalRows);
/*      */ 
/*  926 */       if (!retrieveRevInfo(exportInfoSql, queryBinder))
/*      */       {
/*  929 */         if (SystemUtils.m_verbose)
/*      */         {
/*  931 */           Report.trace("archiver", "No revisions found for dRevClassID " + revClassSet.getStringValueByName("dRevClassID"), null);
/*      */         }
/*  933 */         this.m_exportState.m_revInfoSet = null;
/*      */       }
/*      */       else
/*      */       {
/*  937 */         DataResultSet revInfoSet = this.m_exportState.m_revInfoSet;
/*  938 */         binder.addResultSet("RevisionInfo", revInfoSet);
/*      */ 
/*  940 */         FieldInfo[] fi = ResultSetUtils.createInfoList(revInfoSet, new String[] { "dStatus", "dOutDate" }, true);
/*      */ 
/*  943 */         for (; revInfoSet.isRowPresent(); this.m_exportState.m_numBatched += 1)
/*      */         {
/*  945 */           String id = "";
/*      */           try
/*      */           {
/*  948 */             String status = revInfoSet.getStringValue(fi[0].m_index);
/*  949 */             String outDate = revInfoSet.getStringValue(fi[1].m_index);
/*  950 */             if ((status.equalsIgnoreCase("EXPIRED")) && (outDate.length() == 0) && (this.m_isAuto))
/*      */             {
/*  954 */               if (SystemUtils.m_verbose)
/*      */               {
/*  956 */                 Report.trace("archiver", "Skipping expired document with dID = " + revInfoSet.getStringValue(idIndex), null);
/*      */               }
/*      */ 
/* 1046 */               this.m_exportHelper.m_helperSet.removeAll();
/*      */             }
/*      */             else
/*      */             {
/*  961 */               id = revInfoSet.getStringValue(idIndex);
/*  962 */               Vector newRow = null;
/*  963 */               boolean continueExport = true;
/*      */ 
/*  965 */               if ((this.m_isMostRecentMatching) && (this.m_exportState.m_isDeleteExport))
/*      */               {
/*  967 */                 continueExport = shouldContinueMostRecentMatchingDelete(revInfoSet);
/*      */               }
/*      */ 
/*  970 */               if (continueExport)
/*      */               {
/*  973 */                 newRow = revInfoSet.getCurrentRowValues();
/*      */ 
/*  975 */                 int size = newRow.size();
/*  976 */                 int newSize = batchSet.getNumFields();
/*  977 */                 for (int i = size; i < newSize; ++i)
/*      */                 {
/*  979 */                   newRow.addElement("");
/*      */                 }
/*  981 */                 this.m_exportHelper.m_helperSet.addRow(newRow);
/*      */ 
/*  983 */                 ResultSet rset = this.m_workspace.createResultSet("Qdocuments", binder);
/*  984 */                 if ((rset == null) || (rset.isEmpty()))
/*      */                 {
/*  986 */                   if (rset != null)
/*      */                   {
/*  988 */                     rset.closeInternals();
/*      */                   }
/*      */ 
/* 1046 */                   this.m_exportHelper.m_helperSet.removeAll();
/*      */                 }
/*      */                 else
/*      */                 {
/*  992 */                   DataResultSet drset = new DataResultSet();
/*  993 */                   drset.copy(rset);
/*  994 */                   rset.closeInternals();
/*  995 */                   this.m_exportState.m_docSet = drset;
/*      */ 
/*  997 */                   this.m_cxt.setCachedObject("mergeInFileInfoBatchSet", batchSet);
/*  998 */                   if (PluginFilters.filter("preMergeInFileInfo", this.m_workspace, this.m_archiveData, this.m_cxt) != 0)
/*      */                   {
/* 1046 */                     this.m_exportHelper.m_helperSet.removeAll();
/*      */                   }
/*      */                   else
/*      */                   {
/* 1004 */                     if (!this.m_exportState.m_isDeleteExport)
/*      */                     {
/* 1008 */                       mergeInFileInfo();
/*      */                     }
/*      */ 
/* 1011 */                     batchSet.merge("dID", this.m_exportHelper.m_helperSet, false);
/* 1012 */                     exportedDocs.addElement(id);
/*      */                   }
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/*      */           catch (Exception e)
/*      */           {
/*      */             String msgStr;
/*      */             String msgStr;
/* 1019 */             if (batchSet.isRowPresent())
/*      */             {
/* 1021 */               String docName = ResultSetUtils.getValue(revInfoSet, "dDocName");
/*      */               String msgStr;
/* 1022 */               if (id != null)
/*      */               {
/* 1024 */                 msgStr = LocaleUtils.encodeMessage("csExportFailedForItemNameId", null, docName, id);
/*      */               }
/*      */               else
/*      */               {
/* 1028 */                 msgStr = LocaleUtils.encodeMessage("csExportFailedForItemName", null, docName);
/*      */               }
/*      */             }
/*      */             else
/*      */             {
/*      */               String msgStr;
/* 1031 */               if (id != null)
/*      */               {
/* 1033 */                 msgStr = LocaleUtils.encodeMessage("csExportFailedForItemId", null, id);
/*      */               }
/*      */               else
/*      */               {
/* 1037 */                 msgStr = "!csExportFailedForItem";
/*      */               }
/*      */             }
/* 1040 */             incrementAndCheckErrorCount(e, msgStr);
/*      */           }
/*      */           finally
/*      */           {
/* 1046 */             this.m_exportHelper.m_helperSet.removeAll();
/*      */           }
/*  943 */           revInfoSet.next();
/*      */         }
/*      */ 
/* 1052 */         if ((this.m_exportState.m_revInfoSet != null) && (this.m_exportState.m_revInfoSet.isRowPresent()))
/*      */           break;
/*      */       }
/*  921 */       revClassSet.next(); this.m_exportState.m_classExportCount += 1;
/*      */     }
/*      */ 
/* 1057 */     return batchSet;
/*      */   }
/*      */ 
/*      */   protected boolean shouldContinueMostRecentMatchingDelete(DataResultSet revInfoSet)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1065 */     DataBinder tempBinder = new DataBinder();
/* 1066 */     tempBinder.putLocal("dRevClassID", ResultSetUtils.getValue(revInfoSet, "dRevClassID"));
/* 1067 */     String sql = this.m_exportHelper.createExportQuery("IndexerMostRecentReleasedInfo", "Revisions.dRevClassID=<$dRevClassID$>", "<$orderClause$>");
/*      */ 
/* 1069 */     tempBinder.putLocal("orderClause", " ORDER BY Revisions.dID DESC");
/* 1070 */     sql = this.m_exportHelper.prepareQuery(sql, tempBinder);
/* 1071 */     ResultSet rset = this.m_workspace.createResultSetSQL(sql);
/*      */ 
/* 1073 */     DataResultSet drset = new DataResultSet();
/* 1074 */     drset.copy(rset);
/*      */ 
/* 1076 */     if (drset.first())
/*      */     {
/* 1078 */       if (this.m_extraExportAdditionRevInfos == null)
/*      */       {
/* 1080 */         this.m_extraExportAdditionRevInfos = new DataResultSet();
/* 1081 */         this.m_extraExportAdditionRevInfos.copyFieldInfo(drset);
/*      */       }
/*      */ 
/* 1087 */       this.m_extraExportAdditionRevInfos.addRow(drset.getCurrentRowValues());
/* 1088 */       return false;
/*      */     }
/*      */ 
/* 1091 */     return true;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   protected boolean retrieveRevInfo(String exportInfoSql, String orderByClause)
/*      */     throws ServiceException
/*      */   {
/* 1101 */     if ((this.m_exportState.m_revInfoSet != null) && (this.m_exportState.m_revInfoSet.isRowPresent()))
/*      */     {
/* 1103 */       return true;
/*      */     }
/*      */ 
/* 1106 */     String errMsg = null;
/* 1107 */     String classId = null;
/* 1108 */     DataResultSet revInfoSet = null;
/*      */     ResultSet revInfo;
/*      */     try {
/* 1111 */       classId = this.m_exportState.getClassId();
/* 1112 */       String sql = exportInfoSql + classId + orderByClause;
/*      */ 
/* 1114 */       revInfo = this.m_workspace.createResultSetSQL(sql);
/* 1115 */       if ((revInfo == null) || (revInfo.isEmpty()))
/*      */       {
/* 1117 */         errMsg = LocaleUtils.encodeMessage("csNoRevisionForItemInClass", null, classId);
/* 1118 */         if (revInfo != null)
/*      */         {
/* 1120 */           revInfo.closeInternals();
/*      */         }
/* 1122 */         int i = 0;
/*      */         return i;
/*      */       }
/* 1125 */       revInfoSet = new DataResultSet();
/* 1126 */       revInfoSet.copyEx(revInfo, 0, true);
/* 1127 */       revInfo.closeInternals();
/*      */ 
/* 1129 */       this.m_exportState.m_revInfoSet = revInfoSet;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1134 */       incrementAndCheckErrorCount(e, LocaleUtils.encodeMessage("csExportRecordCreationFailed", null, classId));
/*      */ 
/* 1136 */       revInfo = 0;
/*      */ 
/* 1142 */       return revInfo;
/*      */     }
/*      */     finally
/*      */     {
/* 1140 */       if (errMsg != null)
/*      */       {
/* 1142 */         incrementAndCheckErrorCount(errMsg);
/*      */       }
/*      */     }
/* 1145 */     return true;
/*      */   }
/*      */ 
/*      */   protected boolean retrieveRevInfo(String exportInfoSql, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 1151 */     if ((this.m_exportState.m_revInfoSet != null) && (this.m_exportState.m_revInfoSet.isRowPresent()))
/*      */     {
/* 1153 */       return true;
/*      */     }
/*      */ 
/* 1156 */     String classId = null;
/* 1157 */     DataResultSet revInfoSet = null;
/*      */     try
/*      */     {
/* 1160 */       classId = this.m_exportState.getClassId();
/* 1161 */       String sql = this.m_exportHelper.prepareQuery(exportInfoSql, binder);
/*      */ 
/* 1163 */       ResultSet revInfo = this.m_workspace.createResultSetSQL(sql);
/* 1164 */       if ((revInfo == null) || (revInfo.isEmpty()))
/*      */       {
/* 1166 */         if (revInfo != null)
/*      */         {
/* 1168 */           revInfo.closeInternals();
/*      */         }
/* 1170 */         return false;
/*      */       }
/*      */ 
/* 1173 */       revInfoSet = new DataResultSet();
/* 1174 */       revInfoSet.copyEx(revInfo, 0, true);
/* 1175 */       revInfo.closeInternals();
/*      */ 
/* 1177 */       this.m_exportState.m_revInfoSet = revInfoSet;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1182 */       incrementAndCheckErrorCount(e, LocaleUtils.encodeMessage("csExportRecordCreationFailed", null, classId));
/*      */ 
/* 1184 */       return false;
/*      */     }
/* 1186 */     return true;
/*      */   }
/*      */ 
/*      */   protected void mergeInFileInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1210 */     DataResultSet docSet = this.m_exportState.m_docSet;
/* 1211 */     String tsDir = this.m_exportState.m_fileDirectory;
/*      */ 
/* 1213 */     DataResultSet batchSet = this.m_exportHelper.m_helperSet;
/* 1214 */     FieldInfo[] revInfoFields = this.m_exportHelper.m_revInfoFields;
/*      */ 
/* 1216 */     String dID = batchSet.getStringValue(revInfoFields[0].m_index);
/* 1217 */     String status = batchSet.getStringValue(revInfoFields[5].m_index);
/*      */ 
/* 1219 */     Properties revProps = batchSet.getCurrentRowProps();
/* 1220 */     DataBinder batchData = new DataBinder(SharedObjects.getSecureEnvironment());
/* 1221 */     batchData.setLocalData(revProps);
/* 1222 */     batchData.addResultSet("BatchSet", batchSet);
/*      */ 
/* 1224 */     String changeState = (String)this.m_indexerChangeMap.get(dID);
/* 1225 */     if (changeState != null)
/*      */     {
/* 1229 */       revProps.put("dReleaseState", changeState);
/*      */     }
/*      */ 
/* 1232 */     DataBinder docData = new DataBinder(SharedObjects.getSecureEnvironment());
/* 1233 */     docData.setLocalData(revProps);
/* 1234 */     docData.addResultSet("DocSet", docSet);
/*      */ 
/* 1236 */     String[] docFieldNames = { "dIsPrimary", "dIsWebFormat", "dExtension", "dOriginalName", "dFormat" };
/*      */ 
/* 1238 */     FieldInfo[] finfos = ResultSetUtils.createInfoList(docSet, docFieldNames, true);
/*      */ 
/* 1241 */     boolean isCopyWebDocs = StringUtils.convertToBool(this.m_archiveData.getLocal("aCopyWebDocuments"), false);
/*      */ 
/* 1244 */     for (docSet.first(); docSet.isRowPresent(); docSet.next())
/*      */     {
/* 1246 */       MergeInFileData data = new MergeInFileData(docSet, this.m_workspace, this.m_archiveData, this.m_cxt);
/* 1247 */       data.m_docFieldNames = docFieldNames;
/* 1248 */       data.m_docFieldInfos = finfos;
/* 1249 */       data.m_isCopyWebDocs = isCopyWebDocs;
/* 1250 */       data.m_batchSet = batchSet;
/* 1251 */       data.m_revInfoFields = revInfoFields;
/* 1252 */       data.m_batchData = batchData;
/* 1253 */       data.m_tsDir = tsDir;
/* 1254 */       data.m_docData = docData;
/* 1255 */       data.m_fileStore = this.m_fileStore;
/* 1256 */       data.m_fileUtils = this.m_fileUtils;
/* 1257 */       data.m_archiveDir = this.m_archiveDir;
/* 1258 */       data.m_archiveExportDir = this.m_archiveExportDir;
/*      */ 
/* 1260 */       ArchiveUtils.doMergeInFileInfo(data, dID, status);
/*      */     }
/*      */ 
/* 1264 */     if (isCopyWebDocs)
/*      */     {
/* 1267 */       computeAdditionalRenditions(batchSet, revInfoFields, tsDir, batchData);
/*      */     }
/*      */     else
/*      */     {
/* 1273 */       int rindex = this.m_exportHelper.m_additionalRenditionsOffset;
/* 1274 */       int maxRenditions = AdditionalRenditions.m_maxNum;
/* 1275 */       for (int i = 0; i < maxRenditions; ++i)
/*      */       {
/* 1277 */         if (rindex >= revInfoFields.length) {
/*      */           return;
/*      */         }
/*      */ 
/* 1281 */         int rSetIndex = revInfoFields[(rindex++)].m_index;
/* 1282 */         if (rSetIndex < 0)
/*      */           continue;
/* 1284 */         batchSet.setCurrentValue(rSetIndex, "");
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void computeAdditionalRenditions(DataResultSet batchSet, FieldInfo[] revInfoFields, String tsDir, DataBinder batchData)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1294 */     AdditionalRenditions renSet = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*      */ 
/* 1296 */     int maxRenditions = AdditionalRenditions.m_maxNum;
/*      */ 
/* 1298 */     String revlabel = batchSet.getStringValue(revInfoFields[6].m_index);
/* 1299 */     String docName = batchSet.getStringValue(revInfoFields[1].m_index);
/*      */ 
/* 1301 */     int rindex = this.m_exportHelper.m_additionalRenditionsOffset;
/* 1302 */     String oldExt = batchData.getLocal("dExtension");
/*      */     try
/*      */     {
/* 1305 */       for (int i = 0; i < maxRenditions; rindex += 2)
/*      */       {
/* 1307 */         if (revInfoFields[rindex].m_index >= 0)
/*      */         {
/* 1311 */           String renFlag = batchSet.getStringValue(revInfoFields[rindex].m_index);
/* 1312 */           if (renFlag != null) if (renFlag.trim().length() != 0)
/*      */             {
/* 1316 */               String rext = renSet.getExtension(renFlag);
/* 1317 */               String renName = DocumentPathBuilder.computeRenditionFilename(docName, renFlag, revlabel, rext);
/*      */ 
/* 1321 */               String rendPath = tsDir + "/weblayout/" + LegacyDocumentPathBuilder.computeWebDirPartialPath(batchData) + renName;
/*      */ 
/* 1333 */               String filterName = "handleAdditionalRendition-" + renFlag.toUpperCase();
/* 1334 */               Object[] handleParams = { batchSet, batchData, this.m_archiveExportDir, rendPath, revInfoFields, new Integer(revInfoFields[rindex].m_index), this.m_progress };
/*      */ 
/* 1337 */               this.m_cxt.setCachedObject(filterName + ":parameters", handleParams);
/* 1338 */               int ret = PluginFilters.filter(filterName, this.m_workspace, this.m_binder, this.m_cxt);
/* 1339 */               boolean filterDidArchive = false;
/* 1340 */               if (ret == -1)
/*      */               {
/* 1342 */                 String errMsg = LocaleResources.localizeMessage("!csFilterError," + filterName, null);
/* 1343 */                 throw new DataException(errMsg);
/*      */               }
/* 1345 */               if (ret == 1)
/*      */               {
/* 1347 */                 filterDidArchive = true;
/* 1348 */                 Object retobj = this.m_cxt.getReturnValue();
/* 1349 */                 if ((retobj != null) && (retobj instanceof String))
/*      */                 {
/* 1351 */                   rendPath = (String)retobj;
/*      */                 }
/*      */ 
/*      */               }
/*      */ 
/* 1356 */               if (!filterDidArchive)
/*      */               {
/* 1358 */                 batchData.putLocal("RenditionId", "rendition" + ":" + renFlag);
/*      */ 
/* 1360 */                 batchData.putLocal("dExtension", rext);
/* 1361 */                 IdcFileDescriptor d = this.m_fileStore.createDescriptor(batchData, null, this.m_cxt);
/*      */                 try
/*      */                 {
/* 1364 */                   if (SystemUtils.m_verbose)
/*      */                   {
/* 1366 */                     Report.trace("archiver", "Creating archived file: " + this.m_archiveExportDir + rendPath, null);
/*      */                   }
/* 1368 */                   this.m_fileStore.copyToLocalFile(d, new File(this.m_archiveExportDir + rendPath), null);
/*      */                 }
/*      */                 catch (IOException e)
/*      */                 {
/* 1372 */                   throw new ServiceException(e);
/*      */                 }
/*      */               }
/*      */ 
/* 1376 */               int pathIndex = rindex + 1;
/* 1377 */               batchSet.setCurrentValue(revInfoFields[pathIndex].m_index, rendPath);
/*      */ 
/* 1379 */               Object[] obj = { batchSet, batchData, this.m_archiveExportDir, rendPath, renFlag, this.m_progress };
/* 1380 */               this.m_cxt.setCachedObject("afterComputeAdditionalRenditions:parameters", obj);
/* 1381 */               PluginFilters.filter("afterComputeAdditionalRenditions", this.m_workspace, this.m_binder, this.m_cxt);
/*      */             }
/*      */         }
/* 1305 */         ++i;
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/* 1389 */       if (oldExt != null)
/*      */       {
/* 1391 */         batchData.putLocal("dExtension", oldExt);
/*      */       }
/*      */       else
/*      */       {
/* 1395 */         batchData.removeLocal("dExtension");
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void deleteBatchedDocs(Vector batchedDocs)
/*      */     throws ServiceException
/*      */   {
/* 1403 */     this.m_binder.putLocal("Action", "delete");
/* 1404 */     this.m_binder.putLocal("IsNative", "1");
/*      */ 
/* 1406 */     int count = batchedDocs.size();
/* 1407 */     for (int i = 0; i < count; ++i)
/*      */     {
/* 1409 */       reportProgressPercent("!csProgressDeletingContentItem", i, count);
/*      */       try
/*      */       {
/* 1413 */         String id = (String)batchedDocs.elementAt(i);
/* 1414 */         this.m_binder.putLocal("dID", id);
/* 1415 */         executeCommand("CHECKIN_ARCHIVE", this.m_binder);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1420 */         reportError(e, "");
/*      */         try
/*      */         {
/* 1423 */           cleanUpCommandEnd();
/*      */         }
/*      */         catch (DataException excep)
/*      */         {
/* 1427 */           SystemUtils.dumpException("system", excep);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void cleanUpBatch(DataResultSet batchTable)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1436 */     reportProgressPercent("!csCleaningUpBatchFiles", -1.0F, -1.0F);
/*      */ 
/* 1439 */     int fileIndex = ResultSetUtils.getIndexMustExist(batchTable, "aBatchFile");
/* 1440 */     while (!batchTable.isEmpty())
/*      */     {
/* 1442 */       String fileName = batchTable.getStringValue(fileIndex);
/* 1443 */       batchTable.deleteCurrentRow();
/*      */ 
/* 1445 */       deleteExportDirectory(fileName);
/*      */     }
/*      */ 
/* 1449 */     File exportsFile = FileUtilsCfgBuilder.getCfgFile(this.m_archiveDir + "exports.hda", "Archive", false);
/* 1450 */     if (exportsFile.exists())
/*      */     {
/* 1452 */       exportsFile.delete();
/*      */     }
/*      */ 
/* 1455 */     this.m_lastModified = -2L;
/*      */   }
/*      */ 
/*      */   protected void deleteInvalidDirectories(File[] subdirs, DataResultSet batchTable) throws IOException, DataException, ServiceException
/*      */   {
/* 1460 */     for (File dir : subdirs) {
/* 1461 */       boolean foundMatch = false;
/* 1462 */       int fileIndex = ResultSetUtils.getIndexMustExist(batchTable, "aBatchFile");
/* 1463 */       for (batchTable.first(); batchTable.isRowPresent(); batchTable.next())
/*      */       {
/* 1465 */         String fileName = batchTable.getStringValue(fileIndex);
/* 1466 */         int index = fileName.lastIndexOf(47);
/* 1467 */         String batchDir = null;
/* 1468 */         if (index > 0)
/*      */         {
/* 1470 */           batchDir = fileName.substring(0, index);
/*      */         }
/* 1472 */         if ((batchDir == null) || (!dir.isDirectory()) || 
/* 1474 */           (!batchDir.equals(dir.getName())))
/*      */           continue;
/* 1476 */         foundMatch = true;
/* 1477 */         break;
/*      */       }
/*      */ 
/* 1481 */       if (foundMatch)
/*      */         continue;
/* 1483 */       String dir1 = FileUtils.getAbsolutePath(this.m_archiveExportDir, dir.getName());
/* 1484 */       File d = new File(dir1);
/* 1485 */       FileUtils.deleteDirectory(d, true);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected DataResultSet getBatchFilesData()
/*      */     throws ServiceException
/*      */   {
/* 1492 */     return getBatchFilesDataEx(true);
/*      */   }
/*      */ 
/*      */   protected DataResultSet getBatchFilesDataEx(boolean isLock)
/*      */     throws ServiceException
/*      */   {
/* 1499 */     DataResultSet batchTable = null;
/* 1500 */     File bFile = FileUtilsCfgBuilder.getCfgFile(this.m_archiveDir + "exports.hda", "Archive", false);
/*      */ 
/* 1502 */     String lockDir = null;
/* 1503 */     if (isLock)
/*      */     {
/* 1505 */       lockDir = this.m_collection.m_location;
/*      */     }
/* 1507 */     if (bFile.exists())
/*      */     {
/* 1509 */       this.m_lastModified = bFile.lastModified();
/*      */ 
/* 1511 */       DataBinder batchData = ArchiveUtils.readExportsFile(this.m_archiveDir, lockDir);
/* 1512 */       batchTable = (DataResultSet)batchData.getResultSet("BatchFiles");
/* 1513 */       if (batchTable == null)
/*      */       {
/* 1515 */         batchTable = new DataResultSet(ArchiveUtils.BATCHFILE_COLUMNS);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1520 */       batchTable = new DataResultSet(ArchiveUtils.BATCHFILE_COLUMNS);
/* 1521 */       this.m_lastModified = -2L;
/*      */     }
/*      */ 
/* 1524 */     return batchTable;
/*      */   }
/*      */ 
/*      */   protected DataResultSet addBatchFile(String batchFileName, DataResultSet batchTable)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1530 */     return addBatchFile(batchFileName, batchTable, true);
/*      */   }
/*      */ 
/*      */   protected DataResultSet addBatchFile(String batchFileName, DataResultSet batchTable, boolean isDocBatch)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1537 */     FileUtils.reserveDirectory(this.m_archiveDir);
/*      */     try
/*      */     {
/* 1540 */       batchTable = getBatchFilesDataEx(false);
/*      */ 
/* 1542 */       if (!this.m_isBatchTableUpToDate)
/*      */       {
/* 1544 */         if (batchTable.getNumFields() != ArchiveUtils.BATCHFILE_COLUMNS.length)
/*      */         {
/* 1547 */           FieldInfo fi = new FieldInfo();
/* 1548 */           fi.m_name = ArchiveUtils.BATCHFILE_COLUMNS[4];
/* 1549 */           fi.m_index = 4;
/* 1550 */           fi.m_type = 1;
/*      */ 
/* 1552 */           Vector fields = new IdcVector();
/* 1553 */           fields.addElement(fi);
/* 1554 */           batchTable.mergeFieldsWithFlags(fields, 0);
/*      */         }
/* 1556 */         this.m_isBatchTableUpToDate = true;
/*      */       }
/*      */ 
/* 1559 */       this.m_binder.putLocal("aBatchFile", batchFileName);
/*      */ 
/* 1561 */       String aState = this.m_binder.getLocal("aState");
/* 1562 */       if (aState == null)
/*      */       {
/* 1564 */         this.m_binder.putLocal("aState", "NEW");
/*      */       }
/*      */ 
/* 1567 */       this.m_binder.putLocal("IDC_Name", SharedObjects.getEnvironmentValue("IDC_Name"));
/*      */ 
/* 1569 */       if (isDocBatch)
/*      */       {
/* 1571 */         this.m_binder.putLocal("aIsTableBatch", "0");
/*      */       }
/*      */       else
/*      */       {
/* 1575 */         this.m_binder.putLocal("aIsTableBatch", "1");
/*      */       }
/*      */ 
/* 1578 */       Vector newRow = batchTable.createRow(this.m_binder);
/* 1579 */       batchTable.addRow(newRow);
/*      */ 
/* 1581 */       DataBinder batchData = new DataBinder(true);
/* 1582 */       batchData.addResultSet("BatchFiles", batchTable);
/*      */ 
/* 1585 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1587 */         Report.trace("archiver", "Writing export file: " + this.m_archiveDir + "/exports.hda", null);
/*      */       }
/* 1589 */       this.m_lastModified = ArchiveUtils.writeExportsFile(this.m_archiveDir, batchData);
/*      */     }
/*      */     finally
/*      */     {
/* 1593 */       FileUtils.releaseDirectory(this.m_archiveDir);
/*      */     }
/*      */ 
/* 1596 */     return batchTable;
/*      */   }
/*      */ 
/*      */   protected void updateArchiveData()
/*      */     throws ServiceException
/*      */   {
/* 1605 */     FileUtils.reserveDirectory(this.m_archiveDir);
/*      */     try
/*      */     {
/* 1608 */       this.m_archiveData = ArchiveUtils.readArchiveFile(this.m_collection.m_location, this.m_archiveName, false);
/*      */ 
/* 1612 */       for (Enumeration en = this.m_updatedData.keys(); en.hasMoreElements(); )
/*      */       {
/* 1614 */         String key = (String)en.nextElement();
/* 1615 */         String value = this.m_updatedData.getProperty(key);
/* 1616 */         this.m_archiveData.putLocal(key, value);
/*      */       }
/* 1618 */       ArchiveUtils.writeArchiveFile(this.m_archiveDir, this.m_archiveData, false);
/*      */     }
/*      */     finally
/*      */     {
/* 1622 */       FileUtils.releaseDirectory(this.m_archiveDir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void executeCommand(String cmd, DataBinder binder) throws DataException, ServiceException
/*      */   {
/* 1628 */     Service service = ServiceManager.getInitializedService(cmd, binder, this.m_workspace);
/*      */ 
/* 1630 */     handleArchiveCommandStart(cmd, binder, service);
/*      */ 
/* 1632 */     service.doRequestInternal();
/*      */ 
/* 1637 */     cleanUpCommandEnd();
/*      */   }
/*      */ 
/*      */   protected void deleteExportDirectory(String filename)
/*      */   {
/* 1645 */     String batchDir = filename;
/*      */     try
/*      */     {
/* 1648 */       int index = filename.lastIndexOf(47);
/* 1649 */       if (index > 0)
/*      */       {
/* 1651 */         batchDir = filename.substring(0, index);
/*      */       }
/*      */ 
/* 1654 */       String dir = FileUtils.getAbsolutePath(this.m_archiveExportDir, batchDir);
/* 1655 */       reportProgressPercent(LocaleUtils.encodeMessage("csProgressCleaningUp", null, batchDir), -1.0F, -1.0F);
/*      */ 
/* 1657 */       File d = new File(dir);
/* 1658 */       if (d.exists())
/*      */       {
/* 1660 */         Report.trace("archiver", "Deleting directory " + d.getAbsolutePath(), null);
/* 1661 */         FilePurgerFactory pFactory = ((BaseFileStore)this.m_fileStore).m_fileHelper.m_purger;
/* 1662 */         PurgerInterface purger = pFactory.createPurger(this.m_cxt);
/* 1663 */         FileUtils.deleteDirectoryWithPurge(d, true, purger);
/*      */       }
/*      */       else
/*      */       {
/* 1667 */         Report.trace("archiver", "Unabe to delete directory(" + d.getAbsolutePath() + ". Directory does not exist.", null);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1674 */       reportError(e, LocaleUtils.encodeMessage("csExportDirectoryDeleteFailed", null, batchDir));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected Vector findBatchFileKeyInSet(DataResultSet rset, int colIndex, String batchFile)
/*      */   {
/* 1682 */     int numRows = rset.getNumRows();
/* 1683 */     for (int i = 0; i < numRows; ++i)
/*      */     {
/* 1685 */       Vector v = rset.getRowValues(i);
/* 1686 */       String colVal = (String)v.elementAt(colIndex);
/* 1687 */       if ((colVal == null) || 
/* 1689 */         (colVal.indexOf(batchFile) < 0))
/*      */         continue;
/* 1691 */       rset.setCurrentRow(i);
/* 1692 */       return v;
/*      */     }
/*      */ 
/* 1696 */     return null;
/*      */   }
/*      */ 
/*      */   protected void incrementAndCheckErrorCount(String msg)
/*      */     throws ServiceException
/*      */   {
/* 1704 */     ServiceException e = new ServiceException(msg);
/* 1705 */     incrementAndCheckErrorCount(e, "");
/*      */   }
/*      */ 
/*      */   protected void incrementAndCheckErrorCount(Exception e, String msg)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1713 */       Boolean reportError = Boolean.FALSE;
/* 1714 */       Object[] params = { e, msg, reportError };
/* 1715 */       boolean didIt = checkForAlternateArchiveExceptionHandling(params);
/* 1716 */       e = (Exception)params[0];
/* 1717 */       msg = (String)params[1];
/* 1718 */       reportError = (Boolean)params[2];
/*      */ 
/* 1720 */       if (!didIt)
/*      */       {
/* 1722 */         incrementAndCheckErrorCountStandard(e, msg);
/*      */       }
/* 1728 */       else if (reportError.booleanValue())
/*      */       {
/* 1730 */         reportError(e, msg);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException excep)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/* 1742 */         cleanUpCommandEnd();
/*      */       }
/*      */       catch (DataException excep)
/*      */       {
/* 1746 */         SystemUtils.dumpException("system", excep);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void incrementAndCheckErrorCountStandard(Exception e, String msg) throws ServiceException
/*      */   {
/* 1753 */     reportError(e, msg);
/*      */ 
/* 1755 */     if ((this.m_isAuto) && (this.m_importState != null) && (this.m_importState.m_isDeleteImport))
/*      */     {
/* 1758 */       return;
/*      */     }
/*      */ 
/* 1761 */     if (e instanceof ServiceException)
/*      */     {
/* 1763 */       ServiceException se = (ServiceException)e;
/* 1764 */       if ((se.m_errorCode == -33) && (this.m_isAuto))
/*      */       {
/* 1767 */         return;
/*      */       }
/*      */     }
/*      */ 
/* 1771 */     this.m_errorCount += 1;
/* 1772 */     if (this.m_errorCount < this.m_maxErrorAllowed)
/*      */       return;
/* 1774 */     throw new ServiceException("!csArchiveSuffixAbortingTooManyErrors");
/*      */   }
/*      */ 
/*      */   protected void reportErrorAndExit(String msg)
/*      */     throws ServiceException
/*      */   {
/* 1780 */     String errMsg = reportError(null, msg);
/* 1781 */     throw new ServiceException(errMsg);
/*      */   }
/*      */ 
/*      */   protected String reportError(Exception e, String msg)
/*      */   {
/* 1786 */     String errMsg = LocaleUtils.appendMessage(msg, this.m_errorMsg);
/* 1787 */     if (this.m_isLogError)
/*      */     {
/* 1789 */       if (this.m_isAuto)
/*      */       {
/* 1791 */         String autoMsg = "!csArchiveAutomatedErrorPrefix";
/* 1792 */         errMsg = LocaleUtils.appendMessage(errMsg, autoMsg);
/*      */       }
/* 1794 */       Report.appError("archiver", null, errMsg, e);
/*      */     }
/*      */     else
/*      */     {
/* 1798 */       if (e != null)
/*      */       {
/* 1800 */         IdcMessage idcMsg = IdcMessageFactory.lc(e);
/* 1801 */         errMsg = LocaleUtils.appendMessage(LocaleUtils.encodeMessage(idcMsg), errMsg);
/*      */       }
/* 1803 */       this.m_binder.putLocal("StatusMessageKey", errMsg);
/* 1804 */       this.m_binder.putLocal("StatusMessage", errMsg);
/*      */     }
/* 1806 */     return errMsg;
/*      */   }
/*      */ 
/*      */   protected void buildMessageStubs(boolean isExport)
/*      */   {
/* 1811 */     if (isExport)
/*      */     {
/* 1813 */       this.m_errorMsg = LocaleUtils.encodeMessage("csExportErrorForArchiveInCollection", null, this.m_archiveName, this.m_collection.m_name);
/*      */ 
/* 1815 */       this.m_statusMsg = LocaleUtils.encodeMessage("csExportingArchiveInCollection", null, this.m_archiveName, this.m_collection.m_name);
/*      */     }
/*      */     else
/*      */     {
/* 1820 */       this.m_errorMsg = LocaleUtils.encodeMessage("csImportErrorForArchiveInCollection", null, this.m_archiveName, this.m_collection.m_name);
/*      */ 
/* 1822 */       this.m_statusMsg = LocaleUtils.encodeMessage("csImportingArchiveInCollection", null, this.m_archiveName, this.m_collection.m_name);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void reportProgressPercent(String msg, float amtDone, float total)
/*      */     throws ServiceException
/*      */   {
/* 1832 */     checkForAbort();
/* 1833 */     if (this.m_progress == null)
/*      */       return;
/* 1835 */     String reportMsg = LocaleUtils.appendMessage(msg, this.m_statusMsg);
/* 1836 */     this.m_progress.reportProgress(1, reportMsg, amtDone, total);
/*      */   }
/*      */ 
/*      */   public boolean doImport()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1854 */     reportProgressPercent("!csProgressImportStarted", -1.0F, -1.0F);
/*      */ 
/* 1856 */     boolean hasDoneWork = false;
/* 1857 */     if (!this.m_isAuto)
/*      */     {
/* 1860 */       String errMsg = "!csUserConfigImportFailed";
/*      */       try
/*      */       {
/* 1863 */         boolean importUsers = StringUtils.convertToBool(this.m_binder.getLocal("aImportUsers"), false);
/*      */ 
/* 1865 */         if (importUsers)
/*      */         {
/* 1867 */           importUsers();
/* 1868 */           SubjectManager.refreshSubjectAll("users", this.m_binder, this.m_cxt);
/* 1869 */           SubjectManager.notifyChanged("users");
/* 1870 */           hasDoneWork = true;
/*      */         }
/*      */ 
/* 1873 */         errMsg = "!csContentConfigImportFailed";
/* 1874 */         boolean importDocTypes = StringUtils.convertToBool(this.m_binder.getLocal("aImportDocConfig"), false);
/*      */ 
/* 1876 */         if (importDocTypes)
/*      */         {
/* 1878 */           this.m_st.serialize(this.m_archiveDir, "doctypes.hda", "DocTypes", "dDocType", false);
/* 1879 */           SubjectManager.refreshSubjectAll("doctypes", this.m_binder, this.m_cxt);
/* 1880 */           SubjectManager.notifyChanged("doctypes");
/* 1881 */           hasDoneWork = true;
/*      */         }
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1886 */         if (e.m_errorCode == -16)
/*      */         {
/* 1888 */           errMsg = LocaleUtils.appendMessage(e.getMessage(), errMsg);
/*      */         }
/*      */         else
/*      */         {
/* 1892 */           errMsg = e.getMessage();
/*      */         }
/* 1894 */         throw new ServiceException(e.m_errorCode, errMsg);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1899 */     boolean importTables = (StringUtils.convertToBool(this.m_binder.getLocal("aImportTables"), false)) || (this.m_isAuto);
/*      */ 
/* 1901 */     boolean importDocs = (StringUtils.convertToBool(this.m_binder.getLocal("aImportDocuments"), false)) || (this.m_isAuto);
/*      */ 
/* 1904 */     String overrideRule = null;
/* 1905 */     if (importDocs)
/*      */     {
/* 1907 */       this.m_binder.setEnvironmentValue("doFileCopy", "1");
/*      */ 
/* 1910 */       this.m_importState.buildMaps(this.m_archiveData);
/*      */ 
/* 1913 */       overrideRule = this.m_archiveData.getLocal("aOverrideRule");
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1919 */       if ((importDocs) || (importTables))
/*      */       {
/* 1921 */         boolean isContinue = true;
/* 1922 */         while (isContinue)
/*      */         {
/* 1924 */           DataResultSet drset = getBatchFilesData();
/* 1925 */           this.m_workspace.releaseConnection();
/* 1926 */           if (drset == null) break; if (drset.isEmpty()) {
/*      */             break;
/*      */           }
/*      */ 
/* 1930 */           hasDoneWork = true;
/* 1931 */           isContinue = importExportFile(drset, overrideRule, importDocs, importTables);
/* 1932 */           this.m_importState.m_numProcessedBatchFiles += 1;
/*      */         }
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 1938 */       if (hasDoneWork)
/*      */       {
/* 1940 */         this.m_updatedData.put("aTotalLastImported", String.valueOf(this.m_importState.m_importCount));
/*      */       }
/*      */     }
/* 1943 */     return hasDoneWork;
/*      */   }
/*      */ 
/*      */   public void importDocument(boolean isLogError)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1950 */     this.m_isLogError = isLogError;
/* 1951 */     this.m_binder.setEnvironmentValue("doFileCopy", "1");
/*      */ 
/* 1954 */     this.m_importState.buildMaps(this.m_archiveData);
/*      */ 
/* 1956 */     DataResultSet batchSet = getBatchFilesData();
/* 1957 */     if ((batchSet == null) || (batchSet.isEmpty()))
/*      */     {
/* 1959 */       reportErrorAndExit("!csNoBatchFilesDefinedForArchive");
/*      */     }
/*      */ 
/* 1963 */     String batchFile = this.m_binder.getLocal("aBatchFile");
/* 1964 */     int fileIndex = ResultSetUtils.getIndexMustExist(batchSet, "aBatchFile");
/* 1965 */     Vector row = findBatchFileKeyInSet(batchSet, fileIndex, batchFile);
/* 1966 */     if (row == null)
/*      */     {
/* 1968 */       reportErrorAndExit(LocaleUtils.encodeMessage("csBatchFileNotFoundLikelyDeleted", null, batchFile));
/*      */     }
/*      */ 
/* 1974 */     String expBatchFile = ResultSetUtils.getValue(batchSet, "aBatchFile");
/* 1975 */     String[] batchInfo = ArchiveUtils.breakUpBatchPath(this.m_archiveExportDir, expBatchFile);
/*      */ 
/* 1977 */     DataBinder batchFilesData = ArchiveUtils.readBatchData(batchInfo[0], batchInfo[1]);
/* 1978 */     DataResultSet fileSet = (DataResultSet)batchFilesData.getResultSet("ExportResults");
/* 1979 */     if (fileSet == null)
/*      */     {
/* 1982 */       reportErrorAndExit(LocaleUtils.encodeMessage("csBatchFileMissingExportResults", null, batchFile));
/*      */     }
/*      */ 
/* 1986 */     FieldInfo info = new FieldInfo();
/* 1987 */     if (!fileSet.getFieldInfo("dID", info))
/*      */     {
/* 1990 */       reportErrorAndExit("!csIdFieldMissingInExportResults");
/*      */     }
/*      */ 
/* 1993 */     String id = this.m_binder.getLocal("dID");
/* 1994 */     row = fileSet.findRow(info.m_index, id);
/* 1995 */     if (row == null)
/*      */     {
/* 1997 */       reportErrorAndExit("!csContentItemHasBeenDeletedFromBatchFile");
/*      */     }
/*      */ 
/* 2001 */     String idcName = ResultSetUtils.getValue(batchSet, "IDC_Name");
/* 2002 */     String overrideRule = this.m_archiveData.getLocal("aOverrideRule");
/* 2003 */     if (!computeImportFlags(batchFilesData, overrideRule, idcName))
/*      */     {
/* 2006 */       reportErrorAndExit("csInvalidImportRule");
/*      */     }
/*      */ 
/* 2009 */     String useDRevClassID = this.m_archiveData.getLocal("aUseRevclassID");
/* 2010 */     if (useDRevClassID == null)
/*      */     {
/* 2012 */       useDRevClassID = "0";
/*      */     }
/* 2014 */     this.m_binder.putLocal("UseRevClassFromImport", useDRevClassID);
/*      */ 
/* 2016 */     String useDID = this.m_archiveData.getLocal("aUseDID");
/* 2017 */     if (useDID == null)
/*      */     {
/* 2019 */       useDID = "0";
/*      */     }
/* 2021 */     this.m_binder.putLocal("UseDIDFromImport", useDID);
/*      */ 
/* 2023 */     Vector docs = new IdcVector();
/* 2024 */     this.m_importState.preprocessDoc(fileSet, docs, batchInfo[0]);
/* 2025 */     this.m_cxt.setCachedObject("currentImportDocs", docs);
/* 2026 */     prepareContextForFilter(expBatchFile, null, batchFilesData.getLocal("ContentFileNumber"));
/* 2027 */     PluginFilters.filter("afterPrepareSingleDoc", this.m_workspace, batchFilesData, this.m_cxt);
/* 2028 */     processBatch(docs, true);
/*      */   }
/*      */ 
/*      */   public void importBatch() throws DataException, ServiceException
/*      */   {
/* 2033 */     DataResultSet batchFile = (DataResultSet)this.m_binder.getResultSet("BatchFile");
/* 2034 */     if (batchFile == null)
/*      */     {
/* 2036 */       throw new ServiceException(null, "csResultSetMissing", new Object[] { "BatchFile" });
/*      */     }
/* 2038 */     String tableArchive = ResultSetUtils.getValue(batchFile, "aIsTableBatch");
/* 2039 */     boolean importTable = StringUtils.convertToBool(tableArchive, false);
/* 2040 */     if (!importTable)
/*      */     {
/* 2042 */       this.m_binder.setEnvironmentValue("doFileCopy", "1");
/*      */ 
/* 2045 */       this.m_importState.buildMaps(this.m_archiveData);
/*      */ 
/* 2048 */       String overrideRule = this.m_archiveData.getLocal("aOverrideRule");
/*      */ 
/* 2050 */       this.m_workspace.releaseConnection();
/* 2051 */       if (batchFile.isRowPresent())
/*      */       {
/* 2053 */         importExportFile(batchFile, overrideRule, true, false);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 2058 */       String batchName = ResultSetUtils.getValue(batchFile, "aBatchFile");
/* 2059 */       this.m_archiveTable.doImportTable(batchName);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean importExportFile(DataResultSet batchSet, String overrideRule, boolean importDocs, boolean importTables)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2067 */     int fileIndex = ResultSetUtils.getIndexMustExist(batchSet, "aBatchFile");
/* 2068 */     int idcNameIndex = ResultSetUtils.getIndexMustExist(batchSet, "IDC_Name");
/* 2069 */     int isTableIndex = ResultSetUtils.getIndexMustExist(batchSet, "aIsTableBatch");
/*      */ 
/* 2071 */     String useDRevClassID = this.m_archiveData.getLocal("aUseRevclassID");
/* 2072 */     if (useDRevClassID == null)
/*      */     {
/* 2074 */       useDRevClassID = "0";
/*      */     }
/* 2076 */     this.m_binder.putLocal("UseRevClassFromImport", useDRevClassID);
/*      */ 
/* 2078 */     String useDID = this.m_archiveData.getLocal("aUseDID");
/* 2079 */     if (useDID == null)
/*      */     {
/* 2081 */       useDID = "0";
/*      */     }
/* 2083 */     this.m_binder.putLocal("UseDIDFromImport", useDID);
/*      */ 
/* 2086 */     DataResultSet sortedBatchSet = sortBatchSet(batchSet, fileIndex, isTableIndex);
/*      */ 
/* 2089 */     Vector allDocs = new IdcVector();
/*      */ 
/* 2092 */     this.m_importState.m_numBatchFiles = batchSet.getNumRows();
/* 2093 */     this.m_importState.m_numProcessedBatchFiles = 0;
/* 2094 */     boolean didTableWork = false;
/* 2095 */     boolean doPostTablePreDocFilter = false;
/* 2096 */     for (sortedBatchSet.first(); sortedBatchSet.isRowPresent(); sortedBatchSet.next())
/*      */     {
/* 2098 */       String fileName = sortedBatchSet.getStringValue(fileIndex);
/* 2099 */       String idcName = sortedBatchSet.getStringValue(idcNameIndex);
/* 2100 */       reportProgressPercent(LocaleUtils.encodeMessage("csProgressPreprocessing", null, fileName), this.m_importState.m_numProcessedBatchFiles, this.m_importState.m_numBatchFiles);
/*      */ 
/* 2103 */       boolean isTable = StringUtils.convertToBool(sortedBatchSet.getStringValue(isTableIndex), false);
/*      */ 
/* 2105 */       if ((isTable) && (importTables))
/*      */       {
/* 2107 */         this.m_archiveTable.doImportTable(fileName);
/* 2108 */         didTableWork = true;
/* 2109 */         doPostTablePreDocFilter = true;
/*      */       }
/* 2111 */       if ((!isTable) && (importDocs))
/*      */       {
/* 2113 */         if ((didTableWork) && (doPostTablePreDocFilter))
/*      */         {
/* 2115 */           PluginFilters.filter("postImportTablesPreImportDocs", this.m_workspace, this.m_binder, this.m_cxt);
/* 2116 */           doPostTablePreDocFilter = false;
/*      */         }
/* 2118 */         Vector docs = preprocessBatch(fileName, overrideRule, idcName);
/* 2119 */         if ((this.m_importState.m_isImportValidOnly) && (!this.m_isAuto))
/*      */         {
/* 2124 */           Object[] docPacket = new Object[3];
/* 2125 */           docPacket[0] = fileName;
/* 2126 */           docPacket[1] = idcName;
/* 2127 */           docPacket[2] = docs;
/* 2128 */           allDocs.addElement(docPacket);
/* 2129 */           continue;
/*      */         }
/*      */ 
/* 2132 */         processBatch(docs, false);
/*      */ 
/* 2135 */         String tableName = "DocMetaDefinition";
/* 2136 */         String metaFileName = tableName.toLowerCase() + ".hda";
/* 2137 */         prepareContextForFilter(fileName, metaFileName, null);
/* 2138 */         this.m_cxt.setCachedObject("currentImportDocs", docs);
/*      */ 
/* 2140 */         DataBinder binder = new DataBinder();
/* 2141 */         binder.setEnvironment(this.m_binder.getEnvironment());
/* 2142 */         PluginFilters.filter("afterImportBatch", this.m_workspace, binder, this.m_cxt);
/*      */       }
/*      */ 
/* 2145 */       this.m_importState.m_numProcessedBatchFiles += 1;
/*      */ 
/* 2149 */       if (!this.m_isAuto)
/*      */         continue;
/* 2151 */       cleanUpAfterImport(fileName);
/* 2152 */       return true;
/*      */     }
/*      */ 
/* 2156 */     if ((importDocs) && (this.m_importState.m_isImportValidOnly))
/*      */     {
/* 2159 */       if (this.m_importState.m_invalidOptions != null)
/*      */       {
/* 2161 */         reportOptionErrors();
/* 2162 */         reportError(null, "!csStoppedDueToInvalidOptions");
/*      */       }
/*      */       else
/*      */       {
/* 2167 */         int size = allDocs.size();
/* 2168 */         for (int i = 0; i < size; ++i)
/*      */         {
/* 2170 */           Object[] docPacket = (Object[])(Object[])allDocs.elementAt(i);
/* 2171 */           Vector docs = (Vector)docPacket[2];
/* 2172 */           processBatch(docs, true);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2177 */     if (didTableWork)
/*      */     {
/* 2179 */       PluginFilters.filter("postImportTables", this.m_workspace, this.m_binder, this.m_cxt);
/*      */     }
/*      */ 
/* 2182 */     return false;
/*      */   }
/*      */ 
/*      */   public DataResultSet sortBatchSet(DataResultSet batchSet, int fileIndex, int isTableIndex)
/*      */     throws DataException
/*      */   {
/* 2188 */     DataResultSet firstSort = new DataResultSet();
/* 2189 */     firstSort.copy(batchSet);
/* 2190 */     ResultSetUtils.sortResultSet(firstSort, new String[] { "IDC_Name" });
/*      */ 
/* 2192 */     DataResultSet drset = new DataResultSet();
/* 2193 */     drset.copyFieldInfo(batchSet);
/*      */ 
/* 2195 */     String dirDateFormat = "yy-MMM-dd_HH.mm.ss_SSS";
/* 2196 */     int len = dirDateFormat.length();
/*      */ 
/* 2198 */     String oldLocation = null;
/* 2199 */     List docRows = new ArrayList();
/* 2200 */     for (firstSort.first(); firstSort.isRowPresent(); firstSort.next())
/*      */     {
/* 2202 */       String loc = firstSort.getStringValue(fileIndex);
/* 2203 */       if (loc.length() > len)
/*      */       {
/* 2205 */         loc = loc.substring(0, len);
/*      */       }
/*      */ 
/* 2208 */       boolean isTable = StringUtils.convertToBool(firstSort.getStringValue(isTableIndex), false);
/*      */ 
/* 2211 */       Vector row = firstSort.getCurrentRowValues();
/* 2212 */       if ((oldLocation != null) && (!oldLocation.equals(loc)))
/*      */       {
/* 2215 */         for (int i = 0; i < docRows.size(); ++i)
/*      */         {
/* 2217 */           drset.addRow((Vector)docRows.get(i));
/*      */         }
/* 2219 */         docRows.clear();
/*      */       }
/* 2221 */       if (isTable)
/*      */       {
/* 2223 */         drset.addRow(row);
/*      */       }
/*      */       else
/*      */       {
/* 2227 */         docRows.add(row);
/*      */       }
/* 2229 */       oldLocation = loc;
/*      */     }
/*      */ 
/* 2233 */     for (int i = 0; i < docRows.size(); ++i)
/*      */     {
/* 2235 */       drset.addRow((Vector)docRows.get(i));
/*      */     }
/*      */ 
/* 2238 */     return drset;
/*      */   }
/*      */ 
/*      */   protected void reportOptionErrors()
/*      */   {
/* 2244 */     String error = "";
/* 2245 */     for (Enumeration en = this.m_importState.m_invalidOptions.keys(); en.hasMoreElements(); )
/*      */     {
/* 2247 */       String key = (String)en.nextElement();
/* 2248 */       HashVector values = (HashVector)this.m_importState.m_invalidOptions.get(key);
/* 2249 */       error = LocaleUtils.encodeMessage("csMissingOptionsForKey", error, key, values.toString());
/*      */     }
/* 2251 */     error = LocaleUtils.encodeMessage("csMissingOptions", error);
/* 2252 */     reportError(null, error);
/*      */   }
/*      */ 
/*      */   protected boolean computeImportFlags(DataBinder batchData, String overrideRule, String batchIdc)
/*      */     throws DataException
/*      */   {
/* 2291 */     this.m_cxt.setCachedObject("batchIdc", batchIdc);
/*      */ 
/* 2293 */     boolean isDeleteExport = StringUtils.convertToBool(batchData.getLocal("aIsDeleteExport"), false);
/*      */ 
/* 2295 */     if (this.m_isAuto)
/*      */     {
/* 2299 */       if (isDeleteExport)
/*      */       {
/* 2301 */         overrideRule = "deleteRev";
/*      */       }
/*      */       else
/*      */       {
/* 2305 */         overrideRule = "update";
/*      */       }
/*      */ 
/* 2309 */       String curUser = this.m_binder.getEnvironmentValue("REMOTE_USER");
/* 2310 */       if ((curUser == null) || (curUser.length() == 0))
/*      */       {
/* 2312 */         String importUser = this.m_archiveData.getLocal("aImportLogonUser");
/* 2313 */         if ((importUser == null) || (importUser.length() == 0))
/*      */         {
/* 2316 */           reportError(null, "!csUndefinedReplicationLoginUser");
/*      */         }
/*      */         else
/*      */         {
/* 2320 */           this.m_binder.setEnvironmentValue("REMOTE_USER", importUser);
/*      */ 
/* 2322 */           if (!SharedObjects.getEnvValueAsBoolean("DisableArchiveUserAdminRole", false))
/*      */           {
/* 2324 */             this.m_binder.setEnvironmentValue("EXTERNAL_ROLE", "admin");
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2331 */     if (overrideRule == null)
/*      */     {
/* 2334 */       overrideRule = "update";
/*      */     }
/*      */     else
/*      */     {
/* 2338 */       overrideRule = overrideRule.toLowerCase();
/*      */     }
/*      */ 
/* 2341 */     this.m_importState.m_useRevLabel = false;
/* 2342 */     if ((overrideRule.equalsIgnoreCase("update")) || (overrideRule.equalsIgnoreCase("insertRev")) || (overrideRule.equalsIgnoreCase("deleteRev")))
/*      */     {
/* 2346 */       this.m_importState.m_useRevLabel = true;
/*      */     }
/*      */ 
/* 2349 */     String rule = "update";
/* 2350 */     if (overrideRule.indexOf("insert") >= 0)
/*      */     {
/* 2352 */       rule = "insert";
/*      */     }
/* 2354 */     else if (overrideRule.indexOf("delete") >= 0)
/*      */     {
/* 2356 */       rule = "delete";
/*      */     }
/*      */ 
/* 2361 */     if ((isDeleteExport) && (!this.m_isAuto))
/*      */     {
/* 2363 */       if (rule.equals("update"))
/*      */       {
/* 2365 */         rule = "delete";
/*      */       }
/* 2367 */       else if (rule.equals("insert"))
/*      */       {
/* 2370 */         return false;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2375 */     this.m_importState.m_isDeleteImport = rule.equals("delete");
/*      */ 
/* 2378 */     String curIdc = SharedObjects.getEnvironmentValue("IDC_Name");
/* 2379 */     if (curIdc == null)
/*      */     {
/* 2381 */       throw new DataException("!csArchiverNoIdcName");
/*      */     }
/*      */ 
/* 2384 */     this.m_importState.m_isNativeImport = false;
/* 2385 */     if (rule.equals("update"))
/*      */     {
/* 2387 */       this.m_importState.m_isNativeImport = curIdc.equalsIgnoreCase(batchIdc);
/*      */     }
/*      */ 
/* 2391 */     this.m_importState.m_isImportValidOnly = StringUtils.convertToBool(this.m_archiveData.getLocal("aImportValidOnly"), false);
/*      */ 
/* 2395 */     String dateFormat = batchData.getLocal("blDateFormat");
/* 2396 */     if (dateFormat != null)
/*      */     {
/* 2399 */       this.m_importState.parseAndSetTranslateDate(StringUtils.convertToBool(this.m_archiveData.getLocal("aTranslateDate"), false), dateFormat);
/*      */     }
/*      */     else
/*      */     {
/* 2405 */       throw new DataException("!csArchiverDateUndetermined");
/*      */     }
/*      */ 
/* 2408 */     this.m_binder.putLocal("Action", rule);
/* 2409 */     this.m_binder.putLocal("IsNative", String.valueOf(this.m_importState.m_isNativeImport));
/*      */ 
/* 2412 */     boolean isMostRecentMatching = DataBinderUtils.getBoolean(batchData, "MostRecentMatching", false);
/* 2413 */     if (isMostRecentMatching)
/*      */     {
/* 2415 */       if (this.m_valueMapBackups == null)
/*      */       {
/* 2417 */         this.m_valueMapBackups = new Hashtable();
/*      */       }
/*      */ 
/* 2420 */       String[] fieldsToMap = { "dRevisionID", "dRevLabel" };
/* 2421 */       for (int i = 0; i < fieldsToMap.length; ++i)
/*      */       {
/* 2423 */         String field = fieldsToMap[i];
/* 2424 */         ValueMapData oldData = (ValueMapData)this.m_importState.m_valueMaps.get(field);
/*      */ 
/* 2426 */         if (oldData != null)
/*      */         {
/* 2428 */           this.m_valueMapBackups.put(field, oldData);
/*      */         }
/*      */         else
/*      */         {
/* 2432 */           this.m_valueMapBackups.put(field, "");
/*      */         }
/*      */ 
/* 2435 */         ValueMapData newData = null;
/* 2436 */         if (oldData != null)
/*      */         {
/* 2438 */           newData = new ValueMapData();
/* 2439 */           newData.setDynamicHtmlMerger(oldData.getDynamicHtmlMerger());
/* 2440 */           newData.m_defaultProps = new Properties(oldData.m_defaultProps);
/*      */ 
/* 2442 */           newData.m_scriptMaps = new Hashtable();
/* 2443 */           newData.m_scriptMaps.putAll(oldData.m_scriptMaps);
/*      */ 
/* 2445 */           newData.m_valueMaps = new Hashtable();
/* 2446 */           newData.m_valueMaps.putAll(oldData.m_valueMaps);
/*      */         }
/*      */ 
/* 2449 */         if (newData == null)
/*      */         {
/* 2451 */           newData = new ValueMapData();
/* 2452 */           newData.setDynamicHtmlMerger(new PageMerger(this.m_importState.m_propsWrapper, this.m_cxt));
/*      */         }
/*      */ 
/* 2455 */         this.m_importState.m_valueMaps.put(field, newData);
/* 2456 */         newData.addMap(field, true, null, "1");
/*      */       }
/*      */     }
/*      */ 
/* 2460 */     return true;
/*      */   }
/*      */ 
/*      */   protected Vector preprocessBatch(String batchFileName, String overrideRule, String idcName)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2473 */     Vector docRows = new IdcVector();
/*      */ 
/* 2475 */     String[] batchInfo = ArchiveUtils.breakUpBatchPath(this.m_archiveExportDir, batchFileName);
/*      */ 
/* 2477 */     DataBinder batchFilesData = ArchiveUtils.readBatchData(this.m_archiveExportDir, batchFileName);
/*      */ 
/* 2482 */     this.m_isLastFile = StringUtils.convertToBool(batchFilesData.getLocal("IsLastFile"), false);
/*      */ 
/* 2484 */     if (DataBinderUtils.getBoolean(batchFilesData, "isTableArchive", false))
/*      */     {
/* 2487 */       return docRows;
/*      */     }
/* 2489 */     DataResultSet fileSet = (DataResultSet)batchFilesData.getResultSet("ExportResults");
/* 2490 */     if (fileSet == null)
/*      */     {
/* 2492 */       reportError(null, LocaleUtils.encodeMessage("csBatchFileNotFound", null, batchFileName));
/* 2493 */       return docRows;
/*      */     }
/*      */ 
/* 2497 */     boolean isImportOK = computeImportFlags(batchFilesData, overrideRule, idcName);
/* 2498 */     if (!isImportOK)
/*      */     {
/* 2501 */       return docRows;
/*      */     }
/*      */ 
/* 2504 */     for (; fileSet.isRowPresent(); fileSet.next())
/*      */     {
/* 2506 */       checkForAbort();
/* 2507 */       this.m_importState.preprocessDoc(fileSet, docRows, batchInfo[0]);
/*      */     }
/*      */ 
/* 2511 */     String numFiles = batchFilesData.getLocal("ContentFileNumber");
/* 2512 */     String tableName = "DocMetaDefinition";
/* 2513 */     String metaFileName = tableName.toLowerCase() + ".hda";
/* 2514 */     prepareContextForFilter(batchFileName, metaFileName, numFiles);
/* 2515 */     this.m_cxt.setCachedObject("currentImportDocs", docRows);
/* 2516 */     PluginFilters.filter("afterPreprocessImportBatch", this.m_workspace, batchFilesData, this.m_cxt);
/* 2517 */     return docRows;
/*      */   }
/*      */ 
/*      */   protected void processBatch(Vector docs, boolean isUseDocCount)
/*      */     throws ServiceException
/*      */   {
/* 2523 */     DataBinder myBinder = new DataBinder(this.m_binder.getEnvironment());
/*      */ 
/* 2525 */     int size = docs.size();
/* 2526 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 2528 */       Properties props = (Properties)docs.elementAt(i);
/* 2529 */       String docName = props.getProperty("dDocName");
/* 2530 */       if (isUseDocCount)
/*      */       {
/* 2532 */         reportProgressPercent(LocaleUtils.encodeMessage("csProgressProcessingContentItem", null, docName), i, size);
/*      */       }
/*      */       else
/*      */       {
/* 2538 */         float amtDone = (this.m_importState.m_numProcessedBatchFiles + i / size) / this.m_importState.m_numBatchFiles;
/*      */ 
/* 2540 */         reportProgressPercent(LocaleUtils.encodeMessage("csProgressProcessingContentItem", null, docName), amtDone, 1.0F);
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/* 2546 */         myBinder.setLocalData(props);
/*      */ 
/* 2548 */         executeCommand("CHECKIN_ARCHIVE", myBinder);
/* 2549 */         this.m_importState.m_importCount += 1;
/* 2550 */         Object[] obj = { myBinder, docs, this.m_progress };
/* 2551 */         this.m_cxt.setCachedObject("processArchiverBatch:parameters", obj);
/* 2552 */         PluginFilters.filter("processArchiverBatch", this.m_workspace, this.m_binder, this.m_cxt);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 2556 */         incrementAndCheckErrorCount(e, "");
/*      */       }
/*      */       finally
/*      */       {
/* 2560 */         this.m_workspace.releaseConnection();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void cleanUpAfterImport(String filename)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2575 */     if (this.m_valueMapBackups != null)
/*      */     {
/* 2577 */       for (Enumeration e = this.m_valueMapBackups.keys(); e.hasMoreElements(); )
/*      */       {
/* 2579 */         String key = (String)e.nextElement();
/* 2580 */         Object value = this.m_valueMapBackups.get(key);
/*      */ 
/* 2582 */         if (!value instanceof ValueMapData)
/*      */         {
/* 2584 */           this.m_importState.m_valueMaps.remove(key);
/*      */         }
/*      */         else
/*      */         {
/* 2588 */           this.m_importState.m_valueMaps.put(key, value);
/*      */         }
/*      */       }
/* 2591 */       this.m_valueMapBackups = null;
/*      */     }
/*      */ 
/* 2594 */     if (!this.m_isAuto)
/*      */     {
/* 2596 */       return;
/*      */     }
/*      */ 
/* 2599 */     FileUtils.reserveDirectory(this.m_collection.m_location);
/* 2600 */     FileUtils.reserveDirectory(this.m_collection.m_exportLocation);
/* 2601 */     boolean doDelete = true;
/*      */     try
/*      */     {
/* 2605 */       DataBinder binder = ArchiveUtils.readExportsFile(this.m_archiveDir, null);
/* 2606 */       DataResultSet docSet = (DataResultSet)binder.getResultSet("BatchFiles");
/* 2607 */       if ((docSet == null) || (docSet.isEmpty()))
/*      */       {
/*      */         Boolean isEmpty;
/*      */         return;
/*      */       }
/* 2612 */       doDelete = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("DeleteArchiveAfterAutomaticImport"), true);
/* 2613 */       Object[] obj = { filename, (doDelete) ? Boolean.TRUE : Boolean.FALSE, docSet };
/* 2614 */       this.m_cxt.setCachedObject("cleanUpAfterImport:parameters", obj);
/* 2615 */       int ret = PluginFilters.filter("cleanUpAfterImport", this.m_workspace, this.m_binder, this.m_cxt);
/* 2616 */       if (ret == -1) {
/*      */         Boolean isEmpty;
/*      */         return;
/*      */       }
/* 2620 */       filename = (String)obj[0];
/* 2621 */       doDelete = ((Boolean)obj[1]).booleanValue();
/* 2622 */       docSet = (DataResultSet)obj[2];
/*      */ 
/* 2624 */       Vector values = docSet.findRow(0, filename);
/* 2625 */       if (values != null)
/*      */       {
/* 2627 */         docSet.deleteCurrentRow();
/* 2628 */         ArchiveUtils.writeExportsFile(this.m_archiveDir, binder);
/*      */ 
/* 2630 */         if (doDelete)
/*      */         {
/* 2632 */           FileUtils.deleteFile(this.m_archiveExportDir + '/' + filename);
/* 2633 */           if (SystemUtils.m_verbose)
/*      */           {
/* 2635 */             Report.debug("archiver", "Deleting file: " + this.m_archiveExportDir + '/' + filename, null);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*      */       Boolean isEmpty;
/* 2642 */       FileUtils.releaseDirectory(this.m_collection.m_location);
/* 2643 */       FileUtils.releaseDirectory(this.m_collection.m_exportLocation);
/*      */ 
/* 2645 */       Boolean isEmpty = Boolean.valueOf(isEmptyArchiveDir(filename));
/* 2646 */       if ((doDelete) && (((this.m_isLastFile) || (isEmpty.booleanValue()))))
/*      */       {
/* 2648 */         if (SystemUtils.m_verbose)
/*      */         {
/* 2650 */           Report.trace("archiver", "m_isLastFile: " + this.m_isLastFile + " isEmptyArchiveDir: " + isEmpty, null);
/*      */         }
/*      */ 
/* 2653 */         deleteExportDirectory(filename);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean isEmptyArchiveDir(String filename)
/*      */   {
/* 2660 */     String dirPath = this.m_archiveExportDir + '/' + FileUtils.getDirectory(filename);
/* 2661 */     File dir = new File(dirPath);
/* 2662 */     String[] files = dir.list();
/*      */ 
/* 2665 */     return (files != null) && (files.length == 0);
/*      */   }
/*      */ 
/*      */   public void importTableEntries()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2672 */     this.m_archiveTable.doImportTableEntries();
/*      */   }
/*      */ 
/*      */   protected void importUsers()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2681 */     DataBinder binder = ResourceUtils.readDataBinder(this.m_archiveDir, "users.hda");
/* 2682 */     DataResultSet rset = (DataResultSet)binder.getResultSet("Users");
/* 2683 */     if (rset == null)
/*      */     {
/* 2685 */       Report.appError("archiver", null, null, "csUsersTableMissing", new Object[0]);
/* 2686 */       return;
/*      */     }
/*      */ 
/* 2689 */     this.m_st.importTable("Users", rset, "dName");
/*      */ 
/* 2691 */     this.m_workspace.releaseConnection();
/*      */   }
/*      */ 
/*      */   protected void prepareContextForFilter(String fileName, String metaFileName, String numFiles)
/*      */   {
/* 2700 */     if (this.m_curContentDir != null)
/*      */     {
/* 2702 */       this.m_cxt.setCachedObject("batchDir", this.m_curContentDir);
/*      */     }
/* 2704 */     if ((fileName != null) && (fileName.length() != 0))
/*      */     {
/* 2706 */       this.m_cxt.setCachedObject("batchFileName", fileName);
/*      */     }
/* 2708 */     if ((metaFileName != null) && (metaFileName.length() != 0))
/*      */     {
/* 2710 */       this.m_cxt.setCachedObject("metaFileName", metaFileName);
/*      */     }
/* 2712 */     if ((numFiles == null) || (numFiles.length() == 0))
/*      */       return;
/* 2714 */     this.m_cxt.setCachedObject("fileNumber", numFiles);
/*      */   }
/*      */ 
/*      */   public ArchiveExportStateInformation getExportState()
/*      */   {
/* 2723 */     return this.m_exportState;
/*      */   }
/*      */ 
/*      */   public ArchiveImportStateInformation getImportState()
/*      */   {
/* 2728 */     return this.m_importState;
/*      */   }
/*      */ 
/*      */   public ArchiveExportHelper getExportHelper()
/*      */   {
/* 2733 */     return this.m_exportHelper;
/*      */   }
/*      */ 
/*      */   public DataBinder getArchiveData()
/*      */   {
/* 2738 */     return this.m_archiveData;
/*      */   }
/*      */ 
/*      */   public DataBinder getDataBinder()
/*      */   {
/* 2743 */     return this.m_binder;
/*      */   }
/*      */ 
/*      */   public ExportQueryData getQueryData()
/*      */   {
/* 2748 */     return this.m_queryData;
/*      */   }
/*      */ 
/*      */   public DataResultSet getExtraExportAdditionRevInfos()
/*      */   {
/* 2753 */     return this.m_extraExportAdditionRevInfos;
/*      */   }
/*      */ 
/*      */   public String getTSDirName()
/*      */   {
/* 2763 */     String dirDateFormat = "yy-MMM-dd_HH.mm.ss_SSS";
/* 2764 */     String localeDateFormat = SharedObjects.getEnvironmentValue("LocaleArchiverDateFormat");
/* 2765 */     if ((localeDateFormat != null) && (localeDateFormat.length() > 0))
/*      */     {
/* 2767 */       dirDateFormat = localeDateFormat;
/*      */     }
/*      */ 
/* 2770 */     SimpleDateFormat dirFrmt = new SimpleDateFormat(dirDateFormat);
/* 2771 */     dirFrmt.setTimeZone(TimeZone.getDefault());
/* 2772 */     String tsDir = dirFrmt.format(this.m_archiveDate).toLowerCase();
/*      */ 
/* 2774 */     synchronized (SYNC_OBJ)
/*      */     {
/* 2776 */       m_dirCounter = ++m_dirCounter % 100;
/* 2777 */       tsDir = tsDir + "_";
/* 2778 */       if (m_dirCounter < 10)
/*      */       {
/* 2780 */         tsDir = tsDir + "0";
/*      */       }
/* 2782 */       tsDir = tsDir + m_dirCounter;
/*      */     }
/* 2784 */     return tsDir;
/*      */   }
/*      */ 
/*      */   public void handleArchiveCommandStart(String cmd, DataBinder binder, Service service)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2790 */     Boolean isAutoBool = (this.m_isAuto) ? Boolean.TRUE : Boolean.FALSE;
/* 2791 */     Boolean isQueuedBool = (this.m_isQueued) ? Boolean.TRUE : Boolean.FALSE;
/* 2792 */     Boolean isLogError = (this.m_isLogError) ? Boolean.TRUE : Boolean.FALSE;
/* 2793 */     this.m_cxt.setCachedObject("handleArchiveCommand:params", new Object[] { binder, service, isAutoBool, isQueuedBool, isLogError });
/* 2794 */     PluginFilters.filter("handleArchiveCommandStart", this.m_workspace, this.m_binder, this.m_cxt);
/*      */   }
/*      */ 
/*      */   public void cleanUpCommandEnd() throws DataException, ServiceException
/*      */   {
/* 2799 */     PluginFilters.filter("cleanUpCommandEnd", this.m_workspace, this.m_binder, this.m_cxt);
/* 2800 */     this.m_cxt.setCachedObject("handleArchiveCommand:params", "");
/*      */   }
/*      */ 
/*      */   protected boolean checkForAlternateArchiveExceptionHandling(Object[] params)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2806 */     boolean retVal = false;
/* 2807 */     this.m_cxt.setCachedObject("checkForAlternateArchiveExceptionHandling:params", params);
/* 2808 */     retVal = PluginFilters.filter("checkForAlternateArchiveExceptionHandling", this.m_workspace, this.m_binder, this.m_cxt) != 0;
/*      */ 
/* 2810 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2816 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99142 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiveHandler
 * JD-Core Version:    0.5.4
 */