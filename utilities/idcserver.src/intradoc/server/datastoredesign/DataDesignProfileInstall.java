/*     */ package intradoc.server.datastoredesign;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DocProfileManager;
/*     */ import intradoc.server.utils.CompInstallUtils;
/*     */ import intradoc.server.utils.ComponentLocationUtils;
/*     */ import intradoc.shared.DocProfileData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DataDesignProfileInstall
/*     */ {
/*     */   public DataDesignConfigInfo m_profileConfigInfo;
/*     */   public DataDesignConfigInfo m_profileRuleConfigInfo;
/*  51 */   public Map<String, String> m_dsdColumnUpgradeMap = new HashMap();
/*     */ 
/*  53 */   protected static String m_compName = null;
/*     */   public static final String PROFILE_QUERY_STR = "QprofileVersion";
/*     */   public static final String PROFILE_SECTION_PREFIX = "ComponentProfile";
/*     */   public static final String PROFILE_RULE_QUERY_STR = "QroleVersion";
/*     */   public static final String PROFILE_RULE_SECTION_PREFIX = "ComponentProfileRule";
/*     */ 
/*     */   public DataDesignProfileInstall()
/*     */   {
/*  80 */     Map curConfigTableValues = new HashMap();
/*  81 */     Map configTableValues = new HashMap();
/*     */ 
/*  83 */     this.m_profileConfigInfo = new DataDesignConfigInfo(curConfigTableValues, configTableValues);
/*  84 */     CompInstallUtils.setDataDesignConfigInfo(this.m_profileConfigInfo, 4);
/*     */ 
/*  86 */     this.m_profileRuleConfigInfo = new DataDesignConfigInfo(curConfigTableValues, configTableValues);
/*  87 */     CompInstallUtils.setDataDesignConfigInfo(this.m_profileRuleConfigInfo, 8);
/*     */ 
/*  90 */     this.m_dsdColumnUpgradeMap = DataDesignInstallUtils.getFieldNameUpgradeMap();
/*     */   }
/*     */ 
/*     */   public void configProfileForComponents(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 102 */     DataResultSet drset = DataDesignInstallUtils.getUpgradedAndRenamedTable("DataStoreDesignProfileList", "ProfileList", this.m_dsdColumnUpgradeMap);
/*     */ 
/* 105 */     if ((drset == null) || (drset.getNumRows() == 0))
/*     */     {
/* 108 */       return;
/*     */     }
/*     */ 
/* 111 */     String profileName = null;
/* 112 */     String compVersion = null;
/* 113 */     String checkFlag = null;
/* 114 */     String fileName = null;
/*     */ 
/* 116 */     String[] keys = { "dpName", "dsdComponentName", "dsdVersion", "dsdCheckFlag", "dsdFileName" };
/*     */     try
/*     */     {
/* 121 */       FieldInfo[] fi = ResultSetUtils.createInfoList(drset, keys, true);
/*     */ 
/* 123 */       boolean isUpdated = false;
/*     */ 
/* 125 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 127 */         Vector v = drset.getCurrentRowValues();
/* 128 */         profileName = (String)v.elementAt(fi[0].m_index);
/* 129 */         m_compName = (String)v.elementAt(fi[1].m_index);
/* 130 */         compVersion = (String)v.elementAt(fi[2].m_index);
/* 131 */         checkFlag = (String)v.elementAt(fi[3].m_index);
/* 132 */         fileName = (String)v.elementAt(fi[4].m_index);
/*     */ 
/* 134 */         if ((fileName == null) || (fileName.length() <= 0))
/*     */         {
/* 136 */           fileName = profileName.toLowerCase() + ".hda";
/*     */         }
/* 138 */         String tempCompName = null;
/*     */ 
/* 141 */         boolean canPerformOperations = true;
/*     */ 
/* 144 */         String forceKey = m_compName + ":" + profileName + ":IsForceUpdate";
/* 145 */         String isForceUpdateStr = (String)cxt.getCachedObject(forceKey);
/* 146 */         boolean isForceUpdate = StringUtils.convertToBool(isForceUpdateStr, false);
/*     */ 
/* 152 */         if (isForceUpdate)
/*     */         {
/* 154 */           tempCompName = m_compName;
/* 155 */           m_compName = m_compName + ":" + profileName;
/*     */         }
/*     */ 
/* 159 */         canPerformOperations = DataDesignInstallUtils.evaluateCheckFlag(checkFlag);
/*     */ 
/* 161 */         if ((canPerformOperations) && (!DataDesignInstallUtils.isNewVersion(ws, compVersion, m_compName, this.m_profileConfigInfo)))
/*     */         {
/* 164 */           canPerformOperations = false;
/*     */         }
/*     */ 
/* 167 */         if (isForceUpdate)
/*     */         {
/* 169 */           m_compName = tempCompName;
/*     */         }
/*     */ 
/* 173 */         if (!canPerformOperations)
/*     */           continue;
/* 175 */         isUpdated = true;
/* 176 */         addOrUpdateProfile(ws, profileName, fileName);
/*     */       }
/*     */ 
/* 180 */       if (isUpdated)
/*     */       {
/* 182 */         List updateComponentList = new ArrayList();
/*     */ 
/* 184 */         for (String key : this.m_profileConfigInfo.m_configTableValues.keySet())
/*     */         {
/* 186 */           updateComponentList.add(this.m_profileConfigInfo.m_configTableValues.get(key));
/*     */         }
/*     */ 
/* 189 */         DataDesignInstallUtils.updateConfigTable(ws, updateComponentList);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 194 */       SystemUtils.err(e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addOrUpdateProfile(Workspace ws, String profileName, String fileName)
/*     */     throws DataException, ServiceException
/*     */   {
/* 201 */     boolean isProfileExits = true;
/*     */ 
/* 203 */     String profileService = "ADD_DOCPROFILE";
/*     */ 
/* 205 */     DataResultSet profileSet = SharedObjects.getTable("DocumentProfiles");
/* 206 */     if (profileSet == null)
/*     */     {
/* 208 */       isProfileExits = false;
/*     */     }
/*     */     else
/*     */     {
/* 212 */       int index = ResultSetUtils.getIndexMustExist(profileSet, "dpName");
/*     */ 
/* 214 */       if (profileSet.findRow(index, profileName) == null)
/*     */       {
/* 216 */         isProfileExits = false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 221 */     if (isProfileExits)
/*     */     {
/* 223 */       profileService = "EDIT_DOCPROFILE";
/*     */     }
/*     */ 
/* 226 */     boolean isContinue = addOrUpdateRule(ws, profileName);
/*     */ 
/* 228 */     if (!isContinue)
/*     */       return;
/* 230 */     DataBinder profileBinder = loadBinder(profileName, fileName);
/*     */ 
/* 232 */     String infoStr = (isProfileExits) ? "csCPUdatingProfile" : "csCPAddingProfile";
/*     */     try
/*     */     {
/* 237 */       CompInstallUtils.executeService(ws, profileService, profileBinder);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 242 */       String msg = LocaleUtils.encodeMessage(infoStr, null, m_compName, profileName, profileName);
/* 243 */       SystemUtils.outln(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean addOrUpdateRule(Workspace ws, String profileName)
/*     */     throws DataException, ServiceException
/*     */   {
/* 251 */     DataResultSet profileRulesRS = DataDesignInstallUtils.getUpgradedTable(m_compName + "." + profileName + ".ProfileRules", this.m_dsdColumnUpgradeMap);
/*     */ 
/* 253 */     boolean isSuccess = true;
/* 254 */     if ((profileRulesRS == null) || (profileRulesRS.isEmpty()))
/*     */     {
/* 256 */       return true;
/*     */     }
/*     */ 
/* 259 */     for (profileRulesRS.first(); profileRulesRS.isRowPresent(); profileRulesRS.next())
/*     */     {
/* 261 */       String ruleName = ResultSetUtils.getValue(profileRulesRS, "dpRuleName");
/* 262 */       String fileName = ResultSetUtils.getValue(profileRulesRS, "dsdFileName");
/* 263 */       String version = ResultSetUtils.getValue(profileRulesRS, "dsdVersion");
/*     */ 
/* 265 */       boolean canPerformOperations = true;
/*     */ 
/* 267 */       String dName = m_compName + ":" + profileName;
/*     */ 
/* 269 */       if (!DataDesignInstallUtils.isNewVersion(ws, version, dName, this.m_profileRuleConfigInfo))
/*     */       {
/* 271 */         canPerformOperations = false;
/*     */       }
/*     */ 
/* 274 */       if (!canPerformOperations)
/*     */         continue;
/* 276 */       if ((fileName == null) || (fileName.length() <= 0))
/*     */       {
/* 278 */         fileName = ruleName.toLowerCase() + ".hda";
/*     */       }
/*     */ 
/* 281 */       String ruleService = "ADD_DOCRULE";
/*     */ 
/* 283 */       boolean isRuleExits = true;
/*     */ 
/* 285 */       DocProfileData data = DocProfileManager.getRule(ruleName);
/* 286 */       if (data == null)
/*     */       {
/* 288 */         isRuleExits = false;
/*     */       }
/*     */ 
/* 291 */       if (isRuleExits)
/*     */       {
/* 293 */         ruleService = "EDIT_DOCRULE";
/*     */       }
/* 295 */       String infoStr = (isRuleExits) ? "csCPUdatingRule" : "csCPAddingRule";
/*     */ 
/* 298 */       DataBinder ruleBinder = loadBinder(ruleName, fileName);
/*     */       try
/*     */       {
/* 302 */         CompInstallUtils.executeService(ws, ruleService, ruleBinder);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 306 */         String msg = LocaleUtils.encodeMessage(infoStr, null, m_compName, profileName, ruleName);
/* 307 */         SystemUtils.outln(msg);
/* 308 */         isSuccess = false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 313 */     return isSuccess;
/*     */   }
/*     */ 
/*     */   protected DataBinder loadBinder(String profileName, String fileName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 320 */     DataBinder sBinder = new DataBinder();
/*     */ 
/* 322 */     String path = ComponentLocationUtils.computeAbsoluteComponentLocation(m_compName);
/* 323 */     if ((path != null) && (path.length() > 0))
/*     */     {
/* 325 */       sBinder = ResourceUtils.readDataBinder(FileUtils.getDirectory(path), "/data/profile/" + fileName);
/*     */     }
/*     */ 
/* 328 */     return sBinder;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 333 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 68283 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.datastoredesign.DataDesignProfileInstall
 * JD-Core Version:    0.5.4
 */