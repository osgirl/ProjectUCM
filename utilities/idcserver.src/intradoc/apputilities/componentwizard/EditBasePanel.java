/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
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
/*     */ public class EditBasePanel extends CWizardPanel
/*     */   implements ComponentBinder
/*     */ {
/*  64 */   public static final String[][] SELECTION_MAP = { { "name", "!csCompWizLabelName", "20" }, { "description", "!csCompWizLabelDesc", "30" } };
/*     */   protected JCheckBox m_showAll;
/*     */   protected UdlPanel m_list;
/*     */   protected DialogHelper m_dlgHelper;
/*     */   protected UdlPanel m_selectList;
/*     */   protected JButton m_upBtn;
/*     */   protected JButton m_downBtn;
/*     */   protected DataResultSet m_listData;
/*     */   protected DataResultSet m_selectListData;
/*     */   protected String m_extraInfo;
/*     */   protected ResourceWizard m_resWizard;
/*     */   protected int m_resourceType;
/*     */   protected boolean m_isNewListItem;
/*     */ 
/*     */   public EditBasePanel()
/*     */   {
/*  69 */     this.m_showAll = null;
/*  70 */     this.m_list = null;
/*  71 */     this.m_dlgHelper = null;
/*  72 */     this.m_selectList = null;
/*  73 */     this.m_upBtn = null;
/*  74 */     this.m_downBtn = null;
/*     */ 
/*  77 */     this.m_listData = null;
/*  78 */     this.m_selectListData = null;
/*     */ 
/*  80 */     this.m_resWizard = null;
/*  81 */     this.m_resourceType = -1;
/*     */ 
/*  84 */     this.m_isNewListItem = true;
/*     */   }
/*     */ 
/*     */   public void init(IntradocComponent component, String extraInfo, ContainerHelper helper, ResourceWizard rwizard, int editType) throws ServiceException
/*     */   {
/*  89 */     this.m_resWizard = rwizard;
/*  90 */     this.m_component = component;
/*  91 */     this.m_extraInfo = extraInfo;
/*  92 */     this.m_systemInterface = helper.m_exchange.m_sysInterface;
/*     */ 
/*  94 */     this.m_helper = new ContainerHelper();
/*  95 */     this.m_helper.attachToContainer(this, this.m_systemInterface, helper.m_props);
/*  96 */     this.m_helper.m_componentBinder = this;
/*     */ 
/*  98 */     this.m_helper.makePanelGridBag(this, 1);
/*  99 */     initUI(editType);
/* 100 */     this.m_helper.loadComponentValues();
/* 101 */     initControls(editType);
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */     throws ServiceException
/*     */   {
/* 107 */     setLayout(new BorderLayout());
/* 108 */     JPanel text = new CustomText("", 60);
/* 109 */     add("North", text);
/* 110 */     this.m_helper.m_exchange.addComponent("HelpMessage", text, null);
/*     */   }
/*     */ 
/*     */   public void initUI(int editType)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void initControls(int editType)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void setResourceType(int resourceType)
/*     */   {
/* 125 */     this.m_resourceType = resourceType;
/*     */   }
/*     */ 
/*     */   protected void addNewOrUseExistingPanel(JPanel panel, String colName, boolean addShowAll, boolean addSortList, ItemListener showAllListener, boolean isInfo, ActionListener showInfoListener)
/*     */   {
/* 132 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/* 133 */     this.m_helper.addLabelFieldPairEx(panel, LocaleResources.getString("csCompWizLabelName2", null), new CustomTextField(30), colName, false);
/*     */ 
/* 136 */     gbh.prepareAddRowElement();
/* 137 */     this.m_helper.addComponent(panel, new CustomLabel());
/*     */ 
/* 139 */     gbh.prepareAddLastRowElement();
/* 140 */     JButton selectBtn = new JButton(LocaleResources.getString("csCompWizCommandSelect", null));
/* 141 */     this.m_helper.addComponent(panel, selectBtn);
/*     */ 
/* 143 */     ActionListener listener = new ActionListener(addShowAll, addSortList, showAllListener, isInfo, showInfoListener)
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 147 */         EditBasePanel.this.selectUseExistingEx(this.val$addShowAll, this.val$addSortList, this.val$showAllListener, this.val$isInfo, this.val$showInfoListener);
/*     */       }
/*     */     };
/* 150 */     selectBtn.addActionListener(listener);
/*     */   }
/*     */ 
/*     */   protected void addChoiceList(JPanel panel, String mergeTable, String colname, String label, String name)
/*     */     throws DataException, ServiceException
/*     */   {
/* 156 */     addChoiceListEx(panel, mergeTable, colname, label, name, true);
/*     */   }
/*     */ 
/*     */   protected void addChoiceListEx(JPanel panel, String mergeTable, String colname, String label, String name, boolean isRowEnd)
/*     */     throws DataException, ServiceException
/*     */   {
/* 162 */     Vector list = CWizardUtils.getChoiceList(mergeTable, colname, false);
/* 163 */     ComboChoice tchoice = new ComboChoice();
/* 164 */     if ((list != null) && (list.size() > 0))
/*     */     {
/* 166 */       tchoice.initChoiceList(list);
/*     */     }
/*     */ 
/* 169 */     this.m_helper.addLabelFieldPairEx(panel, label, tchoice, name, isRowEnd);
/*     */   }
/*     */ 
/*     */   protected void addDescrpAndComponent(JPanel panel, String label, String msg, Component comp, String name, boolean noEmptyElement)
/*     */   {
/* 175 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/* 176 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 177 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*     */ 
/* 179 */     gbh.prepareAddLastRowElement();
/* 180 */     this.m_helper.addComponent(panel, new CustomText(LocaleResources.localizeMessage(msg, null), 100));
/*     */ 
/* 182 */     this.m_helper.addLabelFieldPairEx(panel, LocaleResources.localizeMessage(label, null), comp, name, noEmptyElement);
/*     */ 
/* 184 */     if (noEmptyElement)
/*     */       return;
/* 186 */     gbh.addEmptyRow(panel);
/*     */   }
/*     */ 
/*     */   protected JPanel addUpDownButtons(String listTitle, String colName)
/*     */   {
/* 192 */     JPanel panel = new PanePanel();
/* 193 */     panel.setLayout(new BorderLayout());
/*     */ 
/* 196 */     JPanel titlePanel = new PanePanel();
/* 197 */     titlePanel.add(new CustomLabel(LocaleResources.localizeMessage(listTitle, null), 2));
/*     */ 
/* 201 */     this.m_upBtn = new JButton(LocaleResources.getString("csCompWizCommandUp", null));
/* 202 */     this.m_downBtn = new JButton(LocaleResources.getString("csCompWizCommandDown", null));
/*     */ 
/* 204 */     JPanel btnPanel = new PanePanel();
/* 205 */     btnPanel.add(this.m_upBtn);
/* 206 */     btnPanel.add(this.m_downBtn);
/* 207 */     btnPanel.add(new CustomLabel("           "));
/*     */ 
/* 211 */     panel.add("West", titlePanel);
/* 212 */     panel.add("East", btnPanel);
/*     */ 
/* 214 */     ActionListener listener = new Object(colName)
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 218 */         Object obj = e.getSource();
/*     */ 
/* 220 */         int sel = EditBasePanel.this.m_list.getSelectedIndex();
/*     */         int newSel;
/*     */         int newSel;
/* 221 */         if (obj == EditBasePanel.this.m_upBtn)
/*     */         {
/* 223 */           newSel = sel - 1;
/*     */         }
/*     */         else
/*     */         {
/* 227 */           newSel = sel + 1;
/*     */         }
/* 229 */         if ((newSel < 0) || (newSel >= EditBasePanel.this.m_list.getNumRows()))
/*     */           return;
/* 231 */         Properties props = EditBasePanel.this.m_list.getDataAt(sel);
/* 232 */         String col = props.getProperty(this.val$colName);
/* 233 */         Vector v = EditBasePanel.this.m_listData.findRow(0, col);
/* 234 */         EditBasePanel.this.m_listData.deleteCurrentRow();
/* 235 */         EditBasePanel.this.m_listData.insertRowAt(v, newSel);
/* 236 */         EditBasePanel.this.refreshList(col);
/*     */       }
/*     */     };
/* 240 */     this.m_upBtn.addActionListener(listener);
/* 241 */     this.m_downBtn.addActionListener(listener);
/*     */ 
/* 243 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 247 */         int state = e.getStateChange();
/* 248 */         switch (state)
/*     */         {
/*     */         case 1:
/* 251 */           int index = EditBasePanel.this.m_list.getSelectedIndex();
/* 252 */           boolean enableUp = false;
/* 253 */           boolean enableDown = false;
/*     */ 
/* 255 */           if (index >= 0)
/*     */           {
/* 257 */             if (index > 0)
/*     */             {
/* 259 */               enableUp = true;
/*     */             }
/* 261 */             if (index < EditBasePanel.this.m_list.getNumRows() - 1)
/*     */             {
/* 263 */               enableDown = true;
/*     */             }
/*     */           }
/*     */ 
/* 267 */           EditBasePanel.this.enableDisableListButtons(true, enableUp, enableDown);
/* 268 */           break;
/*     */         case 2:
/* 270 */           EditBasePanel.this.enableDisableListButtons(false, false, false);
/*     */         }
/*     */       }
/*     */     };
/* 275 */     this.m_list.addItemListener(iListener);
/* 276 */     enableDisableListButtons(false, false, false);
/*     */ 
/* 278 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void enableDisableListButtons(boolean enableList, boolean enableUp, boolean enableDown)
/*     */   {
/* 283 */     this.m_list.enableDisable(enableList);
/* 284 */     if (this.m_upBtn != null)
/*     */     {
/* 286 */       this.m_upBtn.setEnabled(enableUp);
/*     */     }
/* 288 */     if (this.m_downBtn == null)
/*     */       return;
/* 290 */     this.m_downBtn.setEnabled(enableDown);
/*     */   }
/*     */ 
/*     */   protected JPanel addUdlPanelCommandButtons(boolean isInfo)
/*     */   {
/* 296 */     ActionListener listener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 300 */         String cmdStr = e.getActionCommand();
/*     */ 
/* 302 */         if (cmdStr.equals("add"))
/*     */         {
/* 304 */           EditBasePanel.this.add();
/*     */         }
/* 306 */         else if (cmdStr.equals("delete"))
/*     */         {
/* 308 */           EditBasePanel.this.delete();
/*     */         }
/* 310 */         else if (cmdStr.equals("edit"))
/*     */         {
/* 312 */           EditBasePanel.this.edit();
/*     */         } else {
/* 314 */           if (!cmdStr.equals("info"))
/*     */             return;
/* 316 */           EditBasePanel.this.info();
/*     */         }
/*     */       }
/*     */     };
/* 321 */     return CWizardGuiUtils.addUdlPanelCommandButtons(this.m_helper, this.m_list, listener, isInfo);
/*     */   }
/*     */ 
/*     */   protected void selectUseExisting(boolean addShowAll, boolean addSortList, ItemListener showAllListener)
/*     */   {
/* 327 */     selectUseExistingEx(addShowAll, addSortList, showAllListener, false, null);
/*     */   }
/*     */ 
/*     */   protected void selectUseExistingEx(boolean addShowAll, boolean addSortList, ItemListener showAllListener, boolean isAddInfo, ActionListener showInfoListener)
/*     */   {
/* 333 */     this.m_dlgHelper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("csCompWizTitleSelectResource", null), true);
/*     */ 
/* 335 */     this.m_dlgHelper.m_helpPage = DialogHelpTable.getHelpPage("CW_SelectFromExisting");
/* 336 */     JPanel panel = this.m_dlgHelper.m_mainPanel;
/*     */ 
/* 338 */     this.m_dlgHelper.makePanelGridBag(panel, 1);
/* 339 */     GridBagHelper gbh = this.m_dlgHelper.m_gridHelper;
/* 340 */     gbh.m_gc.weightx = 1.0D;
/* 341 */     gbh.m_gc.weighty = 1.0D;
/*     */ 
/* 343 */     this.m_selectList = createUdlPanel("", 400, 15, "SelectList", true, SELECTION_MAP, SELECTION_MAP[0][0], false);
/*     */ 
/* 346 */     ActionListener listener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 350 */         EditBasePanel.this.onSelect();
/* 351 */         EditBasePanel.this.m_dlgHelper.close();
/*     */       }
/*     */     };
/* 354 */     this.m_selectList.m_list.addActionListener(listener);
/*     */ 
/* 356 */     if (addShowAll)
/*     */     {
/* 358 */       this.m_showAll = new CustomCheckbox(this.m_systemInterface.getString("csCompWizShowAll"));
/*     */ 
/* 360 */       JPanel pnl = new PanePanel();
/* 361 */       this.m_helper.makePanelGridBag(pnl, 2);
/* 362 */       gbh.m_gc.anchor = 17;
/* 363 */       this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 364 */       this.m_helper.addComponent(pnl, this.m_showAll);
/*     */ 
/* 366 */       if (isAddInfo)
/*     */       {
/* 368 */         this.m_helper.m_gridHelper.prepareAddLastRowElement(13);
/* 369 */         this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/*     */ 
/* 371 */         String label = this.m_systemInterface.getString("csCompWizCommandInfo");
/* 372 */         JButton infoButton = this.m_selectList.addButton(label, true);
/* 373 */         infoButton.addActionListener(showInfoListener);
/* 374 */         this.m_helper.addComponent(pnl, infoButton);
/*     */       }
/*     */ 
/* 377 */       this.m_selectList.add("North", pnl);
/* 378 */       if (showAllListener != null)
/*     */       {
/* 380 */         this.m_showAll.addItemListener(showAllListener);
/*     */       }
/*     */ 
/* 383 */       gbh.m_gc.fill = 1;
/* 384 */       gbh.m_gc.weightx = 1.0D;
/* 385 */       gbh.m_gc.weighty = 1.0D;
/*     */     }
/*     */ 
/* 388 */     gbh.prepareAddLastRowElement();
/* 389 */     this.m_dlgHelper.addComponent(panel, this.m_selectList);
/*     */     try
/*     */     {
/* 393 */       initSelectListData(false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 397 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */     }
/* 399 */     refreshSelectList(null);
/*     */ 
/* 401 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 406 */         return EditBasePanel.this.onSelect();
/*     */       }
/*     */     };
/* 409 */     this.m_dlgHelper.addOK(okCallback);
/* 410 */     this.m_dlgHelper.addCancel(null);
/* 411 */     this.m_dlgHelper.addHelp(null);
/* 412 */     this.m_dlgHelper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initSelectListData(boolean isAll)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   protected void refreshList(String selObj)
/*     */   {
/* 423 */     if (this.m_listData == null)
/*     */       return;
/* 425 */     this.m_list.refreshList(this.m_listData, selObj);
/*     */ 
/* 427 */     if (selObj == null)
/*     */     {
/* 429 */       this.m_list.enableDisable(false);
/* 430 */       enableDisableListButtons(false, false, false);
/*     */     }
/*     */     else
/*     */     {
/* 434 */       this.m_list.enableDisable(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadData()
/*     */   {
/* 441 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public boolean validateEntries()
/*     */   {
/* 446 */     boolean validate = StringUtils.convertToBool(this.m_helper.m_props.getProperty("validateEntries"), true);
/*     */ 
/* 448 */     this.m_helper.m_exchange.m_useMsgBox = validate;
/* 449 */     return this.m_helper.retrieveComponentValues();
/*     */   }
/*     */ 
/*     */   protected void add()
/*     */   {
/*     */   }
/*     */ 
/*     */   protected void edit()
/*     */   {
/*     */   }
/*     */ 
/*     */   protected void info()
/*     */   {
/*     */   }
/*     */ 
/*     */   protected void delete()
/*     */   {
/* 469 */     int index = getIndex(IdcMessageFactory.lc("csCompWizDeleteError", new Object[0]));
/*     */ 
/* 471 */     if (index < 0)
/*     */     {
/* 473 */       return;
/*     */     }
/* 475 */     this.m_listData.deleteRow(index);
/* 476 */     refreshList(null);
/*     */   }
/*     */ 
/*     */   protected int getIndex(IdcMessage msg)
/*     */   {
/* 481 */     int index = this.m_list.getSelectedIndex();
/* 482 */     if (index < 0)
/*     */     {
/* 484 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, msg);
/*     */     }
/*     */ 
/* 487 */     return index;
/*     */   }
/*     */ 
/*     */   protected void refreshSelectList(String selObj)
/*     */   {
/* 492 */     this.m_selectList.refreshList(this.m_selectListData, selObj);
/*     */   }
/*     */ 
/*     */   protected boolean onSelect()
/*     */   {
/* 502 */     return true;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 509 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 514 */     return this.m_helper.validateComponentValue(exchange);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 519 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.EditBasePanel
 * JD-Core Version:    0.5.4
 */