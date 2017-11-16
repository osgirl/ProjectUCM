/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
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
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddResourceDlg extends CWizardBaseDlg
/*     */   implements ActionListener, ResourceWizard
/*     */ {
/*     */   protected Hashtable m_flipComponents;
/*  53 */   protected String m_curPanelName = null;
/*  54 */   protected String m_nextPanelName = null;
/*  55 */   protected String m_backPanelName = null;
/*     */   protected JPanel m_flipPanel;
/*  59 */   protected EditBasePanel m_curView = null;
/*  60 */   public JButton m_backBtn = null;
/*  61 */   public JButton m_nextBtn = null;
/*  62 */   public JButton m_finishBtn = null;
/*     */ 
/*  67 */   public static final String[][] PANEL_ORDER = { { "htmlIncludeOrString", "resource,htmlIncludeOrString" }, { "dynResTable", "resource,dynResTable" }, { "staticResTable", "resource,staticResTable" }, { "query", "resource,queryTable,query" }, { "service", "resource,serviceTable,service" }, { "template", "resource,templateTable,templateIntradocTemplates:templateSearchResultTemplates" }, { "environment", "resource" } };
/*     */ 
/*  80 */   public final String[][] FLIP_COMP_LIST = { { "resource", "intradoc.apputilities.componentwizard.EditResourceInfoPanel", "resource", "", "!csCompWizFlipPanelAddResourceDesc", "CW_AddResource" }, { "templateTable", "intradoc.apputilities.componentwizard.EditTableInfoPanel", "resource", "template", "!csCompWizFlipPanelAddTemplateDesc", "CW_TemplateTableInfo" }, { "dynResTable", "intradoc.apputilities.componentwizard.EditTableInfoPanel", "resource", "resource", "!csCompWizFlipPanelAddDynResourceDesc", "CW_DynamicTableInfo" }, { "staticResTable", "intradoc.apputilities.componentwizard.EditTableInfoPanel", "resource", "resource", "!csCompWizFlipPanelAddStaticResourceDesc", "CW_StaticTableInfo" }, { "queryTable", "intradoc.apputilities.componentwizard.EditTableInfoPanel", "resource", "query", "!csCompWizFlipPanelAddQueryTableDesc", "CW_QueryTableInfo" }, { "serviceTable", "intradoc.apputilities.componentwizard.EditTableInfoPanel", "resource", "service", "!csCompWizFlipPanelAddServiceTableDesc", "CW_ServiceTableInfo" }, { "htmlIncludeOrString", "intradoc.apputilities.componentwizard.EditHtmlIncludePanel", "htmlIncludeOrString", "", "!csCompWizFlipPanelAddHTMLResourceDesc", "CW_AddEditInclude" }, { "service", "intradoc.apputilities.componentwizard.EditServicePanel", "service", "", "!csCompWizFlipPanelAddServiceDesc", "CW_AddEditService" }, { "templateIntradocTemplates", "intradoc.apputilities.componentwizard.EditTemplatePanel", "template", "IntradocTemplates", "!csCompWizFlipPanelAddIntradocTemplateDesc", "CW_AddEditIntradocTemplate" }, { "templateSearchResultTemplates", "intradoc.apputilities.componentwizard.EditTemplatePanel", "template", "SearchResultTemplates", "!csCompWizFlipPanelAddSearchResTemplDesc", "CW_AddEditResultTemplate" }, { "query", "intradoc.apputilities.componentwizard.EditQueryPanel", "query", "", "!csCompWizFlipPanelAddQueryDesc", "CW_AddEditQuery" } };
/*     */ 
/*     */   public AddResourceDlg(SystemInterface sys, String title, String helpPage, IntradocComponent comp)
/*     */   {
/* 110 */     super(sys, title, helpPage);
/* 111 */     this.m_helper.m_componentBinder = this;
/*     */ 
/* 114 */     this.m_helper.m_props.put("ComponentName", comp.m_name);
/* 115 */     this.m_component = comp;
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel mainPanel)
/*     */   {
/*     */     try
/*     */     {
/* 123 */       createToolBar();
/* 124 */       initInfoPanel(mainPanel);
/*     */ 
/* 126 */       String type = this.m_helper.m_props.getProperty("type");
/* 127 */       if ((type == null) || (type.length() == 0))
/*     */       {
/* 129 */         type = "htmlIncludeOrString";
/*     */       }
/*     */ 
/* 132 */       determineEnableDisable(type, null, this.m_curPanelName);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 136 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void createToolBar()
/*     */   {
/* 143 */     String[][] BUTTON_INFO = { { "csCompWizAddResourceButtonBack", "back" }, { "csCompWizAddResourceButtonNext", "next" }, { "csCompWizAddResourceButtonFinish", "finish" }, { "csCompWizAddResourceButtonCancel", "cancel" }, { "csCompWizAddResourceButtonHelp", "help" } };
/*     */ 
/* 152 */     for (int i = 0; i < BUTTON_INFO.length; ++i)
/*     */     {
/* 154 */       String cmd = BUTTON_INFO[i][1];
/* 155 */       JButton btn = getDialogHelper().addCommandButton(LocaleResources.getString(BUTTON_INFO[i][0], null), this);
/* 156 */       btn.setActionCommand(cmd);
/*     */ 
/* 158 */       if (cmd.equals("back"))
/*     */       {
/* 160 */         this.m_backBtn = btn;
/*     */       }
/* 162 */       else if (cmd.equals("next"))
/*     */       {
/* 164 */         this.m_nextBtn = btn;
/*     */       } else {
/* 166 */         if (!cmd.equals("finish"))
/*     */           continue;
/* 168 */         this.m_finishBtn = btn;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initInfoPanel(JPanel mainPanel) throws ServiceException
/*     */   {
/* 175 */     this.m_flipPanel = new PanePanel();
/* 176 */     this.m_flipComponents = new Hashtable();
/* 177 */     this.m_curPanelName = "resource";
/*     */ 
/* 179 */     CardLayout flipLayout = new CardLayout();
/* 180 */     this.m_flipPanel.setLayout(flipLayout);
/*     */ 
/* 182 */     for (int i = 0; i < this.FLIP_COMP_LIST.length; ++i)
/*     */     {
/* 184 */       String name = this.FLIP_COMP_LIST[i][0];
/* 185 */       EditBasePanel comp = (EditBasePanel)ComponentClassFactory.createClassInstance(name, this.FLIP_COMP_LIST[i][1], LocaleUtils.encodeMessage("csCompWizLoadPanelError", null, this.FLIP_COMP_LIST[i][0]));
/*     */ 
/* 188 */       comp.init(this.m_component, this.FLIP_COMP_LIST[i][3], this.m_helper, this, 0);
/* 189 */       this.m_flipPanel.add(name, comp);
/* 190 */       this.m_flipComponents.put(name, comp);
/*     */ 
/* 192 */       if (i != 0)
/*     */         continue;
/* 194 */       this.m_curView = comp;
/*     */     }
/*     */ 
/* 198 */     flipLayout.show(this.m_flipPanel, this.m_curPanelName);
/* 199 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 200 */     this.m_helper.addComponent(mainPanel, this.m_flipPanel);
/*     */   }
/*     */ 
/*     */   public void setResourceType(String cwtype)
/*     */   {
/* 205 */     this.m_helper.m_props.put("type", cwtype);
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/* 210 */     getDialogHelper().close();
/*     */   }
/*     */ 
/*     */   public String getComponentName() {
/* 214 */     return this.m_helper.m_props.getProperty("type");
/*     */   }
/*     */ 
/*     */   public void setEnableDisable(boolean isBack, boolean isNext, boolean isFinish)
/*     */   {
/* 224 */     this.m_backBtn.setEnabled(isBack);
/* 225 */     this.m_nextBtn.setEnabled(isNext);
/* 226 */     this.m_finishBtn.setEnabled(isFinish);
/*     */   }
/*     */ 
/*     */   public void determineEnableDisable(String cwType, String mergetable, String curPanelName)
/*     */   {
/* 232 */     String orderList = StringUtils.findString(PANEL_ORDER, cwType, 0, 1);
/* 233 */     Vector v = StringUtils.parseArray(orderList, ',', ',');
/* 234 */     int size = v.size();
/* 235 */     int i = 0;
/*     */ 
/* 237 */     boolean isBack = false;
/* 238 */     boolean isNext = false;
/* 239 */     boolean isFinish = false;
/*     */ 
/* 243 */     if ((cwType.equals("template")) && (((curPanelName.equals("templateIntradocTemplates")) || (curPanelName.equals("templateSearchResultTemplates")))))
/*     */     {
/* 247 */       curPanelName = "templateIntradocTemplates:templateSearchResultTemplates";
/*     */     }
/*     */ 
/* 250 */     for (i = 0; i < v.size(); ++i)
/*     */     {
/* 252 */       String tempStr = (String)v.elementAt(i);
/* 253 */       if (tempStr.trim().equals(curPanelName)) {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 259 */     if (i < size - 1)
/*     */     {
/* 261 */       isNext = true;
/* 262 */       String nextPanel = (String)v.elementAt(i + 1);
/*     */ 
/* 265 */       if ((cwType.equals("template")) && (curPanelName.equals("templateTable")))
/*     */       {
/* 267 */         if ((mergetable == null) || (mergetable.length() == 0))
/*     */         {
/* 269 */           mergetable = "IntradocTemplates";
/*     */         }
/*     */ 
/* 272 */         this.m_nextPanelName = (cwType + mergetable);
/*     */       }
/*     */       else
/*     */       {
/* 276 */         this.m_nextPanelName = nextPanel;
/*     */       }
/*     */     }
/*     */ 
/* 280 */     if (i > 0)
/*     */     {
/* 282 */       isBack = true;
/* 283 */       this.m_backPanelName = ((String)v.elementAt(i - 1));
/*     */     }
/*     */ 
/* 286 */     if (i == size - 1)
/*     */     {
/* 288 */       isFinish = true;
/*     */     }
/*     */ 
/* 291 */     setEnableDisable(isBack, isNext, isFinish);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 301 */     String cmd = e.getActionCommand();
/* 302 */     this.m_helper.retrieveComponentValues();
/*     */ 
/* 304 */     if (cmd.equals("back"))
/*     */     {
/* 306 */       goBackOrNext(false);
/*     */     }
/* 308 */     else if (cmd.equals("next"))
/*     */     {
/* 310 */       goBackOrNext(true);
/*     */     }
/* 312 */     else if (cmd.equals("finish"))
/*     */     {
/* 314 */       finish();
/*     */     }
/* 316 */     else if (cmd.equals("cancel"))
/*     */     {
/* 318 */       DialogHelper dialogHelper = getDialogHelper();
/* 319 */       dialogHelper.m_result = 0;
/* 320 */       dialogHelper.close();
/*     */     }
/* 322 */     else if (cmd.equals("help"))
/*     */     {
/* 324 */       CWizardGuiUtils.launchHelp(this.m_systemInterface, StringUtils.findString(this.FLIP_COMP_LIST, this.m_curPanelName, 0, 5));
/*     */     }
/*     */     else
/*     */     {
/* 329 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizInvalidCommand", new Object[0]));
/*     */ 
/* 331 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void goBackOrNext(boolean isNext)
/*     */   {
/* 338 */     String validateEntries = "true";
/* 339 */     if (!isNext)
/*     */     {
/* 341 */       validateEntries = "false";
/*     */     }
/* 343 */     this.m_helper.m_props.put("validateEntries", validateEntries);
/*     */ 
/* 345 */     if ((!this.m_curView.validateEntries()) && (isNext))
/*     */     {
/* 347 */       return;
/*     */     }
/*     */ 
/* 350 */     Properties props = this.m_helper.m_props;
/* 351 */     String cwType = props.getProperty("type");
/* 352 */     String mergeTable = props.getProperty("mergeTable");
/* 353 */     if (isNext)
/*     */     {
/* 356 */       if (this.m_curPanelName.equals("templateTable"))
/*     */       {
/* 358 */         if ((mergeTable == null) || (mergeTable.length() == 0))
/*     */         {
/* 360 */           mergeTable = "IntradocTemplates";
/*     */         }
/* 362 */         this.m_nextPanelName = (cwType + mergeTable);
/*     */       }
/*     */ 
/* 365 */       this.m_curPanelName = this.m_nextPanelName;
/*     */     }
/*     */     else
/*     */     {
/* 369 */       this.m_curPanelName = this.m_backPanelName;
/*     */     }
/*     */ 
/* 372 */     updatePanel(cwType, mergeTable, isNext);
/*     */   }
/*     */ 
/*     */   protected void finish()
/*     */   {
/* 377 */     DialogHelper dialogHelper = getDialogHelper();
/* 378 */     this.m_finishBtn.setEnabled(false);
/* 379 */     dialogHelper.m_result = 0;
/* 380 */     this.m_helper.m_props.put("validateEntries", "true");
/* 381 */     if ((this.m_curView.validateEntries()) && (addResource()))
/*     */     {
/* 383 */       this.m_finishBtn.setEnabled(true);
/* 384 */       dialogHelper.m_result = 1;
/* 385 */       dialogHelper.close();
/*     */     }
/*     */     else
/*     */     {
/* 389 */       this.m_finishBtn.setEnabled(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void updatePanel(String cwType, String mergetable, boolean isNext)
/*     */   {
/* 395 */     CardLayout panelHandler = (CardLayout)this.m_flipPanel.getLayout();
/* 396 */     EditBasePanel panel = (EditBasePanel)this.m_flipComponents.get(this.m_curPanelName);
/* 397 */     this.m_curView = panel;
/*     */ 
/* 399 */     this.m_curView.loadData();
/* 400 */     panelHandler.show(this.m_flipPanel, this.m_curPanelName);
/* 401 */     String newTitle = LocaleResources.localizeMessage(StringUtils.findString(this.FLIP_COMP_LIST, this.m_curPanelName, 0, 4), null);
/*     */ 
/* 404 */     if (newTitle != null)
/*     */     {
/* 406 */       String filename = this.m_helper.m_props.getProperty("filename");
/* 407 */       if ((isNext) && (filename != null) && (filename.length() > 0))
/*     */       {
/* 409 */         String absPath = FileUtils.getAbsolutePath(this.m_component.m_absCompDir, filename);
/* 410 */         if (CWizardUtils.isReadOnly(absPath))
/*     */         {
/* 412 */           newTitle = newTitle + LocaleUtils.encodeMessage("csCompWizTitleReadOnly", null, filename);
/*     */         }
/*     */       }
/* 415 */       getDialogHelper().m_dialog.setTitle(newTitle);
/*     */     }
/*     */ 
/* 418 */     determineEnableDisable(cwType, mergetable, this.m_curPanelName);
/*     */   }
/*     */ 
/*     */   protected boolean addResource()
/*     */   {
/* 423 */     boolean retVal = true;
/* 424 */     boolean isAppend = StringUtils.convertToBool(this.m_helper.m_props.getProperty("isAppend"), false);
/*     */ 
/* 426 */     int editType = 0;
/*     */ 
/* 428 */     if (isAppend)
/*     */     {
/* 430 */       editType = 1;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 435 */       this.m_component.createOrEditResourceFileInfo(editType, this.m_helper.m_props, null);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 439 */       retVal = false;
/* 440 */       CWizardGuiUtils.reportError(this.m_systemInterface, exp, (IdcMessage)null);
/*     */     }
/*     */ 
/* 443 */     if (retVal == true)
/*     */     {
/* 446 */       String filename = this.m_helper.m_props.getProperty("filename");
/* 447 */       filename = FileUtils.getAbsolutePath(this.m_component.m_absCompDir, filename);
/* 448 */       IdcMessage msg = null;
/* 449 */       int promptId = 3;
/* 450 */       int msgType = 4;
/*     */ 
/* 452 */       if (editType == 0)
/*     */       {
/* 454 */         msg = IdcMessageFactory.lc("csCompWizCreatedFile", new Object[] { filename });
/*     */       }
/*     */ 
/* 457 */       if (msg == null)
/*     */       {
/* 459 */         msg = IdcMessageFactory.lc("csCompWizLaunchEditor", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 463 */         msg.m_prior = IdcMessageFactory.lc("csCompWizLaunchEditor", new Object[0]);
/*     */       }
/*     */ 
/* 466 */       promptId = CWizardGuiUtils.doMessage(this.m_systemInterface, null, msg, msgType);
/*     */ 
/* 468 */       if (promptId == 2)
/*     */       {
/* 470 */         CWizardGuiUtils.launchEditor(this.m_systemInterface, filename);
/*     */       }
/*     */     }
/*     */ 
/* 474 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 479 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.AddResourceDlg
 * JD-Core Version:    0.5.4
 */