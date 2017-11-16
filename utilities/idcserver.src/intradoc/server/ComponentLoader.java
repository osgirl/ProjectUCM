/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcComparator;
/*      */ import intradoc.common.IdcLinguisticComparatorAdapter;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.PathUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ResourceObject;
/*      */ import intradoc.common.ResourceTrace;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.Sort;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.common.VersionInfo;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.loader.IdcClassLoader;
/*      */ import intradoc.resource.ComponentData;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.resource.TableMergeRule;
/*      */ import intradoc.server.utils.ComponentFeatures;
/*      */ import intradoc.server.utils.ComponentListEditor;
/*      */ import intradoc.server.utils.ComponentListManager;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.DocumentPathBuilder;
/*      */ import intradoc.shared.Feature;
/*      */ import intradoc.shared.Features;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.tools.build.ComponentPackager;
/*      */ import intradoc.util.IdcException;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ComponentLoader
/*      */ {
/*      */   public static boolean m_quiet;
/*      */   public static Map m_components;
/*      */   public static Map m_disabledComponents;
/*      */   public static List<IdcMessage> m_loadedComponentMessages;
/*      */   public static Vector m_queries;
/*      */   public static Vector m_services;
/*      */   public static Vector m_templates;
/*      */   public static Vector m_resources;
/*      */   public static Vector m_environments;
/*      */   public static Vector m_installData;
/*      */   public static Map m_idToComponent;
/*      */   protected static ComponentPackager m_componentPackager;
/*      */   public static Vector m_mergeRules;
/*      */   public static Hashtable m_filters;
/*      */   static final String COMPONENT_DIR = "$COMPONENT_DIR";
/*      */   public static String[][] m_resourceFiles;
/*      */ 
/*      */   public static void reset()
/*      */   {
/*  110 */     m_quiet = false;
/*  111 */     m_components = new HashMap();
/*  112 */     m_disabledComponents = new HashMap();
/*  113 */     m_loadedComponentMessages = new ArrayList();
/*      */ 
/*  115 */     m_queries = new IdcVector();
/*  116 */     m_services = new IdcVector();
/*  117 */     m_templates = new IdcVector();
/*  118 */     m_resources = new IdcVector();
/*  119 */     m_environments = new IdcVector();
/*  120 */     m_installData = new IdcVector();
/*  121 */     m_idToComponent = new HashMap();
/*  122 */     m_componentPackager = new ComponentPackager();
/*  123 */     m_componentPackager.m_flags = 4;
/*      */ 
/*  125 */     m_mergeRules = new IdcVector();
/*  126 */     m_filters = new Hashtable();
/*      */ 
/*  128 */     m_resourceFiles = (String[][])null;
/*      */ 
/*  130 */     IdcSystemLoader.m_resourcesAlreadyLoaded = false;
/*      */   }
/*      */ 
/*      */   public static void initDefaults() throws ServiceException
/*      */   {
/*  135 */     if (m_resourceFiles != null)
/*      */     {
/*  137 */       Report.deprecatedUsage("ComponentLoader.m_resources initialized before initDefaults().  You should set PrimaryResourceFile or PrimaryResourceTable in SharedObjects to override the resources instead.");
/*      */ 
/*  143 */       clearVectors();
/*  144 */       addComponentData(m_resourceFiles);
/*  145 */       return;
/*      */     }
/*  147 */     String resourceFile = SharedObjects.getEnvironmentValue("PrimaryResourceFile");
/*  148 */     if (resourceFile == null)
/*      */     {
/*  150 */       String tablesDir = FileUtils.getAbsolutePath(LegacyDirectoryLocator.getResourcesDirectory(), "core/tables/");
/*      */ 
/*  152 */       String productNamePart = "";
/*  153 */       String productName = SharedObjects.getEnvironmentValue("IdcProductName");
/*  154 */       if (productName != null)
/*      */       {
/*  156 */         productNamePart = "-" + productName;
/*  157 */         if (FileUtils.checkFile(tablesDir + "resource_files" + productNamePart + ".htm", true, false) != 0)
/*      */         {
/*  161 */           productNamePart = "";
/*      */         }
/*      */       }
/*  164 */       resourceFile = tablesDir + "resource_files" + productNamePart + ".htm";
/*      */     }
/*  170 */     else if (resourceFile.startsWith("$"))
/*      */     {
/*  172 */       int index = resourceFile.indexOf("/");
/*  173 */       if (index > 0)
/*      */       {
/*  175 */         String prefix = resourceFile.substring(1, index);
/*  176 */         String suffix = resourceFile.substring(index);
/*  177 */         String lookupDir = SharedObjects.getEnvironmentValue(prefix);
/*  178 */         if (lookupDir != null)
/*      */         {
/*  180 */           resourceFile = FileUtils.directorySlashes(lookupDir) + suffix;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  185 */     Report.trace("componentloader", "Loading primary resource file: " + resourceFile, null);
/*      */ 
/*  187 */     ResourceContainer tmpContainer = new ResourceContainer();
/*  188 */     DataLoader.cacheResourceFile(tmpContainer, resourceFile);
/*  189 */     Table theTable = null;
/*  190 */     String tableName = SharedObjects.getEnvironmentValue("PrimaryResourceTable");
/*  191 */     if (tableName != null)
/*      */     {
/*  193 */       theTable = (Table)tmpContainer.m_tables.get(tableName);
/*  194 */       if (theTable == null)
/*      */       {
/*  196 */         Report.trace("componentloader", "unable to find the requested table " + tableName, null);
/*      */       }
/*      */     }
/*      */ 
/*  200 */     if ((theTable == null) && (tmpContainer.m_resourceList.size() > 0))
/*      */     {
/*  202 */       ResourceObject obj = (ResourceObject)tmpContainer.m_resourceList.get(0);
/*      */ 
/*  204 */       theTable = (Table)obj.m_resource;
/*      */     }
/*      */ 
/*  207 */     if (theTable == null)
/*      */     {
/*  210 */       String msg = "!$Unable to find a primary resource table to load.";
/*  211 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  214 */     m_resourceFiles = new String[theTable.getNumRows()][];
/*  215 */     for (int i = 0; i < m_resourceFiles.length; ++i)
/*      */     {
/*  217 */       m_resourceFiles[i] = theTable.getRow(i);
/*      */     }
/*      */ 
/*  221 */     clearVectors();
/*  222 */     addComponentData(m_resourceFiles);
/*      */   }
/*      */ 
/*      */   public static void addComponentData(String[][] resourceFiles)
/*      */     throws ServiceException
/*      */   {
/*  228 */     Map resourceDirMap = new HashMap();
/*      */ 
/*  230 */     List resDirList = new ArrayList();
/*  231 */     List tmplDirList = new ArrayList();
/*  232 */     List reportDirList = new ArrayList();
/*      */ 
/*  234 */     resourceDirMap.put("resourcesDir", resDirList);
/*  235 */     resourceDirMap.put("templateDir", tmplDirList);
/*  236 */     resourceDirMap.put("reportDir", reportDirList);
/*      */ 
/*  238 */     Properties env = SharedObjects.getSafeEnvironment();
/*  239 */     for (Iterator i$ = resourceDirMap.keySet().iterator(); i$.hasNext(); ) { type = (String)i$.next();
/*      */ 
/*  241 */       dirList = (List)resourceDirMap.get(type);
/*  242 */       type = type + "_";
/*  243 */       for (i$ = env.keySet().iterator(); i$.hasNext(); ) { Object key = i$.next();
/*      */ 
/*  245 */         if ((key instanceof String) && (((String)key).startsWith(type)))
/*      */         {
/*  247 */           String val = env.getProperty((String)key);
/*  248 */           String dir = DocumentPathBuilder.evaluatePathScript(val, null, PathUtils.F_VARS_MUST_EXIST, null);
/*  249 */           if (!val.equals(dir))
/*      */           {
/*  251 */             Report.trace("componentloader", "adding " + dir + " (from " + key + "=" + val + ") to search list", null);
/*      */           }
/*      */           else
/*      */           {
/*  256 */             Report.trace("componentloader", "adding " + dir + " (from " + key + ") to search list", null);
/*      */           }
/*      */ 
/*  259 */           dirList.add(dir);
/*      */         } }
/*      */  }
/*      */ 
/*      */     String type;
/*      */     List dirList;
/*      */     Iterator i$;
/*  264 */     resDirList.add(LegacyDirectoryLocator.getResourcesDirectory());
/*  265 */     tmplDirList.add(LegacyDirectoryLocator.getTemplatesDirectory());
/*  266 */     reportDirList.add(LegacyDirectoryLocator.getReportsDirectory());
/*      */ 
/*  268 */     label762: for (String[] resourceFileInfo : resourceFiles)
/*      */     {
/*  270 */       String type = resourceFileInfo[0];
/*  271 */       String resType = resourceFileInfo[1];
/*  272 */       String file = resourceFileInfo[2];
/*  273 */       String table = resourceFileInfo[3];
/*      */ 
/*  275 */       int order = -100;
/*      */       try
/*      */       {
/*  279 */         if (resourceFileInfo.length > 4)
/*      */         {
/*  281 */           order = Integer.parseInt(resourceFileInfo[4]);
/*      */         }
/*      */       }
/*      */       catch (NumberFormatException e)
/*      */       {
/*  286 */         Report.error("componentloader", e, "csComponentUnableToParseResource", new Object[] { resourceFileInfo[4], file });
/*      */ 
/*  288 */         order = -100;
/*      */       }
/*      */ 
/*  291 */       Report.trace("componentloader", "Loading core resource file: " + file + " \ttype: " + type + " \torder: " + order, null);
/*      */ 
/*  293 */       List dirList = (List)resourceDirMap.get(resType);
/*  294 */       if (dirList == null)
/*      */       {
/*  296 */         throw new ServiceException("!$AJK Unknown resource type " + resType + ".");
/*      */       }
/*  298 */       int dirListSize = dirList.size();
/*  299 */       ServiceException se = null;
/*  300 */       for (int i = 0; i < dirListSize; ++i)
/*      */       {
/*  302 */         String dir = (String)dirList.get(i);
/*      */ 
/*  306 */         dir = FileUtils.getAbsolutePath(null, dir);
/*      */         ComponentData data;
/*      */         try
/*      */         {
/*  310 */           data = new ComponentData("Default", type, dir, file, table, order);
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/*  314 */           if (se == null)
/*      */           {
/*  316 */             se = new ServiceException(e, "csComponentLoadingError", new Object[] { "Default" });
/*      */           }
/*  318 */           se.addCause(e);
/*  319 */           break label762:
/*      */         }
/*  321 */         if (type.equals("query"))
/*      */         {
/*  323 */           m_queries.addElement(data);
/*      */         }
/*  325 */         else if (type.equals("resource"))
/*      */         {
/*  327 */           m_resources.addElement(data);
/*      */         }
/*  329 */         else if (type.equals("service"))
/*      */         {
/*  331 */           m_services.addElement(data);
/*      */         }
/*  333 */         else if (type.equals("template"))
/*      */         {
/*  335 */           m_templates.addElement(data);
/*      */         }
/*  337 */         se = null;
/*  338 */         break;
/*      */       }
/*  340 */       if (se == null)
/*      */         continue;
/*  342 */       throw se;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void clearVectors()
/*      */   {
/*  349 */     m_queries.removeAllElements();
/*  350 */     m_resources.removeAllElements();
/*  351 */     m_services.removeAllElements();
/*  352 */     m_templates.removeAllElements();
/*  353 */     m_environments.removeAllElements();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void load(String filePath)
/*      */     throws DataException, ServiceException
/*      */   {
/*  361 */     Report.deprecatedUsage("ComponentLoader.load(String) is deprecated.  Use ComponentLoader.load().");
/*      */ 
/*  363 */     load();
/*      */   }
/*      */ 
/*      */   public static void load()
/*      */     throws DataException, ServiceException
/*      */   {
/*  370 */     ComponentListManager.init();
/*  371 */     ComponentListEditor compLE = ComponentListManager.getEditor();
/*      */ 
/*  375 */     compLE.save();
/*      */ 
/*  380 */     DataResultSet rset = compLE.getComponentSet();
/*      */ 
/*  383 */     SharedObjects.putTable("Components", rset);
/*      */ 
/*  386 */     String[] fields = { "name", "status" };
/*  387 */     FieldInfo[] info = null;
/*      */     try
/*      */     {
/*  390 */       info = ResultSetUtils.createInfoList(rset, fields, true);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  394 */       throw new ServiceException("!csComponentTableFormatError", e);
/*      */     }
/*      */ 
/*  398 */     ComponentClassFactory clFactory = new ComponentClassFactory("ClassAliases");
/*  399 */     SharedObjects.putTable("ClassAliases", clFactory);
/*      */ 
/*  402 */     ResourceTrace.msg("!csComponentLoadJava");
/*      */ 
/*  405 */     ClassLoader clLoader = ComponentLoader.class.getClassLoader();
/*  406 */     IdcClassLoader loader = (clLoader instanceof IdcClassLoader) ? (IdcClassLoader)clLoader : null;
/*  407 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  409 */       String name = rset.getStringValue(info[0].m_index);
/*  410 */       String status = rset.getStringValue(info[1].m_index);
/*  411 */       boolean isEnabled = status.equalsIgnoreCase("Enabled");
/*      */       try
/*      */       {
/*  414 */         DataBinder binder = compLE.getComponentData(name);
/*      */ 
/*  416 */         String componentDirpath = binder.getLocal("ComponentDir");
/*  417 */         if (componentDirpath != null)
/*      */         {
/*  419 */           ComponentPackager packager = m_componentPackager;
/*  420 */           File componentDir = new File(componentDirpath);
/*  421 */           packager.init(componentDir);
/*  422 */           packager.m_componentBinder = binder;
/*  423 */           packager.stampVersion();
/*      */         }
/*      */ 
/*  426 */         if (isEnabled)
/*      */         {
/*  428 */           loadEnabledComponent(name, binder, clFactory, loader);
/*      */         }
/*      */         else
/*      */         {
/*  432 */           loadDisabledComponentInfo(name, binder);
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  437 */         if (SharedObjects.getEnvValueAsBoolean("IgnoreComponentLoadError", false))
/*      */         {
/*  439 */           Report.error("componentloader", e, "csComponentLoadError", new Object[0]);
/*      */         }
/*      */         else
/*      */         {
/*  443 */           throw new ServiceException(e);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  448 */     SharedObjects.putObject("globalObjects", "LoadedComponentMessages", m_loadedComponentMessages);
/*      */   }
/*      */ 
/*      */   public static String[] computeComponentPathArray(String pathString, String componentDir)
/*      */   {
/*  461 */     int COMPONENT_DIR_LENGTH = "$COMPONENT_DIR".length();
/*      */ 
/*  466 */     pathString = pathString.replace(';', ':');
/*  467 */     List pathList = StringUtils.parseArray(pathString, ':', '^');
/*  468 */     int numPaths = pathList.size();
/*  469 */     String[] paths = new String[numPaths];
/*  470 */     for (int i = 0; i < numPaths; ++i)
/*      */     {
/*  472 */       String path = (String)pathList.get(i);
/*  473 */       if (path.startsWith("$COMPONENT_DIR"))
/*      */       {
/*  475 */         IdcStringBuilder str = new IdcStringBuilder(componentDir);
/*  476 */         str.append(path.substring(COMPONENT_DIR_LENGTH));
/*  477 */         path = str.toString();
/*      */       }
/*  479 */       else if (path.startsWith("$"))
/*      */       {
/*      */         try
/*      */         {
/*  483 */           path = DocumentPathBuilder.evaluatePathScript(path, null, 0, null);
/*      */         }
/*      */         catch (Throwable t)
/*      */         {
/*  487 */           Report.trace("componentloader", "unable to compute component classpath", t);
/*      */         }
/*      */       }
/*  490 */       paths[i] = path;
/*      */     }
/*  492 */     return paths;
/*      */   }
/*      */ 
/*      */   public static void logComponentLoadError(String errorClass, String name, IdcMessage msg, Throwable t)
/*      */   {
/*  498 */     Hashtable componentLoadErrors = (Hashtable)SharedObjects.getObject("Errors", errorClass);
/*      */ 
/*  500 */     if (componentLoadErrors == null)
/*      */     {
/*  502 */       componentLoadErrors = new Hashtable();
/*  503 */       SharedObjects.putObject("Errors", errorClass, componentLoadErrors);
/*      */     }
/*  505 */     boolean alreadyReported = componentLoadErrors.get(name) != null;
/*  506 */     Object[] errorObj = { msg, t };
/*  507 */     componentLoadErrors.put(name, errorObj);
/*  508 */     if (alreadyReported)
/*      */     {
/*  510 */       IdcMessage traceMsg = IdcMessageFactory.lc();
/*  511 */       traceMsg.m_msgLocalized = "not reporting duplicate error";
/*  512 */       if (msg != null)
/*      */       {
/*  514 */         traceMsg.setPrior(msg);
/*      */       }
/*  516 */       if (t instanceof IdcException)
/*      */       {
/*  518 */         IdcException exp = (IdcException)t;
/*  519 */         traceMsg.setPrior(exp.m_message);
/*      */       }
/*  521 */       else if (t != null)
/*      */       {
/*  523 */         IdcMessage exp = IdcMessageFactory.lc();
/*  524 */         exp.m_msgLocalized = t.getMessage();
/*  525 */         traceMsg.setPrior(exp);
/*      */       }
/*      */ 
/*  528 */       IdcStringBuilder locMsg = LocaleResources.localizeMessage(null, traceMsg, null);
/*  529 */       Report.trace("componentloader", null, locMsg.toString(), new Object[0]);
/*  530 */       if (SystemUtils.m_verbose)
/*      */       {
/*  532 */         Report.debug("componentloader", null, msg);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  537 */       if (t == null)
/*      */       {
/*  540 */         t = new ServiceException("");
/*      */       }
/*  542 */       Report.error("componentloader", t, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void verifyFeatures()
/*      */     throws ServiceException, DataException
/*      */   {
/*  553 */     ResultSet rset = SharedObjects.getTable("Components");
/*  554 */     if (null == rset)
/*      */     {
/*  556 */       return;
/*      */     }
/*  558 */     ComponentFeatures featureUtils = ComponentListManager.getFeatures(4);
/*  559 */     featureUtils.verifyFeatures(rset, m_components);
/*      */   }
/*      */ 
/*      */   public static List getMissingFeatures(String reqFeatures) throws ServiceException
/*      */   {
/*  564 */     List reqFeaturesList = StringUtils.parseArrayEx(reqFeatures, ',', '^', true);
/*  565 */     int numFeatures = reqFeaturesList.size();
/*  566 */     List missingFeatures = new ArrayList(numFeatures);
/*      */ 
/*  568 */     for (int i = 0; i < numFeatures; ++i)
/*      */     {
/*  570 */       String featureString = (String)reqFeaturesList.get(i);
/*  571 */       Feature feature = new Feature(featureString);
/*      */ 
/*  573 */       if (Features.checkLevel(feature.m_featureName, feature.m_featureLevel))
/*      */         continue;
/*  575 */       missingFeatures.add(feature);
/*      */     }
/*      */ 
/*  579 */     if (missingFeatures.size() > 0)
/*      */     {
/*  581 */       return missingFeatures;
/*      */     }
/*  583 */     return null;
/*      */   }
/*      */ 
/*      */   public static void loadEnabledComponent(String name, DataBinder binder, ComponentClassFactory clFactory, IdcClassLoader loader)
/*      */     throws DataException, ServiceException
/*      */   {
/*  590 */     if (binder == null)
/*      */     {
/*  593 */       Report.warning("componentloader", null, new ServiceException(null, "csComponentDataNotFound", new Object[] { name }));
/*      */ 
/*  595 */       return;
/*      */     }
/*  597 */     m_components.put(name, binder);
/*      */ 
/*  599 */     String tracing = binder.getLocal("defaultTracing");
/*  600 */     List tracingList = StringUtils.makeListFromSequenceSimple(tracing);
/*  601 */     if (tracingList.size() > 0)
/*      */     {
/*  603 */       for (int i = 0; i < tracingList.size(); ++i)
/*      */       {
/*  605 */         SystemUtils.addAsDefaultTrace((String)tracingList.get(i));
/*      */       }
/*      */     }
/*  608 */     String fExts = binder.getLocal("featureExtensions");
/*  609 */     if ((fExts != null) && (fExts.length() > 0))
/*      */     {
/*  611 */       String version = binder.getLocal("version");
/*  612 */       if ((version == null) || (version.length() == 0))
/*      */       {
/*  614 */         version = "1.0";
/*  615 */         binder.putLocal("version", version);
/*      */       }
/*      */ 
/*  618 */       List fExtsList = StringUtils.makeListFromSequenceSimple(fExts);
/*      */ 
/*  621 */       IdcMessage compMsg = IdcMessageFactory.lc("csComponentFeatureExtensionsVersion", new Object[] { name, version, fExtsList });
/*      */ 
/*  623 */       m_loadedComponentMessages.add(compMsg);
/*  624 */       Report.trace("componentloader", null, compMsg);
/*  625 */       if (!m_quiet)
/*      */       {
/*  627 */         String msg = LocaleResources.localizeMessage(null, compMsg, null).toString();
/*  628 */         SystemUtils.outln(msg);
/*      */       }
/*  630 */       Features.registerFeatures(fExts, name);
/*      */     }
/*      */ 
/*  633 */     String requiredServerBuild = binder.getLocal("serverVersion");
/*  634 */     String removeAfterBuild = binder.getLocal("RemoveAfterVersion");
/*  635 */     String systemBuild = VersionInfo.getProductVersionInfo();
/*      */ 
/*  637 */     if ((requiredServerBuild != null) && (requiredServerBuild.length() > 0) && (SystemUtils.isOlderVersion(systemBuild, requiredServerBuild)))
/*      */     {
/*  640 */       String msg = LocaleUtils.encodeMessage("csComponentRequiredBuildWarning", null, new Object[] { name, requiredServerBuild, systemBuild });
/*      */ 
/*  643 */       Report.info("componentloader", null, "csComponentRequiredBuildWarning", new Object[] { null, { name, requiredServerBuild, systemBuild } });
/*      */ 
/*  645 */       SystemUtils.outln(LocaleResources.localizeMessage(msg, null));
/*      */     }
/*      */ 
/*  648 */     if ((removeAfterBuild != null) && (removeAfterBuild.length() > 0) && (SystemUtils.isOlderVersion(removeAfterBuild, systemBuild)))
/*      */     {
/*  651 */       String msg = LocaleUtils.encodeMessage("csComponentRemoveAfterBuildWarning", null, new Object[] { name, removeAfterBuild, systemBuild });
/*      */ 
/*  653 */       Report.info("componentloader", null, "csComponentRemoveAfterBuildWarning", new Object[] { null, { name, removeAfterBuild, systemBuild } });
/*      */ 
/*  655 */       SystemUtils.outln(LocaleResources.localizeMessage(msg, null));
/*      */     }
/*      */ 
/*  658 */     String cmptDir = binder.getLocal("ComponentDir");
/*  659 */     Vector filters = null;
/*  660 */     String tableName = "ResourceDefinition";
/*      */     try
/*      */     {
/*  664 */       ResourceTrace.msg(LocaleUtils.encodeMessage("csComponentLoadName", null, name));
/*      */ 
/*  667 */       Report.trace("componentloader", "loading component " + name, null);
/*  668 */       loadDefinitions(name, binder, cmptDir, tableName);
/*      */ 
/*  671 */       tableName = clFactory.getTableName();
/*  672 */       clFactory.loadClasses(binder);
/*      */ 
/*  675 */       DataResultSet classRset = (DataResultSet)binder.getResultSet(tableName);
/*  676 */       ResourceUtils.doResultSetLog(classRset, "", tableName, null);
/*      */ 
/*  679 */       tableName = "MergeRules";
/*  680 */       loadRules(binder, tableName);
/*      */ 
/*  683 */       tableName = "Filters";
/*  684 */       filters = PluginFilterLoader.cacheFilters(binder, tableName);
/*  685 */       if (filters != null)
/*      */       {
/*  688 */         PluginFilters.registerFilters(filters);
/*      */ 
/*  691 */         DataResultSet filterRset = (DataResultSet)binder.getResultSet(tableName);
/*  692 */         ResourceUtils.doResultSetLog(filterRset, "", "Filters", null);
/*      */       }
/*      */ 
/*  695 */       String installID = binder.getLocal("installID");
/*  696 */       if ((installID != null) && (installID.length() > 0))
/*      */       {
/*  698 */         m_installData.addElement(installID);
/*  699 */         m_idToComponent.put(installID, name);
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  704 */       String msg = LocaleUtils.encodeMessage("csComponentDefTableError", null, tableName);
/*      */ 
/*  706 */       msg = LocaleUtils.encodeMessage("csComponentLoadError", msg, name);
/*  707 */       throw new ServiceException(msg, e);
/*      */     }
/*      */ 
/*  710 */     if (loader == null) {
/*      */       return;
/*      */     }
/*  713 */     String exceptionsString = binder.getLocal("IdcClassLoaderExceptions");
/*  714 */     if (null != exceptionsString)
/*      */     {
/*  716 */       Vector exceptions = StringUtils.parseArray(exceptionsString, ',', '^');
/*  717 */       int numExceptions = exceptions.size();
/*  718 */       for (int i = 0; i < numExceptions; ++i)
/*      */       {
/*  720 */         String exception = (String)exceptions.get(i);
/*  721 */         int length = exception.length();
/*  722 */         int index = exception.indexOf(42);
/*  723 */         if (index < 0)
/*      */         {
/*  725 */           loader.setUseParentForClass(exception, true);
/*      */         }
/*  727 */         else if (index + 1 < length)
/*      */         {
/*  729 */           Report.trace("componentloader", "IdcClassLoaderExceptions only supports prefix wildcards", null);
/*      */         }
/*      */         else
/*      */         {
/*  733 */           exception = exception.substring(0, length - 1);
/*  734 */           loader.setUseParentForClassPrefix(exception);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  740 */     String pathOrderString = binder.getLocal("classpathorder");
/*  741 */     int pathOrder = NumberUtils.parseInteger(pathOrderString, 1);
/*  742 */     if (SystemUtils.m_isDevelopmentEnvironment)
/*      */     {
/*      */       try
/*      */       {
/*  747 */         String classpath = cmptDir + "/classes/";
/*  748 */         File classesDir = new File(classpath);
/*  749 */         if (classesDir.isDirectory())
/*      */         {
/*  751 */           loader.addClassPathElement(classpath, pathOrder);
/*      */         }
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/*      */       }
/*      */     }
/*      */ 
/*  759 */     String classpath = binder.getLocal("classpath");
/*  760 */     if (null == classpath)
/*      */       return;
/*  762 */     String[] paths = computeComponentPathArray(classpath, cmptDir);
/*  763 */     for (int i = 0; i < paths.length; ++i)
/*      */     {
/*  765 */       if (paths[i].length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/*  771 */         loader.addClassPathElement(paths[i], pathOrder);
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadDisabledComponentInfo(String name, DataBinder binder)
/*      */     throws DataException, ServiceException
/*      */   {
/*  785 */     if (binder == null)
/*      */     {
/*  788 */       Report.warning("componentloader", null, "csComponentDataNotFound", new Object[] { name });
/*  789 */       return;
/*      */     }
/*      */ 
/*  792 */     m_disabledComponents.put(name, binder);
/*      */   }
/*      */ 
/*      */   public static void sortComponents()
/*      */   {
/*  798 */     IdcComparator cmp = new IdcComparator()
/*      */     {
/*      */       public int compare(Object obj1, Object obj2)
/*      */       {
/*  802 */         ComponentData c1 = (ComponentData)obj1;
/*  803 */         ComponentData c2 = (ComponentData)obj2;
/*  804 */         if (c1.m_order > c2.m_order)
/*      */         {
/*  806 */           return 1;
/*      */         }
/*  808 */         if (c1.m_order < c2.m_order)
/*      */         {
/*  810 */           return -1;
/*      */         }
/*  812 */         return 0;
/*      */       }
/*      */     };
/*  817 */     sortComponentArray(m_queries, cmp);
/*  818 */     sortComponentArray(m_services, cmp);
/*  819 */     sortComponentArray(m_templates, cmp);
/*  820 */     sortComponentArray(m_resources, cmp);
/*  821 */     sortComponentArray(m_environments, cmp);
/*  822 */     PluginFilters.sortFilters();
/*      */ 
/*  825 */     IdcLinguisticComparatorAdapter lingCmp = new IdcLinguisticComparatorAdapter()
/*      */     {
/*      */       public int compare(Object obj1, Object obj2)
/*      */       {
/*  830 */         TableMergeRule c1 = (TableMergeRule)obj1;
/*  831 */         TableMergeRule c2 = (TableMergeRule)obj2;
/*  832 */         if (c1.m_toTable.equals(c2.m_toTable))
/*      */         {
/*  834 */           if (c1.m_order > c2.m_order)
/*      */           {
/*  836 */             return 1;
/*      */           }
/*  838 */           if (c1.m_order < c2.m_order)
/*      */           {
/*  840 */             return -1;
/*      */           }
/*  842 */           return 0;
/*      */         }
/*      */ 
/*  845 */         int comparison = 0;
/*  846 */         comparison = super.compare(c1.m_toTable, c2.m_toTable);
/*  847 */         return comparison;
/*      */       }
/*      */     };
/*  853 */     lingCmp.init(IdcLinguisticComparatorAdapter.m_defaultRule);
/*      */ 
/*  855 */     sortComponentArray(m_mergeRules, lingCmp);
/*      */   }
/*      */ 
/*      */   public static void loadDefinitions(String cmptName, DataBinder binder, String dir, String tableName)
/*      */     throws DataException, ServiceException
/*      */   {
/*  861 */     DataResultSet rset = (DataResultSet)binder.getResultSet(tableName);
/*  862 */     if (rset == null)
/*      */     {
/*  864 */       return;
/*      */     }
/*      */ 
/*  868 */     String[] fields = { "type", "filename", "tables", "loadOrder" };
/*  869 */     FieldInfo[] info = ResultSetUtils.createInfoList(rset, fields, true);
/*      */ 
/*  872 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  874 */       String type = rset.getStringValue(info[0].m_index);
/*  875 */       String filename = rset.getStringValue(info[1].m_index);
/*  876 */       String tables = rset.getStringValue(info[2].m_index);
/*  877 */       String order = rset.getStringValue(info[3].m_index);
/*  878 */       int iOrder = parseOrder(order);
/*      */ 
/*  880 */       ComponentData data = new ComponentData(cmptName, type, dir, filename, tables, iOrder);
/*      */ 
/*  882 */       if (type.equalsIgnoreCase("query"))
/*      */       {
/*  884 */         m_queries.addElement(data);
/*      */       }
/*  886 */       else if (type.equalsIgnoreCase("template"))
/*      */       {
/*  888 */         m_templates.addElement(data);
/*      */       }
/*  890 */       else if (type.equalsIgnoreCase("service"))
/*      */       {
/*  892 */         m_services.addElement(data);
/*      */       }
/*  894 */       else if (type.equalsIgnoreCase("resource"))
/*      */       {
/*  896 */         m_resources.addElement(data);
/*      */       } else {
/*  898 */         if (!type.equalsIgnoreCase("environment"))
/*      */           continue;
/*  900 */         m_environments.addElement(data);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static int parseOrder(String order) throws DataException
/*      */   {
/*  907 */     int iOrder = 0;
/*      */     try
/*      */     {
/*  910 */       iOrder = Integer.parseInt(order);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  914 */       throw new DataException(LocaleUtils.encodeMessage("csComponentParseOrderError", null, order));
/*      */     }
/*      */ 
/*  917 */     return iOrder;
/*      */   }
/*      */ 
/*      */   public static void loadRules(DataBinder binder, String tableName) throws DataException
/*      */   {
/*  922 */     DataResultSet rset = (DataResultSet)binder.getResultSet(tableName);
/*  923 */     if (rset == null)
/*      */     {
/*  925 */       return;
/*      */     }
/*      */ 
/*  929 */     String[] fields = { "fromTable", "toTable", "column" };
/*  930 */     FieldInfo[] info = ResultSetUtils.createInfoList(rset, fields, true);
/*      */ 
/*  933 */     FieldInfo fieldInfo = new FieldInfo();
/*  934 */     int orderIndex = -1;
/*  935 */     if (rset.getFieldInfo("loadOrder", fieldInfo))
/*      */     {
/*  937 */       orderIndex = fieldInfo.m_index;
/*      */     }
/*      */ 
/*  941 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  943 */       String from = rset.getStringValue(info[0].m_index);
/*  944 */       String to = rset.getStringValue(info[1].m_index);
/*  945 */       String column = rset.getStringValue(info[2].m_index);
/*  946 */       int loadOrder = 1;
/*  947 */       if (orderIndex > -1)
/*      */       {
/*  949 */         String order = rset.getStringValue(orderIndex);
/*  950 */         loadOrder = parseOrder(order);
/*      */       }
/*      */ 
/*  953 */       TableMergeRule rule = new TableMergeRule(from, to, column, loadOrder);
/*      */ 
/*  955 */       m_mergeRules.addElement(rule);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void sortComponentArray(Vector v, IdcComparator cmp)
/*      */   {
/*  964 */     Sort.sortVector(v, cmp);
/*      */   }
/*      */ 
/*      */   public static void addExtraComponentColumn(String name, DataResultSet drset, ComponentData data)
/*      */   {
/*  970 */     String str = SharedObjects.getEnvironmentValue("TablesToSkipComponentInfoMerge");
/*  971 */     Vector ignoreList = StringUtils.parseArray(str, ',', '^');
/*  972 */     int size = ignoreList.size();
/*  973 */     boolean isIgnoredTable = false;
/*  974 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  976 */       String ignore = (String)ignoreList.elementAt(i);
/*  977 */       if (!ignore.equals(name))
/*      */         continue;
/*  979 */       isIgnoredTable = true;
/*  980 */       break;
/*      */     }
/*      */ 
/*  983 */     if (isIgnoredTable)
/*      */       return;
/*      */     try
/*      */     {
/*  987 */       FieldInfo info = new FieldInfo();
/*  988 */       if (!drset.getFieldInfo("idcComponentName", info))
/*      */       {
/*  990 */         Vector fields = ResultSetUtils.createFieldInfo(new String[] { "idcComponentName" }, 30);
/*  991 */         drset.mergeFieldsWithFlags(fields, 2);
/*      */ 
/*  993 */         int index = ((FieldInfo)fields.get(0)).m_index;
/*  994 */         String cmpName = "Default";
/*  995 */         if (data != null)
/*      */         {
/*  997 */           cmpName = data.m_componentName;
/*      */         }
/*  999 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*      */         {
/* 1001 */           drset.setCurrentValue(index, cmpName);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1007 */       Report.trace("system", "Unable to add idcComponentName column to resource table " + name + ".", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static DataBinder getComponentBinder(String componentName)
/*      */   {
/* 1015 */     return (DataBinder)m_components.get(componentName);
/*      */   }
/*      */ 
/*      */   public static String getComponentDir(String componentName)
/*      */   {
/* 1020 */     DataBinder binder = getComponentBinder(componentName);
/*      */ 
/* 1022 */     String dir = null;
/* 1023 */     if (binder != null)
/*      */     {
/* 1025 */       dir = binder.getAllowMissing("ComponentDir");
/*      */     }
/* 1027 */     return dir;
/*      */   }
/*      */ 
/*      */   public static DataBinder getDisabledComponentBinder(String componentName)
/*      */   {
/* 1032 */     return (DataBinder)m_disabledComponents.get(componentName);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1038 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98928 $";
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*  105 */     reset();
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ComponentLoader
 * JD-Core Version:    0.5.4
 */