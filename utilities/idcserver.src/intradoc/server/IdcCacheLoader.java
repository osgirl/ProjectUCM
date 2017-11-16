/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceCacheState;
/*     */ import intradoc.server.subject.AccountsSubjectCallback;
/*     */ import intradoc.server.subject.AliasesSubjectCallback;
/*     */ import intradoc.server.subject.ConfigSubjectCallback;
/*     */ import intradoc.server.subject.DocClassSubjectCallback;
/*     */ import intradoc.server.subject.DocFormatsSubjectCallback;
/*     */ import intradoc.server.subject.DocProfilesSubjectCallback;
/*     */ import intradoc.server.subject.DocTypesSubjectCallback;
/*     */ import intradoc.server.subject.DocumentsSubjectCallback;
/*     */ import intradoc.server.subject.MetaDataSubjectCallback;
/*     */ import intradoc.server.subject.MetaOptListsSubjectCallback;
/*     */ import intradoc.server.subject.RenditionsSubjectCallback;
/*     */ import intradoc.server.subject.SchemaSubjectCallback;
/*     */ import intradoc.server.subject.SubscriptionTypesSubjectCallback;
/*     */ import intradoc.server.subject.UserListSubjectCallback;
/*     */ import intradoc.server.subject.UserMetaOptListsSubjectCallback;
/*     */ import intradoc.server.subject.UserTempCacheSubjectCallback;
/*     */ import intradoc.server.subject.UsersSubjectCallback;
/*     */ import intradoc.server.subject.WorkflowsMiscSubjectCallback;
/*     */ import intradoc.server.subject.WorkflowsSubjectCallback;
/*     */ import intradoc.shared.SharedObjects;
/*     */ 
/*     */ public class IdcCacheLoader
/*     */ {
/*     */   public static void loadMonitoredTables(Workspace workspace)
/*     */     throws DataException, ServiceException
/*     */   {
/*  49 */     SubjectCallbackAdapter usersCallback = new UsersSubjectCallback();
/*  50 */     usersCallback.setWorkspace(workspace);
/*  51 */     usersCallback.setLists(new String[] { "Users", "RoleDefinition", "SecurityGroups" }, null);
/*  52 */     SubjectManager.registerCallback("users", usersCallback);
/*  53 */     addUserFilteredSubject("users");
/*     */ 
/*  56 */     SubjectCallbackAdapter accountsCallback = new AccountsSubjectCallback();
/*  57 */     accountsCallback.setWorkspace(workspace);
/*  58 */     accountsCallback.setLists(new String[] { "DocumentAccounts" }, null);
/*  59 */     SubjectManager.registerCallback("accounts", accountsCallback);
/*  60 */     addUserFilteredSubject("accounts");
/*     */ 
/*  63 */     SubjectCallbackAdapter typesCallback = new DocTypesSubjectCallback();
/*  64 */     typesCallback.setWorkspace(workspace);
/*  65 */     typesCallback.setLists(new String[] { "DocTypes" }, null);
/*  66 */     SubjectManager.registerCallback("doctypes", typesCallback);
/*     */ 
/*  69 */     SubjectCallbackAdapter formatsCallback = new DocFormatsSubjectCallback();
/*  70 */     formatsCallback.setWorkspace(workspace);
/*  71 */     formatsCallback.setLists(new String[] { "DocFormats", "ExtensionFormatMap", "DocumentConversions" }, null);
/*     */ 
/*  73 */     SubjectManager.registerCallback("docformats", formatsCallback);
/*     */ 
/*  76 */     SubjectCallbackAdapter metadataCallback = new MetaDataSubjectCallback();
/*  77 */     metadataCallback.setWorkspace(workspace);
/*  78 */     metadataCallback.setLists(new String[] { "DocMetaDefinition", "UserMetaDefinition" }, null);
/*  79 */     SubjectManager.registerCallback("metadata", metadataCallback);
/*     */ 
/*  82 */     SubjectCallbackAdapter docClassCallback = new DocClassSubjectCallback();
/*  83 */     docClassCallback.setWorkspace(workspace);
/*  84 */     docClassCallback.setLists(new String[] { "DocClassDefinition, DocClasses" }, null);
/*  85 */     SubjectManager.registerCallback("docclasses", docClassCallback);
/*     */ 
/*  88 */     SubjectCallbackAdapter docmetaoptCallback = new MetaOptListsSubjectCallback();
/*  89 */     docmetaoptCallback.setWorkspace(workspace);
/*  90 */     SubjectManager.registerCallback("metaoptlists", docmetaoptCallback);
/*     */ 
/*  93 */     boolean traceStoragePerfomance = SharedObjects.getEnvValueAsBoolean("UserStorageTracePerformance", false);
/*  94 */     boolean traceStorageAttributes = SharedObjects.getEnvValueAsBoolean("UserStorageTraceAttributes", false);
/*  95 */     if ((traceStoragePerfomance) || (traceStorageAttributes))
/*     */     {
/*  97 */       SystemUtils.addAsDefaultTrace("userstorage");
/*  98 */       if (traceStorageAttributes)
/*     */       {
/* 100 */         SystemUtils.m_verbose = true;
/* 101 */         SystemUtils.reportDeprecatedUsage("UserStorageTraceAttributes is deprecated.  Activate the userstorage trace and verbose tracing.");
/*     */       }
/*     */       else
/*     */       {
/* 107 */         SystemUtils.reportDeprecatedUsage("UserStorageTracePerformance is deprecated.  Activate the userstorage trace.");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 114 */     SubjectCallbackAdapter usermetaoptCallback = new UserMetaOptListsSubjectCallback();
/* 115 */     SubjectManager.registerCallback("usermetaoptlists", usermetaoptCallback);
/*     */ 
/* 117 */     SubjectCallbackAdapter documentsCallback = new DocumentsSubjectCallback();
/* 118 */     documentsCallback.setWorkspace(workspace);
/* 119 */     SubjectManager.registerCallback("documents", documentsCallback);
/*     */ 
/* 121 */     SubjectCallbackAdapter userlistCallback = new UserListSubjectCallback();
/* 122 */     userlistCallback.setWorkspace(workspace);
/* 123 */     SubjectManager.registerCallback("userlist", userlistCallback);
/*     */ 
/* 126 */     SubjectCallbackAdapter wfCallback = new WorkflowsSubjectCallback();
/* 127 */     wfCallback.setWorkspace(workspace);
/* 128 */     SubjectManager.registerCallback("workflows", wfCallback);
/*     */ 
/* 130 */     SubjectCallbackAdapter wfTemplatesCallback = new WorkflowsMiscSubjectCallback();
/* 131 */     wfTemplatesCallback.setLists(new String[] { "WfTemplates" }, null);
/*     */ 
/* 133 */     SubjectManager.registerCallback("wftemplates", wfTemplatesCallback);
/*     */ 
/* 135 */     SubjectCallbackAdapter wfScriptsCallback = new WorkflowsMiscSubjectCallback();
/* 136 */     wfScriptsCallback.setLists(new String[] { "WorkflowScripts", "WorkflowTokens" }, null);
/*     */ 
/* 138 */     SubjectManager.registerCallback("wfscripts", wfScriptsCallback);
/*     */ 
/* 141 */     SubjectCallbackAdapter alCallback = new AliasesSubjectCallback();
/* 142 */     alCallback.setWorkspace(workspace);
/* 143 */     alCallback.setLists(new String[] { "Alias", "AliasUserMap" }, null);
/* 144 */     SubjectManager.registerCallback("aliases", alCallback);
/*     */ 
/* 147 */     computeSecurityConfiguration();
/*     */ 
/* 150 */     SubjectCallbackAdapter renCallback = new RenditionsSubjectCallback();
/* 151 */     renCallback.setLists(new String[] { "AdditionalRenditions" }, null);
/* 152 */     SubjectManager.registerCallback("renditions", renCallback);
/*     */ 
/* 155 */     SubjectCallbackAdapter scpCallback = new SubscriptionTypesSubjectCallback();
/* 156 */     scpCallback.setLists(new String[] { "SubscriptionTypes" }, null);
/* 157 */     SubjectManager.registerCallback("subscriptiontypes", scpCallback);
/*     */ 
/* 160 */     loadDocumentProfiles();
/*     */ 
/* 163 */     SubjectCallbackAdapter conCallback = new ConfigSubjectCallback();
/* 164 */     SubjectManager.registerCallback("config", conCallback);
/*     */ 
/* 167 */     SubjectEventMonitor tmpCacheMonitor = new SubjectEventMonitor()
/*     */     {
/*     */       public boolean checkForChange(String subject, long curTime)
/*     */       {
/* 171 */         ResourceCacheState.checkTemporaryCache(curTime);
/* 172 */         return false;
/*     */       }
/*     */ 
/*     */       public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*     */       {
/*     */       }
/*     */     };
/* 181 */     SubjectManager.addSubjectMonitor("tmpcache", tmpCacheMonitor);
/*     */ 
/* 184 */     if (SharedObjects.getEnvValueAsBoolean("UserCacheClusterSupport", false))
/*     */     {
/* 186 */       SubjectCallbackAdapter userTempCachecallback = new UserTempCacheSubjectCallback();
/* 187 */       userTempCachecallback.setWorkspace(workspace);
/* 188 */       SubjectManager.registerCallback("usertempcache", userTempCachecallback);
/*     */     }
/*     */ 
/* 192 */     SubjectCallbackAdapter schemaCallback = new SchemaSubjectCallback();
/* 193 */     schemaCallback.setWorkspace(workspace);
/* 194 */     SubjectManager.registerCallback("schema", schemaCallback);
/*     */   }
/*     */ 
/*     */   public static void computeSecurityConfiguration()
/*     */   {
/* 200 */     boolean hasGlobalUsers = false;
/* 201 */     boolean isProxiedServer = SharedObjects.getEnvValueAsBoolean("IsProxiedServer", false);
/*     */ 
/* 203 */     String hasGlobalUsersStr = SharedObjects.getEnvironmentValue("HasGlobalUsers");
/* 204 */     if (hasGlobalUsersStr == null)
/*     */     {
/* 206 */       boolean isWorkgroup = false;
/* 207 */       if ((!isWorkgroup) && 
/* 209 */         (!isProxiedServer))
/*     */       {
/* 211 */         hasGlobalUsers = true;
/*     */       }
/*     */ 
/* 214 */       if (!hasGlobalUsers)
/*     */       {
/* 216 */         SharedObjects.putEnvironmentValue("HasGlobalUsers", "0");
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 221 */       hasGlobalUsers = StringUtils.convertToBool("HasGlobalUsers", false);
/*     */     }
/*     */ 
/* 225 */     String isExternalStr = SharedObjects.getEnvironmentValue("HasExternalUsers");
/* 226 */     if (isExternalStr != null)
/*     */       return;
/* 228 */     boolean hasWebAuthUsers = (SharedObjects.getEnvValueAsBoolean("NtlmSecurityEnabled", false)) || (SharedObjects.getEnvValueAsBoolean("WebServerAuthOnly", false));
/*     */ 
/* 230 */     boolean isExternal = (hasGlobalUsers) || (isProxiedServer) || (hasWebAuthUsers);
/* 231 */     if (!isExternal)
/*     */       return;
/* 233 */     SharedObjects.putEnvironmentValue("HasExternalUsers", "1");
/*     */   }
/*     */ 
/*     */   public static void addUserFilteredSubject(String subject)
/*     */   {
/* 241 */     String curList = SharedObjects.getEnvironmentValue("UserFilteredSubjects");
/* 242 */     String update = subject;
/* 243 */     if (curList != null)
/*     */     {
/* 245 */       update = curList + "," + subject;
/*     */     }
/* 247 */     SharedObjects.putEnvironmentValue("UserFilteredSubjects", update);
/*     */   }
/*     */ 
/*     */   public static void loadDocumentProfiles() throws DataException, ServiceException
/*     */   {
/* 252 */     DocProfileManager.init();
/*     */ 
/* 254 */     SubjectCallbackAdapter profileCallback = new DocProfilesSubjectCallback();
/* 255 */     profileCallback.setLists(new String[] { "DocumentProfiles", "DocumentRules" }, null);
/*     */ 
/* 257 */     SubjectManager.registerCallback("docprofiles", profileCallback);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 262 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104215 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IdcCacheLoader
 * JD-Core Version:    0.5.4
 */