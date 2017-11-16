/*     */ package intradoc.apputilities.batchloader;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SpiderEditFieldDialog
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_sysInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected FixedSizeList m_valueList;
/*     */   protected CustomTextField m_valueTxt;
/*  64 */   protected boolean m_isAdd = false;
/*  65 */   protected DataResultSet m_mapSet = null;
/*     */ 
/*  67 */   protected String m_mapDir = null;
/*  68 */   protected String m_mapListFileName = "mapping.hda";
/*  69 */   protected String m_mapDefaultFileName = "default.hda";
/*  70 */   protected String m_mapListTableName = "SpiderMappingList";
/*  71 */   protected String m_mapTableName = "SpiderMapping";
/*  72 */   protected String[] m_mapListFields = { "mapName", "mapDescription" };
/*  73 */   protected String[] m_mapFields = { "mapField", "mapValue" };
/*     */ 
/*     */   public SpiderEditFieldDialog(SystemInterface sys)
/*     */   {
/*  77 */     this.m_cxt = sys.getExecutionContext();
/*  78 */     this.m_sysInterface = sys;
/*  79 */     this.m_mapDir = (LegacyDirectoryLocator.getAppDataDirectory() + "search/external/mapping/");
/*     */   }
/*     */ 
/*     */   public void init(boolean isAdd, Properties mapProps, DataResultSet mapSet)
/*     */   {
/*  84 */     String title = null;
/*  85 */     if (isAdd)
/*     */     {
/*  87 */       title = "csSpiderAddFieldDialogTitle";
/*     */     }
/*     */     else
/*     */     {
/*  91 */       title = "csSpiderEditFieldDialogTitle";
/*     */     }
/*  93 */     title = LocaleResources.getString(title, this.m_cxt);
/*  94 */     this.m_helper = new DialogHelper(this.m_sysInterface, LocaleResources.getString(title, this.m_cxt), true);
/*     */ 
/*  96 */     this.m_helper.m_props = mapProps;
/*  97 */     this.m_mapSet = mapSet;
/*  98 */     this.m_isAdd = isAdd;
/*  99 */     initUI();
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/* 105 */     this.m_helper.initStandard(this, null, 2, false, null);
/* 106 */     JPanel mapPanel = initMapPanel();
/* 107 */     initMapFields(mapPanel);
/*     */   }
/*     */ 
/*     */   public JPanel initMapPanel()
/*     */   {
/* 112 */     JPanel mapPanel = new PanePanel();
/* 113 */     mapPanel.setLayout(new GridBagLayout());
/* 114 */     this.m_helper.makePanelGridBag(this.m_helper.m_mainPanel, 1);
/* 115 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 116 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 117 */     this.m_helper.addLastComponentInRow(this.m_helper.m_mainPanel, mapPanel);
/*     */ 
/* 119 */     return mapPanel;
/*     */   }
/*     */ 
/*     */   public void initMapFields(JPanel mapPanel)
/*     */   {
/* 125 */     Component mapFieldComp = null;
/* 126 */     if (this.m_isAdd)
/*     */     {
/* 128 */       mapFieldComp = new CustomTextField(30);
/*     */     }
/*     */     else
/*     */     {
/* 132 */       mapFieldComp = new CustomLabel("", 0);
/*     */     }
/*     */ 
/* 136 */     this.m_helper.m_gridHelper.prepareAddRowElement(18);
/* 137 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 138 */     this.m_helper.addComponent(mapPanel, new CustomLabel(LocaleResources.getString("csSpiderMapFieldLabel", this.m_cxt), 1));
/*     */ 
/* 141 */     this.m_helper.m_gridHelper.prepareAddRowElement(18);
/* 142 */     this.m_helper.addExchangeComponent(mapPanel, mapFieldComp, "mapField");
/*     */ 
/* 145 */     JPanel fieldPanel = new PanePanel();
/* 146 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/* 147 */     this.m_helper.addComponent(mapPanel, fieldPanel);
/*     */ 
/* 150 */     this.m_valueTxt = new CustomTextField(40);
/* 151 */     this.m_helper.m_gridHelper.prepareAddRowElement(18);
/* 152 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 153 */     this.m_helper.addComponent(mapPanel, new CustomLabel(LocaleResources.getString("csSpiderMapValueLabel", this.m_cxt), 1));
/*     */ 
/* 156 */     this.m_helper.m_gridHelper.prepareAddRowElement(18);
/* 157 */     this.m_helper.addExchangeComponent(mapPanel, this.m_valueTxt, "mapValue");
/*     */ 
/* 159 */     JButton addValueBtn = new JButton(" << ");
/* 160 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/* 161 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 162 */     this.m_helper.m_gridHelper.prepareAddRowElement(11);
/* 163 */     this.m_helper.addComponent(mapPanel, addValueBtn);
/*     */ 
/* 165 */     this.m_valueList = new FixedSizeList(8);
/* 166 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 167 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(11);
/* 168 */     this.m_helper.addComponent(mapPanel, this.m_valueList);
/*     */ 
/* 171 */     this.m_valueList.add("<$dir1$>");
/* 172 */     this.m_valueList.add("<$dir2$>");
/* 173 */     this.m_valueList.add("<$extension$>");
/* 174 */     this.m_valueList.add("<$filename$>");
/* 175 */     this.m_valueList.add("<$filepath$>");
/* 176 */     this.m_valueList.add("<$filesize$>");
/* 177 */     this.m_valueList.add("<$filetimestamp$>");
/* 178 */     this.m_valueList.add("<$URL$>");
/*     */ 
/* 181 */     ActionListener addListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/* 185 */         String item = SpiderEditFieldDialog.this.m_valueList.getSelectedItem();
/* 186 */         if (item == null)
/*     */           return;
/* 188 */         String text = SpiderEditFieldDialog.this.m_valueTxt.getText();
/* 189 */         SpiderEditFieldDialog.this.m_valueTxt.setText(text + item);
/*     */       }
/*     */     };
/* 193 */     addValueBtn.addActionListener(addListener);
/*     */   }
/*     */ 
/*     */   public boolean isFieldExists(String mapField)
/*     */   {
/*     */     try
/*     */     {
/* 201 */       int mapFieldIndex = ResultSetUtils.getIndexMustExist(this.m_mapSet, "mapField");
/* 202 */       if (this.m_mapSet.findRow(mapFieldIndex, mapField) != null)
/*     */       {
/* 204 */         return true;
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 209 */       if (SystemUtils.m_verbose)
/*     */       {
/* 211 */         Report.debug("applet", null, e);
/*     */       }
/*     */     }
/* 214 */     return false;
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 219 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(String msg)
/*     */   {
/* 226 */     MessageBox.reportError(this.m_sysInterface.getMainWindow(), msg, LocaleResources.getString("csBatchLoaderMessage", this.m_cxt));
/*     */   }
/*     */ 
/*     */   public void reportError(IdcMessage msg)
/*     */   {
/* 232 */     MessageBox.reportError(this.m_sysInterface, this.m_sysInterface.getMainWindow(), msg, IdcMessageFactory.lc("csBatchLoaderMessage", new Object[0]));
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 240 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 245 */     String name = exchange.m_compName;
/* 246 */     String value = exchange.m_compValue;
/* 247 */     value = value.trim();
/*     */ 
/* 249 */     if (name.equalsIgnoreCase("mapField"))
/*     */     {
/* 251 */       if (value.equals(""))
/*     */       {
/* 253 */         exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderEnterFieldName", new Object[0]);
/* 254 */         return false;
/*     */       }
/* 256 */       if ((this.m_isAdd) && (isFieldExists(value)))
/*     */       {
/* 258 */         exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderFieldNameExists", new Object[] { value });
/* 259 */         return false;
/*     */       }
/*     */     }
/* 262 */     else if ((name.equalsIgnoreCase("mapValue")) && 
/* 264 */       (value.equals("")))
/*     */     {
/* 266 */       exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderEnterFieldValue", new Object[] { value });
/* 267 */       return false;
/*     */     }
/*     */ 
/* 271 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 276 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.batchloader.SpiderEditFieldDialog
 * JD-Core Version:    0.5.4
 */