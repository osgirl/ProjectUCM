/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.utils.ComponentPreferenceData;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.SystemColor;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.io.IOException;
/*     */ import java.util.Iterator;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class CWizardComponentConfigDlg extends CWizardBaseDlg
/*     */ {
/*     */   protected ComponentPreferenceData m_prefData;
/*     */   protected DialogHelper m_dlgHelper;
/*     */   protected boolean m_isInstall;
/*     */   protected DataBinder m_binder;
/*     */   protected ResourceContainer m_configResources;
/*     */ 
/*     */   public CWizardComponentConfigDlg(SystemInterface sys, String title, String helpPage, ComponentPreferenceData prefData, ResourceContainer configResources, boolean isInstall)
/*     */   {
/*  70 */     super(sys, title, helpPage);
/*  71 */     this.m_prefData = prefData;
/*  72 */     this.m_isInstall = isInstall;
/*  73 */     this.m_binder = new DataBinder(SharedObjects.getSafeEnvironment());
/*  74 */     this.m_configResources = configResources;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  81 */     if (this.m_prefData == null)
/*     */     {
/*  83 */       this.m_prefData = new ComponentPreferenceData();
/*     */     }
/*  85 */     if (this.m_configResources == null)
/*     */     {
/*  87 */       this.m_configResources = new ResourceContainer();
/*     */     }
/*     */ 
/*  90 */     initUI();
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  97 */     this.m_dlgHelper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("csCompWizEditPref", null), true, true);
/*     */ 
/*  99 */     this.m_dlgHelper.m_helpPage = DialogHelpTable.getHelpPage("CW_EditPreferenceData");
/*     */ 
/* 101 */     JPanel mainPanel = this.m_dlgHelper.m_mainPanel;
/* 102 */     this.m_dlgHelper.makePanelGridBag(mainPanel, 1);
/* 103 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 110 */           CWizardComponentConfigDlg.this.onEditPreference();
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 114 */           CWizardGuiUtils.reportError(CWizardComponentConfigDlg.this.m_systemInterface, exp, (IdcMessage)null);
/* 115 */           return false;
/*     */         }
/* 117 */         return true;
/*     */       }
/*     */     };
/* 120 */     DialogCallback resetCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 127 */           CWizardComponentConfigDlg.this.onResetPreferences();
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 131 */           return false;
/*     */         }
/* 133 */         return true;
/*     */       }
/*     */     };
/* 137 */     this.m_dlgHelper.addOK(okCallback);
/* 138 */     this.m_dlgHelper.addCancel(null);
/* 139 */     this.m_dlgHelper.addReset(resetCallback);
/*     */ 
/* 143 */     DataResultSet prefTable = this.m_prefData.getPreferenceTable();
/* 144 */     FieldInfo[] fi = null;
/*     */     try
/*     */     {
/* 147 */       fi = ResultSetUtils.createInfoList(prefTable, ComponentPreferenceData.PREF_FIELD_INFO, true);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 151 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/* 152 */       return;
/*     */     }
/*     */ 
/* 158 */     for (prefTable.first(); prefTable.isRowPresent(); prefTable.next())
/*     */     {
/* 160 */       String msgType = prefTable.getStringValue(fi[9].m_index);
/* 161 */       if (msgType.equalsIgnoreCase("info"))
/*     */         continue;
/* 163 */       String pName = prefTable.getStringValue(fi[0].m_index);
/* 164 */       String pValue = prefTable.getStringValue(fi[6].m_index);
/*     */ 
/* 166 */       if (this.m_isInstall)
/*     */       {
/* 168 */         pValue = evaluateStr(pValue);
/*     */       }
/*     */       else
/*     */       {
/* 172 */         pValue = this.m_prefData.m_configData.getProperty(pName);
/* 173 */         if (pValue == null)
/*     */         {
/* 175 */           pValue = this.m_prefData.m_installData.getProperty(pName);
/* 176 */           if (pValue == null)
/*     */           {
/* 178 */             pValue = "";
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 183 */       this.m_dlgHelper.m_props.put(pName, pValue);
/*     */ 
/* 185 */       if (!this.m_isInstall)
/*     */         continue;
/* 187 */       this.m_prefData.m_configData.put(pName, pValue);
/*     */     }
/*     */ 
/* 192 */     String helpButtonText = LocaleResources.getString("csCompWizHelpButtonLabel", null);
/* 193 */     IdcMessage helpButtonTitle = IdcMessageFactory.lc("csCompWizHelpButtonTitle", new Object[0]);
/*     */ 
/* 197 */     for (prefTable.first(); prefTable.isRowPresent(); prefTable.next())
/*     */     {
/* 199 */       String pName = prefTable.getStringValue(fi[0].m_index);
/* 200 */       String pMsg = prefTable.getStringValue(fi[1].m_index);
/* 201 */       String pType = prefTable.getStringValue(fi[3].m_index);
/* 202 */       String pOptListName = prefTable.getStringValue(fi[4].m_index);
/* 203 */       String pCol = prefTable.getStringValue(fi[5].m_index);
/* 204 */       String msgType = prefTable.getStringValue(fi[9].m_index);
/* 205 */       String pIsDisabled = prefTable.getStringValue(fi[10].m_index);
/* 206 */       String pLabel = prefTable.getStringValue(fi[11].m_index);
/*     */ 
/* 209 */       if ((pMsg != null) && (pMsg.length() > 0))
/*     */       {
/* 211 */         pMsg = evaluateStr(pMsg);
/* 212 */         pMsg = LocaleResources.getString(pMsg, null);
/*     */       }
/* 214 */       if ((pLabel != null) && (pLabel.length() > 0))
/*     */       {
/* 218 */         pLabel = LocaleResources.getString(pLabel, null);
/*     */       }
/*     */       else
/*     */       {
/* 222 */         pLabel = pName;
/*     */       }
/*     */ 
/* 226 */       if (!msgType.equalsIgnoreCase("info"))
/*     */       {
/* 230 */         boolean disableRow = false;
/* 231 */         if ((pIsDisabled != null) && (pIsDisabled.length() > 0))
/*     */         {
/* 233 */           pIsDisabled = evaluateStr(pIsDisabled);
/* 234 */           disableRow = StringUtils.convertToBool(pIsDisabled, false);
/*     */         }
/* 236 */         else if (((this.m_isInstall) && (msgType.equalsIgnoreCase("postinstallonly"))) || ((!this.m_isInstall) && (msgType.equalsIgnoreCase("installonly"))))
/*     */         {
/* 239 */           disableRow = true;
/*     */         }
/*     */ 
/* 243 */         Component newPromptField = new JTextField(40);
/* 244 */         CustomLabel newPromptDesc = null;
/* 245 */         if (pType.equalsIgnoreCase("boolean"))
/*     */         {
/* 247 */           newPromptField = new JCheckBox(" ");
/*     */         }
/* 249 */         else if (disableRow)
/*     */         {
/* 251 */           newPromptField = new CustomLabel((String)this.m_dlgHelper.m_props.get(pName));
/*     */         }
/* 255 */         else if (pType.equalsIgnoreCase("options"))
/*     */         {
/* 257 */           DataResultSet drset = new DataResultSet();
/* 258 */           Table t = this.m_configResources.getTable(pOptListName);
/* 259 */           if ((t != null) && (t.getNumRows() > 0))
/*     */           {
/* 261 */             drset.init(t);
/*     */           }
/*     */           else
/*     */           {
/* 265 */             drset = SharedObjects.getTable(pOptListName);
/*     */           }
/* 267 */           ComboChoice choice = new ComboChoice(false);
/*     */ 
/* 269 */           if ((drset != null) && (!drset.isEmpty()))
/*     */           {
/*     */             try
/*     */             {
/* 274 */               FieldInfo[] fi2 = ResultSetUtils.createInfoList(drset, new String[] { pCol }, true);
/* 275 */               for (drset.first(); drset.isRowPresent(); drset.next())
/*     */               {
/* 277 */                 String name = drset.getStringValue(fi2[0].m_index);
/* 278 */                 choice.add(LocaleResources.getString(name, null));
/*     */               }
/*     */             }
/*     */             catch (DataException de)
/*     */             {
/* 283 */               CWizardGuiUtils.reportError(this.m_systemInterface, de, (IdcMessage)null);
/*     */             }
/*     */           }
/* 286 */           newPromptField = choice;
/*     */         }
/*     */ 
/* 291 */         newPromptDesc = this.m_dlgHelper.addLabelFieldPairEx(mainPanel, pLabel + ":", newPromptField, pName, false);
/*     */ 
/* 294 */         JButton helpButton = new JButton(helpButtonText);
/* 295 */         IdcMessage helpMsg = IdcMessageFactory.lc();
/* 296 */         helpMsg.m_msgLocalized = pMsg;
/* 297 */         ActionListener al = new ActionListener(helpButtonTitle, helpMsg)
/*     */         {
/*     */           public void actionPerformed(ActionEvent e)
/*     */           {
/* 301 */             CWizardGuiUtils.doMessage(CWizardComponentConfigDlg.this.m_systemInterface, this.val$helpButtonTitle, this.val$helpMsg, 1);
/*     */           }
/*     */         };
/* 304 */         helpButton.addActionListener(al);
/* 305 */         this.m_dlgHelper.addLastComponentInRow(mainPanel, helpButton);
/*     */ 
/* 308 */         if (disableRow)
/*     */         {
/* 310 */           newPromptField.setEnabled(false);
/* 311 */           newPromptDesc.setForeground(SystemColor.textInactiveText);
/* 312 */           newPromptField.setForeground(SystemColor.textInactiveText);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 318 */         CWizardGuiUtils.addDescription(this.m_dlgHelper, mainPanel, pMsg);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 323 */     if (this.m_isInstall)
/*     */       return;
/* 325 */     ActionListener revertListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent arg0)
/*     */       {
/* 329 */         CWizardComponentConfigDlg.this.m_dlgHelper.m_props.clear();
/* 330 */         CWizardComponentConfigDlg.this.m_dlgHelper.m_props.putAll(CWizardComponentConfigDlg.this.m_prefData.m_installData);
/* 331 */         CWizardComponentConfigDlg.this.m_dlgHelper.m_exchange.exchange(CWizardComponentConfigDlg.this.m_dlgHelper, true);
/*     */       }
/*     */     };
/* 334 */     this.m_dlgHelper.addCommandButton(LocaleResources.getString("csRevertToInstallSettings", null), revertListener);
/*     */   }
/*     */ 
/*     */   protected void onResetPreferences()
/*     */   {
/* 340 */     this.m_dlgHelper.m_props.clear();
/* 341 */     this.m_dlgHelper.m_props.putAll(this.m_prefData.m_configData);
/* 342 */     this.m_dlgHelper.m_exchange.exchange(this.m_dlgHelper, true);
/*     */   }
/*     */ 
/*     */   public boolean hasPreferencesToConfigure()
/*     */   {
/* 353 */     return (((!this.m_isInstall) || (this.m_prefData.hasInstallPrefs()))) && (((this.m_isInstall) || (this.m_prefData.hasPostInstallPrefs())));
/*     */   }
/*     */ 
/*     */   public boolean configureComponent()
/*     */   {
/* 360 */     return this.m_dlgHelper.prompt() == 1;
/*     */   }
/*     */ 
/*     */   protected void onEditPreference() throws DataException
/*     */   {
/* 365 */     Properties tempProps = new Properties();
/* 366 */     DataResultSet prefTable = this.m_prefData.getPreferenceTable();
/* 367 */     FieldInfo[] infos = ResultSetUtils.createInfoList(prefTable, new String[] { "pName", "pNewMsgType", "pPromptType" }, true);
/* 368 */     for (prefTable.first(); prefTable.isRowPresent(); prefTable.next())
/*     */     {
/* 370 */       String msgType = prefTable.getStringValue(infos[1].m_index);
/* 371 */       if ((msgType.equalsIgnoreCase("info")) || ((!this.m_isInstall) && (msgType.equalsIgnoreCase("installonly")))) {
/*     */         continue;
/*     */       }
/* 374 */       String name = prefTable.getStringValue(infos[0].m_index);
/* 375 */       if ((name == null) || (name.length() <= 0))
/*     */         continue;
/* 377 */       tempProps.put(name, this.m_dlgHelper.m_props.getProperty(name));
/*     */     }
/*     */ 
/* 383 */     ComponentPreferenceData.validatePreferenceData(prefTable, tempProps);
/*     */ 
/* 385 */     for (Iterator i$ = tempProps.keySet().iterator(); i$.hasNext(); ) { Object propName = i$.next();
/*     */ 
/* 387 */       String name = (String)propName;
/* 388 */       addProp(tempProps, name, this.m_prefData.m_configData); }
/*     */ 
/*     */   }
/*     */ 
/*     */   protected String evaluateStr(String str)
/*     */   {
/* 394 */     PageMerger pageMerger = new PageMerger(this.m_binder, null);
/*     */     try
/*     */     {
/* 397 */       str = pageMerger.evaluateScript(str);
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 401 */       if (SystemUtils.m_verbose)
/*     */       {
/* 403 */         Report.trace("componentwizard", null, ignore);
/*     */       }
/*     */     }
/* 406 */     return str;
/*     */   }
/*     */ 
/*     */   protected void addProp(Properties fromProps, String name, Properties toProps)
/*     */   {
/* 412 */     String val = fromProps.getProperty(name);
/* 413 */     if ((val == null) || (val.length() == 0))
/*     */     {
/* 416 */       if (!toProps.containsKey(name))
/*     */         return;
/* 418 */       toProps.remove(name);
/*     */     }
/*     */     else
/*     */     {
/* 423 */       toProps.put(name, val);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 429 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.CWizardComponentConfigDlg
 * JD-Core Version:    0.5.4
 */