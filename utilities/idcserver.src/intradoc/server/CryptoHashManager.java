/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.util.Date;
/*     */ 
/*     */ public class CryptoHashManager
/*     */ {
/*     */   public String m_directory;
/*     */   public String m_hashesFileName;
/*     */   public String m_hashesFilePath;
/*     */   public long m_hashesFileTimestamp;
/*     */   public String m_newHash;
/*     */   public String m_currentHash;
/*     */   public String m_priorHash;
/*     */   public long m_activateNewHashTimestamp;
/*     */   public long m_rotateHashesTimestamp;
/*     */ 
/*     */   public CryptoHashManager()
/*     */   {
/*  35 */     this.m_hashesFileName = "hashes.hda";
/*     */ 
/*  37 */     this.m_hashesFileTimestamp = -1L;
/*     */ 
/*  42 */     this.m_activateNewHashTimestamp = -1L;
/*  43 */     this.m_rotateHashesTimestamp = -1L;
/*     */   }
/*     */ 
/*     */   public void init() throws ServiceException {
/*  47 */     String dataDir = SharedObjects.getEnvironmentValue("DataDir");
/*  48 */     if (dataDir == null)
/*     */     {
/*  50 */       dataDir = SharedObjects.getEnvironmentValue("ServerDataDir");
/*     */     }
/*  52 */     dataDir = FileUtils.directorySlashes(dataDir);
/*     */ 
/*  54 */     this.m_directory = (dataDir + "crypto");
/*  55 */     this.m_hashesFilePath = (this.m_directory + "/" + this.m_hashesFileName);
/*  56 */     FileUtils.checkOrCreateDirectoryEx(this.m_directory, 2, true);
/*     */   }
/*     */ 
/*     */   public void checkUpdateHashes() throws ServiceException
/*     */   {
/*  61 */     File f = FileUtilsCfgBuilder.getCfgFile(this.m_hashesFilePath, "Crypto", false);
/*  62 */     if (!f.exists())
/*     */     {
/*  64 */       rotateCryptoHashes(false);
/*     */     }
/*     */     else
/*     */     {
/*  68 */       if (f.lastModified() != this.m_hashesFileTimestamp)
/*     */       {
/*  70 */         loadCryptoHashes(true);
/*     */       }
/*     */ 
/*  73 */       Date now = new Date();
/*  74 */       long nowTimestamp = now.getTime();
/*  75 */       if (this.m_rotateHashesTimestamp <= nowTimestamp)
/*     */       {
/*  77 */         rotateCryptoHashes(true);
/*     */       }
/*     */     }
/*     */ 
/*  81 */     cacheCryptoHashes();
/*     */   }
/*     */ 
/*     */   public void loadCryptoHashes(boolean mustExist) throws ServiceException
/*     */   {
/*  86 */     File f = FileUtilsCfgBuilder.getCfgFile(this.m_hashesFilePath, "Crypto", false);
/*  87 */     if (f.exists())
/*     */     {
/*  89 */       this.m_hashesFileTimestamp = f.lastModified();
/*     */     }
/*     */ 
/*  92 */     DataBinder binder = new DataBinder();
/*  93 */     ResourceUtils.serializeDataBinder(this.m_directory, this.m_hashesFileName, binder, false, mustExist);
/*  94 */     this.m_newHash = binder.getLocal("NewHashPassword");
/*  95 */     this.m_currentHash = binder.getLocal("CurrentHashPassword");
/*  96 */     this.m_priorHash = binder.getLocal("PriorHashPassword");
/*  97 */     this.m_activateNewHashTimestamp = DataBinderUtils.getLocalLong(binder, "ActivateTempHashPasswordTimestamp", -1L);
/*     */ 
/*  99 */     this.m_rotateHashesTimestamp = DataBinderUtils.getLocalLong(binder, "RotateHashPasswordsTimestamp", -1L);
/*     */   }
/*     */ 
/*     */   public void rotateCryptoHashes(boolean mustExist)
/*     */     throws ServiceException
/*     */   {
/* 105 */     FileUtils.reserveDirectory(this.m_directory);
/*     */     try
/*     */     {
/* 108 */       loadCryptoHashes(mustExist);
/*     */ 
/* 110 */       Date now = new Date();
/* 111 */       long nowTimestamp = now.getTime();
/* 112 */       if (this.m_rotateHashesTimestamp <= nowTimestamp)
/*     */       {
/* 114 */         this.m_priorHash = this.m_currentHash;
/* 115 */         this.m_currentHash = this.m_newHash;
/* 116 */         this.m_newHash = StringUtils.createGUIDEx(32, 0, "", true);
/* 117 */         this.m_rotateHashesTimestamp = (nowTimestamp + 604800000L);
/* 118 */         this.m_activateNewHashTimestamp = (nowTimestamp + 3600000L);
/*     */ 
/* 120 */         DataBinder binder = new DataBinder();
/* 121 */         binder.putLocal("NewHashPassword", this.m_newHash);
/* 122 */         if (this.m_currentHash != null)
/*     */         {
/* 124 */           binder.putLocal("CurrentHashPassword", this.m_currentHash);
/*     */         }
/* 126 */         if (this.m_priorHash != null)
/*     */         {
/* 128 */           binder.putLocal("PriorHashPassword", this.m_priorHash);
/*     */         }
/* 130 */         binder.putLocal("ActivateTempHashPasswordTimestamp", "" + this.m_activateNewHashTimestamp);
/* 131 */         binder.putLocal("RotateHashPasswordsTimestamp", "" + this.m_rotateHashesTimestamp);
/* 132 */         ResourceUtils.serializeDataBinder(this.m_directory, this.m_hashesFileName, binder, true, true);
/*     */ 
/* 134 */         File f = FileUtilsCfgBuilder.getCfgFile(this.m_hashesFilePath, "Crypto", false);
/* 135 */         this.m_hashesFileTimestamp = f.lastModified();
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 140 */       FileUtils.releaseDirectory(this.m_directory);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cacheCryptoHashes()
/*     */   {
/* 146 */     if (this.m_currentHash == null)
/*     */     {
/* 148 */       SharedObjects.putEnvironmentValue("LongTermHashPassword", this.m_newHash);
/* 149 */       SharedObjects.putEnvironmentValue("PriorLongTermHashPassword", null);
/* 150 */       SharedObjects.putEnvironmentValue("NewLongTermHashPassword", null);
/*     */     }
/*     */     else
/*     */     {
/* 154 */       Date now = new Date();
/* 155 */       long nowTimestamp = now.getTime();
/* 156 */       if (this.m_activateNewHashTimestamp <= nowTimestamp)
/*     */       {
/* 158 */         SharedObjects.putEnvironmentValue("PriorLongTermHashPassword", this.m_currentHash);
/* 159 */         SharedObjects.putEnvironmentValue("LongTermHashPassword", this.m_newHash);
/* 160 */         SharedObjects.putEnvironmentValue("NewLongTermHashPassword", null);
/*     */       }
/*     */       else
/*     */       {
/* 164 */         SharedObjects.putEnvironmentValue("PriorLongTermHashPassword", this.m_priorHash);
/* 165 */         SharedObjects.putEnvironmentValue("LongTermHashPassword", this.m_currentHash);
/* 166 */         SharedObjects.putEnvironmentValue("NewLongTermHashPassword", this.m_newHash);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 173 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98726 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.CryptoHashManager
 * JD-Core Version:    0.5.4
 */