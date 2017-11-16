/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.GuiUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.CommonDialogs;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.workflow.WorkflowData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.CardLayout;
/*     */ import java.awt.Cursor;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class WorkflowPanel extends WfBasePanel
/*     */ {
/*  70 */   protected DataResultSet m_workflows = null;
/*     */ 
/*  73 */   protected String m_startBtnLabel = null;
/*  74 */   protected String m_cancelBtnLabel = null;
/*  75 */   protected JButton m_startBtn = null;
/*  76 */   protected JButton m_cancelBtn = null;
/*     */ 
/*  78 */   protected UdlPanel m_workflowList = null;
/*  79 */   protected EditViewBase m_curView = null;
/*     */ 
/*  81 */   protected JPanel m_flipPanel = null;
/*  82 */   protected Hashtable m_flipComponents = null;
/*     */ 
/*  84 */   protected String[][] m_displayMap = (String[][])null;
/*  85 */   protected Hashtable m_viewsWorkflowInfo = null;
/*     */ 
/*     */   public WorkflowPanel()
/*     */   {
/*  89 */     this.m_startBtnLabel = "apTitleStart";
/*  90 */     this.m_cancelBtnLabel = "apLabelCancel";
/*     */ 
/*  92 */     String[][] displayMap = { { "INIT", "apTitleInactive" }, { "INPROCESS", "apTitleActive" } };
/*     */ 
/*  98 */     this.m_displayMap = displayMap;
/*  99 */     this.m_viewsWorkflowInfo = new Hashtable();
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys, WorkflowContext ctxt)
/*     */     throws ServiceException
/*     */   {
/* 105 */     super.init(sys, ctxt);
/* 106 */     LocaleResources.localizeDoubleArray(this.m_displayMap, this.m_cxt, 1);
/*     */ 
/* 109 */     refreshList();
/*     */   }
/*     */ 
/*     */   public Insets getInsets()
/*     */   {
/* 115 */     return new Insets(5, 10, 5, 10);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/* 121 */     JPanel wfPanel = initWorkflowList();
/* 122 */     JPanel infoPanel = initInfoPanel();
/*     */ 
/* 124 */     this.m_helper.makePanelGridBag(this, 1);
/* 125 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 126 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 127 */     this.m_helper.addComponent(this, wfPanel);
/* 128 */     this.m_helper.addComponent(this, infoPanel);
/*     */   }
/*     */ 
/*     */   protected JPanel initWorkflowList()
/*     */   {
/* 133 */     createWorkflowList();
/* 134 */     this.m_workflowList.init();
/* 135 */     addDisplayMaps();
/*     */ 
/* 137 */     ItemListener listener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 141 */         WorkflowPanel.this.checkSelection();
/*     */       }
/*     */     };
/* 144 */     this.m_workflowList.m_list.addItemListener(listener);
/* 145 */     this.m_workflowList.useDefaultListener();
/*     */ 
/* 148 */     JPanel btnPanel = new PanePanel();
/* 149 */     JButton addBtn = new JButton(LocaleResources.getString("apDlgButtonAdd", this.m_cxt));
/* 150 */     addBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apTitleAddNewWorkflow", this.m_cxt));
/* 151 */     btnPanel.add(addBtn);
/* 152 */     JButton editBtn = this.m_workflowList.addButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt), true);
/*     */ 
/* 154 */     editBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableButtonEditWorkflow", this.m_cxt));
/* 155 */     btnPanel.add(editBtn);
/* 156 */     JButton deleteBtn = this.m_workflowList.addButton(LocaleResources.getString("apLabelDelete", this.m_cxt), true);
/* 157 */     deleteBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableButtonDeleteWorkflow", this.m_cxt));
/* 158 */     btnPanel.add(deleteBtn);
/*     */ 
/* 160 */     this.m_workflowList.add("South", btnPanel);
/*     */ 
/* 163 */     ActionListener addListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 167 */         WorkflowPanel.this.addOrEditWorkflow(true);
/*     */       }
/*     */     };
/* 170 */     addBtn.addActionListener(addListener);
/*     */ 
/* 172 */     ActionListener editListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 176 */         WorkflowPanel.this.addOrEditWorkflow(false);
/*     */       }
/*     */     };
/* 179 */     editBtn.addActionListener(editListener);
/* 180 */     this.m_workflowList.m_list.addActionListener(editListener);
/*     */ 
/* 182 */     ActionListener deleteListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 186 */         int index = WorkflowPanel.this.m_workflowList.getSelectedIndex();
/* 187 */         if (index < 0)
/*     */         {
/* 190 */           WorkflowPanel.this.reportError(null, IdcMessageFactory.lc("apSelectWorkflowToDelete", new Object[0]));
/* 191 */           return;
/*     */         }
/* 193 */         WorkflowPanel.this.deleteWorkflow(index);
/*     */       }
/*     */     };
/* 196 */     deleteBtn.addActionListener(deleteListener);
/*     */ 
/* 199 */     JPanel wfWrapper = new PanePanel();
/* 200 */     this.m_helper.makePanelGridBag(wfWrapper, 1);
/* 201 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 202 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 203 */     this.m_helper.addComponent(wfWrapper, this.m_workflowList);
/*     */ 
/* 205 */     return wfWrapper;
/*     */   }
/*     */ 
/*     */   protected void createWorkflowList()
/*     */   {
/* 210 */     this.m_workflowList = new UdlPanel(LocaleResources.getString("apTitleCurrentWorkflows", this.m_cxt), null, 300, 20, "Workflows", true);
/*     */ 
/* 212 */     String fieldList = "dWfName,dWfStatus,dCompletionDate,dSecurityGroup";
/* 213 */     this.m_workflowList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleName", this.m_cxt), "dWfName", 4.0D));
/*     */ 
/* 215 */     this.m_workflowList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelStatus", this.m_cxt), "dWfStatus", 3.0D));
/*     */ 
/* 217 */     this.m_workflowList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleCompletedAt", this.m_cxt), "dCompletionDate", 3.0D));
/*     */ 
/* 219 */     this.m_workflowList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleSecurityGroup", this.m_cxt), "dSecurityGroup", 4.0D));
/*     */ 
/* 221 */     this.m_workflowList.setVisibleColumns(fieldList);
/* 222 */     this.m_workflowList.setIDColumn("dWfName");
/*     */   }
/*     */ 
/*     */   protected void addDisplayMaps()
/*     */   {
/* 227 */     DisplayStringCallback dsc = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 233 */         return StringUtils.getPresentationString(WorkflowPanel.this.m_displayMap, value);
/*     */       }
/*     */     };
/* 236 */     this.m_workflowList.setDisplayCallback("dWfStatus", dsc);
/*     */   }
/*     */ 
/*     */   protected void addOrEditWorkflow(boolean isAdd)
/*     */   {
/* 241 */     Properties props = null;
/* 242 */     String title = LocaleResources.getString("apTitleAddNewWorkflow", this.m_cxt);
/* 243 */     String helpPageName = "AddNewWorkflow";
/*     */ 
/* 245 */     if (!isAdd)
/*     */     {
/* 247 */       int index = this.m_workflowList.getSelectedIndex();
/* 248 */       if (index < 0)
/*     */       {
/* 250 */         reportError(IdcMessageFactory.lc("apSelectWorkflowToEdit", new Object[0]));
/* 251 */         return;
/*     */       }
/* 253 */       props = this.m_workflowList.getDataAt(index);
/* 254 */       String wfName = props.getProperty("dWfName");
/* 255 */       title = LocaleResources.getString("apTitleEditWorkflow", this.m_cxt, wfName);
/* 256 */       helpPageName = "EditWorkflow";
/*     */ 
/* 258 */       String state = props.getProperty("dWfStatus");
/* 259 */       if (!state.equalsIgnoreCase("INIT"))
/*     */       {
/* 261 */         MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apCannotEditWorkflowWhileActive", new Object[0]), 1);
/*     */ 
/* 264 */         return;
/*     */       }
/* 266 */       mergeWorkflowInfoProps(wfName, props);
/*     */     }
/*     */ 
/* 269 */     EditWorkflowDlg dlg = new EditWorkflowDlg(this.m_systemInterface, title, this.m_context, this.m_workflowList.getResultSet(), DialogHelpTable.getHelpPage(helpPageName));
/*     */ 
/* 271 */     if ((!dlg.init(props)) || 
/* 273 */       (dlg.prompt() != 1))
/*     */       return;
/*     */     try
/*     */     {
/* 277 */       refreshList(dlg.getWorkflowName());
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 282 */       reportError(exp);
/* 283 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void mergeWorkflowInfoProps(String wfName, Properties props)
/*     */   {
/* 291 */     WorkflowStateInfo workflowInfo = (WorkflowStateInfo)this.m_viewsWorkflowInfo.get(wfName);
/* 292 */     if (workflowInfo == null)
/*     */       return;
/* 294 */     DataBinder data = workflowInfo.getWorkflowData();
/* 295 */     DataBinder.mergeHashTables(props, data.getLocalData());
/*     */   }
/*     */ 
/*     */   protected void deleteWorkflow(int index)
/*     */   {
/* 301 */     Properties props = this.m_workflowList.getDataAt(index);
/* 302 */     String name = props.getProperty("dWfName");
/* 303 */     String state = props.getProperty("dWfStatus");
/* 304 */     IdcMessage msg = IdcMessageFactory.lc("apVerifyWorkflowDelete", new Object[] { name });
/*     */ 
/* 306 */     if (!state.equalsIgnoreCase("INIT"))
/*     */     {
/* 308 */       msg = IdcMessageFactory.lc("apVerifyWorkflowCancelAndDelete", new Object[] { name });
/*     */     }
/*     */ 
/* 311 */     if (MessageBox.doMessage(this.m_systemInterface, msg, 4) != 2) {
/*     */       return;
/*     */     }
/*     */     try
/*     */     {
/* 316 */       DataBinder binder = new DataBinder();
/* 317 */       binder.setLocalData(props);
/*     */ 
/* 319 */       SharedContext shContext = this.m_context.getSharedContext();
/* 320 */       AppContextUtils.executeService(shContext, "DELETE_WORKFLOW", binder, true);
/* 321 */       refreshList(null);
/* 322 */       this.m_workflowList.enableDisable(false);
/* 323 */       checkSelection();
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 327 */       reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected JPanel initInfoPanel()
/*     */   {
/* 334 */     this.m_flipPanel = new PanePanel();
/* 335 */     CardLayout cardLayout = new CardLayout();
/* 336 */     this.m_flipPanel.setLayout(cardLayout);
/* 337 */     this.m_flipComponents = new Hashtable();
/*     */ 
/* 339 */     EditViewBase editView = new EditViewBase();
/* 340 */     this.m_curView = editView;
/* 341 */     addFlipComponent("Empty", editView);
/* 342 */     createFlipComponents();
/* 343 */     cardLayout.show(this.m_flipPanel, "Empty");
/*     */ 
/* 346 */     JPanel controlBtnPanel = new PanePanel();
/* 347 */     this.m_startBtn = new JButton(LocaleResources.getString(this.m_startBtnLabel, this.m_cxt));
/* 348 */     this.m_startBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableButtonStartWorkflow", this.m_cxt));
/* 349 */     controlBtnPanel.add(this.m_startBtn);
/*     */ 
/* 351 */     this.m_cancelBtn = new JButton(LocaleResources.getString(this.m_cancelBtnLabel, this.m_cxt));
/* 352 */     this.m_cancelBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableButtonDisableWorkflow", this.m_cxt));
/* 353 */     controlBtnPanel.add(this.m_cancelBtn);
/*     */ 
/* 355 */     ActionListener wfListener = createWfButtonListener();
/* 356 */     this.m_startBtn.addActionListener(wfListener);
/* 357 */     this.m_cancelBtn.addActionListener(wfListener);
/*     */ 
/* 360 */     enableDisableWfButtons(false);
/*     */ 
/* 362 */     JPanel wrapper = new PanePanel();
/* 363 */     wrapper.setLayout(new BorderLayout());
/* 364 */     wrapper.add("North", new CustomLabel("    "));
/* 365 */     wrapper.add("Center", this.m_flipPanel);
/* 366 */     wrapper.add("South", controlBtnPanel);
/*     */ 
/* 368 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected void createFlipComponents()
/*     */   {
/* 373 */     addFlipComponent("Basic", new WfEditView());
/*     */   }
/*     */ 
/*     */   protected void addFlipComponent(String compId, EditViewBase comp)
/*     */   {
/* 378 */     comp.init(this.m_systemInterface, this.m_context);
/* 379 */     this.m_flipPanel.add(compId, comp);
/* 380 */     this.m_flipComponents.put(compId, comp);
/*     */   }
/*     */ 
/*     */   protected ActionListener createWfButtonListener()
/*     */   {
/* 385 */     ActionListener wfListener = new Object()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 389 */         int index = WorkflowPanel.this.m_workflowList.getSelectedIndex();
/* 390 */         if (index < 0)
/*     */         {
/* 392 */           return;
/*     */         }
/* 394 */         String action = "WORKFLOW_START";
/* 395 */         Properties props = WorkflowPanel.this.m_workflowList.getDataAt(index);
/* 396 */         Object src = e.getSource();
/* 397 */         if (src == WorkflowPanel.this.m_cancelBtn)
/*     */         {
/* 399 */           action = "WORKFLOW_CANCEL";
/* 400 */           if (MessageBox.doMessage(WorkflowPanel.this.m_systemInterface, IdcMessageFactory.lc("apVerifyWorkflowCancel", new Object[0]), 4) == 3)
/*     */           {
/* 404 */             return;
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 410 */           String title = LocaleResources.getString("apTitleStartWorkflow", WorkflowPanel.this.m_cxt, WorkflowPanel.this.m_workflowList.getSelectedObj());
/*     */ 
/* 412 */           String msg = LocaleResources.getString("apWorkflowStartMessageDesc", WorkflowPanel.this.m_cxt);
/*     */ 
/* 414 */           if (CommonDialogs.promptForLargeText(WorkflowPanel.this.m_systemInterface, title, msg, props, LocaleResources.getString("apLabelMessage", WorkflowPanel.this.m_cxt), "wfMessage") == 0)
/*     */           {
/* 418 */             return;
/*     */           }
/*     */         }
/*     */ 
/* 422 */         DataBinder binder = new DataBinder();
/* 423 */         binder.setLocalData(props);
/*     */         try
/*     */         {
/* 426 */           SharedContext shContext = WorkflowPanel.this.m_context.getSharedContext();
/* 427 */           AppContextUtils.executeService(shContext, action, binder, true);
/* 428 */           AppLauncher.notifyInternalSubjectChange("documents");
/* 429 */           String selectedObj = WorkflowPanel.this.m_workflowList.getSelectedObj();
/* 430 */           WorkflowPanel.this.refreshList(selectedObj);
/* 431 */           WorkflowPanel.this.checkSelectionEx(true);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 435 */           WorkflowPanel.this.reportError(exp);
/*     */         }
/*     */       }
/*     */     };
/* 439 */     return wfListener;
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 444 */     checkSelectionEx(false);
/*     */   }
/*     */ 
/*     */   protected void checkSelectionEx(boolean refreshAll)
/*     */   {
/* 451 */     CardLayout panelHandler = (CardLayout)this.m_flipPanel.getLayout();
/* 452 */     int index = this.m_workflowList.getSelectedIndex();
/* 453 */     if (index < 0)
/*     */     {
/* 455 */       this.m_curView = ((EditViewBase)this.m_flipComponents.get("Empty"));
/* 456 */       panelHandler.show(this.m_flipPanel, "Empty");
/* 457 */       enableDisableWfButtons(false);
/* 458 */       return;
/*     */     }
/*     */ 
/* 461 */     Properties props = this.m_workflowList.getDataAt(index);
/* 462 */     String wfName = props.getProperty("dWfName");
/* 463 */     String state = props.getProperty("dWfStatus");
/*     */ 
/* 465 */     boolean isWfInit = state.equalsIgnoreCase("INIT");
/* 466 */     this.m_startBtn.setEnabled(isWfInit);
/* 467 */     this.m_cancelBtn.setEnabled(!isWfInit);
/*     */ 
/* 470 */     WorkflowStateInfo curWorkflow = this.m_curView.getWorkflowInfo();
/*     */ 
/* 472 */     boolean wfSelChanged = (curWorkflow == null) || (!wfName.equals(curWorkflow.m_wfName));
/*     */ 
/* 474 */     if ((!wfSelChanged) && (!refreshAll))
/*     */     {
/* 480 */       return;
/*     */     }
/*     */ 
/* 483 */     if (wfSelChanged)
/*     */     {
/* 485 */       String type = props.getProperty("dWfType");
/* 486 */       EditViewBase editView = (EditViewBase)this.m_flipComponents.get(type);
/* 487 */       panelHandler.show(this.m_flipPanel, type);
/* 488 */       this.m_curView = editView;
/*     */     }
/*     */ 
/* 491 */     this.m_curView.createWorkflowInfo(props);
/* 492 */     this.m_curView.load();
/* 493 */     WorkflowStateInfo newCurWorkflow = this.m_curView.getWorkflowInfo();
/* 494 */     this.m_viewsWorkflowInfo.put(newCurWorkflow.m_wfName, newCurWorkflow);
/*     */   }
/*     */ 
/*     */   protected void setWorkflows()
/*     */   {
/* 499 */     WorkflowData workflows = (WorkflowData)SharedObjects.getTable("App" + WorkflowData.m_tableName);
/* 500 */     this.m_workflows = workflows.getBasicWorkflows();
/*     */   }
/*     */ 
/*     */   public void refreshList(String selectedObj)
/*     */   {
/* 505 */     Cursor prevCursor = GuiUtils.setBusy(this.m_systemInterface);
/*     */     try
/*     */     {
/* 508 */       this.m_context.refreshWorkflows();
/* 509 */       setWorkflows();
/* 510 */       this.m_workflowList.refreshList(this.m_workflows, selectedObj);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 514 */       Report.trace(null, LocaleResources.getString("apUnableToRefreshWorkflowList", this.m_cxt), e);
/*     */     }
/*     */     finally
/*     */     {
/* 518 */       GuiUtils.setCursor(this.m_systemInterface, prevCursor);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void refreshList()
/*     */   {
/* 524 */     refreshList(null);
/*     */   }
/*     */ 
/*     */   protected void enableDisableWfButtons(boolean isEnabled)
/*     */   {
/* 529 */     this.m_startBtn.setEnabled(isEnabled);
/* 530 */     this.m_cancelBtn.setEnabled(isEnabled);
/*     */   }
/*     */ 
/*     */   public void update()
/*     */   {
/* 535 */     String name = this.m_workflowList.getSelectedObj();
/* 536 */     refreshList(name);
/* 537 */     checkSelectionEx(true);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 542 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87480 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.WorkflowPanel
 * JD-Core Version:    0.5.4
 */