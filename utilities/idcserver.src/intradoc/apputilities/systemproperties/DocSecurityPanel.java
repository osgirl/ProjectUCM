/*     */ package intradoc.apputilities.systemproperties;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import java.awt.BorderLayout;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DocSecurityPanel extends SystemPropertiesPanel
/*     */ {
/*     */   protected Hashtable m_fields;
/*     */ 
/*     */   public DocSecurityPanel()
/*     */   {
/*  42 */     this.m_fields = new Hashtable();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/*  48 */     JPanel infoPanel = new CustomPanel();
/*  49 */     this.m_helper.makePanelGridBag(infoPanel, 1);
/*  50 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/*     */ 
/*  53 */     this.m_helper.addPanelTitle(infoPanel, LocaleResources.getString("csDocSecurityPanelTitle", null));
/*  54 */     gridBag.m_gc.weighty = 0.0D;
/*  55 */     gridBag.prepareAddLastRowElement();
/*  56 */     this.m_helper.addComponent(infoPanel, new CustomLabel(""));
/*     */ 
/*  59 */     JPanel tPanel = addNewSubPanel(infoPanel, 1);
/*     */ 
/*  61 */     addCheckbox(tPanel, "csDocSecurityPanelCheckboxAllowGetCopy", "GetCopyAccess");
/*  62 */     addCheckbox(tPanel, "csDocSecurityPanelCheckboxAllowContributor", "ExclusiveCheckout");
/*  63 */     addCheckbox(tPanel, "csDocSecurityPanelCheckboxAllowAuthorDelete", "AuthorDelete");
/*  64 */     addCheckbox(tPanel, "csDocSecurityPanelCheckboxShowKnownAccounts", "ShowOnlyKnownAccounts");
/*     */ 
/*  66 */     setLayout(new BorderLayout());
/*  67 */     add("Center", infoPanel);
/*     */ 
/*  69 */     gridBag.m_gc.weighty = 1.0D;
/*  70 */     gridBag.addEmptyRow(infoPanel);
/*     */   }
/*     */ 
/*     */   protected void addCheckbox(JPanel tPanel, String keyBase, String setting)
/*     */   {
/*  75 */     JCheckBox checkbox = new CustomCheckbox(LocaleResources.getString(keyBase, null));
/*     */ 
/*  77 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  78 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/*  79 */     this.m_helper.m_gridHelper.m_gc.anchor = 17;
/*  80 */     this.m_helper.addExchangeComponent(tPanel, checkbox, setting);
/*  81 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*  82 */     checkboxDescript(tPanel, "!" + keyBase + "Desc");
/*  83 */     this.m_fields.put(setting, setting);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*  94 */     super.exchangeComponentValue(exchange, updateComponent);
/*     */ 
/*  97 */     if (updateComponent)
/*     */       return;
/*  99 */     String name = exchange.m_compName;
/* 100 */     String val = exchange.m_compValue;
/*     */ 
/* 102 */     if (this.m_fields.get(name) != null)
/*     */     {
/* 104 */       String str = this.m_helper.m_props.getProperty(name);
/* 105 */       boolean flag = StringUtils.convertToBool(str, false);
/* 106 */       if (flag)
/*     */       {
/* 108 */         this.m_helper.m_props.put(name, "true");
/*     */       }
/*     */       else
/*     */       {
/* 112 */         this.m_helper.m_props.remove(name);
/*     */       }
/*     */     } else {
/* 115 */       if (val.length() != 0)
/*     */         return;
/* 117 */       this.m_helper.m_props.remove(name);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 125 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 130 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.DocSecurityPanel
 * JD-Core Version:    0.5.4
 */