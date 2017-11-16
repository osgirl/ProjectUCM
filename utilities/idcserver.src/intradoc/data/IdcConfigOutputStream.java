/*     */ package intradoc.data;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class IdcConfigOutputStream extends OutputStream
/*     */ {
/*  27 */   protected IdcConfigFile m_cfgFile = null;
/*  28 */   protected boolean m_isAppend = false;
/*     */ 
/*  32 */   protected byte[] m_content = new byte[1024];
/*  33 */   protected int m_count = 0;
/*     */ 
/*     */   public IdcConfigOutputStream(String path, String feature, Workspace workspace) throws FileNotFoundException
/*     */   {
/*  37 */     this.m_cfgFile = new IdcConfigFile(path, feature, false, workspace);
/*  38 */     if (!this.m_cfgFile.isDirectory())
/*     */       return;
/*  40 */     throw new FileNotFoundException(this.m_cfgFile.m_fileID);
/*     */   }
/*     */ 
/*     */   public IdcConfigOutputStream(String path, String feature, boolean append, Workspace workspace)
/*     */     throws FileNotFoundException
/*     */   {
/*  46 */     this.m_cfgFile = new IdcConfigFile(path, feature, false, workspace);
/*  47 */     this.m_isAppend = append;
/*  48 */     if (!this.m_cfgFile.isDirectory())
/*     */       return;
/*  50 */     throw new FileNotFoundException(this.m_cfgFile.m_fileID);
/*     */   }
/*     */ 
/*     */   public IdcConfigOutputStream(File file, Workspace workspace)
/*     */     throws FileNotFoundException
/*     */   {
/*  56 */     if (file instanceof IdcConfigFile)
/*     */     {
/*  58 */       this.m_cfgFile = ((IdcConfigFile)file);
/*     */     }
/*     */     else
/*     */     {
/*  62 */       this.m_cfgFile = new IdcConfigFile(file.getAbsolutePath(), null, false, workspace);
/*     */     }
/*     */ 
/*  65 */     if (!this.m_cfgFile.isDirectory())
/*     */       return;
/*  67 */     throw new FileNotFoundException(this.m_cfgFile.m_fileID);
/*     */   }
/*     */ 
/*     */   public IdcConfigOutputStream(File file, boolean append, Workspace workspace)
/*     */     throws FileNotFoundException
/*     */   {
/*  73 */     if (file instanceof IdcConfigFile)
/*     */     {
/*  75 */       this.m_cfgFile = ((IdcConfigFile)file);
/*     */     }
/*     */     else
/*     */     {
/*  79 */       this.m_cfgFile = new IdcConfigFile(file.getAbsolutePath(), null, false, workspace);
/*     */     }
/*  81 */     this.m_isAppend = append;
/*     */ 
/*  83 */     if (!this.m_cfgFile.isDirectory())
/*     */       return;
/*  85 */     throw new FileNotFoundException(this.m_cfgFile.m_fileID);
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */     throws IOException
/*     */   {
/*  96 */     if (this.m_cfgFile == null)
/*     */       return;
/*  98 */     this.m_cfgFile.write(this.m_content, this.m_count, this.m_isAppend);
/*  99 */     this.m_isAppend = false;
/* 100 */     this.m_content = null;
/* 101 */     this.m_count = 0;
/* 102 */     this.m_cfgFile = null;
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 109 */     flush();
/*     */   }
/*     */ 
/*     */   public void write(byte[] b)
/*     */   {
/* 115 */     write(b, 0, b.length);
/*     */   }
/*     */ 
/*     */   public void write(byte[] b, int off, int len)
/*     */   {
/* 121 */     int newcount = this.m_count + len;
/* 122 */     if (newcount > this.m_content.length)
/*     */     {
/* 124 */       byte[] newbuf = new byte[Math.max(this.m_content.length << 1, newcount)];
/* 125 */       System.arraycopy(this.m_content, 0, newbuf, 0, this.m_count);
/* 126 */       this.m_content = newbuf;
/*     */     }
/* 128 */     System.arraycopy(b, off, this.m_content, this.m_count, len);
/* 129 */     this.m_count = newcount;
/*     */   }
/*     */ 
/*     */   public void write(int arg0)
/*     */   {
/* 135 */     byte[] b = Integer.toString(arg0).getBytes();
/* 136 */     write(b, 0, b.length);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 141 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99056 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdcConfigOutputStream
 * JD-Core Version:    0.5.4
 */