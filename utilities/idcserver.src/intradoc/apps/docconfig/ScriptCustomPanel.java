/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.DocProfileScriptUtils;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.text.JTextComponent;
/*     */ 
/*     */ public class ScriptCustomPanel extends DocConfigPanel
/*     */   implements ItemListener
/*     */ {
/*     */   protected DataBinder m_scriptData;
/*     */   protected DataResultSet m_clauseSet;
/*     */   protected String m_customKey;
/*     */   protected String m_isCustomKey;
/*     */   protected String m_tableName;
/*     */   protected String m_valueKey;
/*     */   protected String m_scriptType;
/*     */   protected JCheckBox m_isCustomBox;
/*     */   protected JTextComponent m_customScriptText;
/*     */ 
/*     */   public ScriptCustomPanel()
/*     */   {
/*  45 */     this.m_scriptData = null;
/*  46 */     this.m_clauseSet = null;
/*  47 */     this.m_customKey = null;
/*  48 */     this.m_isCustomKey = null;
/*  49 */     this.m_tableName = null;
/*  50 */     this.m_valueKey = null;
/*  51 */     this.m_scriptType = null;
/*     */ 
/*  54 */     this.m_isCustomBox = null;
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  60 */     this.m_scriptData = binder;
/*  61 */     this.m_clauseSet = ((DataResultSet)binder.getResultSet(this.m_tableName));
/*     */ 
/*  63 */     super.initEx(sys, binder);
/*     */ 
/*  65 */     this.m_helper.m_props = this.m_scriptData.getLocalData();
/*  66 */     initUI();
/*     */ 
/*  68 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  73 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*  74 */     gh.useGridBag(this);
/*     */ 
/*  76 */     gh.m_gc.anchor = 18;
/*  77 */     gh.m_gc.weightx = 0.0D;
/*  78 */     gh.m_gc.weighty = 0.0D;
/*  79 */     gh.m_gc.insets = new Insets(5, 5, 5, 5);
/*  80 */     this.m_isCustomBox = new JCheckBox(LocaleResources.getString("apDpTitleCustomScriptExpression", this.m_ctx));
/*  81 */     this.m_helper.addLastComponentInRow(this, this.m_isCustomBox);
/*  82 */     this.m_helper.m_exchange.addComponent(this.m_isCustomKey, this.m_isCustomBox, null);
/*     */ 
/*  84 */     gh.m_gc.weightx = 1.0D;
/*  85 */     gh.m_gc.weighty = 1.0D;
/*  86 */     gh.m_gc.fill = 1;
/*  87 */     this.m_customScriptText = new CustomTextArea(10, 50);
/*  88 */     this.m_helper.addLastComponentInRow(this, this.m_customScriptText);
/*  89 */     this.m_helper.m_exchange.addComponent(this.m_customKey, this.m_customScriptText, null);
/*     */ 
/*  92 */     String str = this.m_scriptData.getLocal(this.m_isCustomKey);
/*  93 */     boolean isSelected = StringUtils.convertToBool(str, false);
/*  94 */     this.m_isCustomBox.setSelected(isSelected);
/*  95 */     this.m_customScriptText.setEnabled(isSelected);
/*     */ 
/*  97 */     this.m_isCustomBox.addItemListener(this);
/*     */   }
/*     */ 
/*     */   protected String formatScript()
/*     */   {
/* 102 */     String str = "";
/*     */     try
/*     */     {
/* 105 */       Properties scriptProps = this.m_scriptData.getLocalData();
/* 106 */       scriptProps.put("ScriptType", this.m_scriptType);
/* 107 */       str = DocProfileScriptUtils.formatString(this.m_clauseSet, scriptProps, this.m_isCustomKey, this.m_valueKey);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 111 */       reportError(e);
/*     */     }
/*     */ 
/* 114 */     return str;
/*     */   }
/*     */ 
/*     */   protected void loadConfiguration(Properties props)
/*     */   {
/* 121 */     this.m_tableName = props.getProperty("TableName");
/* 122 */     this.m_customKey = props.getProperty("CustomKey");
/* 123 */     this.m_isCustomKey = props.getProperty("IsCustomKey");
/* 124 */     this.m_valueKey = props.getProperty("ValueKey");
/* 125 */     this.m_scriptType = props.getProperty("ScriptType");
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 133 */     boolean isChecked = checkCustomScript();
/* 134 */     this.m_scriptData.putLocal(this.m_customKey, String.valueOf(isChecked));
/*     */   }
/*     */ 
/*     */   protected boolean checkCustomScript()
/*     */   {
/* 139 */     this.m_helper.retrieveComponentValues();
/* 140 */     boolean isChecked = this.m_isCustomBox.isSelected();
/* 141 */     this.m_customScriptText.setEnabled(isChecked);
/*     */ 
/* 143 */     if (!isChecked)
/*     */     {
/* 145 */       String str = formatScript();
/* 146 */       this.m_helper.m_exchange.setComponentValue(this.m_customKey, str);
/* 147 */       this.m_customScriptText.setText(str);
/*     */     }
/*     */ 
/* 150 */     return isChecked;
/*     */   }
/*     */ 
/*     */   public void focusGained(FocusEvent e)
/*     */   {
/* 159 */     checkCustomScript();
/*     */   }
/*     */ 
/*     */   public void focusLost(FocusEvent e)
/*     */   {
/* 165 */     this.m_helper.retrieveComponentValues();
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 176 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79728 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.ScriptCustomPanel
 * JD-Core Version:    0.5.4
 */