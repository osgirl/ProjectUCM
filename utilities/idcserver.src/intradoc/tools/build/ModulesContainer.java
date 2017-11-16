/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.PathUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcException;
/*     */ import java.io.File;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ModulesContainer
/*     */ {
/*     */   public BuildManager m_manager;
/*     */   public File m_modulesDir;
/*     */   public Properties m_properties;
/*     */   public boolean m_isRequired;
/*     */   public boolean m_isIncluded;
/*     */   public DataBinder m_buildConfig;
/*     */   public DataResultSet m_modulesTable;
/*     */   protected Component.ProductTags m_defaultProductTags;
/*     */ 
/*     */   public void init(BuildManager manager, File modulesDir, DataBinder buildConfig, boolean isRequired)
/*     */     throws IdcException
/*     */   {
/*  47 */     BuildEnvironment env = manager.m_env;
/*     */ 
/*  49 */     Properties props = this.m_properties = new IdcProperties(env.m_properties);
/*     */ 
/*  51 */     this.m_manager = manager;
/*  52 */     this.m_modulesDir = modulesDir;
/*  53 */     this.m_isRequired = isRequired;
/*  54 */     this.m_isIncluded = false;
/*  55 */     this.m_buildConfig = buildConfig;
/*     */ 
/*  57 */     boolean isModuleDir = modulesDir.isDirectory();
/*  58 */     if (!isModuleDir)
/*     */     {
/*  60 */       StringBuilder msg = new StringBuilder();
/*  61 */       msg.append(modulesDir);
/*  62 */       boolean doesExist = modulesDir.exists();
/*  63 */       msg.append((doesExist) ? " is not a directory" : " does not exist");
/*  64 */       if (isRequired)
/*     */       {
/*  66 */         msg.append(" and is required!");
/*  67 */         throw new DataException(msg.toString());
/*     */       }
/*  69 */       manager.report(6, new Object[] { msg, ", skipping" });
/*  70 */       return;
/*     */     }
/*  72 */     if (buildConfig == null)
/*     */     {
/*  74 */       File file = manager.getBuildConfigFile(modulesDir);
/*  75 */       StringBuilder msg = new StringBuilder();
/*  76 */       msg.append(file);
/*  77 */       boolean doesExist = modulesDir.exists();
/*  78 */       msg.append((doesExist) ? " is not a file" : " does not exist");
/*  79 */       if (isRequired)
/*     */       {
/*  81 */         msg.append(" and is required!");
/*  82 */         throw new DataException(msg.toString());
/*     */       }
/*  84 */       manager.report(6, new Object[] { msg, ", skipping" });
/*  85 */       return;
/*     */     }
/*  87 */     DataResultSet modulesTable = this.m_modulesTable = (DataResultSet)buildConfig.getResultSet("Modules");
/*  88 */     if (modulesTable == null)
/*     */     {
/*  90 */       File file = manager.getBuildConfigFile(modulesDir);
/*  91 */       StringBuilder msg = new StringBuilder();
/*  92 */       msg.append(file);
/*  93 */       msg.append(" missing Modules result set");
/*  94 */       if (isRequired)
/*     */       {
/*  96 */         msg.append(" and is required");
/*  97 */         throw new DataException(msg.toString());
/*     */       }
/*  99 */       manager.report(6, new Object[] { msg, ", skipping" });
/* 100 */       return;
/*     */     }
/* 102 */     this.m_isIncluded = true;
/*     */ 
/* 105 */     Properties binderProps = buildConfig.getLocalData();
/* 106 */     int pathFlags = PathUtils.F_KEEP_UNKNOWN_VARS;
/* 107 */     ExecutionContext context = env.m_context;
/* 108 */     for (String key : binderProps.stringPropertyNames())
/*     */     {
/* 110 */       String oldValue = binderProps.getProperty(key);
/* 111 */       String newValue = PathUtils.substitutePathVariables(oldValue, props, null, pathFlags, context);
/* 112 */       props.setProperty(key, newValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadModules() throws IdcException
/*     */   {
/* 118 */     File modulesDir = this.m_modulesDir;
/* 119 */     String defaultProductTagsString = this.m_buildConfig.getLocal("defaultProductTags");
/* 120 */     this.m_defaultProductTags = new Component.ProductTags(defaultProductTagsString);
/* 121 */     DataResultSet modulesTable = this.m_modulesTable;
/* 122 */     int nameIndex = ResultSetUtils.getIndexMustExist(modulesTable, "name");
/* 123 */     int productTagsIndex = modulesTable.getFieldInfoIndex("productTags");
/* 124 */     for (modulesTable.first(); modulesTable.isRowPresent(); modulesTable.next())
/*     */     {
/* 126 */       String moduleName = modulesTable.getStringValue(nameIndex);
/* 127 */       File moduleDir = new File(modulesDir, moduleName);
/* 128 */       String productTagsOverride = (productTagsIndex < 0) ? null : modulesTable.getStringValue(productTagsIndex);
/*     */ 
/* 130 */       loadModule(moduleDir, productTagsOverride);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadModule(File moduleDir, String productTagsOverride) throws IdcException
/*     */   {
/* 136 */     if (!moduleDir.exists())
/*     */     {
/* 138 */       String msg = new StringBuilder().append(moduleDir).append(" does not exist").toString();
/* 139 */       throw new DataException(msg);
/*     */     }
/* 141 */     BuildManager manager = this.m_manager;
/* 142 */     Module module = Module.createAndLoadModule(manager, moduleDir);
/* 143 */     if (module instanceof Component)
/*     */     {
/* 145 */       Component component = (Component)module;
/* 146 */       Component.ProductTags componentProductTags = component.m_productTags;
/* 147 */       boolean useDefaultProductTags = true;
/* 148 */       if ((productTagsOverride != null) && (productTagsOverride.length() > 0))
/*     */       {
/* 150 */         componentProductTags.clear();
/* 151 */         componentProductTags.addFromString(productTagsOverride);
/* 152 */         useDefaultProductTags = false;
/*     */       }
/* 154 */       if (useDefaultProductTags)
/*     */       {
/* 156 */         componentProductTags.putAll(this.m_defaultProductTags);
/*     */       }
/*     */     }
/*     */ 
/* 160 */     String moduleName = module.m_moduleName;
/* 161 */     Map modules = manager.m_env.m_modules;
/* 162 */     if (modules.containsKey(moduleName))
/*     */     {
/* 164 */       manager.report(4, new Object[] { "Module ", moduleName, " already exists" });
/*     */     }
/*     */     else
/*     */     {
/* 168 */       modules.put(moduleName, module);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 175 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99576 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.ModulesContainer
 * JD-Core Version:    0.5.4
 */