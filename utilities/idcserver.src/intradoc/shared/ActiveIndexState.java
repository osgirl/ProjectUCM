/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ 
/*     */ public class ActiveIndexState
/*     */ {
/*  30 */   protected static DataBinder m_activeIndexData = null;
/*     */   protected static final String m_fileName = "activeindex.hda";
/*     */   public static String m_fileDir;
/*     */ 
/*     */   public static void load()
/*     */     throws ServiceException
/*     */   {
/*  43 */     DataBinder newData = new DataBinder(true);
/*     */ 
/*  45 */     serializeActiveStateIndexData(newData, false);
/*  46 */     m_activeIndexData = newData;
/*     */   }
/*     */ 
/*     */   public static void save() throws ServiceException
/*     */   {
/*  51 */     serializeActiveStateIndexData(m_activeIndexData, true);
/*     */   }
/*     */ 
/*     */   public static DataBinder getIndexStateData()
/*     */   {
/*  59 */     return m_activeIndexData;
/*     */   }
/*     */ 
/*     */   public static String getActiveProperty(String key)
/*     */   {
/*  64 */     return m_activeIndexData.getLocal(key);
/*     */   }
/*     */ 
/*     */   public static void setActiveProperty(String key, String val)
/*     */   {
/*  69 */     m_activeIndexData.putLocal(key, val);
/*     */   }
/*     */ 
/*     */   public static ResultSet getSearchCollections()
/*     */   {
/*  74 */     return m_activeIndexData.getResultSet("SearchCollections");
/*     */   }
/*     */ 
/*     */   public static void setSearchCollections(ResultSet rset)
/*     */   {
/*  79 */     m_activeIndexData.addResultSet("SearchCollections", rset);
/*     */   }
/*     */ 
/*     */   public static void serializeActiveStateIndexData(DataBinder data, boolean isWrite)
/*     */     throws ServiceException
/*     */   {
/*  89 */     String dir = m_fileDir;
/*  90 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 2, true);
/*     */     try
/*     */     {
/*  93 */       FileUtils.reserveDirectory(dir);
/*     */ 
/*  95 */       ResourceUtils.serializeDataBinder(dir, "activeindex.hda", data, isWrite, false);
/*     */     }
/*     */     finally
/*     */     {
/* 100 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 106 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82413 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ActiveIndexState
 * JD-Core Version:    0.5.4
 */