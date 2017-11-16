/*      */ package intradoc.server.utils;
/*      */ 
/*      */ import intradoc.common.FeaturesInterface;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainerUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.MapParameters;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.io.IdcBasicIO;
/*      */ import intradoc.io.IdcBasicIOImplementor;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.ComponentLoader;
/*      */ import intradoc.shared.ResultSetTreeSort;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collection;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ComponentListEditor
/*      */ {
/*      */   protected String m_intradocDir;
/*      */   protected String m_configDir;
/*      */   protected String m_compDir;
/*      */   protected String m_homeDir;
/*      */   protected Map<String, String> m_environment;
/*      */   protected String m_listingFile;
/*      */   protected DataBinder m_compBinder;
/*      */   protected DataResultSet m_componentSet;
/*      */   protected Map m_componentMap;
/*   90 */   public final String[] COMPONENT_INFO_COLUMNS = { "name", "location", "isLoaded", "type", "version", "reason", "exists" };
/*      */   protected DataResultSet m_componentReportSet;
/*      */   protected StateCfg m_stateCfg;
/*  102 */   public static final String[] COMPONENT_COLUMNS = { "name", "location", "status", "classpath", "libpath", "installID", "featureExtensions", "classpathorder", "libpathorder", "Launchers", "LaunchersOrder", "componentsToDisable", "componentTags", "componentType", "useType", "version", "hasPreferenceData" };
/*      */   public IdcBasicIO m_io;
/*      */   public boolean m_isAdminServer;
/*      */   public boolean m_isListsOnly;
/*      */   protected LegacyListEditor m_legacyEditor;
/*      */   protected boolean m_isLegacy;
/*      */   protected boolean m_isCanCreate;
/*      */   protected String m_encoding;
/*  147 */   protected long m_timeStamp = -2L;
/*      */ 
/*  152 */   protected long m_lastLoadedTS = -2L;
/*      */   protected boolean m_componentsInDev;
/*      */ 
/*      */   public ComponentListEditor()
/*      */   {
/*  164 */     this.m_stateCfg = new StateCfg();
/*  165 */     this.m_legacyEditor = new LegacyListEditor();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void init()
/*      */     throws DataException, ServiceException
/*      */   {
/*  175 */     init(false);
/*      */   }
/*      */ 
/*      */   public void init(boolean isCreate)
/*      */     throws DataException, ServiceException
/*      */   {
/*  183 */     this.m_isCanCreate = isCreate;
/*      */ 
/*  185 */     String intradocDir = SharedObjects.getEnvironmentValue("IntradocDir");
/*  186 */     String configDir = SharedObjects.getEnvironmentValue("ConfigDir");
/*  187 */     String compDir = new StringBuilder().append(SharedObjects.getEnvironmentValue("DataDir")).append("components/").toString();
/*  188 */     String homeDir = SharedObjects.getEnvironmentValue("IdcHomeDir");
/*  189 */     Properties safeEnv = SharedObjects.getSafeEnvironment();
/*  190 */     init(intradocDir, configDir, compDir, homeDir, safeEnv);
/*      */   }
/*      */ 
/*      */   public void init(String intradocDir, String configDir, String compDir, String homeDir, Map env)
/*      */     throws DataException, ServiceException
/*      */   {
/*  199 */     this.m_componentsInDev = SharedObjects.getEnvValueAsBoolean("ComponentsUseDevClasses", SystemUtils.m_isDevelopmentEnvironment);
/*      */ 
/*  201 */     this.m_intradocDir = FileUtils.directorySlashes(intradocDir);
/*  202 */     this.m_configDir = FileUtils.directorySlashes(configDir);
/*  203 */     this.m_compDir = FileUtils.directorySlashes(compDir);
/*      */ 
/*  205 */     this.m_environment = env;
/*      */ 
/*  207 */     this.m_stateCfg.init(this.m_intradocDir, this.m_configDir, homeDir);
/*      */ 
/*  210 */     FileUtils.checkOrCreateDirectory(this.m_compDir, 2);
/*      */ 
/*  212 */     this.m_homeDir = homeDir;
/*  213 */     if (this.m_homeDir != null)
/*      */     {
/*  215 */       this.m_homeDir = FileUtils.directorySlashes(this.m_homeDir);
/*      */     }
/*      */ 
/*  218 */     String productName = ComponentLocationUtils.getEnvironmentValue("IdcProductName", this.m_environment);
/*      */ 
/*  220 */     if (productName == null)
/*      */     {
/*  222 */       throw new ServiceException(null, "csUnableToFindValue", new Object[] { "IdcProductName" });
/*      */     }
/*  224 */     setProductName(productName);
/*      */ 
/*  226 */     loadComponents();
/*      */   }
/*      */ 
/*      */   public void setProductName(String productName)
/*      */   {
/*  237 */     this.m_listingFile = new StringBuilder().append(productName).append("_components.hda").toString();
/*      */   }
/*      */ 
/*      */   public Map<String, String> getEnvironment()
/*      */   {
/*  247 */     return this.m_environment;
/*      */   }
/*      */ 
/*      */   public void setIO(IdcBasicIO io)
/*      */   {
/*  252 */     this.m_io = io;
/*      */   }
/*      */ 
/*      */   public void loadComponents() throws DataException, ServiceException
/*      */   {
/*  257 */     loadFromFiles();
/*      */ 
/*  259 */     Map componentMap = new HashMap();
/*  260 */     DataResultSet cmpReport = new DataResultSet(this.COMPONENT_INFO_COLUMNS);
/*      */     try
/*      */     {
/*  264 */       ComponentListUtils.updateComponentListColumns(this.m_componentSet);
/*      */ 
/*  266 */       int statusIndex = ResultSetUtils.getIndexMustExist(this.m_componentSet, "status");
/*  267 */       for (this.m_componentSet.first(); this.m_componentSet.isRowPresent(); this.m_componentSet.next())
/*      */       {
/*  269 */         String status = this.m_componentSet.getStringValue(statusIndex);
/*  270 */         Map map = this.m_componentSet.getCurrentRowMap();
/*  271 */         if (status.equalsIgnoreCase("disabled"))
/*      */         {
/*  274 */           this.m_componentSet.setCurrentValue(statusIndex, "Disabled");
/*      */         }
/*  276 */         if (this.m_isListsOnly)
/*      */           continue;
/*  278 */         loadComponentHdaFile(map, componentMap, cmpReport);
/*      */       }
/*      */ 
/*  282 */       sortComponentReport(cmpReport);
/*      */ 
/*  285 */       this.m_componentMap = componentMap;
/*  286 */       this.m_componentReportSet = cmpReport;
/*  287 */       this.m_lastLoadedTS = System.currentTimeMillis();
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  291 */       throw new ServiceException(e, "csComponentUnableToSetDefaultStatus", new Object[0]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void save()
/*      */     throws ServiceException
/*      */   {
/*  300 */     FileUtils.reserveDirectory(this.m_compDir);
/*  301 */     OutputStream output = null;
/*      */     try
/*      */     {
/*  304 */       if (SharedObjects.getEnvValueAsBoolean("IsInstallerEnv", false))
/*      */       {
/*      */         try
/*      */         {
/*  311 */           output = FileUtils.openOutputStream(new StringBuilder().append(this.m_compDir).append(this.m_listingFile).toString(), 0);
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*  317 */           throw new ServiceException(e, "csComponentsListingSaveError", new Object[] { new StringBuilder().append(this.m_compDir).append(this.m_listingFile).toString() });
/*      */         }
/*      */       }
/*      */ 
/*  321 */       saveListingFile(0, output, null);
/*      */ 
/*  326 */       if (SharedObjects.getEnvValueAsBoolean("AutoSaveStateCfg", true))
/*      */       {
/*  328 */         this.m_stateCfg.updateStateConfig(this.m_componentSet, this.m_componentsInDev, this.m_environment);
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/*  347 */       FileUtils.releaseDirectory(this.m_compDir);
/*  348 */       FileUtils.closeObject(output);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void sortComponentReport(DataResultSet cmpReport)
/*      */   {
/*  354 */     int compNameIndex = cmpReport.getFieldInfoIndex("name");
/*  355 */     int isLoadedIndex = cmpReport.getFieldInfoIndex("isLoaded");
/*      */ 
/*  357 */     ResultSetTreeSort sorter = new ResultSetTreeSort(cmpReport);
/*  358 */     sorter.m_fieldSortTypes = new int[] { 6, 3 };
/*  359 */     sorter.m_isAscendingArray = new boolean[] { true, false };
/*  360 */     sorter.m_isCaseSensitive = false;
/*  361 */     sorter.m_isMulticolumnSort = true;
/*  362 */     sorter.m_sortColIndices = new int[] { compNameIndex, isLoadedIndex };
/*  363 */     sorter.sort();
/*      */   }
/*      */ 
/*      */   public void loadFromFiles() throws DataException, ServiceException
/*      */   {
/*  368 */     FileUtils.reserveDirectory(this.m_compDir);
/*      */     try {
/*  370 */       if (this.m_io == null)
/*      */       {
/*  372 */         this.m_io = new IdcBasicIOImplementor();
/*      */       }
/*  374 */       ArrayList list = new ArrayList();
/*  375 */       list.add(new StringBuilder().append(this.m_compDir).append(this.m_listingFile).toString());
/*  376 */       list.add(new StringBuilder().append(this.m_compDir).append("idc_components.hda").toString());
/*      */ 
/*  380 */       if ((this.m_homeDir != null) && (!this.m_homeDir.equals(this.m_compDir)) && (SharedObjects.getEnvValueAsBoolean("UseHomeDirComponents", true)))
/*      */       {
/*  383 */         list.add(new StringBuilder().append(this.m_homeDir).append("data/components/").append(this.m_listingFile).toString());
/*      */       }
/*      */ 
/*  386 */       this.m_compBinder = null;
/*  387 */       this.m_componentSet = null;
/*  388 */       this.m_timeStamp = -2L;
/*  389 */       for (String listingPath : list)
/*      */       {
/*  391 */         for (int i = 0; i < 2; ++i)
/*      */         {
/*  393 */           Report.trace("componentloader", new StringBuilder().append("ComponentListEditor.loadFromFiles:checking listing file path=").append(listingPath).toString(), null);
/*      */ 
/*  396 */           InputStream in = null;
/*      */           try
/*      */           {
/*  399 */             if (!FileUtils.storeInDB(listingPath))
/*      */             {
/*  401 */               in = this.m_io.getReadStream(listingPath);
/*      */             }
/*      */             else
/*      */             {
/*  405 */               in = FileUtilsCfgBuilder.getCfgInputStream(listingPath);
/*      */             }
/*  407 */             String[] encoding = { this.m_encoding };
/*  408 */             this.m_compBinder = ComponentListUtils.readListingFile(listingPath, in, encoding);
/*  409 */             this.m_componentSet = ((DataResultSet)this.m_compBinder.getResultSet("Components"));
/*  410 */             this.m_encoding = encoding[0];
/*  411 */             File listingFile = FileUtilsCfgBuilder.getCfgFile(listingPath, "Component", false);
/*  412 */             this.m_timeStamp = listingFile.lastModified();
/*      */           }
/*      */           catch (IOException e)
/*      */           {
/*  416 */             Report.trace("componentloader", new StringBuilder().append("unable to load listing file ").append(e.getMessage()).toString(), null);
/*      */ 
/*  418 */             listingPath = new StringBuilder().append(listingPath).append(".old").toString();
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  423 */         if (this.m_compBinder != null) {
/*      */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  429 */       if (this.m_componentSet == null)
/*      */       {
/*  431 */         loadFromLegacyFiles();
/*      */       }
/*      */     }
/*      */     finally {
/*  435 */       FileUtils.releaseDirectory(this.m_compDir);
/*      */     }
/*  437 */     if (this.m_componentSet != null)
/*      */       return;
/*  439 */     if (this.m_timeStamp == -2L)
/*      */     {
/*  441 */       this.m_componentSet = ComponentListUtils.createDefaultComponentsResultSet();
/*  442 */       this.m_compBinder = new DataBinder();
/*  443 */       this.m_compBinder.addResultSet("Components", this.m_componentSet);
/*      */     }
/*      */     else
/*      */     {
/*  447 */       throw new ServiceException("!csInvalidComponentListingFile", null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void loadFromLegacyFiles()
/*      */     throws ServiceException, DataException
/*      */   {
/*  456 */     DataResultSet compSet = null;
/*      */ 
/*  459 */     InputStream in = null;
/*      */     try
/*      */     {
/*  462 */       if (!FileUtils.storeInDB(new StringBuilder().append(this.m_configDir).append("components.hda").toString()))
/*      */       {
/*  464 */         in = this.m_io.getReadStream(new StringBuilder().append(this.m_configDir).append("components.hda").toString());
/*      */       }
/*      */       else
/*      */       {
/*  468 */         in = FileUtilsCfgBuilder.getCfgInputStream(new StringBuilder().append(this.m_configDir).append("components.hda").toString());
/*      */       }
/*  470 */       Report.trace("componentloader", new StringBuilder().append("ComponentListEditor.loadFromLegacyFiles: loading ").append(this.m_configDir).append("components.hda").toString(), null);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  476 */       if (this.m_isCanCreate)
/*      */       {
/*  479 */         Report.trace("componentloader", "ComponentListEditor.loadFromLegacyFiles: create empty component resultset.", null);
/*      */ 
/*  482 */         compSet = ComponentListUtils.createDefaultComponentsResultSet();
/*      */       }
/*      */       else
/*      */       {
/*  486 */         throw new ServiceException(e, "csMissingLegacyComponentListing", new Object[0]);
/*      */       }
/*      */     }
/*  489 */     if (in != null)
/*      */     {
/*  493 */       String oldCmpPath = new StringBuilder().append(this.m_configDir).append("components.hda").toString();
/*  494 */       DataBinder cmpBinder = ComponentListUtils.readListingFile(oldCmpPath, in, null);
/*  495 */       in = null;
/*      */ 
/*  497 */       DataBinder editBinder = null;
/*  498 */       String oldEditCmpPath = new StringBuilder().append(this.m_compDir).append("edit_components.hda").toString();
/*      */       try
/*      */       {
/*  501 */         if (!FileUtils.storeInDB(oldEditCmpPath))
/*      */         {
/*  503 */           in = this.m_io.getReadStream(oldEditCmpPath);
/*      */         }
/*      */         else
/*      */         {
/*  507 */           in = FileUtilsCfgBuilder.getCfgInputStream(oldEditCmpPath);
/*      */         }
/*      */ 
/*  510 */         editBinder = ComponentListUtils.readListingFile(oldEditCmpPath, in, null);
/*  511 */         in = null;
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  515 */         Report.trace("componentloader", new StringBuilder().append("unable to read ").append(oldEditCmpPath).toString(), e);
/*      */       }
/*      */ 
/*  518 */       this.m_legacyEditor.setComponentData(cmpBinder, null);
/*  519 */       this.m_legacyEditor.setEditComponentData(editBinder, null);
/*  520 */       this.m_legacyEditor.loadLegacy();
/*  521 */       this.m_isLegacy = true;
/*      */ 
/*  523 */       compSet = this.m_legacyEditor.m_editComponents;
/*      */     }
/*      */ 
/*  527 */     this.m_compBinder = new DataBinder();
/*  528 */     this.m_compBinder.addResultSet("Components", compSet);
/*      */ 
/*  530 */     this.m_componentSet = compSet;
/*      */   }
/*      */ 
/*      */   public DataBinder loadComponentHdaFile(Map<String, String> map, Map componentMap, DataResultSet cmpReport)
/*      */     throws DataException, ServiceException
/*      */   {
/*  538 */     DataBinder latestBinder = null;
/*  539 */     String name = (String)map.get("name");
/*  540 */     String location = (String)map.get("location");
/*  541 */     String status = (String)map.get("status");
/*  542 */     boolean isEnabled = status.equalsIgnoreCase("enabled");
/*  543 */     boolean isHomeLocal = ComponentLocationUtils.isHomeLocal(this.m_environment);
/*      */ 
/*  545 */     Map reportMap = new HashMap();
/*  546 */     Map hMap = new HashMap();
/*  547 */     Map locMap = new HashMap();
/*      */ 
/*  549 */     String hPath = null;
/*  550 */     boolean hExists = false;
/*  551 */     if (!isHomeLocal)
/*      */     {
/*  555 */       hPath = ComponentLocationUtils.determineComponentLocationWithEnv(map, 2, this.m_environment, false);
/*      */ 
/*  557 */       hExists = FileUtils.checkFile(hPath, true, false) == 0;
/*  558 */       if (!hExists)
/*      */       {
/*  562 */         checkAlternateLocation(2, map, hPath);
/*      */       }
/*      */ 
/*  566 */       hMap.put("type", "home");
/*  567 */       hMap.put("exists", new StringBuilder().append("").append(hExists).toString());
/*  568 */       hMap.put("location", hPath);
/*  569 */       hMap.put("isLoaded", "0");
/*  570 */       reportMap.put("home", hMap);
/*      */     }
/*      */ 
/*  573 */     String locPath = null;
/*  574 */     boolean locExists = false;
/*  575 */     if (location.length() > 0)
/*      */     {
/*  579 */       locPath = ComponentLocationUtils.determineComponentLocationWithEnv(map, 1, this.m_environment, false);
/*      */ 
/*  581 */       locExists = FileUtils.checkFile(locPath, true, false) == 0;
/*  582 */       if (!locExists)
/*      */       {
/*  586 */         checkAlternateLocation(1, map, locPath);
/*      */       }
/*      */ 
/*  590 */       locMap.put("type", "local");
/*  591 */       locMap.put("exists", new StringBuilder().append("").append(locExists).toString());
/*  592 */       locMap.put("location", locPath);
/*  593 */       locMap.put("isLoaded", "0");
/*  594 */       reportMap.put("local", locMap);
/*      */     }
/*      */ 
/*  597 */     if ((!hExists) && (!locExists))
/*      */     {
/*  599 */       IdcMessage msg = IdcMessageFactory.lc("csComponentFileNotFound2", new Object[] { name, locPath });
/*  600 */       ComponentLoader.logComponentLoadError("ComponentListEditor", name, msg, null);
/*      */     }
/*      */     else
/*      */     {
/*  605 */       DataBinder hBinder = null;
/*  606 */       DataBinder binder = null;
/*      */       try
/*      */       {
/*  609 */         String hVersion = null;
/*  610 */         String version = null;
/*  611 */         List typeList = new ArrayList();
/*  612 */         if (hExists)
/*      */         {
/*  614 */           hBinder = ResourceUtils.readDataBinderFromPath(hPath);
/*  615 */           hVersion = hBinder.getLocal("version");
/*  616 */           typeList.add("home");
/*      */ 
/*  618 */           if (hVersion != null)
/*      */           {
/*  620 */             hMap.put("version", hVersion);
/*      */           }
/*      */         }
/*  623 */         if (locExists)
/*      */         {
/*  625 */           binder = ResourceUtils.readDataBinderFromPath(locPath);
/*  626 */           version = binder.getLocal("version");
/*  627 */           typeList.add("local");
/*      */ 
/*  629 */           if (version != null)
/*      */           {
/*  631 */             locMap.put("version", version);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  636 */         boolean isInstaller = StringUtils.convertToBool((String)this.m_environment.get("IsInstallerEnv"), false);
/*      */ 
/*  638 */         boolean isHomeVersionOlder = (locExists) && (!isInstaller);
/*  639 */         if ((hVersion != null) && (version != null))
/*      */         {
/*  641 */           isHomeVersionOlder = isOlderVersion(hVersion, version);
/*      */         }
/*      */ 
/*  647 */         latestBinder = binder;
/*  648 */         String absPath = locPath;
/*  649 */         if (isHomeVersionOlder)
/*      */         {
/*  651 */           latestBinder.putLocal("useType", "local");
/*      */ 
/*  653 */           hMap.put("reason", "old");
/*  654 */           locMap.put("reason", "new");
/*  655 */           locMap.put("isLoaded", "1");
/*      */         }
/*      */         else
/*      */         {
/*  659 */           if (hPath != null)
/*      */           {
/*  661 */             absPath = hPath;
/*      */           }
/*  663 */           boolean useCustom = SharedObjects.getEnvValueAsBoolean(new StringBuilder().append(name).append(":localPreferred").toString(), false);
/*      */ 
/*  665 */           if ((useCustom) && (binder != null))
/*      */           {
/*  667 */             latestBinder.putLocal("useType", "forceLocal");
/*  668 */             locMap.put("reason", "forceLocal");
/*  669 */             locMap.put("isLoaded", "1");
/*      */           }
/*      */           else
/*      */           {
/*  673 */             if (hBinder != null)
/*      */             {
/*  675 */               latestBinder = hBinder;
/*      */             }
/*  677 */             latestBinder.putLocal("useType", "home");
/*      */ 
/*  679 */             String tags = latestBinder.getLocal("componentTags");
/*  680 */             if ((tags != null) && (tags.length() > 0))
/*      */             {
/*  682 */               tags = new StringBuilder().append(tags).append(",").toString();
/*      */             }
/*      */             else
/*      */             {
/*  686 */               tags = "";
/*      */             }
/*  688 */             tags = new StringBuilder().append(tags).append("home").toString();
/*  689 */             latestBinder.putLocal("componentTags", tags);
/*      */ 
/*  691 */             hMap.put("isLoaded", "1");
/*  692 */             hMap.put("reason", "new");
/*  693 */             locMap.put("reason", "old");
/*      */           }
/*      */         }
/*      */ 
/*  697 */         if (!isEnabled)
/*      */         {
/*  699 */           boolean enabled = StringUtils.convertToBool((String)locMap.get("isLoaded"), false);
/*  700 */           if (enabled)
/*      */           {
/*  702 */             locMap.put("isLoaded", "0");
/*  703 */             locMap.put("reason", "disabled");
/*      */           }
/*      */           else
/*      */           {
/*  707 */             hMap.put("isLoaded", "0");
/*  708 */             hMap.put("reason", "disabled");
/*      */           }
/*  710 */           latestBinder.putLocal("useType", "");
/*      */         }
/*      */ 
/*  713 */         String types = StringUtils.createString(typeList, ',', '^');
/*  714 */         latestBinder.putLocal("componentType", types);
/*      */ 
/*  716 */         String cmptDir = FileUtils.getDirectory(absPath);
/*  717 */         latestBinder.putLocal("ComponentDir", cmptDir);
/*  718 */         latestBinder.putLocal("AbsolutePath", absPath);
/*  719 */         componentMap.put(name, latestBinder);
/*      */ 
/*  722 */         updateComponentInfo(name, latestBinder, isEnabled);
/*      */ 
/*  724 */         if (cmpReport != null)
/*      */         {
/*  727 */           addComponentReport(name, cmpReport, reportMap);
/*      */         }
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/*  732 */         ComponentLoader.logComponentLoadError("ComponentListEditor", name, IdcMessageFactory.lc("csComponentLoadError", new Object[0]), t);
/*      */       }
/*      */     }
/*      */ 
/*  736 */     if (latestBinder == null)
/*      */     {
/*  739 */       DataBinder binder = new DataBinder();
/*  740 */       String msg = "!csComponentMissingBinder";
/*  741 */       binder.putLocal("StatusCode", "-1");
/*  742 */       binder.putLocal("StatusMessageKey", msg);
/*  743 */       binder.putLocal("StatusMessage", msg);
/*  744 */       componentMap.put(name, binder);
/*      */     }
/*  746 */     return latestBinder;
/*      */   }
/*      */ 
/*      */   protected void checkAlternateLocation(int type, Map<String, String> map, String expectedPath)
/*      */   {
/*  751 */     String path = null;
/*  752 */     String location = (String)map.get("location");
/*  753 */     String name = (String)map.get("name");
/*      */ 
/*  755 */     boolean isSystem = ComponentLocationUtils.isSystemComponent(map);
/*  756 */     String componentRelPath = new StringBuilder().append(name).append("/").append(FileUtils.getName(location)).toString();
/*  757 */     if (isSystem)
/*      */     {
/*  760 */       path = new StringBuilder().append(ComponentLocationUtils.computeDefaultCustomComponentDirWithEnv(type, false, new boolean[1], this.m_environment)).append(componentRelPath).toString();
/*      */     }
/*      */     else
/*      */     {
/*  765 */       path = new StringBuilder().append(ComponentLocationUtils.computeDefaultSystemComponentDirWithEnv(type, false, new boolean[1], this.m_environment)).append(componentRelPath).toString();
/*      */     }
/*      */ 
/*  772 */     boolean exists = FileUtils.checkFile(path, true, false) == 0;
/*  773 */     if (!exists)
/*      */       return;
/*  775 */     String typeStr = "home";
/*  776 */     if (type == 1)
/*      */     {
/*  778 */       typeStr = "local";
/*      */     }
/*      */ 
/*  782 */     String dirType = "system";
/*  783 */     if (isSystem)
/*      */     {
/*  785 */       dirType = "custom";
/*      */     }
/*  787 */     IdcMessage errMsg = IdcMessageFactory.lc("csComponentLoadrAlternateLocation", new Object[] { name, typeStr, expectedPath, path, dirType });
/*      */ 
/*  789 */     ComponentLoader.logComponentLoadError("ComponentListEditor.checkAlternateLocation", name, errMsg, null);
/*      */   }
/*      */ 
/*      */   public boolean isOlderVersion(String s1, String s2)
/*      */   {
/*  796 */     String versionChars = "0123456789-_.";
/*  797 */     String v1 = parseFromStart(s1, versionChars);
/*  798 */     String v2 = parseFromStart(s2, versionChars);
/*      */ 
/*  800 */     int rc = SystemUtils.compareVersions(v1, v2);
/*  801 */     return rc < 0;
/*      */   }
/*      */ 
/*      */   public String parseFromStart(String str, String characters)
/*      */   {
/*  806 */     if (str == null)
/*      */     {
/*  808 */       return "";
/*      */     }
/*  810 */     int index = 0;
/*  811 */     int endIndex = str.length();
/*  812 */     while ((index < endIndex) && (characters.indexOf(str.charAt(index)) >= 0))
/*      */     {
/*  814 */       ++index;
/*      */     }
/*  816 */     return str.substring(0, index);
/*      */   }
/*      */ 
/*      */   protected void updateComponentInfo(String name, DataBinder binder, boolean isEnabled)
/*      */     throws DataException
/*      */   {
/*  824 */     int num = this.m_componentSet.getNumFields();
/*  825 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  827 */       String field = this.m_componentSet.getFieldName(i);
/*  828 */       if ((field.equals("name")) || (field.equals("location"))) continue; if (field.equals("status")) {
/*      */         continue;
/*      */       }
/*      */ 
/*  832 */       setCurrentColumnValue(field, binder, this.m_componentSet, i);
/*      */     }
/*      */ 
/*  835 */     this.m_stateCfg.updateComponentVarsRow(name, binder);
/*      */   }
/*      */ 
/*      */   protected void addComponentReport(String name, DataResultSet reportSet, Map<String, Map<String, String>> reportMap)
/*      */   {
/*  841 */     for (String key : reportMap.keySet())
/*      */     {
/*  843 */       Map map = (Map)reportMap.get(key);
/*      */ 
/*  846 */       map.put("name", name);
/*      */       try
/*      */       {
/*  851 */         Parameters params = new MapParameters(map);
/*  852 */         List row = reportSet.createRowAsList(params);
/*  853 */         reportSet.addRowWithList(row);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  857 */         Report.trace("componentloader", new StringBuilder().append("Unable to add component report for ").append(name).toString(), e);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void undoChanges(String errMsg, Throwable t)
/*      */     throws ServiceException
/*      */   {
/*  867 */     if (!this.m_isAdminServer)
/*      */     {
/*      */       try
/*      */       {
/*  871 */         loadComponents();
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  875 */         String msg = LocaleUtils.encodeMessage("csUnableToUndoTheChanges", e.getMessage());
/*  876 */         msg = LocaleUtils.appendMessage(errMsg, msg);
/*  877 */         throw new ServiceException(msg, t);
/*      */       }
/*      */     }
/*  880 */     throw new ServiceException(errMsg, t);
/*      */   }
/*      */ 
/*      */   public boolean enableOrDisableComponent(String compNames, boolean isEnable)
/*      */     throws ServiceException, DataException
/*      */   {
/*  886 */     return enableOrDisableComponentEx(compNames, isEnable, true);
/*      */   }
/*      */ 
/*      */   public boolean enableOrDisableComponentEx(String compNames, boolean isEnable, boolean isWrite)
/*      */     throws ServiceException, DataException
/*      */   {
/*  900 */     boolean isChanged = false;
/*  901 */     List toDisable = new ArrayList();
/*  902 */     boolean isCurrentlyEnabled = true;
/*  903 */     OutputStream os = null;
/*      */     try
/*      */     {
/*  906 */       List list = StringUtils.makeListFromSequenceSimple(compNames);
/*  907 */       if (list.size() == 0)
/*      */       {
/*  910 */         int i = 0;
/*      */         return i;
/*      */       }
/*  914 */       checkAndUpdateListingFile();
/*      */ 
/*  917 */       String newStatus = (isEnable) ? "Enabled" : "Disabled";
/*  918 */       FieldInfo[] fi = ResultSetUtils.createInfoList(this.m_componentSet, new String[] { "name", "status" }, true);
/*      */ 
/*  920 */       int nameIndex = fi[0].m_index;
/*  921 */       int statusIndex = fi[1].m_index;
/*      */ 
/*  923 */       for (int i = 0; i < list.size(); ++i)
/*      */       {
/*  925 */         String name = (String)list.get(i);
/*  926 */         if (name.length() == 0) {
/*      */           continue;
/*      */         }
/*      */ 
/*  930 */         Vector row = this.m_componentSet.findRow(nameIndex, name);
/*  931 */         if (row == null)
/*      */         {
/*  934 */           throw new ServiceException(null, "csComponentMissingFromListing", new Object[] { name });
/*      */         }
/*      */ 
/*  937 */         Map map = this.m_componentSet.getCurrentRowMap();
/*  938 */         String status = (String)map.get("status");
/*  939 */         isCurrentlyEnabled = status.equalsIgnoreCase("enabled");
/*  940 */         if (isCurrentlyEnabled == isEnable)
/*      */           continue;
/*  942 */         isChanged = true;
/*  943 */         this.m_componentSet.setCurrentValue(statusIndex, newStatus);
/*      */ 
/*  945 */         if (isCurrentlyEnabled) {
/*      */           continue;
/*      */         }
/*      */ 
/*  949 */         String toDisableStr = (String)map.get("componentsToDisable");
/*  950 */         if ((toDisableStr == null) || (toDisableStr.length() <= 0))
/*      */           continue;
/*  952 */         toDisable.add(toDisableStr);
/*      */       }
/*      */ 
/*  959 */       for (int j = 0; j < toDisable.size(); ++j)
/*      */       {
/*  961 */         List disableCompList = StringUtils.makeListFromEscapedString((String)toDisable.get(j));
/*      */ 
/*  963 */         for (int k = 0; k < disableCompList.size(); ++k)
/*      */         {
/*  966 */           String name = (String)disableCompList.get(k);
/*  967 */           if (this.m_componentSet.findRow(nameIndex, name) == null)
/*      */             continue;
/*  969 */           boolean isSubChanged = enableOrDisableComponentEx(name, false, false);
/*      */ 
/*  971 */           if (!isSubChanged)
/*      */             continue;
/*  973 */           isChanged = true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  978 */       if ((isWrite) && (isChanged))
/*      */       {
/*  981 */         if ((this.m_isAdminServer) && (this.m_isLegacy))
/*      */         {
/*  983 */           updateAndSaveLegacyFiles();
/*      */         }
/*      */         else
/*      */         {
/*  987 */           if (this.m_isAdminServer)
/*      */           {
/*  989 */             this.m_compBinder.putLocal("IsAdminServerUpdate", "1");
/*      */           }
/*  991 */           String path = new StringBuilder().append(this.m_compDir).append(this.m_listingFile).toString();
/*  992 */           if (FileUtils.storeInDB(path))
/*      */           {
/*  994 */             os = FileUtilsCfgBuilder.getCfgOutputStream(path, "Component");
/*      */           }
/*      */           else
/*      */           {
/*  998 */             os = this.m_io.getWriteStream(path);
/*      */           }
/* 1000 */           saveListingFile((this.m_isLegacy) ? 0 : 4, os, this.m_encoding);
/*      */         }
/* 1002 */         Map args = new HashMap();
/* 1003 */         args.put("isComponentsInDev", Boolean.toString(this.m_componentsInDev));
/* 1004 */         args.put("isAdminServer", Boolean.toString(this.m_isAdminServer));
/* 1005 */         this.m_stateCfg.updateStateConfigWithArgs(this.m_compBinder, this.m_environment, args);
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1010 */       String errMsg = LocaleUtils.encodeMessage("csUnableToEnableComponent", null, compNames);
/* 1011 */       if (!isEnable)
/*      */       {
/* 1013 */         errMsg = LocaleUtils.encodeMessage("csUnableToDisableComponent", null, compNames);
/*      */       }
/* 1015 */       undoChanges(errMsg, t);
/*      */     }
/*      */     finally
/*      */     {
/* 1019 */       FileUtils.closeFiles(os, null);
/*      */     }
/* 1021 */     return isChanged;
/*      */   }
/*      */ 
/*      */   public void updateAndSaveLegacyFiles()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1027 */     this.m_legacyEditor.updateComponents();
/*      */ 
/* 1030 */     this.m_legacyEditor.saveListingFiles(this.m_configDir, this.m_compDir);
/*      */   }
/*      */ 
/*      */   public void checkAndUpdateListingFile()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1038 */     if (this.m_isAdminServer)
/*      */       return;
/* 1040 */     File file = FileUtilsCfgBuilder.getCfgFile(new StringBuilder().append(this.m_compDir).append(this.m_listingFile).toString(), "Component", false);
/* 1041 */     long ts = -2L;
/* 1042 */     if (file.exists())
/*      */     {
/* 1044 */       ts = file.lastModified();
/*      */     }
/* 1046 */     if (ts == this.m_timeStamp) {
/*      */       return;
/*      */     }
/* 1049 */     loadComponents();
/*      */   }
/*      */ 
/*      */   public void configureFeatures(ComponentFeatures f, int flags)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1056 */     checkAndUpdateListingFile();
/*      */ 
/* 1059 */     DataResultSet rset = getComponentSet();
/*      */ 
/* 1061 */     String[] fields = { "name", "status" };
/* 1062 */     FieldInfo[] info = null;
/*      */     try
/*      */     {
/* 1065 */       info = ResultSetUtils.createInfoList(rset, fields, true);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1069 */       throw new ServiceException("!csComponentTableFormatError", e);
/*      */     }
/*      */     String name;
/* 1072 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/* 1074 */       name = rset.getStringValue(info[0].m_index);
/* 1075 */       String status = rset.getStringValue(info[1].m_index);
/* 1076 */       boolean isEnabled = status.equalsIgnoreCase("Enabled");
/* 1077 */       if (((flags & 0x4) != 0) && (!isEnabled)) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1081 */       DataBinder binder = getComponentData(name);
/* 1082 */       if (binder == null)
/*      */       {
/* 1085 */         Report.warning("componentloader", null, new ServiceException(null, "csComponentDataNotFound", new Object[] { name }));
/*      */       }
/*      */       else
/*      */       {
/* 1089 */         String features = binder.getLocal("featureExtensions");
/*      */ 
/* 1091 */         List featureList = StringUtils.makeListFromSequenceSimple(features);
/* 1092 */         for (String feature : featureList)
/*      */         {
/* 1094 */           f.m_features.registerFeature(feature, name);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void deleteComponent(Properties props) throws ServiceException {
/* 1102 */     deleteComponent(props);
/*      */   }
/*      */ 
/*      */   public void deleteComponent(Map<String, String> map)
/*      */     throws ServiceException
/*      */   {
/* 1110 */     String name = (String)map.get("name");
/* 1111 */     OutputStream os = null;
/*      */     try
/*      */     {
/* 1114 */       Vector v = this.m_componentSet.findRow(0, name);
/* 1115 */       if (v != null)
/*      */       {
/* 1118 */         int index = this.m_componentSet.getCurrentRow();
/* 1119 */         this.m_componentSet.deleteRow(index);
/* 1120 */         this.m_stateCfg.removeComponentVars(name);
/*      */ 
/* 1122 */         String path = new StringBuilder().append(this.m_compDir).append(this.m_listingFile).toString();
/* 1123 */         if (FileUtils.storeInDB(path))
/*      */         {
/* 1125 */           os = FileUtilsCfgBuilder.getCfgOutputStream(path, "Component");
/*      */         }
/*      */         else
/*      */         {
/* 1129 */           os = this.m_io.getWriteStream(path);
/*      */         }
/*      */ 
/* 1132 */         saveListingFile(4, os, this.m_encoding);
/* 1133 */         this.m_stateCfg.updateStateConfig(this.m_componentSet, this.m_componentsInDev, this.m_environment);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1138 */       String msg = LocaleUtils.encodeMessage("csUnableToRemoveComponent", null, name);
/* 1139 */       undoChanges(msg, e);
/*      */     }
/*      */     finally
/*      */     {
/* 1143 */       FileUtils.closeFiles(os, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void deleteComponents(Collection<String> componentNames) throws ServiceException
/*      */   {
/* 1149 */     boolean isChanged = false;
/* 1150 */     for (String componentName : componentNames)
/*      */     {
/*      */       try
/*      */       {
/* 1154 */         Vector row = this.m_componentSet.findRow(0, componentName);
/* 1155 */         if (row != null)
/*      */         {
/* 1157 */           int index = this.m_componentSet.getCurrentRow();
/* 1158 */           this.m_componentSet.deleteRow(index);
/* 1159 */           this.m_stateCfg.removeComponentVars(componentName);
/* 1160 */           isChanged = true;
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1165 */         String msg = LocaleUtils.encodeMessage("csUnableToRemoveComponent", null, componentName);
/* 1166 */         undoChanges(msg, e);
/*      */       }
/*      */     }
/* 1169 */     if (!isChanged)
/*      */       return;
/* 1171 */     OutputStream os = null;
/*      */     try
/*      */     {
/* 1174 */       String path = new StringBuilder().append(this.m_compDir).append(this.m_listingFile).toString();
/* 1175 */       if (FileUtils.storeInDB(path))
/*      */       {
/* 1177 */         os = FileUtilsCfgBuilder.getCfgOutputStream(path, "Component");
/*      */       }
/*      */       else
/*      */       {
/* 1181 */         os = this.m_io.getWriteStream(path);
/*      */       }
/* 1183 */       saveListingFile(4, os, this.m_encoding);
/* 1184 */       this.m_stateCfg.updateStateConfig(this.m_componentSet, this.m_componentsInDev, this.m_environment);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1188 */       StringBuilder sb = new StringBuilder();
/* 1189 */       for (String componentName : componentNames)
/*      */       {
/* 1191 */         if (sb.length() > 0)
/*      */         {
/* 1193 */           sb.append(',');
/*      */         }
/* 1195 */         sb.append(componentName);
/*      */       }
/* 1197 */       String msg = LocaleUtils.encodeMessage("csUnableToRemoveComponent", null, sb.toString());
/* 1198 */       undoChanges(msg, e);
/*      */     }
/*      */     finally
/*      */     {
/* 1202 */       FileUtils.closeObject(os);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void addComponent(Properties props)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1211 */     addComponent(props, null);
/*      */   }
/*      */ 
/*      */   public void addComponent(Map<String, String> map, DataBinder orgData)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1218 */     checkAndUpdateListingFile();
/*      */ 
/* 1220 */     FieldInfo[] fis = ResultSetUtils.createInfoList(this.m_componentSet, new String[] { "name" }, true);
/*      */ 
/* 1222 */     String name = (String)map.get("name");
/*      */ 
/* 1224 */     Vector row = this.m_componentSet.findRow(fis[0].m_index, name);
/* 1225 */     if (row == null)
/*      */     {
/* 1227 */       row = this.m_componentSet.createEmptyRow();
/* 1228 */       this.m_componentSet.addRow(row);
/* 1229 */       int lastRow = this.m_componentSet.getNumRows() - 1;
/* 1230 */       this.m_componentSet.setCurrentRow(lastRow);
/*      */     }
/*      */ 
/* 1233 */     int numFields = this.m_componentSet.getNumFields();
/* 1234 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/* 1236 */       String clmn = this.m_componentSet.getFieldName(i);
/* 1237 */       String val = (String)map.get(clmn);
/* 1238 */       if ((val == null) && (orgData != null))
/*      */       {
/* 1240 */         val = orgData.getLocal(clmn);
/*      */       }
/* 1242 */       if (val == null)
/*      */         continue;
/* 1244 */       row.setElementAt(val, i);
/*      */     }
/*      */ 
/* 1248 */     OutputStream os = null;
/*      */     try
/*      */     {
/* 1251 */       String path = new StringBuilder().append(this.m_compDir).append(this.m_listingFile).toString();
/* 1252 */       if (FileUtils.storeInDB(path))
/*      */       {
/* 1254 */         os = FileUtilsCfgBuilder.getCfgOutputStream(path, "Component");
/*      */       }
/*      */       else
/*      */       {
/* 1258 */         os = this.m_io.getWriteStream(path);
/*      */       }
/* 1260 */       saveListingFile(4, os, this.m_encoding);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/* 1268 */       FileUtils.closeFiles(os, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean isComponentEnabled(String name) throws DataException
/*      */   {
/* 1274 */     int nameIndex = ResultSetUtils.getIndexMustExist(this.m_componentSet, "name");
/* 1275 */     int statusIndex = ResultSetUtils.getIndexMustExist(this.m_componentSet, "status");
/* 1276 */     Vector row = this.m_componentSet.findRow(nameIndex, name);
/* 1277 */     if (row != null)
/*      */     {
/* 1279 */       String status = this.m_componentSet.getStringValue(statusIndex);
/* 1280 */       return status.equalsIgnoreCase("enabled");
/*      */     }
/* 1282 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean isComponentNameUnique(String name)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1292 */       int index = ResultSetUtils.getIndexMustExist(this.m_componentSet, "name");
/* 1293 */       Vector row = this.m_componentSet.findRow(index, name);
/*      */ 
/* 1296 */       return row == null;
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1303 */       throw new ServiceException(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateLegacyInfo()
/*      */   {
/* 1313 */     Table table = ResourceContainerUtils.getDynamicTableResource("LegacyTaggedComponents");
/* 1314 */     if (table == null)
/*      */       return;
/* 1316 */     DataResultSet drset = new DataResultSet();
/* 1317 */     drset.init(table);
/* 1318 */     this.m_compBinder.addResultSet("LegacyTaggedComponents", drset);
/*      */   }
/*      */ 
/*      */   public void saveListingFile(int flags, OutputStream output, String encoding)
/*      */     throws ServiceException
/*      */   {
/* 1325 */     if (!this.m_isAdminServer)
/*      */     {
/* 1330 */       ResultSet stateSet = this.m_stateCfg.getExportedVariables();
/* 1331 */       this.m_compBinder.addResultSet("StateCfgExportedVars", stateSet);
/*      */     }
/*      */ 
/* 1334 */     String listingPath = new StringBuilder().append(this.m_compDir).append(this.m_listingFile).toString();
/* 1335 */     ComponentListUtils.saveListingFile(listingPath, this.m_compBinder, flags, output, encoding);
/*      */ 
/* 1337 */     File editFile = FileUtilsCfgBuilder.getCfgFile(listingPath, "Component", false);
/* 1338 */     if (editFile.exists())
/*      */     {
/* 1340 */       this.m_timeStamp = editFile.lastModified();
/*      */     }
/*      */     else
/*      */     {
/* 1344 */       this.m_timeStamp = -2L;
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getIntradocDir()
/*      */   {
/* 1353 */     return this.m_intradocDir;
/*      */   }
/*      */ 
/*      */   public void setLegacyOutputStreams(OutputStream activeOs, OutputStream editOs)
/*      */   {
/* 1361 */     this.m_legacyEditor.setOutputStreams(activeOs, editOs);
/*      */   }
/*      */ 
/*      */   public void setLegacyInputStreams(InputStream activeIs, InputStream editIs)
/*      */   {
/* 1369 */     this.m_legacyEditor.setInputStreams(activeIs, editIs);
/*      */   }
/*      */ 
/*      */   public void setStateOutStream(OutputStream os)
/*      */   {
/* 1374 */     this.m_stateCfg.setOutput(os);
/*      */   }
/*      */ 
/*      */   public void closeAllStreams()
/*      */   {
/* 1381 */     FileUtils.closeObject(this.m_stateCfg.getOutput());
/* 1382 */     this.m_stateCfg.setOutput(null);
/*      */ 
/* 1385 */     this.m_legacyEditor.closeAllStreams();
/*      */   }
/*      */ 
/*      */   public void closeStreams(InputStream inStream, OutputStream outStream)
/*      */   {
/*      */     try
/*      */     {
/* 1392 */       if (inStream != null)
/*      */       {
/* 1394 */         inStream.close();
/*      */       }
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 1399 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1401 */         Report.debug(null, null, ignore);
/*      */       }
/*      */     }
/*      */     try
/*      */     {
/* 1406 */       if (outStream != null)
/*      */       {
/* 1408 */         outStream.close();
/*      */       }
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 1413 */       if (!SystemUtils.m_verbose)
/*      */         return;
/* 1415 */       Report.debug(null, null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public DataResultSet getComponentList()
/*      */   {
/* 1428 */     return getComponentSet();
/*      */   }
/*      */ 
/*      */   public DataResultSet getComponentSet()
/*      */   {
/* 1436 */     if (this.m_componentSet == null)
/*      */     {
/* 1438 */       return null;
/*      */     }
/* 1440 */     DataResultSet drset = new DataResultSet();
/* 1441 */     drset.copy(this.m_componentSet);
/* 1442 */     return drset;
/*      */   }
/*      */ 
/*      */   public DataResultSet getLegacyTaggedComponentSet()
/*      */   {
/* 1447 */     DataResultSet drset = (DataResultSet)this.m_compBinder.getResultSet("LegacyTaggedComponents");
/* 1448 */     if (drset == null)
/*      */     {
/* 1452 */       return null;
/*      */     }
/* 1454 */     return drset.shallowClone();
/*      */   }
/*      */ 
/*      */   public DataBinder getComponentData(String name)
/*      */   {
/* 1459 */     DataBinder binder = null;
/* 1460 */     DataBinder cmpBinder = (DataBinder)this.m_componentMap.get(name);
/* 1461 */     if (cmpBinder != null)
/*      */     {
/* 1463 */       binder = new DataBinder();
/* 1464 */       binder.merge(cmpBinder);
/*      */     }
/* 1466 */     return binder;
/*      */   }
/*      */ 
/*      */   public DataResultSet getEnabledComponentList()
/*      */   {
/* 1471 */     return getEnabledDisabledComponents(true);
/*      */   }
/*      */ 
/*      */   public DataResultSet getDisabledComponentList()
/*      */   {
/* 1476 */     return getEnabledDisabledComponents(false);
/*      */   }
/*      */ 
/*      */   protected DataResultSet getEnabledDisabledComponents(boolean isEnabled)
/*      */   {
/* 1481 */     ResultSetFilter rsFilter = new ResultSetFilter(isEnabled)
/*      */     {
/*      */       public int checkRow(String val, int curNumRow, Vector row)
/*      */       {
/* 1485 */         boolean enabled = val.equalsIgnoreCase("enabled");
/* 1486 */         if (this.val$isEnabled)
/*      */         {
/* 1488 */           return (enabled) ? 1 : 0;
/*      */         }
/* 1490 */         return (enabled) ? 0 : 1;
/*      */       }
/*      */     };
/* 1493 */     DataResultSet drset = new DataResultSet();
/* 1494 */     drset.copyFiltered(this.m_componentSet, "status", rsFilter);
/* 1495 */     return drset;
/*      */   }
/*      */ 
/*      */   public String getComponentsListingFilePath()
/*      */   {
/* 1503 */     return new StringBuilder().append(this.m_compDir).append(this.m_listingFile).toString();
/*      */   }
/*      */ 
/*      */   public long getTimeStamp()
/*      */   {
/* 1511 */     return this.m_timeStamp;
/*      */   }
/*      */ 
/*      */   public long getLastLoaded()
/*      */   {
/* 1519 */     return this.m_lastLoadedTS;
/*      */   }
/*      */ 
/*      */   public DataResultSet getComponentReport()
/*      */   {
/* 1524 */     if (this.m_componentReportSet != null)
/*      */     {
/* 1526 */       return this.m_componentReportSet.shallowClone();
/*      */     }
/* 1528 */     return null;
/*      */   }
/*      */ 
/*      */   protected void setCurrentColumnValue(String name, DataBinder binder, DataResultSet drset, int index)
/*      */     throws DataException
/*      */   {
/* 1535 */     String val = binder.getLocal(name);
/* 1536 */     if (val == null)
/*      */     {
/* 1538 */       val = "";
/*      */     }
/*      */ 
/* 1541 */     drset.setCurrentValue(index, val);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1546 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99147 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ComponentListEditor
 * JD-Core Version:    0.5.4
 */