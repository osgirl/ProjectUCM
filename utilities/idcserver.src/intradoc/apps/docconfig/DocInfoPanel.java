/*      */ package intradoc.apps.docconfig;
/*      */ 
/*      */ import intradoc.apps.shared.AppLauncher;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemInterface;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.gui.ContainerHelper;
/*      */ import intradoc.gui.CustomLabel;
/*      */ import intradoc.gui.CustomText;
/*      */ import intradoc.gui.CustomTextField;
/*      */ import intradoc.gui.DialogCallback;
/*      */ import intradoc.gui.DialogHelper;
/*      */ import intradoc.gui.DisplayStringCallback;
/*      */ import intradoc.gui.GridBagHelper;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.gui.PanePanel;
/*      */ import intradoc.gui.iwt.ColumnInfo;
/*      */ import intradoc.gui.iwt.UdlPanel;
/*      */ import intradoc.gui.iwt.UserDrawList;
/*      */ import intradoc.shared.DialogHelpTable;
/*      */ import intradoc.shared.MetaFieldData;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.TableFields;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.gui.ComponentValidator;
/*      */ import intradoc.shared.gui.EditOptionListDlg;
/*      */ import intradoc.shared.gui.SelectValueDlg;
/*      */ import intradoc.shared.schema.SchemaData;
/*      */ import intradoc.shared.schema.SchemaFieldConfig;
/*      */ import intradoc.shared.schema.SchemaFieldData;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import intradoc.shared.schema.SchemaViewConfig;
/*      */ import intradoc.shared.schema.SchemaViewData;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.awt.BorderLayout;
/*      */ import java.awt.Component;
/*      */ import java.awt.Insets;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.awt.event.ItemEvent;
/*      */ import java.awt.event.ItemListener;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.accessibility.AccessibleContext;
/*      */ import javax.swing.JButton;
/*      */ import javax.swing.JCheckBox;
/*      */ import javax.swing.JPanel;
/*      */ 
/*      */ public class DocInfoPanel extends DocConfigPanel
/*      */   implements ActionListener, ItemListener, DisplayStringCallback
/*      */ {
/*      */   public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97523 $";
/*      */   protected UdlPanel m_metaList;
/*   95 */   protected DataResultSet m_metaFields = null;
/*      */ 
/*   99 */   protected String m_targetName = null;
/*  100 */   protected String m_idField = null;
/*  101 */   protected String m_typeField = null;
/*  102 */   protected String m_orderField = null;
/*      */ 
/*  107 */   protected Hashtable m_btnMap = null;
/*      */   protected int m_maxOrder;
/*      */   protected JButton m_databaseUpdate;
/*      */   protected JButton m_rebuildAll;
/*      */   protected JButton m_addFieldButton;
/*      */   protected JButton m_editFieldButton;
/*      */   protected JButton m_deleteFieldButton;
/*      */   protected JButton m_editTreeButton;
/*  121 */   protected Vector m_addFields = null;
/*  122 */   protected Vector m_changeFields = null;
/*  123 */   protected Vector m_deleteFields = null;
/*      */   protected SchemaHelper m_schemaHelper;
/*      */ 
/*      */   public DocInfoPanel()
/*      */   {
/*  130 */     this.m_subject = "dynamicqueries,metadata,schema";
/*      */   }
/*      */ 
/*      */   public boolean isApplicationFields()
/*      */   {
/*  136 */     return !isDocMetaFields();
/*      */   }
/*      */ 
/*      */   public boolean isDocMetaFields()
/*      */   {
/*  141 */     return (this.m_baseClassName != null) && (this.m_baseClassName.equals("DocInfoPanel"));
/*      */   }
/*      */ 
/*      */   public void init(SystemInterface sys)
/*      */     throws ServiceException
/*      */   {
/*  148 */     this.m_schemaHelper = new SchemaHelper();
/*  149 */     super.init(sys);
/*      */ 
/*  151 */     JPanel panel = initUI();
/*      */ 
/*  153 */     this.m_helper.makePanelGridBag(this, 1);
/*      */ 
/*  155 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  156 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/*  157 */     this.m_helper.addLastComponentInRow(this, new CustomText(""));
/*      */ 
/*  159 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  160 */     this.m_helper.addLastComponentInRow(this, panel);
/*      */ 
/*  162 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/*  163 */     this.m_helper.addLastComponentInRow(this, new CustomText(""));
/*  164 */     refreshMetafieldList();
/*      */ 
/*  166 */     enableDisable();
/*      */   }
/*      */ 
/*      */   protected JPanel initUI() throws ServiceException
/*      */   {
/*  171 */     JPanel mainPanel = new PanePanel();
/*  172 */     mainPanel.setLayout(new BorderLayout());
/*      */ 
/*  174 */     String idColumn = null;
/*  175 */     String tableName = null;
/*  176 */     String defaultFields = null;
/*  177 */     String fieldsVar = null;
/*  178 */     if (isApplicationFields())
/*      */     {
/*  180 */       idColumn = "schFieldName";
/*  181 */       tableName = "ApplicationFields";
/*  182 */       defaultFields = "schFieldName,schFieldType,schOrder";
/*  183 */       fieldsVar = "ApplicationFieldsDefaultFieldList";
/*  184 */       this.m_targetName = "Application";
/*  185 */       this.m_idField = "schFieldName";
/*  186 */       this.m_typeField = "schFieldType";
/*  187 */       this.m_orderField = "schOrder";
/*      */     }
/*  189 */     else if (isDocMetaFields())
/*      */     {
/*  191 */       idColumn = "dName";
/*  192 */       tableName = "DocMetaDefinition";
/*  193 */       defaultFields = "dName,dType,dIsEnabled,dIsSearchable,dOrder,dComponentName,dDocMetaSet";
/*  194 */       fieldsVar = "ConfigFieldsDefaultFieldList";
/*  195 */       this.m_targetName = "DocMeta";
/*  196 */       this.m_idField = "dName";
/*  197 */       this.m_typeField = "dType";
/*  198 */       this.m_orderField = "dOrder";
/*      */     }
/*      */     else
/*      */     {
/*  203 */       throw new ServiceException("!$Invalid state in DocInfoPanel");
/*      */     }
/*  205 */     this.m_metaList = new UdlPanel(this.m_systemInterface.getString("apLabelFieldInfo"), null, 275, 20, tableName, true);
/*      */ 
/*  209 */     List columns = new ArrayList();
/*  210 */     String tmp = SharedObjects.getEnvironmentValue(fieldsVar);
/*  211 */     if (tmp == null)
/*      */     {
/*  213 */       tmp = defaultFields;
/*      */     }
/*  215 */     columns = StringUtils.parseArray(tmp, ',', '^');
/*      */ 
/*  218 */     for (int i = 0; i < columns.size(); ++i)
/*      */     {
/*  220 */       String columnName = (String)columns.get(i);
/*  221 */       String[] labelPrefixes = { "apTitle", "apLabel" };
/*      */ 
/*  225 */       String[] labelKeys = { columnName, columnName.substring(1), columnName.substring(3) };
/*      */ 
/*  229 */       String label = null;
/*  230 */       for (int j = 0; j < labelPrefixes.length; ++j)
/*      */       {
/*  232 */         for (int k = 0; k < labelKeys.length; ++k)
/*      */         {
/*  234 */           label = LocaleResources.getStringInternal(labelPrefixes[j] + labelKeys[k], this.m_ctx);
/*      */ 
/*  236 */           if (label != null) {
/*      */             break;
/*      */           }
/*      */         }
/*      */ 
/*  241 */         if (label != null) {
/*      */           break;
/*      */         }
/*      */       }
/*      */ 
/*  246 */       if (label == null)
/*      */       {
/*  248 */         Report.trace("localization", "unable to find label for column " + columnName, null);
/*      */ 
/*  250 */         label = columnName;
/*      */       }
/*  252 */       int defaultLength = 12;
/*  253 */       if ((columnName.indexOf("Is") >= 0) || (columnName.indexOf("Has") >= 0) || (columnName.indexOf("Order") >= 0) || (columnName.indexOf("Use") >= 0))
/*      */       {
/*  258 */         defaultLength = 5;
/*      */       }
/*  260 */       String lookupKey = "DisplayLength:" + columnName;
/*  261 */       if (SharedObjects.getEnvironmentValue(lookupKey) != null)
/*      */       {
/*  263 */         defaultLength = SharedObjects.getEnvironmentInt(lookupKey, defaultLength);
/*      */       }
/*      */ 
/*  267 */       ColumnInfo info = new ColumnInfo(label, columnName, defaultLength);
/*  268 */       this.m_metaList.setColumnInfo(info);
/*      */     }
/*      */ 
/*  272 */     this.m_metaList.setVisibleColumns(StringUtils.createString(columns, ',', '^'));
/*  273 */     this.m_metaList.setIDColumn(idColumn);
/*  274 */     this.m_metaList.init();
/*  275 */     this.m_metaList.useDefaultListener();
/*  276 */     this.m_metaList.addItemListener(this);
/*  277 */     mainPanel.add("Center", this.m_metaList);
/*      */ 
/*  280 */     addListButtons(this.m_metaList);
/*      */ 
/*  283 */     addListComponents();
/*      */ 
/*  286 */     JPanel updateButtonsPanel = new PanePanel();
/*  287 */     mainPanel.add("East", updateButtonsPanel);
/*  288 */     this.m_helper.makePanelGridBag(updateButtonsPanel, 2);
/*  289 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 5, 5, 5);
/*  290 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  291 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.2D;
/*  292 */     this.m_helper.m_gridHelper.addEmptyRow(updateButtonsPanel);
/*      */ 
/*  295 */     if (isDocMetaFields())
/*      */     {
/*  297 */       addAdvancedButton(updateButtonsPanel);
/*  298 */       addUpdateButtons(updateButtonsPanel);
/*      */     }
/*      */ 
/*  302 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  303 */     this.m_helper.m_gridHelper.addEmptyRow(updateButtonsPanel);
/*      */ 
/*  305 */     return mainPanel;
/*      */   }
/*      */ 
/*      */   public void doLayout()
/*      */   {
/*  311 */     super.doLayout();
/*      */   }
/*      */ 
/*      */   protected void addListButtons(UdlPanel list)
/*      */   {
/*  316 */     this.m_btnMap = new Hashtable();
/*  317 */     JPanel btnPanel = list.getButtonPanel();
/*  318 */     this.m_helper.makePanelGridBag(btnPanel, 0);
/*      */ 
/*  320 */     String[][] btnInfo = { { "moveUp", "apLabelUp", "1" }, { "moveDown", "apLabelDown", "1" } };
/*      */ 
/*  325 */     for (int i = 0; i < btnInfo.length; ++i)
/*      */     {
/*  327 */       String cmd = btnInfo[i][0];
/*  328 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/*  329 */       JButton btn = list.addButton(this.m_systemInterface.getString(btnInfo[i][1]), isControlled);
/*  330 */       this.m_helper.addComponent(btnPanel, btn);
/*  331 */       btn.addActionListener(this);
/*  332 */       btn.setActionCommand(cmd);
/*      */ 
/*  334 */       this.m_btnMap.put(cmd, btn);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*      */   {
/*  345 */     SchemaData target = this.m_schemaHelper.getSchemaData("SchemaTargetConfig", this.m_targetName);
/*      */ 
/*  347 */     if (name.equals(this.m_idField))
/*      */     {
/*  350 */       FieldInfo fi = this.m_metaList.getFieldInfo(this.m_orderField);
/*  351 */       int index = fi.m_index;
/*  352 */       String orderStr = (String)row.elementAt(index);
/*  353 */       int order = Integer.parseInt(orderStr);
/*  354 */       if (order > this.m_maxOrder)
/*      */       {
/*  356 */         this.m_maxOrder = order;
/*      */       }
/*      */ 
/*  359 */       if (isApplicationFields())
/*      */       {
/*  361 */         return value;
/*      */       }
/*  363 */       return MetaFieldGui.createDisplayName(value);
/*      */     }
/*  365 */     if (name.equals("dType"))
/*      */     {
/*  367 */       if ("Decimal".equalsIgnoreCase(value))
/*      */       {
/*  369 */         return StringUtils.getPresentationString(TableFields.METAFIELD_TYPE_DECIMAL_OPTION, value);
/*      */       }
/*      */ 
/*  372 */       return StringUtils.getPresentationString(TableFields.METAFIELD_TYPES_OPTIONSLIST, value);
/*      */     }
/*      */ 
/*  375 */     if ((name.equals("dIsEnabled")) || (name.equals("dIsSearchable")))
/*      */     {
/*  378 */       return (StringUtils.convertToBool(value, false)) ? TableFields.YESNO_OPTIONLIST[0][1] : TableFields.YESNO_OPTIONLIST[1][1];
/*      */     }
/*      */ 
/*  382 */     DataResultSet fields = target.getResultSet("TargetFieldInfo");
/*  383 */     for (fields.first(); fields.isRowPresent(); fields.next())
/*      */     {
/*  385 */       Properties props = fields.getCurrentRowProps();
/*  386 */       String targetFieldName = props.getProperty("schFieldName");
/*  387 */       if (!name.equals(targetFieldName))
/*      */         continue;
/*  389 */       String type = props.getProperty("schFieldType");
/*      */ 
/*  391 */       if (!type.equals("boolean"))
/*      */         break;
/*  393 */       return (StringUtils.convertToBool(value, false)) ? TableFields.YESNO_OPTIONLIST[0][1] : TableFields.YESNO_OPTIONLIST[1][1];
/*      */     }
/*      */ 
/*  400 */     return value;
/*      */   }
/*      */ 
/*      */   public String createExtendedDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*      */   {
/*  406 */     return null;
/*      */   }
/*      */ 
/*      */   protected void addListComponents()
/*      */     throws ServiceException
/*      */   {
/*  413 */     this.m_metaList.setDisplayCallback(this.m_idField, this);
/*  414 */     this.m_metaList.setDisplayCallback(this.m_typeField, this);
/*  415 */     this.m_metaList.setDisplayCallback("dIsEnabled", this);
/*  416 */     this.m_metaList.setDisplayCallback("dIsSearchable", this);
/*      */ 
/*  418 */     SchemaData target = this.m_schemaHelper.getSchemaData("SchemaTargetConfig", this.m_targetName);
/*  419 */     if (target == null)
/*      */     {
/*  425 */       Report.trace("system", null, "apSchTargetConfigMissing", new Object[] { this.m_targetName });
/*      */     }
/*      */     else
/*      */     {
/*  429 */       DataResultSet targetFields = target.getResultSet("TargetFieldInfo");
/*  430 */       for (targetFields.first(); targetFields.isRowPresent(); targetFields.next())
/*      */       {
/*  432 */         Properties props = targetFields.getCurrentRowProps();
/*  433 */         String type = props.getProperty("schFieldType");
/*  434 */         if (!type.equals("boolean"))
/*      */           continue;
/*  436 */         String field = props.getProperty("schFieldName");
/*  437 */         this.m_metaList.setDisplayCallback(field, this);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  443 */     JPanel metaButtons = new PanePanel();
/*      */ 
/*  447 */     this.m_addFieldButton = new JButton(LocaleResources.getString("apDlgButtonAdd", this.m_ctx));
/*  448 */     this.m_addFieldButton.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableAddMetadataField", this.m_ctx));
/*  449 */     metaButtons.add(this.m_addFieldButton);
/*  450 */     this.m_editFieldButton = this.m_metaList.addButton(LocaleResources.getString("apDlgButtonEdit", this.m_ctx), true);
/*  451 */     this.m_editFieldButton.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableEditMetadataField", this.m_ctx));
/*  452 */     metaButtons.add(this.m_editFieldButton);
/*      */ 
/*  454 */     this.m_addFieldButton.addActionListener(this);
/*  455 */     this.m_addFieldButton.setActionCommand("addItem");
/*  456 */     this.m_editFieldButton.addActionListener(this);
/*  457 */     this.m_editFieldButton.setActionCommand("editItem");
/*  458 */     this.m_metaList.m_list.addActionListener(this);
/*  459 */     this.m_metaList.m_list.setActionCommand("editItem");
/*  460 */     this.m_editFieldButton.setEnabled(false);
/*      */ 
/*  462 */     this.m_deleteFieldButton = this.m_metaList.addButton(LocaleResources.getString("apLabelDelete", this.m_ctx), true);
/*  463 */     this.m_deleteFieldButton.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableDeleteMetadataField", this.m_ctx));
/*  464 */     metaButtons.add(this.m_deleteFieldButton);
/*  465 */     this.m_deleteFieldButton.addActionListener(this);
/*  466 */     this.m_deleteFieldButton.setActionCommand("deleteItem");
/*  467 */     this.m_deleteFieldButton.setEnabled(false);
/*      */ 
/*  470 */     metaButtons.add(new PanePanel());
/*  471 */     this.m_editTreeButton = this.m_metaList.addButton(LocaleResources.getString("apDlgButtonEditValues", this.m_ctx), true);
/*  472 */     this.m_editTreeButton.getAccessibleContext().setAccessibleName(LocaleResources.getString("apSchEditTreeValues", this.m_ctx));
/*  473 */     metaButtons.add(this.m_editTreeButton);
/*  474 */     this.m_editTreeButton.addActionListener(this);
/*  475 */     this.m_editTreeButton.setActionCommand("editTreeValues");
/*  476 */     this.m_editTreeButton.setEnabled(false);
/*      */ 
/*  479 */     this.m_metaList.add("South", metaButtons);
/*      */   }
/*      */ 
/*      */   public void addItem()
/*      */   {
/*  484 */     if (isDocMetaFields())
/*      */     {
/*  486 */       addOrEditMeta(true);
/*      */     } else {
/*  488 */       if (!isApplicationFields())
/*      */         return;
/*  490 */       addOrEditApplicationField(null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void editItem()
/*      */   {
/*  496 */     if (isDocMetaFields())
/*      */     {
/*  498 */       addOrEditMeta(false);
/*      */     } else {
/*  500 */       if (!isApplicationFields())
/*      */         return;
/*  502 */       addOrEditApplicationField(this.m_metaList.getSelectedObj());
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addOrEditMeta(boolean isAdd)
/*      */   {
/*  508 */     Properties data = null;
/*  509 */     if (!isAdd)
/*      */     {
/*  511 */       int index = this.m_metaList.getSelectedIndex();
/*  512 */       if (index >= 0)
/*      */       {
/*  514 */         data = this.m_metaList.getDataAt(index);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  519 */       data = promptNewFieldName();
/*  520 */       if (data == null)
/*      */       {
/*  522 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  526 */     DataBinder binder = new DataBinder();
/*  527 */     binder.setLocalData(data);
/*      */     try
/*      */     {
/*  530 */       String name = data.getProperty("dName");
/*  531 */       if (name.equals("xIdcProfile"))
/*      */       {
/*  533 */         return;
/*      */       }
/*      */ 
/*  536 */       binder.putLocal("schFieldName", name);
/*  537 */       binder.putLocal("AllowMissing", "1");
/*  538 */       executeService("GET_SCHEMA_FIELD_INFO", binder, false);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  542 */       reportError(e);
/*  543 */       return;
/*      */     }
/*      */ 
/*  546 */     String displayFieldName = MetaFieldGui.getDispFieldName(data);
/*  547 */     String title = LocaleResources.getString("apEditCustomInfoField", this.m_ctx, displayFieldName);
/*      */ 
/*  549 */     String helpPageName = "EditMetaFields";
/*  550 */     if (isAdd)
/*      */     {
/*  552 */       title = LocaleResources.getString("apAddCustomInfoField", this.m_ctx, displayFieldName);
/*      */ 
/*  554 */       helpPageName = "AddMetaFields";
/*      */     }
/*      */ 
/*  557 */     EditMetafieldDlg dlg = new EditMetafieldDlg(this.m_systemInterface, title, (DataResultSet)this.m_metaList.getResultSet(), isAdd, DialogHelpTable.getHelpPage(helpPageName));
/*      */ 
/*  560 */     dlg.init(data, this);
/*  561 */     if (dlg.prompt() != 1) {
/*      */       return;
/*      */     }
/*      */     try
/*      */     {
/*  566 */       data.setProperty("isNewField", "false");
/*  567 */       refreshMetafieldList(data.getProperty("dName"));
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  571 */       reportError(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addOrEditApplicationField(String selectedObject)
/*      */   {
/*  578 */     DataBinder binder = null;
/*  579 */     String title = null;
/*  580 */     String helpPageName = null;
/*  581 */     if (selectedObject != null)
/*      */     {
/*  583 */       SchemaData schemaData = this.m_schemaHelper.m_fields.getData(selectedObject);
/*  584 */       if (schemaData == null)
/*      */       {
/*  586 */         reportError(null, IdcMessageFactory.lc("apSchNoParentFieldInfo", new Object[] { selectedObject }));
/*  587 */         return;
/*      */       }
/*  589 */       binder = schemaData.getData();
/*  590 */       title = LocaleResources.getString("apEditApplicationField", this.m_ctx, selectedObject);
/*      */ 
/*  592 */       helpPageName = "EditApplicationFields";
/*      */     }
/*      */     else
/*      */     {
/*  596 */       binder = new DataBinder();
/*      */ 
/*  598 */       binder.putLocal(this.m_typeField, "Text");
/*  599 */       binder.putLocal(this.m_orderField, Integer.toString(this.m_maxOrder + 1));
/*  600 */       binder.putLocal("schFieldTarget", this.m_targetName);
/*  601 */       title = LocaleResources.getString("apAddApplicationField", this.m_ctx);
/*  602 */       helpPageName = "AddApplicationFields";
/*      */     }
/*      */ 
/*  605 */     EditGlobalFieldDialog editDialog = new EditGlobalFieldDialog(this.m_systemInterface, title, this.m_metaFields, DialogHelpTable.getHelpPage(helpPageName));
/*      */     try
/*      */     {
/*  610 */       editDialog.init(binder, this);
/*  611 */       if (editDialog.prompt() == 1)
/*      */       {
/*  614 */         refreshMetafieldList(binder.getLocal("schFieldName"));
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  619 */       reportError(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void editTreeValues()
/*      */   {
/*  625 */     String name = this.m_metaList.getSelectedObj();
/*  626 */     if (name == null)
/*      */     {
/*  628 */       return;
/*      */     }
/*      */ 
/*  631 */     SchemaFieldConfig fieldConfig = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/*  632 */     SchemaFieldData fieldData = (SchemaFieldData)fieldConfig.getData(name);
/*  633 */     if (fieldData == null)
/*      */     {
/*  635 */       return;
/*      */     }
/*      */ 
/*  638 */     boolean isComplexList = StringUtils.convertToBool(fieldData.get("UseViewList"), false);
/*  639 */     if (!isComplexList)
/*      */     {
/*  641 */       isComplexList = StringUtils.convertToBool(fieldData.get("UseTreeControl"), false);
/*      */     }
/*      */ 
/*  644 */     SchemaViewData viewData = null;
/*  645 */     String viewName = null;
/*  646 */     Properties viewDataProps = null;
/*  647 */     boolean isOptionsListView = false;
/*  648 */     if (!isComplexList)
/*      */     {
/*  650 */       String optionListKey = fieldData.get("dOptionListKey");
/*  651 */       if ((optionListKey != null) && 
/*  653 */         (optionListKey.startsWith("view://")))
/*      */       {
/*  655 */         viewName = optionListKey.substring("view://".length());
/*  656 */         SchemaViewConfig viewConfig = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*  657 */         viewData = (SchemaViewData)viewConfig.getData(viewName);
/*  658 */         if (viewData != null)
/*      */         {
/*  660 */           viewDataProps = (Properties)viewData.getData().getLocalData().clone();
/*      */ 
/*  662 */           String tableName = viewDataProps.getProperty("schTableName");
/*  663 */           if ((tableName != null) && (tableName.equalsIgnoreCase("OptionsList")))
/*      */           {
/*  665 */             String criteriaValue = viewDataProps.getProperty("schCriteriaValue0");
/*  666 */             if (criteriaValue == null)
/*      */             {
/*  668 */               reportError(null, IdcMessageFactory.lc("apSchSelectedViewIsOptionsListWithoutCriteria", new Object[0]));
/*  669 */               return;
/*      */             }
/*      */ 
/*  672 */             isOptionsListView = true;
/*  673 */             viewDataProps.put("dType", "text");
/*  674 */             viewDataProps.put("dOptionListKey", criteriaValue);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  681 */     if (isComplexList)
/*      */     {
/*  683 */       String title = this.m_systemInterface.getString("apSchEditTreeValues");
/*  684 */       String helpPage = DialogHelpTable.getHelpPage("EditMetaDataTreeValues");
/*      */ 
/*  686 */       SchemaHelper schHelper = new SchemaHelper();
/*  687 */       schHelper.computeMaps();
/*      */ 
/*  689 */       SelectValueDlg dlg = new SelectValueDlg(this.m_systemInterface, title, helpPage, this);
/*  690 */       dlg.init(schHelper, name, null, true, true, false);
/*      */ 
/*  692 */       dlg.prompt();
/*      */     }
/*  694 */     else if ((viewData != null) && (!isOptionsListView))
/*      */     {
/*  696 */       String title = LocaleUtils.encodeMessage("apSchEditViewValuesTitle", null, viewName);
/*      */ 
/*  698 */       title = LocaleResources.localizeMessage(title, this.m_ctx);
/*  699 */       EditViewValuesDlg dlg = new EditViewValuesDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage("EditViewValues"));
/*      */ 
/*  701 */       dlg.init(viewDataProps);
/*      */     }
/*      */     else
/*      */     {
/*  706 */       String title = this.m_systemInterface.getString("apLabelOptionList");
/*      */ 
/*  708 */       Properties props = null;
/*  709 */       if (isOptionsListView)
/*      */       {
/*  711 */         props = viewDataProps;
/*      */       }
/*      */       else
/*      */       {
/*  715 */         DataBinder binder = fieldData.getData();
/*  716 */         Properties localData = binder.getLocalData();
/*  717 */         props = (Properties)localData.clone();
/*      */       }
/*      */ 
/*  720 */       EditOptionListDlg edtOptions = new EditOptionListDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage("OptionList"), "UPDATE_OPTION_LIST");
/*      */ 
/*  722 */       edtOptions.init(props);
/*  723 */       edtOptions.prompt();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void rebuildAll()
/*      */   {
/*      */     try
/*      */     {
/*  731 */       if (((this.m_addFields != null) && (this.m_addFields.size() > 0)) || ((this.m_changeFields != null) && (this.m_changeFields.size() > 0)))
/*      */       {
/*  735 */         MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apUpdateDatabaseBeforeAttemptingRebuild", new Object[0]), 1);
/*      */ 
/*  738 */         return;
/*      */       }
/*      */ 
/*  741 */       int rc = MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifySearchIndexRebuild", new Object[0]), 2);
/*      */ 
/*  744 */       if (rc == 1)
/*      */       {
/*  746 */         boolean useVdkLegacyRebuild = SharedObjects.getEnvValueAsBoolean("UseVdkLegacyRebuild", false);
/*  747 */         boolean useVdkLegacySearch = SharedObjects.getEnvValueAsBoolean("UseVdkLegacySearch", false);
/*  748 */         IdcMessage msg = null;
/*      */ 
/*  750 */         if (((!useVdkLegacyRebuild) && (useVdkLegacySearch)) || ((useVdkLegacyRebuild) && (!useVdkLegacySearch)))
/*      */         {
/*  752 */           if ((!useVdkLegacyRebuild) && (useVdkLegacySearch))
/*      */           {
/*  754 */             msg = IdcMessageFactory.lc("apVdk4RebuildWarning", new Object[0]);
/*      */           }
/*  756 */           else if ((useVdkLegacyRebuild) && (!useVdkLegacySearch))
/*      */           {
/*  758 */             msg = IdcMessageFactory.lc("apVdkLegacyRebuildWarning", new Object[0]);
/*      */           }
/*  760 */           assert (msg != null);
/*  761 */           if (MessageBox.doMessage(this.m_systemInterface, msg, 4) != 2)
/*      */           {
/*  764 */             return;
/*      */           }
/*      */         }
/*      */ 
/*  768 */         String action = "START_SEARCH_INDEX";
/*  769 */         DataBinder data = new DataBinder();
/*  770 */         data.putLocal("IsRebuild", "1");
/*      */ 
/*  772 */         boolean supportFastRebuild = SharedObjects.getEnvValueAsBoolean("SupportFastRebuild", false);
/*  773 */         if (supportFastRebuild == true)
/*      */         {
/*  775 */           data.putLocal("fastRebuild", "1");
/*      */         }
/*      */ 
/*  778 */         executeService(action, data, false);
/*  779 */         MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apLabelRebuildInitiated", new Object[0]), 1);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (ServiceException exp)
/*      */     {
/*  785 */       reportError(exp);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addAdvancedButton(JPanel panel)
/*      */   {
/*  792 */     boolean supportAdvanceOp = SharedObjects.getEnvValueAsBoolean("SupportAdvanceConfigOptions", false);
/*  793 */     if (!supportAdvanceOp)
/*      */       return;
/*  795 */     JButton advanceSearchBtn = new JButton(this.m_systemInterface.getString("apAdvanceSearchDesignBtn"));
/*  796 */     this.m_helper.addComponent(panel, advanceSearchBtn);
/*  797 */     advanceSearchBtn.addActionListener(this);
/*  798 */     advanceSearchBtn.setActionCommand("configureSearch");
/*      */   }
/*      */ 
/*      */   protected void addUpdateButtons(JPanel panel)
/*      */   {
/*  804 */     this.m_databaseUpdate = new JButton(this.m_systemInterface.getString("apUpdateDatabaseDesign"));
/*  805 */     this.m_helper.addComponent(panel, this.m_databaseUpdate);
/*  806 */     this.m_databaseUpdate.addActionListener(this);
/*  807 */     this.m_databaseUpdate.setActionCommand("updateDatabase");
/*      */ 
/*  809 */     this.m_rebuildAll = new JButton(LocaleResources.getString("apRebuildSearchIndex", this.m_ctx));
/*  810 */     this.m_helper.addComponent(panel, this.m_rebuildAll);
/*  811 */     this.m_rebuildAll.addActionListener(this);
/*  812 */     this.m_rebuildAll.setActionCommand("rebuildAll");
/*      */   }
/*      */ 
/*      */   protected void refreshMetafieldList() throws ServiceException
/*      */   {
/*  817 */     String selectedObj = this.m_metaList.getSelectedObj();
/*  818 */     refreshMetafieldList(selectedObj);
/*      */   }
/*      */ 
/*      */   protected void refreshMetafieldList(String selectedObj) throws ServiceException
/*      */   {
/*  823 */     this.m_schemaHelper.refresh();
/*  824 */     if (isApplicationFields())
/*      */     {
/*      */       try
/*      */       {
/*  828 */         refreshApplicationFieldsList(selectedObj);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  832 */         throw new ServiceException(e);
/*      */       }
/*      */     } else {
/*  835 */       if (!isDocMetaFields())
/*      */         return;
/*  837 */       DataBinder binder = new DataBinder();
/*      */       try
/*      */       {
/*  840 */         executeService("GET_DMS_TABLES", binder, false);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  844 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/*  848 */       this.m_maxOrder = 0;
/*      */ 
/*  850 */       DataResultSet metaSet = SharedObjects.getTable("DocMetaDefinition");
/*  851 */       this.m_metaFields = metaSet.shallowClone();
/*      */ 
/*  853 */       binder.addResultSet("DocMetaDefinition", metaSet);
/*  854 */       this.m_metaList.refreshList(binder, selectedObj);
/*      */ 
/*  857 */       MetaFieldData mData = new MetaFieldData();
/*      */       try
/*      */       {
/*  860 */         metaSet.first();
/*  861 */         mData.init(metaSet);
/*  862 */         SharedObjects.putTable("DocMetaDefinition", mData);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  866 */         reportError(e, IdcMessageFactory.lc("apUnableToGetSystemInfo", new Object[0]));
/*  867 */         return;
/*      */       }
/*      */ 
/*  870 */       this.m_addFields = new IdcVector();
/*  871 */       this.m_changeFields = new IdcVector();
/*  872 */       this.m_deleteFields = new IdcVector();
/*  873 */       boolean changesNeeded = false;
/*      */ 
/*  875 */       ResultSet rset = binder.getResultSet("DmsTables");
/*  876 */       if (rset != null)
/*      */       {
/*  878 */         for (rset.first(); rset.isRowPresent(); rset.next())
/*      */         {
/*  880 */           String table = rset.getStringValueByName("table");
/*  881 */           ResultSet metaRset = binder.getResultSet(table);
/*  882 */           changesNeeded = (mData.createDiffListMultiEx(table, metaRset, this.m_addFields, this.m_changeFields, this.m_deleteFields, null)) || (changesNeeded);
/*      */         }
/*      */       }
/*      */ 
/*  886 */       this.m_databaseUpdate.setEnabled(changesNeeded);
/*      */ 
/*  888 */       boolean useAltaVista = SharedObjects.getEnvValueAsBoolean("UseAltaVista", false);
/*  889 */       boolean rebuildNeeded = false;
/*  890 */       if (!useAltaVista)
/*      */       {
/*  892 */         rebuildNeeded = !SharedObjects.getEnvValueAsBoolean("IndexCollectionSynced", true);
/*      */       }
/*  894 */       boolean indexDesignRebuildRequired = SharedObjects.getEnvValueAsBoolean("IndexDesignRebuildRequired", false);
/*      */ 
/*  897 */       this.m_rebuildAll.setEnabled(((!changesNeeded) && (rebuildNeeded)) || (indexDesignRebuildRequired));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void refreshApplicationFieldsList(String selectedObj)
/*      */     throws ServiceException, DataException
/*      */   {
/*  904 */     SchemaViewData view = (SchemaViewData)this.m_schemaHelper.m_views.getData("ApplicationFields");
/*  905 */     DataResultSet rset = (DataResultSet)view.getAllViewValues();
/*  906 */     this.m_metaFields = rset;
/*  907 */     DataBinder binder = new DataBinder();
/*  908 */     binder.addResultSet("ApplicationFields", rset);
/*  909 */     this.m_metaList.refreshList(binder, selectedObj);
/*      */   }
/*      */ 
/*      */   protected Properties promptNewFieldName()
/*      */   {
/*  914 */     ComponentValidator cmpValidator = new ComponentValidator(this.m_metaList.getResultSet());
/*  915 */     int maxLength = cmpValidator.getMaxLength("dName", 30) - 1;
/*      */ 
/*  917 */     DialogCallback okCallback = new DialogCallback(maxLength)
/*      */     {
/*      */       public boolean handleDialogEvent(ActionEvent e)
/*      */       {
/*  923 */         Properties promptData = this.m_dlgHelper.m_props;
/*  924 */         String name = MetaFieldGui.getDbFieldName(promptData);
/*  925 */         int val = Validation.checkDatabaseFieldName(MetaFieldGui.createDisplayName(name));
/*      */ 
/*  927 */         switch (val)
/*      */         {
/*      */         case 0:
/*  931 */           if (DocInfoPanel.this.m_metaList.findRowPrimaryField(name) < 0)
/*      */           {
/*  933 */             if (name.length() > this.val$maxLength)
/*      */             {
/*  935 */               this.m_errorMessage = IdcMessageFactory.lc("apInfoFieldNameExceedsMaxLength", new Object[] { Integer.valueOf(this.val$maxLength) });
/*  936 */               return false;
/*      */             }
/*  938 */             promptData.put("dName", name);
/*  939 */             return true;
/*      */           }
/*  941 */           this.m_errorMessage = IdcMessageFactory.lc("apInfoFieldNameConflict", new Object[0]);
/*  942 */           break;
/*      */         case -1:
/*  944 */           this.m_errorMessage = IdcMessageFactory.lc("apSpecifyInfoFieldName", new Object[0]);
/*  945 */           break;
/*      */         case -2:
/*  947 */           this.m_errorMessage = IdcMessageFactory.lc("apNameCannotContainSpaces", new Object[0]);
/*  948 */           break;
/*      */         case -3:
/*  950 */           this.m_errorMessage = IdcMessageFactory.lc("apInvalidCharInFieldName", new Object[0]);
/*  951 */           break;
/*      */         default:
/*  953 */           this.m_errorMessage = IdcMessageFactory.lc("apInvalidNameForInfoField", new Object[0]);
/*      */         }
/*      */ 
/*  956 */         return false;
/*      */       }
/*      */     };
/*  961 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apTitleAddCustomInfoField", this.m_ctx), true);
/*      */ 
/*  963 */     JPanel mainPanel = helper.initStandard(null, okCallback, 2, true, DialogHelpTable.getHelpPage("AddCustomDocInfo"));
/*      */ 
/*  965 */     helper.addLabelFieldPair(mainPanel, this.m_systemInterface.localizeCaption("apLabelFieldName"), new CustomTextField(20), "FieldName");
/*      */ 
/*  969 */     if (helper.prompt() == 1)
/*      */     {
/*  972 */       Properties props = helper.m_props;
/*  973 */       String fieldName = props.getProperty("FieldName");
/*      */ 
/*  975 */       MetaFieldData metaData = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/*  976 */       List oldField = metaData.findRow(metaData.m_nameIndex, "x" + fieldName, 0, 0);
/*  977 */       if (oldField == null)
/*      */       {
/*  979 */         props.put("dType", "Text");
/*  980 */         props.put("isNewField", "true");
/*      */       }
/*      */       else
/*      */       {
/*  984 */         String type = (String)oldField.get(metaData.m_typeIndex);
/*  985 */         props.put("dType", type);
/*  986 */         props.put("dOldType", type);
/*      */       }
/*  988 */       props.put("dIsEnabled", "1");
/*  989 */       props.put("dIsSearchable", "1");
/*  990 */       props.put("dCaption", fieldName);
/*  991 */       props.put("dOrder", Integer.toString(this.m_maxOrder + 1));
/*      */ 
/*  993 */       return props;
/*      */     }
/*  995 */     return null;
/*      */   }
/*      */ 
/*      */   protected void promptSearchDesignChange()
/*      */   {
/* 1001 */     DataBinder binder = new DataBinder();
/*      */     try
/*      */     {
/* 1004 */       executeServiceWithCursor("GET_ADVANCED_SEARCH_OPTIONS", binder);
/*      */ 
/* 1006 */       String helpPage = DialogHelpTable.getHelpPage("AdvancedSearchDesign");
/* 1007 */       AdvancedSearchDesignDlg dlg = new AdvancedSearchDesignDlg(this.m_systemInterface, this.m_systemInterface.getString("apAdvancedSearchDesignTitle"), this, helpPage);
/*      */ 
/* 1009 */       dlg.init(binder);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1013 */       reportError(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean promptDesignChanged(Vector delFields)
/*      */   {
/* 1019 */     JCheckBox[] delBoxes = null;
/*      */ 
/* 1021 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apUpdateDatabaseDesign", this.m_ctx), true, true);
/*      */ 
/* 1023 */     JPanel mainPanel = helper.initStandard(null, null, 2, true, DialogHelpTable.getHelpPage("UpdatingDocInfoFields"));
/*      */ 
/* 1026 */     if (this.m_addFields.size() > 0)
/*      */     {
/* 1028 */       JPanel subPanel = createFieldListSubPanel(mainPanel, LocaleResources.getString("apInfoFieldsToBeAdded", this.m_ctx));
/*      */ 
/* 1030 */       addFieldList(subPanel, this.m_addFields, false);
/*      */     }
/* 1032 */     if (this.m_changeFields.size() > 0)
/*      */     {
/* 1034 */       JPanel subPanel = createFieldListSubPanel(mainPanel, LocaleResources.getString("apInfoFieldsToBeModified", this.m_ctx));
/*      */ 
/* 1036 */       addFieldList(subPanel, this.m_changeFields, false);
/*      */     }
/* 1038 */     if (this.m_deleteFields.size() > 0)
/*      */     {
/* 1040 */       JPanel subPanel = createFieldListSubPanel(mainPanel, LocaleResources.getString("apInfoFieldsToBeDeleted", this.m_ctx));
/*      */ 
/* 1042 */       delBoxes = addFieldList(subPanel, this.m_deleteFields, true);
/*      */     }
/*      */ 
/* 1046 */     if (helper.prompt() == 1)
/*      */     {
/* 1048 */       if (delBoxes != null)
/*      */       {
/* 1050 */         for (int i = 0; i < delBoxes.length; ++i)
/*      */         {
/* 1052 */           if (!delBoxes[i].isSelected())
/*      */             continue;
/* 1054 */           delFields.addElement(this.m_deleteFields.elementAt(i));
/*      */         }
/*      */       }
/*      */ 
/* 1058 */       return true;
/*      */     }
/*      */ 
/* 1061 */     return false;
/*      */   }
/*      */ 
/*      */   protected boolean promptUser(DataBinder binder)
/*      */   {
/* 1066 */     String str = binder.getLocal("BouncedFields");
/* 1067 */     Vector infos = StringUtils.parseArray(str, ':', '*');
/* 1068 */     int size = infos.size();
/* 1069 */     IdcMessage msg = IdcMessageFactory.lc("apFieldsChangedTypeWarning", new Object[0]);
/* 1070 */     IdcMessage tmp = msg;
/* 1071 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1073 */       String infoStr = (String)infos.elementAt(i);
/* 1074 */       Vector info = StringUtils.parseArray(infoStr, ',', '^');
/*      */ 
/* 1076 */       if (i > 0)
/*      */       {
/* 1078 */         tmp.m_prior = IdcMessageFactory.lc();
/* 1079 */         tmp.m_prior.m_msgLocalized = "\n";
/* 1080 */         tmp = tmp.m_prior;
/*      */       }
/* 1082 */       String name = (String)info.elementAt(0);
/* 1083 */       String type = (String)info.elementAt(1);
/* 1084 */       String orgType = (String)info.elementAt(2);
/* 1085 */       tmp.m_prior = IdcMessageFactory.lc("apFieldChangedType", new Object[] { name, orgType, type });
/* 1086 */       tmp = tmp.m_prior;
/*      */     }
/*      */ 
/* 1089 */     int result = MessageBox.doMessage(this.m_systemInterface, msg, 4);
/* 1090 */     return result == 2;
/*      */   }
/*      */ 
/*      */   protected JPanel createFieldListSubPanel(JPanel panel, String title)
/*      */   {
/* 1095 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 1096 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 1097 */     JPanel fieldList = new PanePanel();
/* 1098 */     this.m_helper.addComponent(panel, fieldList);
/*      */ 
/* 1100 */     this.m_helper.makePanelGridBag(fieldList, 0);
/* 1101 */     this.m_helper.addPanelTitle(fieldList, title);
/* 1102 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(10);
/* 1103 */     return fieldList;
/*      */   }
/*      */ 
/*      */   protected JCheckBox[] addFieldList(JPanel panel, Vector v, boolean isSelectable)
/*      */   {
/* 1108 */     int n = v.size();
/* 1109 */     JCheckBox[] boxes = null;
/* 1110 */     if (isSelectable)
/*      */     {
/* 1112 */       boxes = new JCheckBox[n];
/*      */     }
/* 1114 */     for (int i = 0; i < n; ++i)
/*      */     {
/* 1116 */       FieldInfo fi = (FieldInfo)v.elementAt(i);
/* 1117 */       Component comp = null;
/* 1118 */       String name = MetaFieldGui.createDisplayName(fi.m_name);
/* 1119 */       if (isSelectable)
/*      */       {
/* 1121 */         JCheckBox box = new JCheckBox(name);
/* 1122 */         box.setBackground(getBackground());
/* 1123 */         box.setSelected(true);
/* 1124 */         boxes[i] = box;
/* 1125 */         comp = box;
/*      */       }
/*      */       else
/*      */       {
/* 1129 */         comp = new CustomLabel(name);
/*      */       }
/* 1131 */       this.m_helper.addComponent(panel, comp);
/*      */     }
/* 1133 */     return boxes;
/*      */   }
/*      */ 
/*      */   public boolean canExit()
/*      */   {
/* 1139 */     if (((this.m_changeFields != null) && (this.m_changeFields.size() > 0)) || ((this.m_addFields != null) && (this.m_addFields.size() > 0)) || ((this.m_deleteFields != null) && (this.m_deleteFields.size() > 0)))
/*      */     {
/* 1147 */       return MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apDatabaseDesignIsInvalid", new Object[0]), 4) == 2;
/*      */     }
/*      */ 
/* 1152 */     return true;
/*      */   }
/*      */ 
/*      */   public void refreshView()
/*      */     throws ServiceException
/*      */   {
/* 1159 */     refreshMetafieldList();
/*      */   }
/*      */ 
/*      */   public void actionPerformed(ActionEvent event)
/*      */   {
/* 1167 */     this.m_helper.handleActionPerformed(event, this, this.m_systemInterface);
/*      */   }
/*      */ 
/*      */   public void deleteItem() throws ServiceException
/*      */   {
/* 1172 */     int index = this.m_metaList.getSelectedIndex();
/* 1173 */     if (index < 0)
/*      */     {
/* 1175 */       return;
/*      */     }
/* 1177 */     Properties data = this.m_metaList.getDataAt(index);
/* 1178 */     if (data == null)
/*      */     {
/* 1180 */       return;
/*      */     }
/* 1182 */     DataBinder binder = new DataBinder();
/* 1183 */     binder.setLocalData(data);
/*      */     String fieldName;
/*      */     String fieldName;
/* 1186 */     if (!isApplicationFields())
/*      */     {
/* 1189 */       fieldName = MetaFieldGui.getDispFieldName(data);
/*      */     }
/*      */     else
/*      */     {
/* 1193 */       fieldName = binder.getLocal(this.m_idField);
/*      */     }
/*      */     String action;
/* 1196 */     if (isDocMetaFields())
/*      */     {
/* 1198 */       action = "DEL_METADEF";
/*      */     }
/*      */     else
/*      */     {
/*      */       String action;
/* 1200 */       if (isApplicationFields())
/*      */       {
/* 1202 */         action = "DELETE_SCHEMA_FIELD";
/*      */       }
/*      */       else
/*      */       {
/* 1207 */         throw new ServiceException("!$Invalid state in DocInfoPanel");
/*      */       }
/*      */     }
/*      */     String action;
/* 1209 */     IdcMessage msg = IdcMessageFactory.lc("apVerifyFieldDelete", new Object[] { fieldName });
/*      */ 
/* 1211 */     if (MessageBox.doMessage(this.m_systemInterface, msg, 4) != 2) {
/*      */       return;
/*      */     }
/*      */     try
/*      */     {
/* 1216 */       DataResultSet set = SharedObjects.getTable("SchemaFieldConfig");
/* 1217 */       if (set != null)
/*      */       {
/* 1219 */         binder.addResultSet("SchemaFieldConfig", SharedObjects.getTable("SchemaFieldConfig"));
/*      */       }
/*      */ 
/* 1222 */       executeService(action, binder, false);
/* 1223 */       refreshMetafieldList(null);
/*      */     }
/*      */     catch (ServiceException exp)
/*      */     {
/* 1227 */       reportError(exp);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void configureSearch()
/*      */   {
/*      */     try
/*      */     {
/* 1236 */       promptSearchDesignChange();
/* 1237 */       refreshMetafieldList();
/*      */     }
/*      */     catch (ServiceException exp)
/*      */     {
/* 1241 */       reportError(exp, IdcMessageFactory.lc("apErrorUpdatingSearchDesign", new Object[0]));
/*      */     }
/*      */   }
/*      */ 
/*      */   public void moveUp()
/*      */   {
/* 1247 */     moveRow(true);
/*      */   }
/*      */ 
/*      */   public void moveDown()
/*      */   {
/* 1252 */     moveRow(false);
/*      */   }
/*      */ 
/*      */   protected void moveRow(boolean isUp)
/*      */   {
/* 1257 */     int index = this.m_metaList.getSelectedIndex();
/* 1258 */     if (index < 0)
/*      */     {
/* 1260 */       return;
/*      */     }
/* 1262 */     String name = this.m_metaList.getSelectedObj();
/*      */ 
/* 1264 */     DataBinder binder = new DataBinder();
/* 1265 */     binder.putLocal("schFieldName", name);
/* 1266 */     binder.putLocal("isMoveUp", "" + isUp);
/* 1267 */     binder.putLocal("isMove", "true");
/* 1268 */     binder.putLocal("schFieldTarget", this.m_targetName);
/*      */     try
/*      */     {
/* 1272 */       executeService("EDIT_SCHEMA_FIELD", binder, false);
/* 1273 */       refreshMetafieldList(name);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1277 */       reportError(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateDatabase()
/*      */   {
/*      */     try
/*      */     {
/* 1286 */       Vector fieldsToDelete = new IdcVector();
/* 1287 */       if (!promptDesignChanged(fieldsToDelete))
/*      */       {
/* 1289 */         return;
/*      */       }
/*      */ 
/* 1293 */       Vector delList = new IdcVector();
/* 1294 */       for (int i = 0; i < fieldsToDelete.size(); ++i)
/*      */       {
/* 1296 */         FieldInfo fi = (FieldInfo)fieldsToDelete.elementAt(i);
/* 1297 */         delList.addElement(fi.m_name);
/*      */       }
/* 1299 */       String delStr = StringUtils.createString(delList, ',', ',');
/*      */ 
/* 1304 */       DataBinder binder = new DataBinder();
/* 1305 */       binder.putLocal("isBounceOnChange", "1");
/* 1306 */       binder.putLocal("MetaFieldsToDelete", delStr);
/* 1307 */       executeService("UPDATE_META_TABLE", binder, false);
/*      */ 
/* 1309 */       boolean isBounced = StringUtils.convertToBool(binder.getLocal("isBounced"), false);
/*      */ 
/* 1311 */       if ((isBounced) && 
/* 1313 */         (promptUser(binder)))
/*      */       {
/* 1316 */         binder = new DataBinder();
/* 1317 */         binder.putLocal("MetaFieldsToDelete", delStr);
/* 1318 */         executeService("UPDATE_META_TABLE", binder, false);
/*      */       }
/*      */ 
/* 1323 */       refreshMetafieldList();
/*      */     }
/*      */     catch (ServiceException excep)
/*      */     {
/* 1327 */       reportError(excep, IdcMessageFactory.lc("apErrorUpdatingDatabaseDesign", new Object[0]));
/*      */     }
/*      */   }
/*      */ 
/*      */   public void itemStateChanged(ItemEvent e)
/*      */   {
/* 1337 */     String field = this.m_metaList.getSelectedObj();
/* 1338 */     if (field != null)
/*      */     {
/* 1340 */       SchemaFieldConfig fieldConfig = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/* 1341 */       SchemaFieldData fieldData = (SchemaFieldData)fieldConfig.getData(field);
/*      */ 
/* 1344 */       boolean isEnabled = false;
/* 1345 */       if (fieldData != null)
/*      */       {
/* 1347 */         isEnabled = StringUtils.convertToBool(fieldData.get("dIsOptionList"), false);
/*      */       }
/* 1349 */       this.m_editTreeButton.setEnabled(isEnabled);
/*      */ 
/* 1351 */       isEnabled = true;
/* 1352 */       if (field.equals("xIdcProfile"))
/*      */       {
/* 1354 */         isEnabled = false;
/*      */       }
/* 1356 */       this.m_editFieldButton.setEnabled(isEnabled);
/* 1357 */       this.m_deleteFieldButton.setEnabled(isEnabled);
/*      */     }
/*      */     else
/*      */     {
/* 1361 */       this.m_editTreeButton.setEnabled(false);
/* 1362 */       this.m_editFieldButton.setEnabled(false);
/* 1363 */       this.m_deleteFieldButton.setEnabled(false);
/*      */     }
/*      */ 
/* 1366 */     enableDisable();
/*      */   }
/*      */ 
/*      */   protected void enableDisable()
/*      */   {
/* 1372 */     boolean isUpEnabled = false;
/* 1373 */     boolean isDownEnabled = false;
/* 1374 */     String name = this.m_metaList.getSelectedObj();
/*      */ 
/* 1376 */     DataResultSet drset = this.m_metaFields;
/* 1377 */     if ((drset != null) && (name != null))
/*      */     {
/* 1379 */       Vector row = this.m_metaFields.findRow(0, name);
/* 1380 */       if (row != null)
/*      */       {
/* 1382 */         int index = this.m_metaFields.getCurrentRow();
/* 1383 */         int numRows = drset.getNumRows();
/* 1384 */         if (index > 0)
/*      */         {
/* 1386 */           isUpEnabled = true;
/*      */         }
/* 1388 */         if ((numRows > 0) && (index < numRows - 1))
/*      */         {
/* 1390 */           isDownEnabled = true;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1395 */     JButton btn = (JButton)this.m_btnMap.get("moveUp");
/* 1396 */     if (btn != null)
/*      */     {
/* 1398 */       btn.setEnabled(isUpEnabled);
/*      */     }
/*      */ 
/* 1401 */     btn = (JButton)this.m_btnMap.get("moveDown");
/* 1402 */     if (btn == null)
/*      */       return;
/* 1404 */     btn.setEnabled(isDownEnabled);
/*      */   }
/*      */ 
/*      */   protected void loadPanelInformation()
/*      */     throws DataException
/*      */   {
/*      */   }
/*      */ 
/*      */   public void executeService(String action, DataBinder data, boolean isRefresh)
/*      */     throws ServiceException
/*      */   {
/* 1421 */     AppLauncher.executeService(action, data);
/*      */   }
/*      */ 
/*      */   public UserData getUserData()
/*      */   {
/* 1427 */     return AppLauncher.getUserData();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1432 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97523 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.DocInfoPanel
 * JD-Core Version:    0.5.4
 */