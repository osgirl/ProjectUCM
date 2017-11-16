/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.workflow.WfScriptStorage;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditScriptDlg
/*     */   implements ComponentBinder
/*     */ {
/*  60 */   protected SystemInterface m_systemInterface = null;
/*  61 */   protected ExecutionContext m_cxt = null;
/*  62 */   protected WorkflowContext m_context = null;
/*  63 */   protected String m_action = "ADD_WORKFLOW_SCRIPT";
/*  64 */   protected String m_helpPage = null;
/*  65 */   protected DocumentLocalizedProfile m_docProfile = null;
/*     */ 
/*  67 */   protected boolean m_isTemplate = true;
/*  68 */   protected boolean m_isNewStep = false;
/*     */ 
/*  70 */   protected WorkflowStateInfo m_workflowInfo = null;
/*  71 */   protected ScriptBuilderHelper m_scriptHelper = null;
/*  72 */   protected WfScriptStorage m_scriptData = null;
/*     */ 
/*  74 */   protected DialogHelper m_helper = null;
/*  75 */   protected TabPanel m_tabs = null;
/*     */ 
/*  78 */   protected final String[][] PANEL_INFOS = { { "ScriptJumpPanel", "intradoc.apps.workflow.ScriptJumpPanel", "apTitleJumps" }, { "ScriptCustomPanel", "intradoc.apps.workflow.ScriptCustomPanel", "apTitleCustom" }, { "ScriptTestPanel", "intradoc.apps.workflow.ScriptTestPanel", "apTitleTest" } };
/*     */ 
/*     */   public EditScriptDlg(SystemInterface sys, String title, boolean isTemplate, String helpPage)
/*     */   {
/*  87 */     this.m_systemInterface = sys;
/*  88 */     this.m_cxt = sys.getExecutionContext();
/*  89 */     this.m_helper = new DialogHelper(sys, title, true);
/*  90 */     this.m_isTemplate = isTemplate;
/*  91 */     this.m_helpPage = helpPage;
/*     */ 
/*  93 */     UserData userData = AppLauncher.getUserData();
/*  94 */     this.m_docProfile = new DocumentLocalizedProfile(userData, 1, this.m_cxt);
/*     */   }
/*     */ 
/*     */   public int init(WfScriptStorage scriptData, WorkflowStateInfo wfInfo, boolean isNewStep, WorkflowContext ctxt)
/*     */   {
/* 100 */     this.m_workflowInfo = wfInfo;
/* 101 */     this.m_isNewStep = isNewStep;
/* 102 */     this.m_context = ctxt;
/* 103 */     setScriptData(scriptData);
/*     */ 
/* 105 */     boolean isNew = scriptData == null;
/* 106 */     DialogCallback okCallback = createOkCallback();
/*     */ 
/* 108 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, DialogHelpTable.getHelpPage(this.m_helpPage));
/*     */ 
/* 110 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/* 113 */     JPanel top = new CustomPanel();
/* 114 */     gh.prepareAddLastRowElement();
/* 115 */     gh.m_gc.weighty = 1.0D;
/* 116 */     this.m_helper.addComponent(mainPanel, top);
/*     */ 
/* 118 */     gh.useGridBag(top);
/* 119 */     this.m_helper.addPanelTitle(top, LocaleResources.getString("apTitleScriptProperties", this.m_cxt));
/* 120 */     gh.m_gc.fill = 2;
/* 121 */     gh.m_gc.weightx = 1.0D;
/* 122 */     if (this.m_isTemplate)
/*     */     {
/* 124 */       if (isNew)
/*     */       {
/* 126 */         this.m_helper.addLabelFieldPair(top, LocaleResources.getString("apLabelScriptName", this.m_cxt), new CustomTextField(50), "wfScriptName");
/*     */       }
/*     */       else
/*     */       {
/* 131 */         this.m_helper.addLabelDisplayPair(top, LocaleResources.getString("apLabelScriptName", this.m_cxt), 50, "wfScriptName");
/*     */       }
/*     */ 
/* 134 */       this.m_helper.addLabelFieldPair(top, LocaleResources.getString("apLabelDescription", this.m_cxt), new CustomTextField(50), "wfScriptDescription");
/*     */     }
/*     */ 
/* 138 */     createScriptInfo(scriptData);
/* 139 */     int result = 0;
/*     */     try
/*     */     {
/* 142 */       initTabs(top);
/* 143 */       result = this.m_helper.prompt();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 147 */       this.m_context.reportError(e);
/*     */     }
/*     */ 
/* 150 */     return result;
/*     */   }
/*     */ 
/*     */   protected void initTabs(JPanel top) throws ServiceException
/*     */   {
/* 155 */     this.m_tabs = new TabPanel();
/* 156 */     for (int i = 0; i < this.PANEL_INFOS.length; ++i)
/*     */     {
/* 158 */       ScriptPanelBase editPanel = (ScriptPanelBase)ComponentClassFactory.createClassInstance(this.PANEL_INFOS[i][0], this.PANEL_INFOS[i][1], LocaleResources.getString("apUnableToLoadPanel", this.m_cxt, this.PANEL_INFOS[i][0]));
/*     */ 
/* 163 */       editPanel.setScriptInfo(this.m_scriptHelper, this.m_scriptData.getScriptData());
/* 164 */       editPanel.init(this.m_helper, this.m_workflowInfo, this.m_isTemplate, this.m_isNewStep, this.m_context);
/* 165 */       this.m_tabs.addPane(LocaleResources.getString(this.PANEL_INFOS[i][2], this.m_cxt), editPanel, editPanel, false, editPanel);
/*     */     }
/*     */ 
/* 169 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 170 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 171 */     this.m_helper.addComponent(top, this.m_tabs);
/*     */   }
/*     */ 
/*     */   protected void setScriptData(WfScriptStorage sData)
/*     */   {
/* 176 */     if (sData == null)
/*     */     {
/* 178 */       this.m_action = "ADD_WORKFLOW_SCRIPT";
/* 179 */       this.m_scriptData = new WfScriptStorage("");
/*     */     }
/*     */     else
/*     */     {
/* 183 */       this.m_action = "EDIT_WORKFLOW_SCRIPT";
/* 184 */       this.m_scriptData = sData;
/*     */     }
/* 186 */     this.m_helper.m_props = this.m_scriptData.getScriptData().getLocalData();
/*     */   }
/*     */ 
/*     */   protected void createScriptInfo(WfScriptStorage sData)
/*     */   {
/* 193 */     boolean isCriteria = true;
/* 194 */     if (this.m_workflowInfo != null)
/*     */     {
/* 196 */       String type = this.m_workflowInfo.get("dWfType");
/* 197 */       if (type == null)
/*     */       {
/* 202 */         isCriteria = false;
/*     */       }
/*     */       else
/*     */       {
/* 206 */         isCriteria = (type.equalsIgnoreCase("criteria")) || (type.equalsIgnoreCase("subworkflow"));
/*     */       }
/*     */     }
/*     */ 
/* 210 */     ActionListener targetListener = null;
/* 211 */     if ((isCriteria) && (!this.m_isTemplate))
/*     */     {
/* 213 */       targetListener = new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/* 217 */           SelectStepDlg dlg = new SelectStepDlg(EditScriptDlg.this.m_systemInterface, LocaleResources.getString("apTitleSelectTargetStep", EditScriptDlg.this.m_cxt), "SelectScriptTargetStep");
/*     */ 
/* 220 */           if (dlg.init(EditScriptDlg.this.m_workflowInfo, EditScriptDlg.this.m_context) != 1)
/*     */             return;
/* 222 */           Properties props = dlg.getProperties();
/* 223 */           String targetStep = props.getProperty("wfJumpTargetStep");
/* 224 */           EditScriptDlg.this.m_scriptHelper.updateTargetStep(targetStep);
/*     */         }
/*     */ 
/*     */       };
/*     */     }
/*     */ 
/* 230 */     this.m_scriptHelper = new ScriptBuilderHelper(targetListener, isCriteria);
/* 231 */     this.m_scriptHelper.init(this.m_systemInterface);
/* 232 */     this.m_scriptHelper.setDocumentProfile(this.m_docProfile);
/*     */   }
/*     */ 
/*     */   protected DialogCallback createOkCallback()
/*     */   {
/* 237 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 245 */           EditScriptDlg.this.m_tabs.validateAllPanes();
/*     */ 
/* 247 */           if (EditScriptDlg.this.m_isTemplate)
/*     */           {
/* 249 */             DataBinder binder = new DataBinder();
/* 250 */             binder.setLocalData(EditScriptDlg.this.m_helper.m_props);
/* 251 */             binder.merge(EditScriptDlg.this.m_scriptData.getScriptData());
/*     */ 
/* 253 */             SharedContext shContext = EditScriptDlg.this.m_context.getSharedContext();
/* 254 */             shContext.executeService(EditScriptDlg.this.m_action, binder, false);
/*     */           }
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 259 */           this.m_errorMessage = IdcMessageFactory.lc(exp);
/* 260 */           return false;
/*     */         }
/* 262 */         return true;
/*     */       }
/*     */     };
/* 266 */     return okCallback;
/*     */   }
/*     */ 
/*     */   public String getName()
/*     */   {
/* 271 */     return this.m_helper.m_props.getProperty("wfScriptName");
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 279 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 284 */     String name = exchange.m_compName;
/* 285 */     String val = exchange.m_compValue;
/*     */ 
/* 287 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 293 */     boolean isScriptName = false;
/* 294 */     int valResult = 0;
/* 295 */     if (name.equals("wfScriptName"))
/*     */     {
/* 297 */       isScriptName = true;
/* 298 */       valResult = Validation.checkUrlFileSegment(val);
/*     */     }
/* 300 */     else if (name.equals("wfScriptDescription"))
/*     */     {
/* 302 */       valResult = Validation.checkFormField(val);
/*     */     }
/*     */ 
/* 305 */     IdcMessage errMsg = null;
/* 306 */     switch (valResult)
/*     */     {
/*     */     case 0:
/* 309 */       break;
/*     */     case -1:
/* 312 */       if (isScriptName)
/*     */       {
/* 314 */         errMsg = IdcMessageFactory.lc("apSpecifyScriptName", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 318 */         errMsg = IdcMessageFactory.lc("apSpecifyDescription", new Object[0]);
/*     */       }
/* 320 */       break;
/*     */     case -2:
/* 323 */       errMsg = IdcMessageFactory.lc("apSpacesInScriptName", new Object[0]);
/* 324 */       break;
/*     */     default:
/* 327 */       if (isScriptName)
/*     */       {
/* 329 */         errMsg = IdcMessageFactory.lc("apIllegalCharsInScriptName", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 333 */         errMsg = IdcMessageFactory.lc("apIllegalCharsInDescription", new Object[0]);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 338 */     if (errMsg != null)
/*     */     {
/* 340 */       exchange.m_errorMessage = errMsg;
/* 341 */       return false;
/*     */     }
/* 343 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 348 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.EditScriptDlg
 * JD-Core Version:    0.5.4
 */