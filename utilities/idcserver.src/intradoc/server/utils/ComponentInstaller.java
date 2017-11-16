/*      */ package intradoc.server.utils;
/*      */ 
/*      */ import intradoc.apputilities.componentwizard.PreferenceData;
/*      */ import intradoc.apputilities.installer.PermissionInstaller;
/*      */ import intradoc.apputilities.installer.SysInstaller;
/*      */ import intradoc.common.BufferPool;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NativeOsUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StackTrace;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.VersionInfo;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.MultiResultSetFilter;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.SimpleParameters;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.ComponentLoader;
/*      */ import intradoc.server.DataLoader;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import intradoc.zip.ZipFunctions;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.Reader;
/*      */ import java.io.Writer;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.Arrays;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ import java.util.zip.ZipEntry;
/*      */ import java.util.zip.ZipFile;
/*      */ import java.util.zip.ZipOutputStream;
/*      */ 
/*      */ public class ComponentInstaller
/*      */ {
/*      */   protected DataBinder m_log;
/*      */   protected String m_logDataDir;
/*      */   protected String m_logFileName;
/*  108 */   protected String m_manifestName = "manifest.hda";
/*      */ 
/*  113 */   protected String m_newZipName = "manifest.zip";
/*      */   protected String m_backupZipName;
/*      */   protected ZipFile m_zipResource;
/*      */   protected String m_compName;
/*      */   protected DataBinder m_componentBinder;
/*      */   protected DataBinder m_manifestBinder;
/*      */   protected DataBinder m_oldManifestBinder;
/*      */   protected Properties m_backupSourceData;
/*      */   protected Map<String, String> m_args;
/*      */   protected ComponentFeatures m_featureUtils;
/*      */ 
/*      */   @Deprecated
/*      */   protected Hashtable m_manifestErrors;
/*      */   protected HashMap m_manifestExceptions;
/*      */   protected Vector m_successfulComponents;
/*      */   protected Vector m_unsuccessfulComponents;
/*      */   protected Vector m_preventedComponents;
/*      */   protected Vector m_enabledComponents;
/*      */   protected boolean m_directoriesInit;
/*      */   protected String m_idcDir;
/*      */   protected String m_webDir;
/*      */   protected String m_componentDir;
/*      */   protected boolean m_isCustomComponentPath;
/*      */ 
/*      */   @Deprecated
/*      */   protected boolean m_isVerbose;
/*      */   protected boolean m_isBuild;
/*      */   protected boolean m_isUninstall;
/*  233 */   protected boolean m_isSafeMode = true;
/*      */   public boolean m_disableSafeMode;
/*      */   protected ResultSetFilter m_directoryFilter;
/*      */   protected ResultSetFilter m_duplicateFilter;
/*      */   protected Set<String> m_zipEntries;
/*      */   protected ResultSetFilter m_zipFilter;
/*  259 */   protected Map m_duplicateLoads = new HashMap();
/*      */   protected boolean m_isUnix;
/*      */   protected String m_platform;
/*      */   protected NativeOsUtils m_utils;
/*      */   public boolean m_isForeign;
/*      */   protected Map<String, String> m_environment;
/*      */   protected ComponentListEditor m_editor;
/*      */   protected Throwable m_nativeUtilsThrowable;
/*      */   public static final int INSTALL = 0;
/*      */   public static final int UNINSTALL = 1;
/*      */   public static final int BUILD = 2;
/*      */ 
/*      */   public ComponentInstaller()
/*      */   {
/*  294 */     this.m_log = new DataBinder();
/*      */   }
/*      */ 
/*      */   public void initEx(String compName, DataBinder cmpData, DataBinder manifest, Map<String, String> args)
/*      */     throws ServiceException, DataException
/*      */   {
/*  304 */     initWithEditor(compName, cmpData, manifest, args, null);
/*      */   }
/*      */ 
/*      */   public void initForeign(String compName, DataBinder cmpData, DataBinder manifest, Map<String, String> args, ComponentListEditor editor)
/*      */     throws DataException, ServiceException
/*      */   {
/*  320 */     this.m_isForeign = true;
/*  321 */     initWithEditor(compName, cmpData, manifest, args, editor);
/*      */   }
/*      */ 
/*      */   public void initWithEditor(String compName, DataBinder cmpData, DataBinder manifest, Map<String, String> args, ComponentListEditor editor)
/*      */     throws DataException, ServiceException
/*      */   {
/*  339 */     if (editor != null)
/*      */     {
/*  341 */       this.m_environment = editor.getEnvironment();
/*      */     }
/*      */ 
/*  344 */     if (manifest == null)
/*      */     {
/*  346 */       throw new DataException("!csNullManifest");
/*      */     }
/*  348 */     if (args == null)
/*      */     {
/*  350 */       args = new HashMap();
/*      */     }
/*      */ 
/*  353 */     this.m_componentBinder = cmpData;
/*  354 */     this.m_manifestBinder = manifest;
/*  355 */     this.m_args = args;
/*  356 */     this.m_compName = compName;
/*  357 */     if (compName != null)
/*      */     {
/*  359 */       this.m_log.putLocal("ComponentName", compName);
/*      */     }
/*      */ 
/*  362 */     if (EnvUtils.m_useNativeOSUtils)
/*      */     {
/*      */       try
/*      */       {
/*  366 */         this.m_utils = new NativeOsUtils();
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/*  370 */         this.m_nativeUtilsThrowable = t;
/*      */       }
/*      */     }
/*  373 */     this.m_isUnix = EnvUtils.getOSFamily().equals("unix");
/*  374 */     this.m_platform = EnvUtils.getOSName();
/*      */ 
/*  377 */     String newZipName = (String)args.get("NewZipName");
/*  378 */     String backupName = (String)args.get("BackupZipName");
/*  379 */     String zipName = (String)args.get("ZipName");
/*      */ 
/*  381 */     loadIdcDir();
/*      */ 
/*  384 */     this.m_isSafeMode = false;
/*  385 */     this.m_isBuild = StringUtils.convertToBool((String)args.get("Build"), false);
/*  386 */     this.m_isUninstall = StringUtils.convertToBool((String)args.get("Uninstall"), false);
/*      */ 
/*  388 */     String filter = manifest.getLocal("DirectoryFilter");
/*      */ 
/*  390 */     if (!SharedObjects.getEnvValueAsBoolean("DisableDefaultDirectoryFilter", false))
/*      */     {
/*  392 */       StringBuilder sb = new StringBuilder();
/*  393 */       if ((filter != null) && (filter.length() > 0))
/*      */       {
/*  395 */         sb.append(filter);
/*  396 */         sb.append('|');
/*      */       }
/*  398 */       sb.append("Thumbs.db|lockwait.dat|.svn/|*.swp");
/*  399 */       filter = sb.toString();
/*      */     }
/*  401 */     if ((filter != null) && (filter.length() > 0))
/*      */     {
/*  403 */       this.m_directoryFilter = new DirectoryNameFilter(filter);
/*      */     }
/*  405 */     this.m_zipEntries = new HashSet();
/*  406 */     if (StringUtils.convertToBool((String)args.get("RemoveDuplicateZipEntries"), true))
/*      */     {
/*  408 */       this.m_duplicateFilter = new DuplicateEntryFilter(this.m_zipEntries);
/*      */     }
/*  410 */     if ((this.m_directoryFilter != null) || (this.m_duplicateFilter != null))
/*      */     {
/*  412 */       this.m_zipFilter = new MultiResultSetFilter(1, this.m_directoryFilter, this.m_duplicateFilter);
/*      */     }
/*      */ 
/*  416 */     boolean isBackup = StringUtils.convertToBool((String)args.get("Backup"), false);
/*  417 */     boolean isOverwrite = StringUtils.convertToBool((String)args.get("Overwrite"), false);
/*  418 */     if ((isBackup) || (!isOverwrite))
/*      */     {
/*  420 */       this.m_isSafeMode = true;
/*      */     }
/*  422 */     if (this.m_disableSafeMode)
/*      */     {
/*  424 */       this.m_isSafeMode = false;
/*      */     }
/*  426 */     if (newZipName != null)
/*      */     {
/*  428 */       this.m_newZipName = newZipName;
/*      */     }
/*  430 */     if (backupName != null)
/*      */     {
/*  432 */       this.m_backupZipName = backupName;
/*      */     }
/*  434 */     if (zipName != null)
/*      */     {
/*      */       try
/*      */       {
/*  438 */         this.m_zipResource = new ZipFile(zipName);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  442 */         String msg = LocaleUtils.encodeMessage("csCannotAccessZipFile", null, zipName);
/*      */ 
/*  444 */         throw new DataException(msg, e);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  449 */     this.m_manifestErrors = new Hashtable();
/*  450 */     this.m_manifestExceptions = new HashMap();
/*  451 */     this.m_successfulComponents = new IdcVector();
/*  452 */     this.m_unsuccessfulComponents = new IdcVector();
/*  453 */     this.m_preventedComponents = new IdcVector();
/*  454 */     this.m_enabledComponents = new IdcVector();
/*      */ 
/*  456 */     if (editor == null)
/*      */     {
/*  458 */       ComponentListManager.init();
/*  459 */       editor = ComponentListManager.getEditor();
/*      */     }
/*  461 */     this.m_editor = editor;
/*      */   }
/*      */ 
/*      */   public void loadIdcDir()
/*      */   {
/*  469 */     if (this.m_environment == null)
/*      */     {
/*  471 */       this.m_idcDir = SharedObjects.getEnvironmentValue("IntradocDir");
/*      */     }
/*      */     else
/*      */     {
/*  475 */       this.m_idcDir = ((String)this.m_environment.get("IntradocDir"));
/*      */     }
/*  477 */     if (this.m_idcDir == null)
/*      */     {
/*  479 */       Report.trace("componentinstaller", "IntradocDir value unexpectedly empty", null);
/*  480 */       this.m_idcDir = "/UnconfiguredIntradocDir";
/*      */     }
/*  482 */     if (this.m_environment == null)
/*      */     {
/*  484 */       this.m_webDir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/*      */     }
/*      */     else
/*      */     {
/*  488 */       this.m_webDir = ((String)this.m_environment.get("WeblayoutDir"));
/*      */     }
/*      */ 
/*  493 */     String absLoc = null;
/*  494 */     if (this.m_args != null)
/*      */     {
/*  496 */       absLoc = (String)this.m_args.get("AbsoluteDir");
/*      */     }
/*  498 */     if (absLoc != null)
/*      */     {
/*  500 */       this.m_componentDir = new StringBuilder().append(FileUtils.getParent(absLoc)).append("/").toString();
/*      */     }
/*      */     else
/*      */     {
/*  504 */       this.m_componentDir = computeInitialComponentDir();
/*      */     }
/*  506 */     this.m_directoriesInit = true;
/*      */   }
/*      */ 
/*      */   protected String computeInitialComponentDir()
/*      */   {
/*  511 */     Map map = null;
/*  512 */     if (this.m_componentBinder != null)
/*      */     {
/*  514 */       map = this.m_componentBinder.getLocalData();
/*      */     }
/*      */ 
/*  517 */     boolean[] isCustom = new boolean[1];
/*      */ 
/*  519 */     String dir = ComponentLocationUtils.computeDefaultComponentDirWithEnv(map, 1, false, isCustom, this.m_environment);
/*      */ 
/*  521 */     this.m_isCustomComponentPath = isCustom[0];
/*  522 */     return FileUtils.directorySlashes(dir);
/*      */   }
/*      */ 
/*      */   protected String computeComponentLocation(String destination)
/*      */   {
/*  527 */     String dir = "";
/*  528 */     String stub = ComponentLocationUtils.getCustomDirectoryStub();
/*  529 */     boolean isSystem = isSystemComponent();
/*  530 */     if (isSystem)
/*      */     {
/*  532 */       stub = ComponentLocationUtils.getSystemDirectoryStub();
/*      */     }
/*  534 */     if (!destination.startsWith(stub))
/*      */     {
/*  536 */       dir = stub;
/*      */     }
/*  538 */     return new StringBuilder().append(dir).append(destination).toString();
/*      */   }
/*      */ 
/*      */   protected boolean isSystemComponent()
/*      */   {
/*  543 */     if (this.m_componentBinder == null)
/*      */     {
/*  547 */       return false;
/*      */     }
/*      */ 
/*  550 */     Map map = this.m_componentBinder.getLocalData();
/*  551 */     return ComponentLocationUtils.isSystemComponent(map);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   protected void initDirectories()
/*      */     throws ServiceException
/*      */   {
/*  562 */     reportMessage("!csConfiguringManifestDirectories");
/*      */ 
/*  565 */     Properties idcProps = new Properties();
/*  566 */     FileInputStream fin = null;
/*      */     try
/*      */     {
/*  569 */       String idcFileName = "";
/*  570 */       if (this.m_idcDir != null)
/*      */       {
/*  572 */         idcFileName = new StringBuilder().append(idcFileName).append(this.m_idcDir).append("/bin/").toString();
/*      */       }
/*  574 */       idcFileName = new StringBuilder().append(idcFileName).append("intradoc.cfg").toString();
/*      */ 
/*  576 */       FileUtils.loadProperties(idcProps, idcFileName);
/*      */ 
/*  579 */       String cfgFileName = new StringBuilder().append((String)idcProps.get("IntradocDir")).append("/config/config.cfg").toString();
/*      */ 
/*  581 */       FileUtils.loadProperties(idcProps, cfgFileName);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  590 */       FileUtils.closeObject(fin);
/*      */     }
/*      */ 
/*  595 */     this.m_idcDir = ((String)idcProps.get("IntradocDir"));
/*  596 */     this.m_webDir = ((String)idcProps.get("WeblayoutDir"));
/*  597 */     if (this.m_webDir == null)
/*      */     {
/*  599 */       this.m_webDir = new StringBuilder().append(this.m_idcDir).append("/weblayout/").toString();
/*      */     }
/*  601 */     this.m_idcDir = FileUtils.directorySlashesEx(this.m_idcDir, true);
/*  602 */     this.m_webDir = FileUtils.directorySlashesEx(this.m_webDir, true);
/*      */ 
/*  605 */     String path = (this.m_environment == null) ? SharedObjects.getEnvironmentValue("ComponentDir") : (String)this.m_environment.get("ComponentDir");
/*      */ 
/*  607 */     if ((path == null) || (path.length() == 0))
/*      */     {
/*  609 */       path = new StringBuilder().append(this.m_idcDir).append(ComponentLocationUtils.getCustomDirectoryStub()).toString();
/*      */     }
/*      */     else
/*      */     {
/*  613 */       this.m_isCustomComponentPath = true;
/*  614 */       path = FileUtils.directorySlashes(path);
/*      */     }
/*  616 */     this.m_componentDir = path;
/*  617 */     this.m_directoriesInit = true;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public Hashtable getErrors()
/*      */   {
/*  626 */     return this.m_manifestErrors;
/*      */   }
/*      */ 
/*      */   public Map getExceptions()
/*      */   {
/*  632 */     return this.m_manifestExceptions;
/*      */   }
/*      */ 
/*      */   public Vector getSucessfulComponents()
/*      */   {
/*  641 */     if ((this.m_successfulComponents == null) || (this.m_successfulComponents.size() == 0))
/*      */     {
/*  643 */       return null;
/*      */     }
/*  645 */     return (Vector)this.m_successfulComponents.clone();
/*      */   }
/*      */ 
/*      */   public Vector getUnsuccessfulComponents()
/*      */   {
/*  650 */     if (this.m_unsuccessfulComponents == null)
/*      */     {
/*  652 */       return null;
/*      */     }
/*  654 */     return (Vector)this.m_unsuccessfulComponents.clone();
/*      */   }
/*      */ 
/*      */   public Vector getPreventedBundledComponents()
/*      */   {
/*  659 */     if (this.m_preventedComponents == null)
/*      */     {
/*  661 */       return null;
/*      */     }
/*  663 */     return (Vector)this.m_preventedComponents.clone();
/*      */   }
/*      */ 
/*      */   public Vector getEnabledComponents()
/*      */   {
/*  668 */     return this.m_enabledComponents;
/*      */   }
/*      */ 
/*      */   public void executeManifest()
/*      */     throws DataException, ServiceException
/*      */   {
/*  676 */     this.m_backupSourceData = new Properties();
/*      */ 
/*  678 */     if (this.m_isBuild)
/*      */     {
/*  680 */       buildZipFile(false);
/*      */     }
/*      */     else
/*      */     {
/*  685 */       this.m_oldManifestBinder = null;
/*      */ 
/*  689 */       if (this.m_compName != null)
/*      */       {
/*  691 */         String compDir = FileUtils.getAbsolutePath(this.m_componentDir, this.m_compName);
/*  692 */         if (FileUtils.checkFile(compDir, false, false) == 0)
/*      */         {
/*  694 */           this.m_oldManifestBinder = readManifestInfo(compDir);
/*      */         }
/*      */         else
/*      */         {
/*  699 */           this.m_oldManifestBinder = null;
/*  700 */           this.m_isSafeMode = false;
/*      */         }
/*      */       }
/*      */ 
/*  704 */       if (this.m_isSafeMode)
/*      */       {
/*  706 */         this.m_newZipName = this.m_backupZipName;
/*  707 */         if (this.m_newZipName == null)
/*      */         {
/*  709 */           this.m_newZipName = new StringBuilder().append("backup").append(Long.toString(new Date().getTime())).append(".zip").toString();
/*      */         }
/*      */ 
/*  712 */         this.m_newZipName = FileUtils.getAbsolutePath(new StringBuilder().append(DirectoryLocator.getIntradocDir()).append("backup/").toString(), this.m_newZipName);
/*      */ 
/*  714 */         String msg = LocaleUtils.encodeMessage("csBackingUpOldFiles", null, this.m_newZipName);
/*  715 */         reportMessage(msg);
/*  716 */         String backupDir = FileUtils.getParent(this.m_newZipName);
/*  717 */         FileUtils.checkOrCreateDirectory(backupDir, 2);
/*      */         Enumeration e;
/*  721 */         if (this.m_isUninstall)
/*      */         {
/*  723 */           for (e = this.m_manifestBinder.m_localData.keys(); e.hasMoreElements(); )
/*      */           {
/*  725 */             String key = (String)e.nextElement();
/*  726 */             String value = this.m_manifestBinder.getLocal(key);
/*  727 */             this.m_backupSourceData.put(key, value);
/*      */           }
/*      */         }
/*  730 */         buildZipFile(true);
/*      */       }
/*      */ 
/*  733 */       if (this.m_isUninstall)
/*      */       {
/*  736 */         this.m_manifestBinder.m_localData = this.m_backupSourceData;
/*  737 */         removeFiles();
/*      */       }
/*      */       else
/*      */       {
/*  741 */         cleanOldInstall();
/*  742 */         installFiles();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public DataBinder readManifestInfo(String filePath)
/*      */   {
/*  749 */     DataBinder binder = null;
/*      */     try
/*      */     {
/*  752 */       binder = ResourceUtils.readDataBinder(filePath, this.m_manifestName);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  756 */       Report.trace("componentinstaller", new StringBuilder().append("Can't load old manifest file in dir '").append(filePath).append("'").toString(), e);
/*      */     }
/*      */ 
/*  759 */     return binder;
/*      */   }
/*      */ 
/*      */   public DataBinder readManifestInfoFromZip(String zipFilePath)
/*      */     throws ServiceException, DataException
/*      */   {
/*  765 */     return readFileAsBinder(zipFilePath, this.m_manifestName);
/*      */   }
/*      */ 
/*      */   public DataBinder readFileAsBinder(String zipFilePath, String filename)
/*      */     throws ServiceException, DataException
/*      */   {
/*  771 */     DataBinder binder = null;
/*  772 */     ZipFile zipFile = null;
/*  773 */     if ((zipFilePath != null) && (zipFilePath.length() > 0))
/*      */     {
/*      */       try
/*      */       {
/*  778 */         Hashtable zipEntries = new Hashtable();
/*      */         try
/*      */         {
/*  781 */           zipFile = ZipFunctions.readZipFile(zipFilePath, zipEntries);
/*      */         }
/*      */         catch (Exception exp)
/*      */         {
/*  785 */           throw new ServiceException(exp, "csCompWizUnableToReadZipContents", new Object[] { zipFilePath });
/*      */         }
/*      */ 
/*  790 */         ZipEntry manEntry = (ZipEntry)zipEntries.get(filename);
/*  791 */         if (manEntry == null)
/*      */         {
/*  794 */           throw new DataException(LocaleUtils.encodeMessage("csCompWizUnableToFindFile", null, filename, zipFilePath));
/*      */         }
/*      */ 
/*  797 */         binder = ZipFunctions.extractFileAsDataBinder(zipFile, manEntry, filename);
/*      */       }
/*      */       finally
/*      */       {
/*  802 */         FileUtils.closeObject(zipFile);
/*      */       }
/*      */     }
/*      */ 
/*  806 */     return binder;
/*      */   }
/*      */ 
/*      */   protected void buildZipFile(boolean allowMissing)
/*      */     throws ServiceException, DataException
/*      */   {
/*  814 */     reportMessage("!csCompressingFiles");
/*      */ 
/*  821 */     boolean isBackup = StringUtils.convertToBool((String)this.m_args.get("Backup"), false);
/*  822 */     DataResultSet manifestPrior = (DataResultSet)this.m_manifestBinder.getResultSet("Manifest");
/*  823 */     DataResultSet manifest = new DataResultSet();
/*  824 */     ZipFile zipPrior = null;
/*      */ 
/*  826 */     if (isBackup)
/*      */     {
/*  828 */       boolean isError = this.m_oldManifestBinder == null;
/*  829 */       if (!isError)
/*      */       {
/*  831 */         String compDir = new StringBuilder().append(this.m_componentDir).append(this.m_compName).toString();
/*      */ 
/*  835 */         String[] hdas = FileUtils.getMatchingFileNames(compDir, "*.hda");
/*  836 */         if ((hdas == null) || (hdas.length <= 1))
/*      */         {
/*  838 */           isError = true;
/*      */         }
/*      */         else
/*      */         {
/*  842 */           manifest = (DataResultSet)this.m_oldManifestBinder.getResultSet("Manifest");
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  847 */         Report.trace("componentinstaller", "Can't load backup manifest, could not load old manifest.hda", null);
/*      */       }
/*      */ 
/*  851 */       if (isError)
/*      */       {
/*  854 */         postBuildZipFileManifest();
/*  855 */         return;
/*      */       }
/*      */ 
/*  858 */       this.m_manifestBinder.addResultSet("Manifest", manifest);
/*  859 */       zipPrior = this.m_zipResource;
/*  860 */       this.m_zipResource = null;
/*      */     }
/*      */     else
/*      */     {
/*  865 */       manifest.copy(manifestPrior);
/*      */     }
/*      */ 
/*  869 */     Hashtable fileInfos = null;
/*      */     try
/*      */     {
/*  872 */       fileInfos = buildZipFileList(manifest);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  877 */       if (isBackup)
/*      */       {
/*  879 */         Report.trace("componentinstaller", "Couldn't create zip file; cannot find files specified in manifest.", e);
/*      */ 
/*  881 */         postBuildZipFileManifest();
/*  882 */         this.m_manifestBinder.addResultSet("Manifest", manifestPrior);
/*  883 */         this.m_zipResource = zipPrior;
/*  884 */         return;
/*      */       }
/*      */ 
/*  888 */       throw e;
/*      */     }
/*      */ 
/*  891 */     ZipOutputStream zos = null;
/*  892 */     ByteArrayInputStream bais = null;
/*      */     try
/*      */     {
/*  895 */       FileUtils.checkOrCreateDirectory(FileUtils.getDirectory(this.m_newZipName), 5);
/*  896 */       zos = new ZipOutputStream(new FileOutputStream(this.m_newZipName));
/*      */ 
/*  899 */       for (Enumeration en = fileInfos.elements(); en.hasMoreElements(); )
/*      */       {
/*  901 */         Hashtable files = (Hashtable)en.nextElement();
/*  902 */         for (fileEnum = files.elements(); fileEnum.hasMoreElements(); )
/*      */         {
/*  904 */           String[] info = (String[])(String[])fileEnum.nextElement();
/*  905 */           addFileToZip(info[0], info[1], zos, allowMissing);
/*      */         }
/*      */       }
/*      */       Enumeration fileEnum;
/*  909 */       if (this.m_manifestExceptions.isEmpty())
/*      */       {
/*  911 */         postBuildZipFileManifest();
/*      */ 
/*  914 */         zos.putNextEntry(new ZipEntry("manifest.hda"));
/*      */ 
/*  916 */         Writer w = FileUtils.openDataWriterEx(zos, null, 0);
/*  917 */         this.m_manifestBinder.send(w);
/*  918 */         reportMessage("!$  manifest.hda");
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  928 */       FileUtils.closeObjects(zos, bais);
/*      */ 
/*  931 */       if (isBackup)
/*      */       {
/*  933 */         this.m_manifestBinder.addResultSet("Manifest", manifestPrior);
/*  934 */         this.m_zipResource = zipPrior;
/*      */       }
/*      */     }
/*      */ 
/*  938 */     if (this.m_manifestExceptions.isEmpty())
/*      */     {
/*  940 */       String msg = LocaleUtils.encodeMessage("csZipFileCreated", null, this.m_newZipName);
/*      */ 
/*  942 */       reportMessage(msg);
/*      */     }
/*      */     else
/*      */     {
/*  946 */       postBuildZipFileManifest();
/*  947 */       FileUtils.deleteFile(this.m_newZipName);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void postBuildZipFileManifest()
/*      */   {
/*  957 */     this.m_manifestBinder.setLocalData(new Properties());
/*  958 */     if (this.m_compName != null)
/*      */     {
/*  960 */       this.m_manifestBinder.putLocal("ComponentName", this.m_compName);
/*      */     }
/*  962 */     Date now = new Date();
/*  963 */     this.m_manifestBinder.putLocalDate("CreateDate", now);
/*      */   }
/*      */ 
/*      */   protected Hashtable buildZipFileList(DataResultSet manifest)
/*      */     throws DataException, ServiceException
/*      */   {
/*  971 */     String entryType = null;
/*  972 */     String location = null;
/*  973 */     String source = null;
/*  974 */     int entryTypeIndex = ResultSetUtils.getIndexMustExist(manifest, "entryType");
/*  975 */     int locationIndex = ResultSetUtils.getIndexMustExist(manifest, "location");
/*      */ 
/*  977 */     Hashtable fileInfos = new Hashtable();
/*  978 */     for (manifest.first(); manifest.isRowPresent(); manifest.next())
/*      */     {
/*  984 */       entryType = manifest.getStringValue(entryTypeIndex).toLowerCase();
/*  985 */       location = manifest.getStringValue(locationIndex);
/*  986 */       source = expandSource(entryType, location, manifest, 2, false);
/*      */ 
/*  988 */       if (source == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  994 */       if (entryType.startsWith("component"))
/*      */       {
/*  996 */         location = new StringBuilder().append("component/").append(location).toString();
/*      */       }
/*      */       else
/*      */       {
/* 1000 */         location = new StringBuilder().append(entryType).append("/").append(location).toString();
/*      */       }
/*      */ 
/* 1004 */       String parent = null;
/* 1005 */       File file = new File(source);
/* 1006 */       boolean isDirectory = file.isDirectory();
/* 1007 */       if (isDirectory)
/*      */       {
/* 1009 */         parent = FileUtils.fixDirectorySlashes(source, 78).toString();
/*      */       }
/*      */       else
/*      */       {
/* 1013 */         parent = FileUtils.getParent(source);
/* 1014 */         if (parent == null)
/*      */         {
/* 1016 */           String msg = LocaleUtils.encodeMessage("csCompWizZipFileError", null, this.m_args.get("ZipName"));
/*      */ 
/* 1018 */           throw new DataException(msg);
/*      */         }
/* 1020 */         parent = FileUtils.directorySlashes(parent);
/*      */       }
/*      */ 
/* 1023 */       String[] info = null;
/* 1024 */       Hashtable infos = (Hashtable)fileInfos.get(parent);
/* 1025 */       if (infos != null)
/*      */       {
/* 1027 */         info = (String[])(String[])infos.get(parent);
/* 1028 */         if (info != null)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1033 */         info = (String[])(String[])infos.get(source);
/* 1034 */         if (info == null);
/*      */       }
/*      */       else
/*      */       {
/* 1041 */         infos = new Hashtable();
/* 1042 */         fileInfos.put(parent, infos);
/*      */ 
/* 1045 */         info = new String[3];
/* 1046 */         info[0] = location;
/* 1047 */         info[1] = source;
/* 1048 */         info[2] = String.valueOf(isDirectory);
/*      */ 
/* 1050 */         if (isDirectory)
/*      */         {
/* 1052 */           infos.clear();
/* 1053 */           infos.put(parent, info);
/*      */         }
/*      */         else
/*      */         {
/* 1057 */           infos.put(source, info);
/*      */         }
/*      */       }
/*      */     }
/* 1061 */     return fileInfos;
/*      */   }
/*      */ 
/*      */   protected void addFileToZip(String location, String source, ZipOutputStream zos, boolean allowMissing)
/*      */   {
/*      */     try
/*      */     {
/* 1076 */       if (FileUtils.checkFile(source, true, false) == 0)
/*      */       {
/* 1079 */         source = FileUtils.directorySlashesEx(source, false);
/* 1080 */         checkValidDestination(source);
/* 1081 */         location = FileUtils.directorySlashesEx(location, false);
/* 1082 */         if (!this.m_zipEntries.contains(location))
/*      */         {
/* 1084 */           this.m_zipEntries.add(location);
/* 1085 */           ZipFunctions.addFile(location, source, zos);
/*      */         }
/*      */       }
/* 1088 */       else if (FileUtils.checkFile(source, false, false) == 0)
/*      */       {
/* 1090 */         source = FileUtils.directorySlashes(source);
/* 1091 */         location = FileUtils.directorySlashes(location);
/*      */ 
/* 1094 */         ZipFunctions.addDirectoryFiltered(location, source, true, this.m_zipFilter, zos);
/*      */       }
/*      */       else
/*      */       {
/* 1098 */         String msg = LocaleUtils.encodeMessage("csArchiverResourceMissing", null, source);
/*      */ 
/* 1100 */         throw new ServiceException(msg);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1105 */       if (!allowMissing)
/*      */       {
/* 1107 */         String msg = LocaleUtils.encodeMessage("csUnableToIncludeFile", null, source);
/* 1108 */         ServiceException se = new ServiceException(msg, e);
/* 1109 */         Report.trace("system", null, se);
/* 1110 */         reportError(source, se);
/*      */       }
/* 1112 */       return;
/*      */     }
/* 1114 */     reportMessage(new StringBuilder().append("!$  ").append(source).toString());
/*      */   }
/*      */ 
/*      */   protected void removeFiles()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1124 */     reportMessage("!csRemovingFiles");
/* 1125 */     DataResultSet reservedFilesList = SharedObjects.getTable("ReservedFilesList");
/*      */ 
/* 1127 */     DataResultSet manifest = (DataResultSet)this.m_manifestBinder.getResultSet("Manifest");
/*      */ 
/* 1129 */     int entryTypeIndex = ResultSetUtils.getIndexMustExist(manifest, "entryType");
/* 1130 */     int locationIndex = ResultSetUtils.getIndexMustExist(manifest, "location");
/*      */ 
/* 1132 */     String[] retStr = retrieveComponentNameAndLocation(this.m_manifestBinder);
/* 1133 */     String componentLocation = new StringBuilder().append(this.m_componentDir).append(retStr[0]).toString();
/* 1134 */     String componentName = retStr[1];
/*      */ 
/* 1141 */     DataBinder componentData = new DataBinder(true);
/* 1142 */     ResourceUtils.serializeDataBinder(FileUtils.getDirectory(componentLocation), FileUtils.getName(componentLocation), componentData, false, true);
/*      */ 
/* 1145 */     String cmpDefLocation = null;
/* 1146 */     String cmpDefDir = FileUtils.directorySlashes(FileUtils.getDirectory(componentLocation));
/* 1147 */     for (manifest.first(); manifest.isRowPresent(); manifest.next())
/*      */     {
/* 1149 */       String entryType = manifest.getStringValue(entryTypeIndex).toLowerCase();
/* 1150 */       String location = manifest.getStringValue(locationIndex);
/* 1151 */       String source = expandSource(entryType, location, manifest, 1, false);
/*      */ 
/* 1153 */       if (source == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1158 */       if (entryType.equals("component"))
/*      */       {
/* 1160 */         cmpDefLocation = source;
/* 1161 */         FileUtils.renameFile(source, new StringBuilder().append(FileUtils.getDirectory(source)).append("/").append("delete_uninstall.hda").toString());
/*      */       }
/*      */       else {
/* 1164 */         if (source.startsWith(cmpDefDir))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1172 */         boolean isReserved = isEntryTypeReserved(entryType, reservedFilesList);
/* 1173 */         if (isReserved)
/*      */         {
/* 1175 */           deleteUnreservedFiles(entryType, location, source, reservedFilesList);
/*      */         }
/*      */         else
/*      */         {
/* 1179 */           deleteFile(source);
/*      */         }
/*      */       }
/* 1182 */       reportMessage(new StringBuilder().append("!$  ").append(source).toString());
/*      */     }
/*      */ 
/* 1186 */     ComponentUninstallHelper compUninstaller = new ComponentUninstallHelper();
/* 1187 */     compUninstaller.addComponentRow(cmpDefLocation, componentName, null);
/*      */ 
/* 1191 */     DataResultSet distFiles = (DataResultSet)componentData.getResultSet("OtherComponentDistFiles");
/*      */ 
/* 1193 */     if (distFiles != null)
/*      */     {
/* 1195 */       FieldInfo[] fis = ResultSetUtils.createInfoList(distFiles, new String[] { "componentName", "srcPath", "dstPath", "canDeleteDir" }, true);
/*      */ 
/* 1197 */       for (distFiles.first(); distFiles.isRowPresent(); distFiles.next())
/*      */       {
/* 1199 */         String destComponentName = distFiles.getStringValue(fis[0].m_index);
/* 1200 */         String destPath = new StringBuilder().append(this.m_componentDir).append(destComponentName).append('/').append(distFiles.getStringValue(fis[2].m_index)).toString();
/*      */ 
/* 1202 */         String canDeleteStr = distFiles.getStringValue(fis[3].m_index);
/* 1203 */         boolean canDeleteDir = StringUtils.convertToBool(canDeleteStr, false);
/*      */ 
/* 1205 */         File destDir = new File(destPath);
/* 1206 */         if ((!destDir.exists()) || (!canDeleteDir))
/*      */           continue;
/* 1208 */         FileUtils.deleteDirectory(destDir, true);
/* 1209 */         if (!destDir.exists())
/*      */           continue;
/* 1211 */         String msg = LocaleUtils.encodeMessage("csCantDeleteDistDirOnUninstall", null, destDir.getAbsolutePath(), componentName);
/*      */ 
/* 1213 */         ServiceException se = new ServiceException(msg);
/* 1214 */         reportError(componentName, se);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1220 */     reportMessage("!csUninstallCompleted");
/*      */   }
/*      */ 
/*      */   protected boolean isEntryTypeReserved(String entryType, DataResultSet reservedFilesList)
/*      */     throws DataException
/*      */   {
/* 1228 */     boolean retBool = false;
/* 1229 */     if ((reservedFilesList == null) || (reservedFilesList.isEmpty()))
/*      */     {
/* 1231 */       return retBool;
/*      */     }
/*      */ 
/* 1234 */     FieldInfo[] fi = ResultSetUtils.createInfoList(reservedFilesList, new String[] { "entryType" }, true);
/* 1235 */     for (reservedFilesList.first(); reservedFilesList.isRowPresent(); reservedFilesList.next())
/*      */     {
/* 1237 */       String type = reservedFilesList.getStringValue(fi[0].m_index);
/* 1238 */       if ((type == null) || (!type.equals(entryType)))
/*      */         continue;
/* 1240 */       retBool = true;
/* 1241 */       break;
/*      */     }
/*      */ 
/* 1245 */     return retBool;
/*      */   }
/*      */ 
/*      */   protected void deleteUnreservedFiles(String entryType, String location, String source, DataResultSet reservedFilesList)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1254 */     FieldInfo[] fi = ResultSetUtils.createInfoList(reservedFilesList, new String[] { "entryType", "filePath" }, true);
/* 1255 */     String type = null;
/* 1256 */     String filePath = null;
/* 1257 */     boolean isDelete = true;
/* 1258 */     boolean checkSubDir = false;
/* 1259 */     String[][] entryList = (String[][])null;
/*      */ 
/* 1261 */     for (reservedFilesList.first(); reservedFilesList.isRowPresent(); reservedFilesList.next())
/*      */     {
/* 1263 */       type = reservedFilesList.getStringValue(fi[0].m_index);
/* 1264 */       filePath = reservedFilesList.getStringValue(fi[1].m_index);
/*      */ 
/* 1266 */       if ((type == null) || (!type.equals(entryType)))
/*      */         continue;
/* 1268 */       if ((filePath == null) || (filePath.length() == 0) || (filePath.equals(location)) || (location.startsWith(filePath)))
/*      */       {
/* 1270 */         isDelete = false;
/* 1271 */         break;
/*      */       }
/*      */ 
/* 1275 */       if (!filePath.startsWith(location))
/*      */         continue;
/* 1277 */       checkSubDir = true;
/* 1278 */       break;
/*      */     }
/*      */ 
/* 1283 */     if (!isDelete)
/*      */       return;
/* 1285 */     entryList = ResultSetUtils.createFilteredStringTable(reservedFilesList, new String[] { "entryType", "filePath" }, type);
/* 1286 */     deleteUnreservedFilesEx(entryList, location, source, checkSubDir);
/*      */   }
/*      */ 
/*      */   protected void deleteUnreservedFilesEx(String[][] seletedFilesList, String location, String source, boolean checkSubDir)
/*      */     throws ServiceException
/*      */   {
/* 1295 */     if (checkSubDir)
/*      */     {
/* 1297 */       File file = new File(source);
/* 1298 */       String[] files = file.list();
/*      */ 
/* 1300 */       if (files == null)
/*      */       {
/* 1302 */         return;
/*      */       }
/* 1304 */       for (int i = 0; i < files.length; ++i)
/*      */       {
/* 1306 */         String subDir = files[i];
/* 1307 */         boolean isDelete = true;
/* 1308 */         String newLoc = new StringBuilder().append(FileUtils.directorySlashes(location)).append(subDir).toString();
/*      */ 
/* 1310 */         for (int j = 0; j < seletedFilesList.length; ++j)
/*      */         {
/* 1312 */           String filePath = FileUtils.fileSlashes(seletedFilesList[j][0]);
/*      */ 
/* 1314 */           if ((filePath == null) || (filePath.length() == 0) || (filePath.equals(newLoc)))
/*      */           {
/* 1316 */             isDelete = false;
/* 1317 */             break;
/*      */           }
/*      */ 
/* 1320 */           if (!filePath.startsWith(newLoc))
/*      */             continue;
/* 1322 */           isDelete = false;
/* 1323 */           break;
/*      */         }
/*      */ 
/* 1326 */         if (!isDelete)
/*      */           continue;
/* 1328 */         deleteFile(new StringBuilder().append(FileUtils.directorySlashes(source)).append(subDir).toString());
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1334 */       deleteFile(source);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void deleteFile(String source) throws ServiceException
/*      */   {
/* 1340 */     File file = new File(source);
/* 1341 */     if (file.isDirectory())
/*      */     {
/* 1343 */       FileUtils.deleteDirectory(file, true);
/*      */     }
/*      */     else
/*      */     {
/* 1347 */       FileUtils.deleteFile(source);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void cleanOldInstall()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1354 */     if (this.m_oldManifestBinder == null)
/*      */     {
/* 1356 */       return;
/*      */     }
/*      */ 
/* 1359 */     String[] retStr = retrieveComponentNameAndLocation(this.m_oldManifestBinder);
/* 1360 */     String componentLocation = new StringBuilder().append(this.m_componentDir).append(retStr[0]).toString();
/*      */ 
/* 1364 */     DataResultSet manifest = (DataResultSet)this.m_oldManifestBinder.getResultSet("Manifest");
/* 1365 */     DataResultSet reservedFilesList = SharedObjects.getTable("ReservedFilesList");
/* 1366 */     int entryTypeIndex = ResultSetUtils.getIndexMustExist(manifest, "entryType");
/* 1367 */     int locationIndex = ResultSetUtils.getIndexMustExist(manifest, "location");
/* 1368 */     for (manifest.first(); manifest.isRowPresent(); manifest.next())
/*      */     {
/* 1370 */       String entryType = manifest.getStringValue(entryTypeIndex).toLowerCase();
/* 1371 */       String location = manifest.getStringValue(locationIndex);
/* 1372 */       String source = null;
/*      */       try
/*      */       {
/* 1375 */         source = expandSource(entryType, location, manifest, 2, false);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1383 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1385 */           Report.debug("system", null, e);
/*      */         }
/*      */       }
/*      */ 
/* 1389 */       if (source == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1395 */       if ((entryType.equalsIgnoreCase("classes")) || (entryType.equalsIgnoreCase("componentclasses")))
/*      */       {
/* 1398 */         deleteUnreservedFiles(entryType, location, source, reservedFilesList);
/*      */       }
/* 1400 */       reportMessage(new StringBuilder().append("!$  ").append(source).toString());
/*      */     }
/*      */ 
/* 1404 */     FileUtils.deleteFile(new StringBuilder().append(componentLocation).append("delete_uninstall.hda").toString());
/*      */ 
/* 1406 */     reportMessage("!csComponentCleanupCompleted");
/*      */   }
/*      */ 
/*      */   protected void installFiles()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1414 */     reportMessage("!csInstallingFiles");
/*      */ 
/* 1416 */     String compDir = null;
/*      */ 
/* 1420 */     DataBinder manifestOriginal = new DataBinder();
/* 1421 */     manifestOriginal.merge(this.m_manifestBinder);
/*      */ 
/* 1425 */     DataResultSet manifest = (DataResultSet)this.m_manifestBinder.getResultSet("Manifest");
/* 1426 */     DataResultSet manifestDrsetOriginal = new DataResultSet();
/* 1427 */     manifestDrsetOriginal.copy(manifest);
/*      */ 
/* 1431 */     manifestOriginal.addResultSet("Manifest", manifestDrsetOriginal);
/*      */ 
/* 1433 */     int entryTypeIndex = ResultSetUtils.getIndexMustExist(manifest, "entryType");
/* 1434 */     int locationIndex = ResultSetUtils.getIndexMustExist(manifest, "location");
/*      */ 
/* 1436 */     for (manifest.first(); manifest.isRowPresent(); manifest.next())
/*      */     {
/* 1438 */       String entryType = manifest.getStringValue(entryTypeIndex).toLowerCase();
/* 1439 */       String location = manifest.getStringValue(locationIndex);
/*      */ 
/* 1441 */       String destDirectory = expandSource(entryType, location, manifest, 0, true);
/* 1442 */       if (entryType.equals("component"))
/*      */       {
/* 1444 */         compDir = FileUtils.getDirectory(location);
/*      */ 
/* 1447 */         String fullCompDir = FileUtils.getAbsolutePath(destDirectory, compDir);
/* 1448 */         FileUtils.checkOrCreateDirectoryPrepareForLocks(fullCompDir, 1, true);
/*      */ 
/* 1452 */         File dir = new File(new StringBuilder().append(destDirectory).append(compDir).append("/idcautopublish").toString());
/* 1453 */         if ((dir.exists()) && (dir.isDirectory()))
/*      */         {
/* 1455 */           FileUtils.deleteDirectory(dir, true);
/*      */         }
/*      */       }
/*      */ 
/* 1459 */       if (destDirectory == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1465 */       String entryName = null;
/* 1466 */       if (entryType.startsWith("component"))
/*      */       {
/* 1468 */         entryName = new StringBuilder().append("component/").append(location).toString();
/*      */       }
/*      */       else
/*      */       {
/* 1472 */         entryName = new StringBuilder().append(entryType).append("/").append(location).toString();
/*      */       }
/*      */ 
/* 1476 */       if ((entryType.equals("common")) || (entryType.equals("jsp")) || (entryType.equals("images")) || (entryType.equals("resources")) || (entryType.equals("help")) || (entryType.equals("weblayout")))
/*      */       {
/* 1483 */         location = new StringBuilder().append(compDir).append("/idcautopublish/").append(entryType).append("/").append(location).toString();
/*      */       }
/*      */ 
/* 1486 */       extractZipFileOrDirectory(entryName, location, destDirectory);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1492 */       String dest = expandSourceDirectory("component");
/* 1493 */       String path = FileUtils.getAbsolutePath(dest, compDir);
/* 1494 */       ResourceUtils.serializeDataBinder(path, this.m_manifestName, manifestOriginal, true, false);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1498 */       String errMsg = LocaleUtils.encodeMessage("csUnableToExtract", null, this.m_manifestName, compDir);
/*      */ 
/* 1500 */       ServiceException se = new ServiceException(errMsg, e);
/* 1501 */       Report.trace("system", null, se);
/* 1502 */       reportError(compDir, se);
/*      */     }
/*      */ 
/* 1507 */     String[] retStr = retrieveComponentNameAndLocation(this.m_manifestBinder);
/* 1508 */     String location = FileUtils.getAbsolutePath(this.m_componentDir, retStr[0]);
/* 1509 */     String componentName = retStr[1];
/* 1510 */     String componentDir = new StringBuilder().append(this.m_componentDir).append(componentName).append('/').toString();
/*      */ 
/* 1512 */     DataBinder componentData = new DataBinder(true);
/* 1513 */     ResourceUtils.serializeDataBinder(FileUtils.getDirectory(location), FileUtils.getName(location), componentData, false, true);
/*      */ 
/* 1515 */     DataResultSet distFiles = (DataResultSet)componentData.getResultSet("OtherComponentDistFiles");
/*      */ 
/* 1517 */     if (distFiles != null)
/*      */     {
/* 1519 */       FieldInfo[] fis = ResultSetUtils.createInfoList(distFiles, new String[] { "componentName", "srcPath", "dstPath", "canDeleteDir" }, true);
/*      */ 
/* 1521 */       for (distFiles.first(); distFiles.isRowPresent(); distFiles.next())
/*      */       {
/* 1523 */         String destComponentName = distFiles.getStringValue(fis[0].m_index);
/* 1524 */         String srcPath = new StringBuilder().append(componentDir).append(distFiles.getStringValue(fis[1].m_index)).toString();
/* 1525 */         String destPath = new StringBuilder().append(this.m_componentDir).append(destComponentName).append('/').append(distFiles.getStringValue(fis[2].m_index)).toString();
/*      */ 
/* 1527 */         String canDeleteStr = distFiles.getStringValue(fis[3].m_index);
/* 1528 */         boolean canDeleteDir = StringUtils.convertToBool(canDeleteStr, false);
/*      */ 
/* 1530 */         File srcDir = new File(srcPath);
/* 1531 */         File destDir = new File(destPath);
/* 1532 */         if (!srcDir.exists())
/*      */         {
/* 1534 */           String msg = LocaleUtils.encodeMessage("csSrcComponentDistDirDoesNotExist", null, srcDir.getAbsolutePath(), destDir.getAbsolutePath(), componentName);
/*      */ 
/* 1536 */           ServiceException se = new ServiceException(msg);
/* 1537 */           reportError(compDir, se);
/*      */         }
/*      */         else
/*      */         {
/* 1541 */           if (destDir.exists())
/*      */           {
/* 1543 */             if (canDeleteDir)
/*      */             {
/* 1545 */               FileUtils.deleteDirectory(destDir, true);
/* 1546 */               if (destDir.exists())
/*      */               {
/* 1548 */                 String msg = LocaleUtils.encodeMessage("csCantDeleteDestComponentDistDir", null, srcDir.getAbsolutePath(), destDir.getAbsolutePath(), componentName);
/*      */ 
/* 1550 */                 ServiceException se = new ServiceException(msg);
/* 1551 */                 reportError(compDir, se);
/*      */               }
/*      */             }
/*      */             else
/*      */             {
/* 1556 */               String msg = LocaleUtils.encodeMessage("csDestComponentDistDirAlreadyExists", null, srcDir.getAbsolutePath(), destDir.getAbsolutePath(), componentName);
/*      */ 
/* 1558 */               ServiceException se = new ServiceException(msg);
/* 1559 */               reportError(compDir, se);
/*      */             }
/*      */           }
/*      */ 
/* 1563 */           String parent = FileUtils.getParent(destPath);
/* 1564 */           File parentFile = new File(parent);
/* 1565 */           if (!parentFile.exists())
/*      */           {
/* 1567 */             String msg = LocaleUtils.encodeMessage("csDestComponentDistDirParentNotExists", null, srcDir.getAbsolutePath(), destDir.getAbsolutePath(), componentName);
/*      */ 
/* 1569 */             reportError(compDir, new ServiceException(msg));
/*      */           }
/*      */ 
/* 1572 */           if (destDir.exists())
/*      */             continue;
/*      */           try
/*      */           {
/* 1576 */             FileUtils.renameFile(srcPath, destPath);
/*      */           }
/*      */           catch (Exception e)
/*      */           {
/* 1580 */             String msg = LocaleUtils.encodeMessage("csCouldNotRenameComponentDistDir", e.getMessage(), srcDir.getAbsolutePath(), destDir.getAbsolutePath(), componentName);
/*      */ 
/* 1582 */             ServiceException se = new ServiceException(msg, e);
/* 1583 */             Report.trace("system", null, se);
/* 1584 */             reportError(compDir, se);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1591 */     reportMessage("!csInstallCompleted");
/*      */   }
/*      */ 
/*      */   public String getComponentDir()
/*      */   {
/* 1599 */     return this.m_componentDir;
/*      */   }
/*      */ 
/*      */   public void setComponentDir(String cmpDir)
/*      */   {
/* 1604 */     this.m_componentDir = FileUtils.directorySlashes(cmpDir);
/*      */   }
/*      */ 
/*      */   public String expandSourceDirectory(String entryType)
/*      */   {
/* 1613 */     boolean useLegacyLocations = (this.m_isForeign) && (this.m_args.containsKey("UseLegacyComponentDirectories"));
/* 1614 */     String dir = null;
/* 1615 */     if ((entryType.equals("component")) || (entryType.equalsIgnoreCase("componentextra")) || (entryType.equalsIgnoreCase("componentclasses")) || (entryType.equalsIgnoreCase("componentlib")))
/*      */     {
/* 1618 */       dir = this.m_componentDir;
/*      */     }
/* 1620 */     else if (entryType.equals("classes"))
/*      */     {
/* 1622 */       dir = new StringBuilder().append(this.m_idcDir).append("classes/").toString();
/*      */     }
/* 1624 */     else if ((entryType.equals("common")) || (entryType.equals("jsp")) || (entryType.equals("images")) || (entryType.equals("resources")) || (entryType.equals("help")))
/*      */     {
/* 1630 */       if (useLegacyLocations)
/*      */       {
/* 1632 */         dir = new StringBuilder().append(this.m_webDir).append(entryType).append('/').toString();
/*      */       }
/*      */       else
/*      */       {
/* 1636 */         dir = this.m_componentDir;
/*      */       }
/*      */     }
/* 1639 */     else if ((useLegacyLocations) && (entryType.equals("root")))
/*      */     {
/* 1641 */       dir = this.m_idcDir;
/*      */     }
/* 1643 */     else if (entryType.equals("weblayout"))
/*      */     {
/* 1645 */       if (useLegacyLocations)
/*      */       {
/* 1647 */         dir = this.m_webDir;
/*      */       }
/*      */       else
/*      */       {
/* 1651 */         dir = this.m_componentDir;
/*      */       }
/*      */     }
/* 1654 */     else if ((useLegacyLocations) && (entryType.equalsIgnoreCase("data")))
/*      */     {
/* 1656 */       if (this.m_environment == null)
/*      */       {
/* 1658 */         dir = DirectoryLocator.getAppDataDirectory();
/*      */       }
/*      */       else
/*      */       {
/* 1662 */         dir = (String)this.m_environment.get("DataDir");
/*      */       }
/*      */     }
/* 1665 */     else if ((useLegacyLocations) && (entryType.equalsIgnoreCase("bin")))
/*      */     {
/* 1667 */       if (this.m_environment == null)
/*      */       {
/* 1669 */         dir = SharedObjects.getEnvironmentValue("BinDir");
/*      */       }
/*      */       else
/*      */       {
/* 1673 */         dir = (String)this.m_environment.get("BinDir");
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1678 */       String msg = LocaleUtils.encodeMessage("csUnknownFileTypeInManifest", null, entryType);
/*      */ 
/* 1680 */       reportError(entryType, new ServiceException(msg));
/*      */     }
/* 1682 */     if (dir == null)
/*      */     {
/* 1684 */       return dir;
/*      */     }
/* 1686 */     return FileUtils.directorySlashes(dir);
/*      */   }
/*      */ 
/*      */   protected String expandSource(String entryType, String location, DataResultSet manifest, int action, boolean isAsDir)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1693 */     String sourceDir = computeSourceDir(entryType, location, true);
/*      */ 
/* 1696 */     if (entryType.equals("component"))
/*      */     {
/* 1698 */       if (this.m_isBuild)
/*      */       {
/* 1700 */         String compSource = FileUtils.getAbsolutePath(sourceDir, location);
/*      */ 
/* 1702 */         if (FileUtils.checkFile(compSource, true, false) != 0)
/*      */         {
/* 1704 */           String msg = LocaleUtils.encodeMessage("syFileDoesNotExist", null, compSource);
/*      */ 
/* 1706 */           reportError(location, new ServiceException(msg));
/* 1707 */           return null;
/*      */         }
/*      */       }
/* 1710 */       String compName = expandManifestForComponent(location, sourceDir, manifest, action);
/* 1711 */       alterComponentLists(compName, location, action);
/*      */     }
/*      */ 
/* 1714 */     if (isAsDir)
/*      */     {
/* 1716 */       if (sourceDir == null)
/*      */       {
/* 1718 */         return null;
/*      */       }
/* 1720 */       return FileUtils.directorySlashes(sourceDir);
/*      */     }
/* 1722 */     return FileUtils.getAbsolutePath(sourceDir, location);
/*      */   }
/*      */ 
/*      */   protected String computeSourceDir(String entryType, String location, boolean removeSourceLocal)
/*      */   {
/* 1727 */     if (location == null)
/*      */     {
/* 1729 */       String msg = LocaleUtils.encodeMessage("csNoLocationForEntryType", null, entryType);
/*      */ 
/* 1731 */       reportError(location, new ServiceException(msg));
/* 1732 */       return null;
/*      */     }
/*      */ 
/* 1736 */     String sourceDir = this.m_manifestBinder.getLocal(new StringBuilder().append(entryType).append("@").append(location).append(".source").toString());
/*      */ 
/* 1738 */     if (removeSourceLocal)
/*      */     {
/* 1740 */       this.m_manifestBinder.removeLocal(new StringBuilder().append(entryType).append("@").append(location).append(".source").toString());
/*      */     }
/*      */ 
/* 1743 */     if (sourceDir == null)
/*      */     {
/* 1745 */       sourceDir = expandSourceDirectory(entryType);
/* 1746 */       if (sourceDir == null)
/*      */       {
/* 1748 */         String msg = LocaleUtils.encodeMessage("csUnableToComputeSrcDir", null, entryType);
/*      */ 
/* 1750 */         reportError(location, new ServiceException(msg));
/* 1751 */         return null;
/*      */       }
/*      */     }
/*      */ 
/* 1755 */     return sourceDir;
/*      */   }
/*      */ 
/*      */   protected void alterComponentLists(String compName, String destination, int action)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1764 */     if (action == 2)
/*      */     {
/* 1766 */       return;
/*      */     }
/*      */ 
/* 1770 */     if (compName == null)
/*      */     {
/* 1772 */       compName = FileUtils.getName(destination);
/* 1773 */       compName = compName.substring(0, compName.length() - 4);
/*      */     }
/*      */ 
/* 1777 */     this.m_unsuccessfulComponents.add(compName);
/*      */ 
/* 1779 */     Map map = (Map)(Map)this.m_componentBinder.getLocalData().clone();
/* 1780 */     DataBinder.mergeHashTables(map, this.m_manifestBinder.getLocalData());
/* 1781 */     map.put("name", compName);
/* 1782 */     map.put("status", "Disabled");
/*      */ 
/* 1784 */     ComponentListEditor editor = this.m_editor;
/* 1785 */     boolean isEnabled = editor.isComponentEnabled(compName);
/* 1786 */     if (action == 0)
/*      */     {
/* 1788 */       String location = computeComponentLocation(destination);
/* 1789 */       map.put("location", location);
/* 1790 */       editor.addComponent(map, null);
/*      */     }
/* 1792 */     else if (action == 1)
/*      */     {
/* 1794 */       DataBinder binder = editor.getComponentData(compName);
/* 1795 */       String str = binder.getLocal("componentType");
/* 1796 */       List typeList = StringUtils.makeListFromSequenceSimple(str);
/* 1797 */       if (typeList.size() <= 1)
/*      */       {
/* 1799 */         editor.deleteComponent(map);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1804 */     this.m_unsuccessfulComponents.remove(compName);
/* 1805 */     this.m_successfulComponents.addElement(compName);
/* 1806 */     if (isEnabled)
/*      */     {
/* 1808 */       this.m_enabledComponents.addElement(compName);
/*      */     }
/* 1810 */     String msg = (action == 0) ? "csComponentInstallSuccess" : "csComponentUninstallSuccess";
/* 1811 */     msg = LocaleUtils.encodeMessage(msg, null, compName);
/* 1812 */     reportMessage(msg);
/*      */   }
/*      */ 
/*      */   protected void checkValidDestination(String destinationStr)
/*      */     throws ServiceException
/*      */   {
/* 1822 */     File idcClassesDir = new File(new StringBuilder().append(this.m_idcDir).append("classes/intradoc").toString());
/* 1823 */     String idcClassesDirStr = idcClassesDir.getPath();
/*      */ 
/* 1825 */     destinationStr = FileUtils.getAbsolutePath(this.m_idcDir, destinationStr);
/* 1826 */     String destinationDirStr = FileUtils.getDirectory(destinationStr);
/* 1827 */     File destinationDir = new File(destinationDirStr);
/* 1828 */     destinationDirStr = destinationDir.getPath();
/*      */ 
/* 1830 */     if (!destinationDirStr.startsWith(idcClassesDirStr))
/*      */       return;
/* 1832 */     String msg = LocaleUtils.encodeMessage("csCantInstallCoreClasses", null, destinationDir);
/*      */ 
/* 1834 */     throw new ServiceException(msg);
/*      */   }
/*      */ 
/*      */   protected String expandManifestForComponent(String location, String source, DataResultSet manifest, int action)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1846 */     return expandManifestForComponentInternal(location, source, manifest, action, new HashMap());
/*      */   }
/*      */ 
/*      */   protected String expandManifestForComponentInternal(String location, String source, DataResultSet manifest, int action, Map loadList)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1858 */     String[] keys = { "entryType", "location" };
/* 1859 */     FieldInfo[] finfo = ResultSetUtils.createInfoList(manifest, keys, true);
/*      */ 
/* 1863 */     DataBinder temp = null;
/* 1864 */     if ((this.m_zipResource != null) && (action == 0))
/*      */     {
/* 1867 */       temp = extractDataBinderEx(new StringBuilder().append("component/").append(location).toString(), false);
/*      */     }
/*      */     else
/*      */     {
/* 1872 */       temp = extractDataBinderEx(new StringBuilder().append(source).append(location).toString(), true);
/*      */     }
/* 1874 */     loadList.put(location, location);
/*      */ 
/* 1876 */     String locDir = new StringBuilder().append(FileUtils.getDirectory(location)).append("/").toString();
/* 1877 */     String sourceDir = new StringBuilder().append(FileUtils.getDirectory(source)).append("/").toString();
/*      */ 
/* 1881 */     DataResultSet mergeData = new DataResultSet(keys);
/*      */ 
/* 1884 */     for (Enumeration e = temp.getResultSetList(); e.hasMoreElements(); )
/*      */     {
/* 1886 */       ResultSet rset = temp.getResultSet((String)e.nextElement());
/* 1887 */       FieldInfo info = new FieldInfo();
/* 1888 */       if (!rset.getFieldInfo("filename", info)) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1892 */       int indx = info.m_index;
/*      */ 
/* 1894 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*      */       {
/* 1896 */         String fileLocation = rset.getStringValue(indx);
/*      */ 
/* 1898 */         String newLoc = new StringBuilder().append(locDir).append(fileLocation).toString();
/* 1899 */         if (fileLocation.endsWith(".hda"))
/*      */         {
/* 1901 */           if (loadList.get(newLoc) != null)
/*      */           {
/* 1903 */             if (this.m_duplicateLoads.get(newLoc) != null)
/*      */               continue;
/* 1905 */             Report.warning("componentinstaller", new StackTrace(), "csRecursiveManifestDetected", new Object[] { this.m_compName, location });
/*      */ 
/* 1907 */             this.m_duplicateLoads.put(newLoc, newLoc); continue;
/*      */           }
/*      */ 
/* 1911 */           expandManifestForComponentInternal(newLoc, sourceDir, manifest, action, loadList);
/*      */         }
/*      */ 
/* 1915 */         Vector row = mergeData.createEmptyRow();
/* 1916 */         row.setElementAt("componentextra", finfo[0].m_index);
/* 1917 */         row.setElementAt(new StringBuilder().append(locDir).append(fileLocation).toString(), finfo[1].m_index);
/* 1918 */         mergeData.addRow(row);
/*      */ 
/* 1920 */         this.m_manifestBinder.putLocal(new StringBuilder().append("componentextra@").append(locDir).append(fileLocation).append(".source").toString(), sourceDir);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1925 */     manifest.merge("location", mergeData, false);
/*      */ 
/* 1928 */     return temp.getLocal("ComponentName");
/*      */   }
/*      */ 
/*      */   protected void extractZipFileOrDirectory(String entryName, String path, String destination)
/*      */   {
/*      */     try
/*      */     {
/* 1941 */       checkValidDestination(new StringBuilder().append(destination).append("/").append(path).toString());
/*      */ 
/* 1944 */       boolean isError = ZipFunctions.extractZipFileOrDirectoryNotEncodedEntry(this.m_zipResource, entryName, path, destination);
/* 1945 */       if (isError)
/*      */       {
/* 1947 */         String msg = LocaleUtils.encodeMessage("csNoZippedEntry", null, path);
/* 1948 */         reportMessage(msg);
/*      */       }
/*      */       else
/*      */       {
/* 1952 */         reportMessage(new StringBuilder().append("!$  ").append(path).toString());
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1957 */       String errMsg = LocaleUtils.encodeMessage("csUnableToExtract", null, path, destination);
/*      */ 
/* 1959 */       ServiceException se = new ServiceException(errMsg, e);
/* 1960 */       Report.trace("system", null, se);
/* 1961 */       reportError(path, se);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected DataBinder extractDataBinder(String filePath)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1972 */     return extractDataBinderEx(filePath, false);
/*      */   }
/*      */ 
/*      */   protected DataBinder extractDataBinderEx(String filePath, boolean isBuild)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1983 */     DataBinder data = null;
/* 1984 */     if ((this.m_zipResource != null) && (!isBuild))
/*      */     {
/* 1986 */       String msg = LocaleUtils.encodeMessage("csExtractingData", null, filePath);
/*      */ 
/* 1988 */       reportMessage(msg);
/*      */ 
/* 1992 */       String entryKey = StringUtils.encodeHttpHeaderStyle(filePath, false);
/*      */ 
/* 1994 */       ZipEntry entry = this.m_zipResource.getEntry(entryKey);
/* 1995 */       data = ZipFunctions.extractFileAsDataBinder(this.m_zipResource, entry, filePath);
/*      */     }
/*      */     else
/*      */     {
/* 2000 */       String msg = LocaleUtils.encodeMessage("csLoadingDataFromFile", null, filePath);
/*      */ 
/* 2002 */       reportMessage(msg);
/* 2003 */       data = ResourceUtils.readDataBinderFromPath(filePath);
/*      */     }
/* 2005 */     return data;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void setVerbose(boolean newValue)
/*      */   {
/* 2015 */     Report.deprecatedUsage("setVerbose() called.");
/*      */   }
/*      */ 
/*      */   public void reportMessage(String msg)
/*      */   {
/* 2024 */     Report.trace("componentinstaller", LocaleResources.localizeMessage(msg, null), null);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void reportError(String source, String errMsg)
/*      */   {
/* 2038 */     this.m_manifestErrors.put(source, errMsg);
/* 2039 */     this.m_manifestExceptions.put(source, new ServiceException(errMsg));
/*      */   }
/*      */ 
/*      */   public void reportError(String source, Exception exception)
/*      */   {
/* 2051 */     String errMsg = exception.getMessage();
/* 2052 */     if (errMsg == null)
/*      */     {
/* 2054 */       errMsg = "!syNullPointer";
/*      */     }
/* 2056 */     this.m_manifestErrors.put(source, errMsg);
/* 2057 */     this.m_manifestExceptions.put(source, exception);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void closeReaderAndWriter(Reader reader, Writer writer)
/*      */   {
/* 2067 */     SystemUtils.reportDeprecatedUsage("closeReaderAndWriter()");
/*      */     try
/*      */     {
/* 2070 */       if (reader != null)
/*      */       {
/* 2072 */         reader.close();
/*      */       }
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 2077 */       if (SystemUtils.m_verbose)
/*      */       {
/* 2079 */         Report.debug(null, null, ignore);
/*      */       }
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2085 */       if (writer != null)
/*      */       {
/* 2087 */         writer.close();
/*      */       }
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 2092 */       if (!SystemUtils.m_verbose)
/*      */         return;
/* 2094 */       Report.debug(null, null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void closeZipResource()
/*      */   {
/*      */     try
/*      */     {
/* 2106 */       if (this.m_zipResource != null)
/*      */       {
/* 2108 */         this.m_zipResource.close();
/* 2109 */         this.m_zipResource = null;
/*      */       }
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 2114 */       Report.trace(null, null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void applyPermissions(String compName)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2125 */     Properties props = SharedObjects.getOSProperties();
/* 2126 */     DataBinder binder = new DataBinder();
/* 2127 */     binder.setLocalData(props);
/* 2128 */     ExecutionContext context = new ExecutionContextAdaptor();
/* 2129 */     PageMerger merger = new PageMerger(binder, context);
/*      */ 
/* 2131 */     String[] retStr = retrieveComponentNameAndLocation(this.m_manifestBinder);
/* 2132 */     String location = FileUtils.getAbsolutePath(this.m_componentDir, retStr[0]);
/* 2133 */     String compHdaFile = FileUtils.getName(location);
/* 2134 */     String compDir = FileUtils.getDirectory(location);
/*      */ 
/* 2136 */     DataResultSet platformInfo = SharedObjects.getTable("PlatformConfigTable");
/* 2137 */     String osName = EnvUtils.getOSName();
/* 2138 */     String list = ResultSetUtils.findValue(platformInfo, "Platform", osName, "CompatiblePlatforms");
/*      */ 
/* 2140 */     if (list == null)
/*      */     {
/* 2142 */       list = osName;
/*      */     }
/* 2144 */     List l = StringUtils.makeListFromSequenceSimple(list);
/* 2145 */     String[] rsetNames = new String[2 + l.size()];
/* 2146 */     rsetNames[0] = "FilePermissions";
/* 2147 */     rsetNames[1] = new StringBuilder().append("FilePermissions-").append(EnvUtils.getOSFamily()).toString();
/* 2148 */     for (int i = 0; i < rsetNames.length - 2; ++i)
/*      */     {
/* 2150 */       rsetNames[(i + 2)] = new StringBuilder().append("FilePermissions-").append(l.get(i)).toString();
/*      */     }
/*      */ 
/* 2153 */     DataBinder componentDefinition = new DataBinder();
/* 2154 */     ResourceUtils.serializeDataBinder(compDir, compHdaFile, componentDefinition, false, true);
/*      */ 
/* 2156 */     DataBinder[] binders = { this.m_manifestBinder, componentDefinition };
/*      */ 
/* 2158 */     for (int bIndex = 0; bIndex < binders.length; ++bIndex)
/*      */     {
/* 2160 */       for (int i = 0; i < rsetNames.length; ++i)
/*      */       {
/* 2162 */         ResultSet rset = binders[bIndex].getResultSet(rsetNames[i]);
/*      */ 
/* 2164 */         if (rset == null)
/*      */           continue;
/* 2166 */         applyPermissions(compDir, rset, merger);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void applyPermissions(String baseDir, ResultSet rset, PageMerger merger)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2189 */     FieldInfo[] infos = ResultSetUtils.createInfoList(rset, new String[] { "fileName", "permission" }, true);
/*      */ 
/* 2191 */     baseDir = FileUtils.directorySlashes(baseDir);
/* 2192 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/* 2194 */       String fileName = rset.getStringValue(infos[0].m_index);
/*      */       try
/*      */       {
/* 2197 */         fileName = merger.evaluateScript(fileName);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 2202 */         DataException de = new DataException("!csDynHTMLServerScriptExecError");
/* 2203 */         SystemUtils.setExceptionCause(de, e);
/* 2204 */         throw de;
/*      */       }
/* 2206 */       String permission = rset.getStringValue(infos[1].m_index);
/*      */ 
/* 2208 */       if (fileName.indexOf("..") >= 0)
/*      */       {
/* 2210 */         Report.trace("system", new StringBuilder().append("not applying permissions on relative path ").append(fileName).toString(), null);
/*      */       }
/*      */       else {
/* 2213 */         String path = new StringBuilder().append(baseDir).append(fileName).toString();
/* 2214 */         if (FileUtils.checkFile(path, true, false) != 0)
/*      */         {
/* 2216 */           if (!SystemUtils.m_verbose)
/*      */             continue;
/* 2218 */           Report.debug("system", new StringBuilder().append("not applying permissions on nonexistent file ").append(path).toString(), null);
/*      */         }
/*      */         else
/*      */         {
/* 2222 */           applyPermission(path, permission);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void applyPermission(String path, String permission)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2235 */     DataResultSet permissionsTable = SharedObjects.getTable("PermissionConfig");
/*      */ 
/* 2237 */     if (permissionsTable == null)
/*      */     {
/* 2239 */       String msg = LocaleUtils.encodeMessage("csInstallerPermConfigTableMissing", null);
/*      */ 
/* 2241 */       msg = LocaleUtils.encodeMessage("csInstallerPermError", msg, path);
/*      */ 
/* 2243 */       throw new DataException(msg);
/*      */     }
/* 2245 */     Properties props = null;
/* 2246 */     permissionsTable.first();
/* 2247 */     while (permissionsTable.isRowPresent())
/*      */     {
/* 2250 */       props = permissionsTable.getCurrentRowProps();
/* 2251 */       if (props.getProperty("Permission").equals(permission)) {
/*      */         break;
/*      */       }
/*      */ 
/* 2255 */       props = null;
/*      */ 
/* 2248 */       permissionsTable.next();
/*      */     }
/*      */ 
/* 2257 */     if (props == null)
/*      */     {
/* 2259 */       String msg = LocaleUtils.encodeMessage("csInstallerPermConfigError", null);
/*      */ 
/* 2261 */       msg = LocaleUtils.encodeMessage("csInstallerPermError", msg, path);
/*      */ 
/* 2263 */       throw new DataException(msg);
/*      */     }
/* 2265 */     if (this.m_utils == null)
/*      */     {
/* 2267 */       applyPermissionWithExecutable(path, props);
/*      */     }
/*      */     else
/*      */     {
/* 2271 */       applyPermissionWithUtils(path, props);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void applyPermissionWithExecutable(String path, Properties props)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2282 */     String key = (this.m_isUnix) ? "UnixSet" : "WindowsSet";
/* 2283 */     String mode = props.getProperty(key);
/* 2284 */     if ((mode == null) || (mode.length() == 0) || (mode.equals("null")))
/*      */     {
/* 2286 */       return;
/*      */     }
/* 2288 */     DataResultSet executables = SharedObjects.getTable("PermissionExecutables");
/*      */ 
/* 2290 */     Properties exeProps = null;
/* 2291 */     for (executables.first(); executables.isRowPresent(); executables.next())
/*      */     {
/* 2293 */       exeProps = executables.getCurrentRowProps();
/* 2294 */       if (exeProps.getProperty("Platform").equals(this.m_platform)) {
/*      */         break;
/*      */       }
/*      */ 
/* 2298 */       exeProps = null;
/*      */     }
/* 2300 */     if (exeProps == null)
/*      */     {
/* 2302 */       Report.trace("system", new StringBuilder().append("No executable to set permissions on file ").append(path).toString(), null);
/*      */ 
/* 2304 */       return;
/*      */     }
/*      */ 
/* 2307 */     String commandLineTemplate = exeProps.getProperty("CommandLine");
/* 2308 */     if (commandLineTemplate == null)
/*      */     {
/* 2310 */       Report.trace("system", new StringBuilder().append("No command line to set permissions on file ").append(path).toString(), null);
/*      */ 
/* 2312 */       return;
/*      */     }
/* 2314 */     Vector v = StringUtils.parseArray(commandLineTemplate, ' ', ' ');
/* 2315 */     String[] commandLine = (String[])(String[])v.toArray(new String[0]);
/* 2316 */     for (int i = 0; i < commandLine.length; ++i)
/*      */     {
/* 2318 */       if (commandLine[i].equals("${flag}"))
/*      */       {
/* 2320 */         commandLine[i] = mode;
/*      */       } else {
/* 2322 */         if (!commandLine[i].equals("${path}"))
/*      */           continue;
/* 2324 */         commandLine[i] = path;
/*      */       }
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2330 */       byte[] buf = new byte[BufferPool.m_mediumBufferSize];
/* 2331 */       Vector results = new IdcVector();
/* 2332 */       SysInstaller.runCommandSimple(commandLine, results, buf);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2336 */       String msg = LocaleUtils.encodeMessage("csInstallerPermError", null, path);
/*      */ 
/* 2338 */       Report.trace("system", null, e);
/* 2339 */       throw new ServiceException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void applyPermissionWithUtils(String path, Properties props)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2350 */     String setBits = props.getProperty("UnixSetBits");
/* 2351 */     String clearBits = props.getProperty("UnixClearBits");
/* 2352 */     int setMask = Integer.parseInt(setBits, 8);
/* 2353 */     int clearMask = Integer.parseInt(clearBits, 8);
/* 2354 */     PermissionInstaller inst = new PermissionInstaller();
/*      */ 
/* 2358 */     inst.applyPermissions(this.m_utils, path, setMask, clearMask);
/*      */   }
/*      */ 
/*      */   public void doInstallExtra(DataBinder binder, String compName, String location, String fileName, String installID)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2367 */     if (this.m_isForeign)
/*      */     {
/* 2369 */       throw new ServiceException(null, "csComponentCantInstallForeign", new Object[0]);
/*      */     }
/* 2371 */     String locDir = FileUtils.getDirectory(FileUtils.getAbsolutePath(this.m_componentDir, location));
/*      */ 
/* 2373 */     Vector tempSuccessfulComponents = getSucessfulComponents();
/* 2374 */     Vector tempUnsuccessfulComponents = getUnsuccessfulComponents();
/*      */ 
/* 2376 */     ComponentPreferenceData prefData = savePreferenceData(binder, compName, installID, locDir);
/*      */ 
/* 2380 */     this.m_featureUtils = ComponentListManager.getFeatures(8);
/* 2381 */     this.m_featureUtils.checkFeatures(binder, 1);
/* 2382 */     this.m_featureUtils.checkFeatures(binder, 2);
/*      */ 
/* 2387 */     boolean preventDowngrade = StringUtils.convertToBool(binder.getLocal("preventAdditionalComponentDowngrade"), false);
/*      */ 
/* 2389 */     PageMerger pageMerger = new PageMerger(binder, null);
/* 2390 */     String addComps = binder.getLocal("additionalComponents");
/* 2391 */     if ((addComps != null) && (addComps.length() > 0))
/*      */     {
/* 2393 */       Vector v = StringUtils.parseArray(addComps, ',', '^');
/* 2394 */       for (int i = 0; i < v.size(); ++i)
/*      */       {
/* 2396 */         String temp = (String)v.elementAt(i);
/* 2397 */         if ((temp == null) || (temp.length() <= 0))
/*      */           continue;
/* 2399 */         Vector vv = StringUtils.parseArray(temp, ':', '^');
/* 2400 */         if (vv.size() < 2)
/*      */           continue;
/* 2402 */         String aCompName = (String)vv.elementAt(0);
/* 2403 */         String aCompZip = (String)vv.elementAt(1);
/* 2404 */         String param = null;
/* 2405 */         if (vv.size() > 2)
/*      */         {
/* 2407 */           param = (String)vv.elementAt(2);
/*      */         }
/* 2409 */         if ((aCompName == null) || (aCompName.length() == 0))
/*      */         {
/* 2411 */           throw new ServiceException("csAdditionalCompNameMissing");
/*      */         }
/*      */ 
/* 2414 */         if ((aCompZip == null) || (aCompZip.length() == 0))
/*      */         {
/* 2416 */           throw new ServiceException("csAdditionalCompZipNameMissing");
/*      */         }
/*      */ 
/* 2419 */         boolean installAddComp = true;
/* 2420 */         if ((param != null) && (param.length() > 0))
/*      */         {
/* 2422 */           String val = null;
/*      */ 
/* 2426 */           if (param.startsWith("<$"))
/*      */           {
/*      */             try
/*      */             {
/* 2430 */               val = pageMerger.evaluateScript(param);
/* 2431 */               if ((val != null) && (val.length() != 0) && (!val.equals("0")) && (!val.equals("1")) && 
/* 2435 */                 (prefData != null))
/*      */               {
/* 2437 */                 if (prefData.m_installData == null)
/*      */                 {
/* 2439 */                   installAddComp = false;
/*      */                 }
/*      */                 else
/*      */                 {
/* 2443 */                   val = prefData.m_installData.getProperty(val);
/*      */                 }
/*      */               }
/*      */ 
/*      */             }
/*      */             catch (Exception e)
/*      */             {
/* 2450 */               String msg = LocaleUtils.encodeMessage("csAdditionalCompUnableToEvaluateParameter", null, param);
/*      */ 
/* 2452 */               throw new ServiceException(msg, e);
/*      */             }
/*      */           }
/* 2455 */           else if (prefData != null)
/*      */           {
/* 2457 */             if (prefData.m_installData == null)
/*      */             {
/* 2459 */               installAddComp = false;
/*      */             }
/*      */             else
/*      */             {
/* 2463 */               val = prefData.m_installData.getProperty(param);
/*      */             }
/*      */           }
/*      */ 
/* 2467 */           if ((installAddComp) && (!StringUtils.convertToBool(val, true)))
/*      */           {
/* 2469 */             installAddComp = false;
/* 2470 */             this.m_preventedComponents.add(aCompName);
/*      */           }
/*      */         }
/*      */ 
/* 2474 */         if (!installAddComp)
/*      */           continue;
/* 2476 */         tempUnsuccessfulComponents.addElement(aCompName);
/*      */ 
/* 2478 */         String aFileName = new StringBuilder().append(locDir).append("/").append(aCompZip).toString();
/* 2479 */         DataBinder tempAddCompBinder = ZipFunctions.extractFileAsDataBinder(aFileName, "manifest.hda");
/* 2480 */         String[] retStr = retrieveComponentNameAndLocation(tempAddCompBinder);
/* 2481 */         String aLocation = retStr[0];
/*      */ 
/* 2483 */         Map args = new HashMap();
/* 2484 */         args.put("Install", "true");
/* 2485 */         args.put("ZipName", aFileName);
/* 2486 */         if (this.m_args.get("PruneFiles") != null)
/*      */         {
/* 2490 */           args.put("PruneFiles", this.m_args.get("PruneFiles"));
/*      */         }
/*      */ 
/* 2494 */         DataBinder aResDefData = ZipFunctions.extractFileAsDataBinder(aFileName, new StringBuilder().append("component/").append(aLocation).toString());
/* 2495 */         if (aResDefData == null)
/*      */         {
/* 2497 */           throw new ServiceException("!csUnableToLoadResourceDefinition");
/*      */         }
/*      */ 
/* 2500 */         retrieveDefaultPreferenceData(aResDefData, aCompName, binder, pageMerger, aFileName, true, prefData);
/*      */ 
/* 2504 */         ResultSet tempRs = tempAddCompBinder.getResultSet("Manifest");
/* 2505 */         String tempLocation = ResultSetUtils.findValue(tempRs, "entryType", "component", "location");
/* 2506 */         DataBinder componentDef = ZipFunctions.extractFileAsDataBinder(aFileName, new StringBuilder().append("component/").append(tempLocation).toString());
/*      */ 
/* 2509 */         this.m_featureUtils = ComponentListManager.getFeatures(8);
/*      */ 
/* 2513 */         this.m_featureUtils.checkFeatures(componentDef, 1);
/*      */ 
/* 2516 */         if ((!this.m_featureUtils.checkFeatures(componentDef, 2)) && (preventDowngrade))
/*      */         {
/* 2522 */           this.m_preventedComponents.add(aCompName);
/*      */         }
/*      */         else
/*      */         {
/* 2527 */           boolean doNotBackupZip = true;
/* 2528 */           Properties props = binder.getLocalData();
/*      */ 
/* 2530 */           if (props.containsKey("disableZipFileBackup"))
/*      */           {
/* 2532 */             doNotBackupZip = StringUtils.convertToBool(props.getProperty("disableZipFileBackup"), true);
/* 2533 */             if (SystemUtils.m_verbose)
/*      */             {
/* 2535 */               SystemUtils.trace("system", new StringBuilder().append("disableZipFileBackup passed from parent component, value: ").append(doNotBackupZip).toString());
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/* 2540 */             doNotBackupZip = DataBinderUtils.getBoolean(componentDef, "disableZipFileBackup", false);
/* 2541 */             if (SystemUtils.m_verbose)
/*      */             {
/* 2543 */               SystemUtils.trace("system", new StringBuilder().append("disableZipFileBackup extracted from child component, value:").append(doNotBackupZip).toString());
/*      */             }
/*      */           }
/*      */ 
/* 2547 */           if (!doNotBackupZip)
/*      */           {
/* 2549 */             args.put("Backup", "true");
/* 2550 */             args.put("Overwrite", "false");
/*      */           }
/*      */           else
/*      */           {
/* 2554 */             args.put("Backup", "false");
/* 2555 */             args.put("Overwrite", "true");
/*      */           }
/*      */ 
/* 2558 */           String aInstallId = aResDefData.getLocal("installID");
/*      */ 
/* 2560 */           args.put("BackupZipName", getComponentBackupPath(installID, aCompName));
/* 2561 */           executeInstaller(componentDef, tempAddCompBinder, installID, aCompName, args);
/* 2562 */           doInstallExtra(aResDefData, aCompName, aLocation, aFileName, aInstallId);
/* 2563 */           tempUnsuccessfulComponents.remove(aCompName);
/* 2564 */           tempSuccessfulComponents.addElement(aCompName);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2571 */     this.m_unsuccessfulComponents = tempUnsuccessfulComponents;
/* 2572 */     this.m_successfulComponents = tempSuccessfulComponents;
/*      */ 
/* 2576 */     writeLog();
/*      */   }
/*      */ 
/*      */   public ComponentPreferenceData savePreferenceData(DataBinder cmpData, String compName, String installID, String locDir)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2582 */     boolean hasPreferenceData = StringUtils.convertToBool(cmpData.getLocal("hasPreferenceData"), false);
/*      */ 
/* 2584 */     String compDataDir = CompInstallUtils.getInstallConfPath(installID, compName);
/*      */ 
/* 2587 */     ComponentPreferenceData prefData = null;
/* 2588 */     if (hasPreferenceData)
/*      */     {
/* 2590 */       FileUtils.checkOrCreateDirectory(compDataDir, 2);
/*      */ 
/* 2594 */       if ((installID == null) || (installID.length() == 0))
/*      */       {
/* 2596 */         throw new ServiceException("!csInstallIdRequired");
/*      */       }
/*      */ 
/* 2599 */       prefData = new ComponentPreferenceData(locDir, compDataDir);
/* 2600 */       prefData.load();
/*      */ 
/* 2602 */       DataResultSet prefTable = prefData.getPreferenceTable();
/* 2603 */       if (prefTable == null)
/*      */       {
/* 2605 */         throw new ServiceException("!csUnableToLoadPreferenceData");
/*      */       }
/* 2607 */       if (prefTable.isEmpty())
/*      */       {
/* 2609 */         Report.trace("componentinstaller", "Expecting preference data, but PreferenceData resultset is empty", null);
/*      */ 
/* 2611 */         return prefData;
/*      */       }
/*      */ 
/* 2614 */       FieldInfo[] infos = ResultSetUtils.createInfoList(prefTable, new String[] { "pName", "pNewMsgType", "pIsRequired" }, true);
/*      */ 
/* 2616 */       for (prefTable.first(); prefTable.isRowPresent(); prefTable.next())
/*      */       {
/* 2618 */         String msgType = prefTable.getStringValue(infos[1].m_index);
/* 2619 */         if (msgType.equalsIgnoreCase("info"))
/*      */           continue;
/* 2621 */         String name = prefTable.getStringValue(infos[0].m_index);
/* 2622 */         String value = cmpData.getLocal(name);
/* 2623 */         boolean required = StringUtils.convertToBool(prefTable.getStringValue(infos[2].m_index), false);
/*      */ 
/* 2625 */         if (value == null)
/*      */         {
/* 2627 */           if (required)
/*      */           {
/* 2629 */             throw new DataException(null, "syMissingArgument", new Object[] { name });
/*      */           }
/* 2631 */           value = "";
/*      */         }
/*      */ 
/* 2634 */         if ((name == null) || (name.length() <= 0))
/*      */           continue;
/* 2636 */         prefData.m_installData.put(name, value);
/* 2637 */         if ((msgType.equalsIgnoreCase("installonly")) || (value.length() <= 0)) {
/*      */           continue;
/*      */         }
/* 2640 */         prefData.m_configData.put(name, value);
/*      */       }
/*      */ 
/* 2645 */       prefData.save();
/*      */     }
/* 2647 */     return prefData;
/*      */   }
/*      */ 
/*      */   public ComponentPreferenceData retrieveDefaultPreferenceData(DataBinder cmpData, String cmpName, DataBinder binder, PageMerger pageMerger, String zipName, boolean fromZip, ComponentPreferenceData parentPrefData)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2657 */     ComponentPreferenceData prefData = null;
/* 2658 */     if (StringUtils.convertToBool(cmpData.getLocal("hasPreferenceData"), false))
/*      */     {
/* 2660 */       prefData = new ComponentPreferenceData();
/* 2661 */       String installId = cmpData.getLocal("installID");
/* 2662 */       prefData.m_dataDir = CompInstallUtils.getInstallConfPath(installId, cmpName);
/*      */ 
/* 2664 */       if (fromZip)
/*      */       {
/* 2666 */         retrievePreferenceData(prefData, zipName, cmpName, installId);
/*      */       }
/*      */       else
/*      */       {
/* 2670 */         retrievePreferenceDataFromFile(prefData, cmpName, installId);
/*      */       }
/*      */ 
/* 2677 */       DataResultSet prefRset = prefData.getPreferenceTable();
/* 2678 */       FieldInfo[] infos = ResultSetUtils.createInfoList(prefRset, new String[] { "pName", "pValue" }, true);
/*      */ 
/* 2680 */       for (prefRset.first(); prefRset.isRowPresent(); prefRset.next())
/*      */       {
/* 2682 */         String prefName = prefRset.getStringValue(infos[0].m_index);
/* 2683 */         String prefValue = null;
/* 2684 */         if (binder != null)
/*      */         {
/* 2686 */           prefValue = binder.getLocal(prefName);
/*      */         }
/* 2688 */         if ((prefValue == null) || (prefValue.length() == 0))
/*      */         {
/* 2690 */           prefValue = prefRset.getStringValue(infos[1].m_index);
/*      */           try
/*      */           {
/* 2693 */             prefValue = pageMerger.evaluateScript(prefValue);
/*      */           }
/*      */           catch (IOException ignore)
/*      */           {
/* 2697 */             if (SystemUtils.m_verbose)
/*      */             {
/* 2699 */               Report.debug("componentwizard", new StringBuilder().append("ComponentInstaller.doInstallExtra: Error for prefValue=").append(prefValue).toString(), ignore);
/*      */             }
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 2705 */         if ((prefValue == null) || (prefValue.length() <= 0))
/*      */           continue;
/* 2707 */         cmpData.putLocal(prefName, prefValue);
/*      */ 
/* 2711 */         prefData.m_configData.put(prefName, prefValue);
/* 2712 */         prefData.m_installData.put(prefName, prefValue);
/*      */       }
/*      */ 
/* 2718 */       if ((parentPrefData != null) && (parentPrefData.m_installData == null))
/*      */       {
/* 2720 */         cmpData.m_localData.putAll(parentPrefData.m_installData);
/*      */       }
/* 2722 */       if ((parentPrefData != null) && (parentPrefData.m_configData == null))
/*      */       {
/* 2724 */         cmpData.m_localData.putAll(parentPrefData.m_configData);
/*      */       }
/*      */     }
/* 2727 */     return prefData;
/*      */   }
/*      */ 
/*      */   public String getComponentBackupPath(String installID, String compName)
/*      */   {
/* 2732 */     SimpleDateFormat frmt = new SimpleDateFormat("yyyyMMddHHmmss");
/* 2733 */     frmt.setTimeZone(TimeZone.getDefault());
/* 2734 */     String dateStr = frmt.format(new Date());
/*      */ 
/* 2736 */     return new StringBuilder().append(CompInstallUtils.getInstallConfPath(installID, compName)).append(compName).append("-").append(dateStr).append(".zip").toString();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void executeInstaller(DataBinder manifestData, String installID, String compName)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2747 */     executeInstaller(null, manifestData, installID, compName, new HashMap());
/*      */   }
/*      */ 
/*      */   public void executeInstaller(DataBinder compData, DataBinder manifestData, String installID, String compName, Map args)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2754 */     initEx(compName, compData, manifestData, args);
/*      */ 
/* 2756 */     if ((!this.m_isBuild) && ((
/* 2758 */       (this.m_logFileName == null) || (this.m_logDataDir == null))))
/*      */     {
/* 2760 */       if ((installID != null) && (installID.length() > 0))
/*      */       {
/* 2762 */         this.m_logDataDir = installID;
/*      */       }
/*      */       else
/*      */       {
/* 2766 */         this.m_logDataDir = compName;
/*      */       }
/* 2768 */       this.m_logFileName = new StringBuilder().append(this.m_logDataDir).append("-").append(Long.toString(new Date().getTime())).toString();
/*      */     }
/*      */ 
/* 2772 */     this.m_backupZipName = new StringBuilder().append(compName).append("-").append(Long.toString(new Date().getTime())).append(".zip").toString();
/* 2773 */     executeManifest();
/* 2774 */     closeZipResource();
/* 2775 */     if ((!this.m_isBuild) && (!this.m_isUninstall))
/*      */     {
/* 2777 */       applyPermissions(compName);
/*      */     }
/*      */ 
/* 2781 */     Map exceptions = getExceptions();
/* 2782 */     if ((exceptions != null) && (!exceptions.isEmpty()))
/*      */     {
/* 2786 */       String errorString = "!csAdminManifestErrors";
/* 2787 */       ServiceException se = new ServiceException(errorString);
/* 2788 */       for (Iterator i$ = exceptions.entrySet().iterator(); i$.hasNext(); ) { Object entry = i$.next();
/*      */ 
/* 2790 */         Throwable t = (Throwable)((Map.Entry)entry).getValue();
/* 2791 */         se.addCause(t); }
/*      */ 
/* 2793 */       throw se;
/*      */     }
/*      */ 
/* 2796 */     String logMsg = "csComponentInstallSuccess";
/* 2797 */     if (this.m_isUninstall)
/*      */     {
/* 2799 */       logMsg = "csComponentUninstallSuccess";
/*      */     }
/* 2801 */     else if (this.m_isBuild)
/*      */     {
/* 2803 */       logMsg = "csComponentBuildSuccess";
/*      */     }
/*      */ 
/* 2806 */     Report.info("componentinstaller", null, logMsg, new Object[] { compName });
/*      */   }
/*      */ 
/*      */   public String[] retrieveComponentNameAndLocation(DataBinder manifestData)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2812 */     String[] retVal = new String[2];
/*      */ 
/* 2814 */     ResultSet manifest = manifestData.getResultSet("Manifest");
/* 2815 */     if (manifest == null)
/*      */     {
/* 2817 */       throw new ServiceException("!csManifestResultSetMissing");
/*      */     }
/*      */ 
/* 2820 */     String location = ResultSetUtils.findValue(manifest, "entryType", "component", "location");
/*      */ 
/* 2823 */     if (location == null)
/*      */     {
/* 2825 */       throw new ServiceException("!csNoComponentSpecified");
/*      */     }
/*      */ 
/* 2828 */     String compName = manifestData.getLocal("ComponentName");
/* 2829 */     if ((compName == null) || (compName.length() == 0))
/*      */     {
/* 2831 */       compName = FileUtils.getName(location);
/* 2832 */       compName = compName.substring(0, compName.length() - 4);
/*      */     }
/*      */ 
/* 2835 */     retVal[0] = location;
/* 2836 */     retVal[1] = compName;
/* 2837 */     return retVal;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public PreferenceData retrievePreferenceData(String zipPath, String compName, String installID)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2845 */     PreferenceData data = new PreferenceData();
/* 2846 */     retrievePreferenceData(data, zipPath, compName, installID);
/* 2847 */     return data;
/*      */   }
/*      */ 
/*      */   public void retrievePreferenceData(ComponentPreferenceData data, String zipPath, String compName, String installID)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2854 */     if (!this.m_directoriesInit)
/*      */     {
/* 2856 */       loadIdcDir();
/*      */     }
/*      */ 
/* 2859 */     DataBinder tmpBinder = readFileAsBinder(zipPath, new StringBuilder().append("component/").append(compName.toLowerCase()).append("/preference.hda").toString());
/*      */ 
/* 2862 */     buildPreferenceData(tmpBinder, data, compName, installID);
/*      */   }
/*      */ 
/*      */   public void retrievePreferenceDataFromFile(ComponentPreferenceData prefData, String cmpName, String installId)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2868 */     String dir = ComponentLoader.getComponentDir(cmpName);
/* 2869 */     DataBinder prefBinder = ResourceUtils.readDataBinder(dir, "preference.hda");
/* 2870 */     buildPreferenceData(prefBinder, prefData, cmpName, installId);
/*      */   }
/*      */ 
/*      */   public void buildPreferenceData(DataBinder prefBinder, ComponentPreferenceData data, String compName, String installID)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2876 */     DataResultSet prefRset = (DataResultSet)prefBinder.getResultSet("PreferenceData");
/* 2877 */     if (prefRset == null)
/*      */     {
/* 2879 */       throw new ServiceException(LocaleUtils.encodeMessage("csUnableToLoadPreferenceData", null, compName));
/*      */     }
/*      */ 
/* 2883 */     if ((installID == null) || (installID.length() == 0))
/*      */     {
/* 2885 */       throw new ServiceException("!csInstallIdRequired");
/*      */     }
/*      */ 
/* 2888 */     Properties installData = ComponentPreferenceData.getComponentData(compName, installID, 3);
/*      */ 
/* 2890 */     prefRset = ComponentPreferenceData.upgradePrefData(prefRset);
/* 2891 */     FieldInfo[] infos = ResultSetUtils.createInfoList(prefRset, new String[] { "pName", "pValue", "pAlwaysUseDefaultsOnInstall" }, true);
/*      */ 
/* 2893 */     for (prefRset.first(); prefRset.isRowPresent(); prefRset.next())
/*      */     {
/* 2896 */       boolean alwaysUseDefaults = StringUtils.convertToBool(prefRset.getStringValue(infos[2].m_index), false);
/*      */ 
/* 2898 */       if (alwaysUseDefaults) {
/*      */         continue;
/*      */       }
/* 2901 */       String name = prefRset.getStringValue(infos[0].m_index);
/* 2902 */       String instVal = installData.getProperty(name);
/* 2903 */       if (instVal == null)
/*      */         continue;
/* 2905 */       prefRset.setCurrentValue(infos[1].m_index, instVal);
/*      */     }
/*      */ 
/* 2910 */     data.init(prefRset);
/* 2911 */     data.upgrade();
/*      */   }
/*      */ 
/*      */   public void retrievePreferenceResources(String zipPath, String compName, ResourceContainer res)
/*      */   {
/* 2916 */     String instStr = new StringBuilder().append("component/").append(compName).append("/install_strings.htm").toString();
/* 2917 */     String tempInstStrDir = new StringBuilder().append(this.m_componentDir).append(compName).toString();
/* 2918 */     String tempInstStrPath = new StringBuilder().append(tempInstStrDir).append("/install_strings.htm").toString();
/*      */     try
/*      */     {
/* 2922 */       FileUtils.checkOrCreateDirectory(tempInstStrDir, 2);
/* 2923 */       ZipFunctions.extractFileFromZip(zipPath, instStr, tempInstStrPath);
/* 2924 */       if (FileUtils.checkFile(tempInstStrPath, true, false) == 0)
/*      */       {
/* 2927 */         DataLoader.cacheResourceFile(res, tempInstStrPath);
/*      */ 
/* 2930 */         ComponentPreferenceData.loadStrings(SharedObjects.getResources(), tempInstStrPath);
/*      */       }
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/* 2935 */       if (!SystemUtils.m_verbose)
/*      */         return;
/* 2937 */       Report.debug(null, null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkVersion(String compName, DataBinder binder)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2944 */     String serverVersion = binder.getLocal("serverVersion");
/* 2945 */     checkVersion(compName, serverVersion);
/*      */   }
/*      */ 
/*      */   public void checkVersion(String compName, String serverVersion) throws ServiceException, DataException
/*      */   {
/* 2950 */     if ((serverVersion == null) || (serverVersion.length() == 0)) {
/* 2951 */       return;
/*      */     }
/* 2953 */     String csVersion = VersionInfo.getProductVersionInfo();
/* 2954 */     if (csVersion.equals("0.0.0.0"))
/* 2955 */       return;
/* 2956 */     if (SystemUtils.isOlderVersion(csVersion, serverVersion))
/* 2957 */       throw new ServiceException(LocaleUtils.encodeMessage("csComponentNotSupportCSVersion", null, compName, csVersion, serverVersion));
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public boolean checkFeatures(DataBinder componentDefinition, int option)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2968 */     return this.m_featureUtils.checkFeatures(componentDefinition, option);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public Map getAppFeatureLevels(boolean getEnabled, boolean getDisabled, String[] specificComponents)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2978 */     List l = Arrays.asList(specificComponents);
/* 2979 */     return this.m_featureUtils.getAppFeatureLevels(getEnabled, getDisabled, l);
/*      */   }
/*      */ 
/*      */   public DataBinder getLog()
/*      */   {
/* 2987 */     return this.m_log;
/*      */   }
/*      */ 
/*      */   public String getLogDataDir()
/*      */   {
/* 2995 */     return this.m_logDataDir;
/*      */   }
/*      */ 
/*      */   public String getLogFileName()
/*      */   {
/* 3003 */     return this.m_logFileName;
/*      */   }
/*      */ 
/*      */   public void setLogLocation(String logDataDir, String logFileName)
/*      */   {
/* 3013 */     this.m_logDataDir = logDataDir;
/* 3014 */     this.m_logFileName = logFileName;
/*      */   }
/*      */ 
/*      */   public String getLogPath()
/*      */   {
/* 3022 */     return new StringBuilder().append(FileUtils.directorySlashes(DirectoryLocator.getAppDataDirectory())).append("components/").append(this.m_logDataDir).append("/").append(this.m_logFileName).append(".hda").toString();
/*      */   }
/*      */ 
/*      */   public void writeLog()
/*      */     throws ServiceException
/*      */   {
/* 3032 */     DataResultSet featureErrors = this.m_featureUtils.getFeatureErrors();
/* 3033 */     boolean isStrict = this.m_args.get("Strict") != null;
/* 3034 */     if ((isStrict) && (featureErrors.getNumRows() > 0))
/*      */     {
/* 3036 */       IdcMessage origMsg = null;
/* 3037 */       IdcMessage msg = null;
/* 3038 */       for (SimpleParameters params : featureErrors.getSimpleParametersIterable())
/*      */       {
/* 3040 */         IdcMessage tmpMsg = IdcMessageFactory.lc(params.get("errMsg"), new Object[] { params.get("feature"), params.get("featureLevel"), params.get("currFeatureLevel") });
/*      */ 
/* 3045 */         if (msg == null)
/*      */         {
/* 3047 */           origMsg = msg = tmpMsg;
/*      */         }
/*      */         else
/*      */         {
/* 3051 */           msg.m_prior = tmpMsg;
/* 3052 */           msg = tmpMsg;
/*      */         }
/*      */       }
/* 3055 */       throw new ServiceException(null, origMsg);
/*      */     }
/* 3057 */     this.m_log.addResultSet("FeatureErrors", featureErrors);
/*      */ 
/* 3060 */     Vector compList = getSucessfulComponents();
/* 3061 */     if ((compList != null) && (compList.size() > 0))
/*      */     {
/* 3063 */       this.m_log.putLocal("installedComponents", StringUtils.createStringSimple(compList));
/*      */     }
/* 3065 */     compList = getUnsuccessfulComponents();
/* 3066 */     if ((compList != null) && (compList.size() > 0))
/*      */     {
/* 3068 */       this.m_log.putLocal("notInstalledComponents", StringUtils.createStringSimple(compList));
/*      */     }
/*      */ 
/* 3071 */     if ((this.m_args.get("PruneFiles") != null) && (featureErrors.getNumRows() <= 0) && (((compList == null) || (compList.size() <= 0)))) {
/*      */       return;
/*      */     }
/*      */ 
/* 3075 */     String path = getLogPath();
/* 3076 */     String parent = FileUtils.getParent(path);
/* 3077 */     FileUtils.checkOrCreateDirectory(parent, 2);
/* 3078 */     ResourceUtils.serializeDataBinder("", path, this.m_log, true, false);
/*      */   }
/*      */ 
/*      */   public void readLog()
/*      */     throws ServiceException
/*      */   {
/* 3088 */     if ((this.m_logDataDir == null) || (this.m_logDataDir.length() <= 0) || (this.m_logFileName == null) || (this.m_logFileName.length() <= 0)) {
/*      */       return;
/*      */     }
/* 3091 */     DataBinder binder = ResourceUtils.readDataBinderFromPath(getLogPath());
/* 3092 */     this.m_log.merge(binder);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 3137 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99018 $";
/*      */   }
/*      */ 
/*      */   public static class DuplicateEntryFilter
/*      */     implements ResultSetFilter
/*      */   {
/*      */     public final Set<String> m_entryNames;
/*      */ 
/*      */     public DuplicateEntryFilter(Set<String> entryNamesSet)
/*      */     {
/* 3120 */       this.m_entryNames = entryNamesSet;
/*      */     }
/*      */ 
/*      */     public int checkRow(String val, int curNumRows, Vector row) {
/* 3124 */       String entryName = (String)row.get(0);
/* 3125 */       if (this.m_entryNames.contains(entryName))
/*      */       {
/* 3127 */         return 0;
/*      */       }
/* 3129 */       this.m_entryNames.add(entryName);
/* 3130 */       return 1;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static class DirectoryNameFilter
/*      */     implements ResultSetFilter
/*      */   {
/*      */     public final String m_filterStr;
/*      */ 
/*      */     public DirectoryNameFilter(String filter)
/*      */     {
/* 3102 */       this.m_filterStr = filter;
/*      */     }
/*      */ 
/*      */     public int checkRow(String val, int curNumRows, Vector row) {
/* 3106 */       if (StringUtils.matchChars(val, this.m_filterStr, true, true, '*', '?'))
/*      */       {
/* 3108 */         return 0;
/*      */       }
/* 3110 */       return 1;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ComponentInstaller
 * JD-Core Version:    0.5.4
 */