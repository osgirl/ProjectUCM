/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.PromptHandler;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.gui.QueryBuilderHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public abstract class EditQueryBasePanel extends PanePanel
/*     */   implements ComponentBinder, PromptHandler
/*     */ {
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ContainerHelper m_helper;
/*     */   protected PageManagerContext m_pageServices;
/*     */   protected QueryBuilderHelper m_queryHelper;
/*     */   protected String m_queryDataStr;
/*     */   protected Vector m_linkInfo;
/*     */   protected PageData m_pageData;
/*     */   protected ExecutionContext m_ctx;
/*     */ 
/*     */   public EditQueryBasePanel()
/*     */   {
/*  34 */     this.m_systemInterface = null;
/*  35 */     this.m_helper = null;
/*  36 */     this.m_pageServices = null;
/*     */ 
/*  52 */     this.m_ctx = null;
/*     */   }
/*     */ 
/*     */   public void setQueryInfo(QueryBuilderHelper qHelper, String queryDataStr, PageData pageData) {
/*  56 */     this.m_queryHelper = qHelper;
/*  57 */     this.m_queryDataStr = queryDataStr;
/*  58 */     this.m_pageData = pageData;
/*     */   }
/*     */ 
/*     */   public void init(DialogHelper helper, PageManagerContext pageContext, DocumentLocalizedProfile docProfile, boolean isNew)
/*     */     throws ServiceException
/*     */   {
/*  64 */     this.m_systemInterface = helper.m_exchange.m_sysInterface;
/*  65 */     this.m_ctx = this.m_systemInterface.getExecutionContext();
/*  66 */     this.m_pageServices = pageContext;
/*     */ 
/*  68 */     this.m_helper = new ContainerHelper();
/*  69 */     this.m_helper.attachToContainer(this, this.m_systemInterface, helper.m_props);
/*  70 */     this.m_helper.m_componentBinder = this;
/*  71 */     this.m_helper.m_gridHelper.useGridBag(this);
/*     */ 
/*  73 */     initUI();
/*     */ 
/*  75 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected abstract void initUI()
/*     */     throws ServiceException;
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(Exception e, String msg)
/*     */   {
/*  84 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public void reportError(Exception e, IdcMessage msg)
/*     */   {
/*  89 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*  97 */     String name = exchange.m_compName;
/*  98 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 103 */     return true;
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 117 */     if (!this.m_helper.retrieveComponentValues())
/*     */     {
/* 119 */       return 0;
/*     */     }
/* 121 */     return 1;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 126 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditQueryBasePanel
 * JD-Core Version:    0.5.4
 */