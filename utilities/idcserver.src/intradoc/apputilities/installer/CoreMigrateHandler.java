/*      */ package intradoc.apputilities.installer;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ReportSubProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataFormatUtils;
/*      */ import intradoc.data.DataFormatter;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.io.IdcByteHandlerException;
/*      */ import intradoc.io.zip.IdcZipException;
/*      */ import intradoc.io.zip.IdcZipFile;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.IdcExtendedLoader;
/*      */ import intradoc.server.IdcSystemLoader;
/*      */ import intradoc.server.utils.ComponentInstaller;
/*      */ import intradoc.server.utils.ComponentListEditor;
/*      */ import intradoc.server.utils.ComponentLocationUtils;
/*      */ import intradoc.server.utils.SystemPropertiesEditor;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.zip.IdcZipFunctions;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.PrintStream;
/*      */ import java.lang.reflect.Field;
/*      */ import java.net.InetSocketAddress;
/*      */ import java.net.Socket;
/*      */ import java.net.SocketAddress;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collections;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class CoreMigrateHandler
/*      */ {
/*  791 */   public static String[] COMPONENT_BINDER_VARIABLES = { "ComponentDir" };
/*      */ 
/*  795 */   public static final String[] COMPONENTS_TABLE_FIELD_NAMES = { "componentName", "action", "reason", "messageKey", "status" };
/*      */   public static final int COMPONENT_PROGRESS_MULTIPLIER = 10;
/*      */ 
/*      */   public static void verifyTargetConfiguration(MigrateItem item)
/*      */     throws ServiceException
/*      */   {
/*   87 */     MigrationEnvironment env = item.m_environment;
/*      */ 
/*   89 */     String targetIntradocCfg = new StringBuilder().append(env.m_targetBinDir).append("intradoc.cfg").toString();
/*   90 */     SystemPropertiesEditor targetSysProps = new SystemPropertiesEditor(targetIntradocCfg);
/*   91 */     File targetIntradocCfgFile = new File(targetIntradocCfg);
/*   92 */     if (targetIntradocCfgFile.exists())
/*      */     {
/*   94 */       targetSysProps.initIdc();
/*      */     }
/*   96 */     String targetConfigCfg = targetSysProps.getCfgFile();
/*   97 */     if (targetConfigCfg == null)
/*      */     {
/*   99 */       String targetIntradocDir = FileUtils.directorySlashes(FileUtils.getParent(env.m_targetBinDir));
/*  100 */       targetConfigCfg = new StringBuilder().append(targetIntradocDir).append("config/config.cfg").toString();
/*  101 */       targetSysProps.setFilepaths(targetIntradocCfg, targetConfigCfg);
/*      */     }
/*  103 */     File targetConfigCfgFile = new File(targetConfigCfg);
/*  104 */     if (targetConfigCfgFile.exists())
/*      */     {
/*  106 */       targetSysProps.initConfig();
/*      */     }
/*  108 */     boolean isUpgrading = StringUtils.convertToBool(targetSysProps.searchForValue("IsUpgrading"), false);
/*      */ 
/*  110 */     if (isUpgrading)
/*      */     {
/*  112 */       return;
/*      */     }
/*  114 */     boolean hasIDCName = targetSysProps.searchForValue("IDC_Name") != null;
/*  115 */     boolean hasServerPort = targetSysProps.searchForValue("IntradocServerPort") != null;
/*  116 */     boolean isProvisional = StringUtils.convertToBool(targetSysProps.searchForValue("IsProvisionalServer"), false);
/*      */ 
/*  119 */     if ((!hasIDCName) || ((!hasServerPort) && (isProvisional)))
/*      */       return;
/*  121 */     throw new ServiceException(null, "csMigrateTargetAlreadyConfigured", new Object[0]);
/*      */   }
/*      */ 
/*      */   public static void checkIfServerRunning(MigrateItem item)
/*      */     throws ServiceException
/*      */   {
/*  134 */     MigrationEnvironment env = item.m_environment;
/*  135 */     SystemPropertiesEditor sysProps = env.m_sourceSysPropsEditor;
/*  136 */     String host = sysProps.searchForValue("IntradocServerHostName");
/*  137 */     String portString = sysProps.searchForValue("IntradocServerPort");
/*  138 */     int port = NumberUtils.parseInteger(portString, 4444);
/*  139 */     Socket socket = new Socket();
/*      */     boolean foundRunning;
/*      */     try
/*      */     {
/*  143 */       socket.bind(new InetSocketAddress(0));
/*  144 */       SocketAddress addr = (host != null) ? new InetSocketAddress(host, port) : new InetSocketAddress(port);
/*  145 */       socket.connect(addr);
/*  146 */       foundRunning = true;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  150 */       if (SystemUtils.m_verbose)
/*      */       {
/*  152 */         String msg = new StringBuilder().append("good, unable to connect to a running server: ").append(e.getMessage()).toString();
/*  153 */         Report.trace("installer", msg, null);
/*      */       }
/*  155 */       foundRunning = false;
/*      */     }
/*      */     try
/*      */     {
/*  159 */       socket.close();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  163 */       Report.trace("installer", "unable to close socket", e);
/*      */     }
/*  165 */     if (!foundRunning)
/*      */       return;
/*  167 */     IdcMessage msg = new IdcMessage("csMigrateServerIsRunning", new Object[] { (host != null) ? host : "", Integer.toString(port) });
/*      */ 
/*  169 */     throw new ServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   public static void checkJdbcConfig(MigrateItem item)
/*      */   {
/*  182 */     MigrationEnvironment env = item.m_environment;
/*  183 */     Properties props = env.m_sourceEnvironment;
/*  184 */     String schemaName = props.getProperty("DatabaseSchemaName");
/*  185 */     if (schemaName == null)
/*      */       return;
/*  187 */     SharedObjects.putEnvironmentValue("DatabaseSchemaName", schemaName);
/*      */   }
/*      */ 
/*      */   public static void verifyDatabaseSchema(MigrateItem item)
/*      */     throws DataException
/*      */   {
/*  199 */     MigrationEnvironment env = item.m_environment;
/*  200 */     String query = MigrateUtils.evalQuery(env, "checkForConfig10g");
/*  201 */     IdcMessage msg = new IdcMessage("csMigrateInvalid10gDatabase", new Object[0]);
/*      */     ResultSet rset;
/*      */     try
/*      */     {
/*  205 */       rset = env.m_workspace.createResultSetSQL(query);
/*      */     }
/*      */     catch (DataException de)
/*      */     {
/*  209 */       throw new DataException(de, msg);
/*      */     }
/*  211 */     if ((!rset.first()) && (!rset.isRowPresent()))
/*      */     {
/*  213 */       throw new DataException(null, msg);
/*      */     }
/*  215 */     String value = rset.getStringValueByName("dVersion");
/*  216 */     if ((value != null) && (SystemUtils.compareVersions(value, "8.0") >= 0))
/*      */       return;
/*  218 */     throw new DataException(null, msg);
/*      */   }
/*      */ 
/*      */   public static void verifySearchIndexer(MigrateItem item)
/*      */     throws DataException
/*      */   {
/*  230 */     MigrationEnvironment env = item.m_environment;
/*  231 */     SystemPropertiesEditor editor = env.m_sourceSysPropsEditor;
/*  232 */     Properties props = editor.getCfgProperties();
/*  233 */     String indexer = props.getProperty("SearchIndexerEngineName");
/*  234 */     if ((indexer == null) || (!indexer.toLowerCase().startsWith("verity")))
/*      */       return;
/*  236 */     IdcMessage msg = new IdcMessage("csMigrateSearchIndexerNotSupported", new Object[] { indexer });
/*  237 */     throw new DataException(null, msg);
/*      */   }
/*      */ 
/*      */   public static void prepareDatabase(MigrateItem item)
/*      */     throws DataException, ServiceException
/*      */   {
/*  249 */     MigrationEnvironment env = item.m_environment;
/*  250 */     IdcExtendedLoader loader = env.m_extendedLoader = new IdcExtendedLoader();
/*  251 */     ReportProgress progress = env.m_reportProgress;
/*  252 */     ReportSubProgress subprogress = loader.m_progress = MigrateUtils.createReportSubProgress(env);
/*  253 */     loader.setLoaderWorkspace(env.m_workspace);
/*  254 */     loader.m_dontResetSubProgress = true;
/*  255 */     IdcSystemLoader.m_progress = progress;
/*  256 */     IdcSystemLoader.loadSystemVariables();
/*  257 */     SharedObjects.putEnvironmentValue("DoRevClassUpgrade", "1");
/*  258 */     SharedObjects.putEnvironmentValue("SkipSchemaUpgradeForComponents", "1");
/*      */ 
/*  260 */     List detailsList = item.m_detailsList; List summaryList = item.m_summaryList;
/*  261 */     detailsList.add(item.m_name);
/*  262 */     summaryList.add(item.m_name);
/*  263 */     loader.computeDatabaseUpgradeTasks();
/*      */ 
/*  265 */     ExecutionContext cxt = env.m_context;
/*  266 */     String msg = LocaleResources.getString("csRevClassesPopulate", cxt);
/*  267 */     detailsList.add(msg);
/*  268 */     int numRevClassRows = loader.getNumRevClassRows();
/*  269 */     msg = LocaleResources.getString("csMigrateTask_populateRevClasses_summary", cxt, Integer.valueOf(numRevClassRows));
/*  270 */     summaryList.add(msg);
/*      */ 
/*  272 */     item.m_progressUnits = subprogress.m_maxProgress;
/*  273 */     item.m_currentProgress = subprogress.m_curProgress;
/*      */   }
/*      */ 
/*      */   public static void upgradeDatabase(MigrateItem item)
/*      */     throws DataException, ServiceException
/*      */   {
/*  285 */     MigrationEnvironment env = item.m_environment;
/*  286 */     IdcExtendedLoader loader = env.m_extendedLoader;
/*      */ 
/*  288 */     if ((env.m_dbType != null) && (env.m_dbType.startsWith("Microsoft")))
/*      */     {
/*  290 */       String query = MigrateUtils.evalQuery(env, "mssqlDropProcedureGetIdcCounter");
/*      */       try
/*      */       {
/*  293 */         env.m_workspace.executeSQL(query);
/*      */       }
/*      */       catch (DataException de)
/*      */       {
/*      */       }
/*      */ 
/*  299 */       query = MigrateUtils.evalQuery(env, "mssqlCreateProcedureGetIdcCounter");
/*  300 */       env.m_workspace.executeSQL(query);
/*      */     }
/*  302 */     if ((env.m_dbType != null) && (env.m_dbType.startsWith("Oracle")))
/*      */     {
/*      */       try
/*      */       {
/*  306 */         String homeDir = env.m_targetHomeDir;
/*  307 */         String sqlFile = new StringBuilder().append(homeDir).append("/database/oracle/admin/contentprocedures.sql").toString();
/*  308 */         BufferedReader br = FileUtils.openDataReader(sqlFile);
/*      */ 
/*  310 */         StringBuffer sb = new StringBuffer();
/*  311 */         while ((s = br.readLine()) != null)
/*      */         {
/*  313 */           String s;
/*  313 */           if ((s.length() < 1) || (s.startsWith("--"))) continue; if (s.startsWith("REM")) {
/*      */             continue;
/*      */           }
/*      */ 
/*  317 */           if (s.startsWith("/"))
/*      */           {
/*  319 */             env.m_workspace.executeSQL(sb.toString());
/*  320 */             sb = new StringBuffer();
/*      */           }
/*      */ 
/*  324 */           sb.append(new StringBuilder().append(s).append("\n").toString());
/*      */         }
/*      */ 
/*  327 */         br.close();
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*      */       }
/*      */     }
/*      */ 
/*  334 */     loader.performDatabaseUpgradeTasks();
/*  335 */     int units = item.m_currentProgress = item.m_progressUnits;
/*  336 */     MigrateUtils.updateProgress(item, units, new Object[0]);
/*      */   }
/*      */ 
/*      */   public static void editConfig(MigrateItem item)
/*      */     throws DataException, ServiceException
/*      */   {
/*  350 */     MigrationEnvironment env = item.m_environment;
/*  351 */     Class clUpgradeEnvironment = env.getClass();
/*  352 */     SystemPropertiesEditor editor = env.m_targetSysPropsEditor;
/*  353 */     String varsTableName = (String)item.m_params.get("variablesTable");
/*  354 */     DataResultSet varsTable = MigrateUtils.getDynamicDataAsDataResultSet(env, varsTableName);
/*  355 */     String[] fieldNames = { "name", "value", "action" };
/*  356 */     FieldInfo[] fields = ResultSetUtils.createInfoList(varsTable, fieldNames, true);
/*  357 */     int nameIndex = fields[0].m_index; int valueIndex = fields[1].m_index; int flagsIndex = fields[2].m_index;
/*      */ 
/*  359 */     for (varsTable.first(); varsTable.isRowPresent(); varsTable.next())
/*      */     {
/*  361 */       String name = varsTable.getStringValue(nameIndex);
/*  362 */       String newValue = varsTable.getStringValue(valueIndex);
/*  363 */       String actionString = varsTable.getStringValue(flagsIndex);
/*      */ 
/*  365 */       boolean isNamePrefixMatch = name.endsWith("*");
/*  366 */       if (isNamePrefixMatch)
/*      */       {
/*  368 */         name = name.substring(0, name.length() - 1);
/*      */       }
/*      */ 
/*  374 */       boolean willCommentOut = false;
/*      */ 
/*  376 */       boolean willEditValue = false;
/*  377 */       if (actionString.equals("commentOut"))
/*      */       {
/*  379 */         boolean willSetValue = false;
/*  380 */         willCommentOut = true;
/*      */       }
/*  382 */       else if (actionString.equals("copySource"))
/*      */       {
/*  384 */         boolean willSetValue = true;
/*  385 */         newValue = env.m_sourceEnvironment.getProperty(name);
/*      */       }
/*      */       else
/*      */       {
/*      */         boolean willSetValue;
/*  387 */         if (actionString.equals("remove"))
/*      */         {
/*  389 */           willSetValue = false;
/*      */         }
/*      */         else
/*      */         {
/*      */           boolean willSetValue;
/*  391 */           if (actionString.equals("setFromEnv"))
/*      */           {
/*      */             try
/*      */             {
/*  395 */               Field field = clUpgradeEnvironment.getField(newValue);
/*  396 */               Object obj = field.get(env);
/*  397 */               newValue = (obj == null) ? "" : obj.toString();
/*      */             }
/*      */             catch (NoSuchFieldException nsfe)
/*      */             {
/*  401 */               throw new ServiceException(nsfe);
/*      */             }
/*      */             catch (IllegalAccessException iae)
/*      */             {
/*  405 */               throw new ServiceException(iae);
/*      */             }
/*  407 */             willSetValue = true;
/*      */           }
/*      */           else
/*      */           {
/*      */             boolean willSetValue;
/*  409 */             if (actionString.equals("setValue"))
/*      */             {
/*  411 */               willSetValue = true;
/*      */             }
/*  413 */             else if (actionString.equals("editValue"))
/*      */             {
/*  415 */               boolean willSetValue = false;
/*  416 */               willEditValue = true;
/*      */             }
/*      */             else
/*      */             {
/*  420 */               Report.trace("installer", new StringBuilder().append("unknown migrate action \"").append(actionString).append("\" for ").append(name).toString(), null);
/*  421 */               continue;
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */       boolean willSetValue;
/*  425 */       boolean isCfg = false; boolean isFound = false;
/*      */       do
/*      */       {
/*  428 */         Properties props = (isCfg) ? editor.getCfgProperties() : editor.getIdcProperties();
/*  429 */         List keys = (isCfg) ? editor.getCfgVector() : editor.getIdcVector();
/*  430 */         List extra = (isCfg) ? editor.getCfgExtra() : editor.getIdcExtra();
/*      */ 
/*  432 */         int extraSize = extra.size();
/*  433 */         for (int e = 0; e < extraSize; ++e)
/*      */         {
/*  435 */           String extraName = (String)extra.get(e);
/*  436 */           String extraValue = "";
/*  437 */           int index = extraName.indexOf(61);
/*  438 */           if (index != -1)
/*      */           {
/*  440 */             extraValue = extraName.substring(index + 1, extraName.length());
/*  441 */             extraName = extraName.substring(0, index);
/*      */           }
/*  443 */           if (extraName.charAt(0) == '#') {
/*      */             continue;
/*      */           }
/*      */ 
/*  447 */           boolean isMatch = (isNamePrefixMatch) ? extraName.startsWith(name) : extraName.equals(name);
/*  448 */           if (!isMatch) {
/*      */             continue;
/*      */           }
/*      */ 
/*  452 */           isFound = true;
/*  453 */           if ((willSetValue) || (willEditValue))
/*      */           {
/*  455 */             extra.set(e, new StringBuilder().append(extraName).append('=').append(newValue).toString());
/*      */           }
/*  457 */           else if (willCommentOut)
/*      */           {
/*  459 */             extra.set(e, new StringBuilder().append("##REMOVED##").append(extraName).append('=').append(extraValue).toString());
/*      */           }
/*      */           else
/*      */           {
/*  463 */             extra.remove(e);
/*  464 */             --e;
/*      */           }
/*      */         }
/*      */ 
/*  468 */         int keySize = keys.size();
/*  469 */         for (int k = 0; k < keySize; ++k)
/*      */         {
/*  471 */           String key = (String)keys.get(k);
/*  472 */           boolean isMatch = (isNamePrefixMatch) ? key.startsWith(name) : key.equals(name);
/*  473 */           if (!isMatch) {
/*      */             continue;
/*      */           }
/*      */ 
/*  477 */           isFound = true;
/*  478 */           if (willSetValue)
/*      */           {
/*  480 */             props.setProperty(key, newValue);
/*      */           }
/*      */           else
/*      */           {
/*  484 */             if (willCommentOut)
/*      */             {
/*  486 */               String oldValue = props.getProperty(key);
/*  487 */               if (oldValue == null) {
/*      */                 continue;
/*      */               }
/*      */ 
/*  491 */               oldValue = StringUtils.encodeLiteralStringEscapeSequence(oldValue);
/*  492 */               extra.add(new StringBuilder().append("##REMOVED##").append(key).append('=').append(oldValue).toString());
/*      */             }
/*  494 */             props.remove(key);
/*      */           }
/*      */         }
/*  497 */         isCfg = !isCfg;
/*  498 */       }while (isCfg);
/*  499 */       if ((isFound) || (!willSetValue))
/*      */         continue;
/*  501 */       Properties props = editor.getCfgProperties();
/*  502 */       List keys = editor.getCfgVector();
/*  503 */       props.setProperty(name, newValue);
/*  504 */       keys.add(name);
/*      */     }
/*      */ 
/*  509 */     if (item.m_progressUnits <= 2)
/*      */       return;
/*  511 */     item.m_currentProgress += 1;
/*      */   }
/*      */ 
/*      */   public static void saveConfig(MigrateItem item)
/*      */     throws ServiceException
/*      */   {
/*  523 */     MigrationEnvironment env = item.m_environment;
/*  524 */     SystemPropertiesEditor editor = env.m_targetSysPropsEditor;
/*      */ 
/*  526 */     MigrateUtils.updateProgress(item, 1, new Object[0]);
/*  527 */     String parentDir = FileUtils.getParent(editor.getIdcFile());
/*  528 */     FileUtils.checkOrCreateDirectory(parentDir, 3);
/*  529 */     editor.setIdcWritable();
/*  530 */     editor.saveIdc();
/*  531 */     item.m_currentProgress += 1;
/*      */ 
/*  533 */     MigrateUtils.updateProgress(item, 2, new Object[0]);
/*  534 */     parentDir = FileUtils.getParent(editor.getCfgFile());
/*  535 */     FileUtils.checkOrCreateDirectory(parentDir, 3);
/*  536 */     editor.setConfigWritable();
/*  537 */     editor.saveConfig();
/*  538 */     item.m_currentProgress += 1;
/*      */   }
/*      */ 
/*      */   public static void invalidateSourceConfig(MigrateItem item)
/*      */     throws ServiceException
/*      */   {
/*  550 */     MigrationEnvironment env = item.m_environment;
/*      */ 
/*  553 */     String configFilename = env.m_sourceSysPropsEditor.getCfgFile();
/*  554 */     String configDir = FileUtils.directorySlashes(FileUtils.getParent(configFilename));
/*  555 */     String configBackupFilename = new StringBuilder().append(configDir).append("config-backup-").append(env.m_startDateString).append(".cfg").toString();
/*  556 */     int flags = 4;
/*      */     try
/*      */     {
/*  559 */       FileUtils.renameFileEx(configFilename, configBackupFilename, flags);
/*      */     }
/*      */     catch (ServiceException se)
/*      */     {
/*  563 */       flags |= 8;
/*      */       try
/*      */       {
/*  566 */         FileUtils.renameFileEx(configFilename, configBackupFilename, flags);
/*      */       }
/*      */       catch (ServiceException ignore)
/*      */       {
/*  570 */         throw se;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  575 */     String noteKey = (String)item.m_params.get("noteKey");
/*  576 */     String noteLocalized = LocaleResources.getString(noteKey, env.m_context, configBackupFilename);
/*  577 */     List note = StringUtils.parseArray(noteLocalized, '\n', '\n');
/*  578 */     int numNoteLines = note.size();
/*  579 */     StringBuilder sb = new StringBuilder();
/*  580 */     for (int l = 0; l < numNoteLines; ++l)
/*      */     {
/*  582 */       String line = (String)note.get(l);
/*  583 */       if (line.length() > 0)
/*      */       {
/*  585 */         sb.append("# ");
/*  586 */         sb.append(line);
/*  587 */         sb.append('\n');
/*      */       }
/*      */       else
/*      */       {
/*  591 */         sb.append("#\n");
/*      */       }
/*      */     }
/*  594 */     File configFile = new File(configFilename);
/*  595 */     IdcMessage errorMsg = IdcMessageFactory.lc("syUnableToWriteFile", new Object[] { configFilename });
/*  596 */     FileUtils.writeFile(sb.toString(), configFile, null, 0, errorMsg);
/*      */   }
/*      */ 
/*      */   public static void prepareBackup(MigrateItem item)
/*      */     throws ServiceException
/*      */   {
/*  609 */     String targetPrefix = MigrateUtils.computeTargetPrefix(item);
/*  610 */     List filelist = MigrateUtils.walkAndCreatePathsList(targetPrefix, item.m_spec);
/*  611 */     item.m_other.put("backupList", filelist);
/*  612 */     int numFiles = filelist.size();
/*  613 */     if (numFiles <= 0)
/*      */       return;
/*  615 */     String key = "csMigrateTaskOp_backup";
/*  616 */     for (String filename : filelist)
/*      */     {
/*  618 */       MigrateUtils.lcAppendToDetailsListForItemWithKey(item, "csMigrateTaskOp_backup", filename);
/*      */     }
/*  620 */     MigrateUtils.lcAppendToSummaryListForItemWithKey(item, "csMigrateTaskOp_backup", item.m_spec);
/*  621 */     item.m_progressUnits += numFiles;
/*      */   }
/*      */ 
/*      */   public static void performBackup(MigrateItem item)
/*      */     throws ServiceException
/*      */   {
/*  634 */     MigrationEnvironment env = item.m_environment;
/*  635 */     List filelist = (List)item.m_other.get("backupList");
/*  636 */     if (filelist.size() == 0)
/*      */     {
/*  638 */       return;
/*      */     }
/*  640 */     FileUtils.checkOrCreateDirectory(env.m_targetBackupDir, 0);
/*  641 */     String itemSpec = item.m_spec;
/*      */ 
/*  643 */     String targetPrefix = MigrateUtils.computeTargetPrefix(item);
/*  644 */     File source = new File(targetPrefix, itemSpec);
/*  645 */     String sourceName = source.getName();
/*  646 */     StringBuilder targetName = new StringBuilder(sourceName);
/*  647 */     targetName.append('-');
/*  648 */     targetName.append(env.m_startDateString);
/*  649 */     if (env.m_tryRenameBackupFirst)
/*      */     {
/*  651 */       File target = new File(env.m_targetBackupDir, targetName.toString());
/*  652 */       boolean didRename = source.renameTo(target);
/*  653 */       if (SystemUtils.m_verbose)
/*      */       {
/*  655 */         String resultStr = (didRename) ? "successful" : "failed; trying zip instead";
/*  656 */         String msg = new StringBuilder().append("rename from \"").append(source.toString()).append("\" to \"").append(target.toString()).append("\" ").append(resultStr).toString();
/*      */ 
/*  658 */         Report.trace("installer", msg, null);
/*      */       }
/*  660 */       if (didRename)
/*      */       {
/*  662 */         item.m_currentProgress += filelist.size();
/*  663 */         return;
/*      */       }
/*      */     }
/*  666 */     targetName.append(".zip");
/*  667 */     targetName.insert(0, env.m_targetBackupDir);
/*  668 */     Throwable t = null;
/*      */     try
/*      */     {
/*  671 */       IdcZipFile zip = IdcZipFunctions.newIdcZipFile(targetName.toString());
/*  672 */       IdcZipFunctions.addEntriesByName(zip, itemSpec, source.getCanonicalPath(), null);
/*  673 */       zip.finish(-1);
/*      */     }
/*      */     catch (IdcByteHandlerException ibhe)
/*      */     {
/*  677 */       t = ibhe;
/*      */     }
/*      */     catch (IdcZipException ize)
/*      */     {
/*  681 */       t = ize;
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*  685 */       t = ioe;
/*      */     }
/*  687 */     if (t != null)
/*      */     {
/*  689 */       throw new ServiceException(null, "csMigrateBackupFailed", new Object[] { targetName });
/*      */     }
/*  691 */     if (SystemUtils.m_verbose)
/*      */     {
/*  693 */       String msg = new StringBuilder().append("backup \"").append(targetName).append("\" created succesfully").toString();
/*  694 */       Report.trace("installer", msg, null);
/*      */     }
/*  696 */     if (!env.m_tryDeleteAfterZip)
/*      */       return;
/*  698 */     for (int index = filelist.size() - 1; index >= 0; --index)
/*      */     {
/*  700 */       String filename = (String)filelist.get(index);
/*  701 */       File file = new File(targetPrefix, filename);
/*  702 */       if ((!file.exists()) || 
/*  704 */         (file.delete()))
/*      */         continue;
/*  706 */       IdcMessage msg = new IdcMessage("syUnableToDeleteFile", new Object[] { file.getPath() });
/*  707 */       if (env.m_shouldAbortIfDeleteAfterZipFails)
/*      */       {
/*  709 */         throw new ServiceException(null, msg);
/*      */       }
/*  711 */       Report.trace("installer", null, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void prepareCopy(MigrateItem item)
/*      */     throws ServiceException
/*      */   {
/*  726 */     String sourcePrefix = MigrateUtils.computeSourcePrefix(item);
/*  727 */     List filelist = MigrateUtils.walkAndCreatePathsList(sourcePrefix, item.m_spec);
/*  728 */     item.m_other.put("copyList", filelist);
/*  729 */     item.m_progressUnits += filelist.size();
/*  730 */     String maxDepthString = (String)item.m_params.get("maxDisplayDepth");
/*  731 */     int maxDepth = NumberUtils.parseInteger(maxDepthString, 0);
/*  732 */     String key = "csMigrateTaskOp_copy";
/*  733 */     String lastName = "";
/*  734 */     for (String filename : filelist)
/*      */     {
/*  736 */       MigrateUtils.lcAppendToDetailsListForItemWithKey(item, "csMigrateTaskOp_copy", filename);
/*  737 */       if (maxDepth > 0)
/*      */       {
/*  739 */         filename = MigrateUtils.prunePathToDepth(filename, maxDepth);
/*  740 */         if (filename.equals(lastName)) {
/*      */           continue;
/*      */         }
/*      */ 
/*  744 */         lastName = filename;
/*      */       }
/*  746 */       MigrateUtils.lcAppendToSummaryListForItemWithKey(item, "csMigrateTaskOp_copy", filename);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void performCopy(MigrateItem item)
/*      */     throws ServiceException
/*      */   {
/*  758 */     String sourcePrefix = MigrateUtils.computeSourcePrefix(item);
/*  759 */     String targetPrefix = MigrateUtils.computeTargetPrefix(item);
/*  760 */     List filelist = (List)item.m_other.get("copyList");
/*  761 */     int numFiles = filelist.size();
/*  762 */     String lastParent = null;
/*  763 */     for (int f = 0; f < numFiles; ++f)
/*      */     {
/*  765 */       MigrateUtils.updateProgress(item, f + 1, new Object[0]);
/*  766 */       String filename = (String)filelist.get(f);
/*  767 */       String parent = FileUtils.getParent(filename);
/*  768 */       if (!parent.equals(lastParent))
/*      */       {
/*  771 */         int numParents = -1;
/*  772 */         int index = -1;
/*      */         do
/*      */         {
/*  775 */           index = filename.indexOf(47, index + 1);
/*  776 */           ++numParents;
/*      */         }
/*  778 */         while (index >= 0);
/*  779 */         FileUtils.checkOrCreateDirectory(new StringBuilder().append(targetPrefix).append(parent).toString(), numParents);
/*  780 */         lastParent = parent;
/*      */       }
/*  782 */       String sourcePath = new StringBuilder().append(sourcePrefix).append(filename).toString();
/*  783 */       String targetPath = new StringBuilder().append(targetPrefix).append(filename).toString();
/*  784 */       FileUtils.copyFileEx(sourcePath, targetPath, 0);
/*  785 */       item.m_currentProgress += 1;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadComponentLists(MigrateItem item)
/*      */     throws DataException, ServiceException
/*      */   {
/*  808 */     MigrationEnvironment env = item.m_environment;
/*  809 */     env.m_targetComponents = MigrateUtils.createComponentListEditor(env.m_targetEnvironment);
/*  810 */     ComponentListEditor sourceComponents = env.m_sourceComponents;
/*  811 */     DataResultSet componentsSet = sourceComponents.getComponentSet();
/*  812 */     int componentNameIndex = componentsSet.getFieldInfoIndex("name");
/*      */ 
/*  814 */     int numVariables = COMPONENT_BINDER_VARIABLES.length;
/*  815 */     FieldInfo[] fields = new FieldInfo[numVariables];
/*  816 */     List fieldsToAdd = new ArrayList();
/*  817 */     for (int f = 0; f < numVariables; ++f)
/*      */     {
/*  819 */       String variableName = COMPONENT_BINDER_VARIABLES[f];
/*  820 */       FieldInfo finfo = fields[f] =  = new FieldInfo();
/*  821 */       if (componentsSet.getFieldInfo(variableName, finfo))
/*      */         continue;
/*  823 */       finfo.m_name = variableName;
/*  824 */       fieldsToAdd.add(finfo);
/*      */     }
/*      */ 
/*  827 */     if (fieldsToAdd.size() > 0)
/*      */     {
/*  829 */       componentsSet.mergeFieldsWithFlags(fieldsToAdd, 2);
/*      */     }
/*  831 */     for (componentsSet.first(); componentsSet.isRowPresent(); componentsSet.next())
/*      */     {
/*  833 */       String componentName = componentsSet.getStringValue(componentNameIndex);
/*  834 */       DataBinder componentBinder = sourceComponents.getComponentData(componentName);
/*  835 */       for (int f = fields.length - 1; f >= 0; --f)
/*      */       {
/*  837 */         int fieldIndex = fields[f].m_index;
/*  838 */         String variableName = COMPONENT_BINDER_VARIABLES[f];
/*  839 */         String value = componentBinder.getLocal(variableName);
/*  840 */         if (value == null)
/*      */           continue;
/*  842 */         componentsSet.setCurrentValue(fieldIndex, value);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  847 */     env.m_binder.addResultSet("Components", componentsSet);
/*  848 */     env.m_other.put("Components", componentsSet);
/*      */   }
/*      */ 
/*      */   public static void computeAllComponentActions(MigrateItem item)
/*      */     throws DataException, ServiceException
/*      */   {
/*  867 */     MigrationEnvironment env = item.m_environment;
/*  868 */     DataResultSet componentActions = new DataResultSet(COMPONENTS_TABLE_FIELD_NAMES);
/*  869 */     String keyColumn = COMPONENTS_TABLE_FIELD_NAMES[0];
/*  870 */     String[] reasonFieldName = { "reason" };
/*  871 */     int mergeFlags = 4;
/*      */ 
/*  873 */     DataResultSet standardComponents = MigrateUtils.getTable(env, "Components");
/*  874 */     standardComponents.renameField("name", keyColumn);
/*  875 */     String[] standardValue = { "standard" };
/*  876 */     ResultSetUtils.addColumnsWithDefaultValues(standardComponents, null, standardValue, reasonFieldName);
/*  877 */     componentActions.mergeWithFlags(keyColumn, standardComponents, 4, 0);
/*      */ 
/*  879 */     DataResultSet deprecatedComponents = MigrateUtils.getTable(env, "DeprecatedComponents");
/*  880 */     String[] deprecatedValue = { "deprecated" };
/*  881 */     ResultSetUtils.addColumnsWithDefaultValues(deprecatedComponents, null, deprecatedValue, reasonFieldName);
/*  882 */     componentActions.mergeWithFlags(keyColumn, deprecatedComponents, 4, 0);
/*      */ 
/*  884 */     componentActions.fillField(1, "exclude");
/*      */ 
/*  886 */     String componentsTableName = (String)item.m_params.get("componentsTable");
/*  887 */     DataResultSet componentsTable = MigrateUtils.getTable(env, componentsTableName);
/*  888 */     componentActions.mergeWithFlags(keyColumn, componentsTable, 4, 0);
/*      */ 
/*  892 */     String[] componentFieldNames = { "componentName", "reason", "action", "messageKey" };
/*  893 */     FieldInfo[] componentFields = ResultSetUtils.createInfoList(componentActions, componentFieldNames, true);
/*      */ 
/*  896 */     DataResultSet installedComponents = (DataResultSet)env.m_other.get("Components");
/*  897 */     String[] installedComponentNames = { "name", "status", "ComponentDir" };
/*  898 */     FieldInfo[] installedComponentFields = ResultSetUtils.createInfoList(installedComponents, installedComponentNames, true);
/*      */ 
/*  901 */     DataResultSet componentActionPatterns = new DataResultSet(COMPONENTS_TABLE_FIELD_NAMES);
/*      */ 
/*  903 */     for (componentActions.first(); componentActions.isRowPresent(); componentActions.next())
/*      */     {
/*  905 */       String componentNameFromActions = componentActions.getStringValue(componentFields[0].m_index);
/*  906 */       if (!StringUtils.containsWildcards(componentNameFromActions))
/*      */         continue;
/*  908 */       componentActions.deleteCurrentRow();
/*  909 */       for (installedComponents.first(); installedComponents.isRowPresent(); installedComponents.next())
/*      */       {
/*  911 */         String componentName = installedComponents.getStringValue(installedComponentFields[0].m_index);
/*  912 */         if (!StringUtils.match(componentName, componentNameFromActions, false))
/*      */           continue;
/*  914 */         List componentRow = componentActions.getCurrentRowAsList();
/*  915 */         componentRow.set(0, componentName);
/*  916 */         componentActionPatterns.addRowWithList(componentRow);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  922 */     componentActions.merge(null, componentActionPatterns, false);
/*  923 */     ResultSetUtils.sortResultSet(componentActions, COMPONENTS_TABLE_FIELD_NAMES);
/*  924 */     env.m_other.put("AllComponentActions", componentActions);
/*      */ 
/*  926 */     item.m_detailsList.add(item.m_name);
/*      */   }
/*      */ 
/*      */   public static void computeCustomComponents(MigrateItem item)
/*      */     throws DataException, ServiceException
/*      */   {
/*  940 */     MigrationEnvironment env = item.m_environment;
/*  941 */     String sourceIntradocDir = env.m_sourceIntradocDir;
/*  942 */     int sourceIntradocDirLength = sourceIntradocDir.length();
/*      */ 
/*  944 */     Map customComponentDirs = new HashMap();
/*  945 */     String[] componentsToEnableColumns = { "componentName" };
/*  946 */     DataResultSet componentsToEnable = new DataResultSet(componentsToEnableColumns);
/*  947 */     DataResultSet allComponentActions = (DataResultSet)env.m_other.get("AllComponentActions");
/*  948 */     DataResultSet componentActions = new DataResultSet(COMPONENTS_TABLE_FIELD_NAMES);
/*  949 */     allComponentActions.createIndex(null, new int[] { 0 });
/*      */ 
/*  951 */     DataResultSet targetDisabledComponents = env.m_targetComponents.getDisabledComponentList();
/*  952 */     Vector targetDisabledComponentNamesList = ResultSetUtils.loadValuesFromSet(targetDisabledComponents, "name");
/*      */ 
/*  954 */     Collections.sort(targetDisabledComponentNamesList);
/*  955 */     Set targetDisabledComponentNamesSet = new HashSet(targetDisabledComponentNamesList);
/*      */ 
/*  957 */     DataResultSet installedComponents = (DataResultSet)env.m_other.get("Components");
/*  958 */     String[] componentFieldNames = { "name", "status", "ComponentDir" };
/*  959 */     FieldInfo[] componentFields = ResultSetUtils.createInfoList(installedComponents, componentFieldNames, true);
/*      */ 
/*  967 */     for (installedComponents.first(); installedComponents.isRowPresent(); installedComponents.next())
/*      */     {
/*  969 */       String componentName = installedComponents.getStringValue(componentFields[0].m_index);
/*  970 */       boolean isEnabled = installedComponents.getStringValue(componentFields[1].m_index).equals("Enabled");
/*  971 */       String componentDir = installedComponents.getStringValue(componentFields[2].m_index);
/*      */ 
/*  973 */       List row = allComponentActions.findRow(0, componentName, 0, 0);
/*  974 */       if (row != null)
/*      */       {
/*  976 */         if ((isEnabled) && (targetDisabledComponentNamesSet.contains(componentName)))
/*      */         {
/*  978 */           List componentNameList = componentsToEnable.createEmptyRowAsList();
/*  979 */           componentNameList.set(0, componentName);
/*  980 */           componentsToEnable.addRowWithList(componentNameList);
/*      */         }
/*  982 */         componentActions.addRowWithList(row);
/*      */       }
/*      */       else
/*      */       {
/*  987 */         if (componentDir.startsWith(sourceIntradocDir))
/*      */         {
/*  989 */           componentDir = componentDir.substring(sourceIntradocDirLength);
/*      */         }
/*  991 */         if (customComponentDirs.put(componentName, componentDir) != null)
/*      */           continue;
/*  993 */         row = componentActions.createEmptyRowAsList();
/*  994 */         row.set(0, componentName);
/*  995 */         row.set(1, "custom");
/*  996 */         componentActions.addRowWithList(row);
/*      */       }
/*      */     }
/*      */ 
/* 1000 */     componentActions.createIndex(null, new int[] { 0 });
/*      */ 
/* 1005 */     Set customComponentKeys = customComponentDirs.keySet();
/* 1006 */     List customComponentNames = new ArrayList(customComponentKeys);
/* 1007 */     Collections.sort(customComponentNames);
/* 1008 */     String key = "csMigrateTaskOp_copy";
/* 1009 */     int numCustomComponents = customComponentNames.size();
/* 1010 */     for (int c = 0; c < numCustomComponents; ++c)
/*      */     {
/* 1012 */       String componentName = (String)customComponentNames.get(c);
/* 1013 */       String componentDir = (String)customComponentDirs.get(componentName);
/* 1014 */       MigrateUtils.lcAppendToDetailsListForItemWithKey(item, key, componentDir);
/* 1015 */       MigrateUtils.lcAppendToSummaryListForItemWithKey(item, key, componentName);
/*      */     }
/* 1017 */     item.m_progressUnits = (numCustomComponents * 10);
/*      */ 
/* 1022 */     env.m_other.put("customComponentNames", customComponentNames);
/* 1023 */     env.m_other.put("componentsToEnable", componentsToEnable);
/* 1024 */     env.m_other.put("componentActions", componentActions);
/* 1025 */     env.m_binder.addResultSet("ComponentsToEnable", componentsToEnable);
/* 1026 */     env.m_binder.addResultSet("ComponentActions", componentActions);
/*      */   }
/*      */ 
/*      */   public static void saveComponentActions(MigrateItem item)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1038 */     MigrationEnvironment env = item.m_environment;
/* 1039 */     DataResultSet componentActions = (DataResultSet)env.m_other.get("componentActions");
/* 1040 */     env.m_migrateState.addResultSet("Components", componentActions);
/*      */   }
/*      */ 
/*      */   public static void updateListOfEnabledComponents(MigrateItem item)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1054 */     MigrationEnvironment env = item.m_environment;
/* 1055 */     DataResultSet componentsToEnable = (DataResultSet)env.m_other.get("componentsToEnable");
/*      */ 
/* 1057 */     DataResultSet renamedComponents = MigrateUtils.getTable(env, "RenamedComponents");
/* 1058 */     Vector oldComponentNamesList = ResultSetUtils.loadValuesFromSet(renamedComponents, "oldComponentName");
/*      */ 
/* 1060 */     Set oldComponentNamesSet = new HashSet(oldComponentNamesList);
/* 1061 */     Vector newComponentNamesList = ResultSetUtils.loadValuesFromSet(renamedComponents, "newComponentName");
/*      */ 
/* 1064 */     DataResultSet installedComponents = (DataResultSet)env.m_other.get("Components");
/* 1065 */     String[] componentFieldNames = { "name", "status" };
/* 1066 */     FieldInfo[] componentFields = ResultSetUtils.createInfoList(installedComponents, componentFieldNames, true);
/*      */ 
/* 1068 */     for (installedComponents.first(); installedComponents.isRowPresent(); installedComponents.next())
/*      */     {
/* 1070 */       String componentName = installedComponents.getStringValue(componentFields[0].m_index);
/* 1071 */       boolean isEnabled = installedComponents.getStringValue(componentFields[1].m_index).equals("Enabled");
/* 1072 */       if ((!isEnabled) || (!oldComponentNamesSet.contains(componentName)))
/*      */         continue;
/* 1074 */       List componentNameList = componentsToEnable.createEmptyRowAsList();
/* 1075 */       componentNameList.set(0, newComponentNamesList.elementAt(oldComponentNamesList.indexOf(componentName)));
/* 1076 */       componentsToEnable.addRowWithList(componentNameList);
/*      */     }
/*      */ 
/* 1080 */     String[] componentNamesArray = ResultSetUtils.createFilteredStringArrayForColumn(componentsToEnable, "componentName", null, null, false, false);
/*      */ 
/* 1082 */     String componentNames = StringUtils.createStringFromArray(componentNamesArray);
/* 1083 */     String msg = LocaleResources.getString("csMigrateTask_computeComponentLists_enabled", env.m_context, componentNames);
/* 1084 */     item.m_currentProgress += 1;
/* 1085 */     MigrateUtils.updateProgress(item, item.m_currentProgress, new Object[] { msg });
/* 1086 */     env.m_targetComponents.enableOrDisableComponentEx(componentNames, true, true);
/*      */   }
/*      */ 
/*      */   public static void packageCustomComponents(MigrateItem item)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1098 */     MigrationEnvironment env = item.m_environment;
/* 1099 */     List customComponentNames = (List)env.m_other.get("customComponentNames");
/* 1100 */     DataResultSet componentActions = (DataResultSet)env.m_other.get("componentActions");
/* 1101 */     componentActions.fillField(4, "skipped");
/*      */ 
/* 1103 */     int numComponents = customComponentNames.size();
/* 1104 */     for (int c = 0; c < numComponents; ++c)
/*      */     {
/* 1106 */       item.m_currentProgress = (10 * c);
/* 1107 */       MigrateUtils.updateProgress(item, c + 1, new Object[0]);
/* 1108 */       String componentName = (String)customComponentNames.get(c);
/* 1109 */       Vector row = (Vector)componentActions.findRow(0, componentName, 0, 0);
/* 1110 */       if (row == null)
/*      */       {
/* 1112 */         DataFormatter formatter = new DataFormatter("text,rows=-1");
/* 1113 */         IdcAppendable str = formatter.m_output;
/* 1114 */         str.append("Unable to find \"");
/* 1115 */         str.append(componentName);
/* 1116 */         str.append("\" component in actions table:\n");
/* 1117 */         int formatterFlags = 3;
/* 1118 */         DataFormatUtils.appendResultSet(formatter, "", componentActions, 3);
/* 1119 */         Report.trace("installer", formatter.toString(), null);
/* 1120 */         throw new ServiceException("!csAdminUnableToBuildComponent");
/*      */       }
/*      */       boolean wasSuccessful;
/*      */       try
/*      */       {
/* 1125 */         packageComponent(item.m_environment, componentName);
/* 1126 */         wasSuccessful = true;
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1130 */         Report.error("installer", null, e);
/* 1131 */         wasSuccessful = false;
/*      */ 
/* 1133 */         String logFilename = new StringBuilder().append(componentName).append('-').append(env.m_startDateString).append(".log").toString();
/* 1134 */         String logPathname = new StringBuilder().append(env.m_targetMigrateDir).append("components/").append(logFilename).toString();
/*      */         try
/*      */         {
/* 1137 */           String charset = "UTF-8";
/* 1138 */           PrintStream log = new PrintStream(logPathname, "UTF-8");
/* 1139 */           e.printStackTrace(log);
/* 1140 */           log.close();
/*      */         }
/*      */         catch (IOException ioe)
/*      */         {
/* 1144 */           throw new ServiceException(ioe);
/*      */         }
/* 1146 */         row.set(2, new StringBuilder().append("file:").append(logFilename).toString());
/*      */       }
/* 1148 */       row.set(4, (wasSuccessful) ? "packaged" : "failed");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void packageComponent(MigrationEnvironment env, String componentName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1165 */     String componentsDir = new StringBuilder().append(env.m_targetMigrateDir).append("components/").toString();
/* 1166 */     FileUtils.checkOrCreateDirectory(componentsDir, 0);
/*      */ 
/* 1168 */     ComponentListEditor editor = env.m_sourceComponents;
/* 1169 */     DataResultSet components = editor.getComponentSet();
/* 1170 */     int nameIndex = ResultSetUtils.getIndexMustExist(components, "name");
/* 1171 */     if (components.findRow(nameIndex, componentName) == null)
/*      */     {
/* 1173 */       IdcMessage msg = IdcMessageFactory.lc("csAdminComponentDoesNotExist", new Object[] { componentName });
/* 1174 */       throw new DataException(null, msg);
/*      */     }
/* 1176 */     Map map = components.getCurrentRowMap();
/* 1177 */     String compDefFilename = ComponentLocationUtils.determineComponentLocationWithEnv(map, 1, env.m_sourceEnvironment, false);
/*      */ 
/* 1180 */     String componentDir = FileUtils.getDirectory(compDefFilename);
/* 1181 */     String zipFilename = new StringBuilder().append(componentsDir).append(componentName).append('-').append(env.m_startDateString).append(".zip").toString();
/*      */ 
/* 1183 */     ComponentInstaller installer = new ComponentInstaller();
/* 1184 */     String manifestFilename = FileUtils.getAbsolutePath(componentDir, "manifest.hda");
/* 1185 */     if (FileUtils.checkFile(manifestFilename, true, false) < 0)
/*      */     {
/* 1187 */       IdcMessage msg = IdcMessageFactory.lc("csAdminManifestFileMissing", new Object[] { componentDir });
/* 1188 */       throw new DataException(null, msg);
/*      */     }
/* 1190 */     if (FileUtils.checkFile(compDefFilename, true, false) < 0)
/*      */     {
/* 1192 */       IdcMessage msg = IdcMessageFactory.lc("csComponentDataNotFound", new Object[] { componentName });
/* 1193 */       throw new DataException(null, msg);
/*      */     }
/* 1195 */     DataBinder manifest = ResourceUtils.readDataBinderFromPath(manifestFilename);
/* 1196 */     if (manifest == null)
/*      */     {
/* 1198 */       IdcMessage msg = IdcMessageFactory.lc("csAdminUnableToReadManifestFile", new Object[] { componentDir });
/* 1199 */       throw new DataException(null, msg);
/*      */     }
/* 1201 */     DataBinder component = ResourceUtils.readDataBinderFromPath(compDefFilename);
/* 1202 */     Map args = new HashMap();
/* 1203 */     args.put("Build", "true");
/* 1204 */     args.put("NewZipName", zipFilename);
/* 1205 */     args.put("BackupZipName", new StringBuilder().append(zipFilename).append(".bak").toString());
/* 1206 */     args.put("UseLegacyComponentDirectories", "1");
/*      */ 
/* 1210 */     installer.initForeign(componentName, component, manifest, args, editor);
/* 1211 */     installer.executeManifest();
/* 1212 */     Map exceptions = installer.getExceptions();
/* 1213 */     if ((exceptions == null) || (exceptions.isEmpty()))
/*      */       return;
/* 1215 */     ServiceException se = new ServiceException("!csAdminUnableToBuildComponent");
/* 1216 */     for (Iterator i$ = exceptions.entrySet().iterator(); i$.hasNext(); ) { Object entry = i$.next();
/*      */ 
/* 1218 */       Exception ex = (Exception)((Map.Entry)entry).getValue();
/* 1219 */       se.addCause(ex); }
/*      */ 
/* 1221 */     throw se;
/*      */   }
/*      */ 
/*      */   public static void setMigrateStateVariables(MigrateItem item)
/*      */   {
/* 1239 */     MigrationEnvironment env = item.m_environment;
/* 1240 */     DataBinder state = env.m_migrateState;
/* 1241 */     Map params = item.m_params;
/* 1242 */     String variableListString = (String)params.get("variables");
/* 1243 */     List variableList = StringUtils.makeListFromSequenceSimple(variableListString);
/* 1244 */     int numVariables = variableList.size();
/* 1245 */     for (int v = 0; v < numVariables; ++v)
/*      */     {
/* 1247 */       String key = (String)variableList.get(v);
/* 1248 */       String value = (String)params.get(key);
/* 1249 */       if (value == null)
/*      */       {
/* 1251 */         state.removeLocal(key);
/*      */       }
/*      */       else
/*      */       {
/* 1255 */         state.putLocal(key, value);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void changeForceSystemConfigPage(MigrateItem item)
/*      */     throws ServiceException
/*      */   {
/* 1267 */     MigrationEnvironment env = item.m_environment;
/* 1268 */     String forceSystemConfigPage = (String)item.m_params.get("ForceSystemConfigPage");
/* 1269 */     boolean isForceSystemConfigPage = StringUtils.convertToBool(forceSystemConfigPage, true);
/* 1270 */     File forceFile = new File(env.m_targetMigrateDir, "force_system_config_page.dat");
/*      */     try
/*      */     {
/* 1273 */       if (isForceSystemConfigPage)
/*      */       {
/* 1275 */         forceFile.createNewFile();
/*      */       }
/*      */       else
/*      */       {
/* 1279 */         forceFile.delete();
/*      */       }
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/* 1284 */       throw new ServiceException(ioe);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1292 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103658 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.CoreMigrateHandler
 * JD-Core Version:    0.5.4
 */