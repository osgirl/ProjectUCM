/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import java.awt.Insets;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public abstract class CWizardBaseDlg extends CWizardPanel
/*     */   implements ComponentBinder
/*     */ {
/*  40 */   protected String m_helpPage = null;
/*     */ 
/*     */   public CWizardBaseDlg()
/*     */   {
/*     */   }
/*     */ 
/*     */   public CWizardBaseDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  49 */     this.m_helper = new DialogHelper(sys, title, true);
/*  50 */     this.m_systemInterface = sys;
/*  51 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public DialogHelper getDialogHelper()
/*     */   {
/*  56 */     return (DialogHelper)this.m_helper;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  61 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/*  62 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*  63 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  64 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*     */ 
/*  66 */     initUI(mainPanel);
/*  67 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public void init(String compName)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void init(Properties props)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel mainPanel)
/*     */   {
/*     */   }
/*     */ 
/*     */   public Properties getProperties()
/*     */   {
/*  93 */     return this.m_helper.m_props;
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/*  98 */     return ((DialogHelper)this.m_helper).prompt();
/*     */   }
/*     */ 
/*     */   protected JPanel addNewSubPanel(JPanel mainPanel)
/*     */   {
/* 103 */     return addNewSubPanel(mainPanel, true);
/*     */   }
/*     */ 
/*     */   protected JPanel addNewSubPanel(JPanel mainPanel, boolean isCustomPanel)
/*     */   {
/* 109 */     JPanel panel = null;
/*     */ 
/* 111 */     if (isCustomPanel)
/*     */     {
/* 113 */       panel = new CustomPanel();
/*     */     }
/*     */     else
/*     */     {
/* 117 */       panel = new PanePanel();
/*     */     }
/* 119 */     this.m_helper.makePanelGridBag(panel, 1);
/* 120 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/*     */ 
/* 122 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 123 */     this.m_helper.addComponent(mainPanel, panel);
/*     */ 
/* 125 */     return panel;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 135 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 136 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 141 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 142 */     return helper.validateComponentValue(exchange);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 147 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.CWizardBaseDlg
 * JD-Core Version:    0.5.4
 */