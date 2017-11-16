/*     */ package intradoc.apputilities.systemproperties;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.GuiStyles;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class InternetPanel extends SystemPropertiesPanel
/*     */ {
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/*  59 */     JPanel infoPanel = new CustomPanel();
/*  60 */     this.m_helper.makePanelGridBag(infoPanel, 1);
/*  61 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/*     */ 
/*  64 */     this.m_helper.addPanelTitle(infoPanel, LocaleResources.getString("csInternetPanelTitle", null));
/*  65 */     gridBag.m_gc.weighty = 0.0D;
/*  66 */     gridBag.prepareAddLastRowElement();
/*  67 */     this.m_helper.addComponent(infoPanel, new CustomLabel(""));
/*     */ 
/*  70 */     JPanel subPanel = addNewSubPanel(infoPanel, 2);
/*  71 */     this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csInternetPanelLabelHttpAddress", null), new CustomTextField(20), "HttpServerAddress");
/*     */ 
/*  73 */     if (!this.m_isRefinery)
/*     */     {
/*  75 */       this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csInternetPanelLabelMailAddress", null), new CustomTextField(40), "MailServer");
/*     */ 
/*  77 */       this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csInternetPanelLabelAdminAddress", null), new CustomTextField(40), "SysAdminAddress");
/*     */ 
/*  81 */       String tempPort = this.m_helper.m_props.getProperty("SmtpPort");
/*  82 */       if (tempPort == null)
/*     */       {
/*  84 */         this.m_helper.m_props.put("SmtpPort", "25");
/*     */       }
/*  86 */       this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csInternetPanelLabelSMTPPort", null), new CustomTextField(20), "SmtpPort");
/*     */     }
/*     */ 
/*  89 */     this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csInternetPanelLabelRelativeWebRoot", null), new CustomTextField(40), "HttpRelativeWebRoot");
/*     */ 
/*  92 */     setLayout(new BorderLayout());
/*  93 */     add("Center", infoPanel);
/*     */ 
/*  96 */     if ((!EnvUtils.isFamily("unix")) && (!this.m_isRefinery))
/*     */     {
/*  98 */       subPanel = addNewSubPanel(infoPanel, 2);
/*  99 */       gridBag.prepareAddLastRowElement();
/*     */ 
/* 101 */       gridBag.m_gc.fill = 2;
/* 102 */       CustomCheckbox msftCheckbox = new CustomCheckbox(LocaleResources.getString("csInternetPanelLabelEnableNtlm", null));
/* 103 */       Insets oldInsets = gridBag.m_gc.insets;
/* 104 */       gridBag.m_gc.anchor = 15;
/* 105 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 106 */       this.m_helper.addExchangeComponent(subPanel, msftCheckbox, "NtlmSecurityEnabled");
/*     */ 
/* 109 */       ButtonGroup dbGroup = new ButtonGroup();
/* 110 */       gridBag.m_gc.insets = new Insets(0, 25, 0, 0);
/*     */ 
/* 112 */       CustomCheckbox ntlmCheckbox = new CustomCheckbox(LocaleResources.getString("csInternetPanelLabelNtlm", null), dbGroup);
/* 113 */       this.m_helper.m_gridHelper.prepareAddRowElement();
/* 114 */       this.m_helper.addExchangeComponent(subPanel, ntlmCheckbox, "UseNtlm");
/*     */ 
/* 116 */       CustomCheckbox adsiCheckbox = new CustomCheckbox(LocaleResources.getString("csInternetPanelLabelAdsi", null), dbGroup);
/* 117 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 118 */       this.m_helper.addExchangeComponent(subPanel, adsiCheckbox, "UseAdsi");
/*     */ 
/* 120 */       boolean isMsftEnabled = StringUtils.convertToBool(this.m_helper.m_props.getProperty("NtlmSecurityEnabled"), false);
/* 121 */       boolean isAdsiEnabled = StringUtils.convertToBool(this.m_helper.m_props.getProperty("UseAdsi"), false);
/* 122 */       ntlmCheckbox.setEnabled(isMsftEnabled);
/* 123 */       adsiCheckbox.setEnabled(isMsftEnabled);
/* 124 */       ntlmCheckbox.setSelected(!isAdsiEnabled);
/* 125 */       ntlmCheckbox.setSelected(isAdsiEnabled);
/*     */ 
/* 129 */       ItemListener cbListener = new ItemListener(msftCheckbox, ntlmCheckbox, adsiCheckbox)
/*     */       {
/*     */         public void itemStateChanged(ItemEvent e)
/*     */         {
/* 133 */           boolean msftEnabled = this.val$msftCheckbox.isSelected();
/* 134 */           this.val$ntlmCheckbox.setEnabled(msftEnabled);
/* 135 */           this.val$adsiCheckbox.setEnabled(msftEnabled);
/*     */         }
/*     */       };
/* 139 */       msftCheckbox.addItemListener(cbListener);
/*     */ 
/* 141 */       gridBag.m_gc.insets = oldInsets;
/*     */ 
/* 143 */       setLayout(new BorderLayout());
/* 144 */       add("Center", infoPanel);
/*     */     }
/*     */ 
/* 147 */     subPanel = addNewSubPanel(infoPanel, 2);
/* 148 */     gridBag.m_gc.fill = 10;
/* 149 */     gridBag.prepareAddLastRowElement();
/*     */ 
/* 151 */     CustomText sslText = new CustomText(LocaleResources.getString("csInternetPanelSSLText", null), 60);
/* 152 */     GuiStyles.setCustomStyle(sslText, 1);
/*     */ 
/* 154 */     this.m_helper.addComponent(subPanel, sslText);
/*     */ 
/* 156 */     this.m_helper.addLabelFieldPair(subPanel, "", new CustomCheckbox(LocaleResources.getString("csInternetPanelSSLLabel", null)), "UseSSL");
/*     */ 
/* 159 */     gridBag.m_gc.weighty = 1.0D;
/* 160 */     gridBag.addEmptyRow(infoPanel);
/* 161 */     setLayout(new BorderLayout());
/* 162 */     add("Center", infoPanel);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 170 */     super.exchangeComponentValue(exchange, updateComponent);
/*     */ 
/* 173 */     if (updateComponent)
/*     */       return;
/* 175 */     String name = exchange.m_compName;
/* 176 */     String val = exchange.m_compValue;
/*     */ 
/* 178 */     if ((name.equals("HttpRelativeWebRoot")) && (val.length() == 0))
/*     */     {
/* 180 */       this.m_helper.m_props.remove(name);
/*     */     }
/* 182 */     else if ((name.equals("UseSSL")) || (name.equals("NtlmSecurityEnabled")))
/*     */     {
/* 185 */       if (val.equals("1"))
/*     */       {
/* 187 */         this.m_helper.m_props.put(name, "Yes");
/*     */       }
/*     */       else
/*     */       {
/* 191 */         this.m_helper.m_props.put(name, "No");
/*     */       }
/*     */     } else {
/* 194 */       if ((!name.equals("UseAdsi")) && (!name.equals("UseNtlm"))) {
/*     */         return;
/*     */       }
/* 197 */       if (val.equals("1"))
/*     */       {
/* 199 */         this.m_helper.m_props.put(name, "Yes");
/*     */       }
/*     */       else
/*     */       {
/* 203 */         this.m_helper.m_props.remove(name);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 213 */     String name = exchange.m_compName;
/* 214 */     String val = exchange.m_compValue;
/*     */ 
/* 216 */     if ((name.equals("SmtpPort")) && (val.length() != 0) && 
/* 218 */       (Validation.checkInteger(val) != 0))
/*     */     {
/* 220 */       exchange.m_errorMessage = IdcMessageFactory.lc("csInternetPanelSMTPPortError", new Object[0]);
/* 221 */       return false;
/*     */     }
/*     */ 
/* 225 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 230 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.InternetPanel
 * JD-Core Version:    0.5.4
 */