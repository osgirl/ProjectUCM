/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SelectTableDialog extends DialogCallback
/*     */ {
/*  42 */   protected SystemInterface m_systemInterface = null;
/*  43 */   protected ExecutionContext m_context = null;
/*  44 */   protected DialogHelper m_helper = null;
/*     */   protected SchemaTablePanel m_tablePanel;
/*  46 */   protected SharedContext m_shContext = null;
/*  47 */   protected String m_helpPage = null;
/*     */ 
/*  49 */   protected DataBinder m_binder = null;
/*     */ 
/*     */   public SelectTableDialog(SystemInterface sys, SharedContext shContext, String title, String helpPage)
/*     */   {
/*  53 */     this.m_systemInterface = sys;
/*  54 */     this.m_context = sys.getExecutionContext();
/*  55 */     this.m_shContext = shContext;
/*  56 */     this.m_helper = new DialogHelper(sys, title, true);
/*  57 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(Properties props)
/*     */   {
/*  62 */     this.m_helper.m_props = props;
/*  63 */     this.m_binder = new DataBinder();
/*     */ 
/*  66 */     this.m_dlgHelper = this.m_helper;
/*     */     try
/*     */     {
/*  70 */       initUI(this, this.m_binder);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  74 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  78 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */ 
/*  81 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI(DialogCallback okCallback, DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/*  87 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/*  90 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/*  92 */     this.m_binder.setLocalData(this.m_helper.m_props);
/*     */ 
/*  94 */     this.m_tablePanel = new SchemaTablePanel();
/*  95 */     this.m_tablePanel.m_noButtons = true;
/*  96 */     this.m_tablePanel.m_subject = "";
/*  97 */     this.m_tablePanel.m_resultSetName = "UnknownTableList";
/*  98 */     this.m_tablePanel.initEx(this.m_systemInterface, binder);
/*     */ 
/* 100 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 101 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 102 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 103 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 104 */     this.m_helper.addComponent(mainPanel, this.m_tablePanel);
/*     */ 
/* 106 */     this.m_tablePanel.loadPanelInformation();
/*     */   }
/*     */ 
/*     */   public boolean handleDialogEvent(ActionEvent event)
/*     */   {
/* 112 */     IdcMessage errorMessage = this.m_tablePanel.retrievePanelValuesAndValidate();
/* 113 */     if (errorMessage != null)
/*     */     {
/* 115 */       this.m_errorMessage = errorMessage;
/* 116 */       return false;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 121 */       DataBinder binder = new DataBinder();
/* 122 */       binder.putLocal("schTableName", this.m_helper.m_props.getProperty("schTableName"));
/* 123 */       binder.putLocal("IsAddExistingTable", "1");
/* 124 */       this.m_shContext.executeService("ADDOREDIT_SCHEMA_TABLE", binder, true);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 128 */       this.m_errorMessage = LocaleUtils.createMessageListFromThrowable(e);
/* 129 */       return false;
/*     */     }
/* 131 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 136 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80220 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.SelectTableDialog
 * JD-Core Version:    0.5.4
 */