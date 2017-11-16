/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.PathUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.io.HTTPDownloader.StateListener;
/*     */ import intradoc.tools.utils.SimpleFileUtils;
/*     */ import intradoc.tools.utils.TextUtils;
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.MapUtils;
/*     */ import intradoc.util.PatternFilter;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class NativeModule extends Module
/*     */ {
/*     */   protected Binary[] m_nativeBinaries;
/*     */   protected Platform[] m_nativePlatforms;
/*     */ 
/*     */   public List<Module.FetchRule> loadFetchRules()
/*     */     throws IdcException
/*     */   {
/*  75 */     if (this.m_fetchRulesTable == null)
/*     */     {
/*  77 */       loadRulesTables();
/*     */     }
/*  79 */     return super.loadFetchRules();
/*     */   }
/*     */ 
/*     */   public List<Module.ExtractRule> loadExtractRules()
/*     */     throws IdcException
/*     */   {
/*  85 */     if (this.m_extractRulesTable == null)
/*     */     {
/*  87 */       loadRulesTables();
/*     */     }
/*  89 */     return super.loadExtractRules();
/*     */   }
/*     */ 
/*     */   public void updateDependencies(int flags, HTTPDownloader.StateListener callback)
/*     */     throws IdcException
/*     */   {
/*  95 */     super.updateDependencies(flags, callback);
/*  96 */     createLauncherScripts();
/*     */   }
/*     */ 
/*     */   public void loadRulesTables()
/*     */     throws IdcException
/*     */   {
/* 110 */     BuildEnvironment env = this.m_manager.m_env;
/* 111 */     DataBinder buildConfig = this.m_buildConfig;
/* 112 */     ExecutionContext cxt = env.m_context;
/* 113 */     Properties pathProps = new IdcProperties(env.m_properties);
/* 114 */     int pathFlags = PathUtils.F_VARS_MUST_EXIST;
/*     */ 
/* 116 */     String sourcePath = buildConfig.getLocal("source");
/* 117 */     String targetPath = buildConfig.getLocal("target");
/* 118 */     DataResultSet fetchRules = this.m_fetchRulesTable = (DataResultSet)buildConfig.getResultSet("FetchRules");
/* 119 */     if (fetchRules == null)
/*     */     {
/* 121 */       String[] fieldNames = { "source", "localPath", "flags" };
/* 122 */       fetchRules = this.m_fetchRulesTable = new DataResultSet(fieldNames);
/*     */     }
/* 124 */     int fetchIndexSource = ResultSetUtils.getIndexMustExist(fetchRules, "source");
/* 125 */     int fetchIndexTarget = ResultSetUtils.getIndexMustExist(fetchRules, "localPath");
/* 126 */     int fetchIndexFlags = fetchRules.getFieldInfoIndex("flags");
/* 127 */     if (fetchIndexFlags < 0)
/*     */     {
/* 129 */       List newFields = new ArrayList(1);
/* 130 */       newFields.add("flags");
/* 131 */       fetchRules.mergeFieldsWithFlags(newFields, 0);
/* 132 */       fetchIndexFlags = ResultSetUtils.getIndexMustExist(fetchRules, "flags");
/*     */     }
/* 134 */     DataResultSet extractRules = this.m_extractRulesTable = (DataResultSet)buildConfig.getResultSet("ExtractRules");
/* 135 */     if (extractRules == null)
/*     */     {
/* 137 */       String[] fieldNames = { "source", "localPath" };
/* 138 */       extractRules = this.m_extractRulesTable = new DataResultSet(fieldNames);
/*     */     }
/* 140 */     int extractIndexSource = ResultSetUtils.getIndexMustExist(extractRules, "source");
/* 141 */     int extractIndexTarget = ResultSetUtils.getIndexMustExist(extractRules, "localPath");
/* 142 */     StringBuilder filenameBuilder = new StringBuilder();
/* 143 */     String nativeDir = "$BuildDir/native/";
/* 144 */     int nativeDirLength = "$BuildDir/native/".length();
/*     */ 
/* 146 */     Binary[] binaries = getOrComputeNativeBinaries();
/* 147 */     Platform[] platforms = getOrComputeNativePlatforms();
/* 148 */     for (int p = 0; p < platforms.length; ++p)
/*     */     {
/* 150 */       Platform platform = platforms[p];
/* 151 */       if (platform.m_isDisabled) {
/*     */         continue;
/*     */       }
/*     */ 
/* 155 */       String platformName = platform.m_platformName; String idcPlatformName = platform.m_idcPlatformName;
/* 156 */       pathProps.put("platform", platformName);
/* 157 */       pathProps.put("idcPlatform", idcPlatformName);
/* 158 */       String zipSource = PathUtils.substitutePathVariables(sourcePath, pathProps, null, pathFlags, cxt);
/* 159 */       String zipFilename = FileUtils.getName(zipSource);
/* 160 */       filenameBuilder.setLength(0);
/* 161 */       filenameBuilder.append("$BuildDir/native/");
/* 162 */       filenameBuilder.append(zipFilename);
/* 163 */       String zipTarget = filenameBuilder.toString();
/* 164 */       Vector fetchRow = fetchRules.createEmptyRow();
/* 165 */       fetchRow.set(fetchIndexSource, zipSource);
/* 166 */       fetchRow.set(fetchIndexTarget, zipTarget);
/* 167 */       fetchRules.addRow(fetchRow);
/*     */ 
/* 169 */       filenameBuilder.setLength(nativeDirLength);
/* 170 */       filenameBuilder.append(platformName);
/* 171 */       filenameBuilder.append('/');
/* 172 */       String extractTarget = filenameBuilder.toString();
/* 173 */       Vector extractRow = extractRules.createEmptyRow();
/* 174 */       extractRow.set(extractIndexSource, zipTarget);
/* 175 */       extractRow.set(extractIndexTarget, extractTarget);
/* 176 */       extractRules.addRow(extractRow);
/*     */ 
/* 178 */       for (int b = 0; b < binaries.length; ++b)
/*     */       {
/* 180 */         Binary binary = binaries[b];
/* 181 */         if (!binary.m_platformFilter.isIncluded(platformName)) {
/*     */           continue;
/*     */         }
/*     */ 
/* 185 */         String dir = binary.m_type; String extension = ""; String type = binary.m_type;
/* 186 */         if (type.equals("bin"))
/*     */         {
/* 188 */           extension = platform.m_executableExtension;
/*     */         }
/* 190 */         else if (type.equals("lib"))
/*     */         {
/* 192 */           extension = platform.m_libraryExtension;
/*     */         }
/* 194 */         else if (type.equals("script"))
/*     */         {
/* 196 */           dir = "bin";
/* 197 */           extension = platform.m_scriptExtension;
/*     */         }
/*     */ 
/* 200 */         filenameBuilder.setLength(0);
/* 201 */         filenameBuilder.append(binary.m_name);
/* 202 */         filenameBuilder.append(extension);
/* 203 */         String filename = filenameBuilder.toString();
/* 204 */         filenameBuilder.insert(0, '/');
/* 205 */         filenameBuilder.insert(0, platformName);
/* 206 */         filenameBuilder.insert(0, "$BuildDir/native/");
/* 207 */         String sourceFilepath = filenameBuilder.toString();
/* 208 */         pathProps.put("extension", extension);
/* 209 */         pathProps.put("filename", filename);
/* 210 */         pathProps.put("typeDir", dir);
/* 211 */         String targetFilepath = PathUtils.substitutePathVariables(targetPath, pathProps, null, pathFlags, cxt);
/* 212 */         Vector copyRow = fetchRules.createEmptyRow();
/* 213 */         copyRow.set(fetchIndexSource, sourceFilepath);
/* 214 */         copyRow.set(fetchIndexTarget, targetFilepath);
/* 215 */         copyRow.set(fetchIndexFlags, "isForBuild");
/* 216 */         fetchRules.addRow(copyRow);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void createLauncherScripts()
/*     */     throws IdcException
/*     */   {
/* 228 */     BuildEnvironment env = this.m_manager.m_env;
/* 229 */     File branchDir = env.m_branchDir;
/* 230 */     File intradocDir = env.m_intradocDir;
/* 231 */     File intradocNativeDir = new File(intradocDir, "native");
/* 232 */     intradocNativeDir.mkdir();
/* 233 */     File binDir = new File(intradocDir, "bin");
/* 234 */     binDir.mkdir();
/*     */ 
/* 236 */     Pattern excludePattern = Pattern.compile("^#idc");
/* 237 */     File launcherSourceFile = new File(branchDir, "integrations/native/Launcher/Launcher.sh");
/* 238 */     long launcherSourceTime = launcherSourceFile.lastModified();
/* 239 */     File[] launcherTargetDirs = { intradocNativeDir, binDir };
/* 240 */     for (int t = launcherTargetDirs.length - 1; t >= 0; --t)
/*     */     {
/* 242 */       File launcherTargetFile = new File(launcherTargetDirs[t], "Launcher.sh");
/* 243 */       long launcherTargetTime = launcherTargetFile.lastModified();
/* 244 */       if ((launcherTargetTime != 0L) && (launcherTargetTime == launcherSourceTime))
/*     */         continue;
/*     */       try
/*     */       {
/* 248 */         SimpleFileUtils.copyFilteredTextFile(launcherSourceFile, launcherTargetFile, "\n", excludePattern);
/*     */       }
/*     */       catch (IOException ioe)
/*     */       {
/* 252 */         String msg = new StringBuilder().append("unable to copy ").append(launcherSourceFile.getPath()).append(" to ").append(launcherTargetFile.getPath()).toString();
/*     */ 
/* 254 */         throw new ServiceException(msg, ioe);
/*     */       }
/* 256 */       launcherTargetFile.setLastModified(launcherSourceTime);
/* 257 */       launcherTargetFile.setExecutable(true, false);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Binary[] getOrComputeNativeBinaries()
/*     */     throws IdcException
/*     */   {
/* 268 */     Binary[] binaries = this.m_nativeBinaries;
/* 269 */     if (binaries == null)
/*     */     {
/* 271 */       DataResultSet resultSet = (DataResultSet)this.m_buildConfig.getResultSet("Binaries");
/* 272 */       String[] fieldNames = { "name", "type", "platformFilter" };
/* 273 */       FieldInfo[] fields = ResultSetUtils.createInfoList(resultSet, fieldNames, true);
/* 274 */       int nameIndex = fields[0].m_index; int typeIndex = fields[1].m_index; int platformFilterIndex = fields[2].m_index;
/* 275 */       int numBinaries = resultSet.getNumRows();
/* 276 */       binaries = this.m_nativeBinaries = new Binary[numBinaries];
/*     */ 
/* 278 */       for (int b = numBinaries - 1; b >= 0; --b)
/*     */       {
/* 280 */         resultSet.setCurrentRow(b);
/* 281 */         Binary binary = binaries[b] =  = new Binary();
/* 282 */         binary.m_name = resultSet.getStringValue(nameIndex);
/* 283 */         String type = binary.m_type = resultSet.getStringValue(typeIndex);
/* 284 */         String filterString = resultSet.getStringValue(platformFilterIndex);
/* 285 */         binary.m_platformFilter = TextUtils.createPatternFilterFromWildcards(filterString);
/*     */ 
/* 287 */         if ((type.equals("bin")) || (type.equals("lib")) || (type.equals("script")))
/*     */           continue;
/* 289 */         throw new DataException(new StringBuilder().append("unknown type \"").append(type).append("\" for native binary ").append(binary.m_name).toString());
/*     */       }
/*     */     }
/*     */ 
/* 293 */     return binaries;
/*     */   }
/*     */ 
/*     */   public Platform[] getOrComputeNativePlatforms()
/*     */     throws IdcException
/*     */   {
/* 302 */     Platform[] platforms = this.m_nativePlatforms;
/* 303 */     if (platforms == null)
/*     */     {
/* 305 */       DataBinder nativeBinder = this.m_buildConfig;
/* 306 */       DataResultSet resultSet = (DataResultSet)nativeBinder.getResultSet("Platforms");
/* 307 */       String[] fieldNames = { "platform", "idcPlatform", "flags" };
/* 308 */       FieldInfo[] fields = ResultSetUtils.createInfoList(resultSet, fieldNames, true);
/* 309 */       int platformIndex = fields[0].m_index; int idcPlatformIndex = fields[1].m_index;
/* 310 */       int flagsIndex = fields[2].m_index;
/* 311 */       int numPlatforms = resultSet.getNumRows();
/* 312 */       platforms = this.m_nativePlatforms = new Platform[numPlatforms];
/* 313 */       String defaultExecutableExtension = nativeBinder.getLocal("defaultExecutableExtension");
/* 314 */       String defaultLibraryExtension = nativeBinder.getLocal("defaultLibraryExtension");
/* 315 */       String defaultScriptExtension = nativeBinder.getLocal("defaultScriptExtension");
/* 316 */       Map flagsMap = new HashMap();
/*     */ 
/* 318 */       for (int p = numPlatforms - 1; p >= 0; --p)
/*     */       {
/* 320 */         resultSet.setCurrentRow(p);
/* 321 */         Platform platform = platforms[p] =  = new Platform();
/* 322 */         platform.m_platformName = resultSet.getStringValue(platformIndex);
/* 323 */         platform.m_idcPlatformName = resultSet.getStringValue(idcPlatformIndex);
/* 324 */         String flags = resultSet.getStringValue(flagsIndex);
/* 325 */         flagsMap.clear();
/* 326 */         MapUtils.fillMapFromOptionsString(flagsMap, flags);
/* 327 */         platform.m_isDisabled = MapUtils.getBoolValueFromMap(flagsMap, "isDisabled", false);
/* 328 */         String executableExtension = (String)flagsMap.get("executableExtension");
/* 329 */         if (executableExtension == null)
/*     */         {
/* 331 */           executableExtension = (defaultExecutableExtension != null) ? defaultExecutableExtension : "";
/*     */         }
/* 333 */         platform.m_executableExtension = executableExtension;
/* 334 */         String libraryExtension = (String)flagsMap.get("libraryExtension");
/* 335 */         if (libraryExtension == null)
/*     */         {
/* 337 */           libraryExtension = (defaultLibraryExtension != null) ? defaultLibraryExtension : "";
/*     */         }
/* 339 */         platform.m_libraryExtension = libraryExtension;
/* 340 */         String scriptExtension = (String)flagsMap.get("scriptExtension");
/* 341 */         if (scriptExtension == null)
/*     */         {
/* 343 */           scriptExtension = (defaultScriptExtension != null) ? defaultScriptExtension : "";
/*     */         }
/* 345 */         platform.m_scriptExtension = scriptExtension;
/*     */       }
/*     */     }
/* 348 */     return platforms;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 354 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99573 $";
/*     */   }
/*     */ 
/*     */   protected class Platform
/*     */   {
/*     */     public String m_platformName;
/*     */     public String m_idcPlatformName;
/*     */     public boolean m_isDisabled;
/*     */     public String m_executableExtension;
/*     */     public String m_libraryExtension;
/*     */     public String m_scriptExtension;
/*     */ 
/*     */     protected Platform()
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   protected class Binary
/*     */   {
/*     */     public String m_name;
/*     */     public String m_type;
/*     */     public PatternFilter m_platformFilter;
/*     */ 
/*     */     protected Binary()
/*     */     {
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.NativeModule
 * JD-Core Version:    0.5.4
 */