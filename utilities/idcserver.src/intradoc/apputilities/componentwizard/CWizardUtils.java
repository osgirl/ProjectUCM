/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.HtmlChunk;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainerUtils;
/*     */ import intradoc.common.ResourceObject;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CWizardUtils
/*     */ {
/*  40 */   public static boolean m_isLightWeightCW = false;
/*  41 */   public static boolean m_isRefineryCW = false;
/*  42 */   public static final String[] CORE_RESOURCE_TYPE_KEYS = { "isHtmlInclude", "isDataInclude", "isString" };
/*  43 */   public static int[] CORE_RESOURCE_TYPES = { 0, 1, 7 };
/*  44 */   public static final String[] CORE_RESOURCE_TYPE_LABELS = { "csCompWizLabelHtmlInclude", "csCompWizLabelDataInclude", "csCompWizLabelString" };
/*     */ 
/*  46 */   public static final String[][] ADVANCED_SETTINGS_INFO = { { "serverVersion", "csCompWizServerVersion", "csCompWizServerVersionDesc", "choice", "version", "1" }, { "installID", "csCompWizInstallID", "csCompWizInstallIDDesc", "text", "", "0" }, { "componentTags", "csCompWizComponentTags", "csCompWizComponentTagDesc", "multichoice", "tags", "0" }, { "featureExtensions", "csCompWizFeatureExtensions", "csCompWizFeatureExtensionsDesc", "text", "", "0" }, { "requiredFeatures", "csCompWizRequiredFeatures", "csCompWizRequiredFeaturesDesc", "text", "", "0" }, { "classpath", "csCompWizClassPath", "csCompWizClassPathDesc", "choice", "path", "1" }, { "classpathorder", "csCompWizClassPathOrder", "csCompWizClassPathDesc", "int", "", "0" }, { "libpath", "csCompWizLibPath", "csCompWizLibPathDesc", "choice", "path", "0" }, { "libpathorder", "csCompWizClassPathOrder", "csCompWizClassPathOrderDesc", "int", "", "0" }, { "componentsToDisable", "csCompWizDisableComps", "csCompWizDisableCompsDesc", "text", "", "1" }, { "additionalComponents", "csCompWizAdditionalComps", "csCompWizAdditionalCompsDesc", "memo", "", "0" }, { "preventAdditionalComponentDowngrade", "csCompWizPreventDowngrade", "csCompWizPreventDowngradeDesc", "bool", "", "0" } };
/*     */ 
/*     */   public static DataBinder readFile(String dir, String filename, String lockDir)
/*     */     throws ServiceException
/*     */   {
/*  68 */     DataBinder binder = new DataBinder(true);
/*  69 */     ResourceUtils.serializeDataBinder(dir, filename, binder, false, true);
/*  70 */     return binder;
/*     */   }
/*     */ 
/*     */   public static long writeFile(String dir, String name, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  77 */     if (binder != null)
/*     */     {
/*  79 */       ResourceUtils.serializeDataBinder(dir, name, binder, true, false);
/*     */     }
/*     */ 
/*  82 */     File f = new File(dir, name);
/*  83 */     return f.lastModified();
/*     */   }
/*     */ 
/*     */   public static long getLastModified(String path)
/*     */   {
/*  88 */     File file = new File(path);
/*     */ 
/*  90 */     if (!file.exists())
/*     */     {
/*  92 */       return -2L;
/*     */     }
/*     */ 
/*  95 */     return file.lastModified();
/*     */   }
/*     */ 
/*     */   public static boolean isTimeStampChanged(String filePath, long lastTimeStamp)
/*     */   {
/* 100 */     long timestamp = getLastModified(filePath);
/*     */ 
/* 103 */     return lastTimeStamp != timestamp;
/*     */   }
/*     */ 
/*     */   public static Properties loadPropertiesFromFile(String path)
/*     */     throws DataException
/*     */   {
/* 110 */     Properties props = new Properties();
/*     */     try
/*     */     {
/* 113 */       FileUtils.loadProperties(props, path);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 117 */       throw new DataException(LocaleUtils.encodeMessage("csCompWizPropLoadError", null, path, e.getMessage()));
/*     */     }
/*     */ 
/* 120 */     return props;
/*     */   }
/*     */ 
/*     */   public static String findIncludeOrStringData(String name, Map list, int resType)
/*     */     throws ServiceException
/*     */   {
/* 126 */     String data = "";
/* 127 */     boolean isInclude = resType != 7;
/* 128 */     if (isInclude)
/*     */     {
/* 130 */       DynamicHtml dynHtml = (DynamicHtml)list.get(name);
/*     */ 
/* 132 */       if (dynHtml != null)
/*     */       {
/* 134 */         StringBuffer temp = new StringBuffer();
/* 135 */         for (int i = 0; i < dynHtml.m_htmlChunks.size(); ++i)
/*     */         {
/* 137 */           HtmlChunk chunk = (HtmlChunk)dynHtml.m_htmlChunks.get(i);
/* 138 */           if (chunk.m_chunkType != 0)
/*     */             continue;
/* 140 */           String tempStr = new String(chunk.m_chars);
/*     */ 
/* 142 */           temp.append(tempStr);
/*     */         }
/*     */ 
/* 146 */         data = temp.toString();
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 151 */       ResourceObject res = (ResourceObject)list.get(name);
/* 152 */       if (res == null)
/*     */       {
/* 154 */         throw new ServiceException(null, "syStringUndefined", new Object[] { name });
/*     */       }
/* 156 */       data = (String)res.m_resource;
/*     */     }
/*     */ 
/* 159 */     return data;
/*     */   }
/*     */ 
/*     */   protected static void launchExe(String editorPath, String filename)
/*     */     throws ServiceException
/*     */   {
/* 165 */     if (FileUtils.checkFile(filename, true, false) == -16)
/*     */     {
/* 167 */       throw new ServiceException(LocaleUtils.encodeMessage("syFileDoesNotExist", null, filename));
/*     */     }
/*     */ 
/* 170 */     String command = editorPath;
/* 171 */     if (EnvUtils.isFamily("unix"))
/*     */     {
/* 173 */       command = command + " " + filename;
/*     */     }
/*     */     else
/*     */     {
/* 177 */       String sep = File.separator;
/* 178 */       filename = filename.replace('/', sep.charAt(0));
/* 179 */       command = command + " \"" + filename + "\"";
/*     */     }
/*     */     try
/*     */     {
/* 183 */       Runtime run = Runtime.getRuntime();
/* 184 */       run.exec(command);
/*     */     }
/*     */     catch (IOException io)
/*     */     {
/* 188 */       throw new ServiceException("!csCompWizHTMLEditorLaunchError");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String trimTabs(String str, boolean addLineFeed)
/*     */   {
/* 195 */     if ((str == null) || (str.length() == 0))
/*     */     {
/* 197 */       return null;
/*     */     }
/*     */ 
/* 200 */     String retStr = "";
/* 201 */     String tempStr = null;
/* 202 */     StringTokenizer tknz = new StringTokenizer(str, "\n");
/*     */ 
/* 204 */     while (tknz.hasMoreElements())
/*     */     {
/* 206 */       String token = tknz.nextToken();
/*     */ 
/* 208 */       int index = token.lastIndexOf(9);
/*     */ 
/* 210 */       if (index >= 0)
/*     */       {
/* 212 */         tempStr = token.substring(index + 1, token.length()).trim();
/*     */       }
/*     */       else
/*     */       {
/* 216 */         tempStr = token.trim();
/*     */       }
/*     */ 
/* 219 */       retStr = retStr + tempStr;
/*     */ 
/* 221 */       if (addLineFeed)
/*     */       {
/* 223 */         retStr = retStr + '\n';
/*     */       }
/*     */       else
/*     */       {
/* 227 */         retStr = retStr + " ";
/*     */       }
/*     */     }
/*     */ 
/* 231 */     return retStr;
/*     */   }
/*     */ 
/*     */   public static String removeSpaces(String str)
/*     */   {
/* 236 */     int len = str.length();
/* 237 */     String tempStr = "";
/*     */ 
/* 239 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 241 */       char ch = str.charAt(i);
/*     */ 
/* 243 */       if ((ch == ' ') || (ch == '\t') || (ch == '\r')) continue; if (ch == '\n')
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 248 */       tempStr = tempStr + ch;
/*     */     }
/*     */ 
/* 251 */     return tempStr;
/*     */   }
/*     */ 
/*     */   public static Vector getChoiceList(String tablename, String colname, boolean addSelect)
/*     */     throws DataException, ServiceException
/*     */   {
/* 257 */     DataResultSet drset = SharedObjects.getTable(tablename);
/*     */ 
/* 259 */     if (drset == null)
/*     */     {
/* 261 */       throw new ServiceException(LocaleUtils.encodeMessage("csTableNotLoaded", null, tablename));
/*     */     }
/*     */ 
/* 264 */     String[][] table = ResultSetUtils.createStringTable(drset, new String[] { colname });
/*     */ 
/* 266 */     Vector list = new IdcVector();
/*     */ 
/* 268 */     if (addSelect)
/*     */     {
/* 270 */       list.addElement("<Select>");
/*     */     }
/*     */ 
/* 273 */     for (int i = 0; i < table.length; ++i)
/*     */     {
/* 275 */       String str = table[i][0];
/*     */ 
/* 277 */       if ((str == null) || (str.length() == 0)) continue; if (str.equalsIgnoreCase("null"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 282 */       boolean isUnique = true;
/* 283 */       for (int j = 0; j < list.size(); ++j)
/*     */       {
/* 285 */         String tempStr = (String)list.elementAt(j);
/* 286 */         if ((tempStr != null) && (tempStr.length() != 0) && (!tempStr.equalsIgnoreCase(str))) {
/*     */           continue;
/*     */         }
/* 289 */         isUnique = false;
/*     */       }
/*     */ 
/* 293 */       if (!isUnique)
/*     */         continue;
/* 295 */       list.addElement(str);
/*     */     }
/*     */ 
/* 299 */     return list;
/*     */   }
/*     */ 
/*     */   public static String[][] createInfoTableFromNamedTable(String tableName, String fieldName)
/*     */   {
/* 304 */     String[] fields = { fieldName, fieldName };
/* 305 */     String[][] infoTable = (String[][])null;
/*     */ 
/* 307 */     DataResultSet drset = SharedObjects.getTable(tableName);
/* 308 */     if (null != drset)
/*     */     {
/*     */       try
/*     */       {
/* 312 */         infoTable = ResultSetUtils.createStringTable(drset, fields);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 316 */         Report.trace("componentwizard", "Unable to create choice table for " + tableName, e);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 322 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadSharedTable", null, tableName);
/* 323 */       Report.trace("componentwizard", msg, null);
/*     */     }
/* 325 */     return infoTable;
/*     */   }
/*     */ 
/*     */   public static String[][] createInfoTableFromResource(String res, String[] fields)
/*     */   {
/* 331 */     String[][] infoTable = (String[][])null;
/* 332 */     Table table = ResourceContainerUtils.getDynamicTableResource(res);
/* 333 */     if (table != null)
/*     */     {
/* 335 */       DataResultSet drset = new DataResultSet();
/* 336 */       drset.init(table);
/*     */       try
/*     */       {
/* 340 */         infoTable = ResultSetUtils.createStringTable(drset, fields);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 344 */         Report.trace("componentwizard", "Unable to create choice table from resource " + res, e);
/*     */       }
/*     */     }
/*     */ 
/* 348 */     return infoTable;
/*     */   }
/*     */ 
/*     */   public static List createTagsList(DataResultSet components, boolean isForDisplay)
/*     */   {
/* 353 */     List tagList = new ArrayList();
/*     */     try
/*     */     {
/* 357 */       int tagIndex = ResultSetUtils.getIndexMustExist(components, "componentTags");
/* 358 */       for (components.first(); components.isRowPresent(); components.next())
/*     */       {
/* 360 */         String tagStr = components.getStringValue(tagIndex);
/* 361 */         List tags = StringUtils.makeListFromSequenceSimple(tagStr);
/* 362 */         int num = tags.size();
/* 363 */         for (int i = 0; i < num; ++i)
/*     */         {
/* 365 */           String tag = (String)tags.get(i);
/* 366 */           if (tagList.contains(tag))
/*     */             continue;
/* 368 */           tagList.add(tag);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 373 */       String[][] list = createInfoTableFromResource("LegacyTaggedComponents", new String[] { "tags", "tags" });
/*     */ 
/* 375 */       for (int i = 0; i < list.length; ++i)
/*     */       {
/* 377 */         String str = list[i][0];
/* 378 */         List tags = StringUtils.makeListFromSequenceSimple(str);
/* 379 */         int num = tags.size();
/* 380 */         for (int j = 0; j < num; ++j)
/*     */         {
/* 382 */           String tag = (String)tags.get(j);
/* 383 */           if (tagList.contains(tag))
/*     */             continue;
/* 385 */           tagList.add(tag);
/*     */         }
/*     */       }
/*     */ 
/* 389 */       IdcComparator cmp = new IdcComparator()
/*     */       {
/*     */         public int compare(Object obj1, Object obj2)
/*     */         {
/* 393 */           String key1 = (String)obj1;
/* 394 */           String key2 = (String)obj2;
/*     */ 
/* 396 */           return key1.compareTo(key2);
/*     */         }
/*     */       };
/* 399 */       Sort.sortList(tagList, cmp);
/*     */ 
/* 401 */       if (!isForDisplay)
/*     */       {
/* 404 */         int index = tagList.indexOf("home");
/* 405 */         if (index >= 0)
/*     */         {
/* 407 */           tagList.remove(index);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 413 */       Report.trace("componentwizard", "CWizardUtils.createTagsList:Failed to create tag list for display.", e);
/*     */     }
/*     */ 
/* 416 */     return tagList;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String formatErrorMessage(IntradocComponent comp)
/*     */   {
/* 423 */     IdcMessage msg = formatErrorMessage(comp, null);
/* 424 */     return LocaleUtils.encodeMessage(msg);
/*     */   }
/*     */ 
/*     */   public static IdcMessage formatErrorMessage(IntradocComponent comp, Map options)
/*     */   {
/* 429 */     IdcMessage errMsg = null;
/* 430 */     IdcMessage msg = null;
/*     */ 
/* 432 */     if ((comp.m_errorMsg != null) && (comp.m_errorMsg.length() > 0))
/*     */     {
/* 434 */       errMsg = IdcMessageFactory.lc();
/* 435 */       errMsg.m_msgEncoded = comp.m_errorMsg;
/* 436 */       msg = errMsg;
/*     */     }
/*     */ 
/* 439 */     for (int i = 0; i < comp.m_fileInfo.size(); ++i)
/*     */     {
/* 441 */       ResourceFileInfo finfo = (ResourceFileInfo)comp.m_fileInfo.elementAt(i);
/*     */ 
/* 443 */       if (finfo.m_errMsg == null)
/*     */         continue;
/* 445 */       IdcMessage tmp = IdcMessageFactory.lc();
/* 446 */       tmp.m_msgEncoded = finfo.m_errMsg;
/* 447 */       if (msg == null)
/*     */       {
/* 449 */         msg = errMsg = tmp;
/*     */       }
/*     */       else
/*     */       {
/* 453 */         msg.m_prior = tmp;
/* 454 */         msg = tmp;
/*     */       }
/* 456 */       finfo.m_errMsg = null;
/*     */     }
/*     */ 
/* 460 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public static String retrieveDynamicHtml(DataBinder data, String includeName)
/*     */     throws ServiceException
/*     */   {
/* 466 */     IdcCharArrayWriter sw = new IdcCharArrayWriter();
/* 467 */     PageMerger pm = new PageMerger(data, null);
/*     */ 
/* 469 */     DynamicHtml dynHtml = SharedObjects.getHtmlResource(includeName);
/*     */ 
/* 471 */     if (dynHtml == null)
/*     */     {
/* 473 */       throw new ServiceException(LocaleUtils.encodeMessage("csCompWizDynIncludeNotFound", null, includeName));
/*     */     }
/*     */     try
/*     */     {
/* 477 */       dynHtml.outputHtml(sw, pm);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 481 */       throw new ServiceException(e);
/*     */     }
/* 483 */     return sw.toStringRelease().trim();
/*     */   }
/*     */ 
/*     */   public static boolean isReadOnly(String filename)
/*     */   {
/* 489 */     boolean isReadOnly = false;
/* 490 */     if (FileUtils.checkFile(filename, true, true) == -19)
/*     */     {
/* 492 */       isReadOnly = true;
/*     */     }
/*     */ 
/* 495 */     return isReadOnly;
/*     */   }
/*     */ 
/*     */   public static Map getIncludeOrStringMapByType(ResourceFileInfo info, int resourceType)
/*     */   {
/* 500 */     Map map = null;
/* 501 */     if (resourceType == 0)
/*     */     {
/* 503 */       map = info.m_resources.m_dynamicHtml;
/*     */     }
/* 505 */     else if (resourceType == 1)
/*     */     {
/* 507 */       map = info.m_resources.m_dynamicData;
/*     */     }
/*     */     else
/*     */     {
/* 511 */       map = info.m_resources.m_resourceMap;
/*     */     }
/* 513 */     return map;
/*     */   }
/*     */ 
/*     */   public static List getIncludeOrStringListByType(ResourceFileInfo info, int resourceType)
/*     */   {
/* 518 */     List list = null;
/* 519 */     if (resourceType == 0)
/*     */     {
/* 521 */       list = info.m_resources.m_dynamicHtmlList;
/*     */     }
/* 523 */     else if (resourceType == 1)
/*     */     {
/* 525 */       list = info.m_resources.m_dynamicDataList;
/*     */     }
/*     */     else
/*     */     {
/* 529 */       list = info.m_resources.m_stringsList;
/*     */     }
/* 531 */     return list;
/*     */   }
/*     */ 
/*     */   public static ResultSet buildIncludeOrStringResultSetByType(ResourceFileInfo info, int resourceType)
/*     */   {
/* 536 */     List list = getIncludeOrStringListByType(info, resourceType);
/* 537 */     DataResultSet drset = new DataResultSet(new String[] { "includeOrString" });
/* 538 */     for (int i = 0; i < list.size(); ++i)
/*     */     {
/* 540 */       String include = (String)list.get(i);
/* 541 */       if (include == null)
/*     */         continue;
/* 543 */       Vector v = drset.createEmptyRow();
/* 544 */       v.setElementAt(include, 0);
/* 545 */       drset.addRow(v);
/*     */     }
/*     */ 
/* 549 */     return drset;
/*     */   }
/*     */ 
/*     */   public static boolean isCoreResourceType(int resType)
/*     */   {
/* 554 */     boolean retVal = false;
/* 555 */     for (int i = 0; i < CORE_RESOURCE_TYPES.length; ++i)
/*     */     {
/* 557 */       if (CORE_RESOURCE_TYPES[i] != resType)
/*     */         continue;
/* 559 */       retVal = true;
/* 560 */       break;
/*     */     }
/*     */ 
/* 563 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static int determineArrayIndexFromResourceType(int resType)
/*     */   {
/* 568 */     int retVal = 0;
/* 569 */     for (int i = 0; i < CORE_RESOURCE_TYPES.length; ++i)
/*     */     {
/* 571 */       if (CORE_RESOURCE_TYPES[i] != resType)
/*     */         continue;
/* 573 */       retVal = i;
/* 574 */       break;
/*     */     }
/*     */ 
/* 577 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static int determineCoreResourceTypeFromCheckboxes(Properties props)
/*     */   {
/* 582 */     int retVal = 0;
/* 583 */     for (int i = 0; i < CORE_RESOURCE_TYPE_KEYS.length; ++i)
/*     */     {
/* 585 */       String val = props.getProperty(CORE_RESOURCE_TYPE_KEYS[i]);
/* 586 */       if (!StringUtils.convertToBool(val, false))
/*     */         continue;
/* 588 */       retVal = CORE_RESOURCE_TYPES[i];
/* 589 */       break;
/*     */     }
/*     */ 
/* 592 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static void clearCoreResourceTypeFromCheckboxes(Properties props, int curResType)
/*     */   {
/* 597 */     for (int i = 0; i < CORE_RESOURCE_TYPE_KEYS.length; ++i)
/*     */     {
/* 599 */       String val = (CORE_RESOURCE_TYPES[i] == curResType) ? "1" : "";
/* 600 */       props.setProperty(CORE_RESOURCE_TYPE_KEYS[i], val);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String changeDriveLetterToUpper(String path)
/*     */   {
/* 607 */     if (!EnvUtils.isFamily("unix"))
/*     */     {
/* 609 */       int index = path.indexOf(58);
/* 610 */       if (index > 0)
/*     */       {
/* 612 */         String drive = path.substring(0, index);
/* 613 */         drive = drive.toUpperCase();
/* 614 */         path = drive + path.substring(index, path.length());
/*     */       }
/*     */     }
/*     */ 
/* 618 */     return path;
/*     */   }
/*     */ 
/*     */   protected static String findDisplayName(String[][] typeMap, String type)
/*     */   {
/* 623 */     String displayName = "";
/*     */ 
/* 625 */     for (int i = 0; i < typeMap.length; ++i)
/*     */     {
/* 627 */       if (!typeMap[i][0].equalsIgnoreCase(type))
/*     */         continue;
/* 629 */       displayName = typeMap[i][1];
/* 630 */       break;
/*     */     }
/*     */ 
/* 634 */     return displayName;
/*     */   }
/*     */ 
/*     */   public static void writeTemplateFile(DataBinder params, String template, ExecutionContext cxt, String path)
/*     */     throws ServiceException, DataException
/*     */   {
/* 640 */     File file = new File(path);
/*     */ 
/* 642 */     BufferedWriter bw = null;
/* 643 */     DynamicHtmlMerger htmlMerger = new PageMerger(params, cxt);
/*     */ 
/* 645 */     DataResultSet drset = SharedObjects.getTable("IntradocTemplates");
/*     */ 
/* 647 */     if (drset == null)
/*     */     {
/* 649 */       throw new ServiceException(LocaleUtils.encodeMessage("csTableNotLoaded", null, "IntradocTemplates"));
/*     */     }
/*     */ 
/* 652 */     FieldInfo[] info = ResultSetUtils.createInfoList(drset, new String[] { "name", "filename" }, true);
/* 653 */     Vector v = drset.findRow(info[0].m_index, template);
/*     */ 
/* 655 */     String tfilename = (String)v.elementAt(info[1].m_index);
/*     */     try
/*     */     {
/* 658 */       DataLoader.checkCachedPage(template, cxt);
/* 659 */       DynamicHtml dynHtml = ResourceLoader.loadPage(tfilename, false);
/* 660 */       if (dynHtml != null)
/*     */       {
/* 662 */         bw = FileUtils.openDataWriter(file);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 674 */         if (bw != null)
/*     */         {
/* 676 */           bw.flush();
/* 677 */           bw.close();
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 682 */         throw new ServiceException(LocaleUtils.encodeMessage("csCompWizUnableToCreateFile", null, path), e);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       try
/*     */       {
/* 674 */         if (bw != null)
/*     */         {
/* 676 */           bw.flush();
/* 677 */           bw.close();
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 682 */         throw new ServiceException(LocaleUtils.encodeMessage("csCompWizUnableToCreateFile", null, path), e);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String covertPackageToFilePath(String classPath, String location)
/*     */   {
/* 689 */     String filePath = null;
/*     */ 
/* 691 */     Vector v = StringUtils.parseArray(location, '.', '.');
/* 692 */     for (int i = 0; i < v.size(); ++i)
/*     */     {
/* 694 */       if (filePath == null)
/*     */       {
/* 696 */         filePath = classPath;
/*     */       }
/*     */       else
/*     */       {
/* 700 */         filePath = filePath + '/';
/*     */       }
/* 702 */       filePath = filePath + (String)v.elementAt(i);
/*     */ 
/* 704 */       if (i != v.size() - 1)
/*     */         continue;
/* 706 */       filePath = filePath + ".java";
/*     */     }
/*     */ 
/* 710 */     return filePath;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 715 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.CWizardUtils
 * JD-Core Version:    0.5.4
 */