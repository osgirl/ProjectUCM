/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JCheckBox;
/*     */ 
/*     */ public class ScriptCustomPanel extends ScriptPanelBase
/*     */   implements ItemListener
/*     */ {
/*     */   protected JCheckBox m_isCustomBox;
/*     */   protected CustomTextArea m_customScriptText;
/*     */ 
/*     */   public ScriptCustomPanel()
/*     */   {
/*  42 */     this.m_isCustomBox = null;
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  48 */     this.m_helper.m_props = this.m_scriptData.getLocalData();
/*     */ 
/*  50 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*  51 */     gh.m_gc.weightx = 0.0D;
/*  52 */     gh.m_gc.weighty = 0.0D;
/*  53 */     gh.m_gc.insets = new Insets(5, 5, 5, 5);
/*  54 */     this.m_isCustomBox = new JCheckBox(LocaleResources.getString("apTitleCustomScriptExpression", this.m_cxt));
/*  55 */     this.m_helper.addLastComponentInRow(this, this.m_isCustomBox);
/*  56 */     this.m_helper.m_exchange.addComponent("wfIsCustomScript", this.m_isCustomBox, null);
/*     */ 
/*  58 */     gh.m_gc.weightx = 1.0D;
/*  59 */     gh.m_gc.weighty = 1.0D;
/*  60 */     this.m_customScriptText = new CustomTextArea(10, 50);
/*     */ 
/*  62 */     this.m_helper.addLastComponentInRow(this, this.m_customScriptText);
/*  63 */     this.m_helper.m_exchange.addComponent("wfCustomScript", this.m_customScriptText, null);
/*     */ 
/*  66 */     String str = this.m_scriptData.getLocal("wfIsCustomScript");
/*  67 */     boolean isSelected = StringUtils.convertToBool(str, false);
/*  68 */     this.m_isCustomBox.setSelected(isSelected);
/*  69 */     this.m_customScriptText.setEnabled(isSelected);
/*  70 */     this.m_isCustomBox.addItemListener(this);
/*     */   }
/*     */ 
/*     */   protected String formatScript()
/*     */   {
/*  75 */     String str = "";
/*     */     try
/*     */     {
/*  78 */       Properties scriptProps = this.m_scriptData.getLocalData();
/*  79 */       str = WorkflowScriptUtils.formatString(this.m_jumpSet, scriptProps);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  83 */       this.m_context.reportError(e);
/*     */     }
/*     */ 
/*  86 */     return str;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/*  94 */     boolean isChecked = checkCustomScript();
/*  95 */     this.m_scriptData.putLocal("wfIsCustomScript", String.valueOf(isChecked));
/*     */   }
/*     */ 
/*     */   protected boolean checkCustomScript()
/*     */   {
/* 100 */     this.m_helper.retrieveComponentValues();
/* 101 */     boolean isChecked = this.m_isCustomBox.isSelected();
/* 102 */     this.m_customScriptText.setEnabled(isChecked);
/*     */ 
/* 105 */     if (!isChecked)
/*     */     {
/* 107 */       String str = formatScript();
/* 108 */       this.m_helper.m_exchange.setComponentValue("wfIsCustomScript", str);
/* 109 */       this.m_customScriptText.setText(str);
/*     */     }
/*     */ 
/* 112 */     return isChecked;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 121 */     ContainerHelper helper = (ContainerHelper)exchange.m_currentObject;
/* 122 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public void focusGained(FocusEvent e)
/*     */   {
/* 131 */     checkCustomScript();
/*     */   }
/*     */ 
/*     */   public void focusLost(FocusEvent e)
/*     */   {
/* 137 */     this.m_helper.retrieveComponentValues();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 142 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79317 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.ScriptCustomPanel
 * JD-Core Version:    0.5.4
 */