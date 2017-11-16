/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.useradmin.irmintg.util.IRMUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomPasswordField;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.RoleGroupData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JPasswordField;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class IRMConfigDlg extends IRMDlg
/*     */ {
/*     */   protected DisplayChoice m_cmbNonControlSealedContent;
/*     */   protected DisplayChoice m_cmbUnsealType;
/*     */   protected JLabel m_lblNonControlSealedContent;
/*     */   protected JLabel m_lblUnsealType;
/*     */   protected JLabel m_lblDesktopURI;
/*     */   protected JLabel m_lblRightsRefresh;
/*     */   protected JLabel m_lblSealingPwd;
/*     */   protected JLabel m_lblSealingURI;
/*     */   protected JLabel m_lblHttpURI;
/*     */   protected JLabel m_lblSealingUname;
/*     */   protected JPanel m_panelContent;
/*     */   protected JPanel m_panelConstraints;
/*     */   protected JPanel m_panelRights;
/*     */   protected JPanel m_panelServer;
/*     */   protected JTextField m_txtDesktopURI;
/*     */   protected JTextField m_txtSealingURI;
/*     */   protected JTextField m_txtHttpURI;
/*     */   protected JTextField m_txtSealingUname;
/*     */   protected JPasswordField m_pwdSealingPwd;
/*     */   protected JCheckBox m_chkAllowOffline;
/*     */   protected DisplayChoice m_cmbRightsRefresh;
/*     */   protected IRMTabbedPane m_tabPanelConfig;
/*     */   protected IRMDefaultFeaturesDlg m_rightsOptions;
/*     */ 
/*     */   public IRMConfigDlg(SystemInterface sys, String title)
/*     */   {
/* 138 */     super(sys, title);
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 147 */     super.init();
/* 148 */     this.m_tabPanelConfig = new IRMTabbedPane();
/*     */ 
/* 150 */     initServerPanel();
/* 151 */     initContentPanel();
/* 152 */     initRightsPanel();
/* 153 */     initConstraintsPanel();
/*     */ 
/* 155 */     this.m_tabPanelConfig.setFullWidthTab(true);
/*     */ 
/* 157 */     this.m_tabPanelConfig.addPane("Server", this.m_panelServer);
/* 158 */     this.m_tabPanelConfig.addPane("Content", this.m_panelContent);
/* 159 */     this.m_tabPanelConfig.addPane("Rights", this.m_panelRights);
/* 160 */     this.m_tabPanelConfig.addPane("Constraints", this.m_panelConstraints);
/*     */ 
/* 162 */     this.componentsPanelLayout = new GridLayout(1, 0, 10, 10);
/* 163 */     this.m_componentsPanel.setLayout(this.componentsPanelLayout);
/* 164 */     this.m_componentsPanel.add(this.m_tabPanelConfig);
/*     */ 
/* 166 */     packComponents();
/* 167 */     pack();
/*     */ 
/* 169 */     setDefaultConfigData();
/* 170 */     setToolTip();
/* 171 */     this.m_helper.show();
/*     */   }
/*     */ 
/*     */   protected void initConstraintsPanel()
/*     */   {
/* 179 */     this.m_panelConstraints = new PanePanel();
/*     */ 
/* 181 */     this.m_chkAllowOffline = new CustomCheckbox(LocaleResources.getString("apLabelWorkingOffline", this.m_ctx));
/*     */ 
/* 183 */     this.m_cmbRightsRefresh = new DisplayChoice();
/*     */ 
/* 185 */     this.m_lblRightsRefresh = new JLabel(LocaleResources.getString("apLabelRightsRefresh", this.m_ctx));
/*     */ 
/* 189 */     Vector refreshPeriods = IRMUtils.getRefreshPeriods(this.m_ctx);
/* 190 */     this.m_cmbRightsRefresh.init(refreshPeriods);
/*     */ 
/* 192 */     GridBagLayout panelConstraintsLayout = new GridBagLayout();
/* 193 */     GridBagConstraints panelBagConstraints = new GridBagConstraints();
/*     */ 
/* 195 */     this.m_panelConstraints.setLayout(panelConstraintsLayout);
/*     */ 
/* 197 */     panelBagConstraints.insets = new Insets(5, 5, 5, 5);
/* 198 */     panelBagConstraints.anchor = 21;
/*     */ 
/* 200 */     panelBagConstraints.gridx = 0;
/* 201 */     panelBagConstraints.gridy = 0;
/* 202 */     this.m_panelConstraints.add(this.m_chkAllowOffline, panelBagConstraints);
/*     */ 
/* 204 */     panelBagConstraints.gridx = 0;
/* 205 */     panelBagConstraints.gridy = 1;
/* 206 */     this.m_panelConstraints.add(this.m_lblRightsRefresh, panelBagConstraints);
/*     */ 
/* 208 */     panelBagConstraints.gridx = 1;
/* 209 */     panelBagConstraints.gridy = 1;
/* 210 */     this.m_panelConstraints.add(this.m_cmbRightsRefresh, panelBagConstraints);
/*     */   }
/*     */ 
/*     */   protected void initRightsPanel()
/*     */   {
/*     */     try
/*     */     {
/* 220 */       this.m_panelRights = new PanePanel();
/* 221 */       RoleGroupData roleGroup = new RoleGroupData("", "", 15L, "");
/* 222 */       this.m_rightsOptions = new IRMDefaultFeaturesDlg(this.m_panelRights, roleGroup, this.m_ctx, this.m_system);
/*     */ 
/* 224 */       this.m_rightsOptions.init();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 228 */       MessageBox.reportError(this.m_system, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initContentPanel()
/*     */   {
/* 237 */     this.m_panelContent = new PanePanel();
/*     */ 
/* 239 */     this.m_cmbUnsealType = new DisplayChoice();
/* 240 */     this.m_cmbNonControlSealedContent = new DisplayChoice();
/*     */ 
/* 242 */     this.m_lblUnsealType = new JLabel(LocaleResources.getString("apLabelUnsealableType", this.m_ctx));
/*     */ 
/* 244 */     this.m_lblNonControlSealedContent = new JLabel(LocaleResources.getString("apLabelNCSealedContent", this.m_ctx));
/*     */ 
/* 247 */     String[][] contentOptions = IRMUtils.getContentOptions(this.m_ctx);
/* 248 */     this.m_cmbUnsealType.init(contentOptions);
/* 249 */     this.m_cmbNonControlSealedContent.init(contentOptions);
/*     */ 
/* 251 */     GridBagLayout panelContentLayout = new GridBagLayout();
/* 252 */     GridBagConstraints panelBagConstraints = new GridBagConstraints();
/*     */ 
/* 254 */     this.m_panelContent.setLayout(panelContentLayout);
/*     */ 
/* 256 */     panelBagConstraints.insets = new Insets(5, 5, 5, 5);
/* 257 */     panelBagConstraints.anchor = 21;
/*     */ 
/* 259 */     panelBagConstraints.gridx = 0;
/* 260 */     panelBagConstraints.gridy = 1;
/* 261 */     this.m_panelContent.add(this.m_lblUnsealType, panelBagConstraints);
/*     */ 
/* 263 */     panelBagConstraints.gridx = 1;
/* 264 */     panelBagConstraints.gridy = 1;
/* 265 */     this.m_panelContent.add(this.m_cmbUnsealType, panelBagConstraints);
/*     */ 
/* 267 */     panelBagConstraints.gridx = 0;
/* 268 */     panelBagConstraints.gridy = 2;
/* 269 */     this.m_panelContent.add(this.m_lblNonControlSealedContent, panelBagConstraints);
/*     */ 
/* 271 */     panelBagConstraints.gridx = 1;
/* 272 */     panelBagConstraints.gridy = 2;
/* 273 */     this.m_panelContent.add(this.m_cmbNonControlSealedContent, panelBagConstraints);
/*     */   }
/*     */ 
/*     */   protected void initServerPanel()
/*     */   {
/* 281 */     this.m_panelServer = new PanePanel();
/*     */ 
/* 283 */     this.m_lblDesktopURI = new JLabel(LocaleResources.getString("apLabelDesktopURI", this.m_ctx));
/*     */ 
/* 285 */     this.m_lblSealingURI = new JLabel(LocaleResources.getString("apLabelSealingURI", this.m_ctx));
/*     */ 
/* 287 */     this.m_lblHttpURI = new JLabel(LocaleResources.getString("apLabelHttpURI", this.m_ctx));
/*     */ 
/* 290 */     this.m_txtDesktopURI = new CustomTextField(10);
/* 291 */     this.m_txtSealingURI = new CustomTextField(10);
/* 292 */     this.m_txtHttpURI = new CustomTextField(10);
/* 293 */     this.m_txtSealingUname = new CustomTextField(10);
/*     */ 
/* 295 */     this.m_lblSealingUname = new JLabel(LocaleResources.getString("apLabelSealingUname", this.m_ctx));
/*     */ 
/* 297 */     this.m_lblSealingPwd = new JLabel(LocaleResources.getString("apLabelSealingPwd", this.m_ctx));
/*     */ 
/* 299 */     this.m_pwdSealingPwd = new CustomPasswordField(10);
/*     */ 
/* 301 */     GridBagLayout panelServerLayout = new GridBagLayout();
/* 302 */     GridBagConstraints panelBagConstraints = new GridBagConstraints();
/*     */ 
/* 304 */     this.m_panelServer.setLayout(panelServerLayout);
/*     */ 
/* 306 */     panelBagConstraints.insets = new Insets(5, 5, 5, 5);
/* 307 */     panelBagConstraints.anchor = 21;
/*     */ 
/* 309 */     panelBagConstraints.gridx = 0;
/* 310 */     panelBagConstraints.gridy = 0;
/* 311 */     this.m_panelServer.add(this.m_lblDesktopURI, panelBagConstraints);
/*     */ 
/* 313 */     panelBagConstraints.gridx = 1;
/* 314 */     panelBagConstraints.gridy = 0;
/* 315 */     this.m_panelServer.add(this.m_txtDesktopURI, panelBagConstraints);
/*     */ 
/* 317 */     panelBagConstraints.gridx = 0;
/* 318 */     panelBagConstraints.gridy = 1;
/* 319 */     this.m_panelServer.add(this.m_lblSealingURI, panelBagConstraints);
/*     */ 
/* 321 */     panelBagConstraints.gridx = 1;
/* 322 */     panelBagConstraints.gridy = 1;
/* 323 */     this.m_panelServer.add(this.m_txtSealingURI, panelBagConstraints);
/*     */ 
/* 325 */     panelBagConstraints.gridx = 0;
/* 326 */     panelBagConstraints.gridy = 2;
/* 327 */     this.m_panelServer.add(this.m_lblHttpURI, panelBagConstraints);
/*     */ 
/* 329 */     panelBagConstraints.gridx = 1;
/* 330 */     panelBagConstraints.gridy = 2;
/* 331 */     this.m_panelServer.add(this.m_txtHttpURI, panelBagConstraints);
/*     */ 
/* 333 */     panelBagConstraints.gridx = 0;
/* 334 */     panelBagConstraints.gridy = 3;
/* 335 */     this.m_panelServer.add(this.m_lblSealingUname, panelBagConstraints);
/*     */ 
/* 337 */     panelBagConstraints.gridx = 1;
/* 338 */     panelBagConstraints.gridy = 3;
/* 339 */     this.m_panelServer.add(this.m_txtSealingUname, panelBagConstraints);
/*     */ 
/* 341 */     panelBagConstraints.gridx = 0;
/* 342 */     panelBagConstraints.gridy = 4;
/* 343 */     this.m_panelServer.add(this.m_lblSealingPwd, panelBagConstraints);
/*     */ 
/* 345 */     panelBagConstraints.gridx = 1;
/* 346 */     panelBagConstraints.gridy = 4;
/* 347 */     this.m_panelServer.add(this.m_pwdSealingPwd, panelBagConstraints);
/*     */   }
/*     */ 
/*     */   protected void setDefaultConfigData()
/*     */   {
/* 355 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 358 */       AppLauncher.executeService("IRM_GET_CONFIG", binder);
/* 359 */       ResultSet rsConfig = binder.getResultSet("rsConfig");
/*     */ 
/* 361 */       this.m_txtDesktopURI.setText(rsConfig.getStringValueByName("DesktopURI"));
/*     */ 
/* 363 */       this.m_txtSealingURI.setText(rsConfig.getStringValueByName("SealingURI"));
/*     */ 
/* 365 */       this.m_txtHttpURI.setText(rsConfig.getStringValueByName("HttpURI"));
/*     */ 
/* 367 */       this.m_txtSealingUname.setText(rsConfig.getStringValueByName("SealingUName"));
/*     */ 
/* 370 */       this.m_cmbUnsealType.setSelectedItem(rsConfig.getStringValueByName("UnsealTypes"));
/*     */ 
/* 372 */       this.m_cmbNonControlSealedContent.setSelectedItem(rsConfig.getStringValueByName("NonControlSealedContent"));
/*     */ 
/* 376 */       String pwd = binder.get("IRMPassword");
/* 377 */       this.m_pwdSealingPwd.setText(pwd);
/*     */ 
/* 380 */       if (rsConfig.getStringValueByName("AllowOffline") != null)
/*     */       {
/* 383 */         if (!StringUtils.convertToBool(rsConfig.getStringValueByName("AllowOffline"), false))
/*     */         {
/* 386 */           this.m_chkAllowOffline.setSelected(false);
/*     */         }
/*     */         else
/*     */         {
/* 390 */           this.m_chkAllowOffline.setSelected(true);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 395 */       String rightsRefreshAmount = rsConfig.getStringValueByName("RefreshPeriodAmount");
/*     */ 
/* 397 */       String rightsRefreshUnits = rsConfig.getStringValueByName("RefreshPeriodUnits");
/*     */ 
/* 399 */       this.m_cmbRightsRefresh.setSelectedItem(rightsRefreshAmount + " " + rightsRefreshUnits);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 405 */       MessageBox.reportError(this.m_system, new IdcMessage(LocaleResources.getString("apFailedGetMsg", this.m_ctx), new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void okHandler(ActionEvent evt)
/*     */   {
/* 416 */     if (!validateAllFields())
/*     */     {
/* 418 */       return;
/*     */     }
/*     */ 
/* 421 */     String pwd = String.valueOf(this.m_pwdSealingPwd.getPassword());
/*     */ 
/* 423 */     DataBinder binder = new DataBinder();
/* 424 */     Properties localData = binder.getLocalData();
/*     */ 
/* 426 */     localData.put("DesktopURI", this.m_txtDesktopURI.getText());
/*     */ 
/* 428 */     localData.put("SealingURI", this.m_txtSealingURI.getText());
/*     */ 
/* 430 */     localData.put("HttpURI", this.m_txtHttpURI.getText());
/* 431 */     localData.put("SealingUName", this.m_txtSealingUname.getText());
/*     */ 
/* 433 */     localData.put("IRMPassword", pwd);
/* 434 */     localData.put("UnsealTypes", this.m_cmbUnsealType.getSelectedInternalValue());
/*     */ 
/* 436 */     localData.put("NonControlSealedContent", this.m_cmbNonControlSealedContent.getSelectedInternalValue());
/*     */ 
/* 440 */     DataResultSet featureMapResultSet = new DataResultSet();
/* 441 */     this.m_rightsOptions.setMappingResultSet();
/* 442 */     this.m_rightsOptions.getMappingResultSet(featureMapResultSet);
/* 443 */     if (featureMapResultSet.isEmpty())
/*     */     {
/* 445 */       MessageBox.reportError(this.m_system, new IdcMessage(LocaleResources.getString("apInvalidFeatureMappingMsg", this.m_ctx), new Object[0]));
/*     */ 
/* 447 */       return;
/*     */     }
/* 449 */     binder.addResultSet("rsFeatureMap", featureMapResultSet);
/*     */ 
/* 453 */     int isOfflineAllowed = 0;
/* 454 */     if (this.m_chkAllowOffline.isSelected())
/*     */     {
/* 456 */       isOfflineAllowed = 1;
/*     */     }
/* 458 */     localData.put("AllowOffline", String.valueOf(isOfflineAllowed));
/*     */ 
/* 462 */     String refreshPeriod = (String)this.m_cmbRightsRefresh.getSelectedItem();
/* 463 */     String[] refreshPeriodWithUnits = refreshPeriod.split(" ");
/*     */ 
/* 465 */     if (refreshPeriodWithUnits.length < 2)
/*     */     {
/* 467 */       MessageBox.reportError(this.m_system, new IdcMessage(LocaleResources.getString("apInvalidRefreshPeriodMsg", this.m_ctx), new Object[0]));
/*     */ 
/* 469 */       return;
/*     */     }
/* 471 */     localData.put("RefreshPeriodAmount", refreshPeriodWithUnits[0]);
/*     */ 
/* 473 */     localData.put("RefreshPeriodUnits", refreshPeriodWithUnits[1]);
/*     */     try
/*     */     {
/* 479 */       AppLauncher.executeService("IRM_UPDATE_CONFIG", binder);
/*     */ 
/* 481 */       MessageBox.showMessage(new DialogHelper(this.m_system, LocaleResources.getString("apLabelIRMProtection", this.m_ctx), true), new IdcMessage(LocaleResources.getString("apSuccessUpdateMsg", this.m_ctx), new Object[0]), 1);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 488 */       MessageBox.reportError(this.m_system, new IdcMessage(LocaleResources.getString("apFailedUpdateMsg", this.m_ctx), new Object[0]));
/*     */     }
/*     */ 
/* 491 */     dispose();
/*     */   }
/*     */ 
/*     */   protected void cancelHandler(ActionEvent evt)
/*     */   {
/* 500 */     dispose();
/*     */   }
/*     */ 
/*     */   protected boolean validateAllFields()
/*     */   {
/* 509 */     if ((this.m_txtDesktopURI.getText().trim().length() == 0) || (this.m_txtSealingURI.getText().trim().length() == 0) || (this.m_txtHttpURI.getText().trim().length() == 0) || (this.m_txtSealingUname.getText().trim().length() == 0) || (this.m_pwdSealingPwd.getPassword().length == 0) || (this.m_cmbUnsealType.getSelectedItem() == null) || (this.m_cmbNonControlSealedContent.getSelectedItem() == null) || (this.m_cmbRightsRefresh.getSelectedItem() == null))
/*     */     {
/* 518 */       MessageBox.reportError(this.m_system, new IdcMessage(LocaleResources.getString("apRequiredAllFieldsMsg", this.m_ctx), new Object[0]));
/*     */ 
/* 520 */       return false;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 530 */       DataBinder urlBinder = new DataBinder();
/* 531 */       urlBinder.putLocal("DesktopURI", this.m_txtDesktopURI.getText());
/* 532 */       urlBinder.putLocal("SealingURI", this.m_txtSealingURI.getText());
/* 533 */       urlBinder.putLocal("HttpURI", this.m_txtHttpURI.getText());
/* 534 */       AppLauncher.executeService("VALIDATE_IRM_URL", urlBinder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 538 */       MessageBox.reportError(this.m_system, e);
/* 539 */       return false;
/*     */     }
/*     */ 
/* 543 */     if (!IRMUtils.isInputValid(this.m_txtSealingUname.getText().trim()))
/*     */     {
/* 545 */       MessageBox.reportError(this.m_system, new IdcMessage(LocaleResources.getString("apInvalidCharFieldMsg", this.m_ctx), new Object[0]));
/*     */ 
/* 547 */       return false;
/*     */     }
/* 549 */     return true;
/*     */   }
/*     */ 
/*     */   protected void setToolTip()
/*     */   {
/* 558 */     this.m_lblDesktopURI.setToolTipText(LocaleResources.getString("apDesktopURIToolTip", this.m_ctx));
/*     */ 
/* 560 */     this.m_lblSealingURI.setToolTipText(LocaleResources.getString("apSealingURIToolTip", this.m_ctx));
/*     */ 
/* 562 */     this.m_lblHttpURI.setToolTipText(LocaleResources.getString("apHttpURIToolTip", this.m_ctx));
/*     */ 
/* 564 */     this.m_lblSealingUname.setToolTipText(LocaleResources.getString("apSealingUnameToolTip", this.m_ctx));
/*     */ 
/* 566 */     this.m_lblSealingPwd.setToolTipText(LocaleResources.getString("apSealingPwdToolTip", this.m_ctx));
/*     */ 
/* 570 */     this.m_lblUnsealType.setToolTipText(LocaleResources.getString("apUnsealTypeToolTip", this.m_ctx));
/*     */ 
/* 572 */     this.m_lblNonControlSealedContent.setToolTipText(LocaleResources.getString("apNonControlledContentToolTip", this.m_ctx));
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 583 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94362 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMConfigDlg
 * JD-Core Version:    0.5.4
 */