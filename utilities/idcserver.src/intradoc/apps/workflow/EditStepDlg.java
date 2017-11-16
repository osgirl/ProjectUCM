/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.CheckboxPanel;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditStepDlg
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*  61 */   protected ExecutionContext m_cxt = null;
/*     */   protected ComponentValidator m_cmpValidator;
/*  63 */   protected boolean m_isNew = true;
/*     */   protected WorkflowContext m_context;
/*     */   protected WorkflowStateInfo m_workflowInfo;
/*     */   protected WorkflowStateInfo m_wfStepInfo;
/*  69 */   protected boolean m_isCriteria = false;
/*     */   protected String m_helpPage;
/*  73 */   protected final Object[][] STEP_TYPE_OPTIONS = { { ":R:", "!apWfUserReviewRevision", "", "radioeditgroup" }, { ":R:C:CE:", "!apWfUserEditRevision", "", "radioeditgroup" }, { ":R:C:CN:", "!apWfUserNewRevision", "", "radioeditgroup" } };
/*     */ 
/*  80 */   protected WfStepEditView m_stepView = null;
/*     */ 
/*  83 */   protected final String[][] PANEL_INFOS = { { "WfStepEditView", "intradoc.apps.workflow.WfStepEditView", "apTitleUsers" }, { "WfStepOptionsView", "intradoc.apps.workflow.WfStepConditionsPanel", "apTitleExitConditions" }, { "WfStepScriptPanel", "intradoc.apps.workflow.WfStepScriptPanel", "apTitleEvents" } };
/*     */ 
/*     */   public EditStepDlg(SystemInterface sys, String title, ResultSet rset, boolean isCriteria, String helpPage)
/*     */   {
/*  93 */     this.m_helper = new DialogHelper(sys, title, true);
/*  94 */     this.m_systemInterface = sys;
/*  95 */     this.m_cxt = sys.getExecutionContext();
/*  96 */     this.m_helpPage = helpPage;
/*  97 */     this.m_cmpValidator = new ComponentValidator(rset);
/*  98 */     this.m_isCriteria = isCriteria;
/*     */   }
/*     */ 
/*     */   public void init(Properties props, WorkflowContext cxt, WorkflowStateInfo wfInfo, WorkflowStateInfo stepInfo)
/*     */   {
/* 104 */     initEx(props, false, null, cxt, wfInfo, stepInfo);
/*     */   }
/*     */ 
/*     */   public void initEx(Properties props, boolean hasStepView, DialogCallback okCallback, WorkflowContext cxt, WorkflowStateInfo wfInfo, WorkflowStateInfo stepInfo)
/*     */   {
/* 110 */     this.m_context = cxt;
/* 111 */     this.m_workflowInfo = wfInfo;
/* 112 */     this.m_wfStepInfo = stepInfo;
/*     */ 
/* 114 */     String name = null;
/* 115 */     if (props != null)
/*     */     {
/* 117 */       this.m_helper.m_props = props;
/* 118 */       name = props.getProperty("dWfStepName");
/*     */     }
/* 120 */     this.m_isNew = (name == null);
/*     */ 
/* 122 */     JPanel wrapper = this.m_helper.initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 125 */     JPanel mainPanel = new PanePanel();
/*     */ 
/* 127 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 20, 10, 20);
/* 128 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 129 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 130 */     this.m_helper.addComponent(wrapper, mainPanel);
/*     */ 
/* 132 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/* 133 */     initUI(mainPanel, name, hasStepView);
/*     */   }
/*     */ 
/*     */   protected void initUI(JPanel pnl, String name, boolean hasStepView)
/*     */   {
/* 138 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 0, 0, 0);
/* 139 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/*     */ 
/* 141 */     boolean isNew = name == null;
/* 142 */     Component nameCmp = null;
/* 143 */     if (isNew)
/*     */     {
/* 145 */       nameCmp = new JTextField(20);
/*     */     }
/*     */     else
/*     */     {
/* 149 */       nameCmp = new CustomLabel(name);
/*     */     }
/*     */ 
/* 152 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelName", this.m_cxt), nameCmp, "dWfStepName");
/*     */ 
/* 155 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelDescription", this.m_cxt), new JTextField(20), "dWfStepDescription");
/*     */ 
/* 158 */     boolean isElectronicSignaturesInstalled = SharedObjects.getEnvValueAsBoolean("IsElectronicSignaturesInstalled", false);
/*     */ 
/* 160 */     if (isElectronicSignaturesInstalled)
/*     */     {
/* 162 */       CustomCheckbox isSignatureBox = new CustomCheckbox(LocaleResources.getString("apRequiresSignatureOnApproval", this.m_cxt), 1);
/*     */ 
/* 164 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 165 */       this.m_helper.addExchangeComponent(pnl, isSignatureBox, "dWfStepIsSignature");
/*     */     }
/*     */ 
/* 168 */     initStepChoice(pnl);
/*     */ 
/* 170 */     if (!hasStepView)
/*     */       return;
/* 172 */     JPanel stepPanel = createStepView(this.m_helper.m_props, isNew);
/* 173 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 174 */     this.m_helper.addComponent(pnl, stepPanel);
/*     */   }
/*     */ 
/*     */   protected void initStepChoice(JPanel pnl)
/*     */   {
/* 180 */     Object[][] stepRevOptions = (Object[][])null;
/* 181 */     if (this.m_workflowInfo != null)
/*     */     {
/* 183 */       String prj = this.m_workflowInfo.get("dProjectID");
/* 184 */       if (prj.length() > 0)
/*     */       {
/* 186 */         stepRevOptions = new Object[1][];
/* 187 */         stepRevOptions[0] = this.STEP_TYPE_OPTIONS[0];
/*     */       }
/*     */     }
/*     */ 
/* 191 */     if (stepRevOptions == null)
/*     */     {
/* 193 */       stepRevOptions = this.STEP_TYPE_OPTIONS;
/*     */     }
/*     */ 
/* 196 */     CheckboxPanel stepOptions = new CheckboxPanel();
/* 197 */     stepOptions.init(stepRevOptions, false, this.m_systemInterface);
/*     */ 
/* 199 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 200 */     this.m_helper.addExchangeComponent(pnl, stepOptions, "dWfStepType");
/*     */   }
/*     */ 
/*     */   protected JPanel createStepView(Properties props, boolean isNew)
/*     */   {
/* 206 */     TabPanel tabPanel = new TabPanel();
/* 207 */     JPanel panel = tabPanel;
/*     */ 
/* 209 */     int numPanels = this.PANEL_INFOS.length;
/* 210 */     EditViewBase[] panels = new EditViewBase[numPanels];
/* 211 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/*     */       try
/*     */       {
/* 215 */         panels[i] = ((EditViewBase)ComponentClassFactory.createClassInstance(this.PANEL_INFOS[i][0], this.PANEL_INFOS[i][1], LocaleResources.getString("apUnableToLoadTabPanel", this.m_cxt, this.PANEL_INFOS[i][0])));
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 221 */         this.m_context.reportError(e);
/*     */       }
/*     */ 
/* 224 */       panels[i].init(this.m_helper, this.m_context, true, isNew);
/* 225 */       panels[i].setWorkflowInfo(this.m_wfStepInfo);
/* 226 */       panels[i].load();
/*     */ 
/* 228 */       if (i == 0)
/*     */       {
/* 230 */         this.m_stepView = ((WfStepEditView)panels[i]);
/*     */       }
/*     */ 
/* 233 */       tabPanel.addPane(LocaleResources.getString(this.PANEL_INFOS[i][2], this.m_cxt), panels[i]);
/*     */     }
/*     */ 
/* 236 */     return panel;
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 241 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public Properties getData()
/*     */   {
/* 246 */     return this.m_helper.m_props;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 255 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 256 */     helper.exchangeComponentValue(exchange, updateComponent);
/* 257 */     if ((!updateComponent) || (!exchange.m_compName.equals("dWfStepType")))
/*     */       return;
/* 259 */     exchange.m_compValue = WorkflowScriptUtils.getUpgradedStepType(exchange.m_compValue);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 265 */     String name = exchange.m_compName;
/* 266 */     String val = exchange.m_compValue;
/*     */ 
/* 268 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 274 */     IdcMessage errMsg = null;
/*     */ 
/* 276 */     int maxLength = 30;
/* 277 */     if ((name.equals("dWfStepName")) && (this.m_isNew))
/*     */     {
/* 280 */       maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/* 281 */       errMsg = Validation.checkUrlFileSegmentForDB(val, "apStepNameErrorStub", maxLength, null);
/* 282 */       if ((errMsg == null) && (((val.equalsIgnoreCase("contribution")) || (val.equalsIgnoreCase("inactive")))))
/*     */       {
/* 285 */         errMsg = IdcMessageFactory.lc("apStepNameIsReserved", new Object[] { val });
/*     */       }
/*     */     }
/* 288 */     else if (name.equals("dWfStepDescription"))
/*     */     {
/* 290 */       maxLength = this.m_cmpValidator.getMaxLength(name, 80);
/* 291 */       if ((val != null) && (val.length() > maxLength))
/*     */       {
/* 293 */         errMsg = IdcMessageFactory.lc("apStepDescriptionExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */       }
/*     */     }
/* 296 */     else if (name.equals("dWfStepType"))
/*     */     {
/* 299 */       if (this.m_stepView != null)
/*     */       {
/* 301 */         this.m_helper.m_props.put("dAliases", this.m_stepView.getAliases());
/*     */       }
/*     */ 
/* 304 */       while ((i = val.indexOf("::")) >= 0)
/*     */       {
/*     */         int i;
/* 306 */         val = val.substring(0, i) + val.substring(i + 1);
/*     */       }
/* 308 */       exchange.setComponentValue(exchange.m_compName, val);
/*     */     }
/* 310 */     else if ((name.equals("dWfStepWeight")) && ((
/* 312 */       (val == null) || (val.trim().length() == 0))))
/*     */     {
/* 314 */       exchange.setComponentValue(exchange.m_compName, "0");
/*     */     }
/*     */ 
/* 318 */     if (errMsg != null)
/*     */     {
/* 320 */       exchange.m_errorMessage = errMsg;
/* 321 */       return false;
/*     */     }
/* 323 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 328 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93373 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.EditStepDlg
 * JD-Core Version:    0.5.4
 */