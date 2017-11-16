/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.AbstractButton;
/*     */ import javax.swing.ButtonModel;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AdvancedSearchOptionsDlg
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_context;
/*     */   protected SharedContext m_shContext;
/*     */   protected String m_helpPage;
/*  61 */   protected Hashtable m_controlMap = null;
/*     */ 
/*  68 */   String[][] CHECKBOX_INFO = { { "hasDataTable", "apFieldHasDataTableLabel" }, { "isZone", "apFieldIsZonedLabel" }, { "isInSearchResult", "apFieldIsInSearchResult" }, { "isOptimized", "apFieldSearchFieldIsOptimized" }, { "IsSortable", "apFieldSearchFieldIsSortable" }, { "IsInSearchResultFilterCategory", "apFieldInFilterCategory" } };
/*     */ 
/*  78 */   String[][] ZONEBOX_INFO = { { "isZoneSearch", "apFieldIsInZoneSearched" } };
/*     */ 
/*     */   public AdvancedSearchOptionsDlg(SystemInterface sys, String title, SharedContext shContext, String helpPage)
/*     */   {
/*  86 */     this.m_systemInterface = sys;
/*  87 */     this.m_context = sys.getExecutionContext();
/*  88 */     this.m_shContext = shContext;
/*     */ 
/*  90 */     title = LocaleResources.localizeMessage(title, this.m_context);
/*  91 */     this.m_helper = new DialogHelper(sys, title, true);
/*  92 */     this.m_helpPage = helpPage;
/*     */ 
/*  94 */     this.m_controlMap = new Hashtable();
/*     */   }
/*     */ 
/*     */   public int init(Properties props)
/*     */   {
/*  99 */     this.m_helper.m_props = props;
/*     */ 
/* 102 */     String str = props.getProperty("IsSortable");
/* 103 */     boolean isSortable = StringUtils.convertToBool(str, false);
/* 104 */     boolean hasDataTableSortableFieldLinkage = StringUtils.convertToBool(props.getProperty("hasDataTableSortableFieldLinkage"), false);
/*     */ 
/* 106 */     if ((isSortable) && (hasDataTableSortableFieldLinkage))
/*     */     {
/* 108 */       props.put("hasDataTable", str);
/*     */     }
/* 110 */     props.put("isInSearchResult", "1");
/*     */ 
/* 113 */     String advOptions = props.getProperty("advOptions");
/* 114 */     createPropertiesFromString(advOptions, props);
/*     */ 
/* 116 */     StringBuffer buff = new StringBuffer();
/* 117 */     String currentState = props.getProperty("currentState");
/* 118 */     Vector v = StringUtils.parseArray(currentState, ',', '^');
/* 119 */     int size = v.size();
/* 120 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 123 */       String key = (String)v.elementAt(i);
/* 124 */       String val = this.m_systemInterface.getString("apIndex_" + key);
/* 125 */       if (buff.length() > 0)
/*     */       {
/* 127 */         buff.append(",");
/*     */       }
/* 129 */       buff.append(val);
/*     */     }
/* 131 */     props.put("lcState", buff.toString());
/*     */ 
/* 133 */     initUI();
/* 134 */     enableDisable();
/* 135 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public static void createPropertiesFromString(String propertyString, Properties properties)
/*     */   {
/* 146 */     if ((propertyString == null) || (propertyString.length() <= 0))
/*     */       return;
/* 148 */     Vector v = StringUtils.parseArray(propertyString, ',', '^');
/* 149 */     int size = v.size();
/* 150 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 152 */       String key = (String)v.elementAt(i);
/* 153 */       ++i;
/* 154 */       String val = (String)v.elementAt(i);
/* 155 */       properties.put(key, val);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/* 162 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/* 165 */     CustomPanel fieldPanel = new CustomPanel();
/* 166 */     this.m_helper.makePanelGridBag(fieldPanel, 2);
/*     */ 
/* 168 */     gh.prepareAddLastRowElement(17);
/* 169 */     this.m_helper.addLabelDisplayPair(fieldPanel, this.m_systemInterface.getString("apFieldCurrentStateLabel"), 100, "lcState");
/*     */ 
/* 173 */     CustomPanel optionsPanel = new CustomPanel();
/* 174 */     this.m_helper.makePanelGridBag(optionsPanel, 2);
/*     */ 
/* 176 */     ActionListener checkboxActionListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent actionEvent)
/*     */       {
/* 180 */         AbstractButton abstractButton = (AbstractButton)actionEvent.getSource();
/* 181 */         ButtonModel buttonModel = abstractButton.getModel();
/* 182 */         boolean selected = buttonModel.isSelected();
/* 183 */         String actionCommand = actionEvent.getActionCommand();
/*     */ 
/* 187 */         if (!actionCommand.equalsIgnoreCase("isOptimized"))
/*     */           return;
/* 189 */         boolean optimizedTextIsSortable = StringUtils.convertToBool(AdvancedSearchOptionsDlg.this.m_helper.m_props.getProperty("optimizedTextIsSortable"), false);
/*     */ 
/* 192 */         JCheckBox cmp = (JCheckBox)AdvancedSearchOptionsDlg.this.m_controlMap.get("IsSortable");
/* 193 */         if ((selected == true) && (optimizedTextIsSortable == true))
/*     */         {
/* 195 */           cmp.setEnabled(true);
/*     */         }
/* 197 */         if ((selected) || (optimizedTextIsSortable != true))
/*     */           return;
/* 199 */         cmp.setEnabled(false);
/* 200 */         cmp.setSelected(false);
/*     */       }
/*     */     };
/* 206 */     for (int i = 0; i < this.CHECKBOX_INFO.length; ++i)
/*     */     {
/* 208 */       String name = this.CHECKBOX_INFO[i][0];
/* 209 */       String cLabel = this.CHECKBOX_INFO[i][1];
/*     */ 
/* 211 */       CustomCheckbox box = new CustomCheckbox(this.m_systemInterface.getString(cLabel));
/* 212 */       box.setActionCommand(name);
/* 213 */       box.addActionListener(checkboxActionListener);
/* 214 */       gh.prepareAddLastRowElement();
/* 215 */       this.m_helper.addExchangeComponent(optionsPanel, box, name);
/*     */ 
/* 217 */       if (name.equals("isZone"))
/*     */       {
/* 219 */         addZoneOptions(optionsPanel, box);
/*     */       }
/* 221 */       this.m_controlMap.put(name, box);
/*     */     }
/*     */ 
/* 224 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 232 */           Properties props = AdvancedSearchOptionsDlg.this.m_helper.m_props;
/*     */ 
/* 237 */           boolean isSortable = StringUtils.convertToBool(props.getProperty("IsSortable"), false);
/*     */ 
/* 240 */           Vector options = new IdcVector();
/* 241 */           for (int i = 0; i < AdvancedSearchOptionsDlg.this.CHECKBOX_INFO.length; ++i)
/*     */           {
/* 243 */             String key = AdvancedSearchOptionsDlg.this.CHECKBOX_INFO[i][0];
/* 244 */             String val = props.getProperty(key);
/* 245 */             boolean isVal = StringUtils.convertToBool(val, false);
/* 246 */             if (key.equals("hasDataTable"))
/*     */             {
/* 248 */               if (!isVal)
/*     */               {
/* 250 */                 boolean hasDataTableSortableFieldLinkage = StringUtils.convertToBool(props.getProperty("hasDataTableSortableFieldLinkage"), false);
/*     */ 
/* 252 */                 if ((!hasDataTableSortableFieldLinkage) || (!isSortable))
/*     */                   continue;
/* 254 */                 this.m_errorMessage = IdcMessageFactory.lc("apSortFieldMustHaveDataTable", new Object[0]);
/* 255 */                 return false;
/*     */               }
/*     */ 
/* 259 */               props.put(key, "0");
/*     */             }
/* 261 */             else if (key.equals("isZone"))
/*     */             {
/* 263 */               if (!isVal) {
/*     */                 continue;
/*     */               }
/*     */ 
/* 267 */               for (int j = 0; j < AdvancedSearchOptionsDlg.this.ZONEBOX_INFO.length; ++j)
/*     */               {
/* 269 */                 String zKey = AdvancedSearchOptionsDlg.this.ZONEBOX_INFO[j][0];
/* 270 */                 String zVal = props.getProperty(zKey);
/* 271 */                 boolean zIsVal = StringUtils.convertToBool(zVal, false);
/* 272 */                 if (!zIsVal)
/*     */                   continue;
/* 274 */                 options.addElement(zKey);
/* 275 */                 options.addElement(zVal);
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/* 281 */             options.addElement(key);
/* 282 */             options.addElement(val);
/*     */           }
/* 284 */           String str = StringUtils.createString(options, ',', '^');
/* 285 */           props.put("advOptions", str);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 289 */           this.m_errorMessage = LocaleUtils.createMessageListFromThrowable(exp);
/* 290 */           return false;
/*     */         }
/* 292 */         return true;
/*     */       }
/*     */     };
/* 295 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 297 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 298 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 299 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 300 */     this.m_helper.addLastComponentInRow(mainPanel, fieldPanel);
/*     */ 
/* 302 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 303 */     this.m_helper.addLastComponentInRow(mainPanel, optionsPanel);
/*     */   }
/*     */ 
/*     */   protected void addZoneOptions(JPanel optionsPanel, JCheckBox zoneBox)
/*     */   {
/* 308 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 309 */     Insets oldInsets = gh.m_gc.insets;
/* 310 */     gh.m_gc.insets = new Insets(oldInsets.top, oldInsets.left + 10, oldInsets.bottom, oldInsets.right);
/*     */ 
/* 313 */     for (int i = 0; i < this.ZONEBOX_INFO.length; ++i)
/*     */     {
/* 315 */       String name = this.ZONEBOX_INFO[i][0];
/* 316 */       String cLabel = this.ZONEBOX_INFO[i][1];
/*     */ 
/* 318 */       CustomCheckbox box = new CustomCheckbox(this.m_systemInterface.getString(cLabel));
/* 319 */       gh.prepareAddLastRowElement();
/* 320 */       this.m_helper.addExchangeComponent(optionsPanel, box, name);
/* 321 */       this.m_controlMap.put(name, box);
/*     */     }
/*     */ 
/* 325 */     gh.m_gc.insets = oldInsets;
/*     */ 
/* 327 */     JCheckBox zBox = zoneBox;
/* 328 */     ItemListener listener = new ItemListener(zBox)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 332 */         boolean isEnabled = this.val$zBox.isSelected();
/* 333 */         AdvancedSearchOptionsDlg.this.enableDisableZone(isEnabled);
/*     */       }
/*     */     };
/* 336 */     zoneBox.addItemListener(listener);
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 341 */     Properties props = this.m_helper.m_props;
/*     */ 
/* 343 */     String type = props.getProperty("dType").toLowerCase();
/* 344 */     boolean isTextField = (type.indexOf("text") >= 0) || (type.indexOf("string") >= 0) || (type.indexOf("varchar") >= 0);
/* 345 */     boolean allowZoneConfiguration = StringUtils.convertToBool(props.getProperty("allowZoneConfig"), false);
/* 346 */     allowZoneConfiguration &= isTextField;
/* 347 */     boolean allowReturnedFieldChange = StringUtils.convertToBool(props.getProperty("allowReturnedFieldChange"), false);
/* 348 */     boolean isZone = false;
/* 349 */     boolean allowOptimizedFieldChange = StringUtils.convertToBool(props.getProperty("allowOptimizedFieldChange"), false);
/* 350 */     boolean isFieldSortable = true;
/* 351 */     boolean optimizedTextIsSortable = StringUtils.convertToBool(props.getProperty("optimizedTextIsSortable"), false);
/* 352 */     if ((optimizedTextIsSortable == true) && (isTextField == true))
/*     */     {
/* 354 */       isFieldSortable = StringUtils.convertToBool(props.getProperty("isOptimized"), false);
/*     */     }
/* 356 */     boolean supportDrillDownFields = StringUtils.convertToBool(props.getProperty("supportDrillDownFields"), false);
/*     */ 
/* 358 */     for (int i = 0; i < this.CHECKBOX_INFO.length; ++i)
/*     */     {
/* 360 */       String name = this.CHECKBOX_INFO[i][0];
/* 361 */       JCheckBox cmp = (JCheckBox)this.m_controlMap.get(this.CHECKBOX_INFO[i][0]);
/* 362 */       if (cmp == null)
/*     */         continue;
/* 364 */       if (name.equals("isZone"))
/*     */       {
/* 366 */         cmp.setEnabled(allowZoneConfiguration);
/*     */       }
/* 368 */       else if (name.equals("hasDataTable"))
/*     */       {
/* 370 */         cmp.setEnabled(allowZoneConfiguration);
/*     */       }
/* 372 */       else if (name.equalsIgnoreCase("isInSearchResult"))
/*     */       {
/* 374 */         cmp.setEnabled(allowReturnedFieldChange);
/*     */       }
/* 376 */       else if (name.equalsIgnoreCase("isOptimized"))
/*     */       {
/* 378 */         cmp.setEnabled(isTextField & allowOptimizedFieldChange);
/*     */       }
/* 380 */       else if (name.equalsIgnoreCase("IsSortable"))
/*     */       {
/* 382 */         cmp.setEnabled(isFieldSortable);
/*     */       } else {
/* 384 */         if (!name.equalsIgnoreCase("IsInSearchResultFilterCategory"))
/*     */           continue;
/* 386 */         cmp.setEnabled(supportDrillDownFields);
/*     */       }
/*     */     }
/*     */ 
/* 390 */     isZone = StringUtils.convertToBool(props.getProperty("isZone"), false);
/* 391 */     enableDisableZone(isZone);
/*     */   }
/*     */ 
/*     */   protected void enableDisableZone(boolean isEnabled)
/*     */   {
/* 396 */     for (int i = 0; i < this.ZONEBOX_INFO.length; ++i)
/*     */     {
/* 398 */       Component cmp = (Component)this.m_controlMap.get(this.ZONEBOX_INFO[i][0]);
/* 399 */       if (cmp == null)
/*     */         continue;
/* 401 */       cmp.setEnabled(isEnabled);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 408 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80281 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.AdvancedSearchOptionsDlg
 * JD-Core Version:    0.5.4
 */