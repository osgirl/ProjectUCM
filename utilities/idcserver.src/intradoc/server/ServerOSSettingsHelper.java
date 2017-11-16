/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.OSSettingsHelper;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ServerOSSettingsHelper
/*     */   implements OSSettingsHelper
/*     */ {
/*     */   public static final int TABLE = 0;
/*     */   public static final int PROPERTY = 1;
/*     */   protected Map m_substitutionMap;
/*     */   public List<String> m_defaultBaseDirList;
/*     */ 
/*     */   public boolean checkOption(Map options, String option)
/*     */   {
/*  48 */     return options.get(option) != null;
/*     */   }
/*     */ 
/*     */   public String getOption(Map options, String option)
/*     */   {
/*  56 */     return (String)options.get(option);
/*     */   }
/*     */ 
/*     */   public Map normalizeOSPath(String abstractPath, Map options)
/*     */     throws ServiceException
/*     */   {
/*  65 */     HashMap map = new HashMap();
/*  66 */     String osName = EnvUtils.getOSName();
/*  67 */     if (checkOption(options, "type_webserverobject"))
/*     */     {
/*  69 */       String webOSName = (String)getConfigObject("WebServerOSName", osName, options, 1);
/*     */ 
/*  71 */       if (webOSName != null)
/*     */       {
/*  73 */         osName = webOSName;
/*     */       }
/*     */     }
/*  76 */     computeBestPathMappingForOS(osName, map, abstractPath, options);
/*  77 */     return map;
/*     */   }
/*     */ 
/*     */   public void computeBestPathMappingForOS(String osName, Map map, String abstractPath, Map options)
/*     */     throws ServiceException
/*     */   {
/*  88 */     DataResultSet platformConfig = (DataResultSet)getConfigObject("PlatformConfigTable", null, options, 0);
/*     */ 
/*  91 */     FieldInfo platformField = new FieldInfo();
/*  92 */     platformConfig.getFieldInfo("Platform", platformField);
/*  93 */     Vector row = platformConfig.findRow(platformField.m_index, osName);
/*  94 */     if (row == null)
/*     */     {
/*  96 */       String msg = LocaleUtils.encodeMessage("syPlatformNotSupported1", null, osName);
/*     */ 
/*  98 */       throw new ServiceException(msg);
/*     */     }
/* 100 */     Properties props = platformConfig.getCurrentRowProps();
/*     */ 
/* 102 */     computeBestPathMappingForOSProperties(map, abstractPath, props, options);
/*     */   }
/*     */ 
/*     */   public Object parseAbstractPath(String abstractPath)
/*     */   {
/* 112 */     ArrayList l = new ArrayList();
/* 113 */     if (abstractPath != null)
/*     */     {
/* 115 */       int last = 0;
/* 116 */       int p = -1;
/* 117 */       while ((p = abstractPath.indexOf(47, last)) >= 0)
/*     */       {
/* 119 */         if (p == 0)
/*     */         {
/* 121 */           ++last;
/*     */         }
/*     */ 
/* 124 */         String part = abstractPath.substring(last, p);
/* 125 */         l.add(part);
/* 126 */         last = p + 1;
/*     */       }
/* 128 */       if (last < abstractPath.length())
/*     */       {
/* 130 */         String part = abstractPath.substring(last);
/* 131 */         l.add(part);
/*     */       }
/*     */     }
/* 134 */     return l.toArray(new String[0]);
/*     */   }
/*     */ 
/*     */   public String encodeAbstractPath(Object abstractPath)
/*     */   {
/* 139 */     IdcStringBuilder b = new IdcStringBuilder();
/* 140 */     String[] path = (String[])(String[])abstractPath;
/* 141 */     boolean append2 = false;
/* 142 */     for (int i = 0; i < path.length; ++i)
/*     */     {
/* 144 */       String s = path[i];
/* 145 */       if (s.length() == 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 149 */       if (append2)
/*     */       {
/* 151 */         b.append2('/', path[i]);
/*     */       }
/*     */       else
/*     */       {
/* 155 */         b.append(path[i]);
/*     */       }
/* 157 */       append2 = true;
/*     */     }
/* 159 */     return b.toString();
/*     */   }
/*     */ 
/*     */   public void computeBestPathMappingForOSProperties(Map map, String abstractPath, Properties props, Map options)
/*     */     throws ServiceException
/*     */   {
/* 170 */     List baseDirList = null;
/* 171 */     Object dirObj = options.get("base_directory");
/* 172 */     if (dirObj != null)
/*     */     {
/* 174 */       if (dirObj instanceof String)
/*     */       {
/* 176 */         baseDirList = new ArrayList();
/* 177 */         baseDirList.add((String)dirObj);
/*     */       }
/* 179 */       else if (dirObj instanceof List)
/*     */       {
/* 181 */         baseDirList = (List)dirObj;
/*     */       }
/*     */     }
/* 184 */     if (baseDirList == null)
/*     */     {
/* 186 */       if (this.m_defaultBaseDirList == null)
/*     */       {
/* 188 */         Map subMap = this.m_substitutionMap;
/*     */ 
/* 190 */         baseDirList = new ArrayList();
/* 191 */         String prefix = "OSDir_";
/* 192 */         Map safeEnv = SharedObjects.getSafeEnvironment();
/* 193 */         Map secureEnv = SharedObjects.getSecureEnvironment();
/* 194 */         FileUtils.computeAndCaptureOsDirValues(prefix, safeEnv, subMap, baseDirList, 78);
/* 195 */         FileUtils.computeAndCaptureOsDirValues(prefix, secureEnv, subMap, baseDirList, 78);
/* 196 */         baseDirList.add(FileUtils.directorySlashes(DirectoryLocator.getNativeDirectory()));
/*     */       }
/*     */       else
/*     */       {
/* 201 */         baseDirList = this.m_defaultBaseDirList;
/*     */       }
/* 205 */     }
/*     */ IdcStringBuilder b = new IdcStringBuilder();
/* 206 */     boolean shouldBeFile = !checkOption(options, "type_directory");
/*     */     String[] pathInfo;
/*     */     ArrayList attemptedPaths;
/*     */     Iterator i$;
/*     */     try {
/* 209 */       pathInfo = (String[])(String[])parseAbstractPath(abstractPath);
/* 210 */       abstractPath = encodeAbstractPath(pathInfo);
/*     */ 
/* 212 */       boolean strictWebServerObjectPlatforms = getConfigObject("WebServerOSName", EnvUtils.getOSName(), options, 1) != null;
/*     */       String osListString;
/*     */       String osListString;
/* 214 */       if ((checkOption(options, "no_search")) || ((checkOption(options, "type_sharedobject")) && (!checkOption(options, "type_webserverobject"))) || (checkOption(options, "type_jniobject")) || ((strictWebServerObjectPlatforms) && (checkOption(options, "type_webserverobject"))))
/*     */       {
/* 219 */         osListString = props.getProperty("Platform");
/*     */       }
/*     */       else
/*     */       {
/* 223 */         osListString = props.getProperty("CompatiblePlatforms");
/*     */       }
/* 225 */       List osList = StringUtils.makeListFromSequenceSimple(osListString);
/* 226 */       attemptedPaths = new ArrayList();
/* 227 */       for (i$ = osList.iterator(); i$.hasNext(); ) { os = (String)i$.next();
/*     */ 
/* 229 */         for (String baseDir : baseDirList)
/*     */         {
/* 231 */           b.setLength(0);
/* 232 */           if (baseDirList != this.m_defaultBaseDirList)
/*     */           {
/* 234 */             baseDir = FileUtils.directorySlashes(baseDir);
/*     */           }
/* 236 */           b.append(baseDir);
/* 237 */           b.append(os);
/* 238 */           b.append('/');
/*     */ 
/* 240 */           for (int j = 0; j < pathInfo.length; ++j)
/*     */           {
/* 242 */             if (j < pathInfo.length - 1)
/*     */             {
/* 244 */               b.append2(pathInfo[j], '/');
/*     */             }
/*     */             else
/*     */             {
/* 248 */               if ((checkOption(options, "type_sharedobject")) || (checkOption(options, "type_jniobject")) || (checkOption(options, "type_webserverobject")))
/*     */               {
/* 252 */                 String prefix = "";
/* 253 */                 if (!checkOption(options, "type_webserverobject"))
/*     */                 {
/* 255 */                   prefix = props.getProperty("SharedObjectPrefix");
/*     */                 }
/* 257 */                 b.append(prefix);
/* 258 */                 b.append(pathInfo[j]);
/* 259 */                 String suffix = props.getProperty("SharedObjectSuffix");
/* 260 */                 if (checkOption(options, "type_jniobject"))
/*     */                 {
/* 262 */                   suffix = props.getProperty("JniObjectSuffix");
/*     */                 }
/* 264 */                 b.append(suffix);
/* 265 */                 break;
/* 266 */               }if (checkOption(options, "type_executable"))
/*     */               {
/* 268 */                 b.append(pathInfo[j]);
/* 269 */                 String suffix = props.getProperty("ExeSuffix");
/* 270 */                 b.append(suffix);
/* 271 */                 break;
/* 272 */               }if ((checkOption(options, "type_directory")) || (checkOption(options, "type_generic")))
/*     */               {
/* 274 */                 b.append(pathInfo[j]); break;
/*     */               }
/*     */ 
/* 278 */               String msg = LocaleUtils.encodeMessage("csOSSettingsUnknownType", null);
/*     */ 
/* 280 */               throw new ServiceException(msg);
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 285 */           String path = b.toStringNoRelease();
/* 286 */           if (SystemUtils.m_verbose)
/*     */           {
/* 288 */             Report.debug("fileaccess", "checking for " + path, null);
/*     */           }
/* 290 */           map.put("attemptedPaths", attemptedPaths);
/* 291 */           if (FileUtils.checkFile(path, shouldBeFile, false) == 0)
/*     */           {
/* 293 */             map.put("path", path);
/* 294 */             map.put("osName", os);
/* 295 */             DataResultSet platformConfig = (DataResultSet)getConfigObject("PlatformConfigTable", null, options, 0);
/*     */ 
/* 297 */             FieldInfo platformField = new FieldInfo();
/* 298 */             platformConfig.getFieldInfo("Platform", platformField);
/* 299 */             Vector row = platformConfig.findRow(platformField.m_index, os);
/* 300 */             if (row == null)
/*     */             {
/* 302 */               Report.trace("fileaccess", "binary for unknown platform '" + os + "' found", null);
/*     */ 
/* 304 */               map.put("osFamily", "<unknown>");
/*     */             }
/*     */             else
/*     */             {
/* 308 */               Properties osProps = platformConfig.getCurrentRowProps();
/* 309 */               map.put("osFamily", osProps.getProperty("Family"));
/*     */             }
/* 311 */             map.put("isSuccess", "1");
/* 312 */             foundFile(abstractPath, map, options);
/* 313 */             break;
/*     */           }
/* 315 */           attemptedPaths.add(path);
/* 316 */           if (SystemUtils.m_verbose)
/*     */           {
/* 318 */             Report.debug("fileaccess", "failed path check for os=" + os + "  path=" + path, null);
/*     */           }
/*     */         } }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/*     */       String os;
/* 326 */       b.releaseBuffers();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void foundFile(String abstractPath, Map results, Map options)
/*     */     throws ServiceException
/*     */   {
/* 333 */     if (!checkOption(options, "type_executable"))
/*     */       return;
/* 335 */     handleExecutable(abstractPath, results, options);
/*     */   }
/*     */ 
/*     */   public void handleExecutable(String abstractPath, Map results, Map options)
/*     */     throws ServiceException
/*     */   {
/* 342 */     DataResultSet info = (DataResultSet)getConfigObject("ExecutableInfo", null, options, 0);
/*     */ 
/* 344 */     String env = null;
/* 345 */     String ldpath = null;
/*     */     try
/*     */     {
/* 348 */       if (info == null)
/*     */       {
/* 350 */         if (SystemUtils.m_verbose)
/*     */         {
/* 354 */           Report.debug("fileaccess", "ExecutableInfo ResultSet missing.", null);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 359 */         ldpath = ResultSetUtils.findValue(info, "abstractPath", abstractPath, "library_path");
/*     */ 
/* 361 */         if (SystemUtils.m_verbose)
/*     */         {
/* 363 */           Report.debug("fileaccess", "found library_path " + ldpath + " for abstractPath " + abstractPath, null);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 370 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 373 */     env = EnvUtils.getLibraryPathEnvironmentVariableName() + "=" + EnvUtils.getLibraryPath();
/*     */ 
/* 375 */     if (ldpath != null)
/*     */     {
/* 377 */       String path = (String)results.get("path");
/* 378 */       path = FileUtils.fileSlashes(path);
/* 379 */       int index = path.lastIndexOf("/");
/* 380 */       String fileName = path.substring(index + 1);
/* 381 */       String dir = path.substring(0, index);
/*     */ 
/* 383 */       DataBinder binder = new DataBinder();
/* 384 */       binder.setEnvironment(new Properties(SharedObjects.getSafeEnvironment()));
/*     */ 
/* 386 */       binder.putLocal("osName", (String)results.get("osName"));
/* 387 */       binder.putLocal("IdcNativeDir", DirectoryLocator.getNativeDirectory());
/* 388 */       binder.putLocal("executablePath", path);
/* 389 */       binder.putLocal("executableDir", dir);
/* 390 */       binder.putLocal("executableName", fileName);
/* 391 */       ExecutionContext context = (ExecutionContext)options.get("ExecutionContext");
/* 392 */       if (context == null)
/*     */       {
/* 394 */         context = new ExecutionContextAdaptor();
/*     */       }
/* 396 */       PageMerger merger = new PageMerger(binder, context);
/*     */       try
/*     */       {
/* 399 */         ldpath = merger.evaluateScript(ldpath);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/*     */       }
/*     */       finally
/*     */       {
/* 407 */         merger.releaseAllTemporary();
/*     */       }
/* 409 */       List l = StringUtils.makeListFromSequence(ldpath, ',', '^', 0);
/* 410 */       char sep = EnvUtils.getPathSeparator().charAt(0);
/* 411 */       IdcStringBuilder b = new IdcStringBuilder();
/* 412 */       boolean append2 = false;
/* 413 */       for (int i = 0; i < l.size(); ++i)
/*     */       {
/* 415 */         String part = (String)l.get(i);
/* 416 */         if (part.length() == 0) {
/*     */           continue;
/*     */         }
/*     */ 
/* 420 */         if (append2)
/*     */         {
/* 422 */           b.append2(sep, (String)l.get(i));
/*     */         }
/*     */         else
/*     */         {
/* 426 */           b.append((String)l.get(0));
/*     */         }
/* 428 */         append2 = true;
/*     */       }
/* 430 */       env = env + EnvUtils.getPathSeparator() + b.toString();
/*     */     }
/* 432 */     results.put("environment_settings", env);
/*     */   }
/*     */ 
/*     */   public static Object getConfigObject(String name, String defaultValue, Map options, int type)
/*     */   {
/* 438 */     Object obj = options.get(name);
/* 439 */     if (obj != null)
/*     */     {
/* 441 */       return obj;
/*     */     }
/*     */ 
/* 444 */     if (type == 0)
/*     */     {
/* 446 */       obj = SharedObjects.getTable(name);
/*     */     }
/*     */     else
/*     */     {
/* 450 */       obj = SharedObjects.getEnvironmentValue(name);
/*     */     }
/*     */ 
/* 453 */     return obj;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 458 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95962 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ServerOSSettingsHelper
 * JD-Core Version:    0.5.4
 */