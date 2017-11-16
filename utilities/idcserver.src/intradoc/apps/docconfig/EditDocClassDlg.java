/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditDocClassDlg extends DialogCallback
/*     */ {
/*  42 */   protected SystemInterface m_systemInterface = null;
/*  43 */   protected ExecutionContext m_context = null;
/*  44 */   protected DialogHelper m_helper = null;
/*  45 */   protected SharedContext m_shContext = null;
/*  46 */   protected String m_helpPage = null;
/*     */   protected CustomTextField m_docClassField;
/*     */   protected CustomTextField m_defProfField;
/*     */   protected CustomTextField m_descrField;
/*  52 */   protected DataBinder m_binder = null;
/*     */   protected boolean m_isEdit;
/*     */ 
/*     */   public EditDocClassDlg(SystemInterface sys, SharedContext shContext, String title, String helpPage, boolean isEdit)
/*     */   {
/*  57 */     this.m_systemInterface = sys;
/*  58 */     this.m_context = sys.getExecutionContext();
/*  59 */     this.m_shContext = shContext;
/*  60 */     this.m_helper = new DialogHelper(sys, title, true);
/*  61 */     this.m_helpPage = helpPage;
/*  62 */     this.m_isEdit = isEdit;
/*     */   }
/*     */ 
/*     */   public void init(Properties props)
/*     */   {
/*  67 */     this.m_helper.m_props = props;
/*  68 */     this.m_binder = new DataBinder();
/*     */ 
/*  71 */     this.m_dlgHelper = this.m_helper;
/*     */     try
/*     */     {
/*  75 */       initUI(this, this.m_binder);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  79 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  83 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initUI(DialogCallback okCallback, DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/*  90 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/*  93 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/*  95 */     this.m_binder.setLocalData(this.m_helper.m_props);
/*     */ 
/*  97 */     this.m_helper.addLabelFieldPair(mainPanel, this.m_systemInterface.localizeCaption("apDCClassLabel"), this.m_docClassField = new CustomTextField(30), "dDocClass");
/*     */ 
/*  99 */     this.m_helper.addLabelFieldPair(mainPanel, this.m_systemInterface.localizeCaption("apDCDefProfileLabel"), this.m_defProfField = new CustomTextField(30), "dDefaultProfile");
/*     */ 
/* 101 */     this.m_helper.addLabelFieldPair(mainPanel, this.m_systemInterface.localizeCaption("apDCDescLabel"), this.m_descrField = new CustomTextField(30), "dDocClassDescription");
/*     */ 
/* 103 */     this.m_helper.addComponent(mainPanel, new JLabel(this.m_systemInterface.localizeCaption("apDCOptionalFieldsLabel")));
/*     */ 
/* 106 */     if (!this.m_isEdit)
/*     */       return;
/* 108 */     this.m_docClassField.setEditable(false);
/* 109 */     this.m_defProfField.setEditable(false);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 115 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public boolean handleDialogEvent(ActionEvent event)
/*     */   {
/* 122 */     String docClass = this.m_binder.getLocal("dDocClass");
/* 123 */     if ((docClass == null) || (docClass.length() == 0))
/*     */     {
/* 125 */       IdcMessage msg = IdcMessageFactory.lc("apDCEmptyClassPrompt", new Object[0]);
/* 126 */       MessageBox.doMessage(this.m_systemInterface, msg, 1);
/* 127 */       return false;
/*     */     }
/* 129 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 134 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97937 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditDocClassDlg
 * JD-Core Version:    0.5.4
 */