/*      */ package intradoc.resource;
/*      */ 
/*      */ import intradoc.common.AppObjectRepository;
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.Help;
/*      */ import intradoc.common.IdcBreakpointManager;
/*      */ import intradoc.common.IdcBreakpoints;
/*      */ import intradoc.common.IdcLocaleString;
/*      */ import intradoc.common.IdcLocalizationStrings;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IntervalData;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseLocationInfo;
/*      */ import intradoc.common.ParseOutput;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Parser;
/*      */ import intradoc.common.PropertiesTreeNode;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ResourceObject;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StreamEventHandler;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcConfigFile;
/*      */ import intradoc.data.QueryUtils;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.SimpleParameters;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.shared.LocaleLoader;
/*      */ import intradoc.shared.ProgressState;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcPerfectHash;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.CharArrayReader;
/*      */ import java.io.CharArrayWriter;
/*      */ import java.io.CharConversionException;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.FileReader;
/*      */ import java.io.IOException;
/*      */ import java.io.PrintStream;
/*      */ import java.io.Reader;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.LinkedList;
/*      */ import java.util.List;
/*      */ import java.util.Locale;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ResourceLoader
/*      */ {
/*   37 */   public static Hashtable m_reportedMessages = new Hashtable();
/*   38 */   public static String[] m_localeEnvMergeKeys = null;
/*   39 */   public static boolean m_checkForAsciiEncoding = false;
/*   40 */   public static boolean m_helpFilesInWeblayout = false;
/*      */ 
/*   46 */   public static boolean m_loadSystemStringsOnly = false;
/*      */ 
/*   48 */   public static int F_IS_SYSTEM = 1;
/*   49 */   public static int F_IS_TOLERANT = 2;
/*   50 */   public static int F_IS_STRICT = 32;
/*   51 */   public static int F_LOAD_BASE_STRINGS = 4;
/*   52 */   public static int F_PREPEND_RESOURCE = 8;
/*   53 */   public static int F_FORCE_LOAD = 16;
/*      */ 
/*   55 */   public static final String[] XLIFF_NODE_NAMES = { "body", "trans-unit", "id", "source", "target", "file", "source-language", "target-language" };
/*      */ 
/*   66 */   public static int F_IS_XML = 256;
/*   67 */   public static int F_IS_HDA = 512;
/*   68 */   public static int F_IS_HTML = 1024;
/*      */ 
/*      */   public static void loadResourceFile(ResourceContainer res, String filePath)
/*      */     throws ServiceException
/*      */   {
/*   73 */     loadResourceFileEx(res, filePath, filePath, true, null, 0L, null);
/*      */   }
/*      */ 
/*      */   public static void loadResourceFileEx(ResourceContainer res, String key, String filePath, boolean registerContainer, Object auxInfo, long startTime, String lang)
/*      */     throws ServiceException
/*      */   {
/*   80 */     loadResourceFileWithFlags(res, key, filePath, registerContainer, auxInfo, startTime, lang, 0);
/*      */   }
/*      */ 
/*      */   public static void loadResourceFileWithFlags(ResourceContainer res, String key, String filePath, boolean registerContainer, Object auxInfo, long startTime, String lang, int flags)
/*      */     throws ServiceException
/*      */   {
/*   87 */     BufferedReader reader = null;
/*   88 */     File file = null;
/*   89 */     boolean success = false;
/*   90 */     boolean doLoad = true;
/*      */     try
/*      */     {
/*   93 */       file = FileUtilsCfgBuilder.getCfgFile(filePath, null, false);
/*   94 */       long ts = file.lastModified();
/*   95 */       IdcBreakpoints bp = adjustBreakpoints(filePath, file);
/*   96 */       int encodingFlags = (filePath.endsWith(".xlf")) ? F_IS_XML : F_IS_HTML;
/*      */ 
/*   98 */       if ((res.m_handler != null) && (res.m_handler instanceof IdcLocalizationStrings))
/*      */       {
/*  101 */         IdcLocalizationStrings strings = (IdcLocalizationStrings)res.m_handler;
/*  102 */         Map info = strings.getFileLoadInfo(filePath);
/*  103 */         if (info != null)
/*      */         {
/*  105 */           boolean isStringsOnly = StringUtils.convertToBool((String)info.get("onlyStrings"), false);
/*  106 */           long indexTS = NumberUtils.parseLong((String)info.get("ts"), 0L);
/*  107 */           if ((ts == indexTS) && (isStringsOnly))
/*      */           {
/*  109 */             doLoad = false;
/*      */           }
/*      */         }
/*      */       }
/*  113 */       if (doLoad)
/*      */       {
/*  117 */         reader = openResourceReader(file, null, encodingFlags);
/*      */ 
/*  119 */         if ((encodingFlags & F_IS_XML) != 0)
/*      */         {
/*  121 */           res.parseAndAddXmlResourcesEx(reader, filePath, true);
/*  122 */           convertXMLToStringResource(res, lang);
/*      */         }
/*      */         else
/*      */         {
/*  126 */           int loadFlags = 0;
/*  127 */           boolean unencode = res.m_unencodeResourceStrings;
/*  128 */           if (SharedObjects.getEnvValueAsBoolean("LoadStringsForTranslation", false))
/*      */           {
/*  130 */             loadFlags |= 2;
/*  131 */             res.m_unencodeResourceStrings = false;
/*      */           }
/*      */           try
/*      */           {
/*  135 */             res.parseAndAddResourcesWithFlags(reader, filePath, lang, bp, loadFlags);
/*      */           }
/*      */           finally
/*      */           {
/*  139 */             res.m_unencodeResourceStrings = unencode;
/*      */           }
/*      */         }
/*      */ 
/*  143 */         if (res.m_handler != null)
/*      */         {
/*  145 */           Map m = new HashMap();
/*  146 */           m.put("file", filePath);
/*  147 */           m.put("ts", "" + ts);
/*  148 */           res.m_handler.handleStreamEvent("fileLoaded", m, res);
/*      */         }
/*      */       }
/*  151 */       success = true;
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       ResourceCacheInfo info;
/*      */       boolean cacheResState;
/*      */       IdcMessage msg;
/*  165 */       if ((!doLoad) || (reader != null))
/*      */       {
/*  167 */         ResourceCacheInfo info = null;
/*  168 */         if (registerContainer)
/*      */         {
/*  170 */           boolean cacheResState = res.m_dynamicHtmlLoaded;
/*  171 */           if (cacheResState)
/*      */           {
/*  173 */             if ((flags & F_PREPEND_RESOURCE) != 0)
/*      */             {
/*  175 */               info = ResourceCacheState.prependResourceInfo(key, filePath);
/*      */             }
/*      */             else
/*      */             {
/*  179 */               info = ResourceCacheState.addResourceInfo(key, filePath);
/*      */             }
/*  181 */             info.m_languageId = lang;
/*  182 */             info.m_hasDynamicResource = res.m_dynamicResourceLoaded;
/*  183 */             info.m_hasDynamicString = res.m_dynamicStringLoaded;
/*  184 */             info.m_resourceList = res.m_resourceList;
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  189 */           info = new ResourceCacheInfo(key, "FileResource", filePath);
/*      */         }
/*      */ 
/*  192 */         if ((info != null) && (success))
/*      */         {
/*  194 */           info.m_lastLoaded = file.lastModified();
/*  195 */           info.m_size = file.length();
/*  196 */           info.m_resourceObj = res;
/*  197 */           if (auxInfo != null)
/*      */           {
/*  199 */             info.m_size += 2000L;
/*      */           }
/*  201 */           info.m_associatedInfo = auxInfo;
/*  202 */           if (!registerContainer)
/*      */           {
/*  204 */             ResourceCacheState.addTimedTemporaryCache(key, info, startTime);
/*      */           }
/*      */         }
/*  207 */         FileUtils.closeReader(reader);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void convertXMLToStringResource(ResourceContainer res, String lang)
/*      */     throws ServiceException
/*      */   {
/*  215 */     String nodeName = XLIFF_NODE_NAMES[3];
/*      */ 
/*  217 */     List nodeList = res.getXmlNodes();
/*  218 */     if (nodeList.isEmpty())
/*      */     {
/*  220 */       return;
/*      */     }
/*      */ 
/*  223 */     PropertiesTreeNode rootNode = (PropertiesTreeNode)nodeList.get(0);
/*      */ 
/*  225 */     LinkedList callStack = new LinkedList();
/*  226 */     callStack.add(rootNode);
/*  227 */     while (!callStack.isEmpty())
/*      */     {
/*  229 */       rootNode = (PropertiesTreeNode)callStack.removeFirst();
/*      */ 
/*  231 */       if (rootNode.m_name.equals(XLIFF_NODE_NAMES[5]))
/*      */       {
/*  233 */         String source = (String)rootNode.m_properties.get(XLIFF_NODE_NAMES[6]);
/*  234 */         String target = (String)rootNode.m_properties.get(XLIFF_NODE_NAMES[7]);
/*      */ 
/*  237 */         if ((target != null) && (source != null) && (!source.equals(target)))
/*      */         {
/*  240 */           nodeName = XLIFF_NODE_NAMES[4];
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  245 */       if (rootNode.m_name.equals(XLIFF_NODE_NAMES[0]))
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/*  250 */       int numChildren = rootNode.m_subNodes.size();
/*  251 */       for (int i = 0; i < numChildren; ++i)
/*      */       {
/*  253 */         callStack.add(rootNode.m_subNodes.get(i));
/*      */       }
/*  255 */       rootNode = null;
/*      */     }
/*      */ 
/*  258 */     if (rootNode == null)
/*      */     {
/*  260 */       Report.trace("resource", "Root node not found", null);
/*  261 */       nodeList.clear();
/*  262 */       return;
/*      */     }
/*      */ 
/*  266 */     PropertiesTreeNode node = null;
/*  267 */     Vector nodes = rootNode.m_subNodes;
/*      */ 
/*  269 */     int len = nodes.size();
/*  270 */     for (int i = 0; i < len; ++i)
/*      */     {
/*  272 */       node = (PropertiesTreeNode)nodes.elementAt(i);
/*  273 */       if (!node.m_name.equals(XLIFF_NODE_NAMES[1]))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  278 */       String key = null;
/*      */       try
/*      */       {
/*  281 */         key = StringUtils.decodeJavascriptFilename((String)node.m_properties.get(XLIFF_NODE_NAMES[2]));
/*      */       }
/*      */       catch (CharConversionException c)
/*      */       {
/*  286 */         throw new ServiceException(LocaleUtils.encodeMessage("csStringPoorlyEncoded", null, node.m_properties.get(XLIFF_NODE_NAMES[2])));
/*      */       }
/*      */ 
/*  290 */       int numSubNodes = node.m_subNodes.size();
/*  291 */       PropertiesTreeNode subNode = null;
/*  292 */       for (int j = 0; j < numSubNodes; ++j)
/*      */       {
/*  294 */         subNode = (PropertiesTreeNode)node.m_subNodes.elementAt(j);
/*  295 */         if (subNode.m_name.equals(nodeName)) {
/*      */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  301 */       if (subNode == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  306 */       String str = res.decodeResourceStringWithFlags(subNode.m_value, 1);
/*      */ 
/*  308 */       IdcLocaleString lcString = (IdcLocaleString)res.m_stringObjMap.get(key);
/*  309 */       if (lcString == null)
/*      */       {
/*  311 */         lcString = new IdcLocaleString(key);
/*  312 */         res.m_stringObjMap.put(key, lcString);
/*  313 */         res.m_stringsList.add(key);
/*      */       }
/*  315 */       String strLang = lang;
/*  316 */       if ((lang == null) || (lang.length() == 0))
/*      */       {
/*  318 */         strLang = LocaleResources.m_baseLanguage;
/*      */       }
/*  320 */       int langIndex = res.m_languages.getCode(strLang);
/*  321 */       if (langIndex == -1)
/*      */       {
/*  323 */         res.m_languages.add(strLang);
/*  324 */         langIndex = res.m_languages.getCode(strLang);
/*      */       }
/*  326 */       lcString.setLangValue(langIndex, str);
/*      */ 
/*  328 */       ResourceObject obj = new ResourceObject();
/*  329 */       obj.m_name = key;
/*  330 */       obj.m_type = 0;
/*  331 */       obj.m_resource = str;
/*  332 */       res.m_resourceList.add(obj);
/*      */     }
/*  334 */     res.m_dynamicStringLoaded = true;
/*  335 */     nodeList.clear();
/*      */   }
/*      */ 
/*      */   protected static IdcBreakpoints adjustBreakpoints(String filename, File file)
/*      */   {
/*  340 */     if (!IdcBreakpointManager.m_enableDebug)
/*      */     {
/*  343 */       return null;
/*      */     }
/*      */ 
/*  346 */     IdcBreakpoints bp = (IdcBreakpoints)SharedObjects.getObject("breakpoints", filename);
/*  347 */     if (bp == null)
/*      */     {
/*  349 */       bp = new IdcBreakpoints();
/*  350 */       bp.m_fileName = filename;
/*  351 */       SharedObjects.putObject("breakpoints", filename, bp);
/*      */     }
/*      */ 
/*  355 */     boolean isNew = false;
/*  356 */     int len = (int)file.length();
/*  357 */     if (bp.m_lines == null)
/*      */     {
/*  359 */       isNew = true;
/*  360 */       bp.m_lines = new boolean[len];
/*      */     }
/*      */ 
/*  363 */     synchronized (bp.m_fileName)
/*      */     {
/*  365 */       if (bp.m_isDirty)
/*      */       {
/*  367 */         boolean[] lines = new boolean[len];
/*      */ 
/*  369 */         int size = bp.m_newLineNumbers.length;
/*  370 */         for (int i = 0; i < size; ++i)
/*      */         {
/*  372 */           int lineNumber = bp.m_newLineNumbers[i];
/*  373 */           if ((lineNumber < 0) || (len < lineNumber))
/*      */           {
/*  376 */             Report.trace("idcdebug", "ResourceLoader.adjustBreakpoints: invalid line number " + lineNumber + " file=" + bp.m_fileName, null);
/*      */           }
/*      */           else
/*      */           {
/*  381 */             lines[lineNumber] = true;
/*      */           }
/*      */         }
/*  383 */         bp.m_lines = lines;
/*  384 */         bp.m_isDirty = false;
/*      */       }
/*  386 */       else if (!isNew)
/*      */       {
/*  388 */         int size = bp.m_lines.length;
/*  389 */         if (size != len)
/*      */         {
/*  392 */           boolean[] lines = new boolean[len];
/*  393 */           int count = (size > len) ? len : size;
/*  394 */           System.arraycopy(bp.m_lines, 0, lines, 0, count);
/*  395 */           bp.m_lines = lines;
/*      */         }
/*      */       }
/*      */     }
/*  399 */     return bp;
/*      */   }
/*      */ 
/*      */   public static void loadLocalizationStrings(String resourceDir, String dataDir, String localStringDataDir)
/*      */     throws ServiceException
/*      */   {
/*  405 */     Properties environment = (Properties)AppObjectRepository.getObject("environment");
/*      */ 
/*  407 */     LocaleResources.init(environment);
/*      */ 
/*  409 */     ResourceContainer container = SharedObjects.getResources();
/*  410 */     loadLocalizationStrings(container, resourceDir, dataDir, localStringDataDir, 0);
/*      */   }
/*      */ 
/*      */   protected static void setupIndex(ResourceContainer container, Vector validHelpLangs, IdcLocalizationStrings stringIndex)
/*      */     throws ServiceException
/*      */   {
/*  418 */     stringIndex = stringIndex.duplicateIndex();
/*  419 */     LocaleResources.m_stringData = stringIndex;
/*  420 */     validHelpLangs = new IdcVector();
/*  421 */     String helpDir = Help.getHelpDir();
/*  422 */     for (int i = 0; i < LocaleResources.m_languages.size(); ++i)
/*      */     {
/*  424 */       String langId = (String)LocaleResources.m_languages.get(i);
/*  425 */       String helpPath = helpDir + langId;
/*  426 */       if (FileUtils.checkFile(helpPath, false, false) != 0)
/*      */         continue;
/*  428 */       validHelpLangs.addElement(langId);
/*      */     }
/*      */ 
/*  431 */     container.m_handler = stringIndex;
/*      */   }
/*      */ 
/*      */   public static void loadLocalizationStrings(ResourceContainer container, String resourceDir, String dataDir, String localStringDataDir, int flags)
/*      */     throws ServiceException
/*      */   {
/*  438 */     IntervalData interval = new IntervalData("loadLocalizationStrings startup");
/*      */ 
/*  440 */     IdcLocalizationStrings stringIndex = null;
/*      */     try
/*      */     {
/*  443 */       String stringDataDir = FileUtils.getAbsolutePath(dataDir, "strings");
/*  444 */       Vector validHelpLangs = null;
/*  445 */       if ((flags & F_FORCE_LOAD) == 0)
/*      */       {
/*  447 */         Map args = new HashMap();
/*      */ 
/*  450 */         args.put("psName", "LocalizationIndex");
/*  451 */         args.put("psPrefix", "LocalizationIndex");
/*  452 */         args.put("ProgressDirectory", FileUtils.getAbsolutePath(stringDataDir, "log"));
/*      */ 
/*  454 */         ProgressState progressState = new ProgressState();
/*      */         try
/*      */         {
/*  470 */           boolean createStringIndex = false;
/*  471 */           if (createStringIndex)
/*      */           {
/*  476 */             progressState.init(args);
/*  477 */             stringIndex = new IdcLocalizationStrings(SharedObjects.getSecureEnvironment(), stringDataDir, localStringDataDir, progressState);
/*      */ 
/*  479 */             stringIndex.loadStringIndex();
/*  480 */             setupIndex(container, validHelpLangs, stringIndex);
/*      */           }
/*      */ 
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/*  487 */           stringIndex = new IdcLocalizationStrings(SharedObjects.getSecureEnvironment(), stringDataDir, localStringDataDir, progressState);
/*      */           try
/*      */           {
/*  491 */             stringIndex.readConfigFile();
/*      */           }
/*      */           catch (ServiceException e2)
/*      */           {
/*  496 */             throw new ServiceException(e2);
/*      */           }
/*  498 */           Report.trace("localization", "unable to load string index, reserving directory", e);
/*  499 */           stringIndex.reserve("startup");
/*      */           try
/*      */           {
/*  504 */             stringIndex.loadStringIndex();
/*  505 */             Report.trace("localization", "using newly found string index", null);
/*  506 */             setupIndex(container, validHelpLangs, stringIndex);
/*      */           }
/*      */           catch (ServiceException e2)
/*      */           {
/*  510 */             Report.trace("localization", "unable to load string index, will build now", e2);
/*  511 */             flags |= F_FORCE_LOAD;
/*      */ 
/*  514 */             stringIndex = new IdcLocalizationStrings(SharedObjects.getSecureEnvironment(), stringDataDir, localStringDataDir, progressState);
/*      */ 
/*  516 */             container.m_handler = stringIndex;
/*      */ 
/*  519 */             LocaleResources.m_stringData = (IdcLocalizationStrings)container.m_handler;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  524 */       DataResultSet localeConfig = null;
/*  525 */       DataResultSet languageMap = null;
/*  526 */       DataResultSet localeMap = null;
/*  527 */       boolean tolerant = SharedObjects.getEnvValueAsBoolean("TolerateLocalizationFailure", true);
/*      */       try
/*      */       {
/*  531 */         String file = FileUtils.getAbsolutePath(resourceDir, "tables/std_locale.htm");
/*  532 */         loadResourceFile(container, file);
/*  533 */         interval.stop();
/*  534 */         interval.delayTrace("startup", "time to start ");
/*      */ 
/*  536 */         loadAndMergeStdLocaleOverrides(dataDir, container);
/*      */ 
/*  538 */         localeConfig = SharedObjects.getTable("LocaleConfig");
/*  539 */         languageMap = SharedObjects.getTable("LanguageLocationMap");
/*  540 */         localeMap = SharedObjects.getTable("LanguageLocaleMap");
/*      */ 
/*  542 */         String msg = "!$Unable to load localization. ";
/*  543 */         boolean failure = false;
/*  544 */         if (localeConfig == null)
/*      */         {
/*  546 */           msg = msg + "!$The LocaleConfig table is missing. ";
/*  547 */           failure = true;
/*      */         }
/*  549 */         if (languageMap == null)
/*      */         {
/*  551 */           msg = msg + "!$The LanguageLocationMap table is missing. ";
/*  552 */           failure = true;
/*      */         }
/*  554 */         if (localeMap == null)
/*      */         {
/*  556 */           msg = msg + "!$The LanguageLocaleMap table is missing. ";
/*  557 */           failure = true;
/*      */         }
/*  559 */         if (failure)
/*      */         {
/*  561 */           throw new ServiceException(msg);
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  566 */         Report.trace(null, null, e);
/*  567 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/*  570 */       String systemLocale = SharedObjects.getEnvironmentValue("SystemLocale");
/*  571 */       if (systemLocale == null)
/*      */       {
/*  573 */         SharedObjects.putEnvironmentValue("SystemLocaleSpecified", "0");
/*      */ 
/*  575 */         String locale = SharedObjects.getEnvironmentValue("IdcLocale");
/*  576 */         if ((locale != null) && (locale.length() > 0))
/*      */         {
/*  578 */           if (locale.equalsIgnoreCase("EnglishX"))
/*      */           {
/*  580 */             SharedObjects.putEnvironmentValue("SearchLocale", "englishx");
/*      */           }
/*      */           else
/*      */           {
/*  584 */             systemLocale = locale;
/*  585 */             SharedObjects.putEnvironmentValue("SystemLocale", systemLocale);
/*      */           }
/*      */         }
/*      */ 
/*  589 */         if (systemLocale == null)
/*      */         {
/*  591 */           systemLocale = LocaleLoader.determineDefaultLocale();
/*  592 */           if (systemLocale != null)
/*      */           {
/*  594 */             SharedObjects.putEnvironmentValue("SystemLocale", systemLocale);
/*      */           }
/*      */         }
/*      */ 
/*  598 */         if (systemLocale == null)
/*      */         {
/*  600 */           systemLocale = "English-US";
/*  601 */           SharedObjects.putEnvironmentValue("SystemLocale", systemLocale);
/*  602 */           SharedObjects.putEnvironmentValue("SystemLocaleNotFound", "1");
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  607 */         SharedObjects.putEnvironmentValue("SystemLocaleSpecified", "1");
/*      */       }
/*      */ 
/*  610 */       FieldInfo info = new FieldInfo();
/*  611 */       localeConfig.getFieldInfo("lcLocaleId", info);
/*  612 */       if (localeConfig.findRow(info.m_index, systemLocale) == null)
/*      */       {
/*  614 */         String msg = "!$The system locale '" + systemLocale + "' is not defined in the LocaleConfig table.";
/*      */ 
/*  616 */         if (!tolerant) throw new ServiceException(msg);
/*  617 */         reportError(LocaleResources.localizeMessage(msg, null));
/*  618 */         localeConfig.findRow(info.m_index, "English-US");
/*  619 */         SharedObjects.putEnvironmentValue("SystemLocale", "English-US");
/*      */       }
/*      */ 
/*  622 */       Properties props = localeConfig.getCurrentRowProps();
/*  623 */       String systemLangId = props.getProperty("lcLanguageId");
/*      */ 
/*  625 */       boolean loadedSomething = false;
/*  626 */       List languageDirs = null;
/*      */ 
/*  628 */       if (LocaleResources.m_languages.size() == 0)
/*      */       {
/*  630 */         Map languages = new HashMap();
/*  631 */         for (SimpleParameters params : localeConfig.getSimpleParametersIterable())
/*      */         {
/*  633 */           String lang = params.get("lcLanguageId");
/*  634 */           languages.put(lang, lang);
/*      */         }
/*  636 */         for (String lang : languages.keySet())
/*      */         {
/*  638 */           LocaleResources.m_languages.add(lang);
/*      */         }
/*  640 */         container.m_languages = LocaleResources.m_languages;
/*      */       }
/*      */ 
/*  643 */       interval = new IntervalData(resourceDir);
/*  644 */       String path = FileUtils.getAbsolutePath(resourceDir, "lang/");
/*  645 */       loadStrings(container, path + "sy_strings.htm", "", true, flags);
/*  646 */       loadStrings(container, path + "cs_strings.htm", "", true, flags);
/*  647 */       loadStrings(container, path + "ap_strings.htm", "", true, flags);
/*  648 */       loadStrings(container, path + "ww_strings.htm", "", true, flags);
/*  649 */       interval.stop();
/*  650 */       interval.delayTrace("startup", "core strings ");
/*      */ 
/*  652 */       if (container.m_handler == null)
/*      */       {
/*  654 */         LocaleResources.initStrings(container);
/*      */       }
/*      */ 
/*  657 */       languageDirs = LocaleUtils.getLanguageDirectoryList(null, path);
/*      */ 
/*  659 */       for (int i = 0; i < languageDirs.size(); ++i)
/*      */       {
/*  661 */         String dir = (String)languageDirs.get(i);
/*  662 */         Report.trace("localization", "looking in " + dir + " for localization data.", null);
/*  663 */         if (FileUtils.checkFile(dir, false, false) == 0)
/*      */         {
/*  665 */           loadBaseLanguageFilesFromDir(container, dir, null, languageMap);
/*  666 */           loadedSomething = true;
/*      */         }
/*  668 */         if (FileUtils.checkFile(dir + systemLangId, false, false) == 0)
/*      */         {
/*  670 */           loadBaseLanguageFilesFromDir(container, dir, systemLangId, languageMap);
/*  671 */           loadedSomething = true;
/*      */         }
/*  673 */         if (FileUtils.checkFile(dir, true, false) != 0)
/*      */           continue;
/*  675 */         loadStrings(container, dir, null, true, flags);
/*      */       }
/*      */ 
/*  678 */       IdcStringBuilder builder = new IdcStringBuilder();
/*  679 */       String thisOrThese = "this directory";
/*  680 */       for (int i = 0; i < languageDirs.size(); ++i)
/*      */       {
/*  682 */         String dir = (String)languageDirs.get(i);
/*  683 */         if (i > 0)
/*      */         {
/*  685 */           thisOrThese = "these directories";
/*  686 */           builder.append(", ");
/*      */         }
/*  688 */         builder.append(dir);
/*      */       }
/*  690 */       builder.append(".");
/*  691 */       String msg = builder.toString();
/*  692 */       if (!loadedSomething)
/*      */       {
/*  694 */         throw new ServiceException("Unable to load base strings from " + thisOrThese + ": " + msg);
/*      */       }
/*      */ 
/*  697 */       if (container.m_handler == null)
/*      */       {
/*  700 */         LocaleResources.initStrings(container);
/*  701 */         if ((!systemLangId.equals("en")) && 
/*  703 */           (container.m_languages.get(systemLangId) == null))
/*      */         {
/*  705 */           String errorMsg = "Unable to load system language " + systemLangId + " from " + thisOrThese + ": " + msg;
/*  706 */           if (!tolerant)
/*      */           {
/*  708 */             throw new ServiceException(errorMsg);
/*      */           }
/*  710 */           System.err.println(errorMsg);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  715 */       addExtraLocaleConfigColumns(localeConfig);
/*      */ 
/*  719 */       validHelpLangs = new IdcVector();
/*  720 */       String helpDir = Help.getHelpDir();
/*  721 */       for (int i = 0; i < languageDirs.size(); ++i)
/*      */       {
/*  723 */         String dir = (String)languageDirs.get(i);
/*  724 */         if (FileUtils.checkFile(dir + systemLangId, false, false) == 0)
/*      */         {
/*  726 */           loadAllLocalizationStrings(container, dir, helpDir, validHelpLangs, flags);
/*  727 */           loadedSomething = true;
/*      */         }
/*  729 */         else if (FileUtils.checkFile(dir, false, false) == 0)
/*      */         {
/*  731 */           boolean strict = (flags & F_IS_STRICT) != 0;
/*  732 */           int allStringsFlags = flags;
/*  733 */           if ((tolerant) && (!strict))
/*      */           {
/*  735 */             allStringsFlags |= F_IS_TOLERANT;
/*      */           }
/*  737 */           allStringsFlags |= F_LOAD_BASE_STRINGS;
/*  738 */           loadLocalizationStringsEx(container, dir, helpDir, validHelpLangs, allStringsFlags);
/*  739 */           loadedSomething = true;
/*      */         } else {
/*  741 */           if (FileUtils.checkFile(dir, true, false) != 0)
/*      */             continue;
/*  743 */           loadStrings(container, dir, null, false, flags);
/*      */         }
/*      */       }
/*      */ 
/*  747 */       if (container.m_handler == null)
/*      */       {
/*  749 */         LocaleResources.initStrings(container);
/*      */       }
/*      */ 
/*  753 */       Help.setValidHelpLangs(validHelpLangs);
/*  754 */       String validHelpLangsStr = StringUtils.createString(validHelpLangs, ',', ',');
/*  755 */       SharedObjects.putEnvironmentValue("ValidHelpLangs", validHelpLangsStr);
/*      */     }
/*      */     finally
/*      */     {
/*  759 */       IntervalData.doDelayedTraces();
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadAndMergeStdLocaleOverrides(String dataDir, ResourceContainer container)
/*      */     throws ServiceException, DataException
/*      */   {
/*  776 */     String[][] localeTables = { { "LocaleConfig", "lcLocaleId" }, { "SearchLocaleConfig", "lcLocaleId" }, { "LanguageLocaleMap", "lcLanguageId" }, { "LanguageLocationMap", "lcLanguageId" }, { "LanguageDirectionMap", "lcLanguageId" } };
/*      */ 
/*  784 */     Vector[] mergeLists = new IdcVector[localeTables.length];
/*      */ 
/*  786 */     DataBinder binder = new DataBinder();
/*  787 */     ResourceUtils.serializeDataBinder(dataDir + "/locale", "locale_config.hda", binder, false, false);
/*      */ 
/*  804 */     Enumeration en = binder.getResultSetList();
/*  805 */     Vector rsets = new IdcVector();
/*  806 */     while (en.hasMoreElements())
/*      */     {
/*  808 */       rsets.addElement(en.nextElement());
/*      */     }
/*  810 */     for (int i = 0; i < localeTables.length; ++i)
/*      */     {
/*  812 */       mergeLists[i] = new IdcVector();
/*  813 */       int count = rsets.size();
/*  814 */       for (int j = 0; j < count; ++j)
/*      */       {
/*  816 */         String name = (String)rsets.elementAt(j);
/*  817 */         if (!name.startsWith(localeTables[i][0]))
/*      */           continue;
/*  819 */         mergeLists[i].addElement(binder.getResultSet(name));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  825 */     for (int i = 0; i < localeTables.length; ++i)
/*      */     {
/*  827 */       Table table = (Table)container.m_tables.get(localeTables[i][0]);
/*  828 */       DataResultSet coreTable = new DataResultSet();
/*  829 */       coreTable.init(table);
/*      */ 
/*  831 */       if ((i == 0) && (SharedObjects.getEnvValueAsBoolean("AllowUnsupportedLocales", false)))
/*      */       {
/*  834 */         Table unsupportedTable = (Table)container.m_tables.get("UnsupportedLocaleConfig");
/*  835 */         DataResultSet unsupportedResultSet = new DataResultSet();
/*  836 */         unsupportedResultSet.init(unsupportedTable);
/*  837 */         coreTable.merge("lcLocaleId", unsupportedResultSet, false);
/*      */       }
/*      */ 
/*  840 */       rsets = mergeLists[i];
/*  841 */       for (int j = 0; j < rsets.size(); ++j)
/*      */       {
/*  843 */         DataResultSet drset = (DataResultSet)rsets.elementAt(j);
/*  844 */         coreTable.merge(localeTables[i][1], drset, false);
/*      */       }
/*  846 */       SharedObjects.putTable(localeTables[i][0], coreTable);
/*      */     }
/*      */ 
/*  850 */     DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/*  851 */     DataResultSet directions = SharedObjects.getTable("LanguageDirectionMap");
/*  852 */     localeConfig.mergeFields(directions);
/*  853 */     FieldInfo[] localeFields = ResultSetUtils.createInfoList(localeConfig, new String[] { "lcLanguageId", "lcDirection" }, true);
/*      */ 
/*  855 */     for (localeConfig.first(); localeConfig.isRowPresent(); localeConfig.next())
/*      */     {
/*  857 */       String languageId = localeConfig.getStringValue(localeFields[0].m_index);
/*  858 */       String direction = ResultSetUtils.findValue(directions, "lcLanguageId", languageId, "lcDirection");
/*  859 */       if ((direction == null) || (direction.length() == 0))
/*      */       {
/*  861 */         direction = "ltr";
/*      */       }
/*  863 */       localeConfig.setCurrentValue(localeFields[1].m_index, direction);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addExtraLocaleConfigColumns(DataResultSet localeConfig)
/*      */   {
/*  870 */     if (m_localeEnvMergeKeys == null)
/*      */       return;
/*  872 */     Vector vFields = new IdcVector();
/*  873 */     for (int i = 0; i < m_localeEnvMergeKeys.length; ++i)
/*      */     {
/*  875 */       FieldInfo fi = new FieldInfo();
/*  876 */       fi.m_name = ("lc" + m_localeEnvMergeKeys[i]);
/*  877 */       vFields.addElement(fi);
/*      */     }
/*  879 */     localeConfig.mergeFieldsWithFlags(vFields, 0);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void loadLocalizationStringsFromDir(String resourceDir, String helpDir, Vector helpLangs, boolean includeBaseDir, String traceSection)
/*      */     throws ServiceException
/*      */   {
/*  890 */     ResourceContainer container = SharedObjects.getResources();
/*  891 */     if (includeBaseDir)
/*      */     {
/*  893 */       loadAllLocalizationStrings(container, resourceDir + "lang/", helpDir, helpLangs, 0);
/*      */     }
/*      */     else
/*      */     {
/*  897 */       loadLocalizationStringsEx(container, resourceDir + "lang/", helpDir, helpLangs, 0);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void loadAllLocalizationStrings(String dir, String helpDir, Vector helpLangs)
/*      */     throws ServiceException
/*      */   {
/*  906 */     boolean tolerant = SharedObjects.getEnvValueAsBoolean("TolerateLocalizationFailure", true);
/*      */ 
/*  908 */     int flags = (tolerant) ? F_IS_TOLERANT : 0;
/*  909 */     flags |= F_LOAD_BASE_STRINGS;
/*  910 */     ResourceContainer container = SharedObjects.getResources();
/*  911 */     loadLocalizationStringsEx(container, dir, helpDir, helpLangs, flags);
/*      */   }
/*      */ 
/*      */   public static void loadAllLocalizationStrings(ResourceContainer container, String dir, String helpDir, Vector helpLangs, int flags)
/*      */     throws ServiceException
/*      */   {
/*  917 */     boolean tolerant = SharedObjects.getEnvValueAsBoolean("TolerateLocalizationFailure", true);
/*      */ 
/*  919 */     if (tolerant)
/*      */     {
/*  921 */       flags |= F_IS_TOLERANT;
/*      */     }
/*  923 */     flags |= F_LOAD_BASE_STRINGS;
/*  924 */     loadLocalizationStringsEx(container, dir, helpDir, helpLangs, flags);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void loadLocalizationStringsEx(ResourceContainer container, String dir, String helpDir, Vector helpLangs, int flags)
/*      */     throws ServiceException
/*      */   {
/*  934 */     loadLocalizationStrings(container, dir, helpDir, helpLangs, flags);
/*      */   }
/*      */ 
/*      */   public static void loadLocalizationStrings(ResourceContainer container, String dir, String helpDir, Vector helpLangs, int flags)
/*      */     throws ServiceException
/*      */   {
/*  941 */     dir = FileUtils.directorySlashes(dir);
/*  942 */     boolean includeBaseDir = (flags & F_LOAD_BASE_STRINGS) != 0;
/*  943 */     if (helpLangs == null)
/*      */     {
/*  945 */       helpLangs = new IdcVector();
/*      */     }
/*  947 */     if (helpDir == null)
/*      */     {
/*  949 */       helpDir = dir;
/*      */     }
/*      */ 
/*  952 */     DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/*  953 */     String systemLocale = SharedObjects.getEnvironmentValue("SystemLocale");
/*  954 */     DataResultSet languageMap = SharedObjects.getTable("LanguageLocationMap");
/*      */ 
/*  956 */     if (includeBaseDir)
/*      */     {
/*  958 */       IntervalData interval = new IntervalData("load base");
/*  959 */       loadAllLanguageFilesFromDir(container, dir, "", languageMap, flags);
/*  960 */       interval.delayTrace("startup", "strings ");
/*      */     }
/*      */ 
/*  963 */     for (localeConfig.first(); localeConfig.isRowPresent(); localeConfig.next())
/*      */     {
/*  965 */       Properties props = localeConfig.getCurrentRowProps();
/*  966 */       String id = props.getProperty("lcLocaleId");
/*  967 */       boolean isEnabled = StringUtils.convertToBool(props.getProperty("lcIsEnabled"), false);
/*  968 */       if (m_loadSystemStringsOnly)
/*      */       {
/*  970 */         isEnabled = false;
/*      */       }
/*  972 */       if ((flags & F_FORCE_LOAD) != 0)
/*      */       {
/*  974 */         isEnabled = true;
/*      */       }
/*  976 */       if (id.equals(systemLocale))
/*      */       {
/*  978 */         isEnabled = true;
/*      */       }
/*  980 */       if (!isEnabled)
/*      */         continue;
/*  982 */       loadLocalizationStringsForEnabledLocale(dir, helpDir, helpLangs, localeConfig, id, props, container, languageMap, flags);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadLocalizationStringsForEnabledLocale(String dir, String helpDir, Vector helpLangs, DataResultSet localeConfig, String id, Properties curRowProps, ResourceContainer container, DataResultSet languageMap, int flags)
/*      */     throws ServiceException
/*      */   {
/*  993 */     if (m_localeEnvMergeKeys != null)
/*      */     {
/*  995 */       for (int i = 0; i < m_localeEnvMergeKeys.length; ++i)
/*      */       {
/*  997 */         String val = SharedObjects.getEnvironmentValue(id + ":" + m_localeEnvMergeKeys[i]);
/*  998 */         if (val == null)
/*      */           continue;
/* 1000 */         String lcKey = "lc" + m_localeEnvMergeKeys[i];
/*      */         try
/*      */         {
/* 1003 */           int index = ResultSetUtils.getIndexMustExist(localeConfig, lcKey);
/* 1004 */           localeConfig.setCurrentValue(index, val);
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/* 1009 */           Report.trace(null, null, e);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1015 */     String langId = curRowProps.getProperty("lcLanguageId");
/* 1016 */     IntervalData interval = new IntervalData("load " + langId);
/* 1017 */     loadAllLanguageFilesFromDir(container, dir, langId, languageMap, flags);
/* 1018 */     interval.stop();
/* 1019 */     interval.delayTrace("startup", "strings ");
/* 1020 */     if (m_helpFilesInWeblayout)
/*      */     {
/* 1022 */       if (helpDir == null)
/*      */         return;
/* 1024 */       String helpPath = helpDir + langId;
/* 1025 */       if (FileUtils.checkFile(helpPath, false, false) == 0)
/*      */       {
/* 1027 */         helpLangs.addElement(langId);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1034 */       helpLangs.addElement(langId);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void loadExtraStrings(ResourceContainer container, String path, String langId)
/*      */     throws ServiceException
/*      */   {
/* 1043 */     Report.deprecatedUsage("ResourceLoader.loadExtraStrings() is unnecessary.");
/*      */   }
/*      */ 
/*      */   public static String computeLocale(Locale lc, DataResultSet localeMap)
/*      */   {
/* 1056 */     return computeLocale(lc, localeMap, null);
/*      */   }
/*      */ 
/*      */   public static String computeLocale(Locale lc, DataResultSet localeMap, String resourceDir)
/*      */   {
/* 1069 */     String result = null;
/* 1070 */     String javaLocale = lc.toString();
/* 1071 */     javaLocale = LocaleUtils.normalizeId(javaLocale);
/* 1072 */     List dirList = null;
/* 1073 */     if (resourceDir != null)
/*      */     {
/* 1075 */       String langDir = FileUtils.getAbsolutePath(resourceDir, "lang/");
/*      */       try
/*      */       {
/* 1078 */         dirList = LocaleUtils.getLanguageDirectoryList(null, langDir);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1082 */         Report.trace("localization", "Unable to compute language directories", e);
/*      */       }
/*      */     }
/* 1085 */     int numLangDirs = (dirList == null) ? 0 : dirList.size();
/*      */ 
/* 1087 */     String nearestLang = "";
/* 1088 */     for (localeMap.first(); localeMap.isRowPresent(); localeMap.next())
/*      */     {
/* 1090 */       Properties props = localeMap.getCurrentRowProps();
/* 1091 */       String lang = props.getProperty("lcLanguageId");
/* 1092 */       lang = LocaleUtils.normalizeId(lang);
/* 1093 */       if ((!javaLocale.startsWith(lang)) || (lang.length() <= nearestLang.length()))
/*      */         continue;
/* 1095 */       boolean ok = true;
/* 1096 */       if (dirList != null)
/*      */       {
/* 1098 */         ok = false;
/* 1099 */         for (int i = 0; i < numLangDirs; ++i)
/*      */         {
/* 1101 */           String dirname = dirList.get(i) + lang;
/* 1102 */           if (FileUtils.checkFile(dirname, false, false) != 0)
/*      */             continue;
/* 1104 */           ok = true;
/* 1105 */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1110 */       if (!ok)
/*      */         continue;
/* 1112 */       nearestLang = lang;
/* 1113 */       result = props.getProperty("lcLocaleId");
/*      */     }
/*      */ 
/* 1118 */     return result;
/*      */   }
/*      */ 
/*      */   public static void loadStrings(ResourceContainer container, String path, String langId, boolean isSystem, int flags)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1126 */       loadResourceFileEx(container, path, path, true, null, 0L, langId);
/*      */ 
/* 1128 */       container.m_stringsList.add("!reset");
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1132 */       if ((isSystem) || ((flags & F_IS_STRICT) != 0))
/*      */       {
/* 1134 */         if (e instanceof ServiceException)
/*      */         {
/* 1136 */           throw ((ServiceException)e);
/*      */         }
/* 1138 */         String msg = LocaleUtils.encodeMessage("csUnableToLoadStrings2", null, path);
/*      */ 
/* 1140 */         throw new ServiceException(msg, e);
/*      */       }
/*      */     }
/*      */ 
/* 1144 */     String msg = LocaleUtils.encodeMessage("csUnableToLoadStrings2", LocaleUtils.createMessageStringFromThrowable(e), path);
/*      */ 
/* 1148 */     reportError(null, LocaleResources.localizeMessage(msg, null), e);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void loadLanguage(ResourceContainer container, String resDir, String langId, DataResultSet languageMap, boolean isSystem)
/*      */     throws ServiceException
/*      */   {
/* 1160 */     if (isSystem)
/*      */     {
/* 1162 */       loadBaseLanguageFilesFromDir(container, resDir + "lang/", langId, languageMap);
/*      */     }
/*      */     else
/*      */     {
/* 1166 */       loadAllLanguageFilesFromDir(container, resDir + "lang/", langId, languageMap, 0);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadBaseLanguageFilesFromDir(ResourceContainer container, String dir, String langId, DataResultSet languageMap)
/*      */     throws ServiceException
/*      */   {
/* 1175 */     boolean tolerant = SharedObjects.getEnvValueAsBoolean("TolerateLocalizationFailure", true);
/*      */ 
/* 1177 */     int flags = F_IS_SYSTEM | ((tolerant) ? F_IS_TOLERANT : 0);
/*      */ 
/* 1179 */     loadLanguageFilesFromDirEx(container, dir, langId, languageMap, flags);
/*      */   }
/*      */ 
/*      */   public static void loadAllLanguageFilesFromDir(ResourceContainer container, String dir, String langId, DataResultSet languageMap, int flags)
/*      */     throws ServiceException
/*      */   {
/* 1187 */     loadLanguageFilesFromDirEx(container, dir, langId, languageMap, flags);
/*      */   }
/*      */ 
/*      */   protected static void loadLanguageFilesFromDirEx(ResourceContainer container, String dir, String langId, DataResultSet languageMap, int flags)
/*      */     throws ServiceException
/*      */   {
/* 1194 */     boolean baseOnly = (flags & F_IS_SYSTEM) != 0;
/* 1195 */     boolean tolerant = (flags & F_IS_TOLERANT) != 0;
/* 1196 */     boolean strict = (flags & F_IS_STRICT) != 0;
/* 1197 */     if (!strict)
/*      */     {
/* 1199 */       strict = baseOnly;
/*      */     }
/* 1201 */     String path = (langId != null) ? dir + langId : dir;
/* 1202 */     DataResultSet languageDirs = new DataResultSet();
/* 1203 */     languageDirs.copySimpleFiltered(languageMap, "lcLanguageId", langId);
/*      */ 
/* 1205 */     if (FileUtils.checkFile(path, false, false) == 0)
/*      */     {
/* 1207 */       Report.trace("localization", "loading strings from " + path, null);
/*      */       String separator;
/*      */       String separator;
/* 1209 */       if (path.endsWith("/"))
/*      */       {
/* 1211 */         separator = "";
/*      */       }
/*      */       else
/*      */       {
/* 1215 */         separator = "/";
/*      */       }
/*      */ 
/* 1218 */       String suffix = ".htm";
/* 1219 */       if (FileUtils.checkFile(path + separator + "cs_strings.xlf", true, false) == 0)
/*      */       {
/* 1221 */         suffix = ".xlf";
/*      */       }
/* 1223 */       if ((tolerant) && (FileUtils.checkFile(path + separator + "sy_strings" + suffix, true, false) != 0))
/*      */       {
/* 1225 */         Report.trace("localization", "not loading strings from dir " + path, null);
/*      */       }
/*      */       else
/*      */       {
/* 1229 */         loadStrings(container, path + separator + "sy_strings" + suffix, langId, baseOnly, flags);
/* 1230 */         loadStrings(container, path + separator + "cs_strings" + suffix, langId, baseOnly, flags);
/* 1231 */         if (!baseOnly)
/*      */         {
/* 1233 */           loadStrings(container, path + separator + "ap_strings" + suffix, langId, baseOnly, flags);
/* 1234 */           loadStrings(container, path + separator + "ww_strings" + suffix, langId, baseOnly, flags);
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1240 */       if ((baseOnly) && (!tolerant))
/*      */       {
/* 1242 */         throw new ServiceException(-16, "!$Unable to load the system language from '" + path + "'. The directory  does not exist. ");
/*      */       }
/*      */ 
/* 1245 */       Report.trace("localization", "not loadings strings from non-existent dir " + path, null);
/*      */     }
/*      */ 
/* 1248 */     if ((baseOnly) || (!languageDirs.isRowPresent()))
/*      */       return;
/* 1250 */     FieldInfo info = new FieldInfo();
/* 1251 */     languageDirs.getFieldInfo("lcLanguageDirectory", info);
/* 1252 */     for (languageDirs.first(); languageDirs.isRowPresent(); languageDirs.next())
/*      */     {
/* 1254 */       String extraDir = languageDirs.getStringValue(info.m_index);
/* 1255 */       extraDir = FileUtils.directorySlashes(extraDir);
/* 1256 */       if (extraDir.startsWith("shared/"))
/*      */       {
/* 1258 */         String shared = SharedObjects.getEnvironmentValue("SharedDir");
/* 1259 */         if (shared != null)
/*      */         {
/* 1261 */           extraDir = shared + "/" + extraDir.substring("shared/".length());
/*      */         }
/*      */       }
/* 1264 */       if (FileUtils.checkFile(extraDir, false, false) != 0)
/*      */       {
/* 1266 */         if (baseOnly)
/*      */         {
/* 1268 */           throw new ServiceException("!$Unable to load the system language from '" + extraDir + "'. The directory does not exist. ");
/*      */         }
/*      */ 
/* 1271 */         String msg = LocaleResources.getString("csUnableToLoadStrings", null, extraDir);
/*      */ 
/* 1273 */         reportError(msg);
/*      */       }
/*      */ 
/* 1276 */       loadStrings(container, extraDir + "sy_strings.htm", langId, baseOnly, flags);
/* 1277 */       loadStrings(container, extraDir + "cs_strings.htm", langId, baseOnly, flags);
/* 1278 */       if (baseOnly)
/*      */         continue;
/* 1280 */       loadStrings(container, extraDir + "ap_strings.htm", langId, baseOnly, flags);
/* 1281 */       loadStrings(container, extraDir + "ww_strings.htm", langId, baseOnly, flags);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadQueries(String queryFile, String tableNames, Workspace ws, String cmptName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1290 */     ResourceContainer res = new ResourceContainer();
/* 1291 */     loadResourceFile(res, queryFile);
/*      */ 
/* 1293 */     if (tableNames == null)
/*      */     {
/* 1295 */       Map qTables = res.m_tables;
/* 1296 */       Iterator it = qTables.keySet().iterator();
/* 1297 */       while (it.hasNext())
/*      */       {
/* 1299 */         String tableName = (String)it.next();
/* 1300 */         QueryUtils.addQueryTable(ws, res, tableName, true, cmptName);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1305 */       Vector tables = StringUtils.parseArray(tableNames, ',', ',');
/* 1306 */       int num = tables.size();
/* 1307 */       for (int i = 0; i < num; ++i)
/*      */       {
/* 1309 */         String name = (String)tables.elementAt(i);
/* 1310 */         QueryUtils.addQueryTable(ws, res, name, true, cmptName);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static DataBinder loadDataBinderFromFile(String filename) throws ServiceException
/*      */   {
/* 1317 */     return loadDataBinderFromFileWithFlags(filename, 32);
/*      */   }
/*      */ 
/*      */   public static DataBinder loadDataBinderFromFileWithFlags(String filename, int flags) throws ServiceException
/*      */   {
/* 1322 */     DataBinder data = null;
/*      */ 
/* 1324 */     String dir = FileUtils.getDirectory(filename);
/* 1325 */     String file = FileUtils.getName(filename);
/*      */ 
/* 1327 */     boolean lock = (flags & 0x20) == 32;
/* 1328 */     if (lock)
/*      */     {
/* 1330 */       FileUtils.reserveDirectory(dir);
/*      */     }
/*      */     try
/*      */     {
/* 1334 */       data = ResourceUtils.readDataBinder(dir, file);
/*      */     }
/*      */     finally
/*      */     {
/* 1338 */       if (lock)
/*      */       {
/* 1340 */         FileUtils.releaseDirectory(dir);
/*      */       }
/*      */     }
/*      */ 
/* 1344 */     return data;
/*      */   }
/*      */ 
/*      */   public static DynamicHtml loadPage(String filename, boolean isXmlSyntax)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1350 */     BufferedReader reader = null;
/* 1351 */     DynamicHtml dynHtml = null;
/*      */     try
/*      */     {
/* 1354 */       File file = FileUtilsCfgBuilder.getCfgFile(filename, null, false);
/* 1355 */       IdcBreakpoints bp = adjustBreakpoints(filename, file);
/* 1356 */       String[] enc = new String[1];
/* 1357 */       reader = openResourceReader(file, enc, F_IS_HTML);
/*      */ 
/* 1359 */       dynHtml = new DynamicHtml();
/* 1360 */       dynHtml.loadHtmlEx(reader, filename, isXmlSyntax, bp);
/* 1361 */       dynHtml.m_sourceEncoding = enc[0];
/* 1362 */       dynHtml.m_timeStamp = file.lastModified();
/*      */     }
/*      */     catch (FileNotFoundException e)
/*      */     {
/* 1367 */       if (filename.length() == 0)
/*      */       {
/* 1369 */         throw new ServiceException(e, -16, "csResourceLoaderInvalidFilename", new Object[0]);
/*      */       }
/*      */ 
/* 1372 */       throw new ServiceException(e, -16, "csResourceLoaderFileOpenError", new Object[1]);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/* 1388 */         if (reader != null)
/*      */         {
/* 1390 */           reader.close();
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 1398 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1400 */           Report.debug("system", null, e);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1405 */     return dynHtml;
/*      */   }
/*      */ 
/*      */   public static void loadCachedResources(ResourceContainer res, ResourceCacheInfo cacheInfo)
/*      */   {
/* 1410 */     List resourceList = cacheInfo.m_resourceList;
/* 1411 */     if (cacheInfo.m_resourceList == null)
/*      */     {
/* 1419 */       return;
/*      */     }
/* 1421 */     int numResources = resourceList.size();
/* 1422 */     for (int i = 0; i < numResources; ++i)
/*      */     {
/* 1424 */       ResourceObject resourceObj = (ResourceObject)resourceList.get(i);
/* 1425 */       switch (resourceObj.m_type)
/*      */       {
/*      */       case 0:
/* 1428 */         String val = (String)resourceObj.m_resource;
/* 1429 */         String lang = cacheInfo.m_languageId;
/* 1430 */         if ((lang == null) || (lang.length() == 0))
/*      */         {
/* 1432 */           lang = LocaleResources.m_baseLanguage;
/*      */         }
/* 1434 */         String id = resourceObj.m_name;
/* 1435 */         int index = id.lastIndexOf(".");
/* 1436 */         if (index > 0)
/*      */         {
/* 1438 */           lang = resourceObj.m_name.substring(0, index);
/* 1439 */           id = resourceObj.m_name.substring(index + 1);
/*      */         }
/* 1441 */         IdcLocaleString lcString = (IdcLocaleString)res.m_stringObjMap.get(id);
/* 1442 */         if (lcString == null)
/*      */         {
/* 1444 */           lcString = new IdcLocaleString(id);
/* 1445 */           res.m_stringObjMap.put(id, lcString);
/*      */         }
/* 1447 */         int langIndex = res.m_languages.getCode(lang);
/* 1448 */         if (langIndex == -1)
/*      */         {
/* 1450 */           res.m_languages.add(lang);
/* 1451 */           langIndex = res.m_languages.getCode(lang);
/*      */         }
/* 1453 */         lcString.setLangValue(langIndex, val);
/* 1454 */         break;
/*      */       case 1:
/*      */       case 4:
/* 1458 */         res.m_tables.put(resourceObj.m_name, (Table)resourceObj.m_resource);
/* 1459 */         break;
/*      */       case 5:
/* 1462 */         res.m_stringArrays.put(resourceObj.m_name, resourceObj.m_resource);
/* 1463 */         break;
/*      */       case 2:
/*      */       case 3:
/* 1467 */         DynamicHtml dynHtml = (DynamicHtml)resourceObj.m_resource;
/* 1468 */         DynamicHtml priorScript = (DynamicHtml)res.m_dynamicHtml.get(resourceObj.m_name);
/*      */ 
/* 1470 */         if (priorScript != null)
/*      */         {
/* 1472 */           DynamicHtml oRes = dynHtml.findEarliestValidPriorScript(priorScript);
/* 1473 */           if (oRes != null)
/*      */           {
/* 1475 */             dynHtml = dynHtml.shallowCloneWithPriorScript(oRes);
/*      */           }
/*      */ 
/*      */         }
/* 1480 */         else if (resourceObj.m_type == 2)
/*      */         {
/* 1482 */           res.m_dynamicHtmlList.add(resourceObj.m_name);
/*      */         }
/*      */         else
/*      */         {
/* 1486 */           res.m_dynamicDataList.add(resourceObj.m_name);
/*      */         }
/*      */ 
/* 1489 */         res.m_dynamicHtml.put(resourceObj.m_name, dynHtml);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String createParseErrorMessage(ParseSyntaxException e)
/*      */   {
/* 1497 */     int line = e.m_parseInfo.m_parseLine;
/* 1498 */     int charOffset = e.m_parseInfo.m_parseCharOffset;
/* 1499 */     String fileName = e.m_parseInfo.m_fileName;
/* 1500 */     String fn = fileName;
/* 1501 */     if (fn == null)
/*      */     {
/* 1503 */       fn = "!csResourceLoaderUnknownFile";
/*      */     }
/* 1505 */     IdcMessage errorMsg = IdcMessageFactory.lc("csResourceLoaderParseError", new Object[] { fn, new Integer(line + 1), new Integer(charOffset + 1) });
/*      */ 
/* 1509 */     String context = "";
/* 1510 */     if (fileName != null)
/*      */     {
/* 1512 */       BufferedReader reader = null;
/*      */       try
/*      */       {
/* 1515 */         reader = new BufferedReader(new FileReader(fileName));
/* 1516 */         for (int i = 0; i < line; ++i)
/*      */         {
/* 1518 */           reader.readLine();
/*      */         }
/* 1520 */         for (int j = 0; j < 3; ++j)
/*      */         {
/* 1522 */           if (!reader.ready())
/*      */             continue;
/* 1524 */           context = context + "\n->" + reader.readLine();
/*      */         }
/*      */ 
/* 1527 */         context = context + "\n";
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/* 1531 */         ignore.printStackTrace();
/*      */       }
/*      */       finally
/*      */       {
/* 1535 */         FileUtils.closeObject(reader);
/*      */       }
/*      */     }
/* 1538 */     String errMsgStr = LocaleUtils.encodeMessage(errorMsg);
/* 1539 */     String msg = LocaleUtils.appendMessage(context, errMsgStr);
/*      */ 
/* 1541 */     return msg;
/*      */   }
/*      */ 
/*      */   public static BufferedReader openResourceReader(File file, String[] encBuff)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1547 */     return openResourceReader(file, encBuff, F_IS_HTML);
/*      */   }
/*      */ 
/*      */   public static BufferedReader openResourceReader(File file, String[] encBuff, int flags)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1553 */     BufferedReader reader = null;
/* 1554 */     if (file instanceof IdcConfigFile)
/*      */     {
/* 1556 */       reader = new BufferedReader(FileUtilsCfgBuilder.getCfgReader(file));
/*      */     }
/*      */     else
/*      */     {
/* 1560 */       BufferedInputStream bstream = new BufferedInputStream(new FileInputStream(file));
/*      */ 
/* 1562 */       String encoding = null;
/* 1563 */       encoding = detectEncoding(bstream, flags);
/* 1564 */       if (encoding == null)
/*      */       {
/* 1566 */         encoding = DataSerializeUtils.getSystemEncoding();
/*      */       }
/* 1568 */       reader = FileUtils.openDataReader(bstream, encoding);
/* 1569 */       if ((encBuff != null) && (encBuff.length > 0))
/*      */       {
/* 1571 */         encBuff[0] = encoding;
/*      */       }
/*      */     }
/* 1574 */     return reader;
/*      */   }
/*      */ 
/*      */   public static String detectEncoding(BufferedInputStream bstream, int flags)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1580 */     byte[] temp = new byte[512];
/* 1581 */     int len = temp.length;
/* 1582 */     String pageEncoding = null;
/* 1583 */     String encoding = null;
/* 1584 */     boolean isXml = (flags & F_IS_XML) != 0;
/* 1585 */     boolean isHda = (flags & F_IS_HDA) != 0;
/* 1586 */     boolean isHtml = (!isXml) && (!isHda);
/* 1587 */     if (flags == 0)
/*      */     {
/* 1589 */       Report.deprecatedUsage("detectEncoding() should be called with F_IS_XML, F_IS_HDA, or F_IS_HTML.  Defaulting to HTML.");
/*      */     }
/*      */ 
/* 1593 */     char[] prefix = null;
/* 1594 */     String encodingAttributeName = null;
/* 1595 */     boolean isIso = false;
/* 1596 */     if (isHda)
/*      */     {
/* 1598 */       prefix = new char[] { '?', 'h', 'd', 'a' };
/* 1599 */       encodingAttributeName = "jcharset";
/*      */     }
/* 1601 */     else if (isXml)
/*      */     {
/* 1603 */       prefix = new char[] { '?', 'x', 'm', 'l' };
/* 1604 */       encodingAttributeName = "encoding";
/* 1605 */       isIso = true;
/*      */     }
/*      */     else
/*      */     {
/* 1609 */       prefix = new char[] { 'm', 'e', 't', 'a' };
/* 1610 */       encodingAttributeName = "charset";
/* 1611 */       isIso = true;
/*      */     }
/* 1613 */     char[][] prefixTag = { prefix };
/*      */ 
/* 1615 */     bstream.mark(len);
/* 1616 */     len = bstream.read(temp, 0, len);
/*      */ 
/* 1619 */     boolean determinedEncoding = false;
/* 1620 */     encoding = FileUtils.checkForUnicodeEncoding(temp, 0, len);
/* 1621 */     if (encoding != null)
/*      */     {
/* 1623 */       determinedEncoding = true;
/*      */     }
/* 1625 */     if (!determinedEncoding)
/*      */     {
/* 1627 */       char[] charBuf = new char[temp.length];
/* 1628 */       for (int i = 0; i < len; ++i)
/*      */       {
/* 1630 */         charBuf[i] = (char)temp[i];
/*      */       }
/* 1632 */       Reader reader = new CharArrayReader(charBuf, 0, charBuf.length);
/*      */ 
/* 1634 */       CharArrayWriter outbuf = new CharArrayWriter();
/* 1635 */       ParseOutput parseOutput = new ParseOutput(2048);
/* 1636 */       parseOutput.m_writer = outbuf;
/*      */ 
/* 1638 */       int match = Parser.findHtmlPrefixTags(reader, parseOutput, prefixTag);
/* 1639 */       while (match >= 0)
/*      */       {
/* 1642 */         String tag = parseOutput.waitingBufferAsString().trim().toLowerCase();
/*      */ 
/* 1644 */         int index = tag.indexOf(encodingAttributeName);
/* 1645 */         if (index > 0)
/*      */         {
/* 1647 */           index = tag.indexOf(61, index + 1);
/* 1648 */           if (index > 0)
/*      */           {
/* 1650 */             int startIndex = index + 1;
/*      */ 
/* 1653 */             while (startIndex < tag.length())
/*      */             {
/* 1655 */               char ch = tag.charAt(startIndex);
/*      */ 
/* 1658 */               if ((ch == '\'') || (ch == '"'))
/*      */               {
/* 1660 */                 int endIndex = tag.indexOf("" + ch, startIndex + 1);
/* 1661 */                 pageEncoding = tag.substring(startIndex + 1, endIndex).trim();
/* 1662 */                 break;
/*      */               }
/*      */ 
/* 1665 */               if (ch > ' ')
/*      */               {
/*      */                 int endIndex;
/*      */                 int endIndex;
/* 1667 */                 if (isHtml)
/*      */                 {
/* 1669 */                   endIndex = StringUtils.findStoppingIndex(tag, startIndex, '\'', '"', ';');
/*      */                 }
/*      */                 else
/*      */                 {
/* 1673 */                   endIndex = StringUtils.findStoppingIndex(tag, startIndex, '?', '\000', '\000');
/*      */                 }
/*      */ 
/* 1676 */                 if (endIndex <= 0)
/*      */                   break;
/* 1678 */                 pageEncoding = tag.substring(startIndex, endIndex).trim(); break;
/*      */               }
/*      */ 
/* 1683 */               ++startIndex;
/*      */             }
/*      */           }
/*      */         }
/* 1687 */         match = Parser.findHtmlPrefixTags(reader, parseOutput, prefixTag);
/*      */       }
/* 1689 */       parseOutput.releaseBuffers();
/*      */     }
/*      */ 
/* 1692 */     bstream.reset();
/*      */ 
/* 1695 */     if (!determinedEncoding)
/*      */     {
/* 1697 */       if ((pageEncoding != null) && (pageEncoding.length() > 0))
/*      */       {
/* 1699 */         encoding = (isIso) ? DataSerializeUtils.getJavaEncoding(pageEncoding) : pageEncoding;
/*      */       }
/* 1703 */       else if (m_checkForAsciiEncoding)
/*      */       {
/* 1706 */         encoding = FileUtils.checkForASCIIEncoding(temp, 0, len);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1711 */     return encoding;
/*      */   }
/*      */ 
/*      */   public static void reportError(String msg)
/*      */   {
/* 1716 */     reportError(null, msg, null);
/*      */   }
/*      */ 
/*      */   public static void reportError(String section, String msg, Throwable t)
/*      */   {
/* 1721 */     if (!SystemUtils.isActiveTrace("localization"))
/*      */     {
/* 1723 */       t = null;
/*      */     }
/* 1725 */     if (m_reportedMessages.get(msg) != null)
/*      */       return;
/* 1727 */     Report.trace(section, msg, t);
/* 1728 */     m_reportedMessages.put(msg, msg);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1734 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98978 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.ResourceLoader
 * JD-Core Version:    0.5.4
 */