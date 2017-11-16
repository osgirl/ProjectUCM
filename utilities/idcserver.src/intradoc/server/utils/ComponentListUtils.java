/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ComponentListUtils
/*     */ {
/*     */   public static DataResultSet createDefaultComponentsResultSet()
/*     */   {
/*  59 */     return new DataResultSet(ComponentListEditor.COMPONENT_COLUMNS);
/*     */   }
/*     */ 
/*     */   public static String computeListingFileName()
/*     */     throws DataException
/*     */   {
/*  68 */     return computeListingFileName("");
/*     */   }
/*     */ 
/*     */   protected static String getProductName() throws DataException
/*     */   {
/*  73 */     String productName = SharedObjects.getEnvironmentValue("IdcProductName");
/*  74 */     if (productName == null)
/*     */     {
/*  76 */       throw new DataException(null, "csUnableToFindValue", new Object[] { "IdcProductName" });
/*     */     }
/*  78 */     return productName;
/*     */   }
/*     */ 
/*     */   public static String computeListingFileName(String prefix)
/*     */     throws DataException
/*     */   {
/*  88 */     String productName = getProductName();
/*  89 */     return prefix + productName + "_components.hda";
/*     */   }
/*     */ 
/*     */   public static String findListingFilePath(String dir)
/*     */     throws DataException
/*     */   {
/* 100 */     return findListingFilePath(dir, "");
/*     */   }
/*     */ 
/*     */   public static String findListingFilePath(String dir, String prefix)
/*     */     throws DataException
/*     */   {
/* 112 */     dir = FileUtils.directorySlashes(dir);
/* 113 */     String listFile = dir + computeListingFileName(prefix);
/* 114 */     if (FileUtils.checkFile(listFile, 1) == 0)
/*     */     {
/* 116 */       return listFile;
/*     */     }
/* 118 */     listFile = dir + prefix + "idc_components.hda";
/* 119 */     if (FileUtils.checkFile(listFile, 1) == 0)
/*     */     {
/* 121 */       return listFile;
/*     */     }
/*     */ 
/* 124 */     return null;
/*     */   }
/*     */ 
/*     */   public static String computeListingFilePath(String dir)
/*     */     throws DataException
/*     */   {
/* 135 */     return computeListingFilePath(dir, "");
/*     */   }
/*     */ 
/*     */   public static String computeListingFilePath(String dir, String prefix)
/*     */     throws DataException
/*     */   {
/* 147 */     dir = FileUtils.directorySlashes(dir);
/* 148 */     String listFile = dir + computeListingFileName(prefix);
/* 149 */     return listFile;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void saveListingFile(String dir, String file, DataBinder data, boolean mustExist, OutputStream output, String encoding)
/*     */     throws ServiceException
/*     */   {
/* 159 */     dir = FileUtils.directorySlashes(dir);
/* 160 */     saveListingFile(dir + file, data, 4, output, encoding);
/*     */   }
/*     */ 
/*     */   public static void saveListingFile(String path, DataBinder data, int flags, OutputStream output, String encoding)
/*     */     throws ServiceException
/*     */   {
/* 171 */     Report.trace("componentloader", "ComponentListUtils.saveListingFile: path=" + path + " output=" + output + " encoding=" + encoding, null);
/*     */ 
/* 174 */     boolean isAdminServer = StringUtils.convertToBool(data.getLocal("IsAdminServerUpdate"), false);
/* 175 */     long ts = 0L;
/* 176 */     if (isAdminServer)
/*     */     {
/* 180 */       data.removeLocal("IsAdminServerUpdate");
/*     */     }
/*     */     else
/*     */     {
/* 186 */       ts = System.currentTimeMillis();
/*     */     }
/* 188 */     data.putLocal("versionTS", "" + ts);
/*     */ 
/* 190 */     if (output == null)
/*     */     {
/* 192 */       boolean isInError = false;
/* 193 */       String backupFile = path + ".old";
/* 194 */       String tmpFilename = path + ".tmp";
/*     */ 
/* 196 */       File orgFile = FileUtilsCfgBuilder.getCfgFile(path, "components", false);
/* 197 */       boolean orgExists = orgFile.exists();
/* 198 */       if (orgExists)
/*     */       {
/* 201 */         Report.trace("componentloader", "ComponentListUtils.saveListingFile: copy to back up " + path + " to " + tmpFilename, null);
/*     */ 
/* 204 */         FileUtils.copyFile(path, tmpFilename);
/*     */       }
/*     */       try
/*     */       {
/* 208 */         String dir = FileUtils.getDirectory(path);
/* 209 */         String file = FileUtils.getName(path);
/* 210 */         flags |= 1;
/* 211 */         ResourceUtils.serializeDataBinderWithEncoding(dir, file, data, flags, null);
/*     */       }
/*     */       catch (Throwable e)
/*     */       {
/*     */       }
/*     */       finally
/*     */       {
/* 220 */         if (orgExists)
/*     */         {
/* 222 */           if (isInError)
/*     */           {
/* 224 */             FileUtils.renameFile(tmpFilename, path);
/*     */           }
/*     */           else
/*     */           {
/* 228 */             FileUtils.renameFile(tmpFilename, backupFile);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 237 */       BufferedWriter writer = null;
/*     */       try
/*     */       {
/* 240 */         if (encoding == null)
/*     */         {
/* 242 */           encoding = DataSerializeUtils.determineEncoding(data, null);
/*     */         }
/* 244 */         writer = FileUtils.openDataWriterEx(output, encoding, 0);
/* 245 */         writer.write(DataSerializeUtils.createEncodingHeaderString(encoding));
/* 246 */         DataSerializeUtils.sendEx(data, writer, false, null);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 250 */         throw new ServiceException(e, "csComponentsListingSaveError", new Object[1]);
/*     */       }
/*     */       finally
/*     */       {
/* 254 */         FileUtils.closeObject(writer);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static DataBinder readListingFile(String filename, InputStream input, String[] encoding)
/*     */     throws ServiceException
/*     */   {
/* 266 */     Report.trace("componentloader", "ComponentListUtils.readListingFile: filename=" + filename + " input=" + input, null);
/*     */ 
/* 269 */     DataBinder data = null;
/* 270 */     if (input != null)
/*     */     {
/* 272 */       data = new DataBinder(true);
/* 273 */       Reader reader = null;
/* 274 */       if (encoding == null)
/*     */       {
/* 276 */         encoding = new String[1];
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 281 */         BufferedInputStream istream = new BufferedInputStream(input);
/* 282 */         encoding[0] = DataSerializeUtils.detectEncoding(data, istream, null);
/* 283 */         if (encoding[0] != null)
/*     */         {
/* 285 */           reader = new InputStreamReader(istream, encoding[0]);
/*     */         }
/*     */         else
/*     */         {
/* 289 */           reader = new InputStreamReader(istream);
/*     */         }
/* 291 */         data.m_javaEncoding = encoding[0];
/* 292 */         data.receive(new BufferedReader(reader));
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/*     */       }
/*     */       finally
/*     */       {
/* 300 */         FileUtils.closeObject(reader);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 305 */       data = ResourceLoader.loadDataBinderFromFileWithFlags(filename, 16);
/*     */     }
/* 307 */     return data;
/*     */   }
/*     */ 
/*     */   public static DataResultSet createComponentsFile(String filePath)
/*     */     throws ServiceException
/*     */   {
/* 319 */     DataResultSet rset = createDefaultComponentsResultSet();
/* 320 */     DataBinder binder = new DataBinder();
/* 321 */     binder.addResultSet("Components", rset);
/* 322 */     String dirStr = FileUtils.getDirectory(filePath);
/* 323 */     String fileStr = FileUtils.getName(filePath);
/*     */ 
/* 325 */     ResourceUtils.serializeDataBinder(dirStr, fileStr, binder, true, false);
/*     */ 
/* 327 */     return rset;
/*     */   }
/*     */ 
/*     */   public static void addPath(String compDir, Map props, List order, List pathArr, String pname, String path, char sp, boolean componentsInDev, Map<String, String> env)
/*     */     throws UnsupportedEncodingException
/*     */   {
/* 335 */     if ((path == null) || (path.length() <= 0))
/*     */       return;
/* 337 */     String compDirName = "$COMPONENT_DIR";
/* 338 */     boolean isUnique = true;
/*     */ 
/* 341 */     boolean exists = true;
/*     */ 
/* 344 */     char badSp = (EnvUtils.isFamily("windows")) ? ':' : ';';
/* 345 */     byte[] buf = StringUtils.getBytes(path, null);
/* 346 */     for (int ii = 0; ii < buf.length; ++ii)
/*     */     {
/* 348 */       byte b = buf[ii];
/*     */ 
/* 350 */       if (b != (byte)badSp)
/*     */         continue;
/* 352 */       buf[ii] = (byte)sp;
/*     */     }
/*     */ 
/* 355 */     path = new String(buf);
/* 356 */     Vector tempPathArr = StringUtils.parseArray(path, sp, '^');
/* 357 */     for (int ii = 0; ii < tempPathArr.size(); ++ii)
/*     */     {
/* 360 */       String tempPath = (String)tempPathArr.elementAt(ii);
/* 361 */       if (!tempPath.startsWith(compDirName))
/*     */         continue;
/* 363 */       tempPath = compDir + tempPath.substring(compDirName.length(), tempPath.length());
/*     */ 
/* 365 */       tempPathArr.setElementAt(tempPath, ii);
/*     */     }
/*     */ 
/* 369 */     path = StringUtils.createString(tempPathArr, sp, '^');
/* 370 */     String tempPathStr = path;
/* 371 */     if (EnvUtils.isFamily("windows"))
/*     */     {
/* 373 */       tempPathStr = tempPathStr.toLowerCase();
/*     */     }
/* 375 */     for (int i = 0; i < pathArr.size(); ++i)
/*     */     {
/* 377 */       String temp = (String)pathArr.get(i);
/* 378 */       if (EnvUtils.isFamily("windows"))
/*     */       {
/* 380 */         temp = temp.toLowerCase();
/*     */       }
/* 382 */       if (!temp.equals(tempPathStr))
/*     */         continue;
/* 384 */       isUnique = false;
/* 385 */       break;
/*     */     }
/*     */ 
/* 389 */     if (!isUnique)
/*     */       return;
/* 391 */     if ((EnvUtils.isFamily("windows")) || (componentsInDev))
/*     */     {
/* 393 */       int fileCheckResult = 0;
/*     */ 
/* 397 */       String sep = EnvUtils.getPathSeparator();
/* 398 */       if (path.indexOf(sep) < 0)
/*     */       {
/* 403 */         String pathToCheck = null;
/* 404 */         if (path.indexOf(36) < 0)
/*     */         {
/* 406 */           pathToCheck = path;
/*     */         }
/*     */ 
/* 409 */         if (path.lastIndexOf("$") == 0)
/*     */         {
/* 411 */           String idcKey = null;
/* 412 */           String[] keys = { "IdcHomeDir", "IntradocDir", "ComponentDir", "SystemComponentDir" };
/*     */ 
/* 415 */           for (int i = 0; i < keys.length; ++i)
/*     */           {
/* 417 */             String key = keys[i];
/* 418 */             if (!path.startsWith("$" + key))
/*     */               continue;
/* 420 */             idcKey = key;
/* 421 */             break;
/*     */           }
/*     */ 
/* 424 */           String idcDir = null;
/* 425 */           if (idcKey != null)
/*     */           {
/* 427 */             if (env != null)
/*     */             {
/* 429 */               idcDir = ComponentLocationUtils.getEnvironmentValue(idcKey, env);
/*     */             }
/* 433 */             else if (idcKey.equals("IntradocDir"))
/*     */             {
/* 435 */               idcDir = LegacyDirectoryLocator.getIntradocDir();
/*     */             }
/* 437 */             else if (idcKey.equals("$IdcHomeDir"))
/*     */             {
/* 439 */               idcDir = LegacyDirectoryLocator.getHomeDirectory();
/*     */             }
/* 441 */             else if (idcKey.equals("$ComponentDir"))
/*     */             {
/* 443 */               idcDir = SharedObjects.getEnvironmentValue("ComponentDir");
/*     */             }
/* 445 */             else if (idcKey.equals("$SystemComponentDir"))
/*     */             {
/* 447 */               idcDir = SharedObjects.getEnvironmentValue("SystemComponentDir");
/*     */             }
/*     */ 
/* 450 */             if (idcDir != null)
/*     */             {
/* 452 */               pathToCheck = FileUtils.directorySlashes(idcDir) + path.substring(idcKey.length() + 2);
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 457 */         if (pathToCheck != null)
/*     */         {
/* 459 */           String altPath = null;
/* 460 */           if ((componentsInDev) && 
/* 465 */             (path.endsWith(".jar")))
/*     */           {
/* 467 */             altPath = FileUtils.getDirectory(pathToCheck) + "/classes/";
/*     */           }
/*     */ 
/* 470 */           boolean didCheck = false;
/* 471 */           if (altPath != null)
/*     */           {
/* 473 */             fileCheckResult = FileUtils.checkFile(altPath, false, false);
/* 474 */             if (fileCheckResult == 0)
/*     */             {
/* 476 */               if (SystemUtils.m_verbose)
/*     */               {
/* 478 */                 Report.trace("system", "Altering component jar file path to use " + altPath, null);
/*     */               }
/*     */ 
/* 481 */               path = altPath;
/* 482 */               didCheck = true;
/*     */             }
/*     */           }
/* 485 */           if (!didCheck)
/*     */           {
/* 487 */             fileCheckResult = FileUtils.checkFile(pathToCheck, false, false);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 492 */       if ((fileCheckResult != 0) && (fileCheckResult != -24))
/*     */       {
/* 495 */         exists = false;
/* 496 */         if (SystemUtils.m_verbose)
/*     */         {
/* 500 */           String ext = FileUtils.getExtension(path);
/* 501 */           boolean isFile = false;
/* 502 */           int extLength = ext.length();
/* 503 */           if ((extLength > 0) && (ext.indexOf(92) < 0) && (ext.indexOf(47) < 0))
/*     */           {
/* 505 */             isFile = StringUtils.matchEx(ext, "s?|d??|j??|z??", true, true);
/*     */           }
/* 507 */           String fileErrMsg = FileUtils.getErrorMsg(path, isFile, fileCheckResult);
/* 508 */           String fileErrMsgLocalized = LocaleResources.localizeMessage(fileErrMsg, null);
/*     */ 
/* 510 */           Report.trace("system", "Suppressing component path element in computation for launcher. " + fileErrMsgLocalized, null);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 517 */     if (!exists)
/*     */       return;
/* 519 */     pathArr.add(path);
/* 520 */     order.add(pname);
/* 521 */     props.put(pname, path);
/*     */   }
/*     */ 
/*     */   public static boolean updateComponentListColumns(DataResultSet components)
/*     */   {
/* 529 */     boolean isChanged = false;
/* 530 */     List missingColumns = new ArrayList();
/* 531 */     String[] compColumns = ComponentListEditor.COMPONENT_COLUMNS;
/* 532 */     for (int i = 0; i < compColumns.length; ++i)
/*     */     {
/* 534 */       String clmn = compColumns[i];
/* 535 */       FieldInfo fieldInfo = new FieldInfo();
/* 536 */       boolean exists = components.getFieldInfo(clmn, fieldInfo);
/* 537 */       if (exists)
/*     */         continue;
/* 539 */       fieldInfo.m_name = clmn;
/* 540 */       missingColumns.add(fieldInfo);
/*     */     }
/*     */ 
/* 544 */     if (!missingColumns.isEmpty())
/*     */     {
/* 546 */       components.mergeFieldsWithFlags(missingColumns, 0);
/* 547 */       isChanged = true;
/*     */     }
/* 549 */     return isChanged;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 554 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ComponentListUtils
 * JD-Core Version:    0.5.4
 */