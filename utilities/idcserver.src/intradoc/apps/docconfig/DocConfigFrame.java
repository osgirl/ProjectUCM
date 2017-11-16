/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.MainFrame;
/*     */ import intradoc.apps.useradmin.irmintg.IRMConfigDlg;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.PromptHandler;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JMenuBar;
/*     */ import javax.swing.JMenuItem;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DocConfigFrame extends MainFrame
/*     */   implements ActionListener
/*     */ {
/*     */   protected String[][] m_panelInfo;
/*     */   protected TabPanel m_tabPanel;
/*     */   protected DocConfigPanel[] m_dcPanels;
/*     */ 
/*     */   public DocConfigFrame()
/*     */   {
/*  50 */     this.m_panelInfo = new String[][] { { "DocInfoPanel", "intradoc.apps.docconfig.DocInfoPanel", "apDocInfoPanelTitle" }, { "ApplicationInfoPanel", "intradoc.apps.docconfig.DocInfoPanel", "apApplicationInfoPanelTitle" }, { "SchemaTablePanel", "intradoc.apps.docconfig.SchemaTablePanel", "apSchemaTablesPanelTitle" }, { "DocSchemaPanel", "intradoc.apps.docconfig.DocSchemaPanel", "apDocSchemaPanelTitle" }, { "SchemaRelationPanel", "intradoc.apps.docconfig.SchemaRelationPanel", "apSchemaRelationsPanelTitle" }, { "DocClassPanel", "intradoc.apps.docconfig.DocClassPanel", "apDocClassPanelTitle" }, { "DocProfilePanel", "intradoc.apps.docconfig.DocProfilePanel", "apDocProfilePanelTitle" }, { "DocRulePanel", "intradoc.apps.docconfig.DocRulePanel", "apDocRulePanelTitle" } };
/*     */ 
/*  62 */     this.m_tabPanel = null;
/*  63 */     this.m_dcPanels = null;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void init(String title, boolean exitOnClose) throws ServiceException
/*     */   {
/*  69 */     IdcMessage msg = IdcMessageFactory.lc();
/*  70 */     msg.m_msgEncoded = title;
/*  71 */     init(msg, exitOnClose);
/*     */   }
/*     */ 
/*     */   public void init(IdcMessage title, boolean exitOnClose)
/*     */     throws ServiceException
/*     */   {
/*  77 */     super.init(title, exitOnClose);
/*  78 */     this.m_appHelper.attachToAppFrame(this, null, null, title);
/*     */ 
/*  80 */     this.m_cxt = this.m_appHelper.getExecutionContext();
/*     */ 
/*  82 */     buildMenu();
/*     */ 
/*  84 */     this.m_tabPanel = initInfoPanel();
/*     */ 
/*  86 */     PromptHandler allowCloseCallback = new PromptHandler()
/*     */     {
/*     */       public int prompt()
/*     */       {
/*  90 */         for (int i = 0; i < DocConfigFrame.this.m_panelInfo.length; ++i)
/*     */         {
/*  92 */           if (DocConfigFrame.this.m_dcPanels[i].canExit())
/*     */             continue;
/*  94 */           DocConfigFrame.this.m_tabPanel.selectPane(LocaleResources.getString(DocConfigFrame.this.m_panelInfo[i][2], DocConfigFrame.this.m_cxt));
/*  95 */           return 0;
/*     */         }
/*     */ 
/*  99 */         return 1;
/*     */       }
/*     */     };
/* 102 */     this.m_appHelper.m_isCloseAllowedCallback = allowCloseCallback;
/*     */ 
/* 104 */     pack();
/* 105 */     setVisible(true);
/*     */   }
/*     */ 
/*     */   protected TabPanel initInfoPanel()
/*     */     throws ServiceException
/*     */   {
/* 113 */     if (!SharedObjects.getEnvValueAsBoolean("IsBeta", false))
/*     */     {
/* 115 */       String[][] m_panelInfo_withoutDocClasses = { { "DocInfoPanel", "intradoc.apps.docconfig.DocInfoPanel", "apDocInfoPanelTitle" }, { "ApplicationInfoPanel", "intradoc.apps.docconfig.DocInfoPanel", "apApplicationInfoPanelTitle" }, { "SchemaTablePanel", "intradoc.apps.docconfig.SchemaTablePanel", "apSchemaTablesPanelTitle" }, { "DocSchemaPanel", "intradoc.apps.docconfig.DocSchemaPanel", "apDocSchemaPanelTitle" }, { "SchemaRelationPanel", "intradoc.apps.docconfig.SchemaRelationPanel", "apSchemaRelationsPanelTitle" }, { "DocProfilePanel", "intradoc.apps.docconfig.DocProfilePanel", "apDocProfilePanelTitle" }, { "DocRulePanel", "intradoc.apps.docconfig.DocRulePanel", "apDocRulePanelTitle" } };
/*     */ 
/* 126 */       this.m_panelInfo = m_panelInfo_withoutDocClasses;
/*     */     }
/*     */ 
/* 129 */     TabPanel tab = new TabPanel();
/*     */ 
/* 131 */     JPanel mainPanel = this.m_appHelper.m_mainPanel;
/* 132 */     mainPanel.setLayout(new BorderLayout());
/* 133 */     mainPanel.add("Center", tab);
/* 134 */     int numPanels = this.m_panelInfo.length;
/*     */ 
/* 136 */     this.m_dcPanels = new DocConfigPanel[numPanels];
/*     */ 
/* 138 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 140 */       this.m_dcPanels[i] = ((DocConfigPanel)ComponentClassFactory.createClassInstance(this.m_panelInfo[i][0], this.m_panelInfo[i][1], LocaleResources.getString("apUnableToLoadTabPanel", this.m_cxt, LocaleResources.getString(this.m_panelInfo[i][2], this.m_cxt))));
/*     */ 
/* 144 */       if (this.m_panelInfo[i][0].equals("SchemaTablePanel"))
/*     */       {
/* 146 */         SchemaTablePanel panel = (SchemaTablePanel)this.m_dcPanels[i];
/* 147 */         panel.m_verticalButtons = false;
/*     */       }
/* 149 */       this.m_dcPanels[i].setBaseClassName(this.m_panelInfo[i][0]);
/* 150 */       this.m_dcPanels[i].init(this.m_appHelper);
/* 151 */       tab.addPane(LocaleResources.getString(this.m_panelInfo[i][2], this.m_cxt), this.m_dcPanels[i]);
/*     */     }
/*     */ 
/* 154 */     return tab;
/*     */   }
/*     */ 
/*     */   protected void buildMenu()
/*     */   {
/* 159 */     JMenuBar mb = new JMenuBar();
/* 160 */     setJMenuBar(mb);
/*     */ 
/* 162 */     JMenu optMenu = new JMenu(LocaleResources.getString("apOptionsMenuLabel", this.m_cxt));
/* 163 */     mb.add(optMenu);
/*     */ 
/* 165 */     String[][] menuInfo = { { "apSchemaRepublishBase", "publishSchemaBase" }, { "apSchemaRepublish", "publishSchema" }, { "apWeblayoutFilesRepublish", "publishWebLayoutFiles" }, { "apStaticFilesRepublish", "publishStaticFiles" }, { "apDocTypesDialog", "docTypes" }, { "apDocFormatsDialog", "docFormats" } };
/*     */ 
/* 175 */     for (int i = 0; i < menuInfo.length; ++i)
/*     */     {
/* 177 */       JMenuItem mi = new JMenuItem(this.m_appHelper.getString(menuInfo[i][0]));
/*     */ 
/* 179 */       mi.addActionListener(this);
/* 180 */       mi.setActionCommand(menuInfo[i][1]);
/* 181 */       optMenu.add(mi);
/*     */     }
/*     */ 
/* 184 */     addStandardOptions(optMenu);
/*     */ 
/* 187 */     addAppMenu(mb);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/*     */     try
/*     */     {
/* 197 */       String cmd = e.getActionCommand();
/* 198 */       if (cmd.equals("publishSchemaBase"))
/*     */       {
/* 200 */         DataBinder binder = new DataBinder();
/* 201 */         binder.putLocal("UserPublishingRequest", "1");
/* 202 */         binder.putLocal("publishOperation", "base");
/* 203 */         AppLauncher.executeService("PUBLISH_SCHEMA", binder);
/*     */       }
/* 205 */       else if (cmd.equals("publishSchema"))
/*     */       {
/* 207 */         DataBinder binder = new DataBinder();
/* 208 */         binder.putLocal("UserPublishingRequest", "1");
/* 209 */         AppLauncher.executeService("PUBLISH_SCHEMA", binder);
/*     */       }
/* 211 */       else if (cmd.equals("publishWebLayoutFiles"))
/*     */       {
/* 213 */         DataBinder binder = new DataBinder();
/* 214 */         AppLauncher.executeService("PUBLISH_WEBLAYOUT_FILES", binder);
/*     */       }
/* 216 */       else if (cmd.equals("publishStaticFiles"))
/*     */       {
/* 218 */         DataBinder binder = new DataBinder();
/* 219 */         AppLauncher.executeService("PUBLISH_STATIC_FILES", binder);
/*     */       }
/* 221 */       else if (cmd.equals("docTypes"))
/*     */       {
/* 223 */         String title = this.m_appHelper.getString("apDocTypesDialogTitle");
/* 224 */         String helpPage = DialogHelpTable.getHelpPage("EditDocTypes");
/* 225 */         DocTypesDialog typeDlg = new DocTypesDialog(this.m_appHelper, title, helpPage);
/* 226 */         typeDlg.init();
/*     */       }
/* 228 */       else if (cmd.equals("docFormats"))
/*     */       {
/* 230 */         String title = this.m_appHelper.getString("apDocFormatsDialogTitle");
/* 231 */         String helpPage = DialogHelpTable.getHelpPage("EditDocFormats");
/* 232 */         DocFormatsDlg formatDlg = new DocFormatsDlg(this.m_appHelper, title, helpPage);
/* 233 */         formatDlg.init();
/*     */       }
/* 235 */       else if (cmd.equals("irmConfig"))
/*     */       {
/* 238 */         IRMConfigDlg irmConfigDlg = new IRMConfigDlg(this.m_appHelper, LocaleResources.getString("apLabelIRMConfig", this.m_cxt));
/*     */ 
/* 240 */         irmConfigDlg.init();
/*     */       }
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 245 */       AppLauncher.reportOperationError(this.m_appHelper, exp, IdcMessageFactory.lc("", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 251 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99987 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.DocConfigFrame
 * JD-Core Version:    0.5.4
 */