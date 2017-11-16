/*      */ package intradoc.shared.gui;
/*      */ 
/*      */ import intradoc.common.Errors;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.Help;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemInterface;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.gui.DialogCallback;
/*      */ import intradoc.gui.DialogHelper;
/*      */ import intradoc.gui.DisplayChoice;
/*      */ import intradoc.gui.GridBagHelper;
/*      */ import intradoc.gui.GuiText;
/*      */ import intradoc.gui.IdcTreeHelper;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.gui.PanePanel;
/*      */ import intradoc.gui.iwt.DataRetrievalHelper;
/*      */ import intradoc.gui.iwt.RefreshItem;
/*      */ import intradoc.shared.DialogHelpTable;
/*      */ import intradoc.shared.SharedContext;
/*      */ import intradoc.shared.ViewFieldDef;
/*      */ import intradoc.shared.ViewFields;
/*      */ import intradoc.shared.schema.NamedRelationship;
/*      */ import intradoc.shared.schema.SchemaEditHelper;
/*      */ import intradoc.shared.schema.SchemaFieldData;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import intradoc.shared.schema.SchemaRelationData;
/*      */ import intradoc.shared.schema.SchemaTreePointer;
/*      */ import intradoc.shared.schema.SchemaViewData;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.awt.Component;
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
/*      */ import javax.swing.JCheckBox;
/*      */ import javax.swing.JDialog;
/*      */ import javax.swing.JPanel;
/*      */ import javax.swing.JPopupMenu;
/*      */ import javax.swing.JScrollPane;
/*      */ import javax.swing.JTree;
/*      */ import javax.swing.JWindow;
/*      */ import javax.swing.event.TreeExpansionEvent;
/*      */ import javax.swing.event.TreeExpansionListener;
/*      */ import javax.swing.event.TreeSelectionEvent;
/*      */ import javax.swing.event.TreeSelectionListener;
/*      */ import javax.swing.tree.DefaultTreeModel;
/*      */ import javax.swing.tree.DefaultTreeSelectionModel;
/*      */ import javax.swing.tree.TreePath;
/*      */ 
/*      */ public class SelectValueDlg
/*      */   implements ActionListener, TreeSelectionListener, DataRetrievalHelper, RefreshItem, ItemListener, TreeExpansionListener
/*      */ {
/*   94 */   protected SystemInterface m_systemInterface = null;
/*   95 */   protected ExecutionContext m_cxt = null;
/*   96 */   protected IdcLocale m_locale = null;
/*   97 */   protected SharedContext m_shContext = null;
/*   98 */   protected DialogHelper m_helper = null;
/*   99 */   protected String m_helpPage = null;
/*      */ 
/*  101 */   protected SchemaHelper m_schHelper = null;
/*  102 */   protected String m_fieldName = null;
/*  103 */   protected String m_tableName = null;
/*  104 */   protected boolean m_isError = false;
/*      */ 
/*  107 */   protected boolean m_isInited = false;
/*  108 */   protected JScrollPane m_scrollPane = null;
/*  109 */   protected JTree m_tree = null;
/*  110 */   protected IdcTreeHelper m_treeHelper = null;
/*  111 */   protected JCheckBox m_showAllValues = null;
/*      */ 
/*  114 */   protected Vector m_parentList = null;
/*  115 */   protected int m_currentParentIndex = 0;
/*      */ 
/*  120 */   protected boolean m_isField = false;
/*      */ 
/*  123 */   protected Hashtable m_topLevelMap = null;
/*  124 */   protected Hashtable m_storedData = null;
/*      */ 
/*  127 */   protected JWindow m_hoverPopup = null;
/*  128 */   protected JPopupMenu m_popup = null;
/*  129 */   protected Hashtable m_controlComponents = null;
/*  130 */   String[][] MENU_INFO = { { "apSchAddChildNode", "addChildNode" }, { "apSchEditNode", "editNode" }, { "apSchDeleteNode", "deleteNode" } };
/*      */ 
/*      */   public SelectValueDlg(SystemInterface sys, String title, String helpPage, SharedContext shContext)
/*      */   {
/*  139 */     this.m_helper = new DialogHelper(sys, title, true, true);
/*  140 */     this.m_systemInterface = sys;
/*  141 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*  142 */     this.m_locale = ((IdcLocale)this.m_cxt.getLocaleResource(0));
/*  143 */     this.m_shContext = shContext;
/*  144 */     this.m_helpPage = helpPage;
/*  145 */     this.m_topLevelMap = new Hashtable();
/*  146 */     this.m_storedData = new Hashtable();
/*  147 */     this.m_controlComponents = new Hashtable();
/*      */   }
/*      */ 
/*      */   public void init(SchemaHelper schHelper, String fieldName, String tableName, boolean isFieldOnly, boolean isAllowEdit, boolean isSelect)
/*      */   {
/*  153 */     this.m_schHelper = schHelper;
/*  154 */     this.m_isField = isFieldOnly;
/*  155 */     this.m_fieldName = fieldName;
/*  156 */     this.m_tableName = tableName;
/*      */ 
/*  158 */     if (isFieldOnly)
/*      */     {
/*  163 */       Vector fieldList = this.m_schHelper.computeParentListForField(this.m_fieldName);
/*  164 */       if (fieldList.size() == 0)
/*      */       {
/*  166 */         IdcMessage errMsg = IdcMessageFactory.lc("apSchNoParentFieldInfo", new Object[] { this.m_fieldName });
/*  167 */         MessageBox.reportError(this.m_systemInterface, errMsg);
/*  168 */         return;
/*      */       }
/*      */ 
/*  172 */       NamedRelationship namedRel = (NamedRelationship)fieldList.elementAt(0);
/*  173 */       if (namedRel.m_view == null)
/*      */       {
/*  175 */         IdcMessage errMsg = IdcMessageFactory.lc("apSchFieldMissingOptionConfig", new Object[] { this.m_fieldName });
/*  176 */         MessageBox.reportError(this.m_systemInterface, errMsg);
/*  177 */         return;
/*      */       }
/*      */ 
/*  181 */       this.m_parentList = new IdcVector();
/*  182 */       this.m_parentList.addElement(fieldList);
/*      */     }
/*      */     else
/*      */     {
/*  187 */       this.m_parentList = this.m_schHelper.buildParentTree(this.m_tableName, this.m_fieldName);
/*  188 */       if (this.m_parentList.size() == 0)
/*      */       {
/*  190 */         IdcMessage errMsg = IdcMessageFactory.lc("apSchNoParentsForColumn", new Object[] { this.m_fieldName });
/*  191 */         MessageBox.reportError(this.m_systemInterface, errMsg);
/*  192 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  196 */     initUI(isAllowEdit, isSelect);
/*      */     try
/*      */     {
/*  200 */       populateTree(null, 0, false, null);
/*  201 */       this.m_isInited = true;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  205 */       IdcMessage errMsg = IdcMessageFactory.lc(e, "apSchUnableToPopulateValues", new Object[0]);
/*  206 */       MessageBox.reportError(this.m_systemInterface, errMsg);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void initUI(boolean isAllowEdit, boolean isSelectMode)
/*      */   {
/*  212 */     DialogCallback okCallback = createOkCallback();
/*  213 */     JPanel pnl = null;
/*  214 */     if (isSelectMode)
/*      */     {
/*  216 */       pnl = this.m_helper.initStandard(null, okCallback, 2, true, this.m_helpPage);
/*      */     }
/*      */     else
/*      */     {
/*  222 */       pnl = this.m_helper.m_mainPanel;
/*  223 */       this.m_helper.makePanelGridBag(pnl, 2);
/*      */     }
/*      */ 
/*  226 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*  227 */     if (!this.m_isField)
/*      */     {
/*  230 */       gh.m_gc.fill = 0;
/*  231 */       gh.m_gc.weighty = 0.0D;
/*  232 */       addParentList(pnl);
/*      */     }
/*      */ 
/*  235 */     this.m_treeHelper = new IdcTreeHelper();
/*  236 */     this.m_tree = this.m_treeHelper.createTree();
/*  237 */     this.m_scrollPane = new JScrollPane(this.m_tree);
/*  238 */     this.m_tree.setRootVisible(false);
/*  239 */     this.m_tree.addTreeExpansionListener(this);
/*      */ 
/*  241 */     gh.prepareAddLastRowElement(11);
/*  242 */     gh.m_gc.fill = 1;
/*  243 */     gh.m_gc.weighty = 1.0D;
/*  244 */     this.m_helper.addComponent(pnl, this.m_scrollPane);
/*      */ 
/*  246 */     this.m_showAllValues = new JCheckBox(this.m_systemInterface.getString("apLabelShowAllValues"));
/*      */ 
/*  248 */     this.m_showAllValues.addItemListener(this);
/*  249 */     gh.prepareAddLastRowElement(11);
/*  250 */     gh.m_gc.fill = 0;
/*  251 */     gh.m_gc.weighty = (gh.m_gc.weightx = 0.0D);
/*  252 */     this.m_helper.addComponent(pnl, this.m_showAllValues);
/*      */ 
/*  254 */     if (!isAllowEdit)
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/*  260 */     if (!isSelectMode)
/*      */     {
/*  262 */       gh.m_gc.insets = new Insets(0, 5, 0, 0);
/*      */ 
/*  265 */       JPanel btnPanel = new PanePanel();
/*  266 */       this.m_helper.makePanelGridBag(btnPanel, 0);
/*      */ 
/*  268 */       gh.m_gc.fill = 0;
/*  269 */       gh.m_gc.weighty = 0.0D;
/*  270 */       gh.prepareAddRowElement();
/*  271 */       for (int i = 0; i < this.MENU_INFO.length; ++i)
/*      */       {
/*  273 */         String label = this.m_systemInterface.getString(this.MENU_INFO[i][0]);
/*  274 */         String cmd = this.MENU_INFO[i][1];
/*  275 */         JButton btn = new JButton(label);
/*  276 */         btn.setActionCommand(cmd);
/*  277 */         btn.addActionListener(this);
/*  278 */         this.m_controlComponents.put(cmd, btn);
/*      */ 
/*  280 */         this.m_helper.addComponent(btnPanel, btn);
/*      */       }
/*      */ 
/*  284 */       gh.m_gc.insets = new Insets(0, 10, 0, 0);
/*  285 */       JButton btn = new JButton(GuiText.m_closeLabel);
/*  286 */       btn.setActionCommand("close");
/*  287 */       btn.addActionListener(this);
/*  288 */       this.m_helper.addComponent(btnPanel, btn);
/*      */ 
/*  291 */       gh.m_gc.insets = new Insets(0, 5, 0, 0);
/*  292 */       btn = new JButton(GuiText.m_helpLabel);
/*  293 */       btn.setActionCommand("help");
/*  294 */       btn.addActionListener(this);
/*  295 */       boolean isEnabled = this.m_helpPage != null;
/*  296 */       btn.setEnabled(isEnabled);
/*  297 */       this.m_helper.addLastComponentInRow(btnPanel, btn);
/*      */ 
/*  300 */       this.m_helper.addLastComponentInRow(pnl, btnPanel);
/*      */     }
/*      */ 
/*  303 */     checkSelection();
/*  304 */     this.m_treeHelper.m_selectionModel.addTreeSelectionListener(this);
/*      */   }
/*      */ 
/*      */   protected DialogCallback createOkCallback()
/*      */   {
/*  311 */     DialogCallback okCallback = new DialogCallback()
/*      */     {
/*      */       public boolean handleDialogEvent(ActionEvent e)
/*      */       {
/*  316 */         IdcMessage errMsg = null;
/*  317 */         TreePath path = SelectValueDlg.this.m_tree.getSelectionPath();
/*  318 */         if (path != null)
/*      */         {
/*  320 */           ValueNode node = (ValueNode)path.getLastPathComponent();
/*      */ 
/*  324 */           if ((node.getParentIndex() != 0) && (!node.m_isTree))
/*      */           {
/*  326 */             errMsg = IdcMessageFactory.lc("apSchValueSelectedNotChild", new Object[0]);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  331 */           errMsg = IdcMessageFactory.lc("apSchNoValueSelected", new Object[0]);
/*      */         }
/*      */ 
/*  334 */         if (errMsg != null)
/*      */         {
/*  336 */           MessageBox.reportError(SelectValueDlg.this.m_systemInterface, errMsg);
/*  337 */           return false;
/*      */         }
/*  339 */         return true;
/*      */       }
/*      */     };
/*  343 */     return okCallback;
/*      */   }
/*      */ 
/*      */   protected void addParentList(JPanel pnl)
/*      */   {
/*  349 */     DisplayChoice cmp = new DisplayChoice();
/*      */ 
/*  351 */     int size = this.m_parentList.size();
/*  352 */     String[][] options = new String[size][];
/*  353 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  355 */       Vector list = (Vector)this.m_parentList.elementAt(i);
/*      */ 
/*  359 */       int num = list.size();
/*  360 */       NamedRelationship namedRel = (NamedRelationship)list.elementAt(num - 1);
/*  361 */       String fieldName = namedRel.m_field.get("schFieldName");
/*      */ 
/*  363 */       options[i] = new String[2];
/*  364 */       options[i][0] = fieldName;
/*  365 */       options[i][1] = namedRel.m_field.get("dCaption");
/*      */     }
/*      */ 
/*  368 */     cmp.init(options);
/*  369 */     cmp.addItemListener(this);
/*      */ 
/*  371 */     String label = this.m_systemInterface.getString("apSchParent");
/*  372 */     this.m_helper.addLabelFieldPair(pnl, label, cmp, "toplevelParent");
/*      */   }
/*      */ 
/*      */   public int prompt()
/*      */   {
/*  377 */     if (this.m_isInited)
/*      */     {
/*  379 */       return this.m_helper.prompt();
/*      */     }
/*  381 */     return 0;
/*      */   }
/*      */ 
/*      */   public String getSelectedValue()
/*      */   {
/*  386 */     TreePath path = this.m_tree.getSelectionPath();
/*  387 */     if (path != null)
/*      */     {
/*  389 */       ValueNode node = (ValueNode)path.getLastPathComponent();
/*  390 */       String val = node.m_value;
/*  391 */       if (node.m_isTree)
/*      */       {
/*  393 */         SchemaFieldData fieldData = this.m_schHelper.getField(this.m_fieldName);
/*  394 */         boolean isStorePath = fieldData.getBoolean("StoreSelectionPath", false);
/*  395 */         if (isStorePath)
/*      */         {
/*  397 */           String sep = fieldData.get("TreeNodeStorageSeparator", "/");
/*  398 */           while (node.getParent() != null)
/*      */           {
/*  400 */             node = node.getParent();
/*  401 */             if (val.length() > 0)
/*      */             {
/*  403 */               val = sep + val;
/*      */             }
/*  405 */             val = node.m_value + val;
/*      */           }
/*      */         }
/*      */       }
/*  409 */       return val;
/*      */     }
/*  411 */     return null;
/*      */   }
/*      */ 
/*      */   public Properties getSelectedItem()
/*      */   {
/*  416 */     TreePath path = this.m_tree.getSelectionPath();
/*  417 */     if (path != null)
/*      */     {
/*  419 */       ValueNode node = (ValueNode)path.getLastPathComponent();
/*  420 */       return node.m_props;
/*      */     }
/*      */ 
/*  423 */     return null;
/*      */   }
/*      */ 
/*      */   protected String determineNodeId(NamedRelationship namedRel)
/*      */   {
/*  428 */     String id = namedRel.m_id;
/*  429 */     if ((id == null) && (namedRel.m_field != null))
/*      */     {
/*  431 */       id = namedRel.m_field.get("schFieldName");
/*  432 */       namedRel.m_id = id;
/*      */     }
/*  434 */     return id;
/*      */   }
/*      */ 
/*      */   protected void refreshTree(ValueNode item, DataBinder cachedData, boolean isParent, ValueNode selItem, NamedRelationship namedRel, boolean isEdit)
/*      */     throws DataException, ServiceException
/*      */   {
/*  441 */     ValueNode parent = null;
/*  442 */     if (isParent)
/*      */     {
/*  444 */       parent = item;
/*      */     }
/*      */     else
/*      */     {
/*  448 */       parent = item.getParent();
/*      */     }
/*      */ 
/*  451 */     if (parent != null)
/*      */     {
/*  454 */       ValueNode node = parent;
/*  455 */       boolean isPlaceHolder = StringUtils.convertToBool(node.m_props.getProperty("isPlaceHolder"), false);
/*  456 */       if (isPlaceHolder)
/*      */       {
/*  458 */         parent = null;
/*      */       }
/*      */ 
/*  462 */       if (parent != null)
/*      */       {
/*  464 */         ValueNode grandParent = parent.getParent();
/*  465 */         if ((grandParent != null) && (namedRel != null))
/*      */         {
/*  467 */           ValueNode itemData = item;
/*  468 */           String relationClmn = namedRel.m_relation.get("schTable1Column");
/*  469 */           String keyVal = null;
/*  470 */           if (isEdit)
/*      */           {
/*  472 */             keyVal = itemData.m_props.getProperty(relationClmn);
/*      */           }
/*      */           else
/*      */           {
/*  479 */             String relationClmn2 = namedRel.m_relation.get("schTable2Column");
/*  480 */             keyVal = itemData.m_props.getProperty(relationClmn2);
/*      */           }
/*  482 */           int size = grandParent.getChildCount();
/*  483 */           for (int i = 0; i < size; ++i)
/*      */           {
/*  485 */             ValueNode sibling = (ValueNode)grandParent.getChildAt(i);
/*  486 */             ValueNode sibData = sibling;
/*  487 */             if (sibling.equals(parent))
/*      */               continue;
/*  489 */             String relVal = sibData.m_props.getProperty(relationClmn);
/*  490 */             if ((keyVal == null) || (!keyVal.equals(relVal)))
/*      */               continue;
/*  492 */             TreePath siblingPath = this.m_treeHelper.getTreePath(sibling);
/*  493 */             if (!this.m_tree.isExpanded(siblingPath))
/*      */               continue;
/*  495 */             this.m_tree.collapsePath(siblingPath);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  504 */     populateTree(parent, this.m_currentParentIndex, true, selItem);
/*      */ 
/*  506 */     if ((isParent) || (selItem == null) || 
/*  509 */       (!this.m_treeHelper.isExpanded(item)))
/*      */       return;
/*  511 */     populateTree(item, this.m_currentParentIndex - 1, false, selItem);
/*      */   }
/*      */ 
/*      */   protected void populateTree(ValueNode parentItem, int parentListIndex, boolean isRefresh, ValueNode selItem)
/*      */     throws ServiceException, DataException
/*      */   {
/*  522 */     ValueNode vData = null;
/*  523 */     int index = -1;
/*  524 */     Vector parents = null;
/*      */ 
/*  526 */     TreePath currentSelection = this.m_treeHelper.m_selectionModel.getSelectionPath();
/*  527 */     this.m_treeHelper.m_selectionModel.clearSelection();
/*  528 */     checkSelection();
/*      */ 
/*  530 */     if (parentItem != null)
/*      */     {
/*  533 */       ValueNode node = parentItem;
/*  534 */       boolean isPlaceHolder = StringUtils.convertToBool(node.m_props.getProperty("isPlaceHolder"), false);
/*  535 */       if (isPlaceHolder)
/*      */       {
/*  537 */         parentItem = null;
/*  538 */         parentListIndex = this.m_currentParentIndex;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  543 */     Vector topLevelList = null;
/*  544 */     if (parentItem == null)
/*      */     {
/*  548 */       this.m_currentParentIndex = parentListIndex;
/*  549 */       parents = (Vector)this.m_parentList.elementAt(parentListIndex);
/*  550 */       int size = parents.size();
/*  551 */       index = size - 1;
/*      */ 
/*  555 */       String key = "" + parentListIndex;
/*  556 */       topLevelList = (Vector)this.m_topLevelMap.get(key);
/*  557 */       if (topLevelList == null)
/*      */       {
/*  559 */         topLevelList = new IdcVector();
/*  560 */         this.m_topLevelMap.put(key, topLevelList);
/*      */       }
/*  562 */       else if (!isRefresh)
/*      */       {
/*  564 */         boolean isExpanded = index == 0;
/*  565 */         repopulateTree(topLevelList, isExpanded);
/*  566 */         return;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  571 */       vData = parentItem;
/*  572 */       parents = getParentList(vData.m_id);
/*      */ 
/*  574 */       index = computeIndex(vData, parents, true);
/*      */     }
/*      */ 
/*  577 */     if (index < 0)
/*      */     {
/*  579 */       throw new DataException("!apSchBadTreeParentIndex");
/*      */     }
/*      */ 
/*  582 */     NamedRelationship namedRel = (NamedRelationship)parents.elementAt(index);
/*  583 */     String id = determineNodeId(namedRel);
/*  584 */     Vector lookupVector = new IdcVector();
/*  585 */     lookupVector.addElement(id);
/*      */ 
/*  587 */     SchemaViewData view = namedRel.m_view;
/*  588 */     SchemaRelationData relation = null;
/*  589 */     String parentColumnName = null;
/*  590 */     String parentValue = null;
/*  591 */     if (parentItem != null)
/*      */     {
/*  594 */       relation = namedRel.m_relation;
/*      */ 
/*  600 */       parentColumnName = relation.get("schTable1Column");
/*  601 */       parentValue = vData.m_props.getProperty(parentColumnName);
/*  602 */       if (parentValue == null)
/*      */       {
/*  606 */         String errMsg = LocaleUtils.encodeMessage("apSchBadTreeDefinition", null, relation.get("schRelationName"), parentColumnName);
/*      */ 
/*  608 */         throw new DataException(errMsg);
/*      */       }
/*  610 */       lookupVector.addElement(vData.m_value);
/*      */     }
/*  614 */     else if ((namedRel.m_treePointer != null) && (namedRel.m_relation == null))
/*      */     {
/*  618 */       String initVal = namedRel.m_treePointer.m_initialKeyValue;
/*  619 */       if (initVal != null)
/*      */       {
/*  621 */         NamedRelationship recRel = (NamedRelationship)parents.elementAt(0);
/*  622 */         relation = recRel.m_relation;
/*  623 */         parentColumnName = relation.get("schTable1Column");
/*  624 */         parentValue = initVal;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  629 */     String schInternalClmn = view.get("schInternalColumn");
/*  630 */     assert (schInternalClmn != null);
/*  631 */     DataResultSet drset = retrieveViewValues(parentColumnName, parentValue, relation, view);
/*      */ 
/*  633 */     if (parentItem == null)
/*      */     {
/*  635 */       if (isRefresh)
/*      */       {
/*  637 */         parentItem = (ValueNode)topLevelList.elementAt(0);
/*      */       }
/*      */       else
/*      */       {
/*  642 */         String str = this.m_systemInterface.getString("apSchValuesDirectory");
/*  643 */         parentItem = createPlaceHolder(str, id, true);
/*      */ 
/*  645 */         ValueNode root = (ValueNode)this.m_treeHelper.m_dataModel.getRoot();
/*  646 */         this.m_treeHelper.m_dataModel.insertNodeInto(parentItem, root, root.getChildCount());
/*      */ 
/*  649 */         topLevelList.addElement(parentItem);
/*      */       }
/*      */     }
/*  652 */     else if (!isRefresh)
/*      */     {
/*  656 */       this.m_treeHelper.removeChildrenOfNode(parentItem);
/*      */     }
/*      */ 
/*  662 */     String lookupKey = StringUtils.createString(lookupVector, ',', '^');
/*  663 */     String tableName = view.get("schTableName");
/*  664 */     int itnlIndex = ResultSetUtils.getIndexMustExist(drset, schInternalClmn);
/*  665 */     DataBinder binder = new DataBinder();
/*  666 */     binder.setLocalData((Properties)view.getData().getLocalData().clone());
/*  667 */     binder.addResultSet(tableName, drset);
/*  668 */     DataResultSet oldSet = null;
/*  669 */     if (isRefresh)
/*      */     {
/*  671 */       DataBinder oldBinder = (DataBinder)this.m_storedData.get(lookupKey);
/*  672 */       if (oldBinder != null)
/*      */       {
/*  674 */         validateData(binder, oldBinder);
/*      */ 
/*  677 */         oldSet = (DataResultSet)oldBinder.getResultSet(tableName);
/*      */ 
/*  680 */         DataResultSet deleteSet = new DataResultSet();
/*  681 */         deleteSet.copy(oldSet);
/*  682 */         deleteSet.mergeDelete(schInternalClmn, drset, true);
/*      */ 
/*  685 */         for (deleteSet.first(); deleteSet.isRowPresent(); deleteSet.next())
/*      */         {
/*  687 */           String value = deleteSet.getStringValue(itnlIndex);
/*  688 */           for (int i = 0; i < parentItem.getChildCount(); ++i)
/*      */           {
/*  690 */             ValueNode n = (ValueNode)parentItem.getChildAt(i);
/*  691 */             if ((!value.equals(n.m_value)) || (!id.equals(n.m_id)))
/*      */               continue;
/*  693 */             this.m_treeHelper.m_dataModel.removeNodeFromParent(n);
/*  694 */             break;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  702 */     this.m_storedData.put(lookupKey, binder);
/*      */ 
/*  704 */     boolean isFoundSelected = false;
/*  705 */     int count = 0;
/*  706 */     boolean hasPotentialChildren = (index > 0) || (namedRel.m_isRecursive);
/*  707 */     for (drset.first(); drset.isRowPresent(); ++count)
/*      */     {
/*  709 */       ValueNode node = new ValueNode();
/*  710 */       node.m_value = drset.getStringValue(itnlIndex);
/*  711 */       node.m_props = drset.getCurrentRowProps();
/*  712 */       node.m_id = id;
/*  713 */       node.m_parentIndex = index;
/*  714 */       node.m_dataHelper = this;
/*  715 */       node.m_isTree = namedRel.m_isTree;
/*  716 */       node.m_containingObject = view;
/*      */ 
/*  718 */       if (hasPotentialChildren)
/*      */       {
/*  720 */         node.setAllowsChildren(true);
/*  721 */         node.m_typeId = "Directory";
/*      */       }
/*      */       else
/*      */       {
/*  725 */         node.setAllowsChildren(false);
/*  726 */         node.m_typeId = "Item";
/*      */       }
/*      */ 
/*  729 */       boolean isAlreadyPresent = false;
/*  730 */       if (oldSet != null)
/*      */       {
/*  735 */         Vector row = oldSet.findRow(itnlIndex, node.m_value);
/*  736 */         if (row != null)
/*      */         {
/*  739 */           ValueNode oldItem = null;
/*  740 */           for (int i = 0; i < parentItem.getChildCount(); ++i)
/*      */           {
/*  742 */             oldItem = (ValueNode)parentItem.getChildAt(i);
/*  743 */             if ((node.m_value.equals(oldItem.m_value)) && (node.m_id.equals(oldItem.m_id)))
/*      */             {
/*      */               break;
/*      */             }
/*      */ 
/*  748 */             oldItem = null;
/*      */           }
/*      */ 
/*  751 */           if (oldItem == null)
/*      */           {
/*  756 */             Report.trace("schema", "During refresh of schema view tree, found value " + node.m_value + " in old result set, but not in tree.", null);
/*      */           }
/*      */           else
/*      */           {
/*  762 */             ValueNode parent = oldItem.getParent();
/*  763 */             int oldIndex = oldItem.getParentIndex();
/*  764 */             this.m_treeHelper.m_dataModel.removeNodeFromParent(oldItem);
/*  765 */             this.m_treeHelper.m_dataModel.insertNodeInto(node, parent, oldIndex);
/*  766 */             isAlreadyPresent = true;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  771 */       if ((!isFoundSelected) && (selItem != null) && (node.equals(selItem)))
/*      */       {
/*  773 */         isFoundSelected = true;
/*      */       }
/*      */ 
/*  776 */       if (!isAlreadyPresent)
/*      */       {
/*  778 */         this.m_treeHelper.m_dataModel.insertNodeInto(node, parentItem, count);
/*      */ 
/*  781 */         if (hasPotentialChildren)
/*      */         {
/*  783 */           String str = this.m_systemInterface.getString("apSchValuesLoading");
/*  784 */           ValueNode dummyItem = createPlaceHolder(str, null, false);
/*  785 */           this.m_treeHelper.m_dataModel.insertNodeInto(dummyItem, node, 0);
/*  786 */           selItem = node;
/*      */         }
/*      */       }
/*  707 */       drset.next();
/*      */     }
/*      */ 
/*  791 */     if (parentItem != null)
/*      */     {
/*  793 */       TreePath parentPath = this.m_treeHelper.getTreePath(parentItem);
/*  794 */       this.m_tree.expandPath(parentPath);
/*      */     }
/*      */ 
/*  797 */     if (currentSelection != null)
/*      */     {
/*  799 */       this.m_treeHelper.m_selectionModel.setSelectionPath(currentSelection);
/*      */     }
/*  801 */     checkSelection();
/*      */   }
/*      */ 
/*      */   protected DataResultSet retrieveViewValues(String parentColumnName, String parentValue, SchemaRelationData relation, SchemaViewData view)
/*      */     throws DataException
/*      */   {
/*  807 */     Map args = new HashMap();
/*  808 */     if (relation != null)
/*      */     {
/*  810 */       args.put("relationName", relation.m_name);
/*      */     }
/*  812 */     args.put("context", this.m_cxt);
/*  813 */     DataResultSet drset = null;
/*  814 */     if (parentColumnName != null)
/*      */     {
/*  816 */       DataResultSet parentSet = this.m_schHelper.createParentSelectorSet(parentColumnName, parentValue);
/*      */ 
/*  818 */       args.put("parentValues", parentSet);
/*  819 */       drset = (DataResultSet)view.getViewValues(args);
/*      */     }
/*      */     else
/*      */     {
/*  823 */       drset = (DataResultSet)view.getAllViewValuesEx(args);
/*      */     }
/*  825 */     drset = (DataResultSet)view.prepareForConsumption(drset, this.m_cxt, 1);
/*      */ 
/*  827 */     return drset;
/*      */   }
/*      */ 
/*      */   protected int computeIndex(ValueNode vData, Vector parents, boolean isLookup)
/*      */   {
/*  832 */     int index = -1;
/*  833 */     if (vData.getParentIndex() == 0)
/*      */     {
/*  835 */       NamedRelationship nr = (NamedRelationship)parents.elementAt(vData.getParentIndex());
/*  836 */       if (nr.m_isRecursive)
/*      */       {
/*  838 */         int size = parents.size();
/*  839 */         for (int i = size - 1; i >= 0; --i)
/*      */         {
/*  841 */           NamedRelationship nRel = (NamedRelationship)parents.elementAt(i);
/*  842 */           if (nRel.m_treePointer == null)
/*      */             continue;
/*  844 */           index = i - 1;
/*  845 */           if (!isLookup)
/*      */             break;
/*  847 */           vData.m_recursionCount += 1; break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  856 */       index = vData.getParentIndex() - 1;
/*      */     }
/*  858 */     return index;
/*      */   }
/*      */ 
/*      */   protected void repopulateTree(Vector items, boolean isExpanded)
/*      */   {
/*  863 */     ValueNode root = new ValueNode();
/*  864 */     this.m_treeHelper.m_dataModel.setRoot(root);
/*      */ 
/*  866 */     int size = items.size();
/*  867 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  869 */       ValueNode item = (ValueNode)items.elementAt(i);
/*  870 */       this.m_treeHelper.m_dataModel.insertNodeInto(item, root, root.getChildCount());
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void flushTree(IdcMessage errMsg)
/*      */   {
/*  877 */     this.m_topLevelMap.clear();
/*  878 */     this.m_storedData.clear();
/*  879 */     this.m_isError = true;
/*      */ 
/*  881 */     ValueNode root = new ValueNode();
/*  882 */     this.m_treeHelper.m_dataModel.setRoot(root);
/*      */ 
/*  884 */     String text = LocaleResources.localizeMessage(null, errMsg, this.m_cxt).toString();
/*  885 */     ValueNode node = createPlaceHolder(text, null, true);
/*  886 */     node.m_props.put("isErrorNode", "1");
/*      */ 
/*  888 */     this.m_treeHelper.m_dataModel.insertNodeInto(node, root, root.getChildCount());
/*  889 */     checkSelection();
/*      */   }
/*      */ 
/*      */   protected ValueNode createPlaceHolder(String val, String id, boolean isParent)
/*      */   {
/*  894 */     ValueNode dummyNode = new ValueNode();
/*  895 */     dummyNode.m_value = val;
/*  896 */     dummyNode.m_props = new Properties();
/*  897 */     dummyNode.m_id = id;
/*  898 */     dummyNode.m_parentIndex = -1;
/*  899 */     dummyNode.m_dataHelper = this;
/*      */ 
/*  902 */     dummyNode.m_props.put("isPlaceHolder", "1");
/*      */ 
/*  904 */     if (isParent)
/*      */     {
/*  906 */       dummyNode.m_typeId = "Directory";
/*  907 */       dummyNode.setAllowsChildren(true);
/*      */     }
/*      */     else
/*      */     {
/*  911 */       dummyNode.m_typeId = "Item";
/*  912 */       dummyNode.setAllowsChildren(false);
/*      */     }
/*      */ 
/*  915 */     return dummyNode;
/*      */   }
/*      */ 
/*      */   protected void validateData(DataBinder newBinder, DataBinder oldBinder)
/*      */     throws ServiceException, DataException
/*      */   {
/*  928 */     SchemaEditHelper editHelper = new SchemaEditHelper();
/*  929 */     editHelper.validateViewData(newBinder, oldBinder);
/*  930 */     editHelper.detectRelationshipChange(this.m_fieldName, this.m_tableName, this.m_isField, this.m_parentList);
/*      */   }
/*      */ 
/*      */   public void treeExpanded(TreeExpansionEvent event)
/*      */   {
/*  938 */     TreePath path = event.getPath();
/*  939 */     if ((path == null) || (path.getPath().length <= 1))
/*      */       return;
/*  941 */     ValueNode node = (ValueNode)path.getLastPathComponent();
/*  942 */     if (node.getChildCount() != 1)
/*      */       return;
/*  944 */     ValueNode childNode = (ValueNode)node.getChildAt(0);
/*  945 */     boolean isPlaceHolder = StringUtils.convertToBool(childNode.m_props.getProperty("isPlaceHolder"), false);
/*      */ 
/*  947 */     if (!isPlaceHolder)
/*      */       return;
/*  949 */     expandItem(node);
/*      */   }
/*      */ 
/*      */   public void treeCollapsed(TreeExpansionEvent event)
/*      */   {
/*      */   }
/*      */ 
/*      */   public synchronized void expandItem(ValueNode parentItem)
/*      */   {
/*      */     try
/*      */     {
/*  970 */       populateTree(parentItem, -1, false, parentItem);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  974 */       Report.trace("applet", null, e);
/*  975 */       boolean isFatal = true;
/*  976 */       if (e instanceof ServiceException)
/*      */       {
/*  978 */         ServiceException se = (ServiceException)e;
/*  979 */         if (Errors.isNormalUserOperationalErrorCode(se.m_errorCode))
/*      */         {
/*  981 */           isFatal = false;
/*      */         }
/*      */       }
/*      */ 
/*  985 */       IdcMessage errMsg = IdcMessageFactory.lc(e, "apSchUnableToPopulateValues", new Object[] { this.m_fieldName });
/*  986 */       MessageBox.reportError(this.m_systemInterface, errMsg);
/*  987 */       if (!isFatal)
/*      */         return;
/*  989 */       this.m_helper.m_dialog.dispose();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected Vector getParentList(String fieldName)
/*      */   {
/*  996 */     int size = this.m_parentList.size();
/*  997 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  999 */       Vector v = (Vector)this.m_parentList.elementAt(i);
/* 1000 */       int num = v.size();
/* 1001 */       for (int j = 0; j < num; ++j)
/*      */       {
/* 1003 */         NamedRelationship namedRel = (NamedRelationship)v.elementAt(j);
/* 1004 */         String name = determineNodeId(namedRel);
/* 1005 */         if (name.equals(fieldName))
/*      */         {
/* 1007 */           return v;
/*      */         }
/*      */       }
/*      */     }
/* 1011 */     return null;
/*      */   }
/*      */ 
/*      */   public void itemStateChanged(ItemEvent e)
/*      */   {
/* 1019 */     int state = e.getStateChange();
/* 1020 */     Object obj = e.getSource();
/*      */ 
/* 1023 */     if (obj == this.m_showAllValues)
/*      */     {
/* 1026 */       this.m_tree.updateUI();
/* 1027 */       this.m_tree.repaint();
/*      */     } else {
/* 1029 */       if (!obj instanceof DisplayChoice)
/*      */         return;
/* 1031 */       if (this.m_isError)
/*      */       {
/* 1034 */         MessageBox.reportError(this.m_systemInterface, new IdcMessage("apSchConfigurationChange", new Object[0]));
/* 1035 */         return;
/*      */       }
/* 1037 */       switch (state)
/*      */       {
/*      */       case 1:
/* 1041 */         DisplayChoice dChoice = (DisplayChoice)obj;
/* 1042 */         int parentIndex = dChoice.getSelectedIndex();
/*      */         try
/*      */         {
/* 1045 */           this.m_treeHelper.removeAllNodes();
/* 1046 */           populateTree(null, parentIndex, false, null);
/*      */         }
/*      */         catch (Exception exp)
/*      */         {
/* 1050 */           IdcMessage errMsg = new IdcMessage(exp, "apSchUnableToPopulateValues", new Object[] { this.m_fieldName });
/* 1051 */           if (exp instanceof ServiceException)
/*      */           {
/* 1053 */             MessageBox.reportError(this.m_systemInterface, errMsg);
/*      */           }
/*      */           else
/*      */           {
/* 1057 */             flushTree(errMsg);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void valueChanged(TreeSelectionEvent e)
/*      */   {
/* 1071 */     checkSelection();
/*      */   }
/*      */ 
/*      */   protected void checkSelection()
/*      */   {
/* 1076 */     ValueNode node = null;
/* 1077 */     TreePath path = this.m_tree.getSelectionPath();
/* 1078 */     if (path != null)
/*      */     {
/* 1080 */       node = (ValueNode)path.getLastPathComponent();
/*      */     }
/*      */ 
/* 1083 */     for (Enumeration en = this.m_controlComponents.keys(); en.hasMoreElements(); )
/*      */     {
/* 1085 */       String cmd = (String)en.nextElement();
/* 1086 */       Component btn = (Component)this.m_controlComponents.get(cmd);
/* 1087 */       boolean isEnabled = false;
/* 1088 */       if (node != null)
/*      */       {
/* 1090 */         isEnabled = isActionAllowed(cmd, node);
/*      */       }
/* 1092 */       btn.setEnabled(isEnabled);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void actionPerformed(ActionEvent e)
/*      */   {
/* 1101 */     String cmd = e.getActionCommand();
/* 1102 */     doAction(cmd);
/*      */   }
/*      */ 
/*      */   protected void doAction(String cmd)
/*      */   {
/* 1107 */     if (cmd.equals("close"))
/*      */     {
/* 1109 */       this.m_helper.close();
/* 1110 */       return;
/*      */     }
/* 1112 */     if (cmd.equals("help"))
/*      */     {
/*      */       try
/*      */       {
/* 1116 */         Help.display(this.m_helpPage, this.m_cxt);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1120 */         IdcMessage msg = IdcMessageFactory.lc(e);
/* 1121 */         String msgText = LocaleResources.localizeMessage(null, msg, null).toString();
/* 1122 */         Report.trace(null, "Error in launching help: " + msgText, e);
/*      */       }
/* 1124 */       return;
/*      */     }
/*      */ 
/* 1128 */     TreePath path = this.m_tree.getSelectionPath();
/* 1129 */     if (path == null)
/*      */     {
/* 1132 */       return;
/*      */     }
/*      */ 
/* 1135 */     ValueNode node = (ValueNode)path.getLastPathComponent();
/* 1136 */     if (!isActionAllowed(cmd, node))
/*      */     {
/* 1138 */       IdcMessage errMsg = IdcMessageFactory.lc("apSchNodeActionError_" + cmd, new Object[] { node.m_value });
/* 1139 */       MessageBox.reportError(this.m_systemInterface, errMsg);
/*      */     }
/*      */     try
/*      */     {
/* 1143 */       if (cmd.equals("addChildNode"))
/*      */       {
/* 1145 */         addChildNode(node);
/*      */       }
/* 1147 */       else if (cmd.equals("editNode"))
/*      */       {
/* 1149 */         editNode(node);
/*      */       }
/* 1151 */       else if (cmd.equals("deleteNode"))
/*      */       {
/* 1153 */         deleteNode(node);
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1158 */       if (Errors.isNormalUserOperationalErrorCode(e.m_errorCode))
/*      */       {
/* 1160 */         MessageBox.reportError(this.m_systemInterface, e);
/*      */       }
/*      */       else
/*      */       {
/* 1164 */         flushTree(IdcMessageFactory.lc(e));
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1170 */       MessageBox.reportError(this.m_systemInterface, e);
/* 1171 */       flushTree(IdcMessageFactory.lc(e));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addChildNode(ValueNode node) throws ServiceException, DataException
/*      */   {
/* 1177 */     Vector filter = new IdcVector();
/* 1178 */     NamedRelationship[] namedRels = new NamedRelationship[1];
/* 1179 */     DataBinder cachedData = getCachedBinder(node, true, filter, namedRels);
/*      */ 
/* 1181 */     String tableName = cachedData.getLocal("schTableName");
/* 1182 */     DataResultSet drset = (DataResultSet)cachedData.getResultSet(tableName);
/* 1183 */     Properties props = new Properties();
/*      */ 
/* 1185 */     String title = this.m_systemInterface.getString("apSchAddNodeTitle");
/* 1186 */     String helpPage = DialogHelpTable.getHelpPage("SchemaAddNode");
/* 1187 */     AddViewValueDlg dlg = new AddViewValueDlg(this.m_systemInterface, title, helpPage);
/*      */ 
/* 1189 */     String internalClmn = cachedData.getLocal("schInternalColumn");
/* 1190 */     String primaryClmns = cachedData.getLocal("PrimaryColumns");
/* 1191 */     int result = dlg.init(props, internalClmn, drset, filter, true, primaryClmns);
/* 1192 */     if (result != 1)
/*      */       return;
/* 1194 */     TreePath p = this.m_treeHelper.getTreePath(node);
/* 1195 */     this.m_tree.expandPath(p);
/*      */ 
/* 1197 */     executeAction("add", null, props, node, cachedData);
/* 1198 */     refreshTree(node, cachedData, true, node, namedRels[0], true);
/*      */   }
/*      */ 
/*      */   protected void editNode(ValueNode node)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1205 */     Vector filter = new IdcVector();
/* 1206 */     NamedRelationship[] namedRels = new NamedRelationship[1];
/* 1207 */     DataBinder parentData = getCachedBinder(node, false, filter, namedRels);
/*      */ 
/* 1209 */     String tableName = parentData.getLocal("schTableName");
/* 1210 */     DataResultSet drset = (DataResultSet)parentData.getResultSet(tableName);
/*      */ 
/* 1212 */     Properties props = (Properties)node.m_props.clone();
/*      */ 
/* 1215 */     DataResultSet oldSet = new DataResultSet();
/* 1216 */     oldSet.copyFieldInfo(drset);
/* 1217 */     PropParameters params = new PropParameters(props);
/* 1218 */     Vector row = oldSet.createRow(params);
/* 1219 */     oldSet.addRow(row);
/*      */ 
/* 1221 */     DataBinder workingBinder = new DataBinder();
/* 1222 */     workingBinder.addResultSet("Old" + tableName, oldSet);
/*      */ 
/* 1224 */     String title = this.m_systemInterface.getString("apSchEditNodeTitle");
/* 1225 */     String helpPage = DialogHelpTable.getHelpPage("SchemaEditNode");
/* 1226 */     AddViewValueDlg dlg = new AddViewValueDlg(this.m_systemInterface, title, helpPage);
/*      */ 
/* 1228 */     String internalClmn = parentData.getLocal("schInternalColumn");
/* 1229 */     String primaryClmns = parentData.getLocal("PrimaryColumns");
/* 1230 */     int result = dlg.init(props, internalClmn, drset, filter, false, primaryClmns);
/* 1231 */     if (result != 1)
/*      */       return;
/* 1233 */     executeAction("edit", workingBinder, props, node, parentData);
/* 1234 */     refreshTree(node, parentData, false, node, namedRels[0], true);
/*      */   }
/*      */ 
/*      */   protected void deleteNode(ValueNode node)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1240 */     NamedRelationship[] namedRels = new NamedRelationship[1];
/* 1241 */     DataBinder parentData = getCachedBinder(node, false, null, namedRels);
/*      */ 
/* 1243 */     String msg = LocaleUtils.encodeMessage("apSchDeleteNodeOk", null, node.m_value);
/* 1244 */     msg = LocaleResources.localizeMessage(msg, this.m_cxt);
/* 1245 */     int result = MessageBox.doMessage(this.m_systemInterface, msg, 2);
/*      */ 
/* 1247 */     if (result != 1)
/*      */       return;
/* 1249 */     executeAction("delete", null, node.m_props, node, parentData);
/* 1250 */     refreshTree(node, parentData, false, null, namedRels[0], false);
/*      */   }
/*      */ 
/*      */   protected boolean isActionAllowed(String cmd, ValueNode node)
/*      */   {
/* 1257 */     boolean isPlaceHolder = StringUtils.convertToBool(node.m_props.getProperty("isPlaceHolder"), false);
/* 1258 */     boolean isDirectory = node.m_typeId.equals("Directory");
/* 1259 */     boolean isError = StringUtils.convertToBool(node.m_props.getProperty("isErrorNode"), false);
/* 1260 */     if (isError)
/*      */     {
/* 1262 */       return false;
/*      */     }
/*      */ 
/* 1265 */     boolean isEnabled = false;
/* 1266 */     if (cmd.equals("deleteNode"))
/*      */     {
/* 1268 */       isEnabled = !isPlaceHolder;
/*      */     }
/* 1270 */     else if (cmd.equals("editNode"))
/*      */     {
/* 1272 */       isEnabled = !isPlaceHolder;
/*      */     }
/* 1274 */     else if (cmd.equals("addChildNode"))
/*      */     {
/* 1276 */       isEnabled = isDirectory;
/*      */     }
/* 1278 */     return isEnabled;
/*      */   }
/*      */ 
/*      */   protected DataBinder getCachedBinder(ValueNode node, boolean isChild, Vector filter, NamedRelationship[] namedRels)
/*      */     throws ServiceException
/*      */   {
/* 1285 */     Properties nodeProps = node.m_props;
/* 1286 */     boolean isPlaceHolder = StringUtils.convertToBool(nodeProps.getProperty("isPlaceHolder"), false);
/* 1287 */     boolean isDirectory = node.m_typeId.equals("Directory");
/*      */ 
/* 1290 */     NamedRelationship namedRel = null;
/* 1291 */     ValueNode vNode = null;
/*      */ 
/* 1294 */     Vector v = new IdcVector();
/* 1295 */     if (isPlaceHolder)
/*      */     {
/* 1297 */       if (isDirectory)
/*      */       {
/* 1300 */         v.addElement(node.m_id);
/*      */       }
/*      */       else
/*      */       {
/* 1305 */         String errMsg = LocaleUtils.encodeMessage("apSchPlaceHolder", null, node.m_value);
/* 1306 */         new ServiceException(errMsg);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1311 */       Vector parentList = getParentList(node.m_id);
/* 1312 */       if (isChild)
/*      */       {
/* 1314 */         int index = computeIndex(node, parentList, false);
/* 1315 */         if (index < 0)
/*      */         {
/* 1318 */           String errMsg = LocaleUtils.encodeMessage("apSchNoNodeParent", null, node.m_value);
/* 1319 */           throw new ServiceException(-64, errMsg);
/*      */         }
/*      */ 
/* 1322 */         namedRel = (NamedRelationship)parentList.elementAt(index);
/* 1323 */         vNode = node;
/* 1324 */         String id = determineNodeId(namedRel);
/* 1325 */         v.addElement(id);
/* 1326 */         v.addElement(node.m_value);
/*      */       }
/*      */       else
/*      */       {
/* 1330 */         v.addElement(node.m_id);
/* 1331 */         if (node.getParent() != null)
/*      */         {
/* 1333 */           ValueNode pNode = node.getParent();
/* 1334 */           boolean isParentPlaceHolder = StringUtils.convertToBool(pNode.m_props.getProperty("isPlaceHolder"), false);
/*      */ 
/* 1336 */           if (!isParentPlaceHolder)
/*      */           {
/* 1338 */             namedRel = (NamedRelationship)parentList.elementAt(node.getParentIndex());
/* 1339 */             vNode = node.getParent();
/* 1340 */             v.addElement(vNode.m_value);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1346 */     String key = StringUtils.createString(v, ',', '^');
/* 1347 */     DataBinder binder = (DataBinder)this.m_storedData.get(key);
/*      */ 
/* 1349 */     if (binder == null)
/*      */     {
/* 1352 */       throw new ServiceException(-64, "!apSchNodeBinderNotFound");
/*      */     }
/*      */ 
/* 1356 */     if ((filter != null) && (namedRel != null) && (vNode != null))
/*      */     {
/* 1360 */       String tableName = binder.getLocal("schTableName");
/* 1361 */       DataResultSet drset = (DataResultSet)binder.getResultSet(tableName);
/*      */ 
/* 1363 */       String clmn = namedRel.m_relation.get("schTable2Column");
/* 1364 */       FieldInfo fi = new FieldInfo();
/* 1365 */       boolean exists = drset.getFieldInfo(clmn, fi);
/* 1366 */       if (!exists)
/*      */       {
/* 1368 */         String relName = namedRel.m_relation.get("schRelationName");
/* 1369 */         String errMsg = LocaleUtils.encodeMessage("apSchNodeRelationTableChange", null, tableName, relName);
/*      */ 
/* 1371 */         throw new ServiceException(errMsg);
/*      */       }
/*      */ 
/* 1375 */       ViewFields vf = new ViewFields(this.m_cxt);
/* 1376 */       ViewFieldDef vfd = vf.addViewFieldDefFromInfo(fi);
/*      */ 
/* 1378 */       FilterData fd = new FilterData(vfd);
/* 1379 */       String parentClmn = namedRel.m_relation.get("schTable1Column");
/* 1380 */       fd.setValueAt(vNode.m_props.getProperty(parentClmn), 0);
/* 1381 */       fd.setOperatorAt("=", 0);
/* 1382 */       fd.m_isUsed = true;
/* 1383 */       filter.addElement(fd);
/*      */     }
/*      */ 
/* 1386 */     namedRels[0] = namedRel;
/* 1387 */     return binder;
/*      */   }
/*      */ 
/*      */   protected void executeAction(String action, DataBinder binder, Properties props, ValueNode node, DataBinder parentData)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1393 */     PropParameters params = new PropParameters(props);
/*      */ 
/* 1395 */     String tableName = parentData.getLocal("schTableName");
/* 1396 */     DataResultSet drset = (DataResultSet)parentData.getResultSet(tableName);
/*      */ 
/* 1398 */     if (binder == null)
/*      */     {
/* 1400 */       binder = new DataBinder();
/*      */     }
/*      */ 
/* 1403 */     binder.putLocal("schViewName", parentData.getLocal("schViewName"));
/* 1404 */     binder.putLocal("editViewValueAction", action);
/* 1405 */     binder.putLocal("nodeValue", node.m_value);
/*      */ 
/* 1407 */     DataResultSet valSet = new DataResultSet();
/* 1408 */     valSet.copyFieldInfo(drset);
/* 1409 */     Vector row = valSet.createRow(params);
/* 1410 */     valSet.addRow(row);
/*      */ 
/* 1412 */     binder.addResultSet(tableName, valSet);
/* 1413 */     this.m_shContext.executeService("EDIT_SCHEMA_NODE", binder, false);
/*      */   }
/*      */ 
/*      */   public Object get(Object source, Object key)
/*      */   {
/* 1421 */     if ((!key instanceof String) || (!key.equals("label")))
/*      */     {
/* 1423 */       return null;
/*      */     }
/*      */ 
/* 1426 */     ValueNode vData = (ValueNode)source;
/* 1427 */     boolean isPlaceHolder = StringUtils.convertToBool(vData.m_props.getProperty("isPlaceHolder"), false);
/*      */ 
/* 1429 */     if (isPlaceHolder)
/*      */     {
/* 1431 */       return vData.m_value;
/*      */     }
/*      */ 
/* 1434 */     String label = null;
/* 1435 */     if (this.m_showAllValues.isSelected())
/*      */     {
/* 1437 */       label = vData.m_props.toString();
/*      */     }
/* 1439 */     if (label == null)
/*      */     {
/* 1441 */       label = vData.m_props.getProperty("Display." + this.m_locale.m_name);
/*      */     }
/* 1443 */     if (label == null)
/*      */     {
/* 1445 */       label = vData.m_props.getProperty("Display.default");
/*      */     }
/* 1447 */     if ((label == null) && 
/* 1449 */       (vData.m_containingObject != null) && (vData.m_containingObject instanceof SchemaViewData))
/*      */     {
/* 1452 */       SchemaViewData view = (SchemaViewData)vData.m_containingObject;
/* 1453 */       String displayField = view.get("schDisplayField");
/* 1454 */       label = vData.m_props.getProperty(displayField);
/*      */     }
/*      */ 
/* 1458 */     if ((label != null) && (label.length() > 0))
/*      */     {
/* 1460 */       return vData.m_value + ":  " + label;
/*      */     }
/*      */ 
/* 1463 */     return vData.m_value;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1468 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80328 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.SelectValueDlg
 * JD-Core Version:    0.5.4
 */