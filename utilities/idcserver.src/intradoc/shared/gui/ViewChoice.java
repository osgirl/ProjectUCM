/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.AggregateImplementor;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.TextHandler;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class ViewChoice extends PanePanel
/*     */   implements AggregateImplementor, ActionListener, TextHandler
/*     */ {
/*     */   public SystemInterface m_systemInterface;
/*  53 */   public SharedContext m_shContext = null;
/*  54 */   public SchemaHelper m_schHelper = null;
/*  55 */   public ViewFieldDef m_fieldDef = null;
/*     */ 
/*  57 */   protected String m_valueSeparator = ", ";
/*  58 */   protected boolean m_enableMultiSelect = true;
/*     */   protected String m_fieldPrefix;
/*  66 */   public JTextField m_textField = null;
/*  67 */   public JButton m_browseBtn = null;
/*     */ 
/*     */   public ViewChoice(SystemInterface sys, SharedContext shContext)
/*     */   {
/*  71 */     this.m_systemInterface = sys;
/*  72 */     this.m_shContext = shContext;
/*     */   }
/*     */ 
/*     */   public void init(SchemaHelper schHelper, ViewFieldDef fieldDef, int minCols, String brCaption)
/*     */   {
/*  77 */     this.m_schHelper = schHelper;
/*  78 */     this.m_fieldDef = fieldDef;
/*     */ 
/*  80 */     createUI(minCols, brCaption);
/*     */   }
/*     */ 
/*     */   protected void createUI(int minCols, String brCaption)
/*     */   {
/*  85 */     this.m_textField = new CustomTextField(minCols);
/*  86 */     createBrowseButton(brCaption);
/*     */ 
/*  88 */     GridBagConstraints gc = new GridBagConstraints();
/*  89 */     GridBagLayout gridBag = new GridBagLayout();
/*  90 */     setLayout(gridBag);
/*     */ 
/*  92 */     gc.insets = new Insets(0, 0, 0, 0);
/*  93 */     gc.weightx = 1.0D;
/*  94 */     gc.weighty = 0.0D;
/*  95 */     gc.fill = 2;
/*  96 */     gc.anchor = 17;
/*  97 */     gc.gridwidth = -1;
/*     */ 
/*  99 */     gridBag.setConstraints(this.m_textField, gc);
/* 100 */     add(this.m_textField);
/*     */ 
/* 102 */     gc.weightx = 0.0D;
/* 103 */     gc.weighty = 0.0D;
/* 104 */     gc.fill = 0;
/* 105 */     gc.gridwidth = 0;
/*     */ 
/* 107 */     gridBag.setConstraints(this.m_browseBtn, gc);
/* 108 */     add(this.m_browseBtn);
/*     */   }
/*     */ 
/*     */   protected void createBrowseButton(String caption)
/*     */   {
/* 113 */     if ((caption == null) || (caption.length() == 0))
/*     */     {
/* 115 */       caption = "...";
/*     */     }
/* 117 */     this.m_browseBtn = new JButton(caption);
/* 118 */     this.m_browseBtn.addActionListener(this);
/*     */   }
/*     */ 
/*     */   public void setEnabled(boolean isEnabled)
/*     */   {
/* 124 */     this.m_textField.setEnabled(isEnabled);
/* 125 */     this.m_browseBtn.setEnabled(isEnabled);
/*     */   }
/*     */ 
/*     */   public void setEnableMultiSelect(boolean isMulti)
/*     */   {
/* 130 */     this.m_enableMultiSelect = isMulti;
/*     */   }
/*     */ 
/*     */   public void setFieldPrefix(String prefix)
/*     */   {
/* 135 */     this.m_fieldPrefix = prefix;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 142 */     String title = LocaleUtils.encodeMessage("apSchSelectValue", null, this.m_fieldDef.m_caption);
/* 143 */     title = LocaleResources.localizeMessage(title, this.m_systemInterface.getExecutionContext());
/*     */ 
/* 145 */     String helpPage = DialogHelpTable.getHelpPage("SelectViewValue");
/* 146 */     SelectValueDlg dlg = new SelectValueDlg(this.m_systemInterface, title, helpPage, this.m_shContext);
/*     */ 
/* 148 */     boolean isMultiSelect = (this.m_enableMultiSelect) && (this.m_fieldDef.isMultiOptionList());
/*     */ 
/* 150 */     String name = this.m_fieldDef.m_name;
/* 151 */     if ((this.m_fieldPrefix != null) && (name.startsWith(this.m_fieldPrefix)))
/*     */     {
/* 153 */       name = name.substring(this.m_fieldPrefix.length());
/*     */     }
/* 155 */     dlg.init(this.m_schHelper, name, null, true, false, true);
/* 156 */     if (dlg.prompt() != 1)
/*     */       return;
/* 158 */     String currentValue = this.m_textField.getText().trim();
/* 159 */     String val = dlg.getSelectedValue();
/*     */ 
/* 161 */     if ((currentValue.length() != 0) && (isMultiSelect))
/*     */     {
/* 163 */       val = currentValue + this.m_valueSeparator + val;
/*     */     }
/* 165 */     this.m_textField.setText(val);
/*     */   }
/*     */ 
/*     */   public Component getComponent()
/*     */   {
/* 174 */     return this.m_textField;
/*     */   }
/*     */ 
/*     */   public Component getBuddy(int index)
/*     */   {
/* 179 */     return null;
/*     */   }
/*     */ 
/*     */   public Vector getBuddies()
/*     */   {
/* 184 */     return null;
/*     */   }
/*     */ 
/*     */   public JButton getBrowseButton()
/*     */   {
/* 189 */     return this.m_browseBtn;
/*     */   }
/*     */ 
/*     */   public String getText()
/*     */   {
/* 197 */     return this.m_textField.getText();
/*     */   }
/*     */ 
/*     */   public void setText(String text)
/*     */   {
/* 202 */     this.m_textField.setText(text);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 207 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.ViewChoice
 * JD-Core Version:    0.5.4
 */