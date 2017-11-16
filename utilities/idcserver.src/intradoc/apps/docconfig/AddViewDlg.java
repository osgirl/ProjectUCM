/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddViewDlg
/*     */   implements ActionListener, DocConfigContext
/*     */ {
/*  54 */   protected SystemInterface m_systemInterface = null;
/*  55 */   protected ExecutionContext m_context = null;
/*  56 */   protected DialogHelper m_helper = null;
/*  57 */   protected String m_helpPage = null;
/*     */ 
/*  59 */   protected String m_action = null;
/*  60 */   protected DataBinder m_binder = null;
/*     */ 
/*  63 */   protected String m_viewType = null;
/*  64 */   protected TabPanel m_tabPanel = null;
/*  65 */   protected DocConfigPanel[] m_panels = null;
/*     */ 
/*  67 */   protected static String[][] PANEL_INFO = { { "ViewInfoPanel", "intradoc.apps.docconfig.ViewInfoPanel", "apSchViewInfoPanelTitle" }, { "ViewDisplayPanel", "intradoc.apps.docconfig.ViewDisplayPanel", "apSchViewDisplayPanelTitle" }, { "ViewOptionsPanel", "intradoc.apps.docconfig.ViewOptionsPanel", "apSchViewOptionsPanelTitle" }, { "ViewSecurityPanel", "intradoc.apps.docconfig.ViewSecurityPanel", "apSchViewSecurityPanelTitle" } };
/*     */ 
/*     */   public AddViewDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  77 */     this.m_systemInterface = sys;
/*  78 */     this.m_context = sys.getExecutionContext();
/*  79 */     this.m_helper = new DialogHelper(sys, title, true);
/*  80 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(Properties props, SchemaViewConfig viewList)
/*     */   {
/*  85 */     this.m_helper.m_props = props;
/*     */ 
/*  87 */     this.m_binder = retrieveInfo(viewList);
/*  88 */     if (this.m_binder == null)
/*     */     {
/*  90 */       return 0;
/*     */     }
/*     */ 
/*  94 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  99 */         IdcVector errorBuffer = new IdcVector();
/* 100 */         for (int i = 0; i < AddViewDlg.this.m_panels.length; ++i)
/*     */         {
/* 102 */           IdcMessage message = AddViewDlg.this.m_panels[i].retrievePanelValuesAndValidate();
/* 103 */           if (message == null)
/*     */             continue;
/* 105 */           errorBuffer.add(message);
/* 106 */           if (errorBuffer.size() != 0)
/*     */             continue;
/* 108 */           String panelName = LocaleResources.getString(AddViewDlg.PANEL_INFO[i][2], AddViewDlg.this.m_context);
/*     */ 
/* 110 */           AddViewDlg.this.m_tabPanel.selectPane(panelName);
/*     */         }
/*     */ 
/* 114 */         if (errorBuffer.size() > 0)
/*     */         {
/* 116 */           IdcMessage message = null;
/* 117 */           for (IdcMessage msg : errorBuffer)
/*     */           {
/* 119 */             if (message == null)
/*     */             {
/* 121 */               message = msg;
/*     */             }
/*     */             else
/*     */             {
/* 125 */               message.setPrior(msg);
/* 126 */               message = msg;
/*     */             }
/*     */           }
/* 129 */           MessageBox.reportError(AddViewDlg.this.m_systemInterface, message);
/* 130 */           return false;
/*     */         }
/*     */ 
/*     */         try
/*     */         {
/* 136 */           AppLauncher.executeService(AddViewDlg.this.m_action, AddViewDlg.this.m_binder);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 140 */           MessageBox.reportError(AddViewDlg.this.m_systemInterface, exp);
/* 141 */           return false;
/*     */         }
/* 143 */         return true;
/*     */       }
/*     */     };
/* 146 */     okCallback.m_dlgHelper = this.m_helper;
/*     */ 
/* 148 */     this.m_viewType = this.m_helper.m_props.getProperty("schViewType");
/* 149 */     if (this.m_viewType == null)
/*     */     {
/* 151 */       this.m_viewType = "table";
/*     */     }
/*     */     try
/*     */     {
/* 155 */       initUI(okCallback, this.m_binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 159 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */ 
/* 162 */     loadInformation();
/* 163 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI(DialogCallback okCallback, DataBinder binder) throws ServiceException
/*     */   {
/* 168 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 171 */     JPanel pnl = initPanels(binder);
/*     */ 
/* 173 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 174 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 175 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 176 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 177 */     this.m_helper.addComponent(mainPanel, pnl);
/*     */   }
/*     */ 
/*     */   protected JPanel initPanels(DataBinder binder) throws ServiceException
/*     */   {
/* 182 */     this.m_tabPanel = new TabPanel();
/*     */ 
/* 184 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 185 */     mainPanel.setLayout(new BorderLayout());
/* 186 */     mainPanel.add("Center", this.m_tabPanel);
/*     */ 
/* 188 */     int numPanels = 1;
/* 189 */     if (this.m_viewType.equals("table"))
/*     */     {
/* 191 */       numPanels = PANEL_INFO.length;
/*     */     }
/* 193 */     this.m_panels = new DocConfigPanel[numPanels];
/*     */ 
/* 195 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 197 */       this.m_panels[i] = ((DocConfigPanel)ComponentClassFactory.createClassInstance(PANEL_INFO[i][0], PANEL_INFO[i][1], LocaleResources.getString("apUnableToLoadTabPanel", this.m_context, LocaleResources.getString(PANEL_INFO[i][2], this.m_context))));
/*     */ 
/* 201 */       this.m_panels[i].setDocContext(this);
/* 202 */       this.m_panels[i].initEx(this.m_systemInterface, binder);
/* 203 */       this.m_tabPanel.addPane(LocaleResources.getString(PANEL_INFO[i][2], this.m_context), this.m_panels[i]);
/*     */     }
/*     */ 
/* 206 */     return this.m_tabPanel;
/*     */   }
/*     */ 
/*     */   protected DataBinder retrieveInfo(SchemaViewConfig viewList)
/*     */   {
/* 211 */     DataBinder binder = new DataBinder();
/* 212 */     binder.setLocalData(this.m_helper.m_props);
/* 213 */     boolean isNew = StringUtils.convertToBool(this.m_helper.m_props.getProperty("IsNew"), false);
/* 214 */     if (!isNew)
/*     */     {
/* 216 */       this.m_action = "EDIT_SCHEMA_VIEW";
/*     */     }
/*     */     else
/*     */     {
/* 220 */       this.m_action = "ADD_SCHEMA_VIEW";
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 226 */       AppLauncher.executeService("GET_SCHEMA_VIEW_INFO", binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 230 */       Report.trace("schema", null, e);
/* 231 */       MessageBox.reportError(this.m_systemInterface, e);
/* 232 */       return null;
/*     */     }
/*     */ 
/* 236 */     String viewName = this.m_helper.m_props.getProperty("schViewName");
/* 237 */     Vector optList = new IdcVector();
/*     */ 
/* 239 */     for (viewList.first(); viewList.isRowPresent(); viewList.next())
/*     */     {
/* 241 */       SchemaViewData data = (SchemaViewData)viewList.getData();
/* 242 */       if (data == null)
/*     */       {
/* 244 */         Report.trace("schema", "***** data missing for view '" + viewList.getStringValue(0) + "'", null);
/*     */       }
/*     */       else
/*     */       {
/* 248 */         String name = data.get("schViewName");
/* 249 */         if ((viewName != null) && (viewName.equalsIgnoreCase(name))) {
/*     */           continue;
/*     */         }
/* 252 */         optList.addElement(name);
/*     */       }
/*     */     }
/* 255 */     binder.addOptionList("ViewList", optList);
/*     */ 
/* 257 */     return binder;
/*     */   }
/*     */ 
/*     */   protected void loadInformation()
/*     */   {
/* 262 */     int numPanels = this.m_panels.length;
/* 263 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 265 */       this.m_panels[i].loadComponents();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void changeColumns()
/*     */   {
/* 283 */     for (int i = 0; i < this.m_panels.length; ++i)
/*     */     {
/* 285 */       this.m_panels[i].retrievePanelValuesAndValidate();
/*     */     }
/*     */ 
/* 289 */     Properties props = new Properties();
/* 290 */     props.put("schTableName", this.m_helper.m_props.getProperty("schTableName"));
/* 291 */     props.put("schViewColumns", this.m_helper.m_props.getProperty("schViewColumns"));
/* 292 */     props.put("schViewType", this.m_helper.m_props.getProperty("schViewType"));
/*     */ 
/* 294 */     String title = LocaleResources.getString("apSchTitleSelectColumns", this.m_context);
/* 295 */     String helpPage = DialogHelpTable.getHelpPage("SchSelectColumns");
/* 296 */     SelectTableWizard dlg = new SelectTableWizard(this.m_systemInterface, title, helpPage);
/*     */     try
/*     */     {
/* 299 */       int result = dlg.initEx(props, true);
/* 300 */       if (result == 1)
/*     */       {
/* 302 */         String columns = props.getProperty("schViewColumns");
/* 303 */         this.m_helper.m_props.put("schViewColumns", columns);
/* 304 */         notifyChanged();
/*     */       }
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 309 */       MessageBox.reportError(this.m_systemInterface, exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateColumnList(DisplayChoice clmnList, boolean hasEmptyFirstRow, boolean addLocalizationChoices)
/*     */   {
/* 317 */     clmnList.removeAllItems();
/* 318 */     if (hasEmptyFirstRow)
/*     */     {
/* 320 */       clmnList.addItem("");
/*     */     }
/*     */ 
/* 324 */     String str = this.m_helper.m_props.getProperty("schViewColumns");
/* 325 */     Vector columns = StringUtils.parseArray(str, ',', '^');
/* 326 */     clmnList.init(columns);
/*     */ 
/* 328 */     if (!addLocalizationChoices)
/*     */       return;
/* 330 */     clmnList.addItem("Default display expression", "defaultDisplay");
/* 331 */     clmnList.addItem("Localized display expression", "localizedDisplay");
/* 332 */     clmnList.setEnabled(true);
/*     */   }
/*     */ 
/*     */   public void notifyChanged()
/*     */   {
/* 338 */     for (int i = 0; i < this.m_panels.length; ++i)
/*     */     {
/*     */       try
/*     */       {
/* 342 */         this.m_panels[i].refreshView();
/* 343 */         this.m_panels[i].loadComponents();
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 348 */         e.printStackTrace();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 355 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.AddViewDlg
 * JD-Core Version:    0.5.4
 */