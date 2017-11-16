/*     */ package intradoc.apps.docman;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.gui.iwt.CheckboxAggregate;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.AddAliasDlg;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import intradoc.shared.gui.ViewDlg;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddUserSubscription
/*     */   implements ComponentBinder, ActionListener
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_helpPage;
/*  77 */   protected SharedContext m_context = null;
/*  78 */   protected ExecutionContext m_cxt = null;
/*     */   protected Vector m_fieldList;
/*     */   protected Hashtable m_fieldDefs;
/*     */   protected ViewFields m_docFields;
/*  84 */   protected Hashtable m_checkAggs = new Hashtable();
/*     */ 
/*     */   public AddUserSubscription(SystemInterface sys, SharedContext context, String title, String helpPage)
/*     */   {
/*  89 */     this.m_helper = new DialogHelper(sys, title, true);
/*  90 */     this.m_systemInterface = sys;
/*  91 */     this.m_context = context;
/*  92 */     this.m_helpPage = helpPage;
/*  93 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*     */   }
/*     */ 
/*     */   public void init(DialogCallback okCallback, Properties props)
/*     */     throws DataException
/*     */   {
/*  99 */     this.m_helper.m_props = props;
/*     */ 
/* 101 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 104 */     initFieldLists();
/*     */ 
/* 106 */     initUI(mainPanel);
/*     */   }
/*     */ 
/*     */   protected void initFieldLists()
/*     */     throws DataException
/*     */   {
/* 112 */     this.m_docFields = new ViewFields(this.m_cxt);
/* 113 */     this.m_docFields.addStandardDocFields();
/* 114 */     DataResultSet docMeta = SharedObjects.getTable("DocMetaDefinition");
/* 115 */     this.m_docFields.addMetaFields(docMeta);
/* 116 */     this.m_docFields.addField("fParentGUID", LocaleResources.getString("apSubscriptionFolderID", this.m_cxt));
/*     */ 
/* 118 */     this.m_fieldDefs = new Hashtable();
/*     */ 
/* 120 */     Vector v = this.m_docFields.m_viewFields;
/* 121 */     int length = v.size();
/* 122 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 124 */       ViewFieldDef def = (ViewFieldDef)v.elementAt(i);
/* 125 */       this.m_fieldDefs.put(def.m_name, def);
/*     */     }
/*     */ 
/* 129 */     String str = this.m_helper.m_props.getProperty("scpFields");
/* 130 */     this.m_fieldList = StringUtils.parseArray(str, ',', ',');
/*     */   }
/*     */ 
/*     */   protected void initUI(JPanel mainPanel)
/*     */   {
/* 136 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 137 */     String caption = this.m_systemInterface.localizeCaption("apAddSubscriptionUserCaption");
/* 138 */     this.m_helper.addComponent(mainPanel, new CustomLabel(caption, 1));
/*     */ 
/* 141 */     String[][] info = { { "user", "apLabelUser", "1" }, { "alias", "apLabelAlias", "0" } };
/*     */ 
/* 146 */     LocaleResources.localizeDoubleArray(info, this.m_cxt, 1);
/* 147 */     ButtonGroup grp = new ButtonGroup();
/* 148 */     int num = info.length;
/* 149 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 151 */       String name = info[i][0];
/* 152 */       boolean state = StringUtils.convertToBool(info[i][2], false);
/* 153 */       CheckboxAggregate chkAgg = new CheckboxAggregate(info[i][1], 1, grp, state);
/* 154 */       this.m_checkAggs.put(name, chkAgg);
/* 155 */       chkAgg.setId(name);
/* 156 */       ItemListener il = new ItemListener(name)
/*     */       {
/*     */         public void itemStateChanged(ItemEvent e)
/*     */         {
/* 160 */           if (e.getStateChange() != 1)
/*     */             return;
/* 162 */           AddUserSubscription.this.selectUserAliasType(this.val$name);
/*     */         }
/*     */       };
/* 166 */       chkAgg.addItemListener(il);
/*     */ 
/* 169 */       caption = this.m_systemInterface.getString("apLabelSelectButton");
/* 170 */       JButton btn = new JButton(caption);
/* 171 */       btn.setActionCommand(name);
/* 172 */       btn.addActionListener(this);
/* 173 */       chkAgg.addBuddy(btn);
/*     */ 
/* 175 */       chkAgg.setEnabled(state);
/*     */ 
/* 177 */       this.m_helper.addComboComponent(mainPanel, chkAgg, name, name);
/*     */     }
/*     */ 
/* 181 */     this.m_helper.m_gridHelper.addEmptyRow(mainPanel);
/* 182 */     caption = this.m_systemInterface.localizeCaption("apAddSubscriptionUserCriteria");
/* 183 */     this.m_helper.addComponent(mainPanel, new CustomLabel(caption, 1));
/* 184 */     this.m_helper.m_gridHelper.addEmptyRow(mainPanel);
/*     */ 
/* 187 */     int length = this.m_fieldList.size();
/* 188 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 190 */       String fieldName = (String)this.m_fieldList.elementAt(i);
/* 191 */       FieldDef def = (FieldDef)this.m_fieldDefs.get(fieldName);
/* 192 */       caption = fieldName + ":";
/* 193 */       if (def != null)
/*     */       {
/* 195 */         caption = this.m_systemInterface.getString(def.m_caption);
/*     */       }
/* 197 */       String fieldDescript = caption;
/*     */ 
/* 199 */       Component component = createFieldComponent(fieldName, def);
/* 200 */       this.m_helper.addLabelFieldPair(mainPanel, fieldDescript, component, fieldName);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 207 */     int result = this.m_helper.prompt();
/* 208 */     return result;
/*     */   }
/*     */ 
/*     */   protected Component createFieldComponent(String fieldName, FieldDef fieldDef)
/*     */   {
/* 215 */     boolean useOptionList = fieldDef.isMandatoryOptionList();
/* 216 */     Vector options = null;
/* 217 */     String[][] display = (String[][])null;
/* 218 */     if (useOptionList)
/*     */     {
/* 220 */       display = this.m_docFields.getDisplayMap(fieldDef.m_optionListKey);
/* 221 */       if (display == null)
/*     */       {
/* 223 */         options = SharedObjects.getOptList(fieldDef.m_optionListKey);
/*     */       }
/*     */     }
/*     */ 
/* 227 */     boolean isChoice = false;
/* 228 */     if ((options != null) || (display != null))
/*     */     {
/* 230 */       isChoice = true;
/*     */     }
/*     */     Component component;
/* 234 */     if (isChoice)
/*     */     {
/* 236 */       DisplayChoice choice = new DisplayChoice();
/* 237 */       Component component = choice;
/* 238 */       if (display != null)
/*     */       {
/* 240 */         choice.init(display);
/*     */       }
/*     */       else
/*     */       {
/* 244 */         choice.init(options);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 249 */       component = new CustomTextField(20);
/*     */     }
/*     */ 
/* 252 */     return component;
/*     */   }
/*     */ 
/*     */   protected void selectUserAliasType(String selName)
/*     */   {
/* 257 */     for (Enumeration en = this.m_checkAggs.keys(); en.hasMoreElements(); )
/*     */     {
/* 259 */       String key = (String)en.nextElement();
/* 260 */       CheckboxAggregate agg = (CheckboxAggregate)this.m_checkAggs.get(key);
/* 261 */       boolean isEnabled = key.equals(selName);
/* 262 */       agg.setEnabled(isEnabled);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 271 */     String[] val = null;
/* 272 */     String cmd = e.getActionCommand();
/*     */ 
/* 274 */     if (cmd.equals("user"))
/*     */     {
/* 276 */       ViewData viewData = new ViewData(2);
/* 277 */       viewData.m_isViewOnly = false;
/* 278 */       viewData.m_viewName = "UserSelectView";
/* 279 */       String title = this.m_systemInterface.getString("apTitleSelectUser");
/* 280 */       ViewDlg dlg = new ViewDlg(this.m_helper.m_dialog, this.m_systemInterface, title, this.m_context, DialogHelpTable.getHelpPage("SelectUser"));
/*     */ 
/* 282 */       dlg.init(viewData, null);
/*     */ 
/* 284 */       if (dlg.prompt() == 1)
/*     */       {
/* 286 */         val = dlg.getSelectedObjs();
/*     */       }
/*     */     }
/* 289 */     else if (cmd.equals("alias"))
/*     */     {
/* 291 */       String title = this.m_systemInterface.getString("apTitleSelectAlias");
/* 292 */       AddAliasDlg dlg = new AddAliasDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage("SelectAlias"));
/*     */ 
/* 294 */       if ((dlg.init(false)) && 
/* 296 */         (dlg.prompt() == 1))
/*     */       {
/* 298 */         val = dlg.getSelected();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 303 */     if (val == null)
/*     */       return;
/* 305 */     CheckboxAggregate agg = (CheckboxAggregate)this.m_checkAggs.get(cmd);
/* 306 */     agg.setBuddyValue(true, val[0], 0);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 316 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 317 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 322 */     String name = exchange.m_compName;
/* 323 */     String val = exchange.m_compValue;
/* 324 */     IdcMessage errMsg = null;
/*     */ 
/* 326 */     boolean isCheckbox = exchange.m_component instanceof JCheckBox;
/* 327 */     int index = name.indexOf(":enabled");
/* 328 */     if (index >= 0)
/*     */     {
/* 330 */       isCheckbox = true;
/* 331 */       name = name.substring(0, index);
/*     */     }
/*     */ 
/* 334 */     CheckboxAggregate agg = null;
/* 335 */     if (isCheckbox)
/*     */     {
/* 337 */       agg = (CheckboxAggregate)this.m_checkAggs.get(name);
/* 338 */       if (agg == null)
/*     */       {
/* 340 */         exchange.m_errorMessage = IdcMessageFactory.lc("apSubscriptionInterfaceBroken", new Object[] { name });
/* 341 */         return false;
/*     */       }
/* 343 */       if (agg.m_checkbox.isSelected())
/*     */       {
/* 345 */         this.m_helper.m_props.put("dSubscriptionAliasType", agg.m_id);
/*     */       }
/*     */       else
/*     */       {
/* 349 */         String curType = this.m_helper.m_props.getProperty("dSubscriptionAliasType");
/* 350 */         if ((curType != null) && (name.equals(curType)))
/*     */         {
/* 352 */           this.m_helper.m_props.remove("dSubscriptionAliasType");
/*     */         }
/*     */       }
/* 355 */       return true;
/*     */     }
/*     */ 
/* 359 */     index = name.lastIndexOf(58);
/* 360 */     if (index >= 0)
/*     */     {
/* 362 */       name = name.substring(0, index);
/*     */     }
/*     */ 
/* 365 */     agg = (CheckboxAggregate)this.m_checkAggs.get(name);
/* 366 */     if (agg != null)
/*     */     {
/* 369 */       String aliasType = this.m_helper.m_props.getProperty("dSubscriptionAliasType");
/* 370 */       if ((aliasType != null) && (aliasType.equals(name)))
/*     */       {
/* 372 */         this.m_helper.m_props.put("dSubscriptionAlias", val);
/*     */       }
/*     */       else
/*     */       {
/* 376 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 380 */     if ((val == null) || (val.length() == 0))
/*     */     {
/* 383 */       FieldDef def = (FieldDef)this.m_fieldDefs.get(name);
/* 384 */       String desc = (def == null) ? name : def.m_caption;
/*     */ 
/* 386 */       errMsg = IdcMessageFactory.lc("apFieldMustHaveValue", new Object[] { desc });
/*     */     }
/*     */ 
/* 393 */     if (errMsg != null)
/*     */     {
/* 395 */       exchange.m_errorMessage = errMsg;
/* 396 */       return false;
/*     */     }
/* 398 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 403 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98371 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.AddUserSubscription
 * JD-Core Version:    0.5.4
 */