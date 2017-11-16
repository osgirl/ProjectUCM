/*      */ package intradoc.server.workflow;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainerUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.MapParameters;
/*      */ import intradoc.data.QueryUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.InternetFunctions;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.UserProfileManager;
/*      */ import intradoc.server.UserStorage;
/*      */ import intradoc.shared.AliasData;
/*      */ import intradoc.shared.CollaborationData;
/*      */ import intradoc.shared.CollaborationUtils;
/*      */ import intradoc.shared.Collaborations;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.ProfileUtils;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.workflow.WfStepData;
/*      */ import intradoc.shared.workflow.WorkflowInfo;
/*      */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*      */ import intradoc.shared.workflow.WorkflowTemplates;
/*      */ import intradoc.util.CollectionUtils;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class WorkflowUtils
/*      */ {
/*   88 */   public static String[] m_allTopicColumns = null;
/*   89 */   public static String[] m_updateColumns = null;
/*   90 */   public static String[] m_userUpdateColumns = null;
/*      */ 
/*   92 */   public static List m_topicColumnList = new ArrayList();
/*   93 */   public static boolean m_keepRejectEventsInQueue = true;
/*      */ 
/*   96 */   public static String[] WORKFLOWACTIONHISTORY_COLUMNS = { "dWfStepName", "dWfName", "wfActionTs", "wfAction", "wfUsers", "wfMessage" };
/*      */ 
/*      */   public static long writeTemplates(String dir, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  106 */     return writeFile(dir, "wftemplates.hda", binder);
/*      */   }
/*      */ 
/*      */   public static long writeTemplate(String dir, String name, DataBinder binder)
/*      */     throws DataException, ServiceException
/*      */   {
/*  113 */     Properties props = new Properties();
/*  114 */     Properties oldProps = binder.getLocalData();
/*  115 */     binder.setLocalData(props);
/*      */ 
/*  118 */     String oldKey = "dWfAutoContributeStepType";
/*  119 */     String val = oldProps.getProperty(oldKey);
/*  120 */     if (val != null)
/*      */     {
/*  122 */       props.put(oldKey, val);
/*      */     }
/*      */ 
/*  126 */     WorkflowScriptUtils.updateStepExitConditionInfo(oldProps, binder);
/*      */ 
/*  129 */     WorkflowScriptUtils.updateStepCustomInfo(oldProps, binder);
/*      */ 
/*  131 */     long result = writeFile(dir, name, binder);
/*      */ 
/*  133 */     binder.setLocalData(oldProps);
/*  134 */     return result;
/*      */   }
/*      */ 
/*      */   public static long writeFile(String dir, String filename, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  141 */     if (binder != null)
/*      */     {
/*  143 */       ResourceUtils.serializeDataBinder(dir, filename, binder, true, false);
/*      */     }
/*      */ 
/*  146 */     File f = FileUtilsCfgBuilder.getCfgFile(dir + filename, "Workflow", false);
/*  147 */     return f.lastModified();
/*      */   }
/*      */ 
/*      */   public static DataBinder readTemplates() throws ServiceException
/*      */   {
/*  152 */     String dir = DirectoryLocator.getWorkflowDirectory();
/*  153 */     return readFile(dir, "wftemplates.hda", null);
/*      */   }
/*      */ 
/*      */   public static DataBinder readTemplate(String name, boolean isLock) throws ServiceException
/*      */   {
/*  158 */     String dir = DirectoryLocator.getWorkflowDirectory();
/*  159 */     String lockDir = null;
/*  160 */     if (isLock)
/*      */     {
/*  162 */       lockDir = dir;
/*      */     }
/*      */ 
/*  165 */     return readFile(dir, name, lockDir);
/*      */   }
/*      */ 
/*      */   public static DataBinder readFile(String dir, String filename, String lockDir)
/*      */     throws ServiceException
/*      */   {
/*  171 */     DataBinder binder = new DataBinder(true);
/*  172 */     if (lockDir != null)
/*      */     {
/*  174 */       FileUtils.reserveDirectory(lockDir);
/*      */     }
/*      */     try
/*      */     {
/*  178 */       ResourceUtils.serializeDataBinder(dir, filename, binder, false, false);
/*      */     }
/*      */     finally
/*      */     {
/*  182 */       if (lockDir != null)
/*      */       {
/*  184 */         FileUtils.releaseDirectory(lockDir);
/*      */       }
/*      */     }
/*  187 */     return binder;
/*      */   }
/*      */ 
/*      */   public static DataResultSet cacheTemplates(boolean isLock) throws ServiceException, DataException
/*      */   {
/*  192 */     String dir = DirectoryLocator.getWorkflowDirectory();
/*  193 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 1, false);
/*      */ 
/*  195 */     if (isLock)
/*      */     {
/*  197 */       FileUtils.reserveDirectory(dir, true);
/*      */     }
/*      */ 
/*  200 */     WorkflowTemplates templates = new WorkflowTemplates();
/*      */     try
/*      */     {
/*  204 */       DataBinder binder = null;
/*  205 */       String filename = "wftemplates.hda";
/*  206 */       File file = FileUtilsCfgBuilder.getCfgFile(dir + filename, "Workflow", false);
/*  207 */       if (file.exists())
/*      */       {
/*  209 */         binder = ResourceUtils.readDataBinder(dir, filename);
/*      */       }
/*      */       else
/*      */       {
/*  213 */         binder = new DataBinder();
/*      */       }
/*      */ 
/*  216 */       templates.load(binder);
/*      */ 
/*  218 */       boolean isChanged = updateReferences(templates, dir, "dWfTemplateName", "wftemplates.hda");
/*  219 */       if (isChanged)
/*      */       {
/*  222 */         binder.addResultSet("WfTemplates", templates);
/*  223 */         writeTemplates(dir, binder);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  228 */       if (isLock)
/*      */       {
/*  230 */         FileUtils.releaseDirectory(dir, true);
/*      */       }
/*      */     }
/*      */ 
/*  234 */     return templates;
/*      */   }
/*      */ 
/*      */   public static boolean updateReferences(DataResultSet drset, String dir, String lookupKey, String skipKey)
/*      */   {
/*  240 */     boolean isChanged = false;
/*      */ 
/*  242 */     FieldInfo fi = new FieldInfo();
/*  243 */     boolean isFound = drset.getFieldInfo(lookupKey, fi);
/*  244 */     if (isFound)
/*      */     {
/*  246 */       int nameIndex = fi.m_index;
/*  247 */       int numClmns = drset.getNumFields();
/*      */ 
/*  251 */       String[] fileNames = FileUtils.getMatchingFileNames(dir, "*.hda");
/*  252 */       int num = fileNames.length;
/*  253 */       for (int i = 0; i < num; ++i)
/*      */       {
/*  255 */         String name = fileNames[i];
/*  256 */         if (name.equals(skipKey))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  261 */         int index = name.indexOf(".hda");
/*  262 */         if (index < 0) {
/*      */           continue;
/*      */         }
/*      */ 
/*  266 */         name = name.substring(0, index);
/*      */ 
/*  268 */         Vector row = drset.findRow(0, name);
/*  269 */         if (row != null)
/*      */           continue;
/*  271 */         row = new IdcVector();
/*  272 */         for (int j = 0; j < numClmns; ++j)
/*      */         {
/*  274 */           if (j == nameIndex)
/*      */           {
/*  276 */             row.addElement(name);
/*      */           }
/*      */           else
/*      */           {
/*  280 */             row.addElement("");
/*      */           }
/*      */         }
/*  283 */         drset.addRow(row);
/*  284 */         isChanged = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  289 */     return isChanged;
/*      */   }
/*      */ 
/*      */   public static WfStepData determineSteps(Workspace ws, DataBinder binder)
/*      */     throws DataException
/*      */   {
/*  300 */     ResultSet stepSet = ws.createResultSet("QworkflowSteps", binder);
/*  301 */     if (stepSet.isEmpty())
/*      */     {
/*  303 */       String wfName = binder.get("dWfName");
/*  304 */       String errMsg = LocaleUtils.encodeMessage("csWfIncorrectWorkflow", null, wfName);
/*  305 */       throw new DataException(errMsg);
/*      */     }
/*      */ 
/*  308 */     WfStepData stepData = new WfStepData();
/*  309 */     stepData.load(stepSet);
/*  310 */     validateAndUpdateStepData(stepData, ws);
/*      */ 
/*  312 */     return stepData;
/*      */   }
/*      */ 
/*      */   public static Vector computeStepUsersEx(Workspace ws, DataBinder binder, boolean isValidateOnly, boolean isFromSets, ExecutionContext cxt, boolean isSpecial)
/*      */     throws DataException, ServiceException
/*      */   {
/*  318 */     Properties localData = binder.getLocalData();
/*  319 */     if (isFromSets)
/*      */     {
/*  321 */       binder.setLocalData(new Properties());
/*      */     }
/*      */ 
/*  324 */     Vector users = computeStepUsers(ws, binder, isValidateOnly, cxt, isSpecial, false);
/*      */ 
/*  326 */     if (isFromSets)
/*      */     {
/*  329 */       DataBinder cxtBinder = (DataBinder)cxt.getCachedObject("DataBinder");
/*  330 */       if (cxtBinder != null)
/*      */       {
/*  332 */         String hasTokens = cxtBinder.getLocal("hasTokens");
/*  333 */         if (hasTokens == null)
/*      */         {
/*  335 */           hasTokens = "0";
/*      */         }
/*  337 */         cxtBinder.removeLocal("hasTokens");
/*  338 */         localData.put("hasTokens", hasTokens);
/*      */       }
/*  340 */       binder.setLocalData(localData);
/*      */     }
/*      */ 
/*  343 */     return users;
/*      */   }
/*      */ 
/*      */   public static Vector computeStepUsers(Workspace ws, DataBinder binder, boolean isValidateOnly, ExecutionContext cxt, boolean isDelayComputation, boolean useLocal)
/*      */     throws DataException, ServiceException
/*      */   {
/*  349 */     AliasData aliasData = (AliasData)SharedObjects.getTable(AliasData.m_tableName);
/*      */ 
/*  351 */     Vector users = new IdcVector();
/*      */ 
/*  353 */     String stepType = binder.get("dWfStepType");
/*  354 */     String query = null;
/*  355 */     String aliasColumn = null;
/*  356 */     String typeColumn = null;
/*  357 */     if (WorkflowScriptUtils.isAutoContributorStep(stepType))
/*      */     {
/*  359 */       String author = null;
/*  360 */       String rsName = binder.getLocal("SecurityProfileResultSet");
/*  361 */       if (rsName != null)
/*      */       {
/*  363 */         ResultSet rset = binder.getResultSet(rsName);
/*  364 */         author = ResultSetUtils.getValue(rset, "dDocAuthor");
/*      */       }
/*      */       else
/*      */       {
/*  368 */         author = binder.getAllowMissing("dDocAuthor");
/*      */       }
/*  370 */       if ((author != null) && (author.length() > 0))
/*      */       {
/*  372 */         UserData data = UserStorage.retrieveUserDatabaseProfileData(author, ws, cxt);
/*  373 */         addUniqueUser(users, data);
/*      */       }
/*  375 */       return users;
/*      */     }
/*  377 */     query = "QworkflowStepAliases";
/*  378 */     aliasColumn = "dAlias";
/*  379 */     typeColumn = "dAliasType";
/*      */ 
/*  381 */     ResultSet rset = ws.createResultSet(query, binder);
/*  382 */     if (rset == null)
/*      */     {
/*  384 */       throw new DataException("Invalid alias query " + query + ".");
/*      */     }
/*      */ 
/*  387 */     DataResultSet drset = new DataResultSet();
/*  388 */     drset.copy(rset);
/*      */ 
/*  390 */     int aliasIndex = ResultSetUtils.getIndexMustExist(rset, aliasColumn);
/*  391 */     int typeIndex = ResultSetUtils.getIndexMustExist(rset, typeColumn);
/*      */ 
/*  393 */     for (; drset.isRowPresent(); drset.next())
/*      */     {
/*  395 */       String alias = drset.getStringValue(aliasIndex);
/*  396 */       String type = drset.getStringValue(typeIndex);
/*  397 */       if (isDelayComputation)
/*      */       {
/*  402 */         users.addElement(type + ":" + alias);
/*      */       }
/*      */       else
/*      */       {
/*  406 */         createUserList(users, alias, type, aliasData, isValidateOnly, true, useLocal, ws, cxt);
/*      */       }
/*      */     }
/*      */ 
/*  410 */     return users;
/*      */   }
/*      */ 
/*      */   public static void createUserList(Vector users, String alias, String type, AliasData aliasData, boolean isValidateOnly, boolean isComputeUserData, boolean useLocal, Workspace ws, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  418 */     if (type.equalsIgnoreCase("token"))
/*      */     {
/*  420 */       DataBinder cxtBinder = (DataBinder)cxt.getCachedObject("DataBinder");
/*      */ 
/*  424 */       if (isValidateOnly)
/*      */       {
/*  426 */         cxtBinder.putLocal("hasTokens", "1");
/*      */       }
/*      */       else
/*      */       {
/*  430 */         ResultSet rset = null;
/*  431 */         if (!useLocal)
/*      */         {
/*  437 */           rset = cxtBinder.getResultSet("DOC_INFO");
/*  438 */           if (rset != null)
/*      */           {
/*  440 */             cxtBinder.pushActiveResultSet("DOC_INFO", rset);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  445 */         Vector aliases = null;
/*  446 */         Service service = null;
/*  447 */         if (cxt instanceof Service)
/*      */         {
/*  449 */           service = (Service)cxt;
/*      */         }
/*  451 */         boolean oldAllowWorkflowIdocScript = false;
/*  452 */         if (service != null)
/*      */         {
/*  454 */           oldAllowWorkflowIdocScript = service.isConditionVarTrue("allowWorkflowIdocScript");
/*      */         }
/*      */         try
/*      */         {
/*  458 */           PageMerger pageMerger = (PageMerger)cxt.getCachedObject("PageMerger");
/*      */ 
/*  460 */           prepareWfScriptContext(cxt, cxtBinder);
/*      */ 
/*  463 */           String token = WfScriptManager.getTokenScript(alias);
/*  464 */           if (token == null)
/*      */           {
/*  466 */             Report.info(null, null, "csWfMissingToken", new Object[] { alias });
/*      */             return;
/*      */           }
/*  469 */           if (service != null)
/*      */           {
/*  471 */             service.setConditionVar("allowWorkflowIdocScript", true);
/*      */           }
/*  473 */           pageMerger.evaluateScriptReportError(token);
/*      */ 
/*  475 */           String str = cxtBinder.getLocal("tokenUsers");
/*  476 */           aliases = StringUtils.parseArrayEx(str, ',', '^', true);
/*  477 */           cxtBinder.removeLocal("tokenUsers");
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  481 */           Report.error("workflow", e, "csWfTokenEvalError", new Object[] { alias });
/*      */ 
/*  495 */           return;
/*      */         }
/*      */         finally
/*      */         {
/*  486 */           if (service != null)
/*      */           {
/*  488 */             service.setConditionVar("allowWorkflowIdocScript", oldAllowWorkflowIdocScript);
/*      */           }
/*      */ 
/*  493 */           if (rset != null)
/*      */           {
/*  495 */             cxtBinder.popActiveResultSet();
/*      */           }
/*      */         }
/*      */ 
/*  499 */         int num = aliases.size();
/*  500 */         for (int i = 0; i < num; ++i)
/*      */         {
/*  502 */           String user = (String)aliases.elementAt(i);
/*  503 */           if (i == num - 1)
/*      */           {
/*  505 */             type = "user";
/*      */           }
/*      */           else
/*      */           {
/*  509 */             type = (String)aliases.elementAt(++i);
/*      */           }
/*  511 */           if (isComputeUserData)
/*      */           {
/*  513 */             addUniqueUsers(users, user, type, aliasData, ws, cxt);
/*      */           }
/*      */           else
/*      */           {
/*  517 */             addUniqueUserNames(users, user, type, aliasData);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*  522 */     else if (isComputeUserData)
/*      */     {
/*  524 */       addUniqueUsers(users, alias, type, aliasData, ws, cxt);
/*      */     }
/*      */     else
/*      */     {
/*  528 */       addUniqueUserNames(users, alias, type, aliasData);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void prepareWfScriptContext(ExecutionContext cxt, DataBinder cxtBinder)
/*      */     throws DataException, ServiceException
/*      */   {
/*  541 */     WfCompanionData wfCompData = (WfCompanionData)cxt.getCachedObject("WorkflowCompanionData");
/*  542 */     if (wfCompData == null)
/*      */     {
/*  545 */       String docName = cxtBinder.get("dDocName");
/*  546 */       String subDir = cxtBinder.get("dWfDirectory");
/*  547 */       WfCompanionData wfCmpData = WfCompanionManager.getCompanionData(docName, subDir);
/*  548 */       if (wfCmpData == null)
/*      */       {
/*  550 */         return;
/*      */       }
/*  552 */       cxt.setCachedObject("WorkflowCompanionData", wfCmpData);
/*      */     }
/*      */ 
/*  555 */     DataBinder wfResultData = (DataBinder)cxt.getCachedObject("WorkflowScriptResult");
/*  556 */     if (wfResultData == null)
/*      */     {
/*  558 */       cxt.setCachedObject("WorkflowScriptResult", new DataBinder());
/*      */     }
/*      */ 
/*  561 */     if (SecurityUtils.m_useCollaboration)
/*      */     {
/*  566 */       CollaborationData clbraData = (CollaborationData)cxt.getCachedObject("WorkflowCollaborationData");
/*  567 */       if (clbraData == null)
/*      */       {
/*  569 */         retrieveCollaborationData(cxt, cxtBinder);
/*      */       }
/*      */     }
/*      */ 
/*  573 */     String stepName = cxtBinder.get("dWfStepName");
/*  574 */     String wfName = cxtBinder.get("dWfName");
/*  575 */     cxt.setCachedObject("WorkflowPrefix", stepName + "@" + wfName);
/*      */   }
/*      */ 
/*      */   public static void retrieveCollaborationData(ExecutionContext cxt, DataBinder cxtBinder)
/*      */     throws DataException, ServiceException
/*      */   {
/*  582 */     String clbraName = cxtBinder.getAllowMissing("dClbraName");
/*  583 */     if (clbraName == null)
/*      */     {
/*  585 */       String acct = cxtBinder.get("dDocAccount");
/*  586 */       clbraName = CollaborationUtils.parseCollaborationName(acct);
/*      */     }
/*  588 */     if (clbraName == null)
/*      */     {
/*  590 */       String errMsg = LocaleUtils.encodeMessage("csWfClbraRetrievalError", null, cxtBinder.get("dWfName"));
/*      */ 
/*  592 */       throw new ServiceException(errMsg);
/*      */     }
/*      */ 
/*  595 */     CollaborationData clbraData = Collaborations.getOrCreateCollaborationData(clbraName, false);
/*  596 */     cxt.setCachedObject("WorkflowCollaborationData", clbraData);
/*      */ 
/*  600 */     Properties props = clbraData.m_data.getLocalData();
/*  601 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*      */     {
/*  603 */       String name = (String)en.nextElement();
/*  604 */       String val = cxtBinder.getAllowMissing(name);
/*  605 */       if ((val == null) || (val.length() == 0))
/*      */       {
/*  607 */         cxtBinder.putLocal(name, props.getProperty(name));
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void addUniqueUsers(Vector users, String alias, String type, AliasData aliasData, Workspace ws, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  617 */     if (type.equalsIgnoreCase("alias"))
/*      */     {
/*  619 */       DataResultSet rset = aliasData.getUserSet(alias);
/*  620 */       addAliasUsers(users, rset, ws, cxt);
/*      */     }
/*      */     else
/*      */     {
/*  624 */       UserData data = UserStorage.retrieveUserDatabaseProfileData(alias, ws, cxt);
/*  625 */       addUniqueUser(users, data);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void addAliasUsers(Vector users, DataResultSet rset, Workspace ws, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  632 */     int userIndex = ResultSetUtils.getIndexMustExist(rset, "dUserName");
/*  633 */     for (; rset.isRowPresent(); rset.next())
/*      */     {
/*  635 */       String name = rset.getStringValue(userIndex);
/*  636 */       UserData data = UserStorage.retrieveUserDatabaseProfileData(name, ws, cxt);
/*  637 */       addUniqueUser(users, data);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void addUniqueUser(Vector users, UserData userData)
/*      */   {
/*  643 */     if (userData == null)
/*      */     {
/*  645 */       return;
/*      */     }
/*      */ 
/*  648 */     String name = userData.m_name;
/*      */ 
/*  650 */     int num = users.size();
/*  651 */     boolean isFound = false;
/*  652 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  654 */       UserData data = (UserData)users.elementAt(i);
/*  655 */       if (!name.equalsIgnoreCase(data.m_name))
/*      */         continue;
/*  657 */       isFound = true;
/*  658 */       break;
/*      */     }
/*      */ 
/*  662 */     if (isFound)
/*      */       return;
/*  664 */     users.addElement(userData);
/*      */   }
/*      */ 
/*      */   public static void buildUsersList(Vector users, Vector aliases, Vector udList, Workspace ws, ExecutionContext cxt, int desiredPriv)
/*      */     throws DataException, ServiceException
/*      */   {
/*  677 */     AliasData aliasData = (AliasData)SharedObjects.getTable(AliasData.m_tableName);
/*      */ 
/*  679 */     int num = users.size();
/*  680 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  682 */       String user = (String)users.elementAt(i);
/*  683 */       if (user.length() == 0)
/*      */       {
/*  685 */         ++i;
/*      */       }
/*      */       else {
/*  688 */         int priv = NumberUtils.parseInteger((String)users.elementAt(++i), 0);
/*  689 */         if ((desiredPriv != 0) && ((priv & desiredPriv) == 0))
/*      */           continue;
/*  691 */         addUniqueUsers(udList, user, "user", aliasData, ws, cxt);
/*      */       }
/*      */     }
/*      */ 
/*  695 */     num = aliases.size();
/*  696 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  698 */       String alias = (String)aliases.elementAt(i);
/*  699 */       if (alias.length() == 0)
/*      */       {
/*  701 */         ++i;
/*      */       }
/*      */       else {
/*  704 */         int priv = NumberUtils.parseInteger((String)aliases.elementAt(++i), 0);
/*  705 */         if ((desiredPriv != 0) && ((priv & desiredPriv) == 0))
/*      */           continue;
/*  707 */         addUniqueUsers(udList, alias, "alias", aliasData, ws, cxt);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void addUniqueUserNames(Vector users, String alias, String type, AliasData aliasData)
/*      */     throws DataException
/*      */   {
/*  720 */     if (type.equalsIgnoreCase("alias"))
/*      */     {
/*  722 */       DataResultSet rset = aliasData.getUserSet(alias);
/*  723 */       addAliasUserNames(users, rset);
/*      */     }
/*      */     else
/*      */     {
/*  727 */       addUniqueName(users, alias);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void addAliasUserNames(Vector users, DataResultSet rset)
/*      */     throws DataException
/*      */   {
/*  734 */     int userIndex = ResultSetUtils.getIndexMustExist(rset, "dUserName");
/*  735 */     for (; rset.isRowPresent(); rset.next())
/*      */     {
/*  737 */       String name = rset.getStringValue(userIndex);
/*  738 */       addUniqueName(users, name);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void addUniqueName(Vector users, String name)
/*      */   {
/*  744 */     int num = users.size();
/*  745 */     boolean isFound = false;
/*  746 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  748 */       String user = (String)users.elementAt(i);
/*  749 */       if (!name.equalsIgnoreCase(user))
/*      */         continue;
/*  751 */       isFound = true;
/*  752 */       break;
/*      */     }
/*      */ 
/*  756 */     if (isFound)
/*      */       return;
/*  758 */     users.addElement(name);
/*      */   }
/*      */ 
/*      */   public static Properties buildInformStepUsers(Vector users, DataBinder binder, WfStepData stepData, ExecutionContext cxt, WfCompanionData wfCompanionData, WorkflowInfo workflowInfo)
/*      */   {
/*  768 */     String wfName = binder.getLocal("dWfName");
/*  769 */     String stepName = binder.getLocal("dWfStepName");
/*  770 */     String docName = binder.getLocal("dDocName");
/*      */ 
/*  772 */     int numUsers = users.size();
/*  773 */     if (numUsers == 0)
/*      */     {
/*  775 */       Report.info(null, null, "csWfNoUsersForMail", new Object[] { wfName, stepName });
/*  776 */       return null;
/*      */     }
/*      */ 
/*  780 */     Properties props = DataBinderUtils.createMergedProperties(binder);
/*  781 */     DataBinder.mergeHashTables(props, wfCompanionData.m_data.getLocalData());
/*      */ 
/*  785 */     if (stepData.isLastRow())
/*      */     {
/*  787 */       props.put("IsLastStep", "1");
/*      */     }
/*      */ 
/*  790 */     props.put("nextStepName", stepName);
/*      */ 
/*  792 */     if ((!stepData.getIsAll()) && (stepData.getWeight() == 0))
/*      */     {
/*  794 */       props.put("IsNotifyOnly", "1");
/*      */     }
/*      */ 
/*  797 */     Properties oldProps = binder.getLocalData();
/*  798 */     binder.setLocalData(props);
/*      */     try
/*      */     {
/*  801 */       String wfMessage = binder.getLocal("wfMessage");
/*  802 */       if ((wfMessage == null) || (wfMessage.length() == 0))
/*      */       {
/*  804 */         wfMessage = "<$include wf_approve_mail_message$>";
/*      */       }
/*      */ 
/*  808 */       PageMerger pageMerger = (PageMerger)cxt.getCachedObject("PageMerger");
/*  809 */       props.put("wfMessage", pageMerger.evaluateScript(wfMessage));
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  813 */       String errMsg = LocaleUtils.encodeMessage("csWfApproveMsg", null, docName, stepName, wfName);
/*  814 */       props.put("wfMessage", errMsg);
/*      */     }
/*      */     finally
/*      */     {
/*  819 */       binder.setLocalData(oldProps);
/*      */     }
/*      */ 
/*  823 */     Vector userNames = new IdcVector();
/*  824 */     StringBuffer emailBuff = new StringBuffer();
/*      */     int i;
/*  825 */     for (int i = 0; i < numUsers; ++i)
/*      */     {
/*  827 */       UserData userData = (UserData)users.elementAt(i);
/*  828 */       userNames.addElement(userData.m_name);
/*      */ 
/*  830 */       String mailStr = userData.getProperty("dEmail");
/*  831 */       if (mailStr.length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*  835 */       if (emailBuff.length() > 0)
/*      */       {
/*  837 */         emailBuff.append(",");
/*      */       }
/*  839 */       emailBuff.append(mailStr);
/*      */     }
/*      */ 
/*  842 */     boolean isStaging = StringUtils.convertToBool(binder.getLocal("isStaging"), false);
/*  843 */     String template = binder.getLocal("wfMailTemplate");
/*  844 */     if ((template == null) || (template.length() == 0))
/*      */     {
/*  846 */       template = props.getProperty("wfMailTemplate");
/*  847 */       if ((template == null) || (template.length() == 0))
/*      */       {
/*  849 */         if (isStaging)
/*      */         {
/*  851 */           template = "REVIEWER_MAIL";
/*      */         }
/*  855 */         else if (stepData.getCurrentRow() == 0)
/*      */         {
/*  858 */           template = "CONTRIBUTOR_MAIL";
/*      */         }
/*      */         else
/*      */         {
/*  862 */           template = "REVIEWER_MAIL";
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  868 */     String subjectTmp = binder.getLocal("wfMailSubject");
/*  869 */     if ((subjectTmp == null) || (subjectTmp.length() == 0))
/*      */     {
/*  872 */       subjectTmp = props.getProperty("wfMailSubject");
/*  873 */       if ((subjectTmp == null) || (subjectTmp.length() == 0))
/*      */       {
/*  875 */         subjectTmp = "<$include wf_approve_mail_subject$>";
/*      */       }
/*      */     }
/*      */ 
/*  879 */     props.put("mailAddress", emailBuff.toString());
/*  880 */     props.put("mailSubject", subjectTmp);
/*  881 */     props.put("mailTemplate", template);
/*      */ 
/*  883 */     String str = StringUtils.createString(userNames, ',', '^');
/*  884 */     props.put("wfUsers", str);
/*      */ 
/*  887 */     props.put("mailUsers", str);
/*  888 */     props.put("mailUserInfos", InternetFunctions.createMailUserInfoString(users));
/*      */ 
/*  890 */     return props;
/*      */   }
/*      */ 
/*      */   public static DataResultSet getWorkflowDocuments(Workspace ws, DataBinder binder)
/*      */     throws DataException
/*      */   {
/*  899 */     return getWorkflowDocumentsEx(ws, binder, "QworkflowDocuments", true);
/*      */   }
/*      */ 
/*      */   public static DataResultSet getWorkflowDocumentsEx(Workspace ws, DataBinder binder, String baseQuery, boolean allowMissing)
/*      */     throws DataException
/*      */   {
/*  920 */     DataResultSet drset = new DataResultSet();
/*      */ 
/*  922 */     ResultSet rset = ws.createResultSet(baseQuery, binder);
/*  923 */     if (rset.isEmpty())
/*      */     {
/*  925 */       return drset;
/*      */     }
/*      */ 
/*  928 */     int maxWorkflowDocsList = SharedObjects.getEnvironmentInt("MaxWorkflowDocResults", 500);
/*      */ 
/*  930 */     drset.copy(rset, maxWorkflowDocsList);
/*      */ 
/*  932 */     DataBinder args = new DataBinder();
/*  933 */     args.addResultSet("WfDocuments", drset);
/*      */ 
/*  935 */     boolean isExpanded = false;
/*  936 */     while (drset.isRowPresent())
/*      */     {
/*  939 */       int currentRow = drset.getCurrentRow();
/*      */ 
/*  941 */       rset = ws.createResultSet("QdocNameMeta", args);
/*  942 */       DataResultSet dummy = new DataResultSet();
/*  943 */       dummy.copy(rset, 1);
/*  944 */       if (!isExpanded)
/*      */       {
/*  947 */         drset.mergeFields(dummy);
/*  948 */         isExpanded = true;
/*      */       }
/*      */ 
/*  951 */       boolean doNext = true;
/*  952 */       if (!dummy.isEmpty())
/*      */       {
/*  954 */         if (dummy.isRowPresent())
/*      */         {
/*  956 */           drset.merge("dDocName", dummy, false);
/*      */         }
/*      */       }
/*  959 */       else if (!allowMissing)
/*      */       {
/*  961 */         drset.deleteCurrentRow();
/*  962 */         doNext = false;
/*      */       }
/*      */ 
/*  966 */       drset.setCurrentRow(currentRow);
/*  967 */       if (doNext)
/*      */       {
/*  970 */         drset.next();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  976 */     drset.first();
/*      */ 
/*  978 */     return drset;
/*      */   }
/*      */ 
/*      */   public static WfStepData loadCurrentStepInfo(DataBinder binder, Workspace ws, Service service)
/*      */     throws DataException, ServiceException
/*      */   {
/*  984 */     ResultSet rset = ws.createResultSet("QwfCurrentStep", binder);
/*  985 */     if (rset == null)
/*      */     {
/*  987 */       String errMsg = LocaleUtils.encodeMessage("csWfMissingStepsTable", null, binder.get("dWfName"));
/*  988 */       throw new DataException(errMsg);
/*      */     }
/*      */ 
/*  991 */     if (rset.isEmpty())
/*      */     {
/*  993 */       throw new ServiceException("!csWfNotInWorkflow");
/*      */     }
/*      */ 
/*  996 */     WfStepData stepData = new WfStepData();
/*  997 */     stepData.loadStepDataType(rset, 0);
/*  998 */     validateAndUpdateStepData(stepData, ws);
/*  999 */     binder.addResultSet("WorkflowStep", stepData);
/*      */ 
/* 1001 */     Vector users = computeStepUsersEx(ws, binder, false, false, service, false);
/*      */ 
/* 1004 */     boolean hasTokens = StringUtils.convertToBool(binder.getLocal("hasTokens"), false);
/* 1005 */     stepData.addUsers(users, hasTokens);
/*      */ 
/* 1007 */     return stepData;
/*      */   }
/*      */ 
/*      */   public static void validateAndUpdateStepData(WfStepData stepData, Workspace ws)
/*      */     throws DataException
/*      */   {
/* 1013 */     FieldInfo fi1 = new FieldInfo();
/* 1014 */     FieldInfo fi2 = new FieldInfo();
/* 1015 */     DataBinder binder = null;
/* 1016 */     if ((!stepData.getFieldInfo("dWfStepType", fi1)) || (!stepData.getFieldInfo("dWfStepID", fi2)))
/*      */       return;
/* 1018 */     int currentRow = stepData.getCurrentRow();
/*      */ 
/* 1020 */     int typeIndex = fi1.m_index;
/*      */ 
/* 1022 */     for (stepData.first(); stepData.isRowPresent(); stepData.next())
/*      */     {
/* 1024 */       String stepType = stepData.getStringValue(typeIndex);
/* 1025 */       String newStepType = WorkflowScriptUtils.getUpgradedStepType(stepType);
/* 1026 */       if (newStepType.equals(stepType))
/*      */         continue;
/* 1028 */       if (binder == null)
/*      */       {
/* 1030 */         binder = new DataBinder();
/* 1031 */         binder.addResultSet("WfStepData", stepData);
/*      */       }
/* 1033 */       stepData.setCurrentValue(typeIndex, newStepType);
/* 1034 */       ws.execute("UworkflowStepType", binder);
/*      */     }
/*      */ 
/* 1037 */     stepData.setCurrentRow(currentRow);
/*      */   }
/*      */ 
/*      */   public static void computeDocStepInfo(WfStepData stepData, DataBinder binder, Service service, Workspace ws)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1044 */     service.setCachedObject("WfStepData", stepData);
/* 1045 */     if (PluginFilters.filter("workflowComputeDocStepInfo", ws, binder, service) != 0)
/*      */     {
/* 1048 */       return;
/*      */     }
/*      */ 
/* 1052 */     String stepID = binder.getActiveValue("dWfCurrentStepID");
/*      */ 
/* 1055 */     if (stepID.equals("0"))
/*      */     {
/* 1057 */       binder.putLocal("dWfStepName", "inactive");
/* 1058 */       return;
/*      */     }
/*      */ 
/* 1062 */     int stepIndex = ResultSetUtils.getIndexMustExist(stepData, "dWfStepID");
/* 1063 */     Vector stepValues = stepData.findRow(stepIndex, stepID);
/* 1064 */     if (stepValues == null)
/*      */     {
/* 1066 */       String msg = LocaleUtils.encodeMessage("csUnableToFindWorkflowStepInfo2", null, stepID);
/*      */ 
/* 1068 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 1073 */     String wfState = binder.getActiveValue("dWorkflowState");
/* 1074 */     String publishState = binder.getActiveValue("dPublishState");
/* 1075 */     boolean isStaging = publishState.equals("W");
/*      */ 
/* 1078 */     boolean isAdminAccess = service.checkAccess(binder, 8);
/* 1079 */     service.computeGroupPrivilege();
/* 1080 */     String user = binder.getActiveValue("dUser");
/*      */ 
/* 1082 */     boolean isCheckin = false;
/* 1083 */     boolean isCheckout = false;
/* 1084 */     boolean isReview = false;
/* 1085 */     boolean hasApproved = false;
/* 1086 */     if (("RW".indexOf(wfState) >= 0) || (isStaging))
/*      */     {
/* 1088 */       isReview = isUserInStep(user, stepData, service);
/* 1089 */       if (isReview)
/*      */       {
/* 1092 */         DataResultSet wfStates = (DataResultSet)binder.getResultSet("WorkflowStates");
/* 1093 */         if (wfStates != null)
/*      */         {
/* 1095 */           String dID = binder.getActiveValue("dID");
/* 1096 */           DataResultSet users = new DataResultSet();
/* 1097 */           users.copySimpleFiltered(wfStates, "dID", dID);
/* 1098 */           int userIndex = ResultSetUtils.getIndexMustExist(users, "dUserName");
/* 1099 */           Vector v = users.findRow(userIndex, user);
/* 1100 */           if (v != null)
/*      */           {
/* 1103 */             isReview = false;
/* 1104 */             hasApproved = true;
/*      */           }
/*      */         }
/*      */ 
/* 1108 */         if ((!hasApproved) && 
/* 1110 */           ("W".indexOf(wfState) >= 0))
/*      */         {
/* 1113 */           isCheckin = StringUtils.convertToBool(binder.getActiveValue("dIsCheckedOut"), false);
/* 1114 */           if (isCheckin)
/*      */           {
/* 1116 */             isReview = false;
/*      */           }
/*      */           else
/*      */           {
/* 1120 */             isCheckout = true;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/* 1126 */     else if ("E".indexOf(wfState) >= 0)
/*      */     {
/* 1129 */       if (StringUtils.convertToBool(binder.getActiveValue("dIsCheckedOut"), false))
/*      */       {
/* 1131 */         String checkoutUser = binder.getActiveValue("dCheckoutUser");
/* 1132 */         isCheckin = (isAdminAccess) || (checkoutUser.equalsIgnoreCase(user) == true);
/*      */       }
/*      */       else
/*      */       {
/* 1136 */         String stepType = stepData.getStepType();
/* 1137 */         if (WorkflowScriptUtils.isAutoContributorStep(stepType))
/*      */         {
/* 1139 */           String author = binder.getActiveAllowMissing("dDocAuthor");
/* 1140 */           isCheckout = (isAdminAccess) || (author.equalsIgnoreCase(user) == true);
/* 1141 */           if ((isCheckout) && ((
/* 1143 */             (service.isConditionVarTrue("AutoContributorAllowsReview")) || (SharedObjects.getEnvValueAsBoolean("AutoContributorAllowsReview", false)))))
/*      */           {
/* 1147 */             String title = binder.getActiveValue("dDocTitle");
/* 1148 */             String processingState = binder.getActiveValue("dProcessingState");
/* 1149 */             if ((title != null) && (title.length() > 0) && (processingState != null) && (processingState.length() > 0) && (!processingState.equals("C")))
/*      */             {
/* 1152 */               isReview = true;
/*      */             }
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 1159 */           isCheckout = isUserInStep(user, stepData, service);
/*      */         }
/*      */       }
/*      */     }
/* 1163 */     service.setConditionVar("AllowCheckin", isCheckin);
/* 1164 */     service.setConditionVar("AllowCheckout", isCheckout);
/* 1165 */     service.setConditionVar("AllowReview", isReview);
/* 1166 */     service.setConditionVar("HasApproved", hasApproved);
/* 1167 */     service.setConditionVar("IsStaging", isStaging);
/*      */   }
/*      */ 
/*      */   public static boolean isUserInStep(String user, WfStepData stepData, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1173 */     int userIndex = ResultSetUtils.getIndexMustExist(stepData, "dUsers");
/*      */ 
/* 1175 */     String userStr = stepData.getStringValue(userIndex);
/* 1176 */     Vector users = StringUtils.parseArray(userStr, '\t', '^');
/*      */ 
/* 1178 */     AliasData aliasData = (AliasData)SharedObjects.getTable(AliasData.m_tableName);
/* 1179 */     int num = users.size();
/* 1180 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1182 */       String value = (String)users.elementAt(i);
/*      */ 
/* 1184 */       int index = value.indexOf(58);
/* 1185 */       if (index >= 0)
/*      */       {
/* 1190 */         String type = value.substring(0, index);
/* 1191 */         String alias = value.substring(index + 1);
/*      */ 
/* 1193 */         Vector aliasUsers = new IdcVector();
/* 1194 */         createUserList(aliasUsers, alias, type, aliasData, false, false, false, null, cxt);
/*      */ 
/* 1196 */         int count = aliasUsers.size();
/* 1197 */         for (int j = 0; j < count; ++j)
/*      */         {
/* 1199 */           alias = (String)aliasUsers.elementAt(j);
/* 1200 */           if (user.equalsIgnoreCase(alias))
/*      */           {
/* 1202 */             return true;
/*      */           }
/*      */         }
/*      */       }
/* 1206 */       else if (value.equalsIgnoreCase(user))
/*      */       {
/* 1208 */         return true;
/*      */       }
/*      */     }
/*      */ 
/* 1212 */     return false;
/*      */   }
/*      */ 
/*      */   public static boolean isUserInStep(Workspace ws, DataBinder binder, String user, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1218 */     Vector users = computeStepUsers(ws, binder, false, cxt, false, false);
/*      */ 
/* 1220 */     int num = users.size();
/*      */ 
/* 1222 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1224 */       UserData userData = (UserData)users.elementAt(i);
/* 1225 */       if (userData == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1230 */       if (user.equalsIgnoreCase(userData.m_name))
/*      */       {
/* 1233 */         return true;
/*      */       }
/*      */     }
/* 1236 */     return false;
/*      */   }
/*      */ 
/*      */   public static void computeAllowedStepActions(DataBinder wfBinder, UserData userData, ExecutionContext cxt)
/*      */   {
/* 1242 */     String[] wfActions = ResourceContainerUtils.getDynamicFieldListResource("WorkflowActionList");
/*      */ 
/* 1246 */     int size = wfActions.length;
/* 1247 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1249 */       wfBinder.removeLocal(wfActions[i]);
/*      */     }
/* 1251 */     wfBinder.removeLocal("computedWfStepActions");
/*      */ 
/* 1253 */     boolean isCheckin = false;
/* 1254 */     boolean isCheckout = false;
/* 1255 */     boolean isReview = true;
/*      */ 
/* 1257 */     String docTitle = wfBinder.getAllowMissing("dDocTitle");
/* 1258 */     String stepType = wfBinder.getAllowMissing("dWfStepType");
/* 1259 */     if (stepType == null)
/*      */     {
/* 1261 */       stepType = "";
/*      */     }
/*      */ 
/* 1264 */     boolean isCheckedOut = StringUtils.convertToBool(wfBinder.getAllowMissing("dIsCheckedOut"), false);
/*      */ 
/* 1266 */     if (isCheckedOut)
/*      */     {
/* 1268 */       isReview = false;
/* 1269 */       String chkUser = wfBinder.getAllowMissing("dCheckoutUser");
/* 1270 */       String curUser = userData.m_name;
/* 1271 */       isCheckin = curUser.equals(chkUser);
/*      */     }
/* 1273 */     else if ((docTitle == null) || (docTitle.length() == 0))
/*      */     {
/* 1275 */       isReview = false;
/* 1276 */       isCheckout = true;
/*      */     }
/* 1278 */     else if (stepType.indexOf(":C:") > -1)
/*      */     {
/* 1280 */       isCheckout = true;
/*      */     }
/*      */ 
/* 1283 */     String actionState = wfBinder.getAllowMissing("wfQueueActionState");
/* 1284 */     if ((actionState != null) && (actionState.equals("APPROVE")))
/*      */     {
/* 1286 */       isReview = false;
/*      */     }
/*      */ 
/* 1289 */     boolean isAutoContribReview = SharedObjects.getEnvValueAsBoolean("AutoContributorAllowsReview", false);
/*      */ 
/* 1291 */     if ((isReview) && (stepType.indexOf(":R:") < 0) && (((!isAutoContribReview) || (stepType.indexOf(":CA:") < 0))))
/*      */     {
/* 1294 */       isReview = false;
/*      */     }
/*      */ 
/* 1297 */     String projectID = wfBinder.getAllowMissing("dProjectID");
/* 1298 */     boolean isStaging = (projectID != null) && (projectID.length() > 0);
/*      */     try
/*      */     {
/* 1302 */       PluginFilters.filter("workflowPrepareUpdateInQueue", null, wfBinder, cxt);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1306 */       String docName = wfBinder.getAllowMissing("dDocName");
/* 1307 */       Report.error("workflow", e, "csWfActionsError", new Object[] { docName });
/*      */     }
/*      */ 
/* 1311 */     wfBinder.putLocal("ShowWorkflowStepInfo", "1");
/* 1312 */     if (isReview)
/*      */     {
/* 1314 */       wfBinder.putLocal("AllowReview", "1");
/*      */     }
/* 1316 */     if (isCheckin)
/*      */     {
/* 1318 */       wfBinder.putLocal("AllowCheckin", "1");
/*      */     }
/* 1320 */     if (isCheckout)
/*      */     {
/* 1322 */       wfBinder.putLocal("AllowCheckout", "1");
/*      */     }
/* 1324 */     if (isStaging)
/*      */     {
/* 1326 */       wfBinder.putLocal("IsStaging", "1");
/*      */     }
/*      */ 
/* 1330 */     IdcStringBuilder strBuilder = new IdcStringBuilder();
/* 1331 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1333 */       String wfAction = wfActions[i];
/* 1334 */       String str = wfBinder.getLocal(wfAction);
/* 1335 */       if (strBuilder.m_length > 0)
/*      */       {
/* 1337 */         strBuilder.append(',');
/*      */       }
/* 1339 */       strBuilder.append(wfAction);
/* 1340 */       strBuilder.append('=');
/* 1341 */       if (str == null)
/*      */         continue;
/* 1343 */       strBuilder.append("1");
/*      */     }
/*      */ 
/* 1347 */     wfBinder.putLocal("computedWfStepActions", strBuilder.toString());
/*      */   }
/*      */ 
/*      */   public static String[] getTopicColumns(boolean isUpdate, boolean isCurrentUser)
/*      */   {
/* 1356 */     if (m_allTopicColumns == null)
/*      */     {
/* 1359 */       DataResultSet topicColumns = SharedObjects.getTable("WfTopicColumns");
/*      */ 
/* 1362 */       List allColumnsList = new ArrayList();
/* 1363 */       List updateColumnsList = new ArrayList();
/* 1364 */       List userUpdateColumnsList = new ArrayList();
/*      */       int colNameIndex;
/*      */       int vfIndex;
/*      */       try
/*      */       {
/* 1369 */         colNameIndex = ResultSetUtils.getIndexMustExist(topicColumns, "columnName");
/* 1370 */         vfIndex = ResultSetUtils.getIndexMustExist(topicColumns, "visibilityFlags");
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1374 */         Report.trace("workflow", null, e);
/* 1375 */         return null;
/*      */       }
/* 1377 */       for (topicColumns.first(); topicColumns.isRowPresent(); topicColumns.next())
/*      */       {
/* 1379 */         String columnName = topicColumns.getStringValue(colNameIndex);
/* 1380 */         String visibilityFlags = topicColumns.getStringValue(vfIndex);
/* 1381 */         allColumnsList.add(columnName);
/* 1382 */         if (visibilityFlags.indexOf("isUpdateOnly") == -1)
/*      */         {
/* 1384 */           updateColumnsList.add(columnName);
/*      */         }
/* 1386 */         if (visibilityFlags.indexOf("isUserUpdate") != -1)
/*      */           continue;
/* 1388 */         userUpdateColumnsList.add(columnName);
/*      */       }
/*      */ 
/* 1393 */       m_allTopicColumns = StringUtils.convertListToArray(allColumnsList);
/* 1394 */       m_updateColumns = StringUtils.convertListToArray(updateColumnsList);
/* 1395 */       m_userUpdateColumns = StringUtils.convertListToArray(userUpdateColumnsList);
/*      */ 
/* 1397 */       m_keepRejectEventsInQueue = SharedObjects.getEnvValueAsBoolean("WorkflowKeepRejectEventsInQueue", m_keepRejectEventsInQueue);
/*      */     }
/*      */ 
/* 1400 */     if ((isUpdate) && (!isCurrentUser))
/*      */     {
/* 1402 */       return m_updateColumns;
/*      */     }
/* 1404 */     if (isCurrentUser)
/*      */     {
/* 1406 */       return m_userUpdateColumns;
/*      */     }
/* 1408 */     return m_allTopicColumns;
/*      */   }
/*      */ 
/*      */   public static String getTopicColumnStr(String[] columns)
/*      */   {
/* 1414 */     String str = "";
/* 1415 */     int len = columns.length;
/* 1416 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1418 */       if (str.length() > 0)
/*      */       {
/* 1420 */         str = str + ",";
/*      */       }
/* 1422 */       str = str + columns[i];
/*      */     }
/*      */ 
/* 1425 */     return str;
/*      */   }
/*      */ 
/*      */   public static void updateTopicFields(String[][] columns)
/*      */   {
/* 1430 */     for (int i = 0; i < columns.length; ++i)
/*      */     {
/* 1432 */       FieldInfo info = new FieldInfo();
/* 1433 */       info.m_name = columns[i][0];
/* 1434 */       info.m_type = QueryUtils.convertInfoStringToType(columns[i][1]);
/* 1435 */       info.m_maxLen = NumberUtils.parseInteger(columns[i][2], 20);
/* 1436 */       info.m_isFixedLen = true;
/* 1437 */       m_topicColumnList.add(info);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Properties validateTopicData(DataBinder binder)
/*      */     throws DataException
/*      */   {
/* 1444 */     getTopicColumns(false, false);
/*      */ 
/* 1446 */     Properties map = new Properties();
/* 1447 */     int size = m_topicColumnList.size();
/* 1448 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1450 */       FieldInfo fi = (FieldInfo)m_topicColumnList.get(i);
/* 1451 */       String val = binder.getAllowMissing(fi.m_name);
/* 1452 */       if (val == null)
/*      */       {
/* 1454 */         val = "";
/*      */       }
/* 1456 */       else if (val.length() > fi.m_maxLen)
/*      */       {
/* 1459 */         val = val.substring(0, fi.m_maxLen);
/*      */       }
/* 1461 */       map.put(fi.m_name, val);
/*      */     }
/* 1463 */     return map;
/*      */   }
/*      */ 
/*      */   public static void prepareRemoveFromInQueue(String docName, WfCompanionData wfCompanionData, Workspace ws, ExecutionContext cxt, Hashtable userMap, Hashtable workMap)
/*      */   {
/* 1470 */     String str = wfCompanionData.m_data.getLocal("wfUserQueue");
/* 1471 */     Vector oldUsers = StringUtils.parseArray(str, ',', '^');
/*      */ 
/* 1473 */     prepareRemoveFromInQueueEx(docName, oldUsers, ws, cxt, userMap, workMap);
/*      */   }
/*      */ 
/*      */   public static void prepareRemoveFromInQueueEx(String docName, Vector oldUsers, Workspace ws, ExecutionContext cxt, Hashtable userMap, Hashtable workMap)
/*      */   {
/* 1481 */     DataBinder binder = new DataBinder();
/* 1482 */     binder.putLocal("WorkflowInQueue", docName);
/*      */ 
/* 1484 */     int num = oldUsers.size();
/* 1485 */     if (num <= 0)
/*      */       return;
/* 1487 */     boolean useDb = SharedObjects.getEnvValueAsBoolean("UseDatabaseWfInQueue", false);
/* 1488 */     if (useDb)
/*      */     {
/* 1490 */       deleteFromDbQueue(docName, oldUsers, ws);
/*      */     }
/*      */     else
/*      */     {
/* 1494 */       DataBinder delBinder = new DataBinder();
/* 1495 */       ProfileUtils.addTopicEdit("wf_in_queue", "deleteRows", "WorkflowInQueue", binder, delBinder);
/*      */ 
/* 1497 */       delBinder.putLocal("dDocName", docName);
/*      */ 
/* 1499 */       for (int i = 0; i < num; ++i)
/*      */       {
/* 1501 */         String name = (String)oldUsers.elementAt(i);
/*      */         try
/*      */         {
/* 1504 */           UserData userData = UserStorage.retrieveUserDatabaseProfileData(name, ws, cxt);
/*      */ 
/* 1506 */           addToUserWork(userData, delBinder, userMap, workMap);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 1510 */           String errMsg = LocaleUtils.encodeMessage("csWfUserTopicError", null, name);
/* 1511 */           Report.error(null, errMsg, e);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void deleteFromDbQueue(String docName, List users, Workspace ws)
/*      */   {
/* 1520 */     Map map = new HashMap();
/* 1521 */     map.put("dDocName", docName);
/* 1522 */     MapParameters params = new MapParameters(map);
/*      */ 
/* 1524 */     boolean isSwitching = SharedObjects.getEnvValueAsBoolean("WfSwitchingQueueToDatabase", false);
/*      */ 
/* 1526 */     if (isSwitching)
/*      */     {
/* 1528 */       String currentTs = LocaleUtils.formatODBC(new Date());
/* 1529 */       map.put("dWfQueueDeleteTs", currentTs);
/*      */     }
/* 1531 */     int num = users.size();
/* 1532 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1534 */       String name = (String)users.get(i);
/*      */       try
/*      */       {
/* 1537 */         map.put("dUser", name);
/* 1538 */         long result = ws.execute("DwfInQueue", params);
/* 1539 */         Report.trace("workflow", "WorkflowUtils.deleteFromDbQueue: delete for user=" + name + " dDocName= " + docName + " with result=" + result, null);
/*      */ 
/* 1544 */         if (isSwitching)
/*      */         {
/* 1546 */           ResultSet rset = ws.createResultSet("QdeletedWfInQueue", params);
/* 1547 */           if (rset.isEmpty())
/*      */           {
/* 1549 */             ws.execute("IdeletedWfInQueue", params);
/*      */           }
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1555 */         Report.trace("workflow", " WorkflowUtils.deleteFromDbQueue: Unable to delete the workfow in queue entry for user " + name + " and dDocName " + docName, e);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void prepareUpdateInQueue(String docName, DataBinder inBinder, Vector users, Hashtable userMap, Hashtable workMap, boolean isUpdate, boolean isUpdateUser)
/*      */   {
/* 1565 */     if ((users == null) || (users.size() == 0))
/*      */     {
/* 1567 */       return;
/*      */     }
/*      */ 
/* 1571 */     DataBinder binder = new DataBinder();
/* 1572 */     String[] topicColumns = getTopicColumns(isUpdate, isUpdateUser);
/*      */ 
/* 1574 */     ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/* 1575 */     Object[] params = { topicColumns, new Boolean(isUpdate), new Boolean(isUpdateUser), users, userMap, workMap, docName };
/*      */ 
/* 1577 */     cxt.setCachedObject("workflowPrepareUpdateInQueueParams", params);
/*      */     try
/*      */     {
/* 1581 */       if (PluginFilters.filter("workflowPrepareUpdateInQueue", null, inBinder, cxt) == -1)
/*      */       {
/* 1584 */         return;
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1589 */       Report.error(null, "!csWorkflowPrepareUpdateInQueueFilterError", e);
/* 1590 */       return;
/*      */     }
/*      */ 
/* 1594 */     String currentTs = LocaleUtils.formatODBC(new Date());
/* 1595 */     if (!isUpdate)
/*      */     {
/* 1597 */       String actionState = inBinder.getLocal("wfAction");
/* 1598 */       if ((actionState == null) || (!m_keepRejectEventsInQueue) || (actionState.equals("CHECKIN")) || (actionState.equals("APPROVE")))
/*      */       {
/* 1601 */         actionState = "";
/*      */       }
/* 1603 */       inBinder.putLocal("wfQueueActionState", actionState);
/* 1604 */       inBinder.putLocal("wfQueueEnterTs", currentTs);
/*      */     }
/* 1606 */     if ((!isUpdate) || (isUpdateUser))
/*      */     {
/* 1608 */       inBinder.putLocal("wfQueueLastActionTs", currentTs);
/*      */     }
/*      */ 
/* 1611 */     boolean useDb = SharedObjects.getEnvValueAsBoolean("UseDatabaseWfInQueue", false);
/* 1612 */     if (useDb)
/*      */     {
/* 1614 */       updateDbQueue(params, binder, inBinder);
/*      */     }
/*      */     else
/*      */     {
/* 1618 */       preparePneUpdate(params, binder, inBinder);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void preparePneUpdate(Object[] params, DataBinder binder, DataBinder inBinder)
/*      */   {
/* 1624 */     String[] topicColumns = (String[])(String[])params[0];
/* 1625 */     boolean isUpdate = ((Boolean)params[1]).booleanValue();
/* 1626 */     Vector users = (Vector)params[3];
/* 1627 */     Hashtable userMap = (Hashtable)params[4];
/* 1628 */     Hashtable workMap = (Hashtable)params[5];
/* 1629 */     String docName = (String)params[6];
/*      */ 
/* 1631 */     String topicColumnsStr = getTopicColumnStr(topicColumns);
/* 1632 */     binder.putLocal("WorkflowInQueue:columns", topicColumnsStr);
/* 1633 */     if (isUpdate)
/*      */     {
/* 1635 */       binder.putLocal("WorkflowInQueue:updateOnly", "1");
/*      */     }
/* 1637 */     int num = topicColumns.length;
/* 1638 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1640 */       String key = topicColumns[i];
/* 1641 */       String val = inBinder.getAllowMissing(key);
/* 1642 */       if (val == null)
/*      */       {
/* 1644 */         val = "";
/*      */       }
/* 1646 */       binder.putLocal(key, val);
/*      */     }
/*      */ 
/* 1650 */     DataBinder addBinder = new DataBinder();
/* 1651 */     int queueSize = SharedObjects.getEnvironmentInt("WorkflowInQueueSize", 1000);
/* 1652 */     binder.putLocal("WorkflowInQueue:mru", "" + queueSize);
/* 1653 */     ProfileUtils.addTopicEdit("wf_in_queue", "addMruRow", "WorkflowInQueue", binder, addBinder);
/* 1654 */     addBinder.putLocal("dDocName", docName);
/*      */ 
/* 1658 */     int len = users.size();
/* 1659 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1661 */       UserData userData = (UserData)users.elementAt(i);
/* 1662 */       addToUserWork(userData, addBinder, userMap, workMap);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void updateDbQueue(Object[] params, DataBinder binder, DataBinder inBinder)
/*      */   {
/* 1668 */     List users = (List)params[3];
/* 1669 */     String docName = (String)params[6];
/*      */ 
/* 1671 */     int size = m_topicColumnList.size();
/* 1672 */     for (int count = 0; count < size; ++count)
/*      */     {
/* 1674 */       FieldInfo info = (FieldInfo)m_topicColumnList.get(count);
/* 1675 */       String key = info.m_name;
/* 1676 */       String val = inBinder.getAllowMissing(key);
/* 1677 */       if (val == null)
/*      */       {
/* 1679 */         String wfKey = key.substring(1);
/* 1680 */         val = inBinder.getAllowMissing(wfKey);
/*      */       }
/* 1682 */       if (val != null)
/*      */       {
/* 1684 */         if (val.length() > info.m_maxLen)
/*      */         {
/* 1687 */           val = val.substring(0, info.m_maxLen);
/*      */         }
/*      */ 
/*      */       }
/*      */       else {
/* 1692 */         val = "";
/*      */       }
/* 1694 */       binder.putLocal(key, val);
/*      */     }
/*      */ 
/* 1697 */     Provider provider = Providers.getProvider("SystemDatabase");
/* 1698 */     Workspace ws = (Workspace)provider.getProvider();
/*      */ 
/* 1700 */     size = users.size();
/* 1701 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1703 */       String user = null;
/* 1704 */       Object obj = users.get(i);
/* 1705 */       if (obj instanceof UserData)
/*      */       {
/* 1707 */         UserData userData = (UserData)obj;
/* 1708 */         user = userData.m_name;
/*      */       }
/* 1710 */       else if (obj instanceof String)
/*      */       {
/* 1712 */         user = (String)users.get(i);
/*      */       }
/* 1714 */       binder.putLocal("dUser", user);
/*      */ 
/* 1716 */       String query = "UwfInQueue";
/*      */       try
/*      */       {
/* 1720 */         ResultSet rset = ws.createResultSet("QwfInQueue", binder);
/* 1721 */         if (rset.isEmpty())
/*      */         {
/* 1723 */           query = "IwfInQueue";
/*      */         }
/*      */         else
/*      */         {
/* 1727 */           DataResultSet drset = new DataResultSet();
/* 1728 */           drset.copy(rset);
/* 1729 */           binder.addResultSet("WfQueue", drset);
/*      */         }
/* 1731 */         long result = ws.execute(query, binder);
/* 1732 */         Report.trace("workflow", "WorkflowUtils.updateDbQueue: Updated the queue with query = " + query + " for user " + user + " and dDocName = " + docName + " and result= " + result, null);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1738 */         Report.trace("workflow", "WorkflowUtils.updateDbQueue: Unable to update the workflow in queue for user " + user + " and dDocName= " + docName + " with query= " + query, e);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addToUserWork(UserData userData, DataBinder workBinder, Hashtable userMap, Hashtable workMap)
/*      */   {
/* 1748 */     String name = userData.m_name;
/* 1749 */     userMap.put(name, userData);
/*      */ 
/* 1751 */     DataResultSet newSet = (DataResultSet)workBinder.getResultSet("UserTopicEdits");
/*      */ 
/* 1753 */     DataBinder binder = (DataBinder)workMap.get(name);
/* 1754 */     if (binder == null)
/*      */     {
/* 1756 */       binder = new DataBinder();
/* 1757 */       DataResultSet rset = new DataResultSet();
/* 1758 */       rset.copy(newSet);
/* 1759 */       binder.addResultSet("UserTopicEdits", rset);
/*      */ 
/* 1761 */       workMap.put(name, binder);
/*      */     }
/*      */     else
/*      */     {
/* 1765 */       DataResultSet drset = (DataResultSet)binder.getResultSet("UserTopicEdits");
/*      */       try
/*      */       {
/* 1768 */         drset.merge(null, newSet, false);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1772 */         String errMsg = LocaleUtils.encodeMessage("csWfAddUserWork", null, name);
/* 1773 */         Report.error(null, errMsg, e);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void updateWorkflowTopic(Hashtable userMap, Hashtable workMap, Workspace ws, ExecutionContext cxt)
/*      */   {
/* 1782 */     UserProfileManager upm = null;
/* 1783 */     for (Enumeration en = userMap.keys(); en.hasMoreElements(); )
/*      */     {
/* 1785 */       String lookupName = (String)en.nextElement();
/*      */ 
/* 1787 */       UserData userData = (UserData)userMap.get(lookupName);
/* 1788 */       if (userData == null) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1792 */       DataBinder workBinder = (DataBinder)workMap.get(lookupName);
/* 1793 */       if (workBinder == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/* 1800 */         if (upm == null)
/*      */         {
/* 1802 */           upm = new UserProfileManager(userData, ws, cxt);
/* 1803 */           upm.init();
/*      */         }
/*      */         else
/*      */         {
/* 1807 */           upm.initForUser(userData);
/*      */         }
/* 1809 */         upm.updateTopics(workBinder);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1813 */         String errMsg = LocaleUtils.encodeMessage("csWfUpdateUserTopic", null, userData.m_name);
/* 1814 */         Report.error(null, errMsg, e);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static DataResultSet getOrCreateWorkflowActionHistory(WfCompanionData wfCompanionData)
/*      */   {
/* 1821 */     DataResultSet drset = (DataResultSet)wfCompanionData.m_data.getResultSet("WorkflowActionHistory");
/* 1822 */     if (drset == null)
/*      */     {
/* 1824 */       Vector infos = ResultSetUtils.createFieldInfo(WORKFLOWACTIONHISTORY_COLUMNS, 0);
/*      */ 
/* 1827 */       FieldInfo fi = (FieldInfo)infos.elementAt(2);
/* 1828 */       fi.m_type = 5;
/* 1829 */       drset = new DataResultSet();
/* 1830 */       drset.mergeFieldsWithFlags(infos, 0);
/*      */ 
/* 1832 */       wfCompanionData.m_data.addResultSet("WorkflowActionHistory", drset);
/*      */     }
/* 1834 */     return drset;
/*      */   }
/*      */ 
/*      */   public static DataBinder createWorkflowActionHistoryBinder(DataResultSet drset, DataBinder binder, ExecutionContext context)
/*      */   {
/* 1842 */     DataBinder workBinder = new DataBinder();
/* 1843 */     workBinder.putLocal("wfActionTs", LocaleUtils.formatODBC(new Date()));
/* 1844 */     workBinder.putLocal("wfMessage", "");
/*      */ 
/* 1846 */     CollectionUtils.mergeMaps(binder.getLocalData(), workBinder.getLocalData(), WORKFLOWACTIONHISTORY_COLUMNS);
/*      */ 
/* 1849 */     if (null != context)
/*      */     {
/* 1851 */       UserData userData = (UserData)context.getCachedObject("UserData");
/* 1852 */       workBinder.putLocal("wfUsers", userData.m_name);
/*      */     }
/*      */ 
/* 1856 */     workBinder.putLocal("dDocName", binder.getLocal("dDocName"));
/*      */ 
/* 1858 */     return workBinder;
/*      */   }
/*      */ 
/*      */   public static void updateWorkflowActionHistory(DataResultSet drset, DataBinder workBinder)
/*      */   {
/*      */     try
/*      */     {
/* 1866 */       Vector row = drset.createRow(workBinder);
/* 1867 */       drset.addRow(row);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1872 */       String action = workBinder.getLocal("wfAction");
/* 1873 */       String docName = workBinder.getLocal("dDocName");
/* 1874 */       Report.info(null, e, "csWfUpdateHistoryError", new Object[] { docName, action });
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void updateWorkflowHistory(WfCompanionData wfCompanionData, DataBinder binder, ExecutionContext context)
/*      */   {
/* 1883 */     DataResultSet drset = getOrCreateWorkflowActionHistory(wfCompanionData);
/* 1884 */     DataBinder workBinder = createWorkflowActionHistoryBinder(drset, binder, context);
/* 1885 */     updateWorkflowActionHistory(drset, workBinder);
/*      */   }
/*      */ 
/*      */   public static WfCompanionData retrieveCompanionData(DataBinder binder, ExecutionContext context, Workspace workspace)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1893 */     WfCompanionData wfCompanionData = null;
/* 1894 */     Object obj = context.getCachedObject("WorkflowCompanionData");
/* 1895 */     if ((obj != null) && (obj instanceof WfCompanionData))
/*      */     {
/* 1897 */       wfCompanionData = (WfCompanionData)obj;
/*      */     }
/*      */     else
/*      */     {
/* 1902 */       String docName = binder.get("dDocName");
/* 1903 */       String subDir = binder.get("dWfDirectory");
/* 1904 */       wfCompanionData = loadCompanionData(docName, subDir, binder, context, workspace);
/*      */     }
/*      */ 
/* 1907 */     return wfCompanionData;
/*      */   }
/*      */ 
/*      */   public static WfCompanionData loadCompanionData(String docName, String subDir, DataBinder binder, ExecutionContext context, Workspace workspace)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1914 */     WfCompanionData wfCompanionData = WfCompanionManager.getOrCreateCompanionData(docName, subDir, workspace, binder);
/*      */ 
/* 1918 */     context.setCachedObject("WorkflowCompanionData", wfCompanionData);
/*      */ 
/* 1920 */     return wfCompanionData;
/*      */   }
/*      */ 
/*      */   public static void makeCompatibleWithLatestVersion(DataBinder data, String fileType)
/*      */   {
/* 1925 */     String versionProps = data.getLocal("versionProperties");
/* 1926 */     if ((versionProps != null) && (StringUtils.match(versionProps, "*coloninkeypath*", false)))
/*      */       return;
/* 1928 */     Map l = data.getLocalData();
/* 1929 */     Map n = null;
/* 1930 */     Iterator i = l.keySet().iterator();
/* 1931 */     while (i.hasNext())
/*      */     {
/* 1933 */       String key = (String)i.next();
/* 1934 */       if ((key.indexOf(".") > 0) && 
/* 1936 */         (n == null))
/*      */       {
/* 1938 */         n = data.cloneMap(l);
/* 1939 */         String newKey = key.replace('.', ':');
/* 1940 */         n.put(newKey, l.get(key));
/* 1941 */         n.remove(key);
/*      */       }
/*      */     }
/*      */ 
/* 1945 */     if (n != null)
/*      */     {
/* 1947 */       data.setLocalData((Properties)n);
/*      */     }
/* 1949 */     if ((versionProps != null) && (versionProps.length() > 0))
/*      */     {
/* 1951 */       versionProps = versionProps + ",coloninkeypath";
/*      */     }
/*      */     else
/*      */     {
/* 1955 */       versionProps = "coloninkeypath";
/*      */     }
/* 1957 */     data.putLocal("versionProperties", versionProps);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1963 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99974 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.workflow.WorkflowUtils
 * JD-Core Version:    0.5.4
 */