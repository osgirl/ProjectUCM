/*     */ package intradoc.apputilities.systemproperties;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.RevisionSpec;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class OptionsPanel extends SystemPropertiesPanel
/*     */ {
/*     */   protected JCheckBox m_autoNumCheckbox;
/*     */   protected JTextField m_autoNumField;
/*     */ 
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/*  61 */     JPanel infoPanel = new CustomPanel();
/*  62 */     this.m_helper.makePanelGridBag(infoPanel, 1);
/*  63 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/*     */ 
/*  66 */     this.m_helper.addPanelTitle(infoPanel, LocaleResources.getString("csOptionsPanelTitle", null));
/*  67 */     gridBag.m_gc.weighty = 0.0D;
/*  68 */     gridBag.prepareAddLastRowElement();
/*  69 */     this.m_helper.addComponent(infoPanel, new CustomLabel(""));
/*     */ 
/*  72 */     JPanel tPanel = addNewSubPanel(infoPanel, 1);
/*     */ 
/*  74 */     gridBag.m_gc.fill = 0;
/*  75 */     JCheckBox ofCheckbox = new CustomCheckbox(LocaleResources.getString("csOptionsPanelCheckboxAllowOverride", null));
/*     */ 
/*  77 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  78 */     gridBag.m_gc.anchor = 17;
/*  79 */     this.m_helper.addExchangeComponent(tPanel, ofCheckbox, "IsOverrideFormat");
/*     */ 
/*  81 */     JCheckBox daCheckbox = new CustomCheckbox(LocaleResources.getString("csOptionsPanelCheckboxAllowDownloadApplet", null));
/*     */ 
/*  83 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  84 */     gridBag.m_gc.anchor = 17;
/*  85 */     this.m_helper.addExchangeComponent(tPanel, daCheckbox, "DownloadApplet");
/*     */ 
/*  87 */     JCheckBox uaCheckbox = new CustomCheckbox(LocaleResources.getString("csOptionsPanelCheckboxAllowUploadApplet", null));
/*     */ 
/*  89 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  90 */     gridBag.m_gc.anchor = 17;
/*  91 */     this.m_helper.addExchangeComponent(tPanel, uaCheckbox, "MultiUpload");
/*     */ 
/*  93 */     boolean allowProxyingServers = StringUtils.convertToBool(this.m_helper.m_props.getProperty("AllowProxyingServers"), false);
/*     */ 
/*  95 */     if (allowProxyingServers)
/*     */     {
/*  97 */       JCheckBox esCheckbox = new CustomCheckbox(LocaleResources.getString("csOptionsPanelCheckboxEnableEntSearchOnStadardPages", null));
/*     */ 
/*  99 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 100 */       gridBag.m_gc.anchor = 17;
/* 101 */       this.m_helper.addExchangeComponent(tPanel, esCheckbox, "EnterpriseSearchAsDefault");
/*     */     }
/*     */ 
/* 104 */     gridBag.m_gc.fill = 1;
/*     */ 
/* 106 */     setLayout(new BorderLayout());
/* 107 */     add("Center", infoPanel);
/*     */ 
/* 109 */     JPanel mPanel = addNewSubPanel(infoPanel, 1);
/* 110 */     gridBag.m_gc.fill = 0;
/* 111 */     this.m_autoNumCheckbox = new CustomCheckbox(LocaleResources.getString("csOptionsPanelCheckboxAutoContentID", null));
/*     */ 
/* 113 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/* 114 */     gridBag.m_gc.anchor = 17;
/* 115 */     this.m_helper.addExchangeComponent(mPanel, this.m_autoNumCheckbox, "IsAutoNumber");
/* 116 */     gridBag.m_gc.fill = 1;
/*     */ 
/* 118 */     this.m_autoNumField = new CustomTextField(40);
/* 119 */     this.m_helper.addLabelFieldPair(mPanel, LocaleResources.getString("csOptionsPanelLabelAutoNamePrefix", null), this.m_autoNumField, "AutoNumberPrefix");
/*     */ 
/* 122 */     boolean isAutoNumberEnabled = StringUtils.convertToBool(this.m_helper.m_props.getProperty("IsAutoNumber"), false);
/*     */ 
/* 125 */     if (!isAutoNumberEnabled)
/*     */     {
/* 127 */       this.m_autoNumField.setEnabled(false);
/*     */     }
/*     */ 
/* 131 */     ItemListener autoNumListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 135 */         boolean autoNumEnabled = OptionsPanel.this.m_autoNumCheckbox.isSelected();
/*     */ 
/* 137 */         if (autoNumEnabled)
/*     */         {
/* 139 */           OptionsPanel.this.m_autoNumField.setEnabled(true);
/*     */         }
/*     */         else
/* 142 */           OptionsPanel.this.m_autoNumField.setEnabled(false);
/*     */       }
/*     */     };
/* 147 */     this.m_autoNumCheckbox.addItemListener(autoNumListener);
/*     */ 
/* 149 */     JPanel bPanel = addNewSubPanel(infoPanel, 1);
/* 150 */     this.m_helper.addLabelFieldPair(bPanel, LocaleResources.getString("csOptionsPanelLabelMajorRevLabelSequence", null), new CustomTextField(40), "MajorRevSeq");
/*     */ 
/* 153 */     this.m_helper.addLabelFieldPair(bPanel, LocaleResources.getString("csOptionsPanelLabelMinorRevLabelSequence", null), new CustomTextField(40), "MinorRevSeq");
/*     */ 
/* 156 */     gridBag.m_gc.weighty = 1.0D;
/* 157 */     gridBag.addEmptyRow(infoPanel);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 168 */     super.exchangeComponentValue(exchange, updateComponent);
/*     */ 
/* 171 */     if (updateComponent)
/*     */       return;
/* 173 */     String name = exchange.m_compName;
/* 174 */     String val = exchange.m_compValue;
/*     */ 
/* 176 */     if ((name.equals("IsOverrideFormat")) || (name.equals("IsAutoNumber")) || (name.equals("DownloadApplet")) || (name.equals("MultiUpload")))
/*     */     {
/* 181 */       String str = this.m_helper.m_props.getProperty(name);
/* 182 */       boolean flag = StringUtils.convertToBool(str, false);
/*     */ 
/* 184 */       if (flag)
/*     */       {
/* 186 */         this.m_helper.m_props.put(name, "true");
/*     */       }
/*     */       else
/*     */       {
/* 190 */         this.m_helper.m_props.remove(name);
/*     */       }
/*     */     } else {
/* 193 */       if (val.length() != 0)
/*     */         return;
/* 195 */       this.m_helper.m_props.remove(name);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 203 */     String name = exchange.m_compName;
/* 204 */     String val = exchange.m_compValue;
/*     */ 
/* 206 */     if ((val.length() > 0) && (((name.equals("MajorRevSeq")) || (name.equals("MinorRevSeq")))))
/*     */     {
/*     */       try
/*     */       {
/* 211 */         if (name.equals("MinorRevSeq"))
/*     */         {
/* 213 */           String majorRev = SharedObjects.getEnvironmentValue("MajorRevSeq");
/*     */ 
/* 215 */           if (majorRev == null)
/*     */           {
/* 217 */             throw new ServiceException("!csOptionsPanelMajorRevSequenceUndefined");
/*     */           }
/*     */         }
/*     */ 
/* 221 */         SharedObjects.putEnvironmentValue(name, val);
/*     */ 
/* 224 */         RevisionSpec.initImplementor();
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 228 */         SharedObjects.removeEnvironmentValue(name);
/* 229 */         exchange.m_errorMessage = LocaleUtils.createMessageListFromThrowable(e);
/* 230 */         return false;
/*     */       }
/*     */     }
/* 233 */     else if ((name.equals("AutoNumberPrefix")) && 
/* 235 */       (val.indexOf("<$") < 0))
/*     */     {
/* 238 */       int result = Validation.checkUrlFileSegment(val);
/* 239 */       switch (result)
/*     */       {
/*     */       case -2:
/* 242 */         exchange.m_errorMessage = IdcMessageFactory.lc("csOptionsPanelAutoNamePrefixSpacesError", new Object[0]);
/* 243 */         return false;
/*     */       case -3:
/* 246 */         exchange.m_errorMessage = IdcMessageFactory.lc("csOptionsPanelAutoNamePrefixIllegalChars", new Object[] { ";/\\?:@&=+\"#%<>*~|[]ıİ" });
/*     */ 
/* 248 */         return false;
/*     */       }
/*     */ 
/* 251 */       if (val.length() > 15)
/*     */       {
/* 253 */         exchange.m_errorMessage = IdcMessageFactory.lc("csOptionsPanelAutoNamePrefixTooLong", new Object[0]);
/* 254 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 258 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 263 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83608 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.OptionsPanel
 * JD-Core Version:    0.5.4
 */