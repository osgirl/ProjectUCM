/*     */ package intradoc.io.zip;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.net.URL;
/*     */ import java.net.URLConnection;
/*     */ import java.util.Date;
/*     */ 
/*     */ public class IdcZipEntryURLConnection extends URLConnection
/*     */ {
/*     */   URL m_url;
/*     */   IdcZipHandler m_zip;
/*     */   IdcZipEntry m_entry;
/*     */ 
/*     */   protected IdcZipEntryURLConnection(URL Url, IdcZipHandler zip, IdcZipEntry entry)
/*     */   {
/*  35 */     super(Url);
/*  36 */     this.m_url = Url;
/*  37 */     this.m_zip = zip;
/*  38 */     this.m_entry = entry;
/*     */   }
/*     */ 
/*     */   public void connect()
/*     */     throws IOException
/*     */   {
/*  46 */     this.connected = true;
/*     */   }
/*     */ 
/*     */   public String getHeaderField(String name)
/*     */   {
/*  52 */     if (name.equals("content-length"))
/*     */     {
/*  54 */       return String.valueOf(this.m_entry.m_sizeUncompressed);
/*     */     }
/*  56 */     if (name.equals("last-modified"))
/*     */     {
/*  58 */       return String.valueOf(this.m_entry.m_lastModified);
/*     */     }
/*  60 */     if (name.equals("date"))
/*     */     {
/*  62 */       return String.valueOf(new Date().getTime());
/*     */     }
/*  64 */     return null;
/*     */   }
/*     */ 
/*     */   public InputStream getInputStream()
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/*  72 */       return IdcZipUtils.extractEntryAsInputStream(this.m_zip.m_zipenv, this.m_entry);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  76 */       IOException io = new IOException();
/*  77 */       io.initCause(e);
/*  78 */       throw io;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setDoOutput(boolean dooutput)
/*     */   {
/*  85 */     if (!dooutput)
/*     */       return;
/*  87 */     throw new IllegalStateException("protocol does not support output");
/*     */   }
/*     */ 
/*     */   public void setAllowUserInteraction(boolean allowuserinteraction)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 101 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71102 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipEntryURLConnection
 * JD-Core Version:    0.5.4
 */