/*     */ package intradoc.conversion;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class StandardKeyLoader
/*     */   implements KeyLoaderInterface
/*     */ {
/*     */   protected String m_dir;
/*     */   protected boolean m_setExists;
/*     */ 
/*     */   public StandardKeyLoader()
/*     */   {
/*  28 */     this.m_dir = null;
/*  29 */     this.m_setExists = false;
/*     */   }
/*     */ 
/*     */   public void init(Map map) throws ServiceException {
/*  33 */     this.m_dir = ((String)map.get("PrivateDirectory"));
/*  34 */     if (map.get("SkipCreatePrivateDirectoryForSecurity") != null)
/*     */       return;
/*  36 */     FileUtils.checkOrCreateDirectory(this.m_dir, 2, 3);
/*     */   }
/*     */ 
/*     */   public boolean readKeys(DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  43 */     return ResourceUtils.serializeDataBinder(this.m_dir, "private.hda", binder, false, false);
/*     */   }
/*     */ 
/*     */   public boolean canUpdateKeys()
/*     */   {
/*  49 */     return FileUtils.checkPathExists(this.m_dir);
/*     */   }
/*     */ 
/*     */   public boolean writeKeys(DataBinder binder) throws ServiceException
/*     */   {
/*  54 */     return ResourceUtils.serializeDataBinder(this.m_dir, "private.hda", binder, true, false);
/*     */   }
/*     */ 
/*     */   public void load(SecurityObjects sObjects)
/*     */     throws DataException, ServiceException
/*     */   {
/*  60 */     DataBinder binder = new DataBinder();
/*  61 */     boolean defExists = readKeys(binder);
/*  62 */     DataResultSet drset = (DataResultSet)binder.getResultSet("MasterKeys");
/*  63 */     if (drset == null)
/*     */     {
/*  65 */       if ((defExists) || (!this.m_setExists))
/*     */       {
/*     */         return;
/*     */       }
/*     */ 
/*  70 */       Throwable t = new ServiceException("Debug exception for master key tracking.");
/*  71 */       Report.error("system", t, "csMasterKeysLoadError", new Object[] { "MasterKeys", this.m_dir });
/*     */     }
/*     */     else
/*     */     {
/*  76 */       this.m_setExists = true;
/*  77 */       sObjects.loadKeys(drset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void storeKey(Map keyParams, SecurityObjects sObjects)
/*     */     throws DataException, ServiceException
/*     */   {
/*  84 */     FileUtils.reserveDirectory(this.m_dir);
/*     */     try
/*     */     {
/*  87 */       DataBinder binder = new DataBinder();
/*  88 */       boolean exists = ResourceUtils.serializeDataBinder(this.m_dir, "private.hda", binder, false, false);
/*     */ 
/*  90 */       if ((!exists) && (this.m_setExists))
/*     */       {
/*  95 */         String name = FileUtils.fileSlashes(this.m_dir + "/private.hda");
/*  96 */         throw new ServiceException(null, "syUnexpectedFileNotFound", new Object[] { name });
/*     */       }
/*     */ 
/*  99 */       DataResultSet drset = sObjects.updateKeySet(keyParams, binder);
/* 100 */       writeKeys(binder);
/* 101 */       sObjects.loadKeys(drset);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 105 */       throw new ServiceException(e, "csMasterKeysSaveError", new Object[] { "MasterKeys" });
/*     */     }
/*     */     finally
/*     */     {
/* 109 */       FileUtils.releaseDirectory(this.m_dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 115 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.conversion.StandardKeyLoader
 * JD-Core Version:    0.5.4
 */