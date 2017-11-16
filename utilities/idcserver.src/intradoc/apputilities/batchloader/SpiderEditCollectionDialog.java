/*     */ package intradoc.apputilities.batchloader;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.FocusListener;
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SpiderEditCollectionDialog
/*     */   implements ComponentBinder
/*     */ {
/*  59 */   protected DialogHelper m_helper = null;
/*  60 */   protected SystemInterface m_sysInterface = null;
/*  61 */   protected ExecutionContext m_cxt = null;
/*  62 */   protected UdlPanel m_fieldList = null;
/*  63 */   protected JButton m_editBtn = null;
/*  64 */   protected JButton m_deleteBtn = null;
/*  65 */   protected Component m_collectionIDTxt = null;
/*  66 */   protected CustomTextField m_locationTxt = null;
/*  67 */   protected CustomTextField m_fileRootTxt = null;
/*  68 */   protected CustomTextField m_webRootTxt = null;
/*  69 */   protected CustomChoice m_statusChoice = null;
/*  70 */   protected DisplayChoice m_readOnlyChoice = null;
/*     */ 
/*  72 */   protected boolean m_isAdd = false;
/*     */ 
/*  75 */   protected String m_searchDir = null;
/*  76 */   protected String m_fileName = "search_collections.hda";
/*  77 */   protected String m_tableName = "SearchCollections";
/*  78 */   protected String[] m_fields = { "sCollectionID", "sDescription", "sProfile", "sFlag", "sUrlScript", "sPhysicalFileRoot", "sRelativeWebRoot" };
/*     */ 
/*  80 */   protected String[] m_externalFields = { "sPhysicalFileRoot", "sRelativeWebRoot" };
/*  81 */   protected FieldInfo[] m_fieldInfo = null;
/*     */ 
/*     */   public SpiderEditCollectionDialog(SystemInterface sys)
/*     */   {
/*  85 */     this.m_cxt = sys.getExecutionContext();
/*  86 */     this.m_sysInterface = sys;
/*  87 */     this.m_searchDir = (LegacyDirectoryLocator.getAppDataDirectory() + "search/");
/*     */   }
/*     */ 
/*     */   public void init(Properties props, boolean isAdd)
/*     */     throws DataException, ServiceException
/*     */   {
/*  93 */     String title = null;
/*  94 */     if (isAdd)
/*     */     {
/*  96 */       title = "csSpiderAddCollectionDialogTitle";
/*     */     }
/*     */     else
/*     */     {
/* 100 */       title = "csSpiderEditCollectionDialogTitle";
/*     */     }
/*     */ 
/* 103 */     this.m_helper = new DialogHelper(this.m_sysInterface, LocaleResources.getString(title, this.m_cxt), true);
/*     */ 
/* 105 */     this.m_helper.m_props = props;
/* 106 */     this.m_isAdd = isAdd;
/*     */ 
/* 108 */     initUI();
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/* 113 */     this.m_helper.initStandard(this, null, 2, false, null);
/* 114 */     JPanel collectionPanel = initCollectionPanel();
/* 115 */     initCollectionFields(collectionPanel);
/*     */   }
/*     */ 
/*     */   public JPanel initCollectionPanel()
/*     */   {
/* 120 */     JPanel mapPanel = new PanePanel();
/* 121 */     mapPanel.setLayout(new GridBagLayout());
/* 122 */     this.m_helper.makePanelGridBag(this.m_helper.m_mainPanel, 1);
/* 123 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 124 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 125 */     this.m_helper.addLastComponentInRow(this.m_helper.m_mainPanel, mapPanel);
/*     */ 
/* 127 */     return mapPanel;
/*     */   }
/*     */ 
/*     */   public void initCollectionFields(JPanel collectionPanel)
/*     */   {
/* 133 */     if (this.m_isAdd)
/*     */     {
/* 135 */       this.m_collectionIDTxt = new CustomTextField(20);
/*     */     }
/*     */     else
/*     */     {
/* 139 */       this.m_collectionIDTxt = new CustomLabel("", 0);
/*     */     }
/*     */ 
/* 142 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 143 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 144 */     this.m_helper.addComponent(collectionPanel, new CustomLabel(LocaleResources.getString("csSpiderCollectionIDLabel", this.m_cxt), 1));
/*     */ 
/* 147 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 148 */     this.m_helper.addExchangeComponent(collectionPanel, this.m_collectionIDTxt, "sCollectionID");
/*     */ 
/* 151 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 152 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 153 */     this.m_helper.addComponent(collectionPanel, new CustomLabel(LocaleResources.getString("csSpiderMapDescLabel", this.m_cxt), 1));
/*     */ 
/* 156 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 157 */     this.m_helper.addExchangeComponent(collectionPanel, new CustomTextField(40), "sDescription");
/*     */ 
/* 160 */     this.m_locationTxt = new CustomTextField(50);
/* 161 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 162 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 163 */     this.m_helper.addComponent(collectionPanel, new CustomLabel(LocaleResources.getString("csSpiderLocationLable", this.m_cxt), 1));
/*     */ 
/* 166 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 167 */     this.m_helper.addExchangeComponent(collectionPanel, this.m_locationTxt, "sLocation");
/*     */ 
/* 170 */     this.m_fileRootTxt = new CustomTextField(50);
/* 171 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 172 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 173 */     this.m_helper.addComponent(collectionPanel, new CustomLabel(LocaleResources.getString("csSpiderFileRootLabel", this.m_cxt), 1));
/*     */ 
/* 176 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 177 */     this.m_helper.addExchangeComponent(collectionPanel, this.m_fileRootTxt, "sPhysicalFileRoot");
/*     */ 
/* 180 */     this.m_webRootTxt = new CustomTextField(50);
/* 181 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 182 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 183 */     this.m_helper.addComponent(collectionPanel, new CustomLabel(LocaleResources.getString("csSpiderWebRootLabel", this.m_cxt), 1));
/*     */ 
/* 186 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 187 */     this.m_helper.addExchangeComponent(collectionPanel, this.m_webRootTxt, "sRelativeWebRoot");
/*     */ 
/* 190 */     this.m_statusChoice = new CustomChoice();
/* 191 */     this.m_statusChoice.add("enabled");
/* 192 */     this.m_statusChoice.add("disabled");
/* 193 */     this.m_statusChoice.add("optional");
/* 194 */     this.m_statusChoice.setEnabled(true);
/*     */ 
/* 196 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 197 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 198 */     this.m_helper.addComponent(collectionPanel, new CustomLabel(LocaleResources.getString("csCompWizLabelStatus2", this.m_cxt), 1));
/*     */ 
/* 201 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 202 */     this.m_helper.addExchangeComponent(collectionPanel, this.m_statusChoice, "sFlag");
/*     */ 
/* 205 */     CustomTextField otherPropsTxt = new CustomTextField(50);
/* 206 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 207 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 208 */     this.m_helper.addComponent(collectionPanel, new CustomLabel(LocaleResources.getString("csSpiderProperties2", this.m_cxt), 1));
/*     */ 
/* 211 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 212 */     this.m_helper.addExchangeComponent(collectionPanel, otherPropsTxt, "sProperties");
/*     */ 
/* 215 */     FocusListener focusListener = new FocusListener()
/*     */     {
/*     */       public void focusGained(FocusEvent e)
/*     */       {
/*     */       }
/*     */ 
/*     */       public void focusLost(FocusEvent e)
/*     */       {
/* 224 */         SpiderEditCollectionDialog.this.setCollectionLocation();
/*     */       }
/*     */     };
/* 227 */     this.m_collectionIDTxt.addFocusListener(focusListener);
/*     */   }
/*     */ 
/*     */   protected void setCollectionLocation()
/*     */   {
/* 233 */     if (!this.m_isAdd)
/*     */       return;
/* 235 */     CustomTextField customTxt = (CustomTextField)this.m_collectionIDTxt;
/* 236 */     String collectionID = customTxt.getText();
/* 237 */     collectionID = collectionID.trim();
/* 238 */     if (collectionID.equals(""))
/*     */     {
/* 240 */       return;
/*     */     }
/*     */ 
/* 243 */     String location = this.m_locationTxt.getText();
/* 244 */     location = location.trim();
/* 245 */     if (!location.equals(""))
/*     */       return;
/* 247 */     location = LegacyDirectoryLocator.getSearchDirectory() + "external/" + collectionID + "/";
/*     */ 
/* 249 */     this.m_locationTxt.setText(location);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(String msg)
/*     */   {
/* 258 */     MessageBox.reportError(this.m_sysInterface.getMainWindow(), msg, LocaleResources.getString("csSpiderEditMapTitle", this.m_cxt));
/*     */   }
/*     */ 
/*     */   public void reportError(IdcMessage msg)
/*     */   {
/* 264 */     MessageBox.reportError(this.m_sysInterface, this.m_sysInterface.getMainWindow(), msg, IdcMessageFactory.lc("csSpiderEditMapTitle", new Object[0]));
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 270 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 277 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 282 */     String name = exchange.m_compName;
/* 283 */     String value = exchange.m_compValue;
/* 284 */     if (value == null)
/*     */     {
/* 286 */       value = "";
/*     */     }
/*     */     else
/*     */     {
/* 290 */       value = value.trim();
/*     */     }
/*     */ 
/* 294 */     if ((name.equals("sCollectionID")) && (this.m_isAdd))
/*     */     {
/* 296 */       boolean isValid = false;
/*     */ 
/* 299 */       if (value.equals(""))
/*     */       {
/* 301 */         exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderInvalidCollectionID", new Object[] { value });
/*     */       }
/* 303 */       else if (Validation.checkUrlFileSegment(value) != 0)
/*     */       {
/* 305 */         exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderInvalidCollectionID", new Object[] { value });
/*     */       }
/* 308 */       else if (value.equalsIgnoreCase("local"))
/*     */       {
/* 310 */         exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderInvalidCollectionID", new Object[] { value });
/*     */       }
/*     */       else
/*     */       {
/* 314 */         isValid = true;
/*     */       }
/*     */ 
/* 318 */       if (!isValid)
/*     */       {
/* 320 */         this.m_locationTxt.setText("");
/* 321 */         return false;
/*     */       }
/*     */     }
/* 324 */     else if (name.equals("sLocation"))
/*     */     {
/* 326 */       if (value.equals(""))
/*     */       {
/* 328 */         exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderInvalidLocation", new Object[] { value });
/* 329 */         return false;
/*     */       }
/*     */     }
/* 332 */     else if (name.equals("sRelativeWebRoot"))
/*     */     {
/* 334 */       if (!value.equals(""));
/* 344 */       String fileRoot = this.m_fileRootTxt.getText();
/* 345 */       fileRoot = fileRoot.trim();
/*     */ 
/* 347 */       if ((value.equals("")) && (!fileRoot.equals("")))
/*     */       {
/* 349 */         exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderInvalidWebRoot2", new Object[0]);
/* 350 */         return false;
/*     */       }
/*     */     }
/* 353 */     else if (name.equals("sPhysicalFileRoot"))
/*     */     {
/* 355 */       if (!value.equals(""))
/*     */       {
/* 358 */         File file = new File(value);
/* 359 */         if (!file.exists())
/*     */         {
/* 361 */           exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderInvalidFileRoot", new Object[] { value });
/* 362 */           return false;
/*     */         }
/*     */       }
/*     */ 
/* 366 */       String webRoot = this.m_webRootTxt.getText();
/* 367 */       webRoot = webRoot.trim();
/*     */ 
/* 369 */       if ((value.equals("")) && (!webRoot.equals("")))
/*     */       {
/* 371 */         exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderInvalidFileRoot2", new Object[0]);
/* 372 */         return false;
/*     */       }
/*     */     }
/* 375 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 380 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.batchloader.SpiderEditCollectionDialog
 * JD-Core Version:    0.5.4
 */