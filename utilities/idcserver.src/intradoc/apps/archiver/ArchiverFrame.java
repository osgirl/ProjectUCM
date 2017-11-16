/*      */ package intradoc.apps.archiver;
/*      */ 
/*      */ import intradoc.apps.shared.AppLauncher;
/*      */ import intradoc.apps.shared.MainFrame;
/*      */ import intradoc.apps.shared.PromptDialog;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.gui.AppFrameHelper;
/*      */ import intradoc.gui.GridBagHelper;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.gui.TabPanel;
/*      */ import intradoc.gui.iwt.ColumnInfo;
/*      */ import intradoc.gui.iwt.UdlPanel;
/*      */ import intradoc.gui.iwt.UserDrawList;
/*      */ import intradoc.shared.AppContextUtils;
/*      */ import intradoc.shared.ArchiveCollections;
/*      */ import intradoc.shared.ArchiveData;
/*      */ import intradoc.shared.CollectionData;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.SharedContext;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.ViewFields;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.awt.event.ItemEvent;
/*      */ import java.awt.event.ItemListener;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Observable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.swing.JMenu;
/*      */ import javax.swing.JMenuBar;
/*      */ import javax.swing.JMenuItem;
/*      */ import javax.swing.JPanel;
/*      */ import javax.swing.JPopupMenu;
/*      */ import javax.swing.MenuElement;
/*      */ 
/*      */ public class ArchiverFrame extends MainFrame
/*      */   implements CollectionContext, ReportProgress, SharedContext
/*      */ {
/*      */   protected CollectionData m_localCollection;
/*      */   protected CollectionData m_currentCollection;
/*      */   protected String m_currentCollectionMoniker;
/*      */   protected DataBinder m_currentArchive;
/*      */   protected Vector m_watchedArchives;
/*      */   protected JMenu m_editMenu;
/*      */   protected JMenu m_actionsMenu;
/*      */   protected UdlPanel m_archiveList;
/*      */   protected ArchiverPanel[] m_tabPanels;
/*      */   protected TabPanel m_infoPanel;
/*      */   protected DataResultSet m_batchSet;
/*      */   protected Hashtable m_batchFields;
/*      */   protected Hashtable m_batchValues;
/*      */   protected Hashtable m_batchProps;
/*      */   protected final String[][] PANEL_INFOS;
/*      */   protected final String[][] EDIT_MENU;
/*      */ 
/*      */   public ArchiverFrame()
/*      */   {
/*   79 */     this.m_localCollection = null;
/*   80 */     this.m_currentCollection = null;
/*   81 */     this.m_currentCollectionMoniker = null;
/*   82 */     this.m_currentArchive = null;
/*   83 */     this.m_watchedArchives = null;
/*      */ 
/*   86 */     this.m_editMenu = null;
/*   87 */     this.m_actionsMenu = null;
/*   88 */     this.m_archiveList = null;
/*   89 */     this.m_tabPanels = null;
/*   90 */     this.m_infoPanel = null;
/*      */ 
/*   93 */     this.m_batchSet = null;
/*   94 */     this.m_batchFields = new Hashtable();
/*   95 */     this.m_batchValues = new Hashtable();
/*   96 */     this.m_batchProps = new Hashtable();
/*      */ 
/*   99 */     this.PANEL_INFOS = new String[][] { { "GeneralPanel", "intradoc.apps.archiver.GeneralPanel", "apTitleGeneral" }, { "ExportPanel", "intradoc.apps.archiver.ExportPanel", "apTitleExportData" }, { "ImportPanel", "intradoc.apps.archiver.ImportPanel", "apTitleImportMaps" }, { "ReplicationPanel", "intradoc.apps.archiver.ReplicationPanel", "apTitleReplication" }, { "TransferPanel", "intradoc.apps.archiver.TransferPanel", "apTitleTransferTo" } };
/*      */ 
/*  107 */     this.EDIT_MENU = new String[][] { { "apDlgButtonAdd", "add", "edit", "0", "0" }, { "apDlgButtonCopyTo", "copyTo", "edit", "1", "1" }, { "apLabelDelete", "delete", "edit", "0", "1" }, { "separator", "", "", "0", "1" }, { "apDlgButtonExport", "export", "actions", "0", "1" }, { "apDlgButtonImport", "import", "actions", "0", "1" }, { "apDlgButtonTransfer", "transfer", "actions", "0", "1" }, { "apLabelCancel", "cancel", "actions", "0", "0" } };
/*      */   }
/*      */ 
/*      */   public void init(IdcMessage title, boolean exitOnClose)
/*      */     throws ServiceException
/*      */   {
/*  126 */     super.init(title, exitOnClose);
/*  127 */     this.m_appHelper.attachToAppFrame(this, null, null, title);
/*      */ 
/*  130 */     initUI();
/*  131 */     buildMenu();
/*      */ 
/*  133 */     pack();
/*  134 */     setVisible(true);
/*      */ 
/*  137 */     if (connectToCollection(0))
/*      */       return;
/*  139 */     reportError(null, IdcMessageFactory.lc("apUnableToConnectToLocalCollection", new Object[0]));
/*      */   }
/*      */ 
/*      */   protected void buildMenu()
/*      */   {
/*  146 */     int len = this.EDIT_MENU.length;
/*  147 */     String[][] popupCommands = new String[len][2];
/*  148 */     String[][] commands = new String[len][3];
/*      */ 
/*  150 */     boolean isStandAlone = AppLauncher.getIsStandAlone();
/*      */ 
/*  152 */     int cCount = 0;
/*  153 */     int pCount = 0;
/*  154 */     for (int i = 0; i < len; ++i)
/*      */     {
/*  156 */       boolean isStandAloneOnly = StringUtils.convertToBool(this.EDIT_MENU[i][3], false);
/*  157 */       boolean isPopup = StringUtils.convertToBool(this.EDIT_MENU[i][4], true);
/*      */ 
/*  159 */       if ((isStandAloneOnly) && (!isStandAlone))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  164 */       if (isPopup)
/*      */       {
/*  166 */         popupCommands[pCount][0] = LocaleResources.getString(this.EDIT_MENU[i][0], this.m_cxt);
/*  167 */         popupCommands[pCount][1] = this.EDIT_MENU[i][1];
/*  168 */         ++pCount;
/*      */       }
/*      */ 
/*  171 */       commands[cCount][0] = LocaleResources.getString(this.EDIT_MENU[i][0], this.m_cxt);
/*  172 */       commands[cCount][1] = this.EDIT_MENU[i][1];
/*  173 */       commands[cCount][2] = this.EDIT_MENU[i][2];
/*  174 */       ++cCount;
/*      */     }
/*      */ 
/*  178 */     JMenuBar mb = new JMenuBar();
/*  179 */     setJMenuBar(mb);
/*      */ 
/*  181 */     ActionListener archListener = createArchiverMenuListener();
/*      */ 
/*  184 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  185 */     String[][] ARCHIVER_MENU = { { LocaleResources.getString("apDlgButtonOpenArchiveCollection", this.m_cxt), "open", "archiver" }, { LocaleResources.getString("apDlgButtonViewAutomation", this.m_cxt, idcName), "viewAutomation", "archiver" }, { "separator", "", "archiver" } };
/*      */ 
/*  193 */     JMenu archMenu = createMenu("archiver", LocaleResources.getString("apTitleOptions", this.m_cxt), ARCHIVER_MENU, archListener);
/*      */ 
/*  195 */     this.m_editMenu = createMenu("edit", LocaleResources.getString("apTitleEdit", this.m_cxt), commands, archListener);
/*      */ 
/*  197 */     this.m_actionsMenu = createMenu("actions", LocaleResources.getString("apTitleActions", this.m_cxt), commands, archListener);
/*      */ 
/*  200 */     mb.add(archMenu);
/*  201 */     mb.add(this.m_editMenu);
/*  202 */     mb.add(this.m_actionsMenu);
/*  203 */     addAppMenu(mb);
/*      */ 
/*  205 */     addStandardOptions(archMenu);
/*      */ 
/*  208 */     this.m_archiveList.addPopupEx(LocaleResources.getString("apLabelActions", this.m_cxt), popupCommands, archListener);
/*      */   }
/*      */ 
/*      */   protected JMenu createMenu(String type, String title, String[][] menuDef, ActionListener listener)
/*      */   {
/*  215 */     JMenu menu = new JMenu(title);
/*  216 */     for (int i = 0; i < menuDef.length; ++i)
/*      */     {
/*  218 */       String menuStr = menuDef[i][0];
/*  219 */       if (menuStr == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  224 */       String menuType = menuDef[i][2];
/*  225 */       if (!menuType.equals(type))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  230 */       if (menuStr.equalsIgnoreCase("separator"))
/*      */       {
/*  232 */         menu.addSeparator();
/*      */       }
/*      */       else
/*      */       {
/*  236 */         JMenuItem mi = new JMenuItem(menuStr);
/*  237 */         mi.setActionCommand(menuDef[i][1]);
/*  238 */         mi.addActionListener(listener);
/*  239 */         menu.add(mi);
/*      */       }
/*      */     }
/*      */ 
/*  243 */     return menu;
/*      */   }
/*      */ 
/*      */   protected void initUI()
/*      */   {
/*  248 */     JPanel mainPanel = this.m_appHelper.m_mainPanel;
/*  249 */     this.m_appHelper.makePanelGridBag(mainPanel, 1);
/*      */ 
/*  251 */     this.m_archiveList = new UdlPanel(LocaleResources.getString("apLabelCurrentArchivesIn", this.m_cxt), null, 610, 7, "ArchiveData", true);
/*      */ 
/*  254 */     this.m_archiveList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleName", this.m_cxt), "aArchiveName", 10.0D));
/*      */ 
/*  256 */     this.m_archiveList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleDescription", this.m_cxt), "aArchiveDescription", 20.0D));
/*      */ 
/*  258 */     this.m_archiveList.setVisibleColumns("aArchiveName,aArchiveDescription");
/*  259 */     this.m_archiveList.setIDColumn("aArchiveName");
/*  260 */     this.m_archiveList.init();
/*      */ 
/*  262 */     ItemListener iListener = new ItemListener()
/*      */     {
/*      */       public void itemStateChanged(ItemEvent e)
/*      */       {
/*  266 */         ArchiverFrame.this.checkSelection();
/*      */       }
/*      */     };
/*  269 */     ActionListener aListener = new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*  273 */         ArchiverFrame.this.addArchive(false);
/*      */       }
/*      */     };
/*  276 */     this.m_archiveList.addItemListener(iListener);
/*  277 */     this.m_archiveList.m_list.addActionListener(aListener);
/*      */ 
/*  279 */     this.m_infoPanel = null;
/*      */     try
/*      */     {
/*  282 */       this.m_infoPanel = initInfoPanel();
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  286 */       String error = LocaleUtils.encodeMessage("apErrorCreatingArchiveInfoTab", e.getMessage());
/*  287 */       Report.trace("archiver", LocaleResources.localizeMessage(error, this.m_cxt), e);
/*      */     }
/*      */ 
/*  290 */     this.m_appHelper.m_gridHelper.prepareAddLastRowElement();
/*  291 */     this.m_appHelper.m_gridHelper.m_gc.weighty = 1.0D;
/*  292 */     this.m_appHelper.addComponent(mainPanel, this.m_archiveList);
/*  293 */     this.m_appHelper.m_gridHelper.m_gc.weighty = 0.0D;
/*  294 */     this.m_appHelper.addComponent(mainPanel, this.m_infoPanel);
/*      */   }
/*      */ 
/*      */   protected TabPanel initInfoPanel() throws ServiceException
/*      */   {
/*  299 */     TabPanel tab = new TabPanel();
/*      */ 
/*  301 */     int numPanels = this.PANEL_INFOS.length;
/*  302 */     this.m_tabPanels = new ArchiverPanel[numPanels];
/*  303 */     for (int i = 0; i < numPanels; ++i)
/*      */     {
/*  305 */       this.m_tabPanels[i] = ((ArchiverPanel)ComponentClassFactory.createClassInstance(this.PANEL_INFOS[i][0], this.PANEL_INFOS[i][1], LocaleResources.getString("apUnableToLoadTabPanel", this.m_cxt, this.PANEL_INFOS[i][0])));
/*      */ 
/*  309 */       this.m_tabPanels[i].init(this.m_appHelper, this);
/*      */ 
/*  311 */       tab.addPane(LocaleResources.getString(this.PANEL_INFOS[i][2], this.m_cxt), this.m_tabPanels[i]);
/*      */     }
/*      */ 
/*  314 */     return tab;
/*      */   }
/*      */ 
/*      */   protected ActionListener createArchiverMenuListener()
/*      */   {
/*  322 */     ActionListener listener = new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*  326 */         String cmdStr = e.getActionCommand();
/*  327 */         if (cmdStr.equals("open"))
/*      */         {
/*  329 */           ArchiverFrame.this.openNewCollection();
/*      */         }
/*  331 */         else if (cmdStr.equals("viewAutomation"))
/*      */         {
/*  333 */           ArchiverFrame.this.viewAutomation();
/*      */         }
/*  335 */         else if (cmdStr.equals("add"))
/*      */         {
/*  337 */           ArchiverFrame.this.addArchive(true);
/*      */         }
/*  339 */         else if (cmdStr.equals("export"))
/*      */         {
/*  341 */           ArchiverFrame.this.exportArchive();
/*      */         }
/*  343 */         else if (cmdStr.equals("import"))
/*      */         {
/*  345 */           ArchiverFrame.this.importArchive();
/*      */         }
/*  347 */         else if (cmdStr.equals("copyTo"))
/*      */         {
/*  349 */           ArchiverFrame.this.copyArchive();
/*      */         }
/*  351 */         else if (cmdStr.equals("transfer"))
/*      */         {
/*  353 */           ArchiverFrame.this.transferArchive();
/*      */         }
/*      */         else
/*      */         {
/*  357 */           ArchiverFrame.this.doArchiveCommand(cmdStr);
/*      */         }
/*      */       }
/*      */     };
/*  361 */     return listener;
/*      */   }
/*      */ 
/*      */   protected void openNewCollection()
/*      */   {
/*  366 */     OpenCollectionDlg dlg = new OpenCollectionDlg(this.m_appHelper, LocaleResources.getString("apLabelOpenArchiveCollection", this.m_cxt));
/*      */ 
/*  368 */     dlg.init(this.m_currentCollection, this);
/*  369 */     if (dlg.prompt() != 1)
/*      */       return;
/*  371 */     Properties selCollection = dlg.m_selectedCollection;
/*  372 */     connectToCollection(selCollection);
/*      */   }
/*      */ 
/*      */   protected void viewAutomation()
/*      */   {
/*  378 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  379 */     ViewAutomationDlg dlg = new ViewAutomationDlg(this.m_appHelper, LocaleResources.getString("apLabelAutomationFor", this.m_cxt, idcName));
/*      */ 
/*  381 */     dlg.init(this, this.m_currentCollection);
/*      */   }
/*      */ 
/*      */   protected void addArchive(boolean isNew)
/*      */   {
/*  386 */     if (this.m_currentCollection.isProxied())
/*      */     {
/*  388 */       return;
/*      */     }
/*      */ 
/*  391 */     String title = null;
/*  392 */     if (isNew)
/*      */     {
/*  394 */       title = LocaleResources.getString("apTitleAddArchive", this.m_cxt);
/*      */     }
/*      */     else
/*      */     {
/*  398 */       title = LocaleResources.getString("apTitleEditArchive", this.m_cxt);
/*      */     }
/*      */ 
/*  401 */     NewArchiveDlg dlg = new NewArchiveDlg(this.m_appHelper, title, this);
/*  402 */     dlg.init(isNew);
/*  403 */     if (dlg.prompt() != 1)
/*      */       return;
/*  405 */     refreshArchiveList(dlg.getArchiveName());
/*      */   }
/*      */ 
/*      */   protected void copyArchive()
/*      */   {
/*  411 */     CopyArchiveDlg dlg = new CopyArchiveDlg(this.m_appHelper, LocaleResources.getString("apTitleCopyArchive", this.m_cxt), this);
/*      */ 
/*  413 */     dlg.init();
/*  414 */     dlg.prompt();
/*      */   }
/*      */ 
/*      */   protected void transferArchive()
/*      */   {
/*  419 */     Properties props = this.m_currentArchive.getLocalData();
/*      */     try
/*      */     {
/*  422 */       AppContextUtils.executeService(this, "TRANSFER_ARCHIVE", props, true);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  426 */       reportError(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void exportArchive()
/*      */   {
/*  433 */     Properties props = new Properties();
/*  434 */     if (!promptExport(props))
/*      */       return;
/*      */     try
/*      */     {
/*  438 */       props.put("dataSource", "RevisionIDs");
/*  439 */       AppContextUtils.executeService(this, "EXPORT_ARCHIVE", props, true);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  443 */       reportError(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean promptExport(Properties props)
/*      */   {
/*  450 */     Vector exportActionOptions = new IdcVector();
/*  451 */     String useTableOnly = this.m_currentArchive.getLocal("aExportTableOnly");
/*  452 */     boolean isTableOnly = StringUtils.convertToBool(useTableOnly, false);
/*  453 */     if (!isTableOnly)
/*      */     {
/*  455 */       exportActionOptions.addElement(new String[] { "aDoDelete", "apDeleteAfterArchive", "false" });
/*      */     }
/*  457 */     String archiveTableStr = this.m_currentArchive.getLocal("aExportTables");
/*      */ 
/*  459 */     if ((archiveTableStr != null) && (archiveTableStr.length() > 0))
/*      */     {
/*  461 */       exportActionOptions.addElement(new String[] { "aDoExportTable", "apDoTableExport", "true" });
/*      */     }
/*      */ 
/*  464 */     int size = exportActionOptions.size();
/*  465 */     String[] options = new String[size];
/*  466 */     IdcMessage[] captions = new IdcMessage[size];
/*  467 */     boolean[] states = new boolean[size];
/*      */ 
/*  469 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  471 */       String[] arr = (String[])(String[])exportActionOptions.elementAt(i);
/*  472 */       options[i] = arr[0];
/*  473 */       captions[i] = IdcMessageFactory.lc(arr[1], new Object[0]);
/*  474 */       states[i] = StringUtils.convertToBool(arr[2], false);
/*      */     }
/*  476 */     PromptDialog dlg = new PromptDialog(this.m_appHelper, IdcMessageFactory.lc("apTitleExportArchive", new Object[0]), "ExportArchive");
/*      */ 
/*  478 */     dlg.init(options, captions, states, IdcMessageFactory.lc("apVerifyArchiveExport", new Object[0]));
/*  479 */     return dlg.prompt(props);
/*      */   }
/*      */ 
/*      */   protected void importArchive()
/*      */   {
/*  485 */     if (this.m_currentArchive == null)
/*      */     {
/*  487 */       return;
/*      */     }
/*      */ 
/*  490 */     Properties props = this.m_currentArchive.getLocalData();
/*  491 */     if (!promptImport(props))
/*      */       return;
/*      */     try
/*      */     {
/*  495 */       AppContextUtils.executeService(this, "IMPORT_ARCHIVE", props, true);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  499 */       reportError(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean promptImport(Properties props)
/*      */   {
/*  506 */     String userExportDate = props.getProperty("aUsersExportDate");
/*  507 */     String docExportDate = props.getProperty("aDocConfigExportDate");
/*      */ 
/*  509 */     int hasUsers = ((userExportDate != null) && (userExportDate.length() > 0)) ? 1 : 0;
/*  510 */     int hasDocConfig = ((docExportDate != null) && (docExportDate.length() > 0)) ? 1 : 0;
/*      */ 
/*  513 */     int hasTables = 1;
/*      */ 
/*  515 */     int len = 1 + hasUsers + hasDocConfig + hasTables;
/*      */ 
/*  517 */     String[] options = new String[len];
/*  518 */     IdcMessage[] captions = new IdcMessage[len];
/*  519 */     boolean[] states = new boolean[len];
/*      */ 
/*  521 */     int count = 0;
/*  522 */     options[count] = "aImportDocuments";
/*  523 */     captions[count] = IdcMessageFactory.lc("apLabelImportBatchedRevisions", new Object[0]);
/*  524 */     states[count] = true;
/*      */ 
/*  526 */     if (hasUsers > 0)
/*      */     {
/*  528 */       ++count;
/*  529 */       options[count] = "aImportUsers";
/*      */       try
/*      */       {
/*  533 */         userExportDate = LocaleResources.localizeDate(userExportDate, this.m_cxt);
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/*  537 */         if (SystemUtils.m_verbose)
/*      */         {
/*  539 */           Report.debug("applet", null, ignore);
/*      */         }
/*      */       }
/*  542 */       captions[count] = IdcMessageFactory.lc("apLabelImportUserConfigFrom", new Object[] { userExportDate });
/*  543 */       states[count] = false;
/*      */     }
/*      */ 
/*  546 */     if (hasDocConfig > 0)
/*      */     {
/*  548 */       ++count;
/*  549 */       options[count] = "aImportDocConfig";
/*      */       try
/*      */       {
/*  553 */         docExportDate = LocaleResources.localizeDate(docExportDate, this.m_cxt);
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/*  557 */         if (SystemUtils.m_verbose)
/*      */         {
/*  559 */           Report.debug("applet", null, ignore);
/*      */         }
/*      */       }
/*  562 */       captions[count] = IdcMessageFactory.lc("apLabelImportContentConfigFrom", new Object[] { docExportDate });
/*  563 */       states[count] = false;
/*      */     }
/*      */ 
/*  566 */     if (hasTables > 0)
/*      */     {
/*  568 */       ++count;
/*  569 */       options[count] = "aImportTables";
/*  570 */       captions[count] = IdcMessageFactory.lc("apLabelImportTable", new Object[0]);
/*  571 */       states[count] = true;
/*      */     }
/*      */ 
/*  574 */     PromptDialog dlg = new PromptDialog(this.m_appHelper, IdcMessageFactory.lc("apLabelImportArchive", new Object[0]), "ImportArchive");
/*      */ 
/*  576 */     dlg.init(options, captions, states, IdcMessageFactory.lc("apVerifyArchiveImport", new Object[0]));
/*  577 */     return dlg.prompt(props);
/*      */   }
/*      */ 
/*      */   protected void doArchiveCommand(String type)
/*      */   {
/*  582 */     String name = this.m_archiveList.getSelectedObj();
/*      */ 
/*  585 */     String action = null;
/*      */ 
/*  587 */     if (type.equals("import"))
/*      */     {
/*  589 */       IdcMessage msg = IdcMessageFactory.lc("apVerifyArchiveImportFor", new Object[] { name });
/*  590 */       action = "IMPORT_ARCHIVE";
/*      */     }
/*  592 */     else if (type.equals("cancel"))
/*      */     {
/*  594 */       IdcMessage msg = IdcMessageFactory.lc("apVerifyArchiveCancel", new Object[0]);
/*  595 */       action = "CANCEL_ARCHIVE";
/*      */     }
/*  597 */     else if (type.equals("delete"))
/*      */     {
/*  599 */       IdcMessage msg = IdcMessageFactory.lc("apVerifyArchiveDelete", new Object[] { name });
/*  600 */       action = "DELETE_ARCHIVE";
/*      */     }
/*      */     else
/*      */     {
/*  604 */       return;
/*      */     }
/*      */     IdcMessage msg;
/*  607 */     int result = MessageBox.doMessage(this.m_appHelper, msg, 2);
/*  608 */     if (result != 1)
/*      */       return;
/*  610 */     Properties props = new Properties();
/*      */     try
/*      */     {
/*  613 */       AppContextUtils.executeService(this, action, props, true);
/*  614 */       if (type.equals("delete"))
/*      */       {
/*  616 */         removeWatchedArchive(props.getProperty("aArchiveName"));
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  621 */       reportError(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean refreshArchiveList(String selObj)
/*      */   {
/*  631 */     DataBinder binder = new DataBinder();
/*  632 */     IdcMessage errMsg = null;
/*  633 */     boolean result = false;
/*      */     try
/*      */     {
/*  637 */       executeService("GET_ARCHIVES", binder, false);
/*  638 */       DataResultSet rset = (DataResultSet)binder.getResultSet("ArchiveData");
/*      */ 
/*  640 */       if (rset != null)
/*      */       {
/*  642 */         ArchiveData archives = new ArchiveData();
/*  643 */         archives.load(rset);
/*  644 */         ResultSetUtils.sortResultSet(rset, new String[] { "aArchiveName" });
/*  645 */         this.m_archiveList.refreshList(rset, selObj);
/*  646 */         result = true;
/*      */       }
/*      */       else
/*      */       {
/*  651 */         errMsg = IdcMessageFactory.lc("apUnableToGetArchiveInfoForCollection", new Object[] { selObj });
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  657 */       reportError(e);
/*      */     }
/*      */     finally
/*      */     {
/*  661 */       if (errMsg != null)
/*      */       {
/*  663 */         reportError(null, errMsg);
/*      */       }
/*  665 */       checkSelection();
/*      */     }
/*      */ 
/*  668 */     return result;
/*      */   }
/*      */ 
/*      */   protected void watchArchives(DataBinder binder, boolean isUpdate)
/*      */   {
/*  673 */     if (isUpdate)
/*      */     {
/*  675 */       String watchedStr = createWatchedArchiveString();
/*  676 */       binder.putLocal("watchedSubMonikers", watchedStr);
/*      */     }
/*      */     else
/*      */     {
/*  681 */       String refreshStr = binder.getLocal("refreshSubMonikers");
/*      */ 
/*  684 */       binder.removeLocal("watchedSubMonikers");
/*  685 */       binder.removeLocal("refreshSubMonikers");
/*      */ 
/*  687 */       Vector refreshMonikers = StringUtils.parseArray(refreshStr, ',', ',');
/*  688 */       int num = refreshMonikers.size();
/*  689 */       for (int i = 0; i < num; ++i)
/*      */       {
/*  691 */         String name = (String)refreshMonikers.elementAt(i);
/*  692 */         long refreshedCounter = Long.parseLong((String)refreshMonikers.elementAt(++i));
/*  693 */         int index = findWatchedArchive(name);
/*  694 */         if (index < 0)
/*      */         {
/*  697 */           return;
/*      */         }
/*  699 */         ArchiveInfo info = (ArchiveInfo)this.m_watchedArchives.elementAt(index);
/*  700 */         info.markChanged(refreshedCounter);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addWatchedArchive(String name)
/*      */   {
/*  707 */     if (name == null)
/*      */     {
/*  709 */       return;
/*      */     }
/*      */ 
/*  712 */     String subMonikerStr = this.m_currentCollection.getSubMoniker(name);
/*  713 */     int index = findWatchedArchive(subMonikerStr);
/*  714 */     if (index >= 0)
/*      */     {
/*  716 */       return;
/*      */     }
/*      */ 
/*  719 */     ArchiveInfo info = new ArchiveInfo(name, subMonikerStr);
/*  720 */     this.m_watchedArchives.addElement(info);
/*      */   }
/*      */ 
/*      */   protected void removeWatchedArchive(String name)
/*      */   {
/*  725 */     if (name == null)
/*      */     {
/*  727 */       return;
/*      */     }
/*      */ 
/*  730 */     String subMonikerStr = this.m_currentCollection.getSubMoniker(name);
/*  731 */     int index = findWatchedArchive(subMonikerStr);
/*  732 */     if (index < 0)
/*      */     {
/*  734 */       return;
/*      */     }
/*      */ 
/*  737 */     this.m_watchedArchives.removeElementAt(index);
/*      */   }
/*      */ 
/*      */   protected int findWatchedArchive(String moniker)
/*      */   {
/*  742 */     int num = this.m_watchedArchives.size();
/*  743 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  745 */       ArchiveInfo info = (ArchiveInfo)this.m_watchedArchives.elementAt(i);
/*  746 */       if (moniker.equals(info.m_moniker))
/*      */       {
/*  748 */         return i;
/*      */       }
/*      */     }
/*      */ 
/*  752 */     return -1;
/*      */   }
/*      */ 
/*      */   protected int findArchiveInfo(String archiveName)
/*      */   {
/*  757 */     int num = this.m_watchedArchives.size();
/*  758 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  760 */       ArchiveInfo info = (ArchiveInfo)this.m_watchedArchives.elementAt(i);
/*  761 */       if (archiveName.equals(info.m_archiveName))
/*      */       {
/*  763 */         return i;
/*      */       }
/*      */     }
/*      */ 
/*  767 */     return -1;
/*      */   }
/*      */ 
/*      */   protected String createWatchedArchiveString()
/*      */   {
/*  772 */     StringBuffer buffer = new StringBuffer();
/*  773 */     int num = this.m_watchedArchives.size();
/*  774 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  776 */       ArchiveInfo info = (ArchiveInfo)this.m_watchedArchives.elementAt(i);
/*  777 */       if (buffer.length() > 0)
/*      */       {
/*  779 */         buffer.append(',');
/*      */       }
/*  781 */       buffer.append(info.m_moniker);
/*  782 */       buffer.append(',');
/*  783 */       buffer.append(String.valueOf(info.m_timeStamp));
/*      */     }
/*      */ 
/*  786 */     return buffer.toString();
/*      */   }
/*      */ 
/*      */   protected void checkSelection()
/*      */   {
/*  791 */     boolean isSelected = setSelectedArchive();
/*      */ 
/*  793 */     Properties props = null;
/*  794 */     if (isSelected)
/*      */     {
/*  797 */       props = this.m_currentArchive.getLocalData();
/*      */     }
/*      */ 
/*  800 */     setTabData(props, isSelected);
/*      */ 
/*  803 */     enableDisableMenu(this.m_actionsMenu, props, isSelected);
/*  804 */     enableDisableMenu(this.m_editMenu, props, isSelected);
/*      */ 
/*  806 */     JPopupMenu popup = this.m_archiveList.getPopupMenu();
/*  807 */     enableDisableMenu(popup, props, isSelected);
/*      */   }
/*      */ 
/*      */   protected void enableDisableMenu(MenuElement menu, Properties props, boolean isSelected)
/*      */   {
/*  812 */     JMenu theMenu = null;
/*  813 */     MenuElement[] elements = null;
/*  814 */     int itemCount = -1;
/*      */ 
/*  816 */     if (menu instanceof JMenu)
/*      */     {
/*  818 */       theMenu = (JMenu)menu;
/*  819 */       itemCount = theMenu.getItemCount();
/*      */     }
/*  821 */     else if (menu instanceof JPopupMenu)
/*      */     {
/*  823 */       JPopupMenu m = (JPopupMenu)menu;
/*  824 */       elements = m.getSubElements();
/*  825 */       itemCount = elements.length;
/*      */     }
/*      */ 
/*  828 */     for (int i = 0; i < itemCount; ++i)
/*      */     {
/*  830 */       JMenuItem dmi = null;
/*  831 */       if (theMenu != null)
/*      */       {
/*  833 */         dmi = theMenu.getItem(i);
/*      */       }
/*  835 */       else if (elements != null)
/*      */       {
/*  837 */         dmi = (JMenuItem)elements[i];
/*      */       }
/*      */ 
/*  840 */       String actCommand = dmi.getActionCommand();
/*      */ 
/*  842 */       if (actCommand.equals("cancel")) {
/*      */         continue;
/*      */       }
/*      */ 
/*  846 */       if ((this.m_currentCollection.isProxied()) && (!actCommand.equals("transfer")))
/*      */       {
/*  848 */         dmi.setEnabled(false);
/*      */       }
/*  851 */       else if (actCommand.equals("import"))
/*      */       {
/*  853 */         dmi.setEnabled(isSelected);
/*      */       }
/*  855 */       else if (actCommand.equals("export"))
/*      */       {
/*  858 */         boolean isAuto = false;
/*  859 */         if (props != null)
/*      */         {
/*  861 */           isAuto = StringUtils.convertToBool(props.getProperty("aIsAutomatedExport"), false);
/*      */         }
/*      */ 
/*  864 */         dmi.setEnabled((isSelected) && (!isAuto));
/*      */       }
/*  866 */       else if (actCommand.equals("transfer"))
/*      */       {
/*  870 */         boolean isEnabled = isSelected;
/*  871 */         if ((isSelected) && (props != null))
/*      */         {
/*  873 */           String targetArchive = props.getProperty("aTargetArchive");
/*  874 */           if ((targetArchive != null) && (targetArchive.length() > 0))
/*      */           {
/*  876 */             boolean isAutoTransfer = StringUtils.convertToBool(props.getProperty("aIsAutomatedTransfer"), false);
/*      */ 
/*  878 */             isEnabled = !isAutoTransfer;
/*      */           }
/*      */           else
/*      */           {
/*  882 */             isEnabled = false;
/*      */           }
/*      */         }
/*  885 */         dmi.setEnabled(isEnabled);
/*      */       }
/*  887 */       else if ((actCommand.equals("copyTo")) || (actCommand.equals("delete")))
/*      */       {
/*  889 */         dmi.setEnabled(isSelected);
/*      */       }
/*      */       else
/*      */       {
/*  893 */         dmi.setEnabled(true);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean setSelectedArchive()
/*      */   {
/*  900 */     this.m_currentArchive = null;
/*      */ 
/*  902 */     String archiveName = this.m_archiveList.getSelectedObj();
/*  903 */     if (archiveName == null)
/*      */     {
/*  905 */       return false;
/*      */     }
/*      */ 
/*  908 */     ArchiveData archives = (ArchiveData)SharedObjects.getTable("ArchiveData");
/*  909 */     if (archives == null)
/*      */     {
/*  912 */       return false;
/*      */     }
/*      */ 
/*  915 */     this.m_currentArchive = archives.getArchiveData(archiveName);
/*  916 */     if (this.m_currentArchive == null)
/*      */     {
/*  919 */       return false;
/*      */     }
/*      */ 
/*  922 */     this.m_currentArchive.putLocal("aArchiveName", archiveName);
/*  923 */     addWatchedArchive(archiveName);
/*      */ 
/*  925 */     return true;
/*      */   }
/*      */ 
/*      */   protected void setTabData(Properties props, boolean isEnabled)
/*      */   {
/*  930 */     if (props == null)
/*      */     {
/*  932 */       props = new Properties();
/*      */     }
/*      */ 
/*  935 */     int numPanels = this.m_tabPanels.length;
/*  936 */     for (int i = 0; i < numPanels; ++i)
/*      */     {
/*  938 */       this.m_tabPanels[i].setData(props);
/*  939 */       this.m_tabPanels[i].enableDisable(isEnabled);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void update(Observable obs, Object arg)
/*      */   {
/*  949 */     String selObj = this.m_archiveList.getSelectedObj();
/*  950 */     refreshArchiveList(selObj);
/*      */   }
/*      */ 
/*      */   public void removeNotify()
/*      */   {
/*  956 */     if (this.m_currentCollectionMoniker != null)
/*      */     {
/*  958 */       AppLauncher.removeMonikerObserver(this.m_currentCollectionMoniker, this);
/*      */     }
/*  960 */     super.removeNotify();
/*      */   }
/*      */ 
/*      */   public void executeService(String action, DataBinder binder, boolean isRefresh)
/*      */     throws ServiceException
/*      */   {
/*  969 */     watchArchives(binder, true);
/*      */ 
/*  971 */     String name = this.m_archiveList.getSelectedObj();
/*  972 */     loadContext(binder.getLocalData());
/*  973 */     AppLauncher.executeService(action, binder);
/*      */ 
/*  975 */     watchArchives(binder, false);
/*  976 */     if (!isRefresh)
/*      */       return;
/*  978 */     refreshArchiveList(name);
/*      */   }
/*      */ 
/*      */   public UserData getUserData()
/*      */   {
/*  984 */     return AppLauncher.getUserData();
/*      */   }
/*      */ 
/*      */   public SharedContext getSharedContext()
/*      */   {
/*  992 */     return this;
/*      */   }
/*      */ 
/*      */   public DataResultSet getBatchFiles()
/*      */   {
/*  998 */     String archiveName = this.m_currentArchive.getLocal("aArchiveName");
/*  999 */     int index = findArchiveInfo(archiveName);
/* 1000 */     if (index < 0)
/*      */     {
/* 1003 */       return null;
/*      */     }
/* 1005 */     ArchiveInfo info = (ArchiveInfo)this.m_watchedArchives.elementAt(index);
/*      */ 
/* 1007 */     if (info.m_isChanged)
/*      */     {
/* 1009 */       DataBinder binder = new DataBinder();
/*      */       try
/*      */       {
/* 1013 */         executeService("GET_BATCHFILES", binder, false);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1017 */         reportError(e, IdcMessageFactory.lc("apUnableToGetBatchFileInfo", new Object[0]));
/* 1018 */         return null;
/*      */       }
/*      */ 
/* 1021 */       info.m_batchFiles = ((DataResultSet)binder.getResultSet("BatchFiles"));
/* 1022 */       info.m_isChanged = false;
/*      */     }
/*      */ 
/* 1025 */     return info.m_batchFiles;
/*      */   }
/*      */ 
/*      */   public DataResultSet getBatchMetaSet(boolean isLocal, String fileName)
/*      */   {
/* 1030 */     return getBatchMetaSet(isLocal, fileName, false);
/*      */   }
/*      */ 
/*      */   public DataResultSet getBatchMetaSet(boolean isLocal, String fileName, boolean isTableArchive)
/*      */   {
/* 1035 */     DataResultSet rset = null;
/*      */     try
/*      */     {
/* 1038 */       if (isLocal)
/*      */       {
/* 1040 */         if (!isTableArchive)
/*      */         {
/* 1042 */           rset = SharedObjects.getTable("DocMetaDefinition");
/*      */         }
/*      */         else
/*      */         {
/* 1046 */           DataBinder binder = new DataBinder();
/* 1047 */           binder.putLocal("tableNames", fileName);
/* 1048 */           executeService("GET_TABLECOLUMNLIST", binder, false);
/* 1049 */           rset = (DataResultSet)binder.getResultSet("TableColumnList");
/* 1050 */           FieldInfo fi = new FieldInfo();
/* 1051 */           rset.getFieldInfo("columnName", fi);
/*      */ 
/* 1053 */           DataResultSet tmpRset = rset;
/* 1054 */           String[] fields = { "dName", "dCaption" };
/* 1055 */           rset = new DataResultSet(fields);
/* 1056 */           for (tmpRset.first(); tmpRset.isRowPresent(); tmpRset.next())
/*      */           {
/* 1058 */             String name = tmpRset.getStringValue(fi.m_index);
/* 1059 */             Vector row = new IdcVector();
/* 1060 */             row.addElement(name);
/* 1061 */             row.addElement(name);
/* 1062 */             rset.addRow(row);
/*      */           }
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1068 */         rset = (DataResultSet)this.m_batchFields.get(fileName);
/* 1069 */         if (rset != null);
/* 1071 */         DataBinder binder = new DataBinder();
/* 1072 */         binder.putLocal("aBatchFile", fileName);
/* 1073 */         if (isTableArchive)
/*      */         {
/* 1075 */           binder.putLocal("isTableArchive", "1");
/*      */         }
/* 1077 */         executeService("GET_BATCH_SCHEMA", binder, false);
/*      */ 
/* 1079 */         rset = (DataResultSet)binder.getResultSet("BatchFields");
/* 1080 */         this.m_batchFields.put(fileName, rset);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1086 */       reportError(e);
/*      */     }
/* 1088 */     return rset;
/*      */   }
/*      */ 
/*      */   public String[][] getBatchFields(boolean isLocal, String fileName)
/*      */   {
/* 1093 */     return getBatchFields(isLocal, fileName, false);
/*      */   }
/*      */ 
/*      */   public String[][] getBatchFields(boolean isLocal, String fileName, boolean isTableArchive)
/*      */   {
/* 1098 */     DataResultSet rset = getBatchMetaSet(isLocal, fileName, isTableArchive);
/* 1099 */     String[][] metaFields = (String[][])null;
/* 1100 */     if (rset != null)
/*      */     {
/*      */       try
/*      */       {
/* 1104 */         String[] keys = { "dName", "dCaption" };
/* 1105 */         metaFields = ResultSetUtils.createStringTable(rset, keys);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1109 */         reportError(e);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1114 */     ViewFields docFields = new ViewFields(this.m_cxt);
/* 1115 */     String[][] commonFields = docFields.createArchiverCommonFieldsList();
/* 1116 */     int numCommonFields = commonFields.length;
/* 1117 */     if (isTableArchive)
/*      */     {
/* 1119 */       numCommonFields = 0;
/*      */     }
/* 1121 */     int numMetaFields = 0;
/* 1122 */     if (metaFields != null)
/*      */     {
/* 1124 */       numMetaFields = metaFields.length;
/*      */     }
/* 1126 */     int numFields = numCommonFields + numMetaFields;
/*      */ 
/* 1128 */     String[][] fields = new String[numFields][2];
/* 1129 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/* 1131 */       if (i < numCommonFields)
/*      */       {
/* 1133 */         fields[i][0] = commonFields[i][0];
/* 1134 */         fields[i][1] = this.m_appHelper.getString(commonFields[i][1]);
/*      */       }
/*      */       else
/*      */       {
/* 1138 */         fields[i][0] = metaFields[(i - numCommonFields)][0];
/* 1139 */         fields[i][1] = this.m_appHelper.getString(metaFields[(i - numCommonFields)][1]);
/*      */       }
/*      */     }
/* 1142 */     return fields;
/*      */   }
/*      */ 
/*      */   public Vector getBatchValues(String fileName, String fieldName)
/*      */   {
/* 1147 */     String lookupStr = fileName + "+" + fieldName;
/* 1148 */     Vector values = (Vector)this.m_batchValues.get(lookupStr);
/* 1149 */     if (values == null)
/*      */     {
/* 1151 */       DataBinder binder = new DataBinder();
/* 1152 */       binder.putLocal("batchFieldName", fieldName);
/* 1153 */       binder.putLocal("aBatchFile", fileName);
/*      */       try
/*      */       {
/* 1156 */         executeService("GET_BATCH_VALUES", binder, false);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1160 */         reportError(e, IdcMessageFactory.lc("apUnableToGetBatchValues", new Object[0]));
/*      */       }
/*      */ 
/* 1163 */       values = binder.getOptionList("BatchValues");
/* 1164 */       if (values != null)
/*      */       {
/* 1166 */         this.m_batchValues.put(lookupStr, values);
/*      */       }
/*      */     }
/* 1169 */     return values;
/*      */   }
/*      */ 
/*      */   public Properties getBatchProperties(String fileName)
/*      */   {
/* 1174 */     Properties props = (Properties)this.m_batchProps.get(fileName);
/* 1175 */     if (props == null)
/*      */     {
/* 1177 */       DataBinder binder = new DataBinder();
/* 1178 */       binder.putLocal("aBatchFile", fileName);
/*      */       try
/*      */       {
/* 1181 */         executeService("GET_BATCH_PROPERTIES", binder, false);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1185 */         reportError(e, IdcMessageFactory.lc("apUnableToGetBatchProperties", new Object[0]));
/*      */       }
/* 1187 */       Properties localData = binder.getLocalData();
/* 1188 */       props = new Properties();
/*      */ 
/* 1190 */       int offset = fileName.length() + 1;
/* 1191 */       for (Enumeration en = localData.propertyNames(); en.hasMoreElements(); )
/*      */       {
/* 1193 */         String key = (String)en.nextElement();
/* 1194 */         if (key.startsWith(fileName))
/*      */         {
/* 1196 */           String propName = key.substring(offset);
/* 1197 */           props.put(propName, localData.getProperty(key));
/*      */         }
/*      */       }
/* 1200 */       this.m_batchProps.put(fileName, props);
/*      */     }
/*      */ 
/* 1203 */     return props;
/*      */   }
/*      */ 
/*      */   public void loadContext(Properties props)
/*      */   {
/* 1208 */     if (this.m_currentCollection == null)
/*      */     {
/* 1210 */       reportError(null, IdcMessageFactory.lc("apInvalidCollectionHandle", new Object[0]));
/* 1211 */       return;
/*      */     }
/*      */ 
/* 1214 */     if (this.m_currentArchive != null)
/*      */     {
/* 1216 */       String archiveName = this.m_currentArchive.getLocal("aArchiveName");
/* 1217 */       if (archiveName != null)
/*      */       {
/* 1219 */         props.put("aArchiveName", archiveName);
/*      */       }
/*      */     }
/* 1222 */     props.put("IDC_ID", String.valueOf(this.m_currentCollection.m_id));
/* 1223 */     props.put("IDC_Name", this.m_currentCollection.m_name);
/*      */   }
/*      */ 
/*      */   public void loadArchiveData(Properties props)
/*      */   {
/* 1228 */     int index = this.m_archiveList.getSelectedIndex();
/* 1229 */     if (index < 0)
/*      */       return;
/* 1231 */     Properties data = this.m_archiveList.getDataAt(index);
/* 1232 */     for (Enumeration en = data.keys(); en.hasMoreElements(); )
/*      */     {
/* 1234 */       String key = (String)en.nextElement();
/* 1235 */       String value = data.getProperty(key);
/* 1236 */       props.put(key, value);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean connectToCollection(int id)
/*      */   {
/* 1243 */     ArchiveCollections collections = (ArchiveCollections)SharedObjects.getTable(ArchiveCollections.m_tableName);
/*      */ 
/* 1245 */     if (collections == null)
/*      */     {
/*      */       try
/*      */       {
/* 1249 */         DataBinder binder = new DataBinder();
/* 1250 */         AppLauncher.executeService("GET_ARCHIVECOLLECTIONS", binder);
/* 1251 */         DataResultSet rset = (DataResultSet)binder.getResultSet(ArchiveCollections.m_tableName);
/*      */ 
/* 1253 */         collections = new ArchiveCollections();
/* 1254 */         collections.load(rset, false);
/* 1255 */         SharedObjects.putTable(ArchiveCollections.m_tableName, collections);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1259 */         reportError(e);
/* 1260 */         return false;
/*      */       }
/*      */     }
/*      */ 
/* 1264 */     CollectionData data = collections.getCollectionData(id);
/* 1265 */     if (data == null)
/*      */     {
/* 1268 */       return false;
/*      */     }
/*      */ 
/* 1271 */     if (id == 0)
/*      */     {
/* 1274 */       this.m_localCollection = data;
/*      */     }
/*      */ 
/* 1277 */     CollectionData oldCollection = this.m_currentCollection;
/* 1278 */     Vector oldWatchedArchives = this.m_watchedArchives;
/*      */ 
/* 1280 */     this.m_currentCollection = data;
/* 1281 */     this.m_watchedArchives = new IdcVector();
/* 1282 */     String newConStr = data.getMoniker();
/*      */ 
/* 1285 */     String oldConStr = this.m_currentCollectionMoniker;
/* 1286 */     if (oldConStr != null)
/*      */     {
/* 1288 */       AppLauncher.removeMonikerObserver(oldConStr, this);
/*      */     }
/* 1290 */     AppLauncher.addMonikerObserver(newConStr, this);
/* 1291 */     this.m_currentCollectionMoniker = newConStr;
/*      */ 
/* 1294 */     if (refreshArchiveList(null))
/*      */     {
/* 1296 */       this.m_archiveList.setTitle(LocaleResources.getString("apTitleCurrentArchivesIn", this.m_cxt, this.m_currentCollection.m_name));
/*      */     }
/*      */     else
/*      */     {
/* 1301 */       this.m_currentCollection = oldCollection;
/* 1302 */       this.m_watchedArchives = oldWatchedArchives;
/* 1303 */       AppLauncher.removeMonikerObserver(newConStr, this);
/* 1304 */       if (oldConStr != null)
/*      */       {
/* 1306 */         AppLauncher.addMonikerObserver(oldConStr, this);
/* 1307 */         this.m_currentCollectionMoniker = oldConStr;
/*      */       }
/*      */     }
/*      */ 
/* 1311 */     return true;
/*      */   }
/*      */ 
/*      */   protected boolean connectToCollection(Properties collProps)
/*      */   {
/* 1316 */     String idStr = collProps.getProperty("IDC_ID");
/* 1317 */     int id = Integer.parseInt(idStr);
/*      */ 
/* 1319 */     if (!connectToCollection(id))
/*      */     {
/* 1321 */       String name = collProps.getProperty("IDC_Name");
/* 1322 */       reportError(null, IdcMessageFactory.lc("apUnableToConnectToCollection", new Object[] { name }));
/* 1323 */       return false;
/*      */     }
/* 1325 */     return true;
/*      */   }
/*      */ 
/*      */   public String getLocalCollection()
/*      */   {
/* 1330 */     return this.m_localCollection.m_name;
/*      */   }
/*      */ 
/*      */   public CollectionData getCurrentCollection()
/*      */   {
/* 1335 */     return this.m_currentCollection;
/*      */   }
/*      */ 
/*      */   public void reportProgress(int type, String msg, float amtDone, float max)
/*      */   {
/* 1343 */     String pMsg = msg;
/* 1344 */     if ((pMsg != null) && (pMsg.startsWith("!")))
/*      */     {
/* 1346 */       pMsg = LocaleResources.localizeMessage(pMsg, this.m_cxt);
/*      */     }
/*      */ 
/* 1349 */     if (type == 0)
/*      */     {
/* 1351 */       int m = (int)(max + 0.01D);
/* 1352 */       int a = (int)(amtDone + 0.01D);
/* 1353 */       pMsg = pMsg + LocaleResources.getString("apProgressNumberOfNumber", this.m_cxt, new StringBuilder().append("").append(a).toString(), new StringBuilder().append("").append(m).toString());
/*      */     }
/* 1357 */     else if (max >= 0.0F)
/*      */     {
/* 1359 */       float perc = 100.0F * amtDone / max;
/* 1360 */       pMsg = pMsg + LocaleResources.getString("apProgressPercent", this.m_cxt, new StringBuilder().append("").append(perc).toString());
/*      */     }
/* 1362 */     else if (amtDone >= 0.0F)
/*      */     {
/* 1364 */       pMsg = pMsg + LocaleResources.getString("apProgressNumber", this.m_cxt, new StringBuilder().append("").append(amtDone).toString());
/*      */     }
/*      */ 
/* 1368 */     this.m_appHelper.displayStatus(pMsg);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1373 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ArchiverFrame
 * JD-Core Version:    0.5.4
 */