/*     */ package intradoc.tools;
/*     */ 
/*     */ import intradoc.tools.common.ClassFileConstants;
/*     */ import intradoc.tools.utils.TextUtils;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.net.URL;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class ClassDependencyScanner
/*     */   implements ClassFileConstants
/*     */ {
/*     */   public static final int F_USE_REFERENCES_AS_PRIMARIES = 0;
/*     */   public static final int F_USE_REFERENCED_AS_PRIMARIES = 1;
/*     */   public static final int F_ONLY_SHOW_PRIMARIES = 0;
/*     */   public static final int F_ALSO_SHOW_DEPENDENCIES = 2;
/*     */   public static final int F_SORT_PRIMARIES = 4;
/*     */   public static final int F_SORT_DEPENDENCIES = 8;
/*     */   public boolean m_isVerbose;
/*     */   public Map<String, Collection<String>> m_classReferences;
/*     */   public Map<String, Collection<String>> m_referencedClasses;
/*     */   protected List<ClassFile> m_classFiles;
/*     */   protected List<File> m_classpathFiles;
/*     */ 
/*     */   public static void usage()
/*     */   {
/*  42 */     System.err.print("Usage: [-d | -r] -v -cp <dir>[:<dir>...] classnames\nBy default, display the entire list of dependency classes.\nIf -d is used, display the classes and their direct dependencies.\nIf -r is used, display the dependencies and their dependents.\nUse -v for verbose output while scanning.\nFor classnames, the \"*\" and \"**\" wildcards may be used.\n");
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/*  52 */     ClassDependencyScanner scanner = new ClassDependencyScanner();
/*  53 */     int flags = 0;
/*  54 */     int a = 0;
/*  55 */     if ((a < args.length) && (args[0].equals("-v")))
/*     */     {
/*  57 */       scanner.m_isVerbose = true;
/*  58 */       ++a;
/*     */     }
/*  60 */     if (a < args.length)
/*     */     {
/*  62 */       if (args[0].equals("-d"))
/*     */       {
/*  64 */         flags = 2;
/*  65 */         ++a;
/*     */       }
/*  67 */       else if (args[0].equals("-r"))
/*     */       {
/*  69 */         flags = 3;
/*  70 */         ++a;
/*     */       }
/*     */     }
/*  73 */     if ((a < args.length) && (args[0].equals("-v")))
/*     */     {
/*  75 */       scanner.m_isVerbose = true;
/*  76 */       ++a;
/*     */     }
/*  78 */     flags |= 12;
/*  79 */     if ((a + 1 >= args.length) || (!args[a].equals("-cp")))
/*     */     {
/*  81 */       usage();
/*  82 */       return;
/*     */     }
/*  84 */     ++a;
/*  85 */     String[] classpath = args[(a++)].split(":");
/*  86 */     int numClassnames = args.length - a;
/*  87 */     if (numClassnames < 1)
/*     */     {
/*  89 */       usage();
/*  90 */       return;
/*     */     }
/*  92 */     List classnames = new ArrayList(numClassnames);
/*  93 */     while (a < args.length)
/*     */     {
/*  95 */       classnames.add(args[(a++)]);
/*     */     }
/*     */     try
/*     */     {
/*  99 */       scanner.setClasspath(classpath);
/* 100 */       scanner.scan(classnames);
/*     */     }
/*     */     catch (IOException ioe)
/*     */     {
/* 104 */       ioe.printStackTrace();
/* 105 */       return;
/*     */     }
/* 107 */     scanner.printResults(System.out, flags);
/*     */   }
/*     */ 
/*     */   public void setClasspath(String[] classpathElements)
/*     */     throws IOException
/*     */   {
/* 147 */     this.m_classFiles = new ArrayList();
/* 148 */     for (int e = 0; e < classpathElements.length; ++e)
/*     */     {
/* 150 */       File element = new File(classpathElements[e]);
/* 151 */       if (!element.exists()) {
/*     */         continue;
/*     */       }
/*     */ 
/* 155 */       if (!element.isDirectory())
/*     */       {
/* 157 */         throw new IOException(new StringBuilder().append("classpath element \"").append(classpathElements[e]).append("\" is not a directory").toString());
/*     */       }
/* 159 */       if (this.m_isVerbose)
/*     */       {
/* 161 */         System.err.println(new StringBuilder().append("scanning classpath: ").append(element).toString());
/*     */       }
/* 163 */       appendClasspath(element, element, "");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void appendClasspath(File elementDir, File dir, String path)
/*     */   {
/* 169 */     List classfiles = this.m_classFiles;
/* 170 */     File[] files = dir.listFiles();
/* 171 */     Arrays.sort(files);
/* 172 */     for (File file : files)
/*     */     {
/* 174 */       String filename = file.getName();
/* 175 */       if (filename.equals(".svn")) {
/*     */         continue;
/*     */       }
/*     */ 
/* 179 */       if (file.isDirectory())
/*     */       {
/* 181 */         StringBuilder subPath = new StringBuilder(path);
/* 182 */         if (path.length() > 0)
/*     */         {
/* 184 */           subPath.append('.');
/*     */         }
/* 186 */         subPath.append(filename);
/* 187 */         appendClasspath(elementDir, file, subPath.toString());
/*     */       } else {
/* 189 */         if ((!file.isFile()) || (!filename.endsWith(".class")))
/*     */           continue;
/* 191 */         StringBuilder classname = new StringBuilder(path);
/* 192 */         if (path.length() > 0)
/*     */         {
/* 194 */           classname.append('.');
/*     */         }
/* 196 */         classname.append(filename.substring(0, filename.length() - 6));
/* 197 */         ClassFile classfile = new ClassFile(file, classname.toString());
/* 198 */         classfiles.add(classfile);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void scan(List<String> classnames)
/*     */     throws IOException
/*     */   {
/* 211 */     List classfiles = this.m_classFiles;
/* 212 */     List matchingClassfiles = new ArrayList(classfiles.size());
/* 213 */     for (String classname : classnames)
/*     */     {
/* 215 */       String patternString = TextUtils.wildcardPathToRegex(classname);
/* 216 */       pattern = Pattern.compile(patternString);
/* 217 */       for (ClassFile classfile : classfiles)
/*     */       {
/* 219 */         if (pattern.matcher(classfile.m_classname).matches())
/*     */         {
/* 221 */           matchingClassfiles.add(classfile);
/*     */         }
/*     */       }
/*     */     }
/*     */     Pattern pattern;
/* 226 */     if (this.m_classReferences == null)
/*     */     {
/* 228 */       this.m_classReferences = new HashMap();
/*     */     }
/* 230 */     if (this.m_referencedClasses == null)
/*     */     {
/* 232 */       this.m_referencedClasses = new HashMap();
/*     */     }
/* 234 */     for (ClassFile classfile : matchingClassfiles)
/*     */     {
/* 236 */       if (this.m_isVerbose)
/*     */       {
/* 238 */         System.err.println(new StringBuilder().append("processing ").append(classfile.m_classname).toString());
/*     */       }
/* 240 */       scanClass(classfile);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected ClassFile findClass(String classname) throws IOException
/*     */   {
/* 246 */     List classfiles = this.m_classFiles;
/* 247 */     for (ClassFile classfile : classfiles)
/*     */     {
/* 249 */       if (classfile.m_classname.equals(classname))
/*     */       {
/* 251 */         return classfile;
/*     */       }
/*     */     }
/* 254 */     String path = new StringBuilder().append(classname.replace('.', '/')).append(".class").toString();
/* 255 */     URL url = super.getClass().getClassLoader().getResource(path);
/* 256 */     if (url == null)
/*     */     {
/* 258 */       throw new IOException(new StringBuilder().append("unable to find referenced class: ").append(classname).toString());
/*     */     }
/* 260 */     return null;
/*     */   }
/*     */ 
/*     */   protected void scanClass(ClassFile classfile) throws IOException
/*     */   {
/* 265 */     Map classReferences = this.m_classReferences;
/* 266 */     Map referencedClasses = this.m_referencedClasses;
/* 267 */     String myClassname = classfile.m_classname;
/*     */ 
/* 269 */     if (classReferences.containsKey(myClassname))
/*     */     {
/* 271 */       return;
/*     */     }
/* 273 */     Collection myReferences = new ArrayList();
/* 274 */     classReferences.put(myClassname, myReferences);
/*     */ 
/* 276 */     FileInputStream fis = new FileInputStream(classfile);
/* 277 */     DataInputStream dis = new DataInputStream(fis);
/* 278 */     int magic = dis.readInt();
/* 279 */     if (magic != -889275714)
/*     */     {
/* 281 */       throw new IOException(new StringBuilder().append(classfile).append(": bad classfile magic").toString());
/*     */     }
/* 283 */     short minor = dis.readShort();
/* 284 */     short major = dis.readShort();
/* 285 */     if ((major < 45) || (major > 51))
/*     */     {
/* 287 */       throw new IOException(new StringBuilder().append(classfile).append(": unsupported class file version ").append(major).append('.').append(minor).toString());
/*     */     }
/* 289 */     int numConstants = dis.readShort() & 0xFFFF;
/* 290 */     if (numConstants < 1)
/*     */     {
/* 292 */       throw new IOException(new StringBuilder().append(classfile).append(": bad constant pool count").toString());
/*     */     }
/* 294 */     boolean[] areClassnames = new boolean[numConstants];
/* 295 */     String[] utf8Strings = new String[numConstants];
/*     */ 
/* 297 */     for (int c = 1; c < numConstants; ++c)
/*     */     {
/* 299 */       byte tag = dis.readByte();
/* 300 */       int size = 0;
/* 301 */       switch (tag)
/*     */       {
/*     */       case 7:
/* 304 */         int nameIndex = dis.readShort() & 0xFFFF;
/* 305 */         if ((nameIndex < 1) || (nameIndex >= numConstants))
/*     */         {
/* 307 */           throw new IOException(new StringBuilder().append(classfile).append(": bad classname reference index ").append(nameIndex).toString());
/*     */         }
/* 309 */         areClassnames[nameIndex] = true;
/* 310 */         break;
/*     */       case 8:
/*     */       case 16:
/* 313 */         size = 2;
/* 314 */         break;
/*     */       case 15:
/* 316 */         size = 3;
/* 317 */         break;
/*     */       case 3:
/*     */       case 4:
/*     */       case 9:
/*     */       case 10:
/*     */       case 11:
/*     */       case 12:
/*     */       case 18:
/* 325 */         size = 4;
/* 326 */         break;
/*     */       case 5:
/*     */       case 6:
/* 329 */         ++c;
/* 330 */         size = 8;
/* 331 */         break;
/*     */       case 1:
/* 333 */         utf8Strings[c] = dis.readUTF();
/* 334 */         break;
/*     */       case 2:
/*     */       case 13:
/*     */       case 14:
/*     */       case 17:
/*     */       default:
/* 336 */         throw new IOException(new StringBuilder().append(classfile).append(": unknown constant type ").append(tag).toString());
/*     */       }
/* 338 */       while (size >= 4)
/*     */       {
/* 340 */         dis.readInt();
/* 341 */         size -= 4;
/*     */       }
/* 343 */       while (size > 0)
/*     */       {
/* 345 */         dis.readByte();
/* 346 */         --size;
/*     */       }
/*     */     }
/* 349 */     dis.close();
/*     */ 
/* 351 */     for (int c = 1; c < numConstants; ++c)
/*     */     {
/* 353 */       if (areClassnames[c] == 0)
/*     */         continue;
/* 355 */       String classname = utf8Strings[c];
/* 356 */       if (classname == null)
/*     */       {
/* 358 */         throw new IOException(new StringBuilder().append(classfile).append(": bad classname reference at index ").append(c).toString());
/*     */       }
/*     */ 
/* 361 */       boolean wasArray = false;
/* 362 */       while (classname.startsWith("["))
/*     */       {
/* 364 */         classname = classname.substring(1);
/* 365 */         wasArray = true;
/*     */       }
/*     */ 
/* 368 */       if (wasArray)
/*     */       {
/* 370 */         if (!classname.endsWith(";")) {
/*     */           continue;
/*     */         }
/*     */ 
/* 374 */         classname = classname.substring(0, classname.length() - 1);
/*     */       }
/* 376 */       if ((wasArray) && (!classname.endsWith(";")))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 381 */       classname = classname.replace('/', '.');
/*     */ 
/* 384 */       myReferences.add(classname);
/* 385 */       Collection backReferences = (Collection)referencedClasses.get(classname);
/* 386 */       if (backReferences == null)
/*     */       {
/* 388 */         backReferences = new HashSet();
/* 389 */         referencedClasses.put(classname, backReferences);
/*     */       }
/* 391 */       backReferences.add(myClassname);
/*     */ 
/* 394 */       if (classname.startsWith("java")) continue; if (classname.startsWith("sun"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 399 */       ClassFile referencedClassFile = findClass(classname);
/* 400 */       if (referencedClassFile == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 404 */       scanClass(referencedClassFile);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void printResults(PrintStream ps, int flags)
/*     */   {
/* 428 */     boolean useReversePrimaries = (flags & 0x1) != 0;
/* 429 */     boolean shouldShowDependencies = (flags & 0x2) != 0;
/* 430 */     boolean shouldSortPrimaries = (flags & 0x4) != 0;
/* 431 */     boolean shouldSortDependencies = (flags & 0x8) != 0;
/*     */ 
/* 433 */     Map map = (useReversePrimaries) ? this.m_referencedClasses : this.m_classReferences;
/* 434 */     int numPrimary = map.size();
/* 435 */     String[] primaries = new String[numPrimary];
/* 436 */     map.keySet().toArray(primaries);
/* 437 */     if (shouldSortPrimaries)
/*     */     {
/* 439 */       Arrays.sort(primaries);
/*     */     }
/* 441 */     for (String primary : primaries)
/*     */     {
/* 443 */       ps.println(primary);
/* 444 */       if (!shouldShowDependencies)
/*     */         continue;
/* 446 */       Collection dependsCollection = (Collection)map.get(primary);
/* 447 */       int numDepends = dependsCollection.size();
/* 448 */       String[] depends = new String[numDepends];
/* 449 */       int i = 0;
/* 450 */       for (String depend : dependsCollection)
/*     */       {
/* 452 */         depends[(i++)] = depend;
/*     */       }
/* 454 */       if (shouldSortDependencies)
/*     */       {
/* 456 */         Arrays.sort(depends);
/*     */       }
/* 458 */       for (String depend : depends)
/*     */       {
/* 460 */         ps.println(new StringBuilder().append('\t').append(depend).toString());
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 480 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99326 $";
/*     */   }
/*     */ 
/*     */   public class ClassFile extends File
/*     */   {
/*     */     public final String m_classname;
/*     */ 
/*     */     public ClassFile(File file, String classname)
/*     */     {
/* 472 */       super(file, "");
/* 473 */       this.m_classname = classname;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.ClassDependencyScanner
 * JD-Core Version:    0.5.4
 */