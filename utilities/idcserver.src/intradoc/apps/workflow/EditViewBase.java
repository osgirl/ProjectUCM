/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditViewBase extends CustomPanel
/*     */   implements SharedContext
/*     */ {
/*  50 */   protected SystemInterface m_systemInterface = null;
/*  51 */   protected ExecutionContext m_cxt = null;
/*  52 */   protected ContainerHelper m_helper = null;
/*  53 */   protected WorkflowStateInfo m_workflowInfo = null;
/*     */   protected WorkflowContext m_context;
/*  55 */   protected boolean m_isCriteria = false;
/*  56 */   protected boolean m_isNew = false;
/*     */ 
/*     */   public void init(ContainerHelper helper, WorkflowContext cxt, boolean isCriteria, boolean isNew)
/*     */   {
/*  65 */     this.m_systemInterface = helper.m_exchange.m_sysInterface;
/*  66 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*  67 */     this.m_context = cxt;
/*  68 */     this.m_isCriteria = isCriteria;
/*  69 */     this.m_isNew = isNew;
/*     */ 
/*  71 */     this.m_helper = new ContainerHelper();
/*  72 */     this.m_helper.attachToContainer(this, this.m_systemInterface, null);
/*  73 */     this.m_helper.m_exchange = helper.m_exchange;
/*     */ 
/*  75 */     initUI();
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys, WorkflowContext cxt)
/*     */   {
/*  80 */     this.m_systemInterface = sys;
/*  81 */     this.m_cxt = sys.getExecutionContext();
/*  82 */     this.m_helper = new ContainerHelper();
/*  83 */     this.m_helper.attachToContainer(this, sys, null);
/*  84 */     this.m_context = cxt;
/*     */ 
/*  87 */     initUI();
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  93 */     CustomLabel lbl = new CustomLabel();
/*  94 */     lbl.setMinWidth(200);
/*  95 */     this.m_helper.addExchangeComponent(this, lbl, "HelpMessage");
/*     */   }
/*     */ 
/*     */   public void setIsCriteria(boolean isCriteria)
/*     */   {
/* 100 */     this.m_isCriteria = isCriteria;
/*     */   }
/*     */ 
/*     */   public void load()
/*     */   {
/*     */   }
/*     */ 
/*     */   public boolean updateInfo()
/*     */   {
/* 110 */     return true;
/*     */   }
/*     */ 
/*     */   public void createWorkflowInfo(Properties props)
/*     */   {
/* 115 */     this.m_helper.m_props = props;
/* 116 */     this.m_helper.loadComponentValues();
/* 117 */     if (this.m_workflowInfo == null)
/*     */     {
/* 119 */       this.m_workflowInfo = new WorkflowStateInfo(props);
/*     */     }
/*     */     else
/*     */     {
/* 123 */       this.m_workflowInfo.updateData(props);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateProps(Properties props)
/*     */   {
/* 129 */     this.m_workflowInfo.mergeData(props);
/*     */   }
/*     */ 
/*     */   public void setWorkflowInfo(WorkflowStateInfo wfInfo)
/*     */   {
/* 134 */     this.m_workflowInfo = wfInfo;
/* 135 */     this.m_helper.m_props = wfInfo.getWorkflowData().getLocalData();
/* 136 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public WorkflowStateInfo getWorkflowInfo()
/*     */   {
/* 141 */     return this.m_workflowInfo;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(Exception e, String msg)
/*     */   {
/* 149 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public void reportError(Exception e, IdcMessage msg)
/*     */   {
/* 154 */     if (msg != null)
/*     */     {
/* 156 */       MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */     }
/*     */     else
/*     */     {
/* 160 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected UdlPanel createList(String[] listInfo, String[][] buttonInfo, ActionListener aListener, int width, int height)
/*     */   {
/* 167 */     UdlPanel list = new UdlPanel(LocaleResources.getString(listInfo[0], this.m_cxt), null, width, height, listInfo[1], false);
/*     */ 
/* 169 */     list.setVisibleColumns(listInfo[2]);
/* 170 */     list.init();
/* 171 */     list.useDefaultListener();
/*     */ 
/* 173 */     JPanel btnPanel = new PanePanel();
/* 174 */     btnPanel.setLayout(new GridLayout(0, 1));
/*     */ 
/* 176 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/* 178 */       boolean isListControlled = StringUtils.convertToBool(buttonInfo[i][2], false);
/* 179 */       JButton btn = list.addButton(LocaleResources.getString(buttonInfo[i][0], this.m_cxt), isListControlled);
/* 180 */       btn.getAccessibleContext().setAccessibleName(buttonInfo[i][3]);
/* 181 */       btn.setActionCommand(buttonInfo[i][1]);
/* 182 */       if (aListener != null)
/*     */       {
/* 184 */         btn.addActionListener(aListener);
/*     */       }
/*     */ 
/* 187 */       btnPanel.add(btn);
/*     */     }
/*     */ 
/* 190 */     JPanel cbWrapper = new PanePanel();
/* 191 */     cbWrapper.add(btnPanel);
/* 192 */     list.add("East", cbWrapper);
/* 193 */     list.enableDisable(false);
/*     */ 
/* 195 */     return list;
/*     */   }
/*     */ 
/*     */   public void updateEdit(WorkflowStateInfo curStepData)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void executeService(String action, DataBinder data, boolean isRefreshList)
/*     */     throws ServiceException
/*     */   {
/* 209 */     SharedContext shContext = this.m_context.getSharedContext();
/* 210 */     shContext.executeService(action, data, isRefreshList);
/*     */   }
/*     */ 
/*     */   public UserData getUserData()
/*     */   {
/* 215 */     return AppLauncher.getUserData();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 220 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.EditViewBase
 * JD-Core Version:    0.5.4
 */