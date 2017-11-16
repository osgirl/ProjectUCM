/*     */ package intradoc.server.publish;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcTimer;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.config.ConfigFileUtilities;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.ProgressState;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class WebPublishState
/*     */ {
/*     */   public static final int F_PUBLISH_DYNAMIC = 1;
/*     */   public static final int F_PUBLISH_GENERATED = 8;
/*     */   public static final int F_PUBLISH_STATIC = 16;
/*     */   public static final int F_FILTER_STATIC = 32;
/*     */   public static final int F_PUBLISH_IN_THREAD = 256;
/*     */   public static final int F_AT_STARTUP = 1024;
/*     */   public boolean m_isAbort;
/*     */   public int m_flags;
/*     */   public IdcTimer m_timer;
/*     */   public int m_timerFlags;
/*     */   public boolean m_doTrace;
/*     */   public Workspace m_workspace;
/*     */   public ExecutionContext m_context;
/*     */   public ProgressState m_progress;
/*     */   public ConfigFileUtilities m_CFU;
/*     */   public DataBinder m_publishBinder;
/*     */   public PageMerger m_publishMerger;
/*     */   public String m_weblayoutDirectory;
/*     */   public String m_publishDirectory;
/*     */   public String m_lockAgent;
/*     */   public DataBinder m_startupBinder;
/*     */   public StaticPublisher m_staticPublisher;
/*     */   public DataResultSet m_staticFiles;
/*     */   public String[] m_filteredStaticClasses;
/*     */   public String[] m_languages;
/*     */   public Map<String, DataBinder> m_languageBinders;
/*     */   public DynamicPublisher m_dynamicPublisher;
/*     */   public DataResultSet m_dynamicFiles;
/*     */   public WebFeaturesPublisher m_webFeaturesPublisher;
/*     */   public Map<String, PublishedResource> m_publishedResources;
/*     */   public boolean m_doBundling;
/*     */   public Map<String, List<PublishedResourceContainer.Bundle.Filter>> m_bundleFilters;
/*     */   public Map<String, PublishedResourceContainer.Bundle> m_bundles;
/*     */   public Map<String, PublishedResourceContainer.Class> m_publishedClasses;
/*     */   public Map<String, WebFeature> m_features;
/*     */   public PublishedResourceContainer[] m_sortedContainers;
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 120 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80481 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.publish.WebPublishState
 * JD-Core Version:    0.5.4
 */