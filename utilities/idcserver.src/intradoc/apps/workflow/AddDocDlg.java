/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Insets;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddDocDlg
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*  51 */   protected ExecutionContext m_cxt = null;
/*     */   protected ComponentValidator m_cmpValidator;
/*     */   protected WorkflowContext m_context;
/*     */   protected WorkflowStateInfo m_wfInfo;
/*     */   protected String m_helpPage;
/*     */ 
/*     */   public AddDocDlg(SystemInterface sys, String title, ResultSet rset, String helpPage)
/*     */   {
/*  60 */     this.m_helper = new DialogHelper(sys, title, true);
/*  61 */     this.m_systemInterface = sys;
/*  62 */     this.m_cxt = null;
/*  63 */     this.m_helpPage = helpPage;
/*     */ 
/*  65 */     this.m_cmpValidator = new ComponentValidator(rset);
/*     */   }
/*     */ 
/*     */   public void init(WorkflowStateInfo wfInfo, WorkflowContext cxt, DialogCallback okCallback)
/*     */   {
/*  70 */     this.m_wfInfo = wfInfo;
/*  71 */     this.m_context = cxt;
/*     */ 
/*  73 */     JPanel wrapper = this.m_helper.initStandard(this, okCallback, 2, true, this.m_helpPage);
/*     */ 
/*  75 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 20, 10, 20);
/*  76 */     JPanel mainPanel = new PanePanel();
/*  77 */     this.m_helper.addComponent(wrapper, mainPanel);
/*     */ 
/*  79 */     this.m_helper.makePanelGridBag(mainPanel, 2);
/*     */ 
/*  81 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 0, 0, 0);
/*  82 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelContentId", this.m_cxt), new CustomTextField(20), "dDocName");
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/*  88 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public Properties getProperties()
/*     */   {
/*  93 */     return this.m_helper.m_props;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 102 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 103 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 108 */     String name = exchange.m_compName;
/* 109 */     String val = exchange.m_compValue;
/*     */ 
/* 111 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/*     */ 
/* 113 */     IdcMessage errMsg = null;
/* 114 */     if (name.equals("dDocName"))
/*     */     {
/* 116 */       int resultVal = Validation.checkUrlFileSegment(val);
/* 117 */       if (resultVal != 0)
/*     */       {
/* 119 */         switch (resultVal)
/*     */         {
/*     */         case -1:
/* 122 */           errMsg = IdcMessageFactory.lc("apSpecifyContentId", new Object[0]);
/* 123 */           break;
/*     */         case -2:
/* 125 */           errMsg = IdcMessageFactory.lc("apSpacesInContentId", new Object[] { val });
/* 126 */           break;
/*     */         case -3:
/* 128 */           errMsg = IdcMessageFactory.lc("apInvalidCharsInContentId", new Object[] { val });
/*     */         }
/*     */       }
/*     */ 
/* 132 */       if (val.length() > maxLength)
/*     */       {
/* 134 */         errMsg = IdcMessageFactory.lc("apContentIdExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */       }
/* 136 */       if (errMsg != null)
/*     */       {
/* 138 */         exchange.m_errorMessage = errMsg;
/* 139 */         return false;
/*     */       }
/*     */     }
/* 142 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 147 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.AddDocDlg
 * JD-Core Version:    0.5.4
 */