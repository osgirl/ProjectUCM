/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public abstract class EditDlg
/*     */ {
/*  44 */   protected String m_editItems = "";
/*  45 */   protected String m_errMsg = "apErrorEditingArchive";
/*  46 */   protected String m_action = "EDIT_ARCHIVEDATA";
/*     */ 
/*  48 */   protected SystemInterface m_systemInterface = null;
/*  49 */   protected ExecutionContext m_cxt = null;
/*  50 */   protected DialogHelper m_helper = null;
/*  51 */   protected CollectionContext m_collectionContext = null;
/*  52 */   protected String m_helpPage = null;
/*     */ 
/*     */   public EditDlg(SystemInterface sys, String title, CollectionContext context, String helpPage)
/*     */   {
/*  57 */     this.m_systemInterface = sys;
/*  58 */     this.m_cxt = sys.getExecutionContext();
/*  59 */     this.m_helper = new DialogHelper(sys, title, true);
/*  60 */     this.m_collectionContext = context;
/*  61 */     if (helpPage == null)
/*     */       return;
/*  63 */     this.m_helpPage = DialogHelpTable.getHelpPage(helpPage);
/*     */   }
/*     */ 
/*     */   public int init(Properties props)
/*     */   {
/*  69 */     this.m_helper.m_props = ((Properties)props.clone());
/*     */ 
/*  71 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  77 */         if (!EditDlg.this.prepareOkEvent())
/*     */         {
/*  79 */           return false;
/*     */         }
/*     */ 
/*     */         try
/*     */         {
/*  84 */           Properties editData = EditDlg.this.m_helper.m_props;
/*  85 */           editData.put("EditItems", EditDlg.this.m_editItems);
/*     */ 
/*  87 */           SharedContext shContext = EditDlg.this.m_collectionContext.getSharedContext();
/*  88 */           AppContextUtils.executeService(shContext, EditDlg.this.m_action, editData, true);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/*  92 */           MessageBox.reportError(EditDlg.this.m_systemInterface, exp, IdcMessageFactory.lc("apErrorEditingArchive", new Object[0]));
/*  93 */           return false;
/*     */         }
/*  95 */         return true;
/*     */       }
/*     */     };
/*  99 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 102 */     initUI(mainPanel);
/* 103 */     return prompt();
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 108 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public boolean prepareOkEvent()
/*     */   {
/* 113 */     return true;
/*     */   }
/*     */ 
/*     */   public abstract void initUI(JPanel paramJPanel);
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 120 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditDlg
 * JD-Core Version:    0.5.4
 */