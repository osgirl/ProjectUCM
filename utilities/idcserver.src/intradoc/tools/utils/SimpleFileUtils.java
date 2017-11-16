/*     */ package intradoc.tools.utils;
/*     */ 
/*     */ import intradoc.io.BufferedWriterWithLineSeparator;
/*     */ import intradoc.util.PatternFilter;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.Closeable;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileReader;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.PrintWriter;
/*     */ import java.io.Writer;
/*     */ import java.security.MessageDigest;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class SimpleFileUtils
/*     */ {
/*     */   protected static Boolean s_isWindows;
/*     */ 
/*     */   public static void close(Closeable closeable)
/*     */   {
/*  54 */     if (closeable == null)
/*     */       return;
/*     */     try
/*     */     {
/*  58 */       closeable.close();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  62 */       t.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static byte[] computeFileChecksum(File file, MessageDigest digest)
/*     */   {
/*  76 */     digest.reset();
/*  77 */     byte[] buffer = new byte[1048576];
/*  78 */     FileInputStream fis = null;
/*     */     try
/*     */     {
/*  81 */       fis = new FileInputStream(file);
/*     */ 
/*  83 */       while ((len = fis.read(buffer)) != -1)
/*     */       {
/*     */         int len;
/*  85 */         digest.update(buffer, 0, len);
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  90 */       Object localObject1 = null;
/*     */ 
/*  94 */       return localObject1; } finally { close(fis); }
/*     */ 
/*  96 */     return digest.digest();
/*     */   }
/*     */ 
/*     */   public static void copyFilteredTextFile(File source, File target, String lineSeparator, Pattern excludedLinePattern)
/*     */     throws IOException
/*     */   {
/* 112 */     FileReader fr = null;
/* 113 */     BufferedReader br = null;
/* 114 */     FileWriter fw = null;
/* 115 */     BufferedWriter bw = null;
/* 116 */     PrintWriter pw = null;
/*     */     try
/*     */     {
/* 119 */       fr = new FileReader(source);
/* 120 */       br = new BufferedReader(fr);
/* 121 */       fr = null;
/* 122 */       fw = new FileWriter(target);
/* 123 */       bw = (lineSeparator == null) ? new BufferedWriter(fw) : new BufferedWriterWithLineSeparator(fw, lineSeparator);
/*     */ 
/* 126 */       fw = null;
/* 127 */       pw = new PrintWriter(bw);
/* 128 */       bw = null;
/*     */ 
/* 131 */       while ((line = br.readLine()) != null)
/*     */       {
/*     */         String line;
/* 133 */         if ((excludedLinePattern != null) && (excludedLinePattern.matcher(line).find()))
/*     */           continue;
/* 135 */         pw.println(line);
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 141 */       close(fr);
/* 142 */       close(br);
/* 143 */       close(pw);
/* 144 */       close(bw);
/* 145 */       close(pw);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean isWindows()
/*     */   {
/* 152 */     Boolean isWindows = s_isWindows;
/* 153 */     if (isWindows == null)
/*     */     {
/* 155 */       String osName = System.getProperty("os.name");
/* 156 */       isWindows = SimpleFileUtils.s_isWindows = Boolean.valueOf(osName.toLowerCase().startsWith("win"));
/*     */     }
/* 158 */     return isWindows.booleanValue();
/*     */   }
/*     */ 
/*     */   public static void renameFile(File oldFile, File newFile) throws IOException
/*     */   {
/* 163 */     if ((newFile.exists()) && (isWindows()) && 
/* 165 */       (!newFile.delete()))
/*     */     {
/* 167 */       System.gc();
/* 168 */       newFile.delete();
/*     */     }
/*     */ 
/* 171 */     oldFile.renameTo(newFile);
/* 172 */     if (!oldFile.exists())
/*     */     {
/* 174 */       return;
/*     */     }
/* 176 */     if (isWindows())
/*     */     {
/* 178 */       for (int r = 0; (r < 10) && (oldFile.exists()); ++r)
/*     */       {
/* 180 */         System.gc();
/*     */         try
/*     */         {
/* 183 */           Thread.sleep(r++ * 10);
/*     */         }
/*     */         catch (InterruptedException ie)
/*     */         {
/*     */         }
/*     */ 
/* 189 */         newFile.delete();
/* 190 */         oldFile.renameTo(newFile);
/*     */       }
/*     */     }
/* 193 */     throw new IOException("unable to rename " + oldFile + " to " + newFile);
/*     */   }
/*     */ 
/*     */   public static List<String> scanFilesFiltered(File dir, PatternFilter filter, String prefix, List<String> filelist)
/*     */   {
/* 211 */     if (prefix == null)
/*     */     {
/* 213 */       prefix = "";
/*     */     }
/* 215 */     if (filelist == null)
/*     */     {
/* 217 */       filelist = new ArrayList();
/*     */     }
/* 219 */     File[] files = dir.listFiles();
/* 220 */     if (files != null)
/*     */     {
/* 222 */       Arrays.sort(files);
/* 223 */       for (int f = 0; f < files.length; ++f)
/*     */       {
/* 225 */         File file = files[f];
/* 226 */         boolean isDirectory = file.isDirectory();
/* 227 */         String filename = file.getName();
/* 228 */         if (isDirectory)
/*     */         {
/* 230 */           filename = filename + '/';
/*     */         }
/* 232 */         String pathname = prefix + filename;
/* 233 */         if (filter != null)
/*     */         {
/* 235 */           int match = filter.getMatch(pathname);
/* 236 */           if (match == 1) continue; if ((match != 0) && (!isDirectory)) {
/*     */             continue;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 242 */         if (isDirectory)
/*     */         {
/* 244 */           scanFilesFiltered(file, filter, pathname, filelist);
/*     */         }
/*     */         else
/*     */         {
/* 248 */           filelist.add(pathname);
/*     */         }
/*     */       }
/*     */     }
/* 252 */     return filelist;
/*     */   }
/*     */ 
/*     */   public static long scanFilesForLatestModified(File dir, PatternFilter filter, String prefix)
/*     */   {
/* 268 */     if (prefix == null)
/*     */     {
/* 270 */       prefix = "";
/*     */     }
/* 272 */     long latestModified = 0L;
/* 273 */     File[] files = dir.listFiles();
/* 274 */     for (int f = files.length - 1; f >= 0; --f)
/*     */     {
/* 276 */       File file = files[f];
/* 277 */       boolean isDirectory = file.isDirectory();
/* 278 */       String pathname = prefix;
/* 279 */       if (filter != null)
/*     */       {
/* 281 */         String filename = file.getName();
/* 282 */         if (isDirectory)
/*     */         {
/* 284 */           filename = filename + '/';
/*     */         }
/* 286 */         pathname = prefix + filename;
/* 287 */         int match = filter.getMatch(pathname);
/* 288 */         if (match == 1) continue; if ((match != 0) && (!isDirectory))
/*     */           continue;
/*     */       }
/*     */       long lastModified;
/*     */       long lastModified;
/* 295 */       if (isDirectory)
/*     */       {
/* 297 */         lastModified = scanFilesForLatestModified(file, filter, pathname);
/*     */       }
/*     */       else
/*     */       {
/* 301 */         lastModified = file.lastModified();
/*     */       }
/* 303 */       if (lastModified <= latestModified)
/*     */         continue;
/* 305 */       latestModified = lastModified;
/*     */     }
/*     */ 
/* 308 */     return latestModified;
/*     */   }
/*     */ 
/*     */   public static void writeUTF8File(OutputStream out, String[] content)
/*     */     throws IOException
/*     */   {
/* 320 */     byte[] UTF8_SIGNATURE = { -17, -69, -65 };
/* 321 */     Writer w = null;
/*     */     try
/*     */     {
/* 324 */       out.write(UTF8_SIGNATURE);
/* 325 */       w = new OutputStreamWriter(out, "UTF-8");
/* 326 */       for (String str : content)
/*     */       {
/* 328 */         w.write(str);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 333 */       close(w);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 340 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99523 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.utils.SimpleFileUtils
 * JD-Core Version:    0.5.4
 */