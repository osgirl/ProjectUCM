/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.Browser;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.CommonDialogs;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocumentPathBuilder;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import intradoc.shared.gui.ViewDlg;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class PreviewProfileDlg
/*     */   implements ActionListener
/*     */ {
/*  70 */   protected SystemInterface m_systemInterface = null;
/*  71 */   protected ExecutionContext m_cxt = null;
/*  72 */   protected SharedContext m_shContext = null;
/*  73 */   protected DialogHelper m_helper = null;
/*  74 */   protected RuleBuilderHelper m_ruleHelper = null;
/*  75 */   protected String m_helpPage = null;
/*     */ 
/*     */   public PreviewProfileDlg(SystemInterface sys, String title, SharedContext shContext, String helpPage)
/*     */   {
/*  80 */     this.m_systemInterface = sys;
/*  81 */     this.m_cxt = sys.getExecutionContext();
/*  82 */     this.m_shContext = shContext;
/*  83 */     this.m_helper = new DialogHelper(sys, title, true);
/*  84 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public void init(Properties props)
/*     */   {
/*  89 */     this.m_helper.m_props = props;
/*  90 */     initUI();
/*     */ 
/*  92 */     this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  97 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/*  98 */     this.m_helper.m_gridHelper.useGridBag(mainPanel);
/*  99 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/*     */ 
/* 101 */     JPanel stdPanel = createStandardPanel();
/* 102 */     initButtonPanel();
/*     */ 
/* 104 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 105 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 106 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 107 */     this.m_helper.addLastComponentInRow(mainPanel, stdPanel);
/*     */   }
/*     */ 
/*     */   protected JPanel createStandardPanel()
/*     */   {
/* 112 */     JPanel pnl = new PanePanel();
/* 113 */     this.m_helper.m_gridHelper.useGridBag(pnl);
/* 114 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/*     */ 
/* 116 */     this.m_helper.addLastComponentInRow(pnl, new CustomText(this.m_systemInterface.getString("apDpPreviewDescription")));
/*     */ 
/* 120 */     Object[][] choiceInfo = { { "dpEvent", "apDpEventLabel", TableFields.DOCPROFILE_EVENTS }, { "dpAction", "apDpActionLabel", TableFields.DOCPROFILE_ACTIONS }, { "IsWorkflow", "apDpIsWorkflowLabel", TableFields.YESNO_OPTIONLIST } };
/*     */ 
/* 126 */     for (int i = 0; i < choiceInfo.length; ++i)
/*     */     {
/* 128 */       DisplayChoice choice = new DisplayChoice();
/* 129 */       String[][] opts = addDefaultToOptions((String[][])(String[][])choiceInfo[i][2]);
/* 130 */       choice.init(opts);
/* 131 */       this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 132 */       this.m_helper.addLabelFieldPair(pnl, this.m_systemInterface.localizeCaption((String)choiceInfo[i][1]), choice, (String)choiceInfo[i][0]);
/*     */     }
/*     */ 
/* 137 */     String[][] info = { { "docID", "apDpDocIDLabel", "selectDoc" }, { "userName", "apDpUserNameLabel", "selectUser" } };
/*     */ 
/* 142 */     for (int i = 0; i < info.length; ++i)
/*     */     {
/* 144 */       this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 145 */       this.m_helper.addLabelEditPairEx(pnl, this.m_systemInterface.localizeCaption(info[i][1]), 30, info[i][0], false);
/*     */ 
/* 148 */       JButton btn = new JButton(this.m_systemInterface.getString("apLabelSelectButton"));
/* 149 */       this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 150 */       this.m_helper.addLastComponentInRow(pnl, btn);
/* 151 */       btn.addActionListener(this);
/* 152 */       btn.setActionCommand(info[i][2]);
/*     */     }
/*     */ 
/* 155 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void initButtonPanel()
/*     */   {
/* 160 */     String[][] btnInfo = { { "apDpDlgButtonCompute", "showResults" }, { "apDlgButtonShow", "showInBrowser" }, { "apLabelClose", "close" } };
/*     */ 
/* 167 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 169 */       JButton btn = new JButton(this.m_systemInterface.getString(btnInfo[i][0]));
/* 170 */       String cmd = btnInfo[i][1];
/* 171 */       btn.setActionCommand(cmd);
/* 172 */       this.m_helper.addCommandButtonEx(btn, this);
/*     */     }
/*     */ 
/* 176 */     this.m_helper.addHelpInfo(this.m_helpPage);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 184 */     String cmd = e.getActionCommand();
/* 185 */     if (cmd.equals("selectDoc"))
/*     */     {
/* 187 */       ViewData viewData = new ViewData(1);
/* 188 */       viewData.m_viewName = "ProfileDocView";
/*     */ 
/* 190 */       String title = this.m_systemInterface.getString("apTitleSelectContentItem");
/* 191 */       String helpPage = "DpSelectDocument";
/* 192 */       viewData.m_isViewOnly = false;
/* 193 */       ViewDlg viewDlg = new ViewDlg(this.m_helper.m_dialog, this.m_systemInterface, title, this.m_shContext, DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 196 */       viewDlg.init(viewData, null);
/* 197 */       if (viewDlg.prompt() == 1)
/*     */       {
/* 199 */         Vector v = viewDlg.computeSelectedValues("dID", false);
/* 200 */         if (v.size() > 0)
/*     */         {
/* 202 */           String value = (String)v.elementAt(0);
/* 203 */           this.m_helper.m_exchange.setComponentValue("docID", value);
/*     */         }
/*     */       }
/*     */     }
/* 207 */     else if (cmd.equals("selectUser"))
/*     */     {
/* 209 */       ViewData viewData = new ViewData(2);
/* 210 */       viewData.m_viewName = "ProfileUserView";
/* 211 */       String key = "dName";
/* 212 */       String title = LocaleResources.getString("apUserView", this.m_cxt);
/* 213 */       String helpPage = "DpSelectUser";
/* 214 */       viewData.m_isViewOnly = false;
/* 215 */       ViewDlg viewDlg = new ViewDlg(this.m_helper.m_dialog, this.m_systemInterface, title, this.m_shContext, DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 218 */       viewDlg.init(viewData, null);
/* 219 */       if (viewDlg.prompt() == 1)
/*     */       {
/* 221 */         Vector v = viewDlg.computeSelectedValues(key, false);
/* 222 */         if (v.size() > 0)
/*     */         {
/* 224 */           String value = (String)v.elementAt(0);
/* 225 */           this.m_helper.m_exchange.setComponentValue("userName", value);
/*     */         }
/*     */       }
/*     */     }
/* 229 */     else if (cmd.equals("showResults"))
/*     */     {
/* 231 */       showResults();
/*     */     }
/* 233 */     else if (cmd.equals("showInBrowser"))
/*     */     {
/* 235 */       showInBrowser();
/*     */     } else {
/* 237 */       if (!cmd.equals("close"))
/*     */         return;
/* 239 */       this.m_helper.close();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void showResults()
/*     */   {
/* 245 */     boolean isOK = this.m_helper.retrieveComponentValues();
/* 246 */     if (!isOK)
/*     */     {
/* 248 */       return;
/*     */     }
/*     */ 
/* 251 */     DataBinder binder = new DataBinder();
/* 252 */     binder.setLocalData((Properties)this.m_helper.m_props.clone());
/*     */     try
/*     */     {
/* 255 */       AppContextUtils.executeService(this.m_shContext, "DOCPROFILE_PREVIEW", binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 259 */       MessageBox.reportError(this.m_systemInterface, e);
/* 260 */       return;
/*     */     }
/*     */ 
/* 264 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apDpPreviewTitle", this.m_cxt), true);
/*     */ 
/* 266 */     helper.initDialogType(1);
/* 267 */     JPanel mainPanel = helper.m_mainPanel;
/* 268 */     helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 270 */     helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 271 */     helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 272 */     helper.addExchangeComponent(mainPanel, new CustomTextArea(), "Results");
/*     */ 
/* 275 */     Properties props = new Properties();
/* 276 */     String str = binder.getLocal("PreviewResults");
/* 277 */     StringUtils.parseProperties(props, str);
/*     */ 
/* 279 */     str = (String)props.remove("FieldRuleList");
/* 280 */     Vector fieldRuleList = StringUtils.parseArray(str, ',', '^');
/* 281 */     int size = fieldRuleList.size();
/* 282 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 284 */       String fieldRule = (String)fieldRuleList.elementAt(i);
/* 285 */       Vector v = StringUtils.parseArray(fieldRule, ':', '*');
/* 286 */       props.put(v.elementAt(0), "*rule*" + v.elementAt(1));
/*     */     }
/*     */ 
/* 290 */     Vector keys = new IdcVector();
/* 291 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*     */     {
/* 293 */       keys.addElement(en.nextElement());
/*     */     }
/*     */ 
/* 296 */     IdcComparator cmp = new IdcComparator()
/*     */     {
/*     */       public int compare(Object obj1, Object obj2)
/*     */       {
/* 300 */         String key1 = (String)obj1;
/* 301 */         String key2 = (String)obj2;
/*     */ 
/* 303 */         return key1.compareTo(key2);
/*     */       }
/*     */     };
/* 306 */     Sort.sortVector(keys, cmp);
/*     */ 
/* 308 */     StringBuffer buff = new StringBuffer();
/* 309 */     String errMsg = binder.getLocal("ErrorMsg");
/* 310 */     if ((errMsg != null) && (errMsg.length() > 0))
/*     */     {
/* 312 */       buff.append(LocaleResources.localizeMessage(errMsg, this.m_cxt));
/*     */     }
/*     */ 
/* 315 */     size = keys.size();
/* 316 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 318 */       String key = (String)keys.elementAt(i);
/* 319 */       String val = props.getProperty(key);
/*     */ 
/* 321 */       if (val.startsWith("*rule*"))
/*     */       {
/* 323 */         val = val.substring(6);
/* 324 */         String msg = LocaleUtils.encodeMessage("apDpFieldEvaluatedAtRule", null, key, val);
/* 325 */         msg = LocaleResources.localizeMessage(msg, this.m_cxt);
/* 326 */         buff.append(msg);
/*     */       }
/*     */       else
/*     */       {
/* 330 */         buff.append(key);
/* 331 */         buff.append("=");
/* 332 */         buff.append(val);
/*     */       }
/* 334 */       buff.append("\n");
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 340 */       DataResultSet restrictedSet = (DataResultSet)binder.getResultSet("RestrictedLists");
/* 341 */       if (restrictedSet != null)
/*     */       {
/* 343 */         int index = ResultSetUtils.getIndexMustExist(restrictedSet, "fieldName");
/* 344 */         for (restrictedSet.first(); restrictedSet.isRowPresent(); restrictedSet.next())
/*     */         {
/* 346 */           buff.append("\n");
/* 347 */           String fieldName = restrictedSet.getStringValue(index);
/* 348 */           String tableName = fieldName + ".RestrictedList";
/* 349 */           ResultSet rset = binder.getResultSet(tableName);
/* 350 */           if (rset == null)
/*     */           {
/* 352 */             String msg = LocaleUtils.encodeMessage("apDpFieldMissingRestrictedList", null, fieldName);
/*     */ 
/* 354 */             buff.append(msg);
/* 355 */             buff.append("\n");
/*     */           }
/*     */           else
/*     */           {
/* 359 */             String msg = LocaleUtils.encodeMessage("apDpFieldRestrictedList", null, fieldName);
/* 360 */             msg = LocaleResources.localizeMessage(msg, this.m_cxt);
/* 361 */             buff.append(msg);
/* 362 */             buff.append("\n");
/*     */ 
/* 364 */             for (rset.first(); rset.isRowPresent(); rset.next())
/*     */             {
/* 366 */               buff.append(rset.getStringValue(0));
/* 367 */               buff.append("\n");
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 373 */       DataResultSet topSet = (DataResultSet)binder.getResultSet("AssociatedTopFields");
/* 374 */       if (topSet != null)
/*     */       {
/* 376 */         int index = ResultSetUtils.getIndexMustExist(topSet, "parentField");
/* 377 */         for (topSet.first(); topSet.isRowPresent(); topSet.next())
/*     */         {
/* 379 */           String parent = topSet.getStringValue(index);
/* 380 */           String msg = LocaleUtils.encodeMessage("apDpFieldIsGroup", null, parent);
/* 381 */           msg = LocaleResources.localizeMessage(msg, this.m_cxt);
/* 382 */           buff.append(msg);
/* 383 */           buff.append("\n");
/*     */ 
/* 385 */           DataResultSet buddySet = (DataResultSet)binder.getResultSet("AssociatedFields:" + parent);
/* 386 */           if (buddySet == null)
/*     */           {
/* 389 */             msg = this.m_systemInterface.getString("apDpFieldGroupIsMissing");
/* 390 */             buff.append(msg);
/*     */           }
/* 392 */           int bIndex = ResultSetUtils.getIndexMustExist(buddySet, "fieldName");
/* 393 */           for (buddySet.first(); buddySet.isRowPresent(); buddySet.next())
/*     */           {
/* 395 */             String field = buddySet.getStringValue(bIndex);
/* 396 */             buff.append("\t");
/* 397 */             buff.append(field);
/* 398 */             buff.append("\n");
/*     */           }
/* 400 */           buff.append("\n");
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (DataException ignore)
/*     */     {
/* 406 */       if (SystemUtils.m_verbose)
/*     */       {
/* 408 */         Report.debug("system", null, ignore);
/*     */       }
/*     */     }
/*     */ 
/* 412 */     props.put("Results", buff.toString());
/* 413 */     helper.m_props = props;
/* 414 */     helper.loadComponentValues();
/* 415 */     helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void showInBrowser()
/*     */   {
/* 420 */     boolean isOK = this.m_helper.retrieveComponentValues();
/* 421 */     if (!isOK)
/*     */     {
/* 423 */       return;
/*     */     }
/*     */ 
/* 426 */     IdcMessage errMsg = null;
/* 427 */     String urlPath = null;
/*     */     try
/*     */     {
/* 430 */       urlPath = this.m_helper.m_props.getProperty("RelativeCgiWebUrl");
/* 431 */       urlPath = urlPath + "?IdcService=DOCPROFILE_PREVIEW&isBrowser=1";
/*     */ 
/* 433 */       String[] info = { "dpName", "dpEvent", "dpAction", "IsWorkflow", "docID", "userName", "dpTriggerValue" };
/* 434 */       for (int i = 0; i < info.length; ++i)
/*     */       {
/* 436 */         String key = info[i];
/* 437 */         String val = this.m_helper.m_props.getProperty(key);
/* 438 */         if (key.equals("dpEvent"))
/*     */         {
/* 441 */           if (!val.equals("OnRequest"))
/*     */           {
/* 443 */             errMsg = IdcMessageFactory.lc("apDpPreviewMustBeRequest", new Object[0]);
/*     */           }
/*     */         }
/* 446 */         else if (key.equals("userName"))
/*     */         {
/* 448 */           if (val.length() > 0)
/*     */           {
/* 450 */             errMsg = IdcMessageFactory.lc("apDpPreviewCantUseUserName", new Object[0]);
/*     */           }
/*     */         }
/* 453 */         else if ((key.equals("dpAction")) && 
/* 455 */           (val.length() == 0))
/*     */         {
/* 457 */           errMsg = IdcMessageFactory.lc("apDpPreviewDefineAction", new Object[0]);
/*     */         }
/*     */ 
/* 460 */         if (errMsg != null) {
/*     */           break;
/*     */         }
/*     */ 
/* 464 */         if ((val == null) || (val.length() <= 0))
/*     */           continue;
/* 466 */         urlPath = urlPath + "&" + key + "=" + StringUtils.urlEncode(val);
/*     */       }
/*     */ 
/* 469 */       if (errMsg != null)
/*     */       {
/* 471 */         errMsg = IdcMessageFactory.lc(errMsg, "apUnableToViewInBrowser", new Object[0]);
/* 472 */         MessageBox.reportError(this.m_systemInterface, errMsg);
/*     */       }
/*     */       else
/*     */       {
/* 476 */         Browser.showDocument(urlPath);
/*     */       }
/*     */     }
/*     */     catch (Exception excep)
/*     */     {
/* 481 */       if (urlPath != null)
/*     */       {
/* 483 */         urlPath = Browser.computeFullUrlString(DocumentPathBuilder.getBaseAbsoluteRoot(), urlPath);
/*     */ 
/* 486 */         CommonDialogs.showUrlMessage(this.m_systemInterface, this.m_systemInterface.getString("apLabelBrowserNotAccessible"), urlPath, LocaleResources.localizeMessage(LocaleUtils.encodeMessage("apUnableToViewUrl", excep.getMessage()), this.m_cxt));
/*     */       }
/*     */       else
/*     */       {
/* 494 */         MessageBox.reportError(this.m_systemInterface, excep, IdcMessageFactory.lc("apUnableToViewInBrowser", new Object[0]));
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String[][] addDefaultToOptions(String[][] options)
/*     */   {
/* 502 */     int len = options.length;
/* 503 */     String[][] opt = new String[len + 1][];
/*     */ 
/* 506 */     opt[0] = new String[2];
/* 507 */     opt[0][0] = "";
/* 508 */     opt[0][1] = this.m_systemInterface.getString("apDpNoneSpecified");
/* 509 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 511 */       opt[(i + 1)] = new String[2];
/* 512 */       opt[(i + 1)][0] = options[i][0];
/* 513 */       opt[(i + 1)][1] = options[i][1];
/*     */     }
/* 515 */     return opt;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 520 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78892 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.PreviewProfileDlg
 * JD-Core Version:    0.5.4
 */