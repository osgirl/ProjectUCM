/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.ServiceManager;
/*     */ import intradoc.server.datastoredesign.DataDesignConfigInfo;
/*     */ import intradoc.server.datastoredesign.DataDesignInstall;
/*     */ import intradoc.server.datastoredesign.DataDesignInstallUtils;
/*     */ import intradoc.shared.MetaFieldUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.workflow.WfStepData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CompInstallUtils
/*     */ {
/*     */   public static final String COMP_NAME = "ComponentName";
/*     */   public static final String RE_FEATURES = "requiredFeatures";
/*     */   public static final String INSTALL_ID = "installID";
/*     */   public static final String ADD_COMPS = "additionalComponents";
/*     */   public static final String LOCATION = "location";
/*     */   public static final String HAD_INST_STR = "hasInstallStrings";
/*     */   public static final String HAS_PREF_DATA = "hasPreferenceData";
/*     */   public static final String PREVENT_DOWNGRADE = "preventAdditionalComponentDowngrade";
/*     */   public static final String FE_EXTS = "featureExtensions";
/*     */   public static final String TRACES = "defaultTracing";
/*     */   public static final String DISABLE_COMPS = "componentsToDisable";
/*     */   public static final int DATA_DESIGN_TABLE = 1;
/*     */   public static final int DATA_DESIGN_DOCMETA = 2;
/*     */   public static final int DATA_DESIGN_PROFILE = 4;
/*     */   public static final int DATA_DESIGN_PROFILE_RULE = 8;
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeAbsoluteComponentLocation(String name)
/*     */     throws ServiceException, DataException
/*     */   {
/*  83 */     return ComponentLocationUtils.computeAbsoluteComponentDirectory(name);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean hasDocMetaDef(String metaDefName)
/*     */   {
/*  92 */     Report.deprecatedUsage("use MetaFieldUtils.hasDocMetaDef()");
/*  93 */     return MetaFieldUtils.hasDocMetaDef(metaDefName);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void addMetaData(Workspace ws, String name, String caption, String type, String isRequired, String isEnabled, String isSearchable, String isOptionList, String defaultValue, String optionListKey, String optionListType, String order)
/*     */     throws ServiceException, DataException
/*     */   {
/* 106 */     Report.deprecatedUsage("use MetaFieldUtils.updateMetaData()");
/* 107 */     MetaFieldUtils.updateMetaData(ws, null, name, caption, type, isRequired, isEnabled, isSearchable, isOptionList, defaultValue, optionListKey, optionListType, "", order, true);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void hideDocMetaData(Workspace ws, String name)
/*     */     throws ServiceException, DataException
/*     */   {
/* 117 */     Report.deprecatedUsage("use MetaFieldUtils.hideDocMetaData()");
/* 118 */     MetaFieldUtils.hideMetaData(ws, null, name);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void updateMetaData(Workspace ws, String name, String caption, String type, String isRequired, String isEnabled, String isSearchable, String isOptionList, String defaultValue, String optionListKey, String optionListType, String order)
/*     */     throws ServiceException, DataException
/*     */   {
/* 131 */     Report.deprecatedUsage("use MetaFieldUtils.updateMetaData()");
/* 132 */     MetaFieldUtils.updateMetaData(ws, null, name, caption, type, isRequired, isEnabled, isSearchable, isOptionList, defaultValue, optionListKey, optionListType, "", order, false);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void addOrUpdateMetaData(Workspace ws, String name, String caption, String type, String isRequired, String isEnabled, String isSearchable, String isOptionList, String defaultValue, String optionListKey, String optionListType, String order, boolean isNew)
/*     */     throws ServiceException, DataException
/*     */   {
/* 146 */     Report.deprecatedUsage("use MetaFieldUtils.addOrUpdateMetaData()");
/* 147 */     MetaFieldUtils.updateMetaData(ws, null, name, caption, type, isRequired, isEnabled, isSearchable, isOptionList, defaultValue, optionListKey, optionListType, "", order, isNew);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void deleteMetaData(Workspace ws, String name)
/*     */     throws ServiceException, DataException
/*     */   {
/* 158 */     Report.deprecatedUsage("use MetaFieldUtils.deleteMetaData()");
/* 159 */     MetaFieldUtils.deleteMetaData(ws, null, name);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void setOptionList(Workspace ws, String optionListKey, String optionListString)
/*     */     throws ServiceException, DataException
/*     */   {
/* 169 */     Report.deprecatedUsage("use MetaFieldUtils.setOptionList()");
/* 170 */     MetaFieldUtils.setOptionList(ws, null, optionListKey, optionListString);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getOptionListKey(String key)
/*     */   {
/* 179 */     Report.deprecatedUsage("use MetaFieldUtils.getOptionListKey()");
/* 180 */     return MetaFieldUtils.getOptionListKey(key);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void addUserInfoField(Workspace ws, String name)
/*     */     throws ServiceException
/*     */   {
/* 190 */     Report.deprecatedUsage("use MetaFieldUtils.addUserInfoField()");
/*     */     try
/*     */     {
/* 193 */       MetaFieldUtils.addUserInfoField(ws, null, name);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 197 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void deleteUserInfoField(Workspace ws, String name)
/*     */     throws ServiceException, DataException
/*     */   {
/* 207 */     Report.deprecatedUsage("use MetaFieldUtils.deleteUserInfoField()");
/* 208 */     MetaFieldUtils.deleteUserInfoField(ws, null, name);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void addToUserOptionList(Workspace ws, String optionListName, String item)
/*     */     throws ServiceException, DataException
/*     */   {
/* 218 */     Report.deprecatedUsage("use MetaFieldUtils.addToUserOptionList()");
/* 219 */     MetaFieldUtils.addToUserOptionList(ws, null, optionListName, item);
/*     */   }
/*     */ 
/*     */   public static DataBinder createWorkflow(Workspace ws, String name, String description)
/*     */     throws ServiceException, DataException
/*     */   {
/* 229 */     DataBinder wf = new DataBinder();
/* 230 */     wf.putLocal("dWfName", name);
/* 231 */     wf.putLocal("dWfDescription", description);
/* 232 */     wf.putLocal("dSecurityGroup", "Projects");
/* 233 */     wf.putLocal("dWfType", "Criteria");
/* 234 */     wf.putLocal("HasTemplate", "0");
/* 235 */     wf.putLocal("dWfCriteriaName", "xSelectedWorkflow");
/* 236 */     wf.putLocal("dWfCriteriaOperator", "matches");
/* 237 */     wf.putLocal("dWfCriteriaValue", name);
/* 238 */     wf.putLocal("dIsCollaboration", "1");
/*     */ 
/* 240 */     executeService(ws, "ADD_WORKFLOW", wf);
/*     */ 
/* 242 */     return wf;
/*     */   }
/*     */ 
/*     */   public static DataBinder deleteWorkflow(Workspace ws, String name)
/*     */     throws ServiceException, DataException
/*     */   {
/* 251 */     DataBinder wf = new DataBinder();
/* 252 */     wf.putLocal("dWfName", name);
/* 253 */     disableWorkflow(ws, name);
/* 254 */     executeService(ws, "DELETE_WORKFLOW", wf);
/*     */ 
/* 256 */     return wf;
/*     */   }
/*     */ 
/*     */   public static void disableWorkflow(Workspace ws, String workflowName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 265 */     DataBinder eb = new DataBinder();
/* 266 */     eb.putLocal("dWfName", workflowName);
/* 267 */     executeService(ws, "CRITERIAWORKFLOW_DISABLE", eb);
/*     */   }
/*     */ 
/*     */   public static void addWorkflowStep(Workspace ws, String workflowName, String name, String description, String type, String token)
/*     */     throws ServiceException, DataException
/*     */   {
/* 281 */     addWorkflowStep(ws, workflowName, name, description, type, token, false);
/*     */   }
/*     */ 
/*     */   public static void addWorkflowStep(Workspace ws, String workflowName, String name, String description, String type, String token, boolean first)
/*     */     throws ServiceException, DataException
/*     */   {
/* 297 */     DataBinder wfs = new DataBinder();
/* 298 */     wfs.putLocal("dWfName", workflowName);
/* 299 */     wfs.putLocal("dWfStepDescription", description);
/* 300 */     wfs.putLocal("dWfStepName", name);
/* 301 */     wfs.putLocal("dWfStepType", type);
/* 302 */     wfs.putLocal("dWfStepIsAll", "0");
/* 303 */     wfs.putLocal("dWfStepWeight", "1");
/*     */ 
/* 305 */     String entryScript = "";
/* 306 */     if (first)
/*     */     {
/* 308 */       entryScript = "<$exec inc(\"prj_token_init_script\")$>";
/* 309 */       wfs.putLocal("entry_wfIsCustomScript", "1");
/* 310 */       wfs.putLocal("entry_wfCustomScript", "<$exec inc(\"prj_token_init_script\")$>");
/*     */     }
/* 312 */     wfs.putLocal("wfEntryScript", entryScript);
/* 313 */     wfs.putLocal("wfExitScript", "");
/* 314 */     wfs.putLocal("wfUpdateScript", "");
/*     */ 
/* 317 */     String aliasesString = getAliasesString(token, "token");
/* 318 */     wfs.putLocal("dAliases", aliasesString);
/* 319 */     executeService(ws, "ADD_WORKFLOWSTEP", wfs);
/*     */   }
/*     */ 
/*     */   public static String getAliasesString(String alias, String type)
/*     */     throws DataException
/*     */   {
/* 328 */     DataResultSet tokenRS = new DataResultSet(new String[] { "dAlias", "dAliasType" });
/* 329 */     Vector rsRow = new IdcVector();
/* 330 */     rsRow.addElement(alias);
/* 331 */     rsRow.addElement(type);
/* 332 */     tokenRS.addRow(rsRow);
/*     */ 
/* 334 */     String aliasesString = null;
/* 335 */     WfStepData stepData = new WfStepData();
/* 336 */     aliasesString = stepData.getAliasesString(tokenRS);
/*     */ 
/* 338 */     return aliasesString;
/*     */   }
/*     */ 
/*     */   public static void enableWorkflow(Workspace ws, String workflowName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 347 */     DataBinder eb = new DataBinder();
/* 348 */     eb.putLocal("dWfName", workflowName);
/* 349 */     executeService(ws, "CRITERIAWORKFLOW_ENABLE", eb);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static ResultSet getUserMetaDefs(Workspace ws)
/*     */     throws ServiceException
/*     */   {
/* 358 */     Report.deprecatedUsage("use MetaFieldUtils.getUserMetaDefs()");
/*     */     try
/*     */     {
/* 361 */       return MetaFieldUtils.getUserMetaDefs(ws, null);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 365 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected ResultSet getCriteriaWorkflows(Workspace ws, String secGroupName)
/*     */     throws ServiceException
/*     */   {
/* 375 */     DataBinder binder = new DataBinder();
/* 376 */     binder.putLocal("dSecurityGroup", secGroupName);
/* 377 */     executeService(ws, "GET_CRITERIA_WORKFLOWS_FOR_GROUP", binder);
/* 378 */     return binder.getResultSet("WorkflowsForGroup");
/*     */   }
/*     */ 
/*     */   public static void addIndex(Workspace ws, String table, String column)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 390 */       String[] cols = new String[1];
/* 391 */       cols[0] = column;
/* 392 */       ws.addIndex(table, cols);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 396 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void dropIndex(Workspace ws, String table, String metaDataName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 408 */     ws.executeSQL("drop index " + table + "." + table + "_" + metaDataName);
/*     */   }
/*     */ 
/*     */   public static void setFileProperty(Properties p, String filePathName)
/*     */     throws ServiceException, IOException
/*     */   {
/* 417 */     SystemPropertiesEditor ed = new SystemPropertiesEditor();
/*     */ 
/* 419 */     ed.initIdc();
/* 420 */     ed.initConfig();
/* 421 */     Properties props = new Properties();
/* 422 */     Vector vector = new IdcVector();
/* 423 */     Vector extra = new IdcVector();
/* 424 */     SystemPropertiesEditor.readFile(props, vector, extra, filePathName, null);
/*     */ 
/* 428 */     extra = new IdcVector();
/*     */ 
/* 433 */     Enumeration e = p.keys();
/* 434 */     while (e.hasMoreElements())
/*     */     {
/* 436 */       String key = (String)e.nextElement();
/* 437 */       String v = (String)p.get(key);
/* 438 */       props.put(key, v);
/*     */     }
/*     */ 
/* 442 */     e = props.keys();
/* 443 */     while (e.hasMoreElements())
/*     */     {
/* 445 */       String key = (String)e.nextElement();
/* 446 */       vector.addElement(key);
/*     */     }
/*     */ 
/* 449 */     SystemPropertiesEditor.writeFile(props, vector, extra, FileUtilsCfgBuilder.getCfgOutputStream(filePathName, null), null);
/*     */   }
/*     */ 
/*     */   public static void setConfigProperty(Properties p, String configFilePath)
/*     */     throws ServiceException
/*     */   {
/* 460 */     SystemPropertiesEditor ed = null;
/* 461 */     if (configFilePath == null)
/*     */     {
/* 463 */       ed = new SystemPropertiesEditor();
/*     */     }
/*     */     else
/*     */     {
/* 467 */       ed = new SystemPropertiesEditor(configFilePath);
/*     */     }
/*     */ 
/* 470 */     ed.initIdc();
/*     */ 
/* 472 */     Enumeration k = p.keys();
/* 473 */     String klist = "";
/* 474 */     while (k.hasMoreElements())
/*     */     {
/* 476 */       String sep = "";
/* 477 */       sep = (klist.equals("")) ? "" : ",";
/* 478 */       klist = klist + sep + k.nextElement();
/*     */     }
/* 480 */     ed.addKeys(null, klist);
/* 481 */     ed.initConfig();
/* 482 */     ed.mergePropertyValuesEx(null, p, false);
/* 483 */     ed.saveConfig();
/*     */   }
/*     */ 
/*     */   public static void updateConfigProperty(Properties p)
/*     */     throws ServiceException
/*     */   {
/* 490 */     SystemPropertiesEditor ed = new SystemPropertiesEditor();
/* 491 */     ed.initIdc();
/* 492 */     ed.initConfig();
/* 493 */     ed.mergePropertyValuesEx(null, p, false);
/* 494 */     ed.saveConfig();
/*     */   }
/*     */ 
/*     */   public static void removeConfigProperties(String[] configPropertyKeys)
/*     */     throws ServiceException
/*     */   {
/* 501 */     SystemPropertiesEditor ed = new SystemPropertiesEditor();
/* 502 */     ed.initIdc();
/* 503 */     ed.initConfig();
/*     */ 
/* 505 */     ed.removePropertyValues(null, configPropertyKeys);
/* 506 */     ed.saveConfig();
/*     */   }
/*     */ 
/*     */   public static void executeService(Workspace ws, String action, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 518 */       binder.putLocal("IdcService", action);
/* 519 */       binder.setEnvironmentValue("REMOTE_USER", "sysadmin");
/* 520 */       ServiceManager smg = new ServiceManager();
/* 521 */       smg.init(binder, ws);
/* 522 */       smg.processCommand();
/* 523 */       smg.clear();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 527 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Properties loadComponentInstallOrConfigData(String compName, String installID, boolean isInstallData)
/*     */     throws ServiceException, DataException
/*     */   {
/* 538 */     int type = (isInstallData) ? 3 : 2;
/* 539 */     return ComponentPreferenceData.getComponentData(compName, installID, type);
/*     */   }
/*     */ 
/*     */   public static String getInstallConfPath(String installID, String compName)
/*     */   {
/* 548 */     if ((installID == null) || (installID.length() == 0))
/*     */     {
/* 550 */       installID = compName;
/*     */     }
/* 552 */     return FileUtils.directorySlashes(DirectoryLocator.getAppDataDirectory()) + "components/" + installID + "/";
/*     */   }
/*     */ 
/*     */   public static String getInstallID(String compName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 560 */     String location = computeAbsoluteComponentLocation(compName);
/* 561 */     DataBinder binder = new DataBinder();
/* 562 */     ResourceUtils.serializeDataBinder(FileUtils.getDirectory(location), FileUtils.getName(location), binder, false, false);
/*     */ 
/* 564 */     return binder.getLocal("installID");
/*     */   }
/*     */ 
/*     */   public static boolean hasDocType(String docType)
/*     */     throws DataException
/*     */   {
/* 572 */     DataResultSet docTypes = SharedObjects.getTable("DocTypes");
/* 573 */     FieldInfo[] fi = ResultSetUtils.createInfoList(docTypes, new String[] { "dDocType" }, true);
/*     */ 
/* 575 */     Vector v = docTypes.findRow(fi[0].m_index, docType);
/* 576 */     return v != null;
/*     */   }
/*     */ 
/*     */   public static Vector buildVector(Properties props)
/*     */   {
/* 584 */     Vector v = new IdcVector();
/* 585 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*     */     {
/* 587 */       String key = (String)en.nextElement();
/* 588 */       v.addElement(key);
/*     */     }
/*     */ 
/* 591 */     return v;
/*     */   }
/*     */ 
/*     */   public static void hideComponentDocMetaFields(Workspace ws, String compName, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 600 */     boolean isComponentDBInstall = SharedObjects.getEnvValueAsBoolean("isComponentDBInstall", true);
/* 601 */     if (binder == null)
/*     */     {
/* 603 */       binder = new DataBinder();
/*     */     }
/*     */ 
/* 606 */     if ((!isComponentDBInstall) || (ws == null))
/*     */       return;
/*     */     try
/*     */     {
/* 610 */       DataDesignInstall cdbi = new DataDesignInstall();
/* 611 */       cdbi.cleanTablesForComponentUninstall(ws, compName, binder);
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/* 615 */       throw new ServiceException(LocaleUtils.encodeMessage("csUnableToDisableDocMetaFieldsFor", compName), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void setDataDesignConfigInfo(DataDesignConfigInfo ddConfigInfo, int type)
/*     */   {
/* 630 */     String queryStr = "";
/* 631 */     String sectionPrefix = "";
/*     */ 
/* 633 */     switch (type)
/*     */     {
/*     */     case 1:
/*     */     case 2:
/* 638 */       queryStr = "QonlyComponentVersion";
/* 639 */       sectionPrefix = "ComponentDBInstallComponent";
/* 640 */       break;
/*     */     case 4:
/* 643 */       queryStr = "QprofileVersion";
/* 644 */       sectionPrefix = "ComponentProfile";
/* 645 */       break;
/*     */     case 8:
/* 647 */       queryStr = "QroleVersion";
/* 648 */       sectionPrefix = "ComponentProfileRule";
/* 649 */       break;
/*     */     case 3:
/*     */     case 5:
/*     */     case 6:
/*     */     case 7:
/*     */     default:
/* 653 */       Report.error("datastoredesign", "!csNoConfigTypeSet", null);
/*     */     }
/*     */ 
/* 657 */     ddConfigInfo.setQueryInfo(queryStr, sectionPrefix);
/*     */   }
/*     */ 
/*     */   public static String getComponentVersion(Workspace ws, String compName, boolean isProfile, boolean isProfileRule, boolean isComponentOnly)
/*     */     throws DataException, ServiceException
/*     */   {
/* 667 */     DataDesignConfigInfo ddci = new DataDesignConfigInfo();
/* 668 */     if (isProfile)
/*     */     {
/* 670 */       setDataDesignConfigInfo(ddci, 4);
/*     */     }
/* 672 */     else if (isProfileRule)
/*     */     {
/* 674 */       setDataDesignConfigInfo(ddci, 8);
/*     */     }
/* 676 */     else if (isComponentOnly)
/*     */     {
/* 678 */       ddci.setQueryInfo("QcomponentVersionInstallOrUpdate", "");
/*     */     }
/*     */     else
/*     */     {
/* 682 */       setDataDesignConfigInfo(ddci, 1);
/*     */     }
/*     */ 
/* 685 */     return DataDesignInstallUtils.getComponentVersion(ws, compName, ddci);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 690 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97206 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.CompInstallUtils
 * JD-Core Version:    0.5.4
 */