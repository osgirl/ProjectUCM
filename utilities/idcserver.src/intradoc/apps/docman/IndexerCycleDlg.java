/*     */ package intradoc.apps.docman;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class IndexerCycleDlg
/*     */   implements ComponentBinder
/*     */ {
/*  52 */   protected String[] m_numericFields = { "MaxCollectionSize", "IndexerCheckpointCount" };
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected String m_helpPage;
/*     */   protected String m_cycleID;
/*     */   protected Properties m_props;
/*  62 */   protected JPanel m_mainPanel = null;
/*     */ 
/*     */   public IndexerCycleDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  66 */     this.m_helper = new DialogHelper(sys, title, true);
/*  67 */     this.m_systemInterface = sys;
/*  68 */     this.m_helpPage = helpPage;
/*  69 */     this.m_cxt = sys.getExecutionContext();
/*     */   }
/*     */ 
/*     */   public void init(DialogCallback okCallback, Properties props)
/*     */   {
/*  74 */     this.m_helper.m_props = new Properties();
/*  75 */     this.m_props = props;
/*     */ 
/*  77 */     this.m_mainPanel = this.m_helper.initStandard(this, okCallback, 2, true, this.m_helpPage);
/*     */ 
/*  80 */     this.m_helper.makePanelGridBag(this.m_mainPanel, 1);
/*  81 */     GridBagConstraints gbc = this.m_helper.m_gridHelper.m_gc;
/*  82 */     gbc.insets = new Insets(2, 5, 2, 5);
/*     */ 
/*  84 */     this.m_cycleID = ((String)props.get("cycleID"));
/*  85 */     String description = (String)props.get("description");
/*     */ 
/*  87 */     String[][] debugLevelMap = (String[][])null;
/*  88 */     String debugLevelStr = SharedObjects.getEnvironmentValue("IndexerDebugLevels");
/*  89 */     if (debugLevelStr != null)
/*     */     {
/*  91 */       Vector debugLevels = StringUtils.parseArray(debugLevelStr, ',', ',');
/*  92 */       debugLevelMap = new String[debugLevels.size()][2];
/*  93 */       for (int i = 0; i < debugLevelMap.length; ++i)
/*     */       {
/*  95 */         String level = (String)debugLevels.elementAt(i);
/*  96 */         debugLevelMap[i] = { level, LocaleResources.getString("apIndexerDebug_" + level, this.m_cxt) };
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 102 */       debugLevelMap = new String[][] { { "none", LocaleResources.getString("apIndexerDebug_none", this.m_cxt) }, { "verbose", LocaleResources.getString("apIndexerDebug_verbose", this.m_cxt) }, { "debug", LocaleResources.getString("apIndexerDebug_debug", this.m_cxt) }, { "trace", LocaleResources.getString("apIndexerDebug_trace", this.m_cxt) }, { "all", LocaleResources.getString("apIndexerDebug_all", this.m_cxt) } };
/*     */     }
/*     */ 
/* 112 */     String batchSize = SharedObjects.getEnvironmentValue("IndexerAvailableBatchSizes");
/* 113 */     if (batchSize == null)
/*     */     {
/* 115 */       batchSize = "1,5,10,25,100,500";
/*     */     }
/*     */ 
/* 118 */     String checkPointCount = SharedObjects.getEnvironmentValue("IndexerAvailableCheckPointCounts");
/* 119 */     if (checkPointCount == null)
/*     */     {
/* 121 */       checkPointCount = "500,1000,5000,10000";
/*     */     }
/* 123 */     gbc.gridwidth = 0;
/* 124 */     CustomText ct = new CustomText(description, 75);
/* 125 */     this.m_mainPanel.add(ct, gbc);
/*     */ 
/* 127 */     gbc.gridwidth = 1;
/* 128 */     addItem(this.m_mainPanel, "MaxCollectionSize", "apIndexerMaxCollectionSize", true, batchSize);
/* 129 */     addItem(this.m_mainPanel, "IndexerCheckpointCount", "apIndexerCheckpointCount", true, checkPointCount);
/*     */ 
/* 131 */     addItem(this.m_mainPanel, "SearchDebugLevel", "apIndexerSearchDebugLevel", false, debugLevelMap);
/*     */ 
/* 133 */     if (!this.m_cycleID.equals("update"))
/*     */       return;
/* 135 */     JCheckBox control = new JCheckBox(LocaleResources.getString("apTitleEnabled", this.m_cxt));
/* 136 */     addItem(this.m_mainPanel, "sEnableAutoUpdate", "apIndexerEnableAutoUpdate", false, control);
/*     */   }
/*     */ 
/*     */   public Component createLabel(String label)
/*     */   {
/* 142 */     label = LocaleResources.getString(label, this.m_cxt);
/* 143 */     return new CustomLabel(label, 1);
/*     */   }
/*     */ 
/*     */   public void addItem(JPanel panel, String name, String label, boolean isCombo, Object data)
/*     */   {
/* 149 */     Component comp = createLabel(label);
/* 150 */     this.m_helper.addComponent(panel, comp);
/* 151 */     String value = lookupValue(name);
/*     */     Component control;
/*     */     Component control;
/* 153 */     if ((data instanceof String[][]) || (data instanceof String))
/*     */     {
/*     */       String[][] options;
/*     */       String[][] options;
/* 156 */       if (data instanceof String[][])
/*     */       {
/* 158 */         options = (String[][])(String[][])data;
/*     */       }
/*     */       else
/*     */       {
/* 162 */         Vector list = StringUtils.parseArray((String)data, ',', '^');
/* 163 */         options = new String[list.size()][];
/* 164 */         for (int i = 0; i < options.length; ++i)
/*     */         {
/* 166 */           String tmp = (String)list.elementAt(i);
/* 167 */           options[i] = { tmp, tmp };
/*     */         }
/*     */       }
/*     */ 
/* 171 */       if (isCombo)
/*     */       {
/* 173 */         ComboChoice choice = new ComboChoice(value);
/* 174 */         Component control = choice;
/* 175 */         choice.initChoiceList(options);
/*     */       }
/*     */       else
/*     */       {
/* 183 */         DisplayChoice list = new DisplayChoice(value);
/* 184 */         list.init(options);
/* 185 */         list.select(value);
/* 186 */         control = list;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 191 */       control = (Component)data;
/*     */     }
/*     */ 
/* 194 */     this.m_helper.addExchangeComponent(panel, control, name);
/* 195 */     this.m_helper.addLastComponentInRow(panel, new CustomLabel(""));
/*     */   }
/*     */ 
/*     */   public String lookupValue(String name)
/*     */   {
/* 200 */     String value = (String)this.m_props.get(name);
/* 201 */     if (value != null)
/*     */     {
/* 203 */       this.m_helper.m_props.put(name, value);
/*     */     }
/* 205 */     return value;
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 210 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public Properties getData()
/*     */   {
/* 215 */     return this.m_helper.m_props;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 224 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 225 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 230 */     String name = exchange.m_compName;
/* 231 */     String val = exchange.m_compValue;
/* 232 */     boolean isNumeric = false;
/*     */ 
/* 234 */     for (int i = 0; (!isNumeric) && (i < this.m_numericFields.length); ++i)
/*     */     {
/* 236 */       isNumeric = name.equals(this.m_numericFields[i]);
/*     */     }
/*     */ 
/* 242 */     return (val != null) && (val.length() != 0) && (((!isNumeric) || (NumberUtils.parseInteger(val, 0) >= 1)));
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 250 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.IndexerCycleDlg
 * JD-Core Version:    0.5.4
 */