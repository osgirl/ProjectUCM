/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.ClauseDisplay;
/*     */ import intradoc.shared.ClausesData;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.ExportQueryData;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.gui.ValueNode;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.event.TreeSelectionEvent;
/*     */ import javax.swing.event.TreeSelectionListener;
/*     */ import javax.swing.tree.DefaultTreeSelectionModel;
/*     */ 
/*     */ public class ImportPanel extends ArchiverPanel
/*     */   implements ActionListener
/*     */ {
/*     */   protected JButton m_fieldEditBtn;
/*     */   protected JButton m_valueEditBtn;
/*     */   protected JButton m_tableFieldEditBtn;
/*     */   protected JButton m_tableValueEditBtn;
/*     */   protected ClausesData m_fieldMaps;
/*     */   protected ClausesData m_valueMaps;
/*     */   protected boolean m_isImportTable;
/*     */   protected String m_currentTable;
/*     */   protected static final int CONTENT = 0;
/*     */   protected static final int TABLE = 1;
/*     */   protected JButton m_tableAddOptionsBtn;
/*     */   protected JButton m_tableEditBtn;
/*     */   protected JButton m_tableEditFromFileBtn;
/*     */   protected ImportTableList m_tableList;
/*     */   protected boolean m_loadTableList;
/*     */ 
/*     */   public ImportPanel()
/*     */   {
/*  61 */     this.m_fieldEditBtn = null;
/*  62 */     this.m_valueEditBtn = null;
/*  63 */     this.m_tableFieldEditBtn = null;
/*  64 */     this.m_tableValueEditBtn = null;
/*     */ 
/*  66 */     this.m_fieldMaps = null;
/*  67 */     this.m_valueMaps = null;
/*     */ 
/*  69 */     this.m_isImportTable = false;
/*  70 */     this.m_currentTable = null;
/*     */ 
/*  76 */     this.m_tableAddOptionsBtn = null;
/*  77 */     this.m_tableEditBtn = null;
/*  78 */     this.m_tableEditFromFileBtn = null;
/*     */ 
/*  80 */     this.m_tableList = null;
/*  81 */     this.m_loadTableList = true;
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys, CollectionContext context)
/*     */   {
/*  86 */     super.init(sys, context);
/*  87 */     initMaps();
/*     */   }
/*     */ 
/*     */   protected void initMaps()
/*     */   {
/*  92 */     this.m_fieldMaps = new ClausesData();
/*  93 */     ClauseDisplay fieldDisplay = new ClauseDisplay()
/*     */     {
/*     */       public void createClauseString(Vector elts, IdcStringBuilder dispStr)
/*     */       {
/*  97 */         int size = elts.size();
/*  98 */         for (int j = 0; j < size; ++j)
/*     */         {
/* 100 */           if (j == 1)
/*     */           {
/* 102 */             dispStr.append(" --> ");
/*     */           }
/* 104 */           String str = (String)elts.elementAt(j);
/* 105 */           dispStr.append(str);
/*     */         }
/*     */       }
/*     */     };
/* 109 */     this.m_fieldMaps.setClauseDisplay(fieldDisplay, "\n");
/*     */ 
/* 111 */     this.m_valueMaps = new ClausesData();
/* 112 */     ClauseDisplay valueDisplay = new ClauseDisplay()
/*     */     {
/*     */       public void createClauseString(Vector elts, IdcStringBuilder dispStr)
/*     */       {
/* 116 */         boolean isAll = StringUtils.convertToBool((String)elts.elementAt(0), false);
/* 117 */         int i = 1;
/* 118 */         if (isAll)
/*     */         {
/* 120 */           dispStr.append(LocaleResources.getString("apConvertAllValues", ImportPanel.this.m_cxt));
/* 121 */           i = 2;
/*     */         }
/*     */ 
/* 124 */         int size = elts.size();
/* 125 */         for (; i < size; ++i)
/*     */         {
/* 127 */           String value = (String)elts.elementAt(i);
/* 128 */           if (i == 1)
/*     */           {
/* 130 */             dispStr.append(value);
/*     */           }
/* 132 */           if (i == 2)
/*     */           {
/* 134 */             dispStr.append(LocaleResources.getString("apLabelInFieldClause", ImportPanel.this.m_cxt, value));
/*     */           }
/*     */           else {
/* 137 */             if (i != 3)
/*     */               continue;
/* 139 */             dispStr.append(LocaleResources.getString("apLabelToFieldClause", ImportPanel.this.m_cxt, value));
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 145 */     this.m_valueMaps.setClauseDisplay(valueDisplay, "\n");
/*     */   }
/*     */ 
/*     */   public JPanel initUI()
/*     */   {
/* 151 */     TabPanel tab = new TabPanel();
/* 152 */     tab.setFullWidthTab(true);
/* 153 */     this.m_isImportTable = false;
/* 154 */     tab.addPane(LocaleResources.getString("apContentTabLabel", this.m_cxt), initUI(0));
/*     */ 
/* 156 */     this.m_isImportTable = true;
/* 157 */     tab.addPane(LocaleResources.getString("apTableTabLabel", this.m_cxt), initUI(1));
/*     */ 
/* 159 */     JPanel panel = new PanePanel();
/* 160 */     this.m_helper.addComponent(panel, tab);
/* 161 */     return tab;
/*     */   }
/*     */ 
/*     */   public JPanel initUI(int type)
/*     */   {
/* 171 */     JPanel infoPanel = initMapUI(type);
/*     */ 
/* 173 */     JPanel wrapper = new PanePanel();
/* 174 */     wrapper.setLayout(new BorderLayout());
/* 175 */     if (type == 1)
/*     */     {
/* 177 */       JPanel tablePanel = initTableUI();
/* 178 */       wrapper.add("West", tablePanel);
/*     */     }
/* 180 */     wrapper.add("Center", infoPanel);
/*     */ 
/* 182 */     return wrapper;
/*     */   }
/*     */ 
/*     */   public JPanel initMapUI(int type)
/*     */   {
/* 187 */     JPanel fieldMapPanel = initFieldMapsUI(type);
/* 188 */     JPanel valueMapPanel = initValueMapsUI(type);
/*     */ 
/* 190 */     JPanel wrapper = new PanePanel();
/* 191 */     this.m_helper.makePanelGridBag(wrapper, 1);
/* 192 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 193 */     this.m_helper.addLastComponentInRow(wrapper, fieldMapPanel);
/* 194 */     this.m_helper.addLastComponentInRow(wrapper, valueMapPanel);
/*     */ 
/* 196 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected JPanel initFieldMapsUI(int type)
/*     */   {
/* 201 */     String suffix = "";
/* 202 */     if (type == 1)
/*     */     {
/* 204 */       suffix = "Table";
/*     */     }
/* 206 */     JPanel fieldPanel = new PanePanel();
/* 207 */     this.m_helper.makePanelGridBag(fieldPanel, 1);
/* 208 */     this.m_helper.addPanelTitle(fieldPanel, LocaleResources.getString("apLabelFieldMaps", this.m_cxt));
/*     */ 
/* 210 */     Component comp = new CustomTextArea(3, 20);
/* 211 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 212 */     this.m_helper.m_gridHelper.m_gc.gridwidth = 1;
/* 213 */     this.m_helper.addExchangeComponent(fieldPanel, comp, "aFieldMaps" + suffix);
/* 214 */     comp.setEnabled(false);
/*     */ 
/* 217 */     JPanel btnPanel = new PanePanel();
/* 218 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 220 */     JButton fieldEditBtn = null;
/* 221 */     if (type == 0)
/*     */     {
/* 223 */       this.m_fieldEditBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 224 */       this.m_fieldEditBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apLabelEditFieldMaps", this.m_cxt));
/* 225 */       fieldEditBtn = this.m_fieldEditBtn;
/*     */     }
/*     */     else
/*     */     {
/* 229 */       this.m_tableFieldEditBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 230 */       this.m_tableFieldEditBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apLabelEditFieldMaps", this.m_cxt));
/* 231 */       fieldEditBtn = this.m_tableFieldEditBtn;
/*     */     }
/* 233 */     fieldEditBtn.setActionCommand("editFields" + suffix);
/* 234 */     fieldEditBtn.addActionListener(this);
/*     */ 
/* 236 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 237 */     this.m_helper.addComponent(btnPanel, fieldEditBtn);
/*     */ 
/* 239 */     this.m_helper.addLastComponentInRow(fieldPanel, btnPanel);
/*     */ 
/* 241 */     return fieldPanel;
/*     */   }
/*     */ 
/*     */   protected JPanel initValueMapsUI(int type)
/*     */   {
/* 246 */     String suffix = "";
/* 247 */     if (type == 1)
/*     */     {
/* 249 */       suffix = "Table";
/*     */     }
/* 251 */     JPanel valPanel = new PanePanel();
/* 252 */     this.m_helper.makePanelGridBag(valPanel, 1);
/* 253 */     this.m_helper.addPanelTitle(valPanel, LocaleResources.getString("apLabelValueMaps", this.m_cxt));
/*     */ 
/* 256 */     Component comp = new CustomTextArea(3, 20);
/* 257 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 258 */     this.m_helper.m_gridHelper.m_gc.gridwidth = 1;
/* 259 */     this.m_helper.addExchangeComponent(valPanel, comp, "aValueMaps" + suffix);
/* 260 */     comp.setEnabled(false);
/*     */ 
/* 263 */     JPanel btnPanel = new PanePanel();
/* 264 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 266 */     JButton valueEditBtn = null;
/* 267 */     if (type == 0)
/*     */     {
/* 269 */       this.m_valueEditBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 270 */       this.m_valueEditBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apLabelEditValueMaps", this.m_cxt));
/* 271 */       valueEditBtn = this.m_valueEditBtn;
/*     */     }
/*     */     else
/*     */     {
/* 275 */       this.m_tableValueEditBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 276 */       this.m_tableValueEditBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apLabelEditValueMaps", this.m_cxt));
/* 277 */       valueEditBtn = this.m_tableValueEditBtn;
/*     */     }
/* 279 */     valueEditBtn.setActionCommand("editValues" + suffix);
/* 280 */     valueEditBtn.addActionListener(this);
/*     */ 
/* 282 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 283 */     this.m_helper.addLastComponentInRow(btnPanel, valueEditBtn);
/*     */ 
/* 285 */     this.m_helper.addLastComponentInRow(valPanel, btnPanel);
/*     */ 
/* 287 */     return valPanel;
/*     */   }
/*     */ 
/*     */   public JPanel initTableUI()
/*     */   {
/* 292 */     JPanel tablePanel = new PanePanel();
/* 293 */     this.m_tableList = new ImportTableList();
/* 294 */     this.m_tableList.init(this.m_systemInterface, this.m_collectionContext);
/* 295 */     TreeSelectionListener listener = new TreeSelectionListener()
/*     */     {
/*     */       public void valueChanged(TreeSelectionEvent event)
/*     */       {
/* 299 */         boolean isSelected = event.isAddedPath();
/* 300 */         boolean isEnabled = isSelected;
/* 301 */         String cat = ImportPanel.this.m_tableList.getSelectedItemName(true);
/* 302 */         boolean isArchiverConfig = (cat.equals("Archiver")) && (isSelected);
/* 303 */         String archiveName = ImportPanel.this.m_helper.m_props.getProperty("aArchiveName");
/* 304 */         if (isEnabled)
/*     */         {
/* 306 */           isEnabled = cat.equals("Table");
/*     */         }
/* 308 */         String name = ImportPanel.this.m_tableList.getSelectedItemName();
/* 309 */         if ((name == null) || ((archiveName != null) && (name.equals(archiveName))))
/*     */         {
/* 311 */           name = "";
/*     */         }
/* 313 */         boolean isTableEdit = ImportPanel.this.m_helper.m_props.containsKey("aImportTable" + name);
/* 314 */         ImportPanel.this.m_tableEditBtn.setEnabled((isTableEdit) && (isSelected));
/* 315 */         ImportPanel.this.m_tableAddOptionsBtn.setEnabled((((isEnabled) || (isArchiverConfig))) && (!isTableEdit));
/* 316 */         ImportPanel.this.m_tableEditFromFileBtn.setEnabled(isEnabled);
/* 317 */         ImportPanel.this.m_tableFieldEditBtn.setEnabled(isEnabled);
/* 318 */         ImportPanel.this.m_tableValueEditBtn.setEnabled(isEnabled);
/* 319 */         ImportPanel.this.m_loadTableList = false;
/* 320 */         ImportPanel.this.m_helper.loadComponentValues();
/* 321 */         ImportPanel.this.m_loadTableList = true;
/*     */       }
/*     */     };
/* 325 */     this.m_tableList.m_treeSelectionModel.addTreeSelectionListener(listener);
/*     */ 
/* 328 */     ActionListener aListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/* 332 */         String action = event.getActionCommand();
/*     */ 
/* 334 */         Properties props = new Properties();
/*     */ 
/* 336 */         boolean isChanged = false;
/* 337 */         boolean isDelete = false;
/* 338 */         String table = null;
/* 339 */         String editItems = "";
/* 340 */         String cat = ImportPanel.this.m_tableList.getSelectedItemName(true);
/* 341 */         table = ImportPanel.this.m_tableList.getSelectedItemName();
/* 342 */         if (cat.equals("Archiver"))
/*     */         {
/* 344 */           table = "";
/*     */         }
/*     */ 
/* 347 */         if (action.equals("EditFromFile"))
/*     */         {
/* 349 */           props.put("EditFromFile", "1");
/* 350 */           ImportPanel.this.retrieveTableProperties(props);
/* 351 */           EditTableDlg dlg = new EditTableDlg(ImportPanel.this.m_systemInterface, LocaleResources.getString("apArchiverTableViewBatchOptions", ImportPanel.this.m_cxt, table), ImportPanel.this.m_collectionContext, ImportPanel.this.m_cxt);
/*     */ 
/* 354 */           dlg.init(table, props, false, false);
/* 355 */           dlg.prompt();
/*     */         }
/*     */         else
/*     */         {
/* 359 */           props = (Properties)ImportPanel.this.m_helper.m_props.clone();
/* 360 */           ImportPanel.this.m_collectionContext.loadContext(props);
/* 361 */           boolean isAdd = action.equals("Add");
/* 362 */           String title = LocaleResources.getString("apArchiverEditImportOptions", ImportPanel.this.m_cxt, props.getProperty("aArchiveName"));
/* 363 */           if ((table != null) && (table.length() != 0))
/*     */           {
/* 365 */             if (isAdd)
/*     */             {
/* 367 */               title = LocaleResources.getString("apArchiverAddTableImportOptions", ImportPanel.this.m_cxt, table);
/*     */             }
/*     */             else
/*     */             {
/* 371 */               title = LocaleResources.getString("apArchiverEditTableImportOptions", ImportPanel.this.m_cxt, table);
/*     */             }
/*     */           }
/* 374 */           else if (isAdd)
/*     */           {
/* 376 */             title = LocaleResources.getString("apArchiverAddImportOptions", ImportPanel.this.m_cxt, props.getProperty("aArchiveName"));
/*     */           }
/* 378 */           EditTableImportOptionsDlg dlg = new EditTableImportOptionsDlg(ImportPanel.this.m_systemInterface, title, ImportPanel.this.m_collectionContext, ImportPanel.this.m_cxt);
/*     */ 
/* 380 */           dlg.init(table, props, isAdd);
/* 381 */           if (dlg.prompt() == 1)
/*     */           {
/* 383 */             String query = dlg.createQueryString();
/* 384 */             ImportPanel.this.m_helper.m_props.put("aImportTable" + table, query);
/* 385 */             isChanged = true;
/* 386 */             String deleteStr = props.getProperty("isDelete");
/* 387 */             isDelete = StringUtils.convertToBool(deleteStr, false);
/* 388 */             editItems = "aImportTable" + table;
/*     */           }
/*     */         }
/*     */ 
/* 392 */         if (!isChanged)
/*     */           return;
/* 394 */         ImportPanel.this.storeTableChange(editItems, isDelete, false);
/*     */       }
/*     */     };
/* 399 */     PanePanel buttonPanel = new PanePanel();
/* 400 */     buttonPanel.setLayout(new BorderLayout());
/*     */ 
/* 403 */     this.m_tableAddOptionsBtn = new JButton(LocaleResources.getString("apDlgButtonAdd", this.m_cxt));
/* 404 */     this.m_tableAddOptionsBtn.setActionCommand("Add");
/* 405 */     this.m_tableAddOptionsBtn.addActionListener(aListener);
/* 406 */     this.m_tableEditBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 407 */     this.m_tableEditBtn.setActionCommand("Edit");
/* 408 */     this.m_tableEditFromFileBtn = new JButton(LocaleResources.getString("apDlgButtonViewFromFile", this.m_cxt));
/* 409 */     this.m_tableEditFromFileBtn.setActionCommand("EditFromFile");
/* 410 */     this.m_tableEditBtn.addActionListener(aListener);
/* 411 */     this.m_tableEditFromFileBtn.addActionListener(aListener);
/* 412 */     this.m_tableEditBtn.setEnabled(false);
/* 413 */     this.m_tableEditFromFileBtn.setEnabled(false);
/*     */ 
/* 415 */     buttonPanel.add("West", this.m_tableAddOptionsBtn);
/* 416 */     buttonPanel.add("Center", this.m_tableEditBtn);
/* 417 */     buttonPanel.add("East", this.m_tableEditFromFileBtn);
/*     */ 
/* 419 */     tablePanel.setLayout(new BorderLayout());
/* 420 */     this.m_helper.addExchangeComponent(tablePanel, this.m_tableList, "tableList");
/* 421 */     tablePanel.add("South", buttonPanel);
/* 422 */     return tablePanel;
/*     */   }
/*     */ 
/*     */   protected void storeTableChange(String editItems, boolean isDelete, boolean isIgnoreException)
/*     */   {
/* 427 */     this.m_helper.m_props.put("EditItems", editItems);
/* 428 */     this.m_helper.m_props.put("isDelete", "" + isDelete);
/*     */     try
/*     */     {
/* 431 */       SharedContext shContext = this.m_collectionContext.getSharedContext();
/* 432 */       AppContextUtils.executeService(shContext, "EDIT_ARCHIVEDATA", this.m_helper.m_props, true);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 436 */       if (!isIgnoreException)
/*     */       {
/* 438 */         MessageBox.reportError(this.m_systemInterface, e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 443 */     this.m_helper.m_props.put("isDelete", "false");
/*     */   }
/*     */ 
/*     */   protected void retrieveTableProperties(Properties props)
/*     */   {
/* 448 */     ValueNode item = this.m_tableList.getSelectedItem();
/* 449 */     ValueNode parent = item.getParent();
/* 450 */     String table = item.m_value;
/* 451 */     String filePath = parent.m_value;
/* 452 */     filePath = filePath + '/' + table;
/* 453 */     filePath = filePath + "_arTable~1.hda";
/*     */ 
/* 455 */     Properties fileProps = this.m_collectionContext.getBatchProperties(filePath);
/*     */ 
/* 457 */     String[][] keys = ImportTableList.KEYS;
/* 458 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/* 460 */       String value = fileProps.getProperty(keys[i][1]);
/* 461 */       if (value == null)
/*     */         continue;
/* 463 */       props.put(keys[i][0], value);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String convertToString(Vector tables)
/*     */   {
/* 470 */     StringBuffer buf = new StringBuffer();
/*     */ 
/* 472 */     if (tables == null)
/*     */     {
/* 474 */       return null;
/*     */     }
/*     */ 
/* 477 */     int size = tables.size();
/* 478 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 480 */       if (buf.length() > 0)
/*     */       {
/* 482 */         buf.append(',');
/*     */       }
/* 484 */       buf.append(tables.elementAt(i));
/*     */     }
/* 486 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public void enableDisable(boolean isEnabled)
/*     */   {
/* 492 */     CollectionData curCollection = this.m_collectionContext.getCurrentCollection();
/* 493 */     if ((curCollection != null) && (curCollection.isProxied()))
/*     */     {
/* 495 */       isEnabled = false;
/*     */     }
/* 497 */     this.m_fieldEditBtn.setEnabled(isEnabled);
/* 498 */     this.m_valueEditBtn.setEnabled(isEnabled);
/*     */ 
/* 500 */     String cat = this.m_tableList.getSelectedItemName(true);
/* 501 */     String name = this.m_tableList.getSelectedItemName();
/* 502 */     boolean isTableEnabled = (isEnabled) && (cat.equals("Table")) && (this.m_tableList.m_treeSelectionModel.getSelectionPath() != null);
/*     */ 
/* 504 */     this.m_tableEditFromFileBtn.setEnabled(isTableEnabled);
/* 505 */     this.m_tableFieldEditBtn.setEnabled(isTableEnabled);
/* 506 */     this.m_tableValueEditBtn.setEnabled(isTableEnabled);
/*     */ 
/* 508 */     boolean disableBoth = false;
/* 509 */     if ((name == null) || (name.length() == 0))
/*     */     {
/* 511 */       disableBoth = true;
/*     */     }
/* 513 */     boolean isTableEditAllowed = false;
/* 514 */     if (this.m_helper.m_props != null)
/*     */     {
/* 516 */       isTableEditAllowed = this.m_helper.m_props.containsKey("aImportTable" + name);
/*     */     }
/* 518 */     this.m_tableEditBtn.setEnabled((isTableEditAllowed) && (!disableBoth));
/* 519 */     this.m_tableAddOptionsBtn.setEnabled((!isTableEditAllowed) && (!disableBoth));
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 527 */     String cmd = e.getActionCommand();
/*     */ 
/* 529 */     Properties props = (Properties)this.m_helper.m_props.clone();
/* 530 */     boolean isImportTable = false;
/* 531 */     if (cmd.indexOf("Table") != -1)
/*     */     {
/* 533 */       isImportTable = true;
/*     */     }
/* 535 */     ExportQueryData queryData = new ExportQueryData();
/* 536 */     if (isImportTable)
/*     */     {
/* 538 */       String table = this.m_tableList.getSelectedItemName();
/*     */ 
/* 540 */       props.put("editingTable", table);
/* 541 */       props.put("isTableArchive", "1");
/* 542 */       String tableOptions = props.getProperty("aImportTable" + table);
/* 543 */       queryData.parse(tableOptions);
/* 544 */       props.put("aTableName", table);
/* 545 */       String batchFile = queryData.getQueryProp("aBatchFile");
/* 546 */       if (batchFile != null)
/*     */       {
/* 548 */         props.put("aBatchFile" + table, batchFile);
/*     */       }
/*     */     }
/*     */ 
/* 552 */     if (cmd.startsWith("editFields"))
/*     */     {
/* 554 */       EditFieldMapDlg dlg = new EditFieldMapDlg(this.m_systemInterface, LocaleResources.getString("apLabelEditFieldMaps", this.m_cxt), this.m_collectionContext);
/*     */ 
/* 557 */       if (dlg.init(props, this.m_fieldMaps.getClauseDisplay()) == 1)
/*     */       {
/* 559 */         this.m_helper.m_props = props;
/* 560 */         String str = props.getProperty("aFieldMaps");
/* 561 */         this.m_fieldMaps.parse(str);
/* 562 */         this.m_helper.loadComponentValues();
/*     */       }
/*     */     } else {
/* 565 */       if (!cmd.startsWith("editValues"))
/*     */         return;
/* 567 */       String parentTables = queryData.getQueryProp("aParentTables");
/* 568 */       if (parentTables != null)
/*     */       {
/* 570 */         String table = props.getProperty("editingTable");
/* 571 */         table = table + "," + parentTables;
/* 572 */         props.put("editingTable", table);
/*     */       }
/* 574 */       EditValueMapDlg dlg = new EditValueMapDlg(this.m_systemInterface, LocaleResources.getString("apLabelEditValueMaps", this.m_cxt), this.m_collectionContext);
/*     */ 
/* 577 */       if (dlg.init(props, this.m_valueMaps.getClauseDisplay()) != 1)
/*     */         return;
/* 579 */       this.m_helper.m_props = props;
/* 580 */       String str = props.getProperty("aValueMaps");
/* 581 */       this.m_valueMaps.parse(str);
/* 582 */       this.m_helper.loadComponentValues();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*     */     try
/*     */     {
/* 596 */       String value = this.m_helper.m_props.getProperty(name);
/* 597 */       if (name.equals("aFieldMaps"))
/*     */       {
/* 599 */         if (updateComponent)
/*     */         {
/* 601 */           this.m_fieldMaps.parse(value);
/* 602 */           exchange.m_compValue = this.m_fieldMaps.createQueryString();
/*     */         }
/*     */         else
/*     */         {
/* 606 */           this.m_fieldMaps.parse(exchange.m_compValue);
/*     */         }
/*     */       }
/* 609 */       else if (name.equals("aValueMaps"))
/*     */       {
/* 611 */         if (updateComponent)
/*     */         {
/* 613 */           this.m_valueMaps.parse(value);
/* 614 */           exchange.m_compValue = this.m_valueMaps.createQueryString();
/*     */         }
/*     */         else
/*     */         {
/* 618 */           this.m_valueMaps.parse(exchange.m_compValue);
/*     */         }
/*     */       }
/* 621 */       else if (name.equals("aValueMapsTable"))
/*     */       {
/* 623 */         if (updateComponent)
/*     */         {
/* 625 */           String tableValueMap = this.m_helper.m_props.getProperty("aValueMapsTable" + this.m_tableList.getSelectedItemName());
/* 626 */           this.m_valueMaps.parse(tableValueMap);
/* 627 */           exchange.m_compValue = this.m_valueMaps.createQueryString();
/*     */         }
/*     */         else
/*     */         {
/* 632 */           this.m_valueMaps.parse(exchange.m_compValue);
/*     */         }
/*     */       }
/* 635 */       else if (name.equals("aFieldMapsTable"))
/*     */       {
/* 637 */         if (updateComponent)
/*     */         {
/* 639 */           String tableFieldMap = this.m_helper.m_props.getProperty("aFieldMapsTable" + this.m_tableList.getSelectedItemName());
/*     */ 
/* 641 */           this.m_fieldMaps.parse(tableFieldMap);
/* 642 */           exchange.m_compValue = this.m_fieldMaps.createQueryString();
/*     */         }
/*     */         else
/*     */         {
/* 646 */           this.m_fieldMaps.parse(value);
/*     */         }
/*     */       }
/* 649 */       else if ((name.equals("tableList")) && (this.m_loadTableList))
/*     */       {
/* 651 */         ImportTableList list = this.m_tableList;
/* 652 */         list.refreshList(this.m_helper.m_props);
/*     */ 
/* 655 */         exchange.m_component = null;
/*     */       }
/*     */       else
/*     */       {
/* 659 */         this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */       }
/*     */     }
/*     */     catch (ServiceException ignore)
/*     */     {
/* 664 */       Report.trace("applet", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 670 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83686 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ImportPanel
 * JD-Core Version:    0.5.4
 */