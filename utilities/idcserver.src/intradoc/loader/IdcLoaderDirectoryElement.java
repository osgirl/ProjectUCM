/*     */ package intradoc.loader;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.io.IdcRandomAccessByteFile;
/*     */ import intradoc.io.zip.IdcZipEnvironment;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ 
/*     */ public class IdcLoaderDirectoryElement extends IdcLoaderElement
/*     */ {
/*     */   public File m_file;
/*     */ 
/*     */   public IdcLoaderDirectoryElement(IdcLoader loader, File dir)
/*     */   {
/*  42 */     this.m_loader = loader;
/*  43 */     this.m_zipenv = this.m_loader.getZipEnvironment();
/*     */     try
/*     */     {
/*  46 */       this.m_entryPath = dir.getCanonicalPath();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  50 */       this.m_entryPath = dir.getPath();
/*     */     }
/*  52 */     if (this.m_entryPath.charAt(0) != '/')
/*     */     {
/*  63 */       this.m_entryPath = ("/" + this.m_entryPath);
/*     */     }
/*     */     try
/*     */     {
/*  67 */       URL url = new URL("file", "", -1, this.m_entryPath);
/*  68 */       setURL(url);
/*     */     }
/*     */     catch (MalformedURLException e)
/*     */     {
/*  72 */       this.m_loader.report(1, new Object[] { e });
/*     */     }
/*  74 */     this.m_file = dir;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler lookupByName(String name)
/*     */     throws IOException, IdcByteHandlerException
/*     */   {
/*  81 */     File file = new File(this.m_file, name);
/*  82 */     if (!file.exists())
/*     */     {
/*  84 */       return null;
/*     */     }
/*  86 */     if (!file.isFile())
/*     */     {
/*  88 */       throw new IOException("!syPathInvalid," + this.m_entryPath + '/' + name);
/*     */     }
/*  90 */     int flags = 1;
/*  91 */     if (this.m_zipenv.m_doPreload)
/*     */     {
/*  93 */       flags |= 1048576;
/*     */     }
/*  95 */     IdcRandomAccessByteFile bytes = new IdcRandomAccessByteFile(file, flags);
/*  96 */     bytes.init();
/*  97 */     return bytes;
/*     */   }
/*     */ 
/*     */   public URL lookupURLByName(String name)
/*     */     throws IOException
/*     */   {
/* 103 */     File file = new File(this.m_file, name);
/* 104 */     if (!file.exists())
/*     */     {
/* 106 */       return null;
/*     */     }
/* 108 */     if (!file.isFile())
/*     */     {
/* 110 */       throw new IOException("!syPathInvalid," + this.m_entryPath + '/' + name);
/*     */     }
/* 112 */     String canonicalPath = file.getCanonicalPath();
/* 113 */     if (canonicalPath == null)
/*     */     {
/* 115 */       return null;
/*     */     }
/* 117 */     if (File.separatorChar == '\\')
/*     */     {
/* 119 */       canonicalPath = canonicalPath.replace('\\', '/');
/*     */     }
/* 121 */     return new URL("file", "", -1, canonicalPath);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 128 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98477 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.loader.IdcLoaderDirectoryElement
 * JD-Core Version:    0.5.4
 */