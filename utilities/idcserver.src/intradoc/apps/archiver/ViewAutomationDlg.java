/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ViewAutomationDlg
/*     */ {
/*  41 */   protected DialogHelper m_helper = null;
/*  42 */   protected SystemInterface m_systemInterface = null;
/*  43 */   protected ExecutionContext m_cxt = null;
/*     */ 
/*  45 */   protected CollectionContext m_collectionContext = null;
/*  46 */   protected CollectionData m_collectionData = null;
/*     */ 
/*  48 */   protected ViewAutomationPanel[] m_tabPanels = null;
/*     */ 
/*     */   public ViewAutomationDlg(SystemInterface sys, String title)
/*     */   {
/*  52 */     this.m_helper = new DialogHelper(sys, title, true);
/*  53 */     this.m_systemInterface = sys;
/*  54 */     this.m_cxt = sys.getExecutionContext();
/*     */   }
/*     */ 
/*     */   public void init(CollectionContext ctxt, CollectionData data)
/*     */   {
/*  59 */     this.m_collectionContext = ctxt;
/*  60 */     this.m_collectionData = data;
/*     */ 
/*  62 */     initUI();
/*     */     try
/*     */     {
/*  66 */       refreshLists();
/*  67 */       this.m_helper.prompt();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  71 */       this.m_collectionContext.reportError(e, LocaleResources.getString("apUnableToGetAutomationData", this.m_cxt));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  78 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/*  79 */     this.m_helper.makePanelGridBag(mainPanel, 0);
/*     */ 
/*  81 */     JPanel tabPanel = initPanels();
/*  82 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  83 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  84 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*  85 */     this.m_helper.addComponent(mainPanel, tabPanel);
/*     */   }
/*     */ 
/*     */   protected JPanel initPanels()
/*     */   {
/*  90 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*     */ 
/*  94 */     String[][] PANEL_INFOS = { { "apTitleExporters", "apServerAutomaticallyExportingTo", "RegisteredExporters", "REMOVE_EXPORTER" }, { "apTitleImporters", "apServerAutomaticallyImportingFrom", "RegisteredImporters", "REMOVE_IMPORTER" }, { "apTitleTransfers", "apServerAutomaticallyTransferring", "AutomatedTransfers", "REMOVE_TRANSFER" }, { "apTitleQueuedImports", "apServerAutomaticallyQueuedImport", "QueuedImporters", "REMOVE_QUEUED_IMPORT" } };
/*     */ 
/* 107 */     int numPanels = PANEL_INFOS.length;
/* 108 */     TabPanel tab = new TabPanel();
/* 109 */     this.m_tabPanels = new ViewAutomationPanel[numPanels];
/*     */ 
/* 111 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 113 */       this.m_tabPanels[i] = new ViewAutomationPanel(this.m_helper, this.m_collectionContext);
/* 114 */       this.m_tabPanels[i].init(LocaleResources.getString(PANEL_INFOS[i][1], this.m_cxt, idcName), PANEL_INFOS[i][2], PANEL_INFOS[i][3]);
/*     */ 
/* 116 */       tab.addPane(LocaleResources.getString(PANEL_INFOS[i][0], this.m_cxt), this.m_tabPanels[i]);
/*     */     }
/* 118 */     return tab;
/*     */   }
/*     */ 
/*     */   protected void refreshLists() throws ServiceException
/*     */   {
/* 123 */     DataBinder binder = new DataBinder();
/*     */ 
/* 125 */     SharedContext shContext = this.m_collectionContext.getSharedContext();
/* 126 */     shContext.executeService("GET_REPLICATION_DATA", binder, false);
/* 127 */     for (int i = 0; i < this.m_tabPanels.length; ++i)
/*     */     {
/* 129 */       this.m_tabPanels[i].refreshList(binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 135 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ViewAutomationDlg
 * JD-Core Version:    0.5.4
 */