/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.CardLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SelectTableWizard
/*     */   implements ActionListener
/*     */ {
/*  50 */   protected SystemInterface m_systemInterface = null;
/*  51 */   protected ExecutionContext m_context = null;
/*  52 */   protected DialogHelper m_helper = null;
/*  53 */   protected String m_helpPage = null;
/*     */ 
/*  56 */   protected String m_currentPanel = null;
/*  57 */   protected JPanel m_flipPanel = null;
/*  58 */   protected Hashtable m_flipComponents = null;
/*  59 */   protected SchemaTablePanel m_tablePanel = null;
/*     */ 
/*  61 */   protected JButton m_nextButton = null;
/*  62 */   protected JButton m_backButton = null;
/*  63 */   protected JButton m_finishButton = null;
/*     */ 
/*  65 */   protected static String[][] PANEL_INFO = { { "SelectTablePanel", "intradoc.apps.docconfig.SchemaTablePanel", "apSchSelectTablePanelTitle", "selectTable" }, { "SelectColumnsPanel", "intradoc.apps.docconfig.SelectColumnsPanel", "apSchSelectColumnsPanelTitle", "selectColumns" } };
/*     */ 
/*  71 */   protected static String[][] BUTTON_INFO = { { "back", "apLabelBack" }, { "next", "apLabelNext" }, { "finish", "apLabelFinish" } };
/*     */ 
/*  78 */   protected static String[][] FLOW_RULES = { { "selectTable", "selectColumns", "", "IsOptionListView", "IsOptionListView,IsTableView" }, { "selectColumns", "", "selectTable", "IsTableView", "IsTableView" } };
/*     */ 
/*     */   public SelectTableWizard(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  88 */     this.m_systemInterface = sys;
/*  89 */     this.m_context = sys.getExecutionContext();
/*  90 */     this.m_helper = new DialogHelper(sys, title, true);
/*  91 */     this.m_helpPage = helpPage;
/*  92 */     this.m_helper.m_props.put("IsTableView", "1");
/*     */   }
/*     */ 
/*     */   public int init(Properties props) throws ServiceException
/*     */   {
/*  97 */     return initEx(props, false);
/*     */   }
/*     */ 
/*     */   public int initEx(Properties props, boolean isColumnsOnly) throws ServiceException
/*     */   {
/* 102 */     this.m_helper.m_props = props;
/* 103 */     initUI(isColumnsOnly);
/*     */ 
/* 105 */     this.m_helper.m_props.put("IsTableView", "1");
/* 106 */     this.m_helper.m_props.put("schViewType", "table");
/* 107 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI(boolean isColumnsOnly) throws ServiceException
/*     */   {
/* 112 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 113 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 115 */     initPanels(mainPanel, isColumnsOnly);
/* 116 */     initControlButtons(isColumnsOnly);
/*     */   }
/*     */ 
/*     */   protected void initPanels(JPanel mainPanel, boolean isColumnsOnly) throws ServiceException
/*     */   {
/* 121 */     DataBinder binder = new DataBinder();
/* 122 */     binder.setLocalData(this.m_helper.m_props);
/*     */ 
/* 124 */     this.m_flipPanel = new PanePanel();
/* 125 */     this.m_flipComponents = new Hashtable();
/*     */ 
/* 127 */     CardLayout flipLayout = new CardLayout();
/* 128 */     this.m_flipPanel.setLayout(flipLayout);
/*     */ 
/* 130 */     int numPanels = PANEL_INFO.length;
/* 131 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 133 */       String name = PANEL_INFO[i][3];
/* 134 */       if ((isColumnsOnly) && (!name.equals("selectColumns")))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 140 */       DocConfigPanel pnl = (DocConfigPanel)ComponentClassFactory.createClassInstance(PANEL_INFO[i][0], PANEL_INFO[i][1], LocaleResources.getString("apUnableToLoadTabPanel", this.m_context, LocaleResources.getString(PANEL_INFO[i][2], this.m_context)));
/*     */ 
/* 144 */       addFlipComponent(name, binder, pnl);
/*     */ 
/* 146 */       if (((isColumnsOnly) && (name.equals("selectColumns"))) || ((!isColumnsOnly) && (i == 0)))
/*     */       {
/* 149 */         this.m_currentPanel = name;
/*     */         try
/*     */         {
/* 152 */           pnl.loadPanelInformation();
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 156 */           MessageBox.reportError(this.m_systemInterface, e);
/*     */         }
/*     */       }
/*     */ 
/* 160 */       if (!pnl instanceof SchemaTablePanel)
/*     */         continue;
/* 162 */       this.m_tablePanel = ((SchemaTablePanel)pnl);
/* 163 */       this.m_tablePanel.addActionListener(this);
/*     */     }
/*     */ 
/* 167 */     flipLayout.show(this.m_flipPanel, this.m_currentPanel);
/* 168 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 169 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 170 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 171 */     this.m_helper.addLastComponentInRow(mainPanel, this.m_flipPanel);
/*     */   }
/*     */ 
/*     */   protected void addFlipComponent(String id, DataBinder binder, DocConfigPanel dcp)
/*     */     throws ServiceException
/*     */   {
/* 177 */     dcp.initEx(this.m_systemInterface, binder);
/* 178 */     this.m_flipPanel.add(id, dcp);
/* 179 */     this.m_flipComponents.put(id, dcp);
/*     */   }
/*     */ 
/*     */   protected void initControlButtons(boolean isColumnsOnly)
/*     */   {
/* 184 */     if (isColumnsOnly)
/*     */     {
/* 186 */       DialogCallback okCallabck = new DialogCallback()
/*     */       {
/*     */         public boolean handleDialogEvent(ActionEvent e)
/*     */         {
/* 191 */           IdcMessage errMsg = SelectTableWizard.this.retrieveAndValidateInfo(false);
/* 192 */           if (errMsg != null)
/*     */           {
/* 194 */             MessageBox.reportError(SelectTableWizard.this.m_systemInterface, errMsg);
/* 195 */             return false;
/*     */           }
/* 197 */           return true;
/*     */         }
/*     */       };
/* 200 */       this.m_helper.addOK(okCallabck);
/*     */     }
/*     */     else
/*     */     {
/* 204 */       for (int i = 0; i < BUTTON_INFO.length; ++i)
/*     */       {
/* 206 */         String label = LocaleResources.getString(BUTTON_INFO[i][1], this.m_context);
/* 207 */         JButton btn = this.m_helper.addCommandButton(label, this);
/*     */ 
/* 209 */         String cmd = BUTTON_INFO[i][0];
/* 210 */         btn.setActionCommand(cmd);
/*     */ 
/* 212 */         if (cmd.equals("next"))
/*     */         {
/* 214 */           this.m_nextButton = btn;
/*     */         }
/* 216 */         else if (cmd.equals("back"))
/*     */         {
/* 218 */           this.m_backButton = btn;
/*     */         } else {
/* 220 */           if (!cmd.equals("finish"))
/*     */             continue;
/* 222 */           this.m_finishButton = btn;
/*     */         }
/*     */       }
/*     */ 
/* 226 */       this.m_nextButton.setEnabled(true);
/* 227 */       this.m_backButton.setEnabled(false);
/* 228 */       this.m_finishButton.setEnabled(false);
/*     */     }
/*     */ 
/* 231 */     this.m_helper.addCancel(null);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 239 */     Object source = e.getSource();
/* 240 */     String cmd = e.getActionCommand();
/* 241 */     if ((source == this.m_tablePanel) && (this.m_tablePanel != null))
/*     */     {
/* 243 */       cmd = "next";
/*     */     }
/*     */ 
/* 246 */     if ((cmd.equals("next")) || (cmd.equals("finish")))
/*     */     {
/* 249 */       boolean isFinish = cmd.equals("finish");
/* 250 */       IdcMessage errMsg = retrieveAndValidateInfo(isFinish);
/* 251 */       if (errMsg != null)
/*     */       {
/* 253 */         MessageBox.reportError(this.m_systemInterface, errMsg);
/* 254 */         return;
/*     */       }
/* 256 */       if (isFinish)
/*     */       {
/* 258 */         this.m_helper.m_result = 1;
/* 259 */         this.m_helper.close();
/* 260 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 265 */     String gotoPanel = null;
/* 266 */     String[] rule = getFlowRule(this.m_currentPanel);
/* 267 */     if (cmd.equals("next"))
/*     */     {
/* 269 */       gotoPanel = rule[1];
/*     */     }
/* 271 */     else if (cmd.equals("back"))
/*     */     {
/* 273 */       gotoPanel = rule[2];
/*     */     }
/*     */ 
/* 277 */     String nextPanel = "";
/* 278 */     String backPanel = "";
/* 279 */     boolean isFinish = false;
/* 280 */     String[] nextRule = getFlowRule(gotoPanel);
/* 281 */     if (nextRule != null)
/*     */     {
/* 283 */       backPanel = nextRule[2];
/* 284 */       isFinish = StringUtils.convertToBool(this.m_helper.m_props.getProperty(nextRule[3]), false);
/* 285 */       if (!isFinish)
/*     */       {
/* 287 */         nextPanel = nextRule[1];
/*     */       }
/*     */     }
/*     */ 
/* 291 */     DocConfigPanel dcp = (DocConfigPanel)this.m_flipComponents.get(gotoPanel);
/* 292 */     if (cmd.equals("next"))
/*     */     {
/*     */       try
/*     */       {
/* 296 */         dcp.loadPanelInformation();
/*     */       }
/*     */       catch (DataException ex)
/*     */       {
/* 300 */         MessageBox.reportError(this.m_systemInterface, ex);
/* 301 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 306 */     CardLayout layout = (CardLayout)this.m_flipPanel.getLayout();
/* 307 */     layout.show(this.m_flipPanel, gotoPanel);
/* 308 */     this.m_currentPanel = gotoPanel;
/*     */ 
/* 311 */     this.m_finishButton.setEnabled(isFinish);
/* 312 */     boolean isEnabled = false;
/* 313 */     if (nextPanel.length() > 0)
/*     */     {
/* 315 */       isEnabled = true;
/*     */     }
/* 317 */     this.m_nextButton.setEnabled(isEnabled);
/*     */ 
/* 319 */     isEnabled = false;
/* 320 */     if (backPanel.length() > 0)
/*     */     {
/* 322 */       isEnabled = true;
/*     */     }
/* 324 */     this.m_backButton.setEnabled(isEnabled);
/*     */   }
/*     */ 
/*     */   protected String[] getFlowRule(String name)
/*     */   {
/* 329 */     int length = FLOW_RULES.length;
/* 330 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 332 */       if (name.equals(FLOW_RULES[i][0]))
/*     */       {
/* 334 */         return FLOW_RULES[i];
/*     */       }
/*     */     }
/* 337 */     return null;
/*     */   }
/*     */ 
/*     */   protected IdcMessage retrieveAndValidateInfo(boolean isFinish)
/*     */   {
/* 342 */     IdcMessage errMsg = null;
/* 343 */     if (isFinish)
/*     */     {
/* 345 */       for (int i = 0; i < FLOW_RULES.length; ++i)
/*     */       {
/* 347 */         String name = FLOW_RULES[i][0];
/* 348 */         Vector cnds = StringUtils.parseArray(FLOW_RULES[i][4], ',', '^');
/* 349 */         boolean isCheck = false;
/* 350 */         int size = cnds.size();
/* 351 */         for (int j = 0; j < size; ++j)
/*     */         {
/* 353 */           String cnd = (String)cnds.elementAt(j);
/* 354 */           isCheck = StringUtils.convertToBool(this.m_helper.m_props.getProperty(cnd), false);
/* 355 */           if (isCheck) {
/*     */             break;
/*     */           }
/*     */         }
/*     */ 
/* 360 */         if (!isCheck)
/*     */           continue;
/* 362 */         DocConfigPanel dcp = (DocConfigPanel)this.m_flipComponents.get(name);
/* 363 */         errMsg = dcp.retrievePanelValuesAndValidate();
/* 364 */         if (errMsg != null) {
/*     */           break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 373 */       DocConfigPanel dcp = (DocConfigPanel)this.m_flipComponents.get(this.m_currentPanel);
/* 374 */       errMsg = dcp.retrievePanelValuesAndValidate();
/*     */     }
/*     */ 
/* 377 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 382 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.SelectTableWizard
 * JD-Core Version:    0.5.4
 */