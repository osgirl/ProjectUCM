/*     */ package intradoc.apputilities.systemproperties;
/*     */ 
/*     */ import intradoc.common.CommonLocalizationHandler;
/*     */ import intradoc.common.CommonLocalizationHandlerFactory;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ParseStringException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.localization.SharedLocalizationHandler;
/*     */ import intradoc.shared.localization.SharedLocalizationHandlerFactory;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JList;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditLocaleDialog
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected DialogHelper m_helper;
/*     */   public String m_id;
/*     */   public DataBinder m_systemConfig;
/*     */   public DataBinder m_customConfig;
/*     */   public Properties m_props;
/*     */   public Properties m_initialProps;
/*     */   public Hashtable m_fieldRsetMapping;
/*     */   protected JComboBox m_timeZoneChoice;
/*     */   protected List m_timeZoneIds;
/*     */ 
/*     */   public EditLocaleDialog(SystemInterface sysInt, String title)
/*     */   {
/*  85 */     this.m_systemInterface = sysInt;
/*  86 */     this.m_helper = new DialogHelper(sysInt, title, true, false);
/*     */   }
/*     */ 
/*     */   public void init(String id, DataBinder systemConfig, DataBinder customConfig)
/*     */   {
/*  91 */     this.m_id = id;
/*  92 */     this.m_systemConfig = systemConfig;
/*  93 */     this.m_customConfig = customConfig;
/*  94 */     this.m_props = new Properties();
/*  95 */     this.m_fieldRsetMapping = new Hashtable();
/*  96 */     this.m_timeZoneIds = new ArrayList();
/*     */ 
/*  98 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 105 */           return true;
/*     */         }
/*     */         catch (Exception exp) {
/*     */         }
/* 109 */         return false;
/*     */       }
/*     */     };
/* 114 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 2, false, null);
/*     */ 
/* 116 */     initUI(mainPanel);
/*     */   }
/*     */ 
/*     */   protected void initUI(JPanel panel)
/*     */   {
/* 121 */     int LOCALE_ID = 0;
/*     */ 
/* 123 */     int NAME = 0;
/* 124 */     int LABEL = 1;
/* 125 */     int CLASS = 2;
/* 126 */     int FLAGS = 3;
/* 127 */     String[][] fields = { { "lcLocaleId", "csEditLabelLocaleId", "javax.swing.JTextField", "j" }, { "lcIsEnabled", "csEditLabelLocaleEnabled", "javax.swing.JCheckBox", "n" }, { "lcLanguageId", "csEditLabelLanguageId", "javax.swing.JTextField", "" }, { "lcSearchLocale", "csEditLabelSearchLocale", "javax.swing.JComboBox", "" }, { "lcIsoEncoding", "csEditLabelEncoding", "javax.swing.JComboBox", "" }, { "lcDateTimeFormat", "csEditLabelDateFormat", "javax.swing.JTextField", "" }, { "lcDisplayDateFormat", "csEditLabelDisplayDateFormat", "javax.swing.JTextField", "" }, { "lcAlternateParseDateFormats", "csEditLabelAlternateParseDateFormats", "javax.swing.JTextField", "" }, { "lcTimeZone", "csEditLabelTimeZone", "javax.swing.JComboBox", "" } };
/*     */ 
/* 139 */     LocaleResources.localizeDoubleArray(fields, null, 1);
/* 140 */     Component[] components = new Component[fields.length];
/* 141 */     Hashtable componentMap = new Hashtable();
/*     */ 
/* 143 */     if (this.m_id != null)
/*     */     {
/* 145 */       fields[0][2] = "javax.swing.JLabel";
/*     */     }
/*     */ 
/* 148 */     mapResultSets(this.m_systemConfig);
/* 149 */     mapResultSets(this.m_customConfig);
/*     */ 
/* 151 */     this.m_initialProps = this.m_props;
/* 152 */     this.m_props = new Properties(this.m_initialProps);
/*     */ 
/* 154 */     for (int i = 0; i < fields.length; ++i)
/*     */     {
/*     */       try
/*     */       {
/* 158 */         Class cl = Class.forName(fields[i][2]);
/* 159 */         Component comp = (Component)cl.newInstance();
/* 160 */         components[i] = comp;
/* 161 */         if (comp instanceof JCheckBox)
/*     */         {
/* 163 */           JCheckBox box = (JCheckBox)comp;
/* 164 */           box.setText(fields[i][1]);
/*     */         }
/* 166 */         else if ((i == 0) && (comp instanceof JTextField))
/*     */         {
/* 168 */           ((JTextField)comp).setColumns(30);
/*     */         }
/* 170 */         String flags = fields[i][3];
/* 171 */         boolean noLabel = flags.indexOf("n") >= 0;
/* 172 */         boolean joinWithNext = flags.indexOf("j") >= 0;
/*     */ 
/* 174 */         String label = (noLabel) ? "" : fields[i][1];
/* 175 */         this.m_helper.addLabelFieldPairEx(panel, label, comp, fields[i][0], !joinWithNext);
/* 176 */         componentMap.put(fields[i][0], comp);
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 180 */         ignore.printStackTrace();
/*     */       }
/*     */     }
/*     */ 
/* 184 */     JComboBox verityLocaleChoice = (JComboBox)componentMap.get("lcSearchLocale");
/* 185 */     DataResultSet drset = SharedObjects.getTable("SearchLocaleConfig");
/* 186 */     updateChoices(verityLocaleChoice, drset, "lcSearchLocale", null);
/*     */ 
/* 188 */     JComboBox encodingChoice = (JComboBox)componentMap.get("lcIsoEncoding");
/* 189 */     drset = SharedObjects.getTable("IsoJavaEncodingMap");
/* 190 */     FieldInfo lcJavaEncodingInfo = new FieldInfo();
/* 191 */     drset.getFieldInfo("lcJavaEncoding", lcJavaEncodingInfo);
/* 192 */     ResultSetFilter encFilter = new ResultSetFilter(lcJavaEncodingInfo)
/*     */     {
/*     */       public int checkRow(String val, int curNumRows, Vector row)
/*     */       {
/* 196 */         String javaEnc = (String)row.elementAt(this.val$lcJavaEncodingInfo.m_index);
/*     */         try
/*     */         {
/* 199 */           StringUtils.getBytes("Test String", javaEnc);
/* 200 */           return 1;
/*     */         }
/*     */         catch (UnsupportedEncodingException ignore)
/*     */         {
/* 204 */           Report.trace("system", "Encoding " + val + " not supported.", ignore);
/* 205 */         }return 0;
/*     */       }
/*     */     };
/* 209 */     updateChoices(encodingChoice, drset, "lcIsoEncoding", encFilter);
/*     */ 
/* 211 */     this.m_timeZoneChoice = ((JComboBox)componentMap.get("lcTimeZone"));
/* 212 */     SharedLocalizationHandler slh = SharedLocalizationHandlerFactory.createInstance();
/* 213 */     drset = slh.getTimeZones(null);
/* 214 */     slh.prepareTimeZonesForDisplay(drset, null, 3);
/*     */     try
/*     */     {
/* 217 */       FieldInfo[] zoneFields = ResultSetUtils.createInfoList(drset, new String[] { "lcTimeZone", "lcLabel" }, true);
/*     */ 
/* 219 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 221 */         String id = drset.getStringValue(zoneFields[0].m_index);
/* 222 */         String label = drset.getStringValue(zoneFields[1].m_index);
/* 223 */         this.m_timeZoneChoice.addItem(label);
/* 224 */         this.m_timeZoneIds.add(id);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public void mapResultSets(DataBinder binder)
/*     */   {
/* 235 */     Enumeration en = binder.getResultSetList();
/* 236 */     while (en.hasMoreElements())
/*     */     {
/* 238 */       String name = (String)en.nextElement();
/* 239 */       DataResultSet drset = (DataResultSet)binder.getResultSet(name);
/* 240 */       int size = drset.getNumFields();
/* 241 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 243 */         String fieldName = drset.getFieldName(i);
/* 244 */         Vector list = (Vector)this.m_fieldRsetMapping.get(fieldName);
/* 245 */         if (list == null)
/*     */         {
/* 247 */           list = new IdcVector();
/* 248 */           this.m_fieldRsetMapping.put(fieldName, list);
/*     */         }
/*     */ 
/* 251 */         if ((this.m_id != null) && (fieldName.equals("lcLocaleId")) && 
/* 253 */           (drset.findRow(i, this.m_id) != null))
/*     */         {
/* 255 */           for (int j = 0; j < size; ++j)
/*     */           {
/* 257 */             fieldName = drset.getFieldName(j);
/* 258 */             String value = drset.getStringValue(j);
/* 259 */             this.m_props.put(fieldName, value);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 264 */         Object[] info = { binder, drset };
/* 265 */         list.addElement(info);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateResults(String resultSetName)
/*     */   {
/* 272 */     String localeId = this.m_props.getProperty("lcLocaleId");
/* 273 */     DataResultSet localeConfig = (DataResultSet)this.m_systemConfig.getResultSet(resultSetName);
/* 274 */     boolean isCustomLocale = !findRow(localeConfig, "lcLocaleId", localeId);
/* 275 */     if (isCustomLocale)
/*     */     {
/* 277 */       localeConfig = (DataResultSet)this.m_customConfig.getResultSet(resultSetName);
/* 278 */       if (localeConfig == null)
/*     */       {
/* 280 */         localeConfig = (DataResultSet)this.m_systemConfig.getResultSet(resultSetName);
/* 281 */         DataResultSet tmp = new DataResultSet();
/* 282 */         tmp.copy(localeConfig, 1);
/* 283 */         tmp.removeAll();
/* 284 */         localeConfig = tmp;
/* 285 */         this.m_customConfig.addResultSet(resultSetName, localeConfig);
/*     */       }
/* 287 */       boolean isUpdate = findRow(localeConfig, "lcLocaleId", localeId);
/*     */       Vector values;
/* 289 */       if (!isUpdate)
/*     */       {
/* 291 */         Vector values = localeConfig.createEmptyRow();
/* 292 */         localeConfig.addRow(values);
/*     */       }
/*     */       else
/*     */       {
/* 296 */         values = localeConfig.getCurrentRowValues();
/*     */       }
/* 298 */       int size = values.size();
/* 299 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 301 */         String name = localeConfig.getFieldName(i);
/*     */         String value;
/*     */         String value;
/* 303 */         if (name.equals("lcTimeZone"))
/*     */         {
/* 305 */           int selectedIndex = this.m_timeZoneChoice.getSelectedIndex();
/* 306 */           value = (String)this.m_timeZoneIds.get(selectedIndex);
/*     */         }
/*     */         else
/*     */         {
/* 310 */           value = this.m_props.getProperty(name);
/*     */         }
/*     */ 
/* 313 */         if (value == null)
/*     */           continue;
/* 315 */         values.setElementAt(value, i);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 321 */       Vector values = localeConfig.getCurrentRowValues();
/* 322 */       int size = values.size();
/* 323 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 325 */         String name = localeConfig.getFieldName(i);
/*     */         String newValue;
/*     */         String newValue;
/* 327 */         if (name.equals("lcTimeZone"))
/*     */         {
/* 329 */           int selectedIndex = this.m_timeZoneChoice.getSelectedIndex();
/* 330 */           newValue = (String)this.m_timeZoneIds.get(selectedIndex);
/*     */         }
/*     */         else
/*     */         {
/* 334 */           newValue = this.m_props.getProperty(name);
/*     */         }
/*     */ 
/* 337 */         String oldValue = this.m_initialProps.getProperty(name);
/* 338 */         if (!newValue.equals(oldValue)) {
/* 339 */           DataResultSet drset = (DataResultSet)this.m_customConfig.getResultSet(resultSetName + "_" + name);
/*     */ 
/* 341 */           if (drset == null)
/*     */           {
/* 343 */             drset = new DataResultSet(new String[] { "lcLocaleId", name });
/* 344 */             this.m_customConfig.addResultSet(resultSetName + "_" + name, drset);
/*     */           }
/*     */ 
/* 347 */           boolean isUpdate = findRow(drset, "lcLocaleId", localeId);
/*     */           Vector currentRow;
/*     */           Vector currentRow;
/* 349 */           if (isUpdate)
/*     */           {
/* 351 */             currentRow = drset.getCurrentRowValues();
/*     */           }
/*     */           else
/*     */           {
/* 355 */             currentRow = drset.createEmptyRow();
/* 356 */             FieldInfo info = new FieldInfo();
/* 357 */             drset.getFieldInfo("lcLocaleId", info);
/* 358 */             currentRow.setElementAt(localeId, info.m_index);
/* 359 */             drset.addRow(currentRow);
/*     */           }
/* 361 */           FieldInfo info = new FieldInfo();
/* 362 */           drset.getFieldInfo(name, info);
/* 363 */           currentRow.setElementAt(newValue, info.m_index);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean findRow(DataResultSet drset, String column, String value) {
/* 370 */     FieldInfo info = new FieldInfo();
/* 371 */     drset.getFieldInfo(column, info);
/* 372 */     boolean found = drset.findRow(info.m_index, value) != null;
/* 373 */     return found;
/*     */   }
/*     */ 
/*     */   protected void updateChoices(JComboBox choice, DataResultSet drset, String column, ResultSetFilter filter)
/*     */   {
/* 379 */     FieldInfo info = new FieldInfo();
/* 380 */     drset.getFieldInfo(column, info);
/* 381 */     Hashtable usedItems = new Hashtable();
/* 382 */     int count = 0;
/* 383 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 385 */       String value = drset.getStringValue(info.m_index);
/* 386 */       if (usedItems.get(value) == null) {
/* 387 */         usedItems.put(value, value);
/* 388 */         if (filter != null)
/*     */         {
/* 390 */           int rc = filter.checkRow(value, count++, drset.getRowValues(drset.getCurrentRow()));
/* 391 */           if (rc == -1) return;
/* 392 */           if (rc == 0) continue;
/*     */         }
/* 394 */         choice.addItem(value);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public int prompt() {
/* 400 */     int rc = this.m_helper.prompt();
/* 401 */     if (rc == 1)
/*     */     {
/* 403 */       updateResults("LocaleConfig");
/* 404 */       updateResults("SearchLocaleConfig");
/*     */     }
/* 406 */     return rc;
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 411 */     boolean isEmptyVal = (exchange.m_compValue == null) || (exchange.m_compValue.length() == 0);
/* 412 */     boolean isMatched = false;
/* 413 */     if (exchange.m_compName.equals("lcLocaleId"))
/*     */     {
/* 415 */       isMatched = true;
/* 416 */       if (isEmptyVal)
/*     */       {
/* 418 */         exchange.m_errorMessage = IdcMessageFactory.lc("csLocaleIdMustBeSpecified", new Object[0]);
/* 419 */         return false;
/*     */       }
/*     */     }
/* 422 */     if (!isMatched)
/*     */     {
/* 424 */       String[] dateFields = { "lcDateTimeFormat", "lcDisplayDateFormat", "lcAlternateParseDateFormats" };
/* 425 */       for (int i = 0; (i < dateFields.length) && (!isMatched); ++i)
/*     */       {
/* 427 */         if (!exchange.m_compName.equals(dateFields[i]))
/*     */           continue;
/* 429 */         isMatched = true;
/* 430 */         if ((i == 0) && (isEmptyVal))
/*     */         {
/* 432 */           exchange.m_errorMessage = IdcMessageFactory.lc("csDateFormatMustBeSpecified", new Object[0]);
/* 433 */           return false;
/*     */         }
/* 435 */         if (isEmptyVal)
/*     */           continue;
/* 437 */         IdcDateFormat test = new IdcDateFormat();
/*     */         try
/*     */         {
/* 440 */           test.init(exchange.m_compValue);
/*     */         }
/*     */         catch (ParseStringException e)
/*     */         {
/* 444 */           String f = dateFields[i].substring(2);
/*     */ 
/* 446 */           exchange.m_errorMessage = IdcMessageFactory.lc("csDateUnableToParse", new Object[] { f });
/* 447 */           return false;
/*     */         }
/* 449 */         if ((i != 0) || (!test.doesFormatUseLocalizedStrings()))
/*     */           continue;
/* 451 */         exchange.m_errorMessage = IdcMessageFactory.lc("csDateFormatCannotUseLocalizedStrings", new Object[0]);
/* 452 */         return false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 460 */     return true;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 465 */     if (updateComponent)
/*     */     {
/* 467 */       String value = this.m_props.getProperty(exchange.m_compName);
/* 468 */       if (value != null)
/*     */       {
/* 470 */         exchange.m_compValue = value;
/*     */       }
/*     */ 
/* 473 */       if ((value != null) && (exchange.m_compName.equals("lcTimeZone")))
/*     */       {
/* 475 */         CommonLocalizationHandler clh = CommonLocalizationHandlerFactory.createInstance();
/* 476 */         value = clh.getTimeZoneDisplayName(value, 3, null);
/*     */       }
/*     */ 
/* 479 */       if ((value != null) && (exchange.m_component instanceof JList))
/*     */       {
/* 481 */         JList list = (JList)exchange.m_component;
/* 482 */         DefaultListModel model = (DefaultListModel)list.getModel();
/*     */ 
/* 484 */         int size = model.getSize();
/* 485 */         boolean found = false;
/* 486 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 488 */           String item = (String)model.getElementAt(i);
/* 489 */           if (!item.equals(value))
/*     */             continue;
/* 491 */           list.setSelectedIndex(i);
/* 492 */           found = true;
/* 493 */           break;
/*     */         }
/*     */ 
/* 496 */         if (!found)
/*     */         {
/* 498 */           model.addElement(value);
/* 499 */           list.setSelectedIndex(size);
/*     */         }
/*     */       }
/* 502 */       else if ((value != null) && (exchange.m_component instanceof JComboBox))
/*     */       {
/* 504 */         JComboBox choice = (JComboBox)exchange.m_component;
/* 505 */         int size = choice.getItemCount();
/* 506 */         boolean found = false;
/* 507 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 509 */           String item = (String)choice.getItemAt(i);
/* 510 */           if (!item.equals(value))
/*     */             continue;
/* 512 */           choice.setSelectedIndex(i);
/* 513 */           found = true;
/* 514 */           break;
/*     */         }
/*     */ 
/* 517 */         if (!found)
/*     */         {
/* 519 */           choice.addItem(value);
/* 520 */           choice.setSelectedIndex(size);
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 526 */       String value = exchange.m_compValue;
/* 527 */       if (exchange.m_compName.equals("lcTimeZone"))
/*     */       {
/* 529 */         int selectedIndex = this.m_timeZoneChoice.getSelectedIndex();
/* 530 */         value = (String)this.m_timeZoneIds.get(selectedIndex);
/*     */       }
/*     */ 
/* 533 */       this.m_props.put(exchange.m_compName, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 539 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78559 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.EditLocaleDialog
 * JD-Core Version:    0.5.4
 */