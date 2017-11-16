/*     */ package intradoc.apps.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.GuiUtils;
/*     */ import intradoc.common.Help;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.VersionInfo;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.GuiStyles;
/*     */ import intradoc.gui.ImageLabel;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Image;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JMenuBar;
/*     */ import javax.swing.JMenuItem;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class MainFrame extends JFrame
/*     */   implements Observer
/*     */ {
/*  60 */   protected String m_name = "";
/*     */ 
/*  62 */   public AppFrameHelper m_appHelper = null;
/*  63 */   public ExecutionContext m_cxt = null;
/*  64 */   protected MainFrameListener m_listener = null;
/*     */ 
/*     */   public MainFrame()
/*     */   {
/*  68 */     this.m_appHelper = new AppFrameHelper();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void init(String title, boolean exitOnClose)
/*     */     throws ServiceException
/*     */   {
/*  75 */     IdcMessage msg = null;
/*  76 */     if (title != null)
/*     */     {
/*  78 */       msg = IdcMessageFactory.lc();
/*  79 */       msg.m_msgEncoded = title;
/*     */     }
/*  81 */     init(msg, exitOnClose);
/*     */   }
/*     */ 
/*     */   public void init(IdcMessage title, boolean exitOnClose) throws ServiceException
/*     */   {
/*  86 */     this.m_cxt = this.m_appHelper.m_cxt;
/*  87 */     String label = SharedObjects.getEnvironmentValue("InstanceMenuLabel");
/*     */ 
/*  89 */     String msg = LocaleResources.getString("apUnableToLoadMainFrameListener", this.m_cxt);
/*  90 */     this.m_listener = ((MainFrameListener)ComponentClassFactory.createClassInstance("MainFrameListener", "intradoc.apps.shared.MainFrameListener", msg));
/*     */ 
/*  93 */     this.m_listener.init(this);
/*     */     IdcMessage tmp;
/*     */     IdcMessage tmp;
/*  96 */     if (label != null)
/*     */     {
/*  98 */       tmp = IdcMessageFactory.lc("sySysSpecifier", new Object[] { title, label });
/*     */     }
/*     */     else
/*     */     {
/* 102 */       tmp = title;
/*     */     }
/*     */ 
/* 105 */     String titleText = LocaleResources.localizeMessage(null, tmp, this.m_appHelper.m_cxt).toString();
/* 106 */     setTitle(titleText);
/* 107 */     this.m_appHelper.m_exitOnClose = exitOnClose;
/*     */   }
/*     */ 
/*     */   public void setName(String name)
/*     */   {
/* 113 */     this.m_name = name;
/*     */   }
/*     */ 
/*     */   public void addStandardOptions(JMenu optMenu)
/*     */   {
/* 118 */     JMenuItem traceItem = new JMenuItem(LocaleResources.getString("apTracingMenuLabel", this.m_cxt));
/*     */ 
/* 120 */     traceItem.addActionListener(this.m_listener);
/* 121 */     traceItem.setActionCommand("tracing");
/* 122 */     optMenu.add(traceItem);
/*     */ 
/* 124 */     JMenuItem exitItem = new JMenuItem(LocaleResources.getString("apExitMenuItemLabel", this.m_cxt));
/*     */ 
/* 126 */     exitItem.addActionListener(this.m_listener);
/* 127 */     exitItem.setActionCommand("exit");
/* 128 */     optMenu.add(exitItem);
/*     */   }
/*     */ 
/*     */   public void addAppMenu(JMenuBar mb)
/*     */   {
/* 133 */     Vector appInfo = AppLauncher.getAppsForUser();
/* 134 */     int size = appInfo.size();
/* 135 */     if (size > 0)
/*     */     {
/* 137 */       JMenu appMenu = new JMenu(LocaleResources.getString("apTitleApps", this.m_cxt));
/* 138 */       mb.add(appMenu);
/*     */ 
/* 140 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 142 */         AppInfo info = (AppInfo)appInfo.elementAt(i);
/* 143 */         JMenuItem mi = new JMenuItem(LocaleResources.getString(info.m_title, this.m_cxt));
/* 144 */         mi.addActionListener(this.m_listener);
/* 145 */         mi.setActionCommand("launch-" + info.m_appName);
/* 146 */         mi.setEnabled(!this.m_name.equals(info.m_appName));
/* 147 */         appMenu.add(mi);
/*     */       }
/*     */     }
/*     */ 
/* 151 */     addHelpMenu(mb);
/*     */   }
/*     */ 
/*     */   public void displayHelp(String page)
/*     */   {
/*     */     try
/*     */     {
/* 161 */       Help.display(page, this.m_cxt);
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 165 */       reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addHelpMenu(JMenuBar mb)
/*     */   {
/* 171 */     JMenu helpMenu = new JMenu(LocaleResources.getString("apTitleHelp", this.m_cxt));
/* 172 */     mb.add(helpMenu);
/*     */ 
/* 176 */     JMenuItem mi1 = new JMenuItem(LocaleResources.getString("apTitleContents", this.m_cxt));
/* 177 */     mi1.addActionListener(this.m_listener);
/* 178 */     mi1.setActionCommand("contents");
/* 179 */     helpMenu.add(mi1);
/*     */ 
/* 182 */     JMenuItem mi2 = new JMenuItem(LocaleResources.getString("apTitleAboutContentServer", this.m_cxt));
/* 183 */     mi2.addActionListener(this.m_listener);
/* 184 */     mi2.setActionCommand("about");
/* 185 */     helpMenu.add(mi2);
/*     */   }
/*     */ 
/*     */   public void dispose()
/*     */   {
/* 191 */     super.dispose();
/* 192 */     AppLauncher.removeApp(this.m_name);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(String msg)
/*     */   {
/* 199 */     MessageBox.reportError(this.m_appHelper, msg);
/*     */   }
/*     */ 
/*     */   public void reportError(Exception e)
/*     */   {
/* 204 */     MessageBox.reportError(this.m_appHelper, e);
/*     */   }
/*     */ 
/*     */   public void reportError(Exception e, IdcMessage msg)
/*     */   {
/* 209 */     MessageBox.reportError(this.m_appHelper, e, msg);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(Exception e, String msg)
/*     */   {
/* 216 */     MessageBox.reportError(this.m_appHelper, e, msg);
/*     */   }
/*     */ 
/*     */   public void update(Observable obs, Object arg)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void displayAboutInfo()
/*     */   {
/* 229 */     DialogHelper dlgHelper = new DialogHelper(this.m_appHelper, LocaleResources.getString("apTitleAbout", this.m_cxt), true);
/*     */ 
/* 233 */     ImageLabel gifLabel = new ImageLabel(495, 59, true);
/* 234 */     JPanel gifPanel = new CustomPanel();
/* 235 */     gifPanel.setLayout(new BorderLayout());
/* 236 */     CustomLabel coLabel = new CustomLabel(LocaleResources.getString("apTitleWebContentManager", this.m_cxt));
/* 237 */     GuiStyles.setCustomStyle(coLabel, 4);
/* 238 */     coLabel.setHorizontalAlignment(2);
/* 239 */     gifPanel.add("North", gifLabel);
/* 240 */     gifPanel.add("South", coLabel);
/* 241 */     JPanel mainPanel = dlgHelper.m_mainPanel;
/* 242 */     dlgHelper.makePanelGridBag(mainPanel, 2);
/* 243 */     GridBagHelper gridBag = dlgHelper.m_gridHelper;
/*     */ 
/* 246 */     dlgHelper.addOK(null);
/* 247 */     gridBag.prepareAddLastRowElement();
/* 248 */     dlgHelper.addComponent(mainPanel, gifPanel);
/*     */ 
/* 250 */     Image logo = GuiUtils.getAppImage(LocaleResources.getString("apLogoGifName", this.m_cxt));
/* 251 */     gifLabel.setImage(logo);
/*     */ 
/* 254 */     CustomText infoText = new CustomText(LocaleResources.getString(VersionInfo.getProductCopyright(), this.m_cxt) + "\n \n" + LocaleResources.getString(VersionInfo.getProductThirdParty(), this.m_cxt) + "\n \n" + LocaleResources.getString("apContentServerAppletVersion", this.m_cxt, VersionInfo.getProductVersion(), VersionInfo.getProductVersionInfo()) + "\n" + LocaleResources.getString("apContentServerVersion", this.m_cxt, SharedObjects.getEnvironmentValue("IdcProductVersion"), SharedObjects.getEnvironmentValue("IdcProductVersionInfo")) + "\n", 70, 0);
/*     */ 
/* 265 */     gridBag.prepareAddLastRowElement();
/* 266 */     dlgHelper.addComponent(mainPanel, infoText);
/*     */ 
/* 268 */     dlgHelper.show();
/*     */   }
/*     */ 
/*     */   public void displayTracingConfig() throws ServiceException
/*     */   {
/* 273 */     String msg = LocaleResources.getString("apUnableToLoadTracingConfig", this.m_cxt);
/* 274 */     TracingConfigUI traceConfig = (TracingConfigUI)ComponentClassFactory.createClassInstance("TracingConfigUI", "intradoc.apps.shared.TracingConfigUI", msg);
/*     */ 
/* 278 */     traceConfig.init(this.m_appHelper);
/* 279 */     traceConfig.displayTracingConfig();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 284 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92636 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.MainFrame
 * JD-Core Version:    0.5.4
 */