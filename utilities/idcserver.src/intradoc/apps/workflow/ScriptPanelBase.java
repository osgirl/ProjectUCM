/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.PromptHandler;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.FocusListener;
/*     */ 
/*     */ public abstract class ScriptPanelBase extends PanePanel
/*     */   implements ActionListener, ComponentBinder, FocusListener, PromptHandler
/*     */ {
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected WorkflowContext m_context;
/*     */   protected WorkflowStateInfo m_workflowInfo;
/*     */   protected boolean m_isTemplate;
/*     */   protected boolean m_isNewStep;
/*     */   protected ContainerHelper m_helper;
/*     */   public ScriptBuilderHelper m_scriptHelper;
/*     */   protected DataBinder m_scriptData;
/*     */   public DataResultSet m_jumpSet;
/*     */   protected UdlPanel m_jumpList;
/*     */ 
/*     */   public ScriptPanelBase()
/*     */   {
/*  37 */     this.m_systemInterface = null;
/*  38 */     this.m_cxt = null;
/*  39 */     this.m_context = null;
/*  40 */     this.m_workflowInfo = null;
/*  41 */     this.m_isTemplate = false;
/*  42 */     this.m_isNewStep = false;
/*     */ 
/*  44 */     this.m_helper = null;
/*  45 */     this.m_scriptHelper = null;
/*  46 */     this.m_scriptData = null;
/*  47 */     this.m_jumpSet = null;
/*     */ 
/*  49 */     this.m_jumpList = null;
/*     */   }
/*     */ 
/*     */   public void init(DialogHelper helper, WorkflowStateInfo wfInfo, boolean isTemplate, boolean isNew, WorkflowContext ctxt) throws ServiceException
/*     */   {
/*  54 */     this.m_systemInterface = helper.m_exchange.m_sysInterface;
/*  55 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*  56 */     this.m_workflowInfo = wfInfo;
/*  57 */     this.m_isTemplate = isTemplate;
/*  58 */     this.m_isNewStep = isNew;
/*  59 */     this.m_context = ctxt;
/*     */ 
/*  61 */     this.m_helper = new ContainerHelper();
/*  62 */     this.m_helper.attachToContainer(this, this.m_systemInterface, helper.m_props);
/*  63 */     this.m_helper.m_componentBinder = this;
/*  64 */     this.m_helper.makePanelGridBag(this, 1);
/*     */ 
/*  66 */     initUI();
/*     */ 
/*  68 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected abstract void initUI() throws ServiceException;
/*     */ 
/*     */   public void setScriptInfo(ScriptBuilderHelper scriptHelper, DataBinder sData)
/*     */   {
/*  75 */     this.m_scriptHelper = scriptHelper;
/*  76 */     this.m_scriptData = sData;
/*  77 */     this.m_jumpSet = ((DataResultSet)this.m_scriptData.getResultSet("WorkflowScriptJumps"));
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(Exception e, String msg)
/*     */   {
/*  87 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public void reportError(Exception e, IdcMessage msg)
/*     */   {
/*  92 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 100 */     if (!this.m_helper.retrieveComponentValues())
/*     */     {
/* 102 */       return 0;
/*     */     }
/* 104 */     return 1;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 112 */     String name = exchange.m_compName;
/* 113 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 118 */     String name = exchange.m_compName;
/* 119 */     String val = exchange.m_compValue;
/*     */ 
/* 121 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 127 */     if (updateComponent)
/*     */     {
/* 129 */       exchange.m_compValue = this.m_scriptHelper.getQueryProp(name);
/*     */     }
/*     */     else
/*     */     {
/* 133 */       this.m_scriptHelper.setQueryProp(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 139 */     return true;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void focusGained(FocusEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void focusLost(FocusEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 166 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.ScriptPanelBase
 * JD-Core Version:    0.5.4
 */