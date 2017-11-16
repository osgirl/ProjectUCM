/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.PromptHandler;
/*     */ 
/*     */ public abstract class JumpBasePanel extends PanePanel
/*     */   implements ComponentBinder, PromptHandler
/*     */ {
/*  34 */   protected SystemInterface m_systemInterface = null;
/*  35 */   protected ExecutionContext m_cxt = null;
/*  36 */   protected ContainerHelper m_helper = null;
/*     */ 
/*     */   public void init(DialogHelper helper)
/*     */     throws ServiceException
/*     */   {
/*  45 */     this.m_systemInterface = helper.m_exchange.m_sysInterface;
/*  46 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*     */ 
/*  48 */     this.m_helper = new ContainerHelper();
/*  49 */     this.m_helper.attachToContainer(this, this.m_systemInterface, helper.m_props);
/*  50 */     this.m_helper.m_componentBinder = this;
/*  51 */     this.m_helper.makePanelGridBag(this, 1);
/*     */ 
/*  53 */     initUI();
/*     */ 
/*  55 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected abstract void initUI();
/*     */ 
/*     */   public int prompt()
/*     */   {
/*  65 */     if (!this.m_helper.retrieveComponentValues())
/*     */     {
/*  67 */       return 0;
/*     */     }
/*  69 */     return 1;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*  77 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/*  82 */     String name = exchange.m_compName;
/*  83 */     String val = exchange.m_compValue;
/*     */ 
/*  85 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/*  96 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 101 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.JumpBasePanel
 * JD-Core Version:    0.5.4
 */