/*      */ package intradoc.apps.pagebuilder;
/*      */ 
/*      */ import intradoc.apps.shared.AppLauncher;
/*      */ import intradoc.apps.shared.MainFrame;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.GuiUtils;
/*      */ import intradoc.common.IdcComparator;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.Sort;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataExchange;
/*      */ import intradoc.data.DataExchangeBinder;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.gui.AppFrameHelper;
/*      */ import intradoc.gui.CustomFlowLayout;
/*      */ import intradoc.gui.CustomPanel;
/*      */ import intradoc.gui.CustomText;
/*      */ import intradoc.gui.CustomTextArea;
/*      */ import intradoc.gui.DialogCallback;
/*      */ import intradoc.gui.DialogHelper;
/*      */ import intradoc.gui.DynamicComponentExchange;
/*      */ import intradoc.gui.GridBagHelper;
/*      */ import intradoc.gui.IdcTreeHelper;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.gui.PanePanel;
/*      */ import intradoc.gui.iwt.DataRetrievalHelper;
/*      */ import intradoc.gui.iwt.IdcTreeCellRenderer;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedContext;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserDocumentAccessFilter;
/*      */ import intradoc.shared.gui.SecurityEditHelper;
/*      */ import intradoc.shared.gui.ValueNode;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.awt.CardLayout;
/*      */ import java.awt.Dimension;
/*      */ import java.awt.Graphics;
/*      */ import java.awt.GridBagConstraints;
/*      */ import java.awt.Image;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Observable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.accessibility.AccessibleContext;
/*      */ import javax.swing.ImageIcon;
/*      */ import javax.swing.JButton;
/*      */ import javax.swing.JMenu;
/*      */ import javax.swing.JMenuBar;
/*      */ import javax.swing.JMenuItem;
/*      */ import javax.swing.JPanel;
/*      */ import javax.swing.JScrollPane;
/*      */ import javax.swing.JTree;
/*      */ import javax.swing.event.TreeSelectionEvent;
/*      */ import javax.swing.event.TreeSelectionListener;
/*      */ import javax.swing.tree.DefaultTreeModel;
/*      */ import javax.swing.tree.TreePath;
/*      */ 
/*      */ public class PageBuilderFrame extends MainFrame
/*      */   implements DataRetrievalHelper, PageManagerContext, DataExchangeBinder, IdcComparator, SharedContext
/*      */ {
/*      */   protected JButton m_add;
/*      */   protected JButton m_delete;
/*      */   protected JButton m_gotoParent;
/*      */   protected JScrollPane m_pageScrollPane;
/*      */   protected JTree m_pageChoices;
/*      */   protected IdcTreeHelper m_pageChoicesHelper;
/*  112 */   public static Hashtable m_images = new Hashtable();
/*      */   protected JPanel m_flipPanel;
/*      */   protected Hashtable m_flipComponents;
/*      */   protected Hashtable m_indexedPageList;
/*      */   protected Vector m_pageList;
/*      */   protected Hashtable m_treeItems;
/*      */   protected Hashtable m_oldTreeItems;
/*      */   protected Hashtable m_childParentMap;
/*      */   protected DataBinder m_globalCache;
/*      */   protected static final short EXCH_PAGE_LIST = 0;
/*      */   protected static final short EXCH_PAGE_MAP = 1;
/*  140 */   protected static final String[] m_pageListFields = { "PageName", "PageType", "PageLastChanged", "UserAccess" };
/*      */ 
/*  142 */   protected static final String[] m_mapFields = { "PageName", "PageParent" };
/*      */ 
/*  146 */   protected static final String[] m_pageDataTables = { "PageMap", "LinkList" };
/*      */   protected Hashtable m_oldindexedPageList;
/*      */   protected EditContext m_curEditor;
/*      */   protected PageData m_curEditPage;
/*      */   protected PageData m_curSelPage;
/*      */   protected PageData[] m_parList1;
/*      */   protected PageData[] m_parList2;
/*      */   protected short m_exchType;
/*  168 */   protected static short m_refreshRecursiveCount = 0;
/*      */ 
/*  171 */   protected boolean m_insideRefreshCheckSelection = false;
/*      */   protected boolean m_isDirty;
/*      */   protected UserDocumentAccessFilter m_readPageAccess;
/*      */   protected UserDocumentAccessFilter m_adminPageAccess;
/*  184 */   protected ExecutionContext m_ctx = null;
/*      */ 
/*      */   public PageBuilderFrame()
/*      */   {
/*  193 */     this.m_curEditPage = null;
/*  194 */     this.m_curSelPage = null;
/*  195 */     this.m_globalCache = new DataBinder();
/*  196 */     this.m_exchType = 0;
/*  197 */     this.m_indexedPageList = new Hashtable();
/*  198 */     this.m_treeItems = new Hashtable();
/*  199 */     this.m_oldTreeItems = null;
/*  200 */     this.m_pageList = new IdcVector();
/*  201 */     this.m_childParentMap = new Hashtable();
/*  202 */     this.m_parList1 = new PageData[16];
/*  203 */     this.m_parList2 = new PageData[16];
/*  204 */     this.m_isDirty = true;
/*  205 */     this.m_readPageAccess = null;
/*  206 */     this.m_adminPageAccess = null;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void init(String title, boolean closeOnExit)
/*      */     throws ServiceException
/*      */   {
/*  214 */     IdcMessage msg = null;
/*  215 */     if (title != null)
/*      */     {
/*  217 */       msg = IdcMessageFactory.lc();
/*  218 */       msg.m_msgEncoded = title;
/*      */     }
/*  220 */     init(msg, closeOnExit);
/*      */   }
/*      */ 
/*      */   public void init(IdcMessage title, boolean closeOnExit)
/*      */     throws ServiceException
/*      */   {
/*  226 */     super.init(title, closeOnExit);
/*  227 */     this.m_appHelper.attachToAppFrame(this, null, null, title);
/*  228 */     this.m_ctx = this.m_appHelper.getExecutionContext();
/*      */ 
/*  232 */     Image tmpImage = GuiUtils.getAppImage("tree_icons/open.gif");
/*  233 */     m_images.put("open:Directory", tmpImage);
/*  234 */     tmpImage = GuiUtils.getAppImage("tree_icons/closed.gif");
/*  235 */     m_images.put("closed:Directory", tmpImage);
/*      */ 
/*  237 */     tmpImage = GuiUtils.getAppImage("tree_icons/active.gif");
/*  238 */     m_images.put("open:ActiveReport", tmpImage);
/*  239 */     m_images.put("closed:ActiveReport", tmpImage);
/*  240 */     tmpImage = GuiUtils.getAppImage("tree_icons/historical.gif");
/*  241 */     m_images.put("open:SavedReport", tmpImage);
/*  242 */     m_images.put("closed:SavedReport", tmpImage);
/*      */ 
/*  246 */     JMenuBar mb = new JMenuBar();
/*  247 */     JMenu m = new JMenu(LocaleResources.getString("apTitleOptions", this.m_ctx));
/*      */ 
/*  249 */     JMenuItem updatePortal = new JMenuItem(LocaleResources.getString("apLabelUpdatePortal", this.m_ctx));
/*      */ 
/*  251 */     updatePortal.addActionListener(new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*      */         try
/*      */         {
/*  257 */           PageBuilderFrame.this.promptUpdatePortal();
/*      */         }
/*      */         catch (Exception exp)
/*      */         {
/*  261 */           MessageBox.reportError(PageBuilderFrame.this.m_appHelper, exp);
/*      */         }
/*      */       }
/*      */     });
/*  265 */     m.add(updatePortal);
/*      */ 
/*  267 */     if (AppLauncher.isAdmin())
/*      */     {
/*  269 */       JMenuItem resultPages = new JMenuItem(LocaleResources.getString("apDlgButtonQueryResultPages", this.m_ctx));
/*      */ 
/*  271 */       resultPages.addActionListener(new ActionListener()
/*      */       {
/*      */         public void actionPerformed(ActionEvent e)
/*      */         {
/*  275 */           ResultPageDlg dlg = new ResultPageDlg(PageBuilderFrame.this.m_appHelper, LocaleResources.getString("apLabelQueryResultPages", PageBuilderFrame.this.m_ctx));
/*      */ 
/*  277 */           dlg.prompt();
/*      */         }
/*      */       });
/*  280 */       m.add(resultPages);
/*      */     }
/*      */ 
/*  283 */     m.addSeparator();
/*      */ 
/*  285 */     addStandardOptions(m);
/*      */ 
/*  287 */     mb.add(m);
/*      */ 
/*  289 */     addAppMenu(mb);
/*      */ 
/*  291 */     setJMenuBar(mb);
/*      */ 
/*  293 */     GridBagHelper gh = this.m_appHelper.m_gridHelper;
/*  294 */     JPanel pageList = new CustomPanel();
/*  295 */     gh.useGridBag(pageList);
/*  296 */     this.m_appHelper.addPanelTitle(pageList, LocaleResources.getString("apLabelWebPageHierarchy", this.m_ctx));
/*      */ 
/*  299 */     JPanel pageListButtons = new PanePanel();
/*  300 */     pageListButtons.setLayout(new CustomFlowLayout());
/*      */ 
/*  302 */     this.m_add = new JButton(LocaleResources.getString("apDlgButtonAdd", this.m_ctx));
/*  303 */     this.m_add.getAccessibleContext().setAccessibleName(LocaleResources.getString("apLabelAddWebPage", this.m_ctx));
/*  304 */     this.m_add.addActionListener(new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*  308 */         PageBuilderFrame.this.promptAddPage();
/*      */       }
/*      */     });
/*  311 */     pageListButtons.add(this.m_add);
/*      */ 
/*  313 */     this.m_delete = new JButton(LocaleResources.getString("apLabelDelete", this.m_ctx));
/*  314 */     this.m_delete.addActionListener(new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*  318 */         TreePath p = PageBuilderFrame.this.m_pageChoices.getSelectionPath();
/*  319 */         if (p == null)
/*      */           return;
/*  321 */         ValueNode item = (ValueNode)p.getLastPathComponent();
/*  322 */         PageData pdata = (PageData)item.m_extraData;
/*  323 */         PageBuilderFrame.this.deletePage(pdata.m_pageId);
/*      */       }
/*      */     });
/*  327 */     pageListButtons.add(this.m_delete);
/*      */ 
/*  329 */     this.m_gotoParent = new JButton(LocaleResources.getString("apLabelToParent", this.m_ctx));
/*  330 */     this.m_gotoParent.addActionListener(new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*      */         try
/*      */         {
/*  336 */           TreePath p = PageBuilderFrame.this.m_pageChoices.getSelectionPath();
/*  337 */           if (p != null)
/*      */           {
/*  339 */             ValueNode item = (ValueNode)p.getLastPathComponent();
/*  340 */             PageData pdata = (PageData)item.m_extraData;
/*  341 */             PageBuilderFrame.this.loadEditView(pdata.m_parent.m_pageId, pdata.m_pageId, false);
/*      */ 
/*  343 */             PageBuilderFrame.this.checkSelection();
/*      */           }
/*      */         }
/*      */         catch (Exception excep)
/*      */         {
/*  348 */           PageBuilderFrame.this.reportError(excep, IdcMessageFactory.lc("apErrorGoingToParentPage", new Object[0]));
/*      */         }
/*      */       }
/*      */     });
/*  352 */     pageListButtons.add(this.m_gotoParent);
/*      */ 
/*  354 */     this.m_pageChoicesHelper = new IdcTreeHelper();
/*  355 */     this.m_pageChoices = this.m_pageChoicesHelper.createTree();
/*  356 */     this.m_pageScrollPane = new JScrollPane(this.m_pageChoices);
/*  357 */     this.m_pageChoices.setCellRenderer(new IdcTreeCellRenderer());
/*  358 */     this.m_pageChoices.addTreeSelectionListener(new TreeSelectionListener()
/*      */     {
/*      */       public void valueChanged(TreeSelectionEvent e)
/*      */       {
/*  362 */         PageBuilderFrame.this.checkSelection();
/*      */       }
/*      */     });
/*  366 */     gh.prepareAddLastRowElement(11);
/*  367 */     gh.m_gc.fill = 1;
/*  368 */     gh.m_gc.weighty = 1.0D;
/*  369 */     this.m_appHelper.addComponent(pageList, this.m_pageScrollPane);
/*  370 */     gh.m_gc.fill = 0;
/*  371 */     gh.m_gc.weighty = 0.0D;
/*  372 */     this.m_appHelper.addComponent(pageList, pageListButtons);
/*      */ 
/*  374 */     this.m_flipPanel = new PanePanel();
/*  375 */     this.m_flipComponents = new Hashtable();
/*  376 */     CardLayout flipLayout = new CardLayout();
/*  377 */     this.m_flipPanel.setLayout(flipLayout);
/*      */ 
/*  379 */     EditViewBase editPageView = new EditViewBase();
/*  380 */     this.m_curEditor = editPageView;
/*  381 */     addFlipComponent("Empty", editPageView);
/*  382 */     EditViewBase errorPageView = new EditViewBase();
/*  383 */     errorPageView.m_isErrorPage = true;
/*  384 */     addFlipComponent("ErrorPage", errorPageView);
/*  385 */     addFlipComponent("Directory", new EditDirPagePanel());
/*  386 */     addFlipComponent("ActiveReport", new EditReportPagePanel(false));
/*  387 */     addFlipComponent("SavedReport", new EditReportPagePanel(true));
/*  388 */     flipLayout.show(this.m_flipPanel, "Empty");
/*      */ 
/*  391 */     JPanel mainPanel = this.m_appHelper.m_mainPanel;
/*  392 */     gh.useGridBag(mainPanel);
/*  393 */     gh.m_gc.weightx = 1.0D;
/*  394 */     gh.m_gc.weighty = 1.0D;
/*  395 */     gh.m_gc.fill = 1;
/*  396 */     this.m_appHelper.addComponent(mainPanel, pageList);
/*  397 */     gh.m_gc.weightx = 0.5D;
/*  398 */     this.m_appHelper.addComponent(mainPanel, this.m_flipPanel);
/*      */ 
/*  405 */     loadGlobalData(false);
/*      */ 
/*  408 */     this.m_curEditor.updateView(null);
/*      */ 
/*  414 */     pack();
/*  415 */     setVisible(true);
/*      */ 
/*  417 */     this.m_appHelper.displayStatus(LocaleResources.getString("apLabelReady", this.m_ctx));
/*      */ 
/*  420 */     AppLauncher.addSubjectObserver("pagelist", this);
/*  421 */     AppLauncher.addSubjectObserver("users", this);
/*      */   }
/*      */ 
/*      */   protected void addFlipComponent(String compId, EditViewBase comp)
/*      */   {
/*  426 */     comp.init(this.m_appHelper, this);
/*  427 */     this.m_flipPanel.add(compId, comp);
/*  428 */     this.m_flipComponents.put(compId, comp);
/*      */   }
/*      */ 
/*      */   public void pack()
/*      */   {
/*  435 */     if ((getSize().width >= 200) && (getSize().height >= 200))
/*      */       return;
/*  437 */     super.pack();
/*      */   }
/*      */ 
/*      */   public void paint(Graphics g)
/*      */   {
/*  444 */     if ((getSize().width < 200) || (getSize().height < 200))
/*      */     {
/*  446 */       invalidate();
/*  447 */       setLocation(100, 100);
/*  448 */       pack();
/*      */     }
/*  450 */     super.paint(g);
/*      */   }
/*      */ 
/*      */   public void addNotify()
/*      */   {
/*  456 */     super.addNotify();
/*      */   }
/*      */ 
/*      */   public Dimension getPreferredSize()
/*      */   {
/*  464 */     return super.getPreferredSize();
/*      */   }
/*      */ 
/*      */   public void dispose()
/*      */   {
/*  470 */     AppLauncher.removeSubjectObserver("pagelist", this);
/*  471 */     AppLauncher.removeSubjectObserver("users", this);
/*  472 */     super.dispose();
/*      */   }
/*      */ 
/*      */   public void promptAddPage()
/*      */   {
/*  477 */     PageData pageData = new PageData();
/*  478 */     EditPageHelper editPageHelper = new EditPageHelper();
/*  479 */     editPageHelper.m_securityEditHelper = new SecurityEditHelper(this.m_appHelper, this.m_appHelper);
/*  480 */     editPageHelper.m_securityEditHelper.m_userData = getUserData();
/*      */     try
/*      */     {
/*  485 */       if (editPageHelper.promptNewPage(this.m_appHelper, pageData, "Public", this))
/*      */       {
/*  487 */         loadEditView(pageData.m_pageId, null, false);
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  492 */       reportError(e, IdcMessageFactory.lc("apCouldNotLoadPageContents", new Object[0]));
/*      */     }
/*      */   }
/*      */ 
/*      */   public void update(Graphics g)
/*      */   {
/*      */     try
/*      */     {
/*  501 */       loadGlobalData(false);
/*  502 */       super.update(g);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  506 */       MessageBox.reportError(this.m_appHelper, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void promptUpdatePortal() throws ServiceException
/*      */   {
/*  512 */     DataBinder editData = new DataBinder();
/*  513 */     AppLauncher.executeService("LOAD_GLOBALINCLUDES", editData, this.m_appHelper);
/*      */ 
/*  515 */     DialogHelper helper = new DialogHelper(this.m_appHelper, LocaleResources.getString("apLabelUpdatePortalPage", this.m_ctx), true);
/*      */ 
/*  517 */     DialogCallback callback = new DialogCallback(editData)
/*      */     {
/*      */       public boolean handleDialogEvent(ActionEvent e)
/*      */       {
/*      */         try
/*      */         {
/*  524 */           this.val$editData.putLocal("PageFunction", "GetPageList");
/*  525 */           this.val$editData.putLocal("IsRebuild", "1");
/*  526 */           AppLauncher.executeService("SAVE_GLOBALINCLUDES", this.val$editData, PageBuilderFrame.this.m_appHelper);
/*      */         }
/*      */         catch (ServiceException excep)
/*      */         {
/*  530 */           this.m_errorMessage = LocaleUtils.createMessageListFromThrowable(excep);
/*  531 */           return false;
/*      */         }
/*  533 */         return true;
/*      */       }
/*      */     };
/*  537 */     helper.initStandard(null, callback, 1, false, null);
/*  538 */     helper.m_props = editData.getLocalData();
/*      */ 
/*  540 */     JPanel mainPanel = helper.m_mainPanel;
/*  541 */     GridBagConstraints gc = helper.m_gridHelper.m_gc;
/*      */ 
/*  543 */     CustomText customText = new CustomText(LocaleResources.getString("apPortalPageUpdateDesc", this.m_ctx), 80);
/*      */ 
/*  545 */     helper.addLastComponentInRow(mainPanel, customText);
/*  546 */     CustomTextArea editText = new CustomTextArea(20, 80);
/*  547 */     gc.weightx = 1.0D;
/*  548 */     gc.weighty = 1.0D;
/*  549 */     gc.gridwidth = 0;
/*      */ 
/*  551 */     helper.m_exchange.addComponent("portal_message:globalInclude", editText, null);
/*  552 */     JScrollPane scrollPane = new JScrollPane(editText);
/*  553 */     helper.addComponent(mainPanel, scrollPane);
/*      */ 
/*  555 */     helper.prompt();
/*      */   }
/*      */ 
/*      */   protected synchronized void loadGlobalData(boolean isRebuild)
/*      */     throws ServiceException
/*      */   {
/*  563 */     if (isRebuild)
/*      */     {
/*  565 */       this.m_isDirty = true;
/*      */     }
/*  567 */     if (!this.m_isDirty)
/*      */     {
/*  569 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  574 */       DataBinder data = createRequestObject("GetPageList", null);
/*  575 */       if (isRebuild)
/*      */       {
/*  577 */         data.putLocal("IsRebuild", "1");
/*      */       }
/*      */ 
/*  580 */       executePageHandlerService(data, false);
/*      */ 
/*  582 */       data.removeLocal("IsRebuild");
/*      */ 
/*  584 */       this.m_globalCache = data;
/*      */ 
/*  587 */       this.m_oldindexedPageList = this.m_indexedPageList;
/*  588 */       this.m_indexedPageList = new Hashtable();
/*  589 */       this.m_pageList = new IdcVector();
/*  590 */       this.m_childParentMap = new Hashtable();
/*  591 */       this.m_curEditPage = this.m_curEditor.getPageData();
/*  592 */       if (this.m_curEditPage != null)
/*      */       {
/*  594 */         this.m_curEditPage.m_parent = null;
/*      */       }
/*      */ 
/*  598 */       readFromResultset("PageList", m_pageListFields, 0, data);
/*      */ 
/*  601 */       readFromResultset("PageMap", m_mapFields, 1, data);
/*      */ 
/*  603 */       refreshPageList();
/*      */ 
/*  606 */       this.m_oldindexedPageList = null;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       UserData curUser;
/*  616 */       UserData curUser = getUserData();
/*  617 */       if (curUser == null)
/*      */       {
/*  619 */         this.m_readPageAccess = null;
/*  620 */         this.m_adminPageAccess = null;
/*      */       }
/*      */       else
/*      */       {
/*      */         try
/*      */         {
/*  626 */           this.m_readPageAccess = SecurityUtils.getUserDocumentAccessFilter(curUser, 1);
/*      */ 
/*  628 */           this.m_adminPageAccess = SecurityUtils.getUserDocumentAccessFilter(curUser, 8);
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*  633 */           e.printStackTrace();
/*      */         }
/*      */       }
/*      */ 
/*  637 */       this.m_isDirty = false;
/*      */     }
/*      */   }
/*      */ 
/*      */   void readFromResultset(String resultSet, String[] fields, short exchType, DataBinder data)
/*      */     throws DataException
/*      */   {
/*  644 */     this.m_exchType = exchType;
/*  645 */     ResultSet rset = data.getResultSet(resultSet);
/*  646 */     if (rset == null)
/*      */       return;
/*  648 */     DataExchange exch = new DataExchange(rset, fields);
/*  649 */     exch.doExchange(this, false);
/*      */   }
/*      */ 
/*      */   public synchronized void refreshPageList()
/*      */     throws ServiceException
/*      */   {
/*  658 */     if (m_refreshRecursiveCount > 0)
/*      */     {
/*  660 */       return;
/*      */     }
/*  662 */     m_refreshRecursiveCount = (short)(m_refreshRecursiveCount + 1);
/*      */     try
/*      */     {
/*  668 */       if (this.m_curEditPage != null)
/*      */       {
/*  670 */         if ((this.m_indexedPageList.get(this.m_curEditPage.m_pageId) == null) || (!this.m_curEditPage.m_isComplete))
/*      */         {
/*  673 */           loadEditView(this.m_curEditPage.m_pageId, null, false);
/*      */         }
/*  675 */         this.m_curSelPage = this.m_curEditPage;
/*      */       }
/*      */ 
/*  680 */       String curSel = null;
/*  681 */       if (this.m_curSelPage != null)
/*      */       {
/*  683 */         curSel = this.m_curSelPage.m_pageId;
/*      */       }
/*      */ 
/*  687 */       DefaultTreeModel treeModel = (DefaultTreeModel)this.m_pageChoices.getModel();
/*  688 */       ValueNode root = (ValueNode)treeModel.getRoot();
/*  689 */       for (int j = root.getChildCount() - 1; j >= 0; --j)
/*      */       {
/*  691 */         ValueNode node = (ValueNode)root.getChildAt(j);
/*  692 */         treeModel.removeNodeFromParent(node);
/*      */       }
/*      */ 
/*  695 */       int npages = this.m_pageList.size();
/*  696 */       if (npages > 0)
/*      */       {
/*  698 */         PageData[] pageListCopy = new PageData[npages];
/*  699 */         for (int i = 0; i < npages; ++i)
/*      */         {
/*  701 */           PageData pdata = (PageData)this.m_pageList.elementAt(i);
/*  702 */           pageListCopy[i] = pdata;
/*      */         }
/*      */ 
/*  706 */         Sort.sort(pageListCopy, 0, npages - 1, this);
/*      */ 
/*  710 */         char[] prefixChars = new char[102];
/*  711 */         for (i = 0; i < 102; ++i)
/*      */         {
/*  713 */           prefixChars[i] = ' ';
/*      */         }
/*      */ 
/*  716 */         this.m_oldTreeItems = this.m_treeItems;
/*  717 */         this.m_treeItems = new Hashtable();
/*  718 */         for (i = 0; i < npages; ++i)
/*      */         {
/*  720 */           PageData pdata = pageListCopy[i];
/*  721 */           this.m_pageList.setElementAt(pdata, i);
/*  722 */           PageData parent = pdata.m_parent;
/*      */ 
/*  724 */           ValueNode item = (ValueNode)this.m_treeItems.get(pdata.m_pageId);
/*  725 */           if (item == null)
/*      */           {
/*  727 */             item = (ValueNode)this.m_oldTreeItems.get(pdata.m_pageId);
/*  728 */             item = new ValueNode();
/*  729 */             item.m_extraData = pdata;
/*  730 */             item.m_dataHelper = this;
/*  731 */             if ((pdata.m_typeId != null) && (pdata.m_typeId.equals("Directory")))
/*      */             {
/*  733 */               item.setAllowsChildren(true);
/*      */             }
/*      */             else
/*      */             {
/*  737 */               item.setAllowsChildren(false);
/*      */             }
/*      */ 
/*  740 */             this.m_treeItems.put(pdata.m_pageId, item);
/*      */           }
/*      */ 
/*  743 */           if (parent == null)
/*      */           {
/*  745 */             treeModel.insertNodeInto(item, root, root.getChildCount());
/*      */           }
/*      */           else
/*      */           {
/*  749 */             ValueNode parentItem = (ValueNode)this.m_treeItems.get(parent.m_pageId);
/*  750 */             if (parentItem == null)
/*      */             {
/*  752 */               treeModel.insertNodeInto(item, root, root.getChildCount());
/*      */             }
/*      */             else
/*      */             {
/*  756 */               treeModel.insertNodeInto(item, parentItem, parentItem.getChildCount());
/*      */             }
/*      */           }
/*      */ 
/*  760 */           this.m_pageChoices.expandPath(this.m_pageChoicesHelper.getTreePath(item));
/*      */ 
/*  762 */           if ((curSel == null) || (!curSel.equals(pdata.m_pageId)))
/*      */             continue;
/*  764 */           TreePath itemPath = this.m_pageChoicesHelper.getTreePath(item);
/*  765 */           this.m_pageChoices.setSelectionPath(itemPath);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  770 */       TreePath rootPath = new TreePath(root);
/*  771 */       this.m_pageChoices.expandPath(rootPath);
/*      */     }
/*      */     finally
/*      */     {
/*  775 */       checkSelection();
/*  776 */       m_refreshRecursiveCount = (short)(m_refreshRecursiveCount - 1);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkSelection()
/*      */   {
/*  782 */     ValueNode item = null;
/*  783 */     TreePath p = this.m_pageChoices.getSelectionPath();
/*  784 */     if (p != null)
/*      */     {
/*  786 */       item = (ValueNode)p.getLastPathComponent();
/*      */     }
/*      */ 
/*  789 */     boolean isItemSelected = item != null;
/*  790 */     boolean isEditable = isItemSelected;
/*  791 */     boolean isParent = false;
/*      */     try
/*      */     {
/*  795 */       if (isItemSelected)
/*      */       {
/*  797 */         PageData pdata = (PageData)item.m_extraData;
/*  798 */         if (pdata == null)
/*      */         {
/*  800 */           isEditable = false;
/*      */         }
/*      */         else
/*      */         {
/*  804 */           isEditable = pdata.m_accessLevel.equals("15");
/*      */         }
/*  806 */         if (m_refreshRecursiveCount == 0)
/*      */         {
/*  808 */           this.m_curSelPage = pdata;
/*  809 */           if ((this.m_curEditPage == null) || (!this.m_curEditPage.m_pageId.equals(pdata.m_pageId)))
/*      */           {
/*  812 */             this.m_curEditPage = pdata;
/*  813 */             loadEditView(pdata.m_pageId, null, false);
/*      */           }
/*      */         }
/*      */ 
/*  817 */         String parent = pdata.m_binder.getLocal("PageParent");
/*  818 */         if ((parent != null) && (parent.length() != 0))
/*      */         {
/*  820 */           isParent = true;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  825 */         this.m_curSelPage = null;
/*  826 */         loadEditView(null, null, false);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  831 */       reportError(e, IdcMessageFactory.lc("apErrorLoadingSelectedPage", new Object[0]));
/*      */     }
/*  833 */     this.m_delete.setEnabled(isEditable);
/*  834 */     this.m_gotoParent.setEnabled(isParent);
/*      */   }
/*      */ 
/*      */   public void deletePage(String pageId)
/*      */   {
/*  839 */     int ret = MessageBox.doMessage(this.m_appHelper, IdcMessageFactory.lc("apVerifyPageDelete", new Object[] { pageId }), 4);
/*      */ 
/*  841 */     if (ret != 2)
/*      */     {
/*  843 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  848 */       DataBinder data = createRequestObject("DeletePage", pageId);
/*      */ 
/*  850 */       executePageHandlerService(data, true);
/*      */ 
/*  853 */       updatePageMaps(pageId, this.m_globalCache, false);
/*  854 */       this.m_indexedPageList.remove(pageId);
/*  855 */       int size = this.m_pageList.size();
/*  856 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  858 */         PageData pdata = (PageData)this.m_pageList.elementAt(i);
/*  859 */         if (pdata.m_pageId.equals(pageId) != true)
/*      */           continue;
/*  861 */         this.m_pageList.removeElementAt(i);
/*  862 */         break;
/*      */       }
/*      */ 
/*  865 */       if ((this.m_curEditPage != null) && (this.m_curEditPage.m_pageId.equals(pageId)))
/*      */       {
/*  867 */         loadEditView(null, null, false);
/*      */       }
/*  869 */       if ((this.m_curSelPage != null) && (this.m_curSelPage.m_pageId.equals(pageId)))
/*      */       {
/*  871 */         this.m_curSelPage = null;
/*      */       }
/*      */ 
/*  874 */       refreshPageList();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  878 */       reportError(e, IdcMessageFactory.lc("apErrorDeletingPage", new Object[] { null, pageId }));
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setDefaults(PageData data)
/*      */   {
/*  889 */     Properties pageParams = data.m_binder.getLocalData();
/*  890 */     String securityGroup = pageParams.getProperty("dSecurityGroup");
/*  891 */     if (securityGroup == null)
/*      */     {
/*  893 */       securityGroup = "Public";
/*      */     }
/*  895 */     String account = pageParams.getProperty("dDocAccount");
/*  896 */     if (account == null)
/*      */     {
/*  898 */       account = "";
/*      */     }
/*      */ 
/*  901 */     pageParams.put("LocationInfo", data.m_typeId + "," + securityGroup + "," + account);
/*  902 */     if (!data.m_typeId.equals("Directory"))
/*      */       return;
/*  904 */     pageParams.put("TemplatePage", "DIRECTORY_PAGE");
/*      */   }
/*      */ 
/*      */   public void loadEditView(String toPageId, String fromPage, boolean toChild)
/*      */     throws ServiceException
/*      */   {
/*  912 */     PageData pdata = null;
/*  913 */     CardLayout panelHandler = (CardLayout)this.m_flipPanel.getLayout();
/*  914 */     String panelName = "Empty";
/*  915 */     String errMsgKey = null;
/*  916 */     String errMsg = null;
/*  917 */     boolean generateException = false;
/*      */     try
/*      */     {
/*  924 */       if (toPageId != null)
/*      */       {
/*  926 */         pdata = (PageData)this.m_indexedPageList.get(toPageId);
/*  927 */         if (pdata != null)
/*      */         {
/*  929 */           if (!pdata.m_accessLevel.equals("0"))
/*      */           {
/*  931 */             if (!pdata.m_isComplete)
/*      */             {
/*  933 */               loadData(toPageId, pdata);
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/*  938 */             errMsgKey = "!apInsufficientPrivilegeToViewPage";
/*  939 */             errMsg = LocaleResources.getString("apInsufficientPrivilegeToViewPage", this.m_ctx);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  945 */       if (pdata != null)
/*      */       {
/*  947 */         panelName = pdata.m_typeId;
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  952 */       errMsg = e.getMessage();
/*  953 */       errMsgKey = errMsg;
/*  954 */       generateException = true;
/*      */     }
/*      */ 
/*  959 */     if ((errMsg != null) && 
/*  961 */       (pdata != null))
/*      */     {
/*  963 */       panelName = "ErrorPage";
/*  964 */       pdata.m_binder.putLocal("StatusMessageKey", errMsgKey);
/*  965 */       pdata.m_binder.putLocal("StatusMessage", errMsg);
/*  966 */       if ((pdata.m_parent != null) && (pdata.m_parent.m_pageId != null))
/*      */       {
/*  968 */         pdata.m_binder.putLocal("PageParent", pdata.m_parent.m_pageId);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  973 */     EditContext pageEditor = (EditContext)this.m_flipComponents.get(panelName);
/*  974 */     if (pageEditor == null)
/*      */     {
/*  976 */       throw new ServiceException(LocaleResources.getString("apUnrecognizedPageType", this.m_ctx, panelName));
/*      */     }
/*      */ 
/*  979 */     this.m_curEditor = pageEditor;
/*      */ 
/*  982 */     this.m_curEditor.load(getUserData(), pdata, fromPage, toChild);
/*      */ 
/*  985 */     panelHandler.show(this.m_flipPanel, panelName);
/*      */ 
/*  988 */     this.m_curEditPage = pdata;
/*      */ 
/*  990 */     boolean doCheck = false;
/*  991 */     String oldCurPageId = null;
/*  992 */     if ((this.m_curEditPage != null) && (this.m_curEditPage != this.m_curSelPage))
/*      */     {
/*  994 */       m_refreshRecursiveCount = (short)(m_refreshRecursiveCount + 1);
/*      */       try
/*      */       {
/*  999 */         oldCurPageId = this.m_curEditPage.m_pageId;
/* 1000 */         ValueNode item = (ValueNode)this.m_treeItems.get(this.m_curEditPage.m_pageId);
/* 1001 */         if (item != null)
/*      */         {
/* 1003 */           ValueNode parent = item.getParent();
/* 1004 */           if (parent != null)
/*      */           {
/* 1006 */             TreePath parentPath = this.m_pageChoicesHelper.getTreePath(parent);
/* 1007 */             if (!this.m_pageChoices.isExpanded(parentPath))
/*      */             {
/* 1009 */               this.m_pageChoices.expandPath(parentPath);
/*      */             }
/*      */           }
/*      */ 
/* 1013 */           TreePath itemPath = this.m_pageChoicesHelper.getTreePath(item);
/* 1014 */           this.m_pageChoices.setSelectionPath(itemPath);
/* 1015 */           doCheck = true;
/*      */         }
/*      */       }
/*      */       finally
/*      */       {
/* 1020 */         m_refreshRecursiveCount = (short)(m_refreshRecursiveCount - 1);
/*      */       }
/*      */     }
/*      */ 
/* 1024 */     if ((generateException) && (errMsg != null))
/*      */     {
/* 1026 */       throw new ServiceException(errMsg);
/*      */     }
/* 1028 */     if (!doCheck)
/*      */       return;
/* 1030 */     if (!this.m_insideRefreshCheckSelection)
/*      */     {
/*      */       try
/*      */       {
/* 1034 */         this.m_insideRefreshCheckSelection = true;
/* 1035 */         checkSelection();
/*      */       }
/*      */       finally
/*      */       {
/* 1039 */         this.m_insideRefreshCheckSelection = false;
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/* 1044 */       Report.trace("system", "PageBuilderFrame.checkSelection -- selection of page " + oldCurPageId + " was ambiguous and page object was not uniquely selected.", null);
/*      */   }
/*      */ 
/*      */   public Vector getPageList()
/*      */   {
/* 1055 */     return this.m_pageList;
/*      */   }
/*      */ 
/*      */   public PageData getPage(String pageId)
/*      */   {
/* 1061 */     PageData data = (PageData)this.m_indexedPageList.get(pageId);
/* 1062 */     if (data != null)
/*      */     {
/* 1064 */       return data;
/*      */     }
/*      */ 
/* 1070 */     int size = this.m_pageList.size();
/* 1071 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1073 */       PageData pdata = (PageData)this.m_pageList.elementAt(i);
/* 1074 */       if (pdata.m_pageId.equalsIgnoreCase(pageId))
/*      */       {
/* 1076 */         return pdata;
/*      */       }
/*      */     }
/* 1079 */     return null;
/*      */   }
/*      */ 
/*      */   public void loadData(String pageId, PageData data)
/*      */     throws ServiceException
/*      */   {
/* 1089 */     executePageService(pageId, data, "GetPage", LocaleResources.getString("apLabelLoadingPage", this.m_ctx));
/*      */   }
/*      */ 
/*      */   public void saveData(String pageId, PageData data)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1098 */       executePageService(pageId, data, "SavePage", LocaleResources.getString("apLabelSavingPage", this.m_ctx));
/*      */ 
/* 1100 */       data.m_binder.removeLocal("IsNewPage");
/*      */     }
/*      */     finally
/*      */     {
/*      */       DataBinder binder;
/* 1104 */       DataBinder binder = data.m_binder;
/* 1105 */       binder.removeLocal("PageChanged");
/* 1106 */       binder.removeLocal("OldPageChanged");
/*      */     }
/*      */   }
/*      */ 
/*      */   public String[][] getPageTypesList()
/*      */   {
/* 1114 */     String[][] list = (String[][])null;
/* 1115 */     if (AppLauncher.isAdmin())
/*      */     {
/* 1117 */       list = new String[][] { { "Directory", LocaleResources.getString("apLabelDirectory", this.m_ctx) }, { "ActiveReport", LocaleResources.getString("apLabelActiveReport", this.m_ctx) }, { "SavedReport", LocaleResources.getString("apLabelHistoricalReport", this.m_ctx) } };
/*      */     }
/*      */     else
/*      */     {
/* 1126 */       list = new String[][] { { "Directory", LocaleResources.getString("apLabelDirectory", this.m_ctx) } };
/*      */     }
/* 1128 */     return list;
/*      */   }
/*      */ 
/*      */   public PageType getPageType(String typeId)
/*      */   {
/* 1135 */     PageType type = new PageType();
/* 1136 */     type.m_id = typeId;
/* 1137 */     return type;
/*      */   }
/*      */ 
/*      */   public DataBinder getGlobalData()
/*      */   {
/* 1144 */     return this.m_globalCache;
/*      */   }
/*      */ 
/*      */   public boolean prepareNextRow(DataExchange exch, boolean writeToResultSet)
/*      */   {
/* 1155 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean exchange(DataExchange exch, int index, boolean writeToResultSet)
/*      */     throws DataException
/*      */   {
/* 1165 */     String val = exch.getCurValAsString();
/* 1166 */     if (val == null)
/*      */     {
/* 1168 */       throw new DataException(LocaleResources.getString("apNullFieldInData", this.m_ctx, exch.m_curFieldInfo.m_name));
/*      */     }
/*      */ 
/* 1174 */     if (index == 0)
/*      */     {
/*      */       Hashtable pageList;
/*      */       Hashtable pageList;
/* 1177 */       if (this.m_exchType == 0)
/*      */       {
/* 1179 */         pageList = this.m_oldindexedPageList;
/*      */       }
/*      */       else
/*      */       {
/* 1183 */         pageList = this.m_indexedPageList;
/*      */       }
/* 1185 */       PageData pdata = (PageData)pageList.get(val);
/* 1186 */       if (pdata == null)
/*      */       {
/* 1191 */         pdata = new PageData();
/* 1192 */         pdata.m_pageId = val;
/*      */       }
/* 1196 */       else if (this.m_exchType == 0)
/*      */       {
/* 1198 */         pdata.m_parent = null;
/*      */       }
/*      */ 
/* 1201 */       exch.m_curObj = pdata;
/* 1202 */       return true;
/*      */     }
/*      */ 
/* 1206 */     PageData pdata = (PageData)exch.m_curObj;
/*      */ 
/* 1208 */     switch (this.m_exchType)
/*      */     {
/*      */     case 0:
/* 1211 */       switch (index)
/*      */       {
/*      */       case 1:
/* 1214 */         pdata.m_typeId = val;
/* 1215 */         break;
/*      */       case 2:
/* 1217 */         if (pdata.m_lastChanged.equals(val))
/*      */           break label261;
/* 1219 */         pdata.m_isComplete = false;
/* 1220 */         pdata.m_lastChanged = val; break;
/*      */       case 3:
/* 1224 */         if ((pdata.m_accessLevel != null) && (!pdata.m_accessLevel.equals(val)))
/*      */         {
/* 1226 */           pdata.m_isComplete = false;
/*      */         }
/* 1228 */         pdata.m_accessLevel = val;
/*      */       }
/*      */ 
/* 1231 */       break;
/*      */     case 1:
/* 1233 */       label261: if (index != 1) {
/*      */         break label421;
/*      */       }
/* 1236 */       PageData newParent = getPage(val);
/* 1237 */       if (newParent == null)
/*      */         break label421;
/* 1239 */       int treeDepth = 0;
/* 1240 */       boolean isBadParent = false;
/* 1241 */       for (PageData temp = newParent; temp != null; temp = temp.m_parent)
/*      */       {
/* 1243 */         if (temp == pdata)
/*      */         {
/* 1245 */           reportError(null, IdcMessageFactory.lc("apPageCannotBeBothParentAndChild", new Object[] { pdata.m_pageId, newParent.m_pageId }));
/*      */ 
/* 1247 */           isBadParent = true;
/* 1248 */           break;
/*      */         }
/* 1250 */         ++treeDepth;
/* 1251 */         if (treeDepth <= 50)
/*      */           continue;
/* 1253 */         throw new DataException(null, "apTreeDepthIsTooGreat", new Object[0]);
/*      */       }
/*      */ 
/* 1256 */       if (isBadParent)
/*      */         break label421;
/* 1258 */       pdata.m_parent = newParent;
/* 1259 */       pdata.m_binder.putLocal("PageParent", val);
/* 1260 */       this.m_childParentMap.put(pdata.m_pageId, pdata.m_parent);
/*      */     }
/*      */ 
/* 1267 */     label421: return true;
/*      */   }
/*      */ 
/*      */   public void finalizeObject(DataExchange exch, boolean writeToResultSet)
/*      */     throws DataException
/*      */   {
/* 1276 */     PageData pdata = (PageData)exch.m_curObj;
/* 1277 */     if (this.m_exchType != 0)
/*      */       return;
/* 1279 */     this.m_indexedPageList.put(pdata.m_pageId, pdata);
/* 1280 */     this.m_pageList.addElement(pdata);
/*      */   }
/*      */ 
/*      */   public int compare(Object obj1, Object obj2)
/*      */   {
/* 1291 */     int parCount1 = 0; int parCount2 = 0;
/*      */ 
/* 1293 */     PageData pdata1 = (PageData)obj1;
/* 1294 */     PageData pdata2 = (PageData)obj2;
/*      */ 
/* 1297 */     this.m_parList1[(parCount1++)] = pdata1;
/* 1298 */     for (PageData temp = pdata1.m_parent; temp != null; temp = temp.m_parent)
/*      */     {
/* 1300 */       this.m_parList1[(parCount1++)] = temp;
/* 1301 */       if (parCount1 == 16) {
/*      */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1307 */     this.m_parList2[(parCount2++)] = pdata2;
/* 1308 */     for (temp = pdata2.m_parent; temp != null; temp = temp.m_parent)
/*      */     {
/* 1310 */       this.m_parList2[(parCount2++)] = temp;
/* 1311 */       if (parCount2 == 16) {
/*      */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1317 */     int min = parCount1;
/* 1318 */     if (min > parCount2)
/*      */     {
/* 1320 */       min = parCount2;
/*      */     }
/*      */ 
/* 1323 */     for (int i = 0; i < min; ++i)
/*      */     {
/* 1325 */       int result = this.m_parList1[(--parCount1)].m_pageId.toLowerCase().compareTo(this.m_parList2[(--parCount2)].m_pageId.toLowerCase());
/*      */ 
/* 1327 */       if (result != 0)
/*      */       {
/* 1329 */         return result;
/*      */       }
/*      */     }
/*      */ 
/* 1333 */     if (parCount1 < parCount2)
/*      */     {
/* 1335 */       return -1;
/*      */     }
/* 1337 */     if (parCount1 > parCount2)
/*      */     {
/* 1339 */       return 1;
/*      */     }
/* 1341 */     return 0;
/*      */   }
/*      */ 
/*      */   public void update(Observable obs, Object arg)
/*      */   {
/* 1351 */     boolean hasChanged = true;
/* 1352 */     if ((arg != null) && (arg instanceof DataBinder))
/*      */     {
/* 1354 */       DataBinder binder = (DataBinder)arg;
/* 1355 */       String subject = binder.getLocal("subjectNotifyChanged");
/* 1356 */       if ((subject != null) && (subject.equals("users")))
/*      */       {
/* 1359 */         UserData curUser = getUserData();
/* 1360 */         hasChanged = false;
/* 1361 */         if ((curUser != null) && (this.m_readPageAccess != null) && (this.m_adminPageAccess != null))
/*      */         {
/*      */           try
/*      */           {
/* 1365 */             UserDocumentAccessFilter readPageAccess = SecurityUtils.getUserDocumentAccessFilter(curUser, 1);
/*      */ 
/* 1368 */             UserDocumentAccessFilter adminPageAccess = SecurityUtils.getUserDocumentAccessFilter(curUser, 8);
/*      */ 
/* 1371 */             if ((!this.m_readPageAccess.isEqualSecurity(readPageAccess)) || (!this.m_adminPageAccess.isEqualSecurity(adminPageAccess)))
/*      */             {
/* 1374 */               this.m_readPageAccess = readPageAccess;
/* 1375 */               this.m_adminPageAccess = adminPageAccess;
/* 1376 */               hasChanged = true;
/*      */             }
/*      */           }
/*      */           catch (Exception e)
/*      */           {
/* 1381 */             e.printStackTrace();
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1386 */     if ((!hasChanged) || (this.m_isDirty == true))
/*      */     {
/* 1388 */       return;
/*      */     }
/* 1390 */     this.m_isDirty = true;
/* 1391 */     repaint();
/*      */   }
/*      */ 
/*      */   protected DataBinder createRequestObject(String function, String pageId)
/*      */   {
/* 1400 */     DataBinder rData = new DataBinder();
/* 1401 */     rData.putLocal("PageFunction", function);
/* 1402 */     if (pageId != null)
/*      */     {
/* 1404 */       rData.putLocal("PageName", pageId);
/*      */     }
/* 1406 */     return rData;
/*      */   }
/*      */ 
/*      */   public static int getFieldIndex(String field, ResultSet rset)
/*      */   {
/* 1411 */     FieldInfo finfo = new FieldInfo();
/*      */ 
/* 1414 */     rset.getFieldInfo(field, finfo);
/* 1415 */     return finfo.m_index;
/*      */   }
/*      */ 
/*      */   public static String getBinderProperty(DataBinder data, String key)
/*      */   {
/* 1420 */     Properties ldata = data.getLocalData();
/* 1421 */     return ldata.getProperty(key);
/*      */   }
/*      */ 
/*      */   public void updatePageMaps(String pageId, DataBinder data, boolean isMerge)
/*      */     throws DataException
/*      */   {
/* 1429 */     data.removeLocal("PageParent");
/* 1430 */     PageData curParent = (PageData)this.m_childParentMap.get(pageId);
/* 1431 */     if (curParent != null)
/*      */     {
/* 1433 */       data.putLocal("PageParent", curParent.m_pageId);
/*      */     }
/*      */ 
/* 1437 */     int npages = this.m_pageList.size();
/*      */ 
/* 1439 */     for (int i = 0; i < npages; ++i)
/*      */     {
/* 1441 */       PageData pd = (PageData)this.m_pageList.elementAt(i);
/* 1442 */       if ((pd.m_parent == null) || (!pd.m_parent.m_pageId.equals(pageId)))
/*      */         continue;
/* 1444 */       pd.m_parent = null;
/* 1445 */       pd.m_binder.removeLocal("PageParent");
/*      */     }
/*      */ 
/* 1449 */     for (Enumeration ek = this.m_childParentMap.keys(); ek.hasMoreElements(); )
/*      */     {
/* 1451 */       String child = (String)ek.nextElement();
/* 1452 */       PageData parent = (PageData)this.m_childParentMap.get(child);
/* 1453 */       if (parent.m_pageId.equals(pageId))
/*      */       {
/* 1455 */         this.m_childParentMap.remove(child);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1460 */     if (!isMerge) {
/*      */       return;
/*      */     }
/* 1463 */     readFromResultset("PageMap", m_mapFields, 1, data);
/*      */   }
/*      */ 
/*      */   protected void executePageHandlerService(DataBinder data, boolean checkTimestamp)
/*      */     throws ServiceException
/*      */   {
/* 1471 */     if (checkTimestamp == true)
/*      */     {
/* 1473 */       String lastChanged = this.m_globalCache.getLocal("LastChanged");
/* 1474 */       if (lastChanged != null)
/*      */       {
/* 1476 */         data.putLocal("LastChanged", lastChanged);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1481 */     data.removeResultSet("SearchZoneField");
/* 1482 */     data.removeResultSet("SearchQueryOpMap");
/* 1483 */     data.removeResultSet("SearchDateField");
/*      */ 
/* 1485 */     AppLauncher.executeService("PAGE_HANDLER", data, this.m_appHelper);
/*      */ 
/* 1487 */     String hasChanged = data.getLocal("OutOfDate");
/* 1488 */     if (StringUtils.convertToBool(hasChanged, false))
/*      */     {
/* 1490 */       data.removeLocal("OutOfDate");
/* 1491 */       if (checkTimestamp)
/*      */       {
/* 1493 */         this.m_isDirty = true;
/* 1494 */         repaint();
/*      */ 
/* 1496 */         return;
/*      */       }
/*      */     }
/* 1499 */     if (!checkTimestamp) {
/*      */       return;
/*      */     }
/* 1502 */     String lastChanged = data.getLocal("LastChanged");
/* 1503 */     this.m_globalCache.putLocal("LastChanged", lastChanged);
/*      */   }
/*      */ 
/*      */   public void executePageService(String pageId, PageData data, String service, String statusMsg)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1512 */       boolean redisplayPages = false;
/* 1513 */       boolean isGet = service.equals("GetPage");
/* 1514 */       boolean isSave = service.equals("SavePage");
/*      */ 
/* 1516 */       boolean isDependentChanged = data.m_binder.getLocal("PageChanged") != null;
/*      */ 
/* 1519 */       DataBinder bdata = data.m_binder.createShallowCopy();
/* 1520 */       bdata.putLocal("PageFunction", service);
/* 1521 */       bdata.putLocal("PageName", pageId);
/* 1522 */       if (isGet)
/*      */       {
/* 1526 */         bdata.clearResultSets();
/*      */       }
/* 1528 */       executePageHandlerService(bdata, true);
/*      */ 
/* 1531 */       if (this.m_indexedPageList.get(pageId) == null)
/*      */       {
/* 1533 */         this.m_pageList.addElement(data);
/* 1534 */         data.m_parent = ((PageData)this.m_childParentMap.get(pageId));
/* 1535 */         redisplayPages = true;
/*      */       }
/* 1537 */       this.m_indexedPageList.put(pageId, data);
/*      */ 
/* 1541 */       DataBinder curData = null;
/* 1542 */       if (isGet)
/*      */       {
/* 1544 */         curData = bdata;
/*      */       }
/* 1546 */       if (isSave)
/*      */       {
/* 1548 */         curData = data.m_binder;
/* 1549 */         if (!redisplayPages)
/*      */         {
/* 1551 */           redisplayPages = isDependentChanged;
/*      */         }
/*      */       }
/* 1554 */       if (curData != null)
/*      */       {
/* 1556 */         updatePageMaps(pageId, curData, true);
/*      */       }
/*      */ 
/* 1560 */       if (isGet)
/*      */       {
/* 1566 */         DataBinder pbinder = data.m_binder;
/* 1567 */         for (int i = 0; i < m_pageDataTables.length; ++i)
/*      */         {
/* 1569 */           ResultSet rs = bdata.getResultSet(m_pageDataTables[i]);
/* 1570 */           if (rs == null)
/*      */             continue;
/* 1572 */           pbinder.addResultSet(m_pageDataTables[i], rs);
/*      */         }
/*      */       }
/*      */ 
/* 1576 */       if ((isGet) || (isSave))
/*      */       {
/* 1578 */         String lastModified = bdata.getLocal("PageLastChanged");
/* 1579 */         if (lastModified != null)
/*      */         {
/* 1581 */           data.m_lastChanged = lastModified;
/*      */         }
/* 1583 */         data.m_isComplete = true;
/*      */       }
/*      */ 
/* 1586 */       if (redisplayPages)
/*      */       {
/* 1588 */         refreshPageList();
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*      */       String newMsg;
/* 1601 */       if (e.m_errorCode == -22)
/*      */       {
/* 1603 */         String addMsg = null;
/*      */         try
/*      */         {
/* 1606 */           this.m_isDirty = true;
/* 1607 */           repaint();
/*      */         }
/*      */         catch (Exception ge)
/*      */         {
/* 1612 */           addMsg = ge.getMessage();
/*      */         }
/* 1614 */         newMsg = e.getMessage();
/* 1615 */         if (addMsg != null)
/*      */         {
/* 1617 */           newMsg = newMsg + " " + addMsg;
/*      */         }
/*      */       }
/*      */ 
/* 1621 */       throw e;
/*      */     }
/*      */     finally
/*      */     {
/* 1627 */       this.m_appHelper.displayStatus(LocaleResources.getString("apLabelReady", this.m_ctx));
/*      */     }
/*      */   }
/*      */ 
/*      */   public SharedContext getSharedContext()
/*      */   {
/* 1633 */     return this;
/*      */   }
/*      */ 
/*      */   public void executeService(String action, DataBinder data, boolean isRefresh)
/*      */     throws ServiceException
/*      */   {
/* 1642 */     AppLauncher.executeService(action, data);
/*      */   }
/*      */ 
/*      */   public UserData getUserData()
/*      */   {
/* 1647 */     return AppLauncher.getUserData();
/*      */   }
/*      */ 
/*      */   public Object get(Object source, Object key)
/*      */   {
/* 1655 */     ValueNode item = (ValueNode)source;
/* 1656 */     PageData pData = (PageData)item.m_extraData;
/* 1657 */     if (key instanceof String)
/*      */     {
/* 1659 */       String type = (String)key;
/* 1660 */       if (type.equals("label"))
/*      */       {
/* 1662 */         return pData.m_pageId;
/*      */       }
/*      */ 
/* 1665 */       if (type.equals("OpenIcons"))
/*      */       {
/* 1667 */         type = "open:";
/*      */       }
/*      */       else
/*      */       {
/* 1671 */         type = "closed:";
/*      */       }
/*      */ 
/* 1674 */       Image[] img = { null };
/* 1675 */       img[0] = ((Image)m_images.get(type + pData.m_typeId));
/* 1676 */       if (img[0] != null)
/*      */       {
/* 1678 */         return new ImageIcon(img[0]);
/*      */       }
/*      */     }
/*      */ 
/* 1682 */     return null;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1687 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98072 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.PageBuilderFrame
 * JD-Core Version:    0.5.4
 */