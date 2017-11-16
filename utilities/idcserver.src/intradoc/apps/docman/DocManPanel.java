/*      */ package intradoc.apps.docman;
/*      */ 
/*      */ import intradoc.apps.shared.AppLauncher;
/*      */ import intradoc.apps.shared.BasePanel;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemInterface;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.gui.CommonDialogs;
/*      */ import intradoc.gui.ContainerHelper;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.gui.PanePanel;
/*      */ import intradoc.gui.iwt.ColumnInfo;
/*      */ import intradoc.gui.iwt.DataResultSetTableModel;
/*      */ import intradoc.gui.iwt.IdcTable;
/*      */ import intradoc.gui.iwt.UdlPanel;
/*      */ import intradoc.gui.iwt.UserDrawList;
/*      */ import intradoc.gui.iwt.event.IwtItemEvent;
/*      */ import intradoc.shared.DialogHelpTable;
/*      */ import intradoc.shared.DocFieldUtils;
/*      */ import intradoc.shared.DocumentLocalizedProfile;
/*      */ import intradoc.shared.SharedContext;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.gui.AddDocumentDlg;
/*      */ import intradoc.shared.gui.DocView;
/*      */ import intradoc.shared.gui.FilterUtils;
/*      */ import intradoc.shared.gui.RefreshView;
/*      */ import intradoc.shared.gui.ViewData;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.awt.event.FocusEvent;
/*      */ import java.awt.event.FocusListener;
/*      */ import java.awt.event.ItemEvent;
/*      */ import java.awt.event.ItemListener;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Observable;
/*      */ import java.util.Observer;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.swing.JButton;
/*      */ import javax.swing.JMenu;
/*      */ import javax.swing.JMenuItem;
/*      */ import javax.swing.JPanel;
/*      */ import javax.swing.JPopupMenu;
/*      */ import javax.swing.MenuElement;
/*      */ 
/*      */ public class DocManPanel extends BasePanel
/*      */   implements FocusListener, Observer, RefreshView, SharedContext
/*      */ {
/*      */   public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92435 $";
/*      */   protected DocView m_docView;
/*      */   protected JButton m_addDocBtn;
/*      */   protected JButton[] m_deleteBtn;
/*      */   protected boolean m_doNotUpdate;
/*      */   protected String m_user;
/*      */   protected DocumentLocalizedProfile m_docProfile;
/*      */   protected ActionListener m_popupActionListener;
/*      */   protected final String[][] COMMANDS_LIST;
/*      */   protected String[][] m_commands;
/*      */ 
/*      */   public DocManPanel()
/*      */   {
/*   88 */     this.m_addDocBtn = null;
/*   89 */     this.m_deleteBtn = new JButton[2];
/*      */ 
/*   92 */     this.m_doNotUpdate = false;
/*      */ 
/*   94 */     this.m_docProfile = null;
/*   95 */     this.m_popupActionListener = new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*   99 */         String cmdStr = e.getActionCommand();
/*  100 */         if ((cmdStr.equals("approve")) || (cmdStr.equals("reject")))
/*      */         {
/*  102 */           DocManPanel.this.review(cmdStr);
/*      */         }
/*  104 */         else if (cmdStr.equals("addRevision"))
/*      */         {
/*  106 */           DocManPanel.this.addRevision(false);
/*      */         }
/*  108 */         else if (cmdStr.equals("update"))
/*      */         {
/*  110 */           DocManPanel.this.updateDocInfo();
/*      */         }
/*  112 */         else if (cmdStr.equals("subscribers"))
/*      */         {
/*  114 */           DocManPanel.this.viewSubscribers();
/*      */         }
/*  116 */         else if (cmdStr.equals("info"))
/*      */         {
/*  118 */           DocManPanel.this.m_docView.info();
/*      */         }
/*  120 */         else if (cmdStr.equals("computeFileInfo"))
/*      */         {
/*  122 */           DocManPanel.this.computeFileInfo();
/*      */         }
/*      */         else
/*      */         {
/*  126 */           DocManPanel.this.actionCommand(cmdStr);
/*      */         }
/*      */       }
/*      */     };
/*  132 */     this.COMMANDS_LIST = new String[][] { { "apRevisionInfo", "info" }, { "apAddRevision", "addRevision" }, { "apUpdateRevision", "update" }, { "apRevisionSubscribers", "subscribers" }, { "separator", "separator" }, { "apCheckOutRevision", "checkOut" }, { "apUndoCheckOutRevision", "undoCheckOut" }, { "apApproveRevision", "approve" }, { "apRejectRevision", "reject" }, { "separator", "separator" }, { "apResubmitRevision", "resubmit" }, { "apDeleteRevision", "deleteRevision" }, { "apDeleteAllRevisions", "deleteDocument" } };
/*      */ 
/*  148 */     this.m_commands = ((String[][])null);
/*      */   }
/*      */ 
/*      */   public void init(SystemInterface sys, JMenu fMenu) throws ServiceException
/*      */   {
/*  153 */     ExecutionContext cxt = sys.getExecutionContext();
/*  154 */     int len = this.COMMANDS_LIST.length;
/*      */ 
/*  156 */     boolean isBeta = SharedObjects.getEnvValueAsBoolean("IsBeta", false);
/*  157 */     if (isBeta)
/*      */     {
/*  159 */       ++len;
/*      */     }
/*  161 */     this.m_commands = new String[len][];
/*  162 */     int count = 0;
/*  163 */     for (; count < this.COMMANDS_LIST.length; ++count)
/*      */     {
/*  165 */       String key = this.COMMANDS_LIST[count][0];
/*  166 */       this.COMMANDS_LIST[count][0] = LocaleResources.getString(key, cxt);
/*      */ 
/*  168 */       this.m_commands[count] = new String[2];
/*  169 */       this.m_commands[count][0] = this.COMMANDS_LIST[count][0];
/*  170 */       this.m_commands[count][1] = this.COMMANDS_LIST[count][1];
/*      */     }
/*      */ 
/*  173 */     if (isBeta)
/*      */     {
/*  175 */       this.m_commands[count] = new String[2];
/*  176 */       this.m_commands[count][0] = LocaleResources.getString("apComputeFileInfo", cxt);
/*  177 */       this.m_commands[count][1] = "computeFileInfo";
/*      */     }
/*      */ 
/*  180 */     super.init(sys, fMenu);
/*  181 */     update(null, null);
/*  182 */     AppLauncher.addSubjectObserver("documents", this);
/*      */   }
/*      */ 
/*      */   protected void initUI()
/*      */   {
/*  188 */     ContainerHelper helper = new ContainerHelper();
/*  189 */     ExecutionContext cxt = this.m_systemInterface.getExecutionContext();
/*  190 */     helper.attachToContainer(this, this.m_systemInterface, null);
/*  191 */     helper.m_mainPanel = this;
/*  192 */     UserData userData = AppLauncher.getUserData();
/*  193 */     this.m_docProfile = new DocumentLocalizedProfile(userData, 8, this.m_cxt);
/*      */ 
/*  195 */     this.m_docView = new DocView(helper, this, this.m_docProfile);
/*  196 */     ViewData viewData = new ViewData(1);
/*  197 */     viewData.m_isMultipleMode = true;
/*  198 */     this.m_docView.initUI(viewData);
/*      */ 
/*  203 */     this.m_docView.addPopup(LocaleResources.getString("apActionsLabel", cxt), this.m_commands, this.m_popupActionListener);
/*      */ 
/*  206 */     ItemListener listListener = new ItemListener()
/*      */     {
/*      */       public void itemStateChanged(ItemEvent e)
/*      */       {
/*  210 */         int state = e.getStateChange();
/*  211 */         if ((state != 1) && (state != 2))
/*      */         {
/*  213 */           return;
/*      */         }
/*  215 */         if (e instanceof IwtItemEvent)
/*      */         {
/*  217 */           IwtItemEvent itemEvent = (IwtItemEvent)e;
/*  218 */           if (!itemEvent.checkFlag(IwtItemEvent.FINAL_ITEM_EVENT))
/*      */           {
/*  220 */             return;
/*      */           }
/*      */         }
/*  223 */         DocManPanel.this.checkSelection();
/*      */       }
/*      */     };
/*  226 */     this.m_docView.addItemListener(listListener);
/*      */ 
/*  230 */     JPanel docButtons = new PanePanel();
/*      */ 
/*  232 */     if (AppLauncher.getIsStandAlone())
/*      */     {
/*  234 */       JButton addNewDocBtn = new JButton(LocaleResources.getString("apAddNewDocument", cxt));
/*      */ 
/*  236 */       docButtons.add(addNewDocBtn);
/*      */ 
/*  238 */       this.m_addDocBtn = new JButton(LocaleResources.getString("apAddRevision", cxt));
/*      */ 
/*  240 */       docButtons.add(this.m_addDocBtn);
/*  241 */       ActionListener addListener = new Object()
/*      */       {
/*      */         public void actionPerformed(ActionEvent e)
/*      */         {
/*  245 */           boolean isNew = true;
/*  246 */           Object obj = e.getSource();
/*  247 */           if (obj == DocManPanel.this.m_addDocBtn)
/*      */           {
/*  249 */             isNew = false;
/*      */           }
/*      */ 
/*  252 */           DocManPanel.this.addRevision(isNew);
/*      */         }
/*      */       };
/*  255 */       this.m_addDocBtn.addActionListener(addListener);
/*  256 */       addNewDocBtn.addActionListener(addListener);
/*      */     }
/*      */ 
/*  259 */     ActionListener listener = new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*  263 */         String cmdStr = e.getActionCommand();
/*  264 */         DocManPanel.this.actionCommand(cmdStr);
/*      */       }
/*      */     };
/*  268 */     int start = this.COMMANDS_LIST.length - 2;
/*  269 */     int i = start; for (int j = 0; i < this.COMMANDS_LIST.length; ++j)
/*      */     {
/*  271 */       this.m_deleteBtn[j] = new JButton(this.COMMANDS_LIST[i][0]);
/*  272 */       docButtons.add(this.m_deleteBtn[j]);
/*  273 */       this.m_deleteBtn[j].setActionCommand(this.COMMANDS_LIST[i][1]);
/*  274 */       this.m_deleteBtn[j].addActionListener(listener);
/*      */ 
/*  269 */       ++i;
/*      */     }
/*      */ 
/*  278 */     this.m_docView.addButtonToolbar(docButtons);
/*      */ 
/*  280 */     for (int i = 0; i < this.m_commands.length; ++i)
/*      */     {
/*  282 */       String lblStr = this.m_commands[i][0];
/*  283 */       String cmdStr = this.m_commands[i][1];
/*  284 */       if (cmdStr.equalsIgnoreCase("separator"))
/*      */       {
/*  286 */         this.m_fMenu.addSeparator();
/*      */       }
/*      */       else
/*      */       {
/*  290 */         JMenuItem mi = new JMenuItem(lblStr);
/*  291 */         mi.setActionCommand(cmdStr);
/*  292 */         this.m_fMenu.add(mi);
/*  293 */         mi.addActionListener(this.m_popupActionListener);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void updateAccessibilityInfo(ExecutionContext cxt)
/*      */   {
/*      */   }
/*      */ 
/*      */   protected void refreshDocumentList()
/*      */     throws ServiceException
/*      */   {
/*  308 */     this.m_docView.refreshView();
/*      */   }
/*      */ 
/*      */   protected void refreshDocumentList(String object) throws ServiceException
/*      */   {
/*  313 */     this.m_docView.refreshView(object);
/*      */   }
/*      */ 
/*      */   public DataBinder refresh(String rsetName, Vector filterData, DataResultSet defSet) throws ServiceException
/*      */   {
/*  318 */     DataBinder binder = new DataBinder();
/*      */ 
/*  320 */     ViewData viewData = this.m_docView.getViewData();
/*  321 */     FilterUtils.createTopicEdits(viewData.m_viewName + ":filter", binder, defSet);
/*      */ 
/*  324 */     String whereClause = this.m_docView.buildSQL(filterData);
/*      */ 
/*  327 */     binder.putLocal("whereClause", whereClause);
/*  328 */     binder.putLocal("orderClause", "ORDER by Revisions.dDocName");
/*  329 */     binder.putLocal("resultName", rsetName);
/*  330 */     binder.putLocal("dataSource", "Documents");
/*      */     try
/*      */     {
/*  334 */       DocFieldUtils.setFieldTypes(binder);
/*  335 */       AppLauncher.executeService("GET_DATARESULTSET", binder, this.m_systemInterface);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  339 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/*  343 */     UserData userData = AppLauncher.getUserData();
/*  344 */     this.m_docProfile.m_userData = userData;
/*      */ 
/*  346 */     updateAccessibilityInfo(this.m_systemInterface.getExecutionContext());
/*      */ 
/*  348 */     return binder;
/*      */   }
/*      */ 
/*      */   public DataResultSet getMetaData()
/*      */   {
/*  353 */     return SharedObjects.getTable("DocMetaDefinition");
/*      */   }
/*      */ 
/*      */   public SharedContext getSharedContext()
/*      */   {
/*  358 */     return this;
/*      */   }
/*      */ 
/*      */   public void executeService(String action, DataBinder binder, boolean isRefresh)
/*      */     throws ServiceException
/*      */   {
/*  366 */     AppLauncher.executeService(action, binder);
/*      */   }
/*      */ 
/*      */   public UserData getUserData()
/*      */   {
/*  371 */     return AppLauncher.getUserData();
/*      */   }
/*      */ 
/*      */   public void update(Observable obs, Object arg)
/*      */   {
/*  379 */     if (this.m_doNotUpdate)
/*      */     {
/*  381 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  386 */       refreshDocumentList();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  390 */       MessageBox.reportError(this.m_systemInterface, e);
/*      */     }
/*  392 */     updateAccessibilityInfo(this.m_systemInterface.getExecutionContext());
/*      */   }
/*      */ 
/*      */   public void removeNotify()
/*      */   {
/*  398 */     AppLauncher.removeSubjectObserver("documents", this);
/*  399 */     super.removeNotify();
/*      */   }
/*      */ 
/*      */   public void doAction(String action, DocActionData data)
/*      */   {
/*  404 */     ExecutionContext cxt = this.m_systemInterface.getExecutionContext();
/*  405 */     DataResultSet selDocs = data.m_items;
/*      */ 
/*  409 */     DataResultSet items = new DataResultSet();
/*  410 */     items.copyFieldInfo(data.m_items);
/*  411 */     Hashtable docTable = new Hashtable();
/*  412 */     Hashtable revTable = new Hashtable();
/*      */ 
/*  414 */     String curDocName = LocaleResources.getString("apIdUnknown", this.m_systemInterface.getExecutionContext());
/*      */ 
/*  416 */     for (selDocs.first(); selDocs.isRowPresent(); selDocs.next())
/*      */     {
/*  418 */       Properties props = selDocs.getCurrentRowProps();
/*  419 */       curDocName = props.getProperty("dDocName");
/*      */ 
/*  421 */       if ((data.m_eliminateDuplicateDocs) && (docTable.get(curDocName) != null)) continue; if (!isActionAllowed(action, props))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  427 */       String dID = props.getProperty("dID");
/*  428 */       revTable.put(dID, props);
/*  429 */       items.addRow(selDocs.getCurrentRowValues());
/*  430 */       docTable.put(curDocName, props);
/*      */     }
/*      */ 
/*  434 */     boolean isPrompt = false;
/*  435 */     boolean isMultiple = false;
/*  436 */     int num = items.getNumRows();
/*      */     IdcMessage title;
/*  437 */     switch (num)
/*      */     {
/*      */     case 0:
/*  441 */       return;
/*      */     case 1:
/*  444 */       isPrompt = data.m_promptSingle;
/*  445 */       title = IdcMessageFactory.lc(data.m_actionLabel + "_2", new Object[] { curDocName });
/*  446 */       break;
/*      */     default:
/*  449 */       isPrompt = data.m_promptMultiple;
/*  450 */       isMultiple = true;
/*  451 */       title = IdcMessageFactory.lc(data.m_actionLabel + "_1", new Object[0]);
/*      */     }
/*      */ 
/*  456 */     data.m_title = LocaleResources.getString(data.m_title, cxt, "" + num);
/*  457 */     UdlPanel promptList = null;
/*      */ 
/*  459 */     if (isPrompt)
/*      */     {
/*  461 */       promptList = promptUser(items, data);
/*  462 */       if (promptList == null)
/*      */       {
/*  464 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  468 */     boolean yesAll = false;
/*  469 */     boolean noAll = false;
/*  470 */     boolean ignoreAll = false;
/*      */     try
/*      */     {
/*  474 */       this.m_doNotUpdate = true;
/*  475 */       DataResultSet drset = null;
/*  476 */       FieldInfo fi = null;
/*      */ 
/*  478 */       if (promptList != null)
/*      */       {
/*  480 */         drset = (DataResultSet)promptList.getResultSet();
/*  481 */         fi = new FieldInfo();
/*  482 */         drset.getFieldInfo("IsSelected", fi);
/*      */       }
/*      */ 
/*  485 */       for (int i = 0; i < num; ++i)
/*      */       {
/*  487 */         DataBinder binder = new DataBinder();
/*  488 */         Properties localProps = null;
/*      */ 
/*  490 */         if (promptList != null)
/*      */         {
/*  492 */           drset.setCurrentRow(i);
/*  493 */           boolean isSelected = StringUtils.convertToBool(drset.getStringValue(fi.m_index), false);
/*  494 */           if (!isSelected)
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/*  499 */           Properties p = promptList.getDataAt(i);
/*  500 */           String id = (String)p.get("dID");
/*  501 */           localProps = (Properties)revTable.get(id);
/*      */         }
/*      */         else
/*      */         {
/*  505 */           items.setCurrentRow(i);
/*  506 */           localProps = items.getCurrentRowProps();
/*      */         }
/*      */ 
/*  509 */         binder.setLocalData(localProps);
/*  510 */         boolean isAllowed = isActionAllowed(action, localProps);
/*  511 */         if (!isAllowed)
/*      */           continue;
/*      */         int result;
/*  515 */         if ((data.m_promptEach) || (data.isPrompt(localProps)))
/*      */         {
/*      */           int result;
/*  519 */           if ((!yesAll) && (!noAll))
/*      */           {
/*  521 */             IdcMessage msg = createMessage(action, data, binder, i, num);
/*  522 */             int msgBoxType = (isMultiple) ? 128 : 2;
/*      */ 
/*  524 */             result = MessageBox.doMessage(this.m_systemInterface, title, msg, msgBoxType);
/*      */           }
/*      */           else
/*      */           {
/*  529 */             result = (yesAll) ? 2 : 3;
/*      */           }
/*      */         }
/*  532 */         switch (result)
/*      */         {
/*      */         case 9:
/*  535 */           yesAll = true;
/*      */         case 1:
/*      */         case 2:
/*  538 */           if ((data.m_promptOverrideName != null) && (data.m_promptOverrideValue != null))
/*      */           {
/*  540 */             localProps.put(data.m_promptOverrideName, data.m_promptOverrideValue); } break;
/*      */         case 10:
/*  544 */           noAll = true;
/*      */         case 3:
/*  546 */           break;
/*      */         case 0:
/*      */           return;
/*      */         case 4:
/*      */         case 5:
/*      */         case 6:
/*      */         case 7:
/*      */         case 8:
/*      */         default:
/*  553 */           if (data.m_promptMessage != null)
/*      */           {
/*  555 */             localProps.put(data.m_promptForMessageField, LocaleResources.getString(data.m_promptMessage, cxt));
/*      */           }
/*      */ 
/*      */           try
/*      */           {
/*  562 */             String[] fields = { "dFormat", "dReleaseState", "dStatus", "dIndexerState", "dWorkflowState", "dPublishType", "dPublishState", "dMessage" };
/*      */ 
/*  564 */             for (int count = 0; count < fields.length; ++count)
/*      */             {
/*  566 */               binder.removeLocal(fields[count]);
/*      */             }
/*  568 */             label913: executeService(data.m_serviceName, binder, false);
/*      */           }
/*      */           catch (Exception exp)
/*      */           {
/*  572 */             if (!ignoreAll)
/*      */             {
/*  574 */               int msgType = (isMultiple) ? 256 : 1;
/*  575 */               int rc = MessageBox.reportErrorEx(this.m_systemInterface, exp, msgType);
/*  576 */               if ((rc == 0) || (rc == 1)) {
/*      */                 break label913;
/*      */               }
/*      */ 
/*  580 */               if (rc == 11)
/*      */               {
/*  582 */                 ignoreAll = true;
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     finally {
/*  590 */       this.m_doNotUpdate = false;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  595 */       refreshDocumentList();
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  599 */       MessageBox.reportError(this.m_systemInterface, e);
/*      */     }
/*      */ 
/*  602 */     updateAccessibilityInfo(this.m_systemInterface.getExecutionContext());
/*      */   }
/*      */ 
/*      */   protected IdcMessage createMessage(String action, DocActionData data, DataBinder binder, int i, int total)
/*      */   {
/*  608 */     IdcMessage msg = data.buildMessage(binder, i, total);
/*  609 */     return msg;
/*      */   }
/*      */ 
/*      */   protected UdlPanel promptUser(DataResultSet items, DocActionData data)
/*      */   {
/*  614 */     UdlPanel udlPanel = this.m_docView.getList();
/*  615 */     ExecutionContext cxt = this.m_systemInterface.getExecutionContext();
/*      */ 
/*  617 */     ColumnInfo[] infos = new ColumnInfo[data.m_columns.length];
/*  618 */     StringBuffer columns = new StringBuffer();
/*      */ 
/*  620 */     int num = items.getNumRows();
/*  621 */     String[] selectedItems = new String[num];
/*  622 */     FieldInfo info = new FieldInfo();
/*  623 */     items.getFieldInfo("dID", info);
/*      */ 
/*  625 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  627 */       Vector v = items.getRowValues(i);
/*  628 */       selectedItems[i] = ((String)v.elementAt(info.m_index));
/*      */     }
/*      */ 
/*  631 */     UdlPanel promptList = new UdlPanel(LocaleResources.getString("apTitleItems", cxt), null, 320, 10, "Items", true);
/*      */ 
/*  633 */     promptList.m_list.setFlags(271);
/*      */ 
/*  638 */     ColumnInfo c = new ColumnInfo(" ", "IsSelected", 5.0D);
/*  639 */     c.m_isCheckbox = true;
/*  640 */     promptList.setColumnInfo(c);
/*  641 */     columns.append("IsSelected,");
/*  642 */     FieldInfo fi = new FieldInfo();
/*  643 */     fi.m_name = "IsSelected";
/*  644 */     List l = new ArrayList();
/*  645 */     l.add(fi);
/*  646 */     items.mergeFieldsWithFlags(l, 0);
/*  647 */     this.m_docView.getList().m_list.m_tableDataModel.m_rset.mergeFieldsWithFlags(l, 0);
/*      */ 
/*  649 */     for (int i = 0; i < infos.length; ++i)
/*      */     {
/*  651 */       ColumnInfo tmpInfo = udlPanel.getColumnInfo(data.m_columns[i]);
/*  652 */       infos[i] = new ColumnInfo(tmpInfo.m_labelText, tmpInfo.m_fieldId, tmpInfo.m_weight);
/*      */ 
/*  654 */       promptList.setColumnInfo(infos[i]);
/*  655 */       columns.append(data.m_columns[i]);
/*  656 */       columns.append(',');
/*      */     }
/*  658 */     columns.setLength(columns.length() - 1);
/*      */ 
/*  660 */     UdlPanel basePanel = this.m_docView.getList();
/*      */ 
/*  662 */     promptList.setVisibleColumns(columns.toString());
/*  663 */     promptList.init();
/*  664 */     promptList.setDisplayCallbackMap(basePanel.getDisplayCallbackMap());
/*  665 */     promptList.setIDColumn("dID");
/*  666 */     promptList.m_list.m_table.setRowSelectionAllowed(false);
/*  667 */     promptList.refreshListEx(items, selectedItems);
/*      */ 
/*  669 */     String[] tmp = new String[1];
/*  670 */     IdcMessage msg = IdcMessageFactory.lc(data.m_messageKey, new Object[] { Integer.valueOf(num) });
/*  671 */     int result = CommonDialogs.promptMultiselect(this.m_systemInterface, data.m_title, msg, (data.m_promptForMessageField != null) ? tmp : null, "LabelStr", promptList);
/*      */ 
/*  674 */     data.m_promptMessage = tmp[0];
/*      */ 
/*  676 */     if (result != 1)
/*      */     {
/*  678 */       return null;
/*      */     }
/*      */ 
/*  681 */     return promptList;
/*      */   }
/*      */ 
/*      */   public void review(String action)
/*      */   {
/*  689 */     DocActionData data = new DocActionData();
/*  690 */     data.m_items = this.m_docView.getList().getSelectedAsResultSet();
/*      */ 
/*  692 */     data.m_columns = new String[] { "dDocName" };
/*      */ 
/*  694 */     if (action.equals("approve"))
/*      */     {
/*  696 */       data.m_serviceName = "WORKFLOW_APPROVE";
/*  697 */       data.m_actionLabel = "apApproveLabel";
/*  698 */       data.m_title = "apApproveTitle";
/*  699 */       data.m_messageKey = "apApproveMessage";
/*      */     }
/*      */     else
/*      */     {
/*  703 */       data.m_serviceName = "WORKFLOW_REJECT";
/*  704 */       data.m_actionLabel = "apRejectLabel";
/*  705 */       data.m_title = "apRejectTitle";
/*  706 */       data.m_messageKey = "apRejectMessage";
/*      */ 
/*  708 */       data.m_promptForMessageField = "wfRejectMessage";
/*      */     }
/*      */ 
/*  711 */     doAction(action, data);
/*      */   }
/*      */ 
/*      */   public void addRevision(boolean isNew)
/*      */   {
/*  716 */     ExecutionContext cxt = this.m_systemInterface.getExecutionContext();
/*  717 */     String title = LocaleResources.getString("apAddNewItemTitle", cxt);
/*  718 */     Properties data = null;
/*  719 */     String helpPageName = "AddNewDoc";
/*      */ 
/*  721 */     if (!isNew)
/*      */     {
/*  723 */       int index = this.m_docView.getSelectedIndex();
/*  724 */       if (index >= 0)
/*      */       {
/*  726 */         data = this.m_docView.getDataAt(index);
/*  727 */         title = LocaleResources.getString("apAddNewRevisionTitle", cxt, data.getProperty("dDocName"));
/*      */ 
/*  729 */         helpPageName = "AddNewRev";
/*      */       }
/*      */       else
/*      */       {
/*  733 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  737 */     AddDocumentDlg dlg = new AddDocumentDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage(helpPageName));
/*      */ 
/*  740 */     dlg.init(data, false, this, this.m_docProfile);
/*  741 */     if (dlg.prompt() != 1)
/*      */       return;
/*      */     try
/*      */     {
/*  745 */       refreshDocumentList(dlg.getDocID());
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  749 */       MessageBox.reportError(this.m_systemInterface, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateDocInfo()
/*      */   {
/*  756 */     int index = this.m_docView.getSelectedIndex();
/*  757 */     if (index < 0)
/*      */       return;
/*  759 */     ExecutionContext cxt = this.m_systemInterface.getExecutionContext();
/*  760 */     String title = LocaleResources.getString("apUpdateItemInfo", cxt);
/*  761 */     AddDocumentDlg dlg = new AddDocumentDlg(this.m_systemInterface, title, null);
/*      */ 
/*  764 */     Properties data = this.m_docView.getDataAt(index);
/*      */ 
/*  766 */     dlg.init(data, true, this, this.m_docProfile);
/*  767 */     if (dlg.prompt() != 1)
/*      */       return;
/*      */     try
/*      */     {
/*  771 */       refreshDocumentList(dlg.getDocID());
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  775 */       MessageBox.reportError(this.m_systemInterface, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void viewSubscribers()
/*      */   {
/*  783 */     int index = this.m_docView.getSelectedIndex();
/*  784 */     if (index < 0)
/*      */       return;
/*  786 */     Properties data = this.m_docView.getDataAt(index);
/*  787 */     DocumentSubscribersDlg dlg = new DocumentSubscribersDlg(this.m_systemInterface, null, this);
/*  788 */     dlg.init(data);
/*      */   }
/*      */ 
/*      */   public void computeFileInfo()
/*      */   {
/*  794 */     int index = this.m_docView.getSelectedIndex();
/*  795 */     if (index < 0)
/*      */       return;
/*  797 */     Properties data = this.m_docView.getDataAt(index);
/*  798 */     FileInfoDlg dlg = new FileInfoDlg(this.m_systemInterface, null, this);
/*  799 */     dlg.init(data);
/*      */   }
/*      */ 
/*      */   public void actionCommand(String command)
/*      */   {
/*  805 */     DocActionData data = new DocActionData();
/*  806 */     data.m_items = this.m_docView.getList().getSelectedAsResultSet();
/*      */ 
/*  808 */     if (command.equals("resubmit"))
/*      */     {
/*  810 */       data = new DocActionData()
/*      */       {
/*      */         public IdcMessage buildMessage(DataBinder binder, int count, int total)
/*      */         {
/*  815 */           String cnvStatus = binder.getLocal("dProcessingState");
/*  816 */           if ((cnvStatus != null) && (((cnvStatus.equalsIgnoreCase("F")) || (cnvStatus.equalsIgnoreCase("P")))))
/*      */           {
/*  819 */             return IdcMessageFactory.lc("apResubmitMessage2", new Object[] { binder.getLocal("dDocName") });
/*      */           }
/*  821 */           return IdcMessageFactory.lc(this.m_messageKey, new Object[] { binder.getLocal("dDocName") });
/*      */         }
/*      */       };
/*  824 */       data.m_items = this.m_docView.getList().getSelectedAsResultSet();
/*  825 */       data.m_serviceName = "RESUBMIT_FOR_CONVERSION";
/*  826 */       data.m_actionLabel = "apResubmitLabel";
/*      */ 
/*  828 */       data.m_columns = new String[] { "dDocName", "dDocTitle", "dProcessingState" };
/*      */ 
/*  830 */       data.m_title = "apResubmitTitle";
/*  831 */       data.m_messageKey = "apResubmitMessage";
/*      */ 
/*  833 */       data.m_promptField = "dProcessingState";
/*  834 */       data.m_promptValue = "Y";
/*  835 */       data.m_promptOverrideName = "AlwaysResubmit";
/*  836 */       data.m_promptOverrideValue = "true";
/*  837 */       data.m_promptEach = true;
/*      */     }
/*  839 */     else if (command.equals("deleteRevision"))
/*      */     {
/*  841 */       data.m_serviceName = "DELETE_REV_EX";
/*  842 */       data.m_actionLabel = "apDeleteRevLabel";
/*      */ 
/*  844 */       data.m_columns = new String[] { "dDocName", "dDocTitle", "dRevLabel" };
/*      */ 
/*  846 */       data.m_title = "apDeleteRevTitle";
/*  847 */       data.m_messageKey = "apDeleteRevMessage";
/*      */     }
/*  850 */     else if (command.equals("deleteDocument"))
/*      */     {
/*  852 */       data.m_serviceName = "DELETE_DOC";
/*  853 */       data.m_actionLabel = "apDeleteLabel";
/*  854 */       data.m_eliminateDuplicateDocs = true;
/*      */ 
/*  856 */       data.m_columns = new String[] { "dDocName", "dDocTitle", "dRevLabel" };
/*      */ 
/*  858 */       data.m_title = "apDeleteTitle";
/*  859 */       data.m_messageKey = "apDeleteMessage";
/*      */     }
/*  861 */     else if (command.equals("checkOut"))
/*      */     {
/*  863 */       data.m_serviceName = "CHECKOUT";
/*  864 */       data.m_actionLabel = "apCheckOutLabel";
/*  865 */       data.m_eliminateDuplicateDocs = false;
/*      */ 
/*  867 */       data.m_columns = new String[] { "dDocName", "dDocTitle", "dRevLabel" };
/*      */ 
/*  869 */       data.m_title = "apCheckOutTitle";
/*  870 */       data.m_messageKey = "apCheckOutMessage";
/*      */     }
/*  872 */     else if (command.equals("undoCheckOut"))
/*      */     {
/*  874 */       data.m_serviceName = "UNDO_CHECKOUT";
/*  875 */       data.m_actionLabel = "apUndoCheckOutLabel";
/*  876 */       data.m_eliminateDuplicateDocs = false;
/*      */ 
/*  878 */       data.m_columns = new String[] { "dDocName", "dDocTitle", "dCheckoutUser" };
/*      */ 
/*  880 */       data.m_title = "apUndoCheckOutTitle";
/*  881 */       data.m_messageKey = "apUndoCheckOutMessage";
/*      */     }
/*      */     else
/*      */     {
/*  886 */       return;
/*      */     }
/*      */ 
/*  889 */     doAction(command, data);
/*      */   }
/*      */ 
/*      */   public void checkSelection()
/*      */   {
/*  895 */     boolean isAllDisabled = false;
/*  896 */     boolean isMultiSelect = false;
/*  897 */     Properties props = null;
/*      */ 
/*  899 */     int[] indices = this.m_docView.getSelectedIndexes();
/*  900 */     if (indices.length == 0)
/*      */     {
/*  902 */       isAllDisabled = true;
/*      */     }
/*  904 */     else if (indices.length == 1)
/*      */     {
/*  906 */       props = this.m_docView.getDataAt(indices[0]);
/*      */     }
/*      */     else
/*      */     {
/*  910 */       isMultiSelect = true;
/*      */     }
/*      */ 
/*  913 */     enableDisableButtons(isAllDisabled, props);
/*      */ 
/*  915 */     Vector docProps = new IdcVector();
/*  916 */     JPopupMenu popup = this.m_docView.getPopupMenu();
/*  917 */     MenuElement[] popupElements = popup.getSubElements();
/*  918 */     MenuElement[] menuElements = this.m_fMenu.getSubElements()[0].getSubElements();
/*  919 */     for (int i = 0; i < popupElements.length; ++i)
/*      */     {
/*  921 */       JMenuItem pmi = (JMenuItem)popupElements[i];
/*  922 */       JMenuItem dmi = (JMenuItem)menuElements[i];
/*  923 */       String actCommand = pmi.getActionCommand();
/*  924 */       boolean flag = true;
/*  925 */       if (isAllDisabled)
/*      */       {
/*  927 */         flag = false;
/*      */       }
/*  929 */       else if (isMultiSelect)
/*      */       {
/*  931 */         String[] disableList = { "subscribers", "info", "addRevision", "update", "testFS" };
/*  932 */         for (int j = 0; j < disableList.length; ++j)
/*      */         {
/*  934 */           if (!actCommand.equals(disableList[j]))
/*      */             continue;
/*  936 */           flag = false;
/*  937 */           break;
/*      */         }
/*      */ 
/*  940 */         if (flag)
/*      */         {
/*  942 */           boolean isAllowed = false;
/*  943 */           int numDocs = indices.length;
/*  944 */           for (int j = 0; j < numDocs; ++j)
/*      */           {
/*  946 */             Properties dProps = null;
/*  947 */             if (docProps.size() < j)
/*      */             {
/*  949 */               dProps = (Properties)docProps.elementAt(j);
/*      */             }
/*      */             else
/*      */             {
/*  954 */               dProps = this.m_docView.getDataAt(indices[j]);
/*  955 */               docProps.addElement(dProps);
/*      */             }
/*      */ 
/*  958 */             if (!isActionAllowed(actCommand, dProps))
/*      */               continue;
/*  960 */             isAllowed = true;
/*  961 */             break;
/*      */           }
/*      */ 
/*  964 */           flag = isAllowed;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  969 */         flag = isActionAllowed(actCommand, props);
/*      */       }
/*      */ 
/*  972 */       menuSetEnabled(pmi, dmi, flag);
/*      */     }
/*      */ 
/*  975 */     updateAccessibilityInfo(this.m_systemInterface.getExecutionContext());
/*      */   }
/*      */ 
/*      */   protected boolean isActionAllowed(String action, Properties props)
/*      */   {
/*  980 */     boolean result = true;
/*      */ 
/*  982 */     String status = props.getProperty("dStatus");
/*  983 */     String pState = props.getProperty("dPublishState");
/*      */ 
/*  985 */     boolean isDeleted = status.equals("DELETED");
/*  986 */     if (isDeleted)
/*      */     {
/*  988 */       result = false;
/*      */     }
/*  990 */     else if ((action.equals("info")) || (action.equals("testFS")))
/*      */     {
/*  992 */       result = true;
/*      */     }
/*  994 */     else if (pState.length() > 0)
/*      */     {
/*  996 */       if (action.equals("approve"))
/*      */       {
/*  998 */         result = pState.equals("W");
/*      */       }
/* 1000 */       else if ((action.equals("deleteRevision")) || (action.equals("deleteDocument")) || (action.equals("resubmit")))
/*      */       {
/* 1003 */         result = true;
/*      */       }
/*      */       else
/*      */       {
/* 1007 */         result = false;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1012 */       String wfState = props.getProperty("dWorkflowState");
/* 1013 */       boolean isInWf = wfState.length() > 0;
/* 1014 */       if (action.equals("addRevision"))
/*      */       {
/* 1016 */         if ((!AppLauncher.getIsStandAlone()) || ((isInWf) && ("R".indexOf(wfState) >= 0)))
/*      */         {
/* 1019 */           result = false;
/*      */         }
/*      */       }
/* 1022 */       else if ((action.equals("checkOut")) || (action.equals("undoCheckOut")))
/*      */       {
/* 1024 */         if ((isInWf) && ("R".indexOf(wfState) >= 0))
/*      */         {
/* 1026 */           result = false;
/*      */         }
/*      */         else
/*      */         {
/* 1030 */           boolean isCheckedout = StringUtils.convertToBool(props.getProperty("dIsCheckedOut"), false);
/* 1031 */           if (action.equals("checkOut"))
/*      */           {
/* 1033 */             result = !isCheckedout;
/*      */           }
/*      */           else
/*      */           {
/* 1037 */             result = isCheckedout;
/*      */           }
/*      */         }
/*      */       }
/* 1041 */       else if ((action.equals("approve")) || (action.equals("reject")))
/*      */       {
/* 1043 */         result = status.equalsIgnoreCase("REVIEW");
/*      */       }
/* 1045 */       else if (action.equals("update"))
/*      */       {
/* 1047 */         boolean isGenWWW = status.equalsIgnoreCase("GenWWW");
/* 1048 */         result = (((!isInWf) || ("E".indexOf(wfState) < 0))) && (!isGenWWW);
/*      */       }
/*      */     }
/* 1051 */     return result;
/*      */   }
/*      */ 
/*      */   protected void enableDisableButtons(boolean isAllDisabled, Properties props)
/*      */   {
/* 1056 */     boolean hasDelete = true;
/* 1057 */     boolean hasAdd = true;
/*      */ 
/* 1059 */     if (isAllDisabled)
/*      */     {
/* 1061 */       hasDelete = false;
/* 1062 */       hasAdd = false;
/*      */     }
/* 1064 */     else if (props != null)
/*      */     {
/* 1066 */       String status = props.getProperty("dStatus");
/* 1067 */       String publishState = props.getProperty("dPublishState");
/* 1068 */       String wfState = props.getProperty("dWorkflowState");
/* 1069 */       if (status.equals("DELETED"))
/*      */       {
/* 1071 */         hasDelete = false;
/* 1072 */         hasAdd = false;
/*      */       }
/* 1074 */       else if (publishState.length() > 0)
/*      */       {
/* 1076 */         hasAdd = false;
/*      */       }
/* 1078 */       else if (wfState.equals("R"))
/*      */       {
/* 1080 */         hasAdd = false;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1085 */       hasAdd = false;
/*      */     }
/*      */ 
/* 1088 */     if (this.m_addDocBtn != null)
/*      */     {
/* 1090 */       this.m_addDocBtn.setEnabled(hasAdd);
/*      */     }
/* 1092 */     for (int i = 0; i < this.m_deleteBtn.length; ++i)
/*      */     {
/* 1094 */       this.m_deleteBtn[i].setEnabled(hasDelete);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void menuSetEnabled(JMenuItem pmi, JMenuItem dmi, boolean state)
/*      */   {
/* 1100 */     pmi.setEnabled(state);
/* 1101 */     dmi.setEnabled(state);
/*      */   }
/*      */ 
/*      */   public void focusGained(FocusEvent event)
/*      */   {
/* 1107 */     this.m_fMenu.setEnabled(true);
/* 1108 */     checkSelection();
/*      */   }
/*      */ 
/*      */   public void focusLost(FocusEvent event)
/*      */   {
/* 1113 */     int numItems = this.m_fMenu.getItemCount();
/* 1114 */     for (int i = 0; i < numItems; ++i)
/*      */     {
/* 1116 */       JMenuItem item = this.m_fMenu.getItem(i);
/* 1117 */       if (item == null)
/*      */         continue;
/* 1119 */       item.setEnabled(false);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1126 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92435 $"; } 
/*      */   static class DocActionData { public static final int NO = 0;
/*      */     public static final int YES = 1;
/*      */     public static final int PROMPT = 2;
/*      */     public String m_serviceName;
/*      */     public DataResultSet m_items;
/*      */     public String m_actionLabel;
/*      */     public String m_title;
/*      */     public String m_messageKey;
/*      */     public boolean m_eliminateDuplicateDocs;
/*      */     public String[] m_columns;
/*      */     public String m_promptField;
/*      */     public String m_promptValue;
/*      */     public boolean m_promptReverse;
/*      */     public String m_promptOverrideName;
/*      */     public String m_promptOverrideValue;
/*      */     public String m_promptForMessageField;
/*      */     public String m_promptMessage;
/*      */     public boolean m_promptSingle;
/*      */     public boolean m_promptMultiple;
/*      */     public boolean m_promptEach;
/*      */ 
/* 1136 */     DocActionData() { this.m_serviceName = null;
/* 1137 */       this.m_items = null;
/*      */ 
/* 1140 */       this.m_actionLabel = null;
/* 1141 */       this.m_title = null;
/* 1142 */       this.m_messageKey = null;
/* 1143 */       this.m_eliminateDuplicateDocs = false;
/*      */ 
/* 1145 */       this.m_columns = null;
/*      */ 
/* 1147 */       this.m_promptField = null;
/* 1148 */       this.m_promptValue = null;
/* 1149 */       this.m_promptReverse = false;
/* 1150 */       this.m_promptOverrideName = null;
/* 1151 */       this.m_promptOverrideValue = null;
/*      */ 
/* 1153 */       this.m_promptForMessageField = null;
/* 1154 */       this.m_promptMessage = null;
/*      */ 
/* 1156 */       this.m_promptSingle = true;
/* 1157 */       this.m_promptMultiple = true;
/* 1158 */       this.m_promptEach = false; }
/*      */ 
/*      */     public boolean isPrompt(Properties props)
/*      */     {
/* 1162 */       boolean result = false;
/* 1163 */       if (this.m_promptField != null)
/*      */       {
/* 1165 */         String value = props.getProperty(this.m_promptField);
/* 1166 */         result = (this.m_promptValue != null) && (value.equals(this.m_promptValue));
/*      */       }
/*      */ 
/* 1169 */       if (this.m_promptReverse)
/*      */       {
/* 1171 */         return !result;
/*      */       }
/* 1173 */       return result;
/*      */     }
/*      */ 
/*      */     public IdcMessage buildMessage(DataBinder binder, int count, int total)
/*      */     {
/* 1180 */       IdcMessage msg = IdcMessageFactory.lc(this.m_messageKey, new Object[] { binder.getLocal("dDocName") });
/* 1181 */       return msg;
/*      */     } }
/*      */ 
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.DocManPanel
 * JD-Core Version:    0.5.4
 */