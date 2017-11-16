/*      */ package intradoc.tools.build;
/*      */ 
/*      */ import intradoc.common.CommonDataConversion;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.PathUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.io.HTTPDownloader;
/*      */ import intradoc.io.HTTPDownloader.State;
/*      */ import intradoc.io.HTTPDownloader.StateListener;
/*      */ import intradoc.io.zip.IdcZipEntry;
/*      */ import intradoc.io.zip.IdcZipFile;
/*      */ import intradoc.tools.common.JavaCompileManager;
/*      */ import intradoc.tools.common.ZipBuilder;
/*      */ import intradoc.tools.common.ZipBuilder.FileEntries;
/*      */ import intradoc.tools.utils.SimpleFileUtils;
/*      */ import intradoc.tools.utils.TextUtils;
/*      */ import intradoc.util.GenericTracingCallback;
/*      */ import intradoc.util.IdcException;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.MapUtils;
/*      */ import intradoc.util.PatternFilter;
/*      */ import intradoc.zip.IdcZipFunctions;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.FileReader;
/*      */ import java.io.FileWriter;
/*      */ import java.io.IOException;
/*      */ import java.security.MessageDigest;
/*      */ import java.text.DateFormat;
/*      */ import java.text.ParseException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Date;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class Module
/*      */   implements GenericTracingCallback
/*      */ {
/*      */   public static final int F_FORCE_FETCH = 1;
/*      */   public static final int F_FORCE_FETCH_CHECKS = 2;
/*      */   public static final int F_SKIP_LOCAL_FETCHES = 4;
/*      */   public static final int F_SKIP_REMOTE_FETCHES = 8;
/*      */   public static final int F_SKIP_EXTRACTS = 16;
/*      */   public static final int F_IS_EXECUTABLE = 64;
/*      */   public static final int F_IS_FOR_BUILD = 128;
/*      */   public static final int F_VALIDATE_FETCH_SOURCES = 256;
/*      */   public static final int F_SKIP_COMPONENT_CLASSPATH = 1;
/*      */   public static final int F_SKIP_STATECFGEXPORTEDVARS = 2;
/*      */   public static final int F_SKIP_DERIVED_PACKAGES = 4;
/*      */   public static final String DEPENDS_REQUIRED_FIELDNAMES = "source";
/*      */   public static final String DEPENDS_OPTIONAL_FIELDNAMES = "localPath,flags";
/*      */   public static final int INDEX_SOURCE = 0;
/*      */   public static final int INDEX_LOCALPATH = 1;
/*      */   public static final int INDEX_FLAGS = 2;
/*      */   public static final int NUM_FIELDS = 3;
/*      */   protected static String[] s_dependsRequiredFieldnames;
/*      */   protected static String[] s_dependsOptionalFieldnames;
/*      */   protected static boolean s_isInitialized;
/*      */   public BuildManager m_manager;
/*      */   public String m_moduleName;
/*      */   public String m_moduleDirname;
/*      */   public File m_moduleDir;
/*      */   public File m_javaDir;
/*      */   public Properties m_properties;
/*      */   public boolean m_isLoaded;
/*      */   public boolean m_hasJava;
/*      */   public boolean m_isExcludedFromBuild;
/*      */   public DataBinder m_buildConfig;
/*      */   public String[] m_requiredModules;
/*      */   protected List<FetchRule> m_fetchRules;
/*      */   protected List<ExtractRule> m_extractRules;
/*      */   protected DataResultSet m_fetchRulesTable;
/*      */   protected DataResultSet m_extractRulesTable;
/*      */   protected DataResultSet m_packageRulesTable;
/*      */   protected int[] m_fetchRulesIndices;
/*      */   protected int[] m_extractRulesIndices;
/*      */   protected PackageRule.Group m_packages;
/*      */   protected PackageRule.Group m_shiphomeRules;
/*      */ 
/*      */   protected static void staticInit()
/*      */   {
/*   99 */     if (s_isInitialized)
/*      */     {
/*  101 */       return;
/*      */     }
/*  103 */     s_dependsRequiredFieldnames = "source".split(",");
/*  104 */     s_dependsOptionalFieldnames = "localPath,flags".split(",");
/*  105 */     s_isInitialized = true;
/*      */   }
/*      */ 
/*      */   public void init(BuildManager manager, File moduleDir)
/*      */     throws IdcException
/*      */   {
/*  134 */     this.m_manager = manager;
/*  135 */     this.m_moduleDirname = moduleDir.getPath();
/*  136 */     this.m_moduleDir = moduleDir;
/*  137 */     this.m_moduleName = this.m_moduleDir.getName();
/*  138 */     this.m_isLoaded = false;
/*  139 */     File javaDir = this.m_javaDir = new File(moduleDir, "java");
/*  140 */     this.m_hasJava = javaDir.exists();
/*      */ 
/*  143 */     Properties properties = this.m_properties = new IdcProperties(manager.m_env.m_properties);
/*      */ 
/*  146 */     properties.put("ModuleName", this.m_moduleName);
/*  147 */     properties.put("ModuleDir", this.m_moduleDirname);
/*  148 */     properties.put("COMPONENT_DIR", this.m_moduleDirname);
/*      */   }
/*      */ 
/*      */   public void reload()
/*      */     throws IdcException
/*      */   {
/*  158 */     BuildManager manager = this.m_manager;
/*  159 */     BuildEnvironment env = manager.m_env;
/*  160 */     String moduleName = this.m_moduleName;
/*  161 */     DataBinder buildConfig = this.m_buildConfig;
/*  162 */     this.m_requiredModules = null;
/*  163 */     if ((env == null) || (buildConfig == null))
/*      */       return;
/*  165 */     ExecutionContext context = env.m_context;
/*      */ 
/*  167 */     String requiredModulesString = buildConfig.getLocal("RequiredModules");
/*  168 */     if ((requiredModulesString != null) && (requiredModulesString.length() > 0))
/*      */     {
/*  170 */       String[] requiredModules = requiredModulesString.split(",");
/*  171 */       Arrays.sort(requiredModules);
/*  172 */       this.m_requiredModules = requiredModules;
/*      */     }
/*      */ 
/*  176 */     Properties props = this.m_properties;
/*  177 */     Properties binderProps = buildConfig.getLocalData();
/*  178 */     Set stringKeysSet = binderProps.stringPropertyNames();
/*  179 */     int numKeys = stringKeysSet.size();
/*  180 */     String[] stringKeys = new String[numKeys];
/*  181 */     stringKeysSet.toArray(stringKeys);
/*  182 */     Arrays.sort(stringKeys);
/*  183 */     int pathFlags = PathUtils.F_KEEP_UNKNOWN_VARS;
/*  184 */     for (int s = 0; s < numKeys; ++s)
/*      */     {
/*  186 */       String key = stringKeys[s];
/*  187 */       String value = binderProps.getProperty(key);
/*  188 */       props.setProperty(key, value);
/*      */     }
/*  190 */     String propertyNamesString = buildConfig.getLocal("GlobalProperties");
/*  191 */     String[] propertyNames = (propertyNamesString != null) ? propertyNamesString.split(",") : null;
/*      */ 
/*  193 */     if (propertyNames != null)
/*      */     {
/*  195 */       Map environment = env.m_environment;
/*  196 */       for (String propertyName : propertyNames)
/*      */       {
/*  198 */         String value = (String)environment.get(propertyName);
/*  199 */         if (value == null)
/*      */           continue;
/*  201 */         props.setProperty(propertyName, value);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  207 */     int maxIterations = stringKeys.length;
/*      */     boolean hasSubstitutions;
/*      */     do
/*      */     {
/*  210 */       hasSubstitutions = false;
/*  211 */       for (int s = 0; s < numKeys; ++s)
/*      */       {
/*  213 */         String key = stringKeys[s];
/*  214 */         String oldValue = props.getProperty(key);
/*  215 */         String newValue = PathUtils.substitutePathVariables(oldValue, props, null, pathFlags, context);
/*  216 */         if (oldValue.equals(newValue))
/*      */           continue;
/*  218 */         props.setProperty(key, newValue);
/*  219 */         hasSubstitutions = true;
/*      */       }
/*      */     }
/*      */ 
/*  223 */     while ((hasSubstitutions) && (maxIterations-- > 0));
/*      */ 
/*  226 */     Properties envProps = env.m_properties;
/*  227 */     if ((envProps != null) && (propertyNames != null))
/*      */     {
/*  229 */       Map environment = env.m_environment;
/*  230 */       for (String propertyName : propertyNames)
/*      */       {
/*  233 */         String value = props.getProperty(propertyName);
/*  234 */         if (value == null)
/*      */           continue;
/*  236 */         envProps.setProperty(propertyName, value);
/*  237 */         boolean wasFromEnvironment = value.equals(environment.get(propertyName));
/*  238 */         report(6, new Object[] { "(", moduleName, ") ", (wasFromEnvironment) ? "setting " : "using ", propertyName, Character.valueOf('='), value });
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  244 */     DataBinder binder = env.m_binder;
/*  245 */     String resultSetNamesString = buildConfig.getLocal("GlobalResultSets");
/*  246 */     if ((binder != null) && (resultSetNamesString != null) && (resultSetNamesString.length() > 0))
/*      */     {
/*  248 */       String[] resultSetNames = resultSetNamesString.split(",");
/*  249 */       for (String resultSetName : resultSetNames)
/*      */       {
/*  251 */         ResultSet rset = buildConfig.getResultSet(resultSetName);
/*  252 */         binder.addResultSet(resultSetName, rset);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  257 */     if (this.m_isLoaded)
/*      */       return;
/*  259 */     Map modules = env.m_modules;
/*  260 */     StringBuilder sb = new StringBuilder(moduleName);
/*  261 */     sb.append(':');
/*  262 */     int sbLength = sb.length();
/*  263 */     String submodulesString = buildConfig.getLocal("SubModules");
/*  264 */     if ((submodulesString != null) && (submodulesString.length() > 0))
/*      */     {
/*  266 */       File moduleDir = this.m_moduleDir;
/*  267 */       String[] submodules = submodulesString.split(",");
/*  268 */       for (String submodule : submodules)
/*      */       {
/*  270 */         sb.setLength(sbLength);
/*  271 */         sb.append(submodule);
/*  272 */         String subModuleName = sb.toString();
/*  273 */         File subDir = new File(moduleDir, submodule);
/*  274 */         Module module = createAndLoadModule(manager, subDir);
/*  275 */         module.m_moduleName = subModuleName;
/*  276 */         if (modules.containsKey(subModuleName))
/*      */         {
/*  278 */           report(4, new Object[] { "Module ", subModuleName, " already exists" });
/*      */         }
/*      */         else
/*  281 */           modules.put(subModuleName, module);
/*      */       }
/*      */     }
/*  284 */     this.m_isLoaded = true;
/*      */   }
/*      */ 
/*      */   public PackageRule.Group createPackageListFromResultSet(DataResultSet rset, int sourceIndex, int targetIndex, String defaultSource)
/*      */     throws IdcException
/*      */   {
/*  293 */     BuildEnvironment env = this.m_manager.m_env;
/*  294 */     ExecutionContext context = env.m_context;
/*  295 */     Properties props = this.m_properties;
/*  296 */     int pathUtilsFlags = PathUtils.F_KEEP_UNKNOWN_VARS;
/*  297 */     PackageRule.Group rules = new PackageRule.Group();
/*  298 */     int stepIndex = rset.getFieldInfoIndex("step");
/*  299 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*      */       String sourcePath;
/*  302 */       if (sourceIndex >= 0)
/*      */       {
/*  304 */         String sourcePath = rset.getStringValue(sourceIndex);
/*  305 */         sourcePath = PathUtils.substitutePathVariables(sourcePath, props, null, pathUtilsFlags, context);
/*      */       }
/*      */       else
/*      */       {
/*  309 */         sourcePath = defaultSource;
/*      */       }
/*      */ 
/*  312 */       String targetPath = rset.getStringValue(targetIndex);
/*  313 */       targetPath = PathUtils.substitutePathVariables(targetPath, props, null, pathUtilsFlags, context);
/*      */ 
/*  315 */       PatternFilter filter = createPatternFilterFromResultSet(rset);
/*  316 */       PackageRule.Item item = rules.add(targetPath, sourcePath, filter);
/*  317 */       if (stepIndex < 0)
/*      */         continue;
/*  319 */       item.m_stepName = rset.getStringValue(stepIndex);
/*      */     }
/*      */ 
/*  323 */     return rules;
/*      */   }
/*      */ 
/*      */   protected PatternFilter createPatternFilterFromResultSet(DataResultSet rset)
/*      */     throws IdcException
/*      */   {
/*  336 */     PatternFilter filter = null;
/*  337 */     String filterTableNamesString = null;
/*  338 */     boolean hasFilterTableNames = false; boolean hasFilters = false; boolean hasRegexFilters = false;
/*  339 */     int filterIndex = rset.getFieldInfoIndex("wildcardFilter");
/*  340 */     int filterRegexIndex = rset.getFieldInfoIndex("regexFilter");
/*  341 */     int filterTableIndex = rset.getFieldInfoIndex("filterTable");
/*  342 */     if (filterTableIndex < 0)
/*      */     {
/*  344 */       filterTableIndex = rset.getFieldInfoIndex("filterTables");
/*      */     }
/*  346 */     if (filterTableIndex >= 0)
/*      */     {
/*  348 */       filterTableNamesString = rset.getStringValue(filterTableIndex);
/*      */     }
/*  350 */     if ((filterTableNamesString == null) || (filterTableNamesString.length() == 0))
/*      */     {
/*  352 */       filterTableNamesString = "CommonFilter";
/*      */     }
/*      */     else
/*      */     {
/*  356 */       hasFilterTableNames = true;
/*      */     }
/*  358 */     String filterString = null; String filterRegexString = null;
/*  359 */     if (filterRegexIndex >= 0)
/*      */     {
/*  361 */       filterRegexString = rset.getStringValue(filterRegexIndex);
/*  362 */       if ((filterRegexString != null) && (filterRegexString.length() > 0))
/*      */       {
/*  364 */         hasFilters = hasRegexFilters = 1;
/*      */       }
/*      */     }
/*  367 */     if ((!hasFilters) && (filterIndex >= 0))
/*      */     {
/*  369 */       filterString = rset.getStringValue(filterIndex);
/*  370 */       if ((filterString != null) && (filterString.length() > 0))
/*      */       {
/*  372 */         hasFilters = true;
/*      */       }
/*      */     }
/*  375 */     if ((!hasFilters) && (!hasFilterTableNames))
/*      */     {
/*  377 */       filterString = "+**";
/*      */     }
/*  379 */     hasFilters = (filterString != null) && (filterString.length() > 0);
/*  380 */     String[] filterTableNames = filterTableNamesString.split(",");
/*  381 */     for (String filterTableName : filterTableNames)
/*      */     {
/*  383 */       if (filterTableName.length() <= 0)
/*      */         continue;
/*  385 */       DataResultSet table = getResultSet(filterTableName);
/*  386 */       if (table == null)
/*      */       {
/*  388 */         throw new DataException(null, "csResultSetMissing", new Object[] { filterTableName });
/*      */       }
/*  390 */       boolean isRegex = true;
/*  391 */       String filterColumn = "regexFilter";
/*  392 */       if (table.getFieldInfoIndex(filterColumn) < 0)
/*      */       {
/*  394 */         isRegex = false;
/*  395 */         filterColumn = "wildcardFilter";
/*      */       }
/*  397 */       String[] list = ResultSetUtils.createFilteredStringArrayForColumn(table, filterColumn, null, null, false, false);
/*      */ 
/*  399 */       if (isRegex)
/*      */       {
/*  401 */         filter = TextUtils.addRegexArrayToPatternFilter(filter, list);
/*      */       }
/*      */       else
/*      */       {
/*  405 */         filter = TextUtils.addWildcardArrayToPatternFilter(filter, list);
/*      */       }
/*      */     }
/*      */ 
/*  409 */     if (hasRegexFilters)
/*      */     {
/*  411 */       String[] regexStrings = filterRegexString.split(",");
/*  412 */       filter = TextUtils.addRegexArrayToPatternFilter(filter, regexStrings);
/*      */     }
/*  414 */     else if (hasFilters)
/*      */     {
/*  416 */       filter = TextUtils.addWildcardsToPatternFilter(filter, filterString);
/*      */     }
/*  418 */     return filter;
/*      */   }
/*      */ 
/*      */   protected DataResultSet getResultSet(String name)
/*      */   {
/*  423 */     DataBinder binder = this.m_buildConfig;
/*  424 */     if (binder != null)
/*      */     {
/*  426 */       DataResultSet rset = (DataResultSet)binder.getResultSet(name);
/*  427 */       if (rset != null)
/*      */       {
/*  429 */         return rset;
/*      */       }
/*      */     }
/*  432 */     binder = this.m_manager.m_env.m_binder;
/*  433 */     if (binder != null)
/*      */     {
/*  435 */       DataResultSet rset = (DataResultSet)binder.getResultSet(name);
/*  436 */       if (rset != null)
/*      */       {
/*  438 */         return rset;
/*      */       }
/*      */     }
/*  441 */     return null;
/*      */   }
/*      */ 
/*      */   public List<FetchRule> loadFetchRules()
/*      */     throws IdcException
/*      */   {
/*  454 */     List rules = this.m_fetchRules;
/*  455 */     if (rules != null)
/*      */     {
/*  457 */       return rules;
/*      */     }
/*  459 */     DataBinder buildConfig = this.m_buildConfig;
/*  460 */     DataResultSet rulesTable = this.m_fetchRulesTable;
/*  461 */     if ((rulesTable == null) && (buildConfig != null))
/*      */     {
/*  463 */       rulesTable = this.m_fetchRulesTable = (DataResultSet)buildConfig.getResultSet("FetchRules");
/*      */     }
/*  465 */     this.m_fetchRulesIndices = null;
/*  466 */     if (rulesTable == null)
/*      */     {
/*  468 */       return null;
/*      */     }
/*  470 */     if (!s_isInitialized)
/*      */     {
/*  472 */       staticInit();
/*      */     }
/*  474 */     int[] indices = new int[3];
/*  475 */     int i = 0;
/*  476 */     FieldInfo[] requiredFields = ResultSetUtils.createInfoList(rulesTable, s_dependsRequiredFieldnames, true);
/*      */ 
/*  478 */     for (int j = 0; j < requiredFields.length; ++j)
/*      */     {
/*  480 */       indices[(i++)] = requiredFields[j].m_index;
/*      */     }
/*  482 */     FieldInfo[] optionalFields = ResultSetUtils.createInfoList(rulesTable, s_dependsOptionalFieldnames, false);
/*      */ 
/*  484 */     for (int j = 0; j < optionalFields.length; ++j)
/*      */     {
/*  486 */       indices[(i++)] = optionalFields[j].m_index;
/*      */     }
/*      */ 
/*  489 */     int sourceIndex = indices[0]; int pathIndex = indices[1];
/*  490 */     int flagsIndex = indices[2];
/*  491 */     rules = new ArrayList();
/*  492 */     for (rulesTable.first(); rulesTable.isRowPresent(); rulesTable.next())
/*      */     {
/*  494 */       String source = rulesTable.getStringValue(sourceIndex);
/*  495 */       String path = (pathIndex < 0) ? "" : rulesTable.getStringValue(pathIndex);
/*  496 */       String flagsString = (flagsIndex < 0) ? "" : rulesTable.getStringValue(flagsIndex);
/*  497 */       FetchRule rule = createFetchRule(source, path, flagsString);
/*  498 */       rules.add(rule);
/*      */     }
/*  500 */     this.m_fetchRules = rules;
/*  501 */     return rules;
/*      */   }
/*      */ 
/*      */   protected FetchRule createFetchRule(String src, String path, String flags)
/*      */     throws IdcException
/*      */   {
/*  520 */     FetchRule rule = new FetchRule();
/*  521 */     ExecutionContext context = this.m_manager.m_env.m_context;
/*  522 */     Properties props = this.m_properties;
/*  523 */     int pathUtilsFlags = PathUtils.F_KEEP_UNKNOWN_VARS;
/*  524 */     String moduleDirname = this.m_moduleDirname;
/*      */ 
/*  526 */     if (src != null)
/*      */     {
/*  528 */       rule.m_source = src;
/*  529 */       src = PathUtils.substitutePathVariables(src, props, null, pathUtilsFlags, context);
/*  530 */       rule.m_sourcePath = src;
/*  531 */       if (src.startsWith("http://"))
/*      */       {
/*  533 */         rule.m_isSourceRemote = true;
/*      */       }
/*  535 */       else if (FileUtils.isAbsolutePath(src))
/*      */       {
/*  537 */         rule.m_sourceFile = new File(src);
/*      */       }
/*      */       else
/*      */       {
/*  541 */         rule.m_sourceFile = new File(moduleDirname, src);
/*      */       }
/*      */     }
/*      */ 
/*  545 */     if (path != null)
/*      */     {
/*  547 */       rule.m_localPath = path;
/*  548 */       path = PathUtils.substitutePathVariables(path, props, null, pathUtilsFlags, context);
/*  549 */       String targetPath = (FileUtils.isAbsolutePath(path)) ? path : new StringBuilder().append(moduleDirname).append('/').append(path).toString();
/*      */ 
/*  551 */       rule.m_targetPath = targetPath;
/*  552 */       rule.m_targetFile = new File(targetPath);
/*      */     }
/*      */ 
/*  555 */     if (flags != null)
/*      */     {
/*  557 */       Map mapFlags = rule.m_flags = MapUtils.fillMapFromOptionsString(null, flags);
/*  558 */       rule.m_isForBuild = MapUtils.getBoolValueFromMap(mapFlags, "isForBuild", false);
/*      */     }
/*      */ 
/*  561 */     loadFetchRule(rule);
/*  562 */     return rule;
/*      */   }
/*      */ 
/*      */   protected void loadFetchRule(FetchRule rule)
/*      */   {
/*  573 */     BuildManager manager = this.m_manager;
/*  574 */     DataResultSet fetchedResources = manager.m_fetchedResources;
/*  575 */     int[] fieldIndices = manager.m_fetchedResourcesIndices;
/*  576 */     int sourceIndex = fieldIndices[0];
/*  577 */     int md5Index = fieldIndices[1];
/*  578 */     int sizeIndex = fieldIndices[2];
/*  579 */     int timestampIndex = fieldIndices[3];
/*  580 */     String source = rule.m_source;
/*  581 */     List fetchedRow = fetchedResources.findRow(sourceIndex, source, 0, 0);
/*  582 */     String md5String = (fetchedRow == null) ? null : (String)fetchedRow.get(md5Index);
/*  583 */     String sz = (fetchedRow == null) ? null : (String)fetchedRow.get(sizeIndex);
/*  584 */     String ts = (fetchedRow == null) ? null : (String)fetchedRow.get(timestampIndex);
/*      */ 
/*  586 */     if (md5String != null)
/*      */     {
/*  588 */       if (md5String.length() == 32)
/*      */       {
/*  590 */         byte[] bytes = rule.m_md5 = new byte[16];
/*  591 */         int b = 0; for (int s = 0; b < 16; ++b)
/*      */         {
/*  593 */           char ch = md5String.charAt(s++);
/*  594 */           byte nibble = (ch < '') ? NumberUtils.getHexValue((byte)ch) : -1;
/*  595 */           if (nibble < 0)
/*      */           {
/*  597 */             rule.m_md5 = null;
/*  598 */             break;
/*      */           }
/*  600 */           int value = nibble << 4;
/*  601 */           ch = md5String.charAt(s++);
/*  602 */           nibble = (ch < '') ? NumberUtils.getHexValue((byte)ch) : -1;
/*  603 */           if (nibble < 0)
/*      */           {
/*  605 */             rule.m_md5 = null;
/*  606 */             break;
/*      */           }
/*  608 */           value |= nibble;
/*  609 */           bytes[b] = (byte)value;
/*      */         }
/*      */       }
/*  612 */       else if (md5String.length() == 24)
/*      */       {
/*  614 */         rule.m_md5 = CommonDataConversion.uudecode(md5String, null);
/*      */       }
/*      */     }
/*      */ 
/*  618 */     if (sz != null)
/*      */     {
/*  620 */       rule.m_fileLength = NumberUtils.parseLong(sz, 0L);
/*      */     }
/*      */ 
/*  623 */     if (ts == null)
/*      */       return;
/*      */     try
/*      */     {
/*  627 */       Date date = BuildEnvironment.s_iso8601.parse(ts);
/*  628 */       rule.m_lastModified = date.getTime();
/*      */     }
/*      */     catch (ParseException pe)
/*      */     {
/*  632 */       rule.m_lastModified = NumberUtils.parseLong(ts, 0L);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void saveFetchRule(FetchRule rule)
/*      */     throws IdcException
/*      */   {
/*  646 */     BuildManager manager = this.m_manager;
/*  647 */     DataResultSet fetchedResources = manager.m_fetchedResources;
/*  648 */     int[] fieldIndices = manager.m_fetchedResourcesIndices;
/*  649 */     int sourceIndex = fieldIndices[0];
/*  650 */     int md5Index = fieldIndices[1];
/*  651 */     int sizeIndex = fieldIndices[2];
/*  652 */     int timestampIndex = fieldIndices[3];
/*  653 */     String source = rule.m_source;
/*  654 */     Vector fetchedRow = fetchedResources.findRow(sourceIndex, source);
/*  655 */     if (fetchedRow == null)
/*      */     {
/*  657 */       fetchedRow = fetchedResources.createEmptyRow();
/*  658 */       fetchedResources.addRow(fetchedRow);
/*  659 */       fetchedRow.set(sourceIndex, source);
/*      */     }
/*      */ 
/*  662 */     String HEX_DIGITS = "0123456789abcdef";
/*  663 */     char[] md5chars = new char[32];
/*  664 */     byte[] md5bytes = rule.m_md5;
/*  665 */     if (md5bytes != null)
/*      */     {
/*  667 */       if (md5bytes.length != 16)
/*      */       {
/*  669 */         throw new ServiceException(null, new StringBuilder().append("MD5 checksum length mismatch: ").append(md5bytes.length).toString(), new Object[0]);
/*      */       }
/*  671 */       int b = 15; for (int c = 31; b >= 0; --b)
/*      */       {
/*  673 */         byte value = md5bytes[b];
/*  674 */         int nibble = value & 0xF;
/*  675 */         md5chars[(c--)] = "0123456789abcdef".charAt(nibble);
/*  676 */         nibble = value >> 4 & 0xF;
/*  677 */         md5chars[(c--)] = "0123456789abcdef".charAt(nibble);
/*      */       }
/*  679 */       String md5 = new String(md5chars);
/*  680 */       fetchedRow.set(md5Index, md5);
/*      */     }
/*      */ 
/*  683 */     long fileLength = rule.m_fileLength;
/*  684 */     String fileLengthString = (fileLength <= 0L) ? "" : Long.toString(fileLength);
/*  685 */     fetchedRow.set(sizeIndex, fileLengthString);
/*      */ 
/*  687 */     DateFormat iso8601 = BuildEnvironment.s_iso8601;
/*  688 */     long lastModified = rule.m_lastModified;
/*      */     String timestamp;
/*      */     String timestamp;
/*  690 */     if (lastModified == 0L)
/*      */     {
/*  692 */       timestamp = "";
/*      */     }
/*      */     else
/*      */     {
/*  696 */       Date date = new Date(lastModified);
/*  697 */       timestamp = iso8601.format(date);
/*      */     }
/*  699 */     fetchedRow.set(timestampIndex, timestamp);
/*      */   }
/*      */ 
/*      */   public List<ExtractRule> loadExtractRules()
/*      */     throws IdcException
/*      */   {
/*  710 */     List rules = this.m_extractRules;
/*  711 */     if (rules != null)
/*      */     {
/*  713 */       return rules;
/*      */     }
/*  715 */     DataBinder buildConfig = this.m_buildConfig;
/*  716 */     DataResultSet rulesTable = this.m_extractRulesTable;
/*  717 */     if ((rulesTable == null) && (buildConfig != null))
/*      */     {
/*  719 */       rulesTable = this.m_extractRulesTable = (DataResultSet)buildConfig.getResultSet("ExtractRules");
/*      */     }
/*  721 */     this.m_extractRulesIndices = null;
/*  722 */     if (rulesTable == null)
/*      */     {
/*  724 */       return null;
/*      */     }
/*  726 */     if (!s_isInitialized)
/*      */     {
/*  728 */       staticInit();
/*      */     }
/*  730 */     int[] indices = new int[3];
/*  731 */     int i = 0;
/*  732 */     FieldInfo[] requiredFields = ResultSetUtils.createInfoList(rulesTable, s_dependsRequiredFieldnames, true);
/*      */ 
/*  734 */     for (int j = 0; j < requiredFields.length; ++j)
/*      */     {
/*  736 */       indices[(i++)] = requiredFields[j].m_index;
/*      */     }
/*  738 */     FieldInfo[] optionalFields = ResultSetUtils.createInfoList(rulesTable, s_dependsOptionalFieldnames, false);
/*      */ 
/*  740 */     for (int j = 0; j < optionalFields.length; ++j)
/*      */     {
/*  742 */       indices[(i++)] = optionalFields[j].m_index;
/*      */     }
/*  744 */     this.m_extractRulesIndices = indices;
/*      */ 
/*  746 */     int sourceIndex = indices[0]; int pathIndex = indices[1];
/*  747 */     int flagsIndex = indices[2];
/*  748 */     BuildEnvironment env = this.m_manager.m_env;
/*  749 */     ExecutionContext context = env.m_context;
/*  750 */     Properties props = this.m_properties;
/*  751 */     int pathUtilsFlags = PathUtils.F_KEEP_UNKNOWN_VARS;
/*  752 */     String moduleDirname = this.m_moduleDirname;
/*  753 */     rules = new ArrayList();
/*  754 */     for (rulesTable.first(); rulesTable.isRowPresent(); rulesTable.next())
/*      */     {
/*  756 */       ExtractRule rule = new ExtractRule();
/*  757 */       rules.add(rule);
/*      */ 
/*  759 */       String source = rule.m_source = rulesTable.getStringValue(sourceIndex);
/*  760 */       source = PathUtils.substitutePathVariables(source, props, null, pathUtilsFlags, context);
/*  761 */       if (FileUtils.isAbsolutePath(source))
/*      */       {
/*  763 */         rule.m_sourceFile = new File(source);
/*      */       }
/*      */       else
/*      */       {
/*  767 */         rule.m_sourceFile = new File(moduleDirname, source);
/*      */       }
/*      */ 
/*  770 */       String path = (pathIndex < 0) ? "" : rulesTable.getStringValue(pathIndex);
/*  771 */       rule.m_localPath = path;
/*  772 */       path = PathUtils.substitutePathVariables(path, props, null, pathUtilsFlags, context);
/*  773 */       if (FileUtils.isAbsolutePath(path))
/*      */       {
/*  775 */         rule.m_targetFile = new File(path);
/*      */       }
/*      */       else
/*      */       {
/*  779 */         rule.m_targetFile = new File(moduleDirname, path);
/*  780 */         path = new StringBuilder().append(moduleDirname).append('/').append(path).toString();
/*      */       }
/*  782 */       rule.m_targetFilename = path;
/*  783 */       rule.m_isTargetDirectory = path.endsWith("/");
/*      */ 
/*  785 */       String flagsString = (flagsIndex < 0) ? "" : rulesTable.getStringValue(flagsIndex);
/*  786 */       Map flagsMap = rule.m_flags = MapUtils.fillMapFromOptionsString(null, flagsString);
/*  787 */       rule.m_pathStripCount = MapUtils.getIntValueFromMap(flagsMap, "pathStripCount", 0);
/*      */ 
/*  789 */       rule.m_filter = createPatternFilterFromResultSet(rulesTable);
/*      */     }
/*  791 */     this.m_extractRules = rules;
/*  792 */     return rules;
/*      */   }
/*      */ 
/*      */   public PackageRule.Group loadPackageRules()
/*      */     throws IdcException
/*      */   {
/*  803 */     if (this.m_packages != null)
/*      */     {
/*  805 */       return this.m_packages;
/*      */     }
/*  807 */     this.m_packages = null;
/*  808 */     BuildEnvironment env = this.m_manager.m_env;
/*  809 */     DataBinder buildConfig = this.m_buildConfig;
/*  810 */     DataResultSet packageRules = this.m_packageRulesTable;
/*  811 */     if ((packageRules == null) && (buildConfig != null))
/*      */     {
/*  813 */       packageRules = this.m_packageRulesTable = (DataResultSet)buildConfig.getResultSet("PackageRules");
/*      */     }
/*  815 */     if (packageRules == null)
/*      */     {
/*  817 */       PackageRule.Group packages = new PackageRule.Group();
/*  818 */       if (this.m_hasJava)
/*      */       {
/*  820 */         String targetPath = new StringBuilder().append("classes-").append(this.m_moduleName).append(".jar").toString();
/*  821 */         packages.add(targetPath, "classes", env.m_classFilenameFilter);
/*      */       }
/*  823 */       this.m_packages = packages;
/*  824 */       return packages;
/*      */     }
/*      */ 
/*  827 */     int indexLocalPath = ResultSetUtils.getIndexMustExist(packageRules, "targetPackage");
/*  828 */     int indexSource = packageRules.getFieldInfoIndex("source");
/*  829 */     return this.m_packages = createPackageListFromResultSet(packageRules, indexSource, indexLocalPath, "classes");
/*      */   }
/*      */ 
/*      */   public PackageRule.Group loadShiphomeRules()
/*      */     throws IdcException
/*      */   {
/*  840 */     PackageRule.Group packages = this.m_shiphomeRules;
/*  841 */     if (packages != null)
/*      */     {
/*  843 */       return packages;
/*      */     }
/*  845 */     DataBinder buildConfig = this.m_buildConfig;
/*  846 */     if (buildConfig == null)
/*      */     {
/*  848 */       return null;
/*      */     }
/*  850 */     DataResultSet rules = (DataResultSet)buildConfig.getResultSet("ShiphomeRules");
/*  851 */     if (rules == null)
/*      */     {
/*  853 */       return null;
/*      */     }
/*  855 */     int sourceIndex = ResultSetUtils.getIndexMustExist(rules, "source");
/*  856 */     int targetIndex = ResultSetUtils.getIndexMustExist(rules, "target");
/*  857 */     packages = this.m_shiphomeRules = createPackageListFromResultSet(rules, sourceIndex, targetIndex, null);
/*  858 */     return packages;
/*      */   }
/*      */ 
/*      */   public void addToLabelManifest()
/*      */     throws IdcException
/*      */   {
/*  868 */     DataBinder buildConfig = this.m_buildConfig;
/*  869 */     if (buildConfig == null)
/*      */     {
/*  871 */       return;
/*      */     }
/*  873 */     DataResultSet manifestSet = (DataResultSet)buildConfig.getResultSet("LabelManifest");
/*  874 */     if (manifestSet == null)
/*      */     {
/*  876 */       return;
/*      */     }
/*  878 */     List manifest = this.m_manager.m_env.m_labelManifest;
/*  879 */     manifest.add("");
/*  880 */     manifest.add(new StringBuilder().append("### From: ").append(this.m_moduleName).toString());
/*  881 */     Properties props = this.m_properties;
/*  882 */     ExecutionContext context = this.m_manager.m_env.m_context;
/*  883 */     int flags = PathUtils.F_KEEP_UNKNOWN_VARS;
/*  884 */     for (manifestSet.first(); manifestSet.isRowPresent(); manifestSet.next())
/*      */     {
/*  886 */       String line = manifestSet.getStringValue(0);
/*  887 */       String substitutedLine = PathUtils.substitutePathVariables(line, props, null, flags, context);
/*  888 */       manifest.add(substitutedLine);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void appendClasspathTo(List<String> classpath, int flags)
/*      */     throws IdcException
/*      */   {
/*      */   }
/*      */ 
/*      */   protected List<String> appendPathElementsStringTo(List<String> paths, String pathString)
/*      */     throws IdcException
/*      */   {
/*  914 */     Properties props = this.m_properties;
/*  915 */     ExecutionContext context = this.m_manager.m_env.m_context;
/*  916 */     int pathUtilsFlags = PathUtils.F_VARS_MUST_EXIST;
/*  917 */     File moduleDir = this.m_moduleDir;
/*      */ 
/*  920 */     pathString = pathString.replace(';', ':');
/*  921 */     List pathList = StringUtils.parseArray(pathString, ':', '^');
/*  922 */     int numPathElements = pathList.size();
/*  923 */     for (int p = 0; p < numPathElements; ++p)
/*      */     {
/*  925 */       String pathElement = (String)pathList.get(p);
/*  926 */       pathElement = PathUtils.substitutePathVariables(pathElement, props, null, pathUtilsFlags, context);
/*  927 */       if (pathElement.length() <= 0)
/*      */         continue;
/*  929 */       if (!FileUtils.isAbsolutePath(pathElement))
/*      */       {
/*  931 */         File file = new File(moduleDir, pathElement);
/*  932 */         pathElement = file.getPath();
/*      */       }
/*  934 */       if (paths != null)
/*      */       {
/*  936 */         paths.add(pathElement);
/*      */       }
/*      */       else
/*      */       {
/*  940 */         pathList.set(p, pathElement);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  945 */     return (paths != null) ? paths : pathList;
/*      */   }
/*      */ 
/*      */   public boolean buildAndPackage(File targetDir)
/*      */     throws IdcException
/*      */   {
/*  957 */     compileJava();
/*  958 */     buildPackages();
/*      */ 
/*  960 */     return false;
/*      */   }
/*      */ 
/*      */   public void buildPackages()
/*      */     throws IdcException
/*      */   {
/*  970 */     PackageRule.Group packages = loadPackageRules();
/*  971 */     buildPackagesFromList(packages, this.m_moduleDir);
/*      */   }
/*      */ 
/*      */   public void buildPackagesFromList(PackageRule.Group packages, File defaultSourceDir) throws IdcException
/*      */   {
/*  976 */     File moduleDir = this.m_moduleDir;
/*  977 */     GenericTracingCallback trace = this.m_manager.m_env.m_trace;
/*  978 */     long lastTime = System.currentTimeMillis();
/*  979 */     for (PackageRule pkg : packages)
/*      */     {
/*  981 */       if (!pkg.m_isBundled)
/*      */       {
/*  983 */         trace.report(4, new Object[] { "target package has unknown file extension: ", pkg });
/*      */       }
/*  985 */       File zipfile = pkg.getTargetFile(moduleDir);
/*  986 */       if (trace != null)
/*      */       {
/*  988 */         trace.report(7, new Object[] { "updating ", zipfile.getPath(), " ..." });
/*      */       }
/*  990 */       File targetDir = zipfile.getParentFile();
/*  991 */       targetDir.mkdirs();
/*  992 */       String zipfilename = zipfile.getName();
/*  993 */       ZipBuilder zip = new ZipBuilder(targetDir, zipfilename);
/*      */ 
/*  995 */       int numSources = pkg.m_items.size();
/*  996 */       ZipBuilder.FileEntries[] entries = new ZipBuilder.FileEntries[numSources];
/*  997 */       int s = 0;
/*  998 */       for (PackageRule.Item item : pkg.m_items)
/*      */       {
/* 1000 */         String sourceDirname = item.m_dirname;
/* 1001 */         File sourceDir = null;
/* 1002 */         if (!FileUtils.isAbsolutePath(sourceDirname))
/*      */         {
/* 1004 */           sourceDir = defaultSourceDir;
/*      */         }
/* 1006 */         sourceDir = new File(sourceDir, sourceDirname);
/* 1007 */         PatternFilter filter = item.m_filter;
/* 1008 */         List filenames = SimpleFileUtils.scanFilesFiltered(sourceDir, filter, null, null);
/* 1009 */         if ((filenames.size() == 0) && 
/* 1011 */           (trace != null))
/*      */         {
/* 1013 */           trace.report(4, new Object[] { "file scan filter returned no files in ", sourceDirname, "\nfilter:\n", filter });
/*      */         }
/*      */         ZipBuilder tmp325_323 = zip; tmp325_323.getClass(); entries[(s++)] = new ZipBuilder.FileEntries(tmp325_323, sourceDir, filenames, item.m_entryPrefix);
/*      */       }
/* 1019 */       zip.init();
/* 1020 */       int numFilesUpdated = zip.update(true, entries);
/* 1021 */       long now = System.currentTimeMillis();
/* 1022 */       if (trace != null)
/*      */       {
/* 1024 */         long time = now - lastTime;
/* 1025 */         int numEntries = zip.m_zip.m_entries.size();
/* 1026 */         trace.report(6, new Object[] { "updated ", Integer.valueOf(numFilesUpdated), " of ", Integer.valueOf(numEntries), " entries (", Long.valueOf(time), " ms) in ", pkg });
/*      */       }
/*      */ 
/* 1029 */       lastTime = now;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void clean()
/*      */     throws IdcException
/*      */   {
/* 1041 */     BuildEnvironment env = this.m_manager.m_env;
/* 1042 */     File moduleDir = this.m_moduleDir;
/* 1043 */     GenericTracingCallback trace = env.m_trace;
/* 1044 */     PackageRule.Group packages = loadPackageRules();
/* 1045 */     for (PackageRule pkg : packages)
/*      */     {
/* 1047 */       File targetFile = pkg.getTargetFile(moduleDir);
/* 1048 */       if (trace != null)
/*      */       {
/* 1050 */         trace.report(7, new Object[] { "deleting ", targetFile.getPath() });
/*      */       }
/* 1052 */       targetFile.delete();
/*      */     }
/* 1054 */     if (!this.m_hasJava)
/*      */       return;
/* 1056 */     File classesDir = new File(this.m_moduleDir, "classes");
/* 1057 */     if (trace != null)
/*      */     {
/* 1059 */       trace.report(7, new Object[] { "deleting ", classesDir.getPath() });
/*      */     }
/* 1061 */     PatternFilter filter = env.m_classFilenameFilter;
/* 1062 */     List filenames = SimpleFileUtils.scanFilesFiltered(classesDir, filter, null, null);
/* 1063 */     for (int f = filenames.size() - 1; f >= 0; --f)
/*      */     {
/* 1065 */       File classFile = new File(classesDir, (String)filenames.get(f));
/* 1066 */       classFile.delete();
/*      */     }
/* 1068 */     classesDir.delete();
/*      */   }
/*      */ 
/*      */   public void compileJava()
/*      */     throws IdcException
/*      */   {
/* 1079 */     if (!this.m_hasJava)
/*      */     {
/* 1081 */       return;
/*      */     }
/* 1083 */     File javaDir = this.m_javaDir;
/* 1084 */     BuildEnvironment env = this.m_manager.m_env;
/* 1085 */     DataBinder buildConfig = this.m_buildConfig;
/* 1086 */     JavaCompileManager compiler = env.m_javaCompiler;
/* 1087 */     compiler.m_classpath = computeJavaCompileClasspath();
/* 1088 */     List bootclasspath = null;
/* 1089 */     if (buildConfig != null)
/*      */     {
/* 1091 */       String bootclasspathPrepend = this.m_buildConfig.getLocal("JavacBootclasspathPrepend");
/* 1092 */       if (bootclasspathPrepend != null)
/*      */       {
/* 1094 */         bootclasspath = new ArrayList();
/* 1095 */         appendPathElementsStringTo(bootclasspath, bootclasspathPrepend);
/*      */       }
/*      */     }
/* 1098 */     compiler.m_bootclasspathPrepend = bootclasspath;
/* 1099 */     compiler.m_description = this.m_moduleName;
/* 1100 */     List javaFilenames = SimpleFileUtils.scanFilesFiltered(javaDir, env.m_javaFilenameFilter, null, null);
/* 1101 */     File classesDir = new File(this.m_moduleDir, "classes");
/*      */     try
/*      */     {
/* 1104 */       compiler.compileOutdated(javaDir, classesDir, javaFilenames);
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/* 1108 */       throw new ServiceException(ioe);
/*      */     }
/*      */   }
/*      */ 
/*      */   public List<String> computeJavaCompileClasspath()
/*      */     throws IdcException
/*      */   {
/* 1120 */     BuildEnvironment env = this.m_manager.m_env;
/* 1121 */     DataBinder buildConfig = this.m_buildConfig;
/* 1122 */     List classpath = new ArrayList();
/* 1123 */     if (env.m_defaultClasspath != null)
/*      */     {
/* 1125 */       classpath.addAll(env.m_defaultClasspath);
/*      */     }
/* 1127 */     appendClasspathTo(classpath, 4);
/*      */ 
/* 1129 */     String[] requiredModules = this.m_requiredModules;
/* 1130 */     if (requiredModules != null)
/*      */     {
/* 1132 */       Map modules = env.m_modules;
/* 1133 */       for (int r = 0; r < requiredModules.length; ++r)
/*      */       {
/* 1135 */         String requiredModuleName = requiredModules[r];
/* 1136 */         Module requiredModule = (Module)modules.get(requiredModuleName);
/* 1137 */         requiredModule.appendClasspathTo(classpath, 0);
/*      */       }
/*      */     }
/*      */ 
/* 1141 */     String javacClasspath = (buildConfig != null) ? buildConfig.getLocal("JavacClasspath") : null;
/* 1142 */     if (javacClasspath != null)
/*      */     {
/* 1144 */       appendPathElementsStringTo(classpath, javacClasspath);
/*      */     }
/*      */ 
/* 1147 */     return classpath;
/*      */   }
/*      */ 
/*      */   public void prepareShiphome(boolean shouldIncludeGeneratedFiles)
/*      */     throws IdcException
/*      */   {
/* 1158 */     if (!shouldIncludeGeneratedFiles)
/*      */     {
/* 1160 */       addToLabelManifest();
/*      */     }
/* 1162 */     PackageRule.Group packages = loadShiphomeRules();
/* 1163 */     if (packages == null)
/*      */     {
/* 1165 */       return;
/*      */     }
/* 1167 */     BuildEnvironment env = this.m_manager.m_env;
/* 1168 */     File moduleDir = this.m_moduleDir; File shiphomeDir = env.m_shiphomeDir;
/* 1169 */     GenericTracingCallback trace = env.m_trace;
/* 1170 */     long lastTime = System.currentTimeMillis();
/* 1171 */     for (PackageRule pkg : packages)
/*      */     {
/* 1173 */       String targetPrefix = pkg.m_packageFilename;
/* 1174 */       File targetFile = pkg.getTargetFile(shiphomeDir);
/* 1175 */       boolean isTargetADirectory = targetPrefix.endsWith("/");
/* 1176 */       int numSources = pkg.m_items.size();
/* 1177 */       ZipBuilder zip = null;
/* 1178 */       ZipBuilder.FileEntries[] entries = null;
/*      */ 
/* 1180 */       if (isTargetADirectory)
/*      */       {
/* 1182 */         targetPrefix = targetFile.getPath();
/*      */       }
/* 1184 */       else if (pkg.m_isBundled)
/*      */       {
/* 1186 */         File targetDir = targetFile.getParentFile();
/* 1187 */         String targetFilename = targetFile.getName();
/* 1188 */         targetDir.mkdirs();
/* 1189 */         zip = new ZipBuilder(targetDir, targetFilename);
/* 1190 */         zip.init();
/* 1191 */         entries = new ZipBuilder.FileEntries[numSources];
/*      */       }
/*      */       else
/*      */       {
/* 1195 */         String msg = new StringBuilder().append("unknown target path suffix: ").append(targetPrefix).toString();
/* 1196 */         throw new ServiceException(msg);
/*      */       }
/*      */ 
/* 1199 */       int s = 0;
/* 1200 */       for (PackageRule.Item item : pkg)
/*      */       {
/* 1202 */         if ((!shouldIncludeGeneratedFiles) && 
/* 1204 */           ("postGenerate".equals(item.m_stepName)))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1210 */         String sourcePrefix = item.m_dirname;
/* 1211 */         PatternFilter filter = item.m_filter;
/*      */         File sourceFile;
/*      */         File sourceFile;
/* 1213 */         if (FileUtils.isAbsolutePath(sourcePrefix))
/*      */         {
/* 1215 */           sourceFile = new File(sourcePrefix);
/*      */         }
/*      */         else
/*      */         {
/* 1219 */           sourceFile = new File(moduleDir, sourcePrefix);
/*      */         }
/* 1221 */         List sourcePaths = new ArrayList();
/* 1222 */         if (sourceFile.isDirectory())
/*      */         {
/* 1224 */           sourcePrefix = new StringBuilder().append(sourceFile.getPath()).append('/').toString();
/* 1225 */           SimpleFileUtils.scanFilesFiltered(sourceFile, filter, "", sourcePaths);
/* 1226 */           if ((sourcePaths.size() == 0) && (trace != null))
/*      */           {
/* 1228 */             trace.report(4, new Object[] { "file scan filter returned no files in ", sourcePrefix, "\nfilter:\n", filter });
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 1234 */           sourcePaths.add(sourceFile.getName());
/* 1235 */           sourcePrefix = new StringBuilder().append(sourceFile.getParent()).append('/').toString();
/*      */         }
/*      */ 
/* 1238 */         if (isTargetADirectory)
/*      */         {
/* 1240 */           for (String path : sourcePaths)
/*      */           {
/* 1242 */             String sourcePath = new StringBuilder().append(sourcePrefix).append(path).toString();
/* 1243 */             File sourceAsFile = new File(sourcePath);
/* 1244 */             File targetAsFile = new File(targetFile, path);
/* 1245 */             File targetDir = targetAsFile.getParentFile();
/* 1246 */             targetDir.mkdirs();
/* 1247 */             BuildUtils.copyOutdatedFile(sourceAsFile, targetAsFile, this.m_manager.m_env.m_trace);
/*      */           }
/*      */         }
/* 1250 */         else if (zip != null)
/*      */         {
/*      */           ZipBuilder tmp604_602 = zip; tmp604_602.getClass(); entries[(s++)] = new ZipBuilder.FileEntries(tmp604_602, sourceFile, sourcePaths, item.m_entryPrefix);
/*      */         }
/*      */       }
/* 1255 */       long now = System.currentTimeMillis();
/* 1256 */       if (zip != null)
/*      */       {
/* 1258 */         int numFilesUpdated = zip.update(false, entries);
/* 1259 */         trace.report(6, new Object[] { "updated ", Integer.valueOf(numFilesUpdated), " of ", Integer.valueOf(zip.m_zip.m_entries.size()), " entries (", Long.valueOf(now - lastTime), " ms) in ", pkg });
/*      */       }
/*      */ 
/* 1263 */       lastTime = now;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateDependencies(int flags, HTTPDownloader.StateListener callback)
/*      */     throws IdcException
/*      */   {
/* 1284 */     List fetchRules = loadFetchRules();
/*      */     BuildEnvironment env;
/*      */     GenericTracingCallback trace;
/*      */     boolean skipLocalFetches;
/*      */     boolean skipRemoteFetches;
/*      */     boolean isForBuild;
/*      */     boolean isValidateOnly;
/* 1285 */     if (fetchRules != null)
/*      */     {
/* 1287 */       env = this.m_manager.m_env;
/* 1288 */       trace = env.m_trace;
/* 1289 */       skipLocalFetches = (flags & 0x4) != 0;
/* 1290 */       skipRemoteFetches = (flags & 0x8) != 0;
/* 1291 */       isForBuild = (flags & 0x80) != 0;
/* 1292 */       isValidateOnly = (flags & 0x100) != 0;
/* 1293 */       if ((trace != null) && (!skipRemoteFetches))
/*      */       {
/* 1295 */         trace.report(6, new Object[] { "checking remote dependencies for ", this.m_moduleName, " ..." });
/*      */       }
/*      */ 
/* 1298 */       for (FetchRule rule : fetchRules)
/*      */       {
/* 1300 */         if ((isForBuild ^ rule.m_isForBuild)) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1304 */         if (rule.m_isSourceRemote)
/*      */         {
/* 1306 */           if (skipRemoteFetches) {
/*      */             continue;
/*      */           }
/*      */ 
/* 1310 */           checkFetchDependency(rule.m_sourcePath, rule.m_targetPath, rule, flags, callback);
/*      */         }
/*      */         else
/*      */         {
/* 1314 */           if (skipLocalFetches) {
/*      */             continue;
/*      */           }
/*      */ 
/* 1318 */           File sourceFile = rule.m_sourceFile;
/* 1319 */           File targetFile = rule.m_targetFile;
/* 1320 */           boolean isTargetDirectory = rule.m_targetPath.endsWith("/");
/* 1321 */           if (isTargetDirectory)
/*      */           {
/* 1323 */             targetFile = new File(targetFile, sourceFile.getName());
/*      */           }
/* 1325 */           if (isValidateOnly)
/*      */           {
/* 1327 */             Set validatedFiles = env.m_validatedFiles;
/*      */ 
/* 1329 */             if (!validatedFiles.contains(sourceFile))
/*      */             {
/* 1331 */               if (trace != null)
/*      */               {
/* 1333 */                 trace.report(7, new Object[] { "checking ", sourceFile });
/*      */               }
/* 1335 */               if (!sourceFile.exists())
/*      */               {
/* 1337 */                 if (trace != null)
/*      */                 {
/* 1339 */                   trace.report(3, new Object[] { sourceFile, ": does not exist" });
/*      */                 }
/* 1341 */                 env.m_didValidateErrorOccur = true;
/*      */               }
/*      */             }
/* 1344 */             validatedFiles.add(targetFile);
/*      */           }
/*      */           else
/*      */           {
/* 1348 */             targetFile.getParentFile().mkdirs();
/* 1349 */             BuildUtils.copyOutdatedFile(sourceFile, targetFile, trace);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1355 */     List extractRules = loadExtractRules();
/* 1356 */     if ((extractRules == null) || ((flags & 0x10) != 0))
/*      */     {
/* 1358 */       return;
/*      */     }
/* 1360 */     for (ExtractRule rule : extractRules)
/*      */     {
/* 1362 */       checkExtractDependency(rule, flags);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean checkFetchDependency(String URL, String target, FetchRule rule, int flags, HTTPDownloader.StateListener callback)
/*      */     throws IdcException
/*      */   {
/* 1387 */     BuildManager manager = this.m_manager;
/* 1388 */     BuildEnvironment env = manager.m_env;
/* 1389 */     boolean isDirectoryTarget = target.endsWith("/");
/* 1390 */     boolean isDirectoryURL = URL.endsWith("/");
/*      */ 
/* 1392 */     if ((isDirectoryTarget) && (!isDirectoryURL))
/*      */     {
/* 1394 */       int lastSlash = URL.lastIndexOf("/");
/* 1395 */       if (lastSlash < 0)
/*      */       {
/* 1397 */         throw new ServiceException(null, "syPathInvalid", new Object[] { target });
/*      */       }
/* 1399 */       String name = URL.substring(lastSlash + 1);
/* 1400 */       target = new StringBuilder().append(target).append(name).toString();
/*      */     }
/* 1402 */     File targetFile = new File(target);
/*      */ 
/* 1404 */     boolean isValidateOnly = (flags & 0x100) != 0;
/* 1405 */     if (isValidateOnly)
/*      */     {
/* 1407 */       Map headers = (rule == null) ? null : rule.m_extraHTTPHeaders;
/*      */       try
/*      */       {
/* 1410 */         report(7, new Object[] { "validating ", URL });
/* 1411 */         env.m_downloader.requestHEADWithHeaders(URL, headers);
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 1415 */         GenericTracingCallback trace = env.m_trace;
/* 1416 */         if (trace != null)
/*      */         {
/* 1418 */           trace.report(3, new Object[] { "unable to download ", URL, ": ", t.getMessage() });
/*      */         }
/* 1420 */         env.m_didValidateErrorOccur = true;
/*      */       }
/* 1422 */       env.m_validatedFiles.add(targetFile);
/* 1423 */       return false;
/*      */     }
/*      */ 
/* 1427 */     if (isDirectoryURL)
/*      */     {
/* 1429 */       if (!isDirectoryTarget)
/*      */       {
/* 1431 */         throw new ServiceException(null, "syPathInvalid", new Object[] { target });
/*      */       }
/* 1433 */       return checkFetchDirectory(URL, target, flags, callback);
/*      */     }
/*      */ 
/* 1436 */     targetFile.getParentFile().mkdirs();
/* 1437 */     boolean shouldFetch = shouldFetchFile(URL, targetFile, rule, flags);
/* 1438 */     boolean hasFetchedResourceChanged = false;
/* 1439 */     if (shouldFetch)
/*      */     {
/* 1441 */       HTTPDownloader downloader = env.m_downloader;
/* 1442 */       Map headers = (rule == null) ? null : rule.m_extraHTTPHeaders;
/* 1443 */       report(6, new Object[] { "fetching ", URL });
/*      */       HTTPDownloader.State state;
/*      */       try
/*      */       {
/* 1447 */         state = downloader.startRequestGETWithHeaders(URL, headers);
/*      */ 
/* 1449 */         downloader.saveRequest(state, targetFile, callback);
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 1453 */         IdcMessage msg = new IdcMessage("csUnableToDownload", new Object[] { URL });
/* 1454 */         msg = new IdcMessage(msg, "syFileSaveError", new Object[] { target });
/* 1455 */         throw new ServiceException(t, msg);
/*      */       }
/* 1457 */       if (rule != null)
/*      */       {
/* 1459 */         boolean isExecutable = MapUtils.getBoolValueFromMap(rule.m_flags, "isExecutable", false);
/* 1460 */         if (isExecutable)
/*      */         {
/* 1462 */           targetFile.setExecutable(true, false);
/*      */         }
/*      */ 
/* 1465 */         MessageDigest digest = env.m_messageDigest;
/* 1466 */         byte[] md5 = SimpleFileUtils.computeFileChecksum(targetFile, digest);
/* 1467 */         if ((rule.m_lastModified != state.m_lastModified) || (rule.m_fileLength != state.m_contentLength) || (!md5.equals(rule.m_md5)))
/*      */         {
/* 1470 */           rule.m_lastModified = state.m_lastModified;
/* 1471 */           rule.m_fileLength = state.m_contentLength;
/* 1472 */           rule.m_md5 = md5;
/* 1473 */           saveFetchRule(rule);
/* 1474 */           hasFetchedResourceChanged = true;
/*      */         }
/*      */       }
/*      */     }
/* 1478 */     else if (rule != null)
/*      */     {
/* 1480 */       long fileLastModified = targetFile.lastModified();
/* 1481 */       long fileLength = targetFile.length();
/* 1482 */       if ((fileLength != rule.m_fileLength) || (BuildUtils.compareSecondsFromMillis(fileLastModified, rule.m_lastModified) != 0))
/*      */       {
/* 1485 */         MessageDigest digest = env.m_messageDigest;
/* 1486 */         byte[] md5 = SimpleFileUtils.computeFileChecksum(targetFile, digest);
/* 1487 */         rule.m_lastModified = fileLastModified;
/* 1488 */         rule.m_fileLength = fileLength;
/* 1489 */         rule.m_md5 = md5;
/* 1490 */         saveFetchRule(rule);
/* 1491 */         hasFetchedResourceChanged = true;
/*      */       }
/*      */     }
/* 1494 */     if (hasFetchedResourceChanged)
/*      */     {
/* 1496 */       manager.saveBuildState();
/*      */     }
/*      */ 
/* 1499 */     return shouldFetch;
/*      */   }
/*      */ 
/*      */   public boolean shouldFetchFile(String URL, File targetFile, FetchRule rule, int flags)
/*      */     throws IdcException
/*      */   {
/* 1523 */     boolean isFetchForced = (flags & 0x1) != 0;
/* 1524 */     if (isFetchForced)
/*      */     {
/* 1526 */       return true;
/*      */     }
/* 1528 */     boolean isFetchCheckForced = (flags & 0x2) != 0;
/*      */ 
/* 1530 */     long keptLastModified = 0L; long keptLength = 0L;
/* 1531 */     if (rule != null)
/*      */     {
/* 1533 */       keptLastModified = rule.m_lastModified;
/* 1534 */       keptLength = rule.m_fileLength;
/*      */     }
/* 1536 */     if (!targetFile.exists())
/*      */     {
/* 1539 */       return true;
/*      */     }
/* 1541 */     boolean shouldCheckLastModified = rule == null;
/* 1542 */     long fileTimestamp = targetFile.lastModified();
/* 1543 */     long fileLength = targetFile.length();
/* 1544 */     if (!isFetchCheckForced)
/*      */     {
/* 1546 */       if ((shouldCheckLastModified) && (keptLastModified != 0L) && (BuildUtils.compareSecondsFromMillis(fileTimestamp, keptLastModified) != 0))
/*      */       {
/* 1549 */         return true;
/*      */       }
/* 1551 */       if (keptLength != 0L)
/*      */       {
/* 1555 */         return fileLength != keptLength;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1561 */     Map headers = (rule == null) ? null : rule.m_extraHTTPHeaders;
/*      */     try
/*      */     {
/* 1565 */       report(7, new Object[] { "checking ", URL });
/* 1566 */       HTTPDownloader.State state = this.m_manager.m_env.m_downloader.requestHEADWithHeaders(URL, headers);
/* 1567 */       if ((shouldCheckLastModified) && (state.m_lastModified > fileTimestamp))
/*      */       {
/* 1569 */         return true;
/*      */       }
/* 1571 */       if (state.m_contentLength != fileLength)
/*      */       {
/* 1573 */         return true;
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1578 */       IdcMessage msg = new IdcMessage("csUnableToDownload", new Object[] { URL });
/* 1579 */       msg = new IdcMessage(msg, "syFileSaveError", new Object[] { targetFile.getPath() });
/* 1580 */       throw new ServiceException(t, msg);
/*      */     }
/* 1582 */     return false;
/*      */   }
/*      */ 
/*      */   protected boolean checkFetchDirectory(String URL, String target, int flags, HTTPDownloader.StateListener callback)
/*      */     throws IdcException
/*      */   {
/* 1598 */     HTTPDownloader downloader = this.m_manager.m_env.m_downloader;
/*      */ 
/* 1600 */     File targetDir = new File(target);
/* 1601 */     targetDir.mkdirs();
/* 1602 */     long indexLength = 0L;
/* 1603 */     String[] listing = null;
/* 1604 */     File targetIndexFile = new File(targetDir, ".index");
/* 1605 */     if (targetIndexFile.exists())
/*      */     {
/* 1607 */       FileReader reader = null;
/* 1608 */       BufferedReader br = null;
/*      */       try
/*      */       {
/* 1611 */         reader = new FileReader(targetIndexFile);
/* 1612 */         br = new BufferedReader(reader);
/* 1613 */         reader = null;
/* 1614 */         String indexLengthString = br.readLine();
/* 1615 */         indexLength = NumberUtils.parseInteger(indexLengthString, 0);
/* 1616 */         List list = new ArrayList();
/*      */ 
/* 1618 */         while ((line = br.readLine()) != null)
/*      */         {
/*      */           String line;
/* 1620 */           list.add(line);
/*      */         }
/* 1622 */         int numLines = list.size();
/* 1623 */         listing = new String[numLines];
/* 1624 */         list.toArray(listing);
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 1628 */         IdcMessage msg = new IdcMessage("csUnableToDownload", new Object[] { URL });
/*      */ 
/* 1630 */         throw new ServiceException(t);
/*      */       }
/*      */       finally
/*      */       {
/* 1634 */         FileUtils.closeObject(reader);
/* 1635 */         FileUtils.closeObject(br);
/*      */       }
/*      */     }
/* 1638 */     boolean shouldFetch = indexLength == 0L;
/*      */     try
/*      */     {
/* 1641 */       if (!shouldFetch)
/*      */       {
/* 1643 */         report(7, new Object[] { "checking ", URL });
/* 1644 */         HTTPDownloader.State state = downloader.requestHEAD(URL);
/* 1645 */         long contentLength = state.m_contentLength;
/* 1646 */         if ((contentLength < 0L) || (contentLength != indexLength))
/*      */         {
/* 1648 */           shouldFetch = true;
/*      */         }
/*      */       }
/* 1651 */       if (shouldFetch)
/*      */       {
/* 1653 */         report(6, new Object[] { "fetching ", URL });
/* 1654 */         HTTPDownloader.State state = downloader.startRequestGET(URL);
/* 1655 */         listing = downloader.processDirectoryRequest(state, callback);
/* 1656 */         indexLength = state.m_position;
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1661 */       IdcMessage msg = new IdcMessage("csUnableToDownload", new Object[] { URL });
/* 1662 */       throw new ServiceException(t, msg);
/*      */     }
/* 1664 */     if (shouldFetch)
/*      */     {
/* 1666 */       if (listing == null)
/*      */       {
/* 1668 */         throw new ServiceException(null, "bad directory URL", new Object[] { URL });
/*      */       }
/* 1670 */       FileWriter writer = null;
/* 1671 */       BufferedWriter bw = null;
/*      */       try
/*      */       {
/* 1674 */         writer = new FileWriter(targetIndexFile);
/* 1675 */         bw = new BufferedWriter(writer);
/* 1676 */         writer = null;
/* 1677 */         bw.write(Long.toString(indexLength));
/* 1678 */         bw.newLine();
/* 1679 */         for (int i = 0; i < listing.length; ++i)
/*      */         {
/* 1681 */           bw.write(listing[i]);
/* 1682 */           bw.newLine();
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/*      */       }
/*      */       finally
/*      */       {
/* 1693 */         FileUtils.closeObject(writer);
/* 1694 */         FileUtils.closeObject(bw);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1699 */     boolean wasUpdated = false;
/* 1700 */     for (int i = 0; i < listing.length; ++i)
/*      */     {
/* 1702 */       String item = listing[i];
/* 1703 */       String subURL = new StringBuilder().append(URL).append(item).toString();
/* 1704 */       String subTarget = new StringBuilder().append(target).append(item).toString();
/* 1705 */       if (!checkFetchDependency(subURL, subTarget, null, flags, callback))
/*      */         continue;
/* 1707 */       wasUpdated = true;
/*      */     }
/*      */ 
/* 1710 */     return wasUpdated;
/*      */   }
/*      */ 
/*      */   public void checkExtractDependency(ExtractRule rule, int flags)
/*      */     throws IdcException
/*      */   {
/* 1723 */     File sourceFile = rule.m_sourceFile;
/* 1724 */     if (!sourceFile.exists())
/*      */     {
/* 1726 */       throw new ServiceException(null, "syFileDoesNotExist", new Object[] { sourceFile.getPath() });
/*      */     }
/* 1728 */     File targetFile = rule.m_targetFile;
/* 1729 */     long lastModified = targetFile.lastModified(); long sourceLastModified = sourceFile.lastModified();
/* 1730 */     if ((!targetFile.isDirectory()) && (lastModified != 0L) && (BuildUtils.compareSecondsFromMillis(sourceLastModified, lastModified) == 0))
/*      */     {
/* 1733 */       return;
/*      */     }
/*      */ 
/* 1737 */     rule.m_sourceFileLastModified = sourceLastModified;
/* 1738 */     rule.m_targetFileLastModified = lastModified;
/*      */ 
/* 1740 */     String sourceName = sourceFile.getName();
/* 1741 */     int dot = sourceName.lastIndexOf(46);
/* 1742 */     String extension = sourceName.substring(dot + 1);
/* 1743 */     if ((extension.equals("ear")) || (extension.equals("jar")) || (extension.equals("zip")))
/*      */     {
/* 1745 */       checkExtractZipDependency(rule, flags);
/*      */     }
/*      */     else
/*      */     {
/* 1749 */       throw new ServiceException(null, "syUnknownExtension2", new Object[] { sourceName });
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkExtractZipDependency(ExtractRule rule, int flags)
/*      */     throws IdcException
/*      */   {
/* 1763 */     File sourceFile = rule.m_sourceFile;
/* 1764 */     IdcZipFile zip = new IdcZipFile(sourceFile);
/*      */     try
/*      */     {
/* 1767 */       zip.init(IdcZipFunctions.m_defaultZipEnvironment);
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1771 */       throw new ServiceException(t, "syZipExtractionError", new Object[] { sourceFile.getPath() });
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1777 */       Map entriesMap = zip.m_entries;
/* 1778 */       Set entryNamesSet = entriesMap.keySet();
/* 1779 */       String[] entriesArray = new String[entryNamesSet.size()];
/* 1780 */       entryNamesSet.toArray(entriesArray);
/* 1781 */       Arrays.sort(entriesArray);
/* 1782 */       List entriesToExtract = new ArrayList(entriesArray.length);
/* 1783 */       List entryPaths = new ArrayList(entriesArray.length);
/* 1784 */       PatternFilter filter = rule.m_filter;
/* 1785 */       for (int e = 0; e < entriesArray.length; ++e)
/*      */       {
/* 1787 */         String entryName = entriesArray[e];
/* 1788 */         String entryPath = stripPathPrefix(rule, entryName, false);
/* 1789 */         if (entryPath == null) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1793 */         boolean isIncluded = (filter == null) || (filter.isIncluded(entryPath));
/* 1794 */         if (!isIncluded)
/*      */           continue;
/* 1796 */         entriesToExtract.add(entryName);
/* 1797 */         entryPaths.add(entryPath);
/*      */       }
/*      */ 
/* 1801 */       int numEntriesToExtract = entriesToExtract.size();
/* 1802 */       if (numEntriesToExtract == 0)
/*      */       {
/* 1804 */         report(5, new Object[] { "no entries to extract from ", sourceFile.getPath() });
/*      */         return;
/*      */       }
/*      */ 
/* 1808 */       if (numEntriesToExtract == 1)
/*      */       {
/* 1810 */         String entryName = (String)entriesToExtract.get(0);
/* 1811 */         IdcZipEntry entry = (IdcZipEntry)entriesMap.get(entryName);
/* 1812 */         String target = rule.m_targetFilename;
/* 1813 */         File targetFile = rule.m_targetFile;
/* 1814 */         if (target.endsWith("/"))
/*      */         {
/* 1816 */           int lastSlash = entryName.lastIndexOf("/");
/* 1817 */           if (lastSlash < 0)
/*      */           {
/* 1819 */             throw new ServiceException(null, "syPathInvalid", new Object[] { entryName });
/*      */           }
/* 1821 */           String name = entryName.substring(lastSlash + 1);
/* 1822 */           target = new StringBuilder().append(target).append(name).toString();
/* 1823 */           targetFile = new File(targetFile, name);
/*      */         }
/*      */ 
/* 1825 */         checkExtractZipEntry(rule, entry, targetFile);
/*      */         return;
/*      */       }
/*      */ 
/* 1830 */       String target = rule.m_targetFilename;
/* 1831 */       if (!target.endsWith("/"))
/*      */       {
/* 1833 */         target = new StringBuilder().append(target).append('/').toString();
/*      */       }
/* 1835 */       for (int e = 0; e < numEntriesToExtract; ++e)
/*      */       {
/* 1837 */         String entryName = (String)entriesToExtract.get(e);
/* 1838 */         String entryPath = (String)entryPaths.get(e);
/* 1839 */         String targetPath = new StringBuilder().append(target).append(entryPath).toString();
/* 1840 */         File targetFile = new File(targetPath);
/* 1841 */         File targetDir = targetFile.getParentFile();
/* 1842 */         targetDir.mkdirs();
/* 1843 */         IdcZipEntry entry = (IdcZipEntry)entriesMap.get(entryName);
/* 1844 */         checkExtractZipEntry(rule, entry, targetFile);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 1849 */       FileUtils.closeObject(zip);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String stripPathPrefix(ExtractRule rule, String path, boolean keepLastElement)
/*      */   {
/* 1864 */     int stripCount = rule.m_pathStripCount;
/* 1865 */     int index = 0;
/* 1866 */     while (stripCount-- > 0)
/*      */     {
/* 1868 */       int slashIndex = path.indexOf(47, index);
/* 1869 */       if (slashIndex < 0)
/*      */       {
/* 1872 */         if (keepLastElement)
/*      */           break;
/* 1874 */         return null;
/*      */       }
/*      */ 
/* 1878 */       index = slashIndex + 1;
/*      */     }
/* 1880 */     if (index > 0)
/*      */     {
/* 1882 */       if ((!keepLastElement) && (index == path.length()))
/*      */       {
/* 1884 */         return null;
/*      */       }
/* 1886 */       return path.substring(index);
/*      */     }
/* 1888 */     return path;
/*      */   }
/*      */ 
/*      */   public void checkExtractZipEntry(ExtractRule rule, IdcZipEntry entry, File targetFile)
/*      */     throws IdcException
/*      */   {
/* 1903 */     long lastModified = targetFile.lastModified(); long entryLastModified = entry.m_lastModified;
/* 1904 */     if (entryLastModified == -1L)
/*      */     {
/* 1906 */       entryLastModified = rule.m_sourceFileLastModified;
/*      */     }
/* 1908 */     if ((lastModified != 0L) && (lastModified == entryLastModified))
/*      */     {
/* 1910 */       return;
/*      */     }
/* 1912 */     String targetFilename = targetFile.getPath();
/* 1913 */     report(7, new Object[] { "extracting ", targetFilename });
/* 1914 */     if (entry.m_isDirectory)
/*      */     {
/* 1916 */       targetFile.mkdir();
/* 1917 */       targetFile.setLastModified(entryLastModified);
/*      */     }
/*      */     else
/*      */     {
/* 1921 */       IdcZipFunctions.extractEntry(entry, targetFilename, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 1928 */     return this.m_moduleName;
/*      */   }
/*      */ 
/*      */   public void report(int level, Object[] args)
/*      */   {
/* 1933 */     GenericTracingCallback trace = this.m_manager.m_env.m_trace;
/* 1934 */     if (trace != null)
/*      */     {
/* 1936 */       trace.report(level, args);
/*      */     }
/*      */     else
/*      */     {
/* 1940 */       if ((level >= 7) && (!SystemUtils.m_verbose))
/*      */       {
/* 1942 */         return;
/*      */       }
/* 1944 */       StringBuilder str = new StringBuilder();
/* 1945 */       for (Object arg : args)
/*      */       {
/* 1947 */         str.append(arg);
/*      */       }
/* 1949 */       Report.trace("componentinstaller", str.toString(), null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Module createAndLoadModule(BuildManager manager, File moduleDir)
/*      */     throws IdcException
/*      */   {
/* 2019 */     Module module = null;
/* 2020 */     DataBinder binder = manager.loadBuildConfigBinder(moduleDir, false);
/* 2021 */     if (binder != null)
/*      */     {
/* 2023 */       String classname = binder.getLocal("ModuleClassname");
/* 2024 */       if (classname != null)
/*      */       {
/*      */         try
/*      */         {
/* 2028 */           Class moduleClass = Class.forName(classname);
/* 2029 */           module = (Module)moduleClass.newInstance();
/*      */         }
/*      */         catch (Throwable t)
/*      */         {
/* 2033 */           String msg = new StringBuilder().append("unable to instantiate Module for class \"").append(classname).append('"').toString();
/* 2034 */           throw new DataException(t, msg, new Object[0]);
/*      */         }
/*      */       }
/*      */     }
/* 2038 */     File componentBinderFile = null;
/* 2039 */     if (module == null)
/*      */     {
/* 2041 */       String componentBinderName = new StringBuilder().append(moduleDir.getName()).append(".hda").toString();
/* 2042 */       componentBinderFile = new File(moduleDir, componentBinderName);
/* 2043 */       if (componentBinderFile.exists())
/*      */       {
/* 2045 */         module = new Component();
/*      */       }
/*      */       else
/*      */       {
/* 2049 */         module = new Module();
/*      */       }
/*      */     }
/* 2052 */     module.m_buildConfig = binder;
/* 2053 */     if (module instanceof Component)
/*      */     {
/* 2055 */       ((Component)module).init(manager, moduleDir, componentBinderFile);
/*      */     }
/*      */     else
/*      */     {
/* 2059 */       module.init(manager, moduleDir);
/*      */     }
/* 2061 */     module.reload();
/* 2062 */     return module;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2067 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100353 $";
/*      */   }
/*      */ 
/*      */   public static class ExtractRule
/*      */   {
/*      */     public String m_source;
/*      */     public String m_localPath;
/*      */     public File m_sourceFile;
/*      */     public long m_sourceFileLastModified;
/*      */     public String m_targetFilename;
/*      */     public File m_targetFile;
/*      */     public boolean m_isTargetDirectory;
/*      */     public long m_targetFileLastModified;
/*      */     public Map m_flags;
/*      */     public int m_pathStripCount;
/*      */     public PatternFilter m_filter;
/*      */ 
/*      */     public String toString()
/*      */     {
/* 2004 */       return this.m_targetFilename;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static class FetchRule
/*      */   {
/*      */     public String m_source;
/*      */     public String m_localPath;
/*      */     public Map<String, String> m_extraHTTPHeaders;
/*      */     public boolean m_shouldIgnoreLastModified;
/*      */     public String m_sourcePath;
/*      */     public File m_sourceFile;
/*      */     public boolean m_isSourceRemote;
/*      */     public String m_targetPath;
/*      */     public File m_targetFile;
/*      */     public Map m_flags;
/*      */     public boolean m_isForBuild;
/*      */     public long m_lastModified;
/*      */     public long m_fileLength;
/*      */     public byte[] m_md5;
/*      */ 
/*      */     public String toString()
/*      */     {
/* 1977 */       return this.m_source;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.Module
 * JD-Core Version:    0.5.4
 */