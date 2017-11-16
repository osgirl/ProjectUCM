/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ResourcePanel extends BaseResViewPanel
/*     */ {
/*     */   public static final String RES_TABLE_INC_STRING_TYPE = "tablesIncludesStrings";
/*     */   public static final String RES_TABLE_INC_TYPE = "tablesIncludes";
/*     */   public static final String RES_TABLE_STRING_TYPE = "tablesStrings";
/*     */   public static final String RES_INC_STRING_TYPE = "includesStrings";
/*     */   public static final String RES_STRING_TYPE = "resStrings";
/*     */   public static final String RES_INC_DATA_HTML_TYPE = "htmlDataIncludes";
/*     */   public static final String HTML_INC_TYPE = "htmlIncludes";
/*     */   public static final String DATA_INC_TYPE = "dataIncludes";
/*     */   public static final String RES_TABLE_TYPE = "resourceTables";
/*     */   protected BaseResViewPanel[] m_tabPanels;
/*     */   protected final String[][] PANEL_TYPES;
/*     */   protected final String[][] PANEL_INFOS;
/*     */ 
/*     */   public ResourcePanel()
/*     */   {
/*  50 */     this.m_tabPanels = null;
/*     */ 
/*  53 */     this.PANEL_TYPES = new String[][] { { "tablesStrings", "resStrings,resourceTables" }, { "tablesIncludes", "htmlIncludes,dataIncludes,resourceTables" }, { "includesStrings", "htmlIncludes,dataIncludes,resStrings" }, { "htmlDataIncludes", "htmlIncludes,dataIncludes" }, { "tablesIncludesStrings", "htmlIncludes,dataIncludes,resStrings,resourceTables" } };
/*     */ 
/*  61 */     this.PANEL_INFOS = new String[][] { { "htmlIncludes", "intradoc.apputilities.componentwizard.ResourceBasePanel", "csCompWizLabelHTMLIncludes" }, { "dataIncludes", "intradoc.apputilities.componentwizard.ResourceBasePanel", "csCompWizLabelDataIncludes" }, { "resStrings", "intradoc.apputilities.componentwizard.ResourceBasePanel", "csCompWizLabelResourceStrings" }, { "resourceTables", "intradoc.apputilities.componentwizard.ResourceBasePanel", "csCompWizLabelResourceTables" } };
/*     */   }
/*     */ 
/*     */   public void initUI(boolean isTab)
/*     */   {
/*     */     try
/*     */     {
/*  73 */       this.m_helper.makePanelGridBag(this, 1);
/*  74 */       this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  75 */       this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  76 */       this.m_helper.addComponent(this, initInfoPanel());
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  80 */       Report.trace(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void assignResourceFileInfo(ResourceFileInfo info)
/*     */   {
/*  87 */     super.assignResourceFileInfo(info);
/*     */ 
/*  89 */     Vector panels = findInfoPanels(this.m_resourcePanelType);
/*     */ 
/*  91 */     for (int i = 0; i < panels.size(); ++i)
/*     */     {
/*  93 */       this.m_tabPanels[i].m_mergeRules = this.m_mergeRules;
/*  94 */       this.m_tabPanels[i].m_component = this.m_component;
/*  95 */       this.m_tabPanels[i].assignResourceFileInfo(info);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected JPanel initInfoPanel() throws ServiceException
/*     */   {
/* 101 */     TabPanel tab = new TabPanel();
/* 102 */     Vector panels = findInfoPanels(this.m_resourcePanelType);
/*     */ 
/* 104 */     if (panels == null)
/*     */     {
/* 106 */       throw new ServiceException("!csUnableToLoadTabPanels");
/*     */     }
/* 108 */     this.m_tabPanels = new BaseResViewPanel[panels.size()];
/*     */ 
/* 110 */     for (int k = 0; k < panels.size(); ++k)
/*     */     {
/* 112 */       int resourceType = 2;
/* 113 */       String name = (String)panels.elementAt(k);
/* 114 */       String classPath = null;
/* 115 */       String tabStr = null;
/*     */ 
/* 117 */       for (int i = 0; i < this.PANEL_INFOS.length; ++i)
/*     */       {
/* 119 */         if (!name.equals(this.PANEL_INFOS[i][0]))
/*     */           continue;
/* 121 */         classPath = this.PANEL_INFOS[i][1];
/* 122 */         tabStr = this.PANEL_INFOS[i][2];
/* 123 */         break;
/*     */       }
/*     */ 
/* 127 */       if (classPath == null)
/*     */       {
/* 129 */         throw new ServiceException(LocaleUtils.encodeMessage("csUnableToLoadTabPanelFor", null, name));
/*     */       }
/*     */ 
/* 132 */       if (name.equals("htmlIncludes"))
/*     */       {
/* 134 */         resourceType = 0;
/*     */       }
/* 136 */       else if (name.equals("dataIncludes"))
/*     */       {
/* 138 */         resourceType = 1;
/*     */       }
/* 140 */       else if (name.equals("resStrings"))
/*     */       {
/* 142 */         resourceType = 7;
/*     */       }
/*     */ 
/* 145 */       this.m_tabPanels[k] = ((BaseResViewPanel)ComponentClassFactory.createClassInstance(name, classPath, LocaleUtils.encodeMessage("csUnableToLoadTabPanelFor", null, name)));
/*     */ 
/* 148 */       this.m_tabPanels[k].init(this.m_helper);
/* 149 */       this.m_tabPanels[k].initUI(true, resourceType);
/* 150 */       this.m_tabPanels[k].assignResourceFileInfo(this.m_fileInfo);
/* 151 */       tab.addPane(LocaleResources.getString(tabStr, null), this.m_tabPanels[k]);
/*     */     }
/*     */ 
/* 154 */     return tab;
/*     */   }
/*     */ 
/*     */   protected Vector findInfoPanels(String resPanelType)
/*     */   {
/* 159 */     Vector panels = null;
/*     */ 
/* 161 */     for (int i = 0; i < this.PANEL_TYPES.length; ++i)
/*     */     {
/* 163 */       if (!this.PANEL_TYPES[i][0].equals(resPanelType))
/*     */         continue;
/* 165 */       panels = StringUtils.parseArray(this.PANEL_TYPES[i][1], ',', '^');
/* 166 */       break;
/*     */     }
/*     */ 
/* 170 */     return panels;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 175 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.ResourcePanel
 * JD-Core Version:    0.5.4
 */