/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SelectStepDlg
/*     */ {
/*  56 */   protected SystemInterface m_systemInterface = null;
/*  57 */   protected ExecutionContext m_cxt = null;
/*  58 */   protected DialogHelper m_helper = null;
/*  59 */   protected String m_helpPage = null;
/*     */ 
/*  61 */   protected WorkflowContext m_context = null;
/*  62 */   protected WorkflowStateInfo m_workflowInfo = null;
/*  63 */   protected DataResultSet m_workflowSet = null;
/*     */ 
/*  66 */   protected DataResultSet m_stepSet = null;
/*  67 */   protected Hashtable m_workflowStepMap = null;
/*     */ 
/*  70 */   protected UdlPanel m_workflowList = null;
/*  71 */   protected UdlPanel m_stepList = null;
/*     */ 
/*     */   public SelectStepDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  75 */     this.m_systemInterface = sys;
/*  76 */     this.m_cxt = sys.getExecutionContext();
/*  77 */     this.m_helper = new DialogHelper(sys, title, true);
/*  78 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(WorkflowStateInfo wfInfo, WorkflowContext context)
/*     */   {
/*  83 */     this.m_workflowInfo = wfInfo;
/*  84 */     this.m_context = context;
/*     */ 
/*  86 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  91 */         IdcMessage errMsg = null;
/*  92 */         String wfName = null;
/*  93 */         int wfIndex = SelectStepDlg.this.m_workflowList.getSelectedIndex();
/*  94 */         int stepIndex = -1;
/*  95 */         if (wfIndex < 0)
/*     */         {
/*  97 */           errMsg = IdcMessageFactory.lc("apSelectWorkflow", new Object[0]);
/*     */         }
/*     */ 
/* 100 */         if (errMsg == null)
/*     */         {
/* 102 */           Properties wfProps = SelectStepDlg.this.m_workflowList.getDataAt(wfIndex);
/* 103 */           wfName = wfProps.getProperty("dWfName");
/*     */ 
/* 105 */           stepIndex = SelectStepDlg.this.m_stepList.getSelectedIndex();
/* 106 */           if (stepIndex < 0)
/*     */           {
/* 108 */             errMsg = IdcMessageFactory.lc("apSelectWorkflowStep", new Object[0]);
/*     */           }
/*     */         }
/*     */ 
/* 112 */         if (errMsg != null)
/*     */         {
/* 114 */           SelectStepDlg.this.m_context.reportError(null, errMsg);
/* 115 */           return false;
/*     */         }
/*     */ 
/* 118 */         Properties stepProps = SelectStepDlg.this.m_stepList.getDataAt(stepIndex);
/* 119 */         SelectStepDlg.this.m_helper.m_props.put("wfJumpTargetStep", stepProps.getProperty("dWfStepName") + "@" + wfName);
/*     */ 
/* 121 */         return true;
/*     */       }
/*     */     };
/* 125 */     boolean reslt = createResultSets();
/* 126 */     if (!reslt)
/*     */     {
/* 129 */       this.m_context.reportError(null, IdcMessageFactory.lc("apNoWorkflowsForSecurityGroup", new Object[0]));
/* 130 */       return 0;
/*     */     }
/*     */ 
/* 133 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, DialogHelpTable.getHelpPage(this.m_helpPage));
/*     */ 
/* 136 */     initUI(mainPanel);
/*     */ 
/* 138 */     this.m_workflowList.refreshList(this.m_workflowSet, null);
/* 139 */     checkSelection();
/*     */ 
/* 141 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel mainPanel)
/*     */   {
/* 146 */     this.m_workflowList = new UdlPanel(LocaleResources.getString("apTitleWorkflows", this.m_cxt), null, 200, 10, "Workflows", false);
/*     */ 
/* 148 */     this.m_workflowList.setVisibleColumns("dWfName");
/* 149 */     this.m_workflowList.init();
/*     */ 
/* 151 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 155 */         SelectStepDlg.this.checkSelection();
/*     */       }
/*     */     };
/* 158 */     this.m_workflowList.addItemListener(iListener);
/*     */ 
/* 160 */     this.m_stepList = new UdlPanel(LocaleResources.getString("apTitleSteps", this.m_cxt), null, 200, 10, "WorkflowSteps", false);
/*     */ 
/* 162 */     this.m_stepList.setVisibleColumns("dWfStepName");
/* 163 */     this.m_stepList.init();
/* 164 */     this.m_stepList.setDisplayCallback("dWfStepName", this.m_context.createStringCallback());
/*     */ 
/* 166 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 167 */     gh.m_gc.weighty = 1.0D;
/*     */ 
/* 169 */     gh.prepareAddRowElement(13);
/* 170 */     this.m_helper.addComponent(mainPanel, this.m_workflowList);
/*     */ 
/* 172 */     gh.prepareAddLastRowElement();
/* 173 */     this.m_helper.addComponent(mainPanel, this.m_stepList);
/*     */   }
/*     */ 
/*     */   protected boolean createResultSets()
/*     */   {
/* 178 */     boolean isAll = this.m_workflowInfo == null;
/*     */ 
/* 180 */     boolean result = false;
/*     */     try
/*     */     {
/* 183 */       DataBinder binder = new DataBinder();
/* 184 */       String action = null;
/* 185 */       if (isAll)
/*     */       {
/* 187 */         action = "GET_WORKFLOWS_FOR_ALL";
/*     */       }
/*     */       else
/*     */       {
/* 191 */         action = "GET_CRITERIA_WORKFLOWS_FOR_GROUP";
/* 192 */         binder.putLocal("dSecurityGroup", this.m_workflowInfo.get("dSecurityGroup"));
/*     */       }
/*     */ 
/* 195 */       SharedContext shContext = this.m_context.getSharedContext();
/* 196 */       shContext.executeService(action, binder, false);
/*     */ 
/* 198 */       DataResultSet stepSet = (DataResultSet)binder.getResultSet("WorkflowStepsForGroup");
/* 199 */       if ((stepSet != null) && (!stepSet.isEmpty()))
/*     */       {
/* 201 */         result = true;
/* 202 */         this.m_stepSet = stepSet;
/* 203 */         this.m_workflowSet = ((DataResultSet)binder.getResultSet("WorkflowsForGroup"));
/* 204 */         sortWorkflowStepMap(stepSet);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 209 */       this.m_context.reportError(e);
/*     */     }
/* 211 */     return result;
/*     */   }
/*     */ 
/*     */   protected void sortWorkflowStepMap(DataResultSet stepSet)
/*     */   {
/*     */     try
/*     */     {
/* 219 */       FieldInfo[] infos = ResultSetUtils.createInfoList(this.m_workflowSet, new String[] { "dWfID", "dWfType" }, true);
/*     */ 
/* 223 */       FieldInfo[] stepInfos = ResultSetUtils.createInfoList(stepSet, new String[] { "dWfID", "dWfStepType" }, true);
/*     */ 
/* 225 */       int wfIDIndex = stepInfos[0].m_index;
/* 226 */       int wfStepTypeIndex = stepInfos[1].m_index;
/*     */ 
/* 228 */       DataBinder binder = new DataBinder();
/* 229 */       binder.addResultSet("StepSet", stepSet);
/* 230 */       this.m_workflowStepMap = new Hashtable();
/*     */ 
/* 232 */       Hashtable wfTypeMap = new Hashtable();
/* 233 */       for (; stepSet.isRowPresent(); stepSet.next())
/*     */       {
/* 235 */         String wfID = stepSet.getStringValue(wfIDIndex);
/*     */ 
/* 240 */         String wfType = (String)wfTypeMap.get(wfID);
/* 241 */         if (wfType == null)
/*     */         {
/* 243 */           Vector row = this.m_workflowSet.findRow(infos[0].m_index, wfID);
/* 244 */           if (row == null)
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 249 */           wfType = this.m_workflowSet.getStringValue(infos[1].m_index);
/* 250 */           wfTypeMap.put(wfID, wfType);
/*     */         }
/* 252 */         boolean isSubWorkflow = wfType.equals("SubWorkflow");
/*     */ 
/* 254 */         DataResultSet drset = (DataResultSet)this.m_workflowStepMap.get(wfID);
/* 255 */         if (drset == null)
/*     */         {
/* 257 */           drset = new DataResultSet();
/* 258 */           drset.copyFieldInfo(stepSet);
/* 259 */           this.m_workflowStepMap.put(wfID, drset);
/*     */         }
/*     */ 
/* 262 */         boolean isSkip = false;
/* 263 */         if (isSubWorkflow)
/*     */         {
/* 266 */           String stepType = stepSet.getStringValue(wfStepTypeIndex);
/* 267 */           isSkip = WorkflowScriptUtils.isAutoContributorStep(stepType);
/*     */         }
/* 269 */         if (isSkip)
/*     */           continue;
/* 271 */         Vector row = drset.createRow(binder);
/* 272 */         drset.addRow(row);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 278 */       this.m_context.reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 284 */     int index = this.m_workflowList.getSelectedIndex();
/* 285 */     if (index < 0)
/*     */     {
/* 287 */       return;
/*     */     }
/*     */ 
/* 290 */     Properties props = this.m_workflowList.getDataAt(index);
/* 291 */     String id = props.getProperty("dWfID");
/* 292 */     DataResultSet drset = (DataResultSet)this.m_workflowStepMap.get(id);
/* 293 */     if (id == null)
/*     */     {
/* 295 */       drset = new DataResultSet();
/* 296 */       drset.copyFieldInfo(this.m_stepSet);
/* 297 */       this.m_workflowStepMap.put(id, drset);
/*     */     }
/*     */ 
/* 301 */     if ((this.m_workflowInfo == null) && (drset.isRowPresent()))
/*     */     {
/* 303 */       String stepType = ResultSetUtils.getValue(drset, "dWfStepType");
/* 304 */       if (WorkflowScriptUtils.isAutoContributorStep(stepType))
/*     */       {
/* 306 */         drset.deleteCurrentRow();
/*     */       }
/*     */     }
/* 309 */     this.m_stepList.refreshList(drset, null);
/*     */   }
/*     */ 
/*     */   public Properties getProperties()
/*     */   {
/* 314 */     return this.m_helper.m_props;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 319 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.SelectStepDlg
 * JD-Core Version:    0.5.4
 */