/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.ResultData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFields;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.FocusListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextArea;
/*     */ import javax.swing.JTextField;
/*     */ import javax.swing.text.JTextComponent;
/*     */ 
/*     */ public class EditResultCustomPanel extends CustomPanel
/*     */   implements FocusListener, ActionListener, ItemListener
/*     */ {
/*  71 */   protected ContainerHelper m_helper = null;
/*  72 */   protected DataResultSet m_verityTemplates = null;
/*  73 */   protected ResultData m_currentResult = null;
/*  74 */   protected boolean m_isNew = true;
/*     */ 
/*  77 */   protected boolean m_showCustomCheckbox = false;
/*  78 */   protected JCheckBox m_useCustom = null;
/*     */ 
/*  81 */   protected JComboBox m_templateChoiceList = null;
/*     */ 
/*  84 */   protected JTextField m_text1 = null;
/*  85 */   protected JTextArea m_text2 = null;
/*  86 */   protected JButton m_insert1 = null;
/*  87 */   protected JButton m_insert2 = null;
/*  88 */   protected boolean m_sel1 = false;
/*  89 */   protected boolean m_sel2 = false;
/*     */ 
/*  92 */   protected FixedSizeList m_fieldList = null;
/*  93 */   protected Vector m_fieldDefs = null;
/*  94 */   protected String[][] m_fieldCaptions = (String[][])null;
/*     */ 
/*  97 */   protected ExecutionContext m_ctx = null;
/*     */ 
/*  99 */   protected static String[][] HTML_EXTRAS = { { "<br>", "apLabelHtmlPageBreak" } };
/*     */ 
/*     */   public EditResultCustomPanel(boolean showCustomCheckbox, ContainerHelper helper, JComboBox templateChoiceList)
/*     */   {
/* 107 */     this.m_templateChoiceList = templateChoiceList;
/* 108 */     this.m_helper = helper;
/* 109 */     this.m_showCustomCheckbox = showCustomCheckbox;
/* 110 */     this.m_ctx = helper.m_exchange.m_sysInterface.getExecutionContext();
/*     */   }
/*     */ 
/*     */   public EditResultCustomPanel(boolean showCustomCheckbox, ContainerHelper helper)
/*     */   {
/* 116 */     this.m_helper = helper;
/* 117 */     this.m_showCustomCheckbox = showCustomCheckbox;
/* 118 */     this.m_ctx = helper.m_exchange.m_sysInterface.getExecutionContext();
/*     */   }
/*     */ 
/*     */   public void init(ResultData data)
/*     */   {
/* 123 */     init();
/* 124 */     if (data == null)
/*     */       return;
/* 126 */     this.m_currentResult = data;
/* 127 */     this.m_isNew = false;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 133 */     this.m_verityTemplates = SharedObjects.getTable("CurrentVerityTemplates");
/* 134 */     this.m_currentResult = new ResultData();
/* 135 */     this.m_currentResult.setValues(null);
/*     */ 
/* 137 */     int width = 30;
/* 138 */     int height = 15;
/* 139 */     if (this.m_showCustomCheckbox)
/*     */     {
/* 141 */       width = 20;
/* 142 */       height = 10;
/*     */     }
/*     */ 
/* 145 */     JPanel flexPanel = new PanePanel();
/* 146 */     this.m_helper.makePanelGridBag(flexPanel, 1);
/* 147 */     GridBagHelper gridHelper = this.m_helper.m_gridHelper;
/* 148 */     gridHelper.m_gc.insets = new Insets(0, height, height, height);
/*     */ 
/* 150 */     if (this.m_showCustomCheckbox)
/*     */     {
/* 152 */       this.m_useCustom = new CustomCheckbox(LocaleResources.getString("apLabelUseCustomizedText", this.m_ctx));
/* 153 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 154 */       this.m_helper.addExchangeComponent(flexPanel, this.m_useCustom, "UseCustomText");
/* 155 */       this.m_useCustom.addItemListener(this);
/*     */     }
/*     */ 
/* 158 */     gridHelper.m_gc.weightx = 1.0D;
/* 159 */     gridHelper.m_gc.weighty = 0.0D;
/* 160 */     this.m_helper.addLabelFieldPairEx(flexPanel, LocaleResources.getString("apLabelText1", this.m_ctx), this.m_text1 = new CustomTextField(width), "Text1", false);
/*     */ 
/* 162 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 163 */     gridHelper.m_gc.fill = 0;
/* 164 */     gridHelper.m_gc.weightx = 0.0D;
/* 165 */     gridHelper.m_gc.weighty = 0.0D;
/* 166 */     this.m_helper.addComponent(flexPanel, this.m_insert1 = new JButton("  <<  "));
/* 167 */     this.m_text1.addFocusListener(this);
/* 168 */     this.m_insert1.setEnabled(false);
/* 169 */     this.m_insert1.addActionListener(this);
/*     */ 
/* 171 */     gridHelper.m_gc.insets = new Insets(0, height, height, height);
/* 172 */     gridHelper.m_gc.weightx = 1.0D;
/* 173 */     gridHelper.m_gc.weighty = 1.0D;
/* 174 */     gridHelper.m_gc.fill = 1;
/* 175 */     this.m_helper.addLabelFieldPairEx(flexPanel, LocaleResources.getString("apLabelText2", this.m_ctx), this.m_text2 = new CustomTextArea(5, width), "Text2", false);
/* 176 */     gridHelper.prepareAddLastRowElement(17);
/* 177 */     gridHelper.m_gc.weightx = 0.0D;
/* 178 */     gridHelper.m_gc.weighty = 0.0D;
/* 179 */     gridHelper.m_gc.fill = 0;
/* 180 */     this.m_helper.addComponent(flexPanel, this.m_insert2 = new JButton("  <<  "));
/* 181 */     this.m_text2.addFocusListener(this);
/* 182 */     this.m_insert2.setEnabled(false);
/* 183 */     this.m_insert2.addActionListener(this);
/*     */ 
/* 185 */     JPanel listPanel = new PanePanel();
/* 186 */     listPanel.setLayout(new BorderLayout());
/* 187 */     listPanel.add("North", new CustomLabel(LocaleResources.getString("apTitleField", this.m_ctx), 1));
/*     */ 
/* 189 */     listPanel.add("Center", this.m_fieldList = new FixedSizeList(height));
/*     */ 
/* 192 */     this.m_helper.makePanelGridBag(this, 1);
/* 193 */     gridHelper = this.m_helper.m_gridHelper;
/* 194 */     gridHelper.m_gc.weightx = 3.0D;
/* 195 */     gridHelper.m_gc.weighty = 1.0D;
/* 196 */     this.m_helper.addComponent(this, flexPanel);
/* 197 */     gridHelper.m_gc.weightx = 1.0D;
/* 198 */     this.m_helper.addComponent(this, listPanel);
/*     */ 
/* 201 */     initList();
/*     */ 
/* 204 */     if (!this.m_showCustomCheckbox)
/*     */       return;
/* 206 */     setEnableDisableEdit(false);
/*     */   }
/*     */ 
/*     */   protected void initList()
/*     */   {
/* 215 */     ResultSet metaFields = SharedObjects.getTable("DocMetaDefinition");
/* 216 */     ViewFields docFieldsObj = new ViewFields(this.m_ctx);
/*     */     try
/*     */     {
/* 219 */       this.m_fieldDefs = docFieldsObj.createSearchableFieldsList(metaFields);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*     */     }
/*     */ 
/* 226 */     this.m_fieldCaptions = new String[this.m_fieldDefs.size() + HTML_EXTRAS.length][2];
/* 227 */     int numFields = this.m_fieldDefs.size();
/* 228 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 230 */       FieldDef fieldDef = (FieldDef)this.m_fieldDefs.elementAt(i);
/* 231 */       this.m_fieldCaptions[i][0] = ("<$" + fieldDef.m_name + "$> ");
/* 232 */       this.m_fieldCaptions[i][1] = fieldDef.m_caption;
/* 233 */       this.m_fieldList.add(fieldDef.m_caption);
/*     */     }
/*     */ 
/* 237 */     for (int i = 0; i < HTML_EXTRAS.length; ++i)
/*     */     {
/* 239 */       this.m_fieldCaptions[(i + numFields)][0] = HTML_EXTRAS[i][0];
/*     */ 
/* 241 */       String caption = LocaleResources.getString(HTML_EXTRAS[i][1], this.m_ctx);
/* 242 */       this.m_fieldCaptions[(i + numFields)][1] = caption;
/* 243 */       this.m_fieldList.add(caption);
/*     */     }
/*     */ 
/* 246 */     this.m_fieldList.addActionListener(this);
/*     */   }
/*     */ 
/*     */   public ResultData getData()
/*     */   {
/* 254 */     return this.m_currentResult;
/*     */   }
/*     */ 
/*     */   public DataResultSet getTemplates()
/*     */   {
/* 259 */     return this.m_verityTemplates;
/*     */   }
/*     */ 
/*     */   public void alterTemplateTextFields(int index)
/*     */   {
/*     */     try
/*     */     {
/* 269 */       if ((index < 0) || ((this.m_showCustomCheckbox) && (this.m_useCustom.isSelected())))
/*     */       {
/* 271 */         return;
/*     */       }
/* 273 */       this.m_verityTemplates.setCurrentRow(index);
/* 274 */       Properties p = this.m_verityTemplates.getCurrentRowProps();
/* 275 */       this.m_currentResult.setValues(p);
/* 276 */       this.m_text1.setText(this.m_currentResult.get("Text1"));
/* 277 */       this.m_text2.setText(this.m_currentResult.get("Text2"));
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 281 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 283 */       Report.debug("applet", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 293 */     boolean isSelected = e.getStateChange() == 1;
/* 294 */     this.m_templateChoiceList.setEnabled(!isSelected);
/* 295 */     setEnableDisableEdit(isSelected);
/*     */   }
/*     */ 
/*     */   public void setEnableDisableEdit(boolean isEnabled)
/*     */   {
/* 303 */     this.m_text1.setEnabled(isEnabled);
/* 304 */     this.m_text2.setEnabled(isEnabled);
/* 305 */     this.m_fieldList.setEnabled(isEnabled);
/* 306 */     if ((isEnabled) || (this.m_insert1 == null)) {
/*     */       return;
/*     */     }
/* 309 */     this.m_insert1.setEnabled(false);
/* 310 */     this.m_insert2.setEnabled(false);
/*     */   }
/*     */ 
/*     */   public void focusGained(FocusEvent e)
/*     */   {
/* 319 */     Object src = e.getSource();
/* 320 */     if (src == this.m_text1)
/*     */     {
/* 322 */       enableDisable(true);
/*     */     } else {
/* 324 */       if (src != this.m_text2)
/*     */         return;
/* 326 */       enableDisable(false);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void focusLost(FocusEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void enableDisable(boolean isID)
/*     */   {
/* 343 */     this.m_insert1.setEnabled(isID);
/* 344 */     this.m_sel1 = isID;
/*     */ 
/* 346 */     this.m_insert2.setEnabled(!isID);
/* 347 */     this.m_sel2 = (!isID);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 356 */     int index = this.m_fieldList.getSelectedIndex();
/* 357 */     if (index < 0)
/*     */     {
/* 359 */       return;
/*     */     }
/*     */ 
/* 362 */     String str = this.m_fieldCaptions[index][0];
/* 363 */     if (this.m_sel1)
/*     */     {
/* 365 */       insertFieldText(this.m_text1, str);
/*     */     } else {
/* 367 */       if (!this.m_sel2)
/*     */         return;
/* 369 */       insertFieldText(this.m_text2, str);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void insertFieldText(JTextComponent fieldText, String str)
/*     */   {
/* 379 */     String curText = fieldText.getText();
/*     */ 
/* 381 */     boolean useCaret = false;
/* 382 */     int selBegin = fieldText.getSelectionStart();
/* 383 */     int selEnd = fieldText.getSelectionEnd();
/*     */ 
/* 385 */     if ((selEnd < 0) || (selBegin < 0))
/*     */     {
/* 387 */       useCaret = true;
/*     */     }
/*     */ 
/* 390 */     int caretPos = fieldText.getCaretPosition();
/* 391 */     if (caretPos < 0)
/*     */     {
/* 393 */       return;
/*     */     }
/*     */ 
/* 396 */     String begin = "";
/* 397 */     String end = "";
/*     */ 
/* 399 */     if (useCaret)
/*     */     {
/* 401 */       begin = curText.substring(0, caretPos);
/* 402 */       end = curText.substring(caretPos);
/*     */     }
/*     */     else
/*     */     {
/* 406 */       begin = curText.substring(0, selBegin);
/* 407 */       end = curText.substring(selEnd);
/*     */     }
/*     */ 
/* 410 */     if (fieldText instanceof JTextArea)
/*     */     {
/* 412 */       ((JTextArea)fieldText).replaceRange(str, selBegin, selEnd);
/*     */     }
/*     */     else
/*     */     {
/* 416 */       fieldText.setText(begin + str + end);
/*     */     }
/* 418 */     fieldText.requestFocus();
/*     */ 
/* 420 */     fieldText.setCaretPosition(selBegin + str.length());
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 428 */     String name = exchange.m_compName;
/* 429 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 435 */     if (updateComponent)
/*     */     {
/* 437 */       exchange.m_compValue = this.m_currentResult.get(name);
/*     */     }
/*     */     else
/*     */     {
/* 441 */       this.m_currentResult.put(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 447 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditResultCustomPanel
 * JD-Core Version:    0.5.4
 */