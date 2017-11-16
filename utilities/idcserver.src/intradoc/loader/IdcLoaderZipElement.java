/*     */ package intradoc.loader;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.io.zip.IdcZipEntry;
/*     */ import intradoc.io.zip.IdcZipException;
/*     */ import intradoc.io.zip.IdcZipFile;
/*     */ import intradoc.io.zip.IdcZipHandler;
/*     */ import intradoc.io.zip.IdcZipURLStreamHandler;
/*     */ import intradoc.io.zip.IdcZipUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcMessageUtils;
/*     */ import java.io.IOException;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ 
/*     */ public class IdcLoaderZipElement extends IdcLoaderElement
/*     */ {
/*     */   public IdcZipHandler m_zip;
/*     */   public String m_innerPath;
/*     */   public IdcZipURLStreamHandler m_streamHandler;
/*     */ 
/*     */   public IdcLoaderZipElement(IdcLoader loader, String entryPath, IdcZipFile zip, String innerPath)
/*     */   {
/*  51 */     this.m_loader = loader;
/*  52 */     this.m_zipenv = loader.getZipEnvironment();
/*  53 */     this.m_zip = zip;
/*  54 */     this.m_innerPath = (((null == innerPath) || (innerPath.equals(""))) ? null : innerPath);
/*  55 */     String urlPrefix = (this.m_innerPath == null) ? "file" : "idczip";
/*     */ 
/*  57 */     this.m_entryPath = entryPath;
/*  58 */     if ((urlPrefix.equals("file")) && (this.m_entryPath.charAt(0) != '/'))
/*     */     {
/*  60 */       if (this.m_entryPath.charAt(1) == ':')
/*     */       {
/*  64 */         this.m_entryPath = this.m_entryPath.replace('\\', '/');
/*     */       }
/*     */ 
/*  75 */       this.m_entryPath = ("/" + this.m_entryPath);
/*     */     }
/*     */ 
/*  78 */     this.m_streamHandler = new IdcZipURLStreamHandler(zip, this.m_entryPath, this.m_innerPath);
/*  79 */     this.m_streamHandler.m_outerPath = this.m_entryPath;
/*  80 */     this.m_streamHandler.m_innerPath = this.m_innerPath;
/*     */     try
/*     */     {
/*  84 */       IdcZipURLStreamHandler handler = (urlPrefix.equals("idczip")) ? this.m_streamHandler : null;
/*  85 */       URL url = new URL(urlPrefix, "", -1, this.m_streamHandler.m_outerPath, handler);
/*  86 */       setURL(url);
/*     */     }
/*     */     catch (MalformedURLException e)
/*     */     {
/*  90 */       this.m_loader.report(1, new Object[] { e });
/*     */     }
/*     */   }
/*     */ 
/*     */   public IdcByteHandler lookupByName(String name)
/*     */     throws IdcZipException, IdcByteHandlerException
/*     */   {
/*  98 */     if (name.charAt(0) == '/')
/*     */     {
/* 100 */       name = name.substring(1);
/*     */     }
/* 102 */     if (null != this.m_innerPath)
/*     */     {
/* 104 */       name = this.m_innerPath + '/' + name;
/*     */     }
/* 106 */     IdcZipEntry entry = this.m_zip.getEntry(name, -1);
/* 107 */     if (null == entry)
/*     */     {
/* 109 */       return null;
/*     */     }
/* 111 */     if (entry.m_isDirectory)
/*     */     {
/* 113 */       IdcMessage msg = IdcMessageUtils.lc("syZipEntryExtractError", new Object[] { this.m_entryPath + '/' + name });
/* 114 */       msg.m_prior = IdcMessageUtils.lc("syZipEntryExtractDirectory", new Object[0]);
/* 115 */       throw new IdcZipException(msg);
/*     */     }
/* 117 */     IdcByteHandler handler = entry.m_bytesUncompressed;
/* 118 */     if (null == handler)
/*     */     {
/* 120 */       IdcZipUtils.extractEntry(this.m_zip.m_zipenv, entry, null);
/* 121 */       handler = entry.m_bytesUncompressed;
/*     */     }
/* 123 */     handler.setPosition(0L);
/* 124 */     return handler;
/*     */   }
/*     */ 
/*     */   public URL lookupURLByName(String name)
/*     */     throws IdcByteHandlerException, IdcZipException, IOException
/*     */   {
/* 130 */     if (name.charAt(0) == '/')
/*     */     {
/* 132 */       name = name.substring(1);
/*     */     }
/* 134 */     if (null != this.m_innerPath)
/*     */     {
/* 136 */       name = this.m_innerPath + '/' + name;
/*     */     }
/* 138 */     IdcZipEntry entry = this.m_zip.getEntry(name, -1);
/* 139 */     if (null == entry)
/*     */     {
/* 141 */       return null;
/*     */     }
/* 143 */     if (entry.m_isDirectory)
/*     */     {
/* 145 */       IdcMessage msg = IdcMessageUtils.lc("syZipEntryExtractError", new Object[] { this.m_entryPath + '/' + name });
/* 146 */       msg.m_prior = IdcMessageUtils.lc("syZipEntryExtractDirectory", new Object[0]);
/* 147 */       throw new IdcZipException(msg);
/*     */     }
/* 149 */     IdcZipURLStreamHandler streamHandler = this.m_streamHandler;
/* 150 */     if (streamHandler == null)
/*     */     {
/* 152 */       streamHandler = this.m_streamHandler = new IdcZipURLStreamHandler(this.m_zip, this.m_entryPath, this.m_innerPath);
/*     */     }
/* 154 */     return streamHandler.getURLForEntry(name);
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 161 */     this.m_zip = null;
/* 162 */     if (this.m_streamHandler == null)
/*     */       return;
/* 164 */     this.m_streamHandler.clear();
/* 165 */     this.m_streamHandler = null;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 172 */     return this.m_entryPath + '/' + this.m_innerPath;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 178 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98716 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.loader.IdcLoaderZipElement
 * JD-Core Version:    0.5.4
 */