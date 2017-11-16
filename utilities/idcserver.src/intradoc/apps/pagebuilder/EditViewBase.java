/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.Browser;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.CommonDialogs;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.GuiStyles;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocumentPathBuilder;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.gui.SecurityEditHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditViewBase extends CustomPanel
/*     */   implements EditContext, ComponentBinder
/*     */ {
/*     */   EditPageHelper m_editPageHelper;
/*     */   ContainerHelper m_containerHelper;
/*     */   protected PageData m_data;
/*     */   protected DataBinder m_binder;
/*     */   protected PageManagerContext m_pageContext;
/*     */   public String m_curChildPage;
/*     */   public boolean m_isEditAllowed;
/*     */   public boolean m_isErrorPage;
/*     */   public JButton m_editProps;
/*  95 */   protected ExecutionContext m_ctx = null;
/*     */ 
/*     */   public EditViewBase()
/*     */   {
/* 103 */     this.m_containerHelper = null;
/* 104 */     this.m_data = null;
/* 105 */     this.m_binder = null;
/* 106 */     this.m_pageContext = null;
/* 107 */     this.m_curChildPage = null;
/* 108 */     this.m_isErrorPage = false;
/* 109 */     this.m_isEditAllowed = false;
/* 110 */     this.m_editProps = null;
/*     */   }
/*     */ 
/*     */   public void setPageContext(PageManagerContext context)
/*     */   {
/* 115 */     this.m_pageContext = context;
/*     */   }
/*     */ 
/*     */   public void createStandardHeader()
/*     */   {
/* 123 */     setLayout(new BorderLayout());
/*     */ 
/* 125 */     JPanel top = new PanePanel(false);
/*     */ 
/* 127 */     GridBagHelper gh = this.m_containerHelper.m_gridHelper;
/* 128 */     gh.useGridBag(top);
/* 129 */     this.m_containerHelper.addLabelDisplayPairEx(top, LocaleResources.getString("apLabelName", this.m_ctx), 150, "PageName", false);
/*     */ 
/* 131 */     this.m_containerHelper.addLabelDisplayPairEx(top, LocaleResources.getString("apLabelParent", this.m_ctx), 150, "PageParent", this.m_isErrorPage);
/*     */ 
/* 134 */     if (!this.m_isErrorPage)
/*     */     {
/* 136 */       JButton showURL = new JButton(LocaleResources.getString("apDlgButtonShow", this.m_ctx));
/* 137 */       showURL.addActionListener(new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/* 141 */           String urlPath = null;
/*     */           try
/*     */           {
/* 144 */             urlPath = EditViewBase.this.m_binder.getLocal("PageUrl");
/* 145 */             if (urlPath == null)
/*     */             {
/* 147 */               EditViewBase.this.reportError(null, "!apPageHasNoUrlAvailable");
/*     */             }
/* 149 */             Browser.showDocument(urlPath);
/*     */           }
/*     */           catch (Exception excep)
/*     */           {
/* 153 */             if (urlPath != null)
/*     */             {
/* 155 */               urlPath = Browser.computeFullUrlString(DocumentPathBuilder.getBaseAbsoluteRoot(), urlPath);
/*     */ 
/* 158 */               CommonDialogs.showUrlMessage(EditViewBase.this.m_containerHelper.m_exchange.m_sysInterface, LocaleResources.getString("apLabelBrowserNotAccessible", EditViewBase.this.m_ctx), urlPath, LocaleResources.localizeMessage(LocaleUtils.encodeMessage("apUnableToViewUrl", excep.getMessage()), EditViewBase.this.m_ctx));
/*     */             }
/*     */             else
/*     */             {
/* 166 */               EditViewBase.this.reportError(excep, "!apUnableToViewInBrowser");
/*     */             }
/*     */           }
/*     */         }
/*     */       });
/* 171 */       this.m_containerHelper.m_gridHelper.m_gc.anchor = 13;
/* 172 */       this.m_containerHelper.addLastComponentInRow(top, showURL);
/*     */     }
/*     */ 
/* 175 */     add("North", top);
/*     */   }
/*     */ 
/*     */   public JPanel createStandardPageEdit()
/*     */   {
/* 184 */     JPanel propsPanel = new CustomPanel();
/*     */ 
/* 186 */     JPanel propsAllItems = new PanePanel(false);
/* 187 */     JPanel propsButtons = new PanePanel(false);
/*     */ 
/* 190 */     GridBagHelper gh = this.m_containerHelper.m_gridHelper;
/* 191 */     gh.useGridBag(propsAllItems);
/* 192 */     this.m_containerHelper.addPanelTitle(propsAllItems, LocaleResources.getString("apLabelPageProperties", this.m_ctx));
/*     */ 
/* 194 */     gh.m_gc.insets = new Insets(0, 3, 0, 3);
/*     */     try
/*     */     {
/* 197 */       this.m_editPageHelper.addPageProperties(propsAllItems, this.m_containerHelper, true);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 202 */       this.m_containerHelper.addLastComponentInRow(propsAllItems, new CustomText(e.getMessage()));
/*     */     }
/*     */ 
/* 205 */     gh.useGridBag(propsButtons);
/*     */ 
/* 207 */     this.m_editProps = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_ctx));
/* 208 */     this.m_editProps.getAccessibleContext().setAccessibleName(LocaleResources.getString("apLabelEditPageProperties", this.m_ctx));
/* 209 */     propsButtons.add(this.m_editProps);
/*     */ 
/* 211 */     this.m_editProps.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 217 */           EditViewBase.this.editPageProperties();
/*     */         }
/*     */         catch (ServiceException err)
/*     */         {
/* 221 */           EditViewBase.this.reportError(err, "!apUnableToEditPage");
/*     */         }
/*     */       }
/*     */     });
/* 226 */     propsPanel.setLayout(new BorderLayout());
/* 227 */     propsPanel.add("Center", propsAllItems);
/* 228 */     propsPanel.add("East", propsButtons);
/*     */ 
/* 230 */     JPanel editPanel = new PanePanel(false);
/* 231 */     JPanel contentPanel = new CustomPanel();
/* 232 */     editPanel.setLayout(new BorderLayout());
/* 233 */     editPanel.add("North", propsPanel);
/* 234 */     editPanel.add("Center", contentPanel);
/*     */ 
/* 236 */     add("Center", editPanel);
/* 237 */     return contentPanel;
/*     */   }
/*     */ 
/*     */   public void editPageProperties() throws ServiceException
/*     */   {
/* 242 */     String secGroup = this.m_data.m_binder.getLocal("dSecurityGroup");
/* 243 */     String account = this.m_data.m_binder.getLocal("dDocAccount");
/* 244 */     if (secGroup == null)
/*     */     {
/* 246 */       secGroup = "Public";
/*     */     }
/* 248 */     if (account == null)
/*     */     {
/* 250 */       account = "";
/*     */     }
/* 252 */     String oldSecGroup = secGroup;
/* 253 */     String oldAccount = account;
/*     */ 
/* 255 */     DialogCallback okCallback = new DialogCallback(oldSecGroup, oldAccount)
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 263 */           String pageSecurityGroup = EditViewBase.this.getLocalVar("dSecurityGroup");
/* 264 */           String pageAccount = EditViewBase.this.getLocalVar("dDocAccount");
/* 265 */           if (pageAccount == null)
/*     */           {
/* 267 */             pageAccount = "";
/*     */           }
/* 269 */           if ((((!pageSecurityGroup.equalsIgnoreCase(this.val$oldSecGroup)) || (!pageAccount.equalsIgnoreCase(this.val$oldAccount)))) && 
/* 272 */             (EditViewBase.this.m_data.m_parent != null) && 
/* 274 */             (EditViewBase.this.m_pageContext.getPage(EditViewBase.this.m_data.m_parent.m_pageId) != null))
/*     */           {
/* 276 */             EditViewBase.this.setLocalVar("PageChanged", EditViewBase.this.m_data.m_parent.m_pageId);
/*     */           }
/*     */ 
/* 281 */           EditViewBase.this.setLocalVar("LocationInfo", EditViewBase.this.m_data.m_typeId + "," + pageSecurityGroup + "," + pageAccount);
/*     */ 
/* 284 */           EditViewBase.this.m_pageContext.saveData(EditViewBase.this.m_data.m_pageId, EditViewBase.this.m_data);
/*     */         }
/*     */         catch (Exception excep)
/*     */         {
/* 288 */           EditViewBase.this.reportError(excep, "!apErrorSavingPageProperties");
/* 289 */           return false;
/*     */         }
/* 291 */         return true;
/*     */       }
/*     */     };
/* 296 */     DialogHelper helper = new DialogHelper(this.m_containerHelper.m_exchange.m_sysInterface, LocaleResources.getString("apLabelEditPageProperties", this.m_ctx), true);
/*     */ 
/* 298 */     JPanel mainPanel = helper.initStandard(this, okCallback, 0, true, DialogHelpTable.getHelpPage("EditPageProperties"));
/*     */ 
/* 303 */     initHelpers();
/*     */ 
/* 306 */     this.m_editPageHelper.addPageProperties(mainPanel, helper, false);
/*     */ 
/* 309 */     if (helper.prompt() != 1)
/*     */       return;
/* 311 */     updateView("Properties");
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sysInterface, PageManagerContext context)
/*     */   {
/* 317 */     this.m_ctx = sysInterface.getExecutionContext();
/* 318 */     this.m_editPageHelper = new EditPageHelper();
/* 319 */     this.m_containerHelper = new ContainerHelper();
/* 320 */     this.m_containerHelper.attachToContainer(this, sysInterface, null);
/* 321 */     this.m_pageContext = context;
/*     */ 
/* 324 */     initHelpers();
/* 325 */     initDisplay();
/*     */   }
/*     */ 
/*     */   public void initHelpers()
/*     */   {
/* 330 */     this.m_containerHelper.m_componentBinder = this;
/* 331 */     initSecurityHelper(this.m_editPageHelper, this.m_containerHelper, this.m_containerHelper.m_exchange.m_sysInterface);
/*     */   }
/*     */ 
/*     */   public void initSecurityHelper(EditPageHelper editPageHelper, ContainerHelper containerHelper, SystemInterface sysInterface)
/*     */   {
/* 338 */     editPageHelper.m_securityEditHelper = new SecurityEditHelper(containerHelper, sysInterface);
/*     */ 
/* 340 */     editPageHelper.m_securityEditHelper.m_userData = AppLauncher.getUserData();
/*     */   }
/*     */ 
/*     */   public void initDisplay()
/*     */   {
/* 345 */     String messageId = "HelpMessage";
/* 346 */     JPanel container = this;
/* 347 */     if (this.m_isErrorPage)
/*     */     {
/* 349 */       createStandardHeader();
/* 350 */       container = new CustomPanel();
/* 351 */       add("Center", container);
/* 352 */       messageId = "StatusMessage";
/*     */     }
/*     */     else
/*     */     {
/* 358 */       setLayout(new BorderLayout());
/* 359 */       container = new PanePanel(false);
/* 360 */       add("North", container);
/*     */ 
/* 362 */       GridBagHelper gh = this.m_containerHelper.m_gridHelper;
/* 363 */       gh.useGridBag(container);
/*     */     }
/*     */ 
/* 366 */     CustomText comp = new CustomText();
/* 367 */     GuiStyles.setCustomStyle(comp, 1);
/* 368 */     this.m_containerHelper.addExchangeComponent(container, comp, messageId);
/*     */   }
/*     */ 
/*     */   public Dimension getPreferredSize()
/*     */   {
/* 377 */     Dimension d = super.getPreferredSize();
/* 378 */     return d;
/*     */   }
/*     */ 
/*     */   public String getLocalVar(String key)
/*     */   {
/* 384 */     if (this.m_binder == null)
/* 385 */       return null;
/* 386 */     return this.m_editPageHelper.getDisplayValue(key, this.m_binder);
/*     */   }
/*     */ 
/*     */   public void setLocalVar(String key, String val)
/*     */   {
/* 391 */     if (this.m_binder == null)
/*     */     {
/* 393 */       return;
/*     */     }
/* 395 */     this.m_editPageHelper.setInternalValue(key, val, this.m_binder);
/*     */   }
/*     */ 
/*     */   public void removeLocalVar(String key)
/*     */   {
/* 400 */     this.m_binder.removeLocal(key);
/*     */   }
/*     */ 
/*     */   public void setLocalBool(String key, boolean val)
/*     */   {
/* 405 */     if (val)
/*     */     {
/* 407 */       setLocalVar(key, "1");
/*     */     }
/*     */     else
/*     */     {
/* 411 */       removeLocalVar(key);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void load(UserData curUser, PageData data, String fromPage, boolean isChild)
/*     */   {
/* 422 */     this.m_data = data;
/* 423 */     if (data == null)
/*     */     {
/* 425 */       this.m_binder = null;
/*     */     }
/*     */     else
/*     */     {
/* 429 */       this.m_binder = data.m_binder;
/*     */     }
/* 431 */     if (!isChild)
/*     */     {
/* 433 */       this.m_curChildPage = fromPage;
/*     */     }
/*     */     else
/*     */     {
/* 437 */       this.m_curChildPage = null;
/*     */     }
/*     */ 
/* 441 */     determinePrivileges(curUser);
/* 442 */     updatePrivilegeState();
/* 443 */     loadPageView();
/*     */ 
/* 446 */     updateView(null);
/*     */   }
/*     */ 
/*     */   public void updateView(String hint)
/*     */   {
/* 452 */     this.m_containerHelper.loadComponentValues();
/*     */ 
/* 455 */     enableOrDisable();
/*     */ 
/* 458 */     validate();
/*     */   }
/*     */ 
/*     */   public PageData getPageData()
/*     */   {
/* 463 */     return this.m_data;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 471 */     String name = exchange.m_compName;
/*     */ 
/* 473 */     if (this.m_data == null)
/*     */     {
/* 475 */       if (updateComponent == true)
/*     */       {
/* 477 */         if ((name.equals("HelpMessage")) && (this.m_pageContext != null))
/*     */         {
/* 479 */           Vector plist = this.m_pageContext.getPageList();
/* 480 */           if (plist.size() == 0)
/*     */           {
/* 482 */             exchange.m_compValue = LocaleResources.getString("apCreatePageToViewDefinition", this.m_ctx);
/*     */           }
/*     */           else
/*     */           {
/* 487 */             exchange.m_compValue = LocaleResources.getString("apSelectPageToViewDefinition", this.m_ctx);
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 493 */           exchange.m_compValue = "";
/*     */         }
/*     */       }
/* 496 */       return;
/*     */     }
/*     */ 
/* 501 */     if (name.equals("PageName"))
/*     */     {
/* 503 */       if (updateComponent == true)
/*     */       {
/* 505 */         exchange.m_compValue = this.m_data.m_pageId;
/*     */       }
/* 507 */       return;
/*     */     }
/*     */ 
/* 511 */     if (updateComponent == true)
/*     */     {
/* 513 */       exchange.m_compValue = getLocalVar(name);
/*     */     }
/*     */     else
/*     */     {
/* 517 */       setLocalVar(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 523 */     return this.m_editPageHelper.validate(exchange);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(Exception e, String msg)
/*     */   {
/* 533 */     MessageBox.reportError(this.m_containerHelper.m_exchange.m_sysInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public void reportError(Exception e, IdcMessage msg)
/*     */   {
/* 540 */     MessageBox.reportError(this.m_containerHelper.m_exchange.m_sysInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public void determinePrivileges(UserData curUser)
/*     */   {
/* 550 */     this.m_isEditAllowed = ((this.m_data != null) && (this.m_data.m_accessLevel.equals("15")));
/*     */   }
/*     */ 
/*     */   public void updatePrivilegeState()
/*     */   {
/* 555 */     if (this.m_editProps != null)
/*     */     {
/* 557 */       this.m_editProps.setVisible(this.m_isEditAllowed);
/*     */     }
/* 559 */     if (this.m_editPageHelper == null)
/*     */       return;
/* 561 */     this.m_editPageHelper.updatePrivilegeState(this.m_containerHelper);
/*     */   }
/*     */ 
/*     */   public void loadPageView()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void enableOrDisable()
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 578 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditViewBase
 * JD-Core Version:    0.5.4
 */