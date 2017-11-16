/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.gui.AddAliasDlg;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import intradoc.shared.gui.ViewDlg;
/*     */ import intradoc.shared.workflow.WfStepData;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class WfEditView extends EditViewBase
/*     */ {
/*  65 */   protected JButton m_addStepBtn = null;
/*  66 */   protected JButton m_deleteStepBtn = null;
/*  67 */   protected boolean m_isInProcess = false;
/*     */ 
/*  69 */   protected Properties m_contributionStep = null;
/*  70 */   protected DataResultSet m_conAliases = null;
/*     */ 
/*  72 */   protected UdlPanel[] m_lists = null;
/*     */   protected static final int DOC_TYPE = 0;
/*     */   protected static final int CON_TYPE = 1;
/*     */   protected static final int STEP_TYPE = 2;
/*     */ 
/*     */   public WfEditView()
/*     */   {
/*  81 */     this.m_lists = new UdlPanel[3];
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  87 */     DisplayStringCallback displayCallback = this.m_context.createStringCallback();
/*  88 */     ActionListener docListener = createDocListener();
/*  89 */     JPanel docPanel = initDocList(docListener, displayCallback);
/*     */ 
/*  91 */     ActionListener conListener = createAliasListener();
/*  92 */     JPanel conPanel = initConList(conListener, displayCallback);
/*     */ 
/*  94 */     ActionListener stepListener = createStepListener();
/*  95 */     JPanel revStepList = initStepList(stepListener, displayCallback);
/*     */ 
/*  97 */     setInsets(5, 5, 5, 5);
/*     */ 
/*  99 */     setLayout(new GridLayout(3, 1));
/* 100 */     add(docPanel);
/* 101 */     add(conPanel);
/* 102 */     add(revStepList);
/*     */   }
/*     */ 
/*     */   protected JPanel initDocList(ActionListener aListener, DisplayStringCallback displayCallback)
/*     */   {
/* 108 */     String[] listInfo = { "apTitleContent", "WfDocuments", "dDocName,dWfComputed" };
/* 109 */     String[][] buttonInfo = { { LocaleResources.getString("apDlgButtonNew", this.m_cxt), "addNew", "0", LocaleResources.getString("apReadableButtonAddContentWorkflow", this.m_cxt) }, { LocaleResources.getString("apDlgButtonSelect", this.m_cxt), "addSelected", "0", LocaleResources.getString("apReadableButtonSelectContentWorkflow", this.m_cxt) }, { LocaleResources.getString("apLabelDelete", this.m_cxt), "docDelete", "1", LocaleResources.getString("apReadableButtonRemoveContentWorkflow", this.m_cxt) } };
/*     */ 
/* 115 */     UdlPanel docList = createList(listInfo, buttonInfo, aListener, 175, 5);
/* 116 */     docList.setMultipleMode(true);
/* 117 */     docList.setIDColumn("dDocName");
/* 118 */     docList.setDisplayCallback("dWfComputed", displayCallback);
/*     */ 
/* 120 */     this.m_lists[0] = docList;
/*     */ 
/* 122 */     return docList;
/*     */   }
/*     */ 
/*     */   protected JPanel initConList(ActionListener aListener, DisplayStringCallback displayCallback)
/*     */   {
/* 128 */     String[] listInfo = { "apTitleContributors", "WfAliases", "dAlias,dAliasType" };
/* 129 */     String[][] buttonInfo = { { LocaleResources.getString("apDlgButtonAddAlias", this.m_cxt), "conAliasAdd", "0", LocaleResources.getString("apReadableButtonAddAliasWorkflow", this.m_cxt) }, { LocaleResources.getString("apDlgButtonAddUser", this.m_cxt), "conUserAdd", "0", LocaleResources.getString("apReadableButtonAddUserWorkflow", this.m_cxt) }, { LocaleResources.getString("apLabelDelete", this.m_cxt), "conDelete", "1", LocaleResources.getString("apReadableButtonDeleteUserWorkflow", this.m_cxt) } };
/*     */ 
/* 136 */     UdlPanel conList = createList(listInfo, buttonInfo, aListener, 175, 5);
/* 137 */     conList.setMultipleMode(true);
/* 138 */     conList.setIDColumn("dAlias");
/* 139 */     conList.setDisplayCallback("dAliasType", displayCallback);
/*     */ 
/* 141 */     this.m_lists[1] = conList;
/*     */ 
/* 143 */     return conList;
/*     */   }
/*     */ 
/*     */   protected JPanel initStepList(ActionListener aListener, DisplayStringCallback displayCallback)
/*     */   {
/* 148 */     UdlPanel stepList = new UdlPanel(LocaleResources.getString("apTitleSteps", this.m_cxt), null, 175, 5, "WorkflowSteps", false);
/*     */ 
/* 150 */     stepList.setVisibleColumns("dWfStepName,dWfStepType");
/* 151 */     stepList.setDisplayCallback("dWfStepType", displayCallback);
/* 152 */     stepList.init();
/* 153 */     stepList.useDefaultListener();
/*     */ 
/* 155 */     JPanel btnPanel = new PanePanel();
/* 156 */     btnPanel.setLayout(new GridLayout(0, 1));
/*     */ 
/* 158 */     String[][] buttonDefs = { { LocaleResources.getString("apDlgButtonAdd", this.m_cxt), "stepAdd", "0", "apReadableButtonAddWorkflowStep" }, { LocaleResources.getString("apDlgButtonEdit", this.m_cxt), "stepEdit", "1", "apReadableButtonEditWorkflowStep" }, { LocaleResources.getString("apLabelDelete", this.m_cxt), "stepDelete", "1", "apReadableButtonDeleteWorkflowStep" } };
/*     */ 
/* 165 */     for (int i = 0; i < buttonDefs.length; ++i)
/*     */     {
/* 167 */       String cmd = buttonDefs[i][1];
/* 168 */       boolean isListControlled = StringUtils.convertToBool(buttonDefs[i][2], false);
/*     */ 
/* 170 */       JButton btn = stepList.addButton(buttonDefs[i][0], isListControlled);
/* 171 */       btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(buttonDefs[i][3], this.m_cxt));
/* 172 */       btn.setActionCommand(cmd);
/* 173 */       btn.addActionListener(aListener);
/* 174 */       btnPanel.add(btn);
/*     */ 
/* 176 */       if (cmd.equals("stepAdd"))
/*     */       {
/* 178 */         this.m_addStepBtn = btn;
/*     */       } else {
/* 180 */         if (!cmd.equals("stepDelete"))
/*     */           continue;
/* 182 */         this.m_deleteStepBtn = btn;
/*     */       }
/*     */     }
/*     */ 
/* 186 */     JPanel cbWrapper = new PanePanel();
/* 187 */     cbWrapper.add(btnPanel);
/* 188 */     stepList.add("East", cbWrapper);
/* 189 */     stepList.enableDisable(false);
/*     */ 
/* 191 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 195 */         UdlPanel listenerStepList = WfEditView.this.m_lists[2];
/* 196 */         int index = listenerStepList.getSelectedIndex();
/*     */ 
/* 198 */         if (WfEditView.this.m_isInProcess)
/*     */         {
/* 200 */           WfEditView.this.m_addStepBtn.setEnabled(false);
/* 201 */           WfEditView.this.m_deleteStepBtn.setEnabled(false);
/*     */         }
/* 203 */         else if (index < 0)
/*     */         {
/* 205 */           WfEditView.this.m_deleteStepBtn.setEnabled(false);
/*     */         }
/*     */         else
/*     */         {
/* 209 */           WfEditView.this.m_deleteStepBtn.setEnabled(true);
/*     */         }
/*     */       }
/*     */     };
/* 213 */     stepList.addItemListener(iListener);
/*     */ 
/* 215 */     ActionListener xListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 219 */         WfEditView.this.addOrEditStep(false);
/*     */       }
/*     */     };
/* 222 */     stepList.m_list.addActionListener(xListener);
/*     */ 
/* 224 */     this.m_lists[2] = stepList;
/* 225 */     return stepList;
/*     */   }
/*     */ 
/*     */   public void load()
/*     */   {
/* 231 */     String[][] sel = new String[3][];
/* 232 */     refreshLists(sel);
/*     */   }
/*     */ 
/*     */   public void reload()
/*     */   {
/* 237 */     refreshLists(-1, null);
/*     */   }
/*     */ 
/*     */   public void refreshLists(String[][] selObjs)
/*     */   {
/*     */     try
/*     */     {
/* 244 */       DataBinder binder = new DataBinder();
/* 245 */       binder.putLocal("dWfName", this.m_workflowInfo.m_wfName);
/*     */ 
/* 247 */       SharedContext shContext = this.m_context.getSharedContext();
/* 248 */       shContext.executeService("GET_WORKFLOW", binder, false);
/*     */ 
/* 250 */       if (this.m_lists[0] != null)
/*     */       {
/* 252 */         this.m_lists[0].refreshListEx(binder, selObjs[0]);
/*     */       }
/*     */ 
/* 255 */       refreshSteps(binder, selObjs[1], selObjs[2]);
/*     */ 
/* 258 */       DataBinder wfData = this.m_workflowInfo.getWorkflowData();
/* 259 */       wfData.merge(binder);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 263 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apErrorLoadingContentListForWorkflow", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void refreshLists(int EDIT_TYPE, String[] selObjs)
/*     */   {
/* 269 */     String[][] selObjects = new String[3][];
/* 270 */     for (int i = 0; i < this.m_lists.length; ++i)
/*     */     {
/* 272 */       selObjects[i] = null;
/* 273 */       if (i == EDIT_TYPE)
/*     */       {
/* 275 */         selObjects[i] = selObjs;
/*     */       } else {
/* 277 */         if (this.m_lists[i] == null)
/*     */           continue;
/* 279 */         selObjects[i] = this.m_lists[i].getSelectedObjs();
/*     */       }
/*     */     }
/*     */ 
/* 283 */     refreshLists(selObjects);
/*     */   }
/*     */ 
/*     */   public void refreshSteps(DataBinder binder, String[] selConObjs, String[] selStepObjs)
/*     */     throws DataException
/*     */   {
/* 290 */     this.m_conAliases = extractContributorSet(binder);
/*     */ 
/* 292 */     if (this.m_lists[1] != null)
/*     */     {
/* 294 */       this.m_lists[1].refreshListEx(this.m_conAliases, selConObjs);
/*     */     }
/* 296 */     this.m_lists[2].refreshListEx(binder, selStepObjs);
/*     */ 
/* 298 */     String wfStatus = binder.get("dWfStatus");
/* 299 */     this.m_isInProcess = wfStatus.equals("INPROCESS");
/*     */ 
/* 302 */     this.m_addStepBtn.setEnabled(!this.m_isInProcess);
/* 303 */     if (!this.m_deleteStepBtn.isEnabled())
/*     */       return;
/* 305 */     this.m_deleteStepBtn.setEnabled(!this.m_isInProcess);
/*     */   }
/*     */ 
/*     */   protected DataResultSet extractContributorSet(DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 312 */     DataResultSet stepSet = (DataResultSet)binder.getResultSet("WorkflowSteps");
/* 313 */     if (stepSet == null)
/*     */     {
/* 315 */       return null;
/*     */     }
/*     */ 
/* 318 */     String[] keys = { "dWfStepName", "dWfStepType", "dAliases" };
/* 319 */     FieldInfo[] infos = ResultSetUtils.createInfoList(stepSet, keys, true);
/* 320 */     boolean isFound = false;
/* 321 */     for (stepSet.first(); stepSet.isRowPresent(); stepSet.next())
/*     */     {
/* 323 */       String stepName = stepSet.getStringValue(infos[0].m_index);
/* 324 */       String stepType = stepSet.getStringValue(infos[1].m_index);
/*     */ 
/* 328 */       if (!stepName.equals("contribution"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 333 */       binder.putLocal("dWfAutoContributeStepType", stepType);
/* 334 */       isFound = true;
/* 335 */       break;
/*     */     }
/*     */ 
/* 339 */     WfStepData conSet = new WfStepData();
/*     */ 
/* 341 */     if (isFound)
/*     */     {
/* 344 */       this.m_contributionStep = stepSet.getCurrentRowProps();
/* 345 */       conSet.loadAliasStepData(this.m_contributionStep);
/* 346 */       ResultSetUtils.sortResultSet(conSet, new String[] { "dAlias", "dAliasType" });
/* 347 */       stepSet.deleteCurrentRow();
/*     */     }
/*     */ 
/* 350 */     return conSet;
/*     */   }
/*     */ 
/*     */   protected ActionListener createDocListener()
/*     */   {
/* 358 */     ActionListener aListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 362 */         String cmd = e.getActionCommand();
/* 363 */         if (cmd.equals("addNew"))
/*     */         {
/* 365 */           WfEditView.this.addNewDoc();
/*     */         }
/* 367 */         else if (cmd.equals("addSelected"))
/*     */         {
/* 369 */           WfEditView.this.addSelectedDocs();
/*     */         } else {
/* 371 */           if (!cmd.equals("docDelete"))
/*     */             return;
/* 373 */           WfEditView.this.deleteCommand(cmd);
/*     */         }
/*     */       }
/*     */     };
/* 378 */     return aListener;
/*     */   }
/*     */ 
/*     */   protected ActionListener createAliasListener()
/*     */   {
/* 383 */     ActionListener aliasListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 387 */         String cmd = e.getActionCommand();
/* 388 */         if (cmd.equals("conAliasAdd"))
/*     */         {
/* 390 */           WfEditView.this.addContributorAliases();
/*     */         }
/* 392 */         else if (cmd.equals("conUserAdd"))
/*     */         {
/* 394 */           WfEditView.this.addContributorUsers();
/*     */         } else {
/* 396 */           if (!cmd.equals("conDelete"))
/*     */             return;
/* 398 */           WfEditView.this.deleteCommand(cmd);
/*     */         }
/*     */       }
/*     */     };
/* 403 */     return aliasListener;
/*     */   }
/*     */ 
/*     */   protected ActionListener createStepListener()
/*     */   {
/* 408 */     ActionListener stepListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 412 */         String cmd = e.getActionCommand();
/* 413 */         if (cmd.equals("stepAdd"))
/*     */         {
/* 415 */           WfEditView.this.addOrEditStep(true);
/*     */         }
/* 417 */         else if (cmd.equals("stepEdit"))
/*     */         {
/* 419 */           WfEditView.this.addOrEditStep(false);
/*     */         }
/*     */         else
/*     */         {
/* 423 */           WfEditView.this.deleteStep();
/*     */         }
/*     */       }
/*     */     };
/* 428 */     return stepListener;
/*     */   }
/*     */ 
/*     */   protected void addNewDoc()
/*     */   {
/* 433 */     AddDocDlg dlg = new AddDocDlg(this.m_systemInterface, LocaleResources.getString("apTitleAddContentToWorkflow", this.m_cxt), this.m_lists[0].getResultSet(), DialogHelpTable.getHelpPage("AddDocumentToWorkflow"));
/*     */ 
/* 437 */     dlg.init(this.m_workflowInfo, this.m_context, null);
/* 438 */     if (dlg.prompt() != 1)
/*     */       return;
/* 440 */     Properties localData = dlg.getProperties();
/* 441 */     if (!executeCommand("ADD_WORKFLOWDOCUMENT", localData))
/*     */       return;
/* 443 */     String docName = localData.getProperty("dDocName");
/*     */ 
/* 445 */     AppLauncher.notifyInternalSubjectChange("documents");
/* 446 */     String[] names = { docName };
/* 447 */     refreshLists(0, names);
/*     */   }
/*     */ 
/*     */   protected void addSelectedDocs()
/*     */   {
/* 454 */     ViewDlg dlg = new ViewDlg(null, this.m_systemInterface, LocaleResources.getString("apTitleAddContentToWorkflow", this.m_cxt), this, DialogHelpTable.getHelpPage("AddDocumentsToWorkflow"));
/*     */ 
/* 458 */     ViewData viewData = new ViewData(1);
/* 459 */     viewData.m_isViewOnly = false;
/* 460 */     viewData.m_isMultipleMode = true;
/* 461 */     viewData.m_inDateState = true;
/* 462 */     viewData.m_useFilter = true;
/* 463 */     viewData.m_viewName = "DocSelectView";
/*     */ 
/* 465 */     dlg.init(viewData, null);
/* 466 */     if (dlg.prompt() != 1)
/*     */       return;
/* 468 */     Properties props = dlg.getProperties();
/* 469 */     String docNames = dlg.computeSelectedValuesString("dDocName", true);
/* 470 */     if ((docNames == null) || (docNames.length() == 0))
/*     */     {
/* 472 */       reportError(null, IdcMessageFactory.lc("apNothingHasBeenSelected", new Object[0]));
/* 473 */       return;
/*     */     }
/* 475 */     props.put("docNames", docNames);
/*     */ 
/* 477 */     if (!executeCommand("ADD_WORKFLOWDOCUMENTS", props))
/*     */       return;
/* 479 */     Vector docs = StringUtils.parseArray(docNames, '\t', '^');
/* 480 */     String[] docStr = StringUtils.convertListToArray(docs);
/* 481 */     refreshLists(0, docStr);
/*     */   }
/*     */ 
/*     */   protected void addContributorAliases()
/*     */   {
/* 488 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 493 */         Properties localData = this.m_dlgHelper.m_props;
/* 494 */         String alias = localData.getProperty("aliases");
/* 495 */         if ((alias == null) || (alias.length() == 0))
/*     */         {
/* 498 */           MessageBox.reportError(WfEditView.this.m_systemInterface, IdcMessageFactory.lc("apSelectAnAlias", new Object[0]));
/* 499 */           return false;
/*     */         }
/* 501 */         localData.put("dAliasType", "alias");
/*     */ 
/* 503 */         return WfEditView.this.executeCommand("ADD_WORKFLOWALIASES", localData);
/*     */       }
/*     */     };
/* 507 */     AddAliasDlg dlg = new AddAliasDlg(this.m_systemInterface, LocaleResources.getString("apTitleAddAliasToWorkflow", this.m_cxt), DialogHelpTable.getHelpPage("AddAliasToWorkflow"));
/*     */ 
/* 510 */     Properties props = (Properties)this.m_contributionStep.clone();
/* 511 */     if ((!dlg.init(okCallback, props, this.m_conAliases)) || 
/* 513 */       (dlg.prompt() != 1))
/*     */       return;
/* 515 */     String[] selCon = dlg.getSelected();
/* 516 */     refreshLists(1, selCon);
/*     */   }
/*     */ 
/*     */   protected void addContributorUsers()
/*     */   {
/* 523 */     this.m_helper.retrieveComponentValues();
/*     */ 
/* 525 */     ViewData viewData = new ViewData(2);
/* 526 */     viewData.m_isMultipleMode = true;
/* 527 */     viewData.m_isViewOnly = false;
/* 528 */     viewData.m_viewName = "UserSelectView";
/*     */ 
/* 530 */     ViewDlg dlg = new ViewDlg(null, this.m_systemInterface, LocaleResources.getString("apTitleAddUserToStep", this.m_cxt), this, DialogHelpTable.getHelpPage("AddUserToWorkflow"));
/*     */ 
/* 533 */     dlg.init(viewData, null);
/*     */ 
/* 535 */     if (dlg.prompt() != 1)
/*     */       return;
/* 537 */     Properties props = (Properties)this.m_contributionStep.clone();
/* 538 */     props.put("dAliasType", "user");
/*     */ 
/* 540 */     Vector userList = new IdcVector();
/* 541 */     String[] users = dlg.getSelectedObjs();
/* 542 */     for (int i = 0; i < users.length; ++i)
/*     */     {
/* 544 */       userList.addElement(users[i]);
/*     */     }
/* 546 */     String userStr = StringUtils.createString(userList, '\t', '^');
/* 547 */     props.put("aliases", userStr);
/*     */ 
/* 549 */     if (!executeCommand("ADD_WORKFLOWALIASES", props))
/*     */       return;
/* 551 */     refreshLists(1, users);
/*     */   }
/*     */ 
/*     */   protected void deleteStep()
/*     */   {
/* 558 */     UdlPanel stepList = this.m_lists[2];
/* 559 */     int index = stepList.getSelectedIndex();
/* 560 */     if (index < 0)
/*     */     {
/* 562 */       return;
/*     */     }
/*     */ 
/* 565 */     Properties props = stepList.getDataAt(index);
/* 566 */     props.getProperty("dWfStepName");
/*     */ 
/* 568 */     if ((MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifyDeleteStepFromWorkflow", new Object[0]), 4) != 2) || 
/* 572 */       (!executeCommand("DELETE_WORKFLOWSTEP", props)))
/*     */       return;
/* 574 */     refreshLists(2, null);
/*     */   }
/*     */ 
/*     */   protected void deleteCommand(String cmd)
/*     */   {
/* 581 */     String action = null;
/* 582 */     IdcMessage msg = null;
/* 583 */     String idStr = null;
/* 584 */     String key = null;
/*     */ 
/* 586 */     boolean isDoc = false;
/* 587 */     UdlPanel curPnl = this.m_lists[0];
/* 588 */     int editType = 0;
/* 589 */     if (cmd.equals("docDelete"))
/*     */     {
/* 591 */       msg = IdcMessageFactory.lc("apVerifyDeleteContentFromWorkflow", new Object[0]);
/* 592 */       action = "DELETE_WORKFLOWDOCUMENTS";
/* 593 */       idStr = "docNames";
/* 594 */       key = "dDocName";
/* 595 */       isDoc = true;
/*     */     }
/* 597 */     else if (cmd.equals("conDelete"))
/*     */     {
/* 599 */       curPnl = this.m_lists[1];
/* 600 */       msg = IdcMessageFactory.lc("apVerifyDeleteContributorsFromWorkflow", new Object[0]);
/* 601 */       action = "DELETE_WFCONTRIBUTORS";
/* 602 */       idStr = "aliases";
/* 603 */       key = "dAlias";
/* 604 */       editType = 1;
/*     */     }
/*     */ 
/* 607 */     int[] selIndexes = curPnl.getSelectedIndexes();
/* 608 */     if ((selIndexes == null) || (selIndexes.length == 0))
/*     */     {
/* 610 */       return;
/*     */     }
/*     */ 
/* 614 */     Properties props = curPnl.getDataAt(selIndexes[0]);
/*     */ 
/* 616 */     Vector values = new IdcVector();
/* 617 */     for (int i = 0; i < selIndexes.length; ++i)
/*     */     {
/* 619 */       Properties data = curPnl.getDataAt(selIndexes[i]);
/* 620 */       values.addElement(data.getProperty(key));
/*     */     }
/*     */ 
/* 623 */     String str = StringUtils.createString(values, '\t', '^');
/* 624 */     props.put(idStr, str);
/*     */ 
/* 626 */     if ((MessageBox.doMessage(this.m_systemInterface, msg, 4) != 2) || 
/* 628 */       (!executeCommand(action, props)))
/*     */       return;
/* 630 */     if (isDoc)
/*     */     {
/* 632 */       AppLauncher.notifyInternalSubjectChange("documents");
/*     */     }
/* 634 */     refreshLists(editType, null);
/*     */   }
/*     */ 
/*     */   protected boolean executeCommand(String action, Properties props)
/*     */   {
/* 641 */     props.put("dWfName", this.m_workflowInfo.m_wfName);
/* 642 */     props.put("dWfType", this.m_workflowInfo.m_wfType);
/* 643 */     props.put("dSecurityGroup", this.m_workflowInfo.get("dSecurityGroup"));
/*     */     try
/*     */     {
/* 647 */       DataBinder binder = new DataBinder(true);
/* 648 */       binder.setLocalData(props);
/*     */ 
/* 650 */       SharedContext shContext = this.m_context.getSharedContext();
/* 651 */       shContext.executeService(action, binder, false);
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 657 */       IdcMessage msg = IdcMessageFactory.lc("", new Object[0]);
/* 658 */       msg.m_msgLocalized = "";
/* 659 */       reportError(exp, msg);
/* 660 */       return false;
/*     */     }
/* 662 */     return true;
/*     */   }
/*     */ 
/*     */   protected void addOrEditStep(boolean isNew)
/*     */   {
/* 667 */     String title = LocaleResources.getString("apTitleAddNewStep", this.m_cxt);
/* 668 */     Properties props = null;
/* 669 */     String action = "ADD_WORKFLOWSTEP";
/* 670 */     UdlPanel stepList = this.m_lists[2];
/*     */ 
/* 672 */     String helpPage = "AddNewWorkflowStep";
/* 673 */     if (this.m_isCriteria)
/*     */     {
/* 675 */       helpPage = "AddNewWorkflowCriteriaStep";
/*     */     }
/*     */ 
/* 678 */     if (!isNew)
/*     */     {
/* 680 */       int index = stepList.getSelectedIndex();
/* 681 */       if (index < 0)
/*     */       {
/* 683 */         return;
/*     */       }
/* 685 */       props = stepList.getDataAt(index);
/* 686 */       title = LocaleResources.getString("apTitleEditStep", this.m_cxt, props.getProperty("dWfStepName"));
/* 687 */       action = "EDIT_WORKFLOWSTEP";
/* 688 */       helpPage = "EditWorkflowStep";
/* 689 */       if (this.m_isCriteria)
/*     */       {
/* 691 */         helpPage = "EditWorkflowCriteriaStep";
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 696 */       props = new Properties();
/*     */     }
/* 698 */     DataBinder.mergeHashTables(props, this.m_workflowInfo.m_wfData.getLocalData());
/*     */ 
/* 700 */     String okAction = action;
/* 701 */     WorkflowStateInfo wfStepInfo = new WorkflowStateInfo(props, "dWfStepName", "dWfStepType");
/*     */     try
/*     */     {
/* 705 */       wfStepInfo.createStepData(this.m_workflowInfo.m_wfData);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 709 */       this.m_context.reportError(e, IdcMessageFactory.lc("apUnableToRetrieveScriptStepInfo", new Object[0]));
/*     */     }
/*     */ 
/* 712 */     DialogCallback okCallback = new DialogCallback(wfStepInfo, okAction)
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 719 */           Properties localData = this.m_dlgHelper.m_props;
/* 720 */           DataBinder wfBinder = this.val$wfStepInfo.getWorkflowData();
/*     */ 
/* 723 */           DataBinder binder = new DataBinder();
/* 724 */           binder.merge(wfBinder);
/*     */ 
/* 728 */           String stepType = localData.getProperty("dWfStepType");
/* 729 */           if (WorkflowScriptUtils.isContributorStep(stepType))
/*     */           {
/* 731 */             boolean isAll = StringUtils.convertToBool(localData.getProperty("dWfStepIsAll"), false);
/* 732 */             int numRevs = NumberUtils.parseInteger(localData.getProperty("dWfStepWeight"), 0);
/* 733 */             if ((!isAll) && (numRevs == 0))
/*     */             {
/* 735 */               String stepTypeDes = WorkflowScriptUtils.formatStepTypeDescription(stepType);
/* 736 */               IdcMessage errMsg = IdcMessageFactory.lc("apStepTypeRequiresReviewers", new Object[] { null, stepTypeDes });
/*     */ 
/* 738 */               WfEditView.this.m_context.reportError(null, errMsg);
/* 739 */               return false;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 744 */           binder.putLocal("dWfName", WfEditView.this.m_workflowInfo.getWfName());
/* 745 */           binder.putLocal("dWfType", WfEditView.this.m_workflowInfo.getWfType());
/*     */ 
/* 747 */           SharedContext shContext = WfEditView.this.m_context.getSharedContext();
/* 748 */           shContext.executeService(this.val$okAction, binder, false);
/*     */ 
/* 750 */           return true;
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 754 */           WfEditView.this.m_context.reportError(exp);
/*     */         }
/* 756 */         return false;
/*     */       }
/*     */     };
/* 760 */     EditStepDlg dlg = new EditStepDlg(this.m_systemInterface, title, this.m_lists[2].getResultSet(), this.m_isCriteria, DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 764 */     dlg.initEx(props, true, okCallback, this.m_context, this.m_workflowInfo, wfStepInfo);
/*     */ 
/* 766 */     if (dlg.prompt() != 1)
/*     */       return;
/* 768 */     props = dlg.getData();
/* 769 */     String[] newSteps = { props.getProperty("dWfStepName") };
/* 770 */     refreshLists(2, newSteps);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 776 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.WfEditView
 * JD-Core Version:    0.5.4
 */