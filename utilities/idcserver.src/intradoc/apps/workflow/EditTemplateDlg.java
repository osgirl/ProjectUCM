/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.workflow.WfStepData;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.CardLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditTemplateDlg
/*     */   implements ComponentBinder
/*     */ {
/*  73 */   protected SystemInterface m_systemInterface = null;
/*  74 */   protected ExecutionContext m_cxt = null;
/*  75 */   protected DialogHelper m_helper = null;
/*  76 */   protected WorkflowContext m_context = null;
/*  77 */   protected String m_helpPage = null;
/*     */ 
/*  79 */   protected String m_action = null;
/*     */ 
/*  81 */   protected PanePanel m_flipPanel = null;
/*  82 */   protected Hashtable m_flipComponents = null;
/*  83 */   protected EditViewBase m_curView = null;
/*  84 */   protected int m_curIndex = -1;
/*     */ 
/*  86 */   protected UdlPanel m_stepList = null;
/*  87 */   protected DataBinder m_templateData = null;
/*  88 */   protected WorkflowStateInfo m_workflowInfo = null;
/*     */ 
/*  91 */   protected final String[][] STEP_BUTTONS = { { "apDlgButtonAdd", "add", "0" }, { "apDlgButtonEdit", "edit", "1" }, { "apLabelDelete", "delete", "1" } };
/*     */ 
/* 191 */   protected final Object[][] m_autoContributeStepRevisionOptions = { { ":C:CA:CE:", "!apWfStepType_CE", "", "radioeditgroup" }, { ":C:CA:CN:", "!apWfStepType_CN", "", "radioeditgroup" } };
/*     */ 
/*     */   public EditTemplateDlg(SystemInterface sys, String title, WorkflowContext ctxt, String helpPage)
/*     */   {
/* 101 */     this.m_systemInterface = sys;
/* 102 */     this.m_cxt = sys.getExecutionContext();
/* 103 */     this.m_helper = new DialogHelper(sys, title, true);
/* 104 */     this.m_context = ctxt;
/* 105 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(DataBinder data, boolean isNew)
/*     */   {
/* 110 */     if (isNew)
/*     */     {
/* 112 */       this.m_action = "ADD_WF_TEMPLATE";
/*     */     }
/*     */     else
/*     */     {
/* 116 */       this.m_action = "EDIT_WF_TEMPLATE";
/*     */     }
/*     */ 
/* 119 */     setTemplateData(data);
/*     */ 
/* 121 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 128 */           EditTemplateDlg.this.updateStepInfo();
/* 129 */           EditTemplateDlg.this.validateStepInfo();
/*     */ 
/* 131 */           SharedContext shContext = EditTemplateDlg.this.m_context.getSharedContext();
/* 132 */           AppContextUtils.executeService(shContext, EditTemplateDlg.this.m_action, EditTemplateDlg.this.m_templateData);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 136 */           MessageBox.reportError(EditTemplateDlg.this.m_systemInterface, exp);
/* 137 */           return false;
/*     */         }
/* 139 */         return true;
/*     */       }
/*     */     };
/* 143 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 0, true, this.m_helpPage);
/*     */ 
/* 146 */     String name = this.m_templateData.getLocal("dWfTemplateName");
/* 147 */     initUI(mainPanel, name);
/*     */ 
/* 149 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void setTemplateData(DataBinder data)
/*     */   {
/* 154 */     if (data == null)
/*     */     {
/* 156 */       data = new DataBinder();
/*     */     }
/* 158 */     this.m_templateData = data;
/* 159 */     this.m_helper.m_props = data.getLocalData();
/*     */ 
/* 161 */     DataResultSet rset = (DataResultSet)data.getResultSet("WorkflowSteps");
/* 162 */     if (rset == null)
/*     */     {
/* 164 */       rset = new DataResultSet(WfStepData.getTemplateColumns());
/* 165 */       data.addResultSet("WorkflowSteps", rset);
/*     */     }
/*     */ 
/* 169 */     this.m_workflowInfo = new WorkflowStateInfo(data.getLocalData(), "dWfStepName", "dWfStepType");
/* 170 */     this.m_workflowInfo.setWorkflowData(this.m_templateData);
/* 171 */     this.m_workflowInfo.setValue("IsTemplateScript", "1");
/*     */   }
/*     */ 
/*     */   protected void initUI(JPanel mainPanel, String name)
/*     */   {
/* 176 */     JPanel namePanel = initNamePanel(name);
/* 177 */     JPanel stepsPanel = initStepsPanel();
/* 178 */     JPanel stepInfoPanel = initStepInfoPanel();
/*     */ 
/* 180 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 181 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 182 */     this.m_helper.addLastComponentInRow(mainPanel, namePanel);
/* 183 */     this.m_helper.addComponent(mainPanel, stepsPanel);
/* 184 */     this.m_helper.addComponent(mainPanel, stepInfoPanel);
/*     */ 
/* 187 */     this.m_stepList.enableDisable(false);
/*     */ 
/* 189 */     this.m_stepList.refreshList(this.m_templateData, null);
/*     */   }
/*     */ 
/*     */   protected JPanel initNamePanel(String name)
/*     */   {
/* 199 */     JPanel pnl = new CustomPanel();
/* 200 */     this.m_helper.makePanelGridBag(pnl, 1);
/*     */ 
/* 202 */     Component nameCmp = null;
/* 203 */     if (name == null)
/*     */     {
/* 205 */       nameCmp = new CustomTextField(20);
/*     */     }
/*     */     else
/*     */     {
/* 209 */       nameCmp = new CustomLabel(name);
/*     */     }
/*     */ 
/* 212 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelTemplateName", this.m_cxt), nameCmp, "dWfTemplateName");
/*     */ 
/* 214 */     this.m_helper.addLabelEditPair(pnl, LocaleResources.getString("apLabelDescription", this.m_cxt), 40, "dWfTemplateDescription");
/*     */ 
/* 224 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected JPanel initStepsPanel()
/*     */   {
/* 229 */     this.m_stepList = new UdlPanel(LocaleResources.getString("apTitleSteps", this.m_cxt), null, 200, 10, "WorkflowSteps", false);
/*     */ 
/* 231 */     this.m_stepList.setVisibleColumns("dWfStepName,dWfStepType");
/* 232 */     this.m_stepList.setDisplayCallback("dWfStepType", this.m_context.createStringCallback());
/* 233 */     this.m_stepList.init();
/* 234 */     this.m_stepList.useDefaultListener();
/*     */ 
/* 236 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 240 */         int state = e.getStateChange();
/* 241 */         switch (state)
/*     */         {
/*     */         case 2:
/* 244 */           EditTemplateDlg.this.updateInfo();
/* 245 */           break;
/*     */         case 1:
/* 247 */           EditTemplateDlg.this.checkSelection();
/*     */         }
/*     */       }
/*     */     };
/* 252 */     this.m_stepList.addItemListener(iListener);
/*     */ 
/* 254 */     ActionListener aListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 258 */         EditTemplateDlg.this.addOrEditStep(false);
/*     */       }
/*     */     };
/* 261 */     this.m_stepList.m_list.addActionListener(aListener);
/*     */ 
/* 265 */     ActionListener listener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 269 */         String cmd = e.getActionCommand();
/* 270 */         if (cmd.equals("add"))
/*     */         {
/* 272 */           EditTemplateDlg.this.addOrEditStep(true);
/*     */         }
/* 274 */         else if (cmd.equals("edit"))
/*     */         {
/* 276 */           EditTemplateDlg.this.addOrEditStep(false);
/*     */         } else {
/* 278 */           if (!cmd.equals("delete"))
/*     */             return;
/* 280 */           EditTemplateDlg.this.deleteStep();
/*     */         }
/*     */       }
/*     */     };
/* 286 */     JPanel btnPanel = new PanePanel();
/* 287 */     addCommandButtons(this.STEP_BUTTONS, btnPanel, listener);
/*     */ 
/* 289 */     this.m_stepList.add("South", btnPanel);
/*     */ 
/* 291 */     JPanel wrapper = new CustomPanel();
/* 292 */     wrapper.setLayout(new BorderLayout());
/* 293 */     wrapper.add("Center", this.m_stepList);
/*     */ 
/* 295 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected JPanel initStepInfoPanel()
/*     */   {
/* 303 */     this.m_flipPanel = new PanePanel();
/* 304 */     CardLayout cardLayout = new CardLayout();
/* 305 */     this.m_flipPanel.setLayout(cardLayout);
/* 306 */     this.m_flipComponents = new Hashtable();
/*     */ 
/* 309 */     EditViewBase editView = new EditViewBase();
/* 310 */     this.m_curView = editView;
/* 311 */     addFlipComponent("Empty", editView);
/* 312 */     addFlipComponent(":R:", new EditTemplateStepView());
/* 313 */     cardLayout.show(this.m_flipPanel, "Empty");
/*     */ 
/* 315 */     return this.m_flipPanel;
/*     */   }
/*     */ 
/*     */   protected void addFlipComponent(String compId, EditViewBase comp)
/*     */   {
/* 320 */     comp.init(this.m_systemInterface, this.m_context);
/* 321 */     comp.setWorkflowInfo(this.m_workflowInfo);
/* 322 */     this.m_flipPanel.add(compId, comp);
/* 323 */     this.m_flipComponents.put(compId, comp);
/*     */   }
/*     */ 
/*     */   protected void addCommandButtons(String[][] infos, JPanel btnPanel, ActionListener listener)
/*     */   {
/* 328 */     for (int i = 0; i < infos.length; ++i)
/*     */     {
/* 330 */       boolean isControlled = StringUtils.convertToBool(infos[i][2], false);
/* 331 */       JButton btn = this.m_stepList.addButton(LocaleResources.getString(infos[i][0], this.m_cxt), isControlled);
/* 332 */       btn.setActionCommand(infos[i][1]);
/* 333 */       btn.addActionListener(listener);
/* 334 */       btnPanel.add(btn);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 340 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void updateInfo()
/*     */   {
/* 345 */     if (this.m_curIndex < 0)
/*     */     {
/* 347 */       return;
/*     */     }
/*     */ 
/* 350 */     WorkflowStateInfo curStep = this.m_curView.getWorkflowInfo();
/* 351 */     if (curStep != null)
/*     */     {
/* 353 */       updateStepInfo(curStep);
/*     */     }
/* 355 */     checkSelection();
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 362 */     CardLayout panelHandler = (CardLayout)this.m_flipPanel.getLayout();
/* 363 */     this.m_curIndex = this.m_stepList.getSelectedIndex();
/* 364 */     if (this.m_curIndex < 0)
/*     */     {
/* 366 */       this.m_curView = ((EditViewBase)this.m_flipComponents.get("Empty"));
/* 367 */       this.m_workflowInfo.reset();
/* 368 */       panelHandler.show(this.m_flipPanel, "Empty");
/* 369 */       return;
/*     */     }
/*     */ 
/* 372 */     Properties props = getStepDataAt(this.m_curIndex);
/* 373 */     String stepName = props.getProperty("dWfStepName");
/* 374 */     String type = props.getProperty("dWfStepType");
/* 375 */     type = determineStepTypeFlipPanelKey(type);
/*     */ 
/* 378 */     WorkflowStateInfo curStep = this.m_curView.getWorkflowInfo();
/* 379 */     boolean selChanged = (curStep == null) || (!stepName.equals(curStep.m_wfName));
/*     */ 
/* 382 */     if (!selChanged)
/*     */     {
/* 384 */       if (curStep != null)
/*     */       {
/* 386 */         this.m_curView.updateProps(props);
/*     */       }
/* 388 */       return;
/*     */     }
/*     */ 
/* 392 */     updateStepInfo(curStep);
/*     */ 
/* 395 */     this.m_curView = ((EditViewBase)this.m_flipComponents.get(type));
/* 396 */     this.m_curView.updateProps(props);
/* 397 */     this.m_curView.load();
/*     */ 
/* 399 */     panelHandler.show(this.m_flipPanel, type);
/*     */   }
/*     */ 
/*     */   protected String determineStepTypeFlipPanelKey(String type)
/*     */   {
/* 404 */     type = WorkflowScriptUtils.getUpgradedStepType(type);
/* 405 */     if (type.indexOf(":R:") >= 0)
/*     */     {
/* 407 */       return ":R:";
/*     */     }
/* 409 */     return "Empty";
/*     */   }
/*     */ 
/*     */   protected void updateStepList(Properties curStep, boolean isEdit, boolean isNew) throws DataException
/*     */   {
/* 414 */     String tableName = "WorkflowSteps";
/* 415 */     DataResultSet rset = (DataResultSet)this.m_templateData.getResultSet(tableName);
/* 416 */     if (rset == null)
/*     */     {
/* 418 */       throw new DataException(LocaleResources.getString("apTableNotDefined", this.m_cxt, tableName));
/*     */     }
/*     */ 
/* 421 */     String name = curStep.getProperty("dWfStepName");
/*     */ 
/* 423 */     Vector values = rset.findRow(0, name);
/* 424 */     if (isEdit)
/*     */     {
/* 426 */       PropParameters params = new PropParameters(curStep);
/* 427 */       Vector newValues = rset.createRow(params);
/* 428 */       if (values == null)
/*     */       {
/* 430 */         rset.addRow(newValues);
/*     */       }
/* 432 */       else if (!isNew)
/*     */       {
/* 434 */         rset.setRowValues(newValues, rset.getCurrentRow());
/*     */       }
/*     */       else
/*     */       {
/* 439 */         MessageBox.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("apStepNameNotUnique", new Object[0]));
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 444 */       if (values != null)
/*     */       {
/* 446 */         rset.deleteCurrentRow();
/*     */       }
/* 448 */       name = null;
/*     */     }
/*     */ 
/* 451 */     this.m_stepList.refreshList(this.m_templateData, name);
/* 452 */     checkSelection();
/*     */   }
/*     */ 
/*     */   protected void validateStepInfo()
/*     */   {
/* 457 */     DataResultSet rset = (DataResultSet)this.m_templateData.getResultSet("WorkflowSteps");
/* 458 */     if (rset == null)
/*     */     {
/* 460 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 465 */       FieldInfo[] finfo = ResultSetUtils.createInfoList(rset, new String[] { "dWfStepWeight" }, true);
/*     */ 
/* 467 */       int index = finfo[0].m_index;
/* 468 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/* 470 */         String val = rset.getStringValue(index);
/* 471 */         if (val.trim().length() != 0)
/*     */           continue;
/* 473 */         rset.setCurrentValue(index, "0");
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 479 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void updateStepInfo()
/*     */   {
/* 485 */     WorkflowStateInfo curStepData = this.m_curView.getWorkflowInfo();
/* 486 */     updateStepInfo(curStepData);
/*     */   }
/*     */ 
/*     */   protected void updateStepInfo(WorkflowStateInfo curStepData)
/*     */   {
/* 491 */     if (curStepData == null)
/*     */     {
/* 493 */       return;
/*     */     }
/*     */ 
/* 496 */     this.m_curView.updateEdit(curStepData);
/*     */ 
/* 499 */     DataBinder.mergeHashTables(this.m_templateData.getLocalData(), curStepData.m_wfData.getLocalData());
/*     */ 
/* 501 */     DataResultSet rset = (DataResultSet)this.m_templateData.getResultSet("WorkflowSteps");
/* 502 */     if (rset == null)
/*     */     {
/* 504 */       return;
/*     */     }
/*     */ 
/* 507 */     String name = curStepData.get("dWfStepName");
/* 508 */     Vector values = rset.findRow(0, name);
/* 509 */     if (values == null)
/*     */     {
/* 511 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 516 */       values = rset.createRow(curStepData);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 520 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apUnableToAddStep", new Object[0]));
/* 521 */       return;
/*     */     }
/*     */ 
/* 524 */     rset.setRowValues(values, rset.getCurrentRow());
/*     */   }
/*     */ 
/*     */   protected void addOrEditStep(boolean isAdd)
/*     */   {
/* 535 */     Properties props = null;
/* 536 */     String title = LocaleResources.getString("apTitleAddNewStep", this.m_cxt);
/* 537 */     String helpPageName = "AddNewWorkflowTemplateStep";
/*     */ 
/* 539 */     if (!isAdd)
/*     */     {
/* 541 */       int index = this.m_stepList.getSelectedIndex();
/* 542 */       if (index < 0)
/*     */       {
/* 544 */         return;
/*     */       }
/* 546 */       props = getStepDataAt(index);
/* 547 */       title = LocaleResources.getString("apTitleEditStep", this.m_cxt, props.getProperty("dWfStepName"));
/*     */ 
/* 549 */       helpPageName = "EditWorkflowTemplateStep";
/*     */     }
/*     */     else
/*     */     {
/* 553 */       props = new Properties();
/* 554 */       props.put("dWfStepHasWeight", "1");
/* 555 */       props.put("dWfStepWeight", "1");
/*     */     }
/*     */ 
/* 558 */     EditStepDlg dlg = new EditStepDlg(this.m_systemInterface, title, this.m_stepList.getResultSet(), false, DialogHelpTable.getHelpPage(helpPageName));
/*     */ 
/* 560 */     WorkflowStateInfo stepInfo = new WorkflowStateInfo(props, "dWfStepName", "dWfStepType");
/* 561 */     dlg.init(props, this.m_context, null, stepInfo);
/* 562 */     if (dlg.prompt() != 1)
/*     */       return;
/*     */     try
/*     */     {
/* 566 */       updateStepList(props, true, isAdd);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 570 */       IdcMessage error = IdcMessageFactory.lc("apUnableToAddOrEditStep", new Object[0]);
/* 571 */       Report.trace("applet.workflow", null, exp);
/* 572 */       MessageBox.reportError(this.m_systemInterface, exp, error);
/* 573 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteStep()
/*     */   {
/* 580 */     int index = this.m_stepList.getSelectedIndex();
/* 581 */     if (index < 0)
/*     */     {
/* 583 */       return;
/*     */     }
/*     */ 
/* 586 */     Properties props = getStepDataAt(index);
/*     */     try
/*     */     {
/* 589 */       updateStepList(props, false, false);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 593 */       IdcMessage error = IdcMessageFactory.lc("apUnableToDeleteStep", new Object[0]);
/* 594 */       MessageBox.reportError(this.m_systemInterface, e, error);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Properties getStepDataAt(int index)
/*     */   {
/* 600 */     Properties props = new Properties();
/* 601 */     DataResultSet rset = (DataResultSet)this.m_templateData.getResultSet("WorkflowSteps");
/* 602 */     rset.setCurrentRow(index);
/*     */ 
/* 604 */     FieldInfo info = new FieldInfo();
/* 605 */     int nfields = rset.getNumFields();
/* 606 */     for (int i = 0; i < nfields; ++i)
/*     */     {
/* 608 */       rset.getIndexFieldInfo(i, info);
/* 609 */       String val = rset.getStringValue(i);
/* 610 */       if (val == null)
/*     */         continue;
/* 612 */       props.put(info.m_name, val);
/*     */     }
/*     */ 
/* 615 */     return props;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 625 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 626 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 631 */     String name = exchange.m_compName;
/* 632 */     String val = exchange.m_compValue;
/*     */ 
/* 634 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 640 */     if (name.equals("dWfTemplateName"))
/*     */     {
/* 642 */       int valResult = Validation.checkUrlFileSegment(val);
/* 643 */       IdcMessage errMsg = null;
/* 644 */       switch (valResult)
/*     */       {
/*     */       case 0:
/* 647 */         break;
/*     */       case -1:
/* 649 */         errMsg = IdcMessageFactory.lc("apSpecifyTemplateName", new Object[0]);
/* 650 */         break;
/*     */       case -2:
/* 652 */         errMsg = IdcMessageFactory.lc("apSpacesInTemplateName", new Object[0]);
/* 653 */         break;
/*     */       default:
/* 655 */         errMsg = IdcMessageFactory.lc("apIllegalCharsInTemplateName", new Object[0]);
/*     */       }
/*     */ 
/* 658 */       if (errMsg != null)
/*     */       {
/* 660 */         exchange.m_errorMessage = errMsg;
/* 661 */         return false;
/*     */       }
/*     */     }
/* 664 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 669 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.EditTemplateDlg
 * JD-Core Version:    0.5.4
 */