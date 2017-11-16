/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DisplayLabel;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.gui.iwt.IdcList;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.ExportQueryData;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import intradoc.shared.gui.ViewDlg;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Date;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ import javax.swing.event.ListSelectionListener;
/*     */ 
/*     */ public class ExportPanel extends ArchiverPanel
/*     */   implements ActionListener
/*     */ {
/*     */   protected JButton m_queryEditBtn;
/*     */   protected JButton m_queryViewBtn;
/*     */   protected JButton m_addEditBtn;
/*     */   protected JButton m_tableAddBtn;
/*     */   protected JButton m_tableEditBtn;
/*     */   protected JButton m_tableDeleteBtn;
/*     */   protected JButton m_tableQueryEditBtn;
/*     */   protected JButton m_tableQueryViewBtn;
/*     */   protected static final int CONTENT = 0;
/*     */   protected static final int TABLE = 1;
/*     */   protected IdcList m_tableList;
/*     */   protected boolean m_disableSelectionEvent;
/*     */   protected boolean m_isBeta;
/*     */ 
/*     */   public ExportPanel()
/*     */   {
/*  71 */     this.m_queryEditBtn = null;
/*  72 */     this.m_queryViewBtn = null;
/*  73 */     this.m_addEditBtn = null;
/*     */ 
/*  76 */     this.m_tableAddBtn = null;
/*  77 */     this.m_tableEditBtn = null;
/*  78 */     this.m_tableDeleteBtn = null;
/*  79 */     this.m_tableQueryEditBtn = null;
/*  80 */     this.m_tableQueryViewBtn = null;
/*     */ 
/*  85 */     this.m_tableList = null;
/*  86 */     this.m_disableSelectionEvent = false;
/*     */ 
/*  88 */     this.m_isBeta = false;
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys, CollectionContext context)
/*     */   {
/*  96 */     super.init(sys, context);
/*     */   }
/*     */ 
/*     */   public JPanel initUI()
/*     */   {
/* 102 */     TabPanel tab = new TabPanel();
/* 103 */     tab.setFullWidthTab(true);
/* 104 */     tab.addPane(LocaleResources.getString("apContentTabLabel", this.m_cxt), initUI(0));
/* 105 */     tab.addPane(LocaleResources.getString("apTableTabLabel", this.m_cxt), initUI(1));
/*     */ 
/* 107 */     JPanel panel = new PanePanel();
/* 108 */     this.m_helper.addComponent(panel, tab);
/* 109 */     return tab;
/*     */   }
/*     */ 
/*     */   public JPanel initUI(int type)
/*     */   {
/* 119 */     JPanel infoPanel = initQueryInfoUI(type);
/*     */ 
/* 121 */     JPanel wrapper = new PanePanel();
/* 122 */     wrapper.setLayout(new BorderLayout());
/* 123 */     wrapper.add("Center", infoPanel);
/* 124 */     if (type == 0)
/*     */     {
/* 126 */       JPanel addtnlPanel = initAdditionalUI();
/* 127 */       wrapper.add("South", addtnlPanel);
/*     */     }
/*     */     else
/*     */     {
/* 131 */       JPanel tablePanel = initTableUI();
/* 132 */       wrapper.add("West", tablePanel);
/*     */     }
/*     */ 
/* 135 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected JPanel initQueryInfoUI(int type)
/*     */   {
/* 140 */     JPanel pnl = new PanePanel();
/* 141 */     this.m_helper.makePanelGridBag(pnl, 1);
/* 142 */     this.m_helper.addPanelTitle(pnl, LocaleResources.getString("apTitleExportQuery", this.m_cxt));
/*     */ 
/* 144 */     boolean isTable = type == 1;
/*     */ 
/* 146 */     int height = 5;
/* 147 */     String displayName = "aExportQuery";
/* 148 */     if (isTable)
/*     */     {
/* 150 */       displayName = displayName + "Table";
/* 151 */       height = 10;
/*     */     }
/* 153 */     Component comp = new CustomTextArea(height, 20);
/* 154 */     this.m_helper.addExchangeComponent(pnl, comp, displayName);
/* 155 */     comp.setEnabled(false);
/*     */ 
/* 158 */     ActionListener edListener = new ActionListener(isTable)
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 163 */         EditQueryDlg dlg = new EditQueryDlg(ExportPanel.this.m_systemInterface, LocaleResources.getString("apEditExportQuery", ExportPanel.this.m_cxt), ExportPanel.this.m_collectionContext);
/*     */ 
/* 166 */         Properties props = new Properties(ExportPanel.this.m_helper.m_props);
/*     */ 
/* 168 */         if (this.val$isTable)
/*     */         {
/* 170 */           String table = (String)ExportPanel.this.m_tableList.getSelectedValue();
/* 171 */           if ((table != null) && (table.length() != 0))
/*     */           {
/* 173 */             props.put("currentTable", table);
/*     */           }
/*     */           else
/*     */           {
/* 178 */             return;
/*     */           }
/*     */         }
/* 181 */         dlg.init(props);
/*     */       }
/*     */     };
/* 186 */     ActionListener viewListener = new ActionListener(isTable)
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 191 */         ExportPanel.this.viewQueryResults(this.val$isTable);
/*     */       }
/*     */     };
/* 196 */     JPanel btnPanel = new PanePanel();
/* 197 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 199 */     if (isTable)
/*     */     {
/* 201 */       this.m_tableQueryEditBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 202 */       this.m_tableQueryEditBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apEditExportQuery", this.m_cxt));
/* 203 */       this.m_tableQueryEditBtn.addActionListener(edListener);
/* 204 */       this.m_tableQueryViewBtn = new JButton(LocaleResources.getString("apDlgButtonPreview", this.m_cxt));
/* 205 */       this.m_tableQueryViewBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadablePreviewExportQuery", this.m_cxt));
/* 206 */       this.m_tableQueryViewBtn.addActionListener(viewListener);
/* 207 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 208 */       this.m_helper.addLastComponentInRow(btnPanel, this.m_tableQueryEditBtn);
/* 209 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 5, 10, 5);
/* 210 */       this.m_helper.addLastComponentInRow(btnPanel, this.m_tableQueryViewBtn);
/*     */     }
/*     */     else
/*     */     {
/* 214 */       this.m_queryEditBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 215 */       this.m_queryEditBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apEditExportQuery", this.m_cxt));
/* 216 */       this.m_queryEditBtn.addActionListener(edListener);
/* 217 */       this.m_queryViewBtn = new JButton(LocaleResources.getString("apDlgButtonPreview", this.m_cxt));
/* 218 */       this.m_queryViewBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadablePreviewExportQuery", this.m_cxt));
/* 219 */       this.m_queryViewBtn.addActionListener(viewListener);
/* 220 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 221 */       this.m_helper.addLastComponentInRow(btnPanel, this.m_queryEditBtn);
/* 222 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 5, 10, 5);
/* 223 */       this.m_helper.addLastComponentInRow(btnPanel, this.m_queryViewBtn);
/*     */     }
/*     */ 
/* 227 */     JPanel ctlPanel = new PanePanel();
/* 228 */     this.m_helper.makePanelGridBag(ctlPanel, 2);
/*     */ 
/* 231 */     JPanel wrapper = new CustomPanel();
/* 232 */     wrapper.setLayout(new BorderLayout());
/* 233 */     wrapper.add("Center", pnl);
/* 234 */     wrapper.add("East", btnPanel);
/*     */ 
/* 236 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected JPanel initAdditionalUI()
/*     */   {
/* 242 */     JPanel adtnlPanel = new PanePanel();
/* 243 */     this.m_helper.makePanelGridBag(adtnlPanel, 0);
/* 244 */     this.m_helper.addPanelTitle(adtnlPanel, LocaleResources.getString("apLabelAdditionalData", this.m_cxt));
/*     */ 
/* 247 */     DisplayLabel docComp = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/* 248 */     this.m_helper.addLabelFieldPairEx(adtnlPanel, LocaleResources.getString("apLabelContentConfig", this.m_cxt), docComp, "aExportDocConfig", false);
/*     */ 
/* 251 */     DisplayLabel secComp = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/* 252 */     this.m_helper.addLabelFieldPairEx(adtnlPanel, LocaleResources.getString("apLabelUserConfig", this.m_cxt), secComp, "aExportUserConfig", true);
/*     */ 
/* 256 */     ActionListener edListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 260 */         EditAdditionalDlg dlg = new EditAdditionalDlg(ExportPanel.this.m_systemInterface, LocaleResources.getString("apLabelEditAdditionalData", ExportPanel.this.m_cxt), ExportPanel.this.m_collectionContext);
/*     */ 
/* 263 */         dlg.init(ExportPanel.this.m_helper.m_props);
/*     */       }
/*     */     };
/* 268 */     JPanel btnPanel = new PanePanel();
/* 269 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 271 */     this.m_addEditBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 272 */     this.m_addEditBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apLabelEditAdditionalData", this.m_cxt));
/* 273 */     this.m_addEditBtn.addActionListener(edListener);
/* 274 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 275 */     this.m_helper.addComponent(btnPanel, this.m_addEditBtn);
/*     */ 
/* 278 */     JPanel wrapper = new CustomPanel();
/* 279 */     this.m_helper.makePanelGridBag(wrapper, 1);
/* 280 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 281 */     this.m_helper.addComponent(wrapper, adtnlPanel);
/* 282 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 283 */     this.m_helper.addLastComponentInRow(wrapper, btnPanel);
/* 284 */     this.m_helper.m_gridHelper.m_gc.weighty = 10.0D;
/* 285 */     this.m_helper.addLastComponentInRow(wrapper, new PanePanel());
/* 286 */     return wrapper;
/*     */   }
/*     */ 
/*     */   public JPanel initTableUI()
/*     */   {
/* 291 */     JPanel tablePanel = new PanePanel();
/* 292 */     this.m_tableList = new IdcList();
/*     */ 
/* 294 */     JScrollPane tableScrollPane = new JScrollPane(this.m_tableList);
/*     */ 
/* 296 */     ListSelectionListener listener = new ListSelectionListener()
/*     */     {
/*     */       public void valueChanged(ListSelectionEvent event)
/*     */       {
/* 300 */         if (ExportPanel.this.m_disableSelectionEvent)
/*     */         {
/* 302 */           return;
/*     */         }
/*     */ 
/* 305 */         int selectedIndex = ExportPanel.this.m_tableList.getSelectedIndex();
/* 306 */         boolean isSelected = selectedIndex >= 0;
/* 307 */         ExportPanel.this.m_tableEditBtn.setEnabled(isSelected);
/* 308 */         ExportPanel.this.m_tableDeleteBtn.setEnabled(isSelected);
/* 309 */         ExportPanel.this.m_tableQueryEditBtn.setEnabled(isSelected);
/* 310 */         ExportPanel.this.m_tableQueryViewBtn.setEnabled(isSelected);
/*     */ 
/* 313 */         ExportPanel.this.m_helper.loadComponentValues();
/*     */       }
/*     */     };
/* 317 */     this.m_tableList.addListSelectionListener(listener);
/*     */ 
/* 319 */     PanePanel buttonPanel = new PanePanel();
/* 320 */     buttonPanel.setLayout(new BorderLayout());
/*     */ 
/* 322 */     this.m_tableAddBtn = new JButton(LocaleResources.getString("apDlgButtonAdd", this.m_cxt));
/* 323 */     this.m_tableAddBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apArchiverAddTable", this.m_cxt));
/* 324 */     this.m_tableAddBtn.setActionCommand("addTable");
/* 325 */     this.m_tableEditBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 326 */     String readable = LocaleUtils.encodeMessage("apArchiverEditTable", null, "");
/* 327 */     readable = LocaleResources.localizeMessage(readable, this.m_cxt);
/* 328 */     this.m_tableEditBtn.getAccessibleContext().setAccessibleName(readable);
/* 329 */     this.m_tableEditBtn.setActionCommand("editTable");
/* 330 */     this.m_tableDeleteBtn = new JButton(LocaleResources.getString("apDlgButtonDelete", this.m_cxt));
/* 331 */     this.m_tableDeleteBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apDlgButtonDeleteTable", this.m_cxt));
/* 332 */     this.m_tableDeleteBtn.setActionCommand("deleteTable");
/* 333 */     this.m_tableAddBtn.addActionListener(this);
/* 334 */     this.m_tableEditBtn.addActionListener(this);
/* 335 */     this.m_tableDeleteBtn.addActionListener(this);
/* 336 */     this.m_tableEditBtn.setEnabled(false);
/* 337 */     this.m_tableDeleteBtn.setEnabled(false);
/*     */ 
/* 339 */     buttonPanel.add("West", this.m_tableAddBtn);
/* 340 */     buttonPanel.add("Center", this.m_tableEditBtn);
/* 341 */     buttonPanel.add("East", this.m_tableDeleteBtn);
/*     */ 
/* 343 */     tablePanel.setLayout(new BorderLayout());
/* 344 */     this.m_helper.addExchangeComponent(tablePanel, tableScrollPane, "tableList");
/* 345 */     tablePanel.add("South", buttonPanel);
/* 346 */     return tablePanel;
/*     */   }
/*     */ 
/*     */   protected boolean storeTableChange(String editItems, Properties editData, boolean isDelete)
/*     */   {
/* 351 */     editData.put("EditItems", editItems);
/* 352 */     editData.put("isDelete", "" + isDelete);
/*     */     try
/*     */     {
/* 355 */       SharedContext shContext = this.m_collectionContext.getSharedContext();
/* 356 */       AppContextUtils.executeService(shContext, "EDIT_ARCHIVEDATA", editData, true);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 360 */       MessageBox.reportError(this.m_systemInterface, e);
/* 361 */       return false;
/*     */     }
/* 363 */     return true;
/*     */   }
/*     */ 
/*     */   public String computeTables(String table, boolean isDelete)
/*     */   {
/* 368 */     String str = this.m_helper.m_props.getProperty("aExportTables");
/* 369 */     Vector tables = StringUtils.parseArray(str, ',', ',');
/* 370 */     int size = tables.size();
/* 371 */     boolean isFound = false;
/* 372 */     for (int i = size - 1; i >= 0; --i)
/*     */     {
/* 374 */       String t = (String)tables.elementAt(i);
/* 375 */       isFound = t.equalsIgnoreCase(table);
/* 376 */       if (!isFound)
/*     */         continue;
/* 378 */       if (!isDelete)
/*     */         break;
/* 380 */       tables.removeElementAt(i);
/* 381 */       isFound = false;
/*     */     }
/*     */ 
/* 389 */     if ((!isFound) && (!isDelete))
/*     */     {
/* 391 */       tables.addElement(table);
/*     */     }
/* 393 */     str = StringUtils.createString(tables, ',', ',');
/* 394 */     return str;
/*     */   }
/*     */ 
/*     */   public void updateTableList(String tables)
/*     */   {
/* 399 */     this.m_helper.m_props.put("aExportTables", tables);
/* 400 */     this.m_helper.m_exchange.setComponentValue("aExportTables", tables);
/*     */   }
/*     */ 
/*     */   protected String convertToString(Vector tables)
/*     */   {
/* 405 */     StringBuffer buf = new StringBuffer();
/*     */ 
/* 407 */     if (tables == null)
/*     */     {
/* 409 */       return null;
/*     */     }
/*     */ 
/* 412 */     int size = tables.size();
/* 413 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 415 */       if (buf.length() > 0)
/*     */       {
/* 417 */         buf.append(',');
/*     */       }
/* 419 */       buf.append(tables.elementAt(i));
/*     */     }
/* 421 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public void enableDisable(boolean isEnabled)
/*     */   {
/* 427 */     CollectionData curCollection = this.m_collectionContext.getCurrentCollection();
/* 428 */     if ((curCollection != null) && (curCollection.isProxied()))
/*     */     {
/* 430 */       isEnabled = false;
/*     */     }
/* 432 */     this.m_queryEditBtn.setEnabled(isEnabled);
/* 433 */     this.m_queryViewBtn.setEnabled(isEnabled);
/* 434 */     this.m_addEditBtn.setEnabled(isEnabled);
/*     */ 
/* 436 */     this.m_tableAddBtn.setEnabled(isEnabled);
/*     */ 
/* 438 */     boolean isTableEnabled = (isEnabled) && (this.m_tableList.getSelectedIndex() > -1);
/* 439 */     this.m_tableEditBtn.setEnabled(isTableEnabled);
/* 440 */     this.m_tableDeleteBtn.setEnabled(isTableEnabled);
/* 441 */     this.m_tableQueryEditBtn.setEnabled(isTableEnabled);
/* 442 */     this.m_tableQueryViewBtn.setEnabled(isTableEnabled);
/*     */   }
/*     */ 
/*     */   protected void viewQueryResults(boolean isTable)
/*     */   {
/* 451 */     ViewData viewData = null;
/* 452 */     String table = null;
/* 453 */     if (isTable)
/*     */     {
/* 455 */       table = (String)this.m_tableList.getSelectedValue();
/* 456 */       viewData = new ViewData(3, "", table);
/* 457 */       viewData.m_viewName = ("ArchiveViewTable" + table);
/*     */     }
/*     */     else
/*     */     {
/* 461 */       viewData = new ViewData(1, "ArchiverDocuments", "DOCUMENTS");
/* 462 */       viewData.m_viewName = "ArchiverDocView";
/*     */     }
/* 464 */     viewData.m_inDateState = false;
/*     */ 
/* 466 */     ExportQueryData query = getExportQuery(table, isTable);
/*     */     try
/*     */     {
/* 471 */       boolean isUseExportDate = StringUtils.convertToBool(query.getQueryProp("UseExportDate"), false);
/*     */ 
/* 473 */       boolean isAllowPublished = StringUtils.convertToBool(query.getQueryProp("AllowExportPublished"), false);
/*     */ 
/* 477 */       String exportDate = this.m_helper.m_props.getProperty("aLastExport");
/* 478 */       String exportQuery = null;
/* 479 */       if (isTable)
/*     */       {
/* 481 */         String curDate = LocaleUtils.formatODBC(new Date());
/* 482 */         exportQuery = query.createExportTableQuery(exportDate, curDate, false);
/*     */ 
/* 484 */         String parentTable = query.getQueryProp("aParentTables");
/* 485 */         String tableRelations = query.getQueryProp("aTableRelations");
/* 486 */         if ((parentTable != null) && (parentTable.trim().length() != 0) && (tableRelations != null) && (tableRelations.trim().length() != 0))
/*     */         {
/* 489 */           viewData.m_tableName = (table + "," + query.getQueryProp("aParentTables"));
/* 490 */           exportQuery = getRelationQuery(query, exportQuery);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 495 */         exportQuery = query.createQueryString(isUseExportDate, exportDate, isAllowPublished);
/*     */       }
/*     */ 
/* 498 */       viewData.setExtraWhereClause(exportQuery, LocaleResources.getString("apTitleExportQuery", this.m_cxt));
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 503 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apUnableToLaunchPreview", new Object[0]));
/* 504 */       return;
/*     */     }
/*     */ 
/* 508 */     ViewDlg dlg = new ViewDlg(null, this.m_systemInterface, LocaleResources.getString("apLabelContentSatisfyingExportQuery", this.m_cxt), this.m_collectionContext.getSharedContext(), DialogHelpTable.getHelpPage("ViewExportQuery"));
/*     */ 
/* 511 */     dlg.init(viewData, null);
/*     */ 
/* 514 */     dlg.prompt();
/*     */   }
/*     */ 
/*     */   protected String getRelationQuery(ExportQueryData queryData, String exportQuery)
/*     */     throws ServiceException
/*     */   {
/* 520 */     DataBinder binder = new DataBinder();
/* 521 */     binder.putLocal("table", queryData.getQueryProp("aTableName"));
/* 522 */     binder.putLocal("parentTable", queryData.getQueryProp("aParentTables"));
/* 523 */     binder.putLocal("relations", queryData.getQueryProp("aTableRelations"));
/* 524 */     binder.putLocal("whereClause", exportQuery);
/* 525 */     SharedContext context = this.m_collectionContext.getSharedContext();
/*     */ 
/* 527 */     context.executeService("GET_ARCHIVERELATIONQUERY", binder, false);
/* 528 */     return binder.getLocal("QueryString");
/*     */   }
/*     */ 
/*     */   protected ExportQueryData getExportQuery(String tableName, boolean isTable)
/*     */   {
/* 533 */     String queryStr = getExportQueryString(tableName, isTable);
/*     */ 
/* 535 */     ExportQueryData queryData = new ExportQueryData();
/* 536 */     queryData.init(isTable);
/* 537 */     queryData.parse(queryStr);
/*     */ 
/* 539 */     String wildcards = SharedObjects.getEnvironmentValue("DatabaseWildcards");
/* 540 */     if (wildcards != null)
/*     */     {
/* 542 */       queryData.setWildcards(wildcards);
/*     */     }
/* 544 */     return queryData;
/*     */   }
/*     */ 
/*     */   protected String getExportQueryString(String tableName, boolean isTable)
/*     */   {
/* 549 */     if ((isTable) && (((tableName == null) || (tableName.length() == 0))))
/*     */     {
/* 551 */       return null;
/*     */     }
/*     */ 
/* 554 */     String key = "aExportQuery";
/* 555 */     if (isTable)
/*     */     {
/* 557 */       key = "aExportTable" + tableName;
/*     */     }
/*     */ 
/* 560 */     return this.m_helper.m_props.getProperty(key);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 571 */     if (name.equals("aExportQuery"))
/*     */     {
/* 573 */       if (!updateComponent)
/*     */         return;
/* 575 */       ExportQueryData query = getExportQuery(null, false);
/*     */       try
/*     */       {
/* 578 */         exchange.m_compValue = query.createQueryString();
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 582 */         MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apUnableToParseExportQueryData", new Object[0]));
/*     */       }
/*     */ 
/*     */     }
/* 586 */     else if ((name.equals("aExportQueryTable")) && (this.m_tableList.getSelectedValue() != null))
/*     */     {
/* 589 */       if (!updateComponent)
/*     */         return;
/* 591 */       String tableName = (String)this.m_tableList.getSelectedValue();
/* 592 */       ExportQueryData query = getExportQuery(tableName, true);
/*     */       try
/*     */       {
/* 595 */         exchange.m_compValue = query.createQueryString();
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 599 */         MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apUnableToParseExportQueryData", new Object[0]));
/*     */       }
/*     */ 
/*     */     }
/* 604 */     else if (name.equals("tableList"))
/*     */     {
/* 606 */       String tables = this.m_helper.m_props.getProperty("aExportTables");
/* 607 */       IdcList list = this.m_tableList;
/* 608 */       if ((tables != null) && (tables.length() != 0))
/*     */       {
/* 610 */         String selected = (String)list.getSelectedValue();
/* 611 */         Vector tableList = StringUtils.parseArray(tables, ',', ',');
/* 612 */         int size = tableList.size();
/* 613 */         DefaultListModel model = (DefaultListModel)list.getModel();
/* 614 */         this.m_disableSelectionEvent = true;
/* 615 */         model.removeAllElements();
/* 616 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 618 */           list.addElement(tableList.elementAt(i));
/*     */         }
/* 620 */         if (selected != null)
/*     */         {
/* 622 */           int index = tableList.indexOf(selected);
/* 623 */           if (index != -1)
/*     */           {
/* 625 */             list.setSelectedIndex(index);
/*     */           }
/*     */         }
/* 628 */         this.m_disableSelectionEvent = false;
/*     */       }
/*     */       else
/*     */       {
/* 632 */         DefaultListModel model = (DefaultListModel)list.getModel();
/* 633 */         this.m_disableSelectionEvent = true;
/* 634 */         model.removeAllElements();
/* 635 */         this.m_disableSelectionEvent = false;
/*     */       }
/*     */ 
/* 638 */       exchange.m_component = null;
/*     */     }
/*     */     else
/*     */     {
/* 642 */       this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/* 651 */     String action = event.getActionCommand();
/* 652 */     Properties props = (Properties)this.m_helper.m_props.clone();
/*     */ 
/* 654 */     boolean isChanged = false;
/* 655 */     boolean isDelete = false;
/* 656 */     boolean isAdd = true;
/* 657 */     String table = null;
/* 658 */     String editItems = "aExportTables";
/*     */ 
/* 660 */     if ((action.equals("addTable")) || (action.equals("editTable")))
/*     */     {
/* 662 */       String title = this.m_systemInterface.getString("apArchiverAddTable");
/* 663 */       if (action.equals("editTable"))
/*     */       {
/* 665 */         isAdd = false;
/* 666 */         table = (String)this.m_tableList.getSelectedValue();
/* 667 */         title = LocaleUtils.encodeMessage("apArchiverEditTable", null, table);
/* 668 */         title = LocaleResources.localizeMessage(title, this.m_cxt);
/*     */       }
/*     */ 
/* 671 */       EditTableDlg dlg = new EditTableDlg(this.m_systemInterface, title, this.m_collectionContext, this.m_cxt);
/*     */ 
/* 673 */       dlg.init(table, props, true, isAdd);
/* 674 */       if (dlg.prompt() == 1)
/*     */       {
/* 677 */         table = props.getProperty("aTableName");
/* 678 */         String options = dlg.createQueryString();
/* 679 */         props.put("aExportTable" + table, options);
/* 680 */         isChanged = true;
/* 681 */         editItems = editItems + ",aExportTable" + table;
/*     */       }
/*     */     }
/* 684 */     else if (action.equals("deleteTable"))
/*     */     {
/* 686 */       table = (String)this.m_tableList.getSelectedValue();
/* 687 */       if (table != null)
/*     */       {
/* 689 */         if (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apRemoveTableDlg", new Object[] { table }), 2) == 1)
/*     */         {
/* 694 */           isChanged = true;
/* 695 */           isDelete = true;
/*     */         }
/*     */       }
/*     */     }
/* 699 */     if (!isChanged) {
/*     */       return;
/*     */     }
/* 702 */     String tables = null;
/* 703 */     if ((isAdd) || (isDelete))
/*     */     {
/* 705 */       if (table == null)
/*     */       {
/* 707 */         table = props.getProperty("aTableName");
/*     */       }
/* 709 */       tables = computeTables(table, isDelete);
/* 710 */       props.put("aExportTables", tables);
/*     */     }
/*     */ 
/* 713 */     boolean isSuccess = storeTableChange(editItems, props, false);
/* 714 */     if (isDelete)
/*     */     {
/* 716 */       storeTableChange("aExportTable" + table, props, true);
/*     */     }
/*     */ 
/* 719 */     if ((!isSuccess) || ((!isAdd) && (!isDelete)))
/*     */       return;
/* 721 */     updateTableList(tables);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 728 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98778 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ExportPanel
 * JD-Core Version:    0.5.4
 */