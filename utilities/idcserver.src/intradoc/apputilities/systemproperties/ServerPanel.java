/*     */ package intradoc.apputilities.systemproperties;
/*     */ 
/*     */ import intradoc.common.CommonLocalizationHandler;
/*     */ import intradoc.common.CommonLocalizationHandlerFactory;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomPasswordField;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.LocaleLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.localization.SharedLocalizationHandler;
/*     */ import intradoc.shared.localization.SharedLocalizationHandlerFactory;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.text.DateFormat;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ import java.util.Properties;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JPasswordField;
/*     */ 
/*     */ public class ServerPanel extends SystemPropertiesPanel
/*     */   implements Observer
/*     */ {
/*     */   protected String m_defaultLocale;
/*     */   protected String m_defaultTzMsg;
/*     */   protected JComboBox m_localeChoice;
/*     */   protected JComboBox m_tzChoice;
/*     */   protected Vector m_tzValues;
/*     */   protected Properties m_tzValueLabelMap;
/*     */   protected TimeZone m_baseTimeZone;
/*     */   protected long m_baseTimeZoneOffset;
/*     */ 
/*     */   public ServerPanel()
/*     */   {
/*  76 */     this.m_localeChoice = null;
/*     */ 
/*  78 */     this.m_tzValues = new IdcVector();
/*  79 */     this.m_tzValueLabelMap = new Properties();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/*  87 */     if (this.m_sysInterface instanceof SystemPropertiesFrame)
/*     */     {
/*  89 */       ((SystemPropertiesFrame)this.m_sysInterface).addLocaleObserver(this);
/*     */     }
/*     */ 
/*  93 */     JPanel infoPanel = new CustomPanel();
/*  94 */     this.m_helper.makePanelGridBag(infoPanel, 1);
/*  95 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/*     */ 
/*  98 */     this.m_helper.addPanelTitle(infoPanel, LocaleResources.getString("csServerPanelTitle", null));
/*  99 */     gridBag.m_gc.weighty = 0.0D;
/* 100 */     gridBag.prepareAddLastRowElement();
/* 101 */     this.m_helper.addComponent(infoPanel, new CustomLabel(""));
/*     */ 
/* 103 */     gridBag.prepareAddLastRowElement(18);
/*     */ 
/* 106 */     JPanel subPanel = addNewSubPanel(infoPanel, 1);
/*     */ 
/* 108 */     Component component = this.m_localeChoice = new JComboBox();
/* 109 */     this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csServerPanelLabelSystemLocale", null), component, "SystemLocale");
/*     */ 
/* 112 */     component = this.m_tzChoice = new JComboBox();
/* 113 */     this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csServerPanelLabelSystemTimeZone", null), component, "SystemTimeZone");
/*     */ 
/* 116 */     DataResultSet drset = SharedObjects.getTable("LocaleConfig");
/* 117 */     this.m_baseTimeZone = LocaleResources.getSystemTimeZone();
/* 118 */     if (this.m_baseTimeZone == null)
/*     */     {
/* 120 */       DateFormat fmt = DateFormat.getInstance();
/* 121 */       this.m_baseTimeZone = fmt.getTimeZone();
/*     */     }
/* 123 */     this.m_baseTimeZoneOffset = this.m_baseTimeZone.getRawOffset();
/* 124 */     refreshLocaleAndTimeZoneChoice(drset);
/*     */ 
/* 126 */     component = new CustomTextField(15);
/* 127 */     this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csServerPanelLabelInstanceMenu", null), component, "InstanceMenuLabel");
/*     */ 
/* 129 */     component = new CustomTextArea(3, 50);
/* 130 */     this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csServerPanelLabelInstanceDesc", null), component, "InstanceDescription");
/*     */ 
/* 135 */     subPanel = addNewSubPanel(infoPanel, 1);
/*     */ 
/* 137 */     if (StringUtils.convertToBool(this.m_helper.m_props.getProperty("AlwaysReverseLookupForHost"), false))
/*     */     {
/* 140 */       component = new CustomTextField(50);
/* 141 */       this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csServerPanelLabelHostnameFilter", null), component, "SocketHostNameSecurityFilter");
/*     */     }
/*     */ 
/* 145 */     component = new CustomTextField(50);
/* 146 */     if (this.m_helper.m_props.get("SocketHostAddressSecurityFilter") == null)
/*     */     {
/* 148 */       this.m_helper.m_props.put("SocketHostAddressSecurityFilter", "127.0.0.1|::1");
/*     */     }
/* 150 */     this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csServerPanelLabelIPAddressFilter", null), component, "SocketHostAddressSecurityFilter");
/*     */ 
/* 153 */     JPasswordField password = new CustomPasswordField(50);
/* 154 */     this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csServerPanelLabelProxyPassword", null), password, "ProxyPassword");
/*     */ 
/* 157 */     JPasswordField confirmPassword = new CustomPasswordField(50);
/* 158 */     this.m_helper.addLabelFieldPair(subPanel, LocaleResources.getString("csServerPanelLabelConfirmPassword", null), confirmPassword, "confirmPassword");
/*     */ 
/* 161 */     gridBag.m_gc.weighty = 1.0D;
/*     */ 
/* 164 */     setLayout(new BorderLayout());
/* 165 */     add("Center", infoPanel);
/*     */ 
/* 167 */     if (this.m_isRefinery)
/*     */       return;
/* 169 */     JPanel mPanel = addNewSubPanel(infoPanel, 1);
/* 170 */     CustomCheckbox jspCheckbox = new CustomCheckbox(LocaleResources.getString("csServerPanelCheckboxJspEnable", null));
/*     */ 
/* 172 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/* 173 */     this.m_helper.addExchangeComponent(mPanel, jspCheckbox, "IsJspServerEnabled");
/*     */ 
/* 175 */     CustomTextField jspField = new CustomTextField(40);
/* 176 */     this.m_helper.addLabelFieldPair(mPanel, LocaleResources.getString("csServerPanelLabelJspGroups", null), jspField, "JspEnabledGroups");
/*     */ 
/* 179 */     boolean isJspEnabled = StringUtils.convertToBool(this.m_helper.m_props.getProperty("IsJspServerEnabled"), false);
/*     */ 
/* 182 */     if (!isJspEnabled)
/*     */     {
/* 184 */       jspField.setEnabled(false);
/*     */     }
/*     */ 
/* 188 */     ItemListener jspListener = new ItemListener(jspCheckbox, jspField)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 192 */         boolean jspEnabled = this.val$jspCheckbox.isSelected();
/* 193 */         this.val$jspField.setEnabled(jspEnabled);
/*     */       }
/*     */     };
/* 197 */     jspCheckbox.addItemListener(jspListener);
/*     */   }
/*     */ 
/*     */   public void update(Observable obs, Object obj)
/*     */   {
/* 203 */     if (!obj instanceof DataBinder)
/*     */       return;
/* 205 */     DataBinder binder = (DataBinder)obj;
/* 206 */     DataResultSet locales = (DataResultSet)binder.getResultSet("LocaleConfig");
/* 207 */     refreshLocaleAndTimeZoneChoice(locales);
/*     */   }
/*     */ 
/*     */   public void refreshLocaleAndTimeZoneChoice(DataResultSet locales)
/*     */   {
/* 213 */     if (this.m_localeChoice == null)
/*     */     {
/* 215 */       return;
/*     */     }
/*     */ 
/* 218 */     DataResultSet dirMap = SharedObjects.getTable("LanguageDirectionMap");
/* 219 */     String osLocale = LocaleLoader.determineDefaultLocale();
/* 220 */     this.m_defaultLocale = LocaleResources.getString("csServerPanelUnspecifiedLocale", null, osLocale);
/*     */ 
/* 222 */     String selected = (String)this.m_localeChoice.getSelectedItem();
/* 223 */     this.m_localeChoice.removeAllItems();
/* 224 */     this.m_localeChoice.addItem(this.m_defaultLocale);
/* 225 */     for (locales.first(); locales.isRowPresent(); locales.next())
/*     */     {
/*     */       try
/*     */       {
/* 229 */         Properties props = locales.getCurrentRowProps();
/* 230 */         String langId = props.getProperty("lcLanguageId");
/* 231 */         String direction = ResultSetUtils.findValue(dirMap, "lcLanguageId", langId, "lcDirection");
/* 232 */         if ((direction == null) || (direction.equalsIgnoreCase("ltr")))
/*     */         {
/* 234 */           String id = props.getProperty("lcLocaleId");
/* 235 */           this.m_localeChoice.addItem(id);
/*     */         }
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*     */       }
/*     */     }
/*     */ 
/* 243 */     if (selected != null)
/*     */     {
/* 245 */       this.m_localeChoice.setSelectedItem(selected);
/*     */     }
/* 247 */     selected = (String)this.m_localeChoice.getSelectedItem();
/*     */ 
/* 250 */     SharedLocalizationHandler slh = SharedLocalizationHandlerFactory.createInstance();
/* 251 */     DataResultSet timezones = slh.getTimeZones(null);
/* 252 */     slh.prepareTimeZonesForDisplay(timezones, null, 3);
/*     */ 
/* 261 */     CommonLocalizationHandler clh = CommonLocalizationHandlerFactory.createInstance();
/* 262 */     String defaultTz = TimeZone.getDefault().getID();
/* 263 */     TimeZone theDefaultTz = LocaleResources.getTimeZone(defaultTz, null);
/* 264 */     if (theDefaultTz == null)
/*     */     {
/* 266 */       theDefaultTz = TimeZone.getDefault();
/*     */     }
/* 268 */     selected = (String)this.m_tzChoice.getSelectedItem();
/* 269 */     this.m_tzChoice.removeAllItems();
/* 270 */     this.m_tzValues = new IdcVector();
/* 271 */     if (theDefaultTz != null)
/*     */     {
/* 273 */       String timeZoneName = theDefaultTz.getID();
/* 274 */       timeZoneName = clh.getTimeZoneDisplayName(timeZoneName, 2, null);
/*     */ 
/* 276 */       this.m_defaultTzMsg = LocaleResources.getString("csServerPanelUnspecifiedTimeZone", null, timeZoneName);
/*     */     }
/*     */ 
/* 280 */     this.m_tzChoice.addItem(this.m_defaultTzMsg);
/* 281 */     this.m_tzValues.addElement("");
/* 282 */     this.m_tzValueLabelMap.put("", this.m_defaultTzMsg);
/*     */     try
/*     */     {
/* 285 */       FieldInfo[] timeFields = ResultSetUtils.createInfoList(timezones, new String[] { "lcTimeZone", "lcLabel" }, true);
/*     */ 
/* 287 */       for (timezones.first(); timezones.isRowPresent(); timezones.next())
/*     */       {
/* 289 */         String id = timezones.getStringValue(timeFields[0].m_index);
/* 290 */         String label = timezones.getStringValue(timeFields[1].m_index);
/* 291 */         this.m_tzChoice.addItem(label);
/* 292 */         this.m_tzValues.add(id);
/* 293 */         this.m_tzValueLabelMap.put(id, label);
/*     */       }
/* 295 */       if (selected != null)
/*     */       {
/* 297 */         this.m_tzChoice.setSelectedItem(selected);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 302 */       Report.warning("system", null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 314 */     String unchangedPassword = "-----";
/* 315 */     if (updateComponent)
/*     */     {
/* 318 */       if ((exchange.m_compName.equals("ProxyPassword")) || (exchange.m_compName.equals("confirmPassword")))
/*     */       {
/* 321 */         exchange.m_compValue = unchangedPassword;
/* 322 */         return;
/*     */       }
/* 324 */       if (exchange.m_compName.equals("SystemTimeZone"))
/*     */       {
/* 326 */         String value = this.m_helper.m_props.getProperty("SystemTimeZone");
/* 327 */         if (value != null)
/*     */         {
/* 329 */           exchange.m_compValue = this.m_tzValueLabelMap.getProperty(value);
/*     */         }
/*     */ 
/* 332 */         return;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 337 */       if (exchange.m_compName.equals("confirmPassword"))
/*     */       {
/* 339 */         return;
/*     */       }
/* 341 */       if (exchange.m_compName.equals("ProxyPassword"))
/*     */       {
/* 343 */         if (exchange.m_compValue.equals(unchangedPassword))
/*     */         {
/* 345 */           return;
/*     */         }
/* 347 */         this.m_helper.m_props.put("ProxyPasswordEncoding", "");
/*     */       }
/* 350 */       else if ((exchange.m_compName.equals("SystemLocale")) && (exchange.m_compValue.equals(this.m_defaultLocale)))
/*     */       {
/* 353 */         exchange.m_compValue = "";
/*     */       }
/* 355 */       else if (exchange.m_compName.equals("SystemTimeZone"))
/*     */       {
/* 357 */         int index = this.m_tzChoice.getSelectedIndex();
/* 358 */         if (index >= 0)
/*     */         {
/* 360 */           exchange.m_compValue = ((String)this.m_tzValues.elementAt(index));
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 365 */     super.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 371 */     String name = exchange.m_compName;
/* 372 */     String val = exchange.m_compValue;
/*     */ 
/* 374 */     if (name.equals("SocketHostAddressSecurityFilter"))
/*     */     {
/* 376 */       IdcMessage msg = validateIPAddressFilter(val);
/* 377 */       if (msg != null)
/*     */       {
/* 379 */         exchange.m_errorMessage = msg;
/* 380 */         return false;
/*     */       }
/*     */     }
/* 383 */     else if (name.equals("ProxyPassword"))
/*     */     {
/* 385 */       String confirmVal = exchange.getComponentValue("confirmPassword");
/* 386 */       if ((confirmVal != null) && (!confirmVal.equals(val)))
/*     */       {
/* 388 */         exchange.m_errorMessage = IdcMessageFactory.lc("csServerPanelProxyPasswordMismatch", new Object[0]);
/* 389 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 393 */     return true;
/*     */   }
/*     */ 
/*     */   protected boolean isDottedQuadWithWildcards(String str)
/*     */   {
/* 399 */     String[] parts = str.split("\\.");
/* 400 */     if ((parts.length > 4) || (parts.length == 0))
/*     */     {
/* 402 */       return false;
/*     */     }
/* 404 */     if ((parts.length < 4) && (str.indexOf("*") == -1))
/*     */     {
/* 406 */       return false;
/*     */     }
/* 408 */     for (String part : parts)
/*     */     {
/* 410 */       if (part.equals("*")) {
/*     */         continue;
/*     */       }
/*     */ 
/* 414 */       if (!NumberUtils.isInteger(part))
/*     */       {
/* 416 */         return false;
/*     */       }
/*     */     }
/* 419 */     return true;
/*     */   }
/*     */ 
/*     */   protected IdcMessage validateIPAddressFilter(String val)
/*     */   {
/* 425 */     IdcMessage msg = null;
/* 426 */     int l = val.length();
/* 427 */     boolean badChar = false;
/* 428 */     boolean isSpace = false;
/* 429 */     char badCh = '\000';
/* 430 */     for (int i = 0; i < l; ++i)
/*     */     {
/* 432 */       char ch = val.charAt(i);
/* 433 */       if ((!Character.isWhitespace(ch)) && (ch != '"') && (ch != '\'') && (ch != ',') && (ch != ';') && (ch != '!') && (ch != '+')) {
/*     */         continue;
/*     */       }
/* 436 */       isSpace = Character.isWhitespace(ch);
/* 437 */       badChar = true;
/* 438 */       badCh = ch;
/* 439 */       break;
/*     */     }
/*     */ 
/* 442 */     if (badChar)
/*     */     {
/* 444 */       if (isSpace)
/*     */       {
/* 446 */         msg = IdcMessageFactory.lc("csServerPanelIPAddressNoSpaces", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 450 */         String charStr = "" + badCh;
/* 451 */         msg = IdcMessageFactory.lc("csServerPanelIPAddressInvalidChar", new Object[] { charStr });
/*     */       }
/*     */     }
/*     */ 
/* 455 */     if (msg == null)
/*     */     {
/* 457 */       Vector list = new IdcVector();
/* 458 */       for (int index = val.lastIndexOf("|"); val.length() > 0; index = val.lastIndexOf("|"))
/*     */       {
/* 460 */         String tmp = val.substring(index + 1);
/* 461 */         if (index > 0)
/*     */         {
/* 463 */           val = val.substring(0, index);
/*     */         }
/*     */         else
/*     */         {
/* 467 */           val = "";
/*     */         }
/*     */ 
/* 471 */         boolean match = isDottedQuadWithWildcards(tmp);
/* 472 */         if (match) {
/*     */           continue;
/*     */         }
/*     */ 
/* 476 */         if (tmp.startsWith("::"))
/*     */         {
/* 479 */           tmp = "0" + tmp;
/*     */         }
/* 481 */         String[] parts = tmp.split(":");
/* 482 */         if (parts.length <= 8)
/*     */         {
/* 484 */           boolean hasZeroFill = false;
/* 485 */           boolean hasV4Part = false;
/* 486 */           boolean hasWildcard = false;
/* 487 */           match = true;
/* 488 */           for (String part : parts)
/*     */           {
/* 490 */             if (part.length() == 0)
/*     */             {
/* 492 */               if (hasZeroFill)
/*     */               {
/* 494 */                 match = false;
/* 495 */                 break;
/*     */               }
/* 497 */               hasZeroFill = true;
/*     */             }
/* 500 */             else if (isDottedQuadWithWildcards(part))
/*     */             {
/* 502 */               hasV4Part = true;
/*     */             }
/*     */             else {
/* 505 */               for (char c : part.toCharArray())
/*     */               {
/* 507 */                 if (((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'f')) || (c == '*'))
/*     */                 {
/* 512 */                   if (c != '*')
/*     */                     continue;
/* 514 */                   hasWildcard = true;
/*     */                 }
/*     */                 else
/*     */                 {
/* 518 */                   match = false;
/*     */                 }
/*     */               }
/*     */             }
/*     */           }
/* 521 */           int length = parts.length;
/* 522 */           if (hasV4Part)
/*     */           {
/* 526 */             ++length;
/*     */           }
/* 528 */           if ((match) && (!hasWildcard) && (!hasZeroFill) && (length < 8))
/*     */           {
/* 531 */             match = false;
/*     */           }
/*     */         }
/* 534 */         if (match) {
/*     */           continue;
/*     */         }
/*     */ 
/* 538 */         list.insertElementAt(tmp, 0);
/*     */       }
/*     */ 
/* 541 */       if (list.size() > 0)
/*     */       {
/* 543 */         String badAddr = (String)list.elementAt(0);
/* 544 */         if (list.size() == 1)
/*     */         {
/* 546 */           msg = IdcMessageFactory.lc("csServerPanelIPAddressError", new Object[] { badAddr });
/*     */         }
/*     */         else
/*     */         {
/* 550 */           msg = IdcMessageFactory.lc("csServerPanelIPAddressesError", new Object[] { badAddr });
/*     */ 
/* 552 */           for (int i = 1; i < list.size(); ++i)
/*     */           {
/* 554 */             msg = IdcMessageFactory.lc(msg, "csServerPanelIPAddressesList", new Object[] { list.elementAt(i) });
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 559 */     if (msg != null)
/*     */     {
/* 561 */       msg = IdcMessageFactory.lc(msg, "csServerPanelIPAddressHint", new Object[0]);
/*     */     }
/* 563 */     return msg;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 568 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84494 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.ServerPanel
 * JD-Core Version:    0.5.4
 */