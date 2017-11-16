/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.utils.ComponentLocationUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.io.File;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class NewComponentDlg extends CWizardBaseDlg
/*     */ {
/*  73 */   protected JCheckBox m_createNew = null;
/*  74 */   protected JCheckBox m_useExisting = null;
/*  75 */   protected JTextField m_dirField = null;
/*  76 */   protected JTextField m_nameField = null;
/*  77 */   protected JButton m_browseBtn = null;
/*  78 */   protected JCheckBox m_copyExisting = null;
/*     */ 
/*  80 */   protected ComponentWizardManager m_manager = null;
/*     */ 
/*     */   public NewComponentDlg(SystemInterface sys, String title, String helpPage, ComponentWizardManager mgr)
/*     */   {
/*  85 */     super(sys, title, helpPage);
/*  86 */     if (helpPage == null)
/*     */     {
/*  88 */       this.m_helpPage = DialogHelpTable.getHelpPage("CW_AddComponent");
/*     */     }
/*  90 */     this.m_manager = mgr;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  96 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 103 */           NewComponentDlg.this.addOrAttachComponent();
/*     */ 
/* 105 */           if (!NewComponentDlg.this.m_useExisting.isSelected())
/*     */           {
/* 108 */             String name = NewComponentDlg.this.m_helper.m_props.getProperty("name");
/* 109 */             CWizardGuiUtils.doMessage(NewComponentDlg.this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizCreatedComponent", new Object[] { name }), 1);
/*     */           }
/*     */ 
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 116 */           CWizardGuiUtils.reportError(NewComponentDlg.this.m_systemInterface, exp, (IdcMessage)null);
/* 117 */           NewComponentDlg.this.getDialogHelper().m_result = 0;
/* 118 */           return false;
/*     */         }
/*     */ 
/* 121 */         return true;
/*     */       }
/*     */     };
/* 125 */     JPanel mainPanel = getDialogHelper().initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 128 */     initUI(mainPanel);
/*     */ 
/* 131 */     String path = SharedObjects.getEnvironmentValue("ComponentDir");
/* 132 */     if ((path == null) || (path.length() == 0))
/*     */     {
/* 134 */       path = "custom";
/*     */     }
/* 136 */     this.m_helper.m_props.put("location", path);
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel mainPanel)
/*     */   {
/* 142 */     ButtonGroup group = new ButtonGroup();
/*     */ 
/* 144 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 145 */     this.m_createNew = new JCheckBox(LocaleResources.getString("csCompWizLabelCreateNew", null), true);
/* 146 */     group.add(this.m_createNew);
/* 147 */     this.m_helper.addExchangeComponent(mainPanel, this.m_createNew, "CreateNew");
/* 148 */     addNewComponentPanel(mainPanel);
/*     */ 
/* 150 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 151 */     this.m_useExisting = new JCheckBox(LocaleResources.getString("csCompWizLabelUseExisting", null), false);
/* 152 */     group.add(this.m_useExisting);
/* 153 */     this.m_helper.addExchangeComponent(mainPanel, this.m_useExisting, "UseExisting");
/* 154 */     addExistingComponentPanel(mainPanel);
/*     */ 
/* 156 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 160 */         boolean useExisting = NewComponentDlg.this.m_useExisting.isSelected();
/* 161 */         if (useExisting)
/*     */         {
/* 163 */           NewComponentDlg.this.setEnabledNewOrExistingComponents(false);
/*     */         }
/*     */         else
/*     */         {
/* 167 */           NewComponentDlg.this.setEnabledNewOrExistingComponents(true);
/*     */         }
/*     */       }
/*     */     };
/* 171 */     this.m_useExisting.addItemListener(iListener);
/* 172 */     this.m_createNew.addItemListener(iListener);
/* 173 */     setEnabledNewOrExistingComponents(true);
/*     */   }
/*     */ 
/*     */   protected void setEnabledNewOrExistingComponents(boolean flag)
/*     */   {
/* 178 */     Object[] obj = this.m_helper.m_exchange.findComponent("blocation", false);
/* 179 */     JTextField tfield = null;
/* 180 */     if (obj != null)
/*     */     {
/* 182 */       tfield = (JTextField)obj[1];
/*     */     }
/*     */ 
/* 185 */     tfield.setEnabled(!flag);
/* 186 */     this.m_browseBtn.setEnabled(!flag);
/* 187 */     this.m_nameField.setEnabled(flag);
/* 188 */     this.m_dirField.setEnabled(flag);
/* 189 */     this.m_copyExisting.setEnabled(flag);
/*     */   }
/*     */ 
/*     */   protected JPanel addNewComponentPanel(JPanel mainPanel)
/*     */   {
/* 194 */     Insets oldInsets = this.m_helper.m_gridHelper.m_gc.insets;
/* 195 */     JPanel pnl = addNewSubPanel(mainPanel);
/*     */ 
/* 197 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 15, 2, 15);
/* 198 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("csCompWizLabelName2", null), this.m_nameField = new CustomTextField(40), "name");
/*     */ 
/* 200 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("csCompWizLabelDir", null), this.m_dirField = new CustomTextField(40), "location");
/*     */ 
/* 203 */     JPanel copyPanel = addNewSubPanel(pnl, false);
/*     */ 
/* 205 */     this.m_copyExisting = new JCheckBox(LocaleResources.getString("csCompWizLabelCopyExisting", null));
/*     */ 
/* 207 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 208 */     this.m_helper.addComponent(copyPanel, this.m_copyExisting);
/*     */ 
/* 210 */     JButton browseBtn = this.m_helper.addFilePathComponent(copyPanel, 30, LocaleResources.getString("csCompWizLabelFilePath", null), "clocation");
/*     */ 
/* 213 */     this.m_helper.m_gridHelper.m_gc.insets = oldInsets;
/* 214 */     setEnabledNewComponents(browseBtn, false);
/*     */ 
/* 216 */     ItemListener iListener = new ItemListener(browseBtn)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 220 */         boolean copyExisting = NewComponentDlg.this.m_copyExisting.isSelected();
/* 221 */         boolean flag = false;
/* 222 */         if (copyExisting)
/*     */         {
/* 224 */           flag = true;
/*     */         }
/* 226 */         NewComponentDlg.this.setEnabledNewComponents(this.val$browseBtn, flag);
/*     */       }
/*     */     };
/* 229 */     this.m_copyExisting.addItemListener(iListener);
/*     */ 
/* 231 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void setEnabledNewComponents(JButton browseBtn, boolean flag)
/*     */   {
/* 236 */     Object[] obj = this.m_helper.m_exchange.findComponent("clocation", false);
/* 237 */     JTextField tfield = null;
/* 238 */     if (obj != null)
/*     */     {
/* 240 */       tfield = (JTextField)obj[1];
/* 241 */       tfield.setEnabled(flag);
/*     */     }
/* 243 */     browseBtn.setEnabled(flag);
/*     */   }
/*     */ 
/*     */   protected JPanel addExistingComponentPanel(JPanel mainPanel)
/*     */   {
/* 248 */     Insets oldInsets = this.m_helper.m_gridHelper.m_gc.insets;
/* 249 */     JPanel pnl = addNewSubPanel(mainPanel);
/*     */ 
/* 251 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 15, 2, 15);
/* 252 */     this.m_helper.m_gridHelper.prepareAddRowElement(13);
/* 253 */     this.m_helper.addComponent(pnl, new CustomLabel(LocaleResources.getString("csCompWizLabelFilePath", null), 1));
/*     */ 
/* 255 */     this.m_browseBtn = this.m_helper.addFilePathComponent(pnl, 40, null, "blocation");
/*     */ 
/* 258 */     this.m_helper.m_gridHelper.m_gc.insets = oldInsets;
/*     */ 
/* 260 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void addOrAttachComponent() throws ServiceException, DataException
/*     */   {
/* 265 */     Properties props = this.m_helper.m_props;
/*     */ 
/* 267 */     String name = props.getProperty("name");
/* 268 */     String location = null;
/* 269 */     String absPath = null;
/* 270 */     boolean useExisting = this.m_useExisting.isSelected();
/* 271 */     boolean isCopy = this.m_copyExisting.isSelected();
/* 272 */     boolean isNew = false;
/*     */ 
/* 274 */     Map args = new HashMap();
/* 275 */     String baseDir = props.getProperty("location");
/* 276 */     String cmpDir = SharedObjects.getEnvironmentValue("ComponentDir");
/* 277 */     if ((cmpDir != null) && (cmpDir.equals(baseDir)))
/*     */     {
/* 279 */       baseDir = "";
/*     */     }
/*     */ 
/* 282 */     DataBinder orgData = null;
/* 283 */     if (useExisting)
/*     */     {
/* 285 */       location = props.getProperty("blocation");
/* 286 */       if (!location.endsWith(".hda"))
/*     */       {
/* 288 */         throw new ServiceException("!csCompWizComponentFileExtError");
/*     */       }
/*     */ 
/* 291 */       absPath = FileUtils.fileSlashes(location);
/* 292 */       orgData = ResourceUtils.readDataBinderFromPath(absPath);
/* 293 */       name = orgData.getLocal("ComponentName");
/* 294 */       if ((name == null) || (name.length() == 0))
/*     */       {
/* 296 */         name = FileUtils.getParent(absPath);
/*     */       }
/* 298 */       props.put("name", name);
/* 299 */       orgData.putLocal("location", absPath);
/*     */ 
/* 304 */       String[] loc = new String[1];
/* 305 */       boolean isNonLocal = ComponentLocationUtils.isInNonLocalLocation(absPath, orgData.getLocalData(), loc);
/*     */ 
/* 307 */       if (isNonLocal)
/*     */       {
/* 309 */         location = loc[0];
/* 310 */         props.put("isHome", "1");
/* 311 */         orgData.putLocal("componentType", "home");
/*     */       }
/*     */     }
/* 314 */     else if (isCopy)
/*     */     {
/* 316 */       String from = props.getProperty("clocation");
/* 317 */       if (!from.endsWith(".hda"))
/*     */       {
/* 319 */         throw new ServiceException("!csCompWizComponentFileExtError");
/*     */       }
/* 321 */       orgData = ResourceUtils.readDataBinderFromPath(from);
/*     */ 
/* 324 */       String componentTags = orgData.getLocal("componentTags");
/* 325 */       boolean isSystem = ComponentLocationUtils.hasSystemTag(componentTags);
/* 326 */       if ((isSystem) && (!baseDir.equals("system")))
/*     */       {
/* 328 */         throw new ServiceException(null, "csCompWizNewComponentSystemComponent", new Object[] { componentTags });
/*     */       }
/*     */ 
/* 331 */       if ((!isSystem) && (baseDir.equals("system")))
/*     */       {
/* 333 */         throw new ServiceException(null, "csCompWizNewComponentCustomComponent", new Object[] { componentTags });
/*     */       }
/*     */ 
/* 337 */       location = FileUtils.directorySlashes(baseDir) + name + "/";
/* 338 */       String absDir = computeAndCreateAbsoluteDirectory(baseDir, location);
/*     */ 
/* 341 */       String sourceDir = FileUtils.getDirectory(from);
/* 342 */       File sourceFile = new File(sourceDir);
/* 343 */       File targetFile = new File(absDir);
/* 344 */       FileUtils.copyDirectoryWithFlags(sourceFile, targetFile, 1, null, 1);
/*     */ 
/* 347 */       absPath = renameComponent(name, absDir, from, orgData, args, false);
/*     */     }
/*     */     else
/*     */     {
/* 351 */       isNew = true;
/* 352 */       location = FileUtils.directorySlashes(baseDir) + name + "/";
/*     */ 
/* 354 */       String absDir = computeAndCreateAbsoluteDirectory(baseDir, location);
/*     */ 
/* 356 */       location = location + name + ".hda";
/* 357 */       absPath = absDir + name + ".hda";
/*     */     }
/*     */ 
/* 361 */     absPath = CWizardUtils.changeDriveLetterToUpper(absPath);
/*     */ 
/* 364 */     location = ComponentLocationUtils.adjustToRelative(absPath, location, SharedObjects.getSafeEnvironment());
/*     */ 
/* 367 */     props.put("location", location);
/* 368 */     args.put("isNew", "" + isNew);
/* 369 */     args.put("isCopy", "" + isCopy);
/* 370 */     args.put("useExisting", "" + useExisting);
/* 371 */     args.put("absPath", absPath);
/* 372 */     this.m_manager.addOrEditComponent(this.m_helper.m_props, orgData, args);
/*     */ 
/* 375 */     if ((!isCopy) && (!isNew))
/*     */       return;
/* 377 */     this.m_manager.m_component.createReadmeFile();
/*     */   }
/*     */ 
/*     */   protected String computeAndCreateAbsoluteDirectory(String baseDir, String location)
/*     */     throws ServiceException
/*     */   {
/* 384 */     String dir = null;
/* 385 */     if (baseDir.equals("system"))
/*     */     {
/* 387 */       dir = ComponentLocationUtils.computeDefaultSystemComponentDir(1, false, new boolean[1]);
/*     */     }
/*     */     else
/*     */     {
/* 392 */       dir = ComponentLocationUtils.computeDefaultCustomComponentDir(1, false, new boolean[1]);
/*     */     }
/*     */ 
/* 395 */     if (ComponentLocationUtils.startWithStandardDirectories(location))
/*     */     {
/* 397 */       dir = FileUtils.getParent(dir);
/*     */     }
/* 399 */     String absDir = FileUtils.getAbsolutePath(dir, location);
/*     */ 
/* 403 */     int r = FileUtils.checkFile(absDir, false, false);
/* 404 */     if (r != -16)
/*     */     {
/* 406 */       throw new ServiceException(null, "csCompWizNewComponentDirAlreadyExists", new Object[] { absDir });
/*     */     }
/*     */ 
/* 410 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(absDir, 1, true);
/*     */ 
/* 412 */     return absDir;
/*     */   }
/*     */ 
/*     */   protected String renameComponent(String newName, String absDir, String orgPath, DataBinder orgData, Map<String, String> args, boolean isCleanup)
/*     */     throws DataException, ServiceException
/*     */   {
/* 420 */     String orgName = orgData.getLocal("ComponentName");
/* 421 */     updateComponentData(newName, orgName, absDir, orgData, isCleanup);
/* 422 */     if ((orgName == null) || (newName.equals(orgName)))
/*     */     {
/* 424 */       String absPath = absDir + FileUtils.getName(orgPath);
/* 425 */       return absPath;
/*     */     }
/*     */ 
/* 428 */     boolean isResDefChanged = false;
/* 429 */     boolean isMergeRuleChanged = false;
/* 430 */     boolean addSourcePath = true;
/*     */ 
/* 433 */     String absPath = absDir + newName + ".hda";
/* 434 */     String rootPath = "";
/* 435 */     String location = "";
/* 436 */     int index = absPath.indexOf("/" + newName + "/");
/* 437 */     if (index >= 0)
/*     */     {
/* 439 */       rootPath = absPath.substring(0, index + 1);
/*     */     }
/* 441 */     String intradocDir = SharedObjects.getEnvironmentValue("IntradocDir");
/* 442 */     if (absPath.startsWith(intradocDir))
/*     */     {
/* 444 */       location = absPath.substring(intradocDir.length(), absPath.length());
/* 445 */       if (ComponentLocationUtils.startWithStandardDirectories(location))
/*     */       {
/* 447 */         addSourcePath = false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 452 */     args.put("isNew", "0");
/* 453 */     args.put("absPath", absPath);
/* 454 */     IntradocComponent tempComp = new IntradocComponent();
/* 455 */     tempComp.init(newName, orgData.getLocalData(), args, true);
/* 456 */     tempComp.m_binder.m_localData = new Properties();
/*     */ 
/* 459 */     DataResultSet drset = tempComp.getResourceDefTable();
/*     */ 
/* 461 */     String expectedName = orgName.toLowerCase();
/* 462 */     FieldInfo[] info = ResultSetUtils.createInfoList(drset, IntradocComponent.RES_DEF_FIELD_INFO, true);
/*     */ 
/* 465 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 467 */       String type = ResultSetUtils.getValue(drset, info[0].m_name);
/* 468 */       String filename = ResultSetUtils.getValue(drset, info[1].m_name);
/* 469 */       String tables = ResultSetUtils.getValue(drset, info[2].m_name);
/* 470 */       String loadOrder = ResultSetUtils.getValue(drset, info[3].m_name);
/*     */ 
/* 473 */       String dir = FileUtils.getDirectory(filename);
/* 474 */       String fname = FileUtils.getName(filename);
/* 475 */       fname = fname.toLowerCase();
/* 476 */       String oldfname = filename;
/* 477 */       if (fname.startsWith(expectedName))
/*     */       {
/* 479 */         if ((dir != null) && (dir.length() > 0))
/*     */         {
/* 481 */           filename = FileUtils.directorySlashes(dir);
/*     */         }
/*     */         else
/*     */         {
/* 485 */           filename = "";
/*     */         }
/* 487 */         filename = filename + tempComp.m_name.toLowerCase() + fname.substring(expectedName.length(), fname.length());
/*     */ 
/* 490 */         FileUtils.renameFile(absDir + oldfname, absDir + filename);
/* 491 */         drset.setCurrentValue(info[1].m_index, filename);
/* 492 */         isResDefChanged = true;
/*     */       }
/*     */ 
/* 496 */       if ((tables != null) && (tables.length() > 0) && (!tables.equalsIgnoreCase("null")))
/*     */       {
/* 498 */         boolean isTablesChanged = false;
/* 499 */         Vector v = StringUtils.parseArray(tables, ',', '^');
/* 500 */         int size = v.size();
/* 501 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 503 */           String table = (String)v.elementAt(i);
/* 504 */           String tempTable = table.toLowerCase();
/* 505 */           if (!tempTable.startsWith(expectedName))
/*     */             continue;
/* 507 */           table = newName + table.substring(expectedName.length(), tempTable.length());
/* 508 */           v.setElementAt(table, i);
/* 509 */           isTablesChanged = true;
/*     */         }
/*     */ 
/* 513 */         if (isTablesChanged)
/*     */         {
/* 515 */           tables = StringUtils.createString(v, ',', '^');
/* 516 */           drset.setCurrentValue(info[2].m_index, tables);
/* 517 */           isResDefChanged = true;
/*     */         }
/*     */       }
/*     */ 
/* 521 */       ResourceFileInfo finfo = new ResourceFileInfo(tempComp.m_name, tempComp.m_absCompDir, type, tempComp.m_absCompDir + filename, tables, loadOrder);
/*     */ 
/* 523 */       finfo.renameResource(newName, expectedName);
/*     */     }
/*     */ 
/* 526 */     if (isResDefChanged)
/*     */     {
/* 528 */       tempComp.m_binder.addResultSet("ResourceDefinition", drset);
/*     */     }
/*     */ 
/* 532 */     drset = tempComp.getMergeRulesTable();
/* 533 */     info = ResultSetUtils.createInfoList(drset, IntradocComponent.MERGE_RULE_FIELD_INFO, true);
/*     */ 
/* 535 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 537 */       String fromTable = ResultSetUtils.getValue(drset, info[0].m_name);
/* 538 */       String tempTable = fromTable.toLowerCase();
/* 539 */       if (!tempTable.startsWith(expectedName))
/*     */         continue;
/* 541 */       fromTable = newName + fromTable.substring(expectedName.length(), tempTable.length());
/* 542 */       drset.setCurrentValue(info[0].m_index, fromTable);
/* 543 */       isMergeRuleChanged = true;
/*     */     }
/*     */ 
/* 548 */     if (isMergeRuleChanged)
/*     */     {
/* 550 */       tempComp.m_binder.addResultSet("MergeRules", drset);
/*     */     }
/*     */ 
/* 553 */     if ((isMergeRuleChanged) || (isResDefChanged))
/*     */     {
/* 555 */       CWizardUtils.writeFile(tempComp.m_absCompDir, tempComp.m_filename, tempComp.m_binder);
/*     */     }
/*     */ 
/* 559 */     DataBinder manBinder = new DataBinder();
/* 560 */     if (FileUtils.checkFile(absDir + "manifest.hda", true, false) != -16)
/*     */     {
/*     */       try
/*     */       {
/* 564 */         ResourceUtils.serializeDataBinder(absDir, "manifest.hda", manBinder, false, false);
/*     */ 
/* 566 */         manBinder.m_localData = new Properties();
/* 567 */         drset = (DataResultSet)manBinder.getResultSet("Manifest");
/* 568 */         if (drset != null)
/*     */         {
/* 570 */           FieldInfo[] fi = ResultSetUtils.createInfoList(drset, new String[] { "entryType", "location" }, true);
/* 571 */           for (drset.first(); drset.isRowPresent(); drset.next())
/*     */           {
/* 573 */             Vector v = drset.getCurrentRowValues();
/* 574 */             String entryType = (String)v.elementAt(fi[0].m_index);
/* 575 */             String loc = (String)v.elementAt(fi[1].m_index);
/* 576 */             String temploc = loc.toLowerCase();
/* 577 */             if ((entryType.equalsIgnoreCase("component")) && (loc.endsWith(orgName + ".hda")))
/*     */             {
/* 579 */               location = newName + "/" + newName + ".hda";
/* 580 */               v.setElementAt(location, fi[1].m_index);
/*     */             }
/* 582 */             else if (temploc.startsWith(expectedName))
/*     */             {
/* 584 */               location = newName + "/";
/* 585 */               if (loc.length() > expectedName.length())
/*     */               {
/* 587 */                 location = location + loc.substring(expectedName.length() + 1, loc.length());
/*     */               }
/*     */               else
/*     */               {
/* 591 */                 location = location + loc;
/*     */               }
/* 593 */               v.setElementAt(location, fi[1].m_index);
/*     */             }
/*     */ 
/* 596 */             if ((!addSourcePath) || ((!entryType.equalsIgnoreCase("component")) && (!entryType.equalsIgnoreCase("componentextra")) && (!entryType.equalsIgnoreCase("componentclasses")) && (!entryType.equalsIgnoreCase("componentlib"))))
/*     */             {
/*     */               continue;
/*     */             }
/*     */ 
/* 601 */             manBinder.putLocal(entryType.toLowerCase() + "@" + location + ".source", rootPath);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 606 */         ResourceUtils.serializeDataBinder(absDir, "manifest.hda", manBinder, true, false);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 610 */         throw new ServiceException(LocaleUtils.encodeMessage("csCompWizUnableToUpdateManifest", null, newName), e);
/*     */       }
/*     */     }
/* 613 */     return absPath;
/*     */   }
/*     */ 
/*     */   protected void updateComponentData(String newName, String orgName, String absDir, DataBinder cmpData, boolean isCleanup)
/*     */     throws ServiceException
/*     */   {
/* 620 */     if (isCleanup)
/*     */     {
/* 623 */       List newTags = new ArrayList();
/* 624 */       List sysTags = ComponentLocationUtils.getSystemTags();
/* 625 */       List tags = StringUtils.makeListFromSequenceSimple(cmpData.getLocal("componentTags"));
/*     */ 
/* 628 */       int size = tags.size();
/* 629 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 631 */         String tag = (String)tags.get(i);
/* 632 */         if (sysTags.contains(tag))
/*     */           continue;
/* 634 */         newTags.add(tag);
/*     */       }
/*     */ 
/* 637 */       String str = StringUtils.createStringSimple(newTags);
/* 638 */       cmpData.putLocal("componentTags", str);
/*     */     }
/*     */ 
/* 641 */     if (newName.equals(orgName))
/*     */     {
/* 643 */       ResourceUtils.serializeDataBinder(absDir, newName + ".hda", cmpData, true, true);
/*     */     }
/*     */     else
/*     */     {
/* 647 */       cmpData.putLocal("ComponentName", newName);
/* 648 */       ResourceUtils.serializeDataBinder(absDir, newName + ".hda", cmpData, true, false);
/* 649 */       FileUtils.deleteFile(absDir + orgName + ".hda");
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 661 */     String name = exchange.m_compName;
/* 662 */     String val = exchange.m_compValue;
/*     */ 
/* 664 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 669 */     IdcMessage errMsg = null;
/*     */ 
/* 671 */     if ((name.equals("name")) && (!this.m_useExisting.isSelected()))
/*     */     {
/* 673 */       errMsg = Validation.checkUrlFileSegmentForDB(val, "csCompWizLabelCompName", 0, null);
/*     */     }
/*     */ 
/* 677 */     if (errMsg != null)
/*     */     {
/* 679 */       exchange.m_errorMessage = errMsg;
/* 680 */       return false;
/*     */     }
/* 682 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 687 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80400 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.NewComponentDlg
 * JD-Core Version:    0.5.4
 */