/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import intradoc.util.CollectionUtils;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import javax.tools.Diagnostic;
/*     */ import javax.tools.Diagnostic.Kind;
/*     */ import javax.tools.DiagnosticListener;
/*     */ import javax.tools.JavaCompiler;
/*     */ import javax.tools.JavaCompiler.CompilationTask;
/*     */ import javax.tools.JavaFileObject;
/*     */ import javax.tools.StandardJavaFileManager;
/*     */ import javax.tools.ToolProvider;
/*     */ 
/*     */ public class JavaCompileManager
/*     */   implements DiagnosticListener<JavaFileObject>
/*     */ {
/*     */   public static final String JAVAFILE_FILTER = "+**.java";
/*     */   public static final String CLASSFILE_FILTER = "+**.class";
/*     */   public static JavaCompiler s_javaCompiler;
/*     */   public static String s_pathSeparator;
/*     */   public GenericTracingCallback m_trace;
/*     */   public boolean m_areExtraChecksEnabled;
/*     */   public boolean m_isVerboseCompile;
/*     */   public boolean m_isShowCommand;
/*     */   public List<Diagnostic<? extends JavaFileObject>> m_diagnostics;
/*     */   public String m_description;
/*     */   public List<String> m_bootclasspathPrepend;
/*     */   public List<String> m_classpath;
/*     */   public ClassFileEditor m_editor;
/*     */   protected String m_sourceFilenamePrefix;
/*     */ 
/*     */   public JavaCompileManager()
/*     */   {
/*  63 */     if (s_javaCompiler != null)
/*     */       return;
/*  65 */     s_javaCompiler = ToolProvider.getSystemJavaCompiler();
/*  66 */     if (s_javaCompiler == null)
/*     */     {
/*  68 */       throw new RuntimeException("unable to get system Java compiler");
/*     */     }
/*  70 */     s_pathSeparator = System.getProperty("path.separator");
/*     */   }
/*     */ 
/*     */   public void compileOutdated(File sourceDir, File targetDir, List<String> javaFilenames)
/*     */     throws IOException
/*     */   {
/*  86 */     if (s_javaCompiler == null)
/*     */     {
/*  88 */       s_javaCompiler = ToolProvider.getSystemJavaCompiler();
/*  89 */       s_pathSeparator = System.getProperty("path.separator");
/*     */     }
/*     */ 
/*  92 */     long timeStart = System.currentTimeMillis();
/*  93 */     GenericTracingCallback trace = this.m_trace;
/*  94 */     boolean areExtraChecksEnabled = this.m_areExtraChecksEnabled;
/*  95 */     StandardJavaFileManager fileManager = s_javaCompiler.getStandardFileManager(this, null, null);
/*  96 */     targetDir.mkdir();
/*  97 */     this.m_sourceFilenamePrefix = new StringBuilder().append(sourceDir.getPath()).append('/').toString();
/*     */ 
/* 100 */     List filenames = new ArrayList(javaFilenames);
/* 101 */     int numJavaFilesSupplied = filenames.size();
/* 102 */     List javaFiles = new ArrayList(numJavaFilesSupplied);
/* 103 */     List timestamps = new ArrayList(numJavaFilesSupplied);
/* 104 */     StringBuilder str = new StringBuilder(64);
/* 105 */     int numJavaFiles = numJavaFilesSupplied;
/* 106 */     for (int n = 0; n < numJavaFiles; )
/*     */     {
/* 108 */       String javaFilename = (String)filenames.get(n);
/* 109 */       if ((areExtraChecksEnabled) && (!javaFilename.endsWith(".java")))
/*     */       {
/* 111 */         throw new IOException(new StringBuilder().append("filename does not end in .java: ").append(javaFilename).toString());
/*     */       }
/* 113 */       str.setLength(0);
/* 114 */       str.append(javaFilename);
/* 115 */       File javaFile = new File(sourceDir, str.toString());
/* 116 */       str.setLength(str.length() - 4);
/* 117 */       str.append("class");
/* 118 */       File classFile = new File(targetDir, str.toString());
/* 119 */       long javaTimestamp = javaFile.lastModified();
/* 120 */       long classTimestamp = classFile.lastModified();
/* 121 */       if ((classTimestamp == 0L) || (javaTimestamp > classTimestamp))
/*     */       {
/* 123 */         javaFiles.add(javaFile);
/* 124 */         timestamps.add(Long.valueOf(javaTimestamp));
/* 125 */         ++n;
/*     */       }
/*     */       else
/*     */       {
/* 129 */         filenames.remove(n);
/* 130 */         --numJavaFiles;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 135 */     List options = CollectionUtils.appendToList(null, new Object[] { "-g" });
/* 136 */     if (this.m_isVerboseCompile)
/*     */     {
/* 138 */       options.add("-verbose");
/*     */     }
/* 140 */     CollectionUtils.appendToList(options, new Object[] { "-sourcepath", sourceDir.getPath(), "-d", targetDir.getPath() });
/* 141 */     List classpath = this.m_classpath;
/* 142 */     int numClasspathElements = (classpath == null) ? 0 : classpath.size();
/* 143 */     if (numClasspathElements > 0)
/*     */     {
/* 145 */       str.setLength(0);
/* 146 */       String pathSeparator = s_pathSeparator;
/* 147 */       for (int p = 0; p < numClasspathElements; ++p)
/*     */       {
/* 149 */         if (p > 0)
/*     */         {
/* 151 */           str.append(pathSeparator);
/*     */         }
/* 153 */         str.append((String)classpath.get(p));
/*     */       }
/* 155 */       CollectionUtils.appendToList(options, new Object[] { "-classpath", str.toString() });
/*     */     }
/* 157 */     List bootclasspath = this.m_bootclasspathPrepend;
/* 158 */     int numBootclasspathElements = (bootclasspath == null) ? 0 : bootclasspath.size();
/* 159 */     if (numBootclasspathElements > 0)
/*     */     {
/* 161 */       str.setLength(0);
/* 162 */       str.append("-Xbootclasspath/p:");
/* 163 */       String pathSeparator = s_pathSeparator;
/* 164 */       for (int p = 0; p < numBootclasspathElements; ++p)
/*     */       {
/* 166 */         if (p > 0)
/*     */         {
/* 168 */           str.append(pathSeparator);
/*     */         }
/* 170 */         str.append((String)bootclasspath.get(p));
/*     */       }
/* 172 */       CollectionUtils.appendToList(options, new Object[] { str.toString() });
/*     */     }
/* 174 */     if ((this.m_isShowCommand) && (trace != null))
/*     */     {
/* 176 */       str.setLength(0);
/* 177 */       str.append("javac");
/* 178 */       int numOptions = options.size();
/* 179 */       for (int o = 0; o < numOptions; ++o)
/*     */       {
/* 181 */         str.append(' ');
/* 182 */         str.append((String)options.get(o));
/*     */       }
/* 184 */       trace.report(7, new Object[] { str.toString() });
/*     */     }
/* 186 */     Iterable javaFileObjects = fileManager.getJavaFileObjectsFromFiles(javaFiles);
/*     */ 
/* 189 */     long timeBeforeCompile = System.currentTimeMillis();
/* 190 */     if (trace != null)
/*     */     {
/* 192 */       trace.report(6, new Object[] { "compiling (", Integer.valueOf(numJavaFiles), " of ", Integer.valueOf(numJavaFilesSupplied), " files) in ", this.m_description, " ..." });
/*     */     }
/*     */ 
/* 195 */     if (numJavaFiles <= 0)
/*     */     {
/* 197 */       return;
/*     */     }
/* 199 */     JavaCompiler.CompilationTask task = s_javaCompiler.getTask(null, fileManager, this, options, null, javaFileObjects);
/* 200 */     boolean wasSuccessful = task.call().booleanValue();
/* 201 */     long timeAfterCompile = System.currentTimeMillis();
/*     */ 
/* 203 */     matchTimestamps(targetDir, filenames, timestamps);
/* 204 */     long timeAfterStamping = System.currentTimeMillis();
/* 205 */     if (trace != null)
/*     */     {
/* 207 */       long compileTime = timeAfterCompile - timeBeforeCompile;
/* 208 */       long overheadTime = timeAfterStamping - timeStart - compileTime;
/* 209 */       trace.report(7, new Object[] { "compile took ", Long.valueOf(compileTime), " ms (", Long.valueOf(overheadTime), " ms overhead)" });
/*     */     }
/*     */ 
/* 212 */     if (wasSuccessful)
/*     */       return;
/* 214 */     throw new IOException("compilation failed");
/*     */   }
/*     */ 
/*     */   public void matchTimestamps(File targetDir, List<String> javaFilenames, List<Long> stamps)
/*     */     throws IOException
/*     */   {
/* 229 */     boolean areExtraChecksEnabled = this.m_areExtraChecksEnabled;
/* 230 */     ClassFileEditor editor = this.m_editor;
/* 231 */     StringBuilder sb = new StringBuilder(64);
/* 232 */     Map dirListings = new HashMap();
/* 233 */     for (int n = javaFilenames.size() - 1; n >= 0; --n)
/*     */     {
/* 235 */       long timestamp = ((Long)stamps.get(n)).longValue();
/* 236 */       String javaFilename = (String)javaFilenames.get(n);
/* 237 */       if ((areExtraChecksEnabled) && (!javaFilename.endsWith(".java")))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 242 */       String namePrefix = javaFilename.substring(0, javaFilename.length() - 5);
/* 243 */       sb.setLength(0);
/* 244 */       sb.append(namePrefix);
/* 245 */       sb.append(".class");
/* 246 */       String classfilepath = sb.toString();
/* 247 */       File classfile = new File(targetDir, classfilepath);
/* 248 */       String classfilename = classfile.getName();
/* 249 */       File parentDir = classfile.getParentFile();
/* 250 */       String[] files = (String[])dirListings.get(parentDir);
/* 251 */       if (files == null)
/*     */       {
/* 253 */         files = parentDir.list();
/* 254 */         if (files == null)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 259 */         Arrays.sort(files);
/* 260 */         dirListings.put(parentDir, files);
/*     */       }
/* 262 */       int index = Arrays.binarySearch(files, classfilename);
/* 263 */       if (index < 0)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 268 */       if (editor != null)
/*     */       {
/* 270 */         editor.alter(classfile);
/*     */       }
/* 272 */       classfile.setLastModified(timestamp);
/*     */ 
/* 274 */       sb.setLength(0);
/* 275 */       sb.append(classfilename);
/* 276 */       sb.setLength(sb.length() - 6);
/* 277 */       sb.append('$');
/* 278 */       String prefix = sb.toString();
/* 279 */       while (--index >= 0)
/*     */       {
/* 282 */         classfilename = files[(index--)];
/* 283 */         if (!classfilename.startsWith(prefix)) break; if (!classfilename.endsWith(".class")) {
/*     */           break;
/*     */         }
/*     */ 
/* 287 */         classfile = new File(parentDir, classfilename);
/* 288 */         if (editor != null)
/*     */         {
/* 290 */           editor.alter(classfile);
/*     */         }
/* 292 */         classfile.setLastModified(timestamp);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void report(Diagnostic<? extends JavaFileObject> diagnostic)
/*     */   {
/* 300 */     List diagnostics = this.m_diagnostics;
/*     */ 
/* 302 */     Diagnostic.Kind kind = diagnostic.getKind();
/* 303 */     JavaFileObject file = (JavaFileObject)diagnostic.getSource();
/* 304 */     long line = diagnostic.getLineNumber(); long column = diagnostic.getColumnNumber();
/* 305 */     String code = diagnostic.getCode();
/* 306 */     String message = diagnostic.getMessage(null);
/*     */     int level;
/* 308 */     switch (1.$SwitchMap$javax$tools$Diagnostic$Kind[kind.ordinal()])
/*     */     {
/*     */     case 1:
/* 311 */       level = 3;
/* 312 */       break;
/*     */     case 2:
/*     */     case 3:
/* 315 */       level = 4;
/* 316 */       break;
/*     */     case 4:
/* 318 */       level = 5;
/* 319 */       break;
/*     */     case 5:
/*     */     default:
/* 322 */       level = 6;
/*     */     }
/*     */ 
/* 325 */     String filename = (file != null) ? file.toString() : "";
/* 326 */     if (filename.startsWith(this.m_sourceFilenamePrefix))
/*     */     {
/* 328 */       filename = filename.substring(this.m_sourceFilenamePrefix.length());
/*     */     }
/* 330 */     if (code.startsWith("compiler.note.unchecked."))
/*     */     {
/* 332 */       return;
/*     */     }
/* 334 */     this.m_trace.report(level, new Object[] { "at line ", Long.valueOf(line), ", column ", Long.valueOf(column), " of ", filename, " : ", code, "\n", message });
/* 335 */     if (diagnostics == null)
/*     */       return;
/* 337 */     diagnostics.add(diagnostic);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 345 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99523 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.JavaCompileManager
 * JD-Core Version:    0.5.4
 */