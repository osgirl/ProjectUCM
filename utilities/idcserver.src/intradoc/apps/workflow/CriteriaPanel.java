/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.workflow.WorkflowData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CriteriaPanel extends WorkflowPanel
/*     */ {
/*  41 */   public static String[][] OPERATORS = { { "matches", "apQueryFieldMatches" } };
/*     */ 
/*     */   public CriteriaPanel()
/*     */   {
/*  48 */     this.m_startBtnLabel = "apTitleEnable";
/*  49 */     this.m_cancelBtnLabel = "apTitleDisable";
/*     */ 
/*  51 */     String[][] CRITERIA_STATUS = { { "INIT", "apTitleDisabled" }, { "INPROCESS", "apTitleEnabled" } };
/*     */ 
/*  57 */     this.m_displayMap = CRITERIA_STATUS;
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys, WorkflowContext ctxt)
/*     */     throws ServiceException
/*     */   {
/*  64 */     super.init(sys, ctxt);
/*  65 */     LocaleResources.localizeDoubleArray(this.m_displayMap, this.m_cxt, 1);
/*  66 */     LocaleResources.localizeStaticDoubleArray(OPERATORS, this.m_cxt, 1);
/*     */ 
/*  69 */     refreshList(null);
/*     */   }
/*     */ 
/*     */   protected void createWorkflowList()
/*     */   {
/*  75 */     this.m_workflowList = new UdlPanel(LocaleResources.getString("apTitleCriteriaWorkflows", this.m_cxt), null, 300, 20, "CriteriaWorkflows", true);
/*     */ 
/*  77 */     String fieldList = "dWfName,dWfStatus,dSecurityGroup";
/*  78 */     this.m_workflowList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleName", this.m_cxt), "dWfName", 4.0D));
/*     */ 
/*  80 */     this.m_workflowList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelStatus", this.m_cxt), "dWfStatus", 2.0D));
/*     */ 
/*  82 */     this.m_workflowList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleSecurityGroup", this.m_cxt), "dSecurityGroup", 3.0D));
/*     */ 
/*  86 */     String[][] display = this.m_context.buildProjectMap();
/*  87 */     if (display != null)
/*     */     {
/*  89 */       fieldList = fieldList + ",dProjectID";
/*  90 */       this.m_workflowList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleProject", this.m_cxt), "dProjectID", 3.0D));
/*     */ 
/*  92 */       addDisplayMap("dProjectID", display);
/*     */     }
/*     */ 
/*  95 */     this.m_workflowList.setVisibleColumns(fieldList);
/*  96 */     this.m_workflowList.setIDColumn("dWfName");
/*     */   }
/*     */ 
/*     */   public void addDisplayMap(String key, String[][] display)
/*     */   {
/* 101 */     String[][] displayMap = display;
/* 102 */     DisplayStringCallback dsc = new DisplayStringCallbackAdaptor(displayMap)
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 108 */         String dValue = StringUtils.getPresentationString(this.val$displayMap, value);
/* 109 */         if (dValue != null)
/*     */         {
/* 111 */           return dValue;
/*     */         }
/* 113 */         return value;
/*     */       }
/*     */     };
/* 116 */     this.m_workflowList.setDisplayCallback(key, dsc);
/*     */   }
/*     */ 
/*     */   protected void addOrEditWorkflow(boolean isAdd)
/*     */   {
/* 122 */     Properties props = null;
/* 123 */     String title = "apTitleNewCriteriaWorkflow";
/* 124 */     String helpPage = "AddNewCriteriaWorkflow";
/* 125 */     if (!isAdd)
/*     */     {
/* 127 */       int index = this.m_workflowList.getSelectedIndex();
/* 128 */       if (index < 0)
/*     */       {
/* 130 */         return;
/*     */       }
/* 132 */       props = this.m_workflowList.getDataAt(index);
/* 133 */       String wfName = props.getProperty("dWfName");
/* 134 */       title = LocaleResources.getString("apTitleEditCriteriaWorkflow", this.m_cxt, wfName);
/*     */ 
/* 136 */       helpPage = "EditCriteriaWorkflow";
/*     */ 
/* 138 */       getCriteriaInfo(props);
/* 139 */       mergeWorkflowInfoProps(wfName, props);
/*     */     }
/*     */ 
/* 142 */     EditCriteriaDlg dlg = new EditCriteriaDlg(this.m_systemInterface, LocaleResources.getString(title, this.m_cxt), this.m_context, this.m_workflowList.getResultSet(), DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 145 */     if ((!dlg.init(props)) || 
/* 147 */       (dlg.prompt() != 1))
/*     */       return;
/*     */     try
/*     */     {
/* 151 */       refreshList(dlg.getWorkflowName());
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 156 */       reportError(exp);
/* 157 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void getCriteriaInfo(Properties props)
/*     */   {
/* 165 */     WorkflowData wfData = (WorkflowData)SharedObjects.getTable("App" + WorkflowData.m_tableName);
/* 166 */     wfData.getCriteriaInfo(props);
/*     */   }
/*     */ 
/*     */   protected void deleteWorkflow(int index)
/*     */   {
/* 172 */     Properties props = this.m_workflowList.getDataAt(index);
/* 173 */     String name = props.getProperty("dWfName");
/*     */ 
/* 175 */     IdcMessage msg = IdcMessageFactory.lc("apVerifyCriteriaWorkflowDelete", new Object[] { name });
/*     */ 
/* 177 */     if (MessageBox.doMessage(this.m_systemInterface, msg, 4) != 2) {
/*     */       return;
/*     */     }
/*     */     try
/*     */     {
/* 182 */       DataBinder binder = new DataBinder();
/* 183 */       binder.setLocalData(props);
/*     */ 
/* 185 */       SharedContext shContext = this.m_context.getSharedContext();
/* 186 */       AppContextUtils.executeService(shContext, "DELETE_WORKFLOWCRITERIA", binder, true);
/* 187 */       refreshList(null);
/* 188 */       this.m_workflowList.enableDisable(false);
/* 189 */       checkSelection();
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 193 */       reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void createFlipComponents()
/*     */   {
/* 201 */     addFlipComponent("Criteria", new WfCriteriaEditView());
/* 202 */     addFlipComponent("SubWorkflow", new WfCriteriaEditView());
/*     */   }
/*     */ 
/*     */   protected ActionListener createWfButtonListener()
/*     */   {
/* 208 */     ActionListener wfListener = new Object()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 212 */         int index = CriteriaPanel.this.m_workflowList.getSelectedIndex();
/* 213 */         if (index < 0)
/*     */         {
/* 215 */           return;
/*     */         }
/*     */ 
/* 218 */         String action = null;
/* 219 */         IdcMessage msg = null;
/*     */ 
/* 221 */         Properties props = CriteriaPanel.this.m_workflowList.getDataAt(index);
/* 222 */         Object src = e.getSource();
/* 223 */         String wfName = CriteriaPanel.this.m_workflowList.getSelectedObj();
/* 224 */         if (src == CriteriaPanel.this.m_cancelBtn)
/*     */         {
/* 226 */           action = "CRITERIAWORKFLOW_DISABLE";
/* 227 */           msg = IdcMessageFactory.lc("apVerifyCriteriaWorkflowDisable", new Object[] { wfName });
/*     */         }
/*     */         else
/*     */         {
/* 231 */           action = "CRITERIAWORKFLOW_ENABLE";
/* 232 */           msg = IdcMessageFactory.lc("apVerifyCriteriaWorkflowEnable", new Object[] { wfName });
/*     */         }
/* 234 */         if (MessageBox.doMessage(CriteriaPanel.this.m_systemInterface, msg, 4) == 3)
/*     */         {
/* 237 */           return;
/*     */         }
/*     */ 
/* 240 */         DataBinder binder = new DataBinder();
/* 241 */         binder.setLocalData(props);
/*     */         try
/*     */         {
/* 244 */           SharedContext shContext = CriteriaPanel.this.m_context.getSharedContext();
/* 245 */           shContext.executeService(action, binder, true);
/*     */ 
/* 247 */           AppLauncher.notifyInternalSubjectChange("documents");
/* 248 */           String selectedObj = CriteriaPanel.this.m_workflowList.getSelectedObj();
/* 249 */           CriteriaPanel.this.refreshList(selectedObj);
/* 250 */           CriteriaPanel.this.checkSelectionEx(true);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 254 */           CriteriaPanel.this.reportError(exp);
/*     */         }
/*     */       }
/*     */     };
/* 259 */     return wfListener;
/*     */   }
/*     */ 
/*     */   protected void setWorkflows()
/*     */   {
/* 265 */     WorkflowData workflows = (WorkflowData)SharedObjects.getTable("App" + WorkflowData.m_tableName);
/* 266 */     this.m_workflows = workflows.getCriteriaWorkflows();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 271 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78807 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.CriteriaPanel
 * JD-Core Version:    0.5.4
 */