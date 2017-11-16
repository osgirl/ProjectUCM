/*     */ package intradoc.apputilities.batchloader;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SpiderAddMapDialog
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_sysInterface;
/*     */   protected ExecutionContext m_cxt;
/*  51 */   protected String m_mapDir = null;
/*  52 */   protected String m_mapListFileName = "mapping.hda";
/*  53 */   protected String m_mapDefaultFileName = "default.hda";
/*  54 */   protected String m_mapListTableName = "SpiderMappingList";
/*  55 */   protected String m_mapTableName = "SpiderMapping";
/*  56 */   protected String[] m_mapListFields = { "mapName", "mapDescription" };
/*  57 */   protected String[] m_mapFields = { "mapField", "mapValue" };
/*  58 */   protected int m_mapNameIndex = -1;
/*  59 */   protected int m_mapDescriptionIndex = -1;
/*     */ 
/*     */   public SpiderAddMapDialog(SystemInterface sys)
/*     */   {
/*  63 */     this.m_sysInterface = sys;
/*  64 */     this.m_cxt = sys.getExecutionContext();
/*  65 */     this.m_helper = new DialogHelper(this.m_sysInterface, LocaleResources.getString("csSpiderAddMapDialogTitle", this.m_cxt), true);
/*     */ 
/*  67 */     this.m_mapDir = (LegacyDirectoryLocator.getAppDataDirectory() + "search/external/mapping/");
/*     */   }
/*     */ 
/*     */   public void init(Properties mapProps)
/*     */   {
/*  72 */     initUI();
/*  73 */     this.m_helper.m_props = mapProps;
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  78 */     this.m_helper.initStandard(this, null, 2, false, null);
/*  79 */     JPanel mapPanel = initMapPanel();
/*  80 */     initMapFields(mapPanel);
/*     */   }
/*     */ 
/*     */   public JPanel initMapPanel()
/*     */   {
/*  85 */     JPanel mapPanel = new PanePanel();
/*  86 */     mapPanel.setLayout(new GridBagLayout());
/*  87 */     this.m_helper.makePanelGridBag(this.m_helper.m_mainPanel, 1);
/*  88 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  89 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  90 */     this.m_helper.addLastComponentInRow(this.m_helper.m_mainPanel, mapPanel);
/*     */ 
/*  92 */     return mapPanel;
/*     */   }
/*     */ 
/*     */   public void initMapFields(JPanel mapPanel)
/*     */   {
/*  98 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/*  99 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 100 */     this.m_helper.addComponent(mapPanel, new CustomLabel(LocaleResources.getString("csSpiderMapNameLabel", this.m_cxt), 1));
/*     */ 
/* 103 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 104 */     this.m_helper.addExchangeComponent(mapPanel, new CustomTextField(20), "mapName");
/*     */ 
/* 107 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 108 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 109 */     this.m_helper.addComponent(mapPanel, new CustomLabel(LocaleResources.getString("csSpiderMapDescLabel", this.m_cxt), 1));
/*     */ 
/* 112 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 113 */     this.m_helper.addExchangeComponent(mapPanel, new CustomTextField(40), "mapDescription");
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 118 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(String msg)
/*     */   {
/* 125 */     MessageBox.reportError(this.m_sysInterface.getMainWindow(), msg, LocaleResources.getString("csBatchLoaderMessage", this.m_cxt));
/*     */   }
/*     */ 
/*     */   public void reportError(IdcMessage msg)
/*     */   {
/* 131 */     MessageBox.reportError(this.m_sysInterface, this.m_sysInterface.getMainWindow(), msg, IdcMessageFactory.lc("csBatchLoaderMessage", new Object[0]));
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 139 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 144 */     String name = exchange.m_compName;
/* 145 */     String value = exchange.m_compValue;
/* 146 */     value = value.trim();
/*     */ 
/* 148 */     if (name.equalsIgnoreCase("mapName"))
/*     */     {
/* 150 */       if (value.equals(""))
/*     */       {
/* 152 */         exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderEnterMapName", new Object[0]);
/* 153 */         return false;
/*     */       }
/* 155 */       if (value.indexOf(" ") >= 0)
/*     */       {
/* 157 */         exchange.m_errorMessage = IdcMessageFactory.lc("csSpiderInvalidMapping", new Object[] { value });
/* 158 */         return false;
/*     */       }
/*     */     }
/* 161 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 166 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.batchloader.SpiderAddMapDialog
 * JD-Core Version:    0.5.4
 */