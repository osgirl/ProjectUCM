/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocProfileScriptUtils;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditFieldRuleDlg
/*     */   implements ActionListener, ItemListener
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_context;
/*     */   protected SharedContext m_shContext;
/*     */   protected String m_helpPage;
/*  71 */   protected DataBinder m_binder = null;
/*     */ 
/*  73 */   protected ViewFieldDef m_fieldDef = null;
/*     */ 
/*  76 */   protected Hashtable m_controlledMap = null;
/*  77 */   protected final String[][] UI_INFO_MAP = { { "default", "editDefault", "apDprUseDefaultValueLabel", "dprFieldHasDefault", "dprFieldDefaultScriptSummary", "DprEditFieldDefaultValue", "apDprEditDefaultValueTitle" }, { "derived", "editDerived", "apDprIsDerivedFieldLabel", "dprFieldIsDerived", "dprFieldDerivedScriptSummary", "DprEditFieldDerivedValue", "apDprEditDerivedValueTitle" }, { "restricted", "editRestricted", "apDprHasRestrictedListLabel", "dprFieldIsRestricted", "dprFieldRestrictedListSummary", "DprEditFieldRestrictedList", "apDprEditRestrictedListTitle" } };
/*     */ 
/*     */   public EditFieldRuleDlg(SystemInterface sys, String title, SharedContext shContext, String helpPage)
/*     */   {
/*  92 */     this.m_systemInterface = sys;
/*  93 */     this.m_context = sys.getExecutionContext();
/*  94 */     this.m_shContext = shContext;
/*     */ 
/*  96 */     title = LocaleResources.localizeMessage(title, this.m_context);
/*  97 */     this.m_helper = new DialogHelper(sys, title, true);
/*  98 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(DataBinder binder, ViewFieldDef fieldDef, boolean isNew)
/*     */   {
/* 103 */     this.m_helper.m_props = binder.getLocalData();
/* 104 */     this.m_binder = binder;
/* 105 */     this.m_controlledMap = new Hashtable();
/* 106 */     this.m_fieldDef = fieldDef;
/*     */ 
/* 108 */     initUI(isNew);
/* 109 */     loadComponents();
/* 110 */     enableDisable();
/* 111 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI(boolean isNew)
/*     */   {
/* 117 */     CustomPanel typePanel = new CustomPanel();
/* 118 */     this.m_helper.makePanelGridBag(typePanel, 0);
/*     */ 
/* 120 */     DisplayChoice choice = new DisplayChoice();
/* 121 */     choice.init(TableFields.DOCPROFILERULEFIELD_TYPES_OPTIONLIST);
/* 122 */     String label = this.m_systemInterface.localizeCaption("apDpRuleFieldTypeLabel");
/* 123 */     this.m_helper.addLabelFieldPair(typePanel, label, choice, "dpRuleFieldType");
/*     */ 
/* 125 */     CustomTextField reqText = new CustomTextField(30);
/* 126 */     this.m_helper.addLabelFieldPair(typePanel, this.m_systemInterface.localizeCaption("apDpRuleFieldRequiredMsgLabel"), reqText, "dprFieldRequiredMsg");
/*     */ 
/* 130 */     String[] ctrlDef = { "apDpRuleFieldCustomCaptionLabel", "dprFieldUseCustomCaption", "dprFieldCaption" };
/*     */ 
/* 134 */     addBoxTextControls(typePanel, ctrlDef, false, (String[][])null);
/*     */ 
/* 136 */     String[][] opts = DocProfileScriptUtils.createDisplayIncludeOptions("*place*", this.m_context);
/* 137 */     boolean isCombo = opts != null;
/* 138 */     ctrlDef = new String[] { "apDpRuleFieldCustomIncludeLabel", "dprFieldUseCustomInclude", "dprFieldInclude" };
/*     */ 
/* 142 */     addBoxTextControls(typePanel, ctrlDef, isCombo, opts);
/*     */ 
/* 144 */     JCheckBox countBox = new CustomCheckbox(this.m_systemInterface.getString("apDpExcludeFromGroupFieldCount"));
/* 145 */     this.m_helper.addExchangeComponent(typePanel, countBox, "dprFieldExcludeFromGroupCount");
/*     */ 
/* 148 */     CustomPanel panel = new CustomPanel();
/* 149 */     this.m_helper.makePanelGridBag(panel, 1);
/*     */ 
/* 152 */     for (int i = 0; i < this.UI_INFO_MAP.length; ++i)
/*     */     {
/* 154 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/* 155 */       this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 156 */       CustomCheckbox box = new CustomCheckbox(this.m_systemInterface.getString(this.UI_INFO_MAP[i][2]));
/* 157 */       this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 158 */       this.m_helper.addExchangeComponent(panel, box, this.UI_INFO_MAP[i][3]);
/* 159 */       box.addItemListener(this);
/*     */ 
/* 162 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 163 */       this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 164 */       this.m_helper.addComponent(panel, new CustomLabel());
/*     */ 
/* 167 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 25, 2, 5);
/* 168 */       this.m_helper.m_gridHelper.prepareAddRowElement();
/* 169 */       this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 170 */       this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 171 */       this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 172 */       CustomTextArea area = new CustomTextArea(3, 40);
/* 173 */       this.m_helper.addExchangeComponent(panel, area, this.UI_INFO_MAP[i][4]);
/* 174 */       area.setEditable(false);
/*     */ 
/* 176 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 0, 2, 0);
/* 177 */       JButton btn = new JButton(this.m_systemInterface.getString("apDlgButtonEdit"));
/* 178 */       btn.setActionCommand(this.UI_INFO_MAP[i][1]);
/* 179 */       btn.addActionListener(this);
/* 180 */       this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 181 */       this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 182 */       this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 183 */       this.m_helper.addLastComponentInRow(panel, btn);
/*     */ 
/* 186 */       Component[] cntrls = new Component[2];
/* 187 */       cntrls[0] = box;
/* 188 */       cntrls[1] = btn;
/*     */ 
/* 190 */       this.m_controlledMap.put(this.UI_INFO_MAP[i][3], cntrls);
/*     */     }
/*     */ 
/* 194 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 199 */         Properties props = EditFieldRuleDlg.this.m_helper.m_props;
/* 200 */         boolean isRequired = props.getProperty("dpRuleFieldType").equals("required");
/* 201 */         if (isRequired)
/*     */         {
/* 203 */           String reqMsg = props.getProperty("dprFieldRequiredMsg");
/* 204 */           if (reqMsg.length() == 0)
/*     */           {
/* 206 */             this.m_errorMessage = IdcMessageFactory.lc("apDpRequiredMsgMissingError", new Object[0]);
/* 207 */             return false;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 212 */         String[][] customKeys = { { "dprFieldUseCustomCaption", "dprFieldCaption", "apDpCustomFieldCaptionMissingMsg" }, { "dprFieldUseCustomInclude", "dprFieldInclude", "apDpCustomFieldIncludeMissingMsg" } };
/*     */ 
/* 217 */         for (int i = 0; i < customKeys.length; ++i)
/*     */         {
/* 219 */           String key = customKeys[i][0];
/* 220 */           boolean isCustom = StringUtils.convertToBool(props.getProperty(key), false);
/* 221 */           if (!isCustom)
/*     */             continue;
/* 223 */           String val = props.getProperty(customKeys[i][1]);
/* 224 */           if ((val != null) && (val.length() != 0))
/*     */             continue;
/* 226 */           this.m_errorMessage = IdcMessageFactory.lc(customKeys[i][2], new Object[0]);
/* 227 */           return false;
/*     */         }
/*     */ 
/* 231 */         return true;
/*     */       }
/*     */     };
/* 234 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 237 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 238 */     this.m_helper.addLastComponentInRow(mainPanel, typePanel);
/*     */ 
/* 240 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 241 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 242 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 243 */     this.m_helper.addLastComponentInRow(mainPanel, panel);
/*     */   }
/*     */ 
/*     */   protected void addBoxTextControls(JPanel panel, String[] defs, boolean isCombo, String[][] opts)
/*     */   {
/* 249 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 250 */     CustomCheckbox cptBox = new CustomCheckbox(this.m_systemInterface.getString(defs[0]));
/*     */ 
/* 252 */     this.m_helper.addExchangeComponent(panel, cptBox, defs[1]);
/* 253 */     cptBox.addItemListener(this);
/*     */ 
/* 255 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 256 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 257 */     Component cmp = null;
/* 258 */     if (isCombo)
/*     */     {
/* 260 */       ComboChoice choice = new ComboChoice(this.m_systemInterface.getString("apDpNoneSpecified"));
/*     */ 
/* 262 */       choice.initChoiceList(opts);
/* 263 */       cmp = choice;
/*     */     }
/*     */     else
/*     */     {
/* 267 */       cmp = new CustomTextField(30);
/*     */     }
/*     */ 
/* 270 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 271 */     this.m_helper.addExchangeComponent(panel, cmp, defs[2]);
/*     */ 
/* 273 */     Component[] cntrls = new Component[2];
/* 274 */     cntrls[0] = cptBox;
/* 275 */     cntrls[1] = cmp;
/* 276 */     this.m_controlledMap.put(defs[1], cntrls);
/*     */   }
/*     */ 
/*     */   protected void loadComponents()
/*     */   {
/* 282 */     String type = this.m_helper.m_props.getProperty("dpRuleFieldType");
/* 283 */     if ((type != null) && (type.equals("label")))
/*     */     {
/* 285 */       this.m_helper.m_props.put("dpRuleFieldType", "infoOnly");
/*     */     }
/* 287 */     createSummaries();
/* 288 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 293 */     for (Enumeration en = this.m_controlledMap.keys(); en.hasMoreElements(); )
/*     */     {
/* 295 */       String key = (String)en.nextElement();
/* 296 */       boolean isEnabled = StringUtils.convertToBool(this.m_helper.m_props.getProperty(key), false);
/*     */ 
/* 298 */       Component[] objs = (Component[])(Component[])this.m_controlledMap.get(key);
/* 299 */       objs[1].setEnabled(isEnabled);
/*     */ 
/* 301 */       if (((key.equals("dprFieldIsRestricted")) && (!this.m_fieldDef.m_isOptionList)) || ((key.equals("dprFieldIsDerived")) && (this.m_fieldDef.m_name.equals("dDocName"))))
/*     */       {
/* 306 */         objs[0].setEnabled(false);
/* 307 */         objs[1].setEnabled(false);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void createSummaries()
/*     */   {
/* 314 */     String[][] types = { { "default", "dprFieldDefaultScriptSummary" }, { "derived", "dprFieldDerivedScriptSummary" } };
/*     */ 
/* 320 */     for (int i = 0; i < types.length; ++i)
/*     */     {
/* 322 */       String summary = DocProfileScriptUtils.computeScriptString("", this.m_binder, types[i][0], true);
/* 323 */       this.m_helper.m_props.put(types[i][1], summary);
/*     */     }
/*     */ 
/* 326 */     createListSummary();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 334 */     String cmd = e.getActionCommand();
/*     */ 
/* 337 */     String[] info = null;
/* 338 */     int len = this.UI_INFO_MAP.length;
/* 339 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 341 */       if (!cmd.equals(this.UI_INFO_MAP[i][1]))
/*     */         continue;
/* 343 */       info = this.UI_INFO_MAP[i];
/* 344 */       break;
/*     */     }
/*     */ 
/* 348 */     String type = info[0];
/* 349 */     Properties props = DocProfileScriptUtils.loadConfiguration(type);
/* 350 */     DataBinder scriptData = loadEditConfiguration(props);
/*     */ 
/* 352 */     String helpPage = DialogHelpTable.getHelpPage(info[5]);
/* 353 */     if ((cmd.equals("editDefault")) || (cmd.equals("editDerived")))
/*     */     {
/* 355 */       EditProfileScriptDlg dlg = new EditProfileScriptDlg(this.m_systemInterface, this.m_shContext, this.m_systemInterface.getString(info[6]), helpPage);
/*     */ 
/* 358 */       int result = dlg.init(scriptData, props, this.m_fieldDef);
/* 359 */       if (result == 1)
/*     */       {
/* 361 */         this.m_binder.merge(scriptData);
/*     */       }
/*     */ 
/* 364 */       String summary = DocProfileScriptUtils.computeScriptString("", this.m_binder, info[0], true);
/* 365 */       this.m_helper.m_exchange.setComponentValue(info[4], summary);
/*     */     } else {
/* 367 */       if (!cmd.equals("editRestricted"))
/*     */         return;
/* 369 */       EditRestrictedListDlg dlg = new EditRestrictedListDlg(this.m_systemInterface, this.m_systemInterface.getString(info[6]), helpPage);
/*     */ 
/* 372 */       int result = dlg.init(null, scriptData);
/* 373 */       if (result != 1)
/*     */         return;
/* 375 */       this.m_binder.merge(scriptData);
/* 376 */       createListSummary();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void createListSummary()
/*     */   {
/* 383 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("RestrictedList");
/* 384 */     if (drset == null)
/*     */     {
/* 386 */       return;
/*     */     }
/*     */ 
/* 389 */     StringBuffer buff = new StringBuffer();
/*     */     try
/*     */     {
/* 393 */       int index = ResultSetUtils.getIndexMustExist(drset, "dpRuleListValue");
/* 394 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 396 */         if (buff.length() > 0)
/*     */         {
/* 398 */           buff.append("\n");
/*     */         }
/* 400 */         buff.append(drset.getStringValue(index));
/*     */       }
/* 402 */       this.m_helper.m_exchange.setComponentValue("dprFieldRestrictedListSummary", buff.toString());
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 406 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected DataBinder loadEditConfiguration(Properties props)
/*     */   {
/* 412 */     String tableName = props.getProperty("TableName");
/*     */ 
/* 414 */     DataBinder scriptData = new DataBinder();
/* 415 */     Properties localData = this.m_binder.getLocalData();
/* 416 */     localData = (Properties)localData.clone();
/* 417 */     scriptData.setLocalData(localData);
/*     */ 
/* 419 */     DataResultSet clauseSet = (DataResultSet)this.m_binder.getResultSet(tableName);
/* 420 */     DataResultSet drset = null;
/* 421 */     if (clauseSet == null)
/*     */     {
/* 423 */       String[] clmns = null;
/* 424 */       if (tableName == "RestrictedList")
/*     */       {
/* 426 */         clmns = DocProfileScriptUtils.DP_RULE_RESTRICTEDLIST_COLUMNS;
/*     */       }
/*     */       else
/*     */       {
/* 430 */         clmns = DocProfileScriptUtils.DP_RULE_COLUMNS;
/*     */       }
/* 432 */       drset = new DataResultSet(clmns);
/*     */     }
/*     */     else
/*     */     {
/* 436 */       drset = new DataResultSet();
/* 437 */       drset.copy(clauseSet);
/*     */     }
/* 439 */     scriptData.addResultSet(tableName, drset);
/*     */ 
/* 441 */     return scriptData;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 449 */     Object src = e.getSource();
/* 450 */     if (!src instanceof JCheckBox) {
/*     */       return;
/*     */     }
/* 453 */     for (Enumeration en = this.m_controlledMap.elements(); en.hasMoreElements(); )
/*     */     {
/* 455 */       Component[] objs = (Component[])(Component[])en.nextElement();
/* 456 */       if (objs[0] == src)
/*     */       {
/* 458 */         JCheckBox box = (JCheckBox)objs[0];
/* 459 */         boolean state = box.isSelected();
/* 460 */         objs[1].setEnabled(state);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 468 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80607 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditFieldRuleDlg
 * JD-Core Version:    0.5.4
 */