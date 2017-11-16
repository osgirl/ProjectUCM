/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.ArchiveData;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class CollectionArchivesDlg extends EditDlg
/*     */ {
/*  52 */   protected String m_curArchive = null;
/*  53 */   protected UdlPanel m_collectionList = null;
/*  54 */   protected UdlPanel m_archiveList = null;
/*     */ 
/*     */   public CollectionArchivesDlg(SystemInterface sys, String title, CollectionContext context, String helpPage)
/*     */   {
/*  59 */     super(sys, title, context, helpPage);
/*  60 */     this.m_editItems = "aTargetArchive,aTransferOwner";
/*  61 */     this.m_action = "EDIT_TRANSFEROPTIONS";
/*     */   }
/*     */ 
/*     */   public int init(Properties props)
/*     */   {
/*  67 */     this.m_curArchive = props.getProperty("aArchiveName");
/*  68 */     return super.init(props);
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel mainPanel)
/*     */   {
/*  74 */     DataResultSet drset = SharedObjects.getTable("ArchiveCollections");
/*     */ 
/*  76 */     this.m_collectionList = new UdlPanel(LocaleResources.getString("apTitleCollections", this.m_cxt), null, 300, 10, "ArchiveCollections", true);
/*     */ 
/*  78 */     this.m_collectionList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleName", this.m_cxt), "IDC_Name", 10.0D));
/*     */ 
/*  80 */     this.m_collectionList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleLocation", this.m_cxt), "aCollectionLocation", 10.0D));
/*     */ 
/*  82 */     this.m_collectionList.setVisibleColumns("IDC_Name,aCollectionLocation");
/*  83 */     this.m_collectionList.init();
/*     */ 
/*  85 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/*  89 */         CollectionArchivesDlg.this.checkSelection();
/*     */       }
/*     */     };
/*  92 */     this.m_collectionList.addItemListener(iListener);
/*  93 */     this.m_collectionList.refreshList(drset, null);
/*     */ 
/*  95 */     this.m_archiveList = new UdlPanel(LocaleResources.getString("apLabelArchives", this.m_cxt), null, 300, 10, "ArchiveData", true);
/*     */ 
/*  98 */     this.m_archiveList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelName", this.m_cxt), "aArchiveName", 10.0D));
/*     */ 
/* 100 */     this.m_archiveList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelTargetable", this.m_cxt), "aIsTargetable", 20.0D));
/*     */ 
/* 102 */     this.m_archiveList.setVisibleColumns("aArchiveName,aIsTargetable");
/* 103 */     this.m_archiveList.setIDColumn("aArchiveName");
/*     */ 
/* 105 */     DisplayStringCallbackAdaptor dspCallback = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 111 */         String displayStr = StringUtils.getPresentationString(TableFields.YESNO_OPTIONLIST, value);
/* 112 */         if (displayStr == null)
/*     */         {
/* 114 */           displayStr = value;
/*     */         }
/* 116 */         return displayStr;
/*     */       }
/*     */     };
/* 119 */     this.m_archiveList.setDisplayCallback("aIsTargetable", dspCallback);
/* 120 */     this.m_archiveList.init();
/*     */ 
/* 122 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 123 */     gh.m_gc.weighty = 1.0D;
/*     */ 
/* 125 */     gh.prepareAddRowElement(13);
/* 126 */     this.m_helper.addComponent(mainPanel, this.m_collectionList);
/*     */ 
/* 128 */     gh.prepareAddLastRowElement();
/* 129 */     this.m_helper.addComponent(mainPanel, this.m_archiveList);
/*     */   }
/*     */ 
/*     */   public void checkSelection()
/*     */   {
/* 134 */     int index = this.m_collectionList.getSelectedIndex();
/* 135 */     if (index < 0)
/*     */     {
/* 137 */       DataResultSet drset = new DataResultSet();
/* 138 */       this.m_archiveList.refreshList(drset, null);
/* 139 */       return;
/*     */     }
/* 141 */     Properties props = this.m_collectionList.getDataAt(index);
/* 142 */     DataBinder binder = new DataBinder();
/* 143 */     binder.setLocalData(props);
/*     */     try
/*     */     {
/* 148 */       AppLauncher.executeService("GET_ARCHIVES", binder);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 152 */       this.m_collectionContext.reportError(e, "");
/* 153 */       return;
/*     */     }
/*     */ 
/* 156 */     DataResultSet drset = (DataResultSet)binder.getResultSet("ArchiveData");
/* 157 */     ArchiveData aData = new ArchiveData();
/*     */     try
/*     */     {
/* 160 */       aData.loadEx(drset, false);
/*     */ 
/* 163 */       drset = new DataResultSet(new String[] { "aArchiveName", "aIsTargetable" });
/* 164 */       int nameIndex = ResultSetUtils.getIndexMustExist(aData, "aArchiveName");
/* 165 */       for (aData.first(); aData.isRowPresent(); aData.next())
/*     */       {
/* 167 */         String name = aData.getStringValue(nameIndex);
/* 168 */         DataBinder data = aData.getArchiveData(name);
/*     */ 
/* 170 */         boolean isTargetable = StringUtils.convertToBool(data.getLocal("aIsTargetable"), false);
/* 171 */         String str = (isTargetable) ? "1" : "0";
/*     */ 
/* 173 */         Vector v = new IdcVector();
/* 174 */         v.addElement(name);
/* 175 */         v.addElement(str);
/* 176 */         drset.addRow(v);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 181 */       this.m_collectionContext.reportError(e, LocaleResources.getString("apUnableToDisplayArchiveList", this.m_cxt));
/*     */     }
/*     */ 
/* 184 */     this.m_archiveList.refreshList(drset, null);
/*     */   }
/*     */ 
/*     */   public boolean prepareOkEvent()
/*     */   {
/* 190 */     int index = this.m_collectionList.getSelectedIndex();
/* 191 */     if (index < 0)
/*     */     {
/* 193 */       this.m_collectionContext.reportError(LocaleResources.getString("apSelectCollection", this.m_cxt));
/* 194 */       return false;
/*     */     }
/* 196 */     Properties props = this.m_collectionList.getDataAt(index);
/*     */ 
/* 198 */     index = this.m_archiveList.getSelectedIndex();
/* 199 */     if (index < 0)
/*     */     {
/* 201 */       this.m_collectionContext.reportError(LocaleResources.getString("apSelectTargetableArchive", this.m_cxt));
/* 202 */       return false;
/*     */     }
/*     */ 
/* 205 */     Properties collProps = this.m_archiveList.getDataAt(index);
/* 206 */     DataBinder.mergeHashTables(props, collProps);
/*     */ 
/* 208 */     boolean isTargetable = StringUtils.convertToBool(props.getProperty("aIsTargetable"), false);
/* 209 */     if (!isTargetable)
/*     */     {
/* 211 */       this.m_collectionContext.reportError(LocaleResources.getString("apArchiveNotTargetable", this.m_cxt) + " " + LocaleResources.getString("apSelectTargetableArchive", this.m_cxt));
/*     */ 
/* 213 */       return false;
/*     */     }
/*     */ 
/* 217 */     String collName = props.getProperty("IDC_Name");
/* 218 */     String archiveName = props.getProperty("aArchiveName");
/* 219 */     String location = collName + "/" + archiveName;
/*     */ 
/* 222 */     CollectionData curCollection = this.m_collectionContext.getCurrentCollection();
/* 223 */     if ((archiveName.equals(this.m_curArchive)) && (collName.equals(curCollection.m_name)))
/*     */     {
/* 225 */       this.m_collectionContext.reportError(LocaleResources.getString("apArchiveCannotTransferToItself", this.m_cxt) + " " + LocaleResources.getString("apSelectTargetableArchive", this.m_cxt));
/*     */ 
/* 228 */       return false;
/*     */     }
/* 230 */     props.put("aTargetArchive", location);
/* 231 */     props.put("aTransferOwner", SharedObjects.getEnvironmentValue("IDC_Name"));
/*     */ 
/* 233 */     String isAutoTransfer = this.m_helper.m_props.getProperty("aIsAutomatedTransfer");
/* 234 */     if (isAutoTransfer != null)
/*     */     {
/* 236 */       props.put("aIsAutomatedTransfer", isAutoTransfer);
/*     */     }
/*     */ 
/* 239 */     this.m_helper.m_props = props;
/* 240 */     return true;
/*     */   }
/*     */ 
/*     */   public Properties getProperties()
/*     */   {
/* 245 */     return this.m_helper.m_props;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 250 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.CollectionArchivesDlg
 * JD-Core Version:    0.5.4
 */