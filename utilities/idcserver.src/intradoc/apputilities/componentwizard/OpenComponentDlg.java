/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainerUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.server.utils.ComponentListEditor;
/*     */ import intradoc.server.utils.ComponentLocationUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class OpenComponentDlg extends CWizardBaseDlg
/*     */   implements ActionListener
/*     */ {
/*  71 */   public static final String[][] COMPONENT_COL_MAP = { { "name", "!csCompWizLabelName", "30" }, { "status", "!csCompWizLabelStatus", "10" }, { "location", "!csCompWizLabelLocation", "50" }, { "componentType", "!csCompWizLabelTypes", "20" } };
/*     */ 
/*  79 */   protected UdlPanel m_componentList = null;
/*  80 */   protected ComboChoice m_inclusionTagChoice = null;
/*  81 */   protected ComboChoice m_exclusionTagChoice = null;
/*     */ 
/*  83 */   protected JCheckBox m_filterBox = null;
/*  84 */   protected Map<String, String> m_incFilteredTags = null;
/*  85 */   protected Map<String, String> m_exFilteredTags = null;
/*     */ 
/*  87 */   protected ComponentWizardManager m_manager = null;
/*  88 */   protected String m_currentOpenComponent = null;
/*  89 */   protected DataResultSet m_listData = null;
/*  90 */   protected boolean m_useExternalComponentList = false;
/*     */ 
/*  92 */   protected Vector m_controlBtns = new IdcVector();
/*  93 */   protected final String[][] EDIT_BUTTON_INFO = { { "csCompWizCommandAdd", "add", "0" }, { "csCompWizCommandListOpen", "open", "1" }, { "csCompWizCommandUnpackage", "install", "0" }, { "csCompWizCommandUninstall", "uninstall", "1" } };
/*     */ 
/* 101 */   protected final String[][] ENABLE_DISABLE_BUTTON_INFO = { { "csCompWizCommandEnable", "enable", "1" }, { "csCompWizCommandDisable", "disable", "1" }, { "csCompWizLabelHelp", "help", "0" } };
/*     */ 
/* 108 */   protected final String[][] STATUS_STRINGS = { { "Enabled", "csCompWizStatusEnabled" }, { "Disabled", "csCompWizStatusDisabled" } };
/*     */ 
/*     */   public OpenComponentDlg(SystemInterface sys, String title, String helpPage, ComponentWizardManager mgr)
/*     */   {
/* 117 */     super(sys, title, helpPage);
/* 118 */     this.m_manager = mgr;
/*     */ 
/* 120 */     this.m_incFilteredTags = new HashMap();
/* 121 */     this.m_exFilteredTags = new HashMap();
/*     */   }
/*     */ 
/*     */   public void init(String componentName, DataResultSet componentList)
/*     */   {
/* 126 */     String[] selectedComponents = { componentName };
/* 127 */     initUI();
/*     */     try
/*     */     {
/* 130 */       if (componentList != null)
/*     */       {
/* 132 */         this.m_listData = componentList;
/* 133 */         this.m_useExternalComponentList = true;
/*     */       }
/* 135 */       refreshList(selectedComponents);
/* 136 */       this.m_currentOpenComponent = componentName;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 140 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("csCompWizCompListRefreshError", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/* 148 */     this.m_componentList = createUdlPanel("!csCompWizLabelCustComp", 500, 10, "", true, COMPONENT_COL_MAP, COMPONENT_COL_MAP[0][0], false);
/*     */ 
/* 151 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 155 */         String[] selectedComponents = OpenComponentDlg.this.m_componentList.getSelectedObjs();
/*     */         try
/*     */         {
/* 158 */           OpenComponentDlg.this.updateInterfaceFromSelected(selectedComponents);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 162 */           CWizardGuiUtils.reportError(OpenComponentDlg.this.m_systemInterface, exp, IdcMessageFactory.lc("csCompWizCompListRefreshError", new Object[0]));
/*     */         }
/*     */       }
/*     */     };
/* 167 */     this.m_componentList.addItemListener(iListener);
/*     */ 
/* 169 */     ActionListener openListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 173 */         OpenComponentDlg.this.openComponent();
/*     */       }
/*     */     };
/* 176 */     this.m_componentList.m_list.addActionListener(openListener);
/*     */ 
/* 178 */     JPanel filterPanel = createFilterPanel();
/*     */ 
/* 180 */     JPanel btnPanel = new PanePanel();
/* 181 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/* 182 */     addCommandButtons(this.EDIT_BUTTON_INFO, btnPanel, this, true);
/*     */ 
/* 184 */     addCommandButtons(this.ENABLE_DISABLE_BUTTON_INFO, btnPanel, this, true);
/*     */ 
/* 187 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 188 */     mainPanel.setLayout(new BorderLayout());
/* 189 */     mainPanel.add("North", filterPanel);
/* 190 */     mainPanel.add("Center", this.m_componentList);
/* 191 */     mainPanel.add("East", btnPanel);
/*     */   }
/*     */ 
/*     */   public JPanel createFilterPanel()
/*     */   {
/* 196 */     ActionListener listener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 201 */         OpenComponentDlg.this.updateFilteredComponentList();
/*     */       }
/*     */     };
/* 205 */     JPanel btnPanel = new PanePanel();
/* 206 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 208 */     this.m_filterBox = new CustomCheckbox(this.m_systemInterface.getString("csCompWizEnableFilter"));
/* 209 */     this.m_helper.addLabelFieldPairEx(btnPanel, "", this.m_filterBox, "enableFilter", false);
/*     */ 
/* 211 */     JButton updateBtn = this.m_helper.addCommandButton(btnPanel, this.m_systemInterface.getString("csCompWizUpdateFilter"), "filter", listener);
/*     */ 
/* 213 */     updateBtn.setEnabled(false);
/*     */ 
/* 215 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(13);
/* 216 */     this.m_helper.m_gridHelper.addEmptyRowElement(btnPanel);
/*     */ 
/* 218 */     JPanel filterPanel = new CustomPanel();
/* 219 */     this.m_helper.makePanelGridBag(filterPanel, 2);
/*     */ 
/* 221 */     this.m_helper.addLastComponentInRow(filterPanel, btnPanel);
/*     */ 
/* 223 */     String label = this.m_systemInterface.localizeCaption("csCompWizIncFilterComponents");
/* 224 */     this.m_inclusionTagChoice = new ComboChoice(true);
/* 225 */     this.m_helper.addLabelFieldPair(filterPanel, label, this.m_inclusionTagChoice, "filterInclusion");
/*     */ 
/* 227 */     label = this.m_systemInterface.localizeCaption("csCompWizExclFilterComponents");
/* 228 */     this.m_exclusionTagChoice = new ComboChoice(true);
/* 229 */     this.m_helper.addLabelFieldPair(filterPanel, label, this.m_exclusionTagChoice, "filterExclusion");
/*     */ 
/* 231 */     ItemListener iListener = new ItemListener(updateBtn)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 235 */         boolean isChecked = e.getStateChange() == 1;
/* 236 */         this.val$updateBtn.setEnabled(isChecked);
/*     */ 
/* 238 */         OpenComponentDlg.this.updateFilteredComponentList();
/*     */       }
/*     */     };
/* 241 */     this.m_filterBox.addItemListener(iListener);
/*     */ 
/* 243 */     return filterPanel;
/*     */   }
/*     */ 
/*     */   public void updateFilteredComponentList()
/*     */   {
/*     */     try
/*     */     {
/* 250 */       boolean isChecked = this.m_filterBox.isSelected();
/* 251 */       String incTags = "";
/* 252 */       String exTags = "";
/* 253 */       if (isChecked)
/*     */       {
/* 255 */         incTags = this.m_inclusionTagChoice.getText();
/* 256 */         exTags = this.m_exclusionTagChoice.getText();
/*     */       }
/*     */ 
/* 259 */       List incTagList = StringUtils.makeListFromSequenceSimple(incTags);
/* 260 */       List exTagList = StringUtils.makeListFromSequenceSimple(exTags);
/*     */ 
/* 262 */       this.m_incFilteredTags = new HashMap();
/* 263 */       for (int i = 0; i < incTagList.size(); ++i)
/*     */       {
/* 265 */         String tag = (String)incTagList.get(i);
/* 266 */         this.m_incFilteredTags.put(tag, "1");
/*     */       }
/*     */ 
/* 269 */       this.m_exFilteredTags = new HashMap();
/* 270 */       for (int i = 0; i < exTagList.size(); ++i)
/*     */       {
/* 272 */         String tag = (String)exTagList.get(i);
/* 273 */         this.m_exFilteredTags.put(tag, "0");
/*     */       }
/*     */ 
/* 276 */       String[] selectedComponents = this.m_componentList.getSelectedObjs();
/* 277 */       refreshList(selectedComponents);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 281 */       if (Report.m_verbose)
/*     */       {
/* 283 */         Report.debug("componentwizard", "OpenComponentDlg.updateFilteredComponentListUnable: An error was encountered with tag filtering the component list.", e);
/*     */       }
/*     */ 
/* 287 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("csCompWizFilterError", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   public UdlPanel createUdlPanel(String listTitle, int width, int height, String resultSetName, boolean setVisibleColumns, String[][] columnMap, String idColumn, boolean forceVertical)
/*     */   {
/* 295 */     UdlPanel pnl = super.createUdlPanel(listTitle, width, height, resultSetName, setVisibleColumns, columnMap, idColumn, forceVertical);
/*     */ 
/* 297 */     pnl.m_list.setMultipleMode(true);
/*     */ 
/* 300 */     LocaleResources.localizeDoubleArray(this.STATUS_STRINGS, null, 1);
/*     */ 
/* 302 */     DisplayStringCallbackAdaptor stCallback = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 308 */         String displayStr = StringUtils.getPresentationString(OpenComponentDlg.this.STATUS_STRINGS, value);
/* 309 */         if (displayStr == null)
/*     */         {
/* 311 */           displayStr = "";
/*     */         }
/* 313 */         return displayStr;
/*     */       }
/*     */     };
/* 317 */     pnl.setDisplayCallback("status", stCallback);
/*     */ 
/* 319 */     this.m_listData = this.m_manager.getEditComponents();
/* 320 */     DisplayStringCallbackAdaptor locCallback = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String loc, Vector row)
/*     */       {
/* 326 */         String absPath = loc;
/* 327 */         Map map = OpenComponentDlg.this.changeToMap(row);
/* 328 */         String useType = (String)map.get("useType");
/* 329 */         boolean isHome = false;
/* 330 */         if (useType.length() == 0)
/*     */         {
/* 332 */           String typeStr = (String)map.get("componentType");
/* 333 */           List types = StringUtils.makeListFromSequenceSimple(typeStr);
/* 334 */           isHome = types.contains("home");
/*     */         }
/*     */ 
/* 337 */         if ((useType.equals("home")) || (isHome))
/*     */         {
/* 339 */           absPath = ComponentLocationUtils.determineComponentLocation(map, 2);
/*     */         }
/*     */         else
/*     */         {
/* 344 */           absPath = ComponentLocationUtils.determineComponentLocation(map, 1);
/*     */         }
/*     */ 
/* 348 */         if (absPath == null)
/*     */         {
/* 350 */           absPath = "";
/*     */         }
/* 352 */         return absPath;
/*     */       }
/*     */     };
/* 355 */     pnl.setDisplayCallback("location", locCallback);
/*     */ 
/* 357 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected Map changeToMap(Vector row)
/*     */   {
/* 362 */     Map map = new HashMap();
/* 363 */     int size = row.size();
/* 364 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 366 */       String name = this.m_listData.getFieldName(i);
/* 367 */       map.put(name, row.get(i));
/*     */     }
/*     */ 
/* 370 */     return map;
/*     */   }
/*     */ 
/*     */   protected void addCommandButtons(String[][] infos, JPanel btnPanel, ActionListener listener, boolean isControlBtn)
/*     */   {
/* 376 */     Insets stdIns = new Insets(5, 5, 5, 5);
/* 377 */     for (int i = 0; i < infos.length; ++i)
/*     */     {
/* 379 */       Insets ins = stdIns;
/* 380 */       if (i == 0)
/*     */       {
/* 382 */         ins = new Insets(30, 5, 5, 5);
/*     */       }
/* 384 */       this.m_helper.m_gridHelper.m_gc.insets = ins;
/*     */ 
/* 386 */       boolean isControlled = StringUtils.convertToBool(infos[i][2], false);
/* 387 */       JButton btn = this.m_componentList.addButton(LocaleResources.getString(infos[i][0], null), isControlled);
/* 388 */       this.m_helper.addLastComponentInRow(btnPanel, btn);
/*     */ 
/* 390 */       btn.setActionCommand(infos[i][1]);
/* 391 */       btn.addActionListener(this);
/* 392 */       if (!isControlBtn)
/*     */         continue;
/* 394 */       this.m_controlBtns.addElement(btn);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 404 */     String cmdStr = e.getActionCommand();
/*     */ 
/* 406 */     if (cmdStr.equals("add"))
/*     */     {
/* 408 */       addComponent();
/*     */     }
/* 410 */     else if (cmdStr.equals("install"))
/*     */     {
/* 412 */       installUninstallComponent("install", "csCompWizLabelUnpackage", "CW_Unpackage", "csCompWizUnpackageNoComponent", null);
/*     */     }
/* 415 */     else if (cmdStr.equals("uninstall"))
/*     */     {
/* 417 */       int[] indexes = this.m_componentList.getSelectedIndexes();
/* 418 */       if (indexes.length < 1)
/*     */       {
/* 420 */         CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizSelectComponent", new Object[0]));
/*     */ 
/* 422 */         return;
/*     */       }
/*     */ 
/* 425 */       Properties[] props = new Properties[indexes.length];
/* 426 */       for (int i = indexes.length - 1; i >= 0; --i)
/*     */       {
/* 428 */         props[i] = this.m_componentList.getDataAt(indexes[i]);
/*     */       }
/* 430 */       for (int i = 0; i < props.length; ++i)
/*     */       {
/* 432 */         installUninstallComponent("uninstall", "csCompWizLabelUninstall", "CW_Uninstall", "csCompWizUninstallNoComponent", props[i]);
/*     */       }
/*     */ 
/*     */     }
/* 436 */     else if (cmdStr.equals("open"))
/*     */     {
/* 438 */       openComponent();
/*     */     }
/* 440 */     else if (cmdStr.equals("enable"))
/*     */     {
/* 442 */       enableOrDisableComponent(true);
/*     */     }
/* 444 */     else if (cmdStr.equals("disable"))
/*     */     {
/* 446 */       enableOrDisableComponent(false);
/*     */     } else {
/* 448 */       if (!cmdStr.equals("help"))
/*     */         return;
/* 450 */       CWizardGuiUtils.launchHelp(this.m_systemInterface, "CW_ComponentList");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addComponent()
/*     */   {
/* 456 */     NewComponentDlg dlg = new NewComponentDlg(this.m_systemInterface, LocaleResources.getString("csCompWizLabelAddComp", null), null, this.m_manager);
/*     */ 
/* 459 */     dlg.init();
/* 460 */     if (dlg.prompt() != 1)
/*     */       return;
/* 462 */     DialogHelper dialogHelper = getDialogHelper();
/* 463 */     Properties props = dlg.getProperties();
/*     */ 
/* 465 */     dialogHelper.m_result = 1;
/* 466 */     boolean isHome = StringUtils.convertToBool(props.getProperty("isHome"), false);
/* 467 */     if (!isHome)
/*     */     {
/* 469 */       this.m_helper.m_props = props;
/* 470 */       dialogHelper.close();
/*     */     }
/*     */     else
/*     */     {
/* 474 */       IdcMessage msg = IdcMessageFactory.lc("csCompWizHomeComponentAdded", new Object[] { props.getProperty("name") });
/*     */ 
/* 476 */       CWizardGuiUtils.doMessage(this.m_systemInterface, null, msg, 1);
/*     */       try
/*     */       {
/* 479 */         this.m_helper.m_props = new Properties();
/* 480 */         this.m_component = null;
/* 481 */         this.m_manager.clearComponentInfo();
/* 482 */         refreshList(null);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 486 */         CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void installUninstallComponent(String installType, String title, String helpPage, String errMsg, Properties props)
/*     */   {
/* 495 */     IntradocComponent component = null;
/* 496 */     String name = null;
/* 497 */     boolean isLocalOnly = ComponentLocationUtils.isLocalOnly(props);
/* 498 */     if (installType.equals("uninstall"))
/*     */     {
/* 500 */       name = props.getProperty("name");
/* 501 */       String status = props.getProperty("status");
/* 502 */       boolean isLocal = ComponentLocationUtils.isLocal(props);
/* 503 */       if (!isLocal)
/*     */       {
/* 505 */         CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csAdminUninstallNonlocalComponentError", new Object[0]));
/*     */ 
/* 507 */         return;
/*     */       }
/*     */ 
/* 510 */       IdcMessage msg = null;
/* 511 */       if (name.equals(this.m_currentOpenComponent))
/*     */       {
/* 513 */         CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizCannotRemoveOpenComp", new Object[0]));
/*     */ 
/* 515 */         return;
/*     */       }
/*     */ 
/* 518 */       if (status.equalsIgnoreCase("enabled"))
/*     */       {
/* 520 */         if (isLocalOnly)
/*     */         {
/* 522 */           msg = IdcMessageFactory.lc("csCompWizRemoveEnabledCompPrompt", new Object[] { name });
/*     */         }
/*     */         else
/*     */         {
/* 528 */           msg = IdcMessageFactory.lc("csCompWizDisableCompPrompt", new Object[] { name });
/*     */         }
/*     */ 
/*     */       }
/* 533 */       else if (isLocalOnly)
/*     */       {
/* 535 */         msg = IdcMessageFactory.lc("csCompWizRemoveCompPrompt", new Object[] { name });
/*     */       }
/*     */       else
/*     */       {
/* 539 */         msg = IdcMessageFactory.lc("csCompWizDisableCompPrompt", new Object[] { name });
/*     */       }
/*     */ 
/* 543 */       if (CWizardGuiUtils.doMessage(this.m_systemInterface, null, msg, 4) != 2)
/*     */       {
/* 546 */         return;
/*     */       }
/*     */       try
/*     */       {
/* 550 */         FieldInfo[] fi = ResultSetUtils.createInfoList(this.m_listData, new String[] { "name", "location" }, true);
/*     */ 
/* 552 */         Vector v = this.m_listData.findRow(fi[0].m_index, name);
/* 553 */         if (v == null)
/*     */         {
/* 555 */           throw new ServiceException(LocaleUtils.encodeMessage("csCompWizCompNotFound", null, name));
/*     */         }
/*     */ 
/* 559 */         Map map = this.m_listData.getCurrentRowMap();
/* 560 */         Map args = new HashMap();
/* 561 */         args.put("isNew", "0");
/* 562 */         this.m_manager.initComponentInfo(name, map, args);
/* 563 */         component = this.m_manager.m_component;
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 567 */         if (isLocalOnly)
/*     */         {
/* 569 */           msg = IdcMessageFactory.lc("csCompWizCompNotFoundRemoveFromList", new Object[] { name });
/* 570 */           if (CWizardGuiUtils.doMessage(this.m_systemInterface, null, msg, 4) == 2)
/*     */           {
/*     */             try
/*     */             {
/* 575 */               this.m_manager.deleteComponent(props);
/* 576 */               refreshList(null);
/*     */             }
/*     */             catch (Exception exp)
/*     */             {
/* 580 */               CWizardGuiUtils.reportError(this.m_systemInterface, exp, (IdcMessage)null);
/*     */             }
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 586 */           msg = IdcMessageFactory.lc("csCompWizCompNotFound", new Object[] { name });
/* 587 */           CWizardGuiUtils.doMessage(this.m_systemInterface, null, msg, 1);
/*     */         }
/* 589 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 593 */     ManifestEditorDlg installerDlg = new ManifestEditorDlg(this.m_systemInterface, LocaleResources.getString(title, null), DialogHelpTable.getHelpPage(helpPage), component, this.m_manager);
/*     */ 
/* 596 */     installerDlg.init(installType);
/* 597 */     if (installerDlg.m_isError)
/*     */     {
/* 599 */       return;
/*     */     }
/* 601 */     IdcMessage msg = IdcMessageFactory.lc(errMsg, new Object[] { installerDlg.getFilePath() });
/*     */ 
/* 604 */     if ((installerDlg.m_isRemove) || (installerDlg.prompt() == 1))
/*     */     {
/* 606 */       if (installType.equals("install"))
/*     */       {
/*     */         try
/*     */         {
/* 610 */           refreshList(null);
/* 611 */           Vector v = installerDlg.getSucessfulComponents();
/* 612 */           if ((v == null) || (v.size() == 0))
/*     */           {
/* 614 */             CWizardGuiUtils.doMessage(this.m_systemInterface, null, msg, 1);
/*     */           }
/*     */           else
/*     */           {
/* 618 */             String compName = (String)v.elementAt(0);
/* 619 */             DataResultSet editComps = this.m_manager.getEditComponents();
/*     */ 
/* 621 */             if ((compName != null) && (compName.length() > 0) && (editComps != null) && (!editComps.isEmpty()))
/*     */             {
/* 624 */               openComponentEx(compName, editComps);
/*     */             }
/*     */             else
/*     */             {
/* 628 */               CWizardGuiUtils.doMessage(this.m_systemInterface, null, msg, 1);
/*     */             }
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 634 */           CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/*     */         try
/*     */         {
/* 641 */           if (isLocalOnly)
/*     */           {
/* 643 */             this.m_manager.deleteComponent(props);
/*     */           }
/*     */           else
/*     */           {
/* 647 */             msg = IdcMessageFactory.lc("csCompWizLocalComponentUninstalled", new Object[] { props.getProperty("name"), "csCompWizHomeCompExistsInList" });
/*     */ 
/* 649 */             CWizardGuiUtils.doMessage(this.m_systemInterface, null, msg, 1);
/* 650 */             this.m_manager.enableOrDisableComponent(props, false);
/*     */           }
/* 652 */           refreshList(null);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 656 */           CWizardGuiUtils.reportError(this.m_systemInterface, exp, (IdcMessage)null);
/* 657 */           return;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */       try
/*     */       {
/* 666 */         refreshList(null);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 670 */         CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */       }
/*     */   }
/*     */ 
/*     */   protected void openComponent()
/*     */   {
/* 677 */     int index = this.m_componentList.getSelectedIndex();
/* 678 */     if (index < 0)
/*     */     {
/* 681 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizSelectComponent", new Object[0]));
/*     */ 
/* 683 */       return;
/*     */     }
/*     */ 
/* 686 */     Properties props = this.m_componentList.getDataAt(index);
/* 687 */     this.m_helper.m_props = props;
/* 688 */     String name = props.getProperty("name");
/*     */ 
/* 690 */     String componentType = props.getProperty("componentType");
/* 691 */     if ((componentType.contains("home")) && (!componentType.contains("local")))
/*     */     {
/* 693 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizReadOnlyComponent", new Object[0]));
/*     */     }
/*     */     else
/*     */     {
/* 698 */       openComponentEx(name, this.m_listData);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void openComponentEx(String name, DataResultSet data)
/*     */   {
/*     */     try
/*     */     {
/* 706 */       FieldInfo[] fi = ResultSetUtils.createInfoList(data, new String[] { "name", "location" }, true);
/*     */ 
/* 708 */       Vector v = data.findRow(fi[0].m_index, name);
/* 709 */       if (v == null)
/*     */       {
/* 711 */         CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizCompNotFound", new Object[] { name }));
/*     */ 
/* 713 */         return;
/*     */       }
/*     */ 
/* 716 */       Map map = data.getCurrentRowMap();
/* 717 */       String location = (String)map.get("location");
/* 718 */       if (!location.endsWith(".hda"))
/*     */       {
/* 720 */         throw new ServiceException("!csCompWizComponentFileExtError");
/*     */       }
/*     */ 
/* 723 */       DialogHelper dialogHelper = getDialogHelper();
/* 724 */       Map args = new HashMap();
/* 725 */       args.put("isNew", "0");
/* 726 */       this.m_manager.initComponentInfo(name, map, args);
/*     */ 
/* 729 */       this.m_helper.m_props.putAll(map);
/* 730 */       this.m_helper.m_props.put("relativeLocation", map.get("location"));
/* 731 */       this.m_helper.m_props.put("absolutePath", this.m_manager.m_component.m_absLocation);
/* 732 */       this.m_helper.m_props.put("name", name);
/*     */ 
/* 734 */       dialogHelper.m_result = 1;
/* 735 */       dialogHelper.close();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 739 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("csCompWizCompOpenError2", new Object[] { name }));
/*     */ 
/* 741 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void enableOrDisableComponent(boolean enable) {
/* 746 */     IdcMessage errMsg = IdcMessageFactory.lc("csCompWizSelectCompToEnable", new Object[0]);
/*     */ 
/* 748 */     if (!enable)
/*     */     {
/* 750 */       errMsg = IdcMessageFactory.lc("csCompWizSelectCompToDisable", new Object[0]);
/*     */     }
/*     */ 
/* 753 */     int[] indexes = this.m_componentList.getSelectedIndexes();
/* 754 */     if (indexes.length < 1)
/*     */     {
/* 757 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, errMsg);
/* 758 */       return;
/*     */     }
/*     */ 
/* 761 */     String status = "csCompWizStatusDisabled";
/* 762 */     if (enable)
/*     */     {
/* 764 */       status = "csCompWizStatusEnabled";
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 769 */       for (int i = 0; i < indexes.length; ++i)
/*     */       {
/* 771 */         Properties props = this.m_componentList.getDataAt(indexes[i]);
/* 772 */         String name = props.getProperty("name");
/* 773 */         if (this.m_useExternalComponentList)
/*     */         {
/* 775 */           FieldInfo[] fi = ResultSetUtils.createInfoList(this.m_listData, new String[] { "name", "status" }, true);
/*     */ 
/* 777 */           this.m_listData.findRow(fi[0].m_index, name);
/* 778 */           this.m_listData.setCurrentValue(fi[1].m_index, LocaleResources.getString(status, null));
/*     */         }
/*     */ 
/* 781 */         this.m_manager.enableOrDisableComponent(props, enable);
/*     */       }
/* 783 */       refreshList(null);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 787 */       CWizardGuiUtils.reportError(this.m_systemInterface, exp, (IdcMessage)null);
/* 788 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void refreshList(String[] selectedComponents) throws DataException, ServiceException
/*     */   {
/* 794 */     if (!this.m_useExternalComponentList)
/*     */     {
/* 796 */       this.m_manager.m_editor.loadComponents();
/* 797 */       this.m_listData = this.m_manager.getEditComponents();
/*     */     }
/* 799 */     if (this.m_listData == null)
/*     */     {
/* 801 */       return;
/*     */     }
/* 803 */     DataResultSet drset = new DataResultSet();
/* 804 */     drset.copyFieldInfo(this.m_listData);
/*     */ 
/* 812 */     Table table = ResourceContainerUtils.getDynamicTableResource("LegacyTaggedComponents");
/* 813 */     DataResultSet legacyTagSet = new DataResultSet();
/* 814 */     legacyTagSet.init(table);
/*     */ 
/* 817 */     int lNameIndex = ResultSetUtils.getIndexMustExist(legacyTagSet, "componentName");
/* 818 */     int lTagIndex = ResultSetUtils.getIndexMustExist(legacyTagSet, "tags");
/*     */ 
/* 821 */     int index = ResultSetUtils.getIndexMustExist(drset, "location");
/* 822 */     for (this.m_listData.first(); this.m_listData.isRowPresent(); this.m_listData.next())
/*     */     {
/* 824 */       Map map = this.m_listData.getCurrentRowMap();
/* 825 */       String name = (String)map.get("name");
/* 826 */       String location = (String)map.get("location");
/* 827 */       this.m_listData.setCurrentValue(index, location);
/*     */ 
/* 829 */       boolean isExcluded = false;
/* 830 */       boolean isIncluded = false;
/* 831 */       if ((this.m_incFilteredTags == null) || (this.m_incFilteredTags.size() == 0))
/*     */       {
/* 833 */         isIncluded = true;
/*     */       }
/*     */ 
/* 836 */       String tagStr = (String)map.get("componentTags");
/* 837 */       if (tagStr.length() == 0)
/*     */       {
/* 840 */         Vector row = legacyTagSet.findRow(lNameIndex, name);
/* 841 */         if (row != null)
/*     */         {
/* 843 */           tagStr = legacyTagSet.getStringValue(lTagIndex);
/*     */         }
/*     */       }
/* 846 */       List tags = StringUtils.makeListFromSequenceSimple(tagStr);
/* 847 */       int num = tags.size();
/* 848 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 851 */         String tag = (String)tags.get(i);
/* 852 */         String incTag = (String)this.m_incFilteredTags.get(tag);
/* 853 */         String exTag = (String)this.m_exFilteredTags.get(tag);
/* 854 */         if (exTag != null)
/*     */         {
/* 856 */           isExcluded = true;
/*     */         }
/* 858 */         if (incTag == null)
/*     */           continue;
/* 860 */         isIncluded = true;
/*     */       }
/*     */ 
/* 863 */       if ((!isIncluded) || (isExcluded))
/*     */         continue;
/* 865 */       drset.addRow(this.m_listData.getCurrentRowValues());
/*     */     }
/*     */ 
/* 869 */     List tagList = CWizardUtils.createTagsList(this.m_listData, true);
/* 870 */     this.m_inclusionTagChoice.initChoiceList(tagList);
/* 871 */     this.m_exclusionTagChoice.initChoiceList(tagList);
/*     */ 
/* 874 */     ResultSetUtils.sortResultSet(drset, new String[] { "name" });
/* 875 */     if (null == selectedComponents)
/*     */     {
/* 877 */       this.m_componentList.m_list.deselectAll();
/*     */     }
/* 879 */     this.m_componentList.refreshListEx(drset, selectedComponents);
/* 880 */     updateInterfaceFromSelected(selectedComponents);
/*     */   }
/*     */ 
/*     */   protected void updateInterfaceFromSelected(String[] selectedComponents)
/*     */     throws DataException, ServiceException
/*     */   {
/* 886 */     this.m_componentList.enableDisable(null != selectedComponents);
/* 887 */     if (null == selectedComponents)
/*     */     {
/* 889 */       String[] noneSelected = new String[0];
/* 890 */       selectedComponents = noneSelected;
/*     */     }
/* 892 */     enableDisableButtons(selectedComponents);
/*     */   }
/*     */ 
/*     */   protected void enableDisableButtons(String[] selectedComponents)
/*     */     throws DataException, ServiceException
/*     */   {
/* 898 */     FieldInfo[] info = ResultSetUtils.createInfoList(this.m_listData, new String[] { "name", "location", "status" }, true);
/*     */ 
/* 900 */     int selectedCount = selectedComponents.length;
/* 901 */     boolean hasDisabled = false; boolean hasEnabled = false;
/*     */ 
/* 903 */     for (int i = 0; i < selectedCount; ++i)
/*     */     {
/* 905 */       Vector v = this.m_listData.findRow(info[0].m_index, selectedComponents[i]);
/* 906 */       if (null == v) {
/*     */         continue;
/*     */       }
/*     */ 
/* 910 */       String status = (String)v.elementAt(info[2].m_index);
/* 911 */       if (status.equalsIgnoreCase("enabled"))
/*     */       {
/* 913 */         hasEnabled = true;
/*     */       }
/*     */       else
/*     */       {
/* 917 */         hasDisabled = true;
/*     */       }
/*     */     }
/*     */ 
/* 921 */     for (int i = 0; i < this.m_controlBtns.size(); ++i)
/*     */     {
/* 923 */       JButton btn = (JButton)this.m_controlBtns.elementAt(i);
/* 924 */       String cmd = btn.getActionCommand();
/* 925 */       if (cmd.equals("enable"))
/*     */       {
/* 927 */         btn.setEnabled(hasDisabled);
/*     */       }
/* 929 */       if (cmd.equals("disable"))
/*     */       {
/* 931 */         btn.setEnabled(hasEnabled);
/*     */       }
/* 933 */       if (cmd.equals("open"))
/*     */       {
/* 935 */         btn.setEnabled(selectedCount == 1);
/*     */       }
/* 937 */       if (!cmd.equals("uninstall"))
/*     */         continue;
/* 939 */       btn.setEnabled(selectedCount > 0);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getComponentName()
/*     */   {
/* 946 */     return this.m_helper.m_props.getProperty("name");
/*     */   }
/*     */ 
/*     */   public Properties getComponentProperties() {
/* 950 */     return this.m_helper.m_props;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 955 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.OpenComponentDlg
 * JD-Core Version:    0.5.4
 */