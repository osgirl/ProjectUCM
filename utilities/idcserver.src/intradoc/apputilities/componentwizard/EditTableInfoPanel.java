/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.awt.event.TextEvent;
/*     */ import java.awt.event.TextListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditTableInfoPanel extends EditBasePanel
/*     */ {
/*  71 */   protected JTextField m_colNameField = null;
/*  72 */   protected JTextField m_tableNameField = null;
/*  73 */   protected ComboChoice m_mergeTableComboChoice = null;
/*  74 */   protected DisplayChoice m_mergeTableChoice = null;
/*  75 */   protected JCheckBox m_mergeToBox = null;
/*     */ 
/*  77 */   protected CustomLabel m_typeDescLabel = null;
/*  78 */   protected CustomLabel m_typeLabel = null;
/*     */ 
/*     */   public void initUI(int editType)
/*     */   {
/*  89 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/*  90 */     this.m_helper.addLastComponentInRow(this, addInfoPanel());
/*     */ 
/*  92 */     gbh.m_gc.weighty = 1.0D;
/*  93 */     gbh.addEmptyRow(this);
/*  94 */     gbh.m_gc.weighty = 0.0D;
/*     */ 
/*  96 */     this.m_helper.addLastComponentInRow(this, addTableInfoPanel());
/*     */ 
/*  98 */     gbh.m_gc.weighty = 1.0D;
/*  99 */     gbh.addEmptyRow(this);
/*     */   }
/*     */ 
/*     */   protected JPanel addInfoPanel()
/*     */   {
/* 104 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/* 105 */     JPanel panel = new PanePanel();
/* 106 */     this.m_helper.makePanelGridBag(panel, 1);
/*     */ 
/* 108 */     this.m_typeLabel = new CustomLabel();
/* 109 */     this.m_typeDescLabel = new CustomLabel("", 1);
/*     */ 
/* 111 */     gbh.prepareAddLastRowElement();
/* 112 */     this.m_helper.addComponent(panel, this.m_typeDescLabel);
/* 113 */     this.m_helper.addLabelFieldPair(panel, LocaleResources.getString("csCompWizLabelResourceType", null), this.m_typeLabel, "typeLabel");
/*     */ 
/* 115 */     this.m_helper.addLabelDisplayPair(panel, LocaleResources.getString("csCompWizLabelFileName2", null), 30, "filename");
/*     */ 
/* 117 */     this.m_helper.addLabelDisplayPair(panel, LocaleResources.getString("csCompWizLabelLoadOrder2", null), 10, "loadOrder");
/*     */ 
/* 120 */     return panel;
/*     */   }
/*     */ 
/*     */   protected JPanel addTableInfoPanel()
/*     */   {
/* 125 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/* 126 */     JPanel panel = new PanePanel();
/* 127 */     this.m_helper.makePanelGridBag(panel, 1);
/* 128 */     gbh.m_gc.weightx = 1.0D;
/* 129 */     gbh.m_gc.weighty = 1.0D;
/*     */ 
/* 131 */     this.m_helper.addLastComponentInRow(panel, new CustomLabel(LocaleResources.getString("csCompWizLabelTableDef", null), 2));
/*     */ 
/* 134 */     gbh.addEmptyRow(panel);
/*     */ 
/* 136 */     String tableMsg = "!csCompWizTableNamePrompt";
/* 137 */     addDescrpAndComponent(panel, "!csCompWizLabelTableName", tableMsg, this.m_tableNameField = new CustomTextField(60), "tablename", true);
/*     */ 
/* 140 */     if ((this.m_extraInfo.equals("template")) || (this.m_extraInfo.equals("resource")))
/*     */     {
/* 142 */       addMergeTableInfo(panel);
/*     */     }
/*     */ 
/* 145 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void addMergeTableInfo(JPanel panel)
/*     */   {
/* 150 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/*     */ 
/* 152 */     String msg = LocaleResources.getString("csCompWizMergeRuleMsg", null);
/* 153 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 154 */     this.m_helper.addComponent(panel, new CustomText(msg, 100));
/*     */ 
/* 156 */     if (this.m_extraInfo.equals("resource"))
/*     */     {
/* 158 */       this.m_mergeToBox = new CustomCheckbox(LocaleResources.getString("csCompWizLabelCheckboxMergeTo", null));
/* 159 */       ItemListener mergeListener = new ItemListener()
/*     */       {
/*     */         public void itemStateChanged(ItemEvent e)
/*     */         {
/* 163 */           boolean enableMerge = EditTableInfoPanel.this.m_mergeToBox.isSelected();
/* 164 */           boolean flag = false;
/*     */ 
/* 166 */           if (enableMerge)
/*     */           {
/* 168 */             flag = true;
/*     */           }
/* 170 */           EditTableInfoPanel.this.m_mergeTableComboChoice.setEnabled(flag);
/*     */         }
/*     */       };
/* 173 */       this.m_mergeToBox.addItemListener(mergeListener);
/*     */ 
/* 175 */       gbh.prepareAddRowElement();
/* 176 */       this.m_helper.addExchangeComponent(panel, this.m_mergeToBox, "mergeTo");
/*     */ 
/* 178 */       gbh.prepareAddLastRowElement(17);
/* 179 */       this.m_mergeTableComboChoice = new ComboChoice(LocaleResources.getString("csCompWizLabelNone", null));
/* 180 */       this.m_mergeTableComboChoice.setEnabled(false);
/* 181 */       this.m_helper.addExchangeComponent(panel, this.m_mergeTableComboChoice, "mergeTable");
/*     */ 
/* 183 */       TextListener textListener = new TextListener()
/*     */       {
/*     */         public void textValueChanged(TextEvent e)
/*     */         {
/* 187 */           String text = EditTableInfoPanel.this.m_mergeTableComboChoice.getText();
/* 188 */           String newTableName = EditTableInfoPanel.this.m_component.m_name + "_" + text;
/* 189 */           EditTableInfoPanel.this.m_tableNameField.setText(newTableName);
/*     */         }
/*     */       };
/* 192 */       this.m_mergeTableComboChoice.addTextListener(textListener);
/*     */ 
/* 194 */       ActionListener actionListener = new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/* 198 */           String text = EditTableInfoPanel.this.m_mergeTableComboChoice.getText();
/* 199 */           String newTableName = EditTableInfoPanel.this.m_component.m_name + "_" + text;
/* 200 */           EditTableInfoPanel.this.m_tableNameField.setText(newTableName);
/*     */         }
/*     */       };
/* 203 */       this.m_mergeTableComboChoice.addActionListener(actionListener);
/*     */     }
/*     */     else
/*     */     {
/* 207 */       this.m_mergeTableChoice = new DisplayChoice(LocaleResources.getString("csCompWizLabelNone", null));
/* 208 */       this.m_helper.addLabelFieldPair(panel, LocaleResources.getString("csCompWizLabelMergeTable", null), this.m_mergeTableChoice, "mergeTable");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initMergeTableChoice(String type) throws ServiceException, DataException
/*     */   {
/* 214 */     String mergetable = this.m_component.getMergeTableListName(type);
/* 215 */     Vector v = CWizardUtils.getChoiceList(mergetable, "tablename", false);
/*     */ 
/* 217 */     IdcComparator cmp = new IdcComparator()
/*     */     {
/*     */       public int compare(Object obj1, Object obj2)
/*     */       {
/* 221 */         String key1 = (String)obj1;
/* 222 */         String key2 = (String)obj2;
/*     */ 
/* 224 */         return key1.compareTo(key2);
/*     */       }
/*     */     };
/* 227 */     Sort.sortVector(v, cmp);
/*     */ 
/* 229 */     if (type.equals("template"))
/*     */     {
/* 231 */       this.m_mergeTableChoice.init(v);
/*     */     }
/*     */     else
/*     */     {
/* 235 */       this.m_mergeTableComboChoice.initChoiceList(v);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String promptForColumnInfo(String mergetable, String tablename)
/*     */   {
/* 241 */     this.m_dlgHelper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("csCompWizLabelColumnInfo", null), true);
/*     */ 
/* 243 */     this.m_dlgHelper.m_helpPage = DialogHelpTable.getHelpPage("CW_PromptTableColumns");
/* 244 */     JPanel mainPanel = this.m_dlgHelper.m_mainPanel;
/* 245 */     this.m_dlgHelper.makePanelGridBag(mainPanel, 2);
/* 246 */     this.m_colNameField = new CustomTextField(30);
/*     */ 
/* 248 */     this.m_dlgHelper.addOK(null);
/* 249 */     this.m_dlgHelper.addCancel(null);
/* 250 */     this.m_dlgHelper.addHelp(null);
/*     */ 
/* 252 */     String msg = "!csCompWizColumnOrderMsg";
/* 253 */     if ((mergetable != null) && (mergetable.length() > 0))
/*     */     {
/* 255 */       msg = LocaleUtils.encodeMessage("csCompWizMergeTableNotFound", msg, mergetable);
/*     */     }
/*     */ 
/* 259 */     this.m_dlgHelper.addLastComponentInRow(mainPanel, new CustomLabel(LocaleResources.localizeMessage(msg, null)));
/* 260 */     JPanel fieldPanel = new PanePanel();
/* 261 */     fieldPanel.setLayout(new BorderLayout());
/* 262 */     fieldPanel.add("North", new CustomLabel(LocaleResources.getString("csCompWizLabelColumnName2", null), 1));
/*     */ 
/* 264 */     fieldPanel.add("Center", this.m_colNameField);
/* 265 */     this.m_dlgHelper.m_gridHelper.prepareAddRowElement(18);
/* 266 */     this.m_dlgHelper.addComponent(mainPanel, fieldPanel);
/*     */ 
/* 268 */     this.m_list = createUdlPanel("!csCompWizColumnsTitle", 200, 5, "ColumnsData", false, (String[][])null, null, false);
/* 269 */     this.m_list.setVisibleColumns("column");
/* 270 */     this.m_list.setIDColumn("column");
/*     */ 
/* 272 */     this.m_dlgHelper.m_gridHelper.prepareAddRowElement(18);
/* 273 */     this.m_dlgHelper.addComponent(mainPanel, addUdlPanelCommandButtons());
/*     */ 
/* 275 */     this.m_dlgHelper.m_gridHelper.prepareAddLastRowElement();
/* 276 */     this.m_dlgHelper.addComponent(mainPanel, this.m_list);
/*     */ 
/* 278 */     String columns = null;
/* 279 */     this.m_listData = new DataResultSet(new String[] { "column" });
/* 280 */     refreshList(null);
/*     */ 
/* 283 */     if (this.m_dlgHelper.prompt() == 1)
/*     */     {
/* 285 */       Vector cols = new IdcVector();
/*     */ 
/* 288 */       for (this.m_listData.first(); this.m_listData.isRowPresent(); this.m_listData.next())
/*     */       {
/* 290 */         String col = ResultSetUtils.getValue(this.m_listData, "column");
/* 291 */         if ((col == null) || (col.length() <= 0))
/*     */           continue;
/* 293 */         cols.addElement(col);
/*     */       }
/*     */ 
/* 296 */       columns = StringUtils.createString(cols, ',', '^');
/*     */     }
/*     */ 
/* 299 */     return columns;
/*     */   }
/*     */ 
/*     */   public JPanel addUdlPanelCommandButtons()
/*     */   {
/* 304 */     ActionListener inlistener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 308 */         String col = EditTableInfoPanel.this.m_colNameField.getText();
/* 309 */         IdcMessage errMsg = EditTableInfoPanel.this.checkField(col, IdcMessageFactory.lc("csCompWizLabelColumnName", new Object[0]), false, false);
/*     */ 
/* 311 */         if (errMsg == null)
/*     */         {
/* 313 */           int index = col.indexOf(',');
/* 314 */           if (index > 0)
/*     */           {
/* 316 */             errMsg = IdcMessageFactory.lc("csCompWizColumnNameInvalid", new Object[0]);
/*     */           }
/* 320 */           else if (EditTableInfoPanel.this.m_listData.findRow(0, col) != null)
/*     */           {
/* 322 */             errMsg = IdcMessageFactory.lc("csCompWizColumnNameExists", new Object[] { col });
/*     */           }
/*     */         }
/*     */ 
/* 326 */         if (errMsg != null)
/*     */         {
/* 328 */           CWizardGuiUtils.reportError(EditTableInfoPanel.this.m_systemInterface, null, errMsg);
/* 329 */           return;
/*     */         }
/*     */ 
/* 335 */         Vector v = EditTableInfoPanel.this.m_listData.createEmptyRow();
/* 336 */         v.setElementAt(col, 0);
/* 337 */         EditTableInfoPanel.this.m_listData.addRow(v);
/* 338 */         EditTableInfoPanel.this.refreshList(col);
/* 339 */         EditTableInfoPanel.this.m_colNameField.setText("");
/*     */       }
/*     */     };
/* 342 */     JButton insertBtn = new JButton(LocaleResources.getString("csCompWizCommandInsert", null));
/* 343 */     insertBtn.addActionListener(inlistener);
/*     */ 
/* 345 */     ActionListener dellistener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 349 */         EditTableInfoPanel.this.delete();
/*     */       }
/*     */     };
/* 352 */     JButton delBtn = this.m_list.addButton(LocaleResources.getString("csCompWizCommandDelete", null), true);
/* 353 */     delBtn.addActionListener(dellistener);
/*     */ 
/* 355 */     JPanel btnPanel = new PanePanel();
/* 356 */     this.m_helper.makePanelGridBag(btnPanel, 1);
/* 357 */     this.m_helper.addLastComponentInRow(btnPanel, insertBtn);
/* 358 */     this.m_helper.addLastComponentInRow(btnPanel, delBtn);
/*     */ 
/* 360 */     return btnPanel;
/*     */   }
/*     */ 
/*     */   public void loadData()
/*     */   {
/* 367 */     String type = this.m_helper.m_props.getProperty("type");
/* 368 */     String tablename = this.m_helper.m_props.getProperty("tablename");
/* 369 */     if ((tablename == null) || (tablename.length() == 0))
/*     */     {
/* 371 */       tablename = this.m_component.m_name + "_";
/*     */ 
/* 373 */       if ((type.equals("service")) || (type.equals("query")) || (type.equals("template")))
/*     */       {
/* 375 */         tablename = this.m_component.retrieveDefaultTableName(type, false);
/*     */       }
/* 377 */       this.m_helper.m_props.put("tablename", tablename);
/*     */     }
/*     */ 
/* 380 */     super.loadData();
/* 381 */     String typeLabel = StringUtils.findString(IntradocComponent.RES_DEF, type, 0, 1);
/* 382 */     this.m_typeLabel.setText(LocaleResources.localizeMessage(typeLabel, null));
/* 383 */     String descType = null;
/*     */ 
/* 385 */     if (type.equals("dynResTable"))
/*     */     {
/* 387 */       descType = LocaleResources.getString("csCompWizDynResourceTableMsg", null);
/*     */     }
/* 389 */     else if (type.equals("staticResTable"))
/*     */     {
/* 391 */       descType = LocaleResources.getString("csCompWizStaticResourceTableMsg", null);
/*     */     }
/*     */     else
/*     */     {
/* 395 */       descType = type;
/*     */     }
/* 397 */     this.m_typeDescLabel.setText(LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csCompWizNewTable", null, descType), null));
/*     */ 
/* 401 */     if ((!this.m_extraInfo.equals("resource")) && (!this.m_extraInfo.equals("template")))
/*     */       return;
/*     */     try
/*     */     {
/* 405 */       initMergeTableChoice(type);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 409 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 411 */       Report.debug("applet", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateEntries()
/*     */   {
/* 420 */     boolean validate = StringUtils.convertToBool(this.m_helper.m_props.getProperty("validateEntries"), true);
/*     */ 
/* 422 */     if ((!super.validateEntries()) || (!validate))
/*     */     {
/* 424 */       return false;
/*     */     }
/*     */ 
/* 427 */     boolean retVal = true;
/*     */     try
/*     */     {
/* 430 */       String cwtype = this.m_helper.m_props.getProperty("type");
/* 431 */       String tablename = this.m_helper.m_props.getProperty("tablename");
/* 432 */       String mergeTable = this.m_helper.m_props.getProperty("mergeTable");
/* 433 */       String mergeTableColumns = null;
/* 434 */       String mergeColumn = null;
/*     */ 
/* 437 */       if (!this.m_component.isNameUnique(tablename, true))
/*     */       {
/* 439 */         throw new ServiceException(LocaleUtils.encodeMessage("csCompWizTableDefInComponent", null, tablename, this.m_component.m_name));
/*     */       }
/*     */ 
/* 444 */       boolean prompt = false;
/* 445 */       Vector v = this.m_component.retreiveMergeTableInfo(cwtype, mergeTable);
/*     */ 
/* 447 */       if ((v == null) || (v.size() == 0))
/*     */       {
/* 449 */         prompt = true;
/*     */       }
/*     */       else
/*     */       {
/* 453 */         mergeTableColumns = (String)v.elementAt(1);
/* 454 */         mergeColumn = (String)v.elementAt(2);
/* 455 */         this.m_helper.m_props.put("mergeTableColumns", mergeTableColumns);
/* 456 */         this.m_helper.m_props.put("mergeColumn", mergeColumn);
/*     */       }
/*     */ 
/* 459 */       if (mergeTable == null)
/*     */       {
/* 461 */         mergeTable = "";
/*     */       }
/*     */ 
/* 464 */       if (prompt)
/*     */       {
/* 466 */         mergeTableColumns = promptForColumnInfo(mergeTable, tablename);
/*     */ 
/* 468 */         if (mergeTableColumns == null)
/*     */         {
/* 470 */           return false;
/*     */         }
/* 472 */         this.m_helper.m_props.put("mergeTableColumns", mergeTableColumns);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 477 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/* 478 */       retVal = false;
/*     */     }
/*     */ 
/* 481 */     return retVal;
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 489 */     String name = exchange.m_compName;
/* 490 */     String val = exchange.m_compValue;
/* 491 */     String type = this.m_helper.m_props.getProperty("type");
/* 492 */     IdcMessage errMsg = null;
/*     */ 
/* 494 */     if (name.equals("mergeTable"))
/*     */     {
/* 496 */       boolean isEnabled = true;
/*     */ 
/* 498 */       if ((type.equals("dynResTable")) || (type.equals("staticResTable")))
/*     */       {
/* 500 */         if (!this.m_mergeToBox.isSelected())
/*     */         {
/* 502 */           isEnabled = false;
/*     */         }
/*     */ 
/* 505 */         if (isEnabled)
/*     */         {
/* 507 */           errMsg = checkField(val, IdcMessageFactory.lc("csCompWizMergeTableNameMsg", new Object[0]), false, false);
/*     */         }
/*     */       }
/*     */     }
/* 511 */     else if (name.equals("tablename"))
/*     */     {
/* 513 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizMergeTableNameMsg", new Object[0]), false, false);
/*     */     }
/*     */ 
/* 516 */     if (errMsg != null)
/*     */     {
/* 518 */       exchange.m_errorMessage = errMsg;
/* 519 */       return false;
/*     */     }
/* 521 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 526 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.EditTableInfoPanel
 * JD-Core Version:    0.5.4
 */