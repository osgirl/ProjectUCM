/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.SharedContext;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DataSourceCollectionDlg
/*     */ {
/*  42 */   protected SystemInterface m_systemInterface = null;
/*  43 */   protected ExecutionContext m_cxt = null;
/*  44 */   protected DialogHelper m_helper = null;
/*  45 */   protected CollectionContext m_context = null;
/*     */ 
/*  47 */   protected UdlPanel m_providerList = null;
/*     */ 
/*  49 */   DataBinder m_binder = null;
/*     */ 
/*     */   public DataSourceCollectionDlg(SystemInterface sys, String title, CollectionContext ctxt)
/*     */   {
/*  53 */     this.m_systemInterface = sys;
/*  54 */     this.m_cxt = sys.getExecutionContext();
/*  55 */     this.m_helper = new DialogHelper(this.m_systemInterface, title, true);
/*  56 */     this.m_context = ctxt;
/*     */ 
/*  58 */     this.m_binder = new DataBinder();
/*     */   }
/*     */ 
/*     */   public int init()
/*     */   {
/*  63 */     DataResultSet drset = null;
/*     */     try
/*     */     {
/*  66 */       DataBinder data = new DataBinder();
/*     */       try
/*     */       {
/*  70 */         SharedContext sCtxt = this.m_context.getSharedContext();
/*  71 */         sCtxt.executeService("GET_NON_SYSTEM_DATABASE_PROVIDERS", data, false);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*  75 */         this.m_context.reportError(e, "");
/*     */       }
/*     */ 
/*  78 */       drset = (DataResultSet)data.getResultSet("DatabaseProviders");
/*  79 */       if (drset == null)
/*     */       {
/*  81 */         throw new Exception(LocaleResources.getString("apUnableToGetDatabaseProviderList", this.m_cxt));
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  86 */       this.m_context.reportError(e, "");
/*  87 */       return 0;
/*     */     }
/*     */ 
/*  90 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  95 */         int index = DataSourceCollectionDlg.this.m_providerList.getSelectedIndex();
/*  96 */         if (index < 0)
/*     */         {
/*  98 */           DataSourceCollectionDlg.this.m_context.reportError(LocaleResources.getString("apSelectDatabaseProviders", this.m_dlgHelper.m_exchange.m_sysInterface.getExecutionContext()));
/*     */ 
/* 100 */           return false;
/*     */         }
/*     */ 
/* 103 */         Properties props = DataSourceCollectionDlg.this.m_providerList.getDataAt(index);
/* 104 */         String providerName = props.getProperty("ProviderName");
/* 105 */         DataSourceCollectionDlg.this.m_binder.setLocalData(props);
/*     */         try
/*     */         {
/* 109 */           SharedContext sCtxt = DataSourceCollectionDlg.this.m_context.getSharedContext();
/* 110 */           sCtxt.executeService("GET_DATASOURCE_COLLECTION", DataSourceCollectionDlg.this.m_binder, false);
/* 111 */           DataResultSet collection = (DataResultSet)DataSourceCollectionDlg.this.m_binder.getResultSet("Collection");
/*     */ 
/* 114 */           String dir = "idc://idcproviders/" + providerName + "/" + collection.getStringValueByName("CollectionDir");
/*     */ 
/* 116 */           String file = collection.getStringValueByName("CollectionFile");
/*     */ 
/* 118 */           DataBinder data = new DataBinder();
/* 119 */           data = ResourceUtils.readDataBinder(dir, file);
/*     */ 
/* 121 */           String idcName = data.getLocal("IDC_Name");
/* 122 */           if (idcName == null)
/*     */           {
/* 125 */             DataSourceCollectionDlg.this.m_context.reportError(LocaleResources.getString("apUnnamedCollection", DataSourceCollectionDlg.this.m_cxt));
/* 126 */             return false;
/*     */           }
/* 128 */           DataSourceCollectionDlg.this.m_binder.putLocal("IDC_Name", idcName);
/*     */ 
/* 130 */           CollectionDlg dlg = new CollectionDlg(DataSourceCollectionDlg.this.m_systemInterface, LocaleResources.getString("apTitleBrowseToArchiverCollection", DataSourceCollectionDlg.this.m_cxt));
/*     */ 
/* 132 */           if (dlg.init(idcName, dir + file, false) == 1)
/*     */           {
/* 134 */             return true;
/*     */           }
/*     */         }
/*     */         catch (Exception err)
/*     */         {
/* 139 */           DataSourceCollectionDlg.this.m_context.reportError(err, "");
/*     */         }
/* 141 */         return false;
/*     */       }
/*     */     };
/* 146 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, false, null);
/* 147 */     initUI(mainPanel, drset);
/*     */ 
/* 149 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel mainPanel, DataResultSet drset)
/*     */   {
/* 154 */     this.m_providerList = new UdlPanel(LocaleResources.getString("apLabelDatabaseProviders", this.m_cxt), null, 250, 10, "DatabaseProviders", true);
/*     */ 
/* 156 */     this.m_providerList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelProviderName", this.m_cxt), "ProviderName", 10.0D));
/*     */ 
/* 158 */     this.m_providerList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelProviderDescription", this.m_cxt), "ProviderDescription", 10.0D));
/*     */ 
/* 160 */     this.m_providerList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelConnectionState", this.m_cxt), "ConnectionState", 10.0D));
/*     */ 
/* 162 */     this.m_providerList.setVisibleColumns("ProviderName,ProviderDescription,ConnectionState");
/* 163 */     this.m_providerList.init();
/*     */ 
/* 165 */     this.m_providerList.refreshList(drset, null);
/*     */ 
/* 167 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 168 */     gh.m_gc.weighty = 1.0D;
/*     */ 
/* 170 */     gh.prepareAddRowElement(10);
/* 171 */     this.m_helper.addComponent(mainPanel, this.m_providerList);
/*     */   }
/*     */ 
/*     */   public String getIdcName()
/*     */   {
/* 176 */     return this.m_binder.getLocal("IDC_Name");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 181 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97779 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.DataSourceCollectionDlg
 * JD-Core Version:    0.5.4
 */