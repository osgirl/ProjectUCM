/*     */ package intradoc.apputilities.idccommand;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.util.Stack;
/*     */ 
/*     */ public class FileReaderStack
/*     */ {
/*     */   protected Stack m_fileReaders;
/*     */   protected Stack m_fileNames;
/*     */   protected boolean m_trimLines;
/*     */   protected boolean m_ignoreEmptyLines;
/*     */ 
/*     */   public FileReaderStack()
/*     */   {
/*  75 */     this.m_fileReaders = new Stack();
/*  76 */     this.m_fileNames = new Stack();
/*  77 */     this.m_trimLines = false;
/*  78 */     this.m_ignoreEmptyLines = false;
/*     */   }
/*     */ 
/*     */   public void setTrimLines(boolean trim)
/*     */   {
/*  87 */     this.m_trimLines = trim;
/*     */   }
/*     */ 
/*     */   public void setIgnoreEmptyLines(boolean empty)
/*     */   {
/*  96 */     this.m_ignoreEmptyLines = empty;
/*     */   }
/*     */ 
/*     */   public void push(String filename)
/*     */     throws FileNotFoundException
/*     */   {
/* 107 */     if (filename == null)
/*     */     {
/* 109 */       throw new FileNotFoundException("!csIDCCommandFilenameNull");
/*     */     }
/*     */ 
/* 112 */     File file = new File(filename);
/* 113 */     if (!file.exists())
/*     */     {
/* 115 */       throw new FileNotFoundException(LocaleUtils.encodeMessage("syFileDoesNotExist", null, filename));
/*     */     }
/* 117 */     if (!file.canRead())
/*     */     {
/* 119 */       throw new FileNotFoundException(LocaleUtils.encodeMessage("syUnableToReadFile", null, filename));
/*     */     }
/*     */ 
/* 126 */     String absName = FileUtils.getAbsolutePath(filename);
/* 127 */     if (this.m_fileNames.contains(absName) == true) {
/* 128 */       throw new FileNotFoundException(LocaleUtils.encodeMessage("csIDCCommandFileCircularReference", null, filename));
/*     */     }
/*     */ 
/* 132 */     BufferedReader reader = null;
/*     */     try
/*     */     {
/* 136 */       reader = FileUtils.openDataReader(file);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 140 */       Report.trace(null, null, e);
/* 141 */       throw new FileNotFoundException(e.getMessage());
/*     */     }
/* 143 */     this.m_fileReaders.push(reader);
/* 144 */     this.m_fileNames.push(absName);
/*     */   }
/*     */ 
/*     */   public String readLine()
/*     */   {
/* 153 */     if (this.m_fileReaders.empty() == true)
/*     */     {
/* 155 */       return null;
/*     */     }
/*     */ 
/*     */     String line;
/*     */     try
/*     */     {
/* 163 */       line = ((BufferedReader)this.m_fileReaders.peek()).readLine();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 169 */       line = null;
/*     */     }
/*     */ 
/* 176 */     if (line == null)
/*     */     {
/* 178 */       this.m_fileReaders.pop();
/* 179 */       this.m_fileNames.pop();
/* 180 */       while ((!this.m_fileReaders.empty()) && (line == null))
/*     */       {
/*     */         try
/*     */         {
/* 185 */           line = ((BufferedReader)this.m_fileReaders.peek()).readLine();
/*     */         }
/*     */         catch (IOException e)
/*     */         {
/*     */         }
/*     */ 
/* 195 */         if (line != null)
/*     */           continue;
/* 197 */         this.m_fileReaders.pop();
/* 198 */         this.m_fileNames.pop();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 204 */     if ((this.m_trimLines == true) && (line != null))
/*     */     {
/* 206 */       line = line.trim();
/*     */     }
/*     */ 
/* 210 */     if ((this.m_ignoreEmptyLines == true) && (line != null) && 
/* 212 */       (line.length() == 0))
/*     */     {
/* 214 */       line = readLine();
/*     */     }
/*     */ 
/* 219 */     return line;
/*     */   }
/*     */ 
/*     */   public String getCurrentFileName()
/*     */   {
/* 224 */     if (this.m_fileNames.empty() == true)
/*     */     {
/* 226 */       return null;
/*     */     }
/* 228 */     return (String)this.m_fileNames.peek();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 233 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idccommand.FileReaderStack
 * JD-Core Version:    0.5.4
 */