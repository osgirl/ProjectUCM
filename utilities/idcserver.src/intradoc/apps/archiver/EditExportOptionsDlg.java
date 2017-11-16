/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditExportOptionsDlg extends EditDlg
/*     */   implements ComponentBinder
/*     */ {
/*     */   public EditExportOptionsDlg(SystemInterface sys, String title, CollectionContext context)
/*     */   {
/*  40 */     super(sys, title, context, "EditExportOptions");
/*  41 */     this.m_editItems = "aDoReplace,aCopyWebDocuments,aExportTableOnly";
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel pnl)
/*     */   {
/*  47 */     JCheckBox rpBox = new JCheckBox(LocaleResources.getString("apTitleReplaceExistingExportFiles", this.m_cxt));
/*     */ 
/*  49 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  50 */     this.m_helper.addExchangeComponent(pnl, rpBox, "aDoReplace");
/*  51 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  52 */     JCheckBox copyDocuments = new JCheckBox(LocaleResources.getString("apExportCopyWebContent", this.m_cxt));
/*  53 */     this.m_helper.addExchangeComponent(pnl, copyDocuments, "aCopyWebDocuments");
/*  54 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  55 */     JCheckBox tableOnly = new JCheckBox(LocaleResources.getString("apExportTableOnly", this.m_cxt));
/*  56 */     this.m_helper.addExchangeComponent(pnl, tableOnly, "aExportTableOnly");
/*     */ 
/*  58 */     ItemListener listener = new ItemListener(copyDocuments, tableOnly)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent event)
/*     */       {
/*  62 */         JCheckBox checkbox = (JCheckBox)event.getSource();
/*  63 */         JCheckBox opposingCheckbox = this.val$copyDocuments;
/*  64 */         String keyLabel = "aCopyWebDocuments";
/*  65 */         if (checkbox == this.val$copyDocuments)
/*     */         {
/*  68 */           opposingCheckbox = this.val$tableOnly;
/*  69 */           keyLabel = "aExportTableOnly";
/*     */         }
/*  71 */         boolean state = checkbox.isSelected();
/*  72 */         opposingCheckbox.setEnabled(!state);
/*  73 */         if (state != true)
/*     */           return;
/*  75 */         EditExportOptionsDlg.this.m_helper.m_props.put(keyLabel, "0");
/*  76 */         opposingCheckbox.setSelected(false);
/*     */       }
/*     */     };
/*  80 */     tableOnly.addItemListener(listener);
/*  81 */     copyDocuments.addItemListener(listener);
/*     */ 
/*  83 */     this.m_helper.m_componentBinder = this;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*  94 */     String name = exchange.m_compName;
/*  95 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 101 */     if (updateComponent)
/*     */     {
/* 103 */       String value = this.m_helper.m_props.getProperty(name);
/* 104 */       if (value != null)
/*     */       {
/* 106 */         value = value.trim();
/*     */       }
/* 108 */       if (name.equals("aCopyWebDocuments"))
/*     */       {
/* 110 */         String isTableOnly = this.m_helper.m_props.getProperty("aExportTableOnly");
/* 111 */         if (StringUtils.convertToBool(isTableOnly, false))
/*     */         {
/* 113 */           exchange.m_component.setEnabled(false);
/* 114 */           value = "0";
/*     */         }
/*     */       }
/* 117 */       else if (name.equals("aExportTableOnly"))
/*     */       {
/* 119 */         String isCopyWebDoc = this.m_helper.m_props.getProperty("aCopyWebDocuments");
/* 120 */         if (StringUtils.convertToBool(isCopyWebDoc, false))
/*     */         {
/* 122 */           exchange.m_component.setEnabled(false);
/* 123 */           value = "0";
/*     */         }
/*     */       }
/* 126 */       exchange.m_compValue = value;
/*     */     }
/*     */     else
/*     */     {
/* 130 */       this.m_helper.m_props.put(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 136 */     String val = exchange.m_compValue;
/* 137 */     if (val != null)
/*     */     {
/* 139 */       val = val.trim();
/* 140 */       exchange.m_compValue = val;
/*     */     }
/*     */ 
/* 143 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 148 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditExportOptionsDlg
 * JD-Core Version:    0.5.4
 */