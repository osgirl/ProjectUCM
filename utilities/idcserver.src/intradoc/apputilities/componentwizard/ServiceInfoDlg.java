/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Help;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ServiceInfoDlg extends CWizardBaseDlg
/*     */ {
/*  41 */   protected ServiceData m_serviceData = null;
/*     */ 
/*     */   public ServiceInfoDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  45 */     super(sys, title, helpPage);
/*     */   }
/*     */ 
/*     */   public void init(ServiceData data, Vector actions) throws ServiceException
/*     */   {
/*  50 */     super.init();
/*     */ 
/*  52 */     this.m_serviceData = data;
/*     */ 
/*  54 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/*  55 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*  56 */     EditServicePanel srvPanel = new EditServicePanel();
/*  57 */     srvPanel.init(null, null, this.m_helper, null, 5);
/*     */ 
/*  59 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  60 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  61 */     this.m_helper.addComponent(mainPanel, srvPanel);
/*  62 */     DialogHelper dlgHelper = (DialogHelper)this.m_helper;
/*     */     try
/*     */     {
/*  65 */       srvPanel.loadAndSetServiceData(data, actions);
/*  66 */       dlgHelper.addClose(null);
/*  67 */       DialogCallback helpCallback = createHelp();
/*  68 */       dlgHelper.addHelp(helpCallback);
/*  69 */       dlgHelper.prompt();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  73 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public DialogCallback createHelp()
/*     */   {
/*  79 */     DialogCallback dlgCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  84 */         DialogHelper dlgHelper = (DialogHelper)ServiceInfoDlg.this.m_helper;
/*  85 */         dlgHelper.m_helpPage = ServiceInfoDlg.this.determineHelpPage();
/*  86 */         return true;
/*     */       }
/*     */     };
/*  89 */     return dlgCallback;
/*     */   }
/*     */ 
/*     */   public String determineHelpPage()
/*     */   {
/*  94 */     String helpPageStart = DialogHelpTable.getHelpPage("CW_ServicesInfo");
/*  95 */     String helpPage = helpPageStart + "/" + this.m_serviceData.m_name.toLowerCase() + ".htm";
/*     */ 
/*  97 */     String helpDir = Help.getHelpDir();
/*  98 */     int result = FileUtils.checkFile(helpDir + helpPage, true, false);
/*  99 */     if (result != 0)
/*     */     {
/* 102 */       helpPage = DialogHelpTable.getHelpPage("CW_ServiceInfoGeneral");
/*     */     }
/* 104 */     return helpPage;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 109 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.ServiceInfoDlg
 * JD-Core Version:    0.5.4
 */