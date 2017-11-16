/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.ComponentLoader;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ComponentLocationUtils
/*     */ {
/*     */   public static final int F_NO_FORCE_DIR = 0;
/*     */   public static final int F_LOCAL_DIR = 1;
/*     */   public static final int F_HOME_DIR = 2;
/*     */ 
/*     */   public static String computeAbsoluteComponentLocation(String name)
/*     */     throws ServiceException, DataException
/*     */   {
/*  51 */     if (name == null)
/*     */     {
/*  53 */       throw new ServiceException("!csComponentNameRequired");
/*     */     }
/*     */ 
/*  57 */     ComponentListEditor compLE = ComponentListManager.m_editor;
/*  58 */     DataResultSet components = compLE.getComponentSet();
/*  59 */     int index = ResultSetUtils.getIndexMustExist(components, "name");
/*  60 */     List row = components.findRow(index, name, 0, 2);
/*  61 */     if (row == null)
/*     */     {
/*  63 */       String msg = LocaleUtils.encodeMessage("csAdminComponentDoesNotExist", null, name);
/*  64 */       throw new DataException(msg);
/*     */     }
/*  66 */     Map map = components.getCurrentRowMap();
/*     */ 
/*  69 */     String location = determineComponentLocation(map, 0);
/*  70 */     return location;
/*     */   }
/*     */ 
/*     */   public static String computeAbsoluteComponentDirectory(String name)
/*     */     throws ServiceException, DataException
/*     */   {
/*  83 */     String location = computeAbsoluteComponentLocation(name);
/*  84 */     if (location != null)
/*     */     {
/*  86 */       return FileUtils.getDirectory(location);
/*     */     }
/*  88 */     return null;
/*     */   }
/*     */ 
/*     */   public static String determineComponentLocation(Map<String, String> map, int type)
/*     */   {
/* 100 */     return determineComponentLocationWithEnv(map, type, null, false);
/*     */   }
/*     */ 
/*     */   public static String determineComponentLocationWithEnv(Map<String, String> map, int type, Map<String, String> env, boolean hasTags)
/*     */   {
/* 117 */     String absPath = null;
/* 118 */     String name = (String)map.get("name");
/* 119 */     if (name == null)
/*     */     {
/* 121 */       name = (String)map.get("ComponentName");
/*     */     }
/*     */ 
/* 125 */     String location = FileUtils.fileSlashes((String)map.get("location"));
/* 126 */     String filename = name + ".hda";
/* 127 */     boolean isComponentNamedHDA = true;
/* 128 */     if ((location != null) && (location.length() > 0))
/*     */     {
/* 130 */       String newFilename = FileUtils.getName(location);
/* 131 */       if (!filename.equals(newFilename))
/*     */       {
/* 133 */         filename = newFilename;
/* 134 */         isComponentNamedHDA = false;
/*     */       }
/*     */     }
/*     */ 
/* 138 */     boolean isForceHome = type == 2;
/* 139 */     boolean isForceLocal = type == 1;
/* 140 */     boolean isHome = isForceHome;
/* 141 */     if ((!isForceHome) && (!isForceLocal))
/*     */     {
/* 143 */       String useType = (String)map.get("useType");
/* 144 */       if ((useType == null) || (useType.length() == 0))
/*     */       {
/* 146 */         isHome = !isLocal(map);
/*     */       }
/* 148 */       else if (useType.equals("home"))
/*     */       {
/* 150 */         isHome = true;
/*     */       }
/*     */       else
/*     */       {
/* 154 */         isHome = (location == null) || (location.length() == 0);
/*     */       }
/*     */     }
/*     */ 
/* 158 */     String dir = null;
/* 159 */     if (!isHome)
/*     */     {
/* 161 */       String status = (String)map.get("status");
/* 162 */       if ((status != null) && (status.equalsIgnoreCase("enabled")) && (type == 0))
/*     */       {
/* 166 */         dir = ComponentLoader.getComponentDir(name);
/* 167 */         if (dir != null)
/*     */         {
/* 169 */           absPath = FileUtils.getAbsolutePath(dir, filename);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 174 */     if (absPath == null)
/*     */     {
/* 176 */       if (isHome)
/*     */       {
/* 178 */         dir = computeDefaultComponentDirWithEnv(map, 2, hasTags, new boolean[1], env);
/* 179 */         absPath = dir + name + "/" + filename;
/*     */       }
/*     */       else
/*     */       {
/* 183 */         dir = computeDefaultComponentDirWithEnv(map, 1, hasTags, new boolean[1], env);
/* 184 */         if (startWithStandardDirectories(location))
/*     */         {
/* 186 */           String s = getCustomDirectoryStub();
/* 187 */           if (startsWithSystemStub(location))
/*     */           {
/* 189 */             s = getSystemDirectoryStub();
/*     */           }
/* 191 */           location = location.substring(s.length());
/*     */         }
/* 193 */         absPath = FileUtils.getAbsolutePath(dir, location);
/*     */       }
/*     */     }
/* 196 */     if (!isComponentNamedHDA)
/*     */     {
/* 199 */       String componentDirpath = FileUtils.getParent(absPath);
/* 200 */       String componentHDAPath = componentDirpath + '/' + name + ".hda";
/* 201 */       if (FileUtils.checkFile(componentHDAPath, 1) == 0)
/*     */       {
/* 203 */         absPath = componentHDAPath;
/*     */       }
/*     */     }
/* 206 */     if (hasTags)
/*     */     {
/* 208 */       String[] keys = { "SystemComponentDir", "ComponentDir", "IdcHomeDir", "IntradocDir" };
/* 209 */       for (int i = 0; i < keys.length; ++i)
/*     */       {
/* 211 */         String key = keys[i];
/* 212 */         String val = getEnvironmentValue(key, env);
/* 213 */         if ((val == null) || (val.length() <= 0))
/*     */           continue;
/* 215 */         val = FileUtils.directorySlashes(val);
/* 216 */         if (!absPath.startsWith(val))
/*     */           continue;
/* 218 */         absPath = "$" + key + absPath.substring(val.length() - 1);
/* 219 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 224 */     return absPath;
/*     */   }
/*     */ 
/*     */   public static String computeDefaultComponentDirWithEnv(Map<String, String> map, int dirType, boolean hasSubsTags, boolean[] isCustom, Map<String, String> env)
/*     */   {
/* 230 */     String dir = null;
/* 231 */     boolean isSystem = isSystemComponent(map);
/* 232 */     if (isSystem)
/*     */     {
/* 234 */       dir = computeDefaultSystemComponentDirWithEnv(dirType, hasSubsTags, isCustom, env);
/*     */     }
/*     */     else
/*     */     {
/* 238 */       dir = computeDefaultCustomComponentDirWithEnv(dirType, hasSubsTags, isCustom, env);
/*     */     }
/* 240 */     return dir;
/*     */   }
/*     */ 
/*     */   public static String computeDefaultCustomComponentDirWithEnv(int dirType, boolean hasSubsTags, boolean[] isCustom, Map<String, String> env)
/*     */   {
/* 246 */     String dir = null;
/* 247 */     boolean isHome = dirType == 2;
/* 248 */     if (!isHome)
/*     */     {
/* 250 */       dir = getEnvironmentValue("ComponentDir", env);
/* 251 */       if (dir != null)
/*     */       {
/* 253 */         if (hasSubsTags)
/*     */         {
/* 255 */           dir = "$ComponentDir/";
/*     */         }
/* 257 */         isCustom[0] = true;
/*     */       }
/*     */     }
/*     */ 
/* 261 */     if ((dir == null) || (dir.length() == 0) || (isHome))
/*     */     {
/* 263 */       if (isHome)
/*     */       {
/* 265 */         dir = getEnvironmentValue("IdcHomeDir", env);
/* 266 */         if ((hasSubsTags) && (dir != null))
/*     */         {
/* 268 */           dir = "$IdcHomeDir/";
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 273 */         dir = getEnvironmentValue("IntradocDir", env);
/* 274 */         if ((hasSubsTags) && (dir != null))
/*     */         {
/* 276 */           dir = "$IntradocDir/";
/*     */         }
/*     */       }
/* 279 */       dir = FileUtils.directorySlashes(dir);
/* 280 */       dir = dir + getCustomDirectoryStub();
/*     */     }
/* 282 */     return FileUtils.directorySlashes(dir);
/*     */   }
/*     */ 
/*     */   public static String computeDefaultSystemComponentDirWithEnv(int dirType, boolean hasSubsTags, boolean[] isCustom, Map<String, String> env)
/*     */   {
/* 288 */     String dir = null;
/* 289 */     boolean isHome = dirType == 2;
/* 290 */     if (!isHome)
/*     */     {
/* 292 */       dir = getEnvironmentValue("SystemComponentDir", env);
/* 293 */       if (dir != null)
/*     */       {
/* 295 */         if (hasSubsTags)
/*     */         {
/* 297 */           dir = "$SystemComponentDir/";
/*     */         }
/* 299 */         isCustom[0] = true;
/*     */       }
/*     */     }
/*     */ 
/* 303 */     if ((dir == null) || (dir.length() == 0) || (isHome))
/*     */     {
/* 305 */       if (isHome)
/*     */       {
/* 307 */         dir = getEnvironmentValue("IdcHomeDir", env);
/* 308 */         if ((hasSubsTags) && (dir != null))
/*     */         {
/* 310 */           dir = "$IdcHomeDir/";
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 315 */         dir = getEnvironmentValue("IntradocDir", env);
/* 316 */         if ((hasSubsTags) && (dir != null))
/*     */         {
/* 318 */           dir = "$IntradocDir/";
/*     */         }
/*     */       }
/* 321 */       dir = FileUtils.directorySlashes(dir);
/* 322 */       dir = dir + getSystemDirectoryStub();
/*     */     }
/* 324 */     return FileUtils.directorySlashes(dir);
/*     */   }
/*     */ 
/*     */   public static String computeDefaultComponentDir(Map<String, String> map, int dirType, boolean hasSubsTags, boolean[] isCustom)
/*     */   {
/* 330 */     return computeDefaultComponentDirWithEnv(map, dirType, hasSubsTags, isCustom, null);
/*     */   }
/*     */ 
/*     */   public static String computeDefaultCustomComponentDir(int dirType, boolean hasSubsTags, boolean[] isCustom)
/*     */   {
/* 336 */     return computeDefaultCustomComponentDirWithEnv(dirType, hasSubsTags, isCustom, null);
/*     */   }
/*     */ 
/*     */   public static String computeDefaultSystemComponentDir(int dirType, boolean hasSubsTags, boolean[] isCustom)
/*     */   {
/* 342 */     return computeDefaultSystemComponentDirWithEnv(dirType, hasSubsTags, isCustom, null);
/*     */   }
/*     */ 
/*     */   public static boolean isSystemComponent(Map<String, String> map)
/*     */   {
/* 347 */     boolean isSystem = false;
/* 348 */     if (map != null)
/*     */     {
/* 350 */       String tagStr = (String)map.get("componentTags");
/* 351 */       isSystem = hasSystemTag(tagStr);
/*     */     }
/* 353 */     return isSystem;
/*     */   }
/*     */ 
/*     */   public static boolean hasSystemTag(String tagStr)
/*     */   {
/* 358 */     boolean isSystem = false;
/* 359 */     List sTags = getSystemTags();
/* 360 */     List tags = StringUtils.makeListFromSequenceSimple(tagStr);
/* 361 */     for (String tag : sTags)
/*     */     {
/* 363 */       isSystem = tags.contains(tag);
/* 364 */       if (isSystem) {
/*     */         break;
/*     */       }
/*     */     }
/*     */ 
/* 369 */     return isSystem;
/*     */   }
/*     */ 
/*     */   public static List getSystemTags()
/*     */   {
/* 374 */     String sysTagStr = SharedObjects.getEnvironmentValue("SystemTags");
/* 375 */     if (sysTagStr == null)
/*     */     {
/* 377 */       sysTagStr = "idc,system";
/*     */     }
/*     */ 
/* 380 */     List sTags = StringUtils.makeListFromSequenceSimple(sysTagStr);
/* 381 */     return sTags;
/*     */   }
/*     */ 
/*     */   public static boolean isLocal(Map<String, String> map)
/*     */   {
/* 386 */     String tStr = (String)map.get("componentType");
/* 387 */     List types = StringUtils.makeListFromSequenceSimple(tStr);
/* 388 */     return (types.size() == 0) || (types.contains("local"));
/*     */   }
/*     */ 
/*     */   public static boolean isLocalOnly(Map<String, String> map)
/*     */   {
/* 393 */     if (map != null)
/*     */     {
/* 395 */       String tStr = (String)map.get("componentType");
/* 396 */       List types = StringUtils.makeListFromSequenceSimple(tStr);
/* 397 */       if ((types.size() == 0) || ((types.size() == 1) && (types.contains("local"))))
/*     */       {
/* 399 */         return true;
/*     */       }
/*     */     }
/* 402 */     return false;
/*     */   }
/*     */ 
/*     */   public static boolean isInNonLocalLocation(String absPath, Map<String, String> map, String[] loc)
/*     */   {
/* 407 */     if (isHomeLocal(null))
/*     */     {
/* 410 */       return false;
/*     */     }
/* 412 */     boolean isNonLocal = false;
/* 413 */     String hPath = determineComponentLocation(map, 2);
/*     */ 
/* 416 */     String rAbsPath = absPath;
/* 417 */     String rHPath = hPath;
/* 418 */     if (EnvUtils.isFamily("windows"))
/*     */     {
/* 420 */       rAbsPath = absPath.toLowerCase();
/* 421 */       rHPath = hPath.toLowerCase();
/*     */     }
/* 423 */     if (rAbsPath.equals(rHPath))
/*     */     {
/* 425 */       String homeDir = SharedObjects.getEnvironmentValue("IdcHomeDir");
/* 426 */       String rHomeDir = homeDir.toLowerCase();
/* 427 */       int index = rAbsPath.indexOf(rHomeDir);
/* 428 */       index = rHomeDir.length();
/* 429 */       loc[0] = absPath.substring(index);
/* 430 */       isNonLocal = true;
/*     */     }
/* 432 */     return isNonLocal;
/*     */   }
/*     */ 
/*     */   public static boolean isHomeLocal(Map<String, String> env)
/*     */   {
/* 439 */     String homeDir = getEnvironmentValue("IdcHomeDir", env);
/* 440 */     String idcDir = getEnvironmentValue("IntradocDir", env);
/* 441 */     if (EnvUtils.isFamily("windows"))
/*     */     {
/* 443 */       if (homeDir != null)
/*     */       {
/* 445 */         homeDir = homeDir.toLowerCase();
/*     */       }
/* 447 */       idcDir = idcDir.toLowerCase();
/*     */     }
/* 449 */     return (homeDir == null) || (homeDir.equals(idcDir));
/*     */   }
/*     */ 
/*     */   public static List<String> getComponentParentDirectories()
/*     */   {
/* 454 */     List l = new ArrayList();
/*     */ 
/* 456 */     String sysStub = getSystemDirectoryStub();
/* 457 */     String cstmStub = getCustomDirectoryStub();
/* 458 */     String systemDir = SharedObjects.getEnvironmentValue("SystemComponentDir");
/* 459 */     if (systemDir == null)
/*     */     {
/* 461 */       systemDir = LegacyDirectoryLocator.getIntradocDir() + sysStub;
/*     */     }
/*     */     else
/*     */     {
/* 465 */       systemDir = FileUtils.directorySlashes(systemDir);
/*     */     }
/* 467 */     l.add(systemDir);
/*     */ 
/* 469 */     String customDir = SharedObjects.getEnvironmentValue("ComponentDir");
/* 470 */     if (customDir == null)
/*     */     {
/* 472 */       customDir = LegacyDirectoryLocator.getIntradocDir() + cstmStub;
/*     */     }
/*     */     else
/*     */     {
/* 476 */       customDir = FileUtils.directorySlashes(customDir);
/*     */     }
/* 478 */     l.add(customDir);
/*     */ 
/* 480 */     if (!isHomeLocal(null))
/*     */     {
/* 482 */       String homeDir = SharedObjects.getEnvironmentValue("IdcHomeDir");
/* 483 */       if (homeDir != null)
/*     */       {
/* 485 */         homeDir = FileUtils.directorySlashes(homeDir);
/*     */       }
/* 487 */       l.add(homeDir + sysStub);
/* 488 */       l.add(homeDir + cstmStub);
/*     */     }
/*     */ 
/* 491 */     return l;
/*     */   }
/*     */ 
/*     */   public static String getSystemDirectoryStub()
/*     */   {
/* 496 */     return "components/";
/*     */   }
/*     */ 
/*     */   public static String getCustomDirectoryStub()
/*     */   {
/* 501 */     return "custom/";
/*     */   }
/*     */ 
/*     */   public static boolean startsWithSystemStub(String location)
/*     */   {
/* 506 */     return location.startsWith(getSystemDirectoryStub());
/*     */   }
/*     */ 
/*     */   public static boolean startsWithCustomStub(String location)
/*     */   {
/* 511 */     return location.startsWith(getCustomDirectoryStub());
/*     */   }
/*     */ 
/*     */   public static boolean startWithStandardDirectories(String location)
/*     */   {
/* 516 */     return (startsWithSystemStub(location)) || (startsWithCustomStub(location));
/*     */   }
/*     */ 
/*     */   public static String adjustToRelative(String absPath, String location, Map<String, String> env)
/*     */   {
/* 521 */     String intradocDir = getEnvironmentValue("IntradocDir", env);
/* 522 */     if (absPath.startsWith(intradocDir))
/*     */     {
/* 524 */       location = absPath.substring(intradocDir.length(), absPath.length());
/*     */     }
/*     */     else
/*     */     {
/* 528 */       String customDir = getEnvironmentValue("ComponentDir", env);
/* 529 */       String systemDir = getEnvironmentValue("SystemComponentDir", env);
/*     */ 
/* 531 */       String cmpAbsPath = absPath;
/* 532 */       if (EnvUtils.isFamily("windows"))
/*     */       {
/* 534 */         cmpAbsPath = cmpAbsPath.toLowerCase();
/* 535 */         if (customDir != null)
/*     */         {
/* 537 */           customDir = customDir.toLowerCase();
/*     */         }
/* 539 */         if (systemDir != null)
/*     */         {
/* 541 */           systemDir = systemDir.toLowerCase();
/*     */         }
/*     */       }
/*     */ 
/* 545 */       if ((customDir != null) && (cmpAbsPath.startsWith(customDir)))
/*     */       {
/* 547 */         location = getCustomDirectoryStub() + absPath.substring(customDir.length(), absPath.length());
/*     */       }
/* 550 */       else if ((systemDir != null) && (absPath.startsWith(systemDir)))
/*     */       {
/* 552 */         location = getSystemDirectoryStub() + absPath.substring(systemDir.length(), absPath.length());
/*     */       }
/*     */     }
/*     */ 
/* 556 */     return location;
/*     */   }
/*     */ 
/*     */   public static String getEnvironmentValue(String key, Map<String, String> map)
/*     */   {
/* 561 */     String val = null;
/* 562 */     if (map != null)
/*     */     {
/* 564 */       val = (String)map.get(key);
/*     */     }
/* 566 */     if (val == null)
/*     */     {
/* 568 */       val = SharedObjects.getEnvironmentValue(key);
/*     */     }
/* 570 */     return val;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 575 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ComponentLocationUtils
 * JD-Core Version:    0.5.4
 */