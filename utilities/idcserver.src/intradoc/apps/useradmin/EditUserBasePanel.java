/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.PromptHandler;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.shared.gui.SecurityEditHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public abstract class EditUserBasePanel extends PanePanel
/*     */   implements ComponentBinder, PromptHandler
/*     */ {
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_ctx;
/*     */   protected ContainerHelper m_helper;
/*     */   protected ComponentValidator m_cmpValidator;
/*     */   protected SecurityEditHelper m_editHelper;
/*     */   protected UserData m_originalUserData;
/*     */   protected UserData m_loggedInUserData;
/*     */   protected boolean m_isAdmin;
/*     */   protected boolean m_isEditSelf;
/*     */   protected boolean m_isLocal;
/*     */   protected boolean m_isExternal;
/*     */ 
/*     */   public EditUserBasePanel()
/*     */   {
/*  35 */     this.m_systemInterface = null;
/*     */ 
/*  37 */     this.m_helper = null;
/*  38 */     this.m_cmpValidator = null;
/*  39 */     this.m_editHelper = null;
/*     */ 
/*  44 */     this.m_isAdmin = false;
/*  45 */     this.m_isEditSelf = false;
/*  46 */     this.m_isLocal = false;
/*  47 */     this.m_isExternal = false;
/*     */   }
/*     */ 
/*     */   public void setHelperInfo(SystemInterface sys, SecurityEditHelper editHelper, ComponentValidator cmpVal) {
/*  51 */     this.m_systemInterface = sys;
/*  52 */     this.m_ctx = sys.getExecutionContext();
/*  53 */     this.m_editHelper = editHelper;
/*     */ 
/*  55 */     this.m_cmpValidator = cmpVal;
/*     */ 
/*  57 */     this.m_loggedInUserData = AppLauncher.getUserData();
/*  58 */     this.m_isAdmin = AppLauncher.isAdmin();
/*     */   }
/*     */ 
/*     */   public void init(UserData userData, boolean isEdit)
/*     */   {
/*  63 */     this.m_originalUserData = userData;
/*  64 */     Properties props = userData.getProperties();
/*     */ 
/*  66 */     this.m_helper = new ContainerHelper();
/*  67 */     this.m_helper.attachToContainer(this, this.m_systemInterface, props);
/*  68 */     this.m_helper.m_componentBinder = this;
/*     */ 
/*  70 */     this.m_isEditSelf = false;
/*  71 */     if ((isEdit) && (this.m_loggedInUserData != null))
/*     */     {
/*  73 */       String name = this.m_originalUserData.m_name;
/*  74 */       this.m_isEditSelf = this.m_loggedInUserData.m_name.equals(name);
/*     */     }
/*     */ 
/*  78 */     String authType = this.m_originalUserData.getProperty("dUserAuthType");
/*  79 */     this.m_isExternal = authType.equalsIgnoreCase("EXTERNAL");
/*  80 */     this.m_isLocal = ((authType.equalsIgnoreCase("LOCAL")) || (authType.length() == 0));
/*     */ 
/*  84 */     ContainerHelper oldHelper = null;
/*  85 */     if (this.m_editHelper != null)
/*     */     {
/*  87 */       oldHelper = this.m_editHelper.m_helper;
/*  88 */       this.m_editHelper.m_helper = this.m_helper;
/*     */     }
/*     */ 
/*  92 */     initUI(isEdit);
/*     */ 
/*  95 */     if (this.m_editHelper != null)
/*     */     {
/*  97 */       this.m_editHelper.m_helper = oldHelper;
/*     */     }
/*     */ 
/* 100 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected abstract void initUI(boolean paramBoolean);
/*     */ 
/*     */   public void reportError(IdcMessage msg)
/*     */   {
/* 107 */     MessageBox.reportError(this.m_systemInterface, msg);
/*     */   }
/*     */ 
/*     */   public void reportError(Exception e)
/*     */   {
/* 112 */     MessageBox.reportError(this.m_systemInterface, e);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 120 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 125 */     return true;
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 133 */     if (!this.m_helper.retrieveComponentValues())
/*     */     {
/* 135 */       return 0;
/*     */     }
/* 137 */     return 1;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 142 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditUserBasePanel
 * JD-Core Version:    0.5.4
 */