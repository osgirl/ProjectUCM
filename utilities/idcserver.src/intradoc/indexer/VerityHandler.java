/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.shared.ActiveIndexState;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class VerityHandler extends IndexerExecutionHandler
/*     */ {
/*     */   protected String m_styleDir;
/*     */ 
/*     */   public void init(IndexerExecution exec)
/*     */     throws ServiceException
/*     */   {
/*  43 */     super.init(exec);
/*     */ 
/*  46 */     this.m_styleDir = this.m_config.getValue("IndexerStyleDir");
/*     */ 
/*  48 */     loadVerityEncodingInfo();
/*     */ 
/*  50 */     setEnvPathName();
/*  51 */     setIndexerPath();
/*  52 */     setIndexerDir();
/*     */ 
/*  55 */     String logDir = this.m_data.getEnvironmentValue("SearchLogFileDir");
/*  56 */     if (logDir == null)
/*     */     {
/*  58 */       logDir = DirectoryLocator.getLogDirectory() + "verity/";
/*     */     }
/*  60 */     this.m_config.setValue("IndexerLogDir", logDir);
/*     */   }
/*     */ 
/*     */   protected void setEnvPathName()
/*     */   {
/*  65 */     this.m_config.setValue("IndexerEnvName", EnvUtils.getLibraryPathEnvironmentVariableName());
/*     */   }
/*     */ 
/*     */   protected void setIndexerPath()
/*     */     throws ServiceException
/*     */   {
/*  72 */     String indexerPath = this.m_data.getEnvironmentValue("IndexerPath");
/*  73 */     if ((indexerPath != null) && (indexerPath.length() > 0))
/*     */     {
/*  75 */       indexerPath = FileUtils.fileSlashes(indexerPath);
/*     */     }
/*     */     else
/*     */     {
/*  79 */       String osPathComponent = findOSPathComponent(this.m_config.m_currentEngineName);
/*     */ 
/*  81 */       indexerPath = this.m_config.getConfigValue("IndexerPathFrag") + osPathComponent + "/bin/mkvdk" + EnvUtils.getExecutableFileSuffix();
/*     */     }
/*     */ 
/*  86 */     indexerPath = EnvUtils.convertPathToOSConventions(indexerPath);
/*  87 */     this.m_config.setValue("IndexerPath", indexerPath);
/*     */   }
/*     */ 
/*     */   protected String findOSPathComponent(String verityVersion)
/*     */     throws ServiceException
/*     */   {
/*  93 */     DataResultSet platformMap = SharedObjects.getTable("VerityPlatformMap");
/*     */     FieldInfo[] infos;
/*     */     try
/*     */     {
/*  97 */       infos = ResultSetUtils.createInfoList(platformMap, new String[] { "vVerityPlatformKey", "vOSName" }, true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 102 */       if (SystemUtils.m_verbose)
/*     */       {
/* 104 */         Report.debug("indexer", null, e);
/*     */       }
/*     */ 
/* 108 */       String msg = LocaleUtils.encodeMessage("csResourceTableInvalid", e.getMessage(), "VerityPlatformMap");
/*     */ 
/* 110 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 113 */     String key = verityVersion + "," + EnvUtils.getOSName();
/* 114 */     Vector v = platformMap.findRow(infos[0].m_index, key);
/* 115 */     if (v == null)
/*     */     {
/* 117 */       key = EnvUtils.getOSName();
/* 118 */       v = platformMap.findRow(infos[0].m_index, key);
/*     */     }
/*     */ 
/* 121 */     if (v == null)
/*     */     {
/* 123 */       String msg = LocaleUtils.encodeMessage("csIndexerVerityNotSupported", null, verityVersion);
/*     */ 
/* 125 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 128 */     String osPathComponent = (String)v.elementAt(infos[1].m_index);
/* 129 */     if (SystemUtils.m_verbose)
/*     */     {
/* 131 */       Report.debug("indexer", "findOSPathComponent() returning '" + osPathComponent + "' for vdk '" + verityVersion + "'.", null);
/*     */     }
/*     */ 
/* 135 */     return osPathComponent;
/*     */   }
/*     */ 
/*     */   protected void setIndexerDir()
/*     */     throws ServiceException
/*     */   {
/* 143 */     String indexerDir = this.m_config.getValue("IndexerDir");
/* 144 */     if (indexerDir == null)
/*     */     {
/* 146 */       String indexerPath = this.m_config.getValue("IndexerPath");
/*     */ 
/* 149 */       indexerDir = FileUtils.fileSlashes(indexerPath);
/* 150 */       indexerDir = FileUtils.getParent(indexerDir);
/* 151 */       indexerDir = FileUtils.getParent(indexerDir);
/* 152 */       if (indexerDir == null)
/*     */       {
/* 154 */         String msg = LocaleUtils.encodeMessage("csIndexerPathInvalid", null, indexerPath);
/* 155 */         throw new ServiceException(msg);
/*     */       }
/* 157 */       indexerDir = FileUtils.fileSlashes(indexerDir);
/* 158 */       indexerDir = EnvUtils.convertPathToOSConventions(indexerDir);
/* 159 */       this.m_config.setValue("IndexerDir", indexerDir);
/*     */     }
/*     */ 
/* 162 */     String indexerInstallDir = this.m_config.getValue("IndexerInstallDir");
/* 163 */     if (indexerInstallDir != null)
/*     */       return;
/* 165 */     indexerInstallDir = this.m_config.getConfigValue("VerityInstallDir");
/* 166 */     if (indexerInstallDir != null)
/*     */       return;
/* 168 */     indexerInstallDir = FileUtils.getParent(indexerDir) + "/common";
/* 169 */     indexerInstallDir = FileUtils.fileSlashes(indexerInstallDir);
/* 170 */     indexerInstallDir = EnvUtils.convertPathToOSConventions(indexerInstallDir);
/* 171 */     this.m_config.setValue("IndexerInstallDir", indexerInstallDir);
/*     */   }
/*     */ 
/*     */   public void prepareIndexDoc(Properties prop, IndexerInfo ii)
/*     */   {
/* 179 */     boolean supportEncode = this.m_config.getBoolean("IsSupportEncodeKey", true);
/* 180 */     if (((supportEncode) && (((this.m_data.isRebuild()) || (this.m_execution.m_isCreateCollection)))) || ((!this.m_data.isRebuild()) && (SearchLoader.m_encodeVdkKeyForSearch)))
/*     */     {
/* 183 */       String key = (String)prop.get("VdkVgwKey");
/* 184 */       IdcStringBuilder buf = new IdcStringBuilder("z");
/*     */       try
/*     */       {
/* 187 */         StringUtils.appendAsHex(buf, key);
/*     */       }
/*     */       catch (UnsupportedEncodingException ignore)
/*     */       {
/*     */       }
/*     */ 
/* 193 */       key = buf.toString();
/* 194 */       prop.put("VdkVgwKey", key);
/*     */ 
/* 196 */       if ((this.m_execution.m_isCreateCollection) && (!this.m_data.isRebuild()))
/*     */       {
/* 198 */         SearchLoader.m_encodeVdkKeyForSearch = true;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 203 */     String locale = this.m_config.getConfigValue("IndexerLocale");
/* 204 */     String vlangVal = (String)prop.get("xIdcLanguage");
/* 205 */     if ((locale == null) || (!locale.equals("uni")) || (vlangVal == null) || (vlangVal.length() <= 0))
/*     */       return;
/* 207 */     prop.put("VLANG", vlangVal);
/*     */   }
/*     */ 
/*     */   public int parseResults(String input)
/*     */     throws ServiceException
/*     */   {
/* 215 */     int start = 0;
/* 216 */     int end = 0;
/*     */ 
/* 218 */     Vector infoList = this.m_execution.m_indexerInfoList;
/* 219 */     int length = infoList.size();
/* 220 */     int count = -1;
/*     */ 
/* 222 */     String preText = this.m_config.getValue("IndexerDocCountPreText");
/* 223 */     String postText = this.m_config.getValue("IndexerDocCountPostText");
/* 224 */     String skipText = this.m_config.getValue("IndexerSkipText");
/* 225 */     int preTextLength = preText.length();
/*     */ 
/* 227 */     boolean checkDocCount = this.m_config.getBoolean("UseIndexerDocCountCheck", true);
/*     */ 
/* 229 */     while (end <= input.length())
/*     */     {
/* 231 */       end = input.indexOf("\n", start);
/*     */ 
/* 233 */       if (end < 0) {
/*     */         break;
/*     */       }
/*     */ 
/* 237 */       String line = input.substring(start, end);
/*     */ 
/* 247 */       if (line.indexOf(skipText) > 0)
/*     */       {
/* 249 */         IndexerInfo info = null;
/* 250 */         int index = 0;
/* 251 */         int keyStart = 0;
/* 252 */         while (keyStart >= 0)
/*     */         {
/* 254 */           keyStart = line.indexOf("(", index);
/* 255 */           index = keyStart + 1;
/* 256 */           int keyEnd = line.indexOf(")", keyStart);
/* 257 */           if (keyEnd >= 0)
/*     */           {
/* 259 */             index = keyEnd + 1;
/*     */ 
/* 261 */             String key = line.substring(keyStart + 1, keyEnd);
/* 262 */             for (int i = 0; i < length; ++i)
/*     */             {
/* 264 */               info = (IndexerInfo)infoList.elementAt(i);
/* 265 */               if ((key.startsWith("z")) && (info.m_encodedKey != null))
/*     */               {
/* 267 */                 if (!info.m_encodedKey.equals(key))
/*     */                   continue;
/* 269 */                 info.m_indexStatus = 1;
/* 270 */                 break;
/*     */               }
/*     */ 
/* 275 */               if (!info.m_indexKey.equals(key))
/*     */                 continue;
/* 277 */               info.m_indexStatus = 1;
/* 278 */               break;
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/* 285 */       else if (checkDocCount)
/*     */       {
/* 287 */         int i1 = line.indexOf(preText);
/* 288 */         int i2 = line.indexOf(postText);
/* 289 */         if ((i2 > i1) && (i1 > -1))
/*     */         {
/* 291 */           String countText = line.substring(i1 + preTextLength, i2);
/* 292 */           count = NumberUtils.parseInteger(countText.trim(), -1);
/*     */         }
/*     */       }
/*     */ 
/* 296 */       start = end + 1;
/*     */     }
/*     */ 
/* 299 */     return count;
/*     */   }
/*     */ 
/*     */   public void createCollection(String collectionID)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void verifyCollection(IndexerCollectionData currentCollectionDef)
/*     */     throws ServiceException
/*     */   {
/* 313 */     IndexerCollectionData collectionDef = this.m_execution.m_collectionDef;
/* 314 */     if (collectionDef == null)
/*     */     {
/* 316 */       String extension = "intradocbasic";
/*     */ 
/* 319 */       String styleDir = this.m_execution.m_buildCollectionDir + extension + "/style/";
/*     */       try
/*     */       {
/* 322 */         collectionDef = new IndexerCollectionData();
/*     */ 
/* 325 */         readUflStyleFile(styleDir, collectionDef);
/* 326 */         readDftStyleFile(styleDir, collectionDef);
/*     */ 
/* 328 */         this.m_execution.m_collectionDef = collectionDef;
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 332 */         throw new ServiceException("!csIndexerUnableToVerify", e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 337 */     currentCollectionDef.shallowClone(collectionDef);
/*     */   }
/*     */ 
/*     */   protected void readUflStyleFile(String path, IndexerCollectionData collectionDef)
/*     */     throws IOException, ServiceException
/*     */   {
/* 346 */     Hashtable infoMap = new Hashtable();
/* 347 */     BufferedReader reader = null;
/* 348 */     File styleFile = FileUtilsCfgBuilder.getCfgFile(path + "style.ufl", "Search", false);
/*     */     try
/*     */     {
/*     */       try
/*     */       {
/* 353 */         reader = new BufferedReader(FileUtilsCfgBuilder.getCfgReader(styleFile));
/*     */       }
/*     */       catch (FileNotFoundException e)
/*     */       {
/* 357 */         throw new ServiceException(-16, "!csIndexerUnableToParseStyleFile", e);
/*     */       }
/*     */ 
/* 361 */       int state = 0;
/* 362 */       StringBuffer buffer = null;
/* 363 */       boolean isComment = false;
/* 364 */       String thisCollectionSearchString = "VDK4";
/* 365 */       boolean encodeVdkKey = false;
/* 366 */       String dataTable = null;
/*     */       while (true)
/*     */       {
/* 369 */         int c = reader.read();
/*     */ 
/* 371 */         if (c == -1) {
/*     */           break;
/*     */         }
/*     */ 
/* 375 */         switch (state)
/*     */         {
/*     */         case 0:
/* 378 */           if ((c == 123) || (c == 35))
/*     */           {
/* 380 */             buffer = new StringBuffer();
/* 381 */             state = 1;
/* 382 */             if (c == 35)
/*     */             {
/* 384 */               isComment = true;
/*     */             }
/*     */             else
/*     */             {
/* 388 */               isComment = false;
/*     */             }
/*     */           }
/* 391 */           else if ((c != 10) && (c != 13) && (c != 9))
/*     */           {
/* 393 */             buffer = new StringBuffer();
/* 394 */             buffer.append((char)c);
/* 395 */             state = 2; } break;
/*     */         case 1:
/* 399 */           if (c == 125)
/*     */           {
/* 401 */             state = 0;
/*     */ 
/* 403 */             Vector fieldInfoArray = parseFieldInfoArray(buffer.toString());
/*     */ 
/* 408 */             buildStructure(dataTable, fieldInfoArray, collectionDef);
/*     */           }
/* 410 */           else if ((isComment) && (((c == 10) || (c == 13) || (c == 9))))
/*     */           {
/* 412 */             state = 0;
/* 413 */             isComment = false;
/* 414 */             String tempBuf = buffer.toString();
/* 415 */             if (tempBuf.indexOf("SearchCompatibilityVersion") >= 0)
/*     */             {
/* 417 */               int index = tempBuf.indexOf(61);
/* 418 */               if (index > 0)
/*     */               {
/* 420 */                 thisCollectionSearchString = tempBuf.substring(index + 1);
/*     */               }
/*     */             }
/* 423 */             else if (tempBuf.indexOf("EncodeVdkKey") >= 0)
/*     */             {
/* 425 */               int index = tempBuf.indexOf(61);
/* 426 */               if ((((index > 0) ? 1 : 0) & ((tempBuf.indexOf("true", index) > 0) ? 1 : 0)) != 0)
/*     */               {
/* 428 */                 encodeVdkKey = true;
/*     */               }
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 434 */             buffer.append((char)c);
/*     */           }
/* 436 */           break;
/*     */         case 2:
/* 439 */           if ((c == 10) || (c == 13) || (c == 9))
/*     */           {
/* 441 */             state = 0;
/* 442 */             dataTable = parseDataTable(buffer.toString());
/*     */           }
/*     */           else
/*     */           {
/* 446 */             buffer.append((char)c);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 453 */       String currentSearchEngine = SharedObjects.getEnvironmentValue("SearchEngineName");
/* 454 */       if (currentSearchEngine == null)
/*     */       {
/* 456 */         currentSearchEngine = SharedObjects.getEnvironmentValue("SearchIndexerEngineName");
/*     */       }
/*     */ 
/* 459 */       DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/* 460 */       FieldInfo[] fis = null;
/*     */       try
/*     */       {
/* 463 */         fis = ResultSetUtils.createInfoList(searchEngines, new String[] { "seId", "seIndexCompatibility", "seSearchCompatibility" }, true);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 468 */         throw new ServiceException(e);
/*     */       }
/*     */ 
/* 471 */       if (searchEngines.findRow(fis[0].m_index, currentSearchEngine) != null)
/*     */       {
/* 473 */         String currentSearchCompat = searchEngines.getStringValue(fis[2].m_index);
/* 474 */         if (!thisCollectionSearchString.equals(currentSearchCompat))
/*     */         {
/* 476 */           String msg = LocaleUtils.encodeMessage("csVerifyCollectionErrorCurrentSearchIncompatible", null, thisCollectionSearchString);
/* 477 */           Report.trace("indexer", LocaleResources.localizeMessage(msg, null), null);
/* 478 */           Report.error("indexer", msg, null);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 483 */         String msg = LocaleUtils.encodeMessage("csVerifyCollectionErrorInvalidSearchEngine", null, currentSearchEngine);
/* 484 */         Report.trace("indexer", LocaleResources.localizeMessage(msg, null), null);
/* 485 */         Report.error("indexer", msg, null);
/*     */       }
/*     */ 
/* 488 */       FieldInfo urlInfo = new FieldInfo();
/* 489 */       urlInfo.m_type = 6;
/* 490 */       infoMap.put("URL", urlInfo);
/*     */ 
/* 492 */       SearchLoader.m_encodeVdkKeyForSearch = encodeVdkKey;
/*     */     }
/*     */     finally
/*     */     {
/* 496 */       FileUtils.closeObject(reader);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Vector parseFieldInfoArray(String text) throws ServiceException
/*     */   {
/* 502 */     Vector fiArr = new IdcVector();
/* 503 */     Vector tempV = StringUtils.parseArray(text, '\n', '^');
/* 504 */     for (int i = 0; i < tempV.size(); ++i)
/*     */     {
/* 506 */       String temp = (String)tempV.elementAt(i);
/* 507 */       if ((temp == null) || ((temp = temp.trim()).length() <= 0) || 
/* 509 */         (temp.indexOf(61) >= 0))
/*     */         continue;
/* 511 */       FieldInfo info = new FieldInfo();
/* 512 */       parseFieldInfo(info, temp);
/* 513 */       fiArr.addElement(info);
/*     */     }
/*     */ 
/* 518 */     return fiArr;
/*     */   }
/*     */ 
/*     */   protected void parseFieldInfo(FieldInfo info, String text) throws ServiceException
/*     */   {
/* 523 */     String[] elements = parseFieldData(text);
/* 524 */     info.m_name = elements[1];
/* 525 */     if (elements[0].equals("fixwidth:"))
/*     */     {
/* 527 */       if (elements[3].equals("signed-integer"))
/*     */       {
/* 529 */         info.m_type = 3;
/*     */       }
/*     */       else
/*     */       {
/* 533 */         info.m_type = 5;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/* 538 */       info.m_type = 6;
/*     */   }
/*     */ 
/*     */   protected String parseDataTable(String str)
/*     */   {
/* 544 */     String[] elements = parseFieldData(str);
/* 545 */     if ((elements[0] != null) && (elements[0].equals("data-table:")))
/*     */     {
/* 547 */       return elements[1];
/*     */     }
/* 549 */     return null;
/*     */   }
/*     */ 
/*     */   protected String[] parseFieldData(String text)
/*     */   {
/* 555 */     String[] elements = new String[10];
/* 556 */     int length = text.length();
/* 557 */     int state = 0;
/* 558 */     StringBuffer buffer = null;
/* 559 */     int count = 0;
/* 560 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 562 */       char c = text.charAt(i);
/* 563 */       switch (state)
/*     */       {
/*     */       case 0:
/* 566 */         if (Character.isWhitespace(c))
/*     */           continue;
/* 568 */         buffer = new StringBuffer();
/* 569 */         buffer.append(c);
/* 570 */         state = 1; break;
/*     */       case 1:
/* 574 */         if (!Character.isWhitespace(c))
/*     */         {
/* 576 */           buffer.append(c);
/*     */         }
/*     */         else
/*     */         {
/* 580 */           elements[(count++)] = buffer.toString();
/* 581 */           state = 0;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 587 */     if (buffer.length() > 0)
/*     */     {
/* 589 */       elements[(count++)] = buffer.toString();
/*     */     }
/*     */ 
/* 592 */     return elements;
/*     */   }
/*     */ 
/*     */   protected void buildStructure(String dataTable, Vector fieldInfoList, IndexerCollectionData collectionDef)
/*     */   {
/* 598 */     int size = fieldInfoList.size();
/* 599 */     boolean hasDataTable = false;
/* 600 */     if (size == 1)
/*     */     {
/* 602 */       hasDataTable = true;
/*     */     }
/* 604 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 606 */       FieldInfo info = (FieldInfo)fieldInfoList.elementAt(i);
/* 607 */       if (info.m_name.startsWith("z"))
/*     */       {
/* 610 */         info.m_name = info.m_name.substring(1);
/* 611 */         collectionDef.m_securityInfos.put(info.m_name, info);
/*     */       }
/*     */       else
/*     */       {
/* 615 */         collectionDef.m_fieldInfos.put(info.m_name, info);
/*     */       }
/*     */ 
/* 618 */       Properties props = new Properties();
/* 619 */       collectionDef.m_fieldDesignMap.put(info.m_name, props);
/* 620 */       if (!hasDataTable)
/*     */         continue;
/* 622 */       props.put("hasDataTable", "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void readDftStyleFile(String path, IndexerCollectionData collectionDef) throws IOException, ServiceException
/*     */   {
/* 634 */     File styleFile = FileUtilsCfgBuilder.getCfgFile(path + "style.dft", "Search", false);
/*     */     BufferedReader reader;
/*     */     try
/*     */     {
/* 637 */       reader = new BufferedReader(FileUtilsCfgBuilder.getCfgReader(styleFile));
/*     */     }
/*     */     catch (FileNotFoundException e)
/*     */     {
/* 641 */       throw new ServiceException(-16, "!csIndexerUnableToParseSecurityStyleFile", e);
/*     */     }
/*     */ 
/* 659 */     int state = 0;
/* 660 */     StringBuffer buffer = null;
/*     */     while (true)
/*     */     {
/* 663 */       int c = reader.read();
/*     */ 
/* 665 */       if (c == -1) {
/*     */         return;
/*     */       }
/*     */ 
/* 669 */       switch (state)
/*     */       {
/*     */       case 0:
/* 672 */         if (c == 123)
/*     */         {
/* 674 */           buffer = new StringBuffer();
/* 675 */           state = 1; } break;
/*     */       case 1:
/* 679 */         if (c == 125)
/*     */         {
/* 681 */           state = 0;
/*     */ 
/* 684 */           parseZoneFields(buffer.toString(), collectionDef);
/*     */         }
/*     */         else
/*     */         {
/* 688 */           buffer.append((char)c);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void parseZoneFields(String str, IndexerCollectionData collectionDef)
/*     */   {
/* 697 */     Vector tempV = StringUtils.parseArray(str, '\n', '^');
/*     */ 
/* 699 */     Hashtable securityInfos = collectionDef.m_securityInfos;
/* 700 */     Hashtable fieldDesigns = collectionDef.m_fieldDesignMap;
/* 701 */     String fieldName = null;
/* 702 */     Properties props = null;
/* 703 */     for (int i = 0; i < tempV.size(); ++i)
/*     */     {
/* 705 */       String temp = (String)tempV.elementAt(i);
/* 706 */       if ((temp == null) || ((temp = temp.trim()).length() <= 0)) {
/*     */         continue;
/*     */       }
/* 709 */       String[] data = parseFieldData(temp);
/* 710 */       if (data[0].equals("field:"))
/*     */       {
/* 712 */         if (fieldName != null)
/*     */         {
/* 714 */           addZoneField(fieldName, props, securityInfos, fieldDesigns);
/*     */         }
/* 716 */         fieldName = data[1];
/* 717 */         props = new Properties();
/*     */       } else {
/* 719 */         if (fieldName == null)
/*     */           continue;
/* 721 */         String val = data[0];
/* 722 */         int index = val.indexOf(61);
/* 723 */         if (index < 0)
/*     */           continue;
/* 725 */         props.put(val.substring(0, index), val.substring(index + 1));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 731 */     if (fieldName == null)
/*     */       return;
/* 733 */     addZoneField(fieldName, props, securityInfos, fieldDesigns);
/*     */   }
/*     */ 
/*     */   protected void addZoneField(String fieldName, Properties props, Hashtable securityInfos, Hashtable fieldDesigns)
/*     */   {
/* 739 */     String zone = props.getProperty("/zone");
/* 740 */     if (zone == null)
/*     */       return;
/* 742 */     if (fieldName.startsWith("z"))
/*     */     {
/* 744 */       fieldName = fieldName.substring(1);
/* 745 */       FieldInfo info = new FieldInfo();
/* 746 */       info.m_name = fieldName;
/* 747 */       securityInfos.put(fieldName, info);
/*     */     }
/*     */     else
/*     */     {
/* 751 */       Properties fieldProps = (Properties)fieldDesigns.get(fieldName);
/* 752 */       if (fieldProps == null)
/*     */       {
/* 754 */         Report.trace(null, "VerityHandler.addZoneField: The zone field " + fieldName + " does not exist in the ufl file.", null);
/*     */       }
/*     */       else
/*     */       {
/* 759 */         fieldProps.put("isZone", "1");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void validateConfig()
/*     */     throws ServiceException
/*     */   {
/* 768 */     FileUtils.validatePath(this.m_config.getValue("IndexerStyleDir"), IdcMessageFactory.lc("csIndexerStyleDirError", new Object[0]), 0);
/*     */ 
/* 771 */     FileUtils.checkOrCreateDirectory(this.m_config.getValue("IndexerBulkloadDir"), 0);
/*     */ 
/* 774 */     FileUtils.validatePath(this.m_config.getValue("IndexerNoTextFile"), IdcMessageFactory.lc("csIndexerErrorWithDefaultFile", new Object[0]), 1);
/*     */ 
/* 777 */     FileUtils.validatePath(this.m_config.getValue("IndexerPath"), IdcMessageFactory.lc("csIndexerPathError", new Object[0]), 1);
/*     */ 
/* 780 */     String skipText = null;
/* 781 */     String docCountPreText = null;
/* 782 */     String docCountPostText = null;
/* 783 */     String tmp = SharedObjects.getEnvironmentValue("VeritySkipText");
/* 784 */     if ((tmp != null) && (tmp.length() > 0))
/*     */     {
/* 786 */       skipText = tmp;
/*     */     }
/* 788 */     tmp = SharedObjects.getEnvironmentValue("VerityDocCountPrefixText");
/* 789 */     if ((tmp != null) && (tmp.length() > 0))
/*     */     {
/* 791 */       docCountPreText = tmp;
/*     */     }
/* 793 */     tmp = SharedObjects.getEnvironmentValue("VerityDocCountSuffixText");
/* 794 */     if ((tmp != null) && (tmp.length() > 0))
/*     */     {
/* 796 */       docCountPostText = tmp;
/*     */     }
/*     */ 
/* 799 */     if ((skipText != null) && (skipText.length() != 0) && (docCountPreText != null) && (docCountPreText.length() != 0) && (docCountPostText != null) && (docCountPostText.length() != 0)) {
/*     */       return;
/*     */     }
/* 802 */     DataResultSet textConfig = SharedObjects.getTable("VerityContextText");
/* 803 */     FieldInfo[] infos = null;
/* 804 */     if (textConfig != null)
/*     */     {
/*     */       try
/*     */       {
/* 808 */         infos = ResultSetUtils.createInfoList(textConfig, new String[] { "lcSearchLocale", "lcVeritySkipText", "lcVerityCountPreText", "lcVerityCountPostText" }, true);
/*     */       }
/*     */       catch (DataException ignore)
/*     */       {
/* 814 */         Report.trace(null, "The VerityContextText table is misconfigured.", null);
/*     */       }
/*     */     }
/* 817 */     if (infos == null)
/*     */       return;
/* 819 */     String locale = this.m_config.getValue("IndexerLocale");
/* 820 */     Vector v = textConfig.findRow(infos[0].m_index, locale);
/* 821 */     if (v == null)
/*     */       return;
/* 823 */     if ((skipText == null) || (skipText.length() == 0))
/*     */     {
/* 825 */       skipText = (String)v.elementAt(1);
/* 826 */       this.m_config.setValue("VeritySkipText", skipText);
/*     */     }
/*     */ 
/* 829 */     if ((docCountPreText == null) || (docCountPreText.length() == 0))
/*     */     {
/* 831 */       docCountPreText = (String)v.elementAt(2);
/* 832 */       this.m_config.setValue("VerityDocCountPrefixText", docCountPreText);
/*     */     }
/*     */ 
/* 835 */     if ((docCountPostText != null) && (docCountPostText.length() != 0))
/*     */       return;
/* 837 */     docCountPostText = (String)v.elementAt(3);
/* 838 */     this.m_config.setValue("VerityDocCountSuffixText", docCountPostText);
/*     */   }
/*     */ 
/*     */   public boolean checkCollectionExistence(boolean mustExist, String errMsg)
/*     */     throws ServiceException
/*     */   {
/* 848 */     String stylePath = this.m_execution.m_buildCollectionDir + "intradocbasic/style/style.ufl";
/* 849 */     int errVal = FileUtils.checkFile(stylePath, true, false);
/* 850 */     boolean retVal = errVal == 0;
/* 851 */     if ((mustExist) && (!retVal))
/*     */     {
/* 853 */       String fileErrMsg = FileUtils.getErrorMsg(stylePath, true, errVal);
/* 854 */       errMsg = errMsg + "\n" + fileErrMsg;
/* 855 */       ServiceException e = new ServiceException(errMsg);
/* 856 */       Report.trace("indexer", null, e);
/* 857 */       throw e;
/*     */     }
/* 859 */     return retVal;
/*     */   }
/*     */ 
/*     */   public int cleanUp()
/*     */     throws ServiceException
/*     */   {
/* 865 */     if (this.m_data.isRebuild())
/*     */     {
/* 867 */       Hashtable indexerConfigs = (Hashtable)SharedObjects.getObject("globalObjects", "IndexerConfigs");
/* 868 */       IndexerConfig currentConfig = (IndexerConfig)indexerConfigs.get("update");
/* 869 */       IndexerConfig rebuildConfig = (IndexerConfig)indexerConfigs.get("rebuild");
/*     */ 
/* 871 */       String currentSearchCompatibility = currentConfig.getConfigValue("seSearchCompatibility");
/* 872 */       String rebuildSearchCompatibility = rebuildConfig.getConfigValue("seSearchCompatibility");
/*     */ 
/* 874 */       if (!currentSearchCompatibility.equals(rebuildSearchCompatibility))
/*     */       {
/* 876 */         this.m_execution.m_storeIndexOnly = true;
/*     */ 
/* 878 */         String msg = LocaleUtils.encodeMessage("csVdkRebuildIncompatibleEngineWarning", null, rebuildConfig.getConfigValue("seId"));
/* 879 */         Report.trace("indexer", LocaleResources.localizeMessage(msg, null), null);
/* 880 */         Report.warning("indexer", msg, null);
/*     */       }
/*     */     }
/*     */ 
/* 884 */     return 1;
/*     */   }
/*     */ 
/*     */   protected void loadVerityEncodingInfo()
/*     */   {
/* 891 */     String oldVerityLocale = null;
/*     */     String verityLocale;
/*     */     String verityEncoding;
/*     */     try {
/* 895 */       verityLocale = SearchLoader.computeSearchLocale();
/* 896 */       if (!this.m_data.isRebuild())
/*     */       {
/* 898 */         DataResultSet sColls = (DataResultSet)ActiveIndexState.getSearchCollections();
/* 899 */         if (sColls == null)
/*     */         {
/* 901 */           throw new DataException("!$SearchCollections table does not exist.");
/*     */         }
/* 903 */         FieldInfo[] info = ResultSetUtils.createInfoList(sColls, new String[] { "sCollectionID", "sVerityLocale" }, true);
/*     */ 
/* 905 */         Vector v = sColls.findRow(info[0].m_index, SharedObjects.getEnvironmentValue("IDC_Name"));
/* 906 */         if (v != null)
/*     */         {
/* 908 */           oldVerityLocale = verityLocale;
/* 909 */           verityLocale = (String)v.elementAt(info[1].m_index);
/*     */         }
/*     */       }
/*     */ 
/* 913 */       String engineName = this.m_config.getCurrentEngineName();
/*     */ 
/* 916 */       verityEncoding = SearchLoader.computeVerityEncodingEx(verityLocale, engineName);
/*     */ 
/* 920 */       if ((verityEncoding == null) && (oldVerityLocale != null))
/*     */       {
/* 922 */         verityLocale = oldVerityLocale;
/* 923 */         verityEncoding = SearchLoader.computeVerityEncodingEx(verityLocale, engineName);
/*     */       }
/*     */ 
/* 927 */       if (verityEncoding == null)
/*     */       {
/* 929 */         verityEncoding = this.m_config.getValue("IndexerEncoding");
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 934 */       Report.error(null, "!csIndexerCouldNotLocalize", e);
/* 935 */       return;
/*     */     }
/*     */ 
/* 939 */     DataResultSet localeMap = SharedObjects.getTable("SearchLocaleMap");
/* 940 */     FieldInfo fi = new FieldInfo();
/* 941 */     FieldInfo fi2 = new FieldInfo();
/* 942 */     localeMap.getFieldInfo("slmSearchLocale", fi);
/* 943 */     localeMap.getFieldInfo("slmNewSearchLocale", fi2);
/*     */ 
/* 945 */     String engineName = this.m_config.m_currentEngineName;
/* 946 */     boolean finished = false;
/* 947 */     while (!finished)
/*     */     {
/* 949 */       String key = "(" + engineName + ")" + verityLocale;
/* 950 */       if (localeMap.findRow(fi.m_index, key) != null)
/*     */       {
/* 952 */         verityLocale = localeMap.getStringValue(fi2.m_index);
/* 953 */         finished = true;
/*     */       }
/*     */       else
/*     */       {
/* 957 */         int index = engineName.lastIndexOf(".");
/* 958 */         if (index >= 0)
/*     */         {
/* 960 */           engineName = engineName.substring(0, index);
/*     */         }
/*     */         else
/*     */         {
/* 964 */           finished = true;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 969 */     this.m_config.setValue("IndexerLocale", verityLocale);
/* 970 */     this.m_config.setValue("IndexerEncoding", verityEncoding);
/*     */ 
/* 973 */     DataResultSet context = SharedObjects.getTable("VerityContextText");
/* 974 */     Vector row = context.findRow(0, verityLocale);
/* 975 */     if (row == null)
/*     */       return;
/* 977 */     String skipText = (String)row.elementAt(1);
/* 978 */     String preText = (String)row.elementAt(2);
/* 979 */     String postText = (String)row.elementAt(3);
/* 980 */     this.m_config.setValue("IndexerSkipText", skipText);
/* 981 */     this.m_config.setValue("IndexerDocCountPreText", preText);
/* 982 */     this.m_config.setValue("IndexerDocCountPostText", postText);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 988 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.VerityHandler
 * JD-Core Version:    0.5.4
 */