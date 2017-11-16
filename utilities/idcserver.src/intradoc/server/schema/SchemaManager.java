/*     */ package intradoc.server.schema;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ 
/*     */ public class SchemaManager
/*     */ {
/*     */ 
/*     */   @Deprecated
/*  36 */   protected static String m_schemaDir = null;
/*     */ 
/*     */   @Deprecated
/*  39 */   public static SchemaStorage m_tables = null;
/*     */ 
/*     */   @Deprecated
/*  42 */   public static SchemaStorage m_views = null;
/*     */ 
/*     */   @Deprecated
/*  45 */   public static SchemaStorage m_relations = null;
/*     */ 
/*     */   @Deprecated
/*  48 */   public static SchemaStorage m_fields = null;
/*     */ 
/*  50 */   protected static ServerSchemaManager m_manager = null;
/*  51 */   protected static boolean m_isInitialized = false;
/*     */ 
/*     */   @Deprecated
/*     */   public static ExecutionContext m_context;
/*     */ 
/*     */   public static void init(Workspace ws)
/*     */     throws DataException, ServiceException
/*     */   {
/*  60 */     if (m_isInitialized)
/*     */       return;
/*  62 */     m_manager = (ServerSchemaManager)ComponentClassFactory.createClassInstance("ServerSchemaManager", "intradoc.server.schema.StandardSchemaManager", null);
/*     */ 
/*  66 */     m_context = new ExecutionContextAdaptor();
/*  67 */     m_schemaDir = LegacyDirectoryLocator.getAppDataDirectory() + "schema/";
/*     */ 
/*  69 */     m_manager.init(ws);
/*  70 */     m_tables = m_manager.getStorageImplementor("SchemaTableConfig");
/*  71 */     m_views = m_manager.getStorageImplementor("SchemaViewConfig");
/*  72 */     m_relations = m_manager.getStorageImplementor("SchemaRelationConfig");
/*  73 */     m_fields = m_manager.getStorageImplementor("SchemaFieldConfig");
/*  74 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public static ServerSchemaManager getManager(Workspace ws)
/*     */     throws DataException, ServiceException
/*     */   {
/*  81 */     init(ws);
/*  82 */     return m_manager;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void publish(long timer, boolean isImmediate)
/*     */     throws ServiceException
/*     */   {
/*  91 */     DataBinder settings = new DataBinder();
/*  92 */     m_manager.publish(timer, isImmediate, settings);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void publish(long timer, boolean isImmediate, DataBinder settings)
/*     */     throws ServiceException
/*     */   {
/* 101 */     m_manager.publish(timer, isImmediate, settings);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void resetPublishingTimers()
/*     */   {
/* 109 */     m_manager.resetPublishingTimers();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void addSchemaExistingTable(Workspace ws, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 118 */     m_manager.addSchemaExistingTable(ws, binder);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void addSchemaTable(Workspace ws, DataBinder binder)
/*     */     throws ServiceException, DataException
/*     */   {
/* 127 */     m_manager.addSchemaTable(ws, binder);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void editSchemaTable(Workspace ws, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 136 */     m_manager.editSchemaTable(ws, binder);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void deleteSchemaTable(Workspace ws, DataBinder binder)
/*     */     throws ServiceException, DataException
/*     */   {
/* 145 */     m_manager.deleteSchemaTable(ws, binder);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void getSchemaData(DataBinder binder, String rsetName, String prefix)
/*     */     throws DataException, ServiceException
/*     */   {
/* 154 */     m_manager.getSchemaData(binder, rsetName);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 159 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71692 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.SchemaManager
 * JD-Core Version:    0.5.4
 */