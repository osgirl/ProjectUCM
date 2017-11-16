/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.server.project.ProblemReportUtils;
/*     */ import intradoc.server.project.ProjectInfo;
/*     */ import intradoc.server.project.Projects;
/*     */ import intradoc.server.proxy.OutgoingProviderManager;
/*     */ import intradoc.server.proxy.OutgoingProviderMonitor;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SecurityAccessListUtils;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ProblemReportHandler extends ServiceHandler
/*     */ {
/*     */   protected DocServiceHandler m_docHandler;
/*     */   protected DataBinder m_decoder;
/*     */ 
/*     */   public ProblemReportHandler()
/*     */   {
/*  40 */     this.m_docHandler = null;
/*  41 */     this.m_decoder = null;
/*     */   }
/*     */ 
/*     */   public void init(Service service) throws ServiceException, DataException
/*     */   {
/*  46 */     super.init(service);
/*     */ 
/*  48 */     this.m_docHandler = ((DocServiceHandler)ComponentClassFactory.createClassInstance("DocServiceHandler", "intradoc.server.DocServiceHandler", "!csDocServiceHandlerForWorkflowError"));
/*     */ 
/*  51 */     this.m_docHandler.init(this.m_service);
/*  52 */     this.m_docHandler.setIsWorkflow(true);
/*     */ 
/*  54 */     this.m_decoder = new DataBinder();
/*  55 */     this.m_decoder.m_isCgi = true;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void validateProblemReport()
/*     */     throws DataException, ServiceException
/*     */   {
/*  65 */     String caption = this.m_binder.get("dPrCaption");
/*  66 */     if (caption != null)
/*     */     {
/*  68 */       caption = caption.trim();
/*  69 */       this.m_binder.putLocal("dPrCaption", caption);
/*     */     }
/*  71 */     if ((caption != null) && (caption.length() != 0))
/*     */       return;
/*  73 */     this.m_service.createServiceException(null, "!csPrCaptionNotDefined");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addProblemReport()
/*     */     throws DataException, ServiceException
/*     */   {
/*  80 */     DataBinder prData = new DataBinder();
/*  81 */     createProblemReportData(prData);
/*  82 */     ProblemReportUtils.createProblemReport(this.m_binder, prData);
/*     */ 
/*  85 */     Date dte = new Date();
/*  86 */     String createDate = LocaleUtils.formatODBC(dte);
/*  87 */     this.m_binder.putLocal("dPrCreateDate", createDate);
/*     */   }
/*     */ 
/*     */   protected void createProblemReportData(DataBinder prData)
/*     */     throws DataException, ServiceException
/*     */   {
/*  93 */     String extraFields = this.m_binder.getLocal("ExtraProblemReportFields");
/*  94 */     Vector extras = StringUtils.parseArray(extraFields, ',', '^');
/*     */ 
/*  96 */     String[] stdFields = { "prMessage" };
/*     */ 
/*  98 */     int numStd = stdFields.length;
/*  99 */     int num = numStd + extras.size();
/* 100 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 102 */       String key = null;
/* 103 */       if (i < numStd)
/*     */       {
/* 105 */         key = stdFields[i];
/*     */       }
/*     */       else
/*     */       {
/* 109 */         key = (String)extras.elementAt(i - numStd);
/*     */       }
/*     */ 
/* 112 */       String value = this.m_binder.getLocal(key);
/* 113 */       if (value == null)
/*     */         continue;
/* 115 */       prData.putLocal(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateProblemReport()
/*     */     throws DataException, ServiceException
/*     */   {
/* 125 */     Date dte = new Date();
/* 126 */     String changeDate = LocaleUtils.formatODBC(dte);
/* 127 */     this.m_binder.putLocal("dPrChangeDate", changeDate);
/*     */ 
/* 129 */     DataBinder prData = null;
/*     */     try
/*     */     {
/* 132 */       prData = ProblemReportUtils.readProblemReport(this.m_binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 137 */       prData = new DataBinder();
/*     */     }
/*     */ 
/* 141 */     DataBinder newPrData = new DataBinder();
/* 142 */     createProblemReportData(newPrData);
/* 143 */     prData.merge(newPrData);
/*     */ 
/* 145 */     ProblemReportUtils.createProblemReport(this.m_binder, prData);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteProblemReport() throws DataException, ServiceException
/*     */   {
/* 151 */     ProblemReportUtils.deleteProblemReport(this.m_binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void checkProblemReportSecurity() throws DataException, ServiceException
/*     */   {
/* 157 */     boolean isChecked = false;
/* 158 */     int numParams = this.m_currentAction.getNumParams();
/* 159 */     if (numParams > 0)
/*     */     {
/* 161 */       String rsName = this.m_currentAction.getParamAt(0);
/* 162 */       ResultSet rset = this.m_binder.getResultSet(rsName);
/* 163 */       if ((rset == null) || (rset.isEmpty()))
/*     */       {
/* 168 */         int userRights = SecurityUtils.determineGroupPrivilege(this.m_service.getUserData(), "#AppsGroup");
/* 169 */         int rights = SecurityAccessListUtils.getRightsForApp("Workflow");
/* 170 */         if ((rights & userRights) == 0)
/*     */         {
/* 172 */           this.m_service.createServiceException(null, "!csPrPermissionDenied");
/*     */         }
/* 174 */         isChecked = true;
/*     */       }
/*     */     }
/*     */ 
/* 178 */     if (isChecked)
/*     */       return;
/* 180 */     this.m_service.checkSecurity();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void allowProblemReportAction()
/*     */     throws DataException, ServiceException
/*     */   {
/* 188 */     String action = this.m_currentAction.getParamAt(0);
/* 189 */     this.m_service.setCachedObject("TestProblemReportAction", action);
/* 190 */     if (PluginFilters.filter("allowProblemReportAction", this.m_workspace, this.m_binder, this.m_service) != 0)
/*     */     {
/* 193 */       return;
/*     */     }
/*     */ 
/* 196 */     String curState = this.m_binder.getFromSets("dPrState");
/* 197 */     String newState = this.m_binder.getLocal("dPrState");
/* 198 */     if ((!action.equals("delete")) && (((!action.equals("update")) || (curState.equals(newState)) || (newState.equals("OPEN")) || (newState.equals("FIXED"))))) {
/*     */       return;
/*     */     }
/* 201 */     String curAuthor = this.m_binder.getFromSets("dPrAuthor");
/* 202 */     if (this.m_docHandler.checkNonOwnershipAdmin(curAuthor))
/*     */       return;
/* 204 */     this.m_service.createServiceExceptionEx(null, "!csPrPermissionDenied_" + action, -18);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void retrieveProblemReportInfo()
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 215 */       DataBinder prData = ProblemReportUtils.readProblemReportEx(this.m_binder, false);
/* 216 */       this.m_binder.merge(prData);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 220 */       IdcMessage msg = IdcMessageFactory.lc(e);
/* 221 */       this.m_binder.putLocal("PrFileException", LocaleUtils.encodeMessage(msg));
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void buildSourceInfo()
/*     */     throws DataException, ServiceException
/*     */   {
/* 229 */     String srcInstance = this.m_binder.getAllowMissing("dSourceInstanceName");
/* 230 */     if ((srcInstance == null) || (srcInstance.length() == 0))
/*     */     {
/* 232 */       return;
/*     */     }
/*     */ 
/* 235 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 236 */     if (!idcName.equals(srcInstance))
/*     */       return;
/* 238 */     String httpCgiRelativeUrl = LegacyDirectoryLocator.getCgiWebUrl(false);
/* 239 */     this.m_binder.putLocal("SourceCgiPath", httpCgiRelativeUrl);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadPRStateLists()
/*     */     throws DataException, ServiceException
/*     */   {
/* 246 */     ProblemReportUtils.loadStateLists(this.m_binder, this.m_workspace, this.m_service);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void notifyContributor()
/*     */     throws DataException, ServiceException
/*     */   {
/* 259 */     this.m_binder.putLocal("IdcService", "NOTIFY_CONTRIBUTOR");
/* 260 */     this.m_binder.putLocal("dSourceDocID", this.m_binder.get("dSourceDocID"));
/* 261 */     this.m_binder.putLocal("dSourceDocName", this.m_binder.get("dSourceDocName"));
/*     */ 
/* 264 */     this.m_binder.putLocal("HttpPublishServerRoot", LegacyDirectoryLocator.getCgiWebUrl(true));
/*     */ 
/* 267 */     String sourceInstance = this.m_binder.get("dSourceInstanceName");
/* 268 */     if ((sourceInstance == null) || (sourceInstance.trim().length() == 0))
/*     */     {
/* 271 */       return;
/*     */     }
/*     */ 
/* 274 */     String errMsg = LocaleUtils.encodeMessage("csPrUnableToNotifyContributor", null, sourceInstance, this.m_binder.getAllowMissing("dDocName"));
/*     */ 
/* 276 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 277 */     if (sourceInstance.equals(idcName))
/*     */     {
/* 280 */       Properties localData = this.m_binder.getLocalData();
/* 281 */       Properties props = (Properties)localData.clone();
/* 282 */       this.m_binder.setLocalData(props);
/*     */       try
/*     */       {
/* 285 */         this.m_service.executeService("NOTIFY_CONTRIBUTOR");
/*     */       }
/*     */       finally
/*     */       {
/* 289 */         this.m_binder.setLocalData(localData);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 295 */       Provider provider = OutgoingProviderMonitor.getOutgoingProvider(sourceInstance);
/* 296 */       if (provider == null)
/*     */       {
/* 298 */         errMsg = LocaleUtils.appendMessage("!csOutgoingProviderNotConfigured", errMsg);
/*     */ 
/* 300 */         Report.warning(null, errMsg, null);
/* 301 */         return;
/*     */       }
/*     */ 
/* 305 */       Properties props = provider.getProviderState();
/* 306 */       boolean isBad = StringUtils.convertToBool(props.getProperty("IsBadConnection"), false);
/* 307 */       if (isBad)
/*     */       {
/* 309 */         errMsg = LocaleUtils.appendMessage("!csOutgoingProviderConnectionDown", errMsg);
/*     */ 
/* 311 */         Report.warning(null, errMsg, null);
/* 312 */         return;
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 317 */         DataBinder binder = new DataBinder();
/* 318 */         binder.merge(this.m_binder);
/*     */ 
/* 320 */         DataBinder responseData = OutgoingProviderManager.doSecureRequest(provider, binder, this.m_service);
/*     */ 
/* 322 */         int statusCode = NumberUtils.parseInteger(responseData.getLocal("StatusCode"), 0);
/* 323 */         if (statusCode < 0)
/*     */         {
/* 325 */           String stMessage = responseData.getLocal("StatusMessage");
/* 326 */           if (stMessage == null)
/*     */           {
/* 328 */             stMessage = "!csUnknownError";
/*     */           }
/*     */ 
/* 331 */           errMsg = LocaleUtils.appendMessage(stMessage, errMsg);
/* 332 */           Report.warning(null, errMsg, null);
/*     */         }
/*     */ 
/* 336 */         Enumeration en = this.m_binder.getResultSetList();
/* 337 */         while (en.hasMoreElements())
/*     */         {
/* 339 */           String key = (String)en.nextElement();
/* 340 */           ResultSet rset = this.m_binder.getResultSet(key);
/* 341 */           rset.first();
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 346 */         Report.warning(null, errMsg, e);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void notifyPublisher() throws DataException, ServiceException, IOException
/*     */   {
/* 354 */     String projectID = this.m_binder.get("dProjectID");
/* 355 */     ProjectInfo info = Projects.getProjectInfo(projectID);
/*     */ 
/* 357 */     List workflowXml = info.m_workflowXml;
/* 358 */     if ((workflowXml == null) || (workflowXml.size() == 0))
/*     */     {
/* 360 */       return;
/*     */     }
/* 362 */     PropertiesTreeNode node = (PropertiesTreeNode)workflowXml.get(0);
/*     */ 
/* 364 */     Properties props = node.m_properties;
/* 365 */     DataBinder.mergeHashTables(this.m_binder.getLocalData(), props);
/*     */ 
/* 367 */     String email = props.getProperty("authorEmail");
/* 368 */     if (email == null)
/*     */     {
/* 370 */       return;
/*     */     }
/* 372 */     email = this.m_decoder.decode(email);
/*     */ 
/* 375 */     String subjectTmp = "Problem Report: <$dPrCaption$>";
/* 376 */     if (this.m_currentAction.getNumParams() > 0)
/*     */     {
/* 378 */       subjectTmp = this.m_currentAction.getParamAt(0);
/*     */     }
/*     */ 
/* 382 */     UserData userData = UserUtils.createUserData(email);
/* 383 */     userData.setProperty("dEmail", email);
/* 384 */     addToMailQueue(userData, "PR_PUBLISHER_MAIL", subjectTmp);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void sendMailTo() throws DataException, ServiceException
/*     */   {
/* 390 */     String mailPage = this.m_currentAction.getParamAt(0);
/* 391 */     String subjectTmp = this.m_currentAction.getParamAt(1);
/*     */ 
/* 394 */     String author = this.m_binder.get("dDocAuthor");
/* 395 */     UserData userData = UserStorage.retrieveUserDatabaseProfileData(author, this.m_workspace, this.m_service);
/* 396 */     if (userData == null)
/*     */     {
/* 399 */       return;
/*     */     }
/*     */ 
/* 402 */     addToMailQueue(userData, mailPage, subjectTmp);
/*     */   }
/*     */ 
/*     */   protected void addToMailQueue(UserData userData, String mailPage, String subjectTmp)
/*     */   {
/* 410 */     String email = userData.getProperty("dEmail");
/* 411 */     if (email == null)
/*     */     {
/* 414 */       return;
/*     */     }
/*     */ 
/* 417 */     Vector mailQueue = (Vector)this.m_service.getCachedObject("MailQueue");
/* 418 */     if (mailQueue == null)
/*     */     {
/* 420 */       mailQueue = new IdcVector();
/* 421 */       this.m_service.setCachedObject("MailQueue", mailQueue);
/*     */     }
/*     */ 
/* 426 */     Properties props = DataBinderUtils.createMergedProperties(this.m_binder);
/*     */ 
/* 428 */     props.put("mailAddress", email);
/* 429 */     props.put("mailTemplate", mailPage);
/* 430 */     props.put("mailSubject", subjectTmp);
/*     */ 
/* 434 */     props.put("mailUsers", userData.m_name);
/* 435 */     Vector users = new IdcVector();
/* 436 */     users.addElement(userData);
/* 437 */     props.put("mailUserInfos", InternetFunctions.createMailUserInfoString(users));
/*     */ 
/* 439 */     mailQueue.addElement(props);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void searchProblemReports()
/*     */     throws DataException, ServiceException
/*     */   {
/* 446 */     String[][] sqlInfo = DataUtils.lookupSQL("ProblemReports");
/* 447 */     String sql = sqlInfo[0][0];
/* 448 */     String lcSql = sql.toLowerCase();
/* 449 */     if (lcSql.indexOf("where") >= 0)
/*     */     {
/* 451 */       sql = sql + " AND dPrID=0";
/*     */     }
/*     */ 
/* 454 */     ResultSet rset = this.m_workspace.createResultSetSQL(sql);
/*     */ 
/* 456 */     String wildCards = SharedObjects.getEnvironmentValue("DatabaseWildcards");
/* 457 */     char wildCard = wildCards.charAt(0);
/*     */ 
/* 459 */     boolean hasProjectID = false;
/* 460 */     String whereClause = "";
/* 461 */     int num = rset.getNumFields();
/* 462 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 464 */       FieldInfo info = new FieldInfo();
/* 465 */       rset.getIndexFieldInfo(i, info);
/*     */ 
/* 467 */       if (info.m_type != 6)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 472 */       String key = info.m_name;
/* 473 */       String value = this.m_binder.getLocal(key);
/*     */ 
/* 475 */       if (value != null)
/*     */       {
/* 477 */         value = value.trim();
/*     */       }
/* 479 */       if (value == null) continue; if (value.length() == 0)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 484 */       if (key.equals("dProjectID"))
/*     */       {
/* 488 */         if (hasProjectID) {
/*     */           continue;
/*     */         }
/*     */ 
/* 492 */         hasProjectID = true;
/* 493 */         key = "ProblemReports." + key;
/*     */ 
/* 496 */         boolean isPreserveCase = StringUtils.convertToBool(this.m_workspace.getProperty("DatabasePreserveCase"), false);
/* 497 */         if (isPreserveCase)
/*     */         {
/* 499 */           value = value.toUpperCase();
/*     */         }
/*     */       }
/*     */ 
/* 503 */       if (whereClause.length() > 0)
/*     */       {
/* 505 */         whereClause = whereClause + " AND ";
/*     */       }
/* 507 */       whereClause = whereClause + key + " LIKE '" + wildCard + value + wildCard + "'";
/*     */     }
/*     */ 
/* 510 */     this.m_binder.putLocal("dataSource", "ProblemReports");
/* 511 */     this.m_binder.putLocal("whereClause", whereClause);
/* 512 */     this.m_binder.putLocal("resultName", "ProblemReports");
/*     */ 
/* 515 */     this.m_service.setConditionVar("AllowDataSourceAccess", true);
/*     */ 
/* 517 */     this.m_service.createResultSetSQL();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 522 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87442 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ProblemReportHandler
 * JD-Core Version:    0.5.4
 */