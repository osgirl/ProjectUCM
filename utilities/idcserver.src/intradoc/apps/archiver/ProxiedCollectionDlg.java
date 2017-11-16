/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
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
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ProxiedCollectionDlg
/*     */ {
/*  50 */   protected SystemInterface m_systemInterface = null;
/*  51 */   protected ExecutionContext m_cxt = null;
/*  52 */   protected DialogHelper m_helper = null;
/*  53 */   protected CollectionContext m_context = null;
/*     */ 
/*  55 */   protected UdlPanel m_proxiedList = null;
/*  56 */   protected UdlPanel m_collectionList = null;
/*     */ 
/*  58 */   DataBinder m_binder = null;
/*     */ 
/*     */   public ProxiedCollectionDlg(SystemInterface sys, String title, CollectionContext ctxt)
/*     */   {
/*  62 */     this.m_systemInterface = sys;
/*  63 */     this.m_cxt = sys.getExecutionContext();
/*  64 */     this.m_helper = new DialogHelper(this.m_systemInterface, title, true);
/*  65 */     this.m_context = ctxt;
/*     */ 
/*  67 */     this.m_binder = new DataBinder();
/*     */   }
/*     */ 
/*     */   public int init()
/*     */   {
/*  72 */     DataResultSet drset = null;
/*     */     try
/*     */     {
/*  75 */       DataBinder data = new DataBinder();
/*  76 */       SharedContext sCtxt = this.m_context.getSharedContext();
/*  77 */       sCtxt.executeService("GET_PROXIEDSERVERS", data, false);
/*     */ 
/*  79 */       drset = (DataResultSet)data.getResultSet("OutgoingProviders");
/*  80 */       if (drset == null)
/*     */       {
/*  82 */         throw new Exception(LocaleResources.getString("apUnableToGetProxiedServersList", this.m_cxt));
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  87 */       this.m_context.reportError(e, "");
/*  88 */       return 0;
/*     */     }
/*     */ 
/*  91 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  96 */         int index = ProxiedCollectionDlg.this.m_proxiedList.getSelectedIndex();
/*  97 */         if (index < 0)
/*     */         {
/*  99 */           ProxiedCollectionDlg.this.m_context.reportError(LocaleResources.getString("apSelectProxiedServer", this.m_dlgHelper.m_exchange.m_sysInterface.getExecutionContext()));
/*     */ 
/* 101 */           return false;
/*     */         }
/*     */ 
/* 104 */         Properties props = ProxiedCollectionDlg.this.m_proxiedList.getDataAt(index);
/* 105 */         index = ProxiedCollectionDlg.this.m_collectionList.getSelectedIndex();
/* 106 */         if (index < 0)
/*     */         {
/* 108 */           ProxiedCollectionDlg.this.m_context.reportError(LocaleResources.getString("apSelectCollection", this.m_dlgHelper.m_exchange.m_sysInterface.getExecutionContext()));
/*     */ 
/* 110 */           return false;
/*     */         }
/*     */ 
/* 114 */         Properties collProps = ProxiedCollectionDlg.this.m_collectionList.getDataAt(index);
/* 115 */         String exportDir = collProps.getProperty("aCollectionExportLocation");
/* 116 */         if ((exportDir == null) || (exportDir.length() <= 0))
/*     */         {
/* 118 */           collProps.setProperty("aCollectionExportLocation", collProps.getProperty("aCollectionLocation"));
/*     */         }
/* 120 */         CollectionData collData = new CollectionData(-1, collProps.getProperty("IDC_Name"), collProps.getProperty("aCollectionLocation"), collProps.getProperty("aCollectionExportLocation"), "", "");
/*     */ 
/* 122 */         if (collData.isProxied())
/*     */         {
/* 124 */           ProxiedCollectionDlg.this.m_context.reportError(LocaleResources.getString("apSelectNonProxiedCollection", this.m_dlgHelper.m_exchange.m_sysInterface.getExecutionContext()));
/*     */ 
/* 127 */           return false;
/*     */         }
/*     */ 
/* 130 */         DataBinder.mergeHashTables(props, collProps);
/* 131 */         ProxiedCollectionDlg.this.m_binder.setLocalData(props);
/*     */         try
/*     */         {
/* 135 */           AppLauncher.executeService("ADD_PROXIEDCOLLECTION", props);
/* 136 */           return true;
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 140 */           ProxiedCollectionDlg.this.m_context.reportError(exp, "");
/*     */         }
/* 142 */         return false;
/*     */       }
/*     */     };
/* 146 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, DialogHelpTable.getHelpPage("BrowseProxiedCollection"));
/*     */ 
/* 148 */     initUI(mainPanel, drset);
/*     */ 
/* 150 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel mainPanel, DataResultSet drset)
/*     */   {
/* 155 */     this.m_proxiedList = new UdlPanel(LocaleResources.getString("apLabelProxiedServers", this.m_cxt), null, 250, 15, "OutgoingProviders", true);
/*     */ 
/* 157 */     this.m_proxiedList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelInstanceName", this.m_cxt), "psIDC_Name", 10.0D));
/*     */ 
/* 159 */     this.m_proxiedList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelRelativeWebRoot", this.m_cxt), "psHttpRelativeWebRoot", 10.0D));
/*     */ 
/* 161 */     this.m_proxiedList.setVisibleColumns("psIDC_Name,psHttpRelativeWebRoot");
/* 162 */     this.m_proxiedList.init();
/*     */ 
/* 164 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 168 */         ProxiedCollectionDlg.this.checkSelection();
/*     */       }
/*     */     };
/* 171 */     this.m_proxiedList.addItemListener(iListener);
/* 172 */     this.m_proxiedList.refreshList(drset, null);
/*     */ 
/* 174 */     this.m_collectionList = new UdlPanel(LocaleResources.getString("apTitleCollections", this.m_cxt), null, 250, 15, "ProxiedCollections", true);
/*     */ 
/* 176 */     this.m_collectionList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleName", this.m_cxt), "IDC_Name", 10.0D));
/*     */ 
/* 178 */     this.m_collectionList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleLocation", this.m_cxt), "aCollectionLocation", 10.0D));
/*     */ 
/* 180 */     this.m_collectionList.setVisibleColumns("IDC_Name,aCollectionLocation");
/* 181 */     this.m_collectionList.init();
/*     */ 
/* 183 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 184 */     gh.m_gc.weighty = 1.0D;
/*     */ 
/* 186 */     gh.prepareAddRowElement(13);
/* 187 */     this.m_helper.addComponent(mainPanel, this.m_proxiedList);
/*     */ 
/* 189 */     gh.prepareAddLastRowElement();
/* 190 */     this.m_helper.addComponent(mainPanel, this.m_collectionList);
/*     */   }
/*     */ 
/*     */   public void checkSelection()
/*     */   {
/* 195 */     int index = this.m_proxiedList.getSelectedIndex();
/* 196 */     if (index < 0)
/*     */     {
/* 198 */       DataResultSet drset = new DataResultSet();
/* 199 */       this.m_collectionList.refreshList(drset, null);
/* 200 */       return;
/*     */     }
/* 202 */     Properties props = this.m_proxiedList.getDataAt(index);
/* 203 */     DataBinder binder = new DataBinder();
/* 204 */     binder.setLocalData(props);
/*     */     try
/*     */     {
/* 208 */       SharedContext shCtxt = this.m_context.getSharedContext();
/* 209 */       shCtxt.executeService("GET_PROXIED_ARCHIVECOLLECTIONS", binder, false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 213 */       this.m_context.reportError(e, "");
/* 214 */       return;
/*     */     }
/*     */ 
/* 217 */     this.m_collectionList.refreshList(binder, null);
/*     */   }
/*     */ 
/*     */   public String getIdcName()
/*     */   {
/* 222 */     return this.m_binder.getLocal("IDC_Name");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 227 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97159 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ProxiedCollectionDlg
 * JD-Core Version:    0.5.4
 */