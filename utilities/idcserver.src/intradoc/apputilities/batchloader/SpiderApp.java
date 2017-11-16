/*      */ package intradoc.apputilities.batchloader;
/*      */ 
/*      */ import intradoc.apps.shared.AppLauncher;
/*      */ import intradoc.apps.shared.StandAloneApp;
/*      */ import intradoc.common.AppObjectRepository;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.gui.AppFrameHelper;
/*      */ import intradoc.gui.CustomLabel;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.utils.SystemPropertiesEditor;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.Users;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.RandomAccessFile;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.swing.JButton;
/*      */ 
/*      */ public class SpiderApp
/*      */ {
/*      */   protected SpiderFrame m_frame;
/*      */   protected StandAloneApp m_standAlone;
/*      */   protected ExecutionContext m_cxt;
/*      */   protected SystemPropertiesEditor m_propLoader;
/*      */   protected Properties m_idcProperties;
/*      */   protected String m_workingDir;
/*      */   protected DataBinder m_spiderBinder;
/*      */   protected String m_externalCollection;
/*      */   protected String m_spiderDirectory;
/*      */   protected String m_spiderFileFilter;
/*      */   protected String m_spiderMapping;
/*      */   protected String m_metaDataUsage;
/*      */   protected String m_usageFields;
/*      */   protected String m_systemEncoding;
/*      */   protected String m_spiderBatchFileName;
/*      */   protected String m_errorFileName;
/*      */   protected boolean m_doBackground;
/*   67 */   protected boolean m_doExternal = false;
/*   68 */   protected boolean m_spiderStarted = false;
/*   69 */   protected boolean m_isProcessingFiles = false;
/*   70 */   protected boolean m_isFirstRecord = false;
/*   71 */   protected boolean m_spiderExcludeFilter = false;
/*   72 */   protected boolean m_isExiting = false;
/*   73 */   protected boolean m_initialized = false;
/*      */ 
/*   76 */   protected long m_fileTotal = 0L;
/*   77 */   protected long m_fileCounter = 0L;
/*   78 */   protected long m_errorCounter = 0L;
/*   79 */   protected long m_errorMax = 50L;
/*   80 */   protected long m_successCounter = 0L;
/*      */   protected Vector m_mapList;
/*      */   protected Properties m_mapProps;
/*      */   protected DataResultSet m_mapSet;
/*   86 */   protected int m_mapFieldIndex = -1;
/*   87 */   protected int m_mapValueIndex = -1;
/*   88 */   protected String m_mapDir = null;
/*   89 */   protected String m_mapListFileName = "mapping.hda";
/*   90 */   protected String m_mapDefaultFileName = "default.hda";
/*   91 */   protected String m_mapListTableName = "SpiderMappingList";
/*   92 */   protected String m_mapTableName = "SpiderMapping";
/*   93 */   protected String m_oldMapListTableName = "ExternalMappings";
/*   94 */   protected String[] m_oldMapListFields = { "name", "filename" };
/*   95 */   protected String[] m_mapListFields = { "mapName", "mapDescription" };
/*   96 */   protected String[] m_mapFields = { "mapField", "mapValue" };
/*      */   protected Vector m_collectionList;
/*      */   protected Properties m_collectionProps;
/*      */   protected String m_collectionLocation;
/*      */   protected String m_relativeWebRoot;
/*      */   protected String m_physicalFileRoot;
/*  104 */   protected String m_collDir = null;
/*  105 */   protected String m_collFileName = "search_collections.hda";
/*  106 */   protected String m_collTableName = "SearchCollections";
/*      */   protected Properties m_filterExtList;
/*      */   protected Properties m_filterNameList;
/*      */   protected Properties m_usageList;
/*      */ 
/*      */   public SpiderApp(StandAloneApp standAlone, boolean doBackground)
/*      */   {
/*  117 */     this.m_standAlone = standAlone;
/*  118 */     this.m_doBackground = doBackground;
/*      */ 
/*  120 */     this.m_propLoader = new SystemPropertiesEditor();
/*      */ 
/*  122 */     this.m_workingDir = FileUtils.getWorkingDir();
/*      */ 
/*  124 */     this.m_systemEncoding = DataSerializeUtils.getSystemEncoding();
/*      */   }
/*      */ 
/*      */   public void init()
/*      */     throws ServiceException
/*      */   {
/*  130 */     String[] args = (String[])(String[])AppObjectRepository.getObject("CommandLine");
/*      */     try
/*      */     {
/*  135 */       readIntradocCfgFile();
/*  136 */       readCommandLine(args);
/*  137 */       initProperties();
/*      */ 
/*  139 */       initMappings();
/*  140 */       loadCollections();
/*  141 */       initUsages();
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  145 */       throw new ServiceException(e);
/*      */     }
/*  147 */     this.m_initialized = true;
/*      */   }
/*      */ 
/*      */   protected void initProperties()
/*      */   {
/*  152 */     if (this.m_idcProperties.getProperty("SpiderDirectory") == null)
/*      */     {
/*  154 */       this.m_idcProperties.put("SpiderDirectory", "");
/*      */     }
/*      */ 
/*  157 */     if (this.m_idcProperties.getProperty("SpiderBatchFile") == null)
/*      */     {
/*  159 */       this.m_idcProperties.put("SpiderBatchFile", "");
/*      */     }
/*      */ 
/*  162 */     if (this.m_idcProperties.getProperty("SpiderMapping") == null)
/*      */     {
/*  164 */       this.m_idcProperties.put("SpiderMapping", "");
/*      */     }
/*      */ 
/*  167 */     if (this.m_idcProperties.getProperty("ExternalCollection") == null)
/*      */     {
/*  169 */       this.m_idcProperties.put("ExternalCollection", "");
/*      */     }
/*      */ 
/*  172 */     if (this.m_idcProperties.getProperty("MetaDataUsage") == null)
/*      */     {
/*  174 */       this.m_idcProperties.put("MetaDataUsage", "");
/*      */     }
/*      */ 
/*  177 */     if (this.m_idcProperties.getProperty("UsageFields") == null)
/*      */     {
/*  179 */       this.m_idcProperties.put("UsageFields", "");
/*      */     }
/*      */ 
/*  182 */     if (this.m_idcProperties.getProperty("SpiderExcludeFilter") == null)
/*      */     {
/*  184 */       this.m_idcProperties.put("SpiderExcludeFilter", "0");
/*      */     }
/*      */ 
/*  187 */     if (this.m_idcProperties.getProperty("SpiderFileFilter") != null)
/*      */       return;
/*  189 */     this.m_idcProperties.put("SpiderFileFilter", "");
/*      */   }
/*      */ 
/*      */   public void readIntradocCfgFile()
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  198 */       String keys = "SpiderDirectory,SpiderBatchFile,SpiderMapping,ExternalCollection,MetaDataUsage,UsageFields,SpiderExcludeFilter,SpiderFileFilter,BatchLoaderUserName";
/*      */ 
/*  201 */       this.m_propLoader.addKeys(keys, null);
/*      */ 
/*  204 */       this.m_propLoader.initIdc();
/*  205 */       this.m_idcProperties = this.m_propLoader.getIdcProperties();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  209 */       throw new ServiceException(LocaleUtils.encodeMessage("csBatchLoaderFileReadError", e.getMessage()));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void writeIntradocCfgFile()
/*      */     throws ServiceException
/*      */   {
/*  216 */     this.m_idcProperties.put("BatchLoaderUserName", AppLauncher.getUser());
/*      */ 
/*  218 */     String filePath = SharedObjects.getEnvironmentValue("BinDir") + "intradoc.cfg";
/*  219 */     FileUtils.validatePath(filePath, IdcMessageFactory.lc("csBatchLoaderConfigFileError", new Object[0]), 3);
/*      */ 
/*  222 */     this.m_propLoader.mergePropertyValuesEx(this.m_idcProperties, null, true);
/*  223 */     this.m_propLoader.saveIdc();
/*      */   }
/*      */ 
/*      */   public void readCommandLine(String[] args)
/*      */   {
/*  229 */     int numArgs = args.length;
/*  230 */     for (int i = 0; i < numArgs; ++i)
/*      */     {
/*  232 */       String tempStr = args[i].toUpperCase();
/*      */ 
/*  235 */       if (tempStr.startsWith("/SPIDER")) continue; if (tempStr.startsWith("-SPIDER"))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  241 */       if (tempStr.startsWith("/Q")) continue; if (tempStr.startsWith("-Q"))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  247 */       if ((tempStr.startsWith("/LMC")) || (tempStr.startsWith("-LMC")))
/*      */       {
/*  249 */         this.m_doExternal = true;
/*      */       }
/*      */       else
/*      */       {
/*  253 */         boolean valueInNextArg = false;
/*  254 */         boolean validArg = false;
/*  255 */         int argLength = args[i].length();
/*  256 */         String value = "";
/*  257 */         if (argLength <= 2)
/*      */         {
/*  259 */           if (i < numArgs - 1)
/*      */           {
/*  261 */             value = args[(i + 1)];
/*  262 */             valueInNextArg = true;
/*      */           }
/*      */ 
/*      */         }
/*      */         else {
/*  267 */           value = args[i].substring(2, argLength);
/*      */         }
/*      */ 
/*  271 */         if ((tempStr.startsWith("-D")) || (tempStr.startsWith("/D")))
/*      */         {
/*  273 */           this.m_idcProperties.put("SpiderDirectory", value);
/*  274 */           validArg = true;
/*      */         }
/*  277 */         else if ((tempStr.startsWith("-N")) || (tempStr.startsWith("/N")))
/*      */         {
/*  279 */           this.m_idcProperties.put("SpiderBatchFile", value);
/*  280 */           validArg = true;
/*      */         }
/*  283 */         else if ((tempStr.startsWith("-M")) || (tempStr.startsWith("/M")))
/*      */         {
/*  285 */           this.m_idcProperties.put("SpiderMapping", value);
/*  286 */           validArg = true;
/*      */         }
/*  289 */         else if ((tempStr.startsWith("-C")) || (tempStr.startsWith("/C")))
/*      */         {
/*  291 */           this.m_idcProperties.put("ExternalCollection", value);
/*  292 */           validArg = true;
/*      */         }
/*  295 */         else if ((tempStr.startsWith("-E")) || (tempStr.startsWith("/E")))
/*      */         {
/*  297 */           this.m_idcProperties.put("SpiderExcludeFilter", "1");
/*  298 */           this.m_idcProperties.put("SpiderFileFilter", value);
/*  299 */           validArg = true;
/*      */         }
/*  302 */         else if ((tempStr.startsWith("-I")) || (tempStr.startsWith("/I")))
/*      */         {
/*  304 */           this.m_idcProperties.put("SpiderExcludeFilter", "0");
/*  305 */           this.m_idcProperties.put("SpiderFileFilter", value);
/*  306 */           validArg = true;
/*      */         }
/*  309 */         else if ((tempStr.startsWith("-U")) || (tempStr.startsWith("/U")))
/*      */         {
/*  311 */           this.m_idcProperties.put("MetaDataUsage", value);
/*  312 */           validArg = true;
/*      */         }
/*  315 */         else if ((tempStr.startsWith("-F")) || (tempStr.startsWith("/F")))
/*      */         {
/*  317 */           this.m_idcProperties.put("UsageFields", value);
/*  318 */           validArg = true;
/*      */         }
/*      */ 
/*  322 */         if ((!valueInNextArg) || (!validArg))
/*      */           continue;
/*  324 */         ++i;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void dispose()
/*      */   {
/*  331 */     System.exit(0);
/*      */   }
/*      */ 
/*      */   public void createBatchFile() throws DataException, ServiceException
/*      */   {
/*  336 */     if (!this.m_initialized)
/*      */     {
/*  338 */       init();
/*      */     }
/*      */ 
/*  342 */     if (!retrieveProperties())
/*      */     {
/*  344 */       if (this.m_doBackground)
/*      */       {
/*  346 */         dispose();
/*      */       }
/*  348 */       return;
/*      */     }
/*      */ 
/*  352 */     if (this.m_doBackground)
/*      */     {
/*  354 */       String user = this.m_idcProperties.getProperty("BatchLoaderUserName");
/*  355 */       if ((user != null) && (this.m_standAlone != null))
/*      */       {
/*  357 */         Users users = (Users)SharedObjects.getTable("Users");
/*  358 */         UserData userData = users.getLocalUserData(user);
/*  359 */         if (userData != null)
/*      */         {
/*  361 */           this.m_standAlone.setupUserAccess(userData);
/*  362 */           this.m_cxt = this.m_standAlone.getUserContext();
/*      */         }
/*      */         else
/*      */         {
/*  366 */           this.m_standAlone.setUser(user);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  371 */         displayError("!csBatchLoaderUsernameReq");
/*  372 */         dispose();
/*  373 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  377 */     Runnable bg = new Runnable()
/*      */     {
/*      */       public void run()
/*      */       {
/*      */         try
/*      */         {
/*  384 */           if (SpiderApp.this.m_doExternal)
/*      */           {
/*  386 */             SpiderApp.this.readSummaryValues();
/*      */           }
/*      */ 
/*  390 */           SpiderApp.this.setFileTotal();
/*      */ 
/*  392 */           if (SpiderApp.this.m_fileTotal <= 0L)
/*      */           {
/*  394 */             String errMsg = LocaleResources.getString("csSpiderNoFilterMatch", SpiderApp.this.m_cxt);
/*  395 */             SpiderApp.this.displayError(errMsg);
/*      */           }
/*      */           else
/*      */           {
/*  400 */             SpiderApp.this.processFiles();
/*      */ 
/*  403 */             if (SpiderApp.this.m_doExternal)
/*      */             {
/*  405 */               SpiderApp.this.saveSummaryValues();
/*      */             }
/*      */ 
/*  408 */             SpiderApp.this.displayFinalMessage();
/*      */           }
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  413 */           SpiderApp.this.m_spiderStarted = false;
/*  414 */           SpiderApp.this.m_isExiting = false;
/*  415 */           SpiderApp.this.displayError(e.getMessage());
/*      */         }
/*      */         finally
/*      */         {
/*  419 */           if (SpiderApp.this.m_doBackground)
/*      */           {
/*  421 */             SpiderApp.this.dispose();
/*      */           }
/*      */           else
/*      */           {
/*  425 */             SpiderApp.this.m_frame.reportProgress(1, " ", 0.0F, 100.0F);
/*  426 */             SpiderApp.this.m_frame.m_buildBtn.setEnabled(true);
/*      */           }
/*  428 */           SpiderApp.this.m_spiderStarted = false;
/*  429 */           SpiderApp.this.m_isExiting = false;
/*      */         }
/*      */       }
/*      */     };
/*  434 */     setPropertyValues();
/*      */ 
/*  437 */     setMappingProperties();
/*      */ 
/*  440 */     setFilterLists();
/*      */ 
/*  443 */     if (this.m_doExternal)
/*      */     {
/*  445 */       setCollectionPathValues();
/*      */     }
/*      */ 
/*  449 */     this.m_spiderBinder = new DataBinder(SharedObjects.getSecureEnvironment());
/*      */ 
/*  451 */     if (this.m_doBackground)
/*      */     {
/*  453 */       this.m_spiderBinder.putLocal("dUser", this.m_standAlone.getUser());
/*      */     }
/*      */     else
/*      */     {
/*  457 */       this.m_spiderBinder.putLocal("dUser", AppLauncher.getUser());
/*      */     }
/*      */ 
/*  461 */     if (!this.m_doBackground)
/*      */     {
/*  463 */       this.m_frame.m_buildBtn.setEnabled(false);
/*      */     }
/*      */ 
/*  467 */     this.m_spiderStarted = true;
/*  468 */     this.m_isExiting = false;
/*      */ 
/*  471 */     Thread bgThread = new Thread(bg);
/*  472 */     bgThread.start();
/*      */   }
/*      */ 
/*      */   protected void setPropertyValues()
/*      */   {
/*  478 */     if (this.m_doExternal)
/*      */     {
/*  480 */       this.m_externalCollection = this.m_idcProperties.getProperty("ExternalCollection");
/*  481 */       this.m_metaDataUsage = this.m_idcProperties.getProperty("MetaDataUsage");
/*  482 */       this.m_usageFields = this.m_idcProperties.getProperty("UsageFields");
/*      */     }
/*      */ 
/*  485 */     this.m_spiderDirectory = this.m_idcProperties.getProperty("SpiderDirectory");
/*  486 */     this.m_spiderDirectory = FileUtils.directorySlashes(this.m_spiderDirectory);
/*      */ 
/*  488 */     this.m_spiderMapping = this.m_idcProperties.getProperty("SpiderMapping");
/*  489 */     this.m_spiderBatchFileName = this.m_idcProperties.getProperty("SpiderBatchFile");
/*  490 */     this.m_spiderFileFilter = this.m_idcProperties.getProperty("SpiderFileFilter");
/*  491 */     this.m_spiderFileFilter = this.m_spiderFileFilter.trim();
/*  492 */     this.m_spiderExcludeFilter = StringUtils.convertToBool(this.m_idcProperties.getProperty("SpiderExcludeFilter"), true);
/*      */   }
/*      */ 
/*      */   protected void setMappingProperties()
/*      */     throws DataException, ServiceException
/*      */   {
/*  498 */     DataBinder binder = new DataBinder();
/*  499 */     ResourceUtils.serializeDataBinder(this.m_mapDir, this.m_spiderMapping + ".hda", binder, false, false);
/*  500 */     this.m_mapSet = ((DataResultSet)binder.getResultSet(this.m_mapTableName));
/*  501 */     if (this.m_mapSet == null)
/*      */     {
/*  503 */       this.m_mapSet = new DataResultSet(this.m_mapFields);
/*      */     }
/*  505 */     this.m_mapFieldIndex = ResultSetUtils.getIndexMustExist(this.m_mapSet, "mapField");
/*  506 */     this.m_mapValueIndex = ResultSetUtils.getIndexMustExist(this.m_mapSet, "mapValue");
/*      */   }
/*      */ 
/*      */   public void setFilterLists()
/*      */   {
/*  511 */     this.m_filterExtList = new Properties();
/*  512 */     this.m_filterNameList = new Properties();
/*      */ 
/*  514 */     Vector filters = StringUtils.parseArray(this.m_spiderFileFilter, ',', '^');
/*  515 */     int size = filters.size();
/*  516 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  518 */       String filter = (String)filters.elementAt(i);
/*  519 */       filter = filter.trim();
/*  520 */       filter = filter.toUpperCase();
/*      */ 
/*  523 */       boolean isExtensionFilter = false;
/*  524 */       int index = filter.indexOf(".");
/*  525 */       if (index < 0)
/*      */       {
/*  527 */         isExtensionFilter = true;
/*      */       }
/*  529 */       else if ((index <= 0) || ((index == 1) && (filter.charAt(0) == '*')))
/*      */       {
/*  531 */         isExtensionFilter = true;
/*  532 */         filter = filter.substring(index + 1);
/*      */       }
/*      */ 
/*  535 */       if (filter.equals(""))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  540 */       if (isExtensionFilter)
/*      */       {
/*  542 */         this.m_filterExtList.put(filter, filter);
/*      */       }
/*      */       else
/*      */       {
/*  546 */         this.m_filterNameList.put(filter, filter);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void setCollectionPathValues() throws ServiceException
/*      */   {
/*  553 */     DataResultSet drset = getSearchCollections();
/*      */ 
/*  556 */     FieldInfo[] info = null;
/*      */     try
/*      */     {
/*  559 */       info = ResultSetUtils.createInfoList(drset, new String[] { "sCollectionID", "sPhysicalFileRoot", "sRelativeWebRoot" }, true);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  564 */       throw new ServiceException(e);
/*      */     }
/*  566 */     Vector row = drset.findRow(info[0].m_index, this.m_externalCollection);
/*  567 */     if (row == null)
/*      */     {
/*  569 */       return;
/*      */     }
/*      */ 
/*  573 */     this.m_relativeWebRoot = ((String)row.elementAt(info[2].m_index));
/*  574 */     if (this.m_relativeWebRoot == null)
/*      */     {
/*  576 */       this.m_relativeWebRoot = "";
/*      */     }
/*      */     else
/*      */     {
/*  580 */       this.m_relativeWebRoot = this.m_relativeWebRoot.trim();
/*  581 */       this.m_relativeWebRoot = FileUtils.directorySlashes(this.m_relativeWebRoot);
/*      */     }
/*      */ 
/*  584 */     this.m_physicalFileRoot = ((String)row.elementAt(info[1].m_index));
/*  585 */     if (this.m_physicalFileRoot == null)
/*      */     {
/*  587 */       this.m_physicalFileRoot = "";
/*      */     }
/*      */     else
/*      */     {
/*  591 */       this.m_physicalFileRoot = this.m_physicalFileRoot.trim();
/*  592 */       this.m_physicalFileRoot = FileUtils.directorySlashes(this.m_physicalFileRoot);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void readSummaryValues() throws DataException, ServiceException
/*      */   {
/*  598 */     DataResultSet drset = getSearchCollections();
/*  599 */     int collectionIDIndex = ResultSetUtils.getIndexMustExist(drset, "sCollectionID");
/*  600 */     Vector row = drset.findRow(collectionIDIndex, this.m_externalCollection);
/*  601 */     if (row == null)
/*      */     {
/*  604 */       return;
/*      */     }
/*      */ 
/*  608 */     int locationIndex = ResultSetUtils.getIndexMustExist(drset, "sLocation");
/*  609 */     String collectionDir = (String)row.elementAt(locationIndex);
/*  610 */     this.m_spiderBinder.putLocal("collectionDir", collectionDir);
/*      */     try
/*      */     {
/*  615 */       FileUtils.checkOrCreateDirectory(collectionDir, 3);
/*  616 */       File file = new File(collectionDir);
/*  617 */       if (!file.exists())
/*      */       {
/*  619 */         throw new ServiceException("Error");
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  624 */       String error = LocaleResources.getString("csSpiderCollectionDirectoryError", this.m_cxt, collectionDir);
/*      */ 
/*  626 */       throw new ServiceException(error);
/*      */     }
/*      */ 
/*  631 */     DataBinder binder = new DataBinder();
/*  632 */     ResourceUtils.serializeDataBinder(collectionDir, "summary.hda", binder, false, false);
/*      */ 
/*  634 */     Properties props = binder.getLocalData();
/*  635 */     Enumeration e = props.keys();
/*  636 */     while (e.hasMoreElements())
/*      */     {
/*  638 */       String key = (String)e.nextElement();
/*  639 */       if (key.indexOf(":counter") >= 0)
/*      */       {
/*  641 */         String value = props.getProperty(key);
/*  642 */         this.m_spiderBinder.putLocal(key, value);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void saveSummaryValues()
/*      */     throws ServiceException
/*      */   {
/*  650 */     String collectionDir = this.m_spiderBinder.getLocal("collectionDir");
/*      */ 
/*  653 */     DataBinder binder = new DataBinder();
/*  654 */     ResourceUtils.serializeDataBinder(collectionDir, "summary.hda", binder, false, false);
/*      */ 
/*  658 */     Properties props = this.m_spiderBinder.getLocalData();
/*  659 */     Enumeration e = props.keys();
/*  660 */     while (e.hasMoreElements())
/*      */     {
/*  662 */       String key = (String)e.nextElement();
/*  663 */       if (key.indexOf(":counter") >= 0)
/*      */       {
/*  665 */         String value = props.getProperty(key);
/*  666 */         binder.putLocal(key, value);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  671 */     ResourceUtils.serializeDataBinder(collectionDir, "summary.hda", binder, true, false);
/*      */   }
/*      */ 
/*      */   protected void processFiles() throws ServiceException
/*      */   {
/*  676 */     this.m_fileCounter = 0L;
/*  677 */     this.m_successCounter = 0L;
/*  678 */     this.m_errorCounter = 0L;
/*      */ 
/*  680 */     this.m_isProcessingFiles = true;
/*  681 */     this.m_isFirstRecord = true;
/*      */ 
/*  683 */     addExternalDirToLocal();
/*      */ 
/*  685 */     readDirectory(this.m_spiderDirectory, 1);
/*      */   }
/*      */ 
/*      */   protected void addExternalDirToLocal()
/*      */   {
/*  694 */     int length = this.m_spiderDirectory.length();
/*  695 */     String tempPath = this.m_spiderDirectory.substring(0, length - 1);
/*      */ 
/*  697 */     String name = FileUtils.getName(tempPath);
/*  698 */     if ((name == null) || (name.equals("")))
/*      */     {
/*  700 */       name = "root";
/*      */     }
/*  702 */     this.m_spiderBinder.putLocal("dir1", name);
/*      */ 
/*  705 */     for (int i = 2; i <= 25; ++i)
/*      */     {
/*  707 */       this.m_spiderBinder.putLocal("dir" + i, "");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void setFileTotal() throws ServiceException
/*      */   {
/*  713 */     this.m_fileCounter = 0L;
/*  714 */     this.m_isProcessingFiles = false;
/*  715 */     readDirectory(this.m_spiderDirectory, 1);
/*      */ 
/*  717 */     this.m_fileTotal = this.m_fileCounter;
/*      */   }
/*      */ 
/*      */   protected void readDirectory(String directory, int level) throws ServiceException
/*      */   {
/*  722 */     directory = FileUtils.directorySlashesEx(directory, true);
/*      */ 
/*  725 */     File dirFile = new File(directory);
/*  726 */     String[] list = dirFile.list();
/*  727 */     int numFiles = list.length;
/*      */ 
/*  730 */     for (int i = 0; i < numFiles; ++i)
/*      */     {
/*  733 */       if (this.m_isExiting)
/*      */       {
/*      */         return;
/*      */       }
/*      */ 
/*  738 */       String fileName = list[i];
/*  739 */       String filePath = directory + fileName;
/*      */ 
/*  742 */       File currentFile = new File(filePath);
/*  743 */       if (currentFile.isDirectory())
/*      */       {
/*  746 */         if (this.m_isProcessingFiles)
/*      */         {
/*  748 */           this.m_spiderBinder.putLocal("dir" + (level + 1), fileName);
/*      */         }
/*      */ 
/*  751 */         readDirectory(filePath, level + 1);
/*      */ 
/*  754 */         if (!this.m_isProcessingFiles)
/*      */           continue;
/*  756 */         this.m_spiderBinder.putLocal("dir" + (level + 1), "");
/*      */       }
/*      */       else
/*      */       {
/*  762 */         if (!canProcessFile(filePath))
/*      */           continue;
/*  764 */         this.m_fileCounter += 1L;
/*      */ 
/*  766 */         if (!this.m_isProcessingFiles)
/*      */           continue;
/*  768 */         processFile(filePath);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void writeToBatchFile(RandomAccessFile handle, String str)
/*      */     throws IOException
/*      */   {
/*  778 */     if (this.m_systemEncoding == null)
/*      */     {
/*  780 */       handle.writeBytes(str);
/*      */     }
/*      */     else
/*      */     {
/*  784 */       byte[] byteArray = str.getBytes(this.m_systemEncoding);
/*  785 */       handle.write(byteArray);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void processFile(String filePath) throws ServiceException
/*      */   {
/*  791 */     if (this.m_isFirstRecord)
/*      */     {
/*  793 */       initProcessing();
/*      */     }
/*      */ 
/*  796 */     setFileProperties(filePath);
/*      */ 
/*  798 */     RandomAccessFile handle = null;
/*      */     try
/*      */     {
/*  801 */       handle = new RandomAccessFile(this.m_spiderBatchFileName, "rw");
/*  802 */       handle.seek(handle.length());
/*      */ 
/*  805 */       String currentRecord = "";
/*  806 */       boolean isValidRecord = true;
/*  807 */       boolean isInDateAdded = false;
/*      */ 
/*  809 */       for (this.m_mapSet.first(); this.m_mapSet.isRowPresent(); this.m_mapSet.next())
/*      */       {
/*  811 */         String name = this.m_mapSet.getStringValue(this.m_mapFieldIndex);
/*  812 */         String value = this.m_mapSet.getStringValue(this.m_mapValueIndex);
/*      */         try
/*      */         {
/*  816 */           PageMerger pageMerger = new PageMerger(this.m_spiderBinder, null);
/*  817 */           value = pageMerger.evaluateScriptReportError(value);
/*      */         }
/*      */         catch (ParseSyntaxException e)
/*      */         {
/*  821 */           isValidRecord = false;
/*  822 */           this.m_errorCounter += 1L;
/*  823 */           logError(filePath, e.getMessage());
/*  824 */           if (this.m_errorCounter >= this.m_errorMax)
/*      */           {
/*  826 */             if (!this.m_doBackground)
/*      */             {
/*  828 */               this.m_frame.m_progressText.setText("");
/*      */             }
/*      */ 
/*  831 */             String errMsg = LocaleResources.getString("csSpiderErrorMaxMessage", this.m_cxt, this.m_errorFileName);
/*      */ 
/*  833 */             throw new ServiceException(errMsg);
/*      */           }
/*  835 */           break label302:
/*      */         }
/*      */         catch (IllegalArgumentException e)
/*      */         {
/*  839 */           throw new ServiceException(e.getMessage());
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  843 */           String errMsg = LocaleResources.getString("csSpiderUnknownErrorMessage", this.m_cxt);
/*  844 */           throw new ServiceException(errMsg);
/*      */         }
/*      */ 
/*  847 */         if (name.equals("dInDate"))
/*      */         {
/*  849 */           isInDateAdded = true;
/*      */         }
/*      */ 
/*  854 */         currentRecord = currentRecord + name + "=" + value + "\r\n";
/*      */       }
/*      */ 
/*  857 */       if (isValidRecord)
/*      */       {
/*  859 */         label302: writeToBatchFile(handle, currentRecord);
/*      */ 
/*  862 */         if (!isInDateAdded)
/*      */         {
/*  864 */           Date dte = new Date();
/*  865 */           String dateStr = LocaleResources.localizeDate(dte, null);
/*  866 */           writeToBatchFile(handle, "dInDate=" + dateStr + "\r\n");
/*      */         }
/*      */ 
/*  870 */         writeToBatchFile(handle, "primaryFile=" + filePath + "\r\n");
/*      */ 
/*  872 */         writeToBatchFile(handle, "<<EOD>>\r\n");
/*      */ 
/*  874 */         this.m_successCounter += 1L;
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */       String errMsg;
/*  881 */       throw new ServiceException(errMsg);
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/*  887 */         handle.close();
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  891 */         if (SystemUtils.m_verbose)
/*      */         {
/*  893 */           Report.debug("system", null, e);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  898 */     if (this.m_isExiting) {
/*      */       return;
/*      */     }
/*  901 */     float amtDone = (float)this.m_fileCounter / (float)this.m_fileTotal * 100.0F;
/*  902 */     String msg = LocaleUtils.encodeMessage("csBatchLoaderProcessData", null, "" + this.m_fileCounter, "" + this.m_fileTotal);
/*      */ 
/*  906 */     if (this.m_doBackground)
/*      */     {
/*  908 */       Report.trace("system", LocaleResources.localizeMessage(msg, this.m_cxt), null);
/*      */     }
/*      */     else
/*      */     {
/*  913 */       this.m_frame.reportProgress(1, msg, amtDone, 100.0F);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean canProcessFile(String filePath)
/*      */   {
/*  920 */     String fileName = FileUtils.getName(filePath);
/*  921 */     fileName = fileName.toUpperCase();
/*      */ 
/*  923 */     String extension = FileUtils.getExtension(filePath);
/*  924 */     extension = extension.toUpperCase();
/*      */ 
/*  927 */     if (extension.equalsIgnoreCase("HDA"))
/*      */     {
/*  929 */       return false;
/*      */     }
/*      */ 
/*  933 */     if (this.m_spiderFileFilter.equals(""))
/*      */     {
/*  935 */       return true;
/*      */     }
/*      */ 
/*  938 */     boolean foundMatch = false;
/*      */ 
/*  941 */     if (this.m_filterExtList.getProperty(extension) != null)
/*      */     {
/*  943 */       foundMatch = true;
/*      */     }
/*  945 */     else if (this.m_filterNameList.getProperty(fileName) != null)
/*      */     {
/*  947 */       foundMatch = true;
/*      */     }
/*      */ 
/*  950 */     return ((foundMatch) && (!this.m_spiderExcludeFilter)) || ((!foundMatch) && (this.m_spiderExcludeFilter));
/*      */   }
/*      */ 
/*      */   protected void initProcessing()
/*      */     throws ServiceException
/*      */   {
/*  956 */     File batchFile = new File(this.m_spiderBatchFileName);
/*  957 */     if (batchFile.exists())
/*      */     {
/*  959 */       batchFile.delete();
/*      */ 
/*  961 */       if (batchFile.exists())
/*      */       {
/*  963 */         String errMsg = LocaleResources.getString("csSpiderOldFileLockMessage", this.m_cxt, this.m_spiderBatchFileName);
/*      */ 
/*  965 */         throw new ServiceException(errMsg);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  970 */     this.m_errorFileName = (this.m_spiderBatchFileName + ".log");
/*  971 */     File errorFile = new File(this.m_errorFileName);
/*  972 */     if (errorFile.exists())
/*      */     {
/*  974 */       errorFile.delete();
/*      */ 
/*  976 */       if (errorFile.exists())
/*      */       {
/*  978 */         String errMsg = LocaleResources.getString("csSpiderOldFileLockMessage", this.m_cxt, this.m_errorFileName);
/*      */ 
/*  980 */         throw new ServiceException(errMsg);
/*      */       }
/*      */     }
/*  983 */     RandomAccessFile handle = null;
/*      */     try
/*      */     {
/*  987 */       handle = new RandomAccessFile(this.m_spiderBatchFileName, "rw");
/*  988 */       writeToBatchFile(handle, "Action = insert\r\n");
/*      */ 
/*  990 */       if (this.m_doExternal)
/*      */       {
/*  992 */         writeToBatchFile(handle, "sCollectionID=" + this.m_externalCollection + "\r\n");
/*      */ 
/*  995 */         if ((this.m_metaDataUsage != null) && (!this.m_metaDataUsage.equals("")))
/*      */         {
/*  997 */           writeToBatchFile(handle, "MetaDataUsage=" + this.m_metaDataUsage + "\r\n");
/*      */ 
/* 1000 */           if ((this.m_metaDataUsage.equalsIgnoreCase("specified")) && (this.m_usageFields != null) && (!this.m_usageFields.equals("")))
/*      */           {
/* 1003 */             writeToBatchFile(handle, "UsageFields=" + this.m_usageFields + "\r\n");
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */       String errMsg;
/* 1012 */       throw new ServiceException(errMsg);
/*      */     }
/*      */     finally
/*      */     {
/* 1016 */       FileUtils.closeObject(handle);
/*      */     }
/*      */ 
/* 1020 */     if (this.m_doExternal)
/*      */     {
/* 1022 */       this.m_spiderBinder.putLocal("sCollectionID", this.m_externalCollection);
/*      */     }
/*      */ 
/* 1025 */     this.m_isFirstRecord = false;
/*      */   }
/*      */ 
/*      */   protected void setFileProperties(String filePath)
/*      */   {
/* 1031 */     this.m_spiderBinder.putLocal("filepath", filePath);
/*      */ 
/* 1033 */     String fileName = FileUtils.getName(filePath);
/* 1034 */     int extIndex = fileName.lastIndexOf(".");
/* 1035 */     if (extIndex >= 0)
/*      */     {
/* 1037 */       fileName = fileName.substring(0, extIndex);
/*      */     }
/* 1039 */     this.m_spiderBinder.putLocal("filename", fileName);
/*      */ 
/* 1041 */     this.m_spiderBinder.putLocal("extension", FileUtils.getExtension(filePath));
/*      */ 
/* 1044 */     File file = new File(filePath);
/* 1045 */     long lastModified = file.lastModified();
/* 1046 */     Date dte = new Date(lastModified);
/* 1047 */     String timeStamp = LocaleResources.localizeDate(dte, null);
/* 1048 */     this.m_spiderBinder.putLocal("filetimestamp", timeStamp);
/*      */ 
/* 1050 */     long fileLength = file.length();
/* 1051 */     this.m_spiderBinder.putLocal("filesize", String.valueOf(fileLength));
/*      */ 
/* 1053 */     if (!this.m_doExternal)
/*      */       return;
/* 1055 */     addFileURLProperties(filePath);
/*      */   }
/*      */ 
/*      */   protected void addFileURLProperties(String filePath)
/*      */   {
/* 1062 */     this.m_spiderBinder.putLocal("URL", "");
/* 1063 */     this.m_spiderBinder.putLocal("URLpath", "");
/*      */ 
/* 1066 */     if ((this.m_relativeWebRoot.equals("")) || (this.m_physicalFileRoot.equals("")))
/*      */     {
/* 1068 */       return;
/*      */     }
/*      */ 
/* 1071 */     if (!filePath.toUpperCase().startsWith(this.m_physicalFileRoot.toUpperCase()))
/*      */     {
/* 1073 */       return;
/*      */     }
/*      */ 
/* 1077 */     int length = this.m_physicalFileRoot.length();
/* 1078 */     String directory = FileUtils.getDirectory(filePath);
/* 1079 */     String fileName = FileUtils.getName(filePath);
/* 1080 */     directory = FileUtils.directorySlashes(directory);
/* 1081 */     String dirEnd = directory.substring(length);
/* 1082 */     String urlPath = this.m_relativeWebRoot + dirEnd;
/* 1083 */     this.m_spiderBinder.putLocal("URLpath", urlPath);
/* 1084 */     this.m_spiderBinder.putLocal("URL", urlPath + fileName);
/*      */   }
/*      */ 
/*      */   protected void initMappings()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1090 */     this.m_mapDir = (DirectoryLocator.getAppDataDirectory() + "search/external/mapping/");
/*      */ 
/* 1094 */     updateOldMapFiles();
/*      */ 
/* 1097 */     File file = FileUtilsCfgBuilder.getCfgFile(this.m_mapDir, "Search", true);
/* 1098 */     if (!file.exists())
/*      */     {
/* 1100 */       FileUtils.checkOrCreateDirectory(this.m_mapDir, 2);
/*      */     }
/*      */ 
/* 1104 */     boolean listExisted = createMappingListFile();
/* 1105 */     if (!listExisted)
/*      */     {
/* 1108 */       createDefaultMappingFile();
/*      */     }
/*      */ 
/* 1112 */     loadMappings();
/*      */   }
/*      */ 
/*      */   protected void updateOldMapFiles() throws DataException, ServiceException
/*      */   {
/* 1117 */     DataBinder oldMapListBinder = new DataBinder();
/* 1118 */     ResourceUtils.serializeDataBinder(this.m_mapDir, this.m_mapListFileName, oldMapListBinder, false, false);
/* 1119 */     DataResultSet oldMapListSet = (DataResultSet)oldMapListBinder.getResultSet(this.m_oldMapListTableName);
/* 1120 */     if (oldMapListSet == null)
/*      */     {
/* 1123 */       return;
/*      */     }
/*      */ 
/* 1127 */     FieldInfo[] oldMapfi = ResultSetUtils.createInfoList(oldMapListSet, this.m_oldMapListFields, true);
/* 1128 */     DataResultSet newMapListSet = new DataResultSet(this.m_mapListFields);
/* 1129 */     FieldInfo[] newMapfi = ResultSetUtils.createInfoList(newMapListSet, this.m_mapListFields, true);
/* 1130 */     for (oldMapListSet.first(); oldMapListSet.isRowPresent(); oldMapListSet.next())
/*      */     {
/* 1132 */       String oldMapName = oldMapListSet.getStringValue(oldMapfi[1].m_index);
/* 1133 */       String oldMapDesc = oldMapListSet.getStringValue(oldMapfi[0].m_index);
/*      */ 
/* 1136 */       Vector newRow = new IdcVector();
/* 1137 */       newRow.setSize(this.m_mapListFields.length);
/* 1138 */       newRow.setElementAt(oldMapName, newMapfi[0].m_index);
/* 1139 */       newRow.setElementAt(oldMapDesc, newMapfi[1].m_index);
/* 1140 */       newMapListSet.addRow(newRow);
/*      */     }
/*      */ 
/* 1143 */     DataBinder newMapListBinder = new DataBinder();
/* 1144 */     newMapListBinder.addResultSet(this.m_mapListTableName, newMapListSet);
/* 1145 */     ResourceUtils.serializeDataBinder(this.m_mapDir, this.m_mapListFileName, newMapListBinder, true, false);
/*      */ 
/* 1148 */     FieldInfo[] mapfi = null;
/* 1149 */     for (newMapListSet.first(); newMapListSet.isRowPresent(); newMapListSet.next())
/*      */     {
/* 1151 */       String mapName = newMapListSet.getStringValue(newMapfi[0].m_index);
/* 1152 */       DataBinder oldMapBinder = new DataBinder();
/* 1153 */       ResourceUtils.serializeDataBinder(this.m_mapDir, mapName + ".hda", oldMapBinder, false, false);
/*      */ 
/* 1155 */       DataResultSet newMapSet = new DataResultSet(this.m_mapFields);
/* 1156 */       if (mapfi == null)
/*      */       {
/* 1158 */         mapfi = ResultSetUtils.createInfoList(newMapSet, this.m_mapFields, true);
/*      */       }
/*      */ 
/* 1162 */       Properties oldMapValues = oldMapBinder.getLocalData();
/* 1163 */       Enumeration en = oldMapValues.keys();
/* 1164 */       while (en.hasMoreElements())
/*      */       {
/* 1166 */         String key = (String)en.nextElement();
/* 1167 */         String value = oldMapValues.getProperty(key);
/*      */ 
/* 1170 */         if (key.startsWith("bl"))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1175 */         Vector row = new IdcVector();
/* 1176 */         row.setSize(2);
/* 1177 */         row.setElementAt(key, mapfi[0].m_index);
/* 1178 */         row.setElementAt(value, mapfi[1].m_index);
/* 1179 */         newMapSet.addRow(row);
/*      */       }
/*      */ 
/* 1182 */       DataBinder newMapBinder = new DataBinder();
/* 1183 */       newMapBinder.addResultSet(this.m_mapTableName, newMapSet);
/* 1184 */       ResourceUtils.serializeDataBinder(this.m_mapDir, mapName + ".hda", newMapBinder, true, false);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean createMappingListFile() throws DataException, ServiceException
/*      */   {
/* 1190 */     DataBinder mapListBinder = new DataBinder();
/* 1191 */     ResourceUtils.serializeDataBinder(this.m_mapDir, this.m_mapListFileName, mapListBinder, false, false);
/* 1192 */     DataResultSet mapListSet = (DataResultSet)mapListBinder.getResultSet(this.m_mapListTableName);
/* 1193 */     if (mapListSet != null)
/*      */     {
/* 1196 */       return true;
/*      */     }
/*      */ 
/* 1200 */     mapListSet = new DataResultSet(this.m_mapListFields);
/*      */ 
/* 1203 */     Vector row = new IdcVector();
/* 1204 */     row.setSize(this.m_mapListFields.length);
/* 1205 */     row.setElementAt("default", ResultSetUtils.getIndexMustExist(mapListSet, "mapName"));
/* 1206 */     row.setElementAt("Default Mapping", ResultSetUtils.getIndexMustExist(mapListSet, "mapDescription"));
/*      */ 
/* 1208 */     mapListSet.addRow(row);
/*      */ 
/* 1211 */     mapListBinder.addResultSet(this.m_mapListTableName, mapListSet);
/* 1212 */     ResourceUtils.serializeDataBinder(this.m_mapDir, this.m_mapListFileName, mapListBinder, true, false);
/*      */ 
/* 1214 */     return false;
/*      */   }
/*      */ 
/*      */   protected void createDefaultMappingFile() throws DataException, ServiceException
/*      */   {
/* 1219 */     DataBinder mapBinder = new DataBinder();
/* 1220 */     ResourceUtils.serializeDataBinder(this.m_mapDir, this.m_mapDefaultFileName, mapBinder, false, false);
/* 1221 */     DataResultSet mapSet = (DataResultSet)mapBinder.getResultSet(this.m_mapTableName);
/* 1222 */     if (mapSet != null)
/*      */     {
/* 1225 */       return;
/*      */     }
/*      */ 
/* 1229 */     mapSet = new DataResultSet(this.m_mapFields);
/*      */ 
/* 1232 */     addDefaultMappingRow(mapSet, "dDocName", "<$filename$>");
/* 1233 */     addDefaultMappingRow(mapSet, "dDocTitle", "<$filename$>");
/* 1234 */     addDefaultMappingRow(mapSet, "dDocAuthor", "sysadmin");
/* 1235 */     addDefaultMappingRow(mapSet, "dSecurityGroup", "Public");
/*      */ 
/* 1238 */     boolean useAccounts = SharedObjects.getEnvValueAsBoolean("UseAccounts", false);
/* 1239 */     boolean useCollaboration = SharedObjects.getEnvValueAsBoolean("UseCollaboration", false);
/* 1240 */     if ((useAccounts) || (useCollaboration))
/*      */     {
/* 1242 */       addDefaultMappingRow(mapSet, "dDocAccount", "");
/*      */     }
/*      */ 
/* 1246 */     Vector docTypes = SharedObjects.getOptList("docTypes");
/* 1247 */     if ((docTypes != null) && (docTypes.size() > 0))
/*      */     {
/* 1249 */       String docType = (String)docTypes.elementAt(0);
/* 1250 */       addDefaultMappingRow(mapSet, "dDocType", docType);
/*      */     }
/*      */ 
/* 1254 */     mapBinder.addResultSet(this.m_mapTableName, mapSet);
/* 1255 */     ResourceUtils.serializeDataBinder(this.m_mapDir, this.m_mapDefaultFileName, mapBinder, true, false);
/*      */   }
/*      */ 
/*      */   protected void addDefaultMappingRow(DataResultSet mapSet, String field, String value)
/*      */     throws DataException
/*      */   {
/* 1261 */     Vector row = new IdcVector();
/* 1262 */     row.setSize(this.m_mapFields.length);
/* 1263 */     row.setElementAt(field, ResultSetUtils.getIndexMustExist(mapSet, "mapField"));
/* 1264 */     row.setElementAt(value, ResultSetUtils.getIndexMustExist(mapSet, "mapValue"));
/* 1265 */     mapSet.addRow(row);
/*      */   }
/*      */ 
/*      */   protected void loadMappings() throws DataException, ServiceException
/*      */   {
/* 1270 */     this.m_mapList = new IdcVector();
/* 1271 */     this.m_mapProps = new Properties();
/*      */ 
/* 1273 */     DataBinder mapListBinder = new DataBinder();
/* 1274 */     ResourceUtils.serializeDataBinder(this.m_mapDir, this.m_mapListFileName, mapListBinder, false, false);
/* 1275 */     DataResultSet mapListSet = (DataResultSet)mapListBinder.getResultSet(this.m_mapListTableName);
/* 1276 */     if (mapListSet == null)
/*      */       return;
/* 1278 */     int mapNameIndex = ResultSetUtils.getIndexMustExist(mapListSet, "mapName");
/* 1279 */     for (mapListSet.first(); mapListSet.isRowPresent(); mapListSet.next())
/*      */     {
/* 1281 */       String mapName = mapListSet.getStringValue(mapNameIndex);
/*      */ 
/* 1283 */       this.m_mapList.addElement(mapName);
/* 1284 */       this.m_mapProps.put(mapName, mapName);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void setMappingSet()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1292 */     DataBinder mapBinder = new DataBinder();
/* 1293 */     ResourceUtils.serializeDataBinder(this.m_mapDir, this.m_spiderMapping + ".hda", mapBinder, false, false);
/* 1294 */     this.m_mapSet = ((DataResultSet)mapBinder.getResultSet(this.m_mapTableName));
/* 1295 */     if (this.m_mapSet == null)
/*      */     {
/* 1297 */       this.m_mapSet = new DataResultSet(this.m_mapFields);
/*      */     }
/* 1299 */     this.m_mapFieldIndex = ResultSetUtils.getIndexMustExist(this.m_mapSet, "mapField");
/* 1300 */     this.m_mapValueIndex = ResultSetUtils.getIndexMustExist(this.m_mapSet, "mapValue");
/*      */   }
/*      */ 
/*      */   protected DataResultSet getSearchCollections()
/*      */     throws ServiceException
/*      */   {
/* 1306 */     DataBinder collBinder = new DataBinder();
/* 1307 */     ResourceUtils.serializeDataBinder(this.m_collDir, this.m_collFileName, collBinder, false, false);
/* 1308 */     DataResultSet collSet = (DataResultSet)collBinder.getResultSet(this.m_collTableName);
/*      */ 
/* 1311 */     if (collSet != null)
/*      */     {
/* 1313 */       String[] newCollFields = { "sPhysicalFileRoot", "sRelativeWebRoot" };
/* 1314 */       DataResultSet mergeSet = new DataResultSet(newCollFields);
/* 1315 */       collSet.mergeFields(mergeSet);
/*      */ 
/* 1317 */       ResourceUtils.serializeDataBinder(this.m_collDir, this.m_collFileName, collBinder, true, false);
/*      */     }
/* 1319 */     return collSet;
/*      */   }
/*      */ 
/*      */   protected void loadCollections() throws DataException, ServiceException
/*      */   {
/* 1324 */     this.m_collDir = (DirectoryLocator.getAppDataDirectory() + "search");
/*      */ 
/* 1326 */     this.m_collectionList = new IdcVector();
/* 1327 */     this.m_collectionProps = new Properties();
/*      */ 
/* 1329 */     DataResultSet collSet = getSearchCollections();
/* 1330 */     if (collSet == null)
/*      */       return;
/* 1332 */     int idIndex = ResultSetUtils.getIndexMustExist(collSet, "sCollectionID");
/* 1333 */     int profileIndex = ResultSetUtils.getIndexMustExist(collSet, "sProfile");
/* 1334 */     for (collSet.first(); collSet.isRowPresent(); collSet.next())
/*      */     {
/* 1336 */       String profile = collSet.getStringValue(profileIndex);
/* 1337 */       if (!profile.equalsIgnoreCase("external"))
/*      */         continue;
/* 1339 */       String collID = collSet.getStringValue(idIndex);
/* 1340 */       this.m_collectionList.addElement(collID);
/* 1341 */       this.m_collectionProps.put(collID, collID);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void initUsages()
/*      */   {
/* 1350 */     this.m_usageList = new Properties();
/* 1351 */     this.m_usageList.put("Batchload", "Batchload");
/* 1352 */     this.m_usageList.put("Inherited", "Inherited");
/* 1353 */     this.m_usageList.put("Specified", "Specified");
/*      */   }
/*      */ 
/*      */   protected void displayError(String msg)
/*      */   {
/* 1359 */     Report.error("system", msg, null);
/*      */ 
/* 1361 */     if (this.m_doBackground)
/*      */     {
/* 1363 */       Report.trace("system", LocaleResources.localizeMessage(msg, this.m_cxt), null);
/*      */     }
/*      */     else
/*      */     {
/* 1367 */       reportError(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void reportError(String msg)
/*      */   {
/* 1375 */     MessageBox.reportError(this.m_frame.m_appHelper, msg);
/*      */   }
/*      */ 
/*      */   public void reportError(IdcMessage msg)
/*      */   {
/* 1380 */     MessageBox.reportError(this.m_frame.m_appHelper, msg);
/*      */   }
/*      */ 
/*      */   protected void logError(String filePath, String errorMsg) throws ServiceException
/*      */   {
/* 1385 */     RandomAccessFile handle = null;
/*      */     try
/*      */     {
/* 1388 */       handle = new RandomAccessFile(this.m_errorFileName, "rw");
/* 1389 */       handle.seek(handle.length());
/*      */ 
/* 1391 */       writeToBatchFile(handle, filePath + "\r\n");
/* 1392 */       writeToBatchFile(handle, LocaleResources.localizeMessage(errorMsg, this.m_cxt) + "\r\n");
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */       String errMsg;
/* 1397 */       throw new ServiceException(errMsg);
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/* 1403 */         handle.close();
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 1407 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1409 */           Report.debug("system", null, e);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void displayFinalMessage()
/*      */   {
/* 1417 */     IdcMessage msg = null;
/*      */ 
/* 1419 */     if (this.m_successCounter > 0L)
/*      */     {
/* 1421 */       msg = IdcMessageFactory.lc("csSpiderSuccessMessage", new Object[] { this.m_spiderBatchFileName, Long.valueOf(this.m_successCounter), Long.valueOf(this.m_errorCounter) });
/*      */ 
/* 1424 */       if (this.m_errorCounter > 0L)
/*      */       {
/* 1426 */         IdcMessage errMsg = IdcMessageFactory.lc("csSpiderErrorMessage", new Object[] { this.m_errorFileName });
/* 1427 */         msg.m_prior = errMsg;
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1433 */       File file = new File(this.m_spiderBatchFileName);
/* 1434 */       if (file.exists())
/*      */       {
/* 1436 */         file.delete();
/*      */       }
/* 1438 */       msg = IdcMessageFactory.lc("csSpiderNotCreatedMessage", new Object[] { this.m_errorFileName });
/*      */     }
/*      */ 
/* 1441 */     Report.info(null, null, msg);
/*      */ 
/* 1444 */     if (this.m_doBackground)
/*      */     {
/* 1446 */       Report.trace("system", null, msg);
/*      */     }
/*      */     else
/*      */     {
/* 1450 */       MessageBox.doMessage(this.m_frame.m_appHelper, msg, 1);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean retrieveProperties()
/*      */   {
/* 1457 */     if (this.m_doBackground)
/*      */     {
/* 1459 */       Enumeration en = this.m_idcProperties.keys();
/* 1460 */       while (en.hasMoreElements())
/*      */       {
/* 1462 */         String name = (String)en.nextElement();
/* 1463 */         String value = this.m_idcProperties.getProperty(name);
/* 1464 */         String[] errMsg = new String[1];
/* 1465 */         if (!validateProperty(name, value, errMsg))
/*      */         {
/* 1467 */           return false;
/*      */         }
/*      */       }
/*      */     }
/* 1471 */     else if (!this.m_frame.m_appHelper.retrieveComponentValues())
/*      */     {
/* 1473 */       return false;
/*      */     }
/*      */ 
/* 1476 */     return true;
/*      */   }
/*      */ 
/*      */   protected boolean validateProperty(String name, String value, String[] errMsg)
/*      */   {
/* 1481 */     boolean isValid = true;
/* 1482 */     String error = null;
/*      */ 
/* 1484 */     if (value == null)
/*      */     {
/* 1486 */       value = "";
/*      */     }
/*      */     else
/*      */     {
/* 1490 */       value = value.trim();
/*      */     }
/*      */ 
/* 1494 */     if (name.equals("SpiderDirectory"))
/*      */     {
/* 1496 */       File file = new File(value);
/* 1497 */       if (!file.exists())
/*      */       {
/* 1499 */         error = LocaleResources.getString("csSpiderInvalidDirectory", this.m_cxt, value);
/* 1500 */         isValid = false;
/*      */       }
/*      */ 
/*      */     }
/* 1504 */     else if (name.equals("SpiderMapping"))
/*      */     {
/* 1507 */       String map = this.m_mapProps.getProperty(value);
/* 1508 */       if (map == null)
/*      */       {
/* 1510 */         error = LocaleResources.getString("csSpiderInvalidMapping", this.m_cxt, value);
/* 1511 */         isValid = false;
/*      */       }
/*      */ 
/*      */     }
/* 1515 */     else if (name.equals("SpiderBatchFile"))
/*      */     {
/* 1517 */       String directory = FileUtils.getDirectory(value);
/* 1518 */       if (directory == null)
/*      */       {
/* 1520 */         directory = "";
/*      */       }
/* 1522 */       File file = new File(directory);
/* 1523 */       if (!file.exists())
/*      */       {
/* 1525 */         error = LocaleResources.getString("csSpiderInvalidBatchFile", this.m_cxt, value);
/* 1526 */         isValid = false;
/*      */       }
/*      */     }
/* 1529 */     else if (this.m_doExternal)
/*      */     {
/* 1532 */       if (name.equals("ExternalCollection"))
/*      */       {
/* 1535 */         if (this.m_collectionProps.getProperty(value) == null)
/*      */         {
/* 1537 */           error = LocaleResources.getString("csSpiderInvalidCollection", this.m_cxt, value);
/* 1538 */           isValid = false;
/*      */         }
/*      */ 
/*      */       }
/* 1542 */       else if ((name.equals("MetaDataUsage")) && 
/* 1545 */         (!value.equalsIgnoreCase("batchload")) && (!value.equalsIgnoreCase("inherited")) && (!value.equalsIgnoreCase("specified")))
/*      */       {
/* 1549 */         this.m_idcProperties.put("MetaDataUsage", "batchload");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1554 */     if (!isValid)
/*      */     {
/* 1556 */       error = error + "  (" + name + ")";
/*      */     }
/*      */ 
/* 1559 */     if ((!isValid) && (this.m_doBackground))
/*      */     {
/* 1561 */       displayError(error);
/*      */     }
/*      */     else
/*      */     {
/* 1565 */       errMsg[0] = error;
/*      */     }
/*      */ 
/* 1568 */     return isValid;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1573 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.batchloader.SpiderApp
 * JD-Core Version:    0.5.4
 */