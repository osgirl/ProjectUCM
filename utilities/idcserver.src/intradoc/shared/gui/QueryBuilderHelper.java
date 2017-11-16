/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.gui.iwt.IdcList;
/*     */ import intradoc.shared.ClausesData;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextArea;
/*     */ import javax.swing.JTextField;
/*     */ import javax.swing.ListSelectionModel;
/*     */ import javax.swing.event.ChangeEvent;
/*     */ import javax.swing.event.ChangeListener;
/*     */ import javax.swing.text.JTextComponent;
/*     */ 
/*     */ public class QueryBuilderHelper extends ClauseBuilderHelper
/*     */   implements ChangeListener
/*     */ {
/*     */   protected String[][] m_operatorCodes;
/*  73 */   protected DisplayChoice m_clauseFields = null;
/*  74 */   protected JTextField m_clauseValue = null;
/*  75 */   protected DisplayChoice m_operatorValues = null;
/*     */   protected JCheckBox m_isCustomComp;
/*     */   protected JTextComponent m_customQueryText;
/*  84 */   protected boolean m_useCustomQuery = false;
/*     */   protected String m_curClauseFieldType;
/*     */   protected boolean m_curClauseZoneField;
/*  90 */   protected SharedContext m_sharedContext = null;
/*  91 */   protected SchemaHelper m_schHelper = null;
/*     */ 
/*  94 */   protected String m_expressionLabel = "apQueryExpression";
/*  95 */   protected String m_customExpressionLabel = "apCustomQueryExpression";
/*     */ 
/*     */   public QueryBuilderHelper()
/*     */   {
/*  99 */     this.m_useCustomQuery = true;
/*     */   }
/*     */ 
/*     */   public void setQueryLabels(String expLabel, String customLabel)
/*     */   {
/* 104 */     this.m_expressionLabel = expLabel;
/* 105 */     this.m_customExpressionLabel = customLabel;
/*     */   }
/*     */ 
/*     */   public JPanel createStandardQueryPanel(ContainerHelper guiHelper, JPanel queryDefinitionPanel, SharedContext shContext)
/*     */   {
/* 111 */     this.m_sharedContext = shContext;
/* 112 */     this.m_cxt = guiHelper.m_exchange.m_sysInterface.getExecutionContext();
/* 113 */     JPanel pnl = createStandardClausePanel(guiHelper, queryDefinitionPanel, LocaleResources.getString(this.m_expressionLabel, this.m_cxt));
/*     */ 
/* 116 */     if (this.m_useCustomQuery)
/*     */     {
/* 118 */       JPanel customEditArea = new CustomPanel();
/* 119 */       guiHelper.m_gridHelper.reset();
/* 120 */       guiHelper.m_gridHelper.m_gc.weightx = 1.0D;
/* 121 */       guiHelper.m_gridHelper.m_gc.weighty = 1.0D;
/* 122 */       guiHelper.m_gridHelper.m_gc.fill = 1;
/* 123 */       guiHelper.addLastComponentInRow(queryDefinitionPanel, customEditArea);
/*     */ 
/* 126 */       customEditArea.setLayout(new BorderLayout());
/* 127 */       JCheckBox isCustomComp = new JCheckBox(LocaleResources.getString(this.m_customExpressionLabel, this.m_cxt));
/*     */ 
/* 129 */       isCustomComp.setBackground(queryDefinitionPanel.getBackground());
/* 130 */       customEditArea.add("North", isCustomComp);
/* 131 */       JPanel customEditControls = new PanePanel(false);
/* 132 */       customEditArea.add("Center", customEditControls);
/* 133 */       customEditControls.setLayout(new GridBagLayout());
/*     */ 
/* 135 */       JTextArea customQueryText = new CustomTextArea(3, 50);
/* 136 */       guiHelper.addComponent(customEditControls, customQueryText);
/*     */ 
/* 138 */       registerCustomComponents(customQueryText, isCustomComp);
/*     */     }
/* 140 */     return pnl;
/*     */   }
/*     */ 
/*     */   public void registerCustomComponents(JTextComponent customQueryText, JCheckBox isCustomComp)
/*     */   {
/* 145 */     if (!this.m_useCustomQuery)
/*     */       return;
/* 147 */     this.m_customQueryText = customQueryText;
/* 148 */     this.m_isCustomComp = isCustomComp;
/*     */ 
/* 150 */     this.m_clauseList.m_list.getSelectionModel().addListSelectionListener(this);
/*     */ 
/* 152 */     this.m_guiHelper.makePanelGridBag(this.m_clauseEditPanel, 2);
/*     */ 
/* 154 */     this.m_clauseValPanel = new PanePanel(false);
/* 155 */     this.m_guiHelper.makePanelGridBag(this.m_clauseValPanel, 2);
/*     */ 
/* 157 */     this.m_guiHelper.m_exchange.addComponent("CustomQuery", this.m_customQueryText, null);
/* 158 */     this.m_guiHelper.m_exchange.addComponent("IsCustom", this.m_isCustomComp, null);
/* 159 */     this.m_isCustomComp.addChangeListener(this);
/*     */   }
/*     */ 
/*     */   protected void setPanelData()
/*     */   {
/* 166 */     GridBagHelper gh = this.m_guiHelper.m_gridHelper;
/* 167 */     gh.reset();
/* 168 */     this.m_clauseEditPanel.removeAll();
/*     */ 
/* 170 */     this.m_guiHelper.m_exchange.removeComponent("Field");
/* 171 */     this.m_guiHelper.m_exchange.removeComponent("Operator");
/* 172 */     this.m_guiHelper.m_exchange.removeComponent("ValuePanel");
/* 173 */     this.m_guiHelper.m_exchange.removeComponent("ClauseValue");
/*     */ 
/* 175 */     this.m_clauseFields = new DisplayChoice();
/* 176 */     this.m_clauseFields.addItemListener(this);
/*     */ 
/* 178 */     this.m_operatorValues = new DisplayChoice();
/* 179 */     this.m_operatorValues.addItemListener(this);
/*     */ 
/* 181 */     gh.m_gc.anchor = 18;
/* 182 */     gh.m_gc.fill = 2;
/*     */ 
/* 184 */     this.m_guiHelper.addLabelFieldPair(this.m_clauseEditPanel, this.m_fieldTitle, this.m_clauseFields, "Field");
/* 185 */     this.m_guiHelper.addLabelFieldPair(this.m_clauseEditPanel, LocaleResources.getString("apLabelOperator", this.m_cxt), this.m_operatorValues, "Operator");
/*     */ 
/* 188 */     this.m_guiHelper.addLabelFieldPair(this.m_clauseEditPanel, this.m_valueTitle, this.m_clauseValPanel, "ValuePanel");
/*     */ 
/* 190 */     this.m_operatorCodes = this.m_clauseData.getOperatorCodes();
/* 191 */     LocaleResources.localizeStaticDoubleArray(this.m_operatorCodes, this.m_cxt, 1);
/*     */ 
/* 193 */     addFieldList(this.m_clauseFields);
/* 194 */     int opType = 0;
/* 195 */     if (this.m_fieldDefs != null)
/*     */     {
/* 197 */       int size = this.m_fieldDefs.size();
/* 198 */       if (size > 0)
/*     */       {
/* 200 */         ViewFieldDef fieldDef = (ViewFieldDef)this.m_fieldDefs.elementAt(0);
/* 201 */         this.m_curClauseFieldType = fieldDef.m_type;
/* 202 */         this.m_curClauseZoneField = fieldDef.m_isZoneField;
/* 203 */         opType = getOpType(fieldDef);
/*     */       }
/*     */     }
/*     */ 
/* 207 */     addOperatorChoices(opType);
/* 208 */     addOptionListChoices(null);
/* 209 */     this.m_clauseEditPanel.validate();
/*     */   }
/*     */ 
/*     */   public void saveCurrentSelection()
/*     */   {
/* 216 */     super.saveCurrentSelection();
/*     */ 
/* 218 */     String custQueryStr = this.m_guiHelper.m_exchange.getComponentValue("CustomQuery");
/* 219 */     this.m_clauseData.setQueryProp("CustomQuery", custQueryStr);
/*     */   }
/*     */ 
/*     */   public void addOperatorChoices(int opType)
/*     */   {
/* 227 */     GridBagHelper gh = this.m_guiHelper.m_gridHelper;
/* 228 */     gh.m_gc.fill = 2;
/* 229 */     gh.m_gc.weightx = 1.0D;
/*     */ 
/* 231 */     Object[] obj = this.m_guiHelper.m_exchange.findComponent("Operator", false);
/* 232 */     DisplayChoice opChoices = null;
/* 233 */     if (obj == null)
/*     */     {
/* 235 */       opChoices = new DisplayChoice();
/* 236 */       this.m_guiHelper.addComponent(this.m_clauseEditPanel, opChoices);
/*     */     }
/*     */     else
/*     */     {
/* 240 */       opChoices = (DisplayChoice)obj[1];
/*     */     }
/*     */ 
/* 243 */     short[] opRange = this.m_clauseData.comparisonIndexRange(opType);
/* 244 */     int numEntries = opRange[1] - opRange[0];
/* 245 */     String[][] choices = new String[numEntries][];
/* 246 */     int index = 0;
/* 247 */     for (int i = opRange[0]; i < opRange[1]; ++i)
/*     */     {
/* 249 */       choices[index] = this.m_operatorCodes[i];
/* 250 */       ++index;
/*     */     }
/* 252 */     opChoices.init(choices);
/*     */   }
/*     */ 
/*     */   public void addOptionListChoices(ViewFieldDef fieldDef)
/*     */   {
/* 257 */     this.m_clauseValPanel.removeAll();
/* 258 */     this.m_guiHelper.m_exchange.removeComponent("ClauseValue");
/*     */ 
/* 260 */     GridBagHelper gh = this.m_guiHelper.m_gridHelper;
/* 261 */     gh.m_gc.insets = new Insets(0, 0, 0, 0);
/* 262 */     gh.m_gc.anchor = 18;
/* 263 */     gh.m_gc.fill = 2;
/* 264 */     gh.m_gc.weightx = 1.0D;
/*     */ 
/* 266 */     String optKey = null;
/* 267 */     if (fieldDef != null)
/*     */     {
/* 269 */       optKey = fieldDef.m_optionListKey;
/*     */     }
/*     */ 
/* 272 */     Component comp = null;
/* 273 */     if ((fieldDef != null) && (fieldDef.m_hasView))
/*     */     {
/* 276 */       this.m_clauseValue = new JTextField(10);
/* 277 */       comp = this.m_clauseValue;
/* 278 */       gh.prepareAddRowElement();
/* 279 */       this.m_guiHelper.addExchangeComponent(this.m_clauseValPanel, comp, "ClauseValue");
/*     */ 
/* 281 */       JButton selButton = new JButton(LocaleResources.getString("apLabelSelectButton", this.m_cxt));
/*     */ 
/* 283 */       selButton.setActionCommand("select");
/* 284 */       selButton.addActionListener(this);
/*     */ 
/* 286 */       gh.m_gc.weightx = 0.1D;
/* 287 */       gh.m_gc.insets = new Insets(0, 20, 0, 0);
/* 288 */       this.m_guiHelper.addLastComponentInRow(this.m_clauseValPanel, selButton);
/*     */     }
/* 290 */     else if ((fieldDef != null) && (fieldDef.isComplexOptionList()))
/*     */     {
/* 292 */       if (this.m_schHelper == null)
/*     */       {
/* 294 */         this.m_schHelper = new SchemaHelper();
/* 295 */         this.m_schHelper.computeMaps();
/*     */       }
/*     */ 
/* 298 */       ViewChoice vChoice = new ViewChoice(this.m_sysInterface, this.m_sharedContext);
/* 299 */       String btnLabel = this.m_sysInterface.getString("apSelectBtnLabel");
/* 300 */       vChoice.setEnableMultiSelect(false);
/* 301 */       vChoice.init(this.m_schHelper, fieldDef, 10, btnLabel);
/* 302 */       vChoice.setFieldPrefix("#active.");
/* 303 */       this.m_guiHelper.addExchangeComponent(this.m_clauseValPanel, vChoice, "ClauseValue");
/*     */ 
/* 305 */       this.m_clauseValue = ((JTextField)vChoice.getComponent());
/*     */     }
/*     */     else
/*     */     {
/* 309 */       Component optList = getOptChoiceList(optKey);
/* 310 */       if ((optList == null) && (optKey != null) && (this.m_displayMaps != null))
/*     */       {
/* 312 */         String[][] optionMap = (String[][])(String[][])this.m_displayMaps.get(optKey);
/* 313 */         if (optionMap != null)
/*     */         {
/* 315 */           if ((fieldDef != null) && (fieldDef.isComboOptionList()))
/*     */           {
/* 317 */             ComboChoice dc = new ComboChoice();
/* 318 */             dc.initChoiceList(optionMap);
/* 319 */             optList = dc;
/*     */           }
/*     */           else
/*     */           {
/* 323 */             DisplayChoice dc = new DisplayChoice();
/* 324 */             dc.init(optionMap);
/* 325 */             optList = dc;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 330 */       comp = optList;
/* 331 */       if (comp == null)
/*     */       {
/* 333 */         comp = new JTextField(20);
/*     */       }
/* 335 */       this.m_guiHelper.addExchangeComponent(this.m_clauseValPanel, comp, "ClauseValue");
/*     */     }
/*     */ 
/* 338 */     this.m_curOptionListKey = optKey;
/* 339 */     this.m_clauseValPanel.validate();
/*     */   }
/*     */ 
/*     */   protected void refreshClauseList()
/*     */     throws ServiceException
/*     */   {
/* 347 */     Vector currentClause = this.m_curClause;
/*     */ 
/* 351 */     this.m_clauseList.removeAllItems();
/*     */ 
/* 354 */     Vector clauses = this.m_clauseData.m_clauses;
/* 355 */     int nclauses = clauses.size();
/*     */ 
/* 357 */     int curIndex = -1;
/*     */ 
/* 360 */     for (int i = 0; i < nclauses; ++i)
/*     */     {
/* 362 */       Vector elts = (Vector)clauses.elementAt(i);
/* 363 */       if (currentClause == elts)
/*     */       {
/* 365 */         curIndex = i;
/*     */       }
/* 367 */       Object[] args = new Object[2];
/* 368 */       String key = null;
/* 369 */       String op = "";
/* 370 */       for (int j = 0; j < 3; ++j)
/*     */       {
/* 372 */         String str = (String)elts.elementAt(j);
/* 373 */         if (j == 0)
/*     */         {
/* 375 */           String val = StringUtils.getPresentationString(this.m_nameCaptions, str);
/* 376 */           if (val == null)
/*     */           {
/* 380 */             args[0] = str;
/*     */           }
/*     */           else
/*     */           {
/* 384 */             args[0] = val;
/*     */           }
/*     */         }
/* 387 */         else if (j == 1)
/*     */         {
/* 390 */           op = this.m_clauseData.getOperatorString(str);
/* 391 */           if (op == null)
/*     */           {
/* 393 */             op = str;
/*     */           }
/* 395 */           key = "apQueryField_" + op;
/*     */         }
/* 397 */         if (j != 2)
/*     */           continue;
/* 399 */         if ((str == null) || (str.length() == 0))
/*     */         {
/* 401 */           str = "<empty>";
/*     */         }
/* 403 */         else if (str.trim().length() == 0)
/*     */         {
/* 405 */           str = "'" + str + "'";
/*     */         }
/* 407 */         args[1] = str;
/*     */       }
/*     */ 
/* 410 */       this.m_clauseList.add(LocaleResources.getString(key, this.m_cxt, args));
/*     */     }
/*     */ 
/* 413 */     if (curIndex >= 0)
/*     */     {
/* 415 */       this.m_curClause = currentClause;
/* 416 */       this.m_clauseList.select(curIndex);
/*     */     }
/*     */     else
/*     */     {
/* 420 */       this.m_curClause = null;
/*     */     }
/*     */ 
/* 424 */     this.m_clauseData.m_dispStr = LocaleResources.getString("apStandardQueryDisplayString", this.m_cxt);
/*     */ 
/* 428 */     String custQuery = this.m_clauseData.createQueryString();
/* 429 */     this.m_guiHelper.m_exchange.setComponentValue("CustomQuery", custQuery);
/* 430 */     setQueryProp("CustomQuery", custQuery);
/*     */ 
/* 433 */     enableDisable(false);
/*     */   }
/*     */ 
/*     */   protected boolean checkSelectedClauseDocField()
/*     */   {
/* 440 */     String selItem = this.m_clauseFields.getSelectedInternalValue();
/* 441 */     boolean typeChanged = false;
/*     */ 
/* 443 */     if (selItem != null)
/*     */     {
/* 445 */       ViewFieldDef fieldDef = null;
/* 446 */       boolean foundIt = false;
/* 447 */       for (int i = 0; i < this.m_fieldDefs.size(); ++i)
/*     */       {
/* 449 */         fieldDef = (ViewFieldDef)this.m_fieldDefs.elementAt(i);
/* 450 */         if (!fieldDef.m_name.equals(selItem))
/*     */           continue;
/* 452 */         foundIt = true;
/* 453 */         break;
/*     */       }
/*     */ 
/* 457 */       String type = "text";
/* 458 */       if (!foundIt)
/*     */       {
/* 460 */         String msg = LocaleUtils.encodeMessage("apCouldNotFindField", null, selItem);
/*     */ 
/* 462 */         reportError(null, msg);
/*     */       }
/*     */       else
/*     */       {
/* 466 */         type = fieldDef.m_type;
/*     */       }
/*     */ 
/* 469 */       if ((!type.equalsIgnoreCase(this.m_curClauseFieldType)) || (this.m_curClauseZoneField != fieldDef.m_isZoneField))
/*     */       {
/* 472 */         this.m_curClauseFieldType = type;
/* 473 */         this.m_curClauseZoneField = fieldDef.m_isZoneField;
/* 474 */         short opType = getOpType(fieldDef);
/* 475 */         addOperatorChoices(opType);
/* 476 */         typeChanged = true;
/*     */       }
/*     */ 
/* 479 */       if ((foundIt) && (!typeChanged))
/*     */       {
/* 481 */         if ((this.m_curOptionListKey != null) && (fieldDef.m_optionListKey != null))
/*     */         {
/* 483 */           if (!this.m_curOptionListKey.equals(fieldDef.m_optionListKey))
/*     */           {
/* 485 */             typeChanged = true;
/*     */           }
/*     */ 
/*     */         }
/*     */         else {
/* 490 */           typeChanged = this.m_curOptionListKey != fieldDef.m_optionListKey;
/*     */         }
/*     */       }
/*     */ 
/* 494 */       if (foundIt)
/*     */       {
/* 496 */         addOptionListChoices(fieldDef);
/*     */       }
/* 498 */       this.m_clauseEditPanel.validate();
/*     */     }
/* 500 */     return typeChanged;
/*     */   }
/*     */ 
/*     */   protected short getOpType(ViewFieldDef def)
/*     */   {
/* 506 */     if (def != null)
/*     */     {
/* 508 */       if (def.m_isZoneField)
/*     */       {
/* 510 */         return 4;
/*     */       }
/*     */ 
/* 513 */       String type = def.m_type;
/* 514 */       if (type.equalsIgnoreCase("Date"))
/*     */       {
/* 516 */         return 1;
/*     */       }
/* 518 */       if (type.equalsIgnoreCase("Yes/No"))
/*     */       {
/* 520 */         return 2;
/*     */       }
/* 522 */       if ((type.equalsIgnoreCase("Number")) || (type.equalsIgnoreCase("Int")))
/*     */       {
/* 525 */         return 3;
/*     */       }
/*     */     }
/* 528 */     return 0;
/*     */   }
/*     */ 
/*     */   public void enableDisable(boolean loadSelection)
/*     */   {
/* 535 */     super.enableDisable(loadSelection);
/*     */ 
/* 537 */     if (!this.m_useCustomQuery)
/*     */       return;
/* 539 */     boolean isCustom = this.m_clauseData.m_isCustom;
/* 540 */     this.m_customQueryText.setEnabled(isCustom);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 551 */     String name = exchange.m_compName;
/*     */ 
/* 554 */     if ((name.equals("Operator")) && (updateComponent))
/*     */     {
/* 556 */       checkSelectedClauseDocField();
/*     */     }
/*     */ 
/* 559 */     int index = -1;
/* 560 */     if (name.equals("Field"))
/*     */     {
/* 562 */       index = 0;
/*     */     }
/* 564 */     else if (name.equals("Operator"))
/*     */     {
/* 566 */       index = 1;
/*     */     }
/* 568 */     else if (name.equals("ClauseValue"))
/*     */     {
/* 570 */       index = 2;
/*     */     }
/*     */ 
/* 573 */     if (index >= 0)
/*     */     {
/* 575 */       if (updateComponent)
/*     */       {
/* 577 */         exchange.m_compValue = "";
/*     */       }
/*     */ 
/* 580 */       if (this.m_curClause == null)
/*     */         return;
/*     */       String str;
/*     */       String str;
/* 583 */       if (updateComponent)
/*     */       {
/* 585 */         str = (String)this.m_curClause.elementAt(index);
/*     */       }
/*     */       else
/*     */       {
/* 589 */         str = exchange.m_compValue;
/*     */       }
/*     */ 
/* 592 */       if ((name.equals("ClauseValue")) && (this.m_curClauseFieldType.equalsIgnoreCase("date")))
/*     */       {
/* 595 */         str = handleDateValue(str, updateComponent);
/*     */       }
/*     */ 
/* 598 */       if (updateComponent)
/*     */       {
/* 600 */         if (index == 1)
/*     */         {
/* 602 */           str = this.m_clauseData.findOperatorFromAlias(str);
/*     */         }
/* 604 */         exchange.m_compValue = str;
/*     */       }
/*     */       else
/*     */       {
/* 608 */         this.m_curClause.setElementAt(str, index);
/*     */       }
/*     */ 
/*     */     }
/* 614 */     else if (updateComponent)
/*     */     {
/* 616 */       exchange.m_compValue = getQueryProp(name);
/*     */     }
/*     */     else
/*     */     {
/* 620 */       setQueryProp(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String handleDateValue(String str, boolean updateComponent)
/*     */   {
/* 627 */     if (updateComponent)
/*     */     {
/* 629 */       if (str.indexOf("<$") < 0)
/*     */       {
/* 631 */         str = LocaleResources.localizeDate(str, this.m_cxt);
/*     */       }
/*     */ 
/*     */     }
/* 636 */     else if (str.indexOf("<$") < 0)
/*     */     {
/*     */       try
/*     */       {
/* 640 */         Date d = LocaleResources.parseDate(str, this.m_cxt);
/* 641 */         str = LocaleResources.m_odbcFormat.format(d);
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 647 */         str = "(NaD)";
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 652 */     return str;
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 658 */     String name = exchange.m_compName;
/* 659 */     String val = exchange.m_compValue;
/*     */ 
/* 661 */     if (val != null)
/*     */     {
/* 663 */       val = val.trim();
/* 664 */       if (val.length() == 0)
/*     */       {
/* 666 */         val = null;
/*     */       }
/*     */     }
/*     */ 
/* 670 */     if (name.equals("ClauseValue"))
/*     */     {
/* 672 */       if (val == null)
/*     */       {
/* 674 */         String op = exchange.getComponentValue("Operator");
/* 675 */         if ((!op.equals("sqlEq")) && (!op.equals("sqlNeq")) && (!op.equals("matches")))
/*     */         {
/* 677 */           exchange.m_errorMessage = IdcMessageFactory.lc("apNonEmptyConditionRequired", new Object[0]);
/* 678 */           return false;
/*     */         }
/* 680 */         val = "";
/*     */       }
/* 682 */       if (this.m_curClauseFieldType.equalsIgnoreCase("date"))
/*     */       {
/*     */         try
/*     */         {
/* 686 */           if (val.indexOf("<$") < 0)
/*     */           {
/* 689 */             LocaleResources.parseDate(val, this.m_cxt);
/*     */           }
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 694 */           exchange.m_errorMessage = IdcMessageFactory.lc("apDateNotUnderstood", new Object[0]);
/* 695 */           return false;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 700 */     return true;
/*     */   }
/*     */ 
/*     */   public void stateChanged(ChangeEvent e)
/*     */   {
/* 708 */     Object target = e.getSource();
/*     */ 
/* 710 */     if (target != this.m_isCustomComp)
/*     */       return;
/* 712 */     String isCustomVal = this.m_guiHelper.m_exchange.getComponentValue("IsCustom");
/* 713 */     this.m_clauseData.m_isCustom = isCustomVal.equals("1");
/* 714 */     if (!this.m_clauseData.m_isCustom)
/*     */     {
/*     */       try
/*     */       {
/* 718 */         this.m_guiHelper.m_exchange.setComponentValue("CustomQuery", this.m_clauseData.createQueryString());
/*     */       }
/*     */       catch (ServiceException ignore)
/*     */       {
/* 723 */         if (SystemUtils.m_verbose)
/*     */         {
/* 725 */           Report.debug("system", null, ignore);
/*     */         }
/*     */       }
/*     */     }
/* 729 */     enableDisable(false);
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 739 */     Object target = e.getSource();
/* 740 */     if (target != this.m_clauseFields)
/*     */       return;
/* 742 */     checkSelectedClauseDocField();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 752 */     String cmd = e.getActionCommand();
/* 753 */     if (cmd.equals("select"))
/*     */     {
/* 755 */       ViewData viewData = null;
/* 756 */       String key = null;
/* 757 */       String title = null;
/* 758 */       String helpPage = null;
/* 759 */       if (this.m_curOptionListKey.equals("docView"))
/*     */       {
/* 761 */         viewData = new ViewData(1);
/* 762 */         viewData.m_viewName = "DocSelectView";
/* 763 */         key = "dDocName";
/* 764 */         title = LocaleResources.getString("apContentItemView", this.m_cxt);
/* 765 */         helpPage = "SelectDocument";
/*     */       }
/* 767 */       else if (this.m_curOptionListKey.equals("userView"))
/*     */       {
/* 769 */         viewData = new ViewData(2);
/* 770 */         viewData.m_viewName = "UserSelectView";
/* 771 */         key = "dName";
/* 772 */         title = LocaleResources.getString("apUserView", this.m_cxt);
/* 773 */         helpPage = "SelectUser";
/*     */       }
/* 775 */       else if (this.m_curOptionListKey.equals("tableView"))
/*     */       {
/* 778 */         viewData = new ViewData(3, "", "users");
/* 779 */         viewData.m_useInDate = false;
/* 780 */         title = LocaleResources.getString("apTableView", this.m_cxt);
/*     */       }
/*     */ 
/* 783 */       viewData.m_isViewOnly = false;
/* 784 */       ViewDlg viewDlg = new ViewDlg(this.m_sysInterface.getMainWindow(), this.m_sysInterface, title, this.m_sharedContext, DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 787 */       viewDlg.init(viewData, null);
/* 788 */       if (viewDlg.prompt() == 1)
/*     */       {
/* 790 */         Vector v = viewDlg.computeSelectedValues(key, false);
/* 791 */         if (v.size() > 0)
/*     */         {
/* 793 */           String value = (String)v.elementAt(0);
/* 794 */           this.m_clauseValue.setText(value);
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 800 */       super.actionPerformed(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 806 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80447 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.QueryBuilderHelper
 * JD-Core Version:    0.5.4
 */