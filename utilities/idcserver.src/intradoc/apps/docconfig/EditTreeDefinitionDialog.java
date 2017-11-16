/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.gui.SchemaView;
/*     */ import intradoc.shared.schema.SchemaData;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.shared.schema.SchemaRelationData;
/*     */ import intradoc.shared.schema.SchemaTableConfig;
/*     */ import intradoc.shared.schema.SchemaTableData;
/*     */ import intradoc.shared.schema.SchemaTreePointer;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.Window;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditTreeDefinitionDialog extends DialogCallback
/*     */   implements ActionListener, ItemListener
/*     */ {
/*  72 */   protected SystemInterface m_systemInterface = null;
/*     */   protected SharedContext m_context;
/*  74 */   protected DialogHelper m_helper = null;
/*  75 */   protected String m_helpPage = null;
/*     */ 
/*  77 */   protected Properties m_savedProps = null;
/*  78 */   protected boolean m_isNew = false;
/*  79 */   protected DataBinder m_binder = null;
/*  80 */   protected DataBinder m_cachedBinder = null;
/*  81 */   protected boolean m_isFatalError = false;
/*  82 */   protected SchemaHelper m_schHelper = null;
/*     */ 
/*  84 */   protected Object[] m_treeDefinition = null;
/*     */   protected CustomPanel m_treeDefinitionPanel;
/*     */   protected CustomLabel m_currentLevelLabel;
/*     */   protected CustomChoice m_selectionList;
/*     */   protected Vector m_selectionListObjects;
/*     */   protected JButton m_removeLevelButton;
/*     */   protected JButton m_selectParentButton;
/*  94 */   protected Dimension m_lastSize = new Dimension(0, 0);
/*     */ 
/*     */   public EditTreeDefinitionDialog(SystemInterface sys, SchemaHelper schHelper, String title, String helpPage)
/*     */   {
/*  99 */     this.m_systemInterface = sys;
/* 100 */     this.m_helper = new DialogHelper(sys, title, true);
/* 101 */     this.m_schHelper = schHelper;
/* 102 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public void init(Properties props, SharedContext sharedContext, boolean isNew)
/*     */   {
/* 107 */     this.m_context = sharedContext;
/* 108 */     this.m_binder = new DataBinder();
/* 109 */     this.m_binder.setLocalData(props);
/* 110 */     this.m_schHelper = new SchemaHelper();
/* 111 */     this.m_schHelper.computeMaps();
/*     */ 
/* 114 */     this.m_savedProps = ((Properties)props.clone());
/* 115 */     this.m_isNew = isNew;
/*     */ 
/* 117 */     initUI();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/* 122 */     SystemInterface si = this.m_systemInterface;
/* 123 */     JPanel mainPanel = this.m_helper.initStandard(null, this, 2, true, this.m_helpPage);
/*     */ 
/* 125 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 127 */     this.m_helper.attachToContainer(mainPanel, this.m_systemInterface, this.m_binder.getLocalData());
/* 128 */     this.m_helper.m_mainPanel = mainPanel;
/*     */ 
/* 130 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 131 */     this.m_helper.addComponent(mainPanel, new CustomLabel(si.localizeCaption("apSchemaTreeDefinitionLabel"), 1));
/*     */ 
/* 135 */     this.m_treeDefinitionPanel = new CustomPanel();
/* 136 */     this.m_helper.makePanelGridBag(this.m_treeDefinitionPanel, 1);
/*     */ 
/* 138 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 139 */     this.m_helper.addComponent(mainPanel, this.m_treeDefinitionPanel);
/*     */ 
/* 141 */     this.m_currentLevelLabel = new CustomLabel("", 1);
/*     */ 
/* 143 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 144 */     this.m_helper.addComponent(mainPanel, this.m_currentLevelLabel);
/*     */ 
/* 146 */     this.m_selectionList = new CustomChoice();
/* 147 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 148 */     this.m_helper.addComponent(mainPanel, this.m_selectionList);
/* 149 */     this.m_selectionList.addItemListener(this);
/*     */ 
/* 151 */     IdcMessage msg = IdcMessageFactory.lc("apSchemaTreeRemoveLevel_view", new Object[] { null, "1" });
/*     */ 
/* 153 */     this.m_removeLevelButton = new JButton(si.localizeMessage(msg));
/* 154 */     this.m_removeLevelButton.addActionListener(this);
/* 155 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 156 */     this.m_helper.addComponent(mainPanel, this.m_removeLevelButton);
/*     */ 
/* 159 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 160 */     this.m_helper.addComponent(mainPanel, new PanePanel());
/*     */ 
/* 163 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 164 */     JPanel panel = createStoragePanel();
/* 165 */     this.m_helper.addComponent(mainPanel, panel);
/*     */   }
/*     */ 
/*     */   protected JPanel createStoragePanel()
/*     */   {
/* 170 */     JPanel panel = new CustomPanel();
/* 171 */     this.m_helper.makePanelGridBag(panel, 2);
/*     */ 
/* 174 */     CustomLabel storageOptionLabel = new CustomLabel(this.m_systemInterface.getString("apTreeStorageType"), 1);
/*     */ 
/* 177 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 178 */     this.m_helper.addComponent(panel, storageOptionLabel);
/*     */ 
/* 180 */     Insets insets = this.m_helper.m_gridHelper.m_gc.insets;
/* 181 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(insets.top, insets.left + 10, insets.bottom, insets.right);
/*     */ 
/* 184 */     JCheckBox showPathBox = new CustomCheckbox(this.m_systemInterface.getString("apSchemaShowTreePath"));
/*     */ 
/* 186 */     this.m_helper.addExchangeComponent(panel, showPathBox, "ShowSelectionPath");
/*     */ 
/* 188 */     JCheckBox storePathBox = new CustomCheckbox(this.m_systemInterface.getString("apSchemaStoreTreePath"));
/*     */ 
/* 190 */     this.m_helper.addExchangeComponent(panel, storePathBox, "StoreSelectionPath");
/*     */ 
/* 192 */     this.m_helper.addLabelEditPair(panel, this.m_systemInterface.localizeCaption("apSchemaStorageTreeSeparatorCaption"), 5, "TreeNodeStorageSeparator");
/*     */ 
/* 196 */     this.m_helper.addLabelEditPair(panel, this.m_systemInterface.localizeCaption("apSchemaDisplayTreeSeparatorCaption"), 5, "TreeNodeDisplaySeparator");
/*     */ 
/* 200 */     String separator = this.m_helper.m_props.getProperty("TreeNodeDisplaySeparator");
/* 201 */     if (separator == null)
/*     */     {
/* 203 */       this.m_helper.m_props.put("TreeNodeDisplaySeparator", "/");
/*     */     }
/* 205 */     separator = this.m_helper.m_props.getProperty("TreeNodeStorageSeparator");
/* 206 */     if (separator == null)
/*     */     {
/* 208 */       this.m_helper.m_props.put("TreeNodeStorageSeparator", "/");
/*     */     }
/* 210 */     return panel;
/*     */   }
/*     */ 
/*     */   public void updateTreeDefinitionPanel() throws DataException
/*     */   {
/* 215 */     SystemInterface si = this.m_systemInterface;
/* 216 */     JPanel panel = this.m_treeDefinitionPanel;
/* 217 */     panel.removeAll();
/* 218 */     GridBagConstraints gc = new GridBagConstraints();
/* 219 */     gc.weightx = 0.0D;
/*     */ 
/* 221 */     if (this.m_treeDefinition.length == 0)
/*     */     {
/* 223 */       CustomLabel label = new CustomLabel(si.getString("apDpNoneSpecified"));
/*     */ 
/* 225 */       panel.add(label, gc);
/*     */     }
/*     */ 
/* 240 */     for (int i = 0; i < this.m_treeDefinition.length; ++i)
/*     */     {
/* 242 */       int level = i / 2 + 1;
/* 243 */       IdcMessage msg = IdcMessageFactory.lc("apSchemaTreeLevelViewLabel", new Object[] { new Integer(level) });
/*     */ 
/* 245 */       msg = IdcMessageFactory.lc("syCaptionWrapper", new Object[] { msg });
/*     */ 
/* 247 */       SchemaData data = null;
/* 248 */       if (this.m_treeDefinition[i] instanceof SchemaData)
/*     */       {
/* 250 */         data = (SchemaData)this.m_treeDefinition[i];
/*     */ 
/* 252 */         if (i % 2 == 0)
/*     */         {
/* 254 */           CustomLabel label = new CustomLabel(si.localizeMessage(msg), 1);
/*     */ 
/* 256 */           gc.anchor = 13;
/* 257 */           gc.weightx = 0.0D;
/* 258 */           gc.gridwidth = 1;
/* 259 */           panel.add(label, gc);
/*     */ 
/* 261 */           label = new CustomLabel(data.m_name);
/* 262 */           gc.gridwidth = 0;
/* 263 */           gc.anchor = 17;
/* 264 */           gc.weightx = 1.0D;
/* 265 */           panel.add(label, gc);
/*     */         }
/*     */         else
/*     */         {
/* 269 */           CustomLabel label = new CustomLabel("");
/* 270 */           gc.anchor = 13;
/* 271 */           gc.weightx = 0.0D;
/* 272 */           gc.gridwidth = 1;
/* 273 */           panel.add(label, gc);
/*     */ 
/* 275 */           label = new CustomLabel(data.m_name);
/* 276 */           gc.gridwidth = 0;
/* 277 */           gc.anchor = 17;
/* 278 */           gc.weightx = 1.0D;
/* 279 */           panel.add(label, gc);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 284 */         CustomLabel label = new CustomLabel(si.localizeMessage(msg), 1);
/*     */ 
/* 286 */         gc.anchor = 13;
/* 287 */         gc.weightx = 0.0D;
/* 288 */         gc.gridwidth = 1;
/* 289 */         panel.add(label, gc);
/*     */ 
/* 291 */         SchemaTreePointer stp = (SchemaTreePointer)this.m_treeDefinition[i];
/*     */ 
/* 293 */         int index = stp.m_recursiveIndex;
/* 294 */         int recursiveToLevel = index / 2 + 1;
/* 295 */         data = (SchemaViewData)this.m_treeDefinition[index];
/* 296 */         msg = IdcMessageFactory.lc("apSchemaTreeRecursiveWrapper", new Object[] { data.m_name, new Integer(recursiveToLevel) });
/*     */ 
/* 298 */         label = new CustomLabel(si.localizeMessage(msg));
/* 299 */         gc.anchor = 17;
/* 300 */         gc.weightx = 1.0D;
/* 301 */         panel.add(label, gc);
/* 302 */         gc.gridwidth = 0;
/* 303 */         gc.weightx = 0.0D;
/* 304 */         if (recursiveToLevel == 1)
/*     */         {
/* 306 */           this.m_selectParentButton = new JButton(si.getString("apSchemaTreeSelectRoot"));
/*     */ 
/* 308 */           panel.add(this.m_selectParentButton, gc);
/* 309 */           this.m_selectParentButton.addActionListener(this);
/*     */         }
/*     */         else
/*     */         {
/* 313 */           panel.add(new PanePanel(), gc);
/*     */         }
/*     */       }
/*     */     }
/* 317 */     panel.validate();
/*     */   }
/*     */ 
/*     */   public void updateDisplay(boolean autoSelect)
/*     */     throws DataException
/*     */   {
/* 323 */     SystemInterface si = this.m_systemInterface;
/* 324 */     boolean isTerminated = false;
/*     */ 
/* 326 */     Component comp = this.m_helper.m_mainPanel;
/* 327 */     while ((comp != null) && (!comp instanceof Window))
/*     */     {
/* 329 */       comp = comp.getParent();
/*     */     }
/*     */ 
/* 332 */     this.m_lastSize = comp.getPreferredSize();
/* 333 */     updateTreeDefinitionPanel();
/*     */ 
/* 335 */     int currentLevel = this.m_treeDefinition.length / 2;
/* 336 */     int priorLevel = currentLevel - 1;
/*     */ 
/* 338 */     boolean selectView = false;
/*     */     IdcMessage msg;
/* 339 */     if (this.m_treeDefinition.length % 2 == 0)
/*     */     {
/* 341 */       IdcMessage msg = IdcMessageFactory.lc("apSchemaTreeSelectLabel_view", new Object[] { new Integer(1 + currentLevel) });
/* 342 */       selectView = true;
/*     */     }
/*     */     else
/*     */     {
/* 346 */       msg = IdcMessageFactory.lc("apSchemaTreeSelectLabel_relation", new Object[] { new Integer(1 + currentLevel), new Integer(2 + currentLevel) });
/*     */     }
/*     */ 
/* 350 */     this.m_selectionList.setEnabled(true);
/* 351 */     this.m_selectionList.removeAllItems();
/* 352 */     this.m_selectionListObjects = new IdcVector();
/* 353 */     this.m_selectionList.add(si.localizeMessage(msg));
/* 354 */     this.m_selectionListObjects.addElement("");
/* 355 */     if (selectView)
/*     */     {
/* 357 */       SchemaViewConfig views = this.m_schHelper.m_views;
/* 358 */       if (currentLevel == 0)
/*     */       {
/* 360 */         for (views.first(); ; views.next()) { if (!views.isRowPresent())
/*     */             break label616;
/* 362 */           SchemaData data = views.getData();
/* 363 */           this.m_selectionList.add(data.m_name);
/* 364 */           this.m_selectionListObjects.add(data); }
/*     */ 
/*     */ 
/*     */       }
/*     */ 
/* 369 */       SchemaViewData parentView = (SchemaViewData)this.m_treeDefinition[(priorLevel * 2)];
/*     */ 
/* 371 */       SchemaRelationData relationData = (SchemaRelationData)this.m_treeDefinition[(priorLevel * 2 + 1)];
/*     */ 
/* 374 */       String table1 = relationData.get("schTable1Table");
/* 375 */       String table2 = relationData.get("schTable2Table");
/*     */       SchemaTableData tableDef;
/*     */       SchemaTableData tableDef;
/* 376 */       if (table1.equals(parentView.get("schTableName")))
/*     */       {
/* 378 */         tableDef = (SchemaTableData)this.m_schHelper.m_tables.getData(table2);
/*     */       }
/*     */       else
/*     */       {
/* 383 */         tableDef = (SchemaTableData)this.m_schHelper.m_tables.getData(table1);
/*     */       }
/*     */ 
/* 386 */       Vector v = this.m_schHelper.computeViews(tableDef);
/* 387 */       for (int i = 0; i < v.size(); ++i)
/*     */       {
/* 389 */         SchemaData data = (SchemaData)v.elementAt(i);
/* 390 */         this.m_selectionList.add(data.m_name);
/* 391 */         this.m_selectionListObjects.addElement(data);
/* 392 */         for (int j = 0; j < this.m_treeDefinition.length; j += 2)
/*     */         {
/* 394 */           SchemaViewData tmpView = (SchemaViewData)this.m_treeDefinition[j];
/*     */ 
/* 396 */           if (!tmpView.m_name.equalsIgnoreCase(data.m_name))
/*     */             continue;
/* 398 */           msg = IdcMessageFactory.lc("apSchemaTreeRecursiveWrapper", new Object[] { data.m_name, new Integer((j + 2) / 2) });
/*     */ 
/* 400 */           this.m_selectionList.add(si.localizeMessage(msg));
/* 401 */           SchemaTreePointer stp = new SchemaTreePointer();
/* 402 */           stp.m_recursiveIndex = j;
/* 403 */           this.m_selectionListObjects.addElement(stp);
/* 404 */           autoSelect = false;
/*     */         }
/*     */       }
/*     */ 
/* 408 */       if ((v.size() == 1) && (autoSelect))
/*     */       {
/* 410 */         SchemaData data = (SchemaData)v.elementAt(0);
/* 411 */         this.m_selectionList.select(data.m_name);
/* 412 */         ItemEvent event = new ItemEvent(this.m_selectionList, 0, data.m_name, 1);
/*     */ 
/* 414 */         label616: itemStateChanged(event);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 420 */       Object priorObject = this.m_treeDefinition[(currentLevel * 2)];
/* 421 */       if (priorObject instanceof SchemaViewData)
/*     */       {
/* 423 */         SchemaViewData viewData = (SchemaViewData)priorObject;
/* 424 */         Vector v = this.m_schHelper.computeAllViewRelations(viewData.m_name);
/* 425 */         for (int i = 0; i < v.size(); ++i)
/*     */         {
/* 427 */           SchemaData data = (SchemaData)v.elementAt(i);
/* 428 */           this.m_selectionList.add(data.m_name);
/* 429 */           this.m_selectionListObjects.addElement(data);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 434 */         this.m_selectionList.removeAllItems();
/* 435 */         this.m_selectionList.add("");
/* 436 */         this.m_selectionList.setEnabled(false);
/* 437 */         isTerminated = true;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 442 */     if (isTerminated)
/*     */     {
/* 444 */       msg = IdcMessageFactory.lc("apSchemaTreeTerminated", new Object[0]);
/*     */     }
/*     */     else
/*     */     {
/* 448 */       msg = IdcMessageFactory.lc("apSchemaBuildingTreeLevel", new Object[] { new Integer(1 + currentLevel) });
/*     */     }
/* 450 */     this.m_currentLevelLabel.setText(si.localizeMessage(msg));
/*     */ 
/* 453 */     if (this.m_treeDefinition.length == 0)
/*     */     {
/* 455 */       this.m_removeLevelButton.setEnabled(false);
/*     */     }
/*     */     else
/*     */     {
/* 459 */       Object priorObject = this.m_treeDefinition[(this.m_treeDefinition.length - 1)];
/* 460 */       SchemaData data = null;
/* 461 */       String key = null;
/* 462 */       if (priorObject instanceof SchemaViewData)
/*     */       {
/* 464 */         key = "apSchemaTreeRemoveLevel_view";
/* 465 */         data = (SchemaData)priorObject;
/*     */       }
/* 467 */       else if (priorObject instanceof SchemaRelationData)
/*     */       {
/* 469 */         key = "apSchemaTreeRemoveLevel_relation";
/* 470 */         data = (SchemaData)priorObject;
/*     */       }
/* 472 */       else if (priorObject instanceof SchemaTreePointer)
/*     */       {
/* 474 */         key = "apSchemaTreeRemoveLevel_view";
/* 475 */         SchemaTreePointer stp = (SchemaTreePointer)priorObject;
/* 476 */         int index = stp.m_recursiveIndex;
/* 477 */         data = (SchemaData)this.m_treeDefinition[index];
/*     */       }
/* 479 */       msg = IdcMessageFactory.lc(key, new Object[] { data.m_name, new Integer(currentLevel + 1) });
/* 480 */       this.m_removeLevelButton.setText(si.localizeMessage(msg));
/* 481 */       this.m_removeLevelButton.setEnabled(true);
/*     */     }
/*     */ 
/* 484 */     Dimension size = comp.getPreferredSize();
/* 485 */     if ((size.height <= this.m_lastSize.height) && (size.width <= this.m_lastSize.width)) {
/*     */       return;
/*     */     }
/* 488 */     Dimension tmpSize = comp.getPreferredSize();
/* 489 */     size.height = Math.max(size.height, this.m_lastSize.height);
/* 490 */     size.width = Math.max(size.width, this.m_lastSize.width);
/* 491 */     this.m_lastSize = tmpSize;
/* 492 */     comp.setSize(size);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/*     */     try
/*     */     {
/* 500 */       String treeDefinition = this.m_helper.m_props.getProperty("TreeDefinition");
/*     */ 
/* 502 */       this.m_treeDefinition = this.m_schHelper.expandTreeDefinition(treeDefinition);
/*     */ 
/* 504 */       updateDisplay(true);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 508 */       MessageBox.reportError(this.m_systemInterface, e);
/* 509 */       return 0;
/*     */     }
/* 511 */     this.m_helper.loadComponentValues();
/* 512 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public boolean handleDialogEvent(ActionEvent event)
/*     */   {
/* 518 */     DataException exception = null;
/*     */     try
/*     */     {
/* 521 */       this.m_schHelper.validateTreeDefinition(this.m_treeDefinition);
/*     */ 
/* 523 */       String treeDefString = this.m_schHelper.createTreeString(this.m_treeDefinition);
/*     */ 
/* 525 */       this.m_helper.m_props.put("TreeDefinition", treeDefString);
/*     */ 
/* 527 */       return validateStorage();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 531 */       exception = e;
/*     */ 
/* 534 */       MessageBox.reportError(this.m_systemInterface, exception);
/* 535 */     }return false;
/*     */   }
/*     */ 
/*     */   protected boolean validateStorage()
/*     */   {
/* 542 */     Properties props = this.m_helper.m_props;
/* 543 */     if (!this.m_isNew)
/*     */     {
/* 545 */       IdcMessage msg = null;
/* 546 */       String oldSep = this.m_savedProps.getProperty("TreeNodeStorageSeparator");
/* 547 */       String sep = props.getProperty("TreeNodeStorageSeparator");
/* 548 */       if ((oldSep == null) || (!oldSep.equals(sep)))
/*     */       {
/* 550 */         msg = IdcMessageFactory.lc("apTreeStorageSeparatorIsChanged", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 554 */         boolean isOldPad = StringUtils.convertToBool(this.m_savedProps.getProperty("StoreSelectionPath"), false);
/*     */ 
/* 556 */         boolean isPad = StringUtils.convertToBool(props.getProperty("StoreSelectionPath"), false);
/*     */ 
/* 559 */         if (isOldPad != isPad)
/*     */         {
/* 561 */           msg = IdcMessageFactory.lc("apTreeStoragePathIsDifferent", new Object[0]);
/*     */         }
/*     */       }
/*     */ 
/* 565 */       if (msg != null)
/*     */       {
/* 567 */         int r = MessageBox.doMessage(this.m_systemInterface, msg, 2);
/* 568 */         if (r == 0)
/*     */         {
/* 570 */           return false;
/*     */         }
/*     */       }
/*     */     }
/* 574 */     return true;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent event)
/*     */   {
/* 579 */     Object source = event.getSource();
/* 580 */     if (source != this.m_selectionList)
/*     */       return;
/* 582 */     int index = this.m_selectionList.getSelectedIndex();
/* 583 */     if ((index == -1) || (index == 0))
/*     */     {
/* 585 */       return;
/*     */     }
/* 587 */     Object obj = this.m_selectionListObjects.elementAt(index);
/* 588 */     Object[] newDefinition = new Object[this.m_treeDefinition.length + 1];
/* 589 */     System.arraycopy(this.m_treeDefinition, 0, newDefinition, 0, this.m_treeDefinition.length);
/*     */ 
/* 591 */     newDefinition[this.m_treeDefinition.length] = obj;
/* 592 */     this.m_treeDefinition = newDefinition;
/*     */     try
/*     */     {
/* 595 */       updateDisplay(true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 599 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/* 606 */     Object source = event.getSource();
/* 607 */     if (source == this.m_removeLevelButton)
/*     */     {
/* 609 */       Object[] newDefinition = new Object[this.m_treeDefinition.length - 1];
/* 610 */       System.arraycopy(this.m_treeDefinition, 0, newDefinition, 0, newDefinition.length);
/*     */ 
/* 612 */       this.m_treeDefinition = newDefinition;
/*     */     }
/* 614 */     else if (source == this.m_selectParentButton)
/*     */     {
/* 616 */       String helpPage = DialogHelpTable.getHelpPage("SchemaSelectTreeRoot");
/* 617 */       String title = LocaleUtils.encodeMessage("apSchEditViewValuesTitle", null, this.m_systemInterface.getString("apSchemaTreeSelectRootTitle"));
/*     */ 
/* 619 */       EditViewValuesDlg dlg = new EditViewValuesDlg(this.m_systemInterface, title, helpPage);
/* 620 */       dlg.m_isSelectOnly = true;
/*     */ 
/* 622 */       SchemaTreePointer stp = (SchemaTreePointer)this.m_treeDefinition[(this.m_treeDefinition.length - 1)];
/* 623 */       SchemaViewData theView = (SchemaViewData)this.m_treeDefinition[stp.m_recursiveIndex];
/* 624 */       SchemaRelationData relationship = (SchemaRelationData)this.m_treeDefinition[(this.m_treeDefinition.length - 2)];
/* 625 */       String parentColumn = relationship.get("schTable1Column");
/* 626 */       dlg.m_parentColumn = parentColumn;
/*     */ 
/* 628 */       if ((stp.m_initialKeyValue != null) && (stp.m_initialKeyValue.length() > 0))
/*     */       {
/* 630 */         dlg.m_initialKeyValue = stp.m_initialKeyValue;
/*     */       }
/*     */ 
/* 633 */       Properties props = (Properties)theView.getData().getLocalData().clone();
/* 634 */       int result = dlg.init(props);
/* 635 */       if (result == 1)
/*     */       {
/* 637 */         DataResultSet drset = dlg.m_schemaView.getList().getSelectedAsResultSet();
/* 638 */         drset.first();
/* 639 */         String value = ResultSetUtils.getValue(drset, parentColumn);
/* 640 */         stp.m_initialKeyValue = value;
/*     */       }
/*     */     }
/*     */     try
/*     */     {
/* 645 */       updateDisplay(false);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 649 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 655 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditTreeDefinitionDialog
 * JD-Core Version:    0.5.4
 */