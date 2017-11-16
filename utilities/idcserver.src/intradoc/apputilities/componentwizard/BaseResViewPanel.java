/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class BaseResViewPanel extends CWizardPanel
/*     */   implements ComponentBinder, ActionListener
/*     */ {
/*  58 */   protected DialogHelper m_dlgHelper = null;
/*  59 */   protected UdlPanel m_list = null;
/*  60 */   protected EditBasePanel m_editPanel = null;
/*     */ 
/*  63 */   protected DataResultSet m_listData = null;
/*  64 */   protected ResourceFileInfo m_fileInfo = null;
/*  65 */   protected DataResultSet m_mergeRules = null;
/*     */ 
/*  67 */   protected int m_editType = 0;
/*  68 */   protected String m_resourcePanelType = null;
/*     */ 
/*     */   public void init(SystemInterface sys)
/*     */   {
/*  77 */     this.m_systemInterface = sys;
/*  78 */     this.m_helper = new ContainerHelper();
/*  79 */     this.m_helper.attachToContainer(this, sys, null);
/*     */   }
/*     */ 
/*     */   public void init(ContainerHelper helper)
/*     */   {
/*  84 */     this.m_systemInterface = helper.m_exchange.m_sysInterface;
/*     */ 
/*  86 */     this.m_helper = new ContainerHelper();
/*  87 */     this.m_helper.attachToContainer(this, this.m_systemInterface, new Properties());
/*  88 */     this.m_helper.m_exchange = helper.m_exchange;
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  94 */     initUI(false);
/*     */   }
/*     */ 
/*     */   public void initUI(boolean isTab, int resourceType)
/*     */   {
/* 100 */     initUI(isTab);
/*     */   }
/*     */ 
/*     */   public void initUI(boolean isTab)
/*     */   {
/* 105 */     setLayout(new BorderLayout());
/* 106 */     JPanel text = new CustomText("", 60);
/* 107 */     add("North", text);
/* 108 */     this.m_helper.m_exchange.addComponent("HelpMessage", text, null);
/*     */   }
/*     */ 
/*     */   public void assignResourceInfo(Properties props, ResourceFileInfo fi, IntradocComponent component)
/*     */   {
/* 114 */     this.m_helper.m_props = props;
/* 115 */     this.m_helper.loadComponentValues();
/* 116 */     this.m_component = component;
/* 117 */     assignResourceFileInfo(fi);
/*     */   }
/*     */ 
/*     */   public void setProperties(Properties props)
/*     */   {
/* 122 */     this.m_helper.m_props = props;
/* 123 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public void setResourcePanelType(String resPanelType)
/*     */   {
/* 128 */     this.m_resourcePanelType = resPanelType;
/*     */   }
/*     */ 
/*     */   public ResourceFileInfo getResourceFileInfo()
/*     */   {
/* 133 */     return this.m_fileInfo;
/*     */   }
/*     */ 
/*     */   public void assignResourceFileInfo(ResourceFileInfo info)
/*     */   {
/* 138 */     this.m_fileInfo = info;
/*     */   }
/*     */ 
/*     */   protected void refreshList(String selObj)
/*     */   {
/* 143 */     this.m_list.refreshList(this.m_listData, selObj);
/*     */   }
/*     */ 
/*     */   protected JPanel addToolBarPanel()
/*     */   {
/* 149 */     JPanel panel = new PanePanel();
/* 150 */     panel.setLayout(new FlowLayout());
/*     */ 
/* 152 */     for (int i = 0; i < CWizardGuiUtils.LIST_COMMANDS.length; ++i)
/*     */     {
/* 154 */       String command = CWizardGuiUtils.LIST_COMMANDS[i][1];
/* 155 */       boolean flag = true;
/* 156 */       if (command.equals("add"))
/*     */       {
/* 158 */         flag = false;
/*     */       }
/* 160 */       JButton btn = this.m_list.addButton(LocaleResources.getString(CWizardGuiUtils.LIST_COMMANDS[i][0], null), flag);
/* 161 */       btn.setActionCommand(command);
/* 162 */       btn.addActionListener(this);
/* 163 */       panel.add(btn);
/*     */     }
/*     */ 
/* 166 */     this.m_list.m_list.addActionListener(this);
/*     */ 
/* 168 */     return panel;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 173 */     String cmdStr = e.getActionCommand();
/*     */ 
/* 175 */     if (cmdStr.equals("add"))
/*     */     {
/* 177 */       this.m_editType = 1;
/*     */     }
/* 179 */     else if (cmdStr.equals("delete"))
/*     */     {
/* 181 */       this.m_editType = 3;
/*     */     }
/*     */     else
/*     */     {
/* 185 */       this.m_editType = 2;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 190 */       updateResourceFileInfo();
/*     */     }
/*     */     catch (Exception excp)
/*     */     {
/* 194 */       CWizardGuiUtils.reportError(this.m_systemInterface, excp, (IdcMessage)null);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void updateResourceFileInfo() throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   protected void addOrEditOrDelete(Properties props, IdcMessage msg, IdcMessage title) throws ServiceException
/*     */   {
/* 204 */     String type = props.getProperty("type");
/* 205 */     String columnName = props.getProperty("columnName");
/* 206 */     String filename = props.getProperty("filename");
/*     */ 
/* 208 */     if ((type == null) || (type.length() == 0) || (columnName == null) || (columnName.length() == 0) || (filename == null) || (filename.length() == 0))
/*     */     {
/* 212 */       return;
/*     */     }
/*     */ 
/* 215 */     String absPath = FileUtils.getAbsolutePath(this.m_component.m_absCompDir, filename);
/* 216 */     boolean isReadOnly = CWizardUtils.isReadOnly(absPath);
/*     */     Properties tempProps;
/*     */     Enumeration en;
/* 218 */     if ((this.m_editType == 2) || (this.m_editType == 3))
/*     */     {
/* 220 */       int index = this.m_list.getSelectedIndex();
/* 221 */       if (index < 0)
/*     */       {
/* 223 */         CWizardGuiUtils.reportError(this.m_systemInterface, null, msg);
/* 224 */         return;
/*     */       }
/*     */ 
/* 227 */       tempProps = this.m_list.getDataAt(index);
/* 228 */       for (en = tempProps.keys(); en.hasMoreElements(); )
/*     */       {
/* 230 */         String name = (String)en.nextElement();
/* 231 */         String value = tempProps.getProperty(name);
/* 232 */         if (value != null)
/*     */         {
/* 234 */           props.put(name, value);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 239 */     if ((type.equals("template")) && (this.m_editType == 2))
/*     */     {
/* 241 */       String resfilename = props.getProperty("filename");
/* 242 */       if ((resfilename != null) && (resfilename.length() > 0))
/*     */       {
/* 244 */         props.put("resfilename", resfilename);
/*     */       }
/*     */     }
/*     */ 
/* 248 */     String name = props.getProperty(columnName);
/* 249 */     if (((this.m_editType != 2) && (this.m_editType == 3)) || 
/* 254 */       (isReadOnly))
/*     */     {
/* 256 */       title.m_prior = IdcMessageFactory.lc("csCompWizTitleReadOnly", new Object[] { filename });
/*     */     }
/*     */ 
/* 259 */     this.m_dlgHelper = new DialogHelper(this.m_systemInterface, LocaleResources.localizeMessage(null, title, null).toString(), true);
/*     */ 
/* 261 */     this.m_dlgHelper.m_props = props;
/*     */ 
/* 263 */     if (this.m_editType == 3)
/*     */     {
/* 266 */       if (isReadOnly)
/*     */       {
/* 268 */         CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("syFileReadOnly", new Object[] { filename }));
/*     */ 
/* 270 */         return;
/*     */       }
/* 272 */       if (CWizardGuiUtils.doMessage(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizPromptRemove", new Object[] { name }), 4) == 2)
/*     */       {
/*     */         try
/*     */         {
/* 278 */           this.m_component.createOrEditResourceFileInfo(this.m_editType, this.m_dlgHelper.m_props, this.m_fileInfo);
/*     */ 
/* 280 */           Vector v = this.m_listData.findRow(0, name);
/* 281 */           if (v != null)
/*     */           {
/* 283 */             this.m_listData.deleteCurrentRow();
/*     */           }
/* 285 */           refreshList(null);
/* 286 */           return;
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 290 */           throw new ServiceException(e);
/*     */         }
/*     */       }
/* 293 */       return;
/*     */     }
/*     */ 
/* 296 */     addOrEdit(type);
/*     */   }
/*     */ 
/*     */   protected void addOrEdit(String type) throws ServiceException
/*     */   {
/* 301 */     JPanel mainPanel = this.m_dlgHelper.m_mainPanel;
/* 302 */     this.m_dlgHelper.makePanelGridBag(mainPanel, 1);
/* 303 */     this.m_dlgHelper.m_gridHelper.m_gc.weightx = 1.0D;
/* 304 */     this.m_dlgHelper.m_gridHelper.m_gc.weighty = 1.0D;
/*     */ 
/* 306 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 313 */           if (BaseResViewPanel.this.m_editPanel.validateEntries())
/*     */           {
/* 315 */             BaseResViewPanel.this.onOk();
/* 316 */             return true;
/*     */           }
/* 318 */           return false;
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 322 */           CWizardGuiUtils.reportError(BaseResViewPanel.this.m_systemInterface, exp, (IdcMessage)null);
/* 323 */         }return false;
/*     */       }
/*     */     };
/* 327 */     this.m_dlgHelper.addOK(okCallback);
/* 328 */     this.m_dlgHelper.addCancel(null);
/* 329 */     this.m_dlgHelper.addHelp(null);
/*     */ 
/* 331 */     this.m_dlgHelper.m_gridHelper.prepareAddLastRowElement(12);
/* 332 */     this.m_dlgHelper.addComponent(mainPanel, this.m_editPanel);
/*     */ 
/* 335 */     this.m_dlgHelper.prompt();
/*     */   }
/*     */ 
/*     */   protected void onOk() throws ServiceException
/*     */   {
/* 340 */     Properties props = this.m_dlgHelper.m_props;
/* 341 */     String columnName = props.getProperty("columnName");
/* 342 */     String name = props.getProperty(columnName);
/* 343 */     if ((columnName == null) || (columnName.length() == 0))
/*     */     {
/* 345 */       return;
/*     */     }
/*     */ 
/* 348 */     this.m_component.createOrEditResourceFileInfo(this.m_editType, this.m_dlgHelper.m_props, this.m_fileInfo);
/* 349 */     assignResourceFileInfo(this.m_fileInfo);
/*     */ 
/* 351 */     if (columnName.equals("includeOrString"))
/*     */     {
/* 353 */       int resType = CWizardUtils.determineCoreResourceTypeFromCheckboxes(props);
/* 354 */       this.m_listData = ((DataResultSet)CWizardUtils.buildIncludeOrStringResultSetByType(this.m_fileInfo, resType));
/*     */     }
/*     */     else
/*     */     {
/* 358 */       String tablename = props.getProperty("tablename");
/* 359 */       this.m_listData = ((DataResultSet)this.m_fileInfo.m_tables.get(tablename));
/*     */     }
/*     */ 
/* 362 */     refreshList(name);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 370 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 375 */     return this.m_helper.validateComponentValue(exchange);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 380 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.BaseResViewPanel
 * JD-Core Version:    0.5.4
 */