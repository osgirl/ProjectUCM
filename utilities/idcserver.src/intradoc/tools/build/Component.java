/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.PathUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.io.zip.IdcZipFile;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.server.utils.ComponentInstaller;
/*     */ import intradoc.server.utils.ComponentListEditor;
/*     */ import intradoc.tools.utils.SimpleFileUtils;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.MapUtils;
/*     */ import intradoc.util.PatternFilter;
/*     */ import intradoc.zip.IdcZipFunctions;
/*     */ import java.io.File;
/*     */ import java.util.Arrays;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class Component extends Module
/*     */ {
/*     */   public File m_componentBinderFile;
/*     */   public ProductTags m_productTags;
/*     */   public DataBinder m_componentBinder;
/*     */   public boolean m_wasInstalled;
/*     */   public boolean m_haveTagsChanged;
/*     */   protected File m_componentZipFile;
/*     */ 
/*     */   public void init(BuildManager manager, File moduleDir)
/*     */     throws IdcException
/*     */   {
/*  60 */     init(manager, moduleDir, null);
/*     */   }
/*     */ 
/*     */   public void init(BuildManager manager, File moduleDir, File componentBinderFile) throws IdcException
/*     */   {
/*  65 */     super.init(manager, moduleDir);
/*  66 */     if (componentBinderFile == null)
/*     */     {
/*  68 */       String componentBinderName = this.m_moduleName + ".hda";
/*  69 */       componentBinderFile = new File(moduleDir, componentBinderName);
/*     */     }
/*  71 */     this.m_componentBinderFile = componentBinderFile;
/*     */ 
/*  73 */     this.m_productTags = new ProductTags();
/*  74 */     Properties properties = this.m_properties;
/*     */ 
/*  77 */     properties.put("COMPONENT_DIR", this.m_moduleDirname);
/*     */   }
/*     */ 
/*     */   public void reload()
/*     */     throws IdcException
/*     */   {
/*  83 */     super.reload();
/*  84 */     if ((this.m_componentBinder != null) || (this.m_componentBinderFile == null))
/*     */       return;
/*  86 */     String componentBinderPathname = this.m_componentBinderFile.getPath();
/*  87 */     this.m_componentBinder = ResourceLoader.loadDataBinderFromFile(componentBinderPathname);
/*     */   }
/*     */ 
/*     */   public void appendClasspathTo(List<String> classpath, int flags)
/*     */     throws IdcException
/*     */   {
/* 103 */     super.appendClasspathTo(classpath, flags);
/*     */ 
/* 105 */     int firstIndex = classpath.size();
/* 106 */     DataBinder componentBinder = this.m_componentBinder;
/* 107 */     String classpathString = componentBinder.getLocal("classpath");
/* 108 */     if ((classpathString != null) && ((flags & 0x1) == 0))
/*     */     {
/* 110 */       appendPathElementsStringTo(classpath, classpathString);
/*     */     }
/*     */ 
/* 113 */     DataResultSet vars = (DataResultSet)componentBinder.getResultSet("StateCfgExportedVars");
/* 114 */     if ((vars != null) && ((flags & 0x2) == 0))
/*     */     {
/* 116 */       String[] fieldNames = { "name", "value" };
/* 117 */       FieldInfo[] fields = ResultSetUtils.createInfoList(vars, fieldNames, true);
/* 118 */       int nameIndex = fields[0].m_index; int valueIndex = fields[1].m_index;
/* 119 */       for (vars.first(); vars.isRowPresent(); vars.next())
/*     */       {
/* 121 */         String name = vars.getStringValue(nameIndex);
/* 122 */         if (!name.contains("CLASSPATH")) {
/*     */           continue;
/*     */         }
/*     */ 
/* 126 */         String path = vars.getStringValue(valueIndex);
/* 127 */         appendPathElementsStringTo(classpath, path);
/*     */       }
/*     */     }
/*     */ 
/* 131 */     if ((flags & 0x4) == 0)
/*     */       return;
/* 133 */     File moduleDir = this.m_moduleDir;
/* 134 */     Properties props = this.m_properties;
/* 135 */     ExecutionContext context = this.m_manager.m_env.m_context;
/* 136 */     int pathUtilsFlags = PathUtils.F_KEEP_UNKNOWN_VARS;
/*     */ 
/* 138 */     PackageRule.Group packages = loadPackageRules();
/*     */     String path;
/* 139 */     for (int i = classpath.size() - 1; i >= firstIndex; --i)
/*     */     {
/* 141 */       path = (String)classpath.get(i);
/* 142 */       path = PathUtils.substitutePathVariables(path, props, null, pathUtilsFlags, context);
/* 143 */       for (PackageRule pkg : packages)
/*     */       {
/* 145 */         File packageFile = pkg.getTargetFile(moduleDir);
/* 146 */         String packagePath = packageFile.getPath();
/* 147 */         if (path.equals(packagePath))
/*     */         {
/* 149 */           classpath.remove(i);
/* 150 */           break;
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean buildAndPackage(File targetDir)
/*     */     throws IdcException
/*     */   {
/* 160 */     super.buildAndPackage(targetDir);
/*     */ 
/* 162 */     if (this.m_componentBinderFile == null)
/*     */     {
/* 164 */       return false;
/*     */     }
/* 166 */     BuildEnvironment env = this.m_manager.m_env;
/*     */ 
/* 170 */     if (env.m_isReleng)
/*     */     {
/* 172 */       JarSigningModule.setupSignerAndSignPackages(this.m_manager, this);
/*     */     }
/*     */ 
/* 175 */     ComponentPackager packager = env.m_componentPackager;
/* 176 */     if (packager == null)
/*     */     {
/* 178 */       packager = new ComponentPackager();
/* 179 */       packager.m_trace = env.m_trace;
/*     */     }
/* 181 */     packager.m_flags = 1;
/* 182 */     if (env.m_isReleng)
/*     */     {
/* 184 */       packager.m_flags |= 2;
/*     */     }
/* 186 */     File moduleDir = this.m_moduleDir;
/* 187 */     packager.init(moduleDir, this.m_componentBinderFile);
/* 188 */     PatternFilter packageFilter = env.m_componentPackageFilter;
/* 189 */     if (packageFilter == null)
/*     */     {
/* 191 */       packageFilter = env.m_componentPackageFilter = new PatternFilter();
/* 192 */       BuildUtils.addStandardBuildFilters(env, packageFilter);
/*     */     }
/* 194 */     long latestModified = SimpleFileUtils.scanFilesForLatestModified(moduleDir, env.m_componentPackageFilter, null);
/*     */ 
/* 196 */     File zipFile = this.m_componentZipFile = packager.computeZipFile(targetDir);
/* 197 */     long zipModified = zipFile.lastModified();
/* 198 */     if ((zipModified == 0L) || (latestModified > zipModified))
/*     */     {
/* 201 */       packager.packageComponent(zipFile);
/*     */     }
/* 203 */     return true;
/*     */   }
/*     */ 
/*     */   public void install()
/*     */     throws IdcException
/*     */   {
/* 213 */     BuildEnvironment env = this.m_manager.m_env;
/* 214 */     String componentName = this.m_moduleName;
/* 215 */     Map componentsLastProductTags = env.m_componentsLastProductTags;
/* 216 */     ProductTags lastProductTags = (ProductTags)componentsLastProductTags.get(componentName);
/* 217 */     this.m_haveTagsChanged = (!this.m_productTags.equals(lastProductTags));
/*     */ 
/* 219 */     this.m_wasInstalled = false;
/* 220 */     File zipFile = this.m_componentZipFile;
/* 221 */     if (zipFile == null)
/*     */     {
/* 223 */       return;
/*     */     }
/* 225 */     ComponentListEditor editor = env.m_componentListEditor;
/* 226 */     long zipModified = zipFile.lastModified();
/* 227 */     Map componentsLastInstalled = env.m_componentsLastInstalled;
/* 228 */     if (editor.isComponentNameUnique(componentName))
/*     */     {
/* 231 */       componentsLastInstalled.remove(componentName);
/*     */     }
/* 233 */     Long lastInstalled = (Long)componentsLastInstalled.get(componentName);
/*     */ 
/* 235 */     if ((lastInstalled != null) && (zipModified <= lastInstalled.longValue()))
/*     */     {
/* 237 */       return;
/*     */     }
/* 239 */     env.m_trace.report(6, new Object[] { "installing ", this.m_moduleName, " ..." });
/*     */ 
/* 241 */     ComponentInstaller installer = env.m_componentInstaller;
/* 242 */     IdcZipFile zip = IdcZipFunctions.newIdcZipFile(zipFile);
/* 243 */     DataBinder manifestBinder = IdcZipFunctions.extractFileAsDataBinder(zip, "manifest.hda");
/* 244 */     String[] locationAndName = installer.retrieveComponentNameAndLocation(manifestBinder);
/* 245 */     String location = locationAndName[0]; String installID = locationAndName[1];
/* 246 */     String componentBinderPathname = "component/" + location;
/* 247 */     DataBinder componentBinder = IdcZipFunctions.extractFileAsDataBinder(zip, componentBinderPathname);
/* 248 */     installer.m_disableSafeMode = true;
/* 249 */     String zipPathname = zipFile.getPath();
/* 250 */     Map args = MapUtils.fillMapFromOptionsString(null, "Install=true,PruneFiles=true,Strict=true");
/* 251 */     args.put("ZipName", zipPathname);
/* 252 */     installer.executeInstaller(componentBinder, manifestBinder, installID, componentName, args);
/*     */ 
/* 254 */     editor.enableOrDisableComponent(componentName, true);
/* 255 */     componentsLastInstalled.put(componentName, Long.valueOf(zipModified));
/* 256 */     componentsLastProductTags.put(componentName, this.m_productTags);
/* 257 */     this.m_wasInstalled = true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 377 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105370 $";
/*     */   }
/*     */ 
/*     */   public static class ProductTags extends HashMap<String, Component.ProductTag>
/*     */   {
/*     */     public ProductTags()
/*     */     {
/*     */     }
/*     */ 
/*     */     public ProductTags(String productTagsString)
/*     */     {
/* 335 */       addFromString(productTagsString);
/*     */     }
/*     */ 
/*     */     public void addFromString(String productTagsString)
/*     */     {
/* 340 */       if ((productTagsString == null) || (productTagsString.length() <= 0))
/*     */         return;
/* 342 */       String[] productTags = productTagsString.split(",");
/* 343 */       for (int t = productTags.length - 1; t >= 0; --t)
/*     */       {
/* 345 */         String productTagString = productTags[t];
/* 346 */         Component.ProductTag productTag = new Component.ProductTag(productTagString);
/* 347 */         put(productTag.m_productName, productTag);
/*     */       }
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 355 */       Set keySet = keySet();
/* 356 */       int numKeys = keySet.size();
/* 357 */       String[] keys = new String[numKeys];
/* 358 */       keySet.toArray(keys);
/* 359 */       Arrays.sort(keys);
/* 360 */       StringBuilder sb = new StringBuilder();
/* 361 */       for (int k = 0; k < numKeys; ++k)
/*     */       {
/* 363 */         if (k > 0)
/*     */         {
/* 365 */           sb.append(',');
/*     */         }
/* 367 */         Component.ProductTag productTag = (Component.ProductTag)get(keys[k]);
/* 368 */         productTag.appendTo(sb);
/*     */       }
/* 370 */       return sb.toString();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static class ProductTag
/*     */   {
/*     */     public String m_productName;
/*     */     public boolean m_isEnabled;
/*     */ 
/*     */     public ProductTag()
/*     */     {
/* 272 */       this.m_productName = "";
/* 273 */       this.m_isEnabled = true;
/*     */     }
/*     */ 
/*     */     public ProductTag(String productTagString)
/*     */     {
/* 279 */       if ((productTagString == null) || (productTagString.length() <= 0))
/*     */         return;
/* 281 */       String[] productTagArray = productTagString.split(":");
/* 282 */       this.m_productName = productTagArray[0];
/* 283 */       if (productTagArray.length <= 1)
/*     */         return;
/* 285 */       String status = productTagArray[1].toLowerCase();
/* 286 */       if (!status.startsWith("disable"))
/*     */         return;
/* 288 */       this.m_isEnabled = false;
/*     */     }
/*     */ 
/*     */     public void appendTo(StringBuilder sb)
/*     */     {
/* 296 */       sb.append(this.m_productName);
/* 297 */       sb.append(':');
/* 298 */       sb.append((this.m_isEnabled) ? "Enabled" : "Disabled");
/*     */     }
/*     */ 
/*     */     public boolean equals(Object obj)
/*     */     {
/* 304 */       String productName = this.m_productName;
/* 305 */       if ((productName == null) || (!obj instanceof ProductTag))
/*     */       {
/* 307 */         return false;
/*     */       }
/* 309 */       ProductTag productTag = (ProductTag)obj;
/* 310 */       return (this.m_isEnabled == productTag.m_isEnabled) && (productName.equals(productTag.m_productName));
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 316 */       StringBuilder sb = new StringBuilder(this.m_productName);
/* 317 */       appendTo(sb);
/* 318 */       return sb.toString();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.Component
 * JD-Core Version:    0.5.4
 */