/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.AvsQueryData;
/*     */ import intradoc.shared.ClausesData;
/*     */ import intradoc.shared.CommonQueryData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TaminoQueryData;
/*     */ import intradoc.shared.gui.QueryBuilderHelper;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditQueryDefPanel extends EditQueryBasePanel
/*     */ {
/*     */   protected void initUI()
/*     */   {
/*  45 */     this.m_queryHelper.setTitles(LocaleResources.getString("apLabelField", this.m_ctx), LocaleResources.getString("apLabelValue", this.m_ctx));
/*     */ 
/*  49 */     setCustomValueDisplayMaps();
/*     */ 
/*  51 */     initQueryUI();
/*     */   }
/*     */ 
/*     */   protected void setCustomValueDisplayMaps()
/*     */   {
/*  56 */     this.m_queryHelper.setDisplayMap("prevDates", new String[][] { { "<$dateCurrent(-1)$>", LocaleResources.getString("apOneDayAgo", this.m_ctx) }, { "<$dateCurrent(-7)$>", LocaleResources.getString("apOneWeekAgo", this.m_ctx) }, { "<$dateCurrent(-28)$>", LocaleResources.getString("apFourWeeksAgo", this.m_ctx) } });
/*     */ 
/*  63 */     this.m_queryHelper.setFieldOptionListKey("dInDate", "prevDates", "combo");
/*     */ 
/*  65 */     this.m_queryHelper.setDisplayMap("futureDates", new String[][] { { "<$dateCurrent(1)$>", LocaleResources.getString("apOneDay", this.m_ctx) }, { "<$dateCurrent(7)$>", LocaleResources.getString("apOneWeek", this.m_ctx) }, { "<$dateCurrent(28)$>", LocaleResources.getString("apFourWeeks", this.m_ctx) } });
/*     */ 
/*  72 */     this.m_queryHelper.setFieldOptionListKey("dOutDate", "futureDates", "combo");
/*     */   }
/*     */ 
/*     */   public boolean initQueryUI()
/*     */   {
/*  77 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/*  79 */     JPanel queryClauses = new PanePanel();
/*  80 */     gh.useGridBag(queryClauses);
/*  81 */     this.m_helper.addPanelTitle(queryClauses, LocaleResources.getString("apLabelQueryDefinition", this.m_ctx));
/*     */ 
/*  84 */     JPanel queryDefinitionPanel = new PanePanel();
/*  85 */     gh.m_gc.weightx = 1.0D;
/*  86 */     gh.m_gc.weighty = 1.0D;
/*  87 */     gh.m_gc.fill = 1;
/*  88 */     this.m_helper.addComponent(queryClauses, queryDefinitionPanel);
/*     */ 
/*  90 */     this.m_queryHelper.createStandardQueryPanel(this.m_helper, queryDefinitionPanel, this.m_pageServices.getSharedContext());
/*     */ 
/*  93 */     ClausesData queryData = null;
/*     */ 
/*  95 */     boolean useAltaVista = SharedObjects.getEnvValueAsBoolean("UseAltaVista", false);
/*  96 */     boolean useTamino = (SharedObjects.getEnvValueAsBoolean("UseTamino", false)) || (SharedObjects.getEnvValueAsBoolean("UseTaminoXML", false));
/*     */ 
/*  99 */     if (useAltaVista)
/*     */     {
/* 101 */       queryData = new AvsQueryData();
/*     */     }
/* 103 */     else if (useTamino)
/*     */     {
/* 105 */       queryData = new TaminoQueryData();
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 111 */         queryData = new CommonQueryData();
/* 112 */         ((CommonQueryData)queryData).init(null, true);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 116 */         reportError(e, IdcMessageFactory.lc("apErrorLoadingQueryData", new Object[0]));
/* 117 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 121 */     this.m_queryHelper.setData(queryData, this.m_queryDataStr);
/*     */ 
/* 124 */     this.m_queryHelper.markQueryPropForUrl("SortField");
/* 125 */     this.m_queryHelper.markQueryPropForUrl("SortOrder");
/* 126 */     this.m_queryHelper.markQueryPropForUrl("ResultsTitle");
/*     */ 
/* 129 */     gh.m_gc.weightx = 1.0D;
/* 130 */     gh.m_gc.weighty = 1.0D;
/* 131 */     gh.m_gc.fill = 1;
/* 132 */     this.m_helper.addLastComponentInRow(this, queryClauses);
/*     */     try
/*     */     {
/* 137 */       this.m_queryHelper.loadData();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 141 */       reportError(e, IdcMessageFactory.lc("apErrorLoadingQueryData", new Object[0]));
/* 142 */       return false;
/*     */     }
/*     */ 
/* 145 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 150 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditQueryDefPanel
 * JD-Core Version:    0.5.4
 */