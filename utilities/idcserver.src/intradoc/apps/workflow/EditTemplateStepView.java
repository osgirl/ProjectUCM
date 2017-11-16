/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditTemplateStepView extends EditViewBase
/*     */ {
/*     */   protected WorkflowStateInfo m_wfStepInfo;
/*     */   protected WfStepEditView m_stepView;
/*     */   protected EditViewBase[] m_panels;
/*     */   protected final String[][] PANEL_INFOS;
/*     */ 
/*     */   public EditTemplateStepView()
/*     */   {
/*  43 */     this.m_stepView = null;
/*  44 */     this.m_panels = null;
/*     */ 
/*  47 */     this.PANEL_INFOS = new String[][] { { "WfStepEditView", "intradoc.apps.workflow.WfStepEditView", "apTitleUsers" }, { "WfStepOptionsView", "intradoc.apps.workflow.WfStepConditionsPanel", "apTitleExitConditions" }, { "WfStepScriptPanel", "intradoc.apps.workflow.WfStepScriptPanel", "apTitleEvents" } };
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  59 */     TabPanel tabPanel = new TabPanel();
/*  60 */     JPanel panel = tabPanel;
/*     */ 
/*  62 */     int numPanels = this.PANEL_INFOS.length;
/*  63 */     this.m_panels = new EditViewBase[numPanels];
/*  64 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/*     */       try
/*     */       {
/*  68 */         this.m_panels[i] = ((EditViewBase)ComponentClassFactory.createClassInstance(this.PANEL_INFOS[i][0], this.PANEL_INFOS[i][1], LocaleResources.getString("apUnableToLoadTabPanel", this.m_cxt, this.PANEL_INFOS[i][0])));
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/*  74 */         this.m_context.reportError(e);
/*     */       }
/*     */ 
/*  77 */       this.m_panels[i].init(this.m_helper, this.m_context, true, false);
/*     */ 
/*  79 */       if (i == 0)
/*     */       {
/*  81 */         this.m_stepView = ((WfStepEditView)this.m_panels[i]);
/*     */       }
/*     */ 
/*  84 */       tabPanel.addPane(LocaleResources.getString(this.PANEL_INFOS[i][2], this.m_cxt), this.m_panels[i]);
/*     */     }
/*     */ 
/*  87 */     this.m_helper.makePanelGridBag(this, 1);
/*  88 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  89 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  90 */     this.m_helper.addComponent(this, panel);
/*     */   }
/*     */ 
/*     */   public WorkflowStateInfo getWorkflowInfo()
/*     */   {
/*  96 */     int numPanels = this.m_panels.length;
/*  97 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/*  99 */       this.m_panels[i].getWorkflowInfo();
/*     */     }
/* 101 */     return this.m_workflowInfo;
/*     */   }
/*     */ 
/*     */   public void load()
/*     */   {
/*     */     try
/*     */     {
/* 109 */       this.m_workflowInfo.createStepData(this.m_workflowInfo.m_wfData);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 114 */       IdcMessage error = IdcMessageFactory.lc("apUnableToLoadStepInfo", new Object[0]);
/* 115 */       MessageBox.reportError(this.m_systemInterface, e, error);
/*     */     }
/*     */ 
/* 118 */     int numPanels = this.m_panels.length;
/* 119 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 121 */       this.m_panels[i].setWorkflowInfo(this.m_workflowInfo);
/* 122 */       this.m_panels[i].load();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateEdit(WorkflowStateInfo curStepData)
/*     */   {
/* 130 */     DataBinder binder = this.m_workflowInfo.getWorkflowData();
/* 131 */     String stepName = this.m_workflowInfo.get("dWfStepName");
/*     */ 
/* 133 */     int numPanels = this.m_panels.length;
/* 134 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 136 */       if (this.m_panels[i] instanceof WfStepEditView)
/*     */       {
/* 138 */         WfStepEditView stepView = (WfStepEditView)this.m_panels[i];
/* 139 */         curStepData.setValue("dAliases", stepView.getAliases());
/*     */       }
/* 141 */       else if (this.m_panels[i] instanceof WfStepConditionsPanel)
/*     */       {
/* 143 */         WorkflowScriptUtils.updateWorkflowStepCondition(stepName, binder, binder);
/*     */       } else {
/* 145 */         if (!this.m_panels[i] instanceof WfStepScriptPanel) {
/*     */           continue;
/*     */         }
/* 148 */         DataResultSet drset = (DataResultSet)binder.getResultSet("WorkflowStepEvents");
/* 149 */         if (drset == null)
/*     */         {
/* 151 */           drset = new DataResultSet(WorkflowScriptUtils.WF_EVENT_COLUMNS);
/* 152 */           binder.addResultSet("WorkflowStepEvents", drset);
/*     */         }
/* 154 */         Vector row = drset.findRow(0, stepName);
/* 155 */         boolean isAppend = row == null;
/*     */         try
/*     */         {
/* 160 */           row = drset.createRow(binder);
/*     */ 
/* 162 */           if (isAppend)
/*     */           {
/* 164 */             drset.addRow(row);
/*     */           }
/*     */           else
/*     */           {
/* 168 */             int index = drset.getCurrentRow();
/* 169 */             drset.setRowValues(row, index);
/*     */           }
/*     */ 
/* 172 */           WorkflowScriptUtils.exchangeScriptStepInfo(stepName, binder, binder, true, true);
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 177 */           IdcMessage error = IdcMessageFactory.lc("apUnableToUpdateStepInfo", new Object[0]);
/* 178 */           MessageBox.reportError(this.m_systemInterface, null, error);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 186 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81274 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.EditTemplateStepView
 * JD-Core Version:    0.5.4
 */