/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AdvancedSearchDesignDlg
/*     */   implements ActionListener, DisplayStringCallback
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_context;
/*     */   protected SharedContext m_shContext;
/*     */   protected String m_helpPage;
/*  66 */   protected DataBinder m_binder = null;
/*  67 */   protected DataBinder m_Oldbinder = null;
/*     */ 
/*  69 */   protected DataResultSet m_fieldSet = null;
/*     */ 
/*  71 */   protected UdlPanel m_fieldList = null;
/*     */ 
/*     */   public AdvancedSearchDesignDlg(SystemInterface sys, String title, SharedContext shContext, String helpPage)
/*     */   {
/*  76 */     this.m_systemInterface = sys;
/*  77 */     this.m_context = sys.getExecutionContext();
/*  78 */     this.m_shContext = shContext;
/*     */ 
/*  80 */     title = LocaleResources.localizeMessage(title, this.m_context);
/*  81 */     this.m_helper = new DialogHelper(sys, title, true);
/*  82 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(DataBinder binder)
/*     */   {
/*  87 */     this.m_binder = binder;
/*     */ 
/*  89 */     this.m_Oldbinder = new DataBinder();
/*  90 */     this.m_Oldbinder.merge(binder);
/*     */ 
/*  92 */     this.m_helper.m_props = binder.getLocalData();
/*  93 */     this.m_fieldSet = ((DataResultSet)this.m_binder.getResultSet("SearchFieldOptions"));
/*     */ 
/*  95 */     ColumnInfo sortInfo = initUI();
/*  96 */     this.m_fieldList.refreshList(this.m_fieldSet, null);
/*  97 */     this.m_fieldList.setSort(sortInfo, false);
/*  98 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public ColumnInfo initUI()
/*     */   {
/* 103 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/* 106 */     ColumnInfo sortInfo = initList();
/*     */ 
/* 108 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 115 */           boolean isRebuildNeeded = false;
/* 116 */           isRebuildNeeded = AdvancedSearchDesignDlg.this.isCollectionRebuildNeeded(AdvancedSearchDesignDlg.this.m_binder, AdvancedSearchDesignDlg.this.m_Oldbinder);
/*     */ 
/* 118 */           AdvancedSearchDesignDlg.this.m_shContext.executeService("UPDATE_ADVANCED_SEARCH_OPTIONS", AdvancedSearchDesignDlg.this.m_binder, false);
/*     */ 
/* 120 */           if (isRebuildNeeded == true)
/*     */           {
/* 122 */             SharedObjects.putEnvironmentValue("IndexCollectionSynced", "false");
/*     */           }
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 127 */           MessageBox.reportError(AdvancedSearchDesignDlg.this.m_systemInterface, exp, IdcMessageFactory.lc("apErrorUpdatingSearchDesign", new Object[0]));
/* 128 */           return false;
/*     */         }
/* 130 */         return true;
/*     */       }
/*     */     };
/* 133 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/*     */ 
/* 135 */     this.m_helper.m_okCallback = okCallback;
/* 136 */     this.m_helper.m_okCallback.m_dlgHelper = this.m_helper;
/*     */ 
/* 138 */     JButton editButton = this.m_helper.addCommandButton(this.m_systemInterface.getString("apDlgButtonEdit"), this);
/*     */ 
/* 140 */     editButton.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableEditAdvancedOptions", this.m_context));
/* 141 */     editButton.setActionCommand("edit");
/* 142 */     editButton.setEnabled(false);
/* 143 */     this.m_fieldList.addControlComponent(editButton);
/*     */ 
/* 145 */     this.m_helper.addOK(okCallback);
/* 146 */     this.m_helper.addCancel(null);
/* 147 */     this.m_helper.addHelpInfo(this.m_helpPage);
/* 148 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 151 */     String msg = this.m_systemInterface.getString("apAdvSearchDesignDesc");
/* 152 */     this.m_helper.addLastComponentInRow(mainPanel, new CustomText(msg, 100));
/*     */ 
/* 154 */     gh.m_gc.weightx = 1.0D;
/* 155 */     gh.m_gc.weighty = 1.0D;
/* 156 */     this.m_helper.addLastComponentInRow(mainPanel, this.m_fieldList);
/*     */ 
/* 159 */     String allowZoneConfigStr = this.m_binder.getLocal("allowZoneConfig");
/* 160 */     boolean allowZoneConfig = StringUtils.convertToBool(allowZoneConfigStr, false);
/* 161 */     if (allowZoneConfig)
/*     */     {
/* 163 */       JCheckBox box = new CustomCheckbox(this.m_systemInterface.getString("apEnableZoneQuickSearch"));
/* 164 */       gh.m_gc.weightx = 0.0D;
/* 165 */       gh.m_gc.weighty = 0.0D;
/* 166 */       gh.prepareAddLastRowElement();
/* 167 */       this.m_helper.addExchangeComponent(mainPanel, box, "isZoneQuickSearch");
/*     */     }
/*     */ 
/* 170 */     return sortInfo;
/*     */   }
/*     */ 
/*     */   public boolean isCollectionRebuildNeeded(DataBinder newBinder, DataBinder oldBinder)
/*     */   {
/* 183 */     boolean isRebuildNeeded = false;
/*     */ 
/* 185 */     boolean allowOptimizedFieldChange = StringUtils.convertToBool(this.m_helper.m_props.getProperty("allowOptimizedFieldChange"), false);
/*     */ 
/* 187 */     boolean supportDrillDownFields = StringUtils.convertToBool(this.m_helper.m_props.getProperty("supportDrillDownFields"), false);
/*     */ 
/* 191 */     DataResultSet newSearchDesign = (DataResultSet)newBinder.getResultSet("SearchFieldOptions");
/* 192 */     DataResultSet oldSearchDesign = (DataResultSet)oldBinder.getResultSet("SearchFieldOptions");
/*     */ 
/* 194 */     if ((newSearchDesign == null) || (oldSearchDesign == null))
/*     */     {
/* 196 */       return false;
/*     */     }
/*     */ 
/* 199 */     FieldInfo fiFieldName = new FieldInfo();
/* 200 */     oldSearchDesign.getFieldInfo("fieldName", fiFieldName);
/*     */ 
/* 202 */     FieldInfo fiAdvOptions = new FieldInfo();
/* 203 */     oldSearchDesign.getFieldInfo("advOptions", fiAdvOptions);
/*     */ 
/* 206 */     for (oldSearchDesign.first(); oldSearchDesign.next(); )
/*     */     {
/* 208 */       Vector oldFieldDesignRow = oldSearchDesign.getCurrentRowValues();
/* 209 */       String fieldName = (String)oldFieldDesignRow.get(fiFieldName.m_index);
/* 210 */       Vector newFieldDesignRow = newSearchDesign.findRow(fiFieldName.m_index, fieldName);
/*     */ 
/* 212 */       if (newFieldDesignRow == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 217 */       Properties oldFieldProperties = new Properties();
/* 218 */       Properties newFieldProperties = new Properties();
/*     */ 
/* 220 */       AdvancedSearchOptionsDlg.createPropertiesFromString((String)oldFieldDesignRow.get(fiAdvOptions.m_index), oldFieldProperties);
/* 221 */       AdvancedSearchOptionsDlg.createPropertiesFromString((String)newFieldDesignRow.get(fiAdvOptions.m_index), newFieldProperties);
/*     */ 
/* 223 */       boolean oldFieldOptimizedProperty = StringUtils.convertToBool((String)oldFieldProperties.get("isOptimized"), false);
/* 224 */       boolean newFieldOptimizedProperty = StringUtils.convertToBool((String)newFieldProperties.get("isOptimized"), false);
/*     */ 
/* 226 */       boolean oldFieldFilterProperty = StringUtils.convertToBool((String)oldFieldProperties.get("IsInSearchResultFilterCategory"), false);
/* 227 */       boolean newFieldFilterProperty = StringUtils.convertToBool((String)newFieldProperties.get("IsInSearchResultFilterCategory"), false);
/*     */ 
/* 229 */       if (oldFieldOptimizedProperty != newFieldOptimizedProperty)
/*     */       {
/* 231 */         isRebuildNeeded = true;
/*     */ 
/* 234 */         if (allowOptimizedFieldChange != true)
/*     */           break;
/* 236 */         this.m_binder.putLocal("isRebuildNeeded", Boolean.toString(isRebuildNeeded)); break;
/*     */       }
/*     */ 
/* 241 */       if (oldFieldFilterProperty != newFieldFilterProperty)
/*     */       {
/* 243 */         isRebuildNeeded = true;
/*     */ 
/* 246 */         if (supportDrillDownFields != true)
/*     */           break;
/* 248 */         this.m_binder.putLocal("isMetaRebuildNeeded", Boolean.toString(isRebuildNeeded)); break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 255 */     return isRebuildNeeded;
/*     */   }
/*     */ 
/*     */   protected ColumnInfo initList()
/*     */   {
/* 260 */     String columns = "fieldName,dCaption,advOptions";
/* 261 */     this.m_fieldList = new UdlPanel(this.m_systemInterface.getString("apLabelFields"), null, 500, 20, "SearchFieldOptions", true);
/*     */ 
/* 264 */     ColumnInfo sortInfo = new ColumnInfo(this.m_systemInterface.getString("apTitleName"), "fieldName", 10.0D);
/* 265 */     this.m_fieldList.setColumnInfo(sortInfo);
/* 266 */     ColumnInfo info = new ColumnInfo(this.m_systemInterface.getString("apTitleCaption"), "dCaption", 10.0D);
/* 267 */     this.m_fieldList.setColumnInfo(info);
/* 268 */     info = new ColumnInfo(this.m_systemInterface.getString("apTitleSearchOptions"), "advOptions", 20.0D);
/* 269 */     this.m_fieldList.setColumnInfo(info);
/*     */ 
/* 272 */     this.m_fieldList.setVisibleColumns(columns);
/* 273 */     this.m_fieldList.setIDColumn("fieldName");
/* 274 */     this.m_fieldList.setStateColumn("isInSync");
/* 275 */     this.m_fieldList.init();
/* 276 */     this.m_fieldList.useDefaultListener();
/* 277 */     this.m_fieldList.setDisplayCallback("advOptions", this);
/* 278 */     this.m_fieldList.setDisplayCallback("dCaption", this);
/*     */ 
/* 280 */     ActionListener listener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 284 */         AdvancedSearchDesignDlg.this.editFieldOptions();
/*     */       }
/*     */     };
/* 287 */     this.m_fieldList.m_list.addActionListener(listener);
/* 288 */     return sortInfo;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 296 */     editFieldOptions();
/*     */   }
/*     */ 
/*     */   protected void editFieldOptions()
/*     */   {
/* 301 */     int index = this.m_fieldList.getSelectedIndex();
/* 302 */     if (index < 0)
/*     */     {
/* 304 */       return;
/*     */     }
/*     */ 
/* 307 */     Properties props = new Properties(this.m_binder.getLocalData());
/* 308 */     Properties tmpProps = this.m_fieldList.getDataAt(index);
/* 309 */     props.putAll(tmpProps);
/* 310 */     String fieldName = props.getProperty("fieldName");
/* 311 */     String helpPage = DialogHelpTable.getHelpPage("AdvancedSearchOptions");
/* 312 */     String title = LocaleUtils.encodeMessage("apAdvancedSearchOptionsTitle", null, fieldName);
/* 313 */     title = LocaleResources.localizeMessage(title, this.m_context);
/*     */ 
/* 315 */     AdvancedSearchOptionsDlg dlg = new AdvancedSearchOptionsDlg(this.m_systemInterface, title, this.m_shContext, helpPage);
/*     */ 
/* 317 */     int result = dlg.init(props);
/* 318 */     if (result != 1)
/*     */       return;
/*     */     try
/*     */     {
/* 322 */       PropParameters params = new PropParameters(props);
/* 323 */       Vector row = this.m_fieldSet.findRow(0, fieldName);
/* 324 */       if (row != null)
/*     */       {
/* 326 */         index = this.m_fieldSet.getCurrentRow();
/* 327 */         row = this.m_fieldSet.createRow(params);
/* 328 */         this.m_fieldSet.setRowValues(row, index);
/*     */ 
/* 330 */         this.m_fieldList.refreshList(this.m_fieldSet, fieldName);
/*     */       }
/*     */     }
/*     */     catch (DataException exp)
/*     */     {
/* 335 */       MessageBox.reportError(this.m_systemInterface, exp, IdcMessageFactory.lc("apErrorUpdatingOptions", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */   {
/* 345 */     if (name.equals("dCaption"))
/*     */     {
/* 347 */       if (value.length() == 0)
/*     */       {
/* 349 */         value = (String)row.elementAt(0);
/*     */       }
/*     */       else
/*     */       {
/* 353 */         value = this.m_systemInterface.getString(value);
/*     */       }
/*     */     }
/* 356 */     else if (name.equals("advOptions"))
/*     */     {
/* 358 */       StringBuffer buff = new StringBuffer();
/* 359 */       Vector v = StringUtils.parseArray(value, ',', '^');
/* 360 */       int size = v.size();
/* 361 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 363 */         String key = (String)v.elementAt(i);
/* 364 */         ++i;
/* 365 */         String val = (String)v.elementAt(i);
/* 366 */         boolean isVal = StringUtils.convertToBool(val, false);
/* 367 */         if (key.equals("isInSearchResult"))
/*     */         {
/* 369 */           isVal = StringUtils.convertToBool(val, true);
/* 370 */           if (!isVal) {
/*     */             continue;
/*     */           }
/*     */         }
/*     */ 
/* 375 */         if (!isVal)
/*     */           continue;
/* 377 */         if (buff.length() > 0)
/*     */         {
/* 379 */           buff.append(",");
/*     */         }
/* 381 */         buff.append(key);
/*     */       }
/*     */ 
/* 385 */       if (value.length() == 0)
/*     */       {
/* 388 */         buff.append("isInSearchResult");
/*     */       }
/* 390 */       value = buff.toString();
/*     */     }
/* 392 */     return value;
/*     */   }
/*     */ 
/*     */   public String createExtendedDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */   {
/* 397 */     if (name.indexOf("advOptions") >= 0)
/*     */     {
/* 399 */       if (value == null)
/*     */       {
/* 401 */         return "";
/*     */       }
/*     */ 
/* 404 */       StringBuffer buff = new StringBuffer();
/* 405 */       Vector v = StringUtils.parseArray(value, ',', '^');
/* 406 */       int size = v.size();
/* 407 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 409 */         String key = (String)v.elementAt(i);
/* 410 */         ++i;
/* 411 */         String val = (String)v.elementAt(i);
/* 412 */         boolean isVal = StringUtils.convertToBool(val, false);
/* 413 */         if (key.equals("isInSearchResult"))
/*     */         {
/* 415 */           isVal = StringUtils.convertToBool(val, true);
/*     */         }
/*     */ 
/* 418 */         if (!isVal)
/*     */           continue;
/* 420 */         String str = "apIndex_" + key;
/* 421 */         str = this.m_systemInterface.getString(str);
/* 422 */         if (buff.length() > 0)
/*     */         {
/* 424 */           buff.append("\n");
/*     */         }
/* 426 */         buff.append(str);
/*     */       }
/*     */ 
/* 429 */       if (buff.length() == 0)
/*     */       {
/* 432 */         buff.append(this.m_systemInterface.getString("apIndex_isInSearchResult"));
/*     */       }
/*     */ 
/* 435 */       return buff.toString();
/*     */     }
/* 437 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 442 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.AdvancedSearchDesignDlg
 * JD-Core Version:    0.5.4
 */