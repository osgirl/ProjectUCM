/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Properties;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddTokenUserDlg
/*     */   implements ComponentBinder
/*     */ {
/*  46 */   protected SystemInterface m_systemInterface = null;
/*  47 */   protected ExecutionContext m_cxt = null;
/*  48 */   protected DialogHelper m_helper = null;
/*  49 */   protected String m_helpPage = null;
/*     */ 
/*     */   public AddTokenUserDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  53 */     this.m_systemInterface = sys;
/*  54 */     this.m_cxt = sys.getExecutionContext();
/*  55 */     this.m_helper = new DialogHelper(sys, title, true);
/*  56 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init()
/*     */   {
/*  61 */     JPanel mainPanel = this.m_helper.initStandard(this, null, 1, true, DialogHelpTable.getHelpPage(this.m_helpPage));
/*     */ 
/*  64 */     String desc = LocaleResources.getString("apAddTokenUserDesc", this.m_cxt);
/*  65 */     this.m_helper.addLastComponentInRow(mainPanel, new CustomText(desc, 60, 5, 5, 0));
/*     */ 
/*  67 */     ButtonGroup grp = new ButtonGroup();
/*  68 */     JCheckBox userBox = new CustomCheckbox(LocaleResources.getString("apLabelUser", this.m_cxt), true, grp);
/*  69 */     JCheckBox aliasBox = new CustomCheckbox(LocaleResources.getString("apLabelAlias", this.m_cxt), false, grp);
/*     */ 
/*  71 */     this.m_helper.addLabelFieldPairEx(mainPanel, LocaleResources.getString("apLabelType", this.m_cxt), userBox, "isUserType", false);
/*     */ 
/*  73 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  74 */     this.m_helper.addExchangeComponent(mainPanel, aliasBox, "isAliasType");
/*     */ 
/*  76 */     this.m_helper.addLabelEditPair(mainPanel, LocaleResources.getString("apLabelName", this.m_cxt), 30, "user");
/*     */ 
/*  79 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public Properties getProperties()
/*     */   {
/*  84 */     return this.m_helper.m_props;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*  92 */     String name = exchange.m_compName;
/*  93 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/*  98 */     String name = exchange.m_compName;
/*  99 */     String val = exchange.m_compValue;
/*     */ 
/* 101 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 107 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 112 */     IdcMessage errMsg = null;
/* 113 */     if ((name.equals("user")) && ((
/* 115 */       (val == null) || (val.length() == 0))))
/*     */     {
/* 117 */       errMsg = IdcMessageFactory.lc("apSpecifyFieldForUser", new Object[0]);
/*     */     }
/*     */ 
/* 120 */     if (errMsg != null)
/*     */     {
/* 122 */       exchange.m_errorMessage = errMsg;
/* 123 */       return false;
/*     */     }
/*     */ 
/* 126 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 131 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.AddTokenUserDlg
 * JD-Core Version:    0.5.4
 */