/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.ClausesData;
/*     */ import intradoc.shared.RuleClausesData;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.gui.QueryBuilderHelper;
/*     */ import intradoc.shared.gui.ViewChoice;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class RuleBuilderHelper extends QueryBuilderHelper
/*     */ {
/*  61 */   protected String m_title = null;
/*  62 */   protected ActionListener m_listener = null;
/*  63 */   protected String m_scriptType = null;
/*  64 */   protected SharedContext m_shContext = null;
/*     */ 
/*  66 */   protected boolean m_hasValue = false;
/*  67 */   protected ViewFieldDef m_valueFieldDef = null;
/*  68 */   protected Component m_valueField = null;
/*  69 */   protected String m_valueLabel = null;
/*  70 */   protected String m_valueName = null;
/*  71 */   protected JButton m_valueBtn = null;
/*     */ 
/*  74 */   protected Hashtable m_boxMap = null;
/*  75 */   protected Hashtable m_controlMap = null;
/*  76 */   protected String[][] CHECKBOX_INFO = { { "useEvents", "dpEvent", "apDpUseEvent", "1" }, { "useAction", "dpAction", "apDpUseAction", "1" }, { "flags", "dpFlag", "apDpFlags", "0" } };
/*     */ 
/*  83 */   protected String[][] DOCPROFILE_FLAGS = { { "IsWorkflow_1", "apDpIsWorkflow" }, { "IsWorkflow_0", "apDpIsNotWorkflow" } };
/*     */ 
/*     */   public RuleBuilderHelper(String label, String name, String clauseTitle, boolean hasValue, ActionListener listener)
/*     */   {
/*  91 */     this.m_valueLabel = label;
/*  92 */     this.m_valueName = name;
/*  93 */     this.m_title = clauseTitle;
/*  94 */     this.m_listener = listener;
/*  95 */     this.m_hasValue = hasValue;
/*  96 */     this.m_useCustomQuery = false;
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys, ViewFieldDef fieldDef, SharedContext shContext)
/*     */   {
/* 101 */     super.init(sys);
/* 102 */     this.m_valueFieldDef = fieldDef;
/* 103 */     this.m_shContext = shContext;
/*     */   }
/*     */ 
/*     */   public JPanel createStandardRuleClausePanel(ContainerHelper guiHelper, JPanel queryDefinitionPanel, SharedContext shContext, String scriptType)
/*     */   {
/* 109 */     this.m_cxt = guiHelper.m_exchange.m_sysInterface.getExecutionContext();
/* 110 */     this.m_sharedContext = shContext;
/* 111 */     this.m_scriptType = scriptType;
/* 112 */     this.m_boxMap = new Hashtable();
/* 113 */     this.m_controlMap = new Hashtable();
/*     */ 
/* 115 */     JPanel pnl = null;
/* 116 */     if ((scriptType != null) && (scriptType.equals("activation")))
/*     */     {
/* 118 */       pnl = new PanePanel();
/* 119 */       guiHelper.m_gridHelper.useGridBag(pnl);
/* 120 */       createStandardClausePanel(guiHelper, pnl);
/* 121 */       JPanel exPanel = createExtraPanel();
/*     */ 
/* 123 */       TabPanel tabs = new TabPanel();
/* 124 */       tabs.addPane(LocaleResources.getString("apDpGeneralScriptConfig", this.m_cxt), exPanel);
/* 125 */       tabs.addPane(LocaleResources.getString("apDpClauseScript", this.m_cxt), pnl);
/*     */ 
/* 127 */       this.m_guiHelper.addComponent(queryDefinitionPanel, tabs);
/*     */     }
/*     */     else
/*     */     {
/* 131 */       pnl = createStandardClausePanel(guiHelper, queryDefinitionPanel);
/*     */     }
/* 133 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected JPanel createStandardClausePanel(ContainerHelper guiHelper, JPanel queryDefinitionPanel)
/*     */   {
/* 138 */     JPanel pnl = createStandardClausePanel(guiHelper, queryDefinitionPanel, LocaleResources.getString(this.m_title, this.m_cxt));
/*     */ 
/* 141 */     if (this.m_hasValue)
/*     */     {
/* 145 */       boolean hasButton = this.m_listener != null;
/*     */ 
/* 147 */       Component comp = null;
/* 148 */       if (this.m_valueFieldDef.m_isOptionList)
/*     */       {
/* 150 */         if (this.m_valueFieldDef.isComplexOptionList())
/*     */         {
/* 153 */           ViewChoice vChoice = new ViewChoice(this.m_sysInterface, this.m_shContext);
/* 154 */           comp = vChoice;
/*     */ 
/* 156 */           if (this.m_schHelper == null)
/*     */           {
/* 158 */             this.m_schHelper = new SchemaHelper();
/* 159 */             this.m_schHelper.computeMaps();
/*     */           }
/*     */ 
/* 162 */           String btnLabel = this.m_sysInterface.getString("apSelectBtnLabel");
/* 163 */           vChoice.init(this.m_schHelper, this.m_valueFieldDef, 30, btnLabel);
/*     */         }
/*     */         else
/*     */         {
/* 167 */           ComboChoice choiceList = new ComboChoice(30, this.m_valueFieldDef.isMultiOptionList());
/* 168 */           comp = choiceList;
/* 169 */           Vector options = SharedObjects.getOptList(this.m_valueFieldDef.m_optionListKey);
/* 170 */           if (options != null)
/*     */           {
/* 172 */             choiceList.initChoiceList(options);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 178 */         comp = new CustomTextField();
/*     */       }
/* 180 */       this.m_valueField = comp;
/*     */ 
/* 182 */       GridBagHelper gh = this.m_guiHelper.m_gridHelper;
/* 183 */       gh.m_gc.anchor = 18;
/* 184 */       JPanel panel = new PanePanel();
/* 185 */       this.m_guiHelper.makePanelGridBag(panel, 2);
/*     */ 
/* 187 */       this.m_guiHelper.addLabelFieldPairEx(panel, this.m_sysInterface.localizeCaption(this.m_valueLabel), this.m_valueField, this.m_valueName, !hasButton);
/*     */ 
/* 190 */       if (hasButton)
/*     */       {
/* 192 */         this.m_valueBtn = new JButton(LocaleResources.getString("apDpDlgButtonComputeValue", this.m_cxt));
/* 193 */         this.m_valueBtn.addActionListener(this.m_listener);
/* 194 */         this.m_valueBtn.setActionCommand("selectValue");
/* 195 */         gh.m_gc.weightx = 0.1D;
/* 196 */         gh.m_gc.insets = new Insets(0, 20, 0, 0);
/* 197 */         this.m_guiHelper.addLastComponentInRow(panel, this.m_valueBtn);
/*     */       }
/*     */ 
/* 200 */       gh.m_gc.insets = new Insets(20, 5, 0, 5);
/* 201 */       this.m_guiHelper.addLastComponentInRow(pnl, panel);
/*     */     }
/* 203 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected JPanel createExtraPanel()
/*     */   {
/* 208 */     JPanel pnl = new PanePanel();
/* 209 */     this.m_guiHelper.makePanelGridBag(pnl, 1);
/* 210 */     GridBagConstraints gc = this.m_guiHelper.m_gridHelper.m_gc;
/*     */ 
/* 212 */     Insets insets = gc.insets;
/* 213 */     Insets tabInsets = new Insets(insets.top, insets.left + 15, insets.bottom, insets.right);
/*     */ 
/* 215 */     for (int i = 0; i < this.CHECKBOX_INFO.length; ++i)
/*     */     {
/* 217 */       String name = this.CHECKBOX_INFO[i][0];
/* 218 */       String prefix = this.CHECKBOX_INFO[i][1] + "_";
/*     */ 
/* 220 */       this.m_guiHelper.m_gridHelper.prepareAddLastRowElement(18);
/* 221 */       gc.insets = insets;
/*     */ 
/* 223 */       boolean isBox = StringUtils.convertToBool(this.CHECKBOX_INFO[i][3], false);
/* 224 */       if (isBox)
/*     */       {
/* 226 */         CustomCheckbox chkBox = new CustomCheckbox(this.m_sysInterface.getString(this.CHECKBOX_INFO[i][2]));
/* 227 */         this.m_guiHelper.addExchangeComponent(pnl, chkBox, name);
/* 228 */         chkBox.addItemListener(this);
/* 229 */         this.m_boxMap.put(name, chkBox);
/*     */       }
/*     */       else
/*     */       {
/* 233 */         Component cmp = new CustomLabel(this.m_sysInterface.getString(this.CHECKBOX_INFO[i][2]), 1);
/*     */ 
/* 235 */         this.m_guiHelper.addComponent(pnl, cmp);
/*     */       }
/*     */ 
/* 238 */       String[][] options = (String[][])null;
/* 239 */       if (i == 0)
/*     */       {
/* 241 */         options = TableFields.DOCPROFILE_EVENTS;
/*     */       }
/* 243 */       else if (i == 1)
/*     */       {
/* 245 */         options = TableFields.DOCPROFILE_ACTIONS;
/*     */       }
/* 247 */       else if (i == 2)
/*     */       {
/* 249 */         options = this.DOCPROFILE_FLAGS;
/*     */       }
/* 251 */       int len = options.length;
/* 252 */       Vector cntrls = new IdcVector();
/* 253 */       for (int j = 0; j < len; ++j)
/*     */       {
/* 255 */         JCheckBox box = new CustomCheckbox(this.m_sysInterface.getString(options[j][1]), 0);
/*     */ 
/* 257 */         gc.insets = tabInsets;
/* 258 */         if (((j > 0) && (j % 2 == 1)) || (j == len - 1))
/*     */         {
/* 260 */           this.m_guiHelper.m_gridHelper.prepareAddLastRowElement(13);
/*     */         }
/*     */         else
/*     */         {
/* 264 */           this.m_guiHelper.m_gridHelper.prepareAddRowElement(13);
/*     */         }
/* 266 */         this.m_guiHelper.addExchangeComponent(pnl, box, prefix + options[j][0]);
/* 267 */         cntrls.addElement(box);
/*     */       }
/* 269 */       this.m_controlMap.put(name, cntrls);
/*     */     }
/*     */ 
/* 272 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void setPanelData()
/*     */   {
/* 278 */     super.setPanelData();
/*     */ 
/* 280 */     if (!this.m_scriptType.equals("activation"))
/*     */       return;
/* 282 */     for (int i = 0; i < this.CHECKBOX_INFO.length - 1; ++i)
/*     */     {
/* 284 */       String name = this.CHECKBOX_INFO[i][1];
/* 285 */       String str = this.m_clauseData.getQueryProp(name);
/* 286 */       Vector values = StringUtils.parseArray(str, ',', '^');
/* 287 */       int size = values.size();
/* 288 */       if (size <= 0)
/*     */         continue;
/* 290 */       String prefix = name + "_";
/* 291 */       this.m_clauseData.setQueryProp(this.CHECKBOX_INFO[i][0], "1");
/* 292 */       for (int j = 0; j < size; ++j)
/*     */       {
/* 294 */         String key = (String)values.elementAt(j);
/* 295 */         this.m_clauseData.setQueryProp(prefix + key, "1");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 301 */     String str = this.m_clauseData.getQueryProp("dpFlag");
/* 302 */     Vector flags = StringUtils.parseArray(str, ',', '^');
/* 303 */     int size = flags.size();
/* 304 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 306 */       Vector v = StringUtils.parseArray((String)flags.elementAt(i), ':', '*');
/* 307 */       this.m_clauseData.setQueryProp("dpFlag_" + v.elementAt(0) + "_" + (String)v.elementAt(1), "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean exchangeQueryInfo(boolean updateComponents)
/*     */   {
/* 316 */     boolean result = this.m_guiHelper.m_exchange.exchange(this, updateComponents);
/* 317 */     if ((result) && (!updateComponents) && (this.m_scriptType.equals("activation")))
/*     */     {
/* 319 */       Properties props = ((RuleClausesData)this.m_clauseData).getRuleProps();
/* 320 */       Vector[] values = new IdcVector[3];
/* 321 */       for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*     */       {
/* 323 */         String key = (String)en.nextElement();
/* 324 */         for (int i = 0; i < this.CHECKBOX_INFO.length; ++i)
/*     */         {
/* 326 */           String name = this.CHECKBOX_INFO[i][1];
/* 327 */           String prefix = name + "_";
/* 328 */           if (!key.startsWith(prefix))
/*     */             continue;
/* 330 */           String val = props.getProperty(key);
/* 331 */           if (!StringUtils.convertToBool(val, false))
/*     */             continue;
/* 333 */           key = key.substring(prefix.length());
/* 334 */           if (values[i] == null)
/*     */           {
/* 336 */             values[i] = new IdcVector();
/*     */           }
/* 338 */           values[i].addElement(key);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 344 */       for (int i = 0; i < this.CHECKBOX_INFO.length - 1; ++i)
/*     */       {
/* 346 */         String str = "";
/* 347 */         boolean inUse = StringUtils.convertToBool(props.getProperty(this.CHECKBOX_INFO[i][0]), false);
/*     */ 
/* 349 */         if ((inUse) && (values[i] != null))
/*     */         {
/* 351 */           str = StringUtils.createString(values[i], ',', '^');
/*     */         }
/* 353 */         props.put(this.CHECKBOX_INFO[i][1], str);
/*     */       }
/*     */ 
/* 358 */       Vector flags = values[2];
/* 359 */       String str = "";
/* 360 */       if (flags != null)
/*     */       {
/* 362 */         int size = flags.size();
/* 363 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 365 */           String val = (String)flags.elementAt(i);
/* 366 */           int index = val.indexOf("_");
/* 367 */           String key = val.substring(0, index);
/* 368 */           val = val.substring(index + 1);
/*     */ 
/* 370 */           if (str.length() > 0)
/*     */           {
/* 372 */             str = str + ",";
/*     */           }
/* 374 */           str = str + key + ":" + val;
/*     */         }
/*     */       }
/* 377 */       props.put("dpFlag", str);
/*     */     }
/* 379 */     return result;
/*     */   }
/*     */ 
/*     */   public String handleDateValue(String str, boolean updateComponent)
/*     */   {
/* 385 */     if (str.indexOf("(") >= 0)
/*     */     {
/* 387 */       return str;
/*     */     }
/* 389 */     return super.handleDateValue(str, updateComponent);
/*     */   }
/*     */ 
/*     */   public void enableDisable(boolean loadSelection)
/*     */   {
/* 395 */     super.enableDisable(loadSelection);
/* 396 */     boolean isCustom = this.m_clauseData.m_isCustom;
/* 397 */     if (this.m_hasValue)
/*     */     {
/* 399 */       this.m_valueField.setEnabled(!isCustom);
/*     */ 
/* 401 */       if (this.m_valueBtn != null)
/*     */       {
/* 403 */         this.m_valueBtn.setEnabled(!isCustom);
/*     */       }
/*     */     }
/*     */ 
/* 407 */     for (Enumeration en = this.m_controlMap.keys(); en.hasMoreElements(); )
/*     */     {
/* 409 */       String name = (String)en.nextElement();
/* 410 */       JCheckBox box = (JCheckBox)this.m_boxMap.get(name);
/*     */ 
/* 412 */       boolean isBox = false;
/* 413 */       boolean isBoxEnabled = false;
/* 414 */       if (box != null)
/*     */       {
/* 416 */         isBoxEnabled = box.isSelected();
/* 417 */         box.setEnabled(!isCustom);
/* 418 */         isBox = true;
/*     */       }
/*     */ 
/* 421 */       boolean isCntrlEnabled = false;
/* 422 */       if ((!isCustom) && ((
/* 424 */         (!isBox) || ((isBox) && (isBoxEnabled)))))
/*     */       {
/* 426 */         isCntrlEnabled = true;
/*     */       }
/*     */ 
/* 429 */       Vector controls = (Vector)this.m_controlMap.get(name);
/* 430 */       int size = controls.size();
/* 431 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 433 */         Component cmp = (Component)controls.elementAt(i);
/* 434 */         cmp.setEnabled(isCntrlEnabled);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateValueField(String val)
/*     */   {
/* 441 */     this.m_guiHelper.m_exchange.setComponentValue(this.m_valueName, val);
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 448 */     Object target = e.getSource();
/*     */ 
/* 450 */     boolean isHandled = false;
/*     */     Enumeration en;
/* 451 */     if (target instanceof JCheckBox)
/*     */     {
/* 453 */       for (en = this.m_boxMap.keys(); en.hasMoreElements(); )
/*     */       {
/* 455 */         String name = (String)en.nextElement();
/* 456 */         JCheckBox box = (JCheckBox)this.m_boxMap.get(name);
/* 457 */         if (box == target)
/*     */         {
/* 459 */           boolean isEnabled = box.isSelected();
/* 460 */           Vector cmps = (Vector)this.m_controlMap.get(name);
/* 461 */           int size = cmps.size();
/* 462 */           for (int i = 0; i < size; ++i)
/*     */           {
/* 464 */             Component cmp = (Component)cmps.elementAt(i);
/* 465 */             cmp.setEnabled(isEnabled);
/*     */           }
/* 467 */           isHandled = true;
/* 468 */           break;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 473 */     if (isHandled)
/*     */       return;
/* 475 */     super.itemStateChanged(e);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 485 */     String val = exchange.m_compValue;
/*     */ 
/* 487 */     if (val != null)
/*     */     {
/* 489 */       val = val.trim();
/* 490 */       if (val.length() == 0)
/*     */       {
/* 492 */         val = null;
/*     */       }
/*     */     }
/* 495 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 501 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.RuleBuilderHelper
 * JD-Core Version:    0.5.4
 */