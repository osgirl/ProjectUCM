/*      */ package intradoc.apps.docconfig;
/*      */ 
/*      */ import intradoc.apps.shared.AppLauncher;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemInterface;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.gui.ComponentBinder;
/*      */ import intradoc.gui.CustomPanel;
/*      */ import intradoc.gui.DialogCallback;
/*      */ import intradoc.gui.DialogHelper;
/*      */ import intradoc.gui.DynamicComponentExchange;
/*      */ import intradoc.gui.GridBagHelper;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.gui.PanePanel;
/*      */ import intradoc.gui.WindowHelper;
/*      */ import intradoc.gui.iwt.UdlPanel;
/*      */ import intradoc.shared.DialogHelpTable;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.schema.SchemaTableConfig;
/*      */ import intradoc.shared.schema.SchemaTableData;
/*      */ import intradoc.shared.schema.SchemaTableDefinition;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.awt.Insets;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.awt.event.ItemEvent;
/*      */ import java.awt.event.ItemListener;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.swing.JButton;
/*      */ import javax.swing.JComboBox;
/*      */ import javax.swing.JPanel;
/*      */ 
/*      */ public class AddTableDlg extends DialogCallback
/*      */   implements ComponentBinder, ActionListener, ItemListener
/*      */ {
/*   78 */   protected SystemInterface m_systemInterface = null;
/*   79 */   protected ExecutionContext m_context = null;
/*   80 */   protected DialogHelper m_helper = null;
/*   81 */   protected String m_helpPage = null;
/*   82 */   protected String m_action = null;
/*      */   protected boolean m_isAdd;
/*   84 */   protected String m_initialTableName = null;
/*      */ 
/*   87 */   protected UdlPanel m_columnList = null;
/*   88 */   protected String[] m_labels = { "apDatabaseColumnNotSelected", "apSchIsCreateTimestamp", "apSchIsModifyTimestamp" };
/*      */   protected JComboBox m_createChoice;
/*      */   protected JComboBox m_modifyChoice;
/*   96 */   protected Hashtable m_buttons = new Hashtable();
/*      */ 
/*   98 */   protected boolean m_isInfoLoaded = false;
/*   99 */   protected Hashtable m_addedColumns = new Hashtable();
/*  100 */   protected Hashtable m_deletedColumns = new Hashtable();
/*  101 */   protected Hashtable m_modifiedColumns = new Hashtable();
/*  102 */   protected Hashtable m_originalColumns = new Hashtable();
/*  103 */   protected Vector m_originalPrimaryKey = new IdcVector();
/*      */   protected Properties m_actionErrors;
/*      */ 
/*      */   public AddTableDlg(SystemInterface sys, String title, String helpPage)
/*      */   {
/*  109 */     this.m_systemInterface = sys;
/*  110 */     this.m_context = sys.getExecutionContext();
/*  111 */     this.m_helper = new DialogHelper(sys, title, true);
/*  112 */     this.m_helpPage = helpPage;
/*      */   }
/*      */ 
/*      */   public int init(Properties props, boolean isAdd)
/*      */   {
/*  117 */     this.m_isAdd = isAdd;
/*  118 */     this.m_helper.m_props = props;
/*  119 */     props.put("IsCreateTable", new StringBuilder().append("").append(isAdd).toString());
/*  120 */     this.m_action = "ADDOREDIT_SCHEMA_TABLE";
/*      */ 
/*  122 */     this.m_dlgHelper = this.m_helper;
/*      */ 
/*  124 */     this.m_initialTableName = props.getProperty("schTableName");
/*      */ 
/*  126 */     initUI();
/*      */ 
/*  129 */     if (!loadColumnInfo())
/*      */     {
/*  132 */       return 0;
/*      */     }
/*      */ 
/*  135 */     return this.m_helper.prompt();
/*      */   }
/*      */ 
/*      */   protected void initUI()
/*      */   {
/*  140 */     JPanel mainPanel = this.m_helper.initStandard(this, this, 1, true, this.m_helpPage);
/*      */ 
/*  142 */     String label = this.m_systemInterface.getString("apSchemaSynchronizeButtonLabel");
/*      */ 
/*  144 */     JButton synchronizeButton = new JButton(label);
/*  145 */     synchronizeButton.setActionCommand("synchronizeDefinition");
/*  146 */     this.m_helper.addCommandButtonEx(synchronizeButton, this);
/*  147 */     if (this.m_isAdd)
/*      */     {
/*  149 */       synchronizeButton.setEnabled(false);
/*      */     }
/*      */ 
/*  152 */     this.m_columnList = SchemaHelperUtils.initListPanel(false, this.m_context);
/*  153 */     this.m_columnList.addItemListener(this);
/*  154 */     JPanel btnPanel = initButtonPanel();
/*  155 */     JPanel columnInfoPanel = initColumnInfoPanel(btnPanel);
/*  156 */     initInfoPanel(mainPanel);
/*  157 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 5, 5, 5);
/*  158 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  159 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  160 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*  161 */     this.m_helper.addLastComponentInRow(mainPanel, columnInfoPanel);
/*  162 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/*  163 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  164 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/*  165 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 5, 0, 5);
/*  166 */     initTimestampPanel(mainPanel);
/*      */   }
/*      */ 
/*      */   protected void initInfoPanel(JPanel panel)
/*      */   {
/*  171 */     int length = 30;
/*  172 */     this.m_helper.addLabelEditPair(panel, this.m_systemInterface.localizeCaption("apSchTableName"), length, "schTableName");
/*      */ 
/*  175 */     this.m_helper.addLabelEditPair(panel, LocaleResources.getString("apSchTableDescription", this.m_context), length, "schTableDescription");
/*      */   }
/*      */ 
/*      */   protected JPanel initColumnInfoPanel(JPanel btns)
/*      */   {
/*  182 */     JPanel panel = new CustomPanel();
/*  183 */     this.m_helper.makePanelGridBag(panel, 1);
/*      */ 
/*  185 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*  186 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/*  187 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  188 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  189 */     this.m_helper.addComponent(panel, this.m_columnList);
/*      */ 
/*  191 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/*  192 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/*  193 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 5, 0, 5);
/*  194 */     this.m_helper.addLastComponentInRow(panel, btns);
/*  195 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/*      */ 
/*  197 */     return panel;
/*      */   }
/*      */ 
/*      */   protected JPanel initButtonPanel()
/*      */   {
/*  203 */     String[][] btnInfo = { { "add", "apDlgButtonAddColumn", "0" }, { "edit", "apDlgButtonEditColumn", "1" }, { "delete", "apDlgButtonDeleteColumn", "1" }, { "addStandard", "apDlgButtonAddStandardColumns", "0" }, { "up", "apMoveUp", "1" }, { "down", "apMoveDown", "1" } };
/*      */ 
/*  213 */     JPanel btnPanel = new PanePanel();
/*  214 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*  215 */     for (int i = 0; i < btnInfo.length; ++i)
/*      */     {
/*  217 */       String cmd = btnInfo[i][0];
/*  218 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/*      */ 
/*  220 */       JButton btn = this.m_columnList.addButton(LocaleResources.getString(btnInfo[i][1], this.m_context), isControlled);
/*      */ 
/*  222 */       btn.setActionCommand(cmd);
/*  223 */       btn.addActionListener(this);
/*  224 */       this.m_buttons.put(cmd, btn);
/*  225 */       this.m_helper.addLastComponentInRow(btnPanel, btn);
/*      */     }
/*      */ 
/*  228 */     return btnPanel;
/*      */   }
/*      */ 
/*      */   protected void initTimestampPanel(JPanel panel)
/*      */   {
/*  233 */     this.m_createChoice = new JComboBox();
/*  234 */     this.m_modifyChoice = new JComboBox();
/*      */ 
/*  236 */     LocaleResources.localizeArray(this.m_labels, this.m_context);
/*  237 */     this.m_createChoice.addItem(this.m_labels[0]);
/*  238 */     this.m_modifyChoice.addItem(this.m_labels[0]);
/*  239 */     this.m_helper.addLabelFieldPair(panel, this.m_labels[1], this.m_createChoice, "schTableRowCreateTimestamp");
/*  240 */     this.m_helper.addLabelFieldPair(panel, this.m_labels[2], this.m_modifyChoice, "schTableRowModifyTimestamp");
/*      */   }
/*      */ 
/*      */   public void enableDisableButton(String cmd, boolean state)
/*      */   {
/*  245 */     JButton btn = (JButton)this.m_buttons.get(cmd);
/*  246 */     if (btn == null)
/*      */       return;
/*  248 */     btn.setEnabled(state);
/*      */   }
/*      */ 
/*      */   protected boolean loadColumnInfo()
/*      */   {
/*  254 */     DataResultSet[] clmnSet = new DataResultSet[1];
/*  255 */     String selObj = this.m_columnList.getSelectedObj();
/*  256 */     String errMsg = SchemaHelperUtils.loadColumnInfo(selObj, this.m_helper.m_props, clmnSet);
/*      */ 
/*  260 */     Properties props = this.m_helper.m_props;
/*  261 */     boolean exists = StringUtils.convertToBool(props.getProperty("TableExists"), false);
/*  262 */     if ((this.m_isAdd) && 
/*  265 */       (exists))
/*      */     {
/*  267 */       String tableName = props.getProperty("schTableName");
/*  268 */       IdcMessage msg = IdcMessageFactory.lc("apSchUnableToCreateTableExistsPrompt", new Object[] { tableName });
/*  269 */       if (MessageBox.doMessage(this.m_systemInterface, msg, 2) == 0)
/*      */       {
/*  271 */         return false;
/*      */       }
/*  273 */       props.remove("IsCreateTable");
/*      */     }
/*      */ 
/*  277 */     if ((exists) && (!this.m_isAdd))
/*      */     {
/*  279 */       if (props.get("schTableRowCreateTimestamp") == null)
/*      */       {
/*  281 */         props.put("schTableRowCreateTimestamp", "schCreateTimestamp");
/*      */       }
/*  283 */       if (props.get("schTableRowModifyTimestamp") == null)
/*      */       {
/*  285 */         props.put("schTableRowModifyTimestamp", "schModifyTimestamp");
/*      */       }
/*      */     }
/*      */ 
/*  289 */     boolean result = false;
/*  290 */     if (errMsg == null)
/*      */     {
/*  292 */       refresh(clmnSet[0], selObj);
/*  293 */       result = true;
/*      */     }
/*      */     else
/*      */     {
/*  297 */       MessageBox.reportError(this.m_systemInterface, IdcMessageFactory.lc(errMsg, new Object[0]));
/*      */     }
/*  299 */     return result;
/*      */   }
/*      */ 
/*      */   protected void refresh(DataResultSet columns, String selObj)
/*      */   {
/*  304 */     boolean hasPrimary = false;
/*  305 */     boolean hasCreateTimestamp = false;
/*  306 */     boolean hasModifyTimestamp = false;
/*      */ 
/*  308 */     for (columns.first(); columns.isRowPresent(); columns.next())
/*      */     {
/*  310 */       Properties props = columns.getCurrentRowProps();
/*  311 */       String name = props.getProperty(SchemaTableData.TABLE_DEFINITION_COLUMNS[SchemaTableData.COLUMN_NAME_INDEX]);
/*      */ 
/*  314 */       this.m_originalColumns.put(name, props);
/*      */ 
/*  316 */       String isPrimary = props.getProperty(SchemaTableData.TABLE_DEFINITION_COLUMNS[SchemaTableData.PRIMARY_KEY_INDEX]);
/*      */ 
/*  319 */       if (StringUtils.convertToBool(isPrimary, false))
/*      */       {
/*  321 */         hasPrimary = true;
/*  322 */         this.m_originalPrimaryKey.addElement(name);
/*      */       }
/*      */ 
/*  325 */       String isCreateTimestamp = props.getProperty("IsCreateTimestamp");
/*  326 */       if (StringUtils.convertToBool(isCreateTimestamp, false))
/*      */       {
/*  328 */         hasCreateTimestamp = true;
/*      */       }
/*      */ 
/*  331 */       String isModifyTimestamp = props.getProperty("IsModifyTimestamp");
/*  332 */       if (!StringUtils.convertToBool(isModifyTimestamp, false))
/*      */         continue;
/*  334 */       hasModifyTimestamp = true;
/*      */     }
/*      */ 
/*  337 */     DataResultSet schemaObjectPermissions = SharedObjects.getTable("SchemaObjectPermissions");
/*  338 */     String keyField = new StringBuilder().append("SchemaTableConfig/").append(this.m_helper.m_props.getProperty("schTableName")).toString();
/*  339 */     FieldInfo info = new FieldInfo();
/*  340 */     schemaObjectPermissions.getFieldInfo("schObjectKey", info);
/*  341 */     if (schemaObjectPermissions.findRow(info.m_index, keyField) != null)
/*      */     {
/*  343 */       this.m_actionErrors = schemaObjectPermissions.getCurrentRowProps();
/*      */     }
/*      */     else
/*      */     {
/*  347 */       this.m_actionErrors = new IdcProperties();
/*      */     }
/*  349 */     boolean allowModify = isAllowModify();
/*  350 */     if (((hasPrimary) && (hasCreateTimestamp) && (hasModifyTimestamp)) || (!allowModify))
/*      */     {
/*  352 */       enableDisableButton("addStandard", false);
/*      */     }
/*      */     else
/*      */     {
/*  356 */       enableDisableButton("addStandard", true);
/*      */     }
/*      */ 
/*  359 */     enableDisableButton("add", allowModify);
/*      */ 
/*  361 */     String currentCreateChoice = (String)this.m_createChoice.getSelectedItem();
/*  362 */     String currentModifyChoice = (String)this.m_modifyChoice.getSelectedItem();
/*  363 */     this.m_createChoice.removeAllItems();
/*  364 */     this.m_modifyChoice.removeAllItems();
/*  365 */     this.m_createChoice.addItem(this.m_labels[0]);
/*  366 */     this.m_modifyChoice.addItem(this.m_labels[0]);
/*  367 */     for (columns.first(); columns.isRowPresent(); columns.next())
/*      */     {
/*  369 */       String name = columns.getStringValue(SchemaTableDefinition.COLUMN_NAME_INDEX);
/*  370 */       String type = columns.getStringValue(SchemaTableDefinition.COLUMN_TYPE_INDEX);
/*  371 */       if (type.toLowerCase().indexOf("date") < 0)
/*      */         continue;
/*  373 */       this.m_createChoice.addItem(name);
/*  374 */       this.m_modifyChoice.addItem(name);
/*      */     }
/*      */ 
/*  377 */     this.m_createChoice.setSelectedItem(currentCreateChoice);
/*  378 */     this.m_modifyChoice.setSelectedItem(currentModifyChoice);
/*      */ 
/*  380 */     this.m_helper.setEnabled(this.m_createChoice, allowModify);
/*  381 */     this.m_helper.setEnabled(this.m_modifyChoice, allowModify);
/*      */ 
/*  383 */     this.m_columnList.refreshList(columns, selObj);
/*  384 */     this.m_isInfoLoaded = true;
/*      */   }
/*      */ 
/*      */   public boolean isAllowModify()
/*      */   {
/*  389 */     String error = this.m_actionErrors.getProperty("schError_modify");
/*  390 */     return (error == null) || (error.length() == 0);
/*      */   }
/*      */ 
/*      */   protected DataBinder buildBinder() throws ServiceException
/*      */   {
/*  395 */     String tableName = this.m_helper.m_props.getProperty("schTableName");
/*  396 */     if (tableName.length() == 0)
/*      */     {
/*  398 */       throw new ServiceException("!apSchSpecifyTableName");
/*      */     }
/*      */ 
/*  401 */     DataBinder binder = new DataBinder();
/*  402 */     binder.setLocalData(this.m_helper.m_props);
/*      */ 
/*  405 */     DataResultSet rset = (DataResultSet)this.m_columnList.getResultSet();
/*  406 */     if (rset.getNumRows() == 0)
/*      */     {
/*  408 */       throw new ServiceException("!apNeedsAtLeastOneColumn");
/*      */     }
/*  410 */     binder.addResultSet("TableDefinition", rset);
/*      */ 
/*  412 */     String createTimestamp = (String)this.m_createChoice.getSelectedItem();
/*  413 */     String modifyTimestamp = (String)this.m_modifyChoice.getSelectedItem();
/*  414 */     if ((!createTimestamp.equals(this.m_labels[0])) && (!modifyTimestamp.equals(this.m_labels[0])) && (createTimestamp.equals(modifyTimestamp)))
/*      */     {
/*  418 */       throw new ServiceException("!apSchSameTimestampColumns");
/*      */     }
/*      */ 
/*  421 */     if (createTimestamp.equals(this.m_labels[0]))
/*      */     {
/*  423 */       binder.removeLocal("schTableRowCreateTimestamp");
/*      */     }
/*  425 */     if (modifyTimestamp.equals(this.m_labels[0]))
/*      */     {
/*  427 */       binder.removeLocal("schTableRowModifyTimestamp");
/*      */     }
/*      */ 
/*  430 */     String tmp = hashtableKeysToString(this.m_addedColumns);
/*  431 */     binder.putLocal("_AddedColumns", tmp);
/*  432 */     tmp = hashtableKeysToString(this.m_deletedColumns);
/*  433 */     binder.putLocal("_DeletedColumns", tmp);
/*  434 */     tmp = hashtableKeysToString(this.m_modifiedColumns);
/*  435 */     binder.putLocal("_ModifiedColumns", tmp);
/*      */ 
/*  437 */     return binder;
/*      */   }
/*      */ 
/*      */   protected String hashtableKeysToString(Hashtable table)
/*      */   {
/*  442 */     Vector list = new IdcVector();
/*  443 */     Enumeration en = table.keys();
/*  444 */     while (en.hasMoreElements())
/*      */     {
/*  446 */       String key = (String)en.nextElement();
/*  447 */       list.addElement(key);
/*      */     }
/*      */ 
/*  450 */     String tmp = StringUtils.createString(list, ',', '^');
/*  451 */     return tmp;
/*      */   }
/*      */ 
/*      */   public void actionPerformed(ActionEvent e)
/*      */   {
/*  456 */     String cmd = e.getActionCommand();
/*  457 */     DataResultSet columnSet = (DataResultSet)this.m_columnList.getResultSet();
/*  458 */     if (cmd.equals("synchronizeDefinition"))
/*      */     {
/*  460 */       synchronizeTableDefinition();
/*  461 */       refresh(columnSet, null);
/*      */     }
/*  463 */     else if (cmd.equals("add"))
/*      */     {
/*  465 */       addOrEditColumn(true);
/*  466 */       refresh(columnSet, null);
/*      */     }
/*  468 */     else if (cmd.equals("edit"))
/*      */     {
/*  470 */       addOrEditColumn(false);
/*  471 */       refresh(columnSet, null);
/*      */     }
/*  473 */     else if (cmd.equals("delete"))
/*      */     {
/*  475 */       deleteColumn();
/*  476 */       refresh(columnSet, null);
/*      */     }
/*  478 */     else if (cmd.equals("addStandard"))
/*      */     {
/*  480 */       addStandardColumns(columnSet);
/*  481 */       refresh(columnSet, null);
/*      */     }
/*  483 */     else if (cmd.equals("up"))
/*      */     {
/*  485 */       this.m_columnList.setSort(null, false);
/*  486 */       FieldInfo info = new FieldInfo();
/*  487 */       columnSet.getFieldInfo(SchemaTableData.TABLE_DEFINITION_COLUMNS[SchemaTableData.COLUMN_NAME_INDEX], info);
/*      */ 
/*  490 */       String selObj = this.m_columnList.getSelectedObj();
/*  491 */       Vector row = columnSet.findRow(info.m_index, selObj);
/*  492 */       int rowNumber = columnSet.getCurrentRow();
/*  493 */       if (rowNumber > 0)
/*      */       {
/*  495 */         Vector priorValues = columnSet.getRowValues(rowNumber - 1);
/*  496 */         columnSet.setRowValues(priorValues, rowNumber);
/*  497 */         columnSet.setRowValues(row, rowNumber - 1);
/*  498 */         refresh(columnSet, selObj);
/*      */       }
/*      */     } else {
/*  501 */       if (!cmd.equals("down"))
/*      */         return;
/*  503 */       this.m_columnList.setSort(null, false);
/*  504 */       FieldInfo info = new FieldInfo();
/*  505 */       columnSet.getFieldInfo(SchemaTableData.TABLE_DEFINITION_COLUMNS[SchemaTableData.COLUMN_NAME_INDEX], info);
/*      */ 
/*  508 */       String selObj = this.m_columnList.getSelectedObj();
/*  509 */       Vector row = columnSet.findRow(info.m_index, selObj);
/*  510 */       int rowNumber = columnSet.getCurrentRow();
/*  511 */       if (rowNumber + 1 >= columnSet.getNumRows())
/*      */         return;
/*  513 */       Vector nextValues = columnSet.getRowValues(rowNumber + 1);
/*  514 */       columnSet.setRowValues(nextValues, rowNumber);
/*  515 */       columnSet.setRowValues(row, rowNumber + 1);
/*  516 */       refresh(columnSet, selObj);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addStandardColumns(DataResultSet columnSet)
/*      */   {
/*  523 */     String createChoice = (String)this.m_createChoice.getSelectedItem();
/*  524 */     String modifyChoice = (String)this.m_modifyChoice.getSelectedItem();
/*      */ 
/*  526 */     boolean hasPrimaryKey = false;
/*  527 */     boolean hasSourceKey = false;
/*  528 */     boolean isCreateTs = createChoice.equals(this.m_labels[0]);
/*  529 */     boolean isModifyTs = modifyChoice.equals(this.m_labels[0]);
/*  530 */     boolean hasCreateTs = false;
/*  531 */     boolean hasModifyTs = false;
/*  532 */     for (columnSet.first(); columnSet.isRowPresent(); columnSet.next())
/*      */     {
/*  534 */       String isKey = columnSet.getStringValue(SchemaTableDefinition.PRIMARY_KEY_INDEX);
/*      */ 
/*  536 */       if ((!hasPrimaryKey) && (StringUtils.convertToBool(isKey, false)))
/*      */       {
/*  538 */         hasPrimaryKey = true;
/*      */       }
/*  540 */       String name = columnSet.getStringValue(SchemaTableDefinition.COLUMN_NAME_INDEX);
/*  541 */       if ((!hasSourceKey) && (name.equals("schSourceID")))
/*      */       {
/*  543 */         hasSourceKey = true;
/*      */       }
/*  545 */       if ((isCreateTs) && 
/*  547 */         (name.equals("schCreateTimestamp")))
/*      */       {
/*  549 */         hasCreateTs = true;
/*      */       }
/*      */ 
/*  552 */       if ((!isModifyTs) || 
/*  554 */         (!name.equals("schModifyTimestamp")))
/*      */         continue;
/*  556 */       hasModifyTs = true;
/*      */     }
/*      */ 
/*  561 */     Properties props = null;
/*  562 */     if ((!hasPrimaryKey) && (this.m_isAdd))
/*      */     {
/*  564 */       props = new Properties();
/*  565 */       props.put("ColumnName", "schPrimaryKey");
/*  566 */       props.put("ColumnType", "varchar");
/*  567 */       props.put("ColumnLength", "20");
/*  568 */       props.put("IsPrimaryKey", "true");
/*  569 */       addEditColumnDef(columnSet, "schPrimaryKey", props, true);
/*      */     }
/*      */ 
/*  572 */     if (isCreateTs)
/*      */     {
/*  574 */       props = new Properties();
/*  575 */       props.put("ColumnName", "schCreateTimestamp");
/*  576 */       props.put("ColumnType", "date");
/*  577 */       props.put("ColumnLength", "1");
/*  578 */       props.put("IsCreateTimestamp", "true");
/*  579 */       addEditColumnDef(columnSet, "schCreateTimestamp", props, !hasCreateTs);
/*      */     }
/*      */ 
/*  582 */     if (isModifyTs)
/*      */     {
/*  584 */       props = new Properties();
/*  585 */       props.put("ColumnName", "schModifyTimestamp");
/*  586 */       props.put("ColumnType", "date");
/*  587 */       props.put("ColumnLength", "1");
/*  588 */       props.put("IsModifyTimestamp", "true");
/*  589 */       addEditColumnDef(columnSet, "schModifyTimestamp", props, !hasModifyTs);
/*      */     }
/*      */ 
/*  593 */     props = new Properties();
/*  594 */     props.put("ColumnName", "schSourceID");
/*  595 */     props.put("ColumnType", "varchar");
/*  596 */     props.put("ColumnLength", "50");
/*  597 */     addEditColumnDef(columnSet, "schSourceID", props, !hasSourceKey);
/*      */ 
/*  600 */     refresh(columnSet, null);
/*  601 */     if (isCreateTs)
/*      */     {
/*  603 */       this.m_createChoice.setSelectedItem("schCreateTimestamp");
/*      */     }
/*  605 */     if (!isModifyTs)
/*      */       return;
/*  607 */     this.m_modifyChoice.setSelectedItem("schModifyTimestamp");
/*      */   }
/*      */ 
/*      */   public boolean handleDialogEvent(ActionEvent event)
/*      */   {
/*      */     try
/*      */     {
/*  616 */       DataBinder binder = buildBinder();
/*  617 */       AppLauncher.executeService(this.m_action, binder, this.m_systemInterface);
/*      */     }
/*      */     catch (ServiceException exp)
/*      */     {
/*  622 */       if (exp.m_errorCode == -23)
/*      */       {
/*  626 */         IdcMessage message = IdcMessageFactory.lc(exp, "apSchemaSynchronizeTableQuestion", new Object[0]);
/*  627 */         int rc = MessageBox.doMessage(this.m_systemInterface, message, 4);
/*      */ 
/*  629 */         if (rc == 2)
/*      */         {
/*  631 */           synchronizeTableDefinition();
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  636 */         MessageBox.reportError(this.m_systemInterface, exp);
/*      */       }
/*  638 */       return false;
/*      */     }
/*  640 */     return true;
/*      */   }
/*      */ 
/*      */   protected void synchronizeTableDefinition()
/*      */   {
/*  645 */     DataBinder binder = new DataBinder();
/*  646 */     String tableName = this.m_helper.m_props.getProperty("schTableName");
/*      */ 
/*  648 */     binder.putLocal("schTableName", tableName);
/*  649 */     SchemaTableConfig tableConfig = (SchemaTableConfig)SharedObjects.getTable("SchemaTableConfig");
/*      */ 
/*  651 */     if (tableConfig != null)
/*      */     {
/*  653 */       binder.addResultSet("SchemaTableConfig", tableConfig);
/*      */     }
/*      */     try
/*      */     {
/*  657 */       AppLauncher.executeService("SYNCHRONIZE_TABLE_DEFINITION", binder, this.m_systemInterface);
/*      */ 
/*  659 */       tableConfig = (SchemaTableConfig)SharedObjects.getTable("SchemaTableConfig");
/*      */ 
/*  661 */       SchemaTableData currentTableDefinition = (SchemaTableData)tableConfig.getData(tableName);
/*      */ 
/*  663 */       DataResultSet[] columnSet = new DataResultSet[1];
/*  664 */       String selectedColumn = this.m_columnList.getSelectedObj();
/*  665 */       String errMsg = SchemaHelperUtils.loadColumnInfo(selectedColumn, this.m_helper.m_props, columnSet);
/*      */ 
/*  668 */       IdcMessage iErrMsg = null;
/*  669 */       if (errMsg == null)
/*      */       {
/*  671 */         DataResultSet drset = columnSet[0];
/*  672 */         iErrMsg = synchronizeColumns(currentTableDefinition, drset, tableName);
/*  673 */         refresh(drset, selectedColumn);
/*      */       }
/*      */       else
/*      */       {
/*  677 */         iErrMsg = IdcMessageFactory.lc(errMsg, new Object[0]);
/*      */       }
/*  679 */       if (iErrMsg != null)
/*      */       {
/*  681 */         MessageBox.doMessage(this.m_systemInterface, errMsg, 1);
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  686 */       MessageBox.reportError(this.m_systemInterface, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected IdcMessage synchronizeColumns(SchemaTableData currentTableDefinition, DataResultSet drset, String tableName)
/*      */   {
/*  693 */     IdcMessage errMsg = null;
/*  694 */     Hashtable newAddedColumns = new Hashtable();
/*  695 */     Hashtable newDeletedColumns = new Hashtable();
/*  696 */     Hashtable newModifiedColumns = new Hashtable();
/*      */ 
/*  700 */     Hashtable pkMap = new Hashtable();
/*  701 */     if (currentTableDefinition.m_primaryKeyColumns != null)
/*      */     {
/*  703 */       int len = currentTableDefinition.m_primaryKeyColumns.length;
/*  704 */       for (int i = 0; i < len; ++i)
/*      */       {
/*  706 */         pkMap.put(currentTableDefinition.m_primaryKeyColumns[i].m_name, "");
/*      */       }
/*      */     }
/*      */ 
/*  710 */     IdcMessage msg = null;
/*  711 */     Enumeration en = this.m_addedColumns.keys();
/*  712 */     while (en.hasMoreElements())
/*      */     {
/*  714 */       String columnName = (String)en.nextElement();
/*  715 */       FieldInfo info = currentTableDefinition.getFieldInfo(columnName);
/*  716 */       Properties props = (Properties)this.m_addedColumns.get(columnName);
/*  717 */       Object obj = pkMap.get(columnName);
/*  718 */       props.put("IsPrimaryKey", new StringBuilder().append("").append(obj != null).toString());
/*      */ 
/*  720 */       if (info == null)
/*      */       {
/*  722 */         newAddedColumns.put(columnName, props);
/*  723 */         addEditColumnDef(drset, columnName, props, true);
/*      */       }
/*      */       else
/*      */       {
/*  728 */         newModifiedColumns.put(columnName, props);
/*  729 */         msg = IdcMessageFactory.lc(msg, "apSchemaAddColumnConflict", new Object[] { columnName, tableName });
/*  730 */         addEditColumnDef(drset, columnName, props, false);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  735 */     en = this.m_deletedColumns.keys();
/*  736 */     while (en.hasMoreElements())
/*      */     {
/*  738 */       String columnName = (String)en.nextElement();
/*  739 */       FieldInfo info = currentTableDefinition.getFieldInfo(columnName);
/*  740 */       Properties props = (Properties)this.m_deletedColumns.get(columnName);
/*  741 */       Object obj = pkMap.get(columnName);
/*  742 */       props.put("IsPrimaryKey", new StringBuilder().append("").append(obj != null).toString());
/*      */ 
/*  744 */       if (info != null)
/*      */       {
/*  750 */         newDeletedColumns.put(columnName, props);
/*  751 */         Vector row = drset.findRow(0, columnName);
/*  752 */         if (row != null)
/*      */         {
/*  754 */           drset.deleteCurrentRow();
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  759 */     en = this.m_modifiedColumns.keys();
/*  760 */     while (en.hasMoreElements())
/*      */     {
/*  762 */       String columnName = (String)en.nextElement();
/*  763 */       FieldInfo info = currentTableDefinition.getFieldInfo(columnName);
/*  764 */       Properties props = (Properties)this.m_modifiedColumns.get(columnName);
/*  765 */       Object obj = pkMap.get(columnName);
/*  766 */       props.put("IsPrimaryKey", new StringBuilder().append("").append(obj != null).toString());
/*      */ 
/*  768 */       if (info == null)
/*      */       {
/*  770 */         errMsg = IdcMessageFactory.lc("apSchemaModifyDeleteConflict", new Object[] { columnName, tableName });
/*  771 */         errMsg.m_prior = msg;
/*  772 */         newAddedColumns.put(columnName, props);
/*  773 */         addEditColumnDef(drset, columnName, props, true);
/*      */       }
/*      */       else
/*      */       {
/*  783 */         newModifiedColumns.put(columnName, props);
/*  784 */         addEditColumnDef(drset, columnName, props, false);
/*      */       }
/*      */     }
/*      */ 
/*  788 */     this.m_addedColumns = newAddedColumns;
/*  789 */     this.m_deletedColumns = newDeletedColumns;
/*  790 */     this.m_modifiedColumns = newModifiedColumns;
/*      */ 
/*  792 */     return errMsg;
/*      */   }
/*      */ 
/*      */   protected void addOrEditColumn(boolean isAdd)
/*      */   {
/*  797 */     Properties props = new Properties();
/*  798 */     String title = "apSchAddColumnDlg";
/*  799 */     String columnName = null;
/*  800 */     if (!isAdd)
/*      */     {
/*  802 */       title = "apSchEditColumnDlg";
/*  803 */       int selIndex = this.m_columnList.getSelectedIndex();
/*      */ 
/*  805 */       props = this.m_columnList.getDataAt(selIndex);
/*  806 */       columnName = props.getProperty("ColumnName");
/*      */     }
/*  808 */     title = LocaleUtils.encodeMessage(title, null, columnName);
/*  809 */     AddColumnDlg dlg = new AddColumnDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage("AddOrEditSchemaColumn"));
/*      */ 
/*  812 */     DataResultSet clmnSet = (DataResultSet)this.m_columnList.getResultSet();
/*  813 */     Map args = new HashMap();
/*  814 */     args.put("isNewTable", new StringBuilder().append("").append(this.m_isAdd).toString());
/*  815 */     args.put("isNewColumn", new StringBuilder().append("").append(isAdd).toString());
/*  816 */     args.put("isNewToEdit", new StringBuilder().append("").append(isAdd).toString());
/*  817 */     if (!isAdd)
/*      */     {
/*  820 */       Object obj = this.m_addedColumns.get(columnName);
/*  821 */       if (obj != null)
/*      */       {
/*  823 */         args.put("isNewToEdit", "1");
/*      */       }
/*      */     }
/*  826 */     int result = dlg.init(props, clmnSet, args);
/*  827 */     if ((result != 1) || 
/*  829 */       (!addEditColumnDef(clmnSet, columnName, props, isAdd)))
/*      */       return;
/*  831 */     columnName = props.getProperty("ColumnName");
/*  832 */     this.m_columnList.reloadList(columnName);
/*      */   }
/*      */ 
/*      */   protected boolean addEditColumnDef(DataResultSet columnSet, String columnName, Properties props, boolean isAdd)
/*      */   {
/*  840 */     boolean rc = true;
/*      */     try
/*      */     {
/*  843 */       PropParameters params = new PropParameters(props);
/*  844 */       Vector row = columnSet.createRow(params);
/*  845 */       String newColumnName = props.getProperty("ColumnName");
/*  846 */       Vector selRow = null;
/*  847 */       Properties addedProps = (Properties)this.m_addedColumns.get(newColumnName);
/*      */ 
/*  849 */       Properties deletedProps = (Properties)this.m_deletedColumns.get(newColumnName);
/*      */ 
/*  852 */       if (columnName != null)
/*      */       {
/*  854 */         selRow = columnSet.findRow(0, columnName);
/*      */       }
/*      */ 
/*  857 */       if (isAdd)
/*      */       {
/*  859 */         if (selRow == null)
/*      */         {
/*  861 */           columnName = props.getProperty("ColumnName");
/*  862 */           columnSet.addRow(row);
/*  863 */           if (deletedProps != null)
/*      */           {
/*  865 */             this.m_deletedColumns.remove(newColumnName);
/*  866 */             this.m_modifiedColumns.put(newColumnName, props);
/*      */           }
/*      */           else
/*      */           {
/*  870 */             this.m_addedColumns.put(newColumnName, props);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  875 */           rc = false;
/*      */         }
/*      */ 
/*      */       }
/*  880 */       else if (selRow != null)
/*      */       {
/*  882 */         int index = columnSet.getCurrentRow();
/*  883 */         columnSet.setRowValues(row, index);
/*  884 */         if (addedProps == null)
/*      */         {
/*  886 */           if (SystemUtils.m_verbose)
/*      */           {
/*  888 */             Report.debug("schema", new StringBuilder().append("adding column '").append(newColumnName).append("' to m_modifiedColumns").toString(), null);
/*      */           }
/*      */ 
/*  891 */           this.m_modifiedColumns.put(newColumnName, props);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  896 */         rc = false;
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  902 */       MessageBox.reportError(this.m_systemInterface, e);
/*  903 */       rc = false;
/*      */     }
/*      */ 
/*  906 */     return rc;
/*      */   }
/*      */ 
/*      */   protected void deleteColumn()
/*      */   {
/*  911 */     String clmnName = this.m_columnList.getSelectedObj();
/*      */ 
/*  913 */     IdcMessage msg = IdcMessageFactory.lc("apSchDeleteColumnPrompt", new Object[] { clmnName });
/*  914 */     int result = MessageBox.doMessage(this.m_systemInterface, msg, 4);
/*  915 */     if (result != 2) {
/*      */       return;
/*      */     }
/*  918 */     DataResultSet rset = (DataResultSet)this.m_columnList.getResultSet();
/*  919 */     Vector row = rset.findRow(0, clmnName);
/*  920 */     if (this.m_addedColumns.get(clmnName) == null)
/*      */     {
/*  922 */       this.m_deletedColumns.put(clmnName, rset.getCurrentRowProps());
/*      */     }
/*      */     else
/*      */     {
/*  926 */       this.m_addedColumns.remove(clmnName);
/*      */     }
/*  928 */     this.m_modifiedColumns.remove(clmnName);
/*  929 */     if (row != null)
/*      */     {
/*  931 */       rset.deleteCurrentRow();
/*      */     }
/*  933 */     this.m_columnList.reloadList(null);
/*      */   }
/*      */ 
/*      */   public void itemStateChanged(ItemEvent event)
/*      */   {
/*  939 */     int[] indices = this.m_columnList.getSelectedIndexes();
/*  940 */     int rows = this.m_columnList.getNumRows();
/*  941 */     boolean allowModify = isAllowModify();
/*  942 */     if (indices.length != 1)
/*      */     {
/*  944 */       enableDisableButton("up", false);
/*  945 */       enableDisableButton("down", false);
/*      */     }
/*      */     else
/*      */     {
/*  949 */       if (indices[0] == 0)
/*      */       {
/*  951 */         enableDisableButton("up", false);
/*  952 */         enableDisableButton("down", true);
/*      */       }
/*  954 */       else if (indices[0] == rows - 1)
/*      */       {
/*  956 */         enableDisableButton("up", true);
/*  957 */         enableDisableButton("down", false);
/*      */       }
/*      */       else
/*      */       {
/*  961 */         enableDisableButton("up", true);
/*  962 */         enableDisableButton("down", true);
/*      */       }
/*  964 */       enableDisableButton("edit", allowModify);
/*  965 */       enableDisableButton("delete", allowModify);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*      */   {
/*  975 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/*  976 */     helper.exchangeComponentValue(exchange, updateComponent);
/*  977 */     if ((this.m_initialTableName != null) || (!exchange.m_compName.equals("schTableName")))
/*      */       return;
/*  979 */     this.m_initialTableName = exchange.m_compValue;
/*      */   }
/*      */ 
/*      */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*      */   {
/*  985 */     String name = exchange.m_compName;
/*  986 */     String val = exchange.m_compValue;
/*  987 */     IdcMessage errMsg = null;
/*      */ 
/*  989 */     if ((name.equals("schTableName")) && 
/*  991 */       (!this.m_isAdd) && (this.m_initialTableName != null) && (!val.equalsIgnoreCase(this.m_initialTableName)))
/*      */     {
/*  993 */       exchange.m_compValue = this.m_initialTableName;
/*  994 */       errMsg = IdcMessageFactory.lc("apSchemaTableCaseChangeOnly", new Object[] { this.m_initialTableName });
/*      */     }
/*      */ 
/*  998 */     if (errMsg != null)
/*      */     {
/* 1000 */       exchange.m_errorMessage = errMsg;
/* 1001 */       return false;
/*      */     }
/*      */ 
/* 1004 */     return true;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1009 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82989 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.AddTableDlg
 * JD-Core Version:    0.5.4
 */