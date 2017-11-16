/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.project.ProjectInfo;
/*     */ import intradoc.server.project.ProjectUtils;
/*     */ import intradoc.server.project.Projects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Properties;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ProjectDocHandler extends ServiceHandler
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void updatePublishInfo()
/*     */     throws DataException, ServiceException
/*     */   {
/*  40 */     String action = this.m_binder.getLocal("Action");
/*  41 */     if ((action == null) || (action.equalsIgnoreCase("delete")))
/*     */     {
/*  43 */       return;
/*     */     }
/*     */ 
/*  46 */     boolean isPublish = this.m_service.isConditionVarTrue("IsPublish");
/*  47 */     if (!isPublish)
/*     */     {
/*  49 */       return;
/*     */     }
/*     */ 
/*  52 */     boolean isStaging = StringUtils.convertToBool(this.m_binder.getLocal("isStaging"), false);
/*  53 */     if (isStaging)
/*     */     {
/*  55 */       this.m_binder.putLocal("dPublishState", "S");
/*     */     }
/*     */     else
/*     */     {
/*  59 */       this.m_binder.putLocal("dPublishState", "P");
/*     */     }
/*     */ 
/*  63 */     String sourcePath = this.m_binder.getLocal("sourcePath");
/*  64 */     parseSourcePath(sourcePath);
/*     */ 
/*  67 */     String pagePath = this.m_binder.getLocal("pagePath");
/*  68 */     parsePagePath(pagePath);
/*     */ 
/*  71 */     boolean isPending = StringUtils.convertToBool(this.m_binder.getLocal("isPendingContributorRelease"), false);
/*  72 */     if (isPending)
/*     */     {
/*  74 */       this.m_binder.putLocal("dSourcePending", "Y");
/*     */     }
/*     */     else
/*     */     {
/*  78 */       this.m_binder.putLocal("dSourcePending", "N");
/*     */     }
/*     */ 
/*  82 */     String pType = this.m_binder.getLocal("docPublishType");
/*  83 */     parsePublishType(pType);
/*     */ 
/*  89 */     ResultSet rset = this.m_workspace.createResultSet("QprojectDocument", this.m_binder);
/*  90 */     if (rset.isEmpty())
/*     */     {
/*  92 */       this.m_workspace.execute("IprojectDocument", this.m_binder);
/*     */     }
/*     */     else
/*     */     {
/*  97 */       String oldSourceID = ResultSetUtils.getValue(rset, "dSourceDocID");
/*  98 */       String oldSourceInstance = ResultSetUtils.getValue(rset, "dSourceInstanceName");
/*     */ 
/* 100 */       String newSourceID = this.m_binder.getLocal("dSourceDocID");
/* 101 */       String newSourceInstance = this.m_binder.getLocal("dSourceInstanceName");
/*     */ 
/* 103 */       if ((!oldSourceInstance.equals(newSourceInstance)) || (!oldSourceID.equals(newSourceID)))
/*     */       {
/* 106 */         this.m_binder.putLocal("IsSourceChanged", "1");
/*     */       }
/*     */ 
/* 109 */       this.m_workspace.execute("UprojectDocument", this.m_binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void parseSourcePath(String sourcePath)
/*     */     throws ServiceException
/*     */   {
/* 117 */     if ((sourcePath == null) || (sourcePath.length() == 0))
/*     */     {
/* 119 */       this.m_binder.putLocal("dSourceInstanceName", "");
/* 120 */       this.m_binder.putLocal("dSourceDocName", "");
/* 121 */       this.m_binder.putLocal("dSourceDocID", "0");
/* 122 */       return;
/*     */     }
/*     */ 
/* 125 */     Properties props = ProjectUtils.parseSourcePath(sourcePath, "dSourceInstanceName", "idc://");
/*     */ 
/* 127 */     String[][] map = { { "dDocName", "dSourceDocName" }, { "dID", "dSourceDocID" } };
/*     */ 
/* 133 */     for (int i = 0; i < map.length; ++i)
/*     */     {
/* 135 */       String value = props.getProperty(map[i][0]);
/* 136 */       if (value == null)
/*     */         continue;
/* 138 */       props.put(map[i][1], value);
/* 139 */       props.remove(map[i][0]);
/*     */     }
/*     */ 
/* 143 */     DataBinder.mergeHashTables(this.m_binder.getLocalData(), props);
/*     */   }
/*     */ 
/*     */   protected void parsePagePath(String pagePath)
/*     */     throws ServiceException
/*     */   {
/* 150 */     if (pagePath == null)
/*     */     {
/* 152 */       return;
/*     */     }
/*     */ 
/* 155 */     String beginTag = "tcp://";
/* 156 */     int index = pagePath.indexOf(beginTag);
/* 157 */     if (index < 0)
/*     */     {
/* 159 */       String msg = LocaleUtils.encodeMessage("csProjSourcePathInvalidPrefix", null, pagePath, beginTag);
/*     */ 
/* 161 */       throw new ServiceException(msg);
/*     */     }
/* 163 */     pagePath = pagePath.substring(index + beginTag.length());
/*     */ 
/* 165 */     String projectID = null;
/* 166 */     int stopIndex = pagePath.indexOf(47);
/* 167 */     if (stopIndex < 0)
/*     */     {
/* 169 */       projectID = pagePath;
/*     */     }
/*     */     else
/*     */     {
/* 173 */       projectID = pagePath.substring(0, stopIndex);
/*     */     }
/*     */ 
/* 176 */     this.m_binder.putLocal("dProjectID", projectID);
/*     */ 
/* 178 */     String topParent = "";
/* 179 */     String immediateParent = "";
/* 180 */     String middleParents = "";
/* 181 */     if (stopIndex >= 0)
/*     */     {
/* 183 */       pagePath = pagePath.substring(stopIndex + 1);
/*     */ 
/* 185 */       Vector parents = new IdcVector();
/* 186 */       StringTokenizer tokens = new StringTokenizer(pagePath, "/");
/* 187 */       while (tokens.hasMoreTokens())
/*     */       {
/* 189 */         parents.addElement(tokens.nextToken());
/*     */       }
/*     */ 
/* 192 */       int numParents = parents.size();
/* 193 */       if (numParents > 0)
/*     */       {
/* 195 */         topParent = (String)parents.elementAt(0);
/*     */       }
/*     */ 
/* 198 */       if (numParents > 1)
/*     */       {
/* 200 */         immediateParent = (String)parents.elementAt(numParents - 1);
/*     */       }
/*     */ 
/* 203 */       if (numParents > 2)
/*     */       {
/* 206 */         int totalLen = 0;
/* 207 */         for (int i = numParents - 2; i > 0; --i)
/*     */         {
/* 209 */           String parent = (String)parents.elementAt(i);
/* 210 */           int len = parent.length();
/* 211 */           if (totalLen + len > 250)
/*     */           {
/* 213 */             middleParents = ".../" + middleParents;
/* 214 */             break;
/*     */           }
/* 216 */           middleParents = parent + "/" + middleParents;
/* 217 */           totalLen = middleParents.length();
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 222 */     this.m_binder.putLocal("dPrjTopParent", topParent);
/* 223 */     this.m_binder.putLocal("dPrjImmediateParent", immediateParent);
/* 224 */     this.m_binder.putLocal("dPrjMiddleParents", middleParents);
/*     */   }
/*     */ 
/*     */   protected void parsePublishType(String pTypes)
/*     */   {
/* 229 */     if (pTypes == null)
/*     */     {
/* 231 */       this.m_binder.putLocal("dPublishType", "");
/* 232 */       return;
/*     */     }
/*     */ 
/* 249 */     String charType = "";
/* 250 */     Vector types = StringUtils.parseArray(pTypes, ',', ',');
/* 251 */     int num = types.size();
/* 252 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 254 */       String type = (String)types.elementAt(i);
/* 255 */       if ((type.equalsIgnoreCase("toc")) || (type.equalsIgnoreCase("lof")) || (type.equalsIgnoreCase("lot")) || (type.equalsIgnoreCase("index")) || (type.equalsIgnoreCase("frameset")))
/*     */       {
/* 261 */         charType = "N";
/* 262 */         break;
/*     */       }
/* 264 */       if ((type.equalsIgnoreCase("query")) || (type.equalsIgnoreCase("results")))
/*     */       {
/* 268 */         charType = "S";
/* 269 */         break;
/*     */       }
/* 271 */       if ((type.equalsIgnoreCase("page")) || (type.equalsIgnoreCase("firstpage")))
/*     */       {
/* 274 */         charType = "P";
/* 275 */         break;
/*     */       }
/* 277 */       if (type.equals("splitpage"))
/*     */       {
/* 279 */         charType = "I";
/* 280 */         break;
/*     */       }
/* 282 */       if (type.equalsIgnoreCase("entrypoint"))
/*     */       {
/* 284 */         charType = "H";
/* 285 */         break;
/*     */       }
/* 287 */       if (type.equalsIgnoreCase("gallery"))
/*     */       {
/* 289 */         charType = "G";
/* 290 */         break;
/*     */       }
/* 292 */       if (!type.equalsIgnoreCase("graphic"))
/*     */         continue;
/* 294 */       charType = "C";
/* 295 */       break;
/*     */     }
/*     */ 
/* 299 */     if ((charType.length() == 0) && (num > 0))
/*     */     {
/* 301 */       charType = "O";
/*     */     }
/*     */ 
/* 304 */     this.m_binder.putLocal("dPublishType", charType);
/*     */   }
/*     */ 
/*     */   protected void checkWorkflowProject(boolean isStaging) throws ServiceException
/*     */   {
/* 309 */     if (!isStaging)
/*     */     {
/* 311 */       return;
/*     */     }
/* 313 */     String projectID = this.m_binder.getLocal("dProjectID");
/* 314 */     String errMsg = null;
/* 315 */     ProjectInfo info = Projects.getProjectInfo(projectID);
/* 316 */     if (info != null)
/*     */     {
/* 318 */       boolean isRemoved = StringUtils.convertToBool(info.m_properties.getProperty("IsWfProjectRemovalPending"), false);
/* 319 */       if (isRemoved)
/*     */       {
/* 321 */         errMsg = LocaleUtils.encodeMessage("csProjPendingRemoval", null, projectID);
/*     */       }
/*     */ 
/* 324 */       if (!info.m_hasWorkflow)
/*     */       {
/* 326 */         errMsg = LocaleUtils.encodeMessage("csProjNotRegistered", null, projectID);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 332 */       errMsg = LocaleUtils.encodeMessage("csProjDoesNotExist", null, projectID);
/*     */     }
/*     */ 
/* 335 */     if (errMsg == null)
/*     */       return;
/* 337 */     this.m_service.createServiceException(null, errMsg);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadRegisteredProjects()
/*     */     throws DataException, ServiceException
/*     */   {
/* 344 */     String type = this.m_currentAction.getParamAt(0);
/*     */ 
/* 346 */     DataResultSet drset = Projects.buildProjectSetOfType(type);
/* 347 */     this.m_binder.addResultSet("RegisteredProjects", drset);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 352 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70705 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ProjectDocHandler
 * JD-Core Version:    0.5.4
 */