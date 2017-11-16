/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.DocProfileScriptUtils;
/*     */ import intradoc.shared.SharedContext;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditRuleDlg
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_action;
/*     */   protected DataBinder m_binder;
/*     */   protected String m_helpPage;
/*  53 */   protected ExecutionContext m_context = null;
/*  54 */   protected SharedContext m_shContext = null;
/*     */ 
/*  56 */   protected TabPanel m_tabs = null;
/*  57 */   protected DocConfigPanel[] m_panels = null;
/*  58 */   protected static String[][] PANEL_INFO = { { "EditRuleGeneralPanel", "intradoc.apps.docconfig.EditRuleGeneralPanel", "apDpEditRuleGeneralPanelTitle", "general" }, { "EditRuleFieldsPanel", "intradoc.apps.docconfig.EditRuleFieldsPanel", "apDpEditRuleFieldsPanelTitle", "fields" } };
/*     */ 
/*     */   public EditRuleDlg(SystemInterface sys, String title, SharedContext shContext, String helpPage)
/*     */   {
/*  66 */     this.m_helper = new DialogHelper(sys, title, true);
/*  67 */     this.m_systemInterface = sys;
/*  68 */     this.m_context = sys.getExecutionContext();
/*  69 */     this.m_shContext = shContext;
/*     */ 
/*  71 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public boolean init(Properties data, boolean isNew)
/*     */   {
/*  76 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/*  83 */           if (EditRuleDlg.this.m_tabs.validateAllPanes())
/*     */           {
/*  85 */             AppContextUtils.executeService(EditRuleDlg.this.m_shContext, EditRuleDlg.this.m_action, EditRuleDlg.this.m_binder);
/*  86 */             return true;
/*     */           }
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/*  91 */           this.m_errorMessage = LocaleUtils.createMessageListFromThrowable(exp);
/*     */         }
/*  93 */         return false;
/*     */       }
/*     */     };
/*  96 */     okCallback.m_dlgHelper = this.m_helper;
/*  97 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 2, true, this.m_helpPage);
/*     */     try
/*     */     {
/* 102 */       retrieveInformation(data, isNew);
/* 103 */       initUI(mainPanel);
/* 104 */       loadInformation();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 108 */       MessageBox.reportError(this.m_systemInterface, e);
/* 109 */       return false;
/*     */     }
/* 111 */     return true;
/*     */   }
/*     */ 
/*     */   protected void initUI(JPanel mainPanel) throws ServiceException
/*     */   {
/* 116 */     initPanels(mainPanel);
/*     */   }
/*     */ 
/*     */   protected void initPanels(JPanel mainPanel) throws ServiceException
/*     */   {
/* 121 */     int numPanels = PANEL_INFO.length;
/* 122 */     this.m_tabs = new TabPanel();
/* 123 */     this.m_panels = new DocConfigPanel[numPanels];
/*     */ 
/* 125 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 127 */       this.m_panels[i] = ((DocConfigPanel)ComponentClassFactory.createClassInstance(PANEL_INFO[i][0], PANEL_INFO[i][1], LocaleResources.getString("apUnableToLoadTabPanel", this.m_context, LocaleResources.getString(PANEL_INFO[i][2], this.m_context))));
/*     */ 
/* 131 */       this.m_panels[i].initEx(this.m_systemInterface, this.m_binder);
/* 132 */       this.m_tabs.addPane(this.m_systemInterface.getString(PANEL_INFO[i][2]), this.m_panels[i], this.m_panels[i], false, this.m_panels[i]);
/*     */     }
/*     */ 
/* 136 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 137 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 138 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 139 */     this.m_helper.addLastComponentInRow(mainPanel, this.m_tabs);
/*     */   }
/*     */ 
/*     */   protected void retrieveInformation(Properties props, boolean isNew) throws ServiceException
/*     */   {
/* 144 */     this.m_binder = new DataBinder();
/* 145 */     this.m_binder.setLocalData(props);
/* 146 */     if (isNew)
/*     */     {
/* 148 */       this.m_action = "ADD_DOCRULE";
/*     */     }
/*     */     else
/*     */     {
/* 152 */       this.m_action = "EDIT_DOCRULE";
/* 153 */       AppContextUtils.executeService(this.m_shContext, "GET_DOCRULE", this.m_binder);
/*     */     }
/*     */ 
/* 156 */     DataResultSet fieldSet = (DataResultSet)this.m_binder.getResultSet("RuleFields");
/* 157 */     if (fieldSet == null)
/*     */     {
/* 160 */       fieldSet = new DataResultSet(DocProfileScriptUtils.DP_FIELD_RULE_COLUMNS);
/* 161 */       this.m_binder.addResultSet("RuleFields", fieldSet);
/*     */     }
/*     */ 
/* 164 */     this.m_helper.m_props = this.m_binder.getLocalData();
/*     */   }
/*     */ 
/*     */   protected void loadInformation()
/*     */   {
/* 169 */     int numPanels = this.m_panels.length;
/* 170 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 172 */       this.m_panels[i].loadComponents();
/*     */     }
/*     */   }
/*     */ 
/*     */   public int prompt(Properties props, boolean isNew)
/*     */   {
/* 178 */     if (!init(props, isNew))
/*     */     {
/* 180 */       return 0;
/*     */     }
/*     */ 
/* 183 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 188 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditRuleDlg
 * JD-Core Version:    0.5.4
 */