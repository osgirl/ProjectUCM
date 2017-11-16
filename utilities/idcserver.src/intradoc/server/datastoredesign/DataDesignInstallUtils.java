/*     */ package intradoc.server.datastoredesign;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainerUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.shared.MetaFieldUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DataDesignInstallUtils
/*     */ {
/*     */   public static final String DATA_SUB_DIR_OLD = "componentdbinstall/";
/*     */   public static final String DATA_SUB_DIR = "datastoredesign/";
/*     */ 
/*     */   public static boolean isNewlyCreatedColumns(String compName, String tableName, String colName)
/*     */     throws ServiceException
/*     */   {
/*  77 */     upgradeDataDirectoryLocation();
/*     */ 
/*  79 */     String dir = DirectoryLocator.getAppDataDirectory();
/*  80 */     String subDir = getDataDirectoryLocation();
/*     */ 
/*  82 */     FileUtils.checkOrCreateSubDirectory(dir, subDir);
/*     */ 
/*  84 */     dir = dir + subDir;
/*     */ 
/*  86 */     String fileName = "newlyAddedColumnsToTable.hda";
/*     */ 
/*  88 */     File file = FileUtilsCfgBuilder.getCfgFile(dir + fileName, "DataStoreDesign", false);
/*  89 */     DataBinder db = new DataBinder();
/*     */ 
/*  91 */     if (file.exists())
/*     */     {
/*  93 */       db = ResourceUtils.readDataBinder(dir, fileName);
/*     */ 
/*  95 */       DataResultSet newlyAddedColdrset = (DataResultSet)db.getResultSet("newlyAddedColumns");
/*  96 */       newlyAddedColdrset = upgradeFieldNames(newlyAddedColdrset, getFieldNameUpgradeMap(), true);
/*     */ 
/*  98 */       if ((newlyAddedColdrset == null) || (newlyAddedColdrset.getNumRows() == 0))
/*     */       {
/* 100 */         return false;
/*     */       }
/*     */ 
/* 103 */       DataResultSet colNamedrset = filterColumnsForTable(newlyAddedColdrset, compName, tableName);
/*     */ 
/* 105 */       for (colNamedrset.first(); colNamedrset.isRowPresent(); colNamedrset.next())
/*     */       {
/* 107 */         String addedColName = ResultSetUtils.getValue(colNamedrset, "dsdColumnName");
/* 108 */         if (addedColName.equalsIgnoreCase(colName))
/*     */         {
/* 110 */           return true;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 115 */     return false;
/*     */   }
/*     */ 
/*     */   public static boolean isNewlyCreatedTable(String tableName)
/*     */     throws ServiceException
/*     */   {
/* 130 */     upgradeDataDirectoryLocation();
/*     */ 
/* 132 */     String dir = DirectoryLocator.getAppDataDirectory();
/* 133 */     String subDir = getDataDirectoryLocation();
/*     */ 
/* 135 */     FileUtils.checkOrCreateSubDirectory(dir, subDir);
/*     */ 
/* 137 */     dir = dir + subDir;
/*     */ 
/* 139 */     String fileName = "newlyCreatedTable.hda";
/*     */ 
/* 141 */     File file = FileUtilsCfgBuilder.getCfgFile(dir + fileName, "DataStoreDesign", false);
/*     */ 
/* 143 */     DataBinder db = new DataBinder();
/*     */ 
/* 145 */     if (file.exists())
/*     */     {
/* 147 */       db = ResourceUtils.readDataBinder(dir, fileName);
/*     */ 
/* 149 */       DataResultSet newlyAddedTabledrset = (DataResultSet)db.getResultSet("newlyCreatedTable");
/* 150 */       newlyAddedTabledrset = upgradeFieldNames(newlyAddedTabledrset, getFieldNameUpgradeMap(), true);
/*     */ 
/* 152 */       if ((newlyAddedTabledrset == null) || (newlyAddedTabledrset.getNumRows() == 0))
/*     */       {
/* 154 */         return false;
/*     */       }
/*     */ 
/* 157 */       for (newlyAddedTabledrset.first(); newlyAddedTabledrset.isRowPresent(); newlyAddedTabledrset.next())
/*     */       {
/* 159 */         String addedTableName = ResultSetUtils.getValue(newlyAddedTabledrset, "dsdTableName");
/* 160 */         if (addedTableName.equalsIgnoreCase(tableName))
/*     */         {
/* 162 */           return true;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 167 */     return false;
/*     */   }
/*     */ 
/*     */   public static boolean isNewVersion(Workspace ws, String compVersion, String name, DataDesignConfigInfo ddConfigInfo)
/*     */     throws DataException, ServiceException
/*     */   {
/* 174 */     return isNewVersion(ws, compVersion, name, null, ddConfigInfo);
/*     */   }
/*     */ 
/*     */   public static boolean isNewVersion(Workspace ws, String compVersion, String name, String columnName, DataDesignConfigInfo ddConfigInfo)
/*     */     throws DataException, ServiceException
/*     */   {
/* 189 */     String curCompVersion = (String)ddConfigInfo.m_curConfigTableValues.get(name);
/* 190 */     boolean isNewVersion = false;
/* 191 */     boolean isCompInstall = false;
/*     */ 
/* 193 */     if (curCompVersion == null)
/*     */     {
/* 195 */       curCompVersion = getComponentVersion(ws, name, ddConfigInfo);
/*     */ 
/* 197 */       if (curCompVersion == null)
/*     */       {
/* 206 */         if ((columnName != null) && (MetaFieldUtils.hasDocMetaDef(columnName)))
/*     */         {
/* 208 */           String message = "MetaDataField " + columnName + " is already present in DocMetaDefinition. " + "It's version is empty, so it will not be updated";
/*     */ 
/* 210 */           Report.trace("datastoredesign", message, null);
/* 211 */           return isNewVersion;
/*     */         }
/*     */ 
/* 218 */         curCompVersion = "0.0.0.0.0 1/1/1000";
/*     */ 
/* 220 */         isCompInstall = true;
/*     */       }
/* 222 */       ddConfigInfo.m_curConfigTableValues.put(name, curCompVersion);
/*     */     }
/*     */     else
/*     */     {
/* 226 */       curCompVersion = (String)ddConfigInfo.m_curConfigTableValues.get(name);
/*     */     }
/*     */ 
/* 229 */     boolean result = compareComponentVersion(curCompVersion, compVersion);
/*     */ 
/* 231 */     if (result)
/*     */     {
/* 233 */       String latestCompVersion = (String)ddConfigInfo.m_versionMap.get(name);
/* 234 */       if (latestCompVersion == null)
/*     */       {
/* 236 */         ddConfigInfo.m_versionMap.put(name, compVersion);
/*     */       }
/*     */       else
/*     */       {
/* 240 */         result = compareComponentVersion(latestCompVersion, compVersion);
/*     */       }
/*     */ 
/* 243 */       if (result)
/*     */       {
/* 245 */         String version = name + "','" + compVersion;
/* 246 */         String configValues = "('" + ddConfigInfo.m_sectionPrefix;
/*     */ 
/* 248 */         if (isCompInstall)
/*     */         {
/* 250 */           configValues = configValues + "Install','" + version + "','1')";
/*     */         }
/*     */         else
/*     */         {
/* 254 */           configValues = configValues + "Update','" + version + "','5')";
/*     */         }
/*     */ 
/* 257 */         ddConfigInfo.m_versionMap.put(name, compVersion);
/* 258 */         ddConfigInfo.m_configTableValues.put(name, configValues);
/*     */       }
/*     */ 
/* 261 */       isNewVersion = true;
/*     */     }
/*     */ 
/* 264 */     return isNewVersion;
/*     */   }
/*     */ 
/*     */   public static String getComponentVersion(Workspace ws, String compName, DataDesignConfigInfo ddConfigInfo)
/*     */     throws DataException, ServiceException
/*     */   {
/* 276 */     String version = null;
/* 277 */     DataBinder args = new DataBinder();
/* 278 */     args.putLocal("dName", compName);
/*     */ 
/* 280 */     ResultSet rs = ws.createResultSet(ddConfigInfo.m_queryStr, args);
/* 281 */     DataResultSet drset = new DataResultSet();
/* 282 */     drset.copy(rs);
/*     */ 
/* 284 */     String[] keys = { "dName", "dVersion" };
/* 285 */     String[] oracleKeys = { "DNAME", "DVERSION" };
/*     */ 
/* 287 */     if (drset.getNumRows() == 0)
/*     */     {
/* 289 */       return version;
/*     */     }
/*     */     FieldInfo[] fi;
/*     */     try
/*     */     {
/* 294 */       fi = ResultSetUtils.createInfoList(drset, keys, true);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 298 */       fi = ResultSetUtils.createInfoList(drset, oracleKeys, true);
/*     */     }
/* 300 */     String tempVersion = null;
/* 301 */     boolean result = false;
/*     */ 
/* 303 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 305 */       Vector v = drset.getCurrentRowValues();
/*     */ 
/* 307 */       if (version == null)
/*     */       {
/* 309 */         version = (String)v.elementAt(fi[1].m_index);
/* 310 */         if (isValidVersion(version))
/*     */         {
/* 312 */           tempVersion = version;
/*     */         }
/*     */         else
/*     */         {
/* 316 */           version = null;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 321 */         tempVersion = (String)v.elementAt(fi[1].m_index);
/* 322 */         if (!isValidVersion(tempVersion))
/*     */         {
/* 324 */           tempVersion = null;
/*     */         }
/*     */       }
/*     */ 
/* 328 */       if ((tempVersion != null) && (version != null))
/*     */       {
/* 330 */         result = compareComponentVersion(version, tempVersion);
/*     */       }
/*     */ 
/* 333 */       if (!result)
/*     */         continue;
/* 335 */       version = tempVersion;
/*     */     }
/*     */ 
/* 338 */     return version;
/*     */   }
/*     */ 
/*     */   public static boolean isComponentInstalled(Workspace ws, String compName) throws DataException
/*     */   {
/* 343 */     boolean isInstalled = false;
/*     */ 
/* 345 */     DataBinder args = new DataBinder();
/* 346 */     args.putLocal("dName", compName);
/*     */ 
/* 348 */     ResultSet rs = ws.createResultSet("QcomponentVersion", args);
/* 349 */     DataResultSet drset = new DataResultSet();
/* 350 */     drset.copy(rs);
/*     */ 
/* 352 */     if (drset.getNumRows() > 0)
/*     */     {
/* 354 */       isInstalled = true;
/*     */     }
/*     */ 
/* 357 */     return isInstalled;
/*     */   }
/*     */ 
/*     */   public static boolean isValidVersion(String version)
/*     */   {
/* 362 */     String versionStr = null;
/* 363 */     String date = null;
/*     */ 
/* 365 */     int num = version.indexOf(32);
/* 366 */     if (num != -1)
/*     */     {
/* 368 */       versionStr = version.substring(0, num);
/*     */ 
/* 370 */       if (!isValidVersionNumber(versionStr))
/*     */       {
/* 372 */         return false;
/*     */       }
/*     */ 
/* 375 */       date = version.substring(num + 1);
/* 376 */       int indexOfDot = date.indexOf(46);
/* 377 */       if (indexOfDot > 0)
/*     */       {
/* 379 */         date = date.substring(0, indexOfDot);
/*     */       }
/*     */       try
/*     */       {
/* 383 */         IdcDateFormat format = new IdcDateFormat();
/* 384 */         format.init("MM/dd/yy");
/* 385 */         format.parseDate(date);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 389 */         String errMsg = LocaleUtils.encodeMessage("csCBUnableToParseDate", null);
/* 390 */         Report.error("datastoredesign", errMsg, e);
/* 391 */         return false;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 397 */       versionStr = version;
/* 398 */       return isValidVersionNumber(versionStr);
/*     */     }
/* 400 */     return true;
/*     */   }
/*     */ 
/*     */   public static boolean isValidVersionNumber(String version)
/*     */   {
/* 405 */     String copyOfVersion = version;
/*     */ 
/* 407 */     for (int i = 0; i < 4; ++i)
/*     */     {
/* 409 */       int num = copyOfVersion.indexOf(46);
/* 410 */       if (num < 0) {
/*     */         break;
/*     */       }
/*     */ 
/* 414 */       copyOfVersion = copyOfVersion.substring(num + 1);
/*     */     }
/*     */ 
/* 418 */     return copyOfVersion.indexOf(46) <= 0;
/*     */   }
/*     */ 
/*     */   public static boolean compareComponentVersion(String version1, String version2)
/*     */   {
/* 440 */     String pattern = "(\\d+\\.){3}\\d+";
/* 441 */     boolean ver1Match = version1.matches(pattern);
/* 442 */     boolean ver2Match = version2.matches(pattern);
/* 443 */     if ((ver1Match) && (ver2Match))
/*     */     {
/* 445 */       return SystemUtils.compareVersions(version1, version2) <= -1;
/*     */     }
/* 447 */     if (ver1Match)
/*     */     {
/* 449 */       return false;
/*     */     }
/* 451 */     if (ver2Match)
/*     */     {
/* 453 */       return true;
/*     */     }
/*     */ 
/* 457 */     String ver1 = null;
/* 458 */     String date1 = null;
/* 459 */     String ver2 = null;
/* 460 */     String date2 = null;
/*     */ 
/* 462 */     int num = version1.indexOf(32);
/* 463 */     if (num != -1)
/*     */     {
/* 465 */       ver1 = version1.substring(0, num);
/* 466 */       ver1 = formatVersion(ver1);
/* 467 */       date1 = version1.substring(num + 1);
/* 468 */       num = date1.indexOf(46);
/* 469 */       if (num != -1)
/*     */       {
/* 471 */         date1 = date1.substring(0, num);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 476 */       ver1 = version1;
/* 477 */       ver1 = formatVersion(version1);
/* 478 */       date1 = "1/1/1000";
/*     */     }
/*     */ 
/* 481 */     num = version2.indexOf(32);
/* 482 */     if (num != -1)
/*     */     {
/* 484 */       ver2 = version2.substring(0, num);
/* 485 */       ver2 = formatVersion(ver2);
/* 486 */       date2 = version2.substring(num + 1);
/* 487 */       num = date2.indexOf(46);
/* 488 */       if (num != -1)
/*     */       {
/* 490 */         date2 = date2.substring(0, num);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 495 */       ver2 = version2;
/* 496 */       ver2 = formatVersion(version2);
/* 497 */       date2 = "1/1/1000";
/*     */     }
/*     */ 
/* 500 */     String temp1 = null;
/* 501 */     String temp2 = null;
/*     */ 
/* 503 */     for (int i = 0; i < 5; ++i)
/*     */     {
/* 505 */       num = ver1.indexOf(46);
/* 506 */       if (num > 0)
/*     */       {
/* 508 */         temp1 = ver1.substring(0, num);
/* 509 */         ver1 = ver1.substring(num + 1);
/*     */       }
/*     */       else
/*     */       {
/* 513 */         temp1 = ver1;
/*     */       }
/*     */ 
/* 516 */       num = ver2.indexOf(46);
/* 517 */       if (num > 0)
/*     */       {
/* 519 */         temp2 = ver2.substring(0, num);
/* 520 */         ver2 = ver2.substring(num + 1);
/*     */       }
/*     */       else
/*     */       {
/* 524 */         temp2 = ver2;
/*     */       }
/*     */ 
/* 527 */       int value1 = NumberUtils.parseInteger(temp1, 0);
/* 528 */       int value2 = NumberUtils.parseInteger(temp2, 0);
/*     */ 
/* 530 */       if (value2 > value1)
/*     */       {
/* 532 */         return true;
/*     */       }
/*     */ 
/* 535 */       if (value2 < value1)
/*     */       {
/* 537 */         return false;
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 543 */       IdcDateFormat format = new IdcDateFormat();
/* 544 */       format.init("MM/dd/yy");
/*     */ 
/* 547 */       Date dDate1 = format.parseDate(date1);
/* 548 */       Date dDate2 = format.parseDate(date2);
/*     */ 
/* 550 */       if (dDate1.before(dDate2))
/*     */       {
/* 552 */         return true;
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 557 */       String errMsg = LocaleUtils.encodeMessage("csCBUnableToParseDate", null);
/* 558 */       Report.error("datastoredesign", errMsg, e);
/*     */     }
/*     */ 
/* 561 */     return false;
/*     */   }
/*     */ 
/*     */   static String formatVersion(String version)
/*     */   {
/* 566 */     String ver = version;
/* 567 */     int num = 0;
/* 568 */     int i = 0;
/* 569 */     int tempNum = 0;
/* 570 */     for (; i < 5; ++i)
/*     */     {
/* 572 */       if (i != 0)
/*     */       {
/* 574 */         tempNum = num + 1;
/*     */       }
/* 576 */       num = ver.indexOf(46, tempNum);
/* 577 */       if ((num == -1) && (i < 4)) {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 583 */     for (int j = i; j < 4; ++j)
/*     */     {
/* 585 */       ver = ver + ".0";
/*     */     }
/*     */ 
/* 588 */     return ver;
/*     */   }
/*     */ 
/*     */   public static boolean isNewerComponentVersion(String curVersion, String newerVersion, StringBuffer latestVersion)
/*     */     throws DataException, ServiceException
/*     */   {
/* 599 */     boolean result = compareComponentVersion(curVersion, newerVersion);
/*     */ 
/* 601 */     if (result)
/*     */     {
/* 603 */       boolean isLatest = compareComponentVersion(latestVersion.toString(), newerVersion);
/*     */ 
/* 605 */       if (isLatest)
/*     */       {
/* 607 */         latestVersion.setLength(0);
/* 608 */         latestVersion.append(newerVersion);
/*     */       }
/*     */     }
/*     */ 
/* 612 */     return result;
/*     */   }
/*     */ 
/*     */   public static DataResultSet getUpgradedTable(String tableName, Map<String, String> alias)
/*     */     throws DataException, ServiceException
/*     */   {
/* 630 */     DataResultSet drset = SharedObjects.getTable(tableName);
/* 631 */     drset = upgradeFieldNames(drset, alias, true);
/* 632 */     return drset;
/*     */   }
/*     */ 
/*     */   public static DataResultSet getUpgradedAndRenamedTable(String currName, String oldName, Map<String, String> alias)
/*     */     throws DataException, ServiceException
/*     */   {
/* 648 */     DataResultSet drset = SharedObjects.getTable(currName);
/* 649 */     DataResultSet drsetOld = SharedObjects.getTable(oldName);
/*     */ 
/* 651 */     if ((drsetOld != null) && (!drsetOld.isEmpty()))
/*     */     {
/* 654 */       FieldInfo[] fi = ResultSetUtils.createInfoList(drsetOld, new String[] { "dComponentName", "idcComponentName" }, false);
/*     */ 
/* 656 */       String deprStr = "";
/* 657 */       if (fi.length > 0)
/*     */       {
/* 659 */         HashSet deprecatedComponents = new HashSet();
/* 660 */         for (drsetOld.first(); drsetOld.isRowPresent(); drsetOld.next())
/*     */         {
/* 662 */           for (FieldInfo fieldInfo : fi)
/*     */           {
/* 664 */             if (fieldInfo.m_index < 0)
/*     */               continue;
/* 666 */             deprecatedComponents.add(drsetOld.getStringValue(fieldInfo.m_index));
/*     */           }
/*     */         }
/*     */ 
/* 670 */         deprecatedComponents.remove("Default");
/* 671 */         List listDeprComps = new ArrayList();
/* 672 */         listDeprComps.addAll(deprecatedComponents);
/* 673 */         deprStr = StringUtils.createStringSimple(listDeprComps);
/*     */       }
/* 675 */       Report.warning("datastoredesign", null, "csOldTableUsed", new Object[] { oldName, currName, deprStr });
/*     */ 
/* 678 */       drsetOld = upgradeFieldNames(drsetOld, alias, true);
/* 679 */       drset.mergeFields(drsetOld);
/* 680 */       drset.merge(null, drsetOld, false);
/*     */     }
/*     */ 
/* 683 */     return drset;
/*     */   }
/*     */ 
/*     */   public static Map<String, String> getFieldNameUpgradeMap()
/*     */   {
/* 691 */     Table t = ResourceContainerUtils.getDynamicTableResource("DataStoreDesignColumnUpgradeMap");
/* 692 */     Map alias = new HashMap();
/* 693 */     if (t != null)
/*     */     {
/* 695 */       int numRows = t.getNumRows();
/* 696 */       for (int i = 0; i < numRows; ++i)
/*     */       {
/* 698 */         alias.put(t.getString(i, 0), t.getString(i, 1));
/*     */ 
/* 705 */         if (alias.containsKey(t.getString(i, 1)))
/*     */           continue;
/* 707 */         alias.put(t.getString(i, 1), t.getString(i, 1));
/*     */       }
/*     */     }
/*     */ 
/* 711 */     return alias;
/*     */   }
/*     */ 
/*     */   public static DataResultSet upgradeFieldNames(DataResultSet drset, Map<String, String> alias, boolean clone)
/*     */     throws ServiceException
/*     */   {
/* 723 */     if ((drset == null) || (alias == null) || (alias.size() == 0))
/*     */     {
/* 725 */       return drset;
/*     */     }
/*     */ 
/* 728 */     if (clone)
/*     */     {
/* 730 */       DataResultSet drsetCopy = new DataResultSet();
/* 731 */       drsetCopy.copy(drset);
/* 732 */       drset = drsetCopy;
/*     */     }
/*     */ 
/* 735 */     String[] fieldNames = ResultSetUtils.getFieldListAsStringArray(drset);
/*     */     String fieldName;
/* 736 */     for (fieldName : fieldNames)
/*     */     {
/* 738 */       Set keys = alias.keySet();
/* 739 */       for (String key : keys)
/*     */       {
/* 741 */         if (fieldName.compareToIgnoreCase(key) == 0)
/*     */         {
/* 743 */           drset.renameField(fieldName, (String)alias.get(key));
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 748 */     return drset;
/*     */   }
/*     */ 
/*     */   protected static DataResultSet filterColumnsForTable(DataResultSet drset, String compName, String tableName)
/*     */     throws ServiceException
/*     */   {
/* 770 */     drset = upgradeFieldNames(drset, getFieldNameUpgradeMap(), true);
/*     */ 
/* 772 */     DataResultSet temp = new DataResultSet();
/* 773 */     temp.copyFieldInfo(drset);
/* 774 */     temp.copySimpleFiltered(drset, "dsdComponentName", compName);
/*     */ 
/* 776 */     DataResultSet columnsdrset = new DataResultSet();
/* 777 */     columnsdrset.copyFieldInfo(drset);
/* 778 */     columnsdrset.copySimpleFiltered(temp, "dsdTableName", tableName);
/*     */ 
/* 780 */     return columnsdrset;
/*     */   }
/*     */ 
/*     */   public static void updateConfigTable(Workspace ws, List<String> compList)
/*     */     throws DataException
/*     */   {
/* 793 */     String insQueryStr = "Insert INTO Config (dSection, dName,dVersion, dValue) VALUES ";
/*     */ 
/* 795 */     String delQueryStr = "DELETE FROM Config WHERE dSection=";
/*     */ 
/* 797 */     for (String values : compList)
/*     */     {
/* 800 */       String[] split = StringUtils.makeStringArrayFromSequence(values);
/* 801 */       ws.executeSQL(delQueryStr + split[0].substring(1) + " AND dName=" + split[1]);
/*     */ 
/* 804 */       ws.executeSQL(insQueryStr + values);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean evaluateCheckFlag(String checkFlag)
/*     */   {
/* 817 */     boolean canPerformOperations = true;
/* 818 */     if ((checkFlag.length() > 0) && (!checkFlag.equalsIgnoreCase("null")))
/*     */     {
/* 820 */       String[] flags = StringUtils.makeStringArrayFromSequence(checkFlag);
/*     */ 
/* 822 */       for (String flag : flags)
/*     */       {
/* 824 */         canPerformOperations = SharedObjects.getEnvValueAsBoolean(flag, false);
/* 825 */         if (!canPerformOperations)
/*     */         {
/* 827 */           return false;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 832 */     return canPerformOperations;
/*     */   }
/*     */ 
/*     */   public static short changeToFieldType(String type)
/*     */   {
/* 842 */     short temp = 0;
/* 843 */     String lowerType = type.toLowerCase();
/*     */ 
/* 845 */     if (lowerType.contains("varchar"))
/*     */     {
/* 847 */       temp = 6;
/*     */     }
/* 849 */     else if (lowerType.contains("truefalse"))
/*     */     {
/* 851 */       temp = 1;
/*     */     }
/* 853 */     else if (lowerType.contains("integer"))
/*     */     {
/* 855 */       temp = 3;
/*     */     }
/* 857 */     else if (lowerType.contains("date"))
/*     */     {
/* 859 */       temp = 5;
/*     */     }
/* 861 */     else if (lowerType.contains("memo"))
/*     */     {
/* 863 */       temp = 8;
/*     */     }
/* 865 */     else if (lowerType.contains("blob"))
/*     */     {
/* 867 */       temp = 9;
/*     */     }
/* 869 */     else if (lowerType.contains("clob"))
/*     */     {
/* 871 */       temp = 10;
/*     */     }
/*     */ 
/* 874 */     return temp;
/*     */   }
/*     */ 
/*     */   protected static void upgradeDataDirectoryLocation()
/*     */     throws ServiceException
/*     */   {
/* 890 */     String dataDir = DirectoryLocator.getAppDataDirectory();
/* 891 */     File fromDir = FileUtilsCfgBuilder.getCfgFile(dataDir + "componentdbinstall/", null, true);
/* 892 */     File toDir = FileUtilsCfgBuilder.getCfgFile(dataDir + "datastoredesign/", null, true);
/*     */ 
/* 898 */     if ((toDir.exists()) || (!fromDir.exists()))
/*     */     {
/* 900 */       SharedObjects.putEnvironmentValue("useNewDataStoreDesignDataDir", "1");
/* 901 */       return;
/*     */     }
/*     */ 
/* 905 */     FileUtils.copyDirectoryWithFlags(fromDir, toDir, 0, null, 1);
/*     */ 
/* 912 */     SharedObjects.putEnvironmentValue("useNewDataStoreDesignDataDir", "1");
/*     */   }
/*     */ 
/*     */   public static String getDataDirectoryLocation()
/*     */   {
/* 920 */     boolean useNew = SharedObjects.getEnvValueAsBoolean("useNewDataStoreDesignDataDir", false);
/*     */ 
/* 922 */     if (useNew)
/*     */     {
/* 924 */       return "datastoredesign/";
/*     */     }
/*     */ 
/* 927 */     return "componentdbinstall/";
/*     */   }
/*     */ 
/*     */   public static String generateShortIndexName(Workspace ws, String database, String indexName)
/*     */   {
/* 932 */     String shortName = indexName;
/*     */ 
/* 934 */     if (ws != null)
/*     */     {
/* 936 */       String shortNameLen = ws.getProperty("ShortIndexNameLen");
/* 937 */       if (shortNameLen != null)
/*     */       {
/* 939 */         int shortLen = Integer.parseInt(shortNameLen);
/* 940 */         if (indexName.length() > shortLen)
/*     */         {
/* 942 */           shortName = WorkspaceUtils.getShortIndexName(indexName, shortLen);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 950 */       DataResultSet dcc = SharedObjects.getTable("DatabaseConnectionConfigurations");
/* 951 */       if (dcc != null)
/*     */       {
/* 953 */         Vector row = dcc.findRow(0, "UseDatabaseShortIndexName");
/* 954 */         boolean useShortNames = StringUtils.convertToBool((String)row.get(2), false);
/* 955 */         if (useShortNames)
/*     */         {
/* 958 */           row = dcc.findRow(0, "(" + database.toUpperCase() + ")ShortIndexNameLen");
/* 959 */           if (row != null)
/*     */           {
/* 961 */             int shortLen = Integer.parseInt((String)row.get(2));
/* 962 */             if (indexName.length() > shortLen)
/*     */             {
/* 964 */               shortName = WorkspaceUtils.getShortIndexName(indexName, shortLen);
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/* 971 */             row = dcc.findRow(0, "DefaultShortIndexNameLen");
/* 972 */             if (row != null)
/*     */             {
/* 974 */               int shortLen = Integer.parseInt((String)row.get(2));
/* 975 */               if (indexName.length() > shortLen)
/*     */               {
/* 977 */                 shortName = WorkspaceUtils.getShortIndexName(indexName, shortLen);
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 990 */     return shortName;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 995 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97093 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.datastoredesign.DataDesignInstallUtils
 * JD-Core Version:    0.5.4
 */