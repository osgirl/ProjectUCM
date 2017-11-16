/*     */ package intradoc.server.alert;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetMerge;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.File;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collections;
/*     */ import java.util.List;
/*     */ 
/*     */ public class AlertUtils
/*     */ {
/*     */   public static final int F_PERSISTANT_ALERTS = 1;
/*     */   public static final int F_TEMPORARY_ALERTS = 2;
/*     */   public static final int F_RELOAD_ALERT_FILE = 4;
/*     */   public static final String ALERT_DATA_DIR = "alerts/";
/*     */   public static final String ALERT_DATA_FILE = "alerts.hda";
/*     */   public static final String ALERT_RSET_NAME = "Alerts";
/*  87 */   public static final String[] ALERT_RSET_COLS = { "alertId", "alertMsg", "alertUrl", "role", "user" };
/*     */ 
/*  92 */   public static final String[] TRIGGER_RSET_COLS = { "key", "value" };
/*     */   protected static DataResultSet m_persistentAlertCache;
/*     */   protected static DataResultSet m_tempAlertCache;
/*     */   protected static DataResultSet m_allAlertCache;
/*     */   protected static boolean m_isAlertMonitorInitialized;
/* 105 */   protected static long m_timestamp = 0L;
/*     */ 
/* 110 */   protected static long m_loadProblemTimestamp = 0L;
/*     */   protected static final long m_reportLoadProblemInterval = 86400000L;
/*     */ 
/*     */   public static void init()
/*     */     throws ServiceException, DataException
/*     */   {
/* 123 */     getAlertData(7);
/* 124 */     m_isAlertMonitorInitialized = true;
/*     */   }
/*     */ 
/*     */   public static void reloadAlertData()
/*     */   {
/*     */     try
/*     */     {
/* 132 */       getAlertData(7);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 136 */       long curTime = System.currentTimeMillis();
/* 137 */       if (curTime - m_loadProblemTimestamp <= 86400000L)
/*     */         return;
/* 139 */       IdcMessage msg = IdcMessageFactory.lc("csAlertReloadError", new Object[0]);
/* 140 */       Report.warning("system", e, msg);
/* 141 */       m_loadProblemTimestamp = curTime;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void writeAlertData(DataResultSet rset)
/*     */     throws ServiceException
/*     */   {
/* 148 */     String dataDir = DirectoryLocator.getAppDataDirectory() + "alerts/";
/*     */     try
/*     */     {
/* 152 */       FileUtils.checkOrCreateDirectoryPrepareForLocks(dataDir, 3, true);
/* 153 */       FileUtils.reserveDirectory(dataDir);
/*     */ 
/* 156 */       DataBinder binder = new DataBinder();
/* 157 */       binder.addResultSet("Alerts", rset);
/*     */ 
/* 160 */       ResourceUtils.serializeDataBinder(dataDir, "alerts.hda", binder, true, false);
/*     */     }
/*     */     finally
/*     */     {
/* 164 */       FileUtils.releaseDirectory(dataDir);
/*     */     }
/*     */ 
/* 168 */     cleanDataCache();
/* 169 */     SubjectManager.notifyChanged("alertmonitor");
/*     */   }
/*     */ 
/*     */   public static DataResultSet getAlertData(int flags)
/*     */     throws ServiceException, DataException
/*     */   {
/* 178 */     boolean isGetPersistentAlerts = (flags & 0x1) != 0;
/* 179 */     boolean isGetTempAlerts = (flags & 0x2) != 0;
/* 180 */     boolean isGetAll = (isGetPersistentAlerts) && (isGetTempAlerts);
/*     */ 
/* 182 */     boolean isReloadFromFile = ((flags & 0x4) != 0) || (!m_isAlertMonitorInitialized);
/*     */ 
/* 184 */     if ((isGetPersistentAlerts) || (isGetAll))
/*     */     {
/* 187 */       if (isReloadFromFile)
/*     */       {
/* 189 */         String dataDir = DirectoryLocator.getAppDataDirectory() + "alerts/";
/* 190 */         File alertFile = FileUtilsCfgBuilder.getCfgFile(dataDir + "alerts.hda", "Alert", false);
/* 191 */         boolean fileExists = FileUtils.checkFile(dataDir + "alerts.hda", true, false) == 0;
/* 192 */         if (fileExists)
/*     */         {
/*     */           try
/*     */           {
/* 196 */             if (alertFile.lastModified() != m_timestamp)
/*     */             {
/* 198 */               FileUtils.reserveDirectory(dataDir);
/* 199 */               cleanDataCache();
/* 200 */               DataBinder binder = ResourceUtils.readDataBinder(dataDir, "alerts.hda");
/* 201 */               m_persistentAlertCache = (DataResultSet)binder.getResultSet("Alerts");
/* 202 */               m_timestamp = alertFile.lastModified();
/*     */             }
/*     */           }
/*     */           finally
/*     */           {
/* 207 */             FileUtils.releaseDirectory(dataDir);
/*     */           }
/*     */         }
/* 210 */         else if ((m_persistentAlertCache != null) && (m_persistentAlertCache.getNumRows() > 0))
/*     */         {
/* 212 */           m_persistentAlertCache = null;
/*     */         }
/*     */       }
/*     */ 
/* 216 */       if (m_persistentAlertCache == null)
/*     */       {
/* 218 */         cleanDataCache();
/* 219 */         m_persistentAlertCache = new DataResultSet(ALERT_RSET_COLS);
/* 220 */         m_timestamp = 0L;
/*     */       }
/*     */ 
/* 223 */       if (SystemUtils.m_verbose)
/*     */       {
/* 225 */         Report.trace("alerts", null, new IdcMessage("csAlertLoadedPersistantCache", new Object[] { m_persistentAlertCache }));
/*     */       }
/*     */ 
/* 228 */       if (!isGetAll)
/*     */       {
/* 230 */         return m_persistentAlertCache;
/*     */       }
/*     */     }
/*     */ 
/* 234 */     if (isGetTempAlerts)
/*     */     {
/* 236 */       if (m_tempAlertCache == null)
/*     */       {
/* 238 */         m_tempAlertCache = new DataResultSet(ALERT_RSET_COLS);
/*     */       }
/*     */ 
/* 241 */       if (SystemUtils.m_verbose)
/*     */       {
/* 243 */         Report.trace("alerts", null, new IdcMessage("csAlertLoadedTemporaryCache", new Object[] { m_tempAlertCache }));
/*     */       }
/*     */ 
/* 246 */       if (!isGetAll)
/*     */       {
/* 248 */         return m_tempAlertCache;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 253 */     if (m_allAlertCache == null)
/*     */     {
/* 257 */       DataResultSet tmpAllAlertDataCache = new DataResultSet(ALERT_RSET_COLS);
/*     */ 
/* 259 */       if (m_tempAlertCache.getNumRows() > 0)
/*     */       {
/* 261 */         tmpAllAlertDataCache.copy(m_tempAlertCache);
/*     */       }
/*     */ 
/* 264 */       if (m_persistentAlertCache.getNumRows() > 0)
/*     */       {
/* 266 */         ResultSetMerge rsetMerge = new ResultSetMerge(tmpAllAlertDataCache, m_persistentAlertCache, 16);
/*     */ 
/* 268 */         rsetMerge.m_colKey = "alertId";
/* 269 */         rsetMerge.merge();
/*     */       }
/*     */ 
/* 272 */       m_allAlertCache = tmpAllAlertDataCache;
/*     */ 
/* 274 */       if (SystemUtils.m_verbose)
/*     */       {
/* 276 */         Report.trace("alerts", null, new IdcMessage("csAlertLoadedEntireCache", new Object[] { m_allAlertCache }));
/*     */       }
/*     */     }
/*     */ 
/* 280 */     return m_allAlertCache.shallowClone();
/*     */   }
/*     */ 
/*     */   public static void cleanDataCache()
/*     */   {
/* 288 */     m_persistentAlertCache = null;
/* 289 */     m_allAlertCache = null;
/*     */   }
/*     */ 
/*     */   public static void setAlertSimple(String alertId, String alertMsg, String url, int typeFlag)
/*     */     throws ServiceException, DataException
/*     */   {
/* 306 */     DataBinder binder = new DataBinder();
/* 307 */     binder.putLocal("alertId", alertId);
/* 308 */     binder.putLocal("alertMsg", alertMsg);
/* 309 */     if (url != null)
/*     */     {
/* 311 */       binder.putLocal("alertUrl", url);
/*     */     }
/* 313 */     binder.putLocal("flags", "" + typeFlag);
/* 314 */     setAlert(binder);
/*     */   }
/*     */ 
/*     */   public static void setAlert(DataBinder binder)
/*     */     throws ServiceException, DataException
/*     */   {
/* 337 */     int flags = DataBinderUtils.getInteger(binder, "flags", 0);
/* 338 */     boolean isTempAlert = (flags & 0x2) != 0;
/*     */ 
/* 341 */     int getFlags = (isTempAlert) ? 2 : 1;
/* 342 */     DataResultSet alertRset = getAlertData(getFlags);
/* 343 */     if (alertRset == null)
/*     */     {
/* 345 */       alertRset = new DataResultSet(ALERT_RSET_COLS);
/*     */     }
/*     */ 
/* 349 */     String alertId = binder.getEx("alertId", false, true, false, true);
/* 350 */     String alertMsg = binder.getEx("alertMsg", false, true, false, true);
/* 351 */     String alertUrl = binder.getLocal("alertUrl");
/* 352 */     String user = binder.getLocal("user");
/* 353 */     String role = binder.getLocal("role");
/* 354 */     DataResultSet trigRset = (DataResultSet)binder.getResultSet("AlertTriggers");
/*     */ 
/* 357 */     if (alertUrl == null)
/*     */     {
/* 359 */       alertUrl = "";
/*     */     }
/* 361 */     if (user == null)
/*     */     {
/* 363 */       user = "";
/*     */     }
/* 365 */     if (role == null)
/*     */     {
/* 367 */       role = "";
/*     */     }
/* 369 */     if (trigRset == null)
/*     */     {
/* 371 */       trigRset = new DataResultSet(TRIGGER_RSET_COLS);
/*     */     }
/*     */ 
/* 375 */     String[] keys = ResultSetUtils.createFilteredStringArrayForColumn(trigRset, "key", null, null, false, false);
/* 376 */     alertRset.mergeFields(new DataResultSet(keys));
/*     */ 
/* 379 */     alertRset.mergeFields(new DataResultSet(ALERT_RSET_COLS));
/*     */ 
/* 382 */     List rowKeys = new ArrayList();
/* 383 */     rowKeys.addAll(Arrays.asList(ALERT_RSET_COLS));
/* 384 */     rowKeys.addAll(Arrays.asList(keys));
/* 385 */     FieldInfo[] fis = ResultSetUtils.createInfoList(alertRset, (String[])rowKeys.toArray(new String[0]), true);
/*     */ 
/* 388 */     List row = alertRset.findRow(fis[0].m_index, alertId, 0, 0);
/* 389 */     boolean alertExists = row != null;
/* 390 */     if (!alertExists)
/*     */     {
/* 392 */       row = new ArrayList(Arrays.asList(new String[alertRset.getNumFields()]));
/* 393 */       Collections.fill(row, "");
/*     */     }
/*     */ 
/* 397 */     row.set(fis[0].m_index, alertId);
/* 398 */     row.set(fis[1].m_index, alertMsg);
/* 399 */     row.set(fis[2].m_index, alertUrl);
/* 400 */     row.set(fis[3].m_index, role);
/* 401 */     row.set(fis[4].m_index, user);
/*     */ 
/* 403 */     int n = ALERT_RSET_COLS.length;
/* 404 */     int valIndex = trigRset.getFieldInfoIndex("value");
/* 405 */     for (trigRset.first(); trigRset.isRowPresent(); trigRset.next())
/*     */     {
/* 407 */       row.set(fis[(n++)].m_index, trigRset.getStringValue(valIndex));
/*     */     }
/*     */ 
/* 411 */     if (!alertExists)
/*     */     {
/* 413 */       alertRset.addRowWithList(row);
/*     */     }
/*     */ 
/* 416 */     if (!isTempAlert)
/*     */     {
/* 419 */       writeAlertData(alertRset);
/*     */     }
/*     */     else
/*     */     {
/* 423 */       m_allAlertCache = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void deleteAlertSimple(String alertId)
/*     */     throws ServiceException, DataException
/*     */   {
/* 436 */     DataBinder binder = new DataBinder();
/* 437 */     binder.putLocal("alertId", alertId);
/* 438 */     if (!existsAlert(alertId, 3)) {
/*     */       return;
/*     */     }
/* 441 */     deleteAlert(binder);
/*     */   }
/*     */ 
/*     */   public static void deleteAlert(DataBinder binder)
/*     */     throws ServiceException, DataException
/*     */   {
/* 455 */     String alertId = binder.getLocal("alertId");
/* 456 */     boolean tmpDeleteSuccess = deleteAlertImpl(alertId, getAlertData(2));
/* 457 */     DataResultSet persistantAlerts = getAlertData(1);
/* 458 */     boolean persistantDeleteSuccess = deleteAlertImpl(alertId, persistantAlerts);
/*     */ 
/* 460 */     if ((!tmpDeleteSuccess) && (!persistantDeleteSuccess))
/*     */     {
/* 462 */       Report.warning("alerts", null, new IdcMessage("csAlertNotDeletedDoesNotExist", new Object[] { alertId }));
/* 463 */       return;
/*     */     }
/*     */ 
/* 466 */     if (persistantDeleteSuccess)
/*     */     {
/* 469 */       writeAlertData(persistantAlerts);
/*     */     }
/*     */     else
/*     */     {
/* 473 */       m_allAlertCache = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static boolean deleteAlertImpl(String alertId, DataResultSet alertRset) throws ServiceException, DataException
/*     */   {
/* 479 */     if ((alertRset == null) || (alertRset.getNumRows() == 0))
/*     */     {
/* 481 */       return false;
/*     */     }
/*     */ 
/* 484 */     int colIndex = alertRset.getFieldInfoIndex("alertId");
/* 485 */     List row = alertRset.findRow(colIndex, alertId, 0, 0);
/* 486 */     if (row == null)
/*     */     {
/* 488 */       return false;
/*     */     }
/*     */ 
/* 492 */     alertRset.deleteCurrentRow();
/*     */ 
/* 495 */     if (alertRset.getNumRows() == 0)
/*     */     {
/* 497 */       alertRset = new DataResultSet(ALERT_RSET_COLS);
/*     */     }
/*     */     else
/*     */     {
/* 501 */       int numFields = alertRset.getNumFields();
/* 502 */       List fieldsToRemove = new ArrayList();
/* 503 */       for (int a = 0; a < numFields; ++a)
/*     */       {
/* 505 */         String fieldName = alertRset.getFieldName(a);
/*     */ 
/* 508 */         if (StringUtils.findStringIndex(ALERT_RSET_COLS, fieldName) != -1)
/*     */           continue;
/* 510 */         String[] vals = ResultSetUtils.createFilteredStringArrayForColumn(alertRset, fieldName, null, null, false, false);
/* 511 */         boolean hasVal = false;
/* 512 */         for (String val : vals)
/*     */         {
/* 514 */           if ((val == null) || (val.length() <= 0))
/*     */             continue;
/* 516 */           hasVal = true;
/* 517 */           break;
/*     */         }
/*     */ 
/* 521 */         if (hasVal)
/*     */           continue;
/* 523 */         fieldsToRemove.add(fieldName);
/*     */       }
/*     */ 
/* 528 */       alertRset.removeFields((String[])fieldsToRemove.toArray(new String[0]));
/*     */     }
/*     */ 
/* 531 */     return true;
/*     */   }
/*     */ 
/*     */   public static DataResultSet getUserAlerts(DataBinder binder, Workspace workspace, ExecutionContext cxt, UserData user)
/*     */     throws ServiceException, DataException
/*     */   {
/* 543 */     DataResultSet alertRset = getAlertData(3);
/* 544 */     DataResultSet retRset = new DataResultSet(ALERT_RSET_COLS);
/* 545 */     if ((alertRset == null) || (alertRset.getNumRows() == 0))
/*     */     {
/* 548 */       DataResultSet tempAlert = (DataResultSet)cxt.getCachedObject("oneTimeAlert");
/* 549 */       if (tempAlert != null)
/*     */       {
/* 551 */         return tempAlert;
/*     */       }
/* 553 */       return retRset;
/*     */     }
/* 555 */     FieldInfo[] fis = ResultSetUtils.createInfoList(alertRset, ALERT_RSET_COLS, true);
/*     */ 
/* 558 */     binder.addResultSet("AlertData", alertRset);
/*     */ 
/* 561 */     int userIndex = alertRset.getFieldInfoIndex("user");
/* 562 */     int roleIndex = alertRset.getFieldInfoIndex("role");
/* 563 */     int accountIndex = alertRset.getFieldInfoIndex("account");
/* 564 */     for (alertRset.first(); alertRset.isRowPresent(); alertRset.next())
/*     */     {
/* 566 */       boolean addAlert = true;
/*     */ 
/* 569 */       if (userIndex != -1)
/*     */       {
/* 571 */         String userName = alertRset.getStringValue(userIndex);
/* 572 */         if ((userName != null) && (userName.length() > 0))
/*     */         {
/* 574 */           addAlert = user.m_name.equals(userName);
/*     */         }
/*     */       }
/* 577 */       if ((addAlert) && (roleIndex != -1))
/*     */       {
/* 579 */         String role = alertRset.getStringValue(roleIndex);
/* 580 */         if ((role != null) && (role.length() > 0))
/*     */         {
/* 582 */           addAlert = SecurityUtils.isUserOfRole(user, role);
/*     */         }
/*     */       }
/* 585 */       if ((addAlert) && (SecurityUtils.m_useAccounts) && (accountIndex != -1))
/*     */       {
/* 587 */         String account = alertRset.getStringValue(accountIndex);
/* 588 */         if ((account != null) && (account.length() > 0))
/*     */         {
/* 590 */           addAlert = SecurityUtils.isAccountAccessible(user, account, 0);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 597 */       if (addAlert)
/*     */       {
/* 599 */         binder.putLocal("addAlert", (addAlert) ? "1" : "");
/* 600 */         PluginFilters.filter("checkAlertRow", workspace, binder, cxt);
/* 601 */         addAlert = DataBinderUtils.getBoolean(binder, "addAlert", addAlert);
/*     */       }
/*     */ 
/* 604 */       if (!addAlert)
/*     */         continue;
/* 606 */       List newRow = new ArrayList();
/* 607 */       for (int a = 0; a < ALERT_RSET_COLS.length; ++a)
/*     */       {
/* 609 */         newRow.add(alertRset.getStringValue(fis[a].m_index));
/*     */       }
/* 611 */       retRset.addRowWithList(newRow);
/*     */     }
/*     */ 
/* 616 */     DataResultSet tempAlert = (DataResultSet)cxt.getCachedObject("oneTimeAlert");
/* 617 */     if (tempAlert != null)
/*     */     {
/* 619 */       retRset.appendCompatibleRows(tempAlert);
/*     */     }
/*     */ 
/* 622 */     binder.removeResultSet("AlertData");
/* 623 */     return retRset;
/*     */   }
/*     */ 
/*     */   public static boolean existsAlert(String alertId, int flags)
/*     */     throws ServiceException, DataException
/*     */   {
/* 638 */     DataResultSet alertRset = null;
/* 639 */     alertRset = getAlertData(flags);
/*     */ 
/* 641 */     if ((alertRset == null) || (alertRset.getNumRows() == 0))
/*     */     {
/* 643 */       return false;
/*     */     }
/*     */ 
/* 646 */     int colIndex = alertRset.getFieldInfoIndex("alertId");
/* 647 */     List row = alertRset.findRow(colIndex, alertId, 0, 0);
/*     */ 
/* 650 */     return row != null;
/*     */   }
/*     */ 
/*     */   public static void createOneTimeAlert(IdcMessage msg, ExecutionContext cxt, String userName)
/*     */   {
/* 662 */     DataResultSet alertRset = new DataResultSet(ALERT_RSET_COLS);
/* 663 */     List row = new ArrayList(Arrays.asList(new String[ALERT_RSET_COLS.length]));
/* 664 */     Collections.fill(row, "");
/* 665 */     int userIndex = alertRset.getFieldInfoIndex("user");
/* 666 */     int alertMsgIndex = alertRset.getFieldInfoIndex("alertMsg");
/* 667 */     row.set(userIndex, userName);
/* 668 */     row.set(alertMsgIndex, "<$lcMessage('" + LocaleUtils.encodeMessage(msg) + "')$>");
/* 669 */     alertRset.addRowWithList(row);
/*     */ 
/* 671 */     DataResultSet existingAlertRset = (DataResultSet)cxt.getCachedObject("oneTimeAlert");
/* 672 */     if (existingAlertRset != null)
/*     */     {
/* 674 */       existingAlertRset.appendCompatibleRows(alertRset);
/*     */     }
/*     */     else
/*     */     {
/* 678 */       cxt.setCachedObject("oneTimeAlert", alertRset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 684 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.alert.AlertUtils
 * JD-Core Version:    0.5.4
 */