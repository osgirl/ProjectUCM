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
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.CheckboxPanel;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditWorkflowDlg
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*  70 */   protected ExecutionContext m_cxt = null;
/*  71 */   protected WorkflowContext m_context = null;
/*     */   protected String m_helpPage;
/*     */   protected ComponentValidator m_cmpValidator;
/*  74 */   protected boolean m_isNew = true;
/*     */   protected String m_action;
/*     */   protected DataBinder m_binder;
/*  80 */   protected JComboBox m_groupChoice = null;
/*  81 */   protected JCheckBox m_isTemplateBox = null;
/*  82 */   protected JComboBox m_templateChoice = null;
/*  83 */   protected boolean m_hasTemplates = false;
/*     */ 
/*  85 */   protected String[] TYPES = { "Basic" };
/*     */ 
/*  91 */   protected final Object[][] m_autoContributeStepRevisionOptions = { { ":C:CA:CE:", "!apWfStepType_CE", "", "radioeditgroup" }, { ":C:CA:CN:", "!apWfStepType_CN", "", "radioeditgroup" } };
/*     */ 
/*     */   public EditWorkflowDlg(SystemInterface sys, String title, WorkflowContext ctxt, ResultSet rset, String helpPage)
/*     */   {
/* 100 */     this.m_helper = new DialogHelper(sys, title, true);
/* 101 */     this.m_systemInterface = sys;
/* 102 */     this.m_cxt = sys.getExecutionContext();
/* 103 */     this.m_helpPage = helpPage;
/* 104 */     this.m_context = ctxt;
/* 105 */     this.m_cmpValidator = new ComponentValidator(rset);
/*     */   }
/*     */ 
/*     */   public boolean init(Properties data)
/*     */   {
/* 110 */     String name = null;
/* 111 */     if (data != null)
/*     */     {
/* 113 */       this.m_action = "EDIT_WORKFLOW";
/* 114 */       this.m_helper.m_props = data;
/* 115 */       this.m_isNew = false;
/* 116 */       name = data.getProperty("dWfName");
/*     */     }
/*     */     else
/*     */     {
/* 120 */       this.m_action = "ADD_WORKFLOW";
/*     */     }
/*     */ 
/* 123 */     if (!createGroupChoice(this.m_isNew))
/*     */     {
/* 126 */       return false;
/*     */     }
/*     */ 
/* 129 */     JPanel mainPanel = initUI(name, this.m_isNew);
/*     */ 
/* 131 */     if (this.m_isNew)
/*     */     {
/* 134 */       initTemplateChoice(mainPanel);
/*     */ 
/* 137 */       this.m_helper.m_props.put("dWfType", this.TYPES[0]);
/*     */     }
/*     */ 
/* 140 */     return true;
/*     */   }
/*     */ 
/*     */   protected JPanel initUI(String name, boolean isNew)
/*     */   {
/* 145 */     Component nameCmp = null;
/* 146 */     if (isNew)
/*     */     {
/* 148 */       nameCmp = new CustomTextField(20);
/*     */     }
/*     */     else
/*     */     {
/* 152 */       nameCmp = new CustomLabel(name);
/*     */     }
/*     */ 
/* 155 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 162 */           Properties localData = this.m_dlgHelper.m_props;
/* 163 */           EditWorkflowDlg.this.m_binder = new DataBinder(true);
/* 164 */           EditWorkflowDlg.this.m_binder.setLocalData(localData);
/*     */ 
/* 166 */           SharedContext shContext = EditWorkflowDlg.this.m_context.getSharedContext();
/* 167 */           AppContextUtils.executeService(shContext, EditWorkflowDlg.this.m_action, EditWorkflowDlg.this.m_binder, true);
/* 168 */           return true;
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 172 */           MessageBox.reportError(EditWorkflowDlg.this.m_systemInterface, exp);
/*     */         }
/* 174 */         return false;
/*     */       }
/*     */     };
/* 177 */     okCallback.m_dlgHelper = this.m_helper;
/*     */ 
/* 179 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 2, true, this.m_helpPage);
/*     */ 
/* 181 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 20, 10, 20);
/*     */ 
/* 183 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 185 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 0, 0, 0);
/*     */ 
/* 187 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelWorkflowName", this.m_cxt), nameCmp, "dWfName");
/*     */ 
/* 189 */     this.m_helper.addLabelEditPair(mainPanel, LocaleResources.getString("apLabelDescription", this.m_cxt), 40, "dWfDescription");
/*     */ 
/* 192 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 0, 0, 0);
/* 193 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelSecurityGroup", this.m_cxt), this.m_groupChoice, "dSecurityGroup");
/*     */ 
/* 196 */     CheckboxPanel autoContributeStepOptions = new CheckboxPanel();
/* 197 */     autoContributeStepOptions.init(this.m_autoContributeStepRevisionOptions, true, this.m_systemInterface);
/* 198 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelAutoContributeEditRule", this.m_cxt), autoContributeStepOptions, "dWfAutoContributeStepType");
/*     */ 
/* 201 */     return mainPanel;
/*     */   }
/*     */ 
/*     */   protected void initTemplateChoice(JPanel mainPanel)
/*     */   {
/* 206 */     this.m_isTemplateBox = new CustomCheckbox(LocaleResources.getString("apTitleUseTemplate", this.m_cxt));
/*     */ 
/* 208 */     this.m_templateChoice = new CustomChoice();
/* 209 */     initTemplateChoice();
/*     */ 
/* 211 */     if (!this.m_hasTemplates) {
/*     */       return;
/*     */     }
/* 214 */     this.m_helper.m_gridHelper.prepareAddRowElement(13);
/* 215 */     this.m_helper.addExchangeComponent(mainPanel, this.m_isTemplateBox, "HasTemplate");
/* 216 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 217 */     this.m_helper.addExchangeComponent(mainPanel, this.m_templateChoice, "dWfTemplateName");
/*     */ 
/* 219 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 223 */         int state = e.getStateChange();
/* 224 */         boolean isSelected = (state == 1) && (EditWorkflowDlg.this.m_hasTemplates);
/* 225 */         EditWorkflowDlg.this.m_templateChoice.setEnabled(isSelected);
/*     */       }
/*     */     };
/* 228 */     this.m_isTemplateBox.addItemListener(iListener);
/*     */   }
/*     */ 
/*     */   protected void initTemplateChoice()
/*     */   {
/* 234 */     DataResultSet rset = SharedObjects.getTable("WfTemplates");
/* 235 */     if ((rset == null) || (!rset.isRowPresent()))
/*     */     {
/* 237 */       this.m_templateChoice.addItem("  ");
/* 238 */       return;
/*     */     }
/*     */ 
/* 241 */     this.m_hasTemplates = true;
/* 242 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 244 */       String tmpName = rset.getStringValue(0);
/* 245 */       this.m_templateChoice.addItem(tmpName);
/*     */     }
/*     */ 
/* 248 */     this.m_templateChoice.setEnabled(false);
/*     */   }
/*     */ 
/*     */   public boolean createGroupChoice(boolean isAdd)
/*     */   {
/* 253 */     this.m_groupChoice = new CustomChoice();
/*     */ 
/* 255 */     Vector groups = this.m_context.getUsersGroups();
/* 256 */     if ((groups == null) || (groups.size() == 0))
/*     */     {
/* 258 */       String errKey = null;
/* 259 */       if (isAdd)
/*     */       {
/* 261 */         errKey = "apInsufficientRightsToAddWorkflow";
/*     */       }
/*     */       else
/*     */       {
/* 265 */         errKey = "apInsufficientRightsToEditWorkflow";
/*     */       }
/* 267 */       AppLauncher.reportOperationError(this.m_systemInterface, null, IdcMessageFactory.lc(errKey, new Object[0]));
/* 268 */       return false;
/*     */     }
/*     */ 
/* 271 */     int size = groups.size();
/* 272 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 274 */       this.m_groupChoice.addItem(groups.elementAt(i));
/*     */     }
/* 276 */     return true;
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 281 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public String getWorkflowName()
/*     */   {
/* 286 */     return this.m_helper.m_props.getProperty("dWfName");
/*     */   }
/*     */ 
/*     */   public DataBinder getBinder()
/*     */   {
/* 291 */     return this.m_binder;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 300 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 301 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 306 */     String name = exchange.m_compName;
/* 307 */     String val = exchange.m_compValue;
/*     */ 
/* 309 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/*     */ 
/* 311 */     IdcMessage errMsg = null;
/* 312 */     if ((this.m_isNew) && (name.equals("dWfName")))
/*     */     {
/* 315 */       errMsg = Validation.checkUrlFileSegmentForDB(val, "apWorkflowNameErrorStub", maxLength, null);
/*     */     }
/* 318 */     else if ((name.equals("dWfDescription")) && 
/* 320 */       (val != null) && (val.length() > maxLength))
/*     */     {
/* 322 */       errMsg = IdcMessageFactory.lc("apWorkflowDescriptionExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */     }
/*     */ 
/* 326 */     if (errMsg != null)
/*     */     {
/* 328 */       exchange.m_errorMessage = errMsg;
/* 329 */       return false;
/*     */     }
/* 331 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 336 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.EditWorkflowDlg
 * JD-Core Version:    0.5.4
 */