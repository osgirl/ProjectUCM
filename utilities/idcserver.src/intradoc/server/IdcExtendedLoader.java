/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IntervalData;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.LoggingUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ReportSubProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcCounter;
/*      */ import intradoc.data.IdcCounterUtils;
/*      */ import intradoc.data.MapParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.jdbc.JdbcManager;
/*      */ import intradoc.jdbc.JdbcWorkspace;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.alert.AlertUtils;
/*      */ import intradoc.server.datastoredesign.DataDesignInstall;
/*      */ import intradoc.server.datastoredesign.DataDesignProfileInstall;
/*      */ import intradoc.shared.ActiveIndexState;
/*      */ import intradoc.shared.Features;
/*      */ import intradoc.shared.MetaFieldUtils;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SearchCollections;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcException;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStreamReader;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class IdcExtendedLoader
/*      */   implements ExecutionContext
/*      */ {
/*      */   public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102962 $";
/*      */   protected Hashtable m_cachedData;
/*      */   protected Object m_taskRetVal;
/*      */   protected Workspace m_workspace;
/*      */   protected String[] m_dbTables;
/*      */   public ReportSubProgress m_progress;
/*      */   public boolean m_dontResetSubProgress;
/*      */   protected boolean m_hasReportedProgress;
/*      */   protected boolean m_isUseComponentDBTable;
/*      */   protected boolean m_doesRevClassesTableNeedUpgrade;
/*      */   protected boolean m_doRevClassExistenceCheck;
/*      */   protected int m_minRevClassBoundary;
/*      */   protected int m_maxRevClassBoundary;
/*      */   protected DataBinder m_contextBinder;
/*      */   public static final int NUM_PROGRESS_UNITS = 66;
/*      */   public static final int REV_CLASS_RECORDS_PER_QUERY = 10000;
/*      */   protected IdcLocale m_locale;
/*      */   protected String m_languageId;
/*      */   protected String m_pageEncoding;
/*      */   protected IdcDateFormat m_dateFormat;
/*      */   protected TimeZone m_timeZone;
/*      */ 
/*      */   public IdcExtendedLoader()
/*      */   {
/*  108 */     this.m_cachedData = new Hashtable();
/*      */ 
/* 3513 */     this.m_locale = null;
/* 3514 */     this.m_languageId = null;
/* 3515 */     this.m_pageEncoding = null;
/* 3516 */     this.m_dateFormat = null;
/* 3517 */     this.m_timeZone = null;
/*      */   }
/*      */ 
/*      */   public Workspace getLoaderWorkspace()
/*      */   {
/*  127 */     return this.m_workspace;
/*      */   }
/*      */ 
/*      */   public void setLoaderWorkspace(Workspace workspace)
/*      */   {
/*  133 */     this.m_workspace = workspace;
/*      */   }
/*      */ 
/*      */   public void extraAfterConfigInit()
/*      */     throws DataException, ServiceException
/*      */   {
/*  142 */     ResultSet rset = SharedObjects.getTable("DeliverableList");
/*  143 */     if ((rset == null) || (rset.isEmpty()))
/*      */     {
/*  145 */       SharedObjects.putEnvironmentValue("HasDownloadBundle", "0");
/*      */     }
/*      */     else
/*      */     {
/*  149 */       SharedObjects.putEnvironmentValue("HasDownloadBundle", "1");
/*      */     }
/*      */ 
/*  152 */     this.m_contextBinder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  153 */     executeFilter("extraAfterConfigInit", null);
/*      */   }
/*      */ 
/*      */   public void extraBeforeCacheLoadInit(Workspace workspace)
/*      */     throws DataException, ServiceException
/*      */   {
/*  178 */     this.m_workspace = workspace;
/*      */ 
/*  182 */     if (this.m_workspace instanceof JdbcWorkspace)
/*      */     {
/*  184 */       JdbcManager manager = ((JdbcWorkspace)this.m_workspace).getManager();
/*  185 */       boolean saved = manager.allowEmptyTableList();
/*  186 */       manager.setAllowEmptyTableList(false);
/*  187 */       this.m_workspace.getTableList();
/*  188 */       manager.setAllowEmptyTableList(saved);
/*      */     }
/*      */     else
/*      */     {
/*  192 */       Report.trace("null", "extraBeforeCacheLoadInit(), workspace is not a JdbcWorkspace, not testing for empty table list.", null);
/*      */     }
/*      */ 
/*  195 */     this.m_progress = new ReportSubProgress(IdcSystemLoader.m_progress, 0, 0);
/*      */ 
/*  201 */     String upgradeConfig = getDBConfigValue("DevUpdate", "DatabaseUpgrade", null);
/*  202 */     double upgradeValue = NumberUtils.parseDouble(upgradeConfig, 0.0D);
/*  203 */     if ((Features.checkLevel("JDBC", null)) && (((SharedObjects.getEnvValueAsBoolean("ForceDatabaseUpgrade", false)) || (upgradeValue >= 1.0D))))
/*      */     {
/*  207 */       computeDatabaseUpgradeTasks();
/*  208 */       performDatabaseUpgradeTasks();
/*  209 */       if (upgradeValue < 1.0D)
/*      */       {
/*  212 */         setDBConfigValue("DevUpdate", "DatabaseUpgrade", "11.0", "1");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  219 */     updateSearchCollectionsV511();
/*      */ 
/*  222 */     updateRefineryQueueFileNamesV8();
/*      */ 
/*  225 */     if (this.m_hasReportedProgress)
/*      */     {
/*  227 */       String msg = LocaleUtils.encodeMessage("csUpgradeComplete", null);
/*  228 */       reportProgress(msg, 0);
/*      */     }
/*      */ 
/*  234 */     executeFilter("extraBeforeCacheLoadInit", this.m_workspace);
/*      */ 
/*  237 */     initExecutionContext();
/*      */   }
/*      */ 
/*      */   public void computeDatabaseUpgradeTasks()
/*      */     throws DataException, ServiceException
/*      */   {
/*  252 */     this.m_progress.m_maxProgress = (this.m_progress.m_curProgress + 66);
/*  253 */     this.m_isUseComponentDBTable = SharedObjects.getEnvValueAsBoolean("isUseComponentDBInstallToCreateTable", true);
/*      */ 
/*  255 */     computeRevClassesTableUpgrade();
/*  256 */     if (!this.m_doesRevClassesTableNeedUpgrade)
/*      */       return;
/*  258 */     int numRevClassRows = getNumRevClassRows();
/*  259 */     if (numRevClassRows <= 0)
/*      */       return;
/*  261 */     this.m_progress.m_maxProgress += (numRevClassRows + 10000 - 1) / 10000;
/*      */   }
/*      */ 
/*      */   public int getNumRevClassRows()
/*      */   {
/*  269 */     return this.m_maxRevClassBoundary - this.m_minRevClassBoundary + 1;
/*      */   }
/*      */ 
/*      */   public void performDatabaseUpgradeTasks()
/*      */     throws DataException, ServiceException
/*      */   {
/*  283 */     userSecurityUpgrade();
/*      */ 
/*  286 */     accountsUpgrade();
/*      */ 
/*  290 */     aliasUserUpgrade();
/*      */ 
/*  293 */     workflowUpgrade();
/*      */ 
/*  296 */     revisionsUpgrade();
/*      */ 
/*  299 */     subscriptionUpgrade();
/*      */ 
/*  302 */     metafieldsTypeUpgrade();
/*      */ 
/*  305 */     documentsUpgrade();
/*      */ 
/*  308 */     metafieldsAddOptionListType();
/*      */ 
/*  311 */     userPasswordUpgrade();
/*      */ 
/*  314 */     renditionsUpgrade();
/*      */ 
/*  317 */     workflowHistoryUpgrade();
/*      */ 
/*  320 */     archiveHistoryUpgrade();
/*      */ 
/*  325 */     indexerUpgradeV4();
/*      */ 
/*  328 */     usersUpgradeV4();
/*      */ 
/*  331 */     projectUpgrade();
/*      */ 
/*  334 */     expandFieldLengthsV4();
/*      */ 
/*  337 */     addSysManager();
/*      */ 
/*  340 */     datedCacheUpgrade();
/*      */ 
/*  343 */     workflowScriptUpgrade();
/*      */ 
/*  346 */     usersUpgradeV5();
/*      */ 
/*  349 */     projectUpgradeV5();
/*      */ 
/*  352 */     htmlConversionUpgrade();
/*      */ 
/*  355 */     previewUpgradeV5();
/*      */ 
/*  358 */     workflowUpgradeV6();
/*      */ 
/*  361 */     collaborationUpgradeV6();
/*      */ 
/*  364 */     addCollaborationColumnsV6();
/*      */ 
/*  367 */     addInstallFiles();
/*      */ 
/*  370 */     revisionsUpgradeV7();
/*      */ 
/*  373 */     workflowStepsV7();
/*      */ 
/*  376 */     archiveTableUpgrade();
/*      */ 
/*  379 */     revisionsTableUpgrade75();
/*      */ 
/*  382 */     expandMetaFieldSizeV8();
/*      */ 
/*  385 */     expandFormatFieldLengthsV8();
/*      */ 
/*  387 */     updateDocMetaDefinitionV8();
/*      */ 
/*  390 */     updateMimeType();
/*      */ 
/*  392 */     createRefineryJobsTable();
/*      */ 
/*  395 */     updateDocMetaDefinitionV11();
/*      */ 
/*  398 */     dataStoreDesignConfigTables();
/*      */ 
/*  401 */     addDataSourceFields();
/*      */ 
/*  404 */     updateRoleDefinitionV11();
/*      */ 
/*  407 */     updateAliasV11();
/*      */ 
/*  410 */     upgradeCounters();
/*      */ 
/*  413 */     userTimeZoneUpgrade();
/*      */ 
/*  416 */     addProfileField();
/*      */ 
/*  419 */     populateRevClassesTable();
/*      */ 
/*  421 */     dMessageRepair();
/*      */ 
/*  425 */     updateDocFormatsAndExtensionFormatMap();
/*      */   }
/*      */ 
/*      */   public void extraAfterServicesLoadInit()
/*      */     throws DataException, ServiceException
/*      */   {
/*  440 */     if ((Features.checkLevel("JDBC", null)) && (this.m_workspace != null))
/*      */     {
/*  443 */       dataStoreDesignConfigDocMeta();
/*  444 */       dataStoreDesignConfigProfile();
/*  445 */       dataStoreDesignSynchronizeWithSchema();
/*      */     }
/*      */ 
/*  448 */     executeFilter("extraAfterServicesLoadInit", this.m_workspace);
/*      */   }
/*      */ 
/*      */   public void extraAfterProvidersStartedInit()
/*      */     throws DataException, ServiceException
/*      */   {
/*  455 */     executeFilter("extraAfterProvidersStartedInit", this.m_workspace);
/*      */   }
/*      */ 
/*      */   public void executeFilter(String filter, Workspace workspace)
/*      */     throws DataException, ServiceException
/*      */   {
/*  461 */     if (!SharedObjects.getEnvValueAsBoolean("IgnoreComponentLoadError", false))
/*      */     {
/*  463 */       PluginFilters.filter(filter, workspace, this.m_contextBinder, this);
/*      */     }
/*      */     else
/*      */     {
/*      */       try
/*      */       {
/*  469 */         PluginFilters.filter(filter, workspace, this.m_contextBinder, this);
/*      */       }
/*      */       catch (Throwable exception)
/*      */       {
/*  473 */         LoggingUtils.error(exception, null, null);
/*  474 */         exception.printStackTrace();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void userSecurityUpgrade()
/*      */     throws DataException, ServiceException
/*      */   {
/*  484 */     Workspace ws = WorkspaceUtils.getWorkspace("user");
/*  485 */     String updateState = getDBConfigValue("DevUpdate", "UserAttributes", null);
/*  486 */     if (updateState != null)
/*      */     {
/*  488 */       reportProgress(null, 1);
/*  489 */       return;
/*      */     }
/*  491 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "UserSecurityAttributes");
/*      */ 
/*  493 */     reportProgress(msg, 1);
/*      */ 
/*  495 */     if (!doesDBTableExistEx("UserSecurityAttributes", ws))
/*      */     {
/*  498 */       FieldInfo[] attribsfi = createFieldInfo(new String[] { "dUserName", "dAttributeName", "dAttributeType", "dAttributePrivilege", "dAttributeState" }, 30);
/*      */ 
/*  501 */       attribsfi[2].m_maxLen = 8;
/*  502 */       attribsfi[3].m_type = 3;
/*  503 */       attribsfi[4].m_maxLen = 8;
/*  504 */       ws.createTable("UserSecurityAttributes", attribsfi, new String[] { "dUserName", "dAttributeName", "dAttributeType" });
/*      */     }
/*      */ 
/*  508 */     FieldInfo[] usersfi = createFieldInfo(new String[] { "dFullName", "dUserType", "dPasswordEncoding" }, 8);
/*      */ 
/*  510 */     usersfi[0].m_maxLen = 30;
/*      */ 
/*  513 */     String[] userTablePK = { "dName" };
/*  514 */     ws.alterTable("Users", usersfi, null, userTablePK);
/*      */ 
/*  517 */     ws.executeSQL("delete from UserSecurityAttributes");
/*  518 */     ResultSet rset = ws.createResultSet("Users", null);
/*  519 */     DataBinder binder = new DataBinder();
/*  520 */     binder.putLocal("dAttributeType", "role");
/*  521 */     binder.putLocal("dAttributeState", "");
/*  522 */     binder.putLocal("dAttributePrivilege", "15");
/*      */ 
/*  524 */     DataResultSet drset = new DataResultSet();
/*  525 */     drset.copy(rset);
/*      */ 
/*  527 */     binder.addResultSet("Users", drset);
/*      */ 
/*  529 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  531 */       binder.putLocal("dAttributeName", binder.get("dRole"));
/*  532 */       ws.execute("IuserSecurityAttribute", binder);
/*      */     }
/*      */ 
/*  536 */     setDBConfigValue("DevUpdate", "UserAttributes", "3.5Dev", "1");
/*      */   }
/*      */ 
/*      */   public void accountsUpgrade() throws DataException
/*      */   {
/*  541 */     String updateState = getDBConfigValue("DevUpdate", "DocAccounts", null);
/*  542 */     if (updateState != null)
/*      */     {
/*  544 */       reportProgress(null, 1);
/*  545 */       return;
/*      */     }
/*  547 */     String msg = LocaleUtils.encodeMessage("csCreatingTable", null, "DocumentAccounts");
/*      */ 
/*  549 */     reportProgress(msg, 1);
/*      */ 
/*  551 */     if (!doesDBTableExist("DocumentAccounts"))
/*      */     {
/*  554 */       FieldInfo[] docaccountsfi = createFieldInfo(new String[] { "dDocAccount" }, 30);
/*      */ 
/*  556 */       this.m_workspace.createTable("DocumentAccounts", docaccountsfi, new String[] { "dDocAccount" });
/*      */     }
/*      */ 
/*  561 */     setDBConfigValue("DevUpdate", "DocAccounts", "3.5Dev", "1");
/*      */   }
/*      */ 
/*      */   public void aliasUserUpgrade() throws DataException, ServiceException
/*      */   {
/*  566 */     Workspace ws = WorkspaceUtils.getWorkspace("user");
/*  567 */     String updateState = getDBConfigValue("DevUpdate", "AliasUser", null);
/*  568 */     if (updateState != null)
/*      */     {
/*  570 */       reportProgress(null, 1);
/*  571 */       return;
/*      */     }
/*  573 */     String msg = LocaleUtils.encodeMessage("csAliasUpgrades", null);
/*  574 */     reportProgress(msg, 1);
/*      */ 
/*  576 */     if (!doesDBTableExistEx("AliasUser", ws))
/*      */     {
/*  579 */       FieldInfo[] aliasuserfi = createFieldInfo(new String[] { "dAlias", "dUserName" }, 30);
/*      */ 
/*  581 */       ws.createTable("AliasUser", aliasuserfi, new String[] { "dAlias", "dUserName" });
/*      */     }
/*      */ 
/*  586 */     ResultSet rset = ws.createResultSet("Alias", null);
/*  587 */     DataResultSet drset = new DataResultSet();
/*  588 */     drset.copy(rset);
/*      */ 
/*  590 */     int aliasIndex = ResultSetUtils.getIndexMustExist(drset, "dAlias");
/*  591 */     int userIndex = ResultSetUtils.getIndexMustExist(drset, "dUserName");
/*      */ 
/*  593 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  595 */       DataBinder binder = new DataBinder();
/*  596 */       binder.putLocal("dAlias", drset.getStringValue(aliasIndex));
/*  597 */       binder.putLocal("dUserName", drset.getStringValue(userIndex));
/*  598 */       ws.execute("IaliasUser", binder);
/*      */     }
/*      */ 
/*  601 */     FieldInfo[] aliasfi = createFieldInfo(new String[] { "dAliasDescription" }, 50);
/*      */ 
/*  605 */     String[] aliasTablePK = { "dAlias" };
/*  606 */     ws.alterTable("Alias", aliasfi, new String[] { "dAliasType", "dUserName" }, aliasTablePK);
/*      */ 
/*  610 */     setDBConfigValue("DevUpdate", "AliasUser", "3.5Dev", "1");
/*      */   }
/*      */ 
/*      */   public void workflowUpgrade() throws DataException
/*      */   {
/*  615 */     String updateState = getDBConfigValue("DevUpdate", "Workflow", null);
/*  616 */     if ((updateState != null) && (updateState.equals("2")))
/*      */     {
/*  618 */       reportProgress(null, 1);
/*  619 */       return;
/*      */     }
/*  621 */     reportProgress("!csWorkflowUpgrades", 1);
/*      */ 
/*  630 */     String[] tableNames = new String[7];
/*  631 */     String[][][] columns = new String[7][][];
/*  632 */     String[][] primaryKeys = new String[7][];
/*      */ 
/*  634 */     tableNames[0] = "Workflows";
/*  635 */     columns[0] = { { "dWfName", "varchar", "30" }, { "dWfID", "int", "" }, { "dWfDescription", "varchar", "80" }, { "dCompletionDate", "date", "" }, { "dSecurityGroup", "varchar", "30" }, { "dWfStatus", "varchar", "20" }, { "dWfType", "varchar", "20" } };
/*      */ 
/*  645 */     primaryKeys[0] = { "dWfID" };
/*      */ 
/*  647 */     tableNames[1] = "WorkflowDocuments";
/*  648 */     columns[1] = { { "dWfID", "int", "" }, { "dDocName", "varchar", "30" }, { "dWfDocState", "varchar", "20" }, { "dWfComputed", "varchar", "20" }, { "dWfCurrentStepID", "int", "" } };
/*      */ 
/*  656 */     primaryKeys[1] = { "dDocName" };
/*      */ 
/*  658 */     tableNames[2] = "WorkflowAliases";
/*  659 */     columns[2] = { { "dWfStepID", "int", "" }, { "dWfID", "int", "" }, { "dAlias", "varchar", "35" }, { "dAliasType", "varchar", "20" } };
/*      */ 
/*  666 */     primaryKeys[2] = { "dWfStepID", "dAlias", "dAliasType" };
/*      */ 
/*  668 */     tableNames[3] = "WorkflowSteps";
/*  669 */     columns[3] = { { "dWfStepName", "varchar", "30" }, { "dWfStepID", "int", "" }, { "dWfID", "int", "" }, { "dWfStepDescription", "varchar", "80" }, { "dWfStepType", "varchar", "20" }, { "dWfStepIsAll", "boolean", "" }, { "dWfStepWeight", "int", "" } };
/*      */ 
/*  679 */     primaryKeys[3] = { "dWfStepID" };
/*      */ 
/*  681 */     tableNames[4] = "WorkflowCriteria";
/*  682 */     columns[4] = { { "dWfID", "int", "" }, { "dWfCriteriaName", "varchar", "30" }, { "dWfCriteriaOperator", "varchar", "8" }, { "dWfCriteriaValue", "varchar", "30" } };
/*      */ 
/*  689 */     primaryKeys[4] = { "dWfID", "dWfCriteriaName" };
/*      */ 
/*  691 */     tableNames[5] = "WorkflowStates";
/*  692 */     columns[5] = { { "dID", "int", "" }, { "dWfID", "int", "" }, { "dUserName", "varchar", "30" } };
/*      */ 
/*  698 */     primaryKeys[5] = { "dID", "dWfID", "dUserName" };
/*      */ 
/*  700 */     tableNames[6] = "WorkflowDocAttributes";
/*  701 */     columns[6] = { { "dWfID", "int", "" }, { "dDocName", "varchar", "30" }, { "dWfAttribute", "varchar", "30" }, { "dWfAttributeType", "varchar", "8" } };
/*      */ 
/*  708 */     primaryKeys[6] = { "dWfID", "dDocName", "dWfAttributeType" };
/*      */ 
/*  711 */     for (int i = 0; i < tableNames.length; ++i)
/*      */     {
/*  713 */       String tableName = tableNames[i];
/*  714 */       this.m_workspace.deleteTable(tableName);
/*      */     }
/*      */ 
/*  718 */     this.m_workspace.deleteTable("WorkflowAlias");
/*      */ 
/*  721 */     for (int i = 0; i < tableNames.length; ++i)
/*      */     {
/*  723 */       createTable(tableNames[i], columns[i], primaryKeys[i]);
/*      */     }
/*      */ 
/*  726 */     if (updateState == null)
/*      */     {
/*  729 */       String insertSQLStub = "INSERT INTO Counters (dCounterName, dNextIndex) values ";
/*  730 */       String[] counters = { "WfID", "WfStepID" };
/*  731 */       for (int i = 0; i < counters.length; ++i)
/*      */       {
/*  733 */         String insertSQL = insertSQLStub + "('" + counters[i] + "',1)";
/*  734 */         this.m_workspace.executeSQL(insertSQL);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  739 */     setDBConfigValue("DevUpdate", "Workflow", "3.5Dev", "2");
/*      */   }
/*      */ 
/*      */   public void revisionsUpgrade() throws DataException
/*      */   {
/*  744 */     String updateState = getDBConfigValue("DevUpdate", "RevisionsUpgrade", null);
/*  745 */     if (updateState != null)
/*      */     {
/*  747 */       reportProgress(null, 1);
/*  748 */       return;
/*      */     }
/*  750 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "Revisions");
/*  751 */     reportProgress(msg, 1);
/*      */ 
/*  753 */     String[] newColumns = { "dDocAccount", "dReleaseDate" };
/*  754 */     FieldInfo[] extrarevisionsfi = createFieldInfo(newColumns, 30);
/*  755 */     extrarevisionsfi[1].m_type = 5;
/*      */ 
/*  758 */     String[] revisionsTablePK = { "dID" };
/*  759 */     this.m_workspace.alterTable("Revisions", extrarevisionsfi, null, revisionsTablePK);
/*      */ 
/*  761 */     for (int i = 0; i < newColumns.length; ++i)
/*      */     {
/*  763 */       this.m_workspace.addIndex("Revisions", new String[] { newColumns[i] });
/*      */     }
/*      */ 
/*  767 */     setDBConfigValue("DevUpdate", "RevisionsUpgrade", "3.5Dev", "1");
/*      */   }
/*      */ 
/*      */   public void subscriptionUpgrade() throws DataException, ServiceException
/*      */   {
/*  772 */     String updateState = getDBConfigValue("DevUpdate", "Subscription", null);
/*  773 */     String[] pkList = { "dSubscriptionAlias", "dSubscriptionAliasType", "dSubscriptionID", "dSubscriptionType" };
/*      */ 
/*  777 */     if (updateState == null)
/*      */     {
/*  779 */       String msg = LocaleUtils.encodeMessage("csCreatingTable", null, "Subscription");
/*      */ 
/*  781 */       reportProgress(msg, 1);
/*  782 */       if (!doesDBTableExist("Subscription"))
/*      */       {
/*  785 */         FieldInfo[] subsfi = createFieldInfo(new String[] { "dSubscriptionAlias", "dSubscriptionAliasType", "dSubscriptionEmail", "dSubscriptionID", "dSubscriptionType" }, 30);
/*      */ 
/*  789 */         subsfi[2].m_maxLen = 80;
/*  790 */         subsfi[3].m_maxLen = 255;
/*  791 */         this.m_workspace.createTable("Subscription", subsfi, pkList);
/*      */       }
/*      */ 
/*  795 */       setDBConfigValue("DevUpdate", "Subscription", "3.5Dev", "1");
/*      */     }
/*      */     else
/*      */     {
/*  799 */       reportProgress(null, 1);
/*      */     }
/*      */ 
/*  802 */     updateState = getDBConfigValue("DevUpdate", "Subscription", "4.0");
/*      */ 
/*  804 */     if ((updateState == null) || (updateState.equals("1")))
/*      */     {
/*  806 */       String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "Subscription");
/*      */ 
/*  808 */       reportProgress(msg, 1);
/*      */ 
/*  810 */       String dir = DirectoryLocator.getAppDataDirectory() + "subscription/";
/*  811 */       if ((((updateState == null) || (updateState.equals("1")))) && (FileUtils.checkFile(dir + "subscription_types.hda", true, false) != 0))
/*      */       {
/*  815 */         this.m_workspace.executeSQL("UPDATE Subscription SET dSubscriptionType='Basic' where dSubscriptionType='docName'");
/*      */       }
/*      */ 
/*  819 */       if (updateState == null)
/*      */       {
/*  821 */         String[] newColumns = { "dSubscriptionCreateDate", "dSubscriptionNotifyDate", "dSubscriptionUsedDate" };
/*  822 */         FieldInfo[] fi = createFieldInfo(newColumns, 30);
/*  823 */         fi[0].m_type = 5;
/*  824 */         fi[1].m_type = 5;
/*  825 */         fi[2].m_type = 5;
/*  826 */         this.m_workspace.alterTable("Subscription", fi, null, pkList);
/*      */       }
/*      */ 
/*  829 */       setDBConfigValue("DevUpdate", "Subscription", "4.0", "2");
/*      */     }
/*      */     else
/*      */     {
/*  833 */       reportProgress(null, 1);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void metafieldsTypeUpgrade() throws DataException
/*      */   {
/*  839 */     String updateState = getDBConfigValue("DevUpdate", "MetafieldsType", null);
/*  840 */     if (updateState != null)
/*      */     {
/*  842 */       reportProgress(null, 1);
/*  843 */       return;
/*      */     }
/*  845 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "DocMetaDefinition");
/*      */ 
/*  847 */     reportProgress(msg, 1);
/*      */ 
/*  849 */     ResultSet rset = this.m_workspace.createResultSetSQL("SELECT * FROM DocMeta WHERE dID = 0");
/*  850 */     int nfields = rset.getNumFields();
/*  851 */     for (int i = 0; i < nfields; ++i)
/*      */     {
/*  853 */       FieldInfo fi = new FieldInfo();
/*  854 */       rset.getIndexFieldInfo(i, fi);
/*  855 */       if ((fi.m_type != 6) || (!fi.m_isFixedLen) || (fi.m_maxLen <= 50) || (fi.m_maxLen >= 255))
/*      */         continue;
/*  857 */       String updateQuery = "UPDATE DocMetaDefinition SET dType = 'BigText' WHERE dName = '" + fi.m_name + "'";
/*      */ 
/*  859 */       this.m_workspace.executeSQL(updateQuery);
/*      */     }
/*      */ 
/*  864 */     setDBConfigValue("DevUpdate", "MetafieldsType", "3.5Dev", "1");
/*      */   }
/*      */ 
/*      */   public void documentsUpgrade() throws DataException
/*      */   {
/*  869 */     String updateState = getDBConfigValue("DevUpdate", "DocumentsUpgrade", null);
/*  870 */     if (updateState != null)
/*      */     {
/*  872 */       reportProgress(null, 1);
/*  873 */       return;
/*      */     }
/*  875 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "Documents");
/*      */ 
/*  877 */     reportProgress(msg, 1);
/*      */ 
/*  879 */     String[] newColumns = { "dFileSize" };
/*  880 */     FieldInfo[] extraDocumentsFI = createFieldInfo(newColumns, 30);
/*  881 */     extraDocumentsFI[0].m_type = 3;
/*      */ 
/*  883 */     String[] documentsTablePK = { "dDocID" };
/*  884 */     this.m_workspace.alterTable("Documents", extraDocumentsFI, null, documentsTablePK);
/*      */ 
/*  886 */     setDBConfigValue("DevUpdate", "DocumentsUpgrade", "3.6", "1");
/*      */   }
/*      */ 
/*      */   public void metafieldsAddOptionListType() throws DataException
/*      */   {
/*  891 */     String updateState = getDBConfigValue("DevUpdate", "MetafieldsUpgrade", null);
/*  892 */     if (updateState != null)
/*      */     {
/*  894 */       reportProgress(null, 1);
/*  895 */       return;
/*      */     }
/*      */ 
/*  898 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "DocMetaDefinition");
/*  899 */     reportProgress(msg, 1);
/*      */ 
/*  901 */     String[] newColumns = { "dOptionListType" };
/*  902 */     FieldInfo[] extraMetadefFI = createFieldInfo(newColumns, 8);
/*      */ 
/*  904 */     String[] metadefTablePK = { "dName" };
/*  905 */     this.m_workspace.alterTable("DocMetaDefinition", extraMetadefFI, null, metadefTablePK);
/*      */ 
/*  907 */     setDBConfigValue("DevUpdate", "MetafieldsUpgrade", "3.6", "1");
/*      */   }
/*      */ 
/*      */   public void userPasswordUpgrade() throws DataException, ServiceException
/*      */   {
/*  912 */     String updateState = getDBConfigValue("DevUpdate", "UserPasswordLen", null);
/*  913 */     if (updateState != null)
/*      */     {
/*  915 */       reportProgress(null, 2);
/*  916 */       return;
/*      */     }
/*      */ 
/*  919 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "Users");
/*  920 */     reportProgress(msg, 1);
/*      */ 
/*  923 */     String[] userTablePK = { "dName" };
/*  924 */     changeFieldLength("Users", userTablePK, new String[] { "dPassword", "dFullName" }, new int[] { 50, 50 });
/*      */ 
/*  928 */     setDBConfigValue("DevUpdate", "UserPasswordLen", "3.6", "1");
/*      */   }
/*      */ 
/*      */   protected void renditionsUpgrade() throws DataException
/*      */   {
/*  933 */     String updateState = getDBConfigValue("DevUpdate", "AddRenditions", null);
/*  934 */     if (updateState != null)
/*      */     {
/*  936 */       reportProgress(null, 1);
/*  937 */       return;
/*      */     }
/*  939 */     reportProgress("!csRevisionsRenditionsUpgrade", 1);
/*      */ 
/*  941 */     FieldInfo[] revfi = createFieldInfo(new String[] { "dRendition1", "dRendition2" }, 1);
/*      */ 
/*  945 */     String[] revTablePK = { "dID" };
/*  946 */     this.m_workspace.alterTable("Revisions", revfi, null, revTablePK);
/*      */ 
/*  949 */     setDBConfigValue("DevUpdate", "AddRenditions", "3.6.5", "1");
/*      */   }
/*      */ 
/*      */   protected void workflowHistoryUpgrade()
/*      */     throws DataException, ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  959 */       String updateState = getDBConfigValue("DevUpdate", "WorkflowHistory", null);
/*  960 */       if (updateState != null)
/*      */       {
/*  962 */         reportProgress(null, 1);
/*  963 */         return;
/*      */       }
/*      */ 
/*  966 */       if (!doesDBTableExist("WorkflowHistory"))
/*      */       {
/*  969 */         String msg = LocaleUtils.encodeMessage("csCreatingTable", null, "WorkflowHistory");
/*      */ 
/*  971 */         reportProgress(msg, 1);
/*      */ 
/*  973 */         FieldInfo[] attribsfi = createFieldInfo(new String[] { "dActionMillis", "dActionDate", "dAction", "dWfName", "dWfStepName", "dUser", "dID", "dDocName", "dDocType", "dDocTitle", "dDocAuthor", "dRevClassID", "dRevisionID", "dRevLabel", "dSecurityGroup", "dCreateDate", "dInDate", "dOutDate", "dDocAccount" }, 30);
/*      */ 
/*  979 */         attribsfi[0].m_type = 3;
/*  980 */         attribsfi[1].m_type = 5;
/*  981 */         attribsfi[6].m_type = 3;
/*  982 */         attribsfi[9].m_maxLen = 80;
/*  983 */         attribsfi[11].m_type = 3;
/*  984 */         attribsfi[12].m_type = 3;
/*  985 */         attribsfi[13].m_maxLen = 10;
/*  986 */         attribsfi[15].m_type = 5;
/*  987 */         attribsfi[16].m_type = 5;
/*  988 */         attribsfi[17].m_type = 5;
/*      */ 
/*  990 */         this.m_workspace.createTable("WorkflowHistory", attribsfi, new String[] { "dActionDate", "dActionMillis" });
/*      */       }
/*      */       else
/*      */       {
/*  995 */         reportProgress(null, 1);
/*      */       }
/*      */ 
/*  999 */       setDBConfigValue("DevUpdate", "WorkflowHistory", "3.6.5", "1");
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1003 */       throw new ServiceException("!csErrorCreatingTableWorkflowHistory");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void archiveHistoryUpgrade()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1012 */     String updateState = getDBConfigValue("DevUpdate", "ArchiveHistory", null);
/* 1013 */     if ((updateState != null) && (updateState.equals("2")))
/*      */     {
/* 1016 */       reportProgress(null, 2);
/* 1017 */       return;
/*      */     }
/*      */ 
/* 1020 */     if (!doesDBTableExist("ArchiveHistory"))
/*      */     {
/* 1023 */       reportProgress("Creating table ArchiveHistory", 1);
/* 1024 */       FieldInfo[] attribsfi = createFieldInfo(new String[] { "dActionMillis", "dActionDate", "dID", "dDocName", "dDocType", "dDocTitle", "dRevClassID", "dRevisionID", "dRevLabel", "dSecurityGroup", "dArchiveName", "dBatchFile", "dDocAccount" }, 30);
/*      */ 
/* 1028 */       attribsfi[0].m_type = 3;
/* 1029 */       attribsfi[1].m_type = 5;
/* 1030 */       attribsfi[2].m_type = 3;
/* 1031 */       attribsfi[5].m_maxLen = 80;
/* 1032 */       attribsfi[6].m_type = 3;
/* 1033 */       attribsfi[7].m_type = 3;
/* 1034 */       attribsfi[10].m_maxLen = 100;
/* 1035 */       attribsfi[11].m_maxLen = 50;
/*      */ 
/* 1037 */       this.m_workspace.createTable("ArchiveHistory", attribsfi, new String[] { "dActionDate", "dActionMillis", "dID" });
/*      */     }
/*      */     else
/*      */     {
/* 1042 */       reportProgress(null, 1);
/*      */     }
/*      */ 
/* 1045 */     if ((updateState != null) && (updateState.equals("1")))
/*      */     {
/* 1048 */       reportProgress("Updating table ArchiveHistory", 1);
/* 1049 */       String[] newColumns = { "dBatchFile", "dDocAccount" };
/* 1050 */       FieldInfo[] newfi = createFieldInfo(newColumns, 30);
/* 1051 */       newfi[0].m_maxLen = 50;
/*      */ 
/* 1053 */       String[] pk = { "dActionDate", "dActionMillis", "dID" };
/* 1054 */       this.m_workspace.alterTable("ArchiveHistory", newfi, null, pk);
/*      */     }
/*      */     else
/*      */     {
/* 1058 */       reportProgress(null, 1);
/*      */     }
/*      */ 
/* 1062 */     setDBConfigValue("DevUpdate", "ArchiveHistory", "3.6.5", "2");
/*      */   }
/*      */ 
/*      */   protected void indexerUpgradeV4() throws DataException, ServiceException
/*      */   {
/* 1067 */     String updateState = getDBConfigValue("DevUpdate", "Indexer", null);
/*      */ 
/* 1069 */     if ((updateState == null) || (updateState.equals("1")))
/*      */     {
/* 1071 */       reportProgress("!csUpdatingIndexerConfig", 1);
/* 1072 */       FieldInfo[] infos = createFieldInfo(new String[] { "dIndexerState" }, 1);
/* 1073 */       if (updateState == null)
/*      */       {
/* 1076 */         String[] pkList = { "dID" };
/* 1077 */         this.m_workspace.alterTable("Revisions", infos, null, pkList);
/*      */ 
/* 1080 */         this.m_workspace.deleteTable("WebChanges");
/*      */       }
/* 1082 */       setDBConfigValue("DevUpdate", "Indexer", "4.0", "2");
/*      */ 
/* 1086 */       Exception exception = null;
/* 1087 */       String searchDir = DirectoryLocator.getSearchDirectory();
/* 1088 */       File source = FileUtilsCfgBuilder.getCfgFile(searchDir + "index/", "Search", true);
/* 1089 */       if (source.exists())
/*      */       {
/* 1091 */         FileUtils.renameFile(searchDir + "index/", searchDir + "index1/");
/*      */       }
/*      */       try
/*      */       {
/* 1095 */         FileUtils.reserveDirectory(searchDir);
/* 1096 */         DataBinder binder = new DataBinder();
/* 1097 */         if (ResourceUtils.serializeDataBinder(searchDir, "activeindex.hda", binder, false, false))
/*      */         {
/* 1100 */           String activeIndex = binder.get("ActiveIndex");
/* 1101 */           String[][] idMap = SearchLoader.COLLECTION_ID_MAP;
/* 1102 */           DataResultSet drset = (DataResultSet)binder.getResultSet("SearchCollections");
/* 1103 */           infos = ResultSetUtils.createInfoList(drset, new String[] { "sCollectionID", "sLocation" }, true);
/*      */ 
/* 1105 */           boolean isChanged = false;
/* 1106 */           for (int i = 0; i < idMap.length; ++i)
/*      */           {
/*      */             String dir;
/*      */             String dir;
/* 1109 */             if (idMap[i][1].equals("index1/"))
/*      */             {
/* 1111 */               dir = searchDir + "index/";
/*      */             }
/*      */             else
/*      */             {
/* 1115 */               dir = searchDir + idMap[i][1];
/*      */             }
/* 1117 */             Vector v = drset.findRow(infos[0].m_index, "default");
/* 1118 */             if (v == null) {
/*      */               continue;
/*      */             }
/* 1121 */             String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 1122 */             v.setElementAt(idcName, infos[0].m_index);
/*      */ 
/* 1124 */             if (activeIndex.equals(dir))
/*      */             {
/* 1126 */               binder.putLocal("ActiveIndex", idMap[i][0]);
/* 1127 */               v.setElementAt(idMap[i][0], infos[1].m_index);
/*      */             }
/* 1129 */             isChanged = true;
/* 1130 */             break;
/*      */           }
/*      */ 
/* 1133 */           if (isChanged)
/*      */           {
/* 1135 */             ResourceUtils.serializeDataBinder(searchDir, "activeindex.hda", binder, true, false);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1142 */         exception = e;
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1146 */         exception = e;
/*      */       }
/*      */       finally
/*      */       {
/* 1150 */         FileUtils.releaseDirectory(searchDir);
/*      */       }
/*      */ 
/* 1153 */       if (exception != null)
/*      */       {
/* 1155 */         FileUtils.renameFile(searchDir + "index1/", searchDir + "index/");
/* 1156 */         if (exception instanceof DataException)
/*      */         {
/* 1158 */           throw ((DataException)exception);
/*      */         }
/* 1160 */         throw ((ServiceException)exception);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1165 */       reportProgress(null, 1);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void usersUpgradeV4() throws DataException, ServiceException
/*      */   {
/* 1171 */     Workspace ws = WorkspaceUtils.getWorkspace("user");
/* 1172 */     String updateState = getDBConfigValue("DevUpdate", "UsersUpgrade", null);
/* 1173 */     if (updateState != null)
/*      */     {
/* 1175 */       reportProgress(null, 1);
/* 1176 */       return;
/*      */     }
/* 1178 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "Users");
/* 1179 */     reportProgress(msg, 0);
/*      */ 
/* 1181 */     boolean isCreateNewTable = true;
/* 1182 */     String userDir = DirectoryLocator.getUserCacheDir();
/* 1183 */     String filePath = userDir + "users_upgrade.hda";
/* 1184 */     SerializeTable st = new SerializeTable(ws, this.m_progress);
/*      */ 
/* 1186 */     if (doesDBTableExistEx("Users", ws) == true)
/*      */     {
/* 1189 */       ResultSet rset = ws.createResultSetSQL("SELECT * from Users");
/* 1190 */       FieldInfo[] fi = ResultSetUtils.createInfoList(rset, new String[] { "dUserAuthType" }, false);
/* 1191 */       if (fi[0].m_index < 0)
/*      */       {
/* 1193 */         if (FileUtils.checkFile(filePath, false, false) != 0)
/*      */         {
/* 1196 */           st.serialize(userDir, "users_upgrade_temp.hda", "Users", null, true);
/* 1197 */           FileUtils.renameFile(userDir + "users_upgrade_temp.hda", filePath);
/*      */         }
/* 1199 */         ws.deleteTable("Users");
/*      */       }
/*      */       else
/*      */       {
/* 1203 */         isCreateNewTable = false;
/*      */       }
/*      */     }
/*      */ 
/* 1207 */     if (isCreateNewTable)
/*      */     {
/* 1209 */       String[][] columns = { { "dName", "varchar", "50" }, { "dFullName", "varchar", "50" }, { "dEmail", "varchar", "80" }, { "dPasswordEncoding", "varchar", "8" }, { "dPassword", "varchar", "50" }, { "dUserType", "varchar", "30" }, { "dUserAuthType", "varchar", "30" }, { "dUserOrgPath", "varchar", "255" }, { "dUserSourceOrgPath", "varchar", "50" }, { "dUserSourceFlags", "int", "" }, { "dUserArriveDate", "date", "" }, { "dUserChangeDate", "date", "" } };
/*      */ 
/* 1224 */       String[] primaryKeys = { "dName" };
/*      */ 
/* 1226 */       createTableEx(ws, "Users", columns, primaryKeys);
/*      */     }
/*      */ 
/* 1229 */     st.serialize(userDir, "users_upgrade.hda", "Users", "dName", false);
/* 1230 */     FileUtils.deleteFile(filePath);
/*      */ 
/* 1232 */     ws.executeSQL("Update Users set dUserAuthType='LOCAL'");
/*      */ 
/* 1235 */     setDBConfigValue("DevUpdate", "UsersUpgrade", "4.0", "1");
/* 1236 */     reportProgress(null, 1);
/*      */   }
/*      */ 
/*      */   protected void usersUpgradeV5() throws DataException, ServiceException
/*      */   {
/* 1241 */     Workspace ws = WorkspaceUtils.getWorkspace("user");
/* 1242 */     String updateState = getDBConfigValue("DevUpdate", "UsersLocaleUpgrade", null);
/* 1243 */     if (updateState != null)
/*      */     {
/* 1245 */       reportProgress(null, 1);
/* 1246 */       return;
/*      */     }
/* 1248 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "Users");
/* 1249 */     reportProgress(msg, 0);
/*      */ 
/* 1252 */     String[] newColumns = { "dUserLocale", "dUserTimeZone" };
/* 1253 */     FieldInfo[] newfi = createFieldInfo(newColumns, 30);
/* 1254 */     String[] usersTablePK = { "dName" };
/* 1255 */     ws.alterTable("Users", newfi, null, usersTablePK);
/*      */ 
/* 1258 */     setDBConfigValue("DevUpdate", "UsersLocaleUpgrade", "5.0", "1");
/* 1259 */     reportProgress(null, 1);
/*      */   }
/*      */ 
/*      */   protected void userTimeZoneUpgrade() throws DataException, ServiceException
/*      */   {
/* 1264 */     Workspace ws = WorkspaceUtils.getWorkspace("user");
/* 1265 */     String updateState = getDBConfigValue("DevUpdate", "UserTimeZoneUpgrade", null);
/* 1266 */     if (updateState != null)
/*      */     {
/* 1268 */       return;
/*      */     }
/*      */ 
/* 1272 */     DataResultSet origTable = SharedObjects.getTable("UserMetaDefinition");
/* 1273 */     String userMetaDir = LegacyDirectoryLocator.getSystemBaseDirectory("data") + "users/config/";
/* 1274 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(userMetaDir, 2, true);
/* 1275 */     FileUtils.reserveDirectory(userMetaDir);
/*      */     try
/*      */     {
/* 1279 */       File file = FileUtilsCfgBuilder.getCfgFile(userMetaDir + "usermeta.hda", "users", false);
/*      */ 
/* 1281 */       if (file.exists())
/*      */       {
/* 1283 */         DataBinder binder = new DataBinder(true);
/* 1284 */         ResourceUtils.serializeDataBinder(userMetaDir, "usermeta.hda", binder, false, true);
/* 1285 */         DataResultSet userMeta = (DataResultSet)binder.getResultSet("UserMetaDefinition");
/* 1286 */         if (userMeta != null)
/*      */         {
/* 1288 */           FieldInfo nameField = new FieldInfo();
/* 1289 */           if ((userMeta.getFieldInfo("umdName", nameField)) && 
/* 1291 */             (userMeta.findRow(nameField.m_index, "dUserTimeZone") == null))
/*      */           {
/* 1293 */             SharedObjects.putTable("UserMetaDefinition", userMeta);
/*      */ 
/* 1295 */             Map m = new HashMap();
/* 1296 */             m.put("umdName", "dUserTimeZone");
/* 1297 */             m.put("umdType", "BigText");
/* 1298 */             m.put("umdCaption", "apTitleUserTimeZone");
/* 1299 */             m.put("umdIsOptionList", "1");
/* 1300 */             m.put("umdOptionListType", "choice,timezone");
/* 1301 */             m.put("umdOptionListKey", "Users_UserTimeZoneList");
/* 1302 */             m.put("umdIsAdminEdit", "0");
/* 1303 */             m.put("umdIsViewOnly", "0");
/* 1304 */             m.put("umdOverrideBitFlag", MetaFieldUtils.calculateOverrideBitFlag());
/* 1305 */             Vector v = userMeta.createRow(new MapParameters(m));
/* 1306 */             userMeta.addRow(v);
/* 1307 */             ResourceUtils.serializeDataBinder(userMetaDir, "usermeta.hda", binder, true, true);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1315 */       Report.warning(null, e, "csUserMetaFileWillBeRebuilt", new Object[0]);
/*      */     }
/*      */     finally
/*      */     {
/* 1319 */       FileUtils.releaseDirectory(userMetaDir);
/* 1320 */       SharedObjects.putTable("UserMetaDefinition", origTable);
/*      */     }
/*      */ 
/* 1324 */     FieldInfo[] addCols = new FieldInfo[1];
/* 1325 */     addCols[0] = new FieldInfo();
/* 1326 */     addCols[0].m_name = "dUserTimeZone";
/* 1327 */     addCols[0].m_isFixedLen = true;
/* 1328 */     addCols[0].m_maxLen = 100;
/* 1329 */     ws.alterTable("Users", addCols, null, new String[] { "dName" });
/*      */ 
/* 1332 */     setDBConfigValue("DevUpdate", "UserTimeZoneUpgrade", "11gR1", "1");
/*      */   }
/*      */ 
/*      */   protected void projectUpgrade() throws DataException
/*      */   {
/* 1337 */     String updateState = getDBConfigValue("DevUpdate", "Project", null);
/* 1338 */     if ((updateState != null) && (updateState.equals("2")))
/*      */     {
/* 1340 */       reportProgress(null, 1);
/* 1341 */       return;
/*      */     }
/* 1343 */     reportProgress("!csConfiguringProjectSupport", 1);
/* 1344 */     if (updateState == null)
/*      */     {
/* 1352 */       String[] tableNames = new String[3];
/* 1353 */       String[][][] columns = new String[3][][];
/* 1354 */       String[][] primaryKeys = new String[3][];
/*      */ 
/* 1356 */       tableNames[0] = "RegisteredProjects";
/* 1357 */       columns[0] = { { "dProjectID", "varchar", "30" }, { "dPrjDescription", "varchar", "80" }, { "dPrjSourcePath", "varchar", "255" }, { "dPrjUrlPath", "varchar", "255" }, { "dPrjFunctions", "varchar", "80" } };
/*      */ 
/* 1365 */       primaryKeys[0] = { "dProjectID" };
/*      */ 
/* 1367 */       tableNames[1] = "ProjectDocuments";
/* 1368 */       columns[1] = { { "dDocName", "varchar", "30" }, { "dProjectID", "varchar", "30" }, { "dSourceInstanceName", "varchar", "50" }, { "dSourceDocID", "int", "" }, { "dSourceDocName", "varchar", "30" }, { "dSourcePending", "varchar", "1" }, { "dPrjTopParent", "varchar", "30" }, { "dPrjImmediateParent", "varchar", "30" }, { "dPrjMiddleParents", "varchar", "255" } };
/*      */ 
/* 1380 */       primaryKeys[1] = { "dDocName" };
/*      */ 
/* 1382 */       tableNames[2] = "ProblemReports";
/* 1383 */       columns[2] = { { "dPrID", "int", "" }, { "dPrCaption", "varchar", "80" }, { "dPrAuthor", "varchar", "50" }, { "dPrState", "varchar", "20" }, { "dPrSeverity", "varchar", "20" }, { "dPrCreateDate", "date", "" }, { "dPrChangeDate", "date", "" }, { "dProjectID", "varchar", "30" }, { "dDocName", "varchar", "30" }, { "dDocTitle", "varchar", "80" } };
/*      */ 
/* 1396 */       primaryKeys[2] = { "dPrID" };
/*      */ 
/* 1399 */       for (int i = 0; i < tableNames.length; ++i)
/*      */       {
/* 1401 */         createTable(tableNames[i], columns[i], primaryKeys[i]);
/*      */       }
/*      */ 
/* 1405 */       String insertSQLStub = "INSERT INTO Counters (dCounterName, dNextIndex) values ";
/* 1406 */       String[] counters = { "PrID" };
/* 1407 */       for (int i = 0; i < counters.length; ++i)
/*      */       {
/* 1409 */         String insertSQL = insertSQLStub + "('" + counters[i] + "',1)";
/* 1410 */         this.m_workspace.executeSQL(insertSQL);
/*      */       }
/*      */ 
/* 1414 */       String[] newColumns = { "dPublishType", "dPublishState" };
/* 1415 */       FieldInfo[] extrarevisionsfi = createFieldInfo(newColumns, 1);
/* 1416 */       String[] revisionsTablePK = { "dID" };
/* 1417 */       this.m_workspace.alterTable("Revisions", extrarevisionsfi, null, revisionsTablePK);
/*      */ 
/* 1420 */       newColumns = new String[] { "dProjectID" };
/* 1421 */       FieldInfo[] extraworkflowsfi = createFieldInfo(newColumns, 30);
/* 1422 */       String[] workflowsTablePK = { "dWfID" };
/* 1423 */       this.m_workspace.alterTable("Workflows", extraworkflowsfi, null, workflowsTablePK);
/*      */     }
/*      */     else
/*      */     {
/* 1428 */       String[] newColumns = { "dPublishState" };
/* 1429 */       FieldInfo[] extrarevisionsfi = createFieldInfo(newColumns, 1);
/* 1430 */       String[] revisionsTablePK = { "dID" };
/* 1431 */       this.m_workspace.alterTable("Revisions", extrarevisionsfi, null, revisionsTablePK);
/*      */     }
/*      */ 
/* 1435 */     setDBConfigValue("DevUpdate", "Project", "4.0", "2");
/*      */   }
/*      */ 
/*      */   public void expandFieldLengthsV4() throws DataException, ServiceException
/*      */   {
/* 1440 */     String updateState = getDBConfigValue("DevUpdate", "FieldLengthsUpgradeV4", null);
/* 1441 */     if ((updateState != null) && (updateState.equals("2")))
/*      */     {
/* 1443 */       reportProgress(null, 11);
/* 1444 */       return;
/*      */     }
/*      */ 
/* 1447 */     boolean doFullIndexingFixup = false;
/* 1448 */     boolean addCheckoutUserIndex = !this.m_workspace.supportsSqlColumnChange();
/* 1449 */     if (updateState == null)
/*      */     {
/* 1452 */       changeFieldLength("AliasUser", new String[] { "dAlias", "dUserName" }, new String[] { "dUserName" }, new int[] { 50 });
/*      */ 
/* 1454 */       changeFieldLength("Revisions", new String[] { "dID" }, new String[] { "dDocAuthor" }, new int[] { 50 });
/*      */ 
/* 1456 */       changeFieldLength("Subscription", new String[] { "dSubscriptionAlias", "dSubscriptionAliasType", "dSubscriptionID", "dSubscriptionType" }, new String[] { "dSubscriptionAlias" }, new int[] { 50 });
/*      */ 
/* 1459 */       changeFieldLength("UserSecurityAttributes", new String[] { "dUserName", "dAttributeName", "dAttributeType" }, new String[] { "dUserName" }, new int[] { 50 });
/*      */ 
/* 1461 */       changeFieldLength("WorkflowAliases", new String[] { "dWfStepID", "dAlias", "dAliasType" }, new String[] { "dAlias" }, new int[] { 50 });
/*      */ 
/* 1463 */       changeFieldLength("WorkflowHistory", new String[] { "dActionMillis", "dActionDate" }, new String[] { "dDocAuthor" }, new int[] { 50 });
/*      */ 
/* 1465 */       changeFieldLength("WorkflowStates", new String[] { "dID", "dWfID", "dUserName" }, new String[] { "dUserName" }, new int[] { 50 });
/*      */ 
/* 1468 */       changeFieldLength("Revisions", new String[] { "dID" }, new String[] { "dCheckoutUser" }, new int[] { 50 });
/*      */ 
/* 1472 */       changeFieldLength("Workflows", new String[] { "dWfName", "dWfID" }, new String[] { "dProjectID" }, new int[] { 30 });
/*      */ 
/* 1476 */       doFullIndexingFixup = !this.m_workspace.supportsSqlColumnChange();
/*      */     }
/*      */     else
/*      */     {
/* 1480 */       reportProgress(null, 9);
/*      */     }
/*      */ 
/* 1484 */     changeFieldLength("Revisions", new String[] { "dID" }, new String[] { "dCheckoutUser" }, new int[] { 50 });
/*      */     try
/*      */     {
/* 1490 */       this.m_workspace.addIndex("Revisions", new String[] { "dIndexerState" });
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/* 1494 */       String msg = LocaleUtils.encodeMessage("csUnableToAddIndexerStateIndex", ignore.getMessage());
/*      */ 
/* 1496 */       Report.trace(null, LocaleResources.localizeMessage(msg, this), ignore);
/*      */     }
/*      */ 
/* 1499 */     if (addCheckoutUserIndex)
/*      */     {
/*      */       try
/*      */       {
/* 1503 */         this.m_workspace.addIndex("Revisions", new String[] { "dCheckoutUser" });
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/* 1507 */         String msg = LocaleUtils.encodeMessage("csUnableToAddIndex", ignore.getMessage(), "dCheckoutUser");
/*      */ 
/* 1509 */         Report.trace(null, LocaleResources.localizeMessage(msg, this), ignore);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1514 */     if (doFullIndexingFixup)
/*      */     {
/* 1517 */       String[] revFieldsToIndex = { "dDocName", "dRevClassID", "dInDate", "dOutDate", "dStatus", "dReleaseState", "dCheckoutUser" };
/*      */ 
/* 1519 */       String curField = "<nofield>";
/*      */       try
/*      */       {
/* 1522 */         for (int i = 0; i < revFieldsToIndex.length; ++i)
/*      */         {
/* 1524 */           curField = revFieldsToIndex[i];
/* 1525 */           this.m_workspace.addIndex("Revisions", new String[] { curField });
/*      */         }
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/* 1530 */         String msg = LocaleUtils.encodeMessage("csUnableToAddIndex", ignore.getMessage(), curField);
/*      */ 
/* 1532 */         Report.trace(null, LocaleResources.localizeMessage(msg, this), ignore);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1537 */     setDBConfigValue("DevUpdate", "FieldLengthsUpgradeV4", "4.0", "2");
/* 1538 */     reportProgress(null, 1);
/*      */   }
/*      */ 
/*      */   public void addSysManager() throws DataException
/*      */   {
/* 1543 */     String updateState = getDBConfigValue("DevUpdate", "SysManager", null);
/* 1544 */     if (updateState != null)
/*      */     {
/* 1546 */       return;
/*      */     }
/*      */ 
/* 1549 */     String sql = "Select * from RoleDefinition WHERE dRoleName='sysmanager'";
/*      */ 
/* 1552 */     ResultSet rset = this.m_workspace.createResultSetSQL(sql);
/* 1553 */     if (rset.isEmpty())
/*      */     {
/* 1555 */       ResultSet groupSet = this.m_workspace.createResultSetSQL("Select * from SecurityGroups");
/* 1556 */       String[][] groups = ResultSetUtils.createStringTable(groupSet, new String[] { "dGroupName" });
/*      */ 
/* 1558 */       String sqlStub = "insert into RoleDefinition values('sysmanager','";
/* 1559 */       for (int i = 0; i < groups.length; ++i)
/*      */       {
/* 1561 */         String grp = groups[i][0];
/* 1562 */         sql = sqlStub + grp;
/* 1563 */         if ((grp.equalsIgnoreCase("public")) || (grp.equalsIgnoreCase("secure")))
/*      */         {
/* 1565 */           sql = sql + "', 1)";
/*      */         }
/*      */         else
/*      */         {
/* 1569 */           sql = sql + "', 0)";
/*      */         }
/* 1571 */         this.m_workspace.executeSQL(sql);
/*      */       }
/*      */ 
/* 1575 */       this.m_workspace.executeSQL("insert into UserSecurityAttributes (dUserName, dAttributeName, dAttributeType,dAttributePrivilege) values ('sysadmin', 'sysmanager', 'role', 15)");
/*      */     }
/*      */ 
/* 1581 */     setDBConfigValue("DevUpdate", "SysManager", "4.0", "1");
/*      */   }
/*      */ 
/*      */   protected void datedCacheUpgrade() throws DataException
/*      */   {
/* 1586 */     String updateState = getDBConfigValue("DevUpdate", "DatedCaches", null);
/* 1587 */     if (updateState != null)
/*      */     {
/* 1589 */       reportProgress(null, 1);
/* 1590 */       return;
/*      */     }
/* 1592 */     String msg = LocaleUtils.encodeMessage("csCreatingTable", null, "DatedCaches");
/*      */ 
/* 1594 */     reportProgress(msg, 1);
/*      */ 
/* 1596 */     String tableName = "DatedCaches";
/* 1597 */     String[][] columns = { { "dDocName", "varchar", "30" }, { "dCacheAction", "varchar", "10" }, { "dName", "varchar", "50" }, { "dSecurityGroup", "varchar", "30" }, { "dDocAccount", "varchar", "30" }, { "dLastUsedDate", "date", "" } };
/*      */ 
/* 1606 */     String[] primaryKeys = { "dDocName", "dCacheAction" };
/*      */ 
/* 1608 */     createTable(tableName, columns, primaryKeys);
/*      */ 
/* 1611 */     setDBConfigValue("DevUpdate", "DatedCaches", "4.0", "1");
/*      */   }
/*      */ 
/*      */   protected void workflowScriptUpgrade() throws DataException
/*      */   {
/* 1616 */     String updateState = getDBConfigValue("DevUpdate", "WorkflowScript", null);
/* 1617 */     int state = NumberUtils.parseInteger(updateState, 0);
/* 1618 */     if (state == 1)
/*      */     {
/* 1620 */       return;
/*      */     }
/*      */ 
/* 1624 */     FieldInfo[] attribsfi = createFieldInfo(new String[] { "dWfDirectory" }, 100);
/*      */ 
/* 1627 */     String[] tablePK = { "dDocName" };
/* 1628 */     this.m_workspace.alterTable("WorkflowDocuments", attribsfi, null, tablePK);
/*      */ 
/* 1631 */     attribsfi = createFieldInfo(new String[] { "dWfEntryTs" }, 30);
/* 1632 */     attribsfi[0].m_type = 5;
/*      */ 
/* 1634 */     tablePK = new String[] { "dID", "dWfID", "dUserName" };
/* 1635 */     this.m_workspace.alterTable("WorkflowStates", attribsfi, null, tablePK);
/*      */ 
/* 1638 */     setDBConfigValue("DevUpdate", "WorkflowScript", "5.0", "1");
/*      */   }
/*      */ 
/*      */   public void projectUpgradeV5() throws DataException
/*      */   {
/* 1643 */     String updateState = getDBConfigValue("DevUpdate", "ProjectV5", null);
/* 1644 */     if (updateState != null)
/*      */     {
/* 1646 */       reportProgress(null, 1);
/* 1647 */       return;
/*      */     }
/* 1649 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "ProjectDocuments");
/*      */ 
/* 1651 */     reportProgress(msg, 1);
/*      */ 
/* 1654 */     FieldInfo[] attribsfi = createFieldInfo(new String[] { "dPrjAgentID", "dPrjAction" }, 30);
/*      */ 
/* 1656 */     String[] tablePK = { "dDocName" };
/* 1657 */     this.m_workspace.alterTable("ProjectDocuments", attribsfi, null, tablePK);
/*      */ 
/* 1660 */     setDBConfigValue("DevUpdate", "ProjectV5", "5.0", "1");
/*      */   }
/*      */ 
/*      */   public void htmlConversionUpgrade()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1666 */     String updateState = getDBConfigValue("DevUpdate", "HtmlConversions", null);
/* 1667 */     if (updateState != null)
/*      */     {
/* 1669 */       reportProgress(null, 2);
/* 1670 */       return;
/*      */     }
/*      */ 
/* 1673 */     if (!doesDBTableExist("HtmlConversions"))
/*      */     {
/* 1676 */       String msg = LocaleUtils.encodeMessage("csCreatingTable", null, "HtmlConversion");
/*      */ 
/* 1678 */       reportProgress(msg, 1);
/*      */ 
/* 1680 */       FieldInfo[] attribsfi = createFieldInfo(new String[] { "dConversionKey", "dOutputFile", "dConversionDate", "dVaultTimeStamp", "dDependencyKey" }, 100);
/*      */ 
/* 1683 */       attribsfi[1].m_maxLen = 150;
/* 1684 */       attribsfi[2].m_type = 5;
/* 1685 */       attribsfi[3].m_maxLen = 50;
/*      */ 
/* 1687 */       this.m_workspace.createTable("HtmlConversions", attribsfi, new String[] { "dConversionKey" });
/*      */     }
/*      */     else
/*      */     {
/* 1692 */       reportProgress(null, 1);
/*      */     }
/*      */ 
/* 1696 */     setDBConfigValue("DevUpdate", "HtmlConversions", "5.0.1", "1");
/*      */ 
/* 1699 */     updateState = getDBConfigValue("DevUpdate", "HtmlConversionSums", null);
/* 1700 */     if (updateState != null)
/*      */     {
/* 1702 */       reportProgress(null, 1);
/* 1703 */       return;
/*      */     }
/*      */ 
/* 1706 */     if (!doesDBTableExist("HtmlConversionSums"))
/*      */     {
/* 1709 */       String msg = LocaleUtils.encodeMessage("csCreatingTable", null, "HtmlConversionSums");
/*      */ 
/* 1711 */       reportProgress(msg, 1);
/*      */ 
/* 1713 */       FieldInfo[] attribsfi = createFieldInfo(new String[] { "dTotalCached", "dTotalFiles", "dLastAccessed", "dLastKey" }, 25);
/*      */ 
/* 1716 */       attribsfi[2].m_type = 5;
/* 1717 */       attribsfi[3].m_maxLen = 100;
/*      */ 
/* 1719 */       this.m_workspace.createTable("HtmlConversionSums", attribsfi, new String[0]);
/*      */     }
/*      */     else
/*      */     {
/* 1723 */       reportProgress(null, 1);
/*      */     }
/*      */ 
/* 1727 */     setDBConfigValue("DevUpdate", "HtmlConversionSums", "5.0.1", "1");
/*      */   }
/*      */ 
/*      */   public void previewUpgradeV5() throws DataException
/*      */   {
/* 1732 */     String updateState = getDBConfigValue("DevUpdate", "PreviewV5", null);
/* 1733 */     if ((updateState != null) && (updateState.equals("2")))
/*      */     {
/* 1735 */       reportProgress(null, 1);
/* 1736 */       return;
/*      */     }
/*      */ 
/* 1739 */     FieldInfo[] attribsfi = null;
/* 1740 */     if (updateState == null)
/*      */     {
/* 1742 */       attribsfi = createFieldInfo(new String[] { "dCacheHash", "dDocType" }, 30);
/*      */     }
/*      */     else
/*      */     {
/* 1746 */       attribsfi = createFieldInfo(new String[] { "dDocType" }, 30);
/*      */     }
/*      */ 
/* 1749 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "DatedCaches");
/*      */ 
/* 1751 */     reportProgress(msg, 1);
/*      */ 
/* 1753 */     String[] tablePK = { "dDocName", "dCacheAction" };
/* 1754 */     this.m_workspace.alterTable("DatedCaches", attribsfi, null, tablePK);
/*      */ 
/* 1757 */     setDBConfigValue("DevUpdate", "PreviewV5", "5.1", "2");
/*      */   }
/*      */ 
/*      */   public void updateSearchCollectionsV511() throws ServiceException, DataException
/*      */   {
/* 1762 */     if (!Features.checkLevel("Search", null))
/*      */     {
/* 1764 */       return;
/*      */     }
/* 1766 */     String engineName = SharedObjects.getEnvironmentValue("SearchIndexerEngineName");
/* 1767 */     if ((engineName != null) && (!engineName.toLowerCase().startsWith("verity")))
/*      */     {
/* 1769 */       return;
/*      */     }
/* 1771 */     ActiveIndexState.m_fileDir = LegacyDirectoryLocator.getSearchDirectory();
/* 1772 */     ActiveIndexState.load();
/* 1773 */     DataResultSet drset = (DataResultSet)ActiveIndexState.getSearchCollections();
/* 1774 */     if (drset == null)
/*      */       return;
/* 1776 */     FieldInfo info = new FieldInfo();
/* 1777 */     if (drset.getFieldInfo("sVerityLocale", info))
/*      */       return;
/* 1779 */     String path = ActiveIndexState.getActiveProperty("ActiveIndex");
/* 1780 */     path = SearchLoader.getCollectionPath(path);
/* 1781 */     path = path + "/intradocbasic/assists/00000000.abt";
/*      */ 
/* 1783 */     String locale = null;
/* 1784 */     if (FileUtils.checkFile(path, true, false) == 0)
/*      */     {
/* 1786 */       BufferedReader reader = null;
/*      */       try
/*      */       {
/* 1789 */         reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "Cp1252"));
/*      */ 
/* 1791 */         char state = 'I';
/* 1792 */         while (((line = reader.readLine()) != null) && (state != 'F'))
/*      */         {
/* 1794 */           String line = line.trim();
/* 1795 */           switch (state)
/*      */           {
/*      */           case 'I':
/* 1798 */             if (line.equals("field_name: VdkLocaleName"))
/*      */             {
/* 1800 */               state = 'J'; } break;
/*      */           case 'J':
/* 1804 */             if (line.startsWith("value: "))
/*      */             {
/* 1806 */               locale = line.substring("value: ".length());
/* 1807 */               state = 'F';
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*      */       }
/*      */       finally
/*      */       {
/* 1818 */         FileUtils.closeReader(reader);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1823 */       locale = SearchLoader.computeSearchLocale();
/*      */     }
/*      */ 
/* 1826 */     if (locale == null)
/*      */     {
/* 1828 */       throw new ServiceException("!csUnableToDetermineVerityLocale");
/*      */     }
/*      */ 
/* 1831 */     DataResultSet tmpSet = new SearchCollections();
/* 1832 */     tmpSet.merge("sCollectionID", drset, false);
/* 1833 */     FieldInfo[] infos = ResultSetUtils.createInfoList(tmpSet, new String[] { "sCollectionID", "sVerityLocale" }, true);
/*      */ 
/* 1835 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 1836 */     Vector v = tmpSet.findRow(infos[0].m_index, idcName);
/* 1837 */     if (v != null)
/*      */     {
/* 1839 */       v.setElementAt(locale, infos[1].m_index);
/*      */     }
/* 1841 */     ActiveIndexState.setSearchCollections(tmpSet);
/* 1842 */     ActiveIndexState.save();
/*      */   }
/*      */ 
/*      */   public void workflowUpgradeV6()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1849 */     String updateState = getDBConfigValue("DevUpdate", "Workflow6", null);
/* 1850 */     if ((updateState != null) && (updateState.equals("2")))
/*      */     {
/* 1852 */       reportProgress(null, 2);
/* 1853 */       return;
/*      */     }
/*      */ 
/* 1856 */     if (updateState == null)
/*      */     {
/* 1859 */       String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "Workflows");
/*      */ 
/* 1861 */       reportProgress(msg, 1);
/*      */ 
/* 1863 */       FieldInfo[] attribsfi = createFieldInfo(new String[] { "dIsCollaboration" }, 30);
/* 1864 */       attribsfi[0].m_type = 1;
/*      */ 
/* 1866 */       String[] tablePK = { "dWfID" };
/* 1867 */       this.m_workspace.alterTable("Workflows", attribsfi, null, tablePK);
/*      */     }
/*      */     else
/*      */     {
/* 1871 */       reportProgress(null, 1);
/*      */     }
/*      */ 
/* 1876 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "WorkflowDocuments");
/*      */ 
/* 1878 */     reportProgress(msg, 1);
/*      */ 
/* 1880 */     FieldInfo[] attribsfi = createFieldInfo(new String[] { "dClbraName" }, 50);
/*      */ 
/* 1882 */     String[] tablePK = { "dActionMillis", "dActionDate" };
/* 1883 */     this.m_workspace.alterTable("WorkflowHistory", attribsfi, null, tablePK);
/*      */ 
/* 1886 */     tablePK = new String[] { "dDocName" };
/* 1887 */     this.m_workspace.alterTable("WorkflowDocuments", attribsfi, null, tablePK);
/*      */ 
/* 1890 */     setDBConfigValue("DevUpdate", "Workflow6", "6.0", "2");
/*      */   }
/*      */ 
/*      */   public void collaborationUpgradeV6() throws DataException
/*      */   {
/* 1895 */     String updateState = getDBConfigValue("DevUpdate", "Collaboration", null);
/* 1896 */     if (updateState != null)
/*      */     {
/* 1898 */       reportProgress(null, 1);
/* 1899 */       return;
/*      */     }
/*      */ 
/* 1903 */     String msg = LocaleUtils.encodeMessage("csCreatingTable", null, "Collaborations");
/*      */ 
/* 1905 */     reportProgress(msg, 1);
/*      */ 
/* 1907 */     FieldInfo[] attribsfi = createFieldInfo(new String[] { "dClbraName", "dClbraDescription", "dClbraType", "dClbraCreateDate", "dClbraCreatedBy", "dClbraChangeDate", "dClbraChangedBy" }, 50);
/*      */ 
/* 1911 */     attribsfi[1].m_maxLen = 80;
/* 1912 */     attribsfi[2].m_maxLen = 30;
/* 1913 */     attribsfi[3].m_type = 5;
/* 1914 */     attribsfi[5].m_type = 5;
/* 1915 */     this.m_workspace.createTable("Collaborations", attribsfi, new String[] { "dClbraName" });
/*      */ 
/* 1919 */     setDBConfigValue("DevUpdate", "Collaboration", "6.0", "1");
/*      */   }
/*      */ 
/*      */   public void addCollaborationColumnsV6()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1931 */     executeFilter("preAddCollaborationColumnsV6", this.m_workspace);
/*      */ 
/* 1933 */     if ((!SecurityUtils.m_useCollaboration) && (!SecurityUtils.m_useEntitySecurity))
/*      */     {
/* 1935 */       return;
/*      */     }
/*      */ 
/* 1938 */     String[][] info = { { "xClbraUserList", "apNgClbraUserListFieldCaption", "2" }, { "xClbraAliasList", "apNgClbraAliasListFieldCaption", "3" } };
/*      */ 
/* 1944 */     String[][] defaults = { { "dType", "Memo" }, { "dIsRequired", "0" }, { "dIsEnabled", "1" }, { "dIsSearchable", "1" }, { "dIsOptionList", "0" }, { "dDefaultValue", "" }, { "dOptionListKey", "" }, { "dOptionListType", "" } };
/*      */ 
/* 1955 */     Properties props = StringUtils.convertStringArrayToProperties(defaults);
/* 1956 */     boolean isClbraUpdated = false;
/*      */     try
/*      */     {
/* 1959 */       for (int i = 0; i < info.length; ++i)
/*      */       {
/* 1961 */         props.put("dName", info[i][0]);
/* 1962 */         props.put("dCaption", info[i][1]);
/* 1963 */         props.put("dOrder", info[i][2]);
/* 1964 */         props.put("isNewMetaDataField", Integer.valueOf(1));
/* 1965 */         DataBinder binder = new DataBinder();
/* 1966 */         binder.setLocalData(props);
/* 1967 */         ResultSet rset = this.m_workspace.createResultSet("Qmetadef", binder);
/*      */ 
/* 1969 */         if (!rset.isEmpty())
/*      */           continue;
/* 1971 */         MetaFieldUtils.updateMetaDataFromBinder(this.m_workspace, binder, IdcSystemLoader.m_cxt);
/* 1972 */         isClbraUpdated = true;
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1978 */       Report.warning(null, t, "csClbraUnableToAddMetaDataFields", new Object[0]);
/*      */     }
/* 1980 */     if (isClbraUpdated)
/*      */     {
/* 1982 */       Report.info(null, null, "csClbraUpdateSearchIndex", new Object[0]);
/*      */     }
/*      */ 
/* 1985 */     executeFilter("postAddCollaborationColumnsV6", this.m_workspace);
/*      */   }
/*      */ 
/*      */   public void revisionsUpgradeV7() throws ServiceException, DataException
/*      */   {
/* 1990 */     String updateState = getDBConfigValue("DevUpdate", "Revisions7", null);
/* 1991 */     if ((updateState != null) && (updateState.equals("1")))
/*      */     {
/* 1993 */       reportProgress(null, 1);
/* 1994 */       return;
/*      */     }
/*      */ 
/* 1997 */     if (updateState == null)
/*      */     {
/* 1999 */       String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "Revisions");
/*      */ 
/* 2001 */       reportProgress(msg, 1);
/*      */ 
/* 2003 */       FieldInfo[] attribsfi = createFieldInfo(new String[] { "dWorkflowState" }, 1);
/*      */ 
/* 2006 */       String[] tablePK = { "dID" };
/* 2007 */       this.m_workspace.alterTable("Revisions", attribsfi, null, tablePK);
/*      */     }
/*      */ 
/* 2011 */     setDBConfigValue("DevUpdate", "Revisions7", "7.0", "1");
/*      */   }
/*      */ 
/*      */   public void workflowStepsV7() throws ServiceException, DataException
/*      */   {
/* 2016 */     String updateState = getDBConfigValue("DevUpdate", "Workflow7", null);
/* 2017 */     if ((updateState != null) && (updateState.equals("2")))
/*      */     {
/* 2019 */       reportProgress(null, 6);
/* 2020 */       return;
/*      */     }
/*      */ 
/* 2023 */     if (updateState == null)
/*      */     {
/* 2025 */       String[][] valueMap = { { "AutoContributor", ":C:CA:CE:" }, { "Contributor", ":C:CE:" }, { "Reviewer/Contributor", ":R:C:CE:" } };
/*      */ 
/* 2033 */       String msg = LocaleUtils.encodeMessage("csWorkflowUpgrades", null);
/* 2034 */       for (int i = 0; i < valueMap.length; ++i)
/*      */       {
/* 2036 */         this.m_workspace.executeSQL("UPDATE WorkflowSteps SET dWfStepType = '" + valueMap[i][1] + "' where dWfStepType = '" + valueMap[i][0] + "'");
/*      */ 
/* 2040 */         reportProgress(msg, 1);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 2045 */       reportProgress(null, 3);
/*      */     }
/*      */ 
/* 2050 */     int docNameLength = getDocNameLength();
/*      */ 
/* 2053 */     FieldInfo[] attribsfi = createFieldInfo(new String[] { "dDocName" }, docNameLength);
/* 2054 */     String[] tablePK = { "dID", "dWfID", "dUserName" };
/* 2055 */     this.m_workspace.alterTable("WorkflowStates", attribsfi, null, tablePK);
/*      */ 
/* 2061 */     String sqlQueryStates = "SELECT Revisions.dID, Revisions.dDocName FROM Revisions, WorkflowStates WHERE Revisions.dID = WorkflowStates.dID";
/* 2062 */     ResultSet rset = this.m_workspace.createResultSetSQL(sqlQueryStates);
/* 2063 */     DataResultSet curWorkflowDocs = new DataResultSet();
/* 2064 */     curWorkflowDocs.copy(rset);
/* 2065 */     FieldInfo[] fi = ResultSetUtils.createInfoList(curWorkflowDocs, new String[] { "dID", "dDocName" }, true);
/* 2066 */     String part1 = "UPDATE WorkflowStates SET dDocName = '";
/* 2067 */     String part2 = "' WHERE WorkflowStates.dID = ";
/* 2068 */     StringBuffer buf = new StringBuffer();
/* 2069 */     buf.append(part1);
/*      */ 
/* 2071 */     for (curWorkflowDocs.first(); curWorkflowDocs.isRowPresent(); curWorkflowDocs.next())
/*      */     {
/* 2073 */       buf.setLength(part1.length());
/* 2074 */       String id = curWorkflowDocs.getStringValue(fi[0].m_index);
/* 2075 */       String docName = curWorkflowDocs.getStringValue(fi[1].m_index);
/* 2076 */       buf.append(docName);
/* 2077 */       buf.append(part2);
/* 2078 */       buf.append(id);
/* 2079 */       this.m_workspace.executeSQL(buf.toString());
/*      */     }
/* 2081 */     String msg = LocaleUtils.encodeMessage("csWorkflowUpgrades", null);
/* 2082 */     reportProgress(msg, 3);
/*      */ 
/* 2084 */     setDBConfigValue("DevUpdate", "Workflow7", "7.0", "2");
/*      */   }
/*      */ 
/*      */   public void archiveTableUpgrade() throws DataException
/*      */   {
/* 2089 */     String updateState = getDBConfigValue("DevUpdate", "Archiver7", null);
/* 2090 */     if ((updateState != null) && (updateState.equals("3")))
/*      */     {
/* 2092 */       reportProgress(null, 2);
/* 2093 */       return;
/*      */     }
/*      */ 
/* 2096 */     if (!doesDBTableExist("DeletedRows"))
/*      */     {
/* 2099 */       String msg = LocaleUtils.encodeMessage("csCreatingTable", null, "DeletedRows");
/*      */ 
/* 2101 */       reportProgress(msg, 1);
/*      */ 
/* 2103 */       FieldInfo[] attribsfi = createFieldInfo(new String[] { "dRowID", "dTable", "dPrimaryKeys", "dPKColumns", "dPKTypes", "dDeleteDate", "dActionDate", "dSourceID" }, 50);
/*      */ 
/* 2106 */       attribsfi[0].m_maxLen = 16;
/* 2107 */       attribsfi[1].m_maxLen = 30;
/* 2108 */       attribsfi[2].m_maxLen = 255;
/* 2109 */       attribsfi[3].m_maxLen = 150;
/* 2110 */       attribsfi[5].m_type = 5;
/* 2111 */       attribsfi[6].m_type = 5;
/*      */ 
/* 2113 */       this.m_workspace.createTable("DeletedRows", attribsfi, new String[] { "dRowID" });
/*      */     }
/* 2116 */     else if ((updateState == null) || (updateState.equals("1")) || (updateState.equals("2")))
/*      */     {
/* 2118 */       ResultSet rset = this.m_workspace.createResultSetSQL("SELECT * FROM DeletedRows");
/* 2119 */       FieldInfo fTemp = new FieldInfo();
/* 2120 */       if (!rset.getFieldInfo("dActionDate", fTemp))
/*      */       {
/* 2122 */         String msg = LocaleUtils.encodeMessage("csActionDateAddingColumnDeletedRows", null);
/* 2123 */         reportProgress(msg, 1);
/*      */ 
/* 2125 */         FieldInfo[] fi = new FieldInfo[1];
/* 2126 */         fi[0] = new FieldInfo();
/* 2127 */         fi[0].m_name = "dActionDate";
/* 2128 */         fi[0].m_type = 5;
/* 2129 */         this.m_workspace.alterTable("DeletedRows", fi, null, new String[] { "dRowID" });
/*      */       }
/*      */       else
/*      */       {
/* 2133 */         reportProgress(null, 1);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 2138 */       reportProgress(null, 1);
/*      */     }
/*      */ 
/* 2141 */     if (!doesDBTableExist("ArchiveChangedRows"))
/*      */     {
/* 2144 */       String msg = LocaleUtils.encodeMessage("csCreatingTable", null, "ArchiveChangedRows");
/*      */ 
/* 2146 */       reportProgress(msg, 1);
/*      */ 
/* 2148 */       FieldInfo[] attribsfi = createFieldInfo(new String[] { "dRowID", "dTable", "dPrimaryKeys", "dPKColumns", "dPKTypes", "dChangeDate", "dActionDate", "dSourceID" }, 50);
/*      */ 
/* 2151 */       attribsfi[0].m_maxLen = 16;
/* 2152 */       attribsfi[1].m_maxLen = 30;
/* 2153 */       attribsfi[2].m_maxLen = 255;
/* 2154 */       attribsfi[3].m_maxLen = 150;
/* 2155 */       attribsfi[5].m_type = 5;
/* 2156 */       attribsfi[6].m_type = 5;
/*      */ 
/* 2158 */       this.m_workspace.createTable("ArchiveChangedRows", attribsfi, new String[] { "dRowID" });
/*      */     }
/*      */     else
/*      */     {
/* 2163 */       reportProgress(null, 1);
/*      */     }
/*      */ 
/* 2167 */     setDBConfigValue("DevUpdate", "Archiver7", "7.0", "3");
/*      */   }
/*      */ 
/*      */   public void revisionsTableUpgrade75() throws DataException
/*      */   {
/* 2172 */     String updateState = getDBConfigValue("DevUpdate", "Revisions75", null);
/* 2173 */     if (updateState != null)
/*      */     {
/* 2175 */       reportProgress(null, 6);
/* 2176 */       return;
/*      */     }
/*      */ 
/* 2179 */     FieldInfo[] fi = new FieldInfo[1];
/* 2180 */     fi[0] = new FieldInfo();
/* 2181 */     fi[0].m_name = "dRevRank";
/* 2182 */     fi[0].m_type = 3;
/*      */ 
/* 2184 */     String msg = LocaleUtils.encodeMessage("csRevRankAddingColumn", null);
/* 2185 */     reportProgress(msg, 1);
/* 2186 */     ResultSet rset = this.m_workspace.createResultSetSQL("select * from Revisions where dID = 0");
/* 2187 */     FieldInfo fTemp = new FieldInfo();
/* 2188 */     if (!rset.getFieldInfo("dRevRank", fTemp))
/*      */     {
/* 2190 */       this.m_workspace.alterTable("Revisions", fi, null, new String[] { "dID" });
/*      */     }
/*      */ 
/* 2193 */     msg = LocaleUtils.encodeMessage("csRevRankComputingNumberOfRevisionRows", null);
/* 2194 */     reportProgress(msg, 1);
/*      */ 
/* 2197 */     rset = this.m_workspace.createResultSet("QIDCAnalyzeRevCount", null);
/* 2198 */     int numRows = NumberUtils.parseInteger(rset.getStringValue(0), 0);
/* 2199 */     int curPartCount = 0;
/* 2200 */     rset = this.m_workspace.createResultSet("QrevisionsAllRevIDs", null);
/*      */ 
/* 2203 */     msg = LocaleUtils.encodeMessage("csRevRankComputingAndUpdatingAllRevisionRows", null);
/* 2204 */     reportProgress(msg, 1);
/* 2205 */     int rowCount = 0;
/*      */ 
/* 2208 */     DataBinder binder = new DataBinder();
/*      */ 
/* 2211 */     Hashtable revClassCounts = new Hashtable();
/*      */ 
/* 2215 */     DataResultSet drset = new DataResultSet();
/* 2216 */     drset.copy(rset);
/* 2217 */     fi = ResultSetUtils.createInfoList(drset, new String[] { "dID", "dRevClassID" }, true);
/*      */ 
/* 2219 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 2221 */       String dID = drset.getStringValue(fi[0].m_index);
/* 2222 */       String dRevClassID = drset.getStringValue(fi[1].m_index);
/* 2223 */       int revRank = 0;
/* 2224 */       int[] revRankEntry = (int[])(int[])revClassCounts.get(dRevClassID);
/* 2225 */       if (revRankEntry != null)
/*      */       {
/* 2227 */         int curRank = revRankEntry[0];
/* 2228 */         ++curRank;
/* 2229 */         revRankEntry[0] = curRank;
/* 2230 */         revRank = curRank;
/*      */       }
/*      */       else
/*      */       {
/* 2234 */         revRankEntry = new int[] { 0 };
/* 2235 */         revClassCounts.put(dRevClassID, revRankEntry);
/*      */       }
/* 2237 */       binder.putLocal("dID", dID);
/* 2238 */       binder.putLocal("dRevRank", "" + revRank);
/* 2239 */       this.m_workspace.execute("UrevisionRevRank", binder);
/*      */ 
/* 2242 */       ++rowCount;
/* 2243 */       int reportPerNumRows = numRows / 3 + 1;
/* 2244 */       if (reportPerNumRows < 20)
/*      */       {
/* 2246 */         reportPerNumRows = 20;
/*      */       }
/* 2248 */       else if (reportPerNumRows > 500)
/*      */       {
/* 2250 */         reportPerNumRows = 500;
/*      */       }
/* 2252 */       int computationAdj = numRows / 10;
/* 2253 */       if ((rowCount % reportPerNumRows != 0) && (rowCount != numRows)) {
/*      */         continue;
/*      */       }
/* 2256 */       int newPartCount = (rowCount * 3 + computationAdj) / numRows;
/* 2257 */       int reportProgressAmount = newPartCount - curPartCount;
/* 2258 */       curPartCount = newPartCount;
/* 2259 */       msg = LocaleUtils.encodeMessage("csRevRankUpdatingColumn", null, "" + rowCount, "" + numRows);
/* 2260 */       if (reportProgressAmount > 0)
/*      */       {
/* 2262 */         reportProgress(null, reportProgressAmount);
/*      */       }
/* 2264 */       reportProgress(msg, 0);
/*      */     }
/*      */ 
/* 2268 */     reportProgress(null, 3 - curPartCount);
/*      */ 
/* 2270 */     setDBConfigValue("DevUpdate", "Revisions75", "7.5", "1");
/*      */   }
/*      */ 
/*      */   public void expandMetaFieldSizeV8() throws DataException, ServiceException
/*      */   {
/* 2275 */     String updateState = getDBConfigValue("DevUpdate", "MetaFieldLengthUpdateV8", null);
/* 2276 */     if ((SharedObjects.getEnvValueAsBoolean("UsePre80MetaFieldType", true)) || (updateState != null))
/*      */     {
/* 2278 */       reportProgress(null, 2);
/* 2279 */       return;
/*      */     }
/*      */ 
/* 2283 */     FieldInfo[] metaFields = this.m_workspace.getColumnList("DocMeta");
/* 2284 */     String query = "SELECT dName, dType FROM DocMetaDefinition";
/* 2285 */     ResultSet rset = this.m_workspace.createResultSetSQL(query);
/* 2286 */     Vector[] fieldList = getExpandFieldListV8(rset, "dName", "dType", metaFields);
/* 2287 */     String[] tablePK = { "dID" };
/* 2288 */     updateMetaFieldsV8("DocMeta", tablePK, fieldList);
/*      */ 
/* 2292 */     DataResultSet drset = null;
/* 2293 */     String userMetaFile = DirectoryLocator.getSystemBaseDirectory("data") + "users/config/usermeta.hda";
/* 2294 */     int code = FileUtils.checkFile(userMetaFile, true, false);
/* 2295 */     if ((code != -16) && (code != -24))
/*      */     {
/* 2297 */       DataBinder binder = ResourceUtils.readDataBinderFromPath(userMetaFile);
/* 2298 */       drset = (DataResultSet)binder.getResultSet("UserMetaDefinition");
/*      */     }
/*      */     else
/*      */     {
/* 2302 */       drset = SharedObjects.getTable("UserMetaDefinition");
/*      */     }
/*      */ 
/* 2305 */     fieldList = getExpandFieldListV8(drset, "umdName", "umdType", null);
/* 2306 */     tablePK[0] = "dName";
/* 2307 */     updateMetaFieldsV8("Users", tablePK, fieldList);
/*      */ 
/* 2309 */     setDBConfigValue("DevUpdate", "MetaFieldLengthUpdateV8", "8.0", "1");
/*      */   }
/*      */ 
/*      */   protected Vector[] getExpandFieldListV8(ResultSet rset, String nameCol, String typeCol, FieldInfo[] metaFields)
/*      */     throws DataException
/*      */   {
/* 2315 */     Vector[] fieldList = { new IdcVector(), new IdcVector() };
/* 2316 */     FieldInfo nameFI = new FieldInfo();
/* 2317 */     FieldInfo typeFI = new FieldInfo();
/* 2318 */     rset.getFieldInfo(nameCol, nameFI);
/* 2319 */     rset.getFieldInfo(typeCol, typeFI);
/* 2320 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/* 2322 */       String name = rset.getStringValue(nameFI.m_index);
/* 2323 */       String typeStr = rset.getStringValue(typeFI.m_index);
/*      */ 
/* 2325 */       boolean isBigText = typeStr.equalsIgnoreCase("bigtext");
/* 2326 */       boolean isMemo = typeStr.equalsIgnoreCase("memo");
/* 2327 */       if ((!isBigText) && (!isMemo)) {
/*      */         continue;
/*      */       }
/*      */ 
/* 2331 */       boolean isValidField = false;
/* 2332 */       for (int j = 0; (metaFields != null) && (j < metaFields.length); ++j)
/*      */       {
/* 2334 */         if (!name.equalsIgnoreCase(metaFields[j].m_name))
/*      */           continue;
/* 2336 */         isValidField = true;
/* 2337 */         break;
/*      */       }
/*      */ 
/* 2340 */       if (!isValidField) {
/*      */         continue;
/*      */       }
/*      */ 
/* 2344 */       if (isBigText)
/*      */       {
/* 2346 */         fieldList[0].addElement(name);
/*      */       } else {
/* 2348 */         if (!isMemo)
/*      */           continue;
/* 2350 */         fieldList[1].addElement(name);
/*      */       }
/*      */     }
/* 2353 */     return fieldList;
/*      */   }
/*      */ 
/*      */   protected void updateMetaFieldsV8(String table, String[] tablePK, Vector[] fieldList) throws DataException, ServiceException
/*      */   {
/* 2358 */     short bigTextIndex = 0;
/* 2359 */     short memoIndex = 1;
/* 2360 */     int[] fieldLen = new int[fieldList[bigTextIndex].size() + fieldList[memoIndex].size()];
/* 2361 */     int numBigTextField = fieldList[bigTextIndex].size();
/* 2362 */     fieldList[bigTextIndex].addAll(fieldList[memoIndex]);
/* 2363 */     String[] fieldArray = StringUtils.convertListToArray(fieldList[bigTextIndex]);
/* 2364 */     if (fieldArray.length <= 0)
/*      */       return;
/* 2366 */     for (int i = 0; i < fieldLen.length; ++i)
/*      */     {
/* 2368 */       if (i >= numBigTextField)
/*      */       {
/* 2370 */         fieldLen[i] = 0;
/*      */       }
/*      */       else
/*      */       {
/* 2374 */         fieldLen[i] = 200;
/*      */       }
/*      */     }
/* 2377 */     changeFieldLength(table, tablePK, fieldArray, fieldLen);
/*      */   }
/*      */ 
/*      */   protected void expandFormatFieldLengthsV8()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2383 */     String updateState = getDBConfigValue("DevUpdate", "FormatFieldLengthUpdateV8", null);
/* 2384 */     if ((SharedObjects.getEnvValueAsBoolean("UsePre80FormatFieldType", false)) || (updateState != null))
/*      */     {
/* 2386 */       reportProgress(null, 3);
/* 2387 */       return;
/*      */     }
/*      */ 
/* 2391 */     changeFieldLength("DocFormats", new String[] { "dFormat" }, new String[] { "dFormat" }, new int[] { 80 });
/*      */ 
/* 2393 */     changeFieldLength("ExtensionFormatMap", new String[] { "dExtension" }, new String[] { "dFormat" }, new int[] { 80 });
/*      */ 
/* 2395 */     changeFieldLength("Documents", new String[] { "dDocID" }, new String[] { "dFormat" }, new int[] { 80 });
/*      */ 
/* 2398 */     setDBConfigValue("DevUpdate", "FormatFieldLengthUpdateV8", "8.0", "1");
/*      */   }
/*      */ 
/*      */   protected void updateRefineryQueueFileNamesV8() throws ServiceException
/*      */   {
/* 2403 */     String[][] updateQueueNamesMap = { { "RawDocuments", "preconverted" }, { "ConvertedDocuments", "postconverted" } };
/*      */ 
/* 2408 */     String queueDir = DirectoryLocator.getDocConversionDirectory();
/* 2409 */     for (int q = 0; q < updateQueueNamesMap.length; ++q)
/*      */     {
/* 2411 */       String oldPath = queueDir + updateQueueNamesMap[q][0] + ".qdt";
/* 2412 */       String newPath = queueDir + updateQueueNamesMap[q][1] + ".qdt";
/* 2413 */       if (FileUtils.checkFile(oldPath, true, true) != 0)
/*      */         continue;
/* 2415 */       FileUtils.renameFile(oldPath, newPath);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateDocMetaDefinitionV8()
/*      */     throws DataException
/*      */   {
/* 2422 */     String updateState = getDBConfigValue("DevUpdate", "updateDocMetaDefinitionV8", null);
/* 2423 */     if (updateState != null)
/*      */     {
/* 2425 */       reportProgress(null, 1);
/* 2426 */       return;
/*      */     }
/*      */ 
/* 2429 */     String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "DocMetaDefinition");
/* 2430 */     reportProgress(msg, 1);
/*      */ 
/* 2432 */     String[] newColumns = { "dIsPlaceholderField" };
/* 2433 */     FieldInfo[] extraMetadefFI = createFieldInfo(newColumns, 8);
/* 2434 */     extraMetadefFI[0].m_type = 1;
/*      */ 
/* 2436 */     String[] metadefTablePK = { "dName" };
/* 2437 */     this.m_workspace.alterTable("DocMetaDefinition", extraMetadefFI, null, metadefTablePK);
/*      */ 
/* 2439 */     setDBConfigValue("DevUpdate", "updateDocMetaDefinitionV8", "8.0", "1");
/*      */   }
/*      */ 
/*      */   public void updateMimeType() throws DataException
/*      */   {
/* 2444 */     String updateState = getDBConfigValue("DevUpdate", "updateMimeType", null);
/* 2445 */     if (updateState != null)
/*      */     {
/* 2447 */       reportProgress(null, 1);
/* 2448 */       return;
/*      */     }
/*      */ 
/* 2451 */     String query = "UPDATE DocFormats SET dFormat = 'application/postscript' WHERE dFormat = 'test/postscript'";
/* 2452 */     boolean inTran = false;
/*      */     try
/*      */     {
/* 2455 */       this.m_workspace.addBatchSQL(query);
/* 2456 */       query = "UPDATE ExtensionFormatMap SET dFormat = 'application/postscript' WHERE dExtension = 'ps'";
/* 2457 */       this.m_workspace.addBatchSQL(query);
/* 2458 */       this.m_workspace.beginTran();
/* 2459 */       inTran = true;
/* 2460 */       this.m_workspace.executeBatch();
/* 2461 */       this.m_workspace.commitTran();
/* 2462 */       inTran = false;
/*      */     }
/*      */     finally
/*      */     {
/* 2466 */       if (inTran)
/*      */       {
/* 2468 */         this.m_workspace.rollbackTran();
/*      */       }
/* 2470 */       this.m_workspace.clearBatch();
/*      */     }
/*      */ 
/* 2473 */     setDBConfigValue("DevUpdate", "updateMimeType", "8.0", "1");
/*      */   }
/*      */ 
/*      */   public void createRefineryJobsTable() throws DataException
/*      */   {
/* 2478 */     String updateState = getDBConfigValue("DevUpdate", "DocsOutForConvertV8", null);
/* 2479 */     if ((updateState != null) && (updateState.equals("2")))
/*      */     {
/* 2481 */       reportProgress(null, 1);
/* 2482 */       return;
/*      */     }
/*      */ 
/* 2485 */     if (doesDBTableExist("DocsOutForConvert"))
/*      */     {
/* 2487 */       this.m_workspace.deleteTable("DocsOutForConvert");
/*      */     }
/* 2489 */     if (!doesDBTableExist("RefineryJobs"))
/*      */     {
/* 2492 */       String msg = LocaleUtils.encodeMessage("csCreatingTable", null, "RefineryJobs");
/*      */ 
/* 2494 */       reportProgress(msg, 1);
/*      */ 
/* 2496 */       FieldInfo[] attribsfi = createFieldInfo(new String[] { "dConvJobID", "dDocID", "dID", "dDocName", "dConversion", "dConversionState", "dConvProvider", "dConvStartDate", "dConvActionDate", "dConvMessage" }, 30);
/*      */ 
/* 2501 */       attribsfi[0].m_type = 3;
/* 2502 */       attribsfi[1].m_type = 3;
/* 2503 */       attribsfi[2].m_type = 3;
/* 2504 */       attribsfi[5].m_maxLen = 15;
/* 2505 */       attribsfi[7].m_type = 5;
/* 2506 */       attribsfi[8].m_type = 5;
/* 2507 */       attribsfi[9].m_maxLen = 255;
/*      */ 
/* 2509 */       this.m_workspace.createTable("RefineryJobs", attribsfi, new String[] { "dConvJobID" });
/*      */     }
/*      */     else
/*      */     {
/* 2514 */       reportProgress(null, 1);
/*      */     }
/*      */ 
/* 2517 */     String query = "INSERT INTO Counters (dCounterName, dNextIndex) VALUES('RefineryJobID', 1)";
/* 2518 */     this.m_workspace.executeSQL(query);
/*      */ 
/* 2521 */     setDBConfigValue("DevUpdate", "DocsOutForConvertV8", "8.0", "2");
/*      */   }
/*      */ 
/*      */   public void updateDocMetaDefinitionV11()
/*      */     throws DataException
/*      */   {
/* 2527 */     String updateState = getDBConfigValue("DevUpdate", "updateDocMetaDefinitionV11", null);
/* 2528 */     if (updateState != null)
/*      */     {
/* 2530 */       return;
/*      */     }
/* 2532 */     Report.trace(null, "updateDocMetaDefinitionV11: adding decimal column", null);
/* 2533 */     String[] newColumns = { "dDecimalScale" };
/* 2534 */     FieldInfo[] extraMetadefFI = createFieldInfo(newColumns, 8);
/* 2535 */     extraMetadefFI[0].m_type = 3;
/*      */ 
/* 2537 */     String[] metadefTablePK = { "dName" };
/* 2538 */     this.m_workspace.alterTable("DocMetaDefinition", extraMetadefFI, null, metadefTablePK);
/*      */ 
/* 2540 */     setDBConfigValue("DevUpdate", "updateDocMetaDefinitionV11", "11.0", "1");
/*      */   }
/*      */ 
/*      */   public void dataStoreDesignConfigTables()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2548 */     if (!this.m_isUseComponentDBTable)
/*      */       return;
/* 2550 */     DataDesignInstall ddi = getDataDesignInstall();
/* 2551 */     IntervalData interval = new IntervalData("synctables");
/*      */ 
/* 2553 */     Report.trace("datastoredesign", "Start: configure tables for components with componentdbinstall", null);
/* 2554 */     ddi.configTableForComponents(this.m_workspace, this.m_contextBinder, this);
/* 2555 */     Report.trace("datastoredesign", "Finished: configure tables for components with componentdbinstall", null);
/* 2556 */     interval.trace("startup", "Configure tables for core and components using data store design");
/*      */   }
/*      */ 
/*      */   public void dataStoreDesignConfigDocMeta()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2565 */     if (!this.m_isUseComponentDBTable)
/*      */       return;
/* 2567 */     DataDesignInstall ddi = getDataDesignInstall();
/* 2568 */     Report.trace("datastoredesign", "Start: configure DocMeta for components with componentdbinstall", null);
/* 2569 */     ddi.configDocMetaForComponent(this.m_workspace, this.m_contextBinder, this);
/* 2570 */     Report.trace("datastoredesign", "Finished: configure DocMeta for components with componentdbinstall", null);
/*      */   }
/*      */ 
/*      */   public void dataStoreDesignConfigProfile()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2579 */     if (!this.m_isUseComponentDBTable)
/*      */       return;
/* 2581 */     Report.trace("datastoredesign", "Start: configure profiles for components with componentdbinstall", null);
/* 2582 */     DataDesignProfileInstall configProfiles = new DataDesignProfileInstall();
/* 2583 */     configProfiles.configProfileForComponents(this.m_workspace, this.m_contextBinder, this);
/* 2584 */     Report.trace("datastoredesign", "Finished: configure profiles for components with componentdbinstall", null);
/*      */   }
/*      */ 
/*      */   public void dataStoreDesignSynchronizeWithSchema()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2593 */     if (!this.m_isUseComponentDBTable)
/*      */       return;
/* 2595 */     DataDesignInstall ddi = getDataDesignInstall();
/* 2596 */     ddi.synchronizeUpdatedTablesWithSchema(this.m_workspace, this.m_contextBinder, this);
/*      */   }
/*      */ 
/*      */   public void updateRoleDefinitionV11()
/*      */     throws DataException
/*      */   {
/* 2602 */     Workspace ws = WorkspaceUtils.getWorkspace("user");
/* 2603 */     ResultSet rset = ws.createResultSetSQL("SELECT * FROM RoleDefinition");
/* 2604 */     FieldInfo fTemp = new FieldInfo();
/* 2605 */     if (!rset.getFieldInfo("dRoleDisplayName", fTemp))
/*      */     {
/* 2607 */       String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "RoleDefinition");
/* 2608 */       reportProgress(msg, 1);
/*      */ 
/* 2610 */       String[] newColumns = { "dRoleDisplayName" };
/* 2611 */       FieldInfo[] fi = createFieldInfo(newColumns, 100);
/*      */ 
/* 2613 */       String[] pk = { "dRoleName", "dGroupName" };
/* 2614 */       ws.alterTable("RoleDefinition", fi, null, pk);
/*      */     }
/*      */     else
/*      */     {
/* 2618 */       reportProgress(null, 1);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateAliasV11() throws DataException
/*      */   {
/* 2624 */     Workspace ws = WorkspaceUtils.getWorkspace("user");
/* 2625 */     ResultSet rset = ws.createResultSetSQL("SELECT * FROM Alias");
/* 2626 */     FieldInfo fTemp = new FieldInfo();
/* 2627 */     if (!rset.getFieldInfo("dAliasDisplayName", fTemp))
/*      */     {
/* 2629 */       String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, "Alias");
/* 2630 */       reportProgress(msg, 1);
/*      */ 
/* 2632 */       String[] newColumns = { "dAliasDisplayName" };
/* 2633 */       FieldInfo[] fi = createFieldInfo(newColumns, 100);
/*      */ 
/* 2635 */       String[] pk = { "dAlias" };
/* 2636 */       ws.alterTable("Alias", fi, null, pk);
/*      */     }
/*      */     else
/*      */     {
/* 2640 */       reportProgress(null, 1);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void upgradeCounters()
/*      */     throws DataException
/*      */   {
/* 2648 */     boolean isAlreadyUpgraded = false;
/*      */ 
/* 2650 */     String updateState = getDBConfigValue("DevUpdate", "RegisteredCountersUpgrade", null);
/* 2651 */     if (updateState != null)
/*      */     {
/* 2653 */       isAlreadyUpgraded = true;
/*      */     }
/* 2655 */     boolean doUpgradeCounters = SharedObjects.getEnvValueAsBoolean("DoUpgradeCounters", false);
/*      */ 
/* 2657 */     if ((!isAlreadyUpgraded) || (doUpgradeCounters == true))
/*      */     {
/* 2659 */       Report.trace(null, "Performing RegisteredCounters upgrade", null);
/* 2660 */       ResultSet rset = this.m_workspace.createResultSetSQL("SELECT * FROM Counters WHERE dCounterName != 'TranLock'");
/* 2661 */       DataResultSet drset = new DataResultSet();
/* 2662 */       drset.copy(rset);
/*      */ 
/* 2664 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/* 2666 */         String name = drset.getStringValueByName("dCounterName");
/* 2667 */         String initValueStr = drset.getStringValueByName("dNextIndex");
/* 2668 */         long initValue = NumberUtils.parseLong(initValueStr, -1L);
/* 2669 */         IdcCounterUtils.registerCounter(this.m_workspace, name, initValue, 1);
/*      */       }
/*      */ 
/* 2672 */       setDBConfigValue("DevUpdate", "RegisteredCountersUpgrade", "11.1.1.2.0", "1");
/*      */     }
/*      */ 
/* 2678 */     boolean doBackPortCounters = SharedObjects.getEnvValueAsBoolean("DoBackPortCounters", false);
/*      */ 
/* 2680 */     if (doBackPortCounters != true)
/*      */       return;
/* 2682 */     Report.trace(null, "Performing RegisteredCounters backport", null);
/* 2683 */     ResultSet regCntRset = this.m_workspace.createResultSet("QallRegisteredCounters", null);
/* 2684 */     DataResultSet regCntDrset = new DataResultSet();
/* 2685 */     regCntDrset.copy(regCntRset);
/*      */ 
/* 2687 */     for (regCntDrset.first(); regCntDrset.isRowPresent(); regCntDrset.next())
/*      */     {
/* 2689 */       String counterName = regCntDrset.getStringValueByName("dCounterName");
/* 2690 */       String counterKey = IdcCounterUtils.getKey(this.m_workspace, counterName);
/* 2691 */       IdcCounter currentCounter = null;
/*      */ 
/* 2693 */       if ((IdcCounterUtils.m_counters != null) && (IdcCounterUtils.m_counters.get(counterKey) != null))
/*      */       {
/* 2695 */         currentCounter = (IdcCounter)IdcCounterUtils.m_counters.get(counterKey);
/*      */       }
/*      */       else
/*      */       {
/* 2699 */         String counterType = regCntDrset.getStringValueByName("dCounterType");
/* 2700 */         String initValueStr = regCntDrset.getStringValueByName("dCounterInitValue");
/* 2701 */         String incrementStr = regCntDrset.getStringValueByName("dCounterIncrement");
/*      */ 
/* 2703 */         long initValue = NumberUtils.parseLong(initValueStr, 1L);
/* 2704 */         int increment = NumberUtils.parseInteger(incrementStr, 1);
/*      */ 
/* 2706 */         currentCounter = IdcCounterUtils.createIdcCounter(this.m_workspace, counterName, counterType, initValue, increment);
/*      */       }
/*      */ 
/* 2710 */       long nextIndex = currentCounter.nextValue(this.m_workspace);
/* 2711 */       DataBinder counterBinder = new DataBinder();
/* 2712 */       counterBinder.putLocal("dCounterName", counterName);
/*      */ 
/* 2714 */       ResultSet counterRset = this.m_workspace.createResultSet("QnextCounter", counterBinder);
/* 2715 */       if (counterRset.isEmpty())
/*      */       {
/* 2717 */         counterBinder.putLocal("dCounterInitValue", Long.toString(nextIndex));
/* 2718 */         this.m_workspace.execute("IlegacyCounterDef", counterBinder);
/*      */       }
/*      */       else
/*      */       {
/* 2722 */         counterBinder.putLocal("dNextIndex", Long.toString(nextIndex));
/* 2723 */         this.m_workspace.execute("Ucounter", counterBinder);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public DataDesignInstall getDataDesignInstall()
/*      */     throws ServiceException
/*      */   {
/* 2735 */     DataDesignInstall configTables = (DataDesignInstall)getCachedObject("ConfigTables");
/* 2736 */     if (configTables == null)
/*      */     {
/* 2738 */       configTables = new DataDesignInstall();
/* 2739 */       setCachedObject("ConfigTables", configTables);
/*      */     }
/*      */ 
/* 2742 */     return configTables;
/*      */   }
/*      */ 
/*      */   public void addDataSourceFields() throws DataException
/*      */   {
/* 2747 */     checkOrAddColumn("DocMetaDefinition", "dCategory", 100, 1);
/* 2748 */     checkOrAddColumn("DocMetaDefinition", "dExtraDefinition", -1, 0);
/*      */     try
/*      */     {
/* 2753 */       MetaFieldUtils.addMetaData(this.m_workspace, IdcSystemLoader.m_cxt, "xExternalDataSet", "wwExternalDataSet", "BigText", "0", "0", "0", "0", "", "", "", "5");
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 2758 */       throw new DataException(e, "csUnableToAddxExternalDataSet", new Object[0]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addProfileField()
/*      */     throws DataException
/*      */   {
/*      */     try
/*      */     {
/* 2767 */       reportProgress(null, 1);
/* 2768 */       MetaFieldUtils.addMetaData(this.m_workspace, IdcSystemLoader.m_cxt, "xIdcProfile", "wwProfile", "Text", "0", "1", "1", "1", "", "view://ProfileTriggerValues", "choice", "6");
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 2774 */       throw new DataException(e, "csUnableToAddxIdcProfile", new Object[0]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void computeRevClassesTableUpgrade()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2788 */     String updateState = getDBConfigValue("DevUpdate", "RevClasses", null);
/* 2789 */     if (updateState != null)
/*      */     {
/* 2792 */       return;
/*      */     }
/*      */ 
/* 2796 */     String alertId = "csRevClassesPopulateWarning";
/*      */     try
/*      */     {
/* 2799 */       boolean revClassAlertExists = AlertUtils.existsAlert(alertId, 1);
/*      */ 
/* 2802 */       if (revClassAlertExists == true)
/*      */       {
/* 2804 */         DataBinder alertbinder = new DataBinder();
/* 2805 */         alertbinder.putLocal("alertId", alertId);
/*      */ 
/* 2807 */         AlertUtils.deleteAlert(alertbinder);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2812 */       Report.error(null, e, new IdcMessage("csAlertDeletionError", new Object[] { alertId }));
/*      */     }
/*      */ 
/* 2816 */     ResultSet totalRevClassCountRset = this.m_workspace.createResultSet("QrevClassTotalIdsInRevisions", null);
/* 2817 */     String totalRevClassCountString = totalRevClassCountRset.getStringValue(0);
/* 2818 */     int totalRevClassCountInRevisions = NumberUtils.parseInteger(totalRevClassCountString, 0);
/*      */ 
/* 2820 */     if (totalRevClassCountInRevisions == 0)
/*      */     {
/* 2823 */       return;
/*      */     }
/* 2825 */     this.m_doesRevClassesTableNeedUpgrade = true;
/*      */ 
/* 2828 */     if (totalRevClassCountInRevisions > 50000)
/*      */     {
/* 2830 */       boolean doRevClassUpgrade = SharedObjects.getEnvValueAsBoolean("DoRevClassUpgrade", false);
/* 2831 */       if ((!doRevClassUpgrade) && (!doesDBTableExist("RevClasses")))
/*      */       {
/* 2833 */         IdcMessage msg = new IdcMessage("csRevClassesPopulateWarning", new Object[0]);
/* 2834 */         Report.warning(null, null, msg);
/*      */ 
/* 2837 */         DataBinder alertBinder = new DataBinder();
/* 2838 */         alertBinder.putLocal("alertId", "csRevClassesPopulateWarning");
/* 2839 */         alertBinder.putLocal("alertMsg", "<$lcMessage('" + LocaleUtils.encodeMessage(msg) + "')$>");
/*      */         try
/*      */         {
/* 2842 */           AlertUtils.setAlert(alertBinder);
/*      */         }
/*      */         catch (IdcException ignore)
/*      */         {
/*      */         }
/*      */ 
/* 2848 */         return;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2853 */     ResultSet minRevClassRset = this.m_workspace.createResultSet("QrevClassMinInRevisions", null);
/* 2854 */     String minRevClassIdString = minRevClassRset.getStringValue(0);
/* 2855 */     int minRevClassIdInRevisionsTable = NumberUtils.parseInteger(minRevClassIdString, 0);
/*      */ 
/* 2858 */     ResultSet maxRevClassRset = this.m_workspace.createResultSet("QrevClassMaxInRevisions", null);
/* 2859 */     String maxRevClassIdString = maxRevClassRset.getStringValue(0);
/* 2860 */     int maxRevClassIdInRevisionsTable = NumberUtils.parseInteger(maxRevClassIdString, 0);
/*      */ 
/* 2863 */     this.m_minRevClassBoundary = minRevClassIdInRevisionsTable;
/* 2864 */     this.m_maxRevClassBoundary = maxRevClassIdInRevisionsTable;
/*      */ 
/* 2867 */     if (!doesDBTableExist("RevClasses"))
/*      */       return;
/* 2869 */     ResultSet existingRevClassCountRset = this.m_workspace.createResultSet("QrevClassTotalCount", null);
/* 2870 */     String existingRevClassCountString = existingRevClassCountRset.getStringValue(0);
/* 2871 */     int existingRevClassCount = NumberUtils.parseInteger(existingRevClassCountString, 0);
/* 2872 */     if (existingRevClassCount <= 0)
/*      */       return;
/* 2874 */     ResultSet existingRevClassMinRset = this.m_workspace.createResultSet("QrevClassMin", null);
/* 2875 */     String existingRevClassMinString = existingRevClassMinRset.getStringValue(0);
/* 2876 */     int existingMinRevClassId = NumberUtils.parseInteger(existingRevClassMinString, 0);
/*      */ 
/* 2878 */     ResultSet existingRevClassMaxRset = this.m_workspace.createResultSet("QrevClassMax", null);
/* 2879 */     String existingRevClassMaxString = existingRevClassMaxRset.getStringValue(0);
/* 2880 */     int existingMaxRevClassId = NumberUtils.parseInteger(existingRevClassMaxString, 0);
/*      */ 
/* 2883 */     if ((existingMinRevClassId == minRevClassIdInRevisionsTable) && (existingMaxRevClassId < maxRevClassIdInRevisionsTable))
/*      */     {
/* 2887 */       this.m_minRevClassBoundary = (existingMaxRevClassId + 1);
/*      */     }
/* 2889 */     if ((existingMinRevClassId > minRevClassIdInRevisionsTable) && (existingMaxRevClassId == maxRevClassIdInRevisionsTable))
/*      */     {
/* 2893 */       this.m_maxRevClassBoundary = (existingMinRevClassId - 1);
/*      */     }
/* 2895 */     if ((this.m_minRevClassBoundary != minRevClassIdInRevisionsTable) || (this.m_maxRevClassBoundary != maxRevClassIdInRevisionsTable)) {
/*      */       return;
/*      */     }
/*      */ 
/* 2899 */     this.m_doRevClassExistenceCheck = true;
/*      */   }
/*      */ 
/*      */   public void populateRevClassesTable()
/*      */     throws DataException
/*      */   {
/* 2912 */     if (!this.m_doesRevClassesTableNeedUpgrade)
/*      */       return;
/* 2914 */     int maxBoundary = this.m_maxRevClassBoundary;
/* 2915 */     int lowRevClassId = this.m_minRevClassBoundary;
/* 2916 */     int highRevClassId = lowRevClassId + 10000 - 1;
/* 2917 */     if (highRevClassId > maxBoundary)
/*      */     {
/* 2919 */       highRevClassId = maxBoundary;
/*      */     }
/* 2921 */     int numTotalRecords = getNumRevClassRows();
/*      */ 
/* 2924 */     boolean hasProcessedAll = false;
/* 2925 */     long currentRecordsProcessedCount = 0L;
/*      */ 
/* 2927 */     while ((lowRevClassId <= highRevClassId) && (highRevClassId <= maxBoundary) && (!hasProcessedAll))
/*      */     {
/* 2929 */       int[] revClassRangeData = WorkspaceUtils.getRangeBoundaries(this.m_workspace, lowRevClassId, highRevClassId, maxBoundary, 10000, "QrevClassRangeCountInRevisions");
/*      */ 
/* 2932 */       lowRevClassId = revClassRangeData[0];
/* 2933 */       highRevClassId = revClassRangeData[1];
/*      */ 
/* 2935 */       DataBinder binder = new DataBinder();
/* 2936 */       binder.putLocal("lowRevClassID", Integer.toString(lowRevClassId));
/* 2937 */       binder.putLocal("highRevClassID", Integer.toString(highRevClassId));
/*      */ 
/* 2939 */       boolean inTransaction = false;
/*      */       try
/*      */       {
/* 2942 */         this.m_workspace.beginTran();
/*      */ 
/* 2944 */         inTransaction = true;
/*      */ 
/* 2946 */         if (this.m_doRevClassExistenceCheck == true)
/*      */         {
/* 2948 */           currentRecordsProcessedCount = this.m_workspace.execute("IrevClassesUsingRevisionsWithCheck", binder);
/*      */         }
/*      */         else
/*      */         {
/* 2952 */           currentRecordsProcessedCount = this.m_workspace.execute("IrevClassesUsingRevisions", binder);
/*      */         }
/* 2954 */         this.m_workspace.execute("UrevClassesUsingRevisions", binder);
/*      */ 
/* 2956 */         this.m_workspace.commitTran();
/*      */ 
/* 2958 */         inTransaction = false;
/*      */       }
/*      */       finally
/*      */       {
/* 2962 */         if (inTransaction == true)
/*      */         {
/* 2964 */           this.m_workspace.rollbackTran();
/*      */         }
/*      */       }
/*      */ 
/* 2968 */       String traceMsg = LocaleResources.getString("csRevClassesPopulateProgress", this, new Object[] { Long.valueOf(currentRecordsProcessedCount), Integer.valueOf(numTotalRecords), Integer.valueOf(lowRevClassId), Integer.valueOf(highRevClassId) });
/*      */ 
/* 2971 */       reportProgress(traceMsg, 1);
/*      */ 
/* 2973 */       if (highRevClassId >= maxBoundary)
/*      */       {
/* 2975 */         hasProcessedAll = true;
/*      */       }
/*      */       else
/*      */       {
/* 2979 */         lowRevClassId = highRevClassId + 1;
/* 2980 */         highRevClassId = lowRevClassId + 10000 - 1;
/* 2981 */         if (highRevClassId > maxBoundary)
/*      */         {
/* 2983 */           highRevClassId = maxBoundary;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2989 */     setDBConfigValue("DevUpdate", "RevClasses", "11.1.1.2.0", "1");
/*      */   }
/*      */ 
/*      */   public boolean checkOrAddColumn(String table, String column, int defLen, int prog)
/*      */     throws DataException
/*      */   {
/* 2996 */     FieldInfo fi = WorkspaceUtils.getColumnInfo(table, column, this.m_workspace);
/* 2997 */     if (fi == null)
/*      */     {
/* 2999 */       String[] newColumns = { column };
/* 3000 */       FieldInfo[] fis = createFieldInfo(newColumns, defLen);
/* 3001 */       WorkspaceUtils.addColumn(this.m_workspace, table, fis);
/* 3002 */       String msg = LocaleUtils.encodeMessage("csUpdatingTable", null, table);
/* 3003 */       reportProgress(msg, prog);
/* 3004 */       return true;
/*      */     }
/*      */ 
/* 3007 */     reportProgress(null, prog);
/* 3008 */     return false;
/*      */   }
/*      */ 
/*      */   public void addInstallFiles()
/*      */   {
/* 3018 */     String dataDir = DirectoryLocator.getAppDataDirectory();
/* 3019 */     String dir = DirectoryLocator.getSharedDirectory() + "config/data/";
/* 3020 */     File f = FileUtilsCfgBuilder.getCfgFile(dir, null, true);
/* 3021 */     if (!f.exists())
/*      */     {
/* 3024 */       return;
/*      */     }
/*      */ 
/* 3027 */     DataBinder envBinder = new DataBinder(SharedObjects.getSecureEnvironment());
/* 3028 */     PageMerger pageMerger = new PageMerger(envBinder, null);
/*      */ 
/* 3030 */     String[] manifestFiles = FileUtils.getMatchingFileNames(dir, "*.hda");
/* 3031 */     int num = manifestFiles.length;
/* 3032 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 3034 */       String manifest = manifestFiles[i];
/*      */       try
/*      */       {
/* 3037 */         DataBinder binder = ResourceUtils.readDataBinder(dir, manifest);
/* 3038 */         String version = binder.getLocal("Version");
/*      */ 
/* 3041 */         f = FileUtilsCfgBuilder.getCfgFile(dataDir + manifest, null, false);
/* 3042 */         if (f.exists())
/*      */         {
/* 3044 */           DataBinder oldBinder = ResourceUtils.readDataBinder(dataDir, manifest);
/* 3045 */           String oldVersion = oldBinder.getLocal("Version");
/* 3046 */           boolean isOld = SystemUtils.isOlderVersion(oldVersion, version);
/* 3047 */           if (!isOld);
/*      */         }
/*      */ 
/* 3055 */         DataResultSet drset = (DataResultSet)binder.getResultSet("Manifest");
/* 3056 */         if (drset != null)
/*      */         {
/* 3061 */           String[] clmns = { "fileType", "source", "targetDir", "copyRule", "wildCards" };
/* 3062 */           FieldInfo[] fis = ResultSetUtils.createInfoList(drset, clmns, true);
/* 3063 */           for (drset.first(); drset.isRowPresent(); drset.next())
/*      */           {
/* 3065 */             String fileType = drset.getStringValue(fis[0].m_index);
/* 3066 */             String src = drset.getStringValue(fis[1].m_index);
/*      */ 
/* 3069 */             String target = drset.getStringValue(fis[2].m_index);
/* 3070 */             target = pageMerger.evaluateScript(target);
/* 3071 */             target = FileUtils.directorySlashes(target);
/*      */ 
/* 3074 */             String copyRule = drset.getStringValue(fis[3].m_index);
/* 3075 */             boolean isForce = copyRule.equals("force");
/* 3076 */             int copyFlags = 0;
/* 3077 */             if (!isForce)
/*      */             {
/* 3079 */               copyFlags = 4;
/*      */             }
/*      */ 
/* 3082 */             String wildCard = drset.getStringValue(fis[4].m_index);
/* 3083 */             if (wildCard.length() == 0)
/*      */             {
/* 3085 */               wildCard = "*";
/*      */             }
/*      */ 
/* 3088 */             if (fileType.equals("file"))
/*      */             {
/* 3090 */               File from = FileUtilsCfgBuilder.getCfgFile(dir + src, null, false);
/* 3091 */               if (from.exists())
/*      */               {
/* 3093 */                 String name = FileUtils.getName(src);
/* 3094 */                 String targetPath = target + name;
/* 3095 */                 File to = FileUtilsCfgBuilder.getCfgFile(targetPath, null, false);
/* 3096 */                 if ((!to.exists()) || (isForce))
/*      */                 {
/* 3099 */                   String trgDir = FileUtils.getDirectory(targetPath);
/* 3100 */                   FileUtils.checkOrCreateDirectoryPrepareForLocks(trgDir, 5, true);
/*      */ 
/* 3103 */                   FileUtils.copyFileEx(from.getAbsolutePath(), to.getAbsolutePath(), copyFlags);
/*      */                 }
/*      */               }
/*      */               else
/*      */               {
/* 3108 */                 String errMsg = LocaleUtils.encodeMessage("csMIMissingFile", null, dir + src);
/* 3109 */                 throw new ServiceException(errMsg);
/*      */               }
/*      */             }
/* 3112 */             else if (fileType.equals("directory"))
/*      */             {
/* 3114 */               String srcDir = FileUtils.directorySlashes(dir + "/" + src);
/* 3115 */               copyFiles(srcDir, target, wildCard, isForce);
/*      */             }
/*      */             else
/*      */             {
/* 3119 */               String errMsg = LocaleUtils.encodeMessage("csMIUnsupportedType", null, fileType);
/* 3120 */               throw new ServiceException(errMsg);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 3125 */           FileUtils.copyFile(dir + manifest, dataDir + manifest);
/*      */         }
/*      */       }
/*      */       catch (Exception e) {
/* 3129 */         Report.warning(null, e, "csMIFailed", new Object[] { manifest });
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void dMessageRepair()
/*      */   {
/* 3136 */     String dMessageRepairEnabled = SharedObjects.getEnvironmentValue("dMessageFieldRepairEnabled");
/* 3137 */     boolean defValue = (SystemUtils.m_isDevelopmentEnvironment) && (Features.checkLevel("ContentManagement", null));
/*      */ 
/* 3139 */     if ((!StringUtils.convertToBool(dMessageRepairEnabled, defValue)) || (StringUtils.convertToBool(ActiveState.getActiveProperty("dMessageFieldRepaired"), false))) {
/*      */       return;
/*      */     }
/* 3142 */     Report.trace("startup", "Repairing message strings in Revisions.dMessage.\nRecord that work is done is reported in activestate.hda.", null);
/*      */     try
/*      */     {
/* 3147 */       this.m_workspace.executeSQL("Update Revisions set dMessage = '!apSuccess' WHERE dMessage = 'Success'");
/*      */ 
/* 3149 */       Report.trace("startup", "Finished repairing Revisions.dMessage.", null);
/*      */ 
/* 3151 */       ActiveState.setActiveProperty("dMessageFieldRepaired", "1");
/* 3152 */       ActiveState.save();
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 3156 */       Report.trace(null, "Unable to repair Revisions.dMessage column.", t);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateDocFormatsAndExtensionFormatMap()
/*      */     throws DataException
/*      */   {
/* 3163 */     String updateState = getDBConfigValue("DevUpdate", "DocFormatsAndExtensionFormatMapUpgrade", null);
/* 3164 */     if (updateState != null)
/*      */     {
/* 3166 */       return;
/*      */     }
/*      */ 
/* 3169 */     this.m_workspace.executeSQL("UPDATE DocFormats SET dIsEnabled = '1'");
/* 3170 */     this.m_workspace.executeSQL("UPDATE ExtensionFormatMap SET dIsEnabled = '1'");
/*      */ 
/* 3173 */     setDBConfigValue("DevUpdate", "DocFormatsAndExtensionFormatMapUpgrade", "11.1", "1");
/*      */   }
/*      */ 
/*      */   public boolean addColumns(String table, String[] tablePrimaryKey, String[] columns)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3182 */     FieldInfo[] infoArray = this.m_workspace.getColumnList(table);
/* 3183 */     List addingColumns = new ArrayList();
/* 3184 */     for (String columnDef : columns)
/*      */     {
/* 3186 */       boolean found = false;
/* 3187 */       FieldInfo newFieldInfo = new FieldInfo(columnDef);
/* 3188 */       for (FieldInfo info : infoArray)
/*      */       {
/* 3190 */         if (!info.m_name.equalsIgnoreCase(newFieldInfo.m_name))
/*      */           continue;
/* 3192 */         found = true;
/* 3193 */         break;
/*      */       }
/*      */ 
/* 3196 */       if (found) {
/*      */         continue;
/*      */       }
/*      */ 
/* 3200 */       addingColumns.add(newFieldInfo);
/*      */     }
/* 3202 */     FieldInfo[] columnList = new FieldInfo[addingColumns.size()];
/* 3203 */     columnList = (FieldInfo[])(FieldInfo[])addingColumns.toArray(columnList);
/*      */ 
/* 3205 */     if (columnList.length > 0)
/*      */     {
/* 3207 */       this.m_workspace.alterTable(table, columnList, null, tablePrimaryKey);
/* 3208 */       return true;
/*      */     }
/* 3210 */     return false;
/*      */   }
/*      */ 
/*      */   public int getDocNameLength()
/*      */     throws DataException
/*      */   {
/* 3220 */     Object o = getCachedObject("dDocName:length");
/* 3221 */     if ((o != null) && (o instanceof Integer))
/*      */     {
/* 3223 */       Integer l = (Integer)o;
/* 3224 */       int result = l.intValue();
/* 3225 */       return result;
/*      */     }
/* 3227 */     String query = "SELECT dDocName FROM Revisions WHERE dID=0";
/* 3228 */     ResultSet rset = this.m_workspace.createResultSetSQL(query);
/* 3229 */     FieldInfo fi = new FieldInfo();
/* 3230 */     rset.getFieldInfo("dDocName", fi);
/* 3231 */     int docNameLength = fi.m_maxLen;
/* 3232 */     if (docNameLength < 30)
/*      */     {
/* 3234 */       docNameLength = 30;
/*      */     }
/* 3236 */     Integer savedLength = new Integer(docNameLength);
/* 3237 */     setCachedObject("dDocName:length", savedLength);
/* 3238 */     return docNameLength;
/*      */   }
/*      */ 
/*      */   public void copyFiles(String srcDir, String target, String wildCard, boolean isForce)
/*      */     throws ServiceException
/*      */   {
/* 3244 */     int copyFlags = 0;
/* 3245 */     if (!isForce)
/*      */     {
/* 3247 */       copyFlags = 4;
/*      */     }
/*      */ 
/* 3251 */     String[] dirFiles = FileUtils.getMatchingFileNames(srcDir, wildCard);
/* 3252 */     int count = dirFiles.length;
/* 3253 */     for (int j = 0; j < count; ++j)
/*      */     {
/* 3255 */       String name = dirFiles[j];
/* 3256 */       File from = FileUtilsCfgBuilder.getCfgFile(srcDir + name, null);
/* 3257 */       if (!from.isDirectory())
/*      */       {
/* 3259 */         String targetPath = target + name;
/* 3260 */         File to = FileUtilsCfgBuilder.getCfgFile(targetPath, null);
/* 3261 */         if ((!to.exists()) || (isForce))
/*      */         {
/* 3265 */           String trgDir = FileUtils.getDirectory(targetPath);
/* 3266 */           FileUtils.checkOrCreateDirectoryPrepareForLocks(trgDir, 5, true);
/*      */ 
/* 3268 */           FileUtils.copyFileEx(from.getAbsolutePath(), to.getAbsolutePath(), copyFlags);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 3273 */         copyFiles(srcDir + name + "/", target + name + "/", wildCard, isForce);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getDBConfigValue(String section, String name, String appVersionCreated)
/*      */     throws DataException
/*      */   {
/* 3281 */     if (this.m_workspace == null)
/*      */     {
/* 3283 */       return null;
/*      */     }
/*      */ 
/* 3286 */     String query = "SELECT dValue FROM Config WHERE dSection='" + section + "' AND dName='" + name;
/*      */ 
/* 3288 */     if (appVersionCreated != null)
/*      */     {
/* 3290 */       query = query + "' AND dVersion='" + appVersionCreated;
/*      */     }
/* 3292 */     query = query + "'";
/*      */ 
/* 3294 */     ResultSet rset = this.m_workspace.createResultSetSQL(query);
/* 3295 */     if (rset.isRowPresent())
/*      */     {
/* 3297 */       return ResultSetUtils.getValue(rset, "dValue");
/*      */     }
/*      */ 
/* 3300 */     return null;
/*      */   }
/*      */ 
/*      */   public void setDBConfigValue(String section, String name, String appVersionCreated, String value)
/*      */     throws DataException
/*      */   {
/* 3306 */     if (this.m_workspace == null)
/*      */     {
/* 3308 */       throw new DataException("!csUnableToSetConfigNoDatabase");
/*      */     }
/*      */ 
/* 3312 */     boolean inTran = false;
/*      */     try
/*      */     {
/* 3315 */       this.m_workspace.beginTran();
/* 3316 */       inTran = true;
/*      */ 
/* 3318 */       if (getDBConfigValue(section, name, appVersionCreated) != null)
/*      */       {
/* 3320 */         String deleteSQL = "DELETE from Config WHERE dSection='" + section + "' AND dName='" + name + "' AND dVersion='" + appVersionCreated + "'";
/*      */ 
/* 3322 */         this.m_workspace.executeSQL(deleteSQL);
/*      */       }
/*      */ 
/* 3325 */       String insertSQL = "INSERT INTO Config (dSection, dName, dVersion, dValue) values ('" + section + "', '" + name + "', '" + appVersionCreated + "', '" + value + "')";
/*      */ 
/* 3329 */       this.m_workspace.executeSQL(insertSQL);
/* 3330 */       this.m_workspace.commitTran();
/*      */ 
/* 3332 */       inTran = false;
/*      */     }
/*      */     finally
/*      */     {
/* 3336 */       if (inTran == true)
/*      */       {
/* 3338 */         this.m_workspace.rollbackTran();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public FieldInfo[] createFieldInfo(String[] fields, int defLen)
/*      */   {
/* 3347 */     FieldInfo[] fi = new FieldInfo[fields.length];
/* 3348 */     for (int i = 0; i < fi.length; ++i)
/*      */     {
/* 3350 */       fi[i] = new FieldInfo();
/* 3351 */       fi[i].m_name = fields[i];
/* 3352 */       fi[i].m_isFixedLen = (defLen > 0);
/* 3353 */       fi[i].m_maxLen = defLen;
/*      */     }
/* 3355 */     return fi;
/*      */   }
/*      */ 
/*      */   public boolean doesDBTableExist(String tableName) throws DataException
/*      */   {
/* 3360 */     return doesDBTableExistEx(tableName, this.m_workspace);
/*      */   }
/*      */ 
/*      */   public boolean doesDBTableExistEx(String tableName, Workspace ws) throws DataException
/*      */   {
/* 3365 */     loadTableList(ws);
/* 3366 */     return WorkspaceUtils.doesTableExist(ws, tableName, this.m_dbTables);
/*      */   }
/*      */ 
/*      */   public void loadTableList(Workspace ws) throws DataException
/*      */   {
/* 3371 */     if (this.m_dbTables != null)
/*      */       return;
/* 3373 */     this.m_dbTables = ws.getTableList();
/*      */ 
/* 3375 */     if (!WorkspaceUtils.EBRModeActive(ws))
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/* 3380 */     String[] viewList = WorkspaceUtils.getViewList(ws, null);
/* 3381 */     this.m_dbTables = StringUtils.mergeStringArrays(this.m_dbTables, viewList);
/*      */   }
/*      */ 
/*      */   public void changeFieldLength(String tablename, String[] pk, String[] fields, int[] newLen)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3393 */     String msg = LocaleUtils.encodeMessage("csChangingFieldLengths", null, tablename);
/*      */ 
/* 3395 */     reportProgress(msg, 0);
/* 3396 */     boolean pkChanged = false;
/* 3397 */     for (int i = 0; i < fields.length; ++i)
/*      */     {
/* 3399 */       if (StringUtils.findStringIndex(pk, fields[i]) < 0)
/*      */         continue;
/* 3401 */       pkChanged = true;
/* 3402 */       break;
/*      */     }
/*      */ 
/* 3406 */     if ((pkChanged) || (!this.m_workspace.supportsSqlColumnChange()))
/*      */     {
/* 3409 */       FieldInfo[] fieldList = this.m_workspace.getColumnList(tablename);
/* 3410 */       int nTableFields = fieldList.length;
/*      */ 
/* 3413 */       FieldInfo[] newTableFields = new FieldInfo[nTableFields];
/*      */ 
/* 3415 */       for (int j = 0; j < nTableFields; ++j)
/*      */       {
/* 3417 */         FieldInfo fieldInfo = fieldList[j];
/* 3418 */         for (i = 0; i < fields.length; ++i)
/*      */         {
/* 3420 */           if (!fieldInfo.m_name.equalsIgnoreCase(fields[i]))
/*      */             continue;
/* 3422 */           fieldInfo.m_maxLen = newLen[i];
/* 3423 */           if (newLen[i] > 0)
/*      */             continue;
/* 3425 */           fieldInfo.m_isFixedLen = false;
/*      */         }
/*      */ 
/* 3429 */         newTableFields[j] = fieldInfo;
/*      */       }
/*      */ 
/* 3433 */       SerializeTable st = new SerializeTable(this.m_workspace, this.m_progress);
/* 3434 */       String dir = DirectoryLocator.getAppDataDirectory();
/* 3435 */       String name = tablename + "_upgrade.hda";
/* 3436 */       String tmpName = tablename + "_upgrade_temp.hda";
/* 3437 */       name = name.toLowerCase();
/* 3438 */       tmpName = tmpName.toLowerCase();
/* 3439 */       String filePath = dir + name;
/* 3440 */       if (FileUtils.checkFile(filePath, false, false) != 0)
/*      */       {
/* 3442 */         st.serialize(dir, tmpName, tablename, null, true);
/* 3443 */         FileUtils.renameFile(dir + tmpName, dir + name);
/*      */       }
/* 3445 */       this.m_workspace.deleteTable(tablename);
/* 3446 */       this.m_workspace.createTable(tablename, newTableFields, pk);
/* 3447 */       st.serialize(dir, name, tablename, null, false);
/* 3448 */       FileUtils.deleteFile(dir + name);
/*      */     }
/*      */     else
/*      */     {
/* 3452 */       FieldInfo[] fiNew = createFieldInfo(fields, 30);
/* 3453 */       for (i = 0; i < newLen.length; ++i)
/*      */       {
/* 3455 */         if (newLen[i] <= 0)
/*      */         {
/* 3457 */           fiNew[i].m_isFixedLen = false;
/*      */         }
/* 3459 */         fiNew[i].m_maxLen = newLen[i];
/*      */       }
/*      */ 
/* 3463 */       this.m_workspace.alterTable(tablename, fiNew, null, pk);
/*      */     }
/* 3465 */     reportProgress(null, 1);
/*      */   }
/*      */ 
/*      */   public String createFieldCopyQuery(String table, String[] fromFields, String[] toFields)
/*      */   {
/* 3470 */     String updateQuery = "update " + table + " set ";
/* 3471 */     for (int i = 0; i < fromFields.length; ++i)
/*      */     {
/* 3473 */       if (i > 0)
/*      */       {
/* 3475 */         updateQuery = updateQuery + ", ";
/*      */       }
/* 3477 */       updateQuery = updateQuery + toFields[i] + " = " + fromFields[i];
/*      */     }
/* 3479 */     return updateQuery;
/*      */   }
/*      */ 
/*      */   public void createTable(String tableName, String[][] columns, String[] primaryKey) throws DataException
/*      */   {
/* 3484 */     createTableEx(this.m_workspace, tableName, columns, primaryKey);
/*      */   }
/*      */ 
/*      */   public void createTableEx(Workspace ws, String tableName, String[][] columns, String[] primaryKey) throws DataException
/*      */   {
/* 3489 */     WorkspaceUtils.createTable(ws, tableName, columns, primaryKey);
/*      */   }
/*      */ 
/*      */   public void reportProgress(String msg, int amt)
/*      */   {
/* 3496 */     if ((IdcSystemLoader.m_progress != null) && (msg != null))
/*      */     {
/* 3498 */       if ((!this.m_dontResetSubProgress) && (!this.m_hasReportedProgress))
/*      */       {
/* 3500 */         this.m_hasReportedProgress = true;
/* 3501 */         this.m_progress.m_maxProgress -= this.m_progress.m_curProgress;
/* 3502 */         this.m_progress.m_curProgress = 0;
/*      */       }
/*      */ 
/* 3505 */       IdcSystemLoader.m_progress.reportProgress(0, msg, this.m_progress.m_curProgress, this.m_progress.m_maxProgress);
/*      */     }
/*      */ 
/* 3508 */     this.m_progress.m_curProgress += amt;
/*      */   }
/*      */ 
/*      */   protected void initExecutionContext()
/*      */   {
/* 3521 */     setCachedObject("UserLocale", LocaleResources.getLocale("SystemLocale"));
/*      */   }
/*      */ 
/*      */   public Object getControllingObject()
/*      */   {
/* 3526 */     return this;
/*      */   }
/*      */ 
/*      */   public Object getCachedObject(String id)
/*      */   {
/* 3531 */     if ((this.m_timeZone != null) && (id.equals("UserTimeZone")))
/*      */     {
/* 3533 */       return this.m_timeZone;
/*      */     }
/*      */ 
/* 3536 */     return this.m_cachedData.get(id);
/*      */   }
/*      */ 
/*      */   public void setCachedObject(String id, Object obj)
/*      */   {
/* 3541 */     if ((obj instanceof IdcLocale) && (id.equals("UserLocale")))
/*      */     {
/* 3543 */       this.m_locale = ((IdcLocale)obj);
/* 3544 */       this.m_languageId = this.m_locale.m_languageId;
/* 3545 */       this.m_pageEncoding = this.m_locale.m_pageEncoding;
/* 3546 */       this.m_dateFormat = this.m_locale.m_dateFormat.shallowClone();
/* 3547 */       this.m_timeZone = this.m_dateFormat.getTimeZone();
/*      */     }
/*      */ 
/* 3550 */     if ((obj instanceof IdcDateFormat) && (id.equals("UserDateFormat")))
/*      */     {
/* 3552 */       this.m_dateFormat = ((IdcDateFormat)obj);
/*      */     }
/*      */ 
/* 3555 */     if ((obj instanceof TimeZone) && (id.equals("UserTimeZone")))
/*      */     {
/* 3557 */       this.m_timeZone = ((TimeZone)obj);
/* 3558 */       if (this.m_dateFormat != null)
/*      */       {
/* 3560 */         this.m_dateFormat.setTZ(this.m_timeZone);
/*      */       }
/*      */     }
/*      */ 
/* 3564 */     if (obj == null)
/*      */     {
/* 3569 */       this.m_cachedData.remove(id);
/*      */     }
/*      */     else
/*      */     {
/* 3573 */       this.m_cachedData.put(id, obj);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Object getReturnValue()
/*      */   {
/* 3579 */     return this.m_taskRetVal;
/*      */   }
/*      */ 
/*      */   public void setReturnValue(Object str) {
/* 3583 */     this.m_taskRetVal = str;
/*      */   }
/*      */ 
/*      */   public Object getLocaleResource(int id)
/*      */   {
/* 3588 */     Object obj = null;
/*      */ 
/* 3590 */     switch (id)
/*      */     {
/*      */     case 0:
/* 3593 */       obj = this.m_locale;
/* 3594 */       break;
/*      */     case 1:
/* 3597 */       obj = this.m_languageId;
/* 3598 */       break;
/*      */     case 2:
/* 3601 */       obj = this.m_pageEncoding;
/* 3602 */       break;
/*      */     case 3:
/* 3605 */       obj = this.m_dateFormat;
/* 3606 */       break;
/*      */     case 4:
/* 3609 */       obj = this.m_timeZone;
/*      */     }
/*      */ 
/* 3613 */     return obj;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 3620 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102962 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IdcExtendedLoader
 * JD-Core Version:    0.5.4
 */