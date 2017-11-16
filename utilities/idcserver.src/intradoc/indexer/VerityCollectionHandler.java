/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.taskmanager.TaskInfo;
/*     */ import intradoc.taskmanager.TaskInfo.STATUS;
/*     */ import intradoc.taskmanager.TaskMonitor;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.FileReader;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class VerityCollectionHandler extends CollectionHandlerImpl
/*     */ {
/*     */   protected String m_styleDir;
/*     */ 
/*     */   public void init(IndexerWorkObject data, IndexerCollectionManager manager)
/*     */     throws ServiceException
/*     */   {
/*  42 */     super.init(data, manager);
/*     */ 
/*  45 */     this.m_styleDir = this.m_config.getValue("IndexerStyleDir");
/*     */   }
/*     */ 
/*     */   public boolean isCollectionUpToDate(IndexerWorkObject data)
/*     */     throws ServiceException
/*     */   {
/*  51 */     boolean isUpToDate = !data.isRebuild();
/*  52 */     Boolean createdCollection = (Boolean)data.getCachedObject("CollectionDesignUpToDate");
/*  53 */     if (createdCollection != null)
/*     */     {
/*  55 */       isUpToDate = createdCollection.booleanValue();
/*     */     }
/*  57 */     else if (isUpToDate)
/*     */     {
/*  59 */       String colId = CollectionHandlerUtils.getActiveIndex(false, this.m_collections);
/*  60 */       String buildDir = getCollectionPath(colId);
/*  61 */       String collectionStyleDir = calculateVerityCollectionStyleDirectory(buildDir);
/*  62 */       File f = new File(collectionStyleDir);
/*  63 */       isUpToDate = f.exists();
/*     */     }
/*  65 */     return isUpToDate;
/*     */   }
/*     */ 
/*     */   public String manageCollection(IndexerCollectionData def, IndexerWorkObject data)
/*     */     throws ServiceException
/*     */   {
/*  72 */     String colId = CollectionHandlerUtils.getActiveIndex(this.m_data.isRebuild(), this.m_collections);
/*  73 */     String buildDir = getCollectionPath(colId);
/*  74 */     Properties props = new Properties();
/*  75 */     props.put("configFileTemplate", this.m_config.getValue("ConfigFileTemplate"));
/*  76 */     props.put("zoneFileTemplate", this.m_config.getValue("ConfigZoneFileTemplate"));
/*  77 */     props.put("configStyleDir", this.m_styleDir);
/*  78 */     props.put("configDirectory", buildDir);
/*  79 */     props.put("sCollectionID", CollectionHandlerUtils.getCollectionID(this.m_data));
/*     */ 
/*  83 */     FileUtils.deleteDirectory(new File(buildDir), false);
/*     */ 
/*  86 */     File fromDir = new File(this.m_styleDir);
/*  87 */     File toDir = new File(buildDir + "/style");
/*     */ 
/*  89 */     FileUtils.copyDirectoryWithFlags(fromDir, toDir, 2, null, 1);
/*  90 */     createStyleFiles(props, def);
/*     */ 
/*  92 */     this.m_config.setValue("IndexerBuildCollectionDir", buildDir);
/*  93 */     DataResultSet drset = new DataResultSet(new String[] { "key", "value", "removeAfterWard" });
/*  94 */     Vector commandLine = VerityIndexUtils.prepareCommandLine("IndexerCollectionCreationCommandLine", true, drset, this.m_config);
/*  95 */     TaskInfo ti = new TaskInfo("Verity Collection Creation", commandLine, null);
/*  96 */     ti.m_timeout = 100000L;
/*  97 */     ti.m_traceSubject = "indexer";
/*  98 */     TaskMonitor.addToQueue(ti);
/*  99 */     synchronized (ti)
/*     */     {
/* 101 */       if (!ti.m_isFinished)
/*     */       {
/*     */         try
/*     */         {
/* 105 */           ti.wait();
/*     */         }
/*     */         catch (InterruptedException e)
/*     */         {
/* 109 */           Report.debug("indexer", null, e);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 114 */     String result = "DesignUpToDate";
/* 115 */     if ((ti.m_status == TaskInfo.STATUS.FAILURE) || (ti.m_status == TaskInfo.STATUS.TIMEOUT))
/*     */     {
/* 117 */       result = "Error";
/* 118 */       Report.error("indexer", ti.m_errMsg, ti.m_error);
/*     */     }
/* 122 */     else if (ti.m_hasError)
/*     */     {
/* 124 */       Report.warning("indexer", ti.m_errMsg, ti.m_error);
/*     */     }
/*     */ 
/* 128 */     data.setCachedObject("CollectionDesignUpToDate", Boolean.TRUE);
/* 129 */     return result;
/*     */   }
/*     */ 
/*     */   protected void createStyleFiles(Properties props, IndexerCollectionData collectionDef)
/*     */     throws ServiceException
/*     */   {
/* 138 */     String template = props.getProperty("configFileTemplate");
/* 139 */     if (template == null)
/*     */     {
/* 141 */       return;
/*     */     }
/*     */ 
/* 144 */     String buildDir = props.getProperty("configDirectory") + "style/";
/* 145 */     String styleDir = props.getProperty("configStyleDir");
/*     */ 
/* 148 */     File fromDir = new File(styleDir);
/* 149 */     File toDir = new File(buildDir);
/* 150 */     FileUtils.copyDirectoryWithFlags(fromDir, toDir, 2, null, 1);
/*     */ 
/* 153 */     createStyleFiles(buildDir, props, collectionDef);
/*     */   }
/*     */ 
/*     */   protected void createStyleFiles(String dir, Properties props, IndexerCollectionData collectionDef)
/*     */     throws ServiceException
/*     */   {
/* 159 */     String[] columns = { "fieldName", "fieldType", "hasDataTable", "isZone", "indexerID1", "indexerID2" };
/*     */ 
/* 161 */     DataResultSet fieldRows = new DataResultSet(columns);
/*     */ 
/* 163 */     Hashtable infos = collectionDef.m_fieldInfos;
/* 164 */     Hashtable designMap = collectionDef.m_fieldDesignMap;
/*     */     try
/*     */     {
/* 168 */       int m = 0;
/* 169 */       int j = 0;
/* 170 */       int i = 0;
/* 171 */       for (Enumeration e = infos.keys(); e.hasMoreElements(); ++i)
/*     */       {
/* 173 */         String name = (String)e.nextElement();
/*     */ 
/* 175 */         FieldInfo info = (FieldInfo)infos.get(name);
/*     */ 
/* 177 */         String type = "Text";
/* 178 */         switch (info.m_type)
/*     */         {
/*     */         case 3:
/* 181 */           type = "Int";
/* 182 */           break;
/*     */         case 5:
/* 184 */           type = "Date";
/*     */         }
/*     */ 
/* 187 */         Properties fieldProps = (Properties)designMap.get(name);
/* 188 */         addFieldRow(name, type, fieldProps, fieldRows, j, m);
/*     */ 
/* 190 */         ++j;
/* 191 */         if ((i <= 0) || (i % 25 != 0))
/*     */           continue;
/* 193 */         ++m;
/* 194 */         j = 0;
/*     */       }
/*     */ 
/* 199 */       DataResultSet securitySet = addSecurityFields(columns);
/*     */ 
/* 201 */       ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/* 202 */       cxt.setCachedObject("Indexer", this);
/*     */ 
/* 205 */       Properties config = this.m_config.m_currentConfig;
/* 206 */       Properties env = new Properties(config);
/* 207 */       Properties globalEnv = SharedObjects.getSecureEnvironment();
/* 208 */       for (Enumeration en = globalEnv.propertyNames(); en.hasMoreElements(); )
/*     */       {
/* 210 */         String key = (String)en.nextElement();
/* 211 */         String value = globalEnv.getProperty(key);
/* 212 */         env.put(key, value);
/*     */       }
/*     */ 
/* 215 */       DataBinder styleParams = new DataBinder(env);
/* 216 */       styleParams.setLocalData(props);
/*     */ 
/* 218 */       DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/* 219 */       String curEngine = this.m_config.m_currentEngineName;
/* 220 */       FieldInfo fi = new FieldInfo();
/* 221 */       searchEngines.getFieldInfo("seId", fi);
/* 222 */       if (searchEngines.findRow(fi.m_index, curEngine) != null)
/*     */       {
/* 224 */         styleParams.mergeResultSetRowIntoLocalData(searchEngines);
/*     */       }
/*     */ 
/* 227 */       styleParams.m_localData.putAll(this.m_config.m_currentConfig);
/*     */ 
/* 229 */       DynamicHtmlMerger htmlMerger = new PageMerger(styleParams, cxt);
/* 230 */       styleParams.addResultSet("IndexerFields", fieldRows);
/* 231 */       styleParams.addResultSet("SecurityFields", securitySet);
/*     */ 
/* 233 */       String template = props.getProperty("configFileTemplate");
/* 234 */       writeStyleFile(dir, "style.ufl", template, htmlMerger, styleParams, cxt);
/*     */ 
/* 236 */       template = props.getProperty("zoneFileTemplate");
/* 237 */       if (template != null)
/*     */       {
/* 239 */         writeStyleFile(dir, "style.dft", template, htmlMerger, styleParams, cxt);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 244 */       throw new ServiceException("!csIndexerUnableToCreateConfig", e);
/*     */     }
/*     */     catch (ParseSyntaxException e)
/*     */     {
/* 248 */       throw new ServiceException("!csIndexerUnableToCreateConfig", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void writeStyleFile(String dir, String filename, String template, DynamicHtmlMerger htmlMerger, DataBinder styleParams, ExecutionContext cxt)
/*     */     throws ServiceException, IOException, ParseSyntaxException
/*     */   {
/* 257 */     BufferedWriter bw = null;
/*     */     try
/*     */     {
/* 261 */       String path = dir + filename;
/* 262 */       if (FileUtils.checkFile(path, true, true) != -16)
/*     */       {
/* 264 */         FileUtils.deleteFile(path);
/*     */       }
/*     */ 
/* 267 */       DataLoader.checkCachedPage(template, cxt);
/* 268 */       DynamicHtml dynHtml = SharedObjects.getHtmlPage(template);
/* 269 */       String encoding = this.m_config.getValue("IndexerEncoding");
/* 270 */       OutputStream out = new FileOutputStream(path);
/* 271 */       bw = FileUtils.openDataWriterEx(out, encoding, 1);
/* 272 */       if (dynHtml != null);
/*     */       try
/*     */       {
/* 281 */         if (bw != null)
/*     */         {
/* 283 */           bw.flush();
/* 284 */           bw.close();
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 289 */         throw new ServiceException("!csIndexerUnableToCreateConfig", e);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/*     */       try
/*     */       {
/* 281 */         if (bw != null)
/*     */         {
/* 283 */           bw.flush();
/* 284 */           bw.close();
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 289 */         throw new ServiceException("!csIndexerUnableToCreateConfig", e);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addFieldRow(String name, String type, Properties fieldProps, DataResultSet drset, int j, int m)
/*     */   {
/* 298 */     boolean hasDataTable = false;
/* 299 */     boolean isZone = false;
/* 300 */     if (fieldProps != null)
/*     */     {
/* 302 */       hasDataTable = StringUtils.convertToBool(fieldProps.getProperty("hasDataTable"), false);
/* 303 */       isZone = StringUtils.convertToBool(fieldProps.getProperty("isZone"), false);
/*     */     }
/*     */ 
/* 307 */     Vector row = drset.createEmptyRow();
/*     */ 
/* 309 */     row.setElementAt(name, 0);
/* 310 */     row.setElementAt(type, 1);
/* 311 */     row.setElementAt("" + hasDataTable, 2);
/* 312 */     row.setElementAt("" + isZone, 3);
/* 313 */     String indexID1 = String.valueOf((char)(97 + m));
/* 314 */     row.setElementAt(indexID1, 4);
/* 315 */     String indexID2 = String.valueOf((char)(97 + j));
/* 316 */     row.setElementAt(indexID2, 5);
/*     */ 
/* 318 */     drset.addRow(row);
/*     */   }
/*     */ 
/*     */   protected DataResultSet addSecurityFields(String[] columns)
/*     */   {
/* 324 */     String str = SharedObjects.getEnvironmentValue("ZonedSecurityFields");
/* 325 */     Vector securityFields = StringUtils.parseArrayEx(str, ',', '^', true);
/*     */ 
/* 327 */     DataResultSet drset = new DataResultSet(columns);
/* 328 */     int num = securityFields.size();
/* 329 */     int m = 0;
/* 330 */     int j = 0;
/* 331 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 333 */       String name = (String)securityFields.elementAt(i);
/* 334 */       addFieldRow("z" + name, "Text", null, drset, j, m);
/*     */ 
/* 336 */       ++j;
/* 337 */       if ((i <= 0) || (i % 25 != 0))
/*     */         continue;
/* 339 */       ++m;
/* 340 */       j = 0;
/*     */     }
/*     */ 
/* 344 */     return drset;
/*     */   }
/*     */ 
/*     */   public void verifyCollection(IndexerCollectionData currentCollectionDef)
/*     */     throws ServiceException
/*     */   {
/* 350 */     IndexerCollectionData collectionDef = this.m_data.m_collectionDef;
/* 351 */     if (collectionDef == null)
/*     */     {
/* 354 */       collectionDef = new IndexerCollectionData();
/* 355 */       loadCollectionDesign(collectionDef);
/*     */     }
/*     */ 
/* 359 */     currentCollectionDef.shallowClone(collectionDef);
/*     */   }
/*     */ 
/*     */   public boolean loadCollectionDesign(IndexerCollectionData def)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 367 */       String colId = CollectionHandlerUtils.getActiveIndex(this.m_data.isRebuild(), this.m_collections);
/* 368 */       String buildDir = getCollectionPath(colId);
/* 369 */       String collectionStyleDir = calculateVerityCollectionStyleDirectory(buildDir);
/* 370 */       readUflStyleFile(collectionStyleDir, def);
/* 371 */       readDftStyleFile(collectionStyleDir, def);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 375 */       throw new ServiceException("!csIndexerUnableToVerify", e);
/*     */     }
/* 377 */     return true;
/*     */   }
/*     */ 
/*     */   protected void readUflStyleFile(String path, IndexerCollectionData collectionDef)
/*     */     throws IOException, ServiceException
/*     */   {
/* 386 */     Hashtable infoMap = new Hashtable();
/* 387 */     BufferedReader reader = null;
/* 388 */     File styleFile = new File(path + "style.ufl");
/*     */     try
/*     */     {
/*     */       try
/*     */       {
/* 393 */         reader = new BufferedReader(new FileReader(styleFile));
/*     */       }
/*     */       catch (FileNotFoundException e)
/*     */       {
/* 397 */         throw new ServiceException(-16, "!csIndexerUnableToParseStyleFile", e);
/*     */       }
/*     */ 
/* 401 */       int state = 0;
/* 402 */       StringBuffer buffer = null;
/* 403 */       boolean isComment = false;
/* 404 */       String thisCollectionSearchString = "VDK4";
/* 405 */       boolean encodeVdkKey = false;
/* 406 */       String dataTable = null;
/*     */       while (true)
/*     */       {
/* 409 */         int c = reader.read();
/*     */ 
/* 411 */         if (c == -1) {
/*     */           break;
/*     */         }
/*     */ 
/* 415 */         switch (state)
/*     */         {
/*     */         case 0:
/* 418 */           if ((c == 123) || (c == 35))
/*     */           {
/* 420 */             buffer = new StringBuffer();
/* 421 */             state = 1;
/* 422 */             if (c == 35)
/*     */             {
/* 424 */               isComment = true;
/*     */             }
/*     */             else
/*     */             {
/* 428 */               isComment = false;
/*     */             }
/*     */           }
/* 431 */           else if ((c != 10) && (c != 13) && (c != 9))
/*     */           {
/* 433 */             buffer = new StringBuffer();
/* 434 */             buffer.append((char)c);
/* 435 */             state = 2; } break;
/*     */         case 1:
/* 439 */           if (c == 125)
/*     */           {
/* 441 */             state = 0;
/*     */ 
/* 443 */             Vector fieldInfoArray = parseFieldInfoArray(buffer.toString());
/*     */ 
/* 448 */             buildStructure(dataTable, fieldInfoArray, collectionDef);
/*     */           }
/* 450 */           else if ((isComment) && (((c == 10) || (c == 13) || (c == 9))))
/*     */           {
/* 452 */             state = 0;
/* 453 */             isComment = false;
/* 454 */             String tempBuf = buffer.toString();
/* 455 */             if (tempBuf.indexOf("SearchCompatibilityVersion") >= 0)
/*     */             {
/* 457 */               int index = tempBuf.indexOf(61);
/* 458 */               if (index > 0)
/*     */               {
/* 460 */                 thisCollectionSearchString = tempBuf.substring(index + 1);
/*     */               }
/*     */             }
/* 463 */             else if (tempBuf.indexOf("EncodeVdkKey") >= 0)
/*     */             {
/* 465 */               int index = tempBuf.indexOf(61);
/* 466 */               if ((((index > 0) ? 1 : 0) & ((tempBuf.indexOf("true", index) > 0) ? 1 : 0)) != 0)
/*     */               {
/* 468 */                 encodeVdkKey = true;
/*     */               }
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 474 */             buffer.append((char)c);
/*     */           }
/* 476 */           break;
/*     */         case 2:
/* 479 */           if ((c == 10) || (c == 13) || (c == 9))
/*     */           {
/* 481 */             state = 0;
/* 482 */             dataTable = parseDataTable(buffer.toString());
/*     */           }
/*     */           else
/*     */           {
/* 486 */             buffer.append((char)c);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 493 */       String currentSearchEngine = SharedObjects.getEnvironmentValue("SearchEngineName");
/* 494 */       if (currentSearchEngine == null)
/*     */       {
/* 496 */         currentSearchEngine = SharedObjects.getEnvironmentValue("SearchIndexerEngineName");
/*     */       }
/*     */ 
/* 499 */       DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/* 500 */       FieldInfo[] fis = null;
/*     */       try
/*     */       {
/* 503 */         fis = ResultSetUtils.createInfoList(searchEngines, new String[] { "seId", "seIndexCompatibility", "seSearchCompatibility" }, true);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 508 */         throw new ServiceException(e);
/*     */       }
/*     */ 
/* 511 */       if (searchEngines.findRow(fis[0].m_index, currentSearchEngine) != null)
/*     */       {
/* 513 */         String currentSearchCompat = searchEngines.getStringValue(fis[2].m_index);
/* 514 */         if (!thisCollectionSearchString.equals(currentSearchCompat))
/*     */         {
/* 516 */           String msg = LocaleUtils.encodeMessage("csVerifyCollectionErrorCurrentSearchIncompatible", null, thisCollectionSearchString);
/* 517 */           msg = LocaleResources.localizeMessage(msg, null);
/* 518 */           SystemUtils.trace("indexer", msg);
/* 519 */           SystemUtils.err(null, msg);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 524 */         String msg = LocaleUtils.encodeMessage("csVerifyCollectionErrorInvalidSearchEngine", null, currentSearchEngine);
/* 525 */         msg = LocaleResources.localizeMessage(msg, null);
/* 526 */         SystemUtils.trace("indexer", msg);
/* 527 */         SystemUtils.err(null, msg);
/*     */       }
/*     */ 
/* 530 */       FieldInfo urlInfo = new FieldInfo();
/* 531 */       urlInfo.m_type = 6;
/* 532 */       infoMap.put("URL", urlInfo);
/*     */ 
/* 534 */       intradoc.server.SearchLoader.m_encodeVdkKeyForSearch = encodeVdkKey;
/*     */     }
/*     */     finally
/*     */     {
/* 538 */       FileUtils.closeObject(reader);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Vector parseFieldInfoArray(String text) throws ServiceException
/*     */   {
/* 544 */     Vector fiArr = new IdcVector();
/* 545 */     Vector tempV = StringUtils.parseArray(text, '\n', '^');
/* 546 */     for (int i = 0; i < tempV.size(); ++i)
/*     */     {
/* 548 */       String temp = (String)tempV.elementAt(i);
/* 549 */       if ((temp == null) || ((temp = temp.trim()).length() <= 0) || 
/* 551 */         (temp.indexOf(61) >= 0))
/*     */         continue;
/* 553 */       FieldInfo info = new FieldInfo();
/* 554 */       parseFieldInfo(info, temp);
/* 555 */       fiArr.addElement(info);
/*     */     }
/*     */ 
/* 560 */     return fiArr;
/*     */   }
/*     */ 
/*     */   protected void parseFieldInfo(FieldInfo info, String text) throws ServiceException
/*     */   {
/* 565 */     String[] elements = parseFieldData(text);
/* 566 */     info.m_name = elements[1];
/* 567 */     if (elements[0].equals("fixwidth:"))
/*     */     {
/* 569 */       if (elements[3].equals("signed-integer"))
/*     */       {
/* 571 */         info.m_type = 3;
/*     */       }
/*     */       else
/*     */       {
/* 575 */         info.m_type = 5;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/* 580 */       info.m_type = 6;
/*     */   }
/*     */ 
/*     */   protected String parseDataTable(String str)
/*     */   {
/* 586 */     String[] elements = parseFieldData(str);
/* 587 */     if ((elements[0] != null) && (elements[0].equals("data-table:")))
/*     */     {
/* 589 */       return elements[1];
/*     */     }
/* 591 */     return null;
/*     */   }
/*     */ 
/*     */   protected String[] parseFieldData(String text)
/*     */   {
/* 597 */     String[] elements = new String[10];
/* 598 */     int length = text.length();
/* 599 */     int state = 0;
/* 600 */     StringBuffer buffer = null;
/* 601 */     int count = 0;
/* 602 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 604 */       char c = text.charAt(i);
/* 605 */       switch (state)
/*     */       {
/*     */       case 0:
/* 608 */         if (Character.isWhitespace(c))
/*     */           continue;
/* 610 */         buffer = new StringBuffer();
/* 611 */         buffer.append(c);
/* 612 */         state = 1; break;
/*     */       case 1:
/* 616 */         if (!Character.isWhitespace(c))
/*     */         {
/* 618 */           buffer.append(c);
/*     */         }
/*     */         else
/*     */         {
/* 622 */           elements[(count++)] = buffer.toString();
/* 623 */           state = 0;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 629 */     if (buffer.length() > 0)
/*     */     {
/* 631 */       elements[(count++)] = buffer.toString();
/*     */     }
/*     */ 
/* 634 */     return elements;
/*     */   }
/*     */ 
/*     */   protected void buildStructure(String dataTable, Vector fieldInfoList, IndexerCollectionData collectionDef)
/*     */   {
/* 640 */     int size = fieldInfoList.size();
/* 641 */     boolean hasDataTable = false;
/* 642 */     if (size == 1)
/*     */     {
/* 644 */       hasDataTable = true;
/*     */     }
/* 646 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 648 */       FieldInfo info = (FieldInfo)fieldInfoList.elementAt(i);
/* 649 */       if (info.m_name.startsWith("z"))
/*     */       {
/* 652 */         info.m_name = info.m_name.substring(1);
/* 653 */         collectionDef.m_securityInfos.put(info.m_name, info);
/*     */       }
/*     */       else
/*     */       {
/* 657 */         collectionDef.m_fieldInfos.put(info.m_name, info);
/*     */       }
/*     */ 
/* 660 */       Properties props = new Properties();
/* 661 */       collectionDef.m_fieldDesignMap.put(info.m_name, props);
/* 662 */       if (!hasDataTable)
/*     */         continue;
/* 664 */       props.put("hasDataTable", "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void readDftStyleFile(String path, IndexerCollectionData collectionDef)
/*     */     throws IOException, ServiceException
/*     */   {
/* 675 */     BufferedReader reader = null;
/* 676 */     File styleFile = new File(path + "style.dft");
/*     */     try
/*     */     {
/*     */       try
/*     */       {
/* 681 */         reader = new BufferedReader(new FileReader(styleFile));
/*     */       }
/*     */       catch (FileNotFoundException e)
/*     */       {
/* 685 */         throw new ServiceException(-16, "!csIndexerUnableToParseSecurityStyleFile", e);
/*     */       }
/*     */ 
/* 703 */       int state = 0;
/* 704 */       StringBuffer buffer = null;
/*     */       while (true)
/*     */       {
/* 707 */         int c = reader.read();
/*     */ 
/* 709 */         if (c == -1) {
/*     */           break;
/*     */         }
/*     */ 
/* 713 */         switch (state)
/*     */         {
/*     */         case 0:
/* 716 */           if (c == 123)
/*     */           {
/* 718 */             buffer = new StringBuffer();
/* 719 */             state = 1; } break;
/*     */         case 1:
/* 723 */           if (c == 125)
/*     */           {
/* 725 */             state = 0;
/*     */ 
/* 728 */             parseZoneFields(buffer.toString(), collectionDef);
/*     */           }
/*     */           else
/*     */           {
/* 732 */             buffer.append((char)c);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 740 */       FileUtils.closeObject(reader);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void parseZoneFields(String str, IndexerCollectionData collectionDef)
/*     */   {
/* 746 */     Vector tempV = StringUtils.parseArray(str, '\n', '^');
/*     */ 
/* 748 */     Hashtable securityInfos = collectionDef.m_securityInfos;
/* 749 */     Hashtable fieldDesigns = collectionDef.m_fieldDesignMap;
/* 750 */     String fieldName = null;
/* 751 */     Properties props = null;
/* 752 */     for (int i = 0; i < tempV.size(); ++i)
/*     */     {
/* 754 */       String temp = (String)tempV.elementAt(i);
/* 755 */       if ((temp == null) || ((temp = temp.trim()).length() <= 0)) {
/*     */         continue;
/*     */       }
/* 758 */       String[] data = parseFieldData(temp);
/* 759 */       if (data[0].equals("field:"))
/*     */       {
/* 761 */         if (fieldName != null)
/*     */         {
/* 763 */           addZoneField(fieldName, props, securityInfos, fieldDesigns);
/*     */         }
/* 765 */         fieldName = data[1];
/* 766 */         props = new Properties();
/*     */       } else {
/* 768 */         if (fieldName == null)
/*     */           continue;
/* 770 */         String val = data[0];
/* 771 */         int index = val.indexOf(61);
/* 772 */         if (index < 0)
/*     */           continue;
/* 774 */         props.put(val.substring(0, index), val.substring(index + 1));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 780 */     if (fieldName == null)
/*     */       return;
/* 782 */     addZoneField(fieldName, props, securityInfos, fieldDesigns);
/*     */   }
/*     */ 
/*     */   protected void addZoneField(String fieldName, Properties props, Hashtable securityInfos, Hashtable fieldDesigns)
/*     */   {
/* 788 */     String zone = props.getProperty("/zone");
/* 789 */     if (zone == null)
/*     */       return;
/* 791 */     if (fieldName.startsWith("z"))
/*     */     {
/* 793 */       fieldName = fieldName.substring(1);
/* 794 */       FieldInfo info = new FieldInfo();
/* 795 */       info.m_name = fieldName;
/* 796 */       securityInfos.put(fieldName, info);
/*     */     }
/*     */     else
/*     */     {
/* 800 */       Properties fieldProps = (Properties)fieldDesigns.get(fieldName);
/* 801 */       if (fieldProps == null)
/*     */       {
/* 803 */         SystemUtils.trace(null, "VerityHandler.addZoneField: The zone field " + fieldName + " does not exist in the ufl file.");
/*     */       }
/*     */       else
/*     */       {
/* 808 */         fieldProps.put("isZone", "1");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void validateConfiguration()
/*     */     throws ServiceException
/*     */   {
/* 817 */     FileUtils.validatePath(this.m_config.getValue("IndexerStyleDir"), IdcMessageFactory.lc("csIndexerStyleDirError", new Object[0]), 0);
/*     */ 
/* 820 */     FileUtils.checkOrCreateDirectory(this.m_config.getValue("IndexerBulkloadDir"), 0);
/*     */ 
/* 823 */     FileUtils.validatePath(this.m_config.getValue("IndexerNoTextFile"), IdcMessageFactory.lc("csIndexerErrorWithDefaultFile", new Object[0]), 1);
/*     */ 
/* 826 */     FileUtils.validatePath(this.m_config.getValue("IndexerPath"), IdcMessageFactory.lc("csIndexerPathError", new Object[0]), 1);
/*     */ 
/* 829 */     String skipText = null;
/* 830 */     String docCountPreText = null;
/* 831 */     String docCountPostText = null;
/* 832 */     String tmp = SharedObjects.getEnvironmentValue("VeritySkipText");
/* 833 */     if ((tmp != null) && (tmp.length() > 0))
/*     */     {
/* 835 */       skipText = tmp;
/*     */     }
/* 837 */     tmp = SharedObjects.getEnvironmentValue("VerityDocCountPrefixText");
/* 838 */     if ((tmp != null) && (tmp.length() > 0))
/*     */     {
/* 840 */       docCountPreText = tmp;
/*     */     }
/* 842 */     tmp = SharedObjects.getEnvironmentValue("VerityDocCountSuffixText");
/* 843 */     if ((tmp != null) && (tmp.length() > 0))
/*     */     {
/* 845 */       docCountPostText = tmp;
/*     */     }
/*     */ 
/* 848 */     if ((skipText != null) && (skipText.length() != 0) && (docCountPreText != null) && (docCountPreText.length() != 0) && (docCountPostText != null) && (docCountPostText.length() != 0)) {
/*     */       return;
/*     */     }
/* 851 */     DataResultSet textConfig = SharedObjects.getTable("VerityContextText");
/* 852 */     FieldInfo[] infos = null;
/* 853 */     if (textConfig != null)
/*     */     {
/*     */       try
/*     */       {
/* 857 */         infos = ResultSetUtils.createInfoList(textConfig, new String[] { "lcSearchLocale", "lcVeritySkipText", "lcVerityCountPreText", "lcVerityCountPostText" }, true);
/*     */       }
/*     */       catch (DataException ignore)
/*     */       {
/* 863 */         SystemUtils.trace(null, "The VerityContextText table is misconfigured.");
/*     */       }
/*     */     }
/* 866 */     if (infos == null)
/*     */       return;
/* 868 */     String locale = this.m_config.getValue("IndexerLocale");
/* 869 */     Vector v = textConfig.findRow(infos[0].m_index, locale);
/* 870 */     if (v == null)
/*     */       return;
/* 872 */     if ((skipText == null) || (skipText.length() == 0))
/*     */     {
/* 874 */       skipText = (String)v.elementAt(1);
/* 875 */       this.m_config.setValue("VeritySkipText", skipText);
/*     */     }
/*     */ 
/* 878 */     if ((docCountPreText == null) || (docCountPreText.length() == 0))
/*     */     {
/* 880 */       docCountPreText = (String)v.elementAt(2);
/* 881 */       this.m_config.setValue("VerityDocCountPrefixText", docCountPreText);
/*     */     }
/*     */ 
/* 884 */     if ((docCountPostText != null) && (docCountPostText.length() != 0))
/*     */       return;
/* 886 */     docCountPostText = (String)v.elementAt(3);
/* 887 */     this.m_config.setValue("VerityDocCountSuffixText", docCountPostText);
/*     */   }
/*     */ 
/*     */   public String getCollectionPath(String id)
/*     */     throws ServiceException
/*     */   {
/* 898 */     String searchDir = this.m_data.m_searchDir;
/* 899 */     String path = null;
/*     */     try
/*     */     {
/* 902 */       path = ResultSetUtils.findValue(this.m_collections, "IndexerLabel", id, "IndexerExtension");
/* 903 */       if (path == null)
/*     */       {
/* 906 */         String engineName = this.m_config.getValue("SearchIndexerEngineName");
/* 907 */         if ((!this.m_config.getBoolean("hasWarnedActiveIndexNotValid", false)) && ((
/* 910 */           (engineName == null) || ((!engineName.equalsIgnoreCase("DATABASE")) && (!engineName.equalsIgnoreCase("DATABASE.METADATA"))))))
/*     */         {
/* 913 */           SystemUtils.trace("indexer", "Warning: ActiveIndex '" + id + "' is not valid in indexer engine '" + engineName + "'. Please rebuild your collection");
/*     */ 
/* 916 */           String msg = LocaleUtils.encodeMessage("csActiveIndexNotValidInEngine", null, id, engineName);
/* 917 */           SystemUtils.reportError(1, msg);
/* 918 */           this.m_config.m_currentConfig.put("hasWarnedActiveIndexNotValid", "true");
/*     */         }
/*     */ 
/* 921 */         this.m_collections.first();
/* 922 */         FieldInfo fi = new FieldInfo();
/* 923 */         this.m_collections.getFieldInfo("IndexerExtension", fi);
/* 924 */         path = this.m_collections.getStringValue(fi.m_index);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 929 */       SystemUtils.dumpException("indexer", e);
/* 930 */       path = id;
/*     */     }
/*     */ 
/* 933 */     if (path == null)
/*     */     {
/* 935 */       if (FileUtils.checkFile(id, false, true) == 0)
/*     */       {
/* 939 */         String pathName = FileUtils.directorySlashes(id); break label304:
/*     */       }
/*     */ 
/* 943 */       SystemUtils.trace("indexer", "unable to find the collection directory for '" + id + "'.");
/*     */ 
/* 946 */       String msg = LocaleUtils.encodeMessage("csIndexerUnableToFindCollection", null, id);
/*     */ 
/* 948 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 953 */     String pathName = searchDir + path;
/*     */ 
/* 955 */     label304: return FileUtils.directorySlashes(pathName);
/*     */   }
/*     */ 
/*     */   public String calculateVerityCollectionStyleDirectory(String buildDir)
/*     */   {
/* 970 */     return buildDir + "intradocbasic/style/";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 975 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.VerityCollectionHandler
 * JD-Core Version:    0.5.4
 */