/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.awt.event.ActionEvent;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SelectTokenDlg
/*     */ {
/*  45 */   protected SystemInterface m_systemInterface = null;
/*  46 */   protected ExecutionContext m_cxt = null;
/*  47 */   protected DialogHelper m_helper = null;
/*  48 */   protected String m_helpPage = null;
/*     */ 
/*  50 */   protected WorkflowContext m_context = null;
/*     */ 
/*  53 */   protected UdlPanel m_tokenList = null;
/*     */ 
/*     */   public SelectTokenDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  57 */     this.m_systemInterface = sys;
/*  58 */     this.m_cxt = sys.getExecutionContext();
/*  59 */     this.m_helper = new DialogHelper(sys, title, true);
/*  60 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(WorkflowContext context)
/*     */   {
/*  65 */     this.m_context = context;
/*  66 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  71 */         int index = SelectTokenDlg.this.m_tokenList.getSelectedIndex();
/*  72 */         if (index < 0)
/*     */         {
/*  74 */           SelectTokenDlg.this.m_context.reportError(null, IdcMessageFactory.lc("apSelectToken", new Object[0]));
/*  75 */           return false;
/*     */         }
/*  77 */         return true;
/*     */       }
/*     */     };
/*  80 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, DialogHelpTable.getHelpPage(this.m_helpPage));
/*     */ 
/*  82 */     DataResultSet drset = SharedObjects.getTable("WorkflowTokens");
/*     */ 
/*  84 */     if (!drset.isRowPresent())
/*     */     {
/*  86 */       this.m_context.reportError(null, IdcMessageFactory.lc("apNoTokensInSystem", new Object[0]));
/*  87 */       return 0;
/*     */     }
/*  89 */     initUI(mainPanel, drset);
/*     */ 
/*  91 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI(JPanel pnl, DataResultSet drset)
/*     */   {
/*  96 */     this.m_tokenList = new UdlPanel(LocaleResources.getString("apTitleTokens", this.m_cxt), null, 200, 10, "WorkflowTokens", true);
/*     */ 
/*  98 */     this.m_tokenList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleName", this.m_cxt), "wfTokenName", 10.0D));
/*     */ 
/* 100 */     this.m_tokenList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleDescription", this.m_cxt), "wfTokenDescription", 20.0D));
/*     */ 
/* 102 */     this.m_tokenList.setVisibleColumns("wfTokenName,wfTokenDescription");
/* 103 */     this.m_tokenList.setIDColumn("wfTokenName");
/* 104 */     this.m_tokenList.setMultipleMode(true);
/* 105 */     this.m_tokenList.init();
/*     */ 
/* 107 */     this.m_tokenList.refreshList(drset, null);
/*     */ 
/* 109 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 110 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 111 */     this.m_helper.addComponent(pnl, this.m_tokenList);
/*     */   }
/*     */ 
/*     */   public String[] getSelectedObjs()
/*     */   {
/* 116 */     return this.m_tokenList.getSelectedObjs();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 121 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.SelectTokenDlg
 * JD-Core Version:    0.5.4
 */