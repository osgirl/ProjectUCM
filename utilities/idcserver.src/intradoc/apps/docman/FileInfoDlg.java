/*     */ package intradoc.apps.docman;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.SharedContext;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class FileInfoDlg
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected SharedContext m_shContext;
/*     */   protected String m_helpPage;
/*     */ 
/*     */   public FileInfoDlg(SystemInterface sys, String helpPage, SharedContext shContext)
/*     */   {
/*  49 */     this.m_systemInterface = sys;
/*  50 */     String title = this.m_systemInterface.getString("apFileInfoTitle");
/*  51 */     this.m_helper = new DialogHelper(sys, title, true);
/*     */ 
/*  53 */     this.m_helpPage = helpPage;
/*  54 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*  55 */     this.m_shContext = shContext;
/*     */   }
/*     */ 
/*     */   public void init(Properties props)
/*     */   {
/*  60 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/*  63 */       binder.setLocalData(props);
/*  64 */       AppContextUtils.executeService(this.m_shContext, "GET_FS_DOC_INFO", binder);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  68 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apFSDocInfoError", new Object[0]));
/*  69 */       return;
/*     */     }
/*     */ 
/*  72 */     initUI();
/*  73 */     this.m_helper.m_props = binder.getLocalData();
/*  74 */     this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  79 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  85 */         return true;
/*     */       }
/*     */     };
/*  88 */     okCallback.m_dlgHelper = this.m_helper;
/*     */ 
/*  90 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/*  93 */     this.m_helper.addLabelDisplayPair(mainPanel, this.m_systemInterface.localizeCaption("apName"), 150, "dDocName");
/*     */ 
/*  95 */     this.m_helper.addLabelDisplayPair(mainPanel, this.m_systemInterface.localizeCaption("apRelativeURL"), 250, "relativeURL");
/*     */ 
/*  97 */     this.m_helper.addLabelDisplayPair(mainPanel, this.m_systemInterface.localizeCaption("apAbsoluteURL"), 250, "absoluteURL");
/*     */ 
/*  99 */     this.m_helper.addLabelDisplayPair(mainPanel, this.m_systemInterface.localizeCaption("apPrimaryLocation"), 250, "primaryFile");
/*     */ 
/* 101 */     this.m_helper.addLabelDisplayPair(mainPanel, this.m_systemInterface.localizeCaption("apAlternateLocation"), 250, "alternateFile");
/*     */ 
/* 104 */     this.m_helper.addLabelDisplayPair(mainPanel, this.m_systemInterface.localizeCaption("apWebLocation"), 250, "webViewableFile");
/*     */ 
/* 107 */     this.m_helper.addLabelDisplayPair(mainPanel, this.m_systemInterface.localizeCaption("apRenditionLocation"), 250, "renditionFiles");
/*     */ 
/* 111 */     this.m_helper.addLabelDisplayPair(mainPanel, this.m_systemInterface.localizeCaption("apContainerLocation"), 250, "containerLocation");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 126 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.FileInfoDlg
 * JD-Core Version:    0.5.4
 */