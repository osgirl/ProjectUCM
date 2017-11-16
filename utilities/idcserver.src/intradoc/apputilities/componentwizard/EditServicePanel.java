/*      */ package intradoc.apputilities.componentwizard;
/*      */ 
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemInterface;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.gui.ContainerHelper;
/*      */ import intradoc.gui.CustomLabel;
/*      */ import intradoc.gui.CustomText;
/*      */ import intradoc.gui.DialogCallback;
/*      */ import intradoc.gui.DialogHelper;
/*      */ import intradoc.gui.DisplayChoice;
/*      */ import intradoc.gui.DynamicComponentExchange;
/*      */ import intradoc.gui.GridBagHelper;
/*      */ import intradoc.gui.PanePanel;
/*      */ import intradoc.gui.iwt.ComboChoice;
/*      */ import intradoc.gui.iwt.UdlPanel;
/*      */ import intradoc.server.Action;
/*      */ import intradoc.server.ServiceData;
/*      */ import intradoc.shared.DialogHelpTable;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.awt.event.ItemEvent;
/*      */ import java.awt.event.ItemListener;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.swing.JCheckBox;
/*      */ import javax.swing.JPanel;
/*      */ import javax.swing.JTextField;
/*      */ 
/*      */ public class EditServicePanel extends EditBasePanel
/*      */ {
/*   66 */   protected ServiceData m_idcServiceData = null;
/*   67 */   protected Vector m_actions = null;
/*      */ 
/*   70 */   protected int m_actionIdCount = 0;
/*      */ 
/*   73 */   protected Vector m_selectQueries = null;
/*   74 */   protected Vector m_javaMethods = null;
/*   75 */   protected Vector m_execQueries = null;
/*      */ 
/*   77 */   public static final String[][] ACTIONS_COL_MAP = { { "function", "!csCompWizLabelAction", "20" }, { "actionType", "!csCompWizLabelType", "20" } };
/*      */ 
/*   82 */   public static final String[][] TYPE_DEF = { { "1", "csCompWizSvcSelectQuery" }, { "2", "csCompWizSvcExecuteQuery" }, { "3", "csCompWizSvcJavaMethod" }, { "4", "csCompWizSvcLoadOptList" }, { "5", "csCompWizSvcSelectCacheQuery" } };
/*      */ 
/*   91 */   public static final String[][] CONTROL_MASK_DEF = { { "0x01", "!csCompWizSvcCMaskIgnore", "ignoreError" }, { "0x02", "!csCompWizSvcCMaskMustExist", "mustExist" }, { "0x04", "!csCompWizSvcCMaskBeginTrans", "beginTran" }, { "0x08", "!csCompWizSvcCMaskCommitTrans", "commitTran" }, { "0x10", "!csCompWizSvcCMaskMustNotExist", "mustNotExist" }, { "0x20", "!csCompWizSvcCMaskRetryQuery", "retryQuery" }, { "0x40", "!csCompWizSvcCMaskDoNotLog", "doNotLog" } };
/*      */ 
/*  102 */   public static final String[][] ACCESS_LEVEL_DEF = { { "0x01", "!csCompWizSvcAccessRead", "read" }, { "0x02", "!csCompWizSvcAccessWrite", "write" }, { "0x04", "!csCompWizSvcAccessDelete", "delete" }, { "0x08", "!csCompWizSvcAccessAdmin", "admin" }, { "0x10", "!csCompWizSvcAccessGlobal", "global" }, { "0x20", "!csCompWizSvcAccessScriptable", "scriptable" } };
/*      */ 
/*      */   public void initUI(int editType)
/*      */   {
/*  122 */     LocaleResources.localizeStaticDoubleArray(TYPE_DEF, null, 1);
/*      */     try
/*      */     {
/*  126 */       JPanel panel = new PanePanel();
/*  127 */       this.m_helper.makePanelGridBag(panel, 1);
/*      */ 
/*  129 */       if ((editType == 0) || (editType == 1))
/*      */       {
/*  131 */         ItemListener showAllListener = new ItemListener()
/*      */         {
/*      */           public void itemStateChanged(ItemEvent e)
/*      */           {
/*      */             try
/*      */             {
/*  137 */               boolean isAll = EditServicePanel.this.m_showAll.isSelected();
/*  138 */               EditServicePanel.this.initSelectListData(isAll);
/*  139 */               EditServicePanel.this.refreshSelectList(null);
/*      */             }
/*      */             catch (Exception excp)
/*      */             {
/*  143 */               CWizardGuiUtils.reportError(EditServicePanel.this.m_systemInterface, excp, (IdcMessage)null);
/*      */             }
/*      */           }
/*      */         };
/*  147 */         ActionListener showInfoListener = new ActionListener()
/*      */         {
/*      */           public void actionPerformed(ActionEvent e)
/*      */           {
/*  151 */             String name = EditServicePanel.this.m_selectList.getSelectedObj();
/*  152 */             if (name == null)
/*      */             {
/*  154 */               return;
/*      */             }
/*      */ 
/*  157 */             DataResultSet drset = SharedObjects.getTable("IdcServices");
/*  158 */             Vector v = drset.findRow(0, name);
/*  159 */             ServiceData serviceData = new ServiceData();
/*  160 */             String actions = (String)v.elementAt(2);
/*  161 */             Vector actionList = StringUtils.parseArray(actions, '\n', '\\');
/*      */             try
/*      */             {
/*  164 */               serviceData.init(name, (String)v.elementAt(1), actions);
/*  165 */               String title = LocaleUtils.encodeMessage("csCompWizServiceInfoTitle", null, name);
/*  166 */               title = EditServicePanel.this.m_systemInterface.localizeMessage(title);
/*  167 */               ServiceInfoDlg dlg = new ServiceInfoDlg(EditServicePanel.this.m_systemInterface, title, null);
/*  168 */               dlg.init(serviceData, actionList);
/*      */             }
/*      */             catch (ServiceException se)
/*      */             {
/*  172 */               IdcMessage msg = IdcMessageFactory.lc("csCompWizServiceInfoError", new Object[] { name });
/*  173 */               CWizardGuiUtils.reportError(EditServicePanel.this.m_systemInterface, se, msg);
/*      */             }
/*      */             catch (DataException de)
/*      */             {
/*  177 */               IdcMessage msg = IdcMessageFactory.lc("csCompWizServiceInfoError", new Object[] { name });
/*  178 */               CWizardGuiUtils.reportError(EditServicePanel.this.m_systemInterface, de, msg);
/*      */             }
/*      */           }
/*      */         };
/*  182 */         addNewOrUseExistingPanel(panel, "Name", true, true, showAllListener, true, showInfoListener);
/*      */       }
/*      */ 
/*  185 */       addInfoPanel(panel, editType);
/*  186 */       this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  187 */       this.m_helper.addLastComponentInRow(this, panel);
/*      */ 
/*  189 */       if (editType == 2)
/*      */       {
/*  191 */         this.m_idcServiceData = new ServiceData();
/*  192 */         String name = this.m_helper.m_props.getProperty("Name");
/*  193 */         String attributes = this.m_helper.m_props.getProperty("Attributes");
/*  194 */         String actions = this.m_helper.m_props.getProperty("Actions");
/*  195 */         this.m_idcServiceData.init(name, attributes, actions);
/*  196 */         this.m_actions = StringUtils.parseArray(actions, '\n', '\\');
/*  197 */         loadServiceDataInfo();
/*      */       }
/*      */       else
/*      */       {
/*  201 */         this.m_helper.m_props.put("templatePage", "");
/*      */       }
/*  203 */       buildActionsResultSet();
/*  204 */       refreshList(null);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  208 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean onSelect()
/*      */   {
/*      */     try
/*      */     {
/*  217 */       if (this.m_selectList.getSelectedIndex() < 0)
/*      */       {
/*  219 */         throw new ServiceException("!csCompWizChooseItem");
/*      */       }
/*      */ 
/*  222 */       DataResultSet drset = SharedObjects.getTable("IdcServices");
/*      */ 
/*  224 */       if (drset == null)
/*      */       {
/*  226 */         throw new ServiceException("!csCompWizSvcTableNotLoaded");
/*      */       }
/*      */ 
/*  229 */       String name = this.m_selectList.getSelectedObj();
/*  230 */       Vector v = drset.findRow(0, name);
/*      */ 
/*  232 */       if ((v == null) || (v.size() == 0))
/*      */       {
/*  234 */         throw new ServiceException(LocaleUtils.encodeMessage("csCompWizSvcTableItemMissing", null, name));
/*      */       }
/*      */ 
/*  237 */       this.m_idcServiceData = new ServiceData();
/*  238 */       String actions = (String)v.elementAt(2);
/*  239 */       this.m_actions = StringUtils.parseArray(actions, '\n', '\\');
/*      */ 
/*  241 */       this.m_idcServiceData.init(name, (String)v.elementAt(1), actions);
/*  242 */       loadServiceDataInfo();
/*  243 */       buildActionsResultSet();
/*      */ 
/*  245 */       this.m_helper.m_props.put("Name", name);
/*  246 */       this.m_helper.loadComponentValues();
/*  247 */       refreshList(null);
/*      */     }
/*      */     catch (Exception exp)
/*      */     {
/*  251 */       CWizardGuiUtils.reportError(this.m_systemInterface, exp, (IdcMessage)null);
/*  252 */       return false;
/*      */     }
/*  254 */     return true;
/*      */   }
/*      */ 
/*      */   protected void initSelectListData(boolean isAll)
/*      */     throws ServiceException
/*      */   {
/*  260 */     String tablename = "service.common";
/*  261 */     DataResultSet drset = SharedObjects.getTable(tablename);
/*      */ 
/*  263 */     if (drset == null)
/*      */     {
/*  265 */       throw new ServiceException(LocaleUtils.encodeMessage("csTableNotLoaded", null, tablename));
/*      */     }
/*      */ 
/*  268 */     if (isAll)
/*      */     {
/*  270 */       tablename = "IdcServices";
/*  271 */       this.m_selectListData = new DataResultSet(new String[] { "name", "description" });
/*      */ 
/*  273 */       DataResultSet tempRset = SharedObjects.getTable(tablename);
/*  274 */       for (tempRset.first(); tempRset.isRowPresent(); tempRset.next())
/*      */       {
/*  276 */         String name = tempRset.getStringValue(0);
/*  277 */         String desc = "";
/*      */ 
/*  279 */         Vector v = this.m_selectListData.createEmptyRow();
/*  280 */         v.setElementAt(name, 0);
/*  281 */         Vector tempVector = drset.findRow(0, name.trim());
/*      */ 
/*  283 */         if (tempVector != null)
/*      */         {
/*  285 */           desc = (String)tempVector.elementAt(1);
/*      */         }
/*  287 */         v.setElementAt(desc, 1);
/*  288 */         this.m_selectListData.addRow(v);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  293 */       FieldInfo finfo = new FieldInfo();
/*  294 */       drset.getFieldInfo("description", finfo);
/*      */ 
/*  297 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/*  299 */         Vector v = drset.getCurrentRowValues();
/*  300 */         String desc = (String)v.elementAt(finfo.m_index);
/*  301 */         int n = drset.getCurrentRow();
/*      */ 
/*  303 */         v.setElementAt(LocaleResources.getString(desc, null), finfo.m_index);
/*  304 */         drset.setRowValues(v, n);
/*      */       }
/*  306 */       this.m_selectListData = drset;
/*      */     }
/*      */ 
/*  309 */     if (this.m_selectListData != null)
/*      */       return;
/*  311 */     throw new ServiceException(LocaleUtils.encodeMessage("csTableNotLoaded", null, tablename));
/*      */   }
/*      */ 
/*      */   protected void addInfoPanel(JPanel panel, int editType)
/*      */     throws DataException, ServiceException
/*      */   {
/*  317 */     boolean isInfo = editType == 5;
/*  318 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/*  319 */     if (isInfo)
/*      */     {
/*  321 */       this.m_helper.addLabelDisplayPairEx(panel, this.m_systemInterface.getString("csCompWizLabelSvcClass"), 30, "serviceClass", false);
/*      */     }
/*      */     else
/*      */     {
/*  326 */       addChoiceListEx(panel, "ServiceClasses", "class", LocaleResources.getString("csCompWizLabelSvcClass", null), "serviceClass", false);
/*      */     }
/*      */ 
/*  329 */     this.m_helper.m_gridHelper.addEmptyRow(panel);
/*      */ 
/*  331 */     if (isInfo)
/*      */     {
/*  333 */       this.m_helper.addLabelDisplayPairEx(panel, this.m_systemInterface.getString("csCompWizLabelSvcTemplate"), 30, "templatePage", false);
/*      */     }
/*      */     else
/*      */     {
/*  338 */       addChoiceListEx(panel, "IntradocTemplates", "name", LocaleResources.getString("csCompWizLabelSvcTemplate", null), "templatePage", false);
/*      */     }
/*      */ 
/*  341 */     this.m_helper.m_gridHelper.addEmptyRow(panel);
/*      */ 
/*  343 */     if (isInfo)
/*      */     {
/*  345 */       this.m_helper.addLabelDisplayPairEx(panel, this.m_systemInterface.getString("csCompWizLabelSvcType"), 30, "serviceType", false);
/*      */     }
/*      */     else
/*      */     {
/*  350 */       addChoiceListEx(panel, "ServiceTypes", "serviceType", LocaleResources.getString("csCompWizLabelSvcType", null), "serviceType", false);
/*      */     }
/*      */ 
/*  353 */     this.m_helper.m_gridHelper.addEmptyRow(panel);
/*      */ 
/*  356 */     JPanel boxPanel = new PanePanel();
/*  357 */     this.m_helper.makePanelGridBag(boxPanel, 1);
/*      */ 
/*  359 */     for (int i = 0; i < ACCESS_LEVEL_DEF.length; ++i)
/*      */     {
/*  361 */       String aname = LocaleResources.localizeMessage(ACCESS_LEVEL_DEF[i][1], null);
/*  362 */       JCheckBox box = new JCheckBox(aname);
/*  363 */       this.m_helper.addExchangeComponent(boxPanel, box, "is" + ACCESS_LEVEL_DEF[i][2]);
/*  364 */       if (!isInfo)
/*      */         continue;
/*  366 */       box.setEnabled(false);
/*      */     }
/*      */ 
/*  370 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/*  371 */     this.m_helper.addComponent(panel, new CustomLabel(LocaleResources.getString("csCompWizLabelAccessLevel", null), 1));
/*      */ 
/*  373 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  374 */     this.m_helper.addComponent(panel, boxPanel);
/*      */ 
/*  376 */     if (isInfo)
/*      */     {
/*  378 */       this.m_helper.addLabelDisplayPair(panel, LocaleResources.getString("csCompWizLabelSubjNotified", null), 40, "subjects");
/*      */     }
/*      */     else
/*      */     {
/*  383 */       this.m_helper.addLabelEditPair(panel, LocaleResources.getString("csCompWizLabelSubjNotified", null), 40, "subjects");
/*      */     }
/*      */ 
/*  386 */     if (isInfo)
/*      */     {
/*  388 */       this.m_helper.addLabelDisplayPair(panel, LocaleResources.getString("csCompWizLabelErrorMsg", null), 40, "errMsg");
/*      */     }
/*      */     else
/*      */     {
/*  392 */       this.m_helper.addLabelEditPair(panel, LocaleResources.getString("csCompWizLabelErrorMsg", null), 40, "errMsg");
/*      */     }
/*  394 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*      */ 
/*  396 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  397 */     this.m_list = createUdlPanel("", 100, 10, "actionsData", true, ACTIONS_COL_MAP, "id", false);
/*  398 */     this.m_helper.addComponent(panel, this.m_list);
/*      */ 
/*  400 */     if (editType != 5)
/*      */     {
/*  402 */       this.m_list.add("North", addUpDownButtons("!csCompWizLabelAction3", "id"));
/*      */     }
/*  404 */     this.m_list.add("East", addUdlPanelCommandButtons(isInfo));
/*      */   }
/*      */ 
/*      */   public boolean validateEntries()
/*      */   {
/*  410 */     boolean validate = StringUtils.convertToBool(this.m_helper.m_props.getProperty("validateEntries"), true);
/*      */ 
/*  412 */     if ((!super.validateEntries()) || (!validate))
/*      */     {
/*  414 */       return false;
/*      */     }
/*      */ 
/*  417 */     Properties props = this.m_helper.m_props;
/*      */ 
/*  419 */     String accessLevel = Integer.toString(calculateBitFlags(ACCESS_LEVEL_DEF, props));
/*  420 */     this.m_helper.m_props.put("accessLevel", accessLevel);
/*      */ 
/*  422 */     String subService = props.getProperty("serviceType");
/*  423 */     if ((subService == null) || (subService.length() == 0))
/*      */     {
/*  425 */       subService = "null";
/*      */     }
/*  427 */     props.put("serviceType", subService);
/*  428 */     props.put("templatePage", formatSourceStr(props.getProperty("templatePage"), true));
/*  429 */     props.put("subjects", formatSourceStr(props.getProperty("subjects"), true));
/*  430 */     props.put("errMsg", formatSourceStr(props.getProperty("errMsg"), true));
/*      */ 
/*  432 */     DataResultSet actData = new DataResultSet(new String[] { "actionType", "function", "parameters", "controlMask", "errMessage" });
/*      */ 
/*  435 */     for (this.m_listData.first(); this.m_listData.isRowPresent(); this.m_listData.next())
/*      */     {
/*  437 */       Vector copyVector = new IdcVector();
/*  438 */       Vector v = this.m_listData.getCurrentRowValues();
/*      */ 
/*  441 */       if (v == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  446 */       for (int i = 1; i < v.size(); ++i)
/*      */       {
/*  448 */         if (i == 1)
/*      */         {
/*  450 */           String type = (String)v.elementAt(1);
/*  451 */           type = StringUtils.findString(TYPE_DEF, type, 1, 0);
/*  452 */           copyVector.addElement(type);
/*      */         }
/*  454 */         else if (i == 5)
/*      */         {
/*  457 */           Object errObj = v.elementAt(5);
/*  458 */           String errMsg = formatSourceStr(errObj, true);
/*  459 */           copyVector.addElement(errMsg);
/*      */         }
/*      */         else
/*      */         {
/*  463 */           copyVector.addElement(v.elementAt(i));
/*      */         }
/*      */       }
/*      */ 
/*  467 */       actData.addRow(copyVector);
/*      */     }
/*      */ 
/*  470 */     this.m_helper.m_props.put("actionsData", actData);
/*  471 */     return true;
/*      */   }
/*      */ 
/*      */   public void loadAndSetServiceData(ServiceData serviceData, Vector actions)
/*      */     throws ServiceException
/*      */   {
/*  477 */     this.m_idcServiceData = serviceData;
/*  478 */     this.m_actions = actions;
/*  479 */     loadServiceDataInfo();
/*  480 */     buildActionsResultSet();
/*  481 */     this.m_helper.loadComponentValues();
/*  482 */     refreshList(null);
/*      */   }
/*      */ 
/*      */   protected void loadServiceDataInfo()
/*      */   {
/*  487 */     Properties props = this.m_helper.m_props;
/*      */ 
/*  489 */     loadBitFlags(Integer.toString(this.m_idcServiceData.m_accessLevel), ACCESS_LEVEL_DEF, props, false);
/*      */ 
/*  492 */     String service = "";
/*  493 */     if (this.m_idcServiceData.m_classID != null)
/*      */     {
/*  495 */       service = this.m_idcServiceData.m_classID;
/*  496 */       this.m_helper.m_props.put("serviceClasschoice", service);
/*      */     }
/*  498 */     props.put("serviceClass", service);
/*      */ 
/*  500 */     String template = "";
/*  501 */     if (this.m_idcServiceData.m_htmlPage != null)
/*      */     {
/*  503 */       template = this.m_idcServiceData.m_htmlPage;
/*  504 */       this.m_helper.m_props.put("templatePagechoice", template);
/*      */     }
/*  506 */     props.put("templatePage", template);
/*      */ 
/*  508 */     String serviceType = "";
/*  509 */     if ((this.m_idcServiceData.m_serviceType != null) && (!this.m_idcServiceData.m_serviceType.equalsIgnoreCase("null")))
/*      */     {
/*  511 */       serviceType = this.m_idcServiceData.m_serviceType;
/*      */     }
/*  513 */     props.put("serviceType", serviceType);
/*      */ 
/*  515 */     String errMsg = "";
/*  516 */     if (this.m_idcServiceData.m_errorMsg != null)
/*      */     {
/*  518 */       errMsg = this.m_idcServiceData.m_errorMsg;
/*      */     }
/*  520 */     props.put("errMsg", errMsg);
/*      */ 
/*  522 */     String subjects = null;
/*  523 */     if (this.m_idcServiceData.m_subjects != null)
/*      */     {
/*  525 */       subjects = StringUtils.createString(this.m_idcServiceData.m_subjects, ',', ',');
/*      */     }
/*  527 */     props.put("subjects", subjects);
/*      */   }
/*      */ 
/*      */   protected void info()
/*      */   {
/*  533 */     this.m_isNewListItem = false;
/*  534 */     int index = getIndex(IdcMessageFactory.lc("csCompWizSelectParamForInfo", new Object[0]));
/*  535 */     if (index < 0)
/*      */     {
/*  537 */       return;
/*      */     }
/*  539 */     Properties props = this.m_list.getDataAt(index);
/*  540 */     String function = props.getProperty("function");
/*      */ 
/*  542 */     addOrEditEx(props, IdcMessageFactory.lc("csCompWizSvcActionMsg", new Object[] { function }), true);
/*      */   }
/*      */ 
/*      */   protected void add()
/*      */   {
/*  548 */     this.m_isNewListItem = true;
/*  549 */     addOrEdit(null, IdcMessageFactory.lc("csCompWizSvcAddAction", new Object[0]));
/*      */   }
/*      */ 
/*      */   protected void edit()
/*      */   {
/*  555 */     this.m_isNewListItem = false;
/*  556 */     int index = getIndex(IdcMessageFactory.lc("csCompWizSelectParamToEdit", new Object[0]));
/*      */ 
/*  558 */     if (index < 0)
/*      */     {
/*  560 */       return;
/*      */     }
/*      */ 
/*  564 */     Properties props = this.m_list.getDataAt(index);
/*  565 */     String function = props.getProperty("function");
/*      */ 
/*  567 */     addOrEdit(props, IdcMessageFactory.lc("csCompWizSvcActionMsg", new Object[] { function }));
/*      */   }
/*      */ 
/*      */   protected void addOrEdit(Properties props, IdcMessage title)
/*      */   {
/*  572 */     addOrEditEx(props, title, false);
/*      */   }
/*      */ 
/*      */   protected void addOrEditEx(Properties props, IdcMessage title, boolean isInfo)
/*      */   {
/*  577 */     this.m_dlgHelper = new DialogHelper(this.m_systemInterface, LocaleResources.localizeMessage(null, title, null).toString(), true);
/*  578 */     this.m_dlgHelper.m_helpPage = DialogHelpTable.getHelpPage("CW_AddEditAction");
/*      */ 
/*  580 */     JPanel mainPanel = this.m_dlgHelper.m_mainPanel;
/*  581 */     this.m_dlgHelper.makePanelGridBag(mainPanel, 2);
/*      */ 
/*  583 */     DialogCallback okCallback = new DialogCallback()
/*      */     {
/*      */       public boolean handleDialogEvent(ActionEvent e)
/*      */       {
/*      */         try
/*      */         {
/*  590 */           return EditServicePanel.this.onOk();
/*      */         }
/*      */         catch (ServiceException exp)
/*      */         {
/*  594 */           IdcMessage msg = IdcMessageFactory.lc("csCompWizSvcAddActionError", new Object[0]);
/*  595 */           if (!EditServicePanel.this.m_isNewListItem)
/*      */           {
/*  597 */             String function = this.m_dlgHelper.m_props.getProperty("function");
/*  598 */             msg = IdcMessageFactory.lc("csCompWizSvcAddActionFunctionError", new Object[] { function });
/*      */           }
/*  600 */           CWizardGuiUtils.reportError(EditServicePanel.this.m_systemInterface, exp, msg);
/*  601 */         }return false;
/*      */       }
/*      */     };
/*  605 */     if (isInfo)
/*      */     {
/*  607 */       this.m_dlgHelper.addClose(null);
/*      */     }
/*      */     else
/*      */     {
/*  611 */       this.m_dlgHelper.addOK(okCallback);
/*  612 */       this.m_dlgHelper.addCancel(null);
/*  613 */       this.m_dlgHelper.addHelp(null);
/*      */     }
/*      */     try
/*      */     {
/*  617 */       addUI(mainPanel, props, isInfo);
/*      */ 
/*  620 */       if (this.m_dlgHelper.prompt() == 1)
/*      */       {
/*  622 */         return;
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  627 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addUI(JPanel mainPanel, Properties props, boolean isInfo)
/*      */     throws DataException, ServiceException
/*      */   {
/*  634 */     GridBagHelper gbh = this.m_dlgHelper.m_gridHelper;
/*  635 */     ComboChoice fchoice = new ComboChoice();
/*  636 */     DisplayChoice typeChoice = new DisplayChoice();
/*      */ 
/*  638 */     typeChoice.init(TYPE_DEF);
/*      */ 
/*  640 */     ItemListener listener = new ItemListener(typeChoice, fchoice)
/*      */     {
/*      */       public void itemStateChanged(ItemEvent e)
/*      */       {
/*  644 */         int state = e.getStateChange();
/*  645 */         if (state != 1)
/*      */         {
/*  647 */           return;
/*      */         }
/*      */ 
/*  650 */         String item = this.val$typeChoice.getSelectedInternalValue();
/*      */         try
/*      */         {
/*  653 */           this.val$fchoice.setText("");
/*  654 */           EditServicePanel.this.selectFunctionChoice(this.val$fchoice, item);
/*      */         }
/*      */         catch (Exception exp)
/*      */         {
/*  658 */           CWizardGuiUtils.reportError(EditServicePanel.this.m_systemInterface, exp, (IdcMessage)null);
/*      */         }
/*      */       }
/*      */     };
/*  662 */     typeChoice.addItemListener(listener);
/*      */ 
/*  664 */     if (isInfo)
/*      */     {
/*  666 */       this.m_dlgHelper.addLabelDisplayPairEx(mainPanel, this.m_systemInterface.getString("csCompWizLabelType2"), 30, "actionType", false);
/*      */     }
/*      */     else
/*      */     {
/*  671 */       this.m_dlgHelper.addLabelFieldPairEx(mainPanel, this.m_systemInterface.getString("csCompWizLabelType2"), typeChoice, "actionType", false);
/*      */     }
/*      */ 
/*  675 */     gbh.addEmptyRow(mainPanel);
/*      */ 
/*  677 */     if (isInfo)
/*      */     {
/*  679 */       this.m_dlgHelper.addLabelDisplayPairEx(mainPanel, this.m_systemInterface.getString("csCompWizLabelAction2"), 30, "function", false);
/*      */     }
/*      */     else
/*      */     {
/*  684 */       this.m_dlgHelper.addLabelFieldPairEx(mainPanel, this.m_systemInterface.getString("csCompWizLabelAction2"), fchoice, "function", false);
/*      */     }
/*      */ 
/*  687 */     gbh.addEmptyRow(mainPanel);
/*      */ 
/*  689 */     if (isInfo)
/*      */     {
/*  691 */       this.m_dlgHelper.addLabelDisplayPair(mainPanel, this.m_systemInterface.getString("csCompWizLabelParams"), 30, "");
/*      */     }
/*      */     else
/*      */     {
/*  696 */       this.m_dlgHelper.addLabelFieldPair(mainPanel, this.m_systemInterface.getString("csCompWizLabelParams"), new CustomText(LocaleResources.getString("csCompWizSvcParamParseMsg", null), 80), "");
/*      */     }
/*      */ 
/*  701 */     gbh.prepareAddLastRowElement();
/*  702 */     if (isInfo)
/*      */     {
/*  704 */       CustomLabel lbl = new CustomLabel();
/*  705 */       lbl.setMinWidth(30);
/*  706 */       this.m_dlgHelper.addExchangeComponent(mainPanel, lbl, "parameters");
/*      */     }
/*      */     else
/*      */     {
/*  710 */       this.m_dlgHelper.addExchangeComponent(mainPanel, new JTextField(30), "parameters");
/*      */     }
/*      */ 
/*  713 */     LongTextCustomLabel cmLabel = new LongTextCustomLabel("");
/*  714 */     if (isInfo)
/*      */     {
/*  716 */       this.m_dlgHelper.addLabelDisplayPair(mainPanel, this.m_systemInterface.getString("csCompWizLabelControlMask"), 30, "controlMask");
/*      */     }
/*      */     else
/*      */     {
/*  721 */       this.m_dlgHelper.addLabelFieldPair(mainPanel, this.m_systemInterface.getString("csCompWizLabelControlMask"), cmLabel, "controlMask");
/*      */     }
/*      */ 
/*  725 */     JPanel cmPanel = new PanePanel();
/*  726 */     this.m_helper.makePanelGridBag(cmPanel, 1);
/*      */ 
/*  728 */     for (int i = 0; i < CONTROL_MASK_DEF.length; ++i)
/*      */     {
/*  730 */       JCheckBox box = new JCheckBox(LocaleResources.localizeMessage(CONTROL_MASK_DEF[i][1], null));
/*      */ 
/*  732 */       box.setName(CONTROL_MASK_DEF[i][2]);
/*  733 */       box.setEnabled(!isInfo);
/*      */ 
/*  735 */       ItemListener iListener = new ItemListener(box, cmLabel)
/*      */       {
/*      */         public void itemStateChanged(ItemEvent e)
/*      */         {
/*  739 */           String name = this.val$box.getName();
/*  740 */           String tempStr = this.val$cmLabel.getOriginalText();
/*  741 */           Vector flags = StringUtils.parseArray(tempStr, ',', ',');
/*  742 */           if (this.val$box.isSelected())
/*      */           {
/*  744 */             flags.addElement(name);
/*      */           }
/*      */           else
/*      */           {
/*  748 */             for (int count = 0; count < flags.size(); ++count)
/*      */             {
/*  750 */               if (!name.equals(flags.elementAt(count)))
/*      */                 continue;
/*  752 */               flags.removeElementAt(count);
/*      */             }
/*      */           }
/*      */ 
/*  756 */           tempStr = StringUtils.createString(flags, ',', ',');
/*  757 */           this.val$cmLabel.setText(tempStr);
/*      */         }
/*      */       };
/*  760 */       box.addItemListener(iListener);
/*  761 */       gbh.prepareAddLastRowElement();
/*  762 */       this.m_dlgHelper.addExchangeComponent(cmPanel, box, "is" + CONTROL_MASK_DEF[i][2]);
/*      */     }
/*      */ 
/*  765 */     gbh.addEmptyRowElement(mainPanel);
/*  766 */     gbh.prepareAddLastRowElement();
/*  767 */     this.m_dlgHelper.addComponent(mainPanel, cmPanel);
/*      */ 
/*  769 */     gbh.prepareAddLastRowElement();
/*  770 */     this.m_dlgHelper.addComponent(mainPanel, new CustomLabel(this.m_systemInterface.getString("csCompWizLabelErrorMsg2"), 1));
/*      */ 
/*  775 */     if (props != null)
/*      */     {
/*  777 */       String errMsg = props.getProperty("errMessage");
/*  778 */       if ((errMsg != null) && (errMsg.equalsIgnoreCase("null")))
/*      */       {
/*  780 */         props.put("errMessage", "");
/*      */       }
/*      */     }
/*  783 */     gbh.prepareAddLastRowElement();
/*  784 */     if (isInfo)
/*      */     {
/*  786 */       CustomLabel txtFld = new CustomLabel();
/*  787 */       txtFld.setMinWidth(60);
/*  788 */       this.m_dlgHelper.addExchangeComponent(mainPanel, txtFld, "errMessage");
/*      */     }
/*      */     else
/*      */     {
/*  792 */       this.m_dlgHelper.addExchangeComponent(mainPanel, new JTextField(60), "errMessage");
/*      */     }
/*      */ 
/*  796 */     String type = TYPE_DEF[0][0];
/*  797 */     if (!this.m_isNewListItem)
/*      */     {
/*  799 */       this.m_dlgHelper.m_props = props;
/*  800 */       type = props.getProperty("actionType");
/*  801 */       type = StringUtils.findString(TYPE_DEF, type, 1, 0);
/*  802 */       props.put("actionType", type);
/*      */ 
/*  804 */       String controlMask = props.getProperty("controlMask");
/*  805 */       if ((controlMask != null) && (controlMask.length() > 0))
/*      */       {
/*  807 */         loadBitFlags(controlMask, CONTROL_MASK_DEF, props, true);
/*      */       }
/*      */     }
/*  810 */     if (isInfo)
/*      */       return;
/*  812 */     initFunctionChoice();
/*  813 */     selectFunctionChoice(fchoice, type);
/*      */   }
/*      */ 
/*      */   protected void selectFunctionChoice(ComboChoice fchoice, String item)
/*      */     throws ServiceException, DataException
/*      */   {
/*  820 */     int choice = Integer.parseInt(item);
/*  821 */     Vector v = null;
/*      */ 
/*  823 */     switch (choice)
/*      */     {
/*      */     case 1:
/*      */     case 5:
/*  827 */       v = this.m_selectQueries;
/*  828 */       break;
/*      */     case 2:
/*  830 */       v = this.m_execQueries;
/*  831 */       break;
/*      */     case 3:
/*  833 */       v = CWizardUtils.getChoiceList("JavaMethods.common", "name", false);
/*      */     case 4:
/*      */     }
/*      */ 
/*  837 */     if (v != null)
/*      */     {
/*  839 */       fchoice.initChoiceList(v);
/*      */     }
/*      */     else
/*      */     {
/*  843 */       fchoice.removeAllItems();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void initFunctionChoice() throws ServiceException, DataException
/*      */   {
/*  849 */     this.m_selectQueries = new IdcVector();
/*  850 */     this.m_execQueries = new IdcVector();
/*      */ 
/*  852 */     DataResultSet drset = SharedObjects.getTable("IdcQueries");
/*      */ 
/*  854 */     if (drset == null)
/*      */     {
/*  856 */       throw new ServiceException("!csCompWizQueryTableNotLoaded");
/*      */     }
/*      */ 
/*  859 */     String[][] table = ResultSetUtils.createStringTable(drset, new String[] { "name" });
/*      */ 
/*  861 */     for (int i = 0; i < table.length; ++i)
/*      */     {
/*  863 */       String name = table[i][0];
/*  864 */       if (name == null)
/*      */         continue;
/*  866 */       if (name.startsWith("Q"))
/*      */       {
/*  868 */         this.m_selectQueries.addElement(name);
/*      */       } else {
/*  870 */         if ((!name.startsWith("I")) && (!name.startsWith("D")) && (!name.startsWith("U")))
/*      */           continue;
/*  872 */         this.m_execQueries.addElement(name);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean onOk()
/*      */     throws ServiceException
/*      */   {
/*  880 */     Properties props = this.m_dlgHelper.m_props;
/*  881 */     String id = props.getProperty("id");
/*  882 */     String type = props.getProperty("actionType");
/*  883 */     String function = props.getProperty("function");
/*  884 */     String parameters = null;
/*  885 */     String control = null;
/*  886 */     IdcMessage errMsg = null;
/*      */ 
/*  888 */     type = translateActionType(Integer.parseInt(type));
/*  889 */     errMsg = checkField(function, IdcMessageFactory.lc("csCompWizSvcActionFunctionMsg", new Object[0]), false, false);
/*      */ 
/*  891 */     if (errMsg != null)
/*      */     {
/*  893 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, errMsg);
/*  894 */       return false;
/*      */     }
/*      */ 
/*  897 */     control = calculateBitFlagsToString(CONTROL_MASK_DEF, props);
/*  898 */     parameters = formatSourceStr(props.getProperty("parameters"), false);
/*  899 */     String errorStr = formatSourceStr(props.getProperty("errMessage"), true);
/*      */ 
/*  901 */     Vector v = null;
/*  902 */     if (this.m_isNewListItem)
/*      */     {
/*  904 */       v = new IdcVector();
/*  905 */       id = Integer.toString(this.m_actionIdCount);
/*  906 */       v.addElement(id);
/*  907 */       v.addElement(type);
/*  908 */       v.addElement(function);
/*  909 */       v.addElement(parameters);
/*  910 */       v.addElement(control);
/*  911 */       v.addElement(errorStr);
/*  912 */       this.m_listData.addRow(v);
/*      */     }
/*      */     else
/*      */     {
/*  916 */       v = this.m_listData.findRow(0, id);
/*  917 */       if (v != null)
/*      */       {
/*  919 */         v.setElementAt(type, 1);
/*  920 */         v.setElementAt(function, 2);
/*  921 */         v.setElementAt(parameters, 3);
/*  922 */         v.setElementAt(control, 4);
/*  923 */         v.setElementAt(errorStr, 5);
/*      */       }
/*      */     }
/*      */ 
/*  927 */     refreshList(id);
/*  928 */     return true;
/*      */   }
/*      */ 
/*      */   protected String formatSourceStr(Object sourceObj, boolean addNullStr)
/*      */   {
/*  933 */     String sourceStr = null;
/*  934 */     if (sourceObj instanceof IdcMessage)
/*      */     {
/*  936 */       IdcMessage sourceMsg = (IdcMessage)sourceObj;
/*  937 */       sourceStr = sourceMsg.m_msgLocalized;
/*      */     }
/*  941 */     else if (sourceObj != null)
/*      */     {
/*  943 */       sourceStr = sourceObj.toString();
/*      */     }
/*      */ 
/*  946 */     if ((sourceStr == null) || (sourceStr.length() == 0))
/*      */     {
/*  948 */       if (addNullStr)
/*      */       {
/*  950 */         sourceStr = "null";
/*      */       }
/*      */       else
/*      */       {
/*  954 */         sourceStr = "";
/*      */       }
/*      */     }
/*  957 */     return sourceStr;
/*      */   }
/*      */ 
/*      */   protected String translateActionType(int aType) throws ServiceException
/*      */   {
/*  962 */     Integer intType = new Integer(aType);
/*      */ 
/*  964 */     return StringUtils.findString(TYPE_DEF, intType.toString(), 0, 1);
/*      */   }
/*      */ 
/*      */   protected void loadBitFlags(String val, String[][] bitmap, Properties props, boolean isControlMask)
/*      */   {
/*  970 */     String cmLabel = "";
/*  971 */     boolean isInt = isInteger(val);
/*      */ 
/*  973 */     for (int i = 0; i < bitmap.length; ++i)
/*      */     {
/*  975 */       String aname = bitmap[i][2];
/*  976 */       String flag = "false";
/*      */ 
/*  978 */       if (isInt)
/*      */       {
/*  980 */         int intVal = Integer.parseInt(val);
/*  981 */         Integer tempInt = Integer.decode(bitmap[i][0]);
/*  982 */         int tempIntVal = tempInt.intValue();
/*  983 */         if ((tempIntVal & intVal) != 0)
/*      */         {
/*  985 */           flag = "true";
/*      */ 
/*  987 */           if (isControlMask)
/*      */           {
/*  989 */             if (cmLabel.length() > 0)
/*      */             {
/*  991 */               cmLabel = cmLabel + ',';
/*      */             }
/*  993 */             cmLabel = cmLabel + aname;
/*      */           }
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  999 */         Vector flags = StringUtils.parseArray(val, ',', ',');
/* 1000 */         for (int j = 0; j < flags.size(); ++j)
/*      */         {
/* 1002 */           String key = (String)flags.elementAt(j);
/* 1003 */           if (!key.equals(aname))
/*      */             continue;
/* 1005 */           flag = "true";
/* 1006 */           break;
/*      */         }
/*      */       }
/*      */ 
/* 1010 */       props.put("is" + aname, flag);
/*      */     }
/*      */ 
/* 1013 */     if ((!isControlMask) || (!isInt))
/*      */       return;
/* 1015 */     props.put("controlMask", cmLabel);
/*      */   }
/*      */ 
/*      */   protected int calculateBitFlags(String[][] bitmap, Properties props)
/*      */   {
/* 1021 */     int bitFlags = 0;
/* 1022 */     for (int i = 0; i < bitmap.length; ++i)
/*      */     {
/* 1024 */       String cm = bitmap[i][2];
/* 1025 */       String cmtemp = props.getProperty("is" + cm);
/* 1026 */       if ((cmtemp == null) || (!StringUtils.convertToBool(cmtemp, false)))
/*      */         continue;
/* 1028 */       Integer tempInt = Integer.decode(bitmap[i][0]);
/* 1029 */       int tempIntVal = tempInt.intValue();
/* 1030 */       bitFlags += tempIntVal;
/*      */     }
/*      */ 
/* 1034 */     return bitFlags;
/*      */   }
/*      */ 
/*      */   protected String calculateBitFlagsToString(String[][] bitmap, Properties props)
/*      */   {
/* 1039 */     String bitFlags = "";
/* 1040 */     int size = bitmap.length;
/* 1041 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1043 */       String cm = bitmap[i][2];
/* 1044 */       String cmtemp = props.getProperty("is" + cm);
/* 1045 */       if ((cmtemp == null) || (!StringUtils.convertToBool(cmtemp, false)))
/*      */         continue;
/* 1047 */       if (bitFlags.length() > 0)
/*      */       {
/* 1049 */         bitFlags = bitFlags + ",";
/*      */       }
/* 1051 */       bitFlags = bitFlags + cm;
/*      */     }
/*      */ 
/* 1055 */     return bitFlags;
/*      */   }
/*      */ 
/*      */   protected void buildActionsResultSet() throws ServiceException
/*      */   {
/* 1060 */     this.m_listData = new DataResultSet(new String[] { "id", "actionType", "function", "parameters", "controlMask", "errMessage" });
/*      */ 
/* 1063 */     if (this.m_idcServiceData == null)
/*      */     {
/* 1065 */       return;
/*      */     }
/* 1067 */     int size = this.m_idcServiceData.m_actionList.size();
/*      */ 
/* 1069 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1071 */       String temp = (String)this.m_actions.elementAt(i);
/* 1072 */       Vector tempVector = StringUtils.parseArray(temp, ':', '*');
/*      */ 
/* 1074 */       String id = Integer.toString(this.m_actionIdCount++);
/* 1075 */       Action action = (Action)this.m_idcServiceData.m_actionList.elementAt(i);
/* 1076 */       Vector v = this.m_listData.createEmptyRow();
/* 1077 */       String type = translateActionType(action.m_type);
/*      */ 
/* 1079 */       v.setElementAt(id, 0);
/* 1080 */       v.setElementAt(type, 1);
/* 1081 */       v.setElementAt(action.m_function, 2);
/* 1082 */       v.setElementAt(StringUtils.createString(action.m_params, ',', '^'), 3);
/* 1083 */       v.setElementAt(tempVector.elementAt(3), 4);
/* 1084 */       v.setElementAt(action.m_errorMsg, 5);
/* 1085 */       this.m_listData.addRow(v);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean isInteger(String val)
/*      */   {
/* 1091 */     boolean isInteger = true;
/*      */     try
/*      */     {
/* 1094 */       Integer.parseInt(val);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1098 */       isInteger = false;
/*      */     }
/* 1100 */     return isInteger;
/*      */   }
/*      */ 
/*      */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*      */   {
/* 1108 */     String name = exchange.m_compName;
/* 1109 */     String val = exchange.m_compValue;
/*      */ 
/* 1111 */     IdcMessage errMsg = null;
/*      */ 
/* 1113 */     if (name.equals("Name"))
/*      */     {
/* 1115 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizSvcScriptName", new Object[0]), false, false);
/*      */     }
/* 1117 */     else if (name.equals("serviceClass"))
/*      */     {
/* 1119 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizSvcScriptClass", new Object[0]), false, false);
/*      */     }
/*      */     else
/*      */     {
/* 1123 */       return super.validateComponentValue(exchange);
/*      */     }
/*      */ 
/* 1126 */     if (errMsg != null)
/*      */     {
/* 1128 */       exchange.m_errorMessage = errMsg;
/* 1129 */       return false;
/*      */     }
/* 1131 */     return true;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1136 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79062 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.EditServicePanel
 * JD-Core Version:    0.5.4
 */