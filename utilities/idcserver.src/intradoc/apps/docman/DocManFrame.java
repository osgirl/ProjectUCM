/*     */ package intradoc.apps.docman;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.BasePanel;
/*     */ import intradoc.apps.shared.MainFrame;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.StatusBar;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.event.FocusListener;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JMenuBar;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DocManFrame extends MainFrame
/*     */   implements ReportProgress
/*     */ {
/*     */   protected CustomLabel m_indexStatus;
/*     */   String m_lastIndexerMsg;
/*     */   protected JMenu m_fMenu;
/*     */   protected final String[][] PANEL_INFOS;
/*     */   protected Vector m_panels;
/*     */ 
/*     */   public DocManFrame()
/*     */   {
/*  59 */     this.m_lastIndexerMsg = null;
/*     */ 
/*  65 */     this.PANEL_INFOS = new String[][] { { "DocManPanel", "intradoc.apps.docman.DocManPanel", "apContentTabLabel", "0" }, { "SubscriptionPanel", "intradoc.apps.docman.SubscriptionPanel", "apSubscriptionsTabLabel", "1" }, { "IndexerPanel", "intradoc.apps.docman.IndexerPanel", "apIndexerTabLabel", "1" } };
/*     */ 
/*  71 */     this.m_panels = new IdcVector();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void init(String title, boolean exitOnClose)
/*     */     throws ServiceException
/*     */   {
/*  80 */     IdcMessage msg = null;
/*  81 */     if (title != null)
/*     */     {
/*  83 */       msg = IdcMessageFactory.lc();
/*  84 */       msg.m_msgEncoded = title;
/*     */     }
/*  86 */     init(msg, exitOnClose);
/*     */   }
/*     */ 
/*     */   public void init(IdcMessage title, boolean exitOnClose)
/*     */     throws ServiceException
/*     */   {
/*  92 */     super.init(title, exitOnClose);
/*  93 */     this.m_appHelper.attachToAppFrame(this, null, null, title);
/*     */ 
/*  96 */     this.m_indexStatus = new CustomLabel();
/*  97 */     this.m_appHelper.m_statusBar.add("East", this.m_indexStatus);
/*     */ 
/* 100 */     buildMenu();
/* 101 */     boolean isAdmin = AppLauncher.isAdmin();
/* 102 */     JPanel pnl = initTabPanels(isAdmin);
/*     */ 
/* 104 */     JPanel mainPanel = this.m_appHelper.m_mainPanel;
/* 105 */     mainPanel.setLayout(new BorderLayout());
/* 106 */     mainPanel.add("Center", pnl);
/* 107 */     mainPanel.getAccessibleContext().setAccessibleName("mainpanel accessible context name");
/*     */ 
/* 109 */     pack();
/* 110 */     setVisible(true);
/*     */   }
/*     */ 
/*     */   protected JPanel initTabPanels(boolean isAdmin) throws ServiceException
/*     */   {
/* 115 */     TabPanel tab = new TabPanel();
/*     */ 
/* 117 */     int numPanels = this.PANEL_INFOS.length;
/* 118 */     BasePanel[] tabPanels = new BasePanel[numPanels];
/* 119 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 121 */       boolean requiresAdmin = StringUtils.convertToBool(this.PANEL_INFOS[i][3], false);
/* 122 */       if ((requiresAdmin == true) && (!isAdmin))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 127 */       ExecutionContext cxt = this.m_appHelper.getExecutionContext();
/* 128 */       tabPanels[i] = ((BasePanel)ComponentClassFactory.createClassInstance(this.PANEL_INFOS[i][0], this.PANEL_INFOS[i][1], LocaleResources.getString("apUnableToLoadTabPanel", cxt, this.PANEL_INFOS[i][0])));
/*     */ 
/* 132 */       tabPanels[i].init(this.m_appHelper, this.m_fMenu);
/* 133 */       tab.addPane(LocaleResources.getString(this.PANEL_INFOS[i][2], cxt), tabPanels[i], null, false, (FocusListener)tabPanels[i]);
/*     */ 
/* 136 */       this.m_panels.addElement(tabPanels[i]);
/*     */     }
/*     */ 
/* 139 */     return tab;
/*     */   }
/*     */ 
/*     */   protected void buildMenu()
/*     */   {
/* 144 */     JMenuBar mb = new JMenuBar();
/* 145 */     setJMenuBar(mb);
/*     */ 
/* 147 */     ExecutionContext cxt = this.m_appHelper.getExecutionContext();
/* 148 */     JMenu optMenu = new JMenu(LocaleResources.getString("apOptionsMenuLabel", cxt));
/*     */ 
/* 150 */     addStandardOptions(optMenu);
/*     */ 
/* 153 */     mb.add(optMenu);
/*     */ 
/* 156 */     this.m_fMenu = new JMenu(LocaleResources.getString("apFunctionsMenuLabel", cxt));
/* 157 */     mb.add(this.m_fMenu);
/*     */ 
/* 159 */     addAppMenu(mb);
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, float amtDone, float max)
/*     */   {
/* 167 */     ExecutionContext cxt = this.m_appHelper.getExecutionContext();
/*     */ 
/* 169 */     if (max >= 0.01D)
/*     */     {
/* 171 */       String aMsg = null;
/* 172 */       if (type == 0)
/*     */       {
/* 174 */         int m = (int)(max + 0.01D);
/* 175 */         int a = (int)(amtDone + 0.01D);
/* 176 */         aMsg = LocaleUtils.encodeMessage("apReportProgress1", null, "" + a, "" + m);
/*     */       }
/*     */       else
/*     */       {
/* 180 */         float perc = 100.0F * amtDone / max;
/* 181 */         aMsg = LocaleUtils.encodeMessage("apReportProgress2", null, "" + Math.round(perc));
/*     */       }
/*     */ 
/* 184 */       msg = msg + " " + aMsg;
/*     */     }
/* 186 */     if ((this.m_lastIndexerMsg != null) && (msg.equals(this.m_lastIndexerMsg)))
/*     */     {
/* 188 */       return;
/*     */     }
/* 190 */     this.m_lastIndexerMsg = msg;
/*     */ 
/* 192 */     if ((type == -1) && 
/* 194 */       (msg.length() > 50))
/*     */     {
/* 196 */       IdcMessage idcmsg = IdcMessageFactory.lc();
/* 197 */       idcmsg.m_msgEncoded = msg;
/* 198 */       MessageBox.reportError(this.m_appHelper, idcmsg);
/* 199 */       msg = "!apIndexerError";
/*     */     }
/*     */ 
/* 203 */     msg = LocaleResources.localizeMessage(msg, cxt);
/* 204 */     this.m_indexStatus.setText(msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 209 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83557 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.DocManFrame
 * JD-Core Version:    0.5.4
 */