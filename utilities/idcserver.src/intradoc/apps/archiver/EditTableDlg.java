/*      */ package intradoc.apps.archiver;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemInterface;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.gui.ComponentBinder;
/*      */ import intradoc.gui.CustomCheckbox;
/*      */ import intradoc.gui.CustomChoice;
/*      */ import intradoc.gui.CustomLabel;
/*      */ import intradoc.gui.CustomPanel;
/*      */ import intradoc.gui.DialogCallback;
/*      */ import intradoc.gui.DialogHelper;
/*      */ import intradoc.gui.DynamicComponentExchange;
/*      */ import intradoc.gui.GridBagHelper;
/*      */ import intradoc.gui.GuiStyles;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.gui.PanePanel;
/*      */ import intradoc.gui.iwt.ComboChoice;
/*      */ import intradoc.gui.iwt.MultiComboBox;
/*      */ import intradoc.shared.ExportQueryData;
/*      */ import intradoc.shared.SharedContext;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.awt.BorderLayout;
/*      */ import java.awt.Component;
/*      */ import java.awt.FlowLayout;
/*      */ import java.awt.GridBagConstraints;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ItemEvent;
/*      */ import java.awt.event.ItemListener;
/*      */ import java.awt.event.TextEvent;
/*      */ import java.awt.event.TextListener;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.swing.JCheckBox;
/*      */ import javax.swing.JComboBox;
/*      */ import javax.swing.JPanel;
/*      */ import javax.swing.JTextField;
/*      */ 
/*      */ public class EditTableDlg
/*      */   implements ComponentBinder, ItemListener
/*      */ {
/*      */   protected SystemInterface m_sysInterface;
/*      */   protected CollectionContext m_context;
/*      */   protected ExecutionContext m_cxt;
/*      */   protected DialogHelper m_helper;
/*      */   protected boolean m_isAdd;
/*      */   protected boolean m_isExport;
/*   80 */   protected Vector m_existingTables = null;
/*   81 */   protected String m_currentTable = null;
/*   82 */   protected ResultSet m_archivableTables = null;
/*   83 */   protected Vector m_archivableTableNames = null;
/*   84 */   protected String m_curCreateTS = null;
/*   85 */   protected String m_curModifiedTS = null;
/*   86 */   protected Vector m_dateColumnList = null;
/*   87 */   protected Vector m_textColumnList = null;
/*      */ 
/*   89 */   protected boolean m_updateTableName = true;
/*   90 */   protected Hashtable m_cachedTableColumns = new Hashtable();
/*      */ 
/*   92 */   protected boolean m_isInitFailed = false;
/*   93 */   protected boolean m_isFinalCallback = false;
/*   94 */   protected String m_notArchivableTables = null;
/*   95 */   protected String m_overrideTables = null;
/*   96 */   protected String m_tablesNotInDB = null;
/*   97 */   protected String m_tableListPrefix = "aExportTable";
/*   98 */   protected String[] m_valueKeys = null;
/*      */ 
/*  101 */   protected JCheckBox m_allowDeleteParentRows = null;
/*  102 */   protected JCheckBox m_removeWhenNoChild = null;
/*  103 */   protected JCheckBox m_syncChildCheckBox = null;
/*  104 */   protected JCheckBox m_allowDeleteRows = null;
/*  105 */   protected JCheckBox m_useParentTS = null;
/*  106 */   protected JCheckBox m_isCreateNewFieldCheckbox = null;
/*  107 */   protected JCheckBox m_useSourceIDCheckbox = null;
/*      */ 
/*  110 */   protected JTextField m_relationTextField = null;
/*  111 */   protected JComboBox m_relations = null;
/*      */ 
/*  114 */   protected boolean m_isInitialized = false;
/*  115 */   protected boolean m_isAlive = true;
/*  116 */   protected Thread m_bgThread = null;
/*  117 */   protected Thread m_timer = null;
/*  118 */   protected String m_lockObject = "lockObject";
/*      */ 
/*  120 */   protected long m_startTime = -1L;
/*  121 */   protected boolean m_isActive = false;
/*  122 */   protected boolean m_abort = false;
/*      */ 
/*  124 */   protected boolean m_isInItemStateChangedEvent = false;
/*  125 */   protected boolean m_isExchangingComponentValue = false;
/*      */ 
/*      */   public EditTableDlg(SystemInterface sys, String title, CollectionContext cxt, ExecutionContext execCxt)
/*      */   {
/*  129 */     this.m_sysInterface = sys;
/*  130 */     this.m_context = cxt;
/*  131 */     this.m_helper = new DialogHelper(sys, title, true);
/*      */   }
/*      */ 
/*      */   public void init(String table, Properties props, boolean isExport, boolean isAdd)
/*      */   {
/*  136 */     this.m_helper.m_props = props;
/*  137 */     this.m_isAdd = isAdd;
/*  138 */     this.m_isExport = isExport;
/*  139 */     if (!isExport)
/*      */     {
/*  141 */       this.m_tableListPrefix = "aImportTable";
/*      */     }
/*      */ 
/*  144 */     String[][] keys = ImportTableList.KEYS;
/*  145 */     this.m_valueKeys = new String[keys.length];
/*  146 */     for (int i = 0; i < keys.length; ++i)
/*      */     {
/*  148 */       this.m_valueKeys[i] = keys[i][0];
/*      */     }
/*      */ 
/*  151 */     if ((isExport) && (isAdd))
/*      */     {
/*  153 */       String existingTables = props.getProperty("aExportTables");
/*  154 */       if (existingTables == null)
/*      */       {
/*  156 */         props.put("aExportTables", "");
/*      */       }
/*  158 */       this.m_existingTables = StringUtils.parseArray(existingTables, ',', ',');
/*      */     }
/*      */     else
/*      */     {
/*  162 */       if ((table == null) || (table.length() == 0))
/*      */       {
/*  164 */         this.m_isInitFailed = true;
/*      */       }
/*  166 */       this.m_currentTable = table;
/*  167 */       parseOptions();
/*      */       try
/*      */       {
/*  170 */         String dateTable = table;
/*  171 */         if (StringUtils.convertToBool(props.getProperty("aUseParentTS"), false))
/*      */         {
/*  173 */           dateTable = props.getProperty("aParentTables");
/*      */         }
/*  175 */         this.m_dateColumnList = initColumnList(dateTable, "Date");
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/*  179 */         MessageBox.reportError(this.m_sysInterface, ignore);
/*      */       }
/*      */     }
/*      */ 
/*  183 */     DialogCallback callback = new DialogCallback()
/*      */     {
/*      */       public boolean handleDialogEvent(ActionEvent event)
/*      */       {
/*  188 */         EditTableDlg.this.m_isFinalCallback = true;
/*  189 */         boolean result = EditTableDlg.this.m_helper.retrieveComponentValues();
/*  190 */         if ((result) && (EditTableDlg.this.m_isExport))
/*      */         {
/*  192 */           if ((EditTableDlg.this.m_timer != null) && (EditTableDlg.this.m_timer.isAlive()))
/*      */           {
/*  194 */             synchronized (EditTableDlg.this.m_timer)
/*      */             {
/*  196 */               EditTableDlg.this.m_timer.notify();
/*      */             }
/*      */           }
/*  199 */           EditTableDlg.this.stopBackgroundThread();
/*      */ 
/*  201 */           if (EditTableDlg.this.m_isAdd)
/*      */           {
/*  203 */             result = EditTableDlg.this.isCurrentTableExist();
/*  204 */             if (!result)
/*      */             {
/*  206 */               this.m_errorMessage = IdcMessageFactory.lc("apArchiveTableNotExistInDatabase", new Object[] { EditTableDlg.this.m_currentTable });
/*      */ 
/*  210 */               EditTableDlg.this.startBackgroundThread();
/*      */             }
/*      */           }
/*      */         }
/*  214 */         EditTableDlg.this.m_isFinalCallback = result;
/*  215 */         return result;
/*      */       }
/*      */     };
/*  219 */     DialogCallback cancelCallback = new DialogCallback()
/*      */     {
/*      */       public boolean handleDialogEvent(ActionEvent event)
/*      */       {
/*  224 */         if (EditTableDlg.this.m_isExport)
/*      */         {
/*  226 */           EditTableDlg.this.stopBackgroundThread();
/*      */         }
/*  228 */         return true;
/*      */       }
/*      */     };
/*  232 */     JPanel main = this.m_helper.initStandard(this, callback, 3, false, null);
/*  233 */     this.m_helper.m_cancelCallback = cancelCallback;
/*      */ 
/*  235 */     JPanel tablePanel = initTableUI();
/*  236 */     JPanel optionPanel = initOptionUI();
/*      */ 
/*  238 */     main.setLayout(new BorderLayout());
/*  239 */     main.add("North", tablePanel);
/*  240 */     main.add("South", optionPanel);
/*      */ 
/*  242 */     if (!isExport) {
/*      */       return;
/*      */     }
/*  245 */     startBackgroundThread();
/*      */   }
/*      */ 
/*      */   public int prompt()
/*      */   {
/*  251 */     if (this.m_isInitFailed)
/*      */     {
/*  253 */       return 6;
/*      */     }
/*  255 */     return this.m_helper.prompt();
/*      */   }
/*      */ 
/*      */   protected void parseOptions()
/*      */   {
/*  260 */     Properties props = this.m_helper.m_props;
/*  261 */     String options = props.getProperty(this.m_tableListPrefix + this.m_currentTable);
/*      */ 
/*  263 */     if ((options == null) || (options.length() == 0))
/*      */     {
/*  265 */       return;
/*      */     }
/*  267 */     ExportQueryData queryData = new ExportQueryData();
/*  268 */     queryData.parse(options);
/*  269 */     for (int i = 0; i < this.m_valueKeys.length; ++i)
/*      */     {
/*  271 */       String value = queryData.getQueryProp(this.m_valueKeys[i]);
/*  272 */       if (value == null)
/*      */         continue;
/*  274 */       props.put(this.m_valueKeys[i], value);
/*      */     }
/*      */   }
/*      */ 
/*      */   public JPanel initTableUI()
/*      */   {
/*  282 */     CustomPanel panel = new CustomPanel();
/*      */ 
/*  284 */     this.m_helper.makePanelGridBag(panel, 0);
/*  285 */     Vector tables = getArchivableTables();
/*      */ 
/*  288 */     if (this.m_isAdd)
/*      */     {
/*  290 */       Component comp = null;
/*  291 */       if (this.m_isExport)
/*      */       {
/*  293 */         comp = new ComboChoice();
/*  294 */         comp.setName("aTableName");
/*  295 */         ((ComboChoice)comp).initChoiceList(tables);
/*  296 */         TextListener text = new TextListener()
/*      */         {
/*      */           public void textValueChanged(TextEvent event)
/*      */           {
/*  300 */             String table = null;
/*  301 */             Component component = (Component)event.getSource();
/*  302 */             if (!component.isVisible())
/*      */               return;
/*  304 */             table = ((MultiComboBox)component).getText();
/*  305 */             if (table.equalsIgnoreCase(EditTableDlg.this.m_currentTable))
/*      */               return;
/*  307 */             EditTableDlg.this.resetOptions(false, table);
/*      */           }
/*      */         };
/*  312 */         ((ComboChoice)comp).addTextListener(text);
/*      */       }
/*  314 */       this.m_helper.addLabelFieldPair(panel, this.m_sysInterface.localizeCaption("apArchiverLabelTableName"), comp, "aTableName");
/*      */     }
/*      */     else
/*      */     {
/*  320 */       this.m_helper.addLabelDisplayPair(panel, this.m_sysInterface.localizeCaption("apArchiverLabelTableName"), 20, "aTableName");
/*      */     }
/*      */ 
/*  325 */     this.m_useParentTS = getCheckbox("apArchiverLabelUseParentTS", "aUseParentTS");
/*  326 */     this.m_useParentTS.addItemListener(this);
/*  327 */     String parentTables = this.m_helper.m_props.getProperty("aParentTables");
/*  328 */     if ((parentTables == null) || (parentTables.length() == 0))
/*      */     {
/*  330 */       this.m_useParentTS.setEnabled(false);
/*      */     }
/*  332 */     this.m_relationTextField = new JTextField(25);
/*  333 */     this.m_relations = getCustomChoice(150);
/*  334 */     this.m_relations.addItemListener(this);
/*  335 */     if (this.m_isExport)
/*      */     {
/*  337 */       this.m_helper.addLabelFieldPair(panel, this.m_sysInterface.localizeCaption("apArchiverLabelUseParentTSLabel"), this.m_useParentTS, "aUseParentTS");
/*      */ 
/*  340 */       CustomChoice cc = getCustomChoice(200);
/*  341 */       this.m_helper.addLabelFieldPair(panel, this.m_sysInterface.localizeCaption("apArchiverLabelCreateTS"), cc, "aCreateTimeStamp");
/*      */ 
/*  344 */       cc = getCustomChoice(200);
/*  345 */       this.m_helper.addLabelFieldPair(panel, this.m_sysInterface.localizeCaption("apArchiverLabelModifyTS"), cc, "aModifiedTimeStamp");
/*      */ 
/*  349 */       this.m_helper.addLabelFieldPairEx(panel, this.m_sysInterface.localizeCaption("apArchiverLabelParentTable"), this.m_relations, "aParentTables", true);
/*      */ 
/*  352 */       this.m_helper.addLabelFieldPairEx(panel, this.m_sysInterface.localizeCaption("apArchiverLabelTableRelation"), this.m_relationTextField, "aTableRelations", true);
/*      */     }
/*      */     else
/*      */     {
/*  358 */       this.m_helper.addLabelFieldPair(panel, this.m_sysInterface.localizeCaption("apArchiverLabelUseParentTSLabel"), this.m_useParentTS, "aUseParentTS");
/*      */ 
/*  361 */       this.m_useParentTS.setEnabled(false);
/*  362 */       this.m_helper.addLabelDisplayPairEx(panel, this.m_sysInterface.localizeCaption("apArchiverLabelCreateTS"), 0, "aCreateTimeStamp", true);
/*      */ 
/*  365 */       this.m_helper.addLabelDisplayPairEx(panel, this.m_sysInterface.localizeCaption("apArchiverLabelModifyTS"), 0, "aModifiedTimeStamp", true);
/*      */ 
/*  369 */       this.m_helper.addLabelDisplayPairEx(panel, this.m_sysInterface.localizeCaption("apArchiverLabelParentTable"), 0, "aParentTables", true);
/*      */ 
/*  372 */       this.m_helper.addLabelDisplayPairEx(panel, this.m_sysInterface.localizeCaption("apArchiverLabelTableRelation"), 0, "aTableRelations", true);
/*      */     }
/*      */ 
/*  377 */     return panel;
/*      */   }
/*      */ 
/*      */   protected CustomChoice getCustomChoice(int minWidth)
/*      */   {
/*  382 */     CustomChoice cc = new CustomChoice();
/*  383 */     if (minWidth > 0)
/*      */     {
/*  385 */       cc.setMinWidth(minWidth);
/*      */     }
/*  387 */     return cc;
/*      */   }
/*      */ 
/*      */   protected String findTableName(String name)
/*      */   {
/*  392 */     String table = null;
/*  393 */     Vector s = getArchivableTables();
/*      */ 
/*  395 */     int size = s.size();
/*  396 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  398 */       String tmp = (String)s.elementAt(i);
/*  399 */       if (!name.equalsIgnoreCase(tmp))
/*      */         continue;
/*  401 */       table = tmp;
/*  402 */       break;
/*      */     }
/*      */ 
/*  405 */     if (table == null)
/*      */     {
/*      */       try
/*      */       {
/*  409 */         ResultSet rset = getColumnList(name);
/*  410 */         rset.first();
/*  411 */         int index = ResultSetUtils.getIndexMustExist(rset, "tableName");
/*  412 */         table = rset.getStringValue(index);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*      */       }
/*      */     }
/*      */ 
/*  419 */     return table;
/*      */   }
/*      */ 
/*      */   public JPanel initOptionUI()
/*      */   {
/*  424 */     JPanel panel = new CustomPanel();
/*  425 */     this.m_helper.makePanelGridBag(panel, 2);
/*  426 */     JCheckBox newFieldCheckBox = getCheckbox("apArchiverLabelCreateField");
/*  427 */     newFieldCheckBox.setEnabled(false);
/*  428 */     this.m_isCreateNewFieldCheckbox = newFieldCheckBox;
/*  429 */     addOptionComponent(panel, "apArchiverLabelCreateField", "aIsCreateNewField", newFieldCheckBox);
/*  430 */     this.m_helper.addLabelDisplayPairEx(panel, " ", 20, "", false);
/*  431 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/*  432 */     JCheckBox sIDCheckBox = getCheckbox("apArchiverLabelUseSourceID", "aUseSourceID");
/*  433 */     sIDCheckBox.setEnabled(false);
/*  434 */     this.m_useSourceIDCheckbox = sIDCheckBox;
/*      */ 
/*  436 */     addOptionComponent(panel, "apArchiverLabelUseSourceID", "aUseSourceID", sIDCheckBox);
/*  437 */     this.m_allowDeleteRows = getCheckbox("apArchiverLabelRpcdDeleted", "aIsReplicateDeletedRows");
/*  438 */     this.m_allowDeleteRows.setEnabled(false);
/*  439 */     this.m_allowDeleteRows.addItemListener(this);
/*  440 */     addOptionComponent(panel, "apArchiverLabelRpcdDeleted", "aIsReplicateDeletedRows", this.m_allowDeleteRows);
/*      */ 
/*  442 */     this.m_syncChildCheckBox = getCheckbox("apArchiverLabelRemoveChildrenOnImport", "aRemoveExistingChildren");
/*  443 */     addOptionComponent(panel, "apArchiverLabelRemoveChildrenOnImport", "aRemoveExistingChildren", this.m_syncChildCheckBox);
/*  444 */     this.m_syncChildCheckBox.setEnabled(false);
/*      */ 
/*  446 */     this.m_allowDeleteParentRows = getCheckbox("apArchiverLabelAllowDeleteParentRows", "aAllowDeleteParentRows");
/*      */ 
/*  448 */     this.m_allowDeleteParentRows.addItemListener(this);
/*  449 */     addOptionComponent(panel, "apArchiverLabelAllowDeleteParentRows", "aAllowDeleteParentRows", this.m_allowDeleteParentRows);
/*      */ 
/*  451 */     this.m_removeWhenNoChild = getCheckbox("apArchiverLabelRemoveWhenNoChild", "aDeleteParentOnlyWhenNoChild");
/*      */ 
/*  453 */     addOptionComponent(panel, "apArchiverLabelRemoveWhenNoChild", "aDeleteParentOnlyWhenNoChild", this.m_removeWhenNoChild);
/*      */ 
/*  455 */     this.m_allowDeleteParentRows.setEnabled(false);
/*  456 */     this.m_removeWhenNoChild.setEnabled(false);
/*      */ 
/*  458 */     return panel;
/*      */   }
/*      */ 
/*      */   protected void addOptionComponent(JPanel panel, String label, String name, JCheckBox checkbox)
/*      */   {
/*  463 */     if (this.m_isExport)
/*      */     {
/*  465 */       this.m_helper.addExchangeComponent(panel, checkbox, name);
/*      */     }
/*      */     else
/*      */     {
/*  469 */       label = this.m_sysInterface.localizeCaption(label);
/*  470 */       String value = this.m_helper.m_props.getProperty(name);
/*  471 */       boolean isTrue = StringUtils.convertToBool(value, false);
/*  472 */       if (isTrue)
/*      */       {
/*  474 */         value = "apLabelYes";
/*      */       }
/*      */       else
/*      */       {
/*  478 */         value = "apLabelNo";
/*      */       }
/*  480 */       JPanel subPanel = new PanePanel();
/*  481 */       subPanel.setLayout(new FlowLayout());
/*      */ 
/*  483 */       value = this.m_sysInterface.getString(value);
/*  484 */       GridBagConstraints gc = this.m_helper.m_gridHelper.m_gc;
/*  485 */       gc.weightx = 0.0D;
/*  486 */       gc.fill = 0;
/*  487 */       CustomLabel labelComp = new CustomLabel(label);
/*  488 */       labelComp.setFont(GuiStyles.getCustomFont(1));
/*  489 */       subPanel.add(labelComp);
/*  490 */       subPanel.add(new CustomLabel(value));
/*  491 */       this.m_helper.addComponent(panel, subPanel);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected JCheckBox getCheckbox(String label)
/*      */   {
/*  497 */     return getCheckbox(label, null);
/*      */   }
/*      */ 
/*      */   protected JCheckBox getCheckbox(String label, String name)
/*      */   {
/*  502 */     label = LocaleResources.getString(label, this.m_cxt);
/*  503 */     JCheckBox box = new CustomCheckbox(label);
/*  504 */     if ((name != null) && (name.length() > 0))
/*      */     {
/*  506 */       box.setName(name);
/*      */     }
/*  508 */     return box;
/*      */   }
/*      */ 
/*      */   protected void enableDisableOptions()
/*      */   {
/*  513 */     if (this.m_isExport)
/*      */     {
/*  515 */       boolean allowOptions = false;
/*  516 */       if ((this.m_currentTable != null) && (this.m_currentTable.trim().length() > 0))
/*      */       {
/*  518 */         allowOptions = true;
/*      */       }
/*  520 */       this.m_allowDeleteRows.setEnabled(allowOptions);
/*  521 */       this.m_isCreateNewFieldCheckbox.setEnabled(allowOptions);
/*  522 */       this.m_useSourceIDCheckbox.setEnabled(allowOptions);
/*      */     }
/*      */ 
/*  525 */     boolean allowDelete = StringUtils.convertToBool(this.m_helper.m_props.getProperty("aIsReplicateDeletedRows"), false);
/*      */ 
/*  527 */     String parentTable = this.m_helper.m_props.getProperty("aParentTables");
/*  528 */     boolean enable = false;
/*  529 */     if ((parentTable != null) && (parentTable.length() > 0))
/*      */     {
/*  531 */       enable = true;
/*      */     }
/*  533 */     enable = (enable) && (allowDelete) && (this.m_isExport);
/*  534 */     this.m_allowDeleteParentRows.setEnabled(enable);
/*  535 */     enable = (enable) && (StringUtils.convertToBool(this.m_helper.m_props.getProperty("aAllowDeleteParentRows"), false));
/*      */ 
/*  537 */     this.m_removeWhenNoChild.setEnabled(enable);
/*      */ 
/*  539 */     enable = (StringUtils.convertToBool(this.m_helper.m_props.getProperty("aUseParentTS"), false)) && (allowDelete);
/*      */ 
/*  542 */     this.m_syncChildCheckBox.setEnabled(enable);
/*      */   }
/*      */ 
/*      */   public String createQueryString()
/*      */   {
/*  547 */     String query = this.m_helper.m_props.getProperty(this.m_tableListPrefix + this.m_currentTable);
/*  548 */     ExportQueryData queryData = new ExportQueryData();
/*  549 */     queryData.parse(query);
/*  550 */     prepareProps(this.m_helper.m_props);
/*  551 */     queryData.addExportQueryOptions(this.m_helper.m_props, this.m_valueKeys);
/*  552 */     return queryData.formatString();
/*      */   }
/*      */ 
/*      */   protected void prepareProps(Properties props)
/*      */   {
/*  558 */     String isRplcdDeleted = props.getProperty("aIsReplicateDeletedRow");
/*  559 */     if (!StringUtils.convertToBool(isRplcdDeleted, false))
/*      */       return;
/*  561 */     props.put("aDeletedTimeStamp", "dDeleteDate");
/*      */   }
/*      */ 
/*      */   protected Vector getArchivableTables()
/*      */   {
/*  567 */     if (this.m_archivableTableNames != null)
/*      */     {
/*  569 */       return this.m_archivableTableNames;
/*      */     }
/*      */     try
/*      */     {
/*  573 */       ResultSet drset = this.m_archivableTables;
/*  574 */       if (drset == null)
/*      */       {
/*  576 */         Properties props = new Properties();
/*  577 */         drset = getResultSet("GET_ARCHIVABLETABLES", "ArchivableTables", props, true);
/*      */       }
/*      */ 
/*  580 */       if (drset == null)
/*      */       {
/*  582 */         return null;
/*      */       }
/*  584 */       this.m_archivableTableNames = new IdcVector();
/*  585 */       int index = ResultSetUtils.getIndexMustExist(drset, "tableName");
/*  586 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/*  588 */         String table = drset.getStringValue(index);
/*  589 */         if ((this.m_existingTables != null) && (this.m_existingTables.contains(table))) {
/*      */           continue;
/*      */         }
/*      */ 
/*  593 */         this.m_archivableTableNames.addElement(drset.getStringValue(index));
/*      */       }
/*  595 */       this.m_archivableTables = drset;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  599 */       MessageBox.reportError(this.m_sysInterface, e);
/*  600 */       return null;
/*      */     }
/*  602 */     return this.m_archivableTableNames;
/*      */   }
/*      */ 
/*      */   protected ResultSet getResultSet(String service, String resultSetName, Properties props, boolean updateArchivableList)
/*      */     throws ServiceException, DataException
/*      */   {
/*  608 */     DataBinder binder = executeService(service, props);
/*  609 */     if (updateArchivableList)
/*      */     {
/*  611 */       this.m_notArchivableTables = binder.getLocal("NotArchivableTables");
/*  612 */       this.m_overrideTables = binder.getLocal("ArchiverOverrideTables");
/*      */     }
/*      */ 
/*  615 */     return binder.getResultSet(resultSetName);
/*      */   }
/*      */ 
/*      */   protected DataBinder executeService(String service, Properties props)
/*      */     throws ServiceException, DataException
/*      */   {
/*  621 */     DataBinder binder = new DataBinder();
/*  622 */     binder.setLocalData(props);
/*  623 */     SharedContext cxt = this.m_context.getSharedContext();
/*  624 */     cxt.executeService(service, binder, false);
/*  625 */     return binder;
/*      */   }
/*      */ 
/*      */   protected Vector initColumnList(String type)
/*      */     throws DataException, ServiceException
/*      */   {
/*  633 */     return initColumnList(this.m_currentTable, type);
/*      */   }
/*      */ 
/*      */   protected Vector initColumnList(String table, String type) throws DataException, ServiceException
/*      */   {
/*  638 */     if (table == null)
/*      */     {
/*  640 */       return null;
/*      */     }
/*  642 */     ResultSet rset = getColumnList(table);
/*      */ 
/*  644 */     String[][] columnList = ResultSetUtils.createFilteredStringTable(rset, new String[] { "columnType", "columnName" }, type);
/*      */ 
/*  646 */     return convertToVector(columnList, 0);
/*      */   }
/*      */ 
/*      */   protected ResultSet getColumnList(String tableName) throws DataException, ServiceException
/*      */   {
/*  651 */     ResultSet rset = (ResultSet)this.m_cachedTableColumns.get(tableName.toUpperCase());
/*      */ 
/*  653 */     if (rset == null)
/*      */     {
/*  655 */       Properties props = this.m_helper.m_props;
/*  656 */       props.put("tableNames", tableName);
/*  657 */       props.put("isSuppressDataException", "1");
/*  658 */       rset = getResultSet("GET_TABLECOLUMNLIST", "TableColumnList", props, false);
/*      */ 
/*  660 */       this.m_cachedTableColumns.put(tableName.toUpperCase(), rset);
/*      */     }
/*  662 */     return rset;
/*      */   }
/*      */ 
/*      */   public Vector convertToVector(String[][] columns, int index)
/*      */   {
/*  667 */     Vector tables = new IdcVector();
/*  668 */     for (int i = 0; i < columns.length; ++i)
/*      */     {
/*  670 */       tables.addElement(columns[i][index]);
/*      */     }
/*  672 */     return tables;
/*      */   }
/*      */ 
/*      */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*      */   {
/*  684 */     this.m_isExchangingComponentValue = true;
/*      */ 
/*  686 */     String name = exchange.m_compName;
/*  687 */     String noValueStr = this.m_sysInterface.getString("apChoiceNoValues");
/*  688 */     if (updateComponent)
/*      */     {
/*  690 */       String value = this.m_helper.m_props.getProperty(name);
/*  691 */       exchange.m_compValue = value;
/*  692 */       if ((name.equals("aCreateTimeStamp")) || (name.equals("aModifiedTimeStamp")))
/*      */       {
/*  694 */         if (this.m_isExport)
/*      */         {
/*  696 */           if ((this.m_dateColumnList != null) && (this.m_dateColumnList.size() > 0))
/*      */           {
/*  698 */             exchange.m_component.setEnabled(true);
/*      */ 
/*  700 */             String defaultValue = value;
/*  701 */             if (defaultValue == null)
/*      */             {
/*  703 */               defaultValue = getDefaultValue(this.m_currentTable, name.equals("aCreateTimeStamp"));
/*      */             }
/*      */ 
/*  706 */             if (name.equals("aCreateTimeStamp"))
/*      */             {
/*  708 */               exchange.m_compValue = this.m_curCreateTS;
/*      */             }
/*      */             else
/*      */             {
/*  712 */               exchange.m_compValue = this.m_curModifiedTS;
/*      */             }
/*  714 */             JComboBox choice = (JComboBox)exchange.m_component;
/*  715 */             populateChoice(choice, this.m_dateColumnList, defaultValue);
/*  716 */             exchange.m_compValue = defaultValue;
/*      */           }
/*      */           else
/*      */           {
/*  720 */             exchange.m_component.setEnabled(false);
/*  721 */             ((JComboBox)exchange.m_component).removeAllItems();
/*  722 */             ((JComboBox)exchange.m_component).addItem(noValueStr);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  727 */           if ((value == null) || (value.length() == 0))
/*      */           {
/*  729 */             value = noValueStr;
/*      */           }
/*  731 */           exchange.m_compValue = value;
/*      */         }
/*      */ 
/*      */       }
/*  735 */       else if (name.equals("aTableName"))
/*      */       {
/*  737 */         exchange.m_compValue = this.m_currentTable;
/*      */ 
/*  740 */         if (!this.m_updateTableName)
/*      */         {
/*  742 */           exchange.m_component = null;
/*      */         }
/*      */         else
/*      */         {
/*  746 */           this.m_updateTableName = false;
/*      */         }
/*      */       }
/*  749 */       else if (name.equals("aParentTables"))
/*      */       {
/*  751 */         if (!this.m_isExport)
/*      */         {
/*  753 */           if ((value == null) || (value.length() == 0))
/*      */           {
/*  755 */             exchange.m_compValue = noValueStr;
/*      */           }
/*      */         }
/*  758 */         else if ((this.m_currentTable != null) && (this.m_archivableTables != null))
/*      */         {
/*  760 */           String dependancies = null;
/*      */           try
/*      */           {
/*  763 */             dependancies = ResultSetUtils.findValue(this.m_archivableTables, "tableName", this.m_currentTable, "dependencies");
/*      */ 
/*  765 */             Vector choices = StringUtils.parseArray(dependancies, ',', ',');
/*  766 */             populateChoice((JComboBox)exchange.m_component, choices, value);
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/*  770 */             Report.trace("applet", null, e);
/*      */           }
/*  772 */           enableDisableOptions();
/*      */         }
/*      */         else
/*      */         {
/*  776 */           populateChoice((JComboBox)exchange.m_component, new IdcVector(), value);
/*      */         }
/*  778 */         if ((this.m_isExport) && (((value == null) || (value.length() == 0))))
/*      */         {
/*  780 */           this.m_useParentTS.setEnabled(false);
/*      */         }
/*      */       }
/*  783 */       else if ((name.equals("aTableRelations")) && 
/*  785 */         (((value == null) || (value.length() == 0))) && (!this.m_isExport))
/*      */       {
/*  787 */         exchange.m_compValue = noValueStr;
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  793 */       String value = exchange.m_compValue;
/*  794 */       if (value.equals(noValueStr))
/*      */       {
/*  796 */         value = "";
/*      */       }
/*  798 */       this.m_helper.m_props.put(exchange.m_compName, value);
/*      */     }
/*      */ 
/*  801 */     this.m_isExchangingComponentValue = false;
/*      */   }
/*      */ 
/*      */   public void populateChoice(JComboBox choice, Vector columnList, String defaultValue)
/*      */   {
/*  806 */     choice.removeAllItems();
/*  807 */     int size = columnList.size();
/*  808 */     if (size > 0)
/*      */     {
/*  810 */       choice.addItem("");
/*  811 */       choice.setEnabled(true);
/*      */     }
/*      */     else
/*      */     {
/*  815 */       choice.addItem(this.m_sysInterface.getString("apChoiceNoValues"));
/*  816 */       choice.setEnabled(false);
/*      */     }
/*  818 */     boolean hasDefaultValue = false;
/*  819 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  821 */       String value = (String)columnList.elementAt(i);
/*  822 */       if ((defaultValue != null) && (defaultValue.equalsIgnoreCase(value)))
/*      */       {
/*  824 */         hasDefaultValue = true;
/*      */       }
/*  826 */       choice.addItem(columnList.elementAt(i));
/*      */     }
/*      */ 
/*  829 */     if (!hasDefaultValue)
/*      */       return;
/*  831 */     choice.setSelectedItem(defaultValue);
/*      */   }
/*      */ 
/*      */   public String getDefaultValue(String table, boolean isCreate)
/*      */   {
/*  837 */     String colName = "modifiedColumn";
/*  838 */     if (isCreate)
/*      */     {
/*  840 */       colName = "createColumn";
/*      */     }
/*  842 */     String value = "tableName";
/*      */     try
/*      */     {
/*  845 */       value = ResultSetUtils.findValue(this.m_archivableTables, "tableName", table, colName);
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/*  849 */       if (SystemUtils.m_verbose)
/*      */       {
/*  851 */         Report.debug("system", null, ignore);
/*      */       }
/*      */     }
/*  854 */     return value;
/*      */   }
/*      */ 
/*      */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*      */   {
/*  862 */     if (!this.m_isFinalCallback)
/*      */     {
/*  864 */       return true;
/*      */     }
/*  866 */     String name = exchange.m_compName;
/*  867 */     String value = exchange.m_compValue;
/*  868 */     boolean pass = true;
/*  869 */     String msg = null;
/*  870 */     if (name.equals("aTableName"))
/*      */     {
/*  872 */       if (this.m_existingTables != null)
/*      */       {
/*  874 */         pass = !this.m_existingTables.contains(this.m_currentTable);
/*      */       }
/*  876 */       if ((pass) && (this.m_currentTable != null) && (this.m_currentTable.trim().length() > 0))
/*      */       {
/*  878 */         String tmpTableName = "|" + this.m_currentTable.toLowerCase() + "|";
/*  879 */         if (this.m_notArchivableTables != null)
/*      */         {
/*  881 */           pass = this.m_notArchivableTables.indexOf(tmpTableName) < 0;
/*      */         }
/*  883 */         if ((!pass) && (this.m_overrideTables != null))
/*      */         {
/*  885 */           pass = this.m_overrideTables.indexOf(tmpTableName) >= 0;
/*      */         }
/*  887 */         if (!pass)
/*      */         {
/*  889 */           msg = "apArchiveTableNotArchivable";
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  894 */         if ((this.m_currentTable == null) || (this.m_currentTable.trim().length() == 0))
/*      */         {
/*  896 */           msg = "apArchiveExportTableNotDefined";
/*      */         }
/*      */         else
/*      */         {
/*  900 */           msg = "apArchiveTableExistsInArchive";
/*      */         }
/*  902 */         pass = false;
/*      */       }
/*      */     }
/*  905 */     else if (name.equals("aParentTables"))
/*      */     {
/*  907 */       String relation = this.m_relationTextField.getText();
/*  908 */       String noValueStr = this.m_sysInterface.getString("apChoiceNoValues");
/*  909 */       if ((value != null) && (value.trim().length() > 0) && (!value.equals(noValueStr)))
/*      */       {
/*  911 */         if ((relation == null) || (relation.trim().length() == 0))
/*      */         {
/*  913 */           pass = false;
/*  914 */           msg = "apArchiveExportTableRelationsNotDefined";
/*      */         }
/*      */ 
/*      */       }
/*  919 */       else if ((relation != null) && (relation.trim().length() > 0))
/*      */       {
/*  921 */         pass = false;
/*  922 */         msg = "apArchiveExportTableRelationExistsWithoutParent";
/*      */       }
/*      */     }
/*      */ 
/*  926 */     if ((!pass) && (msg != null))
/*      */     {
/*  928 */       exchange.m_errorMessage = IdcMessageFactory.lc(msg, new Object[] { this.m_currentTable });
/*      */     }
/*  930 */     return pass;
/*      */   }
/*      */ 
/*      */   protected boolean isCurrentTableExist()
/*      */   {
/*  935 */     boolean isNotExist = false;
/*      */     try
/*      */     {
/*  938 */       Properties props = new Properties();
/*  939 */       props.put("tableNames", this.m_currentTable);
/*  940 */       props.put("isSuppressDataException", "1");
/*      */ 
/*  942 */       DataBinder binder = executeService("GET_TABLECOLUMNLIST", props);
/*  943 */       String nonExistingTables = binder.getLocal("TablesNotInDatabase");
/*  944 */       if (nonExistingTables != null)
/*      */       {
/*  946 */         isNotExist = nonExistingTables.indexOf("|" + this.m_currentTable.toLowerCase() + "|") >= 0;
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  951 */       Report.trace("applet", null, e);
/*      */     }
/*  953 */     return !isNotExist;
/*      */   }
/*      */ 
/*      */   public void itemStateChanged(ItemEvent event)
/*      */   {
/*  959 */     Object obj = event.getSource();
/*  960 */     boolean isLoadCompValue = false;
/*  961 */     if (obj instanceof JComboBox)
/*      */     {
/*  963 */       choiceHandler((JComboBox)obj);
/*  964 */       isLoadCompValue = true;
/*      */     }
/*  966 */     else if (obj instanceof JCheckBox)
/*      */     {
/*  968 */       checkboxHandler((JCheckBox)obj);
/*  969 */       isLoadCompValue = true;
/*      */     }
/*      */ 
/*  972 */     if (!isLoadCompValue)
/*      */       return;
/*  974 */     if ((this.m_isInItemStateChangedEvent) || (this.m_isExchangingComponentValue))
/*      */     {
/*  976 */       return;
/*      */     }
/*      */ 
/*  979 */     Properties oldProps = this.m_helper.m_props;
/*  980 */     this.m_helper.m_props = ((Properties)oldProps.clone());
/*  981 */     this.m_helper.retrieveComponentValues();
/*  982 */     this.m_helper.loadComponentValues();
/*  983 */     enableDisableOptions();
/*  984 */     this.m_helper.m_props = oldProps;
/*  985 */     this.m_isInItemStateChangedEvent = false;
/*      */   }
/*      */ 
/*      */   protected void choiceHandler(JComboBox choice)
/*      */   {
/*  991 */     int index = choice.getSelectedIndex();
/*      */     try
/*      */     {
/*  994 */       if (index > 0)
/*      */       {
/*  996 */         String keyMap = ResultSetUtils.findValue(this.m_archivableTables, "tableName", this.m_currentTable, "dependencyMapping");
/*  997 */         Vector maps = StringUtils.parseArray(keyMap, ',', ',');
/*  998 */         String map = (String)maps.elementAt(index - 1);
/*  999 */         this.m_relationTextField.setText(map);
/*      */ 
/* 1001 */         this.m_useParentTS.setEnabled(true);
/*      */       }
/*      */       else
/*      */       {
/* 1005 */         this.m_relationTextField.setText("");
/* 1006 */         if (this.m_useParentTS.isSelected())
/*      */         {
/* 1008 */           this.m_useParentTS.setSelected(false);
/*      */           try
/*      */           {
/* 1011 */             this.m_dateColumnList = initColumnList("Date");
/*      */           }
/*      */           catch (Exception ignore)
/*      */           {
/* 1015 */             Report.trace("archiver", null, ignore);
/*      */           }
/*      */         }
/* 1018 */         this.m_useParentTS.setEnabled(false);
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1023 */       Report.trace("archiver", null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkboxHandler(JCheckBox box)
/*      */   {
/* 1029 */     if (!box.getName().equalsIgnoreCase("aUseParentTS"))
/*      */       return;
/* 1031 */     String table = this.m_currentTable;
/* 1032 */     if (box.isSelected())
/*      */     {
/* 1034 */       table = (String)this.m_relations.getSelectedItem();
/*      */     }
/*      */     try
/*      */     {
/* 1038 */       this.m_dateColumnList = initColumnList(table, "Date");
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1042 */       Report.trace("archiver", null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void resetOptions(boolean isItemEvent, String table)
/*      */   {
/* 1053 */     if (this.m_isActive)
/*      */     {
/* 1057 */       this.m_abort = true;
/* 1058 */       return;
/*      */     }
/*      */ 
/* 1061 */     this.m_currentTable = table;
/* 1062 */     if (table != null)
/*      */     {
/* 1064 */       this.m_currentTable = table.trim();
/*      */     }
/* 1066 */     if (isItemEvent)
/*      */     {
/* 1068 */       synchronized (this.m_lockObject)
/*      */       {
/* 1070 */         this.m_lockObject.notify();
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1075 */       this.m_startTime = System.currentTimeMillis();
/* 1076 */       if ((this.m_timer != null) && (this.m_timer.isAlive()))
/*      */         return;
/* 1078 */       Runnable run = new Object()
/*      */       {
/*      */         public void run()
/*      */         {
/* 1083 */           while ((left = 1000L - System.currentTimeMillis() + EditTableDlg.this.m_startTime) > 0L)
/*      */           {
/*      */             long left;
/* 1085 */             synchronized (EditTableDlg.this.m_timer)
/*      */             {
/*      */               try
/*      */               {
/* 1089 */                 EditTableDlg.this.m_timer.wait(left);
/*      */               }
/*      */               catch (Exception ignore)
/*      */               {
/* 1093 */                 Report.trace("system", null, ignore);
/*      */               }
/*      */             }
/*      */           }
/*      */           try
/*      */           {
/* 1099 */             EditTableDlg.this.checkForAbort();
/* 1100 */             if ((!EditTableDlg.this.m_abort) && (!EditTableDlg.this.m_isActive))
/*      */             {
/* 1102 */               synchronized (EditTableDlg.this.m_lockObject)
/*      */               {
/* 1104 */                 EditTableDlg.this.m_lockObject.notify();
/*      */               }
/*      */             }
/*      */           }
/*      */           catch (Exception ignore)
/*      */           {
/* 1110 */             if (!SystemUtils.m_verbose)
/*      */               return;
/* 1112 */             Report.debug("system", null, ignore);
/*      */           }
/*      */         }
/*      */       };
/* 1118 */       this.m_timer = new Thread(run, "timer");
/* 1119 */       this.m_timer.setDaemon(true);
/* 1120 */       this.m_timer.start();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void startBackgroundThread()
/*      */   {
/* 1127 */     if (this.m_isInitialized)
/*      */     {
/* 1129 */       return;
/*      */     }
/* 1131 */     this.m_isInitialized = true;
/* 1132 */     this.m_isAlive = true;
/*      */ 
/* 1134 */     Runnable run = new Object()
/*      */     {
/*      */       public void run()
/*      */       {
/* 1138 */         while (EditTableDlg.this.m_isAlive)
/*      */         {
/* 1140 */           synchronized (EditTableDlg.this.m_lockObject)
/*      */           {
/*      */             try
/*      */             {
/* 1144 */               EditTableDlg.this.m_lockObject.wait();
/*      */             }
/*      */             catch (Throwable t)
/*      */             {
/* 1148 */               Report.trace("system", null, t);
/*      */             }
/*      */           }
/*      */ 
/* 1152 */           if (!EditTableDlg.this.m_isAlive) {
/*      */             return;
/*      */           }
/*      */ 
/* 1156 */           synchronized (EditTableDlg.this.m_lockObject)
/*      */           {
/* 1158 */             EditTableDlg.this.m_isActive = true;
/* 1159 */             EditTableDlg.this.doWork();
/* 1160 */             EditTableDlg.this.m_isActive = false;
/*      */           }
/*      */         }
/*      */       }
/*      */     };
/* 1166 */     this.m_bgThread = new Thread(run, "resetOptions");
/* 1167 */     this.m_bgThread.setDaemon(true);
/* 1168 */     this.m_bgThread.start();
/*      */   }
/*      */ 
/*      */   protected void stopBackgroundThread()
/*      */   {
/* 1173 */     if (this.m_bgThread.isAlive())
/*      */     {
/* 1175 */       this.m_isAlive = false;
/* 1176 */       synchronized (this.m_lockObject)
/*      */       {
/* 1178 */         this.m_lockObject.notify();
/*      */       }
/*      */     }
/*      */ 
/* 1182 */     while (this.m_bgThread.isAlive())
/*      */     {
/* 1186 */       SystemUtils.sleep(100L);
/*      */     }
/* 1188 */     this.m_bgThread = null;
/* 1189 */     this.m_isInitialized = false;
/*      */   }
/*      */ 
/*      */   protected void doWork()
/*      */   {
/* 1194 */     String currentTable = this.m_currentTable;
/* 1195 */     String table = null;
/*      */     try
/*      */     {
/* 1198 */       checkForAbort();
/* 1199 */       Vector dateColumnList = initColumnList("Date");
/* 1200 */       checkForAbort();
/* 1201 */       Vector textColumnList = initColumnList("Text");
/* 1202 */       checkForAbort();
/*      */ 
/* 1205 */       this.m_dateColumnList = dateColumnList;
/* 1206 */       this.m_textColumnList = textColumnList;
/*      */ 
/* 1208 */       table = findTableName(currentTable);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1212 */       Report.trace("applet", null, e);
/*      */     }
/*      */     finally
/*      */     {
/* 1216 */       if (!this.m_abort)
/*      */       {
/* 1218 */         if ((table != null) && (!table.equals(currentTable)))
/*      */         {
/* 1220 */           this.m_currentTable = table;
/* 1221 */           this.m_updateTableName = true;
/*      */         }
/*      */         else
/*      */         {
/* 1225 */           this.m_updateTableName = false;
/*      */         }
/* 1227 */         this.m_helper.m_exchange.exchange(this, true);
/*      */       }
/* 1229 */       this.m_abort = false;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkForAbort() throws ServiceException
/*      */   {
/* 1235 */     if (!this.m_abort)
/*      */       return;
/* 1237 */     throw new ServiceException("aborting the work");
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1243 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditTableDlg
 * JD-Core Version:    0.5.4
 */