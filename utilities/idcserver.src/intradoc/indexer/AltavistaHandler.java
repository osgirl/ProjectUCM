/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcLocale;
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
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.ActiveIndexState;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class AltavistaHandler extends IndexerExecutionHandler
/*     */ {
/*     */   protected Vector m_fieldList;
/*     */   protected Hashtable m_fieldInfos;
/*     */   protected DataResultSet m_drset;
/*     */   protected Vector m_extraFields;
/*     */ 
/*     */   public AltavistaHandler()
/*     */   {
/*  39 */     this.m_fieldList = new IdcVector();
/*     */ 
/*  43 */     this.m_extraFields = new IdcVector();
/*     */   }
/*     */ 
/*     */   public void init(IndexerExecution exec)
/*     */     throws ServiceException
/*     */   {
/*  49 */     if (EnvUtils.isFamily("windows"))
/*     */     {
/*  51 */       exec.m_maintainIndexProcess = true;
/*     */     }
/*     */ 
/*  54 */     super.init(exec);
/*     */ 
/*  59 */     IndexerCollectionData collectionDef = this.m_data.m_collectionDef;
/*  60 */     this.m_fieldInfos = ((Hashtable)collectionDef.m_fieldInfos.clone());
/*     */ 
/*  62 */     Hashtable fis = collectionDef.m_fieldInfos;
/*  63 */     for (Enumeration en = fis.keys(); en.hasMoreElements(); )
/*     */     {
/*  65 */       String fieldName = (String)en.nextElement();
/*  66 */       this.m_fieldList.addElement(fieldName);
/*     */     }
/*  68 */     this.m_drset = this.m_config.getTable("AvsExtraFields");
/*     */ 
/*  70 */     for (this.m_drset.first(); this.m_drset.isRowPresent(); this.m_drset.next())
/*     */     {
/*  72 */       String name = ResultSetUtils.getValue(this.m_drset, "fieldName");
/*  73 */       String type = ResultSetUtils.getValue(this.m_drset, "fieldType");
/*  74 */       this.m_fieldList.addElement(name);
/*  75 */       this.m_extraFields.addElement(name);
/*  76 */       FieldInfo fi = new FieldInfo();
/*  77 */       fi.m_name = name;
/*  78 */       if (type.equalsIgnoreCase("int"))
/*     */       {
/*  80 */         fi.m_type = 3;
/*     */       }
/*  82 */       else if (type.equalsIgnoreCase("memo"))
/*     */       {
/*  84 */         fi.m_type = 8;
/*     */       }
/*  86 */       this.m_fieldInfos.put(name, fi);
/*     */     }
/*     */ 
/*  90 */     String installPath = this.m_data.getEnvironmentValue("AvsInstallDir");
/*     */ 
/*  93 */     if ((installPath == null) || (installPath.length() == 0))
/*     */     {
/*  95 */       installPath = this.m_data.m_sharedDir + "search/avs/";
/*  96 */       SharedObjects.putEnvironmentValue("AvsInstallDir", installPath);
/*     */     }
/*  98 */     String indexerPath = installPath + EnvUtils.getOSName() + "/avs" + EnvUtils.getExecutableFileSuffix();
/*     */ 
/* 100 */     this.m_config.setValue("IndexerPath", indexerPath);
/* 101 */     this.m_config.setValue("IndexerEOD", "\n<<EOD>>");
/*     */ 
/* 103 */     createLinguisticsConfigFile(installPath + "linguistics/segmenter_config.txt", "AVS_LANG_SEG_CONF_FILE", "csAvsLangSegConfFileError", installPath);
/*     */ 
/* 105 */     createLinguisticsConfigFile(installPath + "linguistics/stem_config.txt", "AVS_STEM_CONF_FILE", "csAvsStemConfFileError", installPath);
/*     */ 
/* 107 */     createLinguisticsConfigFile(installPath + "linguistics/spell_config.txt", "AVS_SPELL_CONF_FILE", "csAvsSpellConfFileError", installPath);
/*     */ 
/* 109 */     createLinguisticsConfigFile(installPath + "linguistics/thesaurus_config.txt", "AVS_THESAURUS_CONF_FILE", "csAvsThesaurusConfFileError", installPath);
/*     */   }
/*     */ 
/*     */   protected void createLinguisticsConfigFile(String path, String template, String errStr, String installPath)
/*     */   {
/* 115 */     File file = new File(path);
/* 116 */     BufferedWriter bw = null;
/* 117 */     DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/*     */ 
/* 119 */     DataBinder styleParams = new DataBinder(SharedObjects.getSecureEnvironment());
/* 120 */     if (localeConfig != null)
/*     */     {
/* 122 */       styleParams.addResultSet("LocaleConfig", localeConfig);
/*     */     }
/* 124 */     styleParams.putLocal("AvsInstallDir", installPath);
/*     */ 
/* 126 */     DynamicHtmlMerger htmlMerger = new PageMerger(styleParams, null);
/*     */ 
/* 128 */     DynamicHtml dynHtml = SharedObjects.getHtmlPage(template);
/*     */     try
/*     */     {
/* 133 */       bw = FileUtils.openDataWriter(file);
/* 134 */       dynHtml.outputHtml(bw, htmlMerger);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 138 */       Report.error(null, errStr, e);
/* 139 */       e.printStackTrace();
/*     */     }
/*     */     finally
/*     */     {
/*     */       try
/*     */       {
/* 145 */         if (bw != null)
/*     */         {
/* 147 */           bw.flush();
/* 148 */           bw.close();
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 153 */         Report.error(null, errStr, e);
/* 154 */         e.printStackTrace();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void writeBatchFile(Vector list, Hashtable docProps)
/*     */     throws ServiceException
/*     */   {
/* 162 */     DataBinder binder = new DataBinder();
/*     */ 
/* 164 */     SimpleDateFormat frmt = new SimpleDateFormat("Hmmss");
/* 165 */     Date dte = new Date();
/* 166 */     String tstamp = frmt.format(dte);
/* 167 */     String dir = this.m_execution.m_bulkDir;
/* 168 */     String bulkloadFile = dir + tstamp + ".hda";
/*     */ 
/* 171 */     String[] fieldListArray = StringUtils.convertListToArray(this.m_fieldList);
/* 172 */     IndexerInfo ii = (IndexerInfo)list.elementAt(0);
/* 173 */     if (ii.m_isDelete)
/*     */     {
/* 175 */       fieldListArray = new String[] { "VdkVgwKey" };
/*     */     }
/*     */ 
/* 180 */     this.m_execution.m_bulkLoadSize = 0L;
/*     */ 
/* 183 */     DataResultSet documents = new DataResultSet(fieldListArray);
/* 184 */     binder.addResultSet("AvsDocuments", documents);
/*     */ 
/* 187 */     int len = list.size();
/* 188 */     for (int j = 0; j < len; ++j)
/*     */     {
/* 190 */       ii = (IndexerInfo)list.elementAt(j);
/* 191 */       Properties prop = (Properties)docProps.get(ii.m_indexKey);
/*     */ 
/* 193 */       DataResultSet drset = ResultSetUtils.createResultSetFromProperties(prop, fieldListArray);
/*     */       try
/*     */       {
/* 196 */         documents.merge(null, drset, false);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 200 */         Report.trace("indexer", null, e);
/*     */       }
/* 202 */       this.m_execution.m_bulkLoadSize += ii.m_size;
/*     */     }
/* 204 */     dir = FileUtils.directorySlashes(dir);
/* 205 */     File tempFile = new File(dir + "__temp.dat");
/* 206 */     File dataFile = new File(bulkloadFile);
/*     */ 
/* 208 */     String enc = this.m_config.getValue("IndexerEncoding");
/*     */     try
/*     */     {
/* 211 */       Writer writer = FileUtils.openDataWriter(tempFile, enc);
/* 212 */       binder.send(writer);
/* 213 */       writer.close();
/*     */ 
/* 215 */       if (dataFile.exists())
/*     */       {
/* 217 */         dataFile.delete();
/*     */ 
/* 219 */         if (dataFile.exists())
/*     */         {
/* 221 */           throw new ServiceException(-18, LocaleUtils.encodeMessage("csResourceUtilsFileWriteError", null, dataFile.getAbsolutePath()));
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 227 */       tempFile.renameTo(dataFile);
/* 228 */       this.m_config.setValue("IndexerBulkloadFile", bulkloadFile);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 232 */       throw new ServiceException(-18, "!csResourceUtilsFileIOError", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void prepareIndexDoc(Properties props, IndexerInfo ii)
/*     */   {
/* 240 */     if (props.get("DataProcessed") != null)
/*     */     {
/* 242 */       return;
/*     */     }
/* 244 */     String webFileSize = props.getProperty("WebFileSize");
/* 245 */     String altFileSize = props.getProperty("AlternateFileSize");
/* 246 */     if (webFileSize == null)
/*     */     {
/* 248 */       webFileSize = "";
/*     */     }
/*     */ 
/* 251 */     if (altFileSize == null)
/*     */     {
/* 253 */       altFileSize = "";
/*     */     }
/* 255 */     props.put("WebSize", webFileSize);
/* 256 */     props.put("AltSize", altFileSize);
/*     */ 
/* 258 */     String encoding = this.m_config.getValue("VerityEncoding");
/* 259 */     String langMetaField = this.m_config.getValue("LanguageMetaFieldName");
/* 260 */     if ((encoding != null) && (encoding.equalsIgnoreCase("utf8")))
/*     */     {
/* 262 */       IdcLocale locale = null;
/* 263 */       String language = null;
/* 264 */       String tempLocale = null;
/*     */ 
/* 267 */       if ((langMetaField != null) && (langMetaField.length() > 0))
/*     */       {
/* 269 */         tempLocale = props.getProperty(langMetaField);
/* 270 */         if ((tempLocale != null) && (tempLocale.length() > 0))
/*     */         {
/* 272 */           locale = LocaleResources.getLocale(tempLocale);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 278 */         String docAuthor = props.getProperty("dDocAuthor");
/*     */         try
/*     */         {
/* 281 */           if ((docAuthor != null) && (docAuthor.length() > 0))
/*     */           {
/* 283 */             Properties args = new Properties();
/* 284 */             PropParameters params = new PropParameters(args);
/* 285 */             args.put("dName", docAuthor);
/* 286 */             ResultSet rset = this.m_data.m_workspace.createResultSet("QuserLocale", params);
/* 287 */             tempLocale = ResultSetUtils.getValue(rset, "dUserLocale");
/* 288 */             if ((tempLocale != null) && (tempLocale.length() > 0))
/*     */             {
/* 290 */               locale = LocaleResources.getLocale(tempLocale);
/*     */             }
/*     */           }
/*     */         }
/*     */         catch (DataException ignore)
/*     */         {
/* 296 */           if (SystemUtils.m_verbose)
/*     */           {
/* 298 */             Report.debug("indexer", null, ignore);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 304 */       if (locale == null)
/*     */       {
/* 306 */         locale = LocaleResources.getSystemLocale();
/*     */       }
/*     */ 
/* 309 */       language = locale.m_languageId;
/* 310 */       props.put("AvsUserLocale", language);
/*     */     }
/* 312 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*     */     {
/* 314 */       String key = (String)en.nextElement();
/* 315 */       String value = (String)props.get(key);
/* 316 */       FieldInfo fi = (FieldInfo)this.m_fieldInfos.get(key);
/* 317 */       if (fi == null) continue; if (this.m_extraFields.contains(fi.m_name)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 321 */       if (value.length() == 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 325 */       if (fi.m_type == 5)
/*     */       {
/*     */         try
/*     */         {
/* 329 */           value = fixDate(value);
/* 330 */           long l = Long.parseLong(value) / 1000L;
/* 331 */           value = "<docdata>" + value + "</docdata>" + l;
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 335 */           Report.warning(null, e, "csAvsFixDateError", new Object[] { value });
/*     */         }
/*     */       }
/* 338 */       else if (fi.m_type == 6)
/*     */       {
/* 340 */         value = "<docdata>" + value + "</docdata>" + value;
/*     */       }
/*     */ 
/* 343 */       props.put(key, value);
/*     */     }
/* 345 */     props.put("DataProcessed", "1");
/*     */   }
/*     */ 
/*     */   public int prepare(Hashtable hash, Properties props)
/*     */     throws ServiceException
/*     */   {
/* 353 */     String path = (String)hash.get("configFilePath");
/*     */ 
/* 355 */     if (path == null)
/*     */     {
/* 357 */       String activeIndex = ActiveIndexState.getActiveProperty("ActiveIndex");
/* 358 */       if (activeIndex == null)
/*     */       {
/* 360 */         activeIndex = this.m_execution.getNextIndexerID(activeIndex);
/*     */       }
/* 362 */       String activeCollectionDir = this.m_execution.getCollectionPath(activeIndex);
/*     */ 
/* 364 */       path = activeCollectionDir + "intradocbasic/avsida.hda";
/*     */     }
/* 366 */     if (FileUtilsCfgBuilder.getCfgFile(path, null, false).exists())
/*     */     {
/* 369 */       DataBinder binder = new DataBinder();
/* 370 */       ResourceUtils.serializeDataBinder(FileUtils.getDirectory(path), FileUtils.getName(path), binder, false, true);
/*     */ 
/* 373 */       DataResultSet fieldsMap = (DataResultSet)binder.getResultSet("IndexerFieldsMap");
/* 374 */       if (fieldsMap != null)
/*     */       {
/* 376 */         SharedObjects.putTable("IndexerFieldsMap", fieldsMap);
/*     */       }
/*     */     }
/* 379 */     return 1;
/*     */   }
/*     */ 
/*     */   public void executeIndexer(Vector list, Hashtable props)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void createCollection(String collectionID)
/*     */     throws ServiceException
/*     */   {
/* 391 */     Properties props = new Properties();
/* 392 */     props.put("configFileTemplate", "AVS_METAFIELDS_STYLE_FILE");
/* 393 */     props.put("configFilePath", this.m_config.getScriptValue("AvsConfigPath"));
/* 394 */     props.put("sCollectionID", this.m_execution.getCollectionID());
/* 395 */     this.m_data.m_updateStyleFile = false;
/*     */ 
/* 397 */     createStyleFile(props, this.m_data.m_collectionDef);
/*     */   }
/*     */ 
/*     */   public void createStyleFile(Properties props, IndexerCollectionData collectionDef)
/*     */     throws ServiceException
/*     */   {
/* 403 */     String template = props.getProperty("configFileTemplate");
/* 404 */     String path = props.getProperty("configFilePath");
/*     */ 
/* 406 */     if ((template == null) || (path == null))
/*     */     {
/* 408 */       return;
/*     */     }
/*     */ 
/* 411 */     createAvsIdaFile(props, collectionDef, template, path);
/*     */   }
/*     */ 
/*     */   protected void createAvsIdaFile(Properties props, IndexerCollectionData collectionDef, String template, String path)
/*     */     throws ServiceException
/*     */   {
/* 417 */     FileUtils.checkOrCreateDirectory(FileUtils.getDirectory(path), 2);
/* 418 */     if (FileUtils.checkFile(path, true, true) != -16)
/*     */     {
/* 420 */       FileUtils.deleteFile(path);
/*     */     }
/*     */ 
/* 423 */     String cvtTempDir = this.m_config.getScriptValue("AvsCvtTempDir");
/*     */ 
/* 425 */     FileUtils.checkOrCreateDirectory(FileUtils.getDirectory(cvtTempDir), 2);
/* 426 */     SharedObjects.putEnvironmentValue("CvtTempDir", cvtTempDir);
/*     */ 
/* 428 */     Hashtable infos = collectionDef.m_fieldInfos;
/* 429 */     DataResultSet fieldRows = new DataResultSet(new String[] { "fieldName", "fieldType", "addToDocData" });
/* 430 */     DataResultSet extraFields = this.m_drset;
/* 431 */     BufferedWriter bw = null;
/*     */     try
/*     */     {
/* 434 */       int i = 0;
/* 435 */       Vector docData = this.m_config.getVector("AvsDocData");
/*     */ 
/* 437 */       for (Enumeration e = infos.keys(); e.hasMoreElements(); ++i)
/*     */       {
/* 439 */         Vector row = fieldRows.createEmptyRow();
/* 440 */         String name = (String)e.nextElement();
/* 441 */         row.setElementAt(name, 0);
/*     */ 
/* 443 */         FieldInfo info = (FieldInfo)infos.get(name);
/*     */ 
/* 445 */         String type = "Text";
/* 446 */         switch (info.m_type)
/*     */         {
/*     */         case 3:
/* 449 */           type = "Int";
/* 450 */           break;
/*     */         case 5:
/* 452 */           type = "Date";
/* 453 */           break;
/*     */         case 8:
/* 455 */           type = "Memo";
/*     */         }
/*     */ 
/* 458 */         row.setElementAt(type, 1);
/*     */ 
/* 460 */         if ((docData != null) && (!docData.contains(name)))
/*     */         {
/* 462 */           row.setElementAt("0", 2);
/*     */         }
/*     */         else
/*     */         {
/* 466 */           row.setElementAt("1", 2);
/*     */         }
/* 468 */         fieldRows.addRow(row);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 474 */         fieldRows.merge(null, extraFields, false);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 478 */         Report.trace("indexer", null, e);
/*     */       }
/*     */ 
/* 481 */       ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/* 482 */       cxt.setCachedObject("Indexer", this);
/* 483 */       DataBinder styleParams = new DataBinder(SharedObjects.getSecureEnvironment());
/* 484 */       styleParams.setLocalData(props);
/*     */ 
/* 487 */       styleParams.putLocal("MaxNumericVal", Integer.toString(2147483647));
/*     */ 
/* 489 */       DynamicHtmlMerger htmlMerger = new PageMerger(styleParams, cxt);
/* 490 */       styleParams.addResultSet("IndexerFields", fieldRows);
/*     */ 
/* 492 */       DataLoader.checkCachedPage(template, cxt);
/* 493 */       DynamicHtml dynHtml = SharedObjects.getHtmlPage(template);
/*     */ 
/* 496 */       String enc = this.m_config.getValue("IndexerEncoding");
/* 497 */       bw = FileUtils.openDataWriter(FileUtils.getDirectory(path), FileUtils.getName(path), enc);
/* 498 */       styleParams.m_javaEncoding = enc;
/* 499 */       String encHeader = DataSerializeUtils.packageEncodingHeader(styleParams, null);
/* 500 */       if ((encHeader != null) && (encHeader.length() > 0))
/*     */       {
/* 502 */         bw.write(encHeader);
/*     */       }
/*     */ 
/* 505 */       if (dynHtml != null);
/*     */       try
/*     */       {
/* 522 */         if (bw != null)
/*     */         {
/* 524 */           bw.flush();
/* 525 */           bw.close();
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 530 */         throw new ServiceException("!csIndexerUnableToCreateConfig", e);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */     catch (ParseSyntaxException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       try
/*     */       {
/* 522 */         if (bw != null)
/*     */         {
/* 524 */           bw.flush();
/* 525 */           bw.close();
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 530 */         throw new ServiceException("!csIndexerUnableToCreateConfig", e);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public int parseResults(String input)
/*     */     throws ServiceException
/*     */   {
/* 538 */     if (this.m_config.getBoolean("isMaintainIndexProcess", false))
/*     */     {
/* 540 */       this.m_config.setValue("isIndexProcessStarted", "true");
/*     */     }
/* 542 */     int start = 0;
/* 543 */     int end = 0;
/* 544 */     Vector infoList = this.m_execution.m_indexerInfoList;
/* 545 */     int length = infoList.size();
/* 546 */     int count = -1;
/*     */ 
/* 548 */     while (end <= input.length())
/*     */     {
/* 550 */       end = input.indexOf("\n", start);
/*     */ 
/* 552 */       if (end < 0) {
/*     */         break;
/*     */       }
/*     */ 
/* 556 */       String line = input.substring(start, end).trim();
/* 557 */       if (line.startsWith("Indexing failed"))
/*     */       {
/* 559 */         for (int i = 0; i < length; ++i)
/*     */         {
/* 561 */           IndexerInfo info = (IndexerInfo)infoList.elementAt(i);
/* 562 */           info.m_indexStatus = 4;
/* 563 */           info.m_indexError = line;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 568 */         int idx = line.indexOf("Document:");
/* 569 */         if (idx >= 0)
/*     */         {
/* 571 */           int keyStart = line.indexOf(58);
/* 572 */           int keyEnd = line.indexOf(32);
/*     */ 
/* 574 */           if ((keyStart >= 0) && (keyEnd - keyStart > 0))
/*     */           {
/* 577 */             String key = line.substring(keyStart + 1, keyEnd).trim();
/* 578 */             for (int i = 0; i < length; ++i)
/*     */             {
/* 580 */               IndexerInfo info = (IndexerInfo)infoList.elementAt(i);
/* 581 */               if (!info.m_indexKey.equals(key))
/*     */                 continue;
/* 583 */               if (line.indexOf("failed", keyEnd) >= 0)
/*     */               {
/* 585 */                 info.m_indexStatus = 3;
/* 586 */                 info.m_indexError = line; break;
/*     */               }
/* 588 */               if (line.indexOf("metadataonly", keyEnd) < 0)
/*     */                 break;
/* 590 */               info.m_isMetaDataOnly = true; break;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 599 */       start = end + 1;
/*     */     }
/*     */ 
/* 602 */     return count;
/*     */   }
/*     */ 
/*     */   public void validateConfig()
/*     */     throws ServiceException
/*     */   {
/* 609 */     FileUtils.checkOrCreateDirectory(this.m_execution.m_bulkDir, 0);
/*     */ 
/* 611 */     FileUtils.validatePath(this.m_config.getValue("IndexerPath"), IdcMessageFactory.lc("csIndexerPathError", new Object[0]), 1);
/*     */   }
/*     */ 
/*     */   public boolean checkCollectionExistence(boolean mustExist, String errMessage)
/*     */     throws ServiceException
/*     */   {
/* 618 */     return true;
/*     */   }
/*     */ 
/*     */   protected String fixDate(String s) throws ServiceException
/*     */   {
/* 623 */     long l = 0L;
/*     */     Date date;
/*     */     try
/*     */     {
/* 628 */       date = LocaleResources.m_bulkloadFormat.parseDate(s);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 632 */       date = LocaleResources.parseDate(s, null);
/*     */     }
/* 634 */     l = date.getTime();
/* 635 */     s = "" + l;
/*     */ 
/* 637 */     return s;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 642 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.AltavistaHandler
 * JD-Core Version:    0.5.4
 */