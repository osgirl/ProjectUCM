/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditProfileScriptDlg
/*     */ {
/*  47 */   protected SystemInterface m_systemInterface = null;
/*  48 */   protected ExecutionContext m_cxt = null;
/*  49 */   protected SharedContext m_shContext = null;
/*  50 */   protected String m_helpPage = null;
/*     */ 
/*  52 */   protected RuleBuilderHelper m_scriptHelper = null;
/*  53 */   protected String m_valueLabel = null;
/*  54 */   protected String m_valueName = null;
/*     */ 
/*  56 */   protected String m_tableName = null;
/*  57 */   protected DataBinder m_scriptData = null;
/*  58 */   protected Properties m_configProps = null;
/*     */ 
/*  61 */   protected DialogHelper m_helper = null;
/*  62 */   protected TabPanel m_tabs = null;
/*     */ 
/*  65 */   protected final String[][] PANEL_INFOS = { { "ScriptClausePanel", "intradoc.apps.docconfig.ScriptClausePanel", "apDpConditionsTitle", "" }, { "ScriptCustomPanel", "intradoc.apps.docconfig.ScriptCustomPanel", "apDpCustomTitle", "" }, { "ScriptSideEffectsPanel", "intradoc.apps.docconfig.ScriptSideEffectsPanel", "apDpSideEffectsTitle", "activation" } };
/*     */ 
/*     */   public EditProfileScriptDlg(SystemInterface sys, SharedContext shContext, String title, String helpPage)
/*     */   {
/*  76 */     this.m_systemInterface = sys;
/*  77 */     this.m_cxt = sys.getExecutionContext();
/*  78 */     this.m_shContext = shContext;
/*  79 */     this.m_helper = new DialogHelper(sys, title, true);
/*  80 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(DataBinder binder, Properties configProps, ViewFieldDef fieldDef)
/*     */   {
/*  85 */     DialogCallback okCallback = createOkCallback();
/*     */ 
/*  87 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/*  89 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/*  92 */     JPanel top = new CustomPanel();
/*  93 */     gh.prepareAddLastRowElement();
/*  94 */     gh.m_gc.weighty = 1.0D;
/*  95 */     this.m_helper.addComponent(mainPanel, top);
/*     */ 
/*  97 */     gh.useGridBag(top);
/*  98 */     this.m_helper.addPanelTitle(top, LocaleResources.getString("apDpScriptTitle", this.m_cxt));
/*  99 */     gh.m_gc.fill = 2;
/* 100 */     gh.m_gc.weightx = 1.0D;
/*     */ 
/* 102 */     int result = 0;
/*     */     try
/*     */     {
/* 105 */       prepareScriptData(binder, configProps);
/* 106 */       initTabs(top, binder, configProps, fieldDef);
/* 107 */       result = this.m_helper.prompt();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 111 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/* 113 */     return result;
/*     */   }
/*     */ 
/*     */   protected void prepareScriptData(DataBinder binder, Properties configProps)
/*     */   {
/* 118 */     this.m_scriptData = binder;
/*     */ 
/* 120 */     this.m_tableName = configProps.getProperty("TableName");
/*     */   }
/*     */ 
/*     */   protected void initTabs(JPanel top, DataBinder binder, Properties configProps, ViewFieldDef fieldDef)
/*     */     throws ServiceException
/*     */   {
/* 127 */     String scriptType = configProps.getProperty("ScriptType");
/* 128 */     this.m_tabs = new TabPanel();
/* 129 */     for (int i = 0; i < this.PANEL_INFOS.length; ++i)
/*     */     {
/* 131 */       String params = this.PANEL_INFOS[i][3];
/* 132 */       boolean isAddTab = (params.length() == 0) || (params.indexOf(scriptType) >= 0);
/* 133 */       if (!isAddTab)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 138 */       DocConfigPanel editPanel = (DocConfigPanel)ComponentClassFactory.createClassInstance(this.PANEL_INFOS[i][0], this.PANEL_INFOS[i][1], LocaleResources.getString("apUnableToLoadPanel", this.m_cxt, this.PANEL_INFOS[i][0]));
/*     */ 
/* 143 */       if (editPanel instanceof ScriptClausePanel)
/*     */       {
/* 145 */         ScriptClausePanel pnl = (ScriptClausePanel)editPanel;
/* 146 */         pnl.setFieldDef(fieldDef);
/*     */       }
/* 148 */       editPanel.loadConfiguration(configProps);
/* 149 */       editPanel.initEx(this.m_systemInterface, binder);
/*     */ 
/* 151 */       this.m_tabs.addPane(LocaleResources.getString(this.PANEL_INFOS[i][2], this.m_cxt), editPanel, editPanel, false, editPanel);
/*     */     }
/*     */ 
/* 155 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 156 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 157 */     this.m_helper.addComponent(top, this.m_tabs);
/*     */   }
/*     */ 
/*     */   protected DialogCallback createOkCallback()
/*     */   {
/* 162 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 170 */           EditProfileScriptDlg.this.m_tabs.validateAllPanes();
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 174 */           this.m_errorMessage = LocaleUtils.createMessageListFromThrowable(exp);
/* 175 */           return false;
/*     */         }
/* 177 */         return true;
/*     */       }
/*     */     };
/* 181 */     return okCallback;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 186 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditProfileScriptDlg
 * JD-Core Version:    0.5.4
 */