/*      */ package intradoc.apputilities.installer;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NativeOsUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.data.MutableResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.resource.ResourceObjectLoader;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.utils.SystemPropertiesEditor;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.localization.SharedLocalizationHandler;
/*      */ import intradoc.shared.localization.SharedLocalizationHandlerFactory;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedOutputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.text.ParseException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public abstract class InteractiveInstaller
/*      */ {
/*      */   public static final int F_USE_DEFAULT_VALUE = 1;
/*      */   protected Properties m_installProps;
/*      */   protected Properties m_overrideProps;
/*      */   protected Properties m_originalProps;
/*      */   protected PromptUser m_promptUser;
/*      */   protected InstallLog m_log;
/*      */   protected ReportProgress m_progress;
/*      */   protected DataBinder m_binder;
/*      */   protected String m_userLocale;
/*      */   protected String[][] m_localeChoices;
/*      */   protected Hashtable m_regionTimeZones;
/*      */   protected String m_defaultTimeZone;
/*      */   protected String m_defaultRegion;
/*      */   protected String[][] m_regionChoices;
/*      */   protected String m_supportedDatabases;
/*      */   protected String m_supportedWebServers;
/*   94 */   protected Vector m_propList = new IdcVector();
/*   95 */   protected Vector m_reviewPropList = new IdcVector();
/*   96 */   protected boolean m_isUpdate = false;
/*      */ 
/*   98 */   protected static boolean m_isRefineryInstall = false;
/*      */ 
/*  100 */   protected Properties m_keyLabelMap = new Properties();
/*  101 */   protected Properties m_usedConfigurationValues = new Properties();
/*  102 */   protected Properties m_possibleDefaultValues = new Properties();
/*      */   protected String[][] m_yesNoOptions;
/*      */   protected String m_installationName;
/*      */   protected String m_configFile;
/*      */   protected NativeOsUtils m_utils;
/*  107 */   protected Properties m_passwordFields = new IdcProperties();
/*      */ 
/*  109 */   public String m_registryBase = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Stellent\\Content Server";
/*      */ 
/*  111 */   public String m_defaultWindowsName = "c:/oracle/ucm/server";
/*  112 */   public String m_defaultUnixName = "/oracle/ucm/server";
/*      */   protected String m_platform;
/*      */   protected String m_currentPlatform;
/*      */   protected SysInstaller m_installer;
/*  128 */   public String[] m_encodingTestKeys = { "csNotEnoughParametersForAction", "csConnectionNotOpen", "csUnableToGetFileFormatInfo", "csFieldNotDefined" };
/*      */ 
/*      */   public InteractiveInstaller(Properties installerProps, Properties overrideProps, PromptUser prompter)
/*      */   {
/*  139 */     this.m_installProps = installerProps;
/*  140 */     this.m_originalProps = ((Properties)installerProps.clone());
/*  141 */     this.m_overrideProps = overrideProps;
/*  142 */     this.m_promptUser = prompter;
/*      */   }
/*      */ 
/*      */   public int doInstall(DataBinder binder, InstallLog log, ReportProgress progress)
/*      */     throws DataException, ServiceException
/*      */   {
/*  149 */     this.m_installer = new SysInstaller();
/*  150 */     this.m_installer.init(binder, this.m_installProps, this.m_overrideProps, log, progress, this.m_promptUser);
/*      */ 
/*  152 */     this.m_utils = this.m_installer.m_utils;
/*  153 */     this.m_log = log;
/*  154 */     this.m_binder = binder;
/*  155 */     this.m_progress = progress;
/*      */ 
/*  157 */     this.m_platform = this.m_installer.getInstallValue("Platform", null);
/*  158 */     this.m_currentPlatform = this.m_installer.determineCurrentPlatform();
/*  159 */     if (this.m_currentPlatform == null)
/*      */     {
/*  161 */       throw new ServiceException("Unable to determine your platform.");
/*      */     }
/*  163 */     if (this.m_platform == null)
/*      */     {
/*  165 */       this.m_platform = this.m_currentPlatform;
/*      */     }
/*  167 */     String msg = LocaleResources.getString("csInstallerExplainUse", null);
/*  168 */     this.m_promptUser.outputMessage(msg);
/*      */ 
/*  170 */     Properties platformProps = this.m_installer.getInstallerTable("PlatformConfigTable", this.m_platform);
/*      */ 
/*  172 */     if ((this.m_platform == null) || (platformProps == null))
/*      */     {
/*  174 */       this.m_platform = promptUser("Platform", "!csInstallerPlatformPrompt", "linux", (String[][])null, true);
/*      */     }
/*      */ 
/*  178 */     platformProps = this.m_installer.getInstallerTable("PlatformConfigTable", this.m_platform);
/*      */ 
/*  180 */     if (platformProps == null)
/*      */     {
/*  182 */       throw new ServiceException(null, "csInstallerUnsupportedPlatform", new Object[] { this.m_platform });
/*      */     }
/*  184 */     this.m_supportedDatabases = platformProps.getProperty("SupportedDatabaseServers");
/*      */ 
/*  186 */     this.m_supportedWebServers = platformProps.getProperty("SupportedWebServers");
/*      */ 
/*  189 */     String srcDir = null;
/*  190 */     for (boolean acceptDefaults = true; ; acceptDefaults = false)
/*      */     {
/*  192 */       srcDir = promptUser("SourceDirectory", "!csInstallerInstallSourceLabel", "/", (String[][])null, acceptDefaults);
/*      */ 
/*  194 */       File info = new File(FileUtils.directorySlashes(srcDir) + "intradoc.cfg");
/*      */ 
/*  196 */       if (info.exists()) {
/*      */         break;
/*      */       }
/*      */ 
/*  200 */       this.m_promptUser.outputMessage(LocaleResources.getString("csInstallerMissingInstallFile", null));
/*      */ 
/*  202 */       this.m_originalProps.remove("SourceDirectory");
/*      */     }
/*      */ 
/*  205 */     DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/*  206 */     Vector choices = new IdcVector();
/*  207 */     String defaultLocale = SharedObjects.getEnvironmentValue("SystemLocale");
/*  208 */     this.m_defaultTimeZone = "UTC";
/*  209 */     for (localeConfig.first(); localeConfig.isRowPresent(); localeConfig.next())
/*      */     {
/*  211 */       Properties props = localeConfig.getCurrentRowProps();
/*  212 */       boolean isEnabled = StringUtils.convertToBool(props.getProperty("lcIsEnabled"), false);
/*  213 */       if (!isEnabled)
/*      */         continue;
/*  215 */       String key = props.getProperty("lcLanguageId");
/*  216 */       String name = LocaleResources.getStringInternal("syLanguageName_" + key);
/*  217 */       if (name == null)
/*      */       {
/*  219 */         name = LocaleResources.getStringInternal("syLanguageName");
/*      */       }
/*  221 */       String[] choice = new String[2];
/*  222 */       choice[0] = props.getProperty("lcLocaleId");
/*  223 */       if (choice[0].equals(defaultLocale))
/*      */       {
/*  225 */         this.m_defaultTimeZone = props.getProperty("lcTimeZone");
/*  226 */         if ((this.m_defaultTimeZone == null) || (this.m_defaultTimeZone.length() == 0))
/*      */         {
/*  228 */           String pattern = props.getProperty("lcDateTimeFormat");
/*  229 */           if (pattern != null)
/*      */           {
/*  231 */             IdcDateFormat fmt = new IdcDateFormat();
/*      */             try
/*      */             {
/*  234 */               fmt.init(pattern);
/*  235 */               this.m_defaultTimeZone = fmt.getTimeZone().getID();
/*      */             }
/*      */             catch (ParseException ignore)
/*      */             {
/*  239 */               Report.trace("install", null, ignore);
/*      */             }
/*      */           }
/*      */         }
/*  243 */         if ((this.m_defaultTimeZone == null) || (this.m_defaultTimeZone.length() == 0))
/*      */         {
/*  245 */           this.m_defaultTimeZone = "UTC";
/*      */         }
/*      */       }
/*      */ 
/*  249 */       if (name == null)
/*      */       {
/*  251 */         choice[1] = choice[0];
/*      */       }
/*      */       else
/*      */       {
/*  255 */         choice[1] = (choice[0] + " (" + name + ")");
/*      */       }
/*  257 */       choices.addElement(choice);
/*      */     }
/*      */ 
/*  261 */     this.m_localeChoices = new String[choices.size()][2];
/*  262 */     for (int i = 0; i < this.m_localeChoices.length; ++i)
/*      */     {
/*  264 */       this.m_localeChoices[i] = ((String[])(String[])choices.elementAt(i));
/*      */     }
/*  266 */     this.m_userLocale = promptUser("UserLocale", "!csInstallerSelectUserLocale", defaultLocale, this.m_localeChoices, true);
/*      */ 
/*  268 */     IdcLocale locale = LocaleResources.getLocale(this.m_userLocale);
/*  269 */     if (locale != null)
/*      */     {
/*  271 */       LocaleResources.m_defaultContext.setCachedObject("UserLocale", locale);
/*      */     }
/*  273 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/*  274 */     cxt.setCachedObject("UserLocale", locale);
/*      */ 
/*  276 */     SharedLocalizationHandler slh = SharedLocalizationHandlerFactory.createInstance();
/*  277 */     this.m_regionTimeZones = new Hashtable();
/*  278 */     choices = new IdcVector();
/*  279 */     DataResultSet timezones = slh.getTimeZones(cxt);
/*  280 */     for (timezones.first(); timezones.isRowPresent(); timezones.next())
/*      */     {
/*  282 */       String id = ResultSetUtils.getValue(timezones, "lcTimeZone");
/*  283 */       int index = id.indexOf("/");
/*  284 */       if (index <= 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*  288 */       String region = id.substring(0, index);
/*  289 */       Vector zoneList = (Vector)this.m_regionTimeZones.get(region);
/*  290 */       if (zoneList == null)
/*      */       {
/*  292 */         zoneList = new IdcVector();
/*  293 */         this.m_regionTimeZones.put(region, zoneList);
/*  294 */         choices.addElement(region);
/*      */       }
/*  296 */       zoneList.addElement(id);
/*      */     }
/*  298 */     this.m_regionChoices = new String[choices.size()][2];
/*  299 */     for (int i = 0; i < this.m_regionChoices.length; ++i)
/*      */     {
/*  301 */       String[] choice = new String[2];
/*  302 */       String region = choice[0] =  = choice[1] =  = (String)choices.elementAt(i);
/*  303 */       this.m_regionChoices[i] = choice;
/*      */ 
/*  305 */       Vector tmp = (Vector)this.m_regionTimeZones.get(choice[0]);
/*  306 */       String[][] tmpChoices = new String[tmp.size()][2];
/*  307 */       for (int j = 0; j < tmpChoices.length; ++j)
/*      */       {
/*  309 */         choice = new String[2];
/*      */         String tmp1053_1050 = ((String)tmp.elementAt(j)); choice[1] = tmp1053_1050; choice[0] = tmp1053_1050;
/*  311 */         tmpChoices[j] = choice;
/*      */       }
/*  313 */       this.m_regionTimeZones.put(region, tmpChoices);
/*      */     }
/*  315 */     if (!SharedObjects.getEnvValueAsBoolean("OverrideSystemTimeZone", false))
/*      */     {
/*  317 */       String[][] tmpChoices = new String[this.m_regionChoices.length + 1][2];
/*  318 */       this.m_defaultRegion = "osDefault";
/*  319 */       tmpChoices[0] = { this.m_defaultRegion, LocaleResources.getString("csInstallerUseOSTimeZone", null) };
/*      */ 
/*  321 */       System.arraycopy(this.m_regionChoices, 0, tmpChoices, 1, this.m_regionChoices.length);
/*  322 */       this.m_regionChoices = tmpChoices;
/*      */     }
/*  324 */     if ((this.m_defaultRegion != null) && (this.m_defaultTimeZone != null) && (this.m_defaultRegion.length() == 0) && (this.m_defaultTimeZone.indexOf("/") > 0))
/*      */     {
/*  327 */       int index = this.m_defaultTimeZone.indexOf("/");
/*  328 */       this.m_defaultRegion = this.m_defaultTimeZone.substring(0, index);
/*      */     }
/*      */ 
/*  331 */     this.m_yesNoOptions = this.m_installer.getInstallerTableAsArray("Options_YesNo");
/*      */ 
/*  333 */     LocaleResources.localizeDoubleArray(this.m_yesNoOptions, null, 1);
/*      */ 
/*  335 */     String productList = this.m_overrideProps.getProperty("IdcProductNameList", this.m_installProps.getProperty("IdcProductNameList"));
/*      */ 
/*  337 */     List products = StringUtils.makeListFromSequenceSimple(productList);
/*  338 */     Map usedProducts = new HashMap();
/*  339 */     usedProducts.put("idctemplate", "idctemplate");
/*  340 */     List productChoices = new ArrayList();
/*  341 */     for (String product : products)
/*      */     {
/*  343 */       if (usedProducts.get(product) != null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  347 */       usedProducts.put(product, product);
/*  348 */       String[] choice = new String[2];
/*  349 */       choice[0] = product;
/*  350 */       choice[1] = ("!csProductDescription_" + product);
/*  351 */       productChoices.add(choice);
/*      */     }
/*  353 */     String[][] productChoicesArray = new String[productChoices.size()][];
/*  354 */     productChoicesArray = (String[][])productChoices.toArray(productChoicesArray);
/*  355 */     LocaleResources.localizeDoubleArray(productChoicesArray, null, 1);
/*  356 */     promptUser("IdcProductName", "!csSelectProduct", "idccs", productChoicesArray, true);
/*      */ 
/*  359 */     Vector options = new IdcVector();
/*  360 */     if (!interactWithUser(options))
/*      */     {
/*  362 */       return 1;
/*      */     }
/*  364 */     this.m_installer.addLocaleOptions(options);
/*      */ 
/*  368 */     Properties propsToWrite = saveConfig(this.m_configFile);
/*      */ 
/*  370 */     options.add("--set-IsRelaunch=true");
/*  371 */     if (propsToWrite.size() > 0)
/*      */     {
/*  373 */       options.addElement("--set-ReadPropertiesFromStdin=true");
/*  374 */       options.addElement("--set-ReadPropertiesFromStdinPrompt=");
/*      */     }
/*      */     else
/*      */     {
/*  378 */       options.addElement("--set-ReadPropertiesFromStdin=false");
/*      */     }
/*      */ 
/*  381 */     return this.m_installer.runInstall(this.m_installProps, this.m_configFile, options, null, null, propsToWrite);
/*      */   }
/*      */ 
/*      */   public abstract boolean interactWithUser(Vector paramVector)
/*      */     throws DataException, ServiceException;
/*      */ 
/*      */   public void setIsRefinery(boolean isRefinery)
/*      */   {
/*  390 */     m_isRefineryInstall = isRefinery;
/*  391 */     if (m_isRefineryInstall)
/*      */     {
/*  394 */       SharedObjects.putEnvironmentValue("DefaultApplicationName", "ibr");
/*  395 */       LocaleResources.m_defaultApp = "ibr";
/*      */     }
/*      */     else
/*      */     {
/*  400 */       SharedObjects.putEnvironmentValue("DefaultApplicationName", "");
/*  401 */       LocaleResources.m_defaultApp = null;
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean isUpdate() {
/*  406 */     return this.m_isUpdate;
/*      */   }
/*      */ 
/*      */   public void promptForComponents()
/*      */     throws DataException, ServiceException
/*      */   {
/*  415 */     DataResultSet drset = (DataResultSet)this.m_installer.m_binder.getResultSet("Components");
/*      */ 
/*  417 */     if (drset == null)
/*      */     {
/*  419 */       Report.trace("install", "Components table not defined", null);
/*  420 */       return;
/*      */     }
/*  422 */     IdcMessage explaination = IdcMessageFactory.lc("csInstallerExplainComponents", new Object[0]);
/*  423 */     IdcStringBuilder tmpInstallComponents = new IdcStringBuilder();
/*  424 */     String[][] options = getComponentOptionList(drset, tmpInstallComponents);
/*  425 */     if (options.length == 0)
/*      */     {
/*  427 */       Report.trace("install", "No user-selectable components", null);
/*  428 */       return;
/*      */     }
/*  430 */     String installComponents = tmpInstallComponents.toString();
/*      */     while (true)
/*      */     {
/*  433 */       installComponents = this.m_promptUser.prompt(2, explaination, installComponents, options, null);
/*      */ 
/*  437 */       List l = StringUtils.makeListFromSequence(installComponents, ',', '^', 64);
/*      */ 
/*  439 */       for (int i = 0; i < options.length; ++i)
/*      */       {
/*  441 */         String compName = options[i][0];
/*  442 */         if (l.indexOf(compName) >= 0)
/*      */         {
/*  444 */           setProp("InstallComponent_" + compName, "1");
/*      */         }
/*      */         else
/*      */         {
/*  448 */           setProp("InstallComponent_" + compName, "0");
/*      */         }
/*      */       }
/*  451 */       removeProp("Components");
/*  452 */       this.m_reviewPropList.add("Components=" + installComponents);
/*  453 */       this.m_keyLabelMap.put("Components", "!csInstallerComponentsLabel");
/*      */ 
/*  455 */       ComponentInstallUtils cutils = new ComponentInstallUtils(this.m_installer);
/*  456 */       ComponentAnalyzer a = cutils.analyzeComponents();
/*  457 */       if (a.m_userMessage == null) {
/*      */         return;
/*      */       }
/*      */ 
/*  461 */       String msg = a.m_userMessage;
/*      */ 
/*  463 */       Iterator it = a.m_autoDepends.keySet().iterator();
/*  464 */       while (it.hasNext())
/*      */       {
/*  466 */         String name = (String)it.next();
/*  467 */         List depList = (List)a.m_autoDepends.get(name);
/*  468 */         String listString = StringUtils.createStringEx(depList, ',', '^', true);
/*      */ 
/*  470 */         String tmpMsg = LocaleUtils.encodeMessage("csInstallerDependencyInfo", null, name, listString);
/*      */ 
/*  473 */         msg = LocaleUtils.appendMessage(tmpMsg, msg);
/*      */       }
/*      */ 
/*  476 */       String[][] autoOptions = this.m_installer.getInstallerTableAsArray("Options_EnableRequiredComponents");
/*      */ 
/*  478 */       LocaleResources.localizeDoubleArray(autoOptions, null, 1);
/*  479 */       String autoAccept = this.m_promptUser.prompt(1, null, "true", autoOptions, msg);
/*      */ 
/*  481 */       if (StringUtils.convertToBool(autoAccept, false))
/*      */       {
/*  483 */         for (int i = 0; i < a.m_autoInstallList.size(); ++i)
/*      */         {
/*  485 */           ComponentAnalyzerData c = (ComponentAnalyzerData)a.m_autoInstallList.get(i);
/*      */ 
/*  487 */           setProp("InstallComponent_" + c.m_name, "1");
/*      */         }
/*  489 */         return;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public String[][] getComponentOptionList(MutableResultSet rset, IdcStringBuilder initialSelections)
/*      */     throws ServiceException
/*      */   {
/*  498 */     IdcStringBuilder defaultList = new IdcStringBuilder();
/*  499 */     IdcStringBuilder selectedList = null;
/*  500 */     ArrayList list = new ArrayList();
/*      */ 
/*  502 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  504 */       Properties props = rset.getCurrentRowProps();
/*  505 */       String compName = props.getProperty("ComponentName");
/*  506 */       String conditions = props.getProperty("Conditions");
/*  507 */       conditions = this.m_installer.evaluateScript(conditions);
/*  508 */       if (conditions.equals("optional"))
/*      */       {
/*  510 */         conditions = "optional:false";
/*      */       }
/*  512 */       Report.trace("install", "conditions for " + compName + " evaluate to " + conditions, null);
/*      */ 
/*  514 */       if (!conditions.startsWith("optional:"))
/*      */         continue;
/*  516 */       String flag = conditions.substring("optional:".length());
/*  517 */       if (StringUtils.convertToBool(flag, false))
/*      */       {
/*  519 */         if (defaultList.length() > 0)
/*      */         {
/*  521 */           defaultList.append(',');
/*      */         }
/*  523 */         defaultList.append(compName);
/*      */       }
/*  525 */       if (StringUtils.convertToBool(this.m_installProps.getProperty("InstallComponent_" + compName), false))
/*      */       {
/*  529 */         if (selectedList == null)
/*      */         {
/*  531 */           selectedList = new IdcStringBuilder();
/*      */         }
/*      */         else
/*      */         {
/*  535 */           selectedList.append(',');
/*      */         }
/*  537 */         selectedList.append(compName);
/*      */       }
/*  539 */       String label = LocaleResources.getString("csInstallerCompLabel", null, compName, "csInstallerCompDescr_" + compName);
/*      */ 
/*  542 */       String[] row = { compName, label };
/*  543 */       list.add(row);
/*      */     }
/*      */ 
/*  546 */     if (selectedList == null)
/*      */     {
/*  548 */       initialSelections.append(defaultList);
/*      */     }
/*      */     else
/*      */     {
/*  552 */       initialSelections.append(selectedList);
/*      */     }
/*  554 */     String[][] options = new String[list.size()][];
/*  555 */     options = (String[][])(String[][])list.toArray(options);
/*  556 */     return options;
/*      */   }
/*      */ 
/*      */   protected Properties saveConfig(String path) throws ServiceException
/*      */   {
/*  565 */     OutputStream outputStream = null;
/*      */     Properties propsToWrite;
/*      */     try
/*      */     {
/*  568 */       String dir = FileUtils.getDirectory(path);
/*  569 */       FileUtils.checkOrCreateDirectory(dir, 2);
/*  570 */       outputStream = new BufferedOutputStream(FileUtilsCfgBuilder.getCfgOutputStream(path, null));
/*      */ 
/*  572 */       propsToWrite = saveConfig(outputStream);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  580 */       FileUtils.closeObject(outputStream);
/*      */     }
/*  582 */     return propsToWrite;
/*      */   }
/*      */ 
/*      */   protected Properties saveConfig(OutputStream stream)
/*      */     throws ServiceException
/*      */   {
/*  589 */     Properties propsToWrite = new IdcProperties();
/*  590 */     int propListSize = this.m_propList.size();
/*  591 */     for (Enumeration en = this.m_installProps.propertyNames(); en.hasMoreElements(); )
/*      */     {
/*  593 */       String key = (String)en.nextElement();
/*  594 */       String value = this.m_installProps.getProperty(key);
/*  595 */       String newValue = key + "=" + value;
/*  596 */       boolean found = false;
/*  597 */       for (int i = 0; (!found) && (i < propListSize); ++i)
/*      */       {
/*  599 */         String listValue = (String)this.m_propList.elementAt(i);
/*  600 */         if (!listValue.startsWith(key + "="))
/*      */           continue;
/*  602 */         this.m_propList.setElementAt(newValue, i);
/*  603 */         found = true;
/*      */       }
/*      */ 
/*  606 */       if (!found)
/*      */       {
/*  608 */         this.m_propList.addElement(newValue);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  613 */     Vector tmpList = new IdcVector();
/*  614 */     String passwordList = "";
/*  615 */     for (int i = 0; i < this.m_propList.size(); ++i)
/*      */     {
/*  617 */       String entry = (String)this.m_propList.get(i);
/*  618 */       int index = entry.indexOf("=");
/*  619 */       if (index < 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*  623 */       String key = entry.substring(0, index);
/*  624 */       String value = entry.substring(index + 1);
/*  625 */       entry = key + "=" + StringUtils.encodeLiteralStringEscapeSequence(value);
/*      */ 
/*  627 */       boolean isPassword = (this.m_passwordFields.get(key) != null) || ((this.m_installer.getConfigFlags(key) & 0x80) > 0L);
/*      */ 
/*  629 */       if ((isPassword) && (value.length() == 0)) {
/*      */         continue;
/*      */       }
/*      */ 
/*  633 */       if (isPassword)
/*      */       {
/*  636 */         propsToWrite.put(key, value);
/*  637 */         tmpList.add("#" + key + "=*");
/*  638 */         if (passwordList.length() > 0)
/*      */         {
/*  640 */           passwordList = passwordList + ",";
/*      */         }
/*  642 */         passwordList = passwordList + key;
/*      */       }
/*      */       else
/*      */       {
/*  646 */         tmpList.add(entry);
/*      */       }
/*      */     }
/*  649 */     if (passwordList.length() > 0)
/*      */     {
/*  651 */       ConsolePromptUser tmpPromptUser = new ConsolePromptUser();
/*  652 */       String msgText = LocaleResources.localizeMessage(null, IdcMessageFactory.lc("csInstallerPasswordSaveText", new Object[0]), null).toString();
/*      */ 
/*  654 */       msgText = "\n" + tmpPromptUser.formatParagraph(msgText, 60);
/*  655 */       msgText = msgText.replace("\n", "\n## ");
/*  656 */       tmpList.add(msgText);
/*  657 */       tmpList.add("PasswordPromptList=" + passwordList);
/*      */     }
/*  659 */     SystemPropertiesEditor.writeFile(this.m_installProps, new IdcVector(), tmpList, stream, "UTF8");
/*      */ 
/*  661 */     return propsToWrite;
/*      */   }
/*      */ 
/*      */   public String promptUser(String key, String label, String defaultValue, String[][] options, boolean autoAccept)
/*      */     throws ServiceException
/*      */   {
/*  668 */     Properties props = new Properties();
/*  669 */     return promptUserEx(key, label, defaultValue, options, autoAccept, props);
/*      */   }
/*      */ 
/*      */   public String promptUserEx(String key, String label, String defaultValue, String[][] options, boolean autoAccept, Properties answerProps)
/*      */     throws ServiceException
/*      */   {
/*  678 */     int promptType = 0;
/*  679 */     this.m_keyLabelMap.put(key, label);
/*  680 */     Properties entryInfo = null;
/*  681 */     if (this.m_installer != null)
/*      */     {
/*  683 */       entryInfo = this.m_installer.getInstallerTable("ConfigEntries", key);
/*      */     }
/*      */ 
/*  686 */     long flags = this.m_installer.getConfigFlags(key);
/*  687 */     Properties flagArgs = new Properties();
/*      */ 
/*  689 */     String value = null;
/*  690 */     if (this.m_isUpdate)
/*      */     {
/*  692 */       value = this.m_installer.m_intradocConfig.getProperty(key);
/*  693 */       if ((autoAccept) && (value != null))
/*      */       {
/*  695 */         answerProps.put(key, value);
/*  696 */         return value;
/*      */       }
/*      */     }
/*      */ 
/*  700 */     if ((value == null) && (this.m_installer.m_utils != null))
/*      */     {
/*  702 */       value = this.m_installer.m_utils.getEnv("Idc" + key);
/*  703 */       if ((autoAccept) && (value != null))
/*      */       {
/*  705 */         answerProps.put(key, value);
/*  706 */         return value;
/*      */       }
/*      */     }
/*  709 */     if (value == null)
/*      */     {
/*  711 */       value = this.m_originalProps.getProperty(key);
/*  712 */       if (value != null)
/*      */       {
/*  714 */         answerProps.put(key, value);
/*  715 */         return value;
/*      */       }
/*      */     }
/*  718 */     if (value == null)
/*      */     {
/*  720 */       value = this.m_installProps.getProperty(key);
/*      */     }
/*      */ 
/*  723 */     if ((flags & 0x100) == 0L)
/*      */     {
/*  725 */       if (value != null)
/*      */       {
/*  727 */         defaultValue = value;
/*      */       }
/*  729 */       if ((defaultValue == null) || (defaultValue.length() == 0))
/*      */       {
/*  731 */         String tmp = defaultConfigValueFromOtherServers(key);
/*  732 */         if (tmp != null)
/*      */         {
/*  734 */           defaultValue = tmp;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  739 */     String origDefaultValue = null;
/*  740 */     if ((flags & 0x80) != 0L)
/*      */     {
/*  742 */       if (defaultValue != null)
/*      */       {
/*  747 */         origDefaultValue = defaultValue;
/*  748 */         char[] strArray = defaultValue.toCharArray();
/*  749 */         for (int i = 0; i < strArray.length; ++i)
/*      */         {
/*  751 */           strArray[i] = '*';
/*      */         }
/*  753 */         defaultValue = new String(strArray);
/*      */       }
/*  755 */       promptType = 3;
/*      */     }
/*      */ 
/*  758 */     String promptText = label;
/*  759 */     String explaination = null;
/*  760 */     if (entryInfo != null)
/*      */     {
/*  762 */       explaination = entryInfo.getProperty("Explaination");
/*      */     }
/*      */     boolean acceptValue;
/*  765 */     if ((options != null) && (options.length == 1))
/*      */     {
/*  767 */       value = options[0][0];
/*      */     }
/*      */     else
/*      */     {
/*  771 */       if (options != null)
/*      */       {
/*  773 */         promptType = 1;
/*      */       }
/*  775 */       Report.trace("install", "prompting for key " + key, null);
/*  776 */       answerProps.put(key + "_UserInteractionRequired", "1");
/*  777 */       answerProps.put("UserInteractionRequired", "1");
/*  778 */       for (acceptValue = false; !acceptValue; )
/*      */       {
/*  780 */         acceptValue = true;
/*  781 */         value = this.m_promptUser.prompt(promptType, promptText, defaultValue, options, explaination);
/*      */ 
/*  783 */         if ((value == null) || (entryInfo == null))
/*      */           continue;
/*  785 */         if (((flags & 0x80) != 0L) && 
/*  789 */           (value.equals(defaultValue)) && (origDefaultValue != null))
/*      */         {
/*  791 */           value = origDefaultValue;
/*      */         }
/*      */ 
/*  794 */         if ((flags & 0x10) != 0L)
/*      */         {
/*  796 */           String flagArg = flagArgs.getProperty("max");
/*  797 */           int length = NumberUtils.parseInteger(flagArg, -1);
/*  798 */           if (length <= 0)
/*      */           {
/*  800 */             Report.trace("install", "illegal max length \"" + flagArg + "\"", null);
/*      */           }
/*  805 */           else if (value.length() > length)
/*      */           {
/*  807 */             String msg = LocaleResources.getString("syValueTooLong", null, value, "" + length);
/*      */ 
/*  810 */             this.m_promptUser.outputMessage(msg);
/*  811 */             acceptValue = false;
/*      */           }
/*      */         }
/*      */ 
/*  815 */         if ((!acceptValue) || ((flags & 0x20) == 0L))
/*      */           continue;
/*  817 */         String server = isConfigValueUsed(key, value);
/*  818 */         if (server != null)
/*      */         {
/*  820 */           String msg = LocaleResources.getString("csInstallDuplicateValue", null, value);
/*      */ 
/*  822 */           this.m_promptUser.outputMessage(msg);
/*  823 */           acceptValue = false;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  829 */     checkAbort(value);
/*  830 */     value = value.trim();
/*      */ 
/*  832 */     if (value.length() == 0)
/*      */     {
/*  834 */       value = defaultValue;
/*      */     }
/*      */ 
/*  837 */     boolean isPassword = (flags & 0x80) != 0L;
/*  838 */     if (isPassword)
/*      */     {
/*  840 */       this.m_passwordFields.put(key, value);
/*      */     }
/*      */ 
/*  843 */     String prefix = key + "=";
/*  844 */     int size = this.m_reviewPropList.size();
/*  845 */     if (size > 0)
/*      */     {
/*  847 */       String last = (String)this.m_reviewPropList.elementAt(size - 1);
/*  848 */       if (last.startsWith(prefix))
/*      */       {
/*  850 */         this.m_reviewPropList.removeElementAt(size - 1);
/*      */       }
/*      */     }
/*  853 */     String propEntry = prefix + value;
/*  854 */     if (options != null)
/*      */     {
/*  856 */       propEntry = prefix + value;
/*  857 */       for (int i = 0; i < options.length; ++i)
/*      */       {
/*  859 */         if (!value.equals(options[i][0]))
/*      */           continue;
/*  861 */         propEntry = prefix + options[i][1];
/*  862 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  867 */     if (!isPassword)
/*      */     {
/*  869 */       this.m_reviewPropList.addElement(propEntry);
/*      */     }
/*  871 */     size = this.m_propList.size();
/*  872 */     while (size-- > 0)
/*      */     {
/*  874 */       String entry = (String)this.m_propList.elementAt(size);
/*  875 */       if (entry.startsWith(prefix))
/*      */       {
/*  877 */         this.m_propList.setElementAt(propEntry, size);
/*  878 */         break;
/*      */       }
/*      */     }
/*  881 */     if (size < 0)
/*      */     {
/*  883 */       this.m_propList.addElement(propEntry);
/*      */     }
/*      */ 
/*  886 */     this.m_installProps.put(key, value);
/*  887 */     answerProps.put(key, value);
/*  888 */     return value;
/*      */   }
/*      */ 
/*      */   public String promptUserEx(String key, String nameOverride, String label, String defaultValue, String[][] options, boolean autoAccept)
/*      */     throws ServiceException
/*      */   {
/*  895 */     this.m_keyLabelMap.put(nameOverride, label);
/*  896 */     Properties entryInfo = null;
/*  897 */     if (this.m_installer != null)
/*      */     {
/*  899 */       entryInfo = this.m_installer.getInstallerTable("ConfigEntries", key);
/*      */     }
/*      */ 
/*  902 */     String value = null;
/*  903 */     if (this.m_isUpdate)
/*      */     {
/*  905 */       value = this.m_installer.m_intradocConfig.getProperty(nameOverride);
/*  906 */       if ((autoAccept) && (value != null))
/*      */       {
/*  908 */         return value;
/*      */       }
/*      */     }
/*      */ 
/*  912 */     if (this.m_installer.m_utils != null)
/*      */     {
/*  914 */       value = this.m_installer.m_utils.getEnv("Idc" + nameOverride);
/*  915 */       if (value != null)
/*      */       {
/*  917 */         defaultValue = value;
/*      */       }
/*      */     }
/*  920 */     value = this.m_installProps.getProperty(nameOverride);
/*  921 */     if (value != null)
/*      */     {
/*  923 */       if (autoAccept)
/*      */       {
/*  925 */         return value;
/*      */       }
/*  927 */       defaultValue = value;
/*      */     }
/*  929 */     if ((defaultValue == null) || (defaultValue.length() == 0))
/*      */     {
/*  931 */       String tmp = defaultConfigValueFromOtherServers(nameOverride);
/*  932 */       if (tmp != null)
/*      */       {
/*  934 */         defaultValue = tmp;
/*      */       }
/*      */     }
/*      */ 
/*  938 */     String promptText = label;
/*  939 */     String explaination = null;
/*  940 */     if (entryInfo != null)
/*      */     {
/*  942 */       explaination = entryInfo.getProperty("Explaination");
/*      */     }
/*      */     boolean acceptValue;
/*  945 */     if ((options != null) && (options.length == 1))
/*      */     {
/*  947 */       value = options[0][0];
/*      */     }
/*      */     else
/*      */     {
/*  951 */       for (acceptValue = false; !acceptValue; )
/*      */       {
/*  953 */         acceptValue = true;
/*  954 */         value = this.m_promptUser.prompt((options != null) ? 1 : 0, promptText, defaultValue, options, explaination);
/*      */ 
/*  957 */         if ((value == null) || (entryInfo == null))
/*      */           continue;
/*  959 */         String flagsString = entryInfo.getProperty("Flags");
/*  960 */         Properties flagArgs = new Properties();
/*  961 */         long flags = this.m_installer.parseFlags(flagsString, flagArgs);
/*  962 */         if ((flags & 0x10) != 0L)
/*      */         {
/*  964 */           String flagArg = flagArgs.getProperty("max");
/*  965 */           int length = NumberUtils.parseInteger(flagArg, -1);
/*  966 */           if (length <= 0)
/*      */           {
/*  968 */             Report.trace("install", "illegal max length \"" + flagArg + "\"", null);
/*      */           }
/*  973 */           else if (value.length() > length)
/*      */           {
/*  975 */             String msg = LocaleResources.getString("syValueTooLong", null, value, "" + length);
/*      */ 
/*  978 */             this.m_promptUser.outputMessage(msg);
/*  979 */             acceptValue = false;
/*      */           }
/*      */         }
/*      */ 
/*  983 */         if ((acceptValue) && ((flags & 0x20) != 0L))
/*      */         {
/*  985 */           String server = isConfigValueUsed(nameOverride, value);
/*  986 */           if (server != null)
/*      */           {
/*  988 */             String msg = LocaleResources.getString("csInstallDuplicateValue", null, value);
/*      */ 
/*  990 */             this.m_promptUser.outputMessage(msg);
/*  991 */             acceptValue = false;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  997 */     checkAbort(value);
/*  998 */     value = value.trim();
/*      */ 
/* 1000 */     if (value.length() == 0)
/*      */     {
/* 1002 */       value = defaultValue;
/*      */     }
/*      */ 
/* 1005 */     String prefix = nameOverride + "=";
/* 1006 */     int size = this.m_reviewPropList.size();
/* 1007 */     if (size > 0)
/*      */     {
/* 1009 */       String last = (String)this.m_reviewPropList.elementAt(size - 1);
/* 1010 */       if (last.startsWith(prefix))
/*      */       {
/* 1012 */         this.m_reviewPropList.removeElementAt(size - 1);
/*      */       }
/*      */     }
/* 1015 */     String propEntry = prefix + value;
/* 1016 */     if (options != null)
/*      */     {
/* 1018 */       propEntry = prefix + value;
/* 1019 */       for (int i = 0; i < options.length; ++i)
/*      */       {
/* 1021 */         if (!value.equals(options[i][0]))
/*      */           continue;
/* 1023 */         propEntry = prefix + options[i][1];
/* 1024 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1029 */     this.m_reviewPropList.addElement(propEntry);
/* 1030 */     size = this.m_propList.size();
/* 1031 */     while (size-- > 0)
/*      */     {
/* 1033 */       String entry = (String)this.m_propList.elementAt(size);
/* 1034 */       if (entry.startsWith(prefix))
/*      */       {
/* 1036 */         this.m_propList.setElementAt(propEntry, size);
/* 1037 */         break;
/*      */       }
/*      */     }
/* 1040 */     if (size < 0)
/*      */     {
/* 1042 */       this.m_propList.addElement(propEntry);
/*      */     }
/*      */ 
/* 1045 */     this.m_installProps.put(nameOverride, value);
/* 1046 */     return value;
/*      */   }
/*      */ 
/*      */   public void checkAbort(String value)
/*      */     throws ServiceException
/*      */   {
/* 1052 */     if (value != null)
/*      */       return;
/* 1054 */     throw new ServiceException(-64, "!csInstallerAbort");
/*      */   }
/*      */ 
/*      */   public String[][] cloneDoubleArray(String[][] array)
/*      */   {
/* 1060 */     String[][] outer = new String[array.length][];
/* 1061 */     for (int i = 0; i < outer.length; ++i)
/*      */     {
/* 1063 */       String[] inner = new String[array[i].length];
/* 1064 */       outer[i] = inner;
/* 1065 */       for (int j = 0; j < inner.length; ++j)
/*      */       {
/* 1067 */         String obj = array[i][j];
/* 1068 */         inner[j] = obj;
/*      */       }
/*      */     }
/* 1071 */     return outer;
/*      */   }
/*      */ 
/*      */   public void processRequestList(String list) throws ServiceException
/*      */   {
/* 1076 */     processRequestListEx(list, 1);
/*      */   }
/*      */ 
/*      */   public void processRequestListEx(String list, int promptFlags) throws ServiceException
/*      */   {
/* 1081 */     Vector dbConfigList = StringUtils.parseArray(list, '\n', '\n');
/* 1082 */     int length = dbConfigList.size();
/* 1083 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 1085 */       String line = (String)dbConfigList.elementAt(i);
/* 1086 */       line = line.trim();
/* 1087 */       line = ResourceObjectLoader.stripHtml(line);
/*      */ 
/* 1089 */       if (line.length() < 1) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1093 */       Vector v = StringUtils.parseArray(line, ',', '^');
/* 1094 */       if (v.size() == 3)
/*      */       {
/* 1096 */         v.addElement("");
/*      */       }
/* 1098 */       if (v.size() == 4)
/*      */       {
/* 1100 */         v.addElement("");
/*      */       }
/*      */ 
/* 1103 */       if (v.size() != 5)
/*      */       {
/* 1105 */         this.m_promptUser.outputMessage(LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csInstallerSkipBadLine", null, line), null));
/*      */       }
/*      */       else
/*      */       {
/* 1110 */         String key = (String)v.elementAt(0);
/* 1111 */         String prompt = (String)v.elementAt(1);
/* 1112 */         String defValue = (String)v.elementAt(2);
/* 1113 */         defValue = this.m_installer.substituteVariables(defValue, null);
/* 1114 */         if (defValue.equals("null"))
/*      */         {
/* 1116 */           defValue = null;
/*      */         }
/* 1118 */         String condition = (String)v.elementAt(3);
/* 1119 */         if (condition.length() > 0)
/*      */         {
/* 1121 */           condition = this.m_installer.substituteVariables(condition, null);
/* 1122 */           if (!StringUtils.convertToBool(condition, true)) {
/*      */             continue;
/*      */           }
/*      */         }
/*      */ 
/* 1127 */         String flags = (String)v.elementAt(4);
/* 1128 */         if (flags.indexOf("d") >= 0)
/*      */         {
/* 1130 */           File theDefaultPath = new File(defValue);
/* 1131 */           defValue = theDefaultPath.getAbsolutePath();
/* 1132 */           defValue = FileUtils.directorySlashes(defValue);
/* 1133 */           defValue = FileUtils.removeParentDirReferences(defValue);
/*      */         }
/*      */ 
/* 1136 */         promptUser(key, prompt, defValue, (String[][])null, (promptFlags & 0x1) != 0);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public String[][] createPlatformList(DataResultSet drset, String[] fieldList, String supportedList)
/*      */     throws DataException
/*      */   {
/* 1144 */     String[][] list = ResultSetUtils.createStringTable(drset, fieldList);
/* 1145 */     HashMap newList = new HashMap();
/*      */ 
/* 1150 */     ExecutionContext context = new ExecutionContextAdaptor();
/* 1151 */     context.setCachedObject("DataBinder", this.m_binder);
/* 1152 */     context.setCachedObject("SectionInstaller", this);
/* 1153 */     context.setCachedObject("SysInstaller", this.m_installer);
/* 1154 */     PageMerger merger = new PageMerger(this.m_binder, context);
/* 1155 */     Exception theException = null;
/*      */     try
/*      */     {
/* 1158 */       String newSList = merger.evaluateScriptReportError(supportedList);
/* 1159 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1161 */         Report.debug("install", "script: " + supportedList + " -> " + newSList, null);
/*      */       }
/*      */ 
/* 1164 */       supportedList = newSList;
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1168 */       theException = e;
/*      */     }
/*      */     catch (IllegalArgumentException e)
/*      */     {
/* 1172 */       theException = e;
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/* 1176 */       theException = e;
/*      */     }
/* 1178 */     if (theException != null)
/*      */     {
/* 1180 */       Report.trace("install", null, theException);
/*      */     }
/* 1182 */     Vector sList = StringUtils.parseArray(supportedList, ',', '^');
/*      */ 
/* 1185 */     int scriptIndex = -1;
/* 1186 */     for (int i = 0; i < fieldList.length; ++i)
/*      */     {
/* 1188 */       String name = fieldList[i];
/* 1189 */       if (!name.equals("EnabledScript"))
/*      */         continue;
/* 1191 */       scriptIndex = i;
/* 1192 */       break;
/*      */     }
/*      */ 
/* 1196 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/* 1198 */       String id = list[i][0];
/* 1199 */       if (!sList.contains(id)) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1203 */       if (scriptIndex >= 0)
/*      */       {
/* 1205 */         String script = list[i][scriptIndex];
/* 1206 */         if ((script == null) || (script.length() == 0))
/*      */         {
/* 1208 */           script = "true";
/*      */         }
/* 1210 */         String result = null;
/* 1211 */         theException = null;
/*      */         try
/*      */         {
/* 1214 */           result = merger.evaluateScriptReportError(script);
/* 1215 */           if (SystemUtils.m_verbose)
/*      */           {
/* 1217 */             Report.debug("install", "script: " + script + " -> " + result, null);
/*      */           }
/*      */ 
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/* 1223 */           theException = e;
/*      */         }
/*      */         catch (IllegalArgumentException e)
/*      */         {
/* 1227 */           theException = e;
/*      */         }
/*      */         catch (ParseSyntaxException e)
/*      */         {
/* 1231 */           theException = e;
/*      */         }
/* 1233 */         if (theException != null)
/*      */         {
/* 1235 */           Report.trace("install", null, theException);
/*      */         }
/* 1237 */         if (!StringUtils.convertToBool(result, false)) {
/*      */           continue;
/*      */         }
/*      */       }
/*      */ 
/* 1242 */       newList.put(list[i][0], list[i]);
/*      */     }
/*      */ 
/* 1248 */     int len = sList.size();
/* 1249 */     Vector orderedList = new IdcVector();
/* 1250 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1252 */       String id = (String)sList.elementAt(i);
/* 1253 */       Object obj = newList.get(id);
/* 1254 */       if (obj == null) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1258 */       orderedList.addElement(obj);
/*      */     }
/*      */ 
/* 1261 */     int newListSize = orderedList.size();
/* 1262 */     list = new String[newListSize][];
/* 1263 */     while (newListSize-- > 0)
/*      */     {
/* 1265 */       list[newListSize] = ((String[])(String[])orderedList.elementAt(newListSize));
/*      */     }
/*      */ 
/* 1268 */     return list;
/*      */   }
/*      */ 
/*      */   public String defaultMasterServerDir()
/*      */   {
/* 1273 */     String dir = null;
/* 1274 */     if ((this.m_utils != null) && (this.m_utils.isWindowsRegistrySupported()))
/*      */     {
/* 1276 */       dir = this.m_utils.getRegistryValue(this.m_registryBase + "\\LastMasterServerDirectory");
/*      */ 
/* 1278 */       if (dir == null)
/*      */       {
/* 1280 */         String list = this.m_utils.getRegistryValue(this.m_registryBase + "\\ListOfMasterInstances");
/*      */ 
/* 1282 */         if (list == null)
/*      */         {
/* 1284 */           list = this.m_utils.getRegistryValue(this.m_registryBase + "\\ListOfInstalledInstances");
/*      */         }
/*      */ 
/* 1287 */         if (list == null)
/*      */         {
/* 1289 */           list = "";
/*      */         }
/* 1291 */         Vector v = StringUtils.parseArray(list, ',', '^');
/* 1292 */         int size = v.size();
/* 1293 */         if (size > 0)
/*      */         {
/* 1295 */           dir = (String)v.elementAt(size - 1);
/*      */         }
/*      */       }
/*      */     }
/* 1299 */     if ((dir == null) || (dir.length() == 0))
/*      */     {
/* 1301 */       if (this.m_installer.isWindows())
/*      */       {
/* 1303 */         dir = this.m_defaultWindowsName;
/*      */       }
/*      */       else
/*      */       {
/* 1307 */         dir = this.m_defaultUnixName;
/*      */       }
/*      */     }
/* 1310 */     if ((!this.m_isUpdate) && (FileUtils.checkFile(dir, false, false) == 0) && (FileUtils.checkFile(dir + "/config/config.cfg", true, false) == 0))
/*      */     {
/* 1313 */       dir = computeNextDir(dir);
/*      */     }
/* 1315 */     return dir;
/*      */   }
/*      */ 
/*      */   public static String computeNextDir(String dir)
/*      */   {
/* 1320 */     if (dir == null)
/*      */     {
/* 1322 */       return null;
/*      */     }
/*      */     try
/*      */     {
/* 1326 */       int firstDigit = -1;
/* 1327 */       int lastDigit = -1;
/* 1328 */       int firstLetter = -1;
/* 1329 */       if (dir.charAt(dir.length() - 1) == '/')
/*      */       {
/* 1331 */         dir = dir.substring(0, dir.length() - 1);
/*      */       }
/* 1333 */       for (int i = dir.lastIndexOf("/"); i < dir.length(); ++i)
/*      */       {
/* 1335 */         char c = dir.charAt(i);
/* 1336 */         if ((c >= '0') && (c <= '9'))
/*      */         {
/* 1338 */           if (firstDigit != -1)
/*      */             continue;
/* 1340 */           firstDigit = i;
/*      */         }
/*      */         else
/*      */         {
/* 1345 */           if ((firstDigit < 0) || (lastDigit != -1))
/*      */             continue;
/* 1347 */           lastDigit = i - 1;
/* 1348 */           firstLetter = i;
/* 1349 */           break;
/*      */         }
/*      */       }
/*      */ 
/* 1353 */       if (firstDigit == -1)
/*      */       {
/* 1355 */         dir = dir + "2";
/*      */       }
/* 1357 */       else if ((firstDigit >= 0) && (lastDigit < 0))
/*      */       {
/* 1359 */         lastDigit = dir.length();
/* 1360 */         String numberStr = dir.substring(firstDigit, lastDigit);
/* 1361 */         int number = NumberUtils.parseInteger(numberStr, -1);
/* 1362 */         if (number >= 0)
/*      */         {
/* 1364 */           ++number;
/* 1365 */           dir = dir.substring(0, firstDigit) + number;
/*      */         }
/*      */       }
/* 1368 */       else if ((firstDigit >= 0) && (lastDigit > 0) && (firstLetter == dir.length() - 1))
/*      */       {
/* 1370 */         char letter = dir.charAt(firstLetter);
/* 1371 */         letter = (char)(letter + '\001');
/* 1372 */         dir = dir.substring(0, firstLetter) + letter;
/*      */       }
/*      */       else
/*      */       {
/* 1376 */         dir = dir + ".new";
/*      */       }
/* 1378 */       return dir;
/*      */     }
/*      */     catch (ArrayIndexOutOfBoundsException e)
/*      */     {
/* 1382 */       if (SystemUtils.m_isDevelopmentEnvironment)
/*      */       {
/* 1384 */         throw e;
/*      */       }
/* 1386 */       Report.trace("install", null, e);
/* 1387 */     }return dir;
/*      */   }
/*      */ 
/*      */   public String defaultWebBrowserPath()
/*      */   {
/* 1393 */     List searchDirs = new ArrayList();
/* 1394 */     if (this.m_utils != null)
/*      */     {
/* 1396 */       String envPath = this.m_utils.getEnv("PATH");
/* 1397 */       if (envPath != null)
/*      */       {
/* 1399 */         StringUtils.appendListFromSequence(searchDirs, envPath, 0, envPath.length(), this.m_installer.getInstallerTableValue("PlatformConfigTable", this.m_installer.m_platform, "PathSeperator").charAt(0), '^', 64);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1406 */     String extraPath = this.m_installer.getInstallerTableValue("PlatformConfigTable", this.m_installer.m_platform, "SupplimentalSearchPath");
/*      */ 
/* 1408 */     StringUtils.appendListFromSequenceSimple(searchDirs, extraPath);
/*      */ 
/* 1410 */     List browserExecutables = new ArrayList();
/* 1411 */     String exeSuffix = this.m_installer.getInstallerTableValue("PlatformConfigTable", this.m_installer.m_platform, "ExeSuffix");
/*      */ 
/* 1413 */     String browserExecutableStrings = this.m_installer.getInstallerTableValue("PlatformConfigTable", this.m_installer.m_platform, "WebBrowserList");
/*      */ 
/* 1415 */     StringUtils.appendListFromSequenceSimple(browserExecutables, browserExecutableStrings);
/*      */ 
/* 1417 */     for (Iterator i$ = searchDirs.iterator(); i$.hasNext(); ) { dir = (String)i$.next();
/*      */ 
/* 1419 */       int index = dir.indexOf("/");
/* 1420 */       if (index == -1)
/*      */       {
/* 1422 */         index = dir.length();
/*      */       }
/* 1424 */       if ((dir.startsWith("$")) && (this.m_utils != null))
/*      */       {
/* 1426 */         String key = dir.substring(1, index);
/* 1427 */         String suffix = dir.substring(index);
/* 1428 */         String keyValue = this.m_utils.getEnv(key);
/* 1429 */         dir = keyValue + suffix;
/*      */       }
/* 1431 */       for (String browser : browserExecutables)
/*      */       {
/* 1433 */         String path = dir + "/" + browser + exeSuffix;
/* 1434 */         if (FileUtils.checkFile(path, true, false) == 0)
/*      */         {
/* 1436 */           return path;
/*      */         }
/*      */       } }
/*      */ 
/*      */     String dir;
/* 1441 */     return "";
/*      */   }
/*      */ 
/*      */   public String promptForDirectory(String entry, String label, String defValue)
/*      */     throws ServiceException
/*      */   {
/* 1447 */     return promptForDirectoryWithProps(entry, label, defValue, new Properties());
/*      */   }
/*      */ 
/*      */   public String promptForDirectoryWithProps(String entry, String label, String defValue, Properties answerProps)
/*      */     throws ServiceException
/*      */   {
/* 1454 */     String dir = null;
/*      */     while (true)
/*      */     {
/* 1457 */       dir = promptUserEx(entry, label, defValue, (String[][])null, this.m_isUpdate, answerProps);
/* 1458 */       dir = FileUtils.directorySlashes(dir);
/* 1459 */       File fDir = new File(dir);
/* 1460 */       if (fDir.exists())
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/* 1464 */       Properties props = new Properties();
/* 1465 */       String create = promptUserEx("CreateDir", "!csInstallerCreateDirPrompt", "true", this.m_yesNoOptions, false, props);
/*      */ 
/* 1467 */       removeProp("CreateDir");
/* 1468 */       if (StringUtils.convertToBool(create, false));
/*      */       try
/*      */       {
/* 1472 */         FileUtils.checkOrCreateDirectory(dir, 99);
/* 1473 */         FileUtils.testFileSystem(dir);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1478 */         if (props.getProperty("UserInteractionRequired") == null)
/*      */         {
/* 1482 */           throw e;
/*      */         }
/* 1484 */         this.m_promptUser.outputMessage(LocaleResources.localizeMessage(e.getMessage(), null));
/*      */ 
/* 1488 */         removeProp(entry);
/*      */       }
/*      */     }
/* 1491 */     return dir;
/*      */   }
/*      */ 
/*      */   public String promptForDirectoryEx(String entry, String entryNameOverride, String label, String defValue)
/*      */     throws ServiceException
/*      */   {
/* 1498 */     String dir = null;
/*      */     while (true)
/*      */     {
/* 1501 */       dir = promptUserEx(entry, entryNameOverride, label, defValue, (String[][])null, this.m_isUpdate);
/* 1502 */       dir = FileUtils.directorySlashes(dir);
/* 1503 */       File fDir = new File(dir);
/* 1504 */       if (fDir.exists())
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/* 1508 */       String create = promptUser("CreateDir", "!csInstallerCreateDirPrompt", "true", this.m_yesNoOptions, false);
/*      */ 
/* 1510 */       removeProp("CreateDir");
/* 1511 */       if (StringUtils.convertToBool(create, false));
/*      */       try
/*      */       {
/* 1515 */         FileUtils.checkOrCreateDirectory(dir, 99);
/* 1516 */         FileUtils.testFileSystem(dir);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1521 */         this.m_promptUser.outputMessage(LocaleResources.localizeMessage(e.getMessage(), null));
/*      */ 
/* 1525 */         removeProp(entry);
/*      */       }
/*      */     }
/* 1528 */     return dir;
/*      */   }
/*      */ 
/*      */   public void removeProp(String key)
/*      */   {
/* 1533 */     this.m_installProps.remove(key);
/* 1534 */     key = key + "=";
/* 1535 */     int size = this.m_propList.size();
/* 1536 */     while (size-- > 0)
/*      */     {
/* 1538 */       String entry = (String)this.m_propList.elementAt(size);
/* 1539 */       if (entry.startsWith(key))
/*      */       {
/* 1541 */         this.m_propList.removeElementAt(size);
/*      */       }
/*      */     }
/* 1544 */     size = this.m_reviewPropList.size();
/* 1545 */     while (size-- > 0)
/*      */     {
/* 1547 */       String entry = (String)this.m_reviewPropList.elementAt(size);
/* 1548 */       if (entry.startsWith(key))
/*      */       {
/* 1550 */         this.m_reviewPropList.removeElementAt(size);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setProp(String key, String value)
/*      */   {
/* 1558 */     String prefix = key + "=";
/* 1559 */     String propEntry = prefix + " " + value;
/* 1560 */     int size = this.m_propList.size();
/* 1561 */     while (size-- > 0)
/*      */     {
/* 1563 */       String entry = (String)this.m_propList.elementAt(size);
/* 1564 */       if (entry.startsWith(prefix))
/*      */       {
/* 1566 */         this.m_propList.setElementAt(propEntry, size);
/* 1567 */         break;
/*      */       }
/*      */     }
/* 1570 */     if (size < 0)
/*      */     {
/* 1572 */       this.m_propList.addElement(propEntry);
/*      */     }
/*      */ 
/* 1575 */     this.m_installProps.put(key, value);
/*      */   }
/*      */ 
/*      */   protected void promptForJvmInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1583 */     String defaultOption = null;
/* 1584 */     DataResultSet drset = (DataResultSet)this.m_installer.m_binder.getResultSet("JvmTable");
/* 1585 */     if (drset == null)
/*      */     {
/* 1587 */       String msg = LocaleUtils.encodeMessage("csResultSetMissing", null, "JvmTable");
/*      */ 
/* 1589 */       msg = LocaleUtils.encodeMessage("csUnableToInstallJvm", msg);
/* 1590 */       throw new DataException(msg);
/*      */     }
/* 1592 */     Vector list = new IdcVector();
/* 1593 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1595 */       Properties props = drset.getCurrentRowProps();
/* 1596 */       String p = props.getProperty("Platform");
/* 1597 */       if (!p.equalsIgnoreCase(this.m_platform))
/*      */         continue;
/* 1599 */       String[] item = new String[2];
/* 1600 */       String mediaJDK = this.m_installer.computeDestinationEx(props.getProperty("SourceDir"), false);
/*      */ 
/* 1602 */       String imageJDK = this.m_installer.computeDestinationEx(props.getProperty("DestinationDir"), false);
/*      */ 
/* 1604 */       if ((FileUtils.checkFile(mediaJDK, false, false) != 0) && (FileUtils.checkFile(imageJDK, false, false) != 0))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1609 */       item[0] = (props.getProperty("Platform") + "-" + props.getProperty("Vendor") + "-" + props.getProperty("Version"));
/*      */ 
/* 1612 */       String key = "csInstallJvmDesc_" + props.getProperty("Vendor") + "_" + props.getProperty("Version");
/*      */ 
/* 1614 */       item[1] = LocaleResources.getString(key.replace('.', '_'), null);
/* 1615 */       list.addElement(item);
/* 1616 */       if (defaultOption != null)
/*      */         continue;
/* 1618 */       defaultOption = item[0];
/*      */     }
/*      */ 
/* 1623 */     if ((this.m_isUpdate) && (!this.m_installer.isOlderVersion("6.1.0")))
/*      */     {
/* 1625 */       String[] customJvm = new String[2];
/* 1626 */       customJvm[0] = "current";
/* 1627 */       customJvm[1] = LocaleResources.getString("csInstallCurrentJvmChoice", null);
/* 1628 */       list.addElement(customJvm);
/*      */     }
/* 1630 */     String[] customJvm = new String[2];
/* 1631 */     customJvm[0] = "custom";
/* 1632 */     customJvm[1] = LocaleResources.getString("csInstallCustomJvmChoice", null);
/* 1633 */     list.addElement(customJvm);
/*      */ 
/* 1635 */     String[][] jvmOptions = new String[list.size()][];
/* 1636 */     for (int i = 0; i < jvmOptions.length; ++i)
/*      */     {
/* 1638 */       jvmOptions[i] = ((String[])(String[])list.elementAt(i));
/*      */     }
/*      */ 
/*      */     while (true)
/*      */     {
/* 1643 */       String installJvm = promptUser("InstallJvm", "!csInstallJvmPrompt", defaultOption, jvmOptions, false);
/*      */ 
/* 1645 */       String path = null;
/* 1646 */       if (installJvm.equals("custom"))
/*      */       {
/* 1648 */         path = promptUser("JvmPath", "!csInstallJvmPath", "java", (String[][])null, false);
/*      */       }
/*      */       else {
/* 1651 */         if (installJvm.equals("current"))
/*      */         {
/* 1653 */           String msg = LocaleResources.getString("csInstallUnableToCheckCurrentJvm", null);
/*      */ 
/* 1655 */           this.m_promptUser.outputMessage(msg);
/* 1656 */           return;
/*      */         }
/*      */ 
/* 1660 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*      */         {
/* 1662 */           Properties p = drset.getCurrentRowProps();
/* 1663 */           String key = p.getProperty("Platform") + "-" + p.getProperty("Vendor") + "-" + p.getProperty("Version");
/*      */ 
/* 1666 */           if (!key.equals(installJvm))
/*      */             continue;
/* 1668 */           path = p.getProperty("SourceDir") + "/" + p.getProperty("JvmPath");
/*      */ 
/* 1670 */           path = this.m_installer.computeDestinationEx(path, false);
/* 1671 */           break;
/*      */         }
/*      */ 
/* 1674 */         if (path == null)
/*      */         {
/* 1681 */           this.m_promptUser.outputMessage("Unable to find Jvm info for " + installJvm);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1688 */       Properties props = loadJvmProperties(path, null);
/* 1689 */       String success = props.getProperty("Success");
/* 1690 */       if (StringUtils.convertToBool(success, false))
/*      */       {
/* 1692 */         String msg = LocaleUtils.encodeMessage("csInstallJvmVersion", null, props.getProperty("java.version"));
/*      */ 
/* 1694 */         this.m_installer.m_installLog.notice(msg);
/* 1695 */         this.m_installProps.put("InstallerJvmPath", path);
/* 1696 */         msg = LocaleResources.localizeMessage(msg, null);
/* 1697 */         this.m_promptUser.outputMessage(msg);
/* 1698 */         return;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setJvmPath()
/*      */     throws ServiceException
/*      */   {
/* 1709 */     String jvm = this.m_installer.getConfigValue("JAVA_EXE");
/* 1710 */     if (jvm == null)
/*      */     {
/* 1712 */       String list = this.m_installer.getConfigValue("JAVA_EXE_LIST");
/* 1713 */       if (list != null)
/*      */       {
/* 1715 */         Vector jvmList = StringUtils.parseArray(list, ' ', ' ');
/* 1716 */         for (int i = 0; i < jvmList.size(); ++i)
/*      */         {
/* 1718 */           jvm = (String)jvmList.elementAt(i);
/* 1719 */           if (FileUtils.checkFile(jvm, true, false) == 0) {
/*      */             break;
/*      */           }
/*      */ 
/* 1723 */           jvm = null;
/*      */         }
/*      */       }
/* 1726 */       if (jvm != null)
/*      */       {
/* 1728 */         Report.trace("install", "upgrade will use JVM specified by JAVA_EXE_LIST: \"" + jvm + "\"", null);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1734 */       Report.trace("install", "upgrade will use JVM specified by JAVA_EXE: \"" + jvm + "\"", null);
/*      */     }
/*      */ 
/* 1737 */     if (jvm == null)
/*      */     {
/* 1739 */       jvm = getDefaultJvmPath();
/* 1740 */       if (jvm != null)
/*      */       {
/* 1742 */         jvm = this.m_installer.computeDestinationEx(jvm, false);
/* 1743 */         Report.trace("install", "upgrade will use default JVM", null);
/*      */       }
/*      */     }
/*      */ 
/* 1747 */     if (jvm == null)
/*      */     {
/* 1749 */       jvm = "java";
/* 1750 */       Report.trace("install", "unable to find a JVM", null);
/*      */     }
/*      */     else
/*      */     {
/* 1754 */       setProp("InstallerJvmPath", jvm);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getHostName()
/*      */   {
/* 1760 */     String hostname = "ucm";
/* 1761 */     if (this.m_utils != null)
/*      */     {
/*      */       try
/*      */       {
/* 1765 */         hostname = this.m_utils.getComputerName();
/* 1766 */         if ((hostname != null) && (hostname.length() > 0))
/*      */         {
/* 1768 */           return hostname;
/*      */         }
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 1773 */         Report.trace("install", null, t);
/*      */       }
/*      */     }
/* 1776 */     String[] cmd = { "hostname" };
/* 1777 */     Vector results = new IdcVector();
/*      */     try
/*      */     {
/* 1780 */       int rc = this.m_installer.runCriticalCommand(cmd, results, true);
/* 1781 */       if ((rc == 0) && (results.size() == 1))
/*      */       {
/* 1783 */         String tmp = (String)results.elementAt(0);
/* 1784 */         tmp = tmp.trim();
/* 1785 */         if (tmp.indexOf(" ") == -1)
/*      */         {
/* 1787 */           hostname = tmp;
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 1793 */       Report.trace("install", "unable to get hostname", ignore);
/*      */     }
/*      */ 
/* 1796 */     return hostname;
/*      */   }
/*      */ 
/*      */   public String getDefaultJvmPath()
/*      */   {
/* 1801 */     Properties p = this.m_installer.getInstallerTable("JvmTable", this.m_currentPlatform);
/* 1802 */     if (p == null)
/*      */     {
/* 1804 */       return null;
/*      */     }
/* 1806 */     String path = p.getProperty("SourceDir") + "/" + p.getProperty("JvmPath");
/*      */ 
/* 1808 */     return path;
/*      */   }
/*      */ 
/*      */   public boolean checkSystemEncoding(IdcLocale locale)
/*      */   {
/* 1813 */     boolean failed = false;
/* 1814 */     ExecutionContext context = new ExecutionContextAdaptor();
/* 1815 */     context.setCachedObject("UserLocale", locale);
/* 1816 */     for (int i = 0; i < this.m_encodingTestKeys.length; ++i)
/*      */     {
/* 1818 */       String string = LocaleResources.getString(this.m_encodingTestKeys[i], context);
/*      */ 
/* 1820 */       if (LocaleUtils.testStringEncoding(string, null))
/*      */         continue;
/* 1822 */       failed = true;
/* 1823 */       break;
/*      */     }
/*      */ 
/* 1827 */     return !failed;
/*      */   }
/*      */ 
/*      */   public Properties loadJvmProperties(String path, String[] props)
/*      */     throws ServiceException
/*      */   {
/* 1833 */     Properties properties = new Properties();
/* 1834 */     if (props == null)
/*      */     {
/* 1836 */       props = new String[] { "java.version" };
/*      */     }
/* 1838 */     Vector args = new IdcVector();
/* 1839 */     args.addElement("--report-properties");
/* 1840 */     for (int i = 0; i < props.length; ++i)
/*      */     {
/* 1842 */       args.addElement(props[i]);
/*      */     }
/*      */ 
/* 1845 */     boolean quiet = this.m_installer.m_installLog.m_quiet;
/* 1846 */     this.m_installer.m_installLog.m_quiet = true;
/*      */     try
/*      */     {
/* 1849 */       Vector results = new IdcVector();
/* 1850 */       int rc = this.m_installer.runInstall(this.m_installProps, null, args, path, results, null);
/*      */ 
/* 1852 */       if (results.size() == 0)
/*      */       {
/* 1858 */         Report.trace("install", "Recheck JVM version", null);
/* 1859 */         rc = this.m_installer.runInstall(this.m_installProps, null, args, path, results, null);
/*      */       }
/*      */ 
/* 1862 */       boolean success = false;
/* 1863 */       for (int i = 0; i < results.size(); ++i)
/*      */       {
/* 1865 */         String result = (String)results.elementAt(i);
/* 1866 */         Report.trace("install", "result line " + result, null);
/* 1867 */         if (!result.startsWith("java.version="))
/*      */           continue;
/* 1869 */         success = true;
/*      */       }
/*      */ 
/* 1872 */       if (!success)
/*      */       {
/* 1874 */         Report.trace("install", "JVM version check failed", null);
/* 1875 */         rc = -1;
/*      */       }
/* 1877 */       if (rc != 0)
/*      */       {
/* 1879 */         String msg = LocaleResources.getString("csInstallJvmVersionCheckFailed", null);
/* 1880 */         this.m_promptUser.outputMessage(msg);
/* 1881 */         properties.put("Success", "0");
/*      */       }
/*      */       else
/*      */       {
/* 1885 */         properties.put("Success", "1");
/* 1886 */         for (int i = 0; i < results.size(); ++i)
/*      */         {
/* 1888 */           String line = (String)results.elementAt(i);
/* 1889 */           int index = line.indexOf("=");
/* 1890 */           if (index <= 0)
/*      */             continue;
/* 1892 */           properties.put(line.substring(0, index), line.substring(index + 1));
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/* 1900 */       this.m_installer.m_installLog.m_quiet = quiet;
/*      */     }
/*      */ 
/* 1903 */     return properties;
/*      */   }
/*      */ 
/*      */   public String isConfigValueUsed(String key, String userValue)
/*      */   {
/* 1913 */     Properties entryInfo = this.m_installer.getInstallerTable("ConfigEntries", key);
/*      */ 
/* 1915 */     String server = null;
/* 1916 */     if (entryInfo == null)
/*      */     {
/* 1919 */       Report.trace("install", "isConfigValueUsed() called on unknown key \"" + key + "\"", null);
/*      */ 
/* 1922 */       return null;
/*      */     }
/* 1924 */     long flags = this.m_installer.parseFlags(entryInfo.getProperty("Flags"), null);
/* 1925 */     if ((flags & 0x20) == 0L)
/*      */     {
/* 1927 */       return null;
/*      */     }
/* 1929 */     userValue = this.m_installer.handleFlags(userValue, flags);
/*      */ 
/* 1931 */     if (userValue == null)
/*      */     {
/* 1933 */       Report.trace("install", "isConfigValueUsed() called on a key with null value \"" + key + "\"", null);
/*      */ 
/* 1936 */       return null;
/*      */     }
/* 1938 */     String lookupKey = key + "-" + userValue;
/* 1939 */     server = this.m_usedConfigurationValues.getProperty(lookupKey);
/*      */ 
/* 1941 */     return server;
/*      */   }
/*      */ 
/*      */   public String defaultConfigValueFromOtherServers(String key)
/*      */   {
/* 1950 */     Properties entryInfo = this.m_installer.getInstallerTable("ConfigEntries", key);
/*      */ 
/* 1952 */     if (entryInfo == null)
/*      */     {
/* 1955 */       Report.trace("install", "defaultConfigValueFromOtherServers() called on unknown key \"" + key + "\"", null);
/*      */ 
/* 1958 */       return null;
/*      */     }
/* 1960 */     long flags = this.m_installer.parseFlags(entryInfo.getProperty("Flags"), null);
/* 1961 */     if ((flags & 0x20) != 0L)
/*      */     {
/* 1963 */       return null;
/*      */     }
/*      */ 
/* 1966 */     String value = this.m_possibleDefaultValues.getProperty(key);
/* 1967 */     if (value != null)
/*      */     {
/* 1969 */       value = this.m_installer.handleFlags(value, flags);
/*      */     }
/* 1971 */     return value;
/*      */   }
/*      */ 
/*      */   public void queryInstalledServers(String idcDir) throws ServiceException
/*      */   {
/* 1976 */     String sourceDirectory = this.m_installer.computeDestinationEx("${SourceDirectory}/", false);
/*      */ 
/* 1978 */     Vector args = new IdcVector();
/* 1979 */     args.addElement("-q");
/* 1980 */     args.addElement("--set-IntradocDir=" + idcDir);
/* 1981 */     args.addElement("--set-InstallConfiguration=QueryConfiguration");
/* 1982 */     args.addElement("--set-SourceDirectory=" + sourceDirectory);
/* 1983 */     args.addElement("--set-SystemTimeZone=UTC");
/* 1984 */     args.addElement("--set-IdcProductName=" + this.m_installProps.getProperty("IdcProductName"));
/* 1985 */     this.m_installer.runInstall(this.m_installProps, null, args, null, null, null);
/*      */     try
/*      */     {
/* 1988 */       BufferedReader reader = FileUtils.openDataReader(this.m_installer.computeDestination("install/idc_info.txt"));
/*      */ 
/* 1991 */       while ((line = reader.readLine()) != null)
/*      */       {
/*      */         String line;
/* 1993 */         int index = line.indexOf("=");
/* 1994 */         if (index <= 0) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1998 */         String key = line.substring(0, index);
/* 1999 */         String value = line.substring(index + 1);
/* 2000 */         index = key.indexOf("/");
/* 2001 */         if (index <= 0) {
/*      */           continue;
/*      */         }
/*      */ 
/* 2005 */         String server = key.substring(0, index);
/* 2006 */         key = key.substring(index + 1);
/* 2007 */         Properties entryInfo = this.m_installer.getInstallerTable("ConfigEntries", key);
/*      */ 
/* 2009 */         if (entryInfo != null)
/*      */         {
/* 2011 */           long flags = this.m_installer.parseFlags(entryInfo.getProperty("Flags"), null);
/*      */ 
/* 2013 */           value = this.m_installer.handleFlags(value, flags);
/*      */         }
/*      */ 
/* 2016 */         String tmp = key + "-" + value;
/* 2017 */         if (this.m_possibleDefaultValues.getProperty(key) == null)
/*      */         {
/* 2019 */           this.m_possibleDefaultValues.put(key, value);
/*      */         }
/* 2021 */         tmp = (String)this.m_usedConfigurationValues.put(tmp, server);
/* 2022 */         if (key != null)
/*      */         {
/* 2024 */           Report.trace("install", "the configuration value \"" + key + "\" is used by multiple servers", null);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 2031 */       String msg = LocaleUtils.encodeMessage("csInstallerUnableToScanServers", ignore.getMessage());
/*      */ 
/* 2034 */       msg = LocaleResources.localizeMessage(msg, null);
/* 2035 */       this.m_promptUser.outputMessage(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Vector processInstallationPath(String path, boolean updateProps, Properties defaultProps)
/*      */     throws ServiceException
/*      */   {
/* 2043 */     if (SystemUtils.m_verbose)
/*      */     {
/* 2045 */       Report.debug("install", "processing installation path " + path, null);
/*      */     }
/* 2047 */     String className = this.m_installProps.getProperty("UserInteractionClass");
/* 2048 */     if (className == null)
/*      */     {
/* 2050 */       className = "DefaultUserInteraction";
/*      */     }
/* 2052 */     if (className.indexOf(".") == -1)
/*      */     {
/* 2054 */       className = "intradoc.apputilities.installer." + className;
/*      */     }
/* 2056 */     UserInteraction interaction = (UserInteraction)ComponentClassFactory.createClassInstance(className, className, null);
/*      */ 
/* 2059 */     interaction.init(this.m_installer, this.m_promptUser);
/* 2060 */     Vector results = interaction.processInstallationPath(path, defaultProps);
/* 2061 */     if (updateProps)
/*      */     {
/* 2063 */       for (int i = 0; i < results.size(); ++i)
/*      */       {
/* 2065 */         SettingInfo settingInfo = (SettingInfo)results.elementAt(i);
/* 2066 */         String key = settingInfo.m_name;
/* 2067 */         String value = settingInfo.m_value;
/* 2068 */         if (value == null)
/*      */         {
/* 2070 */           Report.trace("install", "skipping setting " + settingInfo.m_name + " because m_value is null.", null);
/*      */         }
/*      */         else
/*      */         {
/* 2074 */           Properties settingProps = this.m_installer.getInstallerTable("InstallPrompts", key);
/*      */ 
/* 2076 */           String summaryLabel = settingProps.getProperty("SummaryLabel");
/* 2077 */           if ((summaryLabel == null) || (summaryLabel.length() == 0))
/*      */           {
/* 2079 */             summaryLabel = settingProps.getProperty("PromptLabel");
/*      */           }
/* 2081 */           this.m_keyLabelMap.put(key, summaryLabel);
/* 2082 */           long flags = this.m_installer.getConfigFlags(key);
/* 2083 */           boolean isPassword = (flags & 0x80) != 0L;
/* 2084 */           summaryLabel = interaction.getLocalizedText(summaryLabel);
/* 2085 */           if ((settingInfo.m_appliedFlags.indexOf("clearonskip") == -1) && (!isPassword))
/*      */           {
/* 2088 */             this.m_reviewPropList.addElement(key + "=" + value);
/*      */           }
/* 2090 */           this.m_propList.addElement(key + "=" + value);
/* 2091 */           this.m_installProps.put(key, value);
/*      */         }
/*      */       }
/*      */     }
/* 2094 */     return results;
/*      */   }
/*      */ 
/*      */   protected static String trimSlashes(String s)
/*      */   {
/* 2099 */     if (s.startsWith("/"))
/*      */     {
/* 2101 */       s = s.substring(1);
/*      */     }
/* 2103 */     if (s.endsWith("/"))
/*      */     {
/* 2105 */       s = s.substring(0, s.length() - 1);
/*      */     }
/* 2107 */     return s;
/*      */   }
/*      */ 
/*      */   protected static String localizePrompt(String key)
/*      */   {
/* 2112 */     String prompt = "";
/* 2113 */     if ((key != null) && (key.length() > 0))
/*      */     {
/* 2115 */       String tmpKey = "!dr" + key.substring(3);
/* 2116 */       if ((m_isRefineryInstall) && (key.startsWith("!cs")))
/*      */       {
/* 2118 */         prompt = LocaleResources.localizeMessage(tmpKey, null);
/*      */       }
/* 2120 */       if ((prompt == null) || (prompt.length() == 0) || (prompt.equalsIgnoreCase(tmpKey.substring(1))))
/*      */       {
/* 2123 */         prompt = LocaleResources.localizeMessage(key, null);
/*      */       }
/*      */     }
/* 2126 */     return prompt;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2131 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97206 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.InteractiveInstaller
 * JD-Core Version:    0.5.4
 */