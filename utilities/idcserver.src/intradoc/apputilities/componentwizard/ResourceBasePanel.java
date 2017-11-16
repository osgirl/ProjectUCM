/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.ShowColumnData;
/*     */ import intradoc.shared.gui.ShowColumnDlg;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ResourceBasePanel extends BaseResViewPanel
/*     */ {
/*     */   public static final int HTML_INCLUDE = 0;
/*     */   public static final int DATA_INCLUDE = 1;
/*     */   public static final int RES_TABLE = 2;
/*     */   public static final int QUERY = 3;
/*     */   public static final int SERVICE = 4;
/*     */   public static final int TEMPLATE = 5;
/*     */   public static final int ENVIRONMENT = 6;
/*     */   public static final int RES_STRING = 7;
/*  75 */   public static final String[][] SERVICE_COL_MAP = { { "Name", "Name", "30" }, { "Attributes", "Attributes", "30" }, { "Actions", "Actions", "30" } };
/*     */ 
/*  79 */   public static final String[][] QUERY_COL_MAP = { { "name", "Name", "30" }, { "queryStr", "Query", "30" }, { "parameters", "Parameters", "30" } };
/*     */ 
/*  83 */   public static final String[][] TEMPLATE_COL_MAP = { { "name", "!csCompWizLabelName", "20" }, { "filename", "!csCompWizLabelFileName", "30" } };
/*     */   protected JPanel m_tableInfoPanel;
/*     */   protected DisplayChoice m_tblChoice;
/*     */   protected LongTextCustomLabel m_tablename;
/*     */   protected LongTextCustomLabel m_mergeTable;
/*     */   protected JButton m_showColumnsBtn;
/*     */   protected String m_showColumns;
/*     */   protected ViewFields m_showColumnFields;
/*     */   protected Properties m_prevSelectedColumnsForTable;
/*     */   protected FixedSizeList m_envList;
/*     */   protected String m_curTablename;
/*     */   protected String m_prevTableName;
/*     */   protected int m_resourceType;
/*     */   protected boolean m_addMergeTable;
/*     */ 
/*     */   public ResourceBasePanel()
/*     */   {
/*  88 */     this.m_tableInfoPanel = null;
/*  89 */     this.m_tblChoice = null;
/*  90 */     this.m_tablename = null;
/*  91 */     this.m_mergeTable = null;
/*  92 */     this.m_showColumnsBtn = null;
/*  93 */     this.m_showColumns = "";
/*  94 */     this.m_showColumnFields = null;
/*  95 */     this.m_prevSelectedColumnsForTable = null;
/*  96 */     this.m_envList = null;
/*     */ 
/*  99 */     this.m_curTablename = "";
/* 100 */     this.m_prevTableName = null;
/* 101 */     this.m_resourceType = 0;
/*     */ 
/* 104 */     this.m_addMergeTable = false;
/*     */   }
/*     */ 
/*     */   public void initUI(boolean isTab, int resourceType)
/*     */   {
/* 109 */     this.m_helper.makePanelGridBag(this, 1);
/* 110 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 111 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*     */ 
/* 113 */     this.m_resourceType = resourceType;
/* 114 */     int height = 15;
/*     */ 
/* 116 */     if (this.m_resourceType == 6)
/*     */     {
/* 118 */       JPanel listPanel = new PanePanel();
/* 119 */       listPanel.setLayout(new BorderLayout());
/* 120 */       CustomLabel titleLabel = new CustomLabel(LocaleResources.getString("csCompWizLabelCustEnvParams", null), 1);
/*     */ 
/* 123 */       listPanel.add("North", titleLabel);
/* 124 */       this.m_envList = new FixedSizeList(15, 200);
/* 125 */       listPanel.add("Center", this.m_envList);
/*     */ 
/* 128 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 129 */       this.m_helper.addComponent(this, listPanel);
/*     */     }
/*     */     else
/*     */     {
/* 133 */       String tablename = "ResourceTables";
/* 134 */       String listTitle = null;
/* 135 */       String[][] columnMap = (String[][])null;
/* 136 */       String idColumn = null;
/* 137 */       height = 16;
/* 138 */       boolean setVisibleColumns = false;
/*     */ 
/* 140 */       if (isTab)
/*     */       {
/* 142 */         height = 5;
/*     */       }
/*     */ 
/* 145 */       if (this.m_resourceType == 2)
/*     */       {
/* 147 */         setVisibleColumns = true;
/* 148 */         this.m_addMergeTable = true;
/* 149 */         listTitle = "!csCompWizLabelResTables";
/*     */       }
/* 151 */       else if (CWizardUtils.isCoreResourceType(this.m_resourceType))
/*     */       {
/* 153 */         idColumn = "includeOrString";
/* 154 */         if (this.m_resourceType == 0)
/*     */         {
/* 156 */           tablename = "HtmlIncludes";
/* 157 */           listTitle = "!csCompWizLabelCustHTMLInc";
/*     */         }
/* 159 */         else if (this.m_resourceType == 1)
/*     */         {
/* 161 */           tablename = "DataIncludes";
/* 162 */           listTitle = "!csCompWizLabelCustDataInc";
/*     */         }
/*     */         else
/*     */         {
/* 166 */           tablename = "ResourceStrings";
/* 167 */           listTitle = "!csCompWizLabelCustStrings";
/*     */         }
/*     */       }
/* 170 */       else if (this.m_resourceType == 3)
/*     */       {
/* 172 */         tablename = "Queries";
/* 173 */         listTitle = "!csCompWizLabelQueries";
/* 174 */         idColumn = QUERY_COL_MAP[0][0];
/*     */       }
/* 176 */       else if (this.m_resourceType == 4)
/*     */       {
/* 178 */         tablename = "Services";
/* 179 */         listTitle = "!csCompWizLabelServices";
/* 180 */         idColumn = SERVICE_COL_MAP[0][0];
/*     */       }
/* 182 */       else if (this.m_resourceType == 5)
/*     */       {
/* 184 */         setVisibleColumns = true;
/* 185 */         tablename = "Templates";
/* 186 */         listTitle = "!csCompWizLabelTemplates";
/* 187 */         this.m_addMergeTable = true;
/* 188 */         columnMap = TEMPLATE_COL_MAP;
/* 189 */         idColumn = TEMPLATE_COL_MAP[0][0];
/*     */       }
/*     */ 
/* 192 */       this.m_list = createUdlPanel(listTitle, 250, height, tablename, setVisibleColumns, columnMap, idColumn, false);
/*     */ 
/* 195 */       if (CWizardUtils.isCoreResourceType(this.m_resourceType))
/*     */       {
/* 197 */         this.m_list.add("South", addToolBarPanel());
/*     */ 
/* 200 */         this.m_helper.addComponent(this, this.m_list);
/*     */       }
/*     */       else
/*     */       {
/* 204 */         addInfoPanel();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addInfoPanel()
/*     */   {
/* 211 */     this.m_list.add("North", this.m_tableInfoPanel = addTableInfoPanelEx(true));
/* 212 */     this.m_list.add("South", addToolBarPanel());
/*     */ 
/* 214 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/* 215 */     this.m_helper.addComponent(this, this.m_list);
/*     */   }
/*     */ 
/*     */   protected void addTableInfoPanel(boolean isSingleTable)
/*     */   {
/* 220 */     if (this.m_tableInfoPanel != null)
/*     */     {
/* 222 */       this.m_list.remove(this.m_tableInfoPanel);
/*     */     }
/* 224 */     this.m_tableInfoPanel = addTableInfoPanelEx(isSingleTable);
/* 225 */     this.m_list.add("North", this.m_tableInfoPanel);
/* 226 */     this.m_list.validate();
/*     */   }
/*     */ 
/*     */   protected JPanel addTableInfoPanelEx(boolean isSingleTable)
/*     */   {
/* 231 */     JPanel pnl = new PanePanel();
/* 232 */     this.m_helper.makePanelGridBag(pnl, 1);
/* 233 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/*     */ 
/* 235 */     gbh.m_gc.weightx = 1.0D;
/* 236 */     gbh.m_gc.weighty = 1.0D;
/* 237 */     GridBagConstraints oldGc = gbh.m_gc;
/*     */ 
/* 239 */     gbh.m_gc.weightx = 0.0D;
/* 240 */     gbh.prepareAddRowElement(18);
/* 241 */     this.m_helper.addComponent(pnl, new CustomLabel(LocaleResources.getString("csCompWizLabelTableName", null), 1));
/*     */ 
/* 244 */     gbh.prepareAddLastRowElement(18);
/* 245 */     gbh.m_gc.weightx = 10.0D;
/* 246 */     if (isSingleTable)
/*     */     {
/* 248 */       this.m_helper.addExchangeComponent(pnl, this.m_tablename = new LongTextCustomLabel(), "tablename");
/* 249 */       this.m_tblChoice = null;
/*     */     }
/*     */     else
/*     */     {
/* 253 */       this.m_helper.addExchangeComponent(pnl, this.m_tblChoice = new DisplayChoice(), "tablename");
/* 254 */       this.m_tablename = null;
/*     */ 
/* 256 */       ItemListener iListener = new ItemListener()
/*     */       {
/*     */         public void itemStateChanged(ItemEvent e)
/*     */         {
/* 260 */           int state = e.getStateChange();
/* 261 */           if (state != 1)
/*     */             return;
/* 263 */           String selItem = ResourceBasePanel.this.m_tblChoice.getSelectedInternalValue();
/* 264 */           if (selItem == null)
/*     */           {
/* 266 */             return;
/*     */           }
/*     */           try
/*     */           {
/* 270 */             ResourceBasePanel.this.selectTable(selItem);
/* 271 */             ResourceBasePanel.this.m_curTablename = selItem;
/* 272 */             ResourceBasePanel.this.m_prevTableName = ResourceBasePanel.this.m_curTablename;
/*     */           }
/*     */           catch (DataException de)
/*     */           {
/* 276 */             de.printStackTrace();
/*     */           }
/* 278 */           ResourceBasePanel.this.refreshList(null);
/*     */         }
/*     */       };
/* 282 */       this.m_tblChoice.addItemListener(iListener);
/*     */     }
/*     */ 
/* 285 */     if (this.m_addMergeTable)
/*     */     {
/* 287 */       gbh.prepareAddRowElement();
/* 288 */       gbh.m_gc.weightx = 0.0D;
/* 289 */       this.m_helper.addComponent(pnl, new CustomLabel(LocaleResources.getString("csCompWizLabelMergeTo", null), 1));
/*     */ 
/* 291 */       gbh.prepareAddLastRowElement();
/* 292 */       gbh.m_gc.weightx = 10.0D;
/* 293 */       this.m_helper.addExchangeComponent(pnl, this.m_mergeTable = new LongTextCustomLabel(), "mergeTable");
/*     */     }
/* 295 */     this.m_helper.m_gridHelper.m_gc = oldGc;
/*     */ 
/* 297 */     if (this.m_resourceType == 2)
/*     */     {
/* 299 */       gbh.prepareAddLastRowElement();
/* 300 */       this.m_helper.addComponent(pnl, getShowColumnsPanel());
/* 301 */       this.m_prevSelectedColumnsForTable = new Properties();
/*     */     }
/* 303 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected JPanel addToolBarPanel()
/*     */   {
/* 310 */     JPanel panel = null;
/*     */ 
/* 312 */     if ((this.m_resourceType == 2) || (this.m_resourceType == 6))
/*     */     {
/* 314 */       panel = new PanePanel();
/* 315 */       panel.setLayout(new FlowLayout());
/*     */     }
/*     */     else
/*     */     {
/* 319 */       panel = super.addToolBarPanel();
/* 320 */       if (this.m_resourceType == 5)
/*     */       {
/* 322 */         JButton btn = this.m_list.addButton(LocaleResources.getString("csCompWizLanchEditor", null), true);
/* 323 */         btn.setActionCommand("launch");
/* 324 */         btn.addActionListener(this);
/* 325 */         panel.add(btn);
/*     */       }
/*     */     }
/* 328 */     return panel;
/*     */   }
/*     */ 
/*     */   public void assignResourceFileInfo(ResourceFileInfo info)
/*     */   {
/* 335 */     ResourceFileInfo oldInfo = null;
/* 336 */     if (this.m_fileInfo != null)
/*     */     {
/* 338 */       oldInfo = this.m_fileInfo;
/*     */     }
/*     */ 
/* 341 */     String oldTableName = null;
/* 342 */     if (this.m_tblChoice != null)
/*     */     {
/* 344 */       oldTableName = this.m_tblChoice.getSelectedInternalValue();
/*     */     }
/*     */ 
/* 347 */     super.assignResourceFileInfo(info);
/* 348 */     if (info == null)
/*     */     {
/* 350 */       return;
/*     */     }
/*     */ 
/* 353 */     if (CWizardUtils.isCoreResourceType(this.m_resourceType))
/*     */     {
/* 355 */       this.m_listData = ((DataResultSet)CWizardUtils.buildIncludeOrStringResultSetByType(info, this.m_resourceType));
/*     */     }
/* 357 */     else if (this.m_resourceType != 6)
/*     */     {
/* 359 */       if (this.m_resourceType == 2)
/*     */       {
/* 361 */         if (this.m_tablename != null)
/*     */         {
/* 363 */           this.m_prevTableName = this.m_tablename.getText();
/*     */         }
/* 365 */         else if (this.m_tblChoice != null)
/*     */         {
/* 367 */           this.m_prevTableName = this.m_tblChoice.getSelectedInternalValue();
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 372 */       String tablename = null;
/* 373 */       boolean isSingleTable = true;
/* 374 */       if (info.m_tables.size() > 1)
/*     */       {
/* 376 */         isSingleTable = false;
/*     */       }
/*     */ 
/* 379 */       addTableInfoPanel(isSingleTable);
/*     */ 
/* 381 */       if (!isSingleTable)
/*     */       {
/* 383 */         Vector opt = new IdcVector();
/* 384 */         for (Enumeration en = info.m_tables.keys(); en.hasMoreElements(); )
/*     */         {
/* 386 */           opt.addElement(en.nextElement());
/*     */         }
/*     */ 
/* 389 */         this.m_tblChoice.init(opt);
/*     */ 
/* 391 */         if ((oldTableName != null) && (oldTableName.length() > 0) && (oldInfo != null) && (oldInfo == info))
/*     */         {
/* 394 */           this.m_tblChoice.setSelectedItem(oldTableName);
/* 395 */           tablename = oldTableName;
/*     */         }
/*     */         else
/*     */         {
/* 399 */           this.m_tblChoice.setSelectedIndex(0);
/* 400 */           tablename = this.m_tblChoice.getSelectedInternalValue();
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 405 */         Enumeration en = info.m_tables.keys(); if (en.hasMoreElements())
/*     */         {
/* 407 */           tablename = (String)en.nextElement();
/* 408 */           this.m_tablename.setText(tablename);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 413 */       if (tablename == null)
/*     */       {
/* 415 */         this.m_listData = null;
/* 416 */         this.m_tablename.setText(LocaleResources.getString("csCompWizLabelNone", null));
/*     */ 
/* 418 */         if (this.m_addMergeTable)
/*     */         {
/* 420 */           this.m_mergeTable.setText(LocaleResources.getString("csCompWizLabelNone", null));
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/*     */         try
/*     */         {
/* 427 */           selectTable(tablename);
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 431 */           e.printStackTrace();
/*     */         }
/* 433 */         this.m_curTablename = tablename;
/* 434 */         this.m_helper.m_props.put("tablename", tablename);
/*     */       }
/*     */     }
/* 437 */     refreshList(null);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 443 */     String cmdStr = e.getActionCommand();
/*     */ 
/* 445 */     if (cmdStr.equals("launch"))
/*     */     {
/* 447 */       launchEditor();
/*     */     }
/*     */     else
/*     */     {
/* 451 */       super.actionPerformed(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void launchEditor()
/*     */   {
/* 457 */     int index = this.m_list.getSelectedIndex();
/* 458 */     if (index < 0)
/*     */     {
/* 461 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizTemplateEditError", new Object[0]));
/*     */ 
/* 463 */       return;
/*     */     }
/*     */ 
/* 468 */     Properties props = this.m_list.getDataAt(index);
/* 469 */     String filename = props.getProperty("filename");
/* 470 */     filename = FileUtils.getAbsolutePath(FileUtils.getDirectory(this.m_fileInfo.m_filename), filename);
/*     */ 
/* 473 */     CWizardGuiUtils.launchEditor(this.m_systemInterface, filename);
/*     */   }
/*     */ 
/*     */   protected JPanel getShowColumnsPanel()
/*     */   {
/* 478 */     this.m_showColumnsBtn = new JButton(LocaleResources.getString("csCompWizLabelShowColumns", null));
/*     */ 
/* 480 */     ActionListener showListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 484 */         ShowColumnDlg dlg = new ShowColumnDlg(ResourceBasePanel.this.m_systemInterface, LocaleResources.getString("csCompWizLabelShowColumns2", null));
/*     */ 
/* 486 */         ShowColumnData clmnData = new ShowColumnData();
/* 487 */         clmnData.m_columnStr = ResourceBasePanel.this.m_showColumns;
/* 488 */         clmnData.m_columnFields = ResourceBasePanel.this.m_showColumnFields;
/*     */ 
/* 490 */         dlg.init(clmnData);
/* 491 */         if (dlg.prompt() != 1)
/*     */           return;
/* 493 */         dlg.buildShowColumns();
/* 494 */         ResourceBasePanel.this.m_showColumns = clmnData.m_columnStr;
/* 495 */         ResourceBasePanel.this.setVisibleColumns(ResourceBasePanel.this.m_curTablename, ResourceBasePanel.this.m_showColumns);
/* 496 */         ResourceBasePanel.this.refreshList(null);
/*     */       }
/*     */     };
/* 500 */     this.m_showColumnsBtn.addActionListener(showListener);
/*     */ 
/* 502 */     JPanel clmnPanel = new PanePanel();
/*     */ 
/* 504 */     clmnPanel.setLayout(new BorderLayout());
/* 505 */     clmnPanel.add("East", this.m_showColumnsBtn);
/* 506 */     return clmnPanel;
/*     */   }
/*     */ 
/*     */   protected void selectTable(String tablename) throws DataException
/*     */   {
/* 511 */     this.m_listData = ((DataResultSet)this.m_fileInfo.m_tables.get(tablename));
/*     */ 
/* 513 */     if (this.m_addMergeTable)
/*     */     {
/* 515 */       String mergeTable = LocaleResources.getString("csCompWizLabelNone", null);
/* 516 */       this.m_mergeRules = this.m_component.getMergeRulesTable();
/*     */ 
/* 518 */       if (this.m_mergeRules != null)
/*     */       {
/* 520 */         FieldInfo[] info = ResultSetUtils.createInfoList(this.m_mergeRules, new String[] { "fromTable", "toTable" }, true);
/*     */ 
/* 522 */         Vector v = this.m_mergeRules.findRow(info[0].m_index, tablename);
/* 523 */         if (v != null)
/*     */         {
/* 525 */           mergeTable = (String)v.elementAt(info[1].m_index);
/*     */         }
/*     */       }
/* 528 */       this.m_mergeTable.setText(mergeTable);
/* 529 */       this.m_helper.m_props.put("mergeTable", mergeTable);
/*     */     }
/* 531 */     this.m_helper.m_props.put("tablename", tablename);
/*     */ 
/* 533 */     if (this.m_resourceType != 2)
/*     */       return;
/* 535 */     boolean doRefresh = true;
/*     */ 
/* 537 */     if ((this.m_prevTableName != null) && (this.m_prevTableName.equals(tablename)))
/*     */     {
/* 539 */       boolean doMatch = true;
/*     */ 
/* 541 */       if ((this.m_showColumnFields != null) && (this.m_showColumnFields.m_viewFields != null) && (this.m_showColumnFields.m_viewFields.size() > 0))
/*     */       {
/* 545 */         for (int i = 0; i < this.m_showColumnFields.m_viewFields.size(); ++i)
/*     */         {
/* 547 */           ViewFieldDef vfd = (ViewFieldDef)this.m_showColumnFields.m_viewFields.elementAt(i);
/*     */ 
/* 549 */           String col = this.m_listData.getFieldName(i);
/*     */ 
/* 551 */           if (col.equals(vfd.m_name))
/*     */             continue;
/* 553 */           doMatch = false;
/* 554 */           break;
/*     */         }
/*     */ 
/* 558 */         if (doMatch)
/*     */         {
/* 560 */           doRefresh = false;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 565 */     if (!doRefresh) {
/*     */       return;
/*     */     }
/*     */ 
/* 569 */     String col = null;
/* 570 */     int numFields = this.m_listData.getNumFields();
/*     */ 
/* 572 */     if (this.m_prevSelectedColumnsForTable != null)
/*     */     {
/* 576 */       col = this.m_prevSelectedColumnsForTable.getProperty(tablename);
/*     */     }
/* 578 */     if (col == null)
/*     */     {
/* 580 */       col = "";
/*     */ 
/* 582 */       if (numFields > 0)
/*     */       {
/* 584 */         col = this.m_listData.getFieldName(0);
/* 585 */         if (numFields > 1)
/*     */         {
/* 587 */           col = col + ",";
/* 588 */           col = col + this.m_listData.getFieldName(1);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 593 */     setVisibleColumns(tablename, col);
/*     */ 
/* 596 */     this.m_showColumns = col;
/* 597 */     this.m_showColumnFields = new ViewFields(null);
/* 598 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 600 */       String fname = this.m_listData.getFieldName(i);
/* 601 */       this.m_showColumnFields.addField(fname, fname);
/*     */     }
/*     */ 
/* 604 */     if ((this.m_showColumns == null) || (this.m_showColumns.length() == 0))
/*     */     {
/* 606 */       this.m_showColumnsBtn.setEnabled(false);
/*     */     }
/*     */     else
/*     */     {
/* 610 */       this.m_showColumnsBtn.setEnabled(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void setVisibleColumns(String tablename, String cols)
/*     */   {
/* 618 */     if (this.m_prevSelectedColumnsForTable != null)
/*     */     {
/* 620 */       this.m_prevSelectedColumnsForTable.put(tablename, cols);
/*     */     }
/* 622 */     this.m_list.setVisibleColumns(cols);
/*     */   }
/*     */ 
/*     */   protected void refreshList(String selObj)
/*     */   {
/*     */     Properties props;
/*     */     Enumeration en;
/* 628 */     if (this.m_resourceType == 6)
/*     */     {
/* 630 */       this.m_envList.removeAllItems();
/* 631 */       props = this.m_fileInfo.m_environments;
/*     */ 
/* 633 */       for (en = props.keys(); en.hasMoreElements(); )
/*     */       {
/* 635 */         String name = (String)en.nextElement();
/* 636 */         String value = props.getProperty(name);
/* 637 */         this.m_envList.add(name + "=" + value);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 642 */       super.refreshList(selObj);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void updateResourceFileInfo()
/*     */     throws ServiceException
/*     */   {
/* 649 */     IdcMessage selmsg = null;
/* 650 */     IdcMessage msg = null;
/* 651 */     String type = null;
/* 652 */     String columnName = null;
/* 653 */     String tablename = this.m_helper.m_props.getProperty("tablename");
/* 654 */     String mergeTable = this.m_helper.m_props.getProperty("mergeTable");
/* 655 */     String filename = this.m_helper.m_props.getProperty("filename");
/* 656 */     Vector mergeTableInfo = null;
/*     */ 
/* 658 */     if ((filename == null) || (filename.length() == 0))
/*     */     {
/* 660 */       filename = this.m_fileInfo.m_filename;
/*     */     }
/*     */ 
/* 663 */     if ((this.m_resourceType == 0) || (this.m_resourceType == 1))
/*     */     {
/* 665 */       type = "htmlIncludeOrString";
/* 666 */       columnName = "includeOrString";
/* 667 */       boolean isHtmlInclude = this.m_resourceType == 0;
/*     */ 
/* 669 */       if (this.m_editType == 1)
/*     */       {
/* 671 */         msg = (isHtmlInclude) ? IdcMessageFactory.lc("csCompWizLabelAddHTMLResIncl", new Object[0]) : IdcMessageFactory.lc("csCompWizLabelAddDataResIncl", new Object[0]);
/*     */       }
/* 673 */       else if (this.m_editType == 2)
/*     */       {
/* 675 */         msg = (isHtmlInclude) ? IdcMessageFactory.lc("csCompWizLabelAddHTMLResInclFor", new Object[0]) : IdcMessageFactory.lc("csCompWizLabelAddDataResInclFor", new Object[0]);
/* 676 */         selmsg = (isHtmlInclude) ? IdcMessageFactory.lc("csCompWizHtmlResInclEditError", new Object[0]) : IdcMessageFactory.lc("csCompWizDataResInclEditError", new Object[0]);
/*     */       }
/* 678 */       else if (this.m_editType == 3)
/*     */       {
/* 680 */         msg = (isHtmlInclude) ? IdcMessageFactory.lc("csCompWizLabelAddHTMLResInclFor", new Object[0]) : IdcMessageFactory.lc("csCompWizLabelAddDataResInclFor", new Object[0]);
/* 681 */         selmsg = (isHtmlInclude) ? IdcMessageFactory.lc("csCompWizHTMLResInclDeleteError", new Object[0]) : IdcMessageFactory.lc("csCompWizDataResInclDeleteError", new Object[0]);
/*     */       }
/*     */     }
/* 684 */     else if (this.m_resourceType == 7)
/*     */     {
/* 686 */       type = "htmlIncludeOrString";
/* 687 */       columnName = "includeOrString";
/*     */ 
/* 689 */       if (this.m_editType == 1)
/*     */       {
/* 691 */         msg = IdcMessageFactory.lc("csCompWizLabelAddString", new Object[0]);
/*     */       }
/* 693 */       else if (this.m_editType == 2)
/*     */       {
/* 695 */         msg = IdcMessageFactory.lc("csCompWizLabelAddStringFor", new Object[0]);
/* 696 */         selmsg = IdcMessageFactory.lc("csCompWizStingEditError", new Object[0]);
/*     */       }
/* 698 */       else if (this.m_editType == 3)
/*     */       {
/* 700 */         msg = IdcMessageFactory.lc("csCompWizLabelAddStringFor", new Object[0]);
/* 701 */         selmsg = IdcMessageFactory.lc("csCompWizStringDeleteError", new Object[0]);
/*     */       }
/*     */     }
/* 704 */     else if (this.m_resourceType == 2)
/*     */     {
/* 706 */       boolean isDynTable = filename.endsWith(".hda");
/*     */ 
/* 708 */       if (isDynTable)
/*     */       {
/* 710 */         type = "dynResTable";
/*     */       }
/*     */       else
/*     */       {
/* 714 */         type = "staticResTable";
/*     */       }
/*     */     }
/* 717 */     else if (this.m_resourceType == 3)
/*     */     {
/* 719 */       type = "query";
/* 720 */       columnName = "name";
/* 721 */       tablename = this.m_helper.m_props.getProperty("tablename");
/* 722 */       mergeTableInfo = this.m_component.retreiveMergeTableInfo(type, mergeTable);
/*     */ 
/* 724 */       if (this.m_editType == 1)
/*     */       {
/* 726 */         msg = IdcMessageFactory.lc("csCompWizLabelAddQuery", new Object[0]);
/*     */       }
/* 728 */       else if (this.m_editType == 2)
/*     */       {
/* 730 */         selmsg = IdcMessageFactory.lc("csCompWizQueryEditError", new Object[0]);
/* 731 */         msg = IdcMessageFactory.lc("csCompWizLabelEditQueryFor", new Object[0]);
/*     */       }
/* 733 */       else if (this.m_editType == 3)
/*     */       {
/* 735 */         selmsg = IdcMessageFactory.lc("csCompWizQueryDeleteError", new Object[0]);
/*     */       }
/*     */     }
/* 738 */     else if (this.m_resourceType == 4)
/*     */     {
/* 740 */       type = "service";
/* 741 */       columnName = "Name";
/* 742 */       tablename = this.m_helper.m_props.getProperty("tablename");
/* 743 */       mergeTableInfo = this.m_component.retreiveMergeTableInfo(type, mergeTable);
/*     */ 
/* 745 */       if (this.m_editType == 1)
/*     */       {
/* 747 */         msg = IdcMessageFactory.lc("csCompWizLabelAddSvc", new Object[0]);
/*     */       }
/* 749 */       else if (this.m_editType == 2)
/*     */       {
/* 751 */         msg = IdcMessageFactory.lc("csCompWizLabelEditSvcFor", new Object[0]);
/* 752 */         selmsg = IdcMessageFactory.lc("csCompWizSvcEditError", new Object[0]);
/*     */       }
/* 754 */       else if (this.m_editType == 3)
/*     */       {
/* 756 */         selmsg = IdcMessageFactory.lc("csCompWizSvcDeleteError", new Object[0]);
/*     */       }
/*     */     }
/* 759 */     else if (this.m_resourceType == 5)
/*     */     {
/* 761 */       type = "template";
/* 762 */       columnName = "name";
/* 763 */       mergeTableInfo = this.m_component.retreiveMergeTableInfo(type, mergeTable);
/*     */ 
/* 765 */       if (mergeTableInfo.size() == 0)
/*     */       {
/* 767 */         String tTablename = this.m_helper.m_props.getProperty("tablename");
/*     */         String errMsg;
/*     */         String errMsg;
/* 769 */         if ((mergeTable.equalsIgnoreCase("null")) || (mergeTable.equalsIgnoreCase("none")))
/*     */         {
/* 771 */           errMsg = "csCompWizMergeRuleUndefinedFor";
/*     */         }
/*     */         else
/*     */         {
/* 775 */           errMsg = "csCompWizInvalidMergeRule";
/*     */         }
/*     */ 
/* 778 */         throw new ServiceException(LocaleUtils.encodeMessage(errMsg, null, tTablename));
/*     */       }
/*     */ 
/* 781 */       if (this.m_editType == 1)
/*     */       {
/* 783 */         msg = IdcMessageFactory.lc("csCompWizLabelAddTempl1", new Object[0]);
/*     */       }
/* 785 */       else if (this.m_editType == 2)
/*     */       {
/* 787 */         msg = IdcMessageFactory.lc("csCompWizLabelEditTemplFor1", new Object[0]);
/* 788 */         selmsg = IdcMessageFactory.lc("csCompWizTemplEditError", new Object[0]);
/*     */       }
/* 790 */       else if (this.m_editType == 3)
/*     */       {
/* 792 */         selmsg = IdcMessageFactory.lc("csCompWizTemplDeleteError", new Object[0]);
/*     */       }
/*     */ 
/* 795 */       if (this.m_editType != 3)
/*     */       {
/* 797 */         if ((mergeTable != null) && (mergeTable.equals("SearchResultTemplates")))
/*     */         {
/* 799 */           msg.m_prior = IdcMessageFactory.lc("csCompWizLabelAddSRTemplate", new Object[0]);
/*     */         }
/*     */         else
/*     */         {
/* 803 */           msg.m_prior = IdcMessageFactory.lc("csCompWizLabelAddIntradocTemplate", new Object[0]);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 808 */     Properties props = new Properties();
/* 809 */     props.put("type", type);
/* 810 */     props.put("columnName", columnName);
/* 811 */     props.put("filename", filename);
/* 812 */     CWizardUtils.clearCoreResourceTypeFromCheckboxes(props, this.m_resourceType);
/*     */ 
/* 814 */     if ((tablename != null) && (tablename.length() > 0))
/*     */     {
/* 816 */       props.put("tablename", tablename);
/*     */     }
/*     */ 
/* 819 */     if ((mergeTable != null) && (mergeTable.length() > 0))
/*     */     {
/* 821 */       props.put("mergeTable", mergeTable);
/*     */     }
/*     */ 
/* 824 */     if ((mergeTableInfo != null) && (mergeTableInfo.size() > 0))
/*     */     {
/* 826 */       String mergeTableColumns = (String)mergeTableInfo.elementAt(1);
/* 827 */       String mergeColumn = (String)mergeTableInfo.elementAt(2);
/* 828 */       props.put("mergeTableColumns", mergeTableColumns);
/* 829 */       props.put("mergeColumn", mergeColumn);
/*     */     }
/*     */ 
/* 832 */     addOrEditOrDelete(props, selmsg, msg);
/*     */   }
/*     */ 
/*     */   protected void addOrEdit(String type)
/*     */     throws ServiceException
/*     */   {
/* 838 */     String mergetable = null;
/* 839 */     String helpPage = null;
/*     */ 
/* 841 */     if ((this.m_resourceType == 0) || (this.m_resourceType == 1) || (this.m_resourceType == 7))
/*     */     {
/* 843 */       this.m_editPanel = new EditHtmlIncludePanel();
/* 844 */       this.m_editPanel.setResourceType(this.m_resourceType);
/* 845 */       helpPage = "CW_AddEditInclude";
/*     */     }
/* 847 */     else if (this.m_resourceType == 3)
/*     */     {
/* 849 */       this.m_editPanel = new EditQueryPanel();
/* 850 */       helpPage = "CW_AddEditQuery";
/*     */     }
/* 852 */     else if (this.m_resourceType == 4)
/*     */     {
/* 854 */       this.m_editPanel = new EditServicePanel();
/* 855 */       helpPage = "CW_AddEditService";
/*     */     }
/* 857 */     else if (this.m_resourceType == 5)
/*     */     {
/* 859 */       mergetable = this.m_helper.m_props.getProperty("mergeTable");
/* 860 */       this.m_editPanel = new EditTemplatePanel();
/*     */ 
/* 862 */       if (mergetable == null)
/*     */       {
/* 864 */         CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizMergeTableUndefined", new Object[0]));
/*     */ 
/* 866 */         return;
/*     */       }
/*     */ 
/* 869 */       if (mergetable.equals("IntradocTemplates"))
/*     */       {
/* 871 */         helpPage = "CW_AddEditIntradocTemplate";
/*     */       }
/*     */       else
/*     */       {
/* 875 */         helpPage = "CW_AddEditResultTemplate";
/*     */       }
/*     */     }
/*     */ 
/* 879 */     this.m_editPanel.init(this.m_component, mergetable, this.m_dlgHelper, null, this.m_editType);
/* 880 */     this.m_dlgHelper.m_helpPage = DialogHelpTable.getHelpPage(helpPage);
/*     */ 
/* 882 */     super.addOrEdit(type);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 887 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79062 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.ResourceBasePanel
 * JD-Core Version:    0.5.4
 */