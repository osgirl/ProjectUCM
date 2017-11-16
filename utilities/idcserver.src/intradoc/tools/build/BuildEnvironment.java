/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.io.HTTPDownloader;
/*     */ import intradoc.io.HTTPDownloader.StateListener;
/*     */ import intradoc.loader.IdcClassLoader;
/*     */ import intradoc.server.utils.ComponentInstaller;
/*     */ import intradoc.server.utils.ComponentListEditor;
/*     */ import intradoc.tools.common.JavaCompileManager;
/*     */ import intradoc.tools.utils.TextUtils;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.PatternFilter;
/*     */ import java.io.File;
/*     */ import java.security.MessageDigest;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Calendar;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.TimeZone;
/*     */ 
/*     */ public class BuildEnvironment
/*     */ {
/*     */   public static DateFormat s_iso8601;
/*     */   public String m_productVersionName;
/*     */   public String m_productVersionNumber;
/*     */   public String m_productBuild;
/*     */   public GenericTracingCallback m_trace;
/*     */   public int m_traceLevel;
/*     */   public boolean m_isVerbose;
/*     */   public boolean m_isReleng;
/*     */   public File m_branchDir;
/*     */   public File m_buildDir;
/*     */   public File m_componentsDir;
/*     */   public File m_componentZipsDir;
/*     */   public File m_configDir;
/*     */   public File m_intradocDir;
/*     */   public File m_resourcesDir;
/*     */   public File m_shiphomeDir;
/*     */   public File m_buildStateFile;
/*     */   public Map<String, String> m_environment;
/*     */   public DataBinder m_binder;
/*     */   public Properties m_properties;
/*     */   public IdcClassLoader m_loader;
/*     */   public ExecutionContext m_context;
/*     */   public HTTPDownloader m_downloader;
/*     */   public HTTPDownloader.StateListener m_downloadStateListener;
/*     */   public MessageDigest m_messageDigest;
/*     */   public String[] m_productNames;
/*     */   public Map<String, ModulesContainer> m_modulesContainers;
/*     */   public Map<String, Module> m_modules;
/*     */   public List<String> m_sortedModuleNames;
/*     */   public Map<String, String> m_wcProps;
/*     */   public String m_branch;
/*     */   public Set<File> m_validatedFiles;
/*     */   public boolean m_didValidateErrorOccur;
/*     */   public JavaCompileManager m_javaCompiler;
/*     */   public PatternFilter m_javaFilenameFilter;
/*     */   public PatternFilter m_classFilenameFilter;
/*     */   public List<String> m_defaultClasspath;
/*     */   public ComponentPackager m_componentPackager;
/*     */   public PatternFilter m_componentPackageFilter;
/*     */   public ComponentListEditor m_componentListEditor;
/*     */   public ComponentInstaller m_componentInstaller;
/*     */   public Set<String> m_modulesWithJava;
/*     */   public Map<String, Long> m_componentsLastInstalled;
/*     */   public Map<String, Component.ProductTags> m_componentsLastProductTags;
/*     */   public boolean m_isServerGenerationNeeded;
/*     */   public List<String> m_labelManifest;
/*     */ 
/*     */   public void init(Map<String, String> environment)
/*     */   {
/* 127 */     if (environment == null)
/*     */     {
/* 129 */       environment = System.getenv();
/*     */     }
/* 131 */     this.m_environment = environment;
/* 132 */     if (s_iso8601 == null)
/*     */     {
/* 134 */       DateFormat fmt = BuildEnvironment.s_iso8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
/* 135 */       TimeZone tzUTC = TimeZone.getTimeZone("UTC");
/* 136 */       fmt.getCalendar().setTimeZone(tzUTC);
/*     */     }
/* 138 */     if (this.m_properties == null)
/*     */     {
/* 140 */       Properties sysProps = System.getProperties();
/* 141 */       this.m_properties = new IdcProperties(sysProps);
/*     */     }
/* 143 */     if (this.m_binder == null)
/*     */     {
/* 145 */       DataBinder binder = this.m_binder = new DataBinder();
/* 146 */       binder.setLocalData(this.m_properties);
/*     */     }
/* 148 */     if (this.m_loader == null)
/*     */     {
/* 150 */       ClassLoader classLoader = super.getClass().getClassLoader();
/* 151 */       if (classLoader instanceof IdcClassLoader)
/*     */       {
/* 153 */         this.m_loader = ((IdcClassLoader)classLoader);
/*     */       }
/*     */     }
/* 156 */     if (this.m_context == null)
/*     */     {
/* 158 */       this.m_context = new ExecutionContextAdaptor();
/*     */     }
/* 160 */     if (this.m_modulesContainers == null)
/*     */     {
/* 162 */       this.m_modulesContainers = new HashMap();
/*     */     }
/* 164 */     if (this.m_modules == null)
/*     */     {
/* 166 */       this.m_modules = new HashMap();
/*     */     }
/* 168 */     if (this.m_downloader == null)
/*     */     {
/* 170 */       this.m_downloader = new HTTPDownloader(null);
/*     */     }
/* 172 */     if (this.m_messageDigest == null)
/*     */     {
/*     */       try
/*     */       {
/* 176 */         this.m_messageDigest = MessageDigest.getInstance("MD5");
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 180 */         throw new RuntimeException("missing MD5", t);
/*     */       }
/*     */     }
/* 183 */     if (this.m_validatedFiles == null)
/*     */     {
/* 185 */       this.m_validatedFiles = new HashSet();
/*     */     }
/* 187 */     if (this.m_javaFilenameFilter == null)
/*     */     {
/* 189 */       this.m_javaFilenameFilter = TextUtils.createPatternFilterFromWildcards("+**.java");
/*     */     }
/* 191 */     if (this.m_classFilenameFilter == null)
/*     */     {
/* 193 */       this.m_classFilenameFilter = TextUtils.createPatternFilterFromWildcards("+**.class");
/*     */     }
/* 195 */     if (this.m_componentPackager == null)
/*     */     {
/* 197 */       this.m_componentPackager = new ComponentPackager();
/* 198 */       this.m_componentPackager.m_trace = this.m_trace;
/*     */     }
/* 200 */     if (this.m_componentInstaller != null)
/*     */       return;
/* 202 */     this.m_componentInstaller = new ComponentInstaller();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 208 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99795 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.BuildEnvironment
 * JD-Core Version:    0.5.4
 */