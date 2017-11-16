/*     */ package intradoc.server.project;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Projects
/*     */ {
/*  36 */   public static Hashtable m_projects = new Hashtable();
/*     */ 
/*  39 */   public static Hashtable m_cachedWorkMap = new Hashtable();
/*     */ 
/*     */   public static void init()
/*     */   {
/*  43 */     ProjectFileUtils.init();
/*  44 */     ProblemReportUtils.init();
/*     */   }
/*     */ 
/*     */   public static void loadProjects(Workspace ws) throws DataException, ServiceException
/*     */   {
/*  49 */     ResultSet rset = ws.createResultSet("QregisteredProjects", null);
/*     */ 
/*  51 */     DataResultSet projectSet = new DataResultSet();
/*  52 */     projectSet.copy(rset);
/*     */ 
/*  54 */     boolean isPreviewRegistered = false;
/*  55 */     for (; projectSet.isRowPresent(); projectSet.next())
/*     */     {
/*  57 */       Properties props = projectSet.getCurrentRowProps();
/*  58 */       ProjectInfo info = getProjectInfo(props.getProperty("dProjectID"));
/*  59 */       if (info == null)
/*     */       {
/*  61 */         info = new ProjectInfo();
/*  62 */         info.init(props);
/*     */       }
/*     */       else
/*     */       {
/*  66 */         info.m_properties = props;
/*     */       }
/*     */ 
/*  69 */       updateProjectInfo(info);
/*  70 */       addProjectInfo(info);
/*     */ 
/*  72 */       if (isPreviewRegistered)
/*     */         continue;
/*  74 */       isPreviewRegistered = info.m_hasPreview;
/*     */     }
/*     */ 
/*  78 */     SharedObjects.putTable("RegisteredProjects", projectSet);
/*  79 */     SharedObjects.putEnvironmentValue("IsPreviewRegistered", String.valueOf(isPreviewRegistered));
/*     */   }
/*     */ 
/*     */   public static void addProjectInfo(ProjectInfo info)
/*     */   {
/*  84 */     String id = info.m_projectID.toLowerCase();
/*  85 */     m_projects.put(id, info);
/*     */   }
/*     */ 
/*     */   public static void updateProjectInfo(ProjectInfo info) throws ServiceException
/*     */   {
/*  90 */     info.updateFunctionFlags();
/*  91 */     ProjectFileUtils.updateProjectInfo(info, false);
/*     */   }
/*     */ 
/*     */   public static void updateProjectXml(ProjectInfo info, List functions, List xmlNodes) throws ServiceException
/*     */   {
/*  96 */     info.updateFunctionFlags();
/*  97 */     ProjectFileUtils.updateProjectXml(info, functions, xmlNodes);
/*     */   }
/*     */ 
/*     */   public static void deleteProject(String projectID)
/*     */   {
/* 102 */     projectID = projectID.toLowerCase();
/*     */     try
/*     */     {
/* 105 */       m_projects.remove(projectID);
/* 106 */       m_cachedWorkMap.remove(projectID);
/* 107 */       ProjectFileUtils.deleteProjectDirectory(projectID);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 111 */       String msg = LocaleUtils.encodeMessage("csProjectFileDeleteError", t.getMessage(), projectID);
/*     */ 
/* 113 */       Report.trace(null, LocaleResources.localizeMessage(msg, null), t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static DataResultSet buildProjectSetOfType(String type)
/*     */     throws DataException
/*     */   {
/* 120 */     DataResultSet projectSet = SharedObjects.getTable("RegisteredProjects");
/* 121 */     int index = ResultSetUtils.getIndexMustExist(projectSet, "dProjectID");
/*     */ 
/* 123 */     DataResultSet drset = new DataResultSet();
/* 124 */     drset.copyFieldInfo(projectSet);
/*     */ 
/* 126 */     for (; projectSet.isRowPresent(); projectSet.next())
/*     */     {
/* 128 */       String projectID = projectSet.getStringValue(index);
/* 129 */       ProjectInfo info = getProjectInfo(projectID);
/* 130 */       if (info == null) continue; if (!info.m_hasWorkflow) {
/*     */         continue;
/*     */       }
/*     */ 
/* 134 */       Vector row = projectSet.getCurrentRowValues();
/* 135 */       drset.addRow(row);
/*     */     }
/* 137 */     return drset;
/*     */   }
/*     */ 
/*     */   public static ProjectInfo getProjectInfo(String projectID)
/*     */   {
/* 142 */     projectID = projectID.toLowerCase();
/* 143 */     return (ProjectInfo)m_projects.get(projectID);
/*     */   }
/*     */ 
/*     */   public static Hashtable getProjects()
/*     */   {
/* 148 */     return m_projects;
/*     */   }
/*     */ 
/*     */   public static Hashtable createWorkMap(String projectID)
/*     */   {
/* 156 */     projectID = projectID.toLowerCase();
/*     */ 
/* 158 */     Hashtable work = new Hashtable();
/* 159 */     m_cachedWorkMap.put(projectID, work);
/*     */ 
/* 161 */     return work;
/*     */   }
/*     */ 
/*     */   public static void addWork(String projectID, String key, Properties props)
/*     */   {
/* 166 */     Hashtable work = getWorkMap(projectID);
/* 167 */     if (work == null)
/*     */     {
/* 169 */       work = createWorkMap(projectID);
/*     */     }
/*     */ 
/* 172 */     Vector workItem = (Vector)work.get(key);
/* 173 */     if (workItem == null)
/*     */     {
/* 175 */       workItem = new IdcVector();
/* 176 */       work.put(key, workItem);
/*     */     }
/*     */ 
/* 179 */     workItem.addElement(props);
/*     */   }
/*     */ 
/*     */   public static Hashtable getWorkMap(String projectID)
/*     */   {
/* 184 */     projectID = projectID.toLowerCase();
/* 185 */     return (Hashtable)m_cachedWorkMap.get(projectID);
/*     */   }
/*     */ 
/*     */   public static Hashtable removeWorkMap(String projectID)
/*     */   {
/* 190 */     projectID = projectID.toLowerCase();
/* 191 */     return (Hashtable)m_cachedWorkMap.remove(projectID);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 196 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.project.Projects
 * JD-Core Version:    0.5.4
 */