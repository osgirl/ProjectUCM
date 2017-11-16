/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextArea;
/*     */ 
/*     */ public class EditTokenDlg
/*     */   implements ComponentBinder, ActionListener
/*     */ {
/*  58 */   protected SystemInterface m_systemInterface = null;
/*  59 */   protected WorkflowContext m_context = null;
/*  60 */   protected String m_action = "ADD_WORKFLOW_TOKEN";
/*     */ 
/*  62 */   protected DialogHelper m_helper = null;
/*  63 */   protected JTextArea m_usersTxt = null;
/*     */ 
/*     */   public EditTokenDlg(SystemInterface sys, String title)
/*     */   {
/*  67 */     this.m_systemInterface = sys;
/*  68 */     this.m_helper = new DialogHelper(sys, title, true);
/*     */   }
/*     */ 
/*     */   public int init(Properties props, WorkflowContext ctxt)
/*     */   {
/*  73 */     this.m_context = ctxt;
/*  74 */     String helpPage = null;
/*  75 */     boolean isNew = props == null;
/*  76 */     if (isNew)
/*     */     {
/*  78 */       this.m_action = "ADD_WORKFLOW_TOKEN";
/*  79 */       props = new Properties();
/*  80 */       helpPage = "AddTokenScript";
/*     */     }
/*     */     else
/*     */     {
/*  84 */       this.m_action = "EDIT_WORKFLOW_TOKEN";
/*  85 */       helpPage = "EditTokenScript";
/*     */     }
/*  87 */     this.m_helper.m_props = props;
/*     */ 
/*  89 */     DialogCallback okCallback = createOkCallback();
/*     */ 
/*  91 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/*  93 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/*  96 */     JPanel pnl = new CustomPanel();
/*  97 */     this.m_helper.makePanelGridBag(pnl, 1);
/*  98 */     gh.m_gc.insets = new Insets(15, 15, 5, 15);
/*  99 */     this.m_helper.addPanelTitle(pnl, this.m_systemInterface.getString("apTokenDefintionLabel"));
/*     */ 
/* 102 */     this.m_usersTxt = new CustomTextArea(5, 30);
/* 103 */     ExecutionContext exContext = this.m_systemInterface.getExecutionContext();
/*     */ 
/* 105 */     if (isNew)
/*     */     {
/* 107 */       this.m_helper.addLabelEditPair(pnl, LocaleResources.getString("apLabelTokenName", exContext), 30, "wfTokenName");
/*     */     }
/*     */     else
/*     */     {
/* 112 */       this.m_helper.addLabelDisplayPair(pnl, LocaleResources.getString("apLabelTokenName", exContext), 30, "wfTokenName");
/*     */     }
/*     */ 
/* 116 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 15, 5, 15);
/* 117 */     this.m_helper.addLabelEditPair(pnl, LocaleResources.getString("apLabelTokenDescription", exContext), 30, "wfTokenDescription");
/*     */ 
/* 120 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 15, 5, 15);
/* 121 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 122 */     this.m_helper.addLabelFieldPairEx(pnl, LocaleResources.getString("apLabelTokenUsers", exContext), this.m_usersTxt, "wfToken", false);
/*     */ 
/* 126 */     JPanel btnPanel = new PanePanel();
/* 127 */     btnPanel.setLayout(new GridLayout(0, 1));
/* 128 */     JButton btn = new JButton(LocaleResources.getString("apAddTokenUser", exContext));
/* 129 */     btn.setActionCommand("add");
/* 130 */     btn.addActionListener(this);
/* 131 */     btnPanel.add(btn);
/*     */ 
/* 133 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.1D;
/* 134 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.1D;
/* 135 */     gh.m_gc.fill = 0;
/* 136 */     gh.m_gc.insets = new Insets(15, 5, 15, 5);
/* 137 */     this.m_helper.addComponent(pnl, btnPanel);
/*     */ 
/* 139 */     gh.m_gc.fill = 1;
/* 140 */     this.m_helper.addComponent(mainPanel, pnl);
/*     */ 
/* 142 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected DialogCallback createOkCallback()
/*     */   {
/* 147 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 154 */           DataBinder binder = new DataBinder();
/* 155 */           binder.setLocalData(EditTokenDlg.this.m_helper.m_props);
/*     */ 
/* 157 */           SharedContext shContext = EditTokenDlg.this.m_context.getSharedContext();
/* 158 */           shContext.executeService(EditTokenDlg.this.m_action, binder, false);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 162 */           this.m_errorMessage = IdcMessageFactory.lc(exp);
/* 163 */           return false;
/*     */         }
/* 165 */         return true;
/*     */       }
/*     */     };
/* 169 */     return okCallback;
/*     */   }
/*     */ 
/*     */   public String getName()
/*     */   {
/* 174 */     return this.m_helper.m_props.getProperty("wfTokenName");
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 182 */     String cmd = e.getActionCommand();
/* 183 */     if (!cmd.equals("add"))
/*     */       return;
/* 185 */     AddTokenUserDlg dlg = new AddTokenUserDlg(this.m_systemInterface, LocaleResources.getString("apTitleAddTokenUser", this.m_systemInterface.getExecutionContext()), "AddTokenUser");
/*     */ 
/* 188 */     if (dlg.init() != 1)
/*     */       return;
/* 190 */     Properties props = dlg.getProperties();
/* 191 */     String type = "user";
/* 192 */     boolean isUser = StringUtils.convertToBool(props.getProperty("isUserType"), false);
/* 193 */     if (!isUser)
/*     */     {
/* 195 */       type = "alias";
/*     */     }
/*     */ 
/* 198 */     String user = props.getProperty("user");
/*     */ 
/* 200 */     String str = "<$wfAddUser(" + user + ", \"" + type + "\")$>";
/* 201 */     String userStr = this.m_usersTxt.getText();
/* 202 */     if ((userStr != null) && (userStr.trim().length() > 0))
/*     */     {
/* 204 */       userStr = userStr + "\n" + str;
/*     */     }
/*     */     else
/*     */     {
/* 208 */       userStr = str;
/*     */     }
/*     */ 
/* 211 */     this.m_helper.m_exchange.setComponentValue("wfToken", userStr);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 221 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 226 */     String name = exchange.m_compName;
/* 227 */     String val = exchange.m_compValue;
/*     */ 
/* 229 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 235 */     IdcMessage errMsg = null;
/* 236 */     if (name.equals("wfTokenName"))
/*     */     {
/* 238 */       errMsg = Validation.checkUrlFileSegmentForDB(val, "apTokenNameErrorStub", 30, null);
/*     */     }
/*     */ 
/* 241 */     if (errMsg != null)
/*     */     {
/* 243 */       exchange.m_errorMessage = errMsg;
/* 244 */       return false;
/*     */     }
/* 246 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 251 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82384 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.EditTokenDlg
 * JD-Core Version:    0.5.4
 */