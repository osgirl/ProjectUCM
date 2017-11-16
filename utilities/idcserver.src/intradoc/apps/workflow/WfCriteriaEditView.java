/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.workflow.WorkflowData;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class WfCriteriaEditView extends WfEditView
/*     */ {
/*  41 */   CustomLabel m_criteriaLbl = null;
/*     */ 
/*     */   public WfCriteriaEditView()
/*     */   {
/*  45 */     this.m_isCriteria = true;
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  51 */     DisplayStringCallback displayCallback = this.m_context.createStringCallback();
/*  52 */     JPanel critPanel = initCriteriaPanel(null);
/*     */ 
/*  54 */     ActionListener stepListener = createStepListener();
/*  55 */     JPanel stepPanel = initStepList(stepListener, displayCallback);
/*     */ 
/*  58 */     setInsets(5, 20, 10, 5);
/*  59 */     setLayout(new BorderLayout());
/*  60 */     add("North", critPanel);
/*  61 */     add("Center", stepPanel);
/*     */   }
/*     */ 
/*     */   protected JPanel initCriteriaPanel(ActionListener aListener)
/*     */   {
/*  66 */     JPanel pnl = new PanePanel();
/*     */ 
/*  68 */     pnl.setLayout(new BorderLayout());
/*     */ 
/*  70 */     CustomLabel lbl = new CustomLabel(LocaleResources.getString("apTitleCriteria", this.m_cxt), 1);
/*     */ 
/*  72 */     pnl.add("North", lbl);
/*     */ 
/*  74 */     this.m_criteriaLbl = new CustomLabel();
/*  75 */     this.m_criteriaLbl.setAlignment(0);
/*  76 */     this.m_criteriaLbl.setMinWidth(200);
/*  77 */     pnl.add("Center", this.m_criteriaLbl);
/*     */ 
/*  79 */     return pnl;
/*     */   }
/*     */ 
/*     */   public void createWorkflowInfo(Properties props)
/*     */   {
/*  85 */     super.createWorkflowInfo(props);
/*  86 */     WorkflowData wfData = (WorkflowData)SharedObjects.getTable("App" + WorkflowData.m_tableName);
/*  87 */     if (wfData == null)
/*     */     {
/*  89 */       return;
/*     */     }
/*     */ 
/*  92 */     wfData.getCriteriaInfo(props);
/*     */ 
/*  94 */     updateCriteriaDisplay();
/*     */   }
/*     */ 
/*     */   public void updateCriteriaDisplay()
/*     */   {
/*  99 */     String criteriaStr = "";
/* 100 */     if (this.m_workflowInfo != null)
/*     */     {
/* 102 */       String type = this.m_workflowInfo.getWfType();
/* 103 */       if (type.equalsIgnoreCase("criteria"))
/*     */       {
/* 105 */         String name = this.m_workflowInfo.get("dWfCriteriaName");
/* 106 */         if (name != null)
/*     */         {
/* 108 */           String op = this.m_workflowInfo.get("dWfCriteriaOperator");
/* 109 */           op = StringUtils.getPresentationString(CriteriaPanel.OPERATORS, op);
/* 110 */           criteriaStr = name + " " + op + " " + this.m_workflowInfo.get("dWfCriteriaValue");
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 115 */     this.m_criteriaLbl.setText(criteriaStr);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 120 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.WfCriteriaEditView
 * JD-Core Version:    0.5.4
 */