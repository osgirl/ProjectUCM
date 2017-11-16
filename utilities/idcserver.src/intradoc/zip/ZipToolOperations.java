/*     */ package intradoc.zip;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.io.HexDumpOutputStream;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.io.IdcRandomAccessByteFile;
/*     */ import intradoc.io.zip.IdcZipEntry;
/*     */ import intradoc.io.zip.IdcZipEnvironment;
/*     */ import intradoc.io.zip.IdcZipFile;
/*     */ import intradoc.io.zip.IdcZipHandler;
/*     */ import intradoc.io.zip.IdcZipOutputStream;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ import java.util.zip.ZipException;
/*     */ 
/*     */ public class ZipToolOperations
/*     */ {
/*     */   ZipTool.Params m_params;
/*     */   IdcZipHandler m_ziphandler;
/*     */   IdcZipFile m_zipfile;
/*     */   IdcZipOutputStream m_zipstream;
/*     */   List m_paths;
/*     */   List m_excludedPatterns;
/*     */ 
/*     */   ZipToolOperations(ZipTool.Params params)
/*     */   {
/*  56 */     this.m_params = params;
/*     */   }
/*     */ 
/*     */   public void ensureOpen()
/*     */     throws Exception
/*     */   {
/*  62 */     if (this.m_params.m_doesZipfileUseOutput)
/*     */     {
/*  64 */       if (null != this.m_zipstream)
/*     */         return;
/*  66 */       OutputStream out = System.out;
/*  67 */       if (this.m_params.m_isDebug)
/*     */       {
/*  69 */         out = new HexDumpOutputStream(out);
/*     */       }
/*  71 */       this.m_zipstream = new IdcZipOutputStream(out);
/*  72 */       this.m_zipstream.init(null);
/*  73 */       this.m_ziphandler = this.m_zipstream;
/*     */     }
/*     */     else
/*     */     {
/*  78 */       if (null != this.m_zipfile)
/*     */         return;
/*  80 */       String filename = new String(this.m_params.m_zipfileName);
/*  81 */       this.m_zipfile = new IdcZipFile(filename);
/*  82 */       this.m_zipfile.init(null);
/*  83 */       this.m_ziphandler = this.m_zipfile;
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getVersionString()
/*     */   {
/*  90 */     return "ZipTool v1.0 ($Rev: 95352 $)";
/*     */   }
/*     */ 
/*     */   public void prepareFileList(List paths) throws Exception
/*     */   {
/*  95 */     if ((null == this.m_params.m_includes) && (null != this.m_params.m_fromfileName))
/*     */     {
/*  97 */       this.m_params.m_includes = new ArrayList();
/*     */       InputStream in;
/*     */       InputStream in;
/*  99 */       if (this.m_params.m_doesFromUseInput)
/*     */       {
/* 101 */         in = System.in;
/*     */       }
/*     */       else
/*     */       {
/* 105 */         in = new FileInputStream(this.m_params.m_fromfileName);
/*     */       }
/* 107 */       BufferedReader reader = new BufferedReader(new InputStreamReader(in));
/*     */       try
/*     */       {
/* 111 */         while (null != (line = reader.readLine()))
/*     */         {
/*     */           String line;
/* 113 */           if (line.length() <= 0)
/*     */             continue;
/* 115 */           this.m_params.m_includes.add(line);
/*     */         }
/*     */ 
/*     */       }
/*     */       finally
/*     */       {
/* 121 */         FileUtils.closeObject(reader);
/*     */       }
/*     */     }
/* 124 */     if (null == this.m_params.m_includes)
/*     */     {
/* 126 */       this.m_paths = new ArrayList();
/*     */     }
/*     */     else
/*     */     {
/* 130 */       this.m_paths = new ArrayList(this.m_params.m_includes);
/*     */     }
/* 132 */     if (null != paths)
/*     */     {
/* 135 */       this.m_paths.add("");
/* 136 */       this.m_paths.addAll(paths);
/*     */     }
/*     */ 
/* 139 */     if (null == this.m_params.m_excludes)
/*     */     {
/* 141 */       this.m_excludedPatterns = null;
/* 142 */       return;
/*     */     }
/* 144 */     int numExcludes = this.m_params.m_excludes.size();
/* 145 */     if (numExcludes < 1)
/*     */     {
/* 147 */       this.m_excludedPatterns = null;
/* 148 */       return;
/*     */     }
/* 150 */     this.m_excludedPatterns = new ArrayList();
/* 151 */     for (int i = 0; i < numExcludes; ++i)
/*     */     {
/* 153 */       String str = (String)this.m_params.m_excludes.get(i);
/* 154 */       str = str.replace(".", "\\.");
/* 155 */       str = str.replace("*", ".*");
/* 156 */       Pattern pattern = Pattern.compile(str);
/* 157 */       this.m_excludedPatterns.add(pattern);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isExcluded(String path)
/*     */   {
/* 163 */     if (null == this.m_excludedPatterns)
/*     */     {
/* 165 */       return false;
/*     */     }
/* 167 */     int numExcludes = this.m_excludedPatterns.size();
/* 168 */     for (int i = 0; i < numExcludes; ++i)
/*     */     {
/* 170 */       Pattern pattern = (Pattern)this.m_excludedPatterns.get(i);
/* 171 */       Matcher matcher = pattern.matcher(path);
/* 172 */       if (matcher.matches())
/*     */       {
/* 174 */         return true;
/*     */       }
/*     */     }
/* 177 */     return false;
/*     */   }
/*     */ 
/*     */   public void finish() throws Exception
/*     */   {
/* 182 */     if (null != this.m_ziphandler)
/*     */     {
/* 184 */       this.m_ziphandler.finish(-1);
/*     */     }
/* 186 */     this.m_zipfile = null;
/* 187 */     this.m_zipstream = null;
/* 188 */     this.m_ziphandler = null;
/*     */   }
/*     */ 
/*     */   public void extract()
/*     */     throws Exception
/*     */   {
/* 195 */     throw new ZipException("extract not yet implemented");
/*     */   }
/*     */ 
/*     */   public void list() throws Exception
/*     */   {
/* 200 */     throw new ZipException("list not yet implemented");
/*     */   }
/*     */ 
/*     */   public void summary() throws Exception
/*     */   {
/* 205 */     throw new ZipException("summary not yet implemented");
/*     */   }
/*     */ 
/*     */   public void test() throws Exception
/*     */   {
/* 210 */     throw new ZipException("test not yet implemented");
/*     */   }
/*     */ 
/*     */   public void compact()
/*     */     throws Exception
/*     */   {
/* 217 */     throw new ZipException("compact not yet implemented");
/*     */   }
/*     */ 
/*     */   void add(String path, boolean doUpdateOnly) throws Exception
/*     */   {
/* 222 */     if (isExcluded(path))
/*     */     {
/* 224 */       if (this.m_params.m_isVerbose)
/*     */       {
/* 226 */         System.err.println("path excluded: " + path);
/*     */       }
/* 228 */       return;
/*     */     }
/* 230 */     File file = new File(path);
/* 231 */     if (!file.canRead())
/*     */     {
/* 233 */       System.err.println("path does not exist or is not readable: " + path);
/* 234 */       return;
/*     */     }
/* 236 */     boolean isDirectory = file.isDirectory();
/* 237 */     if ((!isDirectory) && (!file.isFile()))
/*     */     {
/* 239 */       System.err.println("path is not a directory or a plain file: " + path);
/* 240 */       return;
/*     */     }
/* 242 */     IdcZipEntry entry = new IdcZipEntry();
/* 243 */     if ((!isDirectory) && (this.m_params.m_compressionLevel != 0))
/*     */     {
/* 245 */       entry.m_compressionMethod = 8;
/*     */     }
/* 247 */     entry.m_lastModified = file.lastModified();
/* 248 */     entry.m_filename = path;
/* 249 */     entry.m_comment = this.m_params.m_comment;
/* 250 */     if (isDirectory)
/*     */     {
/*     */       IdcZipEntry tmp195_193 = entry; tmp195_193.m_externalAttrsMSDOS = (byte)(tmp195_193.m_externalAttrsMSDOS | 0x10);
/*     */       IdcZipEntry tmp208_206 = entry; tmp208_206.m_externalAttrsUnix = (short)(tmp208_206.m_externalAttrsUnix & 0xFFF);
/*     */       IdcZipEntry tmp222_220 = entry; tmp222_220.m_externalAttrsUnix = (short)(tmp222_220.m_externalAttrsUnix | 0x4040);
/* 255 */       entry.m_isDirectory = true;
/*     */     }
/* 257 */     if (file.isHidden())
/*     */     {
/*     */       IdcZipEntry tmp249_247 = entry; tmp249_247.m_externalAttrsMSDOS = (byte)(tmp249_247.m_externalAttrsMSDOS | 0x2);
/*     */     }
/* 261 */     if (!file.canWrite())
/*     */     {
/*     */       IdcZipEntry tmp268_266 = entry; tmp268_266.m_externalAttrsMSDOS = (byte)(tmp268_266.m_externalAttrsMSDOS | 0x1);
/*     */       IdcZipEntry tmp280_278 = entry; tmp280_278.m_externalAttrsUnix = (short)(tmp280_278.m_externalAttrsUnix & 0x92);
/* 265 */       entry.m_isReadOnly = true;
/*     */     }
/*     */     try
/*     */     {
/* 269 */       if (!isDirectory)
/*     */       {
/* 271 */         IdcRandomAccessByteFile bfile = new IdcRandomAccessByteFile(file, 1);
/*     */ 
/* 273 */         bfile.init();
/* 274 */         entry.m_bytesUncompressed = bfile;
/*     */       }
/* 276 */       this.m_ziphandler.putEntry(entry, -1);
/*     */     }
/*     */     catch (IdcByteHandlerException e)
/*     */     {
/* 280 */       this.m_ziphandler.m_zipenv.m_trace.report(3, new Object[] { "Unable to put entry: ", path, e });
/*     */ 
/* 282 */       return;
/*     */     }
/* 284 */     if ((!isDirectory) || (this.m_params.m_noRecursion))
/*     */       return;
/* 286 */     String[] files = file.list();
/* 287 */     for (int i = 0; i < files.length; ++i)
/*     */     {
/* 289 */       add(files[i], doUpdateOnly);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void createOrReplace(boolean doUpdateOnly)
/*     */     throws Exception
/*     */   {
/* 296 */     ensureOpen();
/* 297 */     boolean isRegex = false;
/* 298 */     int numPaths = this.m_paths.size();
/* 299 */     for (int pathIndex = 0; pathIndex < numPaths; ++pathIndex)
/*     */     {
/* 301 */       String path = (String)this.m_paths.get(pathIndex);
/* 302 */       if (path.equals(""))
/*     */       {
/* 304 */         isRegex = true;
/*     */       }
/*     */       else {
/* 307 */         add(path, doUpdateOnly);
/*     */       }
/*     */     }
/* 310 */     isRegex = isRegex;
/*     */   }
/*     */ 
/*     */   public void move() throws Exception
/*     */   {
/* 315 */     throw new ZipException("move not yet implemented");
/*     */   }
/*     */ 
/*     */   public void remove() throws Exception
/*     */   {
/* 320 */     throw new ZipException("remove not yet implemented");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 327 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.zip.ZipToolOperations
 * JD-Core Version:    0.5.4
 */