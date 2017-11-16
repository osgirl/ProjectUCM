/*     */ package intradoc.server.subject;
/*     */ 
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceManager;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import intradoc.server.filter.WebFilterConfigUtils;
/*     */ import intradoc.server.proxy.OutgoingProviderMonitor;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.RoleDefinitions;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserDocumentAccessFilter;
/*     */ import intradoc.shared.Users;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UsersSubjectCallback extends SubjectCallbackAdapter
/*     */ {
/*     */   Workspace m_userWorkspace;
/*     */ 
/*     */   public void setWorkspace(Workspace ws)
/*     */   {
/*  56 */     this.m_workspace = ws;
/*  57 */     this.m_userWorkspace = WorkspaceUtils.getWorkspace("user");
/*     */   }
/*     */ 
/*     */   public void refresh(String subject)
/*     */     throws DataException, ServiceException
/*     */   {
/*  63 */     if (this.m_workspace != null)
/*     */     {
/*  65 */       cacheUsers();
/*  66 */       PluginFilters.filter("cacheTenants", this.m_workspace, null, new ExecutionContextAdaptor());
/*     */     }
/*     */ 
/*  74 */     if ((LegacyDirectoryLocator.hasSeparateUserPublishDir()) && (!OutgoingProviderMonitor.isOnDemand()) && (OutgoingProviderMonitor.isStarted()))
/*     */     {
/*  78 */       DataBinder filterBinder = WebFilterConfigUtils.readFilterConfigFromFile();
/*  79 */       WebFilterConfigUtils.updateWebFilterConfig(filterBinder);
/*     */ 
/*  81 */       ServiceManager mgr = new ServiceManager();
/*  82 */       DataBinder binder = new DataBinder();
/*  83 */       binder.putLocal("IdcService", "UPDATE_FILTER_INFO");
/*  84 */       mgr.init(binder, this.m_workspace);
/*     */ 
/*  86 */       Service service = ServiceManager.getInitializedService("UPDATE_FILTER_INFO", binder, this.m_workspace);
/*     */ 
/*  88 */       service.executeActions();
/*     */     }
/*     */ 
/*  93 */     SchemaHelper schHelper = new SchemaHelper();
/*  94 */     schHelper.markViewCacheDirty("docAuthors", "SecurityGroups");
/*     */   }
/*     */ 
/*     */   public void cacheUsers() throws DataException {
/*  98 */     Users users = new Users();
/*     */     try
/*     */     {
/* 101 */       ResultSet rset = this.m_userWorkspace.createResultSet("QlocalUsers", null);
/* 102 */       if ((((rset == null) || (!rset.isRowPresent()))) && (!SharedObjects.getEnvValueAsBoolean("AllowEmptyUsersList", true)))
/*     */       {
/* 105 */         throw new DataException(null, "csUnableToFindUserList", new Object[0]);
/*     */       }
/* 107 */       users.load(rset);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 111 */       throw new DataException(e, "csUnableToLoadTable", new Object[] { "Users" });
/*     */     }
/* 113 */     SharedObjects.putTable("Users", users);
/*     */ 
/* 116 */     RoleDefinitions roles = new RoleDefinitions();
/*     */     try
/*     */     {
/* 119 */       ResultSet rset = this.m_userWorkspace.createResultSet(roles.getTableName(), null);
/* 120 */       if ((rset == null) || ((!rset.isRowPresent()) && (!SharedObjects.getEnvValueAsBoolean("AllowEmptyUsersList", true))))
/*     */       {
/* 123 */         throw new DataException(null, "csUnableToFindRoleDefinitions", new Object[0]);
/*     */       }
/* 125 */       roles.load(rset);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 129 */       throw new DataException(e, "csUnableToLoadTable", new Object[] { "RoleDefinitions" });
/*     */     }
/* 131 */     SharedObjects.putTable(roles.getTableName(), roles);
/*     */ 
/* 133 */     DataLoader.cacheSecurityGroupLists(this.m_userWorkspace);
/*     */   }
/*     */ 
/*     */   public DataResultSet filterResultSet(String subject, DataBinder binder, Service srv, UserData userData, UserDocumentAccessFilter readFilter, DataResultSet drset, String tableName)
/*     */     throws DataException
/*     */   {
/* 142 */     if ((tableName.equals("RoleDefinition")) || (tableName.equals("SecurityGroups")))
/*     */     {
/* 144 */       String key = "dGroupName";
/* 145 */       DataResultSet retVal = null;
/* 146 */       if ((readFilter.m_groups == null) || (readFilter.m_isAdmin))
/*     */       {
/* 148 */         retVal = drset;
/*     */       }
/*     */       else
/*     */       {
/* 152 */         retVal = copyFilteredByAllowedList(readFilter.m_groups, drset, key);
/*     */       }
/*     */ 
/* 156 */       return retVal;
/*     */     }
/* 158 */     return drset;
/*     */   }
/*     */ 
/*     */   public DataResultSet copyFilteredByAllowedList(String[] list, DataResultSet drset, String key)
/*     */   {
/* 164 */     String[] filterList = list;
/*     */ 
/* 166 */     ResultSetFilter filter = new ResultSetFilter(filterList)
/*     */     {
/*     */       public int checkRow(String val, int curNumRows, Vector row)
/*     */       {
/* 173 */         if ((val.length() > 0) && (val.charAt(0) == '#'))
/*     */         {
/* 175 */           return 1;
/*     */         }
/* 177 */         val = val.toLowerCase();
/* 178 */         if (StringUtils.findStringIndex(this.val$filterList, val) < 0)
/*     */         {
/* 180 */           return 0;
/*     */         }
/* 182 */         return 1;
/*     */       }
/*     */     };
/* 186 */     DataResultSet retVal = new DataResultSet();
/* 187 */     retVal.copyFiltered(drset, key, filter);
/* 188 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 193 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98148 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.UsersSubjectCallback
 * JD-Core Version:    0.5.4
 */