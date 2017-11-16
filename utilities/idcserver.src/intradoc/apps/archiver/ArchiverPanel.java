/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public abstract class ArchiverPanel extends PanePanel
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected ContainerHelper m_helper;
/*     */   protected CollectionContext m_collectionContext;
/*     */ 
/*     */   public ArchiverPanel()
/*     */   {
/*  38 */     this.m_systemInterface = null;
/*  39 */     this.m_cxt = null;
/*  40 */     this.m_helper = null;
/*  41 */     this.m_collectionContext = null;
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys, CollectionContext context) {
/*  45 */     this.m_systemInterface = sys;
/*  46 */     this.m_cxt = sys.getExecutionContext();
/*  47 */     this.m_helper = new ContainerHelper();
/*  48 */     this.m_helper.attachToContainer(this, sys, null);
/*  49 */     this.m_helper.m_componentBinder = this;
/*     */ 
/*  51 */     this.m_collectionContext = context;
/*     */ 
/*  53 */     JPanel pnl = initUI();
/*     */ 
/*  55 */     this.m_helper.makePanelGridBag(this, 1);
/*  56 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  57 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  58 */     this.m_helper.addLastComponentInRow(this, pnl);
/*     */ 
/*  60 */     enableDisable(false);
/*     */   }
/*     */ 
/*     */   public abstract JPanel initUI();
/*     */ 
/*     */   public void enableDisable(boolean isEnabled)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void setData(Properties props)
/*     */   {
/*  72 */     this.m_helper.m_props = props;
/*  73 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*  80 */     String name = exchange.m_compName;
/*  81 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/*  86 */     String name = exchange.m_compName;
/*  87 */     String val = exchange.m_compValue;
/*     */ 
/*  89 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*  95 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 100 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 105 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ArchiverPanel
 * JD-Core Version:    0.5.4
 */