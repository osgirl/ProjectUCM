/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.SharedContext;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ViewAutomationPanel extends PanePanel
/*     */   implements ActionListener
/*     */ {
/*  50 */   protected ContainerHelper m_helper = null;
/*  51 */   protected DialogHelper m_parent = null;
/*  52 */   protected CollectionContext m_collectionContext = null;
/*  53 */   protected ExecutionContext m_cxt = null;
/*  54 */   protected String m_removeAction = null;
/*  55 */   protected boolean m_isTransfer = false;
/*  56 */   protected boolean m_isQueued = false;
/*     */ 
/*  59 */   protected UdlPanel m_collectionList = null;
/*  60 */   protected String m_title = null;
/*  61 */   protected String m_resultSetName = null;
/*     */ 
/*  63 */   protected final String[][] BUTTON_INFO = { { "apLabelRemove", "remove" }, { "apLabelClose", "close" } };
/*     */ 
/*     */   public ViewAutomationPanel(DialogHelper parent, CollectionContext ctxt)
/*     */   {
/*  71 */     this.m_parent = parent;
/*  72 */     this.m_collectionContext = ctxt;
/*  73 */     this.m_cxt = parent.m_exchange.m_sysInterface.getExecutionContext();
/*     */ 
/*  75 */     this.m_helper = new ContainerHelper();
/*  76 */     this.m_helper.attachToContainer(this, parent.m_exchange.m_sysInterface, null);
/*     */   }
/*     */ 
/*     */   public void init(String title, String resultSetName, String remAction)
/*     */   {
/*  81 */     this.m_title = title;
/*  82 */     this.m_resultSetName = resultSetName;
/*  83 */     this.m_removeAction = remAction;
/*     */ 
/*  86 */     resultSetName = resultSetName.toLowerCase();
/*  87 */     this.m_isTransfer = (resultSetName.indexOf("transfer") > 0);
/*  88 */     this.m_isQueued = (resultSetName.indexOf("queue") > 0);
/*     */ 
/*  90 */     JPanel pnl = initUI();
/*     */ 
/*  92 */     this.m_helper.makePanelGridBag(this, 1);
/*  93 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  94 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  95 */     this.m_helper.addLastComponentInRow(this, pnl);
/*     */   }
/*     */ 
/*     */   protected JPanel initUI()
/*     */   {
/* 100 */     this.m_collectionList = new UdlPanel(this.m_title, null, 460, 7, this.m_resultSetName, true);
/* 101 */     this.m_collectionList.setIDColumn("aArchiveLocation");
/*     */ 
/* 103 */     String visibleColumns = null;
/* 104 */     if (this.m_isTransfer)
/*     */     {
/* 106 */       visibleColumns = "aArchiveLocation,aTargetArchive,aStatus";
/*     */     }
/* 108 */     else if (this.m_isQueued)
/*     */     {
/* 110 */       visibleColumns = "aArchiveLocation,aImportLogonUser,aStatus";
/*     */     }
/*     */     else
/*     */     {
/* 114 */       visibleColumns = "aArchiveLocation,aStatus";
/*     */     }
/* 116 */     String locationLabel = (this.m_isTransfer) ? "apLabelTransferSource" : "apLabelLocation";
/* 117 */     this.m_collectionList.setColumnInfo(new ColumnInfo(LocaleResources.getString(locationLabel, this.m_cxt), "aArchiveLocation", 20.0D));
/*     */ 
/* 119 */     if (this.m_isTransfer)
/*     */     {
/* 121 */       this.m_collectionList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelTransferTarget", this.m_cxt), "aTargetArchive", 20.0D));
/*     */     }
/* 124 */     else if (this.m_isQueued)
/*     */     {
/* 126 */       this.m_collectionList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelImportLoginUser", this.m_cxt), "aImportLogonUser", 20.0D));
/*     */     }
/*     */ 
/* 129 */     this.m_collectionList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelStatus", this.m_cxt), "aStatus", 10.0D));
/*     */ 
/* 131 */     this.m_collectionList.setVisibleColumns(visibleColumns);
/*     */ 
/* 133 */     this.m_collectionList.init();
/* 134 */     this.m_collectionList.useDefaultListener();
/*     */ 
/* 136 */     addDisplayMaps();
/*     */ 
/* 139 */     JButton removeBtn = this.m_collectionList.addButton(LocaleResources.getString("apLabelRemove", this.m_cxt), true);
/*     */ 
/* 141 */     removeBtn.setActionCommand("remove");
/* 142 */     removeBtn.addActionListener(this);
/* 143 */     removeBtn.setEnabled(false);
/*     */ 
/* 145 */     JButton closeBtn = this.m_collectionList.addButton(LocaleResources.getString("apLabelClose", this.m_cxt), false);
/*     */ 
/* 147 */     closeBtn.setActionCommand("close");
/* 148 */     closeBtn.addActionListener(this);
/*     */ 
/* 150 */     JPanel buttonPnl = new PanePanel();
/* 151 */     buttonPnl.add(removeBtn);
/* 152 */     buttonPnl.add(closeBtn);
/*     */ 
/* 154 */     this.m_collectionList.add("South", buttonPnl);
/*     */ 
/* 156 */     return this.m_collectionList;
/*     */   }
/*     */ 
/*     */   protected void addDisplayMaps()
/*     */   {
/* 161 */     DisplayStringCallback dsc = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 167 */         String[][] displayMap = { { "true", "connected" }, { "false", "disconnected" } };
/*     */ 
/* 172 */         return StringUtils.getPresentationString(displayMap, value);
/*     */       }
/*     */     };
/* 175 */     this.m_collectionList.setDisplayCallback("aStatus", dsc);
/*     */   }
/*     */ 
/*     */   public void refreshList(DataBinder binder)
/*     */   {
/* 180 */     this.m_collectionList.refreshList(binder, null);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 185 */     String cmd = e.getActionCommand();
/* 186 */     if (cmd.equals("close"))
/*     */     {
/* 188 */       this.m_parent.close();
/*     */     } else {
/* 190 */       if (!cmd.equals("remove"))
/*     */         return;
/* 192 */       removeAction();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void removeAction()
/*     */   {
/* 198 */     int index = this.m_collectionList.getSelectedIndex();
/* 199 */     if (index < 0)
/*     */     {
/* 201 */       return;
/*     */     }
/*     */ 
/* 204 */     Properties props = this.m_collectionList.getDataAt(index);
/*     */     try
/*     */     {
/* 207 */       DataBinder binder = new DataBinder();
/* 208 */       binder.setLocalData(props);
/*     */ 
/* 210 */       SharedContext shContext = this.m_collectionContext.getSharedContext();
/* 211 */       shContext.executeService(this.m_removeAction, binder, true);
/* 212 */       refreshList(binder);
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 216 */       this.m_collectionContext.reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 222 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80439 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ViewAutomationPanel
 * JD-Core Version:    0.5.4
 */