/*     */ package intradoc.autosuggest;
/*     */ 
/*     */ import intradoc.autosuggest.datastore.ContextInfoStorage;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.QueryUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import java.util.Iterator;
/*     */ import java.util.Timer;
/*     */ import java.util.TimerTask;
/*     */ 
/*     */ public class AutoSuggestInitializer
/*     */ {
/*     */   public Timer m_timer;
/*     */   public Workspace m_workspace;
/*     */ 
/*     */   public AutoSuggestInitializer(Workspace ws)
/*     */     throws DataException, ServiceException
/*     */   {
/*  50 */     this.m_workspace = ws;
/*     */   }
/*     */ 
/*     */   public void init() throws ServiceException, DataException {
/*  54 */     FileUtils.checkOrCreateDirectory(AutoSuggestConstants.AUTO_SUGGEST_DIR, 4);
/*  55 */     FileUtils.checkOrCreateDirectory(AutoSuggestConstants.AUTO_SUGGEST_LOCK_DIR, 4);
/*  56 */     ContextInfoStorage.init();
/*  57 */     initContextLockDirs();
/*  58 */     initWorkspace();
/*  59 */     schedule();
/*     */   }
/*     */ 
/*     */   public void initContextLockDirs()
/*     */     throws ServiceException
/*     */   {
/*  67 */     Iterator contextIterator = ContextInfoStorage.getContextsIterator();
/*  68 */     while (contextIterator.hasNext())
/*     */     {
/*  70 */       String contextKey = (String)contextIterator.next();
/*  71 */       FileUtils.checkOrCreateDirectory(AutoSuggestConstants.AUTO_SUGGEST_LOCK_DIR + "/" + contextKey, 4);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void initEnvironment(Workspace workspace)
/*     */     throws DataException
/*     */   {
/*  81 */     boolean useEntitySecurity = SharedObjects.getEnvValueAsBoolean("UseEntitySecurity", false);
/*  82 */     FieldInfo roleListInfo = WorkspaceUtils.getColumnInfo("DocMeta", "xClbraRoleList", workspace);
/*  83 */     if ((useEntitySecurity) && (roleListInfo != null))
/*     */     {
/*  85 */       SharedObjects.putEnvironmentValue("UseRoleSecurity", "1");
/*     */     }
/*     */     else
/*     */     {
/*  89 */       SharedObjects.putEnvironmentValue("UseRoleSecurity", "0");
/*     */     }
/*     */ 
/*  92 */     SharedObjects.putEnvironmentValue("DefaultCacheSubjectNotifications", "1");
/*  93 */     AutoSuggestContext.initEnvironment();
/*     */   }
/*     */ 
/*     */   public void initWorkspace()
/*     */     throws ServiceException
/*     */   {
/* 101 */     DataResultSet columnMap = new DataResultSet(new String[] { "column", "alias" });
/* 102 */     QueryUtils.addColumnMapRow(columnMap, AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER);
/* 103 */     QueryUtils.addColumnMapRow(columnMap, AutoSuggestConstants.FIELD_AUTOSUGGEST_SECURITYGROUP_ID);
/* 104 */     QueryUtils.addColumnMapRow(columnMap, AutoSuggestConstants.FIELD_AUTOSUGGEST_OWNER);
/* 105 */     QueryUtils.addColumnMapRow(columnMap, AutoSuggestConstants.FIELD_AUTOSUGGEST_ACCOUNT_ID);
/* 106 */     QueryUtils.addColumnMapRow(columnMap, AutoSuggestConstants.FIELD_AUTOSUGGEST_USERS);
/* 107 */     QueryUtils.addColumnMapRow(columnMap, AutoSuggestConstants.FIELD_AUTOSUGGEST_GROUPS);
/* 108 */     QueryUtils.addColumnMapRow(columnMap, AutoSuggestConstants.FIELD_AUTOSUGGEST_ROLES);
/* 109 */     QueryUtils.addColumnMapRow(columnMap, AutoSuggestConstants.FIELD_AUTOSUGGEST_GUID);
/* 110 */     QueryUtils.addColumnMapRow(columnMap, AutoSuggestConstants.FIELD_AUTOSUGGEST_PARTITION);
/* 111 */     this.m_workspace.loadColumnMap(columnMap);
/*     */   }
/*     */ 
/*     */   public void schedule()
/*     */   {
/* 118 */     int autoSuggestIndexInterval = SharedObjects.getEnvironmentInt("AutoSuggestIndexInterval", 60);
/* 119 */     this.m_timer = new Timer(AutoSuggestConstants.AUTO_SUGGEST_INDEXER_THREAD_NAME, true);
/* 120 */     this.m_timer.schedule(new AutoSuggestIndexingTask(), 0L, autoSuggestIndexInterval * 1000);
/* 121 */     Report.trace("autosuggest", "Scheduled a AutoSuggest indexing task.", null);
/*     */   }
/*     */ 
/*     */   public static Service createDummyService(DataBinder binder, Workspace ws)
/*     */     throws DataException, ServiceException
/*     */   {
/* 151 */     Service s = new Service();
/* 152 */     s.init(ws, null, binder, new ServiceData());
/* 153 */     s.initDelegatedObjects();
/* 154 */     UserData userData = SecurityUtils.createDefaultAdminUserData();
/* 155 */     s.setUserData(userData);
/* 156 */     return s;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 160 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102609 $";
/*     */   }
/*     */ 
/*     */   class AutoSuggestIndexingTask extends TimerTask
/*     */   {
/*     */     AutoSuggestIndexingTask()
/*     */     {
/*     */     }
/*     */ 
/*     */     public void run()
/*     */     {
/*     */       try
/*     */       {
/* 137 */         Report.trace("autosuggest", "Invoking the AutoSuggest indexing update.", null);
/* 138 */         DataBinder buildBinder = new DataBinder();
/* 139 */         buildBinder.putLocal("isRebuild", "0");
/* 140 */         Service service = AutoSuggestInitializer.createDummyService(buildBinder, AutoSuggestInitializer.this.m_workspace);
/* 141 */         service.executeServiceSimple("BUILD_SUGGESTIONS_INDEX");
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 145 */         Report.error(null, "BUILD_SUGGESTIONS_INDEX failed", e);
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.AutoSuggestInitializer
 * JD-Core Version:    0.5.4
 */