/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.workflow.JumpClausesData;
/*     */ import java.awt.CardLayout;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ScriptJumpPanel extends ScriptPanelBase
/*     */   implements ItemListener
/*     */ {
/*     */   public int m_currentIndex;
/*     */   public JumpClausesData m_currentJump;
/*     */   protected JPanel m_curView;
/*     */   protected JPanel m_flipPanel;
/*     */   protected Hashtable m_flipComponents;
/*     */   protected JButton[] m_controlBtns;
/*     */ 
/*     */   public ScriptJumpPanel()
/*     */   {
/*  64 */     this.m_currentIndex = -1;
/*  65 */     this.m_currentJump = null;
/*     */ 
/*  67 */     this.m_curView = null;
/*  68 */     this.m_flipPanel = null;
/*  69 */     this.m_flipComponents = null;
/*  70 */     this.m_controlBtns = null;
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  76 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/*  80 */     JPanel jumpPanel = createJumpPanel();
/*     */ 
/*  82 */     createScriptPanels();
/*     */ 
/*  85 */     gh.m_gc.insets = new Insets(5, 5, 5, 5);
/*  86 */     gh.m_gc.weightx = 1.0D;
/*  87 */     gh.m_gc.weighty = 0.0D;
/*  88 */     gh.m_gc.fill = 2;
/*  89 */     this.m_helper.addLastComponentInRow(this, jumpPanel);
/*  90 */     gh.m_gc.fill = 1;
/*  91 */     gh.m_gc.weighty = 1.0D;
/*  92 */     this.m_helper.addLastComponentInRow(this, this.m_flipPanel);
/*     */ 
/*  94 */     refreshJumpList(null);
/*     */   }
/*     */ 
/*     */   protected JPanel createJumpPanel()
/*     */   {
/*  99 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/* 101 */     JPanel jumpPanel = new PanePanel();
/* 102 */     this.m_helper.makePanelGridBag(jumpPanel, 1);
/* 103 */     gh.prepareAddRowElement();
/* 104 */     this.m_helper.addComponent(jumpPanel, new CustomLabel(LocaleResources.getString("apLabelJumps", this.m_cxt), 1));
/*     */ 
/* 107 */     this.m_jumpList = new UdlPanel(null, null, 200, 5, "WorkflowScriptJumps", false);
/* 108 */     this.m_jumpList.setVisibleColumns("wfJumpName");
/* 109 */     this.m_jumpList.init();
/* 110 */     this.m_jumpList.useDefaultListener();
/* 111 */     this.m_jumpList.addItemListener(this);
/*     */ 
/* 113 */     this.m_helper.addComponent(jumpPanel, this.m_jumpList);
/*     */ 
/* 116 */     String[][] buttonInfo = { { LocaleResources.getString("apDlgButtonAdd", this.m_cxt), "add", "0" }, { LocaleResources.getString("apDlgButtonEdit", this.m_cxt), "edit", "1" }, { LocaleResources.getString("apLabelDelete", this.m_cxt), "delete", "1" } };
/*     */ 
/* 122 */     JPanel btnPanel = new PanePanel();
/* 123 */     GridLayout gl = new GridLayout(0, 1);
/* 124 */     gl.setVgap(5);
/* 125 */     btnPanel.setLayout(gl);
/*     */ 
/* 127 */     this.m_controlBtns = new JButton[buttonInfo.length];
/* 128 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/* 130 */       boolean isControlled = StringUtils.convertToBool(buttonInfo[i][2], false);
/* 131 */       JButton btn = this.m_jumpList.addButton(buttonInfo[i][0], isControlled);
/* 132 */       btn.setActionCommand(buttonInfo[i][1]);
/* 133 */       btn.addActionListener(this);
/* 134 */       btnPanel.add(btn);
/* 135 */       btn.setEnabled(!isControlled);
/* 136 */       this.m_controlBtns[i] = btn;
/*     */     }
/*     */ 
/* 139 */     gh.prepareAddLastRowElement();
/* 140 */     gh.m_gc.weightx = 0.0D;
/* 141 */     gh.m_gc.weighty = 0.0D;
/* 142 */     Insets oldInsets = gh.m_gc.insets;
/* 143 */     gh.m_gc.insets = new Insets(15, 5, 10, 5);
/* 144 */     this.m_helper.addComponent(jumpPanel, btnPanel);
/* 145 */     gh.m_gc.insets = oldInsets;
/*     */ 
/* 147 */     return jumpPanel;
/*     */   }
/*     */ 
/*     */   protected void createScriptPanels()
/*     */   {
/* 152 */     this.m_flipPanel = new PanePanel();
/* 153 */     CardLayout cardLayout = new CardLayout();
/* 154 */     this.m_flipPanel.setLayout(cardLayout);
/* 155 */     this.m_flipComponents = new Hashtable();
/*     */ 
/* 157 */     JPanel emptyView = new CustomPanel();
/* 158 */     this.m_curView = emptyView;
/* 159 */     this.m_flipPanel.add("Empty", emptyView);
/* 160 */     this.m_flipComponents.put("Empty", emptyView);
/*     */ 
/* 163 */     initScriptHelper();
/*     */ 
/* 165 */     cardLayout.show(this.m_flipPanel, "Empty");
/*     */   }
/*     */ 
/*     */   protected void initScriptHelper()
/*     */   {
/* 170 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/* 172 */     JPanel scriptDefPanel = new PanePanel();
/* 173 */     gh.m_gc.weightx = 1.0D;
/* 174 */     gh.m_gc.weighty = 1.0D;
/* 175 */     gh.m_gc.fill = 1;
/*     */ 
/* 177 */     this.m_scriptHelper.createStandardScriptPanel(this.m_helper, scriptDefPanel, this.m_context.getSharedContext());
/*     */ 
/* 181 */     ResultSet metaFields = SharedObjects.getTable("DocMetaDefinition");
/* 182 */     ViewFields docFieldsObj = new ViewFields(this.m_cxt);
/*     */     try
/*     */     {
/* 186 */       ViewFieldDef fieldDef = docFieldsObj.addField("lastEntryTs", LocaleResources.getString("apTitleStepEntryDate", this.m_cxt));
/*     */ 
/* 188 */       fieldDef.m_type = "date";
/*     */ 
/* 190 */       fieldDef = docFieldsObj.addField("entryCount", LocaleResources.getString("apTitleStepEntryCount", this.m_cxt));
/*     */ 
/* 192 */       fieldDef.m_type = "int";
/*     */ 
/* 194 */       fieldDef = docFieldsObj.addField("wfAction", LocaleResources.getString("apTitleWorkflowAction", this.m_cxt));
/*     */ 
/* 197 */       docFieldsObj.addStandardDocFields();
/* 198 */       docFieldsObj.addField("dFormat", LocaleResources.getString("apTitlePrimaryFileFormat", this.m_cxt));
/*     */ 
/* 200 */       docFieldsObj.addDocDateFields(true, false);
/* 201 */       docFieldsObj.addMetaFields(metaFields);
/*     */ 
/* 203 */       Vector docFields = docFieldsObj.m_viewFields;
/* 204 */       this.m_scriptHelper.setFieldList(docFields, null);
/* 205 */       this.m_scriptHelper.setDisplayMaps(docFieldsObj.m_tableFields.m_displayMaps);
/* 206 */       setCustomValueDisplayMaps();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 210 */       this.m_context.reportError(e, IdcMessageFactory.lc("apErrorCreatingScriptFieldList", new Object[0]));
/*     */     }
/*     */ 
/* 213 */     this.m_scriptHelper.setData(new JumpClausesData(), null);
/*     */     try
/*     */     {
/* 217 */       this.m_scriptHelper.loadData();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 221 */       this.m_context.reportError(e, IdcMessageFactory.lc("apUnableToInitScriptBuilder", new Object[0]));
/*     */     }
/*     */ 
/* 224 */     this.m_flipPanel.add("Script", scriptDefPanel);
/* 225 */     this.m_flipComponents.put("Script", scriptDefPanel);
/*     */   }
/*     */ 
/*     */   protected void setCustomValueDisplayMaps()
/*     */   {
/* 231 */     this.m_scriptHelper.setDisplayMap("prevDates", new String[][] { { "dateCurrent(-1)", LocaleResources.getString("apOneDayAgo", this.m_cxt) }, { "dateCurrent(-7)", LocaleResources.getString("apOneWeekAgo", this.m_cxt) }, { "dateCurrent(-28)", LocaleResources.getString("apFourWeeksAgo", this.m_cxt) } });
/*     */ 
/* 238 */     this.m_scriptHelper.setFieldOptionListKey("lastEntryTs", "prevDates", "combo");
/* 239 */     this.m_scriptHelper.setFieldOptionListKey("dInDate", "prevDates", "combo");
/* 240 */     this.m_scriptHelper.setFieldOptionListKey("dCreateDate", "prevDates", "combo");
/*     */ 
/* 242 */     this.m_scriptHelper.setDisplayMap("futureDates", new String[][] { { "dateCurrent(1)", LocaleResources.getString("apOneDay", this.m_cxt) }, { "dateCurrent(7)", LocaleResources.getString("apOneWeek", this.m_cxt) }, { "dateCurrent(28)", LocaleResources.getString("apFourWeeks", this.m_cxt) } });
/*     */ 
/* 249 */     this.m_scriptHelper.setFieldOptionListKey("dOutDate", "futureDates", "combo");
/*     */ 
/* 251 */     this.m_scriptHelper.setDisplayMap("wfActions", new String[][] { { "APPROVE", LocaleResources.getString("apActionApprove", this.m_cxt) }, { "REJECT", LocaleResources.getString("apActionReject", this.m_cxt) }, { "CHECKIN", LocaleResources.getString("apActionCheckin", this.m_cxt) }, { "CHECKOUT", LocaleResources.getString("apActionCheckout", this.m_cxt) }, { "UNDO_CHECKOUT", LocaleResources.getString("apActionUndoCheckout", this.m_cxt) }, { "CONVERSION", LocaleResources.getString("apActionConverted", this.m_cxt) }, { "META_UPDATE", LocaleResources.getString("apActionMetadataUpdate", this.m_cxt) }, { "TIMED_UPDATE", LocaleResources.getString("apActionTimedUpdate", this.m_cxt) }, { "RESUBMIT", LocaleResources.getString("apActionResubmit", this.m_cxt) }, { "UPDATE", LocaleResources.getString("apActionAnyUpdate", this.m_cxt) } });
/*     */ 
/* 265 */     this.m_scriptHelper.setFieldOptionListKey("wfAction", "wfActions", "");
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 273 */     checkSelection();
/* 274 */     enableDisable();
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 279 */     CardLayout panelHandler = (CardLayout)this.m_flipPanel.getLayout();
/* 280 */     int index = this.m_jumpList.getSelectedIndex();
/* 281 */     if ((index < 0) || (index != this.m_currentIndex))
/*     */     {
/* 283 */       saveSelection(true);
/* 284 */       this.m_currentIndex = index;
/* 285 */       this.m_currentJump = null;
/* 286 */       if (index < 0)
/*     */       {
/* 288 */         panelHandler.show(this.m_flipPanel, "Empty");
/*     */       }
/* 290 */       return;
/*     */     }
/*     */ 
/* 293 */     if (this.m_currentJump == null)
/*     */     {
/* 295 */       this.m_currentJump = new JumpClausesData();
/* 296 */       this.m_currentJump.m_isCustom = StringUtils.convertToBool(this.m_scriptData.getLocal("wfIsCustomScript"), false);
/*     */     }
/*     */ 
/* 300 */     this.m_jumpSet.setCurrentRow(index);
/* 301 */     Properties props = this.m_jumpSet.getCurrentRowProps();
/* 302 */     this.m_currentJump.parseJumpScript(props, this.m_scriptData.getLocalData());
/* 303 */     this.m_currentIndex = index;
/* 304 */     this.m_helper.m_props = props;
/*     */ 
/* 306 */     this.m_scriptHelper.setData(this.m_currentJump, null);
/*     */     try
/*     */     {
/* 309 */       this.m_scriptHelper.loadData();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 313 */       this.m_context.reportError(e);
/*     */     }
/* 315 */     panelHandler.show(this.m_flipPanel, "Script");
/*     */   }
/*     */ 
/*     */   protected boolean saveSelection(boolean isFullSave)
/*     */   {
/* 320 */     if ((this.m_currentIndex >= 0) && (this.m_currentJump != null) && (this.m_currentIndex < this.m_jumpSet.getNumRows()))
/*     */     {
/* 322 */       if (isFullSave)
/*     */       {
/* 324 */         this.m_scriptHelper.saveCurrentSelection();
/* 325 */         if (!this.m_scriptHelper.exchangeQueryInfo(false))
/*     */         {
/* 327 */           return false;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 332 */         this.m_scriptHelper.enableDisableOnSave();
/*     */       }
/*     */ 
/* 335 */       Properties props = this.m_currentJump.formatJumpScript();
/* 336 */       Parameters params = new PropParameters(props);
/*     */       try
/*     */       {
/* 339 */         Vector row = this.m_jumpSet.createRow(params);
/* 340 */         this.m_jumpSet.setRowValues(row, this.m_currentIndex);
/*     */       }
/*     */       catch (Exception exp)
/*     */       {
/* 344 */         if (isFullSave)
/*     */         {
/* 346 */           this.m_context.reportError(exp);
/*     */         }
/*     */         else
/*     */         {
/* 350 */           Report.trace(null, null, exp);
/*     */         }
/* 352 */         return false;
/*     */       }
/*     */     }
/* 355 */     return true;
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 360 */     String str = this.m_scriptData.getLocal("wfIsCustomScript");
/* 361 */     boolean isCustom = StringUtils.convertToBool(str, false);
/* 362 */     if (this.m_currentJump != null)
/*     */     {
/* 364 */       this.m_currentJump.m_isCustom = isCustom;
/*     */     }
/*     */ 
/* 367 */     boolean isEnabled = !isCustom;
/* 368 */     int start = 0;
/* 369 */     if ((this.m_currentJump == null) && (!isCustom))
/*     */     {
/* 372 */       start = 1;
/* 373 */       isEnabled = false;
/* 374 */       this.m_controlBtns[0].setEnabled(true);
/*     */     }
/*     */     else
/*     */     {
/* 378 */       this.m_scriptHelper.enableDisable(false);
/*     */     }
/* 380 */     int len = this.m_controlBtns.length;
/* 381 */     for (int i = start; i < len; ++i)
/*     */     {
/* 383 */       this.m_controlBtns[i].setEnabled(isEnabled);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 390 */     if (saveSelection(true))
/*     */     {
/* 392 */       return 1;
/*     */     }
/* 394 */     return 0;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 402 */     String cmd = e.getActionCommand();
/* 403 */     if (cmd.equals("add"))
/*     */     {
/* 406 */       String helpPage = "AddScriptJump";
/* 407 */       if (this.m_isTemplate)
/*     */       {
/* 409 */         helpPage = "AddScriptTemplateJump";
/*     */       }
/*     */ 
/* 412 */       Properties props = new Properties();
/* 413 */       AddJumpDlg dlg = new AddJumpDlg(this.m_systemInterface, LocaleResources.getString("apTitleAddJump", this.m_cxt), helpPage);
/*     */ 
/* 415 */       if (dlg.init(props, this.m_jumpSet, true) == 1)
/*     */       {
/* 417 */         saveSelection(true);
/*     */ 
/* 420 */         String name = props.getProperty("wfJumpName");
/* 421 */         Vector row = this.m_jumpSet.createEmptyRow();
/* 422 */         row.setElementAt(name, 0);
/* 423 */         this.m_jumpSet.addRow(row);
/*     */ 
/* 425 */         refreshJumpList(name);
/*     */ 
/* 428 */         DataBinder.mergeHashTables(this.m_currentJump.getJumpProperties(), props);
/*     */       }
/*     */     }
/* 431 */     else if (cmd.equals("edit"))
/*     */     {
/* 433 */       String helpPage = "EditScriptJump";
/* 434 */       if (this.m_isTemplate)
/*     */       {
/* 436 */         helpPage = "EditScriptTemplateJump";
/*     */       }
/*     */ 
/* 439 */       String name = this.m_jumpList.getSelectedObj();
/* 440 */       Properties curJumpProps = this.m_currentJump.getJumpProperties();
/* 441 */       AddJumpDlg dlg = new AddJumpDlg(this.m_systemInterface, LocaleResources.getString("apTitleEditJump", this.m_cxt, name), helpPage);
/*     */ 
/* 443 */       if (dlg.init(curJumpProps, null, false) == 1)
/*     */       {
/* 445 */         Properties props = dlg.getProperties();
/* 446 */         DataBinder.mergeHashTables(curJumpProps, props);
/*     */       }
/*     */     } else {
/* 449 */       if (!cmd.equals("delete"))
/*     */         return;
/* 451 */       String name = this.m_jumpList.getSelectedObj();
/* 452 */       if (name == null)
/*     */         return;
/* 454 */       Vector row = this.m_jumpSet.findRow(0, name);
/* 455 */       if (row == null)
/*     */         return;
/* 457 */       this.m_jumpSet.deleteCurrentRow();
/* 458 */       this.m_currentIndex = -1;
/* 459 */       refreshJumpList(null);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void refreshJumpList(String name)
/*     */   {
/* 467 */     this.m_jumpList.refreshList(this.m_jumpSet, name);
/* 468 */     checkSelection();
/*     */   }
/*     */ 
/*     */   public void focusGained(FocusEvent e)
/*     */   {
/* 477 */     enableDisable();
/*     */   }
/*     */ 
/*     */   public void focusLost(FocusEvent e)
/*     */   {
/* 484 */     saveSelection(false);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 489 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81489 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.ScriptJumpPanel
 * JD-Core Version:    0.5.4
 */