/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.GuiUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedLoader;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Image;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DocTypesDialog
/*     */   implements ActionListener
/*     */ {
/*  61 */   protected DialogHelper m_helper = null;
/*  62 */   protected SystemInterface m_systemInterface = null;
/*  63 */   protected ExecutionContext m_ctx = null;
/*  64 */   protected String m_helpPage = null;
/*     */ 
/*  66 */   protected UdlPanel m_typesList = null;
/*  67 */   protected String m_gifDir = "";
/*  68 */   protected Hashtable m_gifCache = new Hashtable();
/*     */ 
/*     */   public DocTypesDialog(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  73 */     this.m_systemInterface = sys;
/*  74 */     this.m_ctx = sys.getExecutionContext();
/*  75 */     this.m_helper = new DialogHelper(sys, title, true);
/*  76 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public void init() throws ServiceException
/*     */   {
/*  81 */     this.m_gifDir = SharedLoader.getDocGifSubDirectory();
/*     */ 
/*  83 */     initUI();
/*  84 */     refreshList();
/*     */ 
/*  86 */     this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  91 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/*  92 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*  93 */     JPanel pnl = initPanels();
/*     */ 
/*  95 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*  96 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/*  97 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  98 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  99 */     this.m_helper.addComponent(mainPanel, pnl);
/*     */   }
/*     */ 
/*     */   protected JPanel initPanels()
/*     */   {
/* 104 */     this.m_typesList = new UdlPanel(LocaleResources.getString("apLabelTypes", this.m_ctx), null, 410, 18, "DocTypes", true);
/*     */ 
/* 106 */     this.m_typesList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleType", this.m_ctx), "dDocType", 2.0D));
/*     */ 
/* 108 */     this.m_typesList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleDescription", this.m_ctx), "dDescription", 3.0D));
/*     */ 
/* 110 */     this.m_typesList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelImage", this.m_ctx), "dGif", 1.0D));
/*     */ 
/* 113 */     this.m_typesList.setVisibleColumns("dDocType,dDescription,dGif");
/* 114 */     this.m_typesList.setIDColumn("dDocType");
/* 115 */     this.m_typesList.setIconColumn("dDocType");
/* 116 */     this.m_typesList.init();
/* 117 */     this.m_typesList.useDefaultListener();
/* 118 */     this.m_typesList.m_iconSize = new Dimension(32, 32);
/*     */ 
/* 120 */     String[][] btnInfo = { { "apDlgButtonAdd", "0", "add", "apTitleAddNewContentType" }, { "apDlgButtonEdit", "1", "edit", "apReadableButtonEditContentType" }, { "apLabelDelete", "1", "delete", "apReadableButtonDeleteContentType" }, { "apLabelClose", "0", "close", "apLabelClose" } };
/*     */ 
/* 127 */     int len = btnInfo.length;
/* 128 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 130 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][1], false);
/* 131 */       JButton btn = this.m_typesList.addButton(LocaleResources.getString(btnInfo[i][0], this.m_ctx), isControlled);
/* 132 */       btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(btnInfo[i][3], this.m_ctx));
/* 133 */       btn.setActionCommand(btnInfo[i][2]);
/* 134 */       this.m_helper.addCommandButtonEx(btn, this);
/*     */     }
/*     */ 
/* 137 */     this.m_helper.addHelpInfo(this.m_helpPage);
/*     */ 
/* 139 */     this.m_typesList.m_list.addActionListener(this);
/* 140 */     this.m_typesList.enableDisable(false);
/*     */ 
/* 143 */     JPanel wrapper = new PanePanel();
/* 144 */     this.m_helper.makePanelGridBag(wrapper, 1);
/* 145 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 10, 5, 10);
/* 146 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 147 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 148 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 149 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 150 */     this.m_helper.addComponent(wrapper, this.m_typesList);
/*     */ 
/* 152 */     return wrapper;
/*     */   }
/*     */ 
/*     */   public void refreshList() throws ServiceException
/*     */   {
/* 157 */     refreshList(null, null);
/*     */   }
/*     */ 
/*     */   public void refreshList(DataBinder binder, String selectedObj) throws ServiceException
/*     */   {
/* 162 */     if (binder == null)
/*     */     {
/* 166 */       binder = new DataBinder();
/* 167 */       AppLauncher.executeService("GET_DOCTYPES", binder);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 172 */       DataResultSet drset = (DataResultSet)binder.getResultSet("DocTypes");
/* 173 */       FieldInfo[] infos = ResultSetUtils.createInfoList(drset, new String[] { "dDocType", "dGif" }, true);
/*     */ 
/* 175 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 177 */         String dDocType = drset.getStringValue(infos[0].m_index);
/* 178 */         Image img = getImage(drset.getStringValue(infos[1].m_index));
/* 179 */         if (img == null)
/*     */           continue;
/* 181 */         this.m_typesList.setRowIcons(dDocType, new Image[] { img });
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 187 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 190 */     int selectedIndex = this.m_typesList.refreshList(binder, selectedObj);
/* 191 */     if (selectedIndex < 0)
/*     */     {
/* 194 */       this.m_helper.m_props = new Properties();
/* 195 */       this.m_helper.loadComponentValues();
/* 196 */       this.m_typesList.enableDisable(false);
/*     */     }
/*     */     else
/*     */     {
/* 200 */       this.m_typesList.enableDisable(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Image getImage(String gifName)
/*     */   {
/* 207 */     Image img = (Image)this.m_gifCache.get(gifName);
/* 208 */     if ((img == null) && (gifName != null) && (gifName.length() != 0))
/*     */     {
/* 210 */       img = GuiUtils.getAppImage(this.m_gifDir + gifName);
/*     */     }
/*     */ 
/* 213 */     return img;
/*     */   }
/*     */ 
/*     */   protected void reportError(IdcMessage msg)
/*     */   {
/* 218 */     MessageBox.reportError(this.m_systemInterface, msg);
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e)
/*     */   {
/* 223 */     MessageBox.reportError(this.m_systemInterface, e);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 231 */     Object obj = e.getSource();
/* 232 */     if (obj instanceof UserDrawList)
/*     */     {
/* 235 */       addOrEditType(false);
/*     */     }
/*     */     else
/*     */     {
/* 240 */       String cmd = e.getActionCommand();
/* 241 */       if (cmd.equals("add"))
/*     */       {
/* 243 */         addOrEditType(true);
/*     */       }
/* 245 */       else if (cmd.equals("edit"))
/*     */       {
/* 247 */         addOrEditType(false);
/*     */       }
/* 249 */       else if (cmd.equals("delete"))
/*     */       {
/* 251 */         deleteType();
/*     */       } else {
/* 253 */         if (!cmd.equals("close"))
/*     */           return;
/* 255 */         this.m_helper.close();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addOrEditType(boolean isAdd)
/*     */   {
/* 262 */     Properties props = null;
/* 263 */     String title = LocaleResources.getString("apTitleAddNewContentType", this.m_ctx);
/* 264 */     String helpPageName = "AddType";
/*     */ 
/* 266 */     if (!isAdd)
/*     */     {
/* 268 */       int index = this.m_typesList.getSelectedIndex();
/* 269 */       if (index < 0)
/*     */       {
/* 271 */         reportError(IdcMessageFactory.lc("apSelectTypeToEdit", new Object[0]));
/* 272 */         return;
/*     */       }
/* 274 */       props = this.m_typesList.getDataAt(index);
/* 275 */       title = LocaleResources.getString("apTitleEditContentType", this.m_ctx, props.getProperty("dDocType"));
/*     */ 
/* 277 */       helpPageName = "EditType";
/*     */     }
/*     */ 
/* 280 */     EditTypeDlg dlg = new EditTypeDlg(this.m_systemInterface, title, this.m_typesList.getResultSet(), DialogHelpTable.getHelpPage(helpPageName));
/*     */ 
/* 282 */     if (dlg.prompt(props) != 1)
/*     */       return;
/*     */     try
/*     */     {
/* 286 */       refreshList(dlg.getBinder(), dlg.getDocType());
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 290 */       reportError(exp);
/* 291 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteType()
/*     */   {
/* 298 */     int index = this.m_typesList.getSelectedIndex();
/* 299 */     if (index < 0)
/*     */     {
/* 302 */       reportError(IdcMessageFactory.lc("apSelectTypeToDelete", new Object[0]));
/* 303 */       return;
/*     */     }
/*     */ 
/* 306 */     Properties props = this.m_typesList.getDataAt(index);
/* 307 */     String name = props.getProperty("dDocType");
/*     */ 
/* 309 */     if (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifyTypeToDelete", new Object[] { name }), 4) != 2)
/*     */     {
/*     */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 316 */       DataBinder binder = new DataBinder();
/* 317 */       binder.setLocalData(props);
/* 318 */       AppLauncher.executeService("DELETE_DOCTYPE", binder);
/* 319 */       refreshList(binder, null);
/* 320 */       this.m_typesList.enableDisable(false);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 324 */       reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 331 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.DocTypesDialog
 * JD-Core Version:    0.5.4
 */