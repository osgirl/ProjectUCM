/*      */ package intradoc.apputilities.componentwizard;
/*      */ 
/*      */ import intradoc.apps.shared.MainFrame;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.GuiUtils;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.gui.AppFrameHelper;
/*      */ import intradoc.gui.CustomLabel;
/*      */ import intradoc.gui.GridBagHelper;
/*      */ import intradoc.gui.PanePanel;
/*      */ import intradoc.gui.TabPanel;
/*      */ import intradoc.resource.ComponentData;
/*      */ import intradoc.server.ComponentLoader;
/*      */ import intradoc.server.DataLoader;
/*      */ import intradoc.server.IdcSystemLoader;
/*      */ import intradoc.server.utils.CompInstallUtils;
/*      */ import intradoc.server.utils.ComponentPreferenceData;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.DialogHelpTable;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import java.awt.BorderLayout;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.awt.event.WindowAdapter;
/*      */ import java.awt.event.WindowEvent;
/*      */ import java.util.HashMap;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.swing.JButton;
/*      */ import javax.swing.JMenu;
/*      */ import javax.swing.JMenuBar;
/*      */ import javax.swing.JMenuItem;
/*      */ import javax.swing.JPanel;
/*      */ 
/*      */ public class CWizardFrame extends MainFrame
/*      */ {
/*      */   protected CWizardPanel[] m_tabPanels;
/*      */   protected JButton m_launchEditorBtn;
/*      */   protected JMenu m_optMenu;
/*      */   protected JMenu m_buildMenu;
/*      */   protected ComponentWizardManager m_manager;
/*      */   protected String m_currentCompName;
/*      */   protected String m_currentCompLocation;
/*      */   protected boolean m_isComponentRefreshed;
/*      */   protected String m_previousCompName;
/*      */   protected final String[][] PANEL_INFOS;
/*      */ 
/*      */   public CWizardFrame()
/*      */   {
/*   77 */     this.m_tabPanels = null;
/*   78 */     this.m_launchEditorBtn = null;
/*   79 */     this.m_optMenu = null;
/*   80 */     this.m_buildMenu = null;
/*      */ 
/*   82 */     this.m_manager = null;
/*   83 */     this.m_currentCompName = null;
/*   84 */     this.m_currentCompLocation = null;
/*   85 */     this.m_isComponentRefreshed = false;
/*   86 */     this.m_previousCompName = null;
/*      */ 
/*   89 */     this.PANEL_INFOS = new String[][] { { "ResourceDefPanel", "intradoc.apputilities.componentwizard.ResourceDefPanel", "csCompWizLabelResourceDef" }, { "JavaCodePanel", "intradoc.apputilities.componentwizard.JavaCodePanel", "csCompWizLabelJavaCodeDef" }, { "InstallPanel", "intradoc.apputilities.componentwizard.InstallPanel", "csCompWizLabelInstall" } };
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void init(String title, boolean exitOnClose)
/*      */     throws ServiceException
/*      */   {
/*  101 */     IdcMessage msg = null;
/*  102 */     if (title != null)
/*      */     {
/*  104 */       msg = IdcMessageFactory.lc();
/*  105 */       msg.m_msgEncoded = title;
/*      */     }
/*  107 */     init(msg, exitOnClose);
/*      */   }
/*      */ 
/*      */   public void init(IdcMessage title, boolean exitOnClose)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  115 */       super.init(title, exitOnClose);
/*  116 */       this.m_appHelper.attachToAppFrame(this, null, null, title);
/*      */ 
/*  118 */       SharedObjects.putEnvironmentValue("IgnoreComponentLoadError", "1");
/*      */ 
/*  120 */       IdcSystemLoader.initComponentData();
/*      */ 
/*  122 */       IdcSystemLoader.loadComponentData();
/*      */ 
/*  124 */       IdcSystemLoader.loadIdocScriptExtensions();
/*      */ 
/*  127 */       loadIdcQueries();
/*  128 */       loadIdcServices();
/*      */ 
/*  130 */       this.m_manager = new ComponentWizardManager();
/*  131 */       this.m_manager.init();
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  135 */       throw new ServiceException("!csCompWizStartError", e);
/*      */     }
/*      */ 
/*  139 */     initUI();
/*  140 */     buildMenu();
/*      */ 
/*  142 */     pack();
/*  143 */     setVisible(true);
/*      */ 
/*  145 */     openOrAddComponent(true, "!csCompWizLabelCompList", null);
/*      */ 
/*  149 */     WindowAdapter winAdapt = new WindowAdapter()
/*      */     {
/*      */       public void windowActivated(WindowEvent e)
/*      */       {
/*  154 */         if (CWizardFrame.this.m_isComponentRefreshed)
/*      */           return;
/*      */         try
/*      */         {
/*  158 */           boolean reloadAll = false;
/*  159 */           if (CWizardFrame.this.neededReloadComponentDefFile())
/*      */           {
/*  161 */             Map map = (Map)CWizardFrame.this.m_appHelper.m_props.clone();
/*  162 */             map.put("location", CWizardFrame.this.m_currentCompLocation);
/*      */ 
/*  164 */             Map args = new HashMap();
/*  165 */             args.put("isNew", "0");
/*  166 */             CWizardFrame.this.m_manager.initComponentInfo(CWizardFrame.this.m_currentCompName, map, args);
/*  167 */             reloadAll = true;
/*      */           }
/*  169 */           CWizardFrame.this.refreshComponentInfoData(CWizardFrame.this.m_currentCompName, reloadAll, false);
/*  170 */           CWizardFrame.this.m_isComponentRefreshed = true;
/*      */         }
/*      */         catch (Exception ignore)
/*      */         {
/*  174 */           Report.trace("system", null, ignore);
/*      */         }
/*      */       }
/*      */ 
/*      */       public void windowDeactivated(WindowEvent e)
/*      */       {
/*  182 */         CWizardFrame.this.m_isComponentRefreshed = false;
/*      */       }
/*      */     };
/*  186 */     addWindowListener(winAdapt);
/*      */ 
/*  188 */     setIconImage(GuiUtils.getAppImage("componentwizard.gif"));
/*      */   }
/*      */ 
/*      */   protected void initUI()
/*      */     throws ServiceException
/*      */   {
/*  194 */     JPanel mainPanel = this.m_appHelper.m_mainPanel;
/*      */ 
/*  200 */     JPanel topPanelOne = addGeneralInfoPanelOne();
/*  201 */     JPanel topPanelTwo = addGeneralInfoPanelTwo();
/*  202 */     JPanel infoPanel = initInfoPanel();
/*      */ 
/*  204 */     this.m_appHelper.makePanelGridBag(mainPanel, 1);
/*  205 */     this.m_appHelper.m_gridHelper.m_gc.weightx = 1.0D;
/*  206 */     this.m_appHelper.m_gridHelper.m_gc.weighty = 0.0D;
/*  207 */     this.m_appHelper.addComponent(mainPanel, topPanelOne);
/*  208 */     this.m_appHelper.addLastComponentInRow(mainPanel, topPanelTwo);
/*  209 */     this.m_appHelper.m_gridHelper.m_gc.weighty = 1.0D;
/*  210 */     this.m_appHelper.addLastComponentInRow(mainPanel, infoPanel);
/*      */   }
/*      */ 
/*      */   protected void buildMenu()
/*      */   {
/*  215 */     String[][] OPTIONS_MENU_INFOS = { { "csCompWizCommandAdd", "add", "1" }, { "csCompWizCommandOpen", "open", "1" }, { "csCompWizCommandClose", "close", "0" }, { "csCompWizCommandUnpackage", "install", "1" }, { "separator", "", "" }, { "csCompWizCommandEnable", "enable", "0" }, { "csCompWizCommandDisable", "disable", "0" }, { "csCompWizCommandConfig", "config", "0" }, { "csCompWizCommandEditReadme", "editReadme", "0" }, { "separator", "", "" }, { "csCompWizCommandSetEditor", "seteditor", "1" }, { "separator", "", "" }, { "csCompWizCommandExit", "exit", "1" } };
/*      */ 
/*  234 */     String[][] BUILD_MENU_INFOS = { { "csCompWizBuildMenuLabelSettings", "buildSettings", "0" }, { "csCompWizBuildMenuLabelBuild", "build", "0" } };
/*      */ 
/*  240 */     String[][] BUILD_MENU_PATCH_INFO = { { "csCompWizBuildMenuLabelPatch", "patchSettings", "0" } };
/*      */ 
/*  245 */     ActionListener menuListener = createMenuListener();
/*  246 */     JMenuBar mb = new JMenuBar();
/*  247 */     setJMenuBar(mb);
/*      */ 
/*  249 */     this.m_optMenu = new JMenu(LocaleResources.getString("csCompWizLabelOptMenu", null));
/*  250 */     addMenuItems(this.m_optMenu, OPTIONS_MENU_INFOS, menuListener);
/*  251 */     mb.add(this.m_optMenu);
/*      */ 
/*  253 */     this.m_buildMenu = new JMenu(LocaleResources.getString("csCompWizLabelBuildMenu", null));
/*  254 */     addMenuItems(this.m_buildMenu, BUILD_MENU_INFOS, menuListener);
/*  255 */     if (SharedObjects.getEnvValueAsBoolean("EnablePatchSettingsMenu", false))
/*      */     {
/*  257 */       addMenuItems(this.m_buildMenu, BUILD_MENU_PATCH_INFO, null);
/*      */     }
/*  259 */     mb.add(this.m_buildMenu);
/*  260 */     addHelpMenu(mb);
/*      */   }
/*      */ 
/*      */   public void addHelpMenu(JMenuBar mb)
/*      */   {
/*  266 */     JMenu helpMenu = new JMenu(LocaleResources.getString("csCompWizLabelHelp", null));
/*  267 */     mb.add(helpMenu);
/*      */ 
/*  269 */     ActionListener listener = new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*  273 */         String command = e.getActionCommand();
/*      */ 
/*  275 */         if (command.equals("contents"))
/*      */         {
/*  277 */           CWizardFrame.this.displayHelp(DialogHelpTable.getHelpPage("CW_Start"));
/*      */         }
/*      */         else
/*      */         {
/*  281 */           CWizardFrame.this.displayAboutInfo();
/*      */         }
/*      */       }
/*      */     };
/*  288 */     JMenuItem mi1 = new JMenuItem(LocaleResources.getString("csCompWizLabelContents", null));
/*  289 */     mi1.addActionListener(listener);
/*  290 */     mi1.setActionCommand("contents");
/*  291 */     helpMenu.add(mi1);
/*      */ 
/*  294 */     JMenuItem mi2 = new JMenuItem(LocaleResources.getString("csCompWizLabelAbout", null));
/*  295 */     mi2.addActionListener(listener);
/*  296 */     mi2.setActionCommand("about");
/*  297 */     helpMenu.add(mi2);
/*      */   }
/*      */ 
/*      */   protected void addMenuItems(JMenu menu, String[][] menuDef, ActionListener listener)
/*      */   {
/*  302 */     for (int i = 0; i < menuDef.length; ++i)
/*      */     {
/*  304 */       String menuStr = LocaleResources.getString(menuDef[i][0], null);
/*  305 */       boolean setEnabled = StringUtils.convertToBool(menuDef[i][2], false);
/*  306 */       if (menuStr == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  311 */       if (menuStr.equalsIgnoreCase("separator"))
/*      */       {
/*  313 */         menu.addSeparator();
/*      */       }
/*      */       else
/*      */       {
/*  317 */         JMenuItem mi = new JMenuItem(menuStr);
/*  318 */         mi.setActionCommand(menuDef[i][1]);
/*  319 */         mi.setEnabled(setEnabled);
/*  320 */         mi.addActionListener(listener);
/*  321 */         menu.add(mi);
/*      */       }
/*      */     }
/*  324 */     menu.addActionListener(listener);
/*      */   }
/*      */ 
/*      */   protected JPanel initInfoPanel() throws ServiceException
/*      */   {
/*  329 */     TabPanel tab = new TabPanel();
/*  330 */     IntradocComponent comp = null;
/*      */ 
/*  332 */     int numPanels = this.PANEL_INFOS.length;
/*  333 */     this.m_tabPanels = new CWizardPanel[numPanels];
/*  334 */     for (int i = 0; i < numPanels; ++i)
/*      */     {
/*  336 */       this.m_tabPanels[i] = ((CWizardPanel)ComponentClassFactory.createClassInstance(this.PANEL_INFOS[i][0], this.PANEL_INFOS[i][1], LocaleUtils.encodeMessage("csCompWizLoadPanelError", null, this.PANEL_INFOS[i][0])));
/*      */ 
/*  339 */       this.m_tabPanels[i].init(this.m_appHelper, comp);
/*  340 */       tab.addPane(LocaleResources.getString(this.PANEL_INFOS[i][2], null), this.m_tabPanels[i]);
/*      */     }
/*  342 */     return tab;
/*      */   }
/*      */ 
/*      */   protected JPanel addGeneralInfoPanelOne()
/*      */   {
/*  348 */     JPanel eastPnl = new PanePanel();
/*  349 */     this.m_appHelper.makePanelGridBag(eastPnl, 0);
/*  350 */     this.m_appHelper.addLabelDisplayPair(eastPnl, LocaleResources.getString("csCompWizLabelName2", null), 400, "name");
/*      */ 
/*  352 */     this.m_appHelper.m_gridHelper.prepareAddRowElement(13);
/*  353 */     this.m_appHelper.addComponent(eastPnl, new CustomLabel(LocaleResources.getString("csCompWizLabelLocation2", null), 1));
/*      */ 
/*  355 */     this.m_appHelper.m_gridHelper.prepareAddRowElement(17);
/*  356 */     LongTextCustomLabel cl = new LongTextCustomLabel();
/*  357 */     cl.setMinWidth(400);
/*  358 */     cl.getClass(); cl.setBreakValue(-1);
/*  359 */     this.m_appHelper.m_gridHelper.m_gc.fill = 2;
/*  360 */     this.m_appHelper.addExchangeComponent(eastPnl, cl, "absolutePath");
/*  361 */     return eastPnl;
/*      */   }
/*      */ 
/*      */   protected JPanel addGeneralInfoPanelTwo()
/*      */   {
/*  366 */     JPanel westPnl = new PanePanel();
/*  367 */     this.m_appHelper.makePanelGridBag(westPnl, 0);
/*  368 */     this.m_appHelper.addLabelDisplayPair(westPnl, LocaleResources.getString("csCompWizLabelStatus2", null), 100, "lcStatus");
/*      */ 
/*  370 */     ActionListener launchListener = new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*  374 */         String absPath = CWizardFrame.this.m_manager.m_component.m_absLocation;
/*  375 */         CWizardGuiUtils.launchEditor(CWizardFrame.this.m_appHelper, absPath);
/*      */       }
/*      */     };
/*  378 */     JPanel btnPanel = new PanePanel();
/*  379 */     btnPanel.setLayout(new BorderLayout());
/*  380 */     this.m_launchEditorBtn = new JButton(LocaleResources.getString("csCompWizLanchEditor", null));
/*  381 */     this.m_launchEditorBtn.addActionListener(launchListener);
/*  382 */     btnPanel.add("West", this.m_launchEditorBtn);
/*  383 */     this.m_launchEditorBtn.setEnabled(false);
/*  384 */     this.m_appHelper.m_gridHelper.addEmptyRowElement(westPnl);
/*  385 */     this.m_appHelper.addComponent(westPnl, btnPanel);
/*      */ 
/*  387 */     return westPnl;
/*      */   }
/*      */ 
/*      */   protected void loadIdcQueries()
/*      */   {
/*  392 */     loadTable(ComponentLoader.m_queries, new String[] { "name", "queryStr", "parameters" }, "IdcQueries", "name");
/*      */   }
/*      */ 
/*      */   protected void loadIdcServices()
/*      */   {
/*  398 */     loadTable(ComponentLoader.m_services, new String[] { "Name", "Attributes", "Actions" }, "IdcServices", "Name");
/*      */   }
/*      */ 
/*      */   protected void loadTable(Vector list, String[] fields, String tablename, String col)
/*      */   {
/*      */     try
/*      */     {
/*  406 */       ResourceContainer res = new ResourceContainer();
/*  407 */       int num = list.size();
/*      */ 
/*  409 */       DataResultSet mergedTable = new DataResultSet(fields);
/*      */ 
/*  411 */       for (int i = 0; i < num; ++i)
/*      */       {
/*  413 */         ComponentData data = (ComponentData)list.elementAt(i);
/*  414 */         String filePath = data.m_file;
/*      */ 
/*  416 */         DataLoader.cacheResourceFile(res, filePath);
/*      */ 
/*  418 */         Vector tables = data.m_tables;
/*  419 */         int numTables = tables.size();
/*  420 */         for (int j = 0; j < numTables; ++j)
/*      */         {
/*  422 */           String tableName = (String)tables.elementAt(j);
/*  423 */           Table tble = res.getTable(tableName);
/*      */ 
/*  425 */           if (tble == null)
/*      */           {
/*  427 */             throw new DataException(LocaleUtils.encodeMessage("csCompWizTableNotFound", null, tableName));
/*      */           }
/*      */ 
/*  431 */           DataResultSet copySet = new DataResultSet();
/*  432 */           copySet.init(tble);
/*  433 */           mergedTable.merge(col, copySet, false);
/*      */         }
/*      */       }
/*      */ 
/*  437 */       SharedObjects.putTable(tablename, mergedTable);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  442 */       Report.trace(null, null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void refreshComponentInfoData(String componentName, boolean reloadAll, boolean displayError) throws ServiceException, DataException
/*      */   {
/*  448 */     String status = null;
/*  449 */     if (componentName == null)
/*      */     {
/*  451 */       status = "Disabled";
/*  452 */       this.m_manager.m_component = null;
/*      */     }
/*      */     else
/*      */     {
/*  456 */       if (this.m_manager.m_component == null)
/*      */       {
/*  458 */         if (displayError)
/*      */         {
/*  460 */           CWizardGuiUtils.reportError(this.m_appHelper, null, IdcMessageFactory.lc("csCompWizCompInfoNotFound", new Object[0]));
/*      */         }
/*  462 */         return;
/*      */       }
/*      */ 
/*  466 */       status = this.m_appHelper.m_props.getProperty("status");
/*  467 */       DataResultSet drset = this.m_manager.getEditComponents();
/*  468 */       Vector v = drset.findRow(0, componentName);
/*  469 */       String newStatus = (String)v.elementAt(2);
/*      */ 
/*  471 */       if (!newStatus.equals(status))
/*      */       {
/*  473 */         status = newStatus;
/*      */       }
/*      */ 
/*  476 */       String lcStatus = null;
/*  477 */       if ((status != null) && (status.equalsIgnoreCase("enabled")))
/*      */       {
/*  479 */         status = "Enabled";
/*  480 */         lcStatus = "csCompWizStatusEnabled";
/*      */       }
/*      */       else
/*      */       {
/*  484 */         status = "Disabled";
/*  485 */         lcStatus = "csCompWizStatusDisabled";
/*      */       }
/*      */ 
/*  488 */       this.m_appHelper.m_props.put("lcStatus", LocaleResources.getString(lcStatus, null));
/*      */ 
/*  491 */       if ((!reloadAll) && (!isComponentInfoReloadNeeded(this.m_manager.m_component)))
/*      */       {
/*  493 */         setEnableDisableMenuItems(status);
/*  494 */         this.m_appHelper.loadComponentValues();
/*  495 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  499 */     IdcMessage firstMessage = null;
/*  500 */     IdcMessage errMsg = null;
/*  501 */     IdcMessage tempMsg = null;
/*  502 */     for (int i = 0; i < this.m_tabPanels.length; ++i)
/*      */     {
/*  504 */       tempMsg = this.m_tabPanels[i].assignComponentInfo(this.m_manager.m_component, reloadAll, null);
/*  505 */       if (tempMsg == null)
/*      */         continue;
/*  507 */       if (errMsg == null)
/*      */       {
/*  509 */         firstMessage = errMsg = tempMsg;
/*      */       }
/*      */       else
/*      */       {
/*  513 */         errMsg.m_prior = tempMsg;
/*  514 */         errMsg = tempMsg;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  519 */     if ((errMsg != null) && (displayError))
/*      */     {
/*  521 */       CWizardGuiUtils.reportError(this.m_appHelper, null, IdcMessageFactory.lc("csCorrectErrors", new Object[] { firstMessage }));
/*      */     }
/*      */ 
/*  525 */     String title = null;
/*  526 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*      */ 
/*  528 */     if ((idcName != null) && (idcName.length() > 0))
/*      */     {
/*  530 */       if (componentName == null)
/*      */       {
/*  532 */         title = LocaleUtils.encodeMessage("csCompWizTitleNoComponent2", null, LocaleResources.getString("csCompWizTitle", null), idcName);
/*      */       }
/*      */       else
/*      */       {
/*  536 */         title = LocaleUtils.encodeMessage("csCompWizTitle2", null, componentName, idcName);
/*      */       }
/*      */ 
/*      */     }
/*  541 */     else if (componentName == null)
/*      */     {
/*  543 */       title = LocaleUtils.encodeMessage("csCompWizTitleNoComponent1", null, LocaleResources.getString("csCompWizTitle", null));
/*      */     }
/*      */     else
/*      */     {
/*  547 */       title = LocaleUtils.encodeMessage("csCompWizTitle1", null, componentName);
/*      */     }
/*      */ 
/*  551 */     setTitle(LocaleResources.localizeMessage(title, null));
/*      */ 
/*  553 */     if (componentName == null)
/*      */     {
/*  555 */       this.m_launchEditorBtn.setEnabled(false);
/*      */     }
/*      */     else
/*      */     {
/*  559 */       this.m_launchEditorBtn.setEnabled(true);
/*  560 */       this.m_previousCompName = componentName;
/*      */     }
/*  562 */     setEnableDisableMenuItems(status);
/*  563 */     if (this.m_appHelper.m_props == null)
/*      */       return;
/*  565 */     this.m_appHelper.loadComponentValues();
/*      */   }
/*      */ 
/*      */   protected void setEnableDisableMenuItems(String status)
/*      */   {
/*  571 */     boolean isEnabled = true;
/*  572 */     if (status.equalsIgnoreCase("enabled"))
/*      */     {
/*  574 */       isEnabled = false;
/*      */     }
/*      */ 
/*  577 */     enableDisableMenuItems(this.m_optMenu, isEnabled);
/*  578 */     enableDisableMenuItems(this.m_buildMenu, isEnabled);
/*      */   }
/*      */ 
/*      */   protected void enableDisableMenuItems(JMenu menu, boolean isEnabled)
/*      */   {
/*  583 */     int numItems = menu.getItemCount();
/*  584 */     for (int i = 0; i < numItems; ++i)
/*      */     {
/*  586 */       JMenuItem mi = menu.getItem(i);
/*  587 */       if (mi == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  592 */       String actCommand = mi.getActionCommand();
/*      */ 
/*  594 */       if (this.m_manager.m_component == null)
/*      */         continue;
/*  596 */       if (actCommand.equals("enable"))
/*      */       {
/*  598 */         mi.setEnabled(isEnabled);
/*      */       }
/*  600 */       else if (actCommand.equals("disable"))
/*      */       {
/*  602 */         mi.setEnabled(!isEnabled);
/*      */       }
/*  604 */       else if ((actCommand.equals("config")) && (this.m_manager.m_component.m_binder != null) && (StringUtils.convertToBool(this.m_manager.m_component.m_binder.getLocal("hasPreferenceData"), false)) && (this.m_manager.m_component.m_prefData != null) && (this.m_manager.m_component.m_prefData.hasPostInstallPrefs()))
/*      */       {
/*  609 */         mi.setEnabled(true);
/*      */       } else {
/*  611 */         if ((!actCommand.equals("buildSettings")) && (!actCommand.equals("close")) && (!actCommand.equals("build")) && (!actCommand.equals("patchSettings")) && (!actCommand.equals("editReadme")))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  617 */         mi.setEnabled(true);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void setMenuItems(boolean isEnabled)
/*      */   {
/*  626 */     setMenuItemStatus(this.m_optMenu, isEnabled);
/*  627 */     setMenuItemStatus(this.m_buildMenu, isEnabled);
/*      */   }
/*      */ 
/*      */   protected void setMenuItemStatus(JMenu menu, boolean isEnabled)
/*      */   {
/*  632 */     int numItems = menu.getItemCount();
/*  633 */     for (int i = 0; i < numItems; ++i)
/*      */     {
/*  635 */       JMenuItem mi = menu.getItem(i);
/*  636 */       if (mi == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  641 */       String actCommand = mi.getActionCommand();
/*  642 */       if (actCommand.equals("enable"))
/*      */       {
/*  644 */         mi.setEnabled(isEnabled);
/*      */       }
/*  646 */       else if (actCommand.equals("disable"))
/*      */       {
/*  648 */         mi.setEnabled(isEnabled);
/*      */       } else {
/*  650 */         if ((!actCommand.equals("buildSettings")) && (!actCommand.equals("close")) && (!actCommand.equals("build")) && (!actCommand.equals("patchSettings")) && (!actCommand.equals("editReadme")) && (!actCommand.equals("config")))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  657 */         mi.setEnabled(false);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean neededReloadComponentDefFile()
/*      */   {
/*  665 */     if ((this.m_currentCompName == null) || (this.m_currentCompLocation == null))
/*      */     {
/*  667 */       return false;
/*      */     }
/*      */ 
/*  671 */     String absPath = this.m_manager.m_component.m_absLocation;
/*  672 */     if (FileUtils.checkFile(absPath, true, false) == -16)
/*      */     {
/*  674 */       return false;
/*      */     }
/*  676 */     if (this.m_manager.m_component != null)
/*      */     {
/*  678 */       return CWizardUtils.isTimeStampChanged(absPath, this.m_manager.m_component.m_lastModified);
/*      */     }
/*  680 */     return false;
/*      */   }
/*      */ 
/*      */   protected boolean isComponentInfoReloadNeeded(IntradocComponent info)
/*      */   {
/*  685 */     if (info.m_fileInfo != null)
/*      */     {
/*  687 */       for (int i = 0; i < info.m_fileInfo.size(); ++i)
/*      */       {
/*  689 */         ResourceFileInfo finfo = (ResourceFileInfo)info.m_fileInfo.elementAt(i);
/*  690 */         if (CWizardUtils.isTimeStampChanged(finfo.m_filename, finfo.m_lastModified))
/*      */         {
/*  692 */           return true;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  697 */     return false;
/*      */   }
/*      */ 
/*      */   protected ActionListener createMenuListener()
/*      */   {
/*  703 */     ActionListener listener = new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*  707 */         String cmdStr = e.getActionCommand();
/*  708 */         if (cmdStr.equals("add"))
/*      */         {
/*  710 */           CWizardFrame.this.openOrAddComponent(false, "!csCompWizLabelAddComp", null);
/*      */         }
/*  712 */         else if (cmdStr.equals("open"))
/*      */         {
/*  714 */           CWizardFrame.this.openOrAddComponent(true, "!csCompWizLabelCompList", null);
/*      */         }
/*  716 */         else if (cmdStr.equals("close"))
/*      */         {
/*  718 */           CWizardFrame.this.closeComponent();
/*      */         }
/*  720 */         else if (cmdStr.equals("seteditor"))
/*      */         {
/*  722 */           CWizardFrame.this.openConfig();
/*      */         }
/*  724 */         else if (cmdStr.equals("editReadme"))
/*      */         {
/*  726 */           CWizardFrame.this.editReadme();
/*      */         }
/*  728 */         else if (cmdStr.equals("enable"))
/*      */         {
/*  730 */           CWizardFrame.this.enableOrDisableComponent(true);
/*      */         }
/*  732 */         else if (cmdStr.equals("disable"))
/*      */         {
/*  734 */           CWizardFrame.this.enableOrDisableComponent(false);
/*      */         }
/*  736 */         else if (cmdStr.equals("config"))
/*      */         {
/*  738 */           CWizardFrame.this.editConfiguration();
/*      */         }
/*  740 */         else if ((cmdStr.equals("build")) || (cmdStr.equals("install")) || (cmdStr.equals("buildSettings")))
/*      */         {
/*  743 */           CWizardFrame.this.doInstall(cmdStr);
/*      */         }
/*  745 */         else if (cmdStr.equals("patchSettings"))
/*      */         {
/*  747 */           CWizardFrame.this.editPatchSettings();
/*      */         }
/*      */         else
/*      */         {
/*  751 */           CWizardFrame.this.dispose();
/*      */         }
/*      */       }
/*      */     };
/*  756 */     return listener;
/*      */   }
/*      */ 
/*      */   protected void editConfiguration()
/*      */   {
/*  762 */     ResourceContainer prefResouces = null;
/*  763 */     if (DataBinderUtils.getBoolean(this.m_manager.m_component.m_binder, "hasInstallStrings", false))
/*      */     {
/*  765 */       prefResouces = this.m_manager.m_component.m_prefData.getPreferenceResources();
/*  766 */       this.m_manager.m_component.m_prefData.loadPreferenceStrings();
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  773 */       this.m_manager.m_component.m_prefData.load();
/*      */ 
/*  776 */       CWizardComponentConfigDlg dlg = new CWizardComponentConfigDlg(this.m_appHelper, "MyTitle", null, this.m_manager.m_component.m_prefData, prefResouces, false);
/*      */ 
/*  778 */       dlg.init();
/*  779 */       boolean doSave = dlg.configureComponent();
/*      */ 
/*  782 */       if (doSave)
/*      */       {
/*  784 */         this.m_manager.m_component.m_prefData.save();
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  789 */       CWizardGuiUtils.reportError(this.m_appHelper, e, (IdcMessage)null);
/*  790 */       return;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void closeComponent()
/*      */   {
/*  796 */     this.m_previousCompName = this.m_currentCompName;
/*  797 */     this.m_currentCompName = null;
/*  798 */     this.m_currentCompLocation = null;
/*  799 */     this.m_appHelper.m_props = new Properties();
/*      */     try
/*      */     {
/*  802 */       refreshComponentInfoData(this.m_currentCompName, true, false);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  806 */       CWizardGuiUtils.reportError(this.m_appHelper, e, IdcMessageFactory.lc("csCompWizUnableClose", new Object[] { this.m_currentCompName }));
/*      */     }
/*  808 */     setMenuItems(false);
/*      */   }
/*      */ 
/*      */   protected void openOrAddComponent(boolean isOpen, String title, DataResultSet componentList)
/*      */   {
/*  813 */     IdcMessage errMsg = null;
/*  814 */     Properties props = null;
/*  815 */     int retVal = 1;
/*      */ 
/*  817 */     if (isOpen)
/*      */     {
/*  819 */       errMsg = IdcMessageFactory.lc("csCompWizCompOpenError", new Object[] { this.m_currentCompName });
/*  820 */       OpenComponentDlg compDlg = new OpenComponentDlg(this.m_appHelper, LocaleResources.localizeMessage(title, null), null, this.m_manager);
/*      */ 
/*  822 */       String currCompName = this.m_currentCompName;
/*      */ 
/*  824 */       if (componentList != null)
/*      */       {
/*  826 */         currCompName = null;
/*      */       }
/*  828 */       compDlg.init(currCompName, componentList);
/*  829 */       retVal = compDlg.prompt();
/*  830 */       props = compDlg.getProperties();
/*      */     }
/*      */     else
/*      */     {
/*  834 */       errMsg = IdcMessageFactory.lc("csCompWizCompAddError", new Object[] { this.m_currentCompName });
/*  835 */       NewComponentDlg newDlg = new NewComponentDlg(this.m_appHelper, LocaleResources.localizeMessage(title, null), null, this.m_manager);
/*      */ 
/*  837 */       newDlg.init();
/*  838 */       retVal = newDlg.prompt();
/*  839 */       props = newDlg.getProperties();
/*      */     }
/*      */ 
/*  842 */     if (retVal != 1)
/*      */       return;
/*  844 */     openComponent(props, errMsg);
/*      */   }
/*      */ 
/*      */   protected void openComponent(Map<String, String> map, IdcMessage errMsg)
/*      */   {
/*  850 */     if (map == null)
/*      */       return;
/*  852 */     this.m_previousCompName = this.m_currentCompName;
/*  853 */     this.m_currentCompName = ((String)map.get("name"));
/*  854 */     this.m_currentCompLocation = ((String)map.get("relativeLocation"));
/*      */ 
/*  856 */     Properties props = new Properties();
/*  857 */     props.putAll(map);
/*  858 */     if (this.m_manager.m_component != null)
/*      */     {
/*  860 */       props.put("absolutePath", this.m_manager.m_component.m_absLocation);
/*      */     }
/*      */     else
/*      */     {
/*  864 */       props.put("absolutePath", "");
/*      */     }
/*  866 */     this.m_appHelper.m_props = props;
/*      */     try
/*      */     {
/*  869 */       refreshComponentInfoData(this.m_currentCompName, true, true);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  873 */       CWizardGuiUtils.reportError(this.m_appHelper, e, errMsg);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void enableOrDisableComponent(boolean setEnabled)
/*      */   {
/*      */     try
/*      */     {
/*  882 */       this.m_manager.enableOrDisableComponent(this.m_appHelper.m_props, setEnabled);
/*  883 */       refreshComponentInfoData(this.m_currentCompName, false, false);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  887 */       CWizardGuiUtils.reportError(this.m_appHelper, e, (IdcMessage)null);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void openConfig()
/*      */   {
/*  893 */     ComponentConfigDlg dlg = new ComponentConfigDlg(this.m_appHelper, LocaleResources.getString("csCompWizLabelHTMLConfig", null), DialogHelpTable.getHelpPage("CW_ComponentConfiguration"));
/*      */ 
/*  896 */     dlg.init();
/*  897 */     dlg.prompt();
/*      */   }
/*      */ 
/*      */   protected void editReadme()
/*      */   {
/*      */     try
/*      */     {
/*  904 */       String absPath = this.m_manager.m_component.createReadmeFile();
/*  905 */       CWizardGuiUtils.launchEditor(this.m_appHelper, absPath);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  909 */       CWizardGuiUtils.reportError(this.m_appHelper, e, (IdcMessage)null);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void editHda()
/*      */   {
/*      */   }
/*      */ 
/*      */   protected void doInstall(String installType)
/*      */   {
/*  920 */     String title = "csCompWizLabelBuildSettings";
/*  921 */     String helpPage = "CW_BuildSettings";
/*      */ 
/*  923 */     IntradocComponent component = this.m_manager.m_component;
/*  924 */     if (installType.equals("build"))
/*      */     {
/*  926 */       title = "csCompWizLabelBuild";
/*  927 */       helpPage = "CW_Build";
/*      */     }
/*  929 */     else if (installType.equals("install"))
/*      */     {
/*  931 */       title = "csCompWizLabelUnpackage";
/*  932 */       helpPage = "CW_install";
/*  933 */       component = null;
/*      */     }
/*      */ 
/*  936 */     boolean hasPrefData = false;
/*  937 */     if (component != null)
/*      */     {
/*  939 */       hasPrefData = StringUtils.convertToBool(this.m_manager.m_component.m_binder.getLocal("hasPreferenceData"), false);
/*      */     }
/*      */ 
/*  943 */     ManifestEditorDlg installerDlg = new ManifestEditorDlg(this.m_appHelper, LocaleResources.getString(title, null), DialogHelpTable.getHelpPage(helpPage), component, this.m_manager);
/*      */ 
/*  946 */     installerDlg.init(installType);
/*      */ 
/*  949 */     if ((installerDlg.m_isError) || (installerDlg.prompt() != 1))
/*      */       return;
/*  951 */     if (installType.equals("install"))
/*      */     {
/*  953 */       processPostUnpackage(installerDlg);
/*      */     }
/*  955 */     if (!installType.equals("buildSettings")) {
/*      */       return;
/*      */     }
/*      */ 
/*  959 */     boolean nowHasPrefData = StringUtils.convertToBool(this.m_manager.m_component.m_binder.getLocal("hasPreferenceData"), false);
/*      */ 
/*  961 */     if ((!hasPrefData) && (nowHasPrefData))
/*      */     {
/*  963 */       this.m_manager.m_component.m_prefData.m_dataDir = CompInstallUtils.getInstallConfPath(this.m_manager.m_component.m_binder.getLocal("installID"), this.m_manager.m_component.m_binder.getLocal("ComponentName"));
/*      */       try
/*      */       {
/*  969 */         this.m_manager.m_component.m_prefData.save();
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  973 */         CWizardGuiUtils.reportError(this.m_appHelper, e, (IdcMessage)null);
/*      */       }
/*      */     } else {
/*  976 */       if ((!hasPrefData) || (nowHasPrefData))
/*      */         return;
/*  978 */       this.m_manager.m_component.m_prefData.m_dataDir = null;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void editPatchSettings()
/*      */   {
/*  986 */     PatchEditorDlg patchDlg = new PatchEditorDlg(this.m_appHelper, LocaleResources.getString("csCompWizLabelPatchSettings", null), null, this.m_manager.m_component);
/*      */ 
/*  990 */     patchDlg.init();
/*  991 */     patchDlg.prompt();
/*      */   }
/*      */ 
/*      */   protected void processPostUnpackage(ManifestEditorDlg installerDlg)
/*      */   {
/*      */     try
/*      */     {
/*  999 */       ComponentWizardManager tempManager = this.m_manager;
/*      */       try
/*      */       {
/* 1003 */         this.m_manager = new ComponentWizardManager();
/* 1004 */         this.m_manager.init();
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1008 */         this.m_manager = tempManager;
/*      */       }
/*      */ 
/* 1011 */       Vector v = installerDlg.getSucessfulComponents();
/* 1012 */       if (v == null)
/*      */       {
/* 1014 */         IdcMessage msg = IdcMessageFactory.lc("csCompWizUnpackageNoComponent", new Object[] { installerDlg.getFilePath() });
/* 1015 */         CWizardGuiUtils.doMessage(this.m_appHelper, null, msg, 1);
/*      */       }
/*      */       else
/*      */       {
/* 1019 */         String[] fields = { "name", "location", "status" };
/* 1020 */         DataResultSet editComps = this.m_manager.getEditComponents();
/* 1021 */         FieldInfo[] fi = ResultSetUtils.createInfoList(editComps, fields, true);
/*      */ 
/* 1023 */         if (v.size() == 1)
/*      */         {
/* 1025 */           String compName = (String)v.elementAt(0);
/* 1026 */           Vector tempRow = editComps.findRow(fi[0].m_index, compName);
/*      */ 
/* 1028 */           if (tempRow != null)
/*      */           {
/* 1030 */             Map map = editComps.getCurrentRowMap();
/* 1031 */             String location = (String)tempRow.elementAt(fi[1].m_index);
/*      */ 
/* 1033 */             this.m_previousCompName = this.m_currentCompName;
/* 1034 */             map.put(fields[0], compName);
/* 1035 */             map.put(fields[1], location);
/*      */ 
/* 1037 */             map.put(fields[2], "Disabled");
/* 1038 */             map.put("relativeLocation", location);
/*      */ 
/* 1040 */             Map args = new HashMap();
/* 1041 */             args.put("isNew", "0");
/* 1042 */             this.m_manager.initComponentInfo(compName, map, args);
/* 1043 */             openComponent(map, IdcMessageFactory.lc("csCompWizOpenComponentError", new Object[] { this.m_currentCompName }));
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1048 */           DataResultSet installComps = new DataResultSet();
/* 1049 */           installComps.copyFieldInfo(editComps);
/* 1050 */           for (int i = 0; i < v.size(); ++i)
/*      */           {
/* 1052 */             String compName = (String)v.elementAt(i);
/* 1053 */             Vector tempRow = editComps.findRow(fi[0].m_index, compName);
/* 1054 */             if (tempRow == null)
/*      */               continue;
/* 1056 */             String location = (String)tempRow.elementAt(fi[1].m_index);
/* 1057 */             tempRow.setElementAt(location, fi[1].m_index);
/* 1058 */             installComps.addRow(tempRow);
/*      */           }
/*      */ 
/* 1062 */           openOrAddComponent(true, "!csCompWizLabelUnpackagedList", installComps);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1068 */       CWizardGuiUtils.reportError(this.m_appHelper, e, (IdcMessage)null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void dispose()
/*      */   {
/* 1075 */     System.exit(0);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1080 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.CWizardFrame
 * JD-Core Version:    0.5.4
 */