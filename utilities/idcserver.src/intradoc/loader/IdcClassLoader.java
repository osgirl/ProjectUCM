/*     */ package intradoc.loader;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.io.IdcByteHandlerInputStream;
/*     */ import intradoc.io.zip.IdcZipEnvironment;
/*     */ import intradoc.io.zip.IdcZipFile;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.MapUtils;
/*     */ import intradoc.util.SimpleTracingCallback;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.net.URL;
/*     */ import java.security.ProtectionDomain;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class IdcClassLoader extends ClassLoader
/*     */   implements IdcLoader
/*     */ {
/*  66 */   protected static String[] DEFAULT_PARENT_ONLY_CLASSNAME_PREFIXES = { "intradoc.io.", "intradoc.loader.", "intradoc.util.", "java.", "javax." };
/*     */   public IdcZipEnvironment m_zipenv;
/*     */   public String m_name;
/*     */   protected ClassLoader m_parent;
/*     */   public boolean m_shouldUseStaticMapForZipfiles;
/*     */   public static Map<String, IdcZipFile> s_zipfiles;
/*     */   public Map<String, IdcZipFile> m_zipfiles;
/*     */   public IdcLoaderElementList m_classPathElements;
/*     */   public Map<String, IdcClassInfo> m_classInfos;
/*     */   protected Map<String, Boolean> m_useParentForClasses;
/*     */   protected char[][] m_useParentForClassPrefixes;
/*     */   public Map<String, Class> m_loadedClasses;
/*     */   protected List<Pattern> m_elementExcludePatternsForResources;
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/* 147 */     String classname = args[0];
/* 148 */     String[] mainArgs = new String[args.length - 1];
/* 149 */     System.arraycopy(args, 1, mainArgs, 0, args.length - 1);
/*     */     try
/*     */     {
/* 152 */       startMain(classname, mainArgs);
/*     */     }
/*     */     catch (InvocationTargetException ite)
/*     */     {
/* 156 */       Throwable t = ite.getCause();
/* 157 */       t.printStackTrace(System.err);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 161 */       e.printStackTrace(System.err);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void startMain(String classname, String[] args)
/*     */     throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/* 183 */     ClassLoader parent = IdcLoaderUtils.getCallerClassLoader();
/* 184 */     IdcClassLoader loader = new IdcClassLoader(parent);
/* 185 */     loader.init(null);
/* 186 */     Thread.currentThread().setContextClassLoader(loader);
/* 187 */     loader.invokeMain(classname, args);
/*     */   }
/*     */ 
/*     */   public String getClasspath()
/*     */   {
/* 195 */     return this.m_classPathElements.getClasspathAsString();
/*     */   }
/*     */ 
/*     */   public IdcClassLoader()
/*     */   {
/* 206 */     initAttributes();
/*     */   }
/*     */ 
/*     */   public IdcClassLoader(ClassLoader parent)
/*     */   {
/* 214 */     super(parent);
/* 215 */     initAttributes();
/*     */   }
/*     */ 
/*     */   public void initAttributes()
/*     */   {
/* 223 */     this.m_zipenv = new IdcZipEnvironment();
/* 224 */     int verbosity = IdcLoaderUtils.getSystemPropertyAsIntWithDefault(this, "idc.loader.verbosity", 3);
/* 225 */     this.m_zipenv.m_verbosity = verbosity;
/* 226 */     this.m_zipfiles = MapUtils.createConcurrentMap();
/* 227 */     if (s_zipfiles == null)
/*     */     {
/* 229 */       s_zipfiles = MapUtils.createConcurrentMap();
/*     */     }
/* 231 */     this.m_classInfos = MapUtils.createConcurrentMap();
/* 232 */     this.m_useParentForClasses = MapUtils.createConcurrentMap();
/* 233 */     int length = DEFAULT_PARENT_ONLY_CLASSNAME_PREFIXES.length;
/* 234 */     this.m_useParentForClassPrefixes = new char[length][];
/* 235 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 237 */       this.m_useParentForClassPrefixes[i] = DEFAULT_PARENT_ONLY_CLASSNAME_PREFIXES[i].toCharArray();
/*     */     }
/* 239 */     this.m_loadedClasses = MapUtils.createConcurrentMap();
/* 240 */     this.m_elementExcludePatternsForResources = new ArrayList();
/*     */   }
/*     */ 
/*     */   public void clearUseParentForClasses()
/*     */   {
/* 248 */     int verbosity = this.m_zipenv.m_verbosity;
/* 249 */     if (verbosity >= 7)
/*     */     {
/* 251 */       Throwable t = new Throwable();
/* 252 */       report(7, new Object[] { "clearUseParentForClasses() at:\n", t });
/*     */     }
/* 254 */     else if (verbosity >= 6)
/*     */     {
/* 256 */       report(6, new Object[] { "clearUseParentForClasses()" });
/*     */     }
/* 258 */     this.m_useParentForClassPrefixes = ((char[][])null);
/* 259 */     this.m_useParentForClasses.clear();
/*     */   }
/*     */ 
/*     */   public void copyUseParentForClasses(IdcClassLoader source)
/*     */   {
/* 264 */     int numPrefixes = source.m_useParentForClassPrefixes.length;
/* 265 */     this.m_useParentForClassPrefixes = new char[numPrefixes][];
/* 266 */     for (int i = 0; i < numPrefixes; ++i)
/*     */     {
/* 268 */       int len = source.m_useParentForClassPrefixes[i].length;
/* 269 */       this.m_useParentForClassPrefixes[i] = new char[len];
/* 270 */       System.arraycopy(source.m_useParentForClassPrefixes[i], 0, this.m_useParentForClassPrefixes[i], 0, len);
/*     */     }
/*     */ 
/* 273 */     this.m_useParentForClasses = MapUtils.cloneMap(source.m_useParentForClasses);
/*     */   }
/*     */ 
/*     */   public void init(String classpathString)
/*     */     throws IOException
/*     */   {
/* 284 */     int verbosity = this.m_zipenv.m_verbosity;
/*     */ 
/* 286 */     if (null == classpathString)
/*     */     {
/* 288 */       classpathString = System.getProperty("idc.classpath");
/*     */     }
/*     */     List pathList;
/* 290 */     if (null == classpathString)
/*     */     {
/* 293 */       classpathString = System.getProperty("java.class.path");
/* 294 */       List pathList = IdcLoaderUtils.splitPathElements(classpathString);
/*     */ 
/* 296 */       String pathExceptionsString = System.getProperty("idc.loader.delegate.path");
/* 297 */       List pathExceptions = (pathExceptionsString == null) ? null : IdcLoaderUtils.splitPathElements(pathExceptionsString);
/*     */ 
/* 299 */       int numPaths = pathList.size(); int i = 0;
/* 300 */       while (i < numPaths)
/*     */       {
/* 302 */         String path = (String)pathList.get(i);
/*     */ 
/* 304 */         if (pathExceptions != null)
/*     */         {
/* 306 */           for (String pathException : pathExceptions)
/*     */           {
/* 308 */             if ((pathException.length() > 0) && (path.endsWith(pathException)))
/*     */             {
/* 310 */               pathList.remove(i);
/* 311 */               --numPaths;
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 316 */         ++i;
/*     */       }
/* 318 */       StringBuilder str = new StringBuilder("idc.classpath is not set, instead using the following from java.class.path:\n");
/*     */ 
/* 320 */       for (i = 0; i < numPaths; ++i)
/*     */       {
/* 322 */         str.append('\t');
/* 323 */         str.append((String)pathList.get(i));
/*     */       }
/* 325 */       if (numPaths < 1)
/*     */       {
/* 327 */         str.append("No valid paths specified.  Please set idc.classpath.");
/*     */       }
/* 329 */       if (verbosity >= 6)
/*     */       {
/* 331 */         report(6, new Object[] { str });
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 336 */       pathList = IdcLoaderUtils.splitPathElements(classpathString);
/*     */     }
/*     */ 
/* 339 */     initWithList(pathList);
/*     */   }
/*     */ 
/*     */   public void initWithList(List<String> pathList)
/*     */     throws IOException
/*     */   {
/* 350 */     this.m_zipenv.init();
/* 351 */     this.m_parent = getParent();
/*     */ 
/* 353 */     int verbosity = this.m_zipenv.m_verbosity;
/* 354 */     if (verbosity >= 6)
/*     */     {
/* 356 */       report(6, new Object[] { "verbosity level: ", Integer.valueOf(this.m_zipenv.m_verbosity) });
/*     */     }
/* 358 */     if (this.m_classPathElements == null)
/*     */     {
/* 360 */       this.m_classPathElements = new IdcLoaderElementList(this);
/*     */     }
/* 362 */     this.m_zipenv.m_doPreload = (0 != IdcLoaderUtils.getSystemPropertyAsIntWithDefault(this, "idc.loader.doPreload", 1));
/* 363 */     if ((this.m_zipenv.m_doPreload) && (verbosity >= 6))
/*     */     {
/* 365 */       report(6, new Object[] { "preloading all classes" });
/*     */     }
/*     */ 
/* 368 */     int defaultLoadOrder = IdcLoaderUtils.getSystemPropertyAsIntWithDefault(this, "idc.loader.defaultLoadOrder", 0);
/* 369 */     this.m_classPathElements.m_loadOrder = defaultLoadOrder;
/*     */ 
/* 371 */     this.m_classPathElements.initElements(pathList);
/*     */   }
/*     */ 
/*     */   public void invokeMain(String classname, String[] args)
/*     */     throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/* 388 */     Class cl = findClass(classname);
/* 389 */     Class[] parameters = { args.getClass() };
/* 390 */     Method method = cl.getMethod("main", parameters);
/* 391 */     Object[] arguments = { args };
/* 392 */     method.invoke(null, arguments);
/*     */   }
/*     */ 
/*     */   public void addClassPathElement(String pathname, int loadOrder)
/*     */     throws Exception
/*     */   {
/* 406 */     IdcLoaderElement element = this.m_classPathElements.addPathElement(pathname, loadOrder);
/* 407 */     if (element == null)
/*     */     {
/* 409 */       return;
/*     */     }
/* 411 */     String elementName = element.getName();
/* 412 */     for (int i = this.m_elementExcludePatternsForResources.size() - 1; i >= 0; --i)
/*     */     {
/* 414 */       Pattern elementNamePattern = (Pattern)this.m_elementExcludePatternsForResources.get(i);
/* 415 */       if (!elementNamePattern.matcher(elementName).matches())
/*     */         continue;
/* 417 */       element.m_isExcludedForResources = true;
/* 418 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setUseParentForClass(String name, boolean useParent)
/*     */   {
/* 431 */     int verbosity = this.m_zipenv.m_verbosity;
/* 432 */     if (verbosity >= 7)
/*     */     {
/* 434 */       Throwable t = new Throwable();
/* 435 */       report(7, new Object[] { "setUseParentForClass(", name, ", ", Boolean.valueOf(useParent), ") at:\n", t });
/*     */     }
/* 437 */     else if (verbosity >= 6)
/*     */     {
/* 439 */       report(6, new Object[] { "setUseParentForClass(", name, ", ", Boolean.valueOf(useParent), ")" });
/*     */     }
/* 441 */     if (useParent)
/*     */     {
/* 443 */       this.m_useParentForClasses.put(name, Boolean.TRUE);
/*     */     }
/*     */     else
/*     */     {
/* 447 */       this.m_useParentForClasses.remove(name);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setUseParentForClassPrefix(String prefix)
/*     */   {
/* 458 */     int verbosity = this.m_zipenv.m_verbosity;
/* 459 */     if (verbosity >= 7)
/*     */     {
/* 461 */       Throwable t = new Throwable();
/* 462 */       report(7, new Object[] { "setUseParentForClassPrefix(", prefix, ") at:\n", t });
/*     */     }
/* 464 */     else if (verbosity >= 6)
/*     */     {
/* 466 */       report(6, new Object[] { "setUseParentForClassPrefix(", prefix, ")" });
/*     */     }
/* 468 */     char[] newPrefix = prefix.toCharArray();
/* 469 */     int len = newPrefix.length;
/* 470 */     if (len < 1)
/*     */     {
/* 472 */       return;
/*     */     }
/* 474 */     char[][] prefixes = this.m_useParentForClassPrefixes;
/* 475 */     int numPrefixes = (null == prefixes) ? 0 : prefixes.length;
/* 476 */     char[][] newPrefixes = new char[numPrefixes + 1][];
/* 477 */     int insertIndex = numPrefixes;
/* 478 */     for (int i = 0; i < numPrefixes; ++i)
/*     */     {
/* 480 */       int cmp = 0;
/* 481 */       int plen = prefixes[i].length;
/* 482 */       for (int index = 0; (index < newPrefix.length) && (index < plen); ++index)
/*     */       {
/* 484 */         cmp = newPrefix[index] - prefixes[i][index];
/* 485 */         if (cmp != 0) {
/*     */           break;
/*     */         }
/*     */       }
/*     */ 
/* 490 */       if (cmp == 0)
/*     */       {
/* 493 */         if (len == plen)
/*     */         {
/* 496 */           return;
/*     */         }
/* 498 */         cmp = len - plen;
/*     */       }
/* 500 */       if (cmp < 0)
/*     */       {
/* 502 */         insertIndex = i;
/* 503 */         break;
/*     */       }
/* 505 */       newPrefixes[i] = prefixes[i];
/*     */     }
/* 507 */     newPrefixes[insertIndex] = newPrefix;
/* 508 */     for (int i = insertIndex; i < numPrefixes; ++i)
/*     */     {
/* 510 */       newPrefixes[(i + 1)] = prefixes[i];
/*     */     }
/* 512 */     this.m_useParentForClassPrefixes = newPrefixes;
/*     */   }
/*     */ 
/*     */   public void addElementExcludePatternForResources(Pattern elementNamePattern)
/*     */   {
/* 517 */     this.m_elementExcludePatternsForResources.add(elementNamePattern);
/* 518 */     IdcLoaderElement[] elements = this.m_classPathElements.m_elements;
/* 519 */     for (int i = elements.length - 1; i >= 0; --i)
/*     */     {
/* 521 */       IdcLoaderElement element = elements[i];
/* 522 */       if (element == null) continue; if (element.m_isExcludedForResources) {
/*     */         continue;
/*     */       }
/*     */ 
/* 526 */       String elementName = element.getName();
/* 527 */       if (!elementNamePattern.matcher(elementName).matches())
/*     */         continue;
/* 529 */       element.m_isExcludedForResources = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean shouldClassUseParentOnly(String name)
/*     */   {
/* 544 */     Boolean b = (Boolean)this.m_useParentForClasses.get(name);
/* 545 */     if (null != b)
/*     */     {
/* 547 */       if (this.m_zipenv.m_verbosity >= 6)
/*     */       {
/* 549 */         report(6, new Object[] { "shouldClassUseParentOnly(", name, "): ", b, " [exact match]" });
/*     */       }
/* 551 */       return b.booleanValue();
/*     */     }
/* 553 */     int len = name.length();
/*     */ 
/* 555 */     char ch0 = name.charAt(0);
/* 556 */     char[][] prefixes = this.m_useParentForClassPrefixes;
/* 557 */     if (null == prefixes)
/*     */     {
/* 559 */       return false;
/*     */     }
/* 561 */     int min = 0; int max = prefixes.length - 1;
/*     */     do
/*     */     {
/* 564 */       int mid = max + min >> 1;
/* 565 */       int cmp = ch0 - prefixes[mid][0];
/* 566 */       if (cmp == 0)
/*     */       {
/* 568 */         int plen = prefixes[mid].length;
/* 569 */         for (int index = 1; (index < len) && (index < plen); ++index)
/*     */         {
/* 571 */           cmp = name.charAt(index) - prefixes[mid][index];
/* 572 */           if (cmp != 0) {
/*     */             break;
/*     */           }
/*     */         }
/*     */ 
/* 577 */         if (cmp == 0)
/*     */         {
/* 585 */           if ((len >= plen) && (this.m_zipenv.m_verbosity >= 6))
/*     */           {
/* 587 */             String prefix = new String(prefixes[mid]);
/* 588 */             report(6, new Object[] { "shouldClassUseParentOnly(", name, "): true [prefix: ", prefix, "]" });
/*     */           }
/* 590 */           return len >= plen;
/*     */         }
/*     */       }
/*     */ 
/* 594 */       if (cmp < 0)
/*     */       {
/* 597 */         max = mid - 1;
/*     */       }
/*     */       else
/*     */       {
/* 602 */         min = mid + 1;
/*     */       }
/*     */     }
/* 604 */     while (max >= min);
/* 605 */     return false;
/*     */   }
/*     */ 
/*     */   protected Class defineClass(IdcClassInfo info)
/*     */     throws ClassFormatError
/*     */   {
/* 621 */     if (this.m_zipenv.m_verbosity >= 7)
/*     */     {
/* 623 */       report(7, new Object[] { "defineClass(", info.m_className, ")" });
/*     */     }
/* 625 */     int i = info.m_className.lastIndexOf(46);
/* 626 */     if (i != -1)
/*     */     {
/* 628 */       String pkgname = info.m_className.substring(0, i);
/*     */ 
/* 630 */       Package pkg = getPackage(pkgname);
/* 631 */       if (pkg == null)
/*     */       {
/* 633 */         definePackage(pkgname, null, null, null, null, null, null, null);
/*     */       }
/*     */     }
/*     */     try
/*     */     {
/* 638 */       ProtectionDomain domain = info.m_origin.m_protectionDomain;
/* 639 */       Class cl = defineClass(info.m_className, info.m_classBytes, 0, info.m_classBytes.length, domain);
/* 640 */       return cl;
/*     */     }
/*     */     catch (ClassFormatError e)
/*     */     {
/* 644 */       if (this.m_zipenv.m_verbosity >= 3)
/*     */       {
/* 646 */         report(3, new Object[] { "bad format for class \"", info.m_className, "\"" });
/*     */       }
/* 648 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Class loadClass(String name, boolean resolve)
/*     */     throws ClassNotFoundException
/*     */   {
/* 664 */     int verbosity = this.m_zipenv.m_verbosity;
/* 665 */     if (verbosity >= 7)
/*     */     {
/* 667 */       report(7, new Object[] { "loadClass(\"", name, "\" ,", Boolean.valueOf(resolve), ")" });
/*     */     }
/* 669 */     boolean useParentOnly = shouldClassUseParentOnly(name);
/*     */ 
/* 671 */     Class cl = null;
/*     */ 
/* 673 */     if (!useParentOnly)
/*     */     {
/*     */       try
/*     */       {
/* 677 */         cl = findClass(name);
/*     */       }
/*     */       catch (ClassNotFoundException ignore)
/*     */       {
/* 682 */         if (verbosity >= 7)
/*     */         {
/* 684 */           report(7, new Object[] { name, " not found." });
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 689 */     if (null == cl)
/*     */     {
/* 691 */       if (verbosity >= 7)
/*     */       {
/* 693 */         report(7, new Object[] { "using parent classloader to load ", name });
/*     */       }
/* 695 */       cl = this.m_parent.loadClass(name);
/*     */     }
/* 697 */     if (resolve)
/*     */     {
/* 699 */       if (verbosity >= 7)
/*     */       {
/* 701 */         report(7, new Object[] { "resolving class ", name });
/*     */       }
/* 703 */       resolveClass(cl);
/*     */     }
/*     */ 
/* 706 */     if (verbosity > 0)
/*     */     {
/* 708 */       ClassLoader loader = cl.getClassLoader();
/* 709 */       String loaderName = (null == loader) ? "<bootstrap loader>" : loader.getClass().getName();
/* 710 */       if (verbosity >= 7)
/*     */       {
/* 712 */         report(7, new Object[] { loaderName, (resolve) ? " resolved " : " loaded ", name });
/*     */       }
/*     */     }
/* 715 */     this.m_loadedClasses.put(name, cl);
/* 716 */     return cl;
/*     */   }
/*     */ 
/*     */   protected Class findClass(String name)
/*     */     throws ClassNotFoundException
/*     */   {
/* 731 */     if (this.m_zipenv.m_verbosity >= 7)
/*     */     {
/* 733 */       report(7, new Object[] { "findClass(\"", name, "\")" });
/*     */     }
/* 735 */     Class cl = findLoadedClass(name);
/* 736 */     if (null != cl)
/*     */     {
/* 738 */       if (this.m_zipenv.m_verbosity >= 6)
/*     */       {
/* 740 */         report(6, new Object[] { name, " already loaded." });
/*     */       }
/* 742 */       return cl;
/*     */     }
/* 744 */     String qualifiedPath = name.replace('.', '/').concat(".class");
/*     */ 
/* 746 */     IdcLoaderElement[] elementPtr = new IdcLoaderElement[1];
/*     */     IdcByteHandler handler;
/*     */     try {
/* 749 */       handler = this.m_classPathElements.lookupByNameAndTrack(qualifiedPath, elementPtr);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 753 */       throw new ClassNotFoundException(name, e);
/*     */     }
/* 755 */     if (null == handler)
/*     */     {
/* 757 */       throw new ClassNotFoundException(name);
/*     */     }
/* 759 */     long size = handler.getSize();
/* 760 */     if (size > 2147483647L)
/*     */     {
/* 762 */       if (this.m_zipenv.m_verbosity >= 3)
/*     */       {
/* 764 */         report(3, new Object[] { "class \"", name, "\" too large" });
/*     */       }
/* 766 */       throw new ClassNotFoundException(name);
/*     */     }
/* 768 */     int numBytes = (int)size;
/* 769 */     byte[] bytes = new byte[numBytes];
/*     */     try
/*     */     {
/* 772 */       int bytesRead = handler.readFrom(0L, bytes, 0, numBytes);
/* 773 */       if (bytesRead < numBytes)
/*     */       {
/* 775 */         throw new IdcByteHandlerException('r', bytesRead, numBytes);
/*     */       }
/* 777 */       if (this.m_zipenv.m_verbosity >= 7)
/*     */       {
/* 779 */         report(7, new Object[] { "loaded ", Integer.valueOf(numBytes), " bytes for class ", name });
/*     */       }
/*     */     }
/*     */     catch (IdcByteHandlerException e)
/*     */     {
/* 784 */       if (this.m_zipenv.m_verbosity >= 3)
/*     */       {
/* 786 */         report(3, new Object[] { "unable to read class \"", name, "\"" });
/*     */       }
/* 788 */       throw new ClassNotFoundException(name, e);
/*     */     }
/* 790 */     IdcClassInfo info = new IdcClassInfo(elementPtr[0], name);
/* 791 */     info.m_classBytes = bytes;
/* 792 */     this.m_classInfos.put(name, info);
/*     */ 
/* 794 */     cl = defineClass(info);
/* 795 */     if (null == cl)
/*     */     {
/* 797 */       throw new ClassNotFoundException(name);
/*     */     }
/* 799 */     info.m_class = cl;
/* 800 */     info.init();
/* 801 */     if (this.m_zipenv.m_verbosity >= 6)
/*     */     {
/* 803 */       report(6, new Object[] { "loaded ", info.toString() });
/*     */     }
/* 805 */     return cl;
/*     */   }
/*     */ 
/*     */   public URL getResource(String name)
/*     */   {
/* 818 */     if (this.m_zipenv.m_verbosity >= 7)
/*     */     {
/* 820 */       report(7, new Object[] { "getResource(\"", name, "\")" });
/*     */     }
/* 822 */     URL url = findResource(name);
/* 823 */     if (url == null)
/*     */     {
/* 825 */       url = super.getResource(name);
/*     */     }
/* 827 */     return url;
/*     */   }
/*     */ 
/*     */   public InputStream getResourceAsStream(String name)
/*     */   {
/* 840 */     if (this.m_zipenv.m_verbosity >= 7)
/*     */     {
/* 842 */       report(7, new Object[] { "getResourceAsStream(\"", name, "\")" });
/*     */     }
/*     */     try
/*     */     {
/* 846 */       IdcByteHandler handler = this.m_classPathElements.lookupByName(name);
/* 847 */       if (null == handler)
/*     */       {
/* 849 */         if (this.m_zipenv.m_verbosity >= 7)
/*     */         {
/* 851 */           report(7, new Object[] { name, " not found." });
/*     */         }
/* 853 */         return super.getResourceAsStream(name);
/*     */       }
/* 855 */       if (this.m_zipenv.m_verbosity >= 7)
/*     */       {
/* 857 */         report(7, new Object[] { name, " found: ", Long.valueOf(handler.getSize()), " bytes." });
/*     */       }
/* 859 */       return new IdcByteHandlerInputStream(handler);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 863 */       if (this.m_zipenv.m_verbosity >= 5)
/*     */       {
/* 865 */         report(5, new Object[] { e });
/*     */       }
/*     */     }
/* 868 */     return null;
/*     */   }
/*     */ 
/*     */   public URL findResource(String name)
/*     */   {
/* 880 */     if (this.m_zipenv.m_verbosity >= 7)
/*     */     {
/* 882 */       report(7, new Object[] { "findResource(\"", name, "\")" });
/*     */     }
/*     */     try
/*     */     {
/* 886 */       return this.m_classPathElements.lookupURLByName(name);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 890 */       report(5, new Object[] { "resource not found: ", e });
/* 891 */     }return null;
/*     */   }
/*     */ 
/*     */   public void report(int level, Object[] args)
/*     */   {
/* 906 */     if (null == this.m_zipenv.m_trace)
/*     */     {
/* 908 */       String name = new StringBuilder().append(super.getClass().getSimpleName()).append(": ").toString();
/* 909 */       SimpleTracingCallback simple = new SimpleTracingCallback(name);
/* 910 */       this.m_zipenv.m_trace = simple;
/*     */     }
/* 912 */     if (level > this.m_zipenv.m_verbosity)
/*     */     {
/* 915 */       return;
/*     */     }
/* 917 */     String name = this.m_name;
/* 918 */     if (name != null)
/*     */     {
/* 920 */       int numArgs = args.length;
/* 921 */       Object[] newArgs = new Object[numArgs + 3];
/* 922 */       System.arraycopy(args, 0, newArgs, 3, numArgs);
/* 923 */       args = newArgs;
/* 924 */       args[0] = "[";
/* 925 */       args[1] = name;
/* 926 */       args[2] = "] ";
/*     */     }
/* 928 */     this.m_zipenv.m_trace.report(level, args);
/*     */   }
/*     */ 
/*     */   public IdcZipEnvironment getZipEnvironment()
/*     */   {
/* 935 */     return this.m_zipenv;
/*     */   }
/*     */ 
/*     */   public Map<String, IdcZipFile> getZipfiles()
/*     */   {
/* 940 */     return (this.m_shouldUseStaticMapForZipfiles) ? s_zipfiles : this.m_zipfiles;
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 949 */     this.m_zipenv.m_trace.report(5, new Object[] { "IdcClassLoader.clear() called" });
/*     */ 
/* 954 */     if (this.m_classInfos != null)
/*     */     {
/* 956 */       Set classInfoKeys = this.m_classInfos.keySet();
/* 957 */       for (String key : classInfoKeys)
/*     */       {
/* 959 */         IdcClassInfo info = (IdcClassInfo)this.m_classInfos.get(key);
/* 960 */         if (info != null)
/*     */         {
/* 962 */           info.clear();
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 967 */     if (this.m_loadedClasses == null)
/*     */       return;
/* 969 */     this.m_loadedClasses.clear();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 976 */     StringBuilder sb = new StringBuilder("IdcClassLoader");
/* 977 */     String name = this.m_name;
/* 978 */     if (name != null)
/*     */     {
/* 980 */       sb.append(" [");
/* 981 */       sb.append(name);
/* 982 */       sb.append(']');
/*     */     }
/* 984 */     if (this.m_classPathElements != null)
/*     */     {
/* 986 */       sb.append(": ");
/* 987 */       sb.append(this.m_classPathElements.toString());
/*     */     }
/* 989 */     return sb.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 995 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99677 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.loader.IdcClassLoader
 * JD-Core Version:    0.5.4
 */