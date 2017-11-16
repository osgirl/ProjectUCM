/*     */ package intradoc.apps.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.shared.SecurityAccessListUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Applications
/*     */ {
/*     */   protected Hashtable m_appInfoMap;
/*     */   protected Vector m_appInfo;
/*     */   protected ExecutionContext m_cxt;
/*     */   public String[][] APP_INFO;
/*     */ 
/*     */   public Applications()
/*     */   {
/*  32 */     this.m_appInfoMap = new Hashtable();
/*  33 */     this.m_appInfo = new IdcVector();
/*  34 */     this.m_cxt = null;
/*     */ 
/*  38 */     this.APP_INFO = new String[][] { { "WebLayout", "intradoc.apps.pagebuilder.PageBuilderFrame", "apTitleWebLayout", "users,accounts,doctypes,docformats,metadata,metaoptlists,usermetaoptlists,pagelist,reports,templates,renditions,collaborations,searchapi,schema,dynamicqueries", "layout.gif", "0" }, { "UserAdmin", "intradoc.apps.useradmin.UserAdminFrame", "apTitleUserAdmin", "users,userlist,aliases,accounts,metadata,usermetaoptlists,collaborations,schema", "user.gif", "0" }, { "RepoMan", "intradoc.apps.docman.DocManFrame", "apTitleRepoMan", "documents,users,accounts,aliases,doctypes,metadata,metaoptlists,usermetaoptlists,docformats,dynamicqueries,renditions,subscriptiontypes,indexerstatus,indexerwork,collaborations,searchapi,schema", "docman.gif", "indexer" }, { "Workflow", "intradoc.apps.workflow.WorkflowFrame", "apTitleWorkflow", "workflows,wftemplates,aliases,users,accounts,metadata,metaoptlists,usermetaoptlists,doctypes,projects,wfscripts,collaborations,schema", "workflow.gif", "0" }, { "ConfigMan", "intradoc.apps.docconfig.DocConfigFrame", "apTitleConfigMan", "users,doctypes,docformats,metadata,metaoptlists,templates,dynamicqueries,schema,docprofiles,accounts,docclasses", "config2.gif", "0" }, { "Archiver", "intradoc.apps.archiver.ArchiverFrame", "apTitleArchiver", "collections,metadata,metaoptlists,users,accounts,doctypes,renditions,collaborations,schema", "archives.gif", "archiver" } };
/*     */   }
/*     */ 
/*     */   protected void init(boolean isStandAlone, Hashtable subjectMap, ExecutionContext cxt)
/*     */   {
/*  95 */     this.m_cxt = cxt;
/*  96 */     loadInfo(this.APP_INFO, subjectMap);
/*     */ 
/*  99 */     IdcComparator cmp = new IdcComparator()
/*     */     {
/*     */       public int compare(Object obj1, Object obj2)
/*     */       {
/* 103 */         AppInfo info1 = (AppInfo)obj1;
/* 104 */         AppInfo info2 = (AppInfo)obj2;
/*     */ 
/* 106 */         String name1 = info1.m_appName;
/* 107 */         String name2 = info2.m_appName;
/*     */ 
/* 109 */         return name1.compareTo(name2);
/*     */       }
/*     */     };
/* 112 */     Sort.sortVector(this.m_appInfo, cmp);
/*     */   }
/*     */ 
/*     */   protected void loadInfo(String[][] appInfos, Hashtable subjectMap)
/*     */   {
/* 117 */     for (int i = 0; i < appInfos.length; ++i)
/*     */     {
/* 119 */       appInfos[i][2] = LocaleResources.getString(appInfos[i][2], this.m_cxt);
/* 120 */       AppInfo info = new AppInfo(appInfos[i]);
/* 121 */       info.m_appRights = SecurityAccessListUtils.getRightsForApp(info.m_appName);
/*     */ 
/* 123 */       this.m_appInfoMap.put(info.m_appName, info);
/* 124 */       addSubjects(info.m_subjects, subjectMap);
/* 125 */       this.m_appInfo.addElement(info);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addSubjects(String[] subjects, Hashtable subjectMap)
/*     */   {
/* 131 */     int size = subjects.length;
/* 132 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 134 */       String name = subjects[i];
/* 135 */       if (subjectMap.get(name) != null)
/*     */         continue;
/* 137 */       SubjectInfo info = new SubjectInfo(name);
/* 138 */       subjectMap.put(name, info);
/*     */     }
/*     */   }
/*     */ 
/*     */   public AppInfo getAppInfo(String name)
/*     */   {
/* 148 */     return (AppInfo)this.m_appInfoMap.get(name);
/*     */   }
/*     */ 
/*     */   public Vector getApps()
/*     */   {
/* 153 */     return this.m_appInfo;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 158 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97978 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.Applications
 * JD-Core Version:    0.5.4
 */