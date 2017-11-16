/*     */ package intradoc.apputilities.systemproperties;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class PathsPanel extends SystemPropertiesPanel
/*     */ {
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/*  46 */     JPanel infoPanel = new CustomPanel();
/*  47 */     this.m_helper.makePanelGridBag(infoPanel, 1);
/*  48 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/*     */ 
/*  51 */     this.m_helper.addPanelTitle(infoPanel, LocaleResources.getString("csPathsPanelTitle", null));
/*  52 */     gridBag.m_gc.weighty = 0.0D;
/*  53 */     gridBag.prepareAddLastRowElement();
/*  54 */     this.m_helper.addComponent(infoPanel, new CustomLabel(""));
/*     */ 
/*  56 */     JPanel subPanel = addNewSubPanel(infoPanel, 1);
/*     */ 
/*  58 */     gridBag.prepareAddLastRowElement(18);
/*  59 */     this.m_helper.addComponent(subPanel, new CustomLabel(LocaleResources.getString("csPathsPanelLabelBrowserPath", null), 1));
/*     */ 
/*  62 */     gridBag.m_gc.weighty = 1.0D;
/*  63 */     gridBag.addEmptyRow(infoPanel);
/*     */ 
/*  66 */     gridBag.prepareAddLastRowElement(18);
/*  67 */     this.m_helper.addFilePathComponent(subPanel, 50, LocaleResources.getString("csPathsPanelBrowserExecFile", null), "WebBrowserPath");
/*     */ 
/*  70 */     this.m_helper.addLastComponentInRow(subPanel, new CustomLabel(""));
/*  71 */     this.m_helper.addLastComponentInRow(subPanel, new CustomLabel(LocaleResources.getString("csPathsPanelLabelJavaClasspath", null), 1));
/*     */ 
/*  74 */     gridBag.prepareAddLastRowElement(18);
/*  75 */     this.m_helper.addExchangeComponent(subPanel, new CustomTextField(50), "BASE_JAVA_CLASSPATH_custom");
/*     */ 
/*  77 */     this.m_helper.addLastComponentInRow(subPanel, new CustomLabel(""));
/*  78 */     this.m_helper.addLastComponentInRow(subPanel, new CustomLabel(LocaleResources.getString("csPathsPanelIdcHomeDir", null), 1));
/*     */ 
/*  81 */     gridBag.prepareAddLastRowElement(18);
/*  82 */     this.m_helper.addExchangeComponent(subPanel, new CustomTextField(50), "IdcHomeDir");
/*     */ 
/*  84 */     setLayout(new BorderLayout());
/*  85 */     add("Center", infoPanel);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*  96 */     if ((exchange.m_compName.equals("WebBrowserPath")) && (exchange.m_compValue != null) && (exchange.m_compValue.length() > 0))
/*     */     {
/* 100 */       exchange.m_compValue = FileUtils.fileSlashes(exchange.m_compValue);
/*     */     }
/*     */ 
/* 103 */     super.exchangeComponentValue(exchange, updateComponent);
/*     */ 
/* 105 */     if (updateComponent)
/*     */       return;
/* 107 */     String name = exchange.m_compName;
/* 108 */     String val = exchange.m_compValue;
/*     */ 
/* 110 */     if (val.length() != 0)
/*     */       return;
/* 112 */     this.m_helper.m_props.remove(name);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 120 */     String name = exchange.m_compName;
/* 121 */     String val = exchange.m_compValue;
/*     */ 
/* 123 */     if ((name.equals("WebBrowserPath")) && (val.length() != 0))
/*     */     {
/* 125 */       IdcMessage errMsg = validatePath(val, 1);
/*     */ 
/* 127 */       if (errMsg != null)
/*     */       {
/* 129 */         exchange.m_errorMessage = errMsg;
/* 130 */         return false;
/*     */       }
/*     */     }
/* 133 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 138 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80941 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.PathsPanel
 * JD-Core Version:    0.5.4
 */