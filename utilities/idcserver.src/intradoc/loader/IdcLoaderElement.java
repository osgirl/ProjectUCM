/*     */ package intradoc.loader;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.zip.IdcZipEnvironment;
/*     */ import java.net.URL;
/*     */ import java.security.CodeSource;
/*     */ import java.security.ProtectionDomain;
/*     */ import java.security.cert.Certificate;
/*     */ 
/*     */ public abstract class IdcLoaderElement
/*     */ {
/*     */   public IdcLoader m_loader;
/*     */   public IdcZipEnvironment m_zipenv;
/*     */   public String m_entryPath;
/*     */   public int m_loadOrder;
/*     */   public URL m_URL;
/*     */   public CodeSource m_codeSource;
/*     */   public ProtectionDomain m_protectionDomain;
/*     */   public boolean m_isExcludedForResources;
/*     */ 
/*     */   protected void setURL(URL url)
/*     */   {
/*  68 */     this.m_URL = url;
/*  69 */     this.m_codeSource = new CodeSource(url, (Certificate[])null);
/*     */ 
/*  76 */     this.m_protectionDomain = new ProtectionDomain(this.m_codeSource, null, null, null);
/*     */   }
/*     */ 
/*     */   public abstract IdcByteHandler lookupByName(String paramString)
/*     */     throws Exception;
/*     */ 
/*     */   public abstract URL lookupURLByName(String paramString)
/*     */     throws Exception;
/*     */ 
/*     */   public void clear()
/*     */   {
/* 102 */     this.m_loader = null;
/* 103 */     this.m_zipenv = null;
/*     */   }
/*     */ 
/*     */   public String getName()
/*     */   {
/* 108 */     return toString();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 114 */     return this.m_entryPath;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 120 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 77118 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.loader.IdcLoaderElement
 * JD-Core Version:    0.5.4
 */