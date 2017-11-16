/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.gui.SecurityEditHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditPageHelper
/*     */   implements ComponentBinder, ItemListener
/*     */ {
/*     */   public PageData m_pageData;
/*     */   public PageManagerContext m_pageContext;
/*     */   public SecurityEditHelper m_securityEditHelper;
/*     */   public Vector m_dirPageDependentComponents;
/*     */   public JPanel m_securityPanel;
/*  77 */   protected ExecutionContext m_ctx = null;
/*     */ 
/*     */   public EditPageHelper()
/*     */   {
/*  82 */     this.m_pageData = null;
/*  83 */     this.m_pageContext = null;
/*  84 */     this.m_securityEditHelper = null;
/*  85 */     this.m_dirPageDependentComponents = null;
/*  86 */     this.m_securityPanel = null;
/*     */   }
/*     */ 
/*     */   public void addPageProperties(JPanel panel, ContainerHelper helper, boolean isReadOnly) throws ServiceException
/*     */   {
/*  91 */     if (this.m_ctx == null) {
/*  92 */       this.m_ctx = helper.m_exchange.m_sysInterface.getExecutionContext();
/*     */     }
/*  94 */     boolean useAccounts = SharedObjects.getEnvValueAsBoolean("UseAccounts", false);
/*  95 */     Component comp = null;
/*  96 */     if (!isReadOnly)
/*     */     {
/*  98 */       comp = new CustomTextField(30);
/*     */     }
/* 100 */     this.m_securityEditHelper.m_helper = helper;
/*     */ 
/* 102 */     helper.m_gridHelper.m_gc.fill = 2;
/*     */ 
/* 105 */     addLabelPairEx(panel, helper, LocaleResources.getString("apLabelPageTitle", this.m_ctx), comp, 300, "PageTitle", true);
/*     */ 
/* 108 */     comp = null;
/* 109 */     comp = new CustomTextArea(3, 50);
/* 110 */     helper.m_gridHelper.m_gc.fill = 1;
/* 111 */     helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 112 */     addLabelPairEx(panel, helper, LocaleResources.getString("apLabelPageDescription", this.m_ctx), comp, 0, "HeaderText", true);
/*     */ 
/* 114 */     helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 115 */     comp.setEnabled(!isReadOnly);
/* 116 */     if (!isReadOnly)
/*     */     {
/* 118 */       JPanel groupPanel = new CustomPanel();
/* 119 */       helper.m_gridHelper.useGridBag(groupPanel);
/* 120 */       helper.m_gridHelper.m_gc.fill = 2;
/* 121 */       this.m_dirPageDependentComponents = new IdcVector();
/*     */ 
/* 131 */       helper.m_gridHelper.m_gc.fill = 2;
/* 132 */       this.m_securityEditHelper.addSecurityGroupEditField(groupPanel, LocaleResources.getString("apLabelSecurityGroup", this.m_ctx), "dSecurityGroup");
/*     */ 
/* 135 */       helper.m_gridHelper.prepareAddLastRowElement();
/* 136 */       CustomCheckbox groupCheckbox = new CustomCheckbox(LocaleResources.getString("apRestrictQueriesBySecurityGroup", this.m_ctx));
/*     */ 
/* 138 */       helper.addExchangeComponent(groupPanel, groupCheckbox, "restrictByGroup");
/* 139 */       this.m_dirPageDependentComponents.addElement(groupCheckbox);
/*     */ 
/* 141 */       helper.addLastComponentInRow(panel, groupPanel);
/* 142 */       if (useAccounts)
/*     */       {
/* 144 */         JPanel accountPanel = new CustomPanel();
/* 145 */         helper.m_gridHelper.useGridBag(accountPanel);
/* 146 */         helper.m_gridHelper.m_gc.fill = 2;
/*     */ 
/* 151 */         this.m_securityEditHelper.addAccountEditField(accountPanel, LocaleResources.getString("apLabelAccount", this.m_ctx), "dDocAccount");
/*     */ 
/* 153 */         helper.m_gridHelper.prepareAddLastRowElement();
/* 154 */         CustomCheckbox accountCheckbox = new CustomCheckbox(LocaleResources.getString("apRestrictQueriesByAccount", this.m_ctx));
/*     */ 
/* 156 */         helper.addExchangeComponent(accountPanel, accountCheckbox, "restrictByAccount");
/* 157 */         this.m_dirPageDependentComponents.addElement(accountCheckbox);
/* 158 */         this.m_securityEditHelper.refreshAccountChoiceList(false, 8);
/* 159 */         helper.addLastComponentInRow(panel, accountPanel);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 164 */       this.m_securityPanel = new PanePanel();
/*     */ 
/* 166 */       addLabelPairEx(panel, helper, LocaleResources.getString("apLabelSecurityGroup", this.m_ctx), null, 130, "dSecurityGroup", false);
/*     */ 
/* 169 */       helper.m_gridHelper.m_gc.weightx = 10.0D;
/* 170 */       helper.addLastComponentInRow(panel, this.m_securityPanel);
/* 171 */       helper.m_gridHelper.m_gc.weightx = 1.0D;
/*     */ 
/* 173 */       helper.m_gridHelper.prepareAddLastRowElement();
/* 174 */       helper.addLabelDisplayPair(panel, LocaleResources.getString("apLabelRestrictions", this.m_ctx), 100, "docQueryRestrictions");
/*     */ 
/* 177 */       addAccountDisplay(helper);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addAccountDisplay(ContainerHelper helper)
/*     */   {
/* 183 */     if (this.m_ctx == null)
/* 184 */       this.m_ctx = helper.m_exchange.m_sysInterface.getExecutionContext();
/* 185 */     if (this.m_securityPanel == null)
/*     */       return;
/* 187 */     boolean useAccounts = SharedObjects.getEnvValueAsBoolean("UseAccounts", false);
/*     */ 
/* 189 */     this.m_securityPanel.removeAll();
/* 190 */     GridBagConstraints oldConstraints = helper.m_gridHelper.m_gc;
/* 191 */     helper.m_gridHelper.useGridBag(this.m_securityPanel);
/* 192 */     if (useAccounts)
/*     */     {
/* 194 */       addLabelPairEx(this.m_securityPanel, helper, LocaleResources.getString("apLabelAccount", this.m_ctx), null, 170, "dDocAccount", true);
/*     */     }
/*     */ 
/* 198 */     helper.m_gridHelper.m_gc = oldConstraints;
/*     */   }
/*     */ 
/*     */   public void addLabelPairEx(JPanel panel, ContainerHelper helper, String label, Component comp, int displen, String name, boolean isLastItemInRow)
/*     */   {
/* 205 */     if (comp == null)
/*     */     {
/* 207 */       helper.addLabelDisplayPairEx(panel, label, displen, name, isLastItemInRow);
/*     */     }
/*     */     else
/*     */     {
/* 211 */       helper.addLabelFieldPairEx(panel, label, comp, name, isLastItemInRow);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean promptNewPage(SystemInterface sysInterface, PageData data, String defaultGroup, PageManagerContext context)
/*     */     throws ServiceException
/*     */   {
/* 219 */     if (this.m_ctx == null) {
/* 220 */       this.m_ctx = sysInterface.getExecutionContext();
/*     */     }
/* 222 */     this.m_pageData = data;
/* 223 */     this.m_pageContext = context;
/*     */ 
/* 225 */     this.m_pageData.m_binder.putLocal("dSecurityGroup", defaultGroup);
/*     */ 
/* 227 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 234 */         EditPageHelper.this.m_pageData.m_accessLevel = "15";
/*     */ 
/* 237 */         EditPageHelper.this.m_pageData.m_binder.putLocal("IsNewPage", "1");
/*     */         try
/*     */         {
/* 242 */           EditPageHelper.this.m_pageContext.setDefaults(EditPageHelper.this.m_pageData);
/* 243 */           EditPageHelper.this.m_pageContext.saveData(EditPageHelper.this.m_pageData.m_pageId, EditPageHelper.this.m_pageData);
/*     */         }
/*     */         catch (ServiceException excep)
/*     */         {
/* 247 */           MessageBox.reportError(this.m_dlgHelper.m_exchange.m_sysInterface, excep, IdcMessageFactory.lc("apErrorCreatingPage", new Object[0]));
/*     */ 
/* 249 */           return false;
/*     */         }
/* 251 */         return true;
/*     */       }
/*     */     };
/* 256 */     DialogHelper helper = new DialogHelper(sysInterface, LocaleResources.getString("apLabelAddWebPage", this.m_ctx), true);
/*     */ 
/* 258 */     JPanel mainPanel = helper.initStandard(this, okCallback, 2, true, DialogHelpTable.getHelpPage("AddWebPage"));
/*     */ 
/* 260 */     JPanel idFields = new CustomPanel();
/* 261 */     JPanel propsFields = new CustomPanel();
/* 262 */     helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 263 */     helper.addLastComponentInRow(mainPanel, idFields);
/* 264 */     helper.m_gridHelper.m_gc.fill = 1;
/* 265 */     helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 266 */     helper.addLastComponentInRow(mainPanel, propsFields);
/*     */ 
/* 268 */     helper.m_gridHelper.useGridBag(idFields);
/* 269 */     helper.addLabelFieldPair(idFields, LocaleResources.getString("apLabelPageName", this.m_ctx), new CustomTextField(45), "PageName");
/*     */ 
/* 271 */     String[][] types = this.m_pageContext.getPageTypesList();
/* 272 */     if (types.length == 1)
/*     */     {
/* 274 */       setInternalValue("PageType", types[0][0], this.m_pageData.m_binder);
/* 275 */       this.m_pageData.m_typeId = types[0][0];
/* 276 */       setInternalValue("DisplayPageType", types[0][1], this.m_pageData.m_binder);
/* 277 */       helper.addLabelDisplayPair(idFields, LocaleResources.getString("apLabelPageType", this.m_ctx), 50, "DisplayPageType");
/*     */     }
/*     */     else
/*     */     {
/* 282 */       DisplayChoice pageTypes = new DisplayChoice();
/* 283 */       pageTypes.init(types);
/* 284 */       pageTypes.addItemListener(this);
/* 285 */       helper.addLabelFieldPair(idFields, LocaleResources.getString("apLabelPageType", this.m_ctx), pageTypes, "PageType");
/*     */     }
/*     */ 
/* 289 */     helper.m_gridHelper.useGridBag(propsFields);
/* 290 */     addPageProperties(propsFields, helper, false);
/*     */ 
/* 292 */     return helper.prompt() == 1;
/*     */   }
/*     */ 
/*     */   public String getDisplayValue(String field, DataBinder binder)
/*     */   {
/* 298 */     String value = binder.getLocal(field);
/* 299 */     if (field.equals("restrictByGroup"))
/*     */     {
/* 301 */       if ((value == null) || (value.length() == 0))
/*     */       {
/* 303 */         value = "1";
/*     */       }
/*     */     }
/* 306 */     else if (field.equals("docQueryRestrictions"))
/*     */     {
/* 308 */       String pageType = binder.getLocal("PageType");
/* 309 */       if ((pageType == null) || (!pageType.equals("Directory")))
/*     */       {
/* 311 */         return LocaleResources.getString("apLabelNotApplicable", this.m_ctx);
/*     */       }
/* 313 */       boolean isRestrictGroup = StringUtils.convertToBool(binder.getLocal("restrictByGroup"), true);
/* 314 */       boolean isRestrictAccount = StringUtils.convertToBool(binder.getLocal("restrictByAccount"), false);
/*     */ 
/* 316 */       if (isRestrictGroup)
/*     */       {
/* 318 */         if (isRestrictAccount)
/* 319 */           value = LocaleResources.getString("apQueriesRestrictedByGroupAndAccount", this.m_ctx);
/*     */         else {
/* 321 */           value = LocaleResources.getString("apQueriesRestrictedByGroup", this.m_ctx);
/*     */         }
/*     */ 
/*     */       }
/* 325 */       else if (isRestrictAccount)
/* 326 */         value = LocaleResources.getString("apQueriesRestrictedByAccount", this.m_ctx);
/*     */       else {
/* 328 */         value = LocaleResources.getString("apQueriesNotRestricted", this.m_ctx);
/*     */       }
/*     */     }
/* 331 */     else if ((field.equals("PageType")) && ((
/* 333 */       (value == null) || (value.length() == 0))))
/*     */     {
/* 335 */       value = "Directory";
/*     */     }
/*     */ 
/* 338 */     return value;
/*     */   }
/*     */ 
/*     */   public void setInternalValue(String field, String val, DataBinder binder)
/*     */   {
/* 343 */     binder.putLocal(field, val);
/*     */   }
/*     */ 
/*     */   public void enableDisable()
/*     */   {
/* 348 */     if (this.m_dirPageDependentComponents == null)
/*     */     {
/* 350 */       return;
/*     */     }
/* 352 */     String pageType = this.m_securityEditHelper.m_helper.m_exchange.getComponentValue("PageType");
/* 353 */     boolean isEnabled = (pageType == null) || (pageType.equals("Directory"));
/* 354 */     for (int i = 0; i < this.m_dirPageDependentComponents.size(); ++i)
/*     */     {
/* 356 */       Component comp = (Component)this.m_dirPageDependentComponents.elementAt(i);
/* 357 */       comp.setEnabled(isEnabled);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updatePrivilegeState(ContainerHelper helper)
/*     */   {
/* 363 */     addAccountDisplay(helper);
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 371 */     enableDisable();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 380 */     String name = exchange.m_compName;
/*     */ 
/* 383 */     if (updateComponent == true)
/*     */     {
/* 385 */       exchange.m_compValue = getDisplayValue(name, this.m_pageData.m_binder);
/* 386 */       return;
/*     */     }
/*     */ 
/* 390 */     setInternalValue(name, exchange.m_compValue, this.m_pageData.m_binder);
/*     */ 
/* 392 */     if (name.equals("PageName"))
/*     */     {
/* 394 */       this.m_pageData.m_pageId = exchange.m_compValue;
/*     */     } else {
/* 396 */       if (!name.equals("PageType"))
/*     */         return;
/* 398 */       this.m_pageData.m_typeId = exchange.m_compValue;
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 404 */     if (this.m_ctx == null) {
/* 405 */       this.m_ctx = exchange.m_sysInterface.getExecutionContext();
/*     */     }
/* 407 */     String name = exchange.m_compName;
/* 408 */     String val = exchange.m_compValue;
/*     */ 
/* 410 */     if ((name.equals("PageType")) && 
/* 412 */       (val == null))
/*     */     {
/* 414 */       exchange.m_errorMessage = IdcMessageFactory.lc("apSelectPageType", new Object[0]);
/* 415 */       return false;
/*     */     }
/*     */ 
/* 419 */     if (name.equals("PageName"))
/*     */     {
/* 421 */       int valResult = Validation.checkUrlFileSegment(val);
/* 422 */       IdcMessage errMsg = null;
/* 423 */       switch (valResult)
/*     */       {
/*     */       case 0:
/* 426 */         break;
/*     */       case -1:
/* 428 */         errMsg = IdcMessageFactory.lc("apSpecifyPageName", new Object[0]);
/* 429 */         break;
/*     */       case -2:
/* 431 */         errMsg = IdcMessageFactory.lc("apSpacesInPageName", new Object[0]);
/* 432 */         break;
/*     */       default:
/* 434 */         errMsg = IdcMessageFactory.lc("apIllegalCharsInPageName", new Object[0]);
/*     */       }
/*     */ 
/* 437 */       if ((errMsg == null) && (this.m_pageContext.getPage(val) != null))
/*     */       {
/* 439 */         errMsg = IdcMessageFactory.lc("apPageAlreadyExists", new Object[0]);
/*     */       }
/* 441 */       if (errMsg != null)
/*     */       {
/* 443 */         exchange.m_errorMessage = errMsg;
/* 444 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 448 */     return validate(exchange);
/*     */   }
/*     */ 
/*     */   public boolean validate(DynamicComponentExchange exchange)
/*     */   {
/* 454 */     if (this.m_ctx == null) {
/* 455 */       this.m_ctx = exchange.m_sysInterface.getExecutionContext();
/*     */     }
/* 457 */     String name = exchange.m_compName;
/* 458 */     String val = exchange.m_compValue;
/* 459 */     if ((name.equals("PageTitle")) && ((
/* 461 */       (val == null) || (val.trim().length() == 0))))
/*     */     {
/* 463 */       exchange.m_errorMessage = IdcMessageFactory.lc("apSpecifyPageTitle", new Object[0]);
/* 464 */       return false;
/*     */     }
/*     */ 
/* 467 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 472 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditPageHelper
 * JD-Core Version:    0.5.4
 */