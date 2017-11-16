/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.DynamicData;
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.PathUtils;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ReportSubProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.utils.ComponentListEditor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class MigrateUtils
/*     */ {
/*  51 */   public static String[] MIGRATORS = { "recordsmanagement.RmaMigrator", "intradoc.apputilities.installer.Migrator" };
/*     */ 
/*     */   public static Migrator createMigrator()
/*     */   {
/*  66 */     for (int m = 0; m < MIGRATORS.length; ++m)
/*     */     {
/*  68 */       String classname = MIGRATORS[m];
/*     */       try
/*     */       {
/*  71 */         Class cl = Class.forName(classname);
/*  72 */         if (!Migrator.class.isAssignableFrom(cl))
/*     */         {
/*  74 */           String msg = new StringBuilder().append(classname).append(" is not an instance of ").append(Migrator.class.getName()).toString();
/*  75 */           throw new RuntimeException(msg);
/*     */         }
/*  77 */         Migrator migrator = (Migrator)cl.newInstance();
/*  78 */         return migrator;
/*     */       }
/*     */       catch (ClassNotFoundException cnfe)
/*     */       {
/*     */       }
/*     */       catch (IllegalAccessException iae)
/*     */       {
/*  87 */         throw new RuntimeException(iae);
/*     */       }
/*     */       catch (InstantiationException ie)
/*     */       {
/*  91 */         throw new RuntimeException(ie);
/*     */       }
/*     */     }
/*  94 */     throw new RuntimeException(new StringBuilder().append("No ").append(Migrator.class.getName()).append(" found.").toString());
/*     */   }
/*     */ 
/*     */   public static ComponentListEditor createComponentListEditor(Properties env)
/*     */     throws DataException, ServiceException
/*     */   {
/* 109 */     String intradocDir = env.getProperty("IntradocDir");
/* 110 */     String configDir = env.getProperty("ConfigDir");
/* 111 */     String componentDir = new StringBuilder().append(env.get("DataDir")).append("components/").toString();
/* 112 */     String homeDir = env.getProperty("IdcHomeDir");
/*     */ 
/* 114 */     ComponentListEditor components = new ComponentListEditor();
/* 115 */     components.init(intradocDir, configDir, componentDir, homeDir, env);
/*     */ 
/* 117 */     return components;
/*     */   }
/*     */ 
/*     */   public static ReportSubProgress createReportSubProgress(MigrationEnvironment env)
/*     */   {
/* 123 */     int currentCount = env.m_maximumProgressUnits;
/* 124 */     ReportSubProgress progress = new ReportSubProgress(env.m_reportProgress, currentCount, 0);
/* 125 */     env.m_reportSubProgresses.add(progress);
/* 126 */     return progress;
/*     */   }
/*     */ 
/*     */   public static String createProgressString(String msg, float amtDone, float max)
/*     */   {
/* 137 */     StringBuilder line = new StringBuilder();
/* 138 */     if (max > 0.0F)
/*     */     {
/* 140 */       int tenthpercent = (int)(amtDone / max * 1000.0F + 0.5D);
/* 141 */       if (tenthpercent < 1000)
/*     */       {
/* 143 */         if (tenthpercent < 100)
/*     */         {
/* 145 */           line.append("  ");
/*     */         }
/*     */         else
/*     */         {
/* 149 */           line.append(' ');
/*     */         }
/*     */       }
/* 152 */       line.append(Integer.toString(tenthpercent / 10));
/* 153 */       line.append('.');
/* 154 */       line.append(Integer.toString(tenthpercent % 10));
/* 155 */       line.append("%: ");
/*     */     }
/* 157 */     line.append(msg);
/* 158 */     return line.toString();
/*     */   }
/*     */ 
/*     */   public static void updateProgress(MigrateItem item, int currentItemListIndex, Object[] extraText)
/*     */   {
/* 174 */     MigrationEnvironment env = item.m_environment;
/* 175 */     List items = env.m_items;
/* 176 */     int numItems = items.size();
/* 177 */     int currentProgressUnits = 0;
/* 178 */     for (int i = 0; i < numItems; ++i)
/*     */     {
/* 180 */       MigrateItem previousItem = (MigrateItem)items.get(i);
/* 181 */       if (item == previousItem)
/*     */       {
/* 183 */         env.m_currentItemIndex = i;
/* 184 */         break;
/*     */       }
/* 186 */       currentProgressUnits += previousItem.m_progressUnits;
/*     */     }
/* 188 */     int currentItemProgressUnits = item.m_currentProgress;
/* 189 */     if (currentItemProgressUnits < 0)
/*     */     {
/* 191 */       currentItemProgressUnits = 0;
/*     */     }
/* 193 */     if (currentItemProgressUnits > item.m_progressUnits)
/*     */     {
/* 195 */       currentItemProgressUnits = item.m_progressUnits;
/*     */     }
/* 197 */     currentProgressUnits += currentItemProgressUnits;
/* 198 */     env.m_currentProgressUnits = currentProgressUnits;
/* 199 */     List detailsList = item.m_detailsList;
/* 200 */     int numListItems = (detailsList != null) ? detailsList.size() : 1;
/* 201 */     if (currentItemListIndex < 0)
/*     */     {
/* 203 */       currentItemListIndex = 0;
/*     */     }
/* 205 */     if (currentItemListIndex > numListItems)
/*     */     {
/* 207 */       currentItemListIndex = numListItems;
/*     */     }
/* 209 */     boolean hasExtraText = extraText.length > 0;
/* 210 */     boolean isVerbose = SystemUtils.m_verbose;
/* 211 */     boolean isReportable = (isVerbose) || (hasExtraText);
/* 212 */     boolean isNewItem = currentItemListIndex == 0;
/* 213 */     if ((isNewItem) || (currentItemListIndex != env.m_currentItemDisplayListIndex))
/*     */     {
/* 215 */       env.m_currentItemDisplayListIndex = currentItemListIndex;
/* 216 */       isReportable |= isNewItem;
/*     */     }
/* 218 */     ReportProgress reporter = env.m_reportProgress;
/* 219 */     if ((!isReportable) || (reporter == null))
/*     */       return;
/* 221 */     float current = currentProgressUnits; float maximum = env.m_maximumProgressUnits;
/* 222 */     if (isNewItem)
/*     */     {
/* 224 */       reporter.reportProgress(1, item.m_name, current, maximum);
/*     */     }
/* 226 */     if ((isVerbose) && (!isNewItem) && (!hasExtraText))
/*     */     {
/* 228 */       String msg = (detailsList != null) ? (String)detailsList.get(currentItemListIndex - 1) : item.m_name;
/* 229 */       reporter.reportProgress(3, msg, current, maximum);
/*     */     }
/* 231 */     if (!hasExtraText)
/*     */       return;
/* 233 */     StringBuilder msg = new StringBuilder();
/* 234 */     for (Object extra : extraText)
/*     */     {
/* 236 */       msg.append(extra);
/*     */     }
/* 238 */     reporter.reportProgress(3, msg.toString(), current, maximum);
/*     */   }
/*     */ 
/*     */   public static void lcAppendToDetailsListForItemWithKey(MigrateItem item, String key, String value)
/*     */   {
/* 253 */     String lcValue = lcValueForItemWithKey(item, key, value);
/* 254 */     item.m_detailsList.add(lcValue);
/*     */   }
/*     */ 
/*     */   public static void lcAppendToSummaryListForItemWithKey(MigrateItem item, String key, String value)
/*     */   {
/* 267 */     String lcValue = lcValueForItemWithKey(item, key, value);
/* 268 */     item.m_summaryList.add(lcValue);
/*     */   }
/*     */ 
/*     */   public static String lcFindKeyForItemWithSuffix(MigrateItem item, String keyName, String keySuffix)
/*     */   {
/* 280 */     String key = (String)item.m_params.get(keyName);
/* 281 */     if (key == null)
/*     */     {
/* 283 */       key = new StringBuilder().append("csMigrateTask_").append(item.m_id).append(keySuffix).toString();
/*     */     }
/* 285 */     return key;
/*     */   }
/*     */ 
/*     */   public static String lcValueForItemWithKey(MigrateItem item, String key, String value)
/*     */   {
/* 296 */     ExecutionContext cxt = item.m_environment.m_context;
/* 297 */     if (LocaleResources.getStringInternal(key, cxt) == null)
/*     */     {
/* 299 */       return value;
/*     */     }
/* 301 */     String str = LocaleResources.getString(key, cxt, value);
/* 302 */     return str;
/*     */   }
/*     */ 
/*     */   public static DataResultSet getTable(MigrationEnvironment env, String tableName)
/*     */     throws ServiceException
/*     */   {
/* 317 */     DataResultSet drset = SharedObjects.getTable(tableName);
/* 318 */     return (drset != null) ? drset : getDynamicDataAsDataResultSet(env, tableName);
/*     */   }
/*     */ 
/*     */   public static DataResultSet getDynamicDataAsDataResultSet(MigrationEnvironment env, String ddTableName)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 333 */       DynamicData dd = env.m_merger.getDynamicDataResource(ddTableName, null);
/* 334 */       if (dd == null)
/*     */       {
/* 336 */         return null;
/*     */       }
/* 338 */       Table table = dd.m_mergedTable;
/* 339 */       DataResultSet drset = new DataResultSet();
/* 340 */       drset.init(table);
/* 341 */       return drset;
/*     */     }
/*     */     catch (ParseSyntaxException pse)
/*     */     {
/* 345 */       throw new ServiceException(pse);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String evalQuery(MigrationEnvironment env, String queryName)
/*     */     throws DataException
/*     */   {
/* 359 */     String tableName = new StringBuilder().append(env.m_migrateType).append("Queries").toString();
/* 360 */     DataResultSet table = SharedObjects.getTable(tableName);
/* 361 */     String queryStr = ResultSetUtils.findValue(table, "queryName", queryName, "queryString");
/*     */     try
/*     */     {
/* 364 */       return env.m_merger.evaluateScript(queryStr);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 368 */       IdcMessage msg = new IdcMessage("csUnableToEvalScript", new Object[] { queryStr });
/* 369 */       throw new DataException(e, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String computeSourcePrefix(MigrateItem item)
/*     */     throws ServiceException
/*     */   {
/* 387 */     MigrationEnvironment env = item.m_environment;
/* 388 */     String intradocDir = env.m_sourceIntradocDir;
/* 389 */     String sourcePrefix = (String)item.m_params.get("sourcePrefix");
/* 390 */     if ((sourcePrefix == null) || (sourcePrefix.length() == 0))
/*     */     {
/* 392 */       return intradocDir;
/*     */     }
/* 394 */     Properties props = (Properties)env.m_other.get("sourceEnvironment");
/* 395 */     if (props == null)
/*     */     {
/* 397 */       props = new IdcProperties(env.m_binder.getLocalData(), env.m_sourceEnvironment);
/* 398 */       env.m_other.put("sourceEnvironment", props);
/*     */     }
/* 400 */     sourcePrefix = PathUtils.substitutePathVariables(sourcePrefix, props, null, 0, env.m_context);
/* 401 */     sourcePrefix = FileUtils.getAbsolutePath(intradocDir, sourcePrefix);
/* 402 */     return sourcePrefix;
/*     */   }
/*     */ 
/*     */   public static String computeTargetPrefix(MigrateItem item)
/*     */     throws ServiceException
/*     */   {
/* 417 */     MigrationEnvironment env = item.m_environment;
/* 418 */     String intradocDir = env.m_targetIntradocDir;
/* 419 */     String targetPrefix = (String)item.m_params.get("targetPrefix");
/* 420 */     if ((targetPrefix == null) || (targetPrefix.length() == 0))
/*     */     {
/* 422 */       return intradocDir;
/*     */     }
/* 424 */     Properties props = (Properties)env.m_other.get("targetEnvironment");
/* 425 */     if (props == null)
/*     */     {
/* 427 */       props = new IdcProperties(env.m_binder.getLocalData(), env.m_targetEnvironment);
/* 428 */       env.m_other.put("targetEnvironment", props);
/*     */     }
/* 430 */     targetPrefix = PathUtils.substitutePathVariables(targetPrefix, props, null, 0, env.m_context);
/* 431 */     targetPrefix = FileUtils.getAbsolutePath(intradocDir, targetPrefix);
/* 432 */     return targetPrefix;
/*     */   }
/*     */ 
/*     */   public static List<String> walkAndCreatePathsList(String prefix, String path)
/*     */     throws ServiceException
/*     */   {
/* 445 */     List filelist = new ArrayList();
/* 446 */     if (path.endsWith("/"))
/*     */     {
/* 448 */       path = path.substring(0, path.length() - 1);
/*     */     }
/*     */     try
/*     */     {
/* 452 */       walkAndAppendPathsToList(prefix, path, filelist);
/*     */     }
/*     */     catch (IOException ioe)
/*     */     {
/* 456 */       throw new ServiceException(ioe);
/*     */     }
/* 458 */     return filelist;
/*     */   }
/*     */ 
/*     */   public static void walkAndAppendPathsToList(String prefix, String path, List<String> paths)
/*     */     throws IOException
/*     */   {
/* 472 */     File f = new File(new StringBuilder().append(prefix).append(path).toString());
/* 473 */     if (f.isFile())
/*     */     {
/* 475 */       paths.add(path);
/*     */     } else {
/* 477 */       if (!f.isDirectory())
/*     */         return;
/* 479 */       String[] entries = f.list();
/* 480 */       Arrays.sort(entries);
/* 481 */       int numEntries = entries.length;
/* 482 */       for (int e = 0; e < numEntries; ++e)
/*     */       {
/* 484 */         String entry = new StringBuilder().append(path).append('/').append(entries[e]).toString();
/* 485 */         walkAndAppendPathsToList(prefix, entry, paths);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void walkAndAppendPathsToList(File path, List<String> paths)
/*     */     throws IOException
/*     */   {
/* 499 */     if (path.isDirectory())
/*     */     {
/* 501 */       File[] entries = path.listFiles();
/* 502 */       Arrays.sort(entries);
/* 503 */       int numEntries = entries.length;
/* 504 */       for (int e = 0; e < numEntries; ++e)
/*     */       {
/* 506 */         File entry = entries[e];
/* 507 */         if (entry.isFile())
/*     */         {
/* 509 */           paths.add(entry.getCanonicalPath());
/*     */         } else {
/* 511 */           if (!path.isDirectory())
/*     */             continue;
/* 513 */           walkAndAppendPathsToList(entry, paths);
/*     */         }
/*     */       }
/*     */     } else {
/* 517 */       if (!path.isFile())
/*     */         return;
/* 519 */       paths.add(path.getCanonicalPath());
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String prunePathToDepth(String path, int depth)
/*     */   {
/* 530 */     int foundDepth = 0; int index = -1;
/*     */     do if ((index = path.indexOf(47, index + 1)) < 0)
/*     */         break;
/* 533 */     while (++foundDepth < depth);
/*     */ 
/* 538 */     if (index < 0)
/*     */     {
/* 540 */       return path;
/*     */     }
/* 542 */     return path.substring(0, index + 1);
/*     */   }
/*     */ 
/*     */   public static boolean loadMigrateState(String intradocDir, String migrateType, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 558 */     if (intradocDir == null)
/*     */     {
/* 560 */       intradocDir = SharedObjects.getEnvironmentValue("IntradocDir");
/*     */     }
/* 562 */     if (binder == null)
/*     */     {
/* 564 */       binder = new DataBinder();
/*     */     }
/* 566 */     String migrateDir = new StringBuilder().append(intradocDir).append(migrateType).toString();
/* 567 */     String migrateStateFile = new StringBuilder().append(migrateType).append("_state.hda").toString();
/* 568 */     String migrateStatePath = new StringBuilder().append(migrateDir).append('/').append(migrateStateFile).toString();
/* 569 */     binder.putLocal("path", migrateStatePath);
/* 570 */     int check = FileUtils.checkFile(migrateStatePath, 1);
/* 571 */     if (check == -16)
/*     */     {
/* 573 */       return false;
/*     */     }
/* 575 */     ResourceUtils.serializeDataBinder(migrateDir, migrateStateFile, binder, false, true);
/* 576 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 582 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83315 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.MigrateUtils
 * JD-Core Version:    0.5.4
 */