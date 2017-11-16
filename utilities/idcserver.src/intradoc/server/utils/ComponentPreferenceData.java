/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ComponentPreferenceData
/*     */ {
/*     */   public static final String INFO_MSG = "info";
/*     */   public static final String INSTALL_ONLY_MSG = "installonly";
/*     */   public static final String POST_INSTALL_ONLY_MSG = "postinstallonly";
/*     */   public static final String CONFIGURABLE_MSG = "configurable";
/*     */   public static final String STRING_PROMPT = "string";
/*     */   public static final String BOOLEAN_PROMPT = "boolean";
/*     */   public static final String INTEGER_PROMPT = "integer";
/*     */   public static final String OPTION_PROMPT = "options";
/*     */   public static final int TYPE_INSTALL_DATA = 1;
/*     */   public static final int TYPE_CONFIG_DATA = 2;
/*     */   public static final int TYPE_ALL_DATA = 3;
/*  79 */   public static final String[] PREF_FIELD_INFO = { "pName", "pMessage", "pMsgType", "pPromptType", "pOptionListName", "pOptionListDispCol", "pValue", "pIsEditable", "pAlwaysUseDefaultsOnInstall", "pNewMsgType", "pIsDisabled", "pLabel", "pIsRequired" };
/*     */ 
/*  94 */   protected DataBinder m_prefData = new DataBinder(true);
/*  95 */   protected String m_prefDir = null;
/*  96 */   protected String m_prefFilename = "preference.hda";
/*  97 */   public final String m_prefResultSetName = "PreferenceData";
/*  98 */   protected DataResultSet m_drset = null;
/*  99 */   protected boolean m_canUpdate = true;
/*     */ 
/* 102 */   protected String m_prefResourcesFilename = "install_strings.htm";
/*     */ 
/* 105 */   public String m_dataDir = null;
/* 106 */   public Properties m_installData = new Properties();
/* 107 */   public Properties m_configData = new Properties();
/*     */ 
/*     */   public ComponentPreferenceData()
/*     */   {
/* 111 */     this.m_drset = new DataResultSet(PREF_FIELD_INFO);
/* 112 */     this.m_prefData.addResultSet("PreferenceData", this.m_drset);
/*     */   }
/*     */ 
/*     */   public ComponentPreferenceData(ResultSet prefData)
/*     */   {
/* 117 */     init(prefData);
/*     */   }
/*     */ 
/*     */   public ComponentPreferenceData(String prefDir, String dataDir)
/*     */   {
/* 122 */     this.m_prefDir = prefDir;
/* 123 */     this.m_dataDir = dataDir;
/*     */   }
/*     */ 
/*     */   public void init(ResultSet prefData)
/*     */   {
/* 128 */     this.m_drset = ((DataResultSet)prefData);
/* 129 */     this.m_prefData.addResultSet("PreferenceData", prefData);
/*     */   }
/*     */ 
/*     */   public void load()
/*     */     throws ServiceException
/*     */   {
/* 135 */     if ((this.m_prefDir != null) && (this.m_prefDir.length() > 0))
/*     */     {
/* 137 */       serializeData(this.m_prefDir, this.m_prefFilename, this.m_prefData, false);
/*     */ 
/* 140 */       this.m_drset = new DataResultSet(PREF_FIELD_INFO);
/* 141 */       DataResultSet tmpdrset = (DataResultSet)this.m_prefData.getResultSet("PreferenceData");
/* 142 */       if (tmpdrset != null)
/*     */       {
/*     */         try
/*     */         {
/* 146 */           this.m_drset = upgradePrefData(tmpdrset);
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 150 */           throw new ServiceException(e);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 156 */     if ((this.m_dataDir == null) || (this.m_dataDir.length() <= 0))
/*     */       return;
/* 158 */     String installFilename = FileUtils.directorySlashesEx(this.m_dataDir + "/install.cfg", false);
/* 159 */     if (FileUtils.checkFile(installFilename, true, false) == 0)
/*     */     {
/* 161 */       SystemPropertiesEditor.readFile(this.m_installData, new IdcVector(), new IdcVector(), installFilename, null);
/*     */     }
/*     */ 
/* 164 */     String configFilename = FileUtils.directorySlashesEx(this.m_dataDir + "/config.cfg", false);
/* 165 */     if (FileUtils.checkFile(configFilename, true, false) != 0)
/*     */       return;
/* 167 */     SystemPropertiesEditor.readFile(this.m_configData, new IdcVector(), new IdcVector(), configFilename, null);
/*     */   }
/*     */ 
/*     */   public void save()
/*     */     throws ServiceException, DataException
/*     */   {
/* 175 */     if ((this.m_canUpdate) && (this.m_prefDir != null) && (this.m_prefDir.length() > 0))
/*     */     {
/* 178 */       this.m_drset = makeBackwardsCompatible(this.m_drset);
/* 179 */       serializeData(this.m_prefDir, this.m_prefFilename, this.m_prefData, true);
/*     */     }
/*     */ 
/* 182 */     if ((this.m_dataDir == null) || (this.m_dataDir.length() <= 0)) {
/*     */       return;
/*     */     }
/*     */     try
/*     */     {
/* 187 */       FileUtils.checkOrCreateDirectory(this.m_dataDir, 2);
/*     */ 
/* 189 */       validatePreferenceData(this.m_drset, this.m_installData);
/* 190 */       validatePreferenceData(this.m_drset, this.m_configData);
/*     */ 
/* 192 */       Vector installKeys = CompInstallUtils.buildVector(this.m_installData);
/* 193 */       Vector configKeys = CompInstallUtils.buildVector(this.m_configData);
/*     */ 
/* 195 */       OutputStream installOutput = new BufferedOutputStream(FileUtilsCfgBuilder.getCfgOutputStream(FileUtils.directorySlashesEx(this.m_dataDir + "/install.cfg", false), "Component", false));
/* 196 */       SystemPropertiesEditor.writeFile(this.m_installData, installKeys, new IdcVector(), installOutput, null);
/*     */ 
/* 198 */       OutputStream configOutput = new BufferedOutputStream(FileUtilsCfgBuilder.getCfgOutputStream(FileUtils.directorySlashesEx(this.m_dataDir + "/config.cfg", false), "Component", false));
/* 199 */       SystemPropertiesEditor.writeFile(this.m_configData, configKeys, new IdcVector(), configOutput, null);
/*     */     }
/*     */     catch (IdcException e)
/*     */     {
/* 203 */       throw new ServiceException(e, "csUnableToWriteInstallConfig", new Object[] { this.m_dataDir });
/*     */     }
/*     */     catch (IOException ioe)
/*     */     {
/* 207 */       throw new ServiceException(ioe, "csUnableToWriteInstallConfig", new Object[] { this.m_dataDir });
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Vector buildVector(Properties props)
/*     */   {
/* 217 */     Vector v = new IdcVector();
/* 218 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*     */     {
/* 220 */       String key = (String)en.nextElement();
/* 221 */       v.addElement(key);
/*     */     }
/*     */ 
/* 224 */     return v;
/*     */   }
/*     */ 
/*     */   public void addEditPrefTableRow(Properties props, boolean isAdd) throws ServiceException, DataException
/*     */   {
/* 229 */     String name = props.getProperty("pName");
/* 230 */     if (this.m_drset == null)
/*     */     {
/* 232 */       this.m_drset = new DataResultSet(PREF_FIELD_INFO);
/*     */     }
/*     */ 
/* 235 */     Vector v = null;
/* 236 */     if (isAdd)
/*     */     {
/* 238 */       v = this.m_drset.createEmptyRow();
/*     */     }
/*     */     else
/*     */     {
/* 242 */       v = this.m_drset.findRow(0, name);
/* 243 */       if ((v == null) || (!v.isEmpty()));
/*     */     }
/*     */ 
/* 249 */     for (int i = 0; i < PREF_FIELD_INFO.length; ++i)
/*     */     {
/* 251 */       String val = props.getProperty(PREF_FIELD_INFO[i]);
/* 252 */       if (val == null)
/*     */       {
/* 254 */         val = "";
/*     */       }
/* 256 */       v.setElementAt(val, i);
/*     */     }
/*     */ 
/* 259 */     if (isAdd)
/*     */     {
/* 261 */       this.m_drset.addRow(v);
/*     */     }
/*     */     else
/*     */     {
/* 265 */       this.m_drset.setRowValues(v, this.m_drset.getCurrentRow());
/*     */     }
/* 267 */     updateData();
/*     */   }
/*     */ 
/*     */   public void deletePrefTableRow(String pName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 273 */     Vector v = this.m_drset.findRow(0, pName);
/* 274 */     if ((v != null) && (v.isEmpty()));
/* 278 */     this.m_drset.deleteCurrentRow();
/*     */ 
/* 281 */     this.m_configData.remove(pName);
/*     */ 
/* 283 */     updateData();
/*     */   }
/*     */ 
/*     */   public DataResultSet getPreferenceTable()
/*     */   {
/* 292 */     return this.m_drset;
/*     */   }
/*     */ 
/*     */   public ResourceContainer getPreferenceResources()
/*     */   {
/* 304 */     ResourceContainer res = new ResourceContainer();
/*     */ 
/* 307 */     if ((this.m_prefDir == null) || (this.m_prefDir.length() > 0))
/*     */     {
/* 309 */       String resourceFilename = FileUtils.directorySlashesEx(this.m_prefDir + "/" + this.m_prefResourcesFilename, false);
/* 310 */       if (FileUtils.checkFile(resourceFilename, true, false) == 0)
/*     */       {
/*     */         try
/*     */         {
/* 314 */           DataLoader.cacheResourceFile(res, resourceFilename);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 318 */           Report.error("componentwizard", e, "csComponemtPreferenceDataMissing", new Object[] { resourceFilename });
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 324 */     return res;
/*     */   }
/*     */ 
/*     */   public void addResultSetsToBinder(ResourceContainer additionalResources, DataBinder binder)
/*     */     throws ServiceException, DataException
/*     */   {
/* 337 */     if (additionalResources == null)
/*     */     {
/* 339 */       additionalResources = new ResourceContainer();
/*     */     }
/*     */ 
/* 342 */     FieldInfo[] infos = ResultSetUtils.createInfoList(this.m_drset, PREF_FIELD_INFO, true);
/* 343 */     for (this.m_drset.first(); this.m_drset.isRowPresent(); this.m_drset.next())
/*     */     {
/* 345 */       String promptType = this.m_drset.getStringValue(infos[3].m_index);
/* 346 */       if (!promptType.equalsIgnoreCase("options"))
/*     */         continue;
/* 348 */       String optName = this.m_drset.getStringValue(infos[4].m_index);
/* 349 */       Table optTable = additionalResources.getTable(optName);
/* 350 */       ResultSet optRset = null;
/* 351 */       if ((optTable != null) && (optTable.getNumRows() > 0))
/*     */       {
/* 353 */         DataResultSet tempDrset = new DataResultSet();
/* 354 */         tempDrset.init(optTable);
/* 355 */         optRset = tempDrset;
/*     */       }
/*     */       else
/*     */       {
/* 359 */         optRset = SharedObjects.getTable(optName);
/*     */       }
/*     */ 
/* 362 */       if ((optRset == null) || (optRset.isEmpty()))
/*     */       {
/* 364 */         throw new ServiceException(LocaleUtils.encodeMessage("csUnableToLoadOptionList", null, optName));
/*     */       }
/*     */ 
/* 367 */       binder.addResultSet(optName, optRset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadPreferenceStrings()
/*     */   {
/* 378 */     if ((this.m_prefDir != null) && (this.m_prefDir.length() <= 0))
/*     */       return;
/* 380 */     String resourceFilename = FileUtils.directorySlashesEx(this.m_prefDir + "/" + this.m_prefResourcesFilename, false);
/* 381 */     if (FileUtils.checkFile(resourceFilename, true, false) != 0)
/*     */       return;
/* 383 */     ResourceContainer res = SharedObjects.getResources();
/*     */     try
/*     */     {
/* 386 */       loadStrings(res, resourceFilename);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 390 */       Report.error("componentwizard", "Couldn't load strings from component resource file \"" + resourceFilename + "\".", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void loadStrings(ResourceContainer res, String filePath)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/* 404 */     BufferedReader reader = null;
/* 405 */     File file = null;
/*     */     try
/*     */     {
/* 408 */       file = FileUtilsCfgBuilder.getCfgFile(filePath, null, false);
/* 409 */       reader = ResourceLoader.openResourceReader(file, null, ResourceLoader.F_IS_HTML);
/* 410 */       res.parseAndAddResources(reader, filePath);
/*     */     }
/*     */     finally
/*     */     {
/* 414 */       if (reader != null)
/*     */       {
/* 416 */         FileUtils.closeReader(reader);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void updateData()
/*     */     throws ServiceException, DataException
/*     */   {
/* 427 */     this.m_prefData.addResultSet("PreferenceData", this.m_drset);
/* 428 */     save();
/*     */   }
/*     */ 
/*     */   public void serializeData(String path, String file, DataBinder data, boolean isWrite) throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 435 */       if (isWrite)
/*     */       {
/* 437 */         FileUtils.reserveDirectory(path);
/*     */       }
/*     */ 
/* 440 */       ResourceUtils.serializeDataBinder(path, file, data, isWrite, false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 448 */       if (isWrite)
/*     */       {
/* 450 */         FileUtils.releaseDirectory(path);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean hasInstallPrefs()
/*     */   {
/* 460 */     if (this.m_drset == null)
/*     */     {
/* 462 */       return false;
/*     */     }
/*     */ 
/* 466 */     for (this.m_drset.first(); this.m_drset.isRowPresent(); this.m_drset.next())
/*     */     {
/* 468 */       String msgType = this.m_drset.getStringValueByName("pNewMsgType");
/* 469 */       if ((msgType.equalsIgnoreCase("installonly")) || (msgType.equalsIgnoreCase("configurable")))
/*     */       {
/* 472 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 476 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean hasPostInstallPrefs()
/*     */   {
/* 484 */     if (this.m_drset == null)
/*     */     {
/* 486 */       return false;
/*     */     }
/*     */ 
/* 490 */     for (this.m_drset.first(); this.m_drset.isRowPresent(); this.m_drset.next())
/*     */     {
/* 492 */       String msgType = this.m_drset.getStringValueByName("pNewMsgType");
/* 493 */       if ((msgType.equalsIgnoreCase("postinstallonly")) || (msgType.equalsIgnoreCase("configurable")))
/*     */       {
/* 496 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 500 */     return false;
/*     */   }
/*     */ 
/*     */   public void upgrade()
/*     */     throws DataException
/*     */   {
/* 508 */     this.m_drset = upgradePrefData(this.m_drset);
/*     */   }
/*     */ 
/*     */   public static DataResultSet upgradePrefData(DataResultSet drset)
/*     */     throws DataException
/*     */   {
/* 535 */     DataResultSet newPref = new DataResultSet(PREF_FIELD_INFO);
/* 536 */     newPref.mergeWithFlags(null, drset, 16, -1);
/* 537 */     FieldInfo fi = new FieldInfo();
/*     */ 
/* 540 */     boolean upgradeUseDefaults = !drset.getFieldInfo("pAlwaysUseDefaultsOnInstall", fi);
/* 541 */     boolean upgradeNewMsgType = !drset.getFieldInfo("pNewMsgType", fi);
/* 542 */     boolean upgradeLabel = !drset.getFieldInfo("pLabel", fi);
/*     */ 
/* 544 */     if ((upgradeUseDefaults) || (upgradeNewMsgType) || (upgradeLabel))
/*     */     {
/* 546 */       newPref = new DataResultSet(PREF_FIELD_INFO);
/*     */ 
/* 548 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 550 */         Vector v = newPref.createEmptyRow();
/* 551 */         for (int j = 0; j < PREF_FIELD_INFO.length; ++j)
/*     */         {
/* 553 */           String val = "";
/* 554 */           String fieldName = PREF_FIELD_INFO[j];
/*     */ 
/* 556 */           if (upgradeNewMsgType)
/*     */           {
/* 562 */             for (int k = 0; k < drset.getNumFields(); ++k)
/*     */             {
/* 564 */               String curField = drset.getFieldName(k);
/*     */ 
/* 567 */               if ((fieldName.equalsIgnoreCase("pNewMsgType")) && (curField.equalsIgnoreCase("pMsgType")))
/*     */               {
/* 575 */                 String curVal = drset.getStringValue(k);
/*     */ 
/* 577 */                 if (curVal.equalsIgnoreCase("info"))
/*     */                 {
/* 580 */                   val = curVal; break;
/*     */                 }
/*     */ 
/* 589 */                 String isEditable = drset.getStringValueByName("pIsEditable");
/* 590 */                 if (StringUtils.convertToBool(isEditable, false))
/*     */                 {
/* 592 */                   val = "configurable";
/*     */                 }
/*     */                 else
/*     */                 {
/* 596 */                   val = "installonly";
/*     */                 }
/*     */ 
/* 599 */                 break;
/*     */               }
/* 601 */               if (!curField.equalsIgnoreCase(fieldName)) {
/*     */                 continue;
/*     */               }
/*     */ 
/* 605 */               val = drset.getStringValue(k);
/* 606 */               break;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 611 */           if ((upgradeLabel) && (fieldName.equalsIgnoreCase("pLabel")))
/*     */           {
/* 613 */             val = drset.getStringValueByName("pName");
/*     */           }
/*     */ 
/* 616 */           if (val == null)
/*     */           {
/* 618 */             val = "";
/*     */           }
/*     */ 
/* 621 */           v.setElementAt(val, j);
/*     */         }
/*     */ 
/* 625 */         newPref.addRow(v);
/*     */       }
/*     */     }
/* 628 */     return newPref;
/*     */   }
/*     */ 
/*     */   public static DataResultSet makeBackwardsCompatible(DataResultSet drset)
/*     */   {
/* 648 */     FieldInfo[] fi = null;
/*     */     try
/*     */     {
/* 651 */       fi = ResultSetUtils.createInfoList(drset, PREF_FIELD_INFO, true);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 655 */       Report.trace("componentwizard", "Could not downgrade preference prompts - invalid format.", e);
/* 656 */       return drset;
/*     */     }
/*     */ 
/* 659 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 662 */       Vector v = drset.getCurrentRowValues();
/* 663 */       String pNewMsgType = (String)v.get(fi[9].m_index);
/* 664 */       String pIsEditable = "";
/* 665 */       String pMsgType = "";
/* 666 */       if (pNewMsgType.equalsIgnoreCase("info"))
/*     */       {
/* 668 */         pMsgType = "info";
/*     */       }
/*     */       else
/*     */       {
/* 672 */         pMsgType = "prompt";
/* 673 */         if (!pNewMsgType.equalsIgnoreCase("installonly"))
/*     */         {
/* 675 */           pIsEditable = "1";
/*     */         }
/*     */       }
/* 678 */       v.setElementAt(pMsgType, fi[2].m_index);
/* 679 */       v.setElementAt(pIsEditable, fi[7].m_index);
/*     */     }
/*     */ 
/* 682 */     return drset;
/*     */   }
/*     */ 
/*     */   public static Properties getComponentData(String compName, String installID, int type)
/*     */     throws ServiceException, DataException
/*     */   {
/* 702 */     Properties retData = new Properties();
/*     */ 
/* 704 */     if ((installID == null) || (installID.length() == 0))
/*     */     {
/* 706 */       installID = CompInstallUtils.getInstallID(compName);
/*     */     }
/*     */ 
/* 709 */     if ((installID != null) && (installID.length() > 0))
/*     */     {
/* 711 */       String dataDir = CompInstallUtils.getInstallConfPath(installID, compName);
/* 712 */       ComponentPreferenceData prefData = new ComponentPreferenceData(null, dataDir);
/* 713 */       prefData.load();
/*     */ 
/* 715 */       if ((type & 0x1) != 0)
/*     */       {
/* 717 */         retData = prefData.m_installData;
/*     */       }
/* 719 */       else if ((type & 0x2) != 0)
/*     */       {
/* 721 */         retData = prefData.m_configData;
/*     */       }
/* 723 */       else if ((type & 0x3) != 0)
/*     */       {
/* 725 */         prefData.m_installData.putAll(prefData.m_configData);
/* 726 */         retData = prefData.m_installData;
/*     */       }
/*     */     }
/*     */ 
/* 730 */     return retData;
/*     */   }
/*     */ 
/*     */   public static void validatePreferenceData(DataResultSet prefData, Properties data)
/*     */     throws DataException
/*     */   {
/* 743 */     FieldInfo[] fis = ResultSetUtils.createInfoList(prefData, new String[] { "pName", "pPromptType", "pIsRequired" }, true);
/*     */ 
/* 745 */     for (prefData.first(); prefData.isRowPresent(); prefData.next())
/*     */     {
/* 747 */       String name = prefData.getStringValue(fis[0].m_index);
/* 748 */       String type = prefData.getStringValue(fis[1].m_index);
/*     */ 
/* 751 */       if (!type.equals("integer"))
/*     */         continue;
/* 753 */       String value = data.getProperty(name);
/* 754 */       if ((value == null) || (value.length() <= 0))
/*     */         continue;
/*     */       try
/*     */       {
/* 758 */         Integer.parseInt(value);
/*     */       }
/*     */       catch (NumberFormatException e)
/*     */       {
/* 762 */         throw new DataException(e, IdcMessageFactory.lc("csInvalidIntegerPromptValue", new Object[] { name, value }));
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setCanUpdate(boolean isUpdate)
/*     */   {
/* 771 */     this.m_canUpdate = isUpdate;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 776 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97206 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ComponentPreferenceData
 * JD-Core Version:    0.5.4
 */