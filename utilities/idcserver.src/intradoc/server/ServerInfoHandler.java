/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.ConfigFileParameters;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.FeaturesInterface;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcComparator;
/*      */ import intradoc.common.IdcLocalizationStrings;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NativeOsUtils;
/*      */ import intradoc.common.PathUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainerUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.Sort;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.common.TableUtils;
/*      */ import intradoc.common.VersionInfo;
/*      */ import intradoc.conversion.CryptoPasswordUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.MapParameters;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.SimpleParameters;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.indexer.IndexerConfig;
/*      */ import intradoc.io.zip.IdcZipEnvironment;
/*      */ import intradoc.loader.IdcClassInfo;
/*      */ import intradoc.loader.IdcClassLoader;
/*      */ import intradoc.loader.IdcLoaderElement;
/*      */ import intradoc.loader.IdcLoaderElementList;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.utils.ComponentListEditor;
/*      */ import intradoc.server.utils.ComponentListManager;
/*      */ import intradoc.server.utils.ComponentLocationUtils;
/*      */ import intradoc.server.utils.ServerInstallUtils;
/*      */ import intradoc.shared.ActiveIndexState;
/*      */ import intradoc.shared.BasicFeatureImplementor;
/*      */ import intradoc.shared.CommonSearchConfig;
/*      */ import intradoc.shared.DocumentPathBuilder;
/*      */ import intradoc.shared.Features;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcPerfectHash;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.IOException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ServerInfoHandler extends ServiceHandler
/*      */ {
/*   97 */   public static String m_defaultSecuredJavaProperties = "weblogic.security.CustomTrustKeyStoreFileName, weblogic.security.CustomTrustKeyStorePassPhrase";
/*      */   public static List m_securedJavaPropertiesList;
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadServerInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/*  105 */     String username = this.m_binder.getLocal("dUser");
/*  106 */     this.m_binder.putLocal("dName", username);
/*  107 */     boolean isLibraryServer = ServerInstallUtils.isLibraryServer();
/*  108 */     this.m_binder.putLocal("isLibraryServer", Boolean.toString(isLibraryServer));
/*  109 */     this.m_binder.putLocal("MinimumClientVersion", VersionInfo.getProductMinimumClientVersion());
/*      */ 
/*  111 */     DataResultSet templates = SharedObjects.getTable("ViewURLTemplates");
/*  112 */     if (templates != null)
/*      */     {
/*  114 */       DataResultSet drest = new DataResultSet(new String[] { "Name", "Value" });
/*  115 */       for (templates.first(); templates.isRowPresent(); templates.next())
/*      */       {
/*  117 */         String name = templates.getStringValueByName("TemplateName");
/*  118 */         String value = SharedObjects.getEnvironmentValue(name);
/*      */ 
/*  120 */         Vector row = new IdcVector();
/*  121 */         row.addElement(name);
/*  122 */         row.addElement(value);
/*  123 */         drest.addRow(row);
/*      */       }
/*  125 */       this.m_binder.addResultSet("TemplateURLs", drest);
/*      */     }
/*      */ 
/*  129 */     ResultSet searchSortFields = this.m_binder.getResultSet("SearchSortFields");
/*  130 */     if (searchSortFields == null)
/*      */     {
/*  132 */       String searchEngineName = SharedObjects.getEnvironmentValue("SearchIndexerEngineName");
/*  133 */       if (searchEngineName != null)
/*      */       {
/*  135 */         Map searchClients = (Map)SharedObjects.getObject("globalObjects", "CommonSearchClientObjects");
/*  136 */         Map searchClient = (Map)searchClients.get(searchEngineName);
/*  137 */         searchSortFields = (DataResultSet)searchClient.get("SearchSortFields");
/*  138 */         if (searchSortFields != null)
/*      */         {
/*  140 */           this.m_binder.addResultSet("SearchSortFields", searchSortFields);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  145 */     PluginFilters.filter("getViewUrlTemplates", this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadConfigInfo()
/*      */   {
/*  151 */     this.m_service.loadSecureEnvironment();
/*      */ 
/*  153 */     this.m_binder.putLocal("JavaVersion", EnvUtils.getJavaVersion());
/*  154 */     this.m_binder.putLocal("OsName", EnvUtils.getOSName());
/*  155 */     this.m_binder.putLocal("OsFamily", EnvUtils.getOSFamily());
/*  156 */     this.m_binder.putLocal("ProductVersion", VersionInfo.getProductVersion());
/*  157 */     this.m_binder.putLocal("ProductVersionInfo", VersionInfo.getProductVersionInfo());
/*  158 */     this.m_binder.putLocal("ProductBuildInfo", VersionInfo.getProductBuildInfo());
/*  159 */     if (this.m_workspace != null)
/*      */     {
/*  161 */       String[] propKeys = { "DatabaseType", "DatabaseVersion", "JdbcDriverName", "JdbcDriverVersion", "UseUnicode", "NumBytesPerCharInDB", "DatabasePreserveCase", "UseDatabaseShortIndexName", "SupportSqlColumnChange", "SupportSqlColumnDelete", "UseUpperCaseColumnMap" };
/*      */ 
/*  165 */       for (int i = 0; i < propKeys.length; ++i)
/*      */       {
/*  167 */         String value = this.m_workspace.getProperty(propKeys[i]);
/*  168 */         if (value == null)
/*      */         {
/*  170 */           value = "";
/*      */         }
/*  172 */         this.m_binder.putLocal(propKeys[i], value);
/*      */       }
/*      */     }
/*      */ 
/*  176 */     String systemEncoding = FileUtils.m_javaSystemEncoding;
/*  177 */     if (systemEncoding == null)
/*      */     {
/*  179 */       systemEncoding = "<undefined>";
/*      */     }
/*  181 */     this.m_binder.putLocal("FileEncoding", systemEncoding);
/*      */     try
/*      */     {
/*  185 */       this.m_binder.putLocal("NativeVersion", NativeOsUtils.getNativeVersion());
/*      */     }
/*      */     catch (Throwable ignore)
/*      */     {
/*  189 */       this.m_binder.putLocal("NativeVersion", "");
/*  190 */       Report.trace("system", null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadConfigFileForm()
/*      */   {
/*  197 */     String intradocDir = LegacyDirectoryLocator.getIntradocDir();
/*  198 */     this.m_binder.putLocal("InstanceDir", intradocDir);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateConfigFileName() throws ServiceException
/*      */   {
/*  204 */     String configPath = this.m_binder.getLocal("ConfigFileName");
/*  205 */     if (!FileUtils.doesPathContainRelativeSegments(configPath))
/*      */       return;
/*  207 */     String msg = LocaleUtils.encodeMessage("csConfigFilePathContainsRelativeSegments", null, configPath);
/*  208 */     this.m_service.createServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadConfigFile()
/*      */     throws ServiceException
/*      */   {
/*  215 */     String intradocDir = LegacyDirectoryLocator.getIntradocDir();
/*  216 */     this.m_binder.putLocal("InstanceDir", intradocDir);
/*      */ 
/*  218 */     String configPath = this.m_binder.getLocal("ConfigFileName");
/*  219 */     if (configPath == null)
/*      */     {
/*  221 */       configPath = "";
/*      */     }
/*      */ 
/*  224 */     String absolutePath = FileUtils.getAbsolutePath(intradocDir + configPath);
/*  225 */     this.m_binder.putLocal("isSearch", "1");
/*  226 */     this.m_binder.putLocal("absolutePath", absolutePath);
/*      */ 
/*  228 */     File file = FileUtilsCfgBuilder.getCfgFile(absolutePath, null);
/*  229 */     if (!file.exists())
/*      */     {
/*  231 */       return;
/*      */     }
/*  233 */     if (file.isFile())
/*      */     {
/*  235 */       this.m_binder.putLocal("isFile", "1");
/*  236 */       DataResultSet drset = new DataResultSet(new String[] { "content" });
/*  237 */       Vector v = new Vector();
/*  238 */       IdcStringBuilder contents = new IdcStringBuilder();
/*      */       try
/*      */       {
/*  241 */         BufferedReader input = FileUtils.openDataReader(file);
/*      */         try
/*      */         {
/*  244 */           String line = null;
/*  245 */           while ((line = input.readLine()) != null)
/*      */           {
/*  247 */             contents.append(line);
/*  248 */             contents.append(System.getProperty("line.separator"));
/*      */           }
/*      */         }
/*      */         finally
/*      */         {
/*  253 */           input.close();
/*      */         }
/*      */       }
/*      */       catch (IOException ex)
/*      */       {
/*  258 */         String msg = LocaleUtils.encodeMessage("csCouldNotLoadFile", null, absolutePath);
/*  259 */         this.m_service.createServiceException(null, msg);
/*      */       }
/*      */ 
/*  262 */       v.add(contents.toString());
/*  263 */       drset.addRow(v);
/*  264 */       this.m_binder.addResultSet("FileInfo", drset);
/*      */     }
/*      */     else
/*      */     {
/*  268 */       this.m_binder.putLocal("isDirectory", "1");
/*  269 */       DataResultSet drset = new DataResultSet(new String[] { "name", "path" });
/*  270 */       String[] fileList = file.list();
/*  271 */       for (int i = 0; i < fileList.length; ++i)
/*      */       {
/*  273 */         configPath = FileUtils.directorySlashes(configPath);
/*  274 */         String path = FileUtils.fileSlashes(configPath + fileList[i]);
/*  275 */         Vector v = new Vector();
/*  276 */         v.add(fileList[i]);
/*  277 */         v.add(path);
/*  278 */         drset.addRow(v);
/*      */       }
/*  280 */       this.m_binder.addResultSet("FileList", drset);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadRunTimeConfigInfo()
/*      */   {
/*  287 */     String loadFrom = ConfigFileParameters.getLoadFromLocation();
/*  288 */     if (loadFrom.equalsIgnoreCase("Database"))
/*      */     {
/*  290 */       this.m_binder.putLocal("RuntimeConfigLoadFrom", "database");
/*  291 */       this.m_binder.putLocal("DataDir", "Load from database");
/*      */     }
/*      */     else
/*      */     {
/*  295 */       this.m_binder.putLocal("RuntimeConfigLoadFrom", "file system");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadSharedTable() throws DataException, ServiceException
/*      */   {
/*  302 */     String tableName = this.m_currentAction.getParamAt(0);
/*  303 */     if (!tableName.equals("Features"))
/*      */     {
/*  305 */       this.m_service.loadSharedTable();
/*      */     }
/*      */     else
/*      */     {
/*  309 */       String loadName = this.m_currentAction.getParamAt(1);
/*  310 */       DataResultSet featuresRset = new DataResultSet();
/*  311 */       FeaturesInterface features = Features.m_features;
/*  312 */       BasicFeatureImplementor tmp = new BasicFeatureImplementor();
/*  313 */       tmp.init();
/*  314 */       featuresRset = tmp.m_featureList;
/*  315 */       for (Map feature : features)
/*      */       {
/*  317 */         Vector row = featuresRset.createRow(new MapParameters(feature));
/*  318 */         featuresRset.addRow(row);
/*      */       }
/*  320 */       this.m_binder.addResultSet(loadName, featuresRset);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadClassLoaderInfo()
/*      */   {
/*  327 */     ClassLoader classLoader = super.getClass().getClassLoader();
/*  328 */     this.m_binder.putLocal("ClassLoader", classLoader.getClass().getSimpleName());
/*  329 */     if (!classLoader instanceof IdcClassLoader)
/*      */     {
/*  332 */       String classpath = System.getProperty("java.class.path");
/*  333 */       String pathSeparator = System.getProperty("path.separator");
/*  334 */       char separator = pathSeparator.charAt(0);
/*  335 */       List paths = StringUtils.parseArray(classpath, separator, separator);
/*  336 */       DataResultSet rsetPaths = ResultSetUtils.createResultSetFromList("ClassPaths", paths, "path");
/*  337 */       this.m_binder.addResultSet("ClassPaths", rsetPaths);
/*  338 */       return;
/*      */     }
/*  340 */     IdcClassLoader loader = (IdcClassLoader)classLoader;
/*  341 */     this.m_binder.putLocal("IdcLoaderVerbosity", String.valueOf(loader.m_zipenv.m_verbosity));
/*      */ 
/*  343 */     DataResultSet rset = makeClassPathsResultSet(loader, null);
/*  344 */     this.m_binder.addResultSet("ClassPaths", rset);
/*      */ 
/*  346 */     int count = loader.m_classInfos.size();
/*  347 */     this.m_binder.putLocal("NumTrackedClasses", String.valueOf(count));
/*  348 */     count = loader.m_loadedClasses.size();
/*  349 */     this.m_binder.putLocal("NumLoadedClasses", String.valueOf(count));
/*      */   }
/*      */ 
/*      */   protected DataResultSet makeClassPathsResultSet(IdcClassLoader loader, Map<IdcLoaderElement, String> trackRows)
/*      */   {
/*  355 */     IdcLoaderElement[] elements = loader.m_classPathElements.m_elements;
/*  356 */     String[] classPathColumns = { "path", "loadOrder" };
/*  357 */     DataResultSet rset = new DataResultSet(classPathColumns);
/*  358 */     int i = 0; for (int rowNum = 0; i < elements.length; ++i)
/*      */     {
/*  360 */       IdcLoaderElement el = elements[i];
/*  361 */       if (null == el) {
/*      */         continue;
/*      */       }
/*      */ 
/*  365 */       if (null != trackRows)
/*      */       {
/*  367 */         trackRows.put(el, String.valueOf(rowNum));
/*      */       }
/*  369 */       List row = new ArrayList(2);
/*  370 */       row.add(el.m_entryPath);
/*  371 */       row.add(String.valueOf(el.m_loadOrder));
/*  372 */       rset.addRowWithList(row);
/*  373 */       ++rowNum;
/*      */     }
/*  375 */     return rset;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadLoadedClassDetails() throws DataException
/*      */   {
/*  381 */     ClassLoader classLoader = super.getClass().getClassLoader();
/*  382 */     this.m_binder.putLocal("ClassLoader", classLoader.getClass().getSimpleName());
/*  383 */     if (!classLoader instanceof IdcClassLoader)
/*      */     {
/*  385 */       return;
/*      */     }
/*  387 */     IdcClassLoader loader = (IdcClassLoader)classLoader;
/*  388 */     String nameFilter = this.m_binder.getLocal("classname");
/*  389 */     Properties props = null;
/*      */ 
/*  391 */     Map trackRows = new HashMap();
/*  392 */     DataResultSet rset = makeClassPathsResultSet(loader, trackRows);
/*  393 */     this.m_binder.addResultSet("ClassPaths", rset);
/*      */ 
/*  395 */     String[] classInfoColumns = { "classname", "ClassPathsRow", "revision", "idcVersionInfo" };
/*  396 */     rset = new DataResultSet(classInfoColumns);
/*  397 */     this.m_binder.addResultSet("ClassDetails", rset);
/*      */ 
/*  399 */     Map allClasses = loader.m_loadedClasses;
/*  400 */     Map classInfos = loader.m_classInfos;
/*  401 */     Iterator iter = allClasses.keySet().iterator();
/*  402 */     while (iter.hasNext())
/*      */     {
/*  404 */       String classname = (String)iter.next();
/*  405 */       if ((null != nameFilter) && (!StringUtils.matchEx(classname, nameFilter, true, false))) {
/*      */         continue;
/*      */       }
/*      */ 
/*  409 */       String origin = ""; String revision = ""; String idcVersionInfo = "";
/*  410 */       IdcClassInfo info = (IdcClassInfo)classInfos.get(classname);
/*  411 */       if (null != info)
/*      */       {
/*  413 */         origin = (String)trackRows.get(info.m_origin);
/*  414 */         if (null == origin)
/*      */         {
/*  416 */           origin = "???";
/*      */         }
/*  418 */         if ((info.m_isInnerClass) || (!info.m_isRegularClass))
/*      */         {
/*  420 */           revision = "N/A";
/*      */         }
/*  422 */         else if (null == info.m_versionInfo)
/*      */         {
/*  424 */           info.lookupVersionInfo();
/*      */         }
/*  426 */         if ((null != info.m_versionInfo) && (info.m_versionInfo instanceof String))
/*      */         {
/*  428 */           idcVersionInfo = (String)info.m_versionInfo;
/*  429 */           if (null == props)
/*      */           {
/*  431 */             props = new Properties();
/*      */           }
/*  433 */           StringUtils.parsePropertiesEx(props, idcVersionInfo, ',', ',', '=');
/*  434 */           String releaseRevision = props.getProperty("releaseRevision");
/*  435 */           if ((null != releaseRevision) && (releaseRevision.startsWith("$Rev: ")))
/*      */           {
/*  437 */             int len = releaseRevision.length();
/*  438 */             if ((releaseRevision.charAt(len - 1) == '$') && (releaseRevision.charAt(len - 2) == ' '))
/*      */             {
/*  440 */               if (len < 9)
/*      */               {
/*  442 */                 revision = "BAD";
/*      */               }
/*      */               else
/*      */               {
/*  446 */                 revision = releaseRevision.substring(6, len - 2);
/*      */               }
/*      */             }
/*      */           }
/*  450 */           props.clear();
/*      */         }
/*      */       }
/*  453 */       List row = new ArrayList(4);
/*  454 */       row.add(classname);
/*  455 */       row.add(origin);
/*  456 */       row.add(revision);
/*  457 */       row.add(idcVersionInfo);
/*  458 */       rset.addRowWithList(row);
/*      */     }
/*      */ 
/*  461 */     int count = classInfos.size();
/*  462 */     this.m_binder.putLocal("NumTrackedClasses", String.valueOf(count));
/*  463 */     count = allClasses.size();
/*  464 */     this.m_binder.putLocal("NumLoadedClasses", String.valueOf(count));
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadSearchIndexerInfo() throws DataException
/*      */   {
/*  470 */     if (Features.checkLevel("Search", null) != true)
/*      */       return;
/*  472 */     CommonSearchConfig csc = SearchIndexerUtils.retrieveSearchConfig(this.m_service);
/*  473 */     String engineName = csc.getCurrentEngineName();
/*  474 */     this.m_binder.putLocal("SearchEngineName", engineName);
/*      */ 
/*  476 */     IndexerConfig indexConfig = SearchIndexerUtils.getIndexerConfig(null, "update");
/*  477 */     String indexerEngineName = indexConfig.getCurrentEngineName();
/*  478 */     if (indexerEngineName == null)
/*      */       return;
/*  480 */     this.m_binder.putLocal("IndexerEngineName", indexerEngineName);
/*      */ 
/*  482 */     if (!indexerEngineName.equalsIgnoreCase("DATABASE.METADATA"))
/*      */     {
/*  484 */       String index = ActiveIndexState.getActiveProperty("ActiveIndex");
/*  485 */       this.m_binder.putLocal("ActiveIndex", index);
/*      */     }
/*      */     else
/*      */     {
/*  489 */       this.m_binder.putLocal("ActiveIndex", "");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadJavaProperties()
/*      */   {
/*  498 */     String[] cols = { "name", "value" };
/*  499 */     DataResultSet rset = new DataResultSet(cols);
/*  500 */     Properties javaProperties = System.getProperties();
/*  501 */     Enumeration e = javaProperties.propertyNames();
/*      */ 
/*  505 */     List securedJavaPropertiesList = getSecuredJavaPropertiesList();
/*      */ 
/*  507 */     while (e.hasMoreElements())
/*      */     {
/*  509 */       String name = (String)e.nextElement();
/*  510 */       String value = javaProperties.getProperty(name);
/*      */ 
/*  512 */       if ((securedJavaPropertiesList != null) && (securedJavaPropertiesList.contains(name.toLowerCase())))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  517 */       Vector v = new IdcVector();
/*  518 */       v.addElement(name);
/*  519 */       v.addElement(value);
/*  520 */       rset.addRow(v);
/*      */     }
/*  522 */     this.m_binder.addResultSet("JavaProperties", rset);
/*      */   }
/*      */ 
/*      */   public static List getSecuredJavaPropertiesList()
/*      */   {
/*  533 */     if (m_securedJavaPropertiesList == null)
/*      */     {
/*  535 */       String securedJavaProperties = SharedObjects.getEnvironmentValue("SecuredJavaProperties");
/*  536 */       if (securedJavaProperties == null)
/*      */       {
/*  538 */         securedJavaProperties = m_defaultSecuredJavaProperties;
/*      */       }
/*  540 */       securedJavaProperties = securedJavaProperties.toLowerCase();
/*  541 */       m_securedJavaPropertiesList = StringUtils.makeListFromSequenceSimple(securedJavaProperties);
/*      */     }
/*      */ 
/*  544 */     return m_securedJavaPropertiesList;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getActionStatus()
/*      */     throws DataException, ServiceException
/*      */   {
/*  551 */     IdcLocalizationStrings strings = LocaleResources.m_stringData;
/*      */ 
/*  553 */     if (strings != null)
/*      */     {
/*  555 */       this.m_binder.putLocal("indexVersion", "" + strings.m_versionNumber);
/*  556 */       this.m_binder.putLocal("indexLanguageCount", "" + strings.m_languageMap.size());
/*  557 */       this.m_binder.putLocal("indexKeyCount", "" + strings.m_stringMap[0].size());
/*  558 */       this.m_binder.putLocal("indexBlockCount", "" + strings.m_langStringBlocks.length);
/*  559 */       this.m_binder.putLocal("indexStringCount", "" + strings.m_valLangBlockData.length / (strings.m_languageMap.size() + 1));
/*      */ 
/*  561 */       this.m_binder.putLocal("indexTagNameCount", "" + strings.m_tagLists.size());
/*  562 */       strings.m_stringMap[0].getCode("");
/*  563 */       this.m_binder.putLocal("indexTreeCount", "" + strings.m_stringMap[1].treeCount());
/*  564 */       this.m_binder.putLocal("indexVertexCount", "" + strings.m_stringMap[0].graphSize());
/*      */     }
/*      */ 
/*  567 */     DataResultSet stateInfo = SharedObjects.getTable("ProgressStateInfo");
/*  568 */     for (SimpleParameters params : stateInfo.getSimpleParametersIterable())
/*      */     {
/*  570 */       String script = params.get("psUIEnabled");
/*  571 */       String progressName = params.get("psName");
/*  572 */       String progressPrefix = params.get("psPrefix");
/*  573 */       String extraFields = params.get("psExtraFields");
/*  574 */       String extraInclude = params.get("psExtraInclude");
/*  575 */       String actions = params.get("psActions");
/*      */       String scriptResult;
/*      */       try
/*      */       {
/*  579 */         scriptResult = this.m_service.m_pageMerger.evaluateScript(script);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  583 */         throw new ServiceException(null, e);
/*      */       }
/*  585 */       if (!StringUtils.convertToBool(scriptResult, false)) {
/*      */         continue;
/*      */       }
/*      */ 
/*  589 */       this.m_binder.putLocal(progressPrefix + "_isActive", "0");
/*  590 */       this.m_binder.putLocal(progressPrefix + "_ProgressEnabled", "1");
/*  591 */       if ((extraInclude != null) && (extraInclude.length() > 0))
/*      */       {
/*  593 */         this.m_binder.putLocal(progressPrefix + "_ExtraInclude", extraInclude);
/*      */       }
/*      */ 
/*  596 */       DataResultSet extraFieldSet = new DataResultSet(new String[] { "ExtraFieldKey", "ExtraFieldLabel", "ExtraFieldEnabled" });
/*      */ 
/*  598 */       this.m_binder.addResultSet(progressPrefix + "_ExtraFields", extraFieldSet);
/*      */ 
/*  600 */       DataResultSet actionSet = new DataResultSet(new String[] { "ActionService", "ActionLabel", "ActionEnabled" });
/*      */ 
/*  602 */       this.m_binder.addResultSet(progressPrefix + "_Actions", actionSet);
/*      */ 
/*  604 */       populateResultSetFromString(extraFieldSet, extraFields);
/*  605 */       populateResultSetFromString(actionSet, actions);
/*      */ 
/*  607 */       String[] loc = findProgressLocation(progressName);
/*  608 */       if (loc == null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  612 */       String dir = loc[0];
/*  613 */       String filePath = loc[1];
/*      */ 
/*  615 */       if (FileUtils.checkFile(dir, 0) != 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  620 */       FileUtils.reserveDirectory(dir);
/*      */       try
/*      */       {
/*  623 */         DataBinder binder = new DataBinder();
/*  624 */         boolean exists = ResourceUtils.serializeDataBinder(dir, "state.hda", binder, false, false);
/*      */ 
/*  627 */         this.m_binder.putLocal("has" + progressPrefix + "State", "" + exists);
/*  628 */         if (exists)
/*      */         {
/*  630 */           binder.applyPrefix(progressPrefix + "_", 0);
/*  631 */           this.m_binder.merge(binder);
/*      */         }
/*      */ 
/*  634 */         int r = FileUtils.checkFile(filePath, true, false);
/*  635 */         boolean hasProgressFile = r == 0;
/*  636 */         this.m_binder.putLocal("has" + progressPrefix + "Progress", "" + hasProgressFile);
/*      */       }
/*      */       finally
/*      */       {
/*  640 */         FileUtils.releaseDirectory(dir);
/*      */       }
/*      */     }
/*      */ 
/*  644 */     if (!Features.checkLevel("Search", null)) {
/*      */       return;
/*      */     }
/*  647 */     IndexerMonitor.getIndexerStatus(this.m_binder);
/*      */ 
/*  650 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("INDEXER_STATUS");
/*  651 */     if (drset == null)
/*      */       return;
/*  653 */     int index = ResultSetUtils.getIndexMustExist(drset, "progressMessage");
/*  654 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  656 */       String value = drset.getStringValue(index);
/*  657 */       Vector v = StringUtils.parseArray(value, ',', '\\');
/*  658 */       if (v.size() != 4)
/*      */         continue;
/*  660 */       int type = Integer.parseInt((String)v.elementAt(0));
/*  661 */       float amtDone = new Float((String)v.elementAt(1)).floatValue();
/*  662 */       float max = new Float((String)v.elementAt(2)).floatValue();
/*  663 */       String msg = (String)v.elementAt(3);
/*      */ 
/*  665 */       value = msg;
/*  666 */       if (max >= 0.01D)
/*      */       {
/*  668 */         if (type == 0)
/*      */         {
/*  670 */           int m = (int)(max + 0.01D);
/*  671 */           int a = (int)(amtDone + 0.01D);
/*  672 */           String aMsg = LocaleUtils.encodeMessage("apReportProgress1", null, "" + a, "" + m);
/*      */ 
/*  674 */           value = value + aMsg;
/*      */         }
/*      */         else
/*      */         {
/*  678 */           float perc = 100.0F * amtDone / max;
/*  679 */           String aMsg = LocaleUtils.encodeMessage("apReportProgress2", null, "" + Math.round(perc));
/*      */ 
/*  681 */           value = value + aMsg;
/*      */         }
/*      */       }
/*  684 */       drset.setCurrentValue(index, value);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void populateResultSetFromString(DataResultSet drset, String data)
/*      */     throws ServiceException
/*      */   {
/*  694 */     List l = StringUtils.makeListFromSequence(data, '\n', '^', 32);
/*  695 */     label19: for (String rowText : l)
/*      */     {
/*  697 */       Vector row = new IdcVector();
/*  698 */       StringUtils.appendListFromSequence(row, rowText, 0, rowText.length(), ',', '^', 32);
/*      */ 
/*  700 */       if (row.size() == 3)
/*      */       {
/*  702 */         String enabledScript = (String)row.get(2);
/*      */         try
/*      */         {
/*  705 */           String enabled = this.m_service.m_pageMerger.evaluateScript(enabledScript);
/*  706 */           if (!StringUtils.convertToBool(enabled, false))
/*      */           {
/*  708 */             break label19:
/*      */           }
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*  713 */           throw new ServiceException(e);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  718 */         row.add("");
/*      */       }
/*  720 */       drset.addRow(row);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getActionProgress() throws DataException, ServiceException
/*      */   {
/*  727 */     String action = this.m_binder.getLocal("action");
/*      */ 
/*  729 */     String[] loc = requireProgressLocation(action);
/*  730 */     String dir = loc[0];
/*  731 */     String filePath = loc[1];
/*      */ 
/*  733 */     FileUtils.reserveDirectory(dir);
/*  734 */     IdcCharArrayWriter buffer = null;
/*  735 */     BufferedReader reader = null;
/*      */     try
/*      */     {
/*  738 */       int r = FileUtils.checkFile(filePath, true, false);
/*  739 */       boolean hasProgressFile = r == 0;
/*  740 */       this.m_binder.putLocal("hasProgressOutput", "" + hasProgressFile);
/*  741 */       if (hasProgressFile)
/*      */       {
/*  743 */         BufferedInputStream bstream = new BufferedInputStream(new FileInputStream(filePath));
/*  744 */         reader = FileUtils.openDataReader(bstream, "UTF8");
/*      */ 
/*  746 */         buffer = new IdcCharArrayWriter();
/*  747 */         FileUtils.copyReaderToWriter(reader, buffer);
/*      */ 
/*  749 */         String pStr = null;
/*  750 */         int length = buffer.m_length;
/*  751 */         int progressTruncate = SharedObjects.getEnvironmentInt("ProgressStateTruncateLength", 16000);
/*      */ 
/*  753 */         if (length > progressTruncate)
/*      */         {
/*  755 */           pStr = new String(buffer.m_charArray, length - progressTruncate, progressTruncate);
/*      */         }
/*      */         else
/*      */         {
/*  759 */           pStr = buffer.toString();
/*      */         }
/*  761 */         buffer.release();
/*  762 */         this.m_binder.putLocal("ProgressOutput", pStr);
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  767 */       this.m_binder.removeLocal("hasProgressOutput");
/*  768 */       Report.error(null, t, "csActionProgressOutput", new Object[] { action });
/*      */     }
/*      */     finally
/*      */     {
/*  772 */       FileUtils.releaseDirectory(dir);
/*  773 */       FileUtils.closeObjects(buffer, reader);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String[] requireProgressLocation(String actionName)
/*      */     throws ServiceException, DataException
/*      */   {
/*  780 */     String[] loc = findProgressLocation(actionName);
/*  781 */     if (loc == null)
/*      */     {
/*  783 */       IdcMessage msg = IdcMessageFactory.lc("csProgressInfoMissing", new Object[] { actionName });
/*  784 */       throw new ServiceException(null, msg);
/*      */     }
/*  786 */     return loc;
/*      */   }
/*      */ 
/*      */   protected String[] findProgressLocation(String actionName)
/*      */     throws ServiceException, DataException
/*      */   {
/*  792 */     DataResultSet actionSet = SharedObjects.getTable("ProgressStateInfo");
/*  793 */     int index = ResultSetUtils.getIndexMustExist(actionSet, "psName");
/*  794 */     Vector row = actionSet.findRow(index, actionName);
/*  795 */     if (row == null)
/*      */     {
/*  797 */       return null;
/*      */     }
/*      */ 
/*  800 */     Map actionArgs = actionSet.getCurrentRowMap();
/*  801 */     String dir = (String)actionArgs.get("ProgressDirectory");
/*  802 */     dir = PathUtils.substitutePathVariables(dir, SharedObjects.getSecureEnvironment(), null, PathUtils.F_VARS_MUST_EXIST, null);
/*      */ 
/*  805 */     String prefix = (String)actionArgs.get("psPrefix");
/*  806 */     String filePath = FileUtils.getAbsolutePath(dir, prefix.toLowerCase() + "trace.log");
/*      */ 
/*  808 */     String[] result = new String[2];
/*  809 */     result[0] = dir;
/*  810 */     result[1] = filePath;
/*      */ 
/*  812 */     return result;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateKeysAndPasswords()
/*      */     throws DataException, ServiceException
/*      */   {
/*  821 */     Map args = new HashMap();
/*  822 */     CryptoPasswordUtils.updateExpiredKeys(args);
/*      */ 
/*  826 */     CryptoPasswordUtils.updateExpiredPasswords(args);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadComponentsInfo() throws ServiceException, DataException
/*      */   {
/*  832 */     String[] cols = { "name", "location", "status", "componentInfoFound", "version", "serverVersion", "RemoveAfterVersion", "classpath", "libpath", "featureExtensions", "requiredFeatures", "additionalComponents", "installID", "hasPreferenceData" };
/*      */ 
/*  835 */     boolean isGetServerInfoOnly = this.m_service.isConditionVarTrue("IsGetServerInfoOnly");
/*  836 */     if (isGetServerInfoOnly)
/*      */     {
/*  838 */       cols = new String[] { "name", "status", "version" };
/*      */     }
/*  840 */     DataResultSet enabledComponents = new DataResultSet(cols);
/*  841 */     DataResultSet disabledComponents = new DataResultSet(cols);
/*      */ 
/*  843 */     ComponentListEditor editor = ComponentListManager.getEditor();
/*  844 */     editor.checkAndUpdateListingFile();
/*  845 */     DataResultSet components = editor.getComponentSet();
/*      */ 
/*  847 */     List tmpEnabledComponentHolder = new ArrayList();
/*  848 */     for (components.first(); components.isRowPresent(); components.next())
/*      */     {
/*  850 */       Map row = components.getCurrentRowMap();
/*      */ 
/*  852 */       String name = (String)row.get("name");
/*  853 */       String status = (String)row.get("status");
/*  854 */       boolean isEnabled = status.equalsIgnoreCase("enabled");
/*      */ 
/*  856 */       DataBinder componentBinder = null;
/*  857 */       if (isEnabled)
/*      */       {
/*  859 */         componentBinder = ComponentLoader.getComponentBinder(name);
/*      */       }
/*      */       else
/*      */       {
/*  863 */         componentBinder = ComponentLoader.getDisabledComponentBinder(name);
/*      */       }
/*  865 */       editor.getComponentData(name);
/*      */ 
/*  867 */       Map newRow = createEmptyRowMap(cols, row, componentBinder);
/*  868 */       if ((componentBinder != null) && (!isGetServerInfoOnly))
/*      */       {
/*  870 */         if (componentBinder.getLocal("StatusCode") == null)
/*      */         {
/*  872 */           newRow.put("componentInfoFound", "1");
/*      */         }
/*  874 */         String cmpPath = componentBinder.getLocal("AbsolutePath");
/*  875 */         boolean exists = (cmpPath != null) && (FileUtils.checkFile(cmpPath, true, false) == 0);
/*  876 */         if (exists)
/*      */         {
/*  879 */           newRow.put("location", cmpPath);
/*      */         }
/*  881 */         String additionalComponents = componentBinder.getLocal("additionalComponents");
/*  882 */         if (additionalComponents == null)
/*      */         {
/*  884 */           additionalComponents = "";
/*      */         }
/*      */         else
/*      */         {
/*  889 */           String temp = additionalComponents;
/*  890 */           IdcStringBuilder buffer = new IdcStringBuilder();
/*      */ 
/*  892 */           while (temp.length() > 0)
/*      */           {
/*  894 */             int index = temp.indexOf(":");
/*  895 */             if (index >= 0)
/*      */             {
/*  897 */               buffer.append(temp.substring(0, temp.indexOf(":")));
/*  898 */               if (temp.indexOf(",") == -1)
/*      */               {
/*  900 */                 temp = "";
/*      */               }
/*      */               else
/*      */               {
/*  904 */                 buffer.append(", ");
/*  905 */                 temp = temp.substring(temp.indexOf(",") + 1);
/*      */               }
/*      */             }
/*      */             else
/*      */             {
/*  910 */               buffer.append(temp);
/*  911 */               break;
/*      */             }
/*      */           }
/*      */ 
/*  915 */           additionalComponents = buffer.toString();
/*      */         }
/*  917 */         newRow.put("additionalComponents", additionalComponents);
/*      */ 
/*  919 */         String installID = ResultSetUtils.getValue(components, "installID");
/*  920 */         if (installID == null)
/*      */         {
/*  922 */           installID = "";
/*      */         }
/*  924 */         newRow.put("installID", installID);
/*      */       }
/*      */ 
/*  927 */       Parameters params = new MapParameters(newRow);
/*  928 */       Vector v = disabledComponents.createRow(params);
/*  929 */       if (isEnabled)
/*      */       {
/*  931 */         tmpEnabledComponentHolder.add(v);
/*      */       }
/*      */       else
/*      */       {
/*  935 */         disabledComponents.addRow(v);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  940 */     EnabledComponentSort cmpSort = new EnabledComponentSort();
/*  941 */     cmpSort.m_components = components;
/*  942 */     Sort.sortList(tmpEnabledComponentHolder, cmpSort);
/*      */ 
/*  944 */     int size = tmpEnabledComponentHolder.size();
/*  945 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  947 */       enabledComponents.addRow((Vector)tmpEnabledComponentHolder.get(i));
/*      */     }
/*  949 */     this.m_binder.addResultSet("EnabledComponents", enabledComponents);
/*  950 */     this.m_binder.addResultSet("DisabledComponents", disabledComponents);
/*      */   }
/*      */ 
/*      */   public Map createEmptyRowMap(String[] cols, Map<String, String> cmpRow, DataBinder cmpData)
/*      */   {
/*  955 */     Map map = new HashMap();
/*  956 */     for (int i = 0; i < cols.length; ++i)
/*      */     {
/*  958 */       String key = cols[i];
/*  959 */       String val = null;
/*  960 */       if (cmpData != null)
/*      */       {
/*  962 */         val = cmpData.getLocal(key);
/*      */       }
/*  964 */       if (val == null)
/*      */       {
/*  966 */         val = (String)cmpRow.get(key);
/*      */       }
/*  968 */       if (val == null)
/*      */       {
/*  970 */         val = "";
/*      */       }
/*  972 */       map.put(cols[i], val);
/*      */     }
/*  974 */     return map;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadEnvironmentVars() throws ServiceException, DataException
/*      */   {
/*  980 */     this.m_binder.putLocal("HttpBaseAbsoluteRoot", DocumentPathBuilder.getBaseAbsoluteRoot());
/*  981 */     this.m_binder.putLocal("HttpCgiPath", DirectoryLocator.getCgiWebUrl(false));
/*  982 */     this.m_binder.putLocal("HttpAbsoluteCgiUrl", DocumentPathBuilder.getBaseAbsoluteRoot() + DirectoryLocator.getCgiWebUrl(false));
/*      */ 
/*  985 */     this.m_binder.putLocal("ProductVersion", VersionInfo.getProductVersion());
/*  986 */     this.m_binder.putLocal("ProductVersionInfo", VersionInfo.getProductVersionInfo());
/*      */ 
/*  989 */     this.m_binder.putLocal("SystemTimeZone", LocaleResources.getSystemTimeZone().getID());
/*      */ 
/*  992 */     addConfigValues("PublishedConfigKeys");
/*  993 */     if (!Features.checkLevel("ContentManagement", null))
/*      */       return;
/*  995 */     UserData userData = this.m_service.getUserData();
/*  996 */     if (!SecurityUtils.isUserOfRole(userData, "admin"))
/*      */       return;
/*  998 */     addConfigValues("PublishedAdminConfigKeys");
/*      */   }
/*      */ 
/*      */   protected void addConfigValues(String dynamicdataName)
/*      */   {
/* 1005 */     Table t = ResourceContainerUtils.getDynamicTableResource(dynamicdataName);
/* 1006 */     int[] col = TableUtils.getIndexList(t, new String[] { "key", "defaultValue", "isBoolean" });
/*      */ 
/* 1008 */     for (int row = 0; row < t.getNumRows(); ++row)
/*      */     {
/* 1010 */       String key = t.getString(row, col[0]);
/* 1011 */       String val = this.m_binder.getEnvironmentValue(key);
/* 1012 */       if ((val == null) || (val.length() == 0))
/*      */       {
/* 1014 */         val = t.getString(row, col[1]);
/*      */       }
/*      */ 
/* 1017 */       boolean isBool = StringUtils.convertToBool(t.getString(row, col[2]), false);
/* 1018 */       if (isBool)
/*      */       {
/* 1020 */         val = (StringUtils.convertToBool(val, false)) ? "1" : "0";
/*      */       }
/*      */ 
/* 1023 */       this.m_binder.putLocal(key, val);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void appendComponentReport() throws ServiceException, DataException
/*      */   {
/* 1030 */     ComponentListEditor cle = ComponentListManager.getEditor();
/* 1031 */     DataResultSet report = cle.getComponentReport();
/* 1032 */     if (report == null)
/*      */       return;
/* 1034 */     this.m_binder.addResultSet("ComponentReport", report);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void findOrphanedComponents()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1041 */     ComponentListEditor cle = ComponentListManager.getEditor();
/* 1042 */     DataResultSet componentSet = cle.getComponentSet();
/* 1043 */     int compNameIndex = componentSet.getFieldInfoIndex("name");
/*      */ 
/* 1045 */     DataResultSet orphanedComponents = new DataResultSet(new String[] { "path" });
/*      */ 
/* 1047 */     String homeDir = null;
/* 1048 */     if (!ComponentLocationUtils.isHomeLocal(null))
/*      */     {
/* 1050 */       homeDir = SharedObjects.getEnvironmentValue("IdcHomeDir");
/* 1051 */       if (homeDir != null)
/*      */       {
/* 1053 */         homeDir = FileUtils.directorySlashes(homeDir);
/*      */       }
/*      */     }
/*      */ 
/* 1057 */     List directories = ComponentLocationUtils.getComponentParentDirectories();
/* 1058 */     for (String path : directories)
/*      */     {
/* 1060 */       if ((homeDir != null) && (path.startsWith(homeDir)))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1065 */       File f = new File(path);
/* 1066 */       if (f.isDirectory())
/*      */       {
/* 1068 */         File[] subDirs = f.listFiles();
/* 1069 */         for (File compFile : subDirs)
/*      */         {
/* 1071 */           if (!compFile.isDirectory())
/*      */             continue;
/* 1073 */           String compName = compFile.getName();
/* 1074 */           if (componentSet.findRow(compNameIndex, compName) != null)
/*      */             continue;
/* 1076 */           Vector v = new IdcVector();
/* 1077 */           v.add(compFile.getAbsolutePath());
/* 1078 */           orphanedComponents.addRow(v);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1085 */     if (orphanedComponents.getNumRows() <= 0)
/*      */       return;
/* 1087 */     this.m_binder.addResultSet("OrphanedComponents", orphanedComponents);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareMessagesXml()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1129 */     if (this.m_binder.getAllowMissing("MsgComponentId") == null)
/*      */     {
/* 1131 */       this.m_binder.putLocal("MsgComponentId", "1000");
/*      */     }
/*      */ 
/* 1135 */     DataResultSet rset = SharedObjects.getTable("UCM_MessageTokens");
/* 1136 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/* 1138 */       Map map = rset.getCurrentRowMap();
/* 1139 */       String key = (String)map.get("msgToken");
/* 1140 */       String value = (String)map.get("msgValue");
/* 1141 */       this.m_binder.putLocal(key, value);
/*      */     }
/*      */ 
/* 1144 */     DataResultSet causeSet = SharedObjects.getTable("UCM_MessageCauses");
/* 1145 */     if (causeSet == null)
/*      */       return;
/* 1147 */     this.m_binder.addResultSet("UCM_MessageCauses", causeSet);
/*      */   }
/*      */ 
/*      */   public DataBinder validateMessages()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1153 */     DataBinder statusData = new DataBinder();
/* 1154 */     Table t = ResourceContainerUtils.getDynamicTableResource("UCM_MessageKeys");
/* 1155 */     if (t == null)
/*      */     {
/* 1157 */       throw new DataException(null, "csResultSetMissing", new Object[] { "UCM_MessageKeys" });
/*      */     }
/* 1159 */     DataResultSet causeSet = (DataResultSet)this.m_binder.getResultSet("UCM_MessageCauses");
/* 1160 */     if (causeSet == null)
/*      */     {
/* 1162 */       throw new DataException(null, "csResultSetMissing", new Object[] { "UCM_MessageCauses" });
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1169 */       Map badKeys = new HashMap();
/* 1170 */       DataResultSet drset = new DataResultSet();
/* 1171 */       drset.init(t);
/* 1172 */       int causeIndex = ResultSetUtils.getIndexMustExist(causeSet, "name");
/*      */ 
/* 1175 */       String[] reqFields = { "number", "type", "level", "category" };
/*      */ 
/* 1178 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/* 1180 */         Map map = drset.getCurrentRowMap();
/* 1181 */         String name = (String)map.get("name");
/* 1182 */         String type = (String)map.get("type");
/*      */ 
/* 1185 */         int len = reqFields.length;
/* 1186 */         for (int i = 0; i < len; ++i)
/*      */         {
/* 1188 */           String val = (String)map.get(reqFields[i]);
/* 1189 */           if ((val != null) && (val.length() != 0))
/*      */             continue;
/* 1191 */           addMessageFieldToList(badKeys, name, reqFields[i]);
/*      */         }
/*      */ 
/* 1197 */         String txt = LocaleResources.getString(name, null);
/* 1198 */         if (txt == null)
/*      */         {
/* 1200 */           txt = (String)map.get("text");
/*      */         }
/* 1202 */         if ((txt == null) || (txt.length() == 0))
/*      */         {
/* 1204 */           addMessageFieldToList(badKeys, name, "text");
/*      */         }
/* 1206 */         if (!type.contains("error"))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1211 */         Vector row = causeSet.findRow(causeIndex, name);
/* 1212 */         if (row != null)
/*      */           continue;
/* 1214 */         addMessageFieldToList(badKeys, name, "cause");
/*      */       }
/*      */ 
/* 1219 */       DataResultSet badKeySet = createErrorMessageKeys(badKeys);
/* 1220 */       statusData.addResultSet("MessageKeyErrors", badKeySet);
/* 1221 */       if (!badKeySet.isEmpty())
/*      */       {
/* 1223 */         statusData.putLocal("msgHasErrors", "1");
/*      */       }
/*      */     }
/*      */     catch (Throwable tt)
/*      */     {
/* 1228 */       this.m_service.createServiceException(tt, "!csMsgValidateError");
/*      */     }
/* 1230 */     return statusData;
/*      */   }
/*      */ 
/*      */   protected void addMessageFieldToList(Map<String, List> map, String key, String value)
/*      */   {
/* 1235 */     List l = (List)map.get(key);
/* 1236 */     if (l == null)
/*      */     {
/* 1238 */       l = new ArrayList();
/* 1239 */       map.put(key, l);
/*      */     }
/* 1241 */     l.add(value);
/*      */   }
/*      */ 
/*      */   protected DataResultSet createErrorMessageKeys(Map<String, List> map)
/*      */   {
/* 1246 */     String[] clmns = { "name", "msgMissingAttributes" };
/* 1247 */     DataResultSet drset = new DataResultSet(clmns);
/* 1248 */     Set set = map.keySet();
/* 1249 */     for (String key : set)
/*      */     {
/* 1251 */       List attList = (List)map.get(key);
/* 1252 */       String val = StringUtils.createString(attList, ',', '^');
/*      */ 
/* 1254 */       Vector row = drset.createEmptyRow();
/* 1255 */       row.setElementAt(key, 0);
/* 1256 */       row.setElementAt(val, 1);
/* 1257 */       drset.addRow(row);
/*      */     }
/* 1259 */     return drset;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void generateMessagesXml()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1266 */     String filepath = this.m_binder.getAllowMissing("MessagesXmlOutputPath");
/* 1267 */     String dir = null;
/* 1268 */     if (filepath == null)
/*      */     {
/* 1270 */       dir = this.m_binder.getAllowMissing("MessagesXmlOutputDir");
/*      */     }
/* 1272 */     if (dir != null)
/*      */     {
/* 1274 */       filepath = FileUtils.directorySlashes(dir) + "ucm_messages.xml";
/*      */     }
/* 1276 */     if (filepath == null)
/*      */     {
/* 1278 */       filepath = LegacyDirectoryLocator.getAppDataDirectory() + "messages/ucm_messages.xml";
/*      */     }
/*      */ 
/* 1281 */     DataBinder statusData = validateMessages();
/* 1282 */     boolean hasErrors = DataBinderUtils.getBoolean(statusData, "msgHasErrors", false);
/*      */ 
/* 1285 */     dir = FileUtils.getDirectory(filepath);
/* 1286 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 3, true);
/* 1287 */     FileUtils.reserveDirectory(dir);
/*      */     try
/*      */     {
/* 1290 */       statusData.putLocal("msgOutFile", filepath);
/*      */ 
/* 1292 */       Date dte = new Date();
/* 1293 */       SimpleDateFormat frmt = new SimpleDateFormat("yyyy-MM-dd-HHmm");
/* 1294 */       String ts = frmt.format(dte);
/*      */ 
/* 1296 */       if (!hasErrors)
/*      */       {
/* 1299 */         File file = FileUtilsCfgBuilder.getCfgFile(filepath, "Message", false);
/* 1300 */         BufferedWriter bw = FileUtils.openDataWriter(file);
/*      */ 
/* 1302 */         statusData.putLocal("msgBuildTs", ts);
/* 1303 */         this.m_service.m_pageMerger.writeResourceInclude("generate_messages_xml", bw, true);
/* 1304 */         FileUtils.closeObject(bw);
/*      */ 
/* 1306 */         String msg = LocaleUtils.encodeMessage("csMsgCreateSuccess", null, filepath);
/* 1307 */         statusData.putLocal("StatusMessageKey", msg);
/* 1308 */         statusData.putLocal("StatusMessage", msg);
/*      */       }
/*      */       else
/*      */       {
/* 1313 */         writeStatusMsgFile(statusData, dir, ts);
/* 1314 */         this.m_service.createServiceException(null, "!csMsgErrorStatus");
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1320 */       IdcMessage msg = IdcMessageFactory.lc(e, "csMsgErrorWrite", new Object[] { filepath });
/* 1321 */       this.m_service.createServiceException(msg);
/*      */     }
/*      */     finally
/*      */     {
/* 1325 */       FileUtils.releaseDirectory(dir);
/* 1326 */       this.m_binder.merge(statusData);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void writeStatusMsgFile(DataBinder data, String dir, String ts)
/*      */   {
/*      */     try
/*      */     {
/* 1335 */       ResourceUtils.serializeDataBinder(dir, "msg_log_" + ts + ".hda", data, true, false);
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1339 */       Report.warning("system", t, "csMsgStatusWriteError", new Object[] { dir });
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getSafeSharedTables() throws ServiceException, DataException
/*      */   {
/* 1346 */     DataResultSet safeTables = SharedObjects.getTable("SafeSharedObjectsTables");
/* 1347 */     String requestedTables = this.m_binder.getLocal("tableNames");
/*      */     FieldInfo fi;
/* 1348 */     if (requestedTables == null)
/*      */     {
/* 1350 */       this.m_binder.addResultSet("SafeSharedObjectsTables", safeTables);
/*      */     }
/*      */     else
/*      */     {
/* 1354 */       fi = new FieldInfo();
/* 1355 */       safeTables.getFieldInfo("TableName", fi);
/*      */ 
/* 1357 */       List l = StringUtils.makeListFromSequenceSimple(requestedTables);
/* 1358 */       for (String tableName : l)
/*      */       {
/* 1360 */         List row = safeTables.findRow(fi.m_index, tableName, 0, 0);
/* 1361 */         if (row != null)
/*      */         {
/* 1363 */           DataResultSet drset = SharedObjects.getTable(tableName);
/* 1364 */           if (drset != null)
/*      */           {
/* 1366 */             this.m_binder.addResultSet(tableName, drset);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getSharedTables() throws ServiceException, DataException
/*      */   {
/* 1376 */     if (!SecurityUtils.isUserOfRole(this.m_service.getUserData(), "admin"))
/*      */     {
/* 1378 */       IdcMessage msg = IdcMessageFactory.lc("csSystemAccessDenied", new Object[0]);
/* 1379 */       this.m_service.createServiceException(msg);
/*      */     }
/*      */ 
/* 1382 */     String requestedTables = this.m_binder.get("tableNames");
/* 1383 */     List l = StringUtils.makeListFromSequenceSimple(requestedTables);
/* 1384 */     for (String tableName : l)
/*      */     {
/* 1386 */       DataResultSet drset = SharedObjects.getTable(tableName);
/* 1387 */       if (drset != null)
/*      */       {
/* 1389 */         this.m_binder.addResultSet(tableName, drset);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getMaxFieldLengths() throws ServiceException, DataException
/*      */   {
/* 1397 */     if (!SecurityUtils.isUserOfRole(this.m_service.getUserData(), "admin"))
/*      */     {
/* 1399 */       IdcMessage msg = IdcMessageFactory.lc("csSystemAccessDenied", new Object[0]);
/* 1400 */       this.m_service.createServiceException(msg);
/*      */     }
/*      */ 
/* 1403 */     String tableNames = this.m_binder.getLocal("tableNames");
/* 1404 */     if (tableNames == null)
/*      */     {
/* 1406 */       tableNames = "DocMeta,Documents,Revisions,FolderFolders,FolderFiles";
/*      */     }
/*      */ 
/* 1410 */     HashMap map = new HashMap();
/* 1411 */     String[] columns = { "fieldName", "fieldLength" };
/* 1412 */     DataResultSet fieldSet = new DataResultSet(columns);
/* 1413 */     String[] tableList = tableNames.split(",");
/* 1414 */     for (int t = 0; t < tableList.length; ++t)
/*      */     {
/* 1417 */       FieldInfo[] fields = this.m_workspace.getColumnList(tableList[t]);
/* 1418 */       for (int i = 0; i < fields.length; ++i)
/*      */       {
/* 1420 */         FieldInfo field = fields[i];
/* 1421 */         if ((field.m_maxLen <= 0) || (map.containsKey(field.m_name)) || ((field.m_type != 2) && (field.m_type != 5) && (field.m_type != 8) && (field.m_type != 6))) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1425 */         Vector vect = new Vector();
/* 1426 */         vect.add(field.m_name);
/* 1427 */         vect.add(field.m_maxLen + "");
/* 1428 */         fieldSet.addRow(vect); map.put(field.m_name, field.m_name);
/*      */       }
/*      */     }
/*      */ 
/* 1432 */     this.m_binder.addResultSet("FIELD_LENGTHS", fieldSet);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1437 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104839 $";
/*      */   }
/*      */ 
/*      */   class EnabledComponentSort
/*      */     implements IdcComparator
/*      */   {
/*      */     public DataResultSet m_components;
/*      */ 
/*      */     EnabledComponentSort()
/*      */     {
/* 1093 */       this.m_components = null;
/*      */     }
/*      */ 
/*      */     public int compare(Object obj1, Object obj2) {
/* 1097 */       int returnValue = 0;
/* 1098 */       if ((obj1 instanceof Vector) && (obj2 instanceof Vector))
/*      */       {
/* 1100 */         Vector a1 = (Vector)obj1;
/* 1101 */         Vector a2 = (Vector)obj2;
/*      */ 
/* 1103 */         if ((a1.size() > 1) && (a2.size() > 1))
/*      */         {
/* 1105 */           String name1 = (String)a1.elementAt(0);
/* 1106 */           String name2 = (String)a2.elementAt(0);
/* 1107 */           if (!name1.equals(name2))
/*      */           {
/* 1109 */             this.m_components.findRow(0, name1);
/* 1110 */             int index1 = this.m_components.getCurrentRow();
/* 1111 */             this.m_components.findRow(0, name2);
/* 1112 */             int index2 = this.m_components.getCurrentRow();
/* 1113 */             returnValue = 1;
/* 1114 */             if (index1 < index2)
/*      */             {
/* 1116 */               returnValue = -1;
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/* 1121 */       return returnValue;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ServerInfoHandler
 * JD-Core Version:    0.5.4
 */