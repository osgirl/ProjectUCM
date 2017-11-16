/*     */ package intradoc.apputilities.systemproperties;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.conversion.CryptoPasswordUtils;
/*     */ import intradoc.conversion.PasswordInfo;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomPasswordField;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ import java.util.Properties;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JRadioButton;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class DBPanel extends SystemPropertiesPanel
/*     */ {
/*  62 */   protected static String CUSTOM_DRIVER_KEY = "JDBC_JAVA_CLASSPATH_custom";
/*  63 */   protected static String SYSTEM_DRIVER_KEY = "JDBC_JAVA_CLASSPATH_system";
/*     */ 
/*  70 */   protected static final String[][] ALL_DRIVERS = { { "csDBPanelMSFTDriverDesc", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://", "host_name:port_number/database_name", "honorcase", "mssql", "", "" }, { "csDBPanelOracleThinDriverDesc", "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@", "host_name:port_number:instance_name", "useuppercase", "oracle", "", "" }, { "csDBPanelOracleOCIDriverDesc", "oracle.jdbc.OracleDriver", "jdbc:oracle:oci:@", "host_name:port_number:instance_name", "useuppercase", "oracle", "", "" }, { "csDBPanelOtherJDBCDriverDesc", "", "", "", "", "", "", "" } };
/*     */   protected String[][] m_database;
/*     */   protected String m_defaultClasspathForCurrentDatabase;
/*     */   protected JCheckBox m_isJdbcCheckbox;
/*     */   protected JCheckBox m_databasePreserveCaseCheckbox;
/*     */   protected JRadioButton[] m_dbCheckbox;
/*     */   protected JTextField m_driverField;
/*     */   protected JCheckBox m_driverClasspathEnabled;
/*     */   protected JTextField m_driverClasspathField;
/*     */   protected JTextField m_dsnField;
/*     */   protected JTextField m_usrField;
/*     */   protected JTextField m_pwdField;
/*     */   protected boolean m_requireUserPassword;
/*     */ 
/*     */   public DBPanel()
/*     */   {
/*  99 */     this.m_requireUserPassword = false;
/*     */   }
/*     */ 
/*     */   protected void handleJdbcStatusChange(boolean jdbcEnabled) {
/* 103 */     for (int i = 0; i < this.m_database.length; ++i)
/*     */     {
/* 105 */       this.m_helper.setEnabled(this.m_dbCheckbox[i], jdbcEnabled);
/*     */     }
/*     */ 
/* 108 */     this.m_helper.setEnabled(this.m_dsnField, jdbcEnabled);
/* 109 */     this.m_helper.setEnabled(this.m_usrField, jdbcEnabled);
/* 110 */     this.m_helper.setEnabled(this.m_pwdField, jdbcEnabled);
/*     */ 
/* 112 */     this.m_helper.setEnabled(this.m_driverField, (jdbcEnabled) && (this.m_dbCheckbox[(this.m_database.length - 1)].isSelected()));
/* 113 */     if (jdbcEnabled)
/*     */     {
/* 115 */       handleDatabaseStatusChange();
/*     */     }
/*     */     else
/*     */     {
/* 119 */       this.m_helper.m_props.remove("DatabasePreserveCase");
/* 120 */       this.m_databasePreserveCaseCheckbox.setSelected(false);
/* 121 */       this.m_helper.setEnabled(this.m_databasePreserveCaseCheckbox, false);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void handleDatabaseStatusChange()
/*     */   {
/* 127 */     String dsnText = this.m_dsnField.getText();
/* 128 */     String origDsnText = this.m_helper.m_props.getProperty("JdbcConnectionString");
/* 129 */     if (origDsnText == null)
/*     */     {
/* 131 */       origDsnText = "";
/*     */     }
/*     */ 
/* 134 */     for (int i = 0; i < this.m_database.length; ++i)
/*     */     {
/* 136 */       if (!this.m_dbCheckbox[i].isSelected())
/*     */         continue;
/* 138 */       if (i != this.m_database.length - 1)
/*     */       {
/* 140 */         this.m_driverField.setText(this.m_database[i][1]);
/*     */       }
/* 142 */       if (dsnText.indexOf(this.m_database[i][2]) != 0)
/*     */       {
/* 144 */         if (origDsnText.indexOf(this.m_database[i][2]) == 0)
/*     */         {
/* 146 */           this.m_dsnField.setText(origDsnText);
/*     */         }
/*     */         else
/*     */         {
/* 150 */           this.m_dsnField.setText(this.m_database[i][2] + this.m_database[i][3]);
/*     */         }
/*     */       }
/*     */ 
/* 154 */       if (this.m_database[i][4].indexOf("useuppercase") >= 0)
/*     */       {
/* 156 */         this.m_databasePreserveCaseCheckbox.setSelected(true);
/* 157 */         this.m_databasePreserveCaseCheckbox.setEnabled(false);
/* 158 */         this.m_helper.m_props.put("DatabasePreserveCase", "true");
/*     */       }
/* 160 */       else if (this.m_database[i][4].indexOf("honorcase") >= 0)
/*     */       {
/* 162 */         this.m_databasePreserveCaseCheckbox.setSelected(false);
/* 163 */         this.m_databasePreserveCaseCheckbox.setEnabled(false);
/* 164 */         this.m_helper.m_props.remove("DatabasePreserveCase");
/*     */       }
/*     */       else
/*     */       {
/* 168 */         this.m_databasePreserveCaseCheckbox.setEnabled(true);
/*     */       }
/*     */ 
/* 171 */       if (this.m_database[i][5].length() > 0)
/*     */       {
/* 173 */         this.m_helper.m_props.put("DatabaseType", this.m_database[i][5]);
/*     */       }
/*     */       else
/*     */       {
/* 177 */         this.m_helper.m_props.remove("DatabaseType");
/*     */       }
/*     */ 
/* 180 */       if (this.m_database[i][7].length() > 0)
/*     */       {
/* 182 */         this.m_helper.setEnabled(this.m_driverClasspathEnabled, true);
/* 183 */         this.m_driverClasspathEnabled.setSelected(false);
/*     */       }
/*     */       else
/*     */       {
/* 187 */         this.m_driverClasspathEnabled.setSelected(true);
/* 188 */         this.m_helper.setEnabled(this.m_driverClasspathEnabled, false);
/*     */       }
/* 190 */       if ((!this.m_driverClasspathEnabled.isSelected()) || (this.m_defaultClasspathForCurrentDatabase == null) || (this.m_driverClasspathField.getText().equals(this.m_defaultClasspathForCurrentDatabase)))
/*     */       {
/* 194 */         this.m_driverClasspathField.setText(this.m_database[i][7]);
/*     */       }
/* 196 */       this.m_defaultClasspathForCurrentDatabase = this.m_database[i][7];
/* 197 */       handleClasspathStatusChange(this.m_driverClasspathEnabled.isSelected());
/* 198 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void handleClasspathStatusChange(boolean userClasspathEnabled)
/*     */   {
/* 205 */     this.m_helper.setEnabled(this.m_driverClasspathField, userClasspathEnabled);
/* 206 */     if (userClasspathEnabled)
/*     */       return;
/* 208 */     this.m_driverClasspathField.setText(this.m_defaultClasspathForCurrentDatabase);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/* 218 */     ResourceContainer rc = new ResourceContainer();
/* 219 */     String file = this.m_helper.m_props.getProperty("DatabaseResourceFile");
/* 220 */     if (file == null)
/*     */     {
/* 222 */       file = DirectoryLocator.getResourcesDirectory() + "core/install/server_install_info.htm";
/*     */     }
/* 224 */     file = FileUtils.fileSlashes(file);
/*     */     try
/*     */     {
/* 227 */       Reader r = FileUtils.openDataReader(file);
/* 228 */       rc.parseAndAddResources(r, file);
/* 229 */       r.close();
/* 230 */       Table t = rc.getTable("DatabaseDriverTable");
/* 231 */       if (t != null)
/*     */       {
/* 233 */         int count = 0;
/* 234 */         int flagColumn = 4;
/* 235 */         for (int i = 0; i < t.getNumRows(); ++i)
/*     */         {
/* 237 */           String flag = t.getString(i, flagColumn);
/* 238 */           if (flag.indexOf("syspropeditor") < 0)
/*     */             continue;
/* 240 */           ++count;
/*     */         }
/*     */ 
/* 243 */         this.m_database = new String[count][];
/* 244 */         count = 0;
/* 245 */         for (int i = 0; i < t.getNumRows(); ++i)
/*     */         {
/* 247 */           String flag = t.getString(i, flagColumn);
/* 248 */           if (flag.indexOf("syspropeditor") < 0)
/*     */             continue;
/* 250 */           this.m_database[(count++)] = t.getRow(i);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 257 */       Report.trace(null, "Unable to read server_install_info.htm.", e);
/*     */     }
/*     */     catch (ParseSyntaxException e)
/*     */     {
/* 261 */       Report.trace(null, "Unable to read server_install_info.htm.", e);
/*     */     }
/* 263 */     if (this.m_database == null)
/*     */     {
/* 265 */       this.m_database = ALL_DRIVERS;
/*     */     }
/*     */ 
/* 270 */     JPanel infoPanel = new CustomPanel();
/* 271 */     this.m_helper.makePanelGridBag(infoPanel, 1);
/* 272 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/*     */ 
/* 276 */     this.m_helper.addPanelTitle(infoPanel, LocaleResources.getString("csDBPanelTitle", null));
/* 277 */     gridBag.m_gc.weighty = 0.0D;
/* 278 */     gridBag.prepareAddLastRowElement();
/* 279 */     this.m_helper.addComponent(infoPanel, new CustomLabel(""));
/*     */ 
/* 282 */     JPanel tPanel = addNewSubPanel(infoPanel, 1);
/*     */ 
/* 285 */     this.m_isJdbcCheckbox = new CustomCheckbox(LocaleResources.getString("csDBPanelJDBCCheckboxDesc", null));
/* 286 */     this.m_databasePreserveCaseCheckbox = new CustomCheckbox(LocaleResources.getString("csDBPanelDBPreserveCaseCheckBoxDesc", null));
/*     */ 
/* 288 */     Insets oldInsets = gridBag.m_gc.insets;
/* 289 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 290 */     gridBag.m_gc.anchor = 17;
/* 291 */     gridBag.m_gc.fill = 0;
/* 292 */     this.m_helper.addExchangeComponent(tPanel, this.m_isJdbcCheckbox, "IsJdbc");
/* 293 */     if (!EnvUtils.isMicrosoftVM())
/*     */     {
/* 295 */       this.m_isJdbcCheckbox.setSelected(true);
/*     */     }
/*     */ 
/* 299 */     ButtonGroup dbGroup = new ButtonGroup();
/* 300 */     gridBag.m_gc.insets = new Insets(0, 25, 0, 0);
/*     */ 
/* 303 */     ItemListener cbListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 307 */         boolean jdbcEnabled = DBPanel.this.m_isJdbcCheckbox.isSelected();
/* 308 */         DBPanel.this.handleJdbcStatusChange(jdbcEnabled);
/*     */       }
/*     */     };
/* 312 */     this.m_dbCheckbox = new JRadioButton[this.m_database.length];
/*     */ 
/* 314 */     JPanel p1 = new PanePanel();
/* 315 */     JPanel p2 = new PanePanel();
/* 316 */     gridBag.useGridBag(p1);
/* 317 */     gridBag.useGridBag(p2);
/* 318 */     JPanel p = p1;
/* 319 */     String currentDriver = this.m_helper.m_props.getProperty("JdbcDriver");
/* 320 */     boolean found = false;
/* 321 */     for (int i = 0; i < this.m_database.length; ++i)
/*     */     {
/* 323 */       if (i + 1 > (this.m_database.length + 1) / 2)
/*     */       {
/* 325 */         p = p2;
/*     */       }
/* 327 */       this.m_dbCheckbox[i] = new JRadioButton(LocaleResources.getString(this.m_database[i][0], null));
/* 328 */       dbGroup.add(this.m_dbCheckbox[i]);
/*     */ 
/* 330 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 331 */       gridBag.m_gc.anchor = 17;
/* 332 */       gridBag.m_gc.fill = 0;
/* 333 */       this.m_helper.addComponent(p, this.m_dbCheckbox[i]);
/* 334 */       if ((found) || ((!this.m_database[i][1].equals(currentDriver)) && (i + 1 != this.m_database.length))) {
/*     */         continue;
/*     */       }
/* 337 */       found = true;
/* 338 */       if (i + 1 == this.m_database.length)
/*     */       {
/* 343 */         this.m_helper.m_props.put(CUSTOM_DRIVER_KEY, "");
/*     */       }
/* 345 */       this.m_dbCheckbox[i].setSelected(true);
/*     */     }
/*     */ 
/* 348 */     gridBag.prepareAddRowElement();
/* 349 */     this.m_helper.addComponent(tPanel, p1);
/* 350 */     gridBag.prepareAddLastRowElement();
/* 351 */     gridBag.m_gc.anchor = 11;
/* 352 */     this.m_helper.addComponent(tPanel, p2);
/* 353 */     gridBag.m_gc.anchor = 10;
/* 354 */     gridBag.m_gc.fill = 2;
/*     */ 
/* 356 */     gridBag.m_gc.insets = oldInsets;
/*     */ 
/* 358 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 359 */     gridBag.m_gc.anchor = 17;
/* 360 */     gridBag.m_gc.fill = 0;
/* 361 */     this.m_helper.addExchangeComponent(tPanel, this.m_databasePreserveCaseCheckbox, "DatabasePreserveCase");
/*     */ 
/* 363 */     String dsnString = this.m_helper.m_props.getProperty("JdbcConnectionString");
/* 364 */     String driverString = this.m_helper.m_props.getProperty("JdbcDriver");
/* 365 */     this.m_driverClasspathField = new CustomTextField(40);
/* 366 */     this.m_driverField = new CustomTextField(40);
/* 367 */     this.m_dsnField = new CustomTextField(40);
/*     */ 
/* 370 */     ItemListener classpathListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 374 */         boolean jdbcEnabled = DBPanel.this.m_driverClasspathEnabled.isSelected();
/* 375 */         DBPanel.this.handleClasspathStatusChange(jdbcEnabled);
/*     */       }
/*     */     };
/* 379 */     this.m_driverClasspathEnabled = new CustomCheckbox(LocaleResources.getString("csDBPanelDBClasspathEnabled", null));
/*     */ 
/* 381 */     this.m_driverClasspathEnabled.addItemListener(classpathListener);
/* 382 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 383 */     gridBag.m_gc.anchor = 17;
/* 384 */     gridBag.m_gc.fill = 0;
/* 385 */     this.m_helper.addExchangeComponent(tPanel, this.m_driverClasspathEnabled, "JdbcUserClasspath");
/*     */ 
/* 387 */     if ((dsnString != null) && (driverString != null))
/*     */     {
/* 389 */       for (int i = 0; i < this.m_database.length; ++i)
/*     */       {
/* 391 */         if ((dsnString.indexOf(this.m_database[i][2]) != 0) || (driverString.indexOf(this.m_database[i][1]) != 0)) {
/*     */           continue;
/*     */         }
/* 394 */         this.m_driverField.setText(driverString);
/* 395 */         this.m_dbCheckbox[i].setSelected(true);
/* 396 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 401 */     p = new PanePanel();
/* 402 */     gridBag.useGridBag(p);
/* 403 */     gridBag.m_gc.fill = 2;
/* 404 */     this.m_helper.addLabelFieldPair(p, LocaleResources.getString("csDBPanelDBClasspath", null), this.m_driverClasspathField, CUSTOM_DRIVER_KEY);
/*     */ 
/* 406 */     this.m_helper.addLabelFieldPair(p, LocaleResources.getString("csDBPanelJDBCDriverLabel", null), this.m_driverField, "JdbcDriver");
/*     */ 
/* 408 */     this.m_helper.addLabelFieldPair(p, LocaleResources.getString("csDBPanelJDBCConnectionString", null), this.m_dsnField, "JdbcConnectionString");
/*     */ 
/* 411 */     this.m_usrField = new CustomTextField(20);
/* 412 */     this.m_helper.addLabelFieldPair(p, LocaleResources.getString("csDBPanelJDBCUsername", null), this.m_usrField, "JdbcUser");
/*     */ 
/* 414 */     this.m_pwdField = new CustomPasswordField(20);
/* 415 */     this.m_helper.addLabelFieldPair(p, LocaleResources.getString("csDBPanelJDBCPassword", null), this.m_pwdField, "JdbcPassword");
/*     */ 
/* 419 */     boolean isJdbcEnabled = StringUtils.convertToBool(this.m_helper.m_props.getProperty("IsJdbc"), true);
/*     */ 
/* 421 */     handleJdbcStatusChange(isJdbcEnabled);
/* 422 */     boolean isClasspathEnabled = this.m_helper.m_props.getProperty(CUSTOM_DRIVER_KEY) != null;
/* 423 */     handleClasspathStatusChange(isClasspathEnabled);
/* 424 */     gridBag.prepareAddLastRowElement();
/* 425 */     this.m_helper.addComponent(tPanel, p);
/* 426 */     gridBag.addEmptyRow(infoPanel);
/* 427 */     gridBag.m_gc.weighty = 1.0D;
/*     */ 
/* 430 */     setLayout(new BorderLayout());
/* 431 */     add("Center", infoPanel);
/*     */ 
/* 433 */     this.m_isJdbcCheckbox.addItemListener(cbListener);
/* 434 */     for (int i = 0; i < this.m_database.length; ++i)
/*     */     {
/* 436 */       this.m_dbCheckbox[i].addItemListener(cbListener);
/*     */     }
/*     */ 
/* 439 */     this.m_isJdbcCheckbox.addItemListener(cbListener);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 450 */     if (exchange.m_compName.equals(CUSTOM_DRIVER_KEY))
/*     */     {
/* 452 */       if (updateComponent)
/*     */       {
/* 454 */         SystemPropertiesFrame frame = (SystemPropertiesFrame)this.m_sysInterface;
/* 455 */         String[] driverKeys = { CUSTOM_DRIVER_KEY, SYSTEM_DRIVER_KEY };
/*     */ 
/* 460 */         for (String key : driverKeys)
/*     */         {
/* 462 */           String val = frame.m_idcProperties.getProperty(key);
/*     */ 
/* 469 */           if (val == null)
/*     */             continue;
/* 471 */           frame.m_cfgProperties.put(key, val);
/* 472 */           frame.m_idcProperties.remove(key);
/*     */         }
/*     */ 
/* 476 */         boolean isCustom = this.m_helper.m_props.getProperty(CUSTOM_DRIVER_KEY) != null;
/* 477 */         this.m_driverClasspathEnabled.setSelected(isCustom);
/* 478 */         handleClasspathStatusChange(isCustom);
/* 479 */         if ((!isCustom) && 
/* 481 */           (this.m_defaultClasspathForCurrentDatabase != null))
/*     */         {
/* 483 */           exchange.m_compName = SYSTEM_DRIVER_KEY;
/*     */         }
/*     */ 
/*     */       }
/* 489 */       else if (this.m_driverClasspathEnabled.isSelected())
/*     */       {
/* 491 */         this.m_helper.m_props.remove(SYSTEM_DRIVER_KEY);
/*     */       }
/*     */       else
/*     */       {
/* 495 */         this.m_helper.m_props.remove(CUSTOM_DRIVER_KEY);
/* 496 */         exchange.m_compName = SYSTEM_DRIVER_KEY;
/*     */       }
/*     */ 
/*     */     }
/* 500 */     else if (exchange.m_compName.equals("JdbcPassword"))
/*     */     {
/*     */       try
/*     */       {
/* 504 */         if (updateComponent)
/*     */         {
/* 506 */           String encoding = (String)this.m_helper.m_props.get("JdbcPasswordEncoding");
/* 507 */           String passwrd = (String)this.m_helper.m_props.get("JdbcPassword");
/*     */ 
/* 509 */           PasswordInfo sInfo = new PasswordInfo("db", encoding, passwrd);
/* 510 */           sInfo.m_field = "JdbcPassword";
/* 511 */           sInfo.m_scope = "system";
/*     */ 
/* 514 */           passwrd = CryptoPasswordUtils.decrypt(sInfo);
/* 515 */           if (passwrd != null)
/*     */           {
/* 517 */             this.m_helper.m_props.put("JdbcPassword", passwrd);
/* 518 */             this.m_helper.m_props.put("JdbcPasswordEncoding", "");
/*     */           }
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 524 */         Report.trace("system", "Unable to decrypt database password.", e);
/* 525 */         IdcMessage msg = IdcMessageFactory.lc(e, "csSysPropsDecryptDBPasswordError", new Object[0]);
/* 526 */         reportError(msg);
/*     */       }
/*     */     }
/*     */ 
/* 530 */     super.exchangeComponentValue(exchange, updateComponent);
/*     */ 
/* 533 */     if (!updateComponent)
/*     */     {
/* 535 */       String name = exchange.m_compName;
/* 536 */       String val = exchange.m_compValue;
/*     */ 
/* 538 */       if (name.equals("JdbcConnectionString"))
/*     */       {
/* 540 */         if ((val == null) || (val.length() == 0))
/*     */         {
/* 542 */           this.m_helper.m_props.remove("JdbcConnectionString");
/*     */         }
/*     */         else
/*     */         {
/* 546 */           this.m_helper.m_props.put("JdbcConnectionString", val);
/*     */         }
/*     */       }
/* 549 */       else if (name.equalsIgnoreCase("IsJdbc"))
/*     */       {
/* 551 */         boolean isJdbcEnabled = StringUtils.convertToBool(this.m_helper.m_props.getProperty("IsJdbc"), true);
/*     */ 
/* 555 */         if (isJdbcEnabled)
/*     */         {
/* 557 */           this.m_helper.m_props.remove("IsJdbc");
/*     */         }
/*     */         else
/*     */         {
/* 561 */           this.m_helper.m_props.put("IsJdbc", "false");
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 567 */       String name = exchange.m_compName;
/*     */ 
/* 569 */       if (!name.equalsIgnoreCase("IsJdbc"))
/*     */         return;
/* 571 */       boolean isJdbcEnabled = StringUtils.convertToBool(this.m_helper.m_props.getProperty("IsJdbc"), true);
/*     */ 
/* 574 */       exchange.m_compValue = ("" + isJdbcEnabled);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 582 */     String name = exchange.m_compName;
/* 583 */     String val = exchange.m_compValue;
/*     */ 
/* 585 */     IdcMessage errMsg = null;
/* 586 */     boolean jdbcEnabled = this.m_isJdbcCheckbox.isSelected();
/* 587 */     if (jdbcEnabled)
/*     */     {
/* 590 */       if (name.equals("JdbcConnectionString"))
/*     */       {
/* 592 */         if (val.length() >= 0)
/*     */         {
/* 594 */           for (int i = 0; i < this.m_database.length - 1; ++i)
/*     */           {
/* 596 */             if (!this.m_dbCheckbox[i].isSelected())
/*     */               continue;
/* 598 */             if (!val.startsWith(this.m_database[i][2]))
/*     */             {
/* 600 */               errMsg = IdcMessageFactory.lc("csDBPanelJDBCConnectionStringMsg", new Object[] { this.m_database[i][0], this.m_database[i][2] });
/*     */             }
/*     */             else
/*     */             {
/* 605 */               this.m_requireUserPassword = isJdbcUserPasswordRequired(val);
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */         else {
/* 612 */           this.m_requireUserPassword = false;
/*     */         }
/*     */       }
/* 615 */       else if ((name.equals("JdbcUser")) && (this.m_requireUserPassword))
/*     */       {
/* 617 */         if ((val == null) || (val.length() == 0))
/*     */         {
/* 619 */           errMsg = IdcMessageFactory.lc("csDBPanelJDBCUsernameRequiredMsg", new Object[0]);
/*     */         }
/*     */       }
/* 622 */       else if ((name.equals("JdbcPassword")) && (this.m_requireUserPassword) && ((
/* 624 */         (val == null) || (val.length() == 0))))
/*     */       {
/* 626 */         errMsg = IdcMessageFactory.lc("csDBPanelJDBCPasswordRequiredMsg", new Object[0]);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 631 */     if (errMsg != null)
/*     */     {
/* 633 */       exchange.m_errorMessage = errMsg;
/* 634 */       return false;
/*     */     }
/* 636 */     return true;
/*     */   }
/*     */ 
/*     */   public static boolean isJdbcUserPasswordRequired(String url)
/*     */   {
/* 645 */     if ((url == null) || (url.length() == 0))
/*     */     {
/* 647 */       return true;
/*     */     }
/* 649 */     url = url.toLowerCase();
/*     */ 
/* 652 */     return (!url.startsWith("jdbc:jtds")) || (url.indexOf(":domain") <= 0);
/*     */   }
/*     */ 
/*     */   public void saveChanges()
/*     */     throws ServiceException
/*     */   {
/* 660 */     super.saveChanges();
/*     */ 
/* 663 */     SystemPropertiesFrame frame = (SystemPropertiesFrame)this.m_sysInterface;
/* 664 */     String[] driverKeys = { CUSTOM_DRIVER_KEY, SYSTEM_DRIVER_KEY };
/*     */ 
/* 669 */     for (String key : driverKeys)
/*     */     {
/* 671 */       String val = frame.m_cfgProperties.getProperty(key);
/* 672 */       if ((val != null) && (val.length() == 0))
/*     */       {
/* 675 */         frame.m_cfgProperties.remove(key);
/* 676 */         frame.m_idcProperties.remove(key);
/*     */       }
/* 678 */       if ((val == null) || (val.startsWith("$"))) {
/*     */         continue;
/*     */       }
/* 681 */       frame.m_cfgProperties.remove(key);
/* 682 */       frame.m_idcProperties.put(key, val);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 689 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84567 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.DBPanel
 * JD-Core Version:    0.5.4
 */