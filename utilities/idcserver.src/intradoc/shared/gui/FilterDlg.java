/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.CheckboxAggregate;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Container;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ 
/*     */ public class FilterDlg
/*     */   implements ComponentBinder, ActionListener
/*     */ {
/*  66 */   protected SystemInterface m_systemInterface = null;
/*  67 */   protected ExecutionContext m_cxt = null;
/*  68 */   protected SharedContext m_shContext = null;
/*  69 */   protected DialogHelper m_helper = null;
/*  70 */   protected String m_helpPage = null;
/*     */ 
/*  72 */   protected DocumentLocalizedProfile m_docProfile = null;
/*  73 */   protected SchemaHelper m_schHelper = null;
/*  74 */   protected ViewFields m_fields = null;
/*  75 */   protected ViewData m_viewData = null;
/*     */ 
/*  78 */   protected boolean m_isFieldOnly = true;
/*     */ 
/*  81 */   protected Hashtable m_filterData = null;
/*     */ 
/*  83 */   protected Properties m_ignoreFields = new Properties();
/*     */ 
/*  86 */   protected Hashtable m_options = new Hashtable();
/*  87 */   protected Hashtable m_displayMaps = new Hashtable();
/*     */ 
/*  90 */   protected boolean m_customMetaInSeparatePanel = true;
/*  91 */   protected boolean m_isSplitFields = true;
/*  92 */   protected boolean m_stdDatesInSeparatePanel = true;
/*     */ 
/*     */   public FilterDlg(SystemInterface sys, String title, String helpPage, SharedContext shContext)
/*     */   {
/*  96 */     this.m_helper = new DialogHelper(sys, title, true, true);
/*  97 */     this.m_systemInterface = sys;
/*  98 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*  99 */     this.m_helpPage = helpPage;
/* 100 */     this.m_shContext = shContext;
/*     */   }
/*     */ 
/*     */   public void init(Hashtable filterData, Properties props)
/*     */   {
/* 105 */     initEx(filterData, props, null, null, null, null);
/*     */   }
/*     */ 
/*     */   public void init(Hashtable data, DocumentLocalizedProfile docProfile, ViewFields fields, ViewData viewData)
/*     */   {
/* 111 */     initEx(data, null, docProfile, fields, viewData, null);
/*     */   }
/*     */ 
/*     */   public void initEx(Hashtable filterData, Properties props, DocumentLocalizedProfile docProfile, ViewFields fields, ViewData viewData, SchemaHelper schHelper)
/*     */   {
/* 117 */     this.m_filterData = filterData;
/* 118 */     this.m_docProfile = docProfile;
/* 119 */     this.m_fields = fields;
/* 120 */     this.m_viewData = viewData;
/* 121 */     this.m_helper.m_scrollPane.setPreferredSize(new Dimension(500, 500));
/*     */ 
/* 123 */     if (schHelper == null)
/*     */     {
/* 125 */       schHelper = new SchemaHelper();
/* 126 */       schHelper.computeMaps();
/*     */     }
/* 128 */     this.m_schHelper = schHelper;
/*     */ 
/* 130 */     JPanel mainPanel = this.m_helper.initStandard(this, null, 2, true, this.m_helpPage);
/*     */ 
/* 133 */     createFilter(mainPanel);
/*     */   }
/*     */ 
/*     */   public void setCustomMetaInSeparatePanel(boolean val)
/*     */   {
/* 138 */     this.m_customMetaInSeparatePanel = val;
/*     */   }
/*     */ 
/*     */   public void setStdDatesInSeparatePanel(boolean val)
/*     */   {
/* 143 */     this.m_stdDatesInSeparatePanel = val;
/*     */   }
/*     */ 
/*     */   public void setIsSplitFields(boolean isSplit)
/*     */   {
/* 148 */     this.m_isSplitFields = isSplit;
/*     */   }
/*     */ 
/*     */   public void setIsFieldOnly(boolean isFieldOnly)
/*     */   {
/* 153 */     this.m_isFieldOnly = isFieldOnly;
/*     */   }
/*     */ 
/*     */   protected void createFilter(JPanel mainPanel)
/*     */   {
/* 159 */     Vector filterDataList = FilterUtils.createFilterData(this.m_fields, this.m_filterData, null, this.m_isSplitFields);
/*     */ 
/* 161 */     JPanel filterPanel = null;
/* 162 */     String curPanelName = null;
/*     */ 
/* 164 */     int size = filterDataList.size();
/* 165 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 167 */       FilterData fd = (FilterData)filterDataList.elementAt(i);
/* 168 */       ViewFieldDef fieldDef = fd.m_fieldDef;
/* 169 */       String name = fd.m_id;
/*     */ 
/* 171 */       if (isIgnoredField(name))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 176 */       String panelName = getPanelName(fd, i);
/* 177 */       if ((curPanelName == null) || (!curPanelName.equals(panelName)))
/*     */       {
/* 180 */         curPanelName = panelName;
/* 181 */         filterPanel = createPanel(curPanelName, mainPanel);
/*     */       }
/*     */ 
/* 184 */       Vector options = null;
/* 185 */       String[][] display = (String[][])null;
/* 186 */       boolean useOptionList = fieldDef.m_isOptionList;
/* 187 */       if (useOptionList)
/*     */       {
/* 189 */         String key = fieldDef.m_optionListKey;
/* 190 */         display = getDisplayMap(key);
/* 191 */         if (display == null)
/*     */         {
/* 193 */           options = getOptionList(key);
/*     */         }
/*     */       }
/*     */ 
/* 197 */       int type = 1;
/* 198 */       if ((options != null) && (options.size() > 0))
/*     */       {
/* 200 */         type = 3;
/*     */       }
/* 202 */       else if (display != null)
/*     */       {
/* 204 */         type = 2;
/*     */       }
/*     */ 
/* 208 */       int numBuddies = fd.m_values.size();
/* 209 */       CheckboxAggregate comp = new CheckboxAggregate(fieldDef.m_caption, type, numBuddies);
/*     */ 
/* 211 */       if (options != null)
/*     */       {
/* 213 */         for (int j = 0; j < numBuddies; ++j)
/*     */         {
/* 215 */           comp.initChoice(options, j);
/*     */         }
/*     */       }
/* 218 */       else if (display != null)
/*     */       {
/* 220 */         for (int j = 0; j < numBuddies; ++j)
/*     */         {
/* 222 */           comp.initChoice(display, j);
/*     */         }
/*     */       }
/*     */ 
/* 226 */       if ((fieldDef.m_hasNamedRelation) || (fieldDef.isComplexOptionList()))
/*     */       {
/* 228 */         comp.addBrowseButton(null, fd.m_id, this);
/*     */       }
/*     */ 
/* 231 */       comp.setData(fd.m_isUsed, fd.m_values);
/* 232 */       this.m_helper.addComboComponent(filterPanel, comp, name + ":enabled", name);
/*     */     }
/*     */ 
/* 235 */     String wildCards = SharedObjects.getEnvironmentValue("DatabaseWildcards");
/* 236 */     if (wildCards == null)
/*     */     {
/* 238 */       wildCards = "%_";
/*     */     }
/* 240 */     String helpMsg = LocaleResources.getString("apWildCardMessage", this.m_cxt, wildCards.substring(0, 1), wildCards.substring(1));
/*     */ 
/* 243 */     Container newPane = new PanePanel();
/* 244 */     newPane.setLayout(new BorderLayout());
/*     */ 
/* 246 */     PanePanel helpPane = new PanePanel();
/* 247 */     helpPane.add(new CustomLabel(helpMsg));
/*     */ 
/* 249 */     newPane.add("South", helpPane);
/* 250 */     newPane.add("Center", this.m_helper.m_scrollPane);
/* 251 */     this.m_helper.m_dialog.add("Center", newPane);
/*     */   }
/*     */ 
/*     */   protected String getPanelName(FilterData data, int index)
/*     */   {
/* 256 */     String panelName = "Standard";
/* 257 */     ViewFieldDef def = data.m_fieldDef;
/* 258 */     if ((def.m_isCustomMeta) && (this.m_customMetaInSeparatePanel))
/*     */     {
/* 260 */       panelName = "MetaPanel";
/*     */     }
/* 262 */     else if ((def.m_isStandardDateField) && (this.m_stdDatesInSeparatePanel))
/*     */     {
/* 264 */       panelName = "DatePanel";
/*     */     }
/* 266 */     return panelName;
/*     */   }
/*     */ 
/*     */   protected JPanel createPanel(String name, JPanel mainPanel)
/*     */   {
/* 271 */     return createTitledPanel(mainPanel, null, true);
/*     */   }
/*     */ 
/*     */   protected JPanel createTitledPanel(JPanel mainPanel, String title, boolean isLast)
/*     */   {
/* 277 */     CustomPanel pnl = new CustomPanel();
/* 278 */     this.m_helper.makePanelGridBag(pnl, 2);
/* 279 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/* 280 */     this.m_helper.addComponent(mainPanel, pnl);
/*     */ 
/* 282 */     if ((title != null) && (title.length() > 0))
/*     */     {
/* 284 */       if (!isLast)
/*     */       {
/* 286 */         this.m_helper.m_gridHelper.prepareAddRowElement();
/*     */       }
/* 288 */       this.m_helper.addComponent(pnl, new CustomLabel(title, 1));
/*     */     }
/*     */ 
/* 291 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected boolean isIgnoredField(String name)
/*     */   {
/* 296 */     String val = this.m_ignoreFields.getProperty(name);
/* 297 */     return val != null;
/*     */   }
/*     */ 
/*     */   protected Vector getOptionList(String key)
/*     */   {
/* 302 */     Vector options = null;
/* 303 */     if (this.m_docProfile != null)
/*     */     {
/* 305 */       options = this.m_docProfile.getOptionList(key, true);
/*     */     }
/* 307 */     if ((options == null) && (this.m_options != null))
/*     */     {
/* 309 */       options = (Vector)this.m_options.get(key);
/*     */     }
/* 311 */     if (options == null)
/*     */     {
/* 313 */       options = SharedObjects.getOptList(key);
/*     */     }
/* 315 */     return options;
/*     */   }
/*     */ 
/*     */   protected String[][] getDisplayMap(String key)
/*     */   {
/* 320 */     String[][] display = (String[][])null;
/* 321 */     if (this.m_displayMaps != null)
/*     */     {
/* 323 */       display = (String[][])(String[][])this.m_displayMaps.get(key);
/*     */     }
/* 325 */     if (display == null)
/*     */     {
/* 327 */       display = this.m_fields.getDisplayMap(key);
/*     */     }
/* 329 */     return display;
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 334 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void setDocFields(ViewFields fields)
/*     */   {
/* 339 */     this.m_fields = fields;
/*     */   }
/*     */ 
/*     */   public void addOptionList(String key, Vector options)
/*     */   {
/* 344 */     this.m_options.put(key, options);
/*     */   }
/*     */ 
/*     */   public void addDisplayMap(String key, String[][] displayMap)
/*     */   {
/* 349 */     this.m_displayMaps.put(key, displayMap);
/*     */   }
/*     */ 
/*     */   public void addIgnoreField(String field)
/*     */   {
/* 354 */     this.m_ignoreFields.put(field, field);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 365 */     String name = exchange.m_compName;
/* 366 */     boolean isCheckbox = exchange.m_component instanceof JCheckBox;
/* 367 */     int index = name.indexOf(":enabled");
/* 368 */     if (index >= 0)
/*     */     {
/* 370 */       isCheckbox = true;
/* 371 */       name = name.substring(0, index);
/*     */     }
/*     */ 
/* 374 */     int buddyIndex = 0;
/* 375 */     if (!isCheckbox)
/*     */     {
/* 377 */       String[] nameParam = new String[1];
/* 378 */       nameParam[0] = name;
/*     */ 
/* 380 */       buddyIndex = determineBuddy(nameParam);
/* 381 */       name = nameParam[0];
/*     */     }
/*     */ 
/* 384 */     FilterData fd = (FilterData)this.m_filterData.get(name);
/* 385 */     if (fd == null)
/*     */     {
/* 387 */       Report.trace(null, "Unable to find the filter data for " + name, null);
/* 388 */       return;
/*     */     }
/* 390 */     boolean isDate = fd.m_fieldDef.m_type.equalsIgnoreCase("date");
/*     */ 
/* 392 */     if (updateComponent)
/*     */     {
/* 394 */       if (isCheckbox)
/*     */       {
/* 396 */         exchange.m_compValue = String.valueOf(fd.m_isUsed);
/*     */       }
/*     */       else
/*     */       {
/* 400 */         String value = fd.getValueAt(buddyIndex);
/* 401 */         if (isDate)
/*     */         {
/* 403 */           value = LocaleResources.localizeDate(value, this.m_cxt);
/*     */         }
/* 405 */         exchange.m_compValue = value;
/*     */       }
/*     */ 
/*     */     }
/* 410 */     else if (isCheckbox)
/*     */     {
/* 412 */       fd.m_isUsed = StringUtils.convertToBool(exchange.m_compValue, false);
/*     */     }
/*     */     else
/*     */     {
/* 416 */       String value = exchange.m_compValue;
/* 417 */       if (isDate)
/*     */       {
/* 419 */         value = LocaleResources.internationalizeDate(value, this.m_cxt);
/*     */       }
/* 421 */       fd.setValueAt(value, buddyIndex);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 429 */     String name = exchange.m_compName;
/* 430 */     String value = exchange.m_compValue;
/* 431 */     boolean isCheckbox = exchange.m_component instanceof JCheckBox;
/*     */ 
/* 433 */     if (!isCheckbox)
/*     */     {
/* 436 */       String[] nameParam = new String[1];
/* 437 */       nameParam[0] = name;
/* 438 */       determineBuddy(nameParam);
/* 439 */       name = nameParam[0];
/*     */ 
/* 443 */       String parentName = name + ":enabled";
/* 444 */       Object[] objs = exchange.findComponent(parentName, false);
/* 445 */       if (objs != null)
/*     */       {
/* 447 */         Component cmp = (Component)objs[1];
/* 448 */         if (cmp instanceof JCheckBox)
/*     */         {
/* 450 */           JCheckBox chbx = (JCheckBox)cmp;
/* 451 */           if (!chbx.isSelected())
/*     */           {
/* 453 */             return true;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 459 */     FilterData fd = (FilterData)this.m_filterData.get(name);
/* 460 */     if (fd == null)
/*     */     {
/* 463 */       return true;
/*     */     }
/*     */ 
/* 466 */     IdcMessage errMsg = null;
/* 467 */     String type = fd.m_fieldDef.m_type;
/* 468 */     if (type.equalsIgnoreCase("int"))
/*     */     {
/*     */       try
/*     */       {
/* 472 */         Integer.parseInt(value);
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 476 */         errMsg = IdcMessageFactory.lc("apValueNotIntegerForField", new Object[] { value, fd.m_fieldDef.m_caption });
/*     */       }
/*     */     }
/* 479 */     else if (type.equalsIgnoreCase("date"))
/*     */     {
/*     */       try
/*     */       {
/* 484 */         if (value.length() > 0)
/*     */         {
/* 486 */           LocaleResources.parseDate(value, this.m_cxt);
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 491 */         errMsg = IdcMessageFactory.lc("apValueNotDate", new Object[] { fd.m_fieldDef.m_caption });
/*     */       }
/*     */     }
/*     */ 
/* 495 */     if (errMsg != null)
/*     */     {
/* 497 */       exchange.m_errorMessage = errMsg;
/* 498 */       return false;
/*     */     }
/* 500 */     return true;
/*     */   }
/*     */ 
/*     */   protected int determineBuddy(String[] param)
/*     */   {
/* 511 */     String name = param[0];
/* 512 */     int buddyIndex = 0;
/* 513 */     int index = name.lastIndexOf(58);
/* 514 */     if (index >= 0)
/*     */     {
/* 517 */       int splitIndex = name.indexOf(":split_");
/* 518 */       if (splitIndex < 0)
/*     */       {
/* 520 */         String str = name.substring(index + 1);
/*     */         try
/*     */         {
/* 523 */           buddyIndex = Integer.parseInt(str);
/* 524 */           name = name.substring(0, index);
/*     */         }
/*     */         catch (Exception ignore)
/*     */         {
/* 528 */           if (SystemUtils.m_verbose)
/*     */           {
/* 530 */             Report.debug("systemparse", null, ignore);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 536 */     param[0] = name;
/* 537 */     return buddyIndex;
/*     */   }
/*     */ 
/*     */   public String getString(String key)
/*     */   {
/* 542 */     return LocaleResources.getString(key, this.m_cxt);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 551 */     String name = e.getActionCommand();
/* 552 */     FilterData fd = (FilterData)this.m_filterData.get(name);
/* 553 */     ViewFieldDef fieldDef = fd.m_fieldDef;
/*     */ 
/* 557 */     String title = LocaleResources.getString("apTitleSelectValue", this.m_cxt);
/* 558 */     String helpPage = DialogHelpTable.getHelpPage("SchemaSelectValue");
/*     */ 
/* 560 */     SelectValueDlg dlg = new SelectValueDlg(this.m_systemInterface, title, helpPage, this.m_shContext);
/* 561 */     dlg.init(this.m_schHelper, fieldDef.m_name, this.m_viewData.m_tableName, this.m_isFieldOnly, true, true);
/* 562 */     if (dlg.prompt() != 1) {
/*     */       return;
/*     */     }
/* 565 */     String val = null;
/* 566 */     Properties props = dlg.getSelectedItem();
/* 567 */     val = props.getProperty(fieldDef.m_name);
/* 568 */     if (val == null)
/*     */     {
/* 570 */       val = dlg.getSelectedValue();
/*     */     }
/* 572 */     fd.setValueAt(val, 0);
/* 573 */     this.m_helper.m_exchange.setComponentValue(fd.m_id, val);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 579 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78495 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.FilterDlg
 * JD-Core Version:    0.5.4
 */