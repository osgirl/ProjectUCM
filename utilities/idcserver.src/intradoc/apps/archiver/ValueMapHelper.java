/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.ClausesData;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.gui.ClauseBuilderHelper;
/*     */ import intradoc.shared.gui.ViewChoice;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class ValueMapHelper extends ClauseBuilderHelper
/*     */ {
/*  57 */   protected String m_outputTitle = "apLabelOutputValue";
/*  58 */   protected CollectionContext m_context = null;
/*     */ 
/*  60 */   protected SharedContext m_sharedContext = null;
/*  61 */   protected SchemaHelper m_schHelper = null;
/*     */   protected JTextField m_mappedValue;
/*     */   protected DisplayChoice m_fields;
/*     */   protected ComboChoice m_values;
/*     */   protected JCheckBox m_mapAllBox;
/*  69 */   protected JButton m_browseValueBtn = null;
/*  70 */   protected DisplayChoice m_valueChoice = null;
/*  71 */   protected boolean m_hasValueChoices = false;
/*  72 */   protected JTextField m_clauseValue = null;
/*     */ 
/*  74 */   protected String m_selectedBatch = null;
/*  75 */   protected String m_selectedField = null;
/*     */ 
/*  77 */   protected boolean m_isTable = false;
/*  78 */   protected String m_tableName = null;
/*     */ 
/*     */   public ValueMapHelper(CollectionContext context)
/*     */   {
/*  82 */     this(context, null, false);
/*     */   }
/*     */ 
/*     */   public ValueMapHelper(CollectionContext context, String tableName, boolean isTable)
/*     */   {
/*  87 */     this.m_numSegments = 4;
/*  88 */     this.m_context = context;
/*  89 */     this.m_isTable = isTable;
/*  90 */     this.m_tableName = tableName;
/*     */   }
/*     */ 
/*     */   public void setTitles(String fieldTitle, String valueTitle, String outputTitle)
/*     */   {
/*  95 */     this.m_fieldTitle = fieldTitle;
/*  96 */     this.m_valueTitle = valueTitle;
/*  97 */     this.m_outputTitle = LocaleResources.getString(outputTitle, this.m_cxt);
/*     */   }
/*     */ 
/*     */   protected void setPanelData()
/*     */   {
/* 103 */     GridBagHelper gh = this.m_guiHelper.m_gridHelper;
/* 104 */     gh.reset();
/* 105 */     this.m_clauseEditPanel.removeAll();
/*     */ 
/* 107 */     gh.addEmptyRowElement(this.m_clauseEditPanel);
/* 108 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, new CustomLabel(this.m_valueTitle));
/* 109 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, new CustomLabel("  "));
/* 110 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, new CustomLabel(this.m_fieldTitle));
/* 111 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, new CustomLabel("  "));
/* 112 */     this.m_guiHelper.addLastComponentInRow(this.m_clauseEditPanel, new CustomLabel(this.m_outputTitle));
/*     */ 
/* 114 */     gh.m_gc.fill = 2;
/*     */ 
/* 116 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, this.m_mapAllBox = new JCheckBox(LocaleResources.getString("apLabelAll", this.m_cxt)));
/*     */ 
/* 118 */     this.m_guiHelper.m_exchange.addComponent("IsMapAll", this.m_mapAllBox, null);
/* 119 */     this.m_mapAllBox.addItemListener(this);
/*     */ 
/* 121 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, this.m_mappedValue = new JTextField(20));
/* 122 */     this.m_guiHelper.m_exchange.removeComponent("InputValue");
/* 123 */     this.m_guiHelper.m_exchange.addComponent("InputValue", this.m_mappedValue, null);
/*     */ 
/* 125 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, new CustomLabel(LocaleResources.getString("apLabelIn", this.m_cxt)));
/*     */ 
/* 128 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, this.m_fields = new DisplayChoice());
/* 129 */     this.m_guiHelper.m_exchange.removeComponent("MapField");
/* 130 */     this.m_guiHelper.m_exchange.addComponent("MapField", this.m_fields, null);
/* 131 */     this.m_fields.addItemListener(this);
/*     */ 
/* 133 */     CustomLabel lbl = new CustomLabel(LocaleResources.getString("apLabelMapsTo", this.m_cxt));
/* 134 */     lbl.setAlignment(0);
/* 135 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, lbl);
/*     */ 
/* 137 */     this.m_guiHelper.addLastComponentInRow(this.m_clauseEditPanel, this.m_clauseValPanel = new PanePanel());
/* 138 */     this.m_clauseValPanel.setLayout(new GridBagLayout());
/*     */ 
/* 140 */     gh.addEmptyRowElement(this.m_clauseEditPanel);
/* 141 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, this.m_valueChoice = new DisplayChoice());
/* 142 */     this.m_valueChoice.addItemListener(this);
/* 143 */     initValueChoice();
/*     */ 
/* 146 */     gh.m_gc.fill = 0;
/* 147 */     gh.m_gc.anchor = 17;
/* 148 */     gh.m_gc.gridx = 3;
/* 149 */     this.m_guiHelper.addLastComponentInRow(this.m_clauseEditPanel, this.m_browseValueBtn = new JButton(LocaleResources.getString("apDlgButtonBrowseForValues", this.m_cxt)));
/*     */ 
/* 152 */     this.m_browseValueBtn.addActionListener(this);
/*     */ 
/* 154 */     gh.addEmptyRowElement(this.m_clauseEditPanel);
/*     */ 
/* 156 */     addFieldList(this.m_fields);
/* 157 */     this.m_fields.addItemListener(this);
/*     */ 
/* 159 */     addOptionListChoices(null);
/* 160 */     this.m_clauseEditPanel.validate();
/*     */   }
/*     */ 
/*     */   public void initValueChoice()
/*     */   {
/* 167 */     this.m_valueChoice.removeAllItems();
/* 168 */     this.m_valueChoice.addItem(LocaleResources.getString("apChoiceNoValues", this.m_cxt));
/* 169 */     this.m_valueChoice.setEnabled(false);
/*     */ 
/* 172 */     this.m_hasValueChoices = false;
/*     */   }
/*     */ 
/*     */   public void addOptionListChoices(ViewFieldDef fieldDef)
/*     */   {
/* 177 */     this.m_clauseValPanel.removeAll();
/* 178 */     this.m_guiHelper.m_exchange.removeComponent("OutputValue");
/*     */ 
/* 180 */     GridBagHelper gh = this.m_guiHelper.m_gridHelper;
/* 181 */     gh.m_gc.insets = new Insets(0, 0, 0, 0);
/* 182 */     gh.m_gc.anchor = 18;
/* 183 */     gh.m_gc.fill = 2;
/* 184 */     gh.m_gc.weightx = 1.0D;
/*     */ 
/* 186 */     String optKey = null;
/* 187 */     if (fieldDef != null)
/*     */     {
/* 189 */       optKey = fieldDef.m_optionListKey;
/*     */     }
/*     */ 
/* 192 */     Component comp = null;
/* 193 */     if ((fieldDef != null) && (fieldDef.isComplexOptionList()))
/*     */     {
/* 195 */       if (this.m_schHelper == null)
/*     */       {
/* 197 */         this.m_schHelper = new SchemaHelper();
/* 198 */         this.m_schHelper.computeMaps();
/*     */       }
/*     */ 
/* 201 */       this.m_sharedContext = this.m_context.getSharedContext();
/* 202 */       ViewChoice vChoice = new ViewChoice(this.m_sysInterface, this.m_sharedContext);
/* 203 */       String btnLabel = this.m_sysInterface.getString("apSelectBtnLabel");
/* 204 */       vChoice.setEnableMultiSelect(false);
/* 205 */       vChoice.init(this.m_schHelper, fieldDef, 20, btnLabel);
/* 206 */       vChoice.setFieldPrefix("#active.");
/* 207 */       this.m_guiHelper.addExchangeComponent(this.m_clauseValPanel, vChoice, "OutputValue");
/*     */ 
/* 209 */       this.m_clauseValue = ((JTextField)vChoice.getComponent());
/*     */     }
/*     */     else
/*     */     {
/* 213 */       Component optList = getOptChoiceList(optKey);
/* 214 */       if ((optList == null) && (optKey != null) && (this.m_displayMaps != null))
/*     */       {
/* 216 */         String[][] optionMap = (String[][])(String[][])this.m_displayMaps.get(optKey);
/* 217 */         if (optionMap != null)
/*     */         {
/* 219 */           if ((fieldDef != null) && (fieldDef.isComboOptionList()))
/*     */           {
/* 221 */             ComboChoice dc = new ComboChoice();
/* 222 */             dc.initChoiceList(optionMap);
/* 223 */             optList = dc;
/*     */           }
/*     */           else
/*     */           {
/* 227 */             DisplayChoice dc = new DisplayChoice();
/* 228 */             dc.init(optionMap);
/* 229 */             optList = dc;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 234 */       comp = optList;
/* 235 */       if (comp == null)
/*     */       {
/* 237 */         comp = new JTextField(30);
/*     */       }
/* 239 */       this.m_guiHelper.addExchangeComponent(this.m_clauseValPanel, comp, "OutputValue");
/*     */     }
/*     */ 
/* 242 */     this.m_curOptionListKey = optKey;
/* 243 */     this.m_clauseValPanel.updateUI();
/*     */   }
/*     */ 
/*     */   protected ComboChoice createOptionChoice(JPanel pnl, String optKey)
/*     */   {
/* 248 */     pnl.removeAll();
/* 249 */     GridBagHelper gh = this.m_guiHelper.m_gridHelper;
/* 250 */     gh.m_gc.fill = 2;
/* 251 */     gh.m_gc.weightx = 1.0D;
/* 252 */     gh.m_gc.insets = new Insets(0, 0, 0, 0);
/*     */ 
/* 254 */     ComboChoice values = getOptChoiceList(optKey);
/* 255 */     if ((values == null) && (optKey != null) && (this.m_displayMaps != null))
/*     */     {
/* 257 */       String[][] optionMap = (String[][])(String[][])this.m_displayMaps.get(optKey);
/* 258 */       if (optionMap != null)
/*     */       {
/* 260 */         ComboChoice dc = new ComboChoice();
/* 261 */         dc.initChoiceList(optionMap);
/* 262 */         values = dc;
/*     */       }
/*     */     }
/*     */ 
/* 266 */     return values;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 275 */     String name = exchange.m_compName;
/* 276 */     int index = -1;
/* 277 */     if (name.equals("IsMapAll"))
/*     */     {
/* 279 */       index = 0;
/*     */     }
/* 281 */     else if (name.equals("InputValue"))
/*     */     {
/* 283 */       index = 1;
/*     */     }
/* 285 */     else if (name.equals("MapField"))
/*     */     {
/* 287 */       index = 2;
/*     */     }
/* 289 */     else if (name.equals("OutputValue"))
/*     */     {
/* 291 */       index = 3;
/*     */     }
/*     */ 
/* 294 */     String str = "";
/* 295 */     if (index >= 0)
/*     */     {
/* 297 */       if (updateComponent)
/*     */       {
/* 299 */         exchange.m_compValue = "";
/*     */       }
/*     */ 
/* 302 */       if (this.m_curClause == null)
/*     */         return;
/* 304 */       if (updateComponent)
/*     */       {
/* 306 */         str = (String)this.m_curClause.elementAt(index);
/* 307 */         exchange.m_compValue = str;
/*     */ 
/* 309 */         if (index == 0)
/*     */         {
/* 311 */           boolean isAll = StringUtils.convertToBool(str, false);
/* 312 */           this.m_mappedValue.setEnabled(!isAll);
/*     */         }
/*     */ 
/* 315 */         if (index != 2) {
/*     */           return;
/*     */         }
/* 318 */         checkSelectedClauseDocField(str);
/*     */       }
/*     */       else
/*     */       {
/* 323 */         str = exchange.m_compValue;
/* 324 */         this.m_curClause.setElementAt(str, index);
/*     */       }
/*     */ 
/*     */     }
/* 330 */     else if (updateComponent)
/*     */     {
/* 332 */       exchange.m_compValue = getQueryProp(name);
/*     */     }
/*     */     else
/*     */     {
/* 336 */       setQueryProp(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 344 */     String val = exchange.m_compValue;
/*     */ 
/* 346 */     if (val != null)
/*     */     {
/* 348 */       val = val.trim();
/* 349 */       if (val.length() == 0)
/*     */       {
/* 351 */         val = null;
/*     */       }
/*     */     }
/* 354 */     return true;
/*     */   }
/*     */ 
/*     */   protected void refreshClauseList()
/*     */     throws ServiceException
/*     */   {
/* 363 */     this.m_clauseList.removeAllItems();
/*     */ 
/* 366 */     Vector clauses = this.m_clauseData.m_clauses;
/* 367 */     int nclauses = clauses.size();
/*     */ 
/* 369 */     int curIndex = -1;
/*     */ 
/* 372 */     for (int i = 0; i < nclauses; ++i)
/*     */     {
/* 374 */       IdcStringBuilder dispStr = new IdcStringBuilder();
/* 375 */       Vector elts = (Vector)clauses.elementAt(i);
/* 376 */       if (this.m_curClause == elts)
/*     */       {
/* 378 */         curIndex = i;
/*     */       }
/*     */ 
/* 381 */       this.m_clauseData.createClauseString(elts, dispStr);
/* 382 */       this.m_clauseList.add(dispStr.toString());
/*     */     }
/*     */ 
/* 385 */     if (curIndex >= 0)
/*     */     {
/* 387 */       this.m_clauseList.select(curIndex);
/*     */     }
/*     */ 
/* 391 */     this.m_clauseData.m_dispStr = LocaleResources.getString("apLabelStandardValueMap", this.m_cxt);
/*     */ 
/* 395 */     enableDisable(false);
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 404 */     Object target = e.getSource();
/*     */ 
/* 406 */     if (target == this.m_clauseList)
/*     */     {
/* 408 */       enableDisable(true);
/*     */     }
/* 410 */     else if (target == this.m_fields)
/*     */     {
/* 412 */       checkSelectedClauseDocField();
/*     */     }
/* 414 */     else if (target == this.m_mapAllBox)
/*     */     {
/* 416 */       enableDisableForAll();
/*     */     } else {
/* 418 */       if (target != this.m_valueChoice)
/*     */         return;
/* 420 */       updateMappedValue();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateMappedValue()
/*     */   {
/* 426 */     String value = this.m_valueChoice.getSelectedInternalValue();
/* 427 */     if (value == null)
/*     */       return;
/* 429 */     this.m_mappedValue.setText(value);
/*     */   }
/*     */ 
/*     */   protected void enableDisableForAll()
/*     */   {
/* 435 */     boolean isAll = this.m_mapAllBox.isSelected();
/* 436 */     this.m_mappedValue.setEnabled(!isAll);
/* 437 */     this.m_valueChoice.setEnabled((this.m_hasValueChoices) && (!isAll));
/* 438 */     this.m_browseValueBtn.setEnabled(!isAll);
/*     */ 
/* 440 */     if (isAll != true)
/*     */       return;
/* 442 */     this.m_mappedValue.setText("");
/*     */   }
/*     */ 
/*     */   public void loadData()
/*     */     throws ServiceException
/*     */   {
/* 449 */     super.loadData();
/* 450 */     enableDisableForAll();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 459 */     Object target = e.getSource();
/* 460 */     if (target == this.m_browseValueBtn)
/*     */     {
/* 462 */       BrowseValueDlg dlg = new BrowseValueDlg(this.m_sysInterface, LocaleResources.getString("apLabelBrowseForValues", this.m_cxt), this.m_context);
/*     */ 
/* 464 */       if (dlg.init(this.m_selectedBatch, this.m_selectedField, this.m_tableName, this.m_isTable) == 1)
/*     */       {
/* 466 */         this.m_selectedBatch = dlg.getSelectedFile();
/* 467 */         this.m_selectedField = dlg.getSelectedField();
/* 468 */         Vector values = this.m_context.getBatchValues(this.m_selectedBatch, this.m_selectedField);
/* 469 */         sortValues(values);
/*     */ 
/* 472 */         int size = values.size();
/* 473 */         boolean isEmpty = false;
/* 474 */         if (size == 0)
/*     */         {
/* 476 */           this.m_context.reportError(LocaleResources.getString("apNoValuesForFieldInBatchFile", this.m_cxt, this.m_selectedField, this.m_selectedBatch));
/*     */ 
/* 478 */           isEmpty = true;
/*     */         }
/*     */ 
/* 481 */         if (isEmpty)
/*     */         {
/* 483 */           initValueChoice();
/*     */         }
/*     */         else
/*     */         {
/* 487 */           this.m_valueChoice.init(values);
/* 488 */           this.m_hasValueChoices = true;
/*     */         }
/*     */ 
/* 491 */         updateMappedValue();
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 496 */       super.actionPerformed(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void sortValues(Vector values)
/*     */   {
/* 502 */     if (values == null)
/*     */     {
/* 504 */       return;
/*     */     }
/*     */ 
/* 507 */     IdcComparator cmp = new IdcComparator()
/*     */     {
/*     */       public int compare(Object obj1, Object obj2)
/*     */       {
/* 511 */         String s1 = (String)obj1;
/* 512 */         String s2 = (String)obj2;
/*     */ 
/* 514 */         return s1.compareTo(s2);
/*     */       }
/*     */     };
/* 518 */     int num = values.size();
/* 519 */     Object[] s = new Object[num];
/*     */ 
/* 521 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 523 */       s[i] = values.elementAt(i);
/*     */     }
/*     */ 
/* 526 */     Sort.sort(s, 0, num - 1, cmp);
/*     */ 
/* 528 */     values.removeAllElements();
/* 529 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 531 */       if (((String)s[i]).trim().length() == 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 535 */       values.addElement(s[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean checkSelectedClauseDocField()
/*     */   {
/* 543 */     String selItem = this.m_fields.getSelectedInternalValue();
/* 544 */     return checkSelectedClauseDocField(selItem);
/*     */   }
/*     */ 
/*     */   protected boolean checkSelectedClauseDocField(String selItem)
/*     */   {
/* 549 */     if (selItem != null)
/*     */     {
/* 551 */       ViewFieldDef fieldDef = null;
/* 552 */       boolean foundIt = false;
/* 553 */       for (int i = 0; i < this.m_fieldDefs.size(); ++i)
/*     */       {
/* 555 */         fieldDef = (ViewFieldDef)this.m_fieldDefs.elementAt(i);
/*     */ 
/* 557 */         if (!fieldDef.m_name.equals(selItem))
/*     */           continue;
/* 559 */         foundIt = true;
/* 560 */         break;
/*     */       }
/*     */ 
/* 563 */       if (!foundIt)
/*     */       {
/* 565 */         reportError(null, LocaleResources.getString("apCannotFindField", this.m_cxt, selItem));
/*     */       }
/* 567 */       else if (foundIt)
/*     */       {
/* 569 */         addOptionListChoices(fieldDef);
/*     */       }
/*     */ 
/* 572 */       this.m_clauseEditPanel.validate();
/*     */     }
/* 574 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 579 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ValueMapHelper
 * JD-Core Version:    0.5.4
 */