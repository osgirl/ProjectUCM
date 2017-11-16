/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.CryptoCommonUtils;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceManager;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.zip.ZipFunctions;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Properties;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ import java.util.zip.ZipEntry;
/*     */ import java.util.zip.ZipOutputStream;
/*     */ 
/*     */ public class EnvironmentPackager
/*     */ {
/*  38 */   protected Properties m_props = new Properties();
/*  39 */   protected String m_tempDir = null;
/*  40 */   protected StringBuffer m_buildErrors = new StringBuffer(4096);
/*  41 */   protected ZipOutputStream m_zos = null;
/*  42 */   protected HashMap m_filesAdded = new HashMap();
/*     */ 
/*     */   public void packageEnvironment(DataBinder binder)
/*     */   {
/*  59 */     this.m_props = ((Properties)binder.getLocalData().clone());
/*  60 */     DataBinder.mergeHashTables(this.m_props, binder.m_environment);
/*     */ 
/*  62 */     this.m_tempDir = DataBinder.m_tempDir;
/*     */ 
/*  64 */     Thread t = new Thread("EnvironmentPackager")
/*     */     {
/*     */       public void run()
/*     */       {
/*  69 */         EnvironmentPackager.this.doPackageEnvironment();
/*     */       }
/*     */     };
/*  72 */     t.setName("EnvironmentPackager");
/*  73 */     t.setPriority(1);
/*  74 */     t.start();
/*     */   }
/*     */ 
/*     */   public void doPackageEnvironment()
/*     */   {
/*  88 */     String tempFile = this.m_props.getProperty("envPkgFileName");
/*  89 */     String tempPath = this.m_tempDir + tempFile;
/*  90 */     String destPath = this.m_props.getProperty("envPkgDestPath");
/*  91 */     String envPkgUrl = this.m_props.getProperty("envPkgUrl");
/*  92 */     DataResultSet pkgPaths = SharedObjects.getTable("EnvironmentPackagerPaths");
/*     */     try
/*     */     {
/*  98 */       this.m_zos = new ZipOutputStream(new FileOutputStream(tempPath));
/*  99 */       int nameIndex = ResultSetUtils.getIndexMustExist(pkgPaths, "name");
/* 100 */       int pathIndex = ResultSetUtils.getIndexMustExist(pkgPaths, "filePath");
/* 101 */       int rootIndex = ResultSetUtils.getIndexMustExist(pkgPaths, "root");
/* 102 */       int digestIndex = ResultSetUtils.getIndexMustExist(pkgPaths, "allowDigest");
/* 103 */       int requiredIndex = ResultSetUtils.getIndexMustExist(pkgPaths, "isRequired");
/*     */ 
/* 105 */       pkgPaths.first();
/* 106 */       while (pkgPaths.isRowPresent())
/*     */       {
/* 108 */         String name = pkgPaths.getStringValue(nameIndex);
/* 109 */         String allFilePaths = pkgPaths.getStringValue(pathIndex);
/* 110 */         String root = pkgPaths.getStringValue(rootIndex);
/* 111 */         boolean allowDigest = StringUtils.convertToBool(pkgPaths.getStringValue(digestIndex), false);
/* 112 */         boolean isRequired = StringUtils.convertToBool(pkgPaths.getStringValue(requiredIndex), false);
/*     */ 
/* 114 */         boolean wantsPackaging = StringUtils.convertToBool(this.m_props.getProperty("package_" + name), false);
/* 115 */         boolean wantsDigest = StringUtils.convertToBool(this.m_props.getProperty("digest_" + name), false);
/*     */ 
/* 117 */         if (allFilePaths.indexOf(36) == 0)
/*     */         {
/* 119 */           if (allFilePaths.equals("$configPage"))
/*     */           {
/* 121 */             addServiceResultToPackage("CONFIG_INFO", "config_info.hda");
/*     */           }
/* 123 */           else if (allFilePaths.equals("$systemAuditInfoPage"))
/*     */           {
/* 125 */             addServiceResultToPackage("GET_SYSTEM_AUDIT_INFO", "system_audit_info.hda");
/*     */           }
/*     */           else
/*     */           {
/* 129 */             String msg = LocaleUtils.encodeMessage("syZipUnableToAddNotFile", null, allFilePaths);
/* 130 */             this.m_buildErrors.append(LocaleResources.localizeMessage(msg, null)).append('\n');
/*     */           }
/*     */ 
/*     */         }
/* 136 */         else if ((wantsDigest) || ((isRequired) && (allowDigest)))
/*     */         {
/* 138 */           addFilesToPackage(allFilePaths, root, name, true);
/*     */         }
/* 142 */         else if ((wantsPackaging) || (isRequired))
/*     */         {
/* 144 */           addFilesToPackage(allFilePaths, root, name, false);
/*     */         }
/*     */ 
/* 148 */         pkgPaths.next();
/*     */       }
/*     */ 
/* 152 */       addSystemEnvironmentFile();
/*     */ 
/* 155 */       if (EnvUtils.isFamily("windows"))
/*     */       {
/* 157 */         addWinServicesDump();
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 162 */       Report.error(null, LocaleUtils.encodeMessage("csUnableToPackageEnv", e.getMessage()), e);
/*     */     }
/*     */     finally
/*     */     {
/* 166 */       FileUtils.closeObject(this.m_zos);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 172 */       FileUtils.copyFile(tempPath, destPath);
/* 173 */       FileUtils.deleteFile(tempPath);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 177 */       Report.trace(null, null, e);
/*     */     }
/*     */ 
/* 180 */     Report.info(null, null, "csEnvPkgFileLocationDone", new Object[] { envPkgUrl });
/*     */   }
/*     */ 
/*     */   protected void addWinServicesDump()
/*     */     throws IOException
/*     */   {
/* 189 */     String osName = EnvUtils.getOSName();
/* 190 */     String location = "";
/* 191 */     location = location + "/os/" + osName + "/bin/NtProcCtrl\" query";
/* 192 */     String command = "\"" + DirectoryLocator.getSharedDirectory() + location;
/*     */ 
/* 195 */     StringBuffer output = new StringBuffer();
/* 196 */     byte[] buf = new byte[1024];
/* 197 */     int nread = 0;
/*     */     try
/*     */     {
/* 200 */       Runtime runner = Runtime.getRuntime();
/* 201 */       Process process = runner.exec(command);
/* 202 */       InputStream procIn = process.getInputStream();
/* 203 */       while ((nread = procIn.read(buf)) > 0)
/*     */       {
/* 205 */         output.append(new String(buf, 0, nread));
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 210 */       this.m_buildErrors.append(LocaleResources.localizeMessage("csEnvPkgUnableToQueryServices", null)).append('\n');
/*     */     }
/*     */ 
/* 215 */     String processList = output.toString();
/* 216 */     ZipEntry entry = new ZipEntry("services.txt");
/* 217 */     registerZipEntry("services.txt", "winServiceDump");
/* 218 */     this.m_zos.putNextEntry(entry);
/* 219 */     this.m_zos.write(processList.getBytes(LocaleResources.getSystemLocale().m_pageEncoding));
/* 220 */     this.m_zos.closeEntry();
/*     */   }
/*     */ 
/*     */   protected void addSystemEnvironmentFile()
/*     */     throws IOException
/*     */   {
/* 233 */     StringBuffer envBuffer = new StringBuffer(10240);
/*     */     try
/*     */     {
/* 237 */       envBuffer.append("General environment:");
/* 238 */       envBuffer.append("\nOS Name:\t").append(System.getProperty("os.name"));
/* 239 */       envBuffer.append("\nOS Version:\t").append(System.getProperty("os.version"));
/* 240 */       envBuffer.append("\nWeb Server:\t").append(this.m_props.getProperty("SERVER_SOFTWARE"));
/* 241 */       envBuffer.append("\nJava Version:\t").append(System.getProperty("java.version"));
/* 242 */       envBuffer.append("\nClasspath:\t").append(System.getProperty("java.class.path"));
/*     */ 
/* 244 */       envBuffer.append("\n\n\nJava Properties:\n");
/* 245 */       Properties envProps = System.getProperties();
/* 246 */       Enumeration e = envProps.keys();
/*     */ 
/* 248 */       while (e.hasMoreElements())
/*     */       {
/* 250 */         String key = (String)e.nextElement();
/* 251 */         String value = envProps.getProperty(key);
/* 252 */         envBuffer.append(key).append(" = ").append(value).append('\n');
/*     */       }
/*     */ 
/* 255 */       envBuffer.append("\n\n\nContent Server Environment:\n");
/* 256 */       e = this.m_props.keys();
/* 257 */       while (e.hasMoreElements())
/*     */       {
/* 259 */         String key = (String)e.nextElement();
/* 260 */         String value = this.m_props.getProperty(key);
/* 261 */         envBuffer.append(key).append(" = ").append(value).append('\n');
/*     */       }
/*     */ 
/* 264 */       envBuffer.append("\n\nSystem Environment:\n");
/* 265 */       String dumpCommand = "sh -c env";
/* 266 */       if (EnvUtils.isFamily("windows"))
/*     */       {
/* 268 */         dumpCommand = "cmd /c set";
/*     */       }
/*     */ 
/* 271 */       Runtime runner = Runtime.getRuntime();
/* 272 */       byte[] buf = new byte[4096];
/* 273 */       int nread = 0;
/* 274 */       Process process = runner.exec(dumpCommand);
/* 275 */       InputStream procIn = process.getInputStream();
/* 276 */       while ((nread = procIn.read(buf)) > 0)
/*     */       {
/* 278 */         envBuffer.append(new String(buf, 0, nread));
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 283 */       this.m_buildErrors.append(LocaleResources.localizeMessage(e.getMessage(), null)).append('\n');
/*     */     }
/*     */ 
/* 287 */     envBuffer.append("\n\nBuild Errors:\n").append(this.m_buildErrors.toString());
/* 288 */     String envData = envBuffer.toString();
/*     */ 
/* 291 */     ZipEntry entry = new ZipEntry("environment.txt");
/* 292 */     registerZipEntry("environment.txt", "system environment");
/* 293 */     this.m_zos.putNextEntry(entry);
/* 294 */     this.m_zos.write(envData.getBytes(LocaleResources.getSystemLocale().m_pageEncoding));
/* 295 */     this.m_zos.closeEntry();
/*     */   }
/*     */ 
/*     */   protected void addFilesToPackage(String allFilePaths, String root, String name, boolean doDigest)
/*     */     throws DataException, IOException
/*     */   {
/* 309 */     String rootPath = LegacyDirectoryLocator.getIntradocDir();
/* 310 */     String logDir = LegacyDirectoryLocator.getLogDirectory();
/* 311 */     String sharedDir = LegacyDirectoryLocator.getSharedDirectory();
/* 312 */     String weblayoutDir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/*     */ 
/* 315 */     if ((root != null) && (root.length() > 0))
/*     */     {
/* 317 */       if (root.equals("weblayout"))
/*     */       {
/* 319 */         rootPath = weblayoutDir;
/*     */       }
/* 321 */       else if (root.equals("log"))
/*     */       {
/* 323 */         rootPath = logDir;
/*     */       }
/* 325 */       else if (root.equals("shared"))
/*     */       {
/* 327 */         rootPath = sharedDir;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 332 */     StringTokenizer st = new StringTokenizer(allFilePaths, ";");
/* 333 */     StringBuffer digestBuffer = new StringBuffer();
/* 334 */     while (st.hasMoreTokens())
/*     */     {
/* 336 */       boolean getAll = false;
/* 337 */       boolean recurse = false;
/* 338 */       String filePath = st.nextToken();
/* 339 */       String absPath = filePath;
/*     */ 
/* 342 */       if (filePath.endsWith("**"))
/*     */       {
/* 344 */         getAll = true;
/* 345 */         recurse = true;
/* 346 */         filePath = filePath.substring(0, filePath.length() - 2);
/*     */       }
/* 348 */       else if (filePath.endsWith("*"))
/*     */       {
/* 350 */         getAll = true;
/* 351 */         filePath = filePath.substring(0, filePath.length() - 1);
/*     */       }
/*     */ 
/* 355 */       if (filePath.length() == 0)
/*     */       {
/* 357 */         absPath = rootPath;
/*     */       }
/*     */       else
/*     */       {
/* 361 */         absPath = FileUtils.getAbsolutePath(rootPath, filePath);
/*     */       }
/*     */ 
/* 364 */       if (doDigest)
/*     */       {
/* 366 */         String digest = createDigestFile(filePath, absPath, getAll, recurse);
/* 367 */         digestBuffer.append(digest);
/*     */       }
/*     */       else
/*     */       {
/* 372 */         boolean checkSensitiveFile = false;
/* 373 */         if ((name.equalsIgnoreCase("config")) || (name.equalsIgnoreCase("install")))
/*     */         {
/* 375 */           checkSensitiveFile = true;
/*     */         }
/* 377 */         doAddFilesToPackage(absPath, getAll, recurse, checkSensitiveFile);
/*     */       }
/*     */     }
/*     */ 
/* 381 */     if (!doDigest) {
/*     */       return;
/*     */     }
/* 384 */     String key = "digest_" + name + ".txt";
/* 385 */     ZipEntry entry = new ZipEntry(key);
/* 386 */     registerZipEntry(key, allFilePaths);
/* 387 */     this.m_zos.putNextEntry(entry);
/* 388 */     this.m_zos.write(digestBuffer.toString().getBytes(LocaleResources.getSystemLocale().m_pageEncoding));
/* 389 */     this.m_zos.closeEntry();
/*     */   }
/*     */ 
/*     */   protected String createDigestFile(String relPath, String absPath, boolean getAll, boolean recurse)
/*     */     throws DataException
/*     */   {
/* 401 */     DataResultSet files = new DataResultSet(new String[] { "relPath", "digest" });
/*     */ 
/* 404 */     doAddFilesToDigest(relPath, absPath, files, getAll, recurse);
/* 405 */     ResultSetUtils.sortResultSet(files, new String[] { "relPath" });
/*     */ 
/* 407 */     StringBuffer digestBuffer = new StringBuffer(4096);
/* 408 */     files.first();
/* 409 */     while (files.isRowPresent())
/*     */     {
/* 411 */       digestBuffer.append(files.getStringValue(0)).append('\t');
/* 412 */       digestBuffer.append(files.getStringValue(1)).append('\n');
/* 413 */       files.next();
/*     */     }
/*     */ 
/* 416 */     return digestBuffer.toString();
/*     */   }
/*     */ 
/*     */   protected void doAddFilesToDigest(String relPath, String absPath, DataResultSet files, boolean getAll, boolean recurse)
/*     */   {
/* 426 */     File f = new File(absPath);
/* 427 */     String[] children = f.list();
/* 428 */     if (children == null)
/*     */     {
/*     */       try
/*     */       {
/* 433 */         String digest = CryptoCommonUtils.sha1UuencodeFile(absPath);
/*     */ 
/* 436 */         Vector row = files.createEmptyRow();
/* 437 */         row.setElementAt(relPath, 0);
/* 438 */         row.setElementAt(digest, 1);
/* 439 */         files.addRow(row);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 444 */         this.m_buildErrors.append(LocaleResources.localizeMessage(e.getMessage(), null)).append('\n');
/*     */       }
/*     */     }
/* 447 */     else if (recurse)
/*     */     {
/* 450 */       for (int i = 0; i < children.length; ++i)
/*     */       {
/* 452 */         doAddFilesToDigest(addSlash(relPath) + children[i], addSlash(absPath) + children[i], files, true, true);
/*     */       }
/*     */     }
/*     */     else {
/* 456 */       if (!getAll) {
/*     */         return;
/*     */       }
/* 459 */       for (int i = 0; i < children.length; ++i)
/*     */       {
/* 461 */         doAddFilesToDigest(addSlash(relPath) + children[i], addSlash(absPath) + children[i], files, false, false);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void doAddFilesToPackage(String path, boolean getAll, boolean recurse, boolean checkSensitiveFile)
/*     */   {
/* 476 */     File f = new File(path);
/* 477 */     String[] children = f.list();
/* 478 */     if (children == null)
/*     */     {
/*     */       try
/*     */       {
/* 482 */         String name = makeEntryNameFromPath(path);
/*     */ 
/* 486 */         if (((checkSensitiveFile) && (((name.toLowerCase().endsWith("config.cfg")) || (name.toLowerCase().endsWith("-new.txt"))))) || (name.toLowerCase().endsWith("-update.txt")))
/*     */         {
/* 489 */           addFileAndRemovePassword(name, path);
/*     */         }
/*     */         else
/*     */         {
/* 493 */           registerZipEntry(name, path);
/* 494 */           ZipFunctions.addFile(name, path, this.m_zos);
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 500 */         this.m_buildErrors.append(LocaleResources.localizeMessage(e.getMessage(), null)).append('\n');
/*     */       }
/*     */     }
/* 503 */     else if (recurse)
/*     */     {
/* 506 */       for (int i = 0; i < children.length; ++i)
/*     */       {
/* 508 */         doAddFilesToPackage(addSlash(path) + children[i], true, true, checkSensitiveFile);
/*     */       }
/*     */     } else {
/* 511 */       if (!getAll) {
/*     */         return;
/*     */       }
/* 514 */       for (int i = 0; i < children.length; ++i)
/*     */       {
/* 516 */         doAddFilesToPackage(addSlash(path) + children[i], false, false, checkSensitiveFile);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addFileAndRemovePassword(String name, String path)
/*     */     throws IOException, ServiceException
/*     */   {
/* 524 */     File f = FileUtilsCfgBuilder.getCfgFile(path, null, false);
/* 525 */     InputStream is = FileUtilsCfgBuilder.getCfgInputStream(f);
/* 526 */     String[] encoding = new String[1];
/* 527 */     String content = FileUtils.loadFile(is, "cfg", encoding);
/*     */ 
/* 529 */     int[] indexes = null;
/* 530 */     int start = 0;
/* 531 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 532 */     while ((indexes = nextPasswordIndex(content, start)) != null)
/*     */     {
/* 534 */       builder.append(content, start, indexes[0] - start);
/* 535 */       builder.append("xxxxxx");
/* 536 */       start = indexes[1];
/*     */     }
/* 538 */     if (start < content.length())
/*     */     {
/* 540 */       builder.append(content.substring(start));
/*     */     }
/*     */ 
/* 543 */     byte[] bytes = builder.toString().getBytes(encoding[0]);
/* 544 */     is = new ByteArrayInputStream(bytes);
/* 545 */     ZipFunctions.addStream(name, is, this.m_zos);
/*     */   }
/*     */ 
/*     */   protected int[] nextPasswordIndex(String content, int beginIndex)
/*     */   {
/* 550 */     int nextStart = content.indexOf("Password=", beginIndex);
/* 551 */     int[] indexes = null;
/* 552 */     if (nextStart >= 0)
/*     */     {
/* 554 */       indexes = new int[] { nextStart + 9, content.length() - 1 };
/* 555 */       for (int i = indexes[0]; i <= indexes[1]; ++i)
/*     */       {
/* 557 */         char c = content.charAt(i);
/* 558 */         if ((c != '\r') && (c != '\n'))
/*     */           continue;
/* 560 */         indexes[1] = i;
/* 561 */         break;
/*     */       }
/*     */     }
/*     */ 
/* 565 */     return indexes;
/*     */   }
/*     */ 
/*     */   protected static String addSlash(String path)
/*     */   {
/* 573 */     int index = path.length() - 1;
/* 574 */     if ((index < 0) || (path.charAt(index) == '/'))
/*     */     {
/* 576 */       return path;
/*     */     }
/* 578 */     return path + "/";
/*     */   }
/*     */ 
/*     */   protected void addServiceResultToPackage(String serviceName, String filePath)
/*     */     throws IOException, DataException, ServiceException
/*     */   {
/* 584 */     DataBinder binder = new DataBinder();
/* 585 */     binder.setEnvironment(new Properties(this.m_props));
/* 586 */     binder.putLocal("IsJava", "1");
/*     */ 
/* 588 */     Service service = ServiceManager.getInitializedService(serviceName, binder, null);
/* 589 */     service.doUnsecuredRequestInternal();
/*     */ 
/* 591 */     ByteArrayOutputStream os = new ByteArrayOutputStream();
/* 592 */     OutputStreamWriter osw = new OutputStreamWriter(os);
/* 593 */     DataSerializeUtils.send(binder, osw, null);
/* 594 */     osw.close();
/* 595 */     os.close();
/*     */ 
/* 597 */     byte[] bytes = os.toByteArray();
/* 598 */     String name = makeEntryNameFromPath(filePath);
/* 599 */     name = StringUtils.encodeHttpHeaderStyle(name, false);
/* 600 */     ZipEntry entry = new ZipEntry(name);
/* 601 */     registerZipEntry(name, "ServiceResult " + serviceName + " " + filePath);
/* 602 */     this.m_zos.putNextEntry(entry);
/* 603 */     this.m_zos.write(bytes);
/*     */ 
/* 605 */     service.clear();
/*     */   }
/*     */ 
/*     */   protected void registerZipEntry(String name, String value)
/*     */   {
/* 610 */     if (this.m_filesAdded.get(name) != null)
/*     */     {
/* 612 */       Report.trace("system", "Added duplicate entry, name=" + name + ", value=" + value, null);
/*     */     }
/* 614 */     this.m_filesAdded.put(name, value);
/*     */   }
/*     */ 
/*     */   protected String makeEntryNameFromPath(String path)
/*     */   {
/* 619 */     int foundSlashIndex = -1;
/* 620 */     boolean foundColon = false;
/* 621 */     int startIndex = 0;
/* 622 */     for (int i = 0; (i < 5) && (i < path.length()); ++i)
/*     */     {
/* 624 */       char ch = path.charAt(i);
/* 625 */       if (foundSlashIndex < 0)
/*     */       {
/* 627 */         if ((ch == '/') || (ch == '\\'))
/*     */         {
/* 629 */           foundSlashIndex = i;
/*     */         } else {
/* 631 */           if (ch != ':')
/*     */             continue;
/* 633 */           foundColon = true;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 638 */         if ((ch == '/') || (ch == '\\'))
/*     */           continue;
/* 640 */         if ((!foundColon) && (foundSlashIndex != 0))
/*     */           break;
/* 642 */         startIndex = i; break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 648 */     return path.substring(startIndex);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 653 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.EnvironmentPackager
 * JD-Core Version:    0.5.4
 */