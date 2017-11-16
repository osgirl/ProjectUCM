/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.workflow.JumpClausesData;
/*     */ import intradoc.shared.workflow.WfScriptStorage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditExitConditionDlg
/*     */ {
/*  54 */   protected SystemInterface m_systemInterface = null;
/*  55 */   protected ExecutionContext m_cxt = null;
/*  56 */   protected WorkflowContext m_context = null;
/*  57 */   protected String m_helpPage = null;
/*  58 */   protected DocumentLocalizedProfile m_docProfile = null;
/*     */ 
/*  60 */   protected WorkflowStateInfo m_workflowInfo = null;
/*  61 */   protected ScriptBuilderHelper m_scriptHelper = null;
/*  62 */   protected WfScriptStorage m_scriptData = null;
/*     */ 
/*  64 */   protected DialogHelper m_helper = null;
/*     */ 
/*     */   public EditExitConditionDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  68 */     this.m_systemInterface = sys;
/*  69 */     this.m_cxt = sys.getExecutionContext();
/*  70 */     this.m_helper = new DialogHelper(sys, title, true);
/*  71 */     this.m_helpPage = helpPage;
/*     */ 
/*  73 */     UserData userData = AppLauncher.getUserData();
/*  74 */     this.m_docProfile = new DocumentLocalizedProfile(userData, 1, this.m_cxt);
/*     */   }
/*     */ 
/*     */   public int init(WorkflowStateInfo wfInfo, WorkflowContext ctxt)
/*     */   {
/*  79 */     this.m_workflowInfo = wfInfo;
/*  80 */     this.m_context = ctxt;
/*     */ 
/*  82 */     DialogCallback okCallback = createOkCallback();
/*     */ 
/*  84 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, DialogHelpTable.getHelpPage(this.m_helpPage));
/*     */ 
/*  86 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/*  89 */     JPanel top = new PanePanel();
/*  90 */     gh.prepareAddLastRowElement();
/*  91 */     this.m_helper.addComponent(mainPanel, top);
/*     */ 
/*  93 */     gh.useGridBag(top);
/*  94 */     this.m_helper.addPanelTitle(top, LocaleResources.getString("apTitleAdditionalExitCondition", this.m_cxt));
/*  95 */     gh.m_gc.fill = 2;
/*  96 */     gh.m_gc.weightx = 1.0D;
/*     */ 
/*  98 */     JPanel pnl = initScriptHelper();
/*  99 */     gh.m_gc.weighty = 1.0D;
/* 100 */     gh.m_gc.fill = 1;
/* 101 */     this.m_helper.addComponent(mainPanel, pnl);
/*     */ 
/* 103 */     int result = 0;
/*     */     try
/*     */     {
/* 106 */       result = this.m_helper.prompt();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 110 */       this.m_context.reportError(e);
/*     */     }
/*     */ 
/* 113 */     return result;
/*     */   }
/*     */ 
/*     */   protected DialogCallback createOkCallback()
/*     */   {
/* 118 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 125 */           EditExitConditionDlg.this.m_scriptHelper.enableDisable(false);
/* 126 */           String str = EditExitConditionDlg.this.m_scriptHelper.getFormatString();
/* 127 */           EditExitConditionDlg.this.m_workflowInfo.setValue("wfAdditionalExitCondition", str);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 131 */           this.m_errorMessage = IdcMessageFactory.lc(exp);
/* 132 */           return false;
/*     */         }
/* 134 */         return true;
/*     */       }
/*     */     };
/* 137 */     return okCallback;
/*     */   }
/*     */ 
/*     */   protected JPanel initScriptHelper()
/*     */   {
/* 142 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/* 144 */     JPanel scriptDefPanel = new PanePanel();
/* 145 */     gh.m_gc.weightx = 1.0D;
/* 146 */     gh.m_gc.weighty = 1.0D;
/* 147 */     gh.m_gc.fill = 1;
/*     */ 
/* 149 */     this.m_scriptHelper = new ScriptBuilderHelper();
/* 150 */     this.m_scriptHelper.init(this.m_systemInterface);
/* 151 */     this.m_scriptHelper.setDocumentProfile(this.m_docProfile);
/* 152 */     this.m_scriptHelper.setQueryLabels("apConditionClause", "apCustomConditionClause");
/* 153 */     this.m_scriptHelper.createStandardQueryPanel(this.m_helper, scriptDefPanel, this.m_context.getSharedContext());
/*     */ 
/* 157 */     ResultSet metaFields = SharedObjects.getTable("DocMetaDefinition");
/* 158 */     ViewFields docFieldsObj = new ViewFields(this.m_cxt);
/*     */     try
/*     */     {
/* 162 */       ViewFieldDef fieldDef = docFieldsObj.addField("lastEntryTs", LocaleResources.getString("apTitleStepEntryDate", this.m_cxt));
/*     */ 
/* 164 */       fieldDef.m_type = "date";
/*     */ 
/* 166 */       fieldDef = docFieldsObj.addField("entryCount", LocaleResources.getString("apTitleStepEntryCount", this.m_cxt));
/*     */ 
/* 168 */       fieldDef.m_type = "int";
/*     */ 
/* 170 */       docFieldsObj.addStandardDocFields();
/* 171 */       docFieldsObj.addField("dFormat", LocaleResources.getString("apTitlePrimaryFileFormat", this.m_cxt));
/*     */ 
/* 173 */       docFieldsObj.addDocDateFields(true, false);
/* 174 */       docFieldsObj.addMetaFields(metaFields);
/*     */ 
/* 176 */       Vector docFields = docFieldsObj.m_viewFields;
/* 177 */       this.m_scriptHelper.setFieldList(docFields, null);
/* 178 */       this.m_scriptHelper.setDisplayMaps(docFieldsObj.m_tableFields.m_displayMaps);
/* 179 */       setCustomValueDisplayMaps();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 183 */       this.m_context.reportError(e, IdcMessageFactory.lc("apErrorCreatingScriptFieldList", new Object[0]));
/*     */     }
/*     */ 
/* 186 */     String cndKey = "wfAdditionalExitCondition";
/* 187 */     String condStr = this.m_workflowInfo.get(cndKey);
/*     */ 
/* 189 */     JumpClausesData clausesData = new JumpClausesData(true);
/* 190 */     clausesData.setClauseDisplay(null, " and\n");
/* 191 */     this.m_scriptHelper.setData(clausesData, condStr);
/*     */     try
/*     */     {
/* 195 */       this.m_scriptHelper.loadData();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 199 */       this.m_context.reportError(e, IdcMessageFactory.lc("apUnableToInitScriptBuilder", new Object[0]));
/*     */     }
/* 201 */     return scriptDefPanel;
/*     */   }
/*     */ 
/*     */   protected void setCustomValueDisplayMaps()
/*     */   {
/* 206 */     this.m_scriptHelper.setDisplayMap("prevDates", new String[][] { { "dateCurrent(-1)", LocaleResources.getString("apOneDayAgo", this.m_cxt) }, { "dateCurrent(-7)", LocaleResources.getString("apOneWeekAgo", this.m_cxt) }, { "dateCurrent(-28)", LocaleResources.getString("apFourWeeksAgo", this.m_cxt) } });
/*     */ 
/* 213 */     this.m_scriptHelper.setFieldOptionListKey("lastEntryTs", "prevDates", "combo");
/* 214 */     this.m_scriptHelper.setFieldOptionListKey("dInDate", "prevDates", "combo");
/* 215 */     this.m_scriptHelper.setFieldOptionListKey("dCreateDate", "prevDates", "combo");
/*     */ 
/* 217 */     this.m_scriptHelper.setDisplayMap("futureDates", new String[][] { { "dateCurrent(1)", LocaleResources.getString("apOneDay", this.m_cxt) }, { "dateCurrent(7)", LocaleResources.getString("apOneWeek", this.m_cxt) }, { "dateCurrent(28)", LocaleResources.getString("apFourWeeks", this.m_cxt) } });
/*     */ 
/* 224 */     this.m_scriptHelper.setFieldOptionListKey("dOutDate", "futureDates", "combo");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 229 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.EditExitConditionDlg
 * JD-Core Version:    0.5.4
 */