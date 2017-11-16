/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.Help;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.IdcFileChooser;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.filechooser.FileFilter;
/*     */ 
/*     */ public class OpenCollectionDlg
/*     */   implements ActionListener
/*     */ {
/*  67 */   protected SystemInterface m_systemInterface = null;
/*  68 */   protected ExecutionContext m_cxt = null;
/*  69 */   protected DialogHelper m_helper = null;
/*  70 */   protected String m_helpPage = null;
/*  71 */   protected CollectionContext m_context = null;
/*     */ 
/*  73 */   protected CollectionData m_currentCollection = null;
/*  74 */   public Properties m_selectedCollection = null;
/*     */ 
/*  76 */   protected UdlPanel m_collectionList = null;
/*     */ 
/*  78 */   protected Vector m_buttons = new IdcVector();
/*  79 */   protected String[][] BUTTON_INFO = { { "apLabelOpen", "open" }, { "apDlgButtonBrowseLocal", "browseLocal" }, { "apDlgButtonBrowseDataSource", "browseDataSource" }, { "apDlgButtonBrowseProxied", "browseProxied" }, { "apLabelRemove", "remove" }, { "apLabelCancel", "cancel" }, { "apLabelHelp", "help" } };
/*     */ 
/*     */   public OpenCollectionDlg(SystemInterface sys, String title)
/*     */   {
/*  93 */     this.m_systemInterface = sys;
/*  94 */     this.m_cxt = sys.getExecutionContext();
/*  95 */     this.m_helper = new DialogHelper(this.m_systemInterface, title, true);
/*  96 */     this.m_helpPage = DialogHelpTable.getHelpPage("OpenArchiverCollection");
/*     */   }
/*     */ 
/*     */   public void init(CollectionData data, CollectionContext context)
/*     */   {
/* 101 */     this.m_currentCollection = data;
/* 102 */     this.m_context = context;
/*     */ 
/* 104 */     this.m_collectionList = new UdlPanel(LocaleResources.getString("apTitleCollections", this.m_cxt), null, 300, 5, "ArchiveCollections", true);
/*     */ 
/* 106 */     this.m_collectionList.setIDColumn("IDC_Name");
/*     */ 
/* 108 */     this.m_collectionList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelInstanceName", this.m_cxt), "IDC_Name", 10.0D));
/*     */ 
/* 110 */     this.m_collectionList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelCollectionLocation", this.m_cxt), "aCollectionLocation", 20.0D));
/*     */ 
/* 112 */     this.m_collectionList.setVisibleColumns("IDC_Name,aCollectionLocation");
/*     */ 
/* 114 */     this.m_collectionList.init();
/* 115 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 119 */         OpenCollectionDlg.this.checkSelection();
/*     */       }
/*     */     };
/* 122 */     this.m_collectionList.addItemListener(iListener);
/*     */ 
/* 124 */     ActionListener aListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 128 */         OpenCollectionDlg.this.openCollection();
/*     */       }
/*     */     };
/* 131 */     this.m_collectionList.m_list.addActionListener(aListener);
/*     */ 
/* 133 */     JPanel btnPanel = new PanePanel();
/* 134 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 136 */     Insets stdIns = new Insets(5, 5, 5, 5);
/* 137 */     for (int i = 0; i < this.BUTTON_INFO.length; ++i)
/*     */     {
/* 139 */       Insets ins = stdIns;
/* 140 */       if (i == 0)
/*     */       {
/* 142 */         ins = new Insets(20, 5, 5, 5);
/*     */       }
/* 144 */       else if (i == this.BUTTON_INFO.length - 1)
/*     */       {
/* 146 */         ins = new Insets(5, 5, 10, 5);
/*     */       }
/* 148 */       this.m_helper.m_gridHelper.m_gc.insets = ins;
/*     */ 
/* 150 */       JButton btn = new JButton(LocaleResources.getString(this.BUTTON_INFO[i][0], this.m_cxt));
/* 151 */       this.m_helper.addLastComponentInRow(btnPanel, btn);
/*     */ 
/* 153 */       btn.setActionCommand(this.BUTTON_INFO[i][1]);
/* 154 */       btn.addActionListener(this);
/* 155 */       this.m_buttons.addElement(btn);
/*     */     }
/*     */ 
/* 158 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 159 */     mainPanel.setLayout(new BorderLayout());
/*     */ 
/* 161 */     mainPanel.add("Center", this.m_collectionList);
/* 162 */     mainPanel.add("East", btnPanel);
/*     */ 
/* 164 */     String sel = null;
/* 165 */     if (this.m_currentCollection != null)
/*     */     {
/* 167 */       sel = this.m_currentCollection.m_name;
/*     */     }
/* 169 */     refreshCollections(sel);
/*     */   }
/*     */ 
/*     */   protected int prompt()
/*     */   {
/* 174 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void refreshCollections(String selObj)
/*     */   {
/* 179 */     DataResultSet rset = SharedObjects.getTable("ArchiveCollections");
/* 180 */     if (rset == null)
/*     */     {
/* 182 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 186 */       ResultSetUtils.sortResultSet(rset, new String[] { "IDC_Name" });
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 190 */       this.m_context.reportError(e, "");
/*     */     }
/* 192 */     this.m_collectionList.refreshList(rset, selObj);
/*     */ 
/* 194 */     checkSelection();
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 200 */     boolean isSelected = true;
/* 201 */     boolean isCurrentFlag = false;
/* 202 */     boolean isDefault = false;
/*     */ 
/* 204 */     int index = this.m_collectionList.getSelectedIndex();
/* 205 */     if (index < 0)
/*     */     {
/* 207 */       isSelected = false;
/*     */     }
/*     */     else
/*     */     {
/* 211 */       Properties props = this.m_collectionList.getDataAt(index);
/* 212 */       String idStr = props.getProperty("IDC_ID");
/* 213 */       int id = Integer.parseInt(idStr);
/* 214 */       if ((this.m_currentCollection != null) && (id == this.m_currentCollection.m_id))
/*     */       {
/* 218 */         isCurrentFlag = true;
/*     */       }
/*     */ 
/* 221 */       if (id == 0)
/*     */       {
/* 223 */         isDefault = true;
/*     */       }
/*     */     }
/*     */ 
/* 227 */     enableDisable(isSelected, isCurrentFlag, isDefault);
/*     */   }
/*     */ 
/*     */   protected void enableDisable(boolean isSelected, boolean isCurrentFlag, boolean isDefault)
/*     */   {
/* 232 */     int size = this.m_buttons.size();
/* 233 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 235 */       JButton btn = (JButton)this.m_buttons.elementAt(i);
/* 236 */       String cmd = btn.getActionCommand();
/* 237 */       if ((cmd.equals("cancel")) || (cmd.equals("help"))) continue; if (cmd.equals("browseProxied")) {
/*     */         continue;
/*     */       }
/*     */ 
/* 241 */       if ((cmd.equals("browseLocal")) || (cmd.equals("browseDataSource")))
/*     */       {
/* 243 */         if (AppLauncher.getIsStandAlone())
/*     */           continue;
/* 245 */         btn.setEnabled(false);
/*     */       }
/*     */       else
/*     */       {
/* 250 */         boolean enable = isSelected;
/* 251 */         if (isSelected == true)
/*     */         {
/* 253 */           if (cmd.equals("open"))
/*     */           {
/* 255 */             enable = !isCurrentFlag;
/*     */           }
/* 257 */           else if (cmd.equals("remove"))
/*     */           {
/* 259 */             enable = (!isDefault) && (!isCurrentFlag);
/*     */           }
/*     */         }
/* 262 */         btn.setEnabled(enable);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 271 */     String cmdStr = e.getActionCommand();
/* 272 */     if (cmdStr.equals("open"))
/*     */     {
/* 274 */       openCollection();
/*     */     }
/* 276 */     else if (cmdStr.equals("cancel"))
/*     */     {
/* 278 */       this.m_helper.m_result = 0;
/* 279 */       this.m_helper.close();
/*     */     }
/* 281 */     else if (cmdStr.equals("browseLocal"))
/*     */     {
/* 283 */       browseForLocalCollection();
/*     */     }
/* 285 */     else if (cmdStr.equals("browseDataSource"))
/*     */     {
/* 287 */       browseForDataSource();
/*     */     }
/* 289 */     else if (cmdStr.equals("browseProxied"))
/*     */     {
/* 291 */       browseForProxiedCollection();
/*     */     }
/* 293 */     else if (cmdStr.equals("remove"))
/*     */     {
/* 295 */       removeCollection();
/*     */     } else {
/* 297 */       if (!cmdStr.equals("help"))
/*     */         return;
/* 299 */       if (this.m_helpPage != null)
/*     */       {
/*     */         try
/*     */         {
/* 303 */           Help.display(this.m_helpPage, this.m_cxt);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 307 */           String error = LocaleUtils.encodeMessage("apErrorInLaunching", exp.getMessage());
/* 308 */           Report.trace(null, LocaleResources.localizeMessage(error, null), exp);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/* 313 */         this.m_context.reportError(LocaleResources.getString("apHelpPageNotFound", this.m_cxt, this.m_helpPage));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void openCollection()
/*     */   {
/* 320 */     int index = this.m_collectionList.getSelectedIndex();
/* 321 */     if (index < 0)
/*     */     {
/* 323 */       return;
/*     */     }
/*     */ 
/* 326 */     this.m_selectedCollection = this.m_collectionList.getDataAt(index);
/*     */ 
/* 328 */     this.m_helper.m_result = 1;
/* 329 */     this.m_helper.close();
/*     */   }
/*     */ 
/*     */   protected void browseForLocalCollection()
/*     */   {
/* 359 */     IdcFileChooser fileDlg = new IdcFileChooser();
/* 360 */     fileDlg.setDialogTitle(LocaleResources.getString("apLabelFindArchiveCollDefFile", this.m_cxt));
/* 361 */     fileDlg.setFileFilter(new CollectionHdaFileFilter());
/* 362 */     fileDlg.showOpenDialog(null);
/*     */ 
/* 364 */     boolean isCreate = false;
/* 365 */     File colFile = fileDlg.getSelectedFile();
/* 366 */     if (colFile == null)
/*     */     {
/* 368 */       return;
/*     */     }
/*     */ 
/* 371 */     String filePath = colFile.getAbsolutePath();
/* 372 */     if (!colFile.exists())
/*     */     {
/* 374 */       int result = MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apNonexistantCollectionDefinitionFile", new Object[] { filePath }), 4);
/*     */ 
/* 377 */       if (result == 3)
/*     */       {
/* 379 */         return;
/*     */       }
/* 381 */       isCreate = true;
/*     */     }
/*     */ 
/* 384 */     String idcName = null;
/* 385 */     if (!isCreate)
/*     */     {
/* 388 */       DataBinder data = new DataBinder();
/*     */       try
/*     */       {
/* 391 */         data = ResourceUtils.readDataBinderFromPath(filePath);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 395 */         this.m_context.reportError(e, LocaleResources.getString("apCannotLoadCollectionInfoFromFile", this.m_cxt, filePath));
/*     */ 
/* 397 */         return;
/*     */       }
/*     */ 
/* 400 */       idcName = data.getLocal("IDC_Name");
/* 401 */       if (idcName == null)
/*     */       {
/* 404 */         this.m_context.reportError(LocaleResources.getString("apUnnamedCollection", this.m_cxt));
/* 405 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 410 */     CollectionDlg dlg = new CollectionDlg(this.m_systemInterface, LocaleResources.getString("apTitleBrowseToArchiverCollection", this.m_cxt));
/*     */ 
/* 412 */     if (dlg.init(idcName, filePath, true) != 1) {
/*     */       return;
/*     */     }
/* 415 */     refreshCollections(dlg.getIdcName());
/*     */   }
/*     */ 
/*     */   protected void browseForDataSource()
/*     */   {
/* 421 */     DataSourceCollectionDlg dlg = new DataSourceCollectionDlg(this.m_systemInterface, LocaleResources.getString("apLabelFindArchiveCollDefFileForDataSource", this.m_cxt), this.m_context);
/*     */ 
/* 423 */     if (dlg.init() != 1)
/*     */       return;
/* 425 */     refreshCollections(dlg.getIdcName());
/*     */   }
/*     */ 
/*     */   protected void browseForProxiedCollection()
/*     */   {
/* 431 */     ProxiedCollectionDlg dlg = new ProxiedCollectionDlg(this.m_systemInterface, LocaleResources.getString("apTitleBrowseForProxiedCollection", this.m_cxt), this.m_context);
/*     */ 
/* 434 */     if (dlg.init() != 1)
/*     */       return;
/* 436 */     refreshCollections(dlg.getIdcName());
/*     */   }
/*     */ 
/*     */   protected boolean removeCollection()
/*     */   {
/* 442 */     int index = this.m_collectionList.getSelectedIndex();
/* 443 */     if (index < 0)
/*     */     {
/* 445 */       return false;
/*     */     }
/*     */ 
/* 448 */     Properties props = this.m_collectionList.getDataAt(index);
/*     */ 
/* 450 */     String collName = props.getProperty("IDC_Name");
/* 451 */     int result = MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifyCollectionRemoval", new Object[] { collName }), 2);
/*     */ 
/* 455 */     if (result == 0)
/*     */     {
/* 457 */       return false;
/*     */     }
/*     */ 
/* 461 */     DataBinder binder = new DataBinder();
/* 462 */     binder.setLocalData(props);
/*     */     try
/*     */     {
/* 465 */       AppLauncher.executeService("REMOVE_COLLECTION", binder);
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 469 */       this.m_context.reportError(exp, "");
/* 470 */       return false;
/*     */     }
/*     */ 
/* 473 */     refreshCollections(null);
/* 474 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 479 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97779 $";
/*     */   }
/*     */ 
/*     */   public class CollectionHdaFileFilter extends FileFilter
/*     */   {
/*     */     public CollectionHdaFileFilter()
/*     */     {
/*     */     }
/*     */ 
/*     */     public boolean accept(File f)
/*     */     {
/* 337 */       if (f.isDirectory())
/*     */       {
/* 339 */         return true;
/*     */       }
/*     */ 
/* 344 */       return f.getName().equalsIgnoreCase("collection.hda");
/*     */     }
/*     */ 
/*     */     public String getDescription()
/*     */     {
/* 353 */       return LocaleResources.getString("apCollectionHdaFileFilterDescription", OpenCollectionDlg.this.m_cxt);
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.OpenCollectionDlg
 * JD-Core Version:    0.5.4
 */