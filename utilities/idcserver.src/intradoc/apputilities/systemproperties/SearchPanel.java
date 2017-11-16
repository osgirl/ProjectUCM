/*     */ package intradoc.apputilities.systemproperties;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import java.awt.BorderLayout;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SearchPanel extends SystemPropertiesPanel
/*     */ {
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/*  46 */     String[] DEBUG_OPTIONS = { "none", "verbose", "debug", "trace", "all" };
/*  47 */     String[] COL_SIZE_OPTIONS = { "1", "5", "10", "25" };
/*     */ 
/*  50 */     JPanel infoPanel = new CustomPanel();
/*  51 */     this.m_helper.makePanelGridBag(infoPanel, 1);
/*  52 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/*     */ 
/*  55 */     this.m_helper.addPanelTitle(infoPanel, LocaleResources.getString("csSearchPanelTitle", null));
/*  56 */     gridBag.m_gc.weighty = 0.0D;
/*  57 */     gridBag.prepareAddLastRowElement();
/*  58 */     this.m_helper.addComponent(infoPanel, new CustomLabel(""));
/*     */ 
/*  61 */     JPanel subPanel = addNewSubPanel(infoPanel, 2);
/*     */ 
/*  63 */     JComboBox colSizeChoice = new CustomChoice();
/*  64 */     for (int i = 0; i < COL_SIZE_OPTIONS.length; ++i)
/*     */     {
/*  66 */       colSizeChoice.addItem(COL_SIZE_OPTIONS[i]);
/*     */     }
/*  68 */     colSizeChoice.setSelectedItem("25");
/*     */ 
/*  70 */     this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csSearchPanelLabelMaxCollectionSize", null), colSizeChoice, "MaxCollectionSize");
/*     */ 
/*  73 */     JComboBox debugChoice = new CustomChoice();
/*  74 */     for (int i = 0; i < DEBUG_OPTIONS.length; ++i)
/*     */     {
/*  76 */       debugChoice.addItem(DEBUG_OPTIONS[i]);
/*     */     }
/*  78 */     debugChoice.setSelectedItem("none");
/*     */ 
/*  80 */     this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csSearchPanelLabelDebugLevel", null), debugChoice, "SearchDebugLevel");
/*     */ 
/*  83 */     setLayout(new BorderLayout());
/*  84 */     add("Center", infoPanel);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*  95 */     String name = exchange.m_compName;
/*  96 */     String val = exchange.m_compValue;
/*     */ 
/*  98 */     super.exchangeComponentValue(exchange, updateComponent);
/*     */ 
/* 100 */     if ((updateComponent) || 
/* 102 */       (val.length() != 0))
/*     */       return;
/* 104 */     this.m_helper.m_props.remove(name);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 111 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83608 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.SearchPanel
 * JD-Core Version:    0.5.4
 */