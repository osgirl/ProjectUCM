/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataExchange;
/*     */ import intradoc.data.DataExchangeBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.IdcList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.gui.SecurityEditHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.ListModel;
/*     */ import javax.swing.ListSelectionModel;
/*     */ 
/*     */ public class EditDirPagePanel extends EditViewBase
/*     */   implements DataExchangeBinder, ActionListener
/*     */ {
/*     */   IdcList m_linksList;
/*     */   JButton m_edit;
/*     */   JButton m_add;
/*     */   JButton m_delete;
/*     */   JButton m_up;
/*     */   JButton m_down;
/*     */   JButton m_goto;
/*     */   int m_selectedLink;
/*     */   protected DataExchange m_linksExchange;
/*     */   protected DataResultSet m_linksInfo;
/*     */   protected DataResultSet m_pageMap;
/*     */   protected String m_curLinkData;
/* 100 */   private static final String[] m_linkListFields = { "LinkType", "LinkData", "LinkTitle", "LinkDescription" };
/*     */ 
/* 102 */   private static final String[] m_mapFields = { "PageName", "PageParent" };
/*     */ 
/* 105 */   private static boolean m_isLinkListLocalized = false;
/* 106 */   private static final String[][] m_linkTypes = { { "Local Page", "apLabelLocalPage" }, { "External URL", "apLabelExternalUrl" }, { "Query", "apLabelQuery" } };
/*     */ 
/*     */   public void initDisplay()
/*     */   {
/* 116 */     this.m_selectedLink = -1;
/*     */ 
/* 119 */     createStandardHeader();
/*     */ 
/* 122 */     JPanel linksPanel = createStandardPageEdit();
/* 123 */     linksPanel.setLayout(new BorderLayout());
/*     */ 
/* 125 */     JPanel linksListPanel = new PanePanel();
/* 126 */     linksListPanel.setLayout(new BorderLayout());
/*     */ 
/* 130 */     JPanel linksListHeaderPanel = new PanePanel();
/* 131 */     JPanel linksListHeaderWest = new PanePanel();
/* 132 */     JPanel linksListHeaderEast = new PanePanel();
/* 133 */     linksListHeaderWest.add(new CustomLabel(LocaleResources.getString("apLabelPageLinks", this.m_ctx), 2));
/*     */ 
/* 135 */     linksListHeaderEast.add(this.m_goto = new JButton(LocaleResources.getString("apLabelToChild", this.m_ctx)));
/* 136 */     this.m_goto.addActionListener(this);
/* 137 */     linksListHeaderEast.add(new CustomLabel("  "));
/* 138 */     linksListHeaderEast.add(this.m_up = new JButton(LocaleResources.getString("apLabelUp", this.m_ctx)));
/* 139 */     this.m_up.addActionListener(this);
/* 140 */     linksListHeaderEast.add(this.m_down = new JButton(LocaleResources.getString("apLabelDown", this.m_ctx)));
/* 141 */     this.m_down.addActionListener(this);
/*     */ 
/* 143 */     linksListHeaderPanel.setLayout(new BorderLayout());
/* 144 */     linksListHeaderPanel.add("West", linksListHeaderWest);
/* 145 */     linksListHeaderPanel.add("East", linksListHeaderEast);
/* 146 */     linksListPanel.add("North", linksListHeaderPanel);
/*     */ 
/* 148 */     FixedSizeList flist = new FixedSizeList(12, 400);
/* 149 */     this.m_linksList = flist.m_list;
/* 150 */     JScrollPane scrollPane = flist.m_scrollPane;
/* 151 */     linksListPanel.add("Center", scrollPane);
/* 152 */     flist.addItemListener(new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 156 */         EditDirPagePanel.this.enableOrDisable();
/*     */       }
/*     */     });
/* 160 */     JPanel linksListButtonsGroup = new PanePanel();
/*     */ 
/* 162 */     JPanel linksButtons = new PanePanel();
/* 163 */     linksButtons.setLayout(new GridLayout(0, 1));
/*     */ 
/* 165 */     linksButtons.add(new CustomLabel(""));
/* 166 */     linksButtons.add(new CustomLabel(""));
/* 167 */     linksButtons.add(this.m_add = new JButton(LocaleResources.getString("apDlgButtonAdd", this.m_ctx)));
/* 168 */     this.m_add.getAccessibleContext().setAccessibleName(LocaleResources.getString("apLabelAddPageLink", this.m_ctx));
/* 169 */     this.m_add.addActionListener(this);
/* 170 */     linksButtons.add(this.m_edit = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_ctx)));
/* 171 */     this.m_edit.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableButtonEditPageLink", this.m_ctx));
/* 172 */     this.m_edit.addActionListener(this);
/* 173 */     linksButtons.add(this.m_delete = new JButton(LocaleResources.getString("apLabelDelete", this.m_ctx)));
/* 174 */     this.m_delete.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableButtonDeletePageLink", this.m_ctx));
/* 175 */     this.m_delete.addActionListener(this);
/*     */ 
/* 177 */     ActionListener listListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 181 */         EditDirPagePanel.this.actionByObject(EditDirPagePanel.this.m_edit);
/*     */       }
/*     */     };
/* 184 */     this.m_linksList.addActionListener(listListener);
/* 185 */     this.m_linksList.setEnabled(true);
/*     */ 
/* 187 */     linksListButtonsGroup.add(linksButtons);
/*     */ 
/* 189 */     linksPanel.add("Center", linksListPanel);
/* 190 */     linksPanel.add("East", linksListButtonsGroup);
/*     */   }
/*     */ 
/*     */   public void addPageLink()
/*     */   {
/* 196 */     DialogHelper helper = new DialogHelper(this.m_containerHelper.m_exchange.m_sysInterface, LocaleResources.getString("apLabelAddPageLink", this.m_ctx), true);
/*     */ 
/* 198 */     JPanel mainPanel = helper.initStandard(null, null, 2, true, DialogHelpTable.getHelpPage("AddPageLink"));
/*     */ 
/* 200 */     DisplayChoice linkTypes = new DisplayChoice();
/* 201 */     String[][] types = getLinkTypesList();
/* 202 */     linkTypes.init(types);
/* 203 */     helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelLinkType", this.m_ctx), linkTypes, "LinkType");
/*     */ 
/* 206 */     if (helper.prompt() != 1)
/*     */       return;
/* 208 */     String linkType = helper.m_props.getProperty("LinkType");
/*     */ 
/* 211 */     Vector linkInfo = createNewLinkInfo();
/* 212 */     linkInfo.setElementAt(linkType, 0);
/*     */ 
/* 214 */     edit(linkInfo, true);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent evt)
/*     */   {
/* 221 */     Object target = evt.getSource();
/* 222 */     actionByObject(target);
/*     */   }
/*     */ 
/*     */   protected void actionByObject(Object target)
/*     */   {
/* 227 */     int sel = this.m_linksList.getSelectedIndex();
/* 228 */     if (sel >= 0)
/*     */     {
/* 230 */       this.m_linksInfo.setCurrentRow(sel);
/*     */     }
/*     */ 
/* 233 */     if (this.m_isEditAllowed)
/*     */     {
/* 235 */       if (target == this.m_add)
/*     */       {
/* 237 */         addPageLink();
/*     */       }
/* 239 */       else if (target == this.m_edit)
/*     */       {
/* 241 */         Vector linkInfo = this.m_linksInfo.getRowValues(sel);
/* 242 */         edit(linkInfo, false);
/*     */       }
/* 244 */       else if (target == this.m_delete)
/*     */       {
/* 246 */         Vector linkInfo = this.m_linksInfo.getRowValues(sel);
/* 247 */         if ((linkInfo != null) && (isLocalPageLink(linkInfo)))
/*     */         {
/* 249 */           String linkData = getLinkData(linkInfo);
/* 250 */           addIfPageReference("PageChanged", linkData);
/*     */         }
/*     */ 
/* 253 */         this.m_linksInfo.deleteCurrentRow();
/*     */ 
/* 255 */         if (!savePage(IdcMessageFactory.lc("apErrorSavingLinkDelete", new Object[0])))
/*     */         {
/* 257 */           this.m_linksInfo.insertRowAt(linkInfo, sel);
/* 258 */           loadLinks();
/*     */         }
/*     */       }
/* 261 */       else if ((target == this.m_up) || (target == this.m_down))
/*     */       {
/*     */         int newSel;
/*     */         int newSel;
/* 264 */         if (target == this.m_up)
/*     */         {
/* 266 */           newSel = sel - 1;
/*     */         }
/*     */         else
/*     */         {
/* 270 */           newSel = sel + 1;
/*     */         }
/* 272 */         if ((newSel >= 0) && (newSel < this.m_linksInfo.getNumRows()))
/*     */         {
/* 274 */           Vector linkInfo = this.m_linksInfo.getRowValues(sel);
/* 275 */           this.m_linksInfo.deleteCurrentRow();
/* 276 */           this.m_linksInfo.insertRowAt(linkInfo, newSel);
/* 277 */           this.m_linksList.setSelectedIndex(newSel);
/* 278 */           savePage(IdcMessageFactory.lc("apErrorSavingLinkMove", new Object[0]));
/*     */         }
/*     */       }
/*     */     }
/* 282 */     if (target != this.m_goto)
/*     */       return;
/* 284 */     Vector linkInfo = this.m_linksInfo.getRowValues(sel);
/* 285 */     String childPage = getLinkData(linkInfo);
/*     */     try
/*     */     {
/* 288 */       this.m_pageContext.loadEditView(childPage, this.m_data.m_pageId, true);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 293 */       reportError(e, IdcMessageFactory.lc("apErrorGoToChildPage", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadPageView()
/*     */   {
/* 306 */     this.m_linksExchange = buildExchange("LinkList", m_linkListFields, false);
/* 307 */     this.m_linksExchange.appendMissingFields();
/* 308 */     this.m_linksInfo = ((DataResultSet)this.m_linksExchange.m_rset);
/*     */ 
/* 311 */     DefaultListModel model = (DefaultListModel)this.m_linksList.getModel();
/* 312 */     model.removeAllElements();
/* 313 */     loadLinks();
/*     */   }
/*     */ 
/*     */   public String[] getFieldNames()
/*     */   {
/* 321 */     return m_linkListFields;
/*     */   }
/*     */ 
/*     */   public Vector createNewLinkInfo()
/*     */   {
/*     */     try
/*     */     {
/* 328 */       return this.m_linksInfo.createRow(null);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 332 */       if (SystemUtils.m_verbose)
/*     */       {
/* 334 */         Report.debug("applet", null, e);
/*     */       }
/*     */     }
/* 337 */     return null;
/*     */   }
/*     */ 
/*     */   public void edit(Vector linkInfo, boolean isNew)
/*     */   {
/* 343 */     String linkType = (String)linkInfo.elementAt(0);
/* 344 */     EditLinkBaseDlg dlg = null;
/* 345 */     SystemInterface sys = this.m_containerHelper.m_exchange.m_sysInterface;
/* 346 */     if (linkType.equals("Query"))
/*     */     {
/* 348 */       UserData userData = this.m_editPageHelper.m_securityEditHelper.m_userData;
/* 349 */       DocumentLocalizedProfile docProfile = new DocumentLocalizedProfile(userData, 1, this.m_ctx);
/*     */ 
/* 351 */       dlg = new EditQueryDlg(sys, this.m_data, docProfile, this.m_pageContext, linkInfo, isNew);
/*     */     }
/*     */     else
/*     */     {
/* 357 */       String[][] pageTypes = getLinkTypesList();
/* 358 */       boolean isLocal = pageTypes[0][0].equals(linkInfo.elementAt(0));
/*     */ 
/* 361 */       String helpPageName = "EditLocalPageLink";
/*     */ 
/* 363 */       if (!isLocal)
/*     */       {
/* 365 */         helpPageName = "EditExternalURL";
/*     */       }
/*     */ 
/* 368 */       EditUrlReferenceDlg urlDlg = new EditUrlReferenceDlg(isLocal, this.m_data, sys, this.m_pageContext, linkInfo, isNew, DialogHelpTable.getHelpPage(helpPageName));
/*     */ 
/* 370 */       urlDlg.m_editPageHelper = new EditPageHelper();
/* 371 */       initSecurityHelper(urlDlg.m_editPageHelper, urlDlg.m_helper, urlDlg.m_sysInterface);
/* 372 */       dlg = urlDlg;
/*     */     }
/*     */ 
/* 375 */     EditLinkBaseDlg dlgParam = dlg;
/* 376 */     DialogCallback okCallback = new DialogCallback(dlgParam)
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 382 */         EditDirPagePanel.this.save(this.val$dlgParam.m_linkInfo, this.val$dlgParam.m_oldLinkInfo, this.val$dlgParam.m_isNew);
/* 383 */         return true;
/*     */       }
/*     */     };
/* 386 */     dlg.prompt(okCallback);
/*     */   }
/*     */ 
/*     */   public void save(Vector linkInfo, Vector oldLinkInfo, boolean isNew)
/*     */   {
/* 391 */     handleContentChange(linkInfo, oldLinkInfo, isNew);
/* 392 */     if (savePage(IdcMessageFactory.lc("apErrorSavingPageUpdate", new Object[0])))
/*     */       return;
/* 394 */     if (isNew)
/*     */     {
/* 396 */       this.m_linksInfo.deleteRow(this.m_linksInfo.getNumRows() - 1);
/*     */     }
/*     */     else
/*     */     {
/* 400 */       int size = linkInfo.size();
/* 401 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 403 */         linkInfo.setElementAt(oldLinkInfo.elementAt(i), i);
/*     */       }
/*     */     }
/* 406 */     loadLinks();
/*     */   }
/*     */ 
/*     */   protected String[][] getLinkTypesList()
/*     */   {
/* 412 */     if (!m_isLinkListLocalized)
/*     */     {
/* 414 */       LocaleResources.localizeStaticDoubleArray(m_linkTypes, this.m_ctx, 1);
/* 415 */       m_isLinkListLocalized = true;
/*     */     }
/* 417 */     return m_linkTypes;
/*     */   }
/*     */ 
/*     */   public boolean prepareNextRow(DataExchange exch, boolean writeToResultSet)
/*     */   {
/* 426 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean exchange(DataExchange exch, int index, boolean writeToResultSet)
/*     */     throws DataException
/*     */   {
/* 433 */     String val = exch.getCurValAsString();
/* 434 */     if (val == null)
/*     */     {
/* 436 */       throw new DataException(LocaleResources.getString("apNullFieldInLink", this.m_ctx, exch.m_curFieldInfo.m_name));
/*     */     }
/*     */ 
/* 441 */     if (index == 0)
/*     */     {
/* 444 */       exch.m_curObj = val;
/*     */     }
/* 446 */     else if (index == 1)
/*     */     {
/* 448 */       this.m_curLinkData = val;
/*     */     }
/* 450 */     else if (index == 2)
/*     */     {
/* 453 */       String linkType = (String)exch.m_curObj;
/* 454 */       String displayChoice = null;
/*     */ 
/* 456 */       if (linkType.equals(m_linkTypes[0][0]))
/*     */       {
/* 458 */         Vector mapEntries = new IdcVector();
/* 459 */         mapEntries.setSize(2);
/* 460 */         mapEntries.setElementAt(this.m_curLinkData, 0);
/* 461 */         mapEntries.setElementAt(this.m_data.m_pageId, 1);
/* 462 */         this.m_pageMap.addRow(mapEntries);
/* 463 */         if ((this.m_selectedLink == -1) && (this.m_curChildPage != null) && (this.m_curChildPage.equals(this.m_curLinkData)))
/*     */         {
/* 466 */           DataResultSet drset = (DataResultSet)exch.m_rset;
/* 467 */           this.m_selectedLink = drset.getCurrentRow();
/*     */         }
/* 469 */         if (this.m_pageContext.getPage(this.m_curLinkData) != null)
/*     */         {
/* 471 */           displayChoice = LocaleResources.getString("apLabelPage", this.m_ctx) + ": " + this.m_curLinkData;
/*     */         }
/*     */         else
/*     */         {
/* 476 */           displayChoice = LocaleResources.getString("apLabelDeletedPage", this.m_ctx) + ": " + this.m_curLinkData;
/*     */         }
/*     */ 
/*     */       }
/* 480 */       else if (linkType.equals(m_linkTypes[1][0]))
/*     */       {
/* 482 */         displayChoice = this.m_curLinkData;
/*     */       }
/*     */       else
/*     */       {
/* 486 */         displayChoice = createCustomDisplayChoice(linkType, this.m_curLinkData);
/*     */       }
/* 488 */       displayChoice = val + " (" + displayChoice + ")";
/*     */ 
/* 490 */       DefaultListModel model = (DefaultListModel)this.m_linksList.getModel();
/* 491 */       model.addElement(displayChoice);
/*     */     }
/*     */ 
/* 494 */     return true;
/*     */   }
/*     */ 
/*     */   public void finalizeObject(DataExchange exch, boolean writeToResultSet)
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   protected DataExchange buildExchange(String rsetName, String[] fields, boolean isNew)
/*     */   {
/* 510 */     ResultSet rset = null;
/* 511 */     if (!isNew)
/*     */     {
/* 513 */       rset = this.m_binder.getResultSet(rsetName);
/*     */ 
/* 517 */       if (rset != null)
/*     */       {
/*     */         try
/*     */         {
/* 521 */           FieldInfo[] finfo = ResultSetUtils.createInfoList(rset, fields, false);
/* 522 */           for (int i = 0; i < finfo.length; ++i)
/*     */           {
/* 524 */             if (finfo[i].m_index == i)
/*     */               continue;
/* 526 */             rset = null;
/* 527 */             break;
/*     */           }
/*     */ 
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 533 */           rset = null;
/*     */         }
/*     */       }
/*     */     }
/* 537 */     if (rset == null)
/*     */     {
/* 539 */       rset = new DataResultSet(fields);
/* 540 */       this.m_binder.addResultSet(rsetName, rset);
/*     */     }
/*     */ 
/* 543 */     return new DataExchange(rset, fields);
/*     */   }
/*     */ 
/*     */   protected boolean savePage(IdcMessage errMsg)
/*     */   {
/* 548 */     loadLinks();
/*     */     try
/*     */     {
/* 552 */       this.m_pageContext.saveData(this.m_data.m_pageId, this.m_data);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 556 */       reportError(e, errMsg);
/* 557 */       return false;
/*     */     }
/*     */ 
/* 560 */     return true;
/*     */   }
/*     */ 
/*     */   protected void handleContentChange(Vector linkInfo, Vector oldLinkInfo, boolean isNew)
/*     */   {
/* 565 */     if (linkInfo == null)
/*     */       return;
/* 567 */     if (isLocalPageLink(linkInfo))
/*     */     {
/* 569 */       boolean isChanged = true;
/* 570 */       String linkData = getLinkData(linkInfo);
/* 571 */       if (!isNew)
/*     */       {
/* 573 */         String oldLinkData = getLinkData(oldLinkInfo);
/* 574 */         isChanged = !oldLinkData.equals(linkData);
/* 575 */         if (isChanged)
/*     */         {
/* 577 */           addIfPageReference("OldPageChanged", oldLinkData);
/*     */         }
/*     */       }
/* 580 */       if (isChanged)
/*     */       {
/* 582 */         addIfPageReference("PageChanged", linkData);
/*     */       }
/*     */     }
/* 585 */     if (isNew != true)
/*     */       return;
/* 587 */     this.m_linksInfo.addRow(linkInfo);
/* 588 */     this.m_binder.putLocal("LinkSelectedIndex", Integer.toString(this.m_linksInfo.getNumRows() - 1));
/*     */ 
/* 590 */     int curSel = this.m_linksList.getSelectedIndex();
/* 591 */     if (curSel < 0)
/*     */       return;
/* 593 */     this.m_linksList.getSelectionModel().removeSelectionInterval(curSel, curSel);
/*     */   }
/*     */ 
/*     */   protected void loadLinks()
/*     */   {
/*     */     try
/*     */     {
/* 603 */       this.m_pageMap = new DataResultSet(m_mapFields);
/* 604 */       this.m_binder.addResultSet("PageMap", this.m_pageMap);
/* 605 */       this.m_selectedLink = this.m_linksList.getSelectedIndex();
/* 606 */       DefaultListModel model = (DefaultListModel)this.m_linksList.getModel();
/* 607 */       model.removeAllElements();
/* 608 */       this.m_linksExchange.doExchange(this, false);
/*     */ 
/* 610 */       if (this.m_selectedLink < 0)
/*     */       {
/* 612 */         String prevChoice = this.m_binder.getLocal("LinkSelectedIndex");
/* 613 */         if (prevChoice != null)
/*     */         {
/* 615 */           this.m_selectedLink = Integer.parseInt(prevChoice);
/*     */         }
/*     */       }
/*     */ 
/* 619 */       if ((this.m_selectedLink >= 0) && (this.m_selectedLink < this.m_linksList.getModel().getSize()))
/*     */       {
/* 621 */         this.m_linksList.getSelectionModel().setSelectionInterval(this.m_selectedLink, this.m_selectedLink);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 626 */       reportError(e, IdcMessageFactory.lc("apErrorLoadingPageLinks", new Object[0]));
/*     */     }
/*     */ 
/* 629 */     enableOrDisable();
/*     */   }
/*     */ 
/*     */   protected String createCustomDisplayChoice(String linkType, String val)
/*     */   {
/* 635 */     if (linkType.equals("Query"))
/*     */     {
/* 644 */       return LocaleResources.getString("apLabelContentQuery", this.m_ctx);
/*     */     }
/* 646 */     return LocaleResources.getString("apLabelUnknownLink", this.m_ctx, val);
/*     */   }
/*     */ 
/*     */   public void updatePrivilegeState()
/*     */   {
/* 652 */     this.m_add.setVisible(this.m_isEditAllowed);
/* 653 */     this.m_edit.setVisible(this.m_isEditAllowed);
/* 654 */     this.m_delete.setVisible(this.m_isEditAllowed);
/* 655 */     this.m_up.setVisible(this.m_isEditAllowed);
/* 656 */     this.m_down.setVisible(this.m_isEditAllowed);
/*     */ 
/* 659 */     super.updatePrivilegeState();
/*     */   }
/*     */ 
/*     */   public void enableOrDisable()
/*     */   {
/* 665 */     int sel = this.m_linksList.getSelectedIndex();
/* 666 */     boolean isSelected = sel >= 0;
/* 667 */     this.m_edit.setEnabled(isSelected);
/* 668 */     this.m_delete.setEnabled(isSelected);
/*     */ 
/* 670 */     int nlinks = this.m_linksList.getModel().getSize();
/* 671 */     this.m_up.setEnabled((isSelected) && (sel > 0));
/* 672 */     this.m_down.setEnabled((isSelected) && (sel < nlinks - 1));
/*     */ 
/* 674 */     boolean isChildPage = false;
/* 675 */     if (isSelected)
/*     */     {
/* 677 */       this.m_linksInfo.setCurrentRow(sel);
/* 678 */       this.m_linksExchange.setCurrentField(0);
/* 679 */       String linkType = this.m_linksExchange.getCurValAsString();
/* 680 */       isChildPage = linkType.equals(m_linkTypes[0][0]);
/* 681 */       if (isChildPage)
/*     */       {
/* 683 */         this.m_linksExchange.setCurrentField(1);
/* 684 */         String childId = this.m_linksExchange.getCurValAsString();
/* 685 */         isChildPage = this.m_pageContext.getPage(childId) != null;
/*     */       }
/* 687 */       this.m_binder.putLocal("LinkSelectedIndex", Integer.toString(sel));
/*     */     }
/* 689 */     this.m_goto.setEnabled(isChildPage);
/*     */ 
/* 691 */     super.enableOrDisable();
/*     */   }
/*     */ 
/*     */   protected boolean isLocalPageLink(Vector linkInfo)
/*     */   {
/* 699 */     String type = (String)linkInfo.elementAt(0);
/* 700 */     return type.equals(m_linkTypes[0][0]);
/*     */   }
/*     */ 
/*     */   protected void addIfPageReference(String key, String page)
/*     */   {
/* 705 */     if (this.m_pageContext.getPage(page) == null)
/*     */       return;
/* 707 */     this.m_binder.putLocal(key, page);
/*     */   }
/*     */ 
/*     */   protected String getLinkData(Vector linkInfo)
/*     */   {
/* 713 */     return (String)linkInfo.elementAt(1);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 718 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96416 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditDirPagePanel
 * JD-Core Version:    0.5.4
 */