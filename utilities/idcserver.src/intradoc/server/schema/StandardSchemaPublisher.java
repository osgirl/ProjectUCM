/*      */ package intradoc.server.schema;
/*      */ 
/*      */ import intradoc.common.AppObjectRepository;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.ForkedOutputStream;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NativeOsUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.PosixStructStat;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.ProgressState;
/*      */ import intradoc.shared.ProgressStateUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.schema.HashableStringArray;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import intradoc.shared.schema.SchemaLoader;
/*      */ import intradoc.shared.schema.SchemaPublishNode;
/*      */ import intradoc.shared.schema.SchemaRelationConfig;
/*      */ import intradoc.shared.schema.SchemaRelationData;
/*      */ import intradoc.shared.schema.SchemaViewConfig;
/*      */ import intradoc.shared.schema.SchemaViewData;
/*      */ import intradoc.util.IdcException;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.io.Writer;
/*      */ import java.security.DigestOutputStream;
/*      */ import java.security.MessageDigest;
/*      */ import java.security.NoSuchAlgorithmException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ import java.util.zip.GZIPOutputStream;
/*      */ 
/*      */ public class StandardSchemaPublisher
/*      */   implements SchemaPublisher
/*      */ {
/*      */   protected boolean m_abort;
/*      */   protected String m_publishDirectory;
/*      */   protected String m_tempDir;
/*      */   protected String m_temp2Dir;
/*      */   protected String m_targetBackupDir;
/*      */   protected String m_targetDir;
/*      */   protected NativeOsUtils m_nativeUtils;
/*      */   protected Workspace m_workspace;
/*      */   protected ExecutionContext m_context;
/*      */   protected Hashtable m_fileTable;
/*      */   protected Vector m_fileList;
/*      */   protected SchemaHelper m_helper;
/*      */   protected SchemaUtils m_utils;
/*      */   protected int m_releaseConnectionCountLimit;
/*      */   protected int m_releaseConnectionCount;
/*      */   protected int m_releaseConnectionMinInterval;
/*      */   protected long m_lastReleaseConnectionTimestamp;
/*      */   protected HashMap m_classOutputObjects;
/*      */   protected HashMap m_tableMap;
/*      */   public String m_publishOperation;
/*      */   public boolean m_useGzipFiles;
/*      */   public boolean m_useGzipExtension;
/*      */   public String m_defaultExtension;
/*      */   public int m_openFileCounter;
/*   71 */   public static int F_NULL_OUTPUT = 1;
/*      */   protected Properties m_environment;
/*      */   protected boolean m_buildDigestFile;
/*      */   protected boolean m_verifyPublishing;
/*      */   protected boolean m_computeDigests;
/*      */   public long m_win32DelayRenameInterval;
/*      */   public long m_renameRetryInitialInterval;
/*      */   public long m_renameRetryIterations;
/*      */   public Hashtable m_utilsInitData;
/*      */   public ProgressState m_progress;
/*      */ 
/*      */   public StandardSchemaPublisher()
/*      */   {
/*   37 */     this.m_abort = false;
/*      */ 
/*   59 */     this.m_lastReleaseConnectionTimestamp = 0L;
/*      */ 
/*   65 */     this.m_useGzipFiles = false;
/*   66 */     this.m_useGzipExtension = false;
/*   67 */     this.m_defaultExtension = ".js";
/*      */ 
/*   69 */     this.m_openFileCounter = 0;
/*      */ 
/*   80 */     this.m_buildDigestFile = false;
/*      */ 
/*   87 */     this.m_verifyPublishing = false;
/*      */ 
/*   96 */     this.m_computeDigests = false;
/*      */ 
/*   98 */     this.m_win32DelayRenameInterval = -1L;
/*   99 */     this.m_renameRetryInitialInterval = -1L;
/*  100 */     this.m_renameRetryIterations = -1L;
/*      */ 
/*  102 */     this.m_utilsInitData = new Hashtable();
/*      */ 
/*  104 */     this.m_progress = null;
/*      */   }
/*      */ 
/*      */   public void init(Workspace ws, String publishDir, ExecutionContext context) throws ServiceException
/*      */   {
/*  109 */     this.m_win32DelayRenameInterval = SharedObjects.getTypedEnvironmentInt("SchemaWin32RenameDelayInterval", 10, 18, 18);
/*      */ 
/*  112 */     this.m_renameRetryInitialInterval = SharedObjects.getTypedEnvironmentInt("SchemaPublishRenameRetryInitialInterval", 100, 18, 18);
/*      */ 
/*  116 */     this.m_renameRetryIterations = SharedObjects.getEnvironmentInt("SchemaPublishRenameRetryIterations", 4);
/*      */ 
/*  118 */     this.m_useGzipFiles = SharedObjects.getEnvValueAsBoolean("GzipPublishedFiles", false);
/*      */ 
/*  120 */     if (this.m_useGzipFiles)
/*      */     {
/*  122 */       this.m_useGzipExtension = SharedObjects.getEnvValueAsBoolean("UseGzipExtension", true);
/*      */     }
/*      */ 
/*  125 */     if (this.m_useGzipExtension)
/*      */     {
/*  127 */       this.m_defaultExtension = ".js.gz";
/*      */     }
/*  129 */     this.m_buildDigestFile = SharedObjects.getEnvValueAsBoolean("SchemaBuildDigestFile", false);
/*  130 */     this.m_verifyPublishing = SharedObjects.getEnvValueAsBoolean("SchemaVerifyPublishing", false);
/*  131 */     this.m_computeDigests = ((this.m_buildDigestFile) || (this.m_verifyPublishing) || (SharedObjects.getEnvValueAsBoolean("SchemaComputeDigests", false)));
/*      */ 
/*  133 */     this.m_releaseConnectionCountLimit = SharedObjects.getEnvironmentInt("SchemaReleaseConnectionCountLimit", 20);
/*      */ 
/*  135 */     this.m_releaseConnectionMinInterval = SharedObjects.getTypedEnvironmentInt("SchemaReleaseConnectionMinInterval", 100, 18, 18);
/*      */ 
/*  138 */     this.m_workspace = ws;
/*  139 */     this.m_publishDirectory = FileUtils.directorySlashes(publishDir);
/*  140 */     this.m_tempDir = (this.m_publishDirectory + "schema.work/");
/*  141 */     this.m_temp2Dir = (this.m_publishDirectory + "schema.done/");
/*  142 */     this.m_targetBackupDir = (this.m_publishDirectory + "schema.old/");
/*  143 */     this.m_targetDir = (this.m_publishDirectory + "schema/");
/*      */ 
/*  146 */     this.m_progress = new ProgressState();
/*  147 */     this.m_progress.init("SchemaPublish");
/*      */ 
/*  149 */     this.m_helper = ((SchemaHelper)context.getCachedObject("SchemaHelper"));
/*  150 */     if (this.m_helper == null)
/*      */     {
/*  152 */       String msg = LocaleUtils.encodeMessage("csRequiredObjectMissing", null, "SchemaHelper");
/*      */ 
/*  154 */       throw new ServiceException(-50, msg);
/*      */     }
/*      */ 
/*  157 */     this.m_utils = ((SchemaUtils)context.getCachedObject("SchemaUtils"));
/*  158 */     if (this.m_utils == null)
/*      */     {
/*  160 */       String msg = LocaleUtils.encodeMessage("csRequiredObjectMissing", null, "SchemaUtils");
/*      */ 
/*  162 */       throw new ServiceException(-50, msg);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  167 */       if (EnvUtils.m_useNativeOSUtils)
/*      */       {
/*  169 */         this.m_nativeUtils = new NativeOsUtils();
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  174 */       ProgressStateUtils.reportError(this.m_progress, "schemapublisher", t, "csNativeOsUtilsError", new Object[0]);
/*      */     }
/*      */ 
/*  178 */     this.m_context = context;
/*  179 */     this.m_context.setCachedObject("SchemaPublisher", this);
/*      */ 
/*  183 */     this.m_environment = ((Properties)this.m_context.getCachedObject("Environment"));
/*  184 */     if (this.m_environment == null)
/*      */     {
/*  186 */       this.m_environment = ((Properties)AppObjectRepository.getObject("safeEnvironment"));
/*      */     }
/*  188 */     if (this.m_environment != null)
/*      */       return;
/*  190 */     this.m_environment = ((Properties)AppObjectRepository.getObject("environment"));
/*      */   }
/*      */ 
/*      */   public String getPublishDirectory()
/*      */   {
/*  196 */     return this.m_publishDirectory;
/*      */   }
/*      */ 
/*      */   public ProgressState getProgress()
/*      */   {
/*  201 */     return this.m_progress;
/*      */   }
/*      */ 
/*      */   protected DataBinder newBasicBinder()
/*      */     throws DataException, ServiceException
/*      */   {
/*  207 */     DataBinder binder = new DataBinder(this.m_environment);
/*  208 */     for (int i = 0; i < SchemaHelper.SCHEMA_CONTAINERS.length; ++i)
/*      */     {
/*  210 */       String name = SchemaHelper.SCHEMA_CONTAINERS[i];
/*  211 */       ResultSet rset = SharedObjects.getTable(name);
/*  212 */       binder.addResultSet(name, rset);
/*      */     }
/*      */ 
/*  217 */     DataResultSet drset = SharedObjects.getTable("StandardFieldIncludes");
/*  218 */     binder.addResultSet("StandardFieldIncludes", drset);
/*  219 */     drset = SharedObjects.getTable("AdditionalFieldIncludes");
/*  220 */     binder.addResultSet("AdditionalFieldIncludes", drset);
/*      */ 
/*  222 */     PluginFilters.filter("schemaNewBinder", this.m_workspace, binder, this.m_context);
/*      */ 
/*  224 */     return binder;
/*      */   }
/*      */ 
/*      */   public void doPublishing(DataBinder settings)
/*      */     throws ServiceException, DataException
/*      */   {
/*  234 */     this.m_publishOperation = "(null)";
/*  235 */     boolean success = false;
/*      */     try
/*      */     {
/*  238 */       this.m_openFileCounter = 0;
/*  239 */       if (this.m_workspace != null)
/*      */       {
/*  241 */         SchemaManager.getManager(this.m_workspace).refresh(this.m_workspace);
/*      */       }
/*  243 */       this.m_helper.init();
/*  244 */       this.m_publishOperation = settings.getLocal("publishOperation");
/*  245 */       if (this.m_publishOperation == null)
/*      */       {
/*  247 */         this.m_publishOperation = "full";
/*      */       }
/*  249 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 4, "m_publishOperation is '" + this.m_publishOperation + "'", null);
/*      */ 
/*  252 */       FileUtils.deleteDirectory(new File(this.m_tempDir), true);
/*  253 */       FileUtils.deleteDirectory(new File(this.m_temp2Dir), true);
/*      */ 
/*  255 */       if (this.m_helper.m_views == null)
/*      */       {
/*  257 */         String msg = LocaleUtils.encodeMessage("csRequiredObjectMissing", null, "SchemaViewConfig");
/*      */ 
/*  259 */         throw new ServiceException(-50, msg);
/*      */       }
/*      */ 
/*  262 */       if (this.m_helper.m_relationships == null)
/*      */       {
/*  264 */         String msg = LocaleUtils.encodeMessage("csRequiredObjectMissing", null, "SchemaRelationConfig");
/*      */ 
/*  266 */         throw new ServiceException(-50, msg);
/*      */       }
/*      */ 
/*  269 */       if (this.m_helper.m_fields == null)
/*      */       {
/*  271 */         String msg = LocaleUtils.encodeMessage("csRequiredObjectMissing", null, "SchemaFieldConfig");
/*      */ 
/*  273 */         throw new ServiceException(-50, msg);
/*      */       }
/*      */ 
/*  279 */       for (int i = 0; i < SchemaHelper.SCHEMA_CONTAINERS.length; ++i)
/*      */       {
/*  281 */         String name = SchemaHelper.SCHEMA_CONTAINERS[i];
/*  282 */         ResultSet rset = SharedObjects.getTable(name);
/*  283 */         this.m_utilsInitData.put(name, rset);
/*      */       }
/*      */ 
/*  286 */       this.m_fileTable = new Hashtable();
/*  287 */       this.m_fileList = new IdcVector();
/*  288 */       DataBinder binder = newBasicBinder();
/*  289 */       binder.merge(settings);
/*  290 */       this.m_utils.init(this.m_utilsInitData);
/*      */ 
/*  294 */       finishClassOutput(false);
/*      */ 
/*  296 */       int rc = PluginFilters.filter("schemaPublishingStart", this.m_workspace, binder, this.m_context);
/*      */ 
/*  298 */       switch (rc)
/*      */       {
/*      */       case 1:
/*  301 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "schemaPublishingStart filter returned FINISHED", null);
/*      */         return;
/*      */       case -1:
/*  306 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "schemaPublishingStart filter returned ABORT, continuing", null);
/*      */       }
/*      */ 
/*  311 */       this.m_tableMap = new HashMap();
/*  312 */       this.m_helper.m_relationships.first();
/*  313 */       while (this.m_helper.m_relationships.isRowPresent())
/*      */       {
/*  316 */         SchemaRelationData data = (SchemaRelationData)this.m_helper.m_relationships.getData();
/*      */ 
/*  318 */         String relationType = data.get("schRelationType");
/*  319 */         if ((relationType == null) || (relationType.equalsIgnoreCase("table")))
/*      */         {
/*  321 */           String childTable = data.get("schTable2Table");
/*  322 */           String relationName = data.get("schRelationName");
/*  323 */           Vector parentList = (Vector)this.m_tableMap.get(childTable);
/*  324 */           if (parentList == null)
/*      */           {
/*  326 */             parentList = new IdcVector();
/*  327 */             this.m_tableMap.put(childTable, parentList);
/*      */           }
/*  329 */           parentList.addElement(relationName);
/*      */         }
/*  314 */         this.m_helper.m_relationships.next();
/*      */       }
/*      */ 
/*  333 */       publishSchemaBase();
/*  334 */       rc = PluginFilters.filter("schemaPublishingBaseFinished", this.m_workspace, binder, this.m_context);
/*      */ 
/*  337 */       this.m_helper.m_views.first();
/*  338 */       while (this.m_helper.m_views.isRowPresent())
/*      */       {
/*  341 */         SchemaViewData data = (SchemaViewData)this.m_helper.m_views.getData();
/*  342 */         this.m_context.setCachedObject("SchemaViewData", data);
/*  343 */         rc = PluginFilters.filter("schemaPublishView", this.m_workspace, binder, this.m_context);
/*      */ 
/*  345 */         switch (rc)
/*      */         {
/*      */         case -1:
/*  348 */           ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "schemaPublishView filter returned ABORT, continuing", null);
/*      */         case 0:
/*  352 */           publishViewValues(data);
/*      */         case 1:
/*      */         }
/*  339 */         this.m_helper.m_views.next();
/*      */       }
/*      */ 
/*  358 */       finishClassOutput(true);
/*      */ 
/*  360 */       rc = PluginFilters.filter("schemaFinalizePublishing", this.m_workspace, binder, this.m_context);
/*      */ 
/*  362 */       switch (rc)
/*      */       {
/*      */       case -1:
/*  365 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "schemaFinalizePublishing filter returned ABORT, continuing", null);
/*      */       case 0:
/*  369 */         if (!finalizePublishing())
/*      */         {
/*  371 */           throw new ServiceException("!csSchemaFailedToMovePublishedFilesIntoPlace");
/*      */         }
/*      */ 
/*      */       case 1:
/*      */       }
/*      */ 
/*  378 */       rc = PluginFilters.filter("schemaPublishingFinished", this.m_workspace, binder, this.m_context);
/*      */ 
/*  380 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "published '" + this.m_publishOperation + "' successfully.  " + this.m_openFileCounter + " files open", null);
/*      */ 
/*  383 */       success = true;
/*      */     }
/*      */     finally
/*      */     {
/*  387 */       if (!success)
/*      */       {
/*  389 */         this.m_progress.setStateValue("latestState", "Failed");
/*  390 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 2, "m_publishOperation '" + this.m_publishOperation + "' failed", null);
/*      */       }
/*      */       else
/*      */       {
/*  396 */         this.m_progress.setStateValue("latestState", "Success");
/*      */       }
/*  398 */       releaseConnection(true);
/*  399 */       finishClassOutput(false);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void finishClassOutput(boolean doCommit) throws ServiceException
/*      */   {
/*  405 */     if (this.m_classOutputObjects == null)
/*      */     {
/*  407 */       this.m_classOutputObjects = new HashMap();
/*  408 */       return;
/*      */     }
/*  410 */     Set entrySet = this.m_classOutputObjects.entrySet();
/*  411 */     Object[] entries = entrySet.toArray();
/*  412 */     for (int i = 0; i < entries.length; ++i)
/*      */     {
/*  414 */       Map.Entry entry = (Map.Entry)entries[i];
/*  415 */       String path = (String)entry.getKey();
/*  416 */       SchemaOutputObject output = (SchemaOutputObject)entry.getValue();
/*      */       try
/*      */       {
/*  419 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "finishing " + path, null);
/*      */ 
/*  421 */         finalizeWriter(output, doCommit);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  425 */         throw new ServiceException(e);
/*      */       }
/*      */     }
/*  428 */     this.m_classOutputObjects.clear();
/*      */   }
/*      */ 
/*      */   public void publishSchemaBase()
/*      */     throws DataException, ServiceException
/*      */   {
/*  434 */     DataBinder binder = newBasicBinder();
/*  435 */     String msg = "!csSchemaUnableToPublishViewBase";
/*      */     try
/*      */     {
/*  438 */       int rc = PluginFilters.filter("schemaBasePublishing", this.m_workspace, binder, this.m_context);
/*      */ 
/*  440 */       switch (rc)
/*      */       {
/*      */       case 1:
/*  443 */         break;
/*      */       case -1:
/*  445 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "schemaBasePublishing returned ABORT, we're continuing.", null);
/*      */       default:
/*  449 */         SchemaOutputObject output = openWriter("base" + this.m_defaultExtension, 0);
/*  450 */         buildJsFile("SCHEMA_BASE_JS", binder, output.m_writer, this.m_context);
/*      */ 
/*  452 */         this.m_classOutputObjects.put("base" + this.m_defaultExtension, output);
/*      */ 
/*  454 */         output = openWriter("finalize" + this.m_defaultExtension, 0);
/*  455 */         buildJsFile("SCHEMA_FINALIZE_JS", binder, output.m_writer, this.m_context);
/*      */ 
/*  457 */         finalizeWriter(output, true);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  463 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, null, e);
/*      */ 
/*  465 */       throw new ServiceException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void publishViewValues(SchemaViewData data)
/*      */     throws DataException, ServiceException
/*      */   {
/*  472 */     if (data == null)
/*      */     {
/*  474 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "The view is not defined.", null);
/*      */ 
/*  476 */       return;
/*      */     }
/*  478 */     boolean doPublish = data.getBoolean("PublishViewData", true);
/*  479 */     if (!doPublish)
/*      */     {
/*  481 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "skipping view '" + data.m_name + "'", null);
/*      */ 
/*  483 */       return;
/*      */     }
/*  485 */     ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "publishing view '" + data.m_name + "'", null);
/*      */ 
/*  488 */     DataBinder binder = new DataBinder(this.m_environment);
/*  489 */     data.populateBinder(binder);
/*      */ 
/*  491 */     publishEntireView(binder, data);
/*  492 */     publishViewRelationshipFragments(binder, data);
/*  493 */     ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "finished view '" + data.m_name + "'", null);
/*      */   }
/*      */ 
/*      */   protected void publishEntireView(DataBinder binder, SchemaViewData data)
/*      */     throws DataException, ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  503 */       String viewColumnsString = data.get("schViewColumns");
/*  504 */       Vector viewColumns = StringUtils.parseArray(viewColumnsString, ',', '^');
/*  505 */       viewColumnsString = "";
/*  506 */       for (int j = 0; j < viewColumns.size(); ++j)
/*      */       {
/*  508 */         if (j > 0)
/*      */         {
/*  510 */           viewColumnsString = viewColumnsString + ", ";
/*      */         }
/*  512 */         viewColumnsString = viewColumnsString + "child." + viewColumns.elementAt(j);
/*      */       }
/*  514 */       binder.putLocal("ViewColumnsString", viewColumnsString);
/*      */ 
/*  516 */       binder.putLocal("PublishedTableName", data.m_canonicalName);
/*  517 */       if (!data.getBoolean("schPublishAllValues", true))
/*      */       {
/*  519 */         return;
/*      */       }
/*      */ 
/*  522 */       String scriptFile = "views/" + StringUtils.encodeJavascriptFilename(data.m_name) + "/all" + this.m_defaultExtension;
/*      */ 
/*  525 */       binder.putLocal("ScriptFile", scriptFile);
/*      */ 
/*  529 */       int flags = 0;
/*  530 */       boolean hasExtraWriters = preparePublishClasses(data);
/*  531 */       if (this.m_publishOperation.equals("base"))
/*      */       {
/*  533 */         if (hasExtraWriters)
/*      */         {
/*  535 */           flags |= F_NULL_OUTPUT;
/*      */         }
/*      */         else
/*      */         {
/*  539 */           if (SystemUtils.m_verbose)
/*      */           {
/*  541 */             Report.debug("schemapublisher", "skipping all values for " + data.m_name + " because it's not required", null);
/*      */           }
/*      */ 
/*  545 */           return;
/*      */         }
/*      */       }
/*      */ 
/*  549 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "getting all values for view " + data.m_name, null);
/*      */ 
/*  551 */       ResultSet rset = data.getAllViewValues();
/*  552 */       releaseConnection(false);
/*  553 */       binder.addResultSet(data.m_canonicalName, rset);
/*      */ 
/*  555 */       int rc = PluginFilters.filter("schemaPublishingEntireView", this.m_workspace, binder, this.m_context);
/*      */ 
/*  557 */       switch (rc)
/*      */       {
/*      */       case 1:
/*  560 */         if (FileUtils.checkFile(scriptFile, true, false) == 0)
/*      */         {
/*      */           break label429;
/*      */         }
/*      */ 
/*  563 */         String msg = LocaleUtils.encodeMessage("csSchemaFilterFailedToCompleteOperation", null, "schemaPublishingEntireView");
/*      */ 
/*  566 */         throw new IOException(msg);
/*      */       case -1:
/*  570 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "schemaPublishingEntireView filter returned ABORT. Continuing.", null);
/*      */       case 0:
/*      */       }
/*      */ 
/*  575 */       SchemaOutputObject output = openWriter(scriptFile, flags);
/*  576 */       buildJsFile("SCHEMA_VIEW_JS", binder, output.m_writer, this.m_context);
/*  577 */       label429: finalizeWriter(output, true);
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  583 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, null, t);
/*      */ 
/*  585 */       boolean reportExceptionOnly = false;
/*  586 */       if (t instanceof IdcException)
/*      */       {
/*  589 */         IdcException iException = (IdcException)t;
/*  590 */         if ((iException.m_errorCode == -27) && (!SharedObjects.getEnvValueAsBoolean("SchemaMisconfigurationStopsPublishing", false)))
/*      */         {
/*  595 */           reportExceptionOnly = true;
/*      */         }
/*      */       }
/*  598 */       if (reportExceptionOnly)
/*      */       {
/*  600 */         Report.error("schema", t, "csSchemaViewResourceNotDefined", new Object[] { data.m_name });
/*      */       }
/*      */       else
/*      */       {
/*  604 */         throw new ServiceException(t, "csSchemaUnableToPublishView", new Object[] { data.m_name });
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean preparePublishClasses(SchemaViewData data)
/*      */     throws DataException, ServiceException
/*      */   {
/*  612 */     List list = data.getMatchingKeys("PublishClassMember_*", null);
/*  613 */     boolean rc = false;
/*  614 */     ArrayList outputStreams = new ArrayList();
/*  615 */     SchemaOutputObject output = null;
/*  616 */     for (int i = 0; i < list.size(); ++i)
/*      */     {
/*  618 */       String key = (String)list.get(i);
/*  619 */       int index = key.indexOf("_");
/*  620 */       String className = key.substring(index + 1);
/*      */       String classFile;
/*      */       String classFile;
/*  622 */       if (className.equals("base"))
/*      */       {
/*  624 */         classFile = "base" + this.m_defaultExtension;
/*      */       }
/*      */       else
/*      */       {
/*  628 */         if (this.m_publishOperation.equals("base"))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  633 */         classFile = "classes/" + StringUtils.encodeJavascriptFilename(className) + this.m_defaultExtension;
/*      */       }
/*      */ 
/*  637 */       output = (SchemaOutputObject)this.m_classOutputObjects.get(classFile);
/*      */ 
/*  639 */       if (output == null)
/*      */       {
/*      */         try
/*      */         {
/*  643 */           output = openWriter(classFile, 0);
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*  647 */           throw new ServiceException(e);
/*      */         }
/*  649 */         this.m_classOutputObjects.put(classFile, output);
/*      */       }
/*  651 */       rc = true;
/*  652 */       outputStreams.add(output.m_output);
/*  653 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "view " + data.m_name + " class " + classFile, null);
/*      */     }
/*      */ 
/*  656 */     if (rc)
/*      */     {
/*  658 */       OutputStream[] l = (OutputStream[])(OutputStream[])outputStreams.toArray(new OutputStream[0]);
/*      */ 
/*  660 */       if (l.length == 1)
/*      */       {
/*  662 */         OutputStream out = l[0];
/*  663 */         this.m_context.setCachedObject("extraOutput", out);
/*      */       }
/*      */       else
/*      */       {
/*  667 */         ForkedOutputStream out = new ForkedOutputStream(l);
/*  668 */         out.m_closeStreams = true;
/*  669 */         this.m_context.setCachedObject("extraOutput", out);
/*      */       }
/*      */     }
/*  672 */     return rc;
/*      */   }
/*      */ 
/*      */   protected void publishViewRelationshipFragments(DataBinder binder, SchemaViewData data)
/*      */     throws DataException, ServiceException
/*      */   {
/*  686 */     if (this.m_publishOperation.equals("base"))
/*      */     {
/*  688 */       return;
/*      */     }
/*      */ 
/*  691 */     ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "publishing fragments for '" + data.m_name + "'", null);
/*      */ 
/*  693 */     this.m_helper.m_relationships.first();
/*  694 */     while (this.m_helper.m_relationships.isRowPresent())
/*      */     {
/*  697 */       SchemaRelationData relationship = (SchemaRelationData)this.m_helper.m_relationships.getData();
/*      */       try
/*      */       {
/*  701 */         publishViewRelationshipFragmentsForView(binder, data, relationship);
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/*  706 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, null, t);
/*      */ 
/*  708 */         String msg = LocaleUtils.encodeMessage("csSchemaUnableToPublishView2", null, data.m_name, relationship.m_name);
/*      */ 
/*  711 */         throw new ServiceException(msg, t);
/*      */       }
/*  695 */       this.m_helper.m_relationships.next();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void publishViewRelationshipFragmentsForView(DataBinder binder, SchemaViewData view, SchemaRelationData relationship)
/*      */     throws DataException, ServiceException
/*      */   {
/*  720 */     ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "working with relationship '" + relationship.m_name + "'", null);
/*      */ 
/*  723 */     SchemaPublishNode tree = new SchemaPublishNode(view, relationship);
/*  724 */     if (computePublishingTree(tree))
/*      */     {
/*  726 */       if (SystemUtils.m_verbose)
/*      */       {
/*  728 */         tree.trace("schemapublisher", this.m_progress);
/*      */       }
/*  730 */       List leafs = findBestTreeLeafs(tree);
/*  731 */       HashMap processedPaths = new HashMap();
/*  732 */       for (int i = 0; i < leafs.size(); ++i)
/*      */       {
/*  734 */         SchemaPublishNode leaf = (SchemaPublishNode)leafs.get(i);
/*  735 */         if (SystemUtils.isActiveTrace("schemapublisher"))
/*      */         {
/*  737 */           IdcStringBuilder msg = new IdcStringBuilder();
/*      */ 
/*  739 */           msg.append("using publishing path ");
/*  740 */           boolean appendComma = false;
/*  741 */           SchemaPublishNode tmp = leaf;
/*  742 */           for (; tmp != null; tmp = tmp.m_parent)
/*      */           {
/*  744 */             if (appendComma)
/*      */             {
/*  746 */               msg.append(", ");
/*      */             }
/*  748 */             if (tmp.m_relationship != null)
/*      */             {
/*  750 */               SchemaRelationData r = tmp.m_relationship;
/*  751 */               msg.append("r ");
/*  752 */               msg.append(r.m_name);
/*  753 */               appendComma = true;
/*      */             }
/*  755 */             if (appendComma)
/*      */             {
/*  757 */               msg.append(", ");
/*      */             }
/*  759 */             SchemaViewData v = tmp.m_view;
/*  760 */             msg.append("v ");
/*  761 */             msg.append(v.m_name);
/*  762 */             appendComma = true;
/*      */ 
/*  764 */             if (tmp.m_recursivePointer == null)
/*      */               continue;
/*  766 */             msg.append(", recursion to ");
/*  767 */             msg.append(tmp.m_recursivePointer.toString());
/*      */           }
/*      */ 
/*  770 */           ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, msg.toString(), null);
/*      */         }
/*      */ 
/*  773 */         if (leaf == null)
/*      */           continue;
/*  775 */         processViewRelationshipPath(processedPaths, binder, view, leaf, null, null, leaf.m_relationship);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  783 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "Unable to find a usable path for view '" + view.m_name + "' and relationship '" + relationship.m_name + "'", null);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean computePublishingTree(SchemaPublishNode tree)
/*      */     throws DataException
/*      */   {
/*  793 */     SchemaViewData view = tree.m_view;
/*  794 */     SchemaRelationData relationship = tree.m_relationship;
/*  795 */     ArrayList children = tree.m_childList;
/*      */ 
/*  797 */     SchemaLoader loader = this.m_helper.m_views.findLoader(view, relationship, null);
/*      */ 
/*  799 */     if (loader == null)
/*      */     {
/*  801 */       if (SystemUtils.m_verbose)
/*      */       {
/*  803 */         Report.debug("schemapublisher", "no loader for relationship '" + relationship.m_name + "'", null);
/*      */       }
/*      */ 
/*  807 */       return false;
/*      */     }
/*  809 */     String allowRelationshipKey = "schAllowRelationship[" + relationship.m_name + "]";
/*  810 */     boolean useAllowableViews = view.getBoolean(allowRelationshipKey, true);
/*  811 */     if (!useAllowableViews)
/*      */     {
/*  813 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, " no relationship allowed for view '" + view.m_name + "' because " + allowRelationshipKey + " set to false", null);
/*      */ 
/*  816 */       return false;
/*      */     }
/*      */ 
/*  819 */     boolean loadGrandChildren = view.getBoolean("schComputeGrandParentRelationships", false);
/*  820 */     boolean useShortestPath = view.getBoolean("schComputeShortestRelationsipPath", false);
/*  821 */     String allowableViewsKey = "schAllowableRelationshipViews[" + relationship.m_name + "]";
/*  822 */     Vector allowableViews = view.getVector(allowableViewsKey);
/*  823 */     int numAllowableViews = allowableViews.size();
/*      */ 
/*  825 */     tree.m_findShortestOnly = useShortestPath;
/*  826 */     tree.m_loader = loader;
/*  827 */     SchemaViewData[] parentViews = loader.getParentViews(relationship);
/*  828 */     if ((parentViews == null) || (parentViews.length == 0))
/*      */     {
/*  830 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "when trying to publish, there weren't any parent views", null);
/*      */ 
/*  832 */       return false;
/*      */     }
/*  834 */     for (int i = 0; i < parentViews.length; ++i)
/*      */     {
/*  836 */       SchemaViewData parentView = parentViews[i];
/*  837 */       SchemaLoader parentLoader = this.m_helper.m_views.findLoader(parentView, null, null);
/*      */ 
/*  839 */       if (parentLoader == null)
/*      */       {
/*  841 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "no loader for parent view '" + parentView.m_name + "' skipping", null);
/*      */       }
/*  846 */       else if ((numAllowableViews > 0) && 
/*  848 */         (StringUtils.findStringListIndex(allowableViews, parentView.m_name) < 0))
/*      */       {
/*  850 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, " excluded '" + parentView.m_name + "', because " + allowableViewsKey + "=" + allowableViews.toString(), null);
/*      */       }
/*      */       else
/*      */       {
/*  857 */         boolean isRecursive = false;
/*  858 */         SchemaPublishNode recursivePointer = null;
/*  859 */         SchemaPublishNode parent = tree;
/*  860 */         for (; parent != null; parent = parent.m_parent)
/*      */         {
/*  862 */           SchemaViewData tmpView = parent.m_view;
/*  863 */           if (!tmpView.m_name.equals(parentView.m_name))
/*      */             continue;
/*  865 */           isRecursive = true;
/*  866 */           recursivePointer = parent;
/*      */         }
/*      */ 
/*  869 */         SchemaRelationData[] parentGrandParentRelations = null;
/*  870 */         if (loadGrandChildren)
/*      */         {
/*  872 */           parentGrandParentRelations = parentLoader.getParentRelations(parentView);
/*      */         }
/*      */ 
/*  875 */         if ((isRecursive) || (parentGrandParentRelations == null) || (parentGrandParentRelations.length == 0))
/*      */         {
/*  878 */           SchemaPublishNode nextLevel = new SchemaPublishNode(parentView, null, tree, parentLoader);
/*      */ 
/*  880 */           if (isRecursive)
/*      */           {
/*  882 */             recursivePointer.m_recursivePointer = nextLevel;
/*  883 */             nextLevel.m_relationship = relationship;
/*      */           }
/*  885 */           children.add(nextLevel);
/*      */         }
/*      */         else
/*      */         {
/*  889 */           for (int j = 0; j < parentGrandParentRelations.length; ++j)
/*      */           {
/*  891 */             SchemaPublishNode nextLevel = new SchemaPublishNode(parentView, parentGrandParentRelations[j], tree, null);
/*      */ 
/*  894 */             children.add(nextLevel);
/*  895 */             if (computePublishingTree(nextLevel))
/*      */               continue;
/*  897 */             nextLevel.m_view = null;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*  902 */     return true;
/*      */   }
/*      */ 
/*      */   protected List findBestTreeLeafs(SchemaPublishNode tree)
/*      */   {
/*  907 */     ArrayList childList = tree.m_childList;
/*  908 */     boolean shortestOnly = tree.m_findShortestOnly;
/*  909 */     List l = new ArrayList();
/*      */ 
/*  913 */     boolean hasChildren = (childList != null) && (childList.size() > 0);
/*  914 */     if (hasChildren)
/*      */     {
/*  916 */       SchemaPublishNode shortestTree = null;
/*  917 */       int shortestLength = -1;
/*  918 */       for (int i = 0; i < childList.size(); ++i)
/*      */       {
/*  920 */         SchemaPublishNode child = (SchemaPublishNode)childList.get(i);
/*  921 */         if (child.m_view == null) {
/*      */           continue;
/*      */         }
/*      */ 
/*  925 */         List childrenList = findBestTreeLeafs(child);
/*  926 */         if (shortestOnly)
/*      */         {
/*  928 */           for (int j = 0; j < childrenList.size(); ++j)
/*      */           {
/*  930 */             SchemaPublishNode bestChild = (SchemaPublishNode)childrenList.get(j);
/*  931 */             int childLength = bestChild.m_depth;
/*  932 */             if (shortestTree == null)
/*      */             {
/*  934 */               shortestTree = bestChild;
/*  935 */               shortestLength = childLength;
/*      */             } else {
/*  937 */               if (childLength >= shortestLength)
/*      */                 continue;
/*  939 */               shortestTree = bestChild;
/*  940 */               shortestLength = childLength;
/*      */             }
/*      */           }
/*      */ 
/*      */         }
/*      */         else {
/*  946 */           l.addAll(childrenList);
/*      */         }
/*      */       }
/*  949 */       if ((shortestTree != null) && (shortestOnly))
/*      */       {
/*  951 */         l.add(shortestTree);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  956 */       l.add(tree);
/*      */     }
/*      */ 
/*  959 */     return l;
/*      */   }
/*      */ 
/*      */   protected void processViewRelationshipPath(Map processedPaths, DataBinder binder, SchemaViewData finalView, SchemaPublishNode path, ResultSet parentResultSet, SchemaViewData parentView, SchemaRelationData parentRelationship)
/*      */     throws DataException, ServiceException
/*      */   {
/*  968 */     SchemaViewData currentView = path.m_view;
/*      */ 
/*  972 */     SchemaPublishNode nextNode = path.m_parent;
/*      */ 
/*  974 */     SchemaPublishNode recursivePointer = path.m_recursivePointer;
/*  975 */     if ((nextNode == null) && (recursivePointer == null))
/*      */     {
/*  977 */       return;
/*      */     }
/*  979 */     if (nextNode == null)
/*      */     {
/*  981 */       nextNode = recursivePointer;
/*      */     }
/*  983 */     SchemaViewData childView = nextNode.m_view;
/*  984 */     SchemaRelationData currentRelationship = nextNode.m_relationship;
/*  985 */     SchemaLoader loader = nextNode.m_loader;
/*      */ 
/*  987 */     ResultSet thisResultSet = null;
/*  988 */     String parentViewName = null;
/*  989 */     if (parentView != null)
/*      */     {
/*  991 */       parentViewName = parentView.m_name;
/*      */     }
/*  993 */     String parentRelationName = null;
/*  994 */     if (parentRelationship != null)
/*      */     {
/*  996 */       parentRelationName = parentRelationship.m_name;
/*      */     }
/*  998 */     thisResultSet = currentView.getParentViewValues(parentResultSet, childView.m_name, currentRelationship.m_name, parentViewName, parentRelationName);
/*      */ 
/* 1003 */     String[] parentFieldsArray = loader.constructParentFieldsArray(currentView, currentRelationship, childView, null);
/*      */ 
/* 1005 */     if ((currentRelationship.getBoolean("schPublishInitialParentValues", false)) && (processedPaths.size() == 0))
/*      */     {
/* 1008 */       if (!thisResultSet instanceof DataResultSet)
/*      */       {
/* 1010 */         DataResultSet drset = new DataResultSet();
/* 1011 */         drset.copy(thisResultSet);
/* 1012 */         thisResultSet = drset;
/* 1013 */         thisResultSet.first();
/*      */       }
/* 1015 */       String[] parentValuesArray = new String[parentFieldsArray.length];
/* 1016 */       for (int i = 0; i < parentValuesArray.length; ++i)
/*      */       {
/* 1018 */         parentValuesArray[i] = "";
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/* 1023 */         publishViewRelationshipFragment(binder, thisResultSet, childView, currentRelationship, parentFieldsArray, parentValuesArray, "@@root", null);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1030 */         String msg = LocaleUtils.encodeMessage("csSchemaUnabletoPublishViewRootFragmentForParent", null, currentView.m_name);
/* 1031 */         throw new ServiceException(msg, e);
/*      */       }
/*      */     }
/*      */ 
/* 1035 */     for (thisResultSet.first(); thisResultSet.isRowPresent(); )
/*      */     {
/* 1038 */       boolean reachedViewBeingPublished = childView.m_name.equals(finalView.m_name);
/*      */ 
/* 1040 */       String[] pathArray = new String[2 + thisResultSet.getNumFields()];
/*      */ 
/* 1044 */       pathArray[0] = childView.m_name;
/* 1045 */       pathArray[1] = currentRelationship.m_name;
/* 1046 */       for (int i = 2; i < pathArray.length; ++i)
/*      */       {
/* 1048 */         pathArray[i] = thisResultSet.getStringValue(i - 2);
/*      */       }
/* 1050 */       HashableStringArray processedPath = new HashableStringArray(pathArray);
/*      */ 
/* 1052 */       if (processedPaths.get(processedPath) == null)
/*      */       {
/* 1054 */         processedPaths.put(processedPath, processedPath);
/* 1055 */         if (reachedViewBeingPublished)
/*      */         {
/* 1057 */           String[] parentValuesArray = loader.constructParentValuesArray(currentView, currentRelationship, childView, thisResultSet, null);
/*      */ 
/* 1059 */           String pseudoPath = "C " + childView.m_name + " R " + currentRelationship.m_name + " PV " + parentValuesArray[0];
/* 1060 */           Object obj = processedPaths.get(pseudoPath);
/* 1061 */           if (obj == null)
/*      */           {
/* 1063 */             processedPaths.put(pseudoPath, currentView.m_name);
/* 1064 */             ResultSet viewValues = childView.getViewValues(currentRelationship.m_name, thisResultSet, null);
/*      */ 
/* 1066 */             String parentInternalName = currentView.get("schInternalColumn");
/* 1067 */             String parentInternalValue = thisResultSet.getStringValueByName(parentInternalName);
/*      */             try
/*      */             {
/* 1070 */               publishViewRelationshipFragment(binder, viewValues, childView, currentRelationship, parentFieldsArray, parentValuesArray, null, parentInternalValue);
/*      */             }
/*      */             catch (Exception e)
/*      */             {
/* 1078 */               String msg = LocaleUtils.encodeMessage("csSchemaUnableToPublishViewFragmentForParent", null, currentView.m_name);
/* 1079 */               throw new ServiceException(msg, e);
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/* 1084 */             String s = obj.toString();
/* 1085 */             String reportStr = pseudoPath + " P1 " + s + " P2 " + currentView.m_name;
/* 1086 */             ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "identical path constructed " + reportStr, null);
/*      */ 
/* 1088 */             if (!SharedObjects.getEnvValueAsBoolean("SchemaAllowDuplicatedPathCreation", false))
/*      */             {
/* 1090 */               String msg = LocaleUtils.encodeMessage("csSchemaDuplicatePathPublished", null, new String[] { s, currentView.m_name, parentValuesArray[0] });
/*      */ 
/* 1092 */               throw new ServiceException(msg);
/*      */             }
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1098 */           processViewRelationshipPath(processedPaths, binder, finalView, nextNode, thisResultSet, currentView, currentRelationship);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1106 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "recursion detected on " + processedPath.toString(), null);
/*      */       }
/* 1036 */       thisResultSet.next();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void publishViewRelationshipFragment(DataBinder binder, ResultSet viewValues, SchemaViewData childView, SchemaRelationData relationship, String[] parentFieldsArray, String[] parentValuesArray, String encodedParentValue, String parentInternalValue)
/*      */     throws IOException, DataException, ServiceException
/*      */   {
/* 1119 */     binder.putLocal("RelationName", relationship.m_name);
/* 1120 */     binder.addResultSet(childView.m_canonicalName, viewValues);
/* 1121 */     binder.putLocal("PublishedTableName", childView.m_canonicalName);
/* 1122 */     binder.putLocal("TheParentColumn", parentFieldsArray[0]);
/* 1123 */     if (parentValuesArray[0] == null)
/*      */     {
/* 1125 */       parentValuesArray[0] = "";
/*      */     }
/* 1127 */     binder.putLocal("ParentValue", parentValuesArray[0]);
/* 1128 */     if (parentInternalValue != null)
/*      */     {
/* 1130 */       binder.putLocal("ParentInternalValue", parentInternalValue);
/*      */     }
/*      */     else
/*      */     {
/* 1134 */       binder.putLocal("ParentInternalValue", "");
/*      */     }
/* 1136 */     prepareBinderForPublishing(childView, binder);
/* 1137 */     String encodedViewName = StringUtils.encodeJavascriptFilename(childView.m_name);
/*      */ 
/* 1139 */     String encodedRelationshipName = StringUtils.encodeJavascriptFilename(relationship.m_name);
/*      */ 
/* 1141 */     if (encodedParentValue == null)
/*      */     {
/* 1143 */       encodedParentValue = StringUtils.encodeJavascriptFilename(parentValuesArray[0]);
/*      */     }
/*      */ 
/* 1146 */     binder.putLocal("viewPath", encodedViewName + "/" + encodedRelationshipName + "/" + encodedParentValue);
/*      */ 
/* 1148 */     String scriptDir = "views/" + encodedViewName + "/" + encodedRelationshipName;
/*      */ 
/* 1153 */     String pathBase = scriptDir + "/" + encodedParentValue;
/* 1154 */     String path = pathBase + this.m_defaultExtension;
/* 1155 */     announceFile(path);
/* 1156 */     binder.putLocal("ScriptFile", path);
/* 1157 */     int rc = PluginFilters.filter("schemaPublishViewFragment", this.m_workspace, binder, this.m_context);
/*      */ 
/* 1159 */     switch (rc)
/*      */     {
/*      */     case 1:
/* 1162 */       if (FileUtils.checkFile(path, true, false) == 0)
/*      */       {
/*      */         break label407;
/*      */       }
/*      */ 
/* 1165 */       String msg = LocaleUtils.encodeMessage("csSchemaFilterFailedToCompleteOperation", null, "schemaPublishViewFragment");
/*      */ 
/* 1168 */       throw new IOException(msg);
/*      */     case -1:
/* 1172 */       ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "schemaPublishViewFragment returned ABORT. Continuing.", null);
/*      */     case 0:
/*      */     }
/*      */ 
/* 1177 */     SchemaOutputObject output = openWriter(path, 0);
/* 1178 */     buildJsFile("SCHEMA_VIEW_JS_FRAGMENT", binder, output.m_writer, this.m_context);
/*      */ 
/* 1180 */     finalizeWriter(output, true);
/*      */ 
/* 1184 */     if (viewValues.isRowPresent())
/*      */     {
/* 1186 */       label407: String msg = LocaleUtils.encodeMessage("csSchemaFragmentPublishingIncomplete", null, childView.m_name, parentValuesArray[0]);
/*      */ 
/* 1189 */       releaseConnection(false);
/* 1190 */       throw new DataException(msg);
/*      */     }
/* 1192 */     releaseConnection(false);
/*      */   }
/*      */ 
/*      */   public void prepareBinderForPublishing(SchemaViewData view, DataBinder binder)
/*      */   {
/* 1197 */     binder.removeLocal("viewPath");
/*      */   }
/*      */ 
/*      */   protected boolean finalizePublishing()
/*      */   {
/* 1202 */     ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "starting finalizePublishing()", null);
/*      */ 
/* 1205 */     if (this.m_buildDigestFile)
/*      */     {
/*      */       try
/*      */       {
/* 1209 */         buildDigestFile();
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 1213 */         ProgressStateUtils.reportError(this.m_progress, "schemapublisher", e, "csSchemaPublishError", new Object[] { this.m_defaultExtension });
/*      */       }
/*      */     }
/*      */ 
/* 1217 */     if (this.m_verifyPublishing)
/*      */     {
/* 1219 */       verifyPublishing(this.m_tempDir);
/*      */     }
/* 1221 */     if ((this.m_publishOperation.equals("base")) && (FileUtils.checkFile(this.m_targetDir, false, true) == 0))
/*      */     {
/*      */       try
/*      */       {
/* 1227 */         FileUtils.renameFile(this.m_tempDir + "base" + this.m_defaultExtension, this.m_targetDir + "base" + this.m_defaultExtension);
/*      */ 
/* 1229 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 2, "published base" + this.m_defaultExtension + " successfully", null);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1235 */         ProgressStateUtils.reportError(this.m_progress, "schemapublisher", e, "csSchemaPublishError", new Object[] { this.m_defaultExtension });
/*      */       }
/*      */ 
/* 1238 */       return true;
/*      */     }
/*      */ 
/* 1241 */     boolean firstDone = false;
/* 1242 */     boolean secondDone = false;
/* 1243 */     boolean thirdDone = false;
/*      */ 
/* 1245 */     File backupDir = new File(this.m_targetBackupDir);
/* 1246 */     File targetDir = new File(this.m_targetDir);
/* 1247 */     File tempDir = new File(this.m_tempDir);
/* 1248 */     File temp2Dir = new File(this.m_temp2Dir);
/*      */     try
/*      */     {
/* 1252 */       if ((backupDir.exists()) && (targetDir.exists()))
/*      */       {
/*      */         try
/*      */         {
/* 1256 */           FileUtils.deleteDirectory(backupDir, true);
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/* 1260 */           ProgressStateUtils.reportError(this.m_progress, "schemapublisher", e, "csSchemaPublishingFinalizationDeleteError", new Object[] { backupDir.getAbsolutePath() });
/*      */ 
/* 1262 */           int i = 0;
/*      */ 
/* 1396 */           if ((firstDone) && (!thirdDone) && (backupDir.exists()))
/*      */           {
/* 1398 */             if (!backupDir.renameTo(targetDir))
/*      */             {
/* 1400 */               ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "unable to restore backup schema directory", null);
/*      */             }
/*      */ 
/*      */           }
/* 1406 */           else if (this.m_verifyPublishing)
/*      */           {
/* 1408 */             verifyPublishing(this.m_targetDir); } return i;
/*      */         }
/*      */       }
/* 1265 */       if (targetDir.exists())
/*      */       {
/* 1267 */         firstDone = targetDir.renameTo(backupDir);
/* 1268 */         if ((firstDone) || (backupDir.exists()))
/*      */         {
/* 1270 */           firstDone = true;
/*      */         }
/*      */         else
/*      */         {
/* 1274 */           int retryCount = 2;
/* 1275 */           if (EnvUtils.getOSFamily().equals("windows"))
/*      */           {
/* 1277 */             retryCount = 4;
/*      */           }
/*      */           int j;
/* 1279 */           int j = 200;
/* 1280 */           while (retryCount-- > 0)
/*      */           {
/* 1282 */             SystemUtils.sleep(j);
/*      */             int k;
/* 1283 */             j *= 2;
/* 1284 */             firstDone = targetDir.renameTo(backupDir);
/* 1285 */             if ((firstDone) || (backupDir.exists()))
/*      */             {
/* 1287 */               firstDone = true;
/* 1288 */               break;
/*      */             }
/* 1290 */             ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "unable to do first rename from " + targetDir.getAbsolutePath() + " to " + backupDir.getAbsolutePath() + ";  " + retryCount + " retries left", null);
/*      */           }
/*      */ 
/* 1297 */           if (!firstDone)
/*      */           {
/* 1299 */             ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, null, "csSchemaPublishingFinalizationRenameError", new Object[] { targetDir.getAbsolutePath(), backupDir.getAbsolutePath() });
/*      */ 
/* 1302 */             int l = 0;
/*      */             return l;
/*      */           }
/*      */         }
/*      */       }
/* 1307 */       if (EnvUtils.isFamily("windows"))
/*      */       {
/* 1309 */         long interval = this.m_renameRetryInitialInterval;
/*      */         int i1;
/* 1310 */         for (int i1 = 0; i1 < this.m_renameRetryIterations; ++i1)
/*      */         {
/* 1312 */           if (temp2Dir.exists())
/*      */           {
/*      */             try
/*      */             {
/* 1316 */               FileUtils.deleteDirectory(temp2Dir, true);
/*      */             }
/*      */             catch (ServiceException e)
/*      */             {
/* 1320 */               ProgressStateUtils.reportError(this.m_progress, "schemapublisher", e, "csSchemaPublishingFinalizationDeleteError", new Object[] { temp2Dir.getAbsolutePath() });
/*      */ 
/* 1323 */               int i3 = 0;
/*      */ 
/* 1396 */               if ((firstDone) && (!thirdDone) && (backupDir.exists()))
/*      */               {
/* 1398 */                 if (!backupDir.renameTo(targetDir))
/*      */                 {
/* 1400 */                   ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "unable to restore backup schema directory", null);
/*      */                 }
/*      */ 
/*      */               }
/* 1406 */               else if (this.m_verifyPublishing)
/*      */               {
/* 1408 */                 verifyPublishing(this.m_targetDir); } return i3;
/*      */             }
/*      */           }
/* 1326 */           if (!tempDir.exists())
/*      */           {
/* 1328 */             ProgressStateUtils.reportError(this.m_progress, "schemapublisher", null, "csSchemaTempDirIsMissing", new Object[] { this.m_tempDir });
/*      */ 
/* 1330 */             e = 0;
/*      */             return e;
/*      */           }
/* 1333 */           secondDone = tempDir.renameTo(temp2Dir);
/* 1334 */           if ((secondDone) || (temp2Dir.exists()))
/*      */           {
/* 1336 */             secondDone = true;
/* 1337 */             break;
/*      */           }
/* 1339 */           ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "unable to rename from " + tempDir.getAbsolutePath() + " to " + temp2Dir.getAbsolutePath() + ", waiting " + interval, null);
/*      */ 
/* 1342 */           SystemUtils.sleep(interval);
/* 1343 */           interval *= 2L;
/*      */         }
/* 1345 */         if (secondDone)
/*      */         {
/* 1347 */           interval = this.m_renameRetryInitialInterval;
/*      */           int i2;
/* 1348 */           for (int i2 = 0; i2 < this.m_renameRetryIterations; ++i2)
/*      */           {
/* 1350 */             thirdDone = temp2Dir.renameTo(targetDir);
/* 1351 */             if (targetDir.exists())
/*      */             {
/* 1353 */               thirdDone = true;
/* 1354 */               break;
/*      */             }
/* 1356 */             SystemUtils.sleep(interval);
/* 1357 */             interval *= 2L;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1368 */         thirdDone = secondDone = tempDir.renameTo(targetDir);
/*      */       }
/*      */ 
/* 1372 */       if (!thirdDone)
/*      */       {
/* 1374 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, null, "csSchemaPublishingFinalizationRenameError", new Object[] { tempDir.getAbsolutePath(), targetDir.getAbsolutePath() });
/*      */ 
/* 1377 */         Report.warning(null, null, "csSchemaPublishingFinalizationRenameError", new Object[] { tempDir.getAbsolutePath(), targetDir.getAbsolutePath() });
/*      */ 
/* 1380 */         if (SharedObjects.getEnvValueAsBoolean("SchemaAllowRecursiveFileMove", EnvUtils.isFamily("windows")))
/*      */         {
/* 1383 */           failList = new IdcVector();
/* 1384 */           ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "trying to recursively move files", null);
/*      */ 
/* 1386 */           thirdDone = recursiveMove(tempDir, targetDir, failList);
/* 1387 */           ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "recursively move files had " + failList.size() + " failures.  rc is " + thirdDone, null);
/*      */         }
/*      */ 
/* 1391 */         Vector failList = thirdDone;
/*      */         return failList;
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 1396 */       if ((firstDone) && (!thirdDone) && (backupDir.exists()))
/*      */       {
/* 1398 */         if (!backupDir.renameTo(targetDir))
/*      */         {
/* 1400 */           ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "unable to restore backup schema directory", null);
/*      */         }
/*      */ 
/*      */       }
/* 1406 */       else if (this.m_verifyPublishing)
/*      */       {
/* 1408 */         verifyPublishing(this.m_targetDir);
/*      */       }
/*      */     }
/*      */ 
/* 1412 */     return true;
/*      */   }
/*      */ 
/*      */   public void buildDigestFile()
/*      */     throws IOException
/*      */   {
/* 1422 */     ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "building digest file", null);
/*      */ 
/* 1424 */     String filePath = createReportFilePath("digests");
/* 1425 */     Writer w = null;
/*      */     try
/*      */     {
/* 1428 */       w = FileUtils.openDataWriterEx(new FileOutputStream(filePath), null, 1);
/*      */ 
/* 1431 */       buildDigestFile(w, this.m_fileList);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1435 */       ProgressStateUtils.reportError(this.m_progress, "schemapublisher", e, "csUnableToBuildDigestFile", new Object[] { filePath });
/*      */     }
/*      */     finally
/*      */     {
/* 1440 */       FileUtils.closeObjectEx(w);
/*      */     }
/* 1442 */     ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "built digest file", null);
/*      */   }
/*      */ 
/*      */   public String createReportFilePath(String prefix)
/*      */   {
/* 1448 */     SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
/* 1449 */     Date now = new Date();
/* 1450 */     String dateString = fmt.format(now);
/* 1451 */     String path = this.m_publishDirectory + "/" + prefix + "-" + dateString + ".txt";
/* 1452 */     path = FileUtils.fileSlashes(path);
/* 1453 */     return path;
/*      */   }
/*      */ 
/*      */   public void buildDigestFile(Writer w, Vector list)
/*      */     throws IOException
/*      */   {
/* 1463 */     int size = list.size();
/* 1464 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1466 */       Object[] entry = (Object[])(Object[])list.elementAt(i);
/* 1467 */       String path = (String)entry[0];
/* 1468 */       byte[] digest = (byte[])(byte[])entry[1];
/* 1469 */       IdcStringBuilder buf = new IdcStringBuilder();
/* 1470 */       for (int j = 0; j < digest.length; ++j)
/*      */       {
/* 1472 */         NumberUtils.appendHexByte(buf, digest[j]);
/*      */       }
/* 1474 */       String digestString = buf.toString();
/* 1475 */       String text = LocaleResources.getString("csDigestFileEntry", null, new Object[] { "SHA1", path, digestString.toLowerCase() });
/*      */ 
/* 1477 */       if (!text.endsWith("\n"))
/*      */       {
/* 1479 */         text = text + "\n";
/*      */       }
/* 1481 */       w.write(text);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void verifyPublishing(String directory)
/*      */   {
/* 1487 */     ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "verifying directory '" + directory + "'", null);
/*      */ 
/* 1489 */     Hashtable wrong = new Hashtable();
/* 1490 */     Hashtable missing = (Hashtable)this.m_fileTable.clone();
/* 1491 */     Hashtable extra = new Hashtable();
/* 1492 */     if (directory.endsWith("/"))
/*      */     {
/* 1494 */       directory = directory.substring(0, directory.length() - 1);
/*      */     }
/* 1496 */     verifyPublishingRecursive(directory, "", this.m_fileTable, wrong, missing, extra);
/*      */ 
/* 1498 */     Writer reportFile = null;
/* 1499 */     String reportFilePath = createReportFilePath("verify");
/*      */     try
/*      */     {
/* 1503 */       String[] labels = { "wrong", "missing", "extra" };
/*      */ 
/* 1507 */       Hashtable[] tables = { wrong, missing, extra };
/*      */ 
/* 1511 */       for (int i = 0; i < tables.length; ++i)
/*      */       {
/* 1513 */         Hashtable theTable = tables[i];
/* 1514 */         String theLabel = labels[i];
/* 1515 */         Enumeration en = theTable.keys();
/* 1516 */         while (en.hasMoreElements())
/*      */         {
/* 1518 */           String path = (String)en.nextElement();
/* 1519 */           if (reportFile == null)
/*      */           {
/* 1521 */             ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "directory '" + directory + "' didn't pass validation", null);
/*      */ 
/* 1524 */             reportFile = FileUtils.openDataWriter(reportFilePath);
/*      */           }
/*      */ 
/* 1527 */           String msg = LocaleResources.getString("csSchemaPublishingValidationError_" + theLabel, null, path);
/*      */ 
/* 1530 */           if (!msg.endsWith("\n"))
/*      */           {
/* 1532 */             msg = msg + "\n";
/*      */           }
/* 1534 */           reportFile.write(msg);
/*      */         }
/*      */       }
/* 1537 */       FileUtils.closeObjectEx(reportFile);
/* 1538 */       reportFile = null;
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1542 */       ProgressStateUtils.reportError(this.m_progress, "schemapublisher", e, "csSchemaPublishingValidationWriteFailure", new Object[] { directory, reportFilePath });
/*      */     }
/*      */     finally
/*      */     {
/* 1547 */       FileUtils.closeObject(reportFile);
/*      */     }
/*      */ 
/* 1550 */     if (reportFile != null)
/*      */     {
/* 1552 */       ProgressStateUtils.reportError(this.m_progress, "schemapublisher", null, "csSchemaPublishingValidationFailed", new Object[] { directory, reportFilePath });
/*      */     }
/*      */ 
/* 1555 */     ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "done verifying directory '" + directory + "'", null);
/*      */   }
/*      */ 
/*      */   public void verifyPublishingRecursive(String directory, String prefix, Hashtable digests, Hashtable wrong, Hashtable missing, Hashtable extra)
/*      */   {
/* 1565 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1567 */       Report.debug("schemapublisher", "verifyPublishingRecursive: " + directory + " " + prefix, null);
/*      */     }
/*      */ 
/* 1570 */     File dir = new File(directory);
/* 1571 */     String[] listing = dir.list();
/* 1572 */     for (int i = 0; i < listing.length; ++i)
/*      */     {
/* 1574 */       String name = listing[i];
/* 1575 */       String path = directory + "/" + name;
/* 1576 */       File file = new File(path);
/* 1577 */       if (file.isDirectory())
/*      */       {
/* 1579 */         verifyPublishingRecursive(path, prefix + name + "/", digests, wrong, missing, extra);
/*      */       }
/*      */       else
/*      */       {
/* 1584 */         String entryPath = prefix + name;
/* 1585 */         Object[] entry = (Object[])(Object[])digests.get(entryPath);
/* 1586 */         if (entry == null)
/*      */         {
/* 1588 */           extra.put(entryPath, entryPath);
/*      */         }
/*      */         else
/*      */         {
/* 1592 */           byte[] correctDigest = (byte[])(byte[])entry[1];
/*      */           byte[] actualDigest;
/*      */           try
/*      */           {
/* 1596 */             actualDigest = computeDigest(path, "SHA1");
/*      */           }
/*      */           catch (Exception e)
/*      */           {
/* 1600 */             if (SystemUtils.m_verbose)
/*      */             {
/* 1602 */               Report.debug("schemapublisher", null, e);
/*      */             }
/* 1604 */             break label305:
/*      */           }
/* 1606 */           missing.remove(entryPath);
/* 1607 */           if (compareByteArrays(correctDigest, actualDigest) == 0)
/*      */             continue;
/* 1609 */           Object[] newEntry = { entryPath, actualDigest };
/*      */ 
/* 1613 */           label305: wrong.put(entryPath, newEntry);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public SchemaOutputObject openWriter(String path, int flags)
/*      */     throws IOException, ServiceException
/*      */   {
/* 1623 */     SchemaOutputObject rc = new SchemaOutputObject();
/*      */ 
/* 1625 */     rc.m_finalPath = path;
/* 1626 */     rc.m_tempPath = (path + ".tmp");
/* 1627 */     String dir = FileUtils.getParent(this.m_tempDir + rc.m_tempPath);
/* 1628 */     FileUtils.checkOrCreateDirectory(dir, 9);
/* 1629 */     File outputFile = new File(this.m_tempDir + rc.m_tempPath);
/* 1630 */     boolean success = false;
/*      */     try
/*      */     {
/* 1633 */       if ((flags & F_NULL_OUTPUT) == 0)
/*      */       {
/* 1635 */         incCounter();
/* 1636 */         rc.m_output = new FileOutputStream(outputFile);
/* 1637 */         if (this.m_computeDigests)
/*      */         {
/*      */           try
/*      */           {
/* 1641 */             rc.m_digester = MessageDigest.getInstance("SHA-1");
/*      */           }
/*      */           catch (NoSuchAlgorithmException e)
/*      */           {
/* 1645 */             IOException exception = new IOException(e.getMessage());
/* 1646 */             SystemUtils.setExceptionCause(exception, e);
/* 1647 */             throw exception;
/*      */           }
/* 1649 */           DigestOutputStream digestOutput = new DigestOutputStream(rc.m_output, rc.m_digester);
/*      */ 
/* 1651 */           rc.m_output = digestOutput;
/*      */         }
/* 1653 */         if (this.m_useGzipFiles)
/*      */         {
/* 1655 */           rc.m_output = new GZIPOutputStream(rc.m_output);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1660 */         rc.m_isNull = true;
/*      */       }
/* 1662 */       OutputStream extraOutput = (OutputStream)this.m_context.getCachedObject("extraOutput");
/*      */ 
/* 1664 */       if (extraOutput != null)
/*      */       {
/* 1666 */         this.m_context.setCachedObject("extraOutput", null);
/* 1667 */         if (rc.m_output != null)
/*      */         {
/* 1669 */           ForkedOutputStream f = new ForkedOutputStream(new OutputStream[] { rc.m_output, extraOutput });
/*      */ 
/* 1671 */           rc.m_output = f;
/* 1672 */           f.m_closeFirstStream = true;
/*      */         }
/*      */         else
/*      */         {
/* 1676 */           rc.m_output = new ForkedOutputStream(new OutputStream[] { extraOutput });
/*      */         }
/*      */       }
/*      */ 
/* 1680 */       if (rc.m_output != null)
/*      */       {
/* 1682 */         rc.m_writer = FileUtils.openDataWriterEx(rc.m_output, "ISO-8859-1", 0);
/*      */       }
/*      */ 
/* 1685 */       success = true;
/*      */     }
/*      */     finally
/*      */     {
/* 1689 */       if (!success)
/*      */       {
/* 1691 */         if ((flags & F_NULL_OUTPUT) == 0)
/*      */         {
/* 1693 */           decCounter();
/*      */         }
/* 1695 */         FileUtils.closeObjectEx(rc.m_writer);
/*      */       }
/*      */     }
/*      */ 
/* 1699 */     return rc;
/*      */   }
/*      */ 
/*      */   public void finalizeWriter(SchemaOutputObject output, boolean doCommit)
/*      */     throws IOException, ServiceException
/*      */   {
/* 1705 */     if (!output.m_isNull)
/*      */     {
/* 1707 */       decCounter();
/*      */     }
/* 1709 */     FileUtils.closeObjectEx(output.m_writer);
/* 1710 */     if ((output.m_isNull) || (!doCommit))
/*      */     {
/* 1712 */       return;
/*      */     }
/*      */ 
/* 1715 */     if ((this.m_computeDigests) && (output.m_digester != null))
/*      */     {
/* 1717 */       byte[] digest = output.m_digester.digest();
/* 1718 */       Object[] data = { output.m_finalPath, digest };
/* 1719 */       this.m_fileTable.put(output.m_finalPath, data);
/* 1720 */       this.m_fileList.addElement(data);
/*      */     }
/*      */ 
/* 1723 */     File finalFile = new File(this.m_tempDir + output.m_finalPath);
/* 1724 */     File tmpFile = new File(this.m_tempDir + output.m_tempPath);
/* 1725 */     String finalPath = finalFile.getAbsolutePath();
/* 1726 */     String tmpPath = tmpFile.getAbsolutePath();
/* 1727 */     if ((EnvUtils.isFamily("windows")) && (this.m_win32DelayRenameInterval > 0L))
/*      */     {
/* 1729 */       SystemUtils.sleep(this.m_win32DelayRenameInterval);
/*      */     }
/* 1731 */     long waitInterval = 200L;
/* 1732 */     int counter = 6;
/* 1733 */     String errMsg = null;
/*      */ 
/* 1735 */     while ((!tmpFile.renameTo(finalFile)) && (--counter > 0))
/*      */     {
/* 1737 */       SystemUtils.sleep(waitInterval);
/* 1738 */       if (finalFile.exists())
/*      */       {
/* 1740 */         if (!tmpFile.exists())
/*      */         {
/* 1742 */           ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "rename success after delay", null); break;
/*      */         }
/*      */ 
/* 1747 */         errMsg = LocaleUtils.encodeMessage("syFileExists", null, finalPath);
/*      */ 
/* 1749 */         break;
/*      */       }
/* 1751 */       if (this.m_nativeUtils != null)
/*      */       {
/* 1753 */         PosixStructStat sb = new PosixStructStat();
/* 1754 */         if ((this.m_nativeUtils.stat(tmpPath, sb) != 0) && (this.m_nativeUtils.stat(finalPath, sb) == 0))
/*      */         {
/* 1757 */           ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "rename success detected with NativeOsUtils", null);
/*      */ 
/* 1759 */           break;
/*      */         }
/*      */       }
/* 1762 */       waitInterval *= 2L;
/*      */     }
/* 1764 */     if ((counter == 0) || (errMsg != null))
/*      */     {
/* 1766 */       String msg = LocaleUtils.encodeMessage("syUnableToRename", errMsg, tmpPath, finalPath);
/*      */ 
/* 1768 */       throw new IOException(msg);
/*      */     }
/*      */ 
/* 1771 */     if ((!SharedObjects.getEnvValueAsBoolean("SchemaPublishLinkIdenticalFiles", true)) || (this.m_publishOperation.equals("base"))) {
/*      */       return;
/*      */     }
/*      */ 
/* 1775 */     String targetPath = this.m_targetDir + output.m_finalPath;
/* 1776 */     for (int t = 0; t < 2; ++t)
/*      */     {
/*      */       boolean isDifferent;
/*      */       try
/*      */       {
/* 1781 */         isDifferent = FileUtils.filesAreDifferent(finalPath, targetPath);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1785 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, null, e);
/*      */ 
/* 1787 */         SystemUtils.sleep(100L);
/* 1788 */         break label649:
/*      */       }
/* 1790 */       if ((isDifferent) || 
/* 1792 */         (this.m_nativeUtils == null))
/*      */         return;
/* 1794 */       FileUtils.renameFile(finalPath, finalPath + ".tmp");
/* 1795 */       if (this.m_nativeUtils.link(targetPath, finalPath) == 0)
/*      */       {
/* 1797 */         FileUtils.deleteFile(finalPath + ".tmp"); return;
/*      */       }
/*      */ 
/* 1801 */       FileUtils.renameFile(finalPath + ".tmp", finalPath);
/* 1802 */       File oldFile = new File(targetPath);
/* 1803 */       long time = oldFile.lastModified();
/* 1804 */       if (!FileUtils.setLastModified(finalPath, time))
/*      */       {
/* 1806 */         ProgressStateUtils.reportProgress(this.m_progress, null, "schemapublisher", 3, "unable to set time on " + finalPath, null);
/*      */       }
/*      */ 
/* 1809 */       label649: return;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void buildJsFile(String page, DataBinder binder, Writer w, ExecutionContext context)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/* 1838 */     context.setCachedObject("DataBinder", binder);
/*      */     try
/*      */     {
/* 1841 */       this.m_utils.mergePage(page, binder, context, w);
/* 1842 */       w.flush();
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/* 1846 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void releaseConnection(boolean forceRelease)
/*      */   {
/* 1857 */     if ((!forceRelease) && (this.m_releaseConnectionCount++ < this.m_releaseConnectionCountLimit))
/*      */     {
/* 1860 */       return;
/*      */     }
/* 1862 */     long now = System.currentTimeMillis();
/* 1863 */     long delta = now - this.m_lastReleaseConnectionTimestamp;
/* 1864 */     if ((!forceRelease) && (delta < this.m_releaseConnectionMinInterval))
/*      */     {
/* 1867 */       return;
/*      */     }
/* 1869 */     if (this.m_workspace != null)
/*      */     {
/* 1871 */       this.m_workspace.releaseConnection();
/*      */     }
/* 1873 */     this.m_releaseConnectionCount = 0;
/* 1874 */     this.m_lastReleaseConnectionTimestamp = now;
/*      */   }
/*      */ 
/*      */   public static byte[] computeDigest(String path, String algo)
/*      */     throws IOException, NoSuchAlgorithmException
/*      */   {
/* 1880 */     InputStream is = null;
/*      */     try
/*      */     {
/* 1883 */       MessageDigest digester = MessageDigest.getInstance(algo);
/* 1884 */       is = new FileInputStream(path);
/* 1885 */       int count = 0;
/* 1886 */       byte[] buf = new byte[8192];
/* 1887 */       while ((count = is.read(buf)) > 0)
/*      */       {
/* 1889 */         digester.update(buf, 0, count);
/*      */       }
/* 1891 */       byte[] arrayOfByte1 = digester.digest();
/*      */ 
/* 1895 */       return arrayOfByte1; } finally { FileUtils.closeObjectEx(is); }
/*      */ 
/*      */   }
/*      */ 
/*      */   public void incCounter()
/*      */   {
/* 1901 */     this.m_openFileCounter += 1;
/* 1902 */     if (!SystemUtils.m_verbose)
/*      */       return;
/* 1904 */     Report.debug("schemapublisher", "incCounter: open files = " + this.m_openFileCounter, null);
/*      */   }
/*      */ 
/*      */   public void decCounter()
/*      */   {
/* 1910 */     this.m_openFileCounter -= 1;
/* 1911 */     if (!SystemUtils.m_verbose)
/*      */       return;
/* 1913 */     Report.debug("schemapublisher", "decCounter: open files = " + this.m_openFileCounter, null);
/*      */   }
/*      */ 
/*      */   public static int compareByteArrays(byte[] b1, byte[] b2)
/*      */   {
/* 1919 */     if (b1.length > b2.length)
/*      */     {
/* 1921 */       return 2;
/*      */     }
/* 1923 */     if (b2.length > b1.length)
/*      */     {
/* 1925 */       return -2;
/*      */     }
/*      */ 
/* 1928 */     for (int i = 0; i < b1.length; ++i)
/*      */     {
/* 1930 */       int diff = b1[i] - b2[i];
/* 1931 */       if (diff > 0)
/*      */       {
/* 1933 */         return 1;
/*      */       }
/* 1935 */       if (diff < 0)
/*      */       {
/* 1937 */         return -1;
/*      */       }
/*      */     }
/* 1940 */     return 0;
/*      */   }
/*      */ 
/*      */   public boolean recursiveMove(File src, File dst, Vector failList)
/*      */   {
/* 1945 */     boolean success = true;
/* 1946 */     if (src.isDirectory())
/*      */     {
/* 1948 */       String[] list = src.list();
/* 1949 */       String srcDir = FileUtils.directorySlashes(src.getAbsolutePath());
/* 1950 */       String dstPath = FileUtils.directorySlashes(dst.getAbsolutePath());
/* 1951 */       if (!dst.exists())
/*      */       {
/* 1953 */         dst.mkdir();
/*      */       }
/* 1955 */       for (int i = 0; i < list.length; ++i)
/*      */       {
/* 1957 */         File newSrc = new File(srcDir + list[i]);
/* 1958 */         File newDst = new File(dstPath + list[i]);
/* 1959 */         boolean rc = recursiveMove(newSrc, newDst, failList);
/* 1960 */         if (rc)
/*      */           continue;
/* 1962 */         success = false;
/*      */       }
/*      */ 
/* 1965 */       src.delete();
/*      */     }
/*      */     else
/*      */     {
/*      */       String dstPath;
/* 1970 */       if (dst.isDirectory())
/*      */       {
/* 1972 */         String dstPath = dst.getAbsolutePath();
/* 1973 */         dstPath = FileUtils.directorySlashes(dstPath) + src.getName();
/*      */       }
/*      */       else
/*      */       {
/* 1977 */         dstPath = dst.getAbsolutePath();
/* 1978 */         if (dst.exists())
/*      */         {
/* 1980 */           dst.delete();
/*      */         }
/*      */       }
/* 1983 */       File dstFile = new File(dstPath);
/* 1984 */       boolean rc = src.renameTo(dstFile);
/* 1985 */       if ((!rc) && (src.exists()))
/*      */       {
/*      */         try
/*      */         {
/* 1989 */           FileUtils.copyFile(src.getAbsolutePath(), dstPath);
/* 1990 */           src.delete();
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/* 1994 */           Object[] error = { src, dstFile, e };
/* 1995 */           failList.addElement(error);
/* 1996 */           success = false;
/*      */         }
/*      */       }
/*      */     }
/* 2000 */     return success;
/*      */   }
/*      */ 
/*      */   protected void announceFile(String path)
/*      */   {
/* 2005 */     if (!SystemUtils.m_verbose)
/*      */       return;
/* 2007 */     Report.debug("schemapublisher", "writing file \"" + path + "\"", null);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2014 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.StandardSchemaPublisher
 * JD-Core Version:    0.5.4
 */