/*     */ package intradoc.io.zip;
/*     */ 
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.net.URL;
/*     */ import java.net.URLConnection;
/*     */ import java.net.URLStreamHandler;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class IdcZipURLStreamHandler extends URLStreamHandler
/*     */ {
/*     */   protected static final String FILE_PREFIX = "file:";
/*     */   protected static final String FIRST_ZIP_PATTERN = "(\\.ear|\\.jar|\\.war|\\.zip)";
/*     */   protected static Pattern s_firstZipPattern;
/*     */   public IdcZipHandler m_zip;
/*     */   public String m_outerPath;
/*     */   public String m_innerPath;
/*     */ 
/*     */   public IdcZipURLStreamHandler(IdcZipHandler zip, String outerPath, String innerPath)
/*     */   {
/*  43 */     this.m_zip = zip;
/*  44 */     this.m_outerPath = outerPath;
/*  45 */     this.m_innerPath = innerPath;
/*     */ 
/*  47 */     if (s_firstZipPattern != null)
/*     */       return;
/*  49 */     s_firstZipPattern = Pattern.compile("(\\.ear|\\.jar|\\.war|\\.zip)");
/*     */   }
/*     */ 
/*     */   public URL getURLForEntry(String entryName)
/*     */     throws IOException
/*     */   {
/*  61 */     String protocol = "idczip";
/*  62 */     StringBuilder fullpath = new StringBuilder(this.m_outerPath);
/*  63 */     fullpath.append('/');
/*  64 */     fullpath.append(entryName);
/*  65 */     Matcher matcher = s_firstZipPattern.matcher(fullpath);
/*  66 */     if (matcher.find())
/*     */     {
/*  68 */       int afterZipIndex = matcher.end();
/*  69 */       fullpath.insert(afterZipIndex, '!');
/*  70 */       fullpath.insert(0, "file:");
/*  71 */       protocol = "jar";
/*     */     }
/*  73 */     return new URL(protocol, "", -1, fullpath.toString(), this);
/*     */   }
/*     */ 
/*     */   protected URLConnection openConnection(URL u)
/*     */     throws IOException
/*     */   {
/*  80 */     StringBuffer str = new StringBuffer();
/*  81 */     String protocol = u.getProtocol();
/*  82 */     String path = u.getPath();
/*  83 */     if (protocol.equals("jar"))
/*     */     {
/*  85 */       if (!path.startsWith("file:"))
/*     */       {
/*  87 */         String msg = new StringBuilder().append("!syZipURLStreamMismatch,").append(u.toString()).append(',').append("file:").toString();
/*  88 */         this.m_zip.m_zipenv.m_trace.report(5, new Object[] { msg });
/*  89 */         throw new IOException(msg);
/*     */       }
/*  91 */       str.append(path.substring("file:".length()));
/*  92 */       Matcher matcher = s_firstZipPattern.matcher(str);
/*  93 */       if (!matcher.find())
/*     */       {
/*  95 */         String msg = new StringBuilder().append("!syZipURLStreamMismatch,").append(u.toString()).append(',').append("(\\.ear|\\.jar|\\.war|\\.zip)").toString();
/*  96 */         this.m_zip.m_zipenv.m_trace.report(5, new Object[] { msg });
/*  97 */         throw new IOException(msg);
/*     */       }
/*  99 */       int afterZipIndex = matcher.end();
/* 100 */       if (str.charAt(afterZipIndex) != '!')
/*     */       {
/* 102 */         String msg = new StringBuilder().append("!syZipURLStreamMismatch,").append(u.toString()).append(',').append("(\\.ear|\\.jar|\\.war|\\.zip)").append('!').toString();
/* 103 */         this.m_zip.m_zipenv.m_trace.report(5, new Object[] { msg });
/* 104 */         throw new IOException(msg);
/*     */       }
/* 106 */       str.deleteCharAt(afterZipIndex);
/* 107 */       path = str.toString();
/* 108 */       str.setLength(0);
/*     */     }
/* 110 */     else if (!protocol.equals("idczip"))
/*     */     {
/* 112 */       this.m_zip.m_zipenv.m_trace.report(5, new Object[] { "unknown protocol: ", u });
/* 113 */       throw new IOException(new StringBuilder().append("unknown protocol: ").append(u).toString());
/*     */     }
/* 115 */     if (null != this.m_outerPath)
/*     */     {
/* 117 */       str.append(this.m_outerPath);
/*     */     }
/* 119 */     int prefixLength = str.length();
/* 120 */     if (null != this.m_innerPath)
/*     */     {
/* 122 */       str.append('/');
/* 123 */       ++prefixLength;
/* 124 */       str.append(this.m_innerPath);
/* 125 */       str.append('/');
/*     */     }
/* 127 */     String prefix = str.toString();
/* 128 */     if (!path.startsWith(prefix))
/*     */     {
/* 130 */       String msg = new StringBuilder().append("!syZipURLStreamMismatch,").append(u.toString()).append(',').append(prefix).toString();
/* 131 */       this.m_zip.m_zipenv.m_trace.report(5, new Object[] { msg });
/* 132 */       throw new IOException(msg);
/*     */     }
/* 134 */     if ((path.length() > prefixLength) && (path.charAt(prefixLength) == '/'))
/*     */     {
/* 137 */       ++prefixLength;
/*     */     }
/* 139 */     String entryPath = path.substring(prefixLength);
/* 140 */     Exception e = null;
/* 141 */     IdcZipEntry entry = null;
/*     */     try
/*     */     {
/* 144 */       entry = this.m_zip.getEntry(entryPath, -1);
/*     */     }
/*     */     catch (IdcZipException z)
/*     */     {
/* 148 */       e = z;
/*     */     }
/*     */     catch (IdcByteHandlerException h)
/*     */     {
/* 152 */       e = h;
/*     */     }
/* 154 */     if (null == entry)
/*     */     {
/* 156 */       if (null != this.m_innerPath)
/*     */       {
/* 158 */         FileNotFoundException fnfe = new FileNotFoundException(path);
/* 159 */         if (null != e)
/*     */         {
/* 161 */           fnfe.initCause(e);
/*     */         }
/* 163 */         this.m_zip.m_zipenv.m_trace.report(5, new Object[] { fnfe });
/* 164 */         throw fnfe;
/*     */       }
/*     */     }
/* 167 */     else if (entry.m_isDirectory)
/*     */     {
/* 169 */       IdcMessage msg = new IdcMessage("syZipEntryExtractError", new Object[] { entry.m_filename });
/* 170 */       msg.m_prior = new IdcMessage("syZipEntryExtractDirectory", new Object[0]);
/* 171 */       IOException io = new IOException();
/* 172 */       io.initCause(new IdcZipException(msg));
/* 173 */       this.m_zip.m_zipenv.m_trace.report(5, new Object[] { msg });
/* 174 */       throw io;
/*     */     }
/*     */ 
/* 178 */     return new IdcZipEntryURLConnection(u, this.m_zip, entry);
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 184 */     this.m_zip = null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 190 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98716 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipURLStreamHandler
 * JD-Core Version:    0.5.4
 */