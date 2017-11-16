/*     */ package intradoc.filestore;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.filter.PurgerInterface;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.RandomAccessFile;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class FilePurger
/*     */   implements PurgerInterface
/*     */ {
/*     */   protected BaseFileStore m_fileStore;
/*  41 */   protected byte[] m_zerosBuffer = null;
/*  42 */   protected byte[] m_onesBuffer = null;
/*  43 */   protected int m_passes = 0;
/*     */ 
/*  45 */   protected ExecutionContext m_context = null;
/*     */ 
/*     */   public FilePurger(FileStoreProvider fs)
/*     */   {
/*  49 */     this.m_fileStore = ((BaseFileStore)fs);
/*     */   }
/*     */ 
/*     */   public void init(ExecutionContext cxt)
/*     */   {
/*  54 */     if (cxt == null)
/*     */     {
/*  56 */       cxt = new ExecutionContextAdaptor();
/*     */     }
/*  58 */     this.m_context = cxt;
/*     */   }
/*     */ 
/*     */   public boolean doPreDelete(File file, Object descriptor, Map args)
/*     */     throws ServiceException
/*     */   {
/*  64 */     if (args == null)
/*     */     {
/*  66 */       args = new HashMap();
/*     */     }
/*  68 */     boolean isPurged = false;
/*  69 */     RandomAccessFile raf = null;
/*     */     try
/*     */     {
/*  72 */       Object[] params = { file, descriptor, args };
/*  73 */       this.m_context.setCachedObject("doPreDelete:parameters", params);
/*  74 */       if (PluginFilters.filter("doPreDelete", null, null, this.m_context) != 0)
/*     */       {
/*  77 */         int i = 1;
/*     */         return i;
/*     */       }
/*  79 */       boolean isPurge = this.m_fileStore.getConfigBoolean("purge", args, false, false);
/*     */ 
/*  81 */       if (isPurge)
/*     */       {
/*  83 */         raf = new RandomAccessFile(file, "rwd");
/*  84 */         long length = raf.length();
/*  85 */         for (int i = 0; i < this.m_passes; ++i)
/*     */         {
/*  87 */           scrubWithBuffer(raf, length, this.m_zerosBuffer);
/*  88 */           scrubWithBuffer(raf, length, this.m_onesBuffer);
/*     */         }
/*  90 */         isPurged = true;
/*     */       }
/*     */     }
/*     */     catch (FileNotFoundException e)
/*     */     {
/*  95 */       Report.trace("filestore", "purge: failure for " + file, e);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 103 */       FileUtils.closeObject(raf);
/*     */     }
/* 105 */     return isPurged;
/*     */   }
/*     */ 
/*     */   protected void scrubWithBuffer(RandomAccessFile raf, long length, byte[] sBuffer)
/*     */     throws IOException
/*     */   {
/* 111 */     raf.seek(0L);
/* 112 */     int buffSize = sBuffer.length;
/* 113 */     for (long j = 0L; j < length; j += buffSize)
/*     */     {
/* 115 */       if (length >= j + buffSize)
/*     */       {
/* 117 */         raf.write(sBuffer);
/*     */       }
/*     */       else
/*     */       {
/* 121 */         long rem = length - j;
/* 122 */         raf.write(sBuffer, 0, (int)rem);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 129 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97564 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FilePurger
 * JD-Core Version:    0.5.4
 */